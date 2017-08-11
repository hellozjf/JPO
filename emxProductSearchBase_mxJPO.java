/*
*  emxProductSearchBase.java
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
* static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.19.2.1.1.1.1.1 Tue Dec 02 15:48:56 2008 GMT ds-srickus Experimental$;
*/

import matrix.util.StringList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.FindLikeInfo;
import matrix.db.BusinessType;
import matrix.db.AttributeType;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.RelationshipType;
import matrix.db.BusinessTypeList;
import matrix.db.BusinessObject;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.productline.Product;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Search;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.domain.util.PersonUtil;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import  java.util.Hashtable;

/*****************************************************************************************************
* Functionality Description: This JPO is used to retreve the business Objects, based on the type.
* @author Enovia MatrixOne
* @version AEF 10.5 - Copyright (c) 2004, MatrixOne, Inc.
*
*****************************************************************************************************/

public class emxProductSearchBase_mxJPO extends emxSearch_mxJPO
{

// The operator names
/** A string constant with the value Includes. */
protected static final String OP_INCLUDES = "Includes";
/** A string constant with the value IsExactly. */
protected static final String OP_IS_EXACTLY = "IsExactly";
/** A string constant with the value IsNot. */
protected static final String OP_IS_NOT = "IsNot";
/** A string constant with the value Matches. */
protected static final String OP_IS_MATCHES = "Matches";
/** A string constant with the value BeginsWith. */
protected static final String OP_BEGINS_WITH = "BeginsWith";
/** A string constant with the value EndsWith. */
protected static final String OP_ENDS_WITH = "EndsWith";
/** A string constant with the value Equals. */
protected static final String OP_EQUALS = "Equals";
/** A string constant with the value DoesNotEqual. */
protected static final String OP_DOES_NOT_EQUAL = "DoesNotEqual";
/** A string constant with the value IsBetween. */
protected static final String OP_IS_BETWEEN = "IsBetween";
/** A string constant with the value IsAtMost. */
protected static final String OP_IS_ATMOST = "IsAtMost";
/** A string constant with the value IsAtLeast. */
protected static final String OP_IS_ATLEAST = "IsAtLeast";
/** A string constant with the value IsMoreThan. */
protected static final String OP_IS_MORE_THAN = "IsMoreThan";
/** A string constant with the value IsLessThan. */
protected static final String OP_IS_LESS_THAN = "IsLessThan";
/** A string constant with the value IsOn. */
protected static final String OP_IS_ON = "IsOn";
/** A string constant with the value IsOnOrBefore. */
protected static final String OP_IS_ON_OR_BEFORE = "IsOnOrBefore";
/** A string constant with the value IsOnOrAfter. */
protected static final String OP_IS_ON_OR_AFTER = "IsOnOrAfter";
protected static final String CURRENT = "current";
protected static final String SATISFIED = "satisfied";
protected static final String MARKETING = "Marketing";

// The operator symbols
/** A string constant with the value &&. */
protected static final String SYMB_AND = " && ";
/** A string constant with the value ||. */
protected static final String SYMB_OR = " || ";
/** A string constant with the value ==. */
protected static final String SYMB_EQUAL = " == ";
/** A string constant with the value !=. */
protected static final String SYMB_NOT_EQUAL = " != ";
/** A string constant with the value >. */
protected static final String SYMB_GREATER_THAN = " > ";
/** A string constant with the value <. */
protected static final String SYMB_LESS_THAN = " < ";
/** A string constant with the value >=. */
protected static final String SYMB_GREATER_THAN_EQUAL = " >= ";
/** A string constant with the value <=. */
protected static final String SYMB_LESS_THAN_EQUAL = " <= ";
/** A string constant with the value ~~. */
protected static final String SYMB_MATCH = " ~~ ";  // Short term fix for Bug #243366, was " ~~ "
/** A string constant with the value '. */
protected static final String SYMB_QUOTE = "'";
/** A string constant with the value *. */
protected static final String SYMB_WILD = "*";
/** A string constant with the value (. */
protected static final String SYMB_OPEN_PARAN = "(";
/** A string constant with the value ). */
protected static final String SYMB_CLOSE_PARAN = ")";
/** A string constant with the value attribute. */
protected static final String SYMB_ATTRIBUTE = "attribute";
/** A string constant with the value [. */
protected static final String SYMB_OPEN_BRACKET = "[";
/** A string constant with the value ]. */
protected static final String SYMB_CLOSE_BRACKET = "]";
/** A string constant with the value to. */
protected static final String SYMB_TO = "to";
/** A string constant with the value from. */
protected static final String SYMB_FROM = "from";
/** A string constant with the value ".". */
protected static final String SYMB_DOT = ".";
/** A string constant with the value "null". */
protected static final String SYMB_NULL = "null";
/** A string constant with the value "Yes". */
protected static final String SYMB_YES = "Yes";



/** A string constant with the value "comboDescriptor_". */
protected static final String COMBO_PREFIX = "comboDescriptor_";
/** A string constant with the value "txt_". */
protected static final String TXT_PREFIX = "txt_";
/** A string constant with the value objectIDs. */
protected static final String OBJECT_IDS = "objectIDs";

/** A string constant with the value All. */
protected static final String ALL = "All";
/** A string constant with the value Default. */
protected static final String DEFAULT = "Default";
/** A string constant with the value Selected. */
protected static final String SELECTED = "Selected";

/** A string constant with the value PLCSearchProductsForm. */
protected static final String SEARCH_PRODUCTS_FORM = "PLCSearchProductsForm";
/** A string constant with the value DesignResponsibility. */
protected static final String MODE_DESIGN_RESPONSIBILITY = "DesignResponsibility";
/** A string constant with the value requestMap. */
protected static final String REQUEST_MAP = "requestMap";
/** A string constant with the value PLCSearchSpecificationsCommand. */
protected static final String SPECIFICATION_COMMAND = "PLCSearchSpecificationsCommand";
/** A string constant with the value PLCSearchCompanyCommand. */
protected static final String COMPANY_COMMAND = "PLCSearchCompanyCommand";
/** A string constant with the value vaultOption. */
protected static final String VAULT_OPTION = "vaultOption";
/** A string constant with the value VaultDisplay. */
protected static final String VAULT_DISPLAY = "VaultDisplay";
/** A string constant with the value VaultDisplay. */
protected static final String VAULT_NAME = "vaultName";
/** A string constant with the value VaultDisplay. */
protected static final String QUERY_LIMIT = "queryLimit";
/** A string constant with the value Release. */
protected static final String STATE_RELEASE = "Release";
/** A string constant with the value last. */
protected static final String REVISION_LAST = "last";
/** A string constant with the value last. */
protected static final String ID = "id";
protected static final String NAME = "name";
protected static final String RELATIONSHIP = "relationship";





// Internationalisation Constants
protected static final String SUITE_KEY = "ProductLine";
/** A string constant with the value emxProduct.Form.Radio.LatestReleasedRevisionOnly. */
protected static final String PRODUCT_REVISION_LATEST_RELEASED_REVISION_ONLY = "emxProduct.Form.Radio.LatestReleasedRevisionOnly";
/** A string constant with the value "emxProduct.Form.Radio.LatestRevisionOnly". */
protected static final String PRODUCT_REVISION_LATEST_REVISION_ONLY = "emxProduct.Form.Radio.LatestRevisionOnly";
/** A string constant with the value emxProduct.Form.Radio.All. */
protected static final String FEATURE_VAULT_ALL = "emxProduct.Form.Radio.All";
/** A string constant with the value emxProduct.Form.Radio.Default. */
protected static final String FEATURE_VAULT_DEFAULT = "emxProduct.Form.Radio.Default";
/** A string constant with the value emxProduct.Form.Radio.Selected. */
protected static final String FEATURE_VAULT_SELECTED = "emxProduct.Form.Radio.Selected";
/** A string constant with the value emxProduct.Form.Radio.Custom. */
protected static final String PRODUCT_CONFIGURATION_PURPOSE_CUSTOM = "emxProduct.Form.Radio.Custom";
/** A string constant with the value emxProduct.Form.Radio.Standard. */
protected static final String PRODUCT_CONFIGURATION_PURPOSE_STANDARD = "emxProduct.Form.Radio.Standard";
/** A string constant with the value emxProduct.Form.Radio.Specific. */
protected static final String PART_REVISION_SPECIFIC = "emxProduct.Form.Radio.Specific";
/** A string constant with the value emxProduct.Form.Radio.LatestReleasedRevision. */
protected static final String PART_REVISION_LATEST_RELEASED = "emxProduct.Form.Radio.LatestReleasedRevision";
/** A string constant with the value emxProduct.Form.Radio.AllRevisions. */
protected static final String PART_REVISION_ALL_REVISIONS = "emxProduct.Form.Radio.AllRevisions";
/** A string constant with the value emxProduct.Form.Radio.RelesedAndUnreleased. */
protected static final String PART_REVISION_RELEASED_AND_UNRELEASED = "emxProduct.Form.Radio.RelesedAndUnreleased";
/** A string constant with the value emxProduct.Form.Checkbox.IncludeVersions. */
protected static final String PRODUCT_VERSIONS = "emxProduct.Form.Checkbox.IncludeVersions";
/** A string constant with the value emxProduct.Form.Radio.AllStates. */
protected static final String ALL_STATES = "emxProduct.Form.Radio.AllStates";
/** A string constant with the value attribute_IsVersionObject. */
protected static final String SYMBOLIC_attribute_IsVersionObject = "attribute_IsVersionObject";
/** A string constant with the value ProductConfigurationPurpose. */
protected static final String SYMBOLIC_attribute_ProductConfigurationPurpose="attribute_ProductConfigurationPurpose";
/** A string constant with the value Order. */
protected static final String ATTRIBUTE_ORDER_VALUE="Order";

//Added by Enovia MatrixOne for Bug# 296547 on 28 Mar 05
/** Alias for the name of the tree menu for Product Line. */
protected static final String MENU_PLCPRODUCTLINE = "type_ProductLine";


/**
* The default constructor.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments.
* @throws Exception if the operation fails
* @since ProductCentral 10.0.0.0
.*/



public emxProductSearchBase_mxJPO (Context context, String[] args) throws Exception
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
   String sContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.UnsupportedClient", context.getSession().getLanguage());
   throw  new Exception(sContentLabel);
 }
return 0;
}

/**
* To Obtain the Builds depending on the Search Criteria.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Build Type
*            Name
*            State
*            Owner
*            Vault Option
* @return a MapList , consisting of the object ids of the selected Objects
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getBuilds(Context context, String[] args)
throws Exception
{
// To read the input Search Criteria fro the Web Form and also the query limit
Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = (String)programMap.get("TypeBuild");
if (strType==null || strType.equals("")) {
  strType = SYMB_WILD;
}

String strName = (String)programMap.get("Name");
// strName = strName.trim();
if (strName==null || strName.equals("")) {
  strName = SYMB_WILD;
}

String strState = (String)programMap.get("State");

String strOwner = (String)programMap.get("OwnerDisplay");

if (strOwner==null || strOwner.equals("")) {
  strOwner = SYMB_WILD;
}


/*  If none of the Vaults are selected, add a *, search in all Vaults */
String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

    if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
    else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(120);

/* To add Wild Card char, '*' incase of an empty field  */
if (strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_CURRENT);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strState);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

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

MapList mapList = null;
// To Obtain the Search Results, based on the query
mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;
}



/**
* To obtain the Products.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Product Type
*            Name
*            Revision condition
*            Revision
*        Description
*            Originator
*            Owner
*            Include Version Flag
*            Vault Option
* @return a MapList , consisting of the object ids of the selected Objects
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getProducts(Context context, String[] args)
throws Exception
{
    MapList mapList = null;
    Map programMap = (Map) JPO.unpackArgs(args);
    Map tempMap = null;
    StringBuffer sbObjSelect = new StringBuffer(20);
    String strSrcDestRelName = "";
    String strRelNameActual = "";
    String strFromObjId = "";
    String strMode = (String)programMap.get(Search.REQ_PARAM_MODE);
    strSrcDestRelName = (String)programMap.get("srcDestRelName");


    if(!( (strSrcDestRelName == null) ||
      ("".equals(strSrcDestRelName))||
      ("null".equalsIgnoreCase(strSrcDestRelName))
      )
      )
    {
     strRelNameActual = PropertyUtil.getSchemaProperty(context,strSrcDestRelName);
    }

    if(( (strRelNameActual == null) ||
      ("".equals(strRelNameActual))||
      ("null".equalsIgnoreCase(strRelNameActual))
      )
      )
    {
     strRelNameActual = ProductLineConstants.RELATIONSHIP_PRODUCTS;
    }

    try {

      // To read the input Search crieteria from the Products form
      short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

      String strType = (String)programMap.get("TypeProduct");
      if (strType==null || strType.equals("")) {
        strType = SYMB_WILD;
      }

      String strName = (String)programMap.get("Name");

      if ( strName==null || strName.equals("")) {
        strName = SYMB_WILD;
      }


      String strRadio = (String)programMap.get("radio");
      String strRevision = null;
      boolean bLatestRevision = false;
      boolean bLatestReleasedRevision = false;
      if (strRadio != null) {
        if (strRadio.equals("input")) {
          strRevision = (String)programMap.get("Revision");
          strRevision = strRevision.trim();
        }
        else if (strRadio.equals("latestReleased")) {
          bLatestReleasedRevision = true;
        }
        else if (strRadio.equals("latest")) {
          bLatestRevision = true;
        }
      }
      if ((strRevision == null) || (strRevision.equals(""))) {
        strRevision = SYMB_WILD;
      }


      String strDesc = (String)programMap.get("Description");

      String strState = (String)programMap.get("State");

      String strOriginator = (String)programMap.get("OriginatorDisplay");


      Person person = Person.getPerson(context);
      String strCompany = person.getCompanyId(context);

      String strOwner = (String)programMap.get("OwnerDisplay");

      if (strOwner==null || strOwner.equals("")) {
        strOwner = SYMB_WILD;
      }


      String strIncludeVersions = (String)programMap.get("IncludeVersions");
      boolean bIncludeVersions = false;
      if (strIncludeVersions != null) {
        bIncludeVersions = true;
      }


      /*  If none of the Vaults are selected, add a *, search in all Vaults */
      String strVault = null;
      String strVaultOption = (String)programMap.get("vaultOption");

         if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                                strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
         else
                strVault = (String)programMap.get("vaults");

      StringList select = new StringList(1);
      select.addElement(DomainConstants.SELECT_ID);
      sbObjSelect.append(SYMB_TO);
      sbObjSelect.append(SYMB_OPEN_BRACKET);
      sbObjSelect.append(strRelNameActual);
      sbObjSelect.append(SYMB_CLOSE_BRACKET);
      sbObjSelect.append(SYMB_DOT);
      sbObjSelect.append(DomainConstants.SELECT_ID);
      strFromObjId = sbObjSelect.toString();
      select.addElement(strFromObjId);

      boolean start = true;
      StringBuffer sbWhereExp = new StringBuffer(120);

      if ((strDesc!=null) && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
        sbWhereExp.append(SYMB_MATCH);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(strDesc);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }

      if ( (strOriginator!=null) && (!strOriginator.equals(SYMB_WILD)) && (!strOriginator.equals("")) ){
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        } else {
          sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(DomainConstants.SELECT_ORIGINATOR);
        sbWhereExp.append(SYMB_MATCH);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(strOriginator);
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

      if (bLatestReleasedRevision) {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        } else {
          sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(DomainConstants.SELECT_CURRENT);
        sbWhereExp.append(SYMB_EQUAL);
        sbWhereExp.append("Release");
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }

      if (bLatestRevision) {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        } else {
          sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(DomainConstants.SELECT_REVISION);
        sbWhereExp.append(SYMB_EQUAL);
        sbWhereExp.append("last");
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }

      if (!bIncludeVersions) {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        } else {
          sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(SYMB_ATTRIBUTE);
        sbWhereExp.append(SYMB_OPEN_BRACKET);
        sbWhereExp.append(ProductLineConstants.ATTRIBUTE_IS_VERSION);
        sbWhereExp.append(SYMB_CLOSE_BRACKET);
        sbWhereExp.append(SYMB_EQUAL);
        sbWhereExp.append("false");
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }


      if ( (strCompany != null) && !(strCompany.equals("")) ) {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        } else {
          sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(SYMB_TO);
        sbWhereExp.append(SYMB_OPEN_BRACKET);
        sbWhereExp.append(ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT);
        sbWhereExp.append(SYMB_CLOSE_BRACKET);
        sbWhereExp.append(SYMB_DOT);
        sbWhereExp.append(SYMB_FROM);
        sbWhereExp.append(SYMB_DOT);
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append(SYMB_EQUAL);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(strCompany);
        sbWhereExp.append(SYMB_QUOTE);
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }


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

      //Begin of Add by Vibhu,Enovia MatrixOne for Bug 310059 on 10/7/2005
      //this where clause is to find those products which have a feature list below them
      //this is because a featureoption should be present in searched products
      String strFeatureOption = (String)programMap.get("FeatureOption");
      if(strFeatureOption!=null && strFeatureOption.equalsIgnoreCase("true"))
      {
        if (start) {
          sbWhereExp.append(SYMB_OPEN_PARAN);
          start = false;
        } else {
          sbWhereExp.append(SYMB_AND);
        }
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(SYMB_FROM);
        sbWhereExp.append(SYMB_OPEN_BRACKET);
        sbWhereExp.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM);
        sbWhereExp.append(SYMB_CLOSE_BRACKET);
        sbWhereExp.append(SYMB_DOT);
        sbWhereExp.append(SYMB_TO);
        sbWhereExp.append(SYMB_DOT);
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append(SYMB_NOT_EQUAL);
        sbWhereExp.append(SYMB_NULL);
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }
      //End of Add by Vibhu,Enovia MatrixOne for Bug 310059 on 10/7/2005
      if (!start) {
        sbWhereExp.append(SYMB_CLOSE_PARAN);
      }
      mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
        //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
        if(bLatestReleasedRevision) {
            mapList = filterForLatestRevisions(context,mapList);
        }
        //End of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
    } catch(Throwable excp) {

      excp.printStackTrace(System.out);
    }

      if (strMode.equals(Search.ADD_EXISTING) )
          {
              for(int i=0;i<mapList.size();i++)
               {
                tempMap = (Map)mapList.get(i);
                tempMap.put(DomainConstants.SELECT_RELATIONSHIP_ID,(String)tempMap.get(strFromObjId));
               }
          }
    return mapList;
}







/**
* To obtain the ProductLines.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Name
*            Owner
*        Description
*            Vault Option
*            Vault Display
* @return a MapList , consisting of the object ids of the selected Objects
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getProductLines(Context context, String[] args)
throws Exception
{

Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = ProductLineConstants.TYPE_PRODUCT_LINE;

String strName = (String)programMap.get("Name");

if (strName==null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strOwner = (String)programMap.get("OwnerDisplay");
if (strOwner==null || strOwner.equals("")) {
  strOwner = SYMB_WILD;
}

String strDesc = (String)programMap.get("Description");

Person person = Person.getPerson(context);
String strCompany = person.getCompanyId(context);

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(120);

if ((strDesc!=null) && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strDesc);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if ( (strCompany != null) && !(strCompany.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_TO);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.RELATIONSHIP_COMPANY_PRODUCT_LINES);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(SYMB_FROM);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(DomainConstants.SELECT_ID);
  sbWhereExp.append(SYMB_EQUAL);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strCompany);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

String strFilteredExpression = getFilteredExpression(context,programMap);

if ( (strFilteredExpression != null && !strFilteredExpression.equals("") ) ) {
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

MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;
}


/**
* To obtain the Test/Use Cases.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Name
*            State
*        Owner
*            Vault Option
*            Vault Display
* @param strType String containing the Type.
* @return a MapList , consisting of the object ids of the selected Objects
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

public static Object getCases(Context context, String[] args,String strType)
throws Exception
{
Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strMode = (String)programMap.get(Search.REQ_PARAM_MODE);

String strName = (String)programMap.get("Name");
if (strName==null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strState = (String)programMap.get("State");

String strDesc = (String)programMap.get("Description");

String strOwner = (String)programMap.get("OwnerDisplay");

if (strOwner==null || strOwner.equals("") ) {
  strOwner = SYMB_WILD;
}

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(40);

if ( (strDesc!=null) && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strDesc);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);

}



if (strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
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

  StringList objSelects = new StringList(1);
  objSelects.addElement(DomainConstants.SELECT_ID);
  int sRecurse = 0;

String strObjectId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);

MapList mapList = null;
 mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
if(strMode.equals(Search.ADD_EXISTING)||strMode.equals(Search.CHOOSER))
 {
  //Begin of Modify
  //By Enovia MatrixOne for Bug# 295470
  //Modified Date: March 3, 2005
  //Moved this line inside the if condition
  String strRelType = PropertyUtil.getSchemaProperty(context,(String)programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME));
  //End of Modify for Bug# 295470

  DomainObject dom = new DomainObject(strObjectId);
  MapList newMapList = null;
  newMapList = dom.getRelatedObjects(context,strRelType,strType,true,false,sRecurse,objSelects,null,null,null,null,strType,null);

  for(int i=0;i<mapList.size();i++)
    {
      String strSelectedId = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_ID);
      strSelectedId = strSelectedId.trim();
      for(int j=0;j<newMapList.size();j++)
        {
          String strParentId = (String)( (Map)newMapList.get(j) ).get(DomainConstants.SELECT_ID);
          strParentId = strParentId.trim();

          if ( strSelectedId.equals(strParentId) )
            {
               mapList.remove(i);
               newMapList.remove(j);
               break;
            }
        }
    }
}
return mapList;
}

/**
* The function to filter the object selection and apppend the default query
* in the where clause.
*
* @param context the eMatrix <code>Context</code> object
* @param programMap holds arguments passed
* @return String , after constructing the Where clause appropriately
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/


public static String getFilteredExpression(Context context,Map programMap) throws Exception
{

String strMode = (String)programMap.get(Search.REQ_PARAM_MODE);
String strObjectId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);
String strSrcDestRelNameSymb = (String)programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME);
String strIsTo = (String)programMap.get(Search.REQ_PARAM_IS_TO);
String strDQ = (String)programMap.get(Search.REQ_PARAM_DEFAULT_QUERY);
String strMidDestRelNameSymb = (String)programMap.get(Search.REQ_PARAM_MID_DEST_REL_NAME);
String strSrcMidRelNameSymb = (String)programMap.get(Search.REQ_PARAM_SRC_MID_REL_NAME);




String strMidDestRelName = null;
if (strMidDestRelNameSymb != null && !strMidDestRelNameSymb.equals("") ) {
  strMidDestRelName = PropertyUtil.getSchemaProperty(context,strMidDestRelNameSymb);
}


String strSrcMidRelName = null;
if (strSrcMidRelNameSymb != null && !strSrcMidRelNameSymb.equals("") ) {
  strSrcMidRelName = PropertyUtil.getSchemaProperty(context,strSrcMidRelNameSymb);
}

String strSrcDestRelName = null;
if (strSrcDestRelNameSymb != null && !strSrcDestRelNameSymb.equals("") ) {
 strSrcDestRelName = PropertyUtil.getSchemaProperty(context,strSrcDestRelNameSymb);
}


StringBuffer sbWhereExp = new StringBuffer(100);
//sbWhereExp.append(SYMB_OPEN_PARAN);
boolean start = true;

String strCommand = (String)programMap.get(Search.REQ_PARAM_COMMAND);

// If add exisitng Object of type other that Part
if ((strCommand != null) && !(strCommand.equals("PLCSearchPartsCommand")))
{


  if (strMode.equals(Search.ADD_EXISTING) ) {
    start = false;
    sbWhereExp.append(SYMB_OPEN_PARAN);
    /* Case where we have an Intermediate relationship */
    if(strSrcMidRelName != null && !strMidDestRelNameSymb.equals("") ) {

     if ( strIsTo.equalsIgnoreCase("true")  )  {
        sbWhereExp.append("!'to[");
        sbWhereExp.append(strMidDestRelName);
        sbWhereExp.append("].from.to[");
        sbWhereExp.append(strSrcMidRelName);
        sbWhereExp.append("].from.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
      }
      else {
        sbWhereExp.append("!'from[");
        sbWhereExp.append(strMidDestRelName);
        sbWhereExp.append("].to.to[");
        sbWhereExp.append(strSrcMidRelName);
        sbWhereExp.append(".from.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
      }
    }
    /* Case where we don't have an intermediate relationship */
    else {

      if ( strIsTo.equalsIgnoreCase("true")  )  {
        sbWhereExp.append("!('to[");
        sbWhereExp.append(strSrcDestRelName);
        sbWhereExp.append("].from.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append(")");

      }
      else {
        sbWhereExp.append("!('from[");
        sbWhereExp.append(strSrcDestRelName);
        sbWhereExp.append("].to.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append(")");

      }
    }
    sbWhereExp.append(SYMB_CLOSE_PARAN);
    /* To remove the duplicate object ids, from Add Existing sub types... */
    sbWhereExp.append(SYMB_AND);
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_ID);
    sbWhereExp.append("!='");
    sbWhereExp.append(strObjectId);
    sbWhereExp.append("'");
    sbWhereExp.append(SYMB_CLOSE_PARAN);

  if(sbWhereExp.toString().indexOf(DomainConstants.SELECT_ID)==-1)
  {
    if(sbWhereExp.length()!=0)
   {
    sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_ID);
    sbWhereExp.append("!='");
    sbWhereExp.append(strObjectId);
    sbWhereExp.append("'");
    sbWhereExp.append(SYMB_CLOSE_PARAN);

sbWhereExp.append(SYMB_AND);
        sbWhereExp.append("!('from[");
        sbWhereExp.append(strSrcDestRelName);
        sbWhereExp.append("].to.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append(")");


   }

  }
}

//End of if Condition for Part

if (strDQ != null && !strDQ.equals("") ) {
 if (!start) {
   sbWhereExp.append(SYMB_AND);
   start = false;
 }

 sbWhereExp.append(SYMB_OPEN_PARAN);
 sbWhereExp.append(strDQ);
 sbWhereExp.append(SYMB_CLOSE_PARAN);
}

String strFilteredExp = null;
String strWhereExp = sbWhereExp.toString();
if( !strWhereExp.equals("") ) {
  strFilteredExp = strWhereExp;
}
return strFilteredExp;
}


/**
* To obtain the Images.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Name
*            Description
*            File Name
*        Owner
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getImages(Context context, String[] args)
throws Exception
{

Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = ProductLineConstants.TYPE_IMAGE;

String strName = (String)programMap.get("Name");
if ( strName==null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strDesc = (String)programMap.get("Description");

String strFileName = (String)programMap.get("File Name");

String strOwner = (String)programMap.get("OwnerDisplay");
if (strOwner==null || strOwner.equals("") ) {
  strOwner = SYMB_WILD;
}

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(50);

if (strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strDesc);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if (strFileName!=null  && (!strFileName.equals(SYMB_WILD)) && (!strFileName.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_FILE_NAME);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strFileName);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

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

MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;

}



/**
* To obtain the Reference Document (Document).
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Document Type
*            Name
*            Revision
*            Title
*            Description
*            State
*            Policy
*        Owner
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getDocuments(Context context, String[] args)
throws Exception
{

Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = (String)programMap.get("TypeDocument");
if (strType==null || strType.equals("") ) {
  strType = SYMB_WILD;
}

String strName = (String)programMap.get("Name");

if (strName==null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strRevision = (String)programMap.get("Revision");

String strLatestRevisionOnly = (String)programMap.get("latestOnly");
boolean bLatestRevisionOnly = false;
if (strLatestRevisionOnly != null) {
  bLatestRevisionOnly = true;
}

String strTitle = (String)programMap.get("Title");

String strDesc = (String)programMap.get("Description");

String strState = (String)programMap.get("State");
if (strState != null) {
  strState = strState.trim();
}
else {
  strState = "";
}

String strPolicy = (String)programMap.get("Policy");


String strOwner = (String)programMap.get("OwnerDisplay");
if ( strOwner==null || strOwner.equals("") ) {
  strOwner = SYMB_WILD;
}

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(120);

if (strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strDesc);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if (strPolicy!=null && (!strPolicy.equals(SYMB_WILD)) && (!strPolicy.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_POLICY);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strPolicy);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if (strTitle!=null && (!strTitle.equals(SYMB_WILD)) && (!strTitle.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_ATTRIBUTE);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.ATTRIBUTE_TITLE);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strTitle);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
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

if (bLatestRevisionOnly) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_REVISION);
  sbWhereExp.append(SYMB_EQUAL);
  sbWhereExp.append("last");
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

//Adding the clause attribute[attribute_IsVersionObject] != True
String strAttrIsVersionObject = PropertyUtil.getSchemaProperty(
  context,
  SYMBOLIC_attribute_IsVersionObject);
if(start) {
sbWhereExp.append(SYMB_OPEN_PARAN);
start = false;
} else {
sbWhereExp.append(SYMB_AND);
}
sbWhereExp.append(SYMB_OPEN_PARAN);
sbWhereExp.append(SYMB_ATTRIBUTE);
sbWhereExp.append(SYMB_OPEN_BRACKET);
sbWhereExp.append(strAttrIsVersionObject);
sbWhereExp.append(SYMB_CLOSE_BRACKET);
sbWhereExp.append(SYMB_NOT_EQUAL);
sbWhereExp.append(SYMB_QUOTE);
sbWhereExp.append("True");
sbWhereExp.append(SYMB_QUOTE);
sbWhereExp.append(SYMB_CLOSE_PARAN);

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

MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;
}



/**
* To obtain the Parts.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Part Type
*            Name
*            Revision Condition
*            Revision
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getParts(Context context, String[] args)
throws Exception
{
Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = (String)programMap.get("TypePart");
if ( strType!=null && strType.equals("")) {
  strType = SYMB_WILD;
}

String strName = (String)programMap.get("Name");
if (strName!=null && strName.equals("")) {
  strName = SYMB_WILD;
}

String strDesc = (String)programMap.get("Description");
if (strDesc!=null && strDesc.equals("")) {
    strDesc = SYMB_WILD;
}

String strRadio = (String)programMap.get("radio");
String strRevision = null;
boolean bLatestAllRevisions = false;
boolean bLatestReleasedRevisions = false;

if (strRadio != null) {
  if (strRadio.equals("input")) {
    strRevision = (String)programMap.get("Revision");
    strRevision = strRevision.trim();
  }
  else if (strRadio.equals("latestReleased")) {
    bLatestReleasedRevisions = true;
  }
  else if (strRadio.equals("latestAllRevisions")) {
    bLatestAllRevisions = true;
  }
  else if (strRadio.equals("allRevisions")) {
    strRevision = SYMB_WILD;
  }
}
if ((strRevision == null) || (strRevision.equals("")) ) {
  strRevision = SYMB_WILD;
}


String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");
     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(100);

if (bLatestReleasedRevisions) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_CURRENT);
  sbWhereExp.append(SYMB_EQUAL);
  sbWhereExp.append("Release");
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if (bLatestAllRevisions) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_REVISION);
  sbWhereExp.append(SYMB_EQUAL);
  sbWhereExp.append("last");
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}
if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
    if (start) {
        sbWhereExp.append(SYMB_OPEN_PARAN);
        start = false;
    } else {
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


MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, strRevision, SYMB_WILD, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
//Begin of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
if(bLatestReleasedRevisions) {
    mapList = filterForLatestRevisions(context,mapList);
}
//End of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
return mapList;
}



/**
* To obtain the Projects.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Name
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getProjects(Context context, String[] args)
throws Exception
{
Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = ProductLineConstants.TYPE_PROJECT_SPACE;

if (strType==null || strType.equals("") ) {
  strType = SYMB_WILD;
}

String strName = (String)programMap.get("Name");
if (strName == null || strName.equals("")) {
  strName = SYMB_WILD;
}

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(100);


String strCompany = (String)programMap.get("Company");
if (strCompany!=null && (!strCompany.equals(SYMB_WILD)) && (!strCompany.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_TO);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.RELATIONSHIP_EMPLOYEE);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(SYMB_FROM);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(DomainConstants.SELECT_ID);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strCompany);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

String strBusinessUnit = (String)programMap.get("BusinessUnit");
if (strCompany!=null && (!strCompany.equals(SYMB_WILD)) && (!strCompany.equals("")) ) {
if (start) {
  sbWhereExp.append(SYMB_OPEN_PARAN);
  start = false;
} else {
  sbWhereExp.append(SYMB_AND);
}
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_TO);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.RELATIONSHIP_EMPLOYEE);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(SYMB_FROM);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(DomainConstants.SELECT_ID);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strBusinessUnit);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

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

MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;
}

/**
* To obtain the Test Cases.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getTestCases(Context context, String[] args)
throws Exception
{
String strType = ProductLineConstants.TYPE_TEST_CASE;
return getCases(context, args,strType);
}



/**
* To obtain the Models.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Name
*            ProductLine
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getModels(Context context, String[] args)
throws Exception
{
Map programMap = (Map) JPO.unpackArgs(args);
String strGetAllModels = (String)programMap.get("getAllModels");
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = ProductLineConstants.TYPE_MODEL;
String strWhereExp = "";

String strName = (String)programMap.get("Name");
if (  strName== null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strContextModel = (String)programMap.get("objectId");
// Begin of Modify by Praveen, Enovia MatrixOne for bug #300606 03/16/05
if ((strContextModel != null) && !("".equals(strContextModel)) && !("null".equals(strContextModel)))
{
    strContextModel = strContextModel.trim();
}
// End of Modify by Praveen, Enovia MatrixOne for bug #300606 03/16/05

String strProductLine = (String)programMap.get("ProductLine");
// Begin of Modify by Praveen, Enovia MatrixOne for bug #300606 03/16/05
if ((strProductLine != null) && !("".equals(strProductLine)) && !("null".equals(strProductLine)))
{
    strProductLine = strProductLine.trim();
}
// End of Modify by Praveen, Enovia MatrixOne for bug #300606 03/16/05

String strDesc = (String)programMap.get("Description");
// Begin of Modify by Praveen, Enovia MatrixOne for bug #300606 03/16/05
if ((strDesc != null) && !("".equals(strDesc)) && !("null".equals(strDesc)))
{
    strDesc = strDesc.trim();
}
// End of Modify by Praveen, Enovia MatrixOne for bug #300606 03/16/05

String strOwner = (String) programMap.get("Owner");

if (strOwner == null
        || strOwner.equals("")
        || SYMB_NULL.equalsIgnoreCase(strOwner)) {
        strOwner = SYMB_WILD;
} else {
        strOwner = strOwner.trim();
}

String strState = (String) programMap.get("State");

if (strState == null
        || strState.equals("")
        || SYMB_NULL.equalsIgnoreCase(strState)) {
        strState = SYMB_WILD;
} else {
        strState = strState.trim();
}

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(150);

if (strDesc != null
        && (!strDesc.equals(SYMB_WILD))
        && (!strDesc.equals(""))
        && !(SYMB_NULL.equalsIgnoreCase(strDesc))) {
        if (start) {
                sbWhereExp.append(SYMB_OPEN_PARAN);
                start = false;
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
        && !(SYMB_NULL.equalsIgnoreCase(strState))) {
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

if ( strProductLine!=null && (!strProductLine.equals(SYMB_WILD)) && (!strProductLine.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  else {
    sbWhereExp.append(SYMB_AND);
  }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(SYMB_TO);
    sbWhereExp.append(SYMB_OPEN_BRACKET);
    sbWhereExp.append(ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS);
    sbWhereExp.append(SYMB_CLOSE_BRACKET);
    sbWhereExp.append(SYMB_DOT);
    sbWhereExp.append(SYMB_FROM);
    sbWhereExp.append(SYMB_DOT);
    sbWhereExp.append(DomainConstants.SELECT_ID);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strProductLine);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
}

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

String strMode = (String)programMap.get(Search.REQ_PARAM_MODE);

if (strMode.equals(Search.ADD_EXISTING) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append("!");
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_TO);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(SYMB_FROM);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(DomainObject.SELECT_ID);
  sbWhereExp.append("!=null");
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if (!start) {
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}
strWhereExp = sbWhereExp.toString();
if (strGetAllModels!=null && strGetAllModels.equalsIgnoreCase("true"))
{
     StringBuffer sbWhereExpForAllModels=new StringBuffer();
     sbWhereExpForAllModels.append(SYMB_OPEN_PARAN);
     sbWhereExpForAllModels.append(DomainConstants.SELECT_ID);
     sbWhereExpForAllModels.append("!=");
     sbWhereExpForAllModels.append(SYMB_QUOTE);
     sbWhereExpForAllModels.append(strContextModel);
     sbWhereExpForAllModels.append(SYMB_QUOTE);
     sbWhereExpForAllModels.append(SYMB_CLOSE_PARAN);
     strWhereExp = sbWhereExpForAllModels.toString();
}
MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, strOwner, strVault, strWhereExp, "", true, select, sQueryLimit);

return mapList;

}

/**
* To obtain the Persons.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            User Name
*            First Name
*            Last Name
*        Company
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getPersons(Context context, String[] args)
throws Exception
{

Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = ProductLineConstants.TYPE_PERSON;

String strName = (String)programMap.get("User Name");

if ( strName==null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strFirstName = (String)programMap.get("First Name");


String strLastName = (String)programMap.get("Last Name");


String strCompany = (String)programMap.get("Company");

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");

    if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(100);

if (strFirstName!=null && (!strFirstName.equals(SYMB_WILD)) && (!strFirstName.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_ATTRIBUTE);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.ATTRIBUTE_FIRST_NAME);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strFirstName);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if ( strLastName!=null && (!strLastName.equals(SYMB_WILD)) && (!strLastName.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_ATTRIBUTE);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.ATTRIBUTE_LAST_NAME);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strLastName);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if (strCompany!=null && (!strCompany.equals(SYMB_WILD)) && (!strCompany.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_TO);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.RELATIONSHIP_EMPLOYEE);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(SYMB_FROM);
  sbWhereExp.append(SYMB_DOT);
  sbWhereExp.append(DomainConstants.SELECT_ID);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strCompany);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

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

MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;

}



/**
* To obtain the Specifications.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Specification Type
*            Name
*            Revision
*        Title
*            Description
*            State
*            Policy
*            Owner
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getSpecifications(Context context, String[] args)
throws Exception
{

Map programMap = (Map) JPO.unpackArgs(args);
short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

String strType = (String)programMap.get("TypeSpecification");
if (  strType==null || strType.equals("") ) {
  strType = SYMB_WILD;
}

String strName = (String)programMap.get("Name");

if ( strName==null || strName.equals("") ) {
  strName = SYMB_WILD;
}

String strRevision = (String)programMap.get("Revision");
if ( strRevision==null || strRevision.equals("") ) {
    strRevision = SYMB_WILD;
  }

String strTitle = (String)programMap.get("Title");

String strDesc = (String)programMap.get("Description");

String strState = (String)programMap.get("State");
if (strState != null) {
  strState = strState.trim();
}
else {
  strState = "";
}

String strPolicy = (String)programMap.get("Policy");

String strOwner = (String)programMap.get("OwnerDisplay");
if ( strOwner==null || strOwner.equals("") ) {
  strOwner = SYMB_WILD;
}

String strVault = null;
String strVaultOption = (String)programMap.get("vaultOption");
     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

StringList select = new StringList(1);
select.addElement(DomainConstants.SELECT_ID);

boolean start = true;
StringBuffer sbWhereExp = new StringBuffer(120);

if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strDesc);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}
//Commented from Onsite coz this condition has a problem so the search results will not come up
/* if (strPolicy!=null && (!strPolicy.equals(SYMB_WILD)) && (!strPolicy.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(DomainConstants.SELECT_POLICY);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strPolicy);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}*/

if ( strTitle!=null && (!strTitle.equals(SYMB_WILD)) && (!strTitle.equals("")) ) {
  if (start) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    start = false;
  } else {
    sbWhereExp.append(SYMB_AND);
  }
  sbWhereExp.append(SYMB_OPEN_PARAN);
  sbWhereExp.append(SYMB_ATTRIBUTE);
  sbWhereExp.append(SYMB_OPEN_BRACKET);
  sbWhereExp.append(ProductLineConstants.ATTRIBUTE_TITLE);
  sbWhereExp.append(SYMB_CLOSE_BRACKET);
  sbWhereExp.append(SYMB_MATCH);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(strTitle);
  sbWhereExp.append(SYMB_QUOTE);
  sbWhereExp.append(SYMB_CLOSE_PARAN);
}

if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals("")) ) {
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

MapList mapList = null;
mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "", true, select, sQueryLimit);
return mapList;
}



/**
* For the "Find Company" funcionality.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Type
*            Name
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getCompanies(Context context, String[] args)
throws Exception
{
MapList mapList = null;
try {

  Map programMap = (Map) JPO.unpackArgs(args);
  short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

  String strType = (String)programMap.get("Type");
  if ( strType==null || strType.equals("") ) {
    strType = SYMB_WILD;
  }

  String strName = (String)programMap.get("Name");

  if ( strName==null || strName.equals("") ) {
            strName = SYMB_WILD;
  }

  String strVault = null;
  String strVaultOption = (String)programMap.get("vaultOption");

      if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

  StringList select = new StringList(1);
  select.addElement(DomainConstants.SELECT_ID);

  boolean start = true;
  StringBuffer sbWhereExp = new StringBuffer(100);

  /*if ( strName!=null && (!strName.equals(SYMB_WILD)) && (!strName.equals("")) ) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(SYMB_ATTRIBUTE);
    sbWhereExp.append(SYMB_OPEN_BRACKET);
    sbWhereExp.append(ProductCentralDomainConstants.ATTRIBUTE_ORGANIZATION_NAME);
    sbWhereExp.append(SYMB_CLOSE_BRACKET);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strName);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }*/

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
  //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/14/2005
  if (!start) {
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }
  //End of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/14/2005
  mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "",true, select,sQueryLimit);
} catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}

return mapList;
}



/**
* The "Find" query.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Type
*            Name
*            Revision
*        Description
*            Owner
*            State
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object find(Context context, String[] args)
throws Exception
{
MapList mapList = null;
try {

  Map programMap = (Map) JPO.unpackArgs(args);
  short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

  String strType = (String)programMap.get("hdnType");

  if (strType==null || strType.equals("")) {
    strType = SYMB_WILD;
  }

  String strName = (String)programMap.get("txtName");

  if (strName== null || strName.equals("")) {
    strName = SYMB_WILD;
  }

  String strRevision = (String)programMap.get("txtRevision");

  if (strRevision==null || strRevision.equals("")) {
    strRevision = SYMB_WILD;
  }

  else {
    strRevision = strRevision.trim();
  }

 String strDesc = (String)programMap.get("txtDescription");

  String strOwner = (String)programMap.get("txtOwner");

  if (strOwner==null || strOwner.equals("")) {
    strOwner = SYMB_WILD;
  }

  else {
    strOwner = strOwner.trim();
  }

  String strState = (String)programMap.get("txtState");

  if (strState==null || strState.equals("")) {
    strState = SYMB_WILD;
  }
  else {
   strState = strState.trim();
  }

  String strVault = null;
  String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

  StringList select = new StringList(1);
  select.addElement(DomainConstants.SELECT_ID);

  boolean start = true;
  StringBuffer sbWhereExp = new StringBuffer(150);

  if (strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals(""))) {
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
    sbWhereExp.append(SYMB_MATCH);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(strDesc);
    sbWhereExp.append(SYMB_QUOTE);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }

  if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals(""))) {
    if (start) {
      sbWhereExp.append(SYMB_OPEN_PARAN);
      start = false;
    }
    else {
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
  if (!start) {
    sbWhereExp.append(SYMB_CLOSE_PARAN);
  }

  mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, sbWhereExp.toString(), "",true, select,sQueryLimit);
 } catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}

return mapList;
}


/**
* The "Find Like" query.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Type
*            Name
*            Revision
*        Description
*            Owner
*            State
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object findLike(Context context, String[] args)
throws Exception
{
MapList mapList = null;
try {

  Map programMap = (Map) JPO.unpackArgs(args);
  short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));

  String strType = (String)programMap.get("hdnType");

  if (strType==null || strType.equals("")) {
    strType = SYMB_WILD;
  }

  String strName = (String)programMap.get("txtName");

  if (strName==null || strName.equals("")) {
    strName = SYMB_WILD;
  }

  String strRevision = (String)programMap.get("txtRevision");

  if (strRevision==null || strRevision.equals("")) {
    strRevision = SYMB_WILD;
  }


  String strDesc = (String)programMap.get("txtDescription");
  String strOwner = (String)programMap.get("txtOwner");

  if (strOwner==null || strOwner.equals("")) {
    strOwner = SYMB_WILD;
  }

  else if (strOwner!=null) {
   strOwner = strOwner.trim();
  }

  String strState = (String)programMap.get("txtState");
  if ( strState==null || strState.equals("")) {
    strState = SYMB_WILD;
  }
  else if(strState!=null) {
   strState = strState.trim();
  }

  String strVault = null;
  String strVaultOption = (String)programMap.get("vaultOption");

     if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

  String strAdvOperator = (String)programMap.get("radAdvancedOperator");

  String strIntermediateOperator = SYMB_AND;
  if (strAdvOperator != null) {
    if (strAdvOperator.equalsIgnoreCase("and")) {
      strIntermediateOperator = SYMB_AND;
    } else {
      strIntermediateOperator = SYMB_OR;
    }
  }

  StringList select = new StringList(1);
  select.addElement(DomainConstants.SELECT_ID);

  boolean start = true;
  StringBuffer sbWhereExpFirst = new StringBuffer(100);

  if ( strDesc!=null && (!strDesc.equals(SYMB_WILD)) && (!strDesc.equals(""))) {
    if (start) {
      sbWhereExpFirst.append(SYMB_OPEN_PARAN);
      start = false;
    }
    sbWhereExpFirst.append(SYMB_OPEN_PARAN);
    sbWhereExpFirst.append(DomainConstants.SELECT_DESCRIPTION);
    sbWhereExpFirst.append(SYMB_MATCH);
    sbWhereExpFirst.append(SYMB_QUOTE);
    sbWhereExpFirst.append(strDesc);
    sbWhereExpFirst.append(SYMB_QUOTE);
    sbWhereExpFirst.append(SYMB_CLOSE_PARAN);
  }

  if ( strState!=null && (!strState.equals(SYMB_WILD)) && (!strState.equals(""))) {
    if (start) {
      sbWhereExpFirst.append(SYMB_OPEN_PARAN);
      start = false;
    }
    else {
      sbWhereExpFirst.append(SYMB_AND);
    }
    sbWhereExpFirst.append(SYMB_OPEN_PARAN);
    sbWhereExpFirst.append(DomainConstants.SELECT_CURRENT);
    sbWhereExpFirst.append(SYMB_MATCH);
    sbWhereExpFirst.append(SYMB_QUOTE);
    sbWhereExpFirst.append(strState);
    sbWhereExpFirst.append(SYMB_QUOTE);
    sbWhereExpFirst.append(SYMB_CLOSE_PARAN);
  }
  if (!start) {
    sbWhereExpFirst.append(SYMB_CLOSE_PARAN);
  }

  start = true;
  StringBuffer sbWhereExpSecond = new StringBuffer(150);

  Set keySet = programMap.keySet();
  int iPrefixLength = COMBO_PREFIX.length();
  for (Iterator iter=keySet.iterator(); iter.hasNext(); ) {
    String key = (String)iter.next();
    if (key.length() > iPrefixLength) {
      if (key.startsWith(COMBO_PREFIX)) {
        String strOperator = (String)programMap.get(key);
        strOperator = strOperator.trim();
        if (!strOperator.equals(SYMB_WILD)) {
          String strSelectFieldName = key.substring(iPrefixLength);
          String strActualFieldName = TXT_PREFIX + strSelectFieldName;
          String strActualValue = (String)programMap.get(strSelectFieldName);
          if ((strActualValue == null) || strActualValue.equals("")) {
            strActualValue = (String)programMap.get(strActualFieldName);
          }
          if (strActualValue != null) {
            if ( !(strActualValue.equals("*")) && !(strActualValue.equals("")) ) {
              if (start) {
                sbWhereExpSecond.append(SYMB_OPEN_PARAN);
                start = false;
              } else {
                sbWhereExpSecond.append(strIntermediateOperator);
              }
              if (strOperator.equals(OP_IS_BETWEEN)) {
                sbWhereExpSecond.append(SYMB_OPEN_PARAN);
              }
              sbWhereExpSecond.append(SYMB_OPEN_PARAN);
              if ((!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_MODIFIED)) && (!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_ORIGINATED))) {
                sbWhereExpSecond.append(SYMB_ATTRIBUTE);
                sbWhereExpSecond.append(SYMB_OPEN_BRACKET);
              }
              sbWhereExpSecond.append(strSelectFieldName);
              if ((!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_MODIFIED)) && (!strSelectFieldName.equalsIgnoreCase(DomainConstants.SELECT_ORIGINATED))) {
                sbWhereExpSecond.append(SYMB_CLOSE_BRACKET);
              }
              if (strOperator.equals(OP_INCLUDES)) {
                sbWhereExpSecond.append(SYMB_MATCH);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_WILD);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_WILD);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_EXACTLY)) {
                sbWhereExpSecond.append(SYMB_EQUAL);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_NOT)) {
                sbWhereExpSecond.append(SYMB_NOT_EQUAL);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_MATCHES)) {
                sbWhereExpSecond.append(SYMB_MATCH);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_BEGINS_WITH)) {
                sbWhereExpSecond.append(SYMB_MATCH);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_WILD);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_ENDS_WITH)) {
                sbWhereExpSecond.append(SYMB_MATCH);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_WILD);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_EQUALS)) {
                sbWhereExpSecond.append(SYMB_EQUAL);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_DOES_NOT_EQUAL)) {
                sbWhereExpSecond.append(SYMB_NOT_EQUAL);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_BETWEEN)) {
                strActualValue = strActualValue.trim();

                int iSpace   = strActualValue.indexOf(" ");
                String strLow  = "";
                String strHigh = "";

                if (iSpace == -1) {
                  strLow  = strActualValue;
                  strHigh = strActualValue;
                } else {
                  strLow  = strActualValue.substring(0,iSpace);
                  strHigh = strActualValue.substring(strLow.length()+1);

                  // Check for extra values and ignore
                  iSpace = strHigh.indexOf(" ");

                  if (iSpace != -1)
                  {
                    strHigh = strHigh.substring(0, iSpace);
                  }

                }
                sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                sbWhereExpSecond.append(strLow);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                sbWhereExpSecond.append(SYMB_AND);
                sbWhereExpSecond.append(SYMB_OPEN_PARAN);
                sbWhereExpSecond.append(SYMB_ATTRIBUTE);
                sbWhereExpSecond.append(SYMB_OPEN_BRACKET);
                sbWhereExpSecond.append(strSelectFieldName);
                sbWhereExpSecond.append(SYMB_CLOSE_BRACKET);
                sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                sbWhereExpSecond.append(strHigh);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_ATMOST)) {
                sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_ATLEAST)) {
                sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_MORE_THAN)) {
                sbWhereExpSecond.append(SYMB_GREATER_THAN);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_LESS_THAN)) {
                sbWhereExpSecond.append(SYMB_LESS_THAN);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_ON)) {
                sbWhereExpSecond.append(SYMB_EQUAL);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_ON_OR_BEFORE)) {
                sbWhereExpSecond.append(SYMB_LESS_THAN_EQUAL);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
              else if (strOperator.equals(OP_IS_ON_OR_AFTER)) {
                sbWhereExpSecond.append(SYMB_GREATER_THAN_EQUAL);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(strActualValue);
                sbWhereExpSecond.append(SYMB_QUOTE);
                sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
              }
            }
          }
        }
      }
    }
  }
  if (!start) {
    sbWhereExpSecond.append(SYMB_CLOSE_PARAN);
  }


  String strWhereExpFirst = sbWhereExpFirst.toString();
  int iLenFirst = strWhereExpFirst.length();
  String strWhereExpSecond = sbWhereExpSecond.toString();
  int iLenSecond = strWhereExpSecond.length();

  StringBuffer sbWhereExp = new StringBuffer(250);
  String strWhereExp = "";

  if ( (iLenFirst > 0) && (iLenSecond > 0) ) {
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(strWhereExpFirst);
    sbWhereExp.append(SYMB_AND);
    sbWhereExp.append(strWhereExpSecond);
    sbWhereExp.append(SYMB_CLOSE_PARAN);
    strWhereExp = sbWhereExp.toString();
  }
  else if (iLenFirst > 0) {
    strWhereExp = strWhereExpFirst;
  }
  else if (iLenSecond > 0) {
    strWhereExp = strWhereExpSecond;
  }

  mapList = DomainObject.findObjects(context, strType,strName, strRevision, strOwner, strVault, strWhereExp, "",true, select,sQueryLimit, null ,null);
} catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}

return mapList;
}



/**
* This method returns the default policy states of the type "Products".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getProductStates(Context context, String[] args)
throws Exception
{

return getStates(context, ProductLineConstants.TYPE_PRODUCTS);
}


/**
* This method returns the default policy states of the type "Builds".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getBuildsStates(Context context, String[] args)
throws Exception
{
return getStates(context, ProductLineConstants.TYPE_BUILDS);
}


/**
* This method returns the default policy states of the type "TestCase".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getTestCaseStates(Context context, String[] args)
throws Exception
{
return getStates(context, ProductLineConstants.TYPE_TEST_CASE);
}


/**
* This method returns the default policy states of the type "Document".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getDocumentsStates(Context context, String[] args)
throws Exception
{
return getStates(context, ProductLineConstants.TYPE_DOCUMENT);
}

/**
* This method returns the default policy states of the type "Model".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-6
*/
public static Object getModelStates(Context context, String[] args)
throws Exception
{
return getStates(context, ProductLineConstants.TYPE_MODEL);
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
String strAllStates = EnoviaResourceBundle.getProperty(context,SUITE_KEY,ALL_STATES,strLocale);
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



/** This method Obtains the HTML format of Product Revisions.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getProductRevisionHTML(Context context, String args[])
throws Exception
{

StringBuffer sb = new StringBuffer(70);
String strLocale = context.getSession().getLanguage();
String strLatestReleased = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRODUCT_REVISION_LATEST_RELEASED_REVISION_ONLY,strLocale);
String strLatestOnly = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRODUCT_REVISION_LATEST_REVISION_ONLY,strLocale);
sb.append("<input type=\"radio\" name=\"radio\" value=\"latestReleased\"></td><td>");
sb.append(strLatestReleased);
sb.append("<tr><td>&nbsp;<input type=\"radio\" name=\"radio\" value=\"latest\"></td><td>");
sb.append(strLatestOnly);
sb.append("</tr><tr><td>&nbsp;<input type=\"radio\" name=\"radio\" value=\"input\" checked></td><td class=\"inputField\"><input type=\"text\" name=\"Revision\" value=\"*\"></td></tr>");
String strTemp = sb.toString();
return strTemp;
}



/** This method Obtains the HTML format of Product Versions.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getProductVersionHTML(Context context, String args[])
throws Exception
{

StringBuffer sb = new StringBuffer(70);
String strLocale = context.getSession().getLanguage();
String strVersion = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRODUCT_VERSIONS,strLocale);
sb.append("<input type=\"checkbox\" name=\"IncludeVersions\"></td>&nbsp;<td>");
sb.append(strVersion);
sb.append("</td>");
String strTemp = sb.toString();

return strTemp;
}



/** This method Obtains the HTML format of Parts Revisions.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

public static Object getPartsRevisionHTML(Context context, String args[])
throws Exception
{

StringBuffer sb = new StringBuffer(200);
String strLocale = context.getSession().getLanguage();
String strSpecific = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PART_REVISION_SPECIFIC,strLocale);
String strLatestReleased = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PART_REVISION_LATEST_RELEASED,strLocale);
String strAllRevisions = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PART_REVISION_ALL_REVISIONS,strLocale);
String strReleasedAndUnreleased = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PART_REVISION_RELEASED_AND_UNRELEASED,strLocale);
sb.append("<input type=\"radio\" name=\"radio\" value=\"input\" checked></td><td><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>");
sb.append(strSpecific);
sb.append("</td><td><input type=\"text\" name=\"Revision\" value=\"*\" size=\"20\"></td></tr></table></td></tr><tr><td>&nbsp;<input type=\"radio\" name=\"radio\" value=\"latestReleased\"></td><td>");
sb.append(strLatestReleased);
sb.append("</td></tr><tr><td>&nbsp;<input type=\"radio\" name=\"radio\" value=\"allRevisions\"></td><td>");
sb.append(strAllRevisions);
sb.append("</td></tr><tr><td>&nbsp;<input type=\"radio\" name=\"radio\" value=\"allLatestRevisions\"></td><td>");
sb.append(strReleasedAndUnreleased);
return sb.toString();
}



/** This method Obtains the HTML format of Documents(Referance Documents) Revisions.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getDocumentsRevisionHTML(Context context, String args[])
throws Exception
{
StringBuffer sb = new StringBuffer(70);
String strLocale = context.getSession().getLanguage();
String strLatestOnly = EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRODUCT_REVISION_LATEST_REVISION_ONLY,strLocale);
sb.append("<input type=\"text\" size=\"20\" name=\"Revision\" value=\"*\">&nbsp;<input type=\"checkbox\" name=\"latestOnly\">");
sb.append(strLatestOnly);
return sb.toString();
}




/** This method Obtains the HTML format of Person name, i.e First name, Second Name and Last Name.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return Vector containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getPersonNameHTML(Context context, String args[])
throws Exception
{
Map programMap = (Map)JPO.unpackArgs(args);
MapList objList = (MapList)programMap.get("objectList");
int iNumOfObjects = objList.size();
StringList listSelect = new StringList(1);
listSelect.addElement(ProductLineConstants.ATTRIBUTE_FIRST_NAME);
listSelect.addElement(ProductLineConstants.ATTRIBUTE_LAST_NAME);
String arrObjId[] = new String[objList.size()];
for (int i = 0; i < objList.size(); i++) {
    Object obj = objList.get(i);
    arrObjId[i] = (String)((Map)objList.get(i)).get(DomainConstants.SELECT_ID);
}
BusinessObjectWithSelectList dataList = null;
dataList = DomainObject.getSelectBusinessObjectData(context, arrObjId, listSelect);
List resVector = new Vector();
for (int i = 0; i < iNumOfObjects; i++) {
  String strFirstName = dataList.getElement(i).getSelectData(ProductLineConstants.ATTRIBUTE_FIRST_NAME);
  String strLastName = dataList.getElement(i).getSelectData(ProductLineConstants.ATTRIBUTE_LAST_NAME);
  resVector.add("<img src=\"../common/images/icons/small/iconSmallPerson.gif\" border=\"0\" alt=\"Person\"></td><td><span class=\"object\">"
  + strFirstName + ", " + strLastName + "</span>");

}
return  resVector;
}



/**  This method Obtains the HTML format of Priority Choices.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return String containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getPriorityChoicesHTML(Context context, String args[])
throws Exception
{
  String strRetValue = null;
try {

  AttributeType atType = new AttributeType(ProductLineConstants.ATTRIBUTE_PRIORITY);

  atType.open(context);
  List list = atType.getChoices();
  atType.close(context);


  if (list != null) {

    StringBuffer sb = new StringBuffer(70);
    sb.append("<select name=\"Priority\"> <option value=\"*\">*</option>");

    for (int i=0; i<list.size(); i++) {
      String strChoice = (String)list.get(i);
      sb.append("<option value=\"");
      sb.append(strChoice + "\">");
      sb.append(strChoice);
      sb.append("</option>");
    }
    sb.append("</select>");

    strRetValue =  sb.toString();
  }
} catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}

return strRetValue;
}


/** This method deletes the selected Objects.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds object ids
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static void deleteCommon(Context context, String args[])
throws Exception
{
try {
  Map inputMap = (Map)JPO.unpackArgs(args);
  String oids[] = (String[])inputMap.get(OBJECT_IDS);
  ProductLineCommon commonBean = new ProductLineCommon();
  commonBean.deleteObjects(context, oids, true);
}
catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}
}


/** This method deletes the selected Products.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds object ids
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static void deleteProduct(Context context, String args[])
throws Exception
{

Map inputMap = (Map)JPO.unpackArgs(args);
String oids[] = (String[])inputMap.get(OBJECT_IDS);
Product productBean = (Product)DomainObject.newInstance(context,ProductLineConstants.TYPE_PRODUCTS,"ProductLine");
productBean.delete(context, oids, "deleteProduct");

}

/** To Obtain the Default Vaults.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the request Map.
* @return the Maplist of default vaults
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getDefaultVaults(Context context, String args[]) throws Exception
{

StringBuffer radioOption = null;
try {
    Map documentMap = (Map) JPO.unpackArgs(args);
    Map requestMap = (Map)documentMap.get("requestMap");
    String strMode = (String)requestMap.get(Search.REQ_PARAM_MODE);

  String strVaults = getAllVaults(context);

  radioOption = new StringBuffer(150);

  String strLocale = context.getSession().getLanguage();
      String vaultDefaultSelection = PersonUtil.getSearchDefaultSelection(context);

            String strAll = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Preferences.AllVaults",strLocale);
            String strDefault = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Preferences.UserDefaultVault",strLocale);
            String strSelected = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Preferences.SelectedVaults",strLocale);
            String strLocal = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Preferences.LocalVaults",strLocale);
       String checked = "";
            if (  PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection) )
   {
              checked = "checked";
   }
  radioOption.append("&nbsp;<input type=\"radio\" value=\"");
      radioOption.append(PersonUtil.SEARCH_DEFAULT_VAULT);
      radioOption.append("\" name=\"vaultOption\" ");
      radioOption.append(checked);
      radioOption.append(">");
  radioOption.append(strDefault);
  radioOption.append("<br>");

      checked = "";
      if (  PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection) )
  {
              checked = "checked";
  }
      radioOption.append("&nbsp;<input type=\"radio\" value=\"");
      radioOption.append(PersonUtil.SEARCH_LOCAL_VAULTS);
      radioOption.append("\" name=\"vaultOption\" ");
      radioOption.append(checked);
      radioOption.append(">");
  radioOption.append(strLocal);
  radioOption.append("<br>");

            checked = "";
     String vaults = "";
     String selVault = "";
     String selDisplayVault = "";
     if (!PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection) &&
               !PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection) &&
               !PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection) )
     {
              checked = "checked";
                              selVault = vaultDefaultSelection;
              selDisplayVault = i18nNow.getI18NVaultNames(context, vaultDefaultSelection, strLocale);
   }
  radioOption.append("&nbsp;<input type=\"radio\" value=\"");
      radioOption.append(selVault);
      radioOption.append("\" name=\"vaultOption\" ");
      radioOption.append(checked);
      radioOption.append(">");
  radioOption.append(strSelected);
  radioOption.append("&nbsp;&nbsp;<input type=\"text\" READONLY name=\"vaultsDisplay\" value =\""+selDisplayVault+"\" id=\"\" size=\"20\" onFocus=\"this.blur();\">");
  radioOption.append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"document.forms[0].vaultOption[2].checked=true;javascript:getTopWindow().showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=true&isFromSearchForm=true')\">");
      radioOption.append("<input type=\"hidden\" name=\"vaults\" value=\"");
      radioOption.append(selVault);
  radioOption.append("\" size=15>");
  radioOption.append("<br>");
      checked = "";
   if (  PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection) )
   {
              checked = "checked";
   }
      radioOption.append("&nbsp;<input type=\"radio\" value=\"");
      radioOption.append(PersonUtil.SEARCH_ALL_VAULTS);
      radioOption.append("\" name=\"vaultOption\" ");
      radioOption.append(checked);
      radioOption.append(">");
  radioOption.append(strAll);
}
catch (Throwable excp) {

 excp.printStackTrace(System.out);
}

return radioOption.toString();
}


/**
* This method returns the default policy states of the type "Document".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return string containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
public static Object getPolicyDropDownDocs(Context context, String[] args)
throws Exception
{
return  getPolicies(context, ProductLineConstants.TYPE_DOCUMENT);
}


/**
* This method returns the default policy states of the type "Document".
*
* @param context the eMatrix <code>Context</code> object
* @param args holds no arguments
* @return string containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

public static Object getPolicyDropDownSpecs(Context context, String[] args)
throws Exception
{
return  getPolicies(context, ProductLineConstants.TYPE_SPECIFICATION);
}


/**
* This method returns the default policies of the given business type.
*
* @param context the eMatrix <code>Context</code> object
* @param strType holds arguments of the request type
* @return string containing the HTML tag
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/
protected static String getPolicies(Context context, String strType)
throws Exception
{
MapList policyList = mxType.getPolicies(context,strType,false);
Map defaultMap = mxType.getDefaultPolicy(context, strType, false);
String strDefaultPolicy = (String)defaultMap.get("name");
StringBuffer sb = new StringBuffer(60);
sb.append("<select name=\"Policy\">");
for (int i=0; i<policyList.size(); i++)
{
  String strPolicy = (String)((Map)policyList.get(i)).get("name");
  sb.append("<option value=\"");
  sb.append(strPolicy);
  if (strPolicy.equals(strDefaultPolicy)) {
    sb.append(" selected");
  }
  sb.append("\">");
  sb.append(strPolicy);
  sb.append("</option>");
}
sb.append("</select>");
return sb.toString();
}


/**To Obtain All the company vaults for the wild card search.
*
* @param context the eMatrix <code>Context</code> object
* @return String containing all the vaults of the company seperated by comma
* @throws Exception if the operation fails
* @since ProductCentral 10-5
*/

protected static String getAllCompanyVaults(Context context) throws Exception {
Person person = Person.getPerson(context, context.getUser());
return person.getCompany(context).getAllVaults(context,false);
}

/** To Obtain the Default Vaults.
*
* @param context the eMatrix <code>Context</code> object
* @return string containg all the vaults seperated by comma
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

  protected static String getAllVaults(Context context) throws Exception {
  //Person person = Person.getPerson(context, context.getUser());
  return getAllCompanyVaults(context);
}


/**
* This method generates the HTML code for type chooser field.
* The types dispalyed in the chooser depends on the TO side of the realtionship passed in.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing request parameters.
* @return String HTML TAG for Type chooser
* @throws Exception if the operation fails
* @since ProductCentral 10.0.0.0
* @grade 0
*/

public String getTypeChooserHTML(Context context, String[] args) throws Exception
{
  try{
    Map documentMap = (Map) JPO.unpackArgs(args);
    Map requestMap = (Map)documentMap.get("requestMap");
    String strMode = (String)requestMap.get(Search.REQ_PARAM_MODE);
    String strCommand = (String)requestMap.get(Search.REQ_PARAM_COMMAND);
    StringBuffer strTypeChooser = new StringBuffer(150);
    String strLocale = context.getSession().getLanguage();
    String strOrganization = i18nNow.getTypeI18NString(DomainConstants.TYPE_ORGANIZATION, strLocale);
    String strCompany = i18nNow.getTypeI18NString(DomainConstants.TYPE_COMPANY, strLocale);
    String strOrganizationHidden = DomainConstants.TYPE_ORGANIZATION;
    String strOrganizationHiddenField = strOrganizationHidden.trim();
    String strCompanyHidden = DomainConstants.TYPE_COMPANY;
    String strCompanyHiddenField = strCompanyHidden.trim();

    if(strMode!=null && strMode.equals(Search.ADD_EXISTING) ) {
      if(strCommand!=null && strCommand.equals(SPECIFICATION_COMMAND) ) {
        String relationshipName = (String)requestMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME);
        String strActualRelName = PropertyUtil.getSchemaProperty(context,relationshipName);
        RelationshipType relType = new RelationshipType(strActualRelName);
        BusinessTypeList BusTypeList = new BusinessTypeList();
        BusTypeList = relType.getToTypes(context);
        StringBuffer strType = new StringBuffer(150);
        Iterator itr = BusTypeList.iterator();
        while ( itr.hasNext() ) {
        BusinessType busChildType = (BusinessType) itr.next();
        BusinessType bListTypeActual ;

        String strListTypeActual = "" ;
        String strFinalTypeName = "";
        strListTypeActual = busChildType.toString();
        strFinalTypeName = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strListTypeActual,true);
        strType.append(strFinalTypeName);
        strType.append(",");
        }
        String strTypes = "";
        String strTypeTemp = "";
        strTypeTemp = strType.toString();
        int nLen = strTypeTemp.length()-1;
        strTypes = strTypeTemp.substring(0,nLen);

        strTypeChooser.append("<input type=\"text\" READONLY name=\"TypeDisplay\" value =\"\"id=\"\" size=\"20\" onFocus=\"this.blur();\">");
        strTypeChooser.append("<input type=\"hidden\" name=\"Type\" value=\"\">");
        strTypeChooser.append("<input type=\"button\" value=\"...\" onclick=\"javascript:getTopWindow().showChooser('../common/emxTypeChooser.jsp?frameName=searchPane&formName=editDataForm&SelectType=single&SelectAbstractTypes=false&InclusionList="+strTypes+"&ObserveHidden=true&SuiteKey=eServiceSuiteProductLine&ShowIcons=true&fieldNameActual=Type&fieldNameDisplay=TypeDisplay&objectId=null','400','400')\">");
      }
    }
    else if(strMode!=null && strMode.equals(Search.CHOOSER) ) {
    if(strCommand!=null && strCommand.equals(COMPANY_COMMAND) ) {
        String strPRCParam1 = (String)requestMap.get(Search.REQ_PARAM_PRCPARAM1);
        if((strPRCParam1 != null) && (strPRCParam1.equals(MODE_DESIGN_RESPONSIBILITY)) ) {
          String strTypes = "type_Organization";
          strTypeChooser.append("<input type=\"text\" READONLY name=\"TypeDisplay\" value =\"" + strOrganization + "\"id=\"\" size=\"20\" onFocus=\"this.blur();\">");
          strTypeChooser.append("<input type=\"hidden\" name=\"Type\" value=\"" + strOrganizationHiddenField + "\">");
          strTypeChooser.append("&nbsp;&nbsp;<input type=\"button\" value=\"...\" onclick=\"javascript:getTopWindow().showChooser('../common/emxTypeChooser.jsp?frameName=searchPane&formName=editDataForm&SelectType=single&SelectAbstractTypes=false&InclusionList="+strTypes+"&ObserveHidden=true&SuiteKey=eServiceSuiteProductLine&ShowIcons=true&fieldNameActual=Type&fieldNameDisplay=TypeDisplay&objectId=null','400','400')\">");
         }
         else {
           String strTypes = "type_Company";
           strTypeChooser.append("<img src=\"images/iconSmallCompany.gif\" border=\"0\" alt=\"Company\"></img><b>" + strCompany);
           strTypeChooser.append("<input type=\"hidden\" name=\"Type\" value=\"" + strCompanyHiddenField + "\">");
        }
       }
     }
     return strTypeChooser.toString();
  } catch (Exception excp) {
    excp.printStackTrace(System.out);
    throw excp;
  }
}

/**
* This method returns the status of state field.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds arguments of the request type
* @return a boolean
* @throws Exception if the operation fails
* @since ProductCentral 10.0.0.0
* @grade 0
*/

public static Boolean getStateFieldStatus(Context context, String[] args) throws Exception
{

boolean bVal = true;
try{
  Map requestMap = (Map) JPO.unpackArgs(args);

  String strMode = (String)requestMap.get(Search.REQ_PARAM_MODE);
  String strPRCParam1 = (String)requestMap.get(Search.REQ_PARAM_PRCPARAM1);
  String strWebForm = (String)requestMap.get("form");
  if ((strWebForm != null) && (strWebForm.equals(SEARCH_PRODUCTS_FORM))) {
    if ((strMode != null) && (strMode.equals(Search.CHOOSER))) {
      if ((strPRCParam1 != null) && (strPRCParam1.equals("ProductConfigurationCreate"))) {
        bVal = false;
      }
    }
  }
} catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}

return new Boolean(bVal);
}


/**
* Method call to get the html code for populating the company organisation chooser.
*
* @param context the eMatrix <code>Context</code> object
* @param args - Holds the parameters passed from the calling method
* @return String containing the HTML tag for organisation chooser
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
* @grade 0
*/
public String getCompanyOrganizationChooserHTML(Context context, String[] args) throws Exception
{
String strLocale = context.getSession().getLanguage();

String strOrganization = i18nNow.getTypeI18NString(DomainConstants.TYPE_ORGANIZATION, strLocale);
String strCompany = i18nNow.getTypeI18NString(DomainConstants.TYPE_COMPANY, strLocale);
String strOrganizationHidden = DomainConstants.TYPE_ORGANIZATION;
String strOrganizationHiddenField = strOrganizationHidden.trim();
String strCompanyHidden = DomainConstants.TYPE_COMPANY;
String strCompanyHiddenField = strCompanyHidden.trim();
try{
   HashMap programMap = (HashMap) JPO.unpackArgs(args);

   Map requestMap = (Map)programMap.get(REQUEST_MAP);
   String strPRCParam1 = (String)requestMap.get(Search.REQ_PARAM_PRCPARAM1);

   StringBuffer strTypeChooser = new StringBuffer(150);
   if((strPRCParam1 != null) && (strPRCParam1.equals(MODE_DESIGN_RESPONSIBILITY)) ) {
    String strTypes = "type_Organization";
    strTypeChooser.append("<input type=\"text\" READONLY name=\"TypeDisplay\" value =\"" + strOrganization + "\"id=\"\" size=\"20\" onFocus=\"this.blur();\">");
    strTypeChooser.append("<input type=\"hidden\" name=\"Type\" value=\"" + strOrganizationHiddenField + "\">");
    strTypeChooser.append("&nbsp;&nbsp;<input type=\"button\" value=\"...\" onclick=\"javascript:getTopWindow().showChooser('../common/emxTypeChooser.jsp?frameName=searchPane&formName=editDataForm&SelectType=single&SelectAbstractTypes=false&InclusionList="+strTypes+"&ObserveHidden=true&SuiteKey=eServiceSuiteProductLine&ShowIcons=true&fieldNameActual=Type&fieldNameDisplay=TypeDisplay&objectId=null','400','400')\">");
   }
   else {
    String strTypes = "type_Company";
    strTypeChooser.append("<img src=\"images/iconSmallCompany.gif\" border=\"0\" alt=\"Company\"></img><b>" + strCompany);
    strTypeChooser.append("<input type=\"hidden\" name=\"Type\" value=\"" + strCompanyHiddenField + "\">");
   }
  return strTypeChooser.toString();
}
catch(Exception ex)  {
 ex.printStackTrace(System.out);
 throw ex;
}
}
    /**
 * Method call to get the Name Depending upon the Mode.
 *
 * @param context the eMatrix <code>Context</code> object
 * @param args Holds hashmap containing the following arguments
 *                  Hashmap Containing the objectList
 *                  Hashmap containg parameters like oject id, tree id, reportFormat
 * @return Object - Vector containing names in last name, first name format
 * @throws Exception if the operation fails
 * @since ProductCentral 10-0-0-0
 * @grade 0
 */
public Vector getObjectNameOnMode (Context context, String[] args) throws Exception {
  HashMap programMap = (HashMap)JPO.unpackArgs(args);
  //Gets the objectList from args
  MapList relBusObjPageList = (MapList)programMap.get("objectList");
  HashMap paramList = (HashMap)programMap.get("paramList");
  //Used to construct the HREF
  String strSuiteDir = (String)paramList.get("SuiteDirectory");
  String strJsTreeID = (String)paramList.get("jsTreeID");
  String strParentObjectId = (String)paramList.get("objectId");
  String strMode = (String)paramList.get(Search.REQ_PARAM_MODE);
  String strReportFormat = (String)paramList.get("reportFormat");
  String strFullName = null;
  Vector vctObjectName = new Vector();
  StringList strProList = new StringList(1);
  strProList.addElement(DomainConstants.SELECT_NAME);
  //No of objects
  int iNoOfObjects = relBusObjPageList.size();

  String arrObjId[] = new String[iNoOfObjects];
    //Getting the bus ids for objects in the table


    for (int i = 0; i < iNoOfObjects; i++) {
        Object obj = relBusObjPageList.get(i);
        if (obj instanceof HashMap) {
            arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
        }
        else if (obj instanceof Hashtable)
        {
            arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
        }
    }


  boolean appendTreeName= false;
  if(iNoOfObjects >0)  {
    DomainObject bo = new DomainObject(arrObjId[0]);


    String strType = bo.getInfo(context,DomainConstants.SELECT_TYPE);

    String StrSymbolicName= FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE,strType,true);
/*Begin of Modify :Raman,Enovia MatrixOne, Bug #300049 3/21/2005*/
if (StrSymbolicName!=null && (!StrSymbolicName.equalsIgnoreCase("null"))&&
    (!StrSymbolicName.equalsIgnoreCase("")))
{
    if (StrSymbolicName.equals("type_ProductLine")) {
        appendTreeName = true;
    }
  }
}
/*End of Modify :Raman,Enovia MatrixOne, Bug #300049 3/21/2005*/
  StringList listSelect = new StringList(2);
  String strAttrb1 = DomainConstants.SELECT_ID;
  String strAttrb2 = DomainConstants.SELECT_NAME;
  listSelect.addElement(strAttrb1);
  listSelect.addElement(strAttrb2);


  //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
  BusinessObjectWithSelectList attributeList = DomainObject.getSelectBusinessObjectData(context, arrObjId, listSelect);



 for (int i = 0; i < iNoOfObjects; i++) {
      String strObjId = attributeList.getElement(i).getSelectData(strAttrb1);


    //Constructing the HREF
            if(strMode != null && strMode.equals(Search.GLOBAL_SEARCH)) {
              if(strReportFormat != null && strReportFormat.length() > 0) {
            strFullName = attributeList.getElement(i).getSelectData(strAttrb2);
      } else {
          if (appendTreeName) {
              //Begin of Modify by Enovia MatrixOne for Bug# 296547 on 28 Mar 05
              //Begin of Modify by Enovia MatrixOne for Bug# 300604 on 04/12/05
			  //Modified for the Bug 323386 Begin
                strFullName = "<a href=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp"
               + "?name="
              + XSSUtil.encodeForHTMLAttribute(context,attributeList.getElement(i).getSelectData(strAttrb2))+"&amp;treeMenu="   //Modified by Enovia MatrixOne for Bug # 304580 Date 05/18/2005
              + MENU_PLCPRODUCTLINE+"&amp;emxSuiteDirectory="
              + XSSUtil.encodeForHTMLAttribute(context,strSuiteDir) +  "&amp;parentOID="
              + XSSUtil.encodeForHTMLAttribute(context,strParentObjectId) + "&amp;jsTreeID=" + XSSUtil.encodeForHTMLAttribute(context,strJsTreeID) + "&amp;objectId="
              + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "', 'null', 'null', 'false', 'content')\" class=\"object\">"
              + XSSUtil.encodeForXML(context,attributeList.getElement(i).getSelectData(strAttrb2))
              + "</a>";
              //End of Modify by Enovia MatrixOne for Bug# 296547 on 28 Mar 05
              }
              else{
              strFullName = "<a href=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?name="
                        + XSSUtil.encodeForHTMLAttribute(context,attributeList.getElement(i).getSelectData(strAttrb2))+"&amp;emxSuiteDirectory="  //Modified By Enovia MatrixOne for Bug # 304580 Date 05/18/2005
                        + XSSUtil.encodeForHTMLAttribute(context,strSuiteDir) +  "&amp;parentOID="
                        + XSSUtil.encodeForHTMLAttribute(context,strParentObjectId) + "&amp;jsTreeID=" + XSSUtil.encodeForHTMLAttribute(context,strJsTreeID) + "&amp;objectId="
                        + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "', 'null', 'null', 'false', 'content')\" class=\"object\">"
                        + XSSUtil.encodeForXML(context,attributeList.getElement(i).getSelectData(strAttrb2))
              + "</a>";
			//Modified for the Bug 323386 End
              //End of Modify by Enovia MatrixOne for Bug# 300604 on 04/12/05
              }
      }
    }
    else {
    //Looks like in full search BPS handling encoding.if we explicitly encode it show encoded characters.
    //XSSOK
     strFullName = attributeList.getElement(i).getSelectData(strAttrb2);
    }
    //Adding into the vector
    vctObjectName.add(strFullName);
  }
  return  vctObjectName;
}

   /**
    * Method to search objects based Basic Properties values.
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args - Holds the parameters passed from the calling method
       When this array is unpacked, arguments corresponding to the following String keys are found:
       queryLimit
       hdnType
       txtName
       txtDescription
       txtOwner
       txtState.
    * @return - Maplist containing the objects found
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList searchObjectsGeneric(
            Context context,
            String[] args)
            throws Exception {
            MapList mapList = null;
            try {

                    Map programMap = (Map) JPO.unpackArgs(args);
                    short sQueryLimit =
                            (short) (java
                                    .lang
                                    .Integer
                                    .parseInt((String) programMap.get(QUERY_LIMIT)));

                    String strType = (String) programMap.get("hdnType");

                    if (strType == null
                            || strType.equals("")
                            || SYMB_NULL.equalsIgnoreCase(strType)) {
                            strType = SYMB_WILD;
                    }

                    String strName = (String) programMap.get("txtName");

                    if (strName == null
                            || strName.equals("")
                            || SYMB_NULL.equalsIgnoreCase(strName)) {
                            strName = SYMB_WILD;
                    }

                    String strRadio = (String)programMap.get("radio");
                    String strRevision = null;
                    boolean bLatestRevision = false;
                    boolean bLatestReleasedRevision = false;
                    if (strRadio != null
                        && !strRadio.equals("")
                            && !(SYMB_NULL.equalsIgnoreCase(strRadio))) {
                      if (strRadio.equals("input")) {
                        strRevision = (String)programMap.get("Revision");
                        strRevision = strRevision.trim();
                      }
                      else if (strRadio.equals("latestReleased")) {
                        bLatestReleasedRevision = true;
                      }
                      else if (strRadio.equals("latest")) {
                        bLatestRevision = true;
                      }
                    }
                    if ((strRevision == null) ||
                        (strRevision.equals(""))||
                        SYMB_NULL.equalsIgnoreCase(strRevision)) {
                      strRevision = SYMB_WILD;
                    }


                    String strDesc = (String) programMap.get("txtDescription");

                    String strOwner = (String) programMap.get("txtOwner");

                    if (strOwner == null
                            || strOwner.equals("")
                            || SYMB_NULL.equalsIgnoreCase(strOwner)) {
                            strOwner = SYMB_WILD;
                    } else {
                            strOwner = strOwner.trim();
                    }

                    String strState = (String) programMap.get("txtState");

                    if (strState == null
                            || strState.equals("")
                            || SYMB_NULL.equalsIgnoreCase(strState)) {
                            strState = SYMB_WILD;
                    } else {
                            strState = strState.trim();
                    }

                    String strVault = "";
                    String strVaultOption = (String) programMap.get(VAULT_OPTION);

                    if (strVaultOption != null
                            && !strVaultOption.equals("")
                            && !(SYMB_NULL.equalsIgnoreCase(strVaultOption))) {
                            if (strVaultOption.equalsIgnoreCase(ALL)) {
                                    strVault = getAllCompanyVaults(context);
                            } else if (strVaultOption.equalsIgnoreCase(DEFAULT)) {
                                    strVault = context.getVault().getName();
                            //Begin of Modify <Ramandeep,Enovia MatrixOne> Bug #299954 3/3/2005
                            } else if (strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_SELECTED_VAULTS)) {
                                    strVault = (String) programMap.get("vaults");
                             //End of Modify for Bug# 299954
                            } else if (
                                    strVault == null
                                            || strVault.equals("")
                                            || SYMB_NULL.equalsIgnoreCase(strVault)) {
                                    strVault = SYMB_WILD;
                            }
                    }

                    StringList slSelect = new StringList(1);
                    slSelect.addElement(DomainConstants.SELECT_ID);

                    boolean bStart = true;
                    StringBuffer sbWhereExp = new StringBuffer(150);

                    if (strDesc != null
                            && (!strDesc.equals(SYMB_WILD))
                            && (!strDesc.equals(""))
                            && !(SYMB_NULL.equalsIgnoreCase(strDesc))) {
                            if (bStart) {
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
                            && !(SYMB_NULL.equalsIgnoreCase(strState))) {
                            if (bStart) {
                                    sbWhereExp.append(SYMB_OPEN_PARAN);
                                    bStart = false;
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
                     if (bLatestReleasedRevision) {
                            if (bStart) {
                              sbWhereExp.append(SYMB_OPEN_PARAN);
                              bStart = false;
                            } else {
                              sbWhereExp.append(SYMB_AND);
                            }
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                            sbWhereExp.append(SYMB_EQUAL);
                            sbWhereExp.append(STATE_RELEASE);
                            sbWhereExp.append(SYMB_CLOSE_PARAN);
                          }

                          if (bLatestRevision) {
                            if (bStart) {
                              sbWhereExp.append(SYMB_OPEN_PARAN);
                              bStart = false;
                            } else {
                              sbWhereExp.append(SYMB_AND);
                            }
                            sbWhereExp.append(SYMB_OPEN_PARAN);
                            sbWhereExp.append(DomainConstants.SELECT_REVISION);
                            sbWhereExp.append(SYMB_EQUAL);
                            sbWhereExp.append(REVISION_LAST);
                            sbWhereExp.append(SYMB_CLOSE_PARAN);
                          }

                    String strFilteredExpression = getFilteredExpression(context,programMap);

                    if ((strFilteredExpression != null)
                            && !(SYMB_NULL.equalsIgnoreCase(strFilteredExpression))
                            && !strFilteredExpression.equals("")) {
                            if (bStart) {
                                    sbWhereExp.append(SYMB_OPEN_PARAN);
                                    bStart = false;
                            } else {
                                    sbWhereExp.append(SYMB_AND);
                            }
                            sbWhereExp.append(strFilteredExpression);
                    }
                    if (!bStart) {
                            sbWhereExp.append(SYMB_CLOSE_PARAN);
                    }

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
            } catch (Exception excp) {
                    excp.printStackTrace(System.out);
                    throw excp;
            }
            return mapList;
    }//End of the method

    /**
    * Method to search Portfolio objects based Basic Properties values.
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args - Holds packed parameters passed from the calling method
    *                          queryLimit
    *                          hdnType
    *                          txtName
    *                          txtDescription
    *                          txtOwner
    *                          txtState.
    * @return - Maplist containing the objects found
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public List searchPortfolios(
        Context context,
        String[] args)
        throws Exception{
            Map programMap = (Map) JPO.unpackArgs(args);

                short sQueryLimit =
                        (short) (java
                                .lang
                                .Integer
                                .parseInt((String) programMap.get(QUERY_LIMIT)));

                String strType = (String) programMap.get("hdnType");

                if (strType == null
                        || strType.equals("")
                        || SYMB_NULL.equalsIgnoreCase(strType)) {
                        strType = SYMB_WILD;
                }

                String strName = (String) programMap.get("txtName");

                if (strName == null
                        || strName.equals("")
                        || SYMB_NULL.equalsIgnoreCase(strName)) {
                        strName = SYMB_WILD;
                }

                String strRadio = (String)programMap.get("radio");
                String strRevision = null;
                boolean bLatestRevision = false;
                boolean bLatestReleasedRevision = false;
                if (strRadio != null
                    && !strRadio.equals("")
                        && !(SYMB_NULL.equalsIgnoreCase(strRadio))) {
                  if (strRadio.equals("input")) {
                    strRevision = (String)programMap.get("Revision");
                    strRevision = strRevision.trim();
                  }
                  else if (strRadio.equals("latestReleased")) {
                    bLatestReleasedRevision = true;
                  }
                  else if (strRadio.equals("latest")) {
                    bLatestRevision = true;
                  }
                }
                if ((strRevision == null) ||
                    (strRevision.equals(""))||
                    SYMB_NULL.equalsIgnoreCase(strRevision)) {
                  strRevision = SYMB_WILD;
                }


                String strDesc = (String) programMap.get("txtDescription");

                String strOwner = (String) programMap.get("txtOwner");

                if (strOwner == null
                        || strOwner.equals("")
                        || SYMB_NULL.equalsIgnoreCase(strOwner)) {
                        strOwner = SYMB_WILD;
                } else {
                        strOwner = strOwner.trim();
                }

                String strState = (String) programMap.get("txtState");

                if (strState == null
                        || strState.equals("")
                        || SYMB_NULL.equalsIgnoreCase(strState)) {
                        strState = SYMB_WILD;
                } else {
                        strState = strState.trim();
                }

                String strVault = "";
                String strVaultOption = (String) programMap.get(VAULT_OPTION);

                if (strVaultOption != null
                        && !strVaultOption.equals("")
                        && !(SYMB_NULL.equalsIgnoreCase(strVaultOption))) {
                        if (strVaultOption.equalsIgnoreCase(ALL)) {
                                strVault = getAllCompanyVaults(context);
                        } else if (strVaultOption.equalsIgnoreCase(DEFAULT)) {
                                strVault = context.getVault().getName();
                        } else if (strVaultOption.equalsIgnoreCase(SELECTED)) {
                                strVault = (String) programMap.get(VAULT_NAME);
                        } else if (
                                strVault == null
                                        || strVault.equals("")
                                        || SYMB_NULL.equalsIgnoreCase(strVault)) {
                                strVault = SYMB_WILD;
                        }
                }

                StringList slSelect = new StringList(1);
                slSelect.addElement(DomainConstants.SELECT_ID);

                boolean bStart = true;
                StringBuffer sbWhereExp = new StringBuffer(150);

                if (strDesc != null
                        && (!strDesc.equals(SYMB_WILD))
                        && (!strDesc.equals(""))
                        && !(SYMB_NULL.equalsIgnoreCase(strDesc))) {
                        if (bStart) {
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
                        && !(SYMB_NULL.equalsIgnoreCase(strState))) {
                        if (bStart) {
                                sbWhereExp.append(SYMB_OPEN_PARAN);
                                bStart = false;
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
                 if (bLatestReleasedRevision) {
                        if (bStart) {
                          sbWhereExp.append(SYMB_OPEN_PARAN);
                          bStart = false;
                        } else {
                          sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                        sbWhereExp.append(SYMB_EQUAL);
                        sbWhereExp.append(STATE_RELEASE);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                      }

                      if (bLatestRevision) {
                        if (bStart) {
                          sbWhereExp.append(SYMB_OPEN_PARAN);
                          bStart = false;
                        } else {
                          sbWhereExp.append(SYMB_AND);
                        }
                        sbWhereExp.append(SYMB_OPEN_PARAN);
                        sbWhereExp.append(DomainConstants.SELECT_REVISION);
                        sbWhereExp.append(SYMB_EQUAL);
                        sbWhereExp.append(REVISION_LAST);
                        sbWhereExp.append(SYMB_CLOSE_PARAN);
                      }


            List objectSelects = new StringList(DomainConstants.SELECT_ID);

            MapList mapList = DomainObject.findObjects(
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

        Map mpToSideObj = new HashMap();
        Map mpToCopy = new HashMap();
        StringList slObjIdList = new StringList();
        for(int i=0;i<mapList.size();i++)
        {
                mpToSideObj = (Map)mapList.get(i);
                for(int j=0;j<objectSelects.size();j++)
                {
                    Object objIdList = (Object)mpToSideObj.get(objectSelects.get(j));
                    if(objIdList != null)
                    {
                       if(objIdList instanceof List)
                        {
                         slObjIdList = (StringList)objIdList;
                        }
                       else if(objIdList instanceof String)
                        {
                         slObjIdList.addElement((String)objIdList);
                        }
                    }//end if
                }//end for
        }//end for
        return  (MapList)mapList ;
    }//End of Method

    /**
    * The function to generate filter expression for the object selection for GBOM Replace functionality.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param programMap holds arguments passed
    * @return String , after constructing the Where clause appropriately
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6SP1
    */
    public static String generateFilterExpression
    (
        Context context,
        Map programMap
    ) throws Exception
    {

        //get ObjectId of the context object
        String strObjectId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);
        //get Source Relationship symbolic Name
        String strSrcDestRelNameSymb = (String)programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME);
        //get the isTo Flag
        String strIsTo = (String)programMap.get(Search.REQ_PARAM_IS_TO);
        //get the Middle  Destination side Relationship symbolic Name
        String strMidDestRelNameSymb = (String)programMap.get(Search.REQ_PARAM_MID_DEST_REL_NAME);
        //get the Middle  source side Relationship symbolic Name
        String strSrcMidRelNameSymb = (String)programMap.get(Search.REQ_PARAM_SRC_MID_REL_NAME);


        //Get Actual Relationship names from the symbolic names.
        String strMidDestRelName = null;
        if(strMidDestRelNameSymb != null && !strMidDestRelNameSymb.equals("") )
        {
            strMidDestRelName = PropertyUtil.getSchemaProperty(context,strMidDestRelNameSymb);
        }


        String strSrcMidRelName = null;
        if(strSrcMidRelNameSymb != null && !strSrcMidRelNameSymb.equals("") )
        {
            strSrcMidRelName = PropertyUtil.getSchemaProperty(context,strSrcMidRelNameSymb);
        }


        String strSrcDestRelName = null;
        if (strSrcDestRelNameSymb != null && !strSrcDestRelNameSymb.equals("") )
        {
            strSrcDestRelName = PropertyUtil.getSchemaProperty(context,strSrcDestRelNameSymb);
        }


        //Construct the Filter Expression.
        /*Builds an expression which will filter the existing objects in the GBOM of the context
        Feature/Product from the output maplist*/
        StringBuffer sbWhereExp = new StringBuffer(100);
        boolean start = true;
        start = false;
        sbWhereExp.append(SYMB_OPEN_PARAN);
         if ( strIsTo.equalsIgnoreCase("true")  )
         {
            sbWhereExp.append("!'to[");
            sbWhereExp.append(strMidDestRelName);
            sbWhereExp.append("].from.to[");
            sbWhereExp.append(strSrcMidRelName);
            sbWhereExp.append("].from.");
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append("'==");
            sbWhereExp.append(strObjectId);
        }
        else
        {
            sbWhereExp.append("!'from[");
            sbWhereExp.append(strMidDestRelName);
            sbWhereExp.append("].to.to[");
            sbWhereExp.append(strSrcMidRelName);
            sbWhereExp.append(".from.");
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append("'==");
            sbWhereExp.append(strObjectId);
        }
        sbWhereExp.append(SYMB_CLOSE_PARAN);
        /*Builds an expression which will filter the context object (Product/Feature)
        from the output maplist*/
        sbWhereExp.append(SYMB_AND);
        sbWhereExp.append(SYMB_OPEN_PARAN);
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("!='");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append("'");
        sbWhereExp.append(SYMB_CLOSE_PARAN);


        String strFilteredExp = null;
        String strWhereExp = sbWhereExp.toString();
        if( !strWhereExp.equals("") )
        {
            strFilteredExp = strWhereExp;
        }
        return strFilteredExp;
    }

    //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005
    /**
    * To Obtain the Latest Revisions of the Objects...
    * @param context the eMatrix <code>Context</code> object
    * @param Maplist of the Objects
    * @return a MapList , consisting of the latest revisions of the objects
    * @throws Exception if the operation fails
    * @since ProductCentral 10-6SP1
    */
    public static MapList filterForLatestRevisions(Context context,MapList mapList) throws Exception
    {
        Map tempMap;
        DomainObject tempDomObj = new DomainObject();
        BusinessObject tempBusObj = new BusinessObject();
        String strObjId;
        String strState;
        String strNextRevId;
        StringList slObjectList = new StringList();
        for(int i=0,n=mapList.size();i<n;i++)
        {
            slObjectList.add((String)((Map) mapList.get(i)).get(DomainConstants.SELECT_ID));
        }
        for(int iCount=0,iSize=mapList.size();iCount<iSize;iCount++)
        {
            tempMap = (Map) mapList.get(iCount);
            strObjId = (String) tempMap.get(DomainConstants.SELECT_ID);
            tempDomObj.setId(strObjId);
            while(!tempDomObj.isLastRevision(context)) {
                tempBusObj = tempDomObj.getNextRevision(context);
                strNextRevId = tempBusObj.getObjectId();
                if(slObjectList.contains(strNextRevId))
                {
                    mapList.remove(tempMap);
                    iSize--;
                    iCount--;
                    break;
                }
                tempDomObj.setId(strNextRevId);
            }
        }
        return mapList;
    }
    //End of Add by Vibhu,Enovia MatrixOne for Bug 311884 on 11/15/2005

/**
* For the "Find Company" funcionality.
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the HashMap containing the following arguments
*            queryLimit
*            Type
*            Name
*            Vault Option
*            Vault Display
* @return MapList , the Object ids matching the search criteria
* @throws Exception if the operation fails
* @since Product Line X3
*/
@com.matrixone.apps.framework.ui.ProgramCallable
public static Object getCompaniesForMassRDO(Context context, String[] args)
throws Exception
{
MapList mapList = null;
try {

  Map programMap = (Map) JPO.unpackArgs(args);
  short sQueryLimit = (short)(java.lang.Integer.parseInt((String)programMap.get("queryLimit")));
  String strType = (String)programMap.get("Type");
  if ( strType==null || strType.equals("") ) {
    strType = SYMB_WILD;
  }
  String strName = (String)programMap.get("Name");
  if ( strName==null || strName.equals("") ) {
            strName = SYMB_WILD;
  }

  String strVault = null;
  String strVaultOption = (String)programMap.get("vaultOption");

      if(strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_DEFAULT_VAULT)|| strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_LOCAL_VAULTS)||strVaultOption.equalsIgnoreCase(PersonUtil.SEARCH_ALL_VAULTS))
                            strVault = PersonUtil.getSearchVaults(context,false,strVaultOption);
     else
            strVault = (String)programMap.get("vaults");

  StringList select = new StringList(1);
  select.addElement(DomainConstants.SELECT_ID);

  boolean start = true;
  StringBuffer sbWhereExp = new StringBuffer(100);


  String strFilteredExpression = getFilteredExpressionForMass(context,programMap);

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
  mapList = DomainObject.findObjects(context, strType,strName, SYMB_WILD, SYMB_WILD, strVault, sbWhereExp.toString(), "",true, select,sQueryLimit);
} catch (Exception excp) {
  excp.printStackTrace(System.out);
  throw excp;
}

return mapList;
}


/**
* The function to filter the object selection and apppend the default query
* in the where clause.
*
* @param context the eMatrix <code>Context</code> object
* @param programMap holds arguments passed
* @return String , after constructing the Where clause appropriately
* @throws Exception if the operation fails
* @since ProductCentral 10-0-0-0
*/

public static String getFilteredExpressionForMass(Context context,Map programMap) throws Exception
{
String strMode = (String)programMap.get(Search.REQ_PARAM_MODE);
String strObjectId = (String)programMap.get(Search.REQ_PARAM_OBJECT_ID);
String strSrcDestRelNameSymb = (String)programMap.get(Search.REQ_PARAM_SRC_DEST_REL_NAME);
String strIsTo = (String)programMap.get(Search.REQ_PARAM_IS_TO);
String strDQ = (String)programMap.get(Search.REQ_PARAM_DEFAULT_QUERY);
String strMidDestRelNameSymb = (String)programMap.get(Search.REQ_PARAM_MID_DEST_REL_NAME);
String strSrcMidRelNameSymb = (String)programMap.get(Search.REQ_PARAM_SRC_MID_REL_NAME);

String strMidDestRelName = null;
if (strMidDestRelNameSymb != null && !strMidDestRelNameSymb.equals("") ) {
  strMidDestRelName = PropertyUtil.getSchemaProperty(context,strMidDestRelNameSymb);
}


String strSrcMidRelName = null;
if (strSrcMidRelNameSymb != null && !strSrcMidRelNameSymb.equals("") ) {
  strSrcMidRelName = PropertyUtil.getSchemaProperty(context,strSrcMidRelNameSymb);
}

String strSrcDestRelName = null;
if (strSrcDestRelNameSymb != null && !strSrcDestRelNameSymb.equals("") ) {
 strSrcDestRelName = PropertyUtil.getSchemaProperty(context,strSrcDestRelNameSymb);
}


StringBuffer sbWhereExp = new StringBuffer(100);
boolean start = true;

String strCommand = (String)programMap.get(Search.REQ_PARAM_COMMAND);

if ((strCommand != null) && !(strCommand.equals("PLCSearchPartsCommand")))
{


  if (strMode.equals(Search.ADD_EXISTING) ) {
    start = false;
    sbWhereExp.append(SYMB_OPEN_PARAN);
    /* Case where we have an Intermediate relationship */
    if(strSrcMidRelName != null && !strMidDestRelNameSymb.equals("") ) {

     if ( strIsTo.equalsIgnoreCase("true")  )  {
        sbWhereExp.append("!'to[");
        sbWhereExp.append(strMidDestRelName);
        sbWhereExp.append("].from.to[");
        sbWhereExp.append(strSrcMidRelName);
        sbWhereExp.append("].from.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
      }
      else {
        sbWhereExp.append("!'from[");
        sbWhereExp.append(strMidDestRelName);
        sbWhereExp.append("].to.to[");
        sbWhereExp.append(strSrcMidRelName);
        sbWhereExp.append(".from.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
      }
    }
    /* Case where we don't have an intermediate relationship */
    else {

      if ( strIsTo.equalsIgnoreCase("false")  )  {
        sbWhereExp.append("!('to[");
        sbWhereExp.append(strSrcDestRelName);
        sbWhereExp.append("].from.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append(")");

      }
      else {
        sbWhereExp.append("!('from[");
        sbWhereExp.append(strSrcDestRelName);
        sbWhereExp.append("].to.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append(")");

      }
    }
    sbWhereExp.append(SYMB_CLOSE_PARAN);
    /* To remove the duplicate object ids, from Add Existing sub types... */
    sbWhereExp.append(SYMB_AND);
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_ID);
    sbWhereExp.append("!='");
    sbWhereExp.append(strObjectId);
    sbWhereExp.append("'");
    sbWhereExp.append(SYMB_CLOSE_PARAN);

  if(sbWhereExp.toString().indexOf(DomainConstants.SELECT_ID)==-1)
  {
    if(sbWhereExp.length()!=0)
   {
    sbWhereExp.append(SYMB_AND);
    }
    sbWhereExp.append(SYMB_OPEN_PARAN);
    sbWhereExp.append(DomainConstants.SELECT_ID);
    sbWhereExp.append("!='");
    sbWhereExp.append(strObjectId);
    sbWhereExp.append("'");
    sbWhereExp.append(SYMB_CLOSE_PARAN);

sbWhereExp.append(SYMB_AND);
        sbWhereExp.append("!('from[");
        sbWhereExp.append(strSrcDestRelName);
        sbWhereExp.append("].to.");
        sbWhereExp.append(DomainConstants.SELECT_ID);
        sbWhereExp.append("'==");
        sbWhereExp.append(strObjectId);
        sbWhereExp.append(")");


   }

  }
}

//End of if Condition for Part

if (strDQ != null && !strDQ.equals("") ) {
 if (!start) {
   sbWhereExp.append(SYMB_AND);
   start = false;
 }

 sbWhereExp.append(SYMB_OPEN_PARAN);
 sbWhereExp.append(strDQ);
 sbWhereExp.append(SYMB_CLOSE_PARAN);
}

String strFilteredExp = null;
String strWhereExp = sbWhereExp.toString();
if( !strWhereExp.equals("") ) {
  strFilteredExp = strWhereExp;
}
return strFilteredExp;
}

/**
* The function to get the productline lines the are in persons company contect
* this filled is been modify to take typeahead functionality in 2012
*
* @param context the eMatrix <code>Context</code> object
* @param programMap holds arguments passed
* @return String , after type fielgd and getting the context company
* @throws Exception if the operation fails
* @since 2012
*/

public static String getProductLinesforSearch(Context context, String[] args) throws Exception
{
  String retvalue = "";
  //gettign the context person Company
  Person person = Person.getPerson(context);
  String strCompany = person.getCompanyId(context);
  //forming a string to get teh productline that are under the company
  retvalue= "TYPES=type_ProductLine:PRODUCTLINE_COMPANY="+strCompany;
  return retvalue;
}

}//End of the class
