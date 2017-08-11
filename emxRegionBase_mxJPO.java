/*
 *  emxRegionBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Region;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * @version 10.0.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxRegionBase_mxJPO extends emxDomainObject_mxJPO
{

  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since 10.0.0.0
   * @grade 0
   */
  public emxRegionBase_mxJPO (Context context, String[] args)
    throws Exception
  {
    super(context, args);
  }

  /**
   * This method is executed if a specific method is not specified.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns int
   * @throws Exception if the operation fails
   * @since 10.0.0.0
   */
  public int mxMain(Context context, String[] args)
   throws Exception
  {
    if (!context.isConnected()) {
      throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
    }
    return 0;
  }



  /**
  * Gets the MapList containing all the Regions / Sub regions connected to the current Company / Regions.
  * @param context the eMatrix <code>Context</code> object
  * @param args holds input arguments.
  * @return a MapList containing all the Regions / Sub regions connected to the current Company / Region.
  * @throws Exception if the operation fails.
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public  Object getRegions(matrix.db.Context context, String[] args) throws Exception
  {
    if (args.length == 0 )
    {
    throw new IllegalArgumentException();
  }
    SelectList objectSelects = new SelectList(1);
    SelectList relSelects = new SelectList(1);
    objectSelects.add(DomainConstants.SELECT_ID);
    relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    String relName=RELATIONSHIP_ORGANIZATION_REGION;
    String objectId = (String)paramMap.get("objectId");
    String relationshipName = (String)paramMap.get("relationshipName");
    if(RELATIONSHIP_SUB_REGION.equals(relationshipName) )
    {
      relName=RELATIONSHIP_SUB_REGION;
    }
    MapList list= new MapList();
    DomainObject obj =  DomainObject.newInstance(context);
    obj.setId(objectId) ;
    list= obj.getRelatedObjects(context,
                                    relName,
                                    QUERY_WILDCARD,
                                    objectSelects,
                                    relSelects,false,true,(short) 1,null,null);
     return list;
  }



  /**
  * Gets the BusinessUnits connected to the Region.
  *
  * @param context The Matrix Context.
  * @param args holds input arguments.
  * @return Maplist of BusinessUnits connected to the Region.
  * @throws Exception If the operation fails.
  * @since 10.0.0.0
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getRegionsBusinessUnits(Context context,String[] args)
        throws Exception
  {
    if (args.length == 0 )
    {
    throw new IllegalArgumentException();
  }
    boolean toSide = true;
    boolean fromSide = false;
    String typePattern = TYPE_BUSINESS_UNIT;

    SelectList objectSelects = new SelectList(2);
    objectSelects.add(DomainConstants.SELECT_ID);
    objectSelects.add(Organization.SELECT_WEB_SITE);

    SelectList relSelects = new SelectList(1);
    relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    String relName=RELATIONSHIP_ORGANIZATION_REGION;
    String objectId = (String)paramMap.get("objectId");
    MapList list= new MapList();
    DomainObject obj =  DomainObject.newInstance(context);
    obj.setId(objectId) ;
    list= obj.getRelatedObjects(context,
                                    relName,
                                    typePattern,
                                    objectSelects,
                                    relSelects,toSide,fromSide,(short) 1,null,null);
    return list;
  }

  /**
  * Update the Regions Name.
  *
  * @param context The Matrix Context.
  * @param args holds input arguments.
  * @return Maplist of BusinessUnits connected to the Region.
  * @throws Exception If the operation fails.
  * @since 10.0.0.0
  */   
public  Boolean updateName(Context context, String[] args) 
    throws Exception
  {
    HashMap programMap=(HashMap)JPO.unpackArgs(args);

    HashMap paramMap = (HashMap)programMap.get("paramMap");

    String relId = (String)paramMap.get("relId");
    String objectId=(String)paramMap.get("objectId");
    String strName = (String)paramMap.get("New Value");
    String languageStr = (String)paramMap.get("languageStr");
    Locale strLocale = context.getLocale();

    Relationship relObj = new Relationship(relId);
    relObj.open(context);
    BusinessObject busParentObj = relObj.getFrom();
    relObj.close(context);
    busParentObj.open(context);
    String strParentType = busParentObj.getTypeName();
    String strParentId   = busParentObj.getObjectId(context);
    busParentObj.close(context);
    
    String strRelName = "";
    
    if(strParentType.equals(Region.TYPE_REGION))
    {
        strRelName = Region.RELATIONSHIP_SUB_REGION;
    }
    else
    {
        strRelName = Region.RELATIONSHIP_ORGANIZATION_REGION;
    }

    Boolean bUpdate = Boolean.valueOf(false);

    matrix.db.Query query = new matrix.db.Query();
    query.open(context);
    query.setBusinessObjectType(Region.TYPE_REGION);
    query.setBusinessObjectName(strName);
    query.setBusinessObjectRevision("*");
    query.setVaultPattern("*");
    query.setOwnerPattern("*");
    query.setWhereExpression("to["+strRelName+"].from.id == "+strParentId);
    BusinessObjectList boList = query.evaluate(context);
    query.close(context);
    int countEqual = boList.size();

    if(countEqual == 0)
    {
        DomainObject domRegionObj = new DomainObject(objectId);
        domRegionObj.open(context);
        domRegionObj.change(context,Region.TYPE_REGION,strName, domRegionObj.getRevision(), domRegionObj.getVault(), domRegionObj.getPolicy().getName());
        domRegionObj.close(context);
        bUpdate = Boolean.valueOf(true);
    }
    else
    {
        String strMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale,"emxComponents.AddRegion.RegionAlreadyExists");
        emxContextUtil_mxJPO.mqlNotice(context,strMessage);
    }
    return bUpdate;
  }

  /**
  * get the Regions Name.
  *
  * @param context The Matrix Context.
  * @param args holds input arguments.
  * @return Maplist of BusinessUnits connected to the Region.
  * @throws Exception If the operation fails.
  * @since 10.0.0.0
  */
  public String getRegionName(Context context, String[] args) 
    throws Exception
  {
    HashMap programMap=(HashMap)JPO.unpackArgs(args);

    HashMap paramMap = (HashMap)programMap.get("paramMap");
    String relId = (String)paramMap.get("relId");
    String objectId=(String)paramMap.get("objectId");

    HashMap requestMap = (HashMap)programMap.get("requestMap");
    String strMode = (String)requestMap.get("mode");
  
    String strReturnVal = "";

    if(requestMap.get("mode") != null)
    {
        strMode = (String) requestMap.get("mode");
    }

    DomainObject domObj = new DomainObject(objectId);
    domObj.open(context);
    String strName = domObj.getInfo(context,DomainObject.SELECT_NAME);
  
    if("edit".equals(strMode))
    {
        strReturnVal = "<input type=\"text\"  name=\"Name\" value=\""+strName+"\" >";
    }
    else
    {
        strReturnVal = strName;
    }

    return  strReturnVal;
  }
  /**
   * Create the Region object.
   *
   * @param context The Matrix Context.
   * @param args holds input arguments.
   * @throws Exception If the operation fails.
   * @since R211
   */
  @com.matrixone.apps.framework.ui.CreateProcessCallable
  public Map createRegionObject(Context context, String args[]) throws FrameworkException {
      try {
          HashMap requestMap = (HashMap)JPO.unpackArgs(args);
          
          Locale locale = (Locale)requestMap.get("localeObj"); 
          String strOrganizationId = (String) requestMap.get("objectId");
          String type = (String) requestMap.get("TypeActual");
          String name = (String) requestMap.get("Name");
          String policy = (String) requestMap.get("Policy");
          policy = policy != null ? FrameworkUtil.getAliasForAdmin(context, "policy", policy, true) : policy;
          
          
          // Need the BusinessUnit id in order to add a Region.
          if (strOrganizationId == null) {
            throw new FrameworkException(ComponentsUtil.i18nStringNow("emxComponents.Common.InvalidRequestParameters", locale.getLanguage()));
          }
          
          DomainObject organization   = new DomainObject(strOrganizationId);
          policy = policy == null || policy.equals("") || policy.equals("null") ? organization.getDefaultPolicy(context, type) : policy;
          
          HashMap map = new HashMap(1);
          map.put(SELECT_ID, FrameworkUtil.autoRevision(context, type, name, policy, organization.getInfo(context, SELECT_VAULT)));
          return map;
      } catch (Exception e) {
          throw new FrameworkException(e);
      }
  }
  /**
   * Connect the Region with Company.
   *
   * @param context The Matrix Context.
   * @param args holds input arguments.
   * @throws Exception If the operation fails.
   * @since R211
   */
  @com.matrixone.apps.framework.ui.PostProcessCallable
  public void createRegionPostProcess(Context context, String[] args) throws Exception
  {
      try
      {
        Region region = (Region)DomainObject.newInstance(context,DomainConstants.TYPE_REGION);
        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
        
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        Map requestMap             = (Map) programMap.get("requestMap");
        Map paramMap               = (Map) programMap.get("paramMap");
        String languageStr         = (String) paramMap.get("languageStr"); 
    
        String name = (String)requestMap.get("Name");       
        String companyId = (String)requestMap.get("parentOID");
        String regionId = (String)paramMap.get("newObjectId");
        String strOrganizationId = (String) requestMap.get("objectId");
           
        String strObjType = "";
        String strRelName = "";
        if(companyId == null || "".equals(companyId) || "null".equals(companyId))
        {
          Company contextCompany = contextPerson.getCompany(context);
          companyId = contextCompany.getInfo(context, DomainConstants.SELECT_ID);
          
        }
       
        strRelName =Region.RELATIONSHIP_ORGANIZATION_REGION;
        
    	if(!companyId.equals(strOrganizationId)){
    		strRelName=Region.RELATIONSHIP_SUB_REGION;
    		 companyId=strOrganizationId;
        }
    	

        matrix.db.Query query = new matrix.db.Query();
        query.open(context);
        query.setBusinessObjectType(DomainConstants.TYPE_REGION);
        query.setBusinessObjectName(name);
        query.setBusinessObjectRevision("*");
        query.setVaultPattern("*");
        query.setOwnerPattern("*");
        query.setWhereExpression("to["+strRelName+"].from.id == "+companyId);
        BusinessObjectList boList = query.evaluate(context);
        query.close(context);
        int countEqual = boList.size();
        
        if(countEqual == 0) {
          // connect the Region with Company          
          region.setId(regionId);
          region.addFromObject(context,new RelationshipType(strRelName),companyId);
        }
        else {
          // to throw the error message if name is already exists
          throw new FrameworkException(ComponentsUtil.i18nStringNow("emxComponents.AddRegion.RegionAlreadyExists", languageStr));           
        }
      }
      catch(Exception  e) {
          throw new FrameworkException(e.toString());
      }  
    
  }
  
	 /**
   	  * Expand program for select Region  
	  * If isFrom and relationshipName names are passed and if the context object is already connected
	  * to a Region it will be disabled for selection.
	  */  
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList selectionRegionExpandProgram(Context context, String[] args) throws FrameworkException {
      try {
          Map programMap = (Map) JPO.unpackArgs(args);
          
          boolean isAddExisting = "true".equalsIgnoreCase((String)programMap.get("isAddExisting"));
          String isFrom= (String) programMap.get("isFrom");
          String relationshipName = (String) programMap.get("relationshipName");
          
          String SELECT_CURRENT_OBJECT_ID = null;
          if(!UIUtil.isNullOrEmpty(isFrom) && !UIUtil.isNullOrEmpty(relationshipName)) {
              relationshipName = PropertyUtil.getSchemaProperty(context,relationshipName);
              SELECT_CURRENT_OBJECT_ID = "true".equalsIgnoreCase(isFrom) ?
                                          "from["+relationshipName+"].to.id" : "to["+relationshipName+"].from.id";
              isAddExisting = true;
          }
          
          StringList selectables = new StringList(2);
          selectables.add(SELECT_ID);
          selectables.add(SELECT_TYPE);
          if(SELECT_CURRENT_OBJECT_ID != null)
              selectables.add(SELECT_CURRENT_OBJECT_ID);
          
          String orgId = (String) programMap.get("objectId");
          Organization org = new Organization(orgId);
          MapList regions = org.getInfo(context, SELECT_TYPE).equals(TYPE_COMPANY) ?
                           new Company(orgId).getRegions(context, null, selectables) :
                           new Region(orgId).getSubRegions(context, null, selectables, false);    
          
          if(isAddExisting) {
              Map reqMap = (Map) programMap.get("RequestValuesMap");
              String parentObjId = ((String[])reqMap.get("objectId"))[0];
              for (int i = 0; i < regions.size(); i++) {
                  Map bu = (Map) regions.get(i);
                  Object connectedObject = bu.get(SELECT_CURRENT_OBJECT_ID);
                  if(connectedObject == null) {
                      continue;
                  } else if (connectedObject instanceof StringList) {
                      if(((StringList)connectedObject).contains(parentObjId)) {
                          bu.put("disableSelection", "true");
                      }                            
                  } else if(connectedObject.equals(parentObjId)) {
                      bu.put("disableSelection", "true");
                  }
              }
          }
          return regions;
      } catch (Exception e) {
          throw new FrameworkException(e);
      }        
  }  

}
