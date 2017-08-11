/*
 *  emxRouteTemplateBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.BusinessUnit;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.RouteTemplate;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * @version Common 10-0-0-0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxRouteTemplateBase_mxJPO extends emxDomainObject_mxJPO
{

	static final String ROUTE_SCOPE = "attribute[Restrict Members]";
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     * @grade 0
     */
    public emxRouteTemplateBase_mxJPO (Context context, String[] args)
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
     * @since Common 10-0-0-0
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
     * showCheckbox - determines if the checkbox needs to be enabled in the column of the RouteTemplate Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     * @grade 0
     */
    public Vector showCheckbox(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector enableCheckbox = new Vector();
            String user = context.getUser();

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String owner = (String)objectMap.get(SELECT_OWNER);

                if(user.equals(owner))
                {
                    enableCheckbox.add("true");
                }
                else
                {
                    enableCheckbox.add("false");
                }
            }
            return enableCheckbox;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * getScope - displays the scope of the template
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10-0-0-0
     * @grade 0
     */
    public Vector getAvailability(Context context, String[] args)
            throws Exception {
// IR-047371V6R2011 - START
        Vector availabilityList = new Vector();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            HashMap paramMap   = (HashMap) programMap.get("paramList");
            String strLang     = (String) paramMap.get("languageStr");
            String strLabelUser       = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(strLang),"emxComponents.SearchTemplate.User");
            String strLabelEnterprise = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(strLang),"emxComponents.SearchTemplate.Enterprise");
            String strProjectElement  = "<a href='javascript:showModalDialog(\"../common/emxTree.jsp?objectId=${OBJECTID}\",575,575)'>${NAME}</a>";
            String strAvailability    = "";

            Iterator objectListItr    = objectList.iterator();
            while( objectListItr.hasNext() ) {
                Map objectMap           = (Map) objectListItr.next();
                String strConnectedId   = (String) objectMap.get( RouteTemplate.SELECT_ROUTE_TEMPLATES_OBJECT_ID );
                String strConnectedType = (String) objectMap.get( RouteTemplate.SELECT_ROUTE_TEMPLATES_TYPE );
                String strSuffix        = (String) objectMap.get( RouteTemplate.SELECT_OWNING_ORGANIZATION_NAME );
                
                /**	IR-268393V6R2014x - k3d
                 * RCA:	Latin characters are not getting encoded properly to be parsed by SAXBuilder in UIFormCommon:addFields()
                 * FIX:	changing encodeForHTML to encodeForXML as ProgramHTMLOutput field value is being parsed as XML
                 */
                if( strSuffix == null || "null".equals( strSuffix ) || "".equals( strSuffix ) ) {
                    strSuffix = XSSUtil.encodeForXML(context,(String) objectMap.get( "to["+ DomainObject.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name" ));
                }

                if( ( TYPE_PROJECT.equals( strConnectedType ) ) || ( TYPE_PROJECT_SPACE.equals( strConnectedType ) )
                                || mxType.isOfParentType( context, strConnectedType, DomainConstants.TYPE_PROJECT_SPACE ) ) {
					if(mxType.isOfParentType( context, strConnectedType, DomainConstants.TYPE_PROJECT_SPACE )){
                		strConnectedType = EnoviaResourceBundle.getTypeI18NString(context, DomainConstants.TYPE_PROJECT_SPACE, strLang);
                	}else{
                		strConnectedType = EnoviaResourceBundle.getTypeI18NString(context, strConnectedType, strLang);
                	}
                    strAvailability = FrameworkUtil.findAndReplace( strProjectElement, "${OBJECTID}", strConnectedId );
                    strAvailability = FrameworkUtil.findAndReplace( strAvailability, "${NAME}", XSSUtil.encodeForHTML(context, strConnectedType) + " : " + strSuffix );
                } else if( TYPE_PERSON.equals( strConnectedType ) ) {
                    strAvailability = strLabelUser + " : " + strSuffix;
                } else {
                    strAvailability = strLabelEnterprise + " : " + strSuffix;
                }
                availabilityList.add( strAvailability );
            }
        } catch (Exception ex) {
            throw ex;
        }
        return availabilityList;
    }
//  IR-047371V6R2011 - END



    /**
    * Gets the MapList containing all Route Templates connected to the BusinessUnit.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing all Route Templates connected to the BusinessUnit .
    * @throws Exception if the operation fails.
    * @since Common 10.0.0.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  Object getBusinessUnitRouteTemplates(matrix.db.Context context, String[] args) throws Exception
    {
       String sScope = "to[" + RELATIONSHIP_ROUTE_TEMPLATES + "].from.type";
       String WorkspaceId                    = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.id";
       String WorkspaceName                  = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name";
       String typeFilter                     = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.type";
      if (args.length == 0 ){
        throw new IllegalArgumentException();
      }
      String busWhere = "current=='Active'";
      SelectList objectSelects = new SelectList(1);
      SelectList relSelects = new SelectList(1);
      objectSelects.add(DomainConstants.SELECT_ID);
      objectSelects.add(RouteTemplate.SELECT_NAME);
      objectSelects.add(RouteTemplate.SELECT_DESCRIPTION);
      objectSelects.add(RouteTemplate.SELECT_REVISION);
      objectSelects.add(RouteTemplate.SELECT_OWNER);
      objectSelects.add(RouteTemplate.SELECT_ROUTE_TEMPLATES_TYPE);
      objectSelects.add(RouteTemplate.SELECT_TYPE);
      objectSelects.add(WorkspaceId);
      objectSelects.add(WorkspaceName);
      objectSelects.add(typeFilter);
      objectSelects.add(RouteTemplate.SELECT_RESTRICT_MEMBERS);

      relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId = (String)paramMap.get("objectId");
      MapList list = null;
      DomainObject obj = (DomainObject) DomainObject.newInstance(context);
      obj.setId(objectId);
      list = obj.getRelatedObjects(context,
                                    RouteTemplate.RELATIONSHIP_OWNING_ORGANIZATION,
                                    TYPE_ROUTE_TEMPLATE,
                                    objectSelects,
                                    relSelects,false,true,(short) 1,busWhere,null);
      return list;
    }

    /**
    * Gets the MapList containing all Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing all Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getAllRouteTemplates(Context context, String[] args) throws Exception
   {
        return getRouteTemplates(context,null);
   }
   /**
    * Gets the MapList containing all Active Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing all Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */

   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getAllSearchRouteTemplates(Context context, String[] args) throws Exception
   {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList routeTemplateList = new MapList();
        String typeFilter                     = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.type";
        
        String sName =(String)programMap.get("txtName");
        String sScope = (String)programMap.get("selScope");
        String templateName="";
        String availability="";
         String where = "(current == Active)";
         boolean checkName=false;
         boolean checkAvailability=false;
         MapList tempList = (MapList)getRouteTemplates(context,where);
         Pattern namePattern = null;
         if( (sName != null) && (!sName.equals("*"))) {
            namePattern = new Pattern(sName);
            checkName=true;
         } else {
            sName = "*";
            namePattern = new Pattern(sName);
            checkName=false;
         }
         Pattern availabilityPattern = null;
         if( (sScope != null) && (!sScope.equals("*"))) {
            if(sScope.equals("User")){
                sScope=DomainObject.TYPE_PERSON;
            }else if(sScope.equals("Enterprise")){
                sScope=DomainObject.TYPE_COMPANY;
            }else if(sScope.equals("Workspace")){
                sScope=DomainObject.TYPE_WORKSPACE;
            }
            availabilityPattern = new Pattern(sScope);
            checkAvailability=true;
         }else {
            sScope = "*";
            availabilityPattern = new Pattern(sScope);
            checkAvailability=false;
         }
         if( !checkName && !checkAvailability )
         {
        	 //return tempList;
        	 routeTemplateList.addAll(tempList);
         }else{
             Hashtable tempMap=null;
             if(tempList != null)
             {
                for(int i=0;i<tempList.size();i++)
                {
                    tempMap=(Hashtable)tempList.get(i);
                    templateName = (String)tempMap.get(DomainObject.SELECT_NAME);
                    availability=(String)tempMap.get(typeFilter);
                    if((checkName) && (checkAvailability))
                    {
                        if ( (namePattern.match(templateName)) && (availabilityPattern.match(availability) ) )
                        {
                            routeTemplateList.add(tempMap);
                        }
                    }else if(checkName)
                    {
                        if (namePattern.match(templateName))
                        {
                            routeTemplateList.add(tempMap);
                        }
                    }else if(checkAvailability)
                    {
                        if (availabilityPattern.match(availability))
                        {
                            routeTemplateList.add(tempMap);
                        }
                    }
                }
             }
        }
         
         String workspaceFilter = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.id";
         String workspaceId = (String)programMap.get("workspaceId");
         MapList routeTemplateListTemp = new MapList();
         if(workspaceId != null && workspaceId.trim().length() > 0){
        	 Hashtable tempMap=null;
        	 for(int i=0;i<routeTemplateList.size();i++)
        	 {
        		 tempMap=(Hashtable)routeTemplateList.get(i);
        		 String resultType = (String)tempMap.get(typeFilter);
        		 String resultWorkspaceId = (String)tempMap.get(workspaceFilter);
        		 if(DomainObject.TYPE_WORKSPACE.equalsIgnoreCase(resultType) )
        		 {
        			 if(workspaceId.equals(resultWorkspaceId)){
        				 routeTemplateListTemp.add(tempMap);
        			 }
        		 } else {
        			 routeTemplateListTemp.add(tempMap);
        		 }
        	 }
        	 return routeTemplateListTemp;
         } else {
        	 return routeTemplateList;
         }
   }

    /**
    * Gets the MapList containing Approval Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing Approval Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getApprovalRouteTemplates(Context context, String[] args) throws Exception
   {
        String SELECT_ROUTE_BASE_PURPOSE = getAttributeSelect(ATTRIBUTE_ROUTE_BASE_PURPOSE);
        String buswhere = SELECT_ROUTE_BASE_PURPOSE + " == Approval";

        return getRouteTemplates(context,buswhere);
   }

    /**
    * Gets the MapList containing Review Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing Review Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getReviewerRouteTemplates(Context context, String[] args) throws Exception
   {
        String SELECT_ROUTE_BASE_PURPOSE = getAttributeSelect(ATTRIBUTE_ROUTE_BASE_PURPOSE);
        String buswhere = SELECT_ROUTE_BASE_PURPOSE + " == Review";

        return getRouteTemplates(context,buswhere);
   }

    /**
    * Gets the MapList containing Standard Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing Standard Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getStandardRouteTemplates(Context context, String[] args) throws Exception
   {
        String SELECT_ROUTE_BASE_PURPOSE = getAttributeSelect(ATTRIBUTE_ROUTE_BASE_PURPOSE);
        String buswhere = SELECT_ROUTE_BASE_PURPOSE + " == Standard";

        return getRouteTemplates(context,buswhere);
   }


    /**
    * Gets the MapList containing all Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing all Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */
   @SuppressWarnings({ "static-access", "deprecation" })
   private Object getRouteTemplates(Context context, String objWhere) throws Exception
   {

       //commented for bug 352540
       //String sScope = "to[" + RELATIONSHIP_ROUTE_TEMPLATES + "].from.type"; 
        
        String WorkspaceId                    = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.id";
        String WorkspaceName                  = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name";
        String typeFilter                     = "to["+RouteTemplate.RELATIONSHIP_ROUTE_TEMPLATES+"].from.type";
       //Added for bug 352071
       String buFilter = RouteTemplate.SELECT_OWNING_ORGANIZATION_ID;
        // build select params
        if (objWhere != null)
        {
            //Added for bug 376335
            if(objWhere.indexOf("current == Active") != -1)
                objWhere=objWhere+" && latest==TRUE";
            //Ended
            else
                objWhere=objWhere+" && revision==last";
            
        }else{
            objWhere="revision==last";
        }


        SelectList selectStmts = new SelectList();


        selectStmts.add(RouteTemplate.SELECT_NAME);
        selectStmts.add(RouteTemplate.SELECT_DESCRIPTION);
        selectStmts.add(RouteTemplate.SELECT_REVISION);
        selectStmts.add(RouteTemplate.SELECT_OWNER);
        //commented for bug 352540
        //selectStmts.add(RouteTemplate.SELECT_ID);
        selectStmts.add(RouteTemplate.SELECT_ROUTE_TEMPLATES_TYPE);
       //Added SELECT_OWNING_ORGANIZATION_ID for bug 352071
       selectStmts.add(RouteTemplate.SELECT_OWNING_ORGANIZATION_ID);
        selectStmts.add(RouteTemplate.SELECT_TYPE);
        selectStmts.add(RouteTemplate.SELECT_ID);
        selectStmts.add(WorkspaceId);
        selectStmts.add(WorkspaceName);
        selectStmts.add(typeFilter);
        selectStmts.add(RouteTemplate.SELECT_RESTRICT_MEMBERS);
//  IR-047371V6R2011 - START
       selectStmts.add( RouteTemplate.SELECT_OWNING_ORGANIZATION_NAME );
//  IR-047371V6R2011 - END


        MapList templateMapList = new MapList();
        MapList templatePersonMapList = new MapList();
       MapList finalTemplateMap = new MapList();
       MapList tempMap = new MapList();
        try
        {
            String orgId = PersonUtil.getUserCompanyId(context);
            DomainObject templateObj = DomainObject.newInstance(context);
            Pattern OrgRelPattern = new Pattern(RELATIONSHIP_ROUTE_TEMPLATES);
            OrgRelPattern.addPattern(DomainConstants.RELATIONSHIP_DIVISION);
            OrgRelPattern.addPattern(RELATIONSHIP_COMPANY_DEPARTMENT );
            Pattern OrgTypePattern = new Pattern(TYPE_ROUTE_TEMPLATE);
            OrgTypePattern.addPattern(DomainConstants.TYPE_BUSINESS_UNIT);
            OrgTypePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
            Pattern includeOrgRelPattern = new Pattern(RELATIONSHIP_ROUTE_TEMPLATES);
            Pattern includeOrgTypePattern = new Pattern(TYPE_ROUTE_TEMPLATE);

            //Get the enterprise level RouteTemplates
            if(orgId != null)
            {
                templateObj.setId(orgId);
                templateMapList = templateObj.getRelatedObjects(context,
                                                                OrgRelPattern.getPattern(),
                                                                OrgTypePattern.getPattern(),
                                                                selectStmts,
                                                                null,
                                                                false,//modified for bug 352540
                                                                true,
                                                                (short)0,
                                                                objWhere,
                                                                null,
                                                                includeOrgTypePattern,
                                                                includeOrgRelPattern,
                                                                null);

            }
            // TO retrieve templates Owned by Current User

     /*       Collection obPersonIds    = null;
            Iterator personItr        = null;
            BusinessObject objPerson = null;

            HashMap personHash = new HashMap();
            personHash.put("",PersonUtil.getPersonObjectID(context));
            obPersonIds = personHash.values();
            personItr = obPersonIds.iterator();

            while(personItr.hasNext())
            {
                objPerson = new BusinessObject((String)personItr.next());
                templateObj.setId(objPerson.getObjectId()); */  //commented for bug 352540

				templateObj.setId(PersonUtil.getPersonObjectID(context));

                ContextUtil.startTransaction(context,false);
                ExpansionIterator expIter = templateObj.getExpansionIterator(context,
                                                                            RELATIONSHIP_ROUTE_TEMPLATES,
                                                                            TYPE_ROUTE_TEMPLATE,
                                                                            selectStmts,
                                                                            new StringList(),
                                                                            false,
                                                                            true,
                                                                            (short)1,
                                                                            objWhere,
                                                                            null,
                                                                            (short)0,
                                                                            false,
                                                                            false,
                                                                            (short)100,
                                                                            false);

                templatePersonMapList =  FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
                expIter.close();
                ContextUtil.commitTransaction(context);

                for (int i=0;i<templatePersonMapList.size();i++)
                {
                	Map map = (Map)templatePersonMapList.get(i);
                	tempMap.add((String)map.get(RouteTemplate.SELECT_ID));
                    templateMapList.add(map);
                }
                    
            
            String  types=DomainConstants.TYPE_PROJECT;
            if(FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null))
            {
            	types += "," + DomainConstants.TYPE_PROJECT_SPACE;
            }
            typeFilter += ".kindof[" + types + "]" + " == TRUE && " + objWhere;
            MapList projectSpaceList = DomainObject.querySelect(context,
                    DomainConstants.TYPE_ROUTE_TEMPLATE,                // type pattern
                    DomainObject.QUERY_WILDCARD, // namePattern
                    DomainObject.QUERY_WILDCARD, // revPattern
                    DomainObject.QUERY_WILDCARD, // ownerPattern
                    DomainObject.QUERY_WILDCARD,                 // get the Person Company vault
                    typeFilter,              // where expression
                    true,                        // expandType
                    selectStmts,               // object selects
                    null,                        // cached list
                    true);
       	   Iterator tempMapItr = projectSpaceList.iterator();
       	   while(tempMapItr.hasNext()) {
                Map map = (Map)tempMapItr.next();
                if(!tempMap.contains((String)map.get(RouteTemplate.SELECT_ID))) {
                    templateMapList.add(map);
                    }
           }

			//get the BUs for the context person
			ArrayList buList = getBURelatedToPerson (context);
            Iterator templateMapListItr = templateMapList.iterator();
           String contextPerson = context.getUser();
           String perName = "";
           String busUnitId = "";
           String owner = "";
       		String type = "";
           
            Map templateMap;
            DomainObject busUnitBO;
            BusinessUnit businessUnit;
            while(templateMapListItr.hasNext()){

                templateMap = (Map)templateMapListItr.next();
                templateMap.remove("level");
               // Modified for bug 352071
                String buFilterQuery=buFilter;
               busUnitId = (String) templateMap.get(buFilterQuery);
               String buTypeOfCompany=PropertyUtil.getSchemaProperty(context,"type_BusinessUnit");
               String deptTypeOfCompany=PropertyUtil.getSchemaProperty(context,"type_Department");
                if(busUnitId != null && !"".equals(busUnitId))
                {
                    busUnitBO = DomainObject.newInstance(context, busUnitId);
                    type = (String) busUnitBO.getInfo(context,DomainConstants.SELECT_TYPE);
                }

                owner = (String) templateMap.get("owner");
                if (contextPerson.equals(owner)) {
                    finalTemplateMap.add(templateMap);
                }

               else if (buTypeOfCompany.equals(type)) {

				   	//Add the Template if the Context Person is part of the BU
					
				   	if (buList.contains(busUnitId)) {
						finalTemplateMap.add(templateMap);
					}
               }
                else if (deptTypeOfCompany.equals(type)) {

                        //Add the Template if the Context Person is part of the BU
                        
                        if (buList.contains(busUnitId)) {
                            finalTemplateMap.add(templateMap);
                        }
                   }
/*
                   businessUnit = new BusinessUnit(busUnitId);
                   StringList personNameList = new StringList();
                   personNameList.add(DomainObject.SELECT_NAME);
                   MapList personMapList = businessUnit.getPersons(context, personNameList,objWhere);
                   Iterator iterator = personMapList.iterator();
                   while (iterator.hasNext()) {
                       Map buPerson = (Map) iterator.next();
                       perName = (String) buPerson.get("name");
                        if(contextPerson.equalsIgnoreCase(perName))
                        {
                            finalTemplateMap.add(templateMap);
                            break;
                        }
                   }
*/

                else {
                   finalTemplateMap.add(templateMap);
               }

               //end of bug 352071

           }

       } catch (Exception e) {

       }

       return finalTemplateMap;

    }

public Vector getScope(Context context, String[] args) throws Exception {
    Vector vScope = new Vector();
    try {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        HashMap paramMap = (HashMap) programMap.get("paramList");
        String langStr = (String) paramMap.get("languageStr");

        Iterator objectListItr = objectList.iterator();
        while (objectListItr.hasNext()) {
            Map objectMap = (Map) objectListItr.next();
            String restrictMembers = (String) objectMap
                    .get(RouteTemplate.SELECT_RESTRICT_MEMBERS);
            if (restrictMembers.equalsIgnoreCase("All")) {
                restrictMembers = EnoviaResourceBundle.getProperty(context,
                		"emxComponentsStringResource", new Locale(langStr),"emxComponents.Common.All");
            } else if (restrictMembers.equalsIgnoreCase("Organization")) {
                restrictMembers = EnoviaResourceBundle.getProperty(context,
                		"emxComponentsStringResource", new Locale(langStr),"emxComponents.Common.Organization");
            }
            vScope.add(restrictMembers);
        }

    } catch (Exception ex) {
        throw ex;
    }
    return vScope;
}
   /**
    * Gets the MapList containing all Revision of the  Route Templates.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a MapList containing all Route Templates.
    * @throws Exception if the operation fails.
    * @since Common 10.0.1.1
    */

   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getRevisions(Context context, String[] args) throws Exception
   {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String routeTemplateId=(String)programMap.get("objectId");
        MapList routeTemplateList = new MapList();
        DomainObject templateObj = DomainObject.newInstance(context);
        templateObj.setId(routeTemplateId);
        MapList templateMapList =  null;
        StringList sList = new StringList();
        sList.addElement(templateObj.SELECT_ID);
        sList.addElement(templateObj.SELECT_NAME);
        sList.addElement(templateObj.SELECT_REVISION);
        sList.addElement(templateObj.SELECT_ORIGINATED);
        templateMapList = templateObj.getRevisions(context,sList,false);
        return templateMapList;
   }


public String getRouteTemplateAvailability(Context context, String[] args) throws Exception
{

        String routTempList = "";
        String strRouteTempId = args[0];
        DomainObject doRouteObject = new DomainObject(strRouteTempId);
        
        //Commented and modified for bug 359291
        //String strType = (String) doRouteObject.getInfo(context,SELECT_TYPE);
        String strType = args[1];

        if (mxType.isOfParentType(context,strType,DomainConstants.TYPE_ROUTE_TEMPLATE)) {
			SelectList busSelList = new SelectList(2);
			busSelList.add("to[" + DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES + "].from.name");
			busSelList.add("to[" + DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES + "].from.type");

			Map scopeMap = doRouteObject.getInfo(context, busSelList);
			String strScopeType = (String) scopeMap.get("to[" + DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES + "].from.type");
			String strScopeName = (String) scopeMap.get("to[" + DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES + "].from.name");
			if ((strScopeName != null)&&(!strScopeName.equals("")))
			{
			  if (mxType.isOfParentType(context,strScopeType,DomainConstants.TYPE_PERSON))
					 {
				routTempList = ((String) scopeMap.get("to[" + DomainConstants.RELATIONSHIP_ROUTE_TEMPLATES + "].from.name"));
				 } else {
					  routTempList = "Enterprise";
					}
			}
        }
        return routTempList;
   }
public ArrayList getBURelatedToPerson (Context context) throws Exception {

				ArrayList BUList = new ArrayList();
				SelectList selectStmts = new SelectList(1);
        		selectStmts.add("id");

				DomainObject personObj = DomainObject.newInstance(context);
                personObj.setId(com.matrixone.apps.common.Person.getPerson(context).getObjectId());
                Pattern typePattern = new Pattern(PropertyUtil.getSchemaProperty(context,"type_BusinessUnit"));
                typePattern.addPattern(DomainConstants.TYPE_BUSINESS_UNIT);
                typePattern.addPattern(DomainConstants.TYPE_DEPARTMENT);
                Pattern relPattern = new Pattern(PropertyUtil.getSchemaProperty(context,"relationship_BusinessUnitEmployee"));
                relPattern.addPattern(DomainConstants.RELATIONSHIP_BUSINESS_UNIT_EMPLOYEE);
                relPattern.addPattern(RELATIONSHIP_MEMBER );
				String objWhere  = "";

                MapList BUMapList = personObj.getRelatedObjects(context,
                                relPattern.getPattern(),
                                typePattern.getPattern(),
                                selectStmts,
                                null,
                                true,
                                true,
                                (short)1,
                                objWhere,
                                "",
                                null,
                                null,
                                null);

				Iterator BUListItr = BUMapList.iterator();
				String busUnitId = "";
				Map BUMap = null;
				while(BUListItr.hasNext()){
					BUMap = (Map)BUListItr.next();
					busUnitId = (String)BUMap.get("id");
					if (busUnitId!=null && !busUnitId.equals("")) {
						BUList.add(busUnitId);
					}
				}

				return BUList;
}

/**
 * gets route template's scope name and id(only for WS and WS vault, Project space scopes) based on its availability
 * @param context
 * @param args - route template id as String
 * @return HashMap with values for scopeName, scopeID (only for Workspace and Workspace vault)
 * @throws Exception
 */

public HashMap getRouteTemplateScopeInfo(Context context, String[] args) throws Exception{
	HashMap routeTemplateScopeMap = new HashMap();
	StringList routeTemplateSelects = new StringList(1);
	String strRouteTemplateId = (String) JPO.unpackArgs(args);
	RouteTemplate routeTemplateObject = (RouteTemplate) DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE_TEMPLATE);
	routeTemplateObject.setId(strRouteTemplateId);
    routeTemplateSelects.add(routeTemplateObject.SELECT_ROUTE_TEMPLATES_TYPE);
    routeTemplateSelects.add("to["+routeTemplateObject.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name");
    routeTemplateSelects.add("to[" + routeTemplateObject.RELATIONSHIP_ROUTE_TEMPLATES + "].from.id");
    routeTemplateSelects.add("attribute[" + routeTemplateObject.ATTRIBUTE_RESTRICT_MEMBERS + "]");
    Map routeTemplateInfo = routeTemplateObject.getInfo(context, routeTemplateSelects);
    String scopeType = (String)routeTemplateInfo.get(routeTemplateObject.SELECT_ROUTE_TEMPLATES_TYPE);
    String sAvailabilityName      = (String)routeTemplateInfo.get("to[" + routeTemplateObject.RELATIONSHIP_ROUTE_TEMPLATES + "].from.name");
    String sAvailabilityId      = (String)routeTemplateInfo.get("to[" + routeTemplateObject.RELATIONSHIP_ROUTE_TEMPLATES + "].from.id");
    routeTemplateScopeMap.put("scopeType", scopeType);
    if(scopeType.equals(DomainConstants.TYPE_PERSON) || scopeType.equals(DomainConstants.TYPE_DEPARTMENT) || 
    		scopeType.equals(DomainConstants.TYPE_ORGANIZATION) || scopeType.equals(DomainConstants.TYPE_COMPANY) || 
    		scopeType.equals(DomainConstants.TYPE_BUSINESS_UNIT)){
        String restrictMembers = (String) routeTemplateInfo.get("attribute[" + routeTemplateObject.ATTRIBUTE_RESTRICT_MEMBERS + "]");
        routeTemplateScopeMap.put("scopeName", restrictMembers);
        
    }else{
    	routeTemplateScopeMap.put("scopeName", sAvailabilityName);
    	routeTemplateScopeMap.put("scopeID", sAvailabilityId);
    }
	return routeTemplateScopeMap;
}

public String showRouteTemplateAvailability(Context context, String[] args) throws Exception
{
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    Map requestMap             = (Map) programMap.get("requestMap");
    String strLanguage         = (String)requestMap.get("languageStr");
    StringBuffer sb            = new StringBuffer();
    i18nNow i18nnow            = new i18nNow();
    String routeTemplateId     = (String) requestMap.get("objectId");
    String mode                = (String) requestMap.get("mode");
    RouteTemplate boProject    = (RouteTemplate)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE_TEMPLATE);
    StringList routeSelects    = new StringList(4);
    boProject.setId(routeTemplateId);
    routeSelects.add(boProject.SELECT_ROUTE_TEMPLATES_TYPE);
    routeSelects.add("to["+boProject.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name");
    routeSelects.add("to["+boProject.RELATIONSHIP_OWNING_ORGANIZATION+"].from.name");
    routeSelects.addElement("to[" + boProject.RELATIONSHIP_ROUTE_TEMPLATES + "].from.id");
    Map routeMap = boProject.getInfo(context,routeSelects);
    String connectedType = (String)routeMap.get(boProject.SELECT_ROUTE_TEMPLATES_TYPE);
    String connectedName =  (String)routeMap.get("to["+boProject.RELATIONSHIP_OWNING_ORGANIZATION+"].from.name");
    String sAvailabilityName      = (String)routeMap.get("to[" + boProject.RELATIONSHIP_ROUTE_TEMPLATES + "].from.name");
    String sAvailabilityId      = (String)routeMap.get("to[" + boProject.RELATIONSHIP_ROUTE_TEMPLATES + "].from.id");
    String sAvailability = "";
    String sChecked="";
    String sLabel=EnoviaResourceBundle.getProperty(context,"Components","emxComponents.CreateRoute.SelectScope",strLanguage);
    String strSelectScope      = sLabel;
    boolean hasEnterpriseAccess = false;
    com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
    if(person.hasRole(context,DomainObject.ROLE_ORGANIZATION_MANAGER) || person.hasRole(context,DomainObject.ROLE_COMPANY_REPRESENTATIVE))
    {
      hasEnterpriseAccess = true;
    }
    if(connectedType.equals(boProject.TYPE_PERSON))
    {
         sChecked="";
    }else if(connectedType.equals(boProject.TYPE_DEPARTMENT) || connectedType.equals(boProject.TYPE_ORGANIZATION) || connectedType.equals(boProject.TYPE_COMPANY) || connectedType.equals(boProject.TYPE_BUSINESS_UNIT)){

         sChecked="";
    }
    else 
    {
         sChecked="checked";
         sLabel=sAvailabilityName;
    }
    if(connectedName == null)
    {
      connectedName = (String)routeMap.get("to["+boProject.RELATIONSHIP_ROUTE_TEMPLATES+"].from.name");
    }
    if(mode.equals("view"))
    {
    if(connectedType.equals(boProject.TYPE_PERSON))
    {
        sAvailability = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.SearchTemplate.User",strLanguage);
        connectedName = PersonUtil.getFullName(context,connectedName);
    }
    else if(connectedType.equals(boProject.TYPE_PROJECT_SPACE)   || mxType.isOfParentType(context,connectedType,DomainConstants.TYPE_PROJECT_SPACE) ){
    	 sAvailability =EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.Projectspace",strLanguage); 	
   	}
    	else if(connectedType.equals(boProject.TYPE_WORKSPACE)) //Modified to handle Sub Type
    {
         sAvailability =EnoviaResourceBundle.getProperty(context,"Components","emxComponents.SearchTemplate.Workspace",strLanguage); 
     }else{
         sAvailability =EnoviaResourceBundle.getProperty(context,"Components","emxComponents.SearchTemplate.Enterprise",strLanguage);
      }
    sb.append(sAvailability);
    sb.append(": ");
    sb.append(connectedName);
    }
    if(mode.equals("edit"))
    {
        sb.append("<input type=\"radio\" name=\"availability\" value=\"User\" ");
                sb.append(connectedType.equals(boProject.TYPE_PERSON)?"checked":"");
                    sb.append(" onClick=\"routeTemplateEditScopeClearAll()\">");
        sb.append(EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.User",strLanguage));
        sb.append("<br>");
    if( hasEnterpriseAccess)
      {
        sb.append("<input type=\"radio\" name=\"availability\" value=\"Enterprise\" " );
                sb.append(connectedType.equals(boProject.TYPE_DEPARTMENT)||connectedType.equals(boProject.TYPE_COMPANY)||connectedType.equals(boProject.TYPE_BUSINESS_UNIT)||connectedType.equals(boProject.TYPE_ORGANIZATION) ?"checked":"");
        sb.append(" onClick=\"setRouteTemplateEditOrganization()\">");
        sb.append(EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.Enterprise",strLanguage));
        sb.append("<br>");
     }
    sb.append("<input type=\"radio\" name=\"availability\" value=\"Workspace\"" );
            sb.append(sChecked);
    sb.append(" onClick=\"setRouteTemplateEditAvailability()\">");
    sb.append("<input READONLY type = \"text\" name = \"txtWSFolder\" id = \"txtWSFolder\" readonly value = \"");
            sb.append(sLabel);
            sb.append("\" size = \"20\">");
            sb.append("<input type = \"hidden\" name = \"folderId\" value = \"");
                    sb.append(sAvailabilityId);
                    sb.append("\" >" );
            sb.append("<input type=button name = \"ellipseButton\" value=\"...\" onClick=showRouteTemplateEditWSChooser() ");
                    sb.append(!sLabel.equals(null)&& !sLabel.equals("null") && !sLabel.equals(strSelectScope) ? "":"disabled");
                    sb.append(" >");

    }
    return sb.toString();
}

@com.matrixone.apps.framework.ui.PostProcessCallable
public void routeTemplateEditProcess(Context context, String[] args) throws Exception
{
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    Map requestMap             = (Map) programMap.get("requestMap");
    Map paramMap               = (Map) programMap.get("paramMap");
    
    String strAutoStopOnRejection = (String)requestMap.get("AutoStopOnRejection");
    String sDescription           = (String)requestMap.get("Description");
    String organizationId         = (String)requestMap.get("organizationId");
    String ownerId                = (String)requestMap.get("OwnerOID");
    String scope                  = (String)requestMap.get("scope");
    String routeTemplateId        = (String)requestMap.get("objectId");
    String sOwner                 = (String)requestMap.get("Owner");
    String sAvailability          = (String)requestMap.get("availability");
    String sExternalAvailable     = (String)requestMap.get("txtWSFolder");//sWorkspaceName
    String sExternalAvailableId   = (String)requestMap.get("folderId");//sWorkspaceId
    String strRouteTaskEdit       = (String)requestMap.get("Route Task Edits");
    String sAttrRouteTaskEdit     = PropertyUtil.getSchemaProperty(context, "attribute_TaskEditSetting");
    String strOldRouteTaskEdits   = null;
    boolean errFlag               = false;
    final String ATTRIBUTE_AUTO_STOP_ON_REJECTION = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection" );
    if(strRouteTaskEdit == null){
        strRouteTaskEdit = "";
    }
    RouteTemplate template        = new RouteTemplate(routeTemplateId);
    if (routeTemplateId != null) {
        RouteTemplate routeTemplateObj = (RouteTemplate)DomainObject.newInstance(context , routeTemplateId );
        strOldRouteTaskEdits  = routeTemplateObj.getAttributeValue(context, sAttrRouteTaskEdit);
        if(strOldRouteTaskEdits == null){
            strOldRouteTaskEdits = "";
        }
        if( (sAvailability == null) || ("".equals(sAvailability)) )
        {
            DomainObject connectDO = DomainObject.newInstance(context);
            if( (sExternalAvailableId != null) && (!sExternalAvailableId.equals("")) )
            {
                connectDO.setId(sExternalAvailableId);
                sAvailability = connectDO.getInfo(context,connectDO.SELECT_TYPE);
            }
        }
        HashMap detailsMap = new HashMap();
        ContextUtil.startTransaction(context, true);
        if(organizationId == null || "null".equals(organizationId))
        {
            organizationId = "";
        }
        if(organizationId.trim().length() > 0)
        {
            String keyVal = "newId=";
            int i = organizationId.indexOf(keyVal);
            if(i > -1) {
                organizationId = organizationId.substring(i+keyVal.length(),organizationId.length());
            }
        }
        detailsMap.put("ownerId" , ownerId);
        detailsMap.put("availability" , sAvailability);
        detailsMap.put("workspaceName" , sExternalAvailable);
        detailsMap.put("workspaceId" , sExternalAvailableId);
        detailsMap.put("description", sDescription);
        detailsMap.put("organizationId" , organizationId); 
        
        String oldRelId     = (String)routeTemplateObj.getInfo(context,"to[" +routeTemplateObj.RELATIONSHIP_OWNING_ORGANIZATION + "].id");
        String oldScopeId     = (String)routeTemplateObj.getInfo(context,"to[" +routeTemplateObj.RELATIONSHIP_OWNING_ORGANIZATION + "].from.id");
        try{
            if(oldScopeId != null && !oldScopeId.equals(organizationId) && !organizationId.equals("")) {
                Organization organization=new Organization(organizationId);
                DomainRelationship.modifyFrom(context, oldRelId, organization);
                String oldRouteScopeId     = (String)routeTemplateObj.getInfo(context,"to[" +routeTemplateObj.RELATIONSHIP_ROUTE_TEMPLATES + "].from.id");
                detailsMap.put("organizationId" , oldRouteScopeId); 
            }
            
            Map mapAttributeValues = new HashMap();
            
            if(!strRouteTaskEdit.equals(strOldRouteTaskEdits)){
                mapAttributeValues.put(sAttrRouteTaskEdit, strRouteTaskEdit);
            }
            
            mapAttributeValues.put(routeTemplateObj.ATTRIBUTE_RESTRICT_MEMBERS, scope);
            
            if (strAutoStopOnRejection != null) {
                mapAttributeValues.put(ATTRIBUTE_AUTO_STOP_ON_REJECTION, strAutoStopOnRejection);
            }
            
            routeTemplateObj.setAttributeValues(context, mapAttributeValues);
            
            routeTemplateObj.editRouteTemplate(context , detailsMap);
            ContextUtil.commitTransaction(context);  
        }catch(Exception e)
        {
            ContextUtil.abortTransaction(context);  
            errFlag        = true;
        }
    }
}

public boolean showOwningOrganizationField(Context context, String[] args) throws Exception
{
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    String mode                = (String)programMap.get("mode");
    return mode.equals("edit")? true : false;
}

public String owningOrganizationProgramHTML(Context context, String[] args) throws Exception
{
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    Map requestMap             = (Map) programMap.get("requestMap");
    String routeTemplateId     = (String) requestMap.get("objectId");
    RouteTemplate boProject    = (RouteTemplate)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE_TEMPLATE);
    boProject.setId(routeTemplateId);
    SelectList selectStmts = new SelectList(7);
    selectStmts.addElement("to[" + boProject.RELATIONSHIP_OWNING_ORGANIZATION + "].from.name");
    selectStmts.addElement("to[" + boProject.RELATIONSHIP_OWNING_ORGANIZATION + "].from.id");
    Map resultMap = boProject.getInfo(context, selectStmts);
    String sOwningOrganization = (String)resultMap.get("to["+boProject.RELATIONSHIP_OWNING_ORGANIZATION+"].from.name");
    String sOwningOrganizationId = (String)resultMap.get("to["+boProject.RELATIONSHIP_OWNING_ORGANIZATION+"].from.id");
    StringBuffer sb = new StringBuffer();
    if(sOwningOrganization==null)
    {
    sOwningOrganization=""; 
    }
    sb.append("<input type=text name=\"organization\" value=\"").append(sOwningOrganization).append("\" size=\"20\" readonly>");
    sb.append("<input type=\"button\" name=\"selectOrganization\" value=\"...\"  onclick=\"javascript:showRTEditOrganizationChooser()\" ").append(!sOwningOrganization.equals(null)&& !sOwningOrganization.equals("null") && !sOwningOrganization.equals("") ? "":"disabled").append(">");
    sb.append("<input type=hidden name=\"organizationId\" value=\"").append(sOwningOrganizationId).append("\" >");
    return sb.toString();
}

	/**
	 * Returns OIDs of 'Route Templates' based on User or Enterprise or Workspace/ProjectSpace Availability
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds input arguments.
	 * @return a StringList containing all OIDS of Route Templates.
	 * @throws Exception if the operation fails.
	 * @since BPS R211
	 */

	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getRouteTemplateIncludeIDs(Context context, String[] args) throws FrameworkException 
	{
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
        
			MapList routeTemplateList = (MapList)getRouteTemplates(context,null);   
			StringList ids = new StringList(routeTemplateList.size());
        
			for (int i = 0; i < routeTemplateList.size(); i++) {
				Map routeTemplate = (Map) routeTemplateList.get(i);
				ids.add((String)routeTemplate.get(SELECT_ID));
			}        
			return ids;
			
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}
     /**
     * Access Function for APPRouteTemplateEditDetails command.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectId - Object Id of the Route object.
     * @return boolean true or false values.
     * @throws Exception if the operation fails.
     * @since CommonComponents R211
     * */
    public boolean checksToEditRouteTemplate(Context context,String[] args) throws Exception {

           HashMap programMap         = (HashMap) JPO.unpackArgs(args);
           String objectId            = (String) programMap.get("objectId");

           return routetemplateEditAccessCheck(context, objectId);
    }    

    protected boolean routetemplateEditAccessCheck(Context context, String objectId) throws Exception {
        
        RouteTemplate boRouteTemplate = (RouteTemplate)DomainObject.newInstance(context,DomainObject.TYPE_ROUTE_TEMPLATE);
        boRouteTemplate.setId(objectId);
        
        StringList selectables = new StringList();
        selectables.add(SELECT_OWNER);
        String selectScopeObject = "to[" + RELATIONSHIP_ROUTE_TEMPLATES + "].from.type";
        selectables.add(selectScopeObject);
        
        DomainObject boObj = DomainObject.newInstance(context, objectId);
        Map objInfo = boObj.getInfo(context, selectables);
        
        String sOwner = (String)objInfo.get(SELECT_OWNER);
        
        if(sOwner.equals(context.getUser()))
            return true;
        
        String scopeType = (String) objInfo.get(selectScopeObject);
        Person personObj  =  Person.getPerson(context);
        if (TYPE_PROJECT.equals(scopeType)) {
        	String contextUserName = (String)context.getUser();
            String scopeId = boRouteTemplate.getInfo(context, "to[" + RELATIONSHIP_ROUTE_TEMPLATES + "].from.id");
            boolean hasFullAccess = DomainAccess.hasObjectOwnershipAccess(context, scopeId, null, contextUserName+ "_PRJ",  DomainAccess.COMMENT_MULTIPLE_OWNERSHIP, "Full");
            return hasFullAccess;

        }
        
        return personObj.isRepresentativeFor(context, personObj.getCompanyId(context));
        
    }
    /**
     * Access Function for APPRouteTemplateActivateDeactivate command.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     * objectId - Object Id of the Route object.
     * @return boolean true or false values.
     * @throws Exception if the operation fails.
     * @since CommonComponents R211
     * */
    public boolean showActivateDeactivateLink(Context context,String[] args) throws Exception {
        
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        String objectId            = (String) programMap.get("objectId");
        boolean result             =  false;
        
        RouteTemplate boRouteTemplate = (RouteTemplate)DomainObject.newInstance(context,DomainObject.TYPE_ROUTE_TEMPLATE);
        boRouteTemplate.setId(objectId);
        StringList accessSelects = new StringList();
        accessSelects.add("current.access[promote]");
        accessSelects.add("current.access[demote]");
        accessSelects.add(SELECT_OWNER);
        
        Map accessMap = boRouteTemplate.getInfo(context,accessSelects);
        String promoteAccess = (String)accessMap.get("current.access[promote]");
        String demoteAccess = (String)accessMap.get("current.access[demote]");
        
        if(("true".equalsIgnoreCase(promoteAccess) || "true".equalsIgnoreCase(demoteAccess)) && routetemplateEditAccessCheck(context, objectId)) {
            result=true;
        }
        return result;
    }
    
    
    protected Map getRangeValuesMap(StringList values, StringList displayValues, StringList selectedValues) {
        HashMap resultMap = new HashMap();
        resultMap.put("field_choices", values);
        resultMap.put("field_display_choices", displayValues);
        resultMap.put("field_value", selectedValues);
        return resultMap;
    }
    
    /**
     * Range Values for Availability field in Route Save As Template form
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public Map getSaveAsTemplateAvailabilityRange(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map)JPO.unpackArgs(args);
            Map paramMap   = (Map)programMap.get("paramMap");
            String sLanguage = (String) paramMap.get("languageStr");
            
            StringList values = new StringList(3);
            StringList range = new StringList(3);
            
            values.add("User");
            range.add(ComponentsUtil.i18nStringNow("emxComponents.SaveTemplateDialog.User", sLanguage));
            
            Person contextUser = Person.getPerson(context);
            if(contextUser.isRepresentativeFor(context, contextUser.getCompanyId(context))) {
                values.add("Enterprise");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.SaveTemplateDialog.Enterprise", sLanguage));
            }
            
            values.add("Workspace");
            range.add(ComponentsUtil.i18nStringNow("emxComponents.Common.WorkProjectspace", sLanguage));
            
            return  getRangeValuesMap(values, range, new StringList());           
            
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
        
    /**
     * Range Values for Scope field in Route Save As Template form
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    
        public Map getSaveAsTemplateScopeRange(Context context, String[] args) throws FrameworkException {

            try {
                Map programMap = (Map)JPO.unpackArgs(args);
                Map paramMap   = (Map)programMap.get("paramMap");
                Map requestMap = (Map)programMap.get("requestMap");
                
                String objectId = (String)requestMap.get("objectId");
                String sLanguage = (String) paramMap.get("languageStr");
                
                StringList values = new StringList(2);
                StringList range = new StringList(2);
                StringList selectedValues = new StringList(1);
                
                values.add("All");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.Common.All", sLanguage));
                
                values.add("Organization");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.Common.Organization", sLanguage));
                
                String selectedScope = getScopeForRouteTemplate(context, objectId);
                if("Organization".equals(selectedScope)){
                	selectedValues.add(ComponentsUtil.i18nStringNow("emxComponents.Common.Organization", sLanguage));
                }else{
                	//to check the radio button 'All' if scope is WS or All
                	selectedValues.add(ComponentsUtil.i18nStringNow("emxComponents.Common.All", sLanguage));
                }
               
                return  getRangeValuesMap(values, range, selectedValues);  
                
            } catch (Exception e) {
                throw new FrameworkException(e);
            }
                   
        }
        
        /**
         * Range Values for RouteTaskEdit field in Route Save As Template form
         * @param context
         * @param args
         * @return
         * @throws FrameworkException
         */
        
        public Map getSaveAsTemplateRouteTaskEditRange(Context context, String[] args) throws FrameworkException {

            try {
                Map programMap = (Map)JPO.unpackArgs(args);
                Map paramMap   = (Map)programMap.get("paramMap");
                String sLanguage = (String) paramMap.get("languageStr");
                
                StringList values = new StringList(4);
                StringList range = new StringList(4);
                
                values.add("Modify/Delete Task List");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.TaskEditSetting.ModifyDeleteTaskList", sLanguage));
                
                values.add("Extend Task List");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.TaskEditSetting.ExtendTaskList", sLanguage));
                
                values.add("Modify Task List");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.TaskEditSetting.ModifyTaskList", sLanguage));
                
                values.add("Maintain Exact Task List");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.TaskEditSetting.MaintainExactTaskList", sLanguage));
                
                return  getRangeValuesMap(values, range, new StringList());                 
            } catch (Exception e) {
                throw new FrameworkException(e);
            }
                   
        }
        
        /**
         * Range Values for RouteTaskEdit field in Route Save As Template form
         * @param context
         * @param args
         * @return
         * @throws FrameworkException
         */
        
        public Map getSaveAsTemplateSaveOptionsRange(Context context, String[] args) throws FrameworkException {

            try {
                Map programMap = (Map)JPO.unpackArgs(args);
                Map requestMap = (Map)programMap.get("requestMap");
                Map paramMap   = (Map)programMap.get("paramMap");
                String sLanguage = (String) paramMap.get("languageStr");
                
                Route route = new Route((String)requestMap.get("objectId"));
                RouteTemplate routeTemp = route.getRouteTemplate(context);
                    
                StringList values = new StringList(2);
                StringList range = new StringList(2);
                
                values.add("NewTemplate");
                range.add(ComponentsUtil.i18nStringNow("emxComponents.SaveTemplateDialog.SavenewTemp", sLanguage));
                
                if(routeTemp != null &&  "TRUE".equalsIgnoreCase(routeTemp.getInfo(context, "current.access[revise]"))) {
                    values.add("Revise");
                    range.add(ComponentsUtil.i18nStringNow("emxComponents.SaveTemplateDialog.ReviseTemp", sLanguage));
                }
                
                return  getRangeValuesMap(values, range, new StringList());                 
            } catch (Exception e) {
                throw new FrameworkException(e);
            }
        }
        
        /**
         * Range Values for Save As Template, Template Data selection
         * @param context
         * @param args
         * @return
         * @throws FrameworkException
         */
        public Map getSaveAsTemplateTemplateDataRange(Context context, String[] args) throws FrameworkException {

            try {
                Map programMap = (Map)JPO.unpackArgs(args);
                Map paramMap   = (Map)programMap.get("paramMap");
                String sLanguage = (String) paramMap.get("languageStr");
                
                return  getRangeValuesMap(new StringList("SaveTaskAssignees"), 
                        new StringList(ComponentsUtil.i18nStringNow("emxComponents.RouteTemplateSaveDialog.TaskAssignees", sLanguage)),new StringList());                 
            } catch (Exception e) {
                throw new FrameworkException(e);
            }
        } 
        
        /**
         * Create JPO for Save Route as Template.
         * @param context
         * @param args
         * @return
         * @throws FrameworkException
         */
		 @com.matrixone.apps.framework.ui.CreateProcessCallable
		 public Map saveRouteAsTemplate(Context context, String[] args) throws FrameworkException {
            try {
                ContextUtil.startTransaction(context, true);
                
                Map programMap = (Map)JPO.unpackArgs(args);
                Map requestValuesMap = (Map) programMap.get("RequestValuesMap");
                
                Map returnMap = new HashMap();
                
                String routeId              = ((String[])requestValuesMap.get("objectId"))[0];
                String strTemplateName      = ((String)programMap.get("Name"));
                String strTemplateDesc      = ((String[])requestValuesMap.get("Description"))[0];
                String strAvailability      = ((String[])requestValuesMap.get("Availability"))[0];
                String workspaceId          = ((String[])requestValuesMap.get("WorkspaceAvailableOID"))[0];
                String strScope             = ((String[])requestValuesMap.get("Scope"))[0];
                String strRouteTaskEdit     = ((String[])requestValuesMap.get("RouteTaskEdits"))[0];
                String strOption            = ((String[])requestValuesMap.get("SaveOptions"))[0];
                String[] tempdateDataArr       = (String[])requestValuesMap.get("TemplateData");
                List tempdateDataList = new ArrayList(tempdateDataArr == null ? 0 : tempdateDataArr.length);

                if(tempdateDataArr != null) {
                    for (int i = 0; i < tempdateDataArr.length; i++) {
                        tempdateDataList.add(tempdateDataArr[i]);
                    }
                }
               
                String languageStr          = (String) programMap.get("languageStr");
                
                if("Workspace".equals(strAvailability) && UIUtil.isNullOrEmpty(workspaceId)) {
                    returnMap.put("ErrorMessage", ComponentsUtil.i18nStringNow("emxComponents.SaveTemplateDialog.WorkspaceAlert", languageStr));
                    return returnMap;

                } 
                
                Route route = new Route(routeId);
                Person contextUser = Person.getPerson(context);
                Company company = contextUser.getCompany(context);
                
                RouteTemplate routeTempConnected = route.getRouteTemplate(context);
                
                String vault = new DomainObject().getDefaultVault(context, company);
                
                boolean isNewTemplate = "NewTemplate".equals(strOption);
                BusinessObject newOrRevisedRTObject = null;
                
                if(isNewTemplate) {
                    newOrRevisedRTObject = new BusinessObject(TYPE_ROUTE_TEMPLATE, strTemplateName, "1", vault);
                    if(newOrRevisedRTObject.exists(context)) {
                        returnMap.put("ErrorMessage", ComponentsUtil.i18nStringNow("emxComponents.RouteTemplateDialog.AlreadyExists", languageStr));
                        return returnMap;
                    }
                    newOrRevisedRTObject.create(context, POLICY_ROUTE_TEMPLATE);
                } else {
                    BusinessObject lastRev = routeTempConnected.getLastRevision(context);
                    lastRev.open(context);
                    newOrRevisedRTObject = lastRev.revise(context, lastRev.getNextSequence(context), vault);
                    lastRev.close(context);
                }
                
                // updating the attributes of Route Template object
                newOrRevisedRTObject.open(context);
                
                AttributeList routeAttrList = new AttributeList();
                
                Map mapRouteInfo = route.getAttributeMap(context);
                
                String ATTRIBUTE_AUTO_STOP_ON_REJECTION = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection");
                String strAutoStopOnRejection = (String)mapRouteInfo.get(ATTRIBUTE_AUTO_STOP_ON_REJECTION);
                String strRouteBasePurpose = (String)mapRouteInfo.get(ATTRIBUTE_ROUTE_BASE_PURPOSE);
                
                // Set value of Auto Stop On Rejection attribute from Route object to Route Template object
                if (!UIUtil.isNullOrEmpty(strAutoStopOnRejection)) {
                    routeAttrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_AUTO_STOP_ON_REJECTION), strAutoStopOnRejection));
                }
                
                // Bug 345546 Save Route Base Purpose value
                if (!UIUtil.isNullOrEmpty(strRouteBasePurpose)) {
                    routeAttrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_ROUTE_BASE_PURPOSE), strRouteBasePurpose));
                }
                
                routeAttrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_ORIGINATOR),context.getUser()));
                //Added for Designate Route Template Tasks as non Editable
                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_TASKEDIT_SETTING), strRouteTaskEdit));
                routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_RESTRICT_MEMBERS), strScope));
                newOrRevisedRTObject.setDescription(context, strTemplateDesc);
                newOrRevisedRTObject.setAttributes(context,routeAttrList);
                newOrRevisedRTObject.promote(context);
                newOrRevisedRTObject.update(context);
                
                // connecting the routetemplate object to the person/company
                DomainObject connectingObj = "User".equals(strAvailability) ? contextUser :
                    "Enterprise".equals(strAvailability) ? company : DomainObject.newInstance(context, workspaceId);
                
                connectingObj.connect(context, new RelationshipType(RELATIONSHIP_ROUTE_TEMPLATES), true, newOrRevisedRTObject);
                
                //Connect new or revised Route Temp to Route 
                if(routeTempConnected != null) {
                    // This pushContext is used to disconnect the Existing RouteTemplate of 
                    //Enterprise scope, for Which the user is not a owner.  
                    ContextUtil.pushContext(context);
                    routeTempConnected.disconnect(context, new RelationshipType(RELATIONSHIP_INITIATING_ROUTE_TEMPLATE), false, route);
                    ContextUtil.popContext(context);
                }
                newOrRevisedRTObject.connect(context,new RelationshipType(RELATIONSHIP_INITIATING_ROUTE_TEMPLATE), false, route);
                
                boolean saveTaskAssignees = tempdateDataList.contains("SaveTaskAssignees");
                String sAttParallelNodeProcessionRule  = PropertyUtil.getSchemaProperty(context,"attribute_ParallelNodeProcessionRule");
                String  sAttReviewTask               =  PropertyUtil.getSchemaProperty(context,"attribute_ReviewTask");
                
                // build Relationship and Type patterns
                Pattern relPersonPattern     = new Pattern(RELATIONSHIP_ROUTE_NODE);
                Pattern typePersonPattern    = new Pattern(TYPE_PERSON);
                typePersonPattern.addPattern(TYPE_ROUTE_TASK_USER);
                
                SelectList objSel = new SelectList();
                objSel.addId();
                objSel.addType();
                objSel.addName();
                
                // build select params for Relationship
                SelectList selectPersonRelStmts = new SelectList();
                selectPersonRelStmts.add(Route.SELECT_ROUTE_SEQUENCE);
                selectPersonRelStmts.add(Route.SELECT_ROUTE_ACTION);
                selectPersonRelStmts.add(Route.SELECT_ROUTE_INSTRUCTIONS);
                selectPersonRelStmts.add(Route.SELECT_SCHEDULED_COMPLETION_DATE);
                selectPersonRelStmts.add(Route.SELECT_TITLE);
                selectPersonRelStmts.add(Route.SELECT_ROUTE_TASK_USER);
                selectPersonRelStmts.add(Route.SELECT_ALLOW_DELEGATION);
                selectPersonRelStmts.addAttribute(ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
                selectPersonRelStmts.addAttribute(ATTRIBUTE_DUEDATE_OFFSET);
                selectPersonRelStmts.addAttribute(ATTRIBUTE_DATE_OFFSET_FROM);
                selectPersonRelStmts.addAttribute(sAttParallelNodeProcessionRule); 
                selectPersonRelStmts.addAttribute(sAttReviewTask);
                selectPersonRelStmts.addAttribute(DomainRelationship.SELECT_TO_ID);
                
                MapList routeNodeList = route.getRelatedObjects(context, relPersonPattern.getPattern(),typePersonPattern.getPattern(),
                        objSel, selectPersonRelStmts,
                        false, true,
                        (short)1,
                        EMPTY_STRING, EMPTY_STRING,
                        0);
                AccessUtil accessUtil = new AccessUtil();
                
                DomainObject routeNodeToConnect = null;
                if(!saveTaskAssignees) {
                    routeNodeToConnect = new DomainObject();
                    routeNodeToConnect.createObject(context, TYPE_ROUTE_TASK_USER, null, null, POLICY_ROUTE_TASK_USER, vault);
                }
                
                for (int i = 0; i < routeNodeList.size(); i++) {
                    Map routeNodeInfo = (Map) routeNodeList.get(i);
                    
                    if(saveTaskAssignees) {
                        routeNodeToConnect = new DomainObject((String) routeNodeInfo.get(SELECT_ID));
                        String rtu = PropertyUtil.getSchemaProperty(context, (String) routeNodeInfo.get(Route.SELECT_ROUTE_TASK_USER));
                        if(!UIUtil.isNullOrEmpty(rtu)) {
                            accessUtil.setAccess(rtu, AccessUtil.ROUTE_ACCESS_GRANTOR, accessUtil.getReadAccess());
                        } else if(routeNodeInfo.get(SELECT_TYPE).equals(TYPE_PERSON)){
                            String personName = (String) routeNodeInfo.get(SELECT_NAME);
                            AccessList accList = route.getAccessForGrantee(context, personName);
                            Access access = (Access) (accList != null && accList.get(0) != null ? accList.get(0) : null);
                            if(access != null) {
                                accessUtil.setAccess(personName,AccessUtil.ROUTE_ACCESS_GRANTOR,access); 
                            }
                        }
                    }
                    
                    DomainRelationship newRouteNodeRel = DomainRelationship.connect(context, new DomainObject(newOrRevisedRTObject), RELATIONSHIP_ROUTE_NODE, routeNodeToConnect);
                    Map newRouteNodeRelAttributes = new HashMap(15);
                    newRouteNodeRelAttributes.put(ATTRIBUTE_ROUTE_SEQUENCE, routeNodeInfo.get(Route.SELECT_ROUTE_SEQUENCE));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_TITLE, routeNodeInfo.get(Route.SELECT_TITLE));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_ROUTE_ACTION, routeNodeInfo.get(Route.SELECT_ROUTE_ACTION));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_ROUTE_INSTRUCTIONS, routeNodeInfo.get(Route.SELECT_ROUTE_INSTRUCTIONS));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_SCHEDULED_COMPLETION_DATE, routeNodeInfo.get(Route.SELECT_SCHEDULED_COMPLETION_DATE));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_ASSIGNEE_SET_DUEDATE, routeNodeInfo.get(getAttributeSelect(ATTRIBUTE_ASSIGNEE_SET_DUEDATE)));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_DUEDATE_OFFSET, routeNodeInfo.get(getAttributeSelect(ATTRIBUTE_DUEDATE_OFFSET)));                   
                    newRouteNodeRelAttributes.put(ATTRIBUTE_DATE_OFFSET_FROM, routeNodeInfo.get(getAttributeSelect(ATTRIBUTE_DATE_OFFSET_FROM)));                   
                    newRouteNodeRelAttributes.put(sAttParallelNodeProcessionRule, routeNodeInfo.get(getAttributeSelect(sAttParallelNodeProcessionRule)));
                    newRouteNodeRelAttributes.put(sAttReviewTask, routeNodeInfo.get(getAttributeSelect(sAttReviewTask)));
                    newRouteNodeRelAttributes.put(ATTRIBUTE_ALLOW_DELEGATION, routeNodeInfo.get(Route.SELECT_ALLOW_DELEGATION));
                    if(saveTaskAssignees)
                        newRouteNodeRelAttributes.put(ATTRIBUTE_ROUTE_TASK_USER, routeNodeInfo.get(Route.SELECT_ROUTE_TASK_USER));

                    newRouteNodeRel.setAttributeValues(context, newRouteNodeRelAttributes);
                }
                if(accessUtil.getAccessList().size() > 0)  {
                    String[] jpoArgs = new String[]{newOrRevisedRTObject.getObjectId(context)};
                    JPO.invoke(context, "emxWorkspaceConstants", jpoArgs, "grantAccess", JPO.packArgs(accessUtil.getAccessList()));
                }
                newOrRevisedRTObject.close(context);
                ContextUtil.commitTransaction(context);
                
                returnMap.put(SELECT_ID, newOrRevisedRTObject.getObjectId(context));
                return returnMap;
            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                throw new FrameworkException(e);
            } finally {
                
            }
        }
        
        
        
        /**
         * getRouteTemplateName - method to return the Route template name if already connected to Route
         * @param context the eMatrix <code>Context</code> object
         * @return String
         * @throws Exception if the operation fails
         * @since R214
         * @grade 0
         */ 
        public String getRouteTemplateName(Context context, String[] args) throws Exception
        {
               Map programMap = (Map)JPO.unpackArgs(args);
               Map requestMap = (Map)programMap.get("requestMap");
               String sLanguage = (String) programMap.get("languageStr");
               String objectId;
               String sRouteTemplateName = EMPTY_STRING;
               if(requestMap == null) {
            	   objectId = ((String)programMap.get("parentOID"));
               }  
               else{
            	   objectId = ((String)requestMap.get("objectId"));
               }
                
               DomainObject dob = new DomainObject(objectId);
               String strTempId = dob.getInfo(context,"from[Initiating Route Template].to.id");
               if(UIUtil.isNotNullAndNotEmpty(strTempId)) {
                       DomainObject boTemplate=DomainObject.newInstance(context,strTempId);
                       SelectList selectStmts = new SelectList();
                       selectStmts.add("name");
                       selectStmts.add("current.access[revise]");
                       boTemplate.open(context);
                       Map routeTemplateMap=boTemplate.getInfo(context, selectStmts);
                       boTemplate.close(context);
                       if("true".equalsIgnoreCase((String)routeTemplateMap.get("current.access[revise]"))){
                       		sRouteTemplateName = (String)routeTemplateMap.get("name");
                       }
                }
            return sRouteTemplateName;
        }
        

        
      public boolean isReviseRouteTemplate(Context context, String[] args) throws Exception{
        	 return (!UIUtil.isNullOrEmpty(getRouteTemplateName(context, args))) ? true : false;
      }
        
        
        
      public boolean isNotReviseRouteTemplate(Context context, String[] args) throws Exception{
              return(!isReviseRouteTemplate(context,args));
      }

      /*setRouteTemplateName - method to update the Route template name if already connected to Route
       * @param context the eMatrix <code>Context</code> object
       * @return String
       * @throws Exception if the operation fails
       * @since R214
       * @grade 0
       */  
      public Object setRouteTemplateName(Context context, String[] args)
        throws Exception
        {
        	
        	HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            String newRTId = (String) paramMap.get("New OID");
            String NewRTName = (String) paramMap.get("New Value");
           
            if(newRTId == null || "null".equals(newRTId))
            {
            	newRTId = "";
            }

            if (!newRTId.equals("")) {           
            DomainObject dob = DomainObject.newInstance(context, objectId);
            dob.setName(context, NewRTName);
            }
            return Boolean.valueOf(true);
        }
     
      /*getTemplateDescription - method to return the Route template description if already connected to Route
      * @param context the eMatrix <code>Context</code> object
      * @return String
      * @throws Exception if the operation fails
      * @since R214
      * @grade 0
      */  
      public String getRouteTemplateDescription(Context context, String[] args) throws Exception
      {
             Map programMap = (Map)JPO.unpackArgs(args);
             Map requestMap = (Map)programMap.get("requestMap");
             String objectId = ((String)requestMap.get("objectId"));
          	 
             DomainObject dob = new DomainObject(objectId);
             String sRouteTemplateDesc = "";
             String strTempId = dob.getInfo(context,"from[Initiating Route Template].to.id");
             if(!UIUtil.isNullOrEmpty(strTempId)) {
                     DomainObject boTemplate=DomainObject.newInstance(context,strTempId);
                     boTemplate.open(context);
                     String strRouteTempDesc=boTemplate.getInfo(context,boTemplate.SELECT_DESCRIPTION);
					 boTemplate.close(context);
                     sRouteTemplateDesc = strRouteTempDesc;
                }
             return sRouteTemplateDesc;
      }
     
      /*setRouteTemplateName - method to update the Route template Description if already connected to Route
       * @param context the eMatrix <code>Context</code> object
       * @return String
       * @throws Exception if the operation fails
       * @since R214
       * @grade 0
       */  
      public Object setRouteTemplateDescrition(Context context, String[] args)
      throws Exception
      {
      	
      	HashMap programMap = (HashMap) JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) programMap.get("paramMap");
          String objectId = (String) paramMap.get("objectId");
          String newRTId = (String) paramMap.get("New OID");
          String NewRTName = (String) paramMap.get("New Value");
         
          if(newRTId == null || "null".equals(newRTId))
          {
          	newRTId = "";
          }

          if (!newRTId.equals("")) {           
          DomainObject dob = DomainObject.newInstance(context, objectId);
          dob.setDescription(context, NewRTName);
          }
          return Boolean.valueOf(true);
      }

        /**
         * Post Process JPO for Route Node Task edit.
         * @param context
         * @param args
         * @return
         * @throws FrameworkException
         */
        
        public HashMap updateRouteNodeTaskForRouteTemplate(Context context, String[] args) throws FrameworkException {
            try {
                Map programMap = (Map)JPO.unpackArgs(args);
                Map paramMap   = (Map)programMap.get("paramMap");
                
                String objectId = (String) paramMap.get("objectId");
                String relId = (String) paramMap.get("relId");
                
                HashMap requestMap = (HashMap)programMap.get("requestMap");
                String newTaskAssignee    = (String)requestMap.get("Assignee");
                StringList newTaskAssigneeInfo = FrameworkUtil.split(newTaskAssignee, "#");
                
                String newAssigneeType = (String) newTaskAssigneeInfo.get(0);
                String newAssigneeID = (String) newTaskAssigneeInfo.get(1);
                String newAssigneeValue = (String) newTaskAssigneeInfo.get(2);
                boolean connectToRTU = !"person".equals(newAssigneeType);
                
                HashMap resultsMap = new HashMap();
                HashMap newValues = new HashMap(3);
                
                newValues.put(ATTRIBUTE_ROUTE_TASK_USER, connectToRTU ? newAssigneeValue : EMPTY_STRING);
                newValues.put(ATTRIBUTE_ROUTE_ACTION, (String) requestMap.get("Action"));
                newValues.put(ATTRIBUTE_ALLOW_DELEGATION, (String) requestMap.get("AllowDelegation"));
                newValues.put(ATTRIBUTE_ROUTE_INSTRUCTIONS, (String) requestMap.get("Instructions"));
                
                RouteTemplate routeTemplate = (RouteTemplate)DomainObject.newInstance(context, objectId, TYPE_ROUTE_TEMPLATE);
                if(routeTemplate.getInfo(context, SELECT_CURRENT).equals(STATE_ROUTE_TEMPLATE_ACTIVE)) {
                    newValues.put("Assignee", newAssigneeID);
                    routeTemplate.revise(context, relId, newValues);
                } else {
                    DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
                    
                    StringList selectables = new StringList(5);
                    selectables.add(DomainRelationship.SELECT_TO_ID);
                    selectables.add(Route.SELECT_ROUTE_TASK_USER);
                    
                    Map relValues = (Map) DomainRelationship.getInfo(context, new String[]{relId}, selectables).get(0);
                    DomainObject dmoAssignee = new DomainObject ((String) relValues.get(DomainRelationship.SELECT_TO_ID));
                    selectables.clear();
                    selectables.add(SELECT_TYPE);
                    selectables.add(SELECT_NAME);
                    selectables.add(SELECT_ID);
                    
                    Map mapAssigneeInfo = dmoAssignee.getInfo (context, selectables);
                    String routeNodeType = (String) mapAssigneeInfo.get(SELECT_TYPE);
                    
                    boolean isConnectedToRTU = routeNodeType.equals(TYPE_ROUTE_TASK_USER);
                    String currentAssignee = (String) (isConnectedToRTU ? relValues.get(Route.SELECT_ROUTE_TASK_USER) : mapAssigneeInfo.get(SELECT_ID));
                    
                    if(!currentAssignee.equals(newAssigneeValue)) {
                        DomainRelationship.setToObject(context, relId, new DomainObject(newAssigneeID));
                    }                   
                    
                    domRel.setAttributeValues(context, newValues);
                }           
                return resultsMap;
            } catch (Exception e) {
                throw new FrameworkException(e);
            }
        }        
        
        /**
    	 * This method fetch the selected Scope of the Route Template.
    	 * method returns the scope of the template if the route already has a template else scope of the route.
    	 * return 'All' if the scope is a Workspace
    	 * @param objectId - Object ID of the Route 
    	 * @throws Exception 
    	 */
    	private String getScopeForRouteTemplate(Context context, String objectId) throws Exception{
    		String routeTemplateScope = EMPTY_STRING;
    		
    		if(UIUtil.isNotNullAndNotEmpty(objectId)){
    			try {
    	               DomainObject doRoute = new DomainObject(objectId);
    	               String strTempId = doRoute.getInfo(context,"from[Initiating Route Template].to.id");
    	               if(UIUtil.isNotNullAndNotEmpty(strTempId)) {
    	                       DomainObject doTemplate=DomainObject.newInstance(context,strTempId);
    	                       routeTemplateScope = doTemplate.getInfo(context, ROUTE_SCOPE);
    	                }
    				if(UIUtil.isNotNullAndNotEmpty(routeTemplateScope)){
        				return routeTemplateScope;
    				}
    				else{
        				return doRoute.getInfo(context, ROUTE_SCOPE);
    				}

    			} catch (Exception e) {
    				
    				e.printStackTrace();
    			}

    		}
    		return EMPTY_STRING;
    	}
}
