/*
 **  emxServiceFolderBase
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
import java.util.Iterator;

import matrix.db.*;
import matrix.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.Company;

public class emxServiceFolderBase_mxJPO extends emxDomainObject_mxJPO {

    /** A string constant with the value type_ServiceFolder */
    public static final String TYPE_SF = PropertyUtil.getSchemaProperty("type_ServiceFolder");

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
    public emxServiceFolderBase_mxJPO(Context context, String[] args) throws Exception {
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
     *                 if the operation fails *
     */
    public int mxMain(Context context, String[] args) throws Exception {
	return 0;
    }

    /**
     * Get the list of Service Folder objects which don't have parent Service
     * Folder.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds the following input arguments:
     * @return a <code>MapList</code> object having the list of Service Folder
     *         objects that don't have parent Service Folder.
     * @throws Exception
     *                 if the operation fails
     * @since ServiceManagement R207
     */
    public MapList getRootFolderList(Context context, String[] args) throws Exception {
	MapList folderList = new MapList();
	List lstFolderSelects = new StringList();
	lstFolderSelects.add(DomainConstants.SELECT_ID);
	String sRelServiceCategory = PropertyUtil.getSchemaProperty(context, "relationship_ServiceCategory");

	// get Service Folders
	// String strWhere = "from[" + sRelServiceCategory +"] != True";
	String strWhere = "";
	folderList = DomainObject.findObjects(context, TYPE_SF, "*", strWhere, (StringList) lstFolderSelects);
	return folderList;
    }

    /**
     * This method gets all the aggregated Services
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

	String sRelServiceCategory = PropertyUtil.getSchemaProperty(context, "relationship_ServiceCategory");
	String sTypeWebService = PropertyUtil.getSchemaProperty(context, "type_WebService");

	SelectList busSelects = new SelectList(4);
	busSelects.add(DomainConstants.SELECT_ID);
	busSelects.add(DomainConstants.SELECT_NAME);
	busSelects.add(DomainConstants.SELECT_REVISION);
	busSelects.add(DomainConstants.SELECT_DESCRIPTION);

	SelectList relSelects = new SelectList(1);
	relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

	DomainObject boKey = new DomainObject(objectId);

	MapList mapServiceList = boKey.expandSelect(context, sRelServiceCategory, sTypeWebService, busSelects, relSelects, false, true, (short) 1,
		"", "", null, false);
	return mapServiceList;
    }

    /**
     * This method gets all the aggregated Service Folders
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

	String sRelServiceCategory = PropertyUtil.getSchemaProperty(context, "relationship_ServiceCategory");
	String sTypeServiceFolder = PropertyUtil.getSchemaProperty(context, "type_ServiceFolder");

	SelectList busSelects = new SelectList(3);
	busSelects.add(DomainConstants.SELECT_ID);
	busSelects.add(DomainConstants.SELECT_NAME);
	busSelects.add(DomainConstants.SELECT_DESCRIPTION);

	SelectList relSelects = new SelectList(1);
	relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

	DomainObject boKey = new DomainObject(objectId);

	MapList mapServiceFolderList = boKey.expandSelect(context, sRelServiceCategory, sTypeServiceFolder, busSelects, relSelects, false, true,
		(short) 1, "", "", null, false);
	return mapServiceFolderList;
    }

    /**
     * Gets the vector output for the checkbox column in the access summary
     * table.
     * 
     * @param context
     *                the eMatrix <code>Context</code> object.
     * @param args
     *                contains a packed HashMap with the following entries:
     *                objectList - a MapList containing the actual maps
     *                "dataMap" containing the data. paramList - a HashMap
     *                containing the following parameters. 
     * @return Vector containing the true or false values.
     * @throws Exception
     *                 if the operation fails.
     * @since ServiceManagement R207
     */
    public Vector getUserCheckboxes(Context context, String[] args) throws Exception {
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	MapList objList = (MapList) programMap.get("objectList");
	HashMap paramList = (HashMap) programMap.get("paramList");
	int objListSize = objList.size();
	Vector columnVals = new Vector(objListSize);
	for (int k = 0; k < objListSize; k++) {
	    columnVals.addElement("true");
	}
	return columnVals;
    }

    /**
     * Get for the specified criteria
     * 
     * @param context
     *                the eMatrix <code>Context</code> object.
     * @param args
     *                contains a Map with the following entries: 
     * selType - a String containing the type(s) to search for 
     * txtName - a String containing a Name pattern to search for 
     * txtRev - a String containing a Revision pattern to search for 
     * txtDesc - a String containing a Description pattern to search for
     * vaultOption - a String containing a Vault pattern to search for 
     * txtOwner - a String containing an Owner pattern to search for 
     * txtOrginator - a String containing an Originator pattern to search for 
     * objectId - a String containing the root business object 
     * selType - a String containing the target Service Folder 
     * relType - a String containing the relationship between root bo and target bo
     * 
     * @return MapList containing search result.
     * @exception Exception
     *                    if the operation fails.
     * @since ServiceManagement R207
     */
    public MapList getFolderSearchResult(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);

	// Retrieve Search criteria
	String selType = (String) paramMap.get("selType");
	String txtName = (String) paramMap.get("txtName");
	String txtRev = (String) paramMap.get("txtRev");
	String txtOwner = (String) paramMap.get("txtOwner");
	String txtOriginator = (String) paramMap.get("txtOriginator");
	String txtDescription = (String) paramMap.get("txtDesc");
	String objectId = (String) paramMap.get("objectId");
	String relType = (String) paramMap.get("relType");

	String slkupOriginator = PropertyUtil.getSchemaProperty(context, "attribute_Originator");
	String sAnd = "&&";
	char chDblQuotes = '\"';

	/** ************************Vault Code Start**************************** */
	// Get the user's vault option & call corresponding methods to get
	// the vault's.
	String txtVault = "";
	String strVaults = "";
	StringList strListVaults = new StringList();

	String txtVaultOption = (String) paramMap.get("vaultOption");
	if (txtVaultOption == null) {
	    txtVaultOption = "";
	}
	if (txtVaultOption.equals("selected"))
	    txtVaultOption = (String) paramMap.get("selVaults");
	String vaultAwarenessString = (String) paramMap.get("vaultAwarenessString");
	if (vaultAwarenessString.equalsIgnoreCase("true")) {
	    if (txtVaultOption.equals("ALL_VAULTS") || txtVaultOption.equals("")) {
		strListVaults = com.matrixone.apps.common.Person.getCollaborationPartnerVaults(context, null);
		StringItr strItr = new StringItr(strListVaults);
		if (strItr.next()) {
		    strVaults = strItr.obj().trim();
		}
		while (strItr.next()) {
		    strVaults += "," + strItr.obj().trim();
		}
		txtVault = strVaults;
	    } else if (txtVaultOption.equals("LOCAL_VAULTS")) {
		com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
		Company company = person.getCompany(context);
		txtVault = company.getLocalVaults(context);
	    } else if (txtVaultOption.equals("DEFAULT_VAULT")) {
		txtVault = context.getVault().getName();
	    } else {
		txtVault = txtVaultOption;
	    }
	} else {
	    if (txtVaultOption.equals("ALL_VAULTS") || txtVaultOption.equals("")) {
		// get ALL vaults
		Iterator mapItr = VaultUtil.getVaults(context).iterator();
		if (mapItr.hasNext()) {
		    txtVault = (String) ((Map) mapItr.next()).get("name");

		    while (mapItr.hasNext()) {
			Map map = (Map) mapItr.next();
			txtVault += "," + (String) map.get("name");
		    }
		}
	    } else if (txtVaultOption.equals("LOCAL_VAULTS")) {
		// get All Local vaults
		strListVaults = VaultUtil.getLocalVaults(context);
		StringItr strItr = new StringItr(strListVaults);
		if (strItr.next()) {
		    strVaults = strItr.obj().trim();
		}
		while (strItr.next()) {
		    strVaults += "," + strItr.obj().trim();
		}
		txtVault = strVaults;
	    } else if (txtVaultOption.equals("DEFAULT_VAULT")) {
		txtVault = context.getVault().getName();
	    } else {
		txtVault = txtVaultOption;
	    }
	}
	// trimming
	txtVault = txtVault.trim();
	/** ************************Vault Code End****************************** */

	String queryLimit = (String) paramMap.get("queryLimit");
	if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")) {
	    queryLimit = "0";
	}

	if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0) {
	    txtName = "*";
	}
	if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0) {
	    txtRev = "*";
	}
	if (txtOwner == null || txtOwner.equalsIgnoreCase("null") || txtOwner.length() <= 0) {
	    txtOwner = "*";
	}
	if (txtDescription != null && !txtDescription.equalsIgnoreCase("null") && txtDescription.equals("*")) {
	    txtDescription = "";
	}
	if (txtOriginator != null && !txtOriginator.equalsIgnoreCase("null") && txtOriginator.equals("*")) {
	    txtOriginator = "";
	}
	String sWhereExp = "";

	if (!(txtOriginator == null || txtOriginator.equalsIgnoreCase("null") || txtOriginator.length() <= 0)) {
	    String sOriginatorQuery = "attribute[" + slkupOriginator + "] ~~ " + chDblQuotes + txtOriginator + chDblQuotes;
	    if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length() <= 0) {
		sWhereExp = sOriginatorQuery;
	    } else {
		sWhereExp += sAnd + " " + sOriginatorQuery;
	    }
	}

	if (!(txtDescription == null || txtDescription.equalsIgnoreCase("null") || txtDescription.length() <= 0)) {
	    String sDescQuery = "description ~~ " + chDblQuotes + txtDescription + chDblQuotes;
	    if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length() <= 0) {
		sWhereExp = sDescQuery;
	    } else {
		sWhereExp += sAnd + " " + sDescQuery;
	    }
	}

	SelectList resultSelects = new SelectList(1);
	resultSelects.add(DomainObject.SELECT_ID);

	MapList tmpresultList = null;
	MapList totalresultList = new MapList();

	tmpresultList = DomainObject.findObjects(context, selType, txtName, txtRev, txtOwner, txtVault, sWhereExp, null, true, resultSelects, Short
		.parseShort(queryLimit), null, null);

	int size2 = 0;
	int size3 = 0;
	MapList subServiceFoldersConnectedList = null;
	MapList parentServiceFoldersConnectedList = null;

	if (objectId != null && !objectId.equals("null")) {
	    DomainObject boServiceFolder = new DomainObject(objectId);
	    SelectList busSelects = new SelectList(1);
	    busSelects.add(DomainConstants.SELECT_ID);

	    SelectList relSelects = new SelectList(1);
	    relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

	    // Sub Service Folders
	    subServiceFoldersConnectedList = boServiceFolder.expandSelect(context, relType, TYPE_SF, busSelects, relSelects, false, true, (short) 1,
		    null, null, null, null, null, null, false);

	    // parent, grandparent, ... Service Folders
	    parentServiceFoldersConnectedList = boServiceFolder.expandSelect(context, relType, TYPE_SF, busSelects, relSelects, true, false,
		    (short) 0, null, null, null, null, null, null, false);

	    if (subServiceFoldersConnectedList != null) {
		size2 = subServiceFoldersConnectedList.size();
	    }
	    if (parentServiceFoldersConnectedList != null) {
		size3 = parentServiceFoldersConnectedList.size();
	    }
	}

	if (tmpresultList != null) {
	    for (int i = 0; i < tmpresultList.size(); i++) {
		boolean isValidated = true;
		Map item = (Map) tmpresultList.get(i);
		String id = (String) item.get(DomainObject.SELECT_ID);

		// 1. check if item is the root bo.
		if (id.equals(objectId)) {
		    isValidated = false;
		}

		// 2. check if the item is sub service folder
		if (isValidated && size2 > 0) {
		    for (int j = 0; j < size2; j++) {
			item = (Map) subServiceFoldersConnectedList.get(j);
			String childId = (String) item.get(DomainConstants.SELECT_ID);
			if (id.equals(childId)) {
			    isValidated = false;
			    break;
			}
		    }
		}

		// 3. check if equal to parent service folder
		if (isValidated && size3 > 0) {
		    for (int k = 0; k < size3; k++) {
			item = (Map) parentServiceFoldersConnectedList.get(k);
			String parentId = (String) item.get(DomainConstants.SELECT_ID);
			if (id.equals(parentId)) {
			    isValidated = false;
			    break;
			}
		    }
		}
		if (isValidated) {
		    totalresultList.add(tmpresultList.get(i));
		}
	    }
	}
	return totalresultList;
    }

}
