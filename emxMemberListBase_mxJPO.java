/* emxMemberListBase
 ** Copyright (c) 2002-2016 Dassault Systemes.
 ** All Rights Reserved
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.util.StringList;
import matrix.util.StringResource;

import com.matrixone.apps.common.MemberList;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.FrameworkStringResource;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.util.XSSUtil;


/**
 * The <code>emxMemberListBase</code> class contains methods to provide
 * functionality related with Member List for Specifications.
 *
 * @version AEF Rossini - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxMemberListBase_mxJPO extends emxDomainObject_mxJPO
{
  /*
   * Type
   */
  /** type "Member List". */
  public static final String TYPE_MEMBERLIST = PropertyUtil.getSchemaProperty("type_MemberList");
  public static final String TYPE_REGION = PropertyUtil.getSchemaProperty("type_Region");
  public static final String TYPE_BUSINESSUNIT = PropertyUtil.getSchemaProperty("type_BusinessUnit");
  //X3: Added Plant
  public static final String TYPE_PLANT = PropertyUtil.getSchemaProperty("type_Plant");
  public static final String TYPE_PERSON = PropertyUtil.getSchemaProperty("type_Person");
  public static final String TYPE_COMPANY = PropertyUtil.getSchemaProperty("type_Company");
  public static final String TYPE_SPECIFICATIONOFFICE = PropertyUtil.getSchemaProperty("type_SpecificationOffice");
  /*
   * Relationships
   */
  /** Relationship "List Member" */
  public static final String RELATIONSHIP_LIST_MEMBER = PropertyUtil.getSchemaProperty("relationship_ListMember");
  /** select the relationship "Default Specification Office" to object name. */
  /** Relationship "Member List" */
  public static final String RELATIONSHIP_MEMBER_LIST = PropertyUtil.getSchemaProperty("relationship_MemberList");
  public static final String RELATIONSHIP_DISTRIBUTION_LIST = PropertyUtil.getSchemaProperty("relationship_DistributionList");

  protected static emxcommonPushPopShadowAgentBase_mxJPO ShadowAgent = null;


  public static String parentSpecOfficeObjId = "";


  /*
   * Policy
   */

  /** policy "Member List". */
  public static final String POLICY_MEMBERLIST =  PropertyUtil.getSchemaProperty("policy_MemberList");


  /**
   * constructor of the emxMemberListBase.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - MapList objectList
   * @returns Object of type MapList
   * @throws Exception if the operation fails
   * @since SpecificationCentral Rossini
   */

  public emxMemberListBase_mxJPO (Context context, String[] args)
  throws Exception
  {
    super(context,args);
  }


  /**
   * Main method of the emxMemberListBase.
   *
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - MapList objectList
   * @return int
   * @throws Exception if the operation fails
   * @since SpecificationCentral Rossini
   */

  public int mxMain(Context context, String[] args)
    throws Exception
  {

    return 0;
  }


  /**
   * Get the list of Members connected to a Member List.
   *
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - MapList objectList
   * @return MapList containing Member
   * @throws Exception if the operation fails
   * @since SpecificationCentral Rossini
   */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getMembers(Context context, String[] args)throws Exception{
    try{
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectID=(String)programMap.get("listId");
      //object for holding selectables for object
      StringList objSelects = new StringList();

      //add the properties for output.
      objSelects.add(DomainConstants.SELECT_ID);

      //similarly create a new string list for holding relationship selectables
      StringList relSelects = new StringList();
      short level = 1;
      DomainObject doObj = new DomainObject(objectID);
      MapList idList = doObj.getRelatedObjects(context, RELATIONSHIP_LIST_MEMBER,TYPE_PERSON+","+DomainConstants.TYPE_BUSINESS_GROUP, objSelects, relSelects, false, true, level, "", "");
      return idList;
    }catch (Exception e) {
      throw e;
    }
  }
  /**
   * Get the list of person connected to a Member List.
   *
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - MapList personList
   * @return MapList containing person
   * @throws Exception if the operation fails
   * @since R212
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getPersons(Context context, String[] args)throws Exception{
      try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectID=(String)programMap.get("listId");
        //object for holding selectables for object
        StringList objSelects = new StringList();

        //add the properties for output.
        objSelects.add(DomainConstants.SELECT_ID);

        //similarly create a new string list for holding relationship selectables
        StringList relSelects = new StringList();
        short level = 1;
        DomainObject doObj = new DomainObject(objectID);
        MapList idList = doObj.getRelatedObjects(context, RELATIONSHIP_LIST_MEMBER,TYPE_PERSON, objSelects, relSelects, false, true, level, "", "");
        return idList;
      }catch (Exception e) {
        throw e;
      }
    }
  /**
   * Get the list of groups connected to a Member List.
   *
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - MapList groupList
   * @return MapList containing groups
   * @throws Exception if the operation fails
   * @since R212
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getGroups(Context context, String[] args)throws Exception{
      try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectID=(String)programMap.get("listId");
        //object for holding selectables for object
        StringList objSelects = new StringList();

        //add the properties for output.
        objSelects.add(DomainConstants.SELECT_ID);
        objSelects.add(DomainConstants.SELECT_NAME);

        //similarly create a new string list for holding relationship selectables
        StringList relSelects = new StringList();
        short level = 1;
        DomainObject doObj = new DomainObject(objectID);
        MapList idList = doObj.getRelatedObjects(context, RELATIONSHIP_LIST_MEMBER,DomainConstants.TYPE_BUSINESS_GROUP, objSelects, relSelects, false, true, level, "", "");
        return idList;
      }catch (Exception e) {
        throw e;
      }
    }
  

  /**
   * Get the list of Member Lists
   *
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - MapList objectList
   * @returns  MapList containing Member lists.
   * @throws Exception if the operation fails
   * @since SpecificationCentral Rossini
   */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getAllDistributionLists(Context context,String args[]) throws Exception
  {
    try
    {

  
  // Get the list of Owned Lists
  MapList ownedList=(MapList)getOwnedDistributionLists(context,args);
  MapList coownedList=(MapList)getCoOwnedDistributionLists(context,args);

  MapList idList=new MapList();

  //idsVector is used for filtering the duplicate id's.
  Vector idsVector=new Vector();

  Iterator mapItr = ownedList.iterator();

  while (mapItr.hasNext())
  {
    Map map = (Map) mapItr.next();
    String id = (String)map.get(DomainConstants.SELECT_ID);
    idsVector.addElement(id);
    // add level so that sorting does not give a number format exception
    map.put(DomainConstants.SELECT_LEVEL, "1");
    idList.add(map);
  }

  Iterator coOwnedmapItr = coownedList.iterator();

  while (coOwnedmapItr.hasNext())
  {
    Map map = (Map) coOwnedmapItr.next();
    String id = (String)map.get(DomainConstants.SELECT_ID);
    if(! idsVector.contains(id) )
    {
      idsVector.addElement(id);
      idList.add(map);
    }
  }


  return idList;
}catch (Exception e) {
  throw e;
}
  }


/**
 * Get the list of Owned Member Lists.
 *
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - MapList objectList
 * @return MapList containing Member List.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getOwnedDistributionLists(Context context,String[] args) throws Exception
{
  try
  {
    MapList idList=new MapList();
    Vector idsVector=new Vector();
    String contextUser=context.getUser();

    //Add the selectables
    StringList objSelects=new StringList();
    objSelects.addElement(DomainConstants.SELECT_ID);

    //Construct the where clause
    String where= "owner.name=='"+contextUser+"'" ;

    DomainObject dObj=new DomainObject();

    //Call Find objects method to get the id's with select list and where clause
    MapList ownedDistList=dObj.findObjects(context,TYPE_MEMBERLIST,"*", "*", contextUser,  "*", where, true, objSelects);
    return ownedDistList;
  }catch (Exception e) {
    throw e;
  }
}



/**
 * Get the list of Co Owned Member Lists.
 *
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - MapList objectList
 * @return MapList containing Member List.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */

public static MapList getCoOwnedDistributionLists(Context context,String args[]) throws Exception
{
  try
  {

	
	
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList idList = new MapList();
    String personId = PersonUtil.getPersonObjectID(context);
    DomainObject person = new DomainObject(personId);


    StringList objSelects = new StringList();
    objSelects.add(DomainConstants.SELECT_ID);
    StringList relSelects = new StringList();
    short level = 0;
    

	String orgId = PersonUtil.getUserCompanyId(context);
	DomainObject company = new DomainObject(orgId);
	
	

  	
	//Call getRelated objects method to get the id's with select list
   // MapList personMap = person.getRelatedObjects(context, RELATIONSHIP_LIST_MEMBER, TYPE_MEMBERLIST,objSelects,relSelects,true,false,level,"","");
    //MapList personMap = company.getRelatedObjects(context, RELATIONSHIP_LIST_MEMBER, TYPE_MEMBERLIST,objSelects,relSelects,false,true,level,"","");
    MapList personMap = company.getRelatedObjects(context,
			                                 RELATIONSHIP_MEMBER_LIST, 
											 TYPE_MEMBERLIST,
											 objSelects,
											 relSelects,
											 false,
											 true,
											 level,"","");

	return personMap;
  }catch (Exception e) {
    throw e;
  }
}



/**
 * Get the Scopes.
 * Used as a Column method in the Table to get Scopes
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - MapList objectList
 * @return Vector containing Scope
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */

public static Vector getScopes(Context context, String[] args)throws Exception
{
  Vector scopesList = new Vector();
    Vector scopesList1 = new Vector();
    try{
  

      ShadowAgent = new emxcommonPushPopShadowAgentBase_mxJPO(context,null); 
      ShadowAgent.pushContext(context,null);

      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList relBusObjPageList = (MapList)programMap.get("objectList");
      HashMap paramMap = (HashMap)programMap.get("paramList");
      String language =  (String)paramMap.get("languageStr");
      String strPersonal="emxComponents.MemberList.Personal";
      String strEnterprise="emxComponents.MemberList.Enterprise";
      String strBundle=EnoviaResourceBundle.getProperty(context,"eServiceSuiteComponents.StringResourceFileId");
      String personal= EnoviaResourceBundle.getProperty(context,strBundle,new Locale(language),strPersonal);
      String enterprise= EnoviaResourceBundle.getProperty(context,strBundle,new Locale(language),strEnterprise);
  
  	int intSize = relBusObjPageList.size();
  	String[] strObjectIds = new String[intSize];
  	int counter = 0;
  	StringList strSelectList = new StringList();
  	String strName = "to["+RELATIONSHIP_MEMBER_LIST+"].from.name";
  	String strType = "to["+RELATIONSHIP_MEMBER_LIST+"].from.type";
  	strSelectList.add(strName);
  	strSelectList.add(strType);
  	for(int i = 0; i < relBusObjPageList.size(); i++)
  	  {
  		Map map = (Map) relBusObjPageList.get(i);
  		String objectId = (String)map.get(SELECT_ID);
  		strObjectIds[counter] = objectId;
  		counter++;
  	  }
  	MapList mapList = DomainObject.getInfo(context,strObjectIds,strSelectList);
  	Iterator iterator = mapList.iterator();
  	while(iterator.hasNext())
  	  {
  		Map map = (Map) iterator.next();
  		String type = (String)map.get(strType);
  		if(type.equals(TYPE_BUSINESSUNIT) || type.equals(TYPE_COMPANY) || type.equals(TYPE_DEPARTMENT) || type.equals(TYPE_PLANT)){
  			scopesList.addElement(enterprise);
  		}else if (type.equals(TYPE_PERSON)){
              scopesList.addElement(personal);
            }
  	  }
    }catch (Exception e) {
      throw e;
    }

    ShadowAgent.popContext(context,null);
	return scopesList;

}





/**
 * Get the list of Business Units.
 * Used as a Column method in the Table to get Business Units.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @return Vector containg Business Unit.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */


public static Vector getBusinessUnits(Context context, String[] args)throws Exception
{
  Vector businessUnitList = new Vector();
    	Vector vec = new Vector();
    try{
  
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList relBusObjPageList = (MapList)programMap.get("objectList");
      HashMap paramMap = (HashMap)programMap.get("paramList");
      StringList objSelects = new StringList();
      String exportFormat = (String)paramMap.get("exportFormat");
      String reportFormat = (String)paramMap.get("reportFormat");
      String retValue="";
  
  
      //add the properties for output.
      objSelects.add(SELECT_NAME);
      objSelects.add(SELECT_TYPE);
      objSelects.add(SELECT_ID);
      String fromObjectTypePattern = TYPE_BUSINESSUNIT+","+TYPE_PERSON+","+TYPE_COMPANY;
  	//similarly create a new string list for holding relationship selectables
      StringList relSelects = new StringList();
      short level = 1;
      Iterator memberListItr=relBusObjPageList.iterator();
  	int intSize = relBusObjPageList.size();
  	String[] strObjectIds = new String[intSize];
  	int counter = 0;
  	StringList strSelectList = new StringList();
  	String strName = "to["+RELATIONSHIP_MEMBER_LIST+"].from.name";
  	String strId = "to["+RELATIONSHIP_MEMBER_LIST+"].from.id";
  	String strType = "to["+RELATIONSHIP_MEMBER_LIST+"].from.type";
  	strSelectList.add(strName);
  	strSelectList.add(strId); 
  	strSelectList.add(strType);
  
  
  	for(int i = 0; i < relBusObjPageList.size(); i++)
  	  {
  		Map map = (Map)relBusObjPageList.get(i);
  		String objectId = (String)map.get(DomainConstants.SELECT_ID);
  		strObjectIds[counter] = objectId;
  		counter++;
  	  }
  	MapList mapList = DomainObject.getInfo(context,strObjectIds,strSelectList);
  	Iterator iterator = mapList.iterator();
  	String strretValue = "";
  	while(iterator.hasNext())
  	  {
  		Map map = (Map) iterator.next();
  		String name = (String)map.get(strName);
  		String id = (String)map.get(strId);
  		String type = (String)map.get(strType);
        
        
        if(type.equals(TYPE_PERSON)){
            
        strretValue=DomainConstants.EMPTY_STRING;
        
        } else{
  		  strretValue=name;
       }
        businessUnitList.addElement(strretValue);
      }
  /*
  	while(memberListItr.hasNext())
      {
        //get the memberlist object id
        String memberListId=(String)((Map)memberListItr.next()).get(DomainConstants.SELECT_ID);
        DomainObject doObj=new DomainObject(memberListId);
  
        // get the list of regions  connected to the memberlist
        MapList buIdList = doObj.getRelatedObjects(context, RELATIONSHIP_MEMBER_LIST,fromObjectTypePattern, objSelects, relSelects, true, false, level, "", "");
        if (buIdList == null || buIdList.size() == 0)
        {
          businessUnitList.addElement("");
        }
        Iterator buIdListItr=buIdList.iterator();
  
  	  while(buIdListItr.hasNext())
        {
          Map bu=(Map)buIdListItr.next();
          if(bu != null){
            String fromType = (String)bu.get(DomainConstants.SELECT_TYPE);
            if(fromType.equals(TYPE_BUSINESSUNIT) || fromType.equals(TYPE_COMPANY) ){
              String objId=(String)bu.get(SELECT_ID);
              if("CSV".equals(exportFormat) || "HTML".equals(reportFormat))
                retValue=(String)bu.get(DomainConstants.SELECT_NAME);
              else
                retValue="<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&relId=null&parentOID=null&jsTreeID=null&objectId="+objId+"', '700', '600', 'false', 'popup')\">"+(String)bu.get(DomainConstants.SELECT_NAME)+"</a>";
                businessUnitList.addElement(retValue);
            }else if (fromType.equals(TYPE_PERSON)){
              businessUnitList.addElement("");
            }
          }else{
            businessUnitList.addElement("");
          }
        }
  
      }
  */
    }catch (Exception e) {
      throw e;
    }
  return businessUnitList;

}


/**
 * Get the Tree Root Name.
 *
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @return String containing Root.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */

public  static String getRoot(Context context, String[] args)
  throws Exception
{
  HashMap programMap = (HashMap) JPO.unpackArgs(args);
  HashMap paramMap = (HashMap)programMap.get("paramMap");
  String id=(String)paramMap.get("objectId");
  BusinessObject bo=new BusinessObject(id);
  DomainObject  dob = new DomainObject(bo);
  String name=dob.getInfo(context,DomainConstants.SELECT_NAME);
  return name;
}


/**
 * Get the Scope.
 * Used as a Field method in the Webform to get Scopes
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - MapList objectList
 * @return String containing Scope.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */

public static String getScope(Context context, String[] args)throws Exception
{
  StringList scopesList = new StringList();
  String retVal="";

  try{

    HashMap programMap=(HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");
    HashMap requestMap = (HashMap)programMap.get("requestMap");
    String objectId=(String)paramMap.get("objectId");
    String mode=(String)requestMap.get("mode");
    String organizationName="";
    String organizationId="";



      String language =  (String)paramMap.get("languageStr");
      String strPersonal="emxComponents.MemberList.Personal";
        String strEnterprise="emxComponents.MemberList.Enterprise";
    String strBundle=EnoviaResourceBundle.getProperty(context,"eServiceSuiteComponents.StringResourceFileId");
    String personal= EnoviaResourceBundle.getProperty(context,strBundle,new Locale(language),strPersonal);
    String enterprise= EnoviaResourceBundle.getProperty(context,strBundle,new Locale(language),strEnterprise);





    String specOfficeId=(String)paramMap.get("specOfficeId");

    if (specOfficeId != null && specOfficeId.trim().length() > 0)
    {
      parentSpecOfficeObjId = specOfficeId;
    } else {
      specOfficeId = parentSpecOfficeObjId;
    }
    String typeName = "";
    if(specOfficeId != null && !specOfficeId.equals("")){
      DomainObject specOfficeObj = DomainObject.newInstance(context,specOfficeId);
      typeName = specOfficeObj.getInfo(context,DomainConstants.SELECT_TYPE);
    }


    StringList objSelects = new StringList();

    //add the properties for output.
    objSelects.add(DomainConstants.SELECT_NAME);
    objSelects.add(DomainConstants.SELECT_TYPE);
    //X3: Added Plant
	//Added Department for bug 362581
    String fromObjectTypePattern = TYPE_BUSINESSUNIT+","+TYPE_PERSON+","+TYPE_PLANT+","+TYPE_COMPANY+","+TYPE_DEPARTMENT;
    Vector personAssignments = PersonUtil.getAssignments(context);
  boolean isAdmin = ( personAssignments.contains(PropertyUtil.getSchemaProperty(context,"role_SpecificationOfficeManager") ) || 
           personAssignments.contains(DomainObject.ROLE_COMPANY_REPRESENTATIVE)|| 
        personAssignments.contains(DomainObject.ROLE_ORGANIZATION_MANAGER));




    //similarly create a new string list for holding relationship selectables
    StringList relSelects = new StringList();
    short level = 1;
    DomainObject doObj=new DomainObject(objectId);

    // get the list of regions  connected to the memberlist
    MapList scopeList = doObj.getRelatedObjects(context, RELATIONSHIP_MEMBER_LIST,fromObjectTypePattern, objSelects, relSelects, true, false, level, "", "");
    Iterator scopeListItr=scopeList.iterator();
    if (scopeList == null || scopeList.size() == 0)
    {
      scopesList.add("");
    }
    while(scopeListItr.hasNext())
    {

      Map scope=(Map)scopeListItr.next();
      if(scope != null)
      {
        String fromType = (String)scope.get(DomainConstants.SELECT_TYPE);

        if(typeName != null && typeName.equals(TYPE_SPECIFICATIONOFFICE) && isAdmin )
        {

          if(mode.equalsIgnoreCase("view"))
          {

            retVal=enterprise;
          }
          else{
            //Added by Ramesh May 23 value='Personal' onClick='javascript:disablePersonalRadio()'
            retVal = "<table cellspacing=5 border=0><tr><td><input type=radio name='rb' value='Personal' onClick='javascript:disablePersonalRadio()'>"+personal+"</td></tr><tr><td><input type=radio name='rb' value='Enterprise' checked >"+enterprise+"</td></tr></table>";


        }
    }else{
      //X3: Added Plant
	  //Added department for bug 362581
      if(fromType.equals(TYPE_BUSINESSUNIT) || fromType.equals(TYPE_COMPANY) || fromType.equals(TYPE_PLANT) || ( fromType.equals(TYPE_PERSON) && isAdmin) || fromType.equals(TYPE_DEPARTMENT) )
      {
        //X3: Added Plant
        if (fromType.equals(TYPE_BUSINESSUNIT) || fromType.equals(TYPE_COMPANY) || fromType.equals(TYPE_PLANT) || fromType.equals(TYPE_DEPARTMENT))
        {
          organizationName=(String)scope.get(DomainConstants.SELECT_NAME);
        }
        else
        {
          String personId=PersonUtil.getPersonObjectID(context);
          DomainObject personObject = DomainObject.newInstance(context,personId);
          StringList obSelects = new StringList();
          obSelects.add(DomainConstants.SELECT_NAME);
          obSelects.add(DomainConstants.SELECT_ID);
          Map companyList = personObject.getRelatedObject(context,DomainConstants.RELATIONSHIP_EMPLOYEE,false,obSelects,null);
          organizationName=(String)companyList.get(SELECT_NAME);
          organizationId=(String)companyList.get(SELECT_ID);

        }
        if(mode == null || mode.equalsIgnoreCase("view"))
        {
          if ( fromType.equals(TYPE_PERSON))
          {
            retVal=personal;
          }
          else
            retVal=enterprise;
        }
        else
        {
          retVal = "<table cellspacing=5 border=0><tr><td><input type=radio name='rb' value='Personal' onClick='javascript:disableOrganization()' ";
          if(fromType.equals(TYPE_PERSON) )
            retVal+=" checked >"+personal+"</td></tr>";
          else
            retVal += " >"+personal+"</td></tr>";
          retVal+="<tr><td><input type=radio name='rb' value='Enterprise' onClick='javascript:enableOrganization(\""+XSSUtil.encodeForJavaScript(context, organizationName)+"\",\""+XSSUtil.encodeForJavaScript(context, organizationId)+"\")'";
          //X3: Added Plant
		  //Added Department for Bug 362581
          if(fromType.equals(TYPE_BUSINESSUNIT) || fromType.equals(TYPE_COMPANY) ||fromType.equals(TYPE_PLANT) || fromType.equals(TYPE_DEPARTMENT))
            retVal+=" checked >"+enterprise+"</td></tr></table>";
          else
            retVal += ">"+enterprise+"</td></tr></table>";

        }
      }
      else if (fromType.equals(TYPE_PERSON) && !isAdmin)
      {

        if(mode != null && mode.equalsIgnoreCase("view"))
        {

          retVal=personal;
        }
        else
        {
          retVal = personal;
        }

      }
    }
  }


}


    }catch (Exception e) {
throw e;
    }
    //XSSOK
return retVal;
}


/**
 * This method is to update Business Unit during edit using web form.
 * @param context the eMatrix <code>Context</code> object
 * @param args  holds request parameters.
 * @throws exception if operation fails.
 * @since SpecificationCentral 10.0.0.0
 *
 */

public static Object setBusinessUnit(Context context, String args[]) throws Exception
{
  HashMap programMap = (HashMap)JPO.unpackArgs(args);
  HashMap requestMap = (HashMap)programMap.get("requestMap");
  HashMap paramMap = (HashMap)programMap.get("paramMap");
  String objectId = (String)paramMap.get("objectId");

  Vector personAssignments = PersonUtil.getAssignments(context);
  boolean isAdmin = ( personAssignments.contains(PropertyUtil.getSchemaProperty(context,"role_SpecificationOfficeManager") ) || 
           personAssignments.contains(DomainObject.ROLE_COMPANY_REPRESENTATIVE)|| 
        personAssignments.contains(DomainObject.ROLE_ORGANIZATION_MANAGER));

  DomainObject memberList = DomainObject.newInstance(context,objectId);
  String relId = memberList.getInfo(context, "to[" + RELATIONSHIP_MEMBER_LIST  + "].id");
  String OwningBusinessUnit = (String)paramMap.get("New OID");
  String OwningBusinessUnitOld = (String)paramMap.get("Old OID");
  try
  {
    if(OwningBusinessUnit != null && !OwningBusinessUnit.equals(OwningBusinessUnitOld) && isAdmin )
    {
      //First disconect with old bu
      DomainRelationship.disconnect(context,relId);
      //Connect with new BU
      DomainRelationship.connect(context,OwningBusinessUnit,RELATIONSHIP_MEMBER_LIST, objectId, false);

    }
  }catch(Exception e)
  {
    e.printStackTrace();

    throw e;
  }
  return null;
}


/**
 * Get the Organization.
 * Used as a Field method in the Webform to get Organization
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - MapList objectList
 * @return StringList containing Organization.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */

public static StringList getOrganization(Context context, String[] args)throws Exception
{
  StringList scopesList = new StringList();
  try{

    HashMap programMap=(HashMap)JPO.unpackArgs(args);

    HashMap paramMap = (HashMap)programMap.get("paramMap");
    String objectId=(String)paramMap.get("objectId");

    StringList objSelects = new StringList();

    //add the properties for output.
    objSelects.add(DomainConstants.SELECT_NAME);
    objSelects.add(DomainConstants.SELECT_TYPE);
    //X3: Added Plant
	//Added department for bug 362581
    String fromObjectTypePattern = TYPE_BUSINESSUNIT+","+TYPE_PERSON+","+TYPE_PLANT+","+TYPE_COMPANY+","+TYPE_DEPARTMENT;

    //similarly create a new string list for holding relationship selectables
    StringList relSelects = new StringList();
    short level = 1;
    DomainObject doObj=new DomainObject(objectId);

    // get the list of regions  connected to the memberlist
    MapList scopeList = doObj.getRelatedObjects(context, RELATIONSHIP_MEMBER_LIST,fromObjectTypePattern, objSelects, relSelects, true, false, level, "", "");

    Iterator scopeListItr=scopeList.iterator();
    if (scopeList == null || scopeList.size() == 0)
    {
      scopesList.add("");
    }
    while(scopeListItr.hasNext())
    {

      Map scope=(Map)scopeListItr.next();
      if(scope != null)
      {
        String fromType = (String)scope.get(DomainConstants.SELECT_TYPE);
        //X3: Added Plant
		//Added department for bug 362581
        if(fromType.equals(TYPE_BUSINESSUNIT) || fromType.equals(TYPE_COMPANY) || fromType.equals(TYPE_PLANT) || fromType.equals(TYPE_DEPARTMENT))
        {
          scopesList.add((String)scope.get(DomainConstants.SELECT_NAME));
        }
        else if (fromType.equals(TYPE_PERSON))
        {
          scopesList.add(" ");
        }

      }
      else
      {
        scopesList.add("");
      }
    }

  }catch (Exception e) {
    throw e;
  }
  return scopesList;
}

/**
 * This method is to update Scope during edit using web form.
 * @param context the eMatrix <code>Context</code> object
 * @param args  holds request parameters.
 * @throws exception if operation fails.
 * @since Specification Central 10.0.0.0
 *
 */

public static Object setScope(Context context, String args[]) throws Exception
{
  HashMap programMap = (HashMap)JPO.unpackArgs(args);
  HashMap requestMap = (HashMap)programMap.get("requestMap");
  HashMap paramMap = (HashMap)programMap.get("paramMap");
  String objectId = (String)paramMap.get("objectId");
  String scopeArray[]=(String[])requestMap.get("rb");
  String scope="";

  if(scopeArray != null )
    scope = scopeArray[0];



  DomainObject memberList = DomainObject.newInstance(context,objectId);

  String relId = memberList.getInfo(context, "to[" + RELATIONSHIP_MEMBER_LIST  + "].id");

  try
  {
    if(scope.equalsIgnoreCase("Personal") )
    {
      //First disconect Organization
      DomainRelationship.disconnect(context,relId);
      //Connect with Person
      DomainRelationship.connect(context,PersonUtil.getPersonObjectID(context),RELATIONSHIP_MEMBER_LIST,objectId, false);

    }
  }catch(Exception e)
  {
    e.printStackTrace();

    throw e;
  }
  return null;
}


     /**
      * Trigger to be executed to check demote
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args  holds request parameters.
      * @return 0 for true and 1 for fasle
      * @throws Exception if the operation fails
      * @since SpecificationCentral 10.0.0.0
      */
public int checkDemote(Context context, String args[]) throws Exception{
  int returnValue = 0;
  String memberListId = args[0];
  Locale loc = emxMailUtil_mxJPO.getLocale(context);
  if (memberListId == null || memberListId.equals("") )
  {
    throw (new IllegalArgumentException(StringResource.format(
      EnoviaResourceBundle.getFrameworkStringResourceProperty(context, FrameworkStringResource.Generic_NullParameterPassed, context.getLocale()),
      "className", "emxMemberList",
      "paramName", "memberListId",
      "methodName", "triggerAction")));
  }
  try{
    ContextUtil.startTransaction(context,true);
    DomainObject dObj = DomainObject.newInstance(context,memberListId);
    if (dObj.hasRelatedObjects(context,RELATIONSHIP_DISTRIBUTION_LIST,false))
    {
      returnValue = 1;
      String strMemberListDemote= "emxComponents.MemberList.MemberListDemote";
      String strBundle=EnoviaResourceBundle.getProperty(context,"eServiceSuiteComponents.StringResourceFileId");//eServiceSuiteComponents.UI.ResourceBundle");
      String strMessage= EnoviaResourceBundle.getProperty(context,strBundle,loc,strMemberListDemote);
      emxContextUtil_mxJPO.mqlNotice(context,strMessage);
      ContextUtil.abortTransaction(context);
    }
    else{
      returnValue = 0;
    }
  }catch(Exception exp)
  {
    returnValue = 1;
    ContextUtil.abortTransaction(context);
    throw exp;
  }
  return returnValue;
}


/**
 * Get the Organization of Member.
 * Used as a Field method in the Webform to get Organization
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectList MapList
 * @return String containing Organization of Member.
 * @throws Exception if the operation fails
 * @since SpecificationCentral Rossini
 */


public static String getMemberOrganization(Context context, String[] args)throws Exception
{


    HashMap programMap=(HashMap)JPO.unpackArgs(args);

    HashMap paramMap = (HashMap)programMap.get("paramMap");

    String relId = (String)paramMap.get("relId");

    String objectId=(String)paramMap.get("objectId");

    HashMap requestMap = (HashMap)programMap.get("requestMap");

    String mode = (String)requestMap.get("mode");



    Vector personAssignments = PersonUtil.getAssignments(context);
    boolean isAdmin = ( personAssignments.contains(PropertyUtil.getSchemaProperty(context,"role_SpecificationOfficeManager") ) || 
             personAssignments.contains(DomainObject.ROLE_COMPANY_REPRESENTATIVE)|| 
          personAssignments.contains(DomainObject.ROLE_ORGANIZATION_MANAGER));
    
    String retStr = "";
    StringList strLst = null;

    if(mode!=null && mode.equals("edit") && isAdmin)
    {

        String org = "";
        StringList orgLst = (StringList)getOrganization(context, args);



        if(orgLst.size() == 1)
        {
            org = (String)orgLst.elementAt(0);
        }
        else
        {
            for(int i=0; i<orgLst.size()-1; i++)
            {
                org += (String)orgLst.elementAt(i) + ", ";
            }
            org += (String)orgLst.elementAt(orgLst.size()-1);
        }

        retStr = "<input type='text' READONLY name='OrganizationDisplay' id='' value='" + org.trim() + "' onFocus='this.blur();'maxlength='' size='20'>";

        retStr += "<input type='hidden' name='Organization'  value='" + org.trim() + "'>";

        retStr += "<input type='hidden' name='OrganizationOID'  value=''>";
		//Modified for bug 362581
        retStr += "<input type='button' "
                + "value='...' onclick="
                + "\"javascript:showModalDialog("
                + "'../components/emxComponentsSelectOwningOrganizationDialogFS.jsp?"
                + "fieldName=OrganizationDisplay&"
                + "fieldNameDisplay=OrganizationDisplay&"
				+ "formPopup=true&"
				+ "fieldId=OrganizationOID&relId="
                + XSSUtil.encodeForJavaScript(context,relId) + "','100','100', true, 'MediumTall')\">&nbsp;";
    }
    else
    {
        retStr = "";
        strLst = (StringList)getOrganization(context, args);



        if(strLst.size() == 1)
        {
            retStr = (String)strLst.elementAt(0);
        }
        else
        {
            for(int i=0; i<strLst.size()-1; i++)
            {
                retStr += (String)strLst.elementAt(i) + ", ";
            }
            retStr += (String)strLst.elementAt(strLst.size()-1);
        }
    }

    return retStr;
}


  /**
   * create HRef for vault field of member list create form
   *
   * @return HashMap -  
   * @throws Exception if the operation fails
   * @since R212
   */

  public static String getVault(Context context, String[] args)throws Exception
  {
      String propertyFile = "emxComponents";
      String propertyKey ="eServiceSuiteComponents.VaultAwareness";
      String vaultAwareness = EnoviaResourceBundle.getProperty(context,propertyFile,context.getLocale(),propertyKey);
      String defaultVault = PersonUtil.getPersonObject(context).getVault();
      String retStr = "";
      if (vaultAwareness.equals("false")) {
          retStr = "<input type='text' READONLY='READONLY' name='Vault' id='' value='"+XSSUtil.encodeForHTMLAttribute(context,defaultVault)+"' onFocus='this.blur()' maxlength='' size='20'></input>";
          retStr += "<input type='button' "
              + "value='...' onclick="
              + "\"javascript:showChooser("
              + "'../components/emxComponentsSelectSearchVaultsDialogFS.jsp?"
              + "multiSelect=false&amp;"
              + "fieldName=Vault','null','null')\"></input>";
      }
      else{
          retStr = defaultVault + "<input type='hidden' name='Vault' value='"+XSSUtil.encodeForHTMLAttribute(context,defaultVault)+"'></input>";
      }
    
      return retStr;
  }
  
  /**
   *  * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
   *        0 - HashMap requestMap
   * post process for member list creation
   * @return Map - Contain objectId of created member list
   * @throws Exception if the operation fails
   * @since R212
   */
  @com.matrixone.apps.framework.ui.CreateProcessCallable
  public Map createMemberListProcess(Context context, String[] args) throws Exception
  { 
      HashMap requestMap         = (HashMap) JPO.unpackArgs(args);
      String languageStr = (String) requestMap.get("languageStr");
      HashMap returnMap = new HashMap(1);
      MemberList memberlist = (MemberList)DomainObject.newInstance(context,MemberList.TYPE_MEMBER_LIST);
      String name = (String) requestMap.get("Name");
      String autoNameCheck = (String) requestMap.get("autoNameCheck");
      String AutoNameSeries = (String) requestMap.get("AutoNameSeries");
      if(UIUtil.isNullOrEmpty(AutoNameSeries)){
    	  AutoNameSeries = "-";
      } 
      String vault = (String) requestMap.get("Vault");
      String revision = (String) requestMap.get("Revision");

      String memberListId ="";
      String OrganizationOID    = (String)requestMap.get("owningBUOID");
      
      String scope = (String)requestMap.get("Scope");
      
      if(UIUtil.isNullOrEmpty(revision)){
              revision = new Policy(MemberList.POLICY_MEMBER_LIST).getSequence(context);
      }
      BusinessObject memberListObject = new BusinessObject(MemberList.TYPE_MEMBER_LIST,name,revision,vault);
      boolean isExists = memberListObject.exists(context);
      if(isExists){
          returnMap.put("ErrorMessage", i18nNow.getTypeI18NString(MemberList.TYPE_MEMBER_LIST, languageStr) + " " + name + " " + revision + " " + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.AlreadyExists"));
      }else{
        if ("true".equalsIgnoreCase(autoNameCheck)|| UIUtil.isNullOrEmpty(name)){
            String typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", MemberList.TYPE_MEMBER_LIST, true);
            String policyAlias = FrameworkUtil.getAliasForAdmin(context, "policy", MemberList.POLICY_MEMBER_LIST, true);
            memberListId = FrameworkUtil.autoName(context, typeAlias, AutoNameSeries, policyAlias);
        }else{
                memberlist.createObject(context, MemberList.TYPE_MEMBER_LIST, name, revision, MemberList.POLICY_MEMBER_LIST, vault);
                memberListId = memberlist.getObjectId(context);
        }

        if(scope.equalsIgnoreCase("Personal") )
        {
          DomainRelationship.connect(context,PersonUtil.getPersonObjectID(context),RELATIONSHIP_MEMBER_LIST,memberListId, false);
        }
        if(scope.equalsIgnoreCase("Enterprise") )
        {
          DomainRelationship.connect(context, OrganizationOID,RELATIONSHIP_MEMBER_LIST,memberListId, false);
        }
        
        returnMap.put(SELECT_ID, memberListId);
      }
      return returnMap;
  }
  
  private StringList getExistingGroupList(Context context,String memberListId){
      try {
          HashMap programMap = new HashMap();
          programMap.put("listId",memberListId);
          MapList idList = getGroups(context,JPO.packArgs(programMap));
          StringList excludeList = new StringList();
          Iterator itr = idList.iterator();
          while(itr.hasNext()){
              excludeList.add( (String) ((Map)itr.next()).get(DomainConstants.SELECT_NAME));
          }
          return excludeList;
    }catch (Exception e) {
              e.printStackTrace();
    }
    return null;
  }
  /**
   * return the StringList of exclude OID.
   *
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - StringList excludeOIDList
   * @return StringList containing Object IDs
   * @throws Exception if the operation fails
   * @since R212
   */
  private StringList getExistingPersonList(Context context, String memberListId) throws Exception
  {
      try
      {
          HashMap programMap = new HashMap();
          programMap.put("listId",memberListId);
          MapList idList = getPersons(context,JPO.packArgs(programMap));
          StringList excludeList = new StringList();
          Iterator itr = idList.iterator();
          while(itr.hasNext()){
              excludeList.add( (String) ((Map)itr.next()).get(DomainConstants.SELECT_ID));
          }
          return excludeList;
      }catch(Exception ex){
          ex.printStackTrace();
      }
      return null;
  }
  /**
   * adding the members in member list
 * @throws Throwable 
   *
   * @since R212
   */
  public void addMembersToMemberList(Context context, String[] args) throws Throwable{
      Map requestMap         = (HashMap) JPO.unpackArgs(args);
      String sMemberListId = (String)requestMap.get("sMemberListId");
      String memberType = (String)requestMap.get("memberType");
      String []memberID = (String[])requestMap.get("memberID");
      DomainObject doObj1 = new DomainObject(sMemberListId);

      if("Person".equals(memberType)){
          StringList existingPersonList = getExistingPersonList(context, sMemberListId);
      for(int count =0; count < memberID.length; count++ ) {
        String typePersonId = memberID[count];
        try
        {
            if(!existingPersonList.contains(typePersonId)){
            DomainRelationship.connect(context,sMemberListId,RELATIONSHIP_LIST_MEMBER, typePersonId, false);
            }
        }catch(Exception e)
        {
          e.printStackTrace();

          throw e;
        }
      }
    }else if("Group".equals(memberType))
     {
        StringList existingGroupList = getExistingGroupList(context, sMemberListId);
        for(int count =0; count < memberID.length; count++ ) {
        String typeGroupId = memberID[count];
        boolean isGroupExist = false;
        DomainObject groupObject = null;
        StringList selectList = new StringList(1);
        selectList.add(SELECT_NAME);
        selectList.add(SELECT_ID);
        MapList groupList = DomainObject.findObjects(context,TYPE_BUSINESS_GROUP, typeGroupId, "-", "*", "*", null, false, selectList);
        if(groupList.size()>0)
            isGroupExist = true;
    if(!isGroupExist){
        try {
            groupObject = (DomainObject)JPO.invokeLocal(context, "emxGroupUtil_mxJPO", null, "createBusinessGroupObject", new String[]{typeGroupId}, DomainObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
       
    String groupId = isGroupExist ?((Map)groupList.get(0)).get(SELECT_ID).toString() : groupObject.getId(context);
    try
    {
        if(!existingGroupList.contains(typeGroupId)){
            DomainRelationship.connect(context,sMemberListId,RELATIONSHIP_LIST_MEMBER, groupId, false);  
        }
    
    }catch(Exception e)
    {
      e.printStackTrace();

      throw e;
    }
     }
  }
  }
  /**
   * gets range values for scope field of member list creation form
   *
   * @return HashMap -  
   * @throws Exception if the operation fails
   * @since R212
   */
  public static HashMap getScopeForCreation(Context context, String[] args)throws Exception
  {
      HashMap programMap=(HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get("paramMap");
     
      String language =  (String)paramMap.get("languageStr");
      String strPersonal="emxComponents.MemberList.Personal";
      String strEnterprise="emxComponents.MemberList.Enterprise";
      String strBundle=EnoviaResourceBundle.getProperty(context,"eServiceSuiteComponents.StringResourceFileId");
      String personal= EnoviaResourceBundle.getProperty(context,strBundle,new Locale(language),strPersonal);
      String enterprise= EnoviaResourceBundle.getProperty(context,strBundle,new Locale(language),strEnterprise);
      String ROLE_VPLMProjectLeader = PropertyUtil.getSchemaProperty(context, "role_VPLMProjectLeader" );
      
      Vector personAssignments = PersonUtil.getAssignments(context);
      boolean isAdmin = ( personAssignments.contains(PropertyUtil.getSchemaProperty(context,"role_SpecificationOfficeManager") ) || 
               personAssignments.contains(DomainObject.ROLE_COMPANY_REPRESENTATIVE)|| 
               personAssignments.contains(DomainObject.ROLE_ORGANIZATION_MANAGER) || PersonUtil.hasAssignment(context, ROLE_VPLMProjectLeader));
      
      StringList fieldChoice = new StringList(1);
      fieldChoice.add("Personal");
      if(isAdmin)
      fieldChoice.add("Enterprise");
      
      StringList fieldDisplayChoice = new StringList(1);
      fieldDisplayChoice.add(personal);
      if(isAdmin)
      fieldDisplayChoice.add(enterprise);
      
      HashMap rangeMap = new HashMap();
      rangeMap.put("field_choices",fieldChoice);
      rangeMap.put("field_display_choices",fieldDisplayChoice);
      return rangeMap;

  }
  /**
   * Gets the vector output in HTML format, for the Name column in the MemberListMemberSummary table.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args contains a packed HashMap with the following entries:
   * objectList - a MapList containing the actual maps "dataMap" containing the data.
   * paramList - a HashMap containing the following parameters.
   * reportFormat - a String to identify the Printer Friendly and Export view.
   * @return Vector of the user display names in the HTML format.
   * @throws Exception if the operation fails.
   * @since CommonComponents R212
   */

  public Vector getMemberListMembersName (Context context, String[] args) throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramList = (HashMap) programMap.get("paramList");
      String languageStr = (String)paramList.get("languageStr");
      String showFullName = (String) paramList.get("showFullName");
      boolean isprinterFriendly = paramList.get("reportFormat") != null;
      String exportFormat = (String) paramList.get("exportFormat");
      MapList objList = (MapList)programMap.get("objectList");

      Vector columnVals = new Vector(objList.size());
      DomainObject doObj = DomainObject.newInstance(context);
      
      for(int k=0; k < objList.size(); k++) {
          Map map = (Map) objList.get(k);
          String strMemberId = (String)map.get("id");
          
          doObj.setId(strMemberId);
          StringList selectable = new StringList(DomainConstants.SELECT_TYPE);
          selectable.addElement(DomainConstants.SELECT_NAME);
          Map strObjMap = doObj.getInfo(context,selectable);
          StringBuffer strBuffName = new StringBuffer();
          
          String objType = (String)strObjMap.get(DomainConstants.SELECT_TYPE);
          String strDisplayName = (String)strObjMap.get(DomainConstants.SELECT_NAME);
          
          if(PropertyUtil.getSchemaProperty(context, "type_BusinessGroup").equalsIgnoreCase(objType)){              
              strDisplayName= i18nNow.getAdminI18NString("Group", strDisplayName, languageStr);
              strBuffName.append("<img src=\"../common/images/iconSmallGroup.gif\" alt=\"\" name=\"person\" id=\"PersonId\" border=\"0\"/>&#160;").append(strDisplayName);
          } else if(DomainConstants.TYPE_PERSON.equalsIgnoreCase(objType)) {   
        	  if("true".equalsIgnoreCase(showFullName))
              strDisplayName = PersonUtil.getFullName(context, strDisplayName);
              if(!isprinterFriendly) {
                  strBuffName.append("<a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&amp;objectId=" + strMemberId + "', '800', '575')\"><img src=\"../common/images/iconSmallPerson.gif\" alt=\"\" name=\"person\" id=\"PersonId\" border=\"0\" /></a>&#160;");
                  strBuffName.append("<a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&amp;objectId=" + strMemberId + "', '800', '575')\">"+XSSUtil.encodeForHTML(context,strDisplayName)+"</a>");
              } else {
            	  if(exportFormat!=null && "csv".equalsIgnoreCase(exportFormat)){
            		  strBuffName.append(XSSUtil.encodeForHTML(context,strDisplayName));
            	  }else{
                	  strBuffName.append("<img src=\"../common/images/iconSmallPerson.gif\" alt=\"\" name=\"person\" id=\"PersonId\" border=\"0\"/>");
                      strBuffName.append(XSSUtil.encodeForHTML(context,strDisplayName));              	 
            	  }
              }
          }
          columnVals.add(strBuffName.toString());
      }
      return columnVals;
  }
}
