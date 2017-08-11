/*   emxLocationBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxemxLocation
**
*/


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
 
import matrix.db.Attribute;
import matrix.db.AttributeItr;
import matrix.db.AttributeList;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;
 
import com.matrixone.apps.common.Person;
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
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.XSSUtil;

/**
 * The <code>emxLocation</code> class contains methods for emxLocation.
 *
 * @version Common 10.5.1.2 - Copyright(c) 2004, MatrixOne, Inc.
 */

public class emxLocationBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */

    public emxLocationBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer: 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Location.SpecifyMethodLocationInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    /**
     * Associate the selected locations to use the context calendar
     * Note- will disconnect from other calendar first before connect
     *
     * @param Context
     * @param location/business unit/task id, any object that can connect to a calendar
     * @throws FrameworkException if operation fails
     * @since Common 10.5.1.2
     * @grade 0
     */
    public void setCalendar(Context context, String args[])
        throws FrameworkException,Exception
    {


        Map programMap = (Map) JPO.unpackArgs(args);
        Map requestMap = (HashMap) programMap.get( "reqMap" );
        Map requestTableMap = (HashMap) programMap.get( "reqTableMap" );

        // The objectId is assumed to be a calendar object
        //String objectId = (String) requestTableMap.get( "objectId" );
        String[] objectId = (String[]) requestMap.get( "objectId" );
        String[] emxTableRowId = (String[]) requestMap.get( "emxTableRowId" );

        String sRelationshipCalendar = PropertyUtil.getSchemaProperty( context, "relationship_Calendar" );

        //Iterate through selected locations and connect to current calendar
        for (int i = 0; i < emxTableRowId.length; i++) {

            this.setId( emxTableRowId[i] );

            try {
                // Disconnect existing calendar
                this.setRelatedObject(context, sRelationshipCalendar, true, null);

                //Connect new calendar
                this.setRelatedObject(context, sRelationshipCalendar, true, objectId[0] );

            } catch ( Exception exc ) {
                System.out.println("EXCEPTION CAUGHT (emxLocation:setCalendar) - " + exc.getMessage() );
                throw new Exception( exc.getMessage() );
            }
        }
    }

    /**
     * Return the locations/business units associated to the calendar
     *
     * @param Context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context calendar object
     * @return MapList containing the id of location objects
     * @throws Exception if operation fails
     * @since Common 10.5.1.2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable 
    public MapList getLocation(Context context,String[] args) throws Exception
      {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = new MapList();

        String objectId = (String)paramMap.get("objectId");
        String RELATIONSHIP_CALENDAR = PropertyUtil.getSchemaProperty(context,"relationship_Calendar");

        StringList busSelects = new StringList(1);
        busSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        com.matrixone.apps.common.WorkCalendar workcalendar =
           (com.matrixone.apps.common.WorkCalendar) DomainObject.newInstance( context, objectId);

        StringList sel = new StringList(4);
        sel.add(DomainConstants.SELECT_ID);
        sel.add(DomainConstants.SELECT_TYPE);
        sel.add(DomainConstants.SELECT_NAME);
        StringList rel = new StringList(1);
        rel.add(DomainRelationship.SELECT_ID);

        objectList = workcalendar.getRelatedObjects(context,
                                RELATIONSHIP_CALENDAR,
                                TYPE_LOCATION,
                                sel,
                                rel,
                                true,
                                false,
                                (short) 0,
                                null,
                                null);
        return objectList;
      }

    /**
     * Return the calendars associated to a location
     *
     * @param Context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context location object
     * @return MapList containing the id of calendar object
     * @throws Exception if operation fails
     * @since Common 10.5.1.2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable 
    public MapList getCalendar(Context context,String[] args) throws Exception
      {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = new MapList();

        String objectId = (String)paramMap.get("objectId");
        String RELATIONSHIP_CALENDAR = PropertyUtil.getSchemaProperty(context,"relationship_Calendar");
        StringList busSelects = new StringList(1);
        busSelects.add(DomainConstants.SELECT_ID);

        DomainObject obj = DomainObject.newInstance(context, objectId);
        Map map = obj.getRelatedObject(context, RELATIONSHIP_CALENDAR, true, busSelects, null);
        if(map != null)
        {
           objectList.add(map);
        }

        return objectList;
    }

    /**
    * Gets the locations based the critera passed.
    * getLocationSearchResults(Context context, String[] args) throws Exception
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since Common 11.0
    */
      @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getLocationSearchResults(Context context, String[] args) throws Exception
    {
        MapList listOfLocations = new MapList();

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");
        String sLocName   = (String)paramMap.get("Name");
        String sLocAddress   = (String)paramMap.get("txt_address");
        String sLocCity   = (String)paramMap.get("txt_city");
        String sLocState   = (String)paramMap.get("txt_state");
        String sLocPostalCode   = (String)paramMap.get("txt_postalcode");
        // MCC Bug fix 328078, need to show only Active Locations
        String sDefaultStates   = (String)paramMap.get("defaultStates");
        String companyId   = (String)paramMap.get("companyId");
        String attr_Address1 = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_Address1);
        String attr_Address2 = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_Address2);

        if(companyId==null || "null".equals(companyId))
        {
            companyId="";
        }


        if (sLocName == null || sLocName.length() <= 0)
        {
          sLocName = "*";
        }
        if (sLocAddress == null || sLocAddress.length() <= 0)
        {
           sLocAddress = "*";
        }
        if (sLocCity == null || sLocCity.length() <= 0)
        {
          sLocCity = "*";
        }
        if (sLocState == null || sLocState.length() <= 0)
        {
          sLocAddress = "*";
        }
        if (sLocPostalCode == null || sLocPostalCode.length() <= 0)
        {
          sLocPostalCode = "*";
        }
        StringBuffer strWhereExp = new StringBuffer();
        if(!sLocAddress.equals("*") && !sLocAddress.equals(""))
        {
         if(sLocAddress.indexOf("*") == -1)
          {
            strWhereExp.append("(attribute["+attr_Address1+"] == \""+sLocAddress+"\") || (attribute["+attr_Address2+"] == \""+sLocAddress+"\")");
          }
         else
         {
            strWhereExp.append("(attribute["+attr_Address1+"] ~= \""+sLocAddress+"\") || (attribute["+attr_Address2+"] == \""+sLocAddress+"\")");
         }
        }

        if(!sLocCity.equals("*") && !sLocCity.equals(""))
        {
         if(sLocCity.indexOf("*") == -1)
         {
            if(strWhereExp.length()>0) {strWhereExp.append(" && ");}
            strWhereExp.append("(attribute["+DomainConstants.ATTRIBUTE_CITY+"] == \""+sLocCity+"\")");
         } else {
            if(strWhereExp.length()>0) {strWhereExp.append(" && ");}
            strWhereExp.append("(attribute["+DomainConstants.ATTRIBUTE_CITY+"] ~= \""+sLocCity+"\")");
           }
        }

        if(!sLocState.equals("*") && !sLocState.equals(""))
        {
         if(sLocState.indexOf("*") == -1)
          {
            if(strWhereExp.length()>0) {strWhereExp.append(" && ");}
            strWhereExp.append("(attribute["+DomainConstants.ATTRIBUTE_STATE_REGION+"] == \""+sLocState+"\")");
          } else {
            if(strWhereExp.length()>0) {strWhereExp.append(" && ");}
            strWhereExp.append("(attribute["+DomainConstants.ATTRIBUTE_STATE_REGION+"] ~= \""+sLocState+"\")");
          }
        }

        if(!sLocPostalCode.equals("*") && !sLocPostalCode.equals(""))
        {
          if(sLocPostalCode.indexOf("*") == -1)
          {
            if(strWhereExp.length()>0) {strWhereExp.append(" && ");}
            strWhereExp.append("(attribute["+DomainConstants.ATTRIBUTE_POSTAL_CODE+"] == \""+sLocPostalCode+"\")");
          } else {
            if(strWhereExp.length()>0) {strWhereExp.append(" && ");}
            strWhereExp.append("(attribute["+DomainConstants.ATTRIBUTE_POSTAL_CODE+"] ~= \""+sLocPostalCode+"\")");
            }
        }
        // MCC Bug fix 328078, need to show only Active Locations
        if( sDefaultStates !=null && !"null".equals(sDefaultStates) && sDefaultStates.length() > 0)
        {
          if(sDefaultStates.indexOf(",") != -1)
          {
              if(strWhereExp.length()>0)
              {
                strWhereExp.append(" && ");
              }
              StringList statesList = FrameworkUtil.split(sDefaultStates,",");
              StringBuffer buffer = new StringBuffer(1);
              buffer.append("(");
              for (int i=0,siz=statesList.size();i<siz;i++)
              {
                  String sPolState = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_LOCATION, (String)statesList.get(i));
                  if(i>0)
                  {
                    buffer.append(" || ");
                  }
                  buffer.append(" (current == \""+sPolState+"\") ");
              }
              buffer.append(")");
              strWhereExp.append (buffer.toString());

          } else {
            if(strWhereExp.length()>0)
            {
                strWhereExp.append(" && ");
            }
                String sPolState = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_LOCATION, sDefaultStates);
                strWhereExp.append("(current == \""+sPolState+"\")");
            }
        }

        String sWhereExp = strWhereExp.toString();
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        if(companyId.equals("null") || companyId == null || "".equalsIgnoreCase(companyId))
        {
        listOfLocations = DomainObject.findObjects(context,
                                                   DomainConstants.TYPE_LOCATION,
                                                   sLocName,
                                                   "*",
                                                   "*",
                                                   context.getVault().getName(),
                                                   sWhereExp,
                                                   false,
                                                   objectSelects);
        }
        else{
            if (!"*".equals(sLocName) && !companyId.equals(""))
               {
                if(strWhereExp.length()>0){
                strWhereExp.append(" && ");
                }
                strWhereExp.append("( name ~~ \""+sLocName+"\")");
               }
           com.matrixone.apps.common.Company company = new com.matrixone.apps.common.Company(companyId);
           listOfLocations=company.getLocations(context,objectSelects,strWhereExp.toString());
        }

        return listOfLocations;
    }

    /**
    * Assigns the Plant ID attribute value as the revision of Location.
    * assignLocationPlantID(Context context, String[] args) throws Exception
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since Common 10-7
    */
     public void assignLocationPlantID(Context context, String[] args) throws Exception
    {
        String objectId  = args[0];
        DomainObject dom = new DomainObject(objectId);
        String rev = dom.getInfo(context,DomainConstants.SELECT_REVISION);
        String attr_PlantID = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_PlantID);
        dom.setAttributeValue(context, attr_PlantID, rev);
    }

    /**
    * Checks Plant ID is unique or not among the Locations available in the company.
    * checkPlanIdUniqueness(Context context, String[] args) throws Exception
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId and param values.
    * @throws Exception If the operation fails.
    * @since Common 10-7
    */
    public int checkPlantIdUniqueness(Context context, String[] args) throws Exception
    {
        String objectId  = args[0];
        String attributeName  = args[1];
        String attr_PlantID = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_PlantID);

        if(attr_PlantID.equals(attributeName)) {
            DomainObject dom = new DomainObject(objectId);
            String rev = dom.getInfo(context,DomainConstants.SELECT_REVISION);
            String selectPlantID = DomainObject.getAttributeSelect(attr_PlantID);
            String objWhere = selectPlantID +" == '"+rev+"'";

            StringList sel = new StringList(1);
            sel.add(DomainConstants.SELECT_ID);

            MapList objectList = dom.findObjects(context,
                                                TYPE_LOCATION,
                                                "*",
                                                objWhere,
                                                sel);
            if(objectList.size() != 0) {
              String strMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(context.getSession().getLanguage()),"emxComponents.Location.PlanIdExists");
              emxContextUtil_mxJPO.mqlNotice(context,strMessage);
              return 1;
            }
        }
        return 0;
    }

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createLocationPostProcess(Context context, String[] args) throws Exception {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        Map requestMap             = (Map) programMap.get("requestMap");
        Map paramMap = (Map) programMap.get("paramMap");
        String languageStr = (String) paramMap.get("languageStr"); 
    
        String sName = (String)requestMap.get("Name");          
        String strOrganizationId = (String)requestMap.get("objectId");
        String sPrimaryContact = (String)requestMap.get("PrimaryContactOID");
        
         String locationId = (String)paramMap.get("newObjectId");
        
        DomainObject organization     = null;
        DomainObject boCompany        = null;
        DomainObject location         = null;
        
        // Need the BusinessUnit id in order to add a location.
        if (strOrganizationId == null) {
          throw new MatrixException(ComponentsUtil.i18nStringNow("emxComponents.Common.InvalidRequestParameters", languageStr));
        }
        
        organization   = new DomainObject(strOrganizationId);
        String strType        = organization.getInfo(context,DomainConstants.SELECT_TYPE);
        
        String strCompId = strOrganizationId;
        boCompany = new DomainObject(strOrganizationId);
    
        if(DomainConstants.TYPE_BUSINESS_UNIT.equals(strType)) {
          while(!DomainConstants.TYPE_COMPANY.equals(strType) && !"".equals(strType)) {
              if(DomainConstants.TYPE_BUSINESS_UNIT.equals(strType)) {
                  strType = boCompany.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DIVISION + "].from.type");
                  strCompId = boCompany.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DIVISION + "].from.id");
                  boCompany= new DomainObject(strCompId);
              }
          }
        }
        
        matrix.db.Query query = new matrix.db.Query();
        query.open(context);
        query.setBusinessObjectType(DomainConstants.TYPE_LOCATION);
        query.setBusinessObjectName(sName);
        query.setBusinessObjectRevision("*");
        query.setVaultPattern("*");
        query.setOwnerPattern("*");
        query.setWhereExpression("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id == "+strCompId);
        BusinessObjectList boList = query.evaluate(context);
        query.close(context);
        if(boList.size() == 0) {
              location = new DomainObject(locationId);
    
              AttributeList attrListLocation = new AttributeList();
              AttributeItr attributeItr = new AttributeItr( location.getAttributes( context ).getAttributes() );
    
              Attribute attribute = null;
              String parameter = "";
              while(attributeItr.next()) {
                  attribute = attributeItr.obj();
                  parameter = (String)requestMap.get(attribute.getName());
                  if ( parameter != null ) {
                      attribute.setValue(parameter );
                  }
                  attrListLocation.add(attribute);
              }
    
              // update the attributes on the relationship
              location.setAttributes(context, attrListLocation);
              location.update(context);
              
              //check if person name exist.
              if(sPrimaryContact !=null && !"null".equalsIgnoreCase(sPrimaryContact) && !"".equalsIgnoreCase(sPrimaryContact)) {
                   String relPrimaryContact =PropertyUtil.getSchemaProperty(context, "relationship_PrimaryContact");
                   //get new person object as a Primary Contact
                   Person personObj = new Person(sPrimaryContact);
                   // Connecting the selected Person with the new Location
                   DomainRelationship.connect(context,
                                               location,//the object to connect from
                                               relPrimaryContact, //the relationship type used for the connection
                                               personObj //the object to connect to 
                                               );
              }
    
              DomainRelationship domOrgRelWithCompany = DomainRelationship.connect(context,boCompany,DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION,location);
               
              DomainRelationship domOrgRelWithBusUnit = null;
              // to connect to Business Unit...if locations are created under business unit.
              if(!strOrganizationId.equals(strCompId)) {
                  domOrgRelWithBusUnit = DomainRelationship.connect(context,organization,DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION,location);
              }
              
              // Update the relationship attributes
              if ( domOrgRelWithCompany != null ) {
                  AttributeList attrRelListLocation = new AttributeList();
                  attributeItr = new AttributeItr( domOrgRelWithCompany.getAttributes( context ) );
                  while(attributeItr.next()) {
                      attribute = attributeItr.obj();
                      parameter = (String)requestMap.get(attribute.getName());
                      String sAttrValue = (parameter != null) ? "Yes" : "No";
                      attribute.setValue(sAttrValue);
                      attrRelListLocation.add(attribute);
                  }
                  // to update Relationship attributes with Company
                  domOrgRelWithCompany.setAttributeValues(context, attrRelListLocation);
                  
                  // to update Relationship attributes with Business Unit
                  if( domOrgRelWithBusUnit != null ) {
                      domOrgRelWithBusUnit.setAttributeValues(context,attrRelListLocation);
                  }
              }
        }  
        
    }
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void editLocation(Context context, String[] args) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        Map requestMap             = (Map) programMap.get("requestMap");
   
        String sName = (String)requestMap.get("Name");          
        String locationId = (String)requestMap.get("objectId");
        String strOrganizationId = (String)requestMap.get("parentOID");
        String relId = (String)requestMap.get("relId");

        String sPrimaryContact = (String)requestMap.get("PrimaryContact");
        boolean bExists = false;

        DomainObject location = null;
        if(locationId != null) {
            location = new DomainObject(locationId);
        }
        
        // show the current primary contact if exists
        String relPrimaryContact =PropertyUtil.getSchemaProperty(context, "relationship_PrimaryContact"); 
        StringList busRelSelects=new StringList();
        busRelSelects.addElement("from["+relPrimaryContact+"].to.name");
        busRelSelects.addElement("from["+relPrimaryContact+"]."+DomainObject.SELECT_RELATIONSHIP_ID);
        Map map = location.getInfo(context,busRelSelects);
        String sPrimaryContactName = (String)map.get("from["+relPrimaryContact+"].to.name");

        //get relationship Id between Person & Location if exist
        String relationshipId =(String)map.get("from["+relPrimaryContact+"]."+DomainObject.SELECT_RELATIONSHIP_ID);
   
        // Update the location name if it was changed
        String strLocName = location.getInfo(context,DomainConstants.SELECT_NAME);        
        if (sName != null && !sName.equals(strLocName)) {
            String strCompId        = strOrganizationId;
            DomainObject boCompany  = new DomainObject(strCompId);
            String strType          = boCompany.getInfo(context,DomainConstants.SELECT_TYPE);
            if(DomainConstants.TYPE_BUSINESS_UNIT.equals(strType)) {
                while(!DomainConstants.TYPE_COMPANY.equals(strType) && !"".equals(strType)) {
                    if(DomainConstants.TYPE_BUSINESS_UNIT.equals(strType)) {
                        strType     = boCompany.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DIVISION + "].from.type");
                        strCompId   = boCompany.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DIVISION + "].from.id");
                        boCompany   = new DomainObject(strCompId);
                    }
                }
            }
            try {
                matrix.db.Query query = new matrix.db.Query();
                query.open(context);
                query.setBusinessObjectType(DomainConstants.TYPE_LOCATION);
                query.setBusinessObjectName(sName);
                query.setBusinessObjectRevision("*");
                query.setVaultPattern("*");
                query.setOwnerPattern("*");
                query.setWhereExpression("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id == "+strCompId);
                BusinessObjectList boList = query.evaluate(context);
                query.close(context);

                if(boList.size() == 0) {
                    String strLocType   = location.getInfo(context,DomainConstants.SELECT_TYPE);
                    String strLocRev    = location.getInfo(context,DomainConstants.SELECT_REVISION);
                    String strLocVault  = location.getInfo(context,DomainConstants.SELECT_VAULT);
                    String strLocPolicy = location.getInfo(context,DomainConstants.SELECT_POLICY);
                    location.change( context, strLocType, sName, strLocRev,
                                 strLocVault, strLocPolicy);
                }      
                else {
                    bExists = true;
                }                         
            } 
            catch ( MatrixException me ) {
            }
        }
        
        if (!bExists) {
            // Update the location attributes
            AttributeList attributeList = new AttributeList( );
            AttributeItr attributeItr = new AttributeItr( location.getAttributes( context ).getAttributes() );
            
            Attribute attribute = null;
            String parameter = "";
            while(attributeItr.next()) {
                attribute = attributeItr.obj();
                parameter = (String)requestMap.get( attribute.getName());
                if(parameter != null) 
                {
                    attribute.setValue(parameter);
                }
                attributeList.add(attribute);
            }            
            location.setAttributes(context, attributeList);
            location.update(context);
            
             try {
                ContextUtil.startTransaction(context, true);
                //get relationship Id between Person & Location if exist
                if(sPrimaryContactName !=null && !"null".equalsIgnoreCase(sPrimaryContactName) && !"".equalsIgnoreCase(sPrimaryContactName)) {
                      if(sPrimaryContact !=null && !"null".equalsIgnoreCase(sPrimaryContact) && !"".equalsIgnoreCase(sPrimaryContact)) {
                          if(!sPrimaryContactName.equals(sPrimaryContact)) { 
                             // disconnect existing connection
                             DomainRelationship.disconnect(context, relationshipId); 
                             
                             //get new person object as a Primary Contact
                             Person personObj = Person.getPerson(context,sPrimaryContact);
                             // Connecting the selected Person with the Location
                             DomainRelationship.connect( context,
                                                         location,//the object to connect from
                                                         relPrimaryContact, //the relationship type used for the connection
                                                         personObj //the object to connect to 
                                                         );
                          }
                      }
                      else {   // disconnect existing connection
                          DomainRelationship.disconnect(context, relationshipId);   
                      }
                }
                else {
                      if(sPrimaryContact !=null && !"null".equalsIgnoreCase(sPrimaryContact) && !"".equalsIgnoreCase(sPrimaryContact)) {
                           //get new person object as a Primary Contact
                           Person personObj = Person.getPerson(context,sPrimaryContact);
                           
                           // Connecting the selected Person with the Location
                           DomainRelationship.connect(context,
                                                      location,//the object to connect from
                                                      relPrimaryContact, //the relationship type used for the connection
                                                      personObj //the object to connect to 
                                                      );
                      
                      }
                }
                // commiting transaction
                ContextUtil.commitTransaction(context);
            }
            catch(Exception e)
            {
                  ContextUtil.abortTransaction(context);
            }
             
            // Get the connected relationship object
            DomainRelationship domOrgRelWithOrg = new DomainRelationship(relId);
            AttributeList attrRelListLocation = new AttributeList();
            attributeItr = new AttributeItr( domOrgRelWithOrg.getAttributes( context ) );
            while(attributeItr.next()) {
                attribute = attributeItr.obj();
                parameter = (String)requestMap.get( attribute.getName());
                String sAttrValue = (parameter != null) ? "Yes" : "No";
                attribute.setValue(sAttrValue);
                attrRelListLocation.add( attribute );
            }
            domOrgRelWithOrg.setAttributeValues(context, attrRelListLocation);      
    }
    }
    public boolean isParentACompany(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args); 
        String sMode = (String)programMap.get("mode");
        String parentId = "";
        
        if("create".equals(sMode)) {
            parentId = (String)programMap.get("parentOID");
        }
        else {
            String locationId = (String)programMap.get("objectId");
            if(locationId != null) {
                DomainObject location = new DomainObject(locationId);
                StringList busRelSelects=new StringList();
                busRelSelects.addElement("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
                Map map = location.getInfo(context,busRelSelects);
                parentId = (String)map.get("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
            }
        }
         
        String sOrgType = "";
        if(parentId != null) {
            sOrgType = (String)new DomainObject(parentId).getInfo(context, DomainConstants.SELECT_TYPE);
        }
        if(sOrgType != null) {
            if(DomainConstants.TYPE_COMPANY.equals(sOrgType)) {
                return true;
            }
        }
        return false;
    }
    public boolean isParentABU(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args); 
        String sMode = (String)programMap.get("mode");
        String parentId = "";
        
        if("create".equals(sMode)) {
            parentId = (String)programMap.get("parentOID");
        }
        else {
            String locationId = (String)programMap.get("objectId");
            if(locationId != null) {
                DomainObject location = new DomainObject(locationId);
                StringList busRelSelects=new StringList();
                busRelSelects.addElement("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
                Map map = location.getInfo(context,busRelSelects);
                parentId = (String)map.get("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
            }
        }        
        
        String sOrgType = "";
        if(parentId != null) {
            sOrgType = (String)new DomainObject(parentId).getInfo(context, DomainConstants.SELECT_TYPE);
        }
        if(sOrgType != null) {
            if(DomainConstants.TYPE_BUSINESS_UNIT.equals(sOrgType)) {
                return true;
            }
        }
        return false;
    }

    public String getLocationTypeData(Context context, String args[]) throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        StringBuffer strLocTypeValue = new StringBuffer();
        String relId = "";
        Map relAttributesMap = null;
        try {
            
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String languageStr = (String) paramMap.get("languageStr");  
            String mode = (String)requestMap.get("mode");
            relId = (String)requestMap.get("relId");
            
            if(relId != null) {
                relAttributesMap = DomainRelationship.getAttributeMap(context,relId);
            }
            
            String sRelAttrBillingAddress  = PropertyUtil.getSchemaProperty(context, "attribute_BillingAddress" );
            String sRelAttrHeadquarters    = PropertyUtil.getSchemaProperty(context, "attribute_HeadquartersSite" );
            String sRelAttrManufacturing   = PropertyUtil.getSchemaProperty(context, "attribute_ManufacturingSite" );
            String sRelAttrShippingAddress = PropertyUtil.getSchemaProperty(context, "attribute_ShippingAddress" );
            
            String i18NsRelAttrBillingAddress  = i18nNow.getAttributeI18NString( sRelAttrBillingAddress,  languageStr);
            String i18NsRelAttrHeadquarters    = i18nNow.getAttributeI18NString( sRelAttrHeadquarters,  languageStr);
            String i18NsRelAttrManufacturing   = i18nNow.getAttributeI18NString( sRelAttrManufacturing,  languageStr);
            String i18NsRelAttrShippingAddress = i18nNow.getAttributeI18NString( sRelAttrShippingAddress,  languageStr);
            
            String strBillingAddress    = "";
            String strHeadquartersSite  = "";
            String strManufacturingSite = "";
            String strShippingAddress   = "";
            if( relAttributesMap != null ) {
                if("Yes".equals((String)relAttributesMap.get(sRelAttrBillingAddress))) {
                    strLocTypeValue.append(i18NsRelAttrBillingAddress);
                    strBillingAddress = "checked";
                }
                if("Yes".equals((String)relAttributesMap.get(sRelAttrHeadquarters))) {
                    if(strLocTypeValue.length() > 0) {
                        strLocTypeValue.append("<br>");
                    }
                    strLocTypeValue.append(i18NsRelAttrHeadquarters);
                    strHeadquartersSite = "checked";
                }
                if("Yes".equals((String)relAttributesMap.get(sRelAttrManufacturing))) {
                    if(strLocTypeValue.length() > 0) {
                        strLocTypeValue.append("<br>");
                    }
                    strLocTypeValue.append(i18NsRelAttrManufacturing);
                    strManufacturingSite = "checked";
                }
                if("Yes".equals((String)relAttributesMap.get(sRelAttrShippingAddress))) {
                    if(strLocTypeValue.length() > 0) {
                        strLocTypeValue.append("<br>");
                    }
                    strLocTypeValue.append(i18NsRelAttrShippingAddress);
                    strShippingAddress = "checked";
                }
                strLocTypeValue.append(" ");
            }
            if("edit".equals(mode)) {
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrBillingAddress).append("\" value=\"").append(sRelAttrBillingAddress).append("\"").append(strBillingAddress).append("/>").append(i18NsRelAttrBillingAddress);
                buffer.append("<br>");
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrHeadquarters).append("\" value=\"").append(sRelAttrHeadquarters).append("\"").append(strHeadquartersSite).append("/>").append(i18NsRelAttrHeadquarters);
                buffer.append("<br>");
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrManufacturing).append("\" value=\"").append(sRelAttrManufacturing).append("\"").append(strManufacturingSite).append("/>").append(i18NsRelAttrManufacturing);
                buffer.append("<br>");
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrShippingAddress).append("\" value=\"").append(sRelAttrShippingAddress).append("\"").append(strShippingAddress).append("/>").append(i18NsRelAttrShippingAddress);
            }
            else if("create".equals(mode)) {
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrBillingAddress).append("\" value=\"").append(sRelAttrBillingAddress).append("\"></input>").append(i18NsRelAttrBillingAddress);
                buffer.append("<br/>");
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrHeadquarters).append("\" value=\"").append(sRelAttrHeadquarters).append("\"></input>").append(i18NsRelAttrHeadquarters);
                buffer.append("<br/>");
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrManufacturing).append("\" value=\"").append(sRelAttrManufacturing).append("\"></input>").append(i18NsRelAttrManufacturing);
                buffer.append("<br/>");
                buffer.append("<input type=\"checkbox\" name=\"").append(sRelAttrShippingAddress).append("\" value=\"").append(sRelAttrShippingAddress).append("\"></input>").append(i18NsRelAttrShippingAddress);
            }
            else {
                buffer.append(strLocTypeValue.toString());
            }
            
        } 
        catch (Exception exp)
        {
            exp.printStackTrace();
            throw exp;
        }
        //XSSOK
        return buffer.toString();
    }
    
    public String getOrgData(Context context, String args[]) throws Exception
    {
        StringBuffer buffer = new StringBuffer();

        try {            
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String orgId = (String)requestMap.get("parentOID");
            String locId = (String)requestMap.get("objectId");
            String mode = (String)requestMap.get("mode");
            Map objMap = null;
            DomainObject orgObj = null;
            String parentType = "";
            String sOrgName = "";
            StringList busSelects = new StringList(2);
            busSelects.add(DomainConstants.SELECT_TYPE);
            busSelects.add(DomainConstants.SELECT_NAME);
            String popTreeUrl = "";
            
            boolean isPrinterFriendly = "true".equals(requestMap.get("PFmode"));

            boolean isExport = (requestMap.get("reportFormat") != null);
  
            
            if("create".equals(mode)) {
                if(orgId != null) {
                    orgObj = new DomainObject(orgId);
                    objMap = (Map) orgObj.getInfo(context, busSelects);
                    parentType = (String)objMap.get(DomainConstants.SELECT_TYPE);
                    sOrgName = (String)objMap.get(DomainConstants.SELECT_NAME);
                }
              /**	IR-268385V6R2014x - k3d
               * RCA:	Latin characters are not getting encoded properly to be parsed by SAXBuilder in UIFormCommon:addFields()
               * FIX:	changing encodeForHTML to encodeForXML as ProgramHTMLOutput field value is being parsed as XML
               */
                if(DomainConstants.TYPE_BUSINESS_UNIT.equals(parentType)) {
                    buffer.append("<img src=\"../common/images/iconSmallBusinessUnit.gif\" name=\"imgBusinessUnit\" id=\"imgBusinessUnit\" alt=\"*\" />").append(XSSUtil.encodeForXML(context, sOrgName));                                    
                }
                else {
                    buffer.append("<img src=\"../common/images/iconSmallCompany.gif\" name=\"imgCompany\" id=\"imgCompany\" alt=\"*\" />").append(XSSUtil.encodeForXML(context, sOrgName));
                }
            }
            else {
                String sParentType = "";
                String sParentId = "";
                String sParentName = "";
                if(locId != null) {
                    DomainObject location = new DomainObject(locId);
                    StringList busRelSelects=new StringList();
                    busRelSelects.addElement("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.type");
                    busRelSelects.addElement("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
                    busRelSelects.addElement("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.name");
                    Map map = location.getInfo(context,busRelSelects);
                    sParentType = (String)map.get("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.type");
                    sParentId = (String)map.get("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
                    sParentName = (String)map.get("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.name");
                }
                popTreeUrl = "../common/emxTree.jsp?objectId=" + sParentId ;
                popTreeUrl  ="javascript:showModalDialog('" + popTreeUrl + "', 800,575);";
                if("edit".equals(mode)) {
                    if(DomainConstants.TYPE_BUSINESS_UNIT.equals(sParentType)) {
                        buffer.append("<img src=\"../common/images/iconSmallBusinessUnit.gif\" name=\"imgBusinessUnit\" id=\"imgBusinessUnit\" alt=\"*\" />").append(sParentName);              
                    }
                    else {                    
                        buffer.append("<img src=\"../common/images/iconSmallCompany.gif\" name=\"imgCompany\" id=\"imgCompany\" alt=\"*\" />").append(sParentName);
                    }
                }
                else {
                    if(DomainConstants.TYPE_BUSINESS_UNIT.equals(sParentType)) {
                    	if(isExport){
                    		buffer.append(sParentName);
                    	}else{
                    		buffer.append("<img src=\"../common/images/iconSmallBusinessUnit.gif\" name=\"imgBusinessUnit\" id=\"imgBusinessUnit\" alt=\"*\" />");
                    		if(isPrinterFriendly){
                    		buffer.append(XSSUtil.encodeForHTML(context,sParentName));
                    	}else{
                    		buffer.append("<a href=\"").append(popTreeUrl).append("\">").append(sParentName).append("</a>");                		
                    	}
                      }                        
                    }
                    else {
                    	if(isExport){
                    		buffer.append(sParentName);
                    	}else{
                    		buffer.append("<img src=\"../common/images/iconSmallCompany.gif\" name=\"imgCompany\" id=\"imgCompany\" alt=\"*\" />");
                    		if(isPrinterFriendly){
                    		buffer.append(XSSUtil.encodeForHTML(context,sParentName));
                    	}else{
                    		buffer.append("<a href=\"").append(popTreeUrl).append("\">").append(sParentName).append("</a>");                    
                    	}
                      }  
                    }
                }
            }
           
        } 
        catch (Exception exp)
        {
            exp.printStackTrace();
            throw exp;
        }
        return buffer.toString();
    }
    
    public String getLocationPrimaryContactSearchQuery(Context context, String args[]) throws Exception
    {
        // unpacking the Arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String editMode = (String)((HashMap)programMap.get("requestMap")).get("mode");
        String orgId = "";
        if("edit".equalsIgnoreCase(editMode)) {
            String locationId = (String)((HashMap)programMap.get("requestMap")).get("objectId");
            if(locationId != null) {
                DomainObject location = new DomainObject(locationId);
                StringList busRelSelects=new StringList();
                busRelSelects.addElement("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
                Map map = location.getInfo(context,busRelSelects);
                orgId = (String)map.get("to["+DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION+"].from.id");
            }
        }
        else {
            orgId = (String)((HashMap)programMap.get("requestMap")).get("parentOID");
        }
        String orgType = new DomainObject(orgId).getInfo(context, DomainConstants.SELECT_TYPE);
        String returnString = (DomainConstants.TYPE_BUSINESS_UNIT.equals(orgType))? "BU_EMPLOYEE_ID="+orgId : "EMPLOYEE_ID="+orgId;
        return returnString;
    }
    
    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createLocationObject(Context context, String args[]) throws FrameworkException {
        try {
            HashMap requestMap = (HashMap)JPO.unpackArgs(args);
            
            Locale locale = (Locale)requestMap.get("localeObj"); 
            String strOrganizationId = (String) requestMap.get("objectId");
            String type = (String) requestMap.get("TypeActual");
            String name = (String) requestMap.get("Name");
            String policy = (String) requestMap.get("Policy");
            policy = policy != null ? FrameworkUtil.getAliasForAdmin(context, "policy", policy, true) : policy;
            
            
            // Need the BusinessUnit id in order to add a location.
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
}
