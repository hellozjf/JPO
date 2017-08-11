/* emxDocumentCentralFindObjectsBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.22 Wed Oct 22 16:02:43 2008 przemek Experimental przemek $
*/

import matrix.db.Context;
import java.util.Locale;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
 /**
  *  The <code>${CLASSNAME}</code> class is used for Searching DC objects
  *  Copyright (c) 2002, MatrixOne, Inc.
  *
  * @exclude
  */

 public class emxDocumentCentralFindObjectsBase_mxJPO
 {

    public emxDocumentCentralFindObjectsBase_mxJPO(Context context, String[] args)
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
    *//*

    public static MapList findObjects (Context context, String[] args)
       throws Exception
    {
       MxDebug.enter ();
       if ((args == null) || (args.length < 1))
       {
          throw (new IllegalArgumentException());
       }

       Map map = (Map) JPO.unpackArgs(args);

       MapList mpList =  new MapList();
       if(map.get("paramMap") != null)
       {
          mpList =  findObjects(context,(Map)map.get("paramMap"));
       }
       else
       {
          // true when this method is invoked for DCGeneralSearchResults table body implementation
          mpList = findObjects(context, map);
       }
       MxDebug.exit ();
       return mpList;
    }


    *//**
    *
    *
    * @param context the eMatrix <code>Context</code> object
    * @param map
    * @throws Exception if the operation fails
    * @since AEF 9.5.1.0
    *//*

    protected static MapList findObjects(Context context, Map aMap)
       throws Exception
    {
       MxDebug.enter ();


        //-Getting the STrings for corresponding Schema Names

        String strBookShelf = PropertyUtil.getSchemaProperty(context,"type_Bookshelf");
        String strBook      = PropertyUtil.getSchemaProperty(context,"type_Book");
        String strLibrary   = PropertyUtil.getSchemaProperty(context,"type_Library");


        String strDocument      = PropertyUtil.getSchemaProperty(context,
                                                 "type_GenericDocument");
        String strHasBookShelves= PropertyUtil.getSchemaProperty(context,
                                          "relationship_HasBookshelves");

        String strHasBooks      = PropertyUtil.getSchemaProperty(context,
                                                "relationship_HasBooks");

        String strHasDocument = PropertyUtil.getSchemaProperty(context,
                                            "relationship_HasDocuments");

        String workSpaceVaultRel = PropertyUtil.getSchemaProperty(context,
                                    "relationship_VaultedDocumentsRev2");
        
        String languageStr = (String)aMap.get("languageStr");
        
        int intLimit	   = 0;
        //--Getting Param values  from Map

        String name            = (String) aMap.get("attribute_Name");
        String type            = (String) aMap.get("attribute_Type");
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
        
        StringBuffer inputMsgBuff = new StringBuffer ();
        inputMsgBuff.append ("Input parameters to find operation:\n");
        inputMsgBuff.append ("\tName:          \"" + name + "\"\n");
        inputMsgBuff.append ("\tType:          \"" + type + "\"\n");
        inputMsgBuff.append ("\tRevision:      \"" + revision + "\"\n");
        inputMsgBuff.append ("\tOwner:         \"" + owner + "\"\n");
        inputMsgBuff.append ("\tApprover:      \"" + approver + "\"\n");
        inputMsgBuff.append ("\tTitle:         \"" + title + "\"\n");
        inputMsgBuff.append ("\tVault:         \"" + vault + "\"\n");
        inputMsgBuff.append ("\tDescription:   \"" + description + "\"\n");
        inputMsgBuff.append ("\tState:         \"" + current + "\"\n");
        inputMsgBuff.append ("\tSearch In:     \"" + searchIn + "\"\n");
        inputMsgBuff.append ("\tAnd/Or:        \"" + andOrParam + "\"\n");
        inputMsgBuff.append ("\tFolderContentAdd:  \""
                             + folderContentAdd + "\"\n");
        inputMsgBuff.append ("\tBase Type:     \"" + baseType + "\"\n");
        inputMsgBuff.append ("\tDialogAction:  \"" + DialogAction + "\"\n");
        inputMsgBuff.append ("\tobjectId:      \"" + objectId + "\"\n");
        inputMsgBuff.append ("\tqueryLimit:    \"" + queryLimit + "\"\n");
        inputMsgBuff.append ("\tchkRevision:   \"" + chkRevision + "\"\n");
        inputMsgBuff.append ("\tkeyword:       \"" + keyword + "\"\n");
        inputMsgBuff.append ("\tOriginator:    \"" + sOriginator + "\"\n");

        MxDebug.message (MxDebug.DL_6,
                         inputMsgBuff.toString ());

        MapList searchMapList = null;
        MapList mpList		  = null;

        try
        {

        //--Where Clause buffer

        StringBuffer where = new StringBuffer("");

        // Check for Only Latest Revisions Checkbox.. Construct Where Clause

        if (chkRevision != null)
        {
            where.append("(revision == last)");
        }

        //Get Where Clase for non-Attribs

        if(description!=null && ! description.equals("*"))
        {
            addContionalOperator(where,"&&");
            setNonAttributeWhereClause(where,"description",description);
        }

        if(keyword!=null && ! keyword.equals("*"))
        {
            addContionalOperator(where,"&&");
            //setNonAttributeWhereClause(where,"description",description);
            where.append("('search[");
            where.append(keyword);
            where.append("]' == TRUE)");
        }


        if(current!=null && ! current.equals("*"))
        {
            current = "const'" + current + "' ";
            addContionalOperator(where,"&&");
            where.append(" current == " + current);
        }

        //Get Where Clause for Attribute-Titile
        //Symbolic Name of Attribute and its value is
        //Passed


        if(title!=null && ! title.equals("*"))
        {
            addContionalOperator(where,"&&");
            setAttributeWhereClause(context,where,"attribute_Title",title);
        }

        //Get Where Clause for Attribute-Approver
        //Symbolic Name of Attribute and its value is
        //Passed

        if(approver!=null && ! approver.equals("*"))
        {
            addContionalOperator(where,"&&");
            setAttributeWhereClause(context,where,"attribute_Approver",approver);
        }

        //due to Common Doc changes, need to filter out documents with Version policy
        if (baseType.equals(strDocument))
        {
           if(where.length()>0)
           {
              addContionalOperator(where,"&&");
           }
           where.append("(attribute[");
           where.append(PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject"));
           where.append("] != \"True\")");
        }
        // Check for if a Search In . Construct Where Clause

        if ( (searchIn != null && searchIn.length() >0 ) || DialogAction.equalsIgnoreCase("searchIn") )
        {

            if(DialogAction.equalsIgnoreCase("searchIn"))
            {
                searchIn=objectId;
            }


            if(where.length()>0)
            {
                where.append(" && ");
            }

            if(folderContentAdd.equalsIgnoreCase("true"))
            {
                where.append ("(to[");
                where.append (workSpaceVaultRel);
                where.append ("].from.id == \"");
                where.append (searchIn);
                where.append ("\")");
            }
            else if(baseType.equals(strBookShelf))
            {
                where.append ("(to[");
                where.append (strHasBookShelves);
                where.append ("].from.id == \"");
                where.append (searchIn);
                where.append ("\")");

            }
            else if(baseType.equals(strBook))
            {
                where.append ("(to[");
                where.append (strHasBooks);
                where.append ("].from.id == \"");
                where.append (searchIn);
                where.append ("\")");
            }
            else if(baseType.equals(strDocument))
            {
                where.append ("(to[");
                where.append (strHasDocument);
                where.append ("].from.id == \"");
                where.append (searchIn);
                where.append ("\")");
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
           if (sOriginator !=null &&! sOriginator.equals("*") && !"".equals(sOriginator.trim()))
           {
              addContionalOperator(whereAdvSearch, operator);
              setAttributeWhereClause(context,whereAdvSearch,"attribute_Originator", sOriginator);
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
        //Logic when attribute_Type=Null or *
        ///Then Even if * is there then it doect mean any type in DB
        // It means sub and Related Types of BaseType to getting relevant
        //relationship to find Sub and related types and preparing type clause
        //to be passed to find objects

        TreeMap typeList= new TreeMap();

        if(type.equals("*"))
        {
           try
           {

              String typeToFind=null;
              String direction=null;

              if(DialogAction.equalsIgnoreCase("searchResults")||DialogAction.
                 equalsIgnoreCase("AddChildren")||DialogAction.
                 equalsIgnoreCase("searchIn"))

              {
                 typeToFind=baseType;
                 direction="to";
              }

              if(DialogAction.equalsIgnoreCase("searchWithin")||
                 DialogAction.equalsIgnoreCase("chooser"))
              {
                 if (baseType.equals(strBook))
                 {
                    typeToFind=strDocument;
                 }
                 if (baseType.equals(strBookShelf))
                 {
                    typeToFind=strBook;
                 }
                 if(baseType.equals(strLibrary))
                 {
                    typeToFind=strBookShelf;
                 }
                 direction="from";
              }

              if(typeToFind.equals(strLibrary))
              {
                typeList = (TreeMap) Library.getSubAndRelatedTypes(context,
                                                                   direction);
              }
              else if(typeToFind.equals(strBook))
              {
                 typeList = (TreeMap) Book.getSubAndRelatedTypes(context,
                                                                 direction);
              }
              else if(typeToFind.equals(strBookShelf))
              {
                 typeList = (TreeMap)Bookshelf.getSubAndRelatedTypes(context,
                                                                     direction);
              }
              else
              {
                 typeList =
                    (TreeMap)GenericDocument.getSubAndRelatedTypes(context,
                                                                   direction);
              }
           }
           catch (Exception e) {
              MxDebug.exception (e, true);
           }
        }
        else if (includeSubTypes.equalsIgnoreCase("True"))
        {
            Map aValueMap = new HashMap();

            aValueMap.put("className",type);

            String[] valueArgs = JPO.packArgs(aValueMap);

            typeList =
                (TreeMap)
                ${CLASS:emxDocumentCentralCommonBase}.subAndRelatedTypes(
                                                       context, valueArgs );

        }

        if (type.equals("*") || includeSubTypes.equalsIgnoreCase("True"))
        {
           if (type.equals ("*")) {
              type = null;
           }

            if(typeList.size()>0)
            {
                java.util.Set set = typeList.keySet();
                Iterator iter = set.iterator();

                while(iter.hasNext())
                {
                  if(type==null)
                  {
                    type=(String)iter.next();
                  }
                  else
                  {
                        type+=","+(String)iter.next();
                  }
                }
            }
        }

        StringList objectSelects = getSearchObjectsSelects(context,aMap);

        StringBuffer callMsgBuff = new StringBuffer ();
        callMsgBuff.append ("Input parameters to find operation:\n");
        callMsgBuff.append ("\tType:          \"" + type + "\"\n");
        callMsgBuff.append ("\tName:          \"" + name + "\"\n");
        callMsgBuff.append ("\tRevision:      \"" + revision + "\"\n");
        callMsgBuff.append ("\tOwner:         \"" + owner + "\"\n");
        callMsgBuff.append ("\tVault:         \"" + vault + "\"\n");
        callMsgBuff.append ("\tWhere:         \n");
        callMsgBuff.append ("\"" + where.toString() + "\"\n");
        callMsgBuff.append ("\tLimit:    \"" + intLimit + "\"\n");

        MxDebug.message (MxDebug.DL_5,
                         callMsgBuff.toString ());

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
                                       false,
                                       objectSelects,
                                       (short)intLimit,
                                       "*",
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
           MxDebug.exception (e, true);
        }

        MxDebug.exit ();
        if(intLimit!=0 && mpList.size() == intLimit){
            StringBuffer sbObjLimitWarning = new StringBuffer();
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageStr),"emxDocumentCentral.Message.ObjectFindLimit"));
            sbObjLimitWarning.append(" ("+queryLimit+") ");
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageStr),"emxDocumentCentral.Message.ObjectFindLimitReached"));
            ${CLASS:emxContextUtil}.mqlWarning(context,sbObjLimitWarning.toString());
        }
        
        return searchMapList;
    }


    *//**
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
     *//*

    protected static void setNonAttributeWhereClause(
                            StringBuffer where,String key, String value)
                        throws Exception
    {
       MxDebug.utilEnter ();

       if(value.indexOf("*")!= -1 || value.indexOf("?")!= -1)
       {
          where.append("("+ key +" ~~ const \"");
       }
       else
       {
          where.append("("+key+" == const \"");
       }
       where.append(value);
       where.append("\")");

       MxDebug.utilExit ();
    }

    *//**
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
         *//*

     protected static void setAttributeWhereClause(Context context,
        StringBuffer where,String symbolicKeyName, String keyValue)
                        throws Exception
    {
       MxDebug.utilEnter ();

       if(keyValue.indexOf("*")!= -1 || keyValue.indexOf("?")!= -1)
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

       MxDebug.utilExit ();
    }


    *//**
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
     *//*

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


    *//**
     *
     *
     * @param context the eMatrix <code>Context</code> void
     * @param map
     * @param context
     * @param StringBuffer where
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     *//*
    protected static void
        setDateWhereClause(StringBuffer where,String attributeKey,
             String attributeFromDate,String attributeToDate,
             String fieldNameforClause) throws Exception
    {
       MxDebug.utilEnter ();

       if(attributeKey.equals("isBefore"))
       {
          if(attributeFromDate!=null)
          {
             where.append("("+fieldNameforClause+"< \"");
             where.append(attributeFromDate+" 12:00:00 AM");
             where.append("\")");
          }
       }
       else if(attributeKey.equals("isAfter"))
       {
          if(attributeFromDate!=null)
          {
             where.append("("+fieldNameforClause+" > \"");
             where.append(attributeFromDate+" 11:59:59 PM");
             where.append("\")");
          }
       }
       else if(attributeKey.equals("Matches"))
       {
          if(attributeFromDate!=null)
          {
             where.append("(("+fieldNameforClause+" > \"");
             where.append(attributeFromDate+" 12:00:00 AM");
             where.append("\")");
             where.append("&&");
             where.append("("+fieldNameforClause+" < \"");
             where.append(attributeFromDate+" 11:59:59 PM");
             where.append("\"))");
          }
       }
       else if(attributeKey.equals("isBetween"))
       {
          if(attributeToDate!=null&& attributeFromDate!=null)
          {

             where.append("(("+fieldNameforClause+"> \"");
             where.append(attributeFromDate+" 12:00:00 AM");
             where.append("\")");
             where.append("&&");
             where.append("("+fieldNameforClause+"< \"");
             where.append(attributeToDate+" 11:59:59 PM");
             where.append("\"))");
          }
       }

       MxDebug.utilExit ();
    }

    *//**
     *
     *
     * @param context the eMatrix <code>Context</code> void
     * @param map
     * @param context
     * @param StringBuffer where
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     *//*
    protected static void
        getMoreSearchDateWhereClause(Context context, Map aMap,
                StringBuffer whereAdvSearch) throws Exception
    {
       MxDebug.utilEnter ();

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

        MxDebug.utilExit ();
    }


    *//**
    * Get Select StringList of Search
    *
    * @param context the eMatrix <code>Context</code> object
    * @param Map the Java <code>Map</code> object
    *
    * @return the eMatrix <code>StringList</code>
    *
    * @throws Exception if the operation fails
    *
    * @since AEF 9.5.1.0
    *//*

    protected static StringList getSearchObjectsSelects(Context context,
                                                        Map     aMap)
       throws Exception
    {
       MxDebug.utilEnter ();

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

       MxDebug.utilExit ();
       return objSelectList;
    }


    *//**
     * Get Full names of Owner
     *
     * @param context the eMatrix <code>Context</code> object
     * @param MapList the Java <code>MapList</code> object
     *
     * @return the eMatrix <cose>MapList</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.1.0
     *//*

     protected static MapList replaceOwnerUserNameToFullName(Context context,
                                                             MapList mpList)
        throws Exception
     {
        MxDebug.utilEnter ();

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
                 String fullname = lastName + "," + firstName;
                 searchMap.put (DomainConstants.SELECT_OWNER,
                                (String) fullname);
              }
           }

           searchListProcessed.add (searchMap);
        }

        //Return the processed mapList

        MxDebug.utilExit ();
        return searchListProcessed;

     }

     *//**
      * @param context, the eMatrix <code>Context</code>
      * @param timeZone, a double holding time zone offset for the session
      * @param dateParamStr, a String containing the date param value to be modified
      *  e.g. an input Feb 1, 2004 will be translated to eMatrixDate Format, 2/1/2004 12:00:00 PM and stripped of the time portion
      *  the output for above will be 2/1/2004
      * @throws Exception if the operation fails
      * @since DC 10-5
      *//*
     protected static String
         getTimeStrippedEmxDateFormat(Context context,
                                      double timeZone,
                                      String dateParamStr) throws Exception
     {
        String returnStr = null;

        if(dateParamStr != null && !"".equals(dateParamStr) && !"null".equals(dateParamStr))
        {
          Locale locale = ${CLASS:emxMailUtil}.getLocale ( context );
          returnStr = eMatrixDateFormat.getFormattedInputDate(context,dateParamStr, timeZone, locale);
          returnStr = returnStr.substring(0,returnStr.indexOf(" "));
        }
        else
        {
          returnStr = dateParamStr;
        }

        return returnStr;
     }
*/
}
