/*   emxOrganizationBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxOrganization
**
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.RelationshipWithSelectList;
import matrix.db.ServerVersion;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Plant;
import com.matrixone.apps.common.util.AttributeUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.StringUtils;
/**
 * The <code>emxOrganizationBase</code> class contains implementation code for emxOrganizationBase.
 *3 * @version Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxOrganizationBase_mxJPO extends emxDomainObject_mxJPO
{

    /** selects an object's RDO. */
    public static final String SELECT_RDO_ID =
            "to[Design Responsibility].from.id";
    public static HashMap assRoleMap = new HashMap();

    public static final String TYPE_MEMBER_LIST = PropertyUtil.getSchemaProperty(DomainSymbolicConstants.SYMBOLIC_type_MemberList);
    public static final String RELATIONSHIP_MEMBER_LIST = PropertyUtil.getSchemaProperty(DomainSymbolicConstants.SYMBOLIC_relationship_MemberList);

    private static final Map countryChooserDetails = new HashMap();

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since EC 9.5.JCI.0
     * @grade 0
     */
    public emxOrganizationBase_mxJPO (Context context, String[] args)
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
     * @since EC 9.5.JCI.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CompanyBase.SpecifyMethodOnPartInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    /*
     * Returns whether the person is an employee of the organizational type.
     *
     * @param context The Matrix Context.
     * @param args holds object id list and parameter list
     * @return a Vector of part origin values
     * @throws FrameworkException If the operation fails.
     * @since Common 10-0-0-0
     * @grade 0
     */
    public Vector getIfEmployee(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String organizationId = (String) paramMap.get("objectId");
        String languageStr = (String)paramMap.get("languageStr");

        // we need to get the type of the object that we want to connect the eople to so that
        // we know what relationship to use later
        BusinessObject busObj = new BusinessObject(organizationId);
        busObj.open(context);
        String sObjectTypeName = busObj.getTypeName();
        String typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", sObjectTypeName, true);
        busObj.close(context);

        Vector columnValues = new Vector(relBusObjPageList.size());

        String bArr [] = new String[relBusObjPageList.size()];
        for (int i = 0; i < relBusObjPageList.size(); i++) {
             bArr [i] = (String)((Map)relBusObjPageList.get(i)).get(SELECT_ID);
        }

        String selectable = "type_Department".equals(typeAlias) ?   "to[" + Organization.RELATIONSHIP_DEPARTMENT_EMPLOYEE + "].from.id" :
                            "type_BusinessUnit".equals(typeAlias) ? "to[" + DomainConstants.RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE + "].from.id" :
                                                                    "to[" + DomainConstants.RELATIONSHIP_EMPLOYEE + "].from.id";

        String YES   =EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Yes");
        String NO    =EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.No");

        MapList personInfo = DomainObject.getInfo(context, bArr, new StringList(selectable));
        for (int i = 0; i < personInfo.size(); i++) {
            Map info = (Map) personInfo.get(i);
            columnValues.add(organizationId.equals(info.get(selectable)) ? YES : NO);
        }
        return columnValues;
    }


    /*
     * Returns the roles the person has for the organization.
     *
     * @param context The Matrix Context.
     * @param args holds object id list and parameter list
     * @return a Vector of part origin values
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     * @grade 0
     */
    public Vector getOrganizationalRoles(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String strLanguage      = (String)paramMap.get("languageStr");

        Vector columnValues = new Vector(relBusObjPageList.size());
        try {
            String[] relIds = new String[relBusObjPageList.size()];
            for (int i = 0; i < relBusObjPageList.size(); i++) {
                relIds[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }
            String selAttrProjRole = DomainObject.getAttributeSelect(ATTRIBUTE_PROJECT_ROLE);
            MapList orgRoles = DomainRelationship.getInfo(context, relIds, new StringList(selAttrProjRole));
            String ROLE_SEPERATOR = ", ";
            for (int i = 0; i < orgRoles.size(); i++) {
                Map relAttributes = (Map) orgRoles.get(i);
                StringList rolesList = FrameworkUtil.split((String) relAttributes.get(selAttrProjRole), "~");
                columnValues.add(getI18NRoleListValue(context, strLanguage, rolesList));
            }
        }
        catch (FrameworkException Ex){
             throw Ex;
        }
        return columnValues;
    }

    
    /** To get the type of License, it can be Casual or Full
     * @param context
     * @param args
     * @return a vector
     * @throws Exception
     */
    public Vector getLicenseType(Context context, String[] args)throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        Vector columnValues = new Vector(objPageList.size());
        
        try {
            for (int i = 0; i < objPageList.size(); i++) {
            	String selAttrCasualHours  = (String)((Hashtable)objPageList.get(i)).get("attribute[" + ATTRIBUTE_LICENSED_HOURS + "]");
                Integer iCasualHours = (Integer.parseInt(selAttrCasualHours));
                if(iCasualHours > 0){
                	columnValues.add(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(context.getSession().getLanguage()), "emxComponents.Common.Licensing.Casual"));
                }else{
                	columnValues.add(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(context.getSession().getLanguage()), "emxComponents.Common.Licensing.Full"));
                }
            }
        }
        catch (Exception Ex){
             throw Ex;
        }
        return columnValues;
    }

    protected String getI18NRoleListValue(Context context, String strLanguage, StringList rolesList) throws MatrixException {
        String ROLE_SEPERATOR = ", ";
        int roleSize = rolesList.size();
        switch (roleSize) {
        case 0:
            return EMPTY_STRING;
        default:
            StringBuffer buffer = new StringBuffer();
            int j = 0;
            for (;j < roleSize - 1; j++) {
                buffer.append(getI18NRoleName(context, strLanguage, (String) rolesList.get(j))).append(ROLE_SEPERATOR);
            }
            buffer.append(getI18NRoleName(context, strLanguage, (String) rolesList.get(j)));
            return buffer.toString();
        }
    }

    protected String getI18NRoleName(Context context, String strLanguage, String strrole) throws MatrixException {
        strrole = PropertyUtil.getSchemaProperty(context,strrole);
        strrole = i18nNow.getRoleI18NString(strrole.trim() ,strLanguage);
        return strrole;
    }

     /**
     * This method gets the Collaboration Partner Summary.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments as it is getting the params list from the Config UI table
     * @returns Maplist of Collaboration Partner Objects
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCollaborationPartners (Context context, String[] args)
        throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);

        // getting the object Id.
        String orgId = (String) paramMap.get("objectId");
        MapList collaborationPartnerList = new MapList();
        try
        {
            Organization org = new Organization(orgId);
            // creating the select list
            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(Organization.SELECT_ORGANIZATION_PHONE_NUMBER);
            selectStmts.addElement(Organization.SELECT_WEB_SITE);
            StringList selectRelStmts = new StringList(2);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
            selectRelStmts.addElement(SELECT_SHARE_TYPES);
            // getting the collaboration partner list
            collaborationPartnerList = org.getCollaborationPartners(context,selectStmts,selectRelStmts,null,null);
        }
        catch (FrameworkException Ex) {
            throw Ex;
        }
        return collaborationPartnerList;
    }


    /**
     * This method gets the internationalized value of "share types" attribute for each element in the MapList.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments as it is getting the params list,objects list from the Config UI table
     * @returns Vector of "share type" values for each row
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     */
    public Vector getShareTypes(Context context, String[] args)
        throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        // getting the HashMap for the key "paramList" and then retreiving the language string
        // from the HashMap
        String languageStr  = (String)((HashMap)programMap.get("paramList")).get("languageStr");
        // getting the MapList of the objects.
        MapList objList     = (MapList)programMap.get("objectList");
        Vector columnVals   = new Vector(objList.size());
        Iterator iterator   = objList.iterator();
        String internationalizedShareType = "";
        // iterating to get the Share Type attribute object
        while (iterator.hasNext())
        {
            Map map = (Map) iterator.next();
            // reinitialize the internationalized share type string to blank
            internationalizedShareType = "";
            // getting the internationalized types by passing the symbolic types.
            StringList I18NSymbolicTypes = i18nNow.getI18NForSymbolicTypes((String)map.get(SELECT_SHARE_TYPES),languageStr);
            for(int index=0;index<I18NSymbolicTypes.size();index++)
            {
                // added the if loop so that "," will not be added after the last element gets added.
                if(index==I18NSymbolicTypes.size()-1)
                {
                    internationalizedShareType += (String)I18NSymbolicTypes.get(index);
                }
                else
                {
                    internationalizedShareType += (String)I18NSymbolicTypes.get(index)+",";
                }
            }
            // putting the comma separeted string in the vector.
            columnVals.addElement(internationalizedShareType);
        }
        return columnVals;
    }

    /**
    * This method gets the href link of "web site" attribute for each element in the MapList.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments as it is getting the params list,objects list from the Config UI table
    * @returns Vector of "web site" values for each row
    * @throws Exception if the operation fails
    * @since Common 10-0-0-0
    */
    public Vector getWebsite(Context context, String[] args)
            throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        // getting the MapList of the objects.
        MapList objList = (MapList)programMap.get("objectList");
        String sLanguage = (String)((HashMap)programMap.get("paramList")).get("languageStr");

        Map paramList = (Map)programMap.get("paramList");

        boolean isPrinterFriendly = false;
        String printerFriendly = (String)paramList.get("reportFormat");

        if ( printerFriendly != null )
        {
            isPrinterFriendly = true;
        }

        Vector columnVals = new Vector(objList.size());
        Iterator iterator = objList.iterator();
        String columnValue = "";
        String website = "";
        String strTempWebSite = "";
        // iterating to get the Share Type attribute object
        while (iterator.hasNext())
        {
            Map map = (Map) iterator.next();
            website = (String)map.get(Organization.SELECT_WEB_SITE);
            website=XSSUtil.encodeForHTMLAttribute(context, website);
            strTempWebSite = website.toLowerCase();
            

            // fix for bug #295605 (internationalization issue)
            i18nNow loc = new i18nNow();


        if(!isPrinterFriendly)
        {
            // add the web site value along with the href tag to show the link on the website

            // fix for bug #295605 (internationalization issue)
            if ( website.trim().length()==0 || website.equals("unknown") ) {
                website=loc.GetString("emxFrameworkStringResource",sLanguage,"emxFramework.Default.Web_Site");
                columnValue=website;
            }
            else if(strTempWebSite.startsWith("http://") || strTempWebSite.startsWith("https://"))
            {
                columnValue = "<a target=\"_blank\" href=\""+website+"\">"+ website + "</a>";
            }
            else
            {
                columnValue = "<a target=\"_blank\" href=\"http://"+website+"\">"+ website + "</a>";
            }
            }
            else
            {
                // fix for bug #295605 (internationalization issue)
                if ( website.trim().length()==0 || website.equals("unknown") ) {
                    website=loc.GetString("emxFrameworkStringResource",sLanguage,"emxFramework.Default.Web_Site");
                    columnValue=website;
                }
                columnValue = website ;
            }

            columnVals.addElement(columnValue);

        }
        return columnVals;
    }

    /**
    * This method returns true if the context user has the given RDO role.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds one argument, the symbolic role name
    * @returns true if context user has the given RDO role, otherwise false
    * @throws Exception if the operation fails
    * @since Common 10-0-0-0
    */
    public String hasRDOrole(Context context, String[] args)
            throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        String role = args[0];
        String returnValue = "false";
        boolean flag = false;
        try
        {
            // find RDO for this object
            String RDOid = getInfo(context, SELECT_RDO_ID);

            // first see if the object is wired to an RDO
            if (RDOid == null || RDOid.equals(""))
            {
                // no RDO, return true
                returnValue = "true";
            }
            else
            {
                // search for RDO entry in cache
                StringList roles = null;
                StringList tempRoles = null;
                Map RDOmap = (Map)PersonUtil.getPersonProperty(context, "RDO");
                if (RDOmap != null)
                {
                    roles = (StringList)RDOmap.get(RDOid);
                }
                else
                {
                    RDOmap = new HashMap(1);
                }

                if (roles == null)
                {
                    //get the Project Role attribute value from the Member rel
                    StringList relSelects = new StringList(1);
                    relSelects.add("attribute["+ATTRIBUTE_PROJECT_ROLE+"]");

                    // get the rel to the specific RDO only
                    String objectWhere = "id == \"" + RDOid + "\"";

                    Person person = Person.getPerson(context);

                    //navigate from person to RDO via the member rel;
                    MapList memberMapList = person.getRelatedObjects(
                                            context,            // context
                                            RELATIONSHIP_MEMBER,// relationship pattern
                                            QUERY_WILDCARD,     // object pattern
                                            null,               // object selects
                                            relSelects,         // relationship selects
                                            true,               // to direction
                                            false,              // from direction
                                            (short) 1,          // recursion level
                                            objectWhere,        // object where clause
                                            null);              // relationship where clause

                    // not found in cache, must add new entry
                    if (memberMapList == null || memberMapList.size() < 1)
                    {
                        // person not member of RDO,
                        // place empty string in RDOmap so search is avoided next time
                        roles = new StringList(1);
                        roles.addElement("");
                    }
                    else
                    {
                        // get roles in the Project Role attribute on the context user's Member connection
                        Map personMap = (Map) memberMapList.get(0);
                        String projectRole = (String) personMap.get("attribute["+ATTRIBUTE_PROJECT_ROLE+"]");

                        if (projectRole != null)
                        {
                            tempRoles = FrameworkUtil.split(projectRole, "~");
                            roles = new StringList(1);
                        }
                        //now need to get the parent roles for the given roles and add those to the list
                        String tempRole = "";
                        String tempCmd = "";
                        String results = "";
                        String parentAlias = "";

                        for (int i=0; i < tempRoles.size(); i++)
                        {
                            tempRole = tempRoles.elementAt(i).toString();

                            if(!UIUtil.isNullOrEmpty(tempRole))
                            {
                                roles.add(tempRole);
                            }else
                            {
                                flag = true;
                            }
                        }


                        for (int i=0; i < roles.size(); i++)
                        {
                            tempRole = roles.elementAt(i).toString();
                            tempRole = PropertyUtil.getSchemaProperty(context,tempRole);

                            tempCmd = "print role $1 select $2 dump $3";
                            results = MqlUtil.mqlCommand(context, tempCmd, tempRole, "parent", "|");
                            StringTokenizer tokens = new StringTokenizer(results,"|");
                            while(tokens.hasMoreTokens())
                            {
                                parentAlias = FrameworkUtil.getAliasForAdmin(context, "role", (String)tokens.nextToken(), true);
                                if (!roles.contains(parentAlias))
                                {
                                    roles.add(parentAlias);
                                }
                            }
                        }
                    }
                    //update RDO cache
                    RDOmap.put(RDOid, roles);
                    PersonUtil.setPersonProperty(context, "RDO", RDOmap);
                }
                // test if given role is in the Project Role attribute
                if (roles != null)
                {
                    if (roles.contains(role))
                    {
                        // role found, return true
                        returnValue = "true";
                    }
                }
            }

            // return value through global RPE
            String command = "set env $1 $2 $3";
            String results = MqlUtil.mqlCommand(context, command, "global", "emxOrganization", returnValue);
        }
        catch (FrameworkException Ex)
        {
            throw Ex;
        }

        finally
        {
            if(flag==true)
            {
                emxContextUtil_mxJPO.mqlNotice(context,
                		EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Common.UnregisteredRoles"));
            }
        }
        return returnValue;
    }


    /**
     * This method decide whether to enable the checkbox for Removing the Collaboration Partners from the Summary Page.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments as it is getting the params list,objects list from the Config UI table
     * @returns Vector of "true/false" values for each row
     * @throws Exception if the operation fails
     * @since Common 10.5
     */
    public Vector showCheckBoxInCollaborationPartners(Context context, String[] args)
    throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        // getting the MapList of the objects.
        MapList objList     = (MapList)programMap.get("objectList");
        // getting the HashMap for the key "paramList"
        HashMap paramMap    = (HashMap)programMap.get("paramList");
        // getting the objectId from the Map
        String objectId     = (String) paramMap.get("objectId");

        int listSize = objList.size();
        Vector columnVals   = new Vector(listSize);

        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) com.matrixone.apps.common.Person.getPerson(context);
        BusinessObject busObjOrg = Company.getCompanyForRep(context,person);
        if(busObjOrg != null)
        {
             busObjOrg.open(context);
            if(objectId.equals(busObjOrg.getObjectId()))
            {
                for(int i = listSize ; i > 0 ; i--)
                {
                    columnVals.add("true");
                }
            }
            else
            {
                for(int i = listSize ; i > 0 ; i--)
                {
                    columnVals.add("false");
                }
            }
            busObjOrg.close(context);
        }
        else
       {
            for(int i = listSize ; i > 0 ; i--)
            {
                columnVals.add("false");
            }
       }

        return columnVals;
    }

       /**
       * This method gets the href link of "web site" attribute for each
       * element in the MapList.
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments as it is getting the params list,
       *        objects list from the Config UI table
       * @returns Vector of "web site" values for each row
       * @throws Exception if the operation fails
       * @since Common 10-0-0-0
       */
           public Vector getWebSiteUsingSelect(Context context, String[] args)
            throws Exception
    {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            // getting the MapList of the objects.
            MapList objList = (MapList)programMap.get("objectList");

            Map paramList = (Map)programMap.get("paramList");

            boolean isPrinterFriendly = false;
            String printerFriendly = (String)paramList.get("reportFormat");

            if (printerFriendly != null )
            {
                isPrinterFriendly = true;
            }

            Vector columnVals = new Vector(objList.size());
            BusinessObjectWithSelectList busObjwsl = null;

            StringList strList = new StringList(1);
            strList.addElement(Organization.SELECT_WEB_SITE);

            if ( objList != null)
            {
              String objIdArray[] = new String[objList.size()];

              for (int i = 0; i < objList.size(); i++)
              {
                 Map objMap = (Map)objList.get(i);
                 objIdArray[i]  = (String)objMap.get("id");
              }

              busObjwsl = BusinessObject.getSelectBusinessObjectData(context,
                                                                     objIdArray,
                                                                     strList);

              String strTempWebSite = "";
              String website = "";
              for (int i = 0; i < objList.size(); i++)
              {
                 StringBuffer columnValue = new StringBuffer(128);
                 website = (String)busObjwsl.getElement(i).getSelectData(Organization.SELECT_WEB_SITE);
                 strTempWebSite = website.toLowerCase();

                 if(!isPrinterFriendly)
                 {
                 // add the web site value along with the href tag to
                 // show the link to the website
                 if ((strTempWebSite.startsWith("http://")) ||
                     (strTempWebSite.startsWith("https://")))
                 {
                    columnValue.append("<a target=\"_blank\" href=\"");
                    columnValue.append(XSSUtil.encodeForHTMLAttribute(context,website));
                    columnValue.append("\">");
                    columnValue.append(XSSUtil.encodeForHTML(context,website));
                    columnValue.append("</a>");
                 }
                 else
                 {
                    columnValue.append("<a target=\"_blank\" href=\"http://");
                    columnValue.append(XSSUtil.encodeForHTMLAttribute(context,website));
                    columnValue.append("\">");
                    columnValue.append(XSSUtil.encodeForHTML(context,website));
                    columnValue.append("</a>");
                 }
                 }
                 else
                 {
                    columnValue.append(website);
                 }
                 columnVals.addElement(columnValue.toString());
              }
            }

            return columnVals;
    }

    /**
     * This method return true if user have permissions on the command
     * otherwise return false.
     *
     * If the person is a Host Company Representative OR Buyer Admin OR Company Representative of the
     * Selected Company then this method will return true.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId and param values.
     * @return Boolean true if FCS is enabled and property set to true othewise return false.
     * @throws Exception If the operation fails.
     * @since EC 10-5.
     */
     public Boolean hasAccessForCompanyNodes(Context context, String[] args)
         throws Exception
    {
        boolean isHostRep = Company.isHostRep(context,com.matrixone.apps.common.Person.getPerson(context));
        if(isHostRep)
        {
            return Boolean.valueOf(true);
        }

        String adminRoleList = "role_BuyerAdministrator,role_Buyer,role_VPLMAdmin";

        com.matrixone.apps.common.Person loginPerson = com.matrixone.apps.common.Person.getPerson(context);

        StringList adminRolesList   = FrameworkUtil.split(adminRoleList,",");
        Iterator adminRolesItr      = adminRolesList.iterator();

        String role = "";
        String roleName = "";
        while (adminRolesItr.hasNext())
        {
            role = (String) adminRolesItr.next();
            roleName = PropertyUtil.getSchemaProperty(context,role);

            if(loginPerson.hasRole(context,roleName))
            {
                return Boolean.valueOf(true);
            }
        }

        String strCompRepId = loginPerson.getInfo(context,"to["+RELATIONSHIP_COMPANY_REPRESENTATIVE+"].from.id");

        if(strCompRepId == null || "".equals(strCompRepId))
        {
            return Boolean.valueOf(false);
        }
        String strCompRepType = loginPerson.getInfo(context,"to["+RELATIONSHIP_COMPANY_REPRESENTATIVE+"].from.type");

        String strTypeToCompare = "";
        String strEmpRel = "";
        if(TYPE_COMPANY.equals(strCompRepType))
        {
            strTypeToCompare = TYPE_COMPANY;
            strEmpRel = RELATIONSHIP_EMPLOYEE;
        }
        else if(TYPE_BUSINESS_UNIT.equals(strCompRepType))
        {
            strTypeToCompare = TYPE_BUSINESS_UNIT;
            strEmpRel = RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE;
        }

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");
        if(objectId == null || "".equals(objectId))
        {
            objectId = loginPerson.getCompanyId(context);
        }

        String strOrganizationId = objectId;
        DomainObject busOrganization = new DomainObject(strOrganizationId);
        String sTypeName = busOrganization.getInfo(context,DomainObject.SELECT_TYPE);

        while(!strTypeToCompare.equals(sTypeName) && !"".equals(sTypeName))
        {
            if(TYPE_BUSINESS_UNIT.equals(sTypeName))
            {
                sTypeName = busOrganization.getInfo(context,"to[" + RELATIONSHIP_DIVISION + "].from.type");
                strOrganizationId = busOrganization.getInfo(context,"to[" + RELATIONSHIP_DIVISION + "].from.id");
                busOrganization = new DomainObject(strOrganizationId);
            }
            else if (TYPE_DEPARTMENT.equals(sTypeName))
            {
                sTypeName = busOrganization.getInfo(context,"to[" + RELATIONSHIP_COMPANY_DEPARTMENT + "].from.type");
                strOrganizationId = busOrganization.getInfo(context,"to[" + RELATIONSHIP_COMPANY_DEPARTMENT + "].from.id");
                busOrganization = new DomainObject(strOrganizationId);
            }
            else if(TYPE_PERSON.equals(sTypeName))
            {
                sTypeName = busOrganization.getInfo(context,"to[" + strEmpRel + "].from.type");
                strOrganizationId = busOrganization.getInfo(context,"to[" + strEmpRel + "].from.id");
                busOrganization = new DomainObject(strOrganizationId);
            }
            else if (TYPE_REGION.equals(sTypeName))
            {
                sTypeName = busOrganization.getInfo(context,"to[" + RELATIONSHIP_ORGANIZATION_REGION + "].from.type");
                strOrganizationId = busOrganization.getInfo(context,"to[" + RELATIONSHIP_ORGANIZATION_REGION + "].from.id");
                busOrganization = new DomainObject(strOrganizationId);
            }
            else
            {
                sTypeName = "";
            }
        }

        if(strCompRepId.equals(strOrganizationId))
        {
            return Boolean.valueOf (true);
        }

        if(TYPE_COMPANY.equals(strTypeToCompare))
        {
            while(TYPE_COMPANY.equals(sTypeName))
            {
                sTypeName = busOrganization.getInfo(context,"to[" + RELATIONSHIP_SUBSIDIARY + "].from.type");
                strOrganizationId = busOrganization.getInfo(context,"to[" + RELATIONSHIP_SUBSIDIARY + "].from.id");
                if(strCompRepId.equals(strOrganizationId))
                {
                    return Boolean.valueOf (true);
                }
                busOrganization = new DomainObject(strOrganizationId);
            }
        }
        else if(TYPE_BUSINESS_UNIT.equals(strTypeToCompare))
        {
            while(TYPE_BUSINESS_UNIT.equals(sTypeName))
            {
                sTypeName = busOrganization.getInfo(context,"to[" + RELATIONSHIP_DIVISION + "].from.type");
                strOrganizationId = busOrganization.getInfo(context,"to[" + RELATIONSHIP_DIVISION + "].from.id");
                if(strCompRepId.equals(strOrganizationId))
                {
                    return Boolean.valueOf (true);
                }
                busOrganization = new DomainObject(strOrganizationId);
            }
        }

        return Boolean.valueOf(false);

    }

    /**
     * This method adds / remove the roles defined in arguments to the person if "Organization Representative"
     * role is connected / disconnect respectively
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId and param values.
     * @throws Exception If the operation fails.
     * @since SC 10-5.
     */
     public void syncRolesOnOrgRepRelationship(Context context, String[] args)
         throws Exception
    {
        if (args.length == 0 )
        {
            throw new IllegalArgumentException();
        }

        String strPersonId          = args[0];
        String strSymbolicRoles     = args[1];
        String strEvent             = args[2];

        String strHostCompanyId = Company.getHostCompany(context);

        com.matrixone.apps.common.Person person = new com.matrixone.apps.common.Person(strPersonId);

        String strSelect = "to["+DomainObject.RELATIONSHIP_EMPLOYEE+"].from.id";
        String strCompanyId = person.getInfo(context,strSelect);

// Commented for IR-023990V6R2011 Dated 04/12/2009 Begins.
/*        if(strHostCompanyId.equals(strCompanyId))
        {*/
// Commented for IR-023990V6R2011 Dated 04/12/2009 Ends.

        	String strPersonName        = person.getInfo(context,person.SELECT_NAME);
            StringList rolesList        = FrameworkUtil.split(strSymbolicRoles,",");
            Iterator rolesListItr       = rolesList.iterator();

            String strAction = "add";

            if(!"Create".equalsIgnoreCase(strEvent))
            {
                strAction = "remove";
            }

            String role = "";
            String roleName = "";
            String strCommand = "";
            while (rolesListItr.hasNext())
            {
                role = (String) rolesListItr.next();
                roleName = PropertyUtil.getSchemaProperty(context,role);
                strCommand = "modify role $1 $2 assign person $3";
                MqlUtil.mqlCommand(context,strCommand,true, roleName, strAction, strPersonName);
            }

// Commented for IR-023990V6R2011 Dated 04/12/2009 Begins.
//        }
// Commented for IR-023990V6R2011 Dated 04/12/2009 Ends.

}

    /**
    * This method gets all the locations associated with the Organization
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllLocations(Context context, String[] args) throws Exception
    {
        MapList listOfLocations = new MapList();

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");

        String strRelLocation           = DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION;
        String strTypeLocation          = DomainConstants.TYPE_LOCATION;

        SelectList busSelects = new SelectList(1);
        busSelects.add(DomainConstants.SELECT_ID);

        SelectList relSelects = new SelectList(1);
        relSelects.add( DomainConstants.SELECT_RELATIONSHIP_ID );

        Organization orgObj = new Organization(objectId);
        try
        {
        ContextUtil.startTransaction(context,false);
        ExpansionIterator expIter     = orgObj.getExpansionIterator(context,
                                                                strRelLocation,
                                                                strTypeLocation,
                                                                busSelects,
                                                                relSelects,
                                                                false,
                                                                true,
                                                                (short)1,
                                                                null,
                                                                null,
                                                                (short)0,
                                                                false,
                                                                false,
                                                                (short)100,
                                                                false);
        listOfLocations = FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
        expIter.close();
        ContextUtil.commitTransaction(context);
        }
        catch(Exception ex)
        {
            ContextUtil.abortTransaction(context);
            throw new Exception(ex.toString());
        }
        return listOfLocations;

    }


    /**
    * This method displays the location types of the locations
    * associated with the Organization
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    public Vector getLocationsTypes(Context context, String[] args) throws Exception
    {
        Vector listOfLocationsTypes = new Vector();

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relLocationsList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String languageStr = (String)paramMap.get("languageStr");

        int size = 0;
        if(relLocationsList != null && (size = relLocationsList.size()) > 0)
        {
            String sRelAttrBillingAddress   = PropertyUtil.getSchemaProperty(context, "attribute_BillingAddress");
            String sRelAttrHeadquarters     = PropertyUtil.getSchemaProperty(context, "attribute_HeadquartersSite");
            String sRelAttrManufacturing    = PropertyUtil.getSchemaProperty(context, "attribute_ManufacturingSite");
            String sRelAttrShippingAddress  = PropertyUtil.getSchemaProperty(context, "attribute_ShippingAddress");

            String i18nsRelAttrBillingAddress   = i18nNow.getAttributeI18NString(sRelAttrBillingAddress,languageStr);
            String i18nsRelAttrHeadquarters     = i18nNow.getAttributeI18NString(sRelAttrHeadquarters,languageStr);
            String i18nsRelAttrManufacturing    = i18nNow.getAttributeI18NString(sRelAttrManufacturing,languageStr);
            String i18nsRelAttrShippingAddress  = i18nNow.getAttributeI18NString(sRelAttrShippingAddress,languageStr);

            StringBuffer strLocTypeValue = null;

            DomainRelationship domRel = null;
            Map tempMap = null;
            Map attrMap = null;
            for(int i = 0 ; i < size ; i++)
            {
                strLocTypeValue = new StringBuffer();
                tempMap = (Map)relLocationsList.get(i);
                domRel = new DomainRelationship((String)tempMap.get(SELECT_RELATIONSHIP_ID));
                attrMap = domRel.getAttributeMap(context);

                if("Yes".equals(attrMap.get(sRelAttrBillingAddress)))
                {
                    strLocTypeValue.append(XSSUtil.encodeForHTML(context,i18nsRelAttrBillingAddress));
                }
                if("Yes".equals(attrMap.get(sRelAttrHeadquarters)))
                {
                    if(strLocTypeValue.length() > 0)
                    {
                        strLocTypeValue.append("<br/>");
                    }
                    strLocTypeValue.append(XSSUtil.encodeForHTML(context,i18nsRelAttrHeadquarters));
                }
                if("Yes".equals(attrMap.get(sRelAttrManufacturing)))
                {
                    if(strLocTypeValue.length() > 0)
                    {
                        strLocTypeValue.append("<br/>");
                    }
                    strLocTypeValue.append(XSSUtil.encodeForHTML(context,i18nsRelAttrManufacturing));
                }
                if("Yes".equals(attrMap.get(sRelAttrShippingAddress)))
                {
                    if(strLocTypeValue.length() > 0)
                    {
                        strLocTypeValue.append("<br/>");
                    }
                    strLocTypeValue.append(XSSUtil.encodeForHTML(context,i18nsRelAttrShippingAddress));
                }
                strLocTypeValue.append(" ");
                listOfLocationsTypes.add(strLocTypeValue.toString());
            }
        }

        return listOfLocationsTypes;

    }


    /**
    * This method gets the existing locations associated with the Organization
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getExistingLocations(Context context, String[] args) throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");

        MapList listOfLocations = new MapList();

        String strCompId        = objectId;
        DomainObject boCompany  = new DomainObject(strCompId);

        String strType          = boCompany.getInfo(context,DomainConstants.SELECT_TYPE);
        if(DomainObject.TYPE_BUSINESS_UNIT.equals(strType))
        {
            while(!DomainObject.TYPE_COMPANY.equals(strType) && !"".equals(strType))
            {
                if(DomainObject.TYPE_BUSINESS_UNIT.equals(strType))
                {
                    strType     = boCompany.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DIVISION + "].from.type");
                    strCompId   = boCompany.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DIVISION + "].from.id");
                    boCompany   = new DomainObject(strCompId);
                }
            }
        }

        String strRelLocation           = DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION;

        StringList strCoLocations       = boCompany.getInfoList(context, "from["+strRelLocation+"].to.id");
        StringList strLocationRelIds    = boCompany.getInfoList(context, "from["+strRelLocation+"].id");

        DomainObject boBusinessUnit     = new DomainObject(objectId);
        StringList strBuLocations       = boBusinessUnit.getInfoList(context, "from["+strRelLocation+"].to.id");

        int size = 0;
        if(strCoLocations != null && (size = strCoLocations.size() ) > 0 )
        {
            Map temp = null;
            String strLocIds = "";
            String strRelId = "";
            for( int i = 0; i < size; i++ )
            {
                strLocIds = (String)strCoLocations.get(i);
                strRelId = (String)strLocationRelIds.get(i);
                if(strBuLocations == null || !strBuLocations.contains(strLocIds) )
                {
                    temp = new HashMap();
                    temp.put(SELECT_ID,strLocIds);
                    temp.put(SELECT_RELATIONSHIP_ID,strRelId);
                    listOfLocations.add(temp);
                }
            }
        }
        return listOfLocations;
    }

    /**
    * This method gets all the Formats associated with the Organization
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllFormats(Context context, String[] args) throws Exception
    {
        MapList listOfFormats = new MapList();

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");

        String strRelSupportedFileFormats   = DomainConstants.RELATIONSHIP_SUPPORTED_FILE_FORMAT;
        String strTypeFileFormats           = DomainConstants.TYPE_FILE_FORMAT;
        //String sRelAttrBillingAddress     = PropertyUtil.getSchemaProperty(context, "attribute_BillingAddress");

        SelectList busSelects = new SelectList(2);
        busSelects.add(DomainConstants.SELECT_ID);
        busSelects.add(DomainConstants.SELECT_NAME);

        SelectList relSelects = new SelectList(1);
        relSelects.add( DomainConstants.SELECT_RELATIONSHIP_ID );

        Organization orgObj = new Organization(objectId);
        try
        {
        ContextUtil.startTransaction(context,false);
        ExpansionIterator expIter     = orgObj.getExpansionIterator(context,
                                                            strRelSupportedFileFormats,
                                                            strTypeFileFormats,
                                                            busSelects,
                                                            relSelects,
                                                            false,
                                                            true,
                                                            (short)1,
                                                            null,
                                                            null,
                                                            (short)0,
                                                            false,
                                                            false,
                                                            (short)100,
                                                            false);
        listOfFormats = FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
        expIter.close();
        ContextUtil.commitTransaction(context);
        }
        catch(Exception ex)
        {
            ContextUtil.abortTransaction(context);
            throw new Exception(ex.toString());
        }
        return listOfFormats;

    }

    /**
    * This method gets all the Formats Names associated with the Organization
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    public Vector getFormatNames(Context context, String[] args) throws Exception
    {
        Vector listOfFormatNames = new Vector();

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList formatList = (MapList)programMap.get("objectList");

        int size = 0;
        if(formatList != null && (size = formatList.size()) > 0)
        {
            Map tempMap = null;
            String strName = "";
            StringTokenizer strTokenizer = null;
            String strFormatName = "";

            for(int i = 0 ; i < size ; i++ )
            {
                tempMap = (Map)formatList.get(i);
                strName = (String)tempMap.get(DomainConstants.SELECT_NAME);
                strTokenizer = new StringTokenizer(strName,":",false);
                strFormatName = strTokenizer.nextToken();

                listOfFormatNames.add(strFormatName);
            }
        }

        return listOfFormatNames;

    }

    /**
    * This method gets all the Formats Versions associated with the Organization
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    public Vector getFormatVersions(Context context, String[] args) throws Exception
    {
        Vector listOfFormatVersions = new Vector();

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList formatList = (MapList)programMap.get("objectList");

        int size = 0;
        if(formatList != null && (size = formatList.size()) > 0)
        {
            Map tempMap = null;
            String strName = "";
            StringTokenizer strTokenizer = null;
            String strFormatName = "";
            String strVersion = "";

            for(int i = 0 ; i < size ; i++ )
            {
                tempMap = (Map)formatList.get(i);
                strName = (String)tempMap.get(DomainConstants.SELECT_NAME);
                strTokenizer = new StringTokenizer(strName,":",false);
                strFormatName = strTokenizer.nextToken();
                strVersion = " ";

                while(strTokenizer.hasMoreTokens())
                {
                    strVersion = strTokenizer.nextToken();
                }

                listOfFormatVersions.add(strVersion);
            }
        }

        return listOfFormatVersions;

    }


    /**
    * This method gets all the Collaboration Requests of the Org, which the logged in person represents
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCollaborationRequests(Context context, String[] args) throws Exception
    {
        MapList collaborationRequestsList = new MapList();
        com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) com.matrixone.apps.common.Person.getPerson(context);

        StringList strListOrgRep = person.getInfoList(context,"to["+DomainConstants.RELATIONSHIP_COMPANY_REPRESENTATIVE +"].from.id");
        int size = 0;
        if(strListOrgRep != null && (size = strListOrgRep.size()) > 0)
        {
            String strRepOrgId = (String)strListOrgRep.get(0);
            DomainObject domRepOrg = new DomainObject(strRepOrgId);

            String strRepOrgName    = domRepOrg.getInfo(context,DomainConstants.SELECT_NAME);
            String strRepOrgType    = domRepOrg.getInfo(context,DomainConstants.SELECT_TYPE);
            StringList strBusSelects = new StringList(2);
            strBusSelects.add(DomainConstants.SELECT_ID);
            strBusSelects.add(DomainConstants.SELECT_TYPE);
            strBusSelects.add(DomainConstants.SELECT_NAME);

            StringList strRelSelects = new StringList(1);
            strRelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

            Map dataMap = null;
            Iterator listItr = null;

            MapList requestsFromList = domRepOrg.getRelatedObjects(context
                                                                ,Company.RELATIONSHIP_COLLABORATION_REQUEST
                                                                ,"*"
                                                                ,strBusSelects
                                                                ,strRelSelects
                                                                ,true
                                                                ,false
                                                                ,(short)1
                                                                ,null
                                                                ,null
                                                                );
            listItr = requestsFromList.iterator();
            while (listItr.hasNext())
            {
                dataMap = (Map)listItr.next();
                dataMap.put(DomainConstants.SELECT_ID,dataMap.get(DomainConstants.SELECT_ID));
                dataMap.put("SELECT_TO_ID",strRepOrgId);
                dataMap.put("SELECT_TO_TYPE",strRepOrgType);
                dataMap.put("SELECT_TO_NAME",strRepOrgName);
                dataMap.put("ENABLE","1");
            }
            if(requestsFromList != null)
            {
                collaborationRequestsList.addAll(requestsFromList);
            }
            MapList requestsToList = domRepOrg.getRelatedObjects(context
                                                                ,Company.RELATIONSHIP_COLLABORATION_REQUEST
                                                                ,"*"
                                                                ,strBusSelects
                                                                ,strRelSelects
                                                                ,false
                                                                ,true
                                                                ,(short)1
                                                                ,null
                                                                ,null
                                                                );
            listItr = requestsToList.iterator();
            while (listItr.hasNext())
            {
                dataMap = (Map)listItr.next();
                dataMap.put("SELECT_TO_ID",dataMap.get(DomainConstants.SELECT_ID));
                dataMap.put("SELECT_TO_TYPE",dataMap.get(DomainConstants.SELECT_TYPE));
                dataMap.put("SELECT_TO_NAME",dataMap.get(DomainConstants.SELECT_NAME));
                dataMap.put(DomainConstants.SELECT_ID,strRepOrgId);
                dataMap.put("ENABLE","0");
            }
            if(requestsToList != null)
            {
                collaborationRequestsList.addAll(requestsToList);
            }
        }
        return collaborationRequestsList;
    }

    /**
    * Decides whether to enable or disable the checkbox
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *
    * @return Vector containing..true or false
    * @throws Exception if the operation fails
    * @since 10.5-SP1
    */
    public Vector getCheckBoxForCollaborationRequestsSummary(Context context,String args[]) throws Exception
    {
        Vector columnVals   = null;
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        // getting the MapList of the objects.
        MapList objList     = (MapList)programMap.get("objectList");

        int listSize = 0;
        Map map = null;
        if(objList != null && (listSize = objList.size()) > 0 )
        {
            columnVals   = new Vector(listSize);
            String strEnable = "";
            for(int i = 0; i < listSize ; i++)
            {
                map = (Map)objList.get(i);
                strEnable = (String)map.get("ENABLE");
                if("0".equals(strEnable))
                {
                    columnVals.add("false");
                }
                else
                {
                    columnVals.add("true");
                }
            }
        }
        return columnVals;
    }

    /**
    * Displays the To Organization in Collaboration Requests Page
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *
    * @return Vector containing..true or false
    * @throws Exception if the operation fails
    * @since 10.5-SP1
    */
    public Vector getToOrgForCollaborationRequestsSummary(Context context,String args[]) throws Exception
    {
        Vector columnVals   = null;
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);

        // getting the MapList of the objects.
        MapList objList     = (MapList)programMap.get("objectList");

        int listSize = 0;
        Map map = null;
        if(objList != null && (listSize = objList.size()) > 0 )
        {
            Map paramList = (Map)programMap.get("paramList");
            boolean isPrinterFriendly = false;
            String PrinterFriendly = (String)paramList.get("reportFormat");
            if ( PrinterFriendly != null )
            {
                isPrinterFriendly = true;
            }
            boolean isExport = false;
            String Export = (String)paramList.get("exportFormat");
            if ( Export != null )
            {
                isExport = true;
            }

            columnVals   = new Vector(listSize);
            String strToOrgId = "";
            String strToOrgType = "";
            String strToOrgName = "";

            String strCompanyImage = "<img src=\"../common/images/iconSmallCompany.gif\" border=\"0\" align=\"absmiddle\">";
            String strBusinessUnitImage = "<img src=\"../common/images/iconSmallBusinessUnit.gif\" border=\"0\" align=\"absmiddle\">";

            StringBuffer strImgUrl = null;
            StringBuffer strNameUrl = null;

            for(int i = 0; i < listSize ; i++)
            {
                map = (Map)objList.get(i);
                strToOrgId = (String)map.get("SELECT_TO_ID");
                strToOrgName = (String)map.get("SELECT_TO_NAME");
                strToOrgType = (String)map.get("SELECT_TO_TYPE");

                if(!isExport)
                {
                    strNameUrl = new StringBuffer();
                    strImgUrl = new StringBuffer();

                    if(!isPrinterFriendly)
                    {
                        strImgUrl.append("<a href=");
                        strImgUrl.append("\"");
                        strImgUrl.append("../common/emxTree.jsp?AppendParameters=true&objectId=");
                        strImgUrl.append(strToOrgId);
                        strImgUrl.append("\"");
                        strImgUrl.append(" target=\"content\" class=\"object\">");

                        strNameUrl.append(strImgUrl.toString());
                    }

                    if(DomainConstants.TYPE_COMPANY.equals(strToOrgType))
                    {
                        strImgUrl.append(strCompanyImage);
                    }
                    else if(DomainConstants.TYPE_BUSINESS_UNIT.equals(strToOrgType))
                    {
                        strImgUrl.append(strBusinessUnitImage);
                    }

                    strNameUrl.append(XSSUtil.encodeForHTML(context,strToOrgName));

                    if(!isPrinterFriendly)
                    {
                        strImgUrl.append("</a>");
                        strNameUrl.append("</a>");
                    }

                    columnVals.add(strImgUrl.toString() + "&nbsp;" + strNameUrl.toString());
                }
                else
                {
                    columnVals.add(strToOrgName);
                }
            }

        }
        return columnVals;
    }

        /**
        * Gets the Capabilities for the Company/Business Unit.
        *
        * @param context The Matrix Context.
        * @param args holds objectId and param values.
        * @return maplist of Capabilites
        * @throws Exception If the operation fails.
        * @since Common 10.5
        */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getCapabilites (Context context,String[] args) throws Exception
       {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId = (String) paramMap.get("objectId");
            MapList mapListCapObj = new MapList();

            String sAttrCapabilityStatus       = PropertyUtil.getSchemaProperty(context,"attribute_CapabilityStatus");
            String sAttrQualificationStatus    = PropertyUtil.getSchemaProperty(context,"attribute_ProcessQualificationStatus");
            String sRelCapability              = PropertyUtil.getSchemaProperty(context,"relationship_Capability");
            String sProcess                    = PropertyUtil.getSchemaProperty(context,"type_Process");

            // Get the id of the Organization from the request construct the Organization Business Object.
            DomainObject boOrganization = new DomainObject(objectId);

            StringList selectRelStatms = new StringList(1);
            selectRelStatms.addElement(DomainObject.SELECT_RELATIONSHIP_ID);

            StringList selectBusStatms = new StringList(1);
            selectBusStatms.addElement(DomainObject.SELECT_ID);
            try
            {
            ContextUtil.startTransaction(context,false);
            ExpansionIterator expIter  = boOrganization.getExpansionIterator(context,
                                                                            sRelCapability,
                                                                            sProcess,
                                                                            selectBusStatms,
                                                                            selectRelStatms,
                                                                            false,
                                                                            true,
                                                                            (short)1,
                                                                            null,
                                                                            null,
                                                                            (short)0,
                                                                            false,
                                                                            false,
                                                                            (short)100,
                                                                            false);
            mapListCapObj = FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
            expIter.close();
            ContextUtil.commitTransaction(context);
            }
            catch(Exception ex)
            {
                ContextUtil.abortTransaction(context);
                throw new Exception(ex.toString());
            }
            return mapListCapObj;
      }

    /**
    * This method returns access permissions of Command depending on the type.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */
     public boolean hasAccessForBUPeopleCreateNew(Context context,String[] args)
     throws Exception
     {

          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String objectId = (String) paramMap.get("objectId");

          try {

          DomainObject dob = new DomainObject(objectId);
          String typeof = dob.getType(context);

          if (typeof.equals(TYPE_COMPANY)||typeof.equals(TYPE_BUSINESS_UNIT)||typeof.equals("SpecificationOffice"))
          {
            return true;
          }else{
            return false;
          }

        }catch (FrameworkException Ex) {
               throw Ex;
        }
     }

     /**
    * This method returns access permissions of Command depending on the type.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */


     public boolean hasAccessForBUAddRemove(Context context,String[] args)
     throws Exception
     {

          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String objectId = (String) paramMap.get("objectId");

          try {

          DomainObject dob = new DomainObject(objectId);
          String typeof = dob.getType(context);

          if (typeof.equals(TYPE_REGION))
          {
            return true;
          }else{
            return false;
          }

        }catch (FrameworkException Ex) {
               throw Ex;
          }
      }

    /**
    * This method returns access permissions of Command depending on the type.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */

    public boolean hasAccessForBUDeleteSelected(Context context,String[] args)
        throws Exception
    {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId = (String) paramMap.get("objectId");

      try {

          DomainObject dob = new DomainObject(objectId);
          String typeof = dob.getType(context);

          if (typeof.equals(TYPE_COMPANY)||typeof.equals(TYPE_BUSINESS_UNIT))
          {
            return true;
          }else{
            return false;
          }

      }catch (FrameworkException Ex) {
           throw Ex;
      }
    }

    /**
    * This method returns access permissions of Command depending on the type.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */

    public boolean hasAccessForLocationRegionAddRemove(Context context,String[] args)
        throws Exception
    {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId = (String) paramMap.get("objectId");

      try {

          DomainObject dob = new DomainObject(objectId);
          String typeof = dob.getType(context);

          if (typeof.equals(TYPE_BUSINESS_UNIT))
          {
            return true;
          }else{
            return false;
          }

      }catch (FrameworkException Ex) {
           throw Ex;
      }
    }

    /**
    * This method returns access permissions of Command depending on the type of the Person logged in
    * Host Company representative only can modify/edit the Host Company Information.
  * This method restricts the Guest Company representative to modify the Host Company Information.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */

    public boolean hasAccessForPeopleAddExisting(Context context,String[] args)
        throws Exception
    {

        boolean isHostCompany = false;
        try{
              //Get the host company
              String hostCompanyStr = PropertyUtil.getSchemaProperty(context,"role_CompanyName");
              //Get the user company
              com.matrixone.apps.common.Person sPerson = new com.matrixone.apps.common.Person();
              sPerson.setToContext(context);
              com.matrixone.apps.common.Company myCompany = sPerson.getCompany(context);
              String myCompanyName = myCompany.getInfo(context,"name");

              if(hostCompanyStr.equals(myCompanyName))
              {
                  isHostCompany = true;
              }
        else
        {
          isHostCompany = false;
        }

        }
        catch(Exception e)
        {
           System.out.println("exception" + e.getMessage());
        }
        return isHostCompany;

    }

    /**
    * This method returns access permissions of Command depending on the type.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since SC 10-5.
    */

    public boolean hasAccessForRegionDeleteCreateNew(Context context,String[] args)
        throws Exception
    {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId = (String) paramMap.get("objectId");

      try {

          DomainObject dob = new DomainObject(objectId);
          String typeof = dob.getType(context);

          if (typeof.equals(TYPE_COMPANY)||typeof.equals(TYPE_REGION))
          {
            return true;
          }else{
            return false;
          }

       }catch (FrameworkException Ex) {
           throw Ex;
       }
    }

     /**
      * Checks if context organization associates with passed relationships as parent (from side)
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args hold the following input arguments:
      * args[0] - Relationship to expand from, mutltiple relationships can be entered
      *              as a string delimited with spaces(" "), "~" or ",".(Mandatory).
      *              Example: relationship_Customer,relationship_Supplier.
      *              Passing in one of the following will  expand on allRelationships. Symbolic name must be used.
      *  args[1] - emxComponents.RelatedObjectExist.Message(Mandatory)
      *               Default : emxComponents.RelatedObjectExist.Message
      *
      * @return  int 0, status code
      * @throws Exception if the operation fails
      * @since Common 11-0-0-0
      */
      public int checkOrganizationReferenced(Context context, String []args) throws Exception
      {
          String strOutput = "";
          int intOutput = 0;
          try
          {
              //pushing context
              ContextUtil.pushContext(context);

              // Create an instant of emxUtil JPO
              emxUtil_mxJPO utilityClass = new emxUtil_mxJPO(context, null);
              // Get Required Environment Variables
              String arguments[] = new String[2];
              arguments[0] = "get env TOOBJECTID";

              ArrayList cmdResults = utilityClass.executeMQLCommands(context, arguments);

              String sObjectId        = (String)cmdResults.get(0);
              StringBuffer sBuffer = new StringBuffer();
              String sRel = "";
              String sRelationship    = args[0];
              String sMessage = args[1];
              StringTokenizer strToken = new StringTokenizer(sRelationship, " ,~");
              String strRel = "";
              String strRelRealName = "";
              emxMailUtil_mxJPO mailUtil = new emxMailUtil_mxJPO(context, null);
              //if message string is not defined then give default message.
              if(sMessage == null && "null".equals(sMessage) && "".equals(sMessage))
              {
                 sMessage="emxComponents.RelatedObjectExist.Message";
              }

              if(sRelationship != null && !"null".equals(sRelationship) && !"".equals(sRelationship))
              {
                    StringList selectStmts = new StringList(5);
                    selectStmts.addElement(SELECT_NAME);
                    while(strToken.hasMoreTokens())
                    {
                        strRel = strToken.nextToken().trim();
                        strRelRealName = PropertyUtil.getSchemaProperty(context,strRel);
                        //if  wrong symbolic relationship name is entered then give alert to user
                        if("".equals(strRelRealName))
                        {
                             // Error out if not registered
                            arguments = new String[5];
                            arguments[0] = "emxFramework.ProgramObject.eServicecommonTrigcRelAttribute_if.InvalidRel";
                            arguments[1] = "1";
                            arguments[2] = "Name";
                            arguments[3] = strRel;
                            arguments[4] = "";
                            strOutput = mailUtil.getMessage(context,arguments);
                            intOutput = 1;
                            break;
                        }
                        else
                        {
                            if(sBuffer.length() > 0)
                            {
                                sBuffer.append(",");
                            }
                            sBuffer.append(strRelRealName);
                        }
                    }//end of while loop

                    if(sBuffer.length() > 0)
                    {
                        sRel = sBuffer.toString();
                        DomainObject domObj = new DomainObject(sObjectId);
                        MapList mapList = domObj.getRelatedObjects( context,
                                                                    strRelRealName,
                                                                    "*",     // object pattern
                                                                    selectStmts,         // object selects
                                                                    null,   // relationship selects
                                                                    false,                 // to direction
                                                                    true,                  // from direction
                                                                    (short) 1,             // recursion level
                                                                    null,           // object where clause
                                                                    null);    // relationship where clause
                        int size = 0;
                        if(mapList != null && (size = mapList.size()) > 0)
                        {
                           String langStr = context.getSession().getLanguage();
                           strOutput = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(langStr),sMessage);
                           intOutput = 1;
                        }
                    }//end of if
              }
              else
              {
                    // Error out if Relationship is blank
                    arguments = new String[5];
                    arguments[0] = "emxFramework.ProgramObject.eServicecommonTrigcRelAttribute_if.InvalidRel";
                    arguments[1] = "1";
                    arguments[2] = "Name";
                    arguments[3] = strRel;
                    arguments[4] = "";
                    strOutput = mailUtil.getMessage(context,arguments);
                    intOutput = 1;
              }
          }
          catch(Exception e)
          {
              intOutput = 1;
              strOutput = e.getMessage();
          }
          finally
          {
              if(intOutput != 0)
              {
                  emxContextUtil_mxJPO.mqlNotice(context,strOutput);
              }
              ContextUtil.popContext(context);
          }

          return intOutput;
      }//end of method

     /**
      * Checks whether selected supplier Organization is BU
      * If BU belongs to Host company, update the attribute_SupplierType of rel as internal
      * Otherwise External
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args hold no arguments
      *
      * @return  void
      * @throws Exception if the operation fails
      * @since Common 11-0-0-0
      */
      public void setSupplierType (Context context, String []args) throws Exception
      {
    String orgId = PropertyUtil.getRPEValue (context, "TOOBJECTID", false);
    String relId = PropertyUtil.getRPEValue (context, "RELID", false);

    String typeBusinessUnit = PropertyUtil.getSchemaProperty(context,
                            DomainSymbolicConstants.SYMBOLIC_type_BusinessUnit);
    String relSupplier = PropertyUtil.getSchemaProperty(context,
                            DomainSymbolicConstants.SYMBOLIC_relationship_Supplier);
    String attrSupplierType = "Supplier Type";

    // above value should be replaced with following, when SYMBOLIC_attribute_SupplierType available in
    // DomainSymbolicConstants.java
    // PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_attribute_SupplierType);

    DomainObject dom = new DomainObject (orgId);

    // if the connected supplier is Business Unit
    if (dom.isKindOf (context, typeBusinessUnit))
    {
      StringList companySelects = new StringList(1);
      companySelects.add(SELECT_ID);
      companySelects.add(SELECT_TYPE);

      MapList list = dom.getRelatedObjects(context,
                        RELATIONSHIP_DIVISION,
                        QUERY_WILDCARD,
                        companySelects,
                        EMPTY_STRINGLIST,
                        true,
                        false,
                        (short) 0,
                        EMPTY_STRING,
                        EMPTY_STRING,
                        new Pattern(TYPE_COMPANY),null,null);

      String compId = (list.size()>0)?((String) ((Map)list.get(0)).get (SELECT_ID)):"";
      String hostCompId = Company.getHostCompany (context);

      // if BU's company == Host Company, then only set the value to Internal
      if (hostCompId.equals(compId))
      {
        DomainRelationship.setAttributeValue(context, relId, attrSupplierType, "Internal");
      }
    }

      }//end of method

  /**
    * This method returns true if the context user has
    * the Access on Message object.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds one argument, object id of Message
    * @returns boolean value
    * @throws Exception if the operation fails
    * @since Common 11.
    */
    public boolean hasRDO(Context context, String[] args) throws Exception
    {

      try{

      String objId = this.getId();
      DomainObject domainObj = new DomainObject(objId);
      String objectType = domainObj.getInfo(context, DomainConstants.SELECT_TYPE);

      String parentId="";

      if(DomainConstants.TYPE_MESSAGE.equalsIgnoreCase(objectType))
      {
         String mqlString = "expand bus $1 to rel $2 recurse to end select $3 $4 dump $5";
           parentId =   MqlUtil.mqlCommand(context,mqlString, objId, RELATIONSHIP_REPLY, "businessobject", "id", "|");
          if(!"".equals(parentId.trim()))
          {
              parentId = parentId.lastIndexOf("|")!=-1?parentId.substring(parentId.lastIndexOf("|") + 1).trim():"";
              DomainObject startObj = new DomainObject(parentId);
              parentId  = startObj.getInfo(context, "to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.id");
          }else{
              parentId = domainObj.getInfo(context, "to["+RELATIONSHIP_MESSAGE+"].from.to["+RELATIONSHIP_THREAD+"].from.id");

          }

          domainObj = new DomainObject(parentId.trim());
      }

      // commented by BSB for bug 377729
      // objectType = domainObj.getInfo(context, DomainConstants.SELECT_TYPE);

       String RDOid = domainObj.getInfo(context, SELECT_RDO_ID);

      // first see if the parent object is wired to an RDO
        if (RDOid == null || RDOid.equals(""))
        {
            // no RDO, return true
            return true;
        }
        else
        {
            // search for RDO entry in cache
            StringList roles = null;
            StringList associationRoles = null;

            if (roles == null)
            {
                //get the Project Role attribute value from the Member rel
                StringList relSelects = new StringList(1);
                relSelects.add("attribute["+ATTRIBUTE_PROJECT_ROLE+"]");

                // get the rel to the specific RDO only
                String objectWhere = "id == \'" + RDOid + "\'";

                Person person = Person.getPerson(context);
                //navigate from person to RDO via the member rel;
                MapList memberMapList = person.getRelatedObjects(
                                        context,            // context
                                        RELATIONSHIP_MEMBER,// relationship pattern
                                        QUERY_WILDCARD,     // object pattern
                                        null,               // object selects
                                        relSelects,         // relationship selects
                                        true,               // to direction
                                        false,              // from direction
                                        (short) 1,          // recursion level
                                        objectWhere,        // object where clause
                                        null);              // relationship where clause

                // not found in cache, must add new entry
                if (memberMapList == null || memberMapList.size() < 1)
                {
                    // person not member of RDO,
                    return false;
                }
                else
                {   // getting role from Association
                    String strPersonName = "";
                    String strRoles = "";
                    String mqlString = "print association $1";
                    String ascString = MqlUtil.mqlCommand(context,mqlString, PropertyUtil.getSchemaProperty(context,"association_PrivateDiscussion"));
                    strRoles=ascString.indexOf("definition")!=-1?ascString.substring(ascString.indexOf("definition") + 10):"";
                    strRoles=strRoles.indexOf("\n")!=-1?strRoles.substring(0,strRoles.indexOf("\n")).trim():"";


                    String strRoleMap = (String) assRoleMap.get(strRoles);
                    if(strRoleMap != null && !"".equals(strRoleMap))
                    {
                        //do nothing
                    }
                    else
                    {

                        StringList assRoles = new StringList(1);
                        StringList strORList = new StringList();
                        StringList strTempList = new StringList();
                        String strTemp = "";
                        StringList strAndList = FrameworkUtil.splitString(strRoles," && ");
                        if(strAndList != null && strAndList.size() > 0)
                        {
                            for(int iTemp = 0; iTemp <strAndList.size(); iTemp++)
                            {
                                strTemp = (String) strAndList.get(iTemp);
                                strTempList = FrameworkUtil.splitString(strTemp," || ");
                                if(strTempList.size() > 1)
                                    assRoles.addAll(strTempList);
                                else
                                    assRoles.add(strTemp);
                            }
                        }

                        StringBuffer strbufRoles = new StringBuffer(1);
                        for(int iassRoles=0; iassRoles < assRoles.size(); iassRoles++)
                        {
                            String strRole = (String) assRoles.get(iassRoles);
                            if(strRole.indexOf("\"") != -1)
                                strRole = FrameworkUtil.findAndReplace(strRole,"\"","");
                            String strSymName = "";

                            if( (strSymName = FrameworkUtil.getAliasForAdmin(context, "role", strRole, true)) != null)
                                strbufRoles.append(strSymName);
                            else if((strSymName = FrameworkUtil.getAliasForAdmin(context, "group", strRole, true)) != null)
                                strbufRoles.append(strSymName);
                            else if((strSymName = FrameworkUtil.getAliasForAdmin(context, "group", strRole, true)) != null)
                                strbufRoles.append(strSymName);
                            strbufRoles.append(",");
                        }
                        strRoleMap = strbufRoles.toString();
                        strRoleMap = strRoleMap.substring(0,strRoleMap.length()-1);
                        assRoleMap.put(strRoles,strRoleMap);

                    }
                    StringList assRoleList = FrameworkUtil.split(strRoleMap,",");

                    // get roles in the Project Role attribute on the context user's Member connection
                    Map personMap = (Map) memberMapList.get(0);
                    String projectRole = (String) personMap.get("attribute["+ATTRIBUTE_PROJECT_ROLE+"]");

                    if (projectRole != null)
                    {
                        roles = FrameworkUtil.split(projectRole, "~");
                    }
                    String tempRole ="";

                   for (int i=0; i < assRoleList.size(); i++)
                   {
                       tempRole = (String) assRoleList.get(i);
                        if (roles.contains(tempRole))
                        {
                        // role found, return true
                            return true;
                        }

                   }

                }
            }
        }

        return false;
       }catch (FrameworkException Ex) {
           throw Ex;
       }

    }
     /**
      * creates the admin user Type with name of the object created and also registers it.
      * the role created, and also registers it.
      * @param context the eMatrix <code>Context</code> object
      * @param args holds one argument, Name of Object Created
      * @return  0 if operation is Success.
      * @throws Exception if the operation fails
      * @since Common X2
      */
    public int createAdminUserOrganizationForOrganization(Context context,String[] args) throws Exception
    {
        boolean contextPushed = false;
        String name =args[0];
        try
        {


            if(!"".equals(name))
            {
                ContextUtil.pushContext(context);
                contextPushed=true;
                ContextUtil.startTransaction(context,false);
                // adding the role with the name of the object created and setting up the property on type with Organization
                String mqlCmd = "add role $1 $2";
                MqlUtil.mqlCommand(context, mqlCmd, name, "asanorg");
                ServerVersion version = new ServerVersion();
                version.open(context);
                String serverVersion = version.getVersion(context);
                version.close(context);

                String symbolicName = "organization_"+name;
                symbolicName = FrameworkUtil.findAndReplace(symbolicName, " ", "");
                // Registering the role
                mqlCmd =" add property $1 on program $2 to role $3";
                MqlUtil.mqlCommand(context, mqlCmd,symbolicName,"eServiceSchemaVariableMapping.tcl",name);
                // setting the Properties
                StringBuffer mqlProperty = new StringBuffer();
                mqlProperty.append("modify role ");
                mqlProperty.append("\"");
                mqlProperty.append(name);
                mqlProperty.append("\" ");
                mqlProperty.append("property \"application\" value \"Framework\"");
                mqlProperty.append("property \"version\" value \""+serverVersion+"\"");
                mqlProperty.append("property \"original name\" value \""+name+"\"");

                MqlUtil.mqlCommand(context, mqlProperty.toString());
                ContextUtil.commitTransaction(context);
            }
        }
        catch (Exception ex)
        {
            ContextUtil.abortTransaction(context);
            return 1;
        }
        finally
        {
            if(contextPushed)
            {
                ContextUtil.popContext(context);
            }
        }

        return 0;
    }
          /**
      * set the Parent object role to the created organization as the Parent Object
      * @param context the eMatrix <code>Context</code> object
      * @param args holds two arguments
      * Name - name of the old object .i.e. before modification
      * NewName - name of the Object which is newly created
      * @return  0 if operation is Success.
      * @throws Exception if the operation fails
      * @since Common X2
      */
    public int inheritParentAdminUserOrganizationOfOrganization(Context context, String[] args) throws Exception
    {
        boolean contextPushed = false;
        String fromName =args[0];
        String toName =args[1];
        try
        {
            if(!"".equals(fromName) && !"".equals(toName))
            {
                ContextUtil.pushContext(context);
                contextPushed=true;
                // modify the role of the to object by setting up the parent role for the from Object
                String mqlCmd = "modify role \"" + toName + "\" asanorg" + " parent \"" + fromName + "\" ";
                MqlUtil.mqlCommand(context, mqlCmd);
            }
        }
        catch (Exception ex)
        {
             throw (new FrameworkException(ex));
        }
        finally
        {
            if(contextPushed)
            {
                ContextUtil.popContext(context);
            }
        }
        return 0;
    }

      /**
      * modifiy the admin organization with the new name of the object
      * @param context the eMatrix <code>Context</code> object
      * @param args holds two arguments
      * Name - name of the old object .i.e. before modification
      * NewName - name of the Object which is newly created
      * @return  0 if operation is Success.
      * @throws Exception if the operation fails
      * @since Common X2
      */
    public int updateAdminUserOrganizationForOrganization(Context context, String[] args) throws Exception
    {
        int OK = 0;
        int ERROR = 1;

        String oldName = args[0];
        String newName = args[1];

        if((oldName == null || "".equals(oldName)) || (newName == null || "".equals(newName)))
            return ERROR;

        try
        {
            if(Organization.hasAdminUserRole(context, oldName)) {
                String mqlCmd = "modify role $1 name $2";
                MqlUtil.mqlCommand(context, mqlCmd, true, oldName, newName);
            }
       }
        catch (Exception ex)
        {
           return ERROR;
        }
        return OK;

    }
    /**
     * This method check whether organization roles is there are not to allot lead role to this organization.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[]
     * @throws Exception if operation fails.
     * @since AEF X3
     */


    public int checkOrganizationRole(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Getting lead roles values from argument.
        String sLeadRoles[] = (String[]) programMap.get("leadRoles");
        String sObjectId = (String) programMap.get("objectId");
        String sRelId = (String) programMap.get("relId");
        //Creating relationship object.
        DomainRelationship domRel = DomainRelationship.newInstance(context, sRelId);
        //Getting attibute project role values for that relationship.
        StringList slRoles = AttributeUtil.getAttributeListValueList(context,domRel, DomainConstants.ATTRIBUTE_PROJECT_ROLE, "~");
        int iRolCount = 0;
        //Logic to check whether organization role is there or not.
        if(sLeadRoles != null && slRoles != null)
        for( int iRolItr=0; iRolItr < sLeadRoles.length ; iRolItr++)
        {
            if((slRoles.contains(sLeadRoles[iRolItr]))==false)
                iRolCount = 1;
        }
        return iRolCount;
    }

    /**
     * This method check whether lead role already assigned to some other person in this organization.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[]
     * @throws Exception if operation fails.
     * @since AEF X3
     */


    public String checkLeadRoleAssign(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        // Getting lead role values from arguments.
        String sLeadRoles[] = (String[]) programMap.get("leadRoles");
        String sObjectId = (String) programMap.get("objectId");
        // Creating person object.
        DomainObject personDo = new DomainObject(sObjectId);
        String sRelId = (String) programMap.get("relId");
        String strLanguage = (String) programMap.get("languageStr");
		strLanguage = UIUtil.isNullOrEmpty(strLanguage) ? context.getSession().getLanguage() : strLanguage;
        int iRolCount = 0;
        String iReturn = "";
            String sIDs = "";
        //Creating relationship object.
        Relationship rel = new Relationship(sRelId);
        rel.open(context);
        DomainObject fromDo = new DomainObject(rel.getFrom());
        rel.close(context);
        StringList slRelSel = new StringList();
        slRelSel.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
        slRelSel.add(DomainConstants.SELECT_ID);
        slRelSel.add("to.id");
        StringList slObjSel = new StringList();
        slObjSel.add(DomainConstants.SELECT_ID);
        slObjSel.add("attribute["+DomainConstants.ATTRIBUTE_FIRST_NAME+"]");
        slObjSel.add("attribute["+DomainConstants.ATTRIBUTE_LAST_NAME+"]");
        //Get all the objects related with Lead Responsibility relationship with attribute.
        MapList mapList = (MapList) fromDo.getRelatedObjects(context,PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility"),"*",slObjSel,slRelSel,false,true,(short)1,"","");
        Iterator mapListItr = mapList.iterator();
        //logic to check whether selected lead role is already assigned to any other person.
        if (mapList != null)
        while (mapListItr.hasNext())
        {
            Map perMap = (Map) mapListItr.next();
            String existingRoles = (String) perMap.get("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
            existingRoles = "~"+existingRoles+"~";
            if(sLeadRoles != null)
            for ( int iLeadRole=0; iLeadRole < sLeadRoles.length ; iLeadRole++)
                if( (existingRoles.indexOf("~"+sLeadRoles[iLeadRole]+"~")!= -1) && !(perMap.get("to.id").equals(personDo.getInfo(context,DomainConstants.SELECT_ID))))
            {
                    iRolCount = 1 ;
                        sIDs += perMap.get(DomainConstants.SELECT_ID) + "#" +sLeadRoles[iLeadRole] + "#" ;
                        //iReturn += PropertyUtil.getSchemaProperty(context,sLeadRoles[iLeadRole])+ ", ";
                        i18nNow loc = new i18nNow();
                        String strrole=PropertyUtil.getSchemaProperty(context,sLeadRoles[iLeadRole]);
                        strrole = i18nNow.getAdminI18NString("Role", strrole.trim() ,strLanguage);
                        iReturn += strrole+" ";
                        iReturn +=loc.GetString("emxComponentsStringResource",strLanguage, "emxComponents.Common.isLeadRoleAssigned")+" ";
                        iReturn += perMap.get("attribute["+DomainConstants.ATTRIBUTE_FIRST_NAME+"]") + " , ";
                        iReturn += perMap.get("attribute["+DomainConstants.ATTRIBUTE_LAST_NAME+"]")+ "\\n" ;
            }
        }

        if(iRolCount==1)
        iReturn = iReturn.substring(0,iReturn.length()-2);
        iReturn = sIDs + iReturn;
        return iReturn;
    }

    /**
     * This method sets lead role to this organization.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[]
     * @throws Exception if operation fails.
     * @since AEF X3
     */


    public int setLeadRoles(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Get lead role values from arguments.
        String sLeadRoles[] = (String[]) programMap.get("leadRoles");
        String sObjectId = (String) programMap.get("objectId");
        String sRelId = (String) programMap.get("relId");
        //Creating  person object
        DomainObject personDo = new DomainObject(sObjectId);
        int iRolCount = 0;
        Relationship rel = new Relationship(sRelId);
        rel.open(context);
        //Creating company 0r business unit object
        DomainObject fromDo = new DomainObject(rel.getFrom());
        rel.close(context);
        StringList slRelSel = new StringList();
        slRelSel.add("to");
        String sLeadResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
        // Do nothing if Lead Responsibility relationship is not present.
        if (!"".equals(sLeadResponsibility)) {
            //Get all the objects related with Lead Responsibility relationship with attribute.
            MapList mapList = (MapList) fromDo.getRelatedObjects(context,sLeadResponsibility,"*",DomainConstants.EMPTY_STRINGLIST,slRelSel,false,true,(short)1,"","");
            Iterator mapListItr = mapList.iterator();
            int checkRole = 0;
            //Logic to set lead roles to person.
            if (mapList != null)
            while (mapListItr.hasNext())
            {
                Map perMap = (Map) mapListItr.next();
                String existingRoles = (String) perMap.get("to");
                if(existingRoles.equals(personDo.getInfo(context,"name")))
                    checkRole=1;
            }

            // If relationship already exists between company or business unit to person.
            if(checkRole==1)
            {
                MQLCommand mqlCom = new MQLCommand();
                mqlCom.executeCommand(context,"print connection bus $1 to $2 relationship $3 select $4 dump", fromDo.getId(), personDo.getId(), sLeadResponsibility, "id");
                String newRelId = mqlCom.getResult().substring(0,mqlCom.getResult().length()-1);
                if(sLeadRoles!=null)
                    AttributeUtil.setAttributeList(context,new DomainRelationship(newRelId),PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole"),sLeadRoles,false,"~");
                else
                    AttributeUtil.setAttributeList(context,new DomainRelationship(newRelId),PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole"),DomainConstants.EMPTY_STRINGLIST,false,"~");
            }
            // If relationship not exists between company or business unit to person.
            else
            {
                RelationshipType relType = new RelationshipType(sLeadResponsibility);
                //Connecting person to business unit or company
                DomainRelationship domRel = DomainRelationship.connect(context,fromDo,relType,personDo);
                if(sLeadRoles!=null)
                    AttributeUtil.setAttributeList(context,domRel,PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole"),sLeadRoles,false,"~");
                else
                    AttributeUtil.setAttributeList(context,domRel,PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole"),DomainConstants.EMPTY_STRINGLIST,false,"~");
            }
        }
        return iRolCount;
    }


    /**
     * This method gets lead role from this organization.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[]
     * @throws Exception if operation fails.
     * @since AEF X3
     */


    public String getPersonLeadRoles(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String sObjectId = (String) programMap.get("objectId");
        String sRelId = (String) programMap.get("relId");
        //Creating person object
        DomainObject personDo = new DomainObject(sObjectId);
        Relationship rel = new Relationship(sRelId);
        rel.open(context);
        //Creating business unit or company
        DomainObject fromDo = new DomainObject(rel.getFrom());
        rel.close(context);
        StringList slRelSel = new StringList();
        slRelSel.add("attribute["+PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole")+"]");
        StringList slObjSel = new StringList();
        slObjSel.add(DomainConstants.SELECT_ID);
        //Get all the objects related with Lead Responsibility relationship with attribute.
        MapList mapList = (MapList) fromDo.getRelatedObjects(context,PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility"),"*",slObjSel,slRelSel,false,true,(short)1,"","");
        Iterator mapListItr = mapList.iterator();
        String str = "";
        //Logic to get lead role values.
        if (mapList != null)
        while (mapListItr.hasNext())
        {
            Map perMap = (Map) mapListItr.next();
            String existingRoles = (String) perMap.get("attribute["+PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole")+"]");
            if(perMap.get(DomainConstants.SELECT_ID).equals(personDo.getInfo(context,DomainConstants.SELECT_ID)))
                str = existingRoles;

        }

        return str;
    }

    /*
     * Returns the lead roles the person has for the organization to table PeopleSummary and roles are comma separated.
     *
     * @param context The Matrix Context.
     * @param args holds object id list and parameter list
     * @return a Vector of part origin values
     * @throws Exception if the operation fails
     * @since Common X3
     * @grade 0
     */

    public Vector getPersonSummaryAssignedLeadRoles(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String strLanguage      = (String)paramMap.get("languageStr");
        String objectId = (String) paramMap.get("objectId");

        String selAttrProjectRole = DomainObject.getAttributeSelect(ATTRIBUTE_PROJECT_ROLE);

        DomainObject obj = DomainObject.newInstance(context);
        obj.setId(objectId);

        MapList leadResPersons = (MapList) obj.getRelatedObjects(context, RELATIONSHIP_LEAD_RESPONSIBILITY, TYPE_PERSON,
                                                                          new StringList(SELECT_ID), new StringList(selAttrProjectRole),
                                                                          false,true,
                                                                          (short)1,
                                                                          EMPTY_STRING, EMPTY_STRING,
                                                                          0,
                                                                          null, null, null);
        Map rolesByPerson = new HashMap(leadResPersons.size());
        for (int i = 0; i < leadResPersons.size(); i++) {
            Map person = (Map) leadResPersons.get(i);
            rolesByPerson.put(person.get(SELECT_ID), person.get(selAttrProjectRole));
        }

        Vector v = new Vector(relBusObjPageList.size());

        for (int i = 0; i < relBusObjPageList.size(); i++) {
            Map person = (Map) relBusObjPageList.get(i);
            String leadRoles = (String) rolesByPerson.get(person.get(SELECT_ID));
            leadRoles = UIUtil.isNullOrEmpty(leadRoles) ? EMPTY_STRING :
                                                          getI18NRoleListValue(context, strLanguage, FrameworkUtil.split(leadRoles, "~"));
            v.add(leadRoles);
        }
        return v;
    }


    /*
     * Returns the lead roles the person has for the organization to table PeopleSummary and roles are comma separated.
     *
     * @param context The Matrix Context.
     * @param args holds object id list and parameter list
     * @return a Vector of part origin values
     * @throws Exception if the operation fails
     * @since Common X3
     *
     * @grade 0
     *
     */

    public Vector getAssignedLeadRoles(Context context, String[] args)
    throws Exception
    {

        //TODO this method needs to be cleaned up
        //There are lot of unnecessary DB hits in loop
        //Use getPersonSummaryAssignedLeadRoles
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String strLanguage      = (String)paramMap.get("languageStr");
        Vector v = new Vector();
        Iterator relBusObjPageListItr = relBusObjPageList.iterator();
        String sRoles = "";
        String sLeadResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
        // Return empty list if Lead Responsibility relationship is not present.
        if (relBusObjPageList != null && !"".equals(sLeadResponsibility))
        while (relBusObjPageListItr.hasNext())
        {
            Map perMap = (Map) relBusObjPageListItr.next();
            String personId = (String)perMap.get(DomainConstants.SELECT_ID);
            Relationship rel = new Relationship((String)perMap.get("id[connection]"));
            rel.open(context);
            DomainObject fromDo = new DomainObject(rel.getFrom());
            rel.close(context);
            StringList slRelSel = new StringList();
            String selAttrProjectRole = "attribute["+PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole")+"]";
            slRelSel.add(selAttrProjectRole);
            StringList slObjSel = new StringList();
            slObjSel.add(DomainConstants.SELECT_ID);
            //Get all the objects related with Lead Responsibility relationship with attribute.
            MapList mapList = (MapList) fromDo.getRelatedObjects(context,sLeadResponsibility,"*",slObjSel,slRelSel,false,true,(short)1,"","");
            Iterator mapListItr = mapList.iterator();

            String str = "";
            String existingRoles = "";
            if (mapList != null)
            while (mapListItr.hasNext())
            {
                Map perMap1 = (Map) mapListItr.next();
                try
                {
                        existingRoles = (String) perMap1.get(selAttrProjectRole);
                }
                catch (Exception ex)
                {
                        existingRoles = (String) ((StringList) perMap1.get(selAttrProjectRole)).get(0);
                }
                if(perMap1.get(DomainConstants.SELECT_ID).equals(personId))
                    str = existingRoles;
            }
            v.add(getI18NRoleListValue(context, strLanguage, FrameworkUtil.split(str, "~")));
        }
        return v;
    }


    /*
     * This method used to return Organization type names to select while searching.
     *
     * @param context The Matrix Context.
     * @return a StringList which contains Organization list.
     * @throws Exception if the operation fails
     * @since Common X3
     * @grade 0
     */


 public StringList getTypeNames(Context context, String args[] )  throws Exception
 {
   StringList slTypeNames = new StringList();
   slTypeNames.add(PropertyUtil.getSchemaProperty(context,"type_Organization"));
   slTypeNames.add(PropertyUtil.getSchemaProperty(context,"type_BusinessUnit"));
   slTypeNames.add(PropertyUtil.getSchemaProperty(context,"type_Company"));
   slTypeNames.add(PropertyUtil.getSchemaProperty(context,"type_Department"));
   return slTypeNames;
 }


    /*
     * This method used to get row ids for ECOrganizationSearchIDs.
     * @param context The Matrix Context, Type,Name as parameters to be appended with emxTable.jsp.
     * @return ids which have all lead roles defined to persons
     * @throws Exception if the operation fails
     * @since Common X3
     * @grade 0
     */


 public MapList organizationSearchIDs(Context context, String args[] ) throws Exception
 {
   MapList slObjectIds = new MapList();
   i18nNow i18nnow = new i18nNow();
   String result="";
   String error="";
   String resultRecords = "";
   String resultFields = "";
   HashMap programMap = (HashMap)JPO.unpackArgs(args);
   //Getting Type, Name and Description
   String sType = (String)programMap.get("Type");
   String sName = (String)programMap.get("Name");
   String sDescription = (String)programMap.get("Description");
   String strLanguage = (String)programMap.get("languageStr");

   if(sType!=null && sName!=null && sDescription!=null)
   {
    //MQL query to get all Organization ids which satisfy the criteria given from webform
    String query = "temp query bus $1 $2 $3 where $4 select $5 dump $6";
    MQLCommand mqlCmd = new MQLCommand();
    if(mqlCmd.executeCommand(context,query, sType, sName, "*", "description smatch "+sDescription, "id", "|"))
    {
     //Storing Result.
     result = mqlCmd.getResult();
    }
    else
    {
     error = mqlCmd.getError();
    }
    // Tokenizing Result
    StringTokenizer stRecords = new StringTokenizer(result,"\\n");
    while (stRecords.hasMoreTokens()) {
        resultRecords = stRecords.nextToken();
        StringTokenizer stFields = new StringTokenizer(resultRecords,"|");
        for(int i=0;stFields.hasMoreTokens();i++) {
            resultFields = stFields.nextToken();
                if(i == 3 ) {
                    //Gets objectid.
                    DomainObject doObj = new DomainObject(resultFields);
                    //Object Selectables list
                    StringList objSelects = new StringList();
                    objSelects.add(DomainConstants.SELECT_ID);
                    //Relationship Selectable list
                    StringList relSelects = new StringList();
                    relSelects.add("attribute["+PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole")+"]");
                    //Get all ids and attribute values
                    MapList mlLeadRoles = doObj.getRelatedObjects(context,PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility"),PropertyUtil.getSchemaProperty(context,"type_Person"),objSelects,relSelects,false,true,(short)1,"","");
                    String sLeadRoles = mlLeadRoles.toString();
                    //Get Defined Lead Roles from properties.
                    String sDefinedLeadRoles = EnoviaResourceBundle.getProperty(context, "emxComponents", context.getLocale(), "emxComponents.RCO.DefinedLeadResponsibilities");
                    String sDefinedLeadRolesList [] = StringUtils.split(sDefinedLeadRoles, ",");
                    //Check for lead roles whether all exists for that Organization or not.
                    int iDLRCount = 0;
                    for(int iDLR = 0 ; iDLR < sDefinedLeadRolesList.length ; iDLR++)
                        if(sLeadRoles.indexOf(sDefinedLeadRolesList[iDLR]) >= 0)
                          iDLRCount++;
                    // If all lead roles are assigned , add id to table.
                    if(sDefinedLeadRolesList.length == iDLRCount)
                    {
                        HashMap hm = new HashMap();
                        hm.put(DomainConstants.SELECT_ID,resultFields);
                        slObjectIds.add(hm);
                       }
                   }
                }
            }
        }
        return slObjectIds;
    }


    /*
     * This method used to decide the occurence of Lead Roles column in People Summary Table.
     * @param context
     * @return boolean value
     * @throws Exception if the operation fails
     * @since Common X3
     * @grade 0
     */



    public Boolean showLeadRoles(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        i18nNow i18nnow = new i18nNow();
        String strLanguage = (String)programMap.get("languageStr");
        //Get value from property file.
        String sLeadRolesEnabled = EnoviaResourceBundle.getProperty(context, "emxComponents", context.getLocale(), "emxComponents.RCO.EnableLeadRolesForOrganizations");
        if(sLeadRolesEnabled.equalsIgnoreCase("true"))
            return Boolean.valueOf(true);
        else
            return Boolean.valueOf(false);
    }

   /**
    * Method to return the list (names) of RDO/RMOs Part is associated to. Used for Advance search for Parts
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments. first parameter object Id of matrix object
    * @return void.
    * @throws Exception if the operation fails.
    * @since X+3
    */
   public String getAdvancedACCESSRDOAndRMOs(Context context, String args[]) throws Exception
   {
        String finalReturn   = "";
        StringList finalList = new StringList();
        String sPartId = args[0];
        DomainObject domPart = new DomainObject(sPartId);
        //Commented and modified for bug 359291
        //String strType = (String) domPart.getInfo(context, DomainConstants.SELECT_TYPE);
        String strType = args[1];

       StringList busSelects = new StringList(2);
       StringList relSelects = new StringList(1);

       busSelects.add(DomainConstants.SELECT_ID);
       busSelects.add(DomainConstants.SELECT_NAME);

       MapList mlRDOs = domPart.getRelatedObjects(context,
                                                  DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
                                                  "*",
                                                  busSelects,
                                                  relSelects,
                                                  true,
                                                  false,
                                                  (short)1,
                                                  "",
                                                  "");

       if(!mlRDOs.isEmpty()) {
           Iterator itrRDO = mlRDOs.iterator();
           while(itrRDO.hasNext()){
               Map mapRDO = (Map)itrRDO.next();
               String sId = (String)mapRDO.get(DomainConstants.SELECT_ID);
               if(!finalList.contains(sId)){
                   finalReturn += matrix.db.SelectConstants.cSelectDelimiter;
                   finalReturn += sId;
                   finalList.add(sId);
               }
           }
       }

      if (mxType.isOfParentType(context,strType,DomainConstants.TYPE_PART))
      {
           String sPartName = domPart.getInfo(context, DomainConstants.SELECT_NAME);
           MapList mlPartMaster = null;
           String sPartMasterId = null;

           //Search for RMO starts
           //Start : IR366583V6R2011. this is the same fix for IR-029792V6R2011, IR-036618V6R2011, IR-038820V6R2011
           mlPartMaster = DomainObject.findObjects(context,
                                                   PropertyUtil.getSchemaProperty(context,"type_PartMaster"),
                                                   sPartName,
                                                   strType,
                                                   "*",
                                                   "*",
                                                   "",
                                                   true,
                                                   busSelects);
           //End : IR366583V6R2011

           if(mlPartMaster!=null && !mlPartMaster.isEmpty()){
               Iterator mapListItr = mlPartMaster.iterator();
               while(mapListItr.hasNext()){
                   Map map = (Map) mapListItr.next();
                   String sName  = (String) map.get(DomainConstants.SELECT_NAME);
                   sPartMasterId = (String) map.get(DomainConstants.SELECT_ID);
                   if(sName.equals(sPartName)){
                       break;
                   }
               }
           }//End of if(mlPartMaster!=null && ......


           if(sPartMasterId!=null && !"".equals(sPartMasterId) && !"null".equals(sPartMasterId)){

               DomainObject domPartMaster = new DomainObject(sPartMasterId);

               MapList mlRMOs = domPartMaster.getRelatedObjects(context,
                                                              DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
                                                              "*",
                                                              busSelects,
                                                              relSelects,
                                                              true,
                                                              false,
                                                              (short)1,
                                                              "",
                                                              "");
               if(!mlRMOs.isEmpty()){
                   Iterator itrRMO = mlRMOs.iterator();
                   while(itrRMO.hasNext()){
                       Map mapRMO = (Map)itrRMO.next();
                       String sId = (String)mapRMO.get(DomainConstants.SELECT_ID);
                       if(!finalList.contains(sId)){
                           finalReturn += matrix.db.SelectConstants.cSelectDelimiter;
                           finalReturn += sId;
                           finalList.add(sId);
                       }
                   }
               }
           }//End of if(sPartMasterId!=null && .....
       }//End of if(sPartId!=null && ......
       return finalReturn;
   }

   /**
    * Access function to show/hide Licensing column called from person Search result table
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds  arguments.
    * @return Boolean holding true/false
    * @throws Exception if the operation fails.
    * @since R208.
    */


   public Boolean hasEditLicenseAccess(Context context, String[] args) throws Exception
   {

        String strLoginUser = context.getUser();
        matrix.db.Person mxDbPerson = new matrix.db.Person(strLoginUser);
        mxDbPerson.open(context);
       if( mxDbPerson.isAssigned(context,DomainConstants.ROLE_ORGANIZATION_MANAGER) || mxDbPerson.isAssigned(context,"Administration Manager"))
       {
        return Boolean.valueOf("true");
       }
       else
       {
        return Boolean.valueOf("false");}
     }
//Added:Nov 6, 2008:OEF:R208:PRG Adv Resource Planning

   /**
    * This Function returns only those person who have the role Resource Manager & Active
    * @param context Matrix Context Object
    * @param args holds input packed arguments send by autonomy search framework
    * @return StringList of Id's which will get excluded from autonomy search
    * @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList getExcludeOIDForResourceManagerSearch(Context context, String args[]) throws Exception
   {
       try
       {
           Map programMap = (Map) JPO.unpackArgs(args);
           String strOrganizationId = (String) programMap.get("objectId");

           String strSearchAllResourceManager = EnoviaResourceBundle.getProperty(context,"emxComponents.ResourcePool.ResourceManager.SearchInAllOrganization") ;
          //String strSearchAllResourceManager = "true";
           boolean isSearchingAllResourceManager =  ("true").equalsIgnoreCase(strSearchAllResourceManager)?true:false;

           final String SELECT_ORGANIZATION_ID = "from.id";
           final String SELECT_MEMBER_NAME = "to.name";
           final String SELECT_MEMBER_ID = "to.id";
           final String SELECT_MEMBER_STATE = "to.current";
           String strRelPattern = RELATIONSHIP_MEMBER;
           String strRelExpression = null;
          // String strVaultPattern = context.getVault().getName();
           String strVaultPattern = "*";

           if(isSearchingAllResourceManager)
           {
               strRelExpression = "";
           }
           else
           {
               //strRelExpression = " "+ SELECT_ORGANIZATION_ID +" == \""+strOrganizationId+"\" ";.
               strRelExpression = " "+ SELECT_ORGANIZATION_ID +" == "+strOrganizationId+" ";
               //   strRelExpression = " "+ SELECT_ORGANIZATION_ID +" != "+strOrganizationId+" ";
           }

           short nObjectLimit = 0;
           StringList slSelectStmts = new StringList();
           StringList slOrderBy = new StringList();

           slSelectStmts.add(SELECT_ORGANIZATION_ID);
           slSelectStmts.add(SELECT_MEMBER_NAME);
           slSelectStmts.add(SELECT_MEMBER_ID);
           slSelectStmts.add(SELECT_MEMBER_STATE);

           RelationshipWithSelectList relationshipWithSelectList = Relationship.query(context,
                                                                                                               strRelPattern,
                                                                                                               strVaultPattern,
                                                                                                               strRelExpression,
                                                                                                               nObjectLimit,
                                                                                                               slSelectStmts,
                                                                                                               slOrderBy);


           RelationshipWithSelect relationshipWithSelect = null;
           String strMemberName = null;
           String strMemberId = null;
           String strMemberState = null;

           StringList slMemberNames = new StringList();
           StringList slMemberIds = new StringList();
           StringList slMemberStates = new StringList();
           MapList mlMemberList = new MapList();



           for (RelationshipWithSelectItr relationshipWithSelectItr = new RelationshipWithSelectItr(relationshipWithSelectList);relationshipWithSelectItr.next();)
           {
               relationshipWithSelect = relationshipWithSelectItr.obj();

               strMemberName = relationshipWithSelect.getSelectData(SELECT_MEMBER_NAME);
               strMemberId = relationshipWithSelect.getSelectData(SELECT_MEMBER_ID);
               strMemberState = relationshipWithSelect.getSelectData(SELECT_MEMBER_STATE);
               HashMap mapMemberInfo = new HashMap();
               mapMemberInfo.put("MemberName",strMemberName);
               mapMemberInfo.put("MemberId",strMemberId);
               mapMemberInfo.put("MemberState",strMemberState);

        	   if (!mlMemberList.contains(mapMemberInfo))
        	   {
               mlMemberList.add(mapMemberInfo);
        	   }

           }

           // Existing code
           StringList  slActiveUserIds = new StringList();
           StringList  slActiveUserNames = new StringList();

           for(int i = 0; i < mlMemberList.size(); i++)
           {
               Map mapUser = (Map) mlMemberList.get(i);

               String strUserId = (String) mapUser.get("MemberId");
               String strUserName= (String) mapUser.get("MemberName");

               String strCurrentState = (String) mapUser.get("MemberState");
               if(strCurrentState.equalsIgnoreCase(DomainConstants.STATE_PERSON_ACTIVE))
               {
                   slActiveUserNames.add(strUserName);
                   slActiveUserIds.add(strUserId);
               }
           }

           //End all Users
           StringList slFinalUsersWithRole = new StringList();
           String strResourceManager = null;
           String sExtProjectUserRole = null;
           StringList queryResultList = new StringList();

           strResourceManager = PropertyUtil.getSchemaProperty(context,"role_ResourceManager");
           matrix.db.Role matrixRole = new matrix.db.Role(strResourceManager);
           matrixRole.open(context);

           StringList projectUsers = new StringList();

           UserList assignments    = matrixRole.getAssignments(context);

           UserList slActiveRMList = new UserList();
           StringList slRMList = new StringList();
           // get all active Resource Managers
           for (int i = 0; i < assignments.size(); i++)
           {
               if (slActiveUserNames.contains(((matrix.db.Person)assignments.get(i)).getName()))
               {
                   slActiveRMList.add (((matrix.db.Person)assignments.get(i)).getName());
               }
               else if(!slActiveUserNames.contains(((matrix.db.Person)assignments.get(i)).getName()))//TODO Also get the members which do not have "Resource Manager" Role
               {
                   String strPersonId  = PersonUtil.getPersonObjectID(context,((matrix.db.Person)assignments.get(i)).getName());
                   slRMList.add(strPersonId);
               }

           }

           String strVPMProjectAdmin = PropertyUtil.getSchemaProperty(context,"role_VPLMProjectAdministrator");
           StringList slVPLMProjectAdminUserList = null;
           if(null != strVPMProjectAdmin && !"null".equalsIgnoreCase(strVPMProjectAdmin.trim()) && !"".equalsIgnoreCase(strVPMProjectAdmin.trim()))
           {
        	   String sCommandStatement = "print role $1 select $2 dump $3";
        	   String strVPLMProjectAdminUsers =  MqlUtil.mqlCommand(context, sCommandStatement,strVPMProjectAdmin, "person",",");
        	   slVPLMProjectAdminUserList = FrameworkUtil.split(strVPLMProjectAdminUsers, ",");
           }
			
           int size = slVPLMProjectAdminUserList != null ? slVPLMProjectAdminUserList.size() : 0;
    	   for (int i = 0; i < size; i++)
    	   {
    		   String sUserName = (String)slVPLMProjectAdminUserList.get(i);
    		   if (slActiveUserNames.contains(sUserName))
    		   {
    			   slActiveRMList.add (sUserName);
    		   }
    		   else 
    		   {
    			   String strPersonId  = PersonUtil.getPersonObjectID(context,(sUserName));
    			   slRMList.add(strPersonId);
    		   }
    	   }
    	   
           // Exclude OID for Person
           String strPerson = null;
           StringList slExcludePersonList = new StringList();

           for (int i = 0;i < mlMemberList.size(); i++)
           {
               Map mapUser = (Map) mlMemberList.get(i);

               String strUserId = (String) mapUser.get("MemberId");
               String strUserName= (String) mapUser.get("MemberName");

               if (!slActiveRMList.contains(strUserName))
               {
                   slExcludePersonList.add(strUserId);
               }
           }
           slExcludePersonList.addAll(slRMList);

       //
      // to exclude the person which are removed from company but active or inactive in database
           String strWhere="to["+RELATIONSHIP_EMPLOYEE+"]==False";

           StringList slTypeSelects = new StringList();
           slTypeSelects.add(SELECT_ID);
           slTypeSelects.add(SELECT_CURRENT);

           MapList mlDBActiveUsers = DomainObject.findObjects(context, TYPE_PERSON, "*", strWhere, slTypeSelects);

           String strPersonId = null;
           String strPersonState = null;

           for (Iterator iterator = mlDBActiveUsers.iterator(); iterator.hasNext();)
           {
        	   Map mapPerson = (Map) iterator.next();
        	   strPersonId = (String) mapPerson.get(SELECT_ID);
        	   strPersonState = (String) mapPerson.get(SELECT_CURRENT);

        	   if(!slExcludePersonList.contains(strPersonId) || !STATE_PERSON_ACTIVE.equalsIgnoreCase(strPersonState))
        	   {
        		   slExcludePersonList.add(strPersonId);
        	   }
           }
          //
         //end
           return slExcludePersonList;
       }
       catch (Exception exp)
       {
           exp.printStackTrace();
           throw exp;
       }

   }

   /**
     * This Method will Assign (Connect) Selected Resource Managers to Business Units, it will also disconnect the resource managers already connected which are not selected
     *
     * @param context Matrix Context object
     * @param args The array of resource manager ids
     * @throws MatrixException if operation fails
     * @since PRG R208
     */
    public int assignResourceManagers (Context context, String[] args) throws MatrixException
    {
        try {
            if (context == null) {
                throw new IllegalArgumentException ("context");
            }

            StringList slResourceManagerIDs =  new StringList(args);
            if (slResourceManagerIDs == null) {
                slResourceManagerIDs = new StringList();
            }
            ContextUtil.startTransaction(context,true);

            Map mapResourceManagerRelIds = new HashMap(); // Used to map Person ID to corresponding rel id

            //
            // Find existing resource managers

            DomainObject dmoBU = DomainObject.newInstance (context,this.getId());

            String strRelationshipPattern = DomainConstants.RELATIONSHIP_RESOURCE_MANAGER;
            String strTypePattern = DomainConstants.TYPE_PERSON;

            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_ID);

            StringList slRelSelect = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);

            boolean getTo = false;
            boolean getFrom = true;
            short recurseToLevel = 1;
            String strBusWhere = "";
            String strRelWhere = "";

            MapList mlResourceManagers = dmoBU.getRelatedObjects(context,
                    strRelationshipPattern, //pattern to match relationships
                    strTypePattern, //pattern to match types
                    slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                    slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                    getTo, //get To relationships
                    getFrom, //get From relationships
                    recurseToLevel, //the number of levels to expand, 0 equals expand all.
                    strBusWhere, //where clause to apply to objects, can be empty ""
                    strRelWhere,0); //where clause to apply to relationship, can be empty ""
            StringList slAlreadyAssignedResourceManagers = new StringList();

            Map mapRelatedObjectInfo = null;
            String resourceManagerID = null;

            for (Iterator itrRelatedObjects = mlResourceManagers.iterator(); itrRelatedObjects .hasNext();)
            {
                mapRelatedObjectInfo = (Map) itrRelatedObjects.next();

                resourceManagerID = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_ID);
                slAlreadyAssignedResourceManagers.add(resourceManagerID);

                mapResourceManagerRelIds.put(resourceManagerID, (String)mapRelatedObjectInfo.get(DomainRelationship.SELECT_ID));
            }

            if (slResourceManagerIDs.size() == 0) {
                if (slAlreadyAssignedResourceManagers.size() != 0) {
                    // If nothing is selected we need to dsconnect the existing Resource Managers
                    StringList slRelIds = new StringList();

                    for (Iterator itrPersons = slAlreadyAssignedResourceManagers
                            .iterator(); itrPersons.hasNext();) {
                        String strPersonID = (String) itrPersons.next();
                        slRelIds.add(mapResourceManagerRelIds.get(strPersonID));
                    }

                    String[] strRelIDs = (String[])slRelIds.toArray(new String[slRelIds.size()]);
                    DomainRelationship.disconnect(context, strRelIDs);

                    return 0;
                }
            }

            //
            // Decide which new ones to be assigned
            //
            StringList slAssignment = new StringList();
            String strCurrentPersonId = null;
            for (Iterator itrPersons = slResourceManagerIDs.iterator(); itrPersons.hasNext();) {
                strCurrentPersonId = (String) itrPersons.next();
                if (!slAlreadyAssignedResourceManagers.contains(strCurrentPersonId)) {
                    slAssignment.add(strCurrentPersonId);
                }
            }

            if (slAssignment.size() != 0) {
                String[] strResourceManagers = new String[slAssignment.size()];
                strResourceManagers = (String[])slAssignment.toArray(strResourceManagers);
                DomainRelationship.connect (context, dmoBU, DomainConstants.RELATIONSHIP_RESOURCE_MANAGER, true, strResourceManagers);
            }

            //
            // Decide which new ones to be unassigned
            //
            StringList slUnassignment = new StringList();
            String strRelId = null;
            for (Iterator itrPersons = slAlreadyAssignedResourceManagers.iterator(); itrPersons.hasNext();) {
                strCurrentPersonId = (String) itrPersons.next();
                if (!slResourceManagerIDs.contains(strCurrentPersonId)) {
                    strRelId = (String)mapResourceManagerRelIds.get(strCurrentPersonId);
                    slUnassignment.add(strRelId);
                }
            }

            if (slUnassignment.size() != 0) {
                String[] strRelIds = new String[slUnassignment.size()];
                strRelIds = (String[])slUnassignment.toArray(strRelIds);
                DomainRelationship.disconnect(context, strRelIds);
            }

            ContextUtil.commitTransaction(context);

            return 0;
        }
        catch (Exception exp) {
            if (context != null) {
                ContextUtil.abortTransaction(context);
            }

            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

    /**
     * Finds the Resource Managers information. This method should not be used for table & form fields.
     *
     * @param context The Matrix Context object
     * @param args String array of selectables
     * @returns MapList information about Resource Managers
     * @throws MatrixException if context is null / the operation fails
     * @since PRG R208
     */
    public MapList getResourceManagers (Context context, String[] args) throws MatrixException
    {

        try {
            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            //
            // Following code find the connected objects
            //
            String strRelationshipPattern = RELATIONSHIP_RESOURCE_MANAGER;
            String strTypePattern = DomainConstants.TYPE_PERSON;

            StringList slBusSelect = new StringList(args);

            StringList slRelSelect = new StringList();

            boolean getTo = false;
            boolean getFrom = true;
            short recurseToLevel = 1;
            String strBusWhere = "";
            String strRelWhere = "";

            MapList mlResourceManagers = getRelatedObjects(context,
                                                            strRelationshipPattern, //pattern to match relationships
                                                            strTypePattern, //pattern to match types
                                                            slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                            slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                            getTo, //get To relationships
                                                            getFrom, //get From relationships
                                                            recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                            strBusWhere, //where clause to apply to objects, can be empty ""
                                                            strRelWhere); //where clause to apply to relationship, can be empty ""
            return mlResourceManagers;
        }
        catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

    /**
     * This method assigns selected persons as Resource managers to the Business Units field when selected from edit form mode.
     *  It is used for
     * -Field ResourceManager in form type_BusinessUnit
     * -Field ResourceManager in form type_Company
     *
     * @param context The Matrix Context object
     * @param args Packed ProgramMap
     * @throws MatrixException if context is null / objectId parameter is invalid / the operation fails
     * @since PRG R208
     */
    public void updateFieldResourceManagerData(Context context, String[] args) throws MatrixException
    {
        try
        {
            if(context == null) {
                throw new IllegalArgumentException("context");
            }
            Map programMap = (Map) JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            String strObjectId = (String)paramMap.get("objectId");
            if (!(strObjectId != null && !"".equals(strObjectId) && !"null".equals(strObjectId))) {
                throw new IllegalArgumentException(ComponentsUtil.i18nStringNow("emxComponents.Organisation.CannotFindObjectIdInProgramMap", context.getLocale().getLanguage()));
            }
            this.setId(strObjectId);
            String resourceMgrIds = (String) paramMap.get("New OID");
            StringList resourceMgrIdList =
                    (resourceMgrIds == null || "".equals(resourceMgrIds) || "null".equals(resourceMgrIds)) ? new StringList() :
                     resourceMgrIds.indexOf("|") != -1 ?  FrameworkUtil.split(resourceMgrIds, "|") : FrameworkUtil.split(resourceMgrIds, ",");
           assignResourceManagers(context, (String[]) resourceMgrIdList.toArray(new String[resourceMgrIdList.size()]));
        }
        catch(Exception exe)
        {
            exe.printStackTrace();
            throw new MatrixException(exe);
        }
    }


    /**
     * Returns comma separated names of the Resource Managers. This method is used for getting value of Resource Manager field on web form.
     * It is used for
     * -Field ResourceManager in form type_BusinessUnit
     * -Field ResourceManager in form type_Company
     *
     * @param context The Matrix Context object
     * @param args Packed programMap
     * @returns String the comma separated names of Resource Managers
     * @throws MatrixException if context is null / objectId parameter is invalid / the operation fails
     * @since PRG R208
     */
    public String getFieldResourceManagersData (Context context, String[] args) throws MatrixException
    {

    	try
    	{
    		// Check method arguments
    		if (context == null)
    		{
    			throw new IllegalArgumentException("context");
    		}

    		Map programMap = (Map) JPO.unpackArgs(args);
    		Map relBusObjPageList = (Map) programMap.get("paramMap");
    		String strObjectId = (String)relBusObjPageList.get("objectId");

    		if ( !(strObjectId != null && !"".equals(strObjectId) && !"null".equals(strObjectId)) )
    		{
    			throw new IllegalArgumentException(ComponentsUtil.i18nStringNow("emxComponents.Organisation.ArgsCannotFindObjectIdInProgramMap", context.getLocale().getLanguage()));
    		}

    		this.setId(strObjectId);

    		MapList mlResourceManagers = this.getResourceManagers(context, new String[] {DomainObject.SELECT_NAME});

    		Map mapRelatedObjectInfo = null;
    		String strResourceManager = null;
    		String strResourceManagerId = null;
    		StringList slResourceManagers = new StringList();
    		for (Iterator itrRelatedObjects = mlResourceManagers.iterator(); itrRelatedObjects .hasNext();)
    		{
    			mapRelatedObjectInfo = (Map) itrRelatedObjects.next();

    			strResourceManager = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_NAME);
    			strResourceManagerId = PersonUtil.getPersonObjectID(context, strResourceManager);
    			strResourceManager = PersonUtil.getFullName(context, strResourceManager);

    			if (strResourceManager != null && !"".equals(strResourceManager) && !"null".equals(strResourceManager))
    			{
    				slResourceManagers.add(strResourceManager);
    			}

    		}

    		return FrameworkUtil.join(slResourceManagers, ";");
    	}
    	catch (IllegalArgumentException iaexp)
    	{
    		iaexp.printStackTrace();
    		throw new MatrixException(iaexp);
    	}
    	catch (Exception exp)
    	{
    		exp.printStackTrace();
    		throw new MatrixException(exp);
    	}
    }


    /**
     *  This Method will Assign (Connect) Selected Resource Managers to Business Units, it will also disconnect the resource managers already connected which are not selected
     *
     * @param context Matrix Context Object
     * @param slResourceManagerIDs StringList of Person IDs
     * @throws Exception
     * @since PRG R208
     */
    public void assignResourceManagers (Context context, StringList  slResourceManagerIDs) throws Exception
    {
        if (context == null)
        {
            throw new MatrixException("Null context");
        }

		this.assignResourceManagers (context, (String[]) slResourceManagerIDs.toArray(new String[slResourceManagerIDs.size()]));
    }


    /**
     * Access method to show Resource Manager filed on Company /Business Unit properties webform
     *
     * @param context
     * @param args
     * @return boolean value
     * @throws Exception if the operation fails
     * @since PRG R208
     */
    public boolean isPMCInstalled(Context context, String[] args) throws Exception
    {
       return FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null);
    }
//End:R208:PRG Adv Resource Planning

    /**
     * When a Business unit, Department or Organization or any child object of Organization is deleted the Role associated
     * with that object gets deleted, if exists.
     * @param context The Matrix Context object
     * @param args comtains the name of role
     * @returns integer depending on failure or success
     * @throws Exception if context is null / the operation fails
     * @since BPS R208
     */
    public int deletePandORoleAction (Context context, String[] args) throws Exception{
    	try
        {
    		String strName = args[0];
    		ContextUtil.startTransaction(context,true);
    		//System.out.println("${CLASSNAME}.roleDeleteAction()strName="+strName);
    		String strMQL = "list role $1;";
            if (MqlUtil.mqlCommand(context, strMQL, true, strName).trim().length() > 0) {
           	 strMQL = "delete role $1;";
           	 MqlUtil.mqlCommand(context, strMQL, true, strName);
            }
            ContextUtil.commitTransaction(context);
        }
    	catch (Exception e){
    		ContextUtil.abortTransaction(context);
    		return 1;
    	}
    	return 0;
    }

    /**
     * Retuns Map having key and values required to show the Contry selection in JSPs
     * Key - Value
     * default - default value for Country attribute
     * valueList - Contry attribute range values
     * optionList - Country attribute option values (I18N strings for range values)
     * manualEntryList - option to show manual entry ("~~~AllowFreeFormEntry~~~")
     * @param context
     * @param language
     * @return
     * @throws Exception
     */
    public Map getCountryChooserDetailsForHTMLDisplay(Context context, String[] args) throws Exception
    {
        String language = (args == null || args.length != 1) ? context.getLocale().getLanguage() : args[0];
        String attrCountry  = PropertyUtil.getSchemaProperty(context, "attribute_Country");

        if(countryChooserDetails.isEmpty()) {
            AttributeType attributeType = new AttributeType(attrCountry);
            attributeType.open(context);
            StringList countryChoiceList = attributeType.getChoices();
            attributeType.close(context);

            List values = new StringList();
            for (Iterator iter = countryChoiceList.iterator(); iter.hasNext();) {
                String choice = (String) iter.next();
                values.add(choice);
            }

            countryChooserDetails.put("default", attributeType.getDefaultValue(context));
            countryChooserDetails.put("valueList", values);
            countryChooserDetails.put("manualEntryList", new StringList("~~~AllowFreeFormEntry~~~"));
        }

        Map details = new HashMap(countryChooserDetails);
        List options = new StringList();

        List values = (List) details.get("valueList");
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            options.add(UINavigatorUtil.getAttrRangeI18NString(attrCountry, (String) iter.next(), language));
        }
        details.put("optionList", options);
        return details;
    }

    /**
     * Method will be invoked by the delete Check trigger on department delete.
     * Method will check for MemberList owned by the Organization and delete the connected Organizations.
     *
     * @param context The Matrix Context object
     * @param args args[0] object Id of the deleted Organization
     * @return integer depending on failure or success
     * @throws Exception if context is null / the operation fails
     * @since BPS R209
     */
    public int deleteOwnedMemberListCheck(Context context, String[] args) throws Exception {
		try {
			StringList objectSelects = new StringList(1);
			objectSelects.addElement(SELECT_ID);

			DomainObject obj = new DomainObject(args[0]);
			MapList list = obj.getRelatedObjects(context, TYPE_MEMBER_LIST, RELATIONSHIP_MEMBER_LIST, objectSelects,
														null, false, true, (short) 1,
														null, null, 0);
			if(list.size() > 0){
				String objectIds[] = new String[list.size()];
				for (int i = 0; i < list.size(); i++) {
					objectIds[i] = (String) ((Map) list.get(i)).get(SELECT_ID);
				}
				DomainObject.deleteObjects(context, objectIds);
			}
		} catch (Exception ex) {
			throw ex;
		}
		return 0;
	}


//Added:02-July-2010:s4e:R210 PRG:AdvanceResourcePlanning
    /**
     * Method to display StandardCost currency values while creating and editing Company,BusinessUnit and Department for PMC
     *
     * @param context The Matrix Context object
     * @return map containing values for currency
     * @throws Exception if context is null / the operation fails
     * @since BPS R210
     */
    public Map getCurrencyRangeForStandardCost(Context context, String[] args) throws MatrixException {
    	try {
    		Map returnMap = new HashMap();
    		String queryOne = "print attribute $1 select $2 dump";

    		String dimensionName    = MqlUtil.mqlCommand(context,queryOne, PropertyUtil.getSchemaProperty(context, "attribute_StandardCost"), "Dimension");
    		String queryTwo         = "print Dimension $1 select $2 dump";
    		String queryThree       = "print Dimension $1 select $2 dump";

    		String units = MqlUtil.mqlCommand(context,queryTwo, dimensionName, "unit.label");
    		String unitsNames = MqlUtil.mqlCommand(context,queryThree, dimensionName, "unit.name");

    		StringList unitOptionsList = new StringList();
    		StringList unitValueList = new StringList();

    		unitValueList=FrameworkUtil.split(units, ",");
    		unitOptionsList=FrameworkUtil.split(unitsNames, ",");

    		returnMap.put("unitOptionsList", unitOptionsList);
    		returnMap.put("unitValueList", unitValueList);
    		return returnMap;
    	} catch (Exception ex) {
			throw new MatrixException(ex);
		}

	}
  //End:02-July-2010:s4e:R210 PRG:AdvanceResourcePlanning
    public boolean isParentCompanyAHostCompany(Context context, String strOrganizationId) throws FrameworkException {
        try {
            String strHostCompId = Company.getHostCompany(context);
            if(strHostCompId.equals(strOrganizationId)) {
                return true;
            }

            DomainObject busOrganization = new DomainObject(strOrganizationId);
            String sOrgTypeName = busOrganization.getInfo(context, SELECT_TYPE);
            boolean foundCompany = true;
            //TODO
            // If organizationId is plant type we don't have logic to check right now.
            while(!sOrgTypeName.equals(TYPE_COMPANY)) {
                String relType = sOrgTypeName.equals(TYPE_BUSINESS_UNIT) ? RELATIONSHIP_DIVISION :
                          sOrgTypeName.equals(TYPE_DEPARTMENT) ? RELATIONSHIP_COMPANY_DEPARTMENT : null;
                if(relType == null) {
                    foundCompany = false;
                    break;
                }
                busOrganization = new DomainObject(busOrganization.getInfo(context, "to[" + relType + "].from.id"));
                sOrgTypeName = busOrganization.getInfo(context, SELECT_TYPE);
            }

            if(!foundCompany)
                return false;

            MapList subsidiaries = busOrganization.getRelatedObjects(context,
                                                                     RELATIONSHIP_SUBSIDIARY,
                                                                     TYPE_COMPANY,
                                                                     new StringList(SELECT_ID),
                                                                     null,
                                                                     false,
                                                                     true,
                                                                     (short)0,
                                                                     null,
                                                                     null,
                                                                     0);
            for (int i = 0; i < subsidiaries.size(); i++) {
                Map subsidiary =  (Map) subsidiaries.get(i);
                if(subsidiary.get(SELECT_ID).equals(strOrganizationId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

	
	public int triggerCheckPromoteOrganizationState(Context context, String[] args) throws FrameworkException
    {
		String sType = args[0];
    	String TYPE_COMPANY = PropertyUtil.getSchemaProperty("type_Company");
 		if (sType != null && !"".equals(sType) && sType.equals(TYPE_COMPANY)){
 			boolean isOnPremise=FrameworkUtil.isOnPremise(context);	
 			return((isOnPremise)? 0:1);
         }else{
        	 return 0;
         }    	
    }
    /**
     * This method gets all the Formats that can be added to an Organization - Add Existing Formats functionality
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId and param values.
     * @throws Exception If the operation fails.
     * @since BPS R211
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getFormatstoAdd(Context context, String[] args) throws FrameworkException {
         try {
             HashMap paramMap = (HashMap)JPO.unpackArgs(args);
             String objectId  = (String) paramMap.get("objectId");

             SelectList busSelects = new SelectList(2);
             busSelects.add(SELECT_ID);
             busSelects.add(SELECT_NAME);

             SelectList relSelects = new SelectList(1);
             relSelects.add(SELECT_RELATIONSHIP_ID );

             Organization orgObj = new Organization(objectId);

             MapList formatObjMapList = DomainObject.findObjects(
                     context,            // eMatrix context
                     TYPE_FILE_FORMAT,  // type pattern
                     QUERY_WILDCARD,         // name pattern
                     QUERY_WILDCARD,     // revision pattern
                     QUERY_WILDCARD,     // owner pattern
                     QUERY_WILDCARD,     // vault pattern
                     "",        // where expression
                     false,               // expand types
                     busSelects);     // object selects

             MapList formatsConnectedList  = orgObj.expandSelect(context,
                     RELATIONSHIP_SUPPORTED_FILE_FORMAT,
                     TYPE_FILE_FORMAT,
                     busSelects,
                     relSelects,
                     false,
                     true,
                     (short)1,
                     null, null, null,
                     null, null, null,
                     false);

             String[] existingFormatIds = new String[formatsConnectedList.size()];
             for (int i = 0; i < existingFormatIds.length; i++) {
                 existingFormatIds[i] = (String)((Map)formatsConnectedList.get(i)).get(SELECT_ID);
             }
             Arrays.sort(existingFormatIds);

             int finalMapListSize = formatObjMapList.size() - formatsConnectedList.size();
             MapList formatFinalMapList = new MapList(finalMapListSize);
             if(finalMapListSize != 0) {
                 for (int i = 0; i < formatObjMapList.size(); i++) {
                     Map format = (Map)formatsConnectedList.get(i);
                     if(Arrays.binarySearch(existingFormatIds, (String)format.get(SELECT_ID)) < 0) {
                         formatFinalMapList.add(format);
                     }
                 }
             }
             return formatFinalMapList;
         } catch (Exception e) {
             throw new FrameworkException(e);
         }
     }

	 /**
   	  * Shows the Company Name as Root object in Select Business Unit, Department, Region, Owning Organization
	  * select windows.
	  * If objectId is there and it is of type Compnay it shows as Root element
	  * If it is not there then it will try to get the compnayId from objectId using
	  * ComponentsUtil.getCompanyIdForChildObjectId() method
	  * If still company id is null, it will show context users company name as root element
	  * If canSelectRootCompany is true it will enable the Root company for selection,
	  * 	if this value is null/false/empty it will be disabled.
	  */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getRootObjectForSelectOrganization(Context context, String[] args) throws FrameworkException {
         try {
             Map programMap = (Map) JPO.unpackArgs(args);
             String objectId = (String) programMap.get("objectId");
             String canSelectRootCompany = (String) programMap.get("canSelectRootCompany");
             String companyIds = null;
             if(!UIUtil.isNullOrEmpty(objectId)){
                 DomainObject contextObj = DomainObject.newInstance(context);
                 contextObj.setId(objectId);
                 companyIds = !TYPE_COMPANY.equals(contextObj.getInfo(context, SELECT_TYPE)) ?
                              ComponentsUtil.getCompanyIdForChildObjectId(context, objectId,false) : objectId;
             }
             companyIds = UIUtil.isNullOrEmpty(companyIds) ? Person.getPerson(context).getCompanyId(context) : companyIds;
             String cmpyIds[]=companyIds.split(",");
             MapList companies = new MapList();
             for(String companyId:cmpyIds){
             	Map companyMap = new HashMap();
             companyMap.put(SELECT_ID, companyId);
             if(!"true".equalsIgnoreCase(canSelectRootCompany))
                 companyMap.put("disableSelection", "true");
             companies.add(companyMap);
             }
             return companies;
         } catch (Exception e) {
             throw new FrameworkException(e);
         }
     }

	 /**
   	  * Expand program for select Owning Organization
	  * Returns Departments, Business Units, and Plants connected to the selected organization
	  */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList selectionOwningOrganizationExpandProgram(Context context, String[] args) throws FrameworkException {
         try {
             Map programMap = (Map) JPO.unpackArgs(args);

             MapList organizations = new MapList();

             StringList selectables = new StringList(2);
             selectables.add(SELECT_ID);

             String orgId = (String) programMap.get("objectId");
             Organization org = new Organization(orgId);
             Plant plant = new Plant();

             organizations.addAll(org.getBusinessUnits(context, 1, selectables, false));
             organizations.addAll(org.getDepartments(context, 1, selectables));
             organizations.addAll(plant.getPlants(context, "emxPlant", "getPlants", orgId, selectables, "on")) ;

             return organizations;
         } catch (Exception e) {
             throw new FrameworkException(e);
        }
     }


}
