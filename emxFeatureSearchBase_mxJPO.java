/*
*  emxFeatureSearchBase.java
*
*  JPO for fetching the relevent Business objects based on the Type selected
*
* Copyright (c) 1992-2016 Dassault Systemes.
*
* All Rights Reserved.
* This program contains proprietary and trade secret information of
* MatrixOne, Inc.  Copyright notice is precautionary only and does
* not evidence any actual or intended publication of such program.
*
* static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.41.2.3.1.1.1.4 Tue Dec 23 11:21:01 2008 GMT ds-dpathak Experimental$;
*/



import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.FindLikeInfo;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineConstants;

/*****************************************************************************************************
* Functionality Description: This JPO is used to retreve the business Objects, based on the type.
* @author Enovia MatrixOne
* @version AEF 10.5 - Copyright (c) 2004, MatrixOne, Inc.
*
*****************************************************************************************************/


public class emxFeatureSearchBase_mxJPO extends emxProductSearch_mxJPO
{

/** A string constant with the value emxProduct.common.Marketing. */
    protected static final String FEATURE_MARKETING = "emxProduct.common.Marketing";
/**
* The default constructor.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments.
* @throws Exception if the operation fails
* @since ProductCentral 10.0.0.0
.*/



public emxFeatureSearchBase_mxJPO (Context context, String[] args) throws Exception
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
* @since ProductCentral 10-0-0-0
*/
public int mxMain(Context context, String[] args)
throws Exception
{
if (!context.isConnected()){
   String sContentLabel = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
   throw  new Exception(sContentLabel);
 }
return 0;
}






/**
* This method returns the default policy states of the type "Product Configuration".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getProductConfigurationStates(Context context, String[] args)
throws Exception
{

return getStates(context, ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION);
}




/**
* This method returns the default policy states.
*
* @param context the eMatrix <code>Context</code> object
* @param strType holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
protected static String getStates(Context context, String strType)
throws Exception
{

Map defaultMap = mxType.getDefaultPolicy(context, strType, false);
String strPolicy = (String)defaultMap.get("name");


BusinessType btType = new BusinessType(strType,context.getVault());
btType.open(context, false);

//To get the Find Like information of the business type selected
FindLikeInfo fLikeObj = btType.getFindLikeInfo(context);
List list = fLikeObj.getStates();

String strLocale = context.getSession().getLanguage();
String strAllStates = EnoviaResourceBundle.getProperty(context,"Configuration",ALL_STATES,strLocale);
StringBuffer sb = new StringBuffer(70);

sb.append("<select name=\"State\"> <option value=\"*\">");
sb.append(strAllStates);
sb.append("</option>");
for (int i=0; i<list.size(); i++) {
  String strState = (String)list.get(i);
  sb.append("<option value=\"");
  sb.append(strState + "\">");
  sb.append(i18nNow.getStateI18NString(strPolicy, strState ,strLocale));
  sb.append("</option>");
}
sb.append("</select>");


return sb.toString();

}




/**
* To obtain the Product Configurations.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap with following arguments:
*            queryLimit
*            Name
*            Revision
*            Description
*            State
*            OwnerDisplay
*            OriginatorDisplay
*            vaultOption
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since AEF 9.6.0.0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getProductConfigurations(Context context, String[] args)
throws Exception
{
MapList mapList = null;
Map programMap = (Map) JPO.unpackArgs(args);
String productId = (String)programMap.get("Product");
String objectId = (String) programMap.get("objectId");
 // To read the input Search crieteria from the Product Configurations form
  short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION ;

String strName = (String)programMap.get("Name");
String forAddExisting = (String)programMap.get("forAddExisting");

  if ( strName==null || strName.equals("")) {
    strName = SYMB_WILD;
  }


  String strRadio = (String)programMap.get("radio");
  String strRevision = null;
  if (strRadio != null) {
    if (strRadio.equals("input")) {
      strRevision = (String)programMap.get("Revision");
      strRevision = strRevision.trim();
    }
    else if (strRadio.equals("latestReleased")) {
      
    }
    else if (strRadio.equals("latest")) {
      
    }
  }
  if ((strRevision == null) || (strRevision.equals(""))) {
    strRevision = SYMB_WILD;
  }


  String strDesc = (String)programMap.get("Description");

  String strState = (String)programMap.get("State");
    
  String strOwner = (String)programMap.get("OwnerDisplay");

  if (strOwner==null || strOwner.equals("")) {
    strOwner = SYMB_WILD;
  }


  /*  If none of the Vaults are selected, add a *, search in all Vaults */
  String strVault = null;
  String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption != null && (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS)))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

  StringList select = new StringList(1);
  select.addElement(DomainConstants.SELECT_ID);
  //
  boolean start = true;
  StringBuffer sbWhereExp = new StringBuffer(120);

if ((strName!=null) && (!strName.equals(SYMB_WILD)) && (!strName.equals("")) ) {
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    }else {
        sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_NAME);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strName);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }

  if ((strDesc!=null) && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    }else {
        sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strDesc);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }

  if ( (strOwner!=null) && (!strOwner.equals(SYMB_WILD)) && (!strOwner.equals("")) ){
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    } else {
      sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_OWNER);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strOwner);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }

  if ((strState != null) && (!strState.equals(SYMB_WILD)) && (!strState.equals(""))) {
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    } else {
      sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strState);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }
    //Begin of Added by Vibhu,Enovia MatrixOne for Bug 311780 on 11/10/2005
    StringBuffer sbWhereForProduct = new StringBuffer();
    sbWhereForProduct.append(SYMB_OPEN_PARAN);
    sbWhereForProduct.append(DomainConstants.SELECT_CURRENT);
    sbWhereForProduct.append(SYMB_NOT_EQUAL);
    sbWhereForProduct.append(SYMB_QUOTE);
    sbWhereForProduct.append(ConfigurationConstants.STATE_ACTIVE);
    sbWhereForProduct.append(SYMB_QUOTE);
    sbWhereForProduct.append(SYMB_AND);
    sbWhereForProduct.append(DomainConstants.SELECT_CURRENT);
    sbWhereForProduct.append(SYMB_NOT_EQUAL);
    sbWhereForProduct.append(SYMB_QUOTE);
    sbWhereForProduct.append(ConfigurationConstants.STATE_INACTIVE);
    sbWhereForProduct.append(SYMB_QUOTE);
    sbWhereForProduct.append(SYMB_AND);
    sbWhereForProduct.append(SYMB_ATTRIBUTE);
    sbWhereForProduct.append(SYMB_OPEN_BRACKET);
    sbWhereForProduct.append(ConfigurationConstants.ATTRIBUTE_VALIDATION_STATUS);
    sbWhereForProduct.append(SYMB_CLOSE_BRACKET);
    sbWhereForProduct.append(SYMB_NOT_EQUAL);
    sbWhereForProduct.append(SYMB_QUOTE);
    sbWhereForProduct.append(ConfigurationConstants.RANGE_VALUE_NOT_VALIDATED);
    sbWhereForProduct.append(SYMB_QUOTE);
    sbWhereForProduct.append(SYMB_CLOSE_PARAN);

    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    } else {
      sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(sbWhereForProduct.toString());
    //End of Add by Vibhu,Enovia MatrixOne for Bug 311780 on 11/10/2005

  if( productId!=null && (!productId.equals(SYMB_WILD)) && (!productId.equals(""))) {
  String strFilteredExpression = getFilteredExpression(context,programMap);

  if ( (strFilteredExpression != null) ) {
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    } else {
      sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(strFilteredExpression);
  }

  if (!start) {
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }
DomainObject dObj = new DomainObject(productId);
mapList = dObj.getRelatedObjects(context,
ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION,
ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION,
select,
null,
true,
true,
(short)1,
//Modified by Vibhu,Enovia MatrixOne for Bug 311780 on 11/10/2005
sbWhereForProduct.toString(),
null,
null,
null,
null);
} else {
    if(forAddExisting != null && "true".equalsIgnoreCase(forAddExisting))
    {
        String strProductConfigurationPurpose = PropertyUtil.getSchemaProperty(context,SYMBOLIC_attribute_ProductConfigurationPurpose);
        if (start) {
            sbWhereExp.append(SYMB_OPEN_PARAN);
            start = false;
        }else {
            sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(SYMB_ATTRIBUTE+SYMB_OPEN_BRACKET+strProductConfigurationPurpose+SYMB_CLOSE_BRACKET+SYMB_EQUAL+ATTRIBUTE_ORDER_VALUE);
        sbWhereExp.append(SYMB_CLOSE_PARAN);

        if (!start) {
            sbWhereExp.append(SYMB_CLOSE_PARAN);
        }
        mapList=new MapList();
        MapList mapList1 = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
        DomainObject domParentObj=new DomainObject(objectId);
        MapList mapList2 = domParentObj.getRelatedObjects(context,
                                                            "*",
                                                            ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION,
                                                            select,
                                                            null,
                                                            true,
                                                            true,
                                                            (short)1,
                                                            null,
                                                            null,
                                                            null,
                                                            null,
                                                            null);

        Iterator itr1 = mapList1.iterator();

        while(itr1.hasNext())
        {
            Map map1=(Map)itr1.next();
            String objectId1=(String)map1.get("id");
            boolean isConnected=false;
            Iterator itr2 = mapList2.iterator();

            while(itr2.hasNext())
            {
                Map map2=(Map)itr2.next();
                String objectId2=(String)map2.get("id");
                if(objectId1.equals(objectId2))
                {
                    isConnected=true;
                    break;
                }
            }

            if(isConnected==false)
            {
                mapList.add(map1);
            }
        }
       }
       else
       {
            //Begin of Add by Praveen, Enovia MatrixOne for Bug# 300305 Date: 3/10/2005
            if (!start) {
            sbWhereExp.append(SYMB_CLOSE_PARAN);
            }
            //End of Add by Praveen, Enovia MatrixOne for Bug# 300305 Date: 3/10/2005
            mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
       }
    }
    return mapList;
}


   /**
    * Method to search parts/products for Replace Functionality in the GBOM of a Feature/Product.
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args - Holds the parameters passed from the calling method
    *  When this array is unpacked, arguments corresponding to the following String keys are found:
    *      queryLimit
    *      hdnType
    *      txtName
    *      txtDescription
    *      txtOwner
    *      txtState.
    * @return - Maplist containing the objects found
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6SP1
    */
    public static MapList getGBOMForReplace
    (
            Context context,
            String[] args
    )throws Exception
    {
            MapList mapList = null;//maplist to hold the final output
            try
            {
                //Put the arguments into a MAP for easy handling
                Map programMap = (Map) JPO.unpackArgs(args);
                //get Query Limit
                short sQueryLimit =
                (short) (java
                            .lang
                            .Integer
                            .parseInt((String) programMap.get(QUERY_LIMIT)));
                //Get the type to be Queried.
                //If Null then set it to *
                String strType = (String) programMap.get("hdnType");

                if (strType == null|| strType.equals("")|| SYMB_NULL.equalsIgnoreCase(strType))
                {
                    strType = SYMB_WILD;
                }

                //Get the name pattern.
                //If Null then set it to *
                String strName = (String) programMap.get("txtName");

                if (strName == null|| strName.equals("")|| SYMB_NULL.equalsIgnoreCase(strName))
                {
                    strName = SYMB_WILD;
                }

                /*Get the value of the radio concerned with the revision
                and set the value appropriately.*/
                //If Null then set it to *
                String strRadio = (String)programMap.get("radio");
                String strRevision = null;
                boolean bLatestRevision = false;
                boolean bLatestReleasedRevision = false;
                if (strRadio != null&& !strRadio.equals("")
                    && !(SYMB_NULL.equalsIgnoreCase(strRadio)))
                {
                    if (strRadio.equals("input"))
                    {
                        strRevision = (String)programMap.get("Revision");
                        strRevision = strRevision.trim();
                    }
                    else if (strRadio.equals("latestReleased"))
                    {
                        bLatestReleasedRevision = true;
                    }
                    else if (strRadio.equals("latest"))
                    {
                        bLatestRevision = true;
                    }
                }
                if ((strRevision == null)
                    ||(strRevision.equals(""))
                    ||SYMB_NULL.equalsIgnoreCase(strRevision))
                {
                    strRevision = SYMB_WILD;
                }

                //Get the Description pattern.and Owner Pattern
                //If Owner Null then set it to *
                String strDesc = (String) programMap.get("txtDescription");
                String strOwner = (String) programMap.get("txtOwner");

                if (strOwner == null|| strOwner.equals("")
                    || SYMB_NULL.equalsIgnoreCase(strOwner))
                {
                    strOwner = SYMB_WILD;
                }
                else
                {
                    strOwner = strOwner.trim();
                }

                //Get the state.
                //If Null then set it to *
                String strState = (String) programMap.get("txtState");

                if (strState == null|| strState.equals("")
                    || SYMB_NULL.equalsIgnoreCase(strState))
                {
                    strState = SYMB_WILD;
                }
                else
                {
                    strState = strState.trim();
                }

                //Get the vault selection.
                String strVault = "";
                String strVaultOption = (String) programMap.get(VAULT_OPTION);

                if (strVaultOption != null
                    && !strVaultOption.equals("")
                    &&!(SYMB_NULL.equalsIgnoreCase(strVaultOption)))
                {
                    if (strVaultOption.equalsIgnoreCase(ALL))
                    {
                        strVault = getAllCompanyVaults(context);
                    }
                    else if (strVaultOption.equalsIgnoreCase(DEFAULT))
                    {
                        strVault = context.getVault().getName();
                    }
                    else if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_SELECTED_VAULTS))
                    {
                        strVault = (String) programMap.get("vaults");
                    }
                    else if (strVault == null
                            || strVault.equals("")
                            || SYMB_NULL.equalsIgnoreCase(strVault))
                    {
                        strVault = SYMB_WILD;
                    }
                }
                //Business Select Stringlist which enables selection of the IDs
                StringList slSelect = new StringList(1);
                slSelect.addElement(DomainConstants.SELECT_ID);

                boolean bStart = true;
                StringBuffer sbWhereExp = new StringBuffer(150);

                /*Depending on the values of the fields acquired above; build the where
                expression for the Query*/

                if (strDesc != null
                    && (!strDesc.equals(SYMB_WILD))
                    && (!strDesc.equals(""))
                    && !(SYMB_NULL.equalsIgnoreCase(strDesc)))
                {
                    if (bStart)
                    {
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        bStart = false;
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
                    sbWhereExp.append(SYMB_MATCH);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strDesc);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if (strState != null
                    && (!strState.equals(SYMB_WILD))
                    && (!strState.equals(""))
                    && !(SYMB_NULL.equalsIgnoreCase(strState)))
                {
                    if (bStart)
                    {
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        bStart = false;
                    }
                    else
                    {
                        sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_MATCH);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(strState);
                    sbWhereExp.append(SYMB_QUOTE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if (bLatestReleasedRevision)
                {
                    if (bStart)
                    {
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        bStart = false;
                    }
                    else
                    {
                        sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                    sbWhereExp.append(SYMB_EQUAL);
                    sbWhereExp.append(STATE_RELEASE);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }

                if (bLatestRevision)
                {
                    if (bStart)
                    {
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        bStart = false;
                    }
                    else
                    {
                        sbWhereExp.append(SYMB_AND);
                    }
                    sbWhereExp.append(SYMB_OPEN_PARAN);
                    sbWhereExp.append(DomainConstants.SELECT_REVISION);
                    sbWhereExp.append(SYMB_EQUAL);
                    sbWhereExp.append(REVISION_LAST);
                    sbWhereExp.append(SYMB_CLOSE_PARAN);
                }
                //Get the Filter expression to be added to where expression
                String strFilteredExpression = generateFilterExpression(context,programMap);

                if ((strFilteredExpression != null)
                    && !(SYMB_NULL.equalsIgnoreCase(strFilteredExpression))
                    && !strFilteredExpression.equals(""))
                    {
                        if (bStart)
                        {
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            bStart = false;
                        }
                        else
                        {
                            sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(strFilteredExpression);
                    }

                    if (!bStart)
                    {
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                    }
                    //Where expression creation complete

                    //Query the database
                    mapList =
                            DomainObject.findObjects(
                                    context,
                                    strType,
                                    strName,
                                    strRevision,
                                    strOwner,
                                    strVault,
                                    sbWhereExp.toString(),
                                    "",
                                    true,
                                    (StringList) slSelect,
                                    sQueryLimit);
                    //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
                    if(bLatestReleasedRevision) {
                        mapList = filterForLatestRevisions(context,mapList);
                    }
                    //End of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
            }
            catch (Exception excp)
            {
                excp.printStackTrace(System.out);
                throw excp;
            }
            return mapList;//return Maplist containing the result
    }//End of the method




/**
* To obtain the Configuration Count.
* @return Vector , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Vector getConfigurationCount(Context context, String[] args)
throws Exception
{
    Vector vtrConfgCnt = new Vector();
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    MapList lstObjectIdsList = (MapList)programMap.get("objectList");

    for (int  i =0 ;i <lstObjectIdsList.size() ;i++ )
    {
        if(lstObjectIdsList.get(i) instanceof HashMap)
        {
            HashMap hm = (HashMap)lstObjectIdsList.get(i);
            String intS = ((Integer)hm.get("PCCount")).toString();
            vtrConfgCnt.add(intS);
        }
        if(lstObjectIdsList.get(i) instanceof Hashtable)
        {
            Hashtable hm = (Hashtable)lstObjectIdsList.get(i);
            String intS = ((Integer)hm.get("PCCount")).toString();
            vtrConfgCnt.add(intS);
        }
    }
    return vtrConfgCnt;
}

/**
 * This Method is used to exclude the context Object Id from the search Results
 *
 * @param context- the eMatrix <code>Context</code> object
 * @param args- holds the HashMap containing the following arguments
 * @return  StringList- consisting of the context object id
 * @throws Exception if the operation fails
 *
 */

@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeContextObject(Context context, String[] args) throws Exception
{
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    String strObjectId = (String)  programMap.get("objectId");
    StringList tempStrList = new StringList();
    tempStrList.addElement(strObjectId);
    return tempStrList;
}

/**
 * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
 *
 * @param context- the eMatrix <code>Context</code> object
 * @param args- holds the HashMap containing the following arguments
 * @return  StringList- consisting of the object ids to be excluded from the Search Results
 * @throws Exception if the operation fails
 * @author Sandeep Kathe(klw)
 */

@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludeConnected(Context context, String[] args) throws Exception
{
    Map programMap = (Map) JPO.unpackArgs(args);
    String strObjectIds = (String)programMap.get("objectId");
    String strRelationship=(String)programMap.get("relName");
    StringList excludeList= new StringList();
    String toType=null;
    String fromType=null;
    boolean bisTo=false;
    boolean bisFrom=false;
    DomainObject domObjFeature = new DomainObject(strObjectIds);
    toType=domObjFeature.getInfo(context,"to["+PropertyUtil.getSchemaProperty(context,strRelationship)+"].from.type");
    fromType=domObjFeature.getInfo(context,"from["+PropertyUtil.getSchemaProperty(context,strRelationship)+"].to.type");

    if(toType!=null){
        bisTo=true;
    }
    else{
        bisFrom=true;
    }
    MapList childObjects=domObjFeature.getRelatedObjects(context,
            PropertyUtil.getSchemaProperty(context,strRelationship),
            toType==null?fromType:toType,
            new StringList(DomainConstants.SELECT_ID),
            null,
            bisTo,
            bisFrom,
           (short) 1,
            DomainConstants.EMPTY_STRING,
            DomainConstants.EMPTY_STRING, 0);
    for(int i=0;i<childObjects.size();i++){
        Map tempMap=(Map)childObjects.get(i);
        excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
    }
    excludeList.add(strObjectIds);
    return excludeList;
}




    /**
     * To obtain the list of Object IDs to be excluded from the search for "Derived From" while
     * creating the PC for tech feature
     *
     * @param context- the eMatrix <code>Context</code> object
     * @param args- holds the HashMap containing the following arguments
     * @return  StringList- consisting of the object ids to be excluded from the Search Results
     * @throws Exception if the operation fails
     * @author Subhash Shukla(iyz)
     */

    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeDerivedConfigurations(Context context, String[] args) throws Exception
    {
        StringList PCExcludeList= new StringList();
        String strPCId = "";
        String strProdId = "";

        Map programMap = (Map) JPO.unpackArgs(args);

        //get all the object ids
        String strBasedOnId = (String)programMap.get("contextBusId");
        String strProdContextId = (String)programMap.get("prodContextId");

        String relPattern = ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+","+
        ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION;

        String typePattern = ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION+","+
        ProductLineConstants.TYPE_PRODUCTS;


        StringList objSel = new StringList();

        objSel.add(DomainConstants.SELECT_ID);
        objSel.add("relationship["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].to.to" +
               "["+ConfigurationConstants.RELATIONSHIP_FEATURE_PRODUCT_CONFIGURATION+"].from.id");

        MapList lstPC = new DomainObject(strBasedOnId).getRelatedObjects(context,
                                                        relPattern,
                                                        typePattern,
                                                        objSel,
                                                        null,
                                                        true,
                                                        true,
                                                       (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING, 0);
        for (int i=0; i < lstPC.size(); i++)
        {
            strPCId =  (String)((Map)lstPC.get(i)).get(DomainConstants.SELECT_ID);
            if(((Map)lstPC.get(i)).containsKey(objSel.get(1)))
            {
                strProdId = (String)((Map)lstPC.get(i)).get(objSel.get(1));

                if(! strProdId.equals(strProdContextId))
                    PCExcludeList.add(strPCId);

            }else
                PCExcludeList.add(strPCId);

        }

        return PCExcludeList;
    }


}//End of the class
