/*   emxObjectAccessBase.
**
**   Copyright (c) 2006-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the implementation of emxObjectAccess.
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;

/**
 * The <code>emxObjectAccessBase</code> class contains implementation code for emxObjectAccess.
 *
 * @version CommonComponents 11.0.JCI.0 - Copyright (c) 2006, MatrixOne, Inc.
 */
public class emxObjectAccessBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since CommonComponents 11.0
    * @grade 0
    */
    public emxObjectAccessBase_mxJPO (Context context, String[] args)
    throws Exception
    {
        super(context, args);
    }


    /**
    * This method is executed if a specific method is not specified.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @return int.
    * @throws Exception if the operation fails.
    * @since CommonComponents 11.0
    */
    public int mxMain(Context context, String[] args)
    throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.ObjectAccess.SpecifyMethodOnObjectAccessInvocation", context.getLocale().getLanguage()));
        }

        return 0;
    }


    /**
     * Gets Access Grantee information for the object.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectId - a String containing the concerned object id.
     * pushGrantor - a String containing the name of the user with whom the push context is done for granting the access.
     * editMode - a String either true or false for deciding the mode.
     * charSet - a String containing the character setting.
     * showAllProgram - a String containing the program name which is called for deciding if all access grantees are to be shown to context user.
     * showAllFunction - a String containing the function name which is called for the above purpose.
     * accessChoice - a String containing the comma seperated values of the access rights available.
     * @return MapList of the Grantees.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAccessGrantees (Context context,String[] args) throws Exception
    {
        MapList accessMapList = new MapList();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String)programMap.get("objectId");
        HashMap requestValuesMap = (HashMap)programMap.get("RequestValuesMap");
        String languageStr = (String)requestValuesMap.get("languageStr");

        // get the access grantor from the command parameter
        String accessGrantor = (String)programMap.get("pushGrantor");
        String editMode = (String)programMap.get("editMode");

        if (editMode == null || "null".equals(editMode) || "".equals(editMode) || !"true".equals(editMode))
        {
            editMode = "false";
        }

        if (accessGrantor == null || "null".equals(accessGrantor) || "".equals(accessGrantor.trim()))
        {
            accessGrantor = PropertyUtil.getSchemaProperty(context, "person_CommonAccessGrantor");
        }
        else
        {
            // get the real name of the grantor from the symbolic name.
            accessGrantor = PropertyUtil.getSchemaProperty(context, accessGrantor);
        }

        String strCharSet =(String)programMap.get("charSet");
        if(strCharSet == null || "null".equals(strCharSet) || strCharSet.trim().equals(""))
        {
            strCharSet = "UTF8";
        }

        // get the parameters which decide the content to show to the user
        String program = (String)programMap.get("showAllProgram");
        String function = (String)programMap.get("showAllFunction");

        boolean showAll = false;
        if(program != null && !"null".equals(program) && !"".equals(program) && function != null && !"null".equals(function) && !"".equals(function))
        {
            HashMap argsMap = new HashMap(1);
			argsMap.put("objectId",objectId);
			String [] arguments = JPO.packArgs(argsMap);

			Boolean showAllValue = (Boolean)JPO.invoke(context, program, null, function, arguments, Boolean.class);
            showAll = showAllValue.booleanValue();
        }

        String contextUser = context.getUser();
        matrix.db.Person personObj = new matrix.db.Person(contextUser);

        // Get the access choices passed to the command
        String accessChoice = (String)programMap.get("accessChoice");
        // Get the valid access list depending on the properties defined
        StringList validAccessList = getValidAccessList (context, accessChoice);

        // Get the actual granted accesses on the object.
        DomainObject doObj = new DomainObject(objectId);
        AccessList accessList = doObj.getAccessForGrantor(context, accessGrantor);
        Iterator itr = accessList.iterator();
        while (itr.hasNext())
        {
            Map map = new HashMap();
            Access accessMask = (Access) itr.next();
            String access = ObjectAccess.checkUserAccess(accessMask, languageStr);
            String user = accessMask.getUser();

            // Put the name as the id for the view mode and "" as id for the edit mode
            if ("false".equalsIgnoreCase(editMode))
            {
                map.put("id", FrameworkUtil.encodeNonAlphaNumeric(user, strCharSet));
            }
            else
            {
                map.put("id", "");
            }

            Map dataMap = new HashMap();
            dataMap.put("userName", user);
            dataMap.put(user, access);

            // Find the type of the user i.e. Person or Role or Group
            String cmd = MqlUtil.mqlCommand(context, "print user $1 select $2 $3 $4 dump $5", user, "isaperson", "isarole", "isagroup","|");
            boolean isPerson = "TRUE|FALSE|FALSE".equalsIgnoreCase(cmd);
            boolean isRole = "FALSE|TRUE|FALSE".equalsIgnoreCase(cmd);
            boolean isGroup = "FALSE|FALSE|TRUE".equalsIgnoreCase(cmd);

            // For the user whom all the grants need to be shown
            if (showAll)
            {
                if("false".equalsIgnoreCase(editMode) || validAccessList.contains(access))
                {
                    if (isPerson)
                    {
                        if("false".equalsIgnoreCase(editMode) || !user.equalsIgnoreCase(contextUser))
                        {
                            dataMap.put("type", "Person");
                            com.matrixone.apps.common.Person tempPerson = com.matrixone.apps.common.Person.getPerson(context, user);
                            String companyName = tempPerson.getInfo(context, "to[" + RELATIONSHIP_EMPLOYEE + "].from.name");
                            if (companyName == null || "null".equals(companyName))
                            {
                                companyName = "";
                            }
                            dataMap.put("organization", companyName);
                            dataMap.put("editAccess", ""+ (!user.equals(contextUser) && validAccessList.contains(access)));
                            map.put("dataMap", dataMap);
                            accessMapList.add(map);
                        }
                    }
                    else
                    {
                        if("false".equalsIgnoreCase(editMode) || validAccessList.contains(access))
                        {
                            if (isRole)
                            {
                                dataMap.put("type", "Role");
                            }
                            else if (isGroup)
                            {
                                dataMap.put("type", "Group");
                            }
                            else
                            {
                                dataMap.put("type", "Association");
                            }
                            dataMap.put("organization", " ");
                            dataMap.put("editAccess", ""+ validAccessList.contains(access));
                            map.put("dataMap", dataMap);
                            accessMapList.add(map);
                        }
                    }
                }
            }
            else
            {    // For the user whom only limited view is available
                if ("false".equalsIgnoreCase(editMode))
                {
                    if (isPerson && user.equalsIgnoreCase(contextUser))
                    {
                        dataMap.put("type", "Person");
                        com.matrixone.apps.common.Person tempPerson = com.matrixone.apps.common.Person.getPerson(context, user);
                        String companyName = tempPerson.getInfo(context, "to[" + RELATIONSHIP_EMPLOYEE + "].from.name");
                        if (companyName == null || "null".equals(companyName))
                        {
                            companyName = "";
                        }
                        dataMap.put("organization", companyName);
                        dataMap.put("editAccess", "false");
                        map.put("dataMap", dataMap);
                        accessMapList.add(map);
                    }
                    else if (personObj.isAssigned(context, user))
                    {
                        if (isRole)
                        {
                            dataMap.put("type", "Role");
                        }
                        else if (isGroup)
                        {
                            dataMap.put("type", "Group");
                        }
                        else
                        {
                            dataMap.put("type", "Association");
                        }
                        dataMap.put("organization", " ");
                        dataMap.put("editAccess", "false");
                        map.put("dataMap", dataMap);
                        accessMapList.add(map);
                    }
                }
            }
        }
        return accessMapList;
    }

    /**
     * Gets the vector output for the checkbox column in the access summary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * paramList - a HashMap containing the following parameters.
     * editMode - a String either true or false for deciding the mode.
     * @return Vector containing the true or false values.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Vector getUserCheckboxes (Context context,String[] args) throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        MapList objList     = (MapList)programMap.get("objectList");
        HashMap paramList = (HashMap) programMap.get("paramList");
        String editMode = (String)paramList.get("editMode");
        if (editMode == null || "null".equals(editMode) || "".equals(editMode) || !"true".equals(editMode))
        {
            editMode = "false";
        }

        int objListSize = objList.size();
        Vector columnVals   = new Vector(objListSize);
        for (int k=0; k < objListSize; k++)
        {
            Map map = (Map)objList.get(k);
            HashMap dataMap = (HashMap)map.get("dataMap");
            String name = (String)dataMap.get("userName");
            String access = (String)dataMap.get(name);
            String type = (String)dataMap.get("type");
            String editAccess = (String)dataMap.get("editAccess");

            if ("false".equalsIgnoreCase(editMode))
            {
                // Add the enabled checkbox for the view mode and for the users which are editable and removable
                columnVals.addElement("" + "true".equalsIgnoreCase(editAccess));
            }
            else
            {
                columnVals.addElement("true");
            }
        }
        return columnVals;
    }


    /**
     * Gets the vector output in HTML format, for the Name column in the access summary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * paramList - a HashMap containing the following parameters.
     * editMode - a String either true or false for deciding the mode.
     * reportFormat - a String to identify the Printer Friendly and Export view.
     * @return Vector of the user display names in the HTML format.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Vector getUserNames (Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramList = (HashMap) programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        // Added for Bug # 344847 - Begin
        String languageStr = (String)paramList.get("languageStr");
        // Added for Bug # 344847 - End

        boolean isprinterFriendly = false;
        if(reportFormat != null)
        {
           isprinterFriendly = true;
        }
        String editMode = (String)paramList.get("editMode");
        if (!"true".equals(editMode) && !isprinterFriendly)
        {
            editMode = "false";
        }

        MapList objList = (MapList)programMap.get("objectList");
        Vector columnVals = new Vector(objList.size());

        for (int k=0; k < objList.size(); k++)
        {
            Map map = (Map) objList.get(k);
            HashMap dataMap = (HashMap)map.get("dataMap");

            String name = (String)dataMap.get("userName");
            String type = (String)dataMap.get("type");
            StringBuffer strBuffName = new StringBuffer();
            // Added for Bug # 344847 - Begin
            if(type.equals("Role"))
            {
                name = i18nNow.getAdminI18NString("Role",name,languageStr);
            }
            if(type.equals("Group"))
            {
                name = i18nNow.getAdminI18NString("Group",name,languageStr);
            }
            // Added for Bug # 344847 - End

            if("false".equalsIgnoreCase(editMode) && type.equalsIgnoreCase("Person"))
            {
                // Add the hyperlinked name with image, for the person in view mode which is not printer friendly
                com.matrixone.apps.common.Person personObj = com.matrixone.apps.common.Person.getPerson(context, name);
                String personId = personObj.getId();
                name = com.matrixone.apps.common.Person.getDisplayName(context, name);
                name = FrameworkUtil.findAndReplace(name, ",", ", ");
                // The next line is added just for the sake of sorting functionality to work properly in the Table page
                if (!"CSV".equalsIgnoreCase(reportFormat))
                {
                    strBuffName.append("<!--"+name+"-->");
                }
                strBuffName.append("<img src=\"../common/images/iconSmallPerson.gif\" border=\"0\"/>&nbsp;");
                strBuffName.append("<b><a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert&jsTreeID="+(String)paramList.get("jsTreeID")+"&emxSuiteDirectory=components&objectId=");
                strBuffName.append(personId);
                strBuffName.append("', '', '', 'false', 'content', '')\">");
                strBuffName.append(XSSUtil.encodeForHTML(context,name));
                strBuffName.append("</a></b>&nbsp;");
                columnVals.addElement(strBuffName.toString());
            }
            else
            {
                // Add the name with image, for the groups and roles and for all users in edit mode
                if (type.equalsIgnoreCase("Person"))
                {
                    name = com.matrixone.apps.common.Person.getDisplayName(context, name);
                    if ("CSV".equalsIgnoreCase(reportFormat))
                    {
                        name = FrameworkUtil.findAndReplace(name, ",", " ");
                    }
                    else
                    {
                        name = FrameworkUtil.findAndReplace(name, ",", ", ");
                    }
                }
                if (!"CSV".equalsIgnoreCase(reportFormat))
                {
                    // The next line is added just for the sake of sorting functionality to work properly in the Table page
                    strBuffName.append("<!--"+name+"-->");
                    strBuffName.append("<img src=\"../common/images/iconSmall"+type+".gif\" border=\"0\"/>&nbsp;");
                }
                if ("true".equals(editMode))
                {
                    String tempName = (String)dataMap.get("userName");
                    strBuffName.append("<input type=\"hidden\" name=\"User"+k+"\" value=\"" + tempName + "\">");
                    strBuffName.append("<input type=\"hidden\" name=\"OldAccess"+k+"\" value=\"" + (String)dataMap.get(tempName) + "\">");
                }
                strBuffName.append(name);
                columnVals.addElement(strBuffName.toString());
            }
        }
        return columnVals;
    }


    /**
     * Gets the vector output, for the user Type column in the access summary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * paramList - a HashMap containing the following parameters.
     * languageStr - a String containing the language information.
     * @return Vector of the user Types in internationalized format.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Vector getUserTypes (Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        HashMap paramList = (HashMap) programMap.get("paramList");
        String languageStr = (String)paramList.get("languageStr");
        Vector columnVals = new Vector(objList.size());

        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            HashMap dataMap = (HashMap) ((Map)i.next()).get("dataMap");
            String type = (String)dataMap.get("type");
            // Internationalize the values of Role, Group or Person.
            if ("Person".equalsIgnoreCase(type))
            {
                columnVals.addElement(i18nNow.getTypeI18NString(type,languageStr));
            }
            else
            {
                columnVals.addElement(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common." + type));
            }
        }
        return columnVals;
    }



    /**
     * Does nothing.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * @return 0.
     * @since CommonComponents 10-7
     */

    public int doNothing(Context context, String[] args)
    {
		return 0;
    }

    /**
     * Gets the vector output, for the user Organization column in the access summary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * @return Vector of the user Organizations.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Vector getUserOrganizations (Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        Vector columnVals = new Vector(objList.size());

        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map map = (Map) i.next();
            HashMap dataMap = (HashMap)map.get("dataMap");
            columnVals.addElement((String)dataMap.get("organization"));
        }
        return columnVals;
    }


    /**
     * Gets the vector output in HTML format, for the Access column in the access summary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * charSet - a MapList containing the actual maps "dataMap" containing the data.
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * paramList - a HashMap containing the following parameters.
     * editMode - a String either true or false for deciding the mode.
     * reportFormat - a String to identify the Printer Friendly and Export view.
     * languageStr - a String containing the language information.
     * accessChoice - a String containing the comma seperated values of the access rights available.
     * @return Vector containing the user access or access choices in HTML format.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Vector getUserAccess (Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        HashMap paramList = (HashMap) programMap.get("paramList");
        String languageStr = (String)paramList.get("languageStr");
        boolean isEditMode = "true".equalsIgnoreCase((String) paramList.get("editMode"));//Is the table in edit mode?

        int objListSize = objList.size();
        Vector columnVals = new Vector(objListSize);
        for (int k=0; k < objListSize; k++)
        {
            Map map = (Map)objList.get(k);
            HashMap dataMap = (HashMap)map.get("dataMap");
            String access = (String)dataMap.get((String)dataMap.get("userName"));
            
            if (!isEditMode) {
                // In readonly mode we want the access values to be internationalized
            String strTempAccess = FrameworkUtil.findAndReplace(access, " ", "");
            strTempAccess = "emxComponents.ObjectAccess."+strTempAccess.trim();
            columnVals.addElement(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),strTempAccess));
        }
            else {
                // In edit mode the access values are used to select the correct value from list box (which is already internationalized)
                columnVals.addElement(access);
            }
        }
        
        return columnVals;
    }


    /**
     * Gets the vector output in HTML format, for the New Window column in the access summary table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectList - a MapList containing the actual maps "dataMap" containing the data.
     * paramList - a HashMap containing the following parameters.
     * editMode - a String either true or false for deciding the mode.
     * reportFormat - a String to identify the Printer Friendly and Export view.
     * @return Vector containing the new window icon with the hyperlink in HTML format.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Vector getUserNewWindow (Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        int objListSize = objList.size();
        HashMap paramList = (HashMap) programMap.get("paramList");
        boolean isprinterFriendly = false;
        if(paramList.get("reportFormat") != null)
        {
           isprinterFriendly = true;
        }
        String editMode = (String)paramList.get("editMode");
        if (!"true".equals(editMode))
        {
            editMode = "false";
        }

        Vector columnVals = new Vector(objListSize);
        for (int i=0; i < objListSize; i++)
        {
            StringBuffer strBuff = new StringBuffer();
            Map map = (Map) objList.get(i);
            HashMap dataMap = (HashMap)map.get("dataMap");
            if ("false".equalsIgnoreCase(editMode) && ((String)dataMap.get("type")).equalsIgnoreCase("Person"))
            {
                // Show the new window icon for the users of type Person with the Hyperlink
                if (!isprinterFriendly)
                {
                    String name = (String)dataMap.get("userName");
                    com.matrixone.apps.common.Person personObj = com.matrixone.apps.common.Person.getPerson(context, name);
                    String personId = personObj.getId();
                    strBuff.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&objectId=");
                    strBuff.append(XSSUtil.encodeForJavaScript(context,personId));
                    strBuff.append("', '");
                    strBuff.append("700");
                    strBuff.append("', '");
                    strBuff.append("600");
                    strBuff.append("', 'false', '");
                    strBuff.append("popup");
                    strBuff.append("')");
                    strBuff.append("\">");
                }
                strBuff.append("<img src=\"../common/images/iconNewWindow.gif\" border=\"0\" align=center/>");
                if (!isprinterFriendly)
                {
                    strBuff.append("</a>&nbsp;");
                }
            }
            else
            {
                strBuff.append("&nbsp;");
            }

            columnVals.addElement(strBuff.toString());
        }
        return columnVals;
    }


    /**
     * Gets the access choices which are to be shown in the select list for the access colummn in edti table page.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * requestMap - a HashMap containing the following parameters.
     * languageStr - a String containing the language information.
     * accessChoice - a String containing the comma seperated values of the access rights available.
     * @return HashMap containing the actual and display values of the access choices.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public HashMap getAccessChoices (Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String languageStr = (String)requestMap.get("languageStr");
        String accessChoice = (String)requestMap.get("accessChoice");

        // Get the valid access choices list
        StringList validAccessList = getValidAccessList(context, accessChoice);

        HashMap accessMap = new HashMap();
        StringList displayValueList = new StringList();
        StringList actualValueList = new StringList();
        String strAccess = "";
        String strTempAccess = "";
        int validAccessListSize = validAccessList.size();

        for (int i = 0; i < validAccessListSize; i++)
        {
            strAccess = (String)validAccessList.get(i);
            // Internationalize the access values for displying
            strTempAccess = FrameworkUtil.findAndReplace(strAccess, " ", "");
            strTempAccess = "emxComponents.ObjectAccess."+strTempAccess.trim();
            displayValueList.addElement(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),strTempAccess));
            actualValueList.addElement(strAccess);
        }

        // Put the display and actual values
        accessMap.put("field_display_choices", displayValueList);
        accessMap.put("field_choices", actualValueList);
        return accessMap;
    }


    /**
     * Checks if the current mode is view or edit depending upon the parameter passed.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * editMode - a String either true or false for deciding the mode.
     * @return Boolean depending on the editMode parameter.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Boolean checkViewMode (Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String editMode = (String)programMap.get("editMode");
        return Boolean.valueOf(!"true".equalsIgnoreCase(editMode));
    }


    /**
     * Checks if the context user has the Librarian role or not.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap
     * @return Boolean depending on if the context user has the Librarian role or not.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public Boolean hasLibrarianRole (Context context, String[] args)
        throws Exception
    {
        String userName = context.getUser();
		matrix.db.Person personObj = new matrix.db.Person(userName);
        // Check if the person is assigned the Librarian role
        boolean isLibrarian = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_Librarian"));
        boolean isVPLMPrjAdmin = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_VPLMProjectAdministrator"));
        boolean isVPLMPrjLeader = personObj.isAssigned(context, PropertyUtil.getSchemaProperty(context,"role_VPLMProjectLeader"));
        return Boolean.valueOf( isLibrarian || isVPLMPrjAdmin || isVPLMPrjLeader || userName.equals(PropertyUtil.getSchemaProperty(context, "person_UserAgent")) );
    }


    /**
     * Gets the valid accesses from the string of accesses passed as a parameter.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param access - a String containing the comma seperated values of the access rights.
     * @return StringList containing the valid accesses.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public StringList getValidAccessList (Context context, String access) throws Exception
    {
        StringList strListAccess = new StringList();
        StringList validAccessList = new StringList();

        if(access != null && !"null".equals(access) && !"".equals(access))
        {
            access = FrameworkUtil.findAndReplace(access, "_", " ");

            // This is the list of available access rights
            strListAccess = FrameworkUtil.split(access, ",");
            int strListAccessSize = strListAccess.size();

            // Checks if the property is defined for each access
            for (int k=0; k < strListAccessSize; k++)
            {
                String tempAccess = (String)strListAccess.get(k);
                String propValue = EnoviaResourceBundle.getProperty(context,"emxComponents.AccessMapping."+FrameworkUtil.findAndReplace(tempAccess, " ", "").trim());
                if(propValue != null && !"null".equals(propValue) && !"".equals(propValue))
                {
                    validAccessList.add(tempAccess);
                }
            }
        }
        return validAccessList;
    }


    /**
     * Sets or Revokes the user grant on the object.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args - contains a packed HashMap with the following entries:
     * dataMap - a HashMap containing the user and access information.
     * boList - a BusinessObjectList containing the Business Objects.
     * accessGrantor - a String representing the access grantor for the push context.
     * charSet - a MapList containing the actual maps "dataMap" containing the data.
     * languageStr - a String containing the language information.
     * action - a String representing the action to be performed. It can be add, remove or edit
     * @return void.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

    public static void setUserAccesses (Context context, String[] args) throws Exception
    {
        HashMap argsMap = (HashMap) JPO.unpackArgs(args);
        HashMap accessMap = (HashMap) argsMap.get("dataMap");
        BusinessObjectList boList = (BusinessObjectList) argsMap.get("boList");
        String accessGrantor = (String) argsMap.get("accessGrantor");
        if (accessGrantor == null || "null".equals(accessGrantor) || "".equals(accessGrantor.trim()))
        {
            accessGrantor = PropertyUtil.getSchemaProperty(context, "person_CommonAccessGrantor");
        }
        else
        {
            // get the real name of the grantor from the symbolic name.
            accessGrantor = PropertyUtil.getSchemaProperty(context, accessGrantor);
        }
        String strCharSet =(String)argsMap.get("charSet");
        if(strCharSet == null || "null".equals(strCharSet) || strCharSet.trim().equals(""))
        {
            strCharSet = "UTF8";
        }
        String action =(String)argsMap.get("action");
        if(action == null || "null".equals(action) || action.trim().equals(""))
        {
            action = "";
        }
        String languageStr = (String) argsMap.get("languageStr");

        ContextUtil.startTransaction(context, true);

        try
        {
            // track removals
            StringList revokeList = new StringList();
            HashMap mailHashMap = new HashMap();

            // utility object to maintain all users permissions
            AccessUtil au = new AccessUtil();
            AccessList accessList = au.getAccessList();
            String[] accessStrArray = {ObjectAccess.READ_WO_DOWNLOAD, au.READ, au.READ_WRITE, au.ADD, au.REMOVE, au.ADD_REMOVE};

            // loop through the users and add their mask to the access list
            java.util.Set set = accessMap.keySet();
            Iterator itr = set.iterator();
            while (itr.hasNext())
            {
                String user = (String) itr.next();
                String access = (String) accessMap.get(user);
                user = FrameworkUtil.decodeURL(user, strCharSet);

                if (access.equals(au.NONE))
                {
                    revokeList.add(user);
                    mailHashMap.put(user, au.NONE);
                }
                for(int i=0; i<accessStrArray.length; i++)
                {
                    if(accessStrArray[i].equals(access))
                    {
                        Access accObj = ObjectAccess.setAccessRight(context,accessStrArray[i], languageStr);
                        au.setAccess(user, accessGrantor, accObj);
                        mailHashMap.put(user, accessStrArray[i]);
                        break;
                    }
                }
            }

            ContextUtil.pushContext(context, accessGrantor, null, null);
            try
            {
                if (accessList.size() > 0)
                {
                    MqlUtil.mqlCommand(context, "trigger off", true);

                    // set the rights for all the users for the given objects
                    matrix.db.BusinessObject.grantAccessRights(context, boList, accessList);
                    MqlUtil.mqlCommand(context, "trigger on", true);
                }

                if (revokeList.size() > 0)
                {
                    MqlUtil.mqlCommand(context, "trigger off", true);

                    // revoke the access rights for the selected users
                    matrix.db.BusinessObject.revokeAccessRights(context, boList, revokeList);
                    MqlUtil.mqlCommand(context, "trigger on", true);
                }
            }
            finally
            {
                ContextUtil.popContext(context);
            }

            String sendNotification = EnoviaResourceBundle.getProperty(context,"emxComponents.ObjectAccess.SendNotification");
            // If the property is set, then send the notification mails to the users
            if ("true".equalsIgnoreCase(sendNotification))
            {
                sendAccessGrantNotification (context, mailHashMap, action, boList, languageStr);
            }

            ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(e);
        }
    }

    /**
     * Sends the notifications to the users for the access grant, modify or revoke.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param userMap - a HashMap containing the user and access information.
     * @param action - a String denoting the action taking place ie. add or revoke.
     * @param boList - a BusinessObjectList containing the business objects passed.
     * @param languageStr - a String containing the language information.
     * @return void.
     * @throws Exception if the operation fails.
     * @since CommonComponents 11.0
     */

   public static void sendAccessGrantNotification (Context context, HashMap userMap, String action, BusinessObjectList boList, String languageStr) throws Exception
   {
        String basePropFile = "emxComponentsStringResource";
        BusinessObjectItr busObjItr = new BusinessObjectItr(boList);
        StringList strListBusObj = new StringList();
        String objId = "";
        while(busObjItr.next())
        {
            BusinessObject busObj = busObjItr.obj();
            if ("remove".equalsIgnoreCase(action))
            {
                objId = busObj.getObjectId();
            }
            else
            {
                strListBusObj.addElement(busObj.getName());
            }
        }

        String objType = null;
        String objName = null;
        String objRevision = null;

        // For revoking the access, get the TNR of the object to be used in the mail message
        if ("remove".equalsIgnoreCase(action))
        {
            if (objId != null && !"null".equals(objId) && !"".equals(objId))
            {
                DomainObject domObj = new DomainObject(objId);

                StringList objectSelects = new StringList(3);
                objectSelects.addElement(DomainConstants.SELECT_TYPE);
                objectSelects.addElement(DomainConstants.SELECT_NAME);
                objectSelects.addElement(DomainConstants.SELECT_REVISION);

                Map objMap = domObj.getInfo(context, objectSelects);

                objType = (String)objMap.get(DomainConstants.SELECT_TYPE);
                objName = (String)objMap.get(DomainConstants.SELECT_NAME);
                objRevision = (String)objMap.get(DomainConstants.SELECT_REVISION);
            }
        }

        String grantAction = null;
        if ("add".equalsIgnoreCase(action))
        {
            grantAction = "Grant";
        }
        else if ("remove".equalsIgnoreCase(action))
        {
            grantAction = "Revoke";
        }
        else
        {
            grantAction = "Modify";
        }
        String subjectKey = "emxComponents.ObjectAccess.MailSubject."+ grantAction +"Access";

        Iterator itr = userMap.keySet().iterator();
        while(itr.hasNext())
        {
            StringList toList = new StringList();
            StringList ccList = new StringList();
            StringList bccList = new StringList();
            String[] subjectKeys = {};
            String[] subjectValues = {};
            String[] messageKeys = new String[4];
            String[] messageValues = new String[4];
            String userName = (String) itr.next();
            String access = (String)userMap.get(userName);
            String strType = null;
            String strTempAccess = FrameworkUtil.findAndReplace(access, " ", "");
            strTempAccess = "emxComponents.ObjectAccess."+strTempAccess.trim();
            strTempAccess = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),strTempAccess);

            // To see if the user is a person or role or group
            String cmd = MqlUtil.mqlCommand(context, "print user $1 select $2 $3 $4 dump $5", userName, "isaperson", "isarole", "isagroup","|");
            boolean isPerson = "TRUE|FALSE|FALSE".equalsIgnoreCase(cmd);
            boolean isRole = "FALSE|TRUE|FALSE".equalsIgnoreCase(cmd);
            boolean isGroup = "FALSE|FALSE|TRUE".equalsIgnoreCase(cmd);
            String userType = "";

            if (isPerson)
            {
                userType = "Person";
                toList.addElement(userName);
            }
            else if (isRole || isGroup)
            {
                userType = "RoleGroup";
                UserItr usrItr = null;
                if (isRole)
                {
                    strType = "Role";
                    matrix.db.Role roleObj = new matrix.db.Role(userName);
                    usrItr = new UserItr(roleObj.getAssignments(context));
                }
                else
                {
                    strType = "Group";
                    matrix.db.Group groupObj = new matrix.db.Group(userName);
                    usrItr = new UserItr(groupObj.getAssignments(context));
                }
                // Get the people having this role and add them to the 'to' list
                while(usrItr.next())
                {
                    User userObj = usrItr.obj();
                    toList.addElement(userObj.getName());
                }
            }
            String messageKey = "emxComponents.ObjectAccess.MailMessage."+ userType + grantAction +"Access";

            // Form the message keys and values for the Person type of users
            if ("Person".equals(userType))
            {
                if ("remove".equalsIgnoreCase(action))
                {
                    // Message keys for the remove action ie. revoking of access
                    messageKeys[0] = "type";
                    messageValues[0] = objType;
                    messageKeys[1] = "name";
                    messageValues[1] = objName;
                    messageKeys[2] = "rev";
                    messageValues[2] = objRevision;
                }
                else
                {
                    // Message keys for the modification of access
                    messageKeys[0] = "strAccess";
                    messageValues[0] = strTempAccess;
                }
            }
            else if ("RoleGroup".equals(userType))
            {
                // Form the message keys and values for the Role type of users
                if ("remove".equalsIgnoreCase(action))
                {
                    // Message keys for the remove action ie. revoking of access
                    messageKeys[0] = "rolegroupName";
                    messageValues[0] = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common." + strType) + " '" + userName + "'";
                    messageKeys[1] = "type";
                    messageValues[1] = objType;
                    messageKeys[2] = "name";
                    messageValues[2] = objName;
                    messageKeys[3] = "rev";
                    messageValues[3] = objRevision;
                }
                else
                {
                    // Message keys for the modification of access
                    messageKeys[0] = "strAccess";
                    messageValues[0] = strTempAccess;
                    messageKeys[1] = "rolegroupName";
                    messageValues[1] = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common." + strType) + " '" + userName + "'";
                }
            }

            // Call the send notification method to actually send the mail
            emxMailUtil_mxJPO.sendNotification(context, toList, ccList, bccList, subjectKey, subjectKeys, subjectValues, messageKey, messageKeys, messageValues, strListBusObj, "", basePropFile);
        }
   }

   
   /**
	* This method is used to check whether Context user is owner of the business object passed or has any of roles 
	* listed as property emxComponents.AccessComponent.ObjectAccess in the emxComponents.properties file 
	* @param context The ematrix context of the request.
	* @param args The String array containing Packed arguments, This also contains list of object ids
	* @return Boolean object - true if context user has access otherwise false
	* @since Common 11.0
	*/

   public Boolean hasAccessRoles (Context context, String[] args)
   {
	   try
	   {
		    HashMap argsMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String) argsMap.get("objectId");

		   
			DomainObject domObj = new DomainObject(objectId);
			String owner = (domObj.getOwner(context)).getName();
			if(owner!=null && owner.equals(context.getUser()))
			{
			   return Boolean.valueOf(true);
			}

            return hasLibrarianRole(context, args);

	   }
	   catch (Exception fe)
	   {
			return Boolean.valueOf(false);
	   }
    }

}
