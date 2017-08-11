/*
 ** emxPLCPartBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.6.2.2.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */
import matrix.db.*;
import matrix.util.StringList;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.productline.*;
import com.matrixone.apps.common.Company;
//Added by Vibhu,Enovia MatrixOne for Bug 311589 on 11/8/2005
import com.matrixone.apps.domain.util.FrameworkUtil;

/**
 * This JPO class has some method pertaining to Part admin type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxPLCPartBase_mxJPO extends emxDomainObject_mxJPO
{

 /**
 *Alias for string current.
 */
 protected static final String CURRENT = DomainConstants.SELECT_CURRENT;
 /**
 *Alias for emxProduct.PartAddRemove.
 */
 protected static final String KEY_FIRST_PART = "emxProduct.PartAddRemove";
 /**
 *Alias for AllowedStates.
 */
 protected static final String KEY_LAST_PART = "AllowedStates";
 /**
 *Alias for dot.
 */
 protected static final String DOT = ".";
 /** A string constant with the value field_display_choices. */
 protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";
 /** A string constant with the value field_choices. */
 protected static final String FIELD_CHOICES = "field_choices";

 // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
 private final static String RELATIONSHIP_NAME = "relationship";


 /**
  * Default Constructor.
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  emxPLCPartBase_mxJPO (Context context, String[] args) throws Exception
  {
    super(context, args);
  }

 /**
  * Main entry point.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @return an integer status code (0 = success)
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  public int mxMain(Context context, String[] args) throws Exception
  {
    if (!context.isConnected()){
      String sContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
      throw  new Exception(sContentLabel);
    }
    return 0;
  }

 /**
  * Method call to get all the products in the data base.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @return Object - MapList containing the id of Product objects
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAllParts(Context context, String[] args) throws Exception
  {
    //Calls the protected method to retrieve the data
    MapList objectList = expandForParts(context,args,DomainConstants.EMPTY_STRING);
    return objectList;
  }

 /**
  * Returns the owned parts value.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds Hashmap containing the object id.
  * @return Maplist containing the ids of Parts.
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getOwnedParts(Context context, String[] args) throws Exception
  {
    //Calls the protected method to retrieve the data
    String whereCondition = "owner== \""+context.getUser() +"\"";
    MapList objectList = expandForParts(context,args,whereCondition);
    return objectList;
  }
    /**
     * This method is used to check if atleast one object is connected through specific relationship, with the expand Limit to 1.
	 *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - id of the business object
     *        1 - side side of expand (from/to)
     *        2 - relationship name
     * @return String true - if the one object is connected
     *				  false - if not even a single object is connected
     * @throws Exception if the operation fails
     * @since AEF BX3-HFx
     */

    public String hasRelationship(Context context, String args[]) throws Exception
    {
        
		String strMqlCmd1 = "expand bus $1 $2 relationship $3  recurse to 1 limit 1 dump";
		String strResult = MqlUtil.mqlCommand(context, strMqlCmd1, true,args[0],args[1],args[2]);

		if ((strResult.trim()).length() > 0)
          return "true";
        return "false";
    }


 /**
  * Method call to get all the parts in the data base connect to the relationship specified.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @param strWhereCondition - string containing the where condition
  * @return Object - MapList containing the id of parts objects
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  protected MapList expandForParts(Context context, String args[], String strWhereCondition)  throws Exception
  {
    // get parent object id
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strParentId = (String)programMap.get("objectId");

        
        // to check if the Technical Feature has the Design Variant or not.
        DomainObject domFeature = new DomainObject(strParentId);
        String strListDesignVariants = "false";
        if(domFeature.isKindOf(context,ProductLineConstants.TYPE_FEATURES)){
            String[] argsTemp = new String[3];
            argsTemp[0] = strParentId;
            argsTemp[1] = "from";
            argsTemp[2] = PropertyUtil.getSchemaProperty(context,"relationship_VariesBy");
            strListDesignVariants = (String)hasRelationship(context,argsTemp);
            
        }
        
        

        setId(strParentId);
        // Maplist to be returned
        MapList mapBusIds = new MapList();
        String strObjPattern = null;
        String strRelPattern = null;
        StringList objSelects = null;
        StringList relSelects = null;
        short sRecurse = 1;
        // For list page under Feature and Product
        // setting the parameters
  		String ruleComplexitySelect = "to["+ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.attribute["+PropertyUtil.getSchemaProperty(context,"attribute_RuleComplexity")+"]";
        if(strWhereCondition!=DomainConstants.EMPTY_STRING) {
        //System.out.println("here");
        strWhereCondition = "from["+ProductLineConstants.RELATIONSHIP_GBOM_TO + "].to.owner ==\""+context.getUser()+"\"";
      } else
        strWhereCondition = null;

        strObjPattern = DomainConstants.QUERY_WILDCARD;
        strRelPattern = ProductLineConstants.RELATIONSHIP_GBOM_FROM+","+ProductLineConstants.RELATIONSHIP_GBOM_TO;
        objSelects = new StringList();
        objSelects.add("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.id");
        objSelects.add("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.type");
        objSelects.add("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.owner");
        objSelects.add(DomainConstants.SELECT_ID);
        objSelects.add(ruleComplexitySelect);
        objSelects.add("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.name");        



        relSelects = new StringList("to.from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].id");         // parameters being querried

        // making the querry
        mapBusIds = getRelatedObjects(context, strRelPattern,
        strObjPattern, objSelects, relSelects, true, true, sRecurse,
        strWhereCondition, DomainConstants.EMPTY_STRING);
        // Tweeking the map keys for display in Table jsp
        Hashtable htTempTable = null;
        String strTemp1 = null;
        String strTemp1a = null;
        String strTemp2 = null;
        String strTemp3 = null;

        for (int i = 0; i < mapBusIds.size(); i++) {
          htTempTable = (Hashtable)mapBusIds.get(i);
          strTemp1 = (String)htTempTable.get(objSelects.get(0));              // object id of feature
          strTemp1a = (String)htTempTable.get(objSelects.get(3));
          strTemp2 = (String)htTempTable.get(relSelects.get(0));              // rel id
          strTemp3 = (String)htTempTable.get(objSelects.get(5));              // part name
          if (strTemp1 != null && strTemp2 != null){
           // the values supported by Table jsp
            htTempTable.put(DomainConstants.SELECT_ID, strTemp1);
            htTempTable.put(DomainConstants.SELECT_TYPE, (String)htTempTable.get(objSelects.get(1)));
            htTempTable.put(DomainConstants.SELECT_RELATIONSHIP_ID, strTemp2);
            htTempTable.put(DomainConstants.SELECT_NAME, strTemp3);            
            htTempTable.put("gbomID", strTemp1a);

            if(htTempTable.keySet().contains(ruleComplexitySelect)){
            	htTempTable.put("ruleComplexity", htTempTable.get(ruleComplexitySelect));
			}else{
                if(strListDesignVariants.equalsIgnoreCase("true")){
                    htTempTable.put("ruleComplexity", "Simple");
                }else{
                    htTempTable.put("ruleComplexity", "Complex");
                }
			}
            // removing the redundancy
            htTempTable.remove(objSelects.get(0));
            htTempTable.remove(relSelects.get(0));
         }
          // in case there are independent intemediate type Feature List (for error handling)
          else {
        	   //Added for IR-039787V6R2011
                mapBusIds.remove(i--);
          }
        }
         //System.out.println("mapBusIds ="+mapBusIds);
    return  mapBusIds;
  }


 /* Modified by <Tanmoy Chatterjee> On <22nd May 2003>
    For the following Bug
    <Even when a feature is in preliminary state parts can be added to it.>
    Fixed as Follows:
    <added the following functions and updated the tool.xls>
*/

 /**
  * Method call to check whether the product/feature is in design engineering state.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @return Object - A Boolean object
  * @throws Exception if the operation fails
  * @since ProductCentral 10-0-0-0
  * @grade 0
  */
  public Boolean linkDisplay(Context context, String[] args) throws Exception{
    HashMap argumentMap = (HashMap) JPO.unpackArgs(args);
      String strObjectId = (String)argumentMap.get("objectId");
      strObjectId = strObjectId.trim();
      setId(strObjectId);
      String strType = getInfo(context,"type");

      //get the current state of the object
      String strCurrentState = getInfo(context,CURRENT);
      strCurrentState = removeSpaces(strCurrentState);
      //get the default policy of the type->product or features
      String strPolicy = getDefaultPolicy(context,strType);
      strPolicy = removeSpaces(strPolicy);
    //forming the key which will be present in the emxProduct.properties
    StringBuffer sKey = new StringBuffer(70);
      sKey  = sKey.append(KEY_FIRST_PART).append(DOT).append(strPolicy).append(DOT).append(KEY_LAST_PART);
      String strKey = sKey.toString();
    //get the Locale in the present context
    String strLocale = context.getSession().getLanguage();
    //get the value corresponding to the key from the emxProductcentral.properties file
    String strAllowedStates = EnoviaResourceBundle.getProperty(context, "ProductLine",strKey,strLocale);
    //check if the current state of the object is amongst the not allowed states
    if((strAllowedStates.indexOf(strCurrentState))!=-1){
      //the current state is amongst the allowed state. in that case return true
      //this true will be used by the emxTable.jsp and the create new link will be displayed
      Boolean b = new Boolean(true);
      return b;
    }else{
      //the link will be displayed by the emxTable.jsp
      Boolean b = new Boolean(false);
      return b;
    }

  }



/**
  * Method call to remove spaces fmor a string.
  *
  * @param strInput string from which spaces are to be removed
  * @return outputstring
  * @throws Exception if the operation fails
  * @since ProductCentral 10-0-0-0
  * @grade 0
  */
  protected String removeSpaces(String strInput) throws Exception{

    StringBuffer sOutput = new StringBuffer(25);
    StringTokenizer st = new StringTokenizer(strInput);
    while (st.hasMoreTokens()) {
             sOutput = sOutput.append(st.nextToken());
    }
    return   sOutput.toString();

  }

/**
* Method displays the edit link for EBOMs
*
* @param context the eMatrix <code>Context</code> object
* @param args - Holds the parameters passed from the calling method
* @throws Exception if the operation fails
* @since ProductCentral 10-5-1-2
* @grade 0
*/

public Vector getEBOMEditIcon(Context context, String[] args)
   throws Exception
   {
      Vector columnValuesVector   = new Vector();
      Map programMap              = (Map) JPO.unpackArgs(args);
      Map paramMap                = (Map) programMap.get("paramList");
      MapList objectList          = (MapList)programMap.get("objectList");
      Iterator objectListItr      = objectList.iterator();
      String strFileComment       = "";
      String partObjectId         = "";
      String gbomToId             = "";

      String emxSuiteDirectory    = (String)paramMap.get("SuiteDirectory");
      String jsTreeID             = (String) paramMap.get("jsTreeID");
      String suiteKey             = (String) paramMap.get("suiteKey");
      String parentOID            = (String)paramMap.get("objectId");
      String statusGif            = "";


      //loop through all the files
      while(objectListItr.hasNext())
      {
          String strURL = "../common/emxForm.jsp?form=PLCEBOMDetails&mode=Edit&formHeader=emxProduct.Heading.EditPartAttributes&HelpMarker=emxhelpproducteditallparts&emxSuiteDirectory="+emxSuiteDirectory+"&parentOID="+parentOID+"&jsTreeID="+jsTreeID+"&suiteKey="+suiteKey;
          Map objectMap       = (Map) objectListItr.next();
          partObjectId        = (String) objectMap.get(DomainConstants.SELECT_ID);
          gbomToId            = (String) objectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
          DomainObject domPartObj = new DomainObject(partObjectId);

          MapList     mapListBusIds       = new MapList();
          String      strObjPattern   = null;
          String      strRelPattern   = null;
          StringList  objSelects      = new StringList();

          objSelects.add("to["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]."+DomainConstants.SELECT_ID);
          objSelects.add("to["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"].from."+DomainConstants.SELECT_ID);

          String strWhereCondition    = "from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].id =='"+gbomToId+"'";
          mapListBusIds = domPartObj.getRelatedObjects(context,
                                                      "*",
                                                      ProductLineConstants.TYPE_GBOM,
                                                      objSelects,
                                                      null,
                                                      true,
                                                      true,
                                                      (short)1,
                                                      strWhereCondition,
                                                      DomainConstants.EMPTY_STRING);
          Map mapBusIds = (Map)mapListBusIds.get(0);
          String relId = (String)mapBusIds.get("to["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"]."+DomainConstants.SELECT_ID);
          strURL += "&relId="+relId+"&objectId="+partObjectId + "&postProcessURL=../productline/EBOMEditPostProcess.jsp";
          statusGif = "<a href=\"javascript:emxTableColumnLinkClick(\'" + strURL + "\',\'570\',\'520\',\'true\',\'popup\',\'\')\"><img src=\"images/iconActionEdit.gif\" border=0 alt=\"Edit\"></a>&nbsp;";
          columnValuesVector.add(statusGif);

      }//End of while loop

      return columnValuesVector;
 }


//Added by Sandeep, Enovia MatrixOne for editing of EBOM attributes in GBOM list page for Bug # 310491

 /**
  * This method is used to update the modified Reference Designators
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return MapList of all the Feature objects
  * @since ProductCentral10.6 SP1
  */

 public Boolean updateReferenceDesignator(Context context, String[] args) throws Exception
   {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;


        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");

        String strrelId  = (String)paramMap.get("relId");
        String objId = (String)paramMap.get("objectId");

        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        String strnewRefDesValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);

        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, strnewRefDesValue);

        return new Boolean(true);
   }

/**
  * This method is used to update the modified F/N Attribute
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return MapList of all the Feature objects
  * @since ProductCentral10.6 SP1
  */

 public Boolean updateFindNumber(Context context, String[] args) throws Exception
   {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;


        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");

        String strrelId  = (String)paramMap.get("relId");
        String objId = (String)paramMap.get("objectId");

        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        String strnewFindNumberValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);

        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, strnewFindNumberValue);

        return new Boolean(true);
   }

 /**
   * This method is used to update the modified Quantity Attribute
   * @param context The ematrix context object.
   * @param String[] The args .
   * @return MapList of all the Feature objects
   * @since ProductCentral10.6 SP1
   */

  public Boolean updateQuantity(Context context, String[] args) throws Exception
    {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;


        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");

        String strrelId  = (String)paramMap.get("relId");
        String objId = (String)paramMap.get("objectId");

        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        String strnewQuantityValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);

        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_QUANTITY, strnewQuantityValue);

        return new Boolean(true);
   }



/**
   * This method is used to update the modified Component Location Attribute
   * @param context The ematrix context object.
   * @param String[] The args .
   * @return MapList of all the Feature objects
   * @since ProductCentral10.6 SP1
   */

  public Boolean updateComponentLocation(Context context, String[] args) throws Exception
    {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;


        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");

        String strrelId  = (String)paramMap.get("relId");
        String objId = (String)paramMap.get("objectId");

        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        String strnewComponentLocationValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);

        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_COMPONENT_LOCATION, strnewComponentLocationValue);

        return new Boolean(true);
   }



/**
   * This method is used to update the modified Usage Attribute
   * @param context The ematrix context object.
   * @param String[] The args .
   * @return MapList of all the Feature objects
   * @since ProductCentral10.6 SP1
   */

  public Boolean updateUsage(Context context, String[] args) throws Exception
    {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;


        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");

        String strrelId  = (String)paramMap.get("relId");
        String objId = (String)paramMap.get("objectId");

        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        String strnewUsageValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);

        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_USAGE, strnewUsageValue);

        return new Boolean(true);
   }


//End of Add by Sandeep, Enovia MatrixOne for Bug # 310491

/**
  * This method is used to retrieve the  F/N Attribute
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return List of attribute values
  * @since ProductCentral10.6 SP1
  */

 public List getFindNumber(Context context, String[] args) throws Exception
 {
    String strIntmdtGBOMId="";
    String strTempGBOMFromRelID;
    StringBuffer sb;
    String strTemp = "<a TITLE=";
    String strEndHrefTitle = ">";
    String strEndHref = "</a>";
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");
    List lstFindNumber = new StringList();
    MapList lstObjectIdsList = (MapList) programMap.get("objectList");
    Map tempMap;

    // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
    String strrelId = DomainConstants.EMPTY_STRING;
    String objId = DomainConstants.EMPTY_STRING;
    String strRelType = DomainConstants.EMPTY_STRING;

    for(int j=0; j<lstObjectIdsList.size();j++)
    {
        tempMap = (Map)lstObjectIdsList.get(j);
        // Modified by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        //String str = (String)tempMap.get(DomainConstants.SELECT_TYPE);
        strrelId  = (String)tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        objId = (String)tempMap.get(DomainConstants.SELECT_ID);

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        strRelType = (String)tempMap.get(RELATIONSHIP_NAME);

        sb = new StringBuffer();
        sb = sb.append(strTemp);

        // Modified by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        if (!strRelType.equals(ProductLineConstants.RELATIONSHIP_GBOM_TO))
        {
            lstFindNumber.add("");
        }

        else
        {
            DomainObject domObj = new DomainObject(objId);
            StringList slTempId = new StringList(DomainConstants.SELECT_ID);
            StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            MapList mlRelObj = new MapList();

            mlRelObj = domObj.getRelatedObjects(context,
                                                ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                                ProductLineConstants.TYPE_GBOM,
                                                slTempId,
                                                slTempRel,
                                                true,
                                                false,
                                                (short)1,
                                                DomainConstants.EMPTY_STRING,
                                                DomainConstants.EMPTY_STRING);


            for (int i=0; i<mlRelObj.size(); i++)
            {

                Map mapListObj = (Map) mlRelObj.get(i);
                String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                if (strTempMapRelID.equals(strrelId) )
                {
                    strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                    break;
                }
            }

            DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
            StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
            StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            MapList mlRelGBOMFromObj = new MapList();

            mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                            ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                            DomainConstants.QUERY_WILDCARD,
                                                            slGBOMFromId,
                                                            slGBOMFromRelID,
                                                            true,
                                                            false,
                                                            (short)1,
                                                            DomainConstants.EMPTY_STRING,
                                                            DomainConstants.EMPTY_STRING);

            Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

            String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);
            String strFindNumber = domRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER);

            sb =  sb.append("\""+strFindNumber+"\"")
                            .append(strEndHrefTitle)
                            .append(strFindNumber)
                            .append(strEndHref);

            lstFindNumber.add (sb.toString());
        }
    }
    return lstFindNumber;
}


//Modified by Yukthesh, Enovia MatrixOne on Nov 21,2005 to remove tooltip and perform validations.
/**
  * This method is used to retrieve the  Quantity Attribute
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return List of attribute values
  * @since ProductCentral10.6 SP1
  */

 public List getQuantity(Context context, String[] args) throws Exception
   {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;

        StringBuffer sb;

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        List lstQuantity = new StringList();
        MapList lstObjectIdsList = (MapList) programMap.get("objectList");
        Map tempMap;

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        String strrelId = DomainConstants.EMPTY_STRING;
        String objId = DomainConstants.EMPTY_STRING;
        String strRelType = DomainConstants.EMPTY_STRING;

        for(int j=0; j<lstObjectIdsList.size();j++)
        {
        tempMap = (Map)lstObjectIdsList.get(j);
        //String str = (String)tempMap.get(DomainConstants.SELECT_TYPE);
        strrelId  = (String)tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        objId = (String)tempMap.get(DomainConstants.SELECT_ID);

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        strRelType = (String)tempMap.get(RELATIONSHIP_NAME);

        sb = new StringBuffer();

        // Modified by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        if (!strRelType.equals(ProductLineConstants.RELATIONSHIP_GBOM_TO))
        {
            lstQuantity.add("");
        }

        else
        {
        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);
        String strQuantity = domRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_QUANTITY);

        sb =  sb.append(strQuantity);
        lstQuantity.add (sb.toString());
        }
    }

        return lstQuantity;
   }

/**
  * This method is used to retrieve the  Component Location Attribute
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return List of attribute values
  * @since ProductCentral10.6 SP1
  */

 public List getComponentLocation(Context context, String[] args) throws Exception
   {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;

        StringBuffer sb;
        String strTemp = "<a TITLE=";
        String strEndHrefTitle = ">";
        String strEndHref = "</a>";

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        List lstComponentLocation = new StringList();
        MapList lstObjectIdsList = (MapList) programMap.get("objectList");
        Map tempMap;

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        String strrelId = DomainConstants.EMPTY_STRING;
        String objId = DomainConstants.EMPTY_STRING;
        String strRelType = DomainConstants.EMPTY_STRING;

        for(int j=0; j<lstObjectIdsList.size();j++)
        {
        tempMap = (Map)lstObjectIdsList.get(j);
        //String str = (String)tempMap.get(DomainConstants.SELECT_TYPE);
        strrelId  = (String)tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        objId = (String)tempMap.get(DomainConstants.SELECT_ID);

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        strRelType = (String)tempMap.get(RELATIONSHIP_NAME);

        sb = new StringBuffer();
        sb = sb.append(strTemp);

        // Modified by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        if (!strRelType.equals(ProductLineConstants.RELATIONSHIP_GBOM_TO))
        {
            lstComponentLocation.add("");
        }

        else
        {
        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);
        String strComponentLocation = domRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);

        sb =  sb.append("\""+strComponentLocation+"\"")
                        .append(strEndHrefTitle)
                        .append(strComponentLocation)
                        .append(strEndHref);

        lstComponentLocation.add (sb.toString());
        }
    }

        return lstComponentLocation;
   }

/**
  * This method is used to retrieve the  Reference Designator Attribute
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return List of attribute values
  * @since ProductCentral10.6 SP1
  */

 public List getReferenceDesignator(Context context, String[] args) throws Exception
   {
        String strIntmdtGBOMId="";
        String strTempGBOMFromRelID;
        StringBuffer sb;
        String strTemp = "<a TITLE=";
        String strEndHrefTitle = ">";
        String strEndHref = "</a>";

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        matrix.util.List lstReferenceDesignator = new StringList();
        MapList lstObjectIdsList = (MapList) programMap.get("objectList");
        Map tempMap;

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        String strrelId = DomainConstants.EMPTY_STRING;
        String objId = DomainConstants.EMPTY_STRING;
        String strRelType = DomainConstants.EMPTY_STRING;

        for(int j=0; j<lstObjectIdsList.size();j++)
        {
        tempMap = (Map)lstObjectIdsList.get(j);
        //String str = (String)tempMap.get(DomainConstants.SELECT_TYPE);
        strrelId  = (String)tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        objId = (String)tempMap.get(DomainConstants.SELECT_ID);

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        strRelType = (String)tempMap.get(RELATIONSHIP_NAME);

        sb = new StringBuffer();
        sb = sb.append(strTemp);

        // Modified by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        if (!strRelType.equals(ProductLineConstants.RELATIONSHIP_GBOM_TO))
        {
            lstReferenceDesignator.add("");
        }

        else
        {
        DomainObject domObj = new DomainObject(objId);
        StringList slTempId = new StringList(DomainConstants.SELECT_ID);
        StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelObj = new MapList();

        mlRelObj = domObj.getRelatedObjects(context,
                                            ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                            ProductLineConstants.TYPE_GBOM,
                                            slTempId,
                                            slTempRel,
                                            true,
                                            false,
                                            (short)1,
                                            DomainConstants.EMPTY_STRING,
                                            DomainConstants.EMPTY_STRING);


        for (int i=0; i<mlRelObj.size(); i++)
        {

            Map mapListObj = (Map) mlRelObj.get(i);
            String strTempMapRelID = (String)mapListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strTempMapRelID.equals(strrelId) )
            {
                strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                break;
            }
        }

        DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
        StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
        StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mlRelGBOMFromObj = new MapList();

        mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                        ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        slGBOMFromId,
                                                        slGBOMFromRelID,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);

        Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);

        String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);
        String strReferenceDesignator = domRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);

        sb =  sb.append("\""+strReferenceDesignator+"\"")
                        .append(strEndHrefTitle)
                        .append(strReferenceDesignator)
                        .append(strEndHref);

        lstReferenceDesignator.add (sb.toString());
        }
    }

        return lstReferenceDesignator;
   }

/**
  * This method is used to get the range values for Usage Attribute
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return TreeMap with the range values
  * @since ProductCentral10.6 SP1
  */
public TreeMap getRangeValuesForUsage(Context context, String[] args) throws Exception
{
    String strAttributeName = ProductLineConstants.ATTRIBUTE_USAGE;
    TreeMap rangeMap = new TreeMap();
    matrix.db.AttributeType attribName =
        new matrix.db.AttributeType(strAttributeName);
    attribName.open(context);
    // actual range values
    List attributeRange = attribName.getChoices();
    //display range values
    List attributeDisplayRange =
    i18nNow.getAttrRangeI18NStringList(ProductLineConstants.ATTRIBUTE_USAGE,
                                       (StringList)attributeRange,
                                       context.getSession().getLanguage());
    for(int i=0;i<attributeRange.size();i++)
    {
        rangeMap.put((String)attributeDisplayRange.get(i),
                     (String)attributeRange.get(i));
    }

    return  rangeMap;

}//end of the method

/**
  * This method is used to retrieve the Usage Attribute for the GBOM
  * @param context The ematrix context object.
  * @param String[] The args .
  * @return List of attribute values
  * @since ProductCentral10.6 SP1
  */

 public List getUsage (Context context, String[] args)
     throws Exception
 {
    List columnVals = new StringList();
    String strIntmdtGBOMId="";
    String strTempGBOMFromRelID;
    StringBuffer sb;
    String strTemp = "<a TITLE=";
    String strEndHrefTitle = ">";
    String strEndHref = "</a>";

    char chBlank = ' ';
    char chUnderScore = '_';


    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)paramMap.get("objectList");
    Iterator ik = objList.iterator();
    String strUsage = "";
    String strLanguage = context.getSession().getLanguage();
    String strUsageValueKey = DomainConstants.EMPTY_STRING;
    String strUsageDisplay = DomainConstants.EMPTY_STRING;

    // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
    String strrelId = DomainConstants.EMPTY_STRING;
    String objId = DomainConstants.EMPTY_STRING;
    String strRelType = DomainConstants.EMPTY_STRING;

    while (ik.hasNext())
    {
        Map m = (Map) ik.next();
        strrelId  = (String)m.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        objId = (String)m.get(DomainConstants.SELECT_ID);
        //String strType = (String)m.get(DomainConstants.SELECT_TYPE);

        // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        strRelType = (String)m.get(RELATIONSHIP_NAME);

        sb = new StringBuffer();
        sb = sb.append(strTemp);

        // Modified by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
        if (strRelType.equals(ProductLineConstants.RELATIONSHIP_GBOM_TO))
        {
            DomainObject domObj = new DomainObject(objId);
            StringList slTempId = new StringList(DomainConstants.SELECT_ID);
            StringList slTempRel = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            MapList mlRelObj = new MapList();
            mlRelObj = domObj.getRelatedObjects(context,
                                               ProductLineConstants.RELATIONSHIP_GBOM_TO,
                                               ProductLineConstants.TYPE_GBOM,
                                               slTempId,
                                               slTempRel,
                                               true,
                                               false,
                                               (short)1,
                                               DomainConstants.EMPTY_STRING,
                                               DomainConstants.EMPTY_STRING);


            for (int i=0; i<mlRelObj.size(); i++)
            {
                Map mapListObj = (Map) mlRelObj.get(i);
                String strTempMapRelID = (String)mapListObj.
                                            get(DomainConstants.SELECT_RELATIONSHIP_ID);
                if (strTempMapRelID.equals(strrelId) )
                {
                     strIntmdtGBOMId = (String)mapListObj.get(DomainConstants.SELECT_ID);
                     break;
                }
            }
                DomainObject domObjGBOM = new DomainObject(strIntmdtGBOMId);
                StringList slGBOMFromId = new StringList(DomainConstants.SELECT_ID);
                StringList slGBOMFromRelID = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                MapList mlRelGBOMFromObj = new MapList();
                mlRelGBOMFromObj = domObjGBOM.getRelatedObjects(context,
                                                                ProductLineConstants.RELATIONSHIP_GBOM_FROM,DomainConstants.QUERY_WILDCARD,slGBOMFromId,
                                                                slGBOMFromRelID,
                                                                true,
                                                                false,
                                                                (short)1,
                                                                DomainConstants.EMPTY_STRING,
                                                                DomainConstants.EMPTY_STRING);

                Map mapGBOMFromListObj = (Map) mlRelGBOMFromObj.get(0);
                String strGBOMFromRelID = (String)mapGBOMFromListObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                DomainRelationship domRel = new DomainRelationship(strGBOMFromRelID);
                strUsage = domRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_USAGE);
                strUsage = strUsage.replace(chBlank, chUnderScore);
                strUsageValueKey = "emxFramework.Range.Usage." + strUsage.toString();
                strUsageDisplay = EnoviaResourceBundle.getProperty(context,"Framework",strUsageValueKey,strLanguage);

                sb =  sb.append("\""+strUsageDisplay+"\"")
                        .append(strEndHrefTitle)
                        .append(strUsageDisplay)
                        .append(strEndHref);

                columnVals.add(sb.toString());


     }
     else{

         columnVals.add("");
     }


    }

    return columnVals;
}
    //Begin of Add by Vibhu,Enovia MatrixOne for Bug311589 on 11/8/2005
    /**
      * This method is used to get the range values for Usage Attribute
      * @param context The ematrix context object.
      * @param String[] The args .
      * @return HashMap with the range values
      * @since ProductCentral10.6 SP1
      */
    public HashMap getRangeValuesUsageForGBOM(Context context, String[] args) throws Exception
    {
        String strAttributeName = ProductLineConstants.ATTRIBUTE_USAGE;
        HashMap rangeMap = new HashMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(strAttributeName);
        attribName.open(context);
        // actual range values
        List attributeRange = attribName.getChoices();
        //display range values
        List attributeDisplayRange =
        i18nNow.getAttrRangeI18NStringList(ProductLineConstants.ATTRIBUTE_USAGE,
                                           (StringList)attributeRange,
                                           context.getSession().getLanguage());
        StringList slDisplayList = new StringList();
        StringList slOriginalList = new StringList();
        for(int i=0;i<attributeRange.size();i++)
        {
            slDisplayList.add((String)attributeDisplayRange.get(i));
            slOriginalList.add((String)attributeRange.get(i));
        }
        rangeMap.put("field_choices",slOriginalList);
        rangeMap.put("field_display_choices",slDisplayList);
        return  rangeMap;

    }//end of the method

    /**
    * updates the Usage value of the GBOM From Relationship Attribute
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
     *        0 - HashMap containing one Map entry for the key "paramMap"
     *      This Map contains the arguments passed to the jsp which called this method.
    * @return integer 0 if the operation is successful.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6.SP1
    */

    public int setUsageForGBOM(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strRelId = (String) paramMap.get("relId");
        String strNewValue = (String) paramMap.get("New Value");
        String strOldValue = (String) paramMap.get("Old value");
        DomainRelationship domRel = new DomainRelationship(strRelId);
        domRel.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_USAGE, strNewValue);
        return 0;
    }

    /**
    * get Usage for the GBOM From Relationship.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
     *        0 - HashMap containing one Map entry for the key "paramMap"
     *      This Map contains the arguments passed to the jsp which called this method.
    * @return StringList (Usage Attribute for the GBOM From Relationship Object.)
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6.SP1
    */

    public StringList getUsageForGBOM(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        // Get the required parameter values from  "programMap" - as required
        String strRelId = (String) paramMap.get("relId");

        // Initialize the variables
        StringList slUsageList = new StringList();
        String language = context.getSession().getLanguage();
        DomainRelationship domRel = new DomainRelationship(strRelId);
        String strUsageValue = domRel.getAttributeValue(context,ProductLineConstants.ATTRIBUTE_USAGE);
        strUsageValue = FrameworkUtil.findAndReplace(strUsageValue," ","_");
        strUsageValue = EnoviaResourceBundle.getProperty(context, "Framework",
                                            "emxFramework.Range.Usage."+strUsageValue,language);
        slUsageList.add(strUsageValue);
        return slUsageList;
    }
    //End of Add by Vibhu,Enovia MatrixOne for Bug311589 on 11/8/2005

}
