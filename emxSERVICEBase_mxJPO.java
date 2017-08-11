/*
 **  emxSERVICEBase
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
import com.matrixone.cbp.ws.keymgt.utils.HistoryUtils;

public class emxSERVICEBase_mxJPO extends emxDomainObject_mxJPO {

    /** A sting constant with the value type_SERVICE */
    public static final String TYPE_SERVICE = PropertyUtil.getSchemaProperty("type_SERVICE");

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
    public emxSERVICEBase_mxJPO(Context context, String[] args) throws Exception {
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
    public MapList getServiceSearchResult(Context context, String[] args) throws Exception {
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
	MapList aggregatedServicesConnectedList = null;

	if (objectId != null && !objectId.equals("null")) {
	    DomainObject boServiceFolder = new DomainObject(objectId);
	    SelectList busSelects = new SelectList(3);
	    busSelects.add(DomainConstants.SELECT_ID);

	    SelectList relSelects = new SelectList(1);
	    relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

	    // Sub Service Folders
	    aggregatedServicesConnectedList = boServiceFolder.expandSelect(context, relType, TYPE_SERVICE, busSelects, relSelects, true, true,
		    (short) 1, null, null, null, null, null, null, false);

	    if (aggregatedServicesConnectedList != null) {
		size2 = aggregatedServicesConnectedList.size();
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

		// 2. check if the item is aggregated service
		if (isValidated && size2 > 0) {
		    for (int j = 0; j < size2; j++) {
			item = (Map) aggregatedServicesConnectedList.get(j);
			String childId = (String) item.get(DomainConstants.SELECT_ID);
			if (id.equals(childId)) {
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
    
    
    /**
     * This method gets usage history associated to a SERVICE object.
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
    
    public Vector getHistoryPerson(Context context, String[] args) throws Exception {
    	//XSSOK
    	return getHistoryColumn(context, args, HistoryUtils.PERSON);
    }
    
    public Vector getHistoryPLMKey(Context context, String[] args) throws Exception {
    	//XSSOK
    	return getHistoryColumn(context, args, HistoryUtils.PLMKEY);
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
