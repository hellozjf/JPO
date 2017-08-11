/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.program.NotificationUtil;


/**
 *  emxProgramCentralNotificationUtilBase
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 **/

public class emxProgramCentralNotificationUtilBase_mxJPO extends emxDomainObject_mxJPO
{
	/**
	 * Constructs a new emxProgramCentralNotificationUtil JPO object
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args an array of String arguments for this method
	 * @throws Exception 
	 * @throws Exception if the operation fails
	 */
	public emxProgramCentralNotificationUtilBase_mxJPO (Context context, String[] args) throws Exception 
	{
		super(context, args);
	}
	
    /**
     * This function notifies the task assignees
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     *        1 - String containing sideDoorFeature. True if URL needs to sent in notification
     * @throws Exception if operation fails
     * @since R210
     */
    public MapList notifyTaskAssignees(Context context, String[] args) throws Exception
    {
        try
        {
        	String strNotificationType = args[0];
        	NotificationUtil notificationUtil = new NotificationUtil();
        	return notificationUtil.notifyTaskAssignees(context, false, strNotificationType);
        }
        catch (Exception e)
        {
            throw new FrameworkException(e);
        }
    }
    
    /** This method creates a mail notification based on an object id.
     * Only use this method if calling from triggers or tcl.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        objectId - a String holding the id of an object to be used for
     *          evaluating selects embedded in the subject and message strings
     *        notificationName - a String holding the name of the Notification object
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF 11.0.0.0
     */

    public static int objectNotification(Context context, String[] args)
            throws Exception {
        if (args == null || args.length < 2) {
            throw (new IllegalArgumentException());
        }
        int index = 0;
        String objectId = args[index++];
        String notificationName = args[index++];
        if(notificationName.equalsIgnoreCase("PMCWorkspaceVaultContentAddedEvent"))
        {
     	  
        	MqlUtil.mqlCommand(context, "set env CONTENT_ADDED_TRIGGER TRUE");
        }
        try
        {
        emxNotificationUtil_mxJPO notify = new  emxNotificationUtil_mxJPO(context, args);	
        notify.objectNotification(context, objectId, notificationName, null);
        }
        catch(Exception e)
        {
       	 e.printStackTrace();
        }
        return 0;
    }
    /**
     * This method creates one or more mail notification based on an object id.
     * The object id passed in will be expanded to get parent id's.
     * The parent ids will be used to create mail notifications.
     * Only use this method if calling from triggers or tcl.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        objectId - id of an object that we want to expand.  From this we will
     *          check the ids from the expand to be used for
     *          evaluating selects embedded in the subject and message strings
     *        notificationName - a String holding the name of the Notification object
     *        relationshipPattern - relationship to expand on
     *        typePattern - type to expand to
     *        toDirection - expand to direction
     *        fromDirection - expand from direction
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF R211
     */

    public static int objectParentsNotification(Context context, String[] args)
            throws Exception {
        if (args == null || args.length < 2) {
            throw (new IllegalArgumentException());
        }
        int index = 0;
        String objectId = args[index++];
        String notificationName = args[index++];
        String relationshipPattern = PropertyUtil.getSchemaProperty(context, args[index++]);
        String typePattern = PropertyUtil.getSchemaProperty(context, args[index++]);
        boolean toDirection = "true".equals((String)args[index++])? true:false;
        boolean fromDirection = "true".equals((String)args[index++])? true:false;
        short level = 1;
      
        StringList objectSelects = new StringList(1);
        objectSelects.add("id");

        DomainObject domainObject = DomainObject.newInstance(context, objectId);
       
        // expand from parent
        MapList mapList = domainObject.getRelatedObjects(
                context,             // context.
                relationshipPattern, // relationship pattern.
                typePattern,         // type pattern.
                objectSelects,       // business object selectables.
                null,                // relationship selectables.
                toDirection,         // expand to direction.
                fromDirection,       // expand from direction.
                level,               // level
                null,                // object where clause
                null,
                0,
                null,
                null,
                null);

        Iterator itr = mapList.iterator();
        if(notificationName.equalsIgnoreCase("PMCWorkspaceVaultContentModifiedEvent"))
        {
     	 
        	String result = (String)MqlUtil.mqlCommand(context, "get env CONTENT_ADDED_TRIGGER");
        
        	if(result.equalsIgnoreCase("TRUE"))
        	{
        		return 0;
        	}
        }
        while (itr.hasNext())
        {
            Map map = (Map) itr.next();
            String id = (String) map.get("id");
            emxNotificationUtil_mxJPO notify = new  emxNotificationUtil_mxJPO(context, args);       
            notify.objectNotification(context, id, notificationName, null);
        }
        return 0;
    
    }
}
