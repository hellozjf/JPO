/* emxCommonProjectManagementBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

*/

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.TaskDateRollup;
import com.matrixone.apps.common.TaskHolder;
import com.matrixone.apps.common.Message;

/**
 * The <code>emxProjectManagementBase</code> class represents the Project Management
 * JPO functionality for the AEF type.
 *
 * @version AEF 9.5.2.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonProjectManagementBase_mxJPO extends com.matrixone.apps.common.ProjectManagement
{

    /** used in triggerPromoteAction and triggerDemoteAction functions. */
    boolean doNotRecurse = false;

    protected static final String SELECT_PARENT_ID =
            "to[" + RELATIONSHIP_SUBTASK + "].from.id";

    /**
     * Constructs a new emxProjectManagement JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @throws Exception if the operation fails
     * @since AEF 9.5.2.0
     */
    public emxCommonProjectManagementBase_mxJPO (Context context, String[] args)
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
     * Constructs a new emxProjectManagement JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param id the business object id
     * @throws Exception if the operation fails
     * @since AEF 9.5.2.0
     */
    public emxCommonProjectManagementBase_mxJPO (String id)
        throws Exception
    {
        // Call the super constructor
        super(id);
    }

    /**
    * Delete override method for Project Management types
    * Deletes the Project/Task related structure
    *
    * Deletes Assessment, Risk, Finacial Items, Tasks, etc.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - ${OBJECTID}
    * @throws Exception if operation fails
    * @since AEF 9.5.2.0
    * @grade 0
    */
    public int triggerDeleteOverride(Context context,
                                    String[] args)
        throws Exception
    {
        //The first time this function is called this value will be false
        //second time around this will be true
        //Instead of recurssing through each of the subtasks the program
        //gets all the tasks in one call and deletes all the tasks
        //thereon if the sub-tasks call this function it returns without
        //doing anything
        if (doNotRecurse)
        {
            //function called recursively return without doing anything
            return 0;
        }

        doNotRecurse = true;

        // get values from args.
        String objectId = args[0];

        //com.matrixone.apps.program.ProjectSpace project = new com.matrixone.apps.program.ProjectSpace(objectId);
        com.matrixone.apps.common.Task project =
          (com.matrixone.apps.common.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK);
        project.setId(objectId);
        StringList sels = new StringList();

        DomainObject domainObject = DomainObject.newInstance(context);

        com.matrixone.apps.common.Task task =
          (com.matrixone.apps.common.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK);

        com.matrixone.apps.common.Route route = null;

        StringList busSelects = new StringList();

        //delete all the subtasks
        busSelects.clear();
        busSelects.add(task.SELECT_ID);
        MapList utsList = task.getTasks(context, (TaskHolder) project, 1, busSelects, null, true);

        if (utsList.size() > 0)
        {
            Iterator itr = utsList.iterator();
            while (itr.hasNext())
            {
                Map map = (Map) itr.next();
                String taskId = (String) map.get(task.SELECT_ID);
                task.setId(taskId);
                task.delete(context, true);
            }
        }

        //delete all Route objects if they are not referenced to any other object;
        //otherwise, just disconnect them from the project.
        route.removeRoutes(context, objectId, true);

        //delete all Thread/Message objects connected to the MessageHolder objects
        //derived from ProjectManagement
        task.setId(objectId);
        com.matrixone.apps.common.Message.deleteMessages(context,(DomainObject) task);

        //delete the "Project Access List" object
        //*** Leave this last to be deleted.
        String accessListId = project.getInfo(context,
                      "to[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.id");

        int ret = 0;
        if (accessListId != null)
        {
            domainObject.setId(accessListId);
            domainObject.remove(context);
        }
        else
        {
            String parentId = project.getInfo(context, SELECT_PARENT_ID);
            if (parentId != null)
            {
                // go ahead and delete the task so that we can perform
                // percent & date rollup.
                ret = 1;
                domainObject.setId(project.getId());
                domainObject.remove(context);


                String[] jpoArgs = new String[1];
                jpoArgs[0] = parentId;
                JPO.invoke(context,                     // matrix context
                           "emxCommonTask",                   // program name
                            null,                       // constructor arguments
                            "calculatePercentComplete", // method name
                            jpoArgs,                    // method arguments
                            null);                      // return class

                
            	//rollup will be done in delete() method of com.matrixone.apps.common.Task therefore commenting this code
               /* TaskDateRollup rollup = new TaskDateRollup(parentId);
                rollup.validateTask(context);*/

            }
        }
        return ret;
    }
}
