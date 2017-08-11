/*
 *  emxQuickFileAccessBase.java
 *
 * Copyright (c) 2005-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;
import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

/**
 * The <code>emxQuickFileAccessBase</code> class contains code for checkin.
 *
 * @version C 10.6.SP1 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxQuickFileAccessBase_mxJPO
{

    /** A string constant with the value emxComponentsStringResource. */
    public static final String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";

    /** A string constant setting of the Quick File Versionable. */
    public static final String SETTING_QF_VERSIONABLE         = "setting_QuickFile_Versionable";

    private static final Boolean BOOLEAN_TRUE = Boolean.valueOf("true");

    private static final Boolean BOOLEAN_FALSE = Boolean.valueOf("false");

     /**
      * Constructor.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @since 10.6.SP1
      */
    public emxQuickFileAccessBase_mxJPO (Context context, String[] args) throws Exception {
    }

     /**
      * This method returns the file list that are connected to the object with the specified relationships
      * in the properties
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds no arguments.
      * @return MapList containing quick file access objects
      * @throws Exception if the operation fails.
      * @since 10.6.SP1
      */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getQuickFileObjects(Context context, String[] args) throws Exception {

        MapList returnList = new MapList();

        try {

            HashMap programMap         = (HashMap) JPO.unpackArgs(args);
            String  masterObjectId     = (String) programMap.get("objectId");
            String strRelation        = (String) programMap.get("relationship");
            String strRelList             = "";
            String sel_Title = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_TITLE);

            // Manipulate the string to get the relationship name
            StringList relationList = FrameworkUtil.split(strRelation, ",");
            StringList objectSelects = new StringList();
            StringBuffer sRelName = new StringBuffer(256);
            StringBuffer sRelId = new StringBuffer(256);

            for(int i=0;i<relationList.size();i++){
                String strRel = (String) relationList.get(i);
                sRelName = sRelName.append(strRel.substring(0,(strRel.lastIndexOf(".")+1)));
                sRelName.append("name");
                sRelId = sRelId.append(strRel.substring(0,(strRel.lastIndexOf(".")+1)));
                sRelId.append("id");
                objectSelects.addElement(sRelName.toString());
                objectSelects.addElement(sRelId.toString());
                DomainObject.MULTI_VALUE_LIST.add(sRelName.toString());
                DomainObject.MULTI_VALUE_LIST.add(sRelId.toString());
                sRelName.delete(0,sRelName.length());
                sRelId.delete(0,sRelId.length());
            }//end of for loop
            //Instantiate the context object to get its document objects
            DomainObject masterObject  = DomainObject.newInstance(context, masterObjectId);
            String[] objectIds = new String[] { masterObjectId };
            ArrayList childObjectIds = new ArrayList();
            MapList tempMapList;
            Map tempMap;
            HashMap dataMap;

            //Object Selects
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(CommonDocument.TYPE_DOCUMENTS);

            if(relationList.size() > 0) {
                tempMapList = masterObject.getInfo(context,objectIds,objectSelects);
                StringList  sObjNameList = null;
                StringList  sObjIdList = null;
                StringList  childObjIdList = new StringList();;

                Iterator itr = tempMapList.iterator();
                while(itr.hasNext()) {
                    tempMap = (Map)itr.next();
                    if(tempMap != null) {
                      for(int i=0;i<relationList.size();i++){
                          String strRel = (String) relationList.get(i);
                          strRelList= strRel.substring(strRel.indexOf("[") + 1, strRel.lastIndexOf("]"));
                          sRelName = sRelName.append(strRel.substring(0,(strRel.lastIndexOf(".")+1)));
                          sRelName.append("name");
                          sObjNameList = (StringList)tempMap.get(sRelName.toString());
                          sRelId = sRelId.append(strRel.substring(0,(strRel.lastIndexOf(".")+1)));
                          sRelId.append("id");
                          sObjIdList = (StringList)tempMap.get(sRelId.toString());
                          if(sObjIdList != null)
                          {
                              for(int j=0;j<sObjIdList.size();j++){
                                  dataMap = new HashMap();
                                  dataMap.put("objectId", sObjIdList.get(j));
                                  dataMap.put("objectName", sObjNameList.get(j));
                                  dataMap.put("RelationShip",strRelList);
                                  childObjectIds.add(dataMap);
                                  childObjIdList.addElement((String) sObjIdList.get(j));
                              }//end of for loop
                          }//end of if loop
                          sRelName.delete(0,sRelName.length());
                          sRelId.delete(0,sRelId.length());
                      }//end of for loop
                   }//end of if loop
                }//end of while loop

                String[] arrChildObjectIds = new String[childObjIdList.size()];
                for(int lCounter = 0;lCounter<childObjIdList.size();lCounter++){
                  arrChildObjectIds[lCounter] = (String) childObjIdList.get(lCounter);
                }

                //To add the Suspend Versioning in object selects.
                String sel_SuspendVer = DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_SuspendVersioning));
                StringList objSelects = new StringList(7);
                objSelects.add(DomainConstants.SELECT_NAME);
                objSelects.add(DomainConstants.SELECT_ID);
                objSelects.add(sel_Title);
                objSelects.add(sel_SuspendVer);
                objSelects.add("vcfile");
                objSelects.add("vcfile[1].vcname");
                objSelects.add("vcfile[1].format");
                objSelects.add("vcfile[1].versionid");

                // To get the title of all the child Document objects through only one get info
                MapList titleList = (MapList) DomainObject.getInfo(context,arrChildObjectIds,objSelects);

                //Got the objectId's of the DOCUMENTS objects
                //For each object Id get the file information, using emxCommonFileUI
                HashMap arguments;
                MapList fileMapList;
                DomainObject childObj;
                boolean versionable;
                for(int cnt = 0; cnt < childObjectIds.size(); cnt++) {
                    arguments = new HashMap();
                    dataMap = (HashMap)childObjectIds.get(cnt);
                    arguments.put("objectId", (String)dataMap.get("objectId"));
                    String[] fargs = JPO.packArgs (arguments);
                    String[] argsLst = null;
                    String strObj = (String) dataMap.get("objectId");
                    childObj = DomainObject.newInstance(context,strObj);

                    Map vctitleMap = (Map) titleList.get(cnt);
                    String isVCFile = (String) vctitleMap.get("vcfile");
                    // if this is a VC file - Right now a DesignSync file
                    if (isVCFile.equals("TRUE"))
                    {
                       Map vcMap = new HashMap();
                       vcMap.put("masterId", (String)dataMap.get("objectId"));
                       vcMap.put("id", (String)dataMap.get("objectId"));
                       vcMap.put(DomainConstants.SELECT_ID, strObj);
                       vcMap.put("Context Type", dataMap.get("RelationShip"));
                       vcMap.put("Context Name", dataMap.get("objectName"));
                       vcMap.put("Master Title", (String) vctitleMap.get(sel_Title));
                       vcMap.put("level", "1");
                       vcMap.put("isVCFile", "TRUE");
                       vcMap.put(CommonDocument.SELECT_TITLE, vctitleMap.get("vcfile[1].vcname"));
                       vcMap.put(SETTING_QF_VERSIONABLE,BOOLEAN_FALSE);
                       // we are not going to get the size for now so make it blank
                       vcMap.put(DomainConstants.SELECT_FILE_SIZE, "");
                       vcMap.put(DomainConstants.SELECT_FILE_NAME, vctitleMap.get("vcfile[1].vcname"));
                       vcMap.put(DomainConstants.SELECT_FILE_FORMAT, vctitleMap.get("vcfile[1].format"));
                       vcMap.put("vcfile[1].versionid", vctitleMap.get("vcfile[1].versionid"));
                       // set these to TRUE so the Acions Icons show up
                       vcMap.put(CommonDocument.SELECT_HAS_UNLOCK_ACCESS, "TRUE");
                       vcMap.put(CommonDocument.SELECT_HAS_LOCK_ACCESS, "TRUE");
                       vcMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, "TRUE");
                       vcMap.put(CommonDocument.SELECT_HAS_CHECKIN_ACCESS, "TRUE");

                       returnList.add(vcMap);
                    }
                    else
                    {
                      //Get the connected Document is versionable or non versionable and get the file List.
                      versionable = CommonDocument.allowFileVersioning(context, (String)dataMap.get("objectId"));

                      // To get the Versioned file
                      if(versionable == true) {
                          emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, argsLst);
                          fileMapList = (MapList)fileUI.getFiles(context, fargs);
                      } else {
                    	  // To get the non Versioned file
                    	  emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, argsLst);
                          fileMapList = (MapList)fileUI.getNonVersionableFiles(context, fargs);
                      }

                      if(fileMapList != null) {
                          Map titleMap = null;
                          for(int lCnt = 0; lCnt < fileMapList.size(); lCnt++) {
                            tempMap = (Map)fileMapList.get(lCnt);
                            if(tempMap.containsKey("masterId")) {
                              String tempStr = (String)tempMap.get("masterId");
                              if(tempStr == null || tempStr.trim().length()==0){
                                tempMap.put("masterId", tempMap.get(DomainConstants.SELECT_ID));
                              }
                            }else {
                              tempMap.put("masterId", tempMap.get(DomainConstants.SELECT_ID));
                            }
                            for(int i = 0;i<titleList.size();i++){
                              titleMap = (Map) titleList.get(i);
                              String id = (String) titleMap.get(DomainConstants.SELECT_ID);
                              String childId = (String) tempMap.get(DomainConstants.SELECT_ID);
                              //To add the suspend versioning val and title in the Summary maplist
                              if(id != null && id.equals(childId)){
                                tempMap.put("Master Title", (String) titleMap.get(sel_Title));
                                tempMap.put(sel_SuspendVer, (String) titleMap.get(sel_SuspendVer));
                                break;
                              }//end of if loop
                            }//end of for loop
                            tempMap.put(CommonDocument.SELECT_TITLE, tempMap.get(DomainConstants.SELECT_FILE_NAME));
                            tempMap.put("Context Type", dataMap.get("RelationShip"));
                            tempMap.put("Context Name", dataMap.get("objectName"));
                            //This is to segregate the objects depending on this versionable property
                            if(versionable){
                              tempMap.put(SETTING_QF_VERSIONABLE,BOOLEAN_TRUE);
                            } else {
                              tempMap.put(SETTING_QF_VERSIONABLE,BOOLEAN_FALSE);
                            }
                            tempMap.put("level", "1");
                            tempMap.put("isVCFile", "FALSE");

                            returnList.add(tempMap);
                          }//end of for loop
                      }//end of if loop
                    }
                }//end of for loop
            }//end of if loop

            //Get the File objects directly checked into the object.
            HashMap arguments = new HashMap();
            arguments.put("objectId", masterObjectId);
            String[] fargs = JPO.packArgs (arguments);
            String[] argsLst = null;


            //To add the Suspend versioning and title values
            String select_SuspendVer = DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_SuspendVersioning));
            String select_Title = DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_TITLE);
            StringList strObjSelects = new StringList(6);
            strObjSelects.add(DomainConstants.SELECT_NAME);
            strObjSelects.add(select_Title);
            strObjSelects.add(select_SuspendVer);
            strObjSelects.add("vcfile");
            strObjSelects.add("vcfile[1].vcname");
            strObjSelects.add("vcfile[1].format");

            MapList fileMap = null;
            Map masterObjDataMap = masterObject.getInfo(context,strObjSelects);
            String strSuspendVal = (String)masterObjDataMap.get(select_SuspendVer);
            if(strSuspendVal == null || "".equals(strSuspendVal)){
                masterObjDataMap.put(select_SuspendVer,"false");
            }
            if(masterObjDataMap == null){
                masterObjDataMap = new HashMap();
            }

            //Get the connected Document is versionable or non versionable and get the file List.
            boolean isVersionable = CommonDocument.allowFileVersioning(context, masterObjectId);

            String isVCFile = (String) masterObjDataMap.get("vcfile");
            // if this is a VC file - Right now a DesignSync file
            if (isVCFile.equals("TRUE"))
            {
               Map vcMap = new HashMap();
               vcMap.put("masterId", masterObjectId);
               vcMap.put("id", masterObjectId);
               vcMap.put("Context Type", "Attachment");
               vcMap.put("Context Name", masterObjDataMap.get("objectName"));
               vcMap.put("Master Title", (String) masterObjDataMap.get(sel_Title));
               vcMap.put("level", "1");
               vcMap.put("isVCFile", "TRUE");
               vcMap.put(CommonDocument.SELECT_TITLE, masterObjDataMap.get("vcfile[1].vcname"));
               vcMap.put(SETTING_QF_VERSIONABLE,BOOLEAN_FALSE);
               // we are not going to get the size for now so make it blank
               vcMap.put(DomainConstants.SELECT_FILE_SIZE, "");
               vcMap.put(DomainConstants.SELECT_FILE_NAME, masterObjDataMap.get("vcfile[1].vcname"));
               vcMap.put(DomainConstants.SELECT_FILE_FORMAT, masterObjDataMap.get("vcfile[1].format"));
               vcMap.put("vcfile[1].versionid", masterObjDataMap.get("vcfile[1].versionid"));
               // set these to TRUE so the Acions Icons show up
               vcMap.put(CommonDocument.SELECT_HAS_UNLOCK_ACCESS, "TRUE");
               vcMap.put(CommonDocument.SELECT_HAS_LOCK_ACCESS, "TRUE");
               vcMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, "TRUE");
               vcMap.put(CommonDocument.SELECT_HAS_CHECKIN_ACCESS, "TRUE");

               returnList.add(vcMap);
            }
            else {
               if(isVersionable == true) {
                  emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, argsLst);
                  fileMap = (MapList)fileUI.getFiles(context, fargs);
               } else {
                  emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, argsLst);
                  fileMap = (MapList)fileUI.getNonVersionableFiles(context, fargs);
               }
            }

            if(fileMap != null) {
                Iterator itr = fileMap.iterator();
                while(itr.hasNext()) {
                    tempMap = (Map)itr.next();
                    //Once decided need to add String Resource entry for "Attachment"
                    tempMap.put("Context Type", "Attachment");
                    //Check whether the tempmap contains masterid
                    if(tempMap.containsKey("masterId")){
                        String tempStr = (String)tempMap.get("masterId");
                        if(tempStr == null || tempStr.trim().length()==0){
                          tempMap.put("masterId", tempMap.get(DomainConstants.SELECT_ID));
                        }
                    } else {
                        tempMap.put("masterId", tempMap.get(DomainConstants.SELECT_ID));
                    }
                    tempMap.put(CommonDocument.SELECT_TITLE, tempMap.get(DomainConstants.SELECT_FILE_NAME));
                    tempMap.put("Context Name", (String)masterObjDataMap.get(DomainConstants.SELECT_NAME));
                    tempMap.put("Master Title", (String)masterObjDataMap.get(select_Title));
                    tempMap.put(select_SuspendVer, (String)masterObjDataMap.get(select_SuspendVer));
                    tempMap.put("level", "1");
                    tempMap.put("isVCFile", "FALSE");
                    //This is to segregate the objects depending on this versionable property
                    if(isVersionable){
                        tempMap.put(SETTING_QF_VERSIONABLE,BOOLEAN_TRUE);
                    } else {
                        tempMap.put(SETTING_QF_VERSIONABLE,BOOLEAN_FALSE);
                    }
                    returnList.add(tempMap);
                }//end of while loop
            }//end of if loop
        } catch(Exception ex) {
          throw new FrameworkException(ex);
        }
        return returnList;
    }

     /**
      * This method returns the file list that are connected to the object with
      * the specified relationships in the properties
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds no arguments.
      * @return MapList containing quick file access objects
      * @throws Exception if the operation fails.
      * @since 10.6.SP1
      */
    public Vector getFileName(Context context, String[] args) throws Exception {

        Vector vName = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramList");
            String reportFormat = (String) paramMap.get("reportFormat");
            boolean reportMode = false;

            if (reportFormat != null && (reportFormat.equals("HTML") || reportFormat.equals("ExcelHTML") || reportFormat.equals("CSV")))
            {
                reportMode = true;
            }
            MapList objectList = (MapList)programMap.get("objectList");
            Map objectMap = null;
            int objectListSize = 0 ;
            String name = "";
            String masterId = "";
            String fileFormat = "";
            String downloadURL = "";
            String versionId = "";
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            StringBuffer sBuff= new StringBuffer(256);

            for(int i = 0 ; i < objectListSize  ; i++)
            {
                sBuff= new StringBuffer(256);
                objectMap = (Map)objectList.get(i);

                masterId = (String) objectMap.get("masterId");
                if (masterId == null)
                {
                    masterId = (String) objectMap.get("objectId");
                }

                versionId = (String) objectMap.get("versionId");

                name = (String) objectMap.get(CommonDocument.SELECT_FILE_NAME);
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);

                // Get the image of the Type. If a specific naming convention
                // of the image is followed , the following code can be generalized.
                if (name == null || "null".equals(name))
                {
                    name = "";
                }

                downloadURL = "javascript:callCheckout('"+ XSSUtil.encodeForJavaScript(context, masterId) +"','download', '"+ XSSUtil.encodeForJavaScript(context, name)+ "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) +"', null, null, null, null, null, '"+ XSSUtil.encodeForJavaScript(context, versionId) +"');";
                if(!reportMode){

                    sBuff.append("<b><a href=\"");
                    sBuff.append(downloadURL);
                    sBuff.append("\">");
                    sBuff.append("<img src='../common/images/iconSmallFile.gif' border=0 />");
                    sBuff.append(XSSUtil.encodeForHTML(context, name));
                    sBuff.append("</a>");
                    sBuff.append("</b>");
                }else if(reportMode && ("CSV".equalsIgnoreCase(reportFormat))){
                    sBuff.append(XSSUtil.encodeForHTML(context, name));
                }else{
                    sBuff.append("<img src='../common/images/iconSmallFile.gif' border=0 />");
                    sBuff.append("<b>");
                    sBuff.append(XSSUtil.encodeForHTML(context, name));
                    sBuff.append("</b>");
                }
                if(UINavigatorUtil.isMobile(context)) {
                	sBuff= new StringBuffer();
                    sBuff.append("<img src='../common/images/iconSmallFile.gif' border=0 />");
                    sBuff.append(XSSUtil.encodeForHTML(context, name));
                }
                vName.add(sBuff.toString());
            }
           return vName;
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex);
        }
    }

    /**
     * This method is used to get the context
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return Vector contains list of Object names
     * @throws Exception if the operation fails.
     * @since 10.6.SP1
     */
    public Vector getFileContext(Context context, String[] args) throws Exception {
        Vector fileContextVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileContext = null;
            String strLanguage = context.getSession().getLanguage();

            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileContext = (String) objectMap.get("Context Type");
                if(fileContext == null){
                  fileContext = "";
                }
                if(fileContext.trim().length()>0 && !fileContext.equalsIgnoreCase("Attachment")){
                  fileContext = i18nNow.getAdminI18NString("Relationship",fileContext,strLanguage);
                } else if(fileContext.equalsIgnoreCase("Attachment")){
                  fileContext = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR,context.getLocale(),"emxComponents.Common.Attachments");
                }
                fileContextVector.add(XSSUtil.encodeForHTML(context, fileContext));
            }
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex);
        }
        finally
        {
            return fileContextVector;
        }
    }

    /**
     * This method is used to get the contained in/context object name
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return Vector contains list of Object names
     * @throws Exception if the operation fails.
     * @since 10.6.SP1
     */
    public Vector getFileContextObjectName(Context context, String[] args) throws Exception {
        Vector fileContextNameVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramList");
            String reportFormat = (String) paramMap.get("reportFormat");
            boolean reportMode = false;
            if (reportFormat != null && (reportFormat.equals("HTML") || reportFormat.equals("ExcelHTML") || reportFormat.equals("CSV")))
            {
                reportMode = true;
            }
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileContextName = null;
            StringBuffer fileContextNameImg = new StringBuffer(256);
            String objectId = null;
            String type = null;
            StringBuffer sBuf = new StringBuffer(256);
            String strDOCUMENTS = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_DOCUMENTS);
            boolean bIsDOCUMENTSchild = false;
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                objectId = (String) objectMap.get("masterId");
                if(objectId == null || "null".equals(objectId)) {
                    objectId = (String) objectMap.get("objectId");
                }
                if(objectId != null && !"null".equals(objectId)) {
                    DomainObject object = DomainObject.newInstance(context, objectId);
                    type = object.getInfo(context, DomainConstants.SELECT_TYPE);
                    if(type != null && !"null".equals(type)) {
                        String strRootType = FrameworkUtil.getBaseType(context,type,null);
                        if(strRootType != null && strDOCUMENTS.equalsIgnoreCase(strRootType)) {
                            bIsDOCUMENTSchild = true;
                        }
                    }
                }
                fileContextNameImg.append("../common/images/");
                //XSSOK
                fileContextNameImg.append(UINavigatorUtil.getTypeIconProperty(context, type));
                fileContextName = (String) objectMap.get("Context Name");
                String titleName = (String) objectMap.get("Master Title");
                if(bIsDOCUMENTSchild && titleName != null && !"".equals(titleName)) {
                  fileContextName = titleName;
                }
                sBuf = new StringBuffer(256);
                if(!reportMode){
					//Bug No 317834 - Start
					/*
					Because column type is programHTMLOutput, the sortable prameter
					needs to come first in the href. earlier, object id coming first in the href
					so the sorting is failed on the column
					*/
                    sBuf.append("<b><a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert&emxSuiteDirectory=components&suiteKey=Components&AppendParameters=true");
					sBuf.append("&fileContextName=");
                    sBuf.append(XSSUtil.encodeForJavaScript(context, fileContextName));
                    sBuf.append("&objectId=");
					//Bug No 317834 - End
                    sBuf.append(XSSUtil.encodeForJavaScript(context, objectId));
                    sBuf.append("', '930', '550', 'false', '");
                    sBuf.append("popup");
                    sBuf.append("')");
                    sBuf.append("\">");
                    sBuf.append("<img src='");
                    sBuf.append(fileContextNameImg.toString());
                    sBuf.append("' border=0 />");
                    sBuf.append(XSSUtil.encodeForHTML(context, fileContextName));
                    sBuf.append("</a></b>");
                }else if(reportMode && ("CSV".equalsIgnoreCase(reportFormat))){
                    sBuf.append(XSSUtil.encodeForHTML(context, fileContextName));
                }else{
                    sBuf.append("<b>");
                    sBuf.append("<img src='");
                    sBuf.append(fileContextNameImg.toString());
                    sBuf.append("' border=0 />");
                    sBuf.append(XSSUtil.encodeForHTML(context, fileContextName));
                    sBuf.append("</b>");
                }
              fileContextNameVector.add(sBuf.toString());
              fileContextNameImg.delete(0,fileContextNameImg.length());
            }
        }
        catch(Exception ex)
        {
            throw new FrameworkException(ex);
        }
        finally
        {
            return fileContextNameVector;
        }
    }

    /**
     * This method is used to get the hyperlinked file revision
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return Vector contains list of Object names
     * @throws Exception if the operation fails.
     * @since 10.6.SP1
     */
    public Vector getVersionColumn(Context context, String[] args) throws FrameworkException {

        Vector vName = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramList");
            String reportFormat = (String) paramMap.get("reportFormat");
            boolean reportMode = false;
            if (reportFormat != null && (reportFormat.equals("HTML") || reportFormat.equals("ExcelHTML") || reportFormat.equals("CSV")))
            {
                reportMode = true;
            }
            MapList objectList = (MapList)programMap.get("objectList");
            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            String sFirstHalf = "javascript:emxTableColumnLinkClick('../common/emxTable.jsp?program=emxCommonFileUI:getVersions&table=APPFileVersions&sortColumnName=Version&sortDirection=descending&header=emxComponents.Common.VersionsPageHeading&FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&FilterFrameSize=1&HelpMarker=emxhelpdocumentfileversions&CancelButton=true&CancelLabel=emxComponents.Common.Close&emxSuiteDirectory=components&suiteKey=Components";
            String sSecondHalf = "','730','450','false','popup','')";
            StringBuffer sbURL = new StringBuffer();
            StringBuffer sbRowItm = new StringBuffer();
            Map objectMap = null;
            String strRevision = "";
            String strDSFARevision = "";
            for(int i = 0 ; i < objectListSize  ; i++) {
                objectMap = (Map)objectList.get(i);
                if(objectMap != null){
                    strRevision = (String)objectMap.get("revision");
                    strDSFARevision = (String)objectMap.get("vcfile[1].versionid");

                  //The version will not be present for the 'Attachments'.In case of
                  //attachments only the non versionable files are shown.
                    if(strRevision != null && strRevision.trim().length()>0 && !"null".equalsIgnoreCase(strRevision)){
                        if(!reportMode){
                            sbURL.append(sFirstHalf);
							//Bug No 317834 - Start
							/*
							Because column type is programHTMLOutput, the sortable prameter
							needs to come first in the href. earlier, object id coming first in the href
							so the sorting is failed on the column
							*/
							sbURL.append("&versionNumber=");
                            sbURL.append(XSSUtil.encodeForJavaScript(context, strRevision));
							//317834 - End
                            sbURL.append("&parentOID=");
                            sbURL.append(XSSUtil.encodeForJavaScript(context, (String)objectMap.get("masterId")));
                            sbURL.append("&objectId=");
                            sbURL.append(XSSUtil.encodeForJavaScript(context, (String)objectMap.get("id")));
                            sbURL.append(sSecondHalf);
                            sbRowItm.append("<a href=\"");
                            sbRowItm.append(sbURL.toString());
                            sbRowItm.append("\">");
                            sbRowItm.append(XSSUtil.encodeForHTML(context, strRevision));
                            sbRowItm.append("</a>");
                        }else if (reportMode){
                            sbRowItm.append(XSSUtil.encodeForHTML(context, strRevision));
                        }
                        vName.add(sbRowItm.toString());
                        sbURL.delete(0,sbURL.length());
                        sbRowItm.delete(0,sbRowItm.length());
                   } else if(strDSFARevision != null && strDSFARevision.trim().length()>0 && !"null".equalsIgnoreCase(strDSFARevision)){
                        vName.add(XSSUtil.encodeForHTML(context, strDSFARevision));
                   } else {
                        vName.add(" ");
                   }
                } else {
                    vName.add("");
               }
           }
             return vName;
        } catch(Exception ex) {
           throw new FrameworkException(ex);
        }
    }

    /**
    *  Get Vector of Strings for Action Icons for both vertionable and non vertionable files
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.6.SP1
    */
    public static Vector getQuickFileActions(Context context, String[] args) throws Exception
    {
        Vector fileActionsVector = new Vector();
        try
        {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objectList = (MapList)programMap.get("objectList");

          Map paramList      = (Map)programMap.get("paramList");
          MapList vertionableObjList = new MapList();
          MapList nonVertionableObjList = new MapList();
          MapList vcFileObjList = new MapList();
          Boolean BVertionable = null;
          String isVCFile = "FALSE";
          String strRowCnt = "rowNum";
          int intRows = objectList.size();
          //Iterating thro the object list and building the versionable and non versionable lists
          for(int i=0;i<objectList.size(); i++){
            fileActionsVector.add(DomainConstants.EMPTY_STRING);
            Map objMap = (Map)objectList.get(i);
            if(objMap != null){
              objMap.put(strRowCnt,Integer.toString(i));

              BVertionable = (Boolean)objMap.get(SETTING_QF_VERSIONABLE);
              isVCFile = (String)objMap.get("isVCFile");
              if (isVCFile != null && isVCFile.equals("TRUE")){
                  vcFileObjList.add(objMap);
              }
              else if(BVertionable != null && BVertionable.booleanValue()){
                  vertionableObjList.add(objMap);
              } else {
                  nonVertionableObjList.add(objMap);
              }
            }//end of if loop
          }//end of for loop
          String strCount = "";
          int rowCount=0;
          HashMap tempProgramMap = new HashMap();
          tempProgramMap.put("paramList",paramList);

          //If there are versionable files retrieve the column data from the getFileActions method
          if(vertionableObjList.size()>0){
            tempProgramMap.put("objectList",vertionableObjList);
            Vector vertionableVector = emxCommonFileUI_mxJPO.getFileActions(context,JPO.packArgs(tempProgramMap));
            for(int j=0; j<vertionableObjList.size(); j++){
              Map objMap =(Map)vertionableObjList.get(j);
              if(objMap != null){
                strCount = (String)objMap.get(strRowCnt);
                if(strCount != null && strCount.trim().length()>0){
                  rowCount = Integer.parseInt(strCount);
                  if(rowCount <= intRows){
                  fileActionsVector.setElementAt((String)vertionableVector.get(j),rowCount);
                  }//end of if loop
                }//end of if loop
              }//end of if loop
            }//end of for loop
          }//end of if loop

          //If there are non versionable files retrieve the column data from the getNonVersionableFileActions method
          if(nonVertionableObjList.size()>0){
            tempProgramMap.put("objectList",nonVertionableObjList);
            Vector nonVertionableVector = emxCommonFileUI_mxJPO.getNonVersionableFileActions(context,JPO.packArgs(tempProgramMap));
            for(int j=0; j<nonVertionableObjList.size(); j++){
              Map objMap = (Map)nonVertionableObjList.get(j);
              if(objMap != null){
                strCount = (String)objMap.get(strRowCnt);
                if(strCount != null && strCount.trim().length()>0){
                  rowCount = Integer.parseInt(strCount);
                  if(rowCount <= intRows){
                      fileActionsVector.setElementAt((String)nonVertionableVector.get(j),rowCount);
                  }//end of if loop
                }//end of if loop
              }//end of if loop
            }//end of for loop
          }//end of if loop

          //If there are VC files retrieve the column data from the getVCFileVersionActions method
          if(vcFileObjList.size()>0){
            tempProgramMap.put("objectList",vcFileObjList);
            Vector vcFileVector = emxVCDocumentUI_mxJPO.getVCFileActions(context,JPO.packArgs(tempProgramMap));
            for(int j=0; j<vcFileObjList.size(); j++){
              Map objMap = (Map)vcFileObjList.get(j);
              if(objMap != null){
                strCount = (String)objMap.get(strRowCnt);
                if(strCount != null && strCount.trim().length()>0){
                  rowCount = Integer.parseInt(strCount);
                  if(rowCount <= intRows){
                      fileActionsVector.setElementAt((String)vcFileVector.get(j),rowCount);

                  }//end of if loop
                }//end of if loop
              }//end of if loop
            }//end of for loop
          }//end of if loop

        } catch(Exception ex) {
          throw new FrameworkException(ex);
        }
        //XSSOK
        return fileActionsVector;
    }
}
