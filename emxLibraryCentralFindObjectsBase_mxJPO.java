/* emxLibraryCentralFindObjectsBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.25 Wed Oct 22 16:02:35 2008 przemek Experimental przemek $
*/

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;

import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.apps.library.LibraryCentralCommon;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;

import matrix.db.*;

import matrix.util.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Locale;
import java.util.StringTokenizer;


 /**
  *  The <code>${CLASSNAME}</code> class is used for Searching DC objects
  *
  */

 public class emxLibraryCentralFindObjectsBase_mxJPO
 {

    public emxLibraryCentralFindObjectsBase_mxJPO(Context context, String[] args)
        throws Exception
    {
        //EMPTY CONSTRUCTOR
    }

     //~ Methods --------------------------------------------------------------

     /**
      * This method is executed if a specific method is not specified.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args the Java <code>String[]</code> object
      * @return int
      * @throws Exception if the operation fails
      */
     public int mxMain (Context context, String[] args) throws Exception
     {
         if (true)
         {
             throw new Exception (
                     "Must specify method on emxWorkspaceVault invocation");
         }

         return 0;
     }

    /**
    *
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains a Map with the following entries:
    * @returns nothing
    * @throws Exception if the operation fails
    * @since AEF 9.5.1.0
    */
    public static MapList findObjects (Context context, String[] args)
       throws Exception
    {
       //MxDebug.enter ();
       if ((args == null) || (args.length < 1))
       {
          throw (new IllegalArgumentException());
       }

       MapList mpList =  new MapList();
       try{
           // Added for IR-020526V6R2011
           ComponentsUtil.checkLicenseReserved(context, LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);

           Map map = (Map) JPO.unpackArgs(args);
           if(map.get("paramMap") != null)
           {
              mpList =  findObjects(context,(Map)map.get("paramMap"));
           }
           else
           {
              // true when this method is invoked for DCGeneralSearchResults table body implementation
              mpList = findObjects(context, map);
           }
       } catch(Exception ex) {
           throw ex;
       }
       return mpList;
    }


    /**
    *
    *
    * @param context the eMatrix <code>Context</code> object
    * @param map
    * @throws Exception if the operation fails
    * @since AEF 9.5.1.0
    */

    protected static MapList findObjects(Context context, Map aMap)
       throws Exception
    {
       //MxDebug.enter ();

        //--Getting Param values  from Map
        String name            = (String) aMap.get("attribute_Name");
        String type            = (String) aMap.get("attribute_Type");
        String strMode            = (String) aMap.get("Mode");
        String strClassificationId = (String) aMap.get("ClassificationId");
        String revision        = (String) aMap.get("attribute_Revision");
        String owner           = (String) aMap.get("attribute_Owner");
        String approver        = (String) aMap.get("attribute_Approver");
        String title           = (String) aMap.get("attribute_title");
        String vault           = (String) aMap.get("attribute_Vault");
        String description     = (String) aMap.get("attribute_Description");
        String current         = (String) aMap.get("attribute_State");
        String searchIn        = (String) aMap.get("attribute_SearchWithin");
        String andOrParam      = (String) aMap.get("andOrParam");
        String folderContentAdd= (String) aMap.get("folderContentAdd");
        String baseType        = (String) aMap.get("baseType");
        String DialogAction    = (String) aMap.get("DialogAction");
        String objectId        = (String) aMap.get("objectId");
        String queryLimit      = (String) aMap.get("QueryLimit");
        String chkRevision     = (String) aMap.get(
                                    "attribute_LatestRevisionChk");
        String includeSubTypes = (String) aMap.get("includeSubTypes");
        String keyword         = (String) aMap.get("attribute_Keyword");
        String sOriginator     = (String) aMap.get("attribute_Originator");

        MapList searchMapList  = null;
        MapList mpList         = null;
        String languageStr     = (String)aMap.get("languageStr");
        String txtVaultOption  = (String)aMap.get("vaultSelction");
        int intLimit           = 0;
        try
        {
            if(txtVaultOption == null) {
                txtVaultOption = "";
            }
            if(!txtVaultOption.equals("SELECTED_VAULT")){

                vault = PersonUtil.getSearchVaults(context, true, txtVaultOption);
            }
            vault = vault.trim();

            if("".equals(vault)) {
                vault = PersonUtil.getDefaultVault(context);
            }


        //--Where Clause buffer

        StringBuffer where = new StringBuffer("");

        // Check for Only Latest Revisions Checkbox.. Construct Where Clause

        if (chkRevision != null)
        {
            where.append("(revision == last)");
        }

        //Get Where Clase for non-Attribs

        if(description!=null && ! description.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            addContionalOperator(where,"&&");
            setNonAttributeWhereClause(where,"description",description);
        }

        if(keyword!=null && ! keyword.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            addContionalOperator(where,"&&");
            where.append("('search[");
            where.append(keyword);
            where.append("]' == TRUE)");
        }


        if(current!=null && ! current.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            StringBuffer buf = new StringBuffer();
            buf.append("const\"");
            buf.append(current);
            buf.append("\" ");
            addContionalOperator(where,"&&");
            where.append(DomainConstants.SELECT_CURRENT +" == " + buf.toString());
        }

        //Get Where Clause for Attribute-Titile
        //Symbolic Name of Attribute and its value is
        //Passed


        if(title!=null && ! title.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            addContionalOperator(where,"&&");
            setAttributeWhereClause(context,where,"attribute_Title",title);
        }

        //Get Where Clause for Attribute-Approver
        //Symbolic Name of Attribute and its value is
        //Passed

        if(approver!=null && ! approver.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            addContionalOperator(where,"&&");
            setAttributeWhereClause(context,where,"attribute_Approver",approver);
        }

        if (DialogAction.equalsIgnoreCase("AddChildren"))
        {
           if(where.length()>0)
           {
              addContionalOperator(where,"&&");
           }
          where.append("(to[");
          where.append( LibraryCentralConstants.RELATIONSHIP_SUBCLASS);
          where.append("] == 'False')");
          where.append(" && ");
          where.append(DomainConstants.SELECT_ID+" != "+objectId);
        }

        //if type is DOCUMENTS , add check for 'Is Version Object']

            BusinessType busType = new BusinessType(type,context.getVault());
            String strParentType = FrameworkUtil.getBaseType(context,type,context.getVault());
            if(com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENTS.equalsIgnoreCase(strParentType)
            || com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENT.equalsIgnoreCase(strParentType)
            || com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENTS.equalsIgnoreCase(type))
            {
               if(where.length()>0)
               {
                  addContionalOperator(where,"&&");
               }
               where.append("(attribute[");
               where.append("Is Version Object");
               where.append("] == 'False')");
            }



        if (strMode != null && (strMode.equalsIgnoreCase("Move")|| strMode.equalsIgnoreCase("Reclassify")))
        {
           if(where.length()>0)
           {
              addContionalOperator(where,"&&");
           }
           if(strClassificationId != null)
           {
                //where.append(DomainConstants.SELECT_ID+" != "+strClassificationId);
            StringBuffer strClassifiedItems = new StringBuffer();
            String strTemp                  = "";
            strClassifiedItems.append(strClassificationId);

            String command              = "expand bus $1 from relationship $2 recurse to all select bus $3 dump $4";
            String strResult            = MqlUtil.mqlCommand(context, command, strClassificationId, LibraryCentralConstants.RELATIONSHIP_SUBCLASS, "id", ",");
            StringTokenizer stResult    = new StringTokenizer(strResult,"\n");
            while(stResult.hasMoreTokens())
            {
                strTemp             = (String)stResult.nextToken();
                strClassificationId = strTemp.substring(strTemp.lastIndexOf(",")+1);
                strClassifiedItems.append(",");
                strClassifiedItems.append(strClassificationId);
            }

               where.append(" (!( "+DomainConstants.SELECT_ID+" matchlist '" + strClassifiedItems.toString() + "' ',' ))");
           }

            if(strClassificationId != null)
            {
               where.append(" && ");
               where.append(DomainConstants.SELECT_ID+" != "+objectId);
            }
        }

        StringBuffer whereAdvSearch = new StringBuffer("");

        if(andOrParam != null)
        {
           String operator ="";
           if(andOrParam.equals("or"))
           {
               operator ="||";
           }
           else
           {
               operator="&&";
           }
           // where clause based on Originator value while doing Advanced Search
           if (sOriginator !=null &&! sOriginator.equals(LibraryCentralConstants.QUERY_WILDCARD) && !"".equals(sOriginator.trim()))
           {
              addContionalOperator(whereAdvSearch, operator);
              setAttributeWhereClause(context,whereAdvSearch,"attribute_Originator", sOriginator);
           }
           // where clause based on additional non-date attributes value while doing Advanced Search
           StringList attrList = new StringList();
           String sAttrib = "";
           String sValue  = "";
           String strattrList = (String)aMap.get("attrList");
           if(strattrList != null && !"".equals(strattrList)) {
                 if(strattrList.endsWith(",")) {
                         strattrList = strattrList.substring(0,strattrList.length()-1);
                 }
                 attrList = FrameworkUtil.split(strattrList, ",");
           }
           for (int i = 0; i < attrList.size(); i++)
           {
               sAttrib = (String)attrList.elementAt(i);
               sValue =  (String)aMap.get(sAttrib);
               if (sValue !=null &&! sValue.equals(LibraryCentralConstants.QUERY_WILDCARD) && !"".equals(sValue.trim()))
               {
                   addContionalOperator(whereAdvSearch, operator);
                   setAttributeWhereClause(context,whereAdvSearch,sAttrib, sValue);
               }
           }
           // where clause based on additional date attributes value while doing Advanced Search
           String sBookShelf = PropertyUtil.getSchemaProperty(context, "type_Bookshelf" );
           String sBook = PropertyUtil.getSchemaProperty(context, "type_Book" );
           if(!(type.equals(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY) || type.equals(sBookShelf) || type.equals(sBook)))
           {
               StringList strDateAttrList = new StringList();
               String sName = "";
               String strDateAttr = (String)aMap.get("attrDateList");
               String timeZoneString  = (String) aMap.get("timeZone");
               double timeZone         = (new Double(timeZoneString)).doubleValue();
               if(strDateAttr != null && !"".equals(strDateAttr)) {
                     if(strDateAttr.endsWith(",")) {
                             strDateAttr = strDateAttr.substring(0,strDateAttr.length()-1);
                     }
                     strDateAttrList = FrameworkUtil.split(strDateAttr, ",");
               }
               for (int i = 0; i < strDateAttrList.size(); i++)
               {
                    sName = (String)strDateAttrList.elementAt(i);
                    String pName = PropertyUtil.getSchemaProperty(context,sName);
                    if(sName != null && !"".equals(sName) && !"null".equals(sName))
                    {
                        String attributeFrom= (String)aMap.get(sName+"_from");
                        String attributeTo= (String)aMap.get(sName+"_to");
                        String attributeOperator= (String)aMap.get(sName+"_operator");

                        if(attributeFrom!=null &&!"null".equalsIgnoreCase(attributeFrom) && attributeFrom.length()>0 )
                        {
                            addContionalOperator(whereAdvSearch, operator);

                            attributeFrom = getTimeStrippedEmxDateFormat(context, timeZone, attributeFrom);
                            attributeTo = getTimeStrippedEmxDateFormat(context, timeZone, attributeTo);

                            setDateWhereClause(whereAdvSearch,
                                                attributeOperator,
                                                attributeFrom,
                                                attributeTo,
                                                "attribute[" + pName + "]");
                        }
                    }
                }
           }
           //MoreSearchWhere Clause
           getMoreSearchDateWhereClause(context,aMap,whereAdvSearch);
        }

        if(whereAdvSearch.length() > 0)
        {
            if(where.length()>0)
            {
                where.append(" && ("+whereAdvSearch.toString()+")");
            }
            else
            {
                where.append(whereAdvSearch.toString());
            }
        }

        // Settings Query Limit
        queryLimit = UIUtil.isNullOrEmpty(queryLimit)?"0":queryLimit;
        Integer integerLimit = new Integer(queryLimit);
        intLimit = integerLimit.intValue();
        StringList objectSelects = getSearchObjectsSelects(context,aMap);
        if(revision!=null && !"null".equalsIgnoreCase(revision) && revision.equals(""))
        {
          revision = null;
        }

        mpList
           = DomainObject.findObjects (context,
                                       type,
                                       name,
                                       revision,
                                       owner,
                                       vault,
                                       where.toString(),
                                       null,
                                       true,
                                       objectSelects,
                                       (short)intLimit,
                                       LibraryCentralConstants.QUERY_WILDCARD,
                                       null);

        //--Post process search results ie. get Full names for owner field
        //in maplist.
        searchMapList = new MapList();

        if(mpList.size()>0)
        {
            searchMapList=replaceOwnerUserNameToFullName(context,mpList);

        }
        }
        catch (Exception e) {
           //MxDebug.exception (e, true);
        }

        //MxDebug.exit ();
        if(intLimit!=0 && mpList.size() == intLimit){
            StringBuffer sbObjLimitWarning = new StringBuffer();
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageStr),"emxDocumentCentral.Message.ObjectFindLimit"));
            sbObjLimitWarning.append(" ("+queryLimit+") ");
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageStr),"emxDocumentCentral.Message.ObjectFindLimitReached"));
            emxContextUtil_mxJPO.mqlWarning(context,sbObjLimitWarning.toString());
        }

        return searchMapList;
    }


    /**
     * set the Where Clause for NonAttributes
     *
     * @param StringBuffer the Java <code>StringBuffer</code> object
     * @param String  the Java <code>String</code> object
     * @param String  the Java <code>String</code> object
     * @return nothing
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.1.0
     */

    protected static void setNonAttributeWhereClause(
                            StringBuffer where,String key, String value)
                        throws Exception
    {
       //MxDebug.utilEnter ();

       if(value.indexOf(LibraryCentralConstants.QUERY_WILDCARD)!= -1 || value.indexOf("?")!= -1)
       {
          where.append("("+ key +" ~~ const \"");
       }
       else
       {
          where.append("("+key+" == const \"");
       }
       where.append(value);
       where.append("\")");

       //MxDebug.utilExit ();
    }

    /**
         * set the Where Clause for Attributes
         *
         * @param StringBuffer the Java <code>StringBuffer</code> object
         * @param String  the Java <code>String</code> object
         *  @param String  the Java <code>String</code> object
         * @return nothing
         *
         * @throws Exception if the operation fails
         *
         * @since AEF 9.5.1.0
         */

     protected static void setAttributeWhereClause(Context context,
        StringBuffer where,String symbolicKeyName, String keyValue)
                        throws Exception
    {
      // MxDebug.utilEnter ();

       if(keyValue.indexOf(LibraryCentralConstants.QUERY_WILDCARD)!= -1 || keyValue.indexOf("?")!= -1)
       {
          where.append("(attribute[");
          where.append(PropertyUtil.getSchemaProperty(context,symbolicKeyName));
          where.append("] ~= const \"");
          where.append(keyValue);
          where.append("\")");

       }
       else
       {
          where.append("(attribute[");
          where.append(PropertyUtil.getSchemaProperty(context,symbolicKeyName));
          where.append("] == const \"");
          where.append(keyValue);
          where.append("\")");
       }

      // MxDebug.utilExit ();
    }


    /**
     * Add contional Operator to where
     *
     * @param StringBuffer the Java <code>StringBuffer</code> object
     * @param String  the Java <code>String</code> object
     *
     * @return nothing
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.1.0
     */

    protected static void addContionalOperator(
        StringBuffer where,String operator)
                        throws Exception
    {
       //Adds the Operator only if Where Clause is non -empty

        if (where.length() > 0)
        {
          where.append(operator);
        }

    }

    /**
     * Sets the date where Clause
     *
     * @param context the eMatrix <code>Context</code> void
     * @param map
     * @param context
     * @param StringBuffer where
     * @throws Exception if the operation fails
     * @exclude
     */
    protected static void
        setDateWhereClause(StringBuffer where,String attributeKey,
             String attributeFromDate,String attributeToDate,
             String fieldNameforClause) throws Exception
    {
       //MxDebug.utilEnter ();

       if(attributeKey.equals("isBefore"))
       {
          if(attributeFromDate!=null)
          {
             where.append("("+fieldNameforClause+"< '");
             where.append(attributeFromDate+" 12:00:00 AM");
             where.append("')");
          }
       }
       else if(attributeKey.equals("isAfter"))
       {
          if(attributeFromDate!=null)
          {
             where.append("("+fieldNameforClause+" > '");
             where.append(attributeFromDate+" 11:59:59 PM");
             where.append("')");
          }
       }
       else if(attributeKey.equals("Matches"))
       {
          if(attributeFromDate!=null)
          {
             where.append("(("+fieldNameforClause+" > '");
             where.append(attributeFromDate+" 12:00:00 AM");
             where.append("')");
             where.append("&&");
             where.append("("+fieldNameforClause+" < '");
             where.append(attributeFromDate+" 11:59:59 PM");
             where.append("'))");
          }
       }
       else if(attributeKey.equals("isBetween"))
       {
          if(attributeToDate!=null&& attributeFromDate!=null)
          {

             where.append("(("+fieldNameforClause+"> '");
             where.append(attributeFromDate+" 12:00:00 AM");
             where.append("')");
             where.append("&&");
             where.append("("+fieldNameforClause+"< '");
             where.append(attributeToDate+" 11:59:59 PM");
             where.append("'))");
          }
       }
    }

    /**
     * Gets Search Date where clause
     *
     * @param context the eMatrix <code>Context</code> void
     * @param map
     * @param context
     * @param StringBuffer where
     * @throws Exception if the operation fails
     * @exclude
     */
    protected static void
        getMoreSearchDateWhereClause(Context context, Map aMap,
                StringBuffer whereAdvSearch) throws Exception
    {

        //---Getting the MorePart search Parametrs from map

        String andOrParam      = (String) aMap.get("andOrParam");
        String modifiedDate     = (String) aMap.get("attribute_ModifiedDate");
        String originatedDate   = (String) aMap.get("attribute_OriginatedDate");
        String timeZoneString  = (String) aMap.get("timeZone");

        double timeZone         = (new Double(timeZoneString)).doubleValue();

        String modifiedCalendar1  =
                        (String) aMap.get("attribute_Modifiedcalendar1");

        String modifiedCalendar2  =
                        (String) aMap.get("attribute_Modifiedcalendar2");

        String originatedCalendar1=
                        (String)aMap.get("attribute_Orginatedcalendar1");

        String originatedCalendar2=
                        (String)aMap.get("attribute_Orginatedcalendar2");

        String type  = (String) aMap.get("attribute_Type");
        String sBookShelf = PropertyUtil.getSchemaProperty(context, "type_Bookshelf" );
        String sBook = PropertyUtil.getSchemaProperty(context, "type_Book" );

        //--The operator depending upon and or

        String operator ="";

        if(andOrParam.equals("or"))
        {
            operator ="||";
        }
        else
        {
            operator="&&";
        }

        if(modifiedDate!=null && modifiedCalendar1 !=null && !"null".equalsIgnoreCase(modifiedDate) && !"null".equalsIgnoreCase(modifiedCalendar1) && !"".equals(modifiedDate) && !"".equals(modifiedCalendar1))
        {
            addContionalOperator(whereAdvSearch, operator);

            modifiedCalendar1 = getTimeStrippedEmxDateFormat(context, timeZone, modifiedCalendar1);
            modifiedCalendar2 = getTimeStrippedEmxDateFormat(context, timeZone, modifiedCalendar2);

            //Where clause for Modified Date
            setDateWhereClause(whereAdvSearch,
                               modifiedDate,
                               modifiedCalendar1,
                               modifiedCalendar2,
                               "Modified");
        }
        if(originatedDate!=null && originatedCalendar1!=null && !"null".equalsIgnoreCase(originatedDate) && !"null".equalsIgnoreCase(originatedCalendar1) && !"".equals(originatedDate) && !"".equals(originatedCalendar1))
        {
            //Where clause for Originated Date
            addContionalOperator(whereAdvSearch, operator);

            originatedCalendar1 = getTimeStrippedEmxDateFormat(context, timeZone, originatedCalendar1);
            originatedCalendar2 = getTimeStrippedEmxDateFormat(context, timeZone, originatedCalendar2);

            setDateWhereClause(whereAdvSearch,
                               originatedDate,
                               originatedCalendar1,
                               originatedCalendar2,
                               "Originated");
        }

        //MxDebug.utilExit ();
    }


    /**
    * Get Select StringList of Search
    *
    * @param context the eMatrix <code>Context</code> object
    * @param Map the Java <code>Map</code> object
    * @return the eMatrix <code>StringList</code>
    * @throws Exception if the operation fails
    * @exclude
    */

    protected static StringList getSearchObjectsSelects(Context context,
                                                        Map     aMap)
       throws Exception
    {
       //MxDebug.utilEnter ();

       String folderContentAdd = (String) aMap.get("folderContentAdd");
       String DialogAction     = (String) aMap.get("DialogAction");

       StringList objSelectList = new StringList(13);

       objSelectList.add(DomainConstants.SELECT_ID);
       objSelectList.add(DomainConstants.SELECT_NAME);
       objSelectList.add(DomainConstants.SELECT_REVISION);
       objSelectList.add(DomainConstants.SELECT_TYPE );

       objSelectList.add("attribute["
                         + PropertyUtil.getSchemaProperty(context,"attribute_Title")
                         + "]");

       objSelectList.add(DomainConstants.SELECT_DESCRIPTION);
       objSelectList.add(DomainConstants.SELECT_OWNER);
       objSelectList.add(DomainConstants.SELECT_CURRENT);
       objSelectList.add(DomainConstants.SELECT_LOCKED );
       objSelectList.add(DomainConstants.SELECT_LOCKER );
       objSelectList.add(DomainConstants.SELECT_POLICY );

       String aceessInform = "";

       //--Getting the Access info based on Dialog Action
       //In case of Add children/Folders we need from connect

       if ((DialogAction.equalsIgnoreCase("Chooser"))
           ||(DialogAction.equalsIgnoreCase("AddChildren")))
       {
          if (DialogAction.equalsIgnoreCase("AddChildren")
              || folderContentAdd.equalsIgnoreCase("True"))
          {
             aceessInform = "toconnect";
          }
          else
          {
             aceessInform = "fromconnect";
          }

          objSelectList.add("current.access[delete]");
          objSelectList.add("current.access["+aceessInform+"]");

       }

       //MxDebug.utilExit ();
       return objSelectList;
    }


    /**
     * Get Full names of Owner
     *
     * @param context the eMatrix <code>Context</code> object
     * @param MapList the Java <code>MapList</code> object
     * @return the eMatrix <cose>MapList</code>
     * @throws Exception if the operation fails
     * @exclude
     */

     protected static MapList replaceOwnerUserNameToFullName(Context context,
                                                             MapList mpList)
        throws Exception
     {
        //MxDebug.utilEnter ();

        //--Getting the owner username in Set

        Iterator iterator = mpList.iterator();
        HashMap aMap;
        java.util.TreeSet userNamesSet = new java.util.TreeSet ();

        while(iterator.hasNext ())
        {
           aMap = (HashMap) iterator.next ();
           userNamesSet.add((String)aMap.get (DomainConstants.SELECT_OWNER));
        }

        //SelectList containing the First,Last Name

        StringList selectList = new StringList (2);

        selectList.addElement (Person.SELECT_FIRST_NAME);
        selectList.addElement (Person.SELECT_LAST_NAME);

        //--Getting the Map it contains username as key and the Map as
        //value containtg selectInformation

        HashMap ownerDetailsMap
           = (HashMap)Person.getPersonsFromNames (context,
                                                  userNamesSet,
                                                  selectList);

        Iterator searchItr = mpList.iterator ();

        String firstName   = null;
        String lastName    = null;

        HashMap searchMap;
        HashMap tempMap;

        MapList searchListProcessed = new MapList ();

        //--replacing the Owner name to full name if Exixts
        //In SearchMapList

        while (searchItr.hasNext ())
        {
           searchMap = (HashMap) searchItr.next ();

           tempMap = (HashMap) ownerDetailsMap.get (
              (String) searchMap.get(DomainConstants.SELECT_OWNER) );

           if (tempMap != null)
           {
              firstName = (String) tempMap.get (Person.SELECT_FIRST_NAME);
              lastName  = (String) tempMap.get (Person.SELECT_LAST_NAME);

              if (firstName != null
                  && firstName.length() > 0
                  && lastName != null
                  && lastName.length() > 0)
              {
                 StringBuffer fullname = new StringBuffer();
                 fullname.append(lastName);
                 fullname.append(",");
                 fullname.append(firstName);
                 searchMap.put (DomainConstants.SELECT_OWNER,
                                 fullname.toString());
              }
           }

           searchListProcessed.add (searchMap);
        }

        //Return the processed mapList

        //MxDebug.utilExit ();
        return searchListProcessed;

     }

     /**
      * Gets time stripped emxdate format
      *
      * @param context, the eMatrix <code>Context</code>
      * @param timeZone, a double holding time zone offset for the session
      * @param dateParamStr, a String containing the date param value to be modified
      *  e.g. an input Feb 1, 2004 will be translated to eMatrixDate Format, 2/1/2004 12:00:00 PM and stripped of the time portion
      *  the output for above will be 2/1/2004
      * @throws Exception if the operation fails
      * @exclude
      */
     protected static String
         getTimeStrippedEmxDateFormat(Context context,
                                      double timeZone,
                                      String dateParamStr) throws Exception
     {
        String returnStr = null;

        if(dateParamStr != null && !"".equals(dateParamStr) && !"null".equals(dateParamStr))
        {
          Locale locale = emxMailUtil_mxJPO.getLocale ( context );
          returnStr = eMatrixDateFormat.getFormattedInputDate(context,dateParamStr, timeZone, locale);
          returnStr = returnStr.substring(0,returnStr.indexOf(" "));
        }
        else
        {
          returnStr = dateParamStr;
        }

        return returnStr;
     }


     /**
    * Gets the search result
    *
    * @param context the eMatrix <code>Context</code> object
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a paramMap with the following entries:
     *    txtName        - a String of specified criteria name
     *    txtRev         - a String of specified criteria revision
     *    txtTypeActual  - a String of specified criteria type
     *    vaultSelction  - a String of specified criteria vault
     *    QueryLimit     - a String of limit on the number of objects found
     *    latestRevision - a String containing the latestRevision info of the object
     * @return MapList containing objects for search result
    * @throws Exception if the operation fails
    * @exclude
    */
     public static MapList doSearch (Context context, String[] args)
        throws Exception
     {
        if ((args == null) || (args.length < 1))
        {
           throw (new IllegalArgumentException());
        }

        MapList mpList =  new MapList();
        try{
            // Added for IR-020526V6R2011
            ComponentsUtil.checkLicenseReserved(context, LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);

            Map map = (Map) JPO.unpackArgs(args);
            if(map.get("paramMap") != null)
            {
               mpList =  findObjects(context,(HashMap)map.get("paramMap"));
            }
            else
            {
               // true when this method is invoked for DCGeneralSearchResults table body implementation
               mpList = doSearch(context, (HashMap)map);
            }
        } catch(Exception ex) {
            throw ex;
        }
        return mpList;
     }

    /**
     * This method get Objects for the specified criteria in General Search.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param paramMap contains the following entries:
     *    txtName        - a String of specified criteria name
     *    txtRev         - a String of specified criteria revision
     *    txtTypeActual  - a String of specified criteria type
     *    vaultSelction  - a String of specified criteria vault
     *    QueryLimit     - a String of limit on the number of objects found
     *    latestRevision - a String containing the latestRevision info of the object
     * @return MapList containing objects for search result
     * @throws Exception if the operation fails
     */

    public static MapList doSearch(Context context , HashMap paramMap)
       throws Exception
    {

        //Retrieve Search criteria
        String selType          = (String)paramMap.get("txtTypeActual");
        String txtName          = (String)paramMap.get("txtName");
        String txtRev           = (String)paramMap.get("txtRev");
        String languageStr = (String)paramMap.get("languageStr");
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

        String queryLimit = (String)paramMap.get("QueryLimit");
        String latestRevision = (String)paramMap.get("latestRevision");


        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
           queryLimit = "0";
        }

        if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
           txtName = "*";
        }

        if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
          txtRev = "*";
        }

        String txtOwner  = (String) paramMap.get("attribute_Owner");
        String txtDescription     = (String) paramMap.get("attribute_Description");
        String txtCurrent         = (String) paramMap.get("attribute_State");


        StringBuffer sWhereExp = new StringBuffer("");
        String txtFormat = "*";
        String txtSearch = "";


        if(latestRevision != null) {
            sWhereExp.append("(revision == last)");
        }

        if(txtDescription!=null && ! txtDescription.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            addContionalOperator(sWhereExp,"&&");
            setNonAttributeWhereClause(sWhereExp,"description",txtDescription);
        }

        if(txtCurrent!=null && ! txtCurrent.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            StringBuffer txtBuffer = new StringBuffer();
            txtBuffer.append("const'");
            txtBuffer.append(txtCurrent);
            txtBuffer.append("' ");
            addContionalOperator(sWhereExp,"&&");
            sWhereExp.append(DomainConstants.SELECT_CURRENT +" == " + txtBuffer.toString());
        }

        if(txtOwner!=null && ! txtOwner.equals(LibraryCentralConstants.QUERY_WILDCARD))
        {
            addContionalOperator(sWhereExp,"&&");
            setNonAttributeWhereClause(sWhereExp,"owner",txtOwner);
        }

        String advWhereExp = UISearch.getAdvanceSearchWhereExpression(context, paramMap);
        if(!"".equals(advWhereExp))
        {
                if(!"".equals(sWhereExp.toString())) {
                        sWhereExp.append(" && ");
                }
                sWhereExp.append("(" + advWhereExp + ")");
        }

        //Modified for 310148
        //BusinessType busType = new BusinessType(selType,context.getVault());
        BusinessType busType = new BusinessType(FrameworkUtil.split(selType,",").get(0).toString(),context.getVault());
        String strParentType = busType.getParent(context);

// Added for IR-013944V6R2011 Dated 28/10/2009 Begins.
        if(com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENTS.equalsIgnoreCase(strParentType) ||
        com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENT.equalsIgnoreCase(strParentType) ||
        com.matrixone.apps.common.CommonDocument.TYPE_DOCUMENTS.equalsIgnoreCase(FrameworkUtil.split(selType,",").get(0).toString()))
// Added for IR-013944V6R2011 Dated 28/10/2009 Ends.

        {
           if(sWhereExp.length()>0)
           {
              addContionalOperator(sWhereExp,"&&");
           }
           sWhereExp.append("(attribute[");
           sWhereExp.append("Is Version Object");
           sWhereExp.append("] == 'False')");
        }


        SelectList resultSelects = new SelectList(7);
        resultSelects.add(DomainConstants.SELECT_ID);
        resultSelects.add(DomainConstants.SELECT_TYPE);
        resultSelects.add(DomainConstants.SELECT_NAME);
        resultSelects.add(DomainConstants.SELECT_REVISION);
        resultSelects.add(DomainConstants.SELECT_DESCRIPTION);
        resultSelects.add(DomainConstants.SELECT_CURRENT);
        resultSelects.add(DomainConstants.SELECT_POLICY);

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

        if(totalresultList.size() == Integer.parseInt(queryLimit)){

            StringBuffer sbObjLimitWarning = new StringBuffer();
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageStr),"emxDocumentCentral.Message.ObjectFindLimit"));
            sbObjLimitWarning.append(" ("+queryLimit+") ");
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageStr),"emxDocumentCentral.Message.ObjectFindLimitReached"));

            emxContextUtil_mxJPO.mqlWarning(context,sbObjLimitWarning.toString());
        }

        return totalresultList;
    }

    /**
     * This method returns the Exclude OID for Add Existing Functions using FullSearch
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        1 - object Ids
     * @return MapList containing objects for search result
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getAddExisitingExcludeOIDs (Context context, String args[])
    throws Exception
    {
        HashMap programMap              = (HashMap) JPO.unpackArgs(args);
        String objectId                 = (String)programMap.get("objectId");
        String useMode                  = (String)programMap.get("useMode");
        useMode                         = UIUtil.isNullOrEmpty(useMode)?"":useMode;
        String whereAccessInfo          = useMode.equalsIgnoreCase("addRetentions")
                                          ? "current.access[fromconnect]~~FALSE"
                                          : "current.access[toconnect]~~FALSE";
        String strfield                 = (String)programMap.get("field");
        String strTypePattern           = getPatternFromFieldParameter(strfield);
        StringBuffer sbWhereExpression  = new StringBuffer(whereAccessInfo);
        StringList objSelects           = new StringList();
        MapList mlExcludeOIDs           = null;
        objSelects.add(DomainConstants.SELECT_ID);
        DomainObject domObj             = new DomainObject(objectId);
        String sType                    = domObj.getType(context);
        String relPattern               = "";
        if("addToFolder".equalsIgnoreCase(useMode)) {
            relPattern                  = LibraryCentralConstants.RELATIONSHIP_VAULTED_DOCUMENTS_REV2;
            sbWhereExpression.append("|| to[");
            sbWhereExpression.append(relPattern);
            sbWhereExpression.append("].from.id ==");
            sbWhereExpression.append(objectId);
        } else if ("addRetentions".equalsIgnoreCase(useMode) || "setRetentionSchedules".equalsIgnoreCase(useMode) ) {
            String stateActive          = LibraryCentralConstants.STATE_RETENTION_ACTIVE;
            relPattern                  = LibraryCentralConstants.RELATIONSHIP_RETAINED_RECORD;
            sbWhereExpression.append(" || current!=");
            sbWhereExpression.append(stateActive);
            sbWhereExpression.append(" || from[");
            sbWhereExpression.append(relPattern);
            sbWhereExpression.append("].to.id ==");
            sbWhereExpression.append(objectId);
        } else if ("addRetainedDocuments".equalsIgnoreCase(useMode) ){
        	
        	DomainObject retentionObj = new DomainObject(objectId);
			MapList RetainedDocumentItemList = (MapList)retentionObj.getRelatedObjects(context,
                                                                    LibraryCentralConstants.RELATIONSHIP_RETAINED_RECORD,
                                                                    "*",
                                                                    new StringList(DomainConstants.SELECT_ID),
                                                                    null,
                                                                    false,
                                                                    true,
                                                                    new Short("1"),
                                                                    "",
                                                                    "",
                                                                    0);
			
			mlExcludeOIDs = 	RetainedDocumentItemList;																	
        	        	
        } else  if ("addClass".equalsIgnoreCase(useMode) ){
            relPattern                  = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
            sbWhereExpression.append("|| to[");
            sbWhereExpression.append(relPattern);
            sbWhereExpression.append("] ~~ TRUE");
            sbWhereExpression.append("|| id ==");
            sbWhereExpression.append(objectId);
            sbWhereExpression.append("|| ( interface matchlist '");
            sbWhereExpression.append(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER);
            sbWhereExpression.append("' ',' )") ;
        } else  if ("addClassificationEndItem".equalsIgnoreCase(useMode) ){
            DomainObject classObj = new DomainObject(objectId);
            MapList classifiedItemList = (MapList)classObj.getRelatedObjects(context,
                                                                    LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,
                                                                    "*",
                                                                    new StringList(DomainConstants.SELECT_ID),
                                                                    null,
                                                                    false,
                                                                    true,
                                                                    new Short("1"),
                                                                    "",
                                                                    "",
                                                                    0);


            mlExcludeOIDs = classifiedItemList;

        } else if(sType.equals(LibraryCentralConstants.TYPE_LIBRARY)){
            relPattern                  = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
            sbWhereExpression.append("|| to[");
            sbWhereExpression.append(relPattern);
            sbWhereExpression.append("].from.id ==");
            sbWhereExpression.append(objectId);
        }

        if (!"addClassificationEndItem".equalsIgnoreCase(useMode) && !"addRetainedDocuments".equalsIgnoreCase(useMode) ){        	
            mlExcludeOIDs               = DomainObject.findObjects(context, strTypePattern, "*", sbWhereExpression.toString(), objSelects);
        }
        StringList slExcludeOIDs        = new StringList();
        addMapListIdsToStringList(slExcludeOIDs,mlExcludeOIDs);

        return slExcludeOIDs;
    }

    /**
     * This method returns the Exclude OID for Move Function using FullSearch
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        1 - object Id
     *        2 - old parent object id (in which the object is present)
     * @return StringList containing Schedule objects for search result
     * @throws Exception if the operation fails
     */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getMoveExcludeOIDs (Context context, String [] args)
    throws Exception
    {
         HashMap programMap                 = (HashMap) JPO.unpackArgs(args);
         String objectId                    = (String)programMap.get("objectId");
         String oldParentObjectId           = (String)programMap.get("oldParentObjectId");
         String field						= (String)programMap.get("field");
         String relPattern                  = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
         StringBuffer sbWhereExpression     = new StringBuffer("current.access[fromconnect]~~FALSE");
         StringList objSelects              = new StringList();
         StringList slExcludeOIDs           = new StringList();
         MapList mlExcludeOIDs              = null;
         DomainObject domObj                = new DomainObject(objectId);
         String objType                       = domObj.getInfo(context,DomainConstants.SELECT_TYPE);
         //Code Added for R216 Release
         //Retrieve the Types, so that objects of those types which don't expose "fromconnect access" to the object being moved,won't populate
         String[] fieldData					= 	field.split("=");
         String types						=	fieldData[1];
         String[] sType						=	types.split(":");
         objSelects.add(DomainConstants.SELECT_ID);
         MapList mlAllSubClass              = (MapList)domObj.getRelatedObjects(context,relPattern,objType,objSelects,new  StringList(),false,true,(short)0,null,null);
         addMapListIdsToStringList(slExcludeOIDs,mlAllSubClass);
         sbWhereExpression.append("||"+ DomainConstants.SELECT_ID+" matchlist \""+objectId+","+oldParentObjectId+"\" \",\"");
         mlExcludeOIDs  = DomainObject.findObjects(context, sType[0], "*", sbWhereExpression.toString(), objSelects);

         addMapListIdsToStringList(slExcludeOIDs,mlExcludeOIDs);
         //Code Added for R216 Release
         //Adding the existing parent ID so that it is avoided in population window.
         slExcludeOIDs.addElement(oldParentObjectId);
         return slExcludeOIDs;
    }

    
    /**
     * This method returns the Include OID for the use case of AddToFolders using FullSearch/Library List Page
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return StringList containing Schedule objects for search result
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getFolderIncludeOIDs (Context context, String[] args) throws Exception
    {
    	try
    	{
    		StringList busSelects = new StringList();
    		StringList returnList=new StringList();
    		
    		busSelects.add(DomainConstants.SELECT_ID);
    		MapList folderList = DomainObject.findObjects(context, PropertyUtil.getSchemaProperty(context,"type_ProjectVault"), "*", "", "*", "*", "", false, busSelects);
            
    		Iterator folderIterator = folderList.iterator();
    		while(folderIterator.hasNext())
            {
            	Map foldersMap=(Map)folderIterator.next();
            	String folderId =(String)foldersMap.get(DomainConstants.SELECT_ID);
            	returnList.add(folderId);
            }
    		
    		return returnList;
    	}
        catch (Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
    }
    
     /**
     * This method returns the Exclude OID for ReClassify Function using FullSearch
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        1 - object Id
     * @return StringList containing Schedule objects for search result
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getReClassifyExcludeOIDs (Context context, String [] args)
    throws Exception
    {
         HashMap programMap                 = (HashMap) JPO.unpackArgs(args);
         String objectId                    = (String)programMap.get("objectId");
         String field                       = (String)programMap.get("field");
         String types = "";
         int beginIndex  = 0;
         int endIndex    = field.length();
         beginIndex      = field.indexOf("TYPES=")+6;
         if(beginIndex>=6){
             int tempEndIndex = field.indexOf(':',beginIndex);
             if(tempEndIndex >0)
                 endIndex = tempEndIndex;

             types = field.substring(beginIndex,endIndex);
         }else{
             DomainObject domObj   = new DomainObject(objectId);
             types                 = domObj.getInfo(context,DomainConstants.SELECT_TYPE);
         }
         StringBuffer sbWhereExpression     = new StringBuffer("current.access[fromconnect]~~FALSE");
         StringList objSelects              = new StringList();
         StringList slExcludeOIDs           = new StringList();
         MapList mlExcludeOIDs              = null;
         objSelects.add(DomainConstants.SELECT_ID);
         sbWhereExpression.append("||"+ DomainConstants.SELECT_ID+" EQ "+objectId);
         mlExcludeOIDs                      = DomainObject.findObjects(context, types, "*", sbWhereExpression.toString(), objSelects);
         addMapListIdsToStringList(slExcludeOIDs,mlExcludeOIDs);
         return slExcludeOIDs;
    }

    /**
     * This method returns the Exclude OID for Add Content to Folders using FullSearch
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        1 - field - search field types
     * @return StringList containing objects for search result
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getAddContentToFolderExcludeOIDs (Context context, String args[])
    throws Exception
    {
         HashMap programMap             = (HashMap) JPO.unpackArgs(args);
         String strfield                = (String)programMap.get("field");
         String strTypePattern          = getPatternFromFieldParameter(strfield);
         StringBuffer sbWhereExpression = new StringBuffer("current.access[toconnect]~~FALSE");
         StringList objSelects          = new StringList(DomainConstants.SELECT_ID);
         MapList mlExcludeOIDs          = DomainObject.findObjects(context, strTypePattern, "*", sbWhereExpression.toString(), objSelects);
         StringList slExcludeOIDs       = new StringList();
         addMapListIdsToStringList(slExcludeOIDs,mlExcludeOIDs);
         return slExcludeOIDs;

    }

     /**
     * This method gets ObjectIds from MapList and adds them in StringList for the Exclude Oids
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        1 -MapList
     * @return StringList containing objects for search result
     * @throws Exception if the operation fails
     */
    public StringList addMapListIdsToStringList (StringList slExcludeOIDs , MapList mlExcludeOIDs) {
        for (int i = 0; i < mlExcludeOIDs.size(); i++) {
            Map map                = (Map) mlExcludeOIDs.get(i);
            String classOID        = (String)map.get(DomainObject.SELECT_ID);
            if(!slExcludeOIDs.contains(classOID)){
                slExcludeOIDs.add(map.get(DomainObject.SELECT_ID));
            }
        }
        return slExcludeOIDs;
    }

    /**
     * This method gets Fields Setting from the requestMap and Returns the comma seperated Types
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *       Fields String
     * @return String containing Types comma seperated
     * @throws Exception if the operation fails
     */
    public String getPatternFromFieldParameter(String strFieldsPattern) {

        Pattern typePattern     = null;
        String sType            = strFieldsPattern.substring(6);
        StringTokenizer stTypeToken = new  StringTokenizer(sType,",");

        while(stTypeToken.hasMoreTokens()){
            String strTempType = (String)stTypeToken.nextToken();
            if(typePattern != null){
                typePattern.addPattern(strTempType);
            } else {
                typePattern = new Pattern(strTempType);
            }
        }
        return typePattern.getPattern();
    }
 }
