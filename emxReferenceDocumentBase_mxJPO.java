/*
 *  emxReferenceDocumentBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */
import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.fcs.mcs.*;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;

/**
 * This JPO class has some methods pertaining to Referance Document type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxReferenceDocumentBase_mxJPO extends emxProductFile_mxJPO
{
    /**
   * The <code>emxReferenceDocumentBase</code> class contains methods related to Referance Document admin type.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxReferenceDocumentBase_mxJPO (Context context, String[] args)
        throws Exception
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
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
        {
                String sContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
                throw new Exception(sContentLabel);
        }
        return 0;
    }

    /**
     * Get the list of all documents related to a BusinessObject in context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the HashMap having the following input arguments:
     *          Object ID
     *      relationship name
     * @return a MapList containing the list of the objetcs.
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public MapList getRelatedDocuments(Context context, String[] args)
        throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        String relationshipName = (String)documentMap.get("relationshipName");
        String objectId = (String)documentMap.get("objectId");
        short level = 1;
        MapList relBusObjPageList = new MapList();
        StringList objectSelects =new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_LOCKED);

        StringList relationSelects =new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        setId(objectId);
        String actualRelationshipName = PropertyUtil.getSchemaProperty(context,relationshipName);
        String strType = "";

        if(relationshipName.equals("relationship_ReferenceDocument"))
        {
            strType = ProductLineConstants.TYPE_DOCUMENT;
            relBusObjPageList = getRelatedObjects(context , actualRelationshipName, strType, objectSelects, relationSelects, false, true,level, "", "");
        }
        else
        {
            strType = ProductLineConstants.TYPE_SPECIFICATION;
            relBusObjPageList = getRelatedObjects(context , actualRelationshipName, strType, objectSelects, relationSelects, false, true, level, "", "");
        }
        return relBusObjPageList;
    }

    /**
     * Get the list of all documents.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Hashmap containing the relationship name.
     * @return a MapList containing the list of objects
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public MapList getAllDocuments(Context context, String[] args)
        throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        String relationshipName = (String)documentMap.get("relationshipName");
        MapList relBusObjPageList = new MapList();
        StringList objectSelects =new StringList(DomainConstants.SELECT_ID);
        String strType = "";
        if(relationshipName.equals("relationship_ReferenceDocument"))
        {
            strType = ProductLineConstants.TYPE_DOCUMENT;
            relBusObjPageList =  DomainObject.findObjects(context, strType, "*", null,  objectSelects);
        }
        else
        {
            strType = ProductLineConstants.TYPE_SPECIFICATION;
            relBusObjPageList =  DomainObject.findObjects(context, strType, "*", null,  objectSelects);
        }
        return relBusObjPageList;
    }

    /**
    * Get whether the business object is locked or not.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - objectList MapList
    * @return Object of type Vector
    * @throws Exception if the operation fails
    * @since ProductCentral 10-0-0-0
    **/
    public Vector getLockedStatus(Context context, String[] args)    throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)documentMap.get("objectList");

        if (relBusObjPageList==null)
        {
            String sContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.NoObjects",context.getSession().getLanguage());
            throw new Exception (sContentLabel);
        }
        int noOfObjects=relBusObjPageList.size();
        Vector statusIconTagList = new Vector(noOfObjects);
        String strLocked = null;
        String statusIconTag = null;

        for (int i = 0; i < noOfObjects; i++)
        {
            Map elementMap = (Map)relBusObjPageList.get(i);
            strLocked = (String)elementMap.get(DomainConstants.SELECT_LOCKED);

            if (strLocked.trim().equalsIgnoreCase("true"))
            {
                statusIconTag = "<img src=\"images/iconLocked.gif\" border=\"0\" >";
            }
            else
            {
                statusIconTag = "<img src=\"images/utilSpacer.gif\" border=\"0\" >";
            }
            statusIconTagList.add(statusIconTag);
        }
        return statusIconTagList;
    }

    /**
    * Gets the list of objects where the Document in context is being used.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the HashMap having the following input arguments:
    *         Object ID
    *     relationship name
    * @return Object of type MapList
    * @throws Exception if the operation fails
    * @since ProductCentral 10-0-0-0
    **/
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getWhereUsed(Context context, String[] args)
        throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        String relationshipName = (String)documentMap.get("relationshipName");
        String objectId = (String)  documentMap.get("objectId");
        short level = 1;
        String actualRelnName = PropertyUtil.getSchemaProperty(context,relationshipName)+","+PropertyUtil.getSchemaProperty(context,"relationship_FeaturesSpecification") +","+PropertyUtil.getSchemaProperty(context,"relationship_ProductSpecification") + ","+   PropertyUtil.getSchemaProperty(context,"relationship_BuildSpecification") + "," + PropertyUtil.getSchemaProperty(context,"relationship_RequirementSpecification") ;
        MapList relBusObjPageList = new MapList();
        StringList objectSelects =new StringList(DomainConstants.SELECT_ID);
        setId(objectId);
        relBusObjPageList = getRelatedObjects(context , actualRelnName, "*", objectSelects, null, true, false, level, "", "");
        return relBusObjPageList;
    }

    /**
    * Gets the list of objects where the Specifications in context is being used.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - Hashmap containing the object id
    * @return Object of type MapList
    * @throws Exception if the operation fails
    * @since ProductCentral 10-0-0-0
    **/
    public MapList getWhereUsedSpecifications(Context context, String[] args)
        throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        String relationshipName = PropertyUtil.getSchemaProperty(context,"relationship_FeaturesSpecification") + PropertyUtil.getSchemaProperty(context,"relationship_ProductSpecification") +   PropertyUtil.getSchemaProperty(context,"relationship_BuildSpecification") + PropertyUtil.getSchemaProperty(context,"relationship_RequirementSpecification") ;
        String objectId = (String)  documentMap.get("objectId");
        short level = 1;
        String actualRelnName = PropertyUtil.getSchemaProperty(context,relationshipName);
        MapList relBusObjPageList = new MapList();
        StringList objectSelects =new StringList(DomainConstants.SELECT_ID);
        setId(objectId);
        relBusObjPageList = getRelatedObjects(context , actualRelnName, "*", objectSelects, null, true, false, level, "", "");
        return relBusObjPageList;
    }
    /**
    * Get whether the business object is locked or not.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - HaspMap containing the object id.
    * @return Object of type StringList
    * @throws Exception if the operation fails
    * @since ProductCentral 10-0-0-0
    **/
    public StringList getLockStatusInText(Context context, String[] args)    throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)documentMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        StringList statusList = new StringList();
        setId(objectId);
        String strLocked = getInfo(context, DomainConstants.SELECT_LOCKED);
        statusList.add(strLocked);
        return statusList;
    }

    /**
    * Gets the user who has locked the business object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - HashMap containing the object id.
    * @return Object of type StringList
    * @throws Exception if the operation fails
    * @since ProductCentral 10-0-0-0
    **/
    public StringList getLockUser(Context context, String[] args)    throws Exception
    {
        HashMap documentMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)documentMap.get("requestMap");
        String objectId = (String)requestMap.get("objectId");
        setId(objectId);
        String strLockUser = getInfo(context, DomainConstants.SELECT_LOCKER);
        StringList statusList = new StringList();
        if (strLockUser != null)
        {
            statusList.add(strLockUser);
        }
        return statusList;
    }

    /**
    * Method to checkin the files in the document.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - HashMap containing the parentid and parent relationship name.
    * @return string containing the object id.
    * @throws Exception if the operation fails
    * @since ProductCentral 10-0-0-0
    **/
     public String checkin(Context context, String[] args) throws Exception
    {

        boolean activeTransaction = true;

        try
        {
            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

            String parentId       = (String) uploadParamsMap.get("parentId");
            String strRelationship  = (String) uploadParamsMap.get("parentRelName");
            String parentRelName = "";
            if ( strRelationship != null )
            {
                    parentRelName = PropertyUtil.getSchemaProperty(context,strRelationship);
            }

            String strUnlock  = (String) uploadParamsMap.get("unlock");
            String strAppend  = (String) uploadParamsMap.get("append");
            String format     = (String) uploadParamsMap.get("format");
            String fileName   = (String) uploadParamsMap.get("fileName");
            String store      = (String) uploadParamsMap.get("store");

            String name       = (String) uploadParamsMap.get("PRCname");
            String title      = (String) uploadParamsMap.get("title");
            String description= (String) uploadParamsMap.get("description");
            String policy   = (String) uploadParamsMap.get("PRCpolicy");
            String vault = (String) uploadParamsMap.get("PRCvault");
            String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
            String receiptValue = (String) uploadParamsMap.get(McsBase.resolveFcsParam("jobReceipt"));
            String owner = (String) uploadParamsMap.get("owner");
            String type = (String) uploadParamsMap.get("Spectype");
            String revision = (String) uploadParamsMap.get("revision");

            boolean unlock = "true".equalsIgnoreCase(strUnlock);
            boolean append = "true".equalsIgnoreCase(strAppend);

            HashMap attributeMap = new HashMap();

            attributeMap.put(ProductLineConstants.ATTRIBUTE_TITLE,title);


            ProductLineCommon prodCtrlCommon = new ProductLineCommon();
                    //Start of write transaction
            ContextUtil.startTransaction(context, true);
            String objectId = prodCtrlCommon.create(context, type, name, revision, description,
                    policy, vault, attributeMap, owner, parentId, parentRelName, true);

            setId(objectId);

            StringList strFileList = new StringList();
            strFileList.add(fileName);



            if ( "false".equalsIgnoreCase(fcsEnabled) )
            {
                    checkinFromServer(context, unlock, append, format, store, strFileList);
            }
            else
            {
                    activeTransaction = false;
                    checkinUpdate(context, objectId, store, format, fileName, strAppend, strUnlock, receiptValue);
            }
            //create the latest Version Doc for the file and connect it to BusinessObject
            DomainObject verDocObject = new DomainObject();

            String uniqueName = verDocObject.getUniqueName("VD_");

            verDocObject.createAndConnect(context, DomainConstants.TYPE_VERSION_DOCUMENT,
                            uniqueName, DomainConstants.RELATIONSHIP_VERSION, this, true);

            HashMap attribMap = new HashMap();
            attribMap.put(DomainConstants.ATTRIBUTE_TITLE, fileName);
            //attribMap.put(DomainConstants.ATTRIBUTE_CHECKIN_REASON, reason);
            attribMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR, context.getUser());
            attribMap.put(DomainConstants.ATTRIBUTE_FILE_VERSION,  "1");
            verDocObject.setAttributeValues(context, attribMap);

            if (activeTransaction) {
             ContextUtil.commitTransaction(context);
            }
            return objectId;
        } catch (Exception e) {
            if (activeTransaction) {
              //Transaction aborted in case of exception
              ContextUtil.abortTransaction(context);
            }
            //The exception with appropriate message is thrown to the caller.
            throw  new FrameworkException(e);
        }

    }
}
