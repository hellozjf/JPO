/*   emxCommonFileBase.java
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the code for checkin
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
/**
 * The <code>emxCommonFileBase</code> class contains code for checkin.
 *
 * @version C 10.0.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonFileBase_mxJPO extends emxDomainObject_mxJPO
{

    static final boolean defaultAppend = false;
    static final boolean defaultUnlock = true;
    static final String defaultFormat = DomainConstants.FORMAT_GENERIC;
    private static String default_Version_Doc_Extension = "VD_";
    protected static final String FIRST_VERSION_DOCUMENT_REV = "1";

      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       * @since Common 10.0.0.0
       * @grade 0
       */
      public emxCommonFileBase_mxJPO (Context context, String[] args)
          throws Exception
      {
          super(context, args);
      }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns nothing
       * @throws Exception if the operation fails
       * @since Common 10.0.0.0
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
          }
          return 0;
      }

    /**
     * This method is executed to create/revise object and/or checkin using FCS/NonFCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String objectId in which file checkedin
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String checkin(Context context, String[] args) throws Exception
     {
          try
          {
              //ContextUtil.startTransaction(context, true);
              if (args == null || args.length < 1)
              {
                  throw (new IllegalArgumentException());
              }
              HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

              String parentId       = (String) uploadParamsMap.get("parentId");
              String objectId       = (String) uploadParamsMap.get("objectId");
              String objectAction   = (String) uploadParamsMap.get("objectAction");
              String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
              String receiptValue = (String) uploadParamsMap.get(DocumentUtil.getJobReceiptParameterName(context));
              String strUnlock  = (String) uploadParamsMap.get("unlock");
              String strAppend  = (String) uploadParamsMap.get("append");
              String format     = (String) uploadParamsMap.get("format");
              String fileName   = (String) uploadParamsMap.get("fileName");
              String store      = (String) uploadParamsMap.get("store");

              boolean unlock = "true".equalsIgnoreCase(strUnlock);
              boolean append = "true".equalsIgnoreCase(strAppend);

              StringList strFileList = new StringList(1);
              strFileList.addElement(fileName);
              if (objectId == null || "".equals(objectId) || "null".equals(objectId) )
              {
                  objectId = "true".equalsIgnoreCase(fcsEnabled) ? checkinCreate(context, args) : mcsCheckinCreate(context, args);
              } else if ("updateVersion".equals(objectAction) ) {
                  objectId = updateVersionDocument(context, args);
                  fcsEnabled = "false";
              } else {
                  if ("revise".equalsIgnoreCase(objectAction))
                  {
                      objectId = "true".equalsIgnoreCase(fcsEnabled) ? checkinRevise(context, args) : mcsCheckinRevise(context, args);
                  }
                  else
                  {
                      if ( "false".equalsIgnoreCase(fcsEnabled) )
                      {
                          DomainObject document = (DomainObject) DomainObject.newInstance(context,objectId);
                          document.checkinFromServer(context, unlock, append, format, store, strFileList);
                      }
                  }
              }
              if ( "true".equalsIgnoreCase(fcsEnabled) && objectId != null)
              {
                  checkinUpdate(context, objectId, store, format, fileName, strAppend, strUnlock, receiptValue);
              }
              //ContextUtil.commitTransaction(context);
              return objectId;
          }
          catch (Exception ex)
          {
              //ContextUtil.abortTransaction(context);
              throw ex;
          }
    }


    /**
     * This method is executed to update the meta date while using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the String Object Id of the Object for Update
     * @param store String to specify in which store these files need to get checked in
     * @param format String the specifies in which format the files should get checked in
                            if this is null then file will be checked in to generic format
     * @param fileName the String fileName moved to the store
     * @param append String true if these files need to get appended to the existing file
                            false if these files need to over-write existing files
     * @param unlock String true to unlock this file after checkin
                            false to hold lock on this document
     * @param receiptValue String specifies the receiptValue of the file moved to store
     * @returns String objectId in which file checkedin
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void checkinUpdate(Context context, String objectId,String store,
                              String format,String fileName, String append,
                              String unlock, String receiptValue)
                throws Exception
    {
        try
        {
            DocumentUtil.checkinUpdate(context, objectId, store, format, fileName, append, unlock, receiptValue);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw (ex);
        }
    }



    /**
     * This method is executed to create an  object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String checkinCreate(Context context, String[] args) throws Exception
     {

          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String parentId       = (String) uploadParamsMap.get("parentId");
          String parentRelName  = (String) uploadParamsMap.get("parentRelName");
          if ( parentRelName != null )
          {
              parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
          }

          String name       = (String) uploadParamsMap.get("name");
          String type       = (String) uploadParamsMap.get("type");
          String title      = (String) uploadParamsMap.get("title");
          if( title == null || title.equals("") )
          {
              title = (String) uploadParamsMap.get("fileName");
          }
          String language   = (String) uploadParamsMap.get("language");
          String description= (String) uploadParamsMap.get("description");

          String policy = (String) uploadParamsMap.get("policy");

          return checkinCreate(context, type, name, policy, description, title, language, parentId, parentRelName);
    }


    /**
    * This method is executed to create a document object and checkin using FCS.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param type the String type name for the new document - default to Document if - null
    * @param name the String name of the document objecct getting created
    * @param policy the String policy for the new Document object
    * @param description the String description of the document objecct
    * @param title the String title attribute vaule of the document objecct
    * @param language the String language attribute vaule of the document objecct
    * @param parentId the String parent object id of the document objecct
    * @param parentRelName the String parentRelationship name of the document objecct
    * @returns String new Document Id
    * @throws Exception if the operation fails
    * @since Common 10.0.0.0
    */
    public String checkinCreate(Context context, String type, String name,
                                      String policy, String description,
                                      String title, String language,
                                      String parentId, String parentRelName)
                      throws Exception
    {
        return checkinCreate(context, type, name, policy, description,
                        title, language, parentId, parentRelName, true);
    }

    /**
     * This method is executed to create a document object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param type the String type name for the new document - default to Document if - null
     * @param name the String name of the document objecct getting created
     * @param policy the String policy for the new Document object
     * @param description the String description of the document objecct
     * @param title the String title attribute vaule of the document objecct
     * @param language the String language attribute vaule of the document objecct
     * @param parentId the String parent object id of the document objecct
     * @param parentRelName the String parentRelationship name of the document objecct
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String checkinCreate(Context context, String type, String name,
                                      String policy, String description,
                                      String title, String language,
                                      String parentId, String parentRelName, boolean isFrom)
                      throws Exception
     {
          try
          {
              ContextUtil.startTransaction(context, true);

              Document document = (Document) DomainObject.newInstance(context,DomainConstants.TYPE_DOCUMENT);

              document = document.create(context, type, name, policy, description, title, language);
              if (parentId != null && parentRelName != null)
              {
                  if ( isFrom )
                  {
                      document.addFromObject(context, new RelationshipType(parentRelName), parentId);
                  } else {
                      document.addToObject(context, new RelationshipType(parentRelName), parentId);
                  }
              }
              ContextUtil.commitTransaction(context);
              return document.getObjectId();
          }
          catch (Exception ex)
          {
              ContextUtil.abortTransaction(context);
              throw ex;
          }
    }

    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
     public String mcsCheckinCreate(Context context, String[] args) throws Exception
     {


          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String parentId       = (String) uploadParamsMap.get("parentId");
          String parentRelName  = (String) uploadParamsMap.get("parentRelName");
          if ( parentRelName != null )
          {
              parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
          }

          String strUnlock  = (String) uploadParamsMap.get("unlock");
          String strAppend  = (String) uploadParamsMap.get("append");
          String format     = (String) uploadParamsMap.get("format");
          String fileName   = (String) uploadParamsMap.get("fileName");
          String store      = (String) uploadParamsMap.get("store");

          String name       = (String) uploadParamsMap.get("name");
          String title      = (String) uploadParamsMap.get("title");
          if( title == null || title.equals("") )
          {
              title = fileName;
          }
          String language   = (String) uploadParamsMap.get("language");
          String description= (String) uploadParamsMap.get("description");
          String type       = (String) uploadParamsMap.get("type");
          String policy       = (String) uploadParamsMap.get("policy");
          String isFromStr = (String) uploadParamsMap.get("isFrom");
          if( isFromStr == null || "".equals(isFromStr) ) {
            isFromStr = "true";
          }

          boolean unlock = "true".equalsIgnoreCase(strUnlock);
          boolean append = "true".equalsIgnoreCase(strAppend);
          boolean isFrom = "true".equalsIgnoreCase(isFromStr);

          StringList fileList = new StringList(1);
          fileList.addElement(fileName);
          return mcsCheckinCreate(context, fileList, type, name, policy, title, description,
                                        store, append, format, unlock, language,
                                        parentId, parentRelName, isFrom);

     }

    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param fileList the StringList of files need to checkin to this document Object
     * @param name the String name of the document objecct getting created
     * @param title the String title attribute vaule of the document objecct
     * @param description String of Description of the document this string will be set as
                            Description of the document created in this method
     * @param store String to specify in which store these files need to get checked in
     * @param append boolean true if these files need to get appended to the existing file
                            false if these files need to over-write existing files
     * @param format String the specifies in which format the files should get checked in
                            if this is null then file will be checked in to generic format
     * @param unlock boolean true to unlock this file after checkin
                            false to hold lock on this document
     * @param language String specifies the language of the file getting checked in
     * @param parentId the String parent object id of the document objecct
     * @param parentRelName the String parentRelationship name of the document objecct
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.1.0
     */
     public String mcsCheckinCreate(Context context, StringList fileList, String type, String name,
                                        String policy, String title, String description, String store,
                                        boolean append, String format,  boolean unlock,
                                        String language, String parentId, String parentRelName, boolean isFrom)
                    throws Exception
     {
          try
          {
              ContextUtil.startTransaction(context, true);
              Document document = (Document) DomainObject.newInstance(context,DomainConstants.TYPE_DOCUMENT);
              document = document.create(context, type, name, policy, description, title, language);
              document.checkinFromServer(context, unlock, append, format, store, fileList);
              if (parentId != null && parentRelName != null)
              {
                  if ( isFrom )
                  {
                      document.addFromObject(context, new RelationshipType(parentRelName), parentId);
                  } else {
                      document.addToObject(context, new RelationshipType(parentRelName), parentId);
                  }
              }
              ContextUtil.commitTransaction(context);
              return document.getObjectId();
          }
          catch (Exception ex )
          {
              ContextUtil.abortTransaction(context);
              throw ex;
          }
     }

    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param fileList the StringList of files need to checkin to this document Object
     * @param name the String name of the document objecct getting created
     * @param title the String title attribute vaule of the document objecct
     * @param description String of Description of the document this string will be set as
                            Description of the document created in this method
     * @param store String to specify in which store these files need to get checked in
     * @param append boolean true if these files need to get appended to the existing file
                            false if these files need to over-write existing files
     * @param format String the specifies in which format the files should get checked in
                            if this is null then file will be checked in to generic format
     * @param unlock boolean true to unlock this file after checkin
                            false to hold lock on this document
     * @param language String specifies the language of the file getting checked in
     * @param parentId the String parent object id of the document objecct
     * @param parentRelName the String parentRelationship name of the document objecct
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String mcsCheckinCreate(Context context, StringList fileList, String name,
                                        String title, String description, String store,
                                        boolean append, String format,  boolean unlock,
                                        String language, String parentId, String parentRelName)
                    throws Exception
     {
          return mcsCheckinCreate(context, fileList, name, title, description,
                            store, append, format, unlock, language,
                            parentId, parentRelName, true);
     }


    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param fileList the StringList of files need to checkin to this document Object
     * @param name the String name of the document objecct getting created
     * @param title the String title attribute vaule of the document objecct
     * @param description String of Description of the document this string will be set as
                            Description of the document created in this method
     * @param store String to specify in which store these files need to get checked in
     * @param append boolean true if these files need to get appended to the existing file
                            false if these files need to over-write existing files
     * @param format String the specifies in which format the files should get checked in
                            if this is null then file will be checked in to generic format
     * @param unlock boolean true to unlock this file after checkin
                            false to hold lock on this document
     * @param language String specifies the language of the file getting checked in
     * @param parentId the String parent object id of the document objecct
     * @param parentRelName the String parentRelationship name of the document objecct
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String mcsCheckinCreate(Context context, StringList fileList, String name,
                                        String title, String description, String store,
                                        boolean append, String format,  boolean unlock,
                                        String language, String parentId, String parentRelName, boolean isFrom)
                    throws Exception
     {
          try
          {
              ContextUtil.startTransaction(context, true);
              Document document = (Document) DomainObject.newInstance(context,DomainConstants.TYPE_DOCUMENT);
              document.checkinCreate(context, fileList, name, title,description, store, append, format,unlock, language);
              if (parentId != null && parentRelName != null)
              {
                  if ( isFrom )
                  {
                      document.addFromObject(context, new RelationshipType(parentRelName), parentId);
                  } else {
                      document.addToObject(context, new RelationshipType(parentRelName), parentId);
                  }
              }
              ContextUtil.commitTransaction(context);
              return document.getObjectId();
          }
          catch (Exception ex )
          {
              ContextUtil.abortTransaction(context);
              throw ex;
          }
     }

    /**
     * This method is executed to revise the document object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String checkinRevise(Context context, String[] args) throws Exception
     {
          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String objectId       = (String) uploadParamsMap.get("objectId");

          String title      = (String) uploadParamsMap.get("title");
          String language   = (String) uploadParamsMap.get("language");
          String description= (String) uploadParamsMap.get("description");

          String reason = (String) uploadParamsMap.get("reason");

          return checkinRevise(context, objectId, description, language, reason);
     }


    /**
     * This method is executed to revise the document object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the String of Object id of the document
     * @param description String of Description of the document this string will be set as
                            Description of the document created in this method
     * @param language String specifies the language of the file getting checked in
     * @param reason String specifies the Reason For Change of the file getting checked in
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String checkinRevise(Context context, String objectId, String description,
                                       String language, String reason) throws Exception
     {
          try
          {
              ContextUtil.startTransaction(context, true);

              Document document = (Document) DomainObject.newInstance(context,objectId);
              document = document.revise(context);
              if ( description != null )
              {
                  document.setDescription(context, description);
              }
              Map attrMap = new HashMap();
              if ( language != null )
              {
                  attrMap.put(DomainConstants.ATTRIBUTE_LANGUAGE, language);
              }
              if ( reason != null )
              {
                  attrMap.put(DomainConstants.ATTRIBUTE_CHECKIN_REASON, reason);
              }
              document.setAttributeValues(context,attrMap);

              ContextUtil.commitTransaction(context);
              return document.getObjectId();
          }
          catch (Exception ex )
          {
              ContextUtil.abortTransaction(context);
              throw ex;
          }

     }


    /**
     * This method is executed to revise the document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public String mcsCheckinRevise(Context context, String[] args) throws Exception
     {


          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String objectId       = (String) uploadParamsMap.get("objectId");

          String strUnlock  = (String) uploadParamsMap.get("unlock");
          String strAppend  = (String) uploadParamsMap.get("append");
          String format     = (String) uploadParamsMap.get("format");
          String fileName   = (String) uploadParamsMap.get("fileName");
          String store      = (String) uploadParamsMap.get("store");

          String language   = (String) uploadParamsMap.get("language");
          String description= (String) uploadParamsMap.get("description");

          boolean unlock = "true".equalsIgnoreCase(strUnlock);
          boolean append = "true".equalsIgnoreCase(strAppend);

          StringList strFileList = new StringList(1);
          strFileList.addElement(fileName);
          Document document = (Document) DomainObject.newInstance(context,objectId);
          document = document.checkinRevise(context,strFileList, description, store, append, format, unlock, language);

          return document.getObjectId();
     }

    /**
     * This method is executed to create/revise object and/or checkin using FCS/NonFCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String objectId in which file checkedin
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public Map multiFileCheckin(Context context, String[] args) throws Exception
     {
          try
          {
              //ContextUtil.startTransaction(context, true);
              if (args == null || args.length < 1)
              {
                  throw (new IllegalArgumentException());
              }
              HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

              String parentId       = (String) uploadParamsMap.get("parentId");
              String objectId       = (String) uploadParamsMap.get("objectId");
              String objectAction   = (String) uploadParamsMap.get("objectAction");
              String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
              String receiptValue = (String) uploadParamsMap.get(DocumentUtil.getJobReceiptParameterName(context));

              String store      = (String) uploadParamsMap.get("store");
              String strCounnt = (String) uploadParamsMap.get("noOfFiles");
              int count = new Integer(strCounnt).intValue();
              StringList fileNames = new StringList(count);
              StringList formats = new StringList(count);
              StringList objectIds = new StringList(count);
              StringList unlocks = new StringList(count);
              StringList appends = new StringList(count);

              Map objectMap = new HashMap();
                 // line below is commented to fix bug 273655
                 //StringList strFileList = new StringList(1);
              boolean getFromObjectMap = true;
              Document document = (Document) newInstance(context,DomainConstants.TYPE_DOCUMENT);
              if ("create".equalsIgnoreCase(objectAction) )
              {
                  objectMap = "true".equalsIgnoreCase(fcsEnabled) ? multiFileCheckinCreate(context, args) : mcsMultiFileCheckinCreate(context, args);
              } else  if ("revise".equalsIgnoreCase(objectAction)) {
                  objectMap = "true".equalsIgnoreCase(fcsEnabled) ? multiFileCheckinRevise(context, args) : mcsMultiFileCheckinRevise(context, args);
              } else if ( "createVersion".equalsIgnoreCase(objectAction) ) {
                  objectMap = checkinCreateVersions(context, args);
                  getFromObjectMap = false;
              }
              else if (objectId != null && !"".equals(objectId) && !"null".equals(objectId) )
              {
                  getFromObjectMap = false;
                  if ( !"true".equalsIgnoreCase(fcsEnabled) )
                  {
                      document.setId(objectId);
                  }
                  for( int i=0; i<count; i++ )
                  {
                      String formatI  = (String)uploadParamsMap.get("format" + i);
                      String fileNameI  = (String)uploadParamsMap.get("title" + i);

                      if (fileNameI != null && !"".equals(fileNameI) && !"null".equals(fileNameI))
                      {
                          if ( !"true".equalsIgnoreCase(fcsEnabled) ) {
                                 // below line is added to fix: 273655
                                 StringList strFileList = new StringList(1);
                              strFileList.addElement(fileNameI);
                              preCheckin(context, args);
                              document.checkinFromServer(context, true, true, formatI, store, strFileList);
                              postCheckin(context, args, objectId);
                          } else {
                              formats.addElement(formatI);
                              fileNames.addElement(fileNameI);
                              objectIds.addElement(objectId);
                              appends.addElement("true");
                              unlocks.addElement("true");
                          }
                      }
                  }
              }

              if ( getFromObjectMap )
              {
                  objectIds = (StringList) objectMap.get("objectId");
                  formats = (StringList) objectMap.get("format");
                  fileNames = (StringList) objectMap.get("fileName");
                  appends = (StringList) objectMap.get("appends");
                  unlocks = (StringList) objectMap.get("unlocks");

              }
              if ( "true".equalsIgnoreCase(fcsEnabled) && objectIds.size() > 0)
              {
                  if ( appends != null && appends.size() == objectIds.size() )
                  {
                    multiFileCheckinUpdate(context, objectIds, store, formats, fileNames, appends, unlocks, receiptValue);
                  } else {
                    multiFileCheckinUpdate(context, objectIds, store, formats, fileNames, receiptValue);
                  }

              }
              //ContextUtil.commitTransaction(context);
              return objectMap;
          }
          catch (Exception ex)
          {
              //ContextUtil.abortTransaction(context);
              throw ex;
          }
    }

    /**
    * This method is executed to update the meta date while using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the String Object Id of the Object for Update
     * @param store String to specify in which store these files need to get checked in
     * @param format String the specifies in which format the files should get checked in
                            if this is null then file will be checked in to generic format
     * @param fileName the String fileName moved to the store
     * @param append String true if these files need to get appended to the existing file
                            false if these files need to over-write existing files
     * @param unlock String true to unlock this file after checkin
                            false to hold lock on this document
     * @param receiptValue String specifies the receiptValue of the file moved to store
     * @returns String objectId in which file checkedin
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void multiFileCheckinUpdate(Context context, StringList objectIds,String store,
                              StringList formats, StringList fileNames, String receiptValue)
                throws Exception
    {
        try
        {
            DocumentUtil.checkinUpdate(context, objectIds, store, formats, fileNames, receiptValue, new StringList(), new StringList(), true);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
    * This method is executed to update the meta date while using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the String Object Id of the Object for Update
     * @param store String to specify in which store these files need to get checked in
     * @param format String the specifies in which format the files should get checked in
                            if this is null then file will be checked in to generic format
     * @param fileName the String fileName moved to the store
     * @param append String true if these files need to get appended to the existing file
                            false if these files need to over-write existing files
     * @param unlock String true to unlock this file after checkin
                            false to hold lock on this document
     * @param receiptValue String specifies the receiptValue of the file moved to store
     * @returns String objectId in which file checkedin
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
    public static void multiFileCheckinUpdate(Context context, StringList objectIds,String store,
                              StringList formats, StringList fileNames, StringList appends,
                              StringList unlocks, String receiptValue)
                throws Exception
    {
        try
        {
            if ( appends != null )
            DocumentUtil.checkinUpdate(context, objectIds, store, formats, fileNames, receiptValue, appends, unlocks, false);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    /**
     * This method is executed to create an  object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public Map multiFileCheckinCreate(Context context, String[] args) throws Exception
     {
          Map objectMap = new HashMap();

          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String strCounnt = (String) uploadParamsMap.get("noOfFiles");
          int count = new Integer(strCounnt).intValue();
          StringList objectIds = new StringList(count);
          StringList formats = new StringList(count);
          StringList fileNames = new StringList(count);
          objectMap.put("format", formats);
          objectMap.put("fileName", fileNames);
          objectMap.put("objectId", objectIds);
          String parentId       = (String) uploadParamsMap.get("parentId");
          String parentRelName  = (String) uploadParamsMap.get("parentRelName");
          if ( parentRelName != null )
          {
              parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
          }

          for( int i=0; i<count; i++ )
          {
              String format  = (String)uploadParamsMap.get("format" + i);
              String title  = (String)uploadParamsMap.get("title" + i);
              String language  = (String)uploadParamsMap.get("language" + i);
              String description  = (String)uploadParamsMap.get("description" + i);
              if ( title != null && !"".equals(title) && !"null".equals(title)) {
                  preCheckin(context, args);
                  String objectId = checkinCreate(context, null, null, null, description, title, language, parentId, parentRelName);
                  postCheckin(context, args, objectId);
                  objectIds.addElement(objectId);
                  formats.addElement(format);
                  fileNames.addElement(title);
              }
          }

          return objectMap;
    }

    /**
     * This method is executed to create a document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Sourcing 10.0.0.0
     */
     public Map mcsMultiFileCheckinCreate(Context context, String[] args) throws Exception
     {

          Map objectMap = new HashMap();

          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

          String parentId       = (String) uploadParamsMap.get("parentId");
          String parentRelName  = (String) uploadParamsMap.get("parentRelName");
          if ( parentRelName != null )
          {
              parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
          }

          String store      = (String) uploadParamsMap.get("store");
          String strCounnt = (String) uploadParamsMap.get("noOfFiles");
          int count = new Integer(strCounnt).intValue();
          StringList objectIds = new StringList(count);
          StringList formats = new StringList(count);
          StringList fileNames = new StringList(count);
          objectMap.put("format", formats);
          objectMap.put("fileName", fileNames);
          objectMap.put("objectId", objectIds);

          for( int i=0; i<count; i++ )
          {
              String format  = (String)uploadParamsMap.get("format" + i);
              String title  = (String)uploadParamsMap.get("title" + i);
              String description  = (String)uploadParamsMap.get("description" + i);
              String language  = (String)uploadParamsMap.get("language" + i);
              if (  title != null && !"".equals(title) && !"null".equals(title) ) {
                  StringList fileList = new StringList(1);
                  fileList.addElement(title);
                  preCheckin(context, args);
                  String objectId = mcsCheckinCreate(context, fileList, null, title,
                                                      description, store, defaultAppend,
                                                      format, defaultUnlock, language,
                                                      parentId, parentRelName);

                  postCheckin(context, args, objectId);
                  objectIds.addElement(objectId);
                  formats.addElement(format);
                  fileNames.addElement(title);
              }
          }

          return objectMap;
     }

    /**
     * This method is executed to revise the document object and checkin using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public Map multiFileCheckinRevise(Context context, String[] args) throws Exception
     {
          Map objectMap = new HashMap();
          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);
          String strCounnt = (String) uploadParamsMap.get("noOfFiles");
          int count = new Integer(strCounnt).intValue();
          StringList objectIds = new StringList(count);
          StringList formats = new StringList(count);
          StringList fileNames = new StringList(count);
          objectMap.put("format", formats);
          objectMap.put("fileName", fileNames);
          objectMap.put("objectId", objectIds);
          for( int i=0; i<count; i++ )
          {
              String format  = (String)uploadParamsMap.get("format" + i);
              String title  = (String)uploadParamsMap.get("title" + i);
              String reason = (String) uploadParamsMap.get("reasonForChange" + i);
              String description  = (String)uploadParamsMap.get("description" + i);
              String objectId       = (String) uploadParamsMap.get("objectId" + i);
              String language       = (String) uploadParamsMap.get("language" + i);
              if (  title != null && !"".equals(title) && !"null".equals(title) ) {
                  preCheckin(context, args);
                  objectId = checkinRevise(context, objectId, description, language, reason);
                  postCheckin(context, args, objectId);

                  objectIds.addElement(objectId);
                  formats.addElement(format);
                  fileNames.addElement(title);
              }
          }

          return objectMap;
     }

    /**
     * This method is executed to revise the document object and checkin without using FCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns String new Document Id
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     */
     public Map mcsMultiFileCheckinRevise(Context context, String[] args) throws Exception
     {

          Map objectMap = new HashMap();
          if (args == null || args.length < 1)
          {
              throw (new IllegalArgumentException());
          }
          HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);
          String store      = (String) uploadParamsMap.get("store");
          String strCounnt = (String) uploadParamsMap.get("noOfFiles");
          int count = new Integer(strCounnt).intValue();
          StringList objectIds = new StringList(count);
          StringList formats = new StringList(count);
          StringList fileNames = new StringList(count);
          objectMap.put("format", formats);
          objectMap.put("fileName", fileNames);
          objectMap.put("objectId", objectIds);
          for( int i=0; i<count; i++ )
          {
              String format  = (String)uploadParamsMap.get("format" + i);
              String title  = (String)uploadParamsMap.get("title" + i);
              String description  = (String)uploadParamsMap.get("description" + i);
              String objectId       = (String) uploadParamsMap.get("objectId" + i);
              String language       = (String) uploadParamsMap.get("language" + i);
              String reason = (String) uploadParamsMap.get("reasonForChange" + i);

              if (  title != null && !"".equals(title) && !"null".equals(title) ) {
                  StringList strFileList = new StringList(1);
                  strFileList.addElement(title);
                  Document document = (Document) DomainObject.newInstance(context,objectId);
                  preCheckin(context, args);
                  document = document.checkinRevise(context,strFileList, description,
                                                      store, defaultAppend, format,
                                                      defaultUnlock, language);
                  if ( reason != null )
                  {
                      document.setAttributeValue(context,DomainConstants.ATTRIBUTE_CHECKIN_REASON, reason);
                  }
                  postCheckin(context, args, document.getObjectId());
                  objectId = document.getObjectId();

                  objectIds.addElement(objectId);
                  formats.addElement(format);
                  fileNames.addElement(title);
              }
          }
          return objectMap;
     }

     public void preCheckin(Context context, String[] args) throws Exception
     {
        //stub need be implemented by applications specific JPOS
     }

     public void postCheckin(Context context, String[] args, String newObjectId) throws Exception
     {
        //stub need be implemented by applications specific JPOS
     }


    public Map checkinCreateVersions(Context context, String[] args) throws Exception
    {
        try
        {
            //ContextUtil.startTransaction(context, true);
            if (args == null || args.length < 1)
            {
                throw (new IllegalArgumentException());
            }
            String user = context.getUser();
            Map objectMap = new HashMap();
            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

            String objectId       = (String) uploadParamsMap.get("objectId");
            String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
            String receiptValue = (String) uploadParamsMap.get(DocumentUtil.getJobReceiptParameterName(context));
            String store      = (String) uploadParamsMap.get("store");

            String strCounnt = (String) uploadParamsMap.get("noOfFiles");
            int count = new Integer(strCounnt).intValue();
            StringList fileNames = new StringList(count);
            StringList formats = new StringList(count);
            StringList objectIds = new StringList(count);
            StringList descriptions = new StringList(count);
            StringList appends = new StringList(count);
            StringList unlocks = new StringList(count);

            DomainObject object = DomainObject.newInstance(context, objectId);
            DomainObject versionDoc = DomainObject.newInstance(context, DomainConstants.TYPE_VERSION_DOCUMENT);
            StringList selectList = new StringList(8);
            selectList.add(DomainConstants.SELECT_LOCKED);
            selectList.add(DomainConstants.SELECT_LOCKER);
            selectList.add(DomainConstants.SELECT_FILE_NAME);
            selectList.add(DomainConstants.SELECT_VAULT);

            Map objectInfo = object.getInfo(context, selectList);
            String locker = (String)objectInfo.get(DomainConstants.SELECT_LOCKER);
            String locked = (String)objectInfo.get(DomainConstants.SELECT_LOCKED);
            String vault = (String)objectInfo.get(DomainConstants.SELECT_VAULT);
            StringList fileNamesList = (StringList) objectInfo.get(DomainConstants.SELECT_FILE_NAME);
            String errorMessage = "";
            DomainObject dummyObject = DomainObject.newInstance(context, TYPE_DOCUMENT);
            boolean deleteDummy = false;

            Map formatFileMap = new HashMap();
            if( "true".equalsIgnoreCase(locked) && !locker.equals(user))
            {
                errorMessage = " Object is locked by another user ";

            } else {
                //object.lock(context);
                for( int i=0; i<count; i++ )
                {
                    String format  = (String)uploadParamsMap.get("format" + i);
                    String fileName  = (String)uploadParamsMap.get("title" + i);
                    String fileDescription = (String)uploadParamsMap.get("description" + i);
                    if (  fileName != null && !"".equals(fileName) && !"null".equals(fileName) ) {

                        if ( !fileNamesList.contains(fileName) && !fileNames.contains(fileName) )
                        {

                            StringList fileList = (StringList)formatFileMap.get(format);
                            if ( fileList == null )
                            {
                                fileList = new StringList();
                                formatFileMap.put(format, fileList);
                            }
                            fileList.add(fileName);

                            formats.addElement(format);
                            fileNames.addElement(fileName);
                            descriptions.addElement(fileDescription);
                            appends.addElement("true");
                            unlocks.addElement("false");
                            objectIds.addElement(objectId);

                        } else {
                            if ( !errorMessage.equals("") )
                            {
                                errorMessage += ", ";
                            } else if ( "true".equalsIgnoreCase(fcsEnabled) ) {
                                dummyObject.createObject(context, TYPE_DOCUMENT, null, null, null, null);
                                deleteDummy = true;
                            }
                            if ( "true".equalsIgnoreCase(fcsEnabled) )
                            {
                                formats.addElement(format);
                                fileNames.addElement(fileName);
                                objectIds.addElement(dummyObject.getObjectId());
                            }
                            errorMessage += fileName;
                        }
                    }
                }
                if( !errorMessage.equals("") )
                {
                    errorMessage += "<BR> \n" + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.Checkin.DocumentsAlreadyExists");
                }
            }

            objectMap.put("errorMessage", errorMessage);

            if ( "false".equalsIgnoreCase(fcsEnabled) ) {

                Iterator formatItr = formatFileMap.keySet().iterator();
                while( formatItr.hasNext() )
                {
                    String format = (String)formatItr.next();
                    StringList fileList = (StringList)formatFileMap.get(format);

                    object.checkinFromServer(context, false, true, format, store, fileList);
                }
            }
            createVersionDocuments(context, objectId, fileNames, descriptions, vault);
            if ( "true".equalsIgnoreCase(fcsEnabled) && objectIds.size() > 0)
            {
                multiFileCheckinUpdate(context, objectIds, store, formats, fileNames, appends, unlocks, receiptValue);
            }
            if( deleteDummy )
            {
                dummyObject.deleteObject(context,true);
            }
            //ContextUtil.commitTransaction(context);
            return objectMap;
        } catch (Exception ex) {
          ex.printStackTrace();
          throw ex;
        }


    }

    public void createVersionDocuments(Context context, String objectId, StringList fileNames, StringList descriptions, String vault) throws Exception
    {
        try
        {
            String user = context.getUser();
            DomainObject object = DomainObject.newInstance(context, objectId);
            DomainObject versionDoc = DomainObject.newInstance(context, DomainConstants.TYPE_VERSION_DOCUMENT);
            if (fileNames.size() == descriptions.size())
            {
                for (int i=0; i< descriptions.size(); i++)
                {

                    String fileDescription = (String)descriptions.get(i);
                    String fileName = (String) fileNames.get(i);
                    versionDoc.createObject(context, DomainConstants.TYPE_VERSION_DOCUMENT,
                                              getUniqueName(default_Version_Doc_Extension),
                                              FIRST_VERSION_DOCUMENT_REV,
                                              DomainConstants.POLICY_VERSION_DOCUMENT, vault);
                    HashMap attributes = new HashMap();
                    attributes.put(DomainConstants.ATTRIBUTE_CHECKIN_REASON, fileDescription);
                    attributes.put(DomainConstants.ATTRIBUTE_FILE_VERSION, FIRST_VERSION_DOCUMENT_REV);
                    attributes.put(DomainConstants.ATTRIBUTE_TITLE, fileName);
                    attributes.put(DomainConstants.ATTRIBUTE_ORIGINATOR, user);
                    versionDoc.setAttributeValues(context, attributes);
                    DomainRelationship.connect(context,
                                               object,
                                               DomainConstants.RELATIONSHIP_VERSION,
                                                 versionDoc);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }


    public String updateVersionDocument(Context context, String[] args ) throws Exception
    {
        try
        {
            if (args == null || args.length < 1)
            {
                throw (new IllegalArgumentException());
            }
            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

            String objectId       = (String) uploadParamsMap.get("objectId");
            String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
            String receiptValue = (String) uploadParamsMap.get(DocumentUtil.getJobReceiptParameterName(context));
            String format     = (String) uploadParamsMap.get("format");
            String fileName   = (String) uploadParamsMap.get("fileName");
            String store      = (String) uploadParamsMap.get("store");
            String reason      = (String) uploadParamsMap.get("reason");

            DomainObject busObject = (DomainObject) DomainObject.newInstance(context,objectId);
            //find the latest version for the file being checked in
            StringList objectSelects = new StringList();
            objectSelects.add(DomainConstants.SELECT_ID);
            objectSelects.add("attribute[" + DomainConstants.ATTRIBUTE_FILE_VERSION + "]");
            int latestVersion = 0;
            String latestVerDocId = "";

            String objWhereExpr = "attribute[" + DomainConstants.ATTRIBUTE_TITLE + "] == " + "\"" + fileName + "\"";

            short level = 1;
            MapList versionList = busObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_VERSION,
                        DomainConstants.TYPE_VERSION_DOCUMENT, objectSelects, null, false, true,
                level, objWhereExpr, "");

            for (int i=0; i<versionList.size(); i++)
            {
                Map tmpMap = (Hashtable)versionList.get(i);
                String fileVersion = (String)tmpMap.get("attribute[" + DomainConstants.ATTRIBUTE_FILE_VERSION + "]");
                int tmpVersion = Integer.parseInt(fileVersion);
                if (tmpVersion > latestVersion)
                {
                    latestVersion = tmpVersion;
                    latestVerDocId = (String)tmpMap.get(DomainConstants.SELECT_ID);
                }
            }

            //Now latestVerDocId represents the id of latest ver doc
            //move the file from BusinessObject to latest Version doc
            MqlUtil.mqlCommand(context, "modify bus $1 move from $2 format $3 file $4", latestVerDocId, objectId, format, fileName);

            //checkin the modified file to BusinessObject
            StringList strFileList = new StringList(1);
            strFileList.addElement(fileName);
            boolean unlock = false;
            boolean append = true;
            if ( "false".equalsIgnoreCase(fcsEnabled) )
            {
                 busObject.checkinFromServer(context, unlock, append, format, store, strFileList);
            }
            else
            {
                   String strAppend = "true";
                   String strUnlock = "false";
                  checkinUpdate(context, objectId, store, format, fileName, strAppend, strUnlock, receiptValue);
            }

            //create the latest Version Doc for the file and connect it to BusinessObject
            DomainObject verDocObject = new DomainObject();

            String uniqueName = verDocObject.getUniqueName("VD_");

            verDocObject.createAndConnect(context, DomainConstants.TYPE_VERSION_DOCUMENT,
                    uniqueName, DomainConstants.RELATIONSHIP_VERSION, busObject, true);

            HashMap attribMap = new HashMap();
            attribMap.put(DomainConstants.ATTRIBUTE_TITLE, fileName);
            attribMap.put(DomainConstants.ATTRIBUTE_CHECKIN_REASON, reason);
            attribMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR, context.getUser());
            attribMap.put(DomainConstants.ATTRIBUTE_FILE_VERSION, Integer.toString(latestVersion+1));
            verDocObject.setAttributeValues(context, attribMap);

            return objectId;
        } catch (Exception ex) {
            throw ex;
        }

    }
    
    public boolean hasFileCheckOutAccess(Context context,String args[]) throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
             HashMap programMap   = (HashMap) JPO.unpackArgs(args);
           String documentId = (String) programMap.get("objectId");
  
        DomainObject docObjet = DomainObject.newInstance(context,documentId);
        StringList selectTypeStmts = new StringList(1);
        selectTypeStmts.add(DomainConstants.SELECT_ID);
        selectTypeStmts.add(CommonDocument.SELECT_MASTER_SUSPEND_VERSIONING);
        selectTypeStmts.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
        selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        selectTypeStmts.add("vcmodule");
        selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
        selectTypeStmts.add(CommonDocument.SELECT_TITLE);

        Map docObjectInfo = docObjet.getInfo(context,selectTypeStmts);
        return (CommonDocument.canCheckout(context, docObjectInfo) && (Boolean.valueOf((String)docObjectInfo.get(CommonDocument.SELECT_IS_VERSION_OBJECT))).booleanValue());
    }
    
    public boolean hasFileCheckinAccess(Context context,String args[]) throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
             HashMap programMap   = (HashMap) JPO.unpackArgs(args);
           String documentId = (String) programMap.get("objectId");
        DomainObject docObjet = DomainObject.newInstance(context,documentId);
        StringList selectTypeStmts = new StringList(1);
        selectTypeStmts.add(DomainConstants.SELECT_ID);
        selectTypeStmts.add(CommonDocument.SELECT_MASTER_SUSPEND_VERSIONING);
        selectTypeStmts.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
        selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_TITLE);
        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
        selectTypeStmts.add(CommonDocument.SELECT_OWNER);

        Map docObjectInfo = docObjet.getInfo(context,selectTypeStmts);
        return (CommonDocument.canCheckin(context, docObjectInfo) && (Boolean.valueOf((String)docObjectInfo.get(CommonDocument.SELECT_IS_VERSION_OBJECT))).booleanValue());
    }
}
