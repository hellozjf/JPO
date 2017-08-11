/*
**  emxSearchBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UISearch;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.StringUtils;


/**
 * The <code>emxSearchBase</code> class contains methods for General Search in the application.
 *
 * @version AEF 10.5.0.0 - Copyright(c) 2003, MatrixOne, Inc.
 */

public class emxSearchBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public emxSearchBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 10.5
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));            
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.AEFFullSearch", new Locale(languageStr));            
            throw new Exception(exMsg);
        }
        return 0;
    }
	//Added for the BUG: 347572
    /*  This method get Objects including the version Objects for the specified criteria in General Search.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    txtName        - a String of specified criteria name
     *    txtRev         - a String of specified criteria revision
     *    txtTypeActual  - a String of specified criteria type
     *    vaultSelction  - a String of specified criteria vault
     *    QueryLimit     - a String of limit on the number of objects found
     *    latestRevision - a String containing the latestRevision info of the object
     * @return MapList containing objects for search result
     * @throws Exception if the operation fails
     */
    public MapList getIntegrationSearchResult(Context context , String[] args)
    throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        paramMap.put("includeVersionObjects","true");
        args = JPO.packArgs(paramMap);
        return getGeneralSearchResult(context, args);
    }
    //Ended for the BUG: 347572

    /**
     * This method get Objects for the specified criteria in General Search.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    txtName        - a String of specified criteria name
     *    txtRev         - a String of specified criteria revision
     *    txtTypeActual  - a String of specified criteria type
     *    vaultSelction  - a String of specified criteria vault
     *    QueryLimit     - a String of limit on the number of objects found
     *    latestRevision - a String containing the latestRevision info of the object
     * @return MapList containing objects for search result
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public MapList getGeneralSearchResult(Context context , String[] args)
       throws Exception
    {

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);

        //Retrieve Search criteria
        String selType          = (String)paramMap.get("txtTypeActual");
        String txtName          = (String)paramMap.get("txtName");
        String txtRev           = (String)paramMap.get("txtRev");
        String txtDiscussionKeywords   = (String)paramMap.get("txtDiscussionKeywords");


        String txtVault   ="";

        String txtVaultOption = (String)paramMap.get("vaultSelction");
		String includeVersionObjects = (String)paramMap.get("includeVersionObjects");
        if(txtVaultOption == null) {
          txtVaultOption = "";
        }

        //get the vaults based upon vault option selection.
        txtVault = PersonUtil.getSearchVaults(context, true, txtVaultOption);


        //trimming
        txtVault = txtVault.trim();

        if("".equals(txtVault)) {
            txtVault = PersonUtil.getDefaultVault(context);
        }

        String queryLimit = (String)paramMap.get("QueryLimit");
        String latestRevision = (String)paramMap.get("latestRevision");


        if (queryLimit == null || "null".equals(queryLimit) || "".equals(queryLimit)){
            queryLimit = (String)paramMap.get("queryLimit");
            if (queryLimit == null || "null".equals(queryLimit) || "".equals(queryLimit)) {
                queryLimit = "100";
            }
        }

        if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
           txtName = "*";
        }

        if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
          txtRev = "*";
        }

        String txtOwner  = "*";


        StringBuffer sWhereExp = new StringBuffer(256);
        String txtFormat = "*";
        String txtSearch = "";


        if(latestRevision != null) {
            sWhereExp.append("(revision == last)");
        }

        String advWhereExp = UISearch.getAdvanceSearchWhereExpression(context, paramMap);
        if(!"".equals(advWhereExp))
        {
                if(!"".equals(sWhereExp.toString())) {
                        sWhereExp.append(" && ");
                }
                sWhereExp.append("(");
                sWhereExp.append(advWhereExp);
                sWhereExp.append(")");
        }


        SelectList resultSelects = new SelectList(7);
        resultSelects.add(DomainConstants.SELECT_ID);
        resultSelects.add(DomainConstants.SELECT_TYPE);
        resultSelects.add(DomainConstants.SELECT_NAME);
        resultSelects.add(DomainConstants.SELECT_REVISION);
        resultSelects.add(DomainConstants.SELECT_VAULT);
        resultSelects.add(DomainConstants.SELECT_DESCRIPTION);
        resultSelects.add(DomainConstants.SELECT_CURRENT);
        resultSelects.add(DomainConstants.SELECT_POLICY);
        //*****added for the bug 339627******//
		if(includeVersionObjects == null )
        resultSelects.add("attribute["+DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT+"]");
        //**********************************//
        if(txtDiscussionKeywords!=null && !"null".equals(txtDiscussionKeywords)&& !"".equals(txtDiscussionKeywords) && !"*".equals(txtDiscussionKeywords))
        {
            resultSelects.add("from["+DomainConstants.RELATIONSHIP_THREAD+"].to.id");
        }
        if (DomainConstants.TYPE_BUG.equals(selType))
        {
            resultSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
            resultSelects.add("attribute["+DomainConstants.ATTRIBUTE_KEYWORDS+"]");
        }

        MapList totalresultList = null;
        totalresultList = DomainObject.findObjects(context,
                                                   selType,
                                                   txtName,
                                                   txtRev,
                                                   txtOwner,
                                                   txtVault,
                                                   sWhereExp.toString(),
                                                   null,
                                                   true,
                                                   resultSelects,
                                                   Short.parseShort(queryLimit),
                                                   txtFormat,
                                                   txtSearch);
        //*****added for the bug 339627*****//
		if( includeVersionObjects == null ){
			for (int count = 0; count<totalresultList.size(); count++)
			{
				Map allresultElements = (Map)totalresultList.get(count);
				String isVersionObject = (String)allresultElements.get("attribute[Is Version Object]");
				if(isVersionObject.equalsIgnoreCase("true"))
				{
					totalresultList.remove(count);
					count--;
				}   
			}
		}
        //********************************//
		int qLimit = Integer.parseInt(queryLimit);
		if (totalresultList != null && totalresultList.size() == qLimit) {
			StringBuffer sbObjLimitWarning = new StringBuffer();
			String languageStr = (String)paramMap.get("languageStr");
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(languageStr), "emxComponents.Warning.ObjectFindLimit"));             
            sbObjLimitWarning.append(qLimit);            
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(languageStr), "emxComponents.Warning.Reached"));            
            MqlUtil.mqlCommand(context, "WARNING $1",sbObjLimitWarning.toString());
        }
        return totalresultList;
    }
//***********Contained In Search**************

    /**
     * This method get Objects for the specified criteria in General Search.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    txtName        - a String of specified criteria name
     *    txtRev         - a String of specified criteria revision
     *    txtTypeActual  - a String of specified criteria type
     *    vaultSelction  - a String of specified criteria vault
     *    QueryLimit     - a String of limit on the number of objects found
     *    latestRevision - a String containing the latestRevision info of the object
     * @return MapList containing objects for search result
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getContextSearchResults(Context context , String[] args)
       throws Exception
        {
           
             return  buildSearchResults(context, args,false);
        
        }

/**
    * getFormatTNR - Will get the type,name,revision & Vaults for the Summary Table
    *       Will be called in the mxlink Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7SP1
    *  @grade 0
    */
    public Vector getFormatTNR(Context context , String[] args) throws Exception {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Vector tnrVector=new Vector();
        MapList busObjList = (MapList)paramMap.get("objectList");
        int iNoOfObjects = busObjList.size();
        for (int obj = 0; obj < iNoOfObjects; obj++) {
            Map map = (Map)busObjList.get(obj);
            String mxlink = (String)map.get("mxLink");
            tnrVector.add(mxlink);
        }
        return tnrVector;
    }

       /**
    * getLatestREVSearchResult - Will get the Latest revisions to the Summary Table
    *       
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @throws Exception if the operation fails
    *  @since Common 10-7 SP1
    *  @grade 0
    */
        public MapList getLatestREVSearchResult(Context context , String[] args)
       throws Exception
    {
           
             return  buildSearchResults(context, args, true);
       
        }
       /**
    *  buildSearchResults - method for building the maplist
    *                for the columns
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this methoda
    *  @return maplist 
    *  @throws Exception if the operation fails
    *  @since Common 10-7SP1
    *  @grade 0
    */
      public MapList buildSearchResults(Context context , String[] args, boolean latest)
       throws Exception
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            MapList mapList=new MapList();

            String selType          = (String)paramMap.get("txtTypeActual");
            String txtName          = (String)paramMap.get("txtName");
            String txtRev           = (String)paramMap.get("txtRev");
            String txtDiscussionKeywords= (String)paramMap.get("txtDiscussionKeywords");
            String txtContainedIn           = (String)paramMap.get("txtContainedIn");
            String txtActualContainedIn           = (String)paramMap.get("txtActualContainedIn");
            String languageStr = (String)paramMap.get("languageStr");
            String findMxLink = (String)paramMap.get("mxLink");

             Map map                     = null;
            String queryLimit = (String)paramMap.get("QueryLimit");

        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
           queryLimit = "0";
        }
            if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
                txtName = "*";
             }

             if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
               txtRev = "*";
             }
             String txtVault   ="";
             String txtVaultOption = (String)paramMap.get("vaultSelction");

             if(txtVaultOption == null) {
               txtVaultOption = "";
             }

             //get the vaults based upon vault option selection.
             txtVault = PersonUtil.getSearchVaults(context, true, txtVaultOption);
             
               //trimming
            txtVault = txtVault.trim();

            if("".equals(txtVault)) {
                txtVault = PersonUtil.getDefaultVault(context);
            }
             if (txtDiscussionKeywords == null || "null".equals(txtDiscussionKeywords) || "".equals(txtDiscussionKeywords))
             {
               txtDiscussionKeywords = "*";
             }
             txtDiscussionKeywords=txtDiscussionKeywords.trim();

            StringList resultSelects = new StringList(7);
            resultSelects.add(DomainConstants.SELECT_ID);
            resultSelects.add(DomainConstants.SELECT_TYPE);
            resultSelects.add(DomainConstants.SELECT_NAME);
            resultSelects.add(DomainConstants.SELECT_REVISION);
            resultSelects.add(DomainConstants.SELECT_VAULT);
            resultSelects.add(DomainConstants.SELECT_DESCRIPTION);
            resultSelects.add(DomainConstants.SELECT_CURRENT);
            resultSelects.add(DomainConstants.SELECT_POLICY);

            if(txtDiscussionKeywords!=null && !"null".equals(txtDiscussionKeywords)&& !"".equals(txtDiscussionKeywords) && !"*".equals(txtDiscussionKeywords))
            {
                resultSelects.add("from["+DomainConstants.RELATIONSHIP_THREAD+"].to.id");
            }
            if (DomainConstants.TYPE_BUG.equals(selType) && !"*".equals(txtDiscussionKeywords))
            {

                resultSelects.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
                resultSelects.add("attribute["+DomainConstants.ATTRIBUTE_KEYWORDS+"]");
            }

            if (txtContainedIn==null||"null".equals(txtContainedIn) || "".equals(txtContainedIn))
            {
                mapList=getGeneralSearchResult(context,args);
            }
            else
            {
                StringBuffer sWhereExp = new StringBuffer(256);
                 if(latest){
                       sWhereExp.append("(revision == last)");
                             }
                String advWhereExp = UISearch.getAdvanceSearchWhereExpression(context, paramMap);
                if(!"".equals(advWhereExp))
                {
                        if(!"".equals(sWhereExp.toString())) {
                                sWhereExp.append(" && ");
                        }
                        sWhereExp.append("(");
                        sWhereExp.append(advWhereExp);
                        sWhereExp.append(")");
                }

                String relationship_list=UISearch.getContainedInObjectRelationship(context,txtActualContainedIn);
                String strWhereExp = sWhereExp.toString();
                strWhereExp = strWhereExp.replaceAll("\"","'");
                
                StringBuffer mqlString = new StringBuffer();
                StringBuffer methStr = new StringBuffer();
                String[] methArgs = new String[3];
                methStr.append("temp webreport searchcriteria $1 ");
                mqlString.append("escape temp query bus \"");
                mqlString.append((String)paramMap.get("txtTypeActual"));
                mqlString.append("\" \"");
                mqlString.append(txtName);
                mqlString.append("\" ");
                mqlString.append(txtRev);
                if(UIUtil.isNotNullAndNotEmpty(txtVaultOption))
                {
                    mqlString.append(" vault \"");
                    mqlString.append(txtVault);
                    mqlString.append("\" ");
                }
                else
                {
                    mqlString.append(" ");
                }

                mqlString.append("over expand bus ");
                mqlString.append(txtActualContainedIn);
                mqlString.append(" rel \"");
                mqlString.append(relationship_list);
                mqlString.append("\" recurse to all");
                methArgs[0] = mqlString.toString();
                methStr.append("groupby value $2 ");
                methArgs[1] = strWhereExp;
                methStr.append("objects select $3");
                methArgs[2] = "id";
                String strMqlResult = MqlUtil.mqlCommand(context,methStr.toString(),false,methArgs);
  
                StringList strResultList = FrameworkUtil.split(strMqlResult,"Objects");
                StringList tokenList = new StringList();
                BufferedReader in = new BufferedReader(new StringReader((String)strResultList.get(strResultList.size()-1)));
                String line;
                String recToken;
                Map objMap = null;
                Vector idVector = new Vector();
                while ((line = in.readLine()) != null)
                {
                    tokenList = FrameworkUtil.split(line,"=");
                    if(tokenList.size()==2)
                    {
                        String booltoken = (String)tokenList.get(0);
                        if(booltoken==null || booltoken.trim().length()<0)
                        {
                            booltoken="";
                        }
                        recToken = (String)tokenList.get(1);
                        recToken = recToken.substring(0,recToken.length()-1);
                        objMap = new HashMap();
                        if(!"false".equalsIgnoreCase(booltoken.trim()) && recToken.length()>0 && idVector.indexOf(recToken) == -1)
                        {
                            idVector.addElement(recToken);
                            objMap = (new DomainObject(recToken)).getInfo(context,resultSelects);
                            mapList.add(objMap);
                        }
                    }
                 
                }

             }
            if(!"*".equals(txtDiscussionKeywords) && !"".equals(txtDiscussionKeywords.trim()))
            {
                MapList searchList=MessageUtil.searchDiscussionsOnKeywords(context,mapList,txtDiscussionKeywords);
                return searchList;
            }
            if("true".equalsIgnoreCase(findMxLink)) {
                 Iterator mapItr = mapList.iterator();

                  while(mapItr.hasNext())
                 {
                    map = (Map)mapItr.next();
                    String type = (String)map.get(DomainConstants.SELECT_TYPE);
                    String name = (String)map.get(DomainConstants.SELECT_NAME);
                    String revision = (String)map.get(DomainConstants.SELECT_REVISION);
                    String vault = (String)map.get(DomainConstants.SELECT_VAULT);
                    StringBuffer resultString = new StringBuffer(100);
                    resultString.append("mxLink:");
                    resultString.append(type);
                    resultString.append("|");
                    resultString.append(name);
                    resultString.append("|");
                    revision = ("".equals(revision))? " " : revision;
                    resultString.append(revision);
                    resultString.append("|");
                    resultString.append(vault);
                    if(latest) {
                        resultString.append("|");
                        resultString.append("latest");
                    }else {
                        if(vault.indexOf(" ") != -1) {
                            resultString.insert(0, "\"");
                            resultString.append("\"");
                        }
                    }
                    map.put("mxLink",resultString.toString());
                 }
            }
            return mapList;
    }
}
