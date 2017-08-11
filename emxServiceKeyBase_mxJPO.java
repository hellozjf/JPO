/*
 **  emxServiceKeyBase
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of MatrixOne,
 **  Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 */

import java.util.Map;
import java.util.Date;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.AttributeType;
import matrix.util.StringList;
import matrix.util.SelectList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.cbp.ws.keymgt.utils.PLMKeyUtils;
import com.matrixone.cbp.ws.keymgt.utils.HistoryUtils;

public class emxServiceKeyBase_mxJPO extends emxDomainObject_mxJPO {

    private static final String sAttrWSKeyApplicationID = PropertyUtil.getSchemaProperty("attribute_WSKeyApplicationID");
    private static final String sAttrWSKeyCredentials = PropertyUtil.getSchemaProperty("attribute_WSKeyCredentials");
    private static final String sAttrExpirationDate = PropertyUtil.getSchemaProperty("attribute_ExpirationDate");
    private static final String sAttrValidityType = PropertyUtil.getSchemaProperty("attribute_WSKeyValidityType");
    private static final String sTypeServiceKey = PropertyUtil.getSchemaProperty("type_ServiceKey");
    protected static final String DEFAULT = "Default";
    protected static final String ALL = "All";
    protected static final String SELECTED = "Selected";

    /**
     * Default Constructor.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds no arguments
     * @throws Exception
     *                 if the operation fails
     */
    public emxServiceKeyBase_mxJPO(Context context, String[] args) throws Exception {
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
     * This method is executed to get Service Keys in DB.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds arguments
     * @returns Object
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement R207
     */
    public Object getServiceKeys(Context context, String[] args) throws Exception {
	try {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap paramMap = (HashMap) programMap.get("paramList");
	    String objectId = (String) programMap.get("objectId");
	    MapList keyList = null;

	    StringList typeSelects = new StringList();
	    typeSelects.add(DomainObject.SELECT_ID);
	    typeSelects.add(DomainObject.SELECT_NAME);
	    typeSelects.add(DomainObject.SELECT_DESCRIPTION);
	    typeSelects.add("attribute[" + sAttrWSKeyApplicationID + "]");
	    typeSelects.add("attribute[" + sAttrWSKeyCredentials + "]");
	    typeSelects.add("attribute[" + sAttrExpirationDate + "]");

	    keyList = DomainObject.findObjects(context, sTypeServiceKey, // typepattern
		    QUERY_WILDCARD, // vault pattern
		    null, // where exp
		    typeSelects);
	    return keyList;
	} catch (Exception ex) {
	    throw ex;
	}
    }

    /**
     * getExpirationDateStatusIcon - gets the status gif to be shown in the
     * column of the Service Key Summary table
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - objectList
     *                MapList
     * @returns Object of type Vector
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement R207
     */
    public Vector getExpirationDateStatusIcon(Context context, String[] args) throws Exception {
	try {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    MapList objectList = (MapList) programMap.get("objectList");

	    Vector statusIconList = new Vector();
	    Date curDate = new Date();
	    String statusImageString;
	    Iterator objectListItr = objectList.iterator();
	    while (objectListItr.hasNext()) {
		Map objectMap = (Map) objectListItr.next();
		String strExpirationDate = (String) objectMap.get("attribute[" + sAttrExpirationDate + "]");

		if (strExpirationDate != null && !strExpirationDate.equals("")
			&& curDate.before(com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(strExpirationDate))) {
		    statusImageString = "<img border='0' src='../common/images/iconStatusGreen.gif' name='green' id='green' alt='*' />";
		} else {
		    statusImageString = "<img border='0' src='../common/images/iconStatusRed.gif' name='red' id='red' alt='*' />";
		}

		statusIconList.add(statusImageString);
	    }
	    //XSSOK
	    return statusIconList;
	} catch (Exception ex) {
	    throw ex;
	}
    }

    /**
     * This method gets all the Services associated with the ServiceKey
     * 
     * @param context
     *                the eMatrix <code>Context</code> object.
     * @param args
     *                holds objectId and param values.
     * @throws Exception
     *                 If the operation fails.
     * @since ServiceManagement R207
     */
    public MapList getServices(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	String objectId = (String) paramMap.get("objectId");

	String sRelServiceAccess = PropertyUtil.getSchemaProperty(context, "relationship_ServiceAccess");
	String sTypeWebService = PropertyUtil.getSchemaProperty(context, "type_WebService");

	SelectList busSelects = new SelectList(4);
	busSelects.add(DomainConstants.SELECT_ID);
	busSelects.add(DomainConstants.SELECT_NAME);
	busSelects.add(DomainConstants.SELECT_REVISION);
	busSelects.add(DomainConstants.SELECT_DESCRIPTION);

	SelectList relSelects = new SelectList(1);
	relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

	DomainObject boKey = new DomainObject(objectId);

	MapList mapServiceList = boKey.expandSelect(context, sRelServiceAccess, sTypeWebService, busSelects, relSelects, false, true, (short) 1, "",
		"", null, false);
	return mapServiceList;
    }

    /**
     * This method gets all the Service Folders associated with the ServiceKey
     * 
     * @param context
     *                the eMatrix <code>Context</code> object.
     * @param args
     *                holds objectId and param values.
     * @throws Exception
     *                 If the operation fails.
     * @since ServiceManagement R207
     */
    public MapList getServiceFolders(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	String objectId = (String) paramMap.get("objectId");

	String sRelServiceAccess = PropertyUtil.getSchemaProperty(context, "relationship_ServiceAccess");
	String sTypeServiceFolder = PropertyUtil.getSchemaProperty(context, "type_ServiceFolder");

	SelectList busSelects = new SelectList(3);
	busSelects.add(DomainConstants.SELECT_ID);
	busSelects.add(DomainConstants.SELECT_NAME);
	busSelects.add(DomainConstants.SELECT_DESCRIPTION);

	SelectList relSelects = new SelectList(1);
	relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

	DomainObject boKey = new DomainObject(objectId);

	MapList mapServiceFolderList = boKey.expandSelect(context, sRelServiceAccess, sTypeServiceFolder, busSelects, relSelects, false, true,
		(short) 1, "", "", null, false);
	return mapServiceFolderList;
    }

    public static Object getValidityTypeList(Context context, String[] args) throws Exception {
	AttributeType validityTypeAtt = new AttributeType(sAttrValidityType);
	validityTypeAtt.open(context);
	StringList validityTypeList = validityTypeAtt.getChoices(context);
	validityTypeAtt.close(context);
	HashMap resultMap = new HashMap();
	resultMap.put("field_choices", validityTypeList);
	resultMap.put("field_display_choices", validityTypeList);
	return resultMap;
    }

    /**
     * This method creates Service Key object
     * 
     * @param context
     *                The ematrix context of the request
     * @param args
     * 
     * @return a <code>HashMap</code> contains Action (CONTINUE or STOP) and
     *         Message (error message)
     * @throws Exception
     * @throws FrameworkException
     * @since ServiceManagement R207
     */
    public HashMap createServiceKey(Context context, String[] args) throws Exception, FrameworkException {

	HashMap returnMap = new HashMap();
	try {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    PLMKeyUtils.generatePLMKey(context, programMap);
	} catch (Exception ex) {
	    returnMap.put("Action", "STOP");
	    returnMap.put("Message", ex.toString());
	}
	return returnMap;
    }

    /**
     * This method renew Service Key object
     * 
     * @param context
     *                The ematrix context of the request
     * @param args
     * 
     * @return a <code>HashMap</code> contains Action (CONTINUE or STOP) and
     *         Message (error message)
     * @throws Exception
     * @throws FrameworkException
     * @since ServiceManagement R207
     */
    public HashMap renewServiceKey(Context context, String[] args) throws Exception, FrameworkException {

	HashMap returnMap = new HashMap();
	try {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    PLMKeyUtils.renewPLMKey(context, programMap);
	} catch (Exception ex) {
	    returnMap.put("Action", "STOP");
	    returnMap.put("Message", ex.toString());
	}
	return returnMap;
    }

    /**
     * Gets the Result for Persons Search
     * 
     * @param context
     *                The Matrix Context.
     * @param args
     *                holds input arguments.
     * @return maplist of Persons
     * @throws Exception
     *                 If the operation fails.
     * @since ServiceManagement R207
     */
    public MapList getPeopleSearchResult(Context context, String[] args) throws Exception {
	if (args.length == 0)
	    throw new IllegalArgumentException();
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	MapList mapList = new MapList();
	com.matrixone.apps.common.Company company = null;

	// Retrieve search criteria
	String UserName = (String) paramMap.get("UserName");
	String FirstName = (String) paramMap.get("FirstName");
	String LastName = (String) paramMap.get("LastName");
	String companyName = (String) paramMap.get("Company");
	String sQueryLimit = (String) paramMap.get("queryLimit");
	String vaultOption = (String) paramMap.get("vaultOption");
	String selectedVault = (String) paramMap.get("vaultName");
	String srcDestRelName = (String) paramMap.get("srcDestRelName");
	if (srcDestRelName != null && (!srcDestRelName.equals("")) && (!srcDestRelName.equalsIgnoreCase("null"))) {
	    srcDestRelName = PropertyUtil.getSchemaProperty(context, srcDestRelName);
	}
	String objectId = (String) paramMap.get("objectId");
	String strMode = (String) paramMap.get("searchmode");

	if (UserName == null || "".equals(UserName))
	    UserName = QUERY_WILDCARD;
	if (FirstName == null || "".equals(FirstName))
	    FirstName = QUERY_WILDCARD;
	if (LastName == null || "".equals(LastName))
	    LastName = QUERY_WILDCARD;

	String busWhere = "";
	if (UserName != null && UserName.trim().length() != 0 && !UserName.trim().equals(QUERY_WILDCARD)) {
	    if (busWhere.length() > 0) {
		busWhere += " && ";
	    }
	    if (UserName.indexOf(QUERY_WILDCARD) == -1 && UserName.indexOf("?") == -1) {
		busWhere += "(name == \"" + UserName + "\")";
	    } else {
		busWhere += "(name ~= \"" + UserName + "\")";
	    }
	}

	if (FirstName != null && FirstName.trim().length() != 0 && !FirstName.trim().equals(QUERY_WILDCARD)) {
	    if (busWhere.length() > 0) {
		busWhere += " && ";
	    }
	    if (FirstName.indexOf(QUERY_WILDCARD) == -1 && FirstName.indexOf("?") == -1) {
		busWhere += "(attribute[" + ATTRIBUTE_FIRST_NAME + "] == \"" + FirstName + "\")";
	    } else {
		busWhere += "(attribute[" + ATTRIBUTE_FIRST_NAME + "] ~= \"" + FirstName + "\")";
	    }
	}

	if (LastName != null && LastName.trim().length() != 0 && !LastName.trim().equals(QUERY_WILDCARD)) {
	    if (busWhere.length() > 0) {
		busWhere += " && ";
	    }
	    if (LastName.indexOf(QUERY_WILDCARD) == -1 && LastName.indexOf("?") == -1) {
		busWhere += "(attribute[" + ATTRIBUTE_LAST_NAME + "] == \"" + LastName + "\")";
	    } else {
		busWhere += "(attribute[" + ATTRIBUTE_LAST_NAME + "] ~= \"" + LastName + "\")";
	    }
	}
	String strVaults = "";
	StringList objectSelects = new StringList(1);
	objectSelects.addElement(SELECT_ID);
	if (companyName == null || companyName.trim().length() == 0) {
	    com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
	    company = person.getCompany(context);
	} else {
	    company = (com.matrixone.apps.common.Company) DomainObject.newInstance(context, DomainConstants.TYPE_COMPANY);// ,
	    // SpecificationCentralCommon.SPECIFICATION);
	    company.setId(companyName);
	}

	if (vaultOption == null) {
	    com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
	    strVaults = person.getVault();
	} else {
	    if (vaultOption.equals(ALL)) {
		strVaults = QUERY_WILDCARD;
	    } else if (vaultOption.equals(DEFAULT)) {
		com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
		strVaults = person.getVault();
	    } else if (vaultOption.equals(SELECTED)) {
		strVaults = selectedVault;
	    }
	}

	if (strVaults.indexOf(QUERY_WILDCARD) == -1) {
	    if (busWhere.length() > 0) {
		busWhere += " && ";
	    }
	    busWhere += "(vault == \"" + strVaults + "\")";
	}

	mapList = (MapList) getGlobalSearchRelObjects(context, company.getId(), RELATIONSHIP_EMPLOYEE, TYPE_PERSON, busWhere, sQueryLimit, "|", "@");

	return mapList;
    }

    /**
     * Gets the MapList containing related objects according to the search
     * criteria.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds input arguments.
     * @return a MapList containing search result.
     * @throws Exception
     *                 if the operation fails.
     * @since ServiceManagement R207
     */
    protected MapList getGlobalSearchRelObjects(matrix.db.Context context, String objectId, String relName, String typeName, String busWhere,
	    String sQueryLimit, String fieldSep, String recordSep) throws Exception {

	String strResult = MqlUtil.mqlCommand(context, "expand bus $1 from relationship $2 type $3 select bus id where $4 dump $5 recordsep $6 terse limit $7",
                                              objectId,relName,typeName,busWhere,fieldSep,recordSep,sQueryLimit);

	java.util.List resultList = FrameworkUtil.split(strResult, fieldSep, recordSep);

	Iterator itr = resultList.iterator();
	StringList resultMap = null;
	HashMap hashMap = null;
	MapList mapList = new MapList();
	while (itr.hasNext()) {
	    resultMap = (StringList) itr.next();
	    if (resultMap.size() > 0) {
		hashMap = new HashMap();
		hashMap.put(KEY_LEVEL, resultMap.elementAt(0));
		hashMap.put(SELECT_ID, resultMap.elementAt(4));
		mapList.add(hashMap);
	    }
	}
	return mapList;
    }
    
    /**
     * This method gets usage history associated to a Service Key object.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments: 0 - String containing
     *                Service object id.
     * @return MapList holds a list of history records.
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement V6R2009x
     */
    public MapList getUsageHistory(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	String keyID = (String) paramMap.get("objectId");
	return HistoryUtils.getUsageHistoryRecord(context, keyID);
    }
    
    public Vector getHistoryDate(Context context, String[] args) throws Exception {
    	//XSSOK
    	return getHistoryColumn(context, args, HistoryUtils.DATE);
    }
    
    public Vector getHistoryApplicationID(Context context, String[] args) throws Exception {
    	//XSSOK
    	return getHistoryColumn(context, args, HistoryUtils.APPLICATION_ID);
    }
    
    public Vector getHistoryServiceID(Context context, String[] args) throws Exception {
    	//XSSOK
    	return getHistoryColumn(context, args, HistoryUtils.SERVICE_ID);
    }
    
    public Vector getHistoryTimeSpent(Context context, String[] args) throws Exception {
    	//XSSOK
    	return getHistoryColumn(context, args, HistoryUtils.TIME_SPENT);
    }
    
    private Vector getHistoryColumn(Context context, String[] args, String colName) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	MapList objectList = (MapList) paramMap.get("objectList");	
	Vector valVactor = new Vector();
	if (objectList != null) {
	    for (int i = 0; i < objectList.size(); i++) {
		Map obj = (Map) objectList.get(i);
		String value = (String) obj.get(colName);
		if (value == null)
		    valVactor.add("");
		else
		    valVactor.add(XSSUtil.encodeForHTML(context, value));
	    }
	}
	return valVactor;
    }
}
