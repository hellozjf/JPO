/*
 *  emxProgramBusinessGoalBase.java
 *
 * Copyright (c) 2003-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.19.2.2 Thu Dec  4 07:55:08 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.19.2.1 Thu Dec  4 01:53:15 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.19 Wed Oct 22 15:49:13 2008 przemek Experimental przemek $
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.BusinessUnit;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.program.BusinessGoal;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

/**
 * @version PMC 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxProgramBusinessGoalBase_mxJPO extends emxDomainObject_mxJPO
{
	/** state "Complete" for the "Business Goals" policy. */
	public static final String STATE_BUSINESS_GOALS_COMPLETE =
		PropertyUtil.getSchemaProperty("policy",
				POLICY_BUSINESS_GOAL,
		"state_Complete");

	/**
	 *
	 * Constructs a new emxProgramBusinessGoalBase JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.0.0
	 */
	public emxProgramBusinessGoalBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		super(context, args);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return int 0 for success and non-zero for failure
	 * @throws Exception if the operation fails
	 * @since PMC 10.0.0.0
	 */
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
	}

	/****************************************************************************************************
	 *       Methods for Config Table Conversion Task
	 ****************************************************************************************************/
	/**
	 * This method gets the list of Business goal objects owned by the
	 *                      user that are not complete.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the id of BusinessGoals objects of the logged in user
	 * @throws Exception if the operation fails
	 * @since PMC 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMyBusinessGoals(Context context, String[] args)
	throws Exception
	{
		try
		{
			DomainObject dmObj = DomainObject.newInstance(context);

			String ownerExpression = "(owner ~= \"" + context.getUser() + "\")";
			String stateExpression = "(current != \"" + STATE_BUSINESS_GOALS_COMPLETE + "\")";
			MapList businessGoalList = new MapList();
			// Add selectables
			StringList busSelects = new StringList(1);
			busSelects.add(BusinessGoal.SELECT_ID);
			//need to have a filter
			//this is the filter for displaying user's BusinessGoal objects

			String whereExpression = ownerExpression + " && " + stateExpression;
			businessGoalList = DomainObject.findObjects(context,
					BusinessGoal.TYPE_BUSINESS_GOAL,
					null,
					whereExpression,
					busSelects);


			return businessGoalList;
		}
		catch (Exception ex)
		{
			throw ex;
		}

	}


	/**
	 * This method gets the list of Business goal objects
	 * associated to the users Business Unit that are not complete.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the id of BusinessGoals objects
	 * @throws Exception if the operation fails
	 * @since PMC 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getBusinessUnitBusinessGoals(Context context, String[] args)
	throws Exception
	{
		try
		{
			MapList businessGoalList = new MapList();
			DomainObject dmObj = DomainObject.newInstance(context);

			// Add selectables
			StringList busSelects = new StringList(1);
			busSelects.add(BusinessGoal.SELECT_ID);
			Person person = (Person) DomainObject.newInstance(context,
					DomainConstants.TYPE_PERSON);
			BusinessUnit businessUnit  = (BusinessUnit) DomainObject.newInstance(context,
					DomainConstants.TYPE_BUSINESS_UNIT);

			String personId = Person.getPerson(context).getId();
			person.setId(personId);

			StringList busSels = new StringList();
			busSels.add(DomainObject.SELECT_ID);

			//
			// Change made for IR-020987V6R2011
			// For Create New user in BU, the person is connected to BU with Business Unit Employee and Member
			// relationship
			// For Add Existing in BU, the person is connected to BU with only Member relationship
			// Hence the Member relationship is used to find out person's BU 
			//

			//get the Business Unit where the user belongs to
			//            Map buMap = person.getRelatedObject(context,
			//                                                DomainConstants.RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE,
			//                                                false,
			//                                                busSels,
			//                                                null);
			//            
			//            if(buMap == null)
			//            {
			//                return businessGoalList;
			//            }

			MapList mlMyBUs = person.getRelatedObjects(context,
					DomainConstants.RELATIONSHIP_MEMBER,
					DomainConstants.TYPE_BUSINESS_UNIT,
					busSels,
					null,
					true,
					false,
					(short)1,
					EMPTY_STRING,
					EMPTY_STRING,
					0);
			String busWhere = "(current != \"" + STATE_BUSINESS_GOALS_COMPLETE + "\")";

			for (Iterator itrMyBUs = mlMyBUs.iterator(); itrMyBUs.hasNext();) {
				Map mapMyBUInfo = (Map) itrMyBUs.next();

				businessUnit.setId((String) mapMyBUInfo.get(DomainObject.SELECT_ID));

				MapList mlBusinessGoalPartialList = businessUnit.getRelatedObjects(context,
						BusinessGoal.RELATIONSHIP_ORGANIZATION_GOAL,
						BusinessGoal.TYPE_BUSINESS_GOAL,
						busSelects,
						null,
						false,
						true,
						(short)1,
						EMPTY_STRING,
						null, 0);

				businessGoalList.addAll(mlBusinessGoalPartialList);
			}

			return businessGoalList;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * This method gets the list of Company's Business Goal objects that
	 * are not complete.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the id of BusinessGoals objects
	 * @throws Exception if the operation fails
	 * @since PMC 10.0.0.0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCompanyBusinessGoals(Context context, String[] args)
	throws Exception
	{
		try
		{
			MapList businessGoalList = new MapList();
			Person person   = (Person) DomainObject.newInstance(context,
					DomainConstants.TYPE_PERSON);
			Company company = (Company) DomainObject.newInstance(context,
					DomainConstants.TYPE_COMPANY);
			DomainObject dmObj = DomainObject.newInstance(context);

			// Add selectables
			StringList busSelects = new StringList(1);
			busSelects.add(BusinessGoal.SELECT_ID);

			String personId = Person.getPerson(context).getId();
			person.setId(personId);

			String companyId = person.getCompanyId(context);
			company.setId(companyId);

			String busWhere = "(current != \"" +
			STATE_BUSINESS_GOALS_COMPLETE + "\")";

			businessGoalList =  company.getRelatedObjects(context,
					BusinessGoal.RELATIONSHIP_ORGANIZATION_GOAL,
					BusinessGoal.TYPE_BUSINESS_GOAL,
					busSelects,
					null,
					false,
					true,
					(short)1,
					busWhere,
					EMPTY_STRING);

			return businessGoalList;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * This function gets the description to be shown in the column of
	 * the Business Goal Summary table.if description is over 30 characters
	 * long then truncates and adds an icon that displays the whole
	 * description as a tool tip.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectList - MapList containing the object Maps
	 * @return Vector containing the description value as String
	 * @throws Exception if the operation fails
	 * @since AEF Rossini Patch 1.0
	 */
	public Vector getDescription(Context context, String[] args)
	throws Exception
	{
		Vector displayDescription = new Vector();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			//added for the bug 326663
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String reportFormat = (String) paramMap.get("reportFormat");
			//till here
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			Map objectMap = null;

			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] =
					(String) objectMap.get(DomainObject.SELECT_ID);
				arrayCount++;
			}
			StringList busSelect = new StringList(1);
			busSelect.add(BusinessGoal.SELECT_DESCRIPTION);
			MapList actionList = DomainObject.getInfo(context, objIdArr, busSelect);
			int actionListSize = 0;

			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			String display = "";
			for (int i = 0; i < actionListSize; i++)
			{
				objectMap = (Map) actionList.get(i);
				String comments = XSSUtil.encodeForHTML(context,(String) objectMap.get(BusinessGoal.SELECT_DESCRIPTION));
				if(comments.length() > 30)
				{
					//added for the bug 326663
					if(reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat)){
						display=comments;
					}else{
						// XSS encoding removed because it is breaking in french and some other
						display =comments;
					}
				}
				else
				{
					display = comments;
				}//ends else

				StringBuffer sBuff = new StringBuffer();
				sBuff.append("<p title=\""+display+"\">");
                                sBuff.append(display);
                                sBuff.append("</p>");
				displayDescription.add(sBuff.toString());

			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return displayDescription;
		}
	}

	/**
	 * The method getBusinessBenefit gets the business benefit to be
	 * shown in the column of the Business Goal Summary table
	 * if business benefit is over 30 characters long then truncates
	 * and adds an icon that business benefit the whole description
	 * as a tool tip.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectList - MapList containing the object Maps
	 * @return Vector containing the business benefit value as String
	 * @throws Exception if the operation fails
	 * @since AEF Rossini Patch 1.0
	 */
	public Vector getBusinessBenefit(Context context, String[] args)
	throws Exception
	{
		Vector displayBusinessBenefit = new Vector();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			//added for the bug 326663
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String reportFormat = (String) paramMap.get("reportFormat");
			//till here
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			Map objectMap = null;
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] =
					(String) objectMap.get(DomainObject.SELECT_ID);
				arrayCount++;
			}
			StringList busSelect = new StringList(1);
			busSelect.add(BusinessGoal.SELECT_BUSINESS_BENEFIT);

			MapList actionList =
				DomainObject.getInfo(context, objIdArr, busSelect);
			int actionListSize = 0;

			if (actionList != null)
			{
				actionListSize = actionList.size();
			}
			String display = "";

			for (int i = 0; i < actionListSize; i++)
			{
				objectMap = (Map) actionList.get(i);
				String benefit = (String) objectMap.get(BusinessGoal.SELECT_BUSINESS_BENEFIT);

				if(benefit.length() > 30)
				{
					//added for the bug 326663
					if(reportFormat!=null && "CSV".equalsIgnoreCase(reportFormat)){
						display=benefit;
					}else{
						// XSS encoding removed because it is breaking in french and some other
						display =benefit;					
					}
				}
				else
				{
					// XSS encoding removed because it is breaking in french and some other
					display = benefit;
				}//ends else

				displayBusinessBenefit.add(display);

			}//ends while
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return displayBusinessBenefit;
		}
	}

	/**
	 * This method displays the owner with lastname,firstname format
	 * also has a link to open a pop up with the owner
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectList - MapList containing the object Maps
	 *        paramList - MapList containing the parameters
	 *        SuiteDirectory - a String giving the name of Suite Directory
	 * @return Vector containing the owner value as String
	 * @throws Exception if the operation fails
	 * @since AEF Rossini Patch1.0
	 */
	public Vector showOwner(Context context, String[] args)
	throws Exception
	{
		Vector vecOwner = new Vector();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");

			HashMap paramList = (HashMap)programMap.get("paramList");
			String suiteDirectory = (String)paramList.get("SuiteDirectory");

			String publicPortal = (String)programMap.get("publicPortal");
			String portalMode = (String) paramList.get("portalMode");
			boolean isPrinterFriendly = false;
			String strPrinterFriendly = (String)paramList.get("reportFormat");
			if ( strPrinterFriendly != null ) {
				isPrinterFriendly = true;
			}

			String targetTree = "emxTree.jsp";
			if(publicPortal != null && "true".equalsIgnoreCase(publicPortal))
			{
				targetTree = "emxNavigator.jsp";
			}

			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			Map objectMap = null;
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] =
					(String) objectMap.get(DomainObject.SELECT_ID);
				arrayCount++;
			}
			StringList busSelect = new StringList(1);
			busSelect.add(BusinessGoal.SELECT_OWNER);

			MapList actionList =
				DomainObject.getInfo(context, objIdArr, busSelect);
			int actionListSize = 0;

			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			for (int i = 0; i < actionListSize; i++)
			{
				objectMap = (Map) actionList.get(i);
				String displayOwner = "";

				String user = (String)objectMap.get(BusinessGoal.SELECT_OWNER);
				String strOwner = Person.getDisplayName(context, user);
				Person tempPerson = Person.getPerson(context, user);
				String ownerId = tempPerson.getInfo(context, Person.SELECT_ID);
				String personURL = targetTree + "?objectId="+XSSUtil.encodeForURL(context,ownerId)+"&amp;mode=replace&amp;jsTreeID="+null+"&amp;AppendParameters=false&amp;emxSuiteDirectory="+suiteDirectory;
				if(!isPrinterFriendly) {
					displayOwner = "<a href='"+personURL+ "'>"+ XSSUtil.encodeForHTML(context,strOwner)+"</a>";
				} else {
					displayOwner =XSSUtil.encodeForHTML(context, strOwner);
				}
				vecOwner.add(displayOwner);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return vecOwner;
		}
	}

	/**
	 * This method displays the edit icon with tool tip
	 * also has a link to open a pop up with the business goal
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectList - MapList containing the object Maps
	 *        paramList - MapList containing the parameters
	 *        SuiteDirectory - a String giving the name of Suite Directory
	 * @return Vector containing the edit icon link value as String
	 * @throws Exception if the operation fails
	 * @since AEF Rossini Patch1.0
	 */
	public Vector editNewWindow(Context context, String[] args)
	throws Exception
	{
		Vector vecEditWindow = new Vector();
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");

			HashMap paramList = (HashMap)programMap.get("paramList");
			String suiteDirectory = (String)paramList.get("SuiteDirectory");

			String publicPortal = (String)programMap.get("publicPortal");
			boolean isPrinterFriendly = false;
			String strPrinterFriendly = (String)paramList.get("reportFormat");
			if ( strPrinterFriendly != null ) {
				isPrinterFriendly = true;
			}

			String targetTree = "emxTree.jsp";
			if(publicPortal != null && "true".equalsIgnoreCase(publicPortal))
			{
				targetTree = "emxNavigator.jsp";
			}

			Iterator objectListItr = objectList.iterator();

			while(objectListItr.hasNext())
			{
				Map objectMap = (Map) objectListItr.next();
				String businessGoalId = (String) objectMap.get(BusinessGoal.SELECT_ID);
				String URL = targetTree + "?objectId="+businessGoalId+"&amp;mode=replace&amp;jsTreeID="+null+"&amp;AppendParameters=false&amp;emxSuiteDirectory="+suiteDirectory;
				String display = "";
				if(!isPrinterFriendly) {
					display= "<a href=\"javascript:showDetailsPopup('"+URL+"')\"><img src='../common/images/iconNewWindow.gif' border='0' alt='"
					+ "New Window" + "' align='absbottom'/></a>";
				} else {
					display = "<img src='../common/images/iconNewWindow.gif' border='0' alt='"
						+ "New Window" + "' align='absbottom'/>";
				}
				vecEditWindow.add(display);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return vecEditWindow;
		}
	}

	/**
	 * This method indicates whether the 'Add To Folder' link should be displayed or not
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectId - String object id
	 * @return boolean indicating whether or not to display the command
	 * @throws Exception if the operation fails
	 * @since AEF 10.5
	 */
	//H1A:Merged with Access Expression @PMCContentAddToFolderActionLink command
	/*public boolean showCommand(Context context, String[] args) throws Exception{
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");

			DomainObject doObj = DomainObject.newInstance(context, strObjectId);
			String strCurrentState = doObj.getInfo(context, DomainConstants.SELECT_CURRENT);
			if(TYPE_BUSINESS_GOAL.equals(doObj.getType(context)) || strCurrentState.equals(DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED))
			{
				return false;
			}

		}catch (Exception ex){
			throw ex;
		}
		return true;
	}*/

	/**
	 * This method gets the list of Business goal objects owned by the
	 *  user that are not complete.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return MapList containing the id of BusinessGoals objects of the logged in user
	 * @throws Exception if the operation fails
	 * @since PMC 10.0.0.0
	 */
	public MapList getProjectBusinessGoals(Context context, String[] args)
	throws Exception
	{
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			com.matrixone.apps.program.ProjectSpace projectSpace =
				(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
						DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			projectSpace.setId(objectId);

			// Add selectables
			StringList busSelects = new StringList(1);
			busSelects.add(BusinessGoal.SELECT_ID);

			return projectSpace.getRelatedObjects(
					context,               // context
					BusinessGoal.RELATIONSHIP_BUSINESS_GOAL_PROJECT_SPACE,  // relationship pattern
					BusinessGoal.QUERY_WILDCARD,        // type filter
					busSelects,            // business object selectables
					null,                  // relationship selectables
					true,                 // expand to direction
					false,                  // expand from direction
					(short)1,              // level
					null,                   // object where clause
					EMPTY_STRING);         // relationship where clause
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
	/**
	 * gets the value for field Parent Goal on View Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "objectId".
	 * @return - String to display Parent Goal
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getParentGoal(Context context, String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String reportFormat=(String) requestMap.get("reportFormat");   
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		String objectId = (String) paramMap.get("objectId");
		businessGoal.setId(objectId);
		StringList busSelects = new StringList(1);
		busSelects.add(businessGoal.SELECT_NAME);
		String objectName = null;
		String altDisplay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.ProgramTop.BusinessGoals", context.getSession().getLanguage());
		MapList parentMapList = (MapList)businessGoal.getParentInfo(context, 1, busSelects);
		if (parentMapList.size() > 0){
			Iterator parentMapListItr = parentMapList.iterator();
			while(parentMapListItr.hasNext()) {
				Map parentGoalMap = (Map) parentMapListItr.next();
				objectName = (String) parentGoalMap.get(businessGoal.SELECT_NAME);
			}
		}
		else if((parentMapList.size() <= 0))
		{
			objectName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.BusinessGoal.TopLevel", context.getSession().getLanguage());
		}

		if("CSV".equalsIgnoreCase(reportFormat)){
			return objectName;
		}
		else{
			StringBuffer sb = new StringBuffer();
			sb.append("<img src=\"../common/images/iconSmallCorpObjective.gif\" border=\"0\" alt=\""+altDisplay+"\"/>");
			sb.append(" "+objectName);
			objectName = sb.toString();
			return objectName;
		}
		//End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x
	}



	/**
	 * gets the value for field Parent Goal on View Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return - String to display Parent Goal
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void editPostProcessActions(Context context, String args[]) throws Exception
	{
	
			com.matrixone.apps.program.BusinessGoal businessGoal =
				(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
	

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objectId = (String)paramMap.get("objectId");
			String businessUnitId= (String)requestMap.get("BusinessUnitEditModeOID");
			
			String programId= (String)requestMap.get("ProgramId");
			businessGoal.startTransaction(context, true);
			businessGoal.setId(objectId);
		
			String personName = (String) requestMap.get("PersonNameEditMode");
		
	                businessGoal.setOwner(context,personName);

		if(ProgramCentralUtil.isNotNullString(businessUnitId) && !businessUnitId.isEmpty()) {
			  businessGoal.changeOrganization(context, businessUnitId);
			}	
	}



	/**
	 * Cheks whether a user has access to Delete Command
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap
	 * @return - boolean value
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public boolean checkDeleteAccess(Context context,String args[]) throws Exception
	{
		boolean hasDeleteAccess = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("parentOID");
		String policyName = PropertyUtil.getSchemaProperty(context,"policy_BusinessGoal");
		String createdStateName = PropertyUtil.getSchemaProperty(context,"policy",policyName,"state_Created");
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		businessGoal.setId(objectId);
		String state = (String) businessGoal.getInfo(context, businessGoal.SELECT_CURRENT);
		if(null != state && null != createdStateName && state.equals(createdStateName))
		{
			hasDeleteAccess = true;
		}
		return hasDeleteAccess;
	}

	/**
	 * Cheks whether a user has access to Edit Command
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap
	 * @return - boolean value
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	/*public boolean checkEditAccess(Context context,String args[]) throws Exception
	{
		boolean hasEditAccess = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("parentOID");
		String policyName = PropertyUtil.getSchemaProperty(context,"policy_BusinessGoal");
		String completeStateName = PropertyUtil.getSchemaProperty(context,"policy",policyName,"state_Complete");
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		businessGoal.setId(objectId);
		String state = (String) businessGoal.getInfo(context, businessGoal.SELECT_CURRENT);
		if(null != state && null != completeStateName && !state.equals(completeStateName))
		{
			hasEditAccess = true;
		}
		return hasEditAccess;
	}*/

	/**
	 * gets the value for field Business Unit on View Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Business Unit on View Form
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getBusinessUnit(Context context, String args[]) throws Exception
	{
		String output = "";
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String reportFormat=(String) requestMap.get("reportFormat");          
		String strMode = (String) requestMap.get("mode");
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		businessGoal.setId(objectId);
		StringList busSelects = new StringList(2);
		busSelects.add(businessGoal.SELECT_ORGANIZATION_NAME);
		busSelects.add(businessGoal.SELECT_ORGANIZATION_ID);
		Map businessGoalMap = businessGoal.getInfo(context, busSelects);
		String organizationId = (String) businessGoalMap.get(businessGoal.SELECT_ORGANIZATION_ID);
		String organizationType = null;
		if(null != organizationId && !"".equals(organizationId)){
			DomainObject domainObject = DomainObject.newInstance(context, organizationId);
			organizationType = domainObject.getInfo(context, domainObject.SELECT_TYPE);
		}
		String organizationName = (String)businessGoalMap.get(businessGoal.SELECT_ORGANIZATION_NAME);
		StringBuffer sb = new StringBuffer();
		if( strMode != null )
		{
			if(organizationType.equals(DomainConstants.TYPE_BUSINESS_UNIT))
			{
				String businessUnit = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.BusinessUnit", context.getSession().getLanguage());
				sb.append("<img src=\"../common/images/iconSmallBusinessUnit.gif\" border=\"0\" alt=\""+XSSUtil.encodeForHTML(context,businessUnit)+"\"/>");
			}
			if(organizationType.equals(DomainConstants.TYPE_COMPANY))
			{
				String company = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.Company", context.getSession().getLanguage());			
				sb.append("<img src=\"../common/images/iconSmallCompany.gif\" border=\"0\" alt=\""+company+"\"/>");
			}
			sb.append(organizationName);
			output = sb.toString();

		}
          	else
		{
			if("CSV".equalsIgnoreCase(reportFormat))
			{
				output=organizationName;
			}
		}
		//End:Modified:30-August-2010:s4e:R210 PRG:IR-067229V6R2011x

		return output;
	}
	/**
	 * gets the value for field Business Unit on View Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Business Unit on View Form
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getBusinessUnitModeEdit(Context context, String args[]) throws Exception
	{
		String output = "";
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String reportFormat=(String) requestMap.get("reportFormat");          
		String strMode = (String) requestMap.get("mode");
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		businessGoal.setId(objectId);
		StringList busSelects = new StringList(2);
		busSelects.add(businessGoal.SELECT_ORGANIZATION_NAME);
		busSelects.add(businessGoal.SELECT_ORGANIZATION_ID);
		Map businessGoalMap = businessGoal.getInfo(context, busSelects);
		String organizationName = (String)businessGoalMap.get(businessGoal.SELECT_ORGANIZATION_NAME);
		StringBuffer sb = new StringBuffer();

		if(strMode != null && strMode.equals("edit"))
		{
			sb.append(organizationName);
			output = sb.toString();
		}
		else
		{
			if("CSV".equalsIgnoreCase(reportFormat))
			{
				output=organizationName;
			}
		}
			return output;
	}



	/**
	 * gets the value for field Owner on View Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Owner on View Form
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getOwner(Context context, String args[]) throws Exception
	{
		String output = "";
		boolean isPrinterFriendly = false;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String printerFriendly = (String) requestMap.get("PFmode");
		String reportFormat=(String) requestMap.get("reportFormat");       
		if(printerFriendly!=null)
			isPrinterFriendly = true;
		String strMode = (String) requestMap.get("mode");
		String jsTreeID = (String) requestMap.get("jsTreeID");
		com.matrixone.apps.common.Person person =
			(com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		businessGoal.setId(objectId);
		StringList busSelects = new StringList(2);
		busSelects.add(businessGoal.SELECT_OWNER);
		Map businessGoalMap = businessGoal.getInfo(context, busSelects);
		String businessGoalOwner = (String) businessGoalMap.get(businessGoal.SELECT_OWNER);
		String ownerId = person.getPerson(context, businessGoalOwner).getId();
		StringBuffer sb = new StringBuffer();
		String personURL = "emxTree.jsp?objectId="+ownerId;
		personURL += "&mode=replace&jsTreeID="+jsTreeID+"&AppendParameters=false";
		if(strMode != null) 
		{
			String ownerAlt = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Owner", context.getSession().getLanguage());
			if(!isPrinterFriendly)
			{
				sb.append("<a href='").append(personURL).append("' >");
				sb.append("<img src=\"../common/images/iconSmallPerson.gif\" border=\"0\" alt=\""+XSSUtil.encodeForHTML(context,ownerAlt)+"\"/>");
				sb.append(businessGoalOwner);
				sb.append("</a>");
				output = sb.toString();
			}
			else
			{
				sb.append("<img src=\"../common/images/iconSmallPerson.gif\" border=\"0\" alt=\""+XSSUtil.encodeForHTML(context,ownerAlt)+"\"/>");
				sb.append(businessGoalOwner);
				output = sb.toString();
			}

		}
		else
		{
			if("CSV".equalsIgnoreCase(reportFormat))
			{
				output=businessGoalOwner;
			}
		}
	return output;
	}

	/**
	 * gets the value for field Owner in Edit mode of Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Owner on View Form
	 * @throws Exception
	 *             if operation fails
	 *
	 */
	public String getOwnerModeEdit(Context context, String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");		
	
		
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
	
		businessGoal.setId(objectId);
		
		String personName = businessGoal.getInfo(context, businessGoal.SELECT_OWNER);
		
		return personName;
	}

	/**
	 * Controls Access for not displaying certain fields on Edit Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Business Unit on View Form
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public boolean checkFieldAccess(Context context,String args[]) throws Exception
	{
		boolean showFields= true;
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String mode = (String) programMap.get("mode");
		if(mode!= null && mode.equals("edit"))
		{
			showFields = false;
		}
		return showFields;
	}

	/**
	 * Creates a new Business Goal
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - void
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public void createBusinessGoal(Context context, String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		BusinessGoal businessGoal = new BusinessGoal();
		String objectId = (String) requestMap.get("objectId");  
		String EffectiveDate = (String) requestMap.get("EffectiveDate");    
		String SuccessMeasurement = (String) requestMap.get("SuccessMeasurement");      
		String OperationalDefinition = (String) requestMap.get("OperationalDefinition");        
		String BusinessUnitName = (String) requestMap.get("BusinessUnit");      
		String GoalDescription = (String) requestMap.get("GoalDescription");        
		String BusinessBenefit = (String) requestMap.get("BusinessBenefit");
		String BusinessGoalName = (String) requestMap.get("BusinessGoalName");
		String Comments = (String) requestMap.get("Comments");
		String BusinessUnitId = (String) requestMap.get("BusinessUnitId");      
		String timeZone = (String) requestMap.get("timeZone");
		String parentId = (String) requestMap.get("parentId");  
		String defaultVault =(String) requestMap.get("defaultVault");
		double clientTZOffset = new Double(timeZone).doubleValue();
		EffectiveDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context,EffectiveDate, clientTZOffset,Locale.getDefault());
		HashMap attributeMap = new HashMap();
		attributeMap.put(businessGoal.ATTRIBUTE_EFFECTIVE_DATE, EffectiveDate);
		attributeMap.put(businessGoal.ATTRIBUTE_BUSINESS_BENEFIT, BusinessBenefit);
		if(null != OperationalDefinition && !"".equals(OperationalDefinition)){
			attributeMap.put(businessGoal.ATTRIBUTE_OPERATIONAL_DEFINITION, OperationalDefinition);
		}
		if(null != SuccessMeasurement && !"".equals(SuccessMeasurement)){
			attributeMap.put(businessGoal.ATTRIBUTE_MEASURE_OF_SUCCESS, SuccessMeasurement);
		}
		if(null != Comments && !"".equals(Comments)){
			attributeMap.put(businessGoal.ATTRIBUTE_COMMENTS, Comments);
		}
		String selectedPolicy = businessGoal.getDefaultPolicy(context);
		businessGoal.startTransaction(context, true);
		businessGoal.create(context, null, BusinessGoalName, selectedPolicy, defaultVault, attributeMap, BusinessUnitId, parentId);
		String objectType = "";
		if(null != objectId && !"".equals(objectId)){
			DomainObject domainObject = DomainObject.newInstance(context, objectId);
			objectType = domainObject.getInfo(context, domainObject.SELECT_TYPE);
		}
		if(null != objectId && !"".equals(objectId) && null != objectType){
			if(mxType.isOfParentType(context,objectType,DomainConstants.TYPE_PROJECT_SPACE)){
				String[] projectIds = {objectId};
				businessGoal.addProjects(context, projectIds);
			} // ends if
		} // ends if
		businessGoal.setState(context, "Created");
		businessGoal.setDescription(context, GoalDescription);
		ContextUtil.commitTransaction(context);

	}

	/**
	 * gets a list of Vaults
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - StringList containing list of Vaults
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	static public StringList GetAllVaults(Context context,String args[])
	throws Exception
	{
		StringList vaultList = new StringList();

		com.matrixone.apps.common.Person person =
			com.matrixone.apps.common.Person.getPerson(context);
		Company company = person.getCompany(context);

		StringList selectList = new StringList(2);
		selectList.add(company.SELECT_VAULT);
		selectList.add(company.SELECT_SECONDARY_VAULTS);
		Map companyMap = company.getInfo(context,selectList);
		StringList secVaultList = FrameworkUtil.split((String)companyMap.get(company.SELECT_SECONDARY_VAULTS),null);
		Iterator itr = secVaultList.iterator();

		String vaults = (String)companyMap.get(company.SELECT_VAULT);
		vaultList.add(vaults);
		while (itr.hasNext() )
		{
			vaultList.add(PropertyUtil.getSchemaProperty(context, (String)itr.next()));
		}
		return vaultList;
	}

	/**
	 * Checks whether Vault field has to be displayed on Create Business Goal
	 * Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Business Unit on View Form
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public boolean displayVaultList(Context context, String args[]) throws Exception
	{
		com.matrixone.apps.common.Person person =
			(com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		String personId = person.getPerson(context).getId();
		person.setId(personId);
		StringList vaultsList = new StringList();
		String vaults = person.getSearchDefaultVaults(context);
		StringList vaultSplit = FrameworkUtil.split(vaults, ",");
		Iterator vaultItr = vaultSplit.iterator();
		while (vaultItr.hasNext()){
			vaultsList.add(((String) vaultItr.next()).trim());
		}   
		if(vaultsList.isEmpty()) {
			vaultsList = GetAllVaults(context,args);
		}
		if(vaultsList.size() > 1)
			return true;
		else
			return false;
	}

	/**
	 * gets the value for field Vault on Create Business Goal Form
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: programMap -
	 *            a HashMap containing the following keys, "paramMap"
	 *            "requestMap".
	 * @return - String to display Vault on Create Form
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getVault(Context context,String args[]) throws Exception
	{
		StringBuffer vault = new StringBuffer();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String sLanguage = (String) requestMap.get("languageStr");
		com.matrixone.apps.common.Person person =
			(com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		String personId = person.getPerson(context).getId();
		person.setId(personId);
		String userVault = person.getVault();
		userVault = i18nNow.getMXI18NString(userVault,"",sLanguage,"Vault");
		StringList vaultsList = new StringList();
		String vaults = person.getSearchDefaultVaults(context);
		StringList vaultSplit = FrameworkUtil.split(vaults, ",");
		Iterator vaultItr = vaultSplit.iterator();
		while (vaultItr.hasNext()){
			vaultsList.add(((String) vaultItr.next()).trim());
		}
		if(vaultsList.isEmpty()) {
			vaultsList = GetAllVaults(context,args);
		}
		vaultsList.sort();
		StringList i18NVaults = new StringList();
		String i18nVault = "";
		String remLeadSpace = "";
		Iterator vaultNewItr = vaultsList.iterator();
		while(vaultNewItr.hasNext()) {
			remLeadSpace = (String)vaultNewItr.next();
			remLeadSpace = remLeadSpace.trim();
			i18nVault  = i18nNow.getMXI18NString(remLeadSpace,"",sLanguage,"Vault");
			i18NVaults.add(i18nVault);
		}
		vault.append("<select name=\"defaultVault\">");
		int size = vaultsList.size();
		for(int i=0;i<size;i++)
		{
			String option = (String)i18NVaults.get(i);
			String value = (String) vaultsList.get(i);
			if(option.equals(userVault))
			{
				vault.append("<option value=\""+option+"\" selected>");
			}
			else
			{
				vault.append("<option value=\""+option+"\" selected>");
			}
			vault.append(value);
			vault.append("</option>");
		}
		vault.append("</select>");
		String output = vault.toString();
		return output;
	}   



	/**
	 * returns true if more than one vault is present.    
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return boolean
	 * @throws MatrixException
	 */
	public boolean checkFieldAccessVault(Context context,String args[]) throws MatrixException
	{
		Map mpBusinessGoal;
		try {
			mpBusinessGoal = getBuisinessGoalVaultRangeValues(context,args);

			StringList  vualtCount =  (StringList) mpBusinessGoal.get("field_choices");
			int size = vualtCount.size();
			boolean showFields= false;
			if(size>1){
				showFields = true;    		
			}
			return showFields;
		}catch(Exception e){
			throw (new MatrixException());
		}
	}

	/**
	 * returns true if Business Goal is connected to a Project else false.    
	 * @param context eMatrix <code>Context</code> object
	 * @param args
	 * @return boolean
	 * @throws MatrixException
	 */
	
	public boolean displaySubGoalCreateCommand(Context context,String args[]) throws MatrixException
	{
		boolean showCommand= true;
		HashMap programMap;
		try {
			programMap = (HashMap) JPO.unpackArgs(args);

			String strBusinessGoalId = (String) programMap.get("objectId");

			DomainObject domBusinessGoal = DomainObject.newInstance(context,strBusinessGoalId);
			StringList slSelectable= new StringList();
			slSelectable.addElement("from["+BusinessGoal.RELATIONSHIP_BUSINESS_GOAL_PROJECT_SPACE+"].to.id");
			Map mpBusinessGoalInfo =  domBusinessGoal.getInfo(context,slSelectable);
			String strProjectSpaceId = (String)mpBusinessGoalInfo.get("from["+BusinessGoal.RELATIONSHIP_BUSINESS_GOAL_PROJECT_SPACE+"].to.id");

			if(ProgramCentralUtil.isNotNullString(strProjectSpaceId)){
				DomainObject domProjectSpace = DomainObject.newInstance(context,strProjectSpaceId);
				if(domProjectSpace.isKindOf(context, DomainObject.TYPE_PROJECT_SPACE)){
					showCommand = false;
				}
			}
			return showCommand;
		}catch(Exception e){
			throw (new MatrixException());
		}
	}

	/**
	 * returns true if SubGoal is not present in Business Goal
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return boolean
	 * @throws MatrixException
	 */

	public boolean hasAccessToAddProjectCommand(Context context,String args[]) throws Exception
	{
		boolean hasAccess = true;
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);   
			String strGoalId = (String) programMap.get("objectId");

			DomainObject domBusinessGoal = DomainObject.newInstance(context,strGoalId);
			StringList slSelectable= new StringList();
			slSelectable.addElement(BusinessGoal.SELECT_HAS_SUBGOAL);
			Map mpBusinessGoalInfo =  domBusinessGoal.getInfo(context,slSelectable);
			if(mpBusinessGoalInfo !=null){
				String hasGoal = (String)mpBusinessGoalInfo.get(BusinessGoal.SELECT_HAS_SUBGOAL);
				if(hasGoal.equalsIgnoreCase("true")){
					hasAccess = false;
				}
			}
			return hasAccess;

		}catch(Exception e){
			throw (new MatrixException());
		}
	}
	/**
	 * get all vaults for BusinessGoalCreateForm Vault Field
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return HashMap
	 * @throws MatrixException
	 */
	public HashMap getBuisinessGoalVaultRangeValues(Context context, String[] args)throws MatrixException
	{
		com.matrixone.apps.common.Person person =
			(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
					DomainConstants.TYPE_PERSON,DomainConstants.PROGRAM);
		try{
			String strPersonId = person.getPerson(context).getId(context);
			person.setId(strPersonId);
			StringList slVaultsList =new StringList();
			HashMap mpTemp = new HashMap();

			StringList slVaultSplit = FrameworkUtil.split(person.getSearchDefaultVaults(context), ",");
			if(!slVaultSplit.isEmpty()) {
				Iterator vaultItr = slVaultSplit.iterator();
				while (vaultItr.hasNext()){
					slVaultsList.add(((String) vaultItr.next()).trim());
				}
				if(slVaultsList.isEmpty()) {
					slVaultsList = GetAllVaults(context,args);
				}
				slVaultsList.sort();

				StringList fieldRangeValues = new StringList();
				StringList fieldDisplayRangeValues = new StringList();
				String strVaultName = "";
				vaultItr = slVaultsList.iterator();
				while(vaultItr.hasNext()) {
					strVaultName = (String)vaultItr.next();
					strVaultName = strVaultName.trim();  
					fieldRangeValues.addElement(strVaultName);
					fieldDisplayRangeValues.addElement(strVaultName);
				}    

				mpTemp.put("field_choices", fieldRangeValues);
				mpTemp.put("field_display_choices", fieldDisplayRangeValues);
			}
			return mpTemp;
		}catch(Exception e){
			throw (new MatrixException());
		}
	}


	/**
	 * get Parent Goal
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return String
	 * @throws MatrixException
	 */
	public String getParentGoals(Context context, String args[]) throws MatrixException
	{

		HashMap programMap;
		try {
			programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");        
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String objectId = (String) requestMap.get("objectId"); 

			if(objectId==null){
				objectId = (String) paramMap.get("objectId"); 
			}
			String strBusinessGoalName = null;       
			String strBusinessGoalType = null;

			if(objectId!=null && !objectId.equals("")){
				DomainObject domainObject = DomainObject.newInstance(context, objectId);
				StringList slSelectables = new StringList(2);
				slSelectables.add(DomainObject.SELECT_TYPE);
				slSelectables.add(DomainObject.SELECT_NAME);
				Map mpParentInfo = domainObject.getInfo(context, slSelectables);
				strBusinessGoalName = (String)mpParentInfo.get(DomainConstants.SELECT_NAME);
				strBusinessGoalType = (String)mpParentInfo.get(DomainConstants.SELECT_TYPE);
			}

			if((!DomainConstants.TYPE_PROJECT_SPACE.equals(strBusinessGoalType)&& (objectId!=null && !objectId.equals(""))))
				return strBusinessGoalName;
			else{
				strBusinessGoalName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.BusinessGoal.TopLevel", context.getSession().getLanguage());
				return (XSSUtil.encodeForHTML(context,strBusinessGoalName));
			}   
		}catch(Exception e){
			throw (new MatrixException());
		}
	}   

	/**
	 * get company list.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 
	 * @return MapList
	 * @throws MatrixException
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getCompany(Context context, String[] args) throws Exception
	{
		Person person = (Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
		String personId = person.getPerson(context).getId(context);
		person.setId(personId);
		String companyId = person.getCompanyId(context);
		MapList mlCompanies =new MapList();
		Map objectMap= new HashMap();
		objectMap.put(DomainObject.SELECT_ID, companyId);
		mlCompanies.add(objectMap);
		return mlCompanies;

	}

	/**
	 * exclude Business Goal which are already added
	 * @param context  the eMatrix <code>Context</code> object
	 * @param args
	 * @return StringList
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeBusinessGoalforAddExisting(Context context, String[] args)
	throws Exception
	{
		MapList mlBusinessGoalList = new MapList();
		StringList slBusinessGoal = new StringList();
		Map mpObjectMap= new HashMap();

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strProjectId=(String) programMap.get("parentOID"); 
			DomainObject dmObj = DomainObject.newInstance(context);	
			
			StringList slSelectables = new StringList(1);
			slSelectables.add(DomainObject.SELECT_ID);
			
			String busWhere =("current=='" + STATE_BUSINESS_GOALS_COMPLETE + "'");
				busWhere += (" || from["+ProgramCentralConstants.RELATIONSHIP_SUBGOAL+"].to.type =='" + DomainObject.TYPE_BUSINESS_GOAL + "'");
				busWhere += (" || from[Business Goal Project Space].to.id=='" + strProjectId + "'");		
				
			mlBusinessGoalList=	findObjects( context,
					DomainObject.TYPE_BUSINESS_GOAL,
					 QUERY_WILDCARD,
					 busWhere,
					slSelectables);
					
			for (int i = 0; i <  mlBusinessGoalList.size(); i++){
				mpObjectMap = (Map) mlBusinessGoalList.get(i);
				String strBusinessGoal = (String)mpObjectMap.get(DomainObject.SELECT_ID);
				slBusinessGoal.add(strBusinessGoal);
			}
		
		return slBusinessGoal;

	}

	/**
	 * get Business Goals of selected Project
	 * @param context  the eMatrix <code>Context</code> object
	 * @param args
	 * @return MapList
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectSpecificBusinessGoals(Context context, String[] args)
	throws MatrixException
	{
		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		com.matrixone.apps.program.ProjectSpace project =
			(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
		MapList mlBusinessGoal = new MapList();
		String strObjectType = null;

		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);   
			String strObjectId = (String) programMap.get("objectId");  
			String strParentId     = (String)programMap.get("parentOID");

			StringList slBusSelects=new StringList();
			slBusSelects.clear();
			slBusSelects.add(DomainObject.SELECT_ID);
			slBusSelects.add(DomainObject.SELECT_NAME);
			slBusSelects.add(DomainObject.SELECT_DESCRIPTION);
			slBusSelects.add(DomainObject.SELECT_CURRENT);
			slBusSelects.add(businessGoal.SELECT_COMMENTS);
			slBusSelects.add(businessGoal.SELECT_BUSINESS_BENEFIT);
			slBusSelects.add(businessGoal.SELECT_EFFECTIVE_DATE);
			slBusSelects.add(DomainObject.SELECT_OWNER);
			slBusSelects.add(businessGoal.SELECT_ORGANIZATION_NAME);
			slBusSelects.add(businessGoal.SELECT_ORGANIZATION_ID);
			slBusSelects.add(DomainObject.SELECT_POLICY);

			if(null != strObjectId && !"".equals(strObjectId)){
				DomainObject domainObject = DomainObject.newInstance(context, strObjectId);
				strObjectType = domainObject.getInfo(context, domainObject.SELECT_TYPE);
			}

			if(null != strObjectId && !"".equals(strObjectId) && null != strObjectType)
			{
				if(mxType.isOfParentType(context,strObjectType,DomainConstants.TYPE_PROJECT_SPACE))
				{
					project.setId(strObjectId); 
					mlBusinessGoal = businessGoal.getBusinessGoals(context, project, slBusSelects, null);
				}   
			}
			return mlBusinessGoal;
		}
		catch (Exception e) {
			throw (new MatrixException());
		}
	}

	/**
	 * gets Business Units of company
	 * @param context  the eMatrix <code>Context</code> object
	 * @param args
	 * @return MapList
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getBusinessUnitSpecifictoCompany(Context context, String[] args) throws MatrixException 
	{

		com.matrixone.apps.common.Company company = (com.matrixone.apps.common.Company) DomainObject.newInstance(context, DomainConstants.TYPE_COMPANY);
		Map programMap;
		BusinessUnit businessUnit  = (BusinessUnit) DomainObject.newInstance(context,DomainConstants.TYPE_BUSINESS_UNIT);
		MapList busUnitList= new MapList();

		try {
			programMap = (Map)JPO.unpackArgs(args);		
			String strCompanyId = (String)programMap.get("objectId");
			company.setId(strCompanyId);

			StringList busSelects =  new StringList();    	 
			busSelects.add(businessUnit.SELECT_ID);
			busSelects.add(businessUnit.SELECT_NAME);
			busSelects.add(SELECT_TYPE);
			busSelects.add(SELECT_CURRENT);

			busUnitList = company.getBusinessUnits(context, 0, busSelects,false);		           

		} catch (Exception e) {
			throw (new MatrixException());
		}
		return busUnitList;

	}


	/**
	 * Connect Business Goal to Company / Business Unit and Project.
	 * @param context  the eMatrix <code>Context</code> object
	 * @param args
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createAndConnectBusinessGoal(Context context, String args[]) throws MatrixException
	{
		BusinessGoal businessGoal = 
				(BusinessGoal)DomainObject.newInstance(context,
											DomainConstants.TYPE_BUSINESS_GOAL,
											DomainConstants.PROGRAM);
		
		businessGoal.createAndConnectBusinessGoal(context,args);
	}



	/**
	 * Connect SubGoal to Company / Business Unit and Business Goal.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createAndConnectSubGoal(Context context, String args[]) throws MatrixException
	{
		BusinessGoal businessGoal = 
				(BusinessGoal)DomainObject.newInstance(context,
											DomainConstants.TYPE_BUSINESS_GOAL,
											DomainConstants.PROGRAM);
		
		businessGoal.createAndConnectSubGoal(context,args);

	}
	/**
	 * get Project Summary.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return MapList
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectSummary(Context context, String[] args) throws MatrixException
	{

		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		com.matrixone.apps.program.ProjectSpace project =
			(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,DomainConstants.PROGRAM);
		MapList slProjects = null;

		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strObjectId = (String) programMap.get("objectId");
			String strLanguage = (String) programMap.get("Accept-Language");

			businessGoal.setId(strObjectId);

			StringList slSelectables = new StringList(11);
			slSelectables.add(project.SELECT_ID);
			slSelectables.add(project.SELECT_TYPE);
			slSelectables.add(project.SELECT_NAME);
			slSelectables.add(project.SELECT_CURRENT);
			slSelectables.add(project.SELECT_ORIGINATED);
			slSelectables.add(project.SELECT_OWNER);
			slSelectables.add(project.SELECT_DESCRIPTION);
			slSelectables.add(project.SELECT_TASK_ESTIMATED_FINISH_DATE);
			slSelectables.add(project.SELECT_TASK_ACTUAL_FINISH_DATE);
			slSelectables.add(project.SELECT_POLICY);
			slSelectables.add(project.SELECT_BASELINE_CURRENT_END_DATE);
			slSelectables.add(project.SELECT_PROGRAM_NAME);

			slProjects = businessGoal.getProjects(context, slSelectables, null);
		}
		catch (Exception e) {
			throw (new MatrixException());
		}
		finally
		{
			return slProjects;
		}
	}



	/**
	 * Exclude Business Goal which are already connected to Project
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return StringList
	 * @throws MatrixException
	 */

	 @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	 public StringList excludeProjectsforAddExisting(Context context, String[] args)throws  MatrixException
	    {
	    	StringList slProjectsList = new StringList();
	    	try {

			String contextPersonId = PersonUtil.getPersonObjectID(context);
	    		Map programMap = (Map)JPO.unpackArgs(args);		
	        	String strBusinessGoalID = (String)programMap.get("objectId");
	        	DomainObject DomBusinessGoal =DomainObject.newInstance(context, strBusinessGoalID); 
	        	String POLICY_PROJECT_SPACE_HOLD_CANCEL = PropertyUtil.getSchemaProperty(context,"policy_ProjectSpaceHoldCancel");
				String STATE_HOLD = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PROJECT_SPACE_HOLD_CANCEL,"state_Hold");
				String STATE_CANCEL = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PROJECT_SPACE_HOLD_CANCEL,"state_Cancel");
	    		
			final String SELECT_MEMBER_ID = "from["+DomainRelationship.RELATIONSHIP_MEMBER+"].to.id";
	    		StringList busSelects = new StringList();
	    		busSelects.add(DomBusinessGoal.SELECT_ID); 
	    		busSelects.add(DomainObject.SELECT_CURRENT); 
			 busSelects.add(DomainObject.SELECT_TYPE); 
			busSelects.add(SELECT_MEMBER_ID);
			busSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);
			 
	    		MapList mpProjectList = DomainObject.findObjects(context,
	    				DomainObject.TYPE_PROJECT_SPACE,
	    				null,
	    				null,
	    				busSelects);                                                

	    		StringList slprojectList = DomBusinessGoal.getInfoList(
	    				context, 
	    				"from["+DomBusinessGoal.RELATIONSHIP_BUSINESS_GOAL_PROJECT_SPACE+"].to.id");

			for (Iterator iterator = mpProjectList.iterator(); iterator.hasNext();) {
				Map projectInfoMap = (Map) iterator.next();
				String memberIds = (String)projectInfoMap.get(SELECT_MEMBER_ID);
				String strPtojectId = (String)projectInfoMap.get(DomainObject.SELECT_ID);
				String strProjectState = (String)projectInfoMap.get(DomainObject.SELECT_CURRENT);
				 String strProjectType = (String)projectInfoMap.get(DomainObject.SELECT_TYPE);
				String strProjectVisibility = (String)projectInfoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);
				StringList slMemberIds = FrameworkUtil.split(memberIds, matrix.db.SelectConstants.cSelectDelimiter);

				 if((slprojectList.contains(strPtojectId)|| strProjectState.equalsIgnoreCase(DomainObject.STATE_PROJECT_SPACE_ARCHIVE)|| 
						 !slMemberIds.contains(contextPersonId)|| strProjectState.equalsIgnoreCase(STATE_CANCEL)
						 ||strProjectType.equalsIgnoreCase(ProgramCentralConstants.TYPE_EXPERIMENT))&& !strProjectVisibility.equals("Company")){
	    				slProjectsList.addElement(strPtojectId);

	    			}

	    		}
	    	}catch (Exception e) {
				throw (new MatrixException());
			}
	    	return slProjectsList;
	    }

	/**
	 * get SubGoal 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return MapList
	 * @throws MatrixException
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSubGoals(Context context, String[] args) throws Exception 
	{

		com.matrixone.apps.program.BusinessGoal businessGoal =
			(com.matrixone.apps.program.BusinessGoal) DomainObject.newInstance(context, DomainConstants.TYPE_BUSINESS_GOAL, DomainConstants.PROGRAM);
		MapList mlBusinessGoals = new MapList();
		MapList mlBusUnit= new MapList();
		
			Map programMap = (Map)JPO.unpackArgs(args);		
			String strObjectId = (String)programMap.get("objectId");
			String strExpandLevel = (String) programMap.get("expandLevel");
			short nExpandLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
			businessGoal.setId(strObjectId);
			StringList slBusSelects =  new StringList();
			slBusSelects.add(DomainObject.SELECT_ID);
			slBusSelects.add(DomainObject.SELECT_NAME);
			slBusSelects.add(DomainObject.SELECT_DESCRIPTION);
			slBusSelects.add(DomainObject.SELECT_CURRENT);
			slBusSelects.add(businessGoal.SELECT_COMMENTS);
			slBusSelects.add(businessGoal.SELECT_BUSINESS_BENEFIT);
			slBusSelects.add(businessGoal.SELECT_EFFECTIVE_DATE);
			slBusSelects.add(DomainObject.SELECT_OWNER);
			slBusSelects.add(DomainObject.SELECT_LEVEL);
			slBusSelects.add(businessGoal.SELECT_ORGANIZATION_NAME);
			slBusSelects.add(businessGoal.SELECT_ORGANIZATION_ID);
			slBusSelects.add(businessGoal.SELECT_HAS_SUBGOAL);
			slBusSelects.add(businessGoal.SELECT_PARENT_GOAL_ID);
			slBusSelects.add(businessGoal.SELECT_POLICY);
			slBusSelects.add("from[" + businessGoal.RELATIONSHIP_BUSINESS_GOAL_PROJECT_SPACE + "].to.id");
			StringList slRelSelects = new StringList(1);
			
			mlBusinessGoals = businessGoal.getSubGoals(context,nExpandLevel, slBusSelects, false);
		
		return mlBusinessGoals;
	} 

	/** get Business Goal name and make it bold if Business Goal has SubGoal
	 * @param context
	 * @param args
	 * @return Vector
	 * @throws MatrixException
	 */
	public Vector getNameColumn (Context context, String[] args) throws MatrixException
	{

		HashMap programMap=new HashMap();
		try {
			programMap = (HashMap) JPO.unpackArgs(args);

			MapList mlObjects = (MapList) programMap.get("objectList");
			HashMap paramList    = (HashMap) programMap.get("paramList");
			boolean isPrinterFriendly = false;
			String strPrinterFriendly = (String)paramList.get("reportFormat");

			if ( strPrinterFriendly != null ) {
				isPrinterFriendly = true;
			}

			String[] strObjectIds = new String[mlObjects.size()];
			int size = mlObjects.size();
			for (int i = 0; i < size; i++) {
				Map mapObject = (Map) mlObjects.get(i);
				String strBusinessGoalId = (String) mapObject.get(DomainObject.SELECT_ID);
				strObjectIds[i] = strBusinessGoalId;
			}
			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			slBusSelect.add(DomainConstants.SELECT_TYPE);
			slBusSelect.add(DomainConstants.SELECT_NAME);
			String strIsSubGoal = "from["+ProgramCentralConstants.RELATIONSHIP_SUBGOAL+"].to.id";
			slBusSelect.add(strIsSubGoal);
			Map mpBusinessGoalInfo = new HashMap();
			BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);

			for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
				BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();
				Map mpBusinessGoal = new HashMap();
				for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
					String strSelectable = (String)itrSelectables.next();
					mpBusinessGoal.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
				}
				mpBusinessGoalInfo.put(objectWithSelect.getSelectData(SELECT_ID), mpBusinessGoal);
			}

			Iterator objectListIterator = mlObjects.iterator();
			Vector vcColumnValues = new Vector(mlObjects.size());    

			while (objectListIterator.hasNext())
			{
				Map mpObjects = (Map) objectListIterator.next();
				String strBusinessGoalId = (String) mpObjects.get(DomainObject.SELECT_ID);
				String strLevel = (String) mpObjects.get(DomainObject.SELECT_LEVEL);
				Map mpObjectInfo = (Map)mpBusinessGoalInfo.get(strBusinessGoalId);
				DomainObject domBusinessGoal  = DomainObject.newInstance(context, strBusinessGoalId);      
				String strName = (String)mpObjectInfo.get(SELECT_NAME);    		
				String strSubGoalIds = (String) mpObjectInfo.get(strIsSubGoal);
				StringBuffer sBuff = new StringBuffer();           
				// XSS encoding removed because it is breaking in french
				//strName = XSSUtil.encodeForURL(context, strName);
				strName = XSSUtil.encodeForXML(context, strName);

				if(!strSubGoalIds.isEmpty()){
					sBuff.append ("<a href=\"../common/emxTree.jsp?objectId="+XSSUtil.encodeForURL(context,strBusinessGoalId)+"\" class=\"object\" target=\"content\" title=\""+strName+"\" >");
					// XSS encoding removed because it is breaking in french and some other
					sBuff.append(strName);
					sBuff.append("</a>");
				}else{  			
					sBuff.append ("<a href=\"../common/emxTree.jsp?objectId="+XSSUtil.encodeForURL(context,strBusinessGoalId)+"\" class=\"object\" target=\"content\"  >");
					// XSS encoding removed because it is breaking in french and some other
					sBuff.append(strName);
					sBuff.append("</a>");
				}
				vcColumnValues.add(sBuff.toString());
			}
			return vcColumnValues;
		} catch (Exception e) {
			throw (new MatrixException());
		}
	}


	  /**
	   * check mode if view then show Owner field as read only
	   * 
	   * @param context the eMatrix <code>Context</code> object
	   * @param args
	   * @return boolean  
	   * @throws MatrixException
	   */
	  public boolean isShowFieldOwnerModeView(Context context, String args[]) throws MatrixException
	  {
	 	 boolean blAccess = false;
	 	 try{
	 		HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
	 		 String strMode = (String) programMap.get("mode");
	 		 if("view".equals(strMode))
	 		 {
	 			 blAccess = true;
	 		 }
	 	 }catch(Exception e) {
	 		 throw new MatrixException(e);		 
	 	 }	 
	 		 return blAccess;
	 	 }
	  /**
	  * check mode if edit then show Owner field as editable
	   * 
	   * @param context the eMatrix <code>Context</code> object
	   * @param args
	   * @return boolean  
	   * @throws Exception  if the operation fails
	   */
	  public boolean isShowFieldOwnerModeEdit(Context context, String args[]) throws MatrixException
	  {
	 	 boolean blAccess = false;
	 	 try{
	 		HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
	 		 String strMode = (String) programMap.get("mode");
	 		 if("edit".equals(strMode))
	 		 {
	 			 blAccess = true;
	 		 }
	 	 }catch(Exception e) {
	 		 throw new MatrixException(e);		 
	 	 }	 
	 		 return blAccess;
	 	 }
	  /**
	   *  * check mode if edit then show Buisiness Unit field as editable
	   * 
	   * @param context the eMatrix <code>Context</code> object
	   * @param args
	   * @return boolean  
	   * @throws Exception  if the operation fails
	   */
	  public boolean isShowFieldBusinessUnitModeEdit(Context context, String args[]) throws MatrixException
	  {
	 	 boolean blAccess = false;
	 	 try{
	 		HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
	 		 String strMode = (String) programMap.get("mode");
	 		 if("edit".equals(strMode))
	 		 {
	 			 blAccess = true;
	 		 }
	 	 }catch(Exception e) {
	 		 throw new MatrixException(e);		 
	 	 }	 
	 		 return blAccess;
	 	 }
	  /**
	   * check mode if view then show Buisiness Unit field as read only  
	   * 
	   * @param context the eMatrix <code>Context</code> object
	   * @param args
	   * @return boolean  
	   * @throws Exception  if the operation fails
	   */
	  public boolean isShowFieldBusinessUnitModeView(Context context, String args[]) throws MatrixException
	  {
	 	 boolean blAccess = false;
	 	 try{
	 		HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
	 		String strMode = (String) programMap.get("mode");
	 		if("view".equals(strMode))
	 		 {
	 			 blAccess = true;
	 		 }
	 	 }catch(Exception e) {
	 		 throw new MatrixException(e);		 
	 	 }	 
	 		 return blAccess;
	 	 }
	
	  public String getOriginator(Context context, String args[]) throws Exception
	  {
		  return ProgramCentralUtil.getOriginator(context, args);
	  }
	
}
