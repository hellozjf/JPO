/* emxChecklistBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.36.2.1 Thu Dec  4 07:55:16 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.36 Tue Oct 28 18:55:12 2008 przemek Experimental przemek $
 */

/*
Change History:
Date       Change By  Release   Bug/Functionality         Details
-----------------------------------------------------------------------------------------------------------------------------
29-Apr-10   VM3        V6R2011X   373332                   Change Code for I18n
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.CheckList;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;


/**
 * The <code>emxChecklistBase</code> class represents the Task JPO
 * functionality for the PRG type.
 * @since PRG 2011x
 * @author VM3
 */

public class emxChecklistBase_mxJPO extends com.matrixone.apps.program.Task
{
	// Create an instant of emxUtil JPO
	protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;
	/* Attribute indicates person completing Checklist*/
	public static final String ATTRIBUTE_COMPLETED_BY = PropertyUtil.getSchemaProperty("attribute_CompletedBy");

	/* Attribute indicates response for the Checklist Item*/
	public static final String ATTRIBUTE_RESPONSE = PropertyUtil.getSchemaProperty("attribute_Response");

	/* Attribute indicates response type for the Checklist Item*/
	public static final String ATTRIBUTE_RESPONSE_TYPE = PropertyUtil.getSchemaProperty("attribute_ResponseType");

	/* Type Checklist Item*/
	public static final String TYPE_CHECKLIST_ITEM = PropertyUtil.getSchemaProperty("type_ChecklistItem");

	/* Type Checklist*/
	public static final String	TYPE_CHECKLIST = PropertyUtil.getSchemaProperty("type_Checklist");

	/* Relationship between all type and type Checklist*/
	public static final String	RELATIONSHIP_CHECKLIST = PropertyUtil.getSchemaProperty("relationship_Checklist");

	/* Relationship between type Checklist and type Checklist*/
	public static final String	RELATIONSHIP_CHECKLIST_ITEM = PropertyUtil.getSchemaProperty("relationship_ChecklistItem");

	/** The "Project Owner" range item on the "Project Access" attribute. */
	protected static final String RANGE_PROJECT_OWNER = "Project Owner";

	/**
	 * Checklist Policy
	 */

	public static final String POLICY_CHECKLIST = PropertyUtil.getSchemaProperty("policy_Checklist"); 

	/**
	 * ChecklistItem Policy
	 */

	public static final String POLICY_CHECKLISTITEM = PropertyUtil.getSchemaProperty("policy_ChecklistItem"); 

	/** Complete State of type Checklist*/
	public static final String STATE_COMPLETE =
		PropertyUtil.getSchemaProperty("policy",
				PropertyUtil.getSchemaProperty("policy_Checklist"),
		"state_Complete");

	/** Active State of type Checklist*/
	public static final String STATE_ACTIVE =
		PropertyUtil.getSchemaProperty("policy",
				PropertyUtil.getSchemaProperty("policy_Checklist"),
		"state_Active");


	/**
	 * Constructs a new emxTask JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the id
	 * @throws Exception if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	public emxChecklistBase_mxJPO (Context context, String[] args)
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
	 * When the checklist is promoted from active state to complete state this function is called.
	 * this trigger is used to get the name of the person who completes the checklist
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns nothing
	 */
	public void triggerChecklistCompletedBy(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			String strContextUser = context.getUser();

			ContextUtil.pushContext(context);
			domObj.setAttributeValue(context, ATTRIBUTE_COMPLETED_BY, strContextUser);
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
		finally{
			ContextUtil.popContext(context);
		}

	}

	/**
	 * When the checklist is demoted from complete state to active state this function is called.
	 * this trigger is used to reset the value of completed by attribute on checklist type
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns nothing
	 */
	public void triggerChecklistDemoteCompletedBy(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			domObj.setAttributeValue(context, ATTRIBUTE_COMPLETED_BY, "");
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	} 

	/**
	 * When the checklist is demoted from complete state to active state this trigger method is called.
	 * this trigger is used to demote all the parent checklists to active state
	 * Note: object id must be passed as first argument.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns nothing
	 */
	public void triggerDemoteParentChecklists(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList();
			String strResponse = "";
			String strType = "";
			String strId = "";
			String strState = "";
			MapList checklist= domObj.getRelatedObjects(
					context,                                     //matrix context
					RELATIONSHIP_CHECKLIST,
					TYPE_CHECKLIST,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					true,                                        // getTo
					false,                                        // getFrom
					(short)0,                                    // recurseToLevel
					null,                                        // objectWhere
					null,                                         // relationshipWhere
					0
			);

			for(int itr = 0; itr < checklist.size(); itr++){
				Map map = (Map)checklist.get(itr);
				strId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context,strId);

				strState = domObject.getInfo(context,SELECT_CURRENT);
				if(null != strState && strState.equalsIgnoreCase(STATE_COMPLETE)){
					domObject.setState(context, STATE_ACTIVE);
				}
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}
	/**
	 * When the checklist is promoted from active state to complete state this function is called.
	 * this check trigger is used to to check whether all the checklist items connected has responces.
	 * Note: object id must be passed as first argument.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @returns integer value
	 * @since PRG 2011x
	 * @author VM3
	 */
	public int triggerChecklistItemResponseCheck(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList();
			String strResponse = "";
			String strType = "";
			String strId = "";
			MapList checklist= domObj.getRelatedObjects(
					context,                                     //matrix context
					RELATIONSHIP_CHECKLIST_ITEM,
					TYPE_CHECKLIST_ITEM,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					false,                                        // getTo
					true,                                        // getFrom
					(short)0,                                    // recurseToLevel
					null,                                        // objectWhere
					null                                         // relationshipWhere
			);

			int allResponses = 0;
			for(int itr = 0; itr < checklist.size(); itr++){
				Map map = (Map)checklist.get(itr);
				strId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context,strId);
				strResponse = domObject.getAttributeValue(context, ATTRIBUTE_RESPONSE);
				if(null == strResponse || strResponse.equalsIgnoreCase(""))
				{
					allResponses = 1;
					break;
				}
			}
			return allResponses;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * When the checklist is in complete state 
	 * then to avoid modification of the connected checklist items.
	 * Note: object id must be passed as first argument.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @returns integer value
	 * @since PRG 2011x
	 * @author VM3
	 */
	public int triggerChecklistItemModifyAttributeNameDescription(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);

			int isModify = 0;
			String strState = domObj.getInfo(context, "to["+RELATIONSHIP_CHECKLIST_ITEM+"].from.current");

			if(null != strState && strState.equalsIgnoreCase(STATE_COMPLETE))
			{
				isModify = 1;
			}

			return isModify;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}


	/**
	 * When the checklist is in complete state 
	 * then to avoid modification of the connected checklist.
	 * Note: object id must be passed as first argument.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @returns integer value
	 * @since PRG 2011x
	 * @author VM3
	 */
	public int triggerChecklistModifyNameDescription(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_CURRENT);

			int isModify = 0;
			String strType = "";
			String strState = "";
			String strId = "";
			Map mapChecklist = domObj.getInfo(context, selects);
			strState = (String)mapChecklist.get(DomainConstants.SELECT_CURRENT);

			if((null != strState || strState.equalsIgnoreCase(""))&& strState.equalsIgnoreCase(STATE_COMPLETE))
			{
				isModify = 1;
			}

			return isModify;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * When the checklist is promoted from active state to complete state this function is called.
	 * this trigger is used to get complete all the sub checklists connected.
	 * Note: object id must be passed as first argument.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws MatrixException if operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns nothing
	 */
	public void triggerChecklistPromoteSubChecklists(Context context, String[] args)
	throws MatrixException
	{
		try{
			// get values from args.
			String objectId = args[0];
			DomainObject domObj = DomainObject.newInstance(context,objectId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList();
			String strResponse = "";
			String strType = "";
			String strId = "";
			String strState = "";
			MapList checklist= domObj.getRelatedObjects(
					context,                                     //matrix context
					RELATIONSHIP_CHECKLIST,
					TYPE_CHECKLIST,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					false,                                        // getTo
					true,                                        // getFrom
					(short)0,                                    // recurseToLevel
					null,                                        // objectWhere
					null                                         // relationshipWhere
			);

			for(int itr = 0; itr < checklist.size(); itr++){
				Map map = (Map)checklist.get(itr);
				strId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context,strId);
				//matrix.db.State state = domObject.getCurrentState(context);
				strState = domObject.getInfo(context,SELECT_CURRENT);
				if(null != strState && strState.equalsIgnoreCase(STATE_ACTIVE)){
					domObject.setState(context, STATE_COMPLETE);
				}
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to show the Response type of the checklist Item.
	 * @param context the eMatrix <code>Context</code> object
	 * @since PRG 2011x
	 * @throws MatrixException if operation fails
	 * @author VM3
	 * @returns StringList
	 */
	public Map getResponseType(Context context, String[] args)
	throws MatrixException
	{
		try{
			Map mapResponseType = new HashMap();
			StringList slDisplayOptions = new StringList();
			StringList slOptions = new StringList();

			for(int i = 1; ;i++) {
				try {
					String strOptionKey = "emxProgramCentral.Checklist.Item.ResponseType" + i + ".Options";
					String strOption = EnoviaResourceBundle.getProperty(context, strOptionKey);
					if(null != strOption && strOption.contains(" ")){
						strOption = strOption.replace(" ", "_");
					}
					String strDisplayOption = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							strOptionKey, context.getSession().getLanguage());
					slOptions.add(strOption);
					slDisplayOptions.add(strDisplayOption);

				} catch(Exception e) {
					break;
				}
			}

			mapResponseType.put("field_choices", slOptions);
			mapResponseType.put("field_display_choices", slDisplayOptions);

			return mapResponseType;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/*public void createChecklistpreProcess(Context context, String[] args) throws MatrixException 
	{
		try{
			Map programMap    = (Map) JPO.unpackArgs(args);
			Map paramMap      = (Map) programMap.get("paramMap");
			Map requestMap    = (Map) programMap.get("requestMap");
			String strRelId = (String) paramMap.get("relId");
			String strFromType = "to["+RELATIONSHIP_CHECKLIST+"].from.type";
			//DomainRelationship domRel = DomainRelationship.newInstance(context,strRelId);
			//domRel.getInfo(context, strFromType., arg2);
			String newObjectId = (String) paramMap.get("newObjectId");
			//String parentId = (String) requestMap.get("parentOID");

			DomainObject dmoChild = DomainObject.newInstance(context,newObjectId);
			strFromType = dmoChild.getInfo(context, strFromType);
			if(null != strFromType && !"".equalsIgnoreCase(strFromType) && strFromType.equalsIgnoreCase(TYPE_CHECKLIST_ITEM)){

			}

			DomainObject dmoParent = DomainObject.newInstance(context,parentId);
			DomainRelationship.connect(context,dmoParent, RELATIONSHIP_CHECKLIST, dmoChild);
			ContextUtil.commitTransaction(context);
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}*/

	/**
	 * This method is used to set the value of attribute Response Type.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns nothing.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createChecklistItem(Context context, String[] args) throws MatrixException 
	{
		try{
			HashMap programMap    = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap      = (HashMap) programMap.get("paramMap");
			HashMap requestMap    = (HashMap) programMap.get("requestMap");
			String strResponse = (String)requestMap.get(ATTRIBUTE_RESPONSE_TYPE);
			String objectId       = (String) paramMap.get("objectId");        	
			DomainObject dmoChild = DomainObject.newInstance(context,objectId);
			dmoChild.setAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE, strResponse);

			//Call createRevisionMethod
			this.createRevision(context, args);
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used as an access function to the Checklist summary table toolbar commands.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns boolean.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * returns boolean
	 */
	public boolean checkCreateObjectAccessFunctionProjectLead(Context context, String[] args) throws MatrixException 
	{
		try{

			HashMap programMap    = (HashMap) JPO.unpackArgs(args);
			String objectId       = (String) programMap.get("objectId"); //Object Id of Gate        	

			if(null == objectId || "".equals(objectId)){
				throw new Exception();
			}

			//Added, since this method is called when copy selected.
			String emxTableRowIdActual = (String) programMap.get("emxTableRowIdActual");
			if(null != emxTableRowIdActual){
				return true;
			}
			//end

			com.matrixone.apps.program.Task taskObj = (com.matrixone.apps.program.Task) 
			DomainObject.newInstance(context,
					DomainConstants.TYPE_TASK,
					DomainConstants.TYPE_PROGRAM);

			taskObj.setId(objectId);

			//Gat parent project
			StringList sl = new StringList();
			sl.add(SELECT_ID);
			Map projectMap = taskObj.getProject(context, sl);

			if(null == projectMap)
				throw new Exception();

			String projectId = (String) projectMap.get(SELECT_ID);

			if(null == projectId || "".equals(projectId)){
				throw new Exception();
			}

			com.matrixone.apps.program.ProjectSpace projectObj = (com.matrixone.apps.program.ProjectSpace)
			DomainObject.newInstance(context,
					DomainConstants.TYPE_PROJECT_SPACE,
					DomainConstants.TYPE_PROGRAM);

			projectObj.setId(projectId);

			//Get Project Access
			String hasAccess = projectObj.getAccess(context);

			if(DomainConstants.ROLE_PROJECT_LEAD.equals(hasAccess) || RANGE_PROJECT_OWNER.equals(hasAccess)){
				return true;
			}

			return false;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to get the value of attribute Response.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns vector.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	public Vector getResponseChecklistItem(Context context, String[] args)
	throws MatrixException
	{
		try{
			Map programMap =   (Map)JPO.unpackArgs(args);
			Map paramList      = (HashMap) programMap.get("paramList");
			MapList objectList = (MapList)programMap.get("objectList"); 
			String strType = "";
			String strTemp = "";
			String objectId = "";
			int size = objectList.size();
			String strAccess = "";
			Vector vecResponse = new Vector(size);
			String strResponse = "";
			String sResponseType = "";
			String isMultiSelect = "true";

			StringList slResponseType = new StringList();
			for(int itr = 1; ; itr++){
				try {
					sResponseType = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options");
					slResponseType.add(sResponseType);
				}
				catch(Exception exp) {
					break;
				}
			}

			for(int iterator = 0; iterator < objectList.size(); iterator++){
				// [MODIFIED::Feb 11, 2011:S4E:R211:	IR-080886V6R2012::Start] 
				Map map = (Map)objectList.get(iterator);
				objectId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context, objectId);
				// [MODIFIED::Feb 11, 2011:S4E:R211:	IR-080886V6R2012::End] 
				if(domObject.isKindOf(context, TYPE_CHECKLIST_ITEM)){
					strResponse =  domObject.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);
					//Added By Nishant
					String strResponseTmp = strResponse.replaceAll("_", " ");
					//End

					for(int itr = 0; itr < slResponseType.size(); itr++){
						int nCount = itr+1;
						//if(null != strResponse && strResponse.equalsIgnoreCase((String)slResponseType.get(itr))){
						if(null != strResponse && strResponseTmp.equalsIgnoreCase((String)slResponseType.get(itr))){
							isMultiSelect = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Checklist.Item.ResponseType"+nCount +".IsMultiSelect");
						}
					}
					strAccess = getResponseForChecklistItem(context,objectId,strResponse,isMultiSelect);
					vecResponse.add(strAccess);
				}
				else{
					vecResponse.add(strTemp);
				}
			}
			return  vecResponse;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This private method is get called from getResponseChecklistItem method to
	 * select and display checkbox or radio button in response column.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @since PRG 2011x
	 * @author VM3
	 * @throws Exception 
	 * @returns String
	 */
	private String getResponseForChecklistItem(Context context, String objectId,String sResponse, String multiSelect)
	throws Exception{
		String strResponseType = sResponse;
		String isMultiSelect = multiSelect;
		StringList strList = new StringList();
		String mode = "select";
		String strResponseTemp = "";
		if(null != strResponseType){
			strList = FrameworkUtil.split(strResponseType, "|");
		}
		if(null != objectId){
			DomainObject dmoObject = DomainObject.newInstance(context, objectId);
			strResponseTemp = dmoObject.getAttributeValue(context, ATTRIBUTE_RESPONSE);//TODo getInfo
		}
		String [] strResponseTypeArray = (String[])strList.toArray(new String[strList.size()]);
		String strResponse = "";
		if(null != isMultiSelect && isMultiSelect.equalsIgnoreCase("false")){
			strResponse = " ";
			String strChecked = "";
			for(int i=0; i< strResponseTypeArray.length;i++) {

				String strValue = strResponseTypeArray[i];
				String strOptionKey = "emxProgramCentral.Checklist.Item.ResponseType." + strValue;
				String strLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						strOptionKey, context.getSession().getLanguage());
				strChecked = "";
				if(null != strResponseTemp && strResponseTemp.equalsIgnoreCase(strResponseTypeArray[i].trim())){
					strChecked = " checked=\"true\"";
				}

				strResponse += "<input type=\"radio\" name=\"" + objectId + "\"" + " id=\"" + strValue + "\" value=\"Single\" onClick=\"javascript:emxUICore.getDataPost('../programcentral/emxProgramCentralChecklistUtil.jsp?objectId=" + objectId + "&amp;response=" + strResponseTypeArray[i] + "&amp;isMultiSelect=" + isMultiSelect + " &amp;strResponseType=" + strResponseType + " &amp;mode=" + mode + "','','','')\" " + strChecked + ">" + strLabel + " " + "</input>";
			}
			strResponse += " ";
		}
		else
		{
			StringList arrList = FrameworkUtil.split(strResponseTemp, "|");
			ArrayList arrResponseList = new ArrayList();

			for(int i = 0;i <strResponseTypeArray.length; i++){
				arrResponseList.add(strResponseTypeArray[i].trim());
			}
			arrList.containsAll(arrResponseList);

			strResponse = " ";
			//String strChecked = "";
			int j = 0;
			int k = 0;
			for(int i=0; i< strResponseTypeArray.length;i++){
				String checked = "";
				if(arrList.size() > 0 && k < arrList.size()){
					int indexOfResopnse = arrResponseList.indexOf(((String)arrList.get(k)).trim());
					if(indexOfResopnse == i){
						checked = " checked=\"true\"";
						k++;
					}
				}

				String strValue = strResponseTypeArray[i];
				String strOptionKey = "emxProgramCentral.Checklist.Item.ResponseType." + strValue;
				String strLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						strOptionKey, context.getSession().getLanguage());
				//strChecked = "";
				if(null != strResponseTemp && arrList.size() > 0 && j < arrList.size()){
					//strChecked = " checked=\"true\"";
					strResponse += "<input type=\"checkbox\" name=\"" + objectId + "\"" + " id=\"" + arrList.get(j) + "\" value=\"Multi\" onClick=\"javascript:emxUICore.getDataPost('../programcentral/emxProgramCentralChecklistUtil.jsp?objectId=" + objectId + "&amp;response=" + strResponseTypeArray[j] + "&amp;isMultiSelect=" + isMultiSelect + "&amp;strResponseType=" + strResponseType + " &amp;mode=" + mode + "','','','')\"" + checked + ">" + strLabel + " " + "</input>";
					j++;
				}
				else{
					strResponse += "<input type=\"checkbox\" name=\"" + objectId + "\"" + " id=\"" + strValue + "\" value=\"Multi\" onClick=\"javascript:emxUICore.getDataPost('../programcentral/emxProgramCentralChecklistUtil.jsp?objectId=" + objectId + "&amp;response=" + strResponseTypeArray[i] + "&amp;isMultiSelect=" + isMultiSelect + "&amp;strResponseType=" + strResponseType + " &amp;mode=" + mode + "','','','')\"" + checked + ">" + strLabel + " " + "</input>";
				}
			}
			strResponse += " ";
		}
		return strResponse;
	}

	/**
	 * This method is used to update the comments attribute of the checklist.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns nothing.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	public void updateCommentsChecklistItem(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			HashMap paramMap      = (HashMap) programMap.get("paramMap");
			String objectID = (String)paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");
			DomainObject dmoChecklistItem = DomainObject.newInstance(context,objectID);
			if(null != newAttrValue){
				dmoChecklistItem.setAttributeValue(context, ATTRIBUTE_COMMENTS, newAttrValue);
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to update the Response Type attribute of the checklist.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns nothing.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	public void updateResponseType(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			HashMap paramMap      = (HashMap) programMap.get("paramMap");
			String objectID = (String)paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");
			DomainObject dmoChecklistItem = DomainObject.newInstance(context,objectID);
			if(null != newAttrValue){
				dmoChecklistItem.setAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE, newAttrValue);
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to decide editability of the response column for the
	 * Checklist Item type only
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * checklist item it is true and for others it is false.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns StringList
	 */
	public StringList editCommentsChecklistItem(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  
			StringList strList = new StringList();

			String strType = ProgramCentralConstants.EMPTY_STRING;
			String strName = ProgramCentralConstants.EMPTY_STRING;
			String sParentState=ProgramCentralConstants.EMPTY_STRING; 
			for(int iterator = 0; iterator < objectList.size(); iterator++){
				Map map = (Map)objectList.get(iterator);
				String objectID = (String)map.get("id");
				DomainObject domObject = DomainObject.newInstance(context, objectID);
				if(null != strType && domObject.isKindOf(context, TYPE_CHECKLIST_ITEM)){
                    //Added for External IR-234359V6R2014 
					sParentState  = (String)domObject.getInfo(context,"to[" +RELATIONSHIP_CHECKLIST_ITEM+ "].from.current");					 
					if(sParentState.equalsIgnoreCase(STATE_COMPLETE))
					{
						strList.add(false); 
					}else if(sParentState.equalsIgnoreCase(STATE_ACTIVE)){			     
					strList.add(true);
				}
				}
				else{
					strList.add(false);
				}
			}
			return strList;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to decide editability of the state column for the
	 * Checklist type only
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * checklist it is true and for others it is false.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns StringList
	 */
	public StringList editStateChecklistSummaryTable(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  
			StringList strList = new StringList();

			for(int iterator = 0; iterator < objectList.size(); iterator++){
				Map map = (Map)objectList.get(iterator);
				String objectID = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context, objectID);


				if(domObject.isKindOf(context, TYPE_CHECKLIST)) {
					strList.add(true);
				}
				else{
					strList.add(false);
				}
			}

			return strList;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used as a range function to get the state of the checklist summary table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns HashMap
	 */
	public HashMap getStateOfChecklist(Context context, String[] args)
	throws MatrixException
	{
		HashMap stateRangeMap = new HashMap();
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			//MapList objectList = (MapList)programMap.get("objectList");  
			//HashMap paramMap      = (HashMap) programMap.get("paramMap");
			HashMap requestMap    = (HashMap) programMap.get("requestMap");
			String objectID = (String)requestMap.get("objectId");
			DomainObject domObject = DomainObject.newInstance(context, objectID);

			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList(1);
			relSelects.addElement(DomainRelationship.SELECT_ID);

			MapList checklist= domObject.getRelatedObjects(
					context,                                     //matrix context
					RELATIONSHIP_CHECKLIST,
					TYPE_CHECKLIST,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					false,                                        // getTo
					true,                                        // getFrom
					(short)0,                                    // recurseToLevel
					null,                                        // objectWhere
					null,                                         // relationshipWhere
					0
			);
			String strPolicy = ProgramCentralConstants.POLICY_CHECKLIST; //IR-179629V6R2013x 
			MapList mlChecklist = new MapList();
			Map map = new HashMap();
			String strStates = "";
			Map mapStates = new HashMap();
			for(int iterator = 0; iterator < checklist.size(); iterator++){
				Map mapCheck = (Map)checklist.get(iterator);
				String strType = (String)mapCheck.get(DomainConstants.SELECT_TYPE);
				String strObjectID =  (String)mapCheck.get(DomainConstants.SELECT_ID);
				DomainObject dmObject = DomainObject.newInstance(context, strObjectID);
				StringList strSelects= new StringList();
				strSelects.add(DomainObject.SELECT_STATES);
				if(null != strType && dmObject.isKindOf(context, TYPE_CHECKLIST)){
					mapStates = dmObject.getInfo(context, strSelects) ;
				}
			}
			StringList strList = new StringList();
			StringList strDispList = new StringList();
			StringList strListStates = (StringList)mapStates.get(DomainConstants.SELECT_STATES);
			if(null != strListStates){
				String str = strListStates.toString().substring(1, strListStates.toString().length()-1);
				String strArray[] = str.split(",");
				for(int iterator = 0; iterator < strArray.length; iterator++){
					//IR-179629V6R2013x Begin
					strStates = i18nNow.getStateI18NString(strPolicy, strArray[iterator].trim(), context.getSession().getLanguage());
					strList.addElement(strArray[iterator]);
					strDispList.addElement(strStates);
					//IR-179629V6R2013x End
				}
			}
			if(null != strList){
				stateRangeMap.put("field_choices", strList);
				stateRangeMap.put("field_display_choices", strDispList);
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
		return  stateRangeMap;
	}

	/**
	 * This method is used to update the state of the type checklist
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns nothing.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	public void updateStateOfChecklist(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			HashMap paramMap      = (HashMap) programMap.get("paramMap");
			//HashMap requestMap    = (HashMap) programMap.get("requestMap"); //BUGBUG
			String objectID = (String)paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");
			DomainObject dmoChecklist = DomainObject.newInstance(context,objectID);
			if(null != newAttrValue){
				dmoChecklist.setState(context, newAttrValue);
			}
		}
		catch(Exception ex){
			//throw new MatrixException(ex);
			//Added for IR-089502V6R2014
			try{
				
				String strMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.CheckList.Response", context.getSession().getLanguage());
				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
			}catch(Exception e)
			{
				throw new MatrixException(e);
			}
			//Added for IR-089502V6R2014
		}
	}	

	//Added:nr2:PRG:R210:18-06-2010:For Checklist functionality	
	/**
	 * This is a helper method to clone and Connect the the child of searched object. 
	 * @param context the eMatrix <code>Context</code> object
	 * @param Map holds the info about child Object
	 *        
	 * @returns String.
	 * @throws MatrixException if the operation fails
	 * @since PMC 11.0.0.0X
	 */

	private String cloneAndConnect(Context context,DomainObject origObj,Map alreadyCloned,String parentId)
	throws MatrixException
	{
		String clonedObjectId = "";
		try{
			StringList OrigObjInfoList = new StringList(5);
			OrigObjInfoList.add(SELECT_NAME);
			OrigObjInfoList.add(SELECT_TYPE);
			OrigObjInfoList.add(SELECT_POLICY);
			OrigObjInfoList.add(SELECT_VAULT);
			//Added:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
			OrigObjInfoList.add(SELECT_DESCRIPTION);
			//End:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x

			String origChildObjId = origObj.getInfo(context,SELECT_ID);
			DomainObject OrigChildObj = DomainObject.newInstance(context,origChildObjId);
			Map OrigChildObjInfoMap = OrigChildObj.getInfo(context,OrigObjInfoList);

			String type = (String) OrigChildObjInfoMap.get(SELECT_TYPE);
			String name = (String) OrigChildObjInfoMap.get(SELECT_NAME);
			String policy = (String) OrigChildObjInfoMap.get(SELECT_POLICY);
			String vault = (String) OrigChildObjInfoMap.get(SELECT_VAULT);
			//Added:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
			String description  = (String) OrigChildObjInfoMap.get(SELECT_DESCRIPTION);
			//End:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
			String relationship = "";
			if(OrigChildObj.isKindOf(context, TYPE_CHECKLIST_ITEM)){
				relationship = RELATIONSHIP_CHECKLIST_ITEM;
			}
			else{
				relationship = RELATIONSHIP_CHECKLIST;
			}

			String clonedParentId = (String) alreadyCloned.get(parentId);
			DomainObject clonedParentObj = DomainObject.newInstance(context, clonedParentId);

			DomainRelationship domRel = createAndConnect(context, type, name, getUniqueName(EMPTY_STRING),
					policy, vault, relationship, clonedParentObj, true);

			StringList sl = new StringList(1);
			sl.add(DomainRelationship.SELECT_TO_ID);
			DomainRelationship domRelObj = new DomainRelationship(domRel);
			Map relationshipData = domRelObj.getRelationshipData(context,sl);

			StringList clonedSelectedObjList = (StringList) relationshipData.get(DomainRelationship.SELECT_TO_ID);
			clonedObjectId = (String) clonedSelectedObjList.get(0);

			//Copy attribute values
			DomainObject clonedObj = DomainObject.newInstance(context,clonedObjectId);

			Map attributes = new HashMap();				
			if(clonedObj.isKindOf(context, TYPE_CHECKLIST)){

				attributes.put(ATTRIBUTE_COMPLETED_BY,"");
			}
			else{
				attributes.put(ATTRIBUTE_COMMENTS,"");
				attributes.put(ATTRIBUTE_RESPONSE,"");
				String orignalRespType = origObj.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);
				attributes.put(ATTRIBUTE_RESPONSE_TYPE,orignalRespType);
			}
			clonedObj.setAttributeValues(context, attributes);
			//Added:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
			clonedObj.setDescription(context, description);
			//End:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x			
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
		return clonedObjectId;
	}

	/**
	 * This is a helper method to clone and Connect the the child of searched object. 
	 * @param context the eMatrix <code>Context</code> object
	 * @param Map holds the info about child Object
	 *        
	 * @returns Map.
	 * @throws MatrixException if the operation fails
	 * @since PMC 11.0.0.0X
	 */

	private Map cloneAndConnect(Context context, Map childMap,Map alreadyCloned) throws MatrixException{
		try{
			//Get Child id
			String childId = (String) childMap.get(SELECT_ID);
			String childType =  (String) childMap.get(SELECT_TYPE);

			//Get its parent
			String rel = "";
			if(TYPE_CHECKLIST_ITEM.equals(childType)){
				rel = RELATIONSHIP_CHECKLIST_ITEM;
			}
			else{
				rel = RELATIONSHIP_CHECKLIST;
			}
			DomainObject childObj = DomainObject.newInstance(context, childId);
			//String parentId = childObj.getInfo(context, "relationship[" + rel + "].from." + SELECT_ID);
			String parentId = childObj.getInfo(context, "to[" + rel + "].from." + SELECT_ID);

			//Check if this parent is cloned. 
			//if parent is cloned, we do not need to clone the parent 
			// We have to clone the child and connected it to already cloned parent 
			StringList toBeCloned = new StringList();
			if(alreadyCloned.containsKey(parentId)){
				String origChildObjId = childObj.getInfo(context,SELECT_ID);
				//Added:nr2:PRG:R212:2 Jun 2011:IR-111132V6R2012x
				//Check if the origChildObjId is already cloned. 
				//This is a special case when selected and searched objects are same.
				if(!alreadyCloned.containsValue(origChildObjId)){
					String clonedObjectId = cloneAndConnect(context,childObj,alreadyCloned,parentId);
					alreadyCloned.put(origChildObjId,clonedObjectId);
				}
				//End
			}
			else{
				//If not, clone it and put this value in the alreadyCloned Map.
				//As to be cloned structure can be arbitrarily deep, we have to start from the top.
				while(!alreadyCloned.containsKey(parentId)){
					toBeCloned.add(parentId);
					DomainObject Obj = DomainObject.newInstance(context, parentId);
					//parentId = childObj.getInfo(context, "relationship[" + rel + "].from." + SELECT_ID);
					parentId = childObj.getInfo(context, "to[" + rel + "].from." + SELECT_ID);
				}
			}

			//The above search will be complete when we reach the the Checklist 
			//that was selected from search result (in the worst case) 
			//StringList toBeCloned contains now the id of the objects to be cloned in increasing order
			//(i.e parent succedes child). So we will start from the last one.
			if(null == toBeCloned || toBeCloned.size() == 0){
				return alreadyCloned;
			}

			for(int i=toBeCloned.size()-1;i>=0;i--){
				String OrigObjId = (String) toBeCloned.get(i);
				DomainObject OrigObj = DomainObject.newInstance(context,OrigObjId);
				String clonedObjectId = cloneAndConnect(context, OrigObj, alreadyCloned, parentId);
				alreadyCloned.put(OrigObjId,clonedObjectId);

				//Change the parent id to the Object just cloned as this is now the parent 
				//of the Object just about to be cloned.
				parentId = clonedObjectId;
			}

			return alreadyCloned;
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
	}
	/**
	 * This method is used to implement the copy selected of the type checklist
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns boolean.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */

	public boolean copySelected(Context context, String[] args) throws MatrixException {
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId"); //Id of checklist selected from Search
			String languageStr = (String) programMap.get("languageStr");
			String strTaskid = (String) programMap.get("taskId"); //Id of the object selected in Checklist summary table
			String strTimeStamp = (String)programMap.get("timeStamp");
			// [ADDED::Jul 6, 2011:s4e:R211:HF-119083V6R2012_::Start] 
			String strFromCommand = (String)programMap.get("fromCommand");
			// [ADDED::Jul 6, 2011:s4e:R211:HF-119083V6R2012_::Start]			
			if(strTaskid == null || "".equals(strTaskid) || strObjectId == null || "".equals(strObjectId)){
				throw new Exception();
			}
			DomainObject searchedObject = DomainObject.newInstance(context, strObjectId);
			DomainObject selectedObject = DomainObject.newInstance(context, strTaskid);

			StringList searchedObjInfo = new StringList(5);
			searchedObjInfo.add(SELECT_TYPE);
			searchedObjInfo.add(SELECT_NAME);
			searchedObjInfo.add(SELECT_POLICY);
			searchedObjInfo.add(SELECT_VAULT);
			//Added:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
			searchedObjInfo.add(SELECT_DESCRIPTION);
			//End:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x

			Map searchedObjInfoMap = searchedObject.getInfo(context,searchedObjInfo);
			String type = (String) searchedObjInfoMap.get(SELECT_TYPE);
			String name = (String) searchedObjInfoMap.get(SELECT_NAME);
			// [MODIFIED::Feb 8, 2011:S4E:version:IR-068217V6R2012::Start] 
			// [MODIFIED::Jul 6, 2011:s4e:R211:HF-119083V6R2012_::Start]
			// "Copy of" should only be appended when the checklist alone is copied through command.
			if(ProgramCentralUtil.isNotNullString(strFromCommand)&&"True".equalsIgnoreCase(strFromCommand))
			{
				name = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.CopyOf", context.getSession().getLanguage())+" "+name;			
				
			}
			String vault = (String) searchedObjInfoMap.get(SELECT_VAULT);
			String policy = (String) searchedObjInfoMap.get(SELECT_POLICY);
			String description = (String) searchedObjInfoMap.get(SELECT_DESCRIPTION);
			//Clone the searched checklist and connect to the selected Object from summary table
			String clonedSelectedObj = "";
			if(selectedObject.isKindOf(context, TYPE_CHECKLIST_ITEM)){
				String checkListItemName = selectedObject.getInfo(context,SELECT_NAME);
				return false;
			}
			else{
				if(searchedObject.isKindOf(context, TYPE_CHECKLIST_ITEM)){
					try{
						ContextUtil.pushContext(context);
						DomainRelationship domRel = createAndConnect(context, type, name, getUniqueName(context), 
								policy, vault,RELATIONSHIP_CHECKLIST_ITEM,selectedObject,true);

						StringList sl = new StringList(1);
						sl.add(DomainRelationship.SELECT_TO_ID);
						DomainRelationship domRelObj = new DomainRelationship(domRel);
						Map relationshipData = domRelObj.getRelationshipData(context,sl);
						StringList clonedSelectedObjList = (StringList) relationshipData.get(DomainRelationship.SELECT_TO_ID);
						clonedSelectedObj = (String) clonedSelectedObjList.get(0);

						DomainObject clonedObj = DomainObject.newInstance(context,clonedSelectedObj);

						Map attributes = new HashMap();					
						attributes.put(ATTRIBUTE_COMMENTS,"");
						attributes.put(ATTRIBUTE_RESPONSE,"");
						String orignalRespType = searchedObject.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);
						attributes.put(ATTRIBUTE_RESPONSE_TYPE,orignalRespType);
						clonedObj.setAttributeValues(context, attributes);

						Map alreadyCloned = new HashMap();
						alreadyCloned.put(strTaskid,null);
						alreadyCloned.put(strObjectId,clonedSelectedObj);
						return true;	
					}
					catch(Exception e){
						throw new MatrixException(e);
					}
					finally{
						ContextUtil.popContext(context);
					}
				}
				else{
					DomainRelationship domRel = createAndConnect(context, type, name, getUniqueName(context), 
							policy, vault,RELATIONSHIP_CHECKLIST,selectedObject,true);
					StringList sl = new StringList(1);
					sl.add(DomainRelationship.SELECT_TO_ID);
					DomainRelationship domRelObj = new DomainRelationship(domRel);
					Map relationshipData = domRelObj.getRelationshipData(context,sl);
					StringList clonedSelectedObjList = (StringList) relationshipData.get(DomainRelationship.SELECT_TO_ID);
					clonedSelectedObj = (String) clonedSelectedObjList.get(0);

					//Copy attribute values
					DomainObject clonedObj = DomainObject.newInstance(context,clonedSelectedObj);

					Map attributes = new HashMap();				
					if(clonedObj.isKindOf(context, TYPE_CHECKLIST)){

						attributes.put(ATTRIBUTE_COMPLETED_BY,"");
					}
					else{
						attributes.put(ATTRIBUTE_COMMENTS,"");
						attributes.put(ATTRIBUTE_RESPONSE,"");
						String orignalRespType = searchedObject.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);
						attributes.put(ATTRIBUTE_RESPONSE_TYPE,orignalRespType);
					}
					clonedObj.setAttributeValues(context, attributes);
					//Added:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
					clonedObj.setDescription(context, description);
					//End:MS9:PRG:R210.HF4:14-Apr-2011:HF-105382V6R2011x
				}

				//Keep the id of Original Objects which are cloned
				//The first one is a dummy one.
				Map alreadyCloned = new HashMap();
				alreadyCloned.put(strTaskid,null);
				alreadyCloned.put(strObjectId,clonedSelectedObj);

				String relPattern = RELATIONSHIP_CHECKLIST + "," + RELATIONSHIP_CHECKLIST_ITEM;
				String typePattern = TYPE_CHECKLIST + "," + TYPE_CHECKLIST_ITEM;

				StringList objectSelects = new StringList(6);
				objectSelects.add(SELECT_TYPE);
				objectSelects.add(SELECT_NAME);
				objectSelects.add(SELECT_REVISION);
				objectSelects.add(SELECT_VAULT);
				objectSelects.add(SELECT_POLICY);
				objectSelects.add(SELECT_ID);

				//Get a fully expanded Map of the children of searched checklist
				MapList childrenMap = searchedObject.getRelatedObjects(context, 
						relPattern, 
						typePattern, 
						objectSelects, 
						null, 
						false, 
						true, 
						(short) 0,  //expand all
						"", 
						null,
						(short) 0);
				if(null != childrenMap && childrenMap.size() > 0){
					for(int i=0;i<childrenMap.size();i++){
						Map childMap = (Map) childrenMap.get(i);
						alreadyCloned = cloneAndConnect(context,childMap,alreadyCloned);
					}
				}
			}//End of Else
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
		return true;
	}
	//End:nr2:PRG:R210:18-06-2010:For Checklist functionality

	/**
	 * This method is used to implement the search table of type checklist
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 * @returns StringList
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getChecklists(Context context, String[] args) throws MatrixException {
		try{
			//HashMap programMap = (HashMap) JPO.unpackArgs(args);
			StringList checklist = new StringList();
			String parentType = ""; 
			String sType = "to["+RELATIONSHIP_CHECKLIST+"].from.type";
			StringList strList = new StringList();
			strList.add(SELECT_NAME);
			strList.add(SELECT_TYPE);
			strList.add(SELECT_ID);
			strList.add(sType);
			MapList mlChecklists = DomainObject.findObjects(context, TYPE_CHECKLIST, ProgramCentralConstants.QUERY_WILDCARD, null, strList);
			for(int itr = 0; itr < mlChecklists.size(); itr++){
				Map map = (Map)mlChecklists.get(itr);
				parentType = (String)map.get(sType);
				if(null != parentType && !parentType.equalsIgnoreCase(TYPE_CHECKLIST)){
					checklist.add(map.get(DomainConstants.SELECT_ID));
				}
			}
			return checklist;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method used to get name of person.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing details of approver.
	 * @throws MatrixException
	 *             if operation fails
	 * @since Added by vm3 for release version V6R2011x.
	 * @author VM3
	 */	
	public Vector getPerson(Context context, String[] args) throws MatrixException {
		Vector vecTemp = new Vector();
		Vector vecPerson = new Vector();
		try {
			Map paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			emxProgramCentralUtil_mxJPO programCentralUtil = new emxProgramCentralUtil_mxJPO(context, args);
			MapList mpTempList = new MapList();

			for (int obIterator = 0; obIterator < objectList.size(); obIterator++) {
				Map mapObject = (Map) objectList.get(obIterator);
				String strObjectId = (String)mapObject.get(DomainConstants.SELECT_ID);

				DomainObject domObj = DomainObject.newInstance(context,strObjectId);
				String strState = domObj.getInfo(context, SELECT_CURRENT);
				if(null != domObj && domObj.isKindOf(context,TYPE_CHECKLIST) && strState.equalsIgnoreCase(STATE_COMPLETE)){
					mpTempList.add(mapObject);
					paramMap.put("objectList", mpTempList);
					//vecTemp  = (Vector) (JPO.invoke(context,"emxProgramCentralUtil",null,"getPersonFullName",JPO.packArgs(paramMap),Vector.class));
					vecTemp = programCentralUtil.getPersonFullName(context, JPO.packArgs(paramMap));
					//START :Added for IR-228987V6R2014x 
					mpTempList.clear();	
					//START :Added for IR-228987V6R2014x 
				}
				else{
					vecTemp.add("");
				}
				vecPerson.add((vecTemp.get(0)).toString());
				vecTemp.clear();
			}


		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}										
		return vecPerson;
	}

	//newly added 
	/**
	 * This is a helper method to clone and Connect the the Checklists and Checklist Items of selected project template. 
	 * @param context the eMatrix <code>Context</code> object
	 * @param Map holds the info about child Object
	 * @returns nothing.
	 * @throws MatrixException if the operation fails
	 * @since PMC 11.0.0.0X
	 * @author VM3
	 */
	public void createChecklistClone(Context context, String[] args)throws MatrixException{
		try{
			String oId =  args[0];
			String strTaskId = args[1];
			DomainObject domGate = DomainObject.newInstance(context,oId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList();
			String strResponse = "";
			String strType = "";
			String strId = "";
			MapList mlChecklists= domGate.getRelatedObjects(
					context,                                     //matrix context
					RELATIONSHIP_CHECKLIST,
					TYPE_CHECKLIST,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					false,                                        // getTo
					true,                                        // getFrom
					(short)1,                                    // recurseToLevel
					null,                                        // objectWhere
					null,										// relationshipWhere
					0                                         
			);
			String strObjectId = "";
			String [] strIds = new String[4];  
			String strLanguage = context.getSession().getLanguage();
			long lTimeStamp = System.currentTimeMillis();
			String strTimestamp = "" + lTimeStamp + "";
			Map mpCloneData = new HashMap(); 
			for(int itrChecklist = 0; itrChecklist < mlChecklists.size(); itrChecklist++){
				Map mapTemp = (Map) mlChecklists.get(itrChecklist);
				strObjectId = (String) mapTemp.get(DomainObject.SELECT_ID);
				mpCloneData.put("objectId", strObjectId);
				mpCloneData.put("taskId", strTaskId);
				mpCloneData.put("timestamp", strTimestamp);
				mpCloneData.put("languageStr", strLanguage);
				copySelected(context, JPO.packArgs(mpCloneData));
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}
	//newly added

	/**
	 * This method is used to update the value of attribute Response Type.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns nothing.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void editChecklistItemDetails(Context context, String[] args) throws MatrixException 
	{
		try{
			HashMap programMap    = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap      = (HashMap) programMap.get("paramMap");
			HashMap requestMap    = (HashMap) programMap.get("requestMap");
			String strResponse = (String)requestMap.get(ATTRIBUTE_RESPONSE_TYPE);
			String objectId       = (String) paramMap.get("objectId");        	
			DomainObject dmoChild = DomainObject.newInstance(context,objectId);
			dmoChild.setAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE, strResponse);
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to refresh the table after clicking on Apply button.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns HashMap.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap postProcessRefreshTable(Context context, String[] args) throws MatrixException 
	{
		try{
			HashMap mapReturn = new HashMap();
			mapReturn.put("Action","refresh");
			return mapReturn;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to expands Checklist Summary Table Objects 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns MapList.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandChecklistSummaryTableObjects(Context context, String[] args) throws MatrixException 
	{
		try{
			Map programMap    = (HashMap) JPO.unpackArgs(args);
			String objectId       = (String) programMap.get("objectId");
			DomainObject domSelectedObject = DomainObject.newInstance(context,objectId);
			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(DomainConstants.SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList();

			String relPattern = RELATIONSHIP_CHECKLIST + "," + RELATIONSHIP_CHECKLIST_ITEM;
			String typePattern = TYPE_CHECKLIST + "," + TYPE_CHECKLIST_ITEM;

			MapList mlChecklists= domSelectedObject.getRelatedObjects(
					context,                                     //matrix context
					relPattern,
					typePattern,                              // type pattern,
					selects,                                     // objectSelects
					relSelects,                                  // relationshipPattern
					false,                                        // getTo
					true,                                        // getFrom
					(short)1,                                    // recurseToLevel
					null,                                        // objectWhere
					null,										// relationshipWhere
					0                                         
			);
			return mlChecklists;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to show the column only for Project Space 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns boolean.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */
	public boolean showColumnOnlyForProjectSpace(Context context, String[] args) throws MatrixException 
	{
		try{

			Map programMap    = (HashMap) JPO.unpackArgs(args);
			String objectId       = (String) programMap.get("objectId");
			boolean boolReturn = false;
			DomainObject domSelectedObject = null;

			if(null != objectId){
				domSelectedObject = DomainObject.newInstance(context,objectId);
				String fromType = "";
				String  fromId = "";
				if(domSelectedObject.isKindOf(context, ProgramCentralConstants.TYPE_TASK_MANAGEMENT)){
					fromType = "to["+ RELATIONSHIP_SUBTASK +"].from.type";
					fromId = "to["+ RELATIONSHIP_SUBTASK +"].from.id";
				}
				else if(domSelectedObject.isKindOf(context, TYPE_CHECKLIST)){
					fromType = "to["+ RELATIONSHIP_CHECKLIST +"].from.type";
					fromId = "to["+ RELATIONSHIP_CHECKLIST +"].from.id";
				}
				else{
					fromType = "to["+ RELATIONSHIP_CHECKLIST_ITEM +"].from.type";
					fromId = "to["+ RELATIONSHIP_CHECKLIST_ITEM +"].from.id";
				}

				String strType = domSelectedObject.getInfo(context, fromType);
				String parentTaskId = domSelectedObject.getInfo(context, fromId);

				if(parentTaskId == null){
					if(domSelectedObject.isKindOf(context, TYPE_PROJECT_SPACE))
						return true;
					else if(domSelectedObject.isKindOf(context, TYPE_PROJECT_TEMPLATE))
						return false;
				}

				if(null != strType && strType.equalsIgnoreCase(TYPE_PROJECT_SPACE)){
					return true;
				}
				else if(null != strType && strType.equalsIgnoreCase(TYPE_PROJECT_TEMPLATE)){
					return false;				
				}
				else{	
					Map pMap = new HashMap();
					pMap.put("objectId",parentTaskId);
					return showColumnOnlyForProjectSpace(context,JPO.packArgs(pMap));
				}
			}
			return false;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to show the column only for Project Template 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns boolean.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x
	 * @author VM3
	 */

	public boolean showColumnOnlyForProjectTemplate(Context context, String[] args) throws MatrixException 
	{
		try{

			Map programMap    = (HashMap) JPO.unpackArgs(args);
			String objectId       = (String) programMap.get("objectId");
			boolean boolReturn = false;
			DomainObject domSelectedObject = null;

			if(null != objectId){
				domSelectedObject = DomainObject.newInstance(context,objectId);
				String fromType = "";
				String  fromId = "";
				if(domSelectedObject.isKindOf(context, ProgramCentralConstants.TYPE_TASK_MANAGEMENT)){
					fromType = "to["+ RELATIONSHIP_SUBTASK +"].from.type";
					fromId = "to["+ RELATIONSHIP_SUBTASK +"].from.id";
				}
				else if(domSelectedObject.isKindOf(context, TYPE_CHECKLIST)){
					fromType = "to["+ RELATIONSHIP_CHECKLIST +"].from.type";
					fromId = "to["+ RELATIONSHIP_CHECKLIST +"].from.id";
				}
				else{
					fromType = "to["+ RELATIONSHIP_CHECKLIST_ITEM +"].from.type";
					fromId = "to["+ RELATIONSHIP_CHECKLIST_ITEM +"].from.id";
				}

				String strType = domSelectedObject.getInfo(context, fromType);
				String parentTaskId = domSelectedObject.getInfo(context, fromId);

				if(parentTaskId == null){
					if(domSelectedObject.isKindOf(context, TYPE_PROJECT_SPACE))
						return false;
					else if(domSelectedObject.isKindOf(context, TYPE_PROJECT_TEMPLATE))
						return true;
				}

				if(null != strType && strType.equalsIgnoreCase(TYPE_PROJECT_SPACE)){
					return false;
				}
				else if(null != strType && strType.equalsIgnoreCase(TYPE_PROJECT_TEMPLATE)){
					return true;				
				}
				else{	
					Map pMap = new HashMap();
					pMap.put("objectId",parentTaskId);
					return showColumnOnlyForProjectTemplate(context,JPO.packArgs(pMap));
				}
			}
			return false;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to Create the revision of newly created Checklist or ChecklistItem. 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns boolean.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x.HF1
	 * @author NR2
	 */	
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createRevision(Context context,String[] args)
	throws MatrixException{
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map paramMap = (Map) programMap.get("paramMap");
			String newObjectId = (String) paramMap.get("newObjectId");
			DomainObject domObj = DomainObject.newInstance(context);
			if(null != newObjectId && !"".equals(newObjectId)){
				domObj.setId(newObjectId);
				String type = domObj.getInfo(context,SELECT_TYPE);
				String name = domObj.getInfo(context,SELECT_NAME);
				String policy = domObj.getInfo(context,SELECT_POLICY);
				String vault = domObj.getInfo(context,SELECT_VAULT);

				name += "X";
				java.util.Date currentTime = new java.util.Date();
				long timeStamp = currentTime.getTime();
				String strTimeStamp = Long.valueOf(timeStamp).toString();
				BusinessObject bObj = domObj.change(context,type,name,strTimeStamp,vault,policy);
				String objectId = bObj.getObjectId(context);

				if(null != objectId && !"".equals(objectId) && !"null".equalsIgnoreCase(objectId)){
					domObj.setId(objectId);
					name = domObj.getInfo(context, SELECT_NAME);
					int iLstIndexOfChangedName = name.lastIndexOf("X");
					name = name.substring(0,iLstIndexOfChangedName);
					bObj = domObj.change(context,type,name,strTimeStamp,vault,policy);
				}
				else{
					bObj = null;
					throw new Exception();
				}
			}
			else{
				domObj = null;
				throw new Exception();
			}
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public Map getChecklistStateRange(Context context, String[] args)
	throws MatrixException{
		Map columnMap = new HashMap();
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			HashMap hmRowValues = (HashMap) programMap.get("rowValues");
			String strObjectId = (String) hmRowValues.get("objectId");
			String strPolicy = ProgramCentralConstants.POLICY_CHECKLIST; 

			DomainObject domObj = DomainObject.newInstance(context,strObjectId);
			StringList slDisp = new StringList();
			if(domObj.isKindOf(context,TYPE_CHECKLIST)){
				StringList sl = ProjectSpace.getStates(context,POLICY_CHECKLIST);
				columnMap.put("RangeValues", sl);
				//IR-179629V6R2013x Begin
				String strStates = "";

				for(int iterator = 0; iterator < sl.size(); iterator++){    				
					strStates = i18nNow.getStateI18NString(strPolicy, sl.get(iterator).toString().trim(), context.getSession().getLanguage());
					slDisp.add(strStates);
				}
				columnMap.put("RangeDisplayValue", slDisp);
				//IR-179629V6R2013x End
			}
			else if(domObj.isKindOf(context,TYPE_CHECKLIST_ITEM)){
				StringList sl = ProjectSpace.getStates(context,POLICY_CHECKLISTITEM);
				columnMap.put("RangeValues", sl);
				columnMap.put("RangeDisplayValue", sl);
			}
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
		return columnMap;
	}

	//Added:28-Feb-2011:MS9:R211 PRG:IR-093884
	/**
	 * This method is used to get the value of attribute Response.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns vector.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2012
	 * @author MS9
	 */
	public Vector getChecklistItemResponse(Context context, String[] args)
	throws MatrixException
	{
		try{
			Map programMap =   (Map)JPO.unpackArgs(args);
			Map paramList      = (HashMap) programMap.get("paramList");
			MapList objectList = (MapList)programMap.get("objectList"); 
			String strType = "";
			String objectId = "";
			int size = objectList.size();
			String strAccess = "";
			Vector vecResponse = new Vector(size);
			String strResponseType = "";
			String strLanguage=context.getSession().getLanguage();
			StringList slResponseType = new StringList();
			StringList sli18ResponseType = new StringList();

			for(int itr = 1; ; itr++){
				try {
					strResponseType = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options");
					slResponseType.add(strResponseType);
					String strKey = "emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options";
					sli18ResponseType.add(EnoviaResourceBundle.getProperty(context, "ProgramCentral", strKey, strLanguage));
				}
				catch(Exception exp) {
					break;
				}
			}

			for(int iterator = 0; iterator < objectList.size(); iterator++)
			{
				Map map = (Map)objectList.get(iterator);
				//Modified:MS9:PRG:R212:5-May-2011:IR-093884V6R2012x
				objectId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context, objectId);
				if(domObject.isKindOf(context, TYPE_CHECKLIST_ITEM))
				{       //end:MS9:PRG:R212:5-May-2011:IR-093884V6R2012x
					strResponseType =  domObject.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);
					strResponseType = strResponseType.replaceAll("_", " ");

					for(int itr = 0; itr < slResponseType.size(); itr++)
					{
						if(null != strResponseType && strResponseType.equalsIgnoreCase((String)slResponseType.get(itr)))
						{
							String stri18ResponseType = (String)sli18ResponseType.get(itr);
							stri18ResponseType = stri18ResponseType.replaceAll("_", " ");
							StringList sli18ResponseList = FrameworkUtil.split(stri18ResponseType, "|");

							String strResponseValue =  domObject.getAttributeValue(context, ATTRIBUTE_RESPONSE);
							StringList slResponseList = FrameworkUtil.split(strResponseValue, ",");
							strAccess = "";
							StringList slResponseListTemp = new StringList();
							slResponseListTemp = FrameworkUtil.split(strResponseType,"|");
							int nCount = 0;
							for (int i = 0; i < slResponseListTemp.size(); i++) 
							{
								if(slResponseList.size()>0 && slResponseListTemp.size()>0)
								{
									if((slResponseList.get(nCount)).equals(slResponseListTemp.get(i)))
									{
										String stri18nName = (String)sli18ResponseList.get(i);
										if(nCount==0)
											strAccess = stri18nName;
										else
											strAccess = strAccess+","+stri18nName;

										if((nCount+1)!=slResponseList.size())
											nCount++;
									}
								}
							}
						}
					}
					vecResponse.add(strAccess);
				}
				else{
					//Added:nr2:PRG:R212:2 Jun 2011:IR-111132V6R2012x
					//Since it is a checklist there should be no response
					vecResponse.add("");
					//Added:nr2:PRG:R212:2 Jun 2011:IR-111132V6R2012x					
				}
			}
			return  vecResponse;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	//Added:28-Feb-2011:MS9:R211 PRG:IR-093884
	/**
	 * This method is used to get the Range value of attribute Response.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Map
	 * @throws MatrixException if the operation fails
	 * @since PRG 2012
	 * @author MS9
	 */
	public Map getResponseValueRanges(Context context, String[] args)throws Exception
	{
		Map returnMap = new HashMap();
		try 
		{
			StringList strList = new StringList();
			strList.add(ProgramCentralConstants.EMPTY_STRING);

			returnMap.put("field_choices", strList);
			returnMap.put("field_display_choices", strList);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
		return returnMap;
	}	

	//Added:28-Feb-2011:MS9:R211 PRG:HF-093884
	/**
	 * This method is used to get the Range value of attribute Response in "Edit Mode".
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Map
	 * @throws MatrixException if the operation fails
	 * @since PRG 2012
	 * @author MS9
	 */
	public Map getResponseValues(Context context, String[] args)throws MatrixException
	{
		Map returnMap = new HashMap();

		try {
			/*HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap hmRowValues = (HashMap) programMap.get("rowValues");
			String strObjectId = (String) hmRowValues.get("objectId");
			String strLan = context.getSession().getLanguage();
			StringList slList = new StringList();
			StringList sli18States = new StringList();

			if(strObjectId==null)
			{
				throw new IllegalArgumentException("Object id is null");
			}

			String stri18ResponseType = "";
			String strLanguage = context.getSession().getLanguage();
			DomainObject domObject = DomainObject.newInstance(context,strObjectId);
*/
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap columnValues = (HashMap) programMap.get("columnValues");
			String type = (String) columnValues.get("Type");
			String strLan = context.getSession().getLanguage();
			String strLanguage = context.getSession().getLanguage();
			String stri18ResponseType = "";
			StringList slList = new StringList();
			StringList sli18States = new StringList();
			String strResponseType = "";

			if(type.equalsIgnoreCase(TYPE_CHECKLIST_ITEM))
			{
				String strResponse =  (String) columnValues.get("ResponseType");

				for(int itr = 1; ; itr++)
				{
					try {
						strResponseType = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options", strLanguage); 
						strResponse = strResponse.replaceAll("_", " ");
						if(null != strResponseType && strResponseType.equalsIgnoreCase(strResponse)){
							stri18ResponseType = strResponseType;
							strResponseType = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options");
							break;
						}
					}
					catch(Exception exp) {
						break;
					}
				}

				String strResponseTmp = strResponseType.replaceAll("_", " ");
				slList = FrameworkUtil.split(strResponseTmp, "|");

				strResponseTmp = stri18ResponseType.replaceAll("_", " ");
				sli18States = FrameworkUtil.split(strResponseTmp, "|");
			}
			//Modified:28-June-2011:MS9:R212 PRG:IR-117664V6R2012x
			returnMap.put("RangeValues",  slList);
			returnMap.put("RangeDisplayValue",sli18States);
			//End:28-June-2011:MS9:R212 PRG:IR-117664V6R2012x
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return returnMap;
	}	

	//Added:28-Feb-2011:MS9:R211 PRG:IR-093884
	/**
	 * This method is used to update value of attribute Response.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns
	 * @throws MatrixException if the operation fails
	 * @since PRG 2012
	 * @author MS9
	 */
	public void updateChecklistItemResponse(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			HashMap paramMap      = (HashMap) programMap.get("paramMap");
			String objectID = (String)paramMap.get("objectId");
			String newAttrValue = (String) paramMap.get("New Value");
			StringList slResponseType = new StringList();
			slResponseType = FrameworkUtil.split(newAttrValue,",");
			String strLanguage = context.getSession().getLanguage();

			DomainObject dmoChecklistItem = DomainObject.newInstance(context,objectID);
			String strResponseType = dmoChecklistItem.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);

			String isMultiSelect = "false";
			for(int itr = 1; ; itr++){
				try {
					String sResponseType = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options");
					if(null != strResponseType && strResponseType.equalsIgnoreCase(sResponseType))
					{
						isMultiSelect = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Checklist.Item.ResponseType"+itr +".IsMultiSelect");
						break;
					}
				}
				catch(Exception exp) {
					break;
				}
			}

			if(isMultiSelect.equals("false") && slResponseType.size()>1)
			{
				String strAlert = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Checklist.SelectSingleResponse", strLanguage);
				strAlert = strAlert + "\\n";
				for(int itr = 1; ; itr++)
				{
					try 
					{
						isMultiSelect = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.Checklist.Item.ResponseType"+itr +".IsMultiSelect");
						if(isMultiSelect.equals("false"))
						{
							String sResponseType = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options");
							strAlert = strAlert + sResponseType +"\\n";
						}
					}
					catch(Exception exp) {
						break;
					}
				}
				MqlUtil.mqlCommand(context, "Notice " + strAlert);	
			}
			else
			{
				dmoChecklistItem.setAttributeValue(context, ATTRIBUTE_RESPONSE, newAttrValue);
			}
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}
	/**
	 * Get Value of Type CheckList
	 * 
	 *  @param context the eMatrix <code>Context</code> object
	 *  @param args
	 *  @return String  
	 *  @throws Exception  if the operation fails
	 */
	public String getTypeCheckList(Context context, String[] args) throws MatrixException
	{    	   
		String strCheckList=ProgramCentralConstants.EMPTY_STRING;
		try{
			String languageStr = context.getSession().getLanguage();	     	 
			strCheckList = EnoviaResourceBundle.getProperty(context, "Framework", 
					"emxFramework.Type.Checklist", languageStr);
		}
		catch(Exception e){

			throw new MatrixException(e);
		}
		return strCheckList;
	}
	/**
	 * Get Value of Type CheckListItem 
	 * 
	 *  @param context the eMatrix <code>Context</code> object
	 *  @param args
	 *  @return String  
	 *  @throws Exception  if the operation fails
	 */
	public String getTypeCheckListItem(Context context, String[] args) throws MatrixException
	{    	   
		String strCheckListItem=ProgramCentralConstants.EMPTY_STRING;
		try{
			String languageStr = context.getSession().getLanguage();	     	 
			strCheckListItem = EnoviaResourceBundle.getProperty(context, "Framework", 
					"emxFramework.Type.Checklist_Item", languageStr);
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
		return strCheckListItem;
	}

	public StringList getName(Context context, String[] args) throws Exception{
		DomainObject item = DomainObject.newInstance(context);
		String templateType = item.TYPE_PROJECT_TEMPLATE;
		StringList nameList = new StringList();
		StringBuffer sbItemLink = new StringBuffer();
		String name = DomainObject.EMPTY_STRING;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Map paramMap = (Map)programMap.get("paramList");
		String editLink =(String)paramMap.get("editLink");
		Iterator objectListIterator = objectList.iterator();
		StringList objectIds = new StringList(objectList.size()); 
		while (objectListIterator.hasNext()){
			Map objectMap = (Map) objectListIterator.next();
			objectIds.add(objectMap.get(ProgramCentralConstants.SELECT_ID));
		}
		String[] arObjectIds = (String[]) objectIds.toArray(new String[objectIds.size()]);
			StringList busSelects = new StringList(2);
			busSelects.add(item.SELECT_NAME);
			busSelects.add(item.SELECT_TYPE);
		busSelects.add(item.SELECT_ID);
		MapList itemList = DomainObject.getInfo(context, arObjectIds, busSelects);
		for (Iterator iterator = itemList.iterator(); iterator.hasNext();) {
			Map itemInfo = (Map) iterator.next();
			String objectType = (String) itemInfo.get(item.SELECT_TYPE);
			String objectName = (String) itemInfo.get(item.SELECT_NAME);
			String objectId = (String) itemInfo.get(item.SELECT_ID);
			sbItemLink = new StringBuffer();
			if(TYPE_CHECKLIST_ITEM.equals(objectType)){
				name = objectName;
			}else if (TYPE_CHECKLIST.equals(objectType)){
				sbItemLink.append("<a href ='javascript:showModalDialog(\"");
				sbItemLink.append("../common/emxIndentedTable.jsp?table=PMCChecklistSummaryTable&amp;sortColumnName=Name,Type&amp;Export=true&amp;expandLevelFilter=true&amp;header=emxProgramCentral.Common.Checklist.ChecklistSummary&amp;suiteKey=ProgramCentral&amp;HelpMarker=emxhelpgatechecklist&amp;expandProgram=emxChecklist:expandChecklistSummaryTableObjects&amp;editLink="+editLink+"&amp;objectId=");
				sbItemLink.append(objectId);
				sbItemLink.append("\", \"875\", \"550\", \"false\", \"popup\")'>");
				sbItemLink.append(XSSUtil.encodeForXML(context, objectName));
				sbItemLink.append("</a>");
      			name = sbItemLink.toString();
			}else {
				sbItemLink.append("<a href ='javascript:showModalDialog(\"");
				sbItemLink.append("../common/emxTree.jsp?&amp;objectId=");
				sbItemLink.append(objectId);
				sbItemLink.append("\", \"875\", \"550\", \"false\", \"popup\")'>");
				sbItemLink.append(XSSUtil.encodeForXML(context, objectName));
				sbItemLink.append("</a>");
      			name = sbItemLink.toString();
			}
			nameList.add(name);
		}
		return nameList;
	}


	public StringList getChecklistItemResponseType(Context context, String[] args)
			throws MatrixException{
		try{
			Map programMap =   (Map)JPO.unpackArgs(args);
			Map paramList      = (HashMap) programMap.get("paramList");
			MapList objectList = (MapList)programMap.get("objectList"); 
			String strType = "";
			String objectId = "";
			int size = objectList.size();
			String strAccess = "";
			StringList vecResponse = new StringList(size);
			String strResponseType = "";
			String strLanguage=context.getSession().getLanguage();
			StringList slResponseType = new StringList();
			StringList sli18ResponseType = new StringList();

			for(int itr = 1; ; itr++){
				try {
					strResponseType = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options");
					slResponseType.add(strResponseType);
					String strKey = "emxProgramCentral.Checklist.Item.ResponseType"+itr+".Options";
					sli18ResponseType.add(EnoviaResourceBundle.getProperty(context, "ProgramCentral", strKey, strLanguage));
				}
				catch(Exception exp) {
					break;
				}
			}

			for(int iterator = 0; iterator < objectList.size(); iterator++){
				Map map = (Map)objectList.get(iterator);
				objectId = (String)map.get(DomainConstants.SELECT_ID);
				DomainObject domObject = DomainObject.newInstance(context, objectId);
				if(domObject.isKindOf(context, TYPE_CHECKLIST_ITEM)){       
					strResponseType =  domObject.getAttributeValue(context, ATTRIBUTE_RESPONSE_TYPE);
					strResponseType = strResponseType.replaceAll("_", " ");

					for(int index=0; index<slResponseType.size(); index++){
						if(ProgramCentralUtil.isNotNullString(strResponseType) 
								&& strResponseType.equalsIgnoreCase((String)slResponseType.get(index)))
						{
							String stri18ResponseType = (String)sli18ResponseType.get(index);
							stri18ResponseType = stri18ResponseType.replaceAll("_", " ");
							vecResponse.add(stri18ResponseType);
							break;
						}
					}
				}
				else{
					vecResponse.add("");
				}
			}
			return  vecResponse;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * Updates the name of the CheckList and CheckList Item 
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args Request arguments
	 * @throws MatrixException if operations fails.
	 */
	public void updateCheckListName(Context context, String[] args) throws MatrixException {
		try {
			CheckList checkList = new CheckList();
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			checkList.setId(objectId);
			String checkListName = (String) paramMap.get("New Value");
			checkList.setName(context, checkListName);
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}

}

