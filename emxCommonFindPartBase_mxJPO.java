/*
 *  emxCommonFindPartBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.common.Person;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.BusinessType;
import matrix.db.PolicyItr;
import matrix.db.Policy;
import matrix.db.StateRequirementList;
import matrix.db.StateRequirementItr;


import matrix.util.StringList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Locale;
import java.util.HashSet ;
import java.util.Hashtable;
import java.util.Vector;
import com.matrixone.apps.framework.ui.UISearch;


/**
 * The <code>emxCommonPartBase</code> class contains implementation code for emxCommonPart.
 *
 * @version Common 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxCommonFindPartBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     * @grade 0
     */
    public emxCommonFindPartBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
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
       //MxDebug.exit ();
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
        String revision        = (String) aMap.get("attribute_Revision");
        String owner           = (String) aMap.get("attribute_Owner");
        String approver        = (String) aMap.get("attribute_Approver");
        String title           = (String) aMap.get("attribute_title");
        String vault           = (String) aMap.get("attribute_Vault");
        String description     = (String) aMap.get("attribute_Description");
        String current         = (String) aMap.get("attribute_State");
        String searchIn        = (String) aMap.get("attribute_SearchWithin");
        String andOrField      = (String) aMap.get("andOrField");
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
        String txtVaultOption = (String)aMap.get("vaultSelction");
        MapList searchMapList = null;

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
            current = "const\"" + current + "\" ";
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

        if (DialogAction.equalsIgnoreCase("AddChildren"))
        {
           if(where.length()>0)
           {
              addContionalOperator(where,"&&");
           }
           where.append("(to[");
           //where.append(getSchemaProperty(context,"attribute_IsVersionObject"));
           where.append(PropertyUtil.getSchemaProperty(context,"relationship_Subclass"));
           where.append("] == 'False')");
        }

        StringBuffer whereAdvSearch = new StringBuffer("");

        String sreqLocaleVar = (String)aMap.get("reqLocaleVar");
        String sandorField    = (String)aMap.get("andOrField");
        if(sreqLocaleVar == null)
        {
            sreqLocaleVar = "";
        }
        aMap.put("reqLocaleVar",sreqLocaleVar);
        //String sAdvSearch = (String)aMap.get("AdvSearch");
        if(sandorField != null)
        {
            String advWhereExp = UISearch.getAdvanceSearchWhereExpression(context, (HashMap)aMap);
            if(!"".equals(advWhereExp))
            {
                if(!"".equals(where.toString())) {
                where.append(" && ");
            }
            where.append("(" + advWhereExp + ")");
            }
        }

        // Settings Query Limit

        int intLimit=0;
        if (queryLimit!= null && !"null".equals(queryLimit) && !"".equals(queryLimit))
        {
            Integer integerLimit = new Integer(queryLimit);
            intLimit = integerLimit.intValue();
        }

        StringList objectSelects = getSearchObjectsSelects(context,aMap);
        if(revision!=null && !"null".equalsIgnoreCase(revision) && revision.equals(""))
        {
          revision = null;
        }
        MapList mpList
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
           //MxDebug.exception (e, true);
        }

        //MxDebug.exit ();
        return searchMapList;
    }


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

    protected static void setNonAttributeWhereClause(
                            StringBuffer where,String key, String value)
                        throws Exception
    {
       //MxDebug.utilEnter ();

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

       //MxDebug.utilExit ();
    }


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
                 String fullname = lastName + "," + firstName;
                 searchMap.put (DomainConstants.SELECT_OWNER,
                                (String) fullname);
              }
           }

           searchListProcessed.add (searchMap);
        }

        //Return the processed mapList

        //MxDebug.utilExit ();
        return searchListProcessed;

     }

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


    protected static void setAttributeWhereClause(Context context,
        StringBuffer where,String symbolicKeyName, String keyValue)
                        throws Exception
    {
      // MxDebug.utilEnter ();

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
          where.append("] == const '");
          where.append(keyValue);
          where.append("\")");
       }

      // MxDebug.utilExit ();
    }


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


     public void addParts(Context context, String[] args)
       throws Exception
     {

        if ((args == null) || (args.length < 1))
        {
          throw (new IllegalArgumentException());
        }

        //Unpaking the Arguments
        Map map = (Map) JPO.unpackArgs(args);

        //Getting the List from Unpaked Map

        String[] childIds   = (String[]) map.get ("childIds");
        String relationship = (String)   map.get ("relationship");
        String parentId     = (String)   map.get ("objectId");
        DomainObject doObj = new DomainObject(parentId);
        DomainRelationship rel = new DomainRelationship();
        rel.connect(context, doObj, relationship, true, childIds);
    }

    public Hashtable getAllPolicyStatesForType(Context context,String[]  packedArgs ) throws Exception{

        HashMap requestMap  = new HashMap();
        String sType = "";
        Set policySet = null ;
        Hashtable hashPolicies = new Hashtable();
        try
        {
            requestMap = (HashMap)JPO.unpackArgs(packedArgs);
            sType = (String)requestMap.get("type");

            policySet = new HashSet();
            BusinessType tmpBusType =  new BusinessType (sType, context.getVault ());

            PolicyItr policyItr = new PolicyItr(tmpBusType.getPolicies (context));

            while ( policyItr.next () ) {
                policySet.add (policyItr.obj().getName());
            }

            Iterator itr = policySet.iterator();
            Set stateSet = new HashSet();

            while(itr.hasNext())
            {
                String policyName =(String)itr.next();
                Policy policy = new Policy(policyName);
                Vector vecAddesStates = new Vector();
                policy.open(context);
                StateRequirementList stateRequirementList = new StateRequirementList();
                stateRequirementList =(StateRequirementList)policy.getStateRequirements(context);
                int iCount = stateRequirementList.size();
                StateRequirementItr stateRequirementItr = new StateRequirementItr(stateRequirementList);
                while (stateRequirementItr.next ())
                {
                        String state = stateRequirementItr.obj().getName();
                        if(!vecAddesStates.contains(state)){
                            vecAddesStates.addElement(state);
                        }
                }
                hashPolicies.put(policyName,vecAddesStates);
            }
        }
        catch (Exception eUnpack)
        {
        }
        return hashPolicies;
   }


}
