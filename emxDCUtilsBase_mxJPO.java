/* emxDCUtilsBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.22 Wed Oct 22 16:02:10 2008 przemek Experimental przemek $
*/

 import matrix.db.Access;
 import matrix.db.Context;
 import matrix.db.JPO;
 import matrix.util.StringList;
 import com.matrixone.apps.domain.util.MapList;
 import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.common.VCDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.XSSUtil;

import com.matrixone.apps.library.LibraryCentralCommon;
import com.matrixone.apps.library.LibraryCentralConstants;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Vector;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  to for DocumentCentral Utilty Programs
 *
 * @exclude
 */

public class emxDCUtilsBase_mxJPO implements LibraryCentralConstants
{

   public static final String SELECT_ATTR_FIRST_NAME
      = "attribute[" + ATTRIBUTE_FIRST_NAME + "]";

   public static final String SELECT_ATTR_LAST_NAME
      = "attribute[" + ATTRIBUTE_LAST_NAME + "]";

    public  emxDCUtilsBase_mxJPO  ()
    {
         //EMPTY CONSTRUCTOR
    }

    /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    *
    *  @since DC 10.5
    */
    public emxDCUtilsBase_mxJPO ( Context context, String[] args )
        throws Exception
    {
     /*
      *  Author    : DL
      *  Date      : 11/7/2002
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }
    }

   /**
    *  This mehtod is executed if a specific method is not specified.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @return int 0
    *  @throws Exception if the operation fails
    *
    *  @since DC 10.5
    */

    public int mxMain(Context context, String []args)
        throws Exception
    {
        return 0;
    }


    /**
    *  Get Maplist containing Revisions Infor for Id passed In
    *  Used for Revision Summary Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds the following input arguments:
    *    0 - HashMap containing one String entry for key "objectId"
    *  @return MapList containing Revisions Info
    *  @throws Exception if the operation fails
    *
    *  @since DC 10.5
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getRevisions(Context context, String[] args)
        throws Exception
    {

       /*
        *  Author    : AnilJ
        *  Date      : 11/7/2002
        *  Notes     :
        *  History   :
        */

        HashMap map = (HashMap) JPO.unpackArgs(args);

        String objectId = (String) map.get("objectId");

        DomainObject busObj = new DomainObject(objectId);

        StringList busSelects = new StringList(1);

        busSelects.add(busObj.SELECT_ID);


        // for the Id passed, get revisions Info
        //
        MapList revisionsList = busObj.getRevisionsInfo(context,busSelects,
                                                          new StringList(0));

        return revisionsList;
    }

    /**
    *  Get Vector of Strings for Security Column in Folder Details List Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args contains a Java <code>HashMap</code>, programMap with the following entries:
    *     objectList    - a MapList with Java <code>Map</code> containing one String entry for the eMatrix <code>WorkspaceVault</code> id
    *     paramList     - a Java <code>HashMap</code> containing list of request parameters list including:
    *     languageStr   - language to localize to.
    *  @return Vector object that contains a vector of html code to
    *     construct the Lock Column.
    *  @throws Exception if the operation fails
    *
    *  @since DC 10.5
    */
    public static Vector getFolderAttributeGlobalReadValues(Context context,
                                                            String[] args)
        throws Exception
    {

       /*
        *  Author    : AnilJ
        *  Date      : 11/7/2002
        *  Notes     :
        *  History   :
        */

        // unpack args
        //
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        // get folder Obj Ids
        //
        MapList foldersMapList = (MapList) programMap.get("objectList");

        HashMap paramMap = (HashMap) programMap.get("paramList");

        String languageStr = (String) paramMap.get("languageStr");

        // vector containing attribute values of Global Read for
        // for folder Ids passed In
        //
        Vector columnValues = new Vector(foldersMapList.size());

        String [] objectIds = new String[foldersMapList.size()];

        // Get Ids from the MapList passed in
        //
        for(int i=0;i<foldersMapList.size();i++)
        {
            Map folderMap = (Map)foldersMapList.get(i);
            objectIds[i] = (String)folderMap.get("id");
        }




        // Get attribute values for the Ids
        //
        StringList selectList = new StringList(1);

        selectList.add("attribute["+ATTRIBUTE_GLOBAL_READ+"]");

        MapList foldersDetailMapList
            = DomainObject.getInfo(context, objectIds, selectList);


        for(int i=0;i<foldersDetailMapList.size();i++)
        {
            Map folderMap = (Map)foldersDetailMapList.get(i);
            String attrGlobalReadValue
                = (String)folderMap.get("attribute["+ATTRIBUTE_GLOBAL_READ+"]");


            i18nNow loc = new i18nNow();

            // if attribute value is false, security is private
            //
            String localizedValue = loc.GetString (
               "emxDocumentCentralStringResource",
               languageStr,
               "emxDocumentCentral.CreateFolder.Private");

            // else security is global
            //
            if(attrGlobalReadValue!=null
                && attrGlobalReadValue.equalsIgnoreCase("True"))
            {
                localizedValue
                    = loc.GetString("emxDocumentCentralStringResource",
                                    languageStr,
                                    "emxDocumentCentral.CreateFolder.Global");
            }

            // put localized display value of attribute global read in vector
            // to be returned
            //
            columnValues.add(localizedValue);
        }

        return columnValues;
    }

    /**
    *  Get Vector of Strings for Lock Icon
    *  @param context the eMatrix <code>Context</code> object
    *  @param args contains a Java <code>HashMap</code>, programMap with the following entries:
    *     objectList    - a MapList with Java <code>Map</code> containing one String entry for id of the BusinessObject to which lock info is obtained
    *     paramList     - a Java <code>HashMap</code> containing list of request parameters list including:
    *     languageStr   - language to localize to.
    *  @return Vector object that contains a vector of html code to
    *     construct the Lock Column.
    *  @throws Exception if the operation fails
    *
    *  @since DC 10.5
    */

    public static Vector getLockIcon(Context context, String[] args)
        throws Exception
    {
       //-Getting The MapList of ObjectIds after Unpaking Args

       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       MapList relBusObjPageList = (MapList)programMap.get("objectList");

       //--Getting the Param Map to get locale

       HashMap paramMap=(HashMap)programMap.get("paramList");

       //--Getting the Locale

       String locale=(String)paramMap.get("languageStr");

       //Getting the LockedBy String

       i18nNow i18obj=new i18nNow();

       String sLockedby=
          i18obj.GetString("emxDocumentCentralStringResource",locale,
                           "emxDocumentCentral.Common.LockedBy");

       //Vector of Strings containing LockInfo to be returned

       Vector lockInfo = new Vector();

       //--Select list (lockInfo/Locker)

       StringList listSelect = new StringList(2);

       listSelect.addElement(DomainConstants.SELECT_LOCKED);
       listSelect.addElement(DomainConstants.SELECT_LOCKER);

       String objIdArray[] = new String[relBusObjPageList.size()];


       if (relBusObjPageList != null)
       {
          for (int i = 0; i < relBusObjPageList.size(); i++)
          {
             objIdArray[i]
                = (String)((Map)relBusObjPageList.get(i)).get("id");

          }


          MapList resultList = null;

          //--Getting Info in One shot
          //
          resultList =DomainObject.getInfo(context,objIdArray, listSelect);

          Iterator iterator = resultList.iterator();
          HashMap aMap;

          while (iterator.hasNext())
          {

             aMap=(HashMap)iterator.next();

             String lock  = (String)aMap.get(DomainConstants.SELECT_LOCKED);
             String locker=(String)aMap.get(DomainConstants.SELECT_LOCKER);

             if (lock.equalsIgnoreCase("TRUE"))
             {
                String formatedLocker = formatUserName( context, locker);
                lockInfo.add("<img border='0' "
                             + "src='images/iconStatusLocked.gif' "
                             + "alt=\""+ sLockedby + " "
                             + XSSUtil.encodeForHTML(context, formatedLocker) + "\"/>");
             }
             else
             {
                 lockInfo.add(" " );
             }

          }
       }

       return lockInfo;
     }

    /**
     * showOwner - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Java <code>HashMap</code>, programMap with the following entries:
     *     objectList    - a MapList with Java <code>Map</code> containing one String entry for id of the BusinessObject on which Owner info is obtained
     * @returns Java <code>Vector</code>
     * @throws Exception if the operation fails
     * @since DC 10.5
     */
    public Vector showOwner(Context context, String[] args)
        throws Exception
    {
       try
       {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objectList = (MapList)programMap.get("objectList");

          Vector vecOwner = new Vector();

           if (objectList != null)
           {
              StringList listSelect = new StringList(1);
              listSelect.addElement(DomainConstants.SELECT_OWNER);

              String objIdArray[] = new String[objectList.size()];

              for (int i = 0; i < objectList.size(); i++)
              {
                  try
                  {
                      objIdArray[i] = (String)((HashMap)objectList.get(i)).get("id");
                  } catch (Exception ex)
                  {
                      objIdArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
                  }
              }

              MapList resultList = null;
              resultList =DomainObject.getInfo(context,objIdArray, listSelect);

              Iterator iterator = resultList.iterator();
              HashMap aMap;

              while (iterator.hasNext())
              {
                 aMap=(HashMap)iterator.next();
                 String userName  = (String)aMap.get(DomainConstants.SELECT_OWNER);
                 if(userName!=null && !userName.equals("null") && !userName.equals("")){
                  vecOwner.add(formatUserName( context, userName));
                 }
              }
           }
          return vecOwner;
       }
       catch (Exception ex)
       {
          //System.out.println("Error in showOwner= " + ex.getMessage());
          throw ex;
       }
    }

    /**
     * formatUserName - displays the userName with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param String - User Name
     *
     * @returns String - formatted user name "LastName, FirstName"
     * @throws Exception if the operation fails
     * @since DC 10.5
     */
    public static String formatUserName(Context context, String userName)
        throws Exception
    {

       String formatedName = null;
       try
       {
          Person person = Person.getPerson(context, userName);

          StringList selects = new StringList();
          selects.addElement(SELECT_ATTR_FIRST_NAME);
          selects.addElement(SELECT_ATTR_LAST_NAME);

          Map namesMap = person.getInfo(context, selects);
          String firstName = (String) namesMap.get (SELECT_ATTR_FIRST_NAME);
          String lastName  = (String) namesMap.get (SELECT_ATTR_LAST_NAME);

          //format the display name as lastName,firstName
          formatedName = lastName + ", " + firstName;

       }
       catch (Exception exp)
       {
          // sometimes owner can be a Role or Group, in that case display the
          // Role / Group name as it is
          formatedName = userName;
       }

       return formatedName;
    }



    /**
     * showOriginator - displays the Originator with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Java <code>HashMap</code>, programMap with the following entries:
     *     objectList    - a MapList with Java <code>Map</code> containing one String entry for id of the BusinessObject on which Originator info is obtained
     * @returns Java <code>Vector</code> containing Originator information
     * @throws Exception if the operation fails
     * @since DC 10.5
     */
    public Vector showOriginator(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector vecOwner = new Vector();

            if (objectList != null)
            {
                StringList listSelect = new StringList(1);
                listSelect.addElement("attribute["+ATTRIBUTE_ORIGINATOR+"]");

                String objIdArray[] = new String[objectList.size()];

                for (int i = 0; i < objectList.size(); i++)
                {
                    try
                    {
                        objIdArray[i] = (String)((HashMap)objectList.get(i)).get("id");
                    } catch (Exception ex)
                    {
                        objIdArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
                    }
                }

                MapList objectInfo = null;

                objectInfo =DomainObject.getInfo(context,objIdArray, listSelect);

                Iterator objectInfoIterator = objectInfo.iterator();
                HashMap aMap;

                while (objectInfoIterator.hasNext())
                {
                  aMap=(HashMap)objectInfoIterator.next();
                  String userName = (String) aMap.get("attribute["+ATTRIBUTE_ORIGINATOR+"]");
                  if(userName!=null && !userName.equals("null") && !userName.equals("")){
                    vecOwner.add(formatUserName( context, userName));
                  }
                }
            }
            return vecOwner;
        }
        catch (Exception ex)
        {
            //System.out.println("Error in showOriginator= " + ex.getMessage());
            throw ex;
        }
    }
    /**
     * showApprover - displays the Approver with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Java <code>HashMap</code>, programMap with the following entries:
     *    objectList    - a MapList with Java <code>Map</code> containing one String entry for id of the BusinessObject on which Approver info is obtained
     * @returns Java <code>Approver</code>
     * @throws Exception if the operation fails
     * @since DC 10.5
     */
    public Vector showApprover(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector vecOwner = new Vector();

            if (objectList != null)
            {
                String sAttrApprover = PropertyUtil.getSchemaProperty(context,"attribute_Approver");
                StringList listSelect = new StringList(1);
                listSelect.addElement("attribute["+sAttrApprover+"]");

                String objIdArray[] = new String[objectList.size()];

                for (int i = 0; i < objectList.size(); i++)
                {
                    try
                    {
                        objIdArray[i] = (String)((HashMap)objectList.get(i)).get("id");
                    } catch (Exception ex)
                    {
                        objIdArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
                    }
                }

                MapList objectInfo = null;

                objectInfo =DomainObject.getInfo(context,objIdArray, listSelect);

                Iterator objectInfoIterator = objectInfo.iterator();
                HashMap aMap;

                while (objectInfoIterator.hasNext())
                {
                  aMap=(HashMap)objectInfoIterator.next();
                  String userName = (String) aMap.get("attribute["+sAttrApprover+"]");
                  if(userName!=null && !userName.equals("null")){
                    vecOwner.add(formatUserName( context, userName));
                  }
                }
            }
            return vecOwner;
        }
        catch (Exception ex)
        {
            //System.out.println("Error in showApprover= " + ex.getMessage());
            throw ex;
        }
    }

     /**
     * showType - displays Types of BusinessObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Java <code>Vector</code> returning Type information
     * @throws Exception if the operation fails
     * @since DC 10.5
     */
    public Vector showType(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap paramMap=(HashMap)programMap.get("paramList");
            Vector vecOwner = new Vector();

            if (objectList != null)
            {
                //--Getting the Locale
                String languageStr=(String)paramMap.get("languageStr");

                StringList listSelect = new StringList(1);
                listSelect.addElement(DomainConstants.SELECT_TYPE);

                String objIdArray[] = new String[objectList.size()];

                for (int i = 0; i < objectList.size(); i++)
                {
                    try
                    {
                        objIdArray[i] = (String)((HashMap)objectList.get(i)).get("id");
                    } catch (Exception ex)
                    {
                        objIdArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
                    }
                }

                MapList objectInfo = null;

                objectInfo =DomainObject.getInfo(context,objIdArray, listSelect);

                Iterator objectInfoIterator = objectInfo.iterator();
                HashMap aMap;

                while (objectInfoIterator.hasNext())
                {
                  aMap=(HashMap)objectInfoIterator.next();
                  String typeName = (String) aMap.get("type");

                  if(typeName!=null && !typeName.equals("null") && !typeName.equals("")){

                    i18nNow loc     = new i18nNow();

                    String strLibrary         =PropertyUtil.getSchemaProperty(context,"type_Library");
                    String strBook            =PropertyUtil.getSchemaProperty(context,"type_Book");
                    String strBookShelf       =PropertyUtil.getSchemaProperty(context,"type_Bookshelf");
                    String strDocument        =PropertyUtil.getSchemaProperty(context,"type_Document");
                    String strGeneric_Document=PropertyUtil.getSchemaProperty(context,"type_GenericDocument");
                    String strDocument_Sheet  =PropertyUtil.getSchemaProperty(context,"type_DocumentSheet");
                    //added for the bug 314802
                    String strworkSpaceVault  =PropertyUtil.getSchemaProperty(context,"type_ProjectVault");

                    if(typeName.equalsIgnoreCase(strLibrary)){
                        typeName = loc.GetString (
                            "emxDocumentCentralStringResource",
                            languageStr,
                            "emxDocumentCentral.Common.Library");
                    }else if(typeName.equalsIgnoreCase(strBook)){
                         typeName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.Book");
                    }else if(typeName.equalsIgnoreCase(strBookShelf)){
                         typeName = loc.GetString (
                            "emxDocumentCentralStringResource",
                            languageStr,
                            "emxDocumentCentral.Common.BookShelf");
                    }else if(typeName.equalsIgnoreCase(strDocument)){
                         typeName = loc.GetString (
                            "emxDocumentCentralStringResource",
                            languageStr,
                            "emxDocumentCentral.Common.Document");
                    }else if(typeName.equalsIgnoreCase(strGeneric_Document)){
                         typeName = loc.GetString (
                            "emxDocumentCentralStringResource",
                            languageStr,
                            "emxDocumentCentral.Common.Generic_Document");
                    }else if(typeName.equalsIgnoreCase(strDocument_Sheet)){
                         typeName = loc.GetString (
                            "emxDocumentCentralStringResource",
                            languageStr,
                            "emxDocumentCentral.Common.Document_Sheet");
                    }
                    //added for the bug 314802
                    else if(typeName.equalsIgnoreCase(strworkSpaceVault)){
                         typeName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.WorkSpaceVault");
                    }
                    vecOwner.add(typeName);
                  }
              }
            }
            return vecOwner;
        }
        catch (Exception ex)
        {
            //System.out.println("Error in showApprover= " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * showState - displays State of BusinessObject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Java <code>Vector</code> returning State information
     * @throws Exception if the operation fails
     * @since DC 10.5
     */
    public Vector showState(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap paramMap=(HashMap)programMap.get("paramList");
            Vector vecOwner = new Vector();

            if (objectList != null)
            {
                //--Getting the Locale
                String languageStr=(String)paramMap.get("languageStr");

                StringList listSelect = new StringList(1);
                listSelect.addElement(DomainConstants.SELECT_CURRENT);

                String objIdArray[] = new String[objectList.size()];

                for (int i = 0; i < objectList.size(); i++)
                {
                    try
                    {
                        objIdArray[i] = (String)((HashMap)objectList.get(i)).get("id");
                    } catch (Exception ex)
                    {
                        objIdArray[i] = (String)((Hashtable)objectList.get(i)).get("id");
                    }
                }

                MapList objectInfo = null;

                objectInfo =DomainObject.getInfo(context,objIdArray, listSelect);

                Iterator objectInfoIterator = objectInfo.iterator();
                HashMap aMap;

                while (objectInfoIterator.hasNext())
                {
                  aMap=(HashMap)objectInfoIterator.next();
                  String stateName = (String) aMap.get("current");

                  if(stateName!=null && !stateName.equals("null") && !stateName.equals(""))
                  {

                    i18nNow loc = new i18nNow();
                    if(stateName.equalsIgnoreCase("Create")){
                        stateName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.Create");
                    }else if(stateName.equalsIgnoreCase("Approved")){
                         stateName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.Approved");
                    }else if(stateName.equalsIgnoreCase("Review")){
                         stateName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.Review");
                    }else if(stateName.equalsIgnoreCase("Locked")){
                         stateName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.Locked");
                    }else if(stateName.equalsIgnoreCase("WIP")){
                         stateName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.WIP");
                    } else if(stateName.equalsIgnoreCase("Released")){
                         stateName = loc.GetString (
                             "emxDocumentCentralStringResource",
                             languageStr,
                             "emxDocumentCentral.Common.Released");
                    } else if(stateName.equalsIgnoreCase("Obsolete")){
                          stateName = loc.GetString (
                              "emxDocumentCentralStringResource",
                              languageStr,
                              "emxDocumentCentral.Common.Obsolete");
                    }else if(stateName.equalsIgnoreCase("ACTIVE")){
                          stateName = loc.GetString (
                              "emxDocumentCentralStringResource",
                              languageStr,
                              "emxDocumentCentral.Common.ACTIVE");

                    }else if(stateName.equalsIgnoreCase("IN-ACTIVE")){
                          stateName = loc.GetString (
                              "emxDocumentCentralStringResource",
                              languageStr,
                              "emxDocumentCentral.Common.IN-ACTIVE");
                    }else if(stateName.equalsIgnoreCase("Exists")){
                         stateName = loc.GetString (
                            "emxDocumentCentralStringResource",
                            languageStr,
                            "emxDocumentCentral.Common.Exists");
                    }
                    vecOwner.add(stateName);
                 }
            }
          }
          return vecOwner;
        }
        catch (Exception ex)
        {
            //System.out.println("Error in showApprover= " + ex.getMessage());
            throw ex;
        }
    }



        /**
         * getAllFolders - Get all folders in Document Central
         *
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns eMatrix <code>MapList</code> containing all folders and sub-folders
         * @throws Exception if the operation fails
         * @since DC 10.5
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllFolders(Context context, String[] args)
            throws Exception
        {
            return getFolders(context, args, "");
        }

        /**
         * getAllTopLevelFolders - Get all top level folders in Document Central
         *
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns eMatrix <code>MapList</code> containing all top level folders
         * @throws Exception if the operation fails
         * @since LC 10.6.SP2
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getAllTopLevelFolders(Context context, String[] args)
            throws Exception
        {
            StringBuffer where = new StringBuffer(DomainConstants.EMPTY_STRING);
            where.append("(to[");
            where.append(DomainObject.RELATIONSHIP_SUB_VAULTS);
            where.append("] == False)");

            return getFolders(context, args, where.toString());
        }

        /**
         * getFolders - Get folders
         *
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns eMatrix <code>MapList</code> containing list of DC Folders
         * @param whereExpression the expression to filter
         * @throws Exception if the operation fails
         * @since LC 10.6.SP2
         */
        protected MapList getFolders(Context context, String[] args, String whereExpression)
            throws Exception
        {
            try
            {
                ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);

                if(whereExpression == null || "null".equals(whereExpression))
                {
                    whereExpression = "";
                }
                StringList busSelects = new StringList();
                busSelects.add(DomainConstants.SELECT_ID);
                MapList mpList =   DomainObject.findObjects(context,
                                                 PropertyUtil.getSchemaProperty(context,"type_ProjectVault"),
                                                 "*",
                                                 "",
                                                 "*",
                                                 "*",
                                                 whereExpression,
                                                 false,
                                                 busSelects);

                MapList returnList=new MapList();
                Iterator it =mpList.iterator();
                while(it.hasNext())
                {
                    Map map=(Map)it.next();
                    String id =(String)map.get(DomainConstants.SELECT_ID);
                    returnList.add(map);
                }

                return returnList;
            }
            catch (Exception ex)
            {
                //System.out.println("Error in getFoldersIOwn = " + ex.getMessage());
                throw ex;
            }
        }

        /**
         * getFoldersIOwn - Get Owned folders in Document Central
         *
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns eMatrix <code>MapList</code> containing list of DC Folders owned by context person
         * @throws Exception if the operation fails
         * @since DC 10.5
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getFoldersIOwn(Context context, String[] args)
            throws Exception
        {
            StringBuffer where = new StringBuffer(DomainConstants.EMPTY_STRING);
            where.append("(");
            where.append(DomainConstants.SELECT_OWNER);
            where.append("==\"");
            where.append(context.getUser());
            where.append("\")");

            return getFolders(context, args, where.toString());
        }

  /**
     * get sub folder list for a given folder boject
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing the parameters - Object id
     * @return Maplist of sub-folders
     * @throws Exception if the operation fails
     * @since LC 10.6.SP2
     */
    public static MapList getSubFolderList(Context context,
        String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");

        String objectId = (String) paramMap.get("objectId");
        DomainObject domainObject =
            DomainObject.newInstance(context, objectId);
        String objectType =
            domainObject.getInfo(context, DomainObject.SELECT_TYPE);

        MapList folderList = new MapList();


        try
        {
            StringList typeSelects = new StringList(1);
            typeSelects.add(DomainObject.SELECT_TYPE);
            typeSelects.add(DomainObject.SELECT_ID);
            typeSelects.add(DomainObject.SELECT_NAME);
            folderList =
                domainObject.getRelatedObjects(context,
                    DomainObject.RELATIONSHIP_SUB_VAULTS,
                    DomainObject.TYPE_PROJECT_VAULT, typeSelects,
                    new StringList(), false, true, (short) 1, null, null);
        }
        catch (Exception e)
        {
            throw new FrameworkException(e);
        }

        return folderList;
    }

        /**
         * getTopLevelFoldersIOwn - Get Owned top level folders in Document Central
         *
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns eMatrix <code>MapList</code> containing list of DC Folders owned by context person
         * @throws Exception if the operation fails
         * @since LC 10.6.SP2
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getTopLevelFoldersIOwn(Context context, String[] args)
            throws Exception
        {
            StringBuffer where = new StringBuffer(DomainConstants.EMPTY_STRING);
            where.append("(");
            where.append(DomainConstants.SELECT_OWNER);
            where.append("==\"");
            where.append(context.getUser());
            where.append("\") && (to[");
            where.append(DomainObject.RELATIONSHIP_SUB_VAULTS);
            where.append("] == False)");

            return getFolders(context, args, where.toString());
        }

        /**
         * isDCFolder - returns true for DC folders
         *
         *
         * @param context the eMatrix <code>Context</code> object
         * @param String holds object id
         * @returns boolean
         * @throws Exception if the operation fails
         * @since DC 10.5
         */

        public boolean isDCFolder(Context context, String id)
         throws Exception
        {
           boolean retVal=false;
           DomainObject dmObj = DomainObject.newInstance(context,id);
           String vaultId=(String)dmObj.getInfo(context,"to["+DomainObject.RELATIONSHIP_WORKSPACE_VAULTS +"].from.id" );
           if(vaultId==null)
           {
               //Not connected with Workspace or Project but may be connected with other
               //workspacevault which may be connected with Workspace or project
               vaultId=(String)dmObj.getInfo(context,"to["+DomainObject.RELATIONSHIP_SUB_VAULTS +"].from.id" );
               if(vaultId==null)
               {
                   retVal=true;
               }
               else
               {
                   retVal=isDCFolder(context,vaultId);
               }
           }
           return retVal;
        }

        /**
         * getMyCheckedOutDocs - Get the list of Master Documents which have at least one
         * file (Version Document) checked out by context user
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @returns eMatrix <code>MapList</code> containing list of Master Documents with at least one checked out file
         * @throws Exception if the operation fails
         * @since DC 10.5
         */

        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getMyCheckedOutDocs(Context context, String[] args)
            throws Exception
        {
          try
          {
                String relPattern         = PropertyUtil.getSchemaProperty(context, DomainObject.SYMBOLIC_relationship_ActiveVersion);
                String attrIsVersionObject = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_attribute_IsVersionObject);
                StringList busSelects     = new StringList(1);
                //busSelects.add(DomainConstants.SELECT_ID);
                busSelects.add("to[" + relPattern +"].from.id");

                String objectWhere = "(attribute[" + attrIsVersionObject + "]==True && locker==\""+ context.getUser() +"\")";

                // get all Generic Documents
                MapList tempList = DomainObject.findObjects(context,
                                                           TYPE_DOCUMENTS,
                										   "*",
                                                           objectWhere,
                                                           busSelects);
                //The tempList could contain duplicate ids
                //need to go through the list to remove duplicate ones
                MapList  docList = new MapList();
                StringList processedIdsList = new StringList();

                if(tempList != null && tempList.size() > 0)
                {
                   for(int i=0; i < tempList.size(); i++){
                       Map objMap = (Map)tempList.get(i);

                       String id = (String)objMap.get("to[" + relPattern + "].from.id");
                       if(id != null && !"".equals(id) && !"null".equals(id) &&
                          !processedIdsList.contains(id)){
                           processedIdsList.add(id);
                           Map newObjMap = new HashMap();
                           newObjMap.put("id",id);
                           docList.add(newObjMap);
                       }
                   }
                }

           return docList;
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
          throw ex;
        }
      }

       /**
        * To enable Revision action links for persons with Revise access
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args the Java <code>String[]<code> object
        *    0 - HashMap containing one String entry for key Object Id
        *
        * @return the Java <code>boolean</code> true or false depending on
        *  object state and access
        *
        * @throws Exception if the operation fails
        *
        * @since AEF 10-5
        */

        public boolean showReviseLinks (Context context, String[] args)
            throws Exception
        {
           Map map                             = (Map) JPO.unpackArgs(args);
           String objectId                     = (String) map.get("objectId");
           boolean showReviseLinks             = false;

           if(objectId != null)
           {
             DomainObject domainObject = DomainObject.newInstance(context , objectId);
             
             showReviseLinks = ( LibraryCentralCommon.hasReviseAccess(context, objectId)
                     && domainObject.isLastRevision(context) );
           }
          return showReviseLinks;
        }

      /**
       * Trigger to perform the promote check on a Document to Reeview State
       * It does the following:
       *   - Ensure Approver is not empty
       *
       * @param context the eMatrix <code>Context</code> object.
         * @param args holds the following input arguments:
         *        0 - objectId String

       * @return 1 to block, 0 to process event.
       * @throws Exception If the operation fails.
       * @since 10-5.
       * @trigger PolicyControlledDesignReleaseRev2StateCreatePromoteCheck.
       * @trigger PolicyControlledProductionReleaseRev2StateCreatePromoteCheck.
       */
      public int checkApprover(Context context, String[] args) throws Exception
      {

            int checkApprover = 0;
            String objectId = args[0];
            DomainObject object = new DomainObject(objectId);
            String sAttrApprover = PropertyUtil.getSchemaProperty(context,"attribute_Approver");
            String sApproverVal = object.getInfo(context, "attribute["+sAttrApprover+"]");
            if(sApproverVal == null || "".equals(sApproverVal) || "null".equals(sApproverVal))
            {
                checkApprover = 1;
                throw new FrameworkException(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.Message.ApproverEmtpy"));
            }
            return checkApprover;
        }

        /**
        * Checks whether the logged in user can create Document Sheet Object
        * Create New link for creating document sheet object will be displayed on check of following condition
        * 1. Parent Generic document object should be in Create or WIP state
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the following input arguments:
        *  0 - Object Id of the Generic Document Object
        * @returns boolean true for Success & false for Fail
        * @throws Exception if the operation fails
        * @since DC 10.5
        */

        public boolean checkCreateAccessForDocumentSheet(Context context, String[] args)
            throws Exception
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId  = (String) paramMap.get("objectId");
            boolean showCreateLink = false;
            if(objectId!=null && !objectId.equals("") && !objectId.equals("null") )
            {
                StringList busSelects = new StringList(2);
                busSelects.add(DomainObject.SELECT_POLICY);
                busSelects.add(DomainObject.SELECT_CURRENT);
                DomainObject docObj = DomainObject.newInstance(context,objectId);

                Map docMap = docObj.getInfo(context, busSelects);
                String docPolicy = (String) docMap.get(DomainObject.SELECT_POLICY);
                String docState = (String) docMap.get(DomainObject.SELECT_CURRENT);
                String STATE_CREATE = PropertyUtil.getSchemaProperty(context,"policy",docPolicy,"state_Create");
                String STATE_WIP = PropertyUtil.getSchemaProperty(context,"policy",docPolicy,"state_WIP");
                if(docState.equalsIgnoreCase(STATE_CREATE) || docState.equalsIgnoreCase(STATE_WIP))
                {
                  showCreateLink = true;
                }
            }
            return showCreateLink;
        }

   /**
    * Get the list of all the Documents created by context user.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList containing list of all Documents created by context user
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getAllDocumentsList(Context context, String[] args) throws Exception
    {
        try{
            StringBuffer objectWhere  = new StringBuffer();
            objectWhere.append("attribute[");
            objectWhere.append(CommonDocument.ATTRIBUTE_IS_VERSION_OBJECT);
            objectWhere.append("] == False && owner ==\"");
            objectWhere.append(context.getUser());
            objectWhere.append("\"");
            StringList selectList      = new StringList(1);
            selectList.addElement(DomainObject.SELECT_ID);
            // get all  Documents of type DOCUMENTS
            MapList result             = DomainObject.findObjects(context,
                                                              TYPE_DOCUMENTS,
                                                              "*",
                                                              objectWhere.toString(),
                                                              selectList);
            return result;
        }catch(Exception exp) {
            throw new Exception(exp.toString());
        }
    }

   /**
    * Get the list of all the pending Documents of type Generic Document and
    * DOCUMENT CLASSIFICATION
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList containing list of all pending Documents.
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getPendingDocumentsList(Context context, String[] args) throws Exception
    {
        try{
            StringBuffer objectWhere  = new StringBuffer();
            objectWhere.append("attribute[");
            objectWhere.append(ATTRIBUTE_APPROVER);
            objectWhere.append("] == context.user.name");
            objectWhere.append(" && (current.signature.signed==FALSE || current.signature.rejected==TRUE)");

            //the type's Generic Document and DOCUMENTCLASSIFICATION
            StringBuffer buf          = new StringBuffer();
            buf.append(TYPE_DOCUMENT_LIBRARY);
            buf.append(",");
            buf.append(TYPE_DOCUMENT_FAMILY);
            buf.append(",");
            buf.append(TYPE_GENERIC_DOCUMENT);

            StringList selectList     = new StringList(1);
            selectList.addElement(DomainObject.SELECT_ID);
            // finding all pending Documents
            MapList result            = DomainObject.findObjects(context,
                                                     buf.toString(),
                                                     "*",
                                                     objectWhere.toString(),
                                                     selectList);
            return result;
        }catch(Exception exp){
            throw new Exception(exp.toString());
        }
    }

   /**
    * Get the list of rejected Documents of type Generic Document
    * and DOCUMENT CLASSIFICATION
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList containing list of all pending Documents.
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public  MapList getRejectedDocumentsList(Context context, String[] args) throws Exception
    {
        try{
            StringBuffer objectWhere  = new StringBuffer();
            objectWhere.append("current.signature.rejected==TRUE && owner ==\"");
            objectWhere.append(context.getUser());
            objectWhere.append("\"");

            StringBuffer buf          = new StringBuffer();
            buf.append(TYPE_DOCUMENT_LIBRARY);
            buf.append(",");
            buf.append(TYPE_DOCUMENT_FAMILY);
            buf.append(",");
            buf.append(TYPE_GENERIC_DOCUMENT);

            StringList selectList     = new StringList(1);
            selectList.addElement(DomainObject.SELECT_ID);
            // finding all rejected Documents
            MapList result            = DomainObject.findObjects(context,
                                                         buf.toString(),
                                                         "*",
                                                         objectWhere.toString(),
                                                         selectList);
            return result;

        }catch(Exception exp){
            throw new Exception(exp.toString());
        }
    }

   /**
    * This method returns the contents of folders and subfolders too
    *
    * @param context the eMatrix <code>Context</code> object
    * @param String holds object id
    * @returns MapList
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getFolderContents(Context context, String[] args) throws Exception
    {
        HashMap programMap        = (HashMap) JPO.unpackArgs(args);
        String objectId           = (String) programMap.get("objectId");
        DomainObject domainObject = new DomainObject(objectId);

        StringBuffer relList      = new StringBuffer();
        relList.append(RELATIONSHIP_SUB_VAULTS).append(",");
        relList.append(RELATIONSHIP_VAULTED_DOCUMENTS_REV2);		

        MapList folderList = new MapList();
        try
        {
            StringList typeSelects = new StringList(1);
            typeSelects.add(DomainObject.SELECT_ID);
            folderList = domainObject.getRelatedObjects(context,
                                           relList.toString(),
                                            "*",
                                            typeSelects,
                                            new StringList(),
                                            false,
                                            true,
                                            (short) 1,
                                            null,
                                            null);
        }catch (Exception e){
            throw new FrameworkException(e);
        }
        return folderList;
    }
}
