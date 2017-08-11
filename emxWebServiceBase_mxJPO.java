/*
 **  emxWebServiceBase
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of MatrixOne,
 **  Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 */

import java.util.Map;
import java.util.Vector;
import java.util.HashMap;

import matrix.db.*;
import matrix.util.List;
import matrix.util.StringList;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

public class emxWebServiceBase_mxJPO extends emxDomainObject_mxJPO {

    /** A sting constant with the value type_DeploymentInstance */
    public static final String TYPE_DI = PropertyUtil.getSchemaProperty("type_DeploymentInstance");
    /** A string constant with the value relationship_Deployment */
    public static final String RELATIONSHIP_SD = PropertyUtil.getSchemaProperty("relationship_ServiceDeployment");
    /** A string constant with the state value Deployed" */
    public static final String STATE_DEPLOYED = "Deployed";
    /** A string constant with the value emxComponentsStringResource. */
    public static final String RESOURCE_BUNDLE_WSManagement_STR = "emxWSManagementStringResource";

    private static final String FILENAME = "filename";
    private static final String MODIFIED = "modified";
    private static final String SIZE = "size";

    /**
     * Constructor
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds no arguments
     * @throws Exception
     *                 if the operation fails
     */
    public emxWebServiceBase_mxJPO(Context context, String[] args) throws Exception {
	super(context, args);
    }

    /**
     * Main entry point.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception
     *                 if the operation fails
     */
    public int mxMain(Context context, String[] args) throws Exception {
	return 0;
    }

    /**
     * This method gets Deployment Description filenames associated to a Service
     * object. (used by "Range Function" and "Range Program" setting defined in
     * a web form)
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - String containing
     *                Service object id
     * @return HashMap holds filenames
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement V6R2009x
     */
    public static Object getDescriptionFilenames(Context context, String[] args) throws Exception {
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	HashMap paramMap = (HashMap) programMap.get("paramMap");

	String wsId = (String) paramMap.get("objectId");
	DomainObject wsObj = new DomainObject(wsId);
	FileList files = wsObj.getFiles(context);

	HashMap tempMap = new HashMap();
	StringList fieldRangeValues = new StringList();
	for (int i = 0; i < files.size(); i++) {
	    File file = (File) files.elementAt(i);
	    fieldRangeValues.addElement(file.getName());
	}
	tempMap.put("field_choices", fieldRangeValues);
	tempMap.put("field_display_choices", fieldRangeValues);
	return tempMap;
    }

    /**
     * This method gets Deployment Description filename information associated
     * to a Service object.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - String containing
     *                Service object id.
     * @return MapList holds a list of description file informations. Each item
     *         holds: object id, filename, modified date, file size.
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement V6R2009x
     */
    public MapList getFileList(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	String wsId = (String) paramMap.get("objectId");
	DomainObject wsObj = new DomainObject(wsId);

	MapList fileMapList = new MapList();

	List lstSelects = new StringList();
	lstSelects.add(SELECT_FILE_NAME);
	lstSelects.add(SELECT_FILE_MODIFIED);
	lstSelects.add(SELECT_FILE_SIZE);
	Map fileInfo = wsObj.getInfo(context, (StringList) lstSelects);
	StringList filenameList = (StringList) fileInfo.get(SELECT_FILE_NAME);
	StringList fileModifiedList = (StringList) fileInfo.get(SELECT_FILE_MODIFIED);
	StringList fileSizeList = (StringList) fileInfo.get(SELECT_FILE_SIZE);
	if (filenameList != null && fileModifiedList != null && fileSizeList != null) {
	    for (int i = 0; i < filenameList.size(); i++) {
		String filename = (String) filenameList.elementAt(i);
		if (!filename.equals("")) {
		    Map fileMap = new HashMap();
		    fileMap.put("id", wsId);
		    fileMap.put(FILENAME, filename);
		    fileMap.put(MODIFIED, (String) fileModifiedList.elementAt(i));
		    fileMap.put(SIZE, (String) fileSizeList.elementAt(i));
		    fileMapList.add(fileMap);
		}
	    }
	}
	return fileMapList;
    }

    /**
     * This method gets Deployment Description filename information associated
     * to a Service object.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - MapList
     *                containing all description file information.
     * @return Vector holding a list of filenames.
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement V6R2009x
     */
    public Vector getFilename(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	MapList objectList = (MapList) paramMap.get("objectList");

	Vector vFilename = new Vector();
	if (objectList != null) {
	    for (int i = 0; i < objectList.size(); i++) {
		Map obj = (Map) objectList.get(i);
		String filename = (String) obj.get(FILENAME);
		if (filename == null)
		    vFilename.add("");
		else
		    vFilename.add(filename);
	    }
	}
	return vFilename;
    }

    /**
     * This method gets Deployment Description filename information associated
     * to a Service object.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - MapList
     *                containing all description file information.
     * @return Vector holding a list of file modified dates.
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement V6R2009x
     */
    public Vector getFileModified(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	MapList objectList = (MapList) paramMap.get("objectList");

	Vector vFileModified = new Vector();
	if (objectList != null) {
	    for (int i = 0; i < objectList.size(); i++) {
		Map obj = (Map) objectList.get(i);
		String fileModified = (String) obj.get(MODIFIED);
		if (fileModified == null)
		    vFileModified.add("");
		else
		    vFileModified.add(fileModified);
	    }
	}
	return vFileModified;
    }

    /**
     * This method gets Deployment Description filename information associated
     * to a Service object.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - MapList
     *                containing all description file information.
     * @return Vector holding a list of file sizes.
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement V6R2009x
     */
    public Vector getFileSize(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	MapList objectList = (MapList) paramMap.get("objectList");

	Vector vFileSize = new Vector();
	if (objectList != null) {
	    for (int i = 0; i < objectList.size(); i++) {
		Map obj = (Map) objectList.get(i);
		String fileSize = (String) obj.get(SIZE);
		if (fileSize == null)
		    vFileSize.add("");
		else
		    vFileSize.add(fileSize);
	    }
	}
	return vFileSize;
    }

    /**
     * This common check trigger method checks if all Deployment Definition
     * objects are in Inactive state.
     * 
     * @param context
     *                The ematrix context of the request
     * @param args
     *                String Array with following arguments: 0 - The Object Id
     *                of the Deployment Instance Object.
     * @throws Exception
     * @throws FrameworkException
     * @since ServiceManagement V6R2009x
     */
    public int checkInactivation(Context context, String[] args) throws Exception, FrameworkException {
	String wsId = args[0];
	DomainObject wsObj = new DomainObject(wsId);

	/*  12/17/2008:  Active and Inactive are reserved for the other purpose in the future rlease. so no check whether all DepInst objects are at Undeployed state or not
	List lstDepInstSelects = new StringList();
	List lstRelationshipSelects = new StringList();
	lstDepInstSelects.add(DomainConstants.SELECT_ID);
	lstDepInstSelects.add(DomainConstants.SELECT_CURRENT);
	// get linked Deployment Instances
	MapList lstDepInstObjects = wsObj.getRelatedObjects(context, RELATIONSHIP_SD, TYPE_DI, (StringList) lstDepInstSelects,
		(StringList) lstRelationshipSelects, false, true, (short) 1, "", "");

	for (int i = 0; i < lstDepInstObjects.size(); i++) {
	    Map depInstMap = (Map) lstDepInstObjects.get(i);
	    String depInstCurrent = (String) depInstMap.get(DomainConstants.SELECT_CURRENT);
	    // check the state of the linked Deployment Instance
	    if (depInstCurrent.equals(STATE_DEPLOYED)) {
		i18nNow i18nnow = new i18nNow();
		String strLanguage = context.getSession().getLanguage();
		String strAlertMessage = i18nnow.GetString(RESOURCE_BUNDLE_WSManagement_STR, strLanguage,
			"emxWSManagement.Inactivate.WebService.ErrorMessage1");
		${CLASS:emxContextUtil}.mqlNotice(context, strAlertMessage);
		return 1;
	    }
	}
	*/
	
	return 0;
    }
}
