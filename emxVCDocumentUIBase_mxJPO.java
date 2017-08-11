/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Store;
import matrix.db.VcFileBranchVersionInfo;
import matrix.db.VcFileInfo;
import matrix.db.VcFileInfoList;
import matrix.db.VcFileSubBranchInfo;
import matrix.db.VcFileSubBranchInfoList;
import matrix.db.VcModuleBranchInfo;
import matrix.db.VcModuleBranchInfoList;
import matrix.db.VcModuleHrefInfo;
import matrix.db.VcModuleHrefInfoList;
import matrix.db.VcModuleInfo;
import matrix.db.VcModuleInfoList;
//import matrix.db.VcModuleBranchInfo;
//import matrix.db.VcModuleBranchInfoList;
import matrix.db.VcModuleVersionInfo;
import matrix.db.VcModuleVersionInfoList;
//import matrix.db.VcModuleHrefInfo;
//import matrix.db.VcModuleHrefInfoList;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FormatUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * The <code>emxDocumentUtilBase</code> class contains utility methods for
 * getting data using configurable table APPDocumentSummary
 *
 * @version Common 10-7 - Copyright (c) 2006, MatrixOne, Inc.
 */
public class emxVCDocumentUIBase_mxJPO {

     /** Select project type from Task */
     static final String SELECT_PROJECT_TYPE_FROM_TASK =
          "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.from["
                + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";

     // This static variable indicates whether DesignSync Store is configured
     static String hasDesignSyncStore = null;

     // support for the multi-level expansion for the Structure Browser
     boolean _addExpandMultiLevelFlag = true;

     final String TYPE_mxsysDSFAHolder;

     final static String _empty = "";
     final static String _space = " ";
     final static String _none = "None";
     // internal var, controls trace
     final static boolean _debug = false;


    /**
     * Constructor
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public emxVCDocumentUIBase_mxJPO (Context context, String[] args) throws Exception {
        TYPE_mxsysDSFAHolder = PropertyUtil.getSchemaProperty(context, "type_mxsysDSFAHolder");
    }


    /**
     * This method is executed if a specific method is not specified.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return nothing
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            throw new Exception(ComponentsUtil
                .i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient",
                context.getLocale().getLanguage()));
        }

        return 0;
    }

   /**
    *  Set boolean for Design sync store entry in the Matrix server
    *  @param context the eMatrix <code>Context</code> object
    *  @return nothing, but sets static boolean variable
    *  @throws Exception if the operation fails
    *  @since Common 10.7
    *  @grade 0
    */
    private void loadDesignSyncStores(Context context) throws Exception {
        try {
        	String stores = MqlUtil.mqlCommand(context, "list $1","store" );
            StringList storeList = FrameworkUtil.split(stores, "\n");
            Iterator storeItr = storeList.iterator();
            StringList servers = new StringList(5);
            String storeType;
            String storeName;

            while(storeItr.hasNext()) {
                storeName = (String) storeItr.next();
                storeType = MqlUtil.mqlCommand(context, "print store $1 select $2 dump", storeName, "type");
                if ("designsync".equalsIgnoreCase(storeType)) {
                    servers.add(storeName);
                }
            }

            if (servers.size() > 0) {
                hasDesignSyncStore = "true";
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }


   /**
    *  returns true if design sync store is set
    *  @param context the eMatrix <code>Context</code> object
    *  @return boolean
    *  @throws Exception if the operation fails
    *  @since Common 10.7
    *  @grade 0
    */
    public boolean hasDesignSyncServer(Context context,String[] args) throws Exception
    {
        boolean bActivateDSFA = FrameworkUtil.isSuiteRegistered(context,"ActivateDSFA",false,null,null);
        if (bActivateDSFA) {
            if (hasDesignSyncStore == null) {
                loadDesignSyncStores(context);
            }
            if ("true".equals(hasDesignSyncStore)) {
                return true;
            }
        }

        return false;
    }

   /**
    *  returns true if object created doesn't have a DSFAImport tag.
    *  else returns false.
    *  @param context the eMatrix <code>Context</code> object
    *  @return boolean
    *  @throws Exception if the operation fails
    *  @since Common 10.7.SP1
    *  @grade 0
    */
    public boolean hasShowCommands(Context context,String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String fromPage = (String) programMap.get("fromPage");

        if (!fromPage.equalsIgnoreCase("Navigate")) {
            String objectId = (String) programMap.get("objectId");
            DomainObject object = DomainObject.newInstance(context, objectId);
            String strCheckintag = object.getInfo(context, "vcfolder[1].checkintag");
            if (strCheckintag != null && strCheckintag.contains("DSFAImport.")) {
                return  false;
            } else {
                return true;
            }
        }

        return true;
    }


   /**
    *
    */
    public boolean isCreateVCZipTarGzLinkEnabled(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            throw new Exception(ComponentsUtil
                    .i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient",
                    context.getLocale().getLanguage()));
        }

        if (!hasDesignSyncServer(context, args))
            return false;

        try {
            //String path = FrameworkProperties.getProperty(context,"emxComponents.LocalDSPath");
            String server = EnoviaResourceBundle.getProperty(context,"emxComponents.LocalDSServer");
            if (server.length() <= 0) {
                return false;
            }
        } catch (Exception ex) {
            // properties are not set
            return false;
        }

        return true;
    }


   /**
    *
    */
    public boolean isCopyFromVCLinkEnabled(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            throw new Exception(ComponentsUtil
                .i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient",
                context.getLocale().getLanguage()));
        }

        if (!hasDesignSyncServer(context, args))
            return false;

        try {
            //String path = EnoviaResourceBundle.getProperty(context,"emxComponents.LocalDSPath");
            String server = EnoviaResourceBundle.getProperty(context,"emxComponents.LocalDSServer");
            if (server.length() <= 0) {
                return false;
            }
        } catch (Exception ex) {
            // properties are not set
            return false;
        }

        return true;
    }

    /**
     * This method is used to get the list of files and their versions in master
     * (i.e. document holder) object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getVCFileVersions(Context context, String[] args) throws Exception {
        MapList retMapList = new MapList();

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            String globalType = "fileFolder";
            VcFileBranchVersionInfo bvInfo = null;
            VcFileInfoList fileInfoList = new VcFileInfoList();
            HashMap map = null;
            String fromPage = "";
            String vcName= "";
            String folderPath ="";
            String branchId = "";
            DomainObject object = DomainObject.newInstance(context, objectId);
            String sCheckoutAccess = "FALSE";

            if (programMap.get("fromPage")!= null) {
                sCheckoutAccess = object.getInfo(context,CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                fromPage = (String) programMap.get("fromPage");

                if (fromPage.equals("FolderContents")) {
                    vcName=(String)programMap.get("vcName") ;
                    folderPath=(String)programMap.get("folderPath") ;
                    bvInfo = object.getVcfileBranchVersionInfo (context, 1,folderPath,vcName, _empty);
                    fileInfoList =bvInfo.getBranchInfoList();
                    fromPage= "FileVersions";
                } else if (fromPage.equals("FileBranchSummary") && programMap.get("branchId")!= null) {
                    branchId=(String)programMap.get("branchId") ;
                    bvInfo =object.getVcfileBranchVersionInfo(context,1,"branchid@"+branchId);
                    fileInfoList =bvInfo.getBranchInfoList();
                } else if (fromPage.equals("FolderContentBranches") && programMap.get("branchId")!= null) {
                    branchId=(String)programMap.get("branchId") ;
                    folderPath=(String)programMap.get("folderPath") ;
                    vcName = (String)programMap.get("vcName") ;
                    bvInfo =object.getVcfileBranchVersionInfo (context, 1, folderPath, vcName, "branchid@"+branchId);
                    fileInfoList =bvInfo.getBranchInfoList();
                    fromPage = "FileVersions";
                } else if (fromPage.equals("searchVCDocuments")) {
                    // Bug 334245 - Get the Version Info when fromPage == searchVCDocuments
                    StringList listSelects = new StringList();
                    listSelects.add(CommonDocument.SELECT_VCFILE_EXISTS);
                    listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                    vcName = (String)programMap.get("vcName") ;
                    listSelects.add(vcName);
                    listSelects.add(folderPath);
                    Map mapTemp= object.getInfo(context,listSelects);
                    String exists = (String)mapTemp.get(CommonDocument.SELECT_VCFILE_EXISTS);
                    sCheckoutAccess = (String)mapTemp.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);

                    if ("TRUE".equals(exists)) {
                        bvInfo = object.getVcfileBranchVersionInfo(context, 1, _empty);
                        fileInfoList = bvInfo.getBranchInfoList();
                        fromPage = "FileBranchSummary";
                        if (folderPath.isEmpty() || folderPath.equals("/")) {
                            VcFileInfo info = (VcFileInfo) fileInfoList.getElement(0);
                            String filePath = info.getPath();
                            folderPath = filePath.substring(filePath.indexOf("/")+1, filePath.lastIndexOf("/"));
                        }
                    }
                } else {
                    StringList listSelects = new StringList();
                    listSelects.add(CommonDocument.SELECT_VCFILE_EXISTS);
                    listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                    Map mapTemp= object.getInfo(context,listSelects);

                    String exists = (String)mapTemp.get(CommonDocument.SELECT_VCFILE_EXISTS);
                    sCheckoutAccess = (String)mapTemp.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                    if ("TRUE".equals(exists)) {
                        bvInfo = object.getVcfileBranchVersionInfo(context, 1, _empty);
                        fileInfoList =bvInfo.getBranchInfoList();
                    }
                }
            }

            // get the file (Version Object) data
            StringList versiontagList = null;
            StringList associatedList = null;

            for (int idx = 0; idx < fileInfoList.size(); idx++) {
                VcFileInfo info = (VcFileInfo) fileInfoList.getElement(idx);
                versiontagList = new StringList();
                associatedList = new StringList();
                if (info.isLatest()) {
                    versiontagList.add("Latest");
                }

                versiontagList.addAll(info.getTags());

                if (_empty.equals(branchId) || (info.getVersionId().length() > branchId.length())) {
                    map = new HashMap();
                    map.put("versionId", info.getVersionId());
                    map.put("author", info.getAuthor());
                    map.put("created", info.getCreated());
                    map.put("comment", info.getComment());
                    map.put("format", info.getFormat());
                    map.put("versiontag", versiontagList);
                    map.put("fromPage", fromPage);
                    map.put("vcName", vcName);
                    map.put("name", vcName);
                    map.put("description", _empty);
                    map.put("Comments", info.getComment());
                    map.put("type", "file");

                    associatedList = info.getIds();
                    map.put("associatedId", associatedList);

                    String fixedFolderPath = (folderPath.isEmpty() ? "/" : folderPath);
                    String associate = _none;
                    if (associatedList != null && associatedList.size() > 0) {
                        associate = associatedList.get(0).toString();
                    }
                    map.put("id", joinex("file",fromPage,fixedFolderPath,vcName,objectId,info.getVersionId(),associate));

                    map.put("icon", "iconSmallFile.gif");
                    map.put("folderPath", folderPath);
                    map.put("checkoutaccess", sCheckoutAccess);
                    map.put("globalType", globalType);
                    retMapList.add(map);
                }

             }
        } catch (Exception ex) {
            if (ex.toString().indexOf("java.lang.StringIndexOutOfBoundsException") != -1) {
                String strLanguage = context.getSession().getLanguage();
                String  strError = EnoviaResourceBundle.getProperty(context,
                            "emxComponentsStringResource",
                            new Locale(strLanguage),
                            "emxComponents.VCDocument.InvalidConnection");
                emxContextUtil_mxJPO.mqlNotice(context, strError);
            } else {
                ex.printStackTrace();
                throw ex;
            }
        }

        return retMapList; //!
    }


   /**
    * getVersion - get Versions strings for File versions Summary Table
    *       Method is called to populate the Version Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector with revision values for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getVersion (Context context, String[] args) throws Exception {
        // unpack and get parameter
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)programMap.get("paramList");
        MapList objectList = (MapList)programMap.get("objectList");
        String objectId = (String) paramList.get("objectId");
        String fromPage = (String) paramList.get("fromPage");
        if (fromPage != null) {
            fromPage = fromPage.trim();
        }
        boolean isprinterFriendly = false;
        if (paramList.get("reportFormat") != null) {
            isprinterFriendly = true;
        }

        Vector versionVector = new Vector(objectList.size());
        Map objectMap = null;
        StringBuffer sBuff = new StringBuffer(256);
        StringBuffer sbNextURL = new StringBuffer(128);

        // loop through objects that are in the UI table.  populate Vector
        // with the appropriate revision value.
        for (int i = 0; i < objectList.size(); i++) {
            sBuff= new StringBuffer(256);
            sbNextURL = new StringBuffer(256);
            objectMap = (Map) objectList.get(i);
            String vcname = (String)objectMap.get("vcName");
            String versionid = (String)objectMap.get("versionId");
            String folderPath = (String)objectMap.get("folderPath");
            // set a revision level for the object.
            sbNextURL.append("../common/emxTree.jsp?objectId=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, objectId));
            sbNextURL.append("&mode=insert&jsTreeID=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, (String) paramList.get("jsTreeID")));
            sbNextURL.append("&treeMenu=type_VCBranches&treeLabel=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, versionid));
            sbNextURL.append("&AppendParameters=true");
            sbNextURL.append("&versionid=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, versionid));
            sbNextURL.append("&fromPage=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, fromPage));
            sbNextURL.append("&vcName=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, vcname));
            sbNextURL.append("&folderPath=");
            sbNextURL.append(folderPath);

            if (!isprinterFriendly) {
                sBuff.append("<a href ='");
                sBuff.append(sbNextURL.toString());
                sBuff.append(" ' class='object' target=\"content\">");
            }
            sBuff.append(XSSUtil.encodeForHTML(context, vcname)+""+XSSUtil.encodeForHTML(context, versionid));
            if (!isprinterFriendly) {
               sBuff.append("</a>");
            }
            versionVector.add(sBuff.toString());
        }

        return versionVector;
    }


  /**
    *  buildVector - Generic method for building Vector for the columns
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this methoda
    *  @return Vector with values specified by the given key
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    *  Updated for V6R2014
    */
    public Object buildVector(Context context, String[] args, String selectable) throws Exception {
        // unpack and get parameter
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector<String> returnVector = new Vector<String>(objectList.size());

        // loop through objects that are in the UI table.
        // extract the value and add it to the result vector.
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            Object obj = objectMap.get(selectable);

            if (obj == null) {
                returnVector.add(_empty);
            } else if (obj.getClass().equals(StringList.class)) {
                returnVector.add(FrameworkUtil.join((StringList)obj, ", "));
            } else {
                returnVector.add((String) obj);
            }
        }

        return returnVector;
    }


  /**
    * getOwner - Will get the Owner for File versions Summary Table
    *       Will be called in the Owner Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getOwner (Context context, String[] args) throws Exception {
        return  buildVector(context, args, "author");
    }

  /**
    * getCheckinDate - Will get the CheckIn Date for File versions Summary Table
    *       Will be called in the CheckIn Date Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getCheckinDate ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "created");
    }

   /*
    * getComments - Will get the Comments for File versions Summary Table
    *       Will be called in the Comments Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getComments ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "comment");
    }

   /*
    * getFormat - Will get the format for File versions Summary Table
    *       Will be called in the Format Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getFormat ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "format");
    }

   /*
    * getTags - extract the Module or file tags from the maps
    * Called to populate the table column.
    * @param context the eMatrix <code>Context</code> object
    * @param args - packed argument maps
    * @return Object (Vector)
    * @throws Exception if the operation fails
    * @since Common 10-7
    * @grade 0
    * Updated per IR-186375V6R2014 on 2013-01-21
    */
    public Object getTags (Context context, String[] args) throws Exception {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)programMap.get("paramList");
        MapList objectList = (MapList)programMap.get("objectList");
        Vector tagVector = new Vector(objectList.size());

        String fromPage = (String) paramList.get("fromPage");
        String level = (String) paramList.get("level");

        // ------------------------------------------------------------
        // Coming from searchVCDocuments, top-level
        // ------------------------------------------------------------
        if (level==null &&  "searchVCDocuments".equalsIgnoreCase(fromPage)) {
            for (int i = 0; i < objectList.size(); i++) {
                tagVector.add(_empty);
            }
            return tagVector;
        }


        // ------------------------------------------------------------
        // Coming from the Vault Browser (and possibly other pages)
        // Implementation changed per IR-186375V6R2014: when expanding
        // the top-level store we may have mixed nuts in objectList,
        // Modules/members and folders/files. Collect all tags.
        // ------------------------------------------------------------
        for (int i = 0; i < objectList.size(); i++) {
            Map objectMap = (Map) objectList.get(i);
            String globalType = (String)objectMap.get("globalType");
            Object tags;

            if (UIUtil.isNullOrEmpty(globalType)) {
                globalType = "fileFolder";
            }

            if (globalType.equals("module")) {
                tags = objectMap.get("tags");
            } else {
                tags = objectMap.get("versiontag");
            }

            if (tags == null) {
                tagVector.add(_empty);
            } else if (tags.getClass().equals(String.class)) {
                tagVector.add(tags);
            } else if (tags.getClass().equals(StringList.class)) {
                tagVector.add(FrameworkUtil.join((StringList) tags, ", "));
            } else {
                // this should never happen!
                tagVector.add(tags.toString());
            }

        }

        return  tagVector;
    }


   /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getVCFileVersionBranches(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            String versionid = (String) programMap.get("versionid");
            DomainObject object = DomainObject.newInstance(context, objectId);
            StringList listSelects = new StringList();
            listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            Map mapTemp= object.getInfo(context,listSelects);
            String sCheckoutAccess = (String)mapTemp.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            VcFileSubBranchInfoList  bvInfo  = null;
            String fromPage = (String)programMap.get("fromPage");
            String vcName = _empty;
            String folderPath = _empty;

            if (matchesOneOf(fromPage, "FolderContents","FolderContentBranches")) {
                vcName = (String) programMap.get("vcName") ;
                folderPath = (String) programMap.get("folderPath") ;
                bvInfo = object.getVcfileSubBranchInfo(context, 1, folderPath, vcName, "versionid@"+versionid);
                fromPage = "FolderContentBranches";
            } else {
                bvInfo = object.getVcfileSubBranchInfo(context, 1, "versionid@"+versionid);
                fromPage = "FileBranchSummary";
            }

            HashMap map = null;
            MapList retMapList = new MapList();
            StringList versiontagList = null;

            for (int idx = 0; idx < bvInfo.size(); idx++) {
                VcFileSubBranchInfo info = (VcFileSubBranchInfo) bvInfo.getElement(idx);
                map = new HashMap();
                map.put("branchId", info.getSubBranchid());
                map.put("locker", info.getLocker());
                map.put("retired", Boolean.valueOf(info.isRetired()).toString());
                map.put("VCTags", info.getTags());
                map.put("objectId", objectId);
                map.put("fromPage", fromPage);
                map.put("vcName", vcName);
                map.put("folderPath", folderPath);
                map.put("name", vcName);
                map.put("description", _empty);
                map.put("Comments", "Testing");
                map.put("comment", "Testing");
                map.put("type", "file");
                map.put("associatedId", new StringList());
                map.put("versionId", info.getSubBranchid());
                map.put("icon","iconSmallFile.gif");

                String fixedFolderPath = (folderPath.isEmpty() ? "/" : folderPath);
                map.put("id", joinex("file",fromPage,fixedFolderPath,vcName,objectId,info.getSubBranchid(),_none));

                versiontagList = new StringList();
                versiontagList.addAll(info.getTags());
                map.put("versiontag", versiontagList);
                map.put("checkoutaccess", sCheckoutAccess);
                map.put("format", "generic");

                retMapList.add(map);
            }

            return retMapList;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }


   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getBranchId( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "branchId");
    }

   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getLockStatus ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "locker");
    }

   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getRetiredStatus ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "retired");
    }

   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getBranchTags ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "VCTags");
    }

    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getVCFolderConfigurations(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            MapList retMapList = new MapList();
            HashMap map = null;
            DomainObject object = DomainObject.newInstance(context, objectId);
            StringList configList = new StringList();
            StringList listSelects = new StringList();
            listSelects.add(CommonDocument.SELECT_VCFOLDER_EXISTS);
            listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            Map mapTemp= object.getInfo(context,listSelects);
            String exists = (String)mapTemp.get(CommonDocument.SELECT_VCFOLDER_EXISTS);
            String sCheckoutAccess = (String)mapTemp.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);

            if ("TRUE".equals(exists)) {
                // need to use this for now to have the logic going used the above one.
                configList = object.getInfoList(context,"vcfolder[1].otherconfig");
            }

            for (int i=0; i< configList.size(); i++) {
                String configName = (String)configList.get(i);
                String description = object.getInfo(context, "vcfolder.otherconfig["+configName+"].description");
                map = new HashMap();
                map.put("configName", configName);
                map.put("Selector","Trunk:Latest");
                if (description != null) {
                    map.put("Description", description);
                } else {
                    map.put("Description", _space);
                }
                map.put("fromPage","FolderConfigurations");
                map.put("objectId",objectId);
                map.put("checkoutaccess", sCheckoutAccess);
                retMapList.add(map);
            }

            return retMapList;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }


   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getVCFolderConfigName( Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map)programMap.get("paramList");
        MapList objectList = (MapList)programMap.get("objectList");
        boolean isprinterFriendly = false;
        if (paramList.get("reportFormat") != null) {
            isprinterFriendly = true;
        }
        Vector branchVector = new Vector(objectList.size());
        Map objectMap = null;
        StringBuffer sBuff= new StringBuffer(256);
        StringBuffer sbNextURL= new StringBuffer(128);

        // loop through objects that are in the UI table.  populate Vector
        // with the appropriate revision value.
        for (int i = 0; i < objectList.size(); i++) {
            sBuff= new StringBuffer(256);
            sbNextURL= new StringBuffer(128);
            objectMap       = (Map) objectList.get(i);
            String objectId = (String) objectMap.get("objectId");
            String fromPage = (String) objectMap.get("fromPage");
            String configName = (String) objectMap.get("configName");
            sbNextURL.append("../common/emxTree.jsp?objectId=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, objectId));
            sbNextURL.append("&mode=insert&jsTreeID=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
            sbNextURL.append("&treeMenu=type_VCSubFolderContents&treeLabel=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, configName));
            sbNextURL.append("&configName=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, configName));
            sbNextURL.append("&AppendParameters=true");
            sbNextURL.append("&fromPage=");
            sbNextURL.append(XSSUtil.encodeForJavaScript(context, fromPage));

            if (!isprinterFriendly) {
                sBuff.append("<a href ='");
                sBuff.append(sbNextURL.toString());
                sBuff.append(" ' class='object' target=\"content\">");
            }
            sBuff.append(configName);
            if(!isprinterFriendly) {
                sBuff.append("</a>");
            }

            branchVector.add(sBuff.toString());
        }

        return  branchVector;
    }



    /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getVCFolderConfigSelector( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "Selector");
    }

   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getVCFolderConfigDescription ( Context context, String[] args) throws Exception {
        return  buildVector(context, args, "Description");
    }

   /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderConfigContentsName(Context context, String[] args) throws Exception {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)programMap.get("paramList");
        MapList objectList = (MapList)programMap.get("objectList");
        String objectId = (String) paramList.get("objectId");
        boolean isprinterFriendly = false;
        if(paramList.get("reportFormat") != null) {
            isprinterFriendly = true;
        }
        Vector nameVector = new Vector(objectList.size());
        Map objectMap = null;
        StringBuffer sBuff= new StringBuffer(256);
        StringBuffer sbNextURL= new StringBuffer(128);

        // loop through objects that are in the UI table.  populate Vector
        // with the appropriate revision value.
        for (int i = 0; i < objectList.size(); i++) {
            sBuff = new StringBuffer(256);
            sbNextURL = new StringBuffer(128);
            objectMap = (Map) objectList.get(i);
            String type = (String)objectMap.get("type");
            String vcname = (String)objectMap.get("name");
            String folderPath = (String)objectMap.get("folderPath");
            String configName = (String)objectMap.get("configName");

            // set a revision level for the object.
            if (type.equals("file")) {
                String versionId = (String)objectMap.get("versionId");
                sbNextURL.append("../common/emxTree.jsp?objectId=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, objectId));
                sbNextURL.append("&mode=insert&jsTreeID=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
                sbNextURL.append("&treeMenu=type_VCFileVersion&treeLabel=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, vcname));
                sbNextURL.append("&AppendParameters=true");
                sbNextURL.append("&vcName=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, vcname));
                sbNextURL.append("&folderPath=");
                //XSSOK
                sbNextURL.append(folderPath);
                sbNextURL.append("&fromPage=");
                sbNextURL.append("FolderContents");
                sbNextURL.append("&configName=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, configName));
            } else if (type.equals("folder")) {
                sbNextURL.append("../common/emxTree.jsp?objectId=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, objectId));
                sbNextURL.append("&mode=insert&jsTreeID=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
                sbNextURL.append("&treeMenu=type_VCSubFolderContents&treeLabel=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, vcname));
                sbNextURL.append("&AppendParameters=true");
                sbNextURL.append("&vcName=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, vcname));
                sbNextURL.append("&folderPath=");
                //XSSOK
                sbNextURL.append(folderPath);
                sbNextURL.append("&fromPage=");
                sbNextURL.append("FolderContents");
                sbNextURL.append("&configName=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, configName));
            }

            if (!isprinterFriendly) {
                sBuff.append("<a href ='");
                sBuff.append(sbNextURL.toString());
                sBuff.append(" ' class='object' target=\"content\">");
            }
            sBuff.append(vcname);
            if (!isprinterFriendly) {
                sBuff.append("</a>");
            }

            nameVector.add(sBuff.toString());
        }

        return nameVector;
    }


   /*
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common 10-7
    *  @grade 0
    */
    public Object getVCFolderConfigActions ( Context context, String[] args) throws Exception {
        Vector folderConfigActionsVector = new Vector();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String objectId = (String) paramList.get("objectId");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null) {
                isprinterFriendly = true;
            }
            String languageStr = (String) paramList.get("languageStr");
            String masterId = null;
            String versionId = null;
            String fileActions = null;
            String fileName = null;
            String folderPath = _empty;
            String fileFormat = "generic";
            boolean canViewAndDownload = true;
            String viewerURL = null;
            String downloadURL = null;
            Map objectMap = new HashMap();
            String type =_empty;
            String sTipDownload = EnoviaResourceBundle.getProperty(context,
                    "emxComponentsStringResource",
                     new Locale(languageStr),
                     "emxComponents.DocumentSummary.ToolTipDownload");
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                    context.getSession().getLanguage());

            for (int i=0; i < objectList.size(); i++) {
                StringBuffer fileActionsStrBuff = new StringBuffer();
                objectMap = (Map) objectList.get(i);
                fileName = (String) objectMap.get("configName");
                folderPath = (String)objectMap.get("folderPath");
                String sCheckoutAccess = (String)objectMap.get("checkoutaccess");
                if (!"TRUE".equalsIgnoreCase(sCheckoutAccess)) {
                    canViewAndDownload = false;
                }

                //canViewAndDownload = "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS));
                if (canViewAndDownload) {
                    if (!isprinterFriendly) {
                        if (type.equals("file")) {
                            downloadURL = "javascript:.callCheckout('"
                                + XSSUtil.encodeForJavaScript(context, objectId)
                                + "','download', '"
                                +  folderPath
                                + "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) + "');";
                            fileFormat = (String) objectMap.get("format");
                            versionId = (String) objectMap.get("versionId");
                            viewerURL = getViewerURL(context, objectId, fileFormat,folderPath, versionId);
                            fileActionsStrBuff.append(viewerURL);
                            fileActionsStrBuff.append("<a href=\"" + downloadURL + "\">");
                            fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                        + sTipDownload
                                        + "\" title=\""
                                        + sTipDownload + "\"></a>&nbsp;");
                        }
                    } else {
                        if (type.equals("file")) {
                            fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionView.gif' alt=\""
                                        + i18nViewerTip
                                        + "\" title=\""
                                        + i18nViewerTip + "\"></a>&nbsp;");
                            fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                        + sTipDownload
                                        + "\" title=\""
                                        + sTipDownload + "\"></a>&nbsp;");
                        }
                    }
                }

                fileActions = fileActionsStrBuff.toString();
                folderConfigActionsVector.add(fileActions);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return folderConfigActionsVector;
        }
    }



   /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderContentsIcon(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector iconVector = new Vector(objectList.size());
        Map objectMap = null;
        StringBuffer sBuff = new StringBuffer();

        // loop through objects that are in the UI table.  populate Vector
        // with the appropriate revision value.
        for (int i = 0; i < objectList.size(); i++) {
            sBuff = new StringBuffer(256);
            objectMap = (Map) objectList.get(i);
            String type = (String)objectMap.get("type");
            if (type.equals("file")) {
                sBuff.append("<img src='images/iconSmallFile.gif' border='0'>");
            }
            if (type.equals("folder")) {
                sBuff.append("<img src='images/iconSmallFolder.gif' border='0'>");
            }
            iconVector.add(sBuff.toString());
        }
        //XSSOK
        return  iconVector;
    }


   /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderContentsName(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)programMap.get("paramList");
        MapList objectList = (MapList)programMap.get("objectList");
        boolean isprinterFriendly = false;
        StringBuffer sBuff = new StringBuffer(256);
        StringBuffer sbNextURL = new StringBuffer(128);
        Map objectMap = null;
        String fromPage = (String) paramList.get("fromPage");
        String level = (String) paramList.get("level");
        Vector versionVector = null;

        Vector nameVector = new Vector(objectList.size());

        if (paramList.get("reportFormat") != null) {
            isprinterFriendly = true;
        }

        if (level == null && "searchVCDocuments".equalsIgnoreCase(fromPage)) {
            DomainObject object = new DomainObject();
            String name = "";
            String objectId = "";
            String vcfile = "";
            String vcfolder = "";
            String title ="";

            for (int i = 0; i < objectList.size(); i++) {
                sBuff = new StringBuffer(256);
                sbNextURL = new StringBuffer(128);
                objectMap = (Map) objectList.get(i);
                name = (String) objectMap.get(DomainConstants.SELECT_NAME);
                title = (String) objectMap.get("attribute[Title]");
                objectId = (String) objectMap.get(DomainConstants.SELECT_ID);
                vcfile = (String) objectMap.get("vcfile");
                vcfolder = (String) objectMap.get("vcfolder");
                sbNextURL.append("../common/emxTree.jsp?objectId=");
                sbNextURL.append(XSSUtil.encodeForJavaScript(context, objectId));
                if (vcfile.equalsIgnoreCase("TRUE")) {
                    sBuff.append("<img src=\"images/iconSmallFile.gif\" border=\"0\" />");
                } else if (vcfolder.equalsIgnoreCase("TRUE")) {
                    sBuff.append("<img src=\"images/iconSmallFolder.gif\"/>");
                }

                if (!isprinterFriendly) {
                    sBuff.append("<a href ='javascript:showModalDialog(\"");
                    sBuff.append(sbNextURL.toString());
                    sBuff.append("\",\"575\",\"575\",\"false\")' class='object' title='" +XSSUtil.encodeForHTMLAttribute(context, name)+ "' target=\"listHidden\">");
                }

                sBuff.append(title);
                if (!isprinterFriendly)
                    sBuff.append("</a>");

                nameVector.add(sBuff.toString());
            }

            return  nameVector;
        }

        String objectId = (String) paramList.get("objectId");
        StringList associatedId = null;

        // Loop through the objects in the UI table.
        // Populate the Vector with the appropriate revision values.
        for (int i = 0; i < objectList.size(); i++) {
            sBuff = new StringBuffer(256);
            sbNextURL = new StringBuffer(128);
            objectMap = (Map) objectList.get(i);
            String type = (String)objectMap.get("type");
            String globalType = (String) objectMap.get("globalType");

            if (UIUtil.isNullOrEmpty(globalType)) {
                globalType = "fileFolder";
            }

            if (globalType.equals("fileFolder")) {
                if (type == null)
                    type = "";
                String vcname = (String) objectMap.get("name");
                if (vcname == null)
                    vcname = "";

                String folderPath = (String) objectMap.get("folderPath");
                if (objectMap.get("associatedId") != null) {
                    associatedId = (StringList) objectMap.get("associatedId");
                } else {
                    associatedId = new StringList();
                }

                // set a revision level for the object.
                if (type.equals("file")) {
                    String versionId = (String)objectMap.get("versionId");
                    sBuff.append("<img ");
                    sBuff.append("src=\"images/iconSmallFile.gif\" ");
                    sBuff.append(" id= \"" + XSSUtil.encodeForHTMLAttribute(context, vcname)+"\"");
                    sBuff.append(" border=\"0\" />");
                    if (associatedId != null && associatedId.size() >0) {
                        sbNextURL.append("../common/emxTree.jsp?objectId=");
                        sbNextURL.append(XSSUtil.encodeForJavaScript(context, associatedId.get(0).toString()));
                        sbNextURL.append("&amp;mode=insert&amp;jsTreeID=");
                        sbNextURL.append(XSSUtil.encodeForJavaScript(context, (String) paramList.get("jsTreeID")));
                        sbNextURL.append("&amp;AppendParameters=true");
                    }
                } else if (type.equals("folder")) {
                    sBuff.append("<img src=\"images/iconSmallFolder.gif\" ");
                    sBuff.append(" id= \""+XSSUtil.encodeForHTMLAttribute(context,vcname)+"\"");
                    sBuff.append(" />");
                    if (associatedId != null && associatedId.size() >0) {
                        sbNextURL.append("../common/emxTree.jsp?objectId=");
                        sbNextURL.append(XSSUtil.encodeForJavaScript(context, associatedId.get(0).toString()));
                        sbNextURL.append("&amp;mode=insert&amp;jsTreeID=");
                        sbNextURL.append(XSSUtil.encodeForJavaScript(context, (String) paramList.get("jsTreeID")));
                        sbNextURL.append("&amp;AppendParameters=true");
                    }
                }

                if (!isprinterFriendly && associatedId.size() > 0) {
                    sBuff.append("<a ");
                    sBuff.append(" id= \""+XSSUtil.encodeForHTMLAttribute(context, vcname)+"\"");
                    sBuff.append(" href ='javascript:showModalDialog(\"");
                    sBuff.append(sbNextURL.toString());
                    sBuff.append("\",\"575\",\"575\",\"false\")' class='object' title='" + XSSUtil.encodeForHTMLAttribute(context,vcname) + "' target=\"listHidden\">");
                }

                sBuff.append(XSSUtil.encodeForHTML(context, vcname));
                if (!isprinterFriendly && associatedId.size() > 0)
                    sBuff.append("</a>");

                nameVector.add(sBuff.toString());

            } // if (globalType.equals("fileFolder"))

            if (globalType.equals("module")) {
                String name =  (String) objectMap.get("name");
                nameVector.add(name);
            }

        } // for (int i from 0 to objectList.size())

        return nameVector;
    }


    /**
     * This method is used to extract version strings from the maps
     * Called to populate the table column.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - packed argument maps
     * @return Object (Vector)
     * @throws Exception if the operation fails
     * @since Common 10-7
     * @grade 0
     * Updated per IR-186375V6R2014 on 2013-01-21
     */
    public Object getVCFolderContentsVersion(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");
            Vector versionVector = new Vector(objectList.size());

            String fromPage = (String) paramList.get("fromPage");
            String level = (String) paramList.get("level");

            // ------------------------------------------------------------
            // Coming from searchVCDocuments, top-level
            // ------------------------------------------------------------
            if (level==null && "searchVCDocuments".equalsIgnoreCase(fromPage)) {
                for (int i = 0; i < objectList.size(); i++) {
                    versionVector.add(_empty);
                }
                return versionVector;
            }

            // ------------------------------------------------------------
            // Coming from the Vault Browser (and possibly other pages)
            // Implementation changed per IR-186375V6R2014: when expanding
            // the top-level store we may have mixed nuts in objectList,
            // Modules/members and folders/files. Collect all versions.
            // ------------------------------------------------------------
            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map) objectList.get(i);
                String globalType = (String) objectMap.get("globalType");
                String version;

                if (UIUtil.isNullOrEmpty(globalType))
                    globalType="fileFolder";

                if (globalType.equals("module")) {
                    version = (String) objectMap.get("version");
                } else {
                    version = (String) objectMap.get("versionId");
                }
                versionVector.add(version==null ? _empty : version);
            }

            return versionVector;

        } catch (Exception e) {
            throw (e);
        }
    }


    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderType(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map) programMap.get("paramList");
            MapList objectList = (MapList) programMap.get("objectList");

            String fromPage = (String) paramList.get("fromPage");
            String level = (String) paramList.get("level");

            // delegate it
            if (level == null && "searchVCDocuments".equalsIgnoreCase(fromPage)) {
                return  buildVector(context, args, DomainConstants.SELECT_TYPE);
            }

            Vector typeVector = new Vector(objectList.size());
            Map objectMap = null;
            StringList associatedList = null;
            String assocatedId = "";
            DomainObject dObj = DomainObject.newInstance(context);

            for (int i = 0; i < objectList.size(); i++) {
                objectMap = (Map) objectList.get(i);
                if (objectMap.get("associatedId") != null) {
                    associatedList = (StringList)objectMap.get("associatedId");
                    if (associatedList.size() == 0) {
                        typeVector.add("");
                    } else {
                        assocatedId= associatedList.get(0).toString();
                        try {
                            dObj.setId(assocatedId);
                            typeVector.add(dObj.getInfo(context,DomainConstants.SELECT_TYPE));
                        } catch (Exception e) {
                            typeVector.add("");
                        }
                    }
                } else {
                    typeVector.add("");
               }
            }

            return typeVector;

        } catch (Exception e) {
            throw (e);
        }
    }

    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public boolean isNewWindowShown(Context context, String[] args) throws Exception {
        try {
            HashMap programMap  = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map) programMap.get("paramList");
            String fromPage = (String) paramList.get("fromPage");
            String level = (String) paramList.get("level");

            if (level == null && "searchVCDocuments".equalsIgnoreCase(fromPage)) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            throw (e);
        }
    }


    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderContentsComments(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map objectMap = null;
            String globalType = "";

            for (int i = 0; i < objectList.size(); i++) {
                objectMap = (Map) objectList.get(i);
                globalType = (String)objectMap.get("globalType");

                if (UIUtil.isNullOrEmpty(globalType))
                    globalType = "fileFolder";

                if (globalType.equals("module"))
                    return getVCModuleComments(context, JPO.packArgs(programMap));
            }

            return  buildVector(context, args, "Comments");

        } catch (Exception e) {
            throw (e);
        }
    }


    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderConfigContentActions(Context context, String[] args) throws Exception {
        Vector folderActionsVector = new Vector();

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String objectId = (String) paramList.get("objectId");
            boolean isprinterFriendly = false;
            DomainObject doObj = DomainObject.newInstance(context,objectId);

            if (paramList.get("reportFormat") != null) {
                isprinterFriendly = true;
            }

            String languageStr = (String) paramList.get("languageStr");
            String masterId = null;
            String fileActions = null;
            String fileName = null;
            String folderPath = "";
            String fileFormat = "generic";
            boolean canViewAndDownload = true;
            String viewerURL = null;
            String downloadURL = null;
            Map objectMap = new HashMap();
            String type ="";
            String versionId ="";
            String sTipDownload = EnoviaResourceBundle.getProperty(context,
                    "emxComponentsStringResource",
                    new Locale(languageStr),
                    "emxComponents.DocumentSummary.ToolTipDownload");
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                    context.getSession().getLanguage());

            for (int i=0; i < objectList.size(); i++) {
                StringBuffer fileActionsStrBuff = new StringBuffer();
                objectMap = (Map) objectList.get(i);
                fileName = (String) objectMap.get("name");
                versionId = (String) objectMap.get("versionId");
                folderPath = (String)objectMap.get("folderPath");
                String sCheckoutAccess = doObj.getInfo(context,CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                if (!"TRUE".equalsIgnoreCase(sCheckoutAccess)) {
                    canViewAndDownload = false;
                }

                type = (String) objectMap.get("type");
                if (type.equals("file")) {
                    folderPath =  folderPath+"/"+XSSUtil.encodeForJavaScript(context, fileName);
                }

                if (canViewAndDownload) {
                    if (!isprinterFriendly) {
                        if (type.equals("file")) {
                            fileFormat = (String) objectMap.get("format");
                            versionId = (String) objectMap.get("versionId");
                            downloadURL = "javascript:callCheckout('"
                                  + XSSUtil.encodeForJavaScript(context, objectId)
                                  + "','download', '"
                                  + folderPath
                                  + "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionId)  +"');";

                            viewerURL = getViewerURL(context, objectId, fileFormat,folderPath,versionId);
                            fileActionsStrBuff.append(viewerURL);
                            fileActionsStrBuff.append("<a href=\"" + downloadURL + "\">");
                            fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                        + sTipDownload
                                        + "\" title=\""
                                        + sTipDownload + "\"></a>&nbsp;");
                        }
                    } else {
                        if (type.equals("file")) {
                           fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionView.gif' alt=\""
                                        + i18nViewerTip
                                        + "\" title=\""
                                        + i18nViewerTip + "\"></a>&nbsp;");
                           fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                        + sTipDownload
                                        + "\" title=\""
                                        + sTipDownload + "\"></a>&nbsp;");
                        }
                    }
                }
                fileActions = fileActionsStrBuff.toString();
                folderActionsVector.add(fileActions);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return folderActionsVector;
        }
    }


    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object getVCFolderContentActions(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector folderActionsVector = new Vector(objectList.size());
        String globalType = "";

        try {
            Map paramList = (Map) programMap.get("paramList");
            String objectId = (String) paramList.get("objectId");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null) {
                isprinterFriendly = true;
            }
            String languageStr = (String) paramList.get("languageStr");
            String masterId = null;
            String fileActions = null;
            String fileName = null;
            String folderPath = "";
            String fileFormat = "generic";
            boolean canViewAndDownload = true;
            String viewerURL = null;
            String downloadURL = null;
            Map objectMap = new HashMap();
            String type ="";
            String versionId ="";
            String sTipDownload = EnoviaResourceBundle.getProperty(context,
                            "emxComponentsStringResource",
                            new Locale(languageStr),
                            "emxComponents.DocumentSummary.ToolTipDownload");
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                                    context.getSession().getLanguage());

            for (int i = 0; i < objectList.size(); i++) {
                objectMap = (Map) objectList.get(i);
                globalType = (String)objectMap.get("globalType");

                if (UIUtil.isNullOrEmpty(globalType)) {
                    globalType="fileFolder";
                }

                if (globalType.equals("fileFolder")) {
                    boolean showFileVersions = true;
                    StringBuffer fileActionsStrBuff = new StringBuffer();
                    objectMap = (Map) objectList.get(i);
                    fileName = (String) objectMap.get("name");
                    versionId = (String) objectMap.get("versionId");
                    DomainObject doObj = DomainObject.newInstance(context,objectId);
                    folderPath = (String)objectMap.get("folderPath");
                    //Commented and modified for bug 351441 start
                    //String sCheckoutAccess = (String)objectMap.get("checkoutaccess");
                    String sCheckoutAccess = doObj.getInfo(context,CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                    //351441 ends
                    if(!"TRUE".equalsIgnoreCase(sCheckoutAccess)) {
                        canViewAndDownload = false;
                    }

                    type = (String) objectMap.get("type");
                    // IR-124966V6R2012x: perhaps not the best,
                    // but the easiest and the least intrusive fix
                    if (type == null)
                        type = "";
                    //

                    if (objectMap.get("showFileVersions") != null)
                        showFileVersions = ((Boolean) objectMap.get("showFileVersions")).booleanValue();

                    // IR-260227V6R2014x
                    if (type.equals("file")) {
                        if (UIUtil.isNullOrEmpty(folderPath)) {
                            folderPath = XSSUtil.encodeForHTMLAttribute(context, fileName);
                        } else {
                            folderPath = XSSUtil.encodeForHTMLAttribute(context, folderPath) +"/"+  XSSUtil.encodeForHTMLAttribute(context, fileName);
                        }
                    }
                    //


                    if (canViewAndDownload) {
                        if (!isprinterFriendly) {
                            if (type.equals("file") && showFileVersions) {
                                fileFormat = (String) objectMap.get("format");
                                versionId = (String) objectMap.get("versionId");
                                downloadURL = "javascript:callCheckout('"
                                    + XSSUtil.encodeForJavaScript(context, objectId)
                                    + "','download', '"
                                    + folderPath
                                    + "', '" + XSSUtil.encodeForJavaScript(context, fileFormat)
                                    + "', null, null, null, null, null, '"
                                    + XSSUtil.encodeForJavaScript(context, versionId) +"');";

                                viewerURL = getVCFolderViewerURL(context,
                                            objectId, fileFormat,folderPath,versionId);
                                fileActionsStrBuff.append(viewerURL);
                                fileActionsStrBuff.append("<a href=\"" + downloadURL + "\">");

                                fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                        + sTipDownload
                                        + "\" title=\""
                                        + sTipDownload + "\"></img></a>&#160;");
                            }
                        } else {
                            if (type.equals("file") && showFileVersions) {
                                fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionView.gif' alt=\""
                                        + i18nViewerTip
                                        + "\" title=\""
                                        + i18nViewerTip + "\"></img></a>");
                                fileActionsStrBuff.append(
                                        "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                        + sTipDownload
                                        + "\" title=\""
                                        + sTipDownload + "\"></img></a>&#160;");
                            }
                        }
                    }

                    if (showFileVersions) {
                        fileActions = fileActionsStrBuff.toString();
                        folderActionsVector.add(fileActions);
                    } else {
                        folderActionsVector.add("");
                    }
                }

                if (globalType.equals("module")) {
                    String actions =  (String) objectMap.get("actions");
                    folderActionsVector.add(actions);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return folderActionsVector;
        }
    }


    /**
     * This method is used to get the viewer URL for all viewers for given
     * format
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param objectId
     *            the objectid from which file need to be checked out
     * @param format
     *            the format from which file need to be checked out
     * @param fileName
     *            the fileName to be checked out
     * @return String URL for all viewers
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public static String getVCFolderViewerURL(Context context, String objectId,
                    String format, String fileName, String versionid) throws Exception {
        try {
            Map formatViewerMap = FormatUtil.getViewerCache(context);
            String returnURL = "";
            String URLParameters = "?action=view&id=" + objectId + "&objectId="
                    + objectId + "&format=" + format + "&file=" + fileName
                    + "&fileName=" + fileName;

            Map formatDetailsMap = (Map) formatViewerMap.get(format);
            if (formatDetailsMap == null) {
                FormatUtil.loadViewerCache(context);
            }

            formatDetailsMap = (Map) formatViewerMap.get(format);
            String viewerURL = "";
            String servletPreFix = EnoviaResourceBundle
                    .getProperty(context,"emxFramework.Viewer.ServletPreFix");
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                                    context.getSession().getLanguage());
            String servletURL = "";
            StringBuffer fileViewerURL = new StringBuffer(256);
            String aliasFormat = FrameworkUtil.getAliasForAdmin(context, "format", format, true);
            FormatUtil formatUtil = new FormatUtil(aliasFormat);
            String viewer = formatUtil.getViewerPreference(context, null);

            if (formatDetailsMap == null) {
                viewerURL = "javascript:callCheckout('"
                        + XSSUtil.encodeForJavaScript(context, objectId)
                        + "','view', '"
                        + XSSUtil.encodeForJavaScript(context, fileName)
                        + "', '"
                        + XSSUtil.encodeForJavaScript(context, format)
                        + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                fileViewerURL.append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                fileViewerURL.append(i18nViewerTip);
                fileViewerURL.append("\" title=\"");
                fileViewerURL.append(i18nViewerTip);
                fileViewerURL.append("\"></img></a>&#160;");
                returnURL = fileViewerURL.toString();
            } else {
                java.util.Set set = formatDetailsMap.keySet();
                Iterator itr = set.iterator();
                boolean needDefaultViewer = false;
                while (itr.hasNext()) {
                    viewerURL = (String) itr.next();
                    if (viewer == null || "".equals(viewer) || "null".equals(viewer) || viewerURL.equals(viewer)) {
                        needDefaultViewer = true;
                        viewerTip = ((String) formatDetailsMap.get(viewerURL));
                        i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                                                context.getSession().getLanguage());

                        if (viewerTip.equalsIgnoreCase("Default")) {
                            viewerURL = "javascript:callCheckout('"
                                    + XSSUtil.encodeForJavaScript(context, objectId)
                                    + "','view', '"
                                    + XSSUtil.encodeForJavaScript(context, fileName)
                                    + "', '" + XSSUtil.encodeForJavaScript(context, format) + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                            fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                            fileViewerURL
                                    .append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\" title=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\"></img></a>&#160;");
                        } else {
                            //viewerURL = servletPreFix + viewerURL + "?action=view&";
                            viewerURL = "javascript:callViewer('"
                                    + XSSUtil.encodeForJavaScript(context, objectId)
                                    + "','view', '"
                                    + XSSUtil.encodeForJavaScript(context, fileName)
                                    + "', '"
                                    + XSSUtil.encodeForJavaScript(context, format)
                                    + "', '"
                                    + viewerURL
                                    + "', null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                            fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                            fileViewerURL
                                    .append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\" title=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\"></img></a>&#160;");
                        }
                    }
                }

                if (!needDefaultViewer) {
                    viewerURL = "javascript:callCheckout('"
                            + XSSUtil.encodeForJavaScript(context, objectId)
                            + "','view', '"
                            +  XSSUtil.encodeForJavaScript(context, fileName)
                            + "', '"
                            + XSSUtil.encodeForJavaScript(context, format) + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                    fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                    fileViewerURL
                            .append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                    fileViewerURL.append(i18nViewerTip);
                    fileViewerURL.append("\" title=\"");
                    fileViewerURL.append(i18nViewerTip);
                    fileViewerURL.append("\"></img></a>&#160;");
                }
                returnURL = fileViewerURL.toString();
            }

            return returnURL;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * gets the list of connected DOCUMENTS to the master Object Used for
     * APPDocumentSummary table
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectId - parent
     *            object OID
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public static Vector getVCFileVersionActions(Context context, String[] args) throws Exception {
        Vector fileActionsVector = new Vector();

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String objectId = (String) paramList.get("objectId");
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null) {
                isprinterFriendly = true;
            }
            String languageStr = (String) paramList.get("languageStr");
            String versionId = null;
            String fileActions = null;
            String fileName = null;
            String fileFormat = "generic";
            boolean canViewAndDownload = true;
            String viewerURL = null;
            String downloadURL = null;
            Map objectMap = new HashMap();
            String sTipDownload = EnoviaResourceBundle.getProperty(context,
                        "emxComponentsStringResource",
                        new Locale(languageStr),
                        "emxComponents.DocumentSummary.ToolTipDownload");

            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                                    context.getSession().getLanguage());

            for (int i=0; i < objectList.size(); i++) {
                StringBuffer fileActionsStrBuff = new StringBuffer();
                objectMap = (Map) objectList.get(i);
                fileName = (String)objectMap.get("vcName");
                String folderPath = (String)objectMap.get("folderPath");
                String versionid = (String)objectMap.get("versionId");
                fileFormat = (String) objectMap.get("format");
                versionId = (String) objectMap.get("versionId");
                String sCheckoutAccess = (String)objectMap.get("checkoutaccess");
                if(!"TRUE".equalsIgnoreCase(sCheckoutAccess)) {
                    canViewAndDownload = false;
                }

                if (folderPath != null && !"null".equals(folderPath)) {
                    fileName = folderPath + "/" + fileName;
                }

                if (canViewAndDownload) {
                    if (!isprinterFriendly) {
                     downloadURL = "javascript:callCheckout('"
                                + objectId
                                + "','download', '"
                                + fileName
                                + "', '" + fileFormat + "', null, null, null, null, null, '"+ versionid +"');";
                        viewerURL = getViewerURL(context, objectId, fileFormat,fileName, versionid);
                        fileActionsStrBuff.append(viewerURL);
                        fileActionsStrBuff.append("<a href=\"" + downloadURL + "\">");
                        fileActionsStrBuff.append(
                                "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                + sTipDownload
                                + "\" title=\""
                                + sTipDownload + "\"></a>&nbsp;");
                    } else {
                        fileActionsStrBuff.append(
                                "<img border='0' src='../common/images/iconActionView.gif' alt=\""
                                + i18nViewerTip
                                + "\" title=\""
                                + i18nViewerTip + "\"></a>&nbsp;");
                        fileActionsStrBuff.append(
                                "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                + sTipDownload
                                + "\" title=\""
                                + sTipDownload + "\"></a>&nbsp;");
                    }
                }

                fileActions = fileActionsStrBuff.toString();
                fileActionsVector.add(fileActions);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return fileActionsVector;
        }
    }


    /**
     * creates URL for Actions or VC Files for Quick File Access
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments:
     *            objectList - maplist of DS file information
     *            paramList - map ofreport information
     * @return Vector of Actions URL's
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public static Vector getVCFileActions(Context context, String[] args) throws Exception {
        Vector fileActionsVector = new Vector();

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String objectId = null;
            boolean isprinterFriendly = false;
            if (paramList.get("reportFormat") != null) {
                isprinterFriendly = true;
            }
            String languageStr = (String) paramList.get("languageStr");
            String versionId = null;
            String fileActions = null;
            String fileName = null;
            String fileFormat = null;
            boolean canViewAndDownload = true;
            String viewerURL = null;
            String downloadURL = null;
            Map objectMap = new HashMap();

            String sTipDownload = EnoviaResourceBundle.getProperty(context,
                            "emxComponentsStringResource",
                            new Locale(languageStr),
                            "emxComponents.DocumentSummary.ToolTipDownload");

            String sTipCheckout = EnoviaResourceBundle.getProperty(context,
                            "emxComponentsStringResource",
                            new Locale(languageStr),
                            "emxComponents.DocumentSummary.ToolTipCheckout");

            String sTipUpdate = EnoviaResourceBundle.getProperty(context,
                            "emxComponentsStringResource",
                            new Locale(languageStr),
                            "emxComponents.DocumentSummary.ToolTipUpdate");

            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                    context.getSession().getLanguage());

            for(int i=0; i<objectList.size(); i++) {
                StringBuffer fileActionsStrBuff = new StringBuffer();
                objectMap = (Map) objectList.get(i);
                fileName = (String) objectMap.get(CommonDocument.SELECT_TITLE);
                String versionid = (String)objectMap.get("versionId");
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                objectId = (String) objectMap.get("masterId");
                versionId = (String) objectMap.get("versionId");

                if (canViewAndDownload) {
                    if (!isprinterFriendly) {
                        downloadURL = "javascript:.callCheckout('"
                                    + objectId
                                    + "','download', '"
                                    + fileName
                                    + "', '" + fileFormat
                                    + "', null, null, null, null, null, '"+ versionid +"');";

                        viewerURL = getViewerURL(context, objectId, fileFormat,fileName, versionid);
                        fileActionsStrBuff.append(viewerURL);
                        fileActionsStrBuff.append("<a href=\"" + downloadURL + "\">");

                        fileActionsStrBuff.append(
                                    "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                    + sTipDownload
                                    + "\" title=\""
                                    + sTipDownload + "\"></a>&nbsp;");

                        downloadURL = "javascript:callCheckout('"
                                    + objectId
                                    + "','checkout', '"
                                    + fileName
                                    + "', '" + fileFormat
                                    + "', null, null, null, null, null, '"+ versionid +"');";

                        fileActionsStrBuff.append("<a href=\"" + downloadURL + "\">");
                        fileActionsStrBuff.append(
                                    "<img style='border:0; padding: 2px;' src='../common/images/iconActionCheckOut.gif' alt=\""
                                    + sTipCheckout
                                    + "\" title=\""
                                    + sTipCheckout + "\"></a>&nbsp;");

                        fileActionsStrBuff.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&showFormat=readonly&showComments=required&objectAction=checkinVCFile&allowFileNameChange=false&noOfFiles=1&JPOName=emxVCDocument&methodName=checkinUpdate','730','450');\">");
                        fileActionsStrBuff.append("<img style='border:0; padding: 2px;' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipUpdate+"\" title=\""+sTipUpdate+"\"></a>&nbsp");

                    } else {
                        fileActionsStrBuff.append(
                                    "<img border='0' src='../common/images/iconActionView.gif' alt=\""
                                    + i18nViewerTip
                                    + "\" title=\""
                                    + i18nViewerTip + "\"></a>&nbsp;");

                        fileActionsStrBuff.append(
                                    "<img border='0' src='../common/images/iconActionDownload.gif' alt=\""
                                    + sTipDownload
                                    + "\" title=\""
                                    + sTipDownload + "\"></a>&nbsp;");

                        fileActionsStrBuff.append(
                                    "<img style='border:0; padding: 2px;' src='../common/images/iconActionCheckOut.gif' alt=\""
                                    + sTipCheckout
                                    + "\" title=\""
                                    + sTipCheckout + "\"></a>&nbsp;");

                        fileActionsStrBuff.append(
                                    "<img style='border:0; padding: 2px;' src='../common/images/iconActionCheckIn.gif' alt=\""
                                    + sTipUpdate
                                    + "\" title=\""
                                    + sTipUpdate + "\"></a>&nbsp;");
                    }
                }

                fileActions = fileActionsStrBuff.toString();
                fileActionsVector.add(fileActions);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return fileActionsVector;
        }
    }

    /**
     * This method is used to get the viewer URL for all viewers for given
     * format
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param objectId
     *            the objectid from which file need to be checked out
     * @param format
     *            the format from which file need to be checked out
     * @param fileName
     *            the fileName to be checked out
     * @return String URL for all viewers
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public static String getViewerURL(Context context, String objectId,
                                String format, String fileName) throws Exception {

        return getViewerURL(context, objectId, format, fileName, "");
    }


    /**
     * This method is used to get the viewer URL for all viewers for given
     * format
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param objectId
     *            the objectid from which file need to be checked out
     * @param format
     *            the format from which file need to be checked out
     * @param fileName
     *            the fileName to be checked out
     * @return String URL for all viewers
     * @throws Exception
     *             if the operation fails
     * @since Common 10.7
     * @grade 0
     */
    public static String getViewerURL(Context context, String objectId,
            String format, String fileName, String versionid) throws Exception {

        try {
            Map formatViewerMap = FormatUtil.getViewerCache(context);
            String returnURL = "";
            String URLParameters = "?action=view&id=" + objectId + "&objectId="
                    + objectId + "&format=" + format + "&file=" + fileName
                    + "&fileName=" + fileName;

            Map formatDetailsMap = (Map) formatViewerMap.get(format);
            if (formatDetailsMap == null) {
                FormatUtil.loadViewerCache(context);
            }
            formatDetailsMap = (Map) formatViewerMap.get(format);
            String viewerURL = "";
            String servletPreFix = EnoviaResourceBundle
                    .getProperty(context,"emxFramework.Viewer.ServletPreFix");

            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                                    context.getSession().getLanguage());
            String servletURL = "";
            StringBuffer fileViewerURL = new StringBuffer(256);
            String aliasFormat = FrameworkUtil.getAliasForAdmin(context, "format", format, true);
            FormatUtil formatUtil = new FormatUtil(aliasFormat);
            String viewer = formatUtil.getViewerPreference(context, null);

            if (formatDetailsMap == null) {
                viewerURL = "javascript:.callCheckout('"
                        + XSSUtil.encodeForJavaScript(context, objectId)
                        + "','view', '"
                        + XSSUtil.encodeForJavaScript(context, fileName)
                        + "', '"
                        + XSSUtil.encodeForJavaScript(context, format)
                        + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";

                fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                fileViewerURL.append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                fileViewerURL.append(i18nViewerTip);
                fileViewerURL.append("\" title=\"");
                fileViewerURL.append(i18nViewerTip);
                fileViewerURL.append("\"></img></a>&#160;");
                returnURL = fileViewerURL.toString();
            } else {
                java.util.Set set = formatDetailsMap.keySet();
                Iterator itr = set.iterator();
                boolean needDefaultViewer = false;
                while (itr.hasNext()) {
                    viewerURL = (String) itr.next();
                    if (viewer == null || "".equals(viewer) || "null".equals(viewer) || viewerURL.equals(viewer)) {
                        needDefaultViewer = true;
                        viewerTip = ((String) formatDetailsMap.get(viewerURL));
                        i18nViewerTip = i18nNow.getViewerI18NString(viewerTip,
                                            context.getSession().getLanguage());

                        if (viewerTip.equalsIgnoreCase("Default")) {
                            viewerURL = "javascript:callCheckout('"
                                    + XSSUtil.encodeForJavaScript(context, objectId)
                                    + "','view', '"
                                    + XSSUtil.encodeForJavaScript(context, fileName)
                                    + "', '" + XSSUtil.encodeForJavaScript(context, format)
                                    + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                            fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                            fileViewerURL
                                .append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\" title=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\"></img></a>&#160;");
                        } else {
                            viewerURL = servletPreFix + viewerURL  + "?action=view&";
                            viewerURL = "javascript:callViewer('"
                                    +XSSUtil.encodeForJavaScript(context, objectId) 
                                    + "','view', '"
                                    + XSSUtil.encodeForJavaScript(context, fileName)
                                    + "', '"
                                    + XSSUtil.encodeForJavaScript(context, format)
                                    + "', '"
                                    + viewerURL
                                    + "', null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                            fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                            fileViewerURL
                                .append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\" title=\"");
                            fileViewerURL.append(i18nViewerTip);
                            fileViewerURL.append("\"></img></a>&#160;");
                        }
                    }
                }
                if (!needDefaultViewer) {
                    viewerURL = "javascript:.callCheckout('"
                            + XSSUtil.encodeForJavaScript(context, objectId)
                            + "','view', '"
                            +XSSUtil.encodeForJavaScript(context, fileName) 
                            + "', '"
                            +XSSUtil.encodeForJavaScript(context, format)  + "', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionid) +"');";
                    fileViewerURL.append("<a href=\"" + viewerURL + "\">");
                    fileViewerURL
                            .append("<img src=\"../common/images/iconActionView.gif\" border=\"0\" alt=\"");
                    fileViewerURL.append(i18nViewerTip);
                    fileViewerURL.append("\" title=\"");
                    fileViewerURL.append(i18nViewerTip);
                    fileViewerURL.append("\"></img></a>&#160;");
                }

                returnURL = fileViewerURL.toString();
            }

            return returnURL;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

   /**
    *  returns true if design sync store is set and object is a Project Space Task
    *  @param context the eMatrix <code>Context</code> object
    *  @return boolean
    *  @throws Exception if the operation fails
    *  @since Common 10.7
    *  @grade 0
    */
    public boolean isProjectSpaceTask(Context context,String[] args) throws Exception {
        boolean hasDSServer = false;
        boolean isProjectSpaceTask = false;
        boolean bActivateDSFA= FrameworkUtil.isSuiteRegistered(context, "ActivateDSFA", false,null,null);

        if (bActivateDSFA) {
            if (hasDesignSyncStore == null) {
                loadDesignSyncStores(context);
            }

            if ("true".equals(hasDesignSyncStore)) {
                hasDSServer = true;
            }
        }

        if (hasDSServer) {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String objectId  = (String) paramMap.get("objectId");

            if (objectId!=null && !objectId.equals("") && !objectId.equals("null")) {
                DomainObject taskObject = DomainObject.newInstance(context, objectId);
                StringList objectSelects = new StringList(2);
                objectSelects.add(DomainConstants.SELECT_TYPE);
                objectSelects.add(SELECT_PROJECT_TYPE_FROM_TASK);
                ContextUtil.pushContext(context);
                Map objectMap = (Map)taskObject.getInfo(context,objectSelects);
                ContextUtil.popContext(context);
                String objectType = (String)objectMap.get(DomainConstants.SELECT_TYPE);
                String parentObjectType = (String)objectMap.get(SELECT_PROJECT_TYPE_FROM_TASK);

                if (objectType!=null && !objectType.equals("") && !objectType.equals("null")
                    && parentObjectType!=null && !parentObjectType.equals("")
                    && !parentObjectType.equals("null") && objectType.equals(DomainConstants.TYPE_TASK)
                    && (parentObjectType.equals(DomainConstants.TYPE_PROJECT_SPACE)
                    || parentObjectType.equals(DomainConstants.TYPE_PROJECT_TEMPLATE)))
                {
                    isProjectSpaceTask = true;
                }
            }
        }

        return isProjectSpaceTask;
    }


    /*
    **
    **
    */
    public StringList getDesignSyncStores(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String stores = MqlUtil.mqlCommand(context, "list $1","store" );
        StringList storeList = FrameworkUtil.split(stores, "\n");
        Iterator storeItr = storeList.iterator();
        StringList servers = new StringList(5);
        String storeType;
        String storeName;

        int i = 0;
        while (storeItr.hasNext()) {
            storeName = (String) storeItr.next();
            storeType = MqlUtil.mqlCommand(context, "print store $1 select $2 dump", storeName, "type");
            if("designsync".equalsIgnoreCase(storeType)) {
                servers.add(storeName);
            }
            i++;
        }

        if (i > 1) {
            servers.add(0, "");
        }

        return servers;
    }


    /*
    **
    **
    */
    public boolean hasNavigateAccess(Context context,String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        StringList slist = getDesignSyncStores(context,args);

        // return true if there are more than one ds store
        return (slist.size() > 1);
    }


   /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7-SP1
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getVCFileBranchVersionInfo(Context context, String[] args) throws Exception {
        MapList retMapList = new MapList();

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            String versionid = (String) programMap.get("versionid");
            String vcName = (String)programMap.get("vcName");
            String folderPath = (String)programMap.get("folderPath");
            String fromPage = (String)programMap.get("fromPage");

            DomainObject object = DomainObject.newInstance(context, objectId);
            VcFileBranchVersionInfo info = null;

            if ("FolderContents".equals(fromPage) || "FolderContentBranches".equals(fromPage)) {
                info = object.getVcfileBranchVersionInfo(context, 1, folderPath, vcName, "versionid@"+versionid);
                fromPage = "FolderContentBranches";
            } else {
                info = object.getVcfileBranchVersionInfo(context, 1, "");
                fromPage = "FileBranchSummary";
            }

            HashMap map = new HashMap();
            String branchId = info.getVersionId();
            String versionId = info.getVersionId();
            branchId = branchId.substring(0, branchId.lastIndexOf("."));
            map.put("branchId", branchId);
            map.put("objectId", objectId);
            map.put("locker", info.getLocker());
            map.put("retired", Boolean.valueOf(info.getIsRetired()).toString());
            map.put("VCTags",info.getBranchTags());
            map.put("vcname", vcName);
            map.put("folderPath", folderPath);

            retMapList.add(map);

            VcFileSubBranchInfoList  bvInfo  = null;
            VcFileSubBranchInfo  sbInfo = null;

            if ("FolderContents".equals(fromPage) || "FolderContentBranches".equals(fromPage)) {
                bvInfo = object.getVcfileSubBranchInfo(context, 1, folderPath, vcName, "versionid@"+versionid);
            } else {
                bvInfo = object.getVcfileSubBranchInfo(context,1,"versionid@"+versionid);
            }

            for (int idx = 0; idx < bvInfo.size(); idx++) {
                sbInfo = (VcFileSubBranchInfo) bvInfo.getElement(idx);
                map = new HashMap();
                map.put("branchId",sbInfo.getSubBranchid());
                map.put("objectId", objectId);
                map.put("locker",sbInfo.getLocker());
                map.put("retired",Boolean.valueOf(sbInfo.isRetired()).toString());
                map.put("VCTags",sbInfo.getTags());
                map.put("vcname", vcName);
                map.put("folderPath", folderPath);
                retMapList.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }

        return retMapList;
    }


   /*
    * getVCModuleContentsName - Will get the Names for Module Summary Table
    *       Will be called in the Name Column.
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of revision values
    *          for the column.
    *  @throws Exception if the operation fails
    *  @since Common R207
    *  @grade 0
    */

    public Object getVCModuleContentsName(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Vector versionVector = new Vector(objectList.size());

            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map) objectList.get(i);
                String name = (String) objectMap.get("name");
                versionVector.add(name);
            }

            return versionVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }


    /*
     * getVCModuleVersions - Will get the Versions for Module Summary Table
     *       Will be called in the Versions Column.
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return Vector object that contains a vector of revision values
     *          for the column.
     *  @throws Exception if the operation fails
     *  @since Common R207
     *  @grade 0
     */
    public Object getVCModuleVersions(Context context, String[] args) throws Exception {
        try {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");
            String fromPage = (String)paramList.get("fromPage");
            String level = (String)paramList.get("level");

            Vector versionVector = new Vector(objectList.size());

            for (int i = 0; i < objectList.size(); i++ ) {
                Map objectMap = (Map)objectList.get(i);
                String version = (String)objectMap.get("version");
                versionVector.add(version);
            }

            return versionVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }


    /*
     * getVCModuleComments - Will get the Comments for Module Summary Table
     *       Will be called in the Comments Column.
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return Vector object that contains a vector of revision values
     *          for the column.
     *  @throws Exception if the operation fails
     *  @since Common R207
     *  @grade 0
     */
    public Object getVCModuleComments(Context context, String[] args) throws Exception {
        try {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            //Map paramList = (Map)programMap.get("paramList");
            //String fromPage = (String)paramList.get("fromPage");
            //String level = (String)paramList.get("level");
            MapList objectList = (MapList)programMap.get("objectList");
            Vector commentVector = new Vector(objectList.size());

            for (int i = 0; i < objectList.size(); i++ ) {
                Map objectMap = (Map)objectList.get(i);
                String comment = (String) objectMap.get("comment");
                commentVector.add(comment==null ? _space : comment);
            }

            return commentVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }


    /*
     * getVCModuleTags - Will get the Tags for Module Summary Table
     *       Will be called in the Tags Column.
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return Vector object that contains a vector of revision values
     *          for the column.
     *  @throws Exception if the operation fails
     *  @since Common R207
     *  @grade 0
     */
    public Object getVCModuleTags(Context context, String[] args) throws Exception {
        try {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            String fromPage = (String)paramList.get("fromPage");
            String level = (String)paramList.get("level");

            Vector versionVector = new Vector(objectList.size());

            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map)objectList.get(i);
                String tags=(String)objectMap.get("tags");
                versionVector.add(tags);
            }

            return versionVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }


    /*
     * getVCModuleHrefs - Will get the Module Href's for Module Summary Table
     *       Will be called in the Other Column.
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return Vector object that contains a vector of revision values
     *          for the column.
     *  @throws Exception if the operation fails
     *  @since Common R207
     *  @grade 0
     */
    public Object getVCModuleHrefs(Context context, String[] args) throws Exception {
        try {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            String fromPage = (String)paramList.get("fromPage");
            String level = (String)paramList.get("level");

            Vector versionVector = new Vector(objectList.size());

            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map)objectList.get(i);
                String hrefname = (String)objectMap.get("hrefname");

                if (hrefname == null)
                    hrefname = "";

                versionVector.add(hrefname);
            }

            return versionVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }

    /*
     * getVCModuleContentActions - Will get the Module Actions for Module Summary Table
     *       Will be called in the Actions Column.
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return Vector object that contains a vector of revision values
     *          for the column.
     *  @throws Exception if the operation fails
     *  @since Common R207
     *  @grade 0
     */

    public Object getVCModuleContentActions(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map) programMap.get("paramList");
            MapList objectList = (MapList) programMap.get("objectList");

            String fromPage = (String) paramList.get("fromPage");
            String level = (String) paramList.get("level");

            Vector versionVector = new Vector(objectList.size());

            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map) objectList.get(i);
                String actions = (String) objectMap.get("actions");

                if (actions == null)
                    actions = "";

                versionVector.add(actions);
            }

            return versionVector;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }

    /**
     * This method is used to get the Module Versions based on
     *  static OR dynamic choice of selectors
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common R207
     * @grade 0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getVCModuleHrefsNavigate(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            //Map paramList = (Map)programMap.get("paramList");
            final boolean recursive = false; //fzz:future

            String objectId = (String) programMap.get("objectId");
            String fromPage = (String) programMap.get("fromPage");
            String name = (String) programMap.get("name");
            String version = (String) programMap.get("version");
            String tags = (String) programMap.get("tags");
            String storePath = (String)programMap.get("storePath");
            String hrefType = (String) programMap.get("hrefType");
            String actions = (String)programMap.get("actions");
            String comment = "";
            String path = "";
            String config = "";
            String relPath = "";

            VcModuleBranchInfoList branchList = null;
            VcModuleBranchInfo branchInfo = null;
            VcModuleVersionInfoList versionList = null;
            VcModuleVersionInfo versionInfo = null;

            DomainObject object = DomainObject.newInstance(context, objectId);

            if (tags.indexOf(":") !=-1 ) {
                int counter = tags.lastIndexOf(":");
                tags = tags.substring(0, counter);
            }

            MapList objectList = new MapList();
            Map map = new HashMap();

            if (storePath.equals("/")) {
                path = "Modules/"+name;
            } else {
                path = name;
            }

            relPath = path + "/" + name + "/" + version;
            String sCheckoutAccess = object.getInfo(context, CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);

            String moduleActions = _space;
            boolean canDownload = true;

            if (!"TRUE".equalsIgnoreCase(sCheckoutAccess)) {
                canDownload = false;
            }

            if (name.indexOf("/")!=-1) {
                name=name.substring(name.lastIndexOf("/")+1);
            }

            if (hrefType.equals("dynamic")) {
                map.put("globalType","module");
                config=null;
                branchList = object.getVcModuleBranchInfo(context, 1, path, recursive, config);

                for (int idx=0;idx<branchList.size();idx++) {
                    branchInfo=(VcModuleBranchInfo) branchList.getElement(idx);
                    String branchName=branchInfo.getBranch();

                    if (branchName.equals(tags)) {
                        config=branchName;
                        versionList = object.getVcModuleVersionInfo(context, 1, path, recursive, config);
                        versionInfo=(VcModuleVersionInfo) versionList.getElement(versionList.size()-1);
                        version=versionInfo.getVersion();
                        comment=versionInfo.getComment();
                        StringList tagsList3 = versionInfo.getTags();
                        tags = FrameworkUtil.join(tagsList3, ",");
                        break;
                    }

                    StringList branchTag = branchInfo.getTags();
                    String branchCheck = "";

                    for (int ldx=0;ldx<branchTag.size();ldx++) {
                        branchCheck=(String)branchTag.get(ldx);
                        if (branchCheck.equals(tags)) {
                            config=branchName;
                            versionList = object.getVcModuleVersionInfo(context, 1, path, recursive, config);
                            versionInfo=(VcModuleVersionInfo) versionList.getElement(versionList.size()-1);
                            version=versionInfo.getVersion();
                            break;
                        }
                    }

                    if (version == null) {
                        config = branchName;
                        versionList = object.getVcModuleVersionInfo(context, 1, path, recursive, config);
                        for (int jdx=0; jdx < versionList.size(); jdx++) {
                            versionInfo = (VcModuleVersionInfo) versionList.getElement(jdx);
                            String versionCheck = versionInfo.getVersion();
                            if (versionCheck.equals(tags)) {
                                version = tags;
                                comment=versionInfo.getComment();
                                StringList tagsList2 = versionInfo.getTags();
                                tags = FrameworkUtil.join(tagsList2, ",");
                                break;
                            }

                            String tagsCheck;
                            StringList tagsList = versionInfo.getTags();

                            if (tagsList.size() == 0) {
                                tagsCheck = _space;
                            } else {
                                for (int kdx=0; kdx < tagsList.size(); kdx++) {
                                    tagsCheck = (String) tagsList.get(kdx);
                                    if (tagsCheck.equals(tags)) {
                                        version = versionInfo.getVersion();
                                        comment = versionInfo.getComment();
                                        StringList tagsList1 = versionInfo.getTags();
                                        break;
                                    }
                                }
                            }
                        }
                    } // if (version == null)
                } //  for (int idx=0;idx<branchList.size();idx++)

                if (version == null) {
                    String strLanguage = context.getSession().getLanguage();
                    String  strError = EnoviaResourceBundle.getProperty(context,
                                    "emxComponentsStringResource",
                                    new Locale(strLanguage),
                                    "emxComponents.VCDocument.UnresolvedDynamicSelector");

                    emxContextUtil_mxJPO.mqlNotice(context, strError);
                    tags = "";
                    map.put("name", name);
                    map.put("tags", tags);
                    map.put("id", joinex("unresolved",fromPage,name,_space,_space,objectId,_space,_none));
                } else {
                    map.put("name", name);
                    map.put("tags", tags);
                    map.put("comment", comment);
                    map.put("version", version);
                    map.put("objectId", objectId);
                    config = version;
                    map.put("id", joinex("version",fromPage,name,path,config,objectId,relPath,_none));
                }
            } else if (hrefType.equals("static")) {
                config = version;
                map.put("id", joinex("version",fromPage,name,path,config,objectId,relPath,_none));
                config = null;
                branchList = object.getVcModuleBranchInfo(context, 1, path, recursive, config);

                for (int idx=0;idx<branchList.size();idx++) {
                    branchInfo=(VcModuleBranchInfo) branchList.getElement(idx);
                    String branchName=branchInfo.getBranch();
                    config=branchName;
                    versionList = object.getVcModuleVersionInfo(context, 1, path, recursive, config);

                    for (int jdx=0; jdx<versionList.size(); jdx++) {
                        versionInfo = (VcModuleVersionInfo) versionList.getElement(jdx);
                        String versionCheck = versionInfo.getVersion();
                        if (versionCheck.equals(version)) {
                            StringList tagsList = versionInfo.getTags();
                            tags = FrameworkUtil.join(tagsList,",");
                            comment = versionInfo.getComment();
                        }
                    }
                }

                map.put("name",name);
                map.put("version",version);
                map.put("tags",tags);
                map.put("comment",comment);
                map.put("hrefname",_space);
                map.put("objectId",objectId);
                map.put("globalType","module");
            }

            if (canDownload) {
                String downloadURL = null;
                StringBuffer moduleActionsStrBuff = new StringBuffer();

                downloadURL = "javascript:callCheckout('"
                    + objectId
                    + "','download', '"
                    + path + "', '"
                    + "generic"+"', null, null, null, null, null, '" + version + "');";

                moduleActionsStrBuff.append("<a href=\"" + downloadURL + "\">");
                moduleActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif'></img></a>&#160;");
                moduleActions = moduleActionsStrBuff.toString();
            } else {
                moduleActions = _space;
            }

            map.put("actions",moduleActions);
            objectList.add(map);

            return objectList;

        } catch (Exception e) {
            e.printStackTrace();
            throw (e);
        }
    }

    // ***********************************************************************************
    // ****   The main entry point to Navigate DesignSync and similar functionality   ****
    // ***********************************************************************************

    /**
     * This expand method is the main entry point for the DesignSync Vault navigation
     * @param context - the Matrix Context
     * @param args - the packed argument maps
     * @return - MapList with the Objects for the Structure Browser
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getVCAllContents(Context context, String[] args) throws Exception {
        //HashMap programMap = (HashMap) JPO.unpackArgs(args);
        //final boolean initialRequest = getInitialRequest(programMap);
        //final short expandLevel = getExpandLevel((String) programMap.get("expandLevel"));

        // -------------------------------------------------------------------------------
        // fzz/TBD: handling the recursion/expandLevel support here vs. the specific
        // work-horses like getVCModuleContents and getVCFolderContents looks very
        // appealing. We have to be careful, though, because these type-oriented procs
        // are public methods and likely to be called directly from various points
        // in the application. We need to analyze whether this could be changed so this
        // method can be made the single entry point for all clients.
        // - If yes, recursion should be controlled here.
        // - If no, this wrapper should be gone and replaced with doGetVCAllContents.
        // NOTE: I also modified all these methods to return MapList rather than Object
        // -------------------------------------------------------------------------------
        // Since getVCModuleContents and getVCFolderContents may be called directly
        // we have to make sure that multiLevel flag is added to the returned MapList,
        // added only once, and is the very last item on the list.
        // -------------------------------------------------------------------------------
        _addExpandMultiLevelFlag = false;

        MapList retList;
        try {
            retList = doGetVCAllContents(context, args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw (ex);
        }

        return addExpandMultiLevelFlag(retList);
    }



    /**
     * This method acts as a global method and calls getVCFolderContents OR getVCModuleContents
     * for further expansion, based on the storePath defined and current object being expanded.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common R207
     * @grade 0
     */
    MapList doGetVCAllContents(Context context, String[] args) throws Exception {
        MapList retMapList = new MapList();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        final boolean initialRequest = getInitialRequest(programMap);
        final short expandLevel = getExpandLevel((String) programMap.get("expandLevel"));

        String fromPage = (String) programMap.get("fromPage");
        String objectId = (String) programMap.get("objectId");
        String objectIdModule = (String) programMap.get("objectIdModule");

        jpoTrace("initialRequest=" + initialRequest + " expandLevel=" + expandLevel
                    + " _addExpandMultiLevelFlag=" + _addExpandMultiLevelFlag
                    + "\n \t\t\t\t  fromPage=" + fromPage + " objectId=" + objectId);

        String storePath = _empty;
        String storeName = _empty;
        StringList folderList = null;

        if (! fromPage.equals("searchVCDocuments")) {
            storeName = (String) programMap.get("storeName");
            if (storeName.isEmpty()) {
                storePath = (String) programMap.get("storePath");
            } else {
                storePath = getStorePath(context, storeName);
            }
        }

        // 1. searchVCDocuments
        if (fromPage.equals("searchVCDocuments") && initialRequest) {
            DomainObject dom = DomainObject.newInstance(context, objectId);
            String objType = dom.getInfo(context, DomainConstants.SELECT_TYPE);
            String objRev = dom.getInfo(context, DomainConstants.SELECT_REVISION);

            if (objType.equals(TYPE_mxsysDSFAHolder)) {
                // probably could getInfo on "vcmodule" and "vcfolder"
                if (objRev.equals("-")) {
                    storeName = dom.getInfo(context, "vcfolder[1].store");
                }
                if (objRev.equalsIgnoreCase("Modules")) {
                    storeName = dom.getInfo(context, "vcmodule[1].store");
                }
                storePath = getStorePath(context, storeName);
            } else {
                return getVCFolderContents(context, JPO.packArgs(programMap));
            }
        }


        String root = "";

        if (storePath.isEmpty()) {
            storePath = "/";
        }

        if (! storePath.contains("/")) {
            root = storePath;
        } else {
            if (storePath.equals("/")) {
                if (initialRequest) {
                    //fzz attempt to handle top-level expansion here ...close, but unfinished
                    if (false && expandLevel > 1) {
                        // collect top-level folders and dispatch them appropriately
                        // (Modules vs Projects, etc) while accumulating results
                        programMap.put("expandLevel", "1");
                        MapList topList = getVCFolderContents(context, JPO.packArgs(programMap));
                        programMap.put("expandLevel", Integer.toString(expandLevel));
                        programMap.put("level", "0"); // red.
                        MapList retList = new MapList();
                        for (Object obj : topList) {
                            Map map = (Map) obj;
                            String type = (String) map.get("type");
                            String path = (String) map.get("folderPath");
                            String id = (String) map.get("id");
                            Map argMap = programMap;
                            if ("folder".equals(type) && "Modules".equals(path)) {
                                argMap.put("objectId", objectIdModule);
                                argMap.put("path", path);
                                retList.addAll(getVCModuleContents(context, JPO.packArgs(argMap)));
                            } else {
                                argMap.put("objectId", id);
                                retList.addAll(getVCFolderContents(context, JPO.packArgs(argMap)));
                            }
                        }
                        return retList;
                    } else {
                        programMap.put("level", "0");
                        programMap.put("objectId", objectId); // red.
                        programMap.put("globalType", "fileFolder");
                        return getVCFolderContents(context, JPO.packArgs(programMap));
                    }
                } else {
                    String folderPath = _empty;
                    String versionId = _empty;
                    String moduleName = _empty;
                    String modulePath = _empty;
                    String moduleConfig = _empty;

                    String id = (String) programMap.get("objectId");
                    java.util.StringTokenizer strtk = new java.util.StringTokenizer(id, "$");
                    String fileOrFolder = strtk.nextToken();

                    if (fileOrFolder.equalsIgnoreCase("folder")) {
                        fromPage = strtk.nextToken();
                        folderPath = strtk.nextToken();
                        objectId = strtk.nextToken();
                        versionId = strtk.nextToken();

                        if (folderPath.equals("Modules")) {
                            if (objectIdModule == null) {
                                DomainObject dom = DomainObject.newInstance(context, objectId);
                                storeName = dom.getInfo(context, "vcfolder[1].store");
                                objectIdModule = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE_mxsysDSFAHolder, storeName, "Modules", "id");
                            }

                            programMap.put("level", null); //?
                            programMap.put("fromPage", fromPage);
                            programMap.put("objectId", objectIdModule);
                            programMap.put("path", "Modules");
                            programMap.put("globalType", "module");
                            programMap.put("storePath", storePath);
                            return getVCModuleContents(context, JPO.packArgs(programMap));
                        } else {
                            programMap.put("level", "0"); //!
                            programMap.put("fromPage", fromPage);
                            programMap.put("objectId", joinex(fileOrFolder,fromPage,folderPath,objectId,versionId,_none));
                            programMap.put("globalType", "fileFolder");
                            return getVCFolderContents(context,JPO.packArgs(programMap));
                        }
                    }

                    if (fileOrFolder.equalsIgnoreCase("file")) {
                        fromPage = strtk.nextToken();
                        folderPath = strtk.nextToken();
                        String fileName = strtk.nextToken();
                        objectId = strtk.nextToken();
                        versionId = strtk.nextToken();

                        programMap.put("level", "0"); //!
                        programMap.put("fromPage", fromPage);
                        programMap.put("objectId", joinex(fileOrFolder,fromPage,folderPath,fileName,objectId,versionId,_none));
                        programMap.put("globalType", "fileFolder");
                        return getVCFolderContents(context, JPO.packArgs(programMap));
                    }

                    if (matchesOneOf(fileOrFolder, "category","module","branch","version","href")) {
                        fromPage = strtk.nextToken();
                        moduleName = strtk.nextToken();
                        modulePath = strtk.nextToken();
                        if (!modulePath.contains("/") && !fromPage.equals("searchVCDocuments")) {
                            modulePath = "Modules/" + modulePath;
                        }
                        moduleConfig = strtk.nextToken();
                        objectId = strtk.nextToken();
                        programMap.put("objectId", joinex(fileOrFolder,fromPage,moduleName,modulePath,moduleConfig,objectId,_space,_none));
                        programMap.put("storePath", storePath);
                        programMap.put("globalType", "module");
                        return getVCModuleContents(context, JPO.packArgs(programMap));
                    }
                }
            } else {
                java.util.StringTokenizer strtk = new java.util.StringTokenizer(storePath, "/");
                root = strtk.nextToken(); //!
            }
        }

        // ********************************************************************
        // Projects
        // ********************************************************************
        if (root.equals("Projects")) {
            programMap.put("level", "0");
            programMap.put("path", storePath);
            programMap.put("globalType", "fileFolder");
            return getVCFolderContents(context, JPO.packArgs(programMap));
        }

        // ********************************************************************
        // Modules
        // ********************************************************************
        if (root.equals("Modules")) {
            if (! initialRequest) {
                // ------------------------------------------------------------
                // a subsequent call
                //-------------------------------------------------------------
                String id = (String) programMap.get("objectId");
                java.util.StringTokenizer strtk = new java.util.StringTokenizer(id, "$");
                String dstype = strtk.nextToken();

                if (matchesOneOf(dstype, "category","module","branch","version","href")) {
                    fromPage = strtk.nextToken();
                    String moduleName = strtk.nextToken();
                    String modulePath = strtk.nextToken();
                    String moduleConfig = strtk.nextToken();
                    objectId = strtk.nextToken();
                    programMap.put("objectId", joinex(dstype,fromPage,moduleName,modulePath,moduleConfig,objectId,_space,_none));
                    programMap.put("storePath", storePath);
                    programMap.put("globalType", "module");
                    return getVCModuleContents(context, JPO.packArgs(programMap));
                }
            } else {
                // ------------------------------------------------------------
                // initial request
                //-------------------------------------------------------------
                if (objectIdModule == null) {
                    DomainObject dObj = DomainObject.newInstance(context, objectId);
                    storeName = dObj.getInfo(context,"vcmodule[1].store");
                    objectIdModule = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 dump", TYPE_mxsysDSFAHolder, storeName, "Modules", "id");
                    programMap.put("level", null); //!
                }

                programMap.put("objectId", objectIdModule);
                if (storePath.lastIndexOf("/") > 0) {
                    programMap.put("path", storePath.replaceFirst("^/", _empty));
                } else {
                    programMap.put("path", _empty);
                }
                programMap.put("storePath", storePath);
                programMap.put("globalType", "module");
                return getVCModuleContents(context, JPO.packArgs(programMap));
            }
        } // root == Modules

        // nothing left to do, return the empty list
        return retMapList;
    }


    /**
     * This wrapper around doGetVCModuleContents supports multi-level expansion
     * @param context - Matrix Context
     * @param args - packed argument maps
     * @return - mapList with results
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getVCModuleContents(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        boolean initialRequest = getInitialRequest(programMap);
        final short expandLevel = getExpandLevel((String) programMap.get("expandLevel"));
        String sbLevel = (String) programMap.get("level");

        jpoTrace("initialRequest="+initialRequest + " level="+sbLevel + " expandLevel="+expandLevel);

        MapList retList = (MapList) doGetVCModuleContents(context, args);
        // -----------------------------------------------------------
        // if expansion is not requested or not possible - return here
        // -----------------------------------------------------------
        if (retList.isEmpty() || expandLevel==1)
            return retList;


        // -----------------------------------------------------------
        // Handle multi-level expansion.
        // For future dev. see comments in getVCAllContents
        // -----------------------------------------------------------
        short level = 1;
        try {
            level = (short) (Integer.parseInt(sbLevel) + 1);
        } catch (NumberFormatException e) {
            level = 1; // red.
        }

        MapList finalList = new MapList();

        // iterate through the result and expand each object
        for (Object obj : retList) {
            Map map = (Map) obj;
            finalList.add(map);

            String id = (String) map.get("id");
            programMap.put("objectId", id);
            jpoTrace("Expanding objectId " + id);
            expandVCObject(context, level, expandLevel, programMap, finalList, true);
        }

        // it's true, if we're called directly
        if (_addExpandMultiLevelFlag)
            addExpandMultiLevelFlag(finalList);

        return finalList;
    }


    /**
     * This method is used to get the list of Module Types
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the input arguments: objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common R207
     * @grade 0
     */
    MapList doGetVCModuleContents(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        final boolean recursive = false; //fzz:future
        final boolean initialRequest = getInitialRequest(programMap);
        //final short expandLevel = getExpandLevel((String) programMap.get("expandLevel"));
        //final String level = (String) programMap.get("level");

        String fromPage = (String) programMap.get("fromPage");
        String path = (String) programMap.get("path");

        String config = (String) programMap.get("config");
        String globalType = (String) programMap.get("globalType");
        String storePath = (String) programMap.get("storePath");
        String objectId = (String) programMap.get("objectId");

        jpoTrace("fromPage="+fromPage + " storePath="+storePath + " path="+path
                + " objectId=" + objectId);

        // fzz: why spaces are used everywhere rather than empty strings?
        if (storePath == null)
            storePath = _space;

        if (!storePath.equals(_space) && !storePath.startsWith("/"))
            storePath = "/" + storePath;

        String name = _space;
        String comment = _space;
        String branch = _space;
        String tags = _space;
        String hrefname = _space;
        String type = _space;
        String relPath = _space;
        String actions = _space;
        String modPath = _space;
        String moduleName = _space;
        StringList tagsList = null;
        DomainObject dom = null;

        MapList retMapList = new MapList();

        //***************************************************************************
        // The first call to display the top-level objects in the structure browser
        //***************************************************************************
        if (initialRequest) {
            if (fromPage.equals("VCModuleContents")) {
                dom = DomainObject.newInstance(context, objectId);
                path = dom.getInfo(context, "vcmodule[1].path");

                modPath = path;
                moduleName = getLeafName(path);

                if (path.equals(".")) {
                    modPath = _space;
                    String storeName = dom.getInfo(context, "vcmodule[1].store");
                    String storeModulePath = getStorePath(context, storeName);
                    moduleName = getLeafName(storeModulePath);
                }

                HashMap map = new HashMap();
                StringBuilder sb = new StringBuilder(256);
                sb.append("<img src='../common/images/iconSmallDSModule.gif'");
                sb.append(" id=\"").append(moduleName).append("\"");
                sb.append(" />");
                sb.append(moduleName);
                map.put("name", sb.toString());
                map.put("globalType", "module");
                map.put("comment", comment);
                map.put("id", joinex("module",fromPage,moduleName,modPath,config,objectId,_empty,_none));
                retMapList.add(map);
            } // fromPage == VCModuleContents


            if (!fromPage.equals("VCModuleContents") && path.contains("/")) {
                path = _space;
                dom = DomainObject.newInstance(context, objectId);
                VcModuleInfoList moduleList = dom.getVcModuleInfo(context, 1, path, recursive, config);
                VcModuleInfo modInfo;

                if (moduleList.size() == 0) {
                    type = "branch"; //? is this always a safe assumption?
                } else {
                   modInfo = (VcModuleInfo) moduleList.getElement(0);
                   type = modInfo.getType();
                }

                // --------------------------------------------------------
                // type is either "module" or "category"
                // --------------------------------------------------------
                if (type.equals("module") || type.equals("category")) {
                    for (int idx=0; idx < moduleList.size(); idx++) {
                        HashMap map = new HashMap();
                        StringBuilder sb = new StringBuilder(256);

                        modInfo = (VcModuleInfo) moduleList.getElement(idx);
                        moduleName = modInfo.getName();
                        comment = modInfo.getComment();
                        String categoryOrModule = modInfo.getType();

                        if (moduleName.contains("/")) {
                            moduleName = getLeafName(moduleName);
                            if (categoryOrModule.equalsIgnoreCase("category")) {
                                sb.append("<img src='../common/images/iconSmallDSCategory.gif'");
                                sb.append(" id=\"").append(moduleName).append("\"");
                                sb.append(" >");
                                sb.append("</img>");
                                sb.append(moduleName);
                                // IR-154937V6R2013x - the category path, similar to a folder
                                // should include path prior to the leaf name: e.g Modules/cat1/cat2
                                //String catPath = moduleName;
                                String catPath = modInfo.getPath().replaceAll("^syncs?:///", "");
                                // 'sync:///Modules/chips/analog' -> 'Modules/chips/analog'
                                map.put("name", sb.toString());
                                map.put("id", joinex("category",fromPage,moduleName,catPath,config,objectId,relPath,_none));
                                map.put("globalType", globalType);
                                retMapList.add(map);
                            } else if (categoryOrModule.equalsIgnoreCase("module")) {
                                sb.append("<img src='../common/images/iconSmallDSModule.gif'");
                                sb.append(" id=\"").append(moduleName).append("\"");
                                sb.append(" />");
                                sb.append(moduleName);
                                modPath = moduleName;
                                // IR-121395V6R2012x
                                relPath = moduleName;
                                map.put("name", sb.toString());
                                map.put("comment", comment);
                                map.put("id", joinex("module",fromPage,moduleName,modPath,config,objectId,relPath,_none));
                                map.put("globalType", globalType);
                                retMapList.add(map);
                            }
                        } // if moduleName contains "/"
                    } // iterating through moduleList
                } else {
                    // --------------------------------------------------------
                    // type is not "module" or "category"
                    // --------------------------------------------------------
                    path = _space;
                    VcModuleBranchInfoList branchList = dom.getVcModuleBranchInfo(context, 1, path, recursive, config);

                    for (int idx=0; idx < branchList.size(); idx++) {
                        HashMap map = new HashMap();
                        StringBuilder sb = new StringBuilder(256);
                        VcModuleBranchInfo branchInfo = (VcModuleBranchInfo) branchList.getElement(idx);
                        moduleName = branchInfo.getBranch();
                        relPath = branchInfo.getName()+"/"+moduleName;
                        comment = branchInfo.getComment();

                        if (comment.equalsIgnoreCase("null"))
                            comment = _space;

                        tagsList = branchInfo.getTags();
                        if (tagsList.size() == 0) {
                            tags = _space;
                        } else {
                            tags = FrameworkUtil.join(tagsList, ",");
                        }

                        config = moduleName;
                        sb.append("<img src='../common/images/iconSmallDSModuleBranch.gif'");
                        sb.append(" id=\"").append(moduleName).append("\"");
                        sb.append(" />");
                        sb.append(moduleName);
                        map.put("name", sb.toString());

                        map.put("version", moduleName);
                        map.put("comment", comment);
                        map.put("tags", tags);
                        map.put("id", joinex("branch",fromPage,moduleName,path,config,objectId,relPath,_none));
                        map.put("globalType", globalType);
                        map.put("storePath", storePath);
                        retMapList.add(map);
                    }
                }
            } else if (!fromPage.equals("VCModuleContents")) {
                dom = DomainObject.newInstance(context, objectId); //!
                VcModuleInfoList moduleList = dom.getVcModuleInfo(context, 1, path, recursive, config);
                VcModuleInfo modInfo;

                for (int idx=0; idx < moduleList.size(); idx++) {
                    HashMap map = new HashMap();
                    StringBuilder sb = new StringBuilder(256);
                    modInfo = (VcModuleInfo) moduleList.getElement(idx);
                    moduleName = modInfo.getName();
                    comment = modInfo.getComment();
                    type = modInfo.getType();

                    map.put("globalType", globalType);
                    map.put("storePath", storePath);

                    if (type.equalsIgnoreCase("category")) {
                        sb.append("<img src='../common/images/iconSmallDSCategory.gif'");
                        sb.append(" id=\"").append(moduleName).append("\"");
                        sb.append(" >");
                        sb.append("</img>");
                        sb.append(moduleName);

                        // IR-154937V6R2013x - the category path, similar to a folder
                        // should include path prior to the leaf name: e.g Modules/cat
                        //String catPath = moduleName;
                        String catPath = modInfo.getPath().replaceAll("^syncs?:///", "");
                        // 'sync:///Modules/chips' -> 'Modules/chips'

                        map.put("name", sb.toString());
                        map.put("version", _space);
                        map.put("id", joinex("category",fromPage,moduleName,catPath,config,objectId,_space,_none));
                        retMapList.add(map);
                    } else {
                        sb.append("<img src='../common/images/iconSmallDSModule.gif'");
                        sb.append(" id=\"").append(moduleName).append("\"");
                        sb.append(" />");
                        sb.append(moduleName);
                        map.put("name", sb.toString());

                        modPath = moduleName;
                        if (storePath.equals("/")) {
                            relPath = "Modules/"+moduleName;
                            modPath = "Modules/"+moduleName;
                        } else {
                            relPath = moduleName;
                        }

                        map.put("version", _space);
                        map.put("comment", comment);
                        map.put("id", joinex("module",fromPage,moduleName,modPath,config,objectId,relPath,_none));
                        map.put("storePath", "Modules");
                        retMapList.add(map);
                    }
                }
            } // if (!fromPage.equals("VCModuleContents"))
        } // if (initialRequest)


        //***************************************************************************
        // A subsequent call to expand (one or all) nodes in the structure browser
        //***************************************************************************
        if (! initialRequest) {
            String id = (String) programMap.get("objectId");
            java.util.StringTokenizer strtk = new java.util.StringTokenizer(id, "$");
            type = strtk.nextToken();
            fromPage = strtk.nextToken();
            moduleName = strtk.nextToken();
            path = strtk.nextToken();
            config = strtk.nextToken();
            objectId = strtk.nextToken();


            // -----------------------------------------------------------------
            // Process Module Category
            // -----------------------------------------------------------------
            if (type.equalsIgnoreCase("category")) {
                dom = DomainObject.newInstance(context, objectId);
                VcModuleInfoList moduleList;
                VcModuleInfo modInfo;
                // here we need the correct relative path under the store
                String subPath = getRelativePath(path, storePath);
                //jpoTrace("@category path="+path + " storePath="+storePath + " subPath="+subPath);
                moduleList = dom.getVcModuleInfo(context, 1, subPath, recursive, config);

                for (int idx=0; idx < moduleList.size(); idx++) {
                    HashMap map = new HashMap();
                    StringBuilder sb = new StringBuilder(256);
                    modInfo = (VcModuleInfo) moduleList.getElement(idx);
                    moduleName = modInfo.getName();
                    if (storePath.equals("/")) {
                        relPath = "Modules/" + moduleName;
                    }

                    comment = modInfo.getComment();
                    String categoryOrModule = modInfo.getType();

                    if (moduleName.contains("/")) {
                        moduleName = getLeafName(moduleName);
                        if (categoryOrModule.equalsIgnoreCase("category")) {
                            sb.append("<img src='../common/images/iconSmallDSCategory.gif'");
                            sb.append(" id=\"").append(moduleName).append("\"");
                            sb.append(" >");
                            sb.append("</img>");
                            sb.append(moduleName);
                            map.put("name", sb.toString());
                            String catPath = path +"/"+ moduleName;
                            map.put("id", joinex("category",fromPage,moduleName,catPath,config,objectId,relPath,_none));
                            map.put("globalType", globalType);
                            retMapList.add(map);
                        } else if (categoryOrModule.equalsIgnoreCase("module")) {
                            sb.append("<img src='../common/images/iconSmallDSModule.gif'");
                            sb.append(" id=\"").append(moduleName).append("\"");
                            sb.append(" />");
                            sb.append(moduleName);
                            map.put("name", sb.toString());
                            map.put("comment", comment);
                            modPath = path +"/"+ moduleName;
                            //IR-185195V6R2013x - get the relative path
                            //relPath = modPath;
                            relPath = getRelativePath(modPath, storePath);
                            map.put("id", joinex("module",fromPage,moduleName,modPath,config,objectId,relPath,_none));
                            map.put("globalType", globalType);
                            retMapList.add(map);
                        }
                    }
                } // iterating through the modules
            } // type == category


            // -----------------------------------------------------------------
            // Process Module
            // -----------------------------------------------------------------
            if (type.equalsIgnoreCase("module")) {
                dom = DomainObject.newInstance(context, objectId);
                VcModuleBranchInfo branchInfo;

                if (config.equalsIgnoreCase("null"))
                    config = null;

                // here we need the correct relative path under the store
                String subPath = getRelativePath(path, storePath);
                //jpoTrace("@module path="+path + " storePath="+storePath + " subPath="+subPath);
                VcModuleBranchInfoList branchList = dom.getVcModuleBranchInfo(context, 1, subPath, recursive, config);

                for (int idx=0; idx < branchList.size(); idx++) {
                    HashMap map = new HashMap();
                    StringBuilder sb = new StringBuilder(256);
                    branchInfo = (VcModuleBranchInfo) branchList.getElement(idx);
                    moduleName = branchInfo.getBranch();
                    if (storePath.equals("/")) {
                        relPath = "Modules/" + moduleName;
                    } else {
                        relPath = moduleName;
                    }

                    comment = branchInfo.getComment();
                    if (comment.equalsIgnoreCase("null"))
                        comment = _space;   //fzz: why not _empty?

                    tagsList = branchInfo.getTags();
                    if (tagsList.size() == 0) {
                        tags = _space;
                    } else {
                        tags = FrameworkUtil.join(tagsList, ",");
                    }
                    config = moduleName;

                    sb.append("<img src='../common/images/iconSmallDSModuleBranch.gif'");
                    sb.append(" id=\"").append(moduleName).append("\"");
                    sb.append(" />");
                    sb.append(moduleName);

                    map.put("name", sb.toString());
                    map.put("version", moduleName);
                    map.put("comment", comment);
                    map.put("tags", tags);
                    map.put("id", joinex("branch",fromPage,moduleName,path,config,objectId,relPath,_none));
                    map.put("globalType", globalType);
                    retMapList.add(map);
                } // iterating through the module branches
            } // if type == module


            // -----------------------------------------------------------------
            // Process Module Branch
            // -----------------------------------------------------------------
            if (type.equalsIgnoreCase("branch")) {
                dom = DomainObject.newInstance(context, objectId);
                String version;
                String moduleActions;

                if (config.equalsIgnoreCase("null"))
                    config = null;

                // here we need the correct relative path under the store
                String subPath = getRelativePath(path, storePath);
                //jpoTrace("@branch path="+path + " storePath="+storePath + " subPath="+subPath);
                VcModuleVersionInfoList versionList = dom.getVcModuleVersionInfo(context, 1, subPath, recursive, config);

                for (int idx=0; idx < versionList.size(); idx++) {
                    HashMap map = new HashMap();
                    StringBuilder sb = new StringBuilder(256);
                    VcModuleVersionInfo versionInfo = (VcModuleVersionInfo) versionList.getElement(idx);
                    version = versionInfo.getVersion();

                    if (path.equals(_space)) {
                        relPath = versionInfo.getName() + "/" + moduleName + "/" + version;
                    } else {
                        // IR-195627V6R2014
                        //relPath = path + "/" + moduleName + "/" + version;
                        relPath = subPath + "/" + version;
                    }

                    comment = versionInfo.getComment();
                    tagsList = versionInfo.getTags();
                    if (tagsList.size() == 0) {
                        tags = _space;
                    } else {
                        tags = FrameworkUtil.join(tagsList, ",");
                    }

                    config = version;
                    String checkoutAccess = dom.getInfo(context, CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);

                    if ("TRUE".equalsIgnoreCase(checkoutAccess)) {
                        StringBuilder sbActions = new StringBuilder();

                        String downloadURL = "javascript:callCheckout('"
                            + objectId + "', 'download', '" + path + "', 'generic'"
                            + ", null, null, null, null, null, '" + version + "');";
                        sbActions.append("<a href=\"").append(downloadURL).append("\">");
                        sbActions.append("<img border='0' src='../common/images/iconActionDownload.gif'></img></a>&#160;");
                        moduleActions = sbActions.toString();  //? why the closing tag for img, not '/>' ?
                    } else {
                        moduleActions = _space;
                    }

                    sb.append("<img src='../common/images/iconSmallDSModuleVersion.gif'");
                    sb.append(" id=\"").append(version).append("\"");
                    sb.append(" />");
                    sb.append(version);

                    map.put("name", sb.toString());
                    map.put("version", version);
                    map.put("comment", comment);
                    map.put("tags", tags);
                    map.put("actions", moduleActions);
                    map.put("id", joinex("version",fromPage,version,path,config,objectId,relPath,_none));
                    map.put("globalType", globalType);
                    map.put("storePath", storePath);
                    retMapList.add(map);
                } // iterating through versions
            } // if type == branch


            // -----------------------------------------------------------------
            // Process Module Version
            // -----------------------------------------------------------------
            if (type.equalsIgnoreCase("version")) {
                dom = DomainObject.newInstance(context, objectId);
                // here we need the correct relative path under the store
                String subPath = getRelativePath(path, storePath);
                //jpoTrace("@version path="+path + " storePath="+storePath + " subPath="+subPath);
                VcModuleHrefInfoList hrefList = dom.getVcModuleHrefInfo(context, 1, subPath, config, recursive);

                VcModuleHrefInfo hrefInfo;
                StringBuilder sb;
                String version;

                for (int idx=0; idx < hrefList.size() ;idx++) {
                    HashMap map = new HashMap();
                    sb = new StringBuilder(1024);
                    hrefInfo = (VcModuleHrefInfo) hrefList.getElement(idx);
                    name = hrefInfo.getHrefName();
                    relPath = path + "/" + config + "/" + name;
                    version = hrefInfo.getVersion();
                    tags = hrefInfo.getTag();
                    hrefname = hrefInfo.getPath();
                    name = getLeafName(name);

                    sb.append("<img src='../common/images/iconSmallDSHref.gif'");
                    sb.append(" id=\"").append(hrefname).append("\"");
                    sb.append(" />");
                    sb.append(name);
                    map.put("name", sb.toString());

                    // Do not provide any actions to external modules.
                    // these are identified by having a URL with ExternalModule as the first part.
                    // Also, blank out the versionin that case, else it ends up as ".0"
                    sb.delete(0, sb.length());
                    if (hrefname.matches("^\\w+://[^/]*/ExternalModule/.+")) {
                        version = "";
                    } else {

                        //? change getVCModuleContents to doGetVCModuleContents in two hrefs below
                        sb.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentVCHrefNavigateProcess.jsp?type=static&amp;hrefPath="+hrefname+"&amp;fromPage=HrefNavigate&amp;name="+hrefInfo.getName()+"&amp;version="+version+"&amp;tags="+tags+"&amp;storePath="+storePath+"&amp;table=APPVCHrefContentsSummary&amp;program=emxVCDocumentUI:getVCModuleHrefsNavigate&amp;expandProgram=emxVCDocumentUI:getVCModuleContents&amp;selection=single&amp;toolbar=APPVCFolderContentActions&amp;emxExpandFilter=3','730','450');\">");
                        sb.append("<img border='0' src='../common/images/iconSmallDSStaticHref.gif' />");
                        sb.append("</a>&#160;");

                        sb.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentVCHrefNavigateProcess.jsp?type=dynamic&amp;hrefPath="+hrefname+"&amp;fromPage=HrefNavigate&amp;name="+hrefInfo.getName()+"&amp;tags="+tags+"&amp;storePath="+storePath+"&amp;table=APPVCHrefContentsSummary&amp;program=emxVCDocumentUI:getVCModuleHrefsNavigate&amp;expandProgram=emxVCDocumentUI:getVCModuleContents&amp;selection=single&amp;toolbar=APPVCFolderContentActions&amp;emxExpandFilter=3','730','450');\">");
                        sb.append("<img border='0' src='../common/images/iconSmallDSDynamicHref.gif' />");
                        sb.append("</a>&#160;");

                    }

                    map.put("actions", sb.toString());
                    map.put("version", version);
                    map.put("tags", tags);
                    map.put("hrefname", hrefname);
                    map.put("id", joinex("href",fromPage,hrefname,path,_space,objectId,relPath,_none));
                    map.put("globalType", "module");
                    retMapList.add(map);
                } // iterating through hrefs
            }  // if type == version"

        } // not an initialRequest


        return retMapList;
   }


    /**
     * This wrapper around doGetVCFolderContents supports multi-level expansion
     * @param context - Matrix Context
     * @param args - packed argument maps
     * @return - mapList with results
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getVCFolderContents(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        boolean initialRequest = getInitialRequest(programMap);
        final short expandLevel = getExpandLevel((String) programMap.get("expandLevel"));
        String sbLevel = (String) programMap.get("level");

        jpoTrace("initialRequest="+initialRequest +" level="+sbLevel +" expandLevel="+expandLevel);

        MapList retList = (MapList) doGetVCFolderContents(context, args);
        // -----------------------------------------------------------
        // if expansion is not requested or not possible - return here
        // -----------------------------------------------------------
        if (retList.isEmpty() || expandLevel==1)
            return retList;


        // -----------------------------------------------------------
        // Handle multi-level expansion.
        // For future dev. see comments in getVCAllContents
        // -----------------------------------------------------------
        short level = 1;
        try {
            level = (short) (Integer.parseInt(sbLevel) + 1);
        } catch (NumberFormatException e) {
            level = 1; // red.
        }

        MapList finalList = new MapList();

        // iterate through the result and expand each object
        for (Object obj : retList) {
            Map map = (Map) obj;
            finalList.add(map);

            String id = (String) map.get("id");
            programMap.put("objectId", id);
            //jpoTrace("Expanding objectId " + id);
            expandVCObject(context, level, expandLevel, programMap, finalList, false);
        }

        // it's true, if we're called directly
        if (_addExpandMultiLevelFlag)
            addExpandMultiLevelFlag(finalList);

        return finalList;
    }


    /**
     * This method is used to get the Branches for the particular version object
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds the following input arguments: 0 - objectList MapList
     * @return Object
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    MapList doGetVCFolderContents(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String fromPage = (String) programMap.get("fromPage");
        String globalType = "fileFolder";
        String folderPath = "";
        String vcName = "";
        String methodToCall = "";
        String versionId = "";
        String fileOrFolder = "";
        boolean foldersOnly = false;
        boolean showFileVersions = true;

        final boolean recursive = false; //fzz:future
        final boolean initialRequest = getInitialRequest(programMap);

        //final short expandLevel = getExpandLevel((String) programMap.get("expandLevel"));
        //final String level = (String) programMap.get("level");

        if ("FolderContentsFoldersOnly".equals(fromPage)) {
            foldersOnly = true;
        }

        MapList retMapList = new MapList();
        String objectId = (String) programMap.get("objectId");

        //jpoTrace("fromPage=" + fromPage + " objectId=" + objectId);

        // nothing to do if objectId isn't avail.
        if (objectId == null || objectId.isEmpty()) {
            return retMapList;
        }


        // -------------------------------------------------------
        // subsequent call
        // -------------------------------------------------------
        if (!initialRequest) {
            String id = (String) programMap.get("objectId");
            java.util.StringTokenizer strtk = new java.util.StringTokenizer(id, "$");
            fileOrFolder = strtk.nextToken();
            fromPage = strtk.nextToken();
            folderPath = strtk.nextToken();
            if (_none.equals(folderPath)) {
              folderPath = _empty;
            } else {
                folderPath = folderPath.replaceFirst("^/", "");
            }

            if (fileOrFolder.equalsIgnoreCase("file") && matchesOneOf(fromPage, "FolderContents")) {
                vcName = strtk.nextToken();
                objectId = strtk.nextToken();
            } else if (fileOrFolder.equalsIgnoreCase("file") && matchesOneOf(fromPage,
                    "FileVersions","FolderContentBranches","FileBranchSummary","searchVCDocuments")) {
                vcName = strtk.nextToken();
                objectId = strtk.nextToken();
                versionId = strtk.nextToken();
            } else {
                objectId = strtk.nextToken();  //fzz is it 'module' ?
            }
            //jpoTrace("objectId=" + objectId + " folderPath=" + folderPath);
        } // not initialRequest


        HashMap map = null;
        String fileName = "";
        String folderName = "";
        String filePath = "";
        DomainObject object = DomainObject.newInstance(context, objectId);
        //StringList fileList =  new StringList();
        StringList folderList = new StringList();
        VcFileBranchVersionInfo bvInfo = null;
        VcFileInfoList fileInfoList = null;
        String configName = (String) programMap.get("configName");
        StringList listSelects = null;
        Map mapFolder = null;
        String sCheckoutAccess = "FALSE";

        // -------------------------------------------------------------------
        // Counterintuitively and quite unfortunately we get here when we
        // execute 'Expand All' (or Expand to X level) at the root of the
        // DS Vault. There we typically have special folders Projects and Modules,
        // and while both are sort-of folders 'Modules' and anything in it must
        // be processed by a different routine. Hence this ugly redirection.
        // The alternative approach involving managing work at the higher level
        // (in doGetVCAllContents), calling appropriate methods and marshalling
        // results was explored and found to have its own disadvantages.
        // -------------------------------------------------------------------
        if (!initialRequest && moduleTypeObject(context, programMap)) {
            //programMap.put("level", "0");
            programMap.put("globalType", "module");
            programMap.put("fromPage", "VCModuleContents");
            // I'd much prefer to call doGetVCModuleContents directly,
            // but doGetVCAllContents handles all args and prep work.
            // We must disable expansion though!
            jpoTrace("Redirecting " + objectId + " to doGetVCAllContents");
            programMap.put("expandLevel", "1");
            return doGetVCAllContents(context, JPO.packArgs(programMap));
        }
        // ----------------------------------------------------------------

        if (fromPage != null) {
            if (fromPage.equals("FolderContents")) {
                if (fileOrFolder.equals("file")) {
                    programMap.put("objectId", objectId);
                    programMap.put("folderPath", folderPath);
                    programMap.put("vcName", vcName);
                    programMap.put("fromPage", fromPage);
                    programMap.put("globalType", globalType);
                    return getVCFileVersions(context, JPO.packArgs(programMap));
                } else {
                    if (configName != null && !configName.isEmpty()) {
                        folderPath = FrameworkUtil.decodeURL(
                                        (String) programMap.get("folderPath"),
                                        com.matrixone.servlet.Framework.getEncoding());
                    }
                    listSelects = new StringList();
                    DomainObject.MULTI_VALUE_LIST.add("vcfolder[1].subfolder["+folderPath+"].subfolder");
                    listSelects.add("vcfolder[1].subfolder["+folderPath+"].subfolder");
                    // listSelects.add("vcfolder[1].store.path");
                    listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                    mapFolder = object.getInfo(context, listSelects);

                    sCheckoutAccess = (String) mapFolder.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                    try {
                        folderList = (StringList) mapFolder.get("vcfolder[1].subfolder["+folderPath+"].subfolder");
                    } catch (ClassCastException CCE) {
                        String sTemp = (String) mapFolder.get("vcfolder[1].subfolder["+folderPath+"].subfolder");
                        folderList.add(sTemp);
                    }
                    fileInfoList = object.getVcFolderFileInfo(context, 1, folderPath, _empty);
                    String table = (String) programMap.get("table");
                    if (matchesOneOf(table, "APPVCFolderContentsSummary","APPVCFileFolderSearchResults")) {
                        showFileVersions = false;
                    } else {
                        showFileVersions = true;
                    }
                }
            } else if (fromPage.equalsIgnoreCase("FileVersions")) {
                programMap.put("objectId", objectId);
                programMap.put("folderPath", folderPath);
                programMap.put("vcName", vcName);
                programMap.put("fromPage", "FolderContents");
                programMap.put("versionid", versionId);
                programMap.put("globalType", globalType);
                return getVCFileVersionBranches(context, JPO.packArgs(programMap));
            } else if (fromPage.equals("FolderContentBranches")) {
                programMap.put("objectId", objectId);
                programMap.put("folderPath", folderPath);
                programMap.put("vcName", vcName);
                programMap.put("fromPage", fromPage);
                programMap.put("branchId", versionId);
                programMap.put("globalType", globalType);
                return getVCFileVersions(context, JPO.packArgs(programMap));
            } else if (fromPage.equals("FolderConfigurations")) {
                listSelects = new StringList();
                listSelects.add("vcfolder[1].otherconfig["+configName+"].subfolder");
                listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                mapFolder = object.getInfo(context, listSelects);
                sCheckoutAccess = (String)mapFolder.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                try {
                    folderList = (StringList) mapFolder.get("vcfolder[1].otherconfig["+configName+"].subfolder");
                } catch (ClassCastException CCE) {
                    String sTemp = (String) mapFolder.get("vcfolder[1].otherconfig["+configName+"].subfolder");
                    folderList.add(sTemp);
                }
                fileInfoList = object.getVcFolderFileInfo(context, 1, _empty, configName);
                showFileVersions = true;
            } else {
                // fromPage == folderContentsAll
                listSelects = new StringList();
                listSelects.add(CommonDocument.SELECT_VCFOLDER_EXISTS);
                // Bug 334245 - Check whether it is a file
                listSelects.add(CommonDocument.SELECT_VCFILE_EXISTS);
                listSelects.add(CommonDocument.SELECT_VCMODULE_EXISTS);
                listSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                Map objInfo = object.getInfo(context, listSelects);
                String existsFile = (String) objInfo.get(CommonDocument.SELECT_VCFILE_EXISTS);
                String existsFolder = (String) objInfo.get(CommonDocument.SELECT_VCFOLDER_EXISTS);
                String existsModule = (String) objInfo.get(CommonDocument.SELECT_VCMODULE_EXISTS);
                sCheckoutAccess = (String) objInfo.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
                showFileVersions = false;
                // Bug 334245 - End

                if ("TRUE".equals(existsFolder)) {
                    // this may run excruciatingly slow for a Project Folder
                    fileInfoList = object.getVcFolderFileInfo(context, 1, _empty, _empty);
                    //commented out since the file info is available in VCFileInfo
                    //fileList = object.getInfoList(context, "vcfolder[1].vcfile");
                    folderList = object.getInfoList(context, "vcfolder[1].subfolder");
                } else if ("TRUE".equals(existsFile)) {
                   // Bug 334245 - if it is a file get File Branch Info
                   programMap.put("objectId", objectId);
                   programMap.put("fromPage", fromPage);
                   if (_empty.equals(vcName)) {
                       vcName = (String) object.getInfo(context, "vcfile[1].vcname");
                   }
                   programMap.put("vcName", vcName);
                   programMap.put("branchId", versionId);
                   programMap.put("globalType", globalType);
                   return getVCFileVersions(context, JPO.packArgs(programMap));
                } else if ("TRUE".equals(existsModule)) {
                    programMap.put("level", null);  //?
                    programMap.put("globalType", "module");
                    programMap.put("fromPage", "VCModuleContents");
                    //?  doGetVCModuleContents
                    return getVCModuleContents(context, JPO.packArgs(programMap));
                }
                // Bug 334245 - End
                fromPage = "FolderContents";
                showFileVersions = false;
            }

        } // if (fromPage != null)


        StringList associatedList = null;

        if ((fileInfoList != null) && !foldersOnly) {
            for (int idx = 0; idx < fileInfoList.size(); idx++) {
                map = new HashMap();
                associatedList = new StringList();
                VcFileInfo info = (VcFileInfo) fileInfoList.getElement(idx);
                filePath = info.getPath();
                fileName = getLeafName(filePath);
                String versionid = "Blank";
                map.put("type", "file");
                map.put("name", fileName);

                if (showFileVersions) {
                    associatedList = info.getIds();
                    versionid = info.getVersionId();
                    map.put("Description", info.getDescription());
                    map.put("Comments", info.getComment());
                    map.put("globalType", globalType);
                    StringList versiontagList = new StringList();
                    if (info.isLatest()) {
                        versiontagList.add("Latest");
                    }
                    versiontagList.addAll(info.getTags());
                    map.put("versiontag", versiontagList);
                    map.put("versionId", info.getVersionId());
                } else {
                    map.put("Description", _empty);
                    map.put("Comments", _empty);
                    map.put("versiontag", _empty);
                    map.put("versionId", _empty);
                }

                if (folderPath.isEmpty() || folderPath.equals("/")) {
                    folderPath = _none;
                    map.put("folderPath", _empty);
                } else {
                    folderPath = folderPath.replaceFirst("^/", "");
                    map.put("folderPath", folderPath);
                }

                map.put("format", info.getFormat());
                map.put("fromPage", fromPage);
                map.put("associatedId", associatedList);

                String associate = _none;
                if (associatedList != null && associatedList.size() > 0) {
                    associate = associatedList.get(0).toString();
                }
                map.put("id", joinex("file",fromPage,folderPath,fileName,objectId,versionid,associate));
                map.put("checkoutaccess", sCheckoutAccess);
                map.put("icon", "iconSmallFile.gif");
                map.put("showFileVersions", Boolean.valueOf(showFileVersions));
                if(configName != null && !configName.isEmpty()) {
                    map.put("configName", configName);
                }
                map.put("globalType", globalType);
                retMapList.add(map);
            } // for (idx from 0 to fileInfoList.size())
        } //  if ((fileInfoList != null) && !(foldersOnly))


        if (folderList != null) {
            for (int i=0; i < folderList.size(); i++) {
                map = new HashMap();
                folderName = (String) folderList.get(i);
                folderName = folderName.replaceFirst("^/", "");

                associatedList = new StringList();

                if (! ReservedDesignSyncFile(folderName)) {
                    String folderPathName = _empty;
                    map.put("type", "folder");
                    map.put("name", folderName);
                    map.put("Description", _empty);
                    map.put("Comments", _empty);
                    if (folderPath.isEmpty()) {
                        map.put("folderPath", folderName);
                        folderPathName = folderName;
                        associatedList = object.getInfoList(context,"vcfolder[1].subfolder["+folderName+"].businessobject");
                    } else {
                        if (folderPath.equals(_none))
                            folderPath = _empty;
                        else
                            folderPath = folderPath.replaceFirst("^/", "");

                        if (folderPath.isEmpty())
                            folderPathName = folderName;
                        else
                            folderPathName = folderPath + "/" + folderName;

                        map.put("folderPath",folderPathName);
                        associatedList = object.getInfoList(context,"vcfolder[1].subfolder["+folderPathName+"].businessobject");
                    }

                    if (associatedList != null && associatedList.size() > 0
                                && !"[]".equals(associatedList.get(0).toString()) && !folderPathName.equals("Modules")) {
                        map.put("id", joinex("folder",fromPage,folderPathName,objectId,_space,associatedList.get(0).toString()));
                        map.put("associatedId", associatedList);
                    } else {
                        map.put("id", joinex("folder",fromPage,folderPathName,objectId,_space,_none));
                    }

                    map.put("format", _empty);
                    map.put("versionId", _empty);
                    map.put("checkoutaccess", sCheckoutAccess);
                    map.put("icon", "iconSmallFolder.gif");
                    map.put("versiontag", _empty);
                    map.put("fromPage", fromPage);
                    map.put("showFileVersions", Boolean.valueOf(showFileVersions));
                    if (configName != null && !configName.isEmpty())
                        map.put("configName", configName);
                    map.put("globalType", globalType);
                    retMapList.add(map);
                } // if folderName is not DS internal object
            } // for (int i from 0 to folderList.size())
        } // if (folderList != null)


        return retMapList;
    }


    // --------------------------------------------------------------------
    // Miscellaneous Utilities
    // --------------------------------------------------------------------
    /**
     * moduleTypeObject - determine whether the given object is some kind of the Module object
     * @param context - Matrix Context
     * @param programMap - the arg map received by the caller
     * @return - a boolean result
     * @throws MatrixException
     */
    static boolean moduleTypeObject(Context context, Map programMap) throws MatrixException {
        // we cannot have a moduleType object on initial request,
        // but this condition is checked by the sole caller.
        //if (getInitialRequest(programMap))
        //    return false;

        String fromPage = (String) programMap.get("fromPage");
        String storePath = (String) programMap.get("storePath");
        String objectId = (String) programMap.get("objectId");
        String objectIdModule = (String) programMap.get("objectIdModule");

        if (storePath == null) {
            String storeName = (String) programMap.get("storeName");
            if (storeName == null) {
                jpoTrace("WARNING: cannot determine type for " + objectId);
                return "VCModuleContents".equals(fromPage);
            }
            storePath = getStorePath(context, storeName);
        }

        // to consider it must be a top-level store
        if (!storePath.isEmpty() && !storePath.equals("/"))
            return false;

        java.util.StringTokenizer strtk = new java.util.StringTokenizer(objectId, "$");
        String dsType = strtk.nextToken(); // file,folder,category,module,branch,version,href...
        fromPage = strtk.nextToken();
        String folderPath = strtk.nextToken();
        String modulePath = strtk.nextToken();

        if (dsType.equals("module") || dsType.equals("category")) {
            return true;
        } else if (dsType.equals("folder")) {
            return (folderPath.matches("^/?Modules(/.*)?$"));
        } else if (dsType.equals("branch")) {
            return (modulePath.matches("^/?Modules/.+$"));
        } else if (dsType.equals("version")) {
            return (modulePath.matches("^/?Modules/.+$"));
        } else if (dsType.equals("href")) {
            return (modulePath.matches("^/?Modules/.+$")); //?
        }

        return false;
    }


    /**
     * expandVCObject - recursive method for expansion of the DS Object
     * @param level - the level of the object in the Structure Browser
     * @param expandLevel - the level to expand to (max-depth)
     * @param programMap - the arg map used for calling other methods
     * @param retList - the resulting MapList used to accumulate the hierarchy
     * @param moduleType - whether the given object is some kind of the DS Module
     * @return - nothing
     */
    void expandVCObject(Context context, short level, short expandLevel, Map programMap, MapList retList, boolean moduleType)
    throws Exception
    {
        // this should never happen
        if (level >= expandLevel) {
            throw new Exception ("expandVCObject called with level >= expandLevel : " + level + " & " +expandLevel);
        }

        String objectId = (String) programMap.get("objectId");
        //jpoTrace("level="+level + " expandLevel="+expandLevel + " moduleType="+moduleType + " retList: " + retList.size());

        MapList xList;
        if (moduleType)
            xList = doGetVCModuleContents(context, JPO.packArgs(programMap));
        else
            xList = doGetVCFolderContents(context, JPO.packArgs(programMap));

        //jpoTrace("Expanded " + objectId + " got " + xList.size() + " items");

        if (xList.isEmpty()) {
            //jpoTrace("Exit on empty result");
            return;
        }

        while (level++  <  expandLevel) {
            for (Object obj : xList) {
                Map map = (Map) obj;
                map.put("level", Integer.toString(level));
                retList.add(map);

                if (level < expandLevel) {
                    String id = (String) map.get("id");
                    programMap.put("objectId", id);
                    jpoTrace("@"+level + ": Add+RecurseTo " + id);
                    expandVCObject(context, level, expandLevel, programMap, retList, moduleType);
                } else {
                    jpoTrace("@"+level + ": Add-NoRecurse " + (String) map.get("id"));
                }
            }
            return;
        }

        return;
    }

    /**
     * getInitialRequest - a wrapper for the trivial shared code
     * @param programMap - the map received from the GUI
     * @return - a boolean indicating the type of request
     */
    static boolean getInitialRequest(Map programMap) {
        String level = (String) programMap.get("level");
        String objectId = (String) programMap.get("objectId");

        // if level==null this is the initial call just before
        // the SB is displayed for the first time.
        // if level=0 (or x) this is probably a subsequent call made
        // by using the "Expand to level x" gadget on the GUI.
        // For all practical purposes this is treated as initial call.
        // If objectId looks like a bunch of dollar-separated tokens,
        // this is a subsequent expand request - we overload this variable
        // with object info throughout the program. (see calls to 'joinex').

        boolean initialRequest =
            ((level==null || level.equals("0")) && !objectId.contains("$"));

        return initialRequest;
    }


    /**
     * getExpandLevel - a wrapper for the trivial shared code
     * @param expandLevel - argument passed from the Structure Browser
     * @return - short integer conditionally derived from the string
     */
    static short getExpandLevel(String expandLevel) {
        short xlevel = 1;

        if ("All".equals(expandLevel)) {
            xlevel = Short.MAX_VALUE;
        } else {
            try {
                xlevel = (short) Integer.parseInt(expandLevel);
            } catch (NumberFormatException e) {
                xlevel = 1; // redundant, but ok
            }
        }

        return xlevel;
    }

    /**
     * getLeafName - given the UNIX path return the leaf-name
     * also remove the trailing slash, if any
     * @param path - relative or absolute UNIX path
     * @return - the leaf name in the path
     */
    static String getLeafName(String path) {
        // replacements must be done in this order
        return path.replaceFirst("/$", "").replaceFirst("^.*/", "");

        // and this would be perfectly fine as well:
        //File f = new File(path);
        //return f.getName();
    }

    /**
     * getRelativePath - wraps the common case of subtracting one path from the other
     * @param path - the containing UNIX path
     * @param subpath - a sub-path of the first argument
     * @return - the relative UNIX path - the result of subtraction
     * Ex: /home/users/dsws/modules/chip, /home/users/dsws => modules/chip
     * Either argument can be absolute or relative path.
     */
    static String getRelativePath(String path, String subpath) {

        if (subpath.equals("/") || subpath.isEmpty()) {
            return path.replaceFirst("^/", "");
        }

        // consider any combinations of leading and
        // trailing slashes and make it safe
        String _path = path.replaceAll("^/|/$", "");
        String _subpath = subpath.replaceAll("^/|/$", "");
        if (_path.matches("^" + _subpath + "(/.+|$)")) {
            return _path.replaceFirst(("^"+_subpath+"/?"), "");
        }

        // normally this won't happen
        return path;
    }


    /**
    * matchesOneOf - case insensitive string match
    * @param str - string to be matched
    * @param values - var string args to compare against
    * @return - boolean indicating whether str matches one of the values
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
    * getStorePath - given the store name return the path
    * @param context - the Matrix Context
    * @param name - name of the Store
    * @throws MatrixException
    * @return - the Path attribute of the Store
    */
    static String getStorePath(Context context, String name) throws MatrixException {
        Store store = new Store(name);
        store.open(context);
        String path = store.getPath(context);
        //store.close(context); //?
        return path;
    }


    /**
    * ReservedDesignSyncFile - checks whether the given name is one of the
    * special DesignSync files or folders that must not be captured
    * @param fname - name of the DS file or folder
    * @return - boolean indicating whether the given name is on the list
    */
    static boolean ReservedDesignSyncFile(String fname) {
        String[] reserved = {
                "sync_server_trace.log",
                "sync_project.txt",
                "Partitions",
                "Backup.sync",
                "Import.sync",
                "Export.sync"
        };

        // the case-sensitive exact match
        for (String name : reserved) {
            if (name.equals(fname))
                return true;
        }

        return false;
    }

    /**
    * Join the variable number of strings using the fixed delimiter
    * @param values - var string args to join together
    * @return - concatenated strings separated with given delimiter
    */
    static String joinex(String ... values) {
        final String delim = "$";
        StringBuffer sb = new StringBuffer();

        for (int i=0; i<values.length; ) {
            sb.append(values[i]);
            if (++i < values.length)
                sb.append(delim);
        }
        return sb.toString();
    }

    /**
    * Add a specific map for the Structure Browser to the MapList
    * @param mapList - MapList returned to the Structure Browser
    * @return - the same mapList with added Map
    */
    static MapList addExpandMultiLevelFlag(MapList mapList) {
        HashMap exmlJPO = new HashMap();
        exmlJPO.put("expandMultiLevelsJPO", "true");
        mapList.add(exmlJPO);
        return mapList;
    }


    // -----------------------------------------------------
    // devel/debug only
    // -----------------------------------------------------
    static void jpoTrace(String msg) {
        if (! _debug)
            return;

        final String clazz = "VCDocumentUI";

        // get the name of the calling method
        String caller = Thread.currentThread().getStackTrace()[2].getMethodName();
        StringBuilder sb = new StringBuilder();
        //sb.append("[").append(caller).append("] ");
        sb.append("[").append(clazz).append("::").append(caller).append("] ");
        sb.append(msg);
        msg = sb.toString();

        System.out.println (msg);
        return;
    }

}


