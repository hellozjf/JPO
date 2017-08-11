// emxProgramBase.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
// static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.13.2.1 Thu Dec  4 07:55:16 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.13 Wed Oct 22 15:49:17 2008 przemek Experimental przemek $
//

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

/**
 * The <code>emxProgramBase</code> class represents the Program JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProgramBase_mxJPO extends com.matrixone.apps.program.Program
{
	/** the company id for this program. */
	protected String _contextCompanyId = null;

	/**
	 * Constructs a new emxProgram JPO object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *     0 - containing one String entry for key "objectId"
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.1
	 */
	public emxProgramBase_mxJPO (Context context, String[] args)
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
	 * This function verifies the user's permission for the given program.
	 * This check is made by verifying the user's company matches the
	 * program's company.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *     0 - containing one String entry for key "objectId"
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.0
	 */
	public boolean hasAccess(Context context, String args[])
			throws Exception
			{
		String id = args[0];
		setId(id);

		if (_contextCompanyId == null)
		{
			com.matrixone.apps.common.Person person = null;
			_contextCompanyId = person.getPerson(context).getCompanyId(context);
		}

		String companyProgramId = getInfo(context, SELECT_COMPANY_ID);

		return (_contextCompanyId.equals(companyProgramId)) ? true : false;
			}

	/****************************************************************************************************
	 *       Methods for Config Table Conversion Task
	 ****************************************************************************************************/
	/**
	 * This method gets the list of all Programs the user has access
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @return MapList containing the ids of program objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllPrograms(Context context, String[] args)
			throws Exception
			{
		return getMyPrograms(context, args, null);
			}

	/**
	 * This method gets the list of Programs in Active State
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @return MapList containing the ids of program objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getActivePrograms(Context context, String[] args)
			throws Exception
			{
		return getMyPrograms(context, args, STATE_PROGRAM_ACTIVE);
			}

	/**
	 * getInActivePrograms - gets the list of Programs in Inactive State
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @return MapList containing the ids of program objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getInActivePrograms(Context context, String[] args)
			throws Exception
			{
		return getMyPrograms(context, args, STATE_PROGRAM_INACTIVE);
			}


	/**
	 * This method gets the list of All Programs the user has access
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @return MapList containing the ids of program objects
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public MapList getMyPrograms(Context context, String[] args,String showSel)
			throws Exception
			{
		// Check license while listing Programs, if license check fails here
		// the programs will not be listed.
		//
		ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PGE_LICENSE_ARRAY);

		// Retrieve the program's program list.
		MapList programList = null;
		try
		{
			com.matrixone.apps.program.Program program = (com.matrixone.apps.program.Program) DomainObject.newInstance(context, DomainConstants.TYPE_PROGRAM,DomainConstants.PROGRAM);
			com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");
			String busWhere = null;

			// Add selectables
			StringList busSelects = new StringList(2);
			busSelects.add(program.SELECT_ID);
			busSelects.add(program.SELECT_VAULT);
			if(showSel == null)
			{
				busWhere = null;
			}
			else if((STATE_PROGRAM_INACTIVE).equals(showSel))
			{
				// modified for the bug 338045
				busWhere = program.SELECT_CURRENT + "== const '"+STATE_PROGRAM_INACTIVE+"'";
			}
			else
			{
				// modified for the bug 338045
				busWhere = program.SELECT_CURRENT + "!= const '"+STATE_PROGRAM_INACTIVE+"'";
			}


			String vaultPattern = "";
			String vaultOption = PersonUtil.getSearchDefaultSelection(context);
			vaultPattern = PersonUtil.getSearchVaults(context, false ,vaultOption);

			//use the matchlist keyword to filter by vaults, need this if option is not "All Vaults"
			if (!vaultOption.equals(PersonUtil.SEARCH_ALL_VAULTS) && vaultPattern.length() > 0)
			{
				if ((busWhere == null) || "".equals(busWhere))
				{
					busWhere = "vault matchlist '" + vaultPattern + "' ','";
				}
				else
				{
					busWhere += "&& vault matchlist '" + vaultPattern + "' ','";
				}
			}

			// pagination change
			if ((objectId == null) || objectId.equals("") || objectId.equals("null"))
			{
				programList = program.getPrograms(context, busSelects, busWhere);
			}
			else
			{
				programList = program.findObjects(context,
						DomainConstants.TYPE_PROGRAM,
						null,
						null,
						null,
						null,//vaultPattern
						busWhere,//busWhere
						false,
						busSelects);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return programList;
		}
			}

	/**
	 * This method displays the owner with lastname,firstname format
	 *             also has a link to open a pop up with the owner
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - MapList containing the object Maps
	 *        1 - MapList containing the parameters
	 * @return Vector containing the owner value as String
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public Vector showOwner(Context context, String[] args)
			throws Exception
			{
		Vector vecOwner = new Vector();
		try
		{
			com.matrixone.apps.program.Program program = (com.matrixone.apps.program.Program) DomainObject.newInstance(context, DomainConstants.TYPE_PROGRAM,DomainConstants.PROGRAM);
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String suiteDirectory = (String)paramList.get("SuiteDirectory");
			Map objectMap = null;
			boolean isPrinterFriendly = false;
			String PrinterFriendly = (String)paramList.get("reportFormat");
			if ( PrinterFriendly != null ) {
				isPrinterFriendly = true;
			}

			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] = (String) objectMap.get(program.SELECT_ID);
				arrayCount++;
			}

			MapList actionList = DomainObject.getInfo(context, objIdArr,
					new StringList(program.SELECT_OWNER));

			Iterator actionsListIterator = actionList.iterator();
			while(actionsListIterator.hasNext())
			{
				String displayOwner = "";
				objectMap = (Map) actionsListIterator.next();
				String user = (String)objectMap.get(program.SELECT_OWNER);
				String strOwner = com.matrixone.apps.common.Person.getDisplayName(context, user);

				com.matrixone.apps.common.Person tempPerson = com.matrixone.apps.common.Person.getPerson(context, user);
				String ownerId = tempPerson.getInfo(context, com.matrixone.apps.common.Person.SELECT_ID);
                  String personURL = "../common/emxTree.jsp?objectId="+XSSUtil.encodeForURL(context,ownerId)+"&amp;mode=replace&amp;jsTreeID=null&amp;AppendParameters=false&amp;emxSuiteDirectory="+suiteDirectory;

				if(!isPrinterFriendly) {
					StringBuffer sBuff = new StringBuffer();
                      sBuff.append("<input type='hidden' name='forsort' value='" +XSSUtil.encodeForHTML(context,strOwner)+ "'/>");
					sBuff.append("<a href='").append(personURL).append("'>").append(XSSUtil.encodeForHTML(context,strOwner)).append("</a>");
					displayOwner = sBuff.toString();
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
	 * This method will be called from Program create form for
	 * connecting the created program object with the company object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - MapList containing the object Maps
	 *        1 - MapList containing the parameters
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since PMC X+2
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public boolean connectCompany (Context context, String[] args) throws Exception {
		// Check license while creating Program, if license check fails here,the programs will not be created.
		ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PRG_LICENSE_ARRAY);
		boolean allowConnection = false;
		try{
		Map map = JPO.unpackArgs(args);
		Map paramMap = (Map)map.get("paramMap");
		Map requestMap = (Map)map.get("requestMap");		
		String objectId = (String) paramMap.get("objectId");
		String programName = (String) requestMap.get("Name");
			
		DomainObject program = DomainObject.newInstance(context, objectId);
			Company company      = com.matrixone.apps.common.Person.getPerson(context).getCompany(context);
			StringList objSelect = new StringList();
			objSelect.add(ProgramCentralConstants.SELECT_PHYSICALID);
			Map companyInfo = company.getInfo(context, objSelect);
			String revision = (String)companyInfo.get(ProgramCentralConstants.SELECT_PHYSICALID);

		String sCommandStatement = " modify bus $1 name $2 revision $3";
		MqlUtil.mqlCommand(context, sCommandStatement,objectId, programName,revision); 
		
			String tempQuery = " temp query bus $1 $2 $3 select $4 dump $5";
			String result = MqlUtil.mqlCommand(context, tempQuery,"Program", programName,revision,"id","|");
			if (ProgramCentralUtil.isNotNullString(result) && result.indexOf("\n") != -1) {
				allowConnection =false;			
			}else{
				// connect to the company.
				DomainRelationship.connect(context,
						company,
						DomainConstants.RELATIONSHIP_COMPANY_PROGRAM,
						program);
				
				allowConnection =true;
			}
		} catch (Exception e) {
			throw new MatrixException(e);
		}
		return allowConnection;
	}
	
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map connectProgramToCompany (Context context, String[] args) throws Exception {

		Map returnMap =new HashMap();
		boolean allowToConnect = connectCompany(context, args);

		if(allowToConnect){
			returnMap.put("Action", "success");
		}else{
			Map map       		= JPO.unpackArgs(args);
			Map paramMap 		= (Map)map.get("paramMap");
			Map requestMap 		= (Map)map.get("requestMap");
			String programName  = (String) requestMap.get("Name");
			String sContextLang = context.getSession().getLanguage();

			Company company      = Person.getPerson(context).getCompany(context);
			String companyName   = company.getInfo(context,DomainConstants.SELECT_NAME);

				Locale locale			= new Locale(sContextLang);
				String[] messageValues = new String[2];
				messageValues[0] =programName;
				messageValues[1] =companyName;
		
				String notice = MessageUtil.getMessage(context,null,
						"emxProgramCentral.Program.errMsg",
						messageValues,
						null,
						locale,
						"emxProgramCentralStringResource"); 

				returnMap.put("Message", notice);
				returnMap.put("Action", "ERROR");
		}

		return returnMap;
	}

	//Added:29-July-09:nr2:R208:PRG:Bug :374096
	//Range function Added to Get Vaults in create Program page
	public HashMap getProgVaultRangeValues(Context context, String[] args)
			throws Exception
			{
		com.matrixone.apps.common.Person person =
				(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
						DomainConstants.TYPE_PERSON,DomainConstants.PROGRAM);


		String sLanguage=context.getSession().getLanguage();

		String ctxPersonId = person.getPerson(context).getId();
		person.setId(ctxPersonId);

		String userVault = person.getVault();

		userVault = i18nNow.getMXI18NString(userVault,"",sLanguage,"Vault");

		StringList vaultsList = GetVaults(context, ctxPersonId);

		if(vaultsList.isEmpty()) {
			vaultsList = GetAllVaults(context);
		}
		vaultsList.sort();

		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();

		// Get internationalized vault names
		StringList i18NVaults = new StringList();
		String i18nVault = "";
		String vaultName = "";
		Iterator vaultItr = vaultsList.iterator();
		while(vaultItr.hasNext()) {
			vaultName = (String)vaultItr.next();
			vaultName = vaultName.trim();
			i18nVault  = i18nNow.getMXI18NString(vaultName,"",sLanguage,"Vault");

			fieldRangeValues.addElement(vaultName);
			fieldDisplayRangeValues.addElement(i18nVault);
		}

		HashMap tempMap = new HashMap();
		tempMap.put("field_choices", fieldRangeValues);
		tempMap.put("field_display_choices", fieldDisplayRangeValues);


		return tempMap;
			}
	/**
	 * This method Splits the string to StringList
	 * PMCProjectConceptCreateForm.
	 *
	 * @param Context
	 *            the context Object
	 * @param String
	 *            the split character
	 * @return StringList
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC V6R2010x
	 */

	public StringList GetVaults(Context context, String theId)
			throws MatrixException
			{
		com.matrixone.apps.common.Person person =
				(com.matrixone.apps.common.Person) DomainObject.newInstance(context,
						DomainConstants.TYPE_PERSON);

		StringList vaultSL = new StringList();
		if(theId != null && !"".equals(theId)) {
			person.setId(theId);
			vaultSL = convertToStringList(person.getSearchDefaultVaults(context), ",");
		}

		return vaultSL;

			}

	/**
	 * This method Splits the string to StringList
	 * PMCProjectConceptCreateForm.
	 *
	 * @param String
	 *            the vault String
	 * @param String
	 *            the split character
	 * @return StringList
	 * @throws Exception
	 *             if the operation fails
	 * @since PMC V6R2010x
	 */
	public StringList convertToStringList(String vaultStr, String splitAt)
			throws MatrixException
			{
		StringList vaultSL = new StringList();
		StringList vaultSplit = FrameworkUtil.split(vaultStr, splitAt);
		Iterator vaultItr = vaultSplit.iterator();
		while (vaultItr.hasNext()){
			vaultSL.add(((String) vaultItr.next()).trim());
		}
		return vaultSL;
			}

	public StringList GetAllVaults(Context context)
			throws MatrixException
			{

		// Get all vaults so that user can choose
		// this is all company's vaults not all vaults from all servers
		StringList vaultList = new StringList();

		com.matrixone.apps.common.Person person =
				com.matrixone.apps.common.Person.getPerson(context);
		com.matrixone.apps.common.Company company = person.getCompany(context);

		StringList selectList = new StringList(2);
		selectList.add(DomainConstants.SELECT_VAULT);
		selectList.add(DomainConstants.SELECT_SECONDARY_VAULTS);
		Map companyMap = company.getInfo(context,selectList);
		StringList secVaultList = FrameworkUtil.split((String)companyMap.get(DomainConstants.SELECT_SECONDARY_VAULTS),null);
		Iterator itr = secVaultList.iterator();

		String vaults = (String)companyMap.get(DomainConstants.SELECT_VAULT);
		vaultList.add(vaults);
		while (itr.hasNext() )
		{
			vaultList.add(PropertyUtil.getSchemaProperty(context, (String)itr.next()));
		}

		return vaultList;
			}


	//END:R208:PRG:Bug :374096
	/**
	 * This method returns true when Program is in Active States and Shows two Actions commands Create New Project & Create New Project concept
	 *  of program>>Categories>>Projects otherwise false.
	 * @param context the user context object for the current session
	 * @param args contains the parameter map.
	 * @throws Exception if the operation fails
	 */
	public boolean isProgramInactive(Context context, String args[]) throws MatrixException
	{
		boolean blAccess = true;
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String)inputMap.get("objectId");

			if(objectId != null && !objectId.equals("null") && !objectId.equals("")) {
				DomainObject progObj = DomainObject.newInstance(context, objectId);
				String type = progObj.getInfo(context, "type");
				if(type.equals(DomainObject.TYPE_PROGRAM)) {
					String currState = progObj.getInfo(context, "current");
					String sInactiveStateName = PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_PROGRAM,"state_Inactive");
					if(sInactiveStateName != null && sInactiveStateName.equals(currState)) {
						blAccess = false;
					}
				}
			}
		}
		catch(Exception e){
			throw new MatrixException(e);
		}
		return blAccess;
	}

	/**
	 *  This method returns vector containing name of vaults in which program is present.
	 *  @param context the ENOVIA <code>Context</code> object
	 * @param returns vector containing name of the vault in which program is present.
	 * @param args The arguments, it contains programMap
	 * @throws Exception if operation fails
	 */
	public Vector getProgramVaults(Context context,String[] args) throws MatrixException {
		Vector vProjectVaults = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			Map mpProgram;
			String strVault = DomainConstants.EMPTY_STRING;
			for(int i=0;i<objectList.size();i++)
			{
				mpProgram = (Map)objectList.get(i);
				strVault = (String)mpProgram.get(DomainConstants.SELECT_VAULT);
				vProjectVaults.add(strVault);
			}

		}catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return vProjectVaults;
	}
	/**
	 * This method returns true if there is Secondary Vault otherwise returns false.
	 *
	 * @param context
	 *       context object which is used while fetching data related application.
	 * @param args
	 *       Holds input argument.
	 * @return
	 *        true if there is secondary vault
	 * @throws MatrixException
	 *         Exception can be thrown in case of method fails to execute.
	 */

	public boolean hasSecondaryVaultInProgram(Context context, String[] args)
			throws MatrixException
			{ try{
				return ProgramCentralUtil.hasSecondaryVault(context,args);
			}catch(Exception e)
			{
				throw new MatrixException(e);
			}
			}
	
	/**
	 * This method returns 0 if the context user has DPM license.
	 * 
	 *@param context the eMatrix <code>Context</code> object.
	 * @param args holds the following input arguments
	 * @return 0 if context user has DPM license.
	 * @throws Exception if operation fails
	 * 
	 */

	public int triggerCheckLicense(Context context, String[] args) throws Exception {

		ComponentsUtil.checkLicenseReserved(context,ProgramCentralConstants.PRG_LICENSE_ARRAY);
		return 0;

	}
}
