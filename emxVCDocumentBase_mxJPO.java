/*
**  emxCommonDocumentBase.java
**
** $RCSfile: ${CLASS:emxCommonDocumentBase}.java $
** $Revision: 2.10 $
** $Date: Jul 12 2012 $
**
** (c) Dassault Systemes, 1993 - 2011.  All rights reserved.
**
*/


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
//import matrix.db.VcModuleInfo;
//import matrix.db.VcModuleInfoList;
//import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
//import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.VCDocument;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.StringUtils;

/**
 * The <code>emxCommonDocumentBase</code> class contains code for checkin.
 *
 * @version VCP 11.0 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxVCDocumentBase_mxJPO extends emxCommonDocument_mxJPO
{
  public static final String DSPATH = "Family_";
  public static final String FILESEPARATOR = "/";
  public static final String PROJECTNAME_SELECT = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.name";
  public static final String PROJECTSTORE_SELECT = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.vcfile[1].store";
  public static final String PROJECTPATH_SELECT = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.vcfile[1].path";
  public static final String PROJECTTYPE_SELECT = "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since VCP 11.0
     * @grade 0
     */
    public emxVCDocumentBase_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since VCP 11.0
     */
    public int mxMain(Context context, String[] args) throws Exception  {
        if (true) {
            throw new Exception(ComponentsUtil.i18nStringNow(
                            "emxComponents.Generic.MethodOnCommonFile",
                            context.getLocale().getLanguage()));
        }
        return 0;
    }


    /**
     * This is the base method executed in common Document model to checkin/update/createmaster using FCS/NonFCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns Map objectMap which contains objectId, filename, format pairs and
     *                                                  errorMessage if any error.
     * @throws Exception if the operation fails
     * @since VCP 11.0
     */
    public Map vcDocumentConnectCheckin(Context context, String[] args) throws Exception {
        try {
            ContextUtil.startTransaction(context, true);
            if (args == null || args.length < 1)
                  throw (new IllegalArgumentException());

            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);
            String objectId     = (String) uploadParamsMap.get("objectId");
            String parentId     = (String) uploadParamsMap.get("parentId");
            String objectAction = (String) uploadParamsMap.get("objectAction");
            Map objectMap = new HashMap();
            Map preCheckinMap = preCheckin(context, uploadParamsMap, parentId);
            String newParentId = (String) preCheckinMap.get("parentId");

            if (hasRealValue(newParentId)) {
                parentId = newParentId;
                uploadParamsMap.put("parentId", parentId);
            }

            // create DOCUMENTS oject with given type, name, revision and other attributes,
            // and checkin selected file/folder into DesignSync in the second step of the wizard.
            if (matchesOneOf(objectAction,
                                VCDocument.OBJECT_ACTION_CREATE_VC_ZIP_TAR_GZ,
                                VCDocument.OBJECT_ACTION_CREATE_VC_FILE_FOLDER,
                                VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND))
            {
                uploadParamsMap.put("incomplete", "true");
                objectMap = createConnect(context, uploadParamsMap);

            // create DOCUMENTs object with given type, name, revision
            // and connect it to selected DesignSync file/folder.
            } else if (matchesOneOf(objectAction,
                                VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER,
                                VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER,
                                VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND,
                                VCDocument.OBJECT_ACTION_CONNECT_VC_FROM_ISSUE_SEARCH))
            {
                uploadParamsMap.put("incomplete", "false");
                objectMap = createConnect(context, uploadParamsMap);

            // create DOCUMENTs object with given 'Type' 'name' 'revision'
            // and copy selected file/folder to local design sync file/folder.
            } else if (matchesOneOf(objectAction, VCDocument.OBJECT_ACTION_COPY_FROM_VC,
                                                  VCDocument.OBJECT_ACTION_CONVERT_COPY_FROM_VC))
            {
                uploadParamsMap.put("incomplete", "true");
                objectMap = createCopy(context, uploadParamsMap);

            // checkin file/folder into DesignSync from the given object
            } else if (matchesOneOf(objectAction, VCDocument.OBJECT_ACTION_CHECKIN_VC_FILE,
                                                  VCDocument.OBJECT_ACTION_CHECKIN_VC_FOLDER))
            {
                objectMap = checkinUpdate(context, uploadParamsMap);
            }

            // convert/checkin?
            else if (matchesOneOf(objectAction, VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER,
                                                VCDocument.OBJECT_ACTION_CONVERT_VC_FILE_FOLDER))
            {
                if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER)) {
                    uploadParamsMap.put("incomplete", "true");
                }
                objectMap = connect(context, uploadParamsMap);
            } else {
                if (hasRealValue(objectId)) {
                    objectMap = objectCheckin(context, uploadParamsMap, null);
                } else {
                    // create given type of object and checkin files in created object without version.
                    objectMap = checkinCreateWithOutVersion(context, uploadParamsMap);
                }
           }

           ContextUtil.commitTransaction(context);
           return objectMap;
        }

        catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * This is the method executed in common Document model to create master using FCS/NonFCS.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap holds all arguments passed through checkin screens.
     * @returns Map objectMap which contains objectId, filename, format pairs and
     *                                                  errorMessage if any error.
     * @throws Exception if the operation fails
     * @since VCP 11.0
     */
     public HashMap connect(Context context, HashMap uploadParamsMap) throws Exception {
        try {
            String objectId     = (String) uploadParamsMap.get("objectId");
            String objectAction = (String) uploadParamsMap.get("objectAction");
            String fileOrFolder = (String) uploadParamsMap.get("vcDocumentType");
            String path         = (String) uploadParamsMap.get("path");
            String incomplete   = (String) uploadParamsMap.get("incomplete");
            String format       = (String) uploadParamsMap.get("format");
            if (format == null) {
                format = (String) uploadParamsMap.get("defaultFormat");
            }

            String errorMessage = "";
            String isVCDocument = "FALSE";

            if (matchesOneOf(objectAction, VCDocument.OBJECT_ACTION_CONNECT_VC_FILE_FOLDER,
                            VCDocument.OBJECT_ACTION_STATE_SENSITIVE_CONNECT_VC_FILE_FOLDER)) {
                incomplete = "false";
            }

            if (hasRealValue(objectId) && objectAction.equalsIgnoreCase(
                VCDocument.OBJECT_ACTION_CONVERT_CHECKIN_VC_FILE_FOLDER))
            {
                Map<String,String> docInfo = getVCDocAccessInfo(context, objectId);
                String vcfile   = docInfo.get("vcfile");
                String vcfolder = docInfo.get("vcfolder");
                String vcmodule = docInfo.get("vcmodule");
                isVCDocument = docInfo.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);

                if ("TRUE".equalsIgnoreCase(vcfile)) {
                    MqlUtil.mqlCommand(context, "disconnect bus " +objectId+ " vcfile",   true);
                } else if ("TRUE".equalsIgnoreCase(vcfolder)) {
                    MqlUtil.mqlCommand(context, "disconnect bus " +objectId+ " vcfolder", true);
                } else if ("TRUE".equalsIgnoreCase(vcmodule)) {
                    MqlUtil.mqlCommand(context, "disconnect bus " +objectId+ " vcmodule", true);
                }
            }

            if (matchesOneOf(fileOrFolder, "folder", "module","version","branch","href")) {
                // remove the leading slash, if there is one
                if (hasRealValue(path))
                    path = path.replaceFirst("^(/|\\\\)", "");
            }

            String[] args = {(String) uploadParamsMap.get("selector")};
            String selector = processSelector(context, args);
            String store = (String) uploadParamsMap.get("server");
            if ((store == null) || store.equalsIgnoreCase("None"))
               store = "";

            StringBuffer cmdBuff = new StringBuffer(250);
            cmdBuff.append("connect bus '");
            cmdBuff.append(objectId);
            cmdBuff.append("' ");

            if (fileOrFolder == null || fileOrFolder.equalsIgnoreCase("File")) {
                cmdBuff.append("vcfile '");
            } else if (matchesOneOf(fileOrFolder, "module","version","branch")) {
                cmdBuff.append("vcmodule '");
            } else {
                cmdBuff.append("vcfolder '");
            }

            cmdBuff.append(path);

            if (fileOrFolder == null || matchesOneOf(fileOrFolder, "file","module","version","branch")) {
                cmdBuff.append("' selector '");
            } else {
                cmdBuff.append("' config '");
            }
            cmdBuff.append(selector);
            cmdBuff.append("' ");

            if ("true".equals(incomplete) && !matchesOneOf(fileOrFolder, "folder","version","module")) {
                cmdBuff.append("incomplete");
            } else if (!matchesOneOf(fileOrFolder, "module","version","branch")) {
                cmdBuff.append("complete");
            }

            cmdBuff.append(" format '");
            cmdBuff.append(format);
            cmdBuff.append("' store '");
            cmdBuff.append(store);
            cmdBuff.append("';");
            String cmd = cmdBuff.toString();
            MqlUtil.mqlCommand(context, cmd, true);
            cmdBuff = new StringBuffer(250);
            cmdBuff.append("modify bus '");
            cmdBuff.append(objectId);
            cmdBuff.append("' add interface '");
            cmdBuff.append(CommonDocument.INTERFACE_VC_DOCUMENT);
            cmdBuff.append("';");
            cmd = cmdBuff.toString();

            if (isVCDocument == null || "FALSE".equalsIgnoreCase(isVCDocument)) {
                MqlUtil.mqlCommand(context, cmd, true);
            }

            HashMap objectMap = new HashMap();
            objectMap.put("format", format);
            objectMap.put("objectId", objectId);
            return objectMap;
        }

        catch (Exception ex )
        {
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * This is the method executed in common Document model to create master using FCS/NonFCS and
     * connect to a VC file/folder.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap holds all arguments passed through checkin screens.
     * @param createVersion boolean to create version objects for master object.
     * @returns Map objectMap which contains objectId, filename, format pairs and
     *                                                  errorMessage if any error.
     * @throws Exception if the operation fails
     * @since VCP 11.0
     */
    public HashMap createConnect(Context context, HashMap uploadParamsMap) throws Exception {
        try {
            HashMap objectMap = create(context, uploadParamsMap);
            String objectId = (String) objectMap.get("objectId");
            emxNotificationUtil_mxJPO notifyUtil = new emxNotificationUtil_mxJPO(context, null);
            notifyUtil.objectNotification(context, objectId, "APPDOCUMENTSContentAddedEvent", null);
            uploadParamsMap.put("objectId", objectId);
            connect(context, uploadParamsMap);
            return objectMap;
        }

        catch (Exception ex ) {
            ex.printStackTrace();
            throw ex;
        }
    }


   /**
    * This is the method executed in common Document model to create master using FCS/NonFCS.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param uploadParamsMap holds all arguments passed through checkin screens.
    * @param createVersion boolean to create version objects for master object.
    * @returns Map objectMap which contains objectId, filename, format pairs and
    *                                                  errorMessage if any error.
    * @throws Exception if the operation fails
    * @since VCP 11.0
    */
    public HashMap create(Context context, HashMap uploadParamsMap) throws Exception {
        try {
            HashMap objectMap = new HashMap();
            String parentId = (String) uploadParamsMap.get("parentId");
            String objectId = (String) uploadParamsMap.get("objectId");
            String fcsEnabled = (String) uploadParamsMap.get("fcsEnabled");
            String parentRelName = (String) uploadParamsMap.get("parentRelName");
            String objectAction = (String) uploadParamsMap.get("objectAction");
            String vcDocument = CommonDocument.INTERFACE_VC_DOCUMENT;
            if (!UIUtil.isNullOrEmpty(parentRelName)) {
                parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
            }

            String isFrom = (String) uploadParamsMap.get("isFrom");
            String errorMessage = "";
            // Master Object Parameters
            String type = (String) uploadParamsMap.get("type");
            String name = (String) uploadParamsMap.get("name");
            String revision = (String) uploadParamsMap.get("revision");
            String policy = (String) uploadParamsMap.get("policy");
            String mDescription = (String) uploadParamsMap.get("description");
            String title = (String) uploadParamsMap.get("title");
            String accessType = (String) uploadParamsMap.get("accessType");
            String language = (String) uploadParamsMap.get("language");
            String vault = (String) uploadParamsMap.get("vault");
            String format = (String) uploadParamsMap.get("format");
            Map mAttrMap = (Map) uploadParamsMap.get("attributeMap");
            String incomplete = (String) uploadParamsMap.get("incomplete");
            String objectGeneratorRevision = (String) uploadParamsMap.get("objectGeneratorRevision");
            String owner = (String) uploadParamsMap.get("person");
            if (UIUtil.isNullOrEmpty(mDescription)) {
                mDescription = (String) uploadParamsMap.get("mDescription");
            }
            if (UIUtil.isNullOrEmpty(title)) {
                title = null;
            }

            //passing relationship name as a url parameter with out using Mapping File
            CommonDocument object = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
            DomainObject parentObject = null;
            if (!UIUtil.isNullOrEmpty(parentId)) {
                parentObject = DomainObject.newInstance(context, parentId);
            }

            if (UIUtil.isNullOrEmpty(parentId) || matchesOneOf(objectAction,
                                                    VCDocument.OBJECT_ACTION_CONNECT_VC_FROM_ISSUE_SEARCH,
                                                    VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND,
                                                    VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND))

            {
                // autoName expects symbolic name for type, policy
                String symDocumentType   = FrameworkUtil.getAliasForAdmin(context, "type", type, true);
                String symDocumentPolicy = FrameworkUtil.getAliasForAdmin(context, "policy", policy, true);

                DomainObject objectsample = new DomainObject();
                String custrev = objectsample.getDefaultRevision(context,policy);
                String docId = FrameworkUtil.autoName(context, symDocumentType, objectGeneratorRevision,
                                                  symDocumentPolicy, vault, custrev, true);
                object.setId(docId);
                object.setDescription(context, mDescription);
                mAttrMap.put("Title", title);
                object.setAttributeValues(context, mAttrMap) ;

                // IR-102978V6R2012x - connect the new doc to the w/s folder
                if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONNECT_VC_ON_DEMAND)) {
                    final String relVaultedObjects = PropertyUtil.getSchemaProperty(context,
                                                          "relationship_VaultedDocuments");
                    // are there any other cases when doc needs to be connected?
                    if (parentRelName == relVaultedObjects) {
                        DomainObject domWSFolder = DomainObject.newInstance(context, parentId);
                        domWSFolder.addRelatedObject(context,
                                                new matrix.db.RelationshipType(parentRelName),
                                                false, docId);
                    }
                }
            } else {
                object = object.createAndConnect(context, type, name, revision, policy, mDescription, vault,
                        title, language, parentObject, parentRelName, isFrom, mAttrMap, objectGeneratorRevision);
            }

            StringList selects = new StringList(2);
            selects.add(DomainConstants.SELECT_ID);
            selects.add(DomainConstants.SELECT_NAME);

            Map objectSelectMap = object.getInfo(context,selects);
            objectId = (String)objectSelectMap.get(DomainConstants.SELECT_ID);

            if (owner != null && !context.getUser().equals(owner)) {
                object.setOwner(context, owner);
            }

            objectMap.put("format", format);
            objectMap.put("objectId", objectId);
            StringList objectIdList = new StringList(1);
            objectIdList.addElement(objectId);
            postCheckin(context, uploadParamsMap, objectIdList);
            return objectMap;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }


   /**
    * This is the method executed in DesignSync Document model to modify object
    * When the previous button is selected.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param uploadParamsMap holds all arguments passed through checkin screens.
    * @returns Map objectMap which contains objectId, filename, format pairs and
    *                                                  errorMessage if any error.
    * @throws Exception if the operation fails
    * @since VCP 10.7.SP1
    */
    public HashMap modifyObject(Context context, String []args) throws Exception {
        try {
            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);
            HashMap objectMap =  new HashMap();
            String objectId = (String) uploadParamsMap.get("objectId");
            ///get the remote Store and Path and save off for the copyfromstore command
            String server = (String) uploadParamsMap.get("server");
            String path = (String) uploadParamsMap.get("path");
            String[] selectorArgs = {(String)uploadParamsMap.get("selector")};
            String selector = processSelector(context, selectorArgs);
            String fileOrFolder = (String) uploadParamsMap.get("vcDocumentType");
            String language = (String) uploadParamsMap.get("language");
            String format = (String) uploadParamsMap.get("format");
            String store = (String) uploadParamsMap.get("server");
            Map mAttrMap = (Map) uploadParamsMap.get("attributeMap");
            String policy = (String) uploadParamsMap.get("policy");
            String mDescription = (String) uploadParamsMap.get("description");
            String title = (String) uploadParamsMap.get("title");
            String name = (String) uploadParamsMap.get("name");


            if (matchesOneOf(fileOrFolder, "folder","module","version")) {
                // remove the leading slash, if there is one
                if (hasRealValue(path))
                    path = path.replaceFirst("^(/|\\\\)", "");
            }

            if ((store == null) || (store.equalsIgnoreCase("None"))) {
               store = "";
            }

            DomainObject object = new DomainObject();
            object.setId(objectId);
            StringList selects = new StringList(6);
            selects.add(DomainConstants.SELECT_TYPE);
            selects.add(DomainConstants.SELECT_NAME);
            selects.add(DomainConstants.SELECT_POLICY);
            selects.add("vcfile");
            selects.add("vcfolder");
            selects.add("vcmodule");

            Map objectSelectMap = object.getInfo(context,selects);

            String vcfile = (String) objectSelectMap.get("vcfile");
            String vcfolder = (String) objectSelectMap.get("vcfolder");
            String vcmodule = (String) objectSelectMap.get("vcmodule");
            String prevPolicy = (String) objectSelectMap.get(DomainConstants.SELECT_POLICY);
            String prevName = (String) objectSelectMap.get(DomainConstants.SELECT_NAME);

            //  if (("TRUE".equalsIgnoreCase(vcfile) && !"File".equalsIgnoreCase(fileOrFolder))
            //      ||("TRUE".equalsIgnoreCase(vcfolder) && !"Folder".equalsIgnoreCase(fileOrFolder))) {
            //
              StringBuffer disBuff = new StringBuffer(250);
              disBuff.append("disconnect bus '");
              disBuff.append(objectId);
              disBuff.append("' ");

              if("TRUE".equalsIgnoreCase(vcfile))
                  disBuff.append(" vcfile ");
              else if ("TRUE".equalsIgnoreCase(vcfolder))
                  disBuff.append(" vcfolder ");
              else if ("TRUE".equalsIgnoreCase(vcmodule))
                  disBuff.append(" vcmodule");

               disBuff.append(";");
               MqlUtil.mqlCommand(context, disBuff.toString());

               StringBuffer cmdBuff = new StringBuffer(250);
               cmdBuff.append("connect bus '");
               cmdBuff.append(objectId);
               cmdBuff.append("' ");

               if(format == null) {
                   format = (String) uploadParamsMap.get("defaultFormat");
               }

               if (fileOrFolder == null || fileOrFolder.equalsIgnoreCase("file")) {
                   cmdBuff.append("vcfile '");
               } else if (fileOrFolder.equalsIgnoreCase("folder")){
                   cmdBuff.append("vcfolder '");
               } else if (matchesOneOf(fileOrFolder, "module","version")) {
                   cmdBuff.append("vcmodule '");
               }

               cmdBuff.append(path);
               if (fileOrFolder == null || matchesOneOf(fileOrFolder, "File","Module","Version")) {
                   cmdBuff.append("' selector '");
               } else {
                   cmdBuff.append("' config '");
               }
               cmdBuff.append(selector);
               cmdBuff.append("' ");

               if (!matchesOneOf(fileOrFolder, "Folder","Module","Version")) {
                   cmdBuff.append("incomplete");
               } else if (fileOrFolder.equalsIgnoreCase("Folder")) {
                   cmdBuff.append("complete");
               }

               cmdBuff.append(" format '");
               cmdBuff.append(format);
               cmdBuff.append("' store '");
               cmdBuff.append(store);
               cmdBuff.append("';");
               String cmd = cmdBuff.toString();

               MqlUtil.mqlCommand(context, cmd);
            //
            //}
            //

            object.setDescription(context,mDescription);
            if (!"".equals(title))
                mAttrMap.put("Title", title);
            else if(name != null && !"".equals(name) && !prevName.equals(name))
                mAttrMap.put("Title", name);

            object.setAttributeValues(context, mAttrMap) ;
            if (name != null && !"".equals(name) && !prevName.equals(name)) {
                String documentNameDelimiter = EnoviaResourceBundle.getProperty(context,"emxComponents.DocumentNameDelimiter");
                name = name + documentNameDelimiter + object.getShortUniqueName(CommonDocument.EMPTY_STRING);
                object.setName(context,name) ;
            }

            if (!prevPolicy.equals(policy)) {
                object.setPolicy(context,policy);
            }
            return objectMap;
        }

        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }


   /**
    * This is the method executed in common Document model to create a copy of a remote
    * design sync file/folder. The file is copied from the specified server/path and checked in
    * to the configured server/path.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param uploadParamsMap holds all arguments passed through checkin screens.
    * @returns Map objectMap which contains objectId, filename, format pairs and
    *                                                  errorMessage if any error.
    * @throws Exception if the operation fails
    * @since VCP 11.0
    */
    public HashMap createCopy(Context context, HashMap uploadParamsMap) throws Exception {
        try {
            HashMap objectMap = new HashMap();
            String objectAction = (String) uploadParamsMap.get("objectAction");
            String objectId = null;

            //create the document object
            if (!objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CONVERT_COPY_FROM_VC))
            {
                objectMap = create(context, uploadParamsMap);
                objectId = (String) objectMap.get("objectId");
                uploadParamsMap.put("objectId", objectId);
            }
            objectId = (String) uploadParamsMap.get("objectId");

            ///get the remote Store and Path and save off for the copyfromstore command
            String remoteStore = (String) uploadParamsMap.get("server");
            String remotePath = (String) uploadParamsMap.get("path");
            String[] args = {(String) uploadParamsMap.get("selector")};
            String selector = processSelector(context, args);
            String fileOrFolder = (String) uploadParamsMap.get("vcDocumentType");
            String language = (String) uploadParamsMap.get("language");

            //get the local Store and Path and set as the store/path for connect command
            String localStore = null;
            String localPath = null;
            try
            {
                localStore = EnoviaResourceBundle.getProperty(context,"emxComponents.LocalDSServer");
                localPath = getDesignSyncDestinationPath(context, objectId);
            }
            catch (Exception ex)
            {
                throw (new FrameworkException(EnoviaResourceBundle.getProperty(context,
                		"emxComponentsStringResource", new Locale(language),"emxComponents.VersionControl.DesignSyncServerNotSet")));
            }

            //add the file name to the end of the path
            String filename = remotePath.substring(remotePath.lastIndexOf(FILESEPARATOR)+1);
            localPath += FILESEPARATOR + filename;

            //update fields in map for connect
            uploadParamsMap.put("server", localStore);
            uploadParamsMap.put("path", localPath);

            objectMap = connect(context, uploadParamsMap);

            StringBuffer cmdBuff = new StringBuffer(250);

            cmdBuff.append("modify bus '");
            cmdBuff.append(objectId);
            cmdBuff.append("' vcconnection index 1 copyfromstore '");
            cmdBuff.append(remoteStore);
            cmdBuff.append("' path '");
            cmdBuff.append(remotePath);
            if (fileOrFolder == null || matchesOneOf(fileOrFolder, "file","module","version","branch"))
            {
                cmdBuff.append("' selector '");
            } else {
                cmdBuff.append("' config '");
            }
            cmdBuff.append(selector);
            cmdBuff.append("';");

            String cmd = cmdBuff.toString();
            MqlUtil.mqlCommand(context, cmd);

            return objectMap;
        }

        catch (Exception ex )
        {
            ex.printStackTrace();
            throw ex;
        }
    }


   /**
    * Returns the local DesignSync path to store files.
    *    The path is computed based on a defined directory concatenated with the Document name.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param uploadParamsMap holds all arguments passed through checkin screens.
    * @returns String specifying the destination path.
    * @throws Exception if the operation fails
    * @since VCP 11.0
    */
    public String getDesignSyncDestinationPath(Context context, String objectId) throws Exception {
        String path = EnoviaResourceBundle.getProperty(context,"emxComponents.LocalDSPath");

        if (path == null) {
            path = "";
        }

        if (!path.isEmpty() && !path.endsWith(FILESEPARATOR)) {
            path += FILESEPARATOR;
        }

        path += DSPATH;
        DomainObject docObj = DomainObject.newInstance(context, objectId);
        path += docObj.getName(context);

        return path;
    }

   /**
    * This is the method executed in common Document model to create master using FCS/NonFCS.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param uploadParamsMap holds all arguments passed through checkin screens.
    * @param createVersion boolean to create version objects for master object.
    * @returns Map objectMap which contains objectId, filename, format pairs and
    *                                                  errorMessage if any error.
    * @throws Exception if the operation fails
    * @since VCP 11.0
    */
    public HashMap checkin(Context context, HashMap uploadParamsMap) throws Exception
    {
        HashMap objectMap = new HashMap();
        return objectMap;
    }

    public Map checkinUpdate(Context context, String[] args) throws Exception {
        try
        {
            ContextUtil.startTransaction(context, true);
            if (args == null || args.length < 1) {
                throw (new IllegalArgumentException());
            }

            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

            String objectId     = (String) uploadParamsMap.get("objectId");
            String parentId     = (String) uploadParamsMap.get("parentId");
            String objectAction = (String) uploadParamsMap.get("objectAction");

            //Added or condition to check for Checkin vc module action Jan 7 2009
            if (matchesOneOf(objectAction, VCDocument.OBJECT_ACTION_CHECKIN_VC_FILE,
                                           VCDocument.OBJECT_ACTION_CHECKIN_VC_FOLDER))
            {
                emxNotificationUtil_mxJPO notifyUtil = new emxNotificationUtil_mxJPO(context, null);
                notifyUtil.objectNotification(context, objectId, "APPDSFAContentModifiedEvent", null);
            } else if (objectAction.equalsIgnoreCase("createVCFileFolder")) {
                StringList objectIds = new StringList(1);
                objectIds.addElement(objectId);
                DSFANotification(context, uploadParamsMap, objectIds);
            }

            Map objectMap = new HashMap();
            objectMap = checkinUpdate(context, uploadParamsMap);
            ContextUtil.commitTransaction(context);
            return objectMap;
        }

        catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            throw ex;
        }
    }

   /**
    * This is the method executed in common Document model to create master using FCS/NonFCS.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param uploadParamsMap holds all arguments passed through checkin screens.
    * @param createVersion boolean to create version objects for master object.
    * @returns Map objectMap which contains objectId, filename, format pairs and
    *                                                  errorMessage if any error.
    * @throws Exception if the operation fails
    * @since VCP 11.0
    */
    public HashMap checkinUpdate(Context context, HashMap uploadParamsMap) throws Exception {
        HashMap objectMap = new HashMap();
        try {
            String objectId  = (String) uploadParamsMap.get("objectId");
            String strCounnt = (String) uploadParamsMap.get("noOfFiles");
            int count = new Integer(strCounnt).intValue();
            String objectAction   = (String) uploadParamsMap.get("objectAction");
            String vcDocumentType = (String) uploadParamsMap.get("vcDocumentType");
            String title = (String) uploadParamsMap.get("title");
            String path  = (String) uploadParamsMap.get("path");

            objectMap.put("objectId", objectId);
            CommonDocument object = (CommonDocument)DomainObject.newInstance(context, objectId);

            for (int i=0; i<count; i++) {
                String comments = (String) uploadParamsMap.get("comments" + i);
                object.setAttributeValue( context, CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
                String format = (String) uploadParamsMap.get("format" + i);
                if (title == null || "".equals(title)) {
                    if ("File".equalsIgnoreCase(vcDocumentType)) {
                        String filename = (String) uploadParamsMap.get("fileName" + i);
                        object.setAttributeValue( context, CommonDocument.ATTRIBUTE_TITLE, filename);
                    }
                    //Added to check for module document type Jan 7 2009
                    else if (matchesOneOf(vcDocumentType, "folder","module","version")) {
                        object.setAttributeValue( context, CommonDocument.ATTRIBUTE_TITLE, path);
                    }
                } else if (objectAction.equalsIgnoreCase(VCDocument.OBJECT_ACTION_CREATE_VC_ON_DEMAND)) {
                    if ("File".equalsIgnoreCase(vcDocumentType)) {
                        String filename = (String) uploadParamsMap.get("fileName" + i);
                        object.setAttributeValue( context, CommonDocument.ATTRIBUTE_TITLE, filename);
                    }
                }
            }

            String receiptValue = (String) uploadParamsMap.get(DocumentUtil.getJobReceiptParameterName(context));
            com.matrixone.fcs.mcs.CheckinEnd.doIt(context, "", receiptValue);

        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return objectMap;
    }


    public Map preCheckin(Context context, HashMap uploadParamsMap, String objectId) throws Exception {
        //stub need be implemented by applications specific JPOS
        Map preCheckinMap = new HashMap();
        return preCheckinMap;
    }

    public void postCheckin(Context context, HashMap uploadParamsMap, StringList newObjectIds) throws Exception {
        //stub need be implemented by applications specific JPOS
    }

    /**
     * This method is used to initiate subscription notifications.
     * Args Array takes objectid, event, boolean if object is a version
     * @param context the eMatrix <code>Context</code> object
     * @param args String array of parameters
     * @throws Exception if the operation fails
     * @since Common 10-0-5-0
     */
    public void handleSubscriptionEvent (matrix.db.Context context, String[] args) throws Exception {
        if (args == null || args.length == 0)
            throw new IllegalArgumentException();

        try {
            String objectId = args[0];
            String event = args[1];
            //String isVersion = args[2]; <- not used?

            if (hasRealValue(objectId)) {
                //get object info
                String selectParentId = "last.to[" + CommonDocument.RELATIONSHIP_LATEST_VERSION + "].from.id";
                StringList selects = new StringList(3);
                selects.add(DomainConstants.SELECT_ID);
                selects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
                selects.add(selectParentId);

                DomainObject parentObject = DomainObject.newInstance(context, objectId);
                Map objectSelectMap = parentObject.getInfo(context,selects);
                String objIsVersion = (String) objectSelectMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
                String objParentId  = (String) objectSelectMap.get(selectParentId);

                // version requires special handling
                if (objIsVersion.equalsIgnoreCase("true")) {
                    // no notification for Version modification
                    if ("Document Modified".equals(event))
                        return;

                    // get the parent oid to fire event on
                    objectId = objParentId;
                }

                String[] oids = new String[1];
                oids[0] = objectId;
                emxSubscriptionManager_mxJPO subMgr = new emxSubscriptionManager_mxJPO(context, oids);
                subMgr.publishEvent (context, event,objectId);
            }
        }

        catch(Exception e){
            throw e;
        }
    }

   /**
    * This is the method executed to create and connect an IC Document type with state sensitivity.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns Map objectMap which contains objectId, filename, format pairs and
    *                                                  errorMessage if any error.
    * @throws Exception if the operation fails
    * @since VCP 11.0
    */
    public Map connectStateSensitiveICDocument(Context context, String[] args) throws Exception {
        String icUrl = "";
        String icOID = "";
        HashMap objectMap = null;

        try {
            ContextUtil.startTransaction(context, true);
            if (args == null || args.length < 1) {
                throw (new IllegalArgumentException());
            }

            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

            String objectId       = (String) uploadParamsMap.get("parentId"); // Task OID
            String parentId       = (String) uploadParamsMap.get("parentId");
            String path           = (String) uploadParamsMap.get("path");
            String store          = (String) uploadParamsMap.get("server");
            String selectedState  = (String) uploadParamsMap.get("state");
            String name           = (String) uploadParamsMap.get("name");
            String icProjectName  = "";
            String icProjectStore = "";
            String icProjectPath  = "";
            String icProjectType  = "";

            if (hasRealValue(name)) {
                icProjectName = name;
            }

            if (! hasRealValue(path)) {
                path = "/";
                uploadParamsMap.put("path", path);
            }

            if (hasRealValue(parentId)) {
                DomainObject taskObject = DomainObject.newInstance(context, parentId);
                try {
                    ContextUtil.pushContext(context);
                    StringList busSelects = new StringList(4);
                    busSelects.add(PROJECTNAME_SELECT);
                    busSelects.add(PROJECTSTORE_SELECT);
                    busSelects.add(PROJECTPATH_SELECT);
                    busSelects.add(PROJECTTYPE_SELECT);

                    Map objMap = (Map)taskObject.getInfo(context, busSelects);
                    if ("".equals(icProjectName))
                        icProjectName = (String)objMap.get(PROJECTNAME_SELECT);
                    icProjectStore = (String)objMap.get(PROJECTSTORE_SELECT);
                    icProjectPath = (String)objMap.get(PROJECTPATH_SELECT);
                    icProjectType = (String)objMap.get(PROJECTTYPE_SELECT);

                    if (icProjectStore == null)
                        icProjectStore = "";
                    if (icProjectPath == null)
                        icProjectPath = "";
                    if (icProjectName == null)
                        icProjectName = "";
                }

                catch (Exception ex) {
                    throw ex;
                }

                finally {
                    ContextUtil.popContext(context);
                }

                StringBuffer DSUrl = new StringBuffer(128);

                if (store.equalsIgnoreCase("None")) {
                    // Use project store
                    uploadParamsMap.put("server", icProjectStore);

                    store = icProjectStore;
                    // Remove leading "/" from path
                    if (path.indexOf("/") == 0)
                        path = path.substring(1);
                    int pathLen = icProjectPath.length();
                    int slashIndex = icProjectPath.lastIndexOf("/");

                    if (((pathLen - 1) == slashIndex) && (slashIndex > 0))  // Remove trailing slash
                        icProjectPath = icProjectPath.substring(0, (slashIndex - 1));
                     else if ((slashIndex == 0)  && (pathLen == 1))         // Remove leading slash only if path equals slash.
                        icProjectPath = "";

                    String tmpPath = icProjectPath + "/" + path;
                    if (tmpPath.indexOf("/") == 0)
                        tmpPath = tmpPath.substring(1);
                    path = tmpPath;
                    uploadParamsMap.put("path", tmpPath);
                }

                if (!"".equals(store)) {
                    // Get the IC URL from store.
                    StringBuffer storeCmd = new StringBuffer(128);
                    storeCmd.append("print store '");
                    storeCmd.append(store);
                    storeCmd.append("' select protocol host port path dump |;" );
                    String storeData = MqlUtil.mqlCommand(context, storeCmd.toString());
                    StringTokenizer storeTok = new StringTokenizer(storeData, "|");
                    String protocol = "";
                    if (storeTok.hasMoreTokens())
                        protocol = storeTok.nextToken();    // protocol

                    if (protocol.indexOf("https") == 0)
                        protocol = "syncs";
                    else if (protocol.indexOf("http") == 0)
                        protocol = "sync";

                    DSUrl.append(protocol);                 // protocol
                    DSUrl.append("://");
                    if (storeTok.hasMoreTokens())
                        DSUrl.append(storeTok.nextToken()); // host
                    else
                        DSUrl.append("localhost");          // host

                    DSUrl.append(":");
                    if (storeTok.hasMoreTokens())
                        DSUrl.append(storeTok.nextToken()); // port
                    else
                        DSUrl.append("2647");               // port

                    if (storeTok.hasMoreTokens()) {
                        String tmpPath = storeTok.nextToken();
                        if (tmpPath.indexOf("/") != 0)
                            DSUrl.append("/");
                        if (!tmpPath.equals("/"))
                            DSUrl.append(tmpPath);          // path
                    }
                }

                if (path.indexOf("/") != 0)
                    DSUrl.append("/");

                DSUrl.append(path);
                icUrl = DSUrl.toString();

                // Set revision as -
                String revision = "-";

                // put name and revision in uploadParamsMap
                // Note: Create process will append "-uniqueid" to icProjectName i.e. ProjectName-000000000
                uploadParamsMap.put("name", icProjectName);
                uploadParamsMap.put("revision", revision);

                Map mAttrMap = (Map) uploadParamsMap.get("attributeMap");
                mAttrMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR, context.getUser());
                mAttrMap.remove(DomainConstants.ATTRIBUTE_IC_URL);
                mAttrMap.remove(DomainConstants.ATTRIBUTE_SYNC_IDENTIFIER);
                uploadParamsMap.put("attributeMap", mAttrMap);

                //set the global env value "CREATE_SYNC_ACCESS" to True
                // to avoid the invocation of "unsubscribe" web service method on creation
                PropertyUtil.setGlobalRPEValue(context, "CREATE_SYNC_ACCESS", "true");

                objectMap = createConnect(context, uploadParamsMap);
                icOID = (String)objectMap.get("objectId");
                String parentOID = (String)objectMap.get("parentId");

                String whereClause = "id == " + icOID;
                StringList relSelects = new StringList(1);
                relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
                MapList relList = taskObject.getRelatedObjects(context,
                                    DomainConstants.RELATIONSHIP_TASK_DELIVERABLE,
                                    "*",
                                    null,
                                    relSelects,
                                    false,
                                    true,
                                    (short) 1,
                                    whereClause,
                                    null);

                Map relMap = (Map)relList.get(0);
                String relId = (String)relMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);

                // Set the relationship attributes on the IC Object
                Map relAttributes = new HashMap(2);
                relAttributes.put(DomainConstants.ATTRIBUTE_COMPLETION_STATE,selectedState);
                DomainRelationship connection = DomainRelationship.newInstance(context, relId);
                connection.setAttributeValues(context, relAttributes);
                DomainObject object = (DomainObject)DomainObject.newInstance(context, icOID);
                object.setAttributeValue(context, DomainConstants.ATTRIBUTE_IC_URL, icUrl);
                if (icProjectType.equals(DomainConstants.TYPE_PROJECT_TEMPLATE)) {
                    object.setAttributeValue(context, DomainConstants.ATTRIBUTE_SYNC_IDENTIFIER, "-1");
                }
            }

            ContextUtil.commitTransaction(context);
            return objectMap;
        }

        catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            // set the global env value "BLOCK_UNSUBSCRIBE_ON_CREATE" to True
            // to avoid the invocation of "unsubscribe" web service method
            // if the IC Object is not subscribe at Sync Server.
            PropertyUtil.setGlobalRPEValue(context, "BLOCK_UNSUBSCRIBE_ON_CREATE", "true");
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
     * This method is executed after create of document object
     *  to handle subscriptions.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param uploadParamsMap HashMap
     * @param objectId String
     * @return void
     * @throws Exception if the operation fails
     * @since Team 11-0
     */
    private void DSFANotification(Context context, HashMap uploadParamsMap, StringList objectIds) throws Exception
    {
        String objectAction = (String) uploadParamsMap.get("objectAction");
        String folderId = (String) uploadParamsMap.get("folderId");
        String parentId = (String) uploadParamsMap.get("parentId");
        String folderParentType = "";
        String folderParentId = "";
        String routeParentId = "";
        String wsType = "";
        String objectId = "";
        String holdId = "";

        String wsRelType = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults");
        StringBuffer selWSId = new StringBuffer(64);
        selWSId.append("to[");
        selWSId.append(wsRelType);
        selWSId.append("].from.id");


        if (objectAction!=null && objectAction.toLowerCase().matches(".*create.*")) {
            if (! hasRealValue(folderId)) {
                if (hasRealValue(parentId)) {
                    DomainObject domainObject = DomainObject.newInstance(context);
                    domainObject.setId(parentId);
                    String rteRelType = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
                    StringBuffer selWSType = new StringBuffer(64);
                    selWSType.append("to[");
                    selWSType.append(wsRelType);
                    selWSType.append("].from.type");

                    StringBuffer selRteId = new StringBuffer(64);
                    selRteId.append("to[");
                    selRteId.append(rteRelType);
                    selRteId.append("].from.id");

                    StringList selects = new StringList(4);
                    selects.addElement(DomainObject.SELECT_TYPE);
                    selects.addElement(selWSType.toString());
                    selects.addElement(selWSId.toString());
                    selects.addElement(selRteId.toString());

                    Map objMap = (Map)domainObject.getInfo(context, selects);
                    folderParentType = (String)objMap.get(selWSType.toString());
                    folderParentId = (String)objMap.get(selWSId.toString());
                    routeParentId = (String)objMap.get(selRteId.toString());
                    String objType = (String)objMap.get(DomainObject.SELECT_TYPE);
                    String folderType = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
                    wsType = PropertyUtil.getSchemaProperty(context,"type_Project");

                    if (objType.equals(folderType)) {
                        folderId = parentId;
                    }
                }
            }

            if (hasRealValue(folderId)) {
                WorkspaceVault folder = (WorkspaceVault)DomainObject.newInstance(context, folderId);
                Workspace workspace = null;
                SubscriptionManager wsSubMgr = null;

                StringList wsSelects = new StringList(1);
                wsSelects.addElement(DomainObject.SELECT_ID);
                Map folderMap = folder.getTopLevelVault(context, wsSelects);
                String topFolderId = (String)folderMap.get(DomainObject.SELECT_ID);

                WorkspaceVault topFolder = (WorkspaceVault)DomainObject.newInstance(context, topFolderId);
                String parentVaultId = (String)topFolder.getInfo(context, selWSId.toString());
                DomainObject vault = (DomainObject)DomainObject.newInstance(context, parentVaultId);
                folderParentType = vault.getInfo(context, DomainObject.SELECT_TYPE);
                if (folderParentType.equals(wsType)) {
                    workspace = (Workspace)DomainObject.newInstance(context, parentVaultId);
                    wsSubMgr = new SubscriptionManager(workspace);
                }

                Iterator i = objectIds.iterator();
                while (i.hasNext()) {
                    objectId = (String)i.next();
                    if ((!"".equals(objectId)) && (!holdId.equals(objectId))) {
                        CommonDocument document = (CommonDocument)DomainObject.newInstance(context, objectId);
                        SubscriptionManager subscriptionMgr = new SubscriptionManager(folder);
                        subscriptionMgr.publishEvent(context, folder.EVENT_CONTENT_ADDED, objectId);
                        holdId = objectId;
                        if (wsSubMgr != null) {
                            wsSubMgr.publishEvent(context, workspace.EVENT_FOLDER_CONTENT_MODIFIED, objectId);
                        }
                    }
                }
            }

            if (routeParentId != null && (!"".equals(routeParentId))) {
                holdId = "";
                Iterator i = objectIds.iterator();
                while (i.hasNext()) {
                    objectId = (String)i.next();
                    if ((!"".equals(objectId)) && (!holdId.equals(objectId))) {
                        Route route = (Route)DomainObject.newInstance(context, routeParentId);
                        SubscriptionManager rteSubMgr = new SubscriptionManager(route);
                        rteSubMgr.publishEvent(context, route.EVENT_CONTENT_ADDED, objectId);
                        holdId = objectId;
                    }
                }
            }
        } else if (objectAction != null && "update".equals(objectAction)) {
            Iterator i = objectIds.iterator();
            while (i.hasNext()) {
                objectId = (String)i.next();
                if ((!"".equals(objectId)) && (!holdId.equals(objectId))) {
                    CommonDocument document = (CommonDocument)DomainObject.newInstance(context, objectId);
                    String docName = document.getInfo(context, DomainObject.SELECT_NAME);
                    SubscriptionManager docSubMgr = new SubscriptionManager(document);
                    docSubMgr.publishEvent(context, document.EVENT_FILE_CHECKED_IN, objectId);
                    holdId = objectId;
                }
            }
        }
    }


   /**
    * This method will process DS Selectors to ensure proper format.
    *    The selector should be Branch:Version, and will default to
    *    Branch:Latest if no Version is given
    *
    * @param selector String
    * @return String
    * @since Common 11-0
    */
    public String processSelector(Context context, String[] args) throws Exception {
        StringBuffer selectorBuf = new StringBuffer(28);
        String selector = args[0];

        if (UIUtil.isNullOrEmpty(selector)) {
            selector = "Trunk:Latest";
        }

        int index = selector.indexOf(":");
        if (index == 0) {
            selectorBuf.append("Trunk");
            selectorBuf.append(selector);
        } else {
            selectorBuf.append(selector);
        }

        if (index == (selector.length() - 1)) {
            selectorBuf.append("Latest");
        }

        // Trunk is a special case that resolves to Trunk:Latest
        if (selector.equals("Trunk")) {
            selectorBuf.append(":Latest");
        }

        StringTokenizer st = new StringTokenizer(selectorBuf.toString(), ".");
        int tokenCount = st.countTokens();
        String tok = st.nextToken();
        int remainder = tokenCount % 2;
        index = selectorBuf.toString().indexOf(":");
        String finalSelector = "";

        if ((remainder == 1) && (index < 0) && (!tok.equals(selectorBuf.toString()))) {
            finalSelector = selectorBuf.toString();
            if (finalSelector.lastIndexOf(".") == (finalSelector.length() - 1))
                finalSelector = finalSelector.substring(0, finalSelector.length() - 1);
            finalSelector = finalSelector + ":Latest";
        } else  {
            finalSelector = selectorBuf.toString();
            try
            {
                int branch = Integer.parseInt(finalSelector.trim());
                finalSelector = finalSelector + ":Latest";
            }
            catch(NumberFormatException ex)
            {
                if (finalSelector.lastIndexOf(".") == (finalSelector.length() - 1))
                    finalSelector = finalSelector.substring(0, finalSelector.length() - 1);
            }
        }

        return(finalSelector);
    }


    /**
     * This method will process DS URL data to ensure proper format.
     *
     * @param URL String
     * @return String
     * @since Common 10-7-SP1
     */
    public String processSyncUrlData(Context context, String[] args) throws Exception {
        StringBuffer urlBuf = new StringBuffer(128);
        String url = args[0];

        String processStr = "";

        if (url.indexOf("syncs://") == 0) {
            processStr = url.substring(8);
            urlBuf.append("syncs://");
        } else if (url.indexOf("sync://") == 0) {
            processStr = url.substring(7);
            urlBuf.append("sync://");
        } else {
            processStr = url;
        }

        while (processStr.indexOf("//") >= 0) {
            processStr = StringUtils.replace(processStr, "//", "/");
        }

        if ((processStr.indexOf("/") == 0) && (processStr.length() > 1))
            processStr = processStr.substring(1);

        int lengthIndex = processStr.length() - 1;
        int lastIndex = processStr.lastIndexOf("/");

        if ((lastIndex == lengthIndex) && (lastIndex != 0) && (lengthIndex > 0))
            processStr = processStr.substring(0, lengthIndex);

        urlBuf.append(processStr);

        return(urlBuf.toString());
    }

    /*
    * Gets the Checkin Status to be displayed in the form for CAD Drawing,CAD Model,Drawing Print,Mark-up
    * This is used in the display of properties page of these objects, normally called from Webform.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments:
    * 0 - String containing the object id.
    * @return String.
    * @throws Exception if the operation fails.
    * @since 10.7.
    */
    public String getVCCheckinStatus(Context context,String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        String languageStr = (String) requestMap.get("languageStr");
        String mode = (String) requestMap.get("mode");

        Map<String,String> dsfaInfo = getDSFAInfo(context, objectId);
        String vcfile   = dsfaInfo.get("vcfile");
        String vcfolder = dsfaInfo.get("vcfolder");
        String vcmodule = dsfaInfo.get("vcmodule");

        StringBuffer strBufCheckinStatus = new StringBuffer();
        String vcCheckinStatus = "";
        String vcCheckinErrorMsg = "";

        String STR_DOCUMENT_CHECKIN_STATUS_ERROR =
            ComponentsUtil.i18nStringNow("emxComponents.VCDocument.NoCheckInStatusError", languageStr);

        if ("TRUE".equalsIgnoreCase(vcfile)) {
            vcCheckinStatus = dsfaInfo.get("vcfile[1].checkinstatus");
            vcCheckinErrorMsg = dsfaInfo.get("vcfile[1].checkinerrormessage");
            if (vcCheckinErrorMsg != null && vcCheckinErrorMsg.length() > 0)
                vcCheckinErrorMsg = FrameworkUtil.encodeURL(vcCheckinErrorMsg,"UTF8");
        } else if ("TRUE".equalsIgnoreCase(vcfolder)) {
            vcCheckinStatus = dsfaInfo.get("vcfolder[1].checkinstatus");
            vcCheckinErrorMsg = dsfaInfo.get("vcfolder[1].checkinerrormessage");
            if(vcCheckinErrorMsg != null && vcCheckinErrorMsg.length() > 0)
                vcCheckinErrorMsg = FrameworkUtil.encodeURL(vcCheckinErrorMsg,"UTF8");
        } else if ("TRUE".equalsIgnoreCase(vcmodule)) {
            vcCheckinStatus = dsfaInfo.get("vcmodule[1].checkinstatus");
            vcCheckinErrorMsg = dsfaInfo.get("vcmodule[1].checkinerrormessage");
            if (vcCheckinErrorMsg != null && vcCheckinErrorMsg.length() > 0)
                vcCheckinErrorMsg = FrameworkUtil.encodeURL(vcCheckinErrorMsg,"UTF8");
        }

        if (vcCheckinStatus == null || "null".equals(vcCheckinStatus)) {
            vcCheckinStatus = "";
        }
        if (vcCheckinErrorMsg == null || "null".equals(vcCheckinErrorMsg)) {
            vcCheckinErrorMsg = "";
        }

        if ("edit".equalsIgnoreCase(mode)) {
            strBufCheckinStatus.append(vcCheckinStatus);
        } else {
            strBufCheckinStatus.append("<script type=\"text/javascript\" language=\"JavaScript\">");
            strBufCheckinStatus.append("var floatingDiv=null;");
            strBufCheckinStatus.append("var STR_CHECKIN_STATUS_ERROR=\""+STR_DOCUMENT_CHECKIN_STATUS_ERROR+"\";");
            strBufCheckinStatus.append("function doFormLoad(){");
            strBufCheckinStatus.append("var imgTag = document.getElementById(\"imgTag\");");
            strBufCheckinStatus.append(" if(imgTag!=null && imgTag!=\"undefined\"){");
            strBufCheckinStatus.append(" imgTag.addEventListener(\"click\",getCheckInErrorInfo,false); }}");
            strBufCheckinStatus.append(" </script>");
            strBufCheckinStatus.append("<script type=\"text/javascript\" language=\"JavaScript\" src=\"./scripts/emxUICore.js\"></script>");
            strBufCheckinStatus.append("<script type=\"text/javascript\" language=\"JavaScript\" src=\"../components/emxComponentsJSFunctions.js\"></script>");
            strBufCheckinStatus.append("<script type=\"text/javascript\">appendStyleSheet(\"emxUIContainedInSearch\");</script>");
            strBufCheckinStatus.append(XSSUtil.encodeForHTML(context,vcCheckinStatus));
            strBufCheckinStatus.append("<img src=\"../common/images/iconActionHelp.gif\" border=\"0\" style=\"cursor: pointer\" id=\"imgTag\" ></img>");
            strBufCheckinStatus.append("<input type=\"hidden\" name=\"CheckinErrorMessage\" value=\""+XSSUtil.encodeForHTMLAttribute(context,vcCheckinErrorMsg)+"\">");
            strBufCheckinStatus.append("<script type=\"text/javascript\" language=\"JavaScript\">doFormLoad()</script>");
        }

        return strBufCheckinStatus.toString();
    }


    /**
    * gets the vcpath Value to be displayed in the form.
    * This is used in the display of properties page of these objects, normally called from Webform.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments:
    * 0 - String containing the object id.
    * @return String.
    * @throws Exception if the operation fails.
    * @since 10.7.
    */
    public StringList getVCPathValue(Context context,String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");

        Map<String,String> dsfaInfo = getDSFAInfo(context, objectId);
        String vcfile   = dsfaInfo.get("vcfile");
        String vcfolder = dsfaInfo.get("vcfolder");
        String vcmodule = dsfaInfo.get("vcmodule");
        String vcPath = "";

        if ("TRUE".equalsIgnoreCase(vcfile)) {
            vcPath = dsfaInfo.get("vcfile[1].path");
            if (vcPath.contains("/")) {
                // remove the last path component - the filename
                vcPath = vcPath.replaceFirst("/[^/]*$", "");
            } else {
                // set path to the path separator
                vcPath = java.io.File.separator;
            }
        } else if ("TRUE".equalsIgnoreCase(vcfolder)) {
            vcPath = dsfaInfo.get("vcfolder[1].path");
        } else if ("TRUE".equalsIgnoreCase(vcmodule)) {
            vcPath = dsfaInfo.get("vcmodule[1].path");
        }

        StringList list = new StringList();
        list.add(vcPath);
        return list;
    }


   /*
    * This method is called for updating selector for CAD Drawing, CAD Model, Drawing Print, spec Objects as
    * update function in their respective Edit pages.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds a HashMap containing the following entries:
    * paramMap - a HashMap containing the following keys, "objectId", "old RDOIds", "New OID", "Old RDO Rel Ids".
    * @return Object - boolean true if the operation is successful
    * @throws Exception if operation fails
    * @since V 10.7 - Copyright (c) 2006, MatrixOne, Inc.
    */
    public Object updateSelector(Context context, String[] args) throws Exception {
        try {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            Map requestMap = (Map)programMap.get("requestMap");

            boolean isVCFolder = false;
            String newSelector = "";
            String oldSelector = "";

            Map<String,String> dsfaInfo = getDSFAInfo(context, objectId);
            String vcfile   = dsfaInfo.get("vcfile");
            String vcfolder = dsfaInfo.get("vcfolder");

            if ("TRUE".equalsIgnoreCase(vcfile)) {
                oldSelector = dsfaInfo.get("vcfile[1].specifier");
                String selector[] = (String[])requestMap.get("FileSelector");
                newSelector = selector[0];
            } else if ("TRUE".equalsIgnoreCase(vcfolder)) {
                oldSelector = dsfaInfo.get("vcfolder[1].config");
                String selector[] = (String[])requestMap.get("FolderSelector");
                newSelector = selector[0];
                isVCFolder = true;
            }

            if (oldSelector ==  null || "null".equals(oldSelector))
                oldSelector = "";

            if (!newSelector.equals(oldSelector)) {
                String branch = "";
                String version = "";
                // validate and clean up Selector
                StringTokenizer tok = new StringTokenizer(newSelector, ":");

                if (tok.hasMoreTokens())
                    branch = tok.nextToken();

                if (tok.hasMoreTokens())
                    version = tok.nextToken();

                if ((version == null) || (version.equals("")))
                    version = "Latest";

                StringBuffer selectorBuf = new StringBuffer(28);
                selectorBuf.append(branch);
                selectorBuf.append(":");
                selectorBuf.append(version);
                newSelector = selectorBuf.toString();

                StringBuffer cmd = new StringBuffer(128);
                cmd.append("modify bus \"");
                cmd.append(objectId);
                if (isVCFolder)
                    cmd.append("\" vcconnection config \"");
                else
                    cmd.append("\" vcconnection selector \"");
                cmd.append(newSelector);
                cmd.append("\"");
                MqlUtil.mqlCommand(context, cmd.toString());
            }

            return Boolean.valueOf(true);
        }

        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    //--------------------------------------------------------------------------
    // Determine whether the given vcDoc has the File Checkin Access
    //--------------------------------------------------------------------------
    public boolean hasVCFileCheckinAccess(Context context, String args[]) throws Exception {
        if (args == null || args.length < 1)
            throw (new IllegalArgumentException());

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

        Map<String,String> docInfo = getVCDocAccessInfo(context, objectId);

        return ("TRUE".equalsIgnoreCase(docInfo.get("vcfile"))
                && CommonDocument.canCheckin(context, docInfo));
    }

    //--------------------------------------------------------------------------
    // Determine whether the given vcDoc has the Folder Checkin Access
    //--------------------------------------------------------------------------
    public boolean hasVCFolderCheckinAccess(Context context, String args[]) throws Exception {
        if (args == null || args.length < 1)
            throw (new IllegalArgumentException());

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

        Map<String,String> docInfo = getVCDocAccessInfo(context, objectId);

        return ("TRUE".equalsIgnoreCase(docInfo.get("vcfolder"))
                && CommonDocument.canCheckin(context, docInfo));
    }

    //--------------------------------------------------------------------------
    // Determine whether the given vcDoc has the Module Checkin Access
    //--------------------------------------------------------------------------
    public boolean hasVCModuleCheckinAccess(Context context, String args[]) throws Exception {
        if (args == null || args.length < 1)
            throw (new IllegalArgumentException());

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

        Map<String,String> docInfo = getVCDocAccessInfo(context, objectId);

        return ("TRUE".equalsIgnoreCase(docInfo.get("vcmodule"))
                && CommonDocument.canCheckin(context, docInfo));
    }


    /**
     * This method checks if the Page is in  Design Sync Selector
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - ObjectId
     * @return boolean - isVersionable
     * @throws Exception if the operation fails
     * @since R211
     */
    public String getVCSelector(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap) programMap.get("paramMap");
        String objectId    = (String)paramMap.get("objectId");

        Map<String,String> dsfaInfo = getDSFAInfo(context, objectId);
        String vcfile   = dsfaInfo.get("vcfile");
        String vcfolder = dsfaInfo.get("vcfolder");
        String vcmodule = dsfaInfo.get("vcmodule");

        String vcSelector = "";

        if ("TRUE".equalsIgnoreCase(vcfile)) {
            vcSelector = dsfaInfo.get("vcfile[1].specifier");
        } else if ("TRUE".equalsIgnoreCase(vcfolder)) {
            vcSelector = dsfaInfo.get("vcfolder[1].config");
        } else if ("TRUE".equalsIgnoreCase(vcmodule)) {
            vcSelector = dsfaInfo.get("vcmodule[1].specifier");
        }

        return vcSelector;
    }

   /**
    * This method checks if the Page is in  Design Sync Server
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments
    *        0 - ObjectId
    * @return boolean - isVersionable
    * @throws Exception if the operation fails
    * @since R211
    */
    public String getVCServer(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap   = (HashMap) programMap.get("paramMap");
        String objectId    = (String)paramMap.get("objectId");

        Map<String,String> dsfaInfo = getDSFAInfo(context, objectId);
        String vcfile   = dsfaInfo.get("vcfile");
        String vcfolder = dsfaInfo.get("vcfolder");
        String vcmodule = dsfaInfo.get("vcmodule");

        String vcServer = "";

        if ("TRUE".equalsIgnoreCase(vcfile)) {
            vcServer = dsfaInfo.get("vcfile[1].store");
        } else if ("TRUE".equalsIgnoreCase(vcfolder)) {
            vcServer = dsfaInfo.get("vcfolder[1].store");
        } else if ("TRUE".equalsIgnoreCase(vcmodule)) {
            vcServer = dsfaInfo.get("vcmodule[1].store");
        }

        return vcServer;
    }


    //----------------------------------------------------------------
    // misc. utilities
    //----------------------------------------------------------------

   /**
    * This method returns the VCDoc DSFA connection info for the given object
    *
    * @param context the eMatrix <code>Context</code> object
    * @param objectId - the matrix Object ID
    * @return Map with the DSFA info
    * @throws Exception if the operation fails
    * @since R213
    */
    protected Map<String,String> getDSFAInfo(Context context, String objectId) throws Exception {
        StringList selectList = new StringList(18);
        selectList.add("vcfile");
        selectList.add("vcfolder");
        selectList.add("vcmodule");

        selectList.add("vcfile.store");
        selectList.add("vcfolder.store");
        selectList.add("vcmodule.store");

        selectList.add("vcfile.path");
        selectList.add("vcfolder.path");
        selectList.add("vcmodule.path");

        selectList.add("vcfile.specifier");
        selectList.add("vcfolder.config");
        selectList.add("vcmodule.specifier");

        selectList.add("vcfile.checkinstatus");
        selectList.add("vcfile.checkinerrormessage");
        selectList.add("vcfolder.checkinstatus");
        selectList.add("vcfolder.checkinerrormessage");
        selectList.add("vcmodule.checkinstatus");
        selectList.add("vcmodule.checkinerrormessage");

        DomainObject dom = new DomainObject(objectId);
        Map<String,String> dsfaMap = dom.getInfo(context, selectList);

        return dsfaMap;
    }

   /**
    * This method returns the VCDoc access info for the given object
    *
    * @param context the eMatrix <code>Context</code> object
    * @param objectId - the matrix Object ID
    * @param vctype - the vc/dsfa connection type - vcfile, vcfolder, vcmodule
    * @return Map with the info
    * @throws Exception if the operation fails
    * @since R213
    */
    protected Map<String,String> getVCDocAccessInfo(Context context, String objectId) throws Exception {
        StringList selectList = new StringList(7);
        selectList.add(DomainConstants.SELECT_ID);
        selectList.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        selectList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectList.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
        selectList.add("vcfile");
        selectList.add("vcfolder");
        selectList.add("vcmodule");

        DomainObject dom = DomainObject.newInstance(context, objectId);
        Map<String,String> vcDocInfo = dom.getInfo(context, selectList);
        return vcDocInfo;
    }


    /**
    * matchesOneOf - case insensitive string match
    * @param str - string to be matched
    * @param values - var string args to compare against
    * @returns - boolean indicating whether str matches one of the values
    */
    static boolean matchesOneOf(String str, String ... values) {
        if (str != null && values != null) {
            for (String val : values) {
                if (str.equalsIgnoreCase(val))
                    return true;
            }
        }

        return false;
    }

    /**
    * hasRealValue - this method wraps the commonly used arg. check
    * @param str - string to be checked
    * @returns - boolean indicating whether steing has a meaningful value
    */
    static boolean hasRealValue(String str) {
        if (str == null)
            return false;

        if (str.isEmpty())
            return false;

        if (str.equals("null"))
            return false;

        return true;
    }

}


