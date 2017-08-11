/*
**   emxCriticalTaskBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.10.2.1 Thu Dec  4 07:55:15 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.10 Wed Oct 22 15:50:30 2008 przemek Experimental przemek $
*/

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

/**
 * The <code>emxCriticalTaskBase</code> class contains methods for emxTask.
 *
 * @version PMC 10.5.1.2 - Copyright(c) 2004, MatrixOne, Inc.
 */

public class emxCriticalTaskBase_mxJPO extends com.matrixone.apps.program.Task
{

   protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;

   /**
    * Constructs a new emxLibCriticalTask JPO object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - String containing the id
    * @throws Exception if the operation fails
    * @since PMC 10.5.1.2
    */

    public emxCriticalTaskBase_mxJPO (Context context, String[] args)
    throws Exception
    {
        // Call the super constructor
        super();

        if (args != null && args.length > 0)
        {
            setId(args[0]);
        }
    }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns int
       * @throws Exception if the operation fails
       * @since PMC 10.5.1.2
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception("must specify method on emxCriticalTaskBase invocation");
          }
          return 0;
      }

    /**
     * Returns the "Critical Task" Status and sends notification if it is TRUE.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     *        0 - objectId of the Task
     * @throws Exception if the operation fails
     * @since PMC 10.5.1.2
     */
    public void triggerModifyCriticalTaskAction (Context context, String[] args)
    throws Exception
    {
        // get values from args.
        String objectId = args[0];
        String SELECT_CRITICAL_TASK = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_CriticalTask") + "]";

        //Determine if Critical Task attribute is set to TRUE.
        setId(objectId);

        String POLICY_PROJECT_TASK = PropertyUtil.getSchemaProperty(context, "policy_ProjectTask");
        String STATE_PROJECT_TASK_ASSIGN = PropertyUtil.getSchemaProperty(context,"policy", POLICY_PROJECT_TASK,"state_Assign");
        String STATE_PROJECT_TASK_ACTIVE =PropertyUtil.getSchemaProperty(context,"policy",POLICY_PROJECT_TASK,"state_Active");


        StringList busSelects = new StringList();
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_DESCRIPTION);
        busSelects.add(SELECT_CRITICAL_TASK);
        busSelects.add(SELECT_OWNER);
        busSelects.add(SELECT_CURRENT);

        Map taskMap = getInfo(context, busSelects);

        String isCriticalTask = taskMap.get(SELECT_CRITICAL_TASK).toString();
        String task_current = (String)taskMap.get(SELECT_CURRENT);
        StringList mailToList = new StringList();
        StringList mailAssigneeToList = new StringList();
        mailToList.add(taskMap.get(SELECT_OWNER));

        StringList objectIdList = new StringList();
        StringList emptyList = new StringList();
        StringList objSelects = new StringList();
        objSelects.add(SELECT_NAME);

        String companyName = null;
        Map projectMap = getProject(context,objSelects);
        String projectName = projectMap.get(SELECT_NAME).toString();
        String taskName = taskMap.get(SELECT_NAME).toString();
        String taskDescription = taskMap.get(SELECT_DESCRIPTION).toString();
        String taskOwner = taskMap.get(SELECT_OWNER).toString();
        String taskAssignee = taskMap.get(SELECT_OWNER).toString();


        busSelects = new StringList();
        busSelects.add(SELECT_NAME);

        MapList assigneeList =  getAssignees(context, busSelects, emptyList, "");

        if(assigneeList != null && assigneeList.size() > 0){
            for(int i =0; i <assigneeList.size(); i++){
                Map taskAssigneeMap = (Map) assigneeList.get(i);
                String assigneeName = taskAssigneeMap.get(SELECT_NAME).toString();
                if(!assigneeName.equals(taskOwner))
                {
                    mailAssigneeToList.add(assigneeName);
                    taskAssignee += ", " + assigneeName;
                }
            }
        }
        objectIdList.add(objectId);
        String mKey[] = {"ProjectName", "TaskName", "TaskDescription",
                                     "TaskAssignee"};
        String mValue[] = {projectName, taskName, taskDescription,
                               taskAssignee};
        // When the task is in either Assign/Active state
        if(STATE_PROJECT_TASK_ASSIGN.equals(task_current) ||  STATE_PROJECT_TASK_ACTIVE .equals(task_current))
        {
            // Sends mail notification to the owners.
            //get the mail subject
            String sMailSubject = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyCriticalTaskOwner.Subject";

            //get the mail message
            String sMailMessage = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyCriticalTaskOwner.Message";
            String sMailAssigneeMessage = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyCriticalTaskAssignee.Message";

            // When the task is not critical
            if(isCriticalTask.equalsIgnoreCase("FALSE"))
            {
                // Sends mail notification to the owners.
                //get the mail subject
                sMailSubject = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNonCriticalTaskOwner.Subject";

                //get the mail message
                sMailMessage = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNonCriticalTaskOwner.Message";
                sMailAssigneeMessage = "emxProgramCentral.ProgramObject.emxProgramTriggerNotifyNonCriticalTaskAssignee.Message";
            }
            sMailSubject  = emxProgramCentralUtilClass.getMessage(
                        context, sMailSubject, null, null, companyName);
            sMailMessage  = emxProgramCentralUtilClass.getMessage(
                            context, sMailMessage, mKey, mValue, companyName);
            sMailAssigneeMessage  = emxProgramCentralUtilClass.getMessage(
                            context, sMailAssigneeMessage, mKey, mValue, companyName);

            String rpeUserName = PropertyUtil.getGlobalRPEValue(context,ContextUtil.MX_LOGGED_IN_USER_NAME);
            MailUtil.setAgentName(context, rpeUserName);

            MailUtil.sendMessage(context, mailToList, null, null, sMailSubject, sMailMessage, objectIdList);
            MailUtil.sendMessage(context, mailAssigneeToList, null, null, sMailSubject, sMailAssigneeMessage, objectIdList);
        }

    }
}


