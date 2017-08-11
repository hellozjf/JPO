
/*   emxEnhancedSearchBase.
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the implementation of emxEnhancedSearch.
**
*/



import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Role;
import matrix.db.UserItr;
import matrix.util.SelectList;
import matrix.util.StringList;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Search;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;


public class emxEnhancedSearchBase_mxJPO extends emxDomainObject_mxJPO {

	// The operator symbols
	/** A string constant with the value &&. */
	protected static final String SYMB_AND = " && ";
	/** A string constant with the value ~~. */
	protected static final String SYMB_MATCH = " ~~ ";
	/** A string constant with the value !=. */
	protected static final String SYMB_NOT_EQUAL = " != ";
	/** A string constant with the value '. */
	protected static final String SYMB_QUOTE = "'";
	/** A string constant with the value *. */
	protected static final String SYMB_WILD = "*";
	/** A string constant with the value (. */
	protected static final String SYMB_OPEN_PARAN = "(";
	/** A string constant with the value ). */
	protected static final String SYMB_CLOSE_PARAN = ")";
	/** A string constant with the value attribute. */
	protected static final String SYMB_ATTRIBUTE = "attribute";
	/** A string constant with the value [. */
	protected static final String SYMB_OPEN_BRACKET = "[";
	/** A string constant with the value ]. */
	protected static final String SYMB_CLOSE_BRACKET = "]";
	/** A string constant with the value to. */
	protected static final String SYMB_TO = "to";
	/** A string constant with the value from. */
	protected static final String SYMB_FROM = "from";
	/** A string constant with the value ".". */
	protected static final String SYMB_DOT = ".";
	/** A string constant with the value "null". */
	protected static final String SYMB_NULL = "null";


	HashMap policyStateMap = new HashMap();

	/* The default constructor */
	public emxEnhancedSearchBase_mxJPO (Context context, String[] args)
		throws Exception {
	  super(context, args);
	}


	/* Main entry point (Written by Tejashwini)*/
	public int mxMain(Context context, String[] args)
		throws Exception {
		if (!context.isConnected()){
   			throw  new Exception(ComponentsUtil.i18nStringNow("emxComponents.EnhancedSearch.JPONotFound", context.getLocale().getLanguage()));
 		}
		return 0;
	}

	/* This method get Results for the specified criteria in Search (Written by Tejashwini)*/
    public Object getSearchResults(Context context, String[] args)
		throws Exception {

		String sAnd					= "&&";
		char chDblQuotes			= '\"';
		HashMap paramMap			= (HashMap)JPO.unpackArgs(args);
		MapList businessObjectList	= new MapList();

		/* Get the request parameters */
		String type				= getParameter(paramMap,"txtTypeDisplay");

		String name             	= getParameter(paramMap,"txtName");
		String revision         	= getParameter(paramMap,"txtRev");
		String state           		= getParameter(paramMap, "txtState");
		String policy           		= getParameter(paramMap, "txtPolicy");
		boolean isLatestRevisionOnly = false;
		String strLatestRevisionOnly = getParameter(paramMap, "latestRevision");
		if (strLatestRevisionOnly != null &&
			strLatestRevisionOnly.equalsIgnoreCase("true") == true)
			 isLatestRevisionOnly = true;

		String sOwner = getParameter(paramMap,"txtOwnerDisplay").trim();
		sOwner = sOwner.trim();

		String searchText       = "";
		String queryLimit       = getParameter(paramMap,"queryLimit");
		String createdBefore    = getParameter(paramMap,"createdBefore");
		String createdAfter    = getParameter(paramMap,"createdAfter");

		String matchCase =  (String)paramMap.get("matchCase");
		boolean bMatchCase = true;

		String timeZone              = (String)paramMap.get("timeZone");
		double clientTZOffset        = new Double(timeZone).doubleValue();
		Locale localeObj =   (Locale) paramMap.get("localeObj");

		// Match Case
		if (null == matchCase || 0 == matchCase.length())
			 bMatchCase = false;

		// Owner
		if (null==sOwner || ("null").equals(sOwner) || (",").equals(sOwner)) {
			sOwner = SYMB_WILD;
		}

		// Revision Type
		String revisionType ="";
		if (isLatestRevisionOnly)
			revisionType = "latest";

	 	/*  If none of the Vaults are selected, add a *, search in all Vaults */
	 	String strVault = null;
	 	String strVaultOption = getParameter(paramMap,"vaultOption");

	  	if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
			strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
	  	else
			strVault = getParameter(paramMap,"vaults");

		String whereClause="";

		try {
			// createdBefore
			if (!(createdBefore== null || createdBefore.equalsIgnoreCase(SYMB_NULL) ||
					createdBefore.length() == 0 ||createdBefore.equals(SYMB_WILD) )) {

				Date date = new Date(createdBefore);
				int intDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
				DateFormat formatter = DateFormat.getDateInstance(intDateFormat, localeObj);
				createdBefore =  formatter.format(date);
				String strInputTime = "11:59:59 PM";
				createdBefore = eMatrixDateFormat.getFormattedInputDateTime(context,createdBefore,strInputTime,clientTZOffset,localeObj);

				String sExpCreatedBefore = "originated<" + "\"" + createdBefore+"\"" ;
				if (whereClause == null || whereClause.equalsIgnoreCase(SYMB_NULL) || whereClause.equals("")||whereClause.length()==0 )
					whereClause ="(("+ sExpCreatedBefore+"))";
				else
					whereClause += " "+sAnd + " ((" + sExpCreatedBefore+"))";
			}


			// createdAfter
			if (!(createdAfter== null || createdAfter.equalsIgnoreCase(SYMB_NULL) ||
					createdAfter.length() == 0 ||createdAfter.equals(SYMB_WILD) )) {
				Date date = new Date(createdAfter);
				int intDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
				DateFormat formatter = DateFormat.getDateInstance(intDateFormat, localeObj);
				createdAfter=  formatter.format(date);
				String strInputTime = "11:59:59 PM";
				createdAfter = eMatrixDateFormat.getFormattedInputDateTime(context,createdAfter,strInputTime,clientTZOffset,localeObj);

				String sExpCreatedAfter = "originated>" + "\"" + createdAfter+"\"" ;
				if (whereClause == null || whereClause.equalsIgnoreCase(SYMB_NULL) || whereClause.equals("")||whereClause.length()==0 )
					whereClause ="(("+ sExpCreatedAfter+"))";
				else
					whereClause += " "+sAnd + " ((" + sExpCreatedAfter+"))";
			}

			// State
			if (!(state== null || state.equalsIgnoreCase(SYMB_NULL) ||
					state.length() == 0 ||state.equals(SYMB_WILD) )) {
				String sExpState = "current" + " ~~ \"" + state+"\"" ;

				if (whereClause == null || whereClause.equalsIgnoreCase(SYMB_NULL) || whereClause.equals("")||whereClause.length()==0 )
					whereClause ="(("+ sExpState+"))";
				else
					whereClause += " "+sAnd + " ((" + sExpState+"))";
			}


			// Policy
			if (!(policy== null || policy.equalsIgnoreCase(SYMB_NULL) ||
				  policy.length() == 0 ||policy.equals(SYMB_WILD) )) {
				String sExpPolicy = "policy" + "==\"" + policy +"\"" ;

				if (whereClause == null || whereClause.equalsIgnoreCase(SYMB_NULL) || whereClause.equals("")||whereClause.length()==0 )
					whereClause ="(("+ sExpPolicy +"))";
				else
					whereClause += " "+sAnd + " ((" + sExpPolicy+"))";
			}


			SelectList resultSelects = new SelectList(1);
			resultSelects.add(SELECT_ID);

			if(type!=null && !"".equals(type.trim())) {
				//find only objects whose type is not hidden
				//mql and adk returns objects whose type is hidden
				//so set whereclause to filter the result

				// Revision
				if(revisionType.equals("latest")) {
					if (whereClause == null || whereClause.equalsIgnoreCase(SYMB_NULL) || whereClause.equals("")||whereClause.length()==0 )
						whereClause += "revision==last";
					else
						whereClause += " "+sAnd + " " + "revision==last";
				}

				// Hidden
				if (whereClause == null || whereClause.equalsIgnoreCase(SYMB_NULL) ||
					whereClause.equals("")||whereClause.length()==0 ) {
						whereClause += SYMB_OPEN_PARAN;
						whereClause += "type.hidden == FALSE";
						whereClause += SYMB_CLOSE_PARAN;
				}
				else {
						whereClause += SYMB_AND;
						whereClause += SYMB_OPEN_PARAN;
						whereClause += "type.hidden == FALSE";
						whereClause += SYMB_CLOSE_PARAN;
				}

				// Match Case
				if (false == bMatchCase && false == name.equals(SYMB_WILD)) {
					  whereClause += " "+sAnd +" "+ "name ~~  '" + name + "'";
					  name = SYMB_WILD;
				}

				/* Method to get Filtered Expression for Search List */
				String strFilteredExpression = getFilteredExpression(context,paramMap);
				if ((strFilteredExpression.trim().length() > 0 )) {
					whereClause += strFilteredExpression;
				}

				/* Search Results */
				businessObjectList = DomainObject.findObjects(  context,
										type, //type
										name,
										revision,
										sOwner,
										strVault,
										whereClause,
										"", //save to the .finder later
										false,
										resultSelects,
										Short.parseShort(queryLimit),
										SYMB_WILD,
										searchText);
			 }
		}
		catch( Exception ex ) {
			businessObjectList = new MapList();
		}

		return businessObjectList;
    }

    /* Method to get Filtered Expression for Search List (Written by Tejashwini)*/
    protected String getFilteredExpression(Context context,HashMap paramMap) {

		String doReconnect = (String)paramMap.get("doReConnect");
		String objectId = (String)paramMap.get("objectId");
		StringBuffer sbWhereExp = new StringBuffer();
		/* Condition for Add Existing */
		String strMode = (String) paramMap.get(Search.REQ_PARAM_MODE);
		if(objectId != null && strMode.equals(Search.ADD_EXISTING)) {
			/* Condition for Re-Connection */
			if(doReconnect.equalsIgnoreCase("false")) {
				/* Get Relationship Direction */
				String isTo = (String)paramMap.get("isTo");
				String srcDestRelName = (String)paramMap.get("srcDestRelName");

				srcDestRelName = PropertyUtil.getSchemaProperty(context,srcDestRelName.trim());

				if(isTo.equalsIgnoreCase("true")) {
					sbWhereExp.append(SYMB_AND);
					sbWhereExp.append("!('from["+srcDestRelName+"].to.id'=="+objectId+")");
				}
				else {
					sbWhereExp.append(SYMB_AND);
					sbWhereExp.append("!('to["+srcDestRelName+"].from.id'=="+objectId+")");
				}
			}

			/* To remove the duplicate object ids, from Add Existing sub types... */
			sbWhereExp.append(SYMB_AND);
			sbWhereExp.append(SYMB_OPEN_PARAN);
			sbWhereExp.append(SELECT_ID);
			sbWhereExp.append(SYMB_NOT_EQUAL);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(objectId);
			sbWhereExp.append(SYMB_QUOTE);
			sbWhereExp.append(SYMB_CLOSE_PARAN);
		}//End Condition

		return sbWhereExp.toString();
	}


	/* Method to get companies*/
	public static Object getCompanies(Context context, String[] args)
		throws Exception {
		MapList mapList = null;
		try {

			Map programMap = (Map) JPO.unpackArgs(args);
			short sQueryLimit = (short)(Integer.parseInt((String)programMap.get("queryLimit")));

			String strType = TYPE_COMPANY;

			String strName = (String)programMap.get("Name");

			if ( strName==null || strName.equals("") ) {
					strName = SYMB_WILD;
			}

			String strVault = null;
			String strVaultOption = (String)programMap.get("vaultOption");

			if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)||
			   strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||
			   strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
				strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
			else
				strVault = (String)programMap.get("vaults");

			StringList select = new StringList(1);
			select.addElement(SELECT_ID);

			mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, "", "",true, select,sQueryLimit);
		}
		catch (Exception excp) {
	  		excp.printStackTrace(System.out);
	  		throw excp;
		}

		return mapList;
	}


	/* Method to get Persons */
	public static Object getPersons(Context context, String[] args)
		throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);

		short sQueryLimit = (short)(Integer.parseInt((String)programMap.get("queryLimit")));

		String strType = TYPE_PERSON;

		String strName = (String)programMap.get("User Name");

		if ( strName==null || strName.trim().equals("") ) {
		  strName = SYMB_WILD;
		}

		String strFirstName = (String)programMap.get("First Name");
		String strLastName = (String)programMap.get("Last Name");
		String strCompany = (String)programMap.get("Company");

		String strVault = null;
		String strVaultOption = (String)programMap.get("vaultOption");

			if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
									strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
			 else
					strVault = (String)programMap.get("vaults");

		StringList select = new StringList(SELECT_ID);
		select.addElement(SELECT_NAME);

		boolean start = true;
		StringBuffer sbWhereExp = new StringBuffer(100);

		if (strFirstName!=null && (!strFirstName.equals(SYMB_WILD)) && (!strFirstName.equals("")) ) {
		  if (start) {
			sbWhereExp.append(SYMB_OPEN_PARAN);
			start = false;
		  }
		  sbWhereExp.append(SYMB_OPEN_PARAN);
		  sbWhereExp.append(SYMB_ATTRIBUTE);
		  sbWhereExp.append(SYMB_OPEN_BRACKET);
		  sbWhereExp.append(ATTRIBUTE_FIRST_NAME);
		  sbWhereExp.append(SYMB_CLOSE_BRACKET);
		  sbWhereExp.append(SYMB_MATCH);
		  sbWhereExp.append(SYMB_QUOTE);
		  sbWhereExp.append(strFirstName);
		  sbWhereExp.append(SYMB_QUOTE);
		  sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if ( strLastName!=null && (!strLastName.equals(SYMB_WILD)) && (!strLastName.equals("")) ) {
		  if (start) {
			sbWhereExp.append(SYMB_OPEN_PARAN);
			start = false;
		  } else {
			sbWhereExp.append(SYMB_AND);
		  }
		  sbWhereExp.append(SYMB_OPEN_PARAN);
		  sbWhereExp.append(SYMB_ATTRIBUTE);
		  sbWhereExp.append(SYMB_OPEN_BRACKET);
		  sbWhereExp.append(ATTRIBUTE_LAST_NAME);
		  sbWhereExp.append(SYMB_CLOSE_BRACKET);
		  sbWhereExp.append(SYMB_MATCH);
		  sbWhereExp.append(SYMB_QUOTE);
		  sbWhereExp.append(strLastName);
		  sbWhereExp.append(SYMB_QUOTE);
		  sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		if (strCompany!=null && (!strCompany.equals(SYMB_WILD)) && (!strCompany.equals("")) ) {
		  if (start) {
			sbWhereExp.append(SYMB_OPEN_PARAN);
			start = false;
		  } else {
			sbWhereExp.append(SYMB_AND);
		  }
		  sbWhereExp.append(SYMB_OPEN_PARAN);
		  sbWhereExp.append(SYMB_TO);
		  sbWhereExp.append(SYMB_OPEN_BRACKET);
		  sbWhereExp.append(RELATIONSHIP_EMPLOYEE);
		  sbWhereExp.append(SYMB_CLOSE_BRACKET);
		  sbWhereExp.append(SYMB_DOT);
		  sbWhereExp.append(SYMB_FROM);
		  sbWhereExp.append(SYMB_DOT);
		  sbWhereExp.append(SELECT_ID);
		  sbWhereExp.append(SYMB_MATCH);
		  sbWhereExp.append(SYMB_QUOTE);
		  sbWhereExp.append(strCompany);
		  sbWhereExp.append(SYMB_QUOTE);
		  sbWhereExp.append(SYMB_CLOSE_PARAN);
		}
		if (!start) {
		  sbWhereExp.append(SYMB_CLOSE_PARAN);
		}

		MapList mapList =
			DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);


		/* Get Persons Having Specified Role */
		String roleToSearch = (String)programMap.get("roleToSearch");
		String valueRoleToSearch="";
		if (roleToSearch == null || roleToSearch.equals("null") || roleToSearch.equals("")){
		    roleToSearch="";
		}
		else{
		    valueRoleToSearch = PropertyUtil.getSchemaProperty(context, roleToSearch);
  		}

  		if(!roleToSearch.equals("") && mapList.size() > 0) {

			mapList = getFilteredPersonList(context,mapList,valueRoleToSearch,sQueryLimit);
		}

		return mapList;
	}

	/* Method to Get Persons Having Specified Role (Written by Tejashwini)*/
	protected static MapList getFilteredPersonList(Context context,MapList mapList,String valueRoleToSearch,short sQueryLimit)
		throws Exception {
		// get all the assignments of the passed role and put in a stringlist.
	    MapList newResultList = new MapList();
	    Role matrixRole = new Role(valueRoleToSearch);
	    matrixRole.open(context);
	    UserItr itr = new UserItr(matrixRole.getAssignments(context));
	    matrixRole.close(context);

	    StringList assginmentsList = new StringList();
	    while(itr.next()) {
			assginmentsList.addElement(itr.obj().getName());
	  	}

	  	Iterator mapItr = mapList.iterator();
	  	int cnt = 0;
	  	int maxResult = sQueryLimit;
	  	boolean limitReached = false;
	  	while (mapItr.hasNext() && !limitReached) {
			Map map = (Map)mapItr.next();
			String name = (String)map.get(SELECT_NAME);
			if(assginmentsList.contains(name)) {
		  		if ((maxResult != 0) && (cnt >= maxResult)) {
					limitReached = true;
		  		}
		  		else {
				 	newResultList.add(map);
		  		}
		  		cnt++;
			}
        }
        return newResultList;
	}


    /* This method displays the name field (Written by Tejashwini)*/
	public static Object getNameDisplay(Context context, String args[])
		throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("<input type=\"text\" name=\"txtName\" value =\"*\" id=\"\" size=\"20\" >");
		sb.append("&nbsp;&nbsp;<input type=\"checkbox\" name=\"matchCase\" >");
		sb.append("&nbsp;Match Case");
		return sb.toString();
	}


	/* This method displays the name field
	public static Object getRevDisplay(Context context, String args[])
		throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("<script>");
		sb.append("function disableRevision(){");
		sb.append("if(document.forms[0].latestRevision.checked) {");
		sb.append("document.forms[0].txtRev.value = \"*\";");
		sb.append("document.forms[0].txtRev.disabled = true;");
		sb.append("} else {");
		sb.append("document.forms[0].txtRev.disabled = false;");
		sb.append("}");
		sb.append("}");
		sb.append("</script>");
		sb.append("<input type=\"text\" name=\"txtRev\" value =\"*\" id=\"\" size=\"20\" >");
		sb.append("&nbsp;&nbsp;<input type=\"checkbox\" name=\"latestRevision\" value =\"true\" onclick=\"javascript:disableRevision()\" >");
		sb.append("&nbsp;Latest Revision only");
		return sb.toString();
	}*/
	/* This method displays the name field (Written by Tejashwini)*/
	public static Object getRevDisplay(Context context, String args[])
	throws Exception {
		StringBuffer radioOption = null;
		try {

			radioOption = new StringBuffer(150);
			String strLocale = context.getSession().getLanguage();
			i18nNow i18nNowInstance = new i18nNow();

		        String strLatestReleased = i18nNowInstance.GetString("emxEngineeringCentralStringResource",strLocale,"emxEngineeringCentral.Form.Radio.LatestReleasedRevisionOnly");
    		        String strLatestOnly = i18nNowInstance.GetString("emxEngineeringCentralStringResource",strLocale,"emxEngineeringCentral.Form.Radio.LatestRevisionOnly");

		        //String checked = "";
		        radioOption.append("&nbsp;<input type=\"radio\" value=\"latestReleased\"");
			//radioOption.append(PersonUtil.SEARCH_ALL_VAULTS);
			radioOption.append(" name=\"revisionOption\" ");
			//radioOption.append("\"checked\"");
			radioOption.append(">");
			radioOption.append(strLatestReleased);
			radioOption.append("<br>");


			radioOption.append("&nbsp;<input type=\"radio\" value=\"latest\"");
			//radioOption.append(PersonUtil.SEARCH_ALL_VAULTS);
			radioOption.append(" name=\"revisionOption\" ");
			//radioOption.append("\"checked\"");
			radioOption.append(">");
			radioOption.append(strLatestOnly);
			radioOption.append("<br><br>");

			radioOption.append("&nbsp;<input type=\"radio\" value=\"input\"");
			//radioOption.append(selVault);
			radioOption.append("\" name=\"revisionOption\" ");
			radioOption.append("checked");
			radioOption.append(">");
			//radioOption.append(strSelected);
			radioOption.append("&nbsp;<input type=\"text\" name=\"revDisplay\" value =\"*\" id=\"\" size=\"20\" >");
			//radioOption.append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"document.forms[0].vaultOption[2].checked=true;javascript:top.showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=true&isFromSearchForm=true')\">");
			//radioOption.append("<input type=\"hidden\" name=\"revision\" value=\"");
			//radioOption.append(selVault);
			//radioOption.append("\" size=15>");

		}catch (Exception excp) {
		  excp.printStackTrace(System.out);
		  throw excp;
		}

	return radioOption.toString();
	}


    /* This method displays the policy of the a type (Written by Tejashwini)*/
    public static Object getAllPencilPolicies(Context context, String args[])
		throws Exception {
			return getDropDownPolicies(context,"Pencil");
	}

	/* This method displays the policy of the a type (Written by Tejashwini)*/
	public static Object getAllPartPolicies(Context context, String args[])
		throws Exception {
			return getDropDownPolicies(context,"Part");
	}

	/* This method returns the policy of the a type (Written by Tejashwini)*/
	public static String getDropDownPolicies(Context context, String strType)
		throws Exception {
		MapList policyList = mxType.getPolicies(context,strType,false);
		Map defaultMap = mxType.getDefaultPolicy(context, strType, false);
		String strDefaultPolicy = (String)defaultMap.get("name");
		StringBuffer sb = new StringBuffer(60);
		sb.append("<select name=\"txtPolicy\" >");

		if(policyList.size() > 1) {
			sb.append("<option value=\"");
			sb.append(SYMB_WILD);
			sb.append("\">");
			sb.append("All");
			sb.append("</option>");
		}

		for (int i=0; i<policyList.size(); i++) {
	  		String strPolicy = (String)((Map)policyList.get(i)).get("name");
	  		sb.append("<option value=\"");
	  		sb.append(strPolicy);
	  		sb.append("\">");
	  		sb.append(strPolicy);
	  		sb.append("</option>");
		}

		sb.append("</select>");
		return sb.toString();
	}


	/* Method to obtain All the company vaults for the wild card search (Written by Tejashwini)*/
	protected static String getAllCompanyVaults(Context context)
		throws Exception {
		Person person = Person.getPerson(context, context.getUser());
		return person.getCompany(context).getAllVaults(context,false);
	}

	/* Method to obtain the Default Vaults (Written by Tejashwini)*/
  	protected static String getAllVaults(Context context) throws Exception {
  		return getAllCompanyVaults(context);
	}

	/* Method to display Vault Chooser (Written by Tejashwini)*/
	public static Object getVaults(Context context, String args[])
		throws Exception {

		StringBuffer radioOption = null;
		try {
			Map documentMap = (Map) JPO.unpackArgs(args);
			String strVaults = getAllVaults(context);
			radioOption = new StringBuffer(150);

			String strLocale = context.getSession().getLanguage();
			i18nNow i18nNowInstance = new i18nNow();

			String vaultDefaultSelection = PersonUtil.getSearchDefaultSelection(context);

			String strAll = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.AllVaults");
			String strDefault = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.UserDefaultVault");
			String strSelected = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.SelectedVaults");
			String strLocal = i18nNowInstance.GetString("emxFrameworkStringResource",strLocale,"emxFramework.Preferences.LocalVaults");

			String checked = "";
			if (PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection)) {
				  checked = "checked";
			}
			radioOption.append("&nbsp;<input type=\"radio\" value=\"");
			radioOption.append(PersonUtil.SEARCH_ALL_VAULTS);
			radioOption.append("\" name=\"vaultOption\" ");
			radioOption.append(checked);
			radioOption.append(">");
			radioOption.append(strAll);
			radioOption.append("<br>");

			checked = "";
			if ( PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection)) {
				  checked = "checked";
			}
			radioOption.append("&nbsp;<input type=\"radio\" value=\"");
			radioOption.append(PersonUtil.SEARCH_LOCAL_VAULTS);
			radioOption.append("\" name=\"vaultOption\" ");
			radioOption.append(checked);
			radioOption.append(">");
			radioOption.append(strLocal);
			radioOption.append("<br>");

			checked = "";
			if (  PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection)) {
				  checked = "checked";
			}
			radioOption.append("&nbsp;<input type=\"radio\" value=\"");
			radioOption.append(PersonUtil.SEARCH_DEFAULT_VAULT);
			radioOption.append("\" name=\"vaultOption\" ");
			radioOption.append(checked);
			radioOption.append(">");
			radioOption.append(strDefault);
			radioOption.append("<br>");

			checked = "";
			String vaults = "";
			String selVault = "";
			String selDisplayVault = "";
			if (!PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection) &&
				!PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection) &&
				!PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection)) {
				checked = "checked";
				selVault = vaultDefaultSelection;
				selDisplayVault = i18nNow.getI18NVaultNames(context, vaultDefaultSelection, strLocale);
			}
			radioOption.append("&nbsp;<input type=\"radio\" value=\"");
			radioOption.append(selVault);
			radioOption.append("\" name=\"vaultOption\" ");
			radioOption.append(checked);
			radioOption.append(">");
			radioOption.append(strSelected);
			radioOption.append("&nbsp;&nbsp;<input type=\"text\" READONLY name=\"vaultsDisplay\" value =\""+selDisplayVault+"\" id=\"\" size=\"20\" onFocus=\"this.blur();\">");
			radioOption.append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"javascript:showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=true&isFromSearchForm=true',500,500)\">");
			radioOption.append("<input type=\"hidden\" name=\"vaults\" value=\"");
			radioOption.append(selVault);
			radioOption.append("\" size=15>");

		}
		catch (Throwable excp) {
			excp.printStackTrace(System.out);
		}
		return radioOption.toString();
	}

	/* This method returns the requested parameter value (Written by Tejashwini)*/
	public String getParameter(HashMap paramMap,String key)
		throws Exception {

		String paramValue = (String)paramMap.get(key);

		if (paramValue == null ||
			paramValue.equalsIgnoreCase("null") ||
			paramValue.length() == 0) {
			if(key.equals("queryLimit"))
				paramValue = "0";
			else {
				if(key.equals("txtWhereClause") ||
				   key.equals("txtPattern")||
				   key.equals("queryName"))
					paramValue = "";
				else
					paramValue = SYMB_WILD;
            }
		}
		return paramValue;
    }

    /* Method to connect the existing image to given object (Written by Tejashwini)*/
    public void addExisting (Context context, String[] args) throws Exception {
		try {
		  //Start of write transaction
		  ContextUtil.startTransaction(context, true);
		  //Unpacking the args
		  HashMap programMap = (HashMap)JPO.unpackArgs(args);


		  //Gets the objectList from args
		  HashMap reqMap = (HashMap)programMap.get("reqMap");

		  //Get all TableRowIds
		  String[] strNewObjIdArr = (String[])reqMap.get("emxTableRowId");
		  //Get the parent ID
		  String[] strParentOIdArr = (String[])reqMap.get("parentOID");
		  String strParentOId = strParentOIdArr[0];

		  //Get the reqTableMap from args
		  HashMap reqTableMap = (HashMap)programMap.get("reqTableMap");

		  //Get the relationship name
		  String[] strRelationshipNameArr = (String[])reqTableMap.get("srcDestRelName");
		  String strRelationshipName = PropertyUtil.getSchemaProperty(context,strRelationshipNameArr[0].trim());

		  //Get the isTo
		  String[] strIsToArr = (String[])reqTableMap.get("isTo");
		  String strIsTo = strIsToArr[0].trim();

		  String fromObjectId="";
		  String toObjectId="";

		  //String is initialized to store the value of ObjectID
		  String strNewObjId = "";
		  for (int a = 0; a < strNewObjIdArr.length; a++) {
			  // retriving individual ObjectID from the String Array
			  strNewObjId = strNewObjIdArr[a];
			  //Connecting the objects with a relation
			  if(strIsTo.equalsIgnoreCase("true")) {
				  toObjectId = strParentOId;
				  fromObjectId = strNewObjId;
		  	  }
		  	  else {
			  	  toObjectId = strNewObjId;
				  fromObjectId = strParentOId;
			  }

			  DomainRelationship.connect(context,
			                             fromObjectId,
			                             strRelationshipName,
			                             toObjectId,
			                             false);
		  }
		//End transaction
		ContextUtil.commitTransaction(context);
		} catch (Exception e) {
			//Transaction aborted in case of exception
			ContextUtil.abortTransaction(context);
			//The exception with appropriate message is thrown to the caller.
			throw  e;
		}
    }
 /* Method to display empty table (Written by Tejashwini)*/
    public static Object getEmptyTable (Context context, String[] args)
    		throws Exception {
		    return new MapList(10);
		}

}//End of the class
