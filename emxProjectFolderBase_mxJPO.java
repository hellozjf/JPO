/*
 *  emxProjectFolderBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: emxProjectFolderBase.java.rca 1.10.2.1 Wed Dec 24 10:59:14 2008 ds-ksuryawanshi Experimental $
 */
 
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.PolicyItr;
import matrix.db.PolicyList;
import matrix.db.SelectConstants;
import matrix.util.MatrixException;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.UserTask;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FormatUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.program.PMCWorkspaceVault;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectRoleVaultAccess;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectTemplate;
import com.matrixone.apps.program.URL;


public class emxProjectFolderBase_mxJPO
{
	protected static final int TRIGGER_SUCCESS = 0;
	protected static final int TRIGGER_FAILURE = 1;
	private Context context;
	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public emxProjectFolderBase_mxJPO (Context context, String[] args)
	throws Exception{
		super();
		this.context = context; 
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return nothing
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
	}

	/****************************************************************************************************
	 *       Methods for Config Table Conversion Task
	 ****************************************************************************************************/
	/**
	 * This method is used to get the list of Workspace Folder objects.
	 * Used for PMCFolderSummary table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectId - containing one String entry for key "objectId"
	 * @return MapList containing the id of WorkSpace objects of the logged in user
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectFolder(Context context, String[] args) throws Exception
	{
		try
		{
			Map programMap = (Map) JPO.unpackArgs(args);
			String objectId = (String) programMap.get("objectId");

			// Added:22-Dec-08:kyp:R207:PRG Controlled Folder
			StringList slBusSelect = new StringList(DomainConstants.SELECT_ID);

			MapList mlFolders = getProjectFolder(context, objectId, slBusSelect, null);

			return mlFolders;
			// End:R207:PRG Controlled Folder
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * Returns the subfolders of project/project template or other subfolders. This method also considers the Controlled Folder
	 * presence, in order to return the latest subfolders only.
	 *
	 * @param context The Matrix Context object
	 * @param strParentId The parent id of the folder/project
	 * @param slBusSelect The bus select for the folders
	 * @param slRelSelect The relationship select for the folders returned
	 * @return The list of subfolders of provided parent
	 * @throws Exception if operation fails
	 */
	public MapList getProjectFolder(Context context, String strParentId, StringList slBusSelect, StringList slRelSelect)
	throws Exception
	{
		String objectId = strParentId;
		try
		{
			final String RELATIONSHIP_LINKED_FOLDERS = PropertyUtil.getSchemaProperty(context, "relationship_LinkedFolders");
			final String TYPE_CONTROLLED_FOLDER = PropertyUtil.getSchemaProperty(context, "type_ControlledFolder");

			DomainObject dmoParentObject = DomainObject.newInstance(context, objectId);
			boolean isToBeProcessForLatestFolders = false;
			String strRelatioshipPattern = "";
			String strTypePattern = "";

			//        Finding all the sub Types of Controlled Folder
			//String strMQL = "print type \"" + DomainConstants.TYPE_CONTROLLED_FOLDER +"\" select derivative dump |";
			String strMQL = "print type $1 select $2 dump $3";
			String strResult = MqlUtil.mqlCommand(context,strMQL,DomainConstants.TYPE_CONTROLLED_FOLDER,"derivative","|");
			StringList slControlledFolderTypeHierarchy = FrameworkUtil.split(strResult, "|");

			// Dont forget to add Controlled Folder type itself into this listing
			slControlledFolderTypeHierarchy.add(DomainConstants.TYPE_CONTROLLED_FOLDER);

			//
			// There are following 3 type that must be checked. The parent type can either be
			// Controlled Folder
			// Workspace Vault
			// or Project Space/Template etc
			// Check the type and decide which relationships are to be used to get the folder listing
			// Please make sure to check the isKindOf rather than direct type comparision also,
			// the order of comparision is also important. We should check Controlled Folder first then the Workspace Vault
			// get yield correct result.
			//
			if (dmoParentObject.isKindOf(context, TYPE_CONTROLLED_FOLDER)) {
				strRelatioshipPattern = DomainConstants.RELATIONSHIP_SUB_VAULTS + "," + RELATIONSHIP_LINKED_FOLDERS;
				strTypePattern = TYPE_CONTROLLED_FOLDER;

				isToBeProcessForLatestFolders = true;
			}
			else if (dmoParentObject.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT)) {
				strRelatioshipPattern = DomainConstants.RELATIONSHIP_SUB_VAULTS;
				strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;
			}
			else {
				strRelatioshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
				strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT + "," + TYPE_CONTROLLED_FOLDER;

				isToBeProcessForLatestFolders = true;
			}

			if (slBusSelect == null) {
				slBusSelect = new StringList();
			}
			if (!slBusSelect.contains(DomainConstants.SELECT_ID)) {
				slBusSelect.add(DomainConstants.SELECT_ID);
			}
			if (!slBusSelect.contains(DomainConstants.SELECT_TYPE)) {
				slBusSelect.add(DomainConstants.SELECT_TYPE);
			}
			if (!slBusSelect.contains(DomainConstants.SELECT_NAME)) {
				slBusSelect.add(DomainConstants.SELECT_NAME);
			}
			if (!slBusSelect.contains(DomainConstants.SELECT_REVISION)) {
				slBusSelect.add(DomainConstants.SELECT_REVISION);
			}

			//Added:2-Feb-09:yox:R207:PRG Bug :366767
			if (!slBusSelect.contains(DomainConstants.SELECT_LAST_ID)) {
				slBusSelect.add(DomainConstants.SELECT_LAST_ID);
			}
			//End:R207:PRG Bug :366767

			if (slRelSelect == null) {
				slRelSelect = new StringList();
			}

			MapList mlFolders = dmoParentObject.getRelatedObjects(context,        // context.
					strRelatioshipPattern,   // rel filter.
					strTypePattern,            // type filter.
					slBusSelect,  // business object selectables.
					slRelSelect,           // relationship selectables.
					false,          // expand to direction.
					true,           // expand from direction.
					(short)1,  // level
					null,           // object where clause
					null);          // relationship where clause

			//
			// If there are Controlled Folders in the result list then we shall onlt show the latest revisions of these
			// to do this we are only getting those objects whose latest revision id is equal to their id itself.
			// We dont have to do anything extra as for normal folder this comparision will always evaluate to be true.
			//
			if (isToBeProcessForLatestFolders) {
				String strFolderId = "";
				String strFolderType = "";
				String strFolderName = "";
				String strFolderRevision = "";
				String strLastFolderRevision = "";

				String strCurrentFolderType = "";
				String strCurrentFolderName = "";
				String strCurrentFolderRevision = "";

				Map mapFolder = null;
				Map mapCurrentFolder = null;
				Map mapLatestFolder = null;
				MapList mlFilteredFolders = new MapList();

				for (Iterator itrFolders = mlFolders.iterator(); itrFolders.hasNext();) {
					mapFolder = (Map) itrFolders.next();

					strFolderId = (String)mapFolder.get(DomainConstants.SELECT_ID);
					strFolderType = (String)mapFolder.get(DomainConstants.SELECT_TYPE);
					strFolderName = (String)mapFolder.get(DomainConstants.SELECT_NAME);
					strFolderRevision = (String)mapFolder.get(DomainConstants.SELECT_REVISION);

					// Assume this is the latest folder
					mapLatestFolder = mapFolder;
					strLastFolderRevision = strFolderRevision;

					if (mlFilteredFolders.contains(mapLatestFolder)) {
						continue;
					}

					// The controlled folders are revisionable, so we shall check for latest revision only for these folders
					if (slControlledFolderTypeHierarchy.contains(strFolderType)) {
						//
						// Following code finds the latest revisions among the connected controlled subfolders folders
						//
						for (Iterator itrFolders2 = mlFolders.iterator(); itrFolders2.hasNext();) {
							mapCurrentFolder = (Map) itrFolders2.next();

							strCurrentFolderType = (String)mapCurrentFolder.get(DomainConstants.SELECT_TYPE);
							strCurrentFolderName = (String)mapCurrentFolder.get(DomainConstants.SELECT_NAME);
							strCurrentFolderRevision = (String)mapCurrentFolder.get(DomainConstants.SELECT_REVISION);

							// If we are looking at the same Type Name of the folder then check for Revision
							if (strFolderType.equals(strCurrentFolderType) && strFolderName.equals(strCurrentFolderName)) {
								if (Integer.parseInt(strCurrentFolderRevision) > Integer.parseInt(strLastFolderRevision)) {
									mapLatestFolder = mapCurrentFolder;
									strLastFolderRevision = strCurrentFolderRevision;
								}
							}
						}
					}

					if (!mlFilteredFolders.contains(mapLatestFolder)) {
						mlFilteredFolders.add(mapLatestFolder);
					}
				}

				mlFolders = mlFilteredFolders;
			}

			return mlFolders;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * method is used to calculate the size of the file object for column Size for folder summary table. 
	 * @param context The Matrix Context object
	 * @param args array containing list of objects to be displayed on S.B.
	 * @return The Vector containing object's size
	 * @throws Exception if operation fails
	 */
	public Vector getColumnDocumentFileSizeData(Context context,String[] args) throws Exception
	{
		// method is revamped for issue IR-127403V6R2013 :: PRG:RG6:R213:21-Sept-2011
		Vector vectorFileSize = new Vector();
		Map programMap = (Map) JPO.unpackArgs(args);

		MapList mapObjectList = (MapList) programMap.get("objectList");
		NumberFormat numberFormat=NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2);
		String arrObjectIds [] = null;
		int size = 0;
		if(null != mapObjectList)
		{	
			size = mapObjectList.size();
		}
		else
		{
			throw new IllegalArgumentException();
		}

		arrObjectIds = new String[size];
		int i=0;
		Map mObjectsInfoMap = new HashMap();
		for (Iterator itrDocuments = mapObjectList.iterator(); itrDocuments.hasNext();)
		{
			Map mapDocument = (Map) itrDocuments.next();
			String strObjectId = (String)mapDocument.get(DomainConstants.SELECT_ID);
			arrObjectIds[i++] = strObjectId;
			mObjectsInfoMap.put(strObjectId, mapDocument);
		}

		//DomainObject dMasterObj = DomainObject.newInstance(context);
		
		Map masterObjectMap = new HashMap();
		StringList slObjectSelects = new StringList(4);
		slObjectSelects.add(DomainConstants.SELECT_ID);
		slObjectSelects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
		slObjectSelects.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
		slObjectSelects.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TITLE);

		BusinessObjectWithSelectList folderContentWithSelectList = null;
		folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,arrObjectIds,slObjectSelects);
		for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
		{
			BusinessObjectWithSelect bows  = itr.obj();
			String sChildObjId        = bows.getSelectData(DomainConstants.SELECT_ID);
			Map mapDocument           = (Map) mObjectsInfoMap.get(sChildObjId);
			String strIsVersionObject = bows.getSelectData(CommonDocument.SELECT_IS_VERSION_OBJECT);
			String sIsDocumentsType   = bows.getSelectData(ProgramCentralConstants.SELECT_IS_DOCUMENTS);

			String strFileSize= null;
			if("true".equalsIgnoreCase(sIsDocumentsType) && "true".equalsIgnoreCase(strIsVersionObject))
			{
				String strMasterId =(String) mapDocument.get("masterId");
				String strFileTitle     = (String) bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TITLE);
				String SELECT_FILE_SIZE = "format.file["+strFileTitle+"].size";

				if(ProgramCentralUtil.isNotNullString(strMasterId))
				{
					Map mMasterObjInfo = (Map)masterObjectMap.get(sChildObjId);
					
					if(mMasterObjInfo == null || mMasterObjInfo.isEmpty()){
						
						StringList slMasterObjectSelects = new StringList(3);
				slMasterObjectSelects.add(DomainConstants.SELECT_ID);
				slMasterObjectSelects.add(SELECT_FILE_SIZE);
				slMasterObjectSelects.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);

						DomainObject masterObject = DomainObject.newInstance(context, strMasterId);
						mMasterObjInfo = masterObject.getInfo(context, slMasterObjectSelects);
						masterObjectMap.put(sChildObjId,mMasterObjInfo);
					}

			        if(null != mMasterObjInfo)
					{
						String sIsMasterObjOfTypeDocuments = (String)mMasterObjInfo.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS);

						if("true".equalsIgnoreCase(sIsMasterObjOfTypeDocuments))
						{
							strFileSize = (String)mMasterObjInfo.get(SELECT_FILE_SIZE);
							String strFormatedFileSize = "0";
							if(ProgramCentralUtil.isNotNullString(strFileSize))
							{
								strFormatedFileSize = numberFormat.format((Float.parseFloat(strFileSize))/1024);
								if(null != strFormatedFileSize  && strFormatedFileSize.length() <= 6)
								{
									vectorFileSize.add(strFormatedFileSize+" KB");
								}
								else
								{
									vectorFileSize.add(numberFormat.format((Float.parseFloat(strFileSize))/(1024*1024))+" MB");
								}
							}
						}
					}
				}
				//End Modified PRG:RG6:R211:DOCMGNT
			}
			// if size of the object is not available then add blank in the return list
			if(ProgramCentralUtil.isNullString(strFileSize))
			{
				vectorFileSize.add(DomainConstants.EMPTY_STRING);
			}
		}

		return vectorFileSize;
	}


	/**
	 * This method is used to get the list of content name as objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        objectList - Contains MapList of maps which contains content name as objects.
	 * @return Vector containing the content value as int.
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public static Vector getContent(Context context, String[] args)
	throws Exception
	{
		Vector vecContent = new Vector();
		try
		{
			com.matrixone.apps.common.WorkspaceVault workspaceVault =
				(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
						DomainConstants.TYPE_WORKSPACE_VAULT);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			int numberOfFiles = 0;
			Map objectMap = null;

			String globalRead = null;
			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] =
					(String) objectMap.get(DomainObject.SELECT_ID);
				arrayCount++;
			}
			StringList busSelect = new StringList(1);
			busSelect.add(workspaceVault.SELECT_CONTENT_ID2);

			MapList actionList =
				DomainObject.getInfo(context, objIdArr, busSelect);
			int actionListSize = 0;

			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			for (int i = 0; i < actionListSize; i++)
			{
				objectMap = (Map) actionList.get(i);
				//Must get content number for each workspace vault
				Object contentIdObject =
					objectMap.get(workspaceVault.SELECT_CONTENT_ID2);

				if (contentIdObject != null)
				{
					//Folder has only one content
					if ((contentIdObject instanceof String) == true)
					{
						numberOfFiles = 1;
					}
					//Else, Folder has multiple contents
					else
					{
						StringList contentIdList =
							(StringList) contentIdObject;
						numberOfFiles = contentIdList.size();
					}
				}
				else
				{
					numberOfFiles = 0;
				}
				vecContent.add(String.valueOf(numberOfFiles));
			}
			//end - for loop
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return vecContent;
		}
	}

	/**
	 * This method is used to get the default access as objects.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *      objectList - Contains MapList of maps which contains default access as objects.
	 * @return Vector containing the  default access value as String.
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public static Vector getDefaultAccess(Context context, String[] args)
	throws Exception
	{
		Vector vecDefaultAccess = new Vector();

		try
		{
			com.matrixone.apps.common.WorkspaceVault workspaceVault =
				(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
						DomainConstants.TYPE_WORKSPACE_VAULT);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			Map objectMap = null;
			String globalRead = null;

			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] =
					(String) objectMap.get(DomainObject.SELECT_ID);
				arrayCount++;
			}
			StringList busSelect = new StringList(1);
			busSelect.add(workspaceVault.SELECT_GLOBAL_READ);

			MapList actionList =
				DomainObject.getInfo(context, objIdArr, busSelect);
			int actionListSize = 0;

			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			for (int i = 0; i < actionListSize; i++)
			{
				objectMap = (Map) actionList.get(i);
				String strGlobalRead =
					(String) objectMap.get(workspaceVault.SELECT_GLOBAL_READ);

				//Determine string value to display for security
				if ("True".equalsIgnoreCase(strGlobalRead))
				{
					globalRead = "emxProgramCentral.Common.Public";
				}
				else
				{
					globalRead = "emxProgramCentral.Common.Private";
				}
				String strAccess = EnoviaResourceBundle.getProperty(context, "ProgramCentral", globalRead, context.getSession().getLanguage());
				vecDefaultAccess.add(strAccess);
			}
			//end - for loop
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return vecDefaultAccess;
		}
	}

	/**
	 * This method is used to get the list of access types as objects.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *         objectList - Contains MapList of Maps which contains access types as objects.
	 * @return Vector containing the access types as String.
	 * @throws Exception if the operation fails
	 * @since PMC 10-6
	 */
	public static Vector getAccessType(Context context, String[] args)
	throws Exception
	{
		Vector vecAccessType = new Vector();
		try
		{
			com.matrixone.apps.common.WorkspaceVault workspaceVault =
				(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
						DomainConstants.TYPE_WORKSPACE_VAULT);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			String[] objIdArr = new String[objectList.size()];
			int arrayCount = 0;
			Map objectMap = null;

			i18nNow i18nnow = new i18nNow();
			String language = context.getSession().getLanguage();
			String inheritedRange = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Inherited", language);
			String specificRange = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Specific", language);
			String strAccessType = "";

			while (objectListIterator.hasNext())
			{
				objectMap = (Map) objectListIterator.next();
				objIdArr[arrayCount] =
					(String) objectMap.get(DomainObject.SELECT_ID);
				arrayCount++;
			}
			StringList busSelect = new StringList(1);
			busSelect.add(workspaceVault.SELECT_ACCESS_TYPE);

			MapList actionList =
				DomainObject.getInfo(context, objIdArr, busSelect);

			int actionListSize = 0;
			if (actionList != null)
			{
				actionListSize = actionList.size();
			}

			for (int i = 0; i < actionListSize; i++)
			{
				objectMap = (Map) actionList.get(i);
				String selectAccessType =
					(String) objectMap.get(workspaceVault.SELECT_ACCESS_TYPE);
				strAccessType = "Specific".equals(selectAccessType) ? specificRange : inheritedRange;
				vecAccessType.add(strAccessType);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
		finally
		{
			return vecAccessType;
		}
	}


	/**
	 * Gets the Default Access.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return Object - boolean true if the operation is successful
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getDefaultAccessValue(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) requestMap.get("objectId");
		String mode = (String) requestMap.get("mode");
		String strAccess = "";

		com.matrixone.apps.common.WorkspaceVault workspaceVaultSource =
			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
					DomainConstants.TYPE_WORKSPACE_VAULT);

		String emxTableRowId = (String)requestMap.get("emxTableRowId");
		if(null!=emxTableRowId&& !"".equals(emxTableRowId))
		{
			StringList slObjectIdList =FrameworkUtil.splitString(emxTableRowId,"|");
			emxTableRowId=(String)slObjectIdList.get(1);
		}

		else if(null == emxTableRowId || "".equals(emxTableRowId))
		{
			emxTableRowId = strObjectId;
		}

		boolean  defaultSecurity=false;
		if(emxTableRowId!=null && emxTableRowId.length()!=0)
		{
			workspaceVaultSource.setId(emxTableRowId);
			defaultSecurity = "True".equalsIgnoreCase((String)workspaceVaultSource.getInfo(context, WorkspaceVault.SELECT_GLOBAL_READ));
		}
		com.matrixone.apps.common.WorkspaceVault workspaceVault =
			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
					DomainConstants.TYPE_WORKSPACE_VAULT);
		StringList busSelect = new StringList(1);
		busSelect.add(WorkspaceVault.SELECT_GLOBAL_READ);
		DomainObject dom = DomainObject.newInstance(context, strObjectId);
		Map objectMap = dom.getInfo(context, busSelect);
		String globalRead = "";
		String strGlobalRead =
			(String) objectMap.get(WorkspaceVault.SELECT_GLOBAL_READ);

		// Determine string value to display for security
		if ("True".equalsIgnoreCase(strGlobalRead))
		{
			globalRead = "emxProgramCentral.Common.Public";
		}
		else
		{
			globalRead = "emxProgramCentral.Common.Private";
		}
		if (mode==null || mode.equalsIgnoreCase("view")) {
			strAccess = EnoviaResourceBundle.getProperty(context, "ProgramCentral", globalRead, context.getSession().getLanguage());
		}
		else if (mode.equalsIgnoreCase("edit")) {

			strAccess = "<table border=\"0\">";

			strAccess += "<tr><td><input type=\"radio\" name=\"DefaultAccess\" id=\"DefaultAccess\" value=\"True\"";
			if ("True".equalsIgnoreCase(strGlobalRead)) {
				strAccess += " checked";
			}
			strAccess += "></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Public", context.getSession().getLanguage()); 
			strAccess += "</td></tr>";
			strAccess += "<tr><td><input type=\"radio\" name=\"DefaultAccess\" id=\"DefaultAccess\" value=\"False\"";
			if ("False".equalsIgnoreCase(strGlobalRead)) {
				strAccess += " checked";
			}
			strAccess += "></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Private", context.getSession().getLanguage());
			strAccess += "</td></tr></table>";

		}

		else if (mode.equalsIgnoreCase("create")) {
			strAccess = "<table border=\"0\">";

			strAccess += "<tr><td><input type='radio' name='DefaultAccess' id='DefaultAccess' value='True'";
			if (defaultSecurity) {
				strAccess += " checked='checked'";
			}
			strAccess += "/></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Public", context.getSession().getLanguage()); 
			strAccess += "</td></tr>";

			strAccess += "<tr><td><input type='radio' name='DefaultAccess' id='DefaultAccess' value='False'";
			if (!defaultSecurity) {
				strAccess += " checked='checked'";
			}
			strAccess += "/></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Private", context.getSession().getLanguage()); 
			strAccess += "</td></tr></table>";

		}
		return strAccess;
	}
	/**
	 *Method is called for column Inherit access of the tables PMCFolderMemberAccessViewTable and PMCFolderRoleAccessViewTable
	 *in order to show the inherit access info for the workspace vault type of objects. 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getVaultInheritAccessValue(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Vector vcInheritAccessValue = new Vector();
		MapList mapObjectList = (MapList) programMap.get("objectList");
		Map mapVaultInheritAccess = null;
		String strObjectId = null;
		String strAccessType = null;
		String sIsWkVault = null;       //Added:PRG:RG6:R212:IR-110145V6R2012x 
		DomainObject dmoVault = DomainObject.newInstance(context);
		i18nNow i18nnow = new i18nNow();
		final String STR_INHERITED_ACCESS_TYPE = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Inherited", "en");
		//Added:PRG:RG6:R212:IR-110145V6R2012x 
		StringList slObjectSelects = new StringList();
		slObjectSelects.add(WorkspaceVault.SELECT_ACCESS_TYPE);
		slObjectSelects.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		//End Added:PRG:RG6:R212:IR-110145V6R2012x

		for (Iterator iterator = mapObjectList.iterator(); iterator.hasNext();)
		{
			mapVaultInheritAccess = (Map) iterator.next();
			strObjectId = (String) mapVaultInheritAccess.get(DomainConstants.SELECT_ID);
			dmoVault.setId(strObjectId);
			Map mObjectInfo = dmoVault.getInfo(context, slObjectSelects);
			//Added:PRG:RG6:R212:IR-110145V6R2012x 
			if(null != mObjectInfo)
			{
				strAccessType = (String)mObjectInfo.get(WorkspaceVault.SELECT_ACCESS_TYPE);
				sIsWkVault = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			}
			//End Added:PRG:RG6:R212:IR-110145V6R2012x
			boolean isWkVault = "true".equalsIgnoreCase(sIsWkVault);
			if(isWkVault)
			{
				if (STR_INHERITED_ACCESS_TYPE.equalsIgnoreCase(strAccessType))
				{
					strAccessType = "emxProgramCentral.Access.AccessType.Inherited.Yes";
				}
				else
				{
					strAccessType = "emxProgramCentral.Access.AccessType.Specific.No";
				}
			}
			else
			{
				strAccessType = "";
			}
			vcInheritAccessValue.add(EnoviaResourceBundle.getProperty(context, "ProgramCentral", strAccessType, context.getSession().getLanguage()));
		}
		return vcInheritAccessValue;
	}
	/**
	 * Gets the Default Access.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return Object - boolean true if the operation is successful
	 * @throws Exception
	 *             if operation fails
	 * @since R210
	 */
	public String getAccessTypeValue(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) requestMap.get("objectId");

		String mode = (String) requestMap.get("mode");
		String strAccess = null;
		WorkspaceVault workspaceVaultSource = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);

		String emxTableRowId = (String)requestMap.get("emxTableRowId");
		i18nNow i18nnow = new i18nNow();
		final String STR_INHERITED_ACCESS_TYPE = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Inherited", "en");
		if(null!=emxTableRowId&& !"".equals(emxTableRowId))
		{
			StringList slObjectIdList =FrameworkUtil.splitString(emxTableRowId,"|");
			emxTableRowId=(String)slObjectIdList.get(1);
		}

		else if(null == emxTableRowId || "".equals(emxTableRowId))
		{
			emxTableRowId = strObjectId;
		}

		boolean  isInherited=false;
		String accessType= DomainConstants.EMPTY_STRING;
		String isWorkspaceVault =DomainConstants.EMPTY_STRING;
		if(emxTableRowId!=null && emxTableRowId.length()!=0)
		{
			workspaceVaultSource.setId(emxTableRowId);

			StringList objectSelects = new StringList(2);
			objectSelects.addElement(WorkspaceVault.SELECT_ACCESS_TYPE);
			objectSelects.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			Map infoMap = workspaceVaultSource.getInfo(context, objectSelects);
			accessType = (String)infoMap.get(WorkspaceVault.SELECT_ACCESS_TYPE);
			isWorkspaceVault = (String)infoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);

			isInherited = STR_INHERITED_ACCESS_TYPE.equalsIgnoreCase(accessType);
		}
		if(strObjectId != null){
			DomainObject dObj=DomainObject.newInstance(context);
			dObj.setId(strObjectId);
			/* if call for parent folder creation from structure browser,
			 * object id of project space came for 'strobjectid'
			 * and by default for parent folder creation inherited is true.
			 */
			if(!"true".equalsIgnoreCase(isWorkspaceVault)){
				isInherited = true;
			}
		}
		String sAccessType = DomainConstants.EMPTY_STRING;
		String strAccessType = accessType;//(String) objectMap.get(WorkspaceVault.SELECT_ACCESS_TYPE);

		// Determine string value to display for security
		if (STR_INHERITED_ACCESS_TYPE.equalsIgnoreCase(strAccessType))
		{
			sAccessType = "emxProgramCentral.Access.AccessType.Inherited.Yes";
		}
		else
		{
			sAccessType = "emxProgramCentral.Access.AccessType.Specific.No";
		}
		if (mode==null || mode.equalsIgnoreCase("view"))
		{
			strAccess = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					sAccessType, context.getSession().getLanguage());
		}
		else if (mode.equalsIgnoreCase("edit"))
		{
			strAccess = "<table border=\"0\">";

			strAccess += "<tr><td><input type=\"radio\" name=\"AccessType\" id=\"AccessType\" value=\"Inherited\"";
			if (STR_INHERITED_ACCESS_TYPE.equalsIgnoreCase(strAccessType))
			{
				strAccess += " checked";
			}
			strAccess += "></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Access.AccessType.Inherited.Yes", context.getSession().getLanguage()); 
			strAccess += "</td></tr>";

			strAccess += "<tr><td><input type=\"radio\" name=\"AccessType\" id=\"AccessType\" value=\"Specific\"";
			if ("Specific".equalsIgnoreCase(strAccessType))
			{
				strAccess += " checked";
			}
			strAccess += "></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Access.AccessType.Specific.No", context.getSession().getLanguage());
			strAccess += "</td></tr></table>";
		}
		else if (mode.equalsIgnoreCase("create"))
		{
			strAccess = "<table border=\"0\">";

			strAccess += "<tr><td><input type='radio' name='AccessType' id='AccessType' value='Inherited'";
			if (isInherited)
			{
				strAccess += " checked='checked'";
			}
			strAccess += "/></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Access.AccessType.Inherited.Yes", context.getSession().getLanguage());
			strAccess += "</td></tr>";

			strAccess += "<tr><td><input type='radio' name='AccessType' id='AccessType' value='Specific'";
			if (!isInherited)
			{
				strAccess += " checked='checked'";
			}
			strAccess += "/></td><td>";
			strAccess += EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Access.AccessType.Specific.No", context.getSession().getLanguage()); 
			strAccess += "</td></tr></table>";
		}

		return strAccess;
	}


	/*
	 * This method updates field value for attribute 'Risk Status'.
	 *
	 * @param context the eMatrix <code>Context</code> object @param args
	 * holds input arguments. @throws Exception if the operation fails
	 *
	 * @since PMC 10-6-SP2
	 */
	public void updateDefaultAccess(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");

		String newGlobalRead = (String)paramMap.get("New Value");

		DomainObject dom = DomainObject.newInstance(context, objectId);
		dom.open(context);
		dom.setAttributeValue(context,
				PropertyUtil.getSchemaProperty(context,"attribute_GlobalRead"),
				newGlobalRead);
		dom.close(context);
	}


	public String getRevision(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strObjectId = (String) requestMap.get("objectId");

		com.matrixone.apps.common.WorkspaceVault workspaceVault =
			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
					DomainConstants.TYPE_WORKSPACE_VAULT);
		String revision=workspaceVault.getUniqueName(context);
		requestMap.put("Revision",revision);
		return revision;
	}

	/**
	 * getPolicyWorkSpaceVault - This Method populates the policy for workspace vault
	 * Used in PMCProjectVaultCreateForm WebForm
	 * This function was added to fix the BUG : 358820
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - Contains a MapList of Maps which contains object names
	 * @return Vector of "Project Role" values for the combobox
	 * @throws Exception if the operation fails
	 * @since PMC V6R2009x
	 */
	public HashMap getPolicyWorkSpaceVault(Context context, String[] args)
	throws Exception
	{
		try {

			//
			// Get the selected type on the folder create form
			// The form selection for type can come in various forms so it needs to be handled correctly.
			// Ex. type=_selectedType:TestChildVault,type_ProjectVault,type_TectChildVault OR
			//     type=type_ProjectVault OR
			//     type=Workspace Vault
			//
			Map programMap = (Map)JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String strSelectedType = (String)requestMap.get("type");
			if (strSelectedType != null) {
				if (strSelectedType.indexOf(":") != -1) {
					// type=_selectedType:TestChildVault,type_ProjectVault,type_TectChildVault
					StringList slSplitType = FrameworkUtil.split(strSelectedType, ":");
					if (slSplitType.size() > 1) {
						strSelectedType = (String)slSplitType.get(1);
						slSplitType = FrameworkUtil.split(strSelectedType, ",");
						if (slSplitType.size() > 0) {
							strSelectedType = (String)slSplitType.get(0);
						}
						else {
							strSelectedType = null;
						}
					}
					else {
						strSelectedType = null;
					}
				}
				else {
					// If it is just command separated value then take the first value
					StringList slSplitType = FrameworkUtil.split(strSelectedType, ",");
					if (slSplitType.size() > 0) {
						strSelectedType = (String)slSplitType.get(0);
					}
					else {
						strSelectedType = null;
					}
				}
			}
			if (strSelectedType == null) {
				strSelectedType = DomainConstants.TYPE_WORKSPACE_VAULT; // Default!
			}
			else {
				// If it is symbolic name then convert it to real name
				if (strSelectedType.startsWith("type_")) {
					strSelectedType = PropertyUtil.getSchemaProperty(context, strSelectedType);
				}
			}

			//
			// Gather the names of policies of parent type of Controlled Folder
			//
			Policy policy = null;
			String strPolicy = null;
			StringList slParentPolicies = new StringList();

			if (DomainConstants.TYPE_CONTROLLED_FOLDER.equals(strSelectedType)) {
				// Get all the parent type
				BusinessType btControlledFolder = new BusinessType(DomainConstants.TYPE_CONTROLLED_FOLDER, context.getVault());
				btControlledFolder.open(context);
				StringList slParentTypes = btControlledFolder.getParents(context);
				btControlledFolder.close(context);

				String strParentType = null;
				PolicyList parentPolicies = null;
				BusinessType btParentFolder = null;
				for (StringItr stringItr = new StringItr(slParentTypes); stringItr.next();) {
					strParentType = stringItr.obj();

					// Get policies of parent types
					btParentFolder = new BusinessType(strParentType, context.getVault());
					btParentFolder.open(context);
					parentPolicies = btParentFolder.getPolicies(context);
					btParentFolder.close(context);

					// Accumulate all the policies
					for (PolicyItr policyItr = new PolicyItr(parentPolicies); policyItr.next();) {
						policy = policyItr.obj();
						strPolicy = policy.getName();
						slParentPolicies.add(strPolicy);
					}
				}
			}

			String sLanguage = context.getSession().getLanguage();

			BusinessType btWorkSpaceVault = new BusinessType(strSelectedType, context.getVault());
			btWorkSpaceVault.open(context);
			PolicyList strList = btWorkSpaceVault.getPolicies(context);
			btWorkSpaceVault.close(context);

			StringList slWorkSpaceVault = new StringList();
			StringList slWorkSpaceVaultTranslated = new StringList();
			HashMap map = new HashMap();
			for(int i=0; i<strList.size();i++){
				policy = (Policy)strList.elementAt(i);
				strPolicy = policy.getName();

				// Template Workspace Vault policy is only used in TMC, so do not show this in PMC
				if (strPolicy == null || DomainConstants.POLICY_TEMPLATE_WORKSPACE_VAULT.equals(strPolicy)) {
					continue;
				}

				// For controlled folders we have to skip the policies of the parent type
				if (DomainConstants.TYPE_CONTROLLED_FOLDER.equals(strSelectedType)) {
					if (slParentPolicies.contains(strPolicy)) {
						continue;
					}
				}

				slWorkSpaceVault.add(strPolicy);
				slWorkSpaceVaultTranslated.add(i18nNow.getAdminI18NString("Policy", strPolicy, sLanguage));
			}

			map.put("field_choices", slWorkSpaceVault);
			map.put("field_display_choices", slWorkSpaceVaultTranslated);

			return  map;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * postProcessActions - This Method will do post process action after creatiinf Folders of type
	 * Workspace Vault and Controlled Folder
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *    objectList - Contains a MapList of Maps which contains object names
	 * @return nothing
	 * @throws Exception if the operation fails
	 * @since PMC V6R2009x
	 */

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void postProcessActions(Context context, String[] args)
	throws Exception
	{

		try {
			com.matrixone.apps.program.ProjectSpace project =
				(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
			com.matrixone.apps.common.WorkspaceVault workspaceVault =
				(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
						DomainConstants.TYPE_WORKSPACE_VAULT);
			com.matrixone.apps.common.WorkspaceVault workspaceVaultSource =
				(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
						DomainConstants.TYPE_WORKSPACE_VAULT);
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			
			// Created/Cloned objects Id
			String objectId = (String) paramMap.get("objectId");
			BusinessObject bus=new BusinessObject(objectId);
			String STR_DATA_VAULT=DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
			String projectOwner=ProgramCentralConstants.EMPTY_STRING;
			String strFolderName = (String)requestMap.get("Name");
			workspaceVault.setId(objectId);
			
			String strTableRowID = (String) requestMap.get("emxTableRowId");
			if(ProgramCentralUtil.isNotNullString(strTableRowID))
			{	String projectID = "";
				Map rowIDList = ProgramCentralUtil.parseTableRowId(context, strTableRowID);
				String parentOId = (String) rowIDList.get("parentOId");
				if(ProgramCentralUtil.isNullString(parentOId))
				{
					parentOId = (String) rowIDList.get("objectId");
					projectID = parentOId;
				}
				else
				{
					projectID = UserTask.getProjectId(context, parentOId);
				}
			 	 StringList busSelect  = new StringList(1);
				busSelect.addElement(DomainConstants.SELECT_OWNER);
				
				
					
					DomainObject projObject = DomainObject.newInstance(context, projectID);
					Map projectInfoMap = projObject.getInfo(context, busSelect);
					 projectOwner = (String)projectInfoMap.get(DomainConstants.SELECT_OWNER);
				
			
			}
			/*
			//below if condition needs to be identified by the schema change for controlled folder policy	
			if(workspaceVault.isKindOf(context, ProgramCentralConstants.TYPE_CONTROLLED_FOLDER))
			{
				workspaceVault.removePrimaryOwnership(context);
			}
			*/
			// Selected Object for Clone/ Sub Folder creation
			String emxTableRowId = (String) requestMap.get("emxTableRowId");
			String sSelctedObjetsParentId = "";

			//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			String isClone = (String) requestMap.get("IsClone");
			boolean isCloneOperation =  "true".equalsIgnoreCase(isClone); 

			String sParsedObjectId = "";
			String sParsedParentObjId = "";
			//Modified PRG:RG6:R212:30-Jun-2011:Related to unique workspace vault creation issue
			Map mParsedRowId = ProgramCentralUtil.parseTableRowId(context, emxTableRowId);
			if(null != mParsedRowId)
			{
				sParsedObjectId = (String)mParsedRowId.get("objectId");   //selected object as parent for creation 
				sParsedParentObjId = (String)mParsedRowId.get("parentOId");  //selected object as parent for clone 
				emxTableRowId = sParsedObjectId;  // selected object
			}
			//End Modified:PRG:RG6:R212:30-Jun-2011:Related to unique workspace vault creation issue 

			//Added:Dec 20, 2010:HP5:R211:PRG
			String strObjectId = "";
			String revision = "";
			/*
			 * if object is selected for creation then use parsed emxtablerow id for getting parent object for revision
			 * if no object selected then use tree as parent and for getting the revision 
			 */
			//Modified:PRG:RG6:R212:30-Jun-2011:
			if(ProgramCentralUtil.isNotNullString(sParsedParentObjId))
			{
				if(isCloneOperation)
				{
					strObjectId = sParsedParentObjId;
				}
				else
				{
					strObjectId = sParsedObjectId;
				}

			}//End Modified:PRG:RG6:R212:30-Jun-2011
			else if(requestMap.containsKey("parentOID"))
			{
				strObjectId = (String) requestMap.get("parentOID");
			}
			else
			{
				strObjectId = (String) requestMap.get("objectId");
			}


			try
			{
				if(ProgramCentralUtil.isNotNullString(strObjectId))   //Modified Line:PRG:RG6:R212:30-Jun-2011
				{
					//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
					String sCommandStatement = "print bus $1 select $2 dump $3";
					revision =  MqlUtil.mqlCommand(context, sCommandStatement,strObjectId, "physicalid", "|"); 
					//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End
				}
				else
				{
					throw new IllegalArgumentException("No parent Object id is not avaialable");
				}
			}
			catch( IllegalArgumentException e)
			{
				e.printStackTrace();
				throw new IllegalArgumentException("strObjectId");
			}

			String security          = (String)requestMap.get("DefaultAccess");
			String accessType        = (String)requestMap.get("AccessType");
			String stpolicy          = (String)requestMap.get("Policy");
			if (!stpolicy.equals(DomainConstants.POLICY_CONTROLLED_FOLDER)){
				//check: same name folder should not exist on same level
				String SELECT_FOLDER_NAME = "from[Data Vaults].to.name";
				String SELECT_SUB_FOLDER_NAME = "from[Sub Vaults].to.name";
				
				String SELECT_FOLDER_REVISION = "from[Data Vaults].to.revision";
				String SELECT_SUB_FOLDER_REVISION = "from[Sub Vaults].to.revision";

				DomainObject.MULTI_VALUE_LIST.add(SELECT_SUB_FOLDER_NAME);
				DomainObject.MULTI_VALUE_LIST.add(SELECT_FOLDER_NAME);
				DomainObject.MULTI_VALUE_LIST.add(SELECT_FOLDER_REVISION);
				DomainObject.MULTI_VALUE_LIST.add(SELECT_SUB_FOLDER_REVISION);

				StringList busSelect  = new StringList(5);
				busSelect.addElement(DomainObject.SELECT_NAME);
				busSelect.addElement(SELECT_FOLDER_NAME);
				busSelect.addElement(SELECT_SUB_FOLDER_NAME);
				busSelect.addElement(SELECT_FOLDER_REVISION);
				busSelect.addElement(SELECT_SUB_FOLDER_REVISION);
				
				DomainObject object = DomainObject.newInstance(context, strObjectId);
				Map objectInfoMap = object.getInfo(context, busSelect);

				String parentName = (String)objectInfoMap.get(DomainObject.SELECT_NAME);
				StringList folderNameList = (StringList)objectInfoMap.get(SELECT_FOLDER_NAME);
				StringList folderRevisionList = (StringList)objectInfoMap.get(SELECT_FOLDER_REVISION);
				
				if(folderNameList == null || folderNameList.isEmpty()){
					folderNameList = (StringList)objectInfoMap.get(SELECT_SUB_FOLDER_NAME);
					folderRevisionList = (StringList)objectInfoMap.get(SELECT_SUB_FOLDER_REVISION);
				}
				
                // NX5 -  DPM S4, #6868
                // If I'm coming from a createQuick Toolbar action, the Folder name already exists as valid
                String nameChk = (String) requestMap.get("IgnoreNameChk");
                boolean bIgnoreNameChk = false;
                if (UIUtil.isNotNullAndNotEmpty(nameChk)) {
                    bIgnoreNameChk = "true".equalsIgnoreCase(nameChk);
                }
                if (!bIgnoreNameChk) {
				boolean isAllowToCreateFolder = true;
				if(folderNameList != null && folderRevisionList != null){
					for(int i=0;i<folderNameList.size();i++) {

						String folderName     = (String)folderNameList.get(i);
						String folderRevision = (String)folderRevisionList.get(i);

						if(strFolderName.equalsIgnoreCase(folderName) &&
								ProgramCentralUtil.isNotNullString(folderRevision)) {
							isAllowToCreateFolder = false;
							break;
						}
					}
				}
				
				if (isAllowToCreateFolder) {
				String sCommandStatement = " modify bus $1 name $2 revision $3";
				MqlUtil.mqlCommand(context, sCommandStatement,objectId, strFolderName,revision); 
			}else{
					String error = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL, 
							"emxProgramCentral.ProjectFolder.ErrorMessage", context.getSession().getLanguage());
					error = parentName +" "+error+" '"+strFolderName+"'.";
					throw new MatrixException(error);
				}
                }

			}else{
				revision=workspaceVault.getUniqueName(context);
				workspaceVault.setAttributeValue(context,DomainConstants.ATTRIBUTE_TITLE,strFolderName);
				workspaceVault.setName(context,revision);
			}

			String strDescription = (String) requestMap.get("Description");
			String parentObjectId = (String) requestMap.get("parentOID");
			if(isCloneOperation){
				emxTableRowId =  (String)requestMap.get("oldObjectId");
			}
			if(null == parentObjectId || (null!=emxTableRowId && !"null".equals(emxTableRowId)))
			{
				if(isCloneOperation){
					parentObjectId = sParsedParentObjId;
				}else{
					parentObjectId = (String) requestMap.get("objectId");
				}
			}
			project.setId(parentObjectId);

			/*
			//Get Parent Project Visibility
			String projectVisibilty = DomainObject.EMPTY_STRING;
			String projectId = DomainObject.EMPTY_STRING;
			if(project.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_MANAGEMENT)){
				projectVisibilty = (String)project.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);				
			}else if(project.isKindOf(context, ProgramCentralConstants.TYPE_WORKSPACE_VAULT)){
				projectId = getProjectIdFromFolder(context, project.getId(context));
				ProjectSpace projectSpace = new ProjectSpace(projectId);
				projectVisibilty = projectSpace.getInfo(context, ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_VISIBILITY);
			}
			if("Company".equals(projectVisibilty) && "Inherited".equals(accessType)){				
				String defaultOrg = PersonUtil.getDefaultOrganization(context, context.getUser());
				String defaultProj = PersonUtil.getDefaultProject(context, context.getUser());
				workspaceVault.setPrimaryOwnership(context, defaultProj, defaultOrg);				
			}
			*/

			//Create inherited ownership for Template folder
			String folderParentId = getProjectIdFromFolder(context, objectId);
			boolean isTemplate = false;
			if(ProgramCentralUtil.isNotNullString(folderParentId)){
				DomainObject projectTemplate = DomainObject.newInstance(context, folderParentId);
				isTemplate=projectTemplate.isKindOf(context, DomainObject.TYPE_PROJECT_TEMPLATE);
				if(isTemplate){
					 DomainAccess.createObjectOwnership(context, objectId, folderParentId, "");
				}
			}


			if(isClone!=null && !"".equals(isClone) && isClone.equalsIgnoreCase("true")){
				workspaceVaultSource.setId(emxTableRowId);
				workspaceVault.setContentRelationshipType(DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2);
				Map accessMap = new HashMap();
				accessMap = getFolderOwnershipAccessPermissions(context,emxTableRowId);
				
				workspaceVault.setUserPermissions(context, accessMap);   //TODO solved

				com.matrixone.apps.common.WorkspaceVault.cloneStructure(context,         // context
						workspaceVaultSource,            // source
						workspaceVault,           // target
						null,
						true);  
			}
/*  FZS - with new security model we don't need to traverse the folder structure setting access on each folder
			else{
				if (! parentObjectType.equals(DomainConstants.TYPE_WORKSPACE_VAULT) && ! parentObjectType.equals(DomainConstants.TYPE_CONTROLLED_FOLDER))
				{
					i18nNow i18nnow      = new i18nNow();
					HashMap accessMap    = new HashMap();
					String inheritedType = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Inherited", "en-us");
					String strNoneAccess = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Folder_Access.None", "en-us");
					if(inheritedType.equalsIgnoreCase(accessType) && !strNoneAccess.equalsIgnoreCase(security))
					{
						StringList busSels = new StringList();
						busSels.add(project.SELECT_NAME);
						MapList ml = project.getMembers(context,busSels,null,"","",true);
						if(ml != null){
							for(int i=0; i < ml.size(); i++){
								Map map = (Map)ml.get(i);
								String member = (String)map.get(project.SELECT_NAME);
								if(!member.equals(context.getUser())){
									accessMap.put(member, security);
								}
							}

							//set permissions on this new created WorkspaceVault object
							workspaceVault.setUserPermissions(context, accessMap);
						}

					}
					//PRG:RG6:R212:22-Jun-2011:110242V6R2012x/112931V6R2012x: connection logic is removed emxCrete.jsp will create the connection:hint foldercreatepreprocess.jsp  
				}

				//PRG:RG6:R212:22-Jun-2011:110242V6R2012x/112931V6R2012x: connection logic is removed emxCrete.jsp will create the connection:hint foldercreatepreprocess.jsp
			}
*/
			// add the originator to the access list
			
			if(!isTemplate)
			{	
				String loggedInUser = context.getUser();
				String defaultAccessGrantPermission = FrameworkProperties.getProperty(context, "emxComponents.WSO_Default_AccessGrant");
			
				Map accessMap = new HashMap();
				accessMap.put(loggedInUser, "Full");
			
				if("Specific".equals(accessType)
						&&projectOwner!=null
						&&!(loggedInUser.equals(projectOwner))
						&&"true".equalsIgnoreCase(defaultAccessGrantPermission)){
				accessMap.put(projectOwner, "Full");
				}
			
				/*if("Specific".equals(accessType)
						&&projectOwner!=null 
						&&!(loggedInUser.equals(projectOwner))
						&&"true".equalsIgnoreCase(defaultAccessGrantPermission)){
				accessMap.put(projectOwner, "Full");
				}*/

				workspaceVault.setUserPermissions(context, accessMap);
				// to grant the 'Default User Access' to all PRG roles
				ProjectRoleVaultAccess roleVaultAccess=ProjectRoleVaultAccess.getInstance(context, security);
				workspaceVault.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS, roleVaultAccess.toXML());
			}

			if (strDescription != null)
			{
				workspaceVault.setDescription(context, strDescription);
			}

		} catch (FrameworkException e) {
			e.printStackTrace();
			throw e;
		} catch (MatrixException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * Returns a Map<String,String> containing direct ownership for a user on specified folder object.
	 * @param context the eMatrix <code>Context</code> object
	 * @param String - folder object id.
	 * @return Map<Strin,Map> - Map of user (KEY_USER) and corresponding access (String).
	 * @throws MatrixException if operation fails
	 */
	private Map getFolderOwnershipAccessPermissions(Context context, String folderId) throws MatrixException{

		Map<String,String> userPermission = new HashMap<String,String>();
		Map<String,Map> userAccessInfo = PMCWorkspaceVault.getFolderOwnershipAccessInfo(context, folderId);
		Iterator<String> itr = userAccessInfo.keySet().iterator();
		while(itr.hasNext()){
			String userName = itr.next();
			Map map = userAccessInfo.get(userName);
			String isDirectOwner = (String)map.get(PMCWorkspaceVault.KEY_IS_DIRECT_OWNERSHIP);

			if("true".equalsIgnoreCase(isDirectOwner)){
				String access = (String)map.get(DomainAccess.KEY_ACCESS_GRANTED);
				userPermission.put(userName, access);
			}
		}
		return userPermission;
	}

	/**
	 * This method gets the Name of the Project Template not value returned
	 * should be: true/false
	 *
	 * PMCProjectTemplateCreateForm
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @return the user access permissions for this project.
	 * @throws FrameworkException
	 *             if operation fails.
	 * @since PMC V6R2008-1
	 */

	public String getName(Context context, String[] args)
	throws Exception
	{

		WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
		String strOuput =DomainConstants.EMPTY_STRING;
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String emxTableRowId = (String)requestMap.get("emxTableRowId");
			//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			String isClone=(String)requestMap.get("IsClone");
			//End:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			//Added:PRG:RG6:R212:Folder delete issue 
			boolean isCloneOperation = "true".equalsIgnoreCase(isClone);
			if(ProgramCentralUtil.isNotNullString(emxTableRowId))
			{
				if(isCloneOperation)
				{
					String sActualObjectIdToClone =  (String)requestMap.get("oldObjectId");  //PRG:RG:R212:as table row id value is changed in pre process jsp for clonning operation (and original id is kept in param oldObjectId)
					if(ProgramCentralUtil.isNotNullString(sActualObjectIdToClone))
					{
						emxTableRowId = sActualObjectIdToClone;
					}
					else
					{
						throw new IllegalArgumentException("object id modified for clone (oldObjectId) is null");
					}
				}
				else
				{
					StringList slObjectIdList =FrameworkUtil.splitString(emxTableRowId,"|");
					emxTableRowId=(String)slObjectIdList.get(1);
				}
			}
			//Added:PRG:RG6:R212:End

			//Added:12-Feb-09:NZF:R207:PRG:Bug:367017
			//String strMQL = "print type \"" + DomainConstants.TYPE_CONTROLLED_FOLDER +"\" select derivative dump |";
			String strMQL = "print type $1 select $2 dump $3";
			String strResult = MqlUtil.mqlCommand(context, strMQL,DomainConstants.TYPE_CONTROLLED_FOLDER,"derivative","|");
			StringList slControlledFolderTypeHierarchy = FrameworkUtil.split(strResult, "|");

			// Dont forget to add Controlled Folder type itself into this listing
			slControlledFolderTypeHierarchy.add(DomainConstants.TYPE_CONTROLLED_FOLDER);
			//
			String SELECT_ATTRIBUTE_FOLDER_TITLE = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Title") + "]";
			StringList strList = new StringList();
			strList.add(DomainConstants.SELECT_TYPE);
			strList.add(SELECT_ATTRIBUTE_FOLDER_TITLE);
			strList.add(DomainConstants.SELECT_NAME);

			String strTypeOfObject = DomainConstants.EMPTY_STRING;
			String strTitle = DomainConstants.EMPTY_STRING;
			String strName= DomainConstants.EMPTY_STRING;

			if(emxTableRowId!=null){
				workspaceVault.setId(emxTableRowId);
				Map mpObjectInfo = workspaceVault.getInfo(context,strList);

				strTypeOfObject = (String)mpObjectInfo.get(DomainConstants.SELECT_TYPE);
				strTitle = (String)mpObjectInfo.get(SELECT_ATTRIBUTE_FOLDER_TITLE);
				strName = (String )mpObjectInfo.get(DomainConstants.SELECT_NAME);
			}
			//End:R207:PRG:Bug:367017
			StringBuffer output = new StringBuffer();
			String sLanguage=context.getSession().getLanguage();
			String cloneName="";
			//Modified:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			/*
			 * The condition below is modified so that name is shown only in case of cloning.
			 */
			if ((emxTableRowId != null && ! "".equals(emxTableRowId) && ! "false".equals(emxTableRowId)) && (null != isClone && "true".equalsIgnoreCase(isClone)))
			{
				// Clone Name:
				//workspaceVault.setId(emxTableRowId);
				cloneName = strName;//workspaceVault.getName(context);
				//Added:23-Jun-09:NR2:R208:PRG:Bug:376474
				if(cloneName.contains("&"))
					cloneName =  cloneName.replaceAll("&","&amp;");
				//End:R208:PRG:Bug:376474
				//Added:12-Feb-09:NZF:R207:PRG:Bug:367017
				if(slControlledFolderTypeHierarchy.contains(strTypeOfObject)){
					cloneName = " " + strTitle;
				}else{
					cloneName = " " + cloneName;
				}
				//End:R207:PRG:Bug:367017
				i18nNow i18nnow = new i18nNow();
				String strCloneMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.CloningOf", sLanguage);
				strCloneMsg=strCloneMsg+cloneName;
				output.append("<input type=\"text\" name=\"Name\" value=\""+strCloneMsg+"\"/><script language=\"JavaScript\">assignValidateMethod(\"Name\", \"validateBadCharsNlength\");</script>");
			}
			else
			{
				output.append("<input type=\"text\" name=\"Name\" value=\"\"/><script language=\"JavaScript\">assignValidateMethod(\"Name\", \"validateBadCharsNlength\");</script>");
			}
			strOuput =output.toString();
		}catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			return strOuput ;
		}
	}

	/**
	 * This method gets the Name of the Project Template not value returned
	 * should be: true/false
	 *
	 * PMCProjectTemplateCreateForm
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @return the user access permissions for this project.
	 * @throws FrameworkException
	 *             if operation fails.
	 * @since PMC V6R2008-1
	 */

	public String getPolicy(Context context, String[] args)
	throws Exception
	{
		com.matrixone.apps.common.WorkspaceVault workspaceVault = (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
		return DomainConstants.POLICY_WORKSPACE_VAULT;

	}

	/**
	 * This method gets the Field for type of the Project Template not value
	 * returned should be: true/false
	 *
	 * PMCProjectTemplateCreateForm
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @return the user access permissions for this project.
	 * @throws FrameworkException
	 *             if operation fails.
	 * @since PMC R207
	 */

	public String getType(Context context, String[] args)
	throws Exception
	{
		String strOuput="";
		try{

			//
			// Get the selected type on the folder create form
			// The form selection for type can come in various forms so it needs to be handled correctly.
			// Ex. type=_selectedType:TestChildVault,type_ProjectVault,type_TectChildVault OR
			//     type=type_ProjectVault OR
			//     type=Workspace Vault
			//
			Map programMap = (Map)JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String strObjectId = (String)requestMap.get("objectId");
			//Added:12-Feb-09:NZF:R207:PRG:Bug:367017
			String strIsClone = (String)requestMap.get("IsClone");
			//End:R207:PRG:Bug:367017

			DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);

			String strParentType = dmoObject.getInfo(context,DomainConstants.SELECT_TYPE);

			//Added:1-Apr-09:nzf:R207:PRG:Bug:361320

			//edited by r96 29-July-13: Project Space, Project Template and Project Concept types are added in hierarchy list along with their subtypes
			StringList slProjectSpaceTypeHierarchy = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_SPACE);
			slProjectSpaceTypeHierarchy.addAll(ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_TEMPLATE));
			slProjectSpaceTypeHierarchy.addAll(ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_CONCEPT));
			//edited by r96 -end
			
			//End:R207:PRG:Bug:361320

			String strSelectedType = (String)requestMap.get("type");
			if (strSelectedType != null) {
				if (strSelectedType.indexOf(":") != -1) {
					// type=_selectedType:TestChildVault,type_ProjectVault,type_TectChildVault
					StringList slSplitType = FrameworkUtil.split(strSelectedType, ":");
					if (slSplitType.size() > 1) {
						strSelectedType = (String)slSplitType.get(1);
						slSplitType = FrameworkUtil.split(strSelectedType, ",");
						if (slSplitType.size() > 0) {
							strSelectedType = (String)slSplitType.get(0);
						}
						else {
							strSelectedType = null;
						}
					}
					else {
						strSelectedType = null;
					}
				}
				else {
					// If it is just command separated value then take the first value
					StringList slSplitType = FrameworkUtil.split(strSelectedType, ",");
					if (slSplitType.size() > 0) {
						strSelectedType = (String)slSplitType.get(0);
					}
					else {
						strSelectedType = null;
					}
				}
			}
			if (strSelectedType == null) {
				strSelectedType = DomainConstants.TYPE_WORKSPACE_VAULT; // Default!
			}
			else {
				// If it is symbolic name then convert it to real name
				if (strSelectedType.startsWith("type_")) {
					strSelectedType = PropertyUtil.getSchemaProperty(context, strSelectedType);
				}
			}

			String strType = "";
			i18nNow i18nnow = new i18nNow();
			String sLanguage=context.getSession().getLanguage();

			StringBuffer output = new StringBuffer();
			strType = i18nnow.getAdminI18NString("Type",strSelectedType,sLanguage);

			String emxTableRowId = (String)requestMap.get("emxTableRowId");


			if(slProjectSpaceTypeHierarchy.contains(strParentType) && !("True".equalsIgnoreCase(strIsClone))){
				output.append("<input type='hidden' name='TypeActual' value=\""+strSelectedType+"\"/>");
				output.append("<input type='text' readonly='true' name='TypeActualDisplay' value=\""+strType+"\"/>");
				output.append("<input type='button' name='btnTypeActual' value='...' onclick=\"javascript:showChooser('emxTypeChooser.jsp?SelectType=single&amp;ReloadOpener=true&amp;SelectAbstractTypes=false&amp;InclusionList=type_ProjectVault&amp;fieldNameActual=TypeActual&amp;fieldNameDisplay=TypeActualDisplay&amp;fieldNameOID=TypeActualOID&amp;suiteKey=ProgramCentral','500','500')\"/>");
			}else if(DomainConstants.TYPE_WORKSPACE_VAULT.equals(strParentType)){
				output.append("<input type='hidden' name='TypeActual' value=\""+strSelectedType+"\"/>");
				output.append("<label>"+strType+"</label>");
			}else if(DomainConstants.TYPE_CONTROLLED_FOLDER.equals(strParentType)){
				//Modified:12-Feb-09:NZF:R207:PRG:Bug:367017
				if("True".equalsIgnoreCase(strIsClone)){
					strType = i18nnow.getAdminI18NString("Type",strParentType,sLanguage);
					output.append("<input type=\"hidden\" name=\"TypeActual\" value=\""+strParentType+"\"/>");
					output.append("<label>"+strType+"</label>");
				}else{
					output.append("<input type='hidden' name='TypeActual' value=\""+strSelectedType+"\"/>");
					output.append("<label>"+strType+"</label>");
				}
				//End:R207:PRG:Bug:367017
			}else{
				output.append("<input type='hidden' name='TypeActual' value=\""+strSelectedType+"\"/>");
				output.append("<label>"+strType+"</label>");
			}


			strOuput = output.toString();

		}catch(Exception ex)
		{
			throw ex;
		}
		return strOuput ;

	}

	/**
	 * This method gets the Description of the Project Template not value
	 * returned should be: true/false
	 *
	 * PMCProjectTemplateCreateForm
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @return the user access permissions for this project.
	 * @throws FrameworkException
	 *             if operation fails.
	 * @since PMC V6R2008-1
	 */

	public String getDescription(Context context, String[] args)
	throws Exception
	{
		String strOuput="";
		try{
			StringBuffer output = new StringBuffer();


			WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String emxTableRowId = (String)requestMap.get("emxTableRowId");
			//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			String isClone=(String)requestMap.get("IsClone");
			//End:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			//Added:PRG:RG6:R212:Start
			boolean isCloneOperation = "true".equalsIgnoreCase(isClone);
			if(ProgramCentralUtil.isNotNullString(emxTableRowId))
			{
				if(isCloneOperation)
				{
					String sActualObjectIdToClone =  (String)requestMap.get("oldObjectId");  //PRG:RG:R212:as table row id value is changed in pre process jsp for clonning operation (and original id is kept in param oldObjectId)
					if(ProgramCentralUtil.isNotNullString(sActualObjectIdToClone))
					{
						emxTableRowId = sActualObjectIdToClone;
					}
					else
					{
						throw new IllegalArgumentException("object id modified for clone (oldObjectId) is null");
					}
				}
				else
				{
					StringList slObjectIdList =FrameworkUtil.splitString(emxTableRowId,"|");
					emxTableRowId=(String)slObjectIdList.get(1);
				}
			}
			//Added:PRG:RG6:R212:End
			String cloneDescription="";
			//Modified:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			/*
			 * The condition in if(isClone) is modified so that name is shown only
			 * in case of cloning.
			 */
			//if ((emxTableRowId != null && ! "".equals(emxTableRowId) && ! "false".equals(emxTableRowId))&& (null != isClone && "true".equalsIgnoreCase(isClone)))
			if ((ProgramCentralUtil.isNotNullString(emxTableRowId) && ! "false".equals(emxTableRowId)) && (null != isClone && "true".equalsIgnoreCase(isClone)))
			{
				workspaceVault.setId(emxTableRowId);
				cloneDescription = workspaceVault.getDescription(context);
				//Added for special character.
				//Modified:12-Feb-09:NZF:R207:PRG:Bug:367017
				//output.append("<input type=\"text\" name=\"Description\" value='"+cloneDescription.trim()+"'/>");
				//Added for special character.
				output.append("<textarea name=\"Description\" rows=\"4\" cols=\"36\">"+XSSUtil.encodeForXML(context, cloneDescription.trim())+"</textarea>");//IR-177624V6R2013x
				//End:R207:PRG:Bug:367017
			}

			else
			{
				output.append("<textarea name=\"Description\" rows=\"4\" cols=\"36\"></textarea>");
			}

			strOuput =output.toString();

		}catch(Exception ex)
		{
			throw ex;
		}
		finally{
			return strOuput ;
		}

	}
	/**
	 * Gets the Access Type Ranges.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return Object - boolean true if the operation is successful
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getAccessTypeRanges(Context context, String[] args)
	throws Exception
	{

		String language       = context.getSession().getLanguage();
		com.matrixone.apps.common.WorkspaceVault workspaceVault =
			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
					DomainConstants.TYPE_WORKSPACE_VAULT);

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String emxTableRowId = (String)requestMap.get("emxTableRowId");
		StringList slObjectIdList = null;

		if(null != emxTableRowId && !"".equals(emxTableRowId))
		{
			slObjectIdList =FrameworkUtil.splitString(emxTableRowId,"|");
			emxTableRowId=(String)slObjectIdList.get(1);
		}

		String strObjectId = (String) requestMap.get("objectId");
		String sAttrProjectVisibility = i18nNow.getDefaultAttributeValueI18NString(DomainConstants.ATTRIBUTE_ACCESS_TYPE,language);
		if (emxTableRowId != null && ! "".equals(emxTableRowId) && ! "false".equals(emxTableRowId))
		{
			com.matrixone.apps.common.WorkspaceVault workspaceVaultSource =
				(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
						DomainConstants.TYPE_WORKSPACE_VAULT);

			workspaceVaultSource.setId(emxTableRowId);
			sAttrProjectVisibility=workspaceVaultSource.getInfo(context, WorkspaceVault.SELECT_ACCESS_TYPE);
		}


		StringBuffer output=new StringBuffer();

		workspaceVault.setId(strObjectId);
		StringList conceptVisibilityList = new StringList();
		MapList typeMapList = mxType.getAttributes(context,DomainConstants.TYPE_WORKSPACE_VAULT);
		MapList addMapList = new MapList();
		Iterator typeMapListItr = typeMapList.iterator();
		while(typeMapListItr.hasNext())
		{
			Map item = (Map) typeMapListItr.next();
			String attrName = (String) item.get("name");
			String attrType = (String) item.get("type");
			if(item.get(DomainConstants.SELECT_NAME).equals(DomainConstants.ATTRIBUTE_ACCESS_TYPE)) {
				Map conceptVisibilityMap = (Map) item;
				conceptVisibilityList = (StringList) conceptVisibilityMap.get("choices");
			}
		}

		StringItr tmpRangeItr = new StringItr(conceptVisibilityList);
		output.append("<select name ='AccessType'>");
		while (tmpRangeItr.next()) {
			String sSelect  = "";
			String sTempStr = tmpRangeItr.obj();
			String i18nAttrVal = i18nNow.getRangeI18NString(DomainConstants.ATTRIBUTE_ACCESS_TYPE, sTempStr, language);

			if(sAttrProjectVisibility!=null && sAttrProjectVisibility.equals(i18nAttrVal))
			{

				output.append("<option value=\""+sTempStr+"\" Selected=\"Selected\">"+i18nAttrVal+"</option>");
			}
			else   output.append("<option value=\""+sTempStr+"\">"+i18nAttrVal+"</option>");
		}
		output.append("</select>");

		return output.toString();

	}
	/**
	 * To update Default Access Type
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return void -
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public void updateDefaultAccessType(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");

		String newGlobalRead = (String)paramMap.get("New Value");

		DomainObject dom = DomainObject.newInstance(context, objectId);
		dom.open(context);
		dom.setAttributeValue(context,
				PropertyUtil.getSchemaProperty(context,"attribute_AccessType"),
				newGlobalRead);
		dom.close(context);
	}
	/**
	 * To show the Parent
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return String - Parent Name, Image
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public String getParent(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String emxTableRowId = (String)requestMap.get("emxTableRowId"); //Modified:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
		ProjectSpace project = (ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

		StringList slObjectIdList =FrameworkUtil.splitString(emxTableRowId,"|");
		//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
		String isClone=(String)requestMap.get("IsClone");
		String parentObjectId = (String) requestMap.get("parentOID");

		if(null ==isClone || !"true".equalsIgnoreCase(isClone)){ //Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			if(null == parentObjectId && null!=emxTableRowId &&!"".equals(emxTableRowId) )
			{
				parentObjectId =(String)slObjectIdList.get(1); //Modified:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
				//  parentObjectId =(String)slObjectIdList.get(1);
			}
			else
			{
				//parentObjectId = (String) requestMap.get("projectID");
				parentObjectId = (String) requestMap.get("objectId");
			}
		}
		else if("true".equalsIgnoreCase(isClone))
		{  
			if(null == parentObjectId && null!=emxTableRowId &&!"".equals(emxTableRowId) )
			{
				slObjectIdList=FrameworkUtil.splitString(emxTableRowId,"|");
				if(slObjectIdList.size()>2)
				{
					parentObjectId =(String)slObjectIdList.get(2);
				}
			}
		}
		//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
		StringBuffer output=new StringBuffer();
		if(null !=parentObjectId && !parentObjectId.equals("")){
			project.setId(parentObjectId);
			StringList objectSelect= new StringList(3);
			objectSelect.addElement(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
			objectSelect.addElement(CommonDocument.SELECT_TITLE);
			objectSelect.addElement(DomainConstants.SELECT_NAME);
			Map infoMap=project.getInfo(context, objectSelect);
			String strName = "";
			String strType =  DomainConstants.TYPE_WORKSPACE_VAULT; 
			if ("true".equalsIgnoreCase((String)infoMap.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER)))
			{
				//strName = project.getInfo(context,CommonDocument.SELECT_TITLE);
				strName = (String)infoMap.get(CommonDocument.SELECT_TITLE);
				strType =  DomainConstants.TYPE_CONTROLLED_FOLDER;
			}
			else
			{
				//strName = project.getInfo(context,DomainConstants.SELECT_NAME);
				strName = (String)infoMap.get(DomainConstants.SELECT_NAME);
			}
			String strSymbolicType = FrameworkUtil.getAliasForAdmin(context, "Type", strType, true);
			String strTypeIcon = EnoviaResourceBundle.getProperty(context, "emxFramework.smallIcon." + strSymbolicType);
			String strTempObjectName = strName.replaceAll("&", "&amp;"); 
			output.append("<img src='../common/images/"+strTypeIcon+"' border='0' alt='New Window' >"+strTempObjectName+"</img>");
		}
		return output.toString();
	}
	/**
	 * To check Access to show Parent
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return boolean - boolean true if the operation is successful
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	public boolean showParent(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String emxTableRowId = (String)programMap.get("emxTableRowId");
		String parentObjectId = (String)programMap.get("parentOID");
		//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
		String isClone=(String)programMap.get("IsClone");
		/*
		 * Below if is added so that 'parent' field should get hidden while cloning.
		 */
		if(!"true".equalsIgnoreCase(isClone)){  //Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
			if(null == parentObjectId && null!=emxTableRowId &&!"".equals(emxTableRowId) )
			{
				StringList slObjectIdList=FrameworkUtil.splitString(emxTableRowId,"|");
				//parentObjectId =(String)slObjectIdList.get(2);
				parentObjectId =(String)slObjectIdList.get(1);
			}
			else
			{
				parentObjectId = (String) programMap.get("objectId");
			}
		}
		else if("true".equalsIgnoreCase(isClone))
		{  
			if(null == parentObjectId && null!=emxTableRowId &&!"".equals(emxTableRowId) )
			{
				StringList slObjectIdList=FrameworkUtil.splitString(emxTableRowId,"|");
				if(slObjectIdList.size()>2)
				{
					parentObjectId =(String)slObjectIdList.get(2);
				}
			}
		}
		//Added:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
		if(parentObjectId==null)
			return false;
		//End:4-June-2010:rg6:R210 PRG:IR-053998V6R2011x
		com.matrixone.apps.program.ProjectSpace project =
			(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

		project.setId(parentObjectId);
		String parentObjectType = project.getInfo(context, DomainConstants.SELECT_TYPE);
		if (!project.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT))
		{
			return false;
		}
		else return true;

	}
	/**
	 * To Perform Post Process Actions After Edit
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds a HashMap containing the following entries: paramMap - a
	 *            HashMap containing the following keys, "objectId".
	 * @return none
	 * @throws Exception
	 *             if operation fails
	 * @since PMC V6R2008-1
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void postProcessActionsAfterEdit(Context context, String[] args)
	throws Exception
	{
		try{
			com.matrixone.apps.common.WorkspaceVault workspaceVault = (com.matrixone.apps.common.WorkspaceVault) DomainObject
			.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
			String STR_DATA_VAULT = DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap) programMap.get("paramMap");
			// Created/Cloned objects Id

			String strOldDefaultAccess= (String)paramMap.get("DefaultAccessfieldValue");

			String objectId    = (String) paramMap.get("objectId");
			workspaceVault.setId(objectId);
			String STR_POLICY_TEMPLATE_WORKSPACE_VAULT = DomainConstants.POLICY_TEMPLATE_WORKSPACE_VAULT;
			StringList slVaultSelect = new StringList();
			slVaultSelect.add(DomainConstants.SELECT_GRANTEE);
			slVaultSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
			slVaultSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);
			slVaultSelect.add(DomainConstants.SELECT_GRANTEEACCESS);
			slVaultSelect.add(DomainConstants.SELECT_OWNER); //Added:PRG:RG6:R212:IR-113096V6R2012x:2-Jun-2011
			Map mapSelect = workspaceVault.getInfo(context, slVaultSelect);

			Map mapUserPermission= workspaceVault.getUserPermissions(context);

			String strDefaultAccess = (String) mapSelect.get(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
			String strProjectRoleAccess = (String) mapSelect.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);
			StringList slGrantee = (StringList) mapSelect.get(DomainConstants.SELECT_GRANTEE);
			String sOwner = (String) mapSelect.get(DomainConstants.SELECT_OWNER);
			Map mapAccess = new HashMap();
			String strGrantee = null;
			String strUserAccess = null;

			for(int k =0; k < slGrantee.size();k++)
			{
				strGrantee = (String)slGrantee.get(k);
				strUserAccess = (String) mapUserPermission.get(strGrantee);
				//Added:PRG:RG6:R212:IR-113096V6R2012x:Start-2-Jun-2011
				boolean isGranteeOwner = ProgramCentralUtil.isNotNullString(strGrantee) ?  strGrantee.equalsIgnoreCase(sOwner) : false;

				if(strUserAccess.equals(strOldDefaultAccess) && !isGranteeOwner)
				{
					mapAccess.put(strGrantee, strDefaultAccess);
				}
				else if(isGranteeOwner)
				{
					mapAccess.put(strGrantee, AccessUtil.ADD_REMOVE);
				}
				//Added:PRG:RG6:R212:IR-113096V6R2012x:End-2-Jun-2011
			}
			//Added:PRG:RG6:R212:IR-124786V6R2012x:12-Aug-2011:start
			if(null != mapAccess && !mapAccess.containsKey(sOwner))
			{
				mapAccess.put(sOwner, AccessUtil.ADD_REMOVE);
			}
			//Added:PRG:RG6:R212:IR-124786V6R2012x:12-Aug-2011:end

			workspaceVault.setUserPermissions(context, mapAccess);

			ProjectRoleVaultAccess roleVaultAccess = new ProjectRoleVaultAccess(context,strProjectRoleAccess);
			Map mapRoleAccess= roleVaultAccess.toMap();

			Iterator itr=mapRoleAccess.keySet().iterator();

			for(;itr.hasNext();)
			{
				String strProjectRole=(String)itr.next();
				String strRoleAccess= (String)mapRoleAccess.get(strProjectRole);

				if(strRoleAccess.equals(strOldDefaultAccess))
				{
					roleVaultAccess.setAccess(strProjectRole, strDefaultAccess);
				}
			}

			workspaceVault.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS, roleVaultAccess.toXML());

			StringList objectSelects= new StringList(1);
			objectSelects.add( DomainConstants.SELECT_ID );

			StringList relSelects= new StringList(1);
			relSelects.add( DomainConstants.SELECT_RELATIONSHIP_ID );

			MapList mapList = workspaceVault.getRelatedObjects(
					context,                   // context.
					STR_DATA_VAULT,   // rel filter.
					DomainConstants.QUERY_WILDCARD,            // type filter.
					objectSelects,             // business object selectables.
					relSelects,                      // relationship selectables.
					true,                     // expand to direction.
					false,                      // expand from direction.
					(short) 1,             // level
					null,                      // object where clause
					null);
			if(mapList!=null && mapList.size()>0)
			{
				Map projectMap = (Map)mapList.get(0);
				if(workspaceVault.getInfo(context, DomainConstants.SELECT_POLICY).equals(STR_POLICY_TEMPLATE_WORKSPACE_VAULT))
				{
					DomainRelationship.disconnect( context, (String)projectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID));
				}
			}
		}
		catch(Exception ex)
		{
		}
	}

	//Added:22-Dec-08:kyp:R207:PRG Controlled Folder
	/**
	 * Method shows higher revision Icon if a higher revision of the object exists
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * @return Vector - returns the program HTML output
	 * @throws Exception if the operation fails
	 * @since PRG R207
	 */
	public Vector getHigherRevisionIcon(Context context, String[] args) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);
		MapList mlObjectList = (MapList) programMap.get("objectList");

		// The List to be returned
		Vector vecColumnData= new Vector();

		//Reading the tooltip from property file.
		String strLanguage = context.getSession().getLanguage();
		String strTooltipHigherRevExists = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
				"emxProgramCentral.ToolTip.HigherRevExists", context.getSession().getLanguage()); 
		String strIcon = EnoviaResourceBundle.getProperty(context, "emxComponents.HigherRevisionImage");
		final String HTML_HIGHER_REVISION_EXISTS = "<img src=\"../common/images/" + strIcon + "\" border=\"0\"  align=\"middle\" title=\"" + strTooltipHigherRevExists + "\"/>";

		Map mapObjectInfo = null;
		String strObjectId = "";
		String strLatestRevisionObjectId = "";

		for (Iterator itrObjectList = mlObjectList.iterator(); itrObjectList.hasNext();) {
			mapObjectInfo = (Map) itrObjectList.next();

			strObjectId = (String)mapObjectInfo.get(DomainConstants.SELECT_ID);
			strLatestRevisionObjectId = (String)mapObjectInfo.get(DomainConstants.SELECT_LAST_ID);

			if (strLatestRevisionObjectId != null && !strLatestRevisionObjectId.equals(strObjectId)) {
				vecColumnData.add(HTML_HIGHER_REVISION_EXISTS);
			}
			else {
				vecColumnData.add("");
			}
		}

		return vecColumnData;
	}
	//End:R207:PRG Controlled Folder

	/** It returns the Full name of Owner
	 *
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */

	public Vector getColumnOwnerFullNameData(Context context,String[] args) throws Exception
	{
		HashMap programMap         = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");

		Map mapObjects = null;
		String strObjectId = null;
		DomainObject dmoObject = null;
		String  strOwnerName = null;
		Vector vcOwnerFullNameData = new Vector();

		for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();)
		{
			mapObjects = (Map) itrObjects.next();
			strObjectId = (String)mapObjects.get(DomainConstants.SELECT_ID);
			dmoObject = DomainObject.newInstance(context,strObjectId);
			strOwnerName = dmoObject.getInfo(context, DomainConstants.SELECT_OWNER);

			vcOwnerFullNameData.add(PersonUtil.getFullName(context, strOwnerName));
		}

		return vcOwnerFullNameData;
	}
	/**
	 * Gets Workspace Vault table data for Project
	 *
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableProjectVaultsData(Context context,String[] args)throws Exception
	{
		Map programMap = (Map)JPO.unpackArgs(args);
		Map RequestValuesMap = (Map)programMap.get("RequestValuesMap");

		String strObjectId = (String) programMap.get("objectId");
		DomainObject dmoProject = DomainObject.newInstance(context,strObjectId);

		boolean getFrom = true;
		boolean getTo = false;
		short recurseToLevel = 1;
		String strBusWhere =DomainObject.SELECT_CURRENT+"!~~"+DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED;

		String strRelWhere = null;
		String strRelationshipPattern= null;
		String strTypePattern = null;

		if(dmoProject.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT))
		{
			//strRelationshipPattern = DomainConstants.RELATIONSHIP_SUBVAULTS+","+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+","+DomainConstants.RELATIONSHIP_LINK_URL+","+DomainConstants.RELATIONSHIP_LINKED_FOLDERS;
			//strTypePattern = DomainConstants.QUERY_WILDCARD;
			//strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT+","+DomainConstants.TYPE_DOCUMENT+","+DomainConstants.TYPE_URL;
			//strRelationshipPattern=  DomainConstants.RELATIONSHIP_SUB_VAULTS+","+DomainConstants.RELATIONSHIP_LINKED_FOLDERS;
			return getTableExpandProjectVaultData(context, args);
		}
		else// if(dmoProject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE))
		{
			strRelationshipPattern =  DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
			strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;

			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			slBusSelect.add(DomainConstants.SELECT_NAME);
			slBusSelect.add(DomainConstants.SELECT_REVISION);
			slBusSelect.add(DomainConstants.SELECT_OWNER);
			slBusSelect.add(ProgramCentralConstants.SELECT_GRANTEE);
			slBusSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
			slBusSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);

			StringList slRelSelect = new StringList();
			slRelSelect.add(DomainRelationship.SELECT_ID);
			MapList mlProjectFolderList = dmoProject.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					recurseToLevel, //the number of levels to expand, 0 equals expand all.
					strBusWhere, //where clause to apply to objects, can be empty ""
					strRelWhere,
					0); //where clause to apply to relationship, can be empty ""

			return mlProjectFolderList;
		}


	}
	/**
	 * Gets Workspace Vault table expanded data
	 *
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableExpandProjectVaultData(Context context,String[]args) throws Exception
	{
		MapList mlProjectVaultContentList = null;

		Map programMap = (Map)JPO.unpackArgs(args);
		String strObjectId =(String) programMap.get("objectId");
		String strSelectedTable =(String) programMap.get("selectedTable");
		String strExpandLevel = (String) programMap.get("expandLevel");

		String strTypePattern = DomainConstants.EMPTY_STRING;
		String strRelationshipPattern = DomainConstants.EMPTY_STRING;
		short recurseToLevel = ProgramCentralUtil.getExpandLevel(strExpandLevel);
		boolean getFrom = true;
		boolean getTo = false;
		String strRelWhere = null;
		String strBusWhere = DomainObject.SELECT_CURRENT+"!~~"+DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED;

		StringList slRelSelect = new StringList(3);
		slRelSelect.add(DomainRelationship.SELECT_ID);
		slRelSelect.add(DomainRelationship.SELECT_FROM_ID);
		slRelSelect.add(DomainRelationship.SELECT_FROM_TYPE);
		slRelSelect.add(DomainRelationship.SELECT_FROM_REVISION);
		slRelSelect.add("from.current");

		StringList slBusSelect = new StringList(10);
		//slBusSelect.add(DomainConstants.SELECT_NAME); di7
		slBusSelect.add(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_TYPE);
		slBusSelect.add(DomainConstants.SELECT_REVISION);
		slBusSelect.add(DomainConstants.SELECT_OWNER);
		slBusSelect.add(DomainConstants.SELECT_OWNER+".isaperson");
		slBusSelect.add(DomainConstants.SELECT_OWNER+".isagroup");
		slBusSelect.add(DomainConstants.SELECT_OWNER+".isarole");
		slBusSelect.add(ProgramCentralConstants.SELECT_GRANTEE);
		slBusSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
		slBusSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);

		StringList slObjTypeSelectables = new StringList(2);
		slObjTypeSelectables.add(ProgramCentralConstants.SELECT_KINDOF_PROJECT_MANAGEMENT);
		slObjTypeSelectables.add(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

		DomainObject domObject = DomainObject.newInstance(context,strObjectId);
		Map objInfoMap = domObject.getInfo(context, slObjTypeSelectables);

		String isKindOfProjectMgmt = (String) objInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_MANAGEMENT);
		String isKindOfTaskMgmt = (String) objInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_TASKMANAGEMENT);

		//For initial loading of folder page.
		if("true".equalsIgnoreCase(isKindOfProjectMgmt)&& "false".equalsIgnoreCase(isKindOfTaskMgmt)
																		&& "1".equals(strExpandLevel)) {
			strRelationshipPattern =  DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
			strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;

		} else { //For subsequent loading of folder page.

			/*slBusSelect.add(CommonDocument.SELECT_HAS_LOCK_ACCESS);
			slBusSelect.add(CommonDocument.SELECT_LOCKER);
			slBusSelect.add(CommonDocument.SELECT_LOCKED);
			slBusSelect.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
			slBusSelect.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
			slBusSelect.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
			slBusSelect.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);

			slBusSelect.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			slBusSelect.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			slBusSelect.add(CommonDocument.SELECT_FILE_NAME);
			slBusSelect.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
			slBusSelect.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
			slBusSelect.add(CommonDocument.SELECT_FILE_FORMAT);
			slBusSelect.add(CommonDocument.SELECT_FILE_SIZE);
			slBusSelect.add(CommonDocument.SELECT_TITLE );
			slBusSelect.add(CommonDocument.SELECT_IS_VERSION_OBJECT);*/

			slBusSelect.add("revisions");
			slBusSelect.add("revisions.id");
			slBusSelect.add("last.revision");            
			//slBusSelect.add("revision");

			strRelationshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "," + 
									 DomainConstants.RELATIONSHIP_SUB_VAULTS + "," +
									 DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2 + "," +
									 DomainConstants.RELATIONSHIP_LINK_URL + "," +
									 CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "," +
									 DomainConstants.RELATIONSHIP_LINKED_FOLDERS;

			strTypePattern = DomainConstants.QUERY_WILDCARD;
		}

		mlProjectVaultContentList = domObject.getRelatedObjects(context,
																strRelationshipPattern, 
																strTypePattern, 
																slBusSelect, 
																slRelSelect, 
																getTo, 
																getFrom, 
																recurseToLevel, 
																strBusWhere, 
																strRelWhere,
																0); 

		int size = mlProjectVaultContentList.size();
		//Removing Project BookMarks from the Folder Summary page, only Folder BookMarks will be shown.		
		if("true".equalsIgnoreCase(isKindOfProjectMgmt) && "false".equalsIgnoreCase(isKindOfTaskMgmt)) {
			
			for (int i = 0; i < size; i++) {
				
				Map mapProjectVaultContent = (Map) mlProjectVaultContentList.get(i);
				String level = (String) mapProjectVaultContent.get(DomainConstants.SELECT_LEVEL);
				String type = (String) mapProjectVaultContent.get(DomainConstants.SELECT_TYPE);
				
				if ("1".equals(level) && DomainConstants.TYPE_URL.equalsIgnoreCase(type)) {
					mlProjectVaultContentList.remove(mapProjectVaultContent);
					size = size-1;
					i--;
				}
			}
		}
		
		
		for (int i = 0; i < size; i++) {
			
			Map mapProjectVaultContent = (Map) mlProjectVaultContentList.get(i);
			mapProjectVaultContent.put("masterId", (String)mapProjectVaultContent.get(DomainRelationship.SELECT_FROM_ID));
		}

		return mlProjectVaultContentList;
	}

	/**
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void updateProjectFolderContentName(Context context,String[] args) throws Exception
	{
		Map mapProgrammap = (Map) JPO.unpackArgs(args);

		Map mpParamMap                      = (Map)mapProgrammap.get("paramMap");
		String strNewNameValue = (String)mpParamMap.get("New Value");
		String strObjectId  = (String)mpParamMap.get("objectId");
		DomainObject domainObject = DomainObject.newInstance(context,strObjectId);
		if(!DomainConstants.EMPTY_STRING.equalsIgnoreCase(strNewNameValue)){
			if (domainObject.isKindOf(context, DomainConstants.TYPE_CONTROLLED_FOLDER))
			{
				domainObject.setAttributeValue(context, DomainConstants.ATTRIBUTE_TITLE, strNewNameValue);
			}
			else
			{
				domainObject.setName(context, strNewNameValue);
			}
		}
	}
	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Vector getColumnFolderDocumentActionData(Context context,String[]args) throws Exception
	{
		Vector vecDocumentFileActions = new Vector();

		Map mapProgramMap = (Map)JPO.unpackArgs(args);

		MapList objectList = (MapList)mapProgramMap.get("objectList");
		Map paramList      = (Map)mapProgramMap.get("paramList");
		String strLanguage = context.getSession().getLanguage();
		String strDocumentPartRel = (String)paramList.get("relId");
		String strPartId = (String)paramList.get("trackUsagePartId");
		DomainObject domainObject = null;
		String strObjectId = DomainConstants.EMPTY_STRING;
		Map mapObject = null;
		String strIsversionObject = DomainConstants.EMPTY_STRING;
		String strObjectType = DomainConstants.EMPTY_STRING;
		boolean isprinterFriendly = false;
		//String STR_OBJECT_ROUTE = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;

		String sTipDownload = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.DocumentSummary.ToolTipDownload", strLanguage);
		String sTipCheckout = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.DocumentSummary.ToolTipCheckout", strLanguage);
		String sTipCheckin  = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.DocumentSummary.ToolTipCheckin", strLanguage);
		String sTipUnlock  = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.DocumentSummary.ToolTipUnlock", strLanguage);
		String sTipLock  = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.DocumentSummary.ToolTipLock", strLanguage);
		String sTipSubscriptions = EnoviaResourceBundle.getProperty(context, "Components", "emxComponents.Command.Subscriptions", strLanguage);
		String sTipAddNewFile = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxComponents.DocumentSummary.ToolTipAddNewFile", strLanguage);
		String sTipBookMark = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.BookmarkSummary.ToolTipBookMark", strLanguage);
		String strEditableMode = (String)paramList.get("editTableMode"); 

		String contextUser = context.getUser();

		if(paramList.get("reportFormat") != null)
		{
			isprinterFriendly = true;
		}
		boolean isVersionObject =false;

		String[] objectIdArr = new String[objectList.size()];
		for(int i=0;i<objectList.size();i++){
			Map objectMap = (Map)objectList.get(i);
			objectIdArr[i] = (String)objectMap.get("id");
		}
		String linkAttrName = PropertyUtil.getSchemaProperty(context,"attribute_MxCCIsObjectLinked");
		String SELECT_IS_WORKSPACE_VAULT  =  "type.kindof["+DomainConstants.TYPE_WORKSPACE_VAULT+"]";
		String SELECT_IS_DOCUMENTS  =  "type.kindof["+CommonDocument.TYPE_DOCUMENTS+"]";
		String SELECT_IS_URL  =  "type.kindof["+CommonDocument.TYPE_URL+"]";
		String SELECT_PARENT_CHECKIN_ACCESS = "to[" + CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "].from.current.access[checkin]";

		StringList slSelectables = new StringList(1);
		slSelectables.addElement(SELECT_IS_WORKSPACE_VAULT);
		slSelectables.addElement(SELECT_IS_DOCUMENTS);
		slSelectables.addElement(SELECT_IS_URL);
		slSelectables.add(CommonDocument.SELECT_ID);
		slSelectables.add(CommonDocument.SELECT_TYPE);
		slSelectables.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
		slSelectables.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
		slSelectables.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
		slSelectables.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
		slSelectables.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
		slSelectables.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
		slSelectables.add(CommonDocument.SELECT_FILE_NAME);
		slSelectables.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
		slSelectables.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
		slSelectables.add("attribute["+DomainConstants.ATTRIBUTE_LINK_URL+"]");
		slSelectables.add("vcfile");
		slSelectables.add(SELECT_PARENT_CHECKIN_ACCESS);
		slSelectables.add(CommonDocument.SELECT_IS_VERSION_OBJECT);

		if(ProgramCentralUtil.isNotNullString(linkAttrName))
		{
			slSelectables.add("attribute[" +linkAttrName +"]");
		}

		MapList folderObjectInfoList = DomainObject.getInfo(context, objectIdArr, slSelectables);

		int listSize = objectList.size();
		for(int i=0;i<listSize;i++)
		{
			mapObject = (Map) objectList.get(i);
			strObjectId = (String)mapObject.get(CommonDocument.SELECT_ID);
			Map folderMap = (Map)folderObjectInfoList.get(i);
			String isDoc = (String)folderMap.get(SELECT_IS_DOCUMENTS);
			String isWorkspaceVault = (String)folderMap.get(SELECT_IS_WORKSPACE_VAULT);
			String isURL = (String)folderMap.get(SELECT_IS_URL);
			String parentCheckinAccess = (String)folderMap.get(SELECT_PARENT_CHECKIN_ACCESS);

			strIsversionObject = (String)mapObject.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
			if(null == strIsversionObject)
			{
				strIsversionObject = (String)folderMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
			}
			isVersionObject=Boolean.parseBoolean(strIsversionObject);

			if("true".equalsIgnoreCase(isWorkspaceVault))
			{
				StringBuilder strProjectVaultActionBuffer = new StringBuilder();
				if(!isprinterFriendly)  //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::Start
				{
					strProjectVaultActionBuffer.append("<a href=\"javascript:showModalDialog('../common/emxForm.jsp?form=PMCWorkspaceVaultSubscriptionForm&amp;formHeader=emxProgramCentral.FolderSubscription.Header&amp;mode=edit&amp;postProcessJPO=emxProjectFolder:createSubscriptionProcess&amp;HelpMarker=emxhelpfoldersubscribe&amp;suiteKey=ProgramCentral&amp;StringResourceFileId=emxProgramCentralStringResource&amp;SuiteDirectory=programcentral&amp;submitAction=doNothing&amp;objectId="); //Modified PRG:RG6:R212:2 Jun 2011:
					strProjectVaultActionBuffer.append(strObjectId);
					strProjectVaultActionBuffer.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;");
				}
				else
				{
					strProjectVaultActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;"); //Modified PRG:RG6:R212:2 Jun 2011:
				} //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::End
				vecDocumentFileActions.add(strProjectVaultActionBuffer.toString());
			}
			else if("true".equalsIgnoreCase(isURL))
			{
				StringBuilder strBookmarkActionBuffer = new StringBuilder();
				String strBookmarkURL= (String)folderMap.get("attribute["+DomainConstants.ATTRIBUTE_LINK_URL+"]");
				//Added for special character.
				if(!strBookmarkURL.contains("http://"))
				{
					strBookmarkURL = "http://"+strBookmarkURL;
				}
				strBookmarkURL = XSSUtil.encodeForHTML(context, strBookmarkURL);
				if(ProgramCentralUtil.isNotNullString(strBookmarkURL) && strBookmarkURL.contains("href="))
				{
					try
					{
						strBookmarkURL = URL.parseBookMarkHref(strBookmarkURL);
					}catch(Exception e)
					{ } // rich text editor inputs while bookmark creation may affect the parsing so added try-catch for parsing url  
				}
				if(!isprinterFriendly) //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::Start
				{
					strBookmarkActionBuffer.append(URL.getBookmarkAnchor(strBookmarkURL));
					strBookmarkActionBuffer.append("<img border=\"0\" src=\"../common/images/iconSmallBookmark.gif\" alt=\"");
					strBookmarkActionBuffer.append(sTipBookMark);
					strBookmarkActionBuffer.append("\" title=\"");
					strBookmarkActionBuffer.append(sTipBookMark);
					strBookmarkActionBuffer.append("\"></img></a>&#160;");
				}
				else
				{
					strBookmarkActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconSmallBookmark.gif' alt=\""+strBookmarkURL+"\" title=\""+strBookmarkURL+"\"></img></a>&#160;");
				} //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::End

				vecDocumentFileActions.add(strBookmarkActionBuffer.toString());
			}
			else if("true".equalsIgnoreCase(isDoc) && !isVersionObject) 
			{
				StringBuilder strDocumentActionBuffer = new StringBuilder();
				String objectType = DomainConstants.EMPTY_STRING;
				String isVersionable = "true";
				StringList files = new StringList();
				String file = DomainConstants.EMPTY_STRING;
				int fileCount = 0;
				StringList locked = new StringList();
				String lock = DomainConstants.EMPTY_STRING;
				String objectId = DomainConstants.EMPTY_STRING;
				int lockCount = 0;
				boolean isLocker = true;
				if(folderMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED) != null && ((StringList)folderMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED)).contains("TRUE")&& folderMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER) != null && !((StringList)folderMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER)).contains(contextUser)){
					isLocker = false;
				}
				boolean hasCheckoutAccess = true;
				boolean hasCheckinAccess = true;
				boolean canCheckin = true;
				boolean moveFilesToVersion = false;
				/* As while using the cut paste when column methods are called
                                params are not available so need to fetch them from DB              */
				if("true".equalsIgnoreCase(strEditableMode)){
					mapObject.putAll(folderMap);
				}				
				objectId       = (String)mapObject.get(CommonDocument.SELECT_ID);
				objectType = (String) mapObject.get(CommonDocument.SELECT_TYPE);
				isVersionable = (String) mapObject.get(CommonDocument.SELECT_SUSPEND_VERSIONING);
				hasCheckoutAccess = (Boolean.valueOf((String) mapObject.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS))).booleanValue();
				hasCheckinAccess = (Boolean.valueOf((String) mapObject.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS))).booleanValue();
				canCheckin = hasCheckinAccess&&isLocker ;
				String vcInterface = (String)mapObject.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
				String vcfile = (String)mapObject.get("vcfile");
				boolean vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;

				boolean vcFileLock= false;
				String vcFileLocker= "";
				String parentType = "";
				boolean isVersionableType = false;
				if(null != objectType){
					parentType = CommonDocument.getParentType(context, objectType);
					isVersionableType = CommonDocument.checkVersionableType(context, objectType);
				}

				moveFilesToVersion = (Boolean.valueOf((String) mapObject.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
				if ( moveFilesToVersion )
				{
					try
					{
						files = (StringList)mapObject.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
					}
					catch(ClassCastException cex )
					{
						files.add((String)mapObject.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION));
					}
				}
				else
				{
					try
					{
						files = (StringList)mapObject.get(CommonDocument.SELECT_FILE_NAME);
					}
					catch(ClassCastException cex )
					{
						files.add((String)mapObject.get(CommonDocument.SELECT_FILE_NAME));
					}
				}
				if ( files != null )
				{
					fileCount = files.size();
					if ( fileCount == 1 )
					{
						file = (String)files.get(0);
						if ( file == null || "".equals(file) || "null".equals(file) )
						{
							fileCount = 0;
						}
					}
				}

				try
				{
					locked = (StringList)mapObject.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
				} catch(ClassCastException cex)
				{
					locked.add((String)mapObject.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED));
				}
				if ( locked != null )
				{
					Iterator itr = locked.iterator();
					while (itr.hasNext())
					{
						lock = (String)itr.next();
						if(lock.equalsIgnoreCase("true"))
						{
							lockCount ++;
						}
					}
				}

				if("true".equalsIgnoreCase(isDoc))
				{
					if(!isprinterFriendly)
					{
						strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../common/emxForm.jsp?form=PMCWorkspaceVaultDocumentSubscriptionForm&amp;suiteKey=ProgramCentral&amp;formHeader=emxProgramCentral.DocumentSubscription.Header&amp;mode=edit&amp;postProcessJPO=emxProjectFolder:createSubscriptionProcess&amp;HelpMarker=emxhelpdocumentsubscribe&amp;toolbar=null&amp;showClipboard=false&amp;objectCompare=false&amp;showPageURLIcon=false&amp;submitAction=doNothing&amp;objectId=");
						strDocumentActionBuffer.append(objectId);
						strDocumentActionBuffer.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;");
					}  else    {
						strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
					}

					// Changes for CLC start here
					//show Download icon for ClearCase Linked Objects
					String isObjLinked = DomainConstants.EMPTY_STRING;
					if (ProgramCentralUtil.isNotNullString(linkAttrName))
					{
						isObjLinked = (String)folderMap.get("attribute[" +linkAttrName +"]");
					}

					if(isObjLinked!=null && !isObjLinked.equals(""))
					{
						if(isObjLinked.equalsIgnoreCase("True"))
						{
							//show download icon for Linked Objects
							strDocumentActionBuffer.append("<a href='../servlet/MxCCCS/MxCCCommandsServlet.java?commandName=downloadallfiles&objectId=");
							strDocumentActionBuffer.append(objectId);
							strDocumentActionBuffer.append("'>");
							strDocumentActionBuffer.append("<img border='0' src='../common/images/iconActionDownload.gif' alt='");
							strDocumentActionBuffer.append(sTipDownload);
							strDocumentActionBuffer.append("' title='");
							strDocumentActionBuffer.append(sTipDownload);
							strDocumentActionBuffer.append("'></a>&nbsp;");
						}
					}
					// Changes for CLC end here..
					if ( (vcDocument || fileCount != 0) && hasCheckoutAccess )
					{
						// [MODIFIED::Jun 9, 2011:S4E:R212:IR-111268V6R2012x::Start]
						// Show download, checkout for all type of files.
						if(!isprinterFriendly)
						{
							strDocumentActionBuffer.append("<a href='javascript:callCheckout(\"");
							strDocumentActionBuffer.append(objectId);
							strDocumentActionBuffer.append("\",\"download\", \"\", \"\",\"");
							strDocumentActionBuffer.append("");
							strDocumentActionBuffer.append("\", \"");
							strDocumentActionBuffer.append("");
							strDocumentActionBuffer.append("\", \"");
							strDocumentActionBuffer.append("Table");
							strDocumentActionBuffer.append("\", \"");
							strDocumentActionBuffer.append("PMCFolderSummary");
							strDocumentActionBuffer.append("\"");
							strDocumentActionBuffer.append(")'>");
							strDocumentActionBuffer.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
							strDocumentActionBuffer.append(sTipDownload);
							strDocumentActionBuffer.append("\" title=\"");
							strDocumentActionBuffer.append(sTipDownload);
							strDocumentActionBuffer.append("\"></img></a>&#160;");
						}
						else
						{
							strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionDownload.gif' alt=\""); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::Start
							strDocumentActionBuffer.append(sTipDownload);
							strDocumentActionBuffer.append("\" title=\"");
							strDocumentActionBuffer.append(sTipDownload);
							strDocumentActionBuffer.append("\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::End
						}
					}
					//checkout w/lock
					if ( "false".equalsIgnoreCase(isVersionable) )
					{
						if ( (( vcDocument && !vcFileLock) || (!vcDocument && fileCount != 0 && lockCount != fileCount)) && (hasCheckoutAccess && hasCheckinAccess ))
						{
							//Modified:NZF:18-May-2011:IR-105183V6R2012x
							if(!isprinterFriendly)
							{
								//Below code is copy modified and pasted from ${CLASS:emxCommonDocumentUIBase}.java.getDocumentActions()
								strDocumentActionBuffer.append("<a href='javascript:callCheckout(\"");
								strDocumentActionBuffer.append(objectId);
								strDocumentActionBuffer.append("\",\"checkout\", \"\", \"\",\"");
								strDocumentActionBuffer.append("false");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("Table");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("programcentral");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");
								strDocumentActionBuffer.append("");
								strDocumentActionBuffer.append("\", \"");                    		
								strDocumentActionBuffer.append("emxProgramCentraFolderUtil.jsp\"");
								strDocumentActionBuffer.append(")'>");
								strDocumentActionBuffer.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
								strDocumentActionBuffer.append(sTipCheckout);
								strDocumentActionBuffer.append("\" title=\"");
								strDocumentActionBuffer.append(sTipCheckout);
								strDocumentActionBuffer.append("\"></img></a>&#160;");
							}
							else
							{
								strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionCheckOut.gif' alt=\""); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::Start
								strDocumentActionBuffer.append(sTipCheckout);
								strDocumentActionBuffer.append("\" title=\"");
								strDocumentActionBuffer.append(sTipCheckout);
								strDocumentActionBuffer.append("\"></img></a>&#160;");  //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::End
							}
						} //End:NZF:18-May-2011:IR-105183V6R2012x
						if (canCheckin)
						{
							//checkin
							if( !vcDocument )
							{
								if(!isprinterFriendly)
								{
									strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?objectId=");
									strDocumentActionBuffer.append(objectId);
									strDocumentActionBuffer.append("&amp;showComments=true&amp;showFormat=true&amp;objectAction=checkin&amp;refreshTableContent=true");
									strDocumentActionBuffer.append("&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp&amp;actionMode=addNewFile");
									strDocumentActionBuffer.append("', '780', '570')\"><img border='0' src='../common/images/iconActionAppend.gif' alt=\""+sTipAddNewFile+"\" title=\""+sTipAddNewFile+"\"></img></a>");
								}
								else
								{
									strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionAppend.gif' alt=\""+sTipAddNewFile+"\" title=\""+sTipAddNewFile+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
								}
							}
							//update
							if ( lockCount > 0 &&  !vcDocument)
							{
								if(!isprinterFriendly)
								{
									strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?objectId=");
									strDocumentActionBuffer.append(objectId);
									strDocumentActionBuffer.append("&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=update&amp;allowFileNameChange=true&amp;actionMode=checkin&amp;refreshTableContent=true");
									strDocumentActionBuffer.append("&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp");
									strDocumentActionBuffer.append("', '730', '450')\"><img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>");
								}
								else
								{
									strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
								}
							}
							else if (((vcDocument && !vcFileLock) || (vcDocument && vcFileLock && vcFileLocker.equals(contextUser))))
							{
								if(!isprinterFriendly)
								{
									if(vcfile!=null && vcfile.equalsIgnoreCase("true")){
										strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?objectId=");
										strDocumentActionBuffer.append(objectId);
										strDocumentActionBuffer.append("&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=false&noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp','730','450');\">");									 
									}
									else {
										strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=true&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp','730','450');\">");
									}
									strDocumentActionBuffer.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></a>");
								}
								else
								{
									strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
								}
							}
						}
					}
				}
				else if (!isVersionableType)
				{
					if(!isprinterFriendly)
					{
						strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonFS.jsp?functionality=DiscussionsSubscribe&amp;suiteKey=Components&amp;objectId=");
						strDocumentActionBuffer.append(objectId);
						strDocumentActionBuffer.append("', '730', '450')\"><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>&#160;");
					} else
					{
						strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconSmallSubscription.gif' alt=\""+sTipSubscriptions+"\" title=\""+sTipSubscriptions+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
					}
					if ( fileCount != 0 && hasCheckoutAccess)
					{
						// Show download, checkout for all type of files.
						if(!isprinterFriendly)
						{
							strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckout.jsp?override=false&amp;objectId=");
							strDocumentActionBuffer.append(objectId);
							strDocumentActionBuffer.append("&amp;action=download");
							strDocumentActionBuffer.append("', '730', '450')\"><img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>");
						}
						else
						{
							strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
						}
					}

					//checkout w/lock
					if ( "".equals(isVersionable) || "false".equalsIgnoreCase(isVersionable) )
					{
						if ( fileCount != 0 && hasCheckoutAccess && hasCheckinAccess && lockCount != fileCount)
						{
							if(!isprinterFriendly)
							{
								strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckout.jsp?override=false&amp;objectId=");
								strDocumentActionBuffer.append(objectId);
								strDocumentActionBuffer.append("&amp;action=checkout");
								strDocumentActionBuffer.append("', '730', '450')\"><img border='0' src='../common/images/iconActionCheckOut.gif' alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"></img></a>");
							}
							else
							{
								strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionCheckOut.gif' alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
							}
						}
						if (canCheckin)
						{
							//checkin
							if(!isprinterFriendly)
							{
								strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?override=false&amp;objectId=");
								strDocumentActionBuffer.append(objectId);
								strDocumentActionBuffer.append("&amp;showComments=true&amp;showFormat=true&amp;objectAction=checkin&amp;refreshTableContent=true");
								strDocumentActionBuffer.append("&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp");
								strDocumentActionBuffer.append("' ,'780','570')\"><img border='0' src='../common/images/iconActionAppend.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>");
							}
							else
							{
								strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionAppend.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
							}

							//update
							if ( lockCount > 0 )
							{
								if(!isprinterFriendly)
								{
									strDocumentActionBuffer.append("<a href=\"javascript:showModalDialog('../components/emxCommonDocumentPreCheckin.jsp?objectId="+objectId+"&amp;showFormat=readonly&amp;showComments=required&amp;objectAction=update&amp;allowFileNameChange=true&amp;refreshTableContent=true&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp','730','450');\">");
									strDocumentActionBuffer.append("<img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>");
								}
								else
								{
									strDocumentActionBuffer.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionCheckIn.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
								}
							}
						}
					}

				}
				vecDocumentFileActions.add(strDocumentActionBuffer.toString());
			}
			else if("true".equalsIgnoreCase(isDoc) && isVersionObject)
			{
				String masterId        = DomainConstants.EMPTY_STRING;
				String versionId       = DomainConstants.EMPTY_STRING;
				String fileActions     = DomainConstants.EMPTY_STRING;
				String fileName        = DomainConstants.EMPTY_STRING;
				String encodedFileName = DomainConstants.EMPTY_STRING;
				String encodedFormat   = DomainConstants.EMPTY_STRING;
				String fileFormat      = DomainConstants.EMPTY_STRING;
				boolean canCheckout    = false;
				boolean canCheckin     = false;
				boolean canViewAndDownload = false;
				boolean canUnlock = false;
				boolean canLock = true;
				long fileSize = 0;
				String strFileSize =DomainConstants.EMPTY_STRING;
				boolean largeFile = false;

				String strViewerURL = DomainConstants.EMPTY_STRING;
				String downloadURL  = DomainConstants.EMPTY_STRING;
				String checkoutURL  = DomainConstants.EMPTY_STRING;
				String checkinURL   = DomainConstants.EMPTY_STRING;
				String unlockURL = DomainConstants.EMPTY_STRING;

				String suspendVersioning     = "False";

				StringBuilder fileActionsStrBuff = new StringBuilder();

				String strType = (String) mapObject.get(DomainConstants.SELECT_TYPE);

				masterId = (String) mapObject.get("masterId");

				if(masterId == null || masterId.trim().matches("")) {
					masterId = (String) mapObject.get("id[parent]");
				}

				//DomainObject dObjectWithNewFileVersion = DomainObject.newInstance(context);
				String sActualFileName =  (String)mapObject.get("name");
				if(ProgramCentralUtil.isNotNullString(sActualFileName))
				{
					StringList slBusSelect = new StringList(2);
					slBusSelect.addElement(DomainConstants.SELECT_ID);
					slBusSelect.addElement(DomainConstants.SELECT_NAME);
					String sWhereClause = "name == \""+sActualFileName+"\"";
					DomainObject dMasterObj = DomainObject.newInstance(context, masterId);
					MapList mlSubFolderList = dMasterObj.getRelatedObjects(context,
							CommonDocument.RELATIONSHIP_ACTIVE_VERSION, //pattern to match relationships
							CommonDocument.TYPE_DOCUMENTS, //pattern to match types
							slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
							null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
							false, //get To relationships
							true, //get From relationships
							(short)1, //the number of levels to expand, 0 equals expand all.
							sWhereClause, //where clause to apply to objects, can be empty ""
							null,
							0);
					if(null != mlSubFolderList && mlSubFolderList.size() ==1)
					{
						Map mNewVersionObjMapAfterCheckIn = (Map)mlSubFolderList.get(0);
						versionId = (String)mNewVersionObjMapAfterCheckIn.get(DomainConstants.SELECT_ID);
						mapObject.put("id", versionId);
					}
					else
					{
						versionId = (String)mapObject.get(DomainConstants.SELECT_ID); 
					}
				}
				else
				{
					versionId = (String)mapObject.get(DomainConstants.SELECT_ID); 
				}


				if(ProgramCentralUtil.isNotNullString(versionId))
				{
					DomainObject dObjectWithNewFileVersion = DomainObject.newInstance(context, versionId);
					
					StringList selectableList = new StringList(11);
					selectableList.add(CommonDocument.SELECT_LOCKED);
					selectableList.add(CommonDocument.SELECT_LOCKER);
					selectableList.add(CommonDocument.SELECT_HAS_LOCK_ACCESS);
					selectableList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
					selectableList.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
					selectableList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
					selectableList.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
					selectableList.add(CommonDocument.SELECT_TITLE);
					selectableList.add(CommonDocument.SELECT_FILE_NAME);
					selectableList.add(CommonDocument.SELECT_FILE_FORMAT);
					selectableList.add(CommonDocument.SELECT_FILE_SIZE);


					Map mFileObjectInfo = dObjectWithNewFileVersion.getInfo(context,selectableList);
					mapObject.putAll(mFileObjectInfo);            
				}

				fileName = (String) mapObject.get(CommonDocument.SELECT_TITLE);
				encodedFileName = fileName;
				encodedFileName = FrameworkUtil.findAndReplace(fileName,"+","%252b");
				encodedFileName = FrameworkUtil.findAndReplace(encodedFileName, "&", "%26");
				
				int fileIndex	=	-1;
				Object fileNameObject	=	mapObject.get(CommonDocument.SELECT_FILE_NAME);

				if (fileNameObject != null && fileNameObject instanceof StringList) {
					fileIndex	=	((StringList)fileNameObject).indexOf(fileName);
				} 

				Object fileFormatObject	= mapObject.get(CommonDocument.SELECT_FILE_FORMAT);
				if(fileFormatObject != null && fileFormatObject instanceof StringList)
				{
					if(fileIndex > -1)
						fileFormat = (String)((StringList) mapObject.get(CommonDocument.SELECT_FILE_FORMAT)).get(fileIndex);
				}
				else
				{
					fileFormat = (String) mapObject.get(CommonDocument.SELECT_FILE_FORMAT);
				}

				if (fileFormat == null || "".equals(fileFormat))
				{
					fileFormat = CommonDocument.FORMAT_GENERIC;
				}

				Object fileSizeObject	= mapObject.get(CommonDocument.SELECT_FILE_FORMAT);
				if(fileSizeObject != null && fileSizeObject instanceof StringList)
				{
					if(fileIndex > -1)
						strFileSize = (String)((StringList)mapObject.get(CommonDocument.SELECT_FILE_SIZE)).get(fileIndex);
				}
				else
				{
					strFileSize = (String) mapObject.get(CommonDocument.SELECT_FILE_SIZE);
				}

				if (ProgramCentralUtil.isNotNullString(strFileSize))
				{
					fileSize = (new Long(strFileSize)).longValue();
				}
				if( fileSize > 2048000000 )
				{
					largeFile = true;
				}
				encodedFormat = fileFormat;

				String viewerTip = "Default";
				
				Map formatViewerMap = FormatUtil.getViewerCache(context);
				Map formatDetailsMap = (Map)formatViewerMap.get(fileFormat);
				java.util.Set set = formatDetailsMap.keySet();
				Iterator itr = set.iterator();
				String viewerURL = ProgramCentralConstants.EMPTY_STRING;
				while (itr.hasNext()){
					viewerURL = (String)itr.next();
					viewerTip = ((String)formatDetailsMap.get(viewerURL));
				}
				String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip, strLanguage);
				String servletPreFix = EnoviaResourceBundle.getProperty(context,"emxFramework.Viewer.ServletPreFix");

				suspendVersioning = (String) mapObject.get(CommonDocument.SELECT_SUSPEND_VERSIONING);

				canCheckout = "true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_HAS_LOCK_ACCESS)) &&
				"true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS)) &&
				"false".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_LOCKED));
				canLock = "true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_HAS_LOCK_ACCESS)) && "false".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_LOCKED));

				/*
				if(ProgramCentralUtil.isNotNullString(masterId))
				{
					//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
					String sCommandStatement = "print bus $1 select $2 dump $3";
					String sCanCheckoutOrLock =  MqlUtil.mqlCommand(context, sCommandStatement,masterId,"from["+STR_OBJECT_ROUTE+"]", "|"); 
					//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

					canCheckout = canCheckout && ("false".equalsIgnoreCase(sCanCheckoutOrLock));
					canLock = canLock && ("false".equalsIgnoreCase(sCanCheckoutOrLock));
				}
				*/

				canCheckin = "true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS)) &&
				"true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_LOCKED)) &&
				contextUser.equals((String)mapObject.get(CommonDocument.SELECT_LOCKER));

				canViewAndDownload = "true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS));

				canUnlock = "true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_LOCKED))&& (contextUser.equals((String)mapObject.get(CommonDocument.SELECT_LOCKER)) && "true".equalsIgnoreCase((String)mapObject.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS)));

				if ( canViewAndDownload )
				{
					if ( !isprinterFriendly )
					{
						// As per discussed with common team to resolve veiwer issue(119102V6R2012_)instead of master id actual file object id
						// is passed and file name is no more passed as parameter for the callcheckout for this case.
						
						if ( viewerTip.equalsIgnoreCase("Default") ){	
						fileActionsStrBuff.append("<a href=\"javascript:callCheckout('");
						//For structure browser Japanese name is getting garbled for IE. So name is removed from parameter 
						//and instead of master id, actual file object id is passed.
						fileActionsStrBuff.append(versionId);
						fileActionsStrBuff.append("','view','");
						fileActionsStrBuff.append("','");
						fileActionsStrBuff.append(fileFormat);
						fileActionsStrBuff.append("', null, null, null, null, null");
						fileActionsStrBuff.append(");\" >");
						}
						else{							
							viewerURL = servletPreFix + viewerURL + "?action=view&amp;";
							fileActionsStrBuff.append("<a href=\"javascript:openWindow(");
							fileActionsStrBuff.append("\'"+viewerURL);
							fileActionsStrBuff.append("id=");
							fileActionsStrBuff.append(masterId);
							fileActionsStrBuff.append("&amp;filename=");
							fileActionsStrBuff.append(fileName);
							fileActionsStrBuff.append("&amp;format=");
							fileActionsStrBuff.append(fileFormat);
							fileActionsStrBuff.append("\',\'570\',\'520\'");
							fileActionsStrBuff.append(");\" >");
						}
						
						fileActionsStrBuff.append( "<img src=\"../common/images/iconActionView.gif\" border='0' alt=\"");
						fileActionsStrBuff.append(i18nViewerTip);
						fileActionsStrBuff.append("\" title=\"");
						fileActionsStrBuff.append(i18nViewerTip);
						fileActionsStrBuff.append("\"></img></a>&#160;");
						// [MODIFIED::4-July-2011:RG6:R212:IR-106313V6R2012x::End]

						// [MODIFIED::Jun 9, 2011:S4E:R212:IR-111268V6R2012x::Start]
						//Changes suggeested by ijb
						fileActionsStrBuff.append("<a href='javascript:callCheckout(\"");
						fileActionsStrBuff.append(versionId);
						fileActionsStrBuff.append("\",\"download\", \"\", \"\",\"");
						fileActionsStrBuff.append("");
						fileActionsStrBuff.append("\", \"");
						fileActionsStrBuff.append("");
						fileActionsStrBuff.append("\", \"");
						fileActionsStrBuff.append("Table");
						fileActionsStrBuff.append("\", \"");
						fileActionsStrBuff.append("PMCFolderSummary");
						fileActionsStrBuff.append("\"");
						fileActionsStrBuff.append(")'>");
						fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
						fileActionsStrBuff.append(sTipDownload);
						fileActionsStrBuff.append("\" title=\"");
						fileActionsStrBuff.append(sTipDownload);
						fileActionsStrBuff.append("\"></img></a>&#160;");
					}
					else
					{
						fileActionsStrBuff.append("<a href=\"#\" ><img border=\"0\" src=\"../common/images/iconActionView.gif\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::Start
						fileActionsStrBuff.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionDownload.gif' alt=\"");
						fileActionsStrBuff.append(sTipDownload);
						fileActionsStrBuff.append("\" title=\"");
						fileActionsStrBuff.append(sTipDownload);
						fileActionsStrBuff.append("\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::End
					}
					// [MODIFIED::Jun 9, 2011:S4E:R212:IR-111268V6R2012x::End]
				}
				if ( "False".equalsIgnoreCase(suspendVersioning) && "true".equalsIgnoreCase(parentCheckinAccess))
				{
					if ( canCheckout )
					{
						// [MODIFIED::Jun 9, 2011:S4E:R212:IR-111268V6R2012x::Start]
						//Changes suggeested by ijb
						if ( !isprinterFriendly )
						{
							//Below code is copy modified and pasted from ${CLASS:emxCommonDocumentUIBase}.java.getDocumentActions()
							fileActionsStrBuff.append("<a href='javascript:callCheckout(\"");
							fileActionsStrBuff.append(versionId);
							fileActionsStrBuff.append("\",\"checkout\", \"\", \"\",\"");
							fileActionsStrBuff.append("false");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("Table");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("programcentral");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("");
							fileActionsStrBuff.append("\", \"");
							fileActionsStrBuff.append("emxProgramCentraFolderUtil.jsp\"");
							fileActionsStrBuff.append(")'>");
							fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\"");
							fileActionsStrBuff.append(sTipCheckout);
							fileActionsStrBuff.append("\" title=\"");
							fileActionsStrBuff.append(sTipCheckout);
							fileActionsStrBuff.append("\"></img></a>&#160;");                		
						}
						else
						{
							fileActionsStrBuff.append("<a href=\"#\" ><img border='0' src='../common/images/iconActionCheckOut.gif' alt=\""); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::Start
							fileActionsStrBuff.append(sTipCheckout);
							fileActionsStrBuff.append("\" title=\"");
							fileActionsStrBuff.append(sTipCheckout);
							fileActionsStrBuff.append("\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::End
						}
						// [MODIFIED::Jun 9, 2011:S4E:R212:IR-111268V6R2012x::End]
					}
					else if ( canCheckin )
					{
						if ( !isprinterFriendly )
						{
							if( largeFile )
							{							 
								checkinURL = "../components/emxCommonDocumentPreCheckin.jsp?showComments=required&amp;actionMode=checkin&amp;refreshTable=true&amp;deleteFromTree=" + versionId
								+ "&amp;objectId="+ masterId + "&amp;showFormat=readonly&amp;largeFileUpdate=true&amp;append=true&amp;objectAction="+CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION +"&amp;"
								+ "format=" + encodedFormat + "&amp;oldFileName="+ encodedFileName + "&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp";							 
								fileActionsStrBuff.append("<a href='javascript:showModalDialog(\"" + checkinURL + "\",730,450)'>");
								fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>&#160;");
							}
							else
							{							 
								
								String oldFileName = encodedFileName;
								encodedFileName = XSSUtil.encodeForJavaScript(context, encodedFileName);

								checkinURL = "../components/emxCommonDocumentPreCheckin.jsp?showComments=required&amp;actionMode=checkin&amp;refreshTable=true&amp;deleteFromTree="+versionId
								+"&amp;objectId="+ masterId+"&amp;showFormat=readonly&amp;append=true&amp;objectAction="+CommonDocument.OBJECT_ACTION_UPDATE_MASTER 
								+"&amp;format="+encodedFormat+"&amp;oldFileName="+encodedFileName +"&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp";

								fileActionsStrBuff.append("<a href='javascript:showModalDialog(\"" + checkinURL + "\",730,450)'>");
								fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>&#160;");
							}
						}
						else
						{
							fileActionsStrBuff.append("<a href=\"#\" ><img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
						}
					}
				}

				if(canUnlock && "true".equalsIgnoreCase(parentCheckinAccess))
				{
					if ( !isprinterFriendly )
					{
						unlockURL = "../components/emxCommonDocumentUnlock.jsp?&amp;objectId="+ versionId + "&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp";
						fileActionsStrBuff.append("<a href=\"javascript:submitWithCSRF('"+unlockURL+"',findFrame(getTopWindow(),'listHidden'))\">");
						fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+sTipUnlock+"\" title=\""+sTipUnlock+"\"></img></a>&#160;");
					}
					else
					{
						fileActionsStrBuff.append("<a href=\"#\" ><img border=\"0\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+sTipUnlock+"\" title=\""+sTipUnlock+"\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
					}
				}

				if (canLock && "true".equalsIgnoreCase(parentCheckinAccess))
				{
					if ( !isprinterFriendly )
					{
						unlockURL = "../components/emxCommonDocumentLock.jsp?&amp;objectId="+ versionId+ "&amp;appDir=programcentral&amp;appProcessPage=emxProgramCentraFolderUtil.jsp";
						fileActionsStrBuff.append("<a href=\"javascript:submitWithCSRF('"+unlockURL+"', findFrame(getTopWindow(),'listHidden'))\">");
						fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionLock.gif\" alt=\""+sTipLock+"\" title=\""+sTipLock+"\"></img></a>&#160;");
					}
					else
					{
						fileActionsStrBuff.append("<a href=\"#\" ><img border=\"0\" src=\"../common/images/iconActionLock.gif\" alt=\""+sTipLock+"\" title=\""+sTipLock+"\"></img></a>&#160;"); //Modified:PRG:RG6:R212:5-July-2011:IR-113397V6R2012x::
					}
				}

				fileActions = fileActionsStrBuff.toString();
				vecDocumentFileActions.add(fileActions);
			}
			else
			{
				vecDocumentFileActions.add("");
			}
		}
		return vecDocumentFileActions;
	}

	/** This method create output string with the common events presents between the subscription map
	 * and  the current string containing the common events.
	 * @param Map subscribed events map
	 * @param String holds the previous commonly subscribed events
	 * @return String holding new commonly subscribed events
	 * @throws Exception
	 * @since PRG R210
	 */
	private String getCommonEventList(Map objSubscriptionMap,String sbCommonEvents){
		StringBuffer sbOutput=new StringBuffer();  // String containing comma separated list of-
		// - common subscribed event
		// if sbCommonEvents is null then function is called for 1st time
		if(sbCommonEvents!=null){
			// get previous common event list
			StringList slCommonEventList=FrameworkUtil.split(sbCommonEvents.toString(),",");
			Iterator it=slCommonEventList.iterator();
			while(it.hasNext()){
				String eventName=(String)it.next();
				// find if current subscription map also contain the common events
				//previously subscribed
				if(objSubscriptionMap.containsKey(eventName)){
					sbOutput.append(eventName);
					sbOutput.append(",");
				}
			}
		}else{
			Set eventKeySet=objSubscriptionMap.keySet();
			Iterator it= eventKeySet.iterator();
			while(it.hasNext()){
				String eventName=(String)it.next();
				sbOutput.append(eventName);
				sbOutput.append(",");
			}
		}
		return sbOutput.toString();
	}

	/**
	 * Method retrieve the available notification events for the given type
	 * @author RG6
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments having requestMap which contains
	 *            rowIds - gives comma separated list of selected objects(Folders)
	 *            objectId - Available when called from action column
	 * @return String - returns the program HTML output containing table showing the list of
	 *                   events available for Folder Subscription
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	public String getSubscribableEventList(Context context, String[] args)
	throws MatrixException {
		String strOuput = null;
		StringBuffer output = new StringBuffer();
		int objectCount = 0;  // count for number of rows checked
		boolean isTypeSimiler=true;
		boolean recursionEnabled = false;
		boolean isMultiObjectCommonEventsExists=true; // if multiple object are checked having -
		//- common subscription events
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String selectedObjectIds = (String) requestMap.get("rowIds");
			String sObjectId="";
			String sObjectType="";
			String sbCommonEvents=null;
			String strLanguage = (String) requestMap.get("languageStr");
			final String STYPE_WORKSPACE_VAULT=DomainConstants.TYPE_WORKSPACE_VAULT;
			final String STYPE_CONTROLLED_FOLDER=DomainConstants.TYPE_CONTROLLED_FOLDER;
			// This value is used when objects of different types are selected
			//change the properties key
			String strUncommonObjs = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.SubscriptionOptions.UncommonObjs", strLanguage);
			String strNoEvents = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.SubscriptionOptions.NoEvents", strLanguage);

			if(selectedObjectIds != null && selectedObjectIds.trim().length()>0){
				// if not null then called from action toolbar menu
				StringList slObjectIdList=new StringList(); //list to store folder object ids
				slObjectIdList=FrameworkUtil.split(selectedObjectIds, ",");
				Iterator it1 = slObjectIdList.iterator();
				/*
				 * get each object id and find it's type and check all the selected objects
				 * are of same type (either Workspace Vault or Controlled Folder) or not.
				 */
				while(it1.hasNext()) {
					String sObjId=(String)it1.next();
					DomainObject dObj = DomainObject.newInstance(context, sObjId);
					StringList selectsList = new StringList();
					selectsList.addElement(DomainConstants.SELECT_TYPE);
					Map selectMap = (Map) dObj.getInfo(context, selectsList);
					// get the type of object
					String objType = (String) selectMap.get(DomainConstants.SELECT_TYPE);
					if(!(STYPE_WORKSPACE_VAULT.equalsIgnoreCase(objType)||STYPE_CONTROLLED_FOLDER.equalsIgnoreCase(objType))){
						isTypeSimiler=false;
						output.append("<script language=\"javascript\">");
						output.append("alert(\"");output.append(strUncommonObjs);output.append("\");");
						output.append("if(top.closeSlideInDialog){");  //Added:PRG:RG6:R212:2-Jun-2011:
						output.append("top.closeSlideInDialog();");   // [MODIFIED::PRG:RG6:Mar 8, 2011:IR-098164V6R2012 :R211::change for slidein window]
						output.append("}");
						output.append("else { window.closeWindow(); }");  //End Added:PRG:RG6:R212:2-Jun-2011:
						output.append("</script>");
						//append javascript alert to o/p
						// close the window
						return output.toString();
						// throw new Exception();  // throw exeption here
					}
					if(objectCount==0){
						sObjectId=sObjId;
						sObjectType=objType;
					}
					objectCount++;
					// Added:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
					/*  new logic modified on 9 august 2010 rg6
                Map objEventMap = SubscriptionUtil.getObjectSubscribedEventsMap(context, sObjId);
                System.out.println("objEventMap==**"+objEventMap);
                Map objSubscriptionMap = (HashMap) objEventMap.get("Subscription Map");
					 */
					Map objSubscriptionMap = getSubscribedEventMap(context,sObjId);
					// End:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
					if(objSubscriptionMap!=null){
						if(sbCommonEvents!=null){ //find the common event list
							if(sbCommonEvents.length()>0){
								sbCommonEvents=getCommonEventList(objSubscriptionMap,sbCommonEvents);
							}else{  // no common events
								isMultiObjectCommonEventsExists=false;
							}
						}else{   // find the common events
							sbCommonEvents=getCommonEventList(objSubscriptionMap,sbCommonEvents);
						}
					}else{  // do not show checked objects
						isMultiObjectCommonEventsExists=false;
					}
				}
			}else{  // if rowid is null then  called from action column
				String objectId = (String) requestMap.get("objectId");
				if(objectId!=null){      // append the object id in the object list
					selectedObjectIds=objectId;
					DomainObject dObj = DomainObject.newInstance(context, objectId);
					StringList selectsList = new StringList();
					selectsList.addElement(DomainConstants.SELECT_TYPE);
					Map selectMap =  dObj.getInfo(context, selectsList);
					String objType = (String) selectMap.get(DomainConstants.SELECT_TYPE);
					sObjectId=objectId;
					sObjectType=objType;
					objectCount=1;
					isMultiObjectCommonEventsExists=false;
				}
			}
			// i/p params  for  the getRelated method
			String strRelationshipPattern = DomainConstants.RELATIONSHIP_SUBVAULTS;
			String strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;
			StringList slBusSelect =  new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			StringList slRelSelect = new StringList();
			// get the available list of notification events for the given object type
			if(sObjectId!=null&&sObjectId.length()>0){
				output.append("<input type=\"hidden\" name=sObjectId");
				output.append(" value=\'");output.append(sObjectId);output.append("\' />");
				output.append("<input type=\"hidden\" name=sObjectType");
				output.append(" value=\'");output.append(sObjectType);output.append("\' />");
				MapList eventList = SubscriptionUtil.getObjectSubscribableEventsList(context, sObjectId,
						sObjectType, requestMap);

				Map objEventMap = new HashMap();
				Map subscriptionMap = new HashMap(); //contains the list of subscribed events for object
				//javascript function to find  out which events are checked
				//and attach the value to corresponding event's hidden type.
				output.append("<script language=\"javascript\">");
				output.append(" function onSubmit(clickcheck){");
				output.append("var obj = document.getElementById(clickcheck.value);");
				output.append("if(obj != null && obj != 'undefined' && obj != 'null') {");
				output.append("obj.value=clickcheck.checked; } }  </script>");
				/*  check if object has child sub folder if yes then show the
                 propagate subscription 'check box' element.               */
				DomainObject domObj =DomainObject.newInstance(context,sObjectId);
				// get the list of sub folders
				/*  rg6         MapList mlSubFolderList = domObj.getRelatedObjects(context,
                     strRelationshipPattern, //pattern to match relationships
                     strTypePattern, //pattern to match types
                     slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                     slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                     false, //get To relationships
                     true, //get From relationships
                     (short)0, //the number of levels to expand, 0 equals expand all.
                     null, //where clause to apply to objects, can be empty ""
                     null,
                     0);
				 */
				if (eventList == null || eventList.size() <= 0) {
					// if no subscription events exists for given type then display the message
					output.append("<table border=\"0\" cellpadding=\"5\"");
					output.append(" cellspacing=\"2\" width=\"100%\">");
					output.append("<tr><td class=\"label\">");output.append(strNoEvents);
					output.append("</td></tr> </table>");
				} else {
					// create the table for displaying the list of events
					output.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"100%\">");
					output.append("<tr>");
					output.append("<td class=\"inputField\">");
					output.append("<table border=\"0\">");

					Iterator it = eventList.iterator();
					Map tempMap = new HashMap();
					String eventName = "";
					String isRecursible = "";
					String recurseRel = "";
					String forceRecursion = "";
					StringList slCommonEventList=null;
					if(objectCount==1){
						// Added:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
						/* new logic modified on 9 th august 2010
						 * objEventMap = SubscriptionUtil.getObjectSubscribedEventsMap(context, sObjectId);
                     subscriptionMap = (HashMap) objEventMap.get("Subscription Map");
						 */
						subscriptionMap = getSubscribedEventMap(context,sObjectId);
						// Added:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
					}else{
						if(isMultiObjectCommonEventsExists){
							slCommonEventList=FrameworkUtil.split(sbCommonEvents,",");
						}else{
							subscriptionMap=null;
						}
					}
					// loop to render all the notification events
					while (it.hasNext()) {
						tempMap = (HashMap) it.next();
						eventName = UIComponent.getSetting(tempMap, "Event Type");
						isRecursible = UIComponent.getSetting(tempMap,"Is Recursible");
						forceRecursion = UIComponent.getSetting(tempMap,"Force Recursion");
						String i18nEventName = UIComponent.getLabel(tempMap);
						String isRecurseSelected = "false";
						boolean subscriptionExists = false;
						//finds if the object has some events previously subscribed to him
						if(objectCount==1){
							// finds if current event is subscribed or not
							subscriptionExists = subscriptionMap.containsKey(eventName);
						}else{
							if(isMultiObjectCommonEventsExists){
								if(slCommonEventList.contains(eventName)){
									subscriptionExists=true;
								}
							}else{
								subscriptionExists=false;
							}
						}
						//if event is subscribed then show the event as 'checked' event
						//if object count is exactly 1 then only show event as checked
						if (subscriptionMap!=null && subscriptionExists ){
							output.append("<tr><td><input type=\"checkbox\" ");
							output.append("onClick=\"onSubmit(this)\"");
							output.append(" value=\'");output.append(eventName);
							output.append("\' name=\"chkUnSubscribeEvent\" checked></td><td>");
							output.append(i18nEventName);output.append("</td></tr>");
							// if subscription exists then set the value to true,
							// hidden parameters used to pass the checked events values,
							// (also used in javascript function)
							output.append("<input type=\"hidden\" name=\'val");
							output.append(eventName);output.append("\' id=\'");
							output.append(eventName);output.append("\' value=\"true\"  />");
						} else {
							// if subscription does not exist then show event as 'unchecked' event
							output.append("<tr><td><input type=\"checkbox\" ");
							output.append("onClick=\"onSubmit(this)\"");
							output.append(" value=\'");
							output.append(eventName);
							output.append("\'  name=\"chkSubscribeEvent\"></td><td>");
							output.append(i18nEventName);output.append("</td></tr>");
							// hidden parameters used to pass the checked events values,
							// (also used in javascript function)
							output.append("<input type=\"hidden\" name=\'val");
							output.append(eventName);output.append("\'");
							output.append(" id=\'");output.append(eventName);output.append("\' value=\"false\"  />");
						}
					}
				}
				output.append("<input type=\"hidden\" name=selectedIds");
				output.append(" value=");output.append(selectedObjectIds);output.append(" />");
			}

			output.append("</table> </td> </tr> </table>");           // complete the table
		} catch (Exception ex) {
			throw new MatrixException(ex);
		} finally {
			return output.toString();
		}
	}

	/**
	 * Method retrieve the available notification events for the given type
	 * @author FZS
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments having requestMap which contains
	 *            rowIds - gives comma separated list of selected objects(Folders)
	 *            objectId - Available when called from action column
	 * @return String - returns the program HTML output containing table showing the list of
	 *                   events available for Folder Subscription
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R211
	 */
	public String getWorkspaceVaultDocumentSubscribableEventList(Context context, String[] args)
	throws MatrixException {
		String strOuput = null;
		StringBuffer output = new StringBuffer();
		int objectCount = 0;  // count for number of rows checked
		boolean isMultiObjectCommonEventsExists=true; // if multiple object are checked having -
		//- common subscription events
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String selectedObjectIds = (String) requestMap.get("rowIds");
			String sObjectId="";
			String sObjectType="";
			String sbCommonEvents=null;
			String strLanguage = (String) requestMap.get("languageStr");
			// This value is used when objects of different types are selected
			//change the properties key
			String strUncommonObjs = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.SubscriptionOptions.UncommonObjs",strLanguage);
			String strNoEvents = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
					"emxProgramCentral.SubscriptionOptions.NoEvents", strLanguage);

			if(selectedObjectIds != null && selectedObjectIds.trim().length()>0){
				// if not null then called from action toolbar menu
				StringList slObjectIdList=new StringList(); //list to store folder object ids
				slObjectIdList=FrameworkUtil.split(selectedObjectIds, ",");
				Iterator it1 = slObjectIdList.iterator();
				/*
				 * get each object id and find it's type and check all the selected objects
				 * are of same type (Documents) or not.
				 */
				while(it1.hasNext()) {
					String sObjId=(String)it1.next();
					DomainObject dObj = DomainObject.newInstance(context, sObjId);
					StringList selectsList = new StringList();
					selectsList.addElement(DomainConstants.SELECT_TYPE);
					Map selectMap = (Map) dObj.getInfo(context, selectsList);
					// get the type of object
					String objType = (String) selectMap.get(DomainConstants.SELECT_TYPE);
					if(!dObj.isKindOf(context, DomainConstants.TYPE_DOCUMENT)){
						output.append("<script language=\"javascript\">");
						output.append("alert(\"");output.append(strUncommonObjs);output.append("\");");
						output.append("window.parent.closeWindow();");
						output.append("</script>");
						//append javascript alert to o/p
						// close the window
						return output.toString();
						// throw new Exception();  // throw exeption here
					}
					if(objectCount==0){
						sObjectId=sObjId;
						sObjectType=objType;
					}
					objectCount++;
					Map objSubscriptionMap = getSubscribedEventMap(context,sObjId);

					if(objSubscriptionMap!=null){
						if(sbCommonEvents!=null){ //find the common event list
							if(sbCommonEvents.length()>0){
								sbCommonEvents=getCommonEventList(objSubscriptionMap,sbCommonEvents);
							}else{  // no common events
								isMultiObjectCommonEventsExists=false;
							}
						}else{   // find the common events
							sbCommonEvents=getCommonEventList(objSubscriptionMap,sbCommonEvents);
						}
					}else{  // do not show checked objects
						isMultiObjectCommonEventsExists=false;
					}
				}
			}else{  // if rowid is null then  called from action column
				String objectId = (String) requestMap.get("objectId");
				if(objectId!=null){      // append the object id in the object list
					selectedObjectIds=objectId;
					DomainObject dObj = DomainObject.newInstance(context, objectId);
					StringList selectsList = new StringList();
					selectsList.addElement(DomainConstants.SELECT_TYPE);
					Map selectMap =  dObj.getInfo(context, selectsList);
					String objType = (String) selectMap.get(DomainConstants.SELECT_TYPE);
					sObjectId=objectId;
					sObjectType=objType;
					objectCount=1;
					isMultiObjectCommonEventsExists=false;
				}
			}
			// i/p params  for  the getRelated method
			StringList slBusSelect =  new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			StringList slRelSelect = new StringList();
			// get the available list of notification events for the given object type
			if(sObjectId!=null&&sObjectId.length()>0){
				output.append("<input type=\"hidden\" name=sObjectId");
				output.append(" value=\'");output.append(sObjectId);output.append("\' />");
				output.append("<input type=\"hidden\" name=sObjectType");
				output.append(" value=\'");output.append(sObjectType);output.append("\' />");
				MapList eventList = SubscriptionUtil.getObjectSubscribableEventsList(context, sObjectId,
						sObjectType, requestMap);

				Map objEventMap = new HashMap();
				Map subscriptionMap = new HashMap(); //contains the list of subscribed events for object
				//javascript function to find  out which events are checked
				//and attach the value to corresponding event's hidden type.
				output.append("<script language=\"javascript\">");
				output.append(" function onSubmit(clickcheck){");
				output.append("var obj = document.getElementById(clickcheck.value);");
				output.append("if(obj != null && obj != 'undefined' && obj != 'null') {");
				output.append("obj.value=clickcheck.checked; } }  </script>");
				/*  check if object has child sub folder if yes then show the
                 propagate subscription 'check box' element.               */
				DomainObject domObj =DomainObject.newInstance(context,sObjectId);

				if (eventList == null || eventList.size() <= 0) {
					// if no subscription events exists for given type then display the message
					output.append("<table border=\"0\" cellpadding=\"5\"");
					output.append(" cellspacing=\"2\" width=\"100%\">");
					output.append("<tr><td class=\"label\">");output.append(strNoEvents);
					output.append("</td></tr> </table>");
				} else {
					// create the table for displaying the list of events
					output.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"100%\">");
					output.append("<tr>");
					output.append("<td class=\"inputField\">");
					output.append("<table border=\"0\">");

					Iterator it = eventList.iterator();
					Map tempMap = new HashMap();
					String eventName = "";
					String isRecursible = "";
					String recurseRel = "";
					String forceRecursion = "";
					StringList slCommonEventList=null;
					if(objectCount==1){
						subscriptionMap = getSubscribedEventMap(context,sObjectId);
					}else{
						if(isMultiObjectCommonEventsExists){
							slCommonEventList=FrameworkUtil.split(sbCommonEvents,",");
						}else{
							subscriptionMap=null;
						}
					}
					// loop to render all the notification events
					while (it.hasNext()) {
						tempMap = (HashMap) it.next();
						eventName = UIComponent.getSetting(tempMap, "Event Type");
						isRecursible = UIComponent.getSetting(tempMap,"Is Recursible");
						forceRecursion = UIComponent.getSetting(tempMap,"Force Recursion");
						String i18nEventName = UIComponent.getLabel(tempMap);
						String isRecurseSelected = "false";
						boolean subscriptionExists = false;
						//finds if the object has some events previously subscribed to him
						if(objectCount==1){
							// finds if current event is subscribed or not
							subscriptionExists = subscriptionMap.containsKey(eventName);
						}else{
							if(isMultiObjectCommonEventsExists){
								if(slCommonEventList.contains(eventName)){
									subscriptionExists=true;
								}
							}else{
								subscriptionExists=false;
							}
						}
						//if event is subscribed then show the event as 'checked' event
						//if object count is exactly 1 then only show event as checked
						if (subscriptionMap!=null && subscriptionExists ){
							output.append("<tr><td><input type=\"checkbox\" ");
							output.append("onClick=\"onSubmit(this)\"");
							output.append(" value=\'");output.append(eventName);
							output.append("\' name=\"chkUnSubscribeEvent\" checked></td><td>");
							output.append(i18nEventName);output.append("</td></tr>");
							// if subscription exists then set the value to true,
							// hidden parameters used to pass the checked events values,
							// (also used in javascript function)
							output.append("<input type=\"hidden\" name=\'val");
							output.append(eventName);output.append("\' id=\'");
							output.append(eventName);output.append("\' value=\"true\"  />");
						} else {
							// if subscription does not exist then show event as 'unchecked' event
							output.append("<tr><td><input type=\"checkbox\" ");
							output.append("onClick=\"onSubmit(this)\"");
							output.append(" value=\'");
							output.append(eventName);
							output.append("\'  name=\"chkSubscribeEvent\"></td><td>");
							output.append(i18nEventName);output.append("</td></tr>");
							// hidden parameters used to pass the checked events values,
							// (also used in javascript function)
							output.append("<input type=\"hidden\" name=\'val");
							output.append(eventName);output.append("\'");
							output.append(" id=\'");output.append(eventName);output.append("\' value=\"false\"  />");
						}
					}
				}
				output.append("<input type=\"hidden\" name=selectedIds");
				output.append(" value=");output.append(selectedObjectIds);output.append(" />");
			}

			output.append("</table> </td> </tr> </table>");           // complete the table
		} catch (Exception ex) {
			throw new MatrixException(ex);
		} finally {
			return output.toString();
		}
	}

	/**
	 * Method to create the subscription for the  each checked event in the subscription form.
	 * This method finds the  checked events for the given parent object and also finds that if parent
	 * want to propagate subscription for the child, if so  then fetch all the child up to the leaf and
	 * create the subscription for each one.
	 *
	 * @author RG6
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments which contains
	 *            requestMap - request Map
	 *            paramMap contains
	 *              selectedIds - list selected folder objects
	 *              subscribed events - contains the events which are passed for subscription creation
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createSubscriptionProcess(Context context, String[] args) throws Exception{

		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			Map objEventMap = new HashMap();
			Map subscriptionMap = new HashMap();
			// Modified:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
			// parameters for get related method
			String strRelationshipPattern = DomainConstants.RELATIONSHIP_SUBVAULTS+","+DomainConstants.RELATIONSHIP_LINKED_FOLDERS;
			// End:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
			String strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT;
			StringList slBusSelect =  new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			StringList slRelSelect = new StringList();
			HashMap paramMap= (HashMap) programMap.get("paramMap");
			String selectedObjectIds = (String)paramMap.get("selectedIds");
			//list of object ids for selected folders
			StringList slObjectIdList=new StringList();

			if(selectedObjectIds != null && selectedObjectIds.trim().length()>0){
				slObjectIdList=FrameworkUtil.split(selectedObjectIds, ",");
			}
			String sObjectId=(String)paramMap.get("sObjectId");
			DomainObject dObj = DomainObject.newInstance(context, sObjectId);
			StringList selectsList = new StringList();
			selectsList.addElement(DomainConstants.SELECT_TYPE);
			Map valueMap =  dObj.getInfo(context, selectsList);
			// get the type of object
			String objType = (String) valueMap.get(DomainConstants.SELECT_TYPE);

			// get the subscribable event list
			MapList eventList = SubscriptionUtil.getObjectSubscribableEventsList(context, sObjectId,
					objType, requestMap);

			String isCurrPropagateSubscriptionExists="false";  // var. to store current propagate sub event's value
			String sPropagateSubscription = "Propagate Subscription";
			isCurrPropagateSubscriptionExists=(String)paramMap.get("val"+sPropagateSubscription);

			for(Iterator it = slObjectIdList.iterator(); it.hasNext();){
				String objectId=(String)it.next();
				/* if propagate subscription is true then propagate the subscription to child types
                    propagate subscription does not work for propagate  un subscription
				 */
				// check if currently propagate subscription exists or not
				if(isCurrPropagateSubscriptionExists!=null && isCurrPropagateSubscriptionExists.equalsIgnoreCase("true")){

					//if propagation is 'true' then get the child objects(sub folders)
					DomainObject domObj =DomainObject.newInstance(context,objectId);

					MapList mlSubFolderList = domObj.getRelatedObjects(context,
							strRelationshipPattern, //pattern to match relationships
							strTypePattern, //pattern to match types
							slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
							slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
							false, //get To relationships
							true, //get From relationships
							(short)0, //the number of levels to expand, 0 equals expand all.
							null, //where clause to apply to objects, can be empty ""
							null,
							0);
					//create the subscription for each child object
					for (Iterator iterator = mlSubFolderList.iterator(); iterator.hasNext();)
					{
						Map objectMap = (Map) iterator.next();
						String sSubFolderObjectId= (String)objectMap.get(DomainConstants.SELECT_ID);
						try{
							//perform subscription process for each sub folder
							objectSubscriptionProcess(context,sSubFolderObjectId,eventList,paramMap,requestMap,false);
						}catch(Exception e){
							throw new MatrixException(e);
						}
					}
				}

				//perform subscription process for the parent  object
				objectSubscriptionProcess(context,objectId,eventList,paramMap,requestMap,true);
			}

		}catch (Exception ex) {

			throw new MatrixException(ex);
		} finally {
			return ;
		}

	}


	/**
	 * This Method is called from the createSubscriptionProcess and perform the actual subscription
	 * process for the given object.
	 * Method finds the subscription for object if it was previously subscribed and
	 * currently not  subscribed then disconnect the object and if vice versa then create  the
	 * subscription for the object.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param objectId
	 *            holds objectId for which to create the subscription
	 * @param eventList
	 *           holds all the available events for the type
	 *  @param paramMap
	 *           holds the paramMap came from request
	 *  @param requestMap
	 *           holds the requestMap
	 *  @param isParentFolder
	 *           holds the that weather the object passed is parent one
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	private  void objectSubscriptionProcess(Context context,String objectId,MapList eventList,HashMap paramMap,HashMap requestMap,boolean isParentFolder) throws Exception{
		//get subscribed events for the object
		StringBuffer sbSubcriptionEvents=new StringBuffer();  // store the list of checked events
		Map subscriptionMap=new HashMap();
		//get the subscription event list for each passed object
		subscriptionMap=getSubscribedEventMap(context,objectId);

		int subEventsCnt=0;  // stores the count of checked events
		Iterator it=eventList.iterator();
		Map tempMap = new HashMap();
		String sEventToken=null;
		// create or disconnect subscription for each checked event
		while(it.hasNext()){
			tempMap = (HashMap) it.next();
			String i18nEventName = UIComponent.getLabel(tempMap);
			// get the event name
			sEventToken = UIComponent.getSetting(tempMap, "Event Type");

			/*
			 * below StringTokenizer logic is added in order to saperate out the recursive events
			 * e.g. 'new discussion' event.
			 */
			StringTokenizer st = new StringTokenizer(sEventToken, ",");
			//finds if the object has some events previously subscribed to him
			boolean subscriptionExists=false;
			while (st.hasMoreTokens()) {
				String sToken = st.nextToken();
				subscriptionExists = subscriptionMap.containsKey(sToken);

				if (subscriptionExists) {
					sEventToken = sToken;
					break;
				}
			}
			String isSubscribed="false";
			//find if the current event is checked or not
			isSubscribed=(String)paramMap.get("val"+sEventToken);
			//check if event was previously subscribed or not
			if(subscriptionMap.containsKey(sEventToken)){
				// event is already subscribed
				if("true".equalsIgnoreCase(isSubscribed)){
					//event is subscribed currently also, do nothing
				}else{
					/*
                         disconnect the object
                         unsubscription does not work in case of propagation
                         only the events for parent level get unsubscribed.
                         Events of subfolders remains intact during unsubscription (propagation)
					 */
					if(isParentFolder){
						Map eventInfoMap = (HashMap) subscriptionMap.get(sEventToken);
						String relId = (String) eventInfoMap.get("ID");
						DomainRelationship.disconnect(context, relId);
					}
				}
			}else{
				//event was not subscribed previously
				if("true".equalsIgnoreCase(isSubscribed)){
					// check box is checked for this event, create subscription
					sbSubcriptionEvents.append(sEventToken+";");
					subEventsCnt++;
				}
			}
		}
		//create the subscription for every checked event
		if(subEventsCnt > 0){
			StringTokenizer strToken = new StringTokenizer(sbSubcriptionEvents.toString(), ";");
			String []subEventsArray=new String[subEventsCnt];
			int i=0;
			while (strToken.hasMoreTokens())
			{
				subEventsArray[i]= strToken.nextToken();
				i++;
			}
			//create the subscription for the given object
			SubscriptionUtil.createSubscriptions(context, objectId, (String[]) subEventsArray, requestMap);
		}
	}

	/**
	 * Method returns the subscription map containing list of notification events to
	 *  which the object is already subscribed.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param objectId
	 *            holds objectId
	 * @return HashMap
	 *               holds the subscription Map
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	private Map getSubscribedEventMap(Context context,String objectId) throws Exception{
		Map resultMap=new HashMap();
		Map subscriptionMap=new HashMap();
		Map pushedSubscriptionMap=new HashMap();
		Map objEventMap=new HashMap();
		try{
			objEventMap = SubscriptionUtil.getObjectSubscribedEventsMap(context, objectId);
			subscriptionMap = (HashMap) objEventMap.get("Subscription Map");
			// Added:rg6:12-Aug-2010:Controlled Folder revise trigger Added issue
			pushedSubscriptionMap = (HashMap) objEventMap.get("Pushed Subscription Map");
			resultMap.putAll(subscriptionMap);
			resultMap.putAll(pushedSubscriptionMap);
		}catch(Exception ex){
			throw new MatrixException(ex);
		}finally{
			return resultMap;
		}
	}

	/** The icon mail message created by this method
	 * @param context
	 *             the eMatrix <code>Context</code> object
	 * @param info
	 *             info map
	 * @return
	 * @throws Exception
	 * @since PRG R210
	 */
	private com.matrixone.jdom.Document getFolderMailXML(Context context, Map info) throws Exception
	{
		// get base url
		String baseURL = (String)info.get("baseURL");
		// get notification name
		String notificationName = (String)info.get("notificationName");
		Map eventCmdMap = UIMenu.getCommand(context, notificationName);
		String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
		final String SFOLDER_TYPE=DomainConstants.TYPE_WORKSPACE_VAULT;
		boolean isDeletedEvent=false;
		//Added: PRG:8-7-2011:I16:R212:IR_117025V6R2012x
		String strLanguage = context.getSession().getLanguage();
		//Ended: PRG:8-7-2011:I16:R212:IR_117025V6R2012x
		String eventKey = "emxProgramCentral.FolderSubscription.Event." + eventName.replace(' ', '_');
		String bundleName = (String)info.get("bundleName");
		String locale = ((Locale)info.get("locale")).toString();
		final String SFOLDER_DELETED = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.FolderSubscription.Event.Folder_Deleted", "en");
		String i18NEvent = EnoviaResourceBundle.getProperty(context, "ProgramCentral", eventKey, strLanguage);
		// tree menu passed as url parameter in order to show the navigation tree for workspace vault
		final String STREE_MENU;
		String messageType = (String)info.get("messageType");
		// get workspace vault id
		String sFolderObjectId = (String)info.get("id");
		// get workspace vault object info
		DomainObject mainDoc = DomainObject.newInstance(context, sFolderObjectId);
		StringList selectList = new StringList(3);
		selectList.addElement(DomainConstants.SELECT_TYPE);
		selectList.addElement(DomainConstants.SELECT_NAME);
		selectList.addElement(DomainConstants.SELECT_REVISION);
		Map folderInfoMap = mainDoc.getInfo(context, selectList);
		String mainFolderType = (String)folderInfoMap.get(DomainConstants.SELECT_TYPE);
		//Modified: PRG:8-7-2011:I16:R212:IR_117025V6R2012x Start
		String i18NMainFolderType = UINavigatorUtil.getAdminI18NString("Type", mainFolderType, strLanguage);
		//Modified: PRG:8-7-2011:I16:R212:IR_117025V6R2012x End
		String mainFolderName = (String)folderInfoMap.get(DomainConstants.SELECT_NAME);
		String mainFolderRev = (String)folderInfoMap.get(DomainConstants.SELECT_REVISION);
		//check if folder is either controlled folder or workspace vault
		if(SFOLDER_TYPE.equalsIgnoreCase(mainFolderType)){
			STREE_MENU = EnoviaResourceBundle.getProperty(context, "eServiceSuiteProgramCentral.emxTreeAlternateMenuName.type_ProjectVault");
		}else{
			STREE_MENU = EnoviaResourceBundle.getProperty(context,"eServiceSuiteProgramCentral.emxTreeAlternateMenuName.type_ControlledFolder");
		}
		if(eventName!=null&&eventName.length()>0){
			if(eventName.equalsIgnoreCase(SFOLDER_DELETED))
				isDeletedEvent=true;
		}
		// header data
		Map headerInfo = new HashMap();
		headerInfo.put("header", i18NEvent + " : " + i18NMainFolderType + " " + mainFolderName + " " + mainFolderRev);
		//body info
		Map bodyInfo = null;
		// footer data
		Map footerInfo=null;
		//check if event is for folder deleted if yes do not show the link
		if(!isDeletedEvent){
			footerInfo = new HashMap();
			ArrayList dataLineInfo = new ArrayList();
			if (messageType.equalsIgnoreCase("html")) {
				String[] messageValues = new String[4];
				messageValues[0] = baseURL + "?objectId=" + sFolderObjectId + "&amp;treeMenu="+ STREE_MENU;
				messageValues[1] = i18NMainFolderType;
				messageValues[2] = mainFolderName;
				messageValues[3] = mainFolderRev;
				String viewLink = MessageUtil.getMessage(context,null,
						"emxProgramCentral.Object.Event.Html.Mail.ViewLink",
						messageValues,null,
						context.getLocale(),bundleName);
				dataLineInfo.add(viewLink);
			} else {
				String[] messageValues = new String[3];
				messageValues[0] = i18NMainFolderType;
				messageValues[1] = mainFolderName;
				messageValues[2] = mainFolderRev;
				String viewLink = MessageUtil.getMessage(context,null,
						"emxProgramCentral.Object.Event.Text.Mail.ViewLink",
						messageValues,null,
						context.getLocale(),bundleName);
				dataLineInfo.add(viewLink);
				dataLineInfo.add(baseURL + "?objectId=" + sFolderObjectId + "&amp;treeMenu="+ STREE_MENU);
			}
			footerInfo.put("dataLines", dataLineInfo);
		}
		return emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, footerInfo);
	}


	/** This method create the icon mail message content for subscription notification
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments contains
                   notificationName -  name of notification object corresponding to event name
                   baseURL - pointing to the object's tree
                   messageType - type of message, 'text'
                   id - object's id
	 * @return String
	 * @throws Exception if the operation fails
	 * @since PRG R210
	 */
	public String getFolderMessageText(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "text");
		com.matrixone.jdom.Document doc = getFolderMailXML(context, info);

		return emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text");

	}

	/**
	 * This method create the icon mail message content for subscription notification
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments contains
                   notificationName -  name of notification object corresponding to event name
                   baseURL - pointing to the object's tree
                   messageType - type of message, 'html'
                   id - object's id
	 * @return String
	 * @throws Exception if the operation fails
	 * @since PRG R210
	 */
	public String getFolderMessageHTML(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "html");
		com.matrixone.jdom.Document doc = getFolderMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));
	}

	/**
	 * This method find object's parent id and delegate the call
	 * to objectNotification method with event either content deleted or content removed
	 * based on the type of document object.
	 *
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 *             args[0] - holds the parent object id of kind workspace vault
	 *             args[1] - content deleted notification object
	 *             args[2] - content removed notification object
	 *             args[3] - child object id of kind DOCUMENTS
	 *
	 * @return void
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	public void deleteRemoveFolderContentNotification(Context context, String[] args)
	throws Exception {
		try{
			if (args == null || args.length < 4) {
				throw (new IllegalArgumentException());
			}
			int index = 0;
			String sFromObjectId = args[index++];
			String notificationName1 = args[index++];
			String notificationName2 = args[index++];
			String sObjectId = args[index++];
			// get the type of object
			DomainObject dObj = DomainObject.newInstance(context,sObjectId);
			StringList selectsList = new StringList();
			selectsList.add(DomainConstants.SELECT_ID);
			
			MapList mlFolderList = dObj.getRelatedObjects(context,
	                		ProgramCentralConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2,
	                        DomainObject.QUERY_WILDCARD,
	                        selectsList, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
	                        null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
	                        true, //get To relationships
	                        false, //get From relationships
	                        (short)1, //the number of levels to expand, 0 equals expand all.
	                        DomainObject.EMPTY_STRING, //where clause to apply to objects, can be empty ""
	                        DomainObject.EMPTY_STRING,//where clause to apply to relationship, can be empty ""
	                        0);

			// if object is document then event is content deleted
			if(dObj.isKindOf(context, CommonDocument.TYPE_DOCUMENTS) && (mlFolderList.size() == 0 )){  //PRG:RG6:R212:2-Jun-2011:
				//delegate the call to object notification for subscription
				emxNotificationUtilBase_mxJPO.objectNotification(context, sFromObjectId, notificationName1, null);
			}else{
				//for non document objects event is removed
				//delegate the call to object notification for subscription
				emxNotificationUtilBase_mxJPO.objectNotification(context, sFromObjectId, notificationName2, null);
			}

		}catch(Exception e){
			throw new MatrixException(e);
		}

	}

	/**
	 * This method creates the propagate subscription check box element
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * @return String programHtlmOutput containing html table for propagate subscription
	 *         html checkbox element.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	public String getPropagateSubscriptionCheckboxElement(Context context, String[] args) throws Exception{

		StringBuffer output =new StringBuffer();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strLanguage = (String) requestMap.get("languageStr");

			String sPropagateSubscription = "Propagate Subscription";
			String si18PropagateSubscription = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.FolderSubscription.Event.PropagateSubscription", strLanguage);

			//create the check box element for propagate subscription
			output.append("<table border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"100%\">");
			output.append("<tr>");
			output.append("<td class=\"inputField\">");
			output.append("<table border=\"0\">");
			output.append("<tr><td>"+"<input type=\"checkbox\" ");
			output.append("onClick=\"onSubmit(this)\"");
			output.append(" value=\'");
			output.append(sPropagateSubscription);
			output.append("\'  name=\"chkSubscribeEvent\"></td><td>");
			output.append(si18PropagateSubscription);
			output.append("</td> </tr> </table> </td> </tr> </table>");

			//append the hidden parameter for the propagate subscription
			output.append("<input type=\"hidden\" name=\'val");
			output.append(sPropagateSubscription);
			output.append("\' id=\'");
			output.append(sPropagateSubscription);
			output.append("\' value=\"false\"  />");

		}catch(Exception e){
			throw new MatrixException(e);
		}finally{
			return output.toString();
		}
	}

	/**
	 * This method delegate the call to object notification for sending the notification and
	 * return '0' to show the successful execution of override trigger.
	 *
	 *@author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * @return integer
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	public int folderDeleteTriggerOverride(Context context,String[] args) throws Exception {
		return emxNotificationUtil_mxJPO.objectNotification(context, args);
	}

	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList getColumnDocumentLockStatusData(Context context,String[]args) throws Exception
	{
		Map mapProgramMap =(Map)JPO.unpackArgs(args);
		MapList objectList = (MapList)mapProgramMap.get("objectList");
		Map paramList = (Map)mapProgramMap.get("paramList");
		boolean isprinterFriendly = false;
		if(paramList.get("reportFormat") != null)
		{
			isprinterFriendly = true;
		}

		StringBuilder baseURLBuf = new StringBuilder(256);
		baseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getFiles&amp;table=APPFileSummary&amp;sortColumnName=PopupName&amp;");
		baseURLBuf.append("popup=true&amp;sortDirection=ascending&amp;popup=true&amp;header=emxComponents.Menu.Files&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;");
		baseURLBuf.append("HelpMarker=emxhelpcommondocuments&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");

		StringBuilder nonVersionableBaseURLBuf = new StringBuilder(256);
		nonVersionableBaseURLBuf.append("emxTable.jsp?program=emxCommonFileUI:getNonVersionableFiles&amp;table=APPNonVersionableFileSummary&amp;sortColumnName=Name&amp;");
		nonVersionableBaseURLBuf.append("popup=true&amp;sortDirection=ascending&amp;popup=true&amp;header=emxComponents.Menu.Files&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;");
		nonVersionableBaseURLBuf.append("HelpMarker=emxhelpcommondocuments&amp;suiteKey=Components&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1");

		StringList vcNewLockStatus = new StringList();

		DomainObject domainObject = null;
		String strObjectId = null;
		Map mapObject = null;
		String strIsversionObject = null;
		boolean isVersionObject =false;

		StringList files = null;
		StringList locked = null;
		String lock = DomainConstants.EMPTY_STRING;
		int lockCount = 0;
		int fileCount = 0;
		String file = DomainConstants.EMPTY_STRING;
		StringBuilder urlStringBuffer = null;
		boolean moveFilesToVersion = false;
		String strEditableMode = (String)paramList.get("editTableMode");   //Added PRG:RG6:R211:DOCMGNT

		int size =objectList.size();
		String[] arrObjectId = new String[size];
		for(int i=0;i<size;i++){
			Map objectMap = (Map)objectList.get(i);
			String objectId = (String)objectMap.get(DomainObject.SELECT_ID);
			arrObjectId[i] = objectId;
		}

		StringList slSelectable = new StringList(10);
		slSelectable.add(CommonDocument.SELECT_ID);
		slSelectable.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
		slSelectable.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
		slSelectable.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
		slSelectable.add(CommonDocument.SELECT_FILE_NAME);
		slSelectable.add(CommonDocument.SELECT_FILE_FORMAT);
		slSelectable.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
		slSelectable.add(CommonDocument.SELECT_LOCKED);
		slSelectable.add(CommonDocument.SELECT_LOCKER);
		slSelectable.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
		slSelectable.add(CommonDocument.SELECT_TYPE);

		MapList objectMapList = DomainObject.getInfo(context, arrObjectId, slSelectable);

		for (int i=0;i<size;i++)
		//for (Iterator iterator = objectList.iterator(); iterator.hasNext();)
		{
			isVersionObject =false;
			mapObject = (Map) objectList.get(i);
			Map mapObject1 = (Map) objectMapList.get(i);
			String isDocument = (String)mapObject1.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS);			
			strObjectId = (String)mapObject.get(DomainConstants.SELECT_ID);
			//domainObject = DomainObject.newInstance(context,strObjectId);
			strIsversionObject = (String)mapObject.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
			if(null!=strIsversionObject)
			{
				isVersionObject=Boolean.parseBoolean(strIsversionObject);
			}
			if(null == strIsversionObject)
			{
				isVersionObject= Boolean.parseBoolean((String)mapObject1.get(CommonDocument.SELECT_IS_VERSION_OBJECT));
			}
			urlStringBuffer = new StringBuilder(256);
			lockCount = 0;
			fileCount = 0;
			files = new StringList();
			locked = new StringList();

			moveFilesToVersion = (Boolean.valueOf((String) mapObject.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();

			StringList thumbnailFileList = new StringList();
			thumbnailFileList = (StringList)mapObject1.get(CommonDocument.SELECT_FILE_FORMAT);
			int thumnailsfilecount= 0;
			if(null != thumbnailFileList){
				for(int j =0; j <thumbnailFileList.size();j++)
				{
					String format = (String)thumbnailFileList.get(j);
					if(DomainObject.FORMAT_MX_MEDIUM_IMAGE.equalsIgnoreCase(format)){
						thumnailsfilecount++;
					}

				}
			}
			if("true".equalsIgnoreCase(isDocument) && !isVersionObject)
			//if(domainObject.isKindOf(context, CommonDocument.TYPE_DOCUMENTS) && !isVersionObject)
			{
				//Added PRG:RG6:R211:DOCMGNT
				/* As while using the cut paste when column methods are called
                                params are not available so need to fetch them from DB              */
				if("true".equalsIgnoreCase(strEditableMode)){
					mapObject.putAll(mapObject1);
				 String objectType = (String) mapObject.get(CommonDocument.SELECT_TYPE);
				 isVersionObject = CommonDocument.checkVersionableType(context, objectType);
				}
				//End Added PRG:RG6:R211:DOCMGNT
				if ( moveFilesToVersion )
				{
					try
					{
						files = (StringList)mapObject.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
					}
					catch(ClassCastException cex )
					{
						files.add((String)mapObject.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION));
					}
				}
				else
				{
					try
					{
						files = (StringList)mapObject.get(CommonDocument.SELECT_FILE_NAME);

					}
					catch(ClassCastException cex )
					{
						files.add((String)mapObject.get(CommonDocument.SELECT_FILE_NAME));
					}
				}
				if ( files != null )
				{
					fileCount = files.size();
					fileCount = fileCount-thumnailsfilecount;
					if ( fileCount == 1 )
					{
						file = (String)files.get(0);
						if ( file == null || "".equals(file) || "null".equals(file) )
						{
							fileCount = 0;
						}
					}
				}

				try
				{
					locked = (StringList)mapObject.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
				}
				catch(ClassCastException cex)
				{
					locked.add((String)mapObject.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED));
				}
				if ( locked != null )
				{
					Iterator itr = locked.iterator();
					while (itr.hasNext())
					{
						lock = (String)itr.next();
						if(lock.equalsIgnoreCase("true"))
						{
							lockCount ++;
						}
					}
				}
				if ( !isVersionObject )
				{
					lock = (String)mapObject.get(CommonDocument.SELECT_LOCKED);
					if(null!=lock && lock.equalsIgnoreCase("true"))
					{
						lockCount = fileCount;
					}
				}

				if(!isprinterFriendly)
				{
					urlStringBuffer.append("<a href =\"javascript:showModalDialog('");
					if ( !isVersionObject )
					{
						urlStringBuffer.append(nonVersionableBaseURLBuf.toString());
					}
					else
					{
						urlStringBuffer.append(baseURLBuf.toString());
					}

					urlStringBuffer.append("&amp;objectId=");
					urlStringBuffer.append(strObjectId);
					urlStringBuffer.append("',730,450)\">");
				}

				urlStringBuffer.append(lockCount + "/" + fileCount);
				if(!isprinterFriendly)
				{
					urlStringBuffer.append("</a>");
				}
				vcNewLockStatus.add(urlStringBuffer.toString());

			}
			else if("true".equalsIgnoreCase(isDocument) && isVersionObject)
			{
				StringBuilder statusImageString = new StringBuilder();
				String fileLocked = null;
				String fileLocker = null;

				fileLocked = (String) mapObject1.get(CommonDocument.SELECT_LOCKED);
				fileLocker = PersonUtil.getFullName(context,(String) mapObject1.get(CommonDocument.SELECT_LOCKER));

				if ("TRUE".equalsIgnoreCase(fileLocked))
				{
					statusImageString.append("<img border=\"0\" src=\"../common/images/iconStatusLocked.gif\" alt=\"" + fileLocker + "\" title=\"" + fileLocker + "\"/>");
				}
				vcNewLockStatus.add(statusImageString.toString());
			}
			else
			{
				vcNewLockStatus.add("");
			}
		}
		return vcNewLockStatus;
	}
	/**
	 *The method is used to get the revision column data in Folder summary table. The method shows 
	 *the revisions for  controlled Folder and all the other revisionable types that can be contained inside workspace vault  
	 *excluding file objects.   
	 * @param context
	 * @param args
	 * @return Vector 
	 * @throws MatrixException
	 */
	public Vector getColumnRevisionStatusData(Context context,String[]args)throws MatrixException
	{
		Vector vcRevisionData = new Vector();
		try
		{
			Map mapProgramMap =(Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)mapProgramMap.get("objectList");
			Map paramList = (Map)mapProgramMap.get("paramList");

			boolean isprinterFriendly = false;
			if(paramList.get("reportFormat") != null)
			{
				isprinterFriendly = true;
			}

			String strObjectId =null;
			String strObjectRevision = null;

			final String SELECT_IS_WORKSPACE_VAULT  =  "type.kindof["+DomainConstants.TYPE_WORKSPACE_VAULT+"]";
			final String SELECT_IS_CONTROLLED_FOLDER  =  "type.kindof["+DomainConstants.TYPE_CONTROLLED_FOLDER+"]";
			final String SELECT_IS_DOCUMENTS  =  "type.kindof["+CommonDocument.TYPE_DOCUMENTS+"]";
			final String SELECT_IS_PRODUCTS =  new StringBuilder().append("type.kindof[").append(ProgramCentralConstants.TYPE_PRODUCTS).append(']').toString();

			StringList slRevDataSelects = new StringList(7);
			slRevDataSelects.add(DomainConstants.SELECT_ID);
			slRevDataSelects.add(SELECT_IS_WORKSPACE_VAULT);
			slRevDataSelects.add(SELECT_IS_CONTROLLED_FOLDER);
			slRevDataSelects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
			slRevDataSelects.add(DomainConstants.SELECT_REVISION);
			slRevDataSelects.add(SELECT_IS_DOCUMENTS);
			slRevDataSelects.add(SELECT_IS_PRODUCTS);
			
			BusinessObjectWithSelectList folderContentWithSelectList = null;

			StringList slRevObjList = new StringList();
			for(Iterator itrRevisionStatus = objectList.iterator(); itrRevisionStatus.hasNext();)
			{
				Map mapRevision = (Map) itrRevisionStatus.next();
				strObjectId = (String) mapRevision.get(CommonDocument.SELECT_ID);
				if(ProgramCentralUtil.isNotNullString(strObjectId))
				{
					slRevObjList.add(strObjectId);
				}
			}

			String[] strRevObjIds = new String[0];
			strRevObjIds = new String[slRevObjList.size()];
			slRevObjList.copyInto(strRevObjIds);

			folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRevObjIds,slRevDataSelects);

			// As while using the cut paste when column methods are called
			//params are not available so need to fetch them from DB
			Map mAllObjectInfo = new HashMap();
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
			{
				BusinessObjectWithSelect bows = itr.obj();
				Map mchildObjInfo  = new HashMap();
				String sChildObjId = bows.getSelectData(DomainConstants.SELECT_ID);
				String isWorkSpaceVaultType = bows.getSelectData(SELECT_IS_WORKSPACE_VAULT);
				String isConrolledFolderType = bows.getSelectData(SELECT_IS_CONTROLLED_FOLDER);
				String isDocumentsType = bows.getSelectData(SELECT_IS_DOCUMENTS);
				String isVersionedObj =  bows.getSelectData(CommonDocument.SELECT_IS_VERSION_OBJECT);
				String isProductType = bows.getSelectData(SELECT_IS_PRODUCTS);
				String sRevData = bows.getSelectData(DomainConstants.SELECT_REVISION);

				mchildObjInfo.put("sObjectId", sChildObjId);
				mchildObjInfo.put("isWorkspaceVault", isWorkSpaceVaultType);
				mchildObjInfo.put("isControlledFolder", isConrolledFolderType);
				mchildObjInfo.put("isDOCUMENTS", isDocumentsType);
				mchildObjInfo.put("isVersionedObject", isVersionedObj);
				mchildObjInfo.put("sRevision", sRevData);
				mchildObjInfo.put("isProductType", isProductType);
				mAllObjectInfo.put(sChildObjId, mchildObjInfo);
			}

			for(Iterator itrRevisionObjectList = objectList.iterator(); itrRevisionObjectList.hasNext();)
			{
				Map mapRevision = (Map) itrRevisionObjectList.next();
				strObjectId = (String) mapRevision.get(CommonDocument.SELECT_ID);		
				Map mChildObjInfo;
				if(ProgramCentralUtil.isNotNullString(strObjectId) && ((mChildObjInfo = (Map)mAllObjectInfo.get(strObjectId))!= null))
				{
					String strIsVersionObject = (String)mapRevision.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
					strObjectRevision = (String)mapRevision.get(CommonDocument.SELECT_REVISION);

					String isWorkSpaceVaultType   = (String)mChildObjInfo.get("isWorkspaceVault");
					String isControlledFolderType = (String)mChildObjInfo.get("isControlledFolder");
					String isDocumentsType        = (String)mChildObjInfo.get("isDOCUMENTS");
					String isProductType = (String) mChildObjInfo.get("isProductType");

					boolean isVersionObject = false;
					if(ProgramCentralUtil.isNullString(strIsVersionObject))
					{
						strIsVersionObject = (String)mChildObjInfo.get("isVersionedObject");
					}

					if(ProgramCentralUtil.isNotNullString(strIsVersionObject))
					{
						isVersionObject = Boolean.parseBoolean(strIsVersionObject);
					}

					boolean isControlledFolder = "TRUE".equalsIgnoreCase(isControlledFolderType);
					boolean isDocument = "TRUE".equalsIgnoreCase(isDocumentsType);
					boolean isProduct = "TRUE".equalsIgnoreCase(isProductType);
					StringBuilder urlStringBuffer = new StringBuilder();
					//PRG:RG6:R212:13-5-2011:revision for controlled folder and document object only	 
					if(isControlledFolder || (isDocument && !isVersionObject) || isProduct)
					{
						boolean isLinkAvailable = true;
						StringBuilder objectRevisionURL = new StringBuilder();
						if(isControlledFolder)
						{
							objectRevisionURL.append("emxTable.jsp?program=emxControlledFolder:getTableControlledFolderRevisionsData&amp;table=PMCControlledFolderRevisionsSummary&amp;sortColumnName=Name&amp;sortDirection=ascending&amp;header=emxProgramCentral.Common.ControlledFolders&amp;HelpMarker=emxhelpcontrolledfolderrevisionssummary&amp;Export=false&amp;suiteKey=ProgramCentral");
						}
						else if(isDocument)
						{
							objectRevisionURL.append("emxTable.jsp?program=emxCommonDocumentUI:getRevisions&amp;popup=true&amp;table=APPDocumentRevisions&amp;header=emxComponents.Common.RevisionsPageHeading&amp;HelpMarker=emxhelpdocumentfilerevisions&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;suiteKey=Components");
						}
						else if (isProduct) 
                                                {
							objectRevisionURL
									.append("emxTable.jsp?program=emxProduct:getRevisionProducts&amp;popup=true&amp;table=APPRevisionsList&amp;header=emxProduct.Heading.Revisions&amp;HelpMarker=emxhelprevisionlist&amp;suiteKey=ProductLine");
						}
						else {
							isLinkAvailable = false;
						}

						if(!isprinterFriendly && isLinkAvailable)
						{
							urlStringBuffer.append("<a ");
							urlStringBuffer.append(" href =\"javascript:showModalDialog('");
							urlStringBuffer.append(objectRevisionURL.toString());
							urlStringBuffer.append("&amp;objectId=");
							urlStringBuffer.append(strObjectId);
							urlStringBuffer.append("',730,450)\">");
						}
						//Modified PRG:RG6:R211:DOCMGNT
						// As while using the cut paste when column methods are called
						//params are not available so need to fetch them from DB              
						if(ProgramCentralUtil.isNotNullString(strObjectRevision))
						{
							urlStringBuffer.append(strObjectRevision);
						}
						else
						{
							strObjectRevision = (String)mChildObjInfo.get("sRevision");
							if(ProgramCentralUtil.isNotNullString(strObjectRevision))
							{
								urlStringBuffer.append(strObjectRevision);
							}
						}
						if(!isprinterFriendly && isLinkAvailable)
						{
							urlStringBuffer.append("</a>");
						}
						vcRevisionData.add( urlStringBuffer.toString()); 
					}
					else
					{
						vcRevisionData.add("");
					} 
				}
			}
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return vcRevisionData;
	}
	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isFolderContentViewTable(Context context,String[]args) throws Exception
	{
		Map mapProgrammap = (Map) JPO.unpackArgs(args);
		String strObjectId =(String) mapProgrammap.get("objectId");
		String strSelectedTable =(String) mapProgrammap.get("selectedTable");

		if("PMCFolderMemberAccessViewTable".equals(strSelectedTable) || "PMCFolderRoleAccessViewTable".equals(strSelectedTable))
		{
			return false;
		}

		return true;

	}
	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean isProjectSpaceVaultSummaryTable(Context context,String[]args) throws Exception
	{
		Map mapProgrammap = (Map) JPO.unpackArgs(args);
		String strObjectId =(String) mapProgrammap.get("objectId");
		DomainObject domainObject = DomainObject.newInstance(context,strObjectId);
		final String SELECT_PARENT_ID="to["+DomainConstants.RELATIONSHIP_PROJECT_VAULTS+"].from.id";

		// [MODIFIED::PRG:RG6:Jan 17, 2011:IR-085202V6R2012:R211:below line]
		if (domainObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE) || domainObject.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT))
		{
			return true;
		}
		else if(domainObject.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT))
		{
			WorkspaceVault vault = new WorkspaceVault(strObjectId);
			StringList list = new StringList();
			list.add(DomainConstants.SELECT_ID);

			Map mapTopLevelVault =vault.getTopLevelVault(context,list);
			String strTopId= (String) mapTopLevelVault.get(DomainConstants.SELECT_ID);

			DomainObject domainObject2 = DomainObject.newInstance(context,strTopId);

			String strTempId= domainObject2.getInfo(context, SELECT_PARENT_ID);
			DomainObject domainObject3 = DomainObject.newInstance(context,strTempId);

			if(!domainObject3.isKindOf(context, DomainConstants.TYPE_PROJECT_TEMPLATE))
			{
				return true;
			}

		}

		return false;
	}
	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */

	public MapList getProjectMemberDynamicColumn(Context context,String[] args)throws Exception
	{
		Map mapProgramMap =(Map)JPO.unpackArgs(args);
		Map mapRequestMap=(Map)mapProgramMap.get("requestMap");

		String strObjectId = (String) mapRequestMap.get("objectId");
		DomainObject domainObject = DomainObject.newInstance(context,strObjectId);

		final String SELECT_PROJECT_ID="to["+DomainConstants.RELATIONSHIP_PROJECT_VAULTS+"].from.id";

		if(domainObject.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT)|| domainObject.isKindOf(context, DomainConstants.TYPE_CONTROLLED_FOLDER))
		{
			WorkspaceVault vault = new WorkspaceVault(strObjectId);
			StringList slSelects = new StringList();
			slSelects.add(DomainConstants.SELECT_ID);
			slSelects.add(DomainConstants.SELECT_NAME);

			Map mapTopLevelVault = vault.getTopLevelVault(context, slSelects);
			String strVaultId= (String)mapTopLevelVault.get(DomainConstants.SELECT_ID);

			DomainObject dmoTopLevelVault = DomainObject.newInstance(context,strVaultId);

			String strProjectId = dmoTopLevelVault.getInfo(context, SELECT_PROJECT_ID);
			mapProgramMap.put("objectId", strProjectId);
		}
		else
		{
			mapProgramMap.put("objectId", strObjectId);
		}

		args= JPO.packArgs(mapProgramMap);

		MapList mlColumns = new MapList();
		//${CLASS:emxTask} task = new ${CLASS:emxTask}(context,args);

		emxProjectMember_mxJPO member = new emxProjectMember_mxJPO(context,args);
		MapList mlMembers = member.getMembers(context, args);

		DomainObject personDomainObject = DomainObject.newInstance(context);
		String strMemberId = null;
		Map mapMember = null;
		String strType = null;
		for(int i = 0; i < mlMembers.size(); i++)
		{
			HashMap mapColumn = new HashMap();
			mapMember= (Map)mlMembers.get(i);

			strType =(String) mapMember.get(DomainConstants.SELECT_TYPE);
			mapColumn.put("label", (String) mapMember.get(Person.SELECT_NAME));
			strMemberId = (String) mapMember.get(DomainConstants.SELECT_ID);

			if(DomainConstants.TYPE_PERSON.equals(strType))
			{
				 if(strMemberId.contains("personid_")){
					 strMemberId = strMemberId.replace("personid_","");
	                } 
				 personDomainObject.setId(strMemberId);
				mapColumn.put("name", personDomainObject.getInfo(context, DomainConstants.SELECT_NAME));
			}
			else
			{
				mapColumn.put("name",strMemberId);
			}

			HashMap mapColumnSettings = new HashMap();
			mapColumnSettings.put("Registered Suite","ProgramCentral");
			mapColumnSettings.put("Column Type","program");

			mapColumnSettings.put("program","emxProjectFolder");
			mapColumnSettings.put("function","getColumnProjectMemberVaultAccessData");

			mapColumnSettings.put("Range Function","getColumnProjectVaultAccessRange");
			mapColumnSettings.put("Range Program","emxProjectFolder");

			mapColumnSettings.put("Input Type","combobox");

			mapColumnSettings.put("Update Function","updateColumnProjectMemberVaultAccessData");
			mapColumnSettings.put("Update Program","emxProjectFolder");

			if("Role".equals(strType))
			{
				mapColumnSettings.put("Group Header","emxProgramCentral.GroupHeader.ProjectRole");
			}
			else if("Group".equals(strType))
			{
				mapColumnSettings.put("Group Header","emxProgramCentral.GroupHeader.ProjectGroup");
			}
			else
			{
				mapColumnSettings.put("Group Header","emxProgramCentral.GroupHeader.ProjectMembers");
			}

			mapColumnSettings.put("Editable","true");
			//Added:28-Mar-2011:MS9: R210.HF4 PRG:
			mapColumnSettings.put("Edit Access Function","editAccessToFolderRows");
			mapColumnSettings.put("Edit Access Program","emxProjectFolder");
			//End:28-Mar-2011:MS9: R210.HF4 PRG:

			//Added:vf2:16-Dec-10:R211:IR-086706
			mapColumnSettings.put("Width","150");
			//End:vf2:16-Dec-10:R211:IR-086706
			mapColumn.put("settings", mapColumnSettings);
			mlColumns.add(mapColumn);
		}
		return mlColumns;
	}

	/**
	 * Updates member accesses for project folder in project folder member access view.
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args
	 * @param RequestMap containing the object information.
	 * @param New Value new accesses to be updated
	 * @return void
	 * @throws MatrixException
	 */
	public void updateColumnProjectMemberVaultAccessData(Context context, String[] args) throws MatrixException
	{
		try {
			Map mapProgrammap = (Map) JPO.unpackArgs(args);
			Map mapRequest = (Map)mapProgrammap.get("requestMap");

			String strObjectid= (String)mapRequest.get("objectId");
			DomainObject ProjectDomainObject = DomainObject.newInstance(context,strObjectid);
			String strProjectOwner= ProjectDomainObject.getInfo(context, DomainConstants.SELECT_OWNER);
			Map mapUserPermission = new HashMap();

			Map mpParamMap= (Map)mapProgrammap.get("paramMap");
			Map mapColumnMap= (Map)mapProgrammap.get("columnMap");
			String strNewValue = (String)mpParamMap.get("New Value");

			WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
			String strObjectId  = (String)mpParamMap.get("objectId");
			workspaceVault.setId(strObjectId);

			Map map= workspaceVault.getUserPermissions(context);
			String strContextUser = context.getUser();
			String strLanguage = context.getSession().getLanguage();

			if(map.keySet().contains(strContextUser))
			{
				String ContextUserAccess= (String)map.get(strContextUser);
				if(ProgramCentralConstants.VAULT_ACCESS_ADD_REMOVE.equals(ContextUserAccess))
				{
					String strPerson=(String) mapColumnMap.get(DomainConstants.SELECT_NAME);

					if(!strProjectOwner.equals(strPerson))
					{
						mapUserPermission.put(strPerson, strNewValue);
					}
					else
					{
						String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.ProjectFolder.NoOwnerEditAccess", strLanguage);
						throw new MatrixException(strTxtNotice);
					}
					workspaceVault.setUserPermissions(context, mapUserPermission);
				}
				else
				{
					String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.ProjectFolder.NoEditAccess", strLanguage);
					strTxtNotice = strTxtNotice +" : "+workspaceVault.getInfo(context, DomainConstants.SELECT_NAME) ;
					throw new MatrixException(strTxtNotice);
				}
			}
			else
			{
				String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.ProjectFolder.NoInAccessList", strLanguage);				
				strTxtNotice = strTxtNotice +" : "+workspaceVault.getInfo(context, DomainConstants.SELECT_NAME) ;
				throw new MatrixException(strTxtNotice);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new MatrixException(e);
		}

	}

	/**
	 * Gets the folder access attributes range values.
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args
	 * @return Map containing the access range list.
	 * @throws MatrixException
	 */
	public Map getColumnProjectVaultAccessRange(Context context,String[]args) throws MatrixException
	{
		WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
		List lstAccessChoices = (List)workspaceVault.getUserPermissionChoices();
		StringList accessChoices = new StringList();
		accessChoices.addAll(lstAccessChoices);
		ListIterator  listItr = accessChoices.listIterator();
		Map mapUserPermissionChoice=new HashMap();
		String strLanguage = context.getSession().getLanguage();
		String strChoice = null;
		String strDisplayChoices = null;
		StringList slDisplayChoices = new StringList();

		for (Iterator iterator = accessChoices.iterator(); iterator.hasNext();)
		{
			strChoice = (String) iterator.next();
			strDisplayChoices =  i18nNow.getRangeI18NString(PropertyUtil.getSchemaProperty(context,"attribute_FolderAccess"),strChoice,strLanguage);
			slDisplayChoices.add(strDisplayChoices);
		}

		mapUserPermissionChoice.put("field_choices",  accessChoices);
		mapUserPermissionChoice.put("field_display_choices",slDisplayChoices);
		return mapUserPermissionChoice;
	}

	/**
	 * Retrieves Member accesses for project folder in project folder member access view.
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args
	 * @param ObjectList List of the objects rendered on the ui for role access view 
	 * @return List of the accesses retrieved for dynamic columns (Project Members) for folder objects.
	 * @throws Exception
	 */
	public Vector getColumnProjectMemberVaultAccessData(Context context,String[] args)throws Exception
	{
		Map mapProgramMap =(Map)JPO.unpackArgs(args);
		Vector vcProjectMemberVaultAccess = new Vector();
		MapList mlObjectList = (MapList)mapProgramMap.get("objectList");
		Map mapColumnMap = (Map)mapProgramMap.get("columnMap");
		String strProjectMemberName = (String) mapColumnMap.get("name");
		String strLanguage = context.getSession().getLanguage();
		Map mapObject= null;
		String strObjectId = null;
		WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
		MapList slVaultGrantees = null;
		String strDefaultUserAccess = null;

		for (Iterator iterator = mlObjectList.iterator(); iterator.hasNext();)
		{
			mapObject = (Map) iterator.next();
			strObjectId = (String) mapObject.get(DomainConstants.SELECT_ID);
			workspaceVault.setId(strObjectId);
			workspaceVault.setContentRelationshipType(workspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);
			HashMap memberMap =(HashMap) workspaceVault.getUserPermissions(context);
			slVaultGrantees = DomainAccess.getAccessSummaryList(context, strObjectId);			

			strDefaultUserAccess = workspaceVault.getAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS);

			if (null != slVaultGrantees && ! slVaultGrantees.isEmpty())
			{
                String strAccess = (String) memberMap.get(strProjectMemberName);
                if (null != strAccess && ! strAccess.isEmpty())
                {
                    String strDisplayAccess = i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS, strAccess, strLanguage);
                    vcProjectMemberVaultAccess.add(strDisplayAccess);
                }
                else
                {
                    String strDisplayAccessNone = i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS,strDefaultUserAccess, strLanguage);
                    vcProjectMemberVaultAccess.add(strDisplayAccessNone);
                }
			}
			else
			{
				String strDisplayAccessNone = i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS,strDefaultUserAccess, strLanguage);
				vcProjectMemberVaultAccess.add(strDisplayAccessNone);

			}

		}
		return vcProjectMemberVaultAccess;
	}

	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public MapList getProjectRoleDynamicColumn(Context context,String[] args)throws Exception
	{
		MapList mlColumns = new MapList();
		emxTask_mxJPO task = new emxTask_mxJPO(context,args);

		HashMap mapProjectRoles =task.getProjectRoleRange(context, args);
		StringList slProjectRolesTranslated= (StringList) mapProjectRoles.get("field_display_choices");
		StringList slProjectRoles= (StringList) mapProjectRoles.get("field_choices");

		for(int i = 1; i < slProjectRolesTranslated.size(); i++)
		{
			HashMap mapColumn = new HashMap();
			mapColumn.put("label", (String)slProjectRolesTranslated.get(i));
			mapColumn.put("name",(String)slProjectRoles.get(i));

			HashMap mapColumnSettings = new HashMap();
			mapColumnSettings.put("Registered Suite","ProgramCentral");
			mapColumnSettings.put("Column Type","program");

			mapColumnSettings.put("program","emxProjectFolder");
			mapColumnSettings.put("function","getColumnProjectRoleVaultAccessData");

			mapColumnSettings.put("Range Function","getColumnProjectVaultAccessRange");
			mapColumnSettings.put("Range Program","emxProjectFolder");

			mapColumnSettings.put("Input Type","combobox");

			mapColumnSettings.put("Update Function","updateColumnProjectRoleVaultAccessData");
			mapColumnSettings.put("Update Program","emxProjectFolder");

			mapColumnSettings.put("Group Header","emxProgramCentral.GroupHeader.ProjectRoleAccess");
			mapColumnSettings.put("Editable","true");

			//Added:28-Mar-2011:MS9: R210.HF4 PRG:
			mapColumnSettings.put("Edit Access Function","editAccessToFolderRows");
			mapColumnSettings.put("Edit Access Program","emxProjectFolder");
			//Added:28-Mar-2011:MS9: R210.HF4 PRG:

			//Added:vf2:16-Dec-10:R211:IR-086706
			mapColumnSettings.put("Width","150");
			//End:vf2:16-Dec-10:R211:IR-086706
			mapColumn.put("settings", mapColumnSettings);
			mlColumns.add(mapColumn);
		}

		return mlColumns;
	}

	/**
	 * Retrieves Role accesses for project folder in project folder role access view.
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args
	 * @param ObjectList List of the objects rendered on the ui for role access view 
	 * @return List of the accesses retrieved for dynamic columns (Roles) for folder objects.
	 * @throws MatrixException
	 */
	public Vector getColumnProjectRoleVaultAccessData(Context context,String[] args)throws MatrixException
	{
		Vector vcProjectRoleVaultAccess = new Vector();
		try
		{
			Map mapProgramMap =(Map)JPO.unpackArgs(args);
			MapList mlObjectList = (MapList)mapProgramMap.get("objectList");
			Map mapColumnMap = (Map)mapProgramMap.get("columnMap");
			String strProjectRole=(String) mapColumnMap.get("name");
			String strLanguage = context.getSession().getLanguage();
			String strResourceFileid = "emxProgramCentralStringResource";

			int objNoOfRows = mlObjectList.size();
			String []arrObjIds = new String[objNoOfRows];
			for(int i=0; i<objNoOfRows; i++)
			{
				Map mapObj = (Map)mlObjectList.get(i);
				String strObjectId = (String) mapObj.get(DomainConstants.SELECT_ID);
				arrObjIds[i] = strObjectId; 
			}

			BusinessObjectWithSelectList folderContentWithSelectList = null;
			BusinessObjectWithSelect bows = null;

			MapList mlRetList = new MapList();

			StringList slObjectSelects = new StringList();
			slObjectSelects.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);
			slObjectSelects.addElement(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);


			folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,arrObjIds,slObjectSelects);

			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
			{
				bows = itr.obj();
				String strProjectRoleAccess = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);
				String strDeafaultFolderAccess = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
				ProjectRoleVaultAccess projectRoleVaultAccess = new ProjectRoleVaultAccess(context,strProjectRoleAccess);
				String strAccess = projectRoleVaultAccess.getAccess(strProjectRole);

				if(!ProgramCentralConstants.VAULT_ACCESS_NONE.equals(strAccess) && (ProgramCentralUtil.isNotNullString(strAccess)))
				{
					String strDisplayAccess = i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS,strAccess, strLanguage);
					vcProjectRoleVaultAccess.add(strDisplayAccess);
				}
				else
				{
					String strDisplayAccess  = "";
					if(ProgramCentralUtil.isNotNullString(strDeafaultFolderAccess))
					{
						strDisplayAccess = i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS,strDeafaultFolderAccess, strLanguage); 
					}
					vcProjectRoleVaultAccess.add(strDisplayAccess);
				}
			}
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}

		return vcProjectRoleVaultAccess;
	}

	/**
	 * Updates Role accesses for project folder in project folder role access view.
	 * @param context   the eMatrix <code>Context</code> object
	 * @param args
	 * @param RequestMap containing the object information.
	 * @param New Value new accesses to be updated
	 * @return void
	 * @throws MatrixException
	 */
	public void updateColumnProjectRoleVaultAccessData(Context context, String[] args) throws MatrixException
	{
		try {
			Map mapProgrammap = (Map) JPO.unpackArgs(args);

			Map mpParamMap= (Map)mapProgrammap.get("paramMap");
			Map mapColumnMap= (Map)mapProgrammap.get("columnMap");
			Map mapRequestMap= (Map)mapProgrammap.get("requestMap");
			String strProjectId= (String)mapRequestMap.get("objectId");
			String strNewAccess = (String)mpParamMap.get("New Value");
			String strObjectId = (String)mpParamMap.get("objectId");
			String strProjectRole = (String) mapColumnMap.get("name");
			WorkspaceVault workspaceVault = (WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
			workspaceVault.setId(strObjectId);

			String strRoleVaultAccess= workspaceVault.getAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS);

			String strDefaultAccess= workspaceVault.getAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS);
			//ProjectRoleVaultAccess roleVaultAccess = ProjectRoleVaultAccess.getInstance(context, strDefaultAccess);
			ProjectRoleVaultAccess roleVaultAccess = new ProjectRoleVaultAccess(context, strRoleVaultAccess);

			String strRolesCurrentAccess= roleVaultAccess.getAccess(strProjectRole);
			ProjectSpace projectSpace = new ProjectSpace(strProjectId);

			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_NAME);
			objectSelects.add(DomainConstants.ATTRIBUTE_PROJECT_ROLE);
			StringList relationshipSelects = new StringList();
			relationshipSelects.add("attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]");
			// String relationshipWhere = "attribute[Project Role]"+"smatch\""+strProjectRole+"\"";
			String relationshipWhere = "attribute["+DomainConstants.ATTRIBUTE_PROJECT_ROLE+"]"+"smatch\""+strProjectRole+"\"";
			String objectWhere =null;

			MapList mlMemberList =  projectSpace.getRelatedObjects(context,
					DomainRelationship.RELATIONSHIP_MEMBER,
					DomainConstants.TYPE_PERSON,
					objectSelects,
					relationshipSelects,
					false,
					true,
					(short)0,
					objectWhere,
					relationshipWhere,
					0);

			Map mapMember = null;
			String strMemberName = null;
			Map mapUserPermission = new HashMap();

			Map mapAccess= workspaceVault.getUserPermissions(context);
			String strMemberCurrentAccess = null;

			for (Iterator iterator = mlMemberList.iterator(); iterator.hasNext();)
			{
				mapMember = (Map) iterator.next();
				strMemberName = (String) mapMember.get(DomainConstants.SELECT_NAME);
				strMemberCurrentAccess = (String)mapAccess.get(strMemberName);
				if(strRolesCurrentAccess.equals(strMemberCurrentAccess))
				{
					mapUserPermission.put(strMemberName, strNewAccess);
				}
				else if(null== strMemberCurrentAccess || "".equals(strMemberCurrentAccess) || "null".equals(strMemberCurrentAccess))
				{
					mapUserPermission.put(strMemberName, strNewAccess);
				}

			}

			workspaceVault.setUserPermissions(context, mapUserPermission);
			roleVaultAccess.setAccess(strProjectRole,strNewAccess);
			workspaceVault.setAttributeValue(context, ProgramCentralConstants.ATTRIBUTE_PROJECT_ROLE_VAULT_ACCESS, roleVaultAccess.toXML());
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}
	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int  triggerCheckProjectRoleVaultAccessModify(Context context, String[] args) throws Exception
	{
		String strWorkspaceVaultId = args[0];
		WorkspaceVault vault = new WorkspaceVault(strWorkspaceVaultId);
		Map map= vault.getUserPermissions(context);
		String strContextUser = context.getUser();
		String strLanguage = context.getSession().getLanguage();

		if(map.keySet().contains(strContextUser))
		{
			String ContextUserAccess= (String)map.get(strContextUser);
			if(ProgramCentralConstants.VAULT_ACCESS_ADD_REMOVE.equals(ContextUserAccess)
					|| "Full".equalsIgnoreCase(ContextUserAccess)){
				return TRIGGER_SUCCESS;
			}
			else
			{
				String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.ProjectFolder.NoEditAccess", strLanguage);
				strTxtNotice = strTxtNotice +" : "+vault.getInfo(context, DomainConstants.SELECT_NAME) ;
				throw new MatrixException(strTxtNotice);
			}
		}
		else
		{
			return TRIGGER_FAILURE;
		}

	}
	/**
	 * If it returns
	 *a. =0: strAccess1 and strAccess2 are same
	 *b. >0: strAccess1 preside over strAccess2
	 * @param strAccess1
	 * @param atrAccess2
	 * @return int
	 */
	public static int compareUesrVaultAccess(String strAccess1,String strAccess2) throws MatrixException
	{
		String[] strVaultAccessPrecedence= ProgramCentralConstants.VAULT_ACCESS_PRECEDENCE;
		StringList slVaultAccessPrecedence = new StringList(strVaultAccessPrecedence);

		if(! slVaultAccessPrecedence.contains(strAccess1) || ! slVaultAccessPrecedence.contains(strAccess2))
		{
			throw new MatrixException("Access argument is Invalid!");
		}
		else
		{
			return  (slVaultAccessPrecedence.indexOf(strAccess1)) - (slVaultAccessPrecedence.indexOf(strAccess2));
		}

	}


	/**
	 * This method find the actual object id for file or document and delegate the call to
	 * getVersions or getFileVersions for getting the  versions of the file or files contained
	 * in document based on type (either file or document).
	 *
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments containing
	 *        programMap - program map
	 *        emxTableRowId - to get the actual object id
	 * @return Object returns  the list of file versions for file object
	 *         or document object.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public Object getVersionForFileRMB(Context context, String[] args) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);

		String  strDocRow = (String)programMap.get("emxTableRowId");
		StringList strDocuments = FrameworkUtil.split(strDocRow, "|");

		String strId = null;
		if (strDocuments.size() >= 2) {
			strId = (String) strDocuments .get(1);
			if (strId != null && !"".equals(strId.trim())) {
				programMap.put("objectId", strId);
				DomainObject dObj = DomainObject.newInstance(context);
				dObj.setId(strId);

				if(dObj.isKindOf(context, DomainConstants.TYPE_DOCUMENT)){
					String strIsVersionObject = dObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT);
					if("true".equalsIgnoreCase(strIsVersionObject)){
						return new emxCommonFileUI_mxJPO(context,args).getVersions(context,args);

					}else{
						return new emxCommonFileUI_mxJPO(context,args).getFileVersions(context,args);

					}
				}
			}
		}
		return new MapList();

	}

	// Added 21-June-2010:rg6:PRG:IR-058619V6R2011x
	/**
	 * getDefaultUSerAccessRange - This Method returns Map containing i18n Range values for attribute
	 * 'Default User Access'.
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * @return Map of i18n Range values for "Default User Access" Attribute
	 * @throws Exception if the operation fails
	 * @since PMC R210
	 */
	public Map getDefaultUSerAccessRange(Context context, String[] args)
	throws Exception
	{
		try {
			String sLang = context.getSession().getLanguage();
			AttributeType attrDefaultUserAccess = new AttributeType(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS);
			attrDefaultUserAccess.open(context);
			StringList strList = attrDefaultUserAccess.getChoices(context);
			attrDefaultUserAccess.close(context);
			StringList slDefaultUserAccessTranslated = new StringList();
			HashMap map = new HashMap();

			slDefaultUserAccessTranslated=i18nNow.getAttrRangeI18NStringList(ProgramCentralConstants.ATTRIBUTE_DEFAULT_USER_ACCESS, strList, sLang);
			map.put("field_choices", strList);
			map.put("field_display_choices", slDefaultUserAccessTranslated);

			return  map;
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}
	// End 21-June-2010:rg6:PRG:IR-058619V6R2011x

	//Added 14-July-2010:rg6:PRGRG6R210
	/**
	 * This method called from the RMB command of the folder
	 * to find the actual object id for document and delegate the call to getRevisions for getting the
	 * Revisions of the document.
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * 					emxTableRowId : row id.
	 * @return MapList list contaning the revisiosn of the document.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRevisionsForDocumentRMB(Context context, String[] args) throws Exception {

		Map programMap = (Map) JPO.unpackArgs(args);
		String  strDocRow= (String)programMap.get("emxTableRowId");
		StringList strDocuments=new StringList();
		strDocuments= FrameworkUtil.split(strDocRow, "|");
		String strDocId = null;  // actual doucment id

		for (int i = 0; i < strDocuments.size(); i++)
		{
			strDocId = (String) strDocuments .get(1);
		}

		if(strDocId!=null){
			programMap.put("objectId", strDocId);
			programMap.put("header", "MyHeader");
		}

		return new emxCommonDocumentUIBase_mxJPO(context,args).getRevisions(context, args);
	}

	/**
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PreProcessCallable
	public HashMap preProcessCheckForEdit (Context context, String[] args) throws Exception
	{
		// unpack the incoming arguments
		HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) inputMap.get("paramMap");
		HashMap tableData = (HashMap) inputMap.get("tableData");
		MapList objectList = (MapList) tableData.get("ObjectList");
		String strObjectId = (String) paramMap.get("objectId");
		HashMap returnMap = null;

/* FZS IR-269416
		DomainObject dom = DomainObject.newInstance(context, strObjectId);
		

		String roleProjectUser = ProgramCentralConstants.ROLE_PROJECT_USER;
		boolean hasEditAccess = PersonUtil.hasAssignment(context, roleProjectUser);
				
		if(hasEditAccess)
		{
			returnMap = new HashMap(2);
			returnMap.put("Action","Continue");
			returnMap.put("ObjectList",objectList);
		}
		else
		{
			returnMap = new HashMap(3);
			returnMap.put("Action","Stop");
			returnMap.put("Message","emxProgramCentral.WBS.NoEdit");
			returnMap.put("ObjectList",objectList);
		}
*/

        returnMap = new HashMap(2);
        returnMap.put("Action","Continue");
        returnMap.put("ObjectList",objectList);

		return returnMap;

	}

	//Added 30-July-2010:rg6:IR_064799V6R2011x
	/**
	 * This method called from the getDynamicCommandsForRMBMenu and as Access Function  from
	 * PMCWorkspaceVaultAddContnetActions and PMCWorkspaceVaultColumnRMBSubMenu, in order to
	 * check weather to generate the RMB or not depending on state of parent controlled folder
	 * (whcih would be superceded or not).
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * 					requestMap : holds, uiType   - structure browser or flat table
	 * 										objectId - parent id of tree in case of SB.
	 * @return boolean  returns true or false depending on state of controlled folder.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	public  boolean isControlledFolderSuperceded(Context context,String[] args) throws Exception{

		Map inputMap;
		boolean isParentSuperceded = false;

		try {
			inputMap = (HashMap)JPO.unpackArgs(args);
			Map requestMap = (Map)inputMap.get("requestMap");
			String strUiType = "";
			String strParentID = "";
			if(requestMap == null){ // if called from access function of menu
				strUiType = (String)inputMap.get("uiType");
				strParentID = (String)inputMap.get("objectId");
			}else{
				strUiType = (String)requestMap.get("uiType");
				strParentID = (String)requestMap.get("objectId");
			}
			if(strUiType != null && "structureBrowser".equalsIgnoreCase(strUiType)){

				if(strParentID != null){
					DomainObject dParentObj = DomainObject.newInstance(context,strParentID);
					if(dParentObj.isKindOf(context, DomainConstants.TYPE_CONTROLLED_FOLDER)){
						String strCurrState = dParentObj.getCurrentState(context).getName();
						if("Superceded".equalsIgnoreCase(strCurrState)){
							isParentSuperceded =true;
						}
					}
				}
			}

		}catch(Exception e){
			throw e;
		}
		return !(isParentSuperceded);
	}
	//Added 22-July-2010:rg6:PRGRG6R210
	/**
	 * This method called from the PMCFolderColumnRMBMenu as dynamic command function for generating
	 * commands in the  RMB menu of the name column in the folder structure for DOCUMENTS AND FILE type.
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds arguments
	 * 					requestMap : holds the RMBID - Id of the folder/document/file
	 * @return Map  contaning Maplist which itself contain the  Map objects of the commands which are created dynamically.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	public Map getDynamicCommandsForRMBMenu(Context context, String[] args) throws Exception
	{
		HashMap resultMap = new HashMap();
		try {

			MapList categoryMapList = new MapList();
			Map inputMap = (HashMap)JPO.unpackArgs(args);
			Map requestMap = (Map)inputMap.get("requestMap");
			UIMenu uiMenu = new UIMenu();

			String strRmbId = (String)requestMap.get("RMBID");  // get the id of the folder/document/file on which click occured
			if(strRmbId == null){
				throw new IllegalArgumentException("RMB Id is Null");
			}

			DomainObject dObj = DomainObject.newInstance(context,strRmbId);
			String strIsVersionObject = dObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT);
			boolean isVersionObject  = Boolean.parseBoolean(strIsVersionObject);
			//Added 30-July-2010:rg6:IR_064799V6R2011x
			boolean isParentSuperceded = ! (isControlledFolderSuperceded(context, args));

			// check if it type of dcoument or file
			if(dObj.isKindOf(context,CommonDocument.TYPE_DOCUMENTS) && ! isVersionObject){ // document type only
				//Code for dynamically creating command "PMCContentAddToFolderActionLink"
				if(!isParentSuperceded){
					Map hmCmdFolderCreate = new HashMap();
					hmCmdFolderCreate = createDynamicCommand(context,"PMCContentAddToFolderActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderCreate);

					//Code for dynamically creating command "PMCDocumentCheckInActionLink"
					Map hmCmdFolderClone = new HashMap();
					hmCmdFolderClone = createDynamicCommand(context,"PMCDocumentCheckInActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderClone);

					//Code for dynamically creating command "PMCDocumentCheckoutActionLink"
					Map hmCmdFolderAccess = new HashMap();
					hmCmdFolderAccess = createDynamicCommand(context,"PMCDocumentCheckoutActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderAccess);

					//Code for dynamically creating command "APPCommonDocumentDownloadActionLink"
					Map hmCmdFolderSubscription = new HashMap();
					hmCmdFolderSubscription = createDynamicCommand(context,"PMCDocumentDownloadActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderSubscription);

					// [ADDED::PRG:rg6:Dec 15, 2010:IR-083798V6R2012 :R211::Start]
					//Code for dynamically creating command "PMCWorkspaceVaultDocumentSubscription"
					Map hmCmdBookmarksCreate = new HashMap();
					hmCmdBookmarksCreate = createDynamicCommand(context,"PMCWorkspaceVaultDocumentSubscription",uiMenu,1); //Modified PRG:RG6:R212:Jun-10-2011:IR-104345V6R2012x
					categoryMapList.add(hmCmdBookmarksCreate);
					// [ADDED::PRG:rg6:Dec 15, 2010:IR-083798V6R2012 :R211::End]

					//Code for dynamically creating command "PMCDocumentFileVersions"
					Map hmCmdCFRelease = new HashMap();
					hmCmdCFRelease = createDynamicCommand(context,"PMCDocumentFileVersions",uiMenu,0);
					categoryMapList.add(hmCmdCFRelease);

					//Code for dynamically creating command "PMCDocumentRevisions"
					Map hmCmdCFRevise = new HashMap();
					hmCmdCFRevise = createDynamicCommand(context,"PMCDocumentRevisions",uiMenu,0);
					categoryMapList.add(hmCmdCFRevise);

					//Code for dynamically creating command "PMCDocumentEditActionLink"
					Map hmCmdDocEdit = new HashMap();
					hmCmdDocEdit = createDynamicCommand(context,"PMCDocumentEditActionLink",uiMenu,1);
					categoryMapList.add(hmCmdDocEdit);

					//Code for dynamically creating command "PMCDocumentRemove"
					Map hmCmdDocRemove = new HashMap();
					hmCmdDocRemove = createDynamicCommand(context,"PMCDocumentRemove",uiMenu,0);
					Map mapSetting = (Map)hmCmdDocRemove.get("settings");
					String strConfMsg = (String)mapSetting.get("Confirm Message");
					String strNewconfMsg = null;

					if(strConfMsg != null){
						strNewconfMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								strConfMsg, context.getSession().getLanguage());
						mapSetting.put("Confirm Message", strNewconfMsg);
					}
					categoryMapList.add(hmCmdDocRemove);

				}else{ //if controlled folder in superceded state

					Map hmCmdFolderCreate = new HashMap();
					hmCmdFolderCreate = createDynamicCommand(context,"PMCContentAddToFolderActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderCreate);

					//Code for dynamically creating command "PMCDocumentCheckoutActionLink"
					Map hmCmdFolderAccess = new HashMap();
					hmCmdFolderAccess = createDynamicCommand(context,"PMCDocumentCheckoutActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderAccess);

					//Code for dynamically creating command "APPCommonDocumentDownloadActionLink"
					Map hmCmdFolderSubscription = new HashMap();
					hmCmdFolderSubscription = createDynamicCommand(context,"APPCommonDocumentDownloadActionLink",uiMenu,0);
					categoryMapList.add(hmCmdFolderSubscription);

					//Code for dynamically creating command "PMCDocumentFileVersions"
					Map hmCmdCFRelease = new HashMap();
					hmCmdCFRelease = createDynamicCommand(context,"PMCDocumentFileVersions",uiMenu,0);
					categoryMapList.add(hmCmdCFRelease);
				}

			}

			if(isVersionObject){   // of type file only
				//Code for dynamically creating command "PMCDocumentCheckoutActionLink"
				Map hmCmdFileCheckout = new HashMap();
				hmCmdFileCheckout = createDynamicCommand(context,"PMCDocumentCheckoutActionLink",uiMenu,0);
				categoryMapList.add(hmCmdFileCheckout);

				//Code for dynamically creating command "APPCommonDocumentDownloadActionLink"
				Map hmCmdFileDownload = new HashMap();
				hmCmdFileDownload = createDynamicCommand(context,"PMCDocumentDownloadActionLink",uiMenu,0);
				categoryMapList.add(hmCmdFileDownload);

				//Code for dynamically creating command "PMCFileVersions"
				Map hmCmdFileVersions = new HashMap();
				hmCmdFileVersions = createDynamicCommand(context,"PMCFileVersions",uiMenu,0);
				categoryMapList.add(hmCmdFileVersions);
				if(!isParentSuperceded){
					//Code for dynamically creating command "PMCDocumentEditActionLink"
					Map hmCmdFileEdit = new HashMap();
					hmCmdFileEdit = createDynamicCommand(context,"PMCDocumentEditActionLink",uiMenu,1);
					categoryMapList.add(hmCmdFileEdit);
				}
			}

			resultMap.put("Children",categoryMapList);

		} catch (Exception e) {
			throw e;
		}

		return resultMap;
	}

	/**
	 * This method called from the getDynamicCommandsForRMBMenu for creating individual command.
	 * @author RG6
	 * @param context the eMatrix <code>Context</code> object
	 * @param commandName : name of the command to be created.
	 * @param UIMenu      : Class object for creating the dynamic command.
	 * @param flag        : used to distinguish the suite directory.
	 * @return Map  contaning Maplist which itself contain the  Map objects of the commands which are created dynamically.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG R210
	 */
	private Map createDynamicCommand(Context context,String commandName,UIMenu uiMenu,int flag) throws Exception{
		if(commandName == null ){
			throw new IllegalArgumentException();
		}
		Map commandParamMap = new HashMap();
		try{
			commandParamMap = uiMenu.getCommand(context, commandName);
			String strLang = context.getSession().getLanguage();
			String href =(String)commandParamMap.get("href");  //modify href of the command as per suite directory
			StringBuffer sbHref = new StringBuffer(href);

			String label = (String)commandParamMap.get("label");  // internationlise the label
			Map mapSetting = (Map)commandParamMap.get("settings");

			String strRegSuite = null;
			String strToReplace = null;
			String strStringResourceFileName = null;
			String strNewLabel = null;
			String strImageIconPath = null;
			// code to replace the href as per suite directory
			if(mapSetting != null){
				strRegSuite = (String)mapSetting.get("Registered Suite");
				if(1 == flag){  // if suite directory is prg and call directory common's file
					strToReplace = "../common";
				}else{
					strToReplace = "../"+strRegSuite.toLowerCase();
				}
				//set the path of the icon image
				strImageIconPath = (String)mapSetting.get("Image");
				if(strImageIconPath != null){
					strImageIconPath = strImageIconPath.replace("${COMMON_DIR}","../common" );
					mapSetting.put("Image", strImageIconPath);
				}
			}

			// find string resource file name
			if(strRegSuite != null){
				if(strRegSuite.equalsIgnoreCase("ProgramCentral")){
					strStringResourceFileName = "emxProgramCentralStringResource";
				}else{
					if(strRegSuite.equalsIgnoreCase("Components"))
						strStringResourceFileName = "emxComponentsStringResource";
					else{
						if(strRegSuite.equalsIgnoreCase("Framework")){
							strStringResourceFileName = "emxFrameworkStringResource";
						}
					}
				}
			}

			// code to internationlise the label
			if(label != null && !"".equalsIgnoreCase(label)){
				strNewLabel = EnoviaResourceBundle.getProperty(context, strRegSuite, label, strLang);
				commandParamMap.put("label",strNewLabel);
			}

			//code to replace ${suite directory} from herf by appropriate folder name
			if(href != null && !"".equalsIgnoreCase(href)){
				href = sbHref.substring(href.indexOf('}')+1);
				href = strToReplace+href;
				commandParamMap.put("href", href);
			}

		}catch(Exception e){
			throw e;
		}
		return commandParamMap;
	}


	/**
	 * This triger delegates the call to triggerLinkSubscriptionsToLatestRevision whcih  get the
	 *  old object from that it get associated publish subscribe object
	 * then get the associated Event objects from which it fetch the person objects and finally
	 * get the revised object's id and push the old object's subscription events to the revised
	 * object for those person objects.
	 *
	 * @author RG6
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            : object id - Old ObjectId before revising the Object
	 * @return int : int based on success or failure of the trigger
	 * @throws MatrixException
	 *             if the operation fails
	 * @since PRG R210
	 */
	public int triggerLinkSubscriptionsToLatestRevision(Context context,
			String[] args) throws MatrixException {
		try {
			return emxProgramCentralUtil_mxJPO.triggerLinkSubscriptionsToLatestRevision(context,args);
		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}

	/**
	 * cutPasteObjectsInFolderStructureBrowser - This Method will move the folder, documents objects
	 * in the Folder SB using the Edit menu.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Void
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public Map cutPasteObjectsInFolderStructureBrowser(Context context,String[] args) throws Exception 
	{
		Map returnHashMap = new HashMap();
		Map programMap = (Map) JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");

		com.matrixone.jdom.Element rootElement = null;
		rootElement = (com.matrixone.jdom.Element)programMap.get("contextData");

		Map argsParamMap = new HashMap();
		//in case of multi root structure it's workspace vault id/	 // in case of single root structure it's project id or folder id
		String sParentOID = (String)rootElement.getAttributeValue("objectId");

		String sContextLang = context.getSession().getLanguage();
		java.util.List lCElement     = rootElement.getChildren();

		//validate cut/paste operation
		//Added:PRG:RG6:R212:24-May-2011:IR-110383V6R2012x
		boolean isValidCutPasteOperation = isValidCutPasteOperation(context,rootElement);
		if(!isValidCutPasteOperation)
		{
			return getErrorMessageMap(context, "emxProgramCentral.Common.InvalidCutPasteOperation",sContextLang);
		}
		//End Added:PRG:RG6:R212:24-May-2011:IR-110383V6R2012x

		if(lCElement != null)
		{
			// value of param 'parentOID' will be null for all trees except project tree
			String strProjectId = (String)paramMap.get("parentOID");
			if(ProgramCentralUtil.isNullString(strProjectId))
			{
				strProjectId = (String)paramMap.get("objectId");
			}

			if(ProgramCentralUtil.isNullString(strProjectId))
			{
				throw new IllegalArgumentException("parent id or object is null");
			}

			Map mTypeSelectInfo = PMCWorkspaceVault.getPMCFolderData(context, strProjectId);  

			String isProjectSpaceType    = (String)mTypeSelectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isProjectTemplateType = (String)mTypeSelectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
			String isProjectConceptType = (String)mTypeSelectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);

			// Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
			// below check if operation perform from project tree or folder tree
			boolean isProjectTree = ("true".equalsIgnoreCase(isProjectSpaceType) ||"true".equalsIgnoreCase(isProjectTemplateType)||"true".equalsIgnoreCase(isProjectConceptType)); 
			if(!isProjectTree)
			{ 
				// as tree is of folder object get the parent project id
				strProjectId = getProjectIdFromFolder(context,strProjectId);
				if(ProgramCentralUtil.isNullString(strProjectId))
				{
					throw new MatrixException("Project id is Null");
				}
				mTypeSelectInfo = PMCWorkspaceVault.getPMCFolderData(context,strProjectId); // get project info
				isProjectSpaceType    = (String)mTypeSelectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				isProjectTemplateType = (String)mTypeSelectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
				isProjectConceptType = (String)mTypeSelectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			}
			//End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
			boolean isCutPasteAllowed = ("true".equalsIgnoreCase(isProjectSpaceType) ||"true".equalsIgnoreCase(isProjectTemplateType)||"true".equalsIgnoreCase(isProjectConceptType));

			if(isCutPasteAllowed)
			{
				argsParamMap.clear();
				argsParamMap.put("objectId",sParentOID);
				argsParamMap.put("parentOID",sParentOID);
				argsParamMap.put("sContextLang",sContextLang);
				argsParamMap.put("isProjectTemplateType",isProjectTemplateType);
				argsParamMap.put("isProjectSpaceType",isProjectSpaceType);
				argsParamMap.put("isProjectConceptType",isProjectConceptType);

				Map parentInfoMap = createParentInfoMap(context,sParentOID);  // get parent info map // map storing info related to current root node

				returnHashMap = performCutPasteOperation(context,lCElement,parentInfoMap,argsParamMap);
			}
			else
			{
				return getErrorMessageMap(context, "emxProgramCentral.Common.InvalidCutPasteOperation",sContextLang);
			}
		}

		return returnHashMap;
	}

	/**
	 * cutObjectsInSB - This Method will get the list of objects to cut, checks weather there parent has access
	 * to disconnect the objects and perform the disconnection.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param Map holds the input arguments:
	 * 	@param Map slCutObject - list of object to cut
	 * 	@param Map mRelID - contains child info map with key as childOID and value as Map
	 *  						containing relid , rowid , oid etc.
	 *  	@param Map argsParamMap -  contains parent and root object info
	 * @return Map - returnHashMap -  result Map containing maplist of modified objects
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	protected Map cutObjectsInSB(Context context,Map mProgramMap) throws MatrixException{

		Map returnHashMap = new HashMap();
		MapList mlItems=new MapList(); // To store the key "changedRows"
		try{
			String sContextLang = context.getSession().getLanguage();
			StringList slCutObjects = (StringList) mProgramMap.get("slCutObject"); // child object list
			Map mRelIds = (Map) mProgramMap.get("mRelID");
			Map parentInfoMap = (Map)mProgramMap.get("parentInfoMap");
			Map argsParamMap = (Map)mProgramMap.get("argsParamMap");
			String sParentOID = (String)argsParamMap.get("parentOID"); // parent node object
			String isProjectTemplateType = (String)argsParamMap.get("isProjectTemplateType");  // param for root node

			BusinessObjectWithSelectList folderContentWithSelectList = null;
			BusinessObjectWithSelect bows = null;
			final String SELECT_VAULTED_DOCUMENT_REV2_REL_ID  =  "relationship["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].id";
			final String SELECT_ATTRIBUTE_TITLE = "attribute[" + DomainConstants.ATTRIBUTE_TITLE + "]";
			boolean hasRemoveAccess = false;
			// selectables required by cut oepration
			StringList slCutOperationSelects = new StringList();
			slCutOperationSelects.add(DomainConstants.SELECT_ID);
			slCutOperationSelects.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			slCutOperationSelects.add(SELECT_VAULTED_DOCUMENT_REV2_REL_ID);
			slCutOperationSelects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);

			//check user has disconnect access bit or not
			int objSize = slCutObjects.size();
			String[] selectedObjIds = new String[objSize];
			slCutObjects.toArray(selectedObjIds);
			StringList busSelect = new StringList(4);
			busSelect.addElement("current.access[todisconnect]");
			busSelect.addElement(DomainObject.SELECT_NAME);
			busSelect.addElement(WorkspaceVault.SELECT_ATTRIBUTE_FOLDER_TITLE);
			busSelect.addElement(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
			folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,selectedObjIds,busSelect);
			boolean hasToDisconnetAccess = true;
			
			String folderNames = null;;
			for(int i=0;i<folderContentWithSelectList.size();i++){
				BusinessObjectWithSelect bws = folderContentWithSelectList.getElement(i);
				String currentAccess    = bws.getSelectData("current.access[todisconnect]");
				//StringList accessList = FrameworkUtil.split(currentAccess, ",");
				
				if(!"TRUE".equalsIgnoreCase(currentAccess)){
					String folderName 			= bws.getSelectData(DomainObject.SELECT_NAME);
					String isControlledFolder 	= bws.getSelectData(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
					if("true".equalsIgnoreCase(isControlledFolder)){
						folderName 	= bws.getSelectData(WorkspaceVault.SELECT_ATTRIBUTE_FOLDER_TITLE);
					}
					if(folderNames == null){
						folderNames = folderName;
					}else{
						folderNames += ", "+ folderName;
					}
					hasToDisconnetAccess 		= false;
				}
			}

			String notice = "";
			if(slCutObjects.size()>0){
				StringList slCutObjectsList = new StringList();
				String isParentWorkspaceVault = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
				String isParentControlledFolder = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
				// Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
				//param for parent node
				String isParentProjectSpace = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String isParentProjectConcept = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
				boolean isParentProjectType = (Boolean)parentInfoMap.get("isParentProjectType"); //either of space/concept/template type
				//End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x

				//if root is not project template the check if parent is workspace vault then get the access info
				if(!"true".equalsIgnoreCase(isProjectTemplateType) && "true".equalsIgnoreCase(isParentWorkspaceVault))
				{
					String [] args = new String [1];
					Map argParamMap = new HashMap();
					Map argProgramMap = new HashMap();
					argParamMap.put("objectId", sParentOID);
					argProgramMap.put("paramMap", argParamMap);
					args = JPO.packArgs(argProgramMap);
					hasRemoveAccess = hasRemoveAccessOnWorkspaceVault(context, args);
				} // Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
				else if("true".equalsIgnoreCase(isProjectTemplateType) && "true".equalsIgnoreCase(isParentWorkspaceVault)) 		 // if root is project template and parent is workspace vault //user access is checked while changing the edit mode in preprocess jpo
				{
					hasRemoveAccess = true;
				}
				else if(isParentProjectType && hasToDisconnetAccess) 		 // if parent is project space/concept/template type
				{
					// currently no need to check the access as they are checked while mode is changed in preprocess checkforeditmethod
					hasRemoveAccess = true;
				} //End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x

				// if parent object has remove accesss for the current user
				if(hasRemoveAccess)
				{
					String[] strCutObjIds = new String[0];
					strCutObjIds = new String[slCutObjects.size()];
					slCutObjects.copyInto(strCutObjIds);
					folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strCutObjIds,slCutOperationSelects);
					for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
					{
						bows = itr.obj();
						String sChildObjId = bows.getSelectData(DomainConstants.SELECT_ID);
						Map mChildInfo = (Map)mRelIds.get(sChildObjId);
						String sRelId = (String)mChildInfo.get("relid");
						//if wk vault type or any project space/template/concept type then move ahead
						if("true".equalsIgnoreCase(isParentWorkspaceVault) || isParentProjectType){
							// check if controlled Folder and it in create state
							String strState = (String)parentInfoMap.get(DomainConstants.SELECT_CURRENT);
							if("true".equalsIgnoreCase(isParentControlledFolder) && !DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(strState)){
								// error msg, CF in create state can only be modified
								notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
										"emxProgramCentral.Folders.PasteObject.NoConnectInsideControlledFolderInReleaseStateNotice", 
										sContextLang);
								returnHashMap.put("Message", notice);
								returnHashMap.put("Action", "ERROR");
								return(returnHashMap);
							}

							StringList slConnectedObjRelIds = new StringList();
							String isChildWorkspaceVault = "";
							String isVersionObject = "";

							slConnectedObjRelIds = bows.getSelectDataList(SELECT_VAULTED_DOCUMENT_REV2_REL_ID);
							isChildWorkspaceVault = bows.getSelectData(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
							isVersionObject = bows.getSelectData(CommonDocument.SELECT_IS_VERSION_OBJECT);

							argsParamMap.put("relId", sRelId);
							argsParamMap.put("relIdList", slConnectedObjRelIds);
							argsParamMap.put("isChildWorkspaceVault", isChildWorkspaceVault);
							argsParamMap.put("isVersionObject",isVersionObject);

							Map objReturnedMap = cutObjectsInWorkspaceVault(context,argsParamMap);

							if(objReturnedMap != null && "SUCCESS".equalsIgnoreCase((String)objReturnedMap.get("Action"))){
								//add code in maplist
								slCutObjectsList.add(sRelId);
								mlItems.add(mChildInfo); //Storing in the global MapList mlItems to be added for "changedRows" key
								returnHashMap.put("changedRows", mlItems);
								returnHashMap.put("Action", "success");
								//	 return returnHashMap;
							}else{
								return objReturnedMap;
							}
						}else{
							// if parent is not workspace vault & project 
							notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Folders.CutObject.NoDisconnectForTypeNotice", sContextLang);
							returnHashMap.put("Message", notice);
							returnHashMap.put("Action", "ERROR");
							return(returnHashMap);
						}
					}
					int size = slCutObjectsList.size();
					if(size > 0){
						String [] sarrCutObj = new String [size];
						slCutObjectsList.copyInto(sarrCutObj);
						DomainRelationship.disconnect(context, sarrCutObj);
						return returnHashMap;
					}
				}
				else
				{
					//Added for 500251: User does not have toDisconnet access on selected object
					Locale locale			= new Locale(sContextLang);
					String[] messageValues = new String[1];
					messageValues[0] = folderNames;
					notice = MessageUtil.getMessage(context,null,
		                                 "emxProgramCentral.Folders.CutObject.NoDisconnectAccess",
		                                 messageValues,
		                                 null,
		                                 locale,
		                                 "emxProgramCentralStringResource"); 
					
					returnHashMap.put("Message", notice);
					returnHashMap.put("Action", "ERROR");
					return(returnHashMap); 
				}
			}
		}catch(Exception e){
			throw new MatrixException(e);
		}
		return returnHashMap;
	}

	/**
	 * cutObjectsInWorkspaceVault - This Method will check if parent has permissions to disconnect and
	 * and connect source object with the parent object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Object - if success then boolean or Map containing error message
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	protected Map cutObjectsInWorkspaceVault(Context context, Map paramMap)throws MatrixException{

		Map returnHashMap = new HashMap();
		try{
			String sParentOID = (String)paramMap.get("parentOID");
			String sRelId = (String)paramMap.get("relId");

			if(null != sParentOID && !"".equalsIgnoreCase(sParentOID) )
			{
				if(sRelId == null || "".equalsIgnoreCase(sRelId))
				{
					throw new IllegalArgumentException("RelId is Null");
				}
			}
			else
			{
				throw new IllegalArgumentException("ParentOID is Null");
			}
			StringList slConnectedRelIds = (StringList)paramMap.get("relIdList");
			String sContextLang = (String)paramMap.get("sContextLang");
			String isProjectTemplateType = (String)paramMap.get("isProjectTemplateType");
			String isProjectSpaceType = (String)paramMap.get("isProjectSpaceType");
			String isProjectConceptType = (String)paramMap.get("isProjectConceptType");
			String isChildWorkspaceVault = (String)paramMap.get("isChildWorkspaceVault");
			String notice = "";
			if("true".equalsIgnoreCase(isChildWorkspaceVault)||
					(slConnectedRelIds != null && slConnectedRelIds.contains(sRelId)))
			{
				String [] args = new String [1];
				Map argParamMap = new HashMap();
				Map argProgramMap = new HashMap();
				argParamMap.put("objectId", sParentOID);
				argProgramMap.put("paramMap", argParamMap);
				args = JPO.packArgs(argProgramMap);

				if("true".equalsIgnoreCase(isProjectTemplateType) ||
						( ("true".equalsIgnoreCase(isProjectSpaceType) || "true".equalsIgnoreCase(isProjectConceptType) ))){ //Modified:4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
					// disconnect the parent folder connected to project or sub folder or document  relationship 
					returnHashMap.put("Action", "SUCCESS");
				}else{
					//error msg saying no Disconnect access on the parent of the source folder
					notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "" +
							"emxProgramCentral.Folders.CutObject.NoDisconnectAccessForParentNotice", sContextLang);
					returnHashMap.put("Message", notice);
					returnHashMap.put("Action", "ERROR");
					return(returnHashMap);
				}
			}
			else
			{
				//error msg saying no disconnect access on the source(child) objects
				notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "" +
						"emxProgramCentral.Folders.CutObject.NoDisconnectForTypeNotice", sContextLang);
				returnHashMap.put("Message", notice);
				returnHashMap.put("Action", "ERROR");
				return(returnHashMap);
			}

		}catch(Exception e){
			throw new MatrixException(e);
		}
		return returnHashMap;
	}

	/**
	 * pasteObjectsToSB - This Method will get the list of objects to paste,checks weather parent has access
	 * to connect the objects and perform the connection based on child object type.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param Map holds the input arguments:
	 * 	@param Map slPasteObject - list of object to paste.
	 * 	@param Map mRelID - contains child info map with key as childOID and value as Map.
	 *  						containing relid , rowid , oid etc.
	 *  	@param Map parentInfoMap -  contains parent object info.
	 *  	@param Map argsParamMap -  contains root object info.
	 * @return Map - returnHashMap -  result Map containing maplist of modified objects.
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	protected Map pasteObjectsToSB(Context context,Map mProgramMap) throws MatrixException{

		Map returnHashMap = new HashMap();
		MapList mlItems=new MapList(); // To store the key "changedRows"
		boolean cxtPush = false;
		try{
			String sContextLang = context.getSession().getLanguage();
			StringList slPasteObject = (StringList) mProgramMap.get("slPasteObject");
			Map mRelIds = (Map) mProgramMap.get("mRelID");
			Map parentInfoMap = (Map)mProgramMap.get("parentInfoMap");
			Map argsParamMap = (Map)mProgramMap.get("argsParamMap");
			String isProjectTemplateType = (String)argsParamMap.get("isProjectTemplateType");
			String isProjectSpaceType    = (String)argsParamMap.get("isProjectSpaceType");
			String isProjectConceptType  = (String)argsParamMap.get("isProjectConceptType");
			String sParentOID = (String)argsParamMap.get("parentOID");
			BusinessObjectWithSelectList folderContentWithSelectList = null;
			BusinessObjectWithSelect bows = null;
			int wkObjCount = 0;
			int docObjCount = 0;
			int prjWkCount = 0;
			int pasteOblListSize = slPasteObject.size();

			StringList slPasteOperationSelects = new StringList();
			slPasteOperationSelects.add(DomainConstants.SELECT_ID);
			slPasteOperationSelects.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			slPasteOperationSelects.add(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
			slPasteOperationSelects.add(WorkspaceVault.SELECT_ACCESS_TYPE);
			slPasteOperationSelects.add(WorkspaceVault.SELECT_OWNER);

			DomainObject domParentObj = DomainObject.newInstance(context);
			String notice = "";

			if(pasteOblListSize >0){
				String isParentWorkspaceVault = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
				String isParentControlledFolder = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
				//rg6 start
				String isParentProjectSpace = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String isParentProjectConcept = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
				boolean isParentProjectType = (Boolean)parentInfoMap.get("isParentProjectType");

				//rg6 end
				domParentObj.setId(sParentOID);
				Map mParentuserPermissions = new HashMap();  // parent user permission map
				String[] strPasteObjIds = new String[0];
				StringList slProjectWorkspaceVaultObj = new StringList();  //list for folder object to connect to project
				StringList slWorkspaceVaultObj = new StringList();  //list for folder object to connect to workspace vault
				StringList slDocumentObj = new StringList();       // list fo doc object to connect

				strPasteObjIds = new String[pasteOblListSize];

				if("true".equalsIgnoreCase(isParentWorkspaceVault)){
					WorkspaceVault parentWorkspaceVaultObj = (WorkspaceVault) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE_VAULT);
					parentWorkspaceVaultObj.setId(sParentOID);
					mParentuserPermissions = parentWorkspaceVaultObj.getUserPermissions(context);
				}

				slPasteObject.copyInto(strPasteObjIds);
				folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strPasteObjIds,slPasteOperationSelects);
				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
				{
					bows = itr.obj();
					String sChildObjId = bows.getSelectData(DomainConstants.SELECT_ID);
					Map mChildInfo = (Map)mRelIds.get(sChildObjId);
					String isChildWorkspaceVault = "";
					String isChildControlledFolder = "";
					isChildWorkspaceVault = bows.getSelectData(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
					isChildControlledFolder = bows.getSelectData(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);

					if("true".equalsIgnoreCase(isParentWorkspaceVault)){
						// check if controlled Folder and it in create state
						String strState = (String)parentInfoMap.get(DomainConstants.SELECT_CURRENT);
						if("true".equalsIgnoreCase(isParentControlledFolder) && !DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(strState)){
							// error msg, CF in create state can only be modified
							notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
									"emxProgramCentral.Folders.PasteObject.NoConnectInsideControlledFolderInReleaseStateNotice", sContextLang);
							returnHashMap.put("Message", notice);
							returnHashMap.put("Action", "ERROR");
							return(returnHashMap);
						}

						// check that parent and child are of same type
						if("true".equalsIgnoreCase(isChildWorkspaceVault)){
							if("true".equalsIgnoreCase(isParentControlledFolder) && !"true".equalsIgnoreCase(isChildControlledFolder)){
								// error msg parent is cf and child is wk vault not allowed difff wk vault types
								notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
										"emxProgramCentral.Folders.PasteObject.NotHomogeneousObjectNotice", sContextLang);
								returnHashMap.put("Message", notice);
								returnHashMap.put("Action", "ERROR");
								return(returnHashMap);
							}
						}

						if("true".equalsIgnoreCase(isChildControlledFolder)){
							if(!"true".equalsIgnoreCase(isParentControlledFolder)){
								//error msg parent is not CF but child is CF not allowed not allowed difff wk vault types
								notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
										"emxProgramCentral.Folders.PasteObject.NotHomogeneousObjectNotice", sContextLang);
								returnHashMap.put("Message", notice);
								returnHashMap.put("Action", "ERROR");
								return(returnHashMap);
							}
						}

						String [] args = new String [1];
						Map argParamMap = new HashMap();
						Map argProgramMap = new HashMap();
						argParamMap.put("objectId", sParentOID);
						argProgramMap.put("paramMap", argParamMap);
						args = JPO.packArgs(argProgramMap);
						// check for parent has add access
						if("true".equalsIgnoreCase(isProjectTemplateType) ||
								( ("true".equalsIgnoreCase(isProjectSpaceType) || "true".equalsIgnoreCase(isProjectConceptType) )&&
										hasAddAccessOnWorkspaceVault(context, args))){

							if("true".equalsIgnoreCase(isChildWorkspaceVault)){
								//common's trigger 'RelationshipSubVaultsCreateAction' is called to set user permissions of parent to child upon creation of new connection
								slWorkspaceVaultObj.add(sChildObjId);
							}else{
								// add child to the  document list
								slDocumentObj.add(sChildObjId);
							}
						}
						else{
							//error msg saying no Connect access on parent of the source folder
							notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
									"emxProgramCentral.Folders.CutObject.NoconnectAccessForParentNotice", sContextLang);
							returnHashMap.put("Message", notice);
							returnHashMap.put("Action", "ERROR");
							return(returnHashMap);
						}
					} //Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
					else if(isParentProjectType)
					{
						// in future may need to check project access  					 
						// in this case there is no need to set new user permissions as the folder is paste as root under project
						if("true".equalsIgnoreCase(isChildWorkspaceVault)) {
							slProjectWorkspaceVaultObj.add(sChildObjId);
						} //End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
						else
						{
							// error mesg saying project is not allowed as parent for types other than workspace vault
							notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
									"emxProgramCentral.Folders.CutObject.InvalidParentObject", sContextLang);
							returnHashMap.put("Message", notice);
							returnHashMap.put("Action", "ERROR");
							return(returnHashMap);
						}
					}
					else
					{
						//error msg saying  parent is not worksapce vault & project type
						notice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.Folders.CutObject.InvalidParentObject", sContextLang);
						returnHashMap.put("Message", notice);
						returnHashMap.put("Action", "ERROR");
						return(returnHashMap);
					}
				}

				prjWkCount  = slProjectWorkspaceVaultObj.size();
				wkObjCount = slWorkspaceVaultObj.size();
				docObjCount = slDocumentObj.size();
				// Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
				if(prjWkCount > 0)
				{
					//connect the  folders to project object 
					String  [] arrProjWorkspaceVaultObj = new String[prjWkCount];
					slProjectWorkspaceVaultObj.copyInto(arrProjWorkspaceVaultObj);
					Map domRelNwIdMap = new HashMap();
					try{
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						cxtPush = true;     
						domRelNwIdMap =  DomainRelationship.connect(context, domParentObj,DomainConstants.RELATIONSHIP_PROJECT_VAULTS, true,arrProjWorkspaceVaultObj);					
					}catch(Exception e) {
						throw new MatrixException(e);
					}
					finally {
						if(cxtPush) {
							ContextUtil.popContext(context);
						}
					}
					
					if(domRelNwIdMap != null){
						for(int i = 0; i< arrProjWorkspaceVaultObj.length; i++){
							String sChildObjectId = arrProjWorkspaceVaultObj[i];
							Map mChildInfo = (Map)mRelIds.get(sChildObjectId);
							String nwRelId = (String)domRelNwIdMap.get(sChildObjectId);
							mChildInfo.put("relid", nwRelId);  //update new rel id
							//add code in maplist
							mlItems.add(mChildInfo); //Storing in the global MapList mlItems to be added for "changedRows" key
						}
					}
				}
				//End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
				if(wkObjCount > 0){
					//connect the subfolders
					String  [] arrWorkspaceVaultObj = new String[wkObjCount];
					slWorkspaceVaultObj.copyInto(arrWorkspaceVaultObj);
					Map domRelNwIdMap = new HashMap();
					try{
						ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
						cxtPush = true;     
						domRelNwIdMap =  DomainRelationship.connect(context, domParentObj,DomainConstants.RELATIONSHIP_SUB_VAULTS, true,arrWorkspaceVaultObj);						
					}catch(Exception e) {
						throw new MatrixException(e);
					}
					finally {
						if(cxtPush) {
							ContextUtil.popContext(context);
						}
					}
					
					if(domRelNwIdMap != null){
						for(int i = 0; i< arrWorkspaceVaultObj.length; i++){
							String sChildObjectId = arrWorkspaceVaultObj[i];
							Map mChildInfo = (Map)mRelIds.get(sChildObjectId);
							String nwRelId = (String)domRelNwIdMap.get(sChildObjectId);
							mChildInfo.put("relid", nwRelId);  //update new rel id
							//add code in maplist
							mlItems.add(mChildInfo); //Storing in the global MapList mlItems to be added for "changedRows" key
						}
					}
				}

				if(docObjCount > 0){
					//connect the document objects
					String  [] arrDocObj = new String[docObjCount];
					slDocumentObj.copyInto(arrDocObj);
					//Added:PRG:RG6:R212:24-Jun-2011:IR-110390V6R2012x
					//as there is no toConnect access on the document object's policy for users other than project owner
					//so even if user have from disconnect access and source folder and fromconnect access on the dest folder 
					// other user(who has access to disconnect) can not connect document to other folder obj on which it has add access
					// so using push pop for connection of the object
					Map domRelNwIdMap = null;
					ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
					cxtPush = true;     
					try{
						domRelNwIdMap =  DomainRelationship.connect(context, domParentObj,DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2, true, arrDocObj);
					}catch(Exception e) {
						throw new MatrixException(e);
					}finally{
						if(cxtPush) {
							ContextUtil.popContext(context);
						}
					}
					//Added:PRG:RG6:R212:24-Jun-2011:IR-110390V6R2012x:End
					if(domRelNwIdMap != null){
						for(int i = 0; i< arrDocObj.length; i++){
							String sChildObjectId = arrDocObj[i];
							Map mChildInfo = (Map)mRelIds.get(sChildObjectId);
							String nwRelId = (String)domRelNwIdMap.get(sChildObjectId);
							mChildInfo.put("relid", nwRelId);  //update new rel id
							//add code in maplist
							mlItems.add(mChildInfo); //Storing in the global MapList mlItems to be added for "changedRows" key
						}
					}
				}

				returnHashMap.put("changedRows", mlItems);
				returnHashMap.put("Action", "success");
			}
		}catch(Exception e){
			throw new MatrixException(e);
		}
		return returnHashMap;
	}

	/**
	 * setUserAccessToChild - This Method will set the permissions of the parent object to the child objects.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Void
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	protected void setUserAccessToChild(Context context,Map paramMap)throws MatrixException{

		StringList slSelectObj = new StringList();
		i18nNow i18n = new i18nNow();

		WorkspaceVault childWorkspaceVaultObj = (WorkspaceVault) DomainObject.newInstance(context,DomainConstants.TYPE_WORKSPACE_VAULT);

		try{
			String sParentOID = (String)paramMap.get("parentOID");
			String sObjectId = (String)paramMap.get("childOID");
			Map mParentAccessMap = (Map)paramMap.get("userPermissions");

			if(null != sParentOID && !"".equalsIgnoreCase(sParentOID) )
			{
				if(sObjectId == null || "".equalsIgnoreCase(sObjectId))
				{
					throw new IllegalArgumentException("ObjectId is Null");
				}
			}
			else
			{
				throw new IllegalArgumentException("ParentOID is Null");
			}

			String isProjectTemplateType = (String)paramMap.get("isProjectTemplateType");
			String strFolderAccess = (String)paramMap.get(WorkspaceVault.SELECT_ACCESS_TYPE);
			String strChildObjOnwer = (String)paramMap.get(WorkspaceVault.SELECT_OWNER);

			childWorkspaceVaultObj.setId(sObjectId);

			Map nwChildAccessMap = new HashMap();

			if(!"true".equalsIgnoreCase(isProjectTemplateType))  {
				final String STR_FOLDER_INHERITED_ACCESS_TYPE =
					i18n.GetString("emxFrameworkStringResource", "en", "emxFramework.Range.Access_Type.Inherited");

				// code for access inheritance (is this code really needed check)
				if(STR_FOLDER_INHERITED_ACCESS_TYPE.equalsIgnoreCase(strFolderAccess)){
					// inherit the members of the parent folder objects
					Set mapKeySet = mParentAccessMap.keySet();
					if(null != mapKeySet){
						Iterator it = mapKeySet.iterator();
						while(it.hasNext()){
							String strMapKey =(String) it.next();
							String strAccessVal = (String)mParentAccessMap.get(strMapKey);
							if(null != strMapKey && !strMapKey.equalsIgnoreCase(strChildObjOnwer)){
								nwChildAccessMap.put(strMapKey, strAccessVal);
							}
						}
					}
				}
			}
			// set access for owner (for specific access no aceess available for child)
			if(null != strChildObjOnwer && !"".equalsIgnoreCase(strChildObjOnwer)){
				nwChildAccessMap.put(strChildObjOnwer,ProgramCentralConstants.VAULT_ACCESS_ADD_REMOVE);
			}
			int mapSize =  nwChildAccessMap.size();
			if( mapSize > 0){
				childWorkspaceVaultObj.setUserPermissions(context, nwChildAccessMap);
			}

		}catch(Exception e){
			throw new MatrixException(e);
		}
	}


	/**
	 * hasRemoveAccessOnWorkspaceVault - This Method will check weather the folder has remove access or not
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	public static boolean hasRemoveAccessOnWorkspaceVault(Context context, String args[])throws Exception{

		Map programMap = (Map) JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");
		String sObjectId = (String)paramMap.get("objectId");
		boolean hasAccess = false;
		if(null == sObjectId || "".equalsIgnoreCase(sObjectId)){
			throw new IllegalArgumentException();
		}
		String sContextUser = context.getUser();
		WorkspaceVault workspaceVault = (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
		workspaceVault.setId(sObjectId);
		Map mUserPersMap = workspaceVault.getUserPermissions(context);
		if(null != mUserPersMap){
			String sContextUserAccess = (String)mUserPersMap.get(sContextUser);
			if(!(ProgramCentralConstants.VAULT_ACCESS_ADD_REMOVE.equalsIgnoreCase(sContextUserAccess) || ProgramCentralConstants.VAULT_ACCESS_REMOVE.equalsIgnoreCase(sContextUserAccess))){
				// if user does not have modify acess over the workspace vault object
				hasAccess = false;
			}else{
				hasAccess = true;
			}
		}
		if(!hasAccess)	// if we don't have access yet, then check Folder has the modify and from connect access 		
			hasAccess = workspaceVault.checkAccess(context, (short)AccessConstants.cModify) && workspaceVault.checkAccess(context, (short)AccessConstants.cFromDisconnect);
		return hasAccess;
	}

	/**
	 * hasAddAccessOnWorkspaceVault - This Method will check weather the folder has add access or not.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return boolean
	 * @throws Exception if the operation fails
	 * @since R211
	 * @author RG6
	 */
	public static boolean hasAddAccessOnWorkspaceVault(Context context, String args[])throws Exception{

		Map programMap = (Map) JPO.unpackArgs(args);
		Map paramMap = (Map)programMap.get("paramMap");
		String sObjectId = (String)paramMap.get("objectId");
		if(null == sObjectId || "".equalsIgnoreCase(sObjectId)){
			throw new IllegalArgumentException();
		}
		boolean hasAccess = false;

		String sContextUser = context.getUser();
		WorkspaceVault workspaceVault = (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT);
		workspaceVault.setId(sObjectId);
		Map mUserPersMap = workspaceVault.getUserPermissions(context);
		if(null != mUserPersMap){
			String sContextUserAccess = (String)mUserPersMap.get(sContextUser);
			if(!(ProgramCentralConstants.VAULT_ACCESS_ADD_REMOVE.equalsIgnoreCase(sContextUserAccess) || ProgramCentralConstants.VAULT_ACCESS_ADD.equalsIgnoreCase(sContextUserAccess))){
				// if user does not have modify acess over the workspace vault object
				hasAccess = false;
			}else{
				hasAccess = true;
			}
		}
		if(!hasAccess)	// if we don't have access yet, then check Folder has the modify, to connect access 		
			hasAccess = workspaceVault.checkAccess(context, (short)AccessConstants.cModify) && workspaceVault.checkAccess(context, (short)AccessConstants.cToConnect);
	return hasAccess;
	}

	/**
	 * This method is used in Autonomy search to fetch the filter PROJECT for documents. It returns the names of the 
	 * Project to which the docuemnt is connected.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            0 : objectId - Object id of the document.
	 *            1:  Type: type of the Object (document).
	 * @return String : A 'matrix.db.SelectConstants.cSelectDelimiter' delimited string of names of the projects.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG V6R2012
	 * @author NR2
	 */
	public String getConnectedProject(Context context, String[] args)
	throws Exception{
		String connectedProject = "";
		try{
			MapList mlParentFolder = getConnectedObjects(context, args);
			DomainObject folder = DomainObject.newInstance(context, DomainConstants.TYPE_WORKSPACE_VAULT); 
			StringList returnList = new StringList();

			for(int i=0;i<mlParentFolder.size();i++){
				Map m = (Map) mlParentFolder.get(i);
				String folderId = (String)m.get(DomainConstants.SELECT_ID);
				folder.setId(folderId);

				do{
					String strFolderParentId = folder.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_SUB_VAULTS + "].from.id");
					if(null != strFolderParentId && !"".equals(strFolderParentId))folder.setId(strFolderParentId);
					else break;
				}while(true);
				String strFolderParentProjectName = folder.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "].from.name");				 

				if((strFolderParentProjectName != null) && !returnList.contains(strFolderParentProjectName)){
					returnList.add(strFolderParentProjectName);
				} 
			} //End for loop

			boolean isMatrixSearch = false;
			if(args.length > 1) 
			{
				isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
			}

			String delimiter = isMatrixSearch?"|":matrix.db.SelectConstants.cSelectDelimiter;

			StringBuffer connectedProjectBuff = new StringBuffer();
			for(int i=0;i<returnList.size();i++){
				String val = returnList.get(i).toString();
				connectedProjectBuff.append(connectedProjectBuff.length()==0?val:delimiter+val);
			}
			return connectedProjectBuff.toString();
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	} //End Method

	/**
	 * This method is used in Autonomy search to fetch the filter Folders for documents. It returns the names of the 
	 * Folders to which the docuemnt is connected.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            0 : objectId - Object id of the document.
	 *            1:  Type: type of the Object (document).
	 * @return String : A 'matrix.db.SelectConstants.cSelectDelimiter' delimited string of names of the projects.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG V6R2012
	 * @author NR2
	 */
	public String getConnectedFolders(Context context,String[] args)throws Exception
	{
		StringBuffer returnBuff = new StringBuffer();
		try{
			MapList mlParentFolder = getConnectedObjects(context,args);
			//Deleted 3
			boolean isMatrixSearch = false;
			if(args.length > 1) 
			{
				isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
			}

			String delimiter = isMatrixSearch?"|":matrix.db.SelectConstants.cSelectDelimiter;

			for(int i=0;i<mlParentFolder.size();i++){
				Map m = (Map) mlParentFolder.get(i);
				String folderName = (String) m.get(DomainConstants.SELECT_NAME);
				 String folderType = (String) m.get(DomainConstants.SELECT_TYPE);
				
				if((DomainConstants.TYPE_CONTROLLED_FOLDER).equals(folderType)){
				     folderName = (String) m.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TITLE); 
				returnBuff.append(returnBuff.length()==0?folderName:delimiter + folderName);
				 }else{
				     returnBuff.append(returnBuff.length()==0?folderName:delimiter + folderName);
				 }
			}
		}
		catch(Exception e){
			throw new Exception();
		}
		return returnBuff.toString();
	}
	/**
	 * This method is used as helper method for methods getConnectedProjects and getConnectedFolders
	 * Fetches the list of folders connected to a document object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            0 : objectId - Object id of the document.
	 *            1:  Type: type of the Object (document).
	 * @return String : A 'matrix.db.SelectConstants.cSelectDelimiter' delimited string of names of the projects.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG V6R2012
	 * @author NR2
	 */
	protected MapList getConnectedObjects(Context context,String[] args) throws Exception
	{
		MapList returnList = new MapList();
		try{
			String docId = args[0];
			boolean isMatrixSearch = false;
			if(args.length > 1){
				isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
			}

			if(null==docId || "".equals(docId) || "null".equalsIgnoreCase(docId)){
				throw new Exception();
			}

			DomainObject doc = DomainObject.newInstance(context, docId);			
			String docType = doc.getInfo(context,DomainConstants.SELECT_TYPE);
			if(mxType.isOfParentType(context, docType, DomainConstants.TYPE_DOCUMENT)){
				StringList sl = new StringList();
				sl.add(DomainConstants.SELECT_ID);
				sl.add(DomainConstants.SELECT_NAME);
				 sl.add(DomainConstants.SELECT_TYPE);
				 sl.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TITLE);

				returnList = doc.getRelatedObjects(context,
						DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2,
						DomainConstants.TYPE_WORKSPACE_VAULT,
						sl,
						null,
						true,
						false,
						(short) 1,
						"",
						"",
						(short) 0);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		return returnList;
	}
	/**
	 * This is used to get the range values of Projects and Folders for a document Object.Used in chooser of Document search.
	 * Fetches the Map of folders/projects connected to a document object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            0 : objectId - Object id of the document.
	 *            1:  Type: type of the Object (document).
	 * @return String : A 'matrix.db.SelectConstants.cSelectDelimiter' delimited string of names of the projects.
	 * @throws Exception
	 *             if the operation fails
	 * @since PRG V6R2012
	 * @author NR2
	 */
	public static Map getRangeValuesForDocuments(Context context,String[] args)
	throws Exception{
		try{
			Map returnMap = new HashMap();
			Map argMap = (Map) JPO.unpackArgs(args);
			String currentField = (String) argMap.get("currentField");

			String selectable = null;
			if("PRG_FOLDER_PROJECTSPACE_NAME".equals(currentField)){
				selectable = "program[emxProjectFolder -method getConnectedProject ${OBJECTID}]"; 
			}
			else if("PRG_FOLDER_NAME".equals(currentField)){
				selectable = "program[emxProjectFolder -method getConnectedFolders ${OBJECTID}]"; 
			}
			else{
				throw new RuntimeException("Unsupported field " + currentField);
			}

			StringList selectList = new StringList();
			selectList.add(DomainConstants.SELECT_ID);
			selectList.add(selectable);
			MapList resultsList = DomainObject.findObjects(context, DomainConstants.TYPE_DOCUMENT, DomainConstants.QUERY_WILDCARD, "", selectList);

			for(Iterator itr=resultsList.iterator();itr.hasNext();){
				Map documentMap = (Map) itr.next();
				String projectNames = (String) documentMap.get(selectable);

				if(null==projectNames || "".equals(projectNames))
				{
					continue;
				}

				StringList slProjectNames = FrameworkUtil.split(projectNames, SelectConstants.cSelectDelimiter);

				if(projectNames.indexOf(SelectConstants.cSelectDelimiter) == -1)
				{
					slProjectNames = FrameworkUtil.split(projectNames, "|");
				}

				for (Iterator itrProjectNames = slProjectNames.iterator(); itrProjectNames.hasNext();) 
				{
					String strProjectName = (String) itrProjectNames.next();
					if (!returnMap.containsKey(strProjectName)) 
					{
						returnMap.put(strProjectName, new Integer(1));
					}
					else 
					{
						Integer count = (Integer)returnMap.get(strProjectName);
						count = new Integer(count.intValue() + 1);
						returnMap.put(strProjectName, count);
					}
				}
			}

			for (Iterator itrProjectNameAndCount = returnMap.keySet().iterator(); itrProjectNameAndCount.hasNext();) 
			{
				String strProjectName = (String) itrProjectNameAndCount.next();
				Integer totalDocuments = (Integer)returnMap.get(strProjectName);

				returnMap.put(strProjectName, strProjectName + " (" + totalDocuments + ")");
			}
			return returnMap;
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}//End of method 

	/**
	 * This method is used to decide editing of Row in all views of folder
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * checklist item it is true and for others it is false.
	 * @throws MatrixException if the operation fails
	 * @since PRG 2011x.HF4
	 * @author MS9
	 * @returns StringList
	 */
	public StringList editAccessToFolderRows(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");  
			StringList strList = new StringList();
			final String STR_SELECT_ACCESS_MODIFY = "current.access[modify]";
			StringList slSelects = new StringList();
			//Added:PRG:RG6:R212:30-july-2011::IR-117051V6R2012x
			slSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			slSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
			slSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			slSelects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
			slSelects.add(STR_SELECT_ACCESS_MODIFY);
			BusinessObjectWithSelectList folderContentWithSelectList = null;
			BusinessObjectWithSelect bows = null;
			int size = objectList.size();
			String[] strObjIds = new String[size];
			String strPersonName = (String)context.getUser();
			for(int i=0;i<size;i++)
			{
				Map map = (Map)objectList.get(i);
				String sObjectId = (String)map.get("id");
				strObjIds[i] = sObjectId; 
			}

			folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strObjIds,slSelects);

			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
			{
				bows = itr.obj();
				String sIsProjectSpace = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String sIsProjectTemplate = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
				String sIsProjectConcept = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
				String sIsVersionObject = bows.getSelectData(CommonDocument.SELECT_IS_VERSION_OBJECT);
				String hasAccess = bows.getSelectData(STR_SELECT_ACCESS_MODIFY);
				
				boolean isProject = "true".equalsIgnoreCase(sIsProjectSpace) || "true".equalsIgnoreCase(sIsProjectTemplate) ||"true".equalsIgnoreCase(sIsProjectConcept);
				if(isProject)
					strList.add(false);
				else if("true".equalsIgnoreCase(hasAccess))
					strList.add(true);
				else
					strList.add(false);
			}

			//End Added:PRG:RG6:R212:30-july-2011::IR-117051V6R2012x
			return strList;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}

	/**
	 * This method is used to decide editing access for name and title column in PMCFolderSummary table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - objectList MapList
	 * @throws MatrixException if the operation fails
	 * @since PRG R212
	 * @author RG6
	 * @returns StringList
	 */
	public StringList editAccessToTitleNameColumn(Context context,String[] args)
	throws MatrixException
	{
		try {
			Map programMap =   (Map)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			Map columnMap = (Map)programMap.get("columnMap");
			String sColumnName = (String)columnMap.get("name");
			if(null == objectList)
			{
				throw new IllegalArgumentException("Object id List is null");
			}

			StringList strList = new StringList();
			StringList slSelects = new StringList();
			final String STR_SELECT_ACCESS_MODIFY = "current.access[modify]";
			slSelects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
			slSelects.add(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
			slSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			slSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
			slSelects.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			slSelects.add(STR_SELECT_ACCESS_MODIFY);
			slSelects.add(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
			BusinessObjectWithSelectList folderContentWithSelectList = null;
			BusinessObjectWithSelect bows = null;
			int size = objectList.size();
			String[] strObjIds = new String[size];

			String strPersonName = (String)context.getUser();

			for(int i=0;i<size;i++)
			{
				Map map = (Map)objectList.get(i);
				String sObjectId = (String)map.get("id");
				strObjIds[i] = sObjectId; 
			}

			folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strObjIds,slSelects);
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
			{
				bows = itr.obj();
				String sChildObjId = bows.getSelectData(DomainConstants.SELECT_ID);

				String sIsVersionObject = bows.getSelectData(CommonDocument.SELECT_IS_VERSION_OBJECT);
				String sIsDocuments = bows.getSelectData(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
				String sIsProjectSpace = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String sIsProjectTemplate = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
				String sIsProjectConcept = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
				String hasModifyAccess = bows.getSelectData(STR_SELECT_ACCESS_MODIFY);
				String sIsControlledFolder = bows.getSelectData(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
				
				boolean isProject = "true".equalsIgnoreCase(sIsProjectSpace) || "true".equalsIgnoreCase(sIsProjectTemplate) ||"true".equalsIgnoreCase(sIsProjectConcept);
				boolean isCFTitle = "Title".equalsIgnoreCase(sColumnName) && "true".equalsIgnoreCase(sIsControlledFolder);
				
				if( ("true".equalsIgnoreCase(sIsDocuments)&& "true".equalsIgnoreCase(sIsVersionObject)) || isCFTitle || isProject)
					strList.add(false);
				else if("true".equalsIgnoreCase(hasModifyAccess))
					strList.add(true);
				else
					strList.add(false);
			}		
			return strList;
		}
		catch(Exception ex){
			throw new MatrixException(ex);
		}
	}
	// Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
	/**
	 * getProjectIdFromFolder - get the project id from workspace vault(folder object) from any level.
	 * @param context the eMatrix <code>Context</code> object
	 * @param folderId Workspace Vault object 
	 * @return String project id
	 * @throws MatrixException if the operation fails
	 * @since R212
	 * @author RG6
	 */
	protected String getProjectIdFromFolder(Context context,String folderId) throws MatrixException
	{
		if(ProgramCentralUtil.isNullString(folderId))
		{
			throw new IllegalArgumentException("Folder id is Null"); 
		}

		DomainObject dFolderObj = DomainObject.newInstance(context);
		dFolderObj.setId(folderId);

		String sFromObjectIdFolder =  "to["+DomainConstants.RELATIONSHIP_SUB_VAULTS+"].from.id";
		String sFromObjectIdProject =  "to["+DomainConstants.RELATIONSHIP_PROJECT_VAULTS+"].from.id";
		String sIsFromObjectFolderType =  "to["+DomainConstants.RELATIONSHIP_SUB_VAULTS+"].from.type.kindof["+DomainConstants.TYPE_WORKSPACE_VAULT+"]";
		String sIsFromObjectProjectType =  "to["+DomainConstants.RELATIONSHIP_PROJECT_VAULTS+"].from.type.kindof["+DomainConstants.TYPE_PROJECT_MANAGEMENT+"]";
		String sIsProjectType = "type.kindof["+DomainConstants.TYPE_PROJECT_MANAGEMENT+"]";

		StringList slFolderSelects = new StringList();
		slFolderSelects.add(sFromObjectIdFolder);
		slFolderSelects.add(sFromObjectIdProject);
		slFolderSelects.add(sIsFromObjectFolderType);
		slFolderSelects.add(sIsFromObjectProjectType);
		slFolderSelects.add(sIsProjectType);

		String sReturnProjectId = "";

		if(dFolderObj.isKindOf(context, DomainConstants.TYPE_PROJECT_MANAGEMENT))
		{
			sReturnProjectId =  folderId;
		}
		else
		{
			while(true)
			{
				Map mFolderInfo = dFolderObj.getInfo(context,slFolderSelects);

				String sIsProject = (String)mFolderInfo.get(sIsFromObjectProjectType);
				if("true".equalsIgnoreCase(sIsProject))
				{
					sReturnProjectId = (String) mFolderInfo.get(sFromObjectIdProject);
					break;
				}

				String sNwFolderId =  (String) mFolderInfo.get(sFromObjectIdFolder);
				dFolderObj.setId(sNwFolderId);
			}

		}
		return sReturnProjectId;
	}

	/**
	 * setUserPermissionsToWorkspaceVault - set the user permissions to the workspace vault object by using project.
	 *for folder with inherited access and default access other than none set
	 *all Project members with default user acccess
	 *context user with default user access(if default user access not none) if so then read
	 *onwer with Add Remove access
	 * @param context the eMatrix <code>Context</code> object
	 * @param sWkVaultId Workspace Vault object id
	 * @param sProjId Project id
	 * @return Void
	 * @throws MatrixException if the operation fails
	 * @since R212
	 * @author RG6
	 */
	protected void setUserPermissionsToWorkspaceVault(Context context,String sWkVaultId, String sProjId)  throws MatrixException
	{
		if(ProgramCentralUtil.isNullString(sWkVaultId) || ProgramCentralUtil.isNullString(sProjId))
		{
			throw new IllegalArgumentException("Either Workspace Vault or Project Id is null");
		}

		com.matrixone.apps.program.ProjectSpace project =
			(com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
		com.matrixone.apps.common.WorkspaceVault workspaceVault =
			(com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
					DomainConstants.TYPE_WORKSPACE_VAULT);

		project.setId(sProjId);
		workspaceVault.setId(sWkVaultId);

		final String sSelectAttributeAccessType = WorkspaceVault.SELECT_ACCESS_TYPE; 
		StringList slWkSelect = new StringList();

		slWkSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
		slWkSelect.add(sSelectAttributeAccessType);
		slWkSelect.add(DomainConstants.SELECT_OWNER);
		Map mWkInfo = new HashMap();
		//Added:PRG:R213:
		//as there is no Read and show access to the public user on the Workspace Vault policy
		ContextUtil.pushContext(context,
				DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,
				DomainConstants.EMPTY_STRING,
				DomainConstants.EMPTY_STRING);
		try
		{
			mWkInfo = workspaceVault.getInfo(context, slWkSelect);
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		finally
		{
			ContextUtil.popContext(context);
		}

		String sDefaultAccess = (String)mWkInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
		String accessType     = (String)mWkInfo.get(sSelectAttributeAccessType);
		String sOwner         = (String) (String)mWkInfo.get(DomainConstants.SELECT_OWNER);
		String sContUser      = context.getUser();
		i18nNow i18nnow      = new i18nNow();
		HashMap accessMap    = new HashMap();

		String inheritedType = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Inherited", "en-us");
		String strNoneAccess = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Folder_Access.None", "en-us");

		boolean isDefaultAccessNone =  strNoneAccess.equalsIgnoreCase(sDefaultAccess);
		// check if access type is inherited and default access is not none
		// for context user set the default user access or read whichever is greater
		if(inheritedType.equalsIgnoreCase(accessType) && !isDefaultAccessNone)
		{
			StringList busSels = new StringList();
			busSels.add(project.SELECT_NAME);
			MapList ml = project.getMembers(context,busSels,null,"","",true);
			if(null !=ml)
			{
				int size = ml.size();
				for(int i=0; i < size; i++)
				{
					Map map = (Map)ml.get(i);
					String sMember = (String)map.get(project.SELECT_NAME);
					if(ProgramCentralUtil.isNotNullString(sMember) && !sMember.equals(sOwner))
					{
						// for project member  set the default user access
						accessMap.put(sMember, sDefaultAccess);	
					}
				}
			}
		}

		if(isDefaultAccessNone)  // if default access is none set context user Read access
		{
			accessMap.put(sContUser, AccessUtil.READ);
		}
		else  //set the context user default user access
		{
			accessMap.put(sContUser, sDefaultAccess);
		}
		// set 'add remove' access to owner
		accessMap.put(sOwner, AccessUtil.ADD_REMOVE);
		//as there is no Read and show access to the public user on the Workspace Vault policy
		/*ContextUtil.pushContext(context,
				DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,
				DomainConstants.EMPTY_STRING,
				DomainConstants.EMPTY_STRING);*/
		try
		{
			// set the accesses to the workspace vault object
			workspaceVault.setUserPermissions(context, accessMap);   
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		/*finally
		{
			ContextUtil.popContext(context);
		}*/

	}
	//End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x

	/**
	 * isValidCutPasteOperation - only performing the cut operation will result in disconnection of the object so in order to
	 * check if user has performed the cut/paste operation properly
	 * @param context the eMatrix <code>Context</code> object
	 * @param Element root element object of folder structure browser
	 * @return boolean true if cut/paste operation is valid/ does not result in only disconnection of the object
	 * @since R212
	 * @author RG6
	 */
	protected boolean isValidCutPasteOperation(Context context,com.matrixone.jdom.Element rootElement)
	{
		boolean isValid = true;
		Map mValidityTestBasedOnObjectId = new HashMap();
		int validCutPasteOpeation = 0; //0 = validOperation, 1 = InvalidOpearion
		int stopSearch = 0; //If 1 stop searching
		com.matrixone.jdom.Element rootParent = rootElement.getParentElement();
		if(null!=rootParent)
		{
			java.util.List childList = rootParent.getChildren();
			int lSize = childList != null ? childList.size() : 0;
			for(int i=0;(i< lSize && stopSearch == 0);i++)
			{  
				com.matrixone.jdom.Element e = (com.matrixone.jdom.Element) childList.get(i);
				java.util.List ccElementList = e.getChildren();
				int elSize = ccElementList != null ? ccElementList.size() : 0 ;
				for(int j=0;j < elSize;j++)
				{
					com.matrixone.jdom.Element ccElement  = (com.matrixone.jdom.Element) ccElementList.get(j);
					String operation = ccElement.getAttributeValue("markup");
					String objId = ccElement.getAttributeValue("objectId");
					if(ProgramCentralUtil.isNullString(operation))
					{
						stopSearch = 1;
						break;
					}
					else if("cut".equalsIgnoreCase(operation))
					{
						validCutPasteOpeation++;
						if(mValidityTestBasedOnObjectId.containsKey(objId))
						{
							Integer value = (Integer) mValidityTestBasedOnObjectId.get(objId);
							mValidityTestBasedOnObjectId.put(objId,(Integer.valueOf(value))+1);

						}
						mValidityTestBasedOnObjectId.put(objId,1);
					}
					else if("add".equalsIgnoreCase(operation))
					{
						if(mValidityTestBasedOnObjectId.containsKey(objId))
						{
							Integer value = (Integer) mValidityTestBasedOnObjectId.get(objId);
							mValidityTestBasedOnObjectId.put(objId,(Integer.valueOf(value))-1);
						}
						validCutPasteOpeation--;
					}
					else
					{
						validCutPasteOpeation = 0;
					}
				}
			}
		}

		if (validCutPasteOpeation > 0)
		{
			isValid = false;
		}
		return isValid;     
	}

	/**
	 * getErrorMessageMap - method is used to create error Map object which is used
	 * to show error messages.
	 * @param sKey String error message
	 * @param sContextLang String language in which error message to be displayed
	 * @return Map contaning the ERROR action and i18n value of the key
	 * @deprecated Use String getErrorMessageMap(Context, String , String)
	 */
	protected Map getErrorMessageMap(String sKey,String sContextLang) throws MatrixException{
		return getErrorMessageMap(context, sKey, sContextLang);
	}
	
	protected Map getErrorMessageMap(Context context, String sKey,String sContextLang) throws MatrixException
	{
		Map returnHashMap = new HashMap();
		try
		{
			String notice = ProgramCentralUtil.getPMCI18nString(context, sKey,sContextLang);
			returnHashMap.put("Message", notice);
			returnHashMap.put("Action", "ERROR");
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return(returnHashMap);
	}

	/**
	 * createParentInfoMap - This method is used to create map which contain information about 
	 * type of object passed as parameter.
	 * @param context the eMatrix <code>Context</code> object
	 * @param String objectId
	 * @return Map containing parent type info
	 * @throws MatrixException if the operation fails
	 * @since R212
	 * @author RG6
	 */
	protected Map createParentInfoMap(Context context,String sParentObjId) throws MatrixException
	{
		if(ProgramCentralUtil.isNullString(sParentObjId))
		{
			throw new IllegalArgumentException("Param Parent id is Null");
		}
		Map parentInfoMap = null;
		try
		{
			parentInfoMap = PMCWorkspaceVault.getPMCFolderData(context,sParentObjId); // get parent info map // map storing info related to current root node
			String isParentWorkspaceVault = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			String isParentControlledFolder = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_CONTROLLED_FOLDER);
			// Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
			String isParentProjectSpace = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isParentProjectConcept = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
			String isParentProjectTemplate = (String)parentInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

			boolean isParentProjectType = "true".equalsIgnoreCase(isParentProjectSpace) 
			|| "true".equalsIgnoreCase(isParentProjectConcept) 
			|| "true".equalsIgnoreCase(isParentProjectTemplate);

			parentInfoMap.put("isParentProjectType", isParentProjectType);
			//End Added :4-May-2011:PRG:RG6:DOCMGNT IR-106710V6R2012x
		}
		catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return parentInfoMap;
	}

	/**
	 * performCutPasteoperation - This method  collect the object for cut/paste operation based on the markups
	 * and calls the corrosponding method.
	 * @param context the eMatrix <code>Context</code> object
	 * @param List Hold the SB object to be cut/paste
	 * @param Map parentInfoMap parent type info Map
	 * @param Map argsParamMap  argument map
	 * @return Map containing success or error info 
	 * @throws MatrixException if the operation fails
	 * @since R212
	 * @author RG6
	 */
	protected Map performCutPasteOperation(Context context,List lCElement,Map parentInfoMap,Map argsParamMap) throws MatrixException
	{
		StringList slCutObjects = new StringList();  // list of objects on which cut operation is performed
		StringList slPasteObjects = new StringList(); //// list of objects on which paste operation is performed
		Map mRelIds = new HashMap();
		String sContextLang = context.getSession().getLanguage();
		Iterator itrC  = lCElement.iterator();
		Map returnMap = new HashMap();
		try
		{
			while(itrC.hasNext())
			{
				com.matrixone.jdom.Element childCElement = (com.matrixone.jdom.Element)itrC.next();
				String sObjectId = (String)childCElement.getAttributeValue("objectId");
				String sRelId = (String)childCElement.getAttributeValue("relId");
				String sRowId = (String)childCElement.getAttributeValue("rowId");
				String markup    = (String)childCElement.getAttributeValue("markup");
				Map mChildInfoMap = new HashMap(); // store the above child info
				mChildInfoMap.put("oid", sObjectId);
				mChildInfoMap.put("rowId", sRowId);
				mChildInfoMap.put("relid", sRelId);
				mChildInfoMap.put("markup", markup);

				if ("resequence".equals(markup))
				{
					return getErrorMessageMap(context, "emxProgramCentral.Folders.ResequenceObject.OperationNotAllowedNotice",sContextLang);
				}
				else if ("add".equals(markup))
				{
					slPasteObjects.add(sObjectId);
					mRelIds.put(sObjectId, mChildInfoMap);
				}
				else if ("cut".equals(markup))
				{
					slCutObjects.add(sObjectId);
					mRelIds.put(sObjectId, mChildInfoMap);
				}
			}

			// call the method to disconnect the objects
			if(slCutObjects.size()>0){
				Map mparamMap = new HashMap();
				mparamMap.put("slCutObject",slCutObjects);
				mparamMap.put("mRelID",mRelIds);
				mparamMap.put("parentInfoMap",parentInfoMap);
				mparamMap.put("argsParamMap",argsParamMap);
				if(slPasteObjects.isEmpty()){
					return  cutObjectsInSB(context,mparamMap);
				}else{
					cutObjectsInSB(context,mparamMap);
				}
			}
			// call method to reconnect the objects
			if(slPasteObjects.size()>0){
				Map mparamMap = new HashMap();
				mparamMap.put("slPasteObject",slPasteObjects);
				mparamMap.put("mRelID",mRelIds);
				mparamMap.put("parentInfoMap",parentInfoMap);
				mparamMap.put("argsParamMap",argsParamMap);
				return  pasteObjectsToSB(context,mparamMap);
			}
		}
		catch(Exception e)
		{
			throw new MatrixException(e); 
		}

		return returnMap;
	}

	/**
	 * isCreateWorksapceVaultCommandAccessible - This Method will check the current
	 * user has access to create folders.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Void
	 * @throws Exception if the operation fails
	 * @since R213
	 * @author RG6 
	 */
	public  boolean isCreateWorksapceVaultCommandAccessible(Context context, String args[])throws MatrixException{

		boolean retResult = false;

		try{

			Map programMap = (Map) JPO.unpackArgs(args);
			String sObjectId = (String)programMap.get("objectId");
			if(null == sObjectId || "".equalsIgnoreCase(sObjectId)){
				return retResult;
			}

			String contUser = context.getUser();
			ProjectSpace project =
				(ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

			DomainObject dDomObj =  DomainObject.newInstance(context);
			dDomObj.setId(sObjectId);
			// for project space allow create for project user
			if(dDomObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)){
				project.setId(sObjectId);

				com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
				StringList projectMemberSelects = new StringList(1);
				projectMemberSelects.add(person.SELECT_NAME);
				MapList mlMember = project.getMembers(context, projectMemberSelects,null, null, null);
				Iterator itMember = mlMember.iterator();

				while(itMember.hasNext()){
					Map mapPerson = (Map)itMember.next();
					String persName = (String)mapPerson.get(person.SELECT_NAME);
					if(persName != null && persName.equalsIgnoreCase(contUser)){
						retResult = true;
						break;
					}
				}
			}
			else{  // access exp as before check for create command 
				StringList strObjectSelect = new StringList();
				final String STR_SELECT_ACCESS_FROMCONNECT = "current.access[fromconnect]";
				final String STR_SELECT_ACCESS_MODIFY = "current.access[modify]";
				strObjectSelect.add(STR_SELECT_ACCESS_FROMCONNECT);
				strObjectSelect.add(STR_SELECT_ACCESS_MODIFY);
				strObjectSelect.add(DomainConstants.SELECT_CURRENT);

				Map mapObjSelInfo = dDomObj.getInfo(context, strObjectSelect);

				String strAccessFromConnect =  (String)mapObjSelInfo.get(STR_SELECT_ACCESS_FROMCONNECT);
				String strAccessModify = (String)mapObjSelInfo.get(STR_SELECT_ACCESS_MODIFY);

				if("true".equalsIgnoreCase(strAccessModify) && "true".equalsIgnoreCase(strAccessFromConnect)){

					if(dDomObj.isKindOf(context, DomainConstants.TYPE_CONTROLLED_FOLDER)){
						String strCFState = (String)mapObjSelInfo.get(DomainConstants.SELECT_CURRENT);
						if(null != strCFState && !"".equalsIgnoreCase(strCFState) && DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(strCFState)){
							return true;
						}else{
							return false;
						}
					}

					return true;
				}
				else{
					retResult = false;
				}

			}

		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return retResult;
	}

	/**
	 * isDeleteWorksapceVaultCommandAccessible - This Method will check the current
	 * user has access to delete folders. 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return Void
	 * @throws Exception if the operation fails
	 * @since R213
	 * @author RG6 
	 */
	public  boolean isDeleteWorksapceVaultCommandAccessible(Context context, String args[])throws MatrixException{

		boolean retResult = false;

		try{

			Map programMap = (Map) JPO.unpackArgs(args);
			String sObjectId = (String)programMap.get("objectId");
			if(null == sObjectId || "".equalsIgnoreCase(sObjectId)){
				return retResult;
			}

			String contUser = context.getUser();
			ProjectSpace project =
				(ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

			DomainObject dDomObj =  DomainObject.newInstance(context);
			dDomObj.setId(sObjectId);
			// for project space allow create for project user
			if(dDomObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)){
				project.setId(sObjectId);

				com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
				StringList projectMemberSelects = new StringList(1);
				projectMemberSelects.add(person.SELECT_NAME);
				MapList mlMember = project.getMembers(context, projectMemberSelects,null, null, null);
				Iterator itMember = mlMember.iterator();

				while(itMember.hasNext()){
					Map mapPerson = (Map)itMember.next();
					String persName = (String)mapPerson.get(person.SELECT_NAME);
					if(persName != null && persName.equalsIgnoreCase(contUser)){
						retResult = true;
						break;
					}
				}
			}
			else{  // access exp as before check for create command 
				StringList strObjectSelect = new StringList();
				final String STR_SELECT_ACCESS_FROMDISCONNECT = "current.access[fromdisconnect]";
				final String STR_SELECT_ACCESS_MODIFY = "current.access[modify]";
				strObjectSelect.add(STR_SELECT_ACCESS_FROMDISCONNECT);
				strObjectSelect.add(STR_SELECT_ACCESS_MODIFY);
				strObjectSelect.add(DomainConstants.SELECT_CURRENT);

				Map mapObjSelInfo = dDomObj.getInfo(context, strObjectSelect);

				String strAccessFromDisConnect =  (String)mapObjSelInfo.get(STR_SELECT_ACCESS_FROMDISCONNECT);
				String strAccessModify = (String)mapObjSelInfo.get(STR_SELECT_ACCESS_MODIFY);

				if("true".equalsIgnoreCase(strAccessModify) && "true".equalsIgnoreCase(strAccessFromDisConnect)){

					if(dDomObj.isKindOf(context, DomainConstants.TYPE_CONTROLLED_FOLDER)){
						String strCFState = (String)mapObjSelInfo.get(DomainConstants.SELECT_CURRENT);
						if(null != strCFState && !"".equalsIgnoreCase(strCFState) && DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(strCFState)){
							return true;
						}else{
							return false;
						}
					}

					return true;
				}
				else{
					retResult = false;
				}

			}

		}catch(Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return retResult;
	}

	/** This method checks Commands Push Subscription accesses
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the input arguments:
	 * @return true if logged in person has Project Lead or has Project Lead or Project Owner project access
	 *         false otherwise
	 * @throws Exception
	 */
	public boolean hasAccessOnPushSubscription(Context context, String[] args)throws Exception
	{
		boolean hasAccess = false;
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map objectList = (Map) programMap.get("objectList");		
			String strObjectId = (String)programMap.get("objectId");
			if(null == strObjectId || "".equalsIgnoreCase(strObjectId)){
				throw new IllegalArgumentException();
			}
			String strProjectAdmin = ProgramCentralConstants.ROLE_PROJECT_ADMINISTRATOR;
			String strProjectLead = ProgramCentralConstants.ROLE_PROJECT_LEAD;
			String strProjectOwner = ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER;
			boolean isProjectAdmin = PersonUtil.getAssignments(context).contains(strProjectAdmin);
			boolean isProjectLead = PersonUtil.getAssignments(context).contains(strProjectLead);
			boolean isLeadOrOwner = false;
			StringList slSelected = new StringList();   //To get Type, isProjectSpce, isWorkspaceVault etc.
			slSelected.add(DomainConstants.SELECT_TYPE);
			slSelected.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			slSelected.add(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			DomainObject dObj = DomainObject.newInstance(context, strObjectId);
			Map mpSelected = dObj.getInfo(context, slSelected);
			String strType = (String)mpSelected.get(DomainConstants.SELECT_TYPE);
			String isProject = (String)mpSelected.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isWorkspaceVault = (String)mpSelected.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
			if(ProgramCentralUtil.isNotNullString(strType) && dObj.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE) && "TRUE".equalsIgnoreCase(isProject))
			{
				ProjectSpace project = new ProjectSpace(strObjectId);
				String strAccess = project.getAccess(context);
				if(strProjectLead.equals(strAccess) || strProjectOwner.equals(strAccess))
				{
					isLeadOrOwner = true;
				}
			}
			else if(ProgramCentralUtil.isNotNullString(strType) && dObj.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT) && "TRUE".equalsIgnoreCase(isWorkspaceVault))
			{
				String strProjectId = getProjectIdFromFolder(context, strObjectId);
				ProjectSpace project = new ProjectSpace(strProjectId);
				String strAccess = project.getAccess(context);
				if(strProjectLead.equals(strAccess) || strProjectOwner.equals(strAccess))
				{
					isLeadOrOwner = true;
				}
			}
			//IR-151631V6R2013x Begin
			else if(ProgramCentralUtil.isNotNullString(strType) && dObj.isKindOf(context, DomainConstants.TYPE_PROJECT_TEMPLATE) && "FALSE".equalsIgnoreCase(isProject))
			{
				if(isProjectAdmin && dObj.getOwner(context).getName().equalsIgnoreCase(context.getUser())) {
					hasAccess = true;
				}
			}
			else if(ProgramCentralUtil.isNotNullString(strType) && dObj.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT) && "FALSE".equalsIgnoreCase(isProject))	
			{
				ProjectSpace project = new ProjectSpace(strObjectId);
				String strAccess = project.getAccess(context);
				if(strProjectLead.equals(strAccess) || strProjectOwner.equals(strAccess))
				{
					isLeadOrOwner = true;
				}
			}
			//if(isProjectLead || isLeadOrOwner || isProjectAdmin)
			if(isLeadOrOwner)
			{
				hasAccess = true;
			}//IR-151631V6R2013x end

		}catch(Exception e)
		{
			throw new MatrixException(e);
		}
		return hasAccess;
	}
	/**
	 * To check if route is connected to Document or not
	 * @param context
	 * @param args
	 * @return returns true if route is connected to document.
	 * @throws Exception
	 */
   //START:16/5/2013:Added for IR-225742V6R2014x 
    public  boolean isContentWithRouteScope(Context context, String args[]) throws Exception {
        
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		StringList routeIdList = (StringList)programMap.get("routeIdList");		
		if(routeIdList == null || routeIdList.size()<=0){
	           return false;
	       }
        
        String []routeObjectArray = ComponentsUIUtil.stringToArray(FrameworkUtil.join(routeIdList,","), ",");
        
        StringList selectRouteList = new StringList();
        String routeStatus = PropertyUtil.getSchemaProperty(context,"attribute_RouteStatus");
        selectRouteList.add("to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"]");
        selectRouteList.add("attribute["+routeStatus+"]");
        MapList routeObjectMapList = DomainObject.getInfo(context,routeObjectArray,selectRouteList);
        
        Iterator routeObjectMapListItr = routeObjectMapList.iterator();
        while(routeObjectMapListItr.hasNext()){
            Map strResult = (Map)routeObjectMapListItr.next();
            routeStatus = (String) strResult.get("attribute["+routeStatus+"]");
            String relRouteScope = (String) strResult.get("to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"]");
            if(relRouteScope != null && "TRUE".equalsIgnoreCase(relRouteScope) && !("Finished".equalsIgnoreCase(routeStatus))){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Trigger executed when user change mode of inherit to specific in folder object.
     * @param context The ENOVIA <code>Context</code> object.
     * @param args holds information about project.
     * @return return integer value.
     * @throws Exception if operation fails.
     */
    public int triggerAccessTypeModifyAction(Context context, String[] args) throws Exception
	{
		String busId = args[0];
		String accessType = args[1];
		
		String parentProjectId = "to[" + DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "].from.id" ;
		String parentFolderId = "to[" + DomainConstants.RELATIONSHIP_SUB_VAULTS + "].from.id" ;

		DomainObject domObj = DomainObject.newInstance(context, busId);

		if(domObj.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT)){
			StringList busSelects = new StringList(2);
			busSelects.add(parentProjectId);
			busSelects.add(parentFolderId);

			Map parentIds = domObj.getInfo(context, busSelects);
			String parentId = (String) parentIds.get(parentProjectId);
			
			if(ProgramCentralUtil.isNullString(parentId)){
				parentId = (String) parentIds.get(parentFolderId);
			}

			if("Inherited".equalsIgnoreCase(accessType)){
				DomainAccess.createObjectOwnership(context, busId, parentId, null, false);
			}else{
				DomainAccess.deleteObjectOwnership(context, busId, parentId, null, false);
			}
		}else if(domObj.isKindOf(context, DomainConstants.TYPE_DOCUMENT)){
   		 // add the originator to the multiple ownership access list in case of specific document
   		if("Specific".equalsIgnoreCase(accessType)){
			DomainAccess.createObjectOwnership(context,busId, PersonUtil.getPersonObjectID(context), DomainAccess.getOwnerAccessName(context, busId),DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
   		}
	  }
		return 0;
	}

	/**
	 * Gets HTML Formatted Names of Folders for the AEF Collection table. 
	 * @param context the ENOVIA Context object
	 * @param args 
	 * @return A List of names of folders in HTML format. 
	 * @throws Exception if operation fails.
	 */
	public Vector getCollectionNames(Context context, String[] args) throws Exception {
		try {
			String strObjectId = ProgramCentralConstants.EMPTY_STRING;
			String strName = ProgramCentralConstants.EMPTY_STRING;
			String strType = ProgramCentralConstants.EMPTY_STRING;
			String strSuiteDir = ProgramCentralConstants.EMPTY_STRING;
			final String TYPE_CONTROLLED_FOLDER = PropertyUtil.getSchemaProperty(context, "type_ControlledFolder");

			Vector vecResult = new Vector();

			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			HashMap paramList = (HashMap)programMap.get("paramList");
			String strNodeId = (String)paramList.get("jsTreeID");

			DomainObject domObject = DomainObject.newInstance(context);
			DomainObject domParentObject = DomainObject.newInstance(context);
			String reportFormat = (String)paramList.get("reportFormat");
			boolean isprinterFriendly = false;
			if(reportFormat != null)
				isprinterFriendly = true;

			StringList slSelectables = new StringList(3);
			slSelectables.addElement(DomainConstants.SELECT_POLICY);
			slSelectables.addElement(DomainConstants.SELECT_NAME);
			slSelectables.addElement(DomainConstants.SELECT_TYPE);
			slSelectables.addElement(DomainConstants.SELECT_ID);

			Map mapObjectInfon = null;
			MapList mlObjectInfo = new MapList();
			int iListSize = objectList.size();
			String[] saObjectIds = new String[iListSize];
			StringList slObjectIds = new StringList();
			for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
				mapObjectInfon = (Map) itrObjects.next();
				slObjectIds.addElement((String)mapObjectInfon.get(DomainConstants.SELECT_ID));                 
			}
			slObjectIds.toArray(saObjectIds);
			mlObjectInfo = DomainObject.getInfo(context, saObjectIds, slSelectables);
			String strRelatioshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "," + DomainConstants.RELATIONSHIP_SUB_VAULTS;
			String strTypePattern = DomainConstants.TYPE_PROJECT_MANAGEMENT;
			StringList slBusSelect = new StringList();
			slBusSelect.add(DomainConstants.SELECT_ID);
			slBusSelect.add(DomainConstants.SELECT_TYPE);
			StringList slRelSelect = new StringList();

			StringList slFoldersTypes = ProgramCentralUtil.getSubTypesList(context, DomainConstants.TYPE_PROJECT_VAULT);

			for (Iterator itrObjects = mlObjectInfo.iterator(); itrObjects.hasNext();) {
				strSuiteDir = "common";
				Map mObject = null;
				mObject = (Map)itrObjects.next();
				strObjectId = (String)mObject.get(DomainConstants.SELECT_ID);
				strName = (String)mObject.get(DomainConstants.SELECT_NAME);
				strType = (String)mObject.get(DomainConstants.SELECT_TYPE);

				if(slFoldersTypes.contains(strType)) {
					domObject.setId(strObjectId);
					MapList mlParent = domObject.getRelatedObjects(context,     // context.
							strRelatioshipPattern,   							// rel filter.
							DomainConstants.QUERY_WILDCARD,						// type filter
							slBusSelect,  										// business object selectables.
							slRelSelect,           								// relationship selectables.
							true,          										// expand to direction.
							false,           									// expand from direction.
							(short)0,  											// level
							null,           									// object where clause
							null,												// relationship where clause
							0);          										//limit

					Map mapParent = null; 
					String strParentid = DomainConstants.EMPTY_STRING;
					for (Iterator itrParent = mlParent.iterator(); itrParent.hasNext();) {
						mapParent = (Map) itrParent.next();
						if(mapParent != null) {
							strParentid = (String)mapParent.get(DomainConstants.SELECT_ID);
							domParentObject.setId(strParentid);

							if(domParentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE) || domParentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_TEMPLATE)
									|| domParentObject.isKindOf(context, DomainConstants.TYPE_PROJECT_CONCEPT)) {
								strSuiteDir = "programcentral";
								break;
							}
						}
					}    				
				}
				String strValue = ProgramCentralConstants.EMPTY_STRING;

				if(!isprinterFriendly) {
					strValue = "<a title=\"" + XSSUtil.encodeForXML(context, strName) + "\" href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory="+strSuiteDir+"&amp;jsTreeID="+strNodeId+"&amp;objectId="+strObjectId+"', 800,600, 'false', 'content', '', 'Normal', 'false')\">" ;
					strValue+=  XSSUtil.encodeForXML(context, strName);
				}
				else {
					strValue+= strName;
				}
				if(!isprinterFriendly) {
					strValue += "</a>";
				}
				vecResult.add(strValue);
			}

			return vecResult;
		}
		catch(Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	/**
	 * DnD column avalibale only Project Folder.
	 * @param context the ENOVIA Context object
	 * @param args Contains the information about objects.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean isDnDAvaliableForTemplate(Context context,String[]args)throws Exception
	{
		boolean isAvaliable = true;
		Map programMap =  JPO.unpackArgs(args);
		String rootObjectId = (String)programMap.get("objectId");
		
		StringList busSelect  = new StringList(2);
		busSelect.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		
		if(ProgramCentralUtil.isNotNullString(rootObjectId)){
		String []objArrId = new String[1];
		objArrId[0] = rootObjectId;
		
		MapList objectInfoList = DomainObject.getInfo(context, objArrId, busSelect);
		Map objectInfoMap = (Map)objectInfoList.get(0);
		
		String isProjectTemplate = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		String isWorkspaceVault = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		
		if("true".equalsIgnoreCase(isProjectTemplate)){
			isAvaliable = false;
		}else if("true".equalsIgnoreCase(isWorkspaceVault)){
			String isParentProjectTemplate = "to[" + DomainConstants.RELATIONSHIP_PROJECT_VAULTS + "].from.type.kindof["+DomainObject.TYPE_PROJECT_TEMPLATE+"]";
			String strRelatioshipPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS + ","+DomainConstants.RELATIONSHIP_SUB_VAULTS;
			String strTypePattern = DomainConstants.TYPE_WORKSPACE_VAULT + "," + ProgramCentralConstants.TYPE_CONTROLLED_FOLDER+ "," + DomainObject.TYPE_PROJECT_TEMPLATE;

			DomainObject folder = DomainObject.newInstance(context, rootObjectId);
			busSelect.clear();
			busSelect.addElement(isParentProjectTemplate);

			MapList mlFolders = folder.getRelatedObjects(context,  
					strRelatioshipPattern,
					strTypePattern,
					busSelect,
					null,
					true,
					true,
					(short)0,
					null,
					null,
					0);    

			for(int i=0;i<mlFolders.size();i++){
				Map folderMap = (Map)mlFolders.get(i);
				String isProjectTemplateObject = (String)folderMap.get(isParentProjectTemplate);
				if("true".equalsIgnoreCase(isProjectTemplateObject)){
					isAvaliable = false;
					break;
				}
			}
		}
		}else{
			isAvaliable = false;
		}
		
		return isAvaliable;
	}

	/**
	 * Folder customize type field is avaliable if selected type is Workspace Vault object. 
	 * @param context the ENOVIA Context object
	 * @param args holds information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean isCustomizeTypeFieldAvaliable(Context context,String[] args)throws Exception
	{
		boolean isAvaliable = false;
		
		Map programMap =  JPO.unpackArgs(args);
		String isClone = (String)programMap.get("IsClone");
		String objectId = (String)programMap.get("objectId");
		if(ProgramCentralUtil.isNotNullString(isClone) && "true".equalsIgnoreCase(isClone)) {
			objectId = (String)programMap.get("oldObjectId");
		}
		
		StringList busSelect  = new StringList(4);
		busSelect.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		
		if(ProgramCentralUtil.isNotNullString(objectId)){
		String []objArrId = new String[1];
			objArrId[0] = objectId;
		
		MapList objectInfoList = DomainObject.getInfo(context, objArrId, busSelect);
		Map objectInfoMap = (Map)objectInfoList.get(0);
		
		String isProjectTemplate = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		String isProjectSpace = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		String isProjectConcept = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		String isWorkspaceVault = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		
		if("true".equalsIgnoreCase(isProjectTemplate) ||"true".equalsIgnoreCase(isProjectSpace)
				||"true".equalsIgnoreCase(isProjectConcept)){
			isAvaliable = false;
		}else if("true".equalsIgnoreCase(isWorkspaceVault)){
			isAvaliable = true;
		}
		}
		
		return isAvaliable;
	}
	
	/**
	 * Folder Basic type field is avaliable if selected type is Project space,Template and Concept object. 
	 * @param context the ENOVIA Context object
	 * @param args holds information about object.
	 * @return boolean value.
	 * @throws Exception if operation fails.
	 */
	public boolean isBasicTypeFieldAvaliable(Context context,String[] args)throws Exception
	{
		boolean isAvaliable = true;
		
		Map programMap =  JPO.unpackArgs(args);
		String isClone = (String)programMap.get("IsClone");
		String objectId = (String)programMap.get("objectId");
		if(ProgramCentralUtil.isNotNullString(isClone) && "true".equalsIgnoreCase(isClone)) {
			objectId = (String)programMap.get("oldObjectId");
		}
		
		
		StringList busSelect  = new StringList(4);
		busSelect.addElement(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		
		if(ProgramCentralUtil.isNotNullString(objectId)){
		String []objArrId = new String[1];
			objArrId[0] = objectId;
		
		MapList objectInfoList = DomainObject.getInfo(context, objArrId, busSelect);
		Map objectInfoMap = (Map)objectInfoList.get(0);
		
		String isProjectTemplate = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_KINDOF_PROJECT_TEMPLATE);
		String isProjectSpace = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		String isProjectConcept = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
		String isWorkspaceVault = (String)objectInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
		
		if("true".equalsIgnoreCase(isProjectTemplate) ||"true".equalsIgnoreCase(isProjectSpace)
				||"true".equalsIgnoreCase(isProjectConcept)){
			isAvaliable = true;
		}else if("true".equalsIgnoreCase(isWorkspaceVault)){
			isAvaliable = false;
		}
		}
		
		return isAvaliable;
	}
	
	/**
     * Create a folder object.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.CreateProcessCallable  
    public Map createFolder(Context context,String[]args)throws Exception
    {
    	Map <String,String>returnMap = new HashMap();
    	try{
    		ContextUtil.startTransaction(context, true);
    		Map programMap 					= (HashMap) JPO.unpackArgs(args);

    		String folderName 				= (String)programMap.get("Name");
    		String folderDescrption 		= (String)programMap.get("Description");
    		String folderVault 				= (String)programMap.get("Vault");
    		String folderPolicy 			= (String)programMap.get("Policy");
    		String folderType 				= (String)programMap.get("TypeActual");

    		DomainObject folder = DomainObject.newInstance(context);

    		String revision = DomainObject.EMPTY_STRING;
    		if (!folderPolicy.equals(DomainConstants.POLICY_CONTROLLED_FOLDER)){
    			revision = folder.getUniqueName(DomainObject.EMPTY_STRING);
    		}

    		PropertyUtil.setGlobalRPEValue(context,DomainAccess.IS_PRG_FOLDER_CREATION, "true");
    		folder.createObject(context,
    				folderType,
    				folderName,
    				revision,
    				folderPolicy,
    				folder.getDefaultVault(context, null));

    		String newFolderId = folder.getId(context);

    		returnMap.put("id", newFolderId);

    		ContextUtil.commitTransaction(context);
    	}catch(Exception ex){
    		ContextUtil.abortTransaction(context);
    		ex.printStackTrace();
    	}

    	return returnMap;
    }
    
    /**
     * Display drop Icon.
     * @param context - The eMatrix <code>Context</code> object.
     * @param args - The args holds information about object.
     * @return list of drop icon.
     * @throws Exception if operation fails.
     */
    public StringList columnDropZone(Context context, String[] args) throws Exception 
    {
    	StringList dropIconList = new StringList();
    	emxGenericColumns_mxJPO genericColumn = new emxGenericColumns_mxJPO(context,args);
    	Vector columnIconList = genericColumn.columnDropZone(context, args);
    	
    	Map programMap 	    = (HashMap) JPO.unpackArgs(args);
    	MapList objectList  = (MapList)programMap.get("objectList");
    	
    	if(objectList != null && objectList.size()>0){
    		
    	String[] objectIdArray = new String[objectList.size()];
    	for (int i=0; i<objectList.size(); i++) {
			Map objectMap = (Map) objectList.get(i);
			String objectId = (String)objectMap.get(DomainObject.SELECT_ID);
			objectIdArray[i] = objectId;
		}
    	
    		StringList dropAccesList = new StringList(3);
    		dropAccesList.addElement("modify");
    		dropAccesList.addElement("fromconnect");
    		dropAccesList.addElement("toconnect");
    		
    		StringList busSelect = new StringList(3);
    	busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
    	busSelect.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
    		busSelect.addElement("current.access");
    	
    	MapList objectInfoList = DomainObject.getInfo(context, objectIdArray, busSelect);
    	
    	for(int i=0;i<objectInfoList.size();i++){
    			Map objectMap = (Map)objectInfoList.get(i);
    		String dropIcon = (String)columnIconList.get(i);
    		
    			String isProject = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
    			String isWorkspaceVault = (String)objectMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
    			String access = (String)objectMap.get("current.access");
    			
    			boolean showDropIcon = false;
    			StringList currentAccessList = new StringList();
    			if(ProgramCentralUtil.isNotNullString(access)&& !access.equalsIgnoreCase("all")){
    				currentAccessList = FrameworkUtil.split(access, ProgramCentralConstants.COMMA);
    				
    				if(currentAccessList.containsAll(dropAccesList)){
    					showDropIcon = true;
    				}
    			}else if(access.equalsIgnoreCase("all")){
    				showDropIcon = true;
    			}
    		
    		if("true".equalsIgnoreCase(isWorkspaceVault)|| "true".equalsIgnoreCase(isProject)){

    				if(showDropIcon){
    			dropIconList.addElement(dropIcon);
    		}else{
    			dropIconList.addElement(DomainObject.EMPTY_STRING);
    		}

    			}else{
    				dropIconList.addElement(DomainObject.EMPTY_STRING);
    			}
    	}
    	}
    	return dropIconList;
    }
    
    /**
     * Display drag Icon.
     * @param context - The eMatrix <code>Context</code> object.
     * @param args - The args holds information about object.
     * @return list of drag icon.
     * @throws Exception if operation fails.
     */
    public StringList columnDragIcon(Context context, String[] args) throws Exception 
    {
    	StringList dragIconList = new StringList();
    	emxGenericColumns_mxJPO genericColumn = new emxGenericColumns_mxJPO(context,args);
    	Vector columnIconList = genericColumn.columnDragIcon(context, args);
    	
    	Map programMap 	    = (HashMap) JPO.unpackArgs(args);
    	MapList objectList  = (MapList)programMap.get("objectList");
    	
    	if(objectList != null && objectList.size()>0){
    		
    	String[] objectIdArray = new String[objectList.size()];
    	for (int i=0; i<objectList.size(); i++) {
			Map objectMap = (Map) objectList.get(i);
			String objectId = (String)objectMap.get(DomainObject.SELECT_ID);
			objectIdArray[i] = objectId;
		}
    	
    		StringList dragAccesList = new StringList(5);
    		dragAccesList.addElement("modify");
    		dragAccesList.addElement("fromconnect");
    		dragAccesList.addElement("toconnect");
    		dragAccesList.addElement("fromdisconnect");
    		dragAccesList.addElement("todisconnect");
    		
    		StringList busSelect = new StringList(5);
    	busSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
    	busSelect.addElement(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
    	busSelect.addElement(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
    	busSelect.addElement(CommonDocument.SELECT_IS_VERSION_OBJECT);
    		busSelect.addElement("current.access");
    	
    	MapList objectInfoList = DomainObject.getInfo(context, objectIdArray, busSelect);
    	
    	for(int i=0;i<objectList.size();i++){
    		boolean isVersionObject = false;
    			Map objectMap = (Map)objectList.get(i);
    		Map obejectInfoMap = (Map)objectInfoList.get(i);
    		String dropIcon = (String)columnIconList.get(i);
    		
    		String isProject = (String)obejectInfoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
    		String isWorkspaceVault = (String)obejectInfoMap.get(ProgramCentralConstants.SELECT_IS_WORKSPACE_VAULT);
    		String isDocument = (String)obejectInfoMap.get(ProgramCentralConstants.SELECT_IS_DOCUMENTS);
    		String isVersionAbleObject = (String)obejectInfoMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
    			String level = (String)objectMap.get(DomainObject.SELECT_LEVEL);
    			String isRootNode = (String)objectMap.get("Root Node");
    		
    		if(ProgramCentralUtil.isNotNullString(isVersionAbleObject)){
    			isVersionObject = Boolean.parseBoolean(isVersionAbleObject);
    		}
    		
    			boolean showDropIcon = false;
    			String access = (String)obejectInfoMap.get("current.access");
    			StringList currentAccessList = new StringList();
    			
    			if(ProgramCentralUtil.isNotNullString(access) && !access.equalsIgnoreCase("all")){
    				currentAccessList = FrameworkUtil.split(access, ProgramCentralConstants.COMMA);
    				
    				if(currentAccessList.containsAll(dragAccesList)){
    					showDropIcon = true;
    				}
    			}else if(access.equalsIgnoreCase("all")){
    				showDropIcon = true;
    			}

    		if("true".equalsIgnoreCase(isRootNode) && "0".equalsIgnoreCase(level)){
    			dragIconList.addElement(DomainObject.EMPTY_STRING);
    		}else if("true".equalsIgnoreCase(isWorkspaceVault) || ("true".equalsIgnoreCase(isDocument)&& !isVersionObject)){

    				if(showDropIcon){
    			dragIconList.addElement(dropIcon);
    		}else{
    			dragIconList.addElement(DomainObject.EMPTY_STRING);
    		}
    				
    			}else{
    				dragIconList.addElement(DomainObject.EMPTY_STRING);
    			}
    	}
    	}
    	
    	return dragIconList;
    }
    

    /**
     * Access Function for all the action commands related to Folder and Document.
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public boolean hasAccessForFolderAndDocumentActions(Context context , String [] args) throws Exception
    {
    	boolean hasAccess = true;
    	ArrayList noMobileCheckCmdList = new ArrayList(); //Document specific Actions
    	noMobileCheckCmdList.add("PMCCreateNewFolderDocument");
    	noMobileCheckCmdList.add("PMCContentAddProjectContentActionLink");
    	noMobileCheckCmdList.add("PMCDocumentContentUploadFiles");
    	noMobileCheckCmdList.add("PMCContentAddToFolderActionLink");
    	noMobileCheckCmdList.add("PMCDocumentRemove");
    	
    	Map programMap = (Map) JPO.unpackArgs(args);
		HashMap settingsMap = (HashMap) programMap.get("SETTINGS");
		String commandName = (settingsMap != null) ?(String)settingsMap.get("CmdName") : DomainConstants.EMPTY_STRING;
    	String objectId= (String)programMap.get("objectId");
    	String isRMB = (String)programMap.get("isRMB");
    	objectId = (Boolean.valueOf(isRMB))?(String)programMap.get("RMBID") : objectId;
    	
    	StringList busSelects1 = new StringList();
    	busSelects1.add(DomainConstants.SELECT_CURRENT);
    	busSelects1.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
    	busSelects1.add("current.access");
    	busSelects1.add("current.revisionable");
    	busSelects1.add("vcfile");
    	busSelects1.add(CommonDocument.SELECT_VCFOLDER);
    	busSelects1.add(CommonDocument.SELECT_VCMODULE);
    	busSelects1.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
    	busSelects1.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
    	busSelects1.add("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"]");
    	
    	DomainObject dmObject = DomainObject.newInstance(context, objectId);
    	Map infoMap = dmObject.getInfo(context, busSelects1);
    	String currState = (String) infoMap.get(DomainConstants.SELECT_CURRENT);
    	String type = (String) infoMap.get(DomainConstants.SELECT_TYPE);
    	String isKindOfTemplate = (String) infoMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
    	String currAccess = (String) infoMap.get("current.access");
    	String currRevisionable = (String) infoMap.get("current.revisionable");
    	boolean vcfile = Boolean.valueOf((String) infoMap.get("vcfile"));
    	boolean vcfolder = Boolean.valueOf((String) infoMap.get(CommonDocument.SELECT_VCFOLDER));
    	boolean vcmodule = Boolean.valueOf((String) infoMap.get(CommonDocument.SELECT_VCMODULE));
    	boolean isVersionObject = Boolean.valueOf((String) infoMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT));
    	boolean suspendVersioning = Boolean.valueOf((String) infoMap.get(CommonDocument.SELECT_SUSPEND_VERSIONING));
    	String isConnectedToRoute = (String) infoMap.get("from["+DomainConstants.RELATIONSHIP_OBJECT_ROUTE+"]");
    	
    	//Template check starts
    	if(Boolean.valueOf(isKindOfTemplate)) {
    		if(noMobileCheckCmdList.indexOf(commandName) == -1 ){
    			String isMobile = context.getCustomData("isMobile");
    			if(isMobile != null && "true".equalsIgnoreCase(isMobile)){
    				return false;
    			}
    		}
	    	ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
	 		if(!projectTemplate.isOwnerOrCoOwner(context, objectId)){
	 			return false;
	 		}
    	} else {//Required from Folder/Sub-Folder summary page.
    		StringList busSelects2 = new StringList();
    		busSelects2.add(DomainConstants.SELECT_ID);
    		busSelects2.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
    		String relPattern = DomainConstants.RELATIONSHIP_PROJECT_VAULTS
    							+","+ DomainConstants.RELATIONSHIP_SUB_VAULTS
    							+","+ DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2
    							+","+ CommonDocument.RELATIONSHIP_ACTIVE_VERSION;
			String typePattern = DomainConstants.QUERY_WILDCARD;
			MapList infoMapList = dmObject.getRelatedObjects(context, relPattern, typePattern, 
															busSelects2, null, true, false, (short)0, 
															DomainConstants.EMPTY_STRING, 
															DomainConstants.EMPTY_STRING, 0);
			int infoMapListSize = infoMapList.size();
			if(infoMapListSize>0){
				Map rootObjMap = (Map) infoMapList.get(infoMapList.size()-1); //Get the root element in the heirarchy.
				String isRootObjKindOfTemplate = (String) rootObjMap.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
				if(Boolean.valueOf(isRootObjKindOfTemplate)) {
					String templateId = (String) rootObjMap.get(DomainConstants.SELECT_ID);
					ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
					if(!projectTemplate.isOwnerOrCoOwner(context, templateId)){
						return false;
					}
				}
			}
    	}
    	//Template check ends

    	//Access check starts
    	boolean hasAllAccess = "All".equalsIgnoreCase(currAccess) ? true:false;
    	if("PMCFolderSummaryCreate".equalsIgnoreCase(commandName) || "PMCFolderSummaryDelete".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || (currAccess.contains("modify") && currAccess.contains("fromconnect"))) && 
    					(!DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type) || 
    					DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(currState));
    	} else if("PMCFolderSummaryCopyLink".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || (currAccess.contains("modify") && currAccess.contains("fromconnect"))) && 
						!DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED.equalsIgnoreCase(currState);
    	} else if("PMCWorkspaceVaultSubscription".equalsIgnoreCase(commandName)){
    		hasAccess = !DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED.equalsIgnoreCase(currState);
    	} else if("PMCControlledFolderRelease".equalsIgnoreCase(commandName) || "PMCProjectFolderRelease".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || currAccess.contains("promote")) && 
    					DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(currState) &&
    					DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type);
    	} else if("PMCControlledFolderRevise".equalsIgnoreCase(commandName) || "PMCProjectFolderRevise".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || currAccess.contains("revise")) &&
    					DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type) &&
    					Boolean.valueOf(currRevisionable);
    	} else if("PMCProjectFolderPropertiesEditRMB".equalsIgnoreCase(commandName) || "PMCProjectFolderPropertiesEdit".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || currAccess.contains("modify")) &&
    					!DomainConstants.STATE_CONTROLLED_FOLDER_SUPERCEDED.equalsIgnoreCase(currState);
    	} else if("PMCCreateNewFolderDocument".equalsIgnoreCase(commandName) || "PMCContentAddProjectContentActionLink".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || 
    					(currAccess.contains("modify") && currAccess.contains("fromconnect") && currAccess.contains("toconnect"))) &&
    					(!DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type) || 
    	    			DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(currState));
    	} else if("PMCDocumentContentUploadFiles".equalsIgnoreCase(commandName)) {
    		hasAccess = (hasAllAccess || currAccess.contains("fromconnect")) &&
    					(!DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type) || 
        	    		DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(currState));
    	} else if("PMCContentAddToFolderActionLink".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || 
						(currAccess.contains("modify") && currAccess.contains("fromconnect") && currAccess.contains("changeowner"))) &&
						((DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type) && 
						DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(currState)) || 
						!DomainConstants.TYPE_BUSINESS_GOAL.equalsIgnoreCase(type));
    	} else if("PMCDocumentRemove".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || 
						(currAccess.contains("modify") && currAccess.contains("fromdisconnect") && currAccess.contains("todisconnect"))) &&
						(!DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(type) || 
						DomainConstants.STATE_CONTROLLED_FOLDER_CREATE.equalsIgnoreCase(currState));
    	} else if("PMCDocumentCheckInActionLink".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || currAccess.contains("checkin")) &&
    					(!vcfile && !vcfolder && !vcmodule) &&
    					(!isVersionObject && !suspendVersioning && !Boolean.valueOf(isConnectedToRoute));
    	} else if("PMCDocumentEditActionLink".equalsIgnoreCase(commandName)){
    		hasAccess = (hasAllAccess || currAccess.contains("modify"));
    	}
    	//Access check ends
    	
    	return hasAccess;
    }
   
    /**
     * Get type list of workspace vault.
     * @param context - The eMatrix <code>Context</code> object.
     * @param args - The args holds information about object.
     * @return map with i18 type list.
     * @throws MatrixException - If operation fails.
     */
    public Map getWorkspaceVaultTypes(Context context, String[] args) throws MatrixException 
    {
        Map<String,StringList> workspaceVaultTypeMap = new HashMap<String,StringList>();
        
        String language = context.getSession().getLanguage();
        String strMQLQuery = "print type $1 select derivative dump $2";
        String strResultSubType = MqlUtil.mqlCommand(context, strMQLQuery,DomainObject.TYPE_WORKSPACE_VAULT, "|");
        StringList folderSubTypeNameList = FrameworkUtil.split(strResultSubType, "|");

		// IR-433327
		//Removing AER specific types from the list if AER is installed
		if(FrameworkUtil.isSuiteRegistered(context, "appVersionAerospaceProgramManagementAccelerator", false, null, null)) {
			folderSubTypeNameList.remove(PropertyUtil.getSchemaProperty(context, "type_ContractPart"));
			folderSubTypeNameList.remove(PropertyUtil.getSchemaProperty(context, "type_ContractSection"));
		}

        String actulWorkspaceVaultTypeName 	 = PropertyUtil.getSchemaProperty(context, "type_ProjectVault" );
        String displayworkspaceVaultTypeName = EnoviaResourceBundle.getTypeI18NString(context,actulWorkspaceVaultTypeName,language);

        StringList actulFolderTypeList 		= new StringList();
        StringList displayFolderTypeList 	= new StringList();
        
        if(folderSubTypeNameList != null && !folderSubTypeNameList.isEmpty()){
        	int typeListSize = folderSubTypeNameList.size();
        	
        	for(int i=0;i< typeListSize;i++){
        		String typeName 		= (String)folderSubTypeNameList.get(i);
        		String displayTypeName 	= EnoviaResourceBundle.getTypeI18NString(context,typeName,language);
        		
        		actulFolderTypeList.addElement(typeName);
        		displayFolderTypeList.addElement(displayTypeName);
        	}
        }
        
        actulFolderTypeList.add(0,actulWorkspaceVaultTypeName);
        displayFolderTypeList.add(0,displayworkspaceVaultTypeName);
        
        workspaceVaultTypeMap.put("field_choices", actulFolderTypeList);
        workspaceVaultTypeMap.put("field_display_choices", displayFolderTypeList);
        
        return workspaceVaultTypeMap;
    }
    
    /**
     * Get access list of folder.
     * @param context - The eMatrix <code>Context</code> object.
     * @param args - The args holds information about object.
     * @return map with i18 access list.
     * @throws MatrixException - If operation fails.
     */
    public Map getFolderAccessType(Context context, String[] args) throws MatrixException {
		StringList range = new StringList();
		StringList translatedRangeList = new StringList();
		StringList rangeList = new StringList();
		AttributeType attributeAccessType = null;
		final String INHERITED_ACCESS_TYPE = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.Access_Type.Inherited", "en");

		try {
			attributeAccessType = new AttributeType(DomainConstants.ATTRIBUTE_ACCESS_TYPE);
			attributeAccessType.open(context);
			range = attributeAccessType.getChoices();

			int size = range.size();
			String lang = context.getSession().getLanguage();

			for (int i = 0; i < size; i++) {
				String translatedAccessType = (String) range.get(i);
				String accessType = ProgramCentralConstants.EMPTY_STRING;

				if (INHERITED_ACCESS_TYPE.equalsIgnoreCase(translatedAccessType)) {
					translatedAccessType = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
							"emxProgramCentral.Access.AccessType.Inherited.Yes", lang);
					accessType = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Access.AccessType.Inherited.Yes",
							"en");
				}
				else {
					translatedAccessType = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
							"emxProgramCentral.Access.AccessType.Specific.No", lang);
					accessType = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Access.AccessType.Specific.No", "en");
				}

				translatedRangeList.addElement(translatedAccessType);
				rangeList.addElement(accessType);
			}

			Map accessTypeMap = new HashMap();
			accessTypeMap.put("field_choices", rangeList);
			accessTypeMap.put("field_display_choices", translatedRangeList);

			return accessTypeMap;

		}
		finally {
			attributeAccessType.close(context);
		}
	}
    
    public boolean isVisibleOnMobileForTemplate(Context context , String [] args) throws Exception
    {
    	boolean isVisibleOnMobile = true;
    	Map programMap = (Map) JPO.unpackArgs(args);
    	String objectId= (String)programMap.get("objectId");
    	DomainObject dmObject = DomainObject.newInstance(context, objectId);
    	
    	if(dmObject.isKindOf(context, ProgramCentralConstants.TYPE_PROJECT_TEMPLATE)) {
    		String isMobile = context.getCustomData("isMobile");
    		if(Boolean.valueOf(isMobile)){
    			isVisibleOnMobile = false;
    		}
    	}
    	return isVisibleOnMobile;
    }

    /**
     * Includes the documents in search result only with toconnect access for logged in user.
     * @param context - The eMatrix <code>Context</code> object.
     * @param args - The args holds information about object.
     * @return Document id list.
     * @throws Exception
     */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList includeDocumentOID(Context context, String []args)throws Exception {

		Map programMap = (HashMap) JPO.unpackArgs(args);
		StringList finalIncludeList = new StringList();
		String typePattern = (String) programMap.get("field");
		typePattern = typePattern.replace("TYPES=","");

		if(typePattern != null && typePattern.contains(":IS_VERSION_OBJECT!=True")){
			typePattern = typePattern.replace(":IS_VERSION_OBJECT!=True","");
		}

		StringList selectList = new StringList(1);
		selectList.add(DomainConstants.SELECT_ID);
	
		String busWhere ="current.access[toconnect]==TRUE";

		MapList resultsList = DomainObject.findObjects(context,
				typePattern, 
				DomainConstants.QUERY_WILDCARD, 
				busWhere, 
				selectList);

		Iterator idsIterator = resultsList.iterator();
		while(idsIterator.hasNext()){
			Map infoMap = (Map)idsIterator.next();
			finalIncludeList.add(infoMap.get(DomainConstants.SELECT_ID));
		}
		return finalIncludeList;
	}	
}
