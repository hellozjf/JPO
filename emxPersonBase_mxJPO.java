/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.Group;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Role;
import matrix.db.User;
import matrix.db.UserItr;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import java.util.Set;

import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxPersonBase_mxJPO extends emxDomainObject_mxJPO
{

    // user name sub key
    public static final String USER_NAME = "<User Name>";
    // last name sub key
    public static final String LAST_NAME = "<Last Name>";
    // first name sub key
    public static final String FIRST_NAME = "<First Name>";

    //  For member list functionality - added on 12 March 2004
    protected static final String DEFAULT = "Default";
    protected static final String ADD_EXISTING="AddExisting";
    protected static final String ALL = "All";
    protected static final String SELECTED = "Selected";
    /** relationship "Assigned Member". */
    public static final String RELATIONSHIP_ASSIGNED_MEMBER= PropertyUtil.getSchemaProperty("relationship_AssignedMember");


    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxPersonBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

    /**
     * This is a trigger method used to keep Person admin object in sync.
     * This is configured on Attribute 'Last Name', 'First Name' as modify override trigger
     * changeName event of Person type and promote/demote action
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        args[0] = even (modify/changeName)
     *        args[1] = old person name
     *        args[2] = new person name
     *        args[3] = attribute name
     *        args[4] = old attribute value
     *        args[5] = new attribute value
     * @return int 0 on success 1 in failure
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public int syncPersonAdminObject(Context context, String[] args)
        throws Exception
    {
        try
        {
            // get full name format from emxSystem.properties file
            // emxFramework.FullName.Format property
            String sFullNameFormat = EnoviaResourceBundle.getProperty(context,"emxFramework.FullName.Format");
            MessageFormat fullNameFormat = null;
            String sPattern = null;
            if (sFullNameFormat != null) {
                sPattern = FrameworkUtil.findAndReplace(sFullNameFormat, USER_NAME, "{0}");
                sPattern = FrameworkUtil.findAndReplace(sPattern, FIRST_NAME, "{1}");
                sPattern = FrameworkUtil.findAndReplace(sPattern, LAST_NAME, "{2}");
                fullNameFormat = new MessageFormat(sPattern);
            }

            // Get Event
            String sEvent = args[0];

            // If event == changeName then (Person business object name change)
            if (sEvent.equals("ChangeName"))
            {
                // Get old person name
                String sOldPersonName = args[1];

                // Get new person name
                String sNewPersonName = args[2];

                // If Full Name Format contains User Name sub key (<User Name>) then
                // Replace all the occurrences of old Person name with
                // new Person name in 'Full Name' field of Person admin object.
                if (sFullNameFormat != null && sFullNameFormat.indexOf(USER_NAME) >= 0)
                {
                    String sFullName = PersonUtil.getFullName(context, sOldPersonName);
                    try
                    {

                        Object aKeyValues[] = fullNameFormat.parse(sFullName);
                        if (aKeyValues.length == 2) {
                            Object aKeyValuesTemp[] = aKeyValues;
                            aKeyValues = new String[3];
                            aKeyValues[0] = sNewPersonName;
                            aKeyValues[1] = aKeyValuesTemp[1];
                            aKeyValues[2] = null;
                        }
                        else
                        {
                            aKeyValues[0] = sNewPersonName;
                        }
                        String sNewFullName = MessageFormat.format(sPattern, aKeyValues);
                        PersonUtil.setFullName(context, sOldPersonName, sNewFullName);
                    }
                    catch (Exception ex)
                    {
                        Object aKeyValues[] = new String[3];
                        aKeyValues[0] = sNewPersonName;
                        aKeyValues[1] = "Unknown";
                        aKeyValues[2] = "Unknown";
                        String sNewFullName = MessageFormat.format(sPattern, aKeyValues);
                        PersonUtil.setFullName(context, sOldPersonName, sNewFullName);
                    }
                }

                // Modify admin Person name to new name
                String sCmd = "modify person \"" + "$1" + "\" name \"" + "$2" + "\"";
                MqlUtil.mqlCommand(context, sCmd, true, sOldPersonName, sNewPersonName);

                // Modify any personal project roles and security context names as well.
                String roles = MqlUtil.mqlCommand(context, "list role \"$1\"", true, "*" + sOldPersonName + "_PRJ");
                StringTokenizer st = new StringTokenizer(roles, "\n");
                while (st.hasMoreTokens()) {
                	String role = st.nextToken();
                	String newRole = role.replaceFirst(sOldPersonName + "_PRJ", sNewPersonName + "_PRJ");
                	MqlUtil.mqlCommand(context, "modify role \"$1\" name \"$2\"", true, role, newRole);
                }
            }
            else if (sEvent.equals("Modify"))
            {
                // Get person name
                String sPersonName = args[1];

                // Get attribute name
                String sAttName = args[3];

                // Get old attribute value
                String sOldAttValue = args[4];

                // Get new attribute value
                String sNewAttValue = args[5];

                // If attribute name == 'First Name' then
                if (sAttName.equals(ATTRIBUTE_FIRST_NAME))
                {
                    // If Full Name Format contains First Name sub key (<First Name>) then
                    // Replace all the occurrences of old attribute values with
                    // new attribute values in 'Full Name' field of Person admin object
                    if (sFullNameFormat != null && sFullNameFormat.indexOf(FIRST_NAME) >= 0)
                    {
                        String sFullName = PersonUtil.getFullName(context, sPersonName);
                        try
                        {
                            Object aKeyValues[] = fullNameFormat.parse(sFullName);
                            if (aKeyValues.length == 2) {
                                Object aKeyValuesTemp[] = aKeyValues;
                                aKeyValues = new String[3];
                                aKeyValues[0] = sPersonName;
                                aKeyValues[1] = sNewAttValue;
                                aKeyValues[2] = null;
                            }
                            else
                            {
                                aKeyValues[0] = sPersonName;
                                aKeyValues[1] = sNewAttValue;
                            }
                            String sNewFullName = MessageFormat.format(sPattern, aKeyValues);
                            PersonUtil.setFullName(context, sPersonName, sNewFullName);
                        }
                        catch(Exception ex)
                        {
                            Object aKeyValues[] = new String[3];
                            aKeyValues[0] = sPersonName;
                            aKeyValues[1] = sNewAttValue;
                            aKeyValues[2] = "Unknown";
                            String sNewFullName = MessageFormat.format(sPattern, aKeyValues);
                            PersonUtil.setFullName(context, sPersonName, sNewFullName);
                        }
                    }
                }
                else if (sAttName.equals(ATTRIBUTE_LAST_NAME))
                {
                    // If Full Name Format contains First Name sub key (<First Name>) then
                    // Replace all the occurrences of old attribute values with
                    // new attribute values in 'Full Name' field of Person admin object
                    if (sFullNameFormat != null && sFullNameFormat.indexOf(LAST_NAME) >= 0)
                    {
                        String sFullName = PersonUtil.getFullName(context, sPersonName);
                        try
                        {
                            Object aKeyValues[] = fullNameFormat.parse(sFullName);
                            if (aKeyValues.length == 2) {
                                Object aKeyValuesTemp[] = aKeyValues;
                                aKeyValues = new String[3];
                                aKeyValues[0] = sPersonName;
                                aKeyValues[1] = null;
                                aKeyValues[2] = sNewAttValue;
                            }
                            else
                            {
                                aKeyValues[0] = sPersonName;
                                aKeyValues[2] = sNewAttValue;
                            }
                            String sNewFullName = MessageFormat.format(sPattern, aKeyValues);
                            PersonUtil.setFullName(context, sPersonName, sNewFullName);
                        }
                        catch(Exception ex)
                        {
                            Object aKeyValues[] = new String[3];
                            aKeyValues[0] = sPersonName;
                            aKeyValues[1] = "Unknown";
                            aKeyValues[2] = sNewAttValue;
                            String sNewFullName = MessageFormat.format(sPattern, aKeyValues);
                            PersonUtil.setFullName(context, sPersonName, sNewFullName);
                        }
                    }
                }
            }
			else if(sEvent.equals("Promote"))
			{
				String personName = args[1];
				String sEmailFlag ="disable";

				String personEmail=PersonUtil.getEmail(context, personName);

				if(! UIUtil.isNullOrEmpty(personEmail))
				        {
				           sEmailFlag ="enable";
				        }

				MqlUtil.mqlCommand(context, "modify person \""+"$1"+"\" type active \""+"$2"+"\" email enable iconmail", true, personName, sEmailFlag);
			}
			else if(sEvent.equals("Demote"))
			{
				String personName = args[1];
				MqlUtil.mqlCommand(context, "modify person \""+"$1"+"\" type inactive disable email disable iconmail", true, personName);

			}
        }
        catch (Exception ex)
        {
            System.out.println("Error in syncPersonAdminObject = " + ex.getMessage());
            throw ex;
        }

        return 0;
    }

    /**
     * Get the last Query run be the context person.
     *
     * @param context the eMatrix <code>Context</code> object
     * @returns HashMap containing query info.
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     * @grade 0
     */
    public HashMap getLastQuery(Context context, String[] args)
        throws Exception
    {
        HashMap lastQuery = new HashMap();
        try
        {
           lastQuery = (HashMap)PersonUtil.getLastQuery(context);
        }
        catch (Exception ex)
        {
            System.out.println("Error in getLastQuery = " + ex.getMessage());
            throw ex;
        }

        return lastQuery;
    }
  /**
    * A method used to return a value for the attribute from the matrix db which has been passed as
    * a parameter in the args. The function returns a fullname in the [Firstname Lastname] format.
    *
    * @author [RTR1] THAKUR Rishabh
    * @since 3dEXPERIENCE2015x
    * @version 1.0, 16/09/2014
    * @param context is the Matrix Context
	* @param args holds input arguments.
    * @return a string containing the name of parameter associate in [Firstname Lastname] format
    */
    public String getMemberFullName(Context context, String[] args) throws Exception
    {
    	String objectID = args[0];
    	String selects= args[1];
    	DomainObject  dObj = newInstance(context,objectID);
    	String selectedValue= dObj.getInfo(context, selects);
    	String fullname = PersonUtil.getFullName(context, selectedValue);
    	return fullname;
    }
	public String getFullName(Context context, String[] args) throws Exception
    {
		String[] adminUsers = { "SLMInstallerAdmin", "3DIndexAdminUser", "ENOVIA_CLOUD", "User Agent" };
		String fullName = "";
		List<String> lAdminUsers = Arrays.asList(adminUsers);

		if(UIUtil.isNotNullAndNotEmpty(args[0]) && !lAdminUsers.contains(args[0])) {
			fullName = PersonUtil.getFullName(context, args[0]);
		}
    	return fullName;
    }


//added on 12march - start
/**
     * Gets the Roles for Persons in PersonSearch Result Page.
     *
     * @param context The Matrix Context.
     * @param args holds input arguments.
     * @return vector containing Roles
     * @throws Exception If the operation fails.
     * @since SpecificationCentral 10.0.0.0
     */
    public StringList getRoleForPerson(Context context,String[] args) throws Exception
      {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        String[] personIds = new String[objList.size()];
        for (int i = 0; i < objList.size(); i++) {
            personIds[i] = (String)((Map)objList.get(i)).get(SELECT_ID);
        }
        StringList roleList = new StringList();
        i18nNow i18nnow = new i18nNow();
        String strLanguage = context.getSession().getLanguage();
        objList = DomainObject.getInfo(context, personIds, new StringList(SELECT_NAME));

        for (int i = 0; i < objList.size(); i++) {
          String personName = (String)((Map)objList.get(i)).get(SELECT_NAME);
          matrix.db.Person person = new matrix.db.Person(personName);
          person.open(context);
          UserItr userItr = new UserItr(person.getAssignments(context));
          person.close(context);

          StringBuffer roleString = new StringBuffer(100);
          while(userItr.next()) {
            User userObj = userItr.obj();
            if(userObj instanceof matrix.db.Role) {
                roleString.append(i18nnow.getRoleI18NString(userObj.getName(), strLanguage)).append(',');
            }
          }
          if(roleString.length() > 0)
              roleString.deleteCharAt(roleString.length() - 1);
          roleList.addElement(roleString.toString());
        }

        return roleList;
    }

/**
     * Gets the Result for Persons Search
     *
     * @param context The Matrix Context.
     * @param args holds input arguments.
     * @return maplist of Persons
     * @throws Exception If the operation fails.
     * @since SpecificationCentral 10.0.0.0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMemberListPeopleSearchResult(Context context,String[] args) throws Exception
    {
      if (args.length == 0 ) throw new IllegalArgumentException();
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      MapList mapList = new MapList();
      com.matrixone.apps.common.Company company = null;

      //Retrieve search criteria
        String UserName         =   (String) paramMap.get("UserName");
        String FirstName         =   (String) paramMap.get("FirstName");
        String LastName     =   (String) paramMap.get("LastName");
        String companyName = (String) paramMap.get("Company");
        String sQueryLimit = (String) paramMap.get("queryLimit");
        String vaultOption  = (String) paramMap.get("vaultOption");
        String selectedVault  = (String) paramMap.get("vaultName");
        String srcDestRelName =   (String) paramMap.get("srcDestRelName");
		if(srcDestRelName!=null && (!srcDestRelName.equals("")) && (!srcDestRelName.equalsIgnoreCase("null")))
		{
        	srcDestRelName = PropertyUtil.getSchemaProperty(context,srcDestRelName);
        }
        String objectId = (String) paramMap.get("objectId");
        String strMode = (String) paramMap.get("searchmode");

        if(UserName == null || "".equals(UserName))
          UserName = QUERY_WILDCARD;
        if(FirstName == null || "".equals(FirstName))
          FirstName = QUERY_WILDCARD;
        if(LastName == null || "".equals(LastName))
          LastName = QUERY_WILDCARD;

        String busWhere = "";
        if(UserName!=null && UserName.trim().length()!=0 && !UserName.trim().equals(QUERY_WILDCARD))
        {
          if(busWhere.length() > 0) {
            busWhere += " && ";
          }
          if(UserName.indexOf(QUERY_WILDCARD) == -1 && UserName.indexOf("?") == -1 )        {
            busWhere += "(name == \""+UserName+"\")";
          } else {
            busWhere += "(name ~= \""+UserName+"\")";
          }
        }
        if(ADD_EXISTING.equalsIgnoreCase(strMode) && objectId != null){
          if(busWhere.length() > 0) {
          busWhere += " && ";
          }
          busWhere += "(!to["+srcDestRelName+"].from.id ~~ \""+objectId+"\")";
          busWhere += " && ";
          busWhere += "(current == Active)";
          if(RELATIONSHIP_ASSIGNED_MEMBER.equals(srcDestRelName)) {
        busWhere += " && ";
        busWhere += "( ( to["+RELATIONSHIP_MEMBER+"].attribute["+ATTRIBUTE_PROJECT_ROLE+"] ~~ \"*role_SpecificationOfficeManager*\" ) && (to["+RELATIONSHIP_MEMBER+"].from.type == "+TYPE_COMPANY+") )";
          }
        }



        if(FirstName!=null && FirstName.trim().length()!=0 && !FirstName.trim().equals(QUERY_WILDCARD)) {
          if(busWhere.length() > 0) {
            busWhere += " && ";
          }
          if(FirstName.indexOf(QUERY_WILDCARD) == -1 && FirstName.indexOf("?") == -1 ) {
            busWhere += "(attribute["+ATTRIBUTE_FIRST_NAME+"] == \""+FirstName+"\")";
          } else {
            busWhere += "(attribute["+ATTRIBUTE_FIRST_NAME+"] ~= \""+FirstName+"\")";
          }
        }

        if(LastName!=null && LastName.trim().length()!=0 && !LastName.trim().equals(QUERY_WILDCARD))
        {
          if(busWhere.length() > 0) {
            busWhere += " && ";
          }
          if(LastName.indexOf(QUERY_WILDCARD) == -1 && LastName.indexOf("?") == -1 )        {
            busWhere += "(attribute["+ATTRIBUTE_LAST_NAME+"] == \""+LastName+"\")";
          } else {
            busWhere += "(attribute["+ATTRIBUTE_LAST_NAME+"] ~= \""+LastName+"\")";
          }
        }
        String strVaults ="";
        StringList objectSelects = new StringList(1);
        objectSelects.addElement(SELECT_ID);
        if(companyName == null || companyName.trim().length() == 0) {
          com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
          company = person.getCompany(context);
        } else {
          company = (com.matrixone.apps.common.Company)DomainObject.newInstance(context, DomainConstants.TYPE_COMPANY);//, SpecificationCentralCommon.SPECIFICATION);
          company.setId(companyName);
        }

        if(vaultOption == null) {
          com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
          strVaults = person.getVault();
        } else {
          if(vaultOption.equals(ALL)) {
            strVaults = QUERY_WILDCARD;
          } else if(vaultOption.equals(DEFAULT)) {
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            strVaults = person.getVault();
          } else if(vaultOption.equals(SELECTED)) {
            strVaults = selectedVault;
          }
        }

        if(strVaults.indexOf(QUERY_WILDCARD) == -1) {
              if(busWhere.length() > 0) { busWhere += " && "; }
              busWhere += "(vault == \""+strVaults+"\")";
        }

        mapList = (MapList) getGlobalSearchRelObjects(context,
                                                      company.getId(),
                                            RELATIONSHIP_MEMBER,
                                            TYPE_PERSON,
                                            busWhere,
                                                      sQueryLimit,
                                                      "|",
                                                      "@");



      return mapList;
    }
/**
    * Gets the MapList containing related objects according to the search criteria.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing search result.
    * @throws Exception if the operation fails.
    * @since SpecificationCentral 10.0.0.0
    */
    protected MapList getGlobalSearchRelObjects(matrix.db.Context context,
                                             String objectId,
                                             String relName,
                                             String typeName,
                                             String busWhere,
                                             String sQueryLimit,
                                             String fieldSep,
                                             String recordSep)
               throws Exception
    {
      StringBuffer mqlCommand = new StringBuffer();

      mqlCommand.append("expand bus ");
      mqlCommand.append("$1");
      mqlCommand.append(" from relationship \"");
      mqlCommand.append("$2");
      mqlCommand.append("\" type \"");
      mqlCommand.append("$3");
      mqlCommand.append("\" select bus id where '");
      mqlCommand.append("$4");
      mqlCommand.append("' dump ");
      mqlCommand.append("$5");
      mqlCommand.append(" recordsep ");
      mqlCommand.append("$6");
      mqlCommand.append(" terse limit ");
      mqlCommand.append("$7");

      String strResult = MqlUtil.mqlCommand(context,mqlCommand.toString(), objectId, relName, typeName, busWhere, fieldSep, recordSep, "" + Short.parseShort(sQueryLimit));
      java.util.List resultList = FrameworkUtil.split(strResult,fieldSep,recordSep);

      Iterator itr = resultList.iterator();
      StringList resultMap = null;
      HashMap hashMap = null;
      MapList mapList = new MapList();
      while(itr.hasNext()) {
        resultMap = (StringList)itr.next();
        if(resultMap.size() > 0) {
          hashMap = new HashMap();
          hashMap.put(KEY_LEVEL,resultMap.elementAt(0));
          hashMap.put(SELECT_ID,resultMap.elementAt(4));
          mapList.add(hashMap);
        }
      }
      return mapList;
    }

//added on 12 march - end

    /**
     * Get Objects for the specified criteria in Person Search.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    userName      - a String of specified criteria user name
     *    lastName      - a String of specified criteria last name
     *    firstName     - a String of specified criteria first name
     *    orgId         - a String of specified criteria Organization
     *    QueryLimit    - a String of limit on the number of objects found
     * @return MapList containing objects for search result
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPersonSearchResult(Context context , String[] args)
       throws Exception
    {

    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    MapList mapList = new MapList();

    HashMap requestValuesMap = (HashMap) paramMap.get("RequestValuesMap");

    String[] caseSensitiveValue = (String[]) requestValuesMap.get("caseSensitiveSearch");
    String caseSensitive = "";

    if(caseSensitiveValue!=null){
    	caseSensitive = (String) caseSensitiveValue[0];
    }
    if(!UIUtil.isNullOrEmpty(caseSensitive) && caseSensitive.equalsIgnoreCase("true"))
    {
    	caseSensitive = "~=";
    }else
    {
    	caseSensitive = "~~";
    }

    //Retrieve Search criteria
    String strUserName      = (String)paramMap.get("userName");
    String strLastName      = (String)paramMap.get("lastName");
    String strFirstName     = (String)paramMap.get("firstName");
    String strOrgId         = (String)paramMap.get("orgId");
    String languageStr      = (String)paramMap.get("languageStr");
    //added for bug 299784
    String fromFile        =(String)paramMap.get("FromFile");


    DomainObject domOrgObj = new DomainObject(strOrgId);
    String strOrgType = domOrgObj.getInfo(context,DomainObject.SELECT_TYPE);

    String queryLimit = (String)paramMap.get("QueryLimit");

    int intQueryLimit = 0;
    if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals(""))
    {
        intQueryLimit = Integer.MAX_VALUE;
    }
    else
    {
        try
        {
            intQueryLimit = Integer.parseInt(queryLimit);
        }
        catch(Exception e)
        {
            intQueryLimit = Integer.MAX_VALUE;
        }
    }

    if (strUserName == null || strUserName.equalsIgnoreCase("null") || strUserName.length() <= 0)
    {
          strUserName = "*";
    }
    if (strLastName == null || strLastName.equalsIgnoreCase("null") || strLastName.length() <= 0)
    {
          strLastName = "*";
    }
    if (strFirstName == null || strFirstName.equalsIgnoreCase("null") || strFirstName.length() <= 0)
    {
          strFirstName = "*";
    }

    String typeCompany          = DomainObject.TYPE_COMPANY;
    String typeBusinessUnit     = DomainObject.TYPE_BUSINESS_UNIT;
    String attrLastName         = DomainObject.ATTRIBUTE_LAST_NAME;
    String attrFirstName        = DomainObject.ATTRIBUTE_FIRST_NAME;
    String name                 = DomainObject.SELECT_NAME;
    String typePerson           = DomainObject.TYPE_PERSON;
    String relEmployee          = DomainObject.RELATIONSHIP_EMPLOYEE;
    String relBusUnitEmployee   = DomainObject.RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE;
    String sPersonActiveState = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_PERSON, "state_Active");

    String relPattern = "";
    if(strOrgType.equals(typeBusinessUnit))
    {
        relPattern = relBusUnitEmployee;
    }
    else
    {
        relPattern = relEmployee;
    }

    String strRelWhere = "";
    //Modified for bug 336145
    if(!strUserName.equals("*") && !strUserName.equals(""))
    {
        if(strUserName.indexOf("*") == -1)
        {
            strRelWhere += "(to.name smatchlist \"" + strUserName + "\" \",\")";
        } else {
            strRelWhere += "(to.name " +caseSensitive+ " \""+strUserName+"\")";
        }
    }

    if(!strLastName.equals("*") && !strLastName.equals(""))
    {
        if(strLastName.indexOf("*") == -1)
        {
            if(strRelWhere.length()>0) {strRelWhere += " && ";}
            strRelWhere += "(\"" + "to.attribute["+attrLastName+"]\" smatchlist \"" + strLastName + "\" \",\")";
        } else {
            if(strRelWhere.length()>0) {strRelWhere += " && ";}
            strRelWhere += "(to.attribute["+attrLastName+"]" +caseSensitive+ " \""+strLastName+"\")";
        }
    }
    if(!strFirstName.equals("*") && !strFirstName.equals(""))
    {
        if(strFirstName.indexOf("*") == -1)
        {
            if(strRelWhere.length()>0) {strRelWhere += " && ";}
            strRelWhere += "(\"" + "to.attribute["+attrFirstName+"]\" smatchlist \"" + strFirstName + "\" \",\")";
        } else {
            if(strRelWhere.length()>0) {strRelWhere += " && ";}
            strRelWhere += "(to.attribute["+attrFirstName+"] " +caseSensitive+ " \""+strFirstName+"\")";
        }
    }
    //End of modification for bug 336145

    /* To display only the Active person in Person Search Results */
        if(strRelWhere.length()>0) {strRelWhere += " && ";}
        strRelWhere += "(to.current.name == \'"+sPersonActiveState+"\')";
    SelectList selectStmts = new SelectList(2);
    selectStmts.add(DomainObject.SELECT_ID);
    selectStmts.add(DomainObject.SELECT_NAME);
    selectStmts.add(DomainObject.SELECT_CURRENT);

    strRelWhere = FrameworkUtil.findAndReplace(strRelWhere,".*",".**");
    strRelWhere = FrameworkUtil.findAndReplace(strRelWhere,"*.","**.");

    try
    {
        ContextUtil.startTransaction(context,false);
        ExpansionIterator expIter   = domOrgObj.getExpansionIterator(context,
                                                                    relPattern,
                                                                    typePerson,
                                                                    selectStmts,
                                                                    new SelectList(),
                                                                    false,
                                                                    true,
                                                                    (short)1,
                                                                    null,
                                                                    strRelWhere,
                                                                    (short)0,
                                                                    false,
                                                                    false,
                                                                    (short)100,
                                                                    false);

        mapList = FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
        expIter.close();
        ContextUtil.commitTransaction(context);
    }
    catch(Exception ex)
    {
        ContextUtil.abortTransaction(context);
        throw new Exception(ex.toString());
    }
    //added for 299784
    if( fromFile!=null && fromFile.length()>0 && fromFile.trim().equals("emxComponentsFindMemberDialog.jsp") )
    {
        Iterator mapItr = mapList.iterator();
        MapList constructedList = new MapList();

        String cState = "";
        while (mapItr.hasNext()) {
            HashMap tempMap = new HashMap();
            Map map = (Map)mapItr.next();
            cState = (String)map.get(DomainObject.SELECT_CURRENT);
            if( cState.equals(DomainConstants.STATE_PERSON_ACTIVE)) {
                constructedList.add(map);
            }
        }
        mapList=constructedList;
    }
    //till here

    if(mapList != null)
    {
        // default dir is ascending and type is string (for sorting)
        mapList.sort(DomainObject.SELECT_NAME,null,null);

        if(mapList.size() > intQueryLimit)
        {
            String strMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Warning.ObjectFindLimit") + queryLimit + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Warning.Reached");
            emxContextUtil_mxJPO.mqlNotice(context,strMessage);

            MapList returnMapList = new MapList();
            for( int i = 0 ; i < intQueryLimit ; i++ )
            {
                returnMapList.add(mapList.get(i));
            }
            return returnMapList;
        }
    }
    return mapList;
  }

    /**
    * Updates the person's default vault value in Admin person definition
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments. first parameter contains personname and second vault name
    * @return void.
    * @throws Exception if the operation fails.
    * @since Profile Mgmt 10.5
    */
    public void updateAdminPersonDefaultVault(matrix.db.Context context, String [] args)
               throws Exception
    {
            if (args.length == 0 )
            {
                throw new IllegalArgumentException();
            }
            String strPersonName = args[0];
            String strVault = args[1];

            String strMql = "modify person \"" + "$1" + "\" vault \"" + "$2" + "\" ";
            try
            {
                ContextUtil.pushContext(context);
                MQLCommand prMQL  = new MQLCommand();
                prMQL.open(context);
                prMQL.executeCommand(context,strMql, strPersonName, strVault);
                String error = prMQL.getError();
                prMQL.close(context);
                if(error != null && error.length() > 0)
                {
                    throw new Exception(error);
                }
            }
            finally
            {
                ContextUtil.popContext(context);
            }
    }
    /**
    * Updates the person's attributes to Admin person definition
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * 1st parameter - Object Id
    * 2st parameter - Person Name
    * 3rd parameter - Attribute Name
    * 4th parameter - Attribute Value
    * @return void.
    * @throws Exception if the operation fails.
    * @since Profile Mgmt 10.5
    */
    public void updateAdminPersonAttributes(matrix.db.Context context, String [] args)
               throws Exception
    {
        if (args.length == 0 )
        {
            throw new IllegalArgumentException();
        }
        String strObjectId          = args[0];
        String strPersonName        = args[1];
        String strAttributeName     = args[2];
        String strAttributeValue    = args[3];


        boolean isAttributeModified = false;
        String strUpdateString  = "";

        com.matrixone.apps.common.Person personObject = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);

        if(personObject.ATTRIBUTE_ADDRESS.equals(strAttributeName))
        {
            isAttributeModified = true;
            strAttributeValue = FrameworkUtil.findAndReplace(strAttributeValue, "\n", " , ");
            strUpdateString = " address " + " \"" + "$2" + "\" ";
        }
        else if(personObject.ATTRIBUTE_WORK_PHONE_NUMBER.equals(strAttributeName))
        {
            isAttributeModified = true;
            strUpdateString = " phone " + " \"" + "$2" + "\" ";
        }
        else if(personObject.ATTRIBUTE_FAX_NUMBER.equals(strAttributeName))
        {
            isAttributeModified = true;
            strUpdateString = " fax " + " \"" + "$2" + "\" ";
        }
        else if(personObject.ATTRIBUTE_FIRST_NAME.equals(strAttributeName))
        {
            // get Full Name format from emxSystem.properties file
            String strFullNameFormat = EnoviaResourceBundle.getProperty(context,"emxFramework.FullName.Format");
            if (strFullNameFormat != null)
            {
                String strPattern = FrameworkUtil.findAndReplace(strFullNameFormat, USER_NAME, "{0}");
                strPattern = FrameworkUtil.findAndReplace(strPattern, FIRST_NAME, "{1}");
                strPattern = FrameworkUtil.findAndReplace(strPattern, LAST_NAME, "{2}");

                personObject.setId(strObjectId);
                String strLastName = personObject.getInfo(context,personObject.SELECT_LAST_NAME);

                Object arrKeyValues [] = new Object [] {strPersonName,strAttributeValue,strLastName};
                String strNewFullName = MessageFormat.format(strPattern, arrKeyValues);

                PersonUtil.setFullName(context, strPersonName, strNewFullName);
            }
        }
        else if(personObject.ATTRIBUTE_LAST_NAME.equals(strAttributeName))
        {
            // get Full Name format from emxSystem.properties file
            String strFullNameFormat = EnoviaResourceBundle.getProperty(context,"emxFramework.FullName.Format");
            if (strFullNameFormat != null)
            {
                String strPattern = FrameworkUtil.findAndReplace(strFullNameFormat, USER_NAME, "{0}");
                strPattern = FrameworkUtil.findAndReplace(strPattern, FIRST_NAME, "{1}");
                strPattern = FrameworkUtil.findAndReplace(strPattern, LAST_NAME, "{2}");

                personObject.setId(strObjectId);
                String strFirstName = personObject.getInfo(context,personObject.SELECT_FIRST_NAME);

                Object arrKeyValues [] = new Object [] {strPersonName,strFirstName,strAttributeValue};
                String strNewFullName = MessageFormat.format(strPattern, arrKeyValues);

                PersonUtil.setFullName(context, strPersonName, strNewFullName);
            }
        }

        if(isAttributeModified)
        {
            StringBuffer strBufMQL = new StringBuffer();
            strBufMQL.append(" modify person ");
            strBufMQL.append(" \"").append("$1").append("\" ");
            strBufMQL.append(strUpdateString);

            MqlUtil.mqlCommand(context,strBufMQL.toString(),true, strPersonName, strAttributeValue);

        }
    }


    /**
    * It sends the notification user during email address change. Its part of override trigger
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * 1st parameter - Object Id
    * 2st parameter - Person Name
    * 3rd parameter - Attribute Name
    * 4th parameter - Attribute Default Value
    * 5th parameter - Attribute Old Value
    * 6th parameter - Attribute New Value
    * 7th parameter - Objects's Current State
    * 8th parameter - Context User Name
    * 9th parameter - Time Stamp
    * @return void.
    * @throws Exception if the operation fails.
    * @since Profile Mgmt 10.5
    */
    public void overrideAdminPersonAttributes(matrix.db.Context context, String [] args)
               throws Exception
    {
        if (args.length == 0 )
        {
            throw new IllegalArgumentException();
        }
        String strObjectId          = args[0];
        String strPersonName        = args[1];
        String strAttributeName     = args[2];
        String strAttributeDefault  = args[3];
        String strAttributeValue    = args[4];
        String strNewAttributeValue = args[5];
        String strCurrentState      = args[6];
        String strContextUser       = args[7];
        String strTimeStamp         = args[8];



        com.matrixone.apps.common.Person personObject = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);

        if(personObject.ATTRIBUTE_EMAIL_ADDRESS.equals(strAttributeName))
        {

            if(STATE_PERSON_ACTIVE.equals(strCurrentState))
            {
                if(!strAttributeValue.equals(strAttributeDefault))
                {
                    String [] arrMailArgs = {
                                            strPersonName
                                            ,"emxFramework.ProgramObject.eServicecommonTrigaNotifyEmailChange_if.Subject"
                                            ,"0"
                                            ,"emxFramework.ProgramObject.eServicecommonTrigaNotifyEmailChange_if.Message"
                                            ,"4"
                                            ,"oldEmail"
                                            ,strAttributeValue
                                            ,"newEmail"
                                            ,strNewAttributeValue
                                            ,"user"
                                            ,strContextUser
                                            ,"time"
                                            ,strTimeStamp
                                            };
                    emxMailUtil_mxJPO.sendNotificationToUser(context,arrMailArgs);
                }
            }
            else
            {
                String [] arrMailArgs = {
                                        strPersonName
                                        ,"emxFramework.ProgramObject.eServicecommonTrigaNotifyEmailChange_if.RegSubject"
                                        ,"0"
                                        ,"emxFramework.ProgramObject.eServicecommonTrigaNotifyEmailChange_if.RegMessage"
                                        ,"0"
                                        };
                emxMailUtil_mxJPO.sendNotificationToUser(context,arrMailArgs);
            }
        }

    }


    /**
     * It Updates the email address change. Its part of override trigger
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * 1st parameter - Object Id
     * 2st parameter - Person Name
     * 3rd parameter - Attribute Name
     * 4th parameter - Attribute New Value
     * @return void.
     * @throws Exception if the operation fails.
     * @since 3DExperience 2015x.FD01
     */
     public void overrideUpdateEmailAddress(matrix.db.Context context, String [] args)
                throws Exception
     {
    	  	if (args.length == 0 )
            {
                throw new IllegalArgumentException();
            }
            String strObjectId          = args[0];
            String strPersonName        = args[1];
            String strAttributeName     = args[2];
            String strNewAttributeValue = args[3];

            com.matrixone.apps.common.Person personObject = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);

            if(personObject.ATTRIBUTE_EMAIL_ADDRESS.equals(strAttributeName))
            {
                MqlUtil.mqlCommand(context,"modify person $1 email $2",true, strPersonName, strNewAttributeValue);
            }
     }


	public String getStrAllRolesSymbolicAssigned(Context context, String args[]) throws Exception {
		String i18nNowRole;

		String strDel = matrix.db.SelectConstants.cSelectDelimiter;
   		boolean geti18nRoleNames = args[args.length -1].toString().equals("i18nStr");
		if (args.length >= 2 && !geti18nRoleNames)
		{
			strDel = "|";
		}
		String personName = args[0];
		StringList roleList = new StringList();
		Set assignments = null;
		String retValue = "";
		assignments = PersonUtil.getAllUserAssignments(context, personName);
		for (Iterator iterator = assignments.iterator(); iterator.hasNext();) {
			Object elem = iterator.next();
			roleList.add(elem.toString());
		}

		retValue = FrameworkUtil.join(roleList, strDel );
		return retValue;
	}

   public String getStrRolesSymbolicAssigned(Context context, String args[])
	    throws Exception {

	String personId = args[0];
	String i18nNowRole;

	String strDel = matrix.db.SelectConstants.cSelectDelimiter;
    boolean geti18nRoleNames = args[args.length -1].toString().equals("i18nStr");
	if (args.length >= 3 && !geti18nRoleNames)
	{
	    strDel = "|";
	}
	DomainObject personObj = new DomainObject(personId);

	matrix.util.SelectList busList = new matrix.util.SelectList(2);

	busList.add(SELECT_NAME);

	busList.add(SELECT_TYPE);

	Map personMap = personObj.getInfo(context, busList);

	String strType = (String) personMap.get(SELECT_TYPE);

	String retValue = "";

    	if (mxType.isOfParentType(context, strType, DomainConstants.TYPE_PERSON)) {

	    matrix.db.Person mxDbPerson = new matrix.db.Person(
		    (String) personMap.get(SELECT_NAME));

	    mxDbPerson.open(context);

	    matrix.db.UserItr userItr = new matrix.db.UserItr(mxDbPerson
		    .getAssignments(context));

	    retValue = "";
    		StringList roleList = new StringList();
    		 String mxRoleName = "";
    		while (userItr.next())
    		{
				matrix.db.User userObj = userItr.obj();
				if (userObj instanceof matrix.db.Role)
				{
							 Role matrixRole = new Role(userItr.obj().getName());
							 mxRoleName = matrixRole.getName();
							 if(geti18nRoleNames){
								 if(mxRoleName.endsWith("_PRJ") || mxRoleName.startsWith("ctx::")){
									 continue;
								 }
								 i18nNowRole = EnoviaResourceBundle.getRoleI18NString(context,matrixRole.getName(), context.getSession().getLanguage());
							 }
							 else{
								 i18nNowRole = mxRoleName;
							 }
							 if(!roleList.contains(i18nNowRole))
							 {
								roleList.add(i18nNowRole);
								if(!geti18nRoleNames){
    				getRoles(context, matrixRole,roleList,geti18nRoleNames);
								}
							 }
				}
    		}
    		retValue = FrameworkUtil.join(roleList, strDel);
	    mxDbPerson.close(context);
	}
	return retValue;
    }

    private void getRoles(Context context, Role userObj, StringList roleList, boolean geti18nRoleNames) throws MatrixException
    {
    	Role matrixRole = userObj;
    	matrixRole.open(context);
    	String i18nNowRole;
    	StringList parentRoleList = matrixRole.getParents();
    	if(null!=parentRoleList && parentRoleList.size()>0)
    	{
    		for (Iterator iterator = parentRoleList.iterator(); iterator.hasNext();)
    		{
    			String parentRoleName = (String) iterator.next();
    			if(geti18nRoleNames && !parentRoleName.startsWith("ctx::")){
    				i18nNowRole = EnoviaResourceBundle.getRoleI18NString(context, parentRoleName, context.getSession().getLanguage());
    			}else{
    				i18nNowRole = parentRoleName;
    			}
    			if(!roleList.contains(i18nNowRole))
    			{
    			    Role parentRole = new Role(parentRoleName);
    			    getRoles(context, parentRole, roleList, geti18nRoleNames);
    				roleList.add(i18nNowRole);

    			}
    		}
    	}
    	matrixRole.close(context);
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignedRoleList(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            String personId = (String)programMap.get("objectId");
            String personName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, personId, "name");
            List assignments = getPersonAssignments(context, personName, true);
            List hidden = FrameworkUtil.split(MqlUtil.mqlCommand(context, "list role $1 where $2", false, "*", "hidden"), System.getProperty("line.separator"));
            hidden.retainAll(assignments);
            return new emxRoleUtil_mxJPO(context, args).getRoleListForSummaryTable(assignments, hidden);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignedGroupList(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            String personId = (String)programMap.get("objectId");
            String personName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", false, personId, "name");

            List assignments = getPersonAssignments(context, personName, false);
            List hidden = FrameworkUtil.split(MqlUtil.mqlCommand(context, "list group $1 where $2", false, "*", "hidden"), System.getProperty("line.separator"));
            hidden.retainAll(assignments);

            return new emxGroupUtil_mxJPO(context, args).getGroupListForSummaryTable(assignments, hidden);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    private List getPersonAssignments(Context context, String personName, boolean isRole) throws FrameworkException {

        try {
            List assignments = new ArrayList();
            matrix.db.Person mxDbPerson = new matrix.db.Person(personName);
            mxDbPerson.open(context);
            UserItr userItr = new UserItr(mxDbPerson.getAssignments(context));
            while(userItr.next()) {
                User userObj = userItr.obj();
                String name = userObj.getName();
                if((isRole && userObj instanceof Role) || (!isRole && userObj instanceof Group)) {
                    assignments.add(name);
                }
            }
            return assignments;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    public void addENGRoles(Context context, String[] args) throws FrameworkException {
   	 try {

   		 boolean cloud = UINavigatorUtil.isCloud(context);
   		 if (cloud) {
   			 String personId = args[0];
   			 String SCId = args[1];
   			 String personName = args[2];
   			 String SCName = args[3];
   			 Map relMemList = null;
   			 String memberRelId = "";

   			 String strRole = SCName.substring(0, SCName.indexOf("."));

   			 if("VPLMProjectLeader".equals(strRole))
   			 {
   				 com.matrixone.apps.common.Person personID = new com.matrixone.apps.common.Person(personId);
                    StringList sExistingRoles = personID.getRoleAssignments(context);

                    if (!sExistingRoles.contains("role_ECRCoordinator"))
      				 {

   				 StringList strRoleList = new StringList();
   				 strRoleList.addElement("Senior Design Engineer");
   				 strRoleList.addElement("Senior Manufacturing Engineer");
   				 strRoleList.addElement("ECR Chairman");
   				 strRoleList.addElement("ECR Coordinator");

      				 MqlUtil.mqlCommand(context, "modify person '"+"$1"+"' type $2",true, context.getUser(), "system");
      				 String str1 = MqlUtil.mqlCommand(context, "print person '"+"$1"+"' select $2", context.getUser(), "system");
   				 personID.addRoles(context, strRoleList);

   				 // Assignment of Lead Roles
   				 StringList relSelects = new StringList(1);
   				 relSelects.add(DomainConstants.SELECT_ID);

   				 MapList memberMapList = personID.getRelatedObjects(
   						 context,            // context
   						 RELATIONSHIP_MEMBER,// relationship pattern
   						 DomainConstants.TYPE_ORGANIZATION,     // type pattern
   						 null,               // object selects
   						 relSelects,         // relationship selects
   						 true,               // to direction
   						 false,              // from direction
   						 (short) 1,          // recursion level
   						 "",        // object where clause
   						 null);

   				 if (memberMapList != null || memberMapList.size() > 1)
   				 {
   					 for (int i = 0; i < memberMapList.size(); i++) {
   						 relMemList =  (Map) memberMapList.get(i);
   						 memberRelId = (String) relMemList.get(DomainConstants.SELECT_ID);
   						 if (memberRelId != null && !"".equals(memberRelId))
   						 {
   							 String iResult2 = Organization.checkLeadRoleAssign(context, personId, memberRelId,
   									 new String[] {"role_ECRChairman","role_ECRCoordinator"});

      							 if(iResult2.equals("")){
      							     Organization.setLeadRoles(context, personId, memberRelId, new String[] {"role_ECRChairman", "role_ECRCoordinator"});
   					 }
      						 }  //End of Member id List
      					 } // End for
      				 }  // End Member Map List
   				 }
   			 }
   		 }
   	 } catch (Exception e) {
   	  	 }
        finally {
   	try {
       MqlUtil.mqlCommand(context, "modify person '"+"$1"+"' type $2",true, context.getUser(), "!system");
        }
        catch (Exception e1) { }
    }

       }

	/**
	 * Updates the perosn default information during its creation
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args request arguments
	 * @return 0 if the person default data is set successfully.
	 * @throws MatrixException if operation fails.
	 */
	public int setPersonDefaultInfo(Context context, String[] args) throws MatrixException{
		try{
			return setPersonDefaultCurrency(context, args);
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	/**
	 * Updates the perosn default preferred currency during person creation
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args request arguments
	 * @return 0 if the preffered currency will be set successfully.
	 * @throws MatrixException if operation fails.
	 */
	private int setPersonDefaultCurrency(Context context, String[] args) throws MatrixException{
		try{
			String personId = args[0];
			DomainObject person = DomainObject.newInstance(context, personId);
			String personName = person.getInfo(context, DomainConstants.SELECT_NAME);
		    String strCurrencyChoice = EnoviaResourceBundle.getProperty(context,"emxComponents.UserPreferenceCurrency");
		    if(!UIUtil.isNullOrEmpty(strCurrencyChoice)){
		        PropertyUtil.setAdminProperty(context, "person", personName, "preference_Currency", strCurrencyChoice);
		    }
			return 0;
		}catch(Exception e){
			throw new MatrixException(e);
		}
	}

	public void createPersonalWorkspace(Context context, String[] args) throws Exception
    {
        try
        {
            String companyId = args[0];
            String personId = args[1];
            DomainObject person = DomainObject.newInstance(context, personId);
            String personName = person.getInfo(context, DomainObject.SELECT_NAME);
            DomainObject org = DomainObject.newInstance(context, companyId);
            String orgName = org.getInfo(context, DomainObject.SELECT_NAME);
            String TYPE_PERSONAL_WORKSPACE = PropertyUtil.getSchemaProperty(context, "type_PersonalWorkspace");
            String POLICY_PERSONAL_WORKSPACE = PropertyUtil.getSchemaProperty(context, "policy_PersonalWorkspace");
            String strPersonalWorkspaceName = "";
            try{
            	strPersonalWorkspaceName = EnoviaResourceBundle.getProperty(context,"emxComponents.Default.PersonalWorkspaceName");
            } catch (Exception mex)
            {
            	//mex.printStackTrace();
            }
            if(strPersonalWorkspaceName == null || "".equals(strPersonalWorkspaceName))
            {
            	strPersonalWorkspaceName =  personName + "Personal Workspace";
            }
            String ATTRIBUTE_FOLDER_CLASSIFICATION = PropertyUtil.getSchemaProperty(context, "attribute_FolderClassification");
            String cmd = "add bus $1 $2 $3 policy $4 vault $5 owner $6 project $7 organization $8 $9 $10";
            MqlUtil.mqlCommand(context, cmd, true, TYPE_PERSONAL_WORKSPACE, strPersonalWorkspaceName, personName, POLICY_PERSONAL_WORKSPACE, person.getDefaultVault(context, person), personName, personName+"_PRJ",orgName, ATTRIBUTE_FOLDER_CLASSIFICATION, "Personal");
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        	throw new MatrixException(ex);
        }
    }
    @SuppressWarnings("unchecked")
	public void deletePersonalWorkspace(Context context, String[] args) throws Exception
    {
        try
        {
        	ContextUtil.pushContext(context);
        	String TYPE_PERSONAL_WORKSPACE = PropertyUtil.getSchemaProperty(context, "type_PersonalWorkspace");
            String personId = args[0];
            DomainObject person = DomainObject.newInstance(context, personId);
            String personName = person.getInfo(context, DomainObject.SELECT_NAME);
            StringList objectSelects = new StringList(5);
            objectSelects.addElement("project");
            objectSelects.addElement(SELECT_ID);
            MapList queryList = DomainObject.findObjects(context, TYPE_PERSONAL_WORKSPACE, "*", personName, personName, person.getDefaultVault(context, person), (String)null, (String)null, true, objectSelects, (short)0);
            if( queryList.size() > 0)
            {
            	Map<String, String> personalWorkspaceMap = (Map<String, String>)queryList.get(0);
            	String personalWorksapceId = (String)personalWorkspaceMap.get(SELECT_ID);
            	String cmd = "delete bus $1";
            	MqlUtil.mqlCommand(context, cmd, personalWorksapceId);
            }
        }
        catch(Exception ex)
        {
        	throw new MatrixException(ex);
        }
        finally
        {
        	ContextUtil.popContext(context);
        }
    }
	public String getPersonalWorkspaceId(Context context, String[] args) throws Exception
    {
		String personId = args[0];
		return getPersonalWorkspaceId(context, personId);
    }
    @SuppressWarnings("unchecked")
	public String getPersonalWorkspaceId(Context context, String personId) throws Exception
    {
        try
        {	String personalWorksapceId = "";
        	String TYPE_PERSONAL_WORKSPACE = PropertyUtil.getSchemaProperty(context, "type_PersonalWorkspace");
            DomainObject person = DomainObject.newInstance(context, personId);
            String personName = person.getInfo(context, DomainObject.SELECT_NAME);
            StringList objectSelects = new StringList(5);
            objectSelects.addElement("project");
            objectSelects.addElement(SELECT_ID);
            MapList queryList = DomainObject.findObjects(context, TYPE_PERSONAL_WORKSPACE, "*", personName, personName, person.getDefaultVault(context, person), (String)null, (String)null, true, objectSelects, (short)0);
            if( queryList.size() > 0)
            {
            	Map<String, String> personalWorkspaceMap = (Map<String, String>)queryList.get(0);
            	personalWorksapceId = (String)personalWorkspaceMap.get(SELECT_ID);
            }
            return personalWorksapceId;
        }
        catch(Exception ex)
        {
        	throw new MatrixException(ex);
        }
    }
	public boolean showPeopleSearch(Context context, String[] args){
    	return !UINavigatorUtil.isTopFrameEnabled(context);
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
	public Boolean hasAccessOnRoleCommand(Context context, String[] args) throws Exception {
		Boolean hasAccess = new Boolean(false);

		HashMap requestMap = (HashMap) JPO.unpackArgs(args);
		String strObjectId = (String) requestMap.get("objectId");
		DomainObject doObj = DomainObject.newInstance(context, strObjectId);

		String objOwner = doObj.getOwner(context).getName();
		if(context.getUser().equalsIgnoreCase(objOwner)) {
			hasAccess = true;
		} else {
			Vector userRoles = PersonUtil.getAssignments(context);

			StringList accessRolesList = new StringList();
			String[] accessRolesArray = {"role_VPLMAdmin", "role_OrganizationManager", "role_CompanyRepresentative"};
			for(String tempRole: accessRolesArray) {
				accessRolesList.add(PropertyUtil.getSchemaProperty(context, tempRole));
			}

			hasAccess = UICache.checkRoleBasedAccess(userRoles, accessRolesList);
		}

		return hasAccess;
	}
}
