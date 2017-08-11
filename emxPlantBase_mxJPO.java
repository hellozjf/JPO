/**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of
**   MatrixOne, Inc.  Copyright notice is precautionary only and does
**   not evidence any actual or intended publication of such program
**
*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Plant;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxBus;


public class emxPlantBase_mxJPO  extends emxDomainObject_mxJPO {

    // The operator symbols
    /** A string constant with the value ==. */
    protected static final String SYMB_EQUAL = " == ";
    /** A string constant with the value '. */
    protected static final String SYMB_QUOTE = "'";
    /** A string constant with the value *. */
    protected static final String SYMB_WILD = "*";
    /** A string constant with the value attribute. */
    protected static final String SYMB_ATTRIBUTE = "attribute";
    /** A string constant with the value [. */
    protected static final String SYMB_OPEN_BRACKET = "[";
    /** A string constant with the value ]. */
    protected static final String SYMB_CLOSE_BRACKET = "]";
    /** A string constant with the value ",". */
    protected static final String SYMB_COMMA = ",";
    /** A string constant with the value "string". */
    protected static final String SYMB_STRING = "string";
    /** A string constant with the value "". */
    protected static final String SYMB_EMPTY_STRING = "";
    /** A string constant with the value "descending". */
    protected static final String SYMB_DESCENDING = "descending";
    /** A string constant with the value "0". */
    protected static final String SYMB_ZERO = "0";
    /** A string constant with the value "0000000001". */
    protected static final String FIRST_PLANT_ID = "0000000001";
    /** A string constant with the value "objectId". */
    protected static final String SELECT_OBJECT_ID = "objectId";
    /** A string constant with the value "current". */
    protected static final String SELECT_STATE = "current";
    /** A string constant that defines string resource file name for common components */
    protected static String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";

    /** A string constant with the value "~". */
    protected static final String SYMB_TILT = "~";
    /** A string constant with the value "true". */
    protected static final String BOOL_TRUE = "true";
    /** A string constant with the value "|". */
    protected static final String BOOL_FALSE = "false";
    /** A string constant with the value "|". */
    protected static final String SYMB_PIPE = "|";

    /** A String constant to represent "from["*/
    private static String SELECT_FROM_LEFTBRACE = "from[";
    /** A String constant to represent "]"*/
    private static String SELECT_RIGHTBRACE = "]";
    /** A String constant to represent "."*/
    private static String DOT = ".";
    /** A String constant to represent "attribute["*/
    private static String SELECT_ATTRIBUTE_LEFTBRACE = "attribute[";
    /** A String constant to represent attribute "Plant ID" */
    private static String ATTRIBUTE_PLANT_ID = PropertyUtil.getSchemaProperty("attribute_PlantID");
    /** A String constant to represent "attribute[Plant ID]"*/
    private static String SELECT_PLANT_ID= SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_PLANT_ID+SELECT_RIGHTBRACE;
    /** A String constant to represent type "Plant"*/
    private static String TYPE_PLANT    = PropertyUtil.getSchemaProperty("type_Plant");


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxPlantBase_mxJPO (Context context, String[] args)
      throws Exception {
        super(context, args);
    }

     /**
     * Main entry point.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args)
      throws Exception {
      if (true) {
          throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Common.ERROR", context.getLocale().getLanguage()));
      }
      return 0;
    }

     /**
     * This method is used to get the list of all Plants connected to Business Unit
     * by Organization Plant relationship.
     * @author Sudeep Kumar Dwivedi/Kaustav Banerjee
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @return Object of type MapList
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPlants(Context context, String args[])
        throws Exception {

        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        String strRelationshipName   = PropertyUtil.getSchemaProperty(context, "relationship_OrganizationPlant");
        String strType               = PropertyUtil.getSchemaProperty(context, "type_Plant");
        String strMode               = (String)programMap.get("strMode");
        StringList objectSelects     = new StringList();
        String strObjectId           = null;
        DomainObject domainObject    = null;
        String objectWhere           = null;
        StringList relSelects        = new StringList();
        MapList plantList            = new MapList();
        String strOrganizationpolicy = PropertyUtil.getSchemaProperty(context, "policy_Organization");
        String strStateActive        = FrameworkUtil.lookupStateName(context, strOrganizationpolicy, "state_Active");

        if(strMode != null) {
            objectSelects = (StringList) programMap.get("objectSelects");
            // Get Parent Organization Id [Business Unit OR Company]
            strObjectId   = (String) programMap.get("plantHolderId");
            domainObject  = new DomainObject(strObjectId);
            objectWhere   = SELECT_STATE + SYMB_EQUAL + strStateActive;
        } else {
            // Get Business Unit Id
            strObjectId = (String)programMap.get("objectId");
            domainObject  = new DomainObject(strObjectId);
            objectSelects .add(DomainConstants.SELECT_ID);
            objectSelects.add(DomainConstants.SELECT_OWNER);
            objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
        }
        plantList = domainObject.getRelatedObjects(context, // matrix context
                                                   strRelationshipName, // relationship pattern
                                                   strType, // type pattern
                                                   objectSelects, // object selects
                                                   relSelects, // relationship selects
                                                   false, // to direction
                                                   true, // from direction
                                                   (short) 1, // recursion level
                                                   objectWhere, // object where clause
                                                   null); // relationship where clause

         return plantList;
    }

     /**
     * This method is used to assign and check the value for attribut Plant ID.
     * @author Sudeep Kumar Dwivedi
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     */
    public void assignPlantID(Context context, String args[])
        throws Exception {

        String objectId  = args[0];
        String strAttrPlantID = PropertyUtil.getSchemaProperty(context, "attribute_PlantID");
        String strPlantType = PropertyUtil.getSchemaProperty(context, "type_Plant");
        StringList objectSelects = new StringList(SYMB_ATTRIBUTE+SYMB_OPEN_BRACKET+strAttrPlantID+SYMB_CLOSE_BRACKET);
		String whereExpression	= "name !='WIP Plant' && name !='Corporate Plant'";
        MapList plantList =
            DomainObject.findObjects(context,
                                     strPlantType,
                                     SYMB_WILD,
        			     whereExpression,
                                     objectSelects);

        setId(objectId);
        if (plantList.isEmpty())
        	return;
        if(plantList.size() == 1)
            setAttributeValue(context,strAttrPlantID,FIRST_PLANT_ID);
        else {
            plantList.sort(SYMB_ATTRIBUTE+SYMB_OPEN_BRACKET+strAttrPlantID+SYMB_CLOSE_BRACKET,SYMB_DESCENDING,SYMB_STRING);
            Map theMap = (Map)plantList.get(0);
            String strValue = (String)theMap.get(SYMB_ATTRIBUTE+SYMB_OPEN_BRACKET+strAttrPlantID+SYMB_CLOSE_BRACKET);
            int intVal = Integer.parseInt(strValue) + 1;
            int zeroAdd = 9;
            int len = strValue.length();

            if(intVal >= 10 && intVal<100)
                zeroAdd = 8;
            else if(intVal >= 100 && intVal<1000)
                zeroAdd = 7;
            else if(intVal >= 1000 && intVal<10000)
                zeroAdd = 6;
            else if(intVal >= 10000 && intVal<100000)
                zeroAdd = 5;
            else if(intVal >= 100000 && intVal<1000000)
                zeroAdd = 4;
            else if(intVal >= 1000000 && intVal<10000000)
                zeroAdd = 3;
            else if(intVal >= 10000000 && intVal<100000000)
                zeroAdd = 2;
            else if(intVal >= 100000000 && intVal<1000000000)
                zeroAdd = 1;

            StringBuffer sb = new StringBuffer();
            for(int i=0 ; i< zeroAdd ; i++){
                sb.append(SYMB_ZERO);
            }
            strValue = SYMB_EMPTY_STRING + intVal ;
            sb.append(strValue);
            setAttributeValue(context, strAttrPlantID, sb.toString());
        }
    }


    /**
    * This method is used to delete Plants.
    * @author Sudeep Kumar Dwivedi
    * @param context the eMatrix <code>Context</code> object
    * @throws Exception if the operation fails
    */
    public void deletePlants(Context context,String[] args)
        throws Exception {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectIDs = (String)paramMap.get("plantId");
        StringTokenizer token   = new StringTokenizer(objectIDs,",");
        String strRelationshipName = PropertyUtil.getSchemaProperty(context, "relationship_ManufacturingResponsibility");
        String strPartMasterType = PropertyUtil.getSchemaProperty(context, "type_PartMaster");
        StringList objectSelects = new StringList();
        StringList relSelects = new StringList();
        String strOrganizationpolicy = PropertyUtil.getSchemaProperty(context, "policy_Organization");
        String strStateInactive  = FrameworkUtil.lookupStateName(context, strOrganizationpolicy, "state_Inactive");
        String strStateActive  = FrameworkUtil.lookupStateName(context, strOrganizationpolicy, "state_Active");
        String strAlertMessage1 = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.PlantDelete.Error1");
        String strAlertMessage2 = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.PlantDelete.Error2");
        String strAlertMessage3 = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxComponents.PlantDelete.Error3");

        StringList errorList = new StringList();

        while(token.hasMoreTokens()) {
            String strPlantId        = (String) token.nextToken();
            DomainObject domPlantObj  = new DomainObject(strPlantId);
            domPlantObj.open(context);
            String strPlantName = domPlantObj.getInfo(context,SELECT_NAME);
            String strState = domPlantObj.getInfo(context,SELECT_CURRENT);
            MapList relBusObjList = domPlantObj.getRelatedObjects(context,
                                                                    strRelationshipName,
                                                                    strPartMasterType,
                                                                    objectSelects,
                                                                    relSelects,
                                                                    false,
                                                                    true,
                                                                    (short)1,
                                                                    null,
                                                                    null);
            int relBusObjListSize = relBusObjList.size();
            domPlantObj.close(context);
            if(strState.equals(strStateInactive) && relBusObjListSize==0)
                domPlantObj.deleteObject(context);
            else {
                if(strState.equals(strStateActive))
                    errorList.add(strPlantName + SYMB_COMMA +strAlertMessage2);
                else if(relBusObjListSize !=0)
                    errorList.add(strPlantName + SYMB_COMMA +strAlertMessage3);
            }
        }

        StringBuffer errMessageBuf = new StringBuffer(strAlertMessage1);
        int size = errorList.size();
        if(size > 0) {
            for(int i = 0; i< size ; i++)
            errMessageBuf.append(errorList.get(i));
            emxContextUtil_mxJPO.mqlNotice(context, errMessageBuf.toString());
        }
    }


    /**
    * This method is used to Display Company in the Create Form,Under which the Plant which is to be creates is.
    * @author Sudeep Kumar Dwivedi
    * @param context the eMatrix <code>Context</code> object
    * @throws Exception if the operation fails
    */
    public String getCompany(Context context, String args[]) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
// Modified for IR-052863V6R2011x -Starts
        /*
        String companyID = (String)requestMap.get("parentOID");
        // Added for IR-024373V6R2011 Dated 18/12/2009 Begins.
        if(companyID==null || companyID.equals("") || "null".equalsIgnoreCase(companyID))
        {
            companyID = (String)requestMap.get("objectId");
        }
        // Added for IR-024373V6R2011 Dated 18/12/2009 Ends.
       */
        String companyID = (String)requestMap.get("objectId");
// Modified for IR-052863V6R2011x -Ends
        DomainObject domCompObj  = new DomainObject(companyID);
        String strCompanyName = domCompObj.getInfo(context,SELECT_NAME);
        return strCompanyName;
    }



    /**
    * This method is used to get all the Plants while searching.
    * @author Sudeep Kumar Dwivedi
    * @param context the eMatrix <code>Context</code> object
    * @throws Exception if the operation fails
    */

    public MapList getSearchPlants(Context context, String args[]) throws Exception {
        HashMap programMap            = (HashMap)JPO.unpackArgs(args);
        String strName                = (String)programMap.get("Name");
        Person person                 = Person.getPerson(context);
        String strRelationshipName    = PropertyUtil.getSchemaProperty(context, "relationship_Menber");
        String strType                = PropertyUtil.getSchemaProperty(context, "type_Plant");
        StringList objectSelects      = new StringList();
        objectSelects.add(SELECT_ID);
        StringList relSelects         = new StringList();
        String objectWhere           = null;

        if( strName==null || strName.equals("") ){
            objectWhere = null  ;
        }
        else if("*".equals(strName)) {
            objectWhere = null;
        }
        else {
            objectWhere=SELECT_NAME+"=="+strName;
        }


        MapList relPlantList          = person.getRelatedObjects(context,
                                                  strRelationshipName,
                                                  strType,
                                                  objectSelects,
                                                  relSelects,
                                                  true,
                                                  false,
                                                  (short)1,
                                                  objectWhere,
                                                  null);
        return relPlantList;

    }

   /**
     * hasResponsibility method checks the RMO access.
     *
     * @param context Context : User's Context.
     * @author Srikanth
     * @return String.
     * @since X3
     * @throws Exception if the operation fails.
     */
    public String hasResponsibility(Context context, String[] args)
        throws Exception
    {
        StringBuffer sbSelect1 = new StringBuffer();
        StringBuffer sbSelect2 = new StringBuffer();
        StringBuffer sbSelect3 = new StringBuffer();

        if (args == null || args.length < 1){
            throw (new IllegalArgumentException());
        }

        String role = args[0];
        String returnValue = BOOL_FALSE;
        StringBuffer sbMQLCommd = null;
        String sContextUser = context.getUser();

        sbSelect1.append(SELECT_FROM_LEFTBRACE);
        sbSelect1.append(DomainConstants.RELATIONSHIP_MEMBER);
        sbSelect2.append(SYMB_PIPE);
        sbSelect2.append(DomainConstants.SELECT_TO_NAME);
        sbSelect2.append(SYMB_EQUAL);
        sbSelect2.append(SYMB_QUOTE+sContextUser+SYMB_QUOTE);
        sbSelect3.append(SELECT_RIGHTBRACE);
        sbSelect3.append(DOT);
        sbSelect3.append(SELECT_ATTRIBUTE_LEFTBRACE);
        sbSelect3.append(DomainConstants.ATTRIBUTE_PROJECT_ROLE);
        sbSelect3.append(SELECT_RIGHTBRACE);


        StringList busSelList = new StringList();
        StringList relSelList = new StringList();

        busSelList.add(DomainObject.SELECT_ID);
        busSelList.add(DomainObject.SELECT_NAME);
        busSelList.add(SELECT_PLANT_ID);
        busSelList.add(SELECT_PLANT_ID);
        busSelList.add(sbSelect1.toString()+sbSelect2.toString()+sbSelect3.toString());
        relSelList.add(DomainRelationship.SELECT_ID);
        try{
            MapList mlPlants = getRelatedObjects(context,
                                                 RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
                                                 TYPE_PLANT,
                                                 busSelList,
                                                 relSelList,
                                                 true,
                                                 false,
                                                 (short)1,
                                                 null,
                                                 null);
            Iterator itrPlants = mlPlants.iterator();
            while(itrPlants.hasNext()){
                Map mPlant = (Map)itrPlants.next();
                String sRolesList = (String)mPlant.get(sbSelect1.toString()+sbSelect3.toString());
                if(sRolesList == null){
                    continue;
                }
                StringList slRoles = FrameworkUtil.split(sRolesList,SYMB_TILT);
                if(slRoles.contains(role)){
                    returnValue = BOOL_TRUE;
                    break;
                }
            }
            String results = MqlUtil.mqlCommand(context, "set env $1 $2 $3", "global", "emxPlant", returnValue);
        }
        catch (Exception Ex){
            throw Ex;
        }
        return returnValue;
    }
        /**
         * The method is used in form_APPCreatePlant and form_type_Plant
         * displayTimeZoneRangeValues method displays time zone range values.
         *
         * @param context Context : User's Context.
         * @param String[] args
         * @return HashMap.
         * @throws Exception if the operation fails.
         */
        public HashMap displayTimeZoneRangeValues(Context context,String[] args) throws Exception
        {
            String strLanguage  =  context.getSession().getLanguage();

            HashMap rangeMap = new HashMap ();
            StringList listChoices = new StringList("");
            StringList listDispChoices = new StringList("");

            Map mapTimeZone = Plant.getTimeZoneMap();

            int mapsize = mapTimeZone.size();

            Iterator keyValuePairs = mapTimeZone.entrySet().iterator();
            for (int i = 0; i < mapsize; i++)
            {
                Map.Entry entry = (Map.Entry) keyValuePairs.next();
                listChoices.add(entry.getKey().toString());
                listDispChoices.add(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, entry.getValue().toString(), context.getLocale()));
            }

            rangeMap.put("field_choices", listChoices);
            rangeMap.put("field_display_choices", listDispChoices);

            return rangeMap;
        }

        /**
         * This method is used in form_type_Plant
		 * getTimeZoneDisplayValue method gets time zone display value.
         *
         * @param context Context : User's Context.
         * @param String[] args
         * @return String.
         * @throws Exception if the operation fails.
         */
        public String getTimeZoneDisplayValue(Context context, String[] args) throws Exception{

            HashMap programMap            = (HashMap)JPO.unpackArgs(args);

            HashMap paramMap = (HashMap)programMap.get("paramMap");
            String sPlantId = (String) paramMap.get("objectId");

            return Plant.getTimeZoneDisplayValue(context, sPlantId);
        }
        /**
         * The method is used in form_APPCreatePlant and form_type_Plant
		 * updatePlantTimeZoneAttribute method updates plant time zone attribute value.
         *
         * @param context Context : User's Context.
         * @param String[] args
         * @return void.
         * @throws Exception if the operation fails.
         */
        public void  updatePlantTimeZoneAttribute(Context context,String[] args)throws Exception {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            paramMap   = (HashMap)programMap.get("paramMap");
            String[] arry = (String[])paramMap.get("New Values");
            String sPlantId = (String)paramMap.get("objectId");
            mxBus.setAttributeValue(context, sPlantId, PropertyUtil.getSchemaProperty(context,"attribute_PlantTimeZone"), arry[0]);
        }
        /**
         * This method is used in table_APPPlantSummary. Column PlantTimeZone
		 *getTimeZoneValues method gets time zone value.
         *
         * @param context Context : User's Context.
         * @param String[] args
         * @return String.
         * @throws Exception if the operation fails.
         */
        public Vector getTimeZoneValues (Context context,
                String[] args)
        throws Exception
        {

            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector columnVals = new Vector(objList.size());

            Iterator i = objList.iterator();
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String id = (String)m.get("id");

                columnVals.addElement(Plant.getTimeZoneDisplayValue(context, id));

            }

            return columnVals;
        }

}
