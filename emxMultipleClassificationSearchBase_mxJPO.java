/*
 *  emxMultipleClassificationSearchBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.19 Wed Oct 22 16:54:23 2008 przemek Experimental przemek $
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.classification.ClassificationConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UISearch;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;


/**
 * The <code>emxMultipleClassificationSearchBase</code> class contains utility methods for
 * getting data using configurable tables  in Library Central.
 *
 *  @exclude
 */
public class emxMultipleClassificationSearchBase_mxJPO
  implements ClassificationConstants
{

   /**
     * Creates emxMultipleClassificationSearchBase object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxMultipleClassificationSearchBase_mxJPO (Context context, String[] args)
        throws Exception
    {

    }
    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns int
     * @throws Exception if the operation fails
     * @exclude
     */
public int mxMain(Context context, String[] args) throws Exception
{
    if (!context.isConnected())
        throw new Exception("not supported on desktop client");
    return 0;
}

/**
* Gets the Clasified Items based on the search Criteria
*
* @param context the eMatrix <code>Context</code> object
* @param args holds the following list of arguments:
*       0 - objectId
*       1 - matchlistLimit
*       2 - txtTypeActual
*       3 - txtName
*       4 - txtRev
*       5 - txtOwner
*       6 - levelSelction
*       7 - state
*       8 - languageStr
*       9 - vaultSelction
* @return thelist of Classified Items matching the search criteria
* @throws Exception if the operation fails
*/

@com.matrixone.apps.framework.ui.ProgramCallable
public Object searchClassifiedItems(Context context, String[] args)throws Exception
{

        try
        {

            HashMap programMap        = (HashMap) JPO.unpackArgs(args);


        //Retrieve Search criteria
        //HashMap paramMap = (HashMap)programMap.get("paramMap");
        String  parentId          = (String) programMap.get("objectId");

        String selType          = (String)programMap.get("txtTypeActual");
        String txtName          = (String)programMap.get("txtName");
        String txtRev           = (String)programMap.get("txtRev");
        String txtOwner         = (String)programMap.get("txtOwner");
        String levelSelction    = (String)programMap.get("levelSelction");
        String currentState     = (String)programMap.get("state");
        String languageStr = (String)programMap.get("languageStr");
        String txtVault   ="";

        String txtVaultOption = (String)programMap.get("vaultSelction");
        if(txtVaultOption == null) {
            txtVaultOption = PersonUtil.SEARCH_ALL_VAULTS;
        }
        //get the vaults based upon vault option selection.
        txtVault = PersonUtil.getSearchVaults(context, true, txtVaultOption);


        //trimming
        txtVault = txtVault.trim();

        if("".equals(txtVault)) {
            txtVault = PersonUtil.getDefaultVault(context);
        }

        String queryLimit = (String)programMap.get("QueryLimit");
        String latestRevision = (String)programMap.get("latestRevision");
        StringBuffer sWhereExp = new StringBuffer();


        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
           queryLimit = "0";
        }

        if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
           txtName = QUERY_WILDCARD;
        }

        if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
          txtRev = QUERY_WILDCARD;
        }

        String txtFormat = QUERY_WILDCARD;
        String txtSearch = "";

        if(latestRevision != null) {
            sWhereExp.append("(revision == last)");
        }

        if(currentState != null && !"".equals(currentState) && !currentState.equals("*"))
        {
            if(!"".equals(sWhereExp.toString())) {
                sWhereExp.append(" && ");
            }
            sWhereExp.append("(current == const'"+currentState+"')");
        }

        if (sWhereExp.toString() != null && !sWhereExp.toString().trim().equals(""))
        {
            sWhereExp.append(" && ");
        }
        sWhereExp.append(" (!(interface matchlist '");
        sWhereExp.append(INTERFACE_CLASSIFICATION_SEARCH_FILTER);
        sWhereExp.append("' ',' ))");

        String strMCMWhereExpression = getMCMAdvanceSearchWhereExpression(context, programMap);
        String andorField = (String) programMap.get("andOrField");
        if(strMCMWhereExpression!=null && !"null".equals(strMCMWhereExpression) && !"".equals(strMCMWhereExpression))
        {
                if(!"".equals(sWhereExp.toString())) {
                    sWhereExp.append(" && ");
                }
                sWhereExp.append("(");
                sWhereExp.append(strMCMWhereExpression.trim());
                sWhereExp.append(")");
        }

        String advWhereExp = UISearch.getAdvanceSearchWhereExpression(context, programMap);

        if(!"".equals(advWhereExp))
        {

            //Start: This code block is added for parametric search within enhancement
                if(!"".equals(sWhereExp)) {
                    if("and".equals(andorField) || (strMCMWhereExpression!=null && "".equals(strMCMWhereExpression))){
                        sWhereExp.append(" && ");
                    } else {
                        sWhereExp.append(" || ");
                    }
                }
            //End

                sWhereExp.append("(");
                sWhereExp.append(advWhereExp);
                sWhereExp.append(")");
        }
        StringList selectStmts = new StringList(1);
        selectStmts.addElement(DomainObject.SELECT_ID);

        
        //Start of "Interface Where condition" block

        // search for end items is performed by matching the interfaces of selected class and all its children
        // if there are large number of interfaces to match , then the where expression "interface matchlist 'list' 'delimiter'" fails 
        // so the interface list is broken to chunks of 500 interfaces in each chunk
        DomainObject parentObj = new DomainObject(parentId);
        String strInterface = parentObj.getAttributeValue(context,ATTRIBUTE_MXSYS_INTERFACE);

        StringList matchingInterfaceChunks = new StringList();
        
        if (!"".equals(strInterface)){
            
            if (levelSelction.equals("searchThisLevel")){
            	matchingInterfaceChunks.add(strInterface);
            }else if (levelSelction.equals("searchSubLevel")){
            	String strInterfaces = MqlUtil.mqlCommand(context, "print interface $1 select $2 dump $3", true,strInterface,"allchildren",",");
            	
            	if(UIUtil.isNotNullAndNotEmpty(strInterfaces)){
            		strInterfaces += ",";
            	}
            	strInterfaces += strInterface;
            	
            	StringList strlistInterfaces = FrameworkUtil.split(strInterfaces, ",");
            	List<String> listInterfaces = strlistInterfaces.toList();
            	int chunkSize = 500;
            	
            	while(listInterfaces.size() > chunkSize){
            		List chunk = listInterfaces.subList(0, chunkSize);
            		listInterfaces = listInterfaces.subList(chunkSize, listInterfaces.size());
            		
            		matchingInterfaceChunks.add(FrameworkUtil.join((String[])chunk.toArray(new String[0]), ","));
            	}
            	
            	if(listInterfaces.size() > 0){
            		matchingInterfaceChunks.add(FrameworkUtil.join((String[])listInterfaces.toArray(new String[0]), ","));
            	}
            }
        }
        
        MapList totalresultList       = new MapList();
          
        for(Iterator matchingInterfaceChunksItr = matchingInterfaceChunks.iterator();matchingInterfaceChunksItr.hasNext();){
              String matchingInterfaceChunk = (String)matchingInterfaceChunksItr.next();
        	  String strWhereExpr = sWhereExp.toString();
        	  if(UIUtil.isNotNullAndNotEmpty(strWhereExpr)){
        		  strWhereExpr += "&&";
        	  }
        	  strWhereExpr += "(interface matchlist '"+matchingInterfaceChunk+"' ',')";
        	  MapList resultList = DomainObject.findObjects(context,
                      selType,
                      txtName,
                      txtRev,
                      txtOwner,
                      txtVault,
                      strWhereExpr,
                      null,
                      true,
                      selectStmts,
                      Short.parseShort(queryLimit),
                      txtFormat,
                      txtSearch);
        	  
        	  totalresultList.addAll(resultList);
          }

          return totalresultList;

        }
        catch (Exception ex)
        {

            ex.printStackTrace();
            throw ex;
        }
    }
  /**
  * Gets the Interface List
  *
  * @param context the eMatrix <code>Context</code> object
  * @param oid ObjectId
  * @return String the list interfaces seperated by comma
  * @throws Exception if the operation fails
  */
    private static String getInterfaceList(Context context, String oid)throws Exception
    {
        // Use this approach rather than the "select derivative" one because this one
        // filters out classes that are not accessible to the user e.g. due to being
        // Inactive or Obsolete

        String expandCmd    = "expand bus $1 from relationship $2 recurse to all select bus $3 dump $4 recordsep $5";
        String expandData   = MqlUtil.mqlCommand(context, expandCmd,
                                                    oid,
                                                    LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
                                                    "attribute["+LibraryCentralConstants.ATTRIBUTE_MXSYSINTERFACE+"]",
                                                    ",",
                                                    "|"
                                                );
        StringList rows     = FrameworkUtil.split(expandData, "|");
        Iterator rowIter    = rows.iterator();
        StringList lst      = new StringList();
        while (rowIter.hasNext()) {
            String row = (String) rowIter.next();
            // something like: 2,Subclass,to,Part Family,BOLT,-,BOLT.1119550075029
            StringList fields = FrameworkUtil.split(row, ",");
            lst.add(fields.get(fields.size() - 1));
        }
        String result = FrameworkUtil.join(lst, ",");
        return result;
    }

    /**
     * Gets the name of the 'Operator' list box.
     *
     * @param strAttributeName The name of the attribute
     * @return The name of the 'Operator' list box
     * @exclude
     */
    public static String getNameOfOperatorListBox(String strAttributeName)
    {
        return "MCM_operator_"+strAttributeName;
    }

    /**
     * Gets the name of the 'Select Value' text box.
     *
     * @param strAttributeName The name of the attribute
     * @return The name of the 'Select Value' text box
     * @exclude
     */
    public static String getNameOfSelectValueListBox(String strAttributeName)
    {
        return "MCM_selectValue_"+strAttributeName;
    }

    /**
     * Gets the name of the 'Enter Value' text box.
     *
     * @param strAttributeName The name of the attribute
     * @return The name of the 'Enter Value' text box
     * @exclude
     */
    public static String getNameOfEnterValueTextBox(String strAttributeName)
    {
        return "MCM_enterValue_"+strAttributeName;
    }
    /**
     * Gets the name of the 'Enter Value'Unit text box.
     *
     * @param strAttributeName The name of the attribute
     * @return The name of the 'Enter Value' text box
     * @exclude
     */
    public static String getNameOfEnterValueUnitTextBox(String strAttributeName)
    {
        return "MCM_enterValue_units_"+strAttributeName;
    }
    /**
     * Separates the parameters that starts with MCM_ into a another HashMap object.
     *
     * @param paramMap HashMap object containing all the parameters
     * @return A new HashMap object with all the attributes that starts with MCM_
     * @exclude
     */
    public static HashMap getMCMAttributesOnly (HashMap paramMap)
    {

        HashMap mapMCMAttributes = new HashMap();

        //Find all the keys in the hashmap
        java.util.Set setKeys = paramMap.keySet();

        Iterator itrSetKeys = setKeys.iterator();
        while (itrSetKeys.hasNext() )
        {
            String strKey = (String)itrSetKeys.next();
            String strValue = "";

            if (strKey != null && strKey.indexOf("MCM_") != -1)
            {
                strValue = (String)paramMap.get(strKey);
                mapMCMAttributes.put(strKey, strValue);
            }// if !
        }// while !

        return mapMCMAttributes;

    }// getMCMAttributesOnly(..) !

    /**
     * Forms the where expression for the MCM_xxx attributes.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param paramMap HashMap object containing all the parameters
     * @return A WHERE expression for the MCM_ attributes with the operator, enter value, select value etc.
     * @throws FrameworkException if the operation fails
     */
    public static String getMCMAdvanceSearchWhereExpression(Context context, HashMap requestParametersMap) throws FrameworkException
    {


        HashMap paramMap = getMCMAttributesOnly (requestParametersMap);

        StringBuffer sbWhereExp = new StringBuffer(256);
        try
        {
                StringList attrList   = new StringList();
                double clientTZOffset = 0.0;
                Locale reqLocale      = null;

                String strattrList = (String)paramMap.get("MCM_AttributeList");
                String andorField = (String) requestParametersMap.get("andOrField");

                if(strattrList != null && !"".equals(strattrList))
                {

                        attrList = FrameworkUtil.split(strattrList, "|");

                        String strTimezone = (String)requestParametersMap.get("timeZone");
                        String strLocaleLang = (String)requestParametersMap.get("reqLocaleLang");
                        String strLocaleCty = (String)requestParametersMap.get("reqLocaleCty");
                        String strLocaleVar = (String)requestParametersMap.get("reqLocaleVar");

                        if (strTimezone!= null && !"null".equals(strTimezone) && !"".equals(strTimezone))
                        {
                            clientTZOffset    = (new Double(strTimezone)).doubleValue();
                        }

                        if (strLocaleLang!= null && strLocaleCty!= null && strLocaleVar!= null)
                        {
                            reqLocale = new Locale( strLocaleLang,strLocaleCty, strLocaleVar);
                        }else{
                            reqLocale = Locale.getDefault();
                        }

                }

                // Attribute string constants
                String sMatrixType         = "Type";
                String sMatrixName         = "Name";
                String sMatrixRevision     = "Revision";
                String sMatrixOwner        = "Owner";
                String sMatrixVault        = "Vault";
                String sMatrixDescription  = "Description";
                String sMatrixCurrent      = "Current";
                String sMatrixModified     = "Modified";
                String sMatrixOriginated   = "Originated";
                String sMatrixGrantor      = "Grantor";
                String sMatrixGrantee      = "Grantee";
                String sMatrixPolicy       = "Policy";


                // Symbolic Operator string constants
                String sOr                 = " || ";
                String sAnd                = " && ";
                String sEqual              = " == const ";
                String sNotEqual           = " != const ";
                String sGreaterThan        = " > const ";
                String sLessThan           = " < const ";
                String sGreaterThanEqual   = " >= const ";
                String sLessThanEqual      = " <= const ";
                String sMatch              = " ~= const ";
                String sQuote              = "\"";
                String sWild               = QUERY_WILDCARD;
                String sOpenParen          = "(";
                String sCloseParen         = ")";
                String sAttribute          = "attribute";
                String sOpenBracket        = "[";
                String sCloseBracket       = "]";

                // Translated string operators
                String sMatrixIncludes     = "Includes";;
                String sMatrixIsExactly    = "IsExactly";;
                String sMatrixIsNot        = "IsNot";
                String sMatrixMatches      = "Matches";
                String sMatrixBeginsWith   = "BeginsWith";
                String sMatrixEndsWith     = "EndsWith";
                String sMatrixEquals       = "Equals";
                String sMatrixDoesNotEqual = "DoesNotEqual";
                String sMatrixIsBetween    = "IsBetween";
                String sMatrixIsAtMost     = "IsAtMost";
                String sMatrixIsAtLeast    = "IsAtLeast";
                String sMatrixIsMoreThan   = "IsMoreThan";
                String sMatrixIsLessThan   = "IsLessThan";
                String sMatrixIsOn         = "IsOn";
                String sMatrixIsOnOrBefore = "IsOnOrBefore";
                String sMatrixIsOnOrAfter  = "IsOnOrAfter";

                String sAttrib = "";
                String sValue  = "";

                Pattern patternAttribute  = new Pattern("");
                Pattern patternOperator   = new Pattern("");

                //String andorField = (String) paramMap.get("andOrField");

                //Loop to get the parameter values
                for (int i = 0; i < attrList.size(); i++)
                {
                        String sTextValue            = "";
                        String sSelectValue          = "";
                        String sTextUnitValue        = "";
                        String strAttributeDataType  = "";
                        sValue                       = "";
                        sAttrib             = (String)attrList.elementAt(i);
                        patternAttribute    = new Pattern(sAttrib);
                        patternOperator     = new Pattern((String)paramMap.get ( getNameOfOperatorListBox(sAttrib) ));

                        sTextValue          = (String) paramMap.get( getNameOfEnterValueTextBox(sAttrib) );
                        sSelectValue        = (String) paramMap.get( getNameOfSelectValueListBox(sAttrib) );


                        try {
                            AttributeType attributeType = new AttributeType(sAttrib);
                            attributeType.open(context);
                            strAttributeDataType = attributeType.getDataType().trim();
                            attributeType.close(context);
                            if (strAttributeDataType == null || "null".equals(strAttributeDataType) || "".equals(strAttributeDataType)) {
                                strAttributeDataType = "";
                            }//if !
                        } catch (Exception exp) {
                            throw (new FrameworkException("Exception caught while getting the attribute type  of '"
                                    + sAttrib
                                    + "':"
                                    + exp.getMessage()));
                        }



                        if("integer".equalsIgnoreCase(strAttributeDataType) || "real".equalsIgnoreCase(strAttributeDataType))
                        {
                            boolean associateWithDimension = UOMUtil.isAssociatedWithDimension(context, sAttrib);
                            if(associateWithDimension)
                            {
                                sTextUnitValue = (String) paramMap.get( getNameOfEnterValueUnitTextBox(sAttrib) );
                            }

                        }
                        if ((sTextValue == null) || (sTextValue.equals("")) || (sTextValue.equals(QUERY_WILDCARD)))
                        {
                                if(sSelectValue != null)
                                {
                                        sValue = sSelectValue;
                                }
                        }
                        else
                        {
                            if (sTextUnitValue != null  && !"".equals(sTextUnitValue.trim()) && !"null".equalsIgnoreCase(sTextUnitValue))
                            {
                                sValue = sTextValue.trim() + " " + sTextUnitValue;
                            }
                            else
                            {
                                sValue = sTextValue.trim();
                                sTextUnitValue = "";
                            }
                        }



                        //To get the where expression if any parameter values exists
                        if (sValue!=null && (!sValue.equals("")) && (!sValue.equals(QUERY_WILDCARD)))
                        {

                                // If some conditiion already added then append && before adding another condition
                                //Start: This code blosk is added for parametric search within enhancement
                               if (sbWhereExp.length() > 0) {
                                        if("and".equals(andorField)){
                                     sbWhereExp.append(sAnd);
                                        } else {
                                                sbWhereExp.append(sOr);
                                        }
                                }
                                //End

                                sbWhereExp.append(sOpenParen);

                                if (patternAttribute.match(sMatrixType) ||
                                    patternAttribute.match(sMatrixName) ||
                                    patternAttribute.match(sMatrixRevision) ||
                                    patternAttribute.match(sMatrixOwner) ||
                                    patternAttribute.match(sMatrixVault) ||
                                    patternAttribute.match(sMatrixDescription) ||
                                    patternAttribute.match(sMatrixCurrent) ||
                                    patternAttribute.match(sMatrixModified) ||
                                    patternAttribute.match(sMatrixOriginated) ||
                                    patternAttribute.match(sMatrixGrantor) ||
                                    patternAttribute.match(sMatrixGrantee) ||
                                    patternAttribute.match(sMatrixPolicy) ||
                                    sAttrib.equals("State"))
                                {
                                        sbWhereExp.append(sAttrib);
                                }
                                else
                                {
                                        sbWhereExp.append(sAttribute);
                                        sbWhereExp.append(sOpenBracket);
                                        sbWhereExp.append(sAttrib);
                                        sbWhereExp.append(sCloseBracket);
                                }

                                if (patternOperator.match(sMatrixIncludes))
                                {
                                        sbWhereExp.append(sMatch);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sWild);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sWild);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsExactly))
                                {
                                        sbWhereExp.append(sEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsNot))
                                {
                                        sbWhereExp.append(sNotEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixMatches))
                                {
                                        sbWhereExp.append(sMatch);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixBeginsWith))
                                {
                                        sbWhereExp.append(sMatch);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sWild);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixEndsWith))
                                {
                                        sbWhereExp.append(sMatch);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sWild);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixEquals))
                                {
                                        sbWhereExp.append(sEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixDoesNotEqual))
                                {
                                        sbWhereExp.append(sNotEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsBetween))
                                {
                                        sValue = sValue.trim();

                                        int iSpace = sValue.indexOf(" ");
                                        String sLow  = "";
                                        String sHigh = "";

                                        if (iSpace == -1)
                                        {
                                                sLow  = sValue;
                                                sHigh = sValue;
                                        }
                                        else
                                        {
                                                sLow  = sValue.substring(0,iSpace);
                                                sHigh = sValue.substring(sLow.length()+1,sValue.length());

                                                // Check for extra values and ignore
                                                iSpace = sHigh.indexOf(" ");

                                                if (iSpace != -1)
                                                {
                                                        sHigh = sHigh.substring(0, iSpace);
                                                }
                                        }

                                        sbWhereExp.append(sGreaterThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sLow);
                                        if( !"".equals( sTextUnitValue ) ) {
                                            sbWhereExp.append( " " );
                                            sbWhereExp.append( sTextUnitValue );
                                        }
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sCloseParen);
                                        sbWhereExp.append(sAnd);
                                        sbWhereExp.append(sOpenParen);

                                        if (patternAttribute.match(sMatrixDescription) ||
                                            patternAttribute.match(sMatrixCurrent)     ||
                                            patternAttribute.match(sMatrixModified)    ||
                                            patternAttribute.match(sMatrixOriginated)  ||
                                            patternAttribute.match(sMatrixGrantor)     ||
                                            patternAttribute.match(sMatrixGrantee)     ||
                                            patternAttribute.match(sMatrixPolicy))
                                        {
                                                sbWhereExp.append(sAttrib);
                                        }
                                        else
                                        {
                                                sbWhereExp.append(sAttribute);
                                                sbWhereExp.append(sOpenBracket);
                                                sbWhereExp.append(sAttrib);
                                                sbWhereExp.append(sCloseBracket);
                                        }
                                        sbWhereExp.append(sLessThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sHigh);
                                        if( !"".equals( sTextUnitValue ) ) {
                                            sbWhereExp.append( " " );
                                            sbWhereExp.append( sTextUnitValue );
                                        }
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsAtMost))
                                {
                                        sbWhereExp.append(sLessThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsAtLeast))
                                {
                                        sbWhereExp.append(sGreaterThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsMoreThan))
                                {
                                        sbWhereExp.append(sGreaterThan);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsLessThan))
                                {
                                        sbWhereExp.append(sLessThan);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsOn))
                                {
                                        String startDate = eMatrixDateFormat.getFormattedInputDateTime(context,sValue,"12:00:00 AM",clientTZOffset,reqLocale);
                                        String endDate = eMatrixDateFormat.getFormattedInputDateTime(context,sValue,"11:59:59 PM",clientTZOffset,reqLocale);
                                        sbWhereExp.append(sLessThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(endDate);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sAnd);
                                        sbWhereExp.append(" ");

                                        if (patternAttribute.match(sMatrixType) ||
                                            patternAttribute.match(sMatrixName) ||
                                            patternAttribute.match(sMatrixRevision) ||
                                            patternAttribute.match(sMatrixOwner) ||
                                            patternAttribute.match(sMatrixVault) ||
                                            patternAttribute.match(sMatrixDescription) ||
                                            patternAttribute.match(sMatrixCurrent) ||
                                            patternAttribute.match(sMatrixModified) ||
                                            patternAttribute.match(sMatrixOriginated) ||
                                            patternAttribute.match(sMatrixGrantor) ||
                                            patternAttribute.match(sMatrixGrantee) ||
                                            patternAttribute.match(sMatrixPolicy) ||
                                            sAttrib.equals("State"))
                                        {
                                            sbWhereExp.append(sAttrib);
                                        }
                                        else
                                        {
                                            sbWhereExp.append(sAttribute);
                                            sbWhereExp.append(sOpenBracket);
                                            sbWhereExp.append(sAttrib);
                                            sbWhereExp.append(sCloseBracket);
                                        }

                                        sbWhereExp.append(sGreaterThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(startDate);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsOnOrBefore))
                                {
                                        sValue=eMatrixDateFormat.getFormattedInputDateTime(context,sValue,"11:59:59 PM",clientTZOffset,reqLocale);
                                        sbWhereExp.append(sLessThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }
                                else if (patternOperator.match(sMatrixIsOnOrAfter))
                                {
                                        sValue=eMatrixDateFormat.getFormattedInputDateTime(context,sValue,"12:00:00 AM",clientTZOffset,reqLocale);
                                        sbWhereExp.append(sGreaterThanEqual);
                                        sbWhereExp.append(sQuote);
                                        sbWhereExp.append(sValue);
                                        sbWhereExp.append(sQuote);
                                }

                                sbWhereExp.append(sCloseParen);
                        }//if !


                }//for !

                return sbWhereExp.toString();
        }//try !
        catch(Exception ex)
        {
            throw new FrameworkException(ex);
        }

    }// getMCMAdvanceSearchWhereExpression(..) !

    /**
     * Breaks the bigger tokenlist of interface where clause into shorter tokenlists of multiple interface matchlist
     * where clauses.
     *
     * @param strTokenList The list of tokens separated with the delimiter given in strGivenTokenDelimiter parameter.
     *        If this parameter is null or "" then result is empty StringList object.
     * @param strGivenTokenDelimiter The delimiter used for separating the indivisual tokens in strTokenList.
     *        If this parameter is null or "" then result is empty StringList object.
     * @param strRequiredTokenDelimiter The delimiter to be used for separating the indivisual tokens in the result.
     *        If this parameter is null then it is considered as "" while concatinating the tokens in result.
     * @param intRequiredTokenCount The maximum no. of tokens present in each of the token list.
     *        If this parameter is <= 0 then result is empty StringList object.
     * @return Where clause containing multiple shorter matchlist conditions
     *
     */

    public static StringList chunkMatchlist(String strTokenList,
                                                           String strGivenTokenDelimiter,
                                                           String strRequiredTokenDelimiter,
                                                           int intRequiredTokenCount)
    {
        StringList strlistResult = new StringList();

    // Check the passed parameters
        if (strTokenList == null || "".equals(strTokenList))
        {
            return strlistResult; // Empty
        }
        if (strGivenTokenDelimiter == null || "".equals(strGivenTokenDelimiter))
        {
            System.err.println("Error : breakTokenList() : strGivenTokenDelimiter parameter is not given.");
            return strlistResult;
        }
        if (strRequiredTokenDelimiter == null)
        {
            strRequiredTokenDelimiter = "";
        }
        if (intRequiredTokenCount <= 0)
        {
            System.err.println("Error : breakTokenList() : intRequiredTokenCount parameter is invalid '"+intRequiredTokenCount+"'.");
            return strlistResult; // Empty
        }

    //Find the count of tokens in the given list
        ArrayList arrlistTokens = new ArrayList();
        StringTokenizer st = new StringTokenizer(strTokenList, strGivenTokenDelimiter);

        while (st.hasMoreTokens())
        {
            arrlistTokens.add(st.nextToken());
        }//while !

    // Form the resultant string of limited tokens
        StringBuffer sbuf = new StringBuffer(256);
        StringList strlistInterfaces = new StringList();
        int intTokenCount = 0;
        for(int i=0; i<arrlistTokens.size(); i++)
        {
            String strToken = (String)arrlistTokens.get(i);
            intTokenCount ++;

            if (intTokenCount <= intRequiredTokenCount)
            {
                if (sbuf.length() > 0)
                {
                    sbuf.append(strRequiredTokenDelimiter);
                }
                sbuf.append(strToken);
            }//if !
            else
            {
                strlistInterfaces.add(sbuf.toString());
                sbuf = new StringBuffer(256);
                intTokenCount = 1;
                sbuf.append(strToken);
            }//else !
        }//for !

        if (sbuf.length() > 0)
        {
            strlistInterfaces.add(sbuf.toString());
            sbuf = null;
        }

    //Form the matchlist condition

        for(int i=0; i<strlistInterfaces.size(); i++)
        {
            String strInterfaceList = (String)strlistInterfaces.get(i);
            sbuf = new StringBuffer(256);
            sbuf.append("(interface matchlist '");
            sbuf.append(strInterfaceList);
            sbuf.append("' '");
            sbuf.append(strRequiredTokenDelimiter);
            sbuf.append("')");
            strlistResult.add(sbuf.toString());
        }//for !
        strlistInterfaces.clear();
        strlistInterfaces = null;

        return strlistResult;
    }// chunkMatchlist(..) !

}
