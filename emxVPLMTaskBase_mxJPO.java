/* emxVPLMTaskBase.java

   Copyright (c) 2007-2016 Dassault Systemes, Inc.
   All Rights Reserved.
   This program contains proprietary and trade secret information of Dassault Systemes,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.1.1.15.1.4.2.2 Thu Dec  4 07:55:25 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.15.1.4.2.1 Thu Dec  4 01:53:40 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.15.1.4 Wed Oct 22 15:49:48 2008 przemek Experimental przemek $
*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;


/**
 * The <code>emxVPLMTaskBase</code> class represents the VPLM Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.7.SP1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxVPLMTaskBase_mxJPO extends emxDomainObject_mxJPO
{



    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxVPLMTaskBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
    /** The project access list id relative to this object. */
    static protected final String SELECT_PROJECT_ACCESS_LIST_ID =
        "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";
    /**
     * This function verifies if a user has PROJECT_LEAD access.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private boolean hasProjectLeadAccess(Context context)
        throws Exception
    {

        //initialize result
        boolean result = false;

        // Check to see if user is Project Assessor
        //String[] args2 = new String[] {"PROJECT_LEAD"};
        //result = hasAccess(context, args2);
        // call emxTaskBase ,, for now
        result = true;

        DomainObject accessListObject = null;
        String accessListID = getInfo(context, SELECT_PROJECT_ACCESS_LIST_ID);
        if ((accessListID != null) && !"".equals(accessListID))
        {
           accessListObject = DomainObject.newInstance(context, accessListID);
        }

        if (accessListObject != null)
        {
            int iAccess = AccessConstants.cModify;
            if (accessListObject.checkAccess(context, (short) iAccess))
            {
                result = true;
            }
        }
        
        return result;
    }

    /**
     * This function gets a VPLM Action attribute given the Action
     *
     * @param context the eMatrix <code>Context</code> object
     * @param actionObj action object
     * @param attributeName attribute to get value of
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private String getActionAttributeValue(Context context, DomainObject actionObj, String attributeName)
        throws Exception
    {

        //Get attribute BO name
        String attributeBOName = PropertyUtil.getSchemaProperty(context, attributeName);
        String attributeValue = actionObj.getAttributeValue(context,  attributeBOName );

        return attributeValue;
    }

    /**
     * This function gets a given character from a given string
     *
     * @param context the eMatrix <code>Context</code> object
     * @param inString the string from which the character is to be removed
     * @param removeChar the character to remove
     * @throws Exception if the operation fails
     * @since AEF X+2
     */
    private String removeChar(Context context, String inString, String removeChar)
        throws Exception
    {
        String returnString = inString;

        //Remove character from the beginning
        while (returnString.substring(0,1) == removeChar) 
        returnString = returnString.substring(1, returnString.length());

        //Remove character from the end
        while (returnString.substring(returnString.length()-1,returnString.length()) == removeChar)
        returnString = returnString.substring(0, returnString.length()-1);

        //Remove character from the middle
        int charIndex = returnString.indexOf(removeChar);
        while (charIndex > 0) 
        {
            returnString = returnString.substring(0, charIndex) + returnString.substring(charIndex+1, returnString.length());
            charIndex = returnString.indexOf(removeChar);
        }

        return returnString;
    }

    /**
     * This function gets a VPLM Action status equivalent of PMC task status specific
     * to a VPLM instance and Action type
     *
     * @param context the eMatrix <code>Context</code> object
     * @param task status vaule to check
     * @throws Exception if the operation fails
     * @since AEF X+2
     */
    private String getActionStatusEquivalent(Context context, String taskStatus)
        throws Exception
    {

        //Create property tag
        String actionStatus = "";
        String tagPrefix = "emxProgramCentral.VPLMMapping.";

        //Get Action Type
        String propertyActionType = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMActionType);
        String actionType = getAttributeValue(context, propertyActionType );
        actionType = removeChar(context, actionType, " ");

        //Get VPLM instance
        String propertyActionInstance = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMInstance);
        String vplmVault = getAttributeValue(context, propertyActionInstance);
        vplmVault = removeChar(context, vplmVault, " ");

        String propertyTag = tagPrefix + vplmVault + "." + actionType + "." + taskStatus;
        actionStatus = EnoviaResourceBundle.getProperty(context, propertyTag);

        return actionStatus;
    }

    /**
     * This function syncroizes the VPLM Task owner to the VPLM Action owner, if needed
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private void synchTaskOwnerToActionOwner(Context context)
        throws Exception
    {

        //Get corresponding action
        //DomainObject actionObj = getCorrespondingAction(context);
        String actionId = getCorrespondingActionId(context);
        DomainObject actionObj = DomainObject.newInstance(context, actionId);

        //Get action owner
        String actionOwner = actionObj.getInfo(context, SELECT_OWNER);
        //String actionOwner = getActionAttributeValue(context,  actionObj, SYMBOLIC_attribute_ENOVIA_AFLAction_V_user );

        //Get task owner
        String taskOwner = getVPLMTaskOwner(context);

        // If the owners don't match, update the task owner to the Action
        if (!actionOwner.equals(taskOwner)) this.setOwner(context, actionOwner);

    }
    /**
     * This function verifies the task is a VPLM Task and in a Project.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
        private boolean isVPLMTaskAndInProject(Context context)
        throws Exception
    {

        //initialize result
        boolean result = false;

        String taskType = getInfo(context, SELECT_TYPE);
        //check if task is a VPLM task
        String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);
        if (taskType.equals(vplmTask))
        {
           
           //String subtaskRelation = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Subtask);
           //String toClause = "to[" + subtaskRelation + "].from.type";
           //String parentType = getInfo(context, toClause);

           //check if parent task is a Project Template
           String rootNodeType = getInfo(context, "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type");

           String templateType = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_ProjectTemplate);
           String conceptType = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_ProjectConcept);
           if (templateType.equals(rootNodeType)) result = false;
           else result = true;
        }
        return result;
    }

    /**
     * This function gets the OID of a VPLM Task corresponding Action
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private String getCorrespondingActionId(Context context)
        throws Exception
    {

        //DomainObject taskObj = DomainObject.newInstance(context, taskId);

        //get Action Id
        String actionRelation = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
        String actionId = getInfo(context, "from["+actionRelation+"].to.id");


        return actionId;
    }

     /**
     * This function gets the VPLM Task corresponding Action
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private DomainObject getCorrespondingAction(Context context)
        throws Exception
    {

        //DomainObject taskObj = DomainObject.newInstance(context, taskId);
        //get corresponding Action
        String actionId = getCorrespondingActionId(context);
        //DomainObject actionObj = DomainObject.newInstance(context, actionId);
        //get Action Id
        //String actionRelation = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
        //String actionId = getInfo(context, "from["+actionRelation+"].to.id");
        DomainObject actionObj = DomainObject.newInstance(context, actionId);


        return actionObj;
    }

    /**
     * This function updates the Action planning Attributes.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param actionObj action object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private void updateActionPlanningAttributes(Context context, DomainObject actionObj)
        throws Exception
    {

        //update Action attributes
        Map actionMap = new HashMap();
        String actionDuration = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_duration);
        String actionPlannedStart = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_start_date);
        String actionPlannedFinish = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_end_date);
        String taskDuration = getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_DURATION);
        //check for zero duration and remove decimal
        if (!taskDuration.equals("0.0") && taskDuration != null)
        {
            if(taskDuration.indexOf(".")>-1) {
                taskDuration = taskDuration.substring(0,taskDuration.indexOf("."));
            }
            actionMap.put(actionDuration, taskDuration);
        }
        //temp disable date send
        actionMap.put(actionPlannedStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE));
        actionMap.put(actionPlannedFinish, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));
        actionObj.setAttributeValues(context, actionMap);

    }

    /**
     * This function gets the task owner of a VPLM Task
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private String getVPLMTaskOwner(Context context)
        throws Exception
    {

        return this.getOwner(context).getName();
    }
    /**
     * This function verifies if a specific user is in a specific group.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param userId person to check
     * @param groupId group to check
     * @throws Exception if the operation fails
     * @since AEF 10.7.1.0
     */
    private boolean isPersonInGroup(Context context, String userId, String groupId)
        throws Exception
    {

        //initialize result
        boolean result = false;

        //check if the user belongs to the group
        //String cmd = "print person  '" + userId + "' select isassigned[" + groupId + "] dump |";
        String cmd = "print person  $1 select $2 dump $3";
        String strResult = MqlUtil.mqlCommand(context,cmd,userId,"isassigned[" + groupId + "]","|");
        if (strResult.toLowerCase().equals("true")) result = true;

        return result;
    }

    /**
     * This function creates a VPLM Action and links it to the parent VPLM Task
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     *        1 - String containing Task Type
     *        2 - String containing Task Name
     *        3 - String containing Task Revision
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
    public int triggerActionVPLMTaskCreate(Context context, String[] args)
        throws Exception
    {
        return 0;
       
    }
    /**
     * This function creates a VPLM Action and links it to the parent VPLM Task
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     *        1 - String containing Task Type
     *        2 - String containing Task Name
     *        3 - String containing Task Revision
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
    private String triggerActionVPLMTaskCreate(Context context, String[] args, boolean internal)
        throws Exception
    {
        //int returnValue = 1;
        //  just in case this action already exits then proceed to promote
        //String actionId = getCorrespondingActionId(context);
        //DomainObject actionObj = DomainObject.newInstance(context, actionId);
        //if (actionObj. != null) return 0;
        String vplmActionId = null;    
        //try
        //{
        //ContextUtil.startTransaction(context, true);
        
        //check task type and parent
        String taskType = getInfo(context, SELECT_TYPE);
        if (!isVPLMTaskAndInProject(context)) return null;
        
        
        String actionName = this.getName() + this.getRevision();

        String actionRevision = "Dummy"; 
        String propertyActionType = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMActionType);
        String actionType = this.getAttributeValue(context, propertyActionType );
       
        String propertyActionInstance = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMInstance);
        String vplmVault = this.getAttributeValue(context, propertyActionInstance);
        
        String relationship = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction); // this also comes in the args
        
        //create and connect action ,, bring this back 
        //DomainRelationship actionDR = createAndConnect(context, actionType, actionName, actionRevision, null, 
        //                                                vplmVault, null, null, true);
        
        
        DomainObject vplmAction = DomainObject.newInstance(context);
     
        vplmAction.createObject(context, actionType, actionName, actionRevision, null,vplmVault);
        
        vplmActionId = vplmAction.getObjectId();
        
        
        //if ( actionDR == null) return 1;
        String command = null;
        String result  = null;
        MQLCommand mqlCommand = null;
        
        //String actionPolicy = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMActionPolicy.Default");
        //command = "add bus \'" + actionType + "\' \'" + actionName + "\' \'" + actionRevision + "\' policy \'" + actionPolicy + "\' vault \'" + vplmVault +"\'";
        ////System.out.println("MQL command ..."+command);
        ////mqlLogWriter ( "Add dummy bus object = " + command + "\n");
        //result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
        //System.out.println("MQL command result ..."+result);
        //mqlLogWriter ( "Add dummy bus object = " + result + "\n");

        String taskNmae = this.getName();
        String taskRevision = this.getRevision();;
       // command = "connect bus \'" + taskType + "\' \'" + taskNmae + "\' \'" + taskRevision + "\' relationship \'" + relationship + "\' to \'" + actionType +"\' \'" + actionName + "\' \'" + actionRevision + "\'" ;
        command = "connect bus $1 $2 $3 $4 $5 to $6 $7 $8";
        ////System.out.println("MQL command ..."+command);
        result  = MqlUtil.mqlCommand(context,command,taskType,taskNmae,taskRevision,"relationship",relationship,actionType,actionName,actionRevision);
        // nothing to evaluate on result
        
        //}
        //catch(Exception ex)
        //{
        //    ContextUtil.abortTransaction(context);
        //}
        //finally
        //{
            
        //    ContextUtil.commitTransaction(context);
        //}
        return vplmActionId;
       

    }

    /**
    * This function marks a VPLM Action when the corresponding
    * VPLM Task is deleted
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - String containing the object id
    * @throws Exception if operation fails
    * @since AEF 10.7.1.0
    */
    public void triggerActionVPLMTaskDelete(Context context, String[] args)

        throws Exception
    {
        try
        {
            //get values from args.
            String taskId = args[0];
            setId(taskId);
            //Check if trigger should be bypassed
            if (isVPLMTaskAndInProject(context))
            {
    
                //get corresponding Action
                String actionId = getCorrespondingActionId(context);
                DomainObject actionObj = DomainObject.newInstance(context, actionId);
    
 
                //delete Action if enabled
                String deleteAction = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.DeleteAction");
                if (!deleteAction.equals("Y"))
                {
                    //set Action description
                    String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
                    		"emxProgramCentral.VPLM.MSG_Defunct", context.getSession().getLanguage());
                    actionObj.setDescription(context, strMsg);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }

    }


    /**
    * This function promotes VPLM Action
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - String containing the object id
    * @return int based on success or failure
    * @throws Exception if operation fails
    * @since AEF 10.7.1.0
    */
    public int triggerCheckVPLMTaskCreatePromote(Context context, String[] args)
        throws Exception
    {
        // get values from args.
        String taskId = args[0];
        setId(taskId);
        //Check if trigger should be bypassed
        if (!isVPLMTaskAndInProject(context)) return 0;

        //get owner
        String taskOwner = getVPLMTaskOwner(context);

        //check if owner is in proper group
        //DomainObject taskObj = DomainObject.newInstance(context, taskId);
        String actionInstance = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMInstance);
        String vaultGroup = getAttributeValue(context, actionInstance);
        
        if( !isPersonInGroup(context, taskOwner, vaultGroup)) 
        {
            String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
            		"emxProgramCentral.VPLM.MSG_NotInGroup", context.getSession().getLanguage());
            emxContextUtil_mxJPO.mqlNotice(context, strMsg);
            return 1;
        }


        //get corresponding action
        String actionId = getCorrespondingActionId(context);
        DomainObject actionObj = DomainObject.newInstance(context, actionId);
        
        //make sure the Action and Task have the same owner
        String actionOwner = actionObj.getInfo(context, SELECT_OWNER);
        if (!taskOwner.equals(actionOwner)) actionObj.setOwner(context,taskOwner);

        //make sure Action isn't complete
        //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
        //String actionStatus = actionObj.getAttributeValue(context, actionStat);
        String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
        //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.VPLM.DesignAction.Complete");
        String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
        if (actionStatus.equals(VPLM_Complete_Equivanent)) return 0; 
        //Set description
        String taskDescription = getInfo(context, SELECT_DESCRIPTION);
        actionObj.setDescription(context, taskDescription);
        //update Action attributes
        Map actionMap = new HashMap();
        String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
        String actionDuration = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_duration);
        String actionStart = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_start_date);
        String actionFinish = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_end_date);
        String actionAbstract = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_abstract);
        String actionReference = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_taskReference);
        actionMap.put(actionDirInt, "DirectIntegration02");
        //actionObj.setAttributeValue(context, actionDirInt, "DirectIntegration02");
        String taskDuration = getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_DURATION);
        if (taskDuration.equals("0.0") || taskDuration == null) taskDuration = "1";
        else
        {
            if(taskDuration.indexOf(".")>-1) {
                taskDuration = taskDuration.substring(0,taskDuration.indexOf("."));
            }
        }
        actionMap.put(actionReference, taskId);
        //actionObj.setAttributeValue(context, actionReference, taskId);
        actionMap.put(actionDuration, taskDuration);
        //actionObj.setAttributeValue(context, actionDuration, taskDuration);
        //temp disable date send
        actionMap.put(actionStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE));
        //actionObj.setAttributeValue(context, actionStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE));
        actionMap.put(actionFinish, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));
        //actionObj.setAttributeValue(context, actionStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));
        String actionName = getInfo(context, SELECT_NAME);
        actionMap.put(actionAbstract, actionName);
        //actionObj.setAttributeValue(context, actionAbstract, actionName);
        actionObj.setAttributeValues(context, actionMap);

        //get status to see if promotion is needed
        //String VPLM_Create_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.VPLM.DesignAction.Create");
        String VPLM_Create_Equivanent = getActionStatusEquivalent(context, "Create");

        //promote Action
        if (actionStatus.equals(VPLM_Create_Equivanent))
        {
            //String VPLM_Assign_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Assign");
            String VPLM_Assign_Equivanent = getActionStatusEquivalent(context, "Assign");
            actionObj.setState(context, VPLM_Assign_Equivanent);
        }


        return 0;

    }

    /**
    * This function demotes VPLM Action to Create Status
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - String containing the object id
    * @throws Exception if operation fails
    * @since AEF 10.7.1.0
    */
    public void triggerActionVPLMTaskAssignDemote(Context context, String[] args)
        throws Exception
    {
        //get values from args.
        String taskId = args[0];
        setId(taskId);
        //Check if trigger should be bypassed
        if (isVPLMTaskAndInProject(context))
        {

            //get corresponding action
            String actionId = getCorrespondingActionId(context);
            DomainObject actionObj = DomainObject.newInstance(context, actionId);

            //make sure Action isn't complete
            //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
            //String actionStatus = actionObj.getAttributeValue(context, actionStat);
            String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
            //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
            String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
            if (!actionStatus.equals(VPLM_Complete_Equivanent))
            {

                //update Action attributes
                Map actionMap = new HashMap();
                String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
                String actionDuration = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_duration);
                String actionStart = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_start_date);
                String actionFinish = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_end_date);
                actionMap.put(actionDirInt, "DirectIntegration01");
                String taskDuration = getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_DURATION);
                //check for zero duration and remove decimal
                if (!taskDuration.equals("0.0") && taskDuration != null)
                {
                    if(taskDuration.indexOf(".")>-1) taskDuration = taskDuration.substring(0,taskDuration.indexOf("."));
                    actionMap.put(actionDuration, taskDuration);
                }
                // temp disable date send
                actionMap.put(actionStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE));
                actionMap.put(actionFinish, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));
                actionObj.setAttributeValues(context, actionMap);

            }
            //  Synchronize owners 
            synchTaskOwnerToActionOwner(context);
        }


    }

    /**
    * This function promotes VPLM Action from Active to Review Status
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - String containing the object id
    * @throws Exception if operation fails
    * @since AEF 10.7.1.0
    */
    public void triggerActionVPLMTaskActivePromote(Context context, String[] args)
        throws Exception
    {
        //get values from args.
        String taskId = args[0];
        setId(taskId);
        //Check if trigger should be bypassed
        if (isVPLMTaskAndInProject(context))
        {

            //get corresponding action
            String actionId = getCorrespondingActionId(context);
            DomainObject actionObj = DomainObject.newInstance(context, actionId);

            //make sure Action isn't complete
            //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
            //String actionStatus = actionObj.getAttributeValue(context, actionStat);
            String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
            //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
            String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
            if (!actionStatus.equals(VPLM_Complete_Equivanent))
            {

                //update Action attributes
                Map actionMap = new HashMap();
                String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
                String actionDuration = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_duration);
                String actionStart = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_start_date);
                String actionFinish = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_end_date);
                actionMap.put(actionDirInt, "DirectIntegration01");
                String taskDuration = getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_DURATION);
                //check for zero duration and remove decimal
                if (!taskDuration.equals("0.0") && taskDuration != null)
                {
                    if(taskDuration.indexOf(".")>-1) taskDuration = taskDuration.substring(0,taskDuration.indexOf("."));
                    actionMap.put(actionDuration, taskDuration);
                }
//              temp disable date send
                actionMap.put(actionStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE));
                actionMap.put(actionFinish, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));
                actionObj.setAttributeValues(context, actionMap);

            }
//          Synchronize owners
            synchTaskOwnerToActionOwner(context);
        }


    }

    /**
     * This function demotes a VPLM Action from Review Check
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskReviewDemote(Context context, String[] args)
         throws Exception
     {
         // get values from args.
         String taskId = args[0];
         setId(taskId);
         //Check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) return 0;


         //get corresponding action
         String actionId = getCorrespondingActionId(context);
         DomainObject actionObj = DomainObject.newInstance(context, actionId);

         //make sure Action isn't complete
         //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
         //String actionStatus = actionObj.getAttributeValue(context, actionStat);
         String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
         //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
         String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
         if (actionStatus.equals(VPLM_Complete_Equivanent)) return 0;

         //make sure the Action and Task have the same owner
         String taskOwner = getVPLMTaskOwner(context);
         String actionOwner = actionObj.getInfo(context, SELECT_OWNER);
         if (!taskOwner.equals(actionOwner)) actionObj.setOwner(context,taskOwner);
         
         //update Action attributes
         Map actionMap = new HashMap();
         String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
         String actionDuration = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_duration);
         String actionStart = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_start_date);
         String actionFinish = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_end_date);
         String actionReference = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_taskReference);
         
         actionMap.put(actionDirInt, "DirectIntegration02");
         actionMap.put(actionReference, taskId);
         String taskDuration = getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_DURATION);
         //check for zero duration and remove decimal
         if (!taskDuration.equals("0.0") && taskDuration != null)
         {
             if(taskDuration.indexOf(".")>-1) taskDuration = taskDuration.substring(0,taskDuration.indexOf("."));
             actionMap.put(actionDuration, taskDuration);
         }
         //temp disable date send
         actionMap.put(actionStart, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_START_DATE));
         actionMap.put(actionFinish, getAttributeValue(context, ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));
         actionObj.setAttributeValues(context, actionMap);

         //get status to see if promotion is needed
         //String VPLM_Review_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Review");
         String VPLM_Review_Equivanent = getActionStatusEquivalent(context, "Review");

         //demote Action
         if (actionStatus.equals(VPLM_Review_Equivanent))
         {
             //String VPLM_Active_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Active");
             String VPLM_Active_Equivanent = getActionStatusEquivalent(context, "Active");
             actionObj.setState(context, VPLM_Active_Equivanent);
         }


         return 0;

     }

    /**
     * This function promotes a VPLM Action from Review Check
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskReviewPromote(Context context, String[] args)
         throws Exception
     {
         // get values from args.
         String taskId = args[0];
         setId(taskId);
         //Check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) return 0;

         //get corresponding action
         String actionId = getCorrespondingActionId(context);
         DomainObject actionObj = DomainObject.newInstance(context, actionId);

         //make sure Action isn't complete
         //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
         //String actionStatus = actionObj.getAttributeValue(context, actionStat);
         String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
         //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
         String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
         if (actionStatus.equals(VPLM_Complete_Equivanent)) return 0;

         //set direct integration flag
         String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
         actionObj.setAttributeValue(context, actionDirInt, "DirectIntegration03");

         //get status to see if promotion is needed
         //String VPLM_Review_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Review");
         String VPLM_Review_Equivanent = getActionStatusEquivalent(context, "Review");

         //check if Action is in Review status
         if (!actionStatus.equals(VPLM_Review_Equivanent)) 
         {
            //${CLASS:emxContextUtil}.mqlNotice(context,"The corresponding VPLM Action status must be in its equivalent of Review for the VPLM Task to be completed.  It's status is " + actionStatus + "." );
            String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
            		"emxProgramCentral.VPLM.MSG_ActionNotReview", context.getSession().getLanguage());
            emxContextUtil_mxJPO.mqlNotice(context, strMsg);
            return 1;
         }

         return 0;

     }

    /**
     * This function demotes a VPLM Action from Complete Check
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskCompleteDemote(Context context, String[] args)
         throws Exception
     {
         // get values from args.
         String taskId = args[0];
         setId(taskId);
         //Check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) return 0;

         //get corresponding action
         String actionId = getCorrespondingActionId(context);
         DomainObject actionObj = DomainObject.newInstance(context, actionId);

         //make sure Action is complete
         //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
         //String actionStatus = actionObj.getAttributeValue(context, actionStat);
         String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
         //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
         String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
         if (!actionStatus.equals(VPLM_Complete_Equivanent)) return 0;

         // demote Action to review
         //String VPLM_Review_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Review");
         String VPLM_Review_Equivanent = getActionStatusEquivalent(context, "Review");
         actionObj.setState(context, VPLM_Review_Equivanent);
         actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
         if (actionStatus.equals(VPLM_Complete_Equivanent)) 
         {
                //${CLASS:emxContextUtil}.mqlNotice(context,"The VPLM Task cannot be demoted to Review because its corresponding VPLM Action is not allowed to be demoted from its Complete status." );
                String strMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
                		"emxProgramCentral.VPLM.MSG_ActionNotDemoted", context.getSession().getLanguage());
                emxContextUtil_mxJPO.mqlNotice(context, strMsg);
                return 1;
         }
         //set direct integration flag
         //String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
         //actionObj.setAttributeValue(context, actionDirInt, "DirectIntegration03");


         return 0;

     }
    /**
     * This function will verify VPLM Action demotion to Review
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public void triggerActionVPLMTaskCompleteDemote(Context context, String[] args)
         throws Exception
     {
         //get values from args.
         String taskId = args[0];
         setId(taskId);
         //Check if trigger should be bypassed
         if (isVPLMTaskAndInProject(context))
         {

             //get corresponding action
             String actionId = getCorrespondingActionId(context);
             DomainObject actionObj = DomainObject.newInstance(context, actionId);

             //make sure Action is in Review status
             //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
             //String actionStatus = actionObj.getAttributeValue(context,  actionStat );
             String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
             //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
             String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
             //String VPLM_Review_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Review");
             String VPLM_Review_Equivanent = getActionStatusEquivalent(context, "Review");
             if (actionStatus.equals(VPLM_Review_Equivanent) || !actionStatus.equals(VPLM_Complete_Equivanent))
             {

                 //set direct integration flag
                 String actionDirInt = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_dirIntFlag);
                 actionObj.setAttributeValue(context, actionDirInt, "DirectIntegration01");

             }
         }
     }

     /**
     * gets the list of deliverables for a VPLM task
     * Used for APPDocumentSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - task OID
     * @returns Object
     * @throws Exception if the operation fails
     * @since Program Central 10.7.SP1
     * @grade 0
     */
    public MapList getDeliverables(Context context, String[] args)
        throws Exception
    {
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        String  taskId          = (String) programMap.get("objectId");
        //String  parentRel         = (String) programMap.get("parentRelName");
        setId(taskId);
        MapList documentList = new MapList();

        try
        {
            if (isVPLMTaskAndInProject(context))
            {
                String objectWhere = "";
                StringList typeSelects = new StringList(1);
                typeSelects.add(SELECT_ID);
                StringList relSelects = new StringList(1);
                relSelects.add(SELECT_RELATIONSHIP_ID);

                DomainObject actionObj = getCorrespondingAction(context);
                Pattern actionPattern  = new Pattern("");
                String actionOutObj = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
                actionPattern.addPattern(actionOutObj);

                documentList = actionObj.getRelatedObjects(context,
                                                          actionPattern.getPattern(),
                                                          "*",
                                                          typeSelects,
                                                          relSelects,
                                                          false,
                                                          true,
                                                          (short)1,
                                                          objectWhere,
                                                          null,
                                                          null,
                                                          null,
                                                          null);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        return documentList;
    }
    /**
     * This function checks if a user can promote from Assign
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskAssignPromote(Context context, String[] args)
         throws Exception
     {
         //get values from args.
         String taskId = args[0];
         setId(taskId);
         int rc = 1;
         // check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) rc = 0;
         else {

             //Synchronize owners
             synchTaskOwnerToActionOwner(context);
    
             // check Access
             
             if (hasProjectLeadAccess(context)) rc = 0;
         }
         
         return rc;

     }

     /**
     * This function checks if a user can promote from Assign
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskActivePromote(Context context, String[] args)
         throws Exception
     {
         //get values from args.
         String taskId = args[0];
         setId(taskId);
         // check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) return 0;

         // check Access
         if (hasProjectLeadAccess(context)) return 0;
         else return 1;

     }

     /**
     * This function checks if a user can demote from Active
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskAssignDemote(Context context, String[] args)
         throws Exception
     {
         //get values from args.
         String taskId = args[0];
         setId(taskId);
         // check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) return 0;

         // check Access
         if (hasProjectLeadAccess(context)) return 0;
         else return 1;

     }

     /**
     * This function checks if a user can demote from Active
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */
     public int triggerCheckVPLMTaskActiveDemote(Context context, String[] args)
         throws Exception
     {
         //get values from args.
         String taskId = args[0];
         setId(taskId);
         // check if trigger should be bypassed
         if (!isVPLMTaskAndInProject(context)) return 0;

         //Synchronize owners
         synchTaskOwnerToActionOwner(context);

         // check Access
         if (hasProjectLeadAccess(context)) return 0;
         else return 1;

     }

     /**
     * This function adds Deliverable Objects and VPLM Action Outputs from predecessor Tasks
     * to Successor Tasks VPLM Action Inputs.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @throws Exception if operation fails
     * @since AEF 10.7.1.0
     */

     public void triggerActionVPLMTaskReviewPromote(Context context, String[] args)

     throws Exception
 {
    try {
       // get values from args.
       String taskId = args[0];
       setId(taskId);
       
	   // Execute finish to start behavior if configured to do so                
	   String finishStart = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.FinishStart");
       if (finishStart.equals("Y"))
       {
		   String finishStartAO = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.AOFinishStart");
		   String actionInputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Input_Affected_Object);
		   String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
		   String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
		   String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);
	                
		   MapList vplmActionList = new MapList();
		   MapList deliverablesList = new MapList();
		   MapList actionOutputList = new MapList();

		   // Get the VPLM Actions
		   String objectWhere = "relationship["+DomainObject.RELATIONSHIP_DEPENDENCY+"].attribute["+DomainObject.ATTRIBUTE_DEPENDENCY_TYPE+"] == \'FS\'";
		   StringList typeSelects = new StringList(2);
		   typeSelects.add("from["+actionRel+"].to.id");
		   typeSelects.add("from["+actionRel+"].to.vault");
		   String actionOutputVault = "";

		   Pattern relPattern  = new Pattern("");
		   relPattern.addPattern(DomainObject.RELATIONSHIP_DEPENDENCY);

		   // get all the Successors VPLM Action Objects
		   vplmActionList = getRelatedObjects(context,
										relPattern.getPattern(),
										"*",
										typeSelects,
										null,
										true,
										false,
										(short)1,
										objectWhere,
										null,
										null,
										null,
										null);
	                                                                                                        
		   // if there is a Successor VPLM Task with a VPLM Action we need to connect things up
		   if (vplmActionList != null && (vplmActionList.size() > 0))
		   {
			  // get the objects Deliverables
			  Pattern relDeliverablePattern  = new Pattern("");
			  relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
			  StringList typeDeliverableSelects = new StringList(2);
			  typeDeliverableSelects.add(SELECT_ID);
			  typeDeliverableSelects.add(SELECT_VAULT);
	    
			  deliverablesList = getRelatedObjects(context,
										   relDeliverablePattern.getPattern(),
										   "*",
										   typeDeliverableSelects,
										   null,
										   false,
										   true,
										   (short)1,
										   null,
										   null,
										   null,
										   null,
										   null);
	    
			  String taskType = getInfo(context, SELECT_TYPE);
	        
			  //check if task is a VPLM task, if it is and configured for we need to get Action Ouput Objects
			  if (taskType.equals(vplmTask) && finishStartAO.equals("Y"))
			  {
				 // get the Action Outputs
				 Pattern relActionOutputPattern  = new Pattern("");
				 relActionOutputPattern.addPattern(actionRel);
	        
				 StringList typeActionOutputSelects = new StringList(2);
				 typeActionOutputSelects.add("from["+actionOutputRel+"].to.id");
				 typeActionOutputSelects.add("from["+actionOutputRel+"].to.vault");
	        
				 actionOutputList = getRelatedObjects(context,
											  relActionOutputPattern.getPattern(),
											  "*",
											  typeActionOutputSelects,
											  null,
											  false,
											  true,
											  (short)1,
											  null,
											  null,
											  null,
											  null,
											  null);
				 // Get vault
				 String propertyActionInstance = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMInstance);
				 actionOutputVault = getAttributeValue(context, propertyActionInstance);
			  }
	       
			  // loop over VPLM Actions to connect Deliverables and Action Outputs
			  for(int i=0; i< vplmActionList.size(); i++)
			  {
				 Map objectmap =(Map) vplmActionList.get(i);
				 String successorVPLMActionId = (String)objectmap.get("from["+actionRel+"].to.id");
				 String successorVPLMActionVault = (String)objectmap.get("from["+actionRel+"].to.vault");

				 if (successorVPLMActionId != null)
				 {
					// get list of ids already connected so we don't fail trying to 
					// connect the same objects more than once
					DomainObject successorVPLMActionObj = DomainObject.newInstance(context, successorVPLMActionId);
					StringList inputObjectIds = successorVPLMActionObj.getInfoList(context, "from["+actionInputRel+"].to.id");
	             
					if (deliverablesList != null && (deliverablesList.size() > 0))
					{
					   for(int x=0; x< deliverablesList.size(); x++)
					   {
						  Map deliverablesmap =(Map) deliverablesList.get(x);
						  String deliverablesId = (String)deliverablesmap.get(SELECT_ID);
						  String deliverablesVault = (String)deliverablesmap.get(SELECT_VAULT);
	                    
						  if ((deliverablesVault != null) && (deliverablesVault.equals(successorVPLMActionVault)))
						  {
							 if (!inputObjectIds.contains(deliverablesId))
							 {
								// connect Deliverable objects to VPLM Action Inputs
								DomainRelationship.connect(context,successorVPLMActionId,actionInputRel, deliverablesId, false);
							 }
						  }
					   }
					}
	        
					// check if task is a VPLM task and if we need to connect
					// VPLM Action Output Objects
					if (taskType.equals(vplmTask) && finishStartAO.equals("Y"))
					{
					   if (actionOutputList != null && (actionOutputList.size() > 0))
					   {
						  //for(int x=0; x< actionOutputList.size(); x++)
						  for(Iterator itr=actionOutputList.iterator(); itr.hasNext(); )
						  {
							 //Map actionOutputmap =(Map) actionOutputList.get(x);
							 Map actionOutputmap =(Map) itr.next();
							 Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.id");
							 StringList tmpList = new StringList();
	                         
							 if (tmpObj !=null && tmpObj instanceof String) {
								 tmpList.add(tmpObj);
							 } else if (tmpObj !=null && tmpObj instanceof StringList) {
								 tmpList = (StringList)tmpObj;
							 }
	                         
							 StringList actionOutputIds = (StringList) tmpList;
							 /*
							  *************************************************************
							  * Missing LOGIC, this code was here to get vault list, but never
							  * used it. AFZ 08/10/2007
							  *  
							 tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.vault");
							 if (tmpObj !=null && tmpObj instanceof String) {
								 tmpList.add(tmpObj);
							 } else if (tmpObj !=null && tmpObj instanceof StringList) {
								 tmpList = (StringList)tmpObj;
							 }
	                         
							 StringList actionOutputVaults = (StringList)tmpList;
							 ******************************************************************
							 */
	                         
							 for(Iterator itrId=actionOutputIds.iterator(); itrId.hasNext(); )
							 {
								 String actionOutputId = (String)itrId.next();
	                 
								 if ((actionOutputVault != null) && (actionOutputVault.equals(successorVPLMActionVault)))
								 {
									if (!inputObjectIds.contains(actionOutputId))
									{
									   DomainRelationship.connect(context,successorVPLMActionId,actionInputRel, actionOutputId, false);
									}
								 }
							 }
						  }
					   }      
					}
				 }
			  }
		   }
	   }
       //Check if trigger should be bypassed
       if (isVPLMTaskAndInProject(context))
       {

         //get corresponding action
         String actionId = getCorrespondingActionId(context);
         DomainObject actionObj = DomainObject.newInstance(context, actionId);

         //make sure Action isn't complete
         //String actionStat = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_status);
         //String actionStatus = actionObj.getAttributeValue(context, actionStat);
         String actionStatus = actionObj.getInfo(context, SELECT_CURRENT);
         //String VPLM_Complete_Equivanent = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.Complete");
         String VPLM_Complete_Equivanent = getActionStatusEquivalent(context, "Complete");
         if (!actionStatus.equals(VPLM_Complete_Equivanent))
         {

             //set task values
             String actionDuration = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_duration);
             String actionActualStart = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_ENOVIA_AFLAction_V_actual_start_date);
             String actualStart = getAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_START_DATE);

             //set action values
             //actionObj.setAttributeValue(context, duration, duration);
             //actionObj.setAttributeValue(context, actualStart, actualStart);
                             //update Action attributes
             Map actionMap = new HashMap();
             String taskDuration = getAttributeValue(context, ATTRIBUTE_TASK_ACTUAL_DURATION);
             //check for zero duration and remove decimal
             if (!taskDuration.equals("0.0") && taskDuration != null)
             {
                 if(taskDuration.indexOf(".")>-1) taskDuration = taskDuration.substring(0,taskDuration.indexOf("."));
                 actionMap.put(actionDuration, taskDuration);
             }
             
             actionMap.put(actionActualStart, actualStart);
             actionObj.setAttributeValues(context, actionMap);


             //promote Action to complete
			 String completePromote = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.VPLMMapping.CompletePromote");
			 if (completePromote.equals("Y")) actionObj.setState(context, VPLM_Complete_Equivanent);

         }
       }
    }
    catch (Exception ex)
    {
        ex.printStackTrace();
        throw ex;
    }
}
     /**
      * This method is executed to get deliverables.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds arguments
      * @returns Object
      * @throws Exception if the operation fails
      * @since PMC X+2
      * @grade 0
      */

     public Object getVPLMTaskDeliverables(Context context, String[] args)
         throws Exception
     {
         try
         {
//           get values from args.
             //String taskId = args[0];
             //setId(taskId);
             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             String objectId = (String) programMap.get("objectId");
			//Modified:11-June-09:yox:R207:PRG:Bug :372619
            // support for export and report format have been provided at lots of places below also
			 HashMap paramList = (HashMap) programMap.get("paramList");
             String strReportFormat = (String)paramList.get("reportFormat");
             String strExportFormat = (String)paramList.get("exportFormat");
            //End:yox:R207:PRG:Bug :372619
			 MapList objectList = (MapList)programMap.get("objectList");
             Map tempMap = null;
             StringList taskListName = null;
             StringList taskListId = null;
             StringList taskListType = null;
             String taskName = "";
             String taskId = "";
             String taskType = "";
             int    loopCounter;
             Vector taskList = new Vector();
             String prefixDeliverableUrl = "<img src='../common/images/utilSpace.gif' width='1' height='25' /><a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
             //Modified:11-June-09:yox:R207:PRG:Bug :372619
			 String prefixSpacerUrl = "<img src='../common/images/utilSpace.gif' width='1' height='25' />";
             //End:yox:R207:PRG:Bug :372619
			 String anchorEnd = "</a>&#160;";
             String suffixDeliverableUrl = "', '930', '650', 'false', 'popup', '')\" class=\"object\">";
             String strImg = "<img border=0 src=../common/images/iconTreeToArrow.gif></img>&#160;";
             //StringBuffer sbfURL = null;
             //sbfURL.append(" ");
             StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
             //taskList.add(sbfURL);
             for (Iterator itr = objectList.iterator(); itr.hasNext(); )
             {
                 tempMap = (Map)itr.next();
                 //taskListId = (StringList)tempMap.get("id");
                 taskId = (String)tempMap.get("id");
                 if (taskId != null && !"".equals(taskId) )
                 {
                 
                 tempURL = new StringBuffer();
                 
                 setId(taskId);
                 // Call to get deliverables for VPLM Tasks
                 //
                 //String[] oids = new String[1];
                 //oids[0] = parentId;
                 //${CLASS:emxTask} emxTask = new ${CLASS:emxTask}(context, oids);
                 //MapList deliverablesList = (MapList)emxTask.getAllDeliverables(context, args);
                 String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
                 String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
                 String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);

                 MapList deliverablesList = new MapList();
                 MapList actionOutputList = new MapList();

                 // Get the VPLM Actions
                 String objectWhere = "relationship[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].attribute[" + DomainObject.ATTRIBUTE_DEPENDENCY_TYPE + "] == \'FS\'";
                 StringList typeSelects = new StringList(2);
                 typeSelects.add("from[" + actionRel + "].to.id");
                 typeSelects.add("from[" + actionRel + "].to.vault");
                 String actionOutputVault = "";

                 Pattern relPattern = new Pattern("");
                 relPattern.addPattern(DomainObject.RELATIONSHIP_DEPENDENCY);

                 // get the objects Deliverables
                 Pattern relDeliverablePattern = new Pattern("");
                 relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
                 StringList typeDeliverableSelects = new StringList(6);
                
                 typeDeliverableSelects.add("from[relationship_TaskDeliverable].to.from[relationship_ActiveVersion].to.attribute[attribute_Title].value");
                 typeDeliverableSelects.add(SELECT_ID);
                 typeDeliverableSelects.add(SELECT_NAME);
                 typeDeliverableSelects.add(SELECT_TYPE);
                 typeDeliverableSelects.add(SELECT_CURRENT);
                 typeDeliverableSelects.add(SELECT_OWNER);

                 deliverablesList = getRelatedObjects(context,
                                              relDeliverablePattern.getPattern(),
                                              "*",
                                              typeDeliverableSelects,
                                              null,
                                              false,
                                              true,
                                              (short)1,
                                              null,
                                              null,
                                              null,
                                              null,
                                              null);

                 taskType = getInfo(context, SELECT_TYPE);

                 //check if task is a VPLM task, if it is we need to get Action Ouput Objects
                 if (taskType.equals(vplmTask))
                 {
                    // get the Action Outputs
                    Pattern relActionOutputPattern = new Pattern("");
                    relActionOutputPattern.addPattern(actionRel);

                    StringList typeActionOutputSelects = new StringList(5);
                    typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
                    typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
                    typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");
                    typeActionOutputSelects.add("from[" + actionOutputRel + "].to.owner");
                    typeActionOutputSelects.add("from[" + actionOutputRel + "].to.current");

                    actionOutputList = getRelatedObjects(context,
                                                 relActionOutputPattern.getPattern(),
                                                 "*",
                                                 typeActionOutputSelects,
                                                 null,
                                                 false,
                                                 true,
                                                 (short)1,
                                                 null,
                                                 null,
                                                 null,
                                                 null,
                                                 null);
                    // Get vault
                    String propertyActionInstance = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_VPLMInstance);
                    actionOutputVault = getAttributeValue(context, propertyActionInstance);
                 }
                 

                 int deliverablesAdded = 0;
                 if (deliverablesList != null && (deliverablesList.size() > 0))
                 {
                    for (int x = 0; x < deliverablesList.size(); x++)
                    {
                        Map deliverablesmap = (Map)deliverablesList.get(x);
                        String deliverablesName = (String)deliverablesmap.get(SELECT_NAME);
                        String deliverablesId = (String)deliverablesmap.get(SELECT_ID);
                        // Get Icon for the type
                        String deliverablesType = (String)deliverablesmap.get(SELECT_TYPE);      
                        String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "type", deliverablesType, true);
                        String typeIcon = "";
                        try{
                            typeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strTypeSymName);
                        }catch(Exception e){
                            typeIcon  = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon.defaultType");
                        }

                        String defaultTypeIcon = "<img src='../common/images/"+typeIcon+"' border='0' />";


                        // deliverable hyperlink formation
						//Modified:11-June-09:yox:R207:PRG:Bug :372619
                        if("CSV".equalsIgnoreCase(strExportFormat)){
                            tempURL.append(XSSUtil.encodeForXML(context,deliverablesName));
                            if(x!= (deliverablesList.size()-1)){
                            tempURL.append(",");
                            }
                        }
                        else{
                            if("HTML".equalsIgnoreCase(strReportFormat))
                            {
                                tempURL.append(prefixSpacerUrl);
                                tempURL.append(defaultTypeIcon);
                                tempURL.append(XSSUtil.encodeForXML(context,deliverablesName));
                            }else{
                                tempURL.append(prefixDeliverableUrl);
                                tempURL.append(deliverablesId);
                                tempURL.append(suffixDeliverableUrl);
                                tempURL.append(defaultTypeIcon);
                                tempURL.append(XSSUtil.encodeForXML(context,deliverablesName));
                                tempURL.append(anchorEnd);
                            }
                        tempURL.append("<br></br>"); //To show each task in different line.
                        }
                        deliverablesAdded ++;
						//End:R207:PRG:Bug :372619
                     }
                  }
                 

                  // check if task is a VPLM task, if it is we need to connect
                  // VPLM Action Output Objects
                  if (taskType.equals(vplmTask))
                  {
                    if (actionOutputList != null && (actionOutputList.size() > 0))
                    {
                        //for(int x=0; x< actionOutputList.size(); x++)
                        for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
                        {
                            //Map actionOutputmap =(Map) actionOutputList.get(x);
                            Map actionOutputmap = (Map)itrAO.next();
							//Get list of objects
							Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.id");
							StringList tmpObjList = new StringList();
	                         
							if (tmpObj !=null && tmpObj instanceof String) {
								 tmpObjList.add(tmpObj);
							} else if (tmpObj !=null && tmpObj instanceof StringList) {
								 tmpObjList = (StringList)tmpObj;
							}
	                         
							StringList actionOutputIds = (StringList) tmpObjList;

							// Get list of object names
							Object tmpName = actionOutputmap.get("from["+actionOutputRel+"].to.name");
							StringList tmpNameList = new StringList();
	                         
							if (tmpName !=null && tmpName instanceof String) {
								 tmpNameList.add(tmpName);
							} else if (tmpName !=null && tmpName instanceof StringList) {
								 tmpNameList = (StringList)tmpName;
							}
							StringList actionOutputNames = (StringList) tmpNameList;

							//Get list of object types
							Object tmpType = actionOutputmap.get("from["+actionOutputRel+"].to.type");
							StringList tmpTypeList = new StringList();
	                         
							if (tmpType !=null && tmpType instanceof String) {
								 tmpTypeList.add(tmpType);
							} else if (tmpType !=null && tmpType instanceof StringList) {
								 tmpTypeList = (StringList)tmpType;
							}
	                         
							StringList actionOutputTypes = (StringList)tmpTypeList;

                            if (actionOutputIds != null && actionOutputNames != null && (actionOutputIds.size() > 0) && (actionOutputNames.size() > 0) )
                            {
                                Iterator itrName = actionOutputNames.iterator();
                                Iterator itrType = actionOutputTypes.iterator();
                                for (Iterator itrId = actionOutputIds.iterator(); itrId.hasNext(); )
                                {
                                    String actionOutputId = (String)itrId.next();
                                    String actionOutputName = (String)itrName.next();
                                    String actionOutputType = (String)itrType.next();
                                    //Get Icon for the type
                                    String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "type", actionOutputType, true);
                                    String typeIcon = "";
                                    try{
                                        typeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strTypeSymName);
                                    }catch(Exception e){
                                        typeIcon  = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon.defaultType");
                                    }

                                    String defaultTypeIcon = "<img src='../common/images/"+typeIcon+"' border='0'/>";
    
                                    // deliverable hyperlink formation
                                    
                                    tempURL.append(prefixDeliverableUrl);
                                    tempURL.append(actionOutputId);
                                    tempURL.append(suffixDeliverableUrl);
                                    tempURL.append(defaultTypeIcon);
                                    tempURL.append(actionOutputName);
                                    tempURL.append(anchorEnd);
                                    tempURL.append("<br></br>"); //To show each task in different line.
                                    deliverablesAdded ++;
    
                                }
                            }
                        }
                    }
                  }
                  if (deliverablesAdded == 0) tempURL.append(" ");
                  taskList.add(tempURL.toString());
                 }
             }
     
             return taskList;
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
             throw ex;
         }
     }
     /**
      * This function get Deliverable types for the Deliverable report.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - String containing the object id
      * @throws Exception if operation fails
      * @since AEF 10.7.1.0
      */
     public Object getVPLMTaskDeliverableTypes(Context context, String[] args)
     throws Exception
     {
     try
     {
//       get values from args.
         //String taskId = args[0];
         //setId(taskId);
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 //Added:11-June-09:yox:R207:PRG:Bug :372619
         HashMap paramList = (HashMap) programMap.get("paramList");
         String languageStr = (String) paramList.get("languageStr");
         String strReportFormat = (String)paramList.get("reportFormat");
         String strExportFormat = (String)paramList.get("exportFormat");
		 //End:R207:PRG:Bug :372619;
         MapList objectList = (MapList)programMap.get("objectList");
         Map tempMap = null;
         
         String taskId = "";
         String taskType = "";
 
         Vector taskList = new Vector();
         String prefixDeliverableUrl = "<b><img src='..common/images/utilSpace.gif' width='1' height='25' />";
         //String anchorEnd = "</a>&nbsp";
         String suffixDeliverableUrl = "</b>";
         //String strImg = "<img border=0 src=../common/images/iconTreeToArrow.gif></img>&nbsp";
         StringBuffer sbfURL = null;
         sbfURL = new StringBuffer();
         sbfURL.append(" ");
         StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
         //taskList.add(sbfURL);
         for (Iterator itr = objectList.iterator(); itr.hasNext(); )
         {
             tempMap = (Map)itr.next();
             //taskListId = (StringList)tempMap.get("id");
             taskId = (String)tempMap.get("id");
             if (taskId != null && !"".equals(taskId) )
             {
             
             tempURL = new StringBuffer();
             
             setId(taskId);
             // Call to get deliverables for VPLM Tasks
             //
             //String[] oids = new String[1];
             //oids[0] = parentId;
             //${CLASS:emxTask} emxTask = new ${CLASS:emxTask}(context, oids);
             //MapList deliverablesList = (MapList)emxTask.getAllDeliverables(context, args);
             String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
             String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
             String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);

             MapList deliverablesList = new MapList();
             MapList actionOutputList = new MapList();

             // Get the VPLM Actions
             //String objectWhere = "relationship[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].attribute[" + DomainObject.ATTRIBUTE_DEPENDENCY_TYPE + "] == \'FS\'";
             StringList typeSelects = new StringList(2);
             typeSelects.add("from[" + actionRel + "].to.id");
             typeSelects.add("from[" + actionRel + "].to.vault");
 

             Pattern relPattern = new Pattern("");
             relPattern.addPattern(DomainObject.RELATIONSHIP_DEPENDENCY);

             // get the objects Deliverables
             Pattern relDeliverablePattern = new Pattern("");
             relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
             StringList typeDeliverableSelects = new StringList(6);
            
             typeDeliverableSelects.add("from[relationship_TaskDeliverable].to.from[relationship_ActiveVersion].to.attribute[attribute_Title].value");
             typeDeliverableSelects.add(SELECT_ID);
             typeDeliverableSelects.add(SELECT_NAME);
             typeDeliverableSelects.add(SELECT_TYPE);
             typeDeliverableSelects.add(SELECT_CURRENT);
             typeDeliverableSelects.add(SELECT_OWNER);

             deliverablesList = getRelatedObjects(context,
                                          relDeliverablePattern.getPattern(),
                                          "*",
                                          typeDeliverableSelects,
                                          null,
                                          false,
                                          true,
                                          (short)1,
                                          null,
                                          null,
                                          null,
                                          null,
                                          null);

             taskType = getInfo(context, SELECT_TYPE);

             //check if task is a VPLM task, if it is we need to get Action Ouput Objects
             if (taskType.equals(vplmTask))
             {
                // get the Action Outputs
                Pattern relActionOutputPattern = new Pattern("");
                relActionOutputPattern.addPattern(actionRel);

                StringList typeActionOutputSelects = new StringList(5);
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.owner");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.current");

                actionOutputList = getRelatedObjects(context,
                                             relActionOutputPattern.getPattern(),
                                             "*",
                                             typeActionOutputSelects,
                                             null,
                                             false,
                                             true,
                                             (short)1,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
                // Get vault

             }
             

             int deliverablesAdded = 0;
             if (deliverablesList != null && (deliverablesList.size() > 0))
             {
                for (int x = 0; x < deliverablesList.size(); x++)
                {
                    Map deliverablesmap = (Map)deliverablesList.get(x);
                    String deliverablesType = (String)deliverablesmap.get(SELECT_TYPE);
                 	//Modified:11-June-09:yox:R207:PRG:Bug :372619
				    deliverablesType = i18nNow.getTypeI18NString(deliverablesType,languageStr);
                    String deliverablesId = (String)deliverablesmap.get(SELECT_ID);
                    // deliverable hyperlink formation
                    if("CSV".equals(strExportFormat)){
                           tempURL.append(deliverablesType);
                        if(x != (deliverablesList.size()-1)){
                            tempURL.append(",");
                        }
                        
                    }else{
                        tempURL.append(prefixDeliverableUrl);
                    	tempURL.append(deliverablesType);
                        tempURL.append(suffixDeliverableUrl);
                        tempURL.append("<br></br>"); //To show each type in different line.
                    }
                    deliverablesAdded ++;
					//End:R207:PRG:Bug :372619
                 }
              }
             

              // check if task is a VPLM task, if it is we need to connect
              // VPLM Action Output Objects
              if (taskType.equals(vplmTask))
              {
                if (actionOutputList != null && (actionOutputList.size() > 0))
                {
                    //for(int x=0; x< actionOutputList.size(); x++)
                    for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
                    {
                        Map actionOutputmap = (Map)itrAO.next();
                        //Get list of objects
						Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.id");
                        StringList tmpObjList = new StringList();
                         
                        if (tmpObj !=null && tmpObj instanceof String) {
                             tmpObjList.add(tmpObj);
                        } else if (tmpObj !=null && tmpObj instanceof StringList) {
                             tmpObjList = (StringList)tmpObj;
                        }
                         
                        StringList actionOutputIds = (StringList) tmpObjList;

						//Get list of object types
						Object tmpType = actionOutputmap.get("from["+actionOutputRel+"].to.type");
                        StringList tmpTypeList = new StringList();
                         
                        if (tmpType !=null && tmpType instanceof String) {
                             tmpTypeList.add(tmpType);
                        } else if (tmpType !=null && tmpType instanceof StringList) {
                             tmpTypeList = (StringList)tmpType;
                        }
                         
                        StringList actionOutputTypes = (StringList)tmpTypeList;

                        if (actionOutputIds != null && actionOutputTypes != null && (actionOutputIds.size() > 0) && (actionOutputTypes.size() > 0) )
                        {
                            Iterator itrType = actionOutputTypes.iterator();
                            for (Iterator itrId = actionOutputIds.iterator(); itrId.hasNext(); )
                            {
                                String actionOutputId = (String)itrId.next();
                                String actionOutputType = (String)itrType.next();

                                // deliverable hyperlink formation
                                
                                tempURL.append(prefixDeliverableUrl);
                                tempURL.append(XSSUtil.encodeForURL(context,actionOutputType));
                                tempURL.append(suffixDeliverableUrl);
                                tempURL.append("<br>"); //To show each type in different line.
                               
                                deliverablesAdded ++;
                            }
                        }
                    }
                }
              }
              if (deliverablesAdded == 0) tempURL.append(" ");
              taskList.add(tempURL.toString());
              
             }
         }
 
         return taskList;
     }
     catch (Exception ex)
     {
         ex.printStackTrace();
         throw ex;
     }
 }
     /**
      * This function get Deliverable states for the Deliverable report.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - String containing the object id
      * @throws Exception if operation fails
      * @since AEF 10.7.1.0
      */
     public Object getVPLMTaskDeliverableStates(Context context, String[] args)
     throws Exception
     {
     try
     {
//       get values from args.

         HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 //Modified:11-June-09:yox:R207:PRG:Bug :372619
         HashMap paramList = (HashMap) programMap.get("paramList");
         String languageStr = (String) paramList.get("languageStr");
         String strReportFormat = (String)paramList.get("reportFormat");
         String strExportFormat = (String)paramList.get("exportFormat");
		 //End:R207:PRG:Bug :372619
         MapList objectList = (MapList)programMap.get("objectList");
         Map tempMap = null;
         
         String taskId = "";
         String taskType = "";
 
         Vector taskList = new Vector();
         String prefixDeliverableUrl = "<b><img src='../common/images/utilSpace.gif' width='1' height='25' />";
         String suffixDeliverableUrl = "</b>";

         StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
         for (Iterator itr = objectList.iterator(); itr.hasNext(); )
         {
             tempMap = (Map)itr.next();
             taskId = (String)tempMap.get("id");
             if (taskId != null && !"".equals(taskId) )
             {
             
             tempURL = new StringBuffer();
             
             setId(taskId);

             String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
             String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
             String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);

             MapList deliverablesList = new MapList();
             MapList actionOutputList = new MapList();

             // Get the VPLM Actions
             //String objectWhere = "relationship[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].attribute[" + DomainObject.ATTRIBUTE_DEPENDENCY_TYPE + "] == \'FS\'";

             // get the objects Deliverables
             Pattern relDeliverablePattern = new Pattern("");
             relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
             StringList typeDeliverableSelects = new StringList(6);
            
             typeDeliverableSelects.add("from[relationship_TaskDeliverable].to.from[relationship_ActiveVersion].to.attribute[attribute_Title].value");
             typeDeliverableSelects.add(SELECT_ID);
             typeDeliverableSelects.add(SELECT_NAME);
             typeDeliverableSelects.add(SELECT_TYPE);
             typeDeliverableSelects.add(SELECT_CURRENT);
             typeDeliverableSelects.add(SELECT_OWNER);
			 //Added:11-June-09:yox:R207:PRG:Bug :372619
             typeDeliverableSelects.add(SELECT_POLICY);
			  //End:PRG:Bug :372619
             deliverablesList = getRelatedObjects(context,
                                          relDeliverablePattern.getPattern(),
                                          "*",
                                          typeDeliverableSelects,
                                          null,
                                          false,
                                          true,
                                          (short)1,
                                          null,
                                          null,
                                          null,
                                          null,
                                          null);

             taskType = getInfo(context, SELECT_TYPE);

             //check if task is a VPLM task, if it is we need to get Action Ouput Objects
             if (taskType.equals(vplmTask))
             {
                // get the Action Outputs
                Pattern relActionOutputPattern = new Pattern("");
                relActionOutputPattern.addPattern(actionRel);

                StringList typeActionOutputSelects = new StringList(5);
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.owner");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.current");

                actionOutputList = getRelatedObjects(context,
                                             relActionOutputPattern.getPattern(),
                                             "*",
                                             typeActionOutputSelects,
                                             null,
                                             false,
                                             true,
                                             (short)1,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
             }
             
             int deliverablesAdded = 0;
             if (deliverablesList != null && (deliverablesList.size() > 0))
             {
                for (int x = 0; x < deliverablesList.size(); x++)
                {
                    Map deliverablesmap = (Map)deliverablesList.get(x);
                    String deliverablesState = (String)deliverablesmap.get(SELECT_CURRENT);
					//Modified:11-June-09:yox:R207:PRG:Bug :372619
                    String deliverablesPolicy = (String)deliverablesmap.get(SELECT_POLICY);
                    deliverablesState = i18nNow.getStateI18NString(deliverablesPolicy,deliverablesState, languageStr);
                    // deliverable state formation
                    if("CSV".equals(strExportFormat)){
                        tempURL.append(deliverablesState);
                        if(x != (deliverablesList.size()-1)){
                            tempURL.append(",");
                        }
                    }else{
                        tempURL.append(prefixDeliverableUrl);
                        tempURL.append(deliverablesState);
                        tempURL.append(suffixDeliverableUrl);
                        tempURL.append("<br></br>"); //To show each state in different line.
                    }
                    deliverablesAdded ++;
                    //End:R207:PRG:Bug :372619
                 }
              }
             
              // check if task is a VPLM task, if it is we need to get states of its outputs
              if (taskType.equals(vplmTask))
              {
                if (actionOutputList != null && (actionOutputList.size() > 0))
                {
                    for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
                    {
                        Map actionOutputmap = (Map)itrAO.next();
						Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.current");
                        StringList tmpList = new StringList();
                         
                        if (tmpObj !=null && tmpObj instanceof String) {
                             tmpList.add(tmpObj);
                        } else if (tmpObj !=null && tmpObj instanceof StringList) {
                             tmpList = (StringList)tmpObj;
                        }
                         
                        StringList actionOutputStates = (StringList) tmpList;
                        if (actionOutputStates != null && (actionOutputStates.size() > 0) )
                        {
                            for (Iterator itrState = actionOutputStates.iterator(); itrState.hasNext(); )
                            {
                                String actionOutputState = (String)itrState.next();

                                // deliverable hyperlink formation
                                
                                tempURL.append(prefixDeliverableUrl);
                                tempURL.append(XSSUtil.encodeForURL(context,actionOutputState));
                                tempURL.append(suffixDeliverableUrl);
                                tempURL.append("<br></br>"); //To show each type in different line.
                               
                                deliverablesAdded ++;

                            }
                        }
                    }
                }
              }
              if (deliverablesAdded == 0) tempURL.append(" ");
              taskList.add(tempURL.toString());
              
             }
         }
 
         return taskList;
     }
     catch (Exception ex)
     {
         ex.printStackTrace();
         throw ex;
     }
 }
     /**
      * This function get Deliverable owners for the Deliverable report.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - String containing the object id
      * @throws Exception if operation fails
      * @since AEF 10.7.1.0
      */
     public Object getVPLMTaskDeliverableOwners(Context context, String[] args)
     throws Exception
     {
     try
     {
//       get values from args.

         HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 //Added:11-June-09:yox:R207:PRG:Bug :372619
         HashMap paramList = (HashMap) programMap.get("paramList");
         String strReportFormat = (String)paramList.get("reportFormat");
         String strExportFormat = (String)paramList.get("exportFormat");
		 //End:R207:PRG:Bug :372619
         MapList objectList = (MapList)programMap.get("objectList");
         Map tempMap = null;
         
         String taskId = "";
         String taskType = "";
 
         Vector taskList = new Vector();
         String prefixDeliverableUrl = "<b><img src='../common/images/utilSpace.gif' width='1' height='25' />";
         String suffixDeliverableUrl = "</b>";

         StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
         for (Iterator itr = objectList.iterator(); itr.hasNext(); )
         {
             tempMap = (Map)itr.next();
             taskId = (String)tempMap.get("id");
             if (taskId != null && !"".equals(taskId) )
             {
             
             tempURL = new StringBuffer();
             
             setId(taskId);

             String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
             String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
             String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);

             MapList deliverablesList = new MapList();
             MapList actionOutputList = new MapList();

             // Get the VPLM Actions
             //String objectWhere = "relationship[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].attribute[" + DomainObject.ATTRIBUTE_DEPENDENCY_TYPE + "] == \'FS\'";

             // get the objects Deliverables
             Pattern relDeliverablePattern = new Pattern("");
             relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
             StringList typeDeliverableSelects = new StringList(6);
            
             typeDeliverableSelects.add("from[relationship_TaskDeliverable].to.from[relationship_ActiveVersion].to.attribute[attribute_Title].value");
             typeDeliverableSelects.add(SELECT_ID);
             typeDeliverableSelects.add(SELECT_NAME);
             typeDeliverableSelects.add(SELECT_TYPE);
             typeDeliverableSelects.add(SELECT_REVISION);
             typeDeliverableSelects.add(SELECT_OWNER);

             deliverablesList = getRelatedObjects(context,
                                          relDeliverablePattern.getPattern(),
                                          "*",
                                          typeDeliverableSelects,
                                          null,
                                          false,
                                          true,
                                          (short)1,
                                          null,
                                          null,
                                          null,
                                          null,
                                          null);

             taskType = getInfo(context, SELECT_TYPE);

             //check if task is a VPLM task, if it is we need to get Action Ouput Objects
             if (taskType.equals(vplmTask))
             {
                // get the Action Outputs
                Pattern relActionOutputPattern = new Pattern("");
                relActionOutputPattern.addPattern(actionRel);

                StringList typeActionOutputSelects = new StringList(5);
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.owner");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.revision");

                actionOutputList = getRelatedObjects(context,
                                             relActionOutputPattern.getPattern(),
                                             "*",
                                             typeActionOutputSelects,
                                             null,
                                             false,
                                             true,
                                             (short)1,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
             }
             
             int deliverablesAdded = 0;
             if (deliverablesList != null && (deliverablesList.size() > 0))
             {
                for (int x = 0; x < deliverablesList.size(); x++)
                {
                    Map deliverablesmap = (Map)deliverablesList.get(x);
                    String deliverablesOwner = (String)deliverablesmap.get(SELECT_OWNER);
                    // deliverable state formation
					//Modified:11-June-09:yox:R207:PRG:Bug :372619
                    if("CSV".equals(strExportFormat)){
                        if(x==0){
                            tempURL.append("\"");
                        }
                        tempURL.append(XSSUtil.encodeForURL(context,deliverablesOwner));
                        if(x != (deliverablesList.size()-1)){
                            tempURL.append(",");
                        }else{
                            tempURL.append("\"");
                        }
                    }else {
                        tempURL.append(prefixDeliverableUrl);
                        tempURL.append(XSSUtil.encodeForURL(context,deliverablesOwner));
                        tempURL.append(suffixDeliverableUrl);
                        tempURL.append("<br></br>"); //To show each owner in different line.
                    }
                    deliverablesAdded ++;
                    //End:R207:PRG:Bug :372619
                 }
              }
             
              // check if task is a VPLM task, if it is we need to get states of its outputs
              if (taskType.equals(vplmTask))
              {
                if (actionOutputList != null && (actionOutputList.size() > 0))
                {
                    for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
                    {
                        Map actionOutputmap = (Map)itrAO.next();
						Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.owner");
                        StringList tmpList = new StringList();
                         
                        if (tmpObj !=null && tmpObj instanceof String) {
                             tmpList.add(tmpObj);
                        } else if (tmpObj !=null && tmpObj instanceof StringList) {
                             tmpList = (StringList)tmpObj;
                        }
                         
                        StringList actionOutputOwners = (StringList) tmpList;
                        if (actionOutputOwners != null && (actionOutputOwners.size() > 0) )
                        {
                            for (Iterator itrOwner = actionOutputOwners.iterator(); itrOwner.hasNext(); )
                            {
                                String actionOutputOwner = (String)itrOwner.next();

                                // deliverable hyperlink formation
                                
                                tempURL.append(prefixDeliverableUrl);
                                tempURL.append(XSSUtil.encodeForURL(context,actionOutputOwner));
                                tempURL.append(suffixDeliverableUrl);
                                tempURL.append("<br></br>"); //To show each owner in different line.
                               
                                deliverablesAdded ++;

                            }
                        }
                    }
                }
              }
              if (deliverablesAdded == 0) tempURL.append(" ");
              taskList.add(tempURL.toString());
              
             }
         }
 
         return taskList;
     }
     catch (Exception ex)
     {
         ex.printStackTrace();
         throw ex;
     }
 }
     /**
      * This function get Deliverable revisions for the Deliverable report.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - String containing the object id
      * @throws Exception if operation fails
      * @since AEF 10.7.1.0
      */
     public Object getVPLMTaskDeliverableRevisions(Context context, String[] args)
     throws Exception
     {
     try
     {
//       get values from args.

         HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 //Added:11-June-09:yox:R207:PRG:Bug :372619
         HashMap paramList = (HashMap) programMap.get("paramList");
         String strReportFormat = (String)paramList.get("reportFormat");
         String strExportFormat = (String)paramList.get("exportFormat");
		 //End:R207:PRG:Bug :372619
         MapList objectList = (MapList)programMap.get("objectList");
         Map tempMap = null;
         
         String taskId = "";
         String taskType = "";
 
         Vector taskList = new Vector();
         String prefixDeliverableUrl = "<b><img src='../common/images/utilSpace.gif' width='1' height='25' />";
         String suffixDeliverableUrl = "</b>";

         StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
         for (Iterator itr = objectList.iterator(); itr.hasNext(); )
         {
             tempMap = (Map)itr.next();
             taskId = (String)tempMap.get("id");
             if (taskId != null && !"".equals(taskId) )
             {
             
             tempURL = new StringBuffer();
             
             setId(taskId);

             String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
             String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
             String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);

             MapList deliverablesList = new MapList();
             MapList actionOutputList = new MapList();

             // Get the VPLM Actions
             //String objectWhere = "relationship[" + DomainObject.RELATIONSHIP_DEPENDENCY + "].attribute[" + DomainObject.ATTRIBUTE_DEPENDENCY_TYPE + "] == \'FS\'";

             // get the objects Deliverables
             Pattern relDeliverablePattern = new Pattern("");
             relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
             StringList typeDeliverableSelects = new StringList(6);
            
             typeDeliverableSelects.add("from[relationship_TaskDeliverable].to.from[relationship_ActiveVersion].to.attribute[attribute_Title].value");
             typeDeliverableSelects.add(SELECT_ID);
             typeDeliverableSelects.add(SELECT_NAME);
             typeDeliverableSelects.add(SELECT_TYPE);
             typeDeliverableSelects.add(SELECT_REVISION);
             typeDeliverableSelects.add(SELECT_OWNER);

             deliverablesList = getRelatedObjects(context,
                                          relDeliverablePattern.getPattern(),
                                          "*",
                                          typeDeliverableSelects,
                                          null,
                                          false,
                                          true,
                                          (short)1,
                                          null,
                                          null,
                                          null,
                                          null,
                                          null);

             taskType = getInfo(context, SELECT_TYPE);

             //check if task is a VPLM task, if it is we need to get Action Ouput Objects
             if (taskType.equals(vplmTask))
             {
                // get the Action Outputs
                Pattern relActionOutputPattern = new Pattern("");
                relActionOutputPattern.addPattern(actionRel);

                StringList typeActionOutputSelects = new StringList(5);
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.owner");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.revision");

                actionOutputList = getRelatedObjects(context,
                                             relActionOutputPattern.getPattern(),
                                             "*",
                                             typeActionOutputSelects,
                                             null,
                                             false,
                                             true,
                                             (short)1,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);
             }
             
             int deliverablesAdded = 0;
             if (deliverablesList != null && (deliverablesList.size() > 0))
             {
                for (int x = 0; x < deliverablesList.size(); x++)
                {
                    Map deliverablesmap = (Map)deliverablesList.get(x);
                    String deliverablesRevision = (String)deliverablesmap.get(SELECT_REVISION);
                    // deliverable revision formation
                    //Modified:11-June-09:yox:R207:PRG:Bug :372619
                    if("CSV".equals(strExportFormat)){
                    	if(deliverablesAdded > 0)
                    		tempURL.append(",");
                        tempURL.append(XSSUtil.encodeForURL(context,deliverablesRevision));
                    }else{
                        tempURL.append(prefixDeliverableUrl);
                        tempURL.append(XSSUtil.encodeForURL(context,deliverablesRevision));
                        tempURL.append(suffixDeliverableUrl);
                        tempURL.append("<br></br>"); //To show each revision in different line.
                    }
                    deliverablesAdded ++;
                    //EndR207:PRG:Bug :372619
                 }
              }
             
              // check if task is a VPLM task, if it is we need to get states of its outputs
              if (taskType.equals(vplmTask))
              {
                if (actionOutputList != null && (actionOutputList.size() > 0))
                {
                    for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
                    {
                        Map actionOutputmap = (Map)itrAO.next();
						Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.revision");
                        StringList tmpList = new StringList();
                         
                        if (tmpObj !=null && tmpObj instanceof String) {
                             tmpList.add(tmpObj);
                        } else if (tmpObj !=null && tmpObj instanceof StringList) {
                             tmpList = (StringList)tmpObj;
                        }
                         
                        StringList actionOutputRevisions = (StringList) tmpList;
                        if (actionOutputRevisions != null && (actionOutputRevisions.size() > 0) )
                        {
                            for (Iterator itrRevision = actionOutputRevisions.iterator(); itrRevision.hasNext(); )
                            {
                                String actionOutputRevision = (String)itrRevision.next();

                                // deliverable hyperlink formation
                                
                                tempURL.append(prefixDeliverableUrl);
                                tempURL.append(XSSUtil.encodeForURL(context,actionOutputRevision));
                                tempURL.append(suffixDeliverableUrl);
                                tempURL.append("<br>"); //To show each revision in different line.
                               
                                deliverablesAdded ++;

                            }
                        }
                    }
                }
              }
              if (deliverablesAdded == 0) tempURL.append(" ");
              taskList.add(tempURL.toString());
              
             }
         }
 
         return taskList;
     }
     catch (Exception ex)
     {
         ex.printStackTrace();
         throw ex;
     }
 }
     /**
      * This method is executed to get file names associated to document deliverables.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds arguments
      * @returns Object
      * @throws Exception if the operation fails
      * @since PMC X+2
      * @grade 0
      */

      public Object getVPLMTaskDeliverableFiles(Context context, String[] args)
     throws Exception
     {
    	 try
    	 {
    		 // get values from args.
    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
    		 //Added:11-June-09:yox:R207:PRG:Bug :372619
    		 HashMap paramList = (HashMap) programMap.get("paramList");
    		 String strReportFormat = (String)paramList.get("reportFormat");
    		 String strExportFormat = (String)paramList.get("exportFormat");
    		 //End:R207:PRG:Bug :372619
    		 String objectId = (String) programMap.get("objectId");
    		 MapList objectList = (MapList)programMap.get("objectList");
    		 Map tempMap = null;
    		 StringList taskListName = null;
    		 StringList taskListId = null;
    		 StringList taskListType = null;
    		 String taskName = "";
    		 String taskId = "";
    		 String taskType = "";
    		 int    loopCounter;
    		 Vector taskList = new Vector();
    		 //Added:11-June-09:yox:R207:PRG:Bug :372619
    		 String prefixSpacerUrl = "<img src='../common/images/utilSpace.gif' width='1' height='25' />";
    		 //End:R207:PRG:Bug :372619
    		 String prefixDeliverableUrl = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
    		 String anchorEnd = "</a>";
    		 String suffixDeliverableUrl = "', '930', '650', 'false', 'popup', '')\" class=\"object\">";
    		 String strImg = "<img border='0' src='../common/images/iconTreeToArrow.gif'></img>&#160;";
    		 StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
    		 String deliverableRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_TaskDeliverable);
    		 String activeVersion = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_ActiveVersion);
    		 String attributeTitle = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_Title);
    		 String select_Files = "from[" + activeVersion + "].to.attribute[" + attributeTitle + "].value";
    		 String select_File_Ids = "from[" + activeVersion + "].to.id";
    		 for (Iterator itr = objectList.iterator(); itr.hasNext(); )
    		 {
    			 tempMap = (Map)itr.next();
    			 //taskListId = (StringList)tempMap.get("id");
    			 taskId = (String)tempMap.get("id");
    			 if (taskId != null && !"".equals(taskId) )
    			 {

    				 tempURL = new StringBuffer();

    				 setId(taskId);
    				 // Call to get deliverables for VPLM Tasks
    				 String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
    				 String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
    				 String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);

    				 MapList deliverablesList = new MapList();
    				 MapList actionOutputList = new MapList();

    				 // get the objects Deliverables
    				 Pattern relDeliverablePattern = new Pattern("");
    				 relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);

    				 StringList typeDeliverableSelects = new StringList(2);           
    				 typeDeliverableSelects.add(select_Files);
    				 typeDeliverableSelects.add(select_File_Ids);
    				 MULTI_VALUE_LIST.add(select_Files);
    				 MULTI_VALUE_LIST.add(select_File_Ids);
    				 //typeDeliverableSelects.add(SELECT_ID);
    				 //typeDeliverableSelects.add(SELECT_NAME);
    				 //typeDeliverableSelects.add(SELECT_TYPE);

    				 deliverablesList = getRelatedObjects(context,
    						 relDeliverablePattern.getPattern(),
    						 "*",
    						 typeDeliverableSelects,
    						 null,
    						 false,
    						 true,
    						 (short)1,
    						 null,
    						 null,
    						 null,
    						 null,
    						 null);

    				 taskType = getInfo(context, SELECT_TYPE);

    				 //check if task is a VPLM task, if it is we need to get Action Ouput Objects
    				 if (taskType.equals(vplmTask))
    				 {
    					 // get the Action Outputs
    					 Pattern relActionOutputPattern = new Pattern("");
    					 relActionOutputPattern.addPattern(actionRel);

    					 StringList typeActionOutputSelects = new StringList(3);
    					 typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
    					 typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
    					 typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");


    					 actionOutputList = getRelatedObjects(context,
    							 relActionOutputPattern.getPattern(),
    							 "*",
    							 typeActionOutputSelects,
    							 null,
    							 false,
    							 true,
    							 (short)1,
    							 null,
    							 null,
    							 null,
    							 null,
    							 null);

    				 }

    				 int deliverablesAdded = 0;
    				 if (deliverablesList != null && (deliverablesList.size() > 0))
    				 {
    					 for (int x = 0; x < deliverablesList.size(); x++)
    					 {
    						 Map deliverablesmap = (Map)deliverablesList.get(x);
    						 StringList deliverablesName = new StringList (0);
    						 StringList deliverablesId = new StringList (0);
    						 Object fileNameObj = deliverablesmap.get(select_Files);
    						 Object fileIdObj = deliverablesmap.get(select_File_Ids);

    						 if(fileIdObj != null || fileNameObj != null){                    
    							 try
    							 {
    								 deliverablesName.addElement((String)fileNameObj);
    								 deliverablesId.addElement((String)fileIdObj);                    
    							 }
    							 catch(Exception ex)
    							 {
    								 deliverablesName = (StringList)fileNameObj;
    								 deliverablesId = (StringList)fileIdObj;

    							 }
    						 }
    						 // deliverable hyperlink formation
    						 if (deliverablesId == null || deliverablesName == null || deliverablesId.isEmpty() || deliverablesName.isEmpty())
    						 {
    							 if (!"true".equals(strExportFormat)) {
    								 tempURL.append("<b>");
    								 tempURL.append(" ");
    								 tempURL.append("</b>");
    								 tempURL.append("<br></br>"); //To show each entry in different line.                    
    							 } else {
    								 if (deliverablesAdded > 0)
    									 tempURL.append(",");

    								 tempURL.append("[]");
    							 }
    						 }
    						 else
    						 {   //modified:14-March-09:yox:R207:PRG:Bug :SR00016218
    							 if (deliverablesId instanceof StringList) {
    								 if(deliverablesId.size()>0){
    									 if(!"CSV".equals(strExportFormat)){
    										 tempURL.append(prefixSpacerUrl);
    									 }
    									 else if(deliverablesAdded > 0)
    										 tempURL.append(",");

    									 tempURL.append("[");
    									 for (int i=0;i < deliverablesId.size();i++) {
    										 String strDeliverableId = (String)deliverablesId.get(i);
    										 String strDeliverableName = (String)deliverablesName.get(i);

    										 if("CSV".equals(strExportFormat)){
    											 tempURL.append(XSSUtil.encodeForXML(context,strDeliverableName));
    											 if(i != (deliverablesId.size()-1)){
    												 tempURL.append(",");  
    											 }
    										 }else{
    											 if("HTML".equals(strReportFormat)){
    												 tempURL.append(XSSUtil.encodeForXML(context,strDeliverableName));
    											 }else{
    												 tempURL.append(prefixDeliverableUrl);
    												 tempURL.append(XSSUtil.encodeForURL(context,strDeliverableId));
    												 tempURL.append("&amp;parentOID="+strDeliverableId);
    												 tempURL.append("&amp;AppendParameters=true");
    												 tempURL.append(suffixDeliverableUrl);
    												 tempURL.append(XSSUtil.encodeForXML(context,strDeliverableName));
    												 tempURL.append(anchorEnd);
    											 }
    											 if(i != (deliverablesId.size()-1)){
    												 tempURL.append(",&#160;");   
    											 }
    										 }
    									 }
    									 tempURL.append("]");                               
    								 }
    							 }
    							 //                       tempURL.append(prefixDeliverableUrl);
    							 //                       tempURL.append(deliverablesId);
    							 //                       tempURL.append(suffixDeliverableUrl);
    							 //                       tempURL.append(deliverablesName);
    							 //                       tempURL.append(anchorEnd);
    							 if(!"CSV".equals(strExportFormat)){
    								 tempURL.append("<br></br>"); //To show each entryin different line.
    							 }
    							 //End:R207:PRG :Bug :SR00016218
    						 }
    						 deliverablesAdded ++;
    					 }
    				 }

    				 // check if task is a VPLM task, if it is we need to connect
    				 // VPLM Action Output Objects
    				 if (taskType.equals(vplmTask))
    				 {
    					 if (actionOutputList != null && (actionOutputList.size() > 0))
    					 {
    						 //for(int x=0; x< actionOutputList.size(); x++)
    						 for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
    						 {
    							 Map actionOutputmap = (Map)itrAO.next();
    							 // Get list of objects
    							 Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.id");
    							 StringList tmpObjList = new StringList();

    							 if (tmpObj !=null && tmpObj instanceof String) {
    								 tmpObjList.add(tmpObj);
    							 } else if (tmpObj !=null && tmpObj instanceof StringList) {
    								 tmpObjList = (StringList)tmpObj;
    							 }

    							 StringList actionOutputIds = (StringList) tmpObjList;
    							 // Get list of object names
    							 Object tmpName = actionOutputmap.get("from["+actionOutputRel+"].to.name");
    							 StringList tmpNameList = new StringList();

    							 if (tmpName !=null && tmpName instanceof String) {
    								 tmpNameList.add(tmpName);
    							 } else if (tmpName !=null && tmpName instanceof StringList) {
    								 tmpNameList = (StringList)tmpName;
    							 }
    							 StringList actionOutputNames = (StringList) tmpNameList;

    							 if (actionOutputIds != null && actionOutputNames != null && (actionOutputIds.size() > 0) && (actionOutputNames.size() > 0) )
    							 {
    								 Iterator itrName = actionOutputNames.iterator();
    								 for (Iterator itrId = actionOutputIds.iterator(); itrId.hasNext(); )
    								 {
    									 String actionOutputId = (String)itrId.next();
    									 String actionOutputName = (String)itrName.next();

    									 // deliverable hyperlink formation

    									 tempURL.append("<b>");
    									 tempURL.append(" ");
    									 tempURL.append("</b>");
    									 tempURL.append("<br></br>"); //To show each task in different line.
    									 deliverablesAdded ++;

    								 }
    							 }
    						 }
    					 }
    				 }
    				 if (deliverablesAdded == 0) tempURL.append(" ");
    				 taskList.add(tempURL.toString());
    			 }
    		 }

    		 return taskList;
    	 }
    	 catch (Exception ex)
    	 {
    		 ex.printStackTrace();
    		 throw ex;
    	 }
     } 
     /**
      * This method is executed to get file version associated to document deliverables.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds arguments
      * @returns Object
      * @throws Exception if the operation fails
      * @since PMC X+2
      * @grade 0
      */

     public Object getVPLMTaskDeliverableVersions(Context context, String[] args)
     throws Exception
 {
     try
     {
//       get values from args.

         HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 //Added:11-June-09:yox:R207:PRG:Bug :372619
         HashMap paramList = (HashMap) programMap.get("paramList");
         String strReportFormat = (String)paramList.get("reportFormat");
         String strExportFormat = (String)paramList.get("exportFormat");
		 //End:PRG:Bug :372619
         String objectId = (String) programMap.get("objectId");
         MapList objectList = (MapList)programMap.get("objectList");
         Map tempMap = null;
         StringList taskListName = null;
         StringList taskListId = null;
         StringList taskListType = null;
         String taskName = "";
         String taskId = "";
         String taskType = "";
         int    loopCounter;
         Vector taskList = new Vector();
         String prefixDeliverableUrl = "<b><img src='../common/images/utilSpace.gif' width='1' height='25'/>";
         String suffixDeliverableUrl = "</b>";
         StringBuffer tempURL = null;  //Used to form url of all objects if multiple tasks are connected.
         String deliverableRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_TaskDeliverable);
         String activeVersion = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_ActiveVersion);
         //String attributeTitle = PropertyUtil.getSchemaProperty(context, SYMBOLIC_attribute_Title);
         //String select_Files = "from[" + activeVersion + "].to.attribute[" + attributeTitle + "].value";
         String select_File_Revs = "from[" + activeVersion + "].to.revision";
         for (Iterator itr = objectList.iterator(); itr.hasNext(); )
         {
             tempMap = (Map)itr.next();
             //taskListId = (StringList)tempMap.get("id");
             taskId = (String)tempMap.get("id");
             if (taskId != null && !"".equals(taskId) )
             {
             
             tempURL = new StringBuffer();
             
             setId(taskId);
             // Call to get deliverables for VPLM Tasks
             String actionOutputRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_Action_Output_Affected_Object);
             String actionRel = PropertyUtil.getSchemaProperty(context, SYMBOLIC_relationship_VPLMAction);
             String vplmTask = PropertyUtil.getSchemaProperty(context, SYMBOLIC_type_VPLMTask);
             
             MapList deliverablesList = new MapList();
             MapList actionOutputList = new MapList();

             // get the objects Deliverables
             Pattern relDeliverablePattern = new Pattern("");
             relDeliverablePattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
             
             StringList typeDeliverableSelects = new StringList(1);           
             typeDeliverableSelects.add(select_File_Revs);
             MULTI_VALUE_LIST.add(select_File_Revs);
             
             deliverablesList = getRelatedObjects(context,
                                          relDeliverablePattern.getPattern(),
                                          "*",
                                          typeDeliverableSelects,
                                          null,
                                          false,
                                          true,
                                          (short)1,
                                          null,
                                          null,
                                          null,
                                          null,
                                          null);

             taskType = getInfo(context, SELECT_TYPE);

             //check if task is a VPLM task, if it is we need to get Action Ouput Objects
             if (taskType.equals(vplmTask))
             {
                // get the Action Outputs
                Pattern relActionOutputPattern = new Pattern("");
                relActionOutputPattern.addPattern(actionRel);

                StringList typeActionOutputSelects = new StringList(3);
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.id");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.name");
                typeActionOutputSelects.add("from[" + actionOutputRel + "].to.type");


                actionOutputList = getRelatedObjects(context,
                                             relActionOutputPattern.getPattern(),
                                             "*",
                                             typeActionOutputSelects,
                                             null,
                                             false,
                                             true,
                                             (short)1,
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);

             }
             
             int deliverablesAdded = 0;
             if (deliverablesList != null && (deliverablesList.size() > 0))
             {
                for (int x = 0; x < deliverablesList.size(); x++)
                {
                    Map deliverablesmap = (Map)deliverablesList.get(x);
                    StringList deliverablesVersion = new StringList (0);
                    
                    try
                    {
                        deliverablesVersion.addElement((String)deliverablesmap.get(select_File_Revs));
                    }
                    catch(Exception ex)
                    {
                        deliverablesVersion = (StringList)deliverablesmap.get(select_File_Revs);
                    }
                    // deliverable hyperlink formation
                    if (deliverablesVersion == null || deliverablesVersion.equals("") )
                    {
                       tempURL.append("<b>");
                       tempURL.append(" ");
                       tempURL.append("</b>");
                       tempURL.append("<br></br>"); //To show each entry in different line.

                    }
                    else
                    {
					//Modified:11-June-09:yox:R207:PRG:Bug :372619
                     if("CSV".equals(strExportFormat)){
                        
                             tempURL.append(deliverablesVersion);
                         if(x!=(deliverablesList.size()-1)){
                             tempURL.append(","); 
                         }
                     }
                     else{
                           tempURL.append(prefixDeliverableUrl);
                           tempURL.append(deliverablesVersion);
                           tempURL.append(suffixDeliverableUrl);
                           tempURL.append("<br></br>"); //To show each entryin different line.
                      }
                     }
                    deliverablesAdded ++;
                    //End:yox:R207:PRG:Bug :372619
                    

                 }
              }
             

              // check if task is a VPLM task, if it is we need to connect
              // VPLM Action Output Objects
              if (taskType.equals(vplmTask))
              {
                if (actionOutputList != null && (actionOutputList.size() > 0))
                {
                    //for(int x=0; x< actionOutputList.size(); x++)
                    for (Iterator itrAO = actionOutputList.iterator(); itrAO.hasNext(); )
                    {
                        Map actionOutputmap = (Map)itrAO.next();
						// Get list of objects
						Object tmpObj = actionOutputmap.get("from["+actionOutputRel+"].to.id");
                        StringList tmpObjList = new StringList();
                         
                        if (tmpObj !=null && tmpObj instanceof String) {
                             tmpObjList.add(tmpObj);
                        } else if (tmpObj !=null && tmpObj instanceof StringList) {
                             tmpObjList = (StringList)tmpObj;
                        }
                         
                        StringList actionOutputIds = (StringList) tmpObjList;
						// Get list of object names
						Object tmpName = actionOutputmap.get("from["+actionOutputRel+"].to.name");
                        StringList tmpNameList = new StringList();
                         
                        if (tmpName !=null && tmpName instanceof String) {
                             tmpNameList.add(tmpName);
                        } else if (tmpName !=null && tmpName instanceof StringList) {
                             tmpNameList = (StringList)tmpName;
                        }
                        StringList actionOutputNames = (StringList) tmpNameList;
                        if (actionOutputIds != null && actionOutputNames != null && (actionOutputIds.size() > 0) && (actionOutputNames.size() > 0) )
                        {
                            Iterator itrName = actionOutputNames.iterator();
                            for (Iterator itrId = actionOutputIds.iterator(); itrId.hasNext(); )
                            {
                                String actionOutputId = (String)itrId.next();
                                String actionOutputName = (String)itrName.next();

                                // deliverable hyperlink formation
                                
                                tempURL.append("<b>");
                                tempURL.append(" ");
                                tempURL.append("</b>");
                                tempURL.append("<br></br>"); //To show each task in different line.
                                deliverablesAdded ++;

                            }
                        }
                    }
                }
              }
              if (deliverablesAdded == 0) tempURL.append(" ");
              taskList.add(tempURL.toString());
             }
         }
 
         return taskList;
     }
     catch (Exception ex)
     {
         ex.printStackTrace();
         throw ex;
     }
   }
 }
