/* emxCommonPersonSearchBase.java

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.SelectConstants;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Risk;

/**
 * The <code>emxTaskBase</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version PRG 2011x - Copyright (c) 2002-2010, MatrixOne, Inc.
 */
public class emxCommonPersonSearchBase_mxJPO extends emxDomainObject_mxJPO
{
	// Create an instant of emxUtil JPO
	//protected ${CLASS:emxProgramCentralUtil} emxProgramCentralUtilClass = null;

	/* Role Project User*/
	public static final String ROLE_PROJECT_USER = PropertyUtil.getSchemaProperty("role_ProjectUser");

	/* Role External Project User*/
	public static final String ROLE_EXTERNAL_PROJECT_USER = PropertyUtil.getSchemaProperty("role_ExternalProjectUser");

	/* Role Project Administrator*/
	public static final String ROLE_PROJECT_ADMINISTRATOR = PropertyUtil.getSchemaProperty("role_ProjectAdministrator");

	/* Role External Project Administrator*/
	public static final String ROLE_EXTERNAL_PROJECT_ADMINISTRATOR = PropertyUtil.getSchemaProperty("role_ExternalProjectAdministrator");

	/* Role External Project Administrator*/
	public static final String ROLE_EXTERNAL_PROJECT_LEAD = PropertyUtil.getSchemaProperty("role_ExternalProjectLead");


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
	public emxCommonPersonSearchBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		super(context, args);
	}

	/**
	 * List out the person in the company eligible to become Owner of a Project Template.
	 * who have role project administrator can be there
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return StringList
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includePersonsForProjectTemplateOwner(Context context, String[] args)
	throws MatrixException
	{
		try {
			String [] strRoles = {ProgramCentralConstants.ROLE_VPLM_PROJECT_LEADER,
					ROLE_PROJECT_ADMINISTRATOR,ROLE_EXTERNAL_PROJECT_ADMINISTRATOR};
			int totalOwnerRoles = strRoles.length;

			String commandTemp = "";
			String strResult = "";
			StringList strListTemp = new StringList();
			StringList strPersonList = new StringList();

			for(int itr = 0; itr < strRoles.length; itr++){
				commandTemp = "print role $1 select $2 dump $3";
				strResult = MqlUtil.mqlCommand(context, commandTemp,strRoles[itr],"person","|");
				strListTemp = FrameworkUtil.split(strResult, "|");

				if(itr == 0){
					strPersonList.addAll(strListTemp);
				}
				for(int iterator = 0; iterator < strListTemp.size(); iterator++){
					if(itr > 0 && null != strListTemp && !strPersonList.contains(strListTemp.get(iterator))){
						strPersonList.add(strListTemp.get(iterator));
					}
				}
			}
			StringList strFinalList = new StringList(); 
			for(int itr = 0; itr <strPersonList.size(); itr++){
				String personId = PersonUtil.getPersonObjectID(context,strPersonList.get(itr).toString());
				strFinalList.add(personId);
			}

			return strFinalList;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * Generates an inclusion list of project members to be added as risk assignee 
	 * in the search page.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args request data.
	 * @return a StringList of members to be shown in the Add Risk Assignee search.
	 * @throws MatrixException if operations on DomainObject fail.
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeRiskAssignees(Context context, String[] args)throws MatrixException
	{
		try {
			final String SELECT_ASSIGNED_RISK_MEMBER_ID = "to[" + ProgramCentralConstants.RELATIONSHIP_ASSIGNED_RISK + "].from.id";
			DomainObject.MULTI_VALUE_LIST.add(SELECT_ASSIGNED_RISK_MEMBER_ID);
			StringList slMemberList = new StringList();
			String [] riskIdArray = new String[1];

			Map programMap = (HashMap) JPO.unpackArgs(args);
			String strProjectId = (String) programMap.get("objectId");
			String strRiskIds = (String) programMap.get("selectedRiskIds");
			DomainObject project = DomainObject.newInstance(context,strProjectId);
			
			StringList selects = new StringList();
			selects.add(DomainObject.SELECT_ID);
			
			StringList relSelects = new StringList();
			relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
			
			if (ProgramCentralUtil.isNotNullString(strRiskIds)) {
				riskIdArray = strRiskIds.split(",");
			}
			
			if(!project.isKindOf(context, DomainObject.TYPE_PROJECT_SPACE)){
				riskIdArray[0] = strProjectId;
				Risk risk = new Risk(strProjectId);
				StringList strProjectSelectsList = new StringList();
				strProjectSelectsList.add(DomainObject.SELECT_ID);
				
				Map mapProjectInfo = risk.getProjectInfo(context, strProjectSelectsList);
				strProjectId = (String)mapProjectInfo.get(DomainObject.SELECT_ID);
			}
			
			ProjectSpace objectProjectSpace = new ProjectSpace(strProjectId);
			MapList projectMemebers =  
					objectProjectSpace.getMembers(context,selects,relSelects,DomainObject.EMPTY_STRING,DomainObject.EMPTY_STRING);

			StringList busSelects = new StringList();
			busSelects.addElement(SELECT_ASSIGNED_RISK_MEMBER_ID);
			
			MapList mlAssigneeList = DomainObject.getInfo(context, riskIdArray, busSelects);
			
			for(int i=0;i<mlAssigneeList.size();i++){
				Map mpAssignee = (Map)mlAssigneeList.get(i);
				Object objectRiskAssigneeId = mpAssignee.get(SELECT_ASSIGNED_RISK_MEMBER_ID);
				if(objectRiskAssigneeId != null){
					if(objectRiskAssigneeId instanceof StringList){
						StringList slAssigneeId = (StringList)objectRiskAssigneeId;
						for(int j=0;j<projectMemebers.size();j++){
							Map <String,String>memberMap = (Map)projectMemebers.get(j);
							String strMemberId = memberMap.get(DomainObject.SELECT_ID);
							if(!slAssigneeId.contains(strMemberId) && !slMemberList.contains(strMemberId)){
								slMemberList.addElement(strMemberId);
							}
						}
					}else{
						slMemberList.addElement(objectRiskAssigneeId);
					}
				}else{
					for(int j=0;j<projectMemebers.size();j++){
						Map <String,String>memberMap = (Map)projectMemebers.get(j);
						String strMemberId = memberMap.get(DomainObject.SELECT_ID);
						slMemberList.addElement(strMemberId);
					}
				}
			}

			return slMemberList;
		}catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * includeMembersAddPerson - This Method is used to include persons
	 * while adding members 
	 * for the project
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return StringList
	 * @throws Exception if the operation fails
	 * @since PRG V6R2011x
	 * @author VM3
	 * 
	 * Note(NR2): Should be deleted, as this has been replace by excludeMembersAddPerson. 
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeMembersAddPerson(Context context, String[] args)
	throws MatrixException
	{
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String projectId = (String) programMap.get("parentOID");

			StringList selects = new StringList();
			selects.add(SELECT_ID);
			selects.add(SELECT_NAME);
			selects.add(SELECT_TYPE);

			StringList relSelects    = new StringList();

			ProjectSpace objectProjectSpace = new ProjectSpace(projectId);
			MapList personList =  objectProjectSpace.getMembers(context, selects, relSelects, null, null);

			StringList strTempList = new StringList();
			for(int itr = 0; itr < personList.size(); itr++){
				Map map = (Map) personList.get(itr);
				String strPersonId = (String) map.get(DomainConstants.SELECT_ID);	
				strTempList.add(strPersonId);
			}

			String [] strRoles = {ROLE_PROJECT_USER,ROLE_EXTERNAL_PROJECT_USER};
			int totalPersonRoles = strRoles.length;

			String str = "";
			String commandTemp = "";
			String strResult = "";
			StringList strListTemp = new StringList();
			StringList strPersonList = new StringList();
			MQLCommand mqlcmd = new MQLCommand();

			for(int itr = 0; itr < strRoles.length; itr++){
				commandTemp = "print role $1 select $2 dump $3";
				strResult = MqlUtil.mqlCommand(context, commandTemp,strRoles[itr],"person","|");
				mqlcmd.executeCommand(context, strResult);
				strListTemp = FrameworkUtil.split(strResult, "|");

				if(itr == 0){
					strPersonList.addAll(strListTemp);
				}
				for(int iterator = 0; iterator < strListTemp.size(); iterator++){
					if(itr > 0 && null != strListTemp && !strPersonList.contains(strListTemp.get(iterator))){
						strPersonList.add(strListTemp.get(iterator));
					}
				}
			}
			StringList strFinalList = new StringList(); 
			for(int itr = 0; itr <strPersonList.size(); itr++){
				String personId = PersonUtil.getPersonObjectID(context,strPersonList.get(itr).toString());
				strFinalList.add(personId);
			}

			for(int itr = 0; itr < strTempList.size(); itr++){
				if(strFinalList.contains(strTempList.get(itr)))
				{
					String id = (String) strTempList.get(itr);
					strFinalList.remove(id);
				}
			}
			return strFinalList;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	
	/**
	 * Gets the ids of all project members to be excluded in the search, 
	 * while adding new members in the project.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args the request arguments
	 * @return a StringList of person object ids that are to be excluded while adding new people as project members.
	 * @throws MatrixException if operation fails.
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getMembersIdsToExclude(Context context, String[] args) throws MatrixException{
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			String projectId = (String) programMap.get("parentOID");
			DomainObject object = DomainObject.newInstance(context, projectId); 
			StringList memberIdList = new StringList();
			boolean isProjectSpace = object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_SPACE);
			boolean isProjectConcept = object.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_CONCEPT); 
			if(isProjectSpace || isProjectConcept){
				String memberId = "";
				StringList objectSelects = new StringList();
				StringList relSelects = new StringList();
				String where = "";
				String relWhere = "";
				objectSelects.add(SELECT_ID);
				ProjectSpace project = new ProjectSpace(projectId);
				MapList memberInfoMapList = project.getMembers(context, objectSelects, relSelects, where, relWhere);
				for (Iterator iterator = memberInfoMapList.iterator(); iterator.hasNext();) {
					Map memberInfoMap = (Map) iterator.next();
					memberId = (String) memberInfoMap.get(SELECT_ID);
					memberIdList.add(memberId);
				}
				return memberIdList;
			}else{
				throw new MatrixException();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}
	/**
	 * excludeMembersAddPerson - This Method is used to exclude Already Project Members from search result of Add Person dialog.
	 * while adding members for the project.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return StringList
	 * @throws MatrixException if the operation fails
	 * @since PRG V6R2012x
	 * @author NR2
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeMembersAddPerson(Context context, String[] args)
	throws MatrixException{
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			String projectId = (String) programMap.get("parentOID");
			StringList slNonEmployee = new StringList();
			emxProgramCentralUtil_mxJPO excludeNonEmployee = new emxProgramCentralUtil_mxJPO(context, args);
			slNonEmployee = excludeNonEmployee.getexcludeOIDforPersonSearch(context, args);

			StringList selects = new StringList();
			selects.add(SELECT_ID);

			StringList relSelects    = new StringList();

			if(ProgramCentralUtil.isNullString(projectId)){
				throw new Exception();
			}

			ProjectSpace objectProjectSpace = new ProjectSpace(projectId);
			MapList personList =  objectProjectSpace.getMembers(context, selects, relSelects, null, null);

			StringList returnMemList = new StringList();
			for(Iterator itr = (personList==null?null:personList.listIterator());itr!=null && itr.hasNext();){
				Map m = (Map)itr.next();
				String memberId = (String)m.get(SELECT_ID);
				returnMemList.add(memberId);
			}
			returnMemList.addAll(slNonEmployee);
			return returnMemList;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}
	//End:nr2:PRG:R212:16 Jun 2011:

	/**
	 * getPersonAccesses - This Method is used get Person Accesses
	 * for the project this is a programHTML 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Vector
	 * @throws Exception if the operation fails
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	public Vector getPersonAccesses(Context context, String[] args)
	throws MatrixException
	{
		Vector returnVec = new Vector();
		try 
		{
			String strAccess = "";
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			String sLanguage = context.getSession().getLanguage();
			AttributeType atrProjectAccess = new AttributeType(DomainConstants.ATTRIBUTE_PROJECT_ACCESS);
			atrProjectAccess.open(context);
			StringList strList = atrProjectAccess.getChoices(context);   
			strList.remove("Project Owner");
			atrProjectAccess.close(context);

			StringList slProjectAccesses = new StringList();
			for(int i=0; i<strList.size();i++){
				String sProjectAccess = (String)strList.get(i);
				if(sProjectAccess != null && !sProjectAccess.startsWith("role_"))
				{
					slProjectAccesses.add(sProjectAccess);
					slProjectAccesses.sort();
				}
			}

			StringList slTempList = new StringList();
			for(int itr = slProjectAccesses.size()-1; itr >= 0 ;itr-- ){
				slTempList.add(slProjectAccesses.get(itr));
			}

			slProjectAccesses = new StringList(slTempList);
			StringList strTempList = new StringList();
			for(int iterator = 0; iterator < slProjectAccesses.size(); iterator++){
				String strTemp = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.CommonPeopleSearch.ProjectAccess"+iterator+".Options", context.getSession().getLanguage());
				strTempList.addElement(strTemp);
			}

			for(int iterator = 0; iterator < objectList.size(); iterator++){
				Map mapTemp = (Map) objectList.get(iterator);
				String strName = (String) mapTemp.get("Name");
				String strId = (String) mapTemp.get("id") + "A";
				strAccess = "";
				strAccess += "<select name = '"+strId+"'>";
				for(int itr = 0; itr < slProjectAccesses.size(); itr++){
					strAccess += "<option value='"+(String)slProjectAccesses.get(itr)+"'>"+(String)strTempList.get(itr)+"</option>";
				}
				strAccess += "</select>";
				returnVec.add(strAccess);
			}
			return returnVec;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * getPersonRoles - This Method is used get Person Roles
	 * for the project this is a programHTML 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Vector
	 * @throws Exception if the operation fails
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	public Vector getPersonRoles(Context context, String[] args)
	throws MatrixException
	{
		Vector returnVec = new Vector();
		try 
		{
			String strRole = "";
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			String sLanguage = context.getSession().getLanguage();

			//[MODIFIED::PRG:RG6:Jan 19, 2011:IR-055750V6R2012:R211::Start]
			// get the project roles
			StringList slProjectRole = new StringList();
			slProjectRole = ProgramCentralUtil.getAllProjectRoles(context);
			// get the translated values
			emxProjectMemberBase_mxJPO projMember = new emxProjectMemberBase_mxJPO(context,args); 
			Map mapRoleI18nStrings = projMember.geti18nProjectRoleRDOValues(context);

			int size = objectList.size();
			for(int iterator = 0; iterator < size; iterator++)
			{
				Map mapTemp = (Map) objectList.get(iterator);
				String strName = (String) mapTemp.get("Name");
				String strId = (String) mapTemp.get("id") + "R";

				StringBuffer returnString= new StringBuffer();
				returnString.append("<select name = \"");
				returnString.append(strId);
				returnString.append("\">");

				for(int itr = 0; itr < slProjectRole.size(); itr++)
				{
					String strRoleTemp = (String)slProjectRole.get(itr);
					String strI18RoleVal = (String)mapRoleI18nStrings.get(strRoleTemp);

					if(ProgramCentralUtil.isNullString(strRoleTemp))
					{
						strRoleTemp = "";
						returnString.append("<option value=\"");
						returnString.append(strRoleTemp);
						returnString.append("\">");
						returnString.append(strRoleTemp);
						returnString.append("</option>");
					}
					else
					{
						if(ProgramCentralUtil.isNullString(strI18RoleVal))
						{
							strI18RoleVal = strRoleTemp;
						}

						returnString.append("<option value=\"");
						returnString.append(XSSUtil.encodeForHTML(context,strRoleTemp));
						returnString.append("\">");
						returnString.append(XSSUtil.encodeForXML(context,strI18RoleVal));
						returnString.append("</option>");
					}

				}

				returnString.append("</select>");
				returnVec.add(returnString.toString());
			}
			//[MODIFIED::PRG:RG6:Jan 19, 2011:IR-055750V6R2012:R211::End]
			return returnVec;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * getDynamicAccessAndRoles - This Method is a dynamic method for columns 
	 * Project Access and Project Role
	 * fot the project
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return MapList
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	public MapList getDynamicAccessAndRoles(Context context, String[] args) throws MatrixException
	{ 
		try
		{
			MapList mlColumns = new MapList();
			Map programMap = (Map)JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");
			if(null != strMode && !"".equalsIgnoreCase(strMode) && strMode.equalsIgnoreCase("addMember")){
				Map mapColumn = new HashMap();
				//For Project Access Column
				String ProjectAccess = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.CommonPeopleSearch.ProjectAccess", context.getSession().getLanguage());
				mapColumn.put("label", ProjectAccess);
				Map mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxCommonPersonSearch");
				mapSettings.put("function","getPersonAccesses");
				mapSettings.put("Column Type","programHTMLOutput");
				mapSettings.put("Editable","true");
				mapSettings.put("Width","150");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);

				//For Project Role Column
				mapColumn = new HashMap();
				String ProjectRole = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.CommonPeopleSearch.ProjectRole", context.getSession().getLanguage());
				mapColumn.put("label", ProjectRole);
				mapSettings = new HashMap();
				mapSettings.put("Registered Suite","ProgramCentral");
				mapSettings.put("program","emxCommonPersonSearch");
				mapSettings.put("function","getPersonRoles");
				mapSettings.put("Column Type","programHTMLOutput");
				mapSettings.put("Editable","true");
				mapSettings.put("Width","150");
				mapColumn.put("settings", mapSettings);
				mlColumns.add(mapColumn);
			}
			return mlColumns;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * getAvailableProjectSpaces - This Method is used to get available projects 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @returns String
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	public String getAvailableProjectSpaces(Context context, String[] args)
	throws MatrixException
	{
		try
		{
			boolean isMatrixSearch = false;
			if(args.length > 1) 
			{
				isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
			}

			DomainObject dmoPerson = DomainObject.newInstance(context, args[0]);

			StringList slObjSelects = new StringList();
			slObjSelects.addElement(SELECT_ID);
			slObjSelects.addElement(SELECT_TYPE);
			slObjSelects.addElement(SELECT_NAME);
			slObjSelects.addElement(SELECT_REVISION);

			StringList slRelSelects = new StringList();
			slRelSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList mlProjects = dmoPerson.getRelatedObjects(context, RELATIONSHIP_MEMBER, TYPE_PROJECT_SPACE,slObjSelects,slRelSelects,true,false,(short)1,null,null,0);

			String delimeter = null;
			if(isMatrixSearch)
			{
				delimeter = "|";
			}
			else
			{
				delimeter = SelectConstants.cSelectDelimiter;
			}

			StringList slProjectNames = new StringList();
			for(int jj=0;jj<mlProjects.size();jj++)
			{	
				Map mapProject = (Map) mlProjects.get(jj);
				slProjectNames.add(mapProject.get(SELECT_NAME));
			}

			StringBuffer sbProjectNames = new StringBuffer();
			for (Iterator itrProjectNames = slProjectNames.iterator(); itrProjectNames.hasNext(); )
			{
				String strProjectName = (String) itrProjectNames.next();

				if (sbProjectNames.length() != 0) 
				{
					sbProjectNames.append(delimeter);
				}
				sbProjectNames.append(strProjectName);
			}

			return sbProjectNames.toString();
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	//IR-177196V6R2014
	/**
	 * Return the Business Skills associated with the person
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context BusinessSkill object
	 * @return String containing BusinessSkill associated with the persons 
	 * @throws Exception if the operation fails
	 * @since since R210
	 */
	public String getBusinessSkills(Context context, String[] args)
	throws Exception
	{
		List featList = new StringList(args[0]);
		boolean isMatrixSearch = false;
		if(args.length > 1) 
		{
			isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
		}

		DomainObject domObj = new DomainObject(args[0]);
		StringList objSelects = new StringList();
		objSelects.addElement(SELECT_ID);
		objSelects.addElement(SELECT_TYPE);
		objSelects.addElement(SELECT_NAME);
		objSelects.addElement(SELECT_REVISION);

		String relpattern=PropertyUtil.getSchemaProperty(context,"relationship_hasBusinessSkill");
		StringList selectRelStmts=new StringList();
		selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		String typepattern=PropertyUtil.getSchemaProperty(context,"type_BusinessSkill");

		MapList businessSkillList = domObj.getRelatedObjects(context, relpattern,typepattern,objSelects,selectRelStmts,false,true,(short)1,null,null);

		String delimiter = null;
		if(isMatrixSearch)
		{
			delimiter = "|";
		}
		else
		{
			delimiter = SelectConstants.cSelectDelimiter;
		}

		StringList idList = new StringList();
		for(int jj=0;jj<businessSkillList.size();jj++)
		{	
			Map objMap = (Map) businessSkillList.get(jj);
			idList.add(objMap.get("name"));
		}
		StringBuffer BSBuff = new StringBuffer();
		for (Iterator BS = idList.iterator(); BS.hasNext(); )
		{
			String name = (String) BS.next();
			BSBuff.append(BSBuff.length() == 0? name:delimiter + name);
		}
		return(BSBuff.toString());
	}
	/**
	 * getAvailableLocations - This Method is used to get available Locations 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @returns String
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	public String getAvailableLocations(Context context, String[] args)
	throws MatrixException
	{
		try
		{
			boolean isMatrixSearch = false;
			if(args.length > 1) 
			{
				isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
			}

			DomainObject dmoPerson = DomainObject.newInstance(context, args[0]);

			StringList slObjSelects = new StringList();
			slObjSelects.addElement(SELECT_ID);
			slObjSelects.addElement(SELECT_TYPE);
			slObjSelects.addElement(SELECT_NAME);
			slObjSelects.addElement(SELECT_REVISION);

			StringList slRelSelects = new StringList();
			slRelSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList mlLocations = dmoPerson.getRelatedObjects(context, RELATIONSHIP_WORKPLACE, TYPE_LOCATION,slObjSelects,slRelSelects,false,true,(short)1,null,null,0);

			String delimeter = null;
			if(isMatrixSearch)
			{
				delimeter = "|";
			}
			else
			{
				delimeter = SelectConstants.cSelectDelimiter;
			}

			StringList s1Locations = new StringList();
			for(int jj=0;jj<mlLocations.size();jj++)
			{	
				Map mapLocation = (Map) mlLocations.get(jj);
				s1Locations.add(mapLocation.get(SELECT_NAME));
			}

			StringBuffer sbLocations = new StringBuffer();
			for (Iterator itrLocations = s1Locations.iterator(); itrLocations.hasNext(); )
			{
				String strLocationName = (String) itrLocations.next();

				if (sbLocations.length() != 0) 
				{
					sbLocations.append(delimeter);
				}
				sbLocations.append(strLocationName);
			}

			return sbLocations.toString();
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * getAvailableCompanies - This Method is used to get available Companies 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @returns String
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */

	public String getAvailableCompanies(Context context, String[] args)
	throws MatrixException
	{
		try{
			boolean isMatrixSearch = false;
			if(args.length > 1) 
			{
				isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
			}

			DomainObject dmoPerson = DomainObject.newInstance(context, args[0]);

			StringList slObjSelects = new StringList();
			slObjSelects.addElement(SELECT_ID);
			slObjSelects.addElement(SELECT_TYPE);
			slObjSelects.addElement(SELECT_NAME);
			slObjSelects.addElement(SELECT_REVISION);

			StringList slRelSelects = new StringList();
			slRelSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList mlCompanies = dmoPerson.getRelatedObjects(context, RELATIONSHIP_MEMBER, TYPE_ORGANIZATION,slObjSelects,slRelSelects,true,false,(short)1,null,null,0);

			String delimeter = null;
			if(isMatrixSearch)
			{
				delimeter = "|";
			}
			else
			{
				delimeter = SelectConstants.cSelectDelimiter;
			}

			StringList slCompanies = new StringList();
			for(int jj=0;jj<mlCompanies.size();jj++)
			{	
				Map mapCompany = (Map) mlCompanies.get(jj);
				slCompanies.add(mapCompany.get(SELECT_NAME));
			}

			StringBuffer sbCompanies = new StringBuffer();
			for (Iterator itrLocations = slCompanies.iterator(); itrLocations.hasNext(); )
			{
				String strCompanyName = (String) itrLocations.next();

				if (sbCompanies.length() != 0) 
				{
					sbCompanies.append(delimeter);
				}
				sbCompanies.append(strCompanyName);
			}

			return sbCompanies.toString();
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * getCriteriaRangeValues - This Method is used to get range values of the search fields 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @returns Map
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	public static Map getCriteriaRangeValues(Context context, String args[])
	throws Exception
	{
		try{
			Map argMap = (Map) JPO.unpackArgs(args);

			String currentField = (String) argMap.get("currentField");
			String selectable = "";
			if("PRG_PEOPLE_PROJECT_SPACE".equalsIgnoreCase(currentField))
			{
				selectable = "program[emxCommonPersonSearch -method getAvailableProjectSpaces ${OBJECTID}]";
			}
			else if("PRG_PEOPLE_LOCATION".equalsIgnoreCase(currentField))
			{
				selectable = "program[emxCommonPersonSearch -method getAvailableLocations ${OBJECTID}]";
			}
			else if("PRG_PEOPLE_COMPANY".equalsIgnoreCase(currentField))
			{
				selectable = "program[emxCommonPersonSearch -method getAvailableCompanies ${OBJECTID}]";
			}
			else if("PRG_PEOPLE_BUSINESS_SKILL".equalsIgnoreCase(currentField))
			{
				selectable = "program[emxCommonPersonSearch -method getBusinessSkills ${OBJECTID}]";
			}

			StringList strList = new StringList();
			strList.add(SELECT_NAME);
			strList.add(SELECT_TYPE);
			strList.add(SELECT_ID);
			strList.add(selectable);

			MapList mlPersons = DomainObject.findObjects(context, TYPE_PERSON, "*", null, strList);

			StringList slPersonNames = new StringList();
			for(int i=0;i<mlPersons.size();i++)
			{
				Map mapPerson = (Map) mlPersons.get(i);
				slPersonNames.addElement((String)mapPerson.get(DomainConstants.SELECT_NAME));
			}

			Map returnMap = new HashMap();
			Iterator itrPersons = mlPersons.iterator();
			while(itrPersons.hasNext())
			{
				Map mapPerson = (HashMap)itrPersons.next();
				String strProjectNames = (String)mapPerson.get(selectable);
				if (strProjectNames == null || "".equals(strProjectNames)) 
				{
					continue;
				}

				StringList slProjectNames = FrameworkUtil.split(strProjectNames, SelectConstants.cSelectDelimiter);

				if(strProjectNames.indexOf(SelectConstants.cSelectDelimiter) == -1)
				{
					slProjectNames = FrameworkUtil.split(strProjectNames, "|");
				}

				for (Iterator itrProjectNames = slProjectNames.iterator(); itrProjectNames.hasNext();) 
				{
					String strProjectName = (String) itrProjectNames.next();
					if (!returnMap.containsKey(strProjectName)) 
					{
						returnMap.put(strProjectName, new Integer(1));
					}
					else 
					{
						Integer count = (Integer)returnMap.get(strProjectName);
						count = new Integer(count.intValue() + 1);
						returnMap.put(strProjectName, count);
					}
				}
			}

			for (Iterator itrProjectNameAndCount = returnMap.keySet().iterator(); itrProjectNameAndCount.hasNext();) 
			{
				String strProjectName = (String) itrProjectNameAndCount.next();
				Integer totalPersons = (Integer)returnMap.get(strProjectName);

				returnMap.put(strProjectName, strProjectName + " (" + totalPersons + ")");

			}

			return returnMap;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}

	/**
	 * includePersonsForProjectOwner - This Method is used to include Project Owner
	 * who have role Project Lead can be there
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return StringList
	 * @exception if fails throws MatrixException
	 * @since PRG V6R2011x
	 * @author VM3
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includePersonsForProjectOwner(Context context, String[] args)
	throws MatrixException
	{
		try {
			String [] strRoles = {ProgramCentralConstants.ROLE_VPLM_PROJECT_LEADER,ROLE_PROJECT_LEAD,ROLE_EXTERNAL_PROJECT_LEAD};
			int totalOwnerRoles = strRoles.length;
			String commandTemp = "";
			String strResult = "";
			StringList strListTemp = new StringList();
			StringList strPersonList = new StringList();

			for(int itr = 0; itr < strRoles.length; itr++){
				commandTemp = "print role $1 select $2 dump $3";
				strResult = MqlUtil.mqlCommand(context, commandTemp,strRoles[itr],"person","|");
				strListTemp = FrameworkUtil.split(strResult, "|");

				if(itr == 0){
					strPersonList.addAll(strListTemp);
				}
				for(int iterator = 0; iterator < strListTemp.size(); iterator++){
					if(itr > 0 && null != strListTemp && !strPersonList.contains(strListTemp.get(iterator))){
						strPersonList.add(strListTemp.get(iterator));
					}
				}
			}
			StringList strFinalList = new StringList(); 
			for(int itr = 0; itr <strPersonList.size(); itr++){
				String personId = PersonUtil.getPersonObjectID(context,strPersonList.get(itr).toString());
				strFinalList.add(personId);

			}
			return strFinalList;
		}
		catch(Exception ex)
		{
			throw new MatrixException(ex);
		}
	}
}
