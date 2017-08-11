/**
 *  emxLifecycleBase.java
 *
 * (c) Dassault Systemes, 1993 - 2016. All rights reserved.
 * This program contains proprietary and trade secret information of
 * ENOVIA MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.Group;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.db.Role;
import matrix.db.State;
import matrix.db.StateList;
import matrix.db.AttributeList;
import matrix.db.Attribute;
import matrix.db.AttributeType;
import matrix.db.Vault;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.InboxTask;
import com.matrixone.apps.common.Lifecycle;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.lifecycle.LifeCyclePolicyDetails;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * This JPO includes the code related to the Lifecycle Mass Approval functionality
 */
public class emxLifecycleBase_mxJPO
{
    private static final String SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE ="attribute["+DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE+"]";
    private static final String SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE ="attribute["+DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE+"]";
    private static final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute[" + DomainObject.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";
    private static final String SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE = "attribute[" + DomainObject.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE + "]";
    private static final String SELECT_ATTRIBUTE_TITLE = "attribute["+DomainObject.ATTRIBUTE_TITLE+"]";
    private static final String SELECT_ATTRIBUTE_COMMENTS = "attribute["+DomainObject.ATTRIBUTE_COMMENTS+"]";
    private static final String SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS = "attribute["+DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS+"]";
    private static final String SELECT_ATTRIBUTE_ROUTE_ACTION = "attribute["+DomainObject.ATTRIBUTE_ROUTE_ACTION +"]";
    private static final String SELECT_ATTRIBUTE_APPROVAL_STATUS = "attribute[" + DomainObject.ATTRIBUTE_APPROVAL_STATUS + "]";
    private static final String SELECT_ATTRIBUTE_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
    private static final String SELECT_ATTRIBUTE_ROUTE_TASK_USER = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_TASK_USER + "]";
    private static final String SELECT_ATTRIBUTE_ROUTE_SEQUENCE = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]";

    private static final String SELECT_ROUTE_TASK_ASSIGNEE_ID = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.id";
    private static final String SELECT_ROUTE_TASK_ASSIGNEE_TYPE = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.type";
    private static final String SELECT_ROUTE_TASK_ASSIGNEE_NAME = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.name";

    private static final String SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_NODE_ID);
    private static final String SELECT_REL_ATTRIBUTE_APPROVAL_STATUS = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_APPROVAL_STATUS);
    private static final String SELECT_REL_ATTRIBUTE_TITLE = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_TITLE);
    private static final String SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS);
    private static final String SELECT_REL_ATTRIBUTE_COMMENTS = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_COMMENTS);
    private static final String SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
    private static final String SELECT_REL_ATTRIBUTE_ACTUAL_COMPLETION_DATE = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE);
    private static final String SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
    private static final String SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY);
    private static final String SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_BASE_STATE);


    private static final String SELECT_ATTRIBUTE_ROUTE_STATUS = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_STATUS + "]";
    private static final String INFO_TYPE_ACTIVATED_TASK = "activatedTask";
    private static final String INFO_TYPE_DEFINED_TASK = "definedTask";
    private static final String INFO_TYPE_SIGNATURE = "signature";
    private static final String INFO_TYPE_EMPTY_ROW = "emptyRow";

    // added for IR - 043921V6R2011
    public static final String  SELECT_OWNING_ORG_ID          =  "from[" + DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE 
                                                     + "].to.to[" + DomainConstants.RELATIONSHIP_OWNING_ORGANIZATION + "].from.id";
    final String SELECT_ATTRIBUTE_ROUTE_TEMPLATE_TASK_EDIT_SETTING = "from[" +DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"]" + ".to.attribute[" + DomainConstants.ATTRIBUTE_TASKEDIT_SETTING + "]";;




    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @grade 0
     */
    public emxLifecycleBase_mxJPO (Context context, String[] args) throws Exception
    {

    }

    /**
     * Method is used to populate the data for Tasks/Signature table in advance lifecycle page
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args The packed argument for JPO, containing the program map.
     *             This program map will have request parameter information, objectId and information about the UI table object.
     * @return MapList of data
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getCurrentTaskSignaturesOnObject(Context context, String[] args) throws Exception {
        try {
            // To hold the table data
            MapList mlTableData = new MapList();

            // Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);

            // Get object id
            String strObjectId = (String)programMap.get("objectId");

            DomainObject dmoObject = null;
            DomainObject dmoRoute = null;

            String strCurrentObjectId = null;
            String strObjectType = null;
            String strObjectStateName = null;
            String strObjectPolicyName = null;
            String strObjectSymbolicStateName = null;
            String strSymbolicPolicyName = null;
            String strRelPattern = null;
            String strTypePattern = null;
            String strObjectWhere = null;
            String strRelWhere = null;
            String strRouteId = null;
            String strRouteNodeId = null;
            String strRouteStatus = null;
            StringList slRelSelect = null;
            StringList slBusSelect = null;
            short nRecurseToLevel = (short)1;
            final boolean GET_TO = true;
            final boolean GET_FROM = true;
            boolean isActivated = false;
            Map mapRouteInfo = null;
            Map mapObjectInfo = null;
            Map mapTemp = null;
            Map mapTemp2 = null;
            Map mapConfigurableParameters = null;
            MapList mlDefinedTasksOnRoute = null;
            MapList mlActivatedTasksOnRoute = null;
            MapList mlTemp = null;
            MapList mlRoutes = null;

            // For i18nNow string formation
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            final String STRING_PENDING = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Pending");
            final String STRING_AWAITING_APPROVAL = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.AwaitingApproval");

            // Create maplist to hold related objects information
            MapList mlAllObjectsInfo = new MapList();

            // Find the configurable parameters
            dmoObject = new DomainObject(strObjectId);
            strObjectType = dmoObject.getInfo(context, DomainObject.SELECT_TYPE);
            Lifecycle lifecycle = new Lifecycle();
            mapConfigurableParameters = lifecycle.getConfigurableParameters(context, strObjectType);

            // If configurable parameters are passed, expand the object to get all those related objects' id
            if (mapConfigurableParameters != null) {
                    strRelPattern = (String)mapConfigurableParameters.get("Relationships");
                    strTypePattern = "*";
                    slRelSelect = new StringList();
                    nRecurseToLevel = (short)1;
                    slBusSelect = new StringList();
                    slBusSelect.add(DomainObject.SELECT_ID);
                    slBusSelect.add(DomainObject.SELECT_NAME);
                    slBusSelect.add(DomainObject.SELECT_TYPE);
                    slBusSelect.add(DomainObject.SELECT_CURRENT);
                    slBusSelect.add(DomainObject.SELECT_POLICY);
                    strRelWhere = null;
                    strObjectWhere = null;

                    // Decide direction of expand
                    boolean isGetTo = false;
                    boolean isGetFrom = false;
                    if ("to".equals((String)mapConfigurableParameters.get("Direction"))) {
                        isGetTo = false;
                        isGetFrom = true;
                    }
                    else if ("from".equals((String)mapConfigurableParameters.get("Direction"))) {
                        isGetTo = true;
                        isGetFrom = false;
                    }
                    else if ("both".equals((String)mapConfigurableParameters.get("Direction"))) {
                        isGetTo = true;
                        isGetFrom = true;
                    }

                    MapList mlRelatedObjects = dmoObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, isGetTo, isGetFrom, nRecurseToLevel, strObjectWhere, strRelWhere);
                    // Set marker inside the maps to identify this is related object
                    Map mapRelatedObjectInfo = null;
                    for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();) {
                        mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
                        mapRelatedObjectInfo.put("isRelatedObject", "true");
                    }

                    mlAllObjectsInfo.addAll(mlRelatedObjects);

            }

            // Get context object information and add it to the related objects map marking it is not related object
            dmoObject = new DomainObject(strObjectId);
            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_NAME);
            slBusSelect.add(DomainObject.SELECT_TYPE);
            slBusSelect.add(DomainObject.SELECT_CURRENT);
            slBusSelect.add(DomainObject.SELECT_POLICY);
            mapObjectInfo = dmoObject.getInfo(context, slBusSelect);
            mapObjectInfo.put("isRelatedObject", "false");
            mapObjectInfo.put(DomainObject.SELECT_ID, strObjectId);

            // Add this object into the all objects list for processing hence forward
            mlAllObjectsInfo.add(mapObjectInfo);
            mapObjectInfo = null;

            // Process each object so found
            // For each object find the route tasks for the state-based routes for current state of the object
            for (Iterator itrAllObjects = mlAllObjectsInfo.iterator(); itrAllObjects.hasNext();) {
                mapObjectInfo = (Map) itrAllObjects.next();

                // Get object information
                strCurrentObjectId = (String)mapObjectInfo.get(DomainObject.SELECT_ID);
                strObjectStateName = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);
                strObjectPolicyName = (String)mapObjectInfo.get(DomainObject.SELECT_POLICY);

                // Get symbolic names of object state and policy
                strObjectSymbolicStateName = FrameworkUtil.reverseLookupStateName(context, strObjectPolicyName, strObjectStateName);
                strSymbolicPolicyName = FrameworkUtil.getAliasForAdmin(context, "Policy", strObjectPolicyName, false);

                dmoObject = new DomainObject(strCurrentObjectId);

                // Get state based route objects for this object
                strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
                strTypePattern = DomainObject.TYPE_ROUTE;
                slRelSelect = new StringList();
                nRecurseToLevel = (short)1;
                slBusSelect = new StringList();
                slBusSelect.add(DomainObject.SELECT_ID);
                slBusSelect.add(DomainObject.SELECT_NAME);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_STATUS);
                strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]=='" + strSymbolicPolicyName + "' && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]=='" + strObjectSymbolicStateName + "'";

                mlRoutes = dmoObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                //Find all the tasks on each route
                for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
                    // Find the route object
                    mapRouteInfo = (Map) itrRoutes.next();
                    strRouteId = (String)mapRouteInfo.get(DomainObject.SELECT_ID);
                    strRouteStatus = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS);
                    dmoRoute = new DomainObject(strRouteId);

                    // Find the tasks defined on the route object, using Route Node relationship
                    strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
                    strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;
                    slBusSelect = new StringList();
                    slBusSelect.add(DomainObject.SELECT_ID);
                    slBusSelect.add(DomainObject.SELECT_TYPE);
                    slBusSelect.add(DomainObject.SELECT_NAME);
                    slRelSelect = new StringList();
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_TITLE);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_APPROVAL_STATUS);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_COMMENTS);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER);
                    nRecurseToLevel = (short)1;
                    strObjectWhere = "";
                    strRelWhere = "";
                    mlDefinedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                    // Find the tasks connected to route object, using Route Task relationship
                    strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
                    strTypePattern = DomainObject.TYPE_INBOX_TASK;
                    slBusSelect = new StringList();
                    slBusSelect.add(DomainObject.SELECT_ID);
                    slBusSelect.add(DomainObject.SELECT_NAME);
                    slBusSelect.add(DomainObject.SELECT_TYPE);
                    slBusSelect.add(DomainObject.SELECT_OWNER);
                    slBusSelect.add(DomainObject.SELECT_CURRENT);
                    slBusSelect.add(SELECT_ATTRIBUTE_TITLE);
                    slBusSelect.add(SELECT_ATTRIBUTE_APPROVAL_STATUS);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                    slBusSelect.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    slBusSelect.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                    slBusSelect.add(SELECT_ATTRIBUTE_COMMENTS);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_ID); // To find if it person is connected
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_TYPE);
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_NAME);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                    slRelSelect = new StringList();
                    nRecurseToLevel = (short)1;
                    strObjectWhere = "";
                    strRelWhere = "";
                    mlActivatedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, GET_TO, !GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                    // Filter the partial tasks created due to Resume Process
                    mlTemp = new MapList();
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp = (Map) itrActiveTasks.next();

                        // If person is connected then it is not partial task
                        if (mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID) != null) {
                            mlTemp.add(mapTemp);
                        }
                    }
                    mlActivatedTasksOnRoute = mlTemp;

                    // Filter out the defined tasks which are already active
                    mlTemp = new MapList();
                    for (Iterator itrDefinedTasks = mlDefinedTasksOnRoute.iterator(); itrDefinedTasks.hasNext();) {
                        mapTemp = (Map) itrDefinedTasks.next();

                        // Find the id of this relationship
                        strRouteNodeId = (String)mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID);

                        // Check if the task with this id is already activated
                        isActivated = false;
                        for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                            mapTemp2 = (Map) itrActiveTasks.next();

                            if (strRouteNodeId != null && strRouteNodeId.equals((String)mapTemp2.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID))) {
                                isActivated = true;
                                break;
                            }
                        }

                        if (!isActivated) {
                            mlTemp.add(mapTemp);
                        }
                    }
                    mlDefinedTasksOnRoute = mlTemp;

                    // Form the final task list of the route object, we want the information to be present in final list with the same key in map

                    // Add all the active tasks on to the final list
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp = (Map) itrActiveTasks.next();

                        mapTemp2 = new HashMap();
                        mapTemp2.put("name", mapTemp.get(DomainObject.SELECT_NAME));
                        mapTemp2.put("title", mapTemp.get(SELECT_ATTRIBUTE_TITLE));
                        mapTemp2.put("approvalStatus", mapTemp.get(SELECT_ATTRIBUTE_APPROVAL_STATUS));
                        mapTemp2.put("routeNodeId", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID));
                        mapTemp2.put("taskId", mapTemp.get(DomainObject.SELECT_ID));
                        mapTemp2.put("routeId", strRouteId);
                        mapTemp2.put("routeStatus", strRouteStatus);
                        mapTemp2.put("infoType", INFO_TYPE_ACTIVATED_TASK);
                        mapTemp2.put("currentState", mapTemp.get(DomainObject.SELECT_CURRENT));
                        mapTemp2.put("dueDate", mapTemp.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE));
                        mapTemp2.put("completionDate", mapTemp.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE));
                        mapTemp2.put("comments", mapTemp.get(SELECT_ATTRIBUTE_COMMENTS));
                        mapTemp2.put("instructions", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS));
                        mapTemp2.put("assigneeId", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID));
                        mapTemp2.put("assigneeType", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_TYPE));
                        mapTemp2.put("assigneeName", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_NAME));
                        mapTemp2.put("routeTaskUser", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_TASK_USER));
                        mapTemp2.put("owner", mapTemp.get(DomainObject.SELECT_OWNER));

                        // Set parent object information
                        mapTemp2.put("parentObjectId", mapObjectInfo.get(DomainObject.SELECT_ID));
                        mapTemp2.put("parentObjectName", mapObjectInfo.get(DomainObject.SELECT_NAME));
                        mapTemp2.put("parentObjectType", mapObjectInfo.get(DomainObject.SELECT_TYPE));
                        mapTemp2.put("parentObjectState", mapObjectInfo.get(DomainObject.SELECT_CURRENT));
                        mapTemp2.put("parentObjectPolicy", mapObjectInfo.get(DomainObject.SELECT_POLICY));
                        mapTemp2.put("isFromRelatedObject", mapObjectInfo.get("isRelatedObject"));
                        mapTemp2.put("relationship", mapObjectInfo.get("relationship"));

                        // Add the tasks due to route objects in the table data list
                        mlTableData.add(mapTemp2);
                    }

                    // Add all the defined tasks on to the final list
                    for (Iterator itrDefinedTasks = mlDefinedTasksOnRoute.iterator(); itrDefinedTasks.hasNext();) {
                        mapTemp = (Map) itrDefinedTasks.next();

                        mapTemp2 = new HashMap();
                        mapTemp2.put("name", mapTemp.get(SELECT_REL_ATTRIBUTE_TITLE));
                        mapTemp2.put("title", mapTemp.get(SELECT_REL_ATTRIBUTE_TITLE));
                        mapTemp2.put("approvalStatus", mapTemp.get(SELECT_REL_ATTRIBUTE_APPROVAL_STATUS));
                        mapTemp2.put("routeNodeId", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID));
                        mapTemp2.put("routeId", strRouteId);
                        mapTemp2.put("routeStatus", strRouteStatus);
                        mapTemp2.put("infoType", INFO_TYPE_DEFINED_TASK);
                        mapTemp2.put("dueDate", mapTemp.get(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE));
                        mapTemp2.put("comments", mapTemp.get(SELECT_REL_ATTRIBUTE_COMMENTS));
                        mapTemp2.put("instructions", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS));
                        mapTemp2.put("assigneeId", mapTemp.get(DomainObject.SELECT_ID));
                        mapTemp2.put("assigneeType", mapTemp.get(DomainObject.SELECT_TYPE));
                        mapTemp2.put("assigneeName", mapTemp.get(DomainObject.SELECT_NAME));
                        mapTemp2.put("routeTaskUser", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER));

                        // Set parent object information
                        mapTemp2.put("parentObjectId", mapObjectInfo.get(DomainObject.SELECT_ID));
                        mapTemp2.put("parentObjectName", mapObjectInfo.get(DomainObject.SELECT_NAME));
                        mapTemp2.put("parentObjectType", mapObjectInfo.get(DomainObject.SELECT_TYPE));
                        mapTemp2.put("parentObjectState", mapObjectInfo.get(DomainObject.SELECT_CURRENT));
                        mapTemp2.put("parentObjectPolicy", mapObjectInfo.get(DomainObject.SELECT_POLICY));
                        mapTemp2.put("isFromRelatedObject", mapObjectInfo.get("isRelatedObject"));
                        mapTemp2.put("relationship", mapObjectInfo.get("relationship"));

                        // Add the tasks due to route objects in the table data list
                        mlTableData.add(mapTemp2);
                    }
                }// for each route object

                //
                // Find signatures' details
                //

                // Find the states of the object
                StateList stateList = LifeCyclePolicyDetails.getStateList(context, dmoObject, strObjectPolicyName);
                
                State fromState = null;
                State toState = null;
                State state = null;

                // Find the current and the next state's State object
                for (Iterator itrStates = stateList.iterator(); itrStates.hasNext();) {
                    // Get a state
                    state = (State) itrStates.next();

                    // If form state is not yet found then check if the current state name is this state
                    if (fromState == null) {
                        if (state.getName().equals(strObjectStateName) ) {
                            fromState = state;
                        }
                    }
                }

                // If we get the from and to state then only there are signatures to be found out
                if (fromState != null) {
                    MapList mlSignatureDetails = null;
                    Map mapSignatureInfo = null;
                    Map mapSignatureDetails = null;
                    String strSignatureName = null;
                    String strResult = null;
                    StringList slSignatureApprovers = null;
                    Vector vecAllBranches = new Vector();

                    /////////////////////////////////////////////////////////////////////////////
                    //
                    // Detect if there are branches
                    //
                    for (Iterator itrAllStates = stateList.iterator(); itrAllStates.hasNext();) {
                        toState = (State)itrAllStates.next();

                        // Skip if this is from state
                        if (toState.getName().equals(fromState.getName())) {
                            continue;
                        }

                        mlSignatureDetails = dmoObject.getSignaturesDetails(context, fromState, toState);

                        if (mlSignatureDetails != null && mlSignatureDetails.size() > 0) {
                            //
                            // We need to find out the due date for the signatures.
                            //
                            for (Iterator itrSignatureDetails = mlSignatureDetails.iterator(); itrSignatureDetails.hasNext(); )
                            {
                                mapSignatureDetails = (Map)itrSignatureDetails.next();
                                // The due date for the signature will be the scheduled date for the next state
                                mapSignatureDetails.put("dueDate", toState.getScheduledDate());
                            }

                            vecAllBranches.add(mlSignatureDetails);
                        }
                    }//for

                    //
                    // If there are multiple branches then show the signtures for all of the branches
                    //
                    mlSignatureDetails = new MapList();

                    //
                    // When there are multiple branches, only show the signatures which are signed,
                    // so filter out the signatures which are signed
                    //
                    for (Iterator itrAllBranches = vecAllBranches.iterator(); itrAllBranches.hasNext();) {
                        MapList mlCurrentBranch = (MapList) itrAllBranches.next();
                        for (Iterator itrCurrentBranchSignatures = mlCurrentBranch.iterator(); itrCurrentBranchSignatures.hasNext();) {
                            Map mapCurrentSignature = (Map) itrCurrentBranchSignatures.next();
                            mlSignatureDetails.add(mapCurrentSignature);
                        }
                    }
                    //
                    //
                    ///////////////////////////////////////////////////////////////////////////////

                    String strMQL = null;
                    String strSelectStateTemplate = "state[" + strObjectStateName + "].signature[${SIGNATURE_NAME}]";
                    StringBuffer strMQLFindApproversTemplate = new StringBuffer(64);
                    String strHasApprove = null;
                    String strHasReject = null;
                    String strHasIgnore = null;
                    String strApproveLink = null;

                    // Form the hyperlink template to approve the signature
                    String strMQLApproveLinkTemplate = "<a href=\"javascript:emxTableColumnLinkClick('emxLifecycleApprovalDialogFS.jsp?objectId=" + strCurrentObjectId + "&signatureName=${SIGNATURE_NAME}&toState=" + toState.getName() + "&fromState=" + fromState.getName() + "&isInCurrentState=true&sHasApprove=${HAS_APPROVE}&sHasReject=${HAS_REJECT}&sHasIgnore=${HAS_IGNORE}','500','400','false','popup','')\"><img border='0' src='../common/images/iconActionApprove.png' />" + STRING_AWAITING_APPROVAL + "</a>";

                    // Form the MQL template to find the approver for signature
                    strMQLFindApproversTemplate.append("print policy \"");
                    strMQLFindApproversTemplate.append(strObjectPolicyName);
                    strMQLFindApproversTemplate.append("\" select ");
                    strMQLFindApproversTemplate.append(strSelectStateTemplate).append(".approve ");
                    strMQLFindApproversTemplate.append(strSelectStateTemplate).append(".reject ");
                    strMQLFindApproversTemplate.append(strSelectStateTemplate).append(".ignore dump ,");
                    
                    
                    // Do processing for each signature details so found
                    for (Iterator itrSignatureDetails = mlSignatureDetails.iterator(); itrSignatureDetails.hasNext();) {
                        mapSignatureInfo = (Map) itrSignatureDetails.next();
                        
                        // Add information in map
                        mapSignatureInfo.put("infoType", INFO_TYPE_SIGNATURE);

                        // Get if user has access
                        strHasApprove = (String)mapSignatureInfo.get("hasapprove");
                        strHasReject = (String)mapSignatureInfo.get("hasreject");
                        strHasIgnore = (String)mapSignatureInfo.get("hasignore");
                        
                        // If this singnature is not signed then find out if this user has access to approve it
                        if (!"true".equalsIgnoreCase((String)mapSignatureInfo.get("signed"))) {
                            strSignatureName = (String)mapSignatureInfo.get("name");

                            // Find out the role/group/person assigned for Approve/Reject/Ignore action for the signature
                            strMQL = FrameworkUtil.findAndReplace(strMQLFindApproversTemplate.toString(), "${SIGNATURE_NAME}", strSignatureName);
                            strResult = MqlUtil.mqlCommand(context, strMQL, true);
                            slSignatureApprovers = FrameworkUtil.split(strResult, ",");

                            // If user has access to work on the signature then show the hyperlink for the signature
                            if ("TRUE".equals(strHasApprove) || "TRUE".equals(strHasReject) || "TRUE".equals(strHasIgnore)) {
                                strApproveLink = FrameworkUtil.findAndReplace(strMQLApproveLinkTemplate, "${SIGNATURE_NAME}", strSignatureName);
                                strApproveLink = FrameworkUtil.findAndReplace(strApproveLink, "${HAS_APPROVE}", strHasApprove);
                                strApproveLink = FrameworkUtil.findAndReplace(strApproveLink, "${HAS_REJECT}", strHasReject);
                                strApproveLink = FrameworkUtil.findAndReplace(strApproveLink, "${HAS_IGNORE}", strHasIgnore);
                                
                                mapSignatureInfo.put("approvalStatus", strApproveLink);                                
                            }
                            else {
                                mapSignatureInfo.put("approvalStatus", STRING_PENDING);
                            }
 
                            mapSignatureInfo.put("approver", strResult);
                        }
                        else {
                            mapSignatureInfo.put("approver", mapSignatureInfo.get("signer"));
                        }

                        // Set parent object information
                        mapSignatureInfo.put("parentObjectId", mapObjectInfo.get(DomainObject.SELECT_ID));
                        mapSignatureInfo.put("parentObjectName", mapObjectInfo.get(DomainObject.SELECT_NAME));
                        mapSignatureInfo.put("parentObjectType", mapObjectInfo.get(DomainObject.SELECT_TYPE));
                        mapSignatureInfo.put("parentObjectState", mapObjectInfo.get(DomainObject.SELECT_CURRENT));
                        mapSignatureInfo.put("parentObjectPolicy", mapObjectInfo.get(DomainObject.SELECT_POLICY));
                        mapSignatureInfo.put("isFromRelatedObject", mapObjectInfo.get("isRelatedObject"));
                        mapSignatureInfo.put("relationship", mapObjectInfo.get("relationship"));

                        // Add signature information into table data maplist
                        mlTableData.add(mapSignatureInfo);
                    }
                }//if for signatures

            }// for for all objects

            return mlTableData;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
        finally {
        }

    }
    
    private MapList getCurrentAssignedTaskSignaturesOnObject(Context context, String objectId) throws Exception {

        try {
            // To hold the table data
            MapList mlTableData = new MapList();

            DomainObject dmoObject = null;
            DomainObject dmoRoute = null;

            String strCurrentObjectId = null;
            String strObjectType = null;
            String strObjectStateName = null;
            String strObjectPolicyName = null;
            String strObjectSymbolicStateName = null;
            String strSymbolicPolicyName = null;
            String strRelPattern = null;
            String strTypePattern = null;
            String strObjectWhere = null;
            String strRelWhere = null;
            String strRouteId = null;
            StringList slRelSelect = null;
            StringList slBusSelect = null;
            short nRecurseToLevel = (short)1;
            final boolean GET_TO = true;
            final boolean GET_FROM = true;
            Map mapRouteInfo = null;
            Map mapObjectInfo = null;
            Map mapTemp = null;
            Map mapConfigurableParameters = null;
            MapList mlActivatedTasksOnRoute = null;
            MapList mlTemp = null;
            MapList mlRoutes = null;
            String strContextUser = context.getUser();
            String strRouteTaskUser = null;
            //String strRouteCurrentRouteNode = null;

            final String POLICY_INBOX_TASK_STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");
            final String POLICY_ROUTE_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_ROUTE, "state_Complete");
            final String SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_CurrentRouteNode") + "]";

            // find the configurable parameters
            dmoObject = new DomainObject(objectId);
            strObjectType = dmoObject.getInfo(context, DomainObject.SELECT_TYPE);
            Lifecycle lifecycle = new Lifecycle();
            mapConfigurableParameters = lifecycle.getConfigurableParameters(context, strObjectType);

            // Create maplist to hold related objects information
            MapList mlAllObjectsInfo = new MapList();

            // If configurable parameters are passed, expand the object to get all those related objects' id
            if (mapConfigurableParameters != null) {
                strRelPattern = (String)mapConfigurableParameters.get("Relationships");
                strTypePattern = "*";
                slRelSelect = new StringList();
                nRecurseToLevel = (short)1;
                slBusSelect = new StringList();
                slBusSelect.add(DomainObject.SELECT_ID);
                slBusSelect.add(DomainObject.SELECT_NAME);
                slBusSelect.add(DomainObject.SELECT_CURRENT);
                slBusSelect.add(DomainObject.SELECT_POLICY);
                strRelWhere = null;
                strObjectWhere = null;

                // Decide direction of expand
                boolean isGetTo = false;
                boolean isGetFrom = false;
                if ("to".equals((String)mapConfigurableParameters.get("Direction"))) {
                    isGetTo = false;
                    isGetFrom = true;
                }
                else if ("from".equals((String)mapConfigurableParameters.get("Direction"))) {
                    isGetTo = true;
                    isGetFrom = false;
                }
                else if ("both".equals((String)mapConfigurableParameters.get("Direction"))) {
                    isGetTo = true;
                    isGetFrom = true;
                }

                MapList mlRelatedObjects = dmoObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, isGetTo, isGetFrom, nRecurseToLevel, strObjectWhere, strRelWhere);

                // Set marker inside the maps to identify this is related object
                Map mapRelatedObjectInfo = null;
                for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();) {
                    mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
                    mapRelatedObjectInfo.put("isRelatedObject", "true");
                }

                mlAllObjectsInfo.addAll(mlRelatedObjects);
            }

            // Get context object information and add it to the related objects map marking it is not related object
            dmoObject = new DomainObject(objectId);
            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_NAME);
            slBusSelect.add(DomainObject.SELECT_CURRENT);
            slBusSelect.add(DomainObject.SELECT_POLICY);
            mapObjectInfo = dmoObject.getInfo(context, slBusSelect);
            mapObjectInfo.put("isRelatedObject", "false");
            mapObjectInfo.put(DomainObject.SELECT_ID, objectId);

            // Add this object into the all objects list for processing hence forward
            mlAllObjectsInfo.add(mapObjectInfo);
            mapObjectInfo = null;
            //--------------------------------------------------------------------------------------------------------------

            // Process each object so found
            // For each object find the route tasks for the state-based routes for current state of the object
            for (Iterator itrAllObjects = mlAllObjectsInfo.iterator(); itrAllObjects.hasNext();) {
                mapObjectInfo = (Map) itrAllObjects.next();

                // Get object information
                strCurrentObjectId = (String)mapObjectInfo.get(DomainObject.SELECT_ID);
                strObjectStateName = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);
                strObjectPolicyName = (String)mapObjectInfo.get(DomainObject.SELECT_POLICY);

                // Get symbolic names of object state and policy
                strObjectSymbolicStateName = FrameworkUtil.reverseLookupStateName(context, strObjectPolicyName, strObjectStateName);
                strSymbolicPolicyName = FrameworkUtil.getAliasForAdmin(context, "Policy", strObjectPolicyName, false);

                dmoObject = new DomainObject(strCurrentObjectId);

                //////////////////////////////////////////////////////////////////////////////////
                // Get state based route objects for this object
                //////////////////////////////////////////////////////////////////////////////////
                strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
                strTypePattern = DomainObject.TYPE_ROUTE;
                slRelSelect = new StringList();
                nRecurseToLevel = (short)1;
                slBusSelect = new StringList();
                slBusSelect.add(DomainObject.SELECT_ID);
                slBusSelect.add(DomainObject.SELECT_NAME);
                slBusSelect.add(DomainObject.SELECT_OWNER);
                slBusSelect.add(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
                strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]=='" + strSymbolicPolicyName + "' && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]=='" + strObjectSymbolicStateName + "'";
                strObjectWhere = "current != '" + POLICY_ROUTE_STATE_COMPLETE + "'";

                mlRoutes = dmoObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);
// IR-043921V6R2011 - Changes START
                for (Iterator itr = mlRoutes.iterator(); itr.hasNext();) {
                    mapRouteInfo = (Map) itr.next();
                    String sOwningOrgId = (String) mapRouteInfo.get( SELECT_OWNING_ORG_ID );
                    if( sOwningOrgId !=null && !"null".equals( sOwningOrgId ) && !"".equals( sOwningOrgId )) {
                        Organization org = (Organization) DomainObject.newInstance( context, sOwningOrgId );
                        StringList busSelects = new StringList(2);
                        busSelects.addElement( DomainConstants.SELECT_ID );
                        busSelects.addElement( DomainConstants.SELECT_NAME );
                        String sWhereClause = "( name == \"" + context.getUser() + "\" )";
                        MapList mlMembers = org.getMemberPersons( context, busSelects, sWhereClause, null );
                        if( mlMembers.isEmpty() ) {
                            itr.remove();
                        }
                    }
                }
// IR-043921V6R2011 - Changes END
                //////////////////////////////////////////////////////////////////////////////////
                //Find all the tasks on each route
                //////////////////////////////////////////////////////////////////////////////////
                for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
                    // Find the route object
                    mapRouteInfo = (Map) itrRoutes.next();

                    strRouteId = (String)mapRouteInfo.get(DomainObject.SELECT_ID);
                    //strRouteName = (String)mapRouteInfo.get(DomainObject.SELECT_NAME);
                    //strRouteOwner = (String)mapRouteInfo.get(DomainObject.SELECT_OWNER);
                    //strRouteCurrentRouteNode = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);

                    dmoRoute = new DomainObject(strRouteId);

                    //////////////////////////////////////////////////////////////////////////////////
                    // Find the tasks defined on the route object, using Route Node relationship
                    //////////////////////////////////////////////////////////////////////////////////
                    strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
                    strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;

                    //////////////////////////////////////////////////////////////////////////////////
                    // Find the Activated tasks connected to route object, using Route Task relationship
                    //////////////////////////////////////////////////////////////////////////////////
                    strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
                    strTypePattern = DomainObject.TYPE_INBOX_TASK;

                    slBusSelect = new StringList();
                    slBusSelect.add(DomainObject.SELECT_NAME);
                    slBusSelect.add(DomainObject.SELECT_ID);
                    slBusSelect.add(DomainObject.SELECT_DESCRIPTION);
                    slBusSelect.add(DomainObject.SELECT_OWNER);
                    slBusSelect.add(DomainObject.SELECT_CURRENT);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_ACTION);
                    slBusSelect.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    slBusSelect.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    slBusSelect.add(SELECT_ATTRIBUTE_TITLE);

                    //slBusSelect.add(SELECT_ATTRIBUTE_APPROVAL_STATUS);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);

                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_ID); // To find if it person is connected
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_TYPE);
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_NAME);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                    slRelSelect = new StringList();
                    nRecurseToLevel = (short)1;
                    strObjectWhere = "current == '" + POLICY_INBOX_TASK_STATE_ASSIGNED + "'";
                    strRelWhere = "";
                    mlActivatedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, GET_TO, !GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);
                    //////////////////////////////////////////////////////////////////////////////////
                    // Filter the partial tasks created due to Resume Process
                    //////////////////////////////////////////////////////////////////////////////////
                    mlTemp = new MapList();
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp = (Map) itrActiveTasks.next();

                        // If person is connected then it is not partial task
                        if (mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID) != null) {
                            mlTemp.add(mapTemp);
                        }
                    }
                    mlActivatedTasksOnRoute = mlTemp;

                    

                    //////////////////////////////////////////////////////////////////////////////////
                    // Add all the active tasks on to the final list (for which user has access)
                    //////////////////////////////////////////////////////////////////////////////////
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp = (Map) itrActiveTasks.next();

                        // Does this user have access to complete this task?
                        if (DomainObject.TYPE_PERSON.equals((String)mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_TYPE))) {
                            // Take only those tasks which are assigned
                            if (!strContextUser.equals((String)mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_NAME))) {
                                continue;
                            }

                            if ("Approve".equals((String)mapTemp.get(SELECT_ATTRIBUTE_ROUTE_ACTION))) {
                                // This is approve kind of task
                                mapTemp.put("ValidApprovalStatusAction", "IT-Approve");
                            }
                            else {
                                // This is non-approve kind of task
                                mapTemp.put("ValidApprovalStatusAction", "IT-Complete");
                            }
                        }
                        else {
                            strRouteTaskUser = (String)mapTemp.get(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                            strRouteTaskUser = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);//Symbolic->Real name

                            // Take only those tasks which can be accepted
                            if (!lifecycle.isRoleOrGroupAssigned(context, strContextUser, strRouteTaskUser)) {
                                continue;
                            }

                            // This task is for accepting
                            mapTemp.put("ValidApprovalStatusAction", "IT-Accept");
                        }

                        mapTemp.put("infoType", INFO_TYPE_ACTIVATED_TASK);
                        mapTemp.put("fromApproveAssignedTasks", "true");

                        mapTemp.put("parentObjectId", strCurrentObjectId);
                        mapTemp.put("parentObjectState", strObjectStateName);

                        // Add the tasks due to route objects in the table data list
                        mlTableData.add(mapTemp);
                    }
                }// for each route object


                ////////////////////////////////////////////////////////////////////////////////////
                // Find signatures' details
                ////////////////////////////////////////////////////////////////////////////////////

                // Find the states of the object
                StateList stateList = dmoObject.getStates(context);
                State fromState = null;
                State toState = null;
                State state = null;

                //////////////////////////////////////////////////////////////////////////////////
                // Find the current and the next state's State object
                //////////////////////////////////////////////////////////////////////////////////
                for (Iterator itrStates = stateList.iterator(); itrStates.hasNext();) {
                    // Get a state
                    state = (State) itrStates.next();

                    // If form state is not yet found then check if the current state name is this state
                    if (fromState == null) {
                        if (state.getName().equals(strObjectStateName) ) {
                            fromState = state;
                        }
                    }
                }

                // If we get the from and to state then only there are signatures to be found out
                if (fromState != null) {
                    MapList mlSignatureDetails = null;
                    Map mapSignatureInfo = null;
                    String strSignatureName = null;
                    String strResult = null;

                    Vector vecAllBranches = new Vector();

                    /////////////////////////////////////////////////////////////////////////////
                    //
                    // Detect if there are branches
                    //
                    for (Iterator itrAllStates = stateList.iterator(); itrAllStates.hasNext();) {
                        toState = (State)itrAllStates.next();

                        // Skip if this is from state
                        if (toState.getName().equals(fromState.getName())) {
                            continue;
                        }

                        mlSignatureDetails = dmoObject.getSignaturesDetails(context, fromState, toState);

                        if (mlSignatureDetails != null && mlSignatureDetails.size() > 0) {
                            vecAllBranches.add(mlSignatureDetails);
                        }
                    }//for

                    //
                    // Show assigned signatures of all branches from this state
                    //
                    mlSignatureDetails = new MapList();

                    //
                    // When there are multiple branches, only show the signatures which are signed,
                    // so filter out the signatures which are signed
                    //
                    for (Iterator itrAllBranches = vecAllBranches.iterator(); itrAllBranches.hasNext();) {
                        MapList mlCurrentBranch = (MapList) itrAllBranches.next();
                        for (Iterator itrCurrentBranchSignatures = mlCurrentBranch.iterator(); itrCurrentBranchSignatures.hasNext();) {
                            Map mapCurrentSignature = (Map) itrCurrentBranchSignatures.next();
                            mlSignatureDetails.add(mapCurrentSignature);
                        }
                    }
                    //
                    //
                    ///////////////////////////////////////////////////////////////////////////////



                    String strMQL = null;
                    String strSelectStateTemplate = "state[" + strObjectStateName + "].signature[${SIGNATURE_NAME}]";
                    StringBuffer strMQLTemplate = new StringBuffer(64);

                    // Form the MQL template to find the approver for signature
                    strMQLTemplate.append("print policy \"");
                    strMQLTemplate.append(strObjectPolicyName);
                    strMQLTemplate.append("\" select ");
                    strMQLTemplate.append(strSelectStateTemplate).append(".approve ");
                    strMQLTemplate.append(strSelectStateTemplate).append(".reject ");
                    strMQLTemplate.append(strSelectStateTemplate).append(".ignore ");
                    strMQLTemplate.append("dump ,");

                    // Do processing for each signature details so found
                    for (Iterator itrSignatureDetails = mlSignatureDetails.iterator(); itrSignatureDetails.hasNext();) {
                        mapSignatureInfo = (Map) itrSignatureDetails.next();

                        // If this singnature is not signed then find out if this user has access to approve it
                        if ("true".equalsIgnoreCase((String)mapSignatureInfo.get("signed"))) {
                            continue;
                        }

                        strSignatureName = (String)mapSignatureInfo.get("name");

                        // Find out the role/group/person assigned for Approve action for the signature
                        strMQL = FrameworkUtil.findAndReplace(strMQLTemplate.toString(), "${SIGNATURE_NAME}", strSignatureName);
                        strResult = MqlUtil.mqlCommand(context, strMQL, true);

                        if (!lifecycle.isRoleOrGroupAssigned(context, context.getUser(), strResult)) {
                            continue;
                        }

                        // The due date for the signature will be the scheduled date for the next state
                        mapSignatureInfo.put("dueDate", toState.getScheduledDate());
                        mapSignatureInfo.put("ValidApprovalStatusAction", "Sign-Approve");
                        mapSignatureInfo.put("approver", strResult);
                        mapSignatureInfo.put("infoType", INFO_TYPE_SIGNATURE);
                        mapSignatureInfo.put("fromApproveAssignedTasks", "true");
                        mapSignatureInfo.put("parentObjectId", strCurrentObjectId);

                        // Add signature information into table data maplist
                        mlTableData.add(mapSignatureInfo);
                    }
                }//if for signatures

            }// for for all objects

            //////////////////////////////////////////////////////////////////////////////////////////////
            // Add a serial number to each map, so that even if the order of map changes,
            // each map has its serial number with it. This number will be used while generating the
            // editable elements in table, and also in final edit process.
            // This portion should be the last part of this method so that each map gets its serial
            // number.
            //////////////////////////////////////////////////////////////////////////////////////////////
            int nSerialNumber = 0;
            for (Iterator objectListItr = mlTableData.iterator(); objectListItr.hasNext(); nSerialNumber++) {
                mapTemp = (Map) objectListItr.next();

                // Add level value else sorting will give problem
                mapTemp.put("serialNumber", String.valueOf(nSerialNumber));
            }
            //////////////////////////////////////////////////////////////////////////////////////////////

            return mlTableData;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Method is used to populate the data for Approve Assigned Tasks table in advance lifecycle page.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args The packed argument for JPO, containing the program map.
     *             This program map will have request parameter information, objectId and information about the UI table object.
     * @return MapList of data
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getCurrentAssignedTaskSignaturesOnObject(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);

        // Get object id
        String strObjectId = (String)programMap.get("objectId");
        return getCurrentAssignedTaskSignaturesOnObject(context, strObjectId);
    }

    /**
     * Method will be used to populate the data for My Task Mass Approvals table from My Tasks functionality.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args The packed argument for JPO, containing the program map.
     *             This program map will have request parameter information, objectId and information about the UI table object.
     * @return MapList of data
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getAllAssignedTasks(Context context, String[] args) throws Exception {

        try {
            final boolean GET_TO = true;
            final boolean GET_FROM = true;

            final String ATTRIBUTE_DUE_DATE = PropertyUtil.getSchemaProperty(context,"attribute_DueDate");
            final String POLICY_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Review");
            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context,"policy", DomainObject.POLICY_INBOX_TASK,"state_Complete");
            final String POLICY_PROJECT_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty( context,"policy", DomainObject.POLICY_PROJECT_TASK, "state_Review");

            final String SELECT_ROUTE_ID = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.id";
            final String SELECT_ROUTE_TYPE = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.type";
            final String SELECT_ROUTE_NAME = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.name";
            final String SELECT_ROUTE_OWNER = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.owner";
            final String SELECT_OBJECT_NAME = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"].from.name";
            final String SELECT_OBJECT_ID = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"].from.id";

            final String SELECT_ATTRIBUTE_DUE_DATE = "attribute[" + ATTRIBUTE_DUE_DATE + "]";


            DomainObject dmoPerson     = PersonUtil.getPersonObject(context);
            StringList selectTypeStmts = new StringList();
            StringList selectRelStmts  = new StringList();
            selectTypeStmts.add(DomainObject.SELECT_NAME);
            selectTypeStmts.add(DomainObject.SELECT_ID);
            selectTypeStmts.add(DomainObject.SELECT_DESCRIPTION);
            selectTypeStmts.add(DomainObject.SELECT_OWNER);
            selectTypeStmts.add(DomainObject.SELECT_CURRENT);
            selectTypeStmts.add(SELECT_ATTRIBUTE_ROUTE_ACTION);
            selectTypeStmts.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
            selectTypeStmts.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
            selectTypeStmts.add(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
            selectTypeStmts.add(SELECT_ATTRIBUTE_TITLE);
            selectTypeStmts.add(SELECT_OBJECT_ID);
            selectTypeStmts.add(SELECT_OBJECT_NAME);
            selectTypeStmts.add(SELECT_ROUTE_ID);
            selectTypeStmts.add(SELECT_ROUTE_NAME);
            selectTypeStmts.add(SELECT_ROUTE_OWNER);
            selectTypeStmts.add(DomainObject.SELECT_TYPE);
            selectTypeStmts.add(SELECT_ROUTE_TYPE);
            selectTypeStmts.add(SELECT_ATTRIBUTE_DUE_DATE);
            selectTypeStmts.add(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
            selectTypeStmts.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
            selectTypeStmts.add(SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);

            Pattern relPattern = new Pattern(DomainObject.RELATIONSHIP_PROJECT_TASK);
            relPattern.addPattern(DomainObject.RELATIONSHIP_ASSIGNED_TASKS);

            Pattern typePattern = new Pattern(DomainObject.TYPE_INBOX_TASK);
            typePattern.addPattern(DomainObject.TYPE_TASK);

            String objectWhere = "(type=='" + DomainObject.TYPE_TASK + "' && current=='" + POLICY_PROJECT_TASK_STATE_REVIEW + "') || " +
                                 "(type=='" + DomainObject.TYPE_INBOX_TASK + "' && current!='" + POLICY_INBOX_TASK_STATE_COMPLETE + "' && current!='" + POLICY_INBOX_TASK_STATE_REVIEW + "')" ;
            String relationshipWhere = null;
            short recurseToLevel = (short)1;
            MapList taskMapList = dmoPerson.getRelatedObjects(context,
                                                              relPattern.getPattern(),
                                                              typePattern.getPattern(),
                                                              selectTypeStmts,
                                                              selectRelStmts,
                                                              GET_TO,
                                                              GET_FROM,
                                                              recurseToLevel,
                                                              objectWhere,
                                                              relationshipWhere);

            // Get the context (top parent) object for WBS Tasks to dispaly appropriate tree for WBS Tasks
            MQLCommand mql = new MQLCommand();
            Map objectMap = null;
            String sTaskType = "";
            String sTaskId = "";
            String sMql = "";
            boolean bResult = false;
            String sResult = "";
            StringTokenizer sResultTkz = null;
            MapList finalTaskMapList = new MapList();
            Iterator objectListItr = taskMapList.iterator();
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                // Add level value else sorting will give problem
                objectMap.put("level","1");

                sTaskType = (String)objectMap.get(DomainObject.SELECT_TYPE);
                // if Task is WBS then add the context (top) object information
                if (DomainObject.TYPE_TASK.equalsIgnoreCase(sTaskType))
                {

                    // Find the project for this task
                    sTaskId = (String)objectMap.get(DomainObject.SELECT_ID);
                    sMql = "expand bus "+sTaskId+" to rel "+DomainObject.RELATIONSHIP_SUBTASK+" recurse to end select bus id dump |";
                    bResult = mql.executeCommand(context, sMql);
                    if(bResult)
                    {
                        sResult = mql.getResult().trim();
                        if(sResult!=null && !"".equals(sResult))
                        {
                            sResultTkz = new StringTokenizer(sResult,"|");
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Type",(String)sResultTkz.nextToken());
                            objectMap.put("Context Object Name",(String)sResultTkz.nextToken());
                            sResultTkz.nextToken();
                            objectMap.put("Context Object Id",(String)sResultTkz.nextToken());
                        }
                    }

                    // Update the Task Type for Approval Status Action
                    objectMap.put("ValidApprovalStatusAction", "WBS-Complete");

                }
                else if (DomainObject.TYPE_INBOX_TASK.equalsIgnoreCase(sTaskType)) {
                    if ("Approve".equals((String)objectMap.get(SELECT_ATTRIBUTE_ROUTE_ACTION))) {
                        // Update the Task Type for Approval Status Action
                        objectMap.put("ValidApprovalStatusAction", "IT-Approve");
                    }
                    else {
                        // Update the Task Type for Approval Status Action
                        objectMap.put("ValidApprovalStatusAction", "IT-Complete");
                    }
                }
                else {
                    throw new Exception("Invalid type of task");
                }

                finalTaskMapList.add(objectMap);
            }//while

            //
            // To get tasks to be accepted
            //
            // Find out the person assignments
            Vector vecPersonAssignments = PersonUtil.getAssignments(context);
            vecPersonAssignments = (Vector)vecPersonAssignments.clone(); // To avoid internal cache modification
            vecPersonAssignments.remove(context.getUser());// Remove current user from list

            StringList slPersonAssignments = new StringList();
            slPersonAssignments.addAll(vecPersonAssignments);

            StringList slParentRolesOrGroups = new StringList();
            String strRoleOrGroup = null;
            Group groupObj = null;
            Role roleObj = null;
            Vector vecParentRolesOrGroups = null;

            // Find all the parent role and group hierarchy
            for (Iterator itrPersonAssignments = slPersonAssignments.iterator(); itrPersonAssignments.hasNext();) {
                strRoleOrGroup = (String) itrPersonAssignments.next();

                // Is it role?
                try {
                    if(isRoleOrGroup(context,"Role",strRoleOrGroup)){
                        roleObj = new Role(strRoleOrGroup);
                        //if(context )
                        roleObj.open(context);
    
                        // Find all its parents
                        vecParentRolesOrGroups = roleObj.getParents(context, true);
                        if(vecParentRolesOrGroups != null){
                            slParentRolesOrGroups.addAll(vecParentRolesOrGroups);
                        }
                        roleObj.close(context);
                    } else if(isRoleOrGroup(context,"Group",strRoleOrGroup)){
                        groupObj = new Group(strRoleOrGroup);
                        groupObj.open(context);

                        // Find all its parents
                        vecParentRolesOrGroups = groupObj.getParents(context, true);
                        if(vecParentRolesOrGroups != null){
                            slParentRolesOrGroups.addAll(vecParentRolesOrGroups);
                        }
                        groupObj.close(context); 
                    }
                }catch(MatrixException me){
                }
            }

            // Add these parents into person assignments list
            slPersonAssignments.addAll(slParentRolesOrGroups);

            String strPersonAssignments = FrameworkUtil.join(slPersonAssignments, ",");
            objectWhere = DomainObject.SELECT_OWNER + " matchlist " + "\"" + strPersonAssignments + "\" \",\"";
            typePattern = new Pattern(DomainObject.TYPE_INBOX_TASK);

            MapList taskMapList1 = DomainObject.findObjects(context,
                                                   typePattern.getPattern(),
                                                   null,
                                                   objectWhere,
                                                   selectTypeStmts);

            // Adding level to each map for sorting to work
            for (objectListItr = taskMapList1.iterator(); objectListItr.hasNext();) {
                objectMap = (Map) objectListItr.next();

                // Add level value else sorting will give problem
                objectMap.put("level","1");

                // Update the Task Type for Approval Status Action
                if (DomainObject.TYPE_INBOX_TASK.equals((String)objectMap.get(DomainObject.SELECT_TYPE))) {
                    objectMap.put("ValidApprovalStatusAction", "IT-Accept");
                }
            }

            finalTaskMapList.addAll(taskMapList1);

            //////////////////////////////////////////////////////////////////////////////////////////////
            // Add a serial number to each map, so that even if the order of map changes,
            // each map has its serial number with it. This number will be used while generating the
            // editable elements in table, and also in final edit process.
            // This portion should be the last part of this method so that each map gets its serial
            // number.
            //////////////////////////////////////////////////////////////////////////////////////////////
            int nSerialNumber = 0;
            for (objectListItr = finalTaskMapList.iterator(); objectListItr.hasNext(); nSerialNumber++) {
                objectMap = (Map) objectListItr.next();

                // Add level value else sorting will give problem
                objectMap.put("serialNumber", String.valueOf(nSerialNumber));
            }
            //////////////////////////////////////////////////////////////////////////////////////////////

            return finalTaskMapList;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Method is used to populate the data for Approvals table from advanced lifecycle functionality.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args The packed argument for JPO, containing the program map.
     *             This program map will have request parameter information, objectId and information about the UI table object.
     * @return MapList of data
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getAllTaskSignaturesOnObject(Context context, String[] args) throws Exception {
        try {
            // To hold the table data
            MapList mlTableData = new MapList();

            // Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);
            // Get object id
            String strObjectId = (String)programMap.get("objectId");

            DomainObject dmoObject = null;
            DomainObject dmoRoute = null;

            String strCurrentStateName = null;
            String strObjectPolicyName = null;
            String strCurrentSymbolicStateName = null;
            String strSymbolicObjectPolicyName = null;
            String strRelPattern = null;
            String strTypePattern = null;
            String strObjectWhere = null;
            String strRelWhere = null;
            String strCurrentRouteId = null;
            String strRouteNodeId = null;
            String strCurrentRouteBaseState = null;
            String strCurrentRouteBasePolicy = null;
            String strCurrentRouteStatus = null;
            String strEndDate = null;
            StringList slRelSelect = null;
            StringList slBusSelect = null;
            short nRecurseToLevel = (short)1;
            final boolean GET_TO = true;
            final boolean GET_FROM = true;
            boolean isActivated = false;
            Map mapRouteInfo = null;
            Map mapTemp = null;
            Map mapTemp2 = null;
            Map mapObjectInfo = null;
            MapList mlDefinedTasksOnRoute = null;
            MapList mlActivatedTasksOnRoute = null;
            MapList mlTemp = null;
            MapList mlRoutes = null;
            State fromState = null;
            State toState = null;

            // For i18nNow string formation
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            final String STRING_PENDING = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Pending");
            final String STRING_AD_HOC = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Default.Route_Base_State");

            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // Find all the person assignment
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            StringList slPersonAssignments = new StringList();
            StringList slParentRolesOrGroups = new StringList();
            StringList slParents = null;
            String strRoleOrGroup = null;
            Group groupObj = null;
            Role roleObj = null;

            // Find out the person assignments
            slPersonAssignments.addAll(PersonUtil.getAssignments(context));

            // Remove user name from the list, as above API adds it into
            slPersonAssignments.remove(context.getUser());

            // Find all the parent role and group hierarchy
            for (Iterator itrPersonAssignments = slPersonAssignments.iterator(); itrPersonAssignments.hasNext();) {
                strRoleOrGroup = (String) itrPersonAssignments.next();

                // Is it role?
                try {
                    if(isRoleOrGroup(context,"Role",strRoleOrGroup)){
                        roleObj = new Role(strRoleOrGroup);
                        roleObj.open(context);
                        // Find all its parents
                        slParents = roleObj.getParents(context, true);
                        if (slParents != null) {
                            slParentRolesOrGroups.addAll(slParents);
                        }
                        roleObj.close(context);
                    }
                    else if(isRoleOrGroup(context,"Group",strRoleOrGroup)){
                            groupObj = new Group(strRoleOrGroup);
                            groupObj.open(context);
    
                            // Find all its parents
                            slParents = groupObj.getParents(context, true);
                            if (slParents != null) {
                                slParentRolesOrGroups.addAll(slParents);
                            }
    
                            groupObj.close(context);
                   }
                } catch (MatrixException me){
                }
            }

            // Add these parents into person assignments list
            slPersonAssignments.addAll(slParentRolesOrGroups);
            /////////////////////////////////////////////////////////////////////////////////////////////////

            // Get context object information and add it to the related objects map marking it is not related object
            dmoObject = new DomainObject(strObjectId);
            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_POLICY);
            mapObjectInfo = dmoObject.getInfo(context, slBusSelect);

            // Get object information
            strObjectPolicyName = (String)mapObjectInfo.get(DomainObject.SELECT_POLICY);
            strSymbolicObjectPolicyName = FrameworkUtil.getAliasForAdmin(context, "Policy", strObjectPolicyName, false);

            ///////////////////////////////////////////////////////////////////////////////////////
            // Find all Routes
            ///////////////////////////////////////////////////////////////////////////////////////
            strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
            strTypePattern = DomainObject.TYPE_ROUTE;
            slRelSelect = new StringList();
            slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);
            slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
            nRecurseToLevel = (short)1;
            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_ID);
            slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_STATUS);
            strRelWhere = "";

            mlRoutes = dmoObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

            ///////////////////////////////////////////////////////////////////////////////////////
            // Find all States
            ///////////////////////////////////////////////////////////////////////////////////////
            StateList stateList = LifeCyclePolicyDetails.getStateList(context, dmoObject, strObjectPolicyName);

            int nTotalStates = stateList.size();
        		
             ///////////////////////////////////////////////////////////////////////////////////////
            // For each state find Route Tasks and Signatures
            ///////////////////////////////////////////////////////////////////////////////////////
            boolean isTaskOrSignAddedForThisState = false;
            for (int nStateIndex = 0; nStateIndex < nTotalStates; nStateIndex++) {
                isTaskOrSignAddedForThisState = false;

                // Find from and to state information
                fromState = (State)stateList.get(nStateIndex);

                // Get states' symbolic name
                strCurrentStateName = fromState.getName();
                strCurrentSymbolicStateName = FrameworkUtil.reverseLookupStateName(context, strObjectPolicyName, strCurrentStateName);

                ////////////////////////////////////////////////////////////////////////////////////
                // Find all the tasks on each route for this state
                ////////////////////////////////////////////////////////////////////////////////////
                for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
                    // Find the route object
                    mapRouteInfo = (Map) itrRoutes.next();
                    strCurrentRouteId = (String)mapRouteInfo.get(DomainObject.SELECT_ID);
                    strCurrentRouteBasePolicy = (String)mapRouteInfo.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
                    strCurrentRouteBaseState = (String)mapRouteInfo.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);
                    strCurrentRouteStatus = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS);

                    // Skip the processing of routes which are not for this state
                    if ( !(strCurrentSymbolicStateName.equals(strCurrentRouteBaseState) && strSymbolicObjectPolicyName.equals(strCurrentRouteBasePolicy))) {
                        continue;
                    }

                    dmoRoute = new DomainObject(strCurrentRouteId);

                    // Find the tasks defined on the route object, using Route Node relationship
                    strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
                    strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;
                    slBusSelect = new StringList();
                    slBusSelect.add(DomainObject.SELECT_ID);
                    slBusSelect.add(DomainObject.SELECT_TYPE);
                    slBusSelect.add(DomainObject.SELECT_NAME);
                    slRelSelect = new StringList();
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_TITLE);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_APPROVAL_STATUS);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_COMMENTS);
                    slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER);
                    slRelSelect.add(SELECT_ATTRIBUTE_ROUTE_ACTION);
                    nRecurseToLevel = (short)1;
                    strObjectWhere = "";
                    strRelWhere = "";
                    mlDefinedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                    // Find the tasks connected to route object, using Route Task relationship
                    strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
                    strTypePattern = DomainObject.TYPE_INBOX_TASK;
                    slBusSelect = new StringList();
                    slBusSelect.add(DomainObject.SELECT_ID);
                    slBusSelect.add(DomainObject.SELECT_NAME);
                    slBusSelect.add(DomainObject.SELECT_TYPE);
                    slBusSelect.add(DomainObject.SELECT_OWNER);
                    slBusSelect.add(DomainObject.SELECT_CURRENT);
                    slBusSelect.add(SELECT_ATTRIBUTE_TITLE);
                    slBusSelect.add(SELECT_ATTRIBUTE_APPROVAL_STATUS);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                    slBusSelect.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    slBusSelect.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                    slBusSelect.add(SELECT_ATTRIBUTE_COMMENTS);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_ID); // To find if it person is connected
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_TYPE);
                    slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_NAME);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                    slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_ACTION);
                    slRelSelect = new StringList();
                    nRecurseToLevel = (short)1;
                    strObjectWhere = "";
                    strRelWhere = "";
                    mlActivatedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, GET_TO, !GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                    // Filter the partial tasks created due to Resume Process
                    mlTemp = new MapList();
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp = (Map) itrActiveTasks.next();

                        // If person is connected then it is not partial task
                        if (mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID) != null) {
                            mlTemp.add(mapTemp);
                        }
                    }
                    mlActivatedTasksOnRoute = mlTemp;

                    // Filter out the defined tasks which are already active
                    mlTemp = new MapList();
                    for (Iterator itrDefinedTasks = mlDefinedTasksOnRoute.iterator(); itrDefinedTasks.hasNext();) {
                        mapTemp = (Map) itrDefinedTasks.next();

                        // Find the id of this relationship
                        strRouteNodeId = (String)mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID);

                        // Check if the task with this id is already activated
                        isActivated = false;
                        for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                            mapTemp2 = (Map) itrActiveTasks.next();

                            if (strRouteNodeId != null && strRouteNodeId.equals((String)mapTemp2.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID))) {
                                isActivated = true;
                                break;
                            }
                        }

                        if (!isActivated) {
                            mlTemp.add(mapTemp);
                        }
                    }
                    mlDefinedTasksOnRoute = mlTemp;

                    // Form the final task list of the route object, we want the information to be present in final list with the same key in map

                    // Add all the active tasks on to the final list
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp = (Map) itrActiveTasks.next();

                        mapTemp2 = new HashMap();
                        mapTemp2.put("name", mapTemp.get(DomainObject.SELECT_NAME));
                        mapTemp2.put("title", mapTemp.get(SELECT_ATTRIBUTE_TITLE));
                        mapTemp2.put("approvalStatus", mapTemp.get(SELECT_ATTRIBUTE_APPROVAL_STATUS));
                        mapTemp2.put("routeNodeId", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID));
                        mapTemp2.put("taskId", mapTemp.get(DomainObject.SELECT_ID));
                        mapTemp2.put("routeId", strCurrentRouteId);
                        mapTemp2.put("routeStatus", strCurrentRouteStatus);
                        mapTemp2.put("infoType", INFO_TYPE_ACTIVATED_TASK);
                        mapTemp2.put("currentState", mapTemp.get(DomainObject.SELECT_CURRENT));
                        mapTemp2.put("dueDate", mapTemp.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE));
                        mapTemp2.put("completionDate", mapTemp.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE));
                        mapTemp2.put("comments", mapTemp.get(SELECT_ATTRIBUTE_COMMENTS));
                        mapTemp2.put("instructions", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS));
                        mapTemp2.put("assigneeId", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID));
                        mapTemp2.put("assigneeType", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_TYPE));
                        mapTemp2.put("assigneeName", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_NAME));
                        mapTemp2.put("routeTaskUser", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_TASK_USER));
                        mapTemp2.put("owner", mapTemp.get(DomainObject.SELECT_OWNER));
                        mapTemp2.put("id", strObjectId + "^" + strCurrentStateName + "^^" + mapTemp.get(DomainObject.SELECT_ID));
                        mapTemp2.put("currentRowState", strCurrentStateName);
                        mapTemp2.put("routeAction", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_ACTION));

                        // Object information
                        mapTemp2.put("parentObjectId", strObjectId);
                        mapTemp2.put("parentObjectState", strCurrentStateName);
                        mapTemp2.put("parentObjectPolicy", strObjectPolicyName);

                        // Level Required for sorting
                        mapTemp2.put("level", "1");


                        // Add the tasks due to route objects in the table data list
                        mlTableData.add(mapTemp2);
                        isTaskOrSignAddedForThisState = true;
                    }

                    // Add all the defined tasks on to the final list
                    for (Iterator itrDefinedTasks = mlDefinedTasksOnRoute.iterator(); itrDefinedTasks.hasNext();) {
                        mapTemp = (Map) itrDefinedTasks.next();

                        mapTemp2 = new HashMap();
                        mapTemp2.put("name", mapTemp.get(SELECT_REL_ATTRIBUTE_TITLE));
                        mapTemp2.put("title", mapTemp.get(SELECT_REL_ATTRIBUTE_TITLE));
                        mapTemp2.put("approvalStatus", mapTemp.get(SELECT_REL_ATTRIBUTE_APPROVAL_STATUS));
                        mapTemp2.put("routeNodeId", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID));
                        mapTemp2.put("routeId", strCurrentRouteId);
                        mapTemp2.put("routeStatus", strCurrentRouteStatus);
                        mapTemp2.put("infoType", INFO_TYPE_DEFINED_TASK);
                        mapTemp2.put("dueDate", mapTemp.get(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE));
                        mapTemp2.put("comments", mapTemp.get(SELECT_REL_ATTRIBUTE_COMMENTS));
                        mapTemp2.put("instructions", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS));
                        mapTemp2.put("assigneeId", mapTemp.get(DomainObject.SELECT_ID));
                        mapTemp2.put("assigneeType", mapTemp.get(DomainObject.SELECT_TYPE));
                        mapTemp2.put("assigneeName", mapTemp.get(DomainObject.SELECT_NAME));
                        mapTemp2.put("routeTaskUser", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER));
                        mapTemp2.put("id", strObjectId + "^" + strCurrentStateName + "^^" + mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID));
                        mapTemp2.put("currentRowState", strCurrentStateName);
                        mapTemp2.put("routeAction", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_ACTION));

                        // Object information
                        mapTemp2.put("parentObjectId", strObjectId);
                        mapTemp2.put("parentObjectState", strCurrentStateName);
                        mapTemp2.put("parentObjectPolicy", strObjectPolicyName);

                        // Level Required for sorting
                        mapTemp2.put("level", "1");

                        // Add the tasks due to route objects in the table data list
                        mlTableData.add(mapTemp2);
                        isTaskOrSignAddedForThisState = true;
                    }
                }// for each route object


                //
                // Find the signature for this state
                //
				strEndDate = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",true,strObjectId ,"state[" + fromState.getName() + "].end");
                // If we get the from and to state then only there are signatures to be found out
                if (fromState != null) {
                    MapList mlSignatureDetails = null;
                    Map mapSignatureInfo = null;
                    Map mapSignatureDetails = null;
                    String strSignatureName = null;
                    String strResult = null;
                    Vector vecAllBranches = new Vector();

                    /////////////////////////////////////////////////////////////////////////////
                    //
                    // Detect if there are branches
                    //
                    for (Iterator itrAllStates = stateList.iterator(); itrAllStates.hasNext();) {
                        toState = (State)itrAllStates.next();

                        // Skip if this is from state
                        if (toState.getName().equals(fromState.getName())) {
                            continue;
                        }

                        mlSignatureDetails = dmoObject.getSignaturesDetails(context, fromState, toState);

                        if (mlSignatureDetails != null && mlSignatureDetails.size() > 0) {
                            //
                            // We need to find out the due date for the signatures.
                            //
                            for (Iterator itrSignatureDetails = mlSignatureDetails.iterator(); itrSignatureDetails.hasNext(); )
                            {
                                mapSignatureDetails = (Map)itrSignatureDetails.next();
                                // The due date for the signature will be the scheduled date for the next state
                                mapSignatureDetails.put("dueDate", toState.getScheduledDate());
                            }

                            vecAllBranches.add(mlSignatureDetails);
                        }
                    }//for

                    // If there are multiple branches then do not show the signatures if they are not signed
                    if (vecAllBranches.size() <= 0) {
                        mlSignatureDetails = new MapList();
                    }
                    else if (vecAllBranches.size() == 1) {
                        mlSignatureDetails = (MapList)vecAllBranches.get(0);
                    }
                    else if(vecAllBranches.size() > 1) {
                        mlSignatureDetails = new MapList();

                        //
                        // When there are multiple branches, only show the signatures which are signed,
                        // so filter out the signatures which are signed
                        //
                        for (Iterator itrAllBranches = vecAllBranches.iterator(); itrAllBranches.hasNext();) {
                            MapList mlCurrentBranch = (MapList) itrAllBranches.next();
                            for (Iterator itrCurrentBranchSignatures = mlCurrentBranch.iterator(); itrCurrentBranchSignatures.hasNext();) {
                                Map mapCurrentSignature = (Map) itrCurrentBranchSignatures.next();
                                if ("true".equalsIgnoreCase((String)mapCurrentSignature.get("signed"))) {
                                    mlSignatureDetails.add(mapCurrentSignature);
                                }
                            }
                        }
                    }
                    //
                    //
                    ///////////////////////////////////////////////////////////////////////////////


                    String strMQL = null;
                    String strSelectStateTemplate = "state[" + fromState.getName() + "].signature[${SIGNATURE_NAME}]";
                    StringBuffer strMQLTemplate = new StringBuffer(64);

                    // Form the MQL template to find the approver for signature
                    strMQLTemplate.append("print policy \"");
                    strMQLTemplate.append(strObjectPolicyName);
                    strMQLTemplate.append("\" select ");
                    strMQLTemplate.append(strSelectStateTemplate).append(".approve ");
                    strMQLTemplate.append(strSelectStateTemplate).append(".reject ");
                    strMQLTemplate.append(strSelectStateTemplate).append(".ignore ");
                    strMQLTemplate.append("dump ,");

                    // Do processing for each signature details so found
                    for (Iterator itrSignatureDetails = mlSignatureDetails.iterator(); itrSignatureDetails.hasNext();) {
                        mapSignatureInfo = (Map) itrSignatureDetails.next();

                        // Add information in map
                        mapSignatureInfo.put("infoType", INFO_TYPE_SIGNATURE);

                        strSignatureName = (String)mapSignatureInfo.get("name");

                        // If this singnature is not signed then find out if this user has access to approve it
                        if (!"true".equalsIgnoreCase((String)mapSignatureInfo.get("signed"))) {
                            // Find out the role/group/person assigned for Approve action for the signature
                            strMQL = FrameworkUtil.findAndReplace(strMQLTemplate.toString(), "${SIGNATURE_NAME}", strSignatureName);
                            strResult = MqlUtil.mqlCommand(context, strMQL, true);

                            mapSignatureInfo.put("approvalStatus", STRING_PENDING);
                            mapSignatureInfo.put("approver", strResult);

                            // If signature is not complete then its completion date is not present
                            mapSignatureInfo.put("completionDate", "");
                        }
                        else {
                            mapSignatureInfo.put("approver", mapSignatureInfo.get("signer"));

                            // If signature is complete and the object state is not the same as signature date
                            // then show the end date of signatures state
                            if (!fromState.isCurrent()) {
                                strEndDate = MqlUtil.mqlCommand(context, "print bus '" + strObjectId + "' select state[" + fromState.getName() + "].end dump", true);
                                mapSignatureInfo.put("completionDate", strEndDate);
                            }
                            else {
                                mapSignatureInfo.put("completionDate", "");
                            }
                        }

                        // Object information
                        mapSignatureInfo.put("parentObjectId", strObjectId);
                        mapSignatureInfo.put("parentObjectState", strCurrentStateName);
                        mapSignatureInfo.put("parentObjectPolicy", strObjectPolicyName);

                        // Level Required for sorting
                        mapSignatureInfo.put("level", "1");

                        mapSignatureInfo.put("id", strObjectId + "^" + strCurrentStateName + "^" + strSignatureName + "^");

                        // Add signature information into table data maplist
                        mapSignatureInfo.put("currentRowState", strCurrentStateName);
                        mlTableData.add(mapSignatureInfo);

                        isTaskOrSignAddedForThisState = true;
                    }

                }//if for signatures

                if (!isTaskOrSignAddedForThisState) {
                    // Add empty information into table data maplist
                    mapTemp2 = new HashMap();
                    // Level Required for sorting
                    mapTemp2.put("level", "1");
                    mapTemp2.put("infoType", INFO_TYPE_EMPTY_ROW);

                    // If the state has no tasks or signatures,
                    // it will reflect the state actual date assuming the state has occurred.
                    if (fromState.isCurrent()) {
                        mapTemp2.put("actualDate", "");
                    }
                    else {
                        mapTemp2.put("actualDate", strEndDate);                        
                    }

                    mapTemp2.put("isEmptyRow", "true");
                    mapTemp2.put("id", strObjectId + "^" + strCurrentStateName );

                    // Object information
                    mapTemp2.put("parentObjectId", strObjectId);
                    mapTemp2.put("parentObjectState", strCurrentStateName);
                    mapTemp2.put("parentObjectPolicy", strObjectPolicyName);

                    mlTableData.add(mapTemp2);
                }
            }// For each state of the object

            ////////////////////////////////////////////////////////////////////////////////////
            // Find all the Ad-Hoc tasks on each route for this state
            ////////////////////////////////////////////////////////////////////////////////////
            for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
                // Find the route object
                mapRouteInfo = (Map) itrRoutes.next();
                strCurrentRouteId = (String)mapRouteInfo.get(DomainObject.SELECT_ID);
                strCurrentRouteBasePolicy = (String)mapRouteInfo.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
                strCurrentRouteBaseState = (String)mapRouteInfo.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);
                //emxFramework.Default.Route_Base_State
                // Skip the processing of state-based routes
                if (!(STRING_AD_HOC.equalsIgnoreCase(strCurrentRouteBaseState))) {
                    continue;
                }

                dmoRoute = new DomainObject(strCurrentRouteId);

                // Find the tasks defined on the route object, using Route Node relationship
                strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
                strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;
                slBusSelect = new StringList();
                slBusSelect.add(DomainObject.SELECT_ID);
                slBusSelect.add(DomainObject.SELECT_TYPE);
                slBusSelect.add(DomainObject.SELECT_NAME);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_ACTION);
                //mapTemp2.put("routeAction", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_ACTION));
                slRelSelect = new StringList();
                slRelSelect.add(SELECT_REL_ATTRIBUTE_TITLE);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_APPROVAL_STATUS);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_COMMENTS);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
                slRelSelect.add(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);

                nRecurseToLevel = (short)1;
                strObjectWhere = "";
                strRelWhere = "";
                mlDefinedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                // Find the tasks connected to route object, using Route Task relationship
                strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
                strTypePattern = DomainObject.TYPE_INBOX_TASK;
                slBusSelect = new StringList();
                slBusSelect.add(DomainObject.SELECT_ID);
                slBusSelect.add(DomainObject.SELECT_NAME);
                slBusSelect.add(DomainObject.SELECT_TYPE);
                slBusSelect.add(DomainObject.SELECT_OWNER);
                slBusSelect.add(DomainObject.SELECT_CURRENT);
                slBusSelect.add(SELECT_ATTRIBUTE_TITLE);
                slBusSelect.add(SELECT_ATTRIBUTE_APPROVAL_STATUS);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                slBusSelect.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                slBusSelect.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                slBusSelect.add(SELECT_ATTRIBUTE_COMMENTS);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_ID); // To find if it person is connected
                slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_TYPE);
                slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_NAME);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_ACTION);
                slRelSelect = new StringList();
                nRecurseToLevel = (short)1;
                strObjectWhere = "";
                strRelWhere = "";
                mlActivatedTasksOnRoute = dmoRoute.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, GET_TO, !GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

                // Filter the partial tasks created due to Resume Process
                mlTemp = new MapList();
                for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                    mapTemp = (Map) itrActiveTasks.next();

                    // If person is connected then it is not partial task
                    if (mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID) != null) {
                        mlTemp.add(mapTemp);
                    }
                }
                mlActivatedTasksOnRoute = mlTemp;

                // Filter out the defined tasks which are already active
                mlTemp = new MapList();
                for (Iterator itrDefinedTasks = mlDefinedTasksOnRoute.iterator(); itrDefinedTasks.hasNext();) {
                    mapTemp = (Map) itrDefinedTasks.next();

                    // Find the id of this relationship
                    strRouteNodeId = (String)mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID);

                    // Check if the task with this id is already activated
                    isActivated = false;
                    for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                        mapTemp2 = (Map) itrActiveTasks.next();

                        if (strRouteNodeId != null && strRouteNodeId.equals((String)mapTemp2.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID))) {
                            isActivated = true;
                            break;
                        }
                    }

                    if (!isActivated) {
                        mlTemp.add(mapTemp);
                    }
                }
                mlDefinedTasksOnRoute = mlTemp;

                // Form the final task list of the route object, we want the information to be present in final list with the same key in map

                // Add all the active tasks on to the final list
                for (Iterator itrActiveTasks = mlActivatedTasksOnRoute.iterator(); itrActiveTasks.hasNext();) {
                    mapTemp = (Map) itrActiveTasks.next();

                    mapTemp2 = new HashMap();
                    mapTemp2.put("name", mapTemp.get(DomainObject.SELECT_NAME));
                    mapTemp2.put("title", mapTemp.get(SELECT_ATTRIBUTE_TITLE));
                    mapTemp2.put("approvalStatus", mapTemp.get(SELECT_ATTRIBUTE_APPROVAL_STATUS));
                    mapTemp2.put("routeNodeId", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID));
                    mapTemp2.put("taskId", mapTemp.get(DomainObject.SELECT_ID));
                    mapTemp2.put("routeId", strCurrentRouteId);
                    mapTemp2.put("infoType", INFO_TYPE_ACTIVATED_TASK);
                    mapTemp2.put("currentState", mapTemp.get(DomainObject.SELECT_CURRENT));
                    mapTemp2.put("dueDate", mapTemp.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE));
                    mapTemp2.put("completionDate", mapTemp.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE));
                    mapTemp2.put("comments", mapTemp.get(SELECT_ATTRIBUTE_COMMENTS));
                    mapTemp2.put("instructions", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS));
                    mapTemp2.put("assigneeId", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_ID));
                    mapTemp2.put("assigneeType", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_TYPE));
                    mapTemp2.put("assigneeName", mapTemp.get(SELECT_ROUTE_TASK_ASSIGNEE_NAME));
                    mapTemp2.put("routeTaskUser", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_TASK_USER));
                    mapTemp2.put("owner", mapTemp.get(DomainObject.SELECT_OWNER));
                    mapTemp2.put("routeBaseState", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY));
                    mapTemp2.put("routeBasePolicy", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE));
                    mapTemp2.put("isAdHoc", "true");
                    mapTemp2.put("routeAction", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_ACTION));
                    // Object information
                    mapTemp2.put("parentObjectId", strObjectId);
                    mapTemp2.put("parentObjectState", strCurrentStateName);
                    mapTemp2.put("parentObjectPolicy", strObjectPolicyName);

                    mapTemp2.put("id", strObjectId + "^^^" + mapTemp.get(DomainObject.SELECT_ID));
                    // Level Required for sorting
                    mapTemp2.put("level", "1");


                    // Add the tasks due to route objects in the table data list
                    mlTableData.add(mapTemp2);
                }

                // Add all the defined tasks on to the final list
                for (Iterator itrDefinedTasks = mlDefinedTasksOnRoute.iterator(); itrDefinedTasks.hasNext();) {
                    mapTemp = (Map) itrDefinedTasks.next();

                    mapTemp2 = new HashMap();
                    mapTemp2.put("name", mapTemp.get(SELECT_REL_ATTRIBUTE_TITLE));
                    mapTemp2.put("title", mapTemp.get(SELECT_REL_ATTRIBUTE_TITLE));
                    mapTemp2.put("approvalStatus", mapTemp.get(SELECT_REL_ATTRIBUTE_APPROVAL_STATUS));
                    mapTemp2.put("routeNodeId", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID));
                    mapTemp2.put("routeId", strCurrentRouteId);
                    mapTemp2.put("infoType", INFO_TYPE_DEFINED_TASK);
                    mapTemp2.put("dueDate", mapTemp.get(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE));
                    mapTemp2.put("comments", mapTemp.get(SELECT_REL_ATTRIBUTE_COMMENTS));
                    mapTemp2.put("instructions", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_INSTRUCTIONS));
                    mapTemp2.put("assigneeId", mapTemp.get(DomainObject.SELECT_ID));
                    mapTemp2.put("assigneeType", mapTemp.get(DomainObject.SELECT_TYPE));
                    mapTemp2.put("assigneeName", mapTemp.get(DomainObject.SELECT_NAME));
                    mapTemp2.put("routeTaskUser", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_TASK_USER));
                    mapTemp2.put("routeBaseState", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY));
                    mapTemp2.put("routeBasePolicy", mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE));
                    mapTemp2.put("isAdHoc", "true");
                    mapTemp2.put("routeAction", mapTemp.get(SELECT_ATTRIBUTE_ROUTE_ACTION));

                    // Object information
                    mapTemp2.put("parentObjectId", strObjectId);
                    mapTemp2.put("parentObjectState", strCurrentStateName);
                    mapTemp2.put("parentObjectPolicy", strObjectPolicyName);

                    mapTemp2.put("id", strObjectId + "^^^" + mapTemp.get(SELECT_REL_ATTRIBUTE_ROUTE_NODE_ID));

                    // Level Required for sorting
                    mapTemp2.put("level", "1");

                    // Add the tasks due to route objects in the table data list
                    mlTableData.add(mapTemp2);
                }
            }// for each route object

            return mlTableData;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Completes the given task using selected actions Accept/Approve/Reject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - args[0] : type to identify what is to be processed. Task or Signature
     *             - args[1]: <object id> or <task id>. If type=Task then id will be task id. If type=Signature then id will be id of the object in context
     *             - args[2]: <state>. The name of the state of the object in context. This parameter will be used when type=Signature
     *             - args[3]: <signature name>. The signature name for the given state and id. This parameter will be used when type=Signature
     *             - args[4]: <action>. The user selection from Approvals page
     *             - args[5]: <comments>. The comments to be applied for task or signature
     * @return 0 means success
     * @throws Exception
     */
    public  int completeTaskOrSignature(Context context, String[] args) throws Exception {
        try {
            int nReturnCode = 0;
            String strObjectId = args[0];
            String strSignatureName = args[2];
            String strTaskId = args[3];
            String strApprovalAction = args[4];
            String strSignComments = args[5];
            String strAlertMessage = null;
            StringList busSelects = null;
            String taskId = null;
            String strObjType = null;
            Map taskMap = null;
            String sMessage = null;

            TimeZone tz = TimeZone.getTimeZone(context.getSession().getTimezone());
			Calendar cal = Calendar.getInstance(tz);
			double dbMilisecondsOffset = (double)cal.DST_OFFSET;
            double iClientTimeOffset = (new Double(dbMilisecondsOffset/(1000*60*60))).doubleValue();

            String strLanguage = context.getSession().getLanguage();
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            ContextUtil.startTransaction(context, true);

            i18nNow loc = new i18nNow();
            com.matrixone.apps.common.UserTask userTask = new com.matrixone.apps.common.UserTask();

            if (strSignatureName != null && !"".equals(strSignatureName)) {
                MQLCommand mqlCommand = new MQLCommand();
                StringBuffer strMqlCmd = new StringBuffer(50);
                strMqlCmd.append(strApprovalAction);
                strMqlCmd.append(" bus ");
                strMqlCmd.append(strObjectId);
                strMqlCmd.append(" signature ");
                strMqlCmd.append("\"");
                strMqlCmd.append(strSignatureName);
                strMqlCmd.append("\"");
                strMqlCmd.append(" comment ");
                strMqlCmd.append("\"");
                strMqlCmd.append(strSignComments);
                strMqlCmd.append("\"");

                mqlCommand.open(context);
                mqlCommand.executeCommand(context, strMqlCmd.toString());
                String strQueryResult = mqlCommand.getResult().trim();
                mqlCommand.close(context);

                if (strQueryResult.length() == 0) {
                    String strError = mqlCommand.getError().trim();

                    if (strError.length() > 0) {
                        throw new MatrixException(strError);
                    }
                }
            }
            else{
                DomainObject taskObject = new DomainObject(strTaskId);
                HashMap appMap = new HashMap();

                if ("approve".equals(strApprovalAction) || "abstain".equals(strApprovalAction)) {//Modified for bug 346480
                    busSelects = new StringList();
                    busSelects.addElement(DomainObject.SELECT_ID);
                    busSelects.addElement(DomainObject.SELECT_TYPE);

                    taskMap = taskObject.getInfo(context, busSelects);
                    taskId = (String)taskMap.get(DomainObject.SELECT_ID);
                    strObjType = (String)taskMap.get(DomainObject.SELECT_TYPE);

                    sMessage = loc.GetString(RESOURCE_BUNDLE ,strLanguage, "emxFramework.common.TaskApproveMessage");
                    appMap.put("emxFramework.common.TaskApproveMessage",sMessage);

                    if (DomainObject.TYPE_INBOX_TASK.equals(strObjType)) {
                        strAlertMessage = userTask.doInboxTaskAction(context, taskId, appMap, strSignComments, "Complete", iClientTimeOffset);
                        
                        // Added for bug 346480
                        // Abstain operation has same effect as that of Approve, only the Approval Status attribute
                        // is set to Abstain.
                        if ("abstain".equals(strApprovalAction)) {
                            InboxTask inboxTask = new InboxTask(taskId);
                            inboxTask.setAttributeValue(context, DomainObject.ATTRIBUTE_APPROVAL_STATUS, "Abstain");
                        }
                    }
                }
                else if("complete".equals(strApprovalAction)){
                    busSelects = new StringList();
                    busSelects.addElement(DomainObject.SELECT_ID);
                    busSelects.addElement(DomainObject.SELECT_TYPE);

                    taskMap              = taskObject.getInfo(context, busSelects);
                    taskId            = (String)taskMap.get(DomainObject.SELECT_ID);
                    strObjType        = (String)taskMap.get(DomainObject.SELECT_TYPE);

                    sMessage = loc.GetString(RESOURCE_BUNDLE ,strLanguage, "emxFramework.common.TaskCompletionMessage");
                    appMap.put("emxFramework.common.TaskCompletionMessage",sMessage);

                    if (DomainObject.TYPE_INBOX_TASK.equals(strObjType))
                    {
                        strAlertMessage = userTask.doInboxTaskAction(context, taskId, appMap, strSignComments, "Complete", iClientTimeOffset);
                    }
                    else if (DomainObject.TYPE_TASK.equals(strObjType)) {
                        strAlertMessage = userTask.doWBSTaskAction(context, taskId, "Complete");
                    }
                }
                else if("reject".equals(strApprovalAction)){
                    busSelects = new StringList();

                    busSelects.addElement(DomainObject.SELECT_ID);
                    busSelects.addElement(DomainObject.SELECT_TYPE);

                    taskMap = taskObject.getInfo(context, busSelects);
                    taskId = (String)taskMap.get(DomainObject.SELECT_ID);
                    strObjType = (String)taskMap.get(DomainObject.SELECT_TYPE);

                    sMessage = loc.GetString(RESOURCE_BUNDLE ,strLanguage, "emxFramework.common.TaskRejectMessage");
                    appMap.put("emxFramework.common.TaskRejectMessage",sMessage);

                    if (DomainObject.TYPE_INBOX_TASK.equals(strObjType)) {
                        strAlertMessage = userTask.doInboxTaskAction(context, taskId, appMap, strSignComments, "Reject", iClientTimeOffset);
                    }
                }
                if (strAlertMessage != null && !"".equals(strAlertMessage)) {
                    throw new Exception(strAlertMessage);
                }
            }

            ContextUtil.commitTransaction(context);

            return nReturnCode;
        }
        catch(Exception exp) {
            ContextUtil.abortTransaction(context);

            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Used to reassign the selected task in Reassign functionality under Approvals tab in advance lifecycle page.
     * Reassigns the active or future tasks to another assignee.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - args[0]: object id in context
     *             - args[1]: the name of the state selected for the object
     *             - args[2]: signature name, if current selection is signature
     *             - args[3]: task object id, if current selection is task
     *             - args[4]: the assignee type, either person, role or group.
     *             - args[5]: the assignee name
     *             - args[6]: the comments
     *
     * @return 0 means success
     * @throws Exception
     */
    public  int reassignTask(Context context, String[] args) throws Exception {
        try {
            String strTaskId = args[3];
            String strNewAssigneeType = args[4];
            String strNewAssignee = args[5];
            String strComments = args[6];

            if ("Person".equals(strNewAssigneeType)) {
                strNewAssignee = PersonUtil.getPersonObjectID(context, strNewAssignee);
            }


            int nReturnCode = 0;
            ContextUtil.startTransaction(context, true);

            String SELECT_TASK_ASSIGNEE_TYPE            =   "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.type";
            String SELECT_TASK_ASSIGNEE_ID              =   "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.id";
            String SELECT_ROUTE_ID                      =   "from[" + DomainObject.RELATIONSHIP_ROUTE_TASK + "].to.id";
            String SELECT_RELATIONSHIP_PROJECT_TASK_ID  =   "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].id";
            String SELECT_ATTRIBUTE_ROUTE_NODE_ID       =   "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";

            String SELECT_REL_ROUTE_TASK_ASSIGNEE_ID    =   "to.id";
            String SELECT_REL_ROUTE_TASK_ASSIGNEE_TYPE  =   "to.type";
            String SELECT_ATTRIBUTE_ROUTE_TASK_USER     =   "attribute["+DomainObject.ATTRIBUTE_ROUTE_TASK_USER+"]";

            String strLanguage = context.getSession().getLanguage();
            i18nNow loc = new i18nNow();

            String strSubject = loc.GetString("emxFrameworkStringResource", strLanguage, "emxFramework.ReassignTask.Notification.Subject");
            String strBody = loc.GetString("emxFrameworkStringResource", strLanguage, "emxFramework.ReassignTask.Notification.Body");
            String strErrorReassignToRoleOrGroup = (String)loc.GetString("emxFrameworkStringResource", strLanguage, "emxFramework.Alert.ActiveTaskCanOnlyReassignToPerson");
            String strExists            = "FALSE";
            String newAssineeAliasName  = "";

            StringList toList = new StringList();
            StringList ccList = new StringList();

            Route route = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);

            if("".equalsIgnoreCase(strTaskId) || "null".equalsIgnoreCase(strTaskId))
            {
                //Show error Task Id not valid
            }
            else
            {
                //check for the business object.
                MQLCommand mqlCommand = new MQLCommand();
                StringBuffer strMqlCmd = new StringBuffer(50);
                strMqlCmd.append(" print ");
                strMqlCmd.append(" bus ");
                strMqlCmd.append(strTaskId);
                strMqlCmd.append(" select ");
                strMqlCmd.append(" exists");
                mqlCommand.open(context);
                mqlCommand.executeCommand(context, strMqlCmd.toString());
                strExists = mqlCommand.getResult().trim();
                mqlCommand.close(context);

				if (strExists.indexOf("FALSE") >= 0)
                {
                    //Process non-exist task object
                    String[] RelIdArray ={strTaskId};
                    StringList resultList = new StringList(10);
                    resultList.add(SELECT_REL_ROUTE_TASK_ASSIGNEE_ID);
                    resultList.add(SELECT_REL_ROUTE_TASK_ASSIGNEE_TYPE);
                    resultList.add("from."+DomainObject.SELECT_ID);
                    resultList.add(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                    resultList.add(SELECT_ATTRIBUTE_TITLE);
                    resultList.add(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    resultList.add(SELECT_ATTRIBUTE_ROUTE_SEQUENCE);
                    resultList.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    resultList.add("attribute[" + DomainObject.ATTRIBUTE_DUEDATE_OFFSET + "]");
                    resultList.add("attribute[" + DomainObject.ATTRIBUTE_DATE_OFFSET_FROM + "]");

                    MapList resultMapList  = DomainRelationship.getInfo(context, RelIdArray, resultList);

                    String strOldAssigneeId     = (String)((Map)resultMapList.get(0)).get(SELECT_REL_ROUTE_TASK_ASSIGNEE_ID);
                    String strOldAssigneeType   = (String)((Map)resultMapList.get(0)).get(SELECT_REL_ROUTE_TASK_ASSIGNEE_TYPE);
                    String tmpOldAssigneeName   = (String)((Map)resultMapList.get(0)).get(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                    String title   = (String)((Map)resultMapList.get(0)).get(SELECT_ATTRIBUTE_TITLE);
                    String routeInstuctions   = (String)((Map)resultMapList.get(0)).get(SELECT_ATTRIBUTE_ROUTE_INSTRUCTIONS);
                    String routeTaskOrder = (String)((Map)resultMapList.get(0)).get(SELECT_ATTRIBUTE_ROUTE_SEQUENCE);
                    String scheduledDate   = (String)((Map)resultMapList.get(0)).get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    String sAttDueDateOffset   = (String)((Map)resultMapList.get(0)).get("attribute[" + DomainObject.ATTRIBUTE_DUEDATE_OFFSET + "]");
                    String sAttDueDateOffsetFrom   = (String)((Map)resultMapList.get(0)).get("attribute[" + DomainObject.ATTRIBUTE_DATE_OFFSET_FROM + "]");
                    String strOldAssigneeName   =  PropertyUtil.getSchemaProperty(context, tmpOldAssigneeName);

                    if("Person".equals(strOldAssigneeType) )
                    {
                        if("Person".equals(strNewAssigneeType))
                        {
                            //Process Person--> Person
                            if(!strOldAssigneeId.equals(strNewAssignee)){

                                DomainObject dmoNewAssignee = new DomainObject(strNewAssignee);
                                String routeId = (String)((Map)resultMapList.get(0)).get("from."+DomainObject.SELECT_ID);
                                DomainObject sRoute = new DomainObject(routeId);
                                DomainRelationship.disconnect(context, strTaskId);
                                matrix.db.Relationship relRouteNode = sRoute.connect(context,new RelationshipType(DomainObject.RELATIONSHIP_ROUTE_NODE),true,dmoNewAssignee);
                                AttributeList routeAttrList = new AttributeList();
                                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_TITLE),title));
                                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS),routeInstuctions));
                                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE),routeTaskOrder));
                                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE),scheduledDate));
                                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_DUEDATE_OFFSET),sAttDueDateOffset));
                                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM),sAttDueDateOffsetFrom));
                                relRouteNode.open(context);
                                relRouteNode.setAttributes(context,routeAttrList);
                                relRouteNode.close(context);
                                //DomainRelationship.connect(context, sRoute, DomainObject.RELATIONSHIP_ROUTE_NODE, dmoNewAssignee);
                                //DomainRelationship.setToObject(context, strTaskId, dmoNewAssignee);

                            }

                        }
                        else
                        {
                            //Process person --> RTU
                            //Create New RTU object
                            if("Role".equals(strNewAssigneeType))
                            {
                                newAssineeAliasName = FrameworkUtil.getAliasForAdmin(context, "Role", strNewAssignee, false);
                            }
                            else
                            {
                                newAssineeAliasName = FrameworkUtil.getAliasForAdmin(context, "group", strNewAssignee, false);
                            }

                                DomainObject dmoRTUObject = DomainObject.newInstance(context);
                                dmoRTUObject.createObject(context, DomainObject.TYPE_ROUTE_TASK_USER, null, null, null, null);
                                DomainRelationship.setToObject(context, strTaskId, dmoRTUObject);

                                DomainRelationship domainRelationship = new DomainRelationship(strTaskId);
                                domainRelationship.setAttributeValue(context,DomainObject.ATTRIBUTE_ROUTE_TASK_USER, newAssineeAliasName);

                        }
                    }
                    else
                    {
                        if("Person".equals(strNewAssigneeType))
                        {
                            //Process Role/Group -->Person
                            //Set to side object
                            DomainObject dmoNewAssignee = new DomainObject(strNewAssignee);
                            DomainRelationship.setToObject(context, strTaskId, dmoNewAssignee);
                            
                            //set Route Task user attribute to blank
                            DomainRelationship domainRelationship = new DomainRelationship(strTaskId);
                            domainRelationship.setAttributeValue(context,DomainObject.ATTRIBUTE_ROUTE_TASK_USER, "");
                            
                            //Delete only if not connected to any object
                            DomainObject dmoRTU = new DomainObject(strOldAssigneeId);
                            StringList slBusSelect = new StringList();
                            slBusSelect.add(DomainObject.SELECT_ID);
                            StringList slRelSelect = new StringList();
                            String strTypePattern  = DomainObject.TYPE_ROUTE + "," + DomainObject.TYPE_ROUTE_TEMPLATE;
                            String strRelPattern   = DomainObject.RELATIONSHIP_ROUTE_NODE;
                            boolean getTo   = true;
                            boolean getFrom = false;
                            short nRecurseToLevel = (short)1;
                            String strObjectWhere  = null;
                            String strRelWhere     = null;

                            MapList mlObjects = dmoRTU.getRelatedObjects(context,
                                                                         strRelPattern,
                                                                         strTypePattern,
                                                                         slBusSelect,
                                                                         slRelSelect,
                                                                         getTo,
                                                                         getFrom,
                                                                         nRecurseToLevel,
                                                                         strObjectWhere,
                                                                         strRelWhere);
                            //Checking to delete only if not connected to any object
                            if(mlObjects.size()==0){
                                String[] arrStrObj ={strOldAssigneeId};
                                DomainObject.deleteObjects(context, arrStrObj);
                            }
                        }
                        else
                        {
                            //Process Role/Group -->Role/Group
                            if("Role".equals(strNewAssigneeType))
                            {
                                newAssineeAliasName = FrameworkUtil.getAliasForAdmin(context, "Role", strNewAssignee, false);
                            }
                            else
                            {
                                newAssineeAliasName = FrameworkUtil.getAliasForAdmin(context, "group", strNewAssignee, false);
                            }

                            if(!strOldAssigneeName.equals(strNewAssignee)){

                                DomainRelationship domainRelationship = new DomainRelationship(strTaskId);
                                domainRelationship.setAttributeValue(context,DomainObject.ATTRIBUTE_ROUTE_TASK_USER, newAssineeAliasName);

                            }

                        }
                    }
                }
                else
                {
                    InboxTask taskObject  = (InboxTask)DomainObject.newInstance(context, strTaskId);
                    //process the task object that exists
                    //check if the new assinee is the Person

                    if("Person".equals(strNewAssigneeType))
                    {
                        //check for the old assinee type

                        StringList taskSelectList = new StringList(2);
                        taskSelectList.add(SELECT_TASK_ASSIGNEE_TYPE);
                        taskSelectList.add(SELECT_RELATIONSHIP_PROJECT_TASK_ID);
                        taskSelectList.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                        taskSelectList.add(SELECT_ROUTE_ID);
                        taskSelectList.add(SELECT_TASK_ASSIGNEE_ID);

                        Map taskMap = taskObject.getInfo(context,taskSelectList);

                        String strOldAssigneeType   = (String)taskMap.get(SELECT_TASK_ASSIGNEE_TYPE);
                        String sRouteId             = (String)taskMap.get(SELECT_ROUTE_ID);
                        String sTaskOldAssigneeId   = (String)taskMap.get(SELECT_TASK_ASSIGNEE_ID);

                        //check if old assinee is person
                        if("Person".equals(strOldAssigneeType))
                        {
                            //Process Person-->Person

                            if(!sTaskOldAssigneeId.equals(strNewAssignee)) {

                                //
                                // Reassign the task
                                //
                                InboxTask inboxTaskObj = (InboxTask)DomainObject.newInstance(context,DomainConstants.TYPE_INBOX_TASK);

                                // Set the task id for the task object
                                inboxTaskObj.setId(strTaskId);

                                // For delegation functionality the Allow Delegation attribute should be TRUE. So we will do the same momentarily is the Allow Delegation is No
                                boolean isAllowedDelegationMomentarily = false;
                                String strAllowDelegation = inboxTaskObj.getAttributeValue(context,DomainObject.ATTRIBUTE_ALLOW_DELEGATION);
                                if (!"true".equalsIgnoreCase(strAllowDelegation)) {
                                    isAllowedDelegationMomentarily = true;
                                    inboxTaskObj.setAttributeValue(context,DomainObject.ATTRIBUTE_ALLOW_DELEGATION, "TRUE");
                                }

                                ////////////////////////////ABSENCE DELEGATION HANDLING/////////////////////////////
                                //
                                // Check if the new assignee has configured absence delegation and he is absent
                                if ("true".equalsIgnoreCase(strAllowDelegation)) {
                                    final String ATTRIBUTE_ABSENCE_DELEGATE = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceDelegate");
                                    final String ATTRIBUTE_ABSENCE_END_DATE = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceEndDate");
                                    final String ATTRIBUTE_ABSENCE_START_DATE = PropertyUtil.getSchemaProperty(context, "attribute_AbsenceStartDate");
                                    final String SELECT_ATTRIBUTE_ABSENCE_DELEGATE = "attribute[" + ATTRIBUTE_ABSENCE_DELEGATE + "]";
                                    final String SELECT_ATTRIBUTE_ABSENCE_END_DATE = "attribute[" + ATTRIBUTE_ABSENCE_END_DATE + "]";
                                    final String SELECT_ATTRIBUTE_ABSENCE_START_DATE = "attribute[" + ATTRIBUTE_ABSENCE_START_DATE + "]";
                                    
                                    SimpleDateFormat dateFormat = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(), context.getLocale()); 
                                    StringList slBusSelect = new StringList();
                                    slBusSelect.addElement(SELECT_ATTRIBUTE_ABSENCE_DELEGATE);
                                    slBusSelect.addElement(SELECT_ATTRIBUTE_ABSENCE_START_DATE);
                                    slBusSelect.addElement(SELECT_ATTRIBUTE_ABSENCE_END_DATE);
                                    
                                    Vector vecAlreadyVisitedNewAssignees = new Vector();
                                    boolean isAbsenceDelegationSet = false;
                            
                                    while (!vecAlreadyVisitedNewAssignees.contains(strNewAssignee)) {
                                        
                                        DomainObject dmoNewAssignee = new DomainObject(strNewAssignee);
                                        Map mapNewAssigneeInfo = dmoNewAssignee.getInfo(context,
                                                slBusSelect);
                            
                                        String strAbsenceDelegate = (String) mapNewAssigneeInfo
                                                .get(SELECT_ATTRIBUTE_ABSENCE_DELEGATE);
                                        String strAbsenceStartDate = (String) mapNewAssigneeInfo
                                                .get(SELECT_ATTRIBUTE_ABSENCE_START_DATE);
                                        String strAbsenceEndDate = (String) mapNewAssigneeInfo
                                                .get(SELECT_ATTRIBUTE_ABSENCE_END_DATE);
                                        
                                        // If the absence delegation is configured then 
                                        if (strAbsenceDelegate != null
                                                && !"".equals(strAbsenceDelegate)
                                                && strAbsenceStartDate != null
                                                && !"".equals(strAbsenceStartDate)
                                                && strAbsenceEndDate != null
                                                && !"".equals(strAbsenceEndDate)) {
                            
                                            // Is the new user absent?
                                            Date dtAbsenceStart = dateFormat.parse(strAbsenceStartDate);
                                            Date dtAbsenceEnd = dateFormat.parse(strAbsenceEndDate);
                                            Date dtToday = new Date();
                                            if (dtToday.after(dtAbsenceStart)
                                                    && dtToday.before(dtAbsenceEnd)) {
                                                vecAlreadyVisitedNewAssignees.add(strNewAssignee);
                                                strNewAssignee = PersonUtil.getPersonObjectID(context,
                                                        strAbsenceDelegate);
                                                isAbsenceDelegationSet = true;
                                            }
                                            else {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }//~while
                                    
                                    if(vecAlreadyVisitedNewAssignees.contains(strNewAssignee)) {
                                        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.CircularReferenceFound", context.getLocale().getLanguage()));
                                    }
                                    vecAlreadyVisitedNewAssignees.clear();
                                }//~if allowdelegation is Yes
                                //
                                ////////////////////////////ABSENCE DELEGATION HANDLING/////////////////////////////
                                
                                // Delegate the current task to the new assignee
                                inboxTaskObj.delegateTask(context, strNewAssignee);

                                // If the attribute Allow Delegation was set "TRUE" momentarily then reset it back
                                if (isAllowedDelegationMomentarily) {
                                    inboxTaskObj.setAttributeValue(context,DomainObject.ATTRIBUTE_ALLOW_DELEGATION, "FALSE");
                                }

                                //
                                //Send Notifications
                                //
                                route.setId(sRouteId);
                                String strRouteOwner = route.getInfo(context, Route.SELECT_OWNER);

                                // Find the new and old task assignee name
                                java.util.Set userList = new java.util.HashSet();
                                userList.add(strNewAssignee);
                                userList.add(sTaskOldAssigneeId);

                                Map mapInfo = Person.getPersonsFromIds(context, userList, new StringList(Person.SELECT_NAME));
                                String strOldTaskAssigneeName = (String)((Map)mapInfo.get(sTaskOldAssigneeId)).get(Person.SELECT_NAME);
                                String strNewTaskAssigneeName = (String)((Map)mapInfo.get(strNewAssignee)).get(Person.SELECT_NAME);
                                taskObject.setOwner(context,strNewTaskAssigneeName);
                                toList.add(strNewTaskAssigneeName);
                                String strCurrentUserName = context.getUser();

                                if (strCurrentUserName.equals(strRouteOwner))
                                {
                                    ccList.add(strOldTaskAssigneeName);

                                }
                                else if (strCurrentUserName.equals(strOldTaskAssigneeName))
                                {
                                    ccList.add(strRouteOwner);

                                }
                                else
                                {
                                    toList.add(strOldTaskAssigneeName);
                                    ccList.add(strRouteOwner);

                                }
                            }
                        }
                        else
                        {
                            emxContextUtil_mxJPO.mqlError(context, strErrorReassignToRoleOrGroup);
                            ContextUtil.commitTransaction(context);
                            return 1;
                        }
                    }
                    else
                    {
                        emxContextUtil_mxJPO.mqlError(context, strErrorReassignToRoleOrGroup);
                            ContextUtil.commitTransaction(context);
                            return 1;
                    }
                }
                //Send Notifications
                if(toList.size() >= 1 && ccList.size() >= 1) {
                    MailUtil.sendMessage(context, toList, ccList, null, strSubject, strBody + strComments, new StringList(strTaskId));
                }
            }

            ContextUtil.commitTransaction(context);

            return nReturnCode;
        }
        catch(Exception exp) {
            ContextUtil.abortTransaction(context);

            exp.printStackTrace();
            throw exp;
        }
    }


    /**
     * Used to add approver at selected state in Add Approver functionality under Approvals tab in advance lifecycle page.
     * Adds an approver dynamically to the object at provided state.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - args[0]: the object id
     *             - args[1]: the state name
     *             - args[2]: the approver type ex. Person, Role or Group
     *             - args[3]: the approver name
     *             - args[4]: the instructions for the new task
     *             - args[5]: the title for the task
     *             - args[6]: the route action for the task
     *             - args[7]: the due date option for the task, with following values
     *              dueDateSet - the dueDate argument has the required value
     *              dueDateOffsetSet - the dueDateOffset and dueDateOffsetFrom arguments has required values
     *              assigneeSetDueDateSet - set Assign Set Due Date on a task
     *             - args[8]: the due date for the task in milliseconds
     *             - args[9]: dueDateOffset - Numeric value in range from 1 to 365 days
     *             - args[10]: dueDateOffsetFrom - either "Route State Date" or "Task Create Date" (Range values of attribute "Date Offset From")
     *             - args[11]: allowDelegation - case-insensitive - True if "Allow Delegation" on task is to be set Yes (otherwise pass False or null)
     *             - args[12]: requiresOwnerReview - case-insensitive - True if "Review Task" on task is to be set Yes (otherwise pass False or null)
     * @return 0 means success
     * @throws Exception
     */
    public int addApproverTask(Context context, String[] args) throws Exception {
         try {
            int nReturnCode = 0;
            ContextUtil.startTransaction(context, true);

            //getting all the parameters
            String strParentObjId = args[0];
            String strParentObjectState = args[1];
            String StrApproverType = args[2];
            String strApprover = args[3];
            String strInstructions = args[4];
            String strTitle = args[5];
            String strRouteAction = args[6];
            String strDueDateOption = args[7];
            String strDueDate = args[8];
            String strDueDateOffset = args[9];
            String strDueDateOffsetFrom = args[10];
            String strAllowDelegation = args[11];
            String strRequiresOwnerReview = args[12];
            
            if ("true".equalsIgnoreCase(strAllowDelegation)) {
                strAllowDelegation = "TRUE";
            }
            else {
                strAllowDelegation = "FALSE";
            }
            if ("true".equalsIgnoreCase(strRequiresOwnerReview)) {
                strRequiresOwnerReview = "Yes";
            }
            else {
                strRequiresOwnerReview = "No";
            }
            
            String strLanguage = context.getSession().getLanguage();

            i18nNow loc = new i18nNow();
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            final String ROUTE_FINISHED = "Finished";
            final String COMPLETED_ROUTE = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Alert.CannotAddTaskToCompletedRoute");
            final String SELECT_ROUTE_TASK_ASSIGNEE_TYPE = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.type";
            final String SELECT_ATTRIBUTE_ROUTE_STATUS = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_STATUS + "]";
            final String ATTRIBUTE_CURRENT_ROUTE_NODE = PropertyUtil.getSchemaProperty(context, "attribute_CurrentRouteNode");
            final String SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE = "attribute[" + ATTRIBUTE_CURRENT_ROUTE_NODE + "]";

            // Get object information
            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_POLICY);
            slBusSelect.add(DomainObject.SELECT_CURRENT);

            DomainObject dmoParentObject = new DomainObject(strParentObjId);
            Map mapObjectInfo = dmoParentObject.getInfo(context, slBusSelect);

            String strParentObjectPolicy = (String)mapObjectInfo.get(DomainObject.SELECT_POLICY);
            String strParentObjectCurrentState = (String)mapObjectInfo.get(DomainObject.SELECT_CURRENT);

            String strSymbolicObjectPolicyName = FrameworkUtil.getAliasForAdmin(context, "Policy", strParentObjectPolicy, false);
            String strSymbolicParentObjectState = FrameworkUtil.reverseLookupStateName(context, strParentObjectPolicy, strParentObjectState);

              //////////////////////////////////////////////////////////////////////////////////////
             // Find all Routes connected to the parent state                                    //
            //////////////////////////////////////////////////////////////////////////////////////
            StringList slRelSelect = new StringList(DomainRelationship.ATTRIBUTE_ROUTE_SEQUENCE);
            short nRecurseToLevel = (short)1;
            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_ID);
            slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_TYPE);
            slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_STATUS);
            slBusSelect.add(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
            slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_TEMPLATE_TASK_EDIT_SETTING);
            String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]=='" + strSymbolicObjectPolicyName + "' && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]=='" + strSymbolicParentObjectState + "'";
            String strObjectWhere = "";
            String strRouteSequence = null;
            String strRouteId = null;
            String strPersonObjId = null;
            String strRouteStatus = null;
            String strRouteTaskUser = "";
            String strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
            String strTypePattern = DomainObject.TYPE_ROUTE;
            Map mapRouteInfo = null;
            Map mapStartedRoute = null;
            Map mapNotStartedRoute = null;
            Map mapStoppededRoute = null;
            Map mapTemp = null;
            Map mapRelRouteNodeAttributes = new HashMap();
            Map mapRouteToBeUsed = null;
            MapList mlTemp = new MapList();
            DomainRelationship dmrRouteNode = null;
            final boolean GET_TO = true;
            final boolean GET_FROM = true;
            boolean isNewRouteCreated = false;
            com.matrixone.apps.common.Route route = (com.matrixone.apps.common.Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);

            //
            // We could have found, only those routes which are not completed, but then we have to decide if
            // new route is to be created / existing route is to be used / approver task cannot be added. So
            // we need to know if there are completed routes for a state or not.
            //
            MapList mlRoutes = dmoParentObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);
            if (mlRoutes.size() == 0) {
            	
            	createNewRoute( context, strRouteId,  route, strParentObjId, strParentObjectState);
            	isNewRouteCreated=true;
            }
            else{

                //
                //Check for finished routes if all the routes at that state are finished
                //then alert a pop up or if there are routes that are started or not started
                // then add the user/role/group to the current route sequence
                //

               //////////////////////////////////////////////////////////////////////////////////////
               // Filter out the finished Routes                                                   //
               //////////////////////////////////////////////////////////////////////////////////////
                for (Iterator itrActiveTasks = mlRoutes.iterator(); itrActiveTasks.hasNext();) {
                    mapTemp = (Map) itrActiveTasks.next();
                    String intiatingRouteTaskEditSetting = (String)mapTemp.get(SELECT_ATTRIBUTE_ROUTE_TEMPLATE_TASK_EDIT_SETTING);
                    if("Maintain Exact Task List".equals(intiatingRouteTaskEditSetting)){
                    	String taskAddApproverAlert = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",  context.getLocale(), "emxComponents.RouteWizard.MaintainExactTaskListTaskAddAlert");
                    	throw new MatrixException(taskAddApproverAlert);
                    }

                    // If person is connected then it is not partial task
                    if (!ROUTE_FINISHED.equals((String)mapTemp.get(SELECT_ATTRIBUTE_ROUTE_STATUS))) {
                        mlTemp.add(mapTemp);
                    }
                }
                mlRoutes = mlTemp;

                if (!(mlRoutes.size() == 0)){
                	

                //if there are unfinished routes
                for(Iterator itrActiveRoutes = mlRoutes.iterator(); itrActiveRoutes.hasNext();){
                    mapRouteInfo = (Map)itrActiveRoutes.next();
                    strRouteStatus = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS);
                    if("Started".equals(strRouteStatus)){
                        mapStartedRoute = mapRouteInfo;
                    }
                    else if("Not Started".equals(strRouteStatus)){
                        mapNotStartedRoute = mapRouteInfo;
                    }
                    else if("Stopped".equals(strRouteStatus)){
                        mapStoppededRoute = mapRouteInfo;
                    }
                }

                if(mapStartedRoute != null) {
                    mapRouteToBeUsed = mapStartedRoute;
                }
                else if(mapStoppededRoute != null) {
                    mapRouteToBeUsed = mapStoppededRoute;
                }
                else if(mapNotStartedRoute != null){
                    mapRouteToBeUsed = mapNotStartedRoute;
                }

                if (mapRouteToBeUsed == null) {
                    String[] formatArgs = {strParentObjId,strParentObjectState};
                    String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.LifeCycle.UsableStateBasedRouteNotFound",formatArgs);
                    throw new Exception(message);
                }

                strRouteId = (String)mapRouteToBeUsed.get(DomainObject.SELECT_ID);
                route = new com.matrixone.apps.common.Route(strRouteId);
                strRouteSequence = (String)mapRouteToBeUsed.get(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
                strRouteStatus = (String)mapRouteToBeUsed.get(SELECT_ATTRIBUTE_ROUTE_STATUS);
                //-----------------------------------------------------------common-----------------------------------------------
				}else
				{
				    createNewRoute( context, strRouteId,  route, strParentObjId, strParentObjectState);
				    isNewRouteCreated= true;
				}
			}

            if("Person".equals(StrApproverType)){
                //Adding Person to the route
                strPersonObjId = PersonUtil.getPersonObjectID(context, strApprover);
                dmrRouteNode = DomainRelationship.connect(context, route, DomainObject.RELATIONSHIP_ROUTE_NODE, new DomainObject(strPersonObjId));
            }
            else if("Role".equals(StrApproverType) || "Group".equals(StrApproverType)) {
                // Create a RTU object and connect it to route
                DomainObject dmoRTU = DomainObject.newInstance(context);
                dmrRouteNode = dmoRTU.createAndConnect(context, DomainObject.TYPE_ROUTE_TASK_USER, DomainObject.RELATIONSHIP_ROUTE_NODE, route, true);
                strRouteTaskUser = FrameworkUtil.getAliasForAdmin(context, StrApproverType, strApprover, true);
            }
            else {
                String[] formatArgs = {StrApproverType};
                String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.LifeCycle.InvalidApproverType",formatArgs);
                throw new Exception(message);
            }

            //Adding the attributes to the route node relationship
            mapRelRouteNodeAttributes = new HashMap();
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ROUTE_TASK_USER, strRouteTaskUser);
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS, strInstructions);
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ROUTE_ACTION, strRouteAction);
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_TITLE, strTitle);
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ALLOW_DELEGATION, strAllowDelegation);
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_REVIEW_TASK, strRequiresOwnerReview);
            mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE, "No");
            
            if ("assigneeSetDueDateSet".equals(strDueDateOption)) {
                mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE, "Yes");
            }
            else if ("dueDateSet".equals(strDueDateOption)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(), Locale.US);
                String strDueDateToSet = dateFormat.format(new Date(Long.parseLong(strDueDate)));
                mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE, strDueDateToSet);
            }
            else if ("dueDateOffsetSet".equals(strDueDateOption)) {
                mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_DUEDATE_OFFSET, strDueDateOffset);
                mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM, strDueDateOffsetFrom);
            }

            if(isNewRouteCreated == true){
                mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, "1");
            }
            else{
                // If the route is not started, then route sequence value will not be present, so assum it as 1
                if (strRouteSequence == null || "".equals(strRouteSequence) || "null".equals(strRouteSequence)) {
                    strRouteSequence = "1";
                }
                mapRelRouteNodeAttributes.put(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, strRouteSequence);
            }
            dmrRouteNode.setAttributeValues(context, mapRelRouteNodeAttributes);

            //Start the route if this is the current state of the object
            if (strParentObjectCurrentState.equals(strParentObjectState) && isNewRouteCreated == true) {
                route.promote(context);
                route.setDueDateFromOffsetForGivenLevelTasks(context, 1);
            }

            if ("Started".equals(strRouteStatus)) {
                // Starting the task at current level
                route.startTasksOnCurrentLevel(context);
            }
            else if ("Stopped".equals(strRouteStatus)) {
                //Starting the task at current level
                route.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Started");
                route.startTasksOnCurrentLevel(context);
                route.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Stopped");
            }

            ContextUtil.commitTransaction(context);

            return nReturnCode;
        }
        catch(Exception exp) {
            ContextUtil.abortTransaction(context);

            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Used to add approvers from template at selected state in Add Approver From Template functionality under Approvals tab in advance lifecycle page
     * Adds approvers dynamically to the object at provided state from Route Template.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - args[0]: the object id
     *             - args[1]: the state name
     *             - args[2]: the route template name
     *
     * @return 0 means success
     * @throws Exception
     */
    public  int addApproverTaskFromTemplate(Context context, String[] args) throws Exception {
        try {
            int nReturnCode = 0;
            ContextUtil.startTransaction(context, true);

            String strObjectId          = args[0]; //object id
            String strState             = args[1]; //state name
            String strRouteTemplateId   = args[2]; //route template id

            String strLanguage          = context.getSession().getLanguage();
            i18nNow loc                 = new i18nNow();
            DomainObject domRouteTemplateObject = new DomainObject(strRouteTemplateId);

            final String RESOURCE_BUNDLE                     = "emxFrameworkStringResource";
            final String ROUTE_FINISHED                      = "Finished";
            final String SELECT_ROUTE_TASK_ASSIGNEE_TYPE     = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.type";
            final String SELECT_ATTRIBUTE_ROUTE_STATUS       = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_STATUS + "]";
            final String ATTRIBUTE_CURRENT_ROUTE_NODE        = PropertyUtil.getSchemaProperty(context, "attribute_CurrentRouteNode");
            final String SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE = "attribute[" + ATTRIBUTE_CURRENT_ROUTE_NODE + "]";
            final String SELECT_REL_ATTRIBUTE_ROUTE_SEQUENCE = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE);
            final String ATTRIBUTE_AUTO_STOP_ON_REJECTION    = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection");
            final String ATTRIBUTE_ROUTE_BASE_PURPOSE    = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose");
            final String SELECT_ATTRIBUTE_AUTO_STOP_REJECTION  = "attribute[" + ATTRIBUTE_AUTO_STOP_ON_REJECTION + "]";
            final String SELECT_ATTRIBUTE_ROUTE_BASE_PURPOSE  = "attribute[" + ATTRIBUTE_ROUTE_BASE_PURPOSE + "]";
            final String COMPLETED_ROUTE = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Alert.CannotAddTaskToCompletedRoute");
            String sAttrRestrictMembers = PropertyUtil.getSchemaProperty(context, "attribute_RestrictMembers" );
            final String SELECT_ATTRIBUTE_ROUTE_SCOPE = "attribute[" + sAttrRestrictMembers + "]";

//          For Bug 347000
            final String STRING_APPROVER_CANNOT_BE_ADDED_INVALID_ROUTE_TEMPLATE = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.ApproversCannotBeAddedInvalidRouteTemplate");

            Map mapRelAttributesNew = new HashMap();

            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_POLICY);
            slBusSelect.add(DomainObject.SELECT_CURRENT);

            DomainObject dmoParentObject = new DomainObject(strObjectId);
            Map mapObjectInfo            = dmoParentObject.getInfo(context, slBusSelect);

            String strParentObjectPolicy       = (String)mapObjectInfo.get(DomainObject.SELECT_POLICY);

            String strSymbolicObjectPolicyName  = FrameworkUtil.getAliasForAdmin(context, "Policy", strParentObjectPolicy, false);
            String strSymbolicParentObjectState = FrameworkUtil.reverseLookupStateName(context, strParentObjectPolicy, strState);

            DomainRelationship dmoRelationship = null;
            Map mapRelAttributes = new HashMap();
            HashMap mapObjectToState = new HashMap();           // used while adding this object as content to the route
            com.matrixone.apps.common.Route route = (com.matrixone.apps.common.Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
            String strRouteId = null;
            boolean isRouteToBeStarted = false;
            boolean isTaskOnCurrentLevelToBeStarted = false;
            boolean isTaskOnCurrentLevelToBeStartedForStoppedRoute = false;

            // Find all Routes  connected to the parent state
            String strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
            String strTypePattern = DomainObject.TYPE_ROUTE;
            StringList slRelSelect = new StringList(DomainRelationship.ATTRIBUTE_ROUTE_SEQUENCE);
            short nRecurseToLevel = (short)1;

            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_ID);
            slBusSelect.add(SELECT_ROUTE_TASK_ASSIGNEE_TYPE);
            slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_STATUS);
            slBusSelect.add(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);

            String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]=='" + strSymbolicObjectPolicyName + "' && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]=='" + strSymbolicParentObjectState + "'";
            String strObjectWhere = "";

            final boolean GET_TO = true;
            final boolean GET_FROM = true;

            MapList mlRoutes = dmoParentObject.getRelatedObjects(context,
                                                                strRelPattern,
                                                                strTypePattern,
                                                                slBusSelect,
                                                                slRelSelect,
                                                                !GET_TO,
                                                                GET_FROM,
                                                                nRecurseToLevel,
                                                                strObjectWhere,
                                                                strRelWhere);

            if (mlRoutes.size() == 0) {
                //Create new Route
                com.matrixone.apps.common.Person objPerson = com.matrixone.apps.common.Person.getPerson(context);
                route = (com.matrixone.apps.common.Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
                String strRouteName = null;

                // Create route object
                strRouteName = FrameworkUtil.autoName(context,
                                                      "type_Route",
                                                      new Policy(DomainObject.POLICY_ROUTE).getFirstInSequence(context),
                                                      "policy_Route",
                                                      null,
                                                      null,
                                                      true,
                                                      true);
                route.createObject(context, DomainConstants.TYPE_ROUTE, strRouteName, null, DomainObject.POLICY_ROUTE, null);
                strRouteId = route.getId();

                // Connect route to the owner
                route.connect(context, new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_ROUTE), true, objPerson);

                // Connect route to the route template
                route.connectTemplate(context, strRouteTemplateId);

                //Getting the current auto stop on rejection attribute for the ROUTE
                String sAutoStopOnRejection = (String)domRouteTemplateObject.getInfo(context,SELECT_ATTRIBUTE_AUTO_STOP_REJECTION);
                String sRouteBasePurpose = (String)domRouteTemplateObject.getInfo(context,SELECT_ATTRIBUTE_ROUTE_BASE_PURPOSE);
                String sRouteScope = (String)domRouteTemplateObject.getInfo(context,SELECT_ATTRIBUTE_ROUTE_SCOPE);
                                

                // Find all the attributes on Route Node relationship and form select list
                mapRelAttributes = DomainRelationship.getTypeAttributes(context, DomainObject.RELATIONSHIP_ROUTE_NODE);
                StringList slRelSelects = new StringList();
                for (Iterator itrAttributes = mapRelAttributes.keySet().iterator(); itrAttributes.hasNext();) {
                    slRelSelects.add(DomainRelationship.getAttributeSelect((String)itrAttributes.next()));
                }//for

                // Form the bus select
                StringList slBusSelects = new StringList(DomainObject.SELECT_ID);
                slBusSelects.add(DomainObject.SELECT_TYPE);

                // Expand and find the route template tasks, users
                DomainObject dmoRouteTemplate = new DomainObject(strRouteTemplateId);

                strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;
                strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
                boolean getTo = false;
                boolean getFrom = true;
                strRelWhere = null;
                
                MapList mlTasks = dmoRouteTemplate.getRelatedObjects(context,
                                                                     strRelPattern,
                                                                     strTypePattern,
                                                                     slBusSelects,
                                                                     slRelSelects,
                                                                     getTo,
                                                                     getFrom,
                                                                     nRecurseToLevel,
                                                                     strObjectWhere,
                                                                     strRelWhere);

                Map mapTask                 = null;
                dmoRelationship             = null;
                DomainObject toObject       = null;
                String strUserId            = null;
                String strUserType          = null;
                String strAttributeName     = null;
                String strAttributeValue    = null;
                String strRouteTaskUser     = null;
                String strTitle             = null;                
                Map mapRelAttributesToSet   = null;

                // We could have found all the user objects and connect them simultaneously. 
                // But there might be multiple tasks to a same user. In this scenario, when all the users are connected,
                // we will not know for which relationship attributes are to be updated. 
                // So find one user (i.e. task) and complete it, then go for next user (i.e. task).
                for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) {

                    // Get each task in route template
                    mapTask = (Map)itrTasks.next();

                    // Create the same tasks for route object
                    strUserId = (String)mapTask.get(DomainObject.SELECT_ID);
                    strUserType = (String)mapTask.get(DomainObject.SELECT_TYPE);
                    
                    // Bug 347000 : Check if the assignee is present and task title is provided
                    strRouteTaskUser = (String)mapTask.get(DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_TASK_USER));
                    strTitle = (String)mapTask.get(DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_TITLE));
                    if((DomainObject.TYPE_ROUTE_TASK_USER).equals(strUserType)){
                        if (strRouteTaskUser == null || "".equals(strRouteTaskUser.trim())) {
                            throw new Exception(STRING_APPROVER_CANNOT_BE_ADDED_INVALID_ROUTE_TEMPLATE);
                        }
                    }
                    if (strTitle == null || "".equals(strTitle.trim())) {
                        throw new Exception(STRING_APPROVER_CANNOT_BE_ADDED_INVALID_ROUTE_TEMPLATE);
                    }
                    
                    //If the task is of RTU type then create new RTU
                    if((DomainObject.TYPE_ROUTE_TASK_USER).equals(strUserType)){
                        toObject = DomainObject.newInstance(context);
                        toObject.createObject(context, DomainObject.TYPE_ROUTE_TASK_USER, null, null, null, null);
                    }
                    else{
                        //Normal task
                        toObject = new DomainObject(strUserId);
                    }

                    dmoRelationship = DomainRelationship.connect(context, route, DomainObject.RELATIONSHIP_ROUTE_NODE, toObject);

                    // Copy all the attributes from route template Route Node relationship to Route
                    mapRelAttributesToSet = new HashMap();
                    for (Iterator itrAttributes = mapRelAttributes.keySet().iterator(); itrAttributes.hasNext();) {
                        strAttributeName = (String)itrAttributes.next();
                        strAttributeValue = (String)mapTask.get(DomainRelationship.getAttributeSelect(strAttributeName));
                        mapRelAttributesToSet.put(strAttributeName, strAttributeValue);
                    }//for

                    dmoRelationship.setAttributeValues(context, mapRelAttributesToSet);
                }//for

                // Add this object as content to this route
                mapObjectToState.put(strObjectId, strState);
                DomainObject domainObject = new DomainObject(strObjectId);
                String strCurrentStateOfObject = domainObject.getInfo(context, DomainObject.SELECT_CURRENT);

                dmoRelationship = DomainRelationship.connect(context, domainObject, DomainObject.RELATIONSHIP_OBJECT_ROUTE, route);

                String strPolicyName = domainObject.getInfo(context, DomainObject.SELECT_POLICY);
                String strStateNameSymbolic = FrameworkUtil.reverseLookupStateName(context, strPolicyName, strState);
                String strPolicyNameSymbolic = FrameworkUtil.getAliasForAdmin(context,"Policy", strPolicyName, true);
                
                mapRelAttributesNew = new HashMap();
                mapRelAttributesNew.put(DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY, strPolicyNameSymbolic);
                mapRelAttributesNew.put(DomainObject.ATTRIBUTE_ROUTE_BASE_STATE, strStateNameSymbolic);
                dmoRelationship.setAttributeValues(context, mapRelAttributesNew);

                //Setting attribute Auto stop on rejection
                route.setAttributeValue(context, ATTRIBUTE_AUTO_STOP_ON_REJECTION, sAutoStopOnRejection);
                route.setAttributeValue(context, ATTRIBUTE_ROUTE_BASE_PURPOSE, sRouteBasePurpose);
                route.setAttributeValue(context, sAttrRestrictMembers, sRouteScope);
                
                
                
                
                // Start the route if this is the current state of the object
                if (strCurrentStateOfObject.equals(strState)) {
                    isRouteToBeStarted = true;
                }
            }
            else {
               //Route already exists  then use it.
               // Filter finished Routes
               Map mapTemp      = null;
               MapList mlTemp   = new MapList();

               for (Iterator itrActiveTasks = mlRoutes.iterator(); itrActiveTasks.hasNext();) {
                    mapTemp = (Map) itrActiveTasks.next();
                    if (!ROUTE_FINISHED.equals((String)mapTemp.get(SELECT_ATTRIBUTE_ROUTE_STATUS))) {
                        mlTemp.add(mapTemp);
                    }
               }

               mlRoutes = mlTemp;

               if (mlRoutes.size() == 0){
                   //All routes are finished Routes
                   emxContextUtil_mxJPO.mqlError(context, COMPLETED_ROUTE);
                   ContextUtil.abortTransaction(context);
                   return 0;
                }
                else {
                    //Non Finished ROUTES processing
                    String strRouteStatus = null;
                    Map mapRouteInfo = null;
                    Map mapStartedRoute = null;
                    Map mapNotStartedRoute = null;
                    Map mapStoppededRoute = null;
                    for(Iterator itrActiveRoutes = mlRoutes.iterator(); itrActiveRoutes.hasNext();){
                        mapRouteInfo = (Map)itrActiveRoutes.next();
                        strRouteStatus = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS);
                        //Find if started status
                        if("Started".equals(strRouteStatus)){
                            mapStartedRoute = mapRouteInfo;
                        }
                        //Find if Not Started status
                        else if("Not Started".equals(strRouteStatus)){
                            mapNotStartedRoute = mapRouteInfo;
                        }
                        //Find if Stopped status
                        else if("Stopped".equals(strRouteStatus)){
                            mapStoppededRoute = mapRouteInfo;
                        }
                    }

                    //set Route to be used
                    Map mapRouteToBeUsed = null;
                    if(mapStartedRoute != null) {
                        mapRouteToBeUsed = mapStartedRoute;
                    }
                    else if(mapStoppededRoute != null) {
                        mapRouteToBeUsed = mapStoppededRoute;
                    }
                    else if(mapNotStartedRoute != null){
                        mapRouteToBeUsed = mapNotStartedRoute;
                    }

                    if (mapRouteToBeUsed == null) {
                        String[] formatArgs = {strObjectId,strState};
                        String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.LifeCycle.UsableStateBasedRouteNotFound",formatArgs);
                        throw new Exception(message);
                    }

                    //end of Route to be used setting

                    strRouteId  = (String)mapRouteToBeUsed.get(DomainObject.SELECT_ID);
                    route       = new com.matrixone.apps.common.Route(strRouteId);

                    // Find all the attributes on Route Node relationship and form select list
                    mapRelAttributes.clear();
                    mapRelAttributes = DomainRelationship.getTypeAttributes(context, DomainObject.RELATIONSHIP_ROUTE_NODE);
                    StringList slRelSelects = new StringList();

                    for (Iterator itrAttributes = mapRelAttributes.keySet().iterator(); itrAttributes.hasNext();) {
                        slRelSelects.add(DomainRelationship.getAttributeSelect((String)itrAttributes.next()));
                    }//for

                    //Getting the current sequence number for the ROUTE
                    String strCurrentRouteSequenceNo = route.getInfo(context,SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);

                    //Finding tasks related to ROUTE Template
                    slRelSelects.add(DomainRelationship.SELECT_ID);
                    slRelSelects.add(SELECT_REL_ATTRIBUTE_ROUTE_SEQUENCE);

                    StringList slBusSelects = new StringList(DomainObject.SELECT_ID);
                    slBusSelects.add(DomainObject.SELECT_TYPE);

                    DomainObject dmoRouteTemplate = new DomainObject(strRouteTemplateId);

                    strTypePattern  = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;
                    strRelPattern   = DomainObject.RELATIONSHIP_ROUTE_NODE;
                    boolean getTo   = false;
                    boolean getFrom = true;
                    nRecurseToLevel = (short)1;
                    strObjectWhere  = null;
                    strRelWhere     = null;

                    MapList mlTasks = dmoRouteTemplate.getRelatedObjects(context,
                                                                         strRelPattern,
                                                                         strTypePattern,
                                                                         slBusSelects,
                                                                         slRelSelects,
                                                                         getTo,
                                                                         getFrom,
                                                                         nRecurseToLevel,
                                                                         strObjectWhere,
                                                                         strRelWhere);

                    Map mapTask = null;
                    dmoRelationship = null;
                    DomainObject fromObject = route;
                    DomainObject toObject = null;
                    String strUserId = null;
                    String strAttributeName = null;
                    String strAttributeValue = null;
                    Map mapRelAttributesToSet = null;
                    String strUserType= null;
                    String currTaskSeqStr= null;

                    //Iterating through each task
                    for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) {

                        mapTask = (Map)itrTasks.next();

                        strUserId                   = (String)mapTask.get(DomainObject.SELECT_ID);
                        strUserType                 = (String)mapTask.get(DomainObject.SELECT_TYPE);
                        currTaskSeqStr              = (String)mapTask.get(SELECT_REL_ATTRIBUTE_ROUTE_SEQUENCE);

                        //If the task is of RTU type then create new RTU
                        if((DomainObject.TYPE_ROUTE_TASK_USER).equals(strUserType)){
                            toObject = DomainObject.newInstance(context);
                            toObject.createObject(context, DomainObject.TYPE_ROUTE_TASK_USER, null, null, null, null);
                        }
                        else{
                            //Normal task
                            toObject = new DomainObject(strUserId);
                        }

                        //connect assinee to Route
                        dmoRelationship = DomainRelationship.connect(context, fromObject, DomainObject.RELATIONSHIP_ROUTE_NODE, toObject);

                        //Copy all the attributes from route template Route Node relationship to Route
                        mapRelAttributesToSet = new HashMap();
                        for (Iterator itrAttributes = mapRelAttributes.keySet().iterator(); itrAttributes.hasNext();) {
                            strAttributeName = (String)itrAttributes.next();
                            strAttributeValue = (String)mapTask.get(DomainRelationship.getAttributeSelect(strAttributeName));
                            mapRelAttributesToSet.put(strAttributeName, strAttributeValue);
                        }//for

                        //set all attributes to new Route
                        dmoRelationship.setAttributeValues(context, mapRelAttributesToSet);

                        //Check for Route Status
                        strRouteStatus = (String)mapRouteToBeUsed.get(SELECT_ATTRIBUTE_ROUTE_STATUS);

                        //
                        // Add the new tasks to the route and adjust the order of the tasks
                        // If route is not started then add tge new tasks at their own orders, else
                        // add the tasks from the current level of the route.
                        //
                        int nNewSequenceNumber = 1;
                        if("Not Started".equals(strRouteStatus)){
                            nNewSequenceNumber = Integer.parseInt(currTaskSeqStr);
                        }
                        else {
                            nNewSequenceNumber = Integer.parseInt(strCurrentRouteSequenceNo) + Integer.parseInt(currTaskSeqStr) - 1;
                        }

                        dmoRelationship.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_SEQUENCE, String.valueOf(nNewSequenceNumber));
                    }//for

                    //Activate tasks on current level accordingly
                    if("Started".equals(strRouteStatus)){
                        isTaskOnCurrentLevelToBeStarted = true;
                    }
                    else if ("Stopped".equals(strRouteStatus)) {
                        isTaskOnCurrentLevelToBeStartedForStoppedRoute = true;
                    }
                }
           }

            ///////////////////////////////////////////////////////////////////////////
            // Update the Route Node Id attribute on the relationships
            //
            strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
            strTypePattern = "*";
            StringList slBusSelects = new StringList();
            StringList slRelSelects = new StringList(DomainRelationship.SELECT_ID);
            boolean getTo = false;
            boolean getFrom = true;
            nRecurseToLevel = (short)1;
            strObjectWhere = "";
            strRelWhere = "";

            MapList mlRouteNodeRelInfo = route.getRelatedObjects(context,
                                                                strRelPattern,
                                                                strTypePattern,
                                                                slBusSelects,
                                                                slRelSelects,
                                                                getTo,
                                                                getFrom,
                                                                nRecurseToLevel,
                                                                strObjectWhere,
                                                                strRelWhere);
            Map mapRouteNodeRelInfo = null;
            String strRouteNodeRelId = null;
            DomainRelationship dmrRouteNode = null;
            for (Iterator itrRouteNodeRelInfo = mlRouteNodeRelInfo.iterator(); itrRouteNodeRelInfo.hasNext();) {
                mapRouteNodeRelInfo = (Map)itrRouteNodeRelInfo.next();
                strRouteNodeRelId = (String)mapRouteNodeRelInfo.get(DomainRelationship.SELECT_ID);
                dmrRouteNode = new DomainRelationship(strRouteNodeRelId);
                strRouteNodeRelId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", strRouteNodeRelId, "physicalid");
                dmrRouteNode.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_NODE_ID, strRouteNodeRelId);
            }
            //
            //
            ///////////////////////////////////////////////////////////////////////////

            String strCurrentRouteSequenceNo = route.getInfo(context,SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
            if (strCurrentRouteSequenceNo == null || "".equals(strCurrentRouteSequenceNo.trim())) {
                strCurrentRouteSequenceNo = "1";
            }
            if (isRouteToBeStarted) {
                route.promote(context);
                route.setDueDateFromOffsetForGivenLevelTasks(context, 1);
            }
            else if (isTaskOnCurrentLevelToBeStarted) {
                route.setDueDateFromOffsetForGivenLevelTasks(context, Integer.parseInt(strCurrentRouteSequenceNo));
                route.startTasksOnCurrentLevel(context);
            }
            else if (isTaskOnCurrentLevelToBeStartedForStoppedRoute) {
                route.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Started");
                route.setDueDateFromOffsetForGivenLevelTasks(context, Integer.parseInt(strCurrentRouteSequenceNo));
                route.startTasksOnCurrentLevel(context);
                route.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Stopped");
            }

            ContextUtil.commitTransaction(context);

            return nReturnCode;

        }
        catch(Exception exp) {
            ContextUtil.abortTransaction(context);
            throw exp;
        }
    }

    /**
     * Used to remove approver at selected state in Remove Selected Approver functionality under Approvals tab in advance lifecycle page
     * Removes Approvers from the object lifecycle state dynamically.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args - args[0]: the object id
     *             - args[1]: the name of the state
     *             - args[2]: the name of the signature, if signature is selected
     *             - args[3]: the task object id, if task is selected
     *
     * @return 0 means success
     * @throws Exception
     */
    public int removeApproverTask(Context context, String[] args) throws Exception {
        try {
            int nReturnCode = 0;
            String strTaskId = args[3];

            String strLanguage = context.getSession().getLanguage();
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            i18nNow loc = new i18nNow();
            final String STRING_SUBJECT = "${TYPE} &{NAME} " + loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.DeleteRoute.DeleteNotification");
            final String SELECT_ROUTE_ID_FROM_TASK = "from[" + DomainObject.RELATIONSHIP_ROUTE_TASK + "].to.id";
            final String ATTRIBUTE_CURRENT_ROUTE_NODE = PropertyUtil.getSchemaProperty(context,"attribute_CurrentRouteNode");
            final String SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE = "attribute[" + ATTRIBUTE_CURRENT_ROUTE_NODE + "]";
            //final String ROUTE_STATUS_STARTED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Range.Route_Status.Started");
            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
            
            final String  sAttCurrentRouteNode           = PropertyUtil.getSchemaProperty(context, "attribute_CurrentRouteNode");
            final String SELECT_CURRENT_ROUTE_NODE = "attribute[" + sAttCurrentRouteNode + "]";
            final String SELECT_ROUTE_SEQUENCE = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]";
            final String SELECT_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
    		final String SELECT_ATTRIBUTE_TITLE = "attribute[" + DomainConstants.ATTRIBUTE_TITLE+ "]";
            final String SELECT_ROUTE_TASK_EDIT_SETTING = "from[" + DomainObject.RELATIONSHIP_ROUTE_TASK + "].to.from[" +DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"].to.attribute[" + DomainConstants.ATTRIBUTE_TASKEDIT_SETTING + "]";
            final String SELECT_TASK_EDIT_SETTING = "from.from[" +DomainConstants.RELATIONSHIP_INITIATING_ROUTE_TEMPLATE +"].to.attribute[" + DomainConstants.ATTRIBUTE_TASKEDIT_SETTING + "]";

 
            //DomainObject dmoRoute = null;
            MapList mlDefinedTasksOnRoute = null;
            String strRelPattern = null;
            String strTypePattern = null;
            StringList slBusSelect = null;
            StringList slRelSelect = null;
            short nRecurseToLevel = (short)1;
            String strObjectWhere = null;
            String strRelWhere = null;
            final boolean GET_TO = true;
            final boolean GET_FROM = true;
            String strRouteId   = null;
            String strRouteNodeId = null;
            String strAssigneeId = null;
            String strAssigneeType = null;
            Map mapInfo = null;
            String sSubject = null;
            String strRouteName = null;
            StringList slBusObjSelect = null;
            Map mapRouteInfo = null;
            String strRouteStatus = null;
            String strCurrentRouteNode = null;
            String strRevision = null;
            String strName = null;
            String strType = null;
            String strCurrentTaskState = null;

            ContextUtil.startTransaction(context, true);

            DomainObject domainObject = new DomainObject(strTaskId);
            boolean isBusinessObject = "true".equalsIgnoreCase(MqlUtil.mqlCommand(context, "print bus '" + strTaskId + "' select exists dump", true));

            if (isBusinessObject) {

                slBusSelect = new StringList();
                slBusSelect.add(SELECT_ROUTE_ID_FROM_TASK);
                slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                slBusSelect.add(DomainObject.SELECT_CURRENT);
                slBusSelect.add(SELECT_ROUTE_TASK_EDIT_SETTING);
                mapInfo = domainObject.getInfo(context, slBusSelect);

                strRouteId = (String)mapInfo.get(SELECT_ROUTE_ID_FROM_TASK);
                strRouteNodeId = (String)mapInfo.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                strCurrentTaskState = (String)mapInfo.get(DomainObject.SELECT_CURRENT);
                String strEditSetting = (String)mapInfo.get(SELECT_ROUTE_TASK_EDIT_SETTING);
                if("Maintain Exact Task List".equalsIgnoreCase(strEditSetting)){
					String taskRemoveAlert = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.RouteWizard.MaintainExactTaskListTaskRemoveAlert");
					throw new MatrixException(taskRemoveAlert);
                }


                if (strRouteId == null || "".equals(strRouteId) || "null".equals(strRouteId)) {
                    return 0;
                }

                if (POLICY_INBOX_TASK_STATE_COMPLETE.equals(strCurrentTaskState)) {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.CannotPerformeOperationOnCompletedTasks", context.getLocale().getLanguage()));
                }

                
                
                // Delete task
                DomainObject.deleteObjects(context, new String[]{strTaskId});
                strTaskId = null;

                //Delete route node relationship
                DomainRelationship.disconnect(context, strRouteNodeId);
                strRouteNodeId = null;
            }
            else {
                strRouteNodeId = strTaskId;

                StringList relationshipSelects = new StringList("from.id");
                relationshipSelects.add("from.name");
                relationshipSelects.add("to.id");
                relationshipSelects.add("to.type");
                relationshipSelects.add(SELECT_TASK_EDIT_SETTING);
                

                MapList mlRouteNodeInfo = DomainRelationship.getInfo(context, new String[]{strRouteNodeId}, relationshipSelects);
                if (mlRouteNodeInfo == null || mlRouteNodeInfo.size() < 1) {
                    return 0;
                }

                mapInfo = (Map)mlRouteNodeInfo.get(0);
                String strEditSetting = (String)mapInfo.get(SELECT_TASK_EDIT_SETTING);
                if("Maintain Exact Task List".equalsIgnoreCase(strEditSetting)){
 					String taskRemoveAlert = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.RouteWizard.MaintainExactTaskListTaskRemoveAlert");
					throw new MatrixException(taskRemoveAlert);
                }
                strRouteId = (String)mapInfo.get("from.id");
                strRouteName = (String)mapInfo.get("from.name");
                strAssigneeId = (String)mapInfo.get("to.id");
                strAssigneeType = (String)mapInfo.get("to.type");
                
                // Find if the task for this relationship is existing.
                DomainObject dmoRouteObject = new DomainObject(strRouteId);
                MapList mlTasks = dmoRouteObject.getRelatedObjects(context, 
                                                                    DomainObject.RELATIONSHIP_ROUTE_TASK, 
                                                                    DomainObject.TYPE_INBOX_TASK, 
                                                                    new StringList(DomainObject.SELECT_ID), 
                                                                    null, 
                                                                    GET_TO, 
                                                                    !GET_FROM, 
                                                                    nRecurseToLevel,
                                                                    "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]=='" + strRouteNodeId + "'",
                                                                    "");
                // Delete tasks
                if (mlTasks != null && mlTasks.size() > 0) {
                    for (Iterator itrTasks = mlTasks.iterator(); itrTasks.hasNext();) {
                        strTaskId = (String)((Map)itrTasks.next()).get(DomainObject.SELECT_ID);
                        DomainObject.deleteObjects(context, new String[]{strTaskId});
                    }
                }
                
                // Delete route node relationship
                DomainRelationship.disconnect(context, strRouteNodeId);
                strRouteNodeId = null;
            }

            //Find if route has some tasks connected?
            com.matrixone.apps.common.Route route = new com.matrixone.apps.common.Route(strRouteId);

            strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
            strTypePattern = "*";
            slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_ID);
            slRelSelect = new StringList();
            nRecurseToLevel = (short)1;
            strObjectWhere = "";
            strRelWhere = "";
            mlDefinedTasksOnRoute = route.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);

            // If there are no defined tasks on route then delete this route as well.
            if (mlDefinedTasksOnRoute.size() == 0) {
                strRouteName = route.getInfo(context, DomainObject.SELECT_NAME);
                sSubject = FrameworkUtil.findAndReplace(STRING_SUBJECT, "${TYPE}", DomainObject.TYPE_ROUTE);
                sSubject = FrameworkUtil.findAndReplace(sSubject, "${NAME}", strRouteName);

                route.deleteRouteObject(context, "", sSubject);
                strRouteId = null;
            }
            else {
                route.startTasksOnCurrentLevel(context);
            }
            
            //BEGIN:
            //checking for the order of Tasks and updating it for the Route
            if (strRouteId != null){
                DomainObject dmoRouteObject = new DomainObject(strRouteId);
                
                //Finding Route Information
                slBusObjSelect = new StringList();
                slBusObjSelect.add(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
                slBusObjSelect.add(Route.SELECT_ROUTE_STATUS);
                slBusObjSelect.add(DomainObject.SELECT_TYPE);
                slBusObjSelect.add(DomainObject.SELECT_NAME);
                slBusObjSelect.add(DomainObject.SELECT_REVISION);
                
                mapInfo = null;
                mapInfo = dmoRouteObject.getInfo(context, slBusObjSelect);
                String strCurrentRouteLevel = (String)mapInfo.get(SELECT_CURRENT_ROUTE_NODE);
                strType = (String)mapInfo.get(DomainObject.SELECT_TYPE);
                strName = (String)mapInfo.get(DomainObject.SELECT_NAME);
                strRevision = (String)mapInfo.get(DomainObject.SELECT_REVISION);
                strRouteStatus = (String)mapInfo.get(Route.SELECT_ROUTE_STATUS);
                
                slBusObjSelect = new StringList();
                slRelSelect = new StringList();
                slRelSelect.add(SELECT_ROUTE_NODE_ID);
                slRelSelect.add(SELECT_ROUTE_SEQUENCE);

                strRelPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
                strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER ;
                String strBusWhere = "";
                strRelWhere = "";
                short nRecurseLevel = (short)1;

                MapList mlRouteNodes = dmoRouteObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusObjSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);
                if(mlRouteNodes.size() != 0){
                    //sorting Maplist according to its sequence
                    mlRouteNodes.sort(SELECT_ROUTE_SEQUENCE, "ascending", "integer");
                    Map mapTaskInfo = null;
                    int nLastUpdatedSequence = 0;
                    int nExpectedSequence = 0;
                    String strCurrentSequence = null;
                    int nCurrentSequence = 0;
                    DomainRelationship dmrObject = null;
                    
                    //Resequencing the Route Sequence
                    for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
                        mapTaskInfo = (Map) itrRouteNodes.next();
                        strCurrentSequence = (String)mapTaskInfo.get(SELECT_ROUTE_SEQUENCE);
                        strRouteNodeId = (String)mapTaskInfo.get(SELECT_ROUTE_NODE_ID);
                        
                        dmrObject = new DomainRelationship(strRouteNodeId);
                        nCurrentSequence = Integer.parseInt(strCurrentSequence);
                        if(nCurrentSequence != nExpectedSequence){
                            if(nCurrentSequence == (nExpectedSequence+1)){
                                if(nLastUpdatedSequence == nCurrentSequence){
                                    //updating the sequence
                                    dmrObject.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_SEQUENCE , String.valueOf(nExpectedSequence));
                                }
                                else{
                                    nExpectedSequence++;
                                }
                            }
                            else{
                                if(nCurrentSequence != nLastUpdatedSequence){
                                    nExpectedSequence++;
                                    nLastUpdatedSequence = nCurrentSequence;
                                }
                                dmrObject.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_SEQUENCE , String.valueOf(nExpectedSequence));
                            }
                        }
                    }
                    // To find if there are tasks on the current level which are NOT complete, in that case Route seq should not change
                    strRelWhere = SELECT_ATTRIBUTE_APPROVAL_STATUS+" == \"\" && "+SELECT_ATTRIBUTE_ROUTE_SEQUENCE+" == \""+(strCurrentRouteLevel)+"\"";
                    mlDefinedTasksOnRoute = route.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseToLevel, strObjectWhere, strRelWhere);
                    
                    if(mlDefinedTasksOnRoute.isEmpty()){
                    	strCurrentRouteLevel = strCurrentRouteLevel+1;
                    }
                    
                    //for activating the Resequenced Tasks on current level
                    String[] strMethodArguments = new String[] {
                            strType, strName, strRevision, String.valueOf(strCurrentRouteLevel), "0"
                        };
                    //Checking the Route Status
                    if("Started".equals(strRouteStatus)){
                        nReturnCode = emxCommonInitiateRoute_mxJPO.InitiateRoute(context, strMethodArguments);
                    }
                    else if("Stopped".equals(strRouteStatus)){
                        dmoRouteObject.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Started");
                        nReturnCode = emxCommonInitiateRoute_mxJPO.InitiateRoute(context, strMethodArguments);
                        dmoRouteObject.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Stopped");
                    }
                }
            }
            
            //END:
            ContextUtil.commitTransaction(context);

            return nReturnCode;
        }
        catch(Exception exp) {
            ContextUtil.abortTransaction(context);

            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Status Gif For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getStatusGifForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String strInfoType = null;
            String strDueDate = null;
            Date dtDueDate = null;
            Date dtCurrentDate = new Date();

            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
            final String POLICY_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");

            final String GREEN_VISUAL = "<img border='0' src='../common/images/iconStatusGreen.gif' name='red' id='red'/>";
            final String RED_VISUAL = "<img border='0' src='../common/images/iconStatusRed.gif' name='red' id='red'/>";
            final String YELLOW_VISUAL = "<img border='0' src='../common/images/iconStatusYellow.gif' name='yellow' id='yellow'/>";
            final String NO_VISUAL = "";

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();

                // Get the type of the task
                strInfoType = (String)mapObjectInfo.get("infoType");

                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    // If the signature is complete then show green visual
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        vecResult.add(GREEN_VISUAL);
                    }
                    else {
                        // If the signature is late then show red visual else show no visual
                        strDueDate = (String)mapObjectInfo.get("dueDate");

                        // If no due date is mentioned then do not show any visual
                        if (strDueDate == null || "".equals(strDueDate.trim())) {
                            vecResult.add(NO_VISUAL);
                        }
                        else {
                            // Is it late?
                            dtDueDate = eMatrixDateFormat.getJavaDate(strDueDate);
                            if (dtDueDate != null && dtCurrentDate.after(dtDueDate)) {
                                vecResult.add(RED_VISUAL);
                            }
                            else {
                                vecResult.add(NO_VISUAL);
                            }
                        }
                    }
                }
                else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    // If the task is late then show red visual else show no visual
                    strDueDate = (String)mapObjectInfo.get("dueDate");

                    // If no due date is mentioned then do not show any visual
                    if (strDueDate == null || "".equals(strDueDate.trim())) {
                        vecResult.add(NO_VISUAL);
                    }
                    else {
                        // Is it late?
                        dtDueDate = eMatrixDateFormat.getJavaDate(strDueDate);
                        if (dtDueDate != null && dtCurrentDate.after(dtDueDate)) {
                            vecResult.add(RED_VISUAL);
                        }
                        else {
                            vecResult.add(NO_VISUAL);
                        }
                    }
                }
                else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                    // If the task is complete then show green visual
                    if (POLICY_INBOX_TASK_STATE_COMPLETE.equals((String)mapObjectInfo.get("currentState"))) {
                        vecResult.add(GREEN_VISUAL);
                    }
                    else if (POLICY_INBOX_TASK_STATE_REVIEW.equals((String)mapObjectInfo.get("currentState"))) {
                        vecResult.add(YELLOW_VISUAL);
                    }
                    else {
                        // If the task is late then show red visual else show no visual
                        strDueDate = (String)mapObjectInfo.get("dueDate");

                        // If no due date is mentioned then do not show any visual
                        if (strDueDate == null || "".equals(strDueDate.trim())) {
                            vecResult.add(NO_VISUAL);
                        }
                        else {
                            // Is it late?
                            dtDueDate = eMatrixDateFormat.getJavaDate(strDueDate);
                            if (dtDueDate != null && dtCurrentDate.after(dtDueDate)) {
                                vecResult.add(RED_VISUAL);
                            }
                            else {
                                vecResult.add(NO_VISUAL);
                            }
                        }
                    }
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }
             //XSSOK
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Name For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getNameForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

            
//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification
            
            Map mapObjectInfo = null;
            String strInfoType = null;
            String strName = null;
            StringBuffer strHTMLBuffer = null;
            String strObjectId = null;
            String strRouteId = null;
            String strRouteNodeId = null;

            StringBuffer strTreeLink = new StringBuffer();
            strTreeLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?relId=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("relId")));
            strTreeLink.append("&parentOID=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("parentOID")));
            strTreeLink.append("&jsTreeID=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
            strTreeLink.append("&suiteKey=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
            strTreeLink.append("&emxSuiteDirectory=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
            strTreeLink.append("&objectId=${OBJECT_ID}&taskName=${NAME}");
            strTreeLink.append("', '', '', 'false', 'popup', '')\"  class=\"object\">");
            strTreeLink.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">${NAME}</a>");

            StringBuffer strTaskPropertiesLink = new StringBuffer(64);
            strTaskPropertiesLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../components/emxRouteTaskDetailsFS.jsp?routeId=${ROUTE_ID}&taskCreated=no&routeNodeId=${ROUTE_NODE_ID}");
            strTaskPropertiesLink.append("&suiteKey=");
            strTaskPropertiesLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
            strTaskPropertiesLink.append("&emxSuiteDirectory=");
            strTaskPropertiesLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
            strTaskPropertiesLink.append("', '800', '575', 'false', 'popup', '')\"  class=\"object\">");
            strTaskPropertiesLink.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">${NAME}</a>");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strName = (String)mapObjectInfo.get("name");
                strInfoType = (String)mapObjectInfo.get("infoType");
                String languageStr = context.getSession().getLanguage();
                String updatedStrName=strName.replace(' ', '_');
				Locale strLocale = new Locale(languageStr);
                

                // The Task name will be hyperlinked to popup the task details.
                // If task name is clicked, task tree and properties page will open in a popup.
                // If the item is a signature, the signature name is displayed without hyperlink.
                strHTMLBuffer = new StringBuffer(64);
                
//              Begin : Bug 346997 code modification
                if (isExporting) {
                    if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                        String i18NString= "emxFramework.Lifecycle."+updatedStrName;
                        String returnString = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, i18NString);
                           if((returnString.indexOf("emxFramework")!= -1) ||(returnString==null) ||("".equals(returnString)) ) {
                               strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strName));  
                           }
                           else{
                               strHTMLBuffer.append(returnString);
                               }
                    }
                    else {
                        if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                            strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strName));
                        }
                        else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                            strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strName));
                        }
                        else {
                            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                        }
                    }
                }
                else {
                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    strHTMLBuffer.append("<img src=\"images/iconSmallSignature.gif\" border=\"0\" align=\"left\">");
                    String i18NString= "emxFramework.Lifecycle."+updatedStrName;
                    String returnString = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, i18NString);
                    if((returnString.indexOf("emxFramework")!= -1) ||(returnString==null) ||("".equals(returnString)) ) {
                           strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strName));  
                    }
                    else{
                         strHTMLBuffer.append(returnString);
                    }
                }
                else {

                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        strObjectId = (String)mapObjectInfo.get("taskId");
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTreeLink.toString(), "${OBJECT_ID}", strObjectId));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", strName));
                    }
                    else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                        strObjectId = (String)mapObjectInfo.get("routeNodeId");
                        strRouteId = (String)mapObjectInfo.get("routeId");
                        strRouteNodeId = (String)mapObjectInfo.get("routeNodeId");
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTaskPropertiesLink.toString(), "${ROUTE_ID}", strRouteId));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", strName));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${ROUTE_NODE_ID}", strRouteNodeId));
                    }
                    else {
                        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                    }
                }
                }
//              End : Bug 346997 code modification
                
                

                vecResult.add(strHTMLBuffer.toString());
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Approver For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getApproverForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap paramMap = (HashMap)programMap.get("paramList");
            String strInfoType = null;

            // For i18nNow string formation
            i18nNow loc = new i18nNow();
            String strLanguage  = (String)paramMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            final String STRING_ROLE = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Common.Role");
            final String STRING_GROUP = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Common.Group");

            String strApprovers = null;;
            String strRoleOrGroup = null;
            StringList slApprovers = null;
            StringList slTranslatedApprovers = null;
            Group groupObj = null;
            Role roleObj = null;

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");

                if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType) || INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {

                    // Decide the name of the approver
                    if (DomainObject.TYPE_ROUTE_TASK_USER.equals(mapObjectInfo.get("assigneeType"))) {
                        // If the type of the assignee is RTU then get the name of the role/group as approver name
                        String strRouteTaskUser = (String)mapObjectInfo.get("routeTaskUser");
                        if (strRouteTaskUser == null || "".equals(strRouteTaskUser.trim()) || "null".equals(strRouteTaskUser)) {
                            vecResult.add("");
                        }
                        else {
                            if (strRouteTaskUser.startsWith("role_")) {
                                strRouteTaskUser = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);//Symbolic->Real name
                                strRouteTaskUser = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Role." + FrameworkUtil.findAndReplace(strRouteTaskUser, " ", "_"));
                                strRouteTaskUser = strRouteTaskUser + "(" + STRING_ROLE + ")";
                                vecResult.add(strRouteTaskUser);
                            }
                            else if (strRouteTaskUser.startsWith("group_")) {
                                strRouteTaskUser = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);//Symbolic->Real name
                                strRouteTaskUser = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Group." + FrameworkUtil.findAndReplace(strRouteTaskUser, " ", "_"));
                                strRouteTaskUser = strRouteTaskUser + "(" + STRING_GROUP + ")";
                                vecResult.add(strRouteTaskUser);
                            }
                            else {
                                vecResult.add("");
                            }
                        }
                    }
                    else {
                        // If the type of the assignee is not RTU then it is person and get the name of the person as approver name
                        vecResult.add((String)mapObjectInfo.get("assigneeName"));
                    }
                }
                else if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        vecResult.add((String)mapObjectInfo.get("approver"));
                    }
                    else {
                        strApprovers = (String)mapObjectInfo.get("approver");
                        slApprovers = FrameworkUtil.split(strApprovers, ",");
                        // Filter the list to avoid duplicate names
                        Set uniqueApprovers = new HashSet(slApprovers);
                        slApprovers = new StringList();
                        for (Iterator itrUniqueNames = uniqueApprovers.iterator(); itrUniqueNames.hasNext();) {
                            slApprovers.add((String) itrUniqueNames.next());
                        }
                        
                        slTranslatedApprovers = new StringList();

                        strRoleOrGroup = null;
                        groupObj = null;
                        roleObj = null;

                        // Find all the parent role and group hierarchy
                        for (Iterator itrApprovers = slApprovers.iterator(); itrApprovers.hasNext();) {
                            strRoleOrGroup = (String) itrApprovers.next();

                            //  //Check if its Role
                            try {
                                if(isRoleOrGroup(context,"Role",strRoleOrGroup)){
                                    roleObj = new Role(strRoleOrGroup);
                                    roleObj.open(context);
    
                                    // Internationalize this value
                                    strRoleOrGroup = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Role." + FrameworkUtil.findAndReplace(strRoleOrGroup, " ", "_"));
                                    strRoleOrGroup = strRoleOrGroup + "(" + STRING_ROLE + ")";
                                    slTranslatedApprovers.add(strRoleOrGroup);
    
                                    roleObj.close(context);
                                }
                                //Check if its Group
                                else if(isRoleOrGroup(context,"Group",strRoleOrGroup)){
                                    groupObj = new Group(strRoleOrGroup);
                                    groupObj.open(context);

                                    // Internationalize this value
                                    strRoleOrGroup = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Group." + FrameworkUtil.findAndReplace(strRoleOrGroup, " ", "_"));
                                    strRoleOrGroup = strRoleOrGroup + "(" + STRING_GROUP + ")";
                                    slTranslatedApprovers.add(strRoleOrGroup);

                                    groupObj.close(context);
                                }
                                //If not Role and Group 
                                else{
                                    slTranslatedApprovers.add(strRoleOrGroup);
                                }
                            } catch (MatrixException me){
                            }
                        }

                        vecResult.add(FrameworkUtil.join(slTranslatedApprovers, ","));
                    }
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Title For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getTitleForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map mapObjectInfo = null;
            String strInfoType = null;
            String strTitle = null;

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");
                strTitle = (String)mapObjectInfo.get("title");

                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    vecResult.add("");
                }
                else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType) || INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    vecResult.add(strTitle);
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Approval Status For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getApprovalStatusForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            
            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
            final String POLICY_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");

            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification
            
            String jsTreeID = (String)paramList.get("jsTreeID");
            Map mapObjectInfo = null;
            String strInfoType = null;
            String strCurrentState = null;
            String strRouteTaskUser = null;
            String strMQL = null;
            String strContextUser = context.getUser();
            String strContextUserId = PersonUtil.getPersonObjectID(context);
            String strAssigneeId = null;
            String strTaskId = null;
            String strLanguage = (String)paramList.get("languageStr");
            String strRoleOrGroupName = null;
            String strCurrentRouteStatus = null;
            String strTaskOrder = null;
            String strRouteNodeId = null;
            String strParentObjectId = null;
            String strParentObjectState = null;
            boolean isToBeAccepted = false;
            MapList mlRelInfo = null;
            Map mapRelInfo = null;
            String strApprovalStatus = null;

            // Find the status strings to be shown
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            i18nNow loc = new i18nNow();
            final String STRING_COMPLETED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Completed");
            final String STRING_APPROVED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Approved");
            final String STRING_REJECTED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Rejected");
            final String STRING_ABSTAINED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Abstained");
            final String STRING_NEEDS_REVIEW = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.NeedsReview");
            final String STRING_IGNORED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Ignored");
            final String STRING_PENDING = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Pending");
            final String STRING_PENDING_ORDER = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.PendingOrder");
            //final String STRING_APPROVE = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Approve");
            final String STRING_ACCEPT = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Accept");
            final String STRING_AWAITING_APPROVAL = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.AwaitingApproval");
            final String STRING_ROUTE_STOPPED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.RouteStopped");
            
            // Form the Accept link template
            String sAcceptURL = "'../components/emxRouteAcceptTask.jsp?taskId=${OBJECT_ID}'";
			StringBuffer strAcceptLink = new StringBuffer(64);
			strAcceptLink.append("<a href=\"javascript:submitWithCSRF("+sAcceptURL+", findFrame(getTopWindow(),'listHidden'));\">");
			strAcceptLink.append(STRING_ACCEPT);
			strAcceptLink.append("</a>");


            //
            // Form the Approve link template
			
			
            StringBuffer strTaskApproveLink = new StringBuffer(64);
            String sTaskApproveURL = "'../common/emxLifecycleApproveRejectPreProcess.jsp?emxTableRowId=${OBJECT_ID}^${STATE}^^${TASK_ID}&objectId=${OBJECT_ID}&suiteKey=Framework&jsTreeId="+XSSUtil.encodeForURL(context, jsTreeID)+" '";
			strTaskApproveLink.append("<a href=\"javascript:submitWithCSRF("+sTaskApproveURL+", findFrame(getTopWindow(),'listHidden'));\">");
			strTaskApproveLink.append("<img border='0' src='../common/images/iconActionApprove.png' />");
			strTaskApproveLink.append(STRING_AWAITING_APPROVAL);
			strTaskApproveLink.append("</a>");
            

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");
                strCurrentRouteStatus = (String)mapObjectInfo.get("routeStatus");
                strRouteNodeId = (String)mapObjectInfo.get("routeNodeId");
                strParentObjectId = (String)mapObjectInfo.get("parentObjectId");
                strParentObjectState = (String)mapObjectInfo.get("parentObjectState");
                strApprovalStatus = (String)mapObjectInfo.get("approvalStatus");
                    
                // The Approval Status reflect Completed for completed tasks;
                // Approved, Rejected or Ignored (if applicable) for tasks/signatures.
                // For unsigned tasks or signatures, the status will reflect "Pending" till the task is completed.
                // If the user has privilege to approve, then the status will reflect Approve
                // and hyperlinked will allow the user to approve.
                // The approve action will be enabled for both signatures and route tasks.
                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    // If signature is completed then show accordingly else show the links for operations
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        if ("true".equalsIgnoreCase((String)mapObjectInfo.get("approved"))) {
                            vecResult.add(STRING_APPROVED);
                        }
                        else if ("true".equalsIgnoreCase((String)mapObjectInfo.get("rejected"))) {
                            vecResult.add(STRING_REJECTED);
                        }
                        else if ("true".equalsIgnoreCase((String)mapObjectInfo.get("ignored"))) {
                            vecResult.add(STRING_IGNORED);
                        }
                    }
                    else {
                        vecResult.add((String)mapObjectInfo.get("approvalStatus"));
                    }
                }
                else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                    // Show Completed for active task else show status depending on some things
                    strCurrentState = (String)mapObjectInfo.get("currentState");
                    if (POLICY_INBOX_TASK_STATE_COMPLETE.equals(strCurrentState)) {
                        if ("Approve".equals(strApprovalStatus)) {
                            vecResult.add(STRING_APPROVED);
                        }
                        else if ("Reject".equals(strApprovalStatus)) {
                            vecResult.add(STRING_REJECTED);
                        } 
                        else if ("Abstain".equals(strApprovalStatus)) {
                            vecResult.add(STRING_ABSTAINED);
                        }
                        else {
                            // Show status as completed
                            vecResult.add(STRING_COMPLETED);
                        }
                    }
                    //START BUG 346838
                    else if(POLICY_INBOX_TASK_STATE_REVIEW.equals(strCurrentState)){
                        vecResult.add(STRING_NEEDS_REVIEW);
                        //END BUG 346838
                    }
                    else {
                        // Show "Route Stopped" for Stopped Route.
                        if ("Stopped".equals(strCurrentRouteStatus)) {
                            vecResult.add(STRING_ROUTE_STOPPED);
                        }
                        else {
                            // If the task is not completed yet,
                            // then see if it is assigned to this user then show the approve link,
                            // otherwise show the pending link
                            strAssigneeId = (String)mapObjectInfo.get("assigneeId");
                            strTaskId = (String)mapObjectInfo.get("taskId");
    
                            if (strContextUserId != null && strContextUserId.equals(strAssigneeId)) {
                                
//                              Begin : Bug 346997 code modification
                                if (isExporting) {
                                    vecResult.add(STRING_AWAITING_APPROVAL);
                                }
                                else {
                                String strFormattedLink = FrameworkUtil.findAndReplace(strTaskApproveLink.toString(), "${TASK_ID}", strTaskId);
                                strFormattedLink = FrameworkUtil.findAndReplace(strFormattedLink, "${OBJECT_ID}", strParentObjectId);
                                strFormattedLink = FrameworkUtil.findAndReplace(strFormattedLink, "${STATE}", strParentObjectState);
                                
                                vecResult.add(strFormattedLink);
                            }
//                              End : Bug 346997 code modification
                            }
                            else {
                                strRouteTaskUser = (String)mapObjectInfo.get("routeTaskUser");
                                if (strRouteTaskUser == null || "".equals(strRouteTaskUser)) {
                                    // If Route Task User value is not available then just show pending
                                    vecResult.add(STRING_AWAITING_APPROVAL);
                                }
                                else if (strRouteTaskUser.startsWith("role_") || strRouteTaskUser.startsWith("group_")) {
                                    // Check if the logged in user belongs to the group to which the
                                    // route task is assigned
                                    strRoleOrGroupName = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);
                                    strMQL = "print person  '" + strContextUser + "' select isassigned[" + strRoleOrGroupName + "] dump";
                                    isToBeAccepted = "true".equalsIgnoreCase(MqlUtil.mqlCommand(context, strMQL, true));


// IR-043921V6R2011 - Changes START
                                    boolean isOrgMember = true;
                                    String sRouteId     = (String) mapObjectInfo.get("routeId");
                                    MapList mlOwningOrg = DomainObject.getInfo( context, new String[] { sRouteId }, new StringList( SELECT_OWNING_ORG_ID ));
                                    String sOwningOrgId = (String) ((Map) mlOwningOrg.get(0)).get( SELECT_OWNING_ORG_ID );
                                    if( sOwningOrgId !=null && !"null".equals( sOwningOrgId ) && !"".equals( sOwningOrgId )) {
                                        Organization org = (Organization) DomainObject.newInstance( context, sOwningOrgId );
                                        StringList busSelects = new StringList(2);
                                        busSelects.addElement( DomainConstants.SELECT_ID );
                                        busSelects.addElement( DomainConstants.SELECT_NAME );
                                        String sWhereClause = "( name == \"" + strContextUser + "\" )";
                                        MapList mlMembers = org.getMemberPersons(context, busSelects, sWhereClause, null );
                                        if( !(mlMembers.size() > 0) ) {
                                            isOrgMember = false;
                                        }
                                    }

                                    if ( isToBeAccepted && strRoleOrGroupName.equals((String)mapObjectInfo.get("owner")) && isOrgMember ) {
// IR-043921V6R2011 - Changes END
//                                      Begin : Bug 346997 code modification
                                        if (isExporting) {
                                            vecResult.add(STRING_ACCEPT);
                                        }
                                        else {
                                        vecResult.add(FrameworkUtil.findAndReplace(strAcceptLink.toString(), "${OBJECT_ID}", strTaskId));
                                    }
//                                      End : Bug 346997 code modification
                                    }
                                    else {
                                        vecResult.add(STRING_AWAITING_APPROVAL);
                                    }
                                }
                                else {
                                    // If Route Task User value is not role or group then just show pending
                                    vecResult.add(STRING_AWAITING_APPROVAL);
                                }
                            }
                        }
                    }
                }
                else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    //
                    // If the route for this task is not active then show "Pending" else show "Pending Order <n>"
                    //
                    if ("Started".equals(strCurrentRouteStatus)) {
                        mlRelInfo = DomainRelationship.getInfo(context, new String[]{strRouteNodeId}, new StringList(SELECT_ATTRIBUTE_ROUTE_SEQUENCE));
                        mapRelInfo = (Map)mlRelInfo.get(0);
                        
                        if (mapRelInfo != null) {
                            strTaskOrder = (String)mapRelInfo.get(SELECT_ATTRIBUTE_ROUTE_SEQUENCE);
                        }
                        if (strTaskOrder == null) {
                            strTaskOrder = "";
                        }
                        vecResult.add(STRING_PENDING_ORDER + " " + XSSUtil.encodeForHTML(context, strTaskOrder));
                    }
                    else {
                        vecResult.add(STRING_PENDING);
                    }
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Approval Or Due Date For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getApprovalOrDueDateForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map mapObjectInfo = null;
            String strInfoType = null;

            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");

                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    // For unsigned signature show the due date and for signed show blank
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        vecResult.add("");
                    }
                    else {
                        vecResult.add((String)mapObjectInfo.get("dueDate"));
                    }
                }
                else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                    if (POLICY_INBOX_TASK_STATE_COMPLETE.equals((String)mapObjectInfo.get("currentState"))) {
                        vecResult.add((String)mapObjectInfo.get("completionDate"));
                    }
                    else {
                        vecResult.add((String)mapObjectInfo.get("dueDate"));
                    }
                }
                else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    vecResult.add((String)mapObjectInfo.get("dueDate"));
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }

            // Note: Date conversion is not required since config table column settings does the conversion

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Comments Or Instructions For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getCommentsOrInstructionsForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String strInfoType = null;
            String strInstructions = null;
            String strComments = null;

            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");

                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        strComments = (String)mapObjectInfo.get("comment");
                        if (strComments == null) {
                            strComments = "";
                        }
                        vecResult.add(strComments);
                    }
                    else {
                        // There will not be any comments for the unsigned signatures
                        vecResult.add("");
                    }
                }
                else {
                    strComments = (String)mapObjectInfo.get("comments");
                    strInstructions = (String)mapObjectInfo.get("instructions");
                    if (strComments == null) {
                        strComments = "";
                    }
                    if (strInstructions == null) {
                        strInstructions = "";
                    }

                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        // Show comments for completed tasks else instructions for incomplete tasks
                        if (POLICY_INBOX_TASK_STATE_COMPLETE.equals((String)mapObjectInfo.get("currentState"))) {
                            vecResult.add(strComments);
                        }
                        else {
                            vecResult.add(strInstructions);
                        }
                    }
                    else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                        vecResult.add(strInstructions);
                    }
                    else {
                        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                    }
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Related Object Columns For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception if operation fails
     */
     public  MapList getRelatedObjectColumnsForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Unpack the parameters
            Map mapProgram = (HashMap) JPO.unpackArgs(args);
            Map mapRequest = (Map)mapProgram.get("requestMap");

            // Find object's type
            String strObjectId = (String)mapRequest.get("objectId");
            DomainObject dmoObject = new DomainObject(strObjectId);
            String strType = dmoObject.getInfo(context, DomainObject.SELECT_TYPE);

            // Find configurable parameters if not passed then no need to show these
            // columns
            Lifecycle lifecycle = new Lifecycle();
            Map mapConfParams = lifecycle.getConfigurableParameters(context, strType);
            if (mapConfParams == null) {
                return new MapList();
            }

            // Check if we have anything to show in the related objects columns
            // If we do not have any such information to show then do not show
            // the columns.
            MapList mlObjects = (MapList) mapProgram.get("objectList");
            Map mapObjectInfo = null;
            boolean isRelatedObjectInfoFound = false;
            for (Iterator itrObjects = mlObjects.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                if ("true".equalsIgnoreCase((String)mapObjectInfo.get("isFromRelatedObject"))) {
                    isRelatedObjectInfoFound = true;
                    break;
                }
            }
            if (!isRelatedObjectInfoFound) {
                return new MapList();
            }

            return lifecycle.getRelatedObjectColumnsForTaskSignaturesTable();

        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
        finally {
        }
    }

    /**
     * Gets Related Object Name For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public Vector getRelatedObjectNameForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            Map mapObjectInfo = null;
            String strRelatedObjectName = null;
            String strRelatedObjectType = null;
            String strRelatedObjectSymbolicType = null;
            String strTypeIcon = null;
            String strRelatedObjectId = null;
            String strHref = null;
            //Added by LVC for bug IR-028850V6R2011   	
            
            String strAlias = null;

//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification
            
            StringBuffer strTreeLink = new StringBuffer();
            strTreeLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
            strTreeLink.append("&suiteKey=");
            strTreeLink.append((String)paramList.get("suiteKey"));
            strTreeLink.append("&emxSuiteDirectory=");
            strTreeLink.append((String)paramList.get("SuiteDirectory"));
            strTreeLink.append("&objectId=${OBJECT_ID}");
            strTreeLink.append("', '', '', 'false', 'popup', '')\"><img border=\"0\" src=\"../common/images/${ICON}\">&nbsp;<b>${NAME}</b></a>");


            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                if ("true".equalsIgnoreCase((String)mapObjectInfo.get("isFromRelatedObject"))) {
                    strRelatedObjectId = (String)mapObjectInfo.get("parentObjectId");
                    strRelatedObjectName = (String)mapObjectInfo.get("parentObjectName");
                    strRelatedObjectType = (String)mapObjectInfo.get("parentObjectType");
                    strRelatedObjectSymbolicType = FrameworkUtil.getAliasForAdmin(context, "Type", strRelatedObjectType, false);
                    //strTypeIcon = FrameworkProperties.getProperty("emxFramework.smallIcon." + strRelatedObjectSymbolicType);
                    strTypeIcon = UINavigatorUtil.getTypeIconProperty(context, strRelatedObjectType);
                    //Added by LVC for bug IR-028850V6R2011   	
                    
                    if (strTypeIcon == null || "".equals(strTypeIcon.trim()) || "null".equals(strTypeIcon.trim()))
                    {
                      Vault sVault = context.getVault();
                      while (strTypeIcon == null || "".equals(strTypeIcon.trim()) || "null".equals(strTypeIcon.trim()))
                      {
                        try
                        {
                          BusinessType busType = new BusinessType(strRelatedObjectType, sVault);
                          busType.open(context);

                          strRelatedObjectType = FrameworkUtil.getBaseType(context, strRelatedObjectType, sVault);
                          if (!strRelatedObjectType.equals(""))
                          {
                            strAlias = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, strRelatedObjectType, true);
                            strTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon." + strRelatedObjectSymbolicType);
                          }
                          busType.close(context);
                        }catch(Exception e){}
                      }
                    }
                    
                    //Addition ends for IR-028850V6R2011   	
                    
//                  Begin : Bug 346997 code modification
                    if (isExporting) {
                        vecResult.add(strRelatedObjectName);
                    }
                    else {
                    strHref = FrameworkUtil.findAndReplace(strTreeLink.toString(), "${OBJECT_ID}", strRelatedObjectId);
                    strHref = FrameworkUtil.findAndReplace(strHref, "${NAME}", strRelatedObjectName);
                    strHref = FrameworkUtil.findAndReplace(strHref, "${ICON}", strTypeIcon);

                    vecResult.add(strHref);
                }
//                  End : Bug 346997 code modification
                    
                }
                else {
                    vecResult.add("");
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Related Object State For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getRelatedObjectStateForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

            Map mapObjectInfo = null;
            String strRelatedObjectState = null;
            String strRelatedObjectPolicy = null;
            String strLanguage = (String)paramList.get("languageStr");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                if ("true".equalsIgnoreCase((String)mapObjectInfo.get("isFromRelatedObject"))) {
                    strRelatedObjectState = (String)mapObjectInfo.get("parentObjectState");
                    strRelatedObjectPolicy = (String)mapObjectInfo.get("parentObjectPolicy");

                    strRelatedObjectState = i18nNow.getStateI18NString(strRelatedObjectPolicy, strRelatedObjectState, strLanguage);
                    vecResult.add(strRelatedObjectState);
                }
                else {
                    vecResult.add("");
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Related Object Relationship For Task Signatures Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getRelatedObjectRelationshipForTaskSignatures(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

            Map mapObjectInfo = null;
            String strLanguage = (String)paramList.get("languageStr");
            String strRelationshipName = null;

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                if ("true".equalsIgnoreCase((String)mapObjectInfo.get("isFromRelatedObject"))) {
                    // i18nNow handling
                    strRelationshipName = (String)mapObjectInfo.get("relationship");
                    strRelationshipName = i18nNow.getAdminI18NString("Relationship", strRelationshipName, strLanguage);
                    vecResult.add(strRelationshipName);
                }
                else {
                    vecResult.add("");
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Checkbox For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getCheckboxForMassTasksApproval(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();
            final boolean ENABLECHECKBOX = true;

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                itrObjects.next();

                vecResult.add(String.valueOf(ENABLECHECKBOX));
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Status Gif For Mass Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getStatusGifForMassApproval(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            String stateComplete = FrameworkUtil.lookupStateName(context, DomainObject.POLICY_INBOX_TASK, "state_Complete");

            Date dueDate   = null;
            Date curDate = new Date();
            String statusImageString = "";
            String statusColor= "";
            String taskDueDate = "";
            String taskCompletedDate = "";
            String sTypeName = "";

            // Do for each object
            String taskState = "";
            Map mapObjectInfo = null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                statusImageString = "";
                mapObjectInfo = (Map) itrObjects.next();
                taskState = (String) mapObjectInfo.get(DomainObject.SELECT_CURRENT);
                sTypeName = (String) mapObjectInfo.get(DomainObject.SELECT_TYPE);

                if (!"true".equalsIgnoreCase((String)mapObjectInfo.get("fromApproveAssignedTasks"))) {
                    if ((DomainObject.TYPE_INBOX_TASK).equalsIgnoreCase(sTypeName))
                    {
                        taskDueDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                        taskCompletedDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                    }
                    else if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTypeName))
                    {
                        taskDueDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
                        taskCompletedDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);
                    }
                }
                else {
                    //////////////////////////////////////////////////////////////////////////
                    // When invoked from Approve Assigned Task
                    //////////////////////////////////////////////////////////////////////////
                    String strInfoType = (String)mapObjectInfo.get("infoType");

                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        taskDueDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                        taskCompletedDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                    }
                    else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                        taskDueDate = (String)mapObjectInfo.get(SELECT_REL_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                        taskCompletedDate = (String)mapObjectInfo.get(SELECT_REL_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                    }
                    else if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                        taskDueDate = (String)mapObjectInfo.get("dueDate");
                        taskCompletedDate = "";
                    }
                }

                if(taskState != null && !"".equals(taskState))
                {
                    if(taskDueDate == null || "".equals(taskDueDate))
                    {
                        dueDate = new Date();
                    }
                    else
                    {
                        dueDate = eMatrixDateFormat.getJavaDate(taskDueDate);
                    }
                    if(!taskState.equals(stateComplete))
                    {
                        if(dueDate != null && curDate.after(dueDate))
                        {
                            statusColor = "Red";
                        }
                        else
                        {
                            statusColor = "Green";
                        }
                    }
                    else
                    {
                        Date actualCompletionDate = eMatrixDateFormat.getJavaDate(taskCompletedDate);

                        if(dueDate != null && actualCompletionDate.after(dueDate))
                        {
                            statusColor = "Red";
                        }
                        else
                        {
                            statusColor = "Green";
                        }
                    }

                    if("Red".equals(statusColor))
                    {
                        statusImageString = "<img border='0' src='../common/images/iconStatusRed.gif' name='red' id='red' alt='*' />";
                    }
                    else if("Green".equals(statusColor))
                    {
                        statusImageString = "<img border='0' src='../common/images/iconStatusGreen.gif' name='red' id='red' alt='*' />";
                    }
                    else
                    {
                        statusImageString="&nbsp;";
                    }
                }

                vecResult.add(statusImageString);
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Name For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getNameForMassTasksApproval(Context context, String[] args) throws Exception {

        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            DomainObject taskObject = new DomainObject();
            String strName= "";
            String sTaskTitle  ="";
            String sTypePQP = PropertyUtil.getSchemaProperty(context,"type_PartQualityPlan");

            StringBuffer prefixLinkBuffer = new StringBuffer();
            
            // Used showDetailsPopup due to bug 346533 
            prefixLinkBuffer.append("<b><a href=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?mode=insert&taskname=");

            StringBuffer tempLinkBuffer = new StringBuffer();
            tempLinkBuffer.append("&relId=");
            tempLinkBuffer.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("relId")));
            tempLinkBuffer.append("&parentOID=");
            tempLinkBuffer.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("parentOID")));
            tempLinkBuffer.append("&jsTreeID=");
            tempLinkBuffer.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
            tempLinkBuffer.append("&objectId=");
            String sContextType = "";

            String strInfoType = null;

            // Do for each object
            Map mapObjectInfo = null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();

                strInfoType = (String)mapObjectInfo.get("infoType");

                if (!"true".equalsIgnoreCase((String)mapObjectInfo.get("fromApproveAssignedTasks"))) {
                    //////////////////////////////////////////////////////////////////////////////////
                    // Case coming from My Task Mass Approval
                    //////////////////////////////////////////////////////////////////////////////////

                    sTaskTitle  = (String)mapObjectInfo.get(DomainObject.SELECT_NAME);
                    if(sTaskTitle!= null && !"".equals(sTaskTitle)) {
                        strName = sTaskTitle;
                    }
                    else
                    {
                        taskObject.setId((String)mapObjectInfo.get(DomainObject.SELECT_ID));
                        strName = taskObject.getInfo(context,"name");
                    }

                    StringBuffer finalURL = new StringBuffer();
                    finalURL.append(prefixLinkBuffer.toString());
                    finalURL.append(XSSUtil.encodeForJavaScript(context, strName));
                    finalURL.append(tempLinkBuffer.toString());
                    finalURL.append(XSSUtil.encodeForJavaScript(context, (String)mapObjectInfo.get(DomainObject.SELECT_ID)));

                    sContextType  =  (String)mapObjectInfo.get("Context Object Type");
                    if(sContextType != null && sContextType.equals(sTypePQP)) {
                        finalURL.append("&suiteKey=");
                        finalURL.append("SupplierCentral");
                        finalURL.append("&emxSuiteDirectory=");
                        finalURL.append("suppliercentral");
                    } else {
                        finalURL.append("&suiteKey=");
                        finalURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
                        finalURL.append("&emxSuiteDirectory=");
                        finalURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
                    }
                    finalURL.append("')\"  class=\"object\">");
                    finalURL.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">");
                    finalURL.append(XSSUtil.encodeForHTML(context, strName));
                    finalURL.append("</a></b>");

                    vecResult.add(finalURL.toString());
                }
                else {
                    //////////////////////////////////////////////////////////////////////////////////
                    // Case coming from Approve Assigned Tasks
                    //////////////////////////////////////////////////////////////////////////////////

                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        strName  = (String)mapObjectInfo.get(DomainObject.SELECT_NAME);

                        StringBuffer finalURL = new StringBuffer();
                        finalURL.append(prefixLinkBuffer.toString());
                        finalURL.append(XSSUtil.encodeForJavaScript(context, strName));
                        finalURL.append(tempLinkBuffer.toString());
                        finalURL.append(XSSUtil.encodeForJavaScript(context, (String)mapObjectInfo.get(DomainObject.SELECT_ID)));
                        finalURL.append("&suiteKey=");
                        finalURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
                        finalURL.append("&emxSuiteDirectory=");
                        finalURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
                        finalURL.append("')\"  class=\"object\">");
                        finalURL.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">");
                        finalURL.append(XSSUtil.encodeForHTML(context, strName));
                        finalURL.append("</a></b>");

                        vecResult.add(finalURL.toString());
                    }
                    else if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                        strName = (String)mapObjectInfo.get("name");

                        StringBuffer finalURL = new StringBuffer();
                        finalURL.append("<img border=\"0\" src=\"images/iconSmallSignature.gif\">");
                        finalURL.append(XSSUtil.encodeForHTML(context, strName));

                        vecResult.add(finalURL.toString());
                    }
                }
            }//for

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Title For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getTitleForMassTasksApproval(Context context, String[] args) throws Exception {

        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Map mapObjectInfo = null;
            String strTitle = "";

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();

                if (!"true".equalsIgnoreCase((String)mapObjectInfo.get("fromApproveAssignedTasks"))) {
                    strTitle = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_TITLE);
                }
                else {
                    if (INFO_TYPE_ACTIVATED_TASK.equals((String)mapObjectInfo.get("infoType"))) {
                        strTitle = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_TITLE);
                    }
                    else {
                        strTitle = "";
                    }
                }

                vecResult.add(strTitle);
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Approver For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getApproverForMassTasksApproval(Context context, String[] args) throws Exception {

        try {

            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");

            MapList objectList = (MapList)programMap.get("objectList");

            String strLanguage = (String)paramList.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            i18nNow loc = new i18nNow();

            String strPersonName = PersonUtil.getFullName(context);//Get person Name

            // Do for each object
            String strOwner = "";
            String sTypeName = "";
            StringList slApprovers = null;
            StringList slTranslatedApprovers = null;
            String strRoleOrGroup = null;
            Group groupObj = null;
            Role roleObj = null;

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                sTypeName = (String)mapObjectInfo.get(DomainObject.SELECT_TYPE);

                if (!"true".equalsIgnoreCase((String)mapObjectInfo.get("fromApproveAssignedTasks"))) {
                    if ((DomainObject.TYPE_TASK).equalsIgnoreCase(sTypeName))
                    {
                       strOwner = strPersonName;
                    }
                    else{
                        strOwner = (String) mapObjectInfo.get(DomainObject.SELECT_OWNER);
                    }
                }
                else {
                    if (INFO_TYPE_ACTIVATED_TASK.equals((String)mapObjectInfo.get("infoType"))) {
                        // If the task is assigned directly to the person
                        if (DomainObject.TYPE_PERSON.equals((String)mapObjectInfo.get(SELECT_ROUTE_TASK_ASSIGNEE_TYPE))) {
                            strOwner = strPersonName;
                        }
                        else {
                            strOwner = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_ROUTE_TASK_USER);
                            if (strOwner != null) {
                                if (strOwner.startsWith("role_")) {
                                    strOwner = PropertyUtil.getSchemaProperty(context, strOwner);//Symbolic->Real name
                                    strOwner = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Role." + FrameworkUtil.findAndReplace(strOwner, " ", "_"));
                                }
                                else {
                                    strOwner = PropertyUtil.getSchemaProperty(context, strOwner);//Symbolic->Real name
                                    strOwner = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Group." + FrameworkUtil.findAndReplace(strOwner, " ", "_"));
                                }
                            }
                            else {
                                strOwner = "";
                            }
                        }
                    }
                    else {
                        strOwner = (String)mapObjectInfo.get("approver");

                        //
                        // This will be comma separated list, show each of the approvers after taking care of internationalization
                        //
                        if (strOwner != null) {
                            slApprovers = FrameworkUtil.split(strOwner, ",");
                            slTranslatedApprovers = new StringList();

                            // Filter the list to avoid duplicate names
                            Set uniqueApprovers = new HashSet(slApprovers);
                            slApprovers = new StringList();
                            for (Iterator itrUniqueNames = uniqueApprovers.iterator(); itrUniqueNames.hasNext();) {
                                slApprovers.add((String) itrUniqueNames.next());
                            }

                            strRoleOrGroup = null;
                            groupObj = null;
                            roleObj = null;

                            // Find all the parent role and group hierarchy
                            for (Iterator itrApprovers = slApprovers.iterator(); itrApprovers.hasNext();) {
                                strRoleOrGroup = (String) itrApprovers.next();

                                // Is it role?
                                try {
                                    if(isRoleOrGroup(context,"Role",strRoleOrGroup)){
                                        roleObj = new Role(strRoleOrGroup);
                                        roleObj.open(context);
    
                                        // Internationalize this value
                                        strRoleOrGroup = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Role." + FrameworkUtil.findAndReplace(strRoleOrGroup, " ", "_"));
                                        slTranslatedApprovers.add(strRoleOrGroup);
    
                                        roleObj.close(context);
                                    }
                                    else if(isRoleOrGroup(context,"Group",strRoleOrGroup)){
                                        groupObj = new Group(strRoleOrGroup);
                                        groupObj.open(context);

                                        // Internationalize this value
                                        strRoleOrGroup = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Group." + FrameworkUtil.findAndReplace(strRoleOrGroup, " ", "_"));
                                        slTranslatedApprovers.add(strRoleOrGroup);

                                        groupObj.close(context);
                                    }else{
                                        slTranslatedApprovers.add(strRoleOrGroup);
                                    }
                                } catch (MatrixException me){
                                }
                            }

                            strOwner = FrameworkUtil.join(slTranslatedApprovers, ", ");
                        }
                        else {
                            strOwner = "";
                        }
                    }
                }

                vecResult.add(strOwner);
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Due Date For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getDueDateForMassTasksApproval(Context context, String[] args) throws Exception {

        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            String strTaskDueDate = "";
            String strTypeName = "";
            String strInfoType = null;
            Map mapObjectInfo = null;

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strTaskDueDate = "";

                strTypeName = (String)mapObjectInfo.get(DomainObject.SELECT_TYPE);

                if (!"true".equalsIgnoreCase((String)mapObjectInfo.get("fromApproveAssignedTasks"))) {
                    if ((DomainObject.TYPE_INBOX_TASK).equalsIgnoreCase(strTypeName))
                    {
                       strTaskDueDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    }
                    else if ((DomainObject.TYPE_TASK).equalsIgnoreCase(strTypeName))
                    {
                       strTaskDueDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
                    }
                }
                else {
                    strInfoType = (String)mapObjectInfo.get("infoType");
                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        strTaskDueDate = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    }
                    else {
                        strTaskDueDate = (String)mapObjectInfo.get("dueDate");
                    }
                }

                vecResult.add(strTaskDueDate);
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Get Object For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getObjectForMassTasksApproval(Context context, String[] args) throws Exception {

        try {
            final String SELECT_REL_ROUTE_ID = "relationship["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.id";

            // Create result vector
            Vector vecResult = new Vector();
            String contentName = "";

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

            DomainObject dmoObject = DomainObject.newInstance(context);

            StringList objectSelects = new StringList();
            objectSelects.add(DomainObject.SELECT_ID);
            objectSelects.add(DomainObject.SELECT_NAME);

            MapList mapList = new MapList();
            Map mapObjectInfo = null;
            String strObjId = null;
            String sRelName = "";
            boolean GET_TO = false;
            boolean GET_FROM = false;
            String strObjType = "";

            // Template for tree link
            StringBuffer strTreeLink = new StringBuffer(64);
            // Used showDetailsPopup due to bug 346533
            strTreeLink.append("<a href=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?relId=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("relId")));
            strTreeLink.append("&parentOID=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("parentOID")));
            strTreeLink.append("&jsTreeID=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
            strTreeLink.append("&suiteKey=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
            strTreeLink.append("&emxSuiteDirectory=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
            strTreeLink.append("&objectId=${OBJECT_ID}");
            strTreeLink.append("')\"  class=\"object\">");
            strTreeLink.append("${NAME}</a>");
            StringBuffer strHTMLBuffer = new StringBuffer(64);

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strObjType = (String)mapObjectInfo.get(DomainObject.SELECT_TYPE);

                if (DomainObject.TYPE_TASK.equals(strObjType)) {
                    strHTMLBuffer = new StringBuffer(FrameworkUtil.findAndReplace(strTreeLink.toString(), "${OBJECT_ID}", (String)mapObjectInfo.get("Context Object Id")));
                    strHTMLBuffer = new StringBuffer(FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", (String)mapObjectInfo.get("Context Object Name")));

                    vecResult.add(strHTMLBuffer.toString());
                }
                else {
                    if (DomainObject.TYPE_INBOX_TASK.equals(strObjType))
                    {
                        strObjId   =(String)mapObjectInfo.get(SELECT_REL_ROUTE_ID);
                        sRelName = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
                        GET_TO = true;
                    }

                    contentName = "";
                    if (strObjId != null && !"".equals(strObjId))
                    {
                        dmoObject.setId(strObjId);

                        mapList = dmoObject.getRelatedObjects(context,
                                                              sRelName,
                                                              "*",
                                                              objectSelects,
                                                              null,
                                                              GET_TO,
                                                              GET_FROM,
                                                              (short)1,
                                                              "",
                                                              "");

                        for (Iterator itrContentObjects = mapList.iterator(); itrContentObjects.hasNext();) {
                            mapObjectInfo = (Map) itrContentObjects.next();

                            strHTMLBuffer = new StringBuffer(FrameworkUtil.findAndReplace(strTreeLink.toString(), "${OBJECT_ID}", (String)mapObjectInfo.get(DomainObject.SELECT_ID)));
                            strHTMLBuffer = new StringBuffer(FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", (String)mapObjectInfo.get(DomainObject.SELECT_NAME)));

                            if (contentName.length() > 0) {
                                contentName += ", ";
                            }
                            contentName += strHTMLBuffer.toString();
                        }
                    }
                    vecResult.add(contentName);
                }

            }//for

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Checks if the invokation is in context of object or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return true if this execution is not in object context
     * @throws Exception
     */
    public  boolean checkIfNotInObjectContext(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestValuesMap = (Map)programMap.get("RequestValuesMap");

        if (requestValuesMap == null) {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.RequestValuesMapNotFound", context.getLocale().getLanguage()));
        }

        String[] strObjectIds = (String[])requestValuesMap.get("objectId");

        if (strObjectIds == null || strObjectIds.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * Gets Approval Status For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getApprovalStatusForMassTasksApproval(Context context, String[] args) throws Exception {

        try {
            // Create result vector
            Vector vecResult = new Vector();

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");
            i18nNow loc = new i18nNow();

            String isTaskApprovalCommentsOn = EnoviaResourceBundle.getProperty(context, "emxComponents.Routes.ShowCommentsForTaskApproval");
            final String STRING_NO_ACTION = (String)loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.NoAction");
            final String STRING_REJECT = (String)loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.Reject");
            final String STRING_APPROVE = (String)loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.Approve");
            final String STRING_ACCEPT = (String)loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.Accept");
            final String STRING_COMPLETE = (String)loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.Complete");
            final String STRING_IGNORE = (String)loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.Ignore");

            final String OPTION_NO_ACTION = "<option value=\"No Action\">" + STRING_NO_ACTION + "</option>";
            final String OPTION_REJECT = "<option value=\"Reject\">" + STRING_REJECT + "</option>";
            final String OPTION_APPROVE = "<option value=\"Approve\">" + STRING_APPROVE + "</option>";
            final String OPTION_ACCEPT = "<option value=\"Accept\">" + STRING_ACCEPT + "</option>";
            final String OPTION_COMPLETE = "<option value=\"Complete\">" + STRING_COMPLETE + "</option>";
            String OPTION_ABSTAIN = null;
            final String OPTION_IGNORE = "<option value=\"Ignore\">" + STRING_IGNORE + "</option>";
            
            boolean showAbstain = "true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxComponents.Routes.ShowAbstainForTaskApproval")); 
            if(showAbstain) 
            {
                OPTION_ABSTAIN = "<option value=\"Abstain\">" + 
                                 loc.GetString("emxFrameworkStringResource",languageStr, "emxFramework.LifecycleTasks.Abstain") + 
                                 "</option>";
            }

            Map mapObjectInfo = null;
            String strListBoxName = null;
            String strListBoxId = null;
            StringBuffer sbufHTML = null;
            String strValidApprovalStatusAction = null;
            String strSerialNumber = null;

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext(); ) {
                mapObjectInfo = (Map) itrObjects.next();

                strValidApprovalStatusAction = (String)mapObjectInfo.get("ValidApprovalStatusAction");
                strSerialNumber = (String)mapObjectInfo.get("serialNumber");

                // Form id & name of the list box
                strListBoxName = "Approval Status" + XSSUtil.encodeForHTMLAttribute(context, strSerialNumber);
                strListBoxId = strListBoxName + "Id";

                // Form the HTML code
                sbufHTML = new StringBuffer(64);
                sbufHTML.append("<select id=\"").append(strListBoxId).append("\" name=\"").append(strListBoxName);
                if(isTaskApprovalCommentsOn.equals("false")){
                	sbufHTML.append("\" onchange=\"clearApprovalAndAbstainTaskComments('");
                	sbufHTML.append(strListBoxId).append("'").append(",'").append(strSerialNumber).append("')");
                }
                sbufHTML.append("\">");
                sbufHTML.append(OPTION_NO_ACTION);

                if ("IT-Accept".equals(strValidApprovalStatusAction)) {
                    sbufHTML.append(OPTION_ACCEPT);
                }
                else if ("IT-Approve".equals(strValidApprovalStatusAction)) {
                    sbufHTML.append(OPTION_APPROVE);
                    sbufHTML.append(OPTION_REJECT);
                    if(showAbstain)
                        sbufHTML.append(OPTION_ABSTAIN);
                }
                else if ("IT-Complete".equals(strValidApprovalStatusAction)) {
                    sbufHTML.append(OPTION_COMPLETE);
                }
                else if ("Sign-Approve".equals(strValidApprovalStatusAction)) {
                    // Begin : Bug 348243 : code modification
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("hasapprove"))) {
                        sbufHTML.append(OPTION_APPROVE);
                    }
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("hasreject"))) {
                        sbufHTML.append(OPTION_REJECT);
                    }
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("hasignore"))) {
                        sbufHTML.append(OPTION_IGNORE);
                    }
                    // End : Bug 348243 : code modification
                }
                else if ("WFT-Accept".equals(strValidApprovalStatusAction)) {
                    sbufHTML.append(OPTION_ACCEPT);
                }
                else if ("WFT-Complete".equals(strValidApprovalStatusAction)) {
                    sbufHTML.append(OPTION_COMPLETE);
                }
                else if ("WBS-Complete".equals(strValidApprovalStatusAction)) {
                    sbufHTML.append(OPTION_COMPLETE);
                }
                sbufHTML.append("</select>");

                // Add to result vector
                vecResult.add(sbufHTML.toString());
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
        finally {
        }
    }

    /**
     * Gets Comments For Mass Tasks Approval Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getCommentsForMassTasksApproval(Context context, String[] args) throws Exception {

        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String strSerialNumber = null;


            // Do for each object
            StringBuffer strHTML = null;
            Map mapObjectInfo = null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext(); ) {
                mapObjectInfo = (Map) itrObjects.next();

                strSerialNumber = (String)mapObjectInfo.get("serialNumber");

                //
                // Form the text area for comments
                //
                strHTML = new StringBuffer(64);
                strHTML.append("<textarea rows=\"5\" name=\"Comments").append(XSSUtil.encodeForHTMLAttribute(context, strSerialNumber)).append("\" id=\"Comments").append(XSSUtil.encodeForHTMLAttribute(context, strSerialNumber)).append("Id\"></textarea>");

                // Output this code
                vecResult.add(strHTML.toString());

            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
        finally {
        }
    }

    /**
     * Gets Checkbox For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getCheckboxForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();
            final boolean ENABLECHECKBOX = true;

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

            String strParentObjId = (String)paramList.get("objectId");

            final String POLICY_INBOX_TASK_STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");

            // Do for each object
            Map mapObjectInfo = null;
            String strCurrentState = "";
            String strParentObjectState = "";
            String strAssigneeType = "";
            String strInfoType = "";
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strCurrentState = (String)mapObjectInfo.get("currentState");//Tasks' state
                strParentObjectState = (String)mapObjectInfo.get("parentObjectState");//State of parent object belonging this task or state
                strAssigneeType = (String)mapObjectInfo.get("assigneeType");
                strInfoType = (String)mapObjectInfo.get("infoType");

                // Tasks for past state can not be selected
                if (PolicyUtil.checkState(context, strParentObjId, strParentObjectState, PolicyUtil.GT)) {
                    vecResult.add(String.valueOf(!ENABLECHECKBOX));
                }
                else {
                    if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                        if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                            vecResult.add(String.valueOf(!ENABLECHECKBOX));
                        }
                        else {
                            vecResult.add(String.valueOf(ENABLECHECKBOX));
                        }
                    }
                    else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        if (!POLICY_INBOX_TASK_STATE_ASSIGNED.equals(strCurrentState)) {
                            vecResult.add(String.valueOf(!ENABLECHECKBOX));
                        }
                        else {
                            // Tasks to be accepted (assigned to role or group) cannot be selected
                            if (!DomainObject.TYPE_PERSON.equals(strAssigneeType)) {
                                vecResult.add(String.valueOf(!ENABLECHECKBOX));
                            }
                            else {
                                vecResult.add(String.valueOf(ENABLECHECKBOX));
                            }
                        }
                    }
                    else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                        vecResult.add(String.valueOf(ENABLECHECKBOX));
                    }
                    else if (INFO_TYPE_EMPTY_ROW.equals(strInfoType)) {
                        vecResult.add(String.valueOf(ENABLECHECKBOX));
                    }
                    else {
                        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskSigOnApprovalTable", context.getLocale().getLanguage()));
                    }
                }
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets State For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getStateForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            Map mapObjectInfo = null;

//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification

            String ParentObjId = (String)paramList.get("objectId");
            DomainObject dmoObject = new DomainObject(ParentObjId);
            String strCurrentParentState = dmoObject.getInfo(context, DomainObject.SELECT_CURRENT);

            String strLanguage = (String)paramList.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            i18nNow loc = new i18nNow();
            final String STRING_AD_HOC_TASK = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.AdHocTask");

            // Do for each object
            String strParentObjectState = null;
            String strParentObjectTranslatedState = null;
            String strParentObjectPolicy = null;

            boolean isAdHoc = false;

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();

                strParentObjectState = (String)mapObjectInfo.get("parentObjectState");
                strParentObjectPolicy = (String)mapObjectInfo.get("parentObjectPolicy");


                isAdHoc = "true".equalsIgnoreCase((String)mapObjectInfo.get("isAdHoc"));


                if (isAdHoc) {
                    vecResult.add(STRING_AD_HOC_TASK);
                }
                else {
                    // Translate state name
                    strParentObjectTranslatedState = i18nNow.getStateI18NString(strParentObjectPolicy, strParentObjectState, strLanguage);

                    // Show current object state name as bold
                    if ( strCurrentParentState.equals(strParentObjectState)) {
//                      Begin : Bug 346997 code modification
                        if (isExporting) {
                            vecResult.add(strParentObjectTranslatedState);
                        }
                        else {
                        vecResult.add("<b>" + strParentObjectTranslatedState + "</b>" );
                    }
//                      End : Bug 346997 code modification
                    }
                    else {
                        vecResult.add(strParentObjectTranslatedState);
                    }
                }
            }
            //XSSOK
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Assignee For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getAssigneeForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            String strInfoType = null;

//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification
            
            // For i18nNow string formation
            i18nNow loc = new i18nNow();
            String strLanguage = (String)paramList.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";

            String strApprovers = null;;
            String strRoleOrGroup = null;
            StringList slApprovers = null;
            StringList slTranslatedApprovers = null;
            Group groupObj = null;
            Role roleObj = null;

            StringBuffer strHTMLBuffer = null;
            StringBuffer strTreeLinkForPerson = new StringBuffer();
            
            // Used showDetailsPopup due to bug 346533
            strTreeLinkForPerson.append("<a aloid= \"true\" rmbID=\"${OBJECT_ID}\" href=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?");
            strTreeLinkForPerson.append("&parentOID=");
            strTreeLinkForPerson.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("parentOID")));
            strTreeLinkForPerson.append("&jsTreeID=");
            strTreeLinkForPerson.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
            strTreeLinkForPerson.append("&suiteKey=");
            strTreeLinkForPerson.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
            strTreeLinkForPerson.append("&objectId=${OBJECT_ID}&personName=${NAME}");
            strTreeLinkForPerson.append("')\"  class=\"object\">");
            strTreeLinkForPerson.append("<img border=\"0\" src=\"images/iconSmallPerson.gif\">${NAME}</a>");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");
                strHTMLBuffer = new StringBuffer(64);

                if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType) || INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {

                    // Decide the name of the approver
                    if (DomainObject.TYPE_ROUTE_TASK_USER.equals(mapObjectInfo.get("assigneeType"))) {
                        // If the type of the assignee is RTU then get the name of the role/group as approver name
                        String strRouteTaskUser = (String)mapObjectInfo.get("routeTaskUser");
                        if (strRouteTaskUser == null || "".equals(strRouteTaskUser.trim()) || "null".equals(strRouteTaskUser)) {
                            vecResult.add(strHTMLBuffer.toString());
                        }
                        else {
                            if (strRouteTaskUser.startsWith("role_")) {
                                strRouteTaskUser = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);//Symbolic->Real name
                                strRouteTaskUser = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Role." + FrameworkUtil.findAndReplace(strRouteTaskUser, " ", "_"));
                                
//                              Begin : Bug 346997 code modification
                                if (isExporting) {
                                    vecResult.add(strRouteTaskUser);
                                }
                                else {
                                strHTMLBuffer.append("<img src=\"images/iconSmallRole.gif\" border=\"0\" align=\"left\">");
                                strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strRouteTaskUser));
                                vecResult.add(strHTMLBuffer.toString());
                            }
//                             End : Bug 346997 code modification
                            }
                            else if (strRouteTaskUser.startsWith("group_")) {
                                strRouteTaskUser = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);//Symbolic->Real name
                                strRouteTaskUser = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Group." + FrameworkUtil.findAndReplace(strRouteTaskUser, " ", "_"));
//                              Begin : Bug 346997 code modification
                                if (isExporting) {
                                    vecResult.add(strRouteTaskUser);
                                }
                                else {
                                strHTMLBuffer.append("<img src=\"images/iconSmallGroup.gif\" border=\"0\" align=\"left\">");
                                strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strRouteTaskUser));
                                vecResult.add(strHTMLBuffer.toString());
                            }
//                             End : Bug 346997 code modification
                            }
                            else {
                                vecResult.add(strHTMLBuffer.toString());
                            }
                        }
                    }
                    else {
                        // If the type of the assignee is not RTU then it is person and get the name of the person as approver name
                        String strPersonId = PersonUtil.getPersonObjectID(context,(String)mapObjectInfo.get("assigneeName"));
                        
//                      Begin : Bug 346997 code modification
                        if (isExporting) {
                            vecResult.add((String)mapObjectInfo.get("assigneeName"));
                        }
                        else {
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTreeLinkForPerson.toString(), "${OBJECT_ID}", strPersonId));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", PersonUtil.getFullName(context,(String)mapObjectInfo.get("assigneeName"))));

                        vecResult.add(strHTMLBuffer.toString());
                    }
//                     End : Bug 346997 code modification
                        

                    }
                }
                else if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        String strPersonId = PersonUtil.getPersonObjectID(context,(String)mapObjectInfo.get("approver"));
//                      Begin : Bug 346997 code modification
                        if (isExporting) {
                            vecResult.add((String)mapObjectInfo.get("approver"));
                        }
                        else {
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTreeLinkForPerson.toString(), "${OBJECT_ID}", strPersonId));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", PersonUtil.getFullName(context,(String)mapObjectInfo.get("approver"))));

                        vecResult.add(strHTMLBuffer.toString());
                    }
//                     End : Bug 346997 code modification
                    }
                    else {
                        strApprovers = (String)mapObjectInfo.get("approver");
                        slApprovers = FrameworkUtil.split(strApprovers, ",");

                        // Filter the list to avoid duplicate names
                        Set uniqueApprovers = new HashSet(slApprovers);
                        slApprovers = new StringList();
                        for (Iterator itrUniqueNames = uniqueApprovers.iterator(); itrUniqueNames.hasNext();) {
                            slApprovers.add((String) itrUniqueNames.next());
                        }

                        slTranslatedApprovers = new StringList();

                        strRoleOrGroup = null;
                        groupObj = null;
                        roleObj = null;

                        // Find all the parent role and group hierarchy
                        for (Iterator itrApprovers = slApprovers.iterator(); itrApprovers.hasNext();) {
                            strRoleOrGroup = (String) itrApprovers.next();

                            // Is it role?
                            try {
                                if(isRoleOrGroup(context,"Role",strRoleOrGroup)){
                                    roleObj = new Role(strRoleOrGroup);
                                    roleObj.open(context);
    
                                    // Internationalize this value
                                    strRoleOrGroup = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Role." + FrameworkUtil.findAndReplace(strRoleOrGroup, " ", "_"));
    //                              Begin : Bug 346997 code modification
                                    if (isExporting) {
                                        slTranslatedApprovers.add(strRoleOrGroup);
                                    }
                                    else {
                                    strHTMLBuffer = new StringBuffer("<img src=\"images/iconSmallRole.gif\" border=\"0\">");
                                    strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strRoleOrGroup));
                                    slTranslatedApprovers.add(strHTMLBuffer.toString());
                                    }
    //                             End : Bug 346997 code modification
                                    roleObj.close(context);
                                }else if(isRoleOrGroup(context,"Group",strRoleOrGroup)){
                                    groupObj = new Group(strRoleOrGroup);
                                    groupObj.open(context);

                                    // Internationalize this value
                                    strRoleOrGroup = loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Group." + FrameworkUtil.findAndReplace(strRoleOrGroup, " ", "_"));
//                                  Begin : Bug 346997 code modification
                                    if (isExporting) {
                                        slTranslatedApprovers.add(strRoleOrGroup);
                                    }
                                    else {
                                    strHTMLBuffer = new StringBuffer("<img src=\"images/iconSmallGroup.gif\" border=\"0\">");
                                    strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strRoleOrGroup));
                                    slTranslatedApprovers.add(strHTMLBuffer.toString());
                                    }
//                                 End : Bug 346997 code modification

                                    groupObj.close(context);
                                } else {
//                                  This is neither role nor group, must be person
                                    String strPersonId = PersonUtil.getPersonObjectID(context,strRoleOrGroup);
//                                  Begin : Bug 346997 code modification
                                    if (isExporting) {
                                        slTranslatedApprovers.add(strRoleOrGroup);
                                    }else {
                                    strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTreeLinkForPerson.toString(), "${OBJECT_ID}", strPersonId));
                                    strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", strRoleOrGroup));

                                    slTranslatedApprovers.add(strHTMLBuffer.toString());
                                    }
                                }
                            } catch (MatrixException me){
                            }
                        }

                        vecResult.add(FrameworkUtil.join(slTranslatedApprovers, ", "));
                    }// if not signed
                }
                else if(INFO_TYPE_EMPTY_ROW.equals(strInfoType)){
                    vecResult.add(strHTMLBuffer.toString());
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Task Or Signature For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getTaskOrSignatureForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");

//          Begin : Bug 346997 code modification
            boolean isExporting = (paramList.get("reportFormat") != null);
//          End : Bug 346997 code modification
            
            Map mapObjectInfo = null;
            String strInfoType = null;
            String strName = null;
            StringBuffer strHTMLBuffer = null;
            String strObjectId = null;
            String strRouteId = null;
            String strRouteNodeId = null;

            StringBuffer strTreeLink = new StringBuffer();
            // Used showDetailsPopup due to bug 346533
            strTreeLink.append("<a aloid= \"true\" rmbID=\"${OBJECT_ID}\" href=\"JavaScript:showDetailsPopup('../common/emxTree.jsp?relId=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("relId")));
            strTreeLink.append("&parentOID=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("parentOID")));
            strTreeLink.append("&jsTreeID=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
            strTreeLink.append("&suiteKey=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
            strTreeLink.append("&emxSuiteDirectory=");
            strTreeLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
            strTreeLink.append("&objectId=${OBJECT_ID}&taskName=${NAME}");
            strTreeLink.append("')\"  class=\"object\">");
            strTreeLink.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">${NAME}</a>");

            StringBuffer strTaskPropertiesLink = new StringBuffer(64);
            strTaskPropertiesLink.append("<a href=\"JavaScript:showDetailsPopup('../components/emxRouteTaskDetailsFS.jsp?routeId=${ROUTE_ID}&taskCreated=no&routeNodeId=${ROUTE_NODE_ID}");
            strTaskPropertiesLink.append("&suiteKey=");
            strTaskPropertiesLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
            strTaskPropertiesLink.append("&emxSuiteDirectory=");
            strTaskPropertiesLink.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
            strTaskPropertiesLink.append("')\"  class=\"object\">");
            strTaskPropertiesLink.append("<img border=\"0\" src=\"images/iconSmallTask.gif\">${NAME}</a>");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strName = (String)mapObjectInfo.get("name");
                strInfoType = (String)mapObjectInfo.get("infoType");


                // The Task name will be hyperlinked to popup the task details.
                // If task name is clicked, task tree and properties page will open in a popup.
                // If the item is a signature, the signature name is displayed without hyperlink.
                strHTMLBuffer = new StringBuffer(64);
                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
//                  Begin : Bug 346997 code modification
                    if (isExporting) {
                        strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strName));
                    }
                    else {
                    strHTMLBuffer.append("<img src=\"images/iconSmallSignature.gif\" border=\"0\" align=\"left\">");
                    strHTMLBuffer.append(XSSUtil.encodeForHTML(context, strName));
                }
//                 End : Bug 346997 code modification
                }
                else {
                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        strObjectId = (String)mapObjectInfo.get("taskId");
//                      Begin : Bug 346997 code modification
                        if (isExporting) {
                            strHTMLBuffer = new StringBuffer (XSSUtil.encodeForHTML(context, strName));
                        }
                        else {
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTreeLink.toString(), "${OBJECT_ID}", strObjectId));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", strName));
                    }
//                     End : Bug 346997 code modification
                    }
                    else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                        strObjectId = (String)mapObjectInfo.get("routeNodeId");
                        strRouteId = (String)mapObjectInfo.get("routeId");
                        strRouteNodeId = (String)mapObjectInfo.get("routeNodeId");
//                      Begin : Bug 346997 code modification
                        if (isExporting) {
                            strHTMLBuffer = new StringBuffer (XSSUtil.encodeForHTML(context, strName));
                        }
                        else {
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strTaskPropertiesLink.toString(), "${ROUTE_ID}", strRouteId));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${NAME}", strName));
                        strHTMLBuffer = new StringBuffer (FrameworkUtil.findAndReplace(strHTMLBuffer.toString(), "${ROUTE_NODE_ID}", strRouteNodeId));
                    }
//                     End : Bug 346997 code modification
                    }
                    else if(INFO_TYPE_EMPTY_ROW.equals(strInfoType)){
                        strHTMLBuffer.append("");
                    }
                    else {
                        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskOnApprovalTable", context.getLocale().getLanguage()));
                    }
                }

                vecResult.add(strHTMLBuffer.toString());
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Task Title For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getTaskTitleForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            // Do for each object
            String strtitle = "";
            String strInfoType = "";
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                strtitle = (String)mapObjectInfo.get("title");
                strInfoType = (String)mapObjectInfo.get("infoType");
                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    vecResult.add("");
                }
                else if(INFO_TYPE_ACTIVATED_TASK.equals(strInfoType) || INFO_TYPE_DEFINED_TASK.equals(strInfoType)){
                    vecResult.add(strtitle);
                }
                else if(INFO_TYPE_EMPTY_ROW.equals(strInfoType)){
                    vecResult.add("");
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Comments Or Instruction For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getCommentsOrInstructionForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String strInfoType = null;
            String strInstructions = null;
            String strComments = null;

            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");

            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");

                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        strComments = (String)mapObjectInfo.get("comment");
                        if (strComments == null) {
                            strComments = "";
                        }
                        vecResult.add(strComments);
                    }
                    else {
                        // There will not be any comments for the unsigned signatures
                        vecResult.add("");
                    }
                }
                else {
                    strComments = (String)mapObjectInfo.get("comments");
                    strInstructions = (String)mapObjectInfo.get("instructions");
                    if (strComments == null) {
                        strComments = "";
                    }
                    if (strInstructions == null) {
                        strInstructions = "";
                    }

                    if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                        // Show comments for completed tasks else instructions for incomplete tasks
                        if (POLICY_INBOX_TASK_STATE_COMPLETE.equals((String)mapObjectInfo.get("currentState"))) {
                            vecResult.add(strComments);
                        }
                        else {
                            vecResult.add(strInstructions);
                        }
                    }
                    else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                        vecResult.add(strInstructions);
                    }
                    else if (INFO_TYPE_EMPTY_ROW.equals(strInfoType)) {
                        vecResult.add("");
                    }
                    else {
                        throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                    }
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Action For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getActionForApprovals(Context context, String[] args) throws Exception {
        try {
            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
            final String POLICY_INBOX_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Review");

            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            
            String ParentObjId = (String)paramList.get("objectId");
            DomainObject dmoObject = new DomainObject(ParentObjId);
            String strCurrentParentState = dmoObject.getInfo(context, DomainObject.SELECT_CURRENT);
            
            Map mapObjectInfo = null;
            String strInfoType = null;
            String strCurrentState = null;
            String strLanguage = (String)paramList.get("languageStr");
            String strRouteAction = null;
            String strParentObjectState = null;
            String strCurrentRouteStatus = null;
            String strTaskOrder = null;
            String strRouteNodeId = null;
            String strApprovalStatus = null;
            MapList mlRelInfo = null;
            Map mapRelInfo = null;
			String strAttrRouteAction =null;

            // Find the status strings to be shown
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            i18nNow loc = new i18nNow();
            final String STRING_APPROVED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Approved");
            final String STRING_REJECTED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Rejected");
            final String STRING_IGNORED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Ignored");
            final String STRING_ABSTAINED = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Abstained");
            final String STRING_NEEDS_REVIEW = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.NeedsReview");
            final String STRING_PENDING = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Pending");
            final String STRING_PENDING_ORDER = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.PendingOrder");
            final String STRING_APPROVE = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Lifecycle.Approve");
            final String STRING_COMMENT = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Comment");
            final String STRING_INFORMATION_ONLY = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.InformationOnly");
            final String STRING_INVESTIGATE = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.Investigate");
            final String STRING_NOTIFY_ONLY = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.NotifyOnly");
            final String STRING_AWAITING_APPROVAL = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.LifecycleTasks.AwaitingApproval");            
            String strAttribName = PropertyUtil.getSchemaProperty(context,"attribute_RouteAction");
            // Do for each object
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");
                strAttrRouteAction = (String)mapObjectInfo.get("routeAction");			
                strRouteAction = i18nNow.getRangeI18NString(strAttribName ,strAttrRouteAction, strLanguage);
                strCurrentState = (String)mapObjectInfo.get("currentState");
                strParentObjectState = (String)mapObjectInfo.get("parentObjectState");
                strCurrentRouteStatus = (String)mapObjectInfo.get("routeStatus");
                strRouteNodeId = (String)mapObjectInfo.get("routeNodeId");
                strApprovalStatus = (String)mapObjectInfo.get("approvalStatus");
                
                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                    // If signature is completed then show accordingly else show the links for operations
                    if ("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))) {
                        if ("true".equalsIgnoreCase((String)mapObjectInfo.get("approved"))) {
                            vecResult.add(STRING_APPROVED);
                        }
                        else if ("true".equalsIgnoreCase((String)mapObjectInfo.get("rejected"))) {
                            vecResult.add(STRING_REJECTED);
                        }
                        else if ("true".equalsIgnoreCase((String)mapObjectInfo.get("ignored"))) {
                            vecResult.add(STRING_IGNORED);
                        }
                        else{
                            vecResult.add(STRING_AWAITING_APPROVAL);
                        }
                    }
                    else {
                        if (strParentObjectState.equalsIgnoreCase(strCurrentParentState)){
                            vecResult.add(STRING_AWAITING_APPROVAL);
                        }
                        else{
                            vecResult.add(STRING_PENDING);
                        }
                    }
                }
                else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                    if (POLICY_INBOX_TASK_STATE_COMPLETE.equalsIgnoreCase(strCurrentState)) {
                        if(STRING_APPROVE.equalsIgnoreCase(strRouteAction)){
                            if ("Approve".equals(strApprovalStatus)) {
                                vecResult.add(STRING_APPROVED);
                            }
                            else if ("Reject".equals(strApprovalStatus)) {
                                vecResult.add(STRING_REJECTED);
                            }
                            else if ("Abstain".equals(strApprovalStatus)) {
                                vecResult.add(STRING_ABSTAINED);
                            }
                            else {
                                vecResult.add(STRING_APPROVED);
                            }
                        }
                        else if(STRING_COMMENT.equalsIgnoreCase(strRouteAction)){
                            vecResult.add(STRING_COMMENT);
                        }
                        else if(STRING_INFORMATION_ONLY.equalsIgnoreCase(strRouteAction)){
                            vecResult.add(STRING_INFORMATION_ONLY);
                        }
                        else if(STRING_INVESTIGATE.equalsIgnoreCase(strRouteAction)){
                            vecResult.add(STRING_INVESTIGATE);
                        }
                        else if(STRING_NOTIFY_ONLY.equalsIgnoreCase(strRouteAction)){
                            vecResult.add(STRING_NOTIFY_ONLY);
                        }
                        else{
                            throw new Exception (ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskActionColumn", context.getLocale().getLanguage()));
                        }
                    }
                    //START BUG 346838
                    else if(POLICY_INBOX_TASK_STATE_REVIEW.equals(strCurrentState)){
                        vecResult.add(STRING_NEEDS_REVIEW);
                        //END BUG 346838
                    }
                    else {
                        vecResult.add(STRING_AWAITING_APPROVAL);
                    }
                }
                else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    //
                    // If the route for this task is not active then show "Pending" else show "Pending Order <n>"
                    //
                    if ("Started".equals(strCurrentRouteStatus)) {
                        mlRelInfo = DomainRelationship.getInfo(context, new String[]{strRouteNodeId}, new StringList(SELECT_ATTRIBUTE_ROUTE_SEQUENCE));
                        mapRelInfo = (Map)mlRelInfo.get(0);
                        
                        if (mapRelInfo != null) {
                            strTaskOrder = (String)mapRelInfo.get(SELECT_ATTRIBUTE_ROUTE_SEQUENCE);
                        }
                        if (strTaskOrder == null) {
                            strTaskOrder = "";
                        }
                        vecResult.add(STRING_PENDING_ORDER + " " + strTaskOrder);
                    }
                    else {
                        vecResult.add(STRING_PENDING);
                    }
                }
                else if(INFO_TYPE_EMPTY_ROW.equals(strInfoType)){
                    vecResult.add("");
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Get Due Date For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getDueDateForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            String strCellValue = "";
            
            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");
            String strEditMode = (String)paramList.get("editTableMode");

            Map mapObjectInfo = null;
            String strInfoType = null;
            String strCurrentTaskState = null;
            String strDueDate = null;
            String strTaskId = null;
            String strRouteNodeId = null;
            String strRouteId = null;
            String strTaskOwner = null;
            String strRouteOwner = null;
            String strContextUser = context.getUser();
            DomainObject dmoTask = null;
            DomainObject dmoRoute = null;
            final String SELECT_ROUTE_OWNER = "from[" + DomainObject.RELATIONSHIP_ROUTE_TASK + "].to.owner";
            boolean canEditDueDate = false;
            boolean canFormatTime = false;
            StringList slTaskSelect = new StringList();
            slTaskSelect.add(DomainObject.SELECT_OWNER);
            slTaskSelect.add(SELECT_ROUTE_OWNER);

            Locale locale = (Locale)paramList.get("localeObj");
            int iDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            DateFormat dateFormat = DateFormat.getDateInstance(iDateFormat, locale);
            SimpleDateFormat dbDateFormat = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(),Locale.ENGLISH);
            String sActualDbDate = "";


            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");

            // Do for each object
            final String COLUMN_NAME = "Due Date";
            String strFieldName = null;
            String strOriginalDueDate = null;
            StringBuffer strHTMLBuffer = null;
            Date dtDueDate = null;
            String strMiliSeconds = null;
            int i = 0;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();i++) {
                mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");
                strCurrentTaskState = (String)mapObjectInfo.get("currentState");
                strOriginalDueDate = (String)mapObjectInfo.get("dueDate");
                strDueDate = "";
                strMiliSeconds = "";

                // Bug 344302
                // After editing Due Date, the edit table is refreshed. The refresh operation will not
                // refresh entire table page but only table body. Due to this for due date column
                // we have to take data from database and not from cached objectList.
                // Now signature due dates cannot be edited, also completed tasks' due date cannot be edited.
                //
                if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                    if(!POLICY_INBOX_TASK_STATE_COMPLETE.equalsIgnoreCase(strCurrentTaskState)){
                        strTaskId = (String)mapObjectInfo.get("taskId");
                        dmoTask = new DomainObject(strTaskId);
                        strOriginalDueDate = dmoTask.getInfo(context, SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                        
                        String timeZone = (String) (paramList != null ? paramList.get("timeZone") : programMap.get("timeZone"));
                        double clientTZOffset   = (new Double(timeZone)).doubleValue();
                        if(! UIUtil.isNullOrEmpty(strOriginalDueDate)){
                        	strDueDate =   eMatrixDateFormat.getFormattedDisplayDateTime(strOriginalDueDate, canFormatTime, clientTZOffset, locale);
                        }
                    }
                }
                else if (INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    strRouteNodeId = (String)mapObjectInfo.get("routeNodeId");
                    strOriginalDueDate = DomainRelationship.getAttributeValue(context, strRouteNodeId, DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                    String timeZone = (String) (paramList != null ? paramList.get("timeZone") : programMap.get("timeZone"));
                    double clientTZOffset   = (new Double(timeZone)).doubleValue();
                    if(! UIUtil.isNullOrEmpty(strOriginalDueDate)){
                    	strDueDate =   eMatrixDateFormat.getFormattedDisplayDateTime(strOriginalDueDate, canFormatTime, clientTZOffset, locale);
                    }
                }
                
                // Format the due date as per matrix date format
                if(UIUtil.isNotNullAndNotEmpty(strDueDate)){
                    dtDueDate = eMatrixDateFormat.getJavaDate(strOriginalDueDate);
                    strDueDate = dateFormat.format(dtDueDate);
                    strMiliSeconds = String.valueOf(dtDueDate.getTime());
                    sActualDbDate = dbDateFormat.format(dtDueDate);
                }

                if (INFO_TYPE_SIGNATURE.equals(strInfoType)) {
                	strCellValue = strDueDate;                    
                }
                else if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType) || INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {

                    if(POLICY_INBOX_TASK_STATE_COMPLETE.equalsIgnoreCase(strCurrentTaskState)){
                    	strCellValue = strDueDate;
                    }
                    else{
                        if ("true".equalsIgnoreCase(strEditMode)) {
                            strRouteId = (String)mapObjectInfo.get("routeId");
                            dmoRoute = new DomainObject(strRouteId);
                            strRouteOwner = dmoRoute.getInfo(context, DomainObject.SELECT_OWNER);

                            if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType)) {
                                //
                                // Show editable due dates only for the task assignee or route onwer
                                //
                                strTaskId = (String)mapObjectInfo.get("taskId");
                                dmoTask = new DomainObject(strTaskId);
                                strTaskOwner = dmoTask.getInfo(context, DomainObject.SELECT_OWNER);

                                canEditDueDate = strContextUser.equals(strTaskOwner) || strContextUser.equals(strRouteOwner);
                            }
                            else {
                                //
                                // Defined (not yet activated) task can only be edited by route owner
                                //
                                canEditDueDate = strContextUser.equals(strRouteOwner);
                            }

                            if (canEditDueDate) {
                                strFieldName = COLUMN_NAME + i;

                                strHTMLBuffer = new StringBuffer(64);
                                strHTMLBuffer.append("<input type=\"text\" readonly=\"readonly\" size=\"\" name=\"").append(strFieldName).append("\" value=\"").append(strDueDate).append("\" id=\"").append(strFieldName).append("\">");
                                strHTMLBuffer.append("&nbsp;<a href=\"javascript:showCalendar('editDataForm', '").append(strFieldName).append("', '").append(sActualDbDate).append("', '', saveFieldObjByName('").append(strFieldName).append("'))\">");
                                strHTMLBuffer.append("<img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\"></a><input type=\"hidden\" name=\"").append(strFieldName).append("_msvalue\"  value=\"").append(strMiliSeconds).append("\">");
                                strHTMLBuffer.append("<input type=\"hidden\" name=\"").append(strFieldName).append("fieldValue\"  value=\"").append(sActualDbDate).append("\">");
                                strHTMLBuffer.append("<input type=\"hidden\" name=\"").append(strFieldName).append("AmPm\"  value=\"\">");
                                strHTMLBuffer.append("<script language=\"JavaScript\">document.forms[\"editDataForm\"][\"").append(strFieldName).append("\"].fieldLabel=\"").append(COLUMN_NAME).append("\";</script>");
                                strCellValue = strHTMLBuffer.toString();
                            }
                            else {
                            	strCellValue = strDueDate;
                            }
                        }
                        else {
                        	strCellValue = strDueDate;
                        }
                    }
                }
                else if (INFO_TYPE_EMPTY_ROW.equals(strInfoType)) {
                	strCellValue = DomainConstants.EMPTY_STRING;
                }
                else {
                    throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.LifeCycle.InvalidTypeOfTaskObject", context.getLocale().getLanguage()));
                }
                strCellValue = (UIUtil.isNotNullAndNotEmpty(strCellValue)?(strCellValue):(DomainConstants.EMPTY_STRING));
                vecResult.add(strCellValue);
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Updates Due Date For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return 0 for success and 1 for error
     * @throws Exception
     */
   
    public  int updateDueDateForApprovals (Context context, String[] args) throws Exception {
        try {
            int result = 0;
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            HashMap requestMap = (HashMap)programMap.get("requestMap");
            String strNewValue = (String) paramMap.get("New Value");
            String strOldValue = (String) paramMap.get("Old value");
            Map mapResult = new HashMap();
            if (strNewValue == null) {
                strNewValue = "";
            }
            if (strOldValue == null) {
                strOldValue = "";
            }
            String timeZone="";
            if(requestMap.get("timeZone") instanceof String){
            	timeZone = (String)requestMap.get("timeZone");
            } else if(requestMap.get("timeZone") instanceof String[]) {
                timeZone = ((String[])requestMap.get("timeZone"))[0];
            }
            double iClientTimeOffset = (new Double(timeZone)).doubleValue();
            
            Locale localeObj =Locale.US;
            if (requestMap.get("localeObj")  instanceof Locale) {
                localeObj = (Locale) requestMap.get("localeObj");
            } else if(requestMap.get("localeObject")  instanceof Locale) {
            	localeObj = (Locale) requestMap.get("localeObject");
            } else if (requestMap.get("localeObj")  instanceof String[]) {
                String[] tempObj = (String[]) requestMap.get("localeObj");
                localeObj = new Locale((String)(tempObj[0]));
            }
            
            i18nNow loc = new i18nNow();
            String strLanguage = (String)programMap.get("languageStr");
            final String RESOURCE_BUNDLE = "emxFrameworkStringResource";
            final String STRING_PAST_DUE_DATE = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxFramework.Alert.CantHavePastDateForTaskDueDate");
            
            String strIds = (String) paramMap.get("objectId");
            StringList stringList = FrameworkUtil.split(strIds, "^");
            DomainRelationship domainRelationship = null;
            String strRouteNodeId = "";
            
            
            if(!stringList.isEmpty()){

               String strObjectId = (String)stringList.get(3);
               if(!"".equals(strObjectId)){
                    if (strNewValue != null && !"".equals(strNewValue) && !strNewValue.equals(strOldValue)) {
                   DomainObject domainObject = new DomainObject(strObjectId);
                        
                        Calendar calCurrent = Calendar.getInstance();
                        // Clear time part of the date and compare only dates
                        calCurrent.set(Calendar.HOUR_OF_DAY, 0);
                        calCurrent.set(Calendar.MINUTE, 0);
                        calCurrent.set(Calendar.SECOND, 0);
                        calCurrent.set(Calendar.MILLISECOND, 0);
                        Date dtCurrentDate = calCurrent.getTime();

                        String formatDueDate=eMatrixDateFormat.getFormattedInputDate(context,strNewValue,iClientTimeOffset,localeObj);
                        Date dtDueDate = eMatrixDateFormat.getJavaDate(formatDueDate,localeObj);
                        Calendar calDueDate = Calendar.getInstance();
                        calDueDate.setTime(dtDueDate);
                        // Clear time part of the date and compare only dates
                        calDueDate.set(Calendar.HOUR_OF_DAY, 0);
                        calDueDate.set(Calendar.MINUTE, 0);
                        calDueDate.set(Calendar.SECOND, 0);
                        calDueDate.set(Calendar.MILLISECOND, 0);
                        dtDueDate = calDueDate.getTime();

                        if (dtDueDate.before(dtCurrentDate)) {
                            throw new MatrixException(STRING_PAST_DUE_DATE);
                        }
                        
                   if (domainObject.exists(context)) {
                	   domainObject.setAttributeValue(context, DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE,formatDueDate);
                       
                       //Getting the route node id
                       strRouteNodeId = domainObject.getInfo(context, SELECT_ATTRIBUTE_ROUTE_NODE_ID);
                       domainRelationship = new DomainRelationship(strRouteNodeId);
                   }
                   else {
                       domainRelationship = new DomainRelationship(strObjectId);
                   }
                   
                   //setting date attribute of Route node Id
                   domainRelationship.setAttributeValue(context, DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE,formatDueDate);
                        result = 0;
                    }
               }
            }
            return result;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets Completed Date For Approvals Table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return The vector containing values for this column
     * @throws Exception
     */
    public  Vector getCompletedDateForApprovals(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
            Map paramList = (Map)programMap.get("paramList");
            String ParentObjId = (String)paramList.get("objectId");
            DomainObject dmoObject = new DomainObject(ParentObjId);
            String strCurrentParentState = dmoObject.getInfo(context, DomainObject.SELECT_CURRENT);
            int iCountStop = 0;// variable used to stop displaying the completed date for those states if they had any demotions earlier
             // Do for each object
            String strInfoType = null;
            String strCurrentState = null;
            String strParentObjectState = null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map mapObjectInfo = (Map) itrObjects.next();
                strInfoType = (String)mapObjectInfo.get("infoType");
                strCurrentState = (String)mapObjectInfo.get("currentState");
                strParentObjectState = (String)mapObjectInfo.get("parentObjectState");

                if (INFO_TYPE_ACTIVATED_TASK.equals(strInfoType) || INFO_TYPE_DEFINED_TASK.equals(strInfoType)) {
                    if (POLICY_INBOX_TASK_STATE_COMPLETE.equalsIgnoreCase(strCurrentState)) {
                        vecResult.add((String)mapObjectInfo.get("completionDate"));
                    }
                    else{
                        vecResult.add("");
                    }
                }
                else if (INFO_TYPE_SIGNATURE.equals(strInfoType)){
                    if("true".equalsIgnoreCase((String)mapObjectInfo.get("signed"))){
                        if(!(strParentObjectState.equalsIgnoreCase(strCurrentParentState))){
                            vecResult.add((String)mapObjectInfo.get("completionDate"));
                        }
                        else{
                            vecResult.add("");
                        }
                    }
                    else{
                        vecResult.add("");
                    }
                }
                else if(INFO_TYPE_EMPTY_ROW.equals(strInfoType)){
                	// do not display the completed date value if currentstate and also for future states
                 	if(!strParentObjectState.equalsIgnoreCase(strCurrentParentState) && iCountStop == 0 ){
                	   vecResult.add((String)mapObjectInfo.get("actualDate"));
                	} else {
                		iCountStop++;
                		vecResult.add("");
                	}
                }
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    //method to Check whether the given signature Name is Role or Group 
    //Returns boolean true or false
    public boolean isRoleOrGroup(Context context,String roleOrGroupName, String sSignName) throws matrix.util.MatrixException {
        
        StringBuffer strb = new StringBuffer(80);
        strb.append("list " + roleOrGroupName + " '");
        strb.append(sSignName);
        strb.append("'");
        matrix.db.MQLCommand command = new matrix.db.MQLCommand();
        command.executeCommand(context, strb.toString());
        if(command.getResult().trim().equals("")){
            return false;
        }
        return true;
    }
    
    public String getTypesToMassApprove(Context context, String[] args) throws Exception {
        
        boolean IT_APPROVE = false;
        boolean SIGNATURE = false;
        MapList tasks = getCurrentAssignedTaskSignaturesOnObject(context, args[0]);
        for (Iterator iter = tasks.iterator(); iter.hasNext();) {
            Map taskInfo = (Map) iter.next();
            String ValidApprovalStatusAction = (String) taskInfo.get("ValidApprovalStatusAction");
            if(!IT_APPROVE) {
                IT_APPROVE = "IT-Approve".equals(ValidApprovalStatusAction);
            }
            if(!SIGNATURE) {
                SIGNATURE = "Sign-Approve".equals(ValidApprovalStatusAction);
            }
            if(IT_APPROVE && SIGNATURE)
                break;
        }
        String typesToMassApprove = "None";
        if(IT_APPROVE && SIGNATURE) {
            typesToMassApprove = "Both";
        } else if(IT_APPROVE) {
            typesToMassApprove = "IT";
        } else if(SIGNATURE) {
            typesToMassApprove = "Signature";
        }
        return typesToMassApprove;
    }
	
	
	/*Checks for the PROMOTE button visibility  on object LifeCycle*/
    public boolean hasNextState(Context context, String[] args) throws Exception 
    {

    	HashMap programMap = (HashMap) JPO.unpackArgs(args);	 
    	String objectId =  (String)programMap.get("objectId");	
    	if(objectId != null && objectId.length() > 0)
    	{
    		DomainObject busObject = new DomainObject(objectId);
    		String policyName = busObject.getPolicy(context).getName();
    		StateList statesList  = LifeCyclePolicyDetails.getStateList(context, busObject, policyName);
    		String currentState = busObject.getInfo(context, DomainObject.SELECT_CURRENT);
    		StringList hiddenStates = LifeCyclePolicyDetails.getHiddenStatesName(context, policyName);
    		int lastIndx=(statesList.size()) - 1;
    		return !((State)statesList.get(lastIndx)).isCurrent() && !hiddenStates.contains(currentState);
    	}
    	return false;   	

    }
    
    
    
    /*Checks for the DEMOTE button visibility  on object LifeCycle*/
    public boolean hasPreviousState(Context context, String[] args) throws Exception 
    {
   	 HashMap programMap = (HashMap) JPO.unpackArgs(args);	 
   	 String objectId =  (String)programMap.get("objectId");	 
     
        if(objectId != null && objectId.length() > 0)
        {
        	DomainObject busObject = new DomainObject(objectId);
    		String policyName = busObject.getPolicy(context).getName();
    		StateList statesList  = LifeCyclePolicyDetails.getStateList(context, busObject, policyName);
    		String currentState = busObject.getInfo(context, DomainObject.SELECT_CURRENT);
    		StringList hiddenStates = LifeCyclePolicyDetails.getHiddenStatesName(context, policyName);
    		return !((State)statesList.get(0)).isCurrent() && !hiddenStates.contains(currentState);
       }
        return false;   	
    }
    
   /**
     * Checks for the Add Approver commands button visibility  on object LifeCycle Approvals channel
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Packed arguments having table data
     * @return boolean to either enable or disable the commands
     * @throws Exception
     */
    
      public boolean showAddApproverCommands(Context context, String[] args) throws Exception {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);    
            String strObjectId =  (String)programMap.get("objectId");
            boolean showCommand = false;
            String strType = "";
            String strRel = DomainRelationship.RELATIONSHIP_OBJECT_ROUTE;
     
            if(strObjectId != null && strObjectId.length() > 0) {
                  DomainObject doObj = DomainObject.newInstance(context,strObjectId);
                  strType = doObj.getInfo(context,DomainConstants.SELECT_TYPE);
                  Map allowedTypes = DomainRelationship.getAllowedTypes(context, strRel, false);
            
                  if(!allowedTypes.isEmpty()) {
                        String strFromType = (String) allowedTypes.get(DomainRelationship.KEY_INCLUDE);
                        StringList strlTypes = FrameworkUtil.split(strFromType,",");
                        String strSymbolicType = FrameworkUtil.getAliasForAdmin(context, "type", strType, true);
                        showCommand = strlTypes.contains(strSymbolicType) ? true : false;
                  }
            }
            
            return showCommand;       
      }

      /**
       * To get I18State names
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args Packed arguments having policy and state name
       * @return i18State name
       */
      public String getI18State(Context context, String[] args) throws FrameworkException,IOException,MatrixException {
    	  String policyName = args[0];
    	  String stateName = args[1];
    	  com.matrixone.apps.domain.util.i18nNow loc = new com.matrixone.apps.domain.util.i18nNow();
          String i18State=loc.getStateI18NString(policyName, stateName, context.getLocale().getLanguage());
    	  BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
    	  writer.write(i18State);
    	  writer.flush();
    	  return i18State;
      }  
	  
	  public void createNewRoute(Context context,String strRouteId,Route route,String strParentObjId,String strParentObjectState )throws Exception{

        //If there are no routes on the state selected create a new route
        String strRouteName = FrameworkUtil.autoName(context,
                                                     "type_Route",
                                                     new Policy(DomainObject.POLICY_ROUTE).getFirstInSequence(context),
                                                     "policy_Route",
                                                     null,
                                                     null,
                                                     true,
                                                     true);

        //
        // Create new route object
        //
        route.createObject(context, DomainConstants.TYPE_ROUTE, strRouteName, null, DomainObject.POLICY_ROUTE, null);

        //
        // Connect the Route Owner
        //
        String strPersonOwner = context.getUser();
        String strPersonOwnerId = PersonUtil.getPersonObjectID(context, strPersonOwner);

        //Connect route to the owner
        route.addRelatedObject(context, new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_ROUTE), false, strPersonOwnerId);

        //connect content to the route
        HashMap mapState = new HashMap();
        mapState.put(strParentObjId, strParentObjectState);
        route.AddContent(context, new String[]{strParentObjId}, mapState);

        strRouteId = route.getId();
       
    
    }
	  
}
