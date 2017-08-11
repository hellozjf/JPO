/*
 * ${CLASSNAME}
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *
 */



import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeTemplate;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.matrixone.apps.common.BusinessUnit;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;




/**
 * The <code>ChangeTemplateBase</code> class contains methods for executing JPO operations related
 * to objects of the admin type  Change.
 * @author R3D
 * @version ECM R215- Copyright (c) 2012, Enovia MatrixOne, Inc.
 **
 */
public class enoECMChangeTemplateBase_mxJPO extends emxDomainObject_mxJPO  {




	/**
	 * Default Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @throws        Exception if the operation fails
	 * @since         ECM R215
	 **
	 */
	public enoECMChangeTemplateBase_mxJPO (Context context, String[] args) throws Exception {

		super(context, args);

	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @return        an integer status code (0 = success)
	 * @throws        Exception when problems occurred in the Common Components
	 * @since         ECM R215
	 **
	 */
	public int mxMain(Context context, String[] args)
			throws Exception
			{

		return 0;
			}

	/**
	 * To create the Change Template Object from Create Component
	 *
	 * @author R3D
	 * @param context the eMatrix code context object
	 * @param args packed hashMap of request parameter
	 * @return Map contains change object id
	 * @throws Exception if the operation fails
	 * @Since ECM R215
	 */

	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map create(Context context, String[] args) throws Exception {

		HashMap programMap   = (HashMap) JPO.unpackArgs(args);
		HashMap requestValue = (HashMap) programMap.get(ChangeConstants.REQUEST_VALUES_MAP);

		Map<String, String> returnMap     = new HashMap<String, String>();


		String sAutoNameChecked = (String) programMap.get("autoNameCheck");

		String changeId   = "";
		String sType 	  = (String) programMap.get("TypeActual");
		String sName 	  = (String) programMap.get("Name");
		String sPolicy    = (String) programMap.get("Policy");
		String sVault     = (String) programMap.get("Vault");

		boolean bAutoName = UIUtil.isNullOrEmpty(sAutoNameChecked)?false:true;

		try{

			ChangeTemplate change = new ChangeTemplate();
			changeId = change.create(context,sType,sName,sPolicy,sVault,bAutoName);

			//Code for Interface
			StringList sSelectList = new StringList();
			sSelectList.add(SELECT_NAME);
			sSelectList.add(SELECT_REVISION);
			sSelectList.add(SELECT_TYPE);

			DomainObject dmObj = new DomainObject(changeId);
			Map sInfoMap = dmObj.getInfo(context, sSelectList);

			String objName = (String)sInfoMap.get(SELECT_NAME);
			String objRevision = (String)sInfoMap.get(SELECT_REVISION);
			String objType = (String)sInfoMap.get(SELECT_TYPE);

			returnMap.put("id", changeId);

		}
		catch (Exception e){
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return returnMap;
	}//end of method

	/**
	 * @author R3D
	 * Updates the Owning Organization in CO WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CO object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R215
	 */
	public void connectOwningOrganization(Context context, String[] args) throws Exception {

		try {
			//unpacking the Arguments from variable args
			HashMap programMap 			= (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   			= (HashMap)programMap.get("paramMap");
			HashMap reqMap 				= (HashMap)programMap.get("requestMap");

			String[] modeCheck          = (String[]) reqMap.get("mode");
			String mode 				= modeCheck[0];
			String loggedInPersonId 	= PersonUtil.getPersonObjectID(context);

			String strNewToTypeObjId 	= (String)paramMap.get(ChangeConstants.NEW_OID);
			String currentCTObjectID 	= (String) paramMap.get("objectId");
			String[]  sAvailability    	= (String[])reqMap.get("Availability");
			String sAvailabilityOption 	= sAvailability[0];
			String user = context.getUser();

			DomainObject dmObj = DomainObject.newInstance(context);
			dmObj.setId(currentCTObjectID); //Template Object

			String relId = dmObj.getInfo(context,"to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].id");

			if("edit".equals(mode)){
				if("Enterprise".equals(sAvailabilityOption)){
					//ContextUtil.pushContext(context);
					DomainRelationship.disconnect(context, relId);
					DomainRelationship.connect(context, DomainObject.newInstance(context, strNewToTypeObjId), ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES, DomainObject.newInstance(context, currentCTObjectID));
					//ContextUtil.popContext(context);
				}
				else if("Personal".equals(sAvailabilityOption)){
					//ContextUtil.pushContext(context);
					DomainRelationship.disconnect(context, relId);
					dmObj.setOwner(context, user);
					DomainRelationship.connect(context, DomainObject.newInstance(context, loggedInPersonId), ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES, DomainObject.newInstance(context, currentCTObjectID));
					//ContextUtil.popContext(context);
				}
			}

			if("create".equals(mode)){
				if("Enterprise".equals(sAvailabilityOption)){
					DomainRelationship.connect(context, DomainObject.newInstance(context, strNewToTypeObjId), ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES, DomainObject.newInstance(context, currentCTObjectID));
				}
				else if("Personal".equals(sAvailabilityOption)){
					DomainRelationship.connect(context, DomainObject.newInstance(context, loggedInPersonId), ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES, DomainObject.newInstance(context, currentCTObjectID));
				}
			}


		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}



	/**
	 * Returns Program HTML to display Owning Organization in Change Template display table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Object of type Vector
	 * @throws Exception if the operation fails
	 * @since Common 10-0-0-0
	 * @grade 0
	 */
	public Vector showOwningOrganizationInStructureBrowser(Context context, String[] args)throws Exception {
		Vector owiningOrgList = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			HashMap paramMap   = (HashMap) programMap.get("paramList");
			String strLang     = (String) paramMap.get("languageStr");
			DomainObject obj = DomainObject.newInstance(context);

			String strAvailability    = "";
			String objectID = "";
			String strOwningOrgType ="";
			String strOwningOrgName ="";
			String strOwningOrgID ="";

			StringList selectList = new StringList();
			selectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
			selectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
			selectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

			owiningOrgList = new Vector(objectList.size());
			Iterator objectListItr    = objectList.iterator();

			while( objectListItr.hasNext() )
			{
				Map objectMap = (Map) objectListItr.next();
				objectID = (String)objectMap.get("id");
				if(!UIUtil.isNullOrEmpty(objectID)){
					obj.setId(objectID);
					Map sResultMap = obj.getInfo(context, selectList);

					if(sResultMap != null)
					{
						strOwningOrgType = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
						strOwningOrgID = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
						strOwningOrgName = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

						obj.setId(strOwningOrgID);

						if(obj.isKindOf(context, TYPE_ORGANIZATION))
						{
							owiningOrgList.add( strOwningOrgName );
						}
						else
						{
							owiningOrgList.add("");
						}
					}
				}


			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return owiningOrgList;
	}//end of method


	/**
	 * This method shows the Owning Organization in Change Template Properties/Edit Page
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] of ObjectIds.
	 * @return          Object containing CO objects
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public String showOwningOrganization(Context context, String[] args) throws Exception{

		HashMap programMap         = (HashMap) JPO.unpackArgs(args);
		Map requestMap             = (Map) programMap.get("requestMap");
		Map paramMap               = (Map) programMap.get("paramMap");

		String objectId            = (String) requestMap.get("objectId");
		String mode                = (String) requestMap.get("mode");
		StringBuffer sb            = new StringBuffer();

		String sConnectedType 	   = "";
		String sConnectedID   	   = "";
		String sConnectedName 	   = "";
		String sOwningOrg  	  	   = "";

		DomainObject changeTemplateObj = DomainObject.newInstance(context);

		StringList sSelectList = new StringList();
		sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
		sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
		sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

		if(!UIUtil.isNullOrEmpty(objectId)){

			changeTemplateObj.setId(objectId);
			Map sResultMap = changeTemplateObj.getInfo(context, sSelectList);

			sConnectedID   = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
			sConnectedType = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
			sConnectedName = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

			changeTemplateObj.setId(sConnectedID);

			//view mode
			if("view".equals(mode)){

				if(changeTemplateObj.isKindOf(context, TYPE_PERSON)){
					sOwningOrg = "";
				}else{
					sOwningOrg = sConnectedName;
				}
				sb.append(sOwningOrg);
			}

			//edit mode
			if("edit".equals(mode)){
				if(changeTemplateObj.isKindOf(context, TYPE_PERSON)){
					sOwningOrg = "";
				}else{
					sOwningOrg = sConnectedName;
				}
				sb.append(sOwningOrg);
			}
		}
		return sb.toString();
	}//end of method

	/**
	 *Returns Program HTML to display Availability in Change Template display table
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @returns Object of type Vector
	 * @throws Exception if the operation fails
	 * @since Common 10-0-0-0
	 * @grade 0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector showTemplateAvailabilityInStructureBrowser(Context context, String[] args)throws Exception {
		//XSSOK
		Vector availabilityList = new Vector();
		try {
			HashMap programMap 		  = (HashMap) JPO.unpackArgs(args);
			MapList objectList 		  = (MapList) programMap.get("objectList");
			HashMap paramMap          = (HashMap) programMap.get("paramList");

			DomainObject obj 		  = DomainObject.newInstance(context);
			String strLang            = (String) paramMap.get("languageStr");


			String strLabelUser       = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.ChangeTemplate.Personal");
			String strLabelEnterprise = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.ChangeTemplate.Enterprise");

			String strAvailability    = "";
			String objectID 		  = "";
			String strConnectedId 	  = "";
			String strConnectedType   = "";
			String strConnectedName   = "";

			StringList selectList = new StringList();
			selectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
			selectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
			selectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

			availabilityList = new Vector(objectList.size());
			Iterator objectListItr    = objectList.iterator();
			while( objectListItr.hasNext() ) {
				Map objectMap = (Map) objectListItr.next();
				objectID = (String)objectMap.get("id");
				if(!UIUtil.isNullOrEmpty(objectID)){
					obj.setId(objectID);
					Map sResultMap   = obj.getInfo(context, selectList);

					strConnectedId   = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
					strConnectedType = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
					strConnectedName = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

					obj.setId(strConnectedId);

					if(obj.isKindOf(context, TYPE_PERSON)){
						strAvailability = strLabelUser + " : " + strConnectedName;
					} else{
						strAvailability = strLabelEnterprise + " : " + strConnectedName;
					}

					availabilityList.add( strAvailability );
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return availabilityList;
	}//end of method

	/**
	 * This method gets the Availability of Change Template on Change Template creation & properties page
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] of ObjectIds.
	 * @return          Object containing CO objects
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public String showChangeTemplateAvailability(Context context, String[] args) throws Exception{

		//XSSOK
		HashMap programMap         = (HashMap) JPO.unpackArgs(args);
		Map requestMap             = (Map) programMap.get("requestMap");
		Map paramMap               = (Map) programMap.get("paramMap");

		String strLanguage         = (String)requestMap.get("languageStr");
		String objectId     	   = (String) requestMap.get("objectId");
		String mode                = (String) requestMap.get("mode");

		StringBuffer sb            = new StringBuffer();
		String strPersonal = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.ChangeTemplate.Personal");
		String strEnterprise = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.ChangeTemplate.Enterprise");
		
		StringBuffer sbEnterprise            = new StringBuffer();
		sbEnterprise.append("<input type=\"radio\" name=\"Availability\" value=\"Personal\" onclick=\"disableOrganizationField(this)\"></input>");
		sbEnterprise.append(strPersonal);
		sbEnterprise.append("<br></br>");
		sbEnterprise.append("<input type=\"radio\" name=\"Availability\" value=\"Enterprise\" checked = \"checked\" onclick=\"disableOrganizationField(this)\"></input>");
		sbEnterprise.append(strEnterprise);

		StringBuffer sbPersonal            = new StringBuffer();
		sbPersonal.append("<input type=\"radio\" name=\"Availability\" value=\"Personal\" checked = \"checked\"></input>");
		sbPersonal.append(strPersonal);

		String sConnectedType = "";
		String sConnectedID   = "";
		String sConnectedName = "";
		String sAvailability  = "";

		
		boolean isChangeAdmin = ChangeUtil.hasChangeAdministrationAccess(context);

		DomainObject changeTemplateObj = DomainObject.newInstance(context);

		StringList sSelectList = new StringList();
		sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
		sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");
		sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");

		Map sResultMap = new HashMap();

		//create mode
		if(mode == null){
			if(isChangeAdmin){
				sb.append(sbEnterprise);
			}
			else{
				sb.append(sbPersonal);
			}

		}
		if(!UIUtil.isNullOrEmpty(objectId)){
			changeTemplateObj.setId(objectId);
			sResultMap = changeTemplateObj.getInfo(context, sSelectList);

			sConnectedType = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
			sConnectedName = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");
			sConnectedID   = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");

			changeTemplateObj.setId(sConnectedID); //Organization/Person Object depending on connected TYPE

			//view mode
		if ("view".equals(mode)){
				if(changeTemplateObj.isKindOf(context,TYPE_PERSON)){
					sAvailability = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.ChangeTemplate.Personal" );
					sConnectedName = PersonUtil.getFullName(context,sConnectedName);
				}else{
					sAvailability = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.ChangeTemplate.Enterprise");

				}
				sb.append(sAvailability);
				sb.append(": ");
				sb.append(sConnectedName);
			}
			//edit mode
				if("edit".equals(mode)){
				if(isChangeAdmin){
					if(changeTemplateObj.isKindOf(context,TYPE_PERSON)){
						sb.append("<input type=\"radio\" name=\"Availability\" value=\"Personal\" checked = \"checked\" onclick=\"disableOrganizationField(this)\"></input>");
						sb.append(strPersonal);
						sb.append("<br></br>");

						sb.append("<input type=\"radio\" name=\"Availability\" value=\"Enterprise\" onclick=\"disableOrganizationField(this)\"></input>");
						sb.append(strEnterprise);

					}
					else{
						sb.append(sbEnterprise);
					}
				}
				else{
					sb.append(sbPersonal);
				}

			}

		}

		return sb.toString();
	}//end of method


	/**
	 * This method gets the Change Order Default type to show on Create Template Web Form
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] of ObjectIds.
	 * @return          Object containing CO objects
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public String getChangeOrderDefaultType(Context context, String[] args) throws Exception{
		return ChangeConstants.TYPE_CHANGE_ORDER;
	}//end of method


	/**
	 * Checks the view mode of the web form display.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a HashMap containing the following entries:
	 * mode - a String containing the mode.
	 * @return Object - boolean true if the mode is view
	 * @throws Exception if operation fails
	 * @since ECM R215
	 */
	public Object checkViewMode(Context context, String[] args)throws Exception{

		@SuppressWarnings("rawtypes")
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strMode = (String) programMap.get("mode");
		Boolean isViewMode = Boolean.valueOf(false);

		// check the mode of the web form.
		if( (strMode == null) || (strMode != null && ("null".equals(strMode) || "view".equalsIgnoreCase(strMode) || "".equals(strMode))) ){
			isViewMode = Boolean.valueOf(true);
		}

		return isViewMode;
	}//end of method


	/**
	 * Program to display the Owning Organization based on the Person's Organization
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments
	 * @return        a <code>MapList</code> object having the list of Change Templates
	 * @throws        Exception if the operation fails
	 * @since         ECM R215
	 **
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getOwningOrganizations(Context context, String[] args) throws Exception{

		if (args.length == 0 ){
			throw new IllegalArgumentException();
		}

		StringList includeOIDList = new StringList();
		StringList sPersonOrganization = new StringList();
		Set setOrganization = new HashSet();

		try{
			DomainObject dmObj = DomainObject.newInstance(context);
			String loggedInPersonId = PersonUtil.getPersonObjectID(context);

			StringBuffer sSelectOrganization = new StringBuffer("from[Assigned Security Context].to.from[");
			sSelectOrganization.append(RELATIONSHIP_SECURITYCONTEXT_ORGANIZATION);
			sSelectOrganization.append("].to.id");

			dmObj.setId(loggedInPersonId); //Person Object
			sPersonOrganization = dmObj.getInfoList(context, sSelectOrganization.substring(0));


			for(int index=0;index<sPersonOrganization.size();index++) {
				String sOrganizationID = (String) sPersonOrganization.get(index);
				setOrganization.add(sOrganizationID);
			}
				Iterator organazationItr = setOrganization.iterator();
				while(organazationItr.hasNext()) {
				String sPersonOrgID = (String)organazationItr.next();
				includeOIDList.add(sPersonOrgID);
			}
		}
		catch (Exception ex){
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return includeOIDList;
	}//end of method

	/**
	 * Include Program to display the Responsible Organization below and above
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments
	 * @return        a <code>MapList</code> object having the list of Change Templates
	 * @throws        Exception if the operation fails
	 * @since         ECM R215
	 **
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getResponsibleOrganizations(Context context, String[] args) throws Exception{

		StringList includeOIDList = new StringList();
		HashSet sFinalOrgSet = new HashSet();
		MapList sOrganizationList = new MapList();

		try{

			if (args.length == 0 ){
				throw new IllegalArgumentException();
			}

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			StringList selectList = new StringList();
			selectList.add(SELECT_ID);
			Person person = Person.getPerson(context);

			String loggedInPersonId = PersonUtil.getPersonObjectID(context);
			DomainObject dmObj = DomainObject.newInstance(context);
			dmObj.setId(loggedInPersonId);
			String loggedInPersonName=dmObj.getInfo(context,DomainConstants.SELECT_NAME);
			String orgName= PersonUtil.getDefaultOrganization(context, loggedInPersonName);
			
			DomainObject hostCompanyObj = DomainObject.newInstance(context,Company.getHostCompany(context));
			String hostCompany = hostCompanyObj.getInfo(context, DomainConstants.SELECT_NAME);	             
			
				
			String orgId = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump $5",DomainConstants.TYPE_ORGANIZATION,orgName,"*","id","|"); 	        
			orgId = orgId.substring(orgId.lastIndexOf('|')+1);
			//If Person is an BU Employee
			if(!UIUtil.isNullOrEmpty(hostCompany) && !(hostCompany.equalsIgnoreCase(orgName))){
		
				String	sPersonBU=orgId;			
				sFinalOrgSet.add(sPersonBU);
				//Getting Business Units and Departments of this Organization
				Company sBUObj = new Company(sPersonBU);
				MapList sOrgList = sBUObj.getBusinessUnitsAndDepartments(context, 0, selectList, false);

				Iterator sOrgItr = sOrgList.iterator();
				while(sOrgItr.hasNext()){
					Map sTempMap = (Map)sOrgItr.next();
					String sOrgID = (String)sTempMap.get(SELECT_ID);
					sFinalOrgSet.add(sOrgID);
				}

				//Getting the Parent Organizations of this Organization
				BusinessUnit sBusinessobj = new BusinessUnit(sPersonBU);
				MapList sParentOrgList = sBusinessobj.getParentInfo(context, 0, selectList);

				Iterator sParentOrgItr = sParentOrgList.iterator();
				while(sParentOrgItr.hasNext()){
					Map sTempMap = (Map)sParentOrgItr.next();
					String sParentBUID = (String)sTempMap.get(SELECT_ID);
					sFinalOrgSet.add(sParentBUID);
				}
			}
			//If the Person is at Host Company Level
			else{
				dmObj.setId(orgId);
				sFinalOrgSet.add(orgId);

				sOrganizationList = dmObj.getRelatedObjects(context,
						RELATIONSHIP_DIVISION+","
								+RELATIONSHIP_COMPANY_DEPARTMENT,
								TYPE_ORGANIZATION,
								selectList,
								null,
								false,
								true,
								(short)0,
								EMPTY_STRING,
								EMPTY_STRING,
								null,
								null,
								null);


				Iterator sItr = sOrganizationList.iterator();
				while(sItr.hasNext()){
					Map sTempMap = (Map)sItr.next();
					String sOrgID = (String)sTempMap.get(SELECT_ID);
					sFinalOrgSet.add(sOrgID);
				}
			}

			//Iterating final return list
			Iterator itr = sFinalOrgSet.iterator();
			while(itr.hasNext()){
				String id = (String)itr.next();
				includeOIDList.add(id);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return includeOIDList;
	}//end of method

	/**
	 * Program to update Default Type field Change Template creation page
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  HashMap containing one String entry for key "objectId"
	 * @return        a <code>void</code>
	 * @throws        Exception if the operation fails
	 * @since         ECM R215
	 **
	 */

	public void updateDefaultType(Context context, String[] args)throws Exception{
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			HashMap fieldMap = (HashMap)programMap.get("fieldMap");
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String objectId = (String)paramMap.get("objectId");
			String sType = (String)paramMap.get("New Value");

			DomainObject dmObj = DomainObject.newInstance(context);
			dmObj.setId(objectId);
			dmObj.setAttributeValue(context, ChangeConstants.ATTRIBUTE_DEFAULT_TYPE, sType);

		}
		catch(Exception e){
			e.printStackTrace();
			throw new FrameworkException(e);
		}
	}//end of method



	/**
	 * Reloads the Owning Organization field on Create Change Templates Web-Form
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  String containing one String entry for key "objectId" of current Organization
	 * @return        a <code>HashMap</code> object Id of Organization.
	 * @throws        Exception if the operation fails
	 * @since         ECM R215
	 **
	 */
	public HashMap reloadOrganizationField(Context context,String[] args)throws FrameworkException{

		HashMap returnMap = new HashMap();
		try{
			HashMap hmProgramMap = (HashMap) JPO.unpackArgs(args);
			HashMap fieldValues = (HashMap) hmProgramMap.get( "fieldValues" );

			String owningOrg = (String)fieldValues.get("OwningOrganization");

			returnMap.put("SelectedValues", owningOrg);
			returnMap.put("SelectedDisplayValues", owningOrg);
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}

		return returnMap;
	}//end of method


	/**
	 * Returns the HTML based Edit Icon in the StructureBrowser
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Vector attachment as HTML
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public Vector showEditIconforStructureBrowser(Context context, String args[])throws FrameworkException{
		try{
			//XSSOK
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Vector columnVals = showEditIconforStructureBrowser(context, programMap);
			if(columnVals.size()!=0){
				return columnVals;
			}
			else{
				return new Vector();
			}
		} catch (Exception e){
			throw new FrameworkException(e);
		}
	}


	/**
	 * Returns the HTML based Edit Icon in the StructureBrowser
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Vector attachment as HTML
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public Vector showEditIconforStructureBrowser(Context context, java.util.HashMap arguMap)throws FrameworkException{

		//XSSOK
		Vector columnVals = null;

		try {

			MapList objectList = (MapList) arguMap.get("objectList");
			StringBuffer sbEditIcon = null;
			DomainObject dmObj = DomainObject.newInstance(context);


			boolean isChangeAdmin = ChangeUtil.hasChangeAdministrationAccess(context);
			String orgId = PersonUtil.getUserCompanyId(context);
			String loggedInPersonId = PersonUtil.getPersonObjectID(context);

			boolean isBUEmployee = false;

			Company companyObj = new Company();

			StringList sSelectList = new StringList();
			sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
			sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");
			sSelectList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");

			String sConnectedType = "";
			String sConnectedID = "";
			String sConnectedName ="";
			String sOwner ="";

			StringBuffer sbStartHref = new StringBuffer();
			sbStartHref.append("<a href=\"JavaScript:emxTableColumnLinkClick('");
			sbStartHref.append("../common/emxForm.jsp?formHeader=Edit Change Template&amp;mode=edit");
			sbStartHref.append("&amp;preProcessJavaScript=setOwningOrganization&amp;HelpMarker=emxhelpparteditdetails&amp;commandName=ECMMyChangeTemplates&amp;refreshStructure=false&amp;postProcessURL=../enterprisechangemgt/ECMCommonRefresh.jsp&amp;suiteKey=EnterpriseChangeMgt&amp;objectId=");

			StringBuffer sbEndHref = new StringBuffer();
			sbEndHref.append("&amp;form=type_ChangeTemplate'");
			sbEndHref.append(", '700', '600', 'true', 'slidein', '')\">");
			sbEndHref.append("<img border=\"0\" src=\"../common/images/iconActionEdit.gif\" title=\"Edit Change Template\"/></a>");

			int listSize = 0;
			if (objectList != null && (listSize = objectList.size()) > 0) {
				columnVals = new Vector(objectList.size());
				Map sTempMap = new HashMap();

				Iterator objectListItr    = objectList.iterator();
				while( objectListItr.hasNext()){
					Map objectMap           = (Map) objectListItr.next();
					String objectID = (String)objectMap.get("id");
					sbEditIcon = new StringBuffer();

					dmObj.setId(objectID);

					Map sResultMap = dmObj.getInfo(context, sSelectList);
					sConnectedType = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
					sConnectedName = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");
					sConnectedID = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");

					dmObj.setId(loggedInPersonId);
					String sPersonBUID = dmObj.getInfo(context,"to["+RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE+"].from.id");

					if(sConnectedType.equals(TYPE_PERSON)){

						if(sOwner.equals(context.getUser()) || sConnectedName.equals(context.getUser())){
							sbEditIcon = new StringBuffer(sbStartHref);
							sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectID));
							sbEditIcon.append(sbEndHref);
						}
					}
					if(!UIUtil.isNullOrEmpty(sPersonBUID)){
						isBUEmployee = true;
						companyObj.setId(sPersonBUID);
						sTempMap.put("id",sPersonBUID);
					}
					if(!isBUEmployee){
						companyObj.setId(orgId);
						sTempMap.put("id",orgId);
					}

					MapList sList = companyObj.getBusinessUnitsAndDepartments(context, 0, new StringList(SELECT_ID), false);
					sList.add(sTempMap);
					Iterator sItr = sList.iterator();

					while(sItr.hasNext()){
						Map sMap = (Map)sItr.next();
						boolean sContains = sMap.containsValue(sConnectedID);
						if(sContains){
							if(isChangeAdmin){
								sbEditIcon = new StringBuffer(sbStartHref);
								sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectID));
								sbEditIcon.append(sbEndHref);
							}
						}
					}
					columnVals.add(sbEditIcon.toString());
				}//end while

			}//end if
			return columnVals;
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}//end of method


	/**
	 * Include Program for Change Template upward usage
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  HashMap containing one String entry for key "objectId"
	 * @return        a <code>StringList</code> object having the list of Change Templates, Object Id of Change Template objects.
	 * @throws        Exception if the operation fails
	 * @since         ECM R215
	 **
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getUsageTemplates(Context context, String[] args) throws Exception{

		if (args.length == 0 ){
			throw new IllegalArgumentException();
		}
		HashSet sFinalSet = new HashSet();
		HashSet sFinalOrgSet = new HashSet();
		MapList sTemplateList = new MapList();
		StringList includeOIDList = new StringList();
		DomainObject dmObj = DomainObject.newInstance(context);
		try{
			String objectId = "";

			//Select statement for Change Templates
			StringBuffer strSelect = new StringBuffer("from[");
			strSelect.append(ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES);
			strSelect.append("].to.id");

			//Select Member Organizations statement
			StringBuffer selectMemberOrg = new StringBuffer("to[");
			selectMemberOrg.append(RELATIONSHIP_MEMBER);
			selectMemberOrg.append("].from.id");

			
			String orgId = PersonUtil.getUserCompanyId(context);
			String loggedInPersonId = PersonUtil.getPersonObjectID(context);
			sFinalOrgSet.add(loggedInPersonId); //In order to get Personal Templates

			//Check if the context user has Change Administrator role
			boolean isChangeAdmin = ChangeUtil.hasChangeAdministrationAccess(context);

			//Getting Member Organizations Object ID
			dmObj.setId(loggedInPersonId); //Person Object
			StringList sMemberOrgList = dmObj.getInfoList(context, selectMemberOrg.substring(0));

			Iterator sItr = sMemberOrgList.iterator();

			while(sItr.hasNext()){
				String sMemberOrgId = (String)sItr.next();
				sFinalOrgSet.add(sMemberOrgId); //To get Templates of current Organization

				//Getting Parent Organizations of this Organization
				DomainObject sBUObj = new DomainObject(sMemberOrgId);
				MapList sOrgList = sBUObj.getRelatedObjects(
														context,                // matrix context
														RELATIONSHIP_DIVISION+","+RELATIONSHIP_COMPANY_DEPARTMENT,  // relationship pattern
								                        "*",                    // object pattern
								                        new StringList(SELECT_ID),             // object selects
								                        EMPTY_STRINGLIST,       // relationship selects
								                        true,                   // to direction
								                        false,                  // from direction
								                        (short) 0,          // recursion level
								                        EMPTY_STRING,           // object where clause
								                        EMPTY_STRING,          // relationship where clause
								                        0);

				Iterator sOrgItr = sOrgList.iterator();
				while(sOrgItr.hasNext()){
					Map tempMap = (Map)sOrgItr.next();
					objectId = (String)tempMap.get(SELECT_ID);
					sFinalOrgSet.add(objectId);
				}

			}

			String[] arrObjectIDs = (String[])sFinalOrgSet.toArray(new String[0]);

			//Getting Templates connected to each Organization & Person
			sTemplateList = DomainObject.getInfo(context, arrObjectIDs, new StringList(strSelect.substring(0)));

			Iterator sTempItr = sTemplateList.iterator();
			while(sTempItr.hasNext()){
				Map tempMap = (Map)sTempItr.next();
				objectId = (String)tempMap.get(strSelect.substring(0));
				if(!UIUtil.isNullOrEmpty(objectId)){
					StringList sSplitList = FrameworkUtil.split(objectId,"\7");
					sFinalSet.addAll(sSplitList);

				}

			}

			Iterator sFinalItr = sFinalSet.iterator();
			while(sFinalItr.hasNext()){
				objectId = (String)sFinalItr.next();
				includeOIDList.add(objectId);
			}


		}
		catch(Exception e){
			throw new FrameworkException(e);
		}

		return includeOIDList;
	}//end of method


	/**
	 * Checks the view mode of the web form display.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a HashMap containing the following entries:
	 * mode - a String containing the mode.
	 * @return Object - boolean true if the mode is view
	 * @throws Exception if operation fails
	 * @since ECM R215
	 */

	public Object checkAccessForEdit(Context context, String[] args)throws Exception{


		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String strMode = (String) programMap.get("mode");
		Boolean isViewMode = Boolean.valueOf(false);



		boolean isChangeAdmin = ChangeUtil.hasChangeAdministrationAccess(context);
		String orgId = PersonUtil.getUserCompanyId(context);

		String loggedInPersonId = PersonUtil.getPersonObjectID(context);
		Company companyObj = new Company();

		String objectId = (String)programMap.get("objectId");
		DomainObject dmObj = DomainObject.newInstance(context);
		dmObj.setId(objectId); //Template Object

		Map sTempMap = new HashMap();

		StringList selectStrList = new StringList();
		selectStrList.add(SELECT_TYPE);
		selectStrList.add(SELECT_ID);
		selectStrList.add(SELECT_OWNER);
		selectStrList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
		selectStrList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");
		selectStrList.add("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");

		Map sResultMap = dmObj.getInfo(context,selectStrList);
		String sOwner = (String)sResultMap.get(SELECT_OWNER);
		String sConnectedType = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.type");
		String sConnectedName = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.name");
		String sConnectedID = (String)sResultMap.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_TEMPLATES+"].from.id");


		dmObj.setId(loggedInPersonId); //Person Object
		String sPersonBUID = dmObj.getInfo(context,"to["+RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE+"].from.id");
		boolean isBUEmployee = false;


		dmObj.setId(sConnectedID);

		if(dmObj.isKindOf(context,TYPE_PERSON)){
			if(sOwner.equals(context.getUser())){
				isViewMode = Boolean.valueOf(true);
			}
		}

		if(!UIUtil.isNullOrEmpty(sPersonBUID)){
			isBUEmployee = true;
		}
		if(isBUEmployee){
			companyObj.setId(sPersonBUID);
			sTempMap.put("id",sPersonBUID);
		}
		else{
			companyObj.setId(orgId);
			sTempMap.put("id",orgId);
		}
		MapList sList = companyObj.getBusinessUnitsAndDepartments(context, 0, new StringList(SELECT_ID), false);
		sList.add(sTempMap);

		Iterator sItr = sList.iterator();
		while(sItr.hasNext()){
			Map sMap = (Map)sItr.next();
			boolean sContains = sMap.containsValue(sConnectedID);
			if(sContains){
				if(isChangeAdmin){
					isViewMode = Boolean.valueOf(true);
				}
			}
		}
		return isViewMode;
	}//end of method





	////////////////////////////CO-CA Search Method//////////////////////////////////////////////

	/**
	 * Returns the All Change Template Object Name in the CO search
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          String containing Change Templates Name
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public String getCOBasedOnChangeTemplate(Context context, String[] args) throws Exception{

		try{
			String objId = args[0];
			String strCO = "";
			StringBuffer select = new StringBuffer("to[");
			select.append(ChangeConstants.RELATIONSHIP_CHANGE_INSTANCE);
			select.append("].from.name");

			DomainObject dmObj = DomainObject.newInstance(context);
			dmObj.setId(objId);

			strCO = dmObj.getInfo(context, select.substring(0));
			return strCO;

		}
		catch(Exception e){
			throw new FrameworkException(e);
		}


	}

	//CO -Organization Search
	/**
	 * Returns the Responsible Organization for CA
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          String RO
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public String getResponsibleOrganizationSearch(Context context, String[] args) throws Exception{


		try{
			String objId = args[0];
			String sRBO = "";
			
			DomainObject dmObj = DomainObject.newInstance(context);
			dmObj.setId(objId);

			sRBO = dmObj.getInfo(context, SELECT_ORGANIZATION);
			return sRBO;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}

	}

	//CO -Organization Search
	/**
	 * Returns the All Responsible Organization in the CO search
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          String RO
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public static Map getAllOrganizationValues(Context context, String args[])throws Exception{

		try{
			Map argMap = (HashMap)JPO.unpackArgs(args);

			String selectable = "";

			StringList strList = new StringList();
			strList.add(SELECT_NAME);
			strList.add(SELECT_TYPE);
			strList.add(SELECT_ID);
			strList.add(selectable);

			MapList mlOrganization = DomainObject.findObjects(context, TYPE_COMPANY, "*", null, strList);


			Map returnMap = new HashMap();
			Iterator itrPersons = mlOrganization.iterator();
			while(itrPersons.hasNext()){
				Map mapPerson = (HashMap)itrPersons.next();
				String person = (String)mapPerson.get(SELECT_NAME);
				returnMap.put(person,person);

			}


			return returnMap;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * Returns the All Change Template Object Name in the CO search program
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Map containing Change Templates Name
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public static Map getAllChangeTemplatesSearch(Context context, String args[])throws Exception{

		try{
			Map argMap = (HashMap)JPO.unpackArgs(args);

			String user = context.getUser();
			String userId = PersonUtil.getPersonObjectID(context);

			String selectable = "";

			StringList strList = new StringList();
			strList.add(SELECT_NAME);
			strList.add(SELECT_TYPE);
			strList.add(SELECT_ID);
			strList.add(selectable);

			String sWhere   = "owner == '" + context.getUser() + "'";

			//Display Change Templates owned by the context user or based on Availability.
			MapList mlOrganization = DomainObject.findObjects(context, ChangeConstants.TYPE_CHANGETEMPLATE, "*", sWhere, strList);


			Map returnMap = new HashMap();
			Iterator itrPersons = mlOrganization.iterator();
			while(itrPersons.hasNext()){
				Map mapPerson = (HashMap)itrPersons.next();
				String person = (String)mapPerson.get(SELECT_NAME);
				returnMap.put(person,person);

			}

			return returnMap;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}//end of method
	/**
	 * Access Method to show or hide type command on Add existing web page
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          boolean output
	 * @throws          Exception if the operation fails
	 * @since           ECM R216
	 */
	public boolean ShowTypeFilterAttributes(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String submitURL = (String)programMap.get("submitURL");
		String functionality = submitURL.substring((submitURL.indexOf("functionality")+"functionality".length()+1), submitURL.length());
		if(UIUtil.isNotNullAndNotEmpty(functionality)&&"addAttributeGroup".equals(functionality)){
			return false;
		}
		return true;
	}

/**
	 * Returns all the attributes associated with Change Template's Interface
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Map containing Change Templates Name
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getInterfaceAttributes(Context context, String[] args)
			throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strCharSet =  (String )programMap.get("charSet");
			String objectId = (String) programMap.get("parentOID"); //Change Template object Id
			if(null == strCharSet ) {
				strCharSet = "UTF-8";
			}
			String result = "";
			MQLCommand cmd = new MQLCommand();
			MapList returnList = new MapList();

			try {
                if (cmd.executeCommand(context, "print bus $1 select $2 dump $3",objectId,"interface.attribute",",")) {
					result = cmd.getResult();
					if ((result != null) && !(result.equals(""))) {
						returnList = new MapList();
						StringTokenizer tokens = new StringTokenizer(result
								.trim(), ",");
						String attrName = "";
						while (tokens.hasMoreTokens()) {
							HashMap objectMap = new HashMap();
							attrName = tokens.nextToken();
							if (attrName != null && !attrName.trim().equals("")) {
								objectMap.put("id", attrName.trim());
								returnList.add(objectMap);
							}

						}
					} else {
						throw new Exception(cmd.getError());
					}
				}
			} catch (Exception ex) {
				throw ex;
			}
			return returnList;
		} catch (Exception ex) {
			throw ex;
		}
	}//end of method

	/**
	 * Returns all the attributes associated with Change Template's Interface
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Map containing Change Templates Name
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public Vector getAttributeName(Context context, String[] args)
			throws Exception {
		//XSSOK
		Vector columnValues = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			HashMap map;
			String strLanguageStr = ((String) ((HashMap) programMap.get("paramList")).get("languageStr"));
			Iterator itr = objList.iterator();
			while (itr.hasNext()) {
				map = (HashMap) itr.next();
				AttributeType att = new AttributeType((String) map.get("id"));
				StringBuffer strI18nAttributeName = new StringBuffer();
				strI18nAttributeName.append("<img align=\"top\" SRC=\"images/iconSmallAttribute.gif\"></img><span class='object'>");
				strI18nAttributeName.append(i18nNow.getAttributeI18NString(
						att.getName(), strLanguageStr));
				strI18nAttributeName.append("</span>");
				columnValues.addElement(strI18nAttributeName.toString());
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		return columnValues;
	}
	/**
	 * Returns the Attribute Name in the StructureBrowser for Attribute Extension
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Vector attachment as HTML
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public Vector getAttributeType(Context context, String[] args)
			throws Exception {
		//XSSOK
		Vector columnValues = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			HashMap map;
			String strLanguageStr = ((String) ((HashMap) programMap
					.get("paramList")).get("languageStr"));
			Iterator itr = objList.iterator();
			while (itr.hasNext()) {
				map = (HashMap) itr.next();
				AttributeType att = new AttributeType((String) map.get("id"));
				att.open(context);
				String strI18nAttributeType = i18nNow.getAttributeTypeI18NString(context,att.getName(),strLanguageStr);
				columnValues.addElement(strI18nAttributeType);
				att.close(context);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception(ex.getMessage());
		}
		return columnValues;
	}
	/**
	 * Returns the Attribute Description in the StructureBrowser for Attribute Extension
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Vector attachment as HTML
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public Vector getAttributeDescription(Context context, String[] args)
			throws Exception {
		//XSSOK
		Vector columnValues = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			HashMap map;
			Iterator itr = objList.iterator();
			while (itr.hasNext()) {
				map = (HashMap) itr.next();
				AttributeType att = new AttributeType((String) map.get("id"));
				att.open(context);
				columnValues.addElement(att.getDescription());
				att.close(context);
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		return columnValues;
	}
	/**
	 * Returns the Attribute Default Value in the StructureBrowser for Attribute Extension
	 *
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args      holds input arguments.
	 * @return          Vector attachment as HTML
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	public Vector getAttributeDefaultValue(Context context, String[] args)
			throws Exception {
		//XSSOK
		Vector columnValues = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			HashMap map;
			Iterator itr = objList.iterator();
			while (itr.hasNext()) {
				map = (HashMap) itr.next();
				AttributeType att = new AttributeType((String) map.get("id"));
				att.open(context);
				columnValues.addElement(att.getDefaultValue());
				att.close(context);
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		return columnValues;
	}

	/**
	 * This method Creates  range values for the Attribute Type in Attribute Search Criteria
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments
	 *        0 - launguagestr
	 * @throws Exception if the operation fails
	 * @since ECM R212
	 */

	public HashMap getAttributeTypes(Context context, String[] args)
			throws Exception
			{
		HashMap rangeMap            = new HashMap();
		try
		{
			HashMap programMap              = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap              = (HashMap)programMap.get("requestMap");
			String languagestr              = (String)requestMap.get("languageStr");
			String frameworkI18NResourceBundle  = "emxFrameworkStringResource";

			String attrTypeFilterBoolean    = EnoviaResourceBundle.getProperty(context, frameworkI18NResourceBundle, context.getLocale(),"emxFramework.Attribute.Type.boolean");

			String attrTypeFilterTimestamp  = EnoviaResourceBundle.getProperty(context, frameworkI18NResourceBundle, context.getLocale(),"emxFramework.Attribute.Type.timestamp");

			String attrTypeFilterInteger    = EnoviaResourceBundle.getProperty(context, frameworkI18NResourceBundle, context.getLocale(),"emxFramework.Attribute.Type.integer");

			String attrTypeFilterReal       = EnoviaResourceBundle.getProperty(context, frameworkI18NResourceBundle, context.getLocale(),"emxFramework.Attribute.Type.real");

			String attrTypeFilterString     = EnoviaResourceBundle.getProperty(context, frameworkI18NResourceBundle, context.getLocale(),"emxFramework.Attribute.Type.string");

			String all                      = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.AttributeType.All");

			StringList fieldChoices         = new StringList();
			StringList fieldDisplayChoices  = new StringList();
			fieldChoices.add("*");
			fieldChoices.add("boolean");
			fieldChoices.add("timestamp");
			fieldChoices.add("integer");
			fieldChoices.add("real");
			fieldChoices.add("string");

			fieldDisplayChoices.add(all);
			fieldDisplayChoices.add(attrTypeFilterBoolean);
			fieldDisplayChoices.add(attrTypeFilterTimestamp);
			fieldDisplayChoices.add(attrTypeFilterInteger);
			fieldDisplayChoices.add(attrTypeFilterReal);
			fieldDisplayChoices.add(attrTypeFilterString);

			rangeMap.put("field_choices", fieldChoices);
			rangeMap.put("field_display_choices", fieldDisplayChoices);
		}
		catch(Exception ex)
		{
			throw new FrameworkException(ex.toString());
		}
		return rangeMap;
			}


	/**
	 * This method returns MapList of Attributes  based on the Search Criteria
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments
	 * @throws Exception if the operation fails
	 * @since ECM R212
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAttributeList(Context context, String[] args)
			throws Exception {
		MapList mlAttributes        = new MapList();
		MapList mlRelatedAttributes = new MapList();
		MapList mlResutList         = new MapList();
		HashMap hmAttribute         = null;
		String strAttributeName     = "";

		HashMap objectMap;
		Iterator itr;
		try {

			HashMap inputMap        = (HashMap)JPO.unpackArgs(args);
			String strDoFilter      = (String)inputMap.get("filter");
			if(null!= strDoFilter && "true".equalsIgnoreCase(strDoFilter)) {
				String strNameMatches   = (String)inputMap.get("ECMAttributeNameMatches");
				String strTypeFilter    = (String)inputMap.get("ECMAttributeType");
				HashMap requestMap      = (HashMap)inputMap.get("RequestValuesMap");

				boolean bUnused = true;

				mlAttributes = getAttributesByQuery(context,strNameMatches,strTypeFilter,bUnused);
				//String  strAttributeGroupName = (String)inputMap.get("parentOID");
				String  strAttributeGroupName = (String)inputMap.get("AGName");
				if(null != strAttributeGroupName && !"null".equalsIgnoreCase(strAttributeGroupName) && !"".equalsIgnoreCase(strAttributeGroupName)){
					String result = "";
					MQLCommand cmd = new MQLCommand();
					MapList returnList = new MapList();

					try {
                        if (cmd.executeCommand(context, "print bus $1 select $2 $3 dump $4",strAttributeGroupName,"attribute","interface.attribute",",")) {
							result = cmd.getResult();
							if ((result != null) && !(result.equalsIgnoreCase(""))&& !(result.equalsIgnoreCase("null"))) {
								itr         = FrameworkUtil.split(result.trim(), "|").iterator();
								while (itr.hasNext()) {
									objectMap = new HashMap();
									strAttributeName = (String)itr.next();
									if (strAttributeName != null && !strAttributeName.trim().equals("")&&!strAttributeName.trim().equals("null")) {
										objectMap.put("id", strAttributeName.trim());
										mlRelatedAttributes.add(objectMap);
									}
								}
							} else {
								throw new Exception(cmd.getError());
							}
						}
					} catch (Exception ex) {
						throw ex;
					}
					Iterator iRelAttributesIter = mlAttributes.iterator();
					while(iRelAttributesIter.hasNext()) {
						hmAttribute = (HashMap)iRelAttributesIter.next();
						if(!mlRelatedAttributes.contains(hmAttribute)) {
							mlResutList.add(hmAttribute);
						}
					}
				} else {
					mlResutList=mlAttributes;
				}
			}
		}
		catch(Exception ex) {
			throw new FrameworkException(ex.toString());
		}

		return mlResutList;
	}//end of method

	/**
	 * This method returns Attributes  based on the Search Criteria
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments
	 *        0 - Attribute Name Matches
	 *        1 - Attribute Type
	 *        2 - objectId
	 *        3 - Unused Attribute
	 * @throws Exception if the operation fails
	 * @since ECM R212
	 */
	public MapList getAttributesByQuery(Context context, String nameMatches,String typeFilter, boolean unused) throws Exception {
		long initialTime = System.currentTimeMillis();

		MapList result = new MapList();
		HashSet usedAttrSet = new HashSet();

		if (unused) {
                    String agAttrData = MqlUtil.mqlCommand(context, "list attribute $1 select $2 dump $3",true,nameMatches,"name",",").trim();
			StringList agAttrs = FrameworkUtil.split(agAttrData, ",");
			usedAttrSet.addAll(agAttrs);
		}
		String allAttrData ="";
		try{
			allAttrData = MqlUtil.mqlCommand(context, "list attribute $1 select $2 $3 $4 $5 $6 $7 dump $8 recordsep $9", true,nameMatches,"name","type","hidden","description", "owner","application","@","|").trim();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		StringList allAttrRows = FrameworkUtil.split(allAttrData, "|");

		HashSet allAttrNames = new HashSet();
		StringList matchingAttrNamesLst = new StringList();
		Iterator matchingAttrRowIter = allAttrRows.iterator();
		while (matchingAttrRowIter.hasNext()) {
			String row = (String) matchingAttrRowIter.next();
			StringList attrTokens = FrameworkUtil.split(row, "@");
			if (attrTokens.size() < 5) { continue;}  // @ or | in attributes descr }
			String name = (String)attrTokens.get(0);
			String type = (String)attrTokens.get(1);
			String hidden = (String)attrTokens.get(2);
			String description = (String)attrTokens.get(3);
			String owner = (String)attrTokens.get(4);
			
			// Skip hidden attributes
			if (hidden.equals("TRUE")) {
				continue;
			}
			
			// Skip Local Attributes
            if (UIUtil.isNotNullAndNotEmpty(owner)) {
                continue;
            }
            
        	// if type filtering, and type doesn't match, skip
			if (typeFilter != null && !typeFilter.equals("") && !typeFilter.equals("*") &&
					!typeFilter.toUpperCase().trim().equals(type.toUpperCase().trim()) ) {
				continue;
			}

			// if unused filtering, and attributes is used, skip
			if (unused && usedAttrSet.contains(name)) {
				continue;
			}

			HashMap tmp = new HashMap();
			tmp.put("id", name);
			result.add(tmp);
		}
		long finalTime = System.currentTimeMillis();
		return result;
	}//end of method


	/**
	 * This method returns Attribute Group names related to context Change Template
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments
	 *        0 - Attribute Name Matches
	 *        1 - Attribute Type
	 *        2 - objectId
	 *        3 - Unused Attribute
	 * @throws Exception if the operation fails
	 * @since ECM R212
	 */
public Vector getAttributeGroupName(Context context,String [] args) throws Exception {

	//XSSOK
		Vector returnMap = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String jsTreeID =  ((String)((HashMap)programMap.get("paramList")).get("jsTreeID"));
		HashMap requestMap =(HashMap)programMap.get("paramList");
        String strCharSet =  (String )requestMap.get("charSet");
        boolean isprinterFriendly = false;
        boolean isExport            = false;
        String href = "";
		MapList objectList = (MapList)programMap.get("objectList");
		Iterator itr = objectList.iterator();
		while(itr.hasNext()){
			HashMap objectMap = (HashMap)itr.next();
			StringBuffer hrefBuffer = new StringBuffer();
			String objName = (String)objectMap.get("id");
			if(isExport)
            {
                hrefBuffer.append(objName);
            }
			else{
					String strDisplayName = "<span class=\"object\">" +objName+"</span>";
					if(isprinterFriendly)
                    {
                        hrefBuffer.append("<table border=\"0\"><tbody><tr><td><img align=\'top\' border=\'0\' SRC=\"images/iconSmallAttributeGroup.gif\"></td><td>");
                        hrefBuffer.append(strDisplayName);
                        hrefBuffer.append("</td></tr></tbody></table>");
                    }
					else{
						String encodedDisplayName = FrameworkUtil.encodeNonAlphaNumeric(objName,strCharSet);
						String encodedTreeDisplayName = FrameworkUtil.encodeNonAlphaNumeric(encodedDisplayName,strCharSet);

						hrefBuffer.append("emxTree.jsp?treeMenu=ECMAttributeGroupMenu&amp;treeLabel=");
                        hrefBuffer.append(encodedTreeDisplayName);
                        hrefBuffer.append("&amp;objectName=");
                        hrefBuffer.append(encodedDisplayName);
                        hrefBuffer.append("&amp;AppendParameters=true&amp;mode=insert&amp;jsTreeID=");
                        hrefBuffer.append(XSSUtil.encodeForHTMLAttribute(context, jsTreeID));
                        hrefBuffer.append("&amp;AGName=");
                        hrefBuffer.append(objName);
                        hrefBuffer.append("&amp;suiteKey=EnterpriseChangeMgt");

                        href = FrameworkUtil.encodeURL(hrefBuffer.toString(), strCharSet);

						String strImageHtmlCode = "<img border=\'0\' align=\'top\' SRC=\"images/iconSmallAttributeGroup.gif\"></img>";

						StringBuffer strHTMLStartAnchorTag = new StringBuffer();
	                    strHTMLStartAnchorTag.append("<a href=\"JavaScript:emxTableColumnLinkClick('");
	                    strHTMLStartAnchorTag.append(href);
	                    strHTMLStartAnchorTag.append("',730,450,'false','content')\" >");
	                    String strHTMLEndAnchorTag = "</a>";

	                    hrefBuffer = new StringBuffer();

	                    hrefBuffer.append("<table border=\"0\"><tbody><tr><td>");
	                    hrefBuffer.append(strImageHtmlCode);
	                    hrefBuffer.append("</td><td>");
	                    hrefBuffer.append(strHTMLStartAnchorTag.toString());
	                    hrefBuffer.append(strDisplayName);
	                    hrefBuffer.append(strHTMLEndAnchorTag);
	                    hrefBuffer.append("</td></tr></tbody></table>");
					}
			}
			returnMap.addElement(hrefBuffer.toString());
			hrefBuffer = new StringBuffer();
		}
		return returnMap;
	}

/**
 * This method returns MapListof Attributes(name) related to context Attribute Group names
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the list of Attribute Groups
 * @throws Exception if the operation fails
 * @since ECM R216
 */
	public Vector getAttributeGroupAttributes(Context context, String [] args) throws Exception{
		Vector columnMap = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		Iterator itr = objectList.iterator();
		while(itr.hasNext()){
			HashMap objectMap = (HashMap)itr.next();
			String attributes = (String)objectMap.get("attributes");
			if(attributes==null){
				attributes = MqlUtil.mqlCommand(context, "print interface $1 select $2 dump $3" ,(String)objectMap.get("id"),"attribute",",");
			}
			columnMap.add(attributes);
		}
		return columnMap;
	}

	/**
	 * This method returns Maplist of Attributes-description related to context Attribute Group names
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the list of Attribute Groups
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	public Vector getAttributeGroupDescription(Context context, String [] args) throws Exception{
		Vector columnMap = new Vector();

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		Iterator itr = objectList.iterator();
		while(itr.hasNext()){
			HashMap objectMap = (HashMap)itr.next();
			String desc = (String)objectMap.get("desc");
			if(desc==null){
				desc = MqlUtil.mqlCommand(context, "print interface $1 select $2 dump $3" ,(String)objectMap.get("id"),ChangeConstants.SELECT_DESCRIPTION,",");
			}
			columnMap.add(desc);
		}
		return columnMap;
	}
	/**
	 * This method returns details of Attribute Group names
	 * @param context the eMatrix <code>Context</code> object
	 * @param queryParamSelect query parameters for query
	 * @param interfaceName to get its related info
	 * @return String of result having required details needed
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	public String getAttributeGroupDetails(Context context,String queryParamSelect ,String interfaceName) throws FrameworkException{

		String MQLCommand = "print interface \"$1\" select $2 dump";

		String result = MqlUtil.mqlCommand(context, MQLCommand,interfaceName,queryParamSelect);

		return result;
	}

	/**
	 * This method returns all the Attribute Group related to context Change Template also having its details (description and attributes)
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds Change Template id
	 * @return MapList list of Change Template also has description and related attributes for respective Attribute Group
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getChangeTemplateInterface(Context context,String [] args) throws Exception{
		MapList returnList = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String)programMap.get("parentOID");
		HashMap objectMap;
		String desc;
		String name;
		String attributes;
		String result = MqlUtil.mqlCommand(context, "print bus $1 select interface dump", objectId);
		String [] attributeGroups  = new String[0];
		if(UIUtil.isNotNullAndNotEmpty(result)){
			attributeGroups = result.split(",");
		}

		for (int i =0;i<attributeGroups.length;i++){
			objectMap = new HashMap();
			name =  attributeGroups[i];
			objectMap.put("id",name.trim());
			desc = getAttributeGroupDetails(context, "description", name);
			objectMap.put("desc",desc.trim());
			attributes = getAttributeGroupDetails(context, "attribute", name);
			objectMap.put("attributes", attributes);
			returnList.add(objectMap);
		}
		return returnList;
	}

	/**
	 * This method creates a new Attribute Group and adds into the context CHange Template
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following data
	 *          1.Change Template id
	 *          2.new Attribute Group Name
	 *          3.description for AG
	 *          4.Attributes to  added to AG
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	public void createAttributeGroup(Context context,String [] args) throws Exception{
		HashMap programMap      = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap        = (HashMap)programMap.get("paramMap");
        HashMap requestMap      = (HashMap)programMap.get("requestMap");
        String objectId         = (String)paramMap.get("objectId");
        String newName          = (String) paramMap.get("New Value");
        String description      = ((String[])requestMap.get("Description"))[0];
        String attributes       = ((String[])requestMap.get("Attributes"))[0];
        description             = FrameworkUtil.findAndReplace(description,"\n","");
        description             = FrameworkUtil.findAndReplace(description,"\r","");
        StringList methodArgsList = new StringList();
        methodArgsList.add(newName);
        methodArgsList.add(ChangeConstants.TYPE_CHANGETEMPLATE);
        methodArgsList.add(ChangeConstants.TYPE_CHANGE_ORDER);
        methodArgsList.add("product");
        methodArgsList.add("ECM");

        StringBuffer queryBuffer = new StringBuffer();
        queryBuffer.append("add interface $1 type $2,$3 property $4 value $5");

        int descCount = 0;
        Iterator itr;
        if(UIUtil.isNotNullAndNotEmpty(description)){
        	queryBuffer.append(" description $6");
        	methodArgsList.add(description);
        }

        MqlUtil.mqlCommand(context, queryBuffer.toString(),methodArgsList);

        try{
        	if(UIUtil.isNotNullAndNotEmpty(attributes)){
        		itr         = FrameworkUtil.split(attributes.trim(), "|").iterator();
        		for(int i=1+descCount;itr.hasNext();i++){
        			MqlUtil.mqlCommand(context, "modify interface $1 add attribute $2",newName,(String)itr.next());
        		}
        	}
        MqlUtil.mqlCommand(context, "mod bus $1 add interface $2",objectId,newName);
        }catch(Exception e){
        	e.printStackTrace();
        	throw new Exception(e);
        }
	}
	/**
	 * This method returns the list of unused Attribute Groups
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following data
	 *          1.Change Template id
	 *          2.filter to show the result
	 *          3.name matches if matching Ag are required to add.
	 * @return list of matching AG
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getUnusedAttributeGroups (Context context,String [] args) throws Exception{
		MapList returnList = new MapList();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String)programMap.get("parentOID");
		String strDoFilter      = (String)programMap.get("filter");
		String strNameMatches;
		if(null!=strDoFilter&&!"".equals(strDoFilter)){
			strNameMatches    = (String)programMap.get("ECMAttributeNameMatches");
			returnList =  ChangeTemplate.getResult(context,objectId,strNameMatches);
		}
		return returnList;
	}
	/**
	 * This method returns the list Attributes related to context Attribute Groups
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following data
	 *          1.Change Template id
	 *          2.filter to show the result
	 *          3.name matches if matching Ag are required to add.
	 * @return list of added Attributes to AttributesGroup
	 * @throws Exception if the operation fails
	 * @since ECM R216
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAttributeGroupAttributesAdded(Context context,String [] args) throws Exception
	{
		MapList returnList 			= new MapList();
		HashMap programMap 			= (HashMap)JPO.unpackArgs(args);

		String attributeGroupName 	= (String)programMap.get("objectName");
		String languageStr			= (String)programMap.get("languageStr");
		HashMap attributesMap;
		String [] attributes;
		String result = MqlUtil.mqlCommand(context, "print interface $1 select $2 dump $3", attributeGroupName,"attribute" , ",").trim();
		if(UIUtil.isNotNullAndNotEmpty(result)){
			attributes 				= result.split(",");
			for(String attribute:attributes){
				attributesMap		= new HashMap();
				attributesMap.put("id", attribute);
				returnList.add(attributesMap);
			}
		}

		return returnList;
	}

	/**
	 * method used to display Change Template-Attribute Group-attributes (if added) dynamically when selected Change Template while creating CO.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        a <code>MapList</code> MapList of attributes with Attribute Group as section header
	 * @throws        Exception if the operation fails
	 * @since         ECM R216
	 **
	*/
	public MapList DisplayInterfaceAttributes(Context context, String args[] ) throws Exception{
		MapList returnList  = new MapList();
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap  = (HashMap)programMap.get("requestMap");
		String CTID         = (String)requestMap.get("tmplId");
		String COID         = (String)requestMap.get("objectId");
		String formName     = (String)requestMap.get("form");
		String CreateMode   = (String)requestMap.get("CreateMode");
		boolean isCreate    = UIUtil.isNotNullAndNotEmpty(CreateMode)?true:false;
		String strObjectId  = UIUtil.isNotNullAndNotEmpty(COID)&&(!isCreate)?COID:CTID;
		String strPolicy;
		if(!UIUtil.isNullOrEmpty(strObjectId)){
			strPolicy       = new DomainObject(strObjectId).getPolicy(context).getName();
			//checking the policy of object to load interface attributes only for CO or CT
			if(strPolicy.equals(ChangeConstants.POLICY_FORMAL_CHANGE)||strPolicy.equals(ChangeConstants.POLICY_FASTTRACK_CHANGE)||strPolicy.equals(ChangeConstants.POLICY_CHANGE_TEMPLATE)){
				returnList      = getAttributeGroupAttributesFromChangeTemplate(context, strObjectId);
				returnList      = getDynamicFieldsMapList(context,returnList,formName,isCreate);
			}
		}
		return returnList;
	}
	/**
	 * method used to display Change Template-Attribute Group-attributes (if added) dynamically when selected Change Template while creating CO.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        a <code>MapList</code> MapList of attributes with Attribute Group as section header
	 * @throws        Exception if the operation fails
	 * @since         ECM R216
	 **
	*/
	public MapList getAttributeGroupAttributesFromChangeTemplate(Context context,String [] args) throws Exception{
		MapList returnList = new MapList();
		String CTID = (String)JPO.unpackArgs(args);
		return returnList = getAttributeGroupAttributesFromChangeTemplate(context,CTID);
	}
	/**
	 * method used to display Change Template-Attribute Group-attributes (if added) dynamically when selected Change Template while creating CO.
	 * @param context the eMatrix <code>Context</code> object
	 * @param CTID    object Id
	 * @return        a <code>MapList</code> MapList of attributes with Attribute Group as section header
	 * @throws        Exception if the operation fails
	 * @since         ECM R216
	 **
	*/
	public MapList getAttributeGroupAttributesFromChangeTemplate(Context context,String CTID) throws Exception{
		MapList fieldMapList  = new MapList();
		DomainObject ctobj   = new DomainObject(CTID);
        StringList selectables  = new StringList();

        StringList slAttributeGroups = new StringList();
        StringList attrGroupList;
        HashMap attibuteGroup;
        String attibuteGroupName;
        Iterator itr;
        MapList attributes;
        String sAllconnectedInterfaces = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",CTID,"interface","|");
        
        //check for ECM specific interfaces
        StringList ECMInterfaces = FrameworkUtil.split(MqlUtil.mqlCommand(context, "list interface $1 where $2","*","property[product].value==\'ECM\'"), "\n");
        String AGName;
        if(UIUtil.isNotNullAndNotEmpty(sAllconnectedInterfaces)){
        	attrGroupList = FrameworkUtil.split(sAllconnectedInterfaces, "|");
        	itr = attrGroupList.iterator();
	        while(itr.hasNext()){ 
	        	AGName = (String)itr.next();
	        	if(ECMInterfaces.contains(AGName.trim()))
	        		slAttributeGroups.add(AGName);
	        }

	        selectables = new StringList();
	        selectables.add("type");
	        selectables.add("range");
	        selectables.add("multiline");
	        selectables.add("valuetype");
	        //for each attribute group
	        for(int i=0;i< slAttributeGroups.size();i++){
	            attibuteGroup = new HashMap();
	            attibuteGroupName = (String)slAttributeGroups.get(i);
	            attibuteGroup.put("attributeGroupName", attibuteGroupName);
	            attributes = getAttributeGroupAttributesDetails(context, attibuteGroupName, selectables);
	            attibuteGroup.put("attributes", attributes);
	            fieldMapList.add(attibuteGroup);
	        }
        }
		return fieldMapList;
	}


	/***
     * This method create the settingsMap and fieldMap to display all the Classification Attributes.
     * The list of Attributes are looped through and check is performed whether the attributes
     * is of type Integer/String/Real/Date/Boolean, the fieldMap is set with the appropriate
     * settings for each of the attribute type.
     * @param context
     * @param classificationAttributesList
     * @param formName
     * @param isCreate
     * @since R216
     * @return MapList containing the settingMap
     * @throws Exception
     */
    private MapList getDynamicFieldsMapList(Context context,MapList classificationAttributesList,String formName,boolean isCreate) throws Exception{
        String FIELD_TYPE_ATTRIBUTE			= "attribute";

    	String INPUT_TYPE_COMBOBOX			= "combobox";
    	String INPUT_TYPE_TEXTAREA			= "textarea";
    	String INPUT_TYPE_TEXTBOX			= "textbox";

    	String SETTING_FIELD_TYPE			= "Field Type";
    	String SETTING_ADMIN_TYPE			= "Admin Type";
    	String SETTING_REGISTERED_SUITE		= "Registered Suite";
    	String SETTING_INPUT_TYPE			= "Input Type";
    	String SETTING_FORMAT				= "format";
    	String SETTING_RANGE_PROGRAM		= "Range Program";
    	String SETTING_RANGE_FUNCTION		= "Range Function";
    	String SETTING_VALIDATE				= "Validate";

    	String SETTING_UPDATE_PROGRAM		= "Update Program";
        String SETTING_UPDATE_FUNCTION		= "Update Function";

    	String EXPRESSION_BUSINESSOBJECT	= "expression_businessobject";

    	String FORMAT_TIMESTAMP				= "timestamp";
    	String FORMAT_DATE					= "date";
    	String FORMAT_INTEGER				= "integer";
        String FORMAT_BOOLEAN				= "boolean";
        String FORMAT_REAL					= "real";
        String FORMAT_NUMERIC				= "numeric";
        String FORMAT_STRING				= "string";

        String BOOLEAN_TRUE					= "true";
        String BOOLEAN_FALSE				= "false";
        String LABEL 						= "label";
        
        Map AttributeGroupMap;
        String attributeGroupName;
        HashMap settingsMapForAGHeader;
        HashMap fieldMapForAGHeader;
        
        HashMap attribute;
    	String attributeName;
    	HashMap fieldMap;
        HashMap settingsMap;
        
        //Define a new MapList to return.
        MapList fieldMapList = new MapList();
        String strLanguage =  context.getSession().getLanguage();

        // attributeAttributeGroupMap contains all the attribute group names to which each attribute belongs
        HashMap attributeAttributeGroupMap = new HashMap();

        if(classificationAttributesList == null)
            return fieldMapList;

        Iterator classItr = classificationAttributesList.iterator();
        while(classItr.hasNext()){
            AttributeGroupMap = (Map)classItr.next();
            attributeGroupName = (String)AttributeGroupMap.get("attributeGroupName");
            settingsMapForAGHeader = new HashMap();
            fieldMapForAGHeader = new HashMap();
            settingsMapForAGHeader.put(SETTING_FIELD_TYPE,"Section Header");
            settingsMapForAGHeader.put(SETTING_REGISTERED_SUITE,"EnterpriseChangeMgt");
            settingsMapForAGHeader.put("Section Level","1");
            fieldMapForAGHeader.put(LABEL,attributeGroupName);
            fieldMapForAGHeader.put("settings", settingsMapForAGHeader);
            fieldMapList.add(fieldMapForAGHeader);

            MapList AGAttributes = (MapList)AttributeGroupMap.get("attributes");

            for(int i=0;i<AGAttributes.size();i++){
            	attribute =  (HashMap)AGAttributes.get(i);
            	attributeName = (String)attribute.get("name");
            	fieldMap = new HashMap();
                settingsMap = new HashMap();
                /*String attributeGroupFieldName = (isCreate==true?attributeGroupName+"|"+attributeName:attributeName);
                fieldMap.put("name",attributeGroupFieldName);*/
                fieldMap.put("name",attributeGroupName+"|"+attributeName);
                fieldMap.put(LABEL,i18nNow.getAttributeI18NString(attributeName,strLanguage));
                fieldMap.put(EXPRESSION_BUSINESSOBJECT,"attribute["+attributeName+"].value");

               // if(!isCreate){
                	fieldMap.put(SETTING_ADMIN_TYPE,"attribute_"+attributeName.replaceAll(" ", ""));
               // }
                String attributeType = (String)attribute.get("type");
                String symbolicAttrName = FrameworkUtil.getAliasForAdmin(context, "attribute", attributeName, true);
                if(attributeType.equals(FORMAT_TIMESTAMP)){
                    settingsMap.put(SETTING_FORMAT, FORMAT_DATE);
                }
               else if(attributeType.equals(FORMAT_BOOLEAN) ){
                    settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                    StringList range = (StringList)attribute.get("range");

                    if(range==null){
                    settingsMap.put(SETTING_RANGE_PROGRAM, "enoECMChangeTemplate");
                    settingsMap.put(SETTING_RANGE_FUNCTION, "getRangeValuesForBooleanAttributes");

                    }
                }
                else if(attributeType.equals(FORMAT_INTEGER)){
                		settingsMap.put(SETTING_FORMAT, FORMAT_INTEGER);
                		if(UOMUtil.isAssociatedWithDimension(context, attributeName)) {
                        	addUOMDetailsToSettingsMap(context,attributeName,fieldMap,settingsMap);
                        }
                        if(formName.equals("type_CreatePart"))
                            settingsMap.put(SETTING_VALIDATE, "isValidInteger");
                        //setting the input type to combobox
                        if((StringList)attribute.get("range")!=null)
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                }
                else if(attributeType.equals(FORMAT_REAL)){
                		settingsMap.put(SETTING_FORMAT, FORMAT_NUMERIC);
                		if(UOMUtil.isAssociatedWithDimension(context, attributeName)) {
                        	addUOMDetailsToSettingsMap(context,attributeName,fieldMap,settingsMap);
                        }
                        if(formName.equals("type_CreatePart"))
                            settingsMap.put(SETTING_VALIDATE, "checkPositiveReal");
                        //setting the input type to combobox
                        if((StringList)attribute.get("range")!=null)
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                }
                else if(attributeType.equals(FORMAT_STRING))
    	        {
                	StringList range = (StringList)attribute.get("range");
                	String isMultiline=(String)attribute.get("multiline");
    	            if(range != null && range.size() > 0) {
    	            		settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                        	settingsMap.put(SETTING_FORMAT, FORMAT_STRING);
    	            } else if (BOOLEAN_TRUE.equalsIgnoreCase(isMultiline)) {
    	            	settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTAREA);
    	            } else {
    	            	settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTBOX);
    	            }
    	        }
                else{

                }

                settingsMap.put(SETTING_FIELD_TYPE,FIELD_TYPE_ATTRIBUTE);
                /*if(isCreate){
                    settingsMap.put(SETTING_UPDATE_PROGRAM,"enoECMChangeTemplate");
                    settingsMap.put(SETTING_UPDATE_FUNCTION,"dummyUpdateFunction");
                }else{

                }*/

                //On Change Handler
                settingsMap.put("OnChange Handler", "reloadDuplicateAttributesInForm");
                settingsMap.put("Editable", "true");
                fieldMap.put("settings",settingsMap);
                fieldMapList.add(fieldMap);
                String attributeGroupNames = (String)attributeAttributeGroupMap.get(attributeName);
                if(attributeGroupNames == null){
                    attributeAttributeGroupMap.put(attributeName, attributeGroupName);
                }else{
                    attributeAttributeGroupMap.put(attributeName, attributeGroupNames + "|" + attributeGroupName);
                }
            }
        }

        //update "AttributeGroups"
        //Attribute Groups information will be used for reloading the duplicate values
        Iterator itr = fieldMapList.iterator();
        while(itr.hasNext()){
            HashMap hFieldMap = (HashMap)itr.next();
            HashMap hSettingsMap = (HashMap)hFieldMap.get("settings");
            if( !"Section Header".equals(hSettingsMap.get(SETTING_FIELD_TYPE)) ){
	            String fieldName = (String)hFieldMap.get("name");
	            String strAttributeName      = "";
	            if(isCreate){
		            
	            	strAttributeName = fieldName.substring(fieldName.indexOf('|')+1);
		            String allAttributeGroupsNames = (String)attributeAttributeGroupMap.get(strAttributeName);
		            hSettingsMap.put("AttributeGroups",allAttributeGroupsNames);
	            }
            }
        }
        // Add a program HTML field which contains a javascript to reload duplicate attributes in the FORM
        HashMap reloadFunctionField = new HashMap();
        HashMap reloadFunctionFieldSettings = new HashMap();
        reloadFunctionFieldSettings.put(SETTING_FIELD_TYPE, "programHTMLOutput");
        reloadFunctionFieldSettings.put("program","enoECMChangeTemplate");
        reloadFunctionFieldSettings.put("function","getReloadDuplicateAttributesInForm");

        reloadFunctionField.put("name","reloadFunctionField");
        reloadFunctionField.put("settings",reloadFunctionFieldSettings);

        fieldMapList.add(reloadFunctionField);

        return fieldMapList;
    }

    /***
     * This method returns details all attributes of context AG having given selectables.
     * settings for each of the attribute type.
     * @param context
     * @param agName=AG name
     * @param selectables=required attribute details
     * @since R216
     * @return MapList containing the result
     * @throws Exception
     */
	protected static MapList getAttributeGroupAttributesDetails(Context context,String agName,StringList selectables)throws Exception{
        StringBuffer cmd = new StringBuffer("print interface $1 select "); // Move select
        String[] newArgs = new String[selectables.size()+1];
        newArgs[0] = agName;
        for(int i=0;i<selectables.size();i++){
            cmd.append("\"$"+(i+2)+"\" ");
            newArgs[i+1] = "attribute."+(String)selectables.get(i);
        }

        String result = MqlUtil.mqlCommand(context,cmd.toString(),true,newArgs);

        HashMap hmAllAttributeDetails = parseMqlOutput(context, result);
        MapList agAttributesDetails = new MapList();

        Set setAllAttributeDetails = hmAllAttributeDetails.keySet();
        Iterator itr = setAllAttributeDetails.iterator();
        String attributeName = new String();
        HashMap hmAttributeDetails = new HashMap();
        while(itr.hasNext()){
            attributeName = (String)itr.next();
            hmAttributeDetails = (HashMap)hmAllAttributeDetails.get(attributeName);
            if(hmAttributeDetails != null){
                hmAttributeDetails.put("name", attributeName);
                agAttributesDetails.add(hmAttributeDetails);
            }
        }

        return agAttributesDetails;
    }
	/**
     * this method parse the mql Output of mutiple lines,
     *  each line is of the form
     *  property[propertyName].subProperty = result
     *     where
     *       property      - should be present
     *                     - should not contain characters [ ] . =
     *                     - property in all the lines should be same
     *       propertyName  - should be present
     *                     - may contain . or = characters
     *                     - should not contain characters [ ]
     *       subProperty   - should be present
     *                     - should not contain . or = characters
     *                     - can end with [i] ,where i is 0, 1, 2, 3 ...
     *       result        - may or may not present
     *
     * @param context the eMatrix <code>Context</code> object
     * @param output mql output to be parsed
     * @return a HashMap with following key value pair
     *            key   - propertyName
     *            value - HashMap with following key value pair
     *                      key   - subProperty
     *                      value - String result
     *
     * @throws Exception
     */
    protected static HashMap parseMqlOutput(Context context,String output) throws Exception{
    	String PROPNAME_START_DELIMITER  = "[";
        String PROPNAME_END_DELIMITER    = "]";
        String RESULT_DELIMITER          = " =";
        String RANGE_START_DELIMITER     = "[";
    	BufferedReader in = new BufferedReader(new StringReader(output));
        String resultLine;
        HashMap mqlResult = new HashMap();
        while((resultLine = in.readLine()) != null){
            String property = null;
            String propertyName = null;
            String subProperty = null;
            String result = null;

            try{
                //identify property propertyValue subProperty subPropertyValue  - start
                boolean hasRanges = false;
                int propNameStartDelimIndex = resultLine.indexOf(PROPNAME_START_DELIMITER);
                int resultDelimIndex        = resultLine.indexOf(RESULT_DELIMITER);

                property                    = resultLine.substring(0, propNameStartDelimIndex);

                int propNameEndDelimIndex   = resultLine.indexOf(PROPNAME_END_DELIMITER, propNameStartDelimIndex);
                propertyName                = resultLine.substring(propNameStartDelimIndex+1, propNameEndDelimIndex);

                String propertyAndValue     = property + PROPNAME_START_DELIMITER+propertyName+PROPNAME_END_DELIMITER;
                String remainingResultLine  = resultLine.substring(propertyAndValue.length());

                // if remaining result starts with .
                int rangeStartDelimIndex    = remainingResultLine.indexOf(RANGE_START_DELIMITER);
                resultDelimIndex            = remainingResultLine.indexOf(RESULT_DELIMITER);
                if((rangeStartDelimIndex != -1) && (rangeStartDelimIndex < resultDelimIndex)){
                    // if [ exists and comes before = , then anything before [ is the subProperty and subProperty contains range of results
                    subProperty = remainingResultLine.substring(1,rangeStartDelimIndex);
                    hasRanges   = true;
                }else{
                    // else , anything Before = is the subProperty
                    subProperty = remainingResultLine.substring(1,resultDelimIndex);
                }

                result   = remainingResultLine.substring(resultDelimIndex+RESULT_DELIMITER.length());

                property = property.trim();
                result   = result.trim();

                //identify property propertyValue subProperty subPropertyValue  - end

                //start building HashMap
                HashMap hmPropertyName;
                String strSubProperty;
                StringList slSubProperty;

                hmPropertyName = (HashMap)mqlResult.get(propertyName);
                if(hmPropertyName == null){
                    hmPropertyName = new HashMap();
                    mqlResult.put(propertyName, hmPropertyName);
                }
                if(hasRanges){
                    slSubProperty = (StringList)hmPropertyName.get(subProperty);
                    if(slSubProperty == null){
                        slSubProperty = new StringList();
                        hmPropertyName.put(subProperty,slSubProperty);
                    }
                    slSubProperty.add(result);
                }else{
                    hmPropertyName.put(subProperty,result);
                }

            }catch(Exception e){
                // if there is exception during parsing a line , proceed to next line
            }
        }

        return mqlResult;
    }

    /***
     *  This method adds all the UOM details required to display Classification Attribute
     *  during create Generic Document/Part. To display UOM details settingsMap should
     *  contain Field Type=Attribute, otherwise the UI would display only textbox next to the
     *  UOM Field.Once the map contains FieldType=Attribute, BPS code assumes that this Attribute
     *  is defined on the Type, but in case of Classification Attributes it's not,
     *  Hence to overcome this bug a Dummy update program & function is used  here, If a update program
     *  & Function is defined BPS wouldn't check whether the attribute is defined on the type.
     * @param context
     * @param attributeName
     * @param fieldMap
     * @param settingsMap
     * @since R216
     * @throws FrameworkException
     */
        private void addUOMDetailsToSettingsMap(Context context,String attributeName,HashMap fieldMap,HashMap settingsMap) throws FrameworkException{
        	String UOM_ASSOCIATEDWITHUOM     = "AssociatedWithUOM";
            String DB_UNIT                   = "DB Unit";
            String UOM_UNIT_LIST             = "DB UnitList";
            String UOM_INPUT_UNIT            = "Input Unit";
            String SETTING_EDITABLE_FIELD    = "Editable";
            String BOOLEAN_TRUE              = "true";
            String SETTING_INPUT_TYPE			= "Input Type";
            String INPUT_TYPE_TEXTBOX			= "textbox";

        	fieldMap.put(UOM_ASSOCIATEDWITHUOM, BOOLEAN_TRUE);
        	fieldMap.put(DB_UNIT, UOMUtil.getSystemunit(context, null,attributeName,null));
        	fieldMap.put(UOM_UNIT_LIST, UOMUtil.getDimensionUnits(context, attributeName));
            settingsMap.put(SETTING_EDITABLE_FIELD,BOOLEAN_TRUE);
            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTBOX);
        }

        /***
         * This is a dummy Update Function used for Displaying Classification Attributes during Create
         * @param context
         * @param args
         * @throws Exception
         */
         public void dummyUpdateFunction(Context context, String[] args)
         throws Exception
         {
        	 // do nothing
         }

         /***
          * Function returns javascript to reload duplicate values in fields
          * @param context
          * @param args
          * @throws Exception
          */
         public static String getReloadDuplicateAttributesInForm(Context context,String[] args){

             String reloadFunction =  "<script type=\"text/javascript\" >" + "\n" +
                     "//<!--" + "\n" +
                     "function reloadDuplicateAttributesInForm(fieldName,fieldValue){"+ "\n" +
                         "var currentActualValue  = fieldValue.current.actual;"+ "\n" +
                         "var currentDisplayValue = fieldValue.current.display;"+ "\n" +
                         "var fieldNameValues     =  fieldName.split(\"|\");"+ "\n" +
                         "if (FormHandler) {"+ "\n" +
                             "var attributeGroups     = FormHandler.GetField(fieldName).GetSettingValue(\"AttributeGroups\");"+ "\n" +
                             "if(attributeGroups){"+ "\n"+
                             "var attributeGroupsList = attributeGroups.split(\"|\");"+ "\n" +
                             "for(var i = 0; i < attributeGroupsList.length; i++) {"+
                                 "if(attributeGroupsList[i] != \"\" && attributeGroupsList[i] != fieldNameValues[0]) {"+
                                     "FormHandler.GetField(attributeGroupsList[i]+\"|\"+fieldNameValues[1]).SetFieldValue(currentActualValue,currentDisplayValue);"+
                                 "}"+
                             "}"+
                             "}"+

                         "}"+ "\n" +
                     "}"+ "\n" +
                     "//-->" + "\n" +
                 "</script>";


             return reloadFunction;
         }

         /***
          * Function returns map of boolean range values for boolean attribute field
          * @param context
          * @param args
          * @throws Exception
          */
         public HashMap getRangeValuesForBooleanAttributes(Context context,String [] args) throws Exception {
        	 HashMap rangeMap = new HashMap();

             try
             {
                 StringList fieldChoices = new StringList();
                 StringList fieldDisplayChoices = new StringList();
                 HashMap programMap = (HashMap)JPO.unpackArgs(args);
                 HashMap paramMap = (HashMap)programMap.get("paramMap");
                 String language = (String)paramMap.get("languageStr");
                 String trueStr = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(language),"emxFramework.Range.BooleanAttribute.TRUE");
                 String falseStr = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(language),"emxFramework.Range.BooleanAttribute.FALSE");
                 //Modifed for IR-057820
                 fieldChoices.add("TRUE");
                 fieldChoices.add("FALSE");
                 fieldDisplayChoices.add(trueStr);
                 fieldDisplayChoices.add(falseStr);
                 rangeMap.put("field_choices", fieldChoices);
                 rangeMap.put("field_display_choices", fieldDisplayChoices);
             }catch(Exception ex)
             {
                 throw new FrameworkException(ex.toString());
             }
             return rangeMap;
         }
         
         /**
          * Delete Check Trigger on type Change Template to check whether Change Object is attached to Change Template before deletion
          * @param context the matrix context
          * @param args containing Change Template object Id
          * @return value 1 to block delete action, 0 to continue deletion
          * @throws Exception if operation fails
          */
         public int checkChangeTemplateOnDelete(Context context, String[] args) throws Exception {
        	int result =0;
     		try {
     			String strObjectId = args[0];
     			String errorMsg1 			= EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Alert.TemplateDeleteError1");
     			String errorMsg2 			= EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Alert.TemplateDeleteError2");
     			StringBuffer sbAlert = new StringBuffer();
     			DomainObject domObj = DomainObject.newInstance(context, strObjectId);
     			String strRel = "from["+ChangeConstants.RELATIONSHIP_CHANGE_INSTANCE+"].to.name";
     			StringList sLObject = new StringList();
     			sLObject.add(strRel);
     			sLObject.add(DomainConstants.SELECT_NAME);
     			Map mpCTDetails = domObj.getInfo(context, sLObject);
     			String strCO = (String) mpCTDetails.get(strRel);
     			if(null!=strCO && !"".equals(strCO)){
     				result = 1;
     				sbAlert.append(errorMsg1);
     				sbAlert.append(" '");
     				sbAlert.append(mpCTDetails.get(DomainConstants.SELECT_NAME));
     				sbAlert.append("' ");
     				sbAlert.append(errorMsg2);
                    MqlUtil.mqlCommand(context, "notice $1", sbAlert.toString());
     			}
     		}
     		catch (Exception ex) {
     			result =1;
     			ex.printStackTrace();
     			throw new FrameworkException(ex.getMessage());
     		}
     		return result;
     	}


}


