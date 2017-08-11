/** ${CLASSNAME}

   Copyright (c) 1999-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program
   @sice Program Central R210
   @author NR2
*/

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import matrix.db.AccessConstants;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;

/**
 * The <code>emxProjectHoldAndCancelBase</code> class represents the JPO to process Project
 * Hold and Cancel states.
 * 
 * @version PRG R210 - Copyright (c) 2010, MatrixOne, Inc.
 * @sice Program Central R210
 * @author NR2
 */
public class emxProjectHoldAndCancelBase_mxJPO extends emxDomainObject_mxJPO
{
    // Create an instant of emxUtil JPO
    protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;
    /** Resume action on Project. */
    public static final String RESUME = "Resume";

    //Added for Notification
    public static final String PROJECT_PUTONHOLD = "0";
    public static final String PROJECT_RESUMED = "1";
    public static final String PROJECT_CANCELLED = "2";
    //End
        
    //Added:nr2:PRG:R210:06-Aug-2010:IR-065782V6R2011x
    public static final String HOLD = "Hold";
    public static boolean isGateToolBarCommandClicked = false;
    //End:nr2:PRG:R210:06-Aug-2010:IR-065782V6R2011x
    /**
     * Constructs.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since PRG V6R2011x
	 * @author NR2
     */
    public emxProjectHoldAndCancelBase_mxJPO (Context context, String[] args) throws Exception
    {
		// Call the super constructor
		super(context,args);
		if (args != null && args.length > 0)
		{
			setId(args[0]);
		}
    }
    
    /**
     * This is called from trigger when Project is moved to Cancel state.
     * 
     * @param context The Matrix Context object
     * @param args string[] contains Project Id, from state,to state and Relationship 'resource pool'.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
     */    
    public void triggerDisconnectResourcePlan(Context context, String[] args)
    throws MatrixException{
    	DebugUtil.debug("Entering triggerCheckLastDecision");
    	try{
    	//Get all Resource Plans connected to this project.
    	String projectId  = args[0];  //Project id
        String fromState = args[1];
        String toState   = args[2];
        String relationship = args[3];
    	
	        final String SELECT_RELATIONSHIP_RESOURCE_PLAN_ID = "from[" + RELATIONSHIP_RESOURCE_PLAN + "].id";
        //Get all resource request connected to this project by Resource plan relationship
		com.matrixone.apps.program.ProjectSpace projObj = (com.matrixone.apps.program.ProjectSpace) 
														   DomainObject.newInstance(context,
														   DomainConstants.TYPE_PROJECT_SPACE,
														   DomainConstants.TYPE_PROGRAM);
		
			if(null == projectId || "".equals(projectId)){
				throw new Exception();
			}
			
			projObj.setId(projectId);
			
			StringList resourcePlanIdList =  projObj.getInfoList(context, SELECT_RELATIONSHIP_RESOURCE_PLAN_ID);
			//DomainRelationship domRel = DomainRelationship.newInstance(context,RELATIONSHIP_RESOURCE_PLAN,DomainConstants.PROGRAM);
			if(null != resourcePlanIdList && resourcePlanIdList.size() > 0){
				for(int i=0;i<resourcePlanIdList.size();i++)
					DomainRelationship.disconnect(context, (String)resourcePlanIdList.get(i));
			}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
		}
		return;
    }
    
    /**
     * This is called from trigger when Project is moved to Cancel state.
     * This trigger prevents the promotion of Gates to Complete state from LifeCycle page or Project WBS.
     * 
     * @param context The Matrix Context object
     * @param args string[] contains Gate Id, from state,to state and Relationship 'Subtask'.
     * @return 0 if Sucessful
     *         1 if Any error
     * @throws Exception if operation fails
	 * @since Program Central R210
	 * @author NR2
     */    
    public int triggerCheckLastDecision(Context context, String[] args)
    throws Exception
    {
    	DebugUtil.debug("Entering triggerCheckLastDecision");

    	boolean matches = true;
    	String gateId  = args[0];  //Gate id
        String fromState = args[1];
        String toState   = args[2];
        String relationship = args[3];
    	String invokedFrom = args[4];
        
        String languageStr        = context.getSession().getLanguage();
        //Check if the last decision connected is Approve, Conditionally Approve or Cancel
        String APPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.Gate.Approve", "en");
        String CONDITIONALLYAPPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.Gate.ConditionallyApprove", "en");
        String CANCEL = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.Gate.Cancel", "en");

    	DomainObject gateObj = DomainObject.newInstance(context);
        gateObj.setId(gateId);
        
        String policyName = gateObj.getInfo(context,SELECT_POLICY);
    	if(ProgramCentralConstants.POLICY_PROJECT_REVIEW.equals(policyName) && gateObj.isKindOf(context,ProgramCentralConstants.TYPE_GATE)){
        StringList objectSelects = new StringList();
        objectSelects.add(SELECT_NAME);
        objectSelects.add(DomainConstants.SELECT_REVISION);
        
	        String where = SELECT_NAME + "=='" + APPROVE + "'||";
	        where += SELECT_NAME + "=='" + CONDITIONALLYAPPROVE + "'||";
	        //Added Newly
            where += SELECT_NAME + "=='" + HOLD + "'||";
            where += SELECT_NAME + "=='" + RESUME + "'||";
            //End
            where += SELECT_NAME + "=='" + CANCEL + "'";
        
        
        MapList relatedDecisions = gateObj.getRelatedObjects(context, 
                DomainConstants.RELATIONSHIP_DECISION, //Relationship type 
                DomainConstants.TYPE_DECISION, //objects type
                objectSelects, //objectSelectable
                null,           //relationship Selectables
                true,          //from
                false,           //to
                (short) 0,      //level
                where,          //where filter
                null,        
                (short) 0);     //limit
        	
        	//Just check if the any such decision is created
        	//To check if the user is smart
        	boolean isDecision = false;
        	String errMsg1 = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Gate.errMsg1", languageStr);
	        	//Added Newly
	        		String pattern = "^\\d+$";
	        		MapList mlListToRemove = new MapList();
    		if (relatedDecisions != null && !relatedDecisions.isEmpty()){
		        		for(int i=0;i<relatedDecisions.size();i++){
		        			Map tempMap = (Hashtable) relatedDecisions.get(i);
		        			String revision = (String) tempMap.get(SELECT_REVISION);
		        			matches = revision.matches(pattern);
		        			if(revision.length() <= 12 || !matches){
		        				mlListToRemove.add(tempMap);
		        			}
		        		}
		        		relatedDecisions.removeAll(mlListToRemove);
	        		}
		        	else{
    			if(!"JSP".equalsIgnoreCase(invokedFrom)){
		        		MqlUtil.mqlCommand(context, "notice " + errMsg1);
    			}
		        		return 1;
		        	}

	        		//From the remaining sort
	        		if(relatedDecisions.size() > 0){
	        			relatedDecisions.sort(DomainConstants.SELECT_REVISION,"descending","String");
	        			Map mLastDecision = (Map) relatedDecisions.get(0);
	        			String lastDecision = (String) mLastDecision.get(DomainConstants.SELECT_NAME);
	        			if(null != lastDecision && (APPROVE.equals(lastDecision) || CONDITIONALLYAPPROVE.equals(lastDecision)
	        					|| CANCEL.equals(lastDecision)) || isGateToolBarCommandClicked){
	        				isDecision = true;
	        			}
	        			else{
	        				isDecision = false;
	        			}

	        			if(!isDecision){
    				if(!"JSP".equalsIgnoreCase(invokedFrom)){
	        				MqlUtil.mqlCommand(context, "notice " + errMsg1);
    				}
	        				isGateToolBarCommandClicked = false;
	        				return 1;
	        			}
	        		}
	        		else{
    			if(!"JSP".equalsIgnoreCase(invokedFrom)){
        				MqlUtil.mqlCommand(context, "notice " + errMsg1);
    			}
        				isGateToolBarCommandClicked = false;
        				return 1;
	        		}
	        	//End
        	} //End If
    		isGateToolBarCommandClicked = false;
        	return 0;
    }

    /**
     * This is called from methods hold,resume or cancel.
     * This method send notification to project members when project is put in hold or resumed or cancelled. 
     * 
     * @param context The Matrix Context object
     * @param args string[] contains projectId, from state,to state and description of event.
     * @return void
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
     */    
    public void notifyProjectMembersOnPolicyChange(Context context,String[] args)
    throws MatrixException{
    	try{
    		DebugUtil.debug("Entering triggerNotifyProjectMembers");
    		// get values from args.
    		String objectId = args[0]; //Object Id
    		String oldValue = args[1];//Previous Policy/State Name 
    		String newValue = args[2]; //New Policy/State Name
    		String changeDesc = args[3]; //Policy Changed OR state changed
    		
    		ProjectSpace ps = null;
    		if(null != objectId || !"".equals(objectId) || !"null".equals(objectId)){
    			ps = (ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
    			ps.setId(objectId);
    		}
    		
    		StringList busSelects = new StringList(6);
    		busSelects.add(SELECT_TYPE);
    		busSelects.add(SELECT_NAME);
    		busSelects.add(SELECT_REVISION);
    		busSelects.add(SELECT_POLICY);
    		busSelects.add(SELECT_CURRENT);

    		Map objMap = ps.getInfo(context, busSelects);
    		String projectType = (String) objMap.get(SELECT_TYPE);
    		String projectName = (String) objMap.get(SELECT_NAME);
    		String projectRevision = (String) objMap.get(SELECT_REVISION);
    		String projectPolicy = (String) objMap.get(SELECT_POLICY);
    		String projectCurrentState = (String) objMap.get(SELECT_CURRENT);

    		busSelects.clear();
    		busSelects.add(SELECT_NAME);
    		busSelects.add(SELECT_ID);

    		StringList relSelects = new StringList();
    		String relWhere = "";
    		// get the members of this Project
    		MapList memberList = ps.getMembers(context,busSelects,relSelects, null, relWhere);
    		// get assignees of this Project
    		MapList mlPeopleAssignedToTasks = ps.getProjectTaskAssignees(context, objectId);
    		StringList slPeopleAssignedToTasks = new StringList(mlPeopleAssignedToTasks.size());
    		for(int i = 0; i < mlPeopleAssignedToTasks.size(); i++) {
    			slPeopleAssignedToTasks.add((String)((Map)mlPeopleAssignedToTasks.get(i)).get(SELECT_ID));
    		}
    		
    		
    		if (memberList.size() > 0)
    		{
    			Iterator itr = memberList.iterator();
    			StringList mailToList = new StringList(1);
    			StringList mailCcList = new StringList(1);
    			//build the To List
    			//send one mail to all the assinees in one mail
    			while (itr.hasNext())
    			{
    				Map map = (Map) itr.next();
    				String personName = (String) map.get(SELECT_NAME);
    				String personid = (String) map.get(SELECT_ID);
    				slPeopleAssignedToTasks.remove(personid);

    				// Set the "to" list.
    				mailToList.addElement(personName);
    			}
    			//code added to send mail notification to assignees begin
    			String[] strExternalProjectMembers = new String[slPeopleAssignedToTasks.size()];
    			
    			Object[] NonProjMemberIds = slPeopleAssignedToTasks.toArray();
    			for (int j = 0; j < slPeopleAssignedToTasks.size(); j++)
    				strExternalProjectMembers[j] = (String)NonProjMemberIds[j];

    			MapList mlTaskAssigneeInfo = DomainObject.getInfo(context, strExternalProjectMembers, busSelects);
    			for (Iterator iterator = mlTaskAssigneeInfo.iterator(); iterator.hasNext();) {
    				Map mpPersonDetails = (Map) iterator.next();
    				String personName = (String) mpPersonDetails.get(SELECT_NAME);
    				mailToList.addElement(personName);
    			}
    			//code added to send mail notification to assignees end

    			// Set the mail subject and message and send the mail.

    			// Sends mail notification to the owners,members,assignees.

    			//get the mail subject
    			String sMailSubject = "";
    			if(PROJECT_PUTONHOLD.equals(changeDesc))
    				sMailSubject = "emxProgramCentral.ProjectPolicyChange.emxProgramTriggerNotifyMembers.SubjectForHold";
    			else if(PROJECT_RESUMED.equals(changeDesc))
    				sMailSubject = "emxProgramCentral.ProjectPolicyChange.emxProgramTriggerNotifyMembers.SubjectForResume";
   				else
   					sMailSubject = "emxProgramCentral.ProjectStateChange.emxProgramTriggerNotifyMembers.SubjectForCancel";
    			String companyName = null;
    			sMailSubject  = emxProgramCentralUtilClass.getMessage(
    					context, sMailSubject, null, null, companyName);

    			//get the mail message
    			String sMailMessage = "";
    			if(PROJECT_PUTONHOLD.equals(changeDesc) || PROJECT_RESUMED.equals(changeDesc))
    				sMailMessage = "emxProgramCentral.ProjectStateChange.emxProgramTriggerNotifyMembers.Message";
    			else
    				sMailMessage = "emxProgramCentral.ProjectStateChange.emxProgramTriggerNotifyMembers.Message";

    			// Added for internationalized state values
    			String policy_ProjectSpace = PropertyUtil.getSchemaProperty(context, "policy_ProjectSpace");
    			String policy_ProjectSpaceHoldCancel = PropertyUtil.getSchemaProperty(context, "policy_ProjectSpaceHoldCancel");

    			String policyStrOld = "";
    			switch (oldValue){
    			case "Create" :
    			case "Assign" :
    			case "Active" :
    			case "Review" :
    			case "Complete" :
    			case "Archive" : 
    				policyStrOld = policy_ProjectSpace;
    				break;
    			case "Hold" :
    			case "Cancel" :
    				policyStrOld = policy_ProjectSpaceHoldCancel;
    				break;
    			}
    			String policyStrNew = "";
    			switch (newValue){
    			case "Create" :
    			case "Assign" :
    			case "Active" :
    			case "Review" :
    			case "Complete" :
    			case "Archive" : 
    				policyStrNew = policy_ProjectSpace;
    				break;
    			case "Hold" :
    			case "Cancel" :
    				policyStrNew = policy_ProjectSpaceHoldCancel;
    				break;
    			}

    			String i18OldValue = EnoviaResourceBundle.getStateI18NString(context, policyStrOld, oldValue, context.getLocale().getLanguage());
    			String i18NewValue = EnoviaResourceBundle.getStateI18NString(context, policyStrNew, newValue, context.getLocale().getLanguage());
    			
                        String mKey[] = {"ProjectType", "ProjectName","projectRevision","From","To"};
    			String mValue[] = {projectType, projectName,projectRevision,i18OldValue,i18NewValue};
    			sMailMessage  = emxProgramCentralUtilClass.getMessage(
    					context, sMailMessage, mKey, mValue, companyName);

    			//String rpeUserName = PropertyUtil.getGlobalRPEValue(context,ContextUtil.MX_LOGGED_IN_USER_NAME);
    			String rpeUserName = MailUtil.getAgentName(context);
    			MailUtil.setAgentName(context, rpeUserName);

    			//MailUtil.setAgentName(context, context.getUser());

    			/*MailUtil.sendMessage(context, mailToList, mailCcList, null,
    					sMailSubject, sMailMessage , null);*/

    			  Map note = new HashMap();
    	            note.put("toList", mailToList);
    	            note.put("ccList", mailCcList);
    	            note.put("bccList", null);
    	            note.put("subject", sMailSubject);
    	            note.put("message", sMailMessage);
    	            note.put("objectIdList", null);

    	            // Pack arguments into string array.
    	            String[] arg = JPO.packArgs(note);

    	            // Call the jpo to send the message.
    	            JPO.invoke(context, "emxMailUtil", null, "sendMessage", arg);
    			
    		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}

        DebugUtil.debug("Exiting triggerNotifyProjectMembers function");
    	
    }

    
    /**
     * This method has the common conditions to let the Hold,Cancel,Resume,Approve and Conditionally Approve
     * commands to be visible.
     * 
     * @param context The Matrix Context object
     * @param projObj Object of type ProjectSpace, parent of this Gate.
     * @param taskObj Object of type Task, Gate in Context. 
     * @return true if hasAccess
     *         false if does not have access.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
     */    
    private boolean hasAccess(Context context,ProjectSpace projObj, Task taskObj) throws MatrixException{
    	try{
    		if(!projObj.isKindOf(context, TYPE_PROJECT_SPACE)){
    			return false;
    		}
    		
            //String strAccess = projObj.getAccess(context); //Does it return accesses of logged in user??
    		//if((!taskObj.isKindOf(context, ProgramCentralConstants.TYPE_GATE)) || !(ProgramCentralConstants.PROJECT_ROLE_PROJECT_LEAD.equals(strAccess) || ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER.equals(strAccess))){ 
    		boolean hasModifyAcc = projObj.checkAccess(context, (short)AccessConstants.cModify);
    		if((!taskObj.isKindOf(context, ProgramCentralConstants.TYPE_GATE)) || !hasModifyAcc){	
            	return false;
            }
            
            String current = taskObj.getInfo(context,SELECT_CURRENT);
            if(taskObj.isKindOf(context, ProgramCentralConstants.TYPE_GATE) && current.equals(ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE)){
            	return false; 
            }
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return true;
    }

    /**
     * This method checks if Approve and ConditionallyApprove commands should be visible.
     * 
     * @param context The Matrix Context object
     * @param args, contains object id of the Gate in context.
     * @return true if hasAccess
     *         false if does not have access.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
     */    
    public boolean isApproveEnabled(Context context,String[] args) throws MatrixException{
    	boolean HASACCESS = true;
    	try{
    		Map programMap =(Map) JPO.unpackArgs(args);
    		String objectId = (String) programMap.get("objectId");
    		com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,DomainConstants.TYPE_PROGRAM);
    		if(null != objectId && !"".equals(objectId)){
    			taskObj.setId(objectId);
    		}

    		//Get parent project
    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);
    		//String projectId = Task.getParentProject(context, taskObj);
    		//
    		
    		com.matrixone.apps.program.ProjectSpace projObj = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.TYPE_PROGRAM);
    		
    		// if this object is not of type gate or the user does not have lead access do not show the command    		
    		if(null != projectId && !"".equals(projectId)){
    			projObj.setId(projectId);
			 
				try{
    			if(!hasAccess(context,projObj,taskObj))
    				return false;
					}catch(Exception e){
					return false;
				}
    		}
    		
    		String state =  projObj.getInfo(context, SELECT_CURRENT);
    		if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equalsIgnoreCase(state) || ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equalsIgnoreCase(state) 
    		   || STATE_PROJECT_SPACE_ARCHIVE.equals(state)){
    			HASACCESS = false;
    		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return HASACCESS;
    }
    
    /**
     * This method checks if Hold commands should be visible.
     * 
     * @param context The Matrix Context object
     * @param args, contains object id of the Gate in context.
     * @return true if hasAccess
     *         false if does not have access.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
     */    
    public boolean isHoldEnabled(Context context,String[] args) throws MatrixException{
    	boolean HASACCESS = true;
    	try{
    		Map programMap =(Map) JPO.unpackArgs(args);
    		String objectId = (String) programMap.get("objectId");
    		com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,DomainConstants.TYPE_PROGRAM);
    		if(null != objectId && !"".equals(objectId)){
    			taskObj.setId(objectId);
    		}
    		
    		//Get parent project
    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);

    		com.matrixone.apps.program.ProjectSpace projObj = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.TYPE_PROGRAM);
    		if(null != projectId && !"".equals(projectId)){
    			projObj.setId(projectId);
        		// if this object is not of type gate or the user does not have lead access do not show the command    		
        		if(null != projectId && !"".equals(projectId)){
        			projObj.setId(projectId);
					try{
        			if(!hasAccess(context,projObj,taskObj))
        				return false;
					}catch(Exception e){
						return false;
					}
        		}
    		}
    		
    		String state =  projObj.getInfo(context, SELECT_CURRENT);
    		if(STATE_PROJECT_SPACE_REVIEW.equals(state) || STATE_PROJECT_SPACE_COMPLETE.equals(state) || STATE_PROJECT_SPACE_ARCHIVE.equals(state)
    			|| ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equalsIgnoreCase(state) || 
    			ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equalsIgnoreCase(state)){
    			HASACCESS = false;
    		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return HASACCESS;
    }

    /**
     * This method checks if Resume commands should be visible.
     * 
     * @param context The Matrix Context object
     * @param args, contains object id of the Gate in context.
     * @return true if hasAccess
     *         false if does not have access.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    public boolean isReactivateEnabled(Context context,String[] args) throws MatrixException{
    	boolean HASACCESS = false;
    	try{
    		Map programMap =(Map) JPO.unpackArgs(args);
    		String objectId = (String) programMap.get("objectId");
    		com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,DomainConstants.TYPE_PROGRAM);
    		if(null != objectId && !"".equals(objectId)){
    			taskObj.setId(objectId);
    		}
    		
    		//Get parent project
    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);
    		
    		com.matrixone.apps.program.ProjectSpace projObj = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.TYPE_PROGRAM);
    		if(null != projectId && !"".equals(projectId)){
    			projObj.setId(projectId);
        		// if this object is not of type gate or the user does not have lead access do not show the command    		
        		if(null != projectId && !"".equals(projectId)){
        			projObj.setId(projectId);
					try{
        			if(!hasAccess(context,projObj,taskObj))
        				return false;
					}catch(Exception e){
						return false;
					}
        		}
    		}
    		
    		//Added:nr2:PRG:R210:06-Aug-2010:IR-065782V6R2011x
    		//Show Resume command only on the gate on which projetc was put in hold
    		StringList objectSelects = new StringList();
    		objectSelects.add(SELECT_NAME);
    		objectSelects.add(SELECT_REVISION);
    		
	        MapList relatedDecisions = taskObj.getRelatedObjects(context, 
	                DomainConstants.RELATIONSHIP_DECISION, //Relationship type 
	                DomainConstants.TYPE_DECISION, //objects type
	                objectSelects, //objectSelectable
	                null,           //relationship Selectables
	                true,          //from
	                false,           //to
	                (short) 1,      //level
	                null,          //where filter
	                null,        
	                (short) 0);     //limit
	        
            boolean isGateHavingLastDecisionHold = false;
            String decisionName = "";
            if(relatedDecisions != null && relatedDecisions.size() > 0){
            	relatedDecisions.sort(SELECT_REVISION, "descending", "String");
            	Map lastRevisionMap = (Hashtable) relatedDecisions.get(0);
            	relatedDecisions.clear();
            	decisionName = (String) lastRevisionMap.get(DomainConstants.SELECT_NAME);
            }
            //START::Commented and Added for external IR-203539V6R2014 
            /*if(null != decisionName&& HOLD.equals(decisionName)){
            	isGateHavingLastDecisionHold = true;
            }*/
            if(null != decisionName){
            	isGateHavingLastDecisionHold = true;
            }
            //END::Commented and Added for external IR-203539V6R2014 
    		//End:nr2:PRG:R210:06-Aug-2010:IR-065782V6R2011x
    		String state =  projObj.getInfo(context, SELECT_CURRENT);
    		if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(state) && isGateHavingLastDecisionHold){
    			HASACCESS = true;
    		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return HASACCESS;
    }
    
    /**
     * This method checks if Cancel commands should be visible.
     * 
     * @param context The Matrix Context object
     * @param args, contains object id of the Gate in context.
     * @return true if hasAccess
     *         false if does not have access.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    public boolean isCancelEnabled(Context context,String[] args) throws MatrixException{
    	boolean HASACCESS = true;
    	try{
    		Map programMap =(Map) JPO.unpackArgs(args);
    		String objectId = (String) programMap.get("objectId");
    		com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,DomainConstants.TYPE_PROGRAM);
    		
    		if(null == objectId || "".equals(objectId)){
    			throw new Exception();
    		}
    		
    		taskObj.setId(objectId);
    		//Get parent project
    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);

    		com.matrixone.apps.program.ProjectSpace projObj = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.TYPE_PROGRAM);
        		// if this object is not of type gate or the user does not have lead access do not show the command
    		
    		if(null == projectId || "".equals(projectId)){
    			throw new Exception();
    		}

   			projObj.setId(projectId);
			try{
   			if(!hasAccess(context,projObj,taskObj))
				return false;
			}catch(Exception e){
				return false;
			}
    		String state =  projObj.getInfo(context, SELECT_CURRENT);
    		if(STATE_PROJECT_SPACE_REVIEW.equals(state) || STATE_PROJECT_SPACE_COMPLETE.equals(state) || STATE_PROJECT_SPACE_ARCHIVE.equals(state)
        			|| ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equalsIgnoreCase(state)){
        			HASACCESS = false;
        		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return HASACCESS;
    }
    
    /**
     * This method gets the Decision name when user clicks on Approve,ConditioallyApprove,Hold,Resume or Cancel 
     * commands in Gate Default Portal view.
     * 
     * @param context The Matrix Context object
     * @param args, contains switch(which is name of the command clicked).
     * @return name of the command clicked
     * @throws MatrixException if operation fails
 	 * @since Program Central R210
	 * @author NR2
	 */    
    public String getDecisionName(Context context,String[] args) throws MatrixException{
    	String Name = "";
    	try{
    		Map programMap = (Map)JPO.unpackArgs(args);
    		Map requestMap = (HashMap)programMap.get("requestMap");
    		
    		String decisionName = (String) requestMap.get("switch");		
    		StringBuffer returnBuff = new StringBuffer();
    		//Modified:7-Mar-11:s2e:R211:PRG:IR-098670V6R2012: Translation Issue
    		String sLanguage = context.getSession().getLanguage();
    		/*returnBuff.append(decisionName);
    		Name = returnBuff.toString();*/
    		
    		if("Hold".equals(decisionName))
    		{
    			String stri18nHold = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
    					"emxProgramCentral.Common.Gate.Hold", sLanguage);
    			returnBuff.append(stri18nHold);
        		Name = returnBuff.toString();
    		}
    		else if("Cancel".equals(decisionName)){
    			String stri18nCancel = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
    					"emxProgramCentral.Common.Gate.Cancel", sLanguage);
    			returnBuff.append(stri18nCancel);
        		Name = returnBuff.toString();
    		}
    		else if("Resume".equals(decisionName)){
    			String stri18nResume = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
    					"emxProgramCentral.Common.Gate.Resume", sLanguage);
    			returnBuff.append(stri18nResume);
    		Name = returnBuff.toString();
    	}
    		else if("Approve".equals(decisionName))
    		{
    			String stri18nApprove = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
    					"emxProgramCentral.Common.Gate.Approve", sLanguage);
    			returnBuff.append(stri18nApprove);
        		Name = returnBuff.toString();
    		}
    		else if("ConditionallyApprove".equals(decisionName)){
    			String stri18nConditionallyApprove = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
    					"emxProgramCentral.Common.Gate.ConditionallyApprove", sLanguage);
    			returnBuff.append(stri18nConditionallyApprove);
        		Name = returnBuff.toString();
    		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return Name;
    }
    
    /**
     * This method is the main method which is called when user clicks on any of the commands in
     * Gate default portal. It calls the Cancel,Hold or Resume methods based on the Command clicked.
     * 
     * @param context The Matrix Context object
     * @param args, contains object id of the Gate in context etc.
     * @return Map containing CONTINUE/STOP flag for processing.
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map projectPolicyChangeSequence(Context context,String[] args) throws MatrixException{
    	Map returnMap = new HashMap();
    	try{
    		//This flag is added to identify if gate toolbar commands are clicked is being called from
    		isGateToolBarCommandClicked = true;
        	Map programMap = (Map)JPO.unpackArgs(args);
        	Map formMap = (HashMap) programMap.get("formMap");
        	Map paramMap = (HashMap)programMap.get("paramMap");
        	String Description = (String) paramMap.get("Description");
        	
        	Map requestMap = (HashMap) formMap.get("requestMap");
        	String objectId = (String) requestMap.get("objectId"); //Gate id
        	String decisionName = (String) requestMap.get("switch");
        	
        	String language = context.getSession().getLanguage();
        	boolean SUCCESS = true;
        	String userRole = EMPTY_STRING;
        	//Added:12-APR-2011:S4E\MS9:R210.HF4 PRG:HF-097114V6R2011x
        	//setting environment variable "decisionName" which is used in triggerCheckDependency method 
        	//to check the dependency in case of approve/conditionally approve/hold/cancel state
        	//MqlUtil.mqlCommand(context,"set env decisionName '"+decisionName+"'");
        	String mqlQueryString = "set env $1 $2";
        	MqlUtil.mqlCommand(context,mqlQueryString,"decisionName",decisionName);
			//End:12-APR-2011:S4E\MS9:R210.HF4 PRG:HF-097114V6R2011x
        	
        	Task taskObj = (Task) DomainObject.newInstance(context,TYPE_TASK,PROGRAM);
    		if(ProgramCentralUtil.isNullString(objectId)){
    			throw new Exception("Object Id is NULL or Empty");
    		}
    		taskObj.setId(objectId);
    		
    		String taskOwnerUserName = taskObj.getOwner(context).getName();
    		String loggedInUserName = context.getUser();
    		boolean isOwner = loggedInUserName.equalsIgnoreCase(taskOwnerUserName);
    		
    		//Connect only if GateObj
    		if(!taskObj.isKindOf(context,ProgramCentralConstants.TYPE_GATE)){ 
    			throw new MatrixException("Can not Connect to if not a Gate");
    		}
    		
    		DomainObject domObj = DomainObject.newInstance(context);    		
    			
    		//Added newly
    		java.util.Calendar cal = new GregorianCalendar();
    		long timeInMS = cal.getTimeInMillis();
    		//End
    		if(null != decisionName && !"".equals(decisionName)){
	    		DomainRelationship domRel = domObj.createAndConnect(context,
	    															TYPE_DECISION, //Type
	    															decisionName,        //Name
	    															String.valueOf(timeInMS),  //Revision
	    															POLICY_DECISION,           //Policy
	    															getVault(),				   //Vault
	    															RELATIONSHIP_DECISION,	   //relationship
	    															taskObj,		//obj to connect	
	    															false);         //this object is in from side
    		}
    		
    		//Add description
    		ContextUtil.startTransaction(context, true);
    		domObj.setDescription(context, Description);
    		
    		//Move Gate to Complete state on creation of any decision
    		
            String APPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.Approve", "en");
            String CONDITIONALLYAPPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.ConditionallyApprove", "en");
    		//Get parent project
    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);
    		
            ProjectSpace projObj = (ProjectSpace) DomainObject.newInstance(context,TYPE_PROJECT_SPACE,PROGRAM);
    		
            if(ProgramCentralUtil.isNotNullString(projectId)){
    			projObj.setId(projectId);
    			userRole = projObj.getAccess(context);
    		}
    		//IR-083481V6R2013x Begin
    		if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(decisionName)){
    			if(!isOwner){
        			SUCCESS=false;
        			String errMsg1 = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL, 
        					"emxProgramCentral.HoldAndCancel.errMsg3",language);
        			String errMsg2 = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL, 
        					"emxProgramCentral.HoldAndCancel.errMsg4",language);
        			throw new MatrixException(errMsg1 + " " + userRole + " " +  errMsg2);
        		}
    		}//IR-083481V6R2013x End
    		//Refactored:PRG:R212:21 Jun 2011:IR-116125V6R2012x
    		if(!userRole.equals(ProgramCentralConstants.PROJECT_ROLE_PROJECT_LEAD) && !isOwner){
    			SUCCESS=false;
    			String errMsg1 = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL, 
    					"emxProgramCentral.HoldAndCancel.errMsg3",language);
    			String errMsg2 = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL, 
    					"emxProgramCentral.HoldAndCancel.errMsg4",language);
    			throw new MatrixException(errMsg1 + " " + userRole + " " +  errMsg2);
    		}
    		
			try{
			//	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

	    		if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(decisionName)){
	    				SUCCESS = holdProject(context,objectId);
	    		}
	    		else if(RESUME.equals(decisionName)){
	    				SUCCESS = reactivateProject(context,objectId);
	    		}
	    		else if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(decisionName)){
	    				SUCCESS = cancelProject(context,objectId);
	    				taskObj.setState(context,ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE);
	    		}
	    		else if(APPROVE.equals(decisionName) || CONDITIONALLYAPPROVE.equals(decisionName)){
	    				taskObj.setState(context,ProgramCentralConstants.STATE_PROJECT_REVIEW_COMPLETE);
		    	}
			} catch(Exception e){
				e.printStackTrace();
				throw new MatrixException(e);
			} finally{
				try{
    				//ContextUtil.popContext(context);
    			}
				catch(Exception e){
					e.printStackTrace();
					throw new MatrixException(e);
				}
    		}
    		
    		if(!SUCCESS){
    			throw new MatrixException("Action " + decisionName + "Unsuccessful");
    		}
    		ContextUtil.commitTransaction(context);
    	}
    	catch(Exception e){
    		if(ContextUtil.isTransactionActive(context))
    		        ContextUtil.abortTransaction(context);
    		throw new MatrixException(e);
    	}
    	//End Refactored:PRG:R212:21 Jun 2011:IR-116125V6R2012x
    	returnMap.put("Action","CONTINUE");
    	return returnMap;
    }
    /**
     * This method is called to put the project in Hold State
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    private boolean holdProject(Context context, String gateId) throws MatrixException{
    	boolean SUCCESS = true;
    	try{
    		com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
    																					DomainConstants.TYPE_TASK,
    																					DomainConstants.TYPE_PROGRAM);
    		if(null != gateId && !"".equals(gateId))
    			taskObj.setId(gateId);
    		
    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);
    		//Modified:9-Mar-2011:NZF/MS9:IR-089591
    		if("Create".equals(taskObj.getInfo(context, SELECT_CURRENT))){
    			taskObj.setState(context, DomainConstants.STATE_PROJECT_SPACE_REVIEW);
    			SUCCESS = hold(context,projectId);
    		}else{
    		SUCCESS = hold(context,projectId);
    	}
    	}catch(Exception e){
    		if(ContextUtil.isTransactionActive(context))
    		    ContextUtil.abortTransaction(context);
    		throw new MatrixException(e);
    	}
    	//End:9-Mar-2011:NZF/MS9:IR-089591
    	return SUCCESS;
    }

    /**
     * This method is called to put the project in Hold State
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    private boolean hold(Context context,String projectId) throws Exception{
    	
    	boolean SUCCESS = true;
    	String[] arrJPOArgs = new String[3];
    	String[] arrJPOArguments = new String[1];
    	HashMap programMap = new HashMap();
    	
    	com.matrixone.apps.program.ProjectSpace projectObj = (com.matrixone.apps.program.ProjectSpace)
    														DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,
    														DomainConstants.TYPE_PROGRAM);

		if(null != projectId && !"".equals(projectId)){
			projectObj.setId(projectId);
		}
		StringList projectList = new StringList(SELECT_CURRENT);
		projectList.add(SELECT_POLICY);
		Map projectMap = projectObj.getInfo(context, projectList);
		String current = (String)projectMap.get(SELECT_CURRENT);
		String policy = (String)projectMap.get(SELECT_POLICY);

		programMap.put(SELECT_ID, projectId);
		arrJPOArguments = JPO.packArgs(programMap);

		String prevState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);
		//Added:nr2:PRG:R212:7 July 2011:IR-118644V6R2012x
		if((STATE_PROJECT_SPACE_CREATE.equals(current) || STATE_PROJECT_SPACE_ASSIGN.equals(current) || STATE_PROJECT_SPACE_ACTIVE.equals(current)
			|| STATE_PROJECT_SPACE_CREATE.equals(prevState) || STATE_PROJECT_SPACE_ASSIGN.equals(prevState) ||
			STATE_PROJECT_SPACE_ACTIVE.equals(prevState) || "".equals(prevState)) && !ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equalsIgnoreCase(current))
		{
			//Keep the current state safe in the attribute
			programMap.put(SELECT_CURRENT, current);
			programMap.put(SELECT_POLICY, policy);
			arrJPOArgs = JPO.packArgs(programMap);
			JPO.invoke(context, "emxProgramCentralUtilBase", null, "setPreviousState",arrJPOArgs,String.class);
			//End
			projectObj.setPolicy(context, ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL);	
    		
		//Added For Notification
    		String[] args = new String[4];
    		args[0] = projectId;
    		args[1] = current;
    		args[2] = projectObj.getInfo(context,SELECT_CURRENT);
    		args[3] = "0";
    		notifyProjectMembersOnPolicyChange(context,args);
    		//End
		}
		else{
			SUCCESS = false;
		}
		return SUCCESS;
    }

    /**
     * This method is called to Resume the project from Hold state
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    private boolean reactivateProject(Context context, String gateId) throws MatrixException{
    	boolean SUCCESS = true;
    	try{
			com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
												DomainConstants.TYPE_TASK,
												DomainConstants.TYPE_PROGRAM);
			if(null != gateId && !"".equals(gateId))
				taskObj.setId(gateId);

    		StringList sl = new StringList();
    		sl.add(SELECT_ID);
    		Map projectMap = taskObj.getProject(context, sl);
    		String projectId = (String)projectMap.get(SELECT_ID);
			
		SUCCESS = resume(context,projectId);
    	}
		catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
			return SUCCESS;
    }
    /**
     * This method is called to Resume the project from Hold state
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    private boolean resume(Context context,String projectId) throws Exception{
    	boolean SUCCESS = true;
    	com.matrixone.apps.program.ProjectSpace projectObj = (com.matrixone.apps.program.ProjectSpace)
    														DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,
    														DomainConstants.TYPE_PROGRAM);

		if(null != projectId && !"".equals(projectId)){
			projectObj.setId(projectId);
		}
		
		String current = projectObj.getInfo(context,SELECT_CURRENT);
		
		if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(current)){
    		String[] arrJPOArguments = new String[1];
			HashMap programMap = new HashMap();
			programMap.put(SELECT_ID, projectId);
			arrJPOArguments = JPO.packArgs(programMap);
			
			String previousState = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousState",arrJPOArguments,String.class);
			String previousPolicy = (String)JPO.invoke(context, "emxProgramCentralUtilBase", null, "getPreviousPolicy",arrJPOArguments,String.class);
			projectObj.setPolicy(context, previousPolicy);
    		
			
			//projectObj.setState(context, STATE_PROJECT_SPACE_ACTIVE);
			if(previousState == null){
				previousState = "";
			}
			projectObj.setState(context, previousState);
			
			//Added For Notification
    		String[] args = new String[4];
    		args[0] = projectId;
    		args[1] = current;
    		args[2] = projectObj.getInfo(context, SELECT_CURRENT); 
    		args[3] = "1";
    		notifyProjectMembersOnPolicyChange(context,args);
    		//End
		}
		else{
			SUCCESS = false;
		}
		return SUCCESS;
    }

    /**
     * This method is called to Cancel the Project
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    private boolean cancelProject(Context context, String gateId) throws MatrixException{
    	boolean SUCCESS = true;
    	try{
				com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
													DomainConstants.TYPE_TASK,
													DomainConstants.TYPE_PROGRAM);
				if(null != gateId && !"".equals(gateId))
				taskObj.setId(gateId);
				
	    		StringList sl = new StringList();
	    		sl.add(SELECT_ID);
	    		Map projectMap = taskObj.getProject(context, sl);
	    		String projectId = (String)projectMap.get(SELECT_ID);

			SUCCESS = cancel(context,projectId);
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return SUCCESS;
    }
    /**
     * This method is called to Cancel the Project
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
    private boolean cancel(Context context,String projectId) throws MatrixException{
    	boolean SUCCESS = true;
    	com.matrixone.apps.program.ProjectSpace projectObj = (com.matrixone.apps.program.ProjectSpace)
    														DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,
    														DomainConstants.TYPE_PROGRAM);

		if(null != projectId && !"".equals(projectId)){
			projectObj.setId(projectId);
		}
		String current = projectObj.getInfo(context,SELECT_CURRENT);
		if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(current)){
			projectObj.promote(context);	
		}
		else if(STATE_PROJECT_SPACE_CREATE.equals(current)||STATE_PROJECT_SPACE_ASSIGN.equals(current)
				||STATE_PROJECT_SPACE_ACTIVE.equals(current)){
			projectObj.setPolicy(context, ProgramCentralConstants.POLICY_PROJECT_SPACE_HOLD_CANCEL);
			
			//Added:nr2:PRG:R210:20-Aug-2010:IR-068348V6R2011x			
			MqlUtil.mqlCommand(context, "trigger off" , true);
			projectObj.promote(context);
			MqlUtil.mqlCommand(context, "trigger on" , true);
			//End:nr2:PRG:R210:20-Aug-2010:IR-068348V6R2011x
			
    		//Added For Notification
    		String[] args = new String[4];
    		args[0] = projectId;
    		args[1] = current;
    		args[2] = projectObj.getInfo(context,SELECT_CURRENT);
    		args[3] = "2";
    		notifyProjectMembersOnPolicyChange(context,args);
    		//End
		}
		else{
			SUCCESS = false;
		}
		return SUCCESS;
    }
    /* End Methods to be used in Gate Portal View. */
    
    /**
     * This method is called to Hold,Cancel or Resume the Project.
	 * will be called from the Project Summary page Commands
     * @param context The Matrix Context object
     * @param args, contains gate id of the Gate in context.
     * @return boolean indicating SUCCESS or FAILURE
     * @throws MatrixException if operation fails
	 * @since Program Central R210
	 * @author NR2
	 */    
	@com.matrixone.apps.framework.ui.ProgramCallable
	public StringList projectHoldCancelResume(Context context,String[] args)throws MatrixException {
    	StringList returnVal = new StringList(); //Will contain all the projectIds on which selected operations could not be performed
    	try{
    		Map programMap = (Map) JPO.unpackArgs(args);
    		String[] projectIds = (String[]) programMap.get("emxTableRowId");
    		String command = (String) programMap.get("command");

        	com.matrixone.apps.program.ProjectSpace projectObj = (com.matrixone.apps.program.ProjectSpace)
																  DomainObject.newInstance(context,DomainConstants.TYPE_PROJECT_SPACE,
																  DomainConstants.TYPE_PROGRAM);
    		
    		for(int i=0;(null != projectIds && i < projectIds.length );i++){
	    		String projectId = projectIds[i];
	    		projectObj.setId(projectId);
	    		String userRole = projectObj.getAccess(context);
	    		String projectOwnerUserName = projectObj.getOwner(context).getName();
	    		String loggedInUserName = context.getUser();
	    		boolean isOwner = loggedInUserName.equalsIgnoreCase(projectOwnerUserName);
	    		
    			if(command.equals(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD)){
        			if(userRole.equals(ProgramCentralConstants.ROLE_PROJECT_LEAD) || isOwner){
        			//	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						try {
			    			if(!hold(context,projectId)){
			    				returnVal.add(projectId);
			    			}
						} finally {	
						//	ContextUtil.popContext(context);
						}
		    			//when project is hold
		    			notifyMailForResourceRequest(context,projectId,command);		    			
        			}
        			else{ //Not a project lead or project owner, this project could not be processed.
        				returnVal.add(projectId);
        			}
	    		}
	    		else if(command.equals(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL)){
	    			////IR-083481V6R2013x Begin
        			//if(userRole.equals(ProgramCentralConstants.ROLE_PROJECT_LEAD) || userRole.equals(ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER)){
        			if(isOwner){//IR-083481V6R2013x End
        			//	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						try {
	        				if(!cancel(context,projectId)){
	        					returnVal.add(projectId);
	        				}
						} finally {	
						//	ContextUtil.popContext(context);
						}
        				notifyMailForResourceRequest(context,projectId,command);
        			}
        			else{ //Not a project lead or project owner, this project could not be processed.
        				returnVal.add(projectId);
        			}
	    		}
	    		else if(command.equals(RESUME)){ //TODO
        			if(userRole.equals(ProgramCentralConstants.ROLE_PROJECT_LEAD) || isOwner){
        			//	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						try {
	        				if(!resume(context,projectId)){
	        					returnVal.add(projectId);
	        				}
						} finally {	
							//ContextUtil.popContext(context);
						}
        				notifyMailForResourceRequest(context,projectId,command);
	    			}
        			else{ //Not a project lead or project owner, this project could not be processed.
        				returnVal.add(projectId);
        			}
	    		}
	    		else{
	    			//Do Whatever, no action on projects in Approve or Conditionally Approve
	    		}
    		}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return returnVal;
    } 
	
	/**
	 * This method used to send Icon mail to resource pool approver.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 
	 *            projectId,projectState,
	 *            
	 * @return void - 
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2012.
	 * 
	 */	    
    private void notifyMailForResourceRequest(Context context, String projectId, String projectState) throws MatrixException {
    	if(ProgramCentralUtil.isNotNullString(projectId) && ProgramCentralUtil.isNotNullString(projectState)) {
	    	int requestCount = 0;
    		try {
	    		DomainObject domObjProject = newInstance(context, projectId);	
	    		String strRelationshipType = RELATIONSHIP_RESOURCE_PLAN;
	    		String strType = TYPE_RESOURCE_REQUEST;
	    		StringList busSelect = new StringList();
	    		busSelect.add(SELECT_ID);
	    		busSelect.add(SELECT_TYPE);
	    		busSelect.add(SELECT_NAME);
	    		busSelect.add(SELECT_CURRENT);
	    	    String whereClause = "("+SELECT_CURRENT+"=="+STATE_RESOURCE_REQUEST_REQUESTED+")";	    		
	            MapList mlRequests = domObjProject.getRelatedObjects(context,
	            		strRelationshipType, //pattern to match relationships
	            		strType, //pattern to match types
	            		busSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
	                    null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
	                    false, //get To relationships
	                    true, //get From relationships
	                    (short)1, //the number of levels to expand, 0 equals expand all.
	                    whereClause, //where clause to apply to objects, can be empty ""
	                    EMPTY_STRING,0); //where clause to apply to relationship, can be empty ""	
	            		requestCount = mlRequests.size();
	    		if(requestCount > 0) {
	    			Map map = new HashMap();
	    			for(int i=0; i < requestCount; i++) {
	    				Map resourceRequestMap = (Map) mlRequests.get(i);
	    				String strResourceRequest = (String)resourceRequestMap.get(SELECT_ID);
	    				String strRequestName = (String)resourceRequestMap.get(SELECT_NAME);
	    				if(ProgramCentralUtil.isNotNullString(strResourceRequest)) {
	    					StringList resourceMgrList = getResourceManagerForRequest(context, strResourceRequest);
	    					for(int j=0;j<resourceMgrList.size();j++) {	    						
	    						StringList slTempName = (StringList)map.get(resourceMgrList.get(j));	
		    					if(null == slTempName || slTempName.size()==0) {
	    							map.put(resourceMgrList.get(j), new StringList(strRequestName));
	    						} else if(!slTempName.contains(strRequestName)) {	    						    					
	    							slTempName.add(strRequestName);
	    							map.put(resourceMgrList.get(j), slTempName);
	    						} 	    						
	    					}	    					
	    				}	    				
	    			}	    			
	    			for(int s=0;s<map.size();s++){
	    				sendIconMail(context,projectId,projectState,map);  				
	    			}	    			
	    		}	    		    		
	    	} catch(Exception e) {
	    		throw new MatrixException(e);
	    	}
    	}
    }
    
	/**
	 * This method used to get approver list for Resource Request which are in
	 * submitted state.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: strResourceReq
	 * 
	 * @return StringList - containing the list of approver.
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2012.
	 * 
	 */	    
    private StringList getResourceManagerForRequest(Context context,String strResourceReq)throws MatrixException {
    	StringList resourceManagerList = new StringList();
    	DomainObject domRequest = newInstance(context, strResourceReq);
		StringBuffer sbRelationshipType = new StringBuffer(50);
		sbRelationshipType.append(RELATIONSHIP_RESOURCE_POOL);		
		sbRelationshipType.append(",");
		sbRelationshipType.append(RELATIONSHIP_RESOURCE_MANAGER);		
		StringBuffer sbType = new StringBuffer(50);		
		sbType.append(TYPE_ORGANIZATION);
		sbType.append(",");
		sbType.append(TYPE_PERSON);
		StringList busSelect = new StringList();
		busSelect.add(SELECT_ID);
		busSelect.add(SELECT_TYPE);		
		busSelect.add(SELECT_NAME);	
		busSelect.add(SELECT_LEVEL);	
        MapList orgList = domRequest.getRelatedObjects(context,
        		sbRelationshipType.toString(), //pattern to match relationships
        		sbType.toString(), //pattern to match types
        		busSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                true, //get To relationships
                true, //get From relationships
                (short)2, //the number of levels to expand, 0 equals expand all.
                null, //where clause to apply to objects, can be empty ""
                EMPTY_STRING,
                0); //where clause to apply to relationship, can be empty ""	       
        if(null != orgList) {
        	for(int k=0;k<orgList.size();k++) {
        		Map orgMap = (Map)orgList.get(k);
        		if("2".equals(orgMap.get(SELECT_LEVEL).toString())) {
        			resourceManagerList.add(orgMap.get(SELECT_ID).toString());
        		}        		
        	}
        }
    	return resourceManagerList;
    }
    
	/**
	 * This method used to send iconmail to approver.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: 
	 *            projectId,projectState,
	 *            Map  - Containing details of approver
	 * @return void - 
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vf2 for release version V6R2012.
	 * 
	 */	    
    private void sendIconMail(Context context, String projectId,String projectState,Map map) throws Exception {
    	String projectName = "";
        String projectPolicy = ProgramCentralConstants.POLICY_PROJECT_SPACE;
    	if(ProgramCentralUtil.isNotNullString(projectId)) {
			DomainObject domObjProject = newInstance(context, projectId);
			projectName = domObjProject.getInfo(context, SELECT_NAME);
            projectPolicy = domObjProject.getInfo(context, SELECT_POLICY);
		}
		String strMgr = null;
		StringList slRequestName = new StringList();		
		String sMailSubject = "";

        // IR-448083
        String i18ProjectState = EnoviaResourceBundle.getStateI18NString(context, projectPolicy, projectState, context.getLocale().getLanguage());
		String strMgrName = null;
		for(Iterator itr = map.keySet().iterator();itr.hasNext();){
			strMgr = (String)itr.next();
			slRequestName = (StringList)map.get(strMgr);
			int size = slRequestName.size();
			StringBuffer sbRequestNames = new StringBuffer(60);
			for(int k=0;k<size;k++){
				sbRequestNames.append(slRequestName.get(k));
				if(size-1 > k)
					sbRequestNames.append(",");						
			}
			StringList mailToList = new StringList(1);
			StringList mailCcList = new StringList(1);	
			if(ProgramCentralUtil.isNotNullString(strMgr)) {
				DomainObject domObjMgr = newInstance(context, strMgr);
				strMgrName = domObjMgr.getInfo(context, SELECT_NAME);
				mailToList.add(strMgrName);
				
				if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_HOLD.equals(projectState))		
					sMailSubject = "emxProgramCentral.ProjectPolicyChange.ResourceRequest.SubjectForHold";
				else if(ProgramCentralConstants.STATE_PROJECT_SPACE_HOLD_CANCEL_CANCEL.equals(projectState))
					sMailSubject = "emxProgramCentral.ProjectStateChange.ResourceRequest.SubjectForCancel";
				else					
					sMailSubject = "emxProgramCentral.ProjectPolicyChange.ResourceRequest.SubjectForResume";
				String companyName = null;
				sMailSubject  = emxProgramCentralUtilClass.getMessage(
						context, sMailSubject, null, null, companyName);

				//get the mail message
				String sMailMessage = "";					
				sMailMessage = "emxProgramCentral.ProjectStateChange.ResourceRequest.Message";				
				String mKey[] = {"RequestName", "ProjectName","State"};

                // IR-447723
                // String mValue[] = {sbRequestNames.toString(), projectName,projectState};
                String mValue[] = {sbRequestNames.toString(), projectName, i18ProjectState};
				sMailMessage  = emxProgramCentralUtilClass.getMessage(
						context, sMailMessage, mKey, mValue, companyName);
				String rpeUserName = PropertyUtil.getGlobalRPEValue(context,ContextUtil.MX_LOGGED_IN_USER_NAME);
				MailUtil.setAgentName(context, rpeUserName);
				MailUtil.sendMessage(context, mailToList, mailCcList, null,
						sMailSubject, sMailMessage , null);	
				strMgrName = null;
			}
		}
    }	
	/**
	 * This method will hide the commands as per different Project View
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args  
	 *            Map  - Containing details of program and functions called
	 * @return boolean - 
	 * @throws FrameworkException
	 *             if operation fails
	 * @since Added by asa13 for release version V6R2015x.
	 * 
	 */	 
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean isCommandVisible(Context context , String args[]) throws FrameworkException {
		try {
			Map requestMap = (Map) JPO.unpackArgs(args);
			String programs = (String)requestMap.get("program");
			if(ProgramCentralUtil.isNotNullString(programs))
			{
			Map settingsMap =(Map) requestMap.get("SETTINGS");
			String commandName = (String)settingsMap.get("Command Name");
			boolean isResumeorCancel = "Resume".equalsIgnoreCase(commandName) ||  "Cancel".equalsIgnoreCase(commandName );
			boolean isResume = "Resume".equalsIgnoreCase(commandName);
			boolean isDelete = "Delete".equalsIgnoreCase(commandName);
			StringList slPrograms = FrameworkUtil.split(programs, ProgramCentralConstants.COMMA);
			String program = (String)slPrograms.get(0);
			// Canceled project cannot be Resumed
			if(("emxProjectSpace:getCancelProjects".equalsIgnoreCase(program) && !isDelete)
					|| "emxProjectSpace:getCompletedProjects".equalsIgnoreCase(program) ||
					("emxProjectSpace:getHoldProjects".equalsIgnoreCase(program) && !isResumeorCancel) ||
					("emxProjectSpace:getActiveProjects".equalsIgnoreCase(program)&& isResume)){
				return false;
			}
			}

		} catch (Exception e) {
			throw new FrameworkException();
		}
		return true;
	}
	
}
