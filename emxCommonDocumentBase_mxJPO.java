/*   emxCommonDocumentBase.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.DocumentUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxCommonFileBase</code> class contains code for checkin.
 *
 * @version VCP 10.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonDocumentBase_mxJPO extends emxDocument_mxJPO
{

		/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since VCP 10.5.0.0
	 * @grade 0
	 */
	public emxCommonDocumentBase_mxJPO (Context context, String[] args)
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
	 * @since VCP 10.5.0.0
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
	 * This is the base method executed in common Document model to checkin/update/createmaster using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since VCP 10.5.0.0
	 */
	public Map commonDocumentCheckin(Context context, String[] args) throws Exception
	 {
		  try
		  {
			  if (args == null || args.length < 1)
			  {
				  throw (new IllegalArgumentException());
			  }
			  HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);
			  String objectId       = (String) uploadParamsMap.get("objectId");
			  String parentId       = (String) uploadParamsMap.get("parentId");
			  String objectAction   = (String) uploadParamsMap.get("objectAction");

			  Map objectMap = new HashMap();
			  String strCounnt = (String) uploadParamsMap.get("noOfFiles");
			  int count = 0;
			  try{
				  count = new Integer(strCounnt).intValue();
			  }catch(Exception ex){}

			  for(int i = 0; i < count; i++){
				  String fileName = (String) uploadParamsMap.get("fileName" + i);
				  if(fileName.length() >= 255){
						  objectMap.put("errorMessage", EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.FileNameLengthInvalid"));
						  return objectMap;
				  }
			  }

			  ContextUtil.startTransaction(context, true);

			  Map preCheckinMap = preCheckin(context, uploadParamsMap, parentId);
			  String newParentId = (String) preCheckinMap.get("parentId");
			  if (newParentId != null && !"".equals(newParentId) && !"null".equals(newParentId))
			  {
				  parentId = newParentId;
				  uploadParamsMap.put("parentId", parentId);
			  }

			  if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER) )
			  {
				  /*
				   create Master with given 'type' 'name' 'revision' and other attributes
				   and checckin all file selected in second step to the object created.
				   and Crete Version object for each file.
				  */
				  objectMap = createMaster(context, uploadParamsMap);

			  }
			  else if( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE) )
			  {
				  /*
				   create Master Document with given 'Type' = Document 'name' = autoName 'revision' = default
				   and checckin each file in separate object created.
				   and Crete Version object for each file.
				  */
				  objectMap = createMasterPerFile(context, uploadParamsMap);

			  }
			  else if( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER) )
			  {
				  /*
				   create Document with given 'Type' = Document 'name' = autoName 'revision' = default
				   and checckin each file in separate object created.
				  */
				  objectMap = updateMaster(context, uploadParamsMap);

			  }
			  else if( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_VERSION_FILE) )
			  {
				  /*
				   version a File in given object
				  */
				  objectMap = versionFile(context, uploadParamsMap);
			  }
			  else if( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION) )
			  {
				  /*
				   Checkin the files into the given object
				   and for each file create version object.
				  */
				  objectMap = checkinWithVersion(context, uploadParamsMap);
			  }
			  else if( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) )
			  {
				  /*
				   Checkin the files into the given object
				   and for each file create version object.
				  */
				  objectMap = updateHolder(context, uploadParamsMap);
			  }
			  else
			  {
				  if (objectId != null && !"".equals(objectId) && !"null".equals(objectId))
				  {
					  objectMap = objectCheckin(context, uploadParamsMap, null);
				  }
				  else
				  {
					  /*
					   create given type of object and checkin files in created object with out version.
					  */
					  objectMap = checkinCreateWithOutVersion(context, uploadParamsMap);

				  }
			 }
			 ContextUtil.commitTransaction(context);
			 return objectMap;
		  }
		  catch (Exception ex)
		  {
			  ContextUtil.abortTransaction(context);
			  ex.printStackTrace();
			  throw ex;
		  }
	}

	/**
	 * This is the method used in common Document model to checkin using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @param objectMap holds all objectDetails for checkin.
	 * @returns objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since VCP 10.5.0.0
	 */
	public Map objectCheckin(Context context, HashMap uploadParamsMap, HashMap objectMap) throws Exception
	{
		  try
		  {
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

			  boolean getFromObjectMap = false;
			  if( objectAction.equals(CommonDocument.OBJECT_ACTION_CREATE_MASTER) ||
				  objectAction.equals(CommonDocument.OBJECT_ACTION_CREATE_MASTER_PER_FILE) ||
				  objectAction.equals(CommonDocument.OBJECT_ACTION_UPDATE_MASTER) ||
				  objectAction.equals(CommonDocument.OBJECT_ACTION_VERSION_FILE) ||
				  objectAction.equals(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER) ||
				  objectAction.equals(CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION) ||
				  objectAction.equals(CommonDocument.OBJECT_ACTION_CREATE_CHECKIN) )
			  {
				  getFromObjectMap = true;
			  }
			  String errorMessage = "";
			  boolean deleteDummy = false;
			  CommonDocument dummyObject = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENT);
			  if (!getFromObjectMap && (objectId != null && !"".equals(objectId) && !"null".equals(objectId)) )
			  {
				  if ( objectMap == null )
				  {
					  objectMap = new HashMap();
				  }
				  objectMap.put("format", formats);
				  objectMap.put("fileName", fileNames);
				  objectMap.put("objectId", objectIds);
				  boolean unlock = true;
				  boolean append = true;
				  String strUnlock  = (String) uploadParamsMap.get("unlock");
				  String strAppend  = (String) uploadParamsMap.get("append");
				  if( strUnlock != null && !"".equals(strUnlock) && !"null".equals(strUnlock) )
				  {
					  unlock = "true".equalsIgnoreCase(strUnlock);
				  }
				  if( strAppend != null && !"".equals(strAppend) && !"null".equals(strAppend) )
				  {
					  append = "true".equalsIgnoreCase(strAppend);
				  }

				  for( int i=0; i<count; i++ )
				  {
					  String formatI  = (String)uploadParamsMap.get("format" + i);
					  String fileNameI  = (String)uploadParamsMap.get("fileName" + i);
					  String unlockI  = (String) uploadParamsMap.get("unlock" + i);
					  String appendI  = (String) uploadParamsMap.get("append" + i);
					  if( unlockI != null && !"".equals(unlockI) && !"null".equals(unlockI) )
					  {
						  unlock = "true".equalsIgnoreCase(unlockI);
					  }
					  if( appendI != null && !"".equals(appendI) && !"null".equals(appendI) )
					  {
						  append = "true".equalsIgnoreCase(appendI);
					  }

					  if (fileNameI != null && !"".equals(fileNameI) && !"null".equals(fileNameI))
					  {
						  if ( !checkDuplicate(context, uploadParamsMap, fileNameI, objectId) )
						  {
							  formats.addElement(formatI);
							  fileNames.addElement(fileNameI);
							  objectIds.addElement(objectId);
							  appends.addElement((Boolean.valueOf(append)).toString());
							  unlocks.addElement((Boolean.valueOf(unlock)).toString());
						  } else {
							  if ( !errorMessage.equals("") )
							  {
								  errorMessage += ", ";
							  } else if ( "true".equalsIgnoreCase(fcsEnabled) ){
								  dummyObject.createObject(context,CommonDocument.TYPE_DOCUMENT, null, null, null, null);
								  deleteDummy = true;
							  }
							  if ( "true".equalsIgnoreCase(fcsEnabled) )
							  {
								  formats.addElement(formatI);
								  fileNames.addElement(fileNameI);
								  objectIds.addElement(dummyObject.getObjectId());
							  }
							  errorMessage += fileNameI;
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

			  for(int i = 0; i < fileNames.size(); i++){
				  if(((String)fileNames.get(i)).length() >= 255){
						  throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileNameLength", context.getLocale().getLanguage()));
				  }
			  }

			  String mcsUrl       = (String) uploadParamsMap.get("mcsUrl");
			  MqlUtil.mqlCommand(context, "set env global MCSURL \""+mcsUrl+"\"");
			  //context.setProperties((Properties) (new Properties()).setProperty("MCSUrl", mcsUrl));

			  if ( "true".equalsIgnoreCase(fcsEnabled) && objectIds.size() > 0)
			  {
				  if ( appends != null && appends.size() == objectIds.size() )
				  {
					multiFileCheckinUpdate(context, objectIds, store, formats, fileNames, appends, unlocks, receiptValue);
				  } else {
					multiFileCheckinUpdate(context, objectIds, store, formats, fileNames, receiptValue);
				  }

			  } else {

				  if ( appends != null && appends.size() == objectIds.size() )
				  {
					multiFileCheckinFromServer(context, objectIds, store, formats, fileNames, appends, unlocks);
				  } else {
					multiFileCheckinFromServer(context, objectIds, store, formats, fileNames);
				  }
			  }

			  postCheckin(context, uploadParamsMap, objectIds);
			  if( !errorMessage.equals("") )
			  {
				  errorMessage += "\n" + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.DocumentsAreNotLockedByUser");
				  objectMap.put("errorMessage", errorMessage);
			  }
			  if ( deleteDummy )
			  {
				  dummyObject.deleteObject(context,true);
			  }
			  return objectMap;
		  }
		  catch (Exception ex)
		  {
			  ex.printStackTrace();
			  throw ex;
		  }
	}

	/**
	 * This is the method executed in common Document model to create master object using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since VCP 10.5.0.0
	 */
	 public HashMap createMaster(Context context, HashMap uploadParamsMap) throws Exception
	 {
		  try
		  {
			  return createMaster(context, uploadParamsMap, true);
		  }
		  catch(Exception ex)
		  {
			  ex.printStackTrace();
			  throw ex;
		  }
	 }

	/**
	 * This is the method executed in common Document model to checkinCreateWithOutVersion using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since Common 10.5.0.0
	 */
	 public HashMap checkinCreateWithOutVersion(Context context, HashMap uploadParamsMap) throws Exception
	 {
		  try
		  {
			  return createMaster(context, uploadParamsMap, false);
		  }
		  catch (Exception ex )
		  {
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
	 * @since VCP 10.5.0.0
	 */
	 public HashMap createMaster(Context context, HashMap uploadParamsMap, boolean createVersion) throws Exception
	 {
		  HashMap objectMap = new HashMap();
		  try
		  {
			  String parentId       = (String) uploadParamsMap.get("parentId");
			  String objectId       = (String) uploadParamsMap.get("objectId");
			  String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
			  String parentRelName  = (String) uploadParamsMap.get("parentRelName");
			  if ( parentRelName != null )
			  {
			      parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
			  }
			  String isFrom  = (String) uploadParamsMap.get("isFrom");

			  String strCounnt = (String) uploadParamsMap.get("noOfFiles");
			  int count = new Integer(strCounnt).intValue();

              boolean isFilePresent = false;

			  // Defining the ObjectMap parameters and putting them into the Map.
			  StringList objectIds = new StringList(count);
			  StringList formats = new StringList(count);
			  StringList fileNames = new StringList(count);
			  objectMap.put("format", formats);
			  objectMap.put("fileName", fileNames);
			  objectMap.put("objectId", objectIds);
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
			  Map mAttrMap = (Map) uploadParamsMap.get("attributeMap");
			  String objectGeneratorRevision = (String) uploadParamsMap.get("objectGeneratorRevision");
			  String owner = (String) uploadParamsMap.get("person");

			  if (mDescription == null || "".equals(mDescription) || "null".equals(mDescription))
			  {
				  mDescription = (String) uploadParamsMap.get("mDescription");
			  }

			  if (title == null || "".equals(title) || "null".equals(title))
			  {
				  title = null;
			  }

			  //passing relationship name as a url parameter with out using Mapping File
			  CommonDocument object = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
			  DomainObject parentObject = null;
			  if ( parentId != null && !"".equals(parentId) && !"null".equals(parentId) )
			  {
				  parentObject = DomainObject.newInstance(context, parentId);
			  }

              // FZS - Hitachi IR-368889
              PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "true", false);

			  object = object.createAndConnect(context, type, name, revision, policy, mDescription,
										vault, title, language, parentObject, parentRelName, isFrom, mAttrMap, objectGeneratorRevision);

			  StringList selects = new StringList(2);
			  selects.add(DomainConstants.SELECT_ID);
			  selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			  // Need to Add back -SC
			  //selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			  Map objectSelectMap = object.getInfo(context,selects);

			  objectId = (String)objectSelectMap.get(DomainConstants.SELECT_ID);
			  boolean moveFilesToVersion = Boolean.valueOf((String) objectSelectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();

			  // Iterating through multiple files and creating version objects
			  // for each file and connecting with Master Object.
			  boolean deleteDummy = false;
			  CommonDocument dummyObject = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENT);
			  for ( int i=0; i<count; i++)
			  {
				  Map attrMap = (Map) uploadParamsMap.get("attrMap" + i);
				  if( attrMap == null )
				  {
					  attrMap = new HashMap();
				  }
                  //364067 - inheriting attributest to files
                  Set attrSet = mAttrMap.keySet();
                  Iterator attrItr = attrSet.iterator();
                  String attrName = null;
                  String attrvalue = "";
                  while(attrItr.hasNext())
                  {
                      attrName = (String)attrItr.next();
                      attrvalue = (String)mAttrMap.get(attrName);
                      attrMap.put(attrName, attrvalue);
                  }
                  //364067 - inheriting attributest to files ends
				  String description = (String) uploadParamsMap.get("description" + i);
				  String comments = (String) uploadParamsMap.get("comments" + i);
				  attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
				  String fileName = (String) uploadParamsMap.get("fileName" + i);
				  String format = (String) uploadParamsMap.get("format" + i);
				  if (fileName != null && !"".equals(fileName) && !"null".equals(fileName))
				  {
                      isFilePresent = true;
					  if ( !checkDuplicate(context, uploadParamsMap, fileName, objectId) )
					  {
						  formats.addElement(format);
						  fileNames.addElement(fileName);
						  String checkinId = objectId;
						  if ( createVersion )
						  {
							  checkinId = object.createVersion(context, description, fileName, attrMap);
						  }
						  if ( moveFilesToVersion )
						  {
							  objectIds.addElement(checkinId);
						  } else {
							  objectIds.addElement(objectId);
						  }

					  } else {
						  if ( !errorMessage.equals("") )
						  {
							  errorMessage += ", ";
						  } else if ( "true".equalsIgnoreCase(fcsEnabled) ){
							  dummyObject.createObject(context,CommonDocument.TYPE_DOCUMENT, null, null, null, null);
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

              if(isFilePresent==false)  //if no files are uploaded, Document id is added to the return map
              {
                  objectIds.addElement(objectId);
              }

			  if( !errorMessage.equals("") )
			  {
				  errorMessage += "<BR> \n" + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponentsDocumentManagement.Checkin.DocumentsAlreadyExists");
			  }
			  objectMap.put("errorMessage", errorMessage);
			  objectCheckin(context, uploadParamsMap, objectMap);
			  if( owner != null && !context.getUser().equals(owner) )
			  {
				  object.setOwner(context, owner);
			  }
			  if ( deleteDummy )
			  {
				  dummyObject.deleteObject(context,true);
			  }
			  return objectMap;
		  }
		  catch (Exception ex )
		  {
              PropertyUtil.setRPEValue(context, "MX_ALLOW_POV_STAMPING", "false", false);
			  ex.printStackTrace();
			  throw ex;			  
		  }
	}

	/**
	 * This is the method executed in common Document model to createMasterPerFile using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since Common 10.5.0.0
	 */
	 public HashMap createMasterPerFile(Context context, HashMap uploadParamsMap) throws Exception
	 {

		  try
		  {
			  HashMap objectMap = new HashMap();
			  String parentId       = (String) uploadParamsMap.get("parentId");
			  String objectId       = (String) uploadParamsMap.get("objectId");
			  String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
			  String parentRelName  = (String) uploadParamsMap.get("parentRelName");
			  if ( parentRelName != null )
			  {
				  parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
			  }
			  String isFrom  = (String) uploadParamsMap.get("isFrom");

			  String strCounnt = (String) uploadParamsMap.get("noOfFiles");
			  int count = new Integer(strCounnt).intValue();
			  StringList objectIds = new StringList(count);
			  StringList formats = new StringList(count);
			  StringList fileNames = new StringList(count);
			  objectMap.put("format", formats);
			  objectMap.put("fileName", fileNames);
			  objectMap.put("objectId", objectIds);

			  String errorMessage = "";

			  // Master Object Parameters
			  String type = (String) uploadParamsMap.get("type");
			  String name = (String) uploadParamsMap.get("name");
			  String revision = (String) uploadParamsMap.get("revision");
			  String policy = (String) uploadParamsMap.get("policy");
			  String mDescription = (String) uploadParamsMap.get("mDescription");
			  String mtitle = (String) uploadParamsMap.get("title");
			  String accessType = (String) uploadParamsMap.get("accessType");
			  String language = (String) uploadParamsMap.get("language");
			  String vault = (String) uploadParamsMap.get("vault");
			  Map mAttrMap = (Map) uploadParamsMap.get("attributeMap");
			  String objectGeneratorRevision = (String) uploadParamsMap.get("objectGeneratorRevision");

			  if (mDescription == null || "".equals(mDescription) || "null".equals(mDescription))
			  {
				  mDescription = (String) uploadParamsMap.get("description");
			  }

			  // Iterating through multiple files and creating version objects
			  // for each file and connecting with Master Object.
			  DomainObject parentObject = null;
			  if ( parentId != null && !"".equals(parentId) && !"null".equals(parentId) )
			  {
				  parentObject = DomainObject.newInstance(context, parentId);
			  }
			  boolean deleteDummy = false;
			  CommonDocument dummyObject = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENT);
			  for ( int i=0; i<count; i++)
			  {
				  CommonDocument object = (CommonDocument) DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
				  Map attrMap = (Map) uploadParamsMap.get("attrMap" + i);
				  String description = (String) uploadParamsMap.get("description" + i);
				  String title = (String) uploadParamsMap.get("title" + i);
				  String fileName = (String) uploadParamsMap.get("fileName" + i);
				  String format = (String) uploadParamsMap.get("format" + i);
				  String comments = (String) uploadParamsMap.get("comments" + i);
				  String masterDescription = null;
				  if ( attrMap == null )
				  {
					  attrMap = new HashMap();
				  }
				  attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);

				  // if mDescription is passed use that value
				  // else use description value for master object also
				  if (mDescription != null && "".equals(mDescription) && "null".equals(mDescription))
				  {
					  masterDescription = mDescription;
				  }
				  else
				  {
					   masterDescription = description;
				  }

				  if (title == null || "".equals(title) || "null".equals(title))
				  {
					  title = fileName;
				  }

				  if (fileName != null && !"".equals(fileName) && !"null".equals(fileName))
				  {
					  if ( !checkDuplicate(context, uploadParamsMap, fileName, null) )
					  {
						  object.createAndConnect(context, type, name, revision, policy, masterDescription,
													vault, title, language, parentObject, parentRelName, isFrom, mAttrMap, objectGeneratorRevision);
						  StringList selects = new StringList(2);
						  selects.add(DomainConstants.SELECT_ID);
						  selects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
						  Map objectSelectMap = object.getInfo(context,selects);

						  objectId = (String)objectSelectMap.get(DomainConstants.SELECT_ID);
						  boolean moveFilesToVersion = (Boolean.valueOf((String) objectSelectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
						  formats.addElement(format);
						  fileNames.addElement(fileName);
						  String checkinId = object.createVersion(context, description, fileName, attrMap);
						  if ( moveFilesToVersion )
						  {
							  objectIds.addElement(checkinId);
						  } else {
							  objectIds.addElement(objectId);
						  }

					  } else {
						  if ( !errorMessage.equals("") )
						  {
							  errorMessage += ", ";
						  } else if ( "true".equalsIgnoreCase(fcsEnabled) ){
							  dummyObject.createObject(context,CommonDocument.TYPE_DOCUMENT, null, null, null, null);
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
				  errorMessage += "<BR> \n" + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponentsDocumentManagement.Checkin.DocumentsAlreadyExists");
			  }
			  objectMap.put("errorMessage", errorMessage);
			  objectCheckin(context, uploadParamsMap, objectMap);
			  if ( deleteDummy )
			  {
				  dummyObject.deleteObject(context,true);
			  }
			  return objectMap;
		  }
		  catch (Exception ex )
		  {
			  ex.printStackTrace();
			  throw ex;
		  }
	}


	/**
	 * This is the method executed in common Document model to checkinWithVersion using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since Common 10.5.0.0
	 */
	 public HashMap checkinWithVersion(Context context, HashMap uploadParamsMap) throws Exception
	 {
		  try
		  {
			  HashMap objectMap = new HashMap();
			  String parentId       = (String) uploadParamsMap.get("parentId");
			  String objectId       = (String) uploadParamsMap.get("objectId");
			  String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
			  String parentRelName  = (String) uploadParamsMap.get("parentRelName");
			  if ( parentRelName != null )
			  {
				  parentRelName = PropertyUtil.getSchemaProperty(context,parentRelName);
			  }
			  String isFrom  = (String) uploadParamsMap.get("isFrom");

			  // Future Use -SC
			  //StringList oIds = (StringList) uploadParamsMap.get("objectIds");
			  String strCounnt = (String) uploadParamsMap.get("noOfFiles");
			  String description = (String) uploadParamsMap.get("description");

			  int count = new Integer(strCounnt).intValue();
			  StringList objectIds = new StringList(count);
			  StringList formats = new StringList(count);
			  StringList fileNames = new StringList(count);
			  objectMap.put("format", formats);
			  objectMap.put("fileName", fileNames);
			  objectMap.put("objectId", objectIds);
			  String errorMessage = "";
              //modified for the bug 328378 -Start
              String [] strfileNames = null;
              String strFile = null;
              //modified for the bug 328378 -end
			  CommonDocument object = (CommonDocument) DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
			  StringList selectList = new StringList();
              StringList selectFileList = new StringList();
			  Map selectMap = new HashMap();
			  StringList fileList = new StringList();
			  StringList fileLockerList = new StringList();
              StringList fileRevisionList = new StringList();
              StringList fileIdList = new StringList();
			  boolean moveFilesToVersion = false;
			  if( objectId != null && !"".equals(objectId) && !"null".equals(objectId) )
			  {
				  object = (CommonDocument) DomainObject.newInstance(context, objectId);
				  //selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
				  //selectList.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
				  selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
                  selectList.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
                  DomainObject.MULTI_VALUE_LIST.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
				  selectMap = object.getInfo(context, selectList);
				  fileList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
				  fileLockerList = (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
				  if(null!=fileList && fileList.size()>0){
                      strfileNames = new String[fileList.size()];
                      for(int j = 0;j < fileList.size();j++) {
                          strfileNames[j] = (String) fileList.get(j);

                          }
                      }
                   // till here 328378
				  //fileLockerList = (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
				  moveFilesToVersion = Boolean.valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
			  }
			  boolean deleteDummy = false;
			  CommonDocument dummyObject = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENT);
			  for ( int i=0; i<count; i++)
			  {
				  Map attrMap = (Map) uploadParamsMap.get("attrMap" + i);
				  String localObjectId =  (String) uploadParamsMap.get("objectId" + i );
				  if ( localObjectId != null && !"".equals(localObjectId) && !"null".equals(localObjectId) )
				  {
					  object = (CommonDocument) DomainObject.newInstance(context, localObjectId);
					  selectList = new StringList();
					  selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
					  selectList.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
					  selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
					  selectMap = object.getInfo(context, selectList);
					  fileList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
					  fileLockerList = (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
					  moveFilesToVersion = Boolean.valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
                       if(null!=fileList && fileList.size()>0){
                           strfileNames = new String[fileList.size()];
                           for(int j = 0;j < fileList.size();j++) {
                               strfileNames[j] = (String) fileList.get(j);
                               }
                            }
				  } else {
					  localObjectId = objectId;
				  }
				  String comments = (String) uploadParamsMap.get("comments" + i);
				  String fileName = (String) uploadParamsMap.get("fileName" + i);
				  String format = (String) uploadParamsMap.get("format" + i);
				  if ( attrMap == null )
				  {
					  attrMap = new HashMap();
				  }
				  attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);

				  // Getting old file name this is used for Update.
				  String oldFileName = (String) uploadParamsMap.get("oldFileName" + i);
				  if( count == 1 && (fileName == null || "".equals(fileName) || "null".equals(fileName)) )
				  {
					  attrMap = (Map) uploadParamsMap.get("attrMap");
					  comments = (String) uploadParamsMap.get("comments");
					  fileName = (String) uploadParamsMap.get("fileName");
					  format = (String) uploadParamsMap.get("format");

					  // Getting old file name this is used for Update.
					  oldFileName = (String) uploadParamsMap.get("oldFileName");

				  }
				  // Setting to file name if old file name is null
				  if ( oldFileName == null || "".equals(oldFileName) || "null".equals(oldFileName) )
				  {
					  oldFileName = fileName;
				  }
				  if (fileName != null && !"".equals(fileName) && !"null".equals(fileName) )
				  {
					  int index = -1;
                      //Modified for the bug 328378
                       //if ( fileList != null ) //commentd
                      if ( strfileNames != null && strfileNames.length>0)
					  {
                          //index = fileList.indexOf(oldFileName); //commneted

                           for(int indx=0; indx<strfileNames.length; indx++)
                           {
                             String strFileName = strfileNames[indx];
                             strFile = strFileName;
                             if(fileName.equalsIgnoreCase(strFileName)){
                                   index = indx;
                                    break;
                                 }
                            }
                       }
                          //328378 till here
					  if ( index != -1 && index <= fileLockerList.size() )
					  {
						  String locker = (String)fileLockerList.get(index);
						  if ( locker.equalsIgnoreCase(context.getUser()) )
						  {
							  if (oldFileName.equalsIgnoreCase(fileName))
							  {
								  // Current User is having this file locked so we need to revise the version object.
								  // and move file to old revision of version obejct and checkin files into master obejct.
								  formats.addElement(format);
								  fileNames.addElement(fileName);
                                  //String checkinId = object.reviseVersion(context, oldFileName, fileName, attrMap);
                                    String checkinId = object.reviseVersion(context, strFile, fileName, attrMap);//modified for the 328378
								  if ( moveFilesToVersion )
								  {
									  objectIds.addElement(checkinId);
								  } else {
									  objectIds.addElement(localObjectId);
								  }
							  } else {
								  if ( !errorMessage.equals("") )
								  {
									  errorMessage += ", ";
								  } else if ( "true".equalsIgnoreCase(fcsEnabled) ){
									  dummyObject.createObject(context,CommonDocument.TYPE_DOCUMENT, null, null, null, null);
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

						  } else {
							  if ( !errorMessage.equals("") )
							  {
								  errorMessage += ", ";
							  } else if ( "true".equalsIgnoreCase(fcsEnabled) ){
								  dummyObject.createObject(context,CommonDocument.TYPE_DOCUMENT, null, null, null, null);
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

					  } else {
			                  formats.addElement(format);
                              fileNames.addElement(fileName);
							  // changed to reviseVersion for the bug 343952
                              String checkinId = object.reviseVersion(context, oldFileName, fileName, attrMap);
                              if ( moveFilesToVersion )
                              {
                                  objectIds.addElement(checkinId);
                              } else {
                                  objectIds.addElement(localObjectId);
                              }
                         }
				  }
			  }
			  if( !errorMessage.equals("") )
			  {
				  StringList errorFileList = FrameworkUtil.split(errorMessage, ",");
				  errorMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.DocumentsAreNotLockedByUser");
				  Iterator itr = errorFileList.iterator();
				  while (itr.hasNext())
				  {
					  errorMessage += " \n" + (String)itr.next();
				  }
			  }
			  objectMap.put("errorMessage", errorMessage);
			  objectCheckin(context, uploadParamsMap, objectMap);
			  if ( deleteDummy )
			  {
				  dummyObject.deleteObject(context,true);
			  }
			  return objectMap;
		  }
		  catch (Exception ex )
		  {
			  ex.printStackTrace();
			  throw ex;
		  }
	}


	/**
	 * This is the method executed in common Document model to updateHolder using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since Common 10.5.0.0
	 */
	 public HashMap updateHolder(Context context, HashMap uploadParamsMap) throws Exception
	 {
		  try
		  {
			  return checkinWithVersion(context, uploadParamsMap);
		  }
		  catch (Exception ex )
		  {
			  ex.printStackTrace();
			  throw ex;
		  }
	}

	/**
	 * This is the method executed in common Document model to updateMaster using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since Common 10.5.0.0
	 */
	 public HashMap updateMaster(Context context, HashMap uploadParamsMap) throws Exception
	 {
		  try
		  {
			  return checkinWithVersion(context, uploadParamsMap);
		  }
		  catch (Exception ex )
		  {
			  ex.printStackTrace();
			  throw ex;
		  }
	}

	/**
	 * This is the method executed in common Document model to versionFile using FCS/NonFCS.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param uploadParamsMap holds all arguments passed through checkin screens.
	 * @returns Map objectMap which contains objectId, filename, format pairs and
	 *                                                  errorMessage if any error.
	 * @throws Exception if the operation fails
	 * @since Common 10.5.0.0
	 */
	 public HashMap versionFile(Context context, HashMap uploadParamsMap) throws Exception
	 {
		  try
		  {
			  return checkinWithVersion(context, uploadParamsMap);
		  }
		  catch (Exception ex )
		  {
			  ex.printStackTrace();
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
	public void multiFileCheckinUpdate(Context context, StringList objectIds,String store,
							  StringList formats, StringList fileNames, String receiptValue)
				throws Exception
	{
		try
		{
			DocumentUtil.checkinUpdate(context, objectIds, store, formats, fileNames, receiptValue, new StringList(), new StringList(), true);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
	public void multiFileCheckinUpdate(Context context, StringList objectIds,String store,
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
			ex.printStackTrace();
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
	 * @returns String objectId in which file checkedin
	 * @throws Exception if the operation fails
	 * @since Common 10.0.0.0
	 */
	public void multiFileCheckinFromServer(Context context, StringList objectIds,String store,
							  StringList formats, StringList fileNames)
				throws Exception
	{
		try
		{
			  Map objectMap = new HashMap();
			  if ( (objectIds.size() == formats.size()) && (objectIds.size() == fileNames.size()) )
			  {
					String objectId = "";
					String format = "";
					StringList formatFileList = new StringList(fileNames.size());
					for (int i=0; i< objectIds.size(); i++)
					{
						  objectId = (String) objectIds.get(i);
						  format = (String) formats.get(i);
						  if ( objectMap.containsKey(objectId) )
						  {
							  Map formatMap = (Map)objectMap.get(objectId);
							  if ( formatMap.containsKey(format) )
							  {
								  formatFileList = (StringList) formatMap.get(format);
								  formatFileList.addElement((String) fileNames.get(i));
							  } else {
								  formatFileList = new StringList();
								  formatFileList.addElement((String) fileNames.get(i));
								  formatMap.put(format, formatFileList);
							  }
						  } else {
							  Map formatMap = new HashMap();
							  formatFileList = new StringList();
							  formatFileList.addElement((String) fileNames.get(i));
							  formatMap.put(format, formatFileList);
							  objectMap.put(objectId, formatMap);
						  }
					}


					java.util.Set objectSet = objectMap.keySet();
					Iterator itr = objectSet.iterator();
					while( itr.hasNext() )
					{
						objectId = (String) itr.next();
						DomainObject domainObject = DomainObject.newInstance(context, objectId);
						Map formatMap = (Map) objectMap.get(objectId);
						java.util.Set formatSet = formatMap.keySet();
						Iterator firmatItr = formatSet.iterator();
						while( firmatItr.hasNext() )
						{
							format = (String) firmatItr.next();
							StringList fileList = (StringList) formatMap.get(format);
							domainObject.checkinFromServer(context, true, true, format, store, fileList);
						}
					}
			  }
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
	 * @returns String objectId in which file checkedin
	 * @throws Exception if the operation fails
	 * @since Common 10.0.0.0
	 */
	public void multiFileCheckinFromServer(Context context, StringList objectIds,String store,
							  StringList formats, StringList fileNames, StringList appends,
							  StringList unlocks)
				throws Exception
	{
		try
		{
			/* This need to be changed to use append and replace parameter later  */
			multiFileCheckinFromServer(context, objectIds, store, formats, fileNames);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
	}

	public Map preCheckin(Context context, HashMap uploadParamsMap, String objectId) throws Exception
	{
	  //stub need be implemented by applications specific JPOS
	  Map preCheckinMap = new HashMap();
	  return preCheckinMap;
	}

	public void postCheckin(Context context, HashMap uploadParamsMap, StringList newObjectIds) throws Exception
	{
	  //stub need be implemented by applications specific JPOS
	}


	public boolean checkDuplicate(Context context, HashMap uploadParamsMap, String fileName, String objectId) throws Exception
	{
		String objectAction = (String) uploadParamsMap.get("objectAction");
		if (objectId == null || "".equals(objectId) || "null".equals(objectId) )
		{
			return false;
		}
		boolean isDuplicateFile = false;
		if ( objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_MASTER)
			 ||
			 objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_VERSION_FILE)
			 ||
			 objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CHECKIN_WITH_VERSION)
			 ||
			 objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_UPDATE_HOLDER)
            		 ||
             		objectAction.equalsIgnoreCase(CommonDocument.OBJECT_ACTION_CHECKIN_WITHOUT_VERSION) )
		{
            //IR-030365V6R2011
			//CommonDocument commonDocument = (CommonDocument)newInstance(context, objectId);
            DomainObject commonDocument = (DomainObject)newInstance(context, objectId);
			StringList selectList = new StringList(9);
			selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			selectList.add(CommonDocument.SELECT_FILE_NAME);
            		selectList.add(CommonDocument.SELECT_LOCKER);
			Map selectMap = commonDocument.getInfo(context, selectList);
			StringList titleList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			StringList fileList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAME);
            		String locker = (String) selectMap.get(CommonDocument.SELECT_LOCKER);
			boolean moveFilesToVersion = Boolean.valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
			if ( moveFilesToVersion )
			{
				if ( titleList!= null && titleList.contains(fileName) )
				{
					isDuplicateFile = true;
				}
			} else {
				if ( fileList != null && fileList.contains(fileName) && !locker.equals(context.getUser()))
				{
					isDuplicateFile = true;
				}
			}


		}
		return isDuplicateFile;
	}

	/**
	 * This method is used to initiate subscription notifications.
	 * Args Array takes objectid, event, boolean if object is a version
	 * @param context the eMatrix <code>Context</code> object
	 * @param args String array of parameters
	 * @throws Exception if the operation fails
	 * @since Common 10-0-5-0
	 */
	public void handleSubscriptionEvent (matrix.db.Context context, String[] args)
		throws Exception
	{

	  try
	  {
		if ( args == null || args.length == 0)
		{
		  throw new IllegalArgumentException();
		}

		String objectId = args[0];
		String event = args[1];
		String isVersion = args[2];

		if (objectId != null && !"".equals(objectId) && !"null".equals(objectId)){
		  //get object info
		  String selectParentId = "last.to[" + CommonDocument.RELATIONSHIP_LATEST_VERSION + "].from.id";

		  StringList selects = new StringList(3);
		  selects.add(DomainConstants.SELECT_ID);
		  selects.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
		  selects.add(selectParentId);

		  DomainObject parentObject = DomainObject.newInstance(context, objectId);
		  Map objectSelectMap = parentObject.getInfo(context,selects);
		  String objIsVersion = (String)objectSelectMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
		  String objParentId = (String)objectSelectMap.get(selectParentId);
		  // do not send notification for Version object modification
		  if (objIsVersion.equalsIgnoreCase("true") && "Document Modified".equals(event))
		  {
			return;
		  }

		  if (objIsVersion.equalsIgnoreCase("true")){
			//get parent oid to fire event on
			objectId = objParentId;
		  }

		  String[] oids = new String[1];
		  oids[0] = objectId;
		  emxSubscriptionManager_mxJPO subMgr = new emxSubscriptionManager_mxJPO(context, oids);
		  subMgr.publishEvent (context, event,objectId);

		} //end check for empty objectid param.

	  }catch(Exception e){
		throw e;
	  }
	} //eom

	/**
	 * This is method is used to purge old version of checked in file
	 * this method need to be called from checkin action trigger from any DOCUMENTS type object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 *            args[0] should be ${OBJECTID}
	 *            args[1] should be ${FILENAME_ORIGINAL}
	 * These arguments need to be set as eService Program Arguments 1 and 2
	 *      in eService Trigger Program Parameters object.
	 * @throws Exception if the operation fails
	 * @since VCP 10.5.0.0
	 * @grade 0
	 */
	public static void purgePreviousVersions(Context context, String[] args) throws Exception
	{
		try
		{
			if ( args == null || args.length == 0)
			{
				throw new IllegalArgumentException();
			}

			String objectId = args[0];
			String fileName = args[1];
			CommonDocument commonDocument = (CommonDocument)DomainObject.newInstance(context, objectId);
			StringList selectList = new StringList(10);
			selectList.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
			selectList.add(CommonDocument.SELECT_ID);
			selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			selectList.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
			Map objectMap = commonDocument.getInfo(context, selectList);
			String isVersionObject = (String) objectMap.get(CommonDocument.SELECT_IS_VERSION_OBJECT);
			boolean isVersion = (Boolean.valueOf(isVersionObject)).booleanValue();
			if ( isVersion )
			{
				MapList mlist = commonDocument.getRevisionsInfo(context, selectList, null);
				Iterator itr = mlist.iterator();
				while (itr.hasNext() )
				{
					Map m = (Map) itr.next();
					String oid = (String)m.get(SELECT_ID);
					if ( !oid.equals(objectId) )
					{
						MqlUtil.mqlCommand(context, "delete bus '" + oid +"'");
					}
				}
			} else {
				StringList files = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
				StringList versionIds = (StringList)objectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
				int index = files.indexOf(fileName);
				if ( index > -1 && versionIds.size() >= index )
				{
					String versionId = (String)versionIds.get(index);
					commonDocument.setId(versionId);
					MapList mlist = commonDocument.getRevisionsInfo(context, selectList, new StringList());
					Iterator itr = mlist.iterator();
					while (itr.hasNext() )
					{
						Map m = (Map) itr.next();
						String oid = (String)m.get(SELECT_ID);
						if ( !oid.equals(versionId) )
						{
							MqlUtil.mqlCommand(context, "delete bus '" + oid +"'");
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 *            args[0] is DOCUMENTS or its derived object - objectId to cehckin
	 *            args[1] is File Path where the file is located in the disk EX: c:\temp\
	 *            args[2] is File Name which need to be checked in given path EX:xyz.doc
	 *            args[3] is File Format in which this file need to be checked in EX:generic
	 *            args[4] is store Name where this file should be checked in EX:STORE
	 *            args[5] is unlock this should be 'true' or 'false' true to unlock the version object.
	 *            args[6] is server key word to checkin from from server
	 *                        this should be server when calling from thinck client
	 *                        this should be client when calling from powerweb
	 *            args[7] is Comments to the file getting checked in
	 *  Example to call this method through mql
	 *  execute program emxCommonDocument -method checkinBus objectId c:\temp\ tempFile.txt generic STORE false server 'New File Added';
	 *
	 * @returns nothing
	 * @throws Exception if the operation fails
	 */
	 //*            args[8] is old File Name which need to be versiond by the new File getting checked in
	 //*                        this is an optional parameter. This is required only to change an existing file with the new file
	 //*                        and file name are different.

	public int checkinBus(Context context, String[] args) throws Exception
	{
		if(!context.isConnected())
		{
			throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
		}
		try
		{
			String oid = null;
			String filePath = null;
			String fileName = null;
			String format = null;
			String store = null;
			String unlock = null;
			String server = null;
			String comments = null;
			String oldFileName = null;
			try
			{
			  oid = args[0];
			  filePath = args[1];
			  fileName = args[2];
			  format = args[3];
			  store = args[4];
			  unlock = args[5];
			  server = args[6];
			  comments = args[7];
			} catch(Exception ex) {
				//Ignore exception
			}
			if ( oid == null || "".equals(oid) )
			{
				throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.ObjectIdNotEmpty", context.getLocale().getLanguage()));
			}
			if ( fileName == null || "".equals(fileName) )
			{
				throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileNameNotEmpty", context.getLocale().getLanguage()));
			}
			if ( format == null || "".equals(format) )
			{
				throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileFormateNotEmpty", context.getLocale().getLanguage()));
			}
			if ( store == null || "".equals(store) )
			{
				throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonDocumentBase.FileStoreNameNotEmpty", context.getLocale().getLanguage()));
			}

			if ( unlock != null && "true".equalsIgnoreCase(unlock) )
			{
				unlock = "unlock";
			} else {
				unlock = "";
			}
			if ( server == null || "".equals(server) || "null".equals(server) )
			{
				server = "server";
			}
			server = server.toLowerCase();
			if(!"server".equals(server) && !"client".equals(server) )
			{
				server = "server";
			}
			if ( oldFileName == null || "".equals(oldFileName) || "null".equals(oldFileName) )
			{
				oldFileName = fileName;
			}
			Map attrMap = new HashMap();
			ContextUtil.startTransaction(context, true);
			CommonDocument object = (CommonDocument) DomainObject.newInstance(context, oid);
			StringList selectList = new StringList();
			selectList.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			selectList.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
			selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			Map selectMap = object.getInfo(context, selectList);
			StringList fileList = (StringList) selectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			StringList fileLockerList = (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
			boolean moveFilesToVersion = Boolean.valueOf((String) selectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION)).booleanValue();
			if ( comments != null && !"".equals(comments) && !"null".equals(comments) )
			{
				attrMap.put(CommonDocument.ATTRIBUTE_CHECKIN_REASON, comments);
			}

			String objectId = object.reviseVersion(context, oldFileName, fileName, attrMap);
			if ( objectId == null )
			{
				String errorMessage = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.DocumentsAreNotLockedByUser");
				throw new Exception(errorMessage + " \n" + oldFileName);
			}

			if ( !moveFilesToVersion )
			{
				objectId = oid;
			}
			String cmd = "checkin bus " + objectId + " " + unlock + " " + server +" format '" + format +"' store '"+ store +"' append '" + filePath + fileName;
			MqlUtil.mqlCommand(context, cmd);

			ContextUtil.commitTransaction(context);
			return 0;
		} catch (Exception ex) {
			ex.printStackTrace();
			ContextUtil.abortTransaction(context);
			throw ex;

		}
	}


	public boolean allowFileVersioning(Context context, String[] args) throws Exception
	{
		try
		{
			HashMap programMap        = (HashMap) JPO.unpackArgs(args);
			String  objectId          = (String) programMap.get("objectId");
			return checkVersionable(context, objectId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public boolean disallowFileVersioning(Context context, String[] args) throws Exception
	{
		try
		{
			HashMap programMap        = (HashMap) JPO.unpackArgs(args);
			String  objectId          = (String) programMap.get("objectId");
			return(!checkVersionable(context, objectId));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public boolean checkVersionable(Context context, String[] args) throws Exception
	{
		String objectId = args[0];
		return checkVersionable(context, objectId);
	}

	public boolean checkVersionableType(Context context, String[] args) throws Exception
	{
		String objectType = args[0];
		return checkVersionableType(context, objectType);
	}

	public boolean checkVersionable(Context context, String objectId) throws Exception
	{
		try
		{
			DomainObject object = new DomainObject(objectId);
			String type = object.getInfo(context, SELECT_TYPE);
			return checkVersionableType(context, type);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	public boolean checkVersionableType(Context context, String type) throws Exception
	{
		try
		{
			String parentType = CommonDocument.getParentType(context, type);
			if( CommonDocument.TYPE_DOCUMENTS.equals(parentType) )
			{
				String property = PropertyUtil.getAdminProperty(context, "Type", type, "DISALLOW_VERSIONING");
				if ( property != null && "true".equalsIgnoreCase(property) )
				{
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return true;
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkContentAdded(Context context, String[] args) throws Exception
	{
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String)programMap.get("id");
			if (checkVersionable(context, objectId))
			{
				return 1;
			}

			String event = MqlUtil.mqlCommand(context, "get env EVENT");
			if (!event.equalsIgnoreCase("Checkin"))
			{
				return 1;
			}

			String appendFlag = MqlUtil.mqlCommand(context, "get env APPENDFLAG");
			String format = MqlUtil.mqlCommand(context, "get env FORMAT");
			String fileName = MqlUtil.mqlCommand(context, "get env FILENAME_ORIGINAL");
			DomainObject object = DomainObject.newInstance(context, objectId);
			StringList checkedInFiles = object.getInfoList(context,"format[" + format + "].file.name");

			if (("True".equalsIgnoreCase(appendFlag) && !checkedInFiles.contains(fileName)) ||
				("False".equalsIgnoreCase(appendFlag) && checkedInFiles.size() == 0))
			{
				return 0;
			} else {
				return 1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkContentModified(Context context, String[] args) throws Exception
	{
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String objectId = (String)programMap.get("id");
			if (checkVersionable(context, objectId))
			{
				return 1;
			}

			String event = MqlUtil.mqlCommand(context, "get env EVENT");
			if (!event.equalsIgnoreCase("Checkin"))
			{
				return 1;
			}

			String appendFlag = MqlUtil.mqlCommand(context, "get env APPENDFLAG");
			String format = MqlUtil.mqlCommand(context, "get env FORMAT");
			String fileName = MqlUtil.mqlCommand(context, "get env FILENAME_ORIGINAL");
			DomainObject object = DomainObject.newInstance(context, objectId);
			StringList checkedInFiles = object.getInfoList(context,"format[" + format + "].file.name");

			if (("True".equalsIgnoreCase(appendFlag) && checkedInFiles.contains(fileName)) ||
				("False".equalsIgnoreCase(appendFlag) && checkedInFiles.size() > 0))
			{
				return 0;
			} else {
				return 1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkModified(Context context, String[] args) throws Exception
	{
		String history = MqlUtil.mqlCommand(context, "get env $1", "TRANSHISTORY");
		if (history.indexOf("modify -") < 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	/**preventing the checkout notification on checking in the image file
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	
	public int checkCheckout(Context context, String[] args) throws Exception
	{
		String fromCheckin = MqlUtil.mqlCommand(context, "get env $1", "MX_FROM_CHECKIN");
		if ("true".equals(fromCheckin)) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * @param context
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public com.matrixone.jdom.Document getContentMailXML(Context context, Map info) throws Exception
	{
		// get base url
		String baseURL = (String)info.get("baseURL");
		// get notification name
		String notificationName = (String)info.get("notificationName");
		HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
		String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
		String eventKey = "emxComponents.DOCUMENTS.Event." + eventName.replace(' ', '_');
		String bundleName = (String)info.get("bundleName");
		String locale = ((Locale)info.get("locale")).toString();
		Locale userLocale=new Locale(locale);
		String i18NEvent = EnoviaResourceBundle.getProperty(context, bundleName, userLocale, eventKey);
		String messageType = (String)info.get("messageType");

		// get document id
		String mainDocId = (String)info.get("id");
		// get document object info
		DomainObject mainDoc = DomainObject.newInstance(context, mainDocId);
		StringList selectList = new StringList(7);
		selectList.addElement(SELECT_TYPE);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_REVISION);
        selectList.add("vcfile");
        selectList.add("vcfile[1].vcname");
        selectList.add(CommonDocument.SELECT_VCFOLDER);
        selectList.add("vcfolder[1].vcname");

		Map mainDocInfo = mainDoc.getInfo(context, selectList);
		String mainDocType = (String)mainDocInfo.get(SELECT_TYPE);

		String i18NMainDocType = UINavigatorUtil.getAdminI18NString("Type", mainDocType, locale);
		String mainDocName = (String)mainDocInfo.get(SELECT_NAME);
		String mainDocRev = (String)mainDocInfo.get(SELECT_REVISION);
		String fileName = "";

        String isVCFile = (String) mainDocInfo.get("vcfile");
        String isVCFolder = (String) mainDocInfo.get(CommonDocument.SELECT_VCFOLDER);
        // if this is a VC file - Right now a DesignSync file
        if (isVCFile.equals("TRUE"))
        {
           fileName = (String) mainDocInfo.get("vcfile[1].vcname");
        }
        else if (isVCFolder.equals("TRUE"))
        {
           fileName = (String) mainDocInfo.get("vcfolder[1].vcname");
        }
        else
        {
		   String event = MqlUtil.mqlCommand(context, "get env $1", "EVENT");
		   if ("Checkin".equalsIgnoreCase(event) || "Checkout".equalsIgnoreCase(event))
		   {
			   fileName = MqlUtil.mqlCommand(context, "get env $1","FILENAME_ORIGINAL");
		   } else
		   {
			   // get file checked in
			   String versionDocId = MqlUtil.mqlCommand(context, "get env $1","TOOBJECTID");
			  // DomainObject versionDoc = DomainObject.newInstance(context, versionDocId);
			   // fileName = versionDoc.getInfo(context, getAttributeSelect(ATTRIBUTE_TITLE));
			   fileName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", 
			   true,versionDocId,"attribute["+ATTRIBUTE_TITLE+"]" );
		   }
        }

		// header data
		HashMap headerInfo = new HashMap();
		headerInfo.put("header", i18NEvent + " : " + i18NMainDocType + " " + mainDocName + " " + mainDocRev);

		// body data
		HashMap bodyInfo = new HashMap();
		HashMap fieldInfo = new HashMap();

		fieldInfo.put(EnoviaResourceBundle.getProperty(context,bundleName,new Locale(locale), "emxComponents.DOCUMENTS.Event.Mail.File_Name"), fileName);
		bodyInfo.put(i18NEvent, fieldInfo);
		// footer data
		HashMap footerInfo = new HashMap();
		ArrayList dataLineInfo = new ArrayList();
		if (messageType.equalsIgnoreCase("html"))
		{
			//String[] messageKeys = {"href", "type", "name", "revision"};
			String[] messageValues = new String[4];
			messageValues[0] = baseURL + "?objectId=" + mainDocId;
			messageValues[1] = i18NMainDocType;
			messageValues[2] = mainDocName;
			messageValues[3] = mainDocRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxComponents.Object.Event.Html.Mail.ViewLink",
													 messageValues,null,
													 userLocale,bundleName);


			dataLineInfo.add(viewLink);
		} else {
			//String[] messageKeys = {"type", "name", "revision"};
			String[] messageValues = new String[3];
			messageValues[0] = i18NMainDocType;
			messageValues[1] = mainDocName;
			messageValues[2] = mainDocRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxComponents.Object.Event.Text.Mail.ViewLink",
													 messageValues,null,
													 userLocale,bundleName);


			dataLineInfo.add(viewLink);
			dataLineInfo.add(baseURL + "?objectId=" + mainDocId);
		}
		//dataLineInfo.add("Click here to <a href=\"" + baseURL + "?objectId=" + mainDocId + "\"> view </a>" + i18NMainDocType + " " + mainDocName + " " + mainDocRev);
		footerInfo.put("dataLines", dataLineInfo);

		return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, footerInfo));
	}

	/**
	 * @param context
	 * @param info
	 * @return
	 * @throws Exception
	 */
	public com.matrixone.jdom.Document getDocumentMailXML(Context context, Map info) throws Exception
	{
		// get base url
		String baseURL = (String)info.get("baseURL");
		// get notification name
		String notificationName = (String)info.get("notificationName");
		HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
		String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
		String eventKey = "emxComponents.DOCUMENTS.Event." + eventName.replace(' ', '_');
		String bundleName = (String)info.get("bundleName");
		String locale = ((Locale)info.get("locale")).toString();
		Locale userLocale=new Locale(locale);
		String i18NEvent = EnoviaResourceBundle.getProperty(context, bundleName, userLocale, eventKey);
		String messageType = (String)info.get("messageType");

		// get document id
		String mainDocId = (String)info.get("id");
		// get document object info
		DomainObject mainDoc = DomainObject.newInstance(context, mainDocId);
		StringList selectList = new StringList(3);
		selectList.addElement(SELECT_TYPE);
		selectList.addElement(SELECT_NAME);
		selectList.addElement(SELECT_REVISION);
		Map mainDocInfo = mainDoc.getInfo(context, selectList);
		String mainDocType = (String)mainDocInfo.get(SELECT_TYPE);
		String i18NMainDocType = UINavigatorUtil.getAdminI18NString("type", mainDocType, locale);
		String mainDocName = (String)mainDocInfo.get(SELECT_NAME);
		String mainDocRev = (String)mainDocInfo.get(SELECT_REVISION);


		// header data
		HashMap headerInfo = new HashMap();
		headerInfo.put("header", i18NEvent + " : " + mainDocType + " " + XSSUtil.encodeForHTML(context, mainDocName) + " " + mainDocRev);

		// footer data
		HashMap footerInfo = new HashMap();
		ArrayList dataLineInfo = new ArrayList();
		if (messageType.equalsIgnoreCase("html"))
		{
			//String[] messageKeys = {"href", "type", "name", "revision"};
			String[] messageValues = new String[4];
			messageValues[0] = baseURL + "?objectId=" + mainDocId;
			messageValues[1] = i18NMainDocType;
			messageValues[2] = XSSUtil.encodeForHTML(context, mainDocName);
			messageValues[3] = mainDocRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxComponents.Object.Event.Html.Mail.ViewLink",
													 messageValues,null,
													 userLocale,bundleName);


			dataLineInfo.add(viewLink);
		} else {
			//String[] messageKeys = {"type", "name", "revision"};
			String[] messageValues = new String[3];
			messageValues[0] = i18NMainDocType;
			messageValues[1] = XSSUtil.encodeForHTML(context, mainDocName);
			messageValues[2] = mainDocRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxComponents.Object.Event.Text.Mail.ViewLink",
													 messageValues,null,
													 userLocale,bundleName);


			dataLineInfo.add(viewLink);
			dataLineInfo.add(baseURL + "?objectId=" + mainDocId);
		}

		footerInfo.put("dataLines", dataLineInfo);

		return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, null, footerInfo));

	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getContentMessageHTML(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "html");
		com.matrixone.jdom.Document doc = getContentMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getContentMessageText(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "text");
		com.matrixone.jdom.Document doc = getContentMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getDocumentMessageText(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "text");
		com.matrixone.jdom.Document doc = getDocumentMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getDocumentMessageHTML(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "html");
		com.matrixone.jdom.Document doc = getDocumentMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }
	     /**
     * Deletes the file versions when a master document is deleted.
     * @param context the context for this request.
     * @param args the parameters for this request.
     *   args[0] = objectid of document object.
     *
     */

    public int deleteFileVersions(Context context, String[] args) throws MatrixException
    {
        DomainObject domainObject = DomainObject.newInstance(context, args[0]);
        if (domainObject instanceof CommonDocument)
        {
            CommonDocument doc = (CommonDocument)domainObject;
            String attrVal = doc.getAttributeValue(context, CommonDocument.ATTRIBUTE_IS_VERSION_OBJECT);
            boolean isVersionObject = "true".equalsIgnoreCase(attrVal);
            if (!isVersionObject)
            {
                doc.purge(context, null);
            }
        }
        return 0;
    }

    /**
     * Access function for displaying the download action command
     * @param context the context for this request.
     * @param args the parameters for this request.
     *
     */
    public boolean hasDocumentDownloadAccess(Context context,String args[]) throws Exception
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
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
        selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        Map docObjectInfo = docObjet.getInfo(context,selectTypeStmts);
        return CommonDocument.canDownload(context, docObjectInfo);
    }

    /**
     * Access function for displaying the Checkout action command
     * @param context the context for this request.
     * @param args the parameters for this request.
     *
     */
    public boolean hasDocumentCheckOutAccess(Context context,String args[]) throws Exception
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
        selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
        selectTypeStmts.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
        selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        selectTypeStmts.add("vcmodule");
        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
        selectTypeStmts.add(CommonDocument.SELECT_REVISION);
        selectTypeStmts.add(CommonDocument.SELECT_LATEST_REVISION);

        Map docObjectInfo = docObjet.getInfo(context,selectTypeStmts);
        String docRevision=(String)docObjectInfo.get(CommonDocument.SELECT_REVISION);
        String latestRevision=(String)docObjectInfo.get(CommonDocument.SELECT_LATEST_REVISION);
        return (CommonDocument.canCheckout(context, docObjectInfo) &&  !(Boolean.valueOf((String)docObjectInfo.get(CommonDocument.SELECT_IS_VERSION_OBJECT))).booleanValue()
        		&&(latestRevision.equalsIgnoreCase(docRevision)));
    }

    /**
     * Access function for displaying the Checkin/Updatefiles action command
     * @param context the context for this request.
     * @param args the parameters for this request.
     *
     */
    public boolean hasDocumentCheckinAccess(Context context,String args[]) throws Exception
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
        selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
        selectTypeStmts.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
        selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
        selectTypeStmts.add(CommonDocument.SELECT_OWNER);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
        selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
        selectTypeStmts.add(CommonDocument.SELECT_REVISION);
        selectTypeStmts.add(CommonDocument.SELECT_LATEST_REVISION);
        Map docObjectInfo = docObjet.getInfo(context,selectTypeStmts);
        String docRevision=(String)docObjectInfo.get(CommonDocument.SELECT_REVISION);
        String latestRevision=(String)docObjectInfo.get(CommonDocument.SELECT_LATEST_REVISION);
        return (CommonDocument.canCheckin(context, docObjectInfo) &&  !(Boolean.valueOf((String)docObjectInfo.get(CommonDocument.SELECT_IS_VERSION_OBJECT))).booleanValue()
        	&&(latestRevision.equalsIgnoreCase(docRevision)));
    }

    /**
     * Access function for displaying the Checkin/upload files action command
     * @param context the context for this request.
     * @param args the parameters for this request.
     *
     */
    public boolean hasDocumentAddFilesAccess(Context context,String args[]) throws Exception
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
        selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
        selectTypeStmts.add(CommonDocument.SELECT_IS_VERSION_OBJECT);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS);
        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
        selectTypeStmts.add(CommonDocument.SELECT_LATEST_REVISION);
        selectTypeStmts.add(CommonDocument.SELECT_REVISION);
        Map docObjectInfo = docObjet.getInfo(context,selectTypeStmts);
        String docRevision=(String)docObjectInfo.get(CommonDocument.SELECT_REVISION);
        String latestRevision=(String)docObjectInfo.get(CommonDocument.SELECT_LATEST_REVISION);
        return (CommonDocument.canAddFiles(context, docObjectInfo) && !(Boolean.valueOf((String)docObjectInfo.get(CommonDocument.SELECT_IS_VERSION_OBJECT))).booleanValue()
        		&& (latestRevision.equalsIgnoreCase(docRevision)));
    }

    /**
     *  This method gets called from 'APPFileCheckOutFolderPreference' command to check if 
     *  applet is on or not and accordingly the command is displayed in the preference.
     *  
     * @deprecated since V6R2014x as the command will always be displayed 
     *  in the preference(IR-253253V6R2014x). This method should be removed in V6R2015.
     */
    @Deprecated
    public boolean hasCheckinAppletAccess(Context context,String args[]) throws Exception
    {

    	String checkinAppletValue = EnoviaResourceBundle.getProperty(context,"emxFramework.UseApplet");
    	if ("true".equalsIgnoreCase(checkinAppletValue)){
    		return true;
    	}
		return false;
    }

    /**
     * @param context
     * @param info
     * @return
     * @throws Exception
     */
    public com.matrixone.jdom.Document getFolderDocumentMailXML(Context context, Map info) throws Exception
    {
        // get base url
        String baseURL = (String)info.get("baseURL");
        // get notification name
        String notificationName = (String)info.get("notificationName");
        HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
        String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
        String eventKey = "emxComponents.DOCUMENTS.Event." + eventName.replace(' ', '_');
        String bundleName = (String)info.get("bundleName");
        String locale = ((Locale)info.get("locale")).toString();
        String i18NEvent = UINavigatorUtil.getI18nString(eventKey, bundleName, locale);
        String messageType = (String)info.get("messageType");

        // get document object id and info
        String versionDocId = MqlUtil.mqlCommand(context, "get env TOOBJECTID");
        DomainObject versionDoc = DomainObject.newInstance(context, versionDocId);
        StringList selectList = new StringList(3);
        selectList.addElement(SELECT_TYPE);
        selectList.addElement(SELECT_NAME);
        selectList.addElement(SELECT_REVISION);
        Map mainDocInfo = versionDoc.getInfo(context, selectList);
        String mainDocType = (String)mainDocInfo.get(SELECT_TYPE);
        String i18NMainDocType = UINavigatorUtil.getAdminI18NString("type", mainDocType, locale);
        String mainDocName = (String)mainDocInfo.get(SELECT_NAME);
        String mainDocRev = (String)mainDocInfo.get(SELECT_REVISION);


        // header data
        HashMap headerInfo = new HashMap();
        headerInfo.put("header", i18NEvent + " : " + mainDocType + " " + mainDocName + " " + mainDocRev);

        // footer data
        HashMap footerInfo = new HashMap();
        ArrayList dataLineInfo = new ArrayList();
        if (messageType.equalsIgnoreCase("html"))
        {
            //String[] messageKeys = {"href", "type", "name", "revision"};
            String[] messageValues = new String[4];
            messageValues[0] = baseURL + "?objectId=" + versionDocId;
            messageValues[1] = i18NMainDocType;
            messageValues[2] = mainDocName;
            messageValues[3] = mainDocRev;
            String viewLink = MessageUtil.getMessage(context,null,
                                                     "emxComponents.Object.Event.Html.Mail.ViewLink",
                                                     messageValues,null,
                                                     context.getLocale(),bundleName);


            dataLineInfo.add(viewLink);
        } else {
            //String[] messageKeys = {"type", "name", "revision"};
            String[] messageValues = new String[3];
            messageValues[0] = i18NMainDocType;
            messageValues[1] = mainDocName;
            messageValues[2] = mainDocRev;
            String viewLink = MessageUtil.getMessage(context,null,
                                                     "emxComponents.Object.Event.Text.Mail.ViewLink",
                                                     messageValues,null,
                                                     context.getLocale(),bundleName);


            dataLineInfo.add(viewLink);
            dataLineInfo.add(baseURL + "?objectId=" + versionDocId);
        }

        footerInfo.put("dataLines", dataLineInfo);

        return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, null, footerInfo));
    }

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getFolderContentMessageText(Context context, String[] args) throws Exception
	{
		Map info = (Map)JPO.unpackArgs(args);
		info.put("messageType", "text");
		com.matrixone.jdom.Document doc = getFolderDocumentMailXML(context, info);

		return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

	}

    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getFolderContentMessageHTML(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "html");
        com.matrixone.jdom.Document doc = getFolderDocumentMailXML(context, info);

        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }

    /**
     * Trigger to Auto promote the Document during creation conditionally based on a RACE VPLMAutoPromoteNextMinorRev
     * setting.  This trigger will check the setting and auto-promote if needed to support "collaborative creation mode".
     * @param context the Enovia <code>Context</code> object
     * @param args trigger parameters which include the list of policies to promote against and trigger event.
     *             for copy event, additional NEWTYPE, NEWNAME, NEWREV & NEWVAULT must be passed.
     * @throws Exception
     */
    public void autoPromoteCollaborativeMode(Context context, String args[]) throws Exception {
        String autoPrmote = MqlUtil.mqlCommand(context, "list expression $1 select $2 dump",
                                "VPLMAutoPromoteNextMinorRev", "value");
        if ("true".equalsIgnoreCase(autoPrmote)) {
            String objectId = getObjectId(context);
            String triggerPolicy = null;
            String event = args[1];
            if ("Revision".equalsIgnoreCase(event)) {
                String newRev = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4",
                        objectId, "next.id", "next.policy", "|");
                StringList revInfo = FrameworkUtil.split(newRev, "|");
                objectId = (String) revInfo.get(0);
                triggerPolicy = (String) revInfo.get(1);
            } else if ("Copy".equalsIgnoreCase(event)) {
                String objType  = args[2];
                String objName  = args[3];
                String objRev   = args[4];
                String objVault = args[5];
                String triggerInfo = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 in $4 select $5 $6 dump $7",
                        objType, objName, objRev, objVault, "id", "policy", "|");
                StringList revInfo = FrameworkUtil.split(triggerInfo, "|");
                objectId = (String) revInfo.get(0);
                triggerPolicy = (String) revInfo.get(1);
            } else {
                triggerPolicy = this.getPolicy(context).getName();
            }

            boolean historyOff = false;
            boolean triggerOff = true;
            boolean runAsSuperUser = true;

            //check intended policies passed with trigger
            String policies = args[0];
            StringList policyList = FrameworkUtil.split(policies, ",");
            for (int i=0; i < policyList.size(); i++) {
                String policy = policyList.get(i).toString().trim();
                String dbPolicy = PropertyUtil.getSchemaProperty(context, policy);
                if (triggerPolicy.equals(dbPolicy) || triggerPolicy.equals(policy)) {
                    MqlUtil.mqlCommand(context, historyOff, triggerOff, "promote bus $1", runAsSuperUser, objectId);
                    break;
                }
            }
        }
    }
	/**
     * Check Trigger to block the promotion of locked documents to release state 
     * This trigger will check if the document is locked, it wont allow it to be released
     * @param context the Enovia <code>Context</code> object
     * @param args trigger parameters which include the OBJECTID.
     * @throws Exception
     */
	public int blockPromotionForLockedDocuments(Context context, String[] args) throws Exception {
    	String oid = args[0];
    	String strLockedDocError = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",new Locale(context.getSession().getLanguage()),"emxComponents.Common.LockedDocumentAlertMsg");
        	
    	int status = 0;    	
    	if(isDocumentLocked(context, oid)){
    		emxContextUtilBase_mxJPO.mqlNotice(context, strLockedDocError);
    		status = 1;
    	}    	
    	return status;
    }

	private boolean isDocumentLocked(Context context, String oid) throws Exception, FrameworkException {		
		String relActiveVersion = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_relationship_ActiveVersion);
    	StringList busSelects = new StringList("from["+relActiveVersion+"].to.locker");
    	
    	DomainObject bus = new DomainObject(oid);
    	Map objDetails = bus.getInfo(context, busSelects);
    	StringList locker = (StringList) objDetails.get("from["+relActiveVersion+"].to.locker");
    	
    	boolean isLocked = false;
    	if(locker != null){
    		for(int count=0; count<locker.size(); count++){
        		if(UIUtil.isNotNullAndNotEmpty((String)locker.get(count))) {
        			isLocked = true;
        			break;
        		}
        	}
    	}
		return isLocked;
	}
}
