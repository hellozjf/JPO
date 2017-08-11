/*
 * emxControlledFolderBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.1 Wed Dec 24 10:59:51 2008 ds-ksuryawanshi Experimental $
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.User;
import matrix.util.MatrixException;
import matrix.util.StringList;

import matrix.db.AccessConstants;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;


/**
 * @version PMC R207 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxControlledFolderBase_mxJPO extends emxProjectFolder_mxJPO
{
    //  Create an instant of emxUtil JPO
    protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;

    /**
     * This variable will be used for recognizing if the trigger show be allowed to call recursivly or not
     */
    private boolean allowTriggerRecursion = true;
    /**
     *
     * Constructor
     * @param context
     * @param args
     * @throws Exception
     */


    public emxControlledFolderBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
        // TODO Auto-generated constructor stub
    }


    /**
     * The method will return values for Folder Names since it is governed by two policies
     * The naming convention is different
     * This will be used in Summary pages
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: paramMap - a
     *            HashMap containing the following keys, "objectId".
     * @return Vector
     * @throws Exception
     *             if operation fails
     * @since PMC V6R207
     */

    public  StringList getProjectFolderNames(Context context, String[] args) throws Exception
    {
    	try 
    	{
    		// Do for each object
    		String strFolderId = ProgramCentralConstants.EMPTY_STRING;
    		String strtitle = ProgramCentralConstants.EMPTY_STRING;
    		String strPolicy = ProgramCentralConstants.EMPTY_STRING;
    		String strName = ProgramCentralConstants.EMPTY_STRING;
    		String strType = ProgramCentralConstants.EMPTY_STRING;
    		// Create result vector
    		StringList vecResult = new StringList();

    		// Get object list information from packed arguments
    		Map programMap = (Map) JPO.unpackArgs(args);
    		MapList objectList = (MapList)programMap.get("objectList");
    		Map paramList = (Map)programMap.get("paramList");
    		String strParentObjId = (String)paramList.get("projectID");
    		String strSuiteKey= (String)paramList.get("suiteKey");
    		String strNodeId = (String)paramList.get("jsTreeID");

    		// Added:10-Feb-09:kyp:R207:RMT Bug 369181
    		//
    		// When the Project name in the my Requirement Specifications table is clicked, the
    		// suite directory is passed as requirements, so we should not get the suite dir from
    		// paramList rather take it from colunMap. The suiteKey is passed correctly as ProgramCentral.
    		//
    		Map mapColumn = (Map)programMap.get("columnMap");
    		Map mapSettings = (Map)mapColumn.get("settings");
    		String strRegisteredSuite = (String)mapSettings.get("Registered Suite");
    		String strSuiteDir = EnoviaResourceBundle.getProperty(context, "eServiceSuite" + strRegisteredSuite + ".Directory");
    		// End:R207:RMT Bug 369181

    		//DomainObject domFolder = DomainObject.newInstance(context);
    		//Added:2-Feb-09:yox:R207:PRG Bug :366766
    		String reportFormat = (String)paramList.get("reportFormat");
    		boolean isprinterFriendly = false;
    		if(reportFormat != null)
    		{
    			isprinterFriendly = true;
    		}
    		//End:R207:PRG Bug :366766
    		StringList slDocTypes = ProgramCentralUtil.getSubTypesList(context, CommonDocument.TYPE_DOCUMENTS);

    		String SELECT_ATTRIBUTE_FOLDER_TITLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Title") + "]";
    		String SELECT_ATTRIBUTE_VERSION = "attribute["+DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT+"]";
    		StringList slSelectables = new StringList(6);
    		slSelectables.addElement(DomainConstants.SELECT_POLICY);
    		slSelectables.addElement(SELECT_ATTRIBUTE_FOLDER_TITLE);
    		slSelectables.addElement(DomainConstants.SELECT_NAME);
    		slSelectables.addElement(DomainConstants.SELECT_TYPE);
    		slSelectables.addElement(DomainConstants.SELECT_ID);
    		slSelectables.addElement(SELECT_ATTRIBUTE_VERSION);

    		int iListSize = objectList.size();
    		String[] saFolderIds = new String[iListSize];
    		StringList slFolderIds = new StringList();
    		StringList slParentIds = new StringList();
    		
    		for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();)
    		{
    			Map mapObjectInfon = (Map) itrObjects.next();
    			slFolderIds.addElement((String)mapObjectInfon.get("id"));                 
    			slParentIds.addElement((String)mapObjectInfon.get("masterId"));
    		}
    		
    		slFolderIds.toArray(saFolderIds);
    		MapList mlFolderInfo = DomainObject.getInfo(context, saFolderIds, slSelectables);
    		int iParentId=0;
    		for (Iterator itrObjects = mlFolderInfo.iterator(); itrObjects.hasNext();)
    		{
    			Map mTempFolder = null;
    			mTempFolder = (Map)itrObjects.next();
    			strFolderId = (String)mTempFolder.get(DomainConstants.SELECT_ID);
    			//domFolder.setId(strFolderId);
    			//String strIsVersionObject = domFolder.getAttributeValue(context, DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT);
    			String strIsVersionObject = (String)mTempFolder.get(SELECT_ATTRIBUTE_VERSION);
    			boolean isVersionObject  = Boolean.parseBoolean(strIsVersionObject);

    			strtitle = (String)mTempFolder.get(SELECT_ATTRIBUTE_FOLDER_TITLE);
    			strName = (String)mTempFolder.get(DomainConstants.SELECT_NAME);
    			strPolicy = (String)mTempFolder.get(DomainConstants.SELECT_POLICY);
    			strType= (String)mTempFolder.get(DomainConstants.SELECT_TYPE);
    			String strObjectName = ProgramCentralConstants.EMPTY_STRING;

    			if(strPolicy.equals(DomainConstants.POLICY_WORKSPACE_VAULT))
    			{
    				strObjectName = strName;
    			}
    			else if(strPolicy.equals(DomainConstants.POLICY_CONTROLLED_FOLDER) )
    			{
    				strObjectName = strtitle;
    			}
    			else if(slDocTypes.contains(strType) && isVersionObject)
    			{
    				strObjectName = strtitle;
    				strParentObjId = (String)slParentIds.get(iParentId);
    			}
    			else
    			{
    				strObjectName = strName;
    			}

    			StringBuilder strValue = new StringBuilder();
    			if(!isprinterFriendly && !isVersionObject)
    			{
    				strValue.append("<a title='").append(XSSUtil.encodeForXML(context, strObjectName));
    				strValue.append("' href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=");
    				strValue.append(strSuiteDir).append("&amp;parentOID=").append(XSSUtil.encodeForURL(context,strParentObjId));
    				strValue.append("&amp;jsTreeID=").append(XSSUtil.encodeForURL(context,strNodeId)).append("&amp;AppendParameters=true");
    				strValue.append("&amp;displayView=details").append("&amp;suiteKey=").append(strSuiteKey);
    				strValue.append("&amp;objectId=").append(XSSUtil.encodeForURL(context,strFolderId)).append("','','','false','content')\">");
    				strValue.append( XSSUtil.encodeForXML(context, strObjectName));
    			}
    			else
    			{
    				strValue.append(XSSUtil.encodeForHTML(context,strObjectName));
    			}
    			if(!isprinterFriendly && !isVersionObject)
    			{
    				strValue.append("</a>");
    			}
    			vecResult.add(strValue.toString());
    			iParentId++;
    		}
    		return vecResult;
    	}
    	catch(Exception exp)
    	{
    		exp.printStackTrace();
    		throw exp;
    	}
    }

    /**
     * The method will return values for Folder Name in the Form "PMCProjectVaultViewForm"
     * since it is governed by to policies and Naming convention is different
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: paramMap - a
     *            HashMap containing the following keys, "objectId".
     * @return String
     * @throws Exception if operation fails
     * @since PMC V6R207
     */

    public  String getProjectFolderName(Context context, String[] args) throws Exception {
        try {
            // Create result vector
          String strProjFolderName = "";

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap) programMap.get("paramMap");
            String objectId    = (String) paramMap.get("objectId");

            DomainObject domFolder = DomainObject.newInstance(context);

            //SELECTABLES FOR GETTING THE CORRECT NAME OF THE PROJECT FOLDER
            String ATTRIBUTE_FOLDER_TITLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Title") + "]";
            StringList slSelectables = new StringList(3);
            slSelectables.addElement(DomainConstants.SELECT_POLICY);
            slSelectables.addElement(ATTRIBUTE_FOLDER_TITLE);
            slSelectables.addElement(DomainConstants.SELECT_NAME);

            Map mapObjectInfo = null;

            // Do for each object
            String strFolderId = "";
            String strtitle = "";
            String strPolicy = "";
            String strName = "";
            strFolderId = objectId;
            domFolder.setId(strFolderId);
            mapObjectInfo = domFolder.getInfo(context,slSelectables);
            strtitle = (String)mapObjectInfo.get(ATTRIBUTE_FOLDER_TITLE);
            strName = (String)mapObjectInfo.get(DomainConstants.SELECT_NAME);
            strPolicy = (String)mapObjectInfo.get(DomainConstants.SELECT_POLICY);

            if(strPolicy.equals(DomainConstants.POLICY_WORKSPACE_VAULT)){
                strProjFolderName = strName;
            }else{
                strProjFolderName = strtitle;
            }

            return strProjFolderName;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * This method is used to get the list of Workspace Folder objects with policy Controlled Folder.
     * Used for getTableControlledFolderRevisionsData table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        objectId - containing one String entry for key "objectId"
     * @return MapList containing the id of WorkSpace objects of the logged in user
     * @throws Exception if the operation fails
     * @since PMC V6R207
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableControlledFolderRevisionsData(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        MapList mpFolderList = new MapList();
        //HashMap folder = null;
        String strFolderRevId="";
        try
        {
            DomainObject domFolder =  DomainObject.newInstance(context, objectId);

//          SELECTABLES FOR GETTING THE CORRECT NAME OF THE PROJECT FOLDER
            String ATTRIBUTE_FOLDER_TITLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Title") + "]";
            StringList slSelectables = new StringList(3);
            slSelectables.addElement(DomainConstants.SELECT_POLICY);
            slSelectables.addElement(ATTRIBUTE_FOLDER_TITLE);
            slSelectables.addElement(DomainConstants.SELECT_NAME);
            BusinessObjectList boControllFolderAllRevsions = domFolder.getRevisions(context);
            for(int i=0;i<boControllFolderAllRevsions.size();i++){
                strFolderRevId = boControllFolderAllRevsions.getElement(i).getObjectId();
                domFolder.setId(strFolderRevId);
                mpFolderList.add(i,(Object)domFolder.getInfo(context,slSelectables));
            }

            return mpFolderList;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;

        }
    }

    /**
     * This methode will be used to update name of the type "Workspace Vault" and "Controlled Folder"
     * since it is govened by two different policies and each policy has its corresponding way of stoaring name
     *
     * @param context The Matrix Context object
     * @param args holds a HashMap containing the following entries: paramMap - a
     *             HashMap containing the following keys, "objectId".
     * @returns Object <description>
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    public Object updateControlledFolderName(Context context, String[] args)
    throws Exception {
        Object resultObject = null;
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            String strNewValue = (String) paramMap.get("New Value");
            String strPolicy = "";

            StringList slSelectables = new StringList(1);
            slSelectables.addElement(DomainConstants.SELECT_POLICY);

            //
            // Following code creates a business object
            //
            DomainObject dmoObject = DomainObject.newInstance(context);
            dmoObject.setId(objectId);
            Map mpValues = dmoObject.getInfo(context,slSelectables);
            strPolicy = (String)mpValues.get(DomainConstants.SELECT_POLICY);

            //
            //Below code will chek the Policy of the object and take necessary action
            //
            if(DomainConstants.POLICY_CONTROLLED_FOLDER.equals(strPolicy)){
                dmoObject.setAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE,strNewValue);
            }else{
                dmoObject.setName(context,strNewValue);
            }



            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            // Method processing logic goes here
            return resultObject;
        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

    /**
     * This getReplacedString method is used for replacing a key from href string
     * @param strSourceString
     * @param toBeReplacedKey
     * @param strtoBeReplaceValue
     * @return String
     * @since PRG R207
     */

    private String replaceQueryStringParameter(String strSourceString,String toBeReplacedKey,String strtoBeReplaceValue){

        String strTempResult = "";
        String strActualResult = "";
        try {

            StringList strHrefParts = FrameworkUtil.splitString(strSourceString,toBeReplacedKey);
            String strHrefPart = "";
            if(strHrefParts.size()>0){
                strHrefPart = (String)strHrefParts.get(1);
                strTempResult = strHrefPart.substring(strHrefPart.indexOf("&"),strHrefPart.length());
                strActualResult = (String)strHrefParts.get(0)+toBeReplacedKey+strtoBeReplaceValue+strTempResult;
            }else{
                return strSourceString;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strActualResult;
    }

    /**
     * This methode will be used to generate dynamic commands for Menu "PMCtype_ControlledFolder"
     *
     * @param context The Matrix Context object
     * @param args holds a HashMap containing the following entries: paramMap - a
     *             HashMap containing the following keys, "objectId".
     * @returns HashMap containing information about commands to dynamically generate
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */

    public Map getDynamicCategories (Context context, String[]args )throws Exception
    {
        HashMap resultMap = new HashMap();
        try {

            MapList categoryMapList = new MapList();
            Map inputMap = JPO.unpackArgs(args);
            Map paramMap = (Map) inputMap.get("paramMap");
            String objectId = (String)paramMap.get("objectId");
            
            UIMenu uiMenu = new UIMenu();
       
	    Map hmCmdDiscussions = ProgramCentralUtil.createDynamicCommand(context,"APPDiscussionCommand",uiMenu,true);
            categoryMapList.add(hmCmdDiscussions);
            String strHref = (String)hmCmdDiscussions.get("href");
            strHref = replaceQueryStringParameter(strHref,"header=","emxComponents.PMCDiscussionSummary.Heading");
            hmCmdDiscussions.put("href",strHref);

            Map hmCmdPMCProp = ProgramCentralUtil.createDynamicCommand(context,"PMCProperties",uiMenu,true);
            categoryMapList.add(hmCmdPMCProp);
            strHref = (String)hmCmdPMCProp.get("href");
            strHref = replaceQueryStringParameter(strHref,"formHeader=","emxProgramCentral.Common.ControlledFolderFormBasics");
            hmCmdPMCProp.put("href",strHref);
            
            resultMap.put("Children",categoryMapList);

        } catch (Exception e) {
        	throw new MatrixException(e);
        }
        return resultMap;

    }

    /**
     * This trigger revises all released parents (recursively)
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      0 - String containing the object id (Old Revision)
     *      1 - String containing the state to check for
     * @throws MatrixException if the operation fails
     * @return int based on success or failure
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */

    public int triggerAutoReviseReleasedParents(Context context, String[] args) throws MatrixException {
        final int SUCCESS = 0;
        try {
            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            // Method processing logic goes here
            String strObjectId  = args[0];
            String strSubFoldersParentsState = "";
            String strOldSubfolderParentsId = "";

            DomainObject dmoSubFoldersParent = DomainObject.newInstance(context);

            StringList slBusSelects = new StringList(2);
            slBusSelects.add(DomainConstants.SELECT_ID);
            slBusSelects.add(DomainConstants.SELECT_CURRENT);

            DomainObject dmoReviseFolderObject = DomainObject.newInstance(context,strObjectId);

            //Getting all the parent folders of previous revision.

            MapList mlSubFolderList = dmoReviseFolderObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_SUB_VAULTS+","+DomainConstants.RELATIONSHIP_LINKED_FOLDERS,
                    DomainConstants.TYPE_CONTROLLED_FOLDER,
                    slBusSelects,
                    null,       // relationshipSelects
                    true,       // getTo
                    false,      // getFrom
                    (short)1,   // recurseToLevel
                    null,       // objectWhere
                    null);      // relationshipWhere

            // Modified:9-Apr-109:nzf:R207:PRG:Bug:371113

            //ContextUtil.pushContext(context);

            User userOrignalOwner = null;
            String strOrignalUserName ="";

            for (Iterator itrFolderStructure = mlSubFolderList.iterator(); itrFolderStructure.hasNext();) {

                Map mpFolderInfo = (Map)itrFolderStructure.next();
                strOldSubfolderParentsId = (String)mpFolderInfo.get(DomainConstants.SELECT_ID);
                strSubFoldersParentsState = (String)mpFolderInfo.get(DomainConstants.SELECT_CURRENT);

                //If State is in Release state then revise it
                if(DomainConstants.STATE_CONTROLLED_FOLDER_RELEASE.equals(strSubFoldersParentsState)){

                    dmoSubFoldersParent.setId(strOldSubfolderParentsId);

                    //Added for setting the orignal owner of the folder
                    userOrignalOwner = dmoSubFoldersParent.getOwner(context);
                    strOrignalUserName = userOrignalOwner.getName();
                    BusinessObject boNewRevision = dmoSubFoldersParent.reviseObject(context,false);

                    //This will make sure that the owner of the folder is not overwritten by owner of child folder
                    //Just in case if the folder revision takes place recursivly..
                    boNewRevision.setAttributeValue(context,DomainConstants.ATTRIBUTE_ORIGINATOR,strOrignalUserName);
                    boNewRevision.setOwner(userOrignalOwner);

                }

            }
            // End:R207:PRG:Bug:371113

            return SUCCESS;
        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
        finally {
            //ContextUtil.popContext(context);
        }
    }

    /**
     * This trigger program parameter object will be created using following values for the attributes
     *
     * @param context The Matrix Context object
     * @param args holds the following input arguments:
     *        0 - String containing the object id (Old revision)
     * @return int based on success or failure
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    public int triggerSupercedePreviousRev(Context context, String[] args) throws MatrixException {
        int SUCCESS = 0;
        try {
            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            // Method processing logic goes here
            String strObjectId  = args[0];

            DomainObject dmoFoldersToBeRevised = DomainObject.newInstance(context,strObjectId);

            //Set the state of the to be revised folder to Susperseded
            ContextUtil.pushContext(context);
            dmoFoldersToBeRevised.setState(context,DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED);


            return SUCCESS;
        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
        finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * This trigger demotes the ControlledFolder's previous revision in "Release" state once the latest revision is deleted.
     * @param context The Matrix Context object
     * @param args holds the following input arguments:
     *        0 - Folder Name
     *        1 - Current Revision number
     * @return int based on success or failure
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */

    //Added:13-Mar-09:nzf:R207:PRG:Bug:370352

    public int triggerControlledFolderDeleteAction(Context context, String[] args) throws MatrixException {
        int SUCCESS = 0;
        try {
            // Check methode arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            // Method processing logic goes here
            String strFolderName  = args[0];
            String strCurrentRev = args[1];

            int intCurrentRev = Integer.parseInt(strCurrentRev);
            if(intCurrentRev!=1){
                intCurrentRev = intCurrentRev-1;
                String strPrevRev = ""+intCurrentRev;
              //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
                String sCommandStatement = "temp query bus $1 $2 $3 select $4";
                String strResult =  MqlUtil.mqlCommand(context, sCommandStatement,true,DomainConstants.TYPE_CONTROLLED_FOLDER, strFolderName,strPrevRev,DomainConstants.SELECT_ID); 
               //PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

                //As we just want the object id and we get result as "id = 40528.55485.8568.25811" we split the string.
                StringList slResult = FrameworkUtil.splitString(strResult,"= ");
                strResult=slResult.get(1).toString();


                DomainObject dmoFoldersToBeDemoted = DomainObject.newInstance(context,strResult);
                try{
                //Set the state of the to be previous revision of the deleted folder.
                ContextUtil.pushContext(context);
                dmoFoldersToBeDemoted.setState(context,DomainConstants.STATE_CONTROLLED_FOLDER_RELEASE);
                }finally{
                    ContextUtil.popContext(context);
                }
            }
            return SUCCESS;
        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

    //End:R207:PRG:Bug:370352

    /**
     * This trigger links a new child folder (Sub Vaults relationship only) to the latest revision of the parent folder
     *
     * @param context The Matrix Context object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    public int triggerLinkToLatestRevisedParent(Context context, String[] args) throws MatrixException {
        int SUCCESS = 0;
        try {
            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            //      Method processing logic goes here
            String strObjectId  = args[0];
            String strLatestFolderId = "";
            String strParentFolderId = "";
            String strParentType = "";
            String strLatestParentFolderId = "";

            DomainObject dmoOldFolder = DomainObject.newInstance(context,strObjectId);

            strLatestFolderId = dmoOldFolder.getInfo(context,DomainConstants.SELECT_LAST_ID);

            DomainObject dmoLatestFolder = DomainObject.newInstance(context,strLatestFolderId);

            StringList slBusSelects = new StringList();
            slBusSelects.add(DomainConstants.SELECT_ID);
            slBusSelects.add(DomainConstants.SELECT_TYPE);
            slBusSelects.add(DomainConstants.SELECT_LAST_ID);


            MapList mlParentFolders = dmoOldFolder.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_SUB_VAULTS+","+DomainConstants.RELATIONSHIP_LINKED_FOLDERS,
                    DomainConstants.TYPE_CONTROLLED_FOLDER,
                    slBusSelects,
                    null,       // relationshipSelects
                    true,      // getTo
                    false,       // getFrom
                    (short) 1,  // recurseToLevel
                    null,       // objectWhere
                    null);      // relationshipWhere

            DomainObject dmoParentFolder = DomainObject.newInstance(context);
            Map mpFolderInfo = null;

            String strMQL = "print type \"" + DomainConstants.TYPE_CONTROLLED_FOLDER +"\" select derivative dump |";
            String strResult = MqlUtil.mqlCommand(context, strMQL, true); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
            StringList slControlledFolderTypeHierarchy = FrameworkUtil.split(strResult, "|");

            // Dont forget to add Controlled Folder type itself into this listing
            slControlledFolderTypeHierarchy.add(DomainConstants.TYPE_CONTROLLED_FOLDER);

            ContextUtil.pushContext(context);

            for (Iterator itrFolderStructure = mlParentFolders.iterator(); itrFolderStructure.hasNext();) {

                mpFolderInfo = (Map)itrFolderStructure.next();
                strParentFolderId = (String)mpFolderInfo.get(DomainConstants.SELECT_ID);
                strParentType = (String)mpFolderInfo.get(DomainConstants.SELECT_TYPE);
                strLatestParentFolderId = (String)mpFolderInfo.get(DomainConstants.SELECT_LAST_ID);

                //For finding if the parent is of type "Controlled Folder"

                if(slControlledFolderTypeHierarchy.contains(strParentType)){
                    dmoParentFolder.setId(strLatestParentFolderId);
                    DomainRelationship.connect(context,dmoParentFolder,DomainConstants.RELATIONSHIP_SUB_VAULTS,dmoLatestFolder);
                }

                break;

            }
            return 0;
        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
        finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * This trigger links (via the Linked Folder relationship) the newly revised folder to the previous revisions sub folders
     *
     * @param context The Matrix Context object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     *        1 - String containing the state to check for
     * @return int based on success or failure
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */

    public int triggerLinkSubFolders(Context context, String[] args) throws MatrixException {
        try {
            final int SUCCESS = 0;
            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            String strObjectId  = args[0];
            String strLatestFolderId = "";
            String strOldSubfolderId = "";


            StringList slBusSelects = new StringList(2);
            slBusSelects.add(DomainConstants.SELECT_ID);
            slBusSelects.add(DomainConstants.SELECT_CURRENT);

            DomainObject dmoReviseFolderObject = DomainObject.newInstance(context,strObjectId);

            //Getting all the subfolders of previous revision.

            MapList mlSubFolderList = dmoReviseFolderObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_SUB_VAULTS+","+DomainConstants.RELATIONSHIP_LINKED_FOLDERS,
                    DomainConstants.TYPE_CONTROLLED_FOLDER,
                    slBusSelects,
                    null,       // relationshipSelects
                    false,      // getTo
                    true,       // getFrom
                    (short) 1,  // recurseToLevel
                    null,       // objectWhere
                    null);      // relationshipWhere

            strLatestFolderId = dmoReviseFolderObject.getInfo(context,DomainConstants.SELECT_LAST_ID);

            DomainObject dmoLatestFolder = DomainObject.newInstance(context,strLatestFolderId);
            String[] strOldSubfolderIds = new String[mlSubFolderList.size()];
            int i=0;
            Map mpFolderInfo = null;
            for (Iterator itrFolderStructure = mlSubFolderList.iterator(); itrFolderStructure.hasNext();) {

                mpFolderInfo = (Map)itrFolderStructure.next();
                strOldSubfolderIds[i++] = (String)mpFolderInfo.get(DomainConstants.SELECT_ID);
            }
            //Connect all subfolders in one shot
            ContextUtil.pushContext(context);
            DomainRelationship.connect(context,dmoLatestFolder,DomainConstants.RELATIONSHIP_LINKED_FOLDERS,true,strOldSubfolderIds);

            return SUCCESS;

        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
        finally {
            ContextUtil.popContext(context);
        }
    }

    /**
     * When Controlled Folder is promoted from Create to Review this trigger performs following operation
     *    1.    Check the status of children states against the list of completion states.
     *    2.    If any child is not in one of those states,
     *    3.    The promotion should fail with an error specifying which object(s) failed.
     *
     * @param context The Matrix Context object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     *        1 - String containing the relationships to which "Controlled Folder" are connected
     *        2 - String containing the relationships to which "Controlled Folder" Documents are connected
     *        3 - String containing the policy states in which Documents are considered to be Complete
     * @return int based on success or failure
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    public int triggerCheckChildrenStates(Context context, String[] args) throws MatrixException {



        final int SUCCESS = 0;
        final int FAILURE = 1;
        int nReturn = SUCCESS;

        try {
            // Check metho arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }

            //Object ID
            String strObjectId  = args[0];
            StringList slFinalStringList = new StringList();

            //Type of relationships to search Example args[0] = "relationship_SubVaults,relationship_LinkedFolders"

            //All the SubFolders
            String strRelsToWhichCFsConnected = args[1];
            StringList slRelsToWhichCFsConnected = FrameworkUtil.splitString(strRelsToWhichCFsConnected,",");
            for(int i=0;i<slRelsToWhichCFsConnected.size();i++){

                slFinalStringList.add(PropertyUtil.getSchemaProperty(context,slRelsToWhichCFsConnected.get(i).toString()));

            }

            //All the Documents connected to the "Controlled Fodler"
            String strRelsToWhichCFDocumentsConnected = args[2];

            StringList slRelsToWhichCFDocumentsConnected = FrameworkUtil.splitString(strRelsToWhichCFDocumentsConnected,",");
            for(int i=0;i<slRelsToWhichCFDocumentsConnected.size();i++){

                slFinalStringList.add(PropertyUtil.getSchemaProperty(context,slRelsToWhichCFDocumentsConnected.get(i).toString()));

            }

            //Join all the Relationship List
            String strAllRelsToSearch = FrameworkUtil.join(slFinalStringList,",");

            //The completion states of the documents connected to the "Controlled Folder"
            String strPolicyStatesIndicatingCompletion = args[3];
            HashMap hmPolicyCompletionStates = new HashMap();

            StringList slPolicyStatesIndicatingCompletion = FrameworkUtil.splitString(strPolicyStatesIndicatingCompletion,",");
            StringList slPolicyCompletionStates = new StringList();
            String strSymbolicPolicyName = "";
            String strSymbolicStateName = "";
            String strRealPolicyName = "";
            String strRealStateName = "";
            for(int i=0;i<slPolicyStatesIndicatingCompletion.size();i++){

                slPolicyCompletionStates = FrameworkUtil.splitString(slPolicyStatesIndicatingCompletion.get(i).toString(),":");

            	if(null != slPolicyCompletionStates && slPolicyCompletionStates.size() > 1)             	// [ADDED::PRG:RG6:Jun 14, 2011:IR-113108V6R2012x :R212:]
            	{
                strSymbolicPolicyName = (String)slPolicyCompletionStates.get(0);
                strSymbolicStateName = (String)slPolicyCompletionStates.get(1);
            		if(ProgramCentralUtil.isNotNullString(strSymbolicPolicyName) && ProgramCentralUtil.isNotNullString(strSymbolicStateName))             	// [ADDED::PRG:RG6:Jun 14, 2011:IR-113108V6R2012x :R212:]
            		{
                strRealPolicyName = PropertyUtil.getSchemaProperty(context, strSymbolicPolicyName);
                strRealStateName = PropertyUtil.getSchemaProperty(context, "Policy", strRealPolicyName, strSymbolicStateName);

                //Creating HashMap for Policy and its completion states
                hmPolicyCompletionStates.put(strRealPolicyName, strRealStateName);
            }
            	}
            }

            final String SELECT_ATTRIBUTE_FOLDER_TITLE = "attribute[" + DomainConstants.ATTRIBUTE_TITLE + "]";
            DomainObject dmoParentFoldersToBeRevised = DomainObject.newInstance(context,strObjectId);

            StringList slBusSelects = new StringList();
            slBusSelects.add(DomainConstants.SELECT_ID);
            slBusSelects.add(DomainConstants.SELECT_CURRENT);
            slBusSelects.add(DomainConstants.SELECT_POLICY);
            slBusSelects.add(DomainConstants.SELECT_TYPE);
            slBusSelects.add(DomainConstants.SELECT_NAME);
            slBusSelects.add(DomainConstants.SELECT_REVISION);
            slBusSelects.add(SELECT_ATTRIBUTE_FOLDER_TITLE);

            //DomainObject dmoReviseFolderObject = DomainObject.newInstance(context,strObjectId);


            //Getting all the subfolders of previous revision.

            MapList mlSubFolderList = dmoParentFoldersToBeRevised.getRelatedObjects(context,
                    strAllRelsToSearch,
                    "*",
                    slBusSelects,
                    null,       // relationshipSelects
                    false,      // getTo
                    true,       // getFrom
                    (short) 1,  // recurseToLevel
                    null,       // objectWhere
                    null);      // relationshipWhere

            DomainObject dmoLatestSubFolder = DomainObject.newInstance(context);

            String strPolicy = "";
            String strCurrentState="";
            String strRevision = "";
            String strType = "";
            //For indicating valid usecase to promote
            HashMap hmInvalidCases =  new HashMap();
            i18nNow i18n = new i18nNow();
            String strLanguage = context.getSession().getLanguage();
            String strErrorMsg = i18n.GetString("emxProgramCentralStringResource",strLanguage,"emxProgramCentral.ControlledFolder.PromoteCheck.ErrorMessage");

            //Finding all subtypes of Controlled Folder

            String strMQL = "print type \"" + DomainConstants.TYPE_CONTROLLED_FOLDER +"\" select derivative dump |";
            String strResult = MqlUtil.mqlCommand(context, strMQL, true); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
            StringList slControlledFolderTypeHierarchy = FrameworkUtil.split(strResult, "|");

            // Dont forget to add Controlled Folder type itself into this listing
            slControlledFolderTypeHierarchy.add(DomainConstants.TYPE_CONTROLLED_FOLDER);

            for (Iterator itrFolderStructure = mlSubFolderList.iterator(); itrFolderStructure.hasNext();) {

                Map mpFolderInfo = (Map)itrFolderStructure.next();
                strPolicy = (String)mpFolderInfo.get(DomainConstants.SELECT_POLICY);
                strCurrentState = (String)mpFolderInfo.get(DomainConstants.SELECT_CURRENT);
                strType = (String)mpFolderInfo.get(DomainConstants.SELECT_TYPE);

                //
                // The controlled folder objects with Superceded state will be skipped
                // while determining if they are at their completion state, because
                // they are already substituted by latest folders.
                //
                if (slControlledFolderTypeHierarchy.contains(strType) && DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED.equals(strCurrentState)) {
                    continue;
                }
                //Check Added for IR IR-280553V6R2015x.
                if(ProgramCentralConstants.TYPE_DOCUMENT.equalsIgnoreCase(strType) && "OBSOLETE".equalsIgnoreCase(strCurrentState)){
                	continue;
                }
                
                //Modified:24-Feb-09:nzf:R207:PRG:Bug:369622
                if(!(hmPolicyCompletionStates.containsKey(strPolicy) && strCurrentState.equals(hmPolicyCompletionStates.get(strPolicy)))
                        && !hmPolicyCompletionStates.containsValue(strCurrentState))
                {//End:R207:PRG:Bug:369622
                    strRevision =  (String)mpFolderInfo.get(DomainConstants.SELECT_REVISION);


                    if(slControlledFolderTypeHierarchy.contains(strType)){

                        hmInvalidCases.put(mpFolderInfo.get(SELECT_ATTRIBUTE_FOLDER_TITLE),mpFolderInfo.get(strCurrentState));
                        strErrorMsg = strErrorMsg +","+strType+" "+ mpFolderInfo.get(SELECT_ATTRIBUTE_FOLDER_TITLE).toString()+ " "+ strRevision;
                    }
                    else{

                        hmInvalidCases.put(mpFolderInfo.get(DomainConstants.SELECT_NAME),mpFolderInfo.get(strCurrentState));
                        strErrorMsg = strErrorMsg +","+strType+" "+ mpFolderInfo.get(DomainConstants.SELECT_NAME).toString()+ " "+ strRevision;
                    }
                }
            }

            //In case if HashMap "hmInvalidCases" is having some values
            //It indiacates that the promotion fails

            if(hmInvalidCases.size()!=0){
                nReturn = FAILURE;
                MqlUtil.mqlCommand(context, "notice " + "\""+strErrorMsg+"\"" ); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
            }


            return nReturn;
        } catch (IllegalArgumentException iaexp) {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

	/* This  method disconnects the connected object from the to end of the Vaulted Documents Rev2 relationship
	 * if the object is connected to a Controlled Folder and that folder is "frozen" (Release/Superceded).
	 * This basically undoes the replicate that was done automatically via the revision rule on the relationship.
	 * @param context The ematrix context of the request.
	 * @param args This string array contains following arguments:
	 *          0 - The object id of the folder
	 *          1 - The object id of the connected object
	 *          2 - The relationship id of the new connection
	 *			3 - The parent event (i.e. revise or clone)
	 *
	 * @throws MatrixException
	 * @since PRG X5
	 */

	public void removeNewRevision(Context context, String[] args) throws MatrixException
	{

		String fromObjectId = args[0];
		String toObjectId = args[1];
		String relId = args[2];
		String parentEvent = args[3];

		// Only consider if the parent event is a revise, not clone
		//
		if (parentEvent.equals("revise")) {
			String folderType = "";
			String strCurrentState = "";

            StringList slBusSelects = new StringList(2);

            slBusSelects.add(DomainConstants.SELECT_TYPE);
            slBusSelects.add(DomainConstants.SELECT_CURRENT);

           /* DomainObject domFolder = DomainObject.newInstance(context,fromObjectId);

            Map folderInfo = domFolder.getInfo(context, slBusSelects);*/
            
            Map folderInfo = new HashMap();
            try{
            	ProgramCentralUtil.pushUserContext(context);
            	DomainObject domFolder = DomainObject.newInstance(context,fromObjectId);
            	folderInfo = domFolder.getInfo(context, slBusSelects);
            }catch(Exception e){
            }finally {
            	ProgramCentralUtil.popUserContext(context);
            }



			folderType = (String)folderInfo.get(DomainConstants.SELECT_TYPE);
			strCurrentState = (String)folderInfo.get(DomainConstants.SELECT_CURRENT);

			// Only undo the replicate if this is a controlled folder and it is not
			// in the frozen state (Release or Superceded)
			//
			if ((folderType.equals(DomainConstants.TYPE_CONTROLLED_FOLDER)) && (!strCurrentState.equals(DomainConstants.STATE_CONTROLLED_FOLDER_CREATE))) {

				// push context to user agent to avoid access issues
				// turn triggers off since we don't want disconnect triggers to fire for this connection
				// turn history off to avoid history records containing user agent and to avoid seeing disconnects for this rel
				// disconnect the newly created connection (undo the replicate)
				//
				try {

					ContextUtil.pushContext(context);

					MqlUtil.mqlCommand(context, "trigger off", true); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
					MqlUtil.mqlCommand(context,"history off;", true); //PRG:RG6:R213:Mql Injection:Static Mql:18-Oct-2011
					//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
					String sCommandStatement = "delete connection $1";
					String sPlaceHolder =  MqlUtil.mqlCommand(context, sCommandStatement,relId); 
					//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End
        		} catch (Exception exp) {
            		exp.printStackTrace();
            		throw new MatrixException(exp);
				}
        		finally {
            		ContextUtil.popContext(context);
					MqlUtil.mqlCommand(context, "trigger on", true); //PRG:RG6:R213:Mql Injection:Static Mql:17-Oct-2011
					MqlUtil.mqlCommand(context, "history on", true); //PRG:RG6:R213:Mql Injection:Static Mql:17-Oct-2011
        		}
			}
		}
	}
	
	/**
	 * This API is used to connect bookmarks from last revision folder to newly created folder revision
	 * @param context Matrix context object
	 * @param args String[] packed from trigger definition 
	 * @return int 0 on success
	 * @throws MatrixException
	 */
	public int triggerLinkBookmarksToRevisedFolder(Context context,String[]args) throws MatrixException
	{
		try
		{
			final int SUCCESS = 0;
			String strObjectId = args[0];
			if(null == strObjectId || "".equals(strObjectId))
			{
				throw new IllegalArgumentException("Invalid Argument");
			}
			DomainObject dmoOldFolderObject= DomainObject.newInstance(context,strObjectId);

			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add(DomainConstants.SELECT_NAME);

			StringList relationshipSelects = new StringList();

			MapList mlBookmarkList = dmoOldFolderObject.getRelatedObjects(context,
					DomainConstants.RELATIONSHIP_LINK_URL, 
					DomainConstants.TYPE_URL, 
					objectSelects, 
					relationshipSelects, 
					false, 
					true, 
					(short)1, 
					null, 
					null,
					0);

			String strLatestFolderId=dmoOldFolderObject.getInfo(context,DomainConstants.SELECT_LAST_ID);

			DomainObject dmoLatestFolder = DomainObject.newInstance(context,strLatestFolderId);
			String[] strOldBookmarkIds = new String[mlBookmarkList.size()];
			int i=0;
			Map mapBookmarkInfo = null;

			for (Iterator itrBookmarks = mlBookmarkList.iterator(); itrBookmarks.hasNext();)
			{
				mapBookmarkInfo = (Map)itrBookmarks.next();
				strOldBookmarkIds[i++] = (String)mapBookmarkInfo.get(DomainConstants.SELECT_ID);
			}
			
			DomainObject domainObject = DomainObject.newInstance(context);
			BusinessObject urlBusinessObject = null;
			int nBookmarkSize= strOldBookmarkIds.length;
			String[] bookmarkBusinessObjectIds = new String[nBookmarkSize];
			
			for(int j = 0; j < nBookmarkSize; j++)
			{
				String strLinkedBookmarkId =strOldBookmarkIds[j];
				domainObject.setId(strLinkedBookmarkId);

				urlBusinessObject= domainObject.cloneObject(context, null);
				urlBusinessObject.change(context, DomainConstants.TYPE_URL, domainObject.getName(), domainObject.getUniqueName(context), context.getVault().toString(), DomainConstants.POLICY_URL);
				bookmarkBusinessObjectIds[j] = urlBusinessObject.getObjectId();
			}
			
			DomainRelationship.connect(context,dmoLatestFolder,DomainConstants.RELATIONSHIP_LINK_URL,true,bookmarkBusinessObjectIds);
			
			return SUCCESS;
		}

		catch (Exception exp)
		{
			throw new MatrixException(exp);
		}
}	
	/**
	 * Indicates Higher Revision needs to add for Folder content
	 * @param context Matrix context object
	 * @param args objectList
	 * @return Vector
	 * @throws MatrixException
	 */
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector showHigherRevisionIcons(Context context,String[]args) throws Exception {

		Vector vecResult = new Vector();
		Map programMap = (Map) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		int objectListSize = objectList.size();
		String strLanguage = context.getSession().getLanguage();	

		StringList slParentIdList = new StringList(objectListSize);
		StringList slControlFolders = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_CONTROLLED_FOLDER);
		StringList slDocTypes = ProgramCentralUtil.getSubTypesList(context, CommonDocument.TYPE_DOCUMENTS);

		for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {

			Map parentInfoMap = (Map) itrObjects.next();
			String sParentId = (String)parentInfoMap.get("masterId");
			if(ProgramCentralUtil.isNotNullString(sParentId)){
				slParentIdList.add(sParentId);
			}else{
				sParentId = (String)parentInfoMap.get("id[parent]");
				if(ProgramCentralUtil.isNotNullString(sParentId)){		
					slParentIdList.add(sParentId);
				}
			}
		}
		String[] sParentObjectIds = (String[])slParentIdList.toArray(new String[slParentIdList.size()]);

		MapList parentInfoList = new MapList();
		if(sParentObjectIds != null && slParentIdList.size()!=0) {

			StringList busParentSelects = new StringList(2);
			busParentSelects.add(ProgramCentralConstants.SELECT_ID);
			busParentSelects.add("from["+ProgramCentralConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].to.id");

			parentInfoList = DomainObject.getInfo(context, sParentObjectIds, busParentSelects);
		}
		MapList mParentInfoList = new MapList();
		for(int k=0; k<parentInfoList.size();k++){
			Map parentInfoMap = (Map)parentInfoList.get(k);
			String sId = (String)parentInfoMap.get(DomainConstants.SELECT_ID);
			Map mParentIdMap = new HashMap();
			mParentIdMap.put(sId, parentInfoMap);
			mParentInfoList.add(mParentIdMap);
		}

		for(int i=0; i< objectListSize; i++) {

			Map bows = (Map)objectList.get(i);
			String sParentType = (String)bows.get(DomainRelationship.SELECT_FROM_TYPE);
			String sObjectType = (String)bows.get(DomainConstants.SELECT_TYPE);
			String sParentRevision = (String)bows.get(DomainRelationship.SELECT_FROM_REVISION);
			String sParentState = (String)bows.get("from.current");

			if(sObjectType==null || sParentType==null){
				vecResult.add(DomainObject.EMPTY_STRING);
				continue;
			}
			if(!sParentType.equalsIgnoreCase(DomainConstants.TYPE_CONTROLLED_FOLDER) ||	((sObjectType.equalsIgnoreCase(DomainConstants.TYPE_PROJECT_SPACE) ||
					sObjectType.equalsIgnoreCase(DomainConstants.TYPE_CONTROLLED_FOLDER)) || sParentType.equalsIgnoreCase(DomainConstants.TYPE_DOCUMENT))){

				vecResult.add(DomainObject.EMPTY_STRING);
			}/*else if(sObjectType.equalsIgnoreCase(DomainConstants.TYPE_DOCUMENT) && ("1").equalsIgnoreCase(sParentRevision) && DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(sParentState)){
				vecResult.add(DomainObject.EMPTY_STRING);
			}*/else{
				String folderId = (String)sParentObjectIds[i];
				if(slDocTypes.contains(sObjectType) && slControlFolders.contains(sParentType)){

					for(int l=0; l<mParentInfoList.size();l++) {
						Map mParentMap = (Map)mParentInfoList.get(l);
						Map controledFolderMap = (Map)mParentMap.get(folderId);
						if (null != controledFolderMap) {
							controledFolderMap.remove(DomainConstants.SELECT_ID);
							if(!controledFolderMap.isEmpty()){
								bows.putAll(controledFolderMap);
								break;
							}
						}
					}

					StringList slKeyRevision = new StringList();
					StringList slObjRevId  = new StringList();
					String sLastRevId = "";
					String strObjectId =(String)bows.get(DomainConstants.SELECT_ID);                
					String sParentId = (String)bows.get(DomainRelationship.SELECT_FROM_ID);	
					String sLastRev = (String)bows.get("last.revision");
					Object connectedDocs = bows.get("from["+ProgramCentralConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].to.id");
					StringList connectedDocList = new StringList();

					if(connectedDocs!=null){
						if(connectedDocs instanceof StringList){
							connectedDocList = (StringList) connectedDocs;
						}else if(connectedDocs instanceof String){
							connectedDocList.add((String) connectedDocs);
						}
					}
					StringList strLst = new StringList();
					Object obj = bows.get("revisions");
					if(obj!=null){
						if(obj instanceof StringList){
							strLst = (StringList) obj;
						}else if(obj instanceof String){
							strLst.add((String) obj);
						}
					}
					int revListSize = strLst.size();
					for(int j=0; j<revListSize; j++){
						String strKey = "revisions[" + j + "].id";
						slKeyRevision.add(strKey);
						slObjRevId.add((String)(bows.get(strKey)));                    			
					}
					if(slObjRevId != null){
						int lastValue;
						try {
							lastValue	=	Integer.parseInt(sLastRev);
							sLastRevId = (String)bows.get("revisions[" + lastValue + "].id");
						}
						catch(NumberFormatException numberFormatException){
							sLastRevId = (String)bows.get("revisions[" + sLastRev + "].id");
						}                    	
					}
	
					boolean bPresent = false;                    
					String sObjectRev = "";
					String sObjectRevNo = "";
					int slObjRevIdSize = slObjRevId.size();
					for(int iItr =0; iItr < slObjRevIdSize;iItr++){                		
						sObjectRev = (String)slKeyRevision.get(iItr);
						sObjectRevNo = sObjectRev.substring(sObjectRev.indexOf("[") + 1, sObjectRev.indexOf("]"));
						if(sLastRev.equals(sObjectRevNo)){
							if(connectedDocList.contains(sLastRevId)){
								bPresent = true;
								break;
							}
						}                		
					}
					if(bPresent == false && sLastRevId != null && sParentState != null){

						if(sParentState.equals(DomainConstants.STATE_CONTROLLED_FOLDER_RELEASE)) {

							String toolTip = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ControlledFolder.ShowHigherRevisionDisabledMsg", strLanguage);
							vecResult.add("<a title= \"" + toolTip + "\"> <img border='0' src='../common/images/iconSmallFaintHigherRevision.gif'/></a>");
						}else if(sParentState.equals(DomainConstants.STATE_CONTROLLED_FOLDER_CREATE)){
							String title = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ControlledFolder.ShowHigherRevisionActiveMsg", strLanguage);
							StringBuilder sBuff = new StringBuilder();

							String strURL = "../programcentral/emxProgramCentralFolderProcess.jsp?action=getLatestRev&amp;objectId=" +XSSUtil.encodeForURL(context, strObjectId) + "&amp;parentOID=" + XSSUtil.encodeForURL(context,sParentId) ;
							sBuff.append("<a href=\""+strURL+"\" target=\"listHidden\">");
							sBuff.append("<img border=\"0\" src=\"../common/images/iconSmallHigherRevision.gif\" title=\""+title+"\"></img></a>&#160;");
							vecResult.add(sBuff.toString());
						}else{
							vecResult.add(DomainObject.EMPTY_STRING);
						}
					}else{
						vecResult.add(DomainObject.EMPTY_STRING);
					}
				}else{
					vecResult.add(DomainObject.EMPTY_STRING);
				}
			}
		}
		return vecResult;

	}
	
	/* This method gets the field value for attribute 'Originator' on Basic information
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @throws Exception if the operation fails
    * @since PMC V6R2014
    */
    public String getOriginator(Context context, String args[]) throws Exception
    {
        return ProgramCentralUtil.getOriginator(context, args);
    }
    
    /**
     * Show multiple ownership page based on folder parent.
     * @param context The eMatrix <code>Context</code> object.
     * @param objectId is a folder id.
     * @return boolean value;
     * @throws Exception if operation fails.
     */
    public boolean hasAccessToViewMultipleOwnershipPage(Context context,String objectId)throws Exception
    { 
    	boolean hasAccess = true;
    	boolean isProjectTemplate = false;
    	
    	if(ProgramCentralUtil.isNotNullString(objectId)){
    		DomainObject dmoParentObject = DomainObject.newInstance(context, objectId);
    		
        	String strRelatioshipPattern = DomainObject.EMPTY_STRING;
        	String strTypePattern = DomainObject.EMPTY_STRING;
        	
        	StringList slBusSelect = new StringList();
        	StringList slRelSelect = new StringList();

        	if (dmoParentObject.isKindOf(context, ProgramCentralConstants.TYPE_CONTROLLED_FOLDER)) {
        		strRelatioshipPattern = DomainConstants.RELATIONSHIP_SUB_VAULTS + "," + DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
        		strTypePattern = ProgramCentralConstants.TYPE_CONTROLLED_FOLDER +"," + DomainObject.TYPE_PROJECT_TEMPLATE;
        	}else {
        		strRelatioshipPattern = strRelatioshipPattern = DomainConstants.RELATIONSHIP_SUB_VAULTS + "," + DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
        		strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT +","+DomainObject.TYPE_PROJECT_TEMPLATE;
        	}

        	slBusSelect.add(DomainConstants.SELECT_ID);
        	slBusSelect.add(DomainConstants.SELECT_TYPE);
        	slBusSelect.add(DomainConstants.SELECT_NAME);

        	MapList mlFolders = dmoParentObject.getRelatedObjects(context,
        			strRelatioshipPattern,
        			strTypePattern,
        			slBusSelect,
        			slRelSelect,
        			true,
        			true,
        			(short)0,
        			null,
        			null,
        			0);
        	if(mlFolders != null ){
        		for(int i=0;i<mlFolders.size();i++){
        			Map folderMap = (Map)mlFolders.get(i);
        			String objectType = (String)folderMap.get("type");
        			
        			if(objectType.equalsIgnoreCase(DomainObject.TYPE_PROJECT_TEMPLATE)){
        				isProjectTemplate = true;
        				break;
        			}
        		}
        	}
        	
        	if(isProjectTemplate){
        		hasAccess = false;
        	}
    	}
    	
    	return hasAccess;
    }
    
    /**
     * Show multiple ownership page according to folder access.
     * @param context The eMatrix <code>Context</code> object.
     * @param args holds information about object.
     * @return command setting info map.
     * @throws Exception if operation fails.
     */
    public Map getDynamicCategoriesForWorkSpaceFolder(Context context, String[]args )throws Exception
    {
        Map resultMap = new HashMap();
        try {

            MapList categoryMapList = new MapList();
            Map inputMap = JPO.unpackArgs(args);
            Map paramMap = (Map) inputMap.get("paramMap");
            String objectId = (String)paramMap.get("objectId");
            
            UIMenu uiMenu = new UIMenu();

            if(ProgramCentralUtil.isNotNullString(objectId)){
            	boolean hasAccessToViewPage = hasAccessToViewMultipleOwnershipPage(context,objectId);
            	
            	if(hasAccessToViewPage){
            		DomainObject folderObject = DomainObject.newInstance(context, objectId);
            		String folderAccessType = folderObject.getAttributeValue(context, DomainObject.ATTRIBUTE_ACCESS_TYPE);
            		
            		Map multipleOwnershipCmdMap = ProgramCentralUtil.createDynamicCommand(context,"DomainAccessTreeCategory",uiMenu,true);
            		Map multipleOwnershipPageMap = ProgramCentralUtil.createDynamicCommand(context,"APPDiscussionCommand",uiMenu,true);

            		String description = (String)multipleOwnershipCmdMap.get("description");
            		Map settingsMap = (Map)multipleOwnershipCmdMap.get("settings");
            		String name = (String)multipleOwnershipCmdMap.get("name");
            		String label = (String)multipleOwnershipCmdMap.get("label");
            		Map propertiesMap = (Map)multipleOwnershipCmdMap.get("properties");
            		String sHref = (String)multipleOwnershipCmdMap.get("href");
            		
            		if(folderAccessType.equalsIgnoreCase("Inherited")){
            			sHref = replaceQueryStringParameter(sHref,"toolbar=",DomainObject.EMPTY_STRING);
            		}


            		if(sHref != null && sHref.contains("&editLink=true")){
            			boolean editFlag = folderObject.checkAccess(context, (short) AccessConstants.cModify);
            			if(!editFlag){
            				sHref = replaceQueryStringParameter(sHref,"editLink=","false");
            			}
            			
            		}

            		multipleOwnershipPageMap.put("href", sHref);
            		multipleOwnershipPageMap.put("properties", propertiesMap);
            		multipleOwnershipPageMap.put("label", label);
            		multipleOwnershipPageMap.put("settings", settingsMap);
            		multipleOwnershipPageMap.put("description", description);
            		multipleOwnershipPageMap.put("name", name);

            		categoryMapList.add(multipleOwnershipPageMap);
            	}
            }
            
            resultMap.put("Children",categoryMapList);

        } catch (Exception e) {
        	throw new MatrixException(e);
        }
        return resultMap;

    }
}
