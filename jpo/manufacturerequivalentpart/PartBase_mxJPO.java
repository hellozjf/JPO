package jpo.manufacturerequivalentpart;
/*
 ** PartBase.java
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.PolicyItr;
import matrix.db.PolicyList;
import matrix.db.RelationshipType;
import matrix.db.Vault;
import matrix.db.StateRequirementList;
import matrix.db.StateRequirement;

import matrix.util.List;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.jsystem.util.StringUtils;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.OrganizationUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.manufacturerequivalentpart.Part;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 *
 * The <code>Part</code> class in ManufacturerEquivalentPart...
 *
 * Copyright (c) 2007-2016 Dassault Systemes..
 */
public class PartBase_mxJPO extends DomainObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new PartBase JPO object.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     */

    public PartBase_mxJPO(Context context, String[] args) throws Exception {
        super();
    }

    protected static final String KEY_ALLOCATION_REL_ID = "AllocRespRelId";

    /** The String which is used to display content as label. */
    protected static final String KEY_LABEL_LOC_EQUIV = "labelLocEquiv";

    /**
     * Gets the Manufacturer Equivalent Parts attached to an Enterprise Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectId - a String
     *            containing the Enterprise Part id.
     * @return a MapList of Manufaturer Equivalent Part object ids and
     *         relationship ids.
     * @throws Exception
     *             if the operation fails.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEnterpriseManufacturerEquivalents(Context context,
            String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        String isMPN = (String) paramMap.get("isMPN");

        MapList equivList = new MapList();

        MapList listLocEquivMEPs = new MapList();
        MapList listCorpMEPs = new MapList();

        try {
            DomainObject partObj = DomainObject.newInstance(context, objectId);

            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);

            //IR-010504 - Starts
            Map mapLocIds               = new HashMap();
            String sFromRelId           = "from.relationship["+ RELATIONSHIP_LOCATION_EQUIVALENT +"].id";
            StringList selectRelStmts = new StringList(2);
            selectRelStmts.addElement(sFromRelId);
            //IR-010504 - Ends
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

            StringBuffer sbRelPattern = new StringBuffer(RELATIONSHIP_LOCATION_EQUIVALENT);

            //commented for Bug 371338
            //IR-010504 - Starts
            //reverting 371338 fix
            sbRelPattern.append(',');
            sbRelPattern.append(RELATIONSHIP_MANUFACTURER_EQUIVALENT);
            //IR-010504 - Ends

            StringBuffer typePattern = new StringBuffer(TYPE_PART);
            if (isMPN == null || isMPN.equalsIgnoreCase("True")) {
                typePattern.append(',');
                typePattern.append(TYPE_MPN);
            }

            StringBuffer sbTypePattern = new StringBuffer(typePattern.toString());
            sbTypePattern.append(',');
            sbTypePattern.append(TYPE_LOCATION_EQUIVALENT_OBJECT);

            // fetching list of related MEPs via location Equivalent Object
            listLocEquivMEPs = partObj.getRelatedObjects(context, sbRelPattern
                    .toString(), // relationship pattern
                    sbTypePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 2, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            // fetching list of related MEPs via Manufacturer Equivalent
            listCorpMEPs = partObj.getRelatedObjects(context,
                    RELATIONSHIP_MANUFACTURER_EQUIVALENT, // relationship
                    // pattern
                    typePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            Map tempMap = null;

            // to hold ids of MEP connected to EP
            Vector vecMepId = new Vector();
            HashMap mapMepId = new HashMap();
            // to hold relIds with which MEPs are connected to EP
            // will hold Location Equiv rel id if MEP is Location Context
            // will hold Manufacturer Equiv rel id if MEP is Corporate Context
            Vector vecRelId = new Vector();

            // Iterating to Location Context MEP list to load MEP Ids and rel
            // Ids
            for (int i = 0; i < listLocEquivMEPs.size(); i++) {
                tempMap = (Map) listLocEquivMEPs.get(i);

                // Checking for level in resultlist: level 1 will have
                // relationship id
                // level 2 will have mep object id
                // Adding id of MEP and relationship id for type Location
                // Equivalent
                if ("2".equals((String) tempMap.get("level"))) {
                    vecMepId.addElement(tempMap.get(SELECT_ID));
                    mapMepId.put(tempMap.get(SELECT_ID), tempMap);
                    //IR-010504 - Starts
                    mapLocIds.put(tempMap.get(SELECT_ID), tempMap.get(sFromRelId));
                    //IR-010504 - Ends
                }

                if ("1".equals((String) tempMap.get("level"))) {
                    vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
                }
            }// end of for (listCorpMEPs)

            // Iterating to Corporate Context MEP list to load MEP Ids and rel
            // Ids
            for (int i = 0; i < listCorpMEPs.size(); i++) {
                tempMap = (Map) listCorpMEPs.get(i);

                vecMepId.addElement(tempMap.get(SELECT_ID));
                mapMepId.put(tempMap.get(SELECT_ID), tempMap);
                vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
            }// end of for (listCorpMEPs)

            for (int k = 0; k < vecMepId.size(); k++) {
                Map resultMap = (Map) mapMepId.get((String) vecMepId
                        .elementAt(k));
                resultMap.remove("level");// need to be removed , else show
                // message as - the level sequence
                // may not be as expected..
                //IR-010504 - Starts
                //if mep id is obtained by expanding through Location Equivalent
                //relation then Loc Equi rel id has to be substituted
                //in place of Manufacturer Equivalent rel id.
                /*
                resultMap.put(SELECT_RELATIONSHIP_ID, (String) vecRelId
                        .elementAt(k));
                */
                String sLocEquiRelId = (String) mapLocIds.get(vecMepId.elementAt(k));
                if (sLocEquiRelId != null && !"".equals(sLocEquiRelId)) {
                    resultMap.put(SELECT_RELATIONSHIP_ID, sLocEquiRelId);
                }
                //IR-010504 - Ends
                equivList.add(resultMap);
            }
        } catch (FrameworkException Ex) {
            throw Ex;
        }

        return equivList;
    }

    /**
     * Gets the MEP names blocking MPN Names.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds object id.
     * @return a StringList of MEP & MPN Names.
     * @throws Exception
     *             If the operation fails.
     */
    public StringList getMEPNames(Context context, String[] args)
            throws Exception {
        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            Map paramList = (HashMap) paramMap.get("paramList");
            String suiteDir = (String) paramList.get("SuiteDirectory");
            String suiteKey = (String) paramList.get("suiteKey");
            String jsTreeID = (String) paramList.get("jsTreeID");
            String reportFormat = (String) paramList.get("reportFormat");

            boolean isexport = false;
            String export = (String) paramList.get("exportFormat");
            if (export != null) {
                isexport = true;
            }

            String publicPortal = (String) paramList.get("publicPortal");
            String linkFile = (publicPortal != null && publicPortal
                    .equalsIgnoreCase("true")) ? "emxNavigator.jsp"
                    : "emxTree.jsp";

            String parentOID = (String) paramList.get("objectId");

            MapList objectList = (MapList) paramMap.get("objectList");

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                int objectListSize = objectList.size();
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);

                    String type = (String) dataMap.get(SELECT_TYPE);
                    String strId = (String) dataMap.get(SELECT_ID);
                    String displayValue = "&nbsp;";

                    DomainObject dmObject = newInstance(context, strId);
                    String sPolicyClassification = dmObject.getInfo(context, "policy.property[PolicyClassification].value");
                    String sTypeIcon = getIcons(context,type,sPolicyClassification);
                    String imgSrc = "<img src='images/" + sTypeIcon
                            + "' border='0'>";

                    if (type.equals(TYPE_MPN)) {
                        String showBlankName = FrameworkProperties
                                .getProperty(context, "emxManufacturerEquivalentPart.EngrPlaceholderMEP.ShowBlankName");
                        if ("false".equalsIgnoreCase(showBlankName)) {
							displayValue = EnoviaResourceBundle.getProperty(context ,
                                            "emxManufacturerEquivalentPartStringResource",
                                            context.getLocale(),"emxManufacturerEquivalentPart.EngrPlaceholderMEP.DefaultName");

                        } else {
                            imgSrc = "";
                        }
                    } else {
                        displayValue = (String) dataMap.get(SELECT_NAME);
                    }

                    StringBuffer output = new StringBuffer(" ");
                    // do not show hyperlinks if it is a printer friendly or
                    // excel export page
                    // length will be >0 when format is HTML, ExcelHTML, CSV or
                    // TXT
                    if (isexport) {
                        output.append(displayValue);
                    } else if (null != reportFormat
                            && !reportFormat.equals("null")
                            && (reportFormat.length() > 0)) {
                        output.append(imgSrc);
						output.append("&nbsp;");
						output.append(displayValue);
                    } else {
                        output
                                .append("<table border=\"0\"><tr><td valign=\"top\">");
                        output
                                .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                        + linkFile + "?emxSuiteDirectory=");
                        output.append(suiteDir);
                        output.append("&suiteKey=");
                        output.append(suiteKey);
                        output.append("&jsTreeID=");
                        output.append(jsTreeID);
                        output.append("&parentOID=");
                        output.append(parentOID);
                        output.append("&relId=");
                        output.append((String) dataMap
                                .get(SELECT_RELATIONSHIP_ID));
                        output.append("&objectId=");
                        output.append(strId);
                        output
                                .append("', '700', '600', 'false', 'popup', '')\" class=\"object\">");
                        output.append(imgSrc);
                        output.append("</a></td>");
                        if (!"&nbsp;".equals(displayValue)) {
                            output.append("<td>&nbsp;");
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + linkFile + "?emxSuiteDirectory=");
                            output.append(suiteDir);
                            output.append("&suiteKey=");
                            output.append(suiteKey);
                            output.append("&jsTreeID=");
                            output.append(jsTreeID);
                            output.append("&parentOID=");
                            output.append(parentOID);
                            output.append("&relId=");
                            output.append((String) dataMap
                                    .get(SELECT_RELATIONSHIP_ID));
                            output.append("&objectId=");
                            output.append((String) dataMap.get(SELECT_ID));
                            output
                                    .append("', '700', '600', 'false', 'popup', '')\" class=\"object\">");
                            output.append(displayValue);
                            output.append("</a>&nbsp;</td>");
                        }
                        output.append("</tr></table>");
                    }

                    result.add(output.toString());
                }

            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }
        return result;

    }
    /**
     * Gets the MEP names blocking MPN Names.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds object id.
     * @return a StringList of MEP & MPN Names.
     * @throws Exception
     *             If the operation fails.
     */
    public StringList getMEPNamesSB(Context context, String[] args)
    throws Exception {
		StringList result = new StringList();
	try {
		    HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		
		    Map paramList = (HashMap) paramMap.get("paramList");
		    String suiteDir = (String) paramList.get("SuiteDirectory");
		    String suiteKey = (String) paramList.get("suiteKey");
		    String jsTreeID = (String) paramList.get("jsTreeID");
		    String reportFormat = (String) paramList.get("reportFormat");
		
		    boolean isexport = false;
		    String export = (String) paramList.get("exportFormat");
		    if (export != null) {
		        isexport = true;
		    }
		
		    String publicPortal = (String) paramList.get("publicPortal");
		    String linkFile = (publicPortal != null && publicPortal
		            .equalsIgnoreCase("true")) ? "emxNavigator.jsp"
		            : "emxTree.jsp";
		
		    String parentOID = (String) paramList.get("objectId");
		
		    MapList objectList = (MapList) paramMap.get("objectList");

    if (objectList != null && objectList.size() > 0) {
        // construct array of ids
        int objectListSize = objectList.size();
        for (int i = 0; i < objectListSize; i++) {
            Map dataMap = (Map) objectList.get(i);

            String type = (String) dataMap.get(SELECT_TYPE);
            String strId = (String) dataMap.get(SELECT_ID);
            String displayValue = "&#160;";

            DomainObject dmObject = newInstance(context, strId);
            String sPolicyClassification = dmObject.getInfo(context, "policy.property[PolicyClassification].value");
            String sTypeIcon = getIcons(context,type,sPolicyClassification);
            String imgSrc = "<img src='../common/images/" + sTypeIcon + "' border='0' alt='*'/>";

            if (type.equals(TYPE_MPN)) {
                String showBlankName = FrameworkProperties
                        .getProperty(context, "emxManufacturerEquivalentPart.EngrPlaceholderMEP.ShowBlankName");
                if ("false".equalsIgnoreCase(showBlankName)) {
                   
					displayValue = EnoviaResourceBundle.getProperty(context ,
                                    "emxManufacturerEquivalentPartStringResource",
                                    context.getLocale(),"emxManufacturerEquivalentPart.EngrPlaceholderMEP.DefaultName");

                } else {
                    imgSrc = "";
                }
            } else {
                displayValue = (String) dataMap.get(SELECT_NAME);
            }

            StringBuffer output = new StringBuffer(" ");
           
            if (isexport) {
                output.append(XSSUtil.encodeForHTML(context, displayValue));
            } else if (null != reportFormat
                    && !reportFormat.equals("null")
                    && (reportFormat.length() > 0)) {
                output.append(XSSUtil.encodeForHTML(context,imgSrc));
				output.append("&nbsp;");
				output.append(XSSUtil.encodeForHTML(context,displayValue));
            } else {
               
                    output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                    + XSSUtil.encodeForURL(context,linkFile) + "?emxSuiteDirectory=");
                    output.append(XSSUtil.encodeForURL(context,suiteDir));
                    output.append("&amp;suiteKey=");
                    output.append(XSSUtil.encodeForURL(context,suiteKey));
                    output.append("&amp;jsTreeID=");
                    output.append(XSSUtil.encodeForURL(context,jsTreeID));
                    output.append("&amp;parentOID=");
                    output.append(XSSUtil.encodeForURL(context,parentOID));
                    output.append("&amp;relId=");
                    output.append(XSSUtil.encodeForURL(context,(String) dataMap.get(SELECT_RELATIONSHIP_ID)));
                    output.append("&amp;objectId=");
                    output.append(XSSUtil.encodeForURL(context,(String) dataMap.get(SELECT_ID)));
                    output.append("', '700', '600', 'false', 'popup', '')\" class=\"object\">");
                    output.append(imgSrc);
                    output.append(XSSUtil.encodeForXML(context,displayValue));
                    output.append("</a>");
              
            }

            result.add(output.toString());
        }

    }// end if

} catch (Exception Ex) {
    throw Ex;
}
return result;

}
    /**
     * Gets the MEP Types blocking MPN Types.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds object id.
     * @return a StringList of MEP & MPN Types.
     * @throws Exception
     *             If the operation fails.
     */
    public StringList getMEPTypes(Context context, String[] args)
            throws Exception {
        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramList = (HashMap) paramMap.get("paramList");
            String languageStr = (String) paramList.get("languageStr");
            MapList objectList = (MapList) paramMap.get("objectList");

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                int objectListSize = objectList.size();
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);

                    String type = (String) dataMap.get(SELECT_TYPE);

                    if (!type.equals(TYPE_MPN)) {
                        result
                                .add(i18nNow.getTypeI18NString(type,
                                        languageStr));
                    } else {
                        result.add(" ");
                    }
                }

            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }
        return result;
    }

    /**
     * Gets the MEP Revisions blocking MPN Revisions.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds object id.
     * @return a StringList of MEP & MPN Revisions.
     * @throws Exception
     *             If the operation fails.
     */
    public StringList getMEPRevisions(Context context, String[] args)
            throws Exception {
        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            MapList objectList = (MapList) paramMap.get("objectList");

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                int objectListSize = objectList.size();
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);

                    String type = (String) dataMap.get(SELECT_TYPE);

                    if (!type.equals(TYPE_MPN)) {
                        result.add((String) dataMap.get(SELECT_REVISION));
                    } else {
                        result.add(" ");
                    }
                }

            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }
        return result;

    }

    /**
     * Gets the new Window Column for Launch but not for Channel.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds object id.
     * @return Boolean, whether to show new window column or not.
     * @throws Exception
     *             If the operation fails.
     */
    public Boolean isNewWindowViewable(Context context, String[] args)
            throws Exception {
        Boolean isColumnVisible = Boolean.FALSE;
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            String launched = (String) paramMap.get("launched");
            String portalMode = (String) paramMap.get("portalMode");

            if ((launched != null && launched.equalsIgnoreCase("true"))
                    && (portalMode != null && portalMode
                            .equalsIgnoreCase("false"))) {
                isColumnVisible = Boolean.TRUE;
            }
        } catch (Exception Ex) {
            throw Ex;
        }
        return isColumnVisible;

    }

    /**
     * Gets the Names of Location(s) with which a Manufacturer Equivalent Part
     * is connected.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectList - a
     *            MapList of object information. paramList - a Map of parameter
     *            values, SuiteDirectory, suiteKey, reportFormat, publicPortal.
     * @return a StringList HTML output of Names of Location(s).
     * @throws Exception
     *             if the operation fails.
     */

    public StringList getMEPLocationNamesHTMLOutput(Context context,
            String[] args) throws Exception {
        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (HashMap) paramMap.get("paramList");
            String suiteDir = (String) paramList.get("SuiteDirectory");
            String suiteKey = (String) paramList.get("suiteKey");
            String reportFormat = (String) paramList.get("reportFormat");
            boolean isexport = false;
            String export = (String) paramList.get("exportFormat");
            if (export != null) {
                isexport = true;
            }

            String publicPortal = (String) paramList.get("publicPortal");
            String linkFile = (publicPortal != null && publicPortal
                    .equalsIgnoreCase("true")) ? "emxNavigator.jsp"
                    : "emxTree.jsp";

            MapList objectList = (MapList) paramMap.get("objectList");

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);
                    oidList[i] = (String) dataMap.get("id");
                }

                StringList selects = new StringList();
                // Select for Location Name and Id via the relationship route
                // Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResNameSel = "to["
                        + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].from.name";
                String manEquAloResIdSel = "to["
                        + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";

                // Select for Location Name and Id via the relationship route
                // Part --> Manufacture Equivalent --> Allocation Responsibility
                String aloResNameSel = "to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].from.name";
                String aloResIdSel = "to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";

                DomainObject.MULTI_VALUE_LIST.add(manEquAloResNameSel);
                selects.add(manEquAloResNameSel);

                DomainObject.MULTI_VALUE_LIST.add(manEquAloResIdSel);
                selects.add(manEquAloResIdSel);

                // Select Location Name and Id via the relationship route
                // Part --> Allocation Responsibility
                DomainObject.MULTI_VALUE_LIST.add(aloResNameSel);
                selects.add(aloResNameSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResIdSel);
                selects.add(aloResIdSel);

                // Get Location Name and Id information
                MapList locMaplist = getInfo(context, oidList, selects);

                StringList manEquAloResNameList = null;
                StringList manEquAloResIdList = null;
                StringList aloResNameList = null;
                StringList aloResIdList = null;

                Iterator locMapListItr = locMaplist.iterator();
                while (locMapListItr.hasNext()) {
                    Map locMap = (Map) locMapListItr.next();
                    // Get Location Name and Id via the relationship route
                    // Part --> Manufacture Equivalent --> Allocation
                    // Responsibility
                    manEquAloResNameList = (StringList) locMap
                            .get(manEquAloResNameSel);
                    manEquAloResIdList = (StringList) locMap
                            .get(manEquAloResIdSel);

                    // Get Location Name and Id via the relationship route
                    // Part --> Allocation Responsibility
                    aloResNameList = (StringList) locMap.get(aloResNameSel);
                    aloResIdList = (StringList) locMap.get(aloResIdSel);

                    if (manEquAloResNameList == null) {
                        manEquAloResNameList = new StringList();
                    }
                    if (aloResNameList == null) {
                        aloResNameList = new StringList();
                    }

                    if (manEquAloResIdList == null) {
                        manEquAloResIdList = new StringList();
                    }
                    if (aloResIdList == null) {
                        aloResIdList = new StringList();
                    }

                    // index of the last equiv location
                    int lastEquivLoc = -1;

                    // save the last equivalent location
                    if (manEquAloResNameList != null
                            && manEquAloResNameList.size() > 0) {
                        lastEquivLoc = manEquAloResNameList.size();
                    }
                    // Combine the above two Name lists intto one Name list
                    if (aloResNameList != null && aloResNameList.size() > 0) {
                        for (int i = 0; i < aloResNameList.size(); i++) {
                            manEquAloResNameList.add((String) aloResNameList
                                    .get(i));
                        }
                    }

                    // Combine the above two Id lists into one Id list
                    if (aloResIdList != null && aloResIdList.size() > 0) {
                        for (int j = 0; j < aloResIdList.size(); j++) {
                            manEquAloResIdList
                                    .add((String) aloResIdList.get(j));
                        }
                    }

                    StringBuffer output = new StringBuffer(32);
					output.append(' ');

                    Iterator locNameListItr = manEquAloResNameList.iterator();
                    Iterator locIdListItr = manEquAloResIdList.iterator();
                    int equivLocCount = 0;

                    while (locNameListItr.hasNext() && locIdListItr.hasNext()) {
                        String locName = (String) locNameListItr.next();
                        String locId = (String) locIdListItr.next();

                        // display the "(equiv)" if the relationship route is
                        // Part --> Manufacture Equivalent --> Allocation
                        // Responsibility
                        equivLocCount++;
                        String labelLocEquiv = (lastEquivLoc >= equivLocCount) ? "(equiv)"
                                : "";

                        if (isexport) {
                            output.append(XSSUtil.encodeForHTML(context, locName));
                        }
                        // do not show hyperlinks if it is a printer friendly or
                        // excel export page
                        // length will be >0 when format is HTML, ExcelHTML, CSV
                        // or TXT
                        else if (null != reportFormat
                                && !reportFormat.equals("null")
                                && (reportFormat.length() > 0)) {
                            output.append(XSSUtil.encodeForHTML(context,locName));
                        } else {
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + XSSUtil.encodeForURL(context,linkFile) + "?emxSuiteDirectory=");
                            output.append(XSSUtil.encodeForURL(context,suiteDir));
                            output.append("&amp;suiteKey=");
                            output.append(XSSUtil.encodeForURL(context,suiteKey));
                            output.append("&amp;objectId=");
                            output.append(XSSUtil.encodeForURL(context,locId));
                            output.append("', '', '', 'false', 'popup', '')\">");
                            output.append(XSSUtil.encodeForHTML(context,locName));
                            output.append("</a><br> ");
                            output.append(XSSUtil.encodeForHTML(context,labelLocEquiv));
                            output.append(" </br>&#160;<br></br>");
                        }
                    }

                    if (!"".equals(output.toString())) {
                        result.add(output.toString());
                    }
                }// end while
                // Added for Bug 313092
                DomainObject.MULTI_VALUE_LIST.remove(manEquAloResNameSel);
                DomainObject.MULTI_VALUE_LIST.remove(manEquAloResIdSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResNameSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResIdSel);
            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }

        return result;

    }// end of method getMEPLocationNamesHTMLOutput()

    /**
     * Gets the Names of Location(s) with which a Manufacturer Equivalent Part
     * is connected for Structure Browser.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectList - a
     *            MapList of object information. paramList - a Map of parameter
     *            values, SuiteDirectory, suiteKey, reportFormat, publicPortal.
     * @return a StringList HTML output of Names of Location(s).
     * @throws Exception
     *             if the operation fails.
     */

    public StringList getMEPLocationNamesHTMLOutputSB(Context context, String[] args) 
    throws Exception 
    {
    	StringList result = new StringList();
    	try {
    		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    		Map paramList = (HashMap) paramMap.get("paramList");
    		String suiteDir = (String) paramList.get("SuiteDirectory");
    		String suiteKey = (String) paramList.get("suiteKey");
    		String reportFormat = (String) paramList.get("reportFormat");
    		boolean isexport = false;
    		String export = (String) paramList.get("exportFormat");
    		if (export != null) {
    			isexport = true;
    		}

    		String publicPortal = (String) paramList.get("publicPortal");
    		String linkFile = (publicPortal != null && publicPortal.equalsIgnoreCase("true")) ? "emxNavigator.jsp": "emxTree.jsp";

    		MapList objectList = (MapList) paramMap.get("objectList");

    		if (objectList != null && objectList.size() > 0) {
    			// construct array of ids
    			/*int objectListSize = objectList.size();
    			String[] oidList = new String[objectListSize];
    			for (int i = 0; i < objectListSize; i++) {
    				Map dataMap = (Map) objectList.get(i);
    				oidList[i] = (String) dataMap.get("id");
    			}*/

    			//StringList selects = new StringList();
    			// Select for Location Name and Id via the relationship route
    			// Part --> Manufacture Equivalent --> Allocation Responsibility
    			String manEquAloResNameSel = "to["
    				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
    				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
    			
    			String manEquAloResIdSel = "to["
    				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
    				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";

    			// Select for Location Name and Id via the relationship route
    			// Part --> Manufacture Equivalent --> Allocation Responsibility
    			String aloResNameSel = "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
    			String aloResIdSel = "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";

    			/*DomainObject.MULTI_VALUE_LIST.add(manEquAloResNameSel);
    			selects.add(manEquAloResNameSel);

    			DomainObject.MULTI_VALUE_LIST.add(manEquAloResIdSel);
    			selects.add(manEquAloResIdSel);

    			// Select Location Name and Id via the relationship route
    			// Part --> Allocation Responsibility
    			DomainObject.MULTI_VALUE_LIST.add(aloResNameSel);
    			selects.add(aloResNameSel);

    			DomainObject.MULTI_VALUE_LIST.add(aloResIdSel);
    			selects.add(aloResIdSel);

    			// Get Location Name and Id information
    			MapList locMaplist = getInfo(context, oidList, selects);*/

    			StringList manEquAloResNameList = null;
    			StringList manEquAloResIdList = null;
    			StringList aloResNameList = null;
    			StringList aloResIdList = null;

    			//Iterator locMapListItr = locMaplist.iterator();
    			Iterator locMapListItr=objectList.iterator();
    			while (locMapListItr.hasNext()) {
    				Map locMap = (Map) locMapListItr.next();
    				// Get Location Name and Id via the relationship route
    				// Part --> Manufacture Equivalent --> Allocation
    				// Responsibility
    				manEquAloResNameList = (StringList) locMap.get(manEquAloResNameSel);
    				manEquAloResIdList = (StringList) locMap.get(manEquAloResIdSel);

    				// Get Location Name and Id via the relationship route
    				// Part --> Allocation Responsibility
    				aloResNameList = (StringList) locMap.get(aloResNameSel);
    				aloResIdList = (StringList) locMap.get(aloResIdSel);

    				if (manEquAloResNameList == null) {
    					manEquAloResNameList = new StringList();
    				}
    				if (aloResNameList == null) {
    					aloResNameList = new StringList();
    				}

    				if (manEquAloResIdList == null) {
    					manEquAloResIdList = new StringList();
    				}
    				if (aloResIdList == null) {
    					aloResIdList = new StringList();
    				}

    				// index of the last equiv location
    				int lastEquivLoc = -1;

    				// save the last equivalent location
    				if (manEquAloResNameList != null
    						&& manEquAloResNameList.size() > 0) {
    					lastEquivLoc = manEquAloResNameList.size();
    				}
    				// Combine the above two Name lists intto one Name list
    				if (aloResNameList != null && aloResNameList.size() > 0) {
    					for (int i = 0; i < aloResNameList.size(); i++) {
    						manEquAloResNameList.add((String) aloResNameList.get(i));
    					}
    				}

    				// Combine the above two Id lists into one Id list
    				if (aloResIdList != null && aloResIdList.size() > 0) {
    					for (int j = 0; j < aloResIdList.size(); j++) {
    						manEquAloResIdList.add((String) aloResIdList.get(j));
    					}
    				}

    				StringBuffer output = new StringBuffer(32);
					output.append(' ');

    				Iterator locNameListItr = manEquAloResNameList.iterator();
    				Iterator locIdListItr = manEquAloResIdList.iterator();
    				int equivLocCount = 0;

    				while (locNameListItr.hasNext() && locIdListItr.hasNext()) {
    					String locName = (String) locNameListItr.next();
    					String locId = (String) locIdListItr.next();

    					// display the "(equiv)" if the relationship route is
    					// Part --> Manufacture Equivalent --> Allocation
    					// Responsibility
    					equivLocCount++;
    					String labelLocEquiv = (lastEquivLoc >= equivLocCount) ? "(equiv)" : "";

    					if (isexport) {
    						output.append(XSSUtil.encodeForHTML(context, locName));
    					}
    					// do not show hyperlinks if it is a printer friendly or
    					// excel export page
    					// length will be >0 when format is HTML, ExcelHTML, CSV
    					// or TXT
    					else if (null != reportFormat
    							&& !reportFormat.equals("null")
    							&& (reportFormat.length() > 0)) 
    					{
    						output.append(XSSUtil.encodeForHTML(context,locName));
    					} 
    					else {
    						output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/");
    						output.append(XSSUtil.encodeForURL(context,linkFile));
    						output.append("?emxSuiteDirectory=");
    						output.append(XSSUtil.encodeForURL(context,suiteDir));
    						output.append("&amp;suiteKey=");
    						output.append(XSSUtil.encodeForURL(context,suiteKey));
    						output.append("&amp;objectId=");
    						output.append(XSSUtil.encodeForURL(context,locId));
    						output.append("', '', '', 'false', 'popup', '')\">");
    						output.append(XSSUtil.encodeForHTML(context,locName));
    						output.append("</a> <br />");
    						output.append(XSSUtil.encodeForHTML(context,labelLocEquiv));
    						output.append(" <br />&#160;<br />");
    					}
    				}

    				if (!"".equals(output.toString())) {
    					result.add(output.toString());
    				}
    			}// end while
    			// Added for Bug 313092
    			/*DomainObject.MULTI_VALUE_LIST.remove(manEquAloResNameSel);
    			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResIdSel);
    			DomainObject.MULTI_VALUE_LIST.remove(aloResNameSel);
    			DomainObject.MULTI_VALUE_LIST.remove(aloResIdSel);*/
    		}// end if

    	} catch (Exception Ex) {
    		throw Ex;
    	}

    	return result;

    }
    /**
     * @param context
     * @param args
     * @return StringLsit of MEP Location Status
     * @throws Exception
     */
    public StringList getMEPLocationStatusHTMLOutput(Context context,
            String[] args) throws Exception {
        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            Map paramList = (HashMap) paramMap.get("paramList");
            MapList objectList = (MapList) paramMap.get("objectList");

            boolean isexport = false;
            String export = (String) paramList.get("exportFormat");
            if (export != null) {
                isexport = true;
            }

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);
                    oidList[i] = (String) dataMap.get("id");
                }

                StringList selects = new StringList();

                // Location Status select via the relationship route
                // Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResStaSel = "to["
                        + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].attribute[" + ATTRIBUTE_LOCATION_STATUS
                        + "].value";

                // Location Status select via the relationship route
                // Part --> Allocation Responsibility
                String aloResStaSel = "to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].attribute[" + ATTRIBUTE_LOCATION_STATUS
                        + "].value";

                // Select for Location Status
                DomainObject.MULTI_VALUE_LIST.add(manEquAloResStaSel);
                selects.add(manEquAloResStaSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResStaSel);
                selects.add(aloResStaSel);

                // Get Location Status Info
                MapList statusMaplist = getInfo(context, oidList, selects);

                StringList manEquAloResStaList = null;
                StringList aloResStaList = null;

                Iterator statusMapListItr = statusMaplist.iterator();
                while (statusMapListItr.hasNext()) {
                    Map statusMap = (Map) statusMapListItr.next();
                    // Get Location Status
                    manEquAloResStaList = (StringList) statusMap
                            .get(manEquAloResStaSel);
                    aloResStaList = (StringList) statusMap.get(aloResStaSel);

                    if (manEquAloResStaList == null) {
                        manEquAloResStaList = new StringList();
                    }

                    if (aloResStaList == null) {
                        aloResStaList = new StringList();
                    }

                    // Combine the above two status lists into one status list
                    if (aloResStaList != null && aloResStaList.size() > 0) {
                        for (int i = 0; i < aloResStaList.size(); i++) {
                            manEquAloResStaList.add((String) aloResStaList
                                    .get(i));
                        }
                    }

                    StringBuffer output = new StringBuffer(" ");
                    Iterator statusListItr = manEquAloResStaList.iterator();
                    while (statusListItr.hasNext()) {
                        String allocStatus = (String) statusListItr.next();
                        allocStatus = allocStatus.replace(' ', '_');
                        allocStatus = "emxFramework.Range.Location_Status."
                                + allocStatus;
                        allocStatus = EnoviaResourceBundle.getProperty(context ,"emxFrameworkStringResource",
                                context.getLocale(),allocStatus);

                        output.append(allocStatus);
                        if (isexport) {
                            output.append(" \n");
                        } else {
                            output.append(" <br>&nbsp;<br>");
                        }

                    }

                    if (!"".equals(output.toString())) {
                        result.add(output.toString());
                    }
                }// end while
                // Added for Bug 313092
                DomainObject.MULTI_VALUE_LIST.remove(manEquAloResStaSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResStaSel);

            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }

        return result;
    }// end of method getMEPLocationStatusHTMLOutput()

    /**
     * @param context
     * @param args
     * @return StringLsit of MEP Location Status
     * @throws Exception
     */
    public StringList getMEPLocationStatusHTMLOutputSB(Context context, String[] args) 
    throws Exception {
    	StringList result = new StringList();
    	try {
    		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    		Map paramList = (HashMap) paramMap.get("paramList");
    		MapList objectList = (MapList) paramMap.get("objectList");

    		boolean isexport = false;
    		String export = (String) paramList.get("exportFormat");
    		if (export != null) {
    			isexport = true;
    		}

    		if (objectList != null && objectList.size() > 0) {
    			// construct array of ids
    			/*int objectListSize = objectList.size();
    			String[] oidList = new String[objectListSize];
    			for (int i = 0; i < objectListSize; i++) {
    				Map dataMap = (Map) objectList.get(i);
    				oidList[i] = (String) dataMap.get("id");
    			}

    			StringList selects = new StringList();*/

    			// Location Status select via the relationship route
    			// Part --> Manufacture Equivalent --> Allocation Responsibility
    			String manEquAloResStaSel = "to["
    				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT 
    				+ "].from.to["
    				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY
    				+ "].attribute[" 
    				+ ATTRIBUTE_LOCATION_STATUS
    				+ "].value";

    			// Location Status select via the relationship route
    			// Part --> Allocation Responsibility
    			String aloResStaSel = "to["
    				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY
    				+ "].attribute[" 
    				+ ATTRIBUTE_LOCATION_STATUS
    				+ "].value";

    			// Select for Location Status
    			/*DomainObject.MULTI_VALUE_LIST.add(manEquAloResStaSel);
    			selects.add(manEquAloResStaSel);

    			DomainObject.MULTI_VALUE_LIST.add(aloResStaSel);
    			selects.add(aloResStaSel);

    			// Get Location Status Info
    			MapList statusMaplist = getInfo(context, oidList, selects);*/

    			StringList manEquAloResStaList = null;
    			StringList aloResStaList = null;

    			//Iterator statusMapListItr = statusMaplist.iterator();
    			Iterator statusMapListItr = objectList.iterator();
    			while (statusMapListItr.hasNext()) {
    				Map statusMap = (Map) statusMapListItr.next();
    				// Get Location Status
    				manEquAloResStaList = (StringList) statusMap.get(manEquAloResStaSel);
    				aloResStaList = (StringList) statusMap.get(aloResStaSel);

    				if (manEquAloResStaList == null) {
    					manEquAloResStaList = new StringList();
    				}

    				if (aloResStaList == null) {
    					aloResStaList = new StringList();
    				}

    				// Combine the above two status lists into one status list
    				if (aloResStaList != null && aloResStaList.size() > 0) {
    					for (int i = 0; i < aloResStaList.size(); i++) {
    						manEquAloResStaList.add((String) aloResStaList.get(i));
    					}
    				}

    				StringBuffer output = new StringBuffer(32);
					output.append(' ');
    				Iterator statusListItr = manEquAloResStaList.iterator();
    				while (statusListItr.hasNext()) {
    					String allocStatus = (String) statusListItr.next();
    					allocStatus = allocStatus.replace(' ', '_');
    					allocStatus = "emxFramework.Range.Location_Status." + allocStatus;
    					allocStatus = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource" ,context.getLocale(),allocStatus);
    					output.append(XSSUtil.encodeForXML(context, allocStatus));
    					if (isexport) {
    						output.append(" \n");
    					} else {
    						output.append(" <br />&#160;<br />");
    					}
    				}

    				if (!"".equals(output.toString())) {
    					result.add(output.toString());
    				}
    			}// end while
    			// Added for Bug 313092
    			/*DomainObject.MULTI_VALUE_LIST.remove(manEquAloResStaSel);
    			DomainObject.MULTI_VALUE_LIST.remove(aloResStaSel);*/

    		}// end if

    	} catch (Exception Ex) {
    		throw Ex;
    	}

    	return result;
    }
    /**
     * Gets the Location Preference attribute value on Allocation Responsibility
     * relationship between Location(s) and MEP.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectList - a
     *            MapList of object information. paramList - a Map of parameter
     *            values, SuiteDirectory, suiteKey.
     * @return a StringList HTML output of Status attribute value.
     * @throws Exception
     *             if the operation fails.
     */

    public StringList getMEPLocationPreferenceHTMLOutput(Context context,
            String[] args) throws Exception {

        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            Map paramList = (HashMap) paramMap.get("paramList");
            MapList objectList = (MapList) paramMap.get("objectList");

            boolean isexport = false;
            String export = (String) paramList.get("exportFormat");
            if (export != null) {
                isexport = true;
            }

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);
                    oidList[i] = (String) dataMap.get("id");
                }

                StringList selects = new StringList();

                // Location Preference select via the relationship route
                // Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResPreSel = "to["
                        + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].attribute[" + ATTRIBUTE_LOCATION_PREFERENCE
                        + "].value";

                // Location Preference select via the relationship route
                // Part --> Allocation Responsibility
                String aloResPreSel = "to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].attribute[" + ATTRIBUTE_LOCATION_PREFERENCE
                        + "].value";

                // Select for Location Status
                DomainObject.MULTI_VALUE_LIST.add(manEquAloResPreSel);
                selects.add(manEquAloResPreSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResPreSel);
                selects.add(aloResPreSel);

                // Get Location Preference information
                MapList prefMaplist = getInfo(context, oidList, selects);

                StringList manEquAloResPrefList = null;
                StringList aloResPrefList = null;

                Iterator prefMapListItr = prefMaplist.iterator();
                while (prefMapListItr.hasNext()) {
                    Map prefMap = (Map) prefMapListItr.next();

                    // Get Location Preference
                    manEquAloResPrefList = (StringList) prefMap
                            .get(manEquAloResPreSel);
                    aloResPrefList = (StringList) prefMap.get(aloResPreSel);

                    if (manEquAloResPrefList == null) {
                        manEquAloResPrefList = new StringList();
                    }

                    if (aloResPrefList == null) {
                        aloResPrefList = new StringList();
                    }

                    // Combine the above two status lists into one status list
                    if (aloResPrefList != null && aloResPrefList.size() > 0) {
                        for (int i = 0; i < aloResPrefList.size(); i++) {
                            manEquAloResPrefList.add((String) aloResPrefList
                                    .get(i));
                        }
                    }

                    StringBuffer output = new StringBuffer(" ");
                    Iterator prefListItr = manEquAloResPrefList.iterator();
                    while (prefListItr.hasNext()) {
                        String allocPref = (String) prefListItr.next();
                        allocPref = allocPref.replace(' ', '_');
                        allocPref = "emxFramework.Range.Location_Preference."
                                + allocPref;
                        allocPref = EnoviaResourceBundle.getProperty(context ,"emxFrameworkStringResource",
                                context.getLocale(),allocPref);

                        output.append(allocPref);
                        if (isexport) {
                            output.append(" \n");
                        } else {
                            output.append(" <br>&nbsp;<br>");
                        }
                    }

                    if (!"".equals(output.toString())) {
                        result.add(output.toString());
                    }
                }// end while
                // Added for Bug 313092
                DomainObject.MULTI_VALUE_LIST.remove(manEquAloResPreSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResPreSel);

            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }

        return result;
    }// end of method getMEPLocationPreferenceHTMLOutput()
	
	/**
     * Gets the Location Preference attribute value on Allocation Responsibility
     * relationship between Location(s) and MEP for Structure Browsers.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectList - a
     *            MapList of object information. paramList - a Map of parameter
     *            values, SuiteDirectory, suiteKey.
     * @return a StringList HTML output of Status attribute value.
     * @throws Exception
     *             if the operation fails.
     */

    public StringList getMEPLocationPreferenceHTMLOutputSB(Context context, String[] args) 
    throws Exception 
    {
        StringList result = new StringList();
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (HashMap) paramMap.get("paramList");
            MapList objectList = (MapList) paramMap.get("objectList");

            boolean isexport = false;
            String export = (String) paramList.get("exportFormat");
            if (export != null) {
                isexport = true;
            }

            if (objectList != null && objectList.size() > 0) {
                // construct array of ids
                /*int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for (int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map) objectList.get(i);
                    oidList[i] = (String) dataMap.get("id");
                }

                StringList selects = new StringList();*/

                // Location Preference select via the relationship route
                // Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResPreSel = "to["
                        + RELATIONSHIP_MANUFACTURER_EQUIVALENT 
                        + "].from.to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].attribute[" 
                        + ATTRIBUTE_LOCATION_PREFERENCE
                        + "].value";

                // Location Preference select via the relationship route
                // Part --> Allocation Responsibility
                String aloResPreSel = "to["
                        + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                        + "].attribute[" 
                        + ATTRIBUTE_LOCATION_PREFERENCE
                        + "].value";

                // Select for Location Status
                /*DomainObject.MULTI_VALUE_LIST.add(manEquAloResPreSel);
                selects.add(manEquAloResPreSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResPreSel);
                selects.add(aloResPreSel);

                // Get Location Preference information
                MapList prefMaplist = getInfo(context, oidList, selects);*/

                StringList manEquAloResPrefList = null;
                StringList aloResPrefList = null;

               // Iterator prefMapListItr = prefMaplist.iterator();
                Iterator prefMapListItr = objectList.iterator();
                while (prefMapListItr.hasNext()) {
                    Map prefMap = (Map) prefMapListItr.next();

                    // Get Location Preference
                    manEquAloResPrefList = (StringList) prefMap.get(manEquAloResPreSel);
                    aloResPrefList = (StringList) prefMap.get(aloResPreSel);

                    if (manEquAloResPrefList == null) {
                        manEquAloResPrefList = new StringList();
                    }

                    if (aloResPrefList == null) {
                        aloResPrefList = new StringList();
                    }

                    // Combine the above two status lists into one status list
                    if (aloResPrefList != null && aloResPrefList.size() > 0) {
                        for (int i = 0; i < aloResPrefList.size(); i++) {
                            manEquAloResPrefList.add((String) aloResPrefList.get(i));
                        }
                    }

                    StringBuffer output = new StringBuffer(32);
					output.append(' ');
                    Iterator prefListItr = manEquAloResPrefList.iterator();
                    while (prefListItr.hasNext()) {
                        String allocPref = (String) prefListItr.next();
                        allocPref = allocPref.replace(' ', '_');
                        allocPref = "emxFramework.Range.Location_Preference." + allocPref;
                        allocPref = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource" ,context.getLocale(),allocPref);
                        output.append(XSSUtil.encodeForXML(context, allocPref));
                        if (isexport) {
                            output.append(" \n");
                        } else {
                            output.append(" <br />&#160;<br />");
                        }
                    }

                    if (!"".equals(output.toString())) {
                        result.add(output.toString());
                    }
                }// end while
                // Added for Bug 313092
                /*DomainObject.MULTI_VALUE_LIST.remove(manEquAloResPreSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResPreSel);*/

            }// end if

        } catch (Exception Ex) {
            throw Ex;
        }

        return result;
    }

    /**
     * Returns true if the MCC is installed otherwise false.
     *
     * @mx.whereUsed This method will be called from displayPartRevision
     * @mx.summary This method check whether MCC is installed or not.Given below
     *             is a code:
     *             FrameworkUtil.isSuiteRegistered(context,"appVersionMaterialsComplianceCentral",false,null,null)
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     * @return boolean true or false based condition.
     * @throws Exception
     *             if the operation fails.
     */
    public boolean isMCCInstalled(Context context, String[] args)
            throws Exception {

        boolean mccInstall = FrameworkUtil.isSuiteRegistered(context,
                "appVersionMaterialsComplianceCentral", false, null, null);

        return mccInstall;
    }

    /**
     * Gets the Enterprise Parts attached to a Manufacturer Equivalent Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap with the following entries: objectId -
     *            the object id of the Manufacturer Equivalent Part.
     * @return a MapList of Enterprise Part object ids and relationship ids.
     * @throws Exception
     *             if the operation fails.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEnterpriseEquivalents(Context context, String[] args)
            throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);

        String objectId = (String) paramMap.get("objectId");
        MapList equivList = new MapList();

        MapList listMEPs = null;
        MapList corpMEPs = null;

        StringBuffer sbRelPattern = new StringBuffer(RELATIONSHIP_MANUFACTURER_EQUIVALENT);
        sbRelPattern.append(',');
        sbRelPattern.append(RELATIONSHIP_LOCATION_EQUIVALENT);

        // MCC Bug 330835 Fix to include MCC EP in the EP type pattern
        String sTypeCompliancePart = PropertyUtil.getSchemaProperty(context,
                "type_ComplianceEnterprisePart");

        StringBuffer sbTypePattern = new StringBuffer(TYPE_LOCATION_EQUIVALENT_OBJECT);
        sbTypePattern.append(',');
        sbTypePattern.append(TYPE_PART);
        sbTypePattern.append(',');
        sbTypePattern.append(sTypeCompliancePart);

        try {
            Part partObj = new Part(objectId);
            StringList selectStmts = new StringList(1);
            selectStmts.addElement(SELECT_ID);

            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

            // fetching any connected EPs via Location Equivalent object
            // (Location Context MEP)
            listMEPs = partObj.getRelatedObjects(context, sbRelPattern
                    .toString(), // relationship pattern
                    sbTypePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    false, // from direction
                    (short) 2, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            // fetching EPs connected directly using Manf Equiv Relation
            // (Corporate Context MEP)
            // MCC Bug 330835 Fix to include MCC EP in the EP type pattern
            StringBuffer scorpTypePattern = new StringBuffer(TYPE_PART);
            scorpTypePattern.append(',');
            scorpTypePattern.append(sTypeCompliancePart);

            corpMEPs = partObj.getRelatedObjects(context,
                    RELATIONSHIP_MANUFACTURER_EQUIVALENT, // relationship
                    // pattern
                    scorpTypePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    false, // from direction
                    (short) 1, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            for (int i = 0; i < listMEPs.size(); i++) {
                Map tempMap2 = (Map) listMEPs.get(i);
                // EP id exists at level 2 if Location Context MEP
                if ("2".equals((String) tempMap2.get("level"))) {
                    equivList.add(tempMap2);
                }
            }// end of for listMEPs

            for (int j = 0; j < corpMEPs.size(); j++) {
                Map tempMap2 = (Map) corpMEPs.get(j);
                equivList.add(tempMap2);
            }// end of For corpMEPs
        } catch (FrameworkException Ex) {
            throw Ex;
        }
        return equivList;
    }

    /**
     * Show the Manufacturer Location field.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a html String to show the Manufacturer location field.
     * @throws Exception
     *             if the operation fails.
     */
    public String showManufacturerLocationField(Context context, String[] args)
            throws Exception {
        StringBuffer outPut = new StringBuffer(1024);

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) paramMap.get("requestMap");

        String isAddExisting = (String) requestMap.get("mepAddExisting");

        if("true".equals(isAddExisting)){
        outPut
         .append("<script language=\"javascript\" >function loadManu(){var manId=document.forms[0].Manufacturer.value;showModalDialog('../common/emxSearch.jsp?defaultSearch=MEPLocationSearchCommand&isManufacturingLocation=Yes&defaultStates=state_Active&companyId='+manId, 700, 500);}</script>");
        }
        else{
        outPut
        .append("<script language=\"javascript\" >function loadManu(){var manId=document.forms[0].Manufacturer.value;showModalDialog('../common/emxSearch.jsp?defaultSearch=MEPLocationSearchCommand&amp;isManufacturingLocation=Yes&amp;defaultStates=state_Active&amp;companyId='+manId, 700, 500);}</script>");

        }

        outPut
                .append("<input type=\"text\" name=\"ManufacturerLocationDisplay");
        outPut.append("\" size=\"20\" value=\"\"");
        outPut.append(" readonly=\"readonly\"/>");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnManufacturerLocationChooser\" size=\"200\" ");
        outPut
                .append("value=\"...\" alt=\"\"  onClick=\"javascript:loadManu();\"");
        outPut.append("/>");
        outPut
                .append("<input type=\"hidden\" name=\"ManufacturerLocation\" value=\"\"></input>");
        outPut
                .append("<input type=\"hidden\" name=\"ManufacturerLocationOID\" value=\"\"></input>");
        return outPut.toString();
    }

    /**
     * Show the Usage Location field.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a html String to show the Usage location field.
     * @throws Exception
     *             if the operation fails.
     */
    public String showUsageLocationField(Context context, String[] args)
            throws Exception {
        StringBuffer outPut = new StringBuffer(1024);
        StringList selectList = new StringList(2);
        selectList.addElement(DomainObject.SELECT_NAME);
        selectList.addElement(DomainObject.SELECT_ID);
        String strReset = EnoviaResourceBundle.getProperty(context, 
                "emxManufacturerEquivalentPartStringResource",
                context.getLocale(),"emxManufacturerEquivalentPart.Common.Reset");

		String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_Location:CURRENT=policy_Organization.state_Active"
				+ "&amp;HelpMarker=emxhelpfullsearch&amp;table=OrganizationList&amp;selection=single"
				+ "&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formname=type_CreateMEP"
				+ "&amp;fieldNameActual=UsageLocation&amp;fieldNameDisplay=UsageLocationDisplay"
				+ "&amp;includeOIDprogram=jpo.manufacturerequivalentpart.Part:getManufacturingLocation&amp;submitURL=../manufacturerequivalentpart/emxMEPChooserProcess.jsp?searchMode=fullTextSearch&amp;mode=namechooser";

        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person
                .getPerson(context);
        Company contextComp = contextPerson.getCompany(context);
        Map compMap = contextComp.getInfo(context, selectList);

        String defaultManufacturer = (String) compMap.get(DomainObject.SELECT_NAME);
        //IR 034531
        if(defaultManufacturer.indexOf('&') != -1){
        	defaultManufacturer = FrameworkUtil.findAndReplace(defaultManufacturer,"&","&amp;");
        }
        String defaultManufacturerId = (String) compMap.get(DomainObject.SELECT_ID);

        outPut.append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>");
        outPut.append("<input type=\"text\" name=\"UsageLocationDisplay\"");
        outPut.append(" size=\"20\" value=\"" + XSSUtil.encodeForHTMLAttribute(context, defaultManufacturer) + "\"");
        outPut.append(" readonly=\"readonly\">");
        outPut.append("</input>");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
        outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        outPut.append(XSSUtil.encodeForHTML(context, strURL));
        outPut.append("', 700, 500)\">");
        outPut.append("</input>");
        outPut.append("<a href=\"JavaScript:ResetField('emxCreateForm','UsageLocation','UsageLocationDisplay','"
                        + XSSUtil.encodeForJavaScript(context, defaultManufacturer)
                        + "','"
                        + XSSUtil.encodeForJavaScript(context,defaultManufacturerId)
                        + "')\">");
        outPut.append(XSSUtil.encodeForXML(context,strReset));
		outPut.append("</a>");
        outPut.append("<input type=\"hidden\" name=\"UsageLocation\" value=\""
                + XSSUtil.encodeForHTMLAttribute(context,defaultManufacturerId) + "\"></input>");
        return outPut.toString();
    }

    /**
     * Gets the Organizations list.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a maplist of organizations/locations.
     * @throws FrameworkException
     *             if the operation fails.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOraganizationSearchResult(Context context, String[] args)
            throws FrameworkException {
        MapList organizationList = new MapList();
        try {
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person
                    .getPerson(context);
            Company company = person.getCompany(context);
            //Added for Bug # : 357589
            String RELATIONSHIP_ORGANIZATION_PLANT    = PropertyUtil.getSchemaProperty(context,"relationship_OrganizationPlant");
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String selectType = (String) paramMap.get("type");
            String txtName = (String) paramMap.get("Name");
            // String sWhereExp = "";
            String strRelLocation = "*";
            String strTypeLocation = "*";
            boolean start = false;
            SelectList busSelects = new SelectList(1);
            busSelects.add(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_TYPE);
            busSelects.add(DomainConstants.SELECT_NAME);
            busSelects.add(DomainConstants.SELECT_DESCRIPTION);
            busSelects.add("current.access[fromconnect]");

            SelectList relSelects = new SelectList(1);

            StringBuffer sWhereExp = new StringBuffer();
            if (!"*".equals(txtName)) {
                sWhereExp.append('(');
                sWhereExp.append("name ~~ \"");
                sWhereExp.append(txtName);
                sWhereExp.append('\"');
                sWhereExp.append(')');
                start = true;
            }

            // set the type & relationship names as per individual search
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            if (DomainConstants.TYPE_COMPANY.equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_SUBSIDIARY;
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            } else if (DomainConstants.TYPE_ORGANIZATION.equals(selectType)) {
                strTypeLocation = DomainConstants.TYPE_ORGANIZATION;
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            } else if (DomainConstants.TYPE_BUSINESS_UNIT.equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_DIVISION;
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            } else if (DomainConstants.TYPE_DEPARTMENT.equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT;
           //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            } else if (DomainConstants.TYPE_LOCATION.equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION;
           //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            } else if (PropertyUtil.getSchemaProperty(context, "type_Plant").equals(selectType)) { //Added for bug :357589
                strRelLocation = RELATIONSHIP_ORGANIZATION_PLANT;
            }

            /**
             * ************************Vault Code
             * Start****************************
             */
            // Get the user's vault option & call corresponding methods to get
            // the vault's.
            String txtVault = "";
            String strVaults = "";
            StringList strListVaults = new StringList();

            String txtVaultOption = (String) paramMap.get("vaultOption");
            if (txtVaultOption == null || "".equals(txtVaultOption)) {
                txtVaultOption = (String) paramMap.get("vaultsDisplay");
            }

            if ("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption)) {
                // get ALL vaults
                Iterator mapItr = VaultUtil.getVaults(context).iterator();
                if (mapItr.hasNext()) {
                    txtVault = (String) ((Map) mapItr.next()).get("name");
                    while (mapItr.hasNext()) {
                        Map map = (Map) mapItr.next();
                        txtVault += "," + (String) map.get("name");
                    }
                }
                if (start) {
                    sWhereExp.append(" && ");
                }

                sWhereExp.append('(');
                sWhereExp.append("Vault");
                sWhereExp.append(" matchlist ");
                sWhereExp.append('\'');
                sWhereExp.append(txtVault);
                sWhereExp.append("\' \',\')");

            } else if ("LOCAL_VAULTS".equals(txtVaultOption)) {
                // get All Local vaults
                strListVaults = OrganizationUtil.getLocalVaultsList(context,
                        company.getObjectId());

                StringItr strItr = new StringItr(strListVaults);
                if (strItr.next()) {
                    strVaults = strItr.obj().trim();
                }
                while (strItr.next()) {
                    strVaults += "," + strItr.obj().trim();
                }
                txtVault = strVaults;
                if (start) {
                    sWhereExp.append(" && ");
                }
                sWhereExp.append('(');
                sWhereExp.append("Vault");
                sWhereExp.append(" matchlist ");
                sWhereExp.append('\'');
                sWhereExp.append(txtVault);
                sWhereExp.append("\' \',\')");

            } else if ("DEFAULT_VAULT".equals(txtVaultOption)) {
                txtVault = context.getVault().getName();
                if (start) {
                    sWhereExp.append(" && ");
                }
                sWhereExp.append('(');
                sWhereExp.append("Vault == \"");
                sWhereExp.append(txtVault);
                sWhereExp.append('\"');
                sWhereExp.append(')');
            } else {
                txtVault = txtVaultOption;
                if (start) {
                    sWhereExp.append(" && ");
                }
                sWhereExp.append('(');
                sWhereExp.append("Vault");
                sWhereExp.append(" matchlist ");
                sWhereExp.append('\'');
                sWhereExp.append(txtVault);
                sWhereExp.append("\' \',\')");

            }
            /**
             * *****************************Vault Code
             * End**************************************
             */

            organizationList = company.getRelatedObjects(context,
                    strRelLocation, // relationship pattern
                    strTypeLocation, // object pattern
                    busSelects, // object selects
                    relSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    sWhereExp.toString(), // object where clause
                    ""); // relationship where clause
            return organizationList;
        } catch (Exception Ex) {
            throw new FrameworkException(Ex);
        }
    }

    /**
     * Show the Manufacturer field.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a html String to show the Manufacturer field.
     * @throws Exception
     *             if the operation fails.
     */
    public String showManufacturerField(Context context, String[] args)
            throws Exception {
        StringBuffer returnString = new StringBuffer(1024);
       try{
           HashMap paramMap = (HashMap) JPO.unpackArgs(args);

           HashMap requestMap = (HashMap) paramMap.get("requestMap");

           String isAddExisting = (String) requestMap.get("mepAddExisting");

        StringList selectList = new StringList(2);
        selectList.addElement(DomainObject.SELECT_NAME);
    		selectList.addElement(DomainObject.SELECT_ID);
    		
    		com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
    		Company contextComp = contextPerson.getCompany(context);
    		Map compMap         = contextComp.getInfo(context, selectList);

    		String defaultManufacturer = (String) compMap.get(DomainObject.SELECT_NAME);
    		String defaultManufacturerId = (String) compMap.get(DomainObject.SELECT_ID);

    		String strReset = EnoviaResourceBundle.getProperty(context ,
                    "emxManufacturerEquivalentPartStringResource",
                    context.getLocale(),"emxManufacturerEquivalentPart.Common.Reset");
        //Start IR:034531
        if(defaultManufacturer.indexOf('&') != -1){
        	defaultManufacturer = FrameworkUtil.findAndReplace(defaultManufacturer,"&","&amp;");
        }
		//Added IR-010681
   
    		String strURL = "../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active"+
    		"&amp;HelpMarker=emxhelpselectorganization&amp;showInitialResults=false&amp;table=OrganizationList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true"+
    		"&amp;clearCustomRev=true&amp;clearManuLoc=true&amp;formname=type_CreateMEP&amp;submitURL=../manufacturerequivalentpart/emxMEPChooserProcess.jsp?searchMode=fullTextSearch&amp;fieldNameActual=Manufacturer&amp;fieldNameDisplay=ManufacturerDisplay";
     		
    		//Ends IR-010681
        returnString
                .append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>");
        returnString
                .append("<input type=\"text\" name=\"ManufacturerDisplay\"");
        returnString.append(" size=\"20\" value=\"" + defaultManufacturer
                + "\"");
        returnString.append(" readonly=\"readonly\">");
        returnString.append("</input>");
        returnString.append("<input class=\"button\" type=\"button\"");
        returnString.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
        returnString
                .append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        returnString.append(strURL);
        returnString.append("', 700, 500)\">");
        returnString.append("</input>");
      //Modified for IR-048513V6R2012x, IR-118107V6R2012x start
        returnString
 	           .append("<a href=\"JavaScript:ResetField('forms[0]','"+PropertyUtil.getSchemaProperty(context, "attribute_Manufacturer")
 	           		+"','ManufacturerDisplay','"
                        + defaultManufacturer
                        + "','"
                        + defaultManufacturerId
                        + "');resetManuLoc();");
      //Modified for IR-048513V6R2012x, IR-118107V6R2012x end
        
        if("true".equals(isAddExisting)){

        returnString.append("\">");
        }
        else{

            returnString.append("loadRevision();\">");
        }

        returnString.append(strReset);
		returnString.append("</a>");
        returnString
                .append("<input type=\"hidden\" name=\"Manufacturer\" value=\""
                        + defaultManufacturerId + "\"></input>");

       }catch(Exception e){
           e.printStackTrace();
       }
        return returnString.toString();
    }

    /**
     * Show the Vault field.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a html String to show the Vault field.
     * @throws Exception
     *             if the operation fails.
     */
    public String showVaultField(Context context, String[] args)
            throws Exception {
        StringBuffer radioOption = null;
        try {
            radioOption = new StringBuffer(2048);

            String strLocale = context.getSession().getLanguage();

            String vaultDefaultSelection = PersonUtil
                    .getSearchDefaultSelection(context);

            String strAll = EnoviaResourceBundle.getProperty(context ,
                    "emxFrameworkStringResource",context.getLocale(),
                    "emxFramework.Preferences.AllVaults");
            String strDefault = EnoviaResourceBundle.getProperty(context ,
                    "emxFrameworkStringResource", context.getLocale(),
                    "emxFramework.Preferences.DefaultVault");
            String strSelected = EnoviaResourceBundle.getProperty(context ,
                    "emxFrameworkStringResource", context.getLocale(),
                    "emxFramework.Preferences.SelectedVaults");
            String strLocal = EnoviaResourceBundle.getProperty(context ,
                    "emxFrameworkStringResource", context.getLocale(),
                    "emxFramework.Preferences.LocalVaults");
            
            String checked = "";
            if (PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection)) {
                checked = "checked";
            }

            radioOption
                    .append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>");
            radioOption
                    .append("<script language=\"JavaScript\"> function clearSelectedVaults() { document.forms[0].vaultOption[3].value= \"\";document.forms[0].vaultsDisplay.value=\"\";}</script>");
            radioOption.append("&nbsp;<input type=\"radio\" value=\"");
            radioOption.append(PersonUtil.SEARCH_DEFAULT_VAULT);
            radioOption
                    .append("\" name=\"vaultOption\" onclick=\"JavaScript:clearSelectedVaults()\"");
            radioOption.append(checked);
            radioOption.append('>');
            radioOption.append(strDefault);
            radioOption.append("<br>");

            checked = "";

            if (PersonUtil.SEARCH_ALL_VAULTS.equals(vaultDefaultSelection)) {
                checked = "checked";
            }
            radioOption.append("&nbsp;<input type=\"radio\" value=\"");
            radioOption.append(PersonUtil.SEARCH_ALL_VAULTS);
            radioOption
                    .append("\" name=\"vaultOption\" onclick=\"JavaScript:clearSelectedVaults()\"");
            radioOption.append(checked);
            radioOption.append('>');
            radioOption.append(strAll);
            radioOption.append("<br>");

            checked = "";
            if (PersonUtil.SEARCH_LOCAL_VAULTS.equals(vaultDefaultSelection)) {
                checked = "checked";
            }
            radioOption.append("&nbsp;<input type=\"radio\" value=\"");
            radioOption.append(PersonUtil.SEARCH_LOCAL_VAULTS);
            radioOption
                    .append("\" name=\"vaultOption\" onclick=\"JavaScript:clearSelectedVaults()\"");
            radioOption.append(checked);
            radioOption.append('>');
            radioOption.append(strLocal);
            radioOption.append("<br>");

            checked = "";
            String selVault = "";
            String selDisplayVault = "";
            if (!PersonUtil.SEARCH_DEFAULT_VAULT.equals(vaultDefaultSelection)
                    && !PersonUtil.SEARCH_LOCAL_VAULTS
                            .equals(vaultDefaultSelection)
                    && !PersonUtil.SEARCH_ALL_VAULTS
                            .equals(vaultDefaultSelection)) {
                checked = "checked";
                selVault = vaultDefaultSelection;
                selDisplayVault = i18nNow.getI18NVaultNames(context,
                        vaultDefaultSelection, strLocale);
            }
            radioOption.append("&nbsp;<input type=\"radio\" value=\"");
            radioOption.append(selVault);
            radioOption.append("\" name=\"vaultOption\" ");
            radioOption.append(checked);
            radioOption.append('>');
            radioOption.append(strSelected);
            radioOption
                    .append("&nbsp;&nbsp;<input type=\"text\" READONLY name=\"vaultsDisplay\" value =\""
                            + selDisplayVault
                            + "\" id=\"\" size=\"20\"                onFocus=\"this.blur();\">");
            radioOption.append("<script language=\"JavaScript\">assignValidateMethod('vaultsDisplay', 'vaultSelection');</script>");

            radioOption.append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"document.forms[0].vaultOption[3].checked=true;javascript:getTopWindow().showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=false&isFromSearchForm=true')\">");
            radioOption.append("<input type=\"hidden\" name=\"vaults\" value=\"");
            radioOption.append(selVault);
            radioOption.append("\" size=15>");
            radioOption.append("<br>");

        } catch (Throwable excp) {

        }

        return radioOption.toString();

    }
    	
	 /**
     * Gets the Location/Organization list.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap which caontains-type and name ..
     * @return a StringList of location/Organizations object ids.
     * @throws FrameworkException if the operation fails.
	 */
	 
	 @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	 public StringList getManufacturingLocation(Context context, String[] args) throws FrameworkException {		 
		 String type = DomainConstants.TYPE_LOCATION + "," + DomainConstants.TYPE_ORGANIZATION;
		 
		 Person person = (Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		 Company company = person.getPerson(context).getCompany(context);
		 
		 StringList objectSelect = new StringList(DomainConstants.SELECT_ID);
	 
		 MapList list = company.getRelatedObjects(context, DomainConstants.QUERY_WILDCARD,
			 type, objectSelect, null, false, true, (short) 1, null, null, null, null, null);
		 
		 int size = list.size();
		 StringList sListOrgAndLoc = new StringList(size);
		 
		 for (int i = 0; i < size; i++) {
			 sListOrgAndLoc.add(((Map) list.get(i)).get(DomainConstants.SELECT_ID));
		 }
		 			 
		 return sListOrgAndLoc;
	 }

    /**
     * Gets the Location/Organization list page.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap which caontains-type and name to be
     *            searched for.
     * @return a maplist of location/Organizations
     * @throws FrameworkException
     *             if the operation fails.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getManufacturingLocationSearchResults(Context context, String[] args)
            throws FrameworkException {
        MapList organizationList = new MapList();
        try {

            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            String sType = (String) paramMap.get("type");
            String sName = (String) paramMap.get("Name");
            String sSelManufacturerId=(String)paramMap.get("companyId");

            String strRelLocation = "*";
            SelectList busSelects = new SelectList(1);
            busSelects.add(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_TYPE);
            busSelects.add(DomainConstants.SELECT_NAME);
            busSelects.add(DomainConstants.SELECT_DESCRIPTION);
            busSelects.add("current.access[fromconnect]");
            String attrPlantId   = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_PlantID);
            busSelects.add(DomainObject.getAttributeSelect(attrPlantId));
            StringBuffer sWhereExp = new StringBuffer(64);
            String txtVault   = "";
            String strVaults  = "";
            String relWhere = "";

            String isManufacturingLocation   = (String)paramMap.get("isManufacturingLocation");
           //Modified for IR-048513V6R2012x, IR-118107V6R2012x
			if(DomainConstants.TYPE_COMPANY.equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_SUBSIDIARY;
            }
			//Modified for IR-048513V6R2012x, IR-118107V6R2012x
			else if(DomainConstants.TYPE_BUSINESS_UNIT.equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_DIVISION;

            }
			//Modified for IR-048513V6R2012x, IR-118107V6R2012x
		    else if(DomainConstants.TYPE_DEPARTMENT.equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT;
            }
		  //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            else if(DomainConstants.TYPE_LOCATION.equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION;

                if(isManufacturingLocation != null && "Yes".equalsIgnoreCase(isManufacturingLocation)) {
                    if(sWhereExp.length()>0) {sWhereExp.append(" && ");}
                    String relOrganizationLocation = PropertyUtil.getSchemaProperty(
                            context, "relationship_OrganizationLocation");
                    String attrManufacturingSite = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_ManufacturingSite);

                    sWhereExp.append("relationship["+relOrganizationLocation+"].attribute["+attrManufacturingSite+"]==\"Yes\"");
                }
           }
            String txtVaultOption = (String) paramMap.get("vaultOption");

            if(sSelManufacturerId != null &&  !"null".equalsIgnoreCase(sSelManufacturerId) && !"".equals(sSelManufacturerId))
            {
              if ("DEFAULT_VAULT".equals(txtVaultOption))
                {
                    txtVault = context.getVault().getName();
                }

              else if("ALL_VAULTS".equals(txtVaultOption))
                 {
                   txtVault="Vault ~= const\"*\"";

                 }
                 else if("LOCAL_VAULTS".equals(txtVaultOption))
                 {

                    if(txtVault.indexOf(',')>0)
                    {
                         StringTokenizer sVaults = new StringTokenizer(txtVault,",");
                         if(sVaults.hasMoreTokens())
                         {

                             strVaults ="(Vault==\""+sVaults.nextToken()+"\"";
                         }
                         while(sVaults.hasMoreTokens())
                         {
                             strVaults += "|| Vault==\"" + sVaults.nextToken()+"\"";
                         }
                         txtVault=strVaults+")" ;
                    }
                    else
                    {
                       txtVault="Vault ==\""+txtVault+"\"" ;
                    }
                 }
                 else
                 {
                   txtVault="Vault ==\""+txtVault+"\"" ;
                 }
            if (!"*".equals(sName)) {
                     if(sWhereExp.length()>0) {sWhereExp.append(" && ");}
                     sWhereExp.append('(');
                     sWhereExp.append("name ~~ \"");
                     sWhereExp.append(sName);
                     sWhereExp.append('\"');
                     sWhereExp.append(')');

                 }

                 if(sWhereExp.length() > 0) {
					sWhereExp.append(" && ");
				}
                 sWhereExp.append(txtVault);


                sWhereExp.append(" && current == \"" + PropertyUtil.getSchemaProperty(context,"policy",DomainObject.POLICY_LOCATION,"state_Active") +"\"");
               DomainObject domObj = new DomainObject(sSelManufacturerId.trim());



                 organizationList = domObj.getRelatedObjects(context, strRelLocation,
                         sType, busSelects, null, false, true, (short) 1, sWhereExp.toString(),
                         relWhere);


                  }else{

            com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject
                    .newInstance(context, DomainConstants.TYPE_PERSON);
            Company company = person.getPerson(context).getCompany(context);


                      organizationList = company.getRelatedObjects(context, strRelLocation,
                              sType, busSelects, null, false, true, (short) 1, sWhereExp.toString(),
                    relWhere);
                  }
        } catch (Exception Ex) {
            throw new FrameworkException(Ex);
        }
        return organizationList;
    }


    /**
     * Connects the created Manufacturer Equivalent Part for the given
     * Enterprise Part and location objects.
     *
     * @param context
     *            The Matrix Context.
     * @param args
     *            holds a packed HashMap which contains usagelcoation,mepid ep
     *            id,manufacturer,mfg location,loc status
     * @throws FrameworkException
     *             If the operation fails.
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createMfg(Context context, String[] args) throws Exception {

        final String LOCATION_EQUIVALENT_OBJECT = "type_LocationEquivalentObject";
        final String LOCATION_EQUIVALENT_POLICY = "policy_LocationEquivalent";
        String[] smepObjId={};
        try {
            DomainObject doEntPart = null;
            DomainObject doLocation = null;
            DomainObject doManufacturer = null;
            DomainObject doLocEquiv = null;
            DomainObject mepObject = null;
            String sLocEquivObjId = null;
            ContextUtil.startTransaction(context, true);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String sLocId = (String) requestMap.get("UsageLocation");
            String sLocPreference = (String) requestMap.get("Preference");
            String sLocStatus = (String) requestMap.get("Status");
            String smepId = (String) paramMap.get("objectId");
            smepObjId =new String[]{smepId};

            String manufacturerId = (String) requestMap.get("Manufacturer");
            String sManufacturerLocationId = (String) requestMap
                    .get("ManufacturerLocation");
            //Code modified for bug id ;IR-023402V6R2011 change param parantOID -->objectId
            String sEpId = (String) requestMap.get("objectId");
            if (manufacturerId != null && !manufacturerId.equals("null")
                    && manufacturerId.length() > 0) {
                doManufacturer = new DomainObject(manufacturerId);
            } else {
                throw new MatrixException(
                        "emxManufacturerEquivalent.Part.InvalidManufacturer");
            }
            Company contextComp = com.matrixone.apps.common.Person.getPerson(
                    context).getCompany(context);
            StringList selectList = new StringList(2);
            selectList.addElement(DomainObject.SELECT_NAME);
            selectList.addElement(DomainObject.SELECT_ID);
            Map compMap = contextComp.getInfo(context, selectList);

            String sDefaultLocId = (String) compMap.get(DomainObject.SELECT_ID);
            DomainObject doDefaultLocation = new DomainObject(sDefaultLocId);
            if (sLocId != null && sDefaultLocId.equals(sLocId)) {
                sLocId = null;
            }
            if (sLocId != null && !"null".equals(sLocId) && sLocId.length() > 0) {
                doLocation = new DomainObject(sLocId);
            }
            if (smepId != null && !"null".equals(smepId) && smepId.length() > 0) {
                mepObject = DomainObject.newInstance(context, smepId);
            }
            if (sEpId != null && !sEpId.equals("null") && sEpId.length() > 0) {
                doEntPart = new DomainObject(sEpId);
            }
            
            if (doManufacturer != null) {
                // connect to company with "Manufacturing Responsibility"
                mepObject.addRelatedObject(context, new RelationshipType(
                        RELATIONSHIP_MANUFACTURING_RESPONSIBILITY), true,
                        manufacturerId);

            }
            
            String sCompEngrRole = PropertyUtil.getSchemaProperty(context,"role_ComponentEngineer");
           if (doEntPart != null) {
                // generating autonamed intermediate location equivalent object
                if (sLocId != null && !"null".equals(sLocId)
                        && sLocId.length() > 0) {
                    // starting of the code for the bug 318698
                    sLocEquivObjId = FrameworkUtil.autoName(context,
                            LOCATION_EQUIVALENT_OBJECT, "",
                            LOCATION_EQUIVALENT_POLICY, null, null, false,
                            false);
                    // end of the code for the bug 318698..
                }

                if (sLocEquivObjId != null) {
                    doLocEquiv = DomainObject.newInstance(context,
                            sLocEquivObjId);
                }

                // connect MEP to hostCompany directly using Allocation Resp
                // relation
                // (Corporate Context)
                if (doDefaultLocation != null && doLocEquiv == null) {
                    // connecting MEP and hostCompany with Allocation Resp
                    // relationship
                    RelationshipType relAllocResp = new RelationshipType(
                            RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                    DomainRelationship doRelationship = DomainRelationship
                            .connect(context, doDefaultLocation, relAllocResp,
                                    mepObject);

                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                }
                // connecting Location Context MEP relationships via
                // intermediate
                // location equivalent object
                if (doLocEquiv != null) {
                    // connecting MEP from Loc Equiv Obj for selected location
                    // with rel_ManfEquivalent
                    RelationshipType relManuEquiv = new RelationshipType(RELATIONSHIP_MANUFACTURER_EQUIVALENT);
                    DomainRelationship.connect(context, doLocEquiv, relManuEquiv, mepObject);

                    if (doLocation != null) {
                        // connecting selected Location from Loc Equiv Obj with
                        // Allocation Responsibility relationship
                        RelationshipType relAllocResp = new RelationshipType(
                                RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                        DomainRelationship doRelationship = DomainRelationship
                                .connect(context, doLocation, relAllocResp,
                                        doLocEquiv);

                        doRelationship.setAttributeValue(context,
                                ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                        doRelationship.setAttributeValue(context,
                                ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                    }
                }

                // If context is a component engineer, allow the connection
                // of the mep to the enterprise part by switching user to super
                // user. This will enable these connections regardless of the
                // access
                // o the component engineer in that state.
                if (context.isAssigned(sCompEngrRole)) {
                    if (sEpId != null && !"null".equalsIgnoreCase(sEpId)
                            && !"".equalsIgnoreCase(sEpId)) {


                        // if no location is chosen,MEP created shall be
                        // corporate context
                        // and this MEP is directly connected to EP using
                        // Manufacturer Equivalent
                        // relationship
                        if (doLocEquiv == null) {
                            // connecting EP and MEP directly with Manufacturer
                            // Equivalent relationship
                            ContextUtil.pushContext(context);
                            String conEpMEPCmd = "connect bus $1 relationship $2 from $3;";
                            MqlUtil.mqlCommand(context, "history off");
                            MqlUtil.mqlCommand(context, conEpMEPCmd, false,mepObject.getInfo(context, SELECT_ID),RELATIONSHIP_MANUFACTURER_EQUIVALENT,sEpId);
                            MqlUtil.mqlCommand(context, "history on");
                            ContextUtil.popContext(context);

                            String historyEpMEPCommand = "modify bus $1 add history $2 comment $3";
                            MqlUtil.mqlCommand(context,
                                    historyEpMEPCommand,sEpId,"connect","connect "+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"  to "+mepObject.getInfo(context, SELECT_NAME));
                        }

                        ContextUtil.pushContext(context);
                        if (doLocEquiv != null) {
                            String connectLocCmd = "connect bus $1 relationship $2 from $3;";
                            MqlUtil.mqlCommand(context, "history off");
                            MqlUtil.mqlCommand(context, connectLocCmd, false,doLocEquiv.getInfo(context, SELECT_ID),RELATIONSHIP_LOCATION_EQUIVALENT,sEpId);
                            MqlUtil.mqlCommand(context, "history on");

                            String historyCommand = "modify bus $1 add history $2 comment $3";
                            MqlUtil.mqlCommand(context, historyCommand,sEpId,"connect","connect "+RELATIONSHIP_LOCATION_EQUIVALENT+"  to "+doLocEquiv.getInfo(context, SELECT_NAME));
                        }
                        ContextUtil.popContext(context);
                    }
                }
                // context is not component engineer role
                else {
                    // connecting Location Context MEP relationships via
                    // intermediate
                    // location equivalent object
                    if (doLocEquiv != null) {
                        // connecting EP to Loc Equiv Obj for selected location
                        // with rel_LocationEquivalent
                        doEntPart.addRelatedObject(context,
                                new RelationshipType(
                                 //RELATIONSHIP_MANUFACTURER_EQUIVALENT),-IR-026840V6R2011-need to refer rel_LocationEquivalent when the location is chosen
                                 RELATIONSHIP_LOCATION_EQUIVALENT),
                                 false, doLocEquiv.getInfo(context, SELECT_ID));
                    }
                    // connect corporateMEP to EP
                    else {
                        doEntPart.addRelatedObject(context,
                                new RelationshipType(
                                        RELATIONSHIP_MANUFACTURER_EQUIVALENT),
                                false, smepId);
                    }
                }
                // EPart null
            
            } else {
                // connect MEP to selected Location directly using Allocation
                // Resp relation
                // (Corporate Context)
                if (doLocation != null) {
                    // connecting MEP and selected Location with Allocation
                    // Responsibility relationship
                    RelationshipType relAllocResp = new RelationshipType(
                            RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                    DomainRelationship doRelationship = DomainRelationship
                            .connect(context, doLocation, relAllocResp,
                                    mepObject);

                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                }
                else {
	                // connect MEP to hostCompany directly using Allocation Resp
	                // relation
	                // (Corporate Context)
	                if (doDefaultLocation != null) {
	                    // connecting MEP and hostCompany with Allocation Resp
	                    // relationship
	                    RelationshipType relAllocResp = new RelationshipType(
	                            RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
	                    DomainRelationship doRelationship = DomainRelationship
	                            .connect(context, doDefaultLocation, relAllocResp,
	                                    mepObject);
	
	                    doRelationship.setAttributeValue(context,
	                            ATTRIBUTE_LOCATION_STATUS, sLocStatus);
	                    doRelationship.setAttributeValue(context,
	                            ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
	                }
                }

            }// end of if-else (enterprise Part != null)

            if (sManufacturerLocationId != null
                    && !"null".equalsIgnoreCase(sManufacturerLocationId)
                    && !"".equals(sManufacturerLocationId)) {
                // Getting Manufacturing Location name
                String relManufacturingLocation = PropertyUtil
                        .getSchemaProperty(context,
                                "relationship_ManufacturingLocation");
                // connect location with MEP Part
                mepObject.addToObject(context, new RelationshipType(
                        relManufacturingLocation), sManufacturerLocationId);
            }
              //072884V6R2012 start
                 String sEndItem = mepObject.getAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_EndItem"));
                if("Yes".equals(sEndItem)) {
                    mepObject.setAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_EndItemOverrideEnabled"), "No");
                } else {
                    mepObject.setAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_EndItemOverrideEnabled"), "Yes");
                }
             //072884V6R2012 end
            ContextUtil.commitTransaction(context);

        } catch (Exception e) {
            e.printStackTrace();
             try{
            ContextUtil.abortTransaction(context);
                Part.deleteMEPs(context,smepObjId);

             }catch(Exception ex){
                 throw (new Exception("MEP not created.Please create again with valid data"));
             }


            throw (new FrameworkException(e));
        }
    }

    /**
     * Informs whether the current part in context is Manufacturer Equivalent
     * Part or not.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap with the following entries: objectId - a
     *            String containing the Part id.
     * @return boolean true if Part Policy is "Equivalent", else false
     * @throws Exception
     *             if operation fails
     */
    public Boolean isMepPart(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strPartId = (String) programMap.get("objectId");

            // return true if the part is a MEP part
            // setId(strPartId);
            DomainObject domObj = DomainObject.newInstance(context, strPartId);


            String strPartClassification = domObj.getInfo(context,
                    "policy.property[PolicyClassification].value");

            String type = domObj.getInfo(context, SELECT_TYPE);
            //to fix IR-096652V6R2012
            if (strPartClassification!=null && strPartClassification.equals("Equivalent") && !type.equalsIgnoreCase( TYPE_MPN ) ) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw (new FrameworkException(e));
        }
    }

    /**
     * Returns whether the Part is a Manufacturer Equivalent or Enterprise Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap of the following entries: paramMap - a HashMap
     *            containing String values for "objectId", "languageStr". This
     *            Map contains the arguments passed to the jsp which called this
     *            method.
     * @return Object - Part Policy Classification in a StringList.
     * @throws Exception
     *             if operation fails
     */
    public Object getMepPartOrigin(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strPartId = (String) paramMap.get("objectId");

        setId(strPartId);
        // Get the part policy classification
        String strPartClassification = getInfo(context,
                "policy.property[PolicyClassification].value");
        String strManufacturerEquivalent = EnoviaResourceBundle.getProperty(context ,
                "emxManufacturerEquivalentPartStringResource",
                context.getLocale(),"emxManufacturerEquivalentPart.Part.MfgEquivalent");
        String strEnterprise = EnoviaResourceBundle.getProperty(context ,
                "emxManufacturerEquivalentPartStringResource",
                context.getLocale(),"emxManufacturerEquivalentPart.Part.Enterprise");
        StringList PartOriginList = new StringList(1);

        if ("Equivalent".equals(strPartClassification)) {
            PartOriginList.addElement(strManufacturerEquivalent);
        } else {
            PartOriginList.addElement(strEnterprise);
        }

        return PartOriginList;
    }

    /**
     * Displays the text field for Revision in Edit and View Part web form
     * screen
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *
     * @return String
     * @throws Exception
     *             if the operation fails.
     *
     */

    public Object displayPartRevision(Context context, String[] args)
            throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        Map requestMap = (Map) programMap.get("requestMap");
        String strPartId = (String) requestMap.get("objectId");
        String strMode = (String) requestMap.get("mode");
        String reportFormat = (String) requestMap.get("reportFormat");

        StringList strList = new StringList(2);
        strList.add(SELECT_REVISION);
        strList.add("policy.property[PolicyClassification].value");

        DomainObject domObj = new DomainObject(strPartId);
        Map map = domObj.getInfo(context, strList);

        String strRevision = (String) map.get(SELECT_REVISION);

        boolean isMep = false;
        String strPolicyClassification = (String) map
                .get("policy.property[PolicyClassification].value");
        if ("Equivalent".equals(strPolicyClassification)) {
            isMep = true;
        }

        boolean isViewMode = false;
        if (strMode == null || "null".equals(strMode)
                || "view".equalsIgnoreCase(strMode) || "".equals(strMode)) {
            isViewMode = true;
        }

        StringBuffer strBuf = new StringBuffer();
            if (isViewMode || !isMep) {
            
            strBuf.append(XSSUtil.encodeForHTML(context, strRevision));
            if (reportFormat == null || reportFormat.length() == 0
                    || "null".equals(reportFormat)) {
                strBuf.append(
                        "<input type=\"hidden\" name=\"Revision\" value=\"")
                        .append(XSSUtil.encodeForHTMLAttribute(context, strRevision)).append("\">");
            }
        } else {

            String customRevision = FrameworkProperties
                    .getProperty(context, "emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            if (customRevision == null) {
                customRevision = "false";
            } else {
                customRevision = customRevision.trim();
            }
            if (reportFormat == null || reportFormat.length() == 0
                    || "null".equals(reportFormat)) {
                strBuf
                        .append(
                                "<input type=\"text\" name=\"Revision\" value=\"")
                        .append(XSSUtil.encodeForHTMLAttribute(context, strRevision)).append("\" ");
            }
            /* EC-MCC interoperability */
            /* Added one more condition to check MCC is installed or not. */
            if ("false".equals(customRevision) || isMCCInstalled(context, args)) {
                strBuf.append(" READONLY ");
            }
            strBuf.append('>');
        }

        return strBuf.toString();
    }

    /**
     * Updates the Revision for a Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     * @return int "0" success "1" failure
     * @throws Exception
     *             if operation fails
     */
    public int updatePartRevision(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strPartId = (String) paramMap.get("objectId");
        String[] revisionValues = (String[]) requestMap.get("Revision");
        String strRevValue = "";

        if (revisionValues != null && revisionValues.length > 0) {
            strRevValue = revisionValues[0];
        }
        DomainObject domObj = new DomainObject(strPartId);
        StringList strList = new StringList();
        strList.add(SELECT_TYPE);
        strList.add(SELECT_NAME);
        strList.add(SELECT_REVISION);
        strList.add("policy.property[PolicyClassification].value");

        Map map = domObj.getInfo(context, strList);

        boolean isMep = false;
        String strPolicyClassification = (String) map
                .get("policy.property[PolicyClassification].value");
        if ("Equivalent".equals(strPolicyClassification)) {
            isMep = true;
        }

        String strRev = (String) map.get(SELECT_REVISION);
        if (isMep && !"".equals(strRevValue) && !strRev.equals(strRevValue)) {
            String strType = (String) map.get(SELECT_TYPE);
            String strName = (String) map.get(SELECT_NAME);
            MapList mapList = DomainObject.findObjects(context, strType,
                    strName, strRevValue, QUERY_WILDCARD, QUERY_WILDCARD, "",
                    false, null);
            if (mapList == null || mapList.size() == 0) {
                Part pt = new Part();
                pt.setRevision(context, strPartId, strRevValue, strName);
            } else {
                String strMessage = EnoviaResourceBundle.getProperty(context ,
                        "emxManufacturerEquivalentPartStringResource",
                        context.getLocale(),"emxManufacturerEquivalentPart.MEP.Exists");
                MqlUtil.mqlCommand(context, "notice $1",strMessage);

                return 1;

            }
        }
        return 0;
    }

    /**
     * This method is used get the policy with same policy classification as the
     * current policy of a Part
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: paramMap - a
     *            HashMap containing the following keys, "objectId", "old RDO
     *            Ids", "New OID".
     * @return String which contains HTML code to display a dropdown with all
     *         the policies
     * @throws Exception
     *             if operation fails
     */

    public String getPolicyClassificationPolicies(Context context, String[] args)
            throws Exception {
        StringBuffer returnString = new StringBuffer();

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String languageStr = (String) requestMap.get("languageStr");
        String strMode = (String) requestMap.get("mode");
        String strPartId = (String) requestMap.get("objectId");
        DomainObject partObj = new DomainObject(strPartId);
        StringList strList = new StringList();
        strList.add(SELECT_POLICY);
        strList.add(SELECT_TYPE);
        Map map = partObj.getInfo(context, strList);
        String currentPartPolicyName = (String) map.get(SELECT_POLICY);
        String strType = (String) map.get(SELECT_TYPE);
        String currentPolicyClassification = Part.getPolicyClassification(
                context, currentPartPolicyName);

        if ("edit".equalsIgnoreCase(strMode)) {

            String isPolicyEdit = FrameworkProperties
                    .getProperty(context, "emxManufacturerEquivalentPart.Policy.EnablePartPolicyEditing");
            if ("true".equalsIgnoreCase(isPolicyEdit)) {
                boolean hasChangePolicyAccess = FrameworkUtil.hasAccess(
                        context, partObj, "changepolicy");
                if (hasChangePolicyAccess) {
                    BusinessType partBusType = new BusinessType(strType,
                            context.getVault());
                    partBusType.open(context);
                    // Get the policies of that Object
                    PolicyList partPolicyList = partBusType
                            .getPolicies(context);
                    
                    
                 
                    if(("MFG".equals(requestMap.get("EditMode") )|| (PropertyUtil.getSchemaProperty(context,"policy_ManufacturingPart").equalsIgnoreCase(partObj.getInfo(context,"policy"))))){
                        
                        currentPartPolicyName = partObj.getInfo(context,"policy");
                        returnString.append(i18nNow.getAdminI18NString("Policy",
                                currentPartPolicyName, languageStr));
                        return returnString.toString();
                    }
                   
                    PolicyItr partPolicyItr = new PolicyItr(partPolicyList);
                    partBusType.close(context);

                    while (partPolicyItr.next()) {
                        Policy partPolicy = partPolicyItr.obj();
                        String partPolicyName = partPolicy.getName();
                        String policyClassification = Part
                                .getPolicyClassification(context,
                                        partPolicyName);
                        if (policyClassification
                                .equalsIgnoreCase(currentPolicyClassification)) {
                            if (returnString.length() == 0) {
                                returnString
                                        .append("<select name=\"PolicyDisplay\">");
                            }
                            returnString
                                    .append("<option value=\""
                                            + XSSUtil.encodeForHTMLAttribute(context,partPolicyName)
                                            + "\" "
                                            + (currentPartPolicyName
                                                    .equals(partPolicyName) ? "selected=\"true\""
                                                    : "")
                                            + ">"
                                            + i18nNow.getAdminI18NString(
                                                    "Policy", partPolicyName,
                                                    languageStr) + "</option>");

                        }
                    }
                    if (returnString.length() != 0) {
                        returnString.append("</select>");
                    }
                }
            }
        }

        if (returnString.length() == 0) {
            returnString.append(i18nNow.getAdminI18NString("Policy",
                    currentPartPolicyName, languageStr));
        }

        return returnString.toString();

    } // end of method

    /**
     * This method is used to update the policy of a Part
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds a HashMap containing the following entries: paramMap - a
     *            HashMap containing the following keys, "objectId"
     *            requestMap-PolicyDisplay
     * @return Object - boolean true if the operation is successful
     * @throws Exception
     *             if operation fails
     */

    public Object updatePolicy(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjId = (String) paramMap.get("objectId");

        String[] strNewPolicies = (String[]) requestMap.get("PolicyDisplay");
        String strNewPolicy = "";
        if (strNewPolicies != null && !"null".equals(strNewPolicies)) {
            strNewPolicy = strNewPolicies[0];

            Part partObj = new Part(strObjId);
            String strCurrentPolicy = partObj.getInfo(context, SELECT_POLICY); // Old
            // Vale
            if (strNewPolicy != null && !"null".equals(strNewPolicy)
                    && !strNewPolicy.equalsIgnoreCase(strCurrentPolicy)) {
                partObj.open(context);
                partObj.setPolicy(context, strNewPolicy);
                partObj.close(context);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * Gets the Enterprise Parts associated with the Manufacturer Equivalent
     * Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a HashMap of the following entries: objectList - a
     *            MapList of object information. paramList - a Map of parameter
     *            values, SuiteDirectory, suiteKey, reportFormat, publicPortal.
     * @return a StringList of connected Enterprise Parts as HTMLOutput.
     * @throws Exception
     *             if the operation fails.
     */
    public StringList getMEPEnterpriseParts(Context context, String[] args)
            throws Exception {

        StringList result = new StringList();
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        Map paramList = (HashMap) paramMap.get("paramList");

        String sFrom = (String) paramList.get("from") ;

        String symbAND = "&";

        if(sFrom != null && !"null".equalsIgnoreCase(sFrom) && sFrom.equalsIgnoreCase("FullTextSearch")){
        	symbAND = "&amp;";
        }

        String suiteDir = (String) paramList.get("SuiteDirectory");
        String suiteKey = (String) paramList.get("suiteKey");

        String reportFormat = (String) paramList.get("reportFormat");
        String publicPortal = (String) paramList.get("publicPortal");

        boolean isexport = false;
        String export = (String) paramList.get("exportFormat");
        if (export != null) {
            isexport = true;
        }

        String linkFile = (publicPortal != null && publicPortal
                .equalsIgnoreCase("true")) ? "emxNavigator.jsp" : "emxTree.jsp";

        String LocContextEntId = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT
                + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.id";
        DomainObject.MULTI_VALUE_LIST.add(LocContextEntId);
        String LocContextEntName = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT
                + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT
                + "].from.name";
        DomainObject.MULTI_VALUE_LIST.add(LocContextEntName);
        String LocContextEntRev = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT
                + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT
                + "].from.revision";
        DomainObject.MULTI_VALUE_LIST.add(LocContextEntRev);

        String CorpContextEntId = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT
                + "].from.id";
        DomainObject.MULTI_VALUE_LIST.add(CorpContextEntId);
        String CorpContextEntType = "to["
                + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.type";
        DomainObject.MULTI_VALUE_LIST.add(CorpContextEntType);
        String CorpContextEntName = "to["
                + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.name";
        DomainObject.MULTI_VALUE_LIST.add(CorpContextEntName);
        String CorpContextEntRev = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT
                + "].from.revision";
        DomainObject.MULTI_VALUE_LIST.add(CorpContextEntRev);

        StringList selectStmts = new StringList(11);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_TYPE);
        selectStmts.addElement(SELECT_NAME);
        selectStmts.addElement(SELECT_REVISION);
        selectStmts.addElement(LocContextEntId);
        selectStmts.addElement(LocContextEntName);
        selectStmts.addElement(LocContextEntRev);
        selectStmts.addElement(CorpContextEntId);
        selectStmts.addElement(CorpContextEntType);
        selectStmts.addElement(CorpContextEntName);
        selectStmts.addElement(CorpContextEntRev);
        StringList relSelectStmts = new StringList(2);
        relSelectStmts.addElement(SELECT_RELATIONSHIP_ID);
        relSelectStmts.addElement(SELECT_RELATIONSHIP_NAME);

        try {
            Iterator itr = objectList.iterator();
            int i = 0;
            int count = objectList.size();
            String[] arrobjectId = new String[count];
            while (itr.hasNext()) {
                Map m = (Map) itr.next();
                arrobjectId[i] = (String) m.get("id");
                i++;
            }
            MapList listEquiv = DomainObject.getInfo(context, arrobjectId,
                    selectStmts);
            StringList entIdList = null;
            StringList entTypeList = null;
            StringList entNameList = null;
            StringList entRevList = null;
            String entId = "";
            String entName = "";
            String entRev = "";
            boolean hasEquiv = false;
            for (i = 0; i < listEquiv.size(); i++) {
                StringBuffer output = new StringBuffer(512);
                hasEquiv = false;
                Map map = (Map) listEquiv.get(i);
                entIdList = (StringList) map.get(LocContextEntId);
                entNameList = (StringList) map.get(LocContextEntName);
                entRevList = (StringList) map.get(LocContextEntRev);
                if (entIdList != null && entIdList.size() > 0) {
                    for (int j = 0; j < entIdList.size(); j++) {
                        hasEquiv = true;
                        entId = (String) entIdList.get(j);
                        entName = (String) entNameList.get(j);
                        entRev = (String) entRevList.get(j);
                        if (isexport) {
                            output.append(XSSUtil.encodeForHTML(context,entName) + " " + XSSUtil.encodeForHTML(context,entRev) + " \n");
                        }
                        // do not show hyperlinks if it is a printer friendly or
                        // excel export page
                        // length will be >0 when format is HTML, ExcelHTML, CSV
                        // or TXT
                        else if (null != reportFormat
                                && !reportFormat.equals("null")
                                && (reportFormat.length() > 0)) {
                            output.append(XSSUtil.encodeForHTML(context,entName) + " " + XSSUtil.encodeForHTML(context,entRev) + "<br>");
                        } else {
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + XSSUtil.encodeForURL(context, linkFile)
                                            + "?emxSuiteDirectory="
                                            + XSSUtil.encodeForURL(context,suiteDir)
                                            + symbAND+"suiteKey="
                                            + XSSUtil.encodeForURL(context,suiteKey)
                                            + symbAND+"objectId="
                                            + XSSUtil.encodeForURL(context,entId)
                                            + "', '', '', 'false', 'popup', '')\">"
                                            + XSSUtil.encodeForHTML(context,entName)
                                            + " "
                                            + XSSUtil.encodeForHTML(context,entRev)
                                            + "</a>   ");
                        }
                    }
                }
                entIdList = (StringList) map.get(CorpContextEntId);
                entTypeList = (StringList) map.get(CorpContextEntType);
                entNameList = (StringList) map.get(CorpContextEntName);
                entRevList = (StringList) map.get(CorpContextEntRev);
                if (entIdList != null && entIdList.size() > 0) {
                    for (int j = 0; j < entIdList.size(); j++) {
                        // skip the location equivalent objects - these are
                        // handled in the above loop
                        if (((String) entTypeList.get(j))
                                .equals(TYPE_LOCATION_EQUIVALENT_OBJECT)) {
                            continue;
                        }
                        hasEquiv = true;
                        entId = (String) entIdList.get(j);
                        entName = (String) entNameList.get(j);
                        entRev = (String) entRevList.get(j);
                        if (isexport) {
                            output.append(XSSUtil.encodeForHTML(context,entName) + " " + XSSUtil.encodeForHTML(context,entRev) + " \n");
                        }
                        // do not show hyperlinks if it is a printer friendly or
                        // excel export page
                        // length will be >0 when format is HTML, ExcelHTML, CSV
                        // or TXT
                        else if (null != reportFormat
                                && !reportFormat.equals("null")
                                && (reportFormat.length() > 0)) {
                            output.append(XSSUtil.encodeForHTML(context,entName) + " " + XSSUtil.encodeForHTML(context,entRev) + "<br>");
                        } else {
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + XSSUtil.encodeForURL(context,linkFile)
                                            + "?emxSuiteDirectory="
                                            + XSSUtil.encodeForURL(context,suiteDir)
                                            + symbAND+"suiteKey="
                                            + XSSUtil.encodeForURL(context,suiteKey)
                                            + symbAND+"objectId="
                                            + XSSUtil.encodeForURL(context,entId)
                                            + "', '', '', 'false', 'popup', '')\">"
                                            + XSSUtil.encodeForHTML(context,entName)
                                            + " "
                                            + XSSUtil.encodeForHTML(context,entRev)
                                            + "</a>   ");
                        }
                    }
                }
                if (!"".equals(output.toString())) {
                    result.add(output.toString());
                }

                if (!hasEquiv && symbAND.equalsIgnoreCase("&")) {
                    result.add("&nbsp;");
                }else {
                	result.add("");
                }

            }// end of while

        } catch (FrameworkException Ex) {
            throw Ex;
        }
        // Added for Bug 313092
        DomainObject.MULTI_VALUE_LIST.remove(LocContextEntId);
        DomainObject.MULTI_VALUE_LIST.remove(LocContextEntName);
        DomainObject.MULTI_VALUE_LIST.remove(LocContextEntRev);

        DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntId);
        DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntType);
        DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntName);
        DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntRev);
        return result;
    }// end of method getMEPEnterpriseParts ()

    /**
     * Returns list of location of the manufacturer based on selected
     * manufacturer.
     *
     * @mx.whereUsed This method will be called when the user clicks on the
     *               "Manufacturer Equivalent Parts" link from My Desk
     *               -->Engineering menu.Used "ENCManufacturerEquivalentParts"
     *               table.
     * @mx.summary This method will return the list of location name that are
     *             existing on the "To" side of the relationship "Manufacturing
     *             Location" associated with selected Manufacturer in the
     *             summary page.
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds HashMap containing the following entries: objectId of
     *            Part.
     *
     * @return a Vector : manufacturer Location -->Part(if exist)
     * @throws Exception
     *             if the operation fails.
     */
    public static Vector showManufacturerLocations(Context context,
            String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Vector columnVals = null;
        // getting the MapList of the objects.
        MapList objList = (MapList) programMap.get("objectList");

        int listSize = 0;
        Map map = null;

        if (objList != null && (listSize = objList.size()) > 0) {
            columnVals = new Vector(listSize);
            String relManufacturingLocation = PropertyUtil.getSchemaProperty(
                    context, "relationship_ManufacturingLocation");
            String sManufacturingLocationName = "";
            String strLocation="";
            for (int i = 0; i < listSize; i++) {
                map = (Map) objList.get(i);
                sManufacturingLocationName=(String) map.get("from[" + relManufacturingLocation + "].to.name");
                // check if any Manufacturing Location is connected to any
                // company
                
                strLocation = (UIUtil.isNullOrEmpty(sManufacturingLocationName)) ? "" : sManufacturingLocationName;
                
                columnVals.add(strLocation);
                
            }// end of for loop
        }

        return columnVals;
    }

    /**
     * Program that decides whether a check box is displayed or not
     *
     * @param context
     * @param args
     * @return vector for checkbox containing either "true" or "false"
     * @throws Exception
     */
    public Vector displaySummaryCheckbox(Context context, String[] args)
            throws Exception {
        Vector resultList = new Vector();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            // com.matrixone.apps.common.Person contextPerson =
            // (Person)DomainObject.newInstance(context,
            // DomainConstants.TYPE_PERSON);

            // String hostCompanyId =
            // contextPerson.getPerson(context).getCompany(context).getInfo(context,DomainConstants.SELECT_ID);
            String hostCompanyId = com.matrixone.apps.common.Person.getPerson(
                    context).getCompanyId(context);
            MapList objectList = (MapList) programMap.get("objectList");
            for (Iterator listItr = objectList.iterator(); listItr.hasNext();) {
                Map curObjMap = (HashMap) listItr.next();
                String id = (String) curObjMap.get("id");
                if (hostCompanyId.equals(id)) {
                    resultList.add("false");
                } else {
                    resultList.add("true");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // throw new FrameworkException(ex);
        }
        return resultList;
    }

    /**
     * Thsi mehtod deisplays the lcoation preference field
     *
     * @param context
     * @param args
     * @return vector for Location Preference
     * @throws FrameworkException
     */
    public Vector showLocationPreference(Context context, String[] args)
            throws FrameworkException {
        Vector columnVals = null;
        try {
            HashMap arguMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) arguMap.get("paramList");
            String sMode = (String) paramMap.get("editTableMode");
            MapList objList = (MapList) arguMap.get("objectList");
            int listSize = 0;
            Map map = null;
            String srelId = "";
            String attrRange = "";
            // AttributeType attrType = new AttributeType("Location
            // Preference");

            // String defaultValue = attrType.getDefaultValue(context);
          			 
			 //Modified for IR-048513V6R2012x, IR-118107V6R2012x 
			 StringList attrRanges = FrameworkUtil.getRanges(context, DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE);           
			 
            if (objList != null && (listSize = objList.size()) > 0) {
                columnVals = new Vector(listSize);
                for (int i = 0; i < listSize; i++) {
                    map = (Map) objList.get(i);
                    srelId = (String) map.get("id[connection]");
                    //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                    String oldLocPref = DomainRelationship.getAttributeValue(
                     context, srelId, DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE);
                    if (sMode != null && "true".equals(sMode)) {
                        StringBuffer sbOption = new StringBuffer();
                        int rangeSize = attrRanges.size();
                        sbOption
                                .append("<select name=\"Preference" + i + "\">");
                        for (int j = 0; j < rangeSize; j++) {
                            attrRange = (String) attrRanges.elementAt(j);
                            if (attrRange.equals(oldLocPref)) {
                                sbOption.append("<option value=\"" + attrRange
                                        + "\" selected >");
                                sbOption.append(attrRange);
                                sbOption.append("</option>");
                            } else {
                                sbOption.append("<option value=\"" + attrRange
                                        + "\">");
                                sbOption.append(attrRange);
                                sbOption.append("</option>");
                            }
                        }// end of for

                        sbOption.append("</select>");
                        columnVals.add(sbOption.toString());
                    }
                    if (sMode != null && "false".equals(sMode)) {
                        columnVals.add(oldLocPref);
                    }
                }// end of for
            } else {
                columnVals.add("");
            }

        }// end of try
        catch (Exception e) {
            e.printStackTrace();
            // throw new FrameworkException(e);
        }
        return columnVals;
    }

    /**
     * This method updatesLocationPreference
     *
     * @param context
     * @param args
     * @throws FrameworkException
     */
    public void updateLocationPreference(Context context, String[] args)
            throws FrameworkException {
        try {
            HashMap arguMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) arguMap.get("paramMap");
            HashMap requestMap = (HashMap) arguMap.get("requestMap");
            String relId = (String) paramMap.get("relId");
          //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            if (requestMap.get("objCount") != null
                    || !"null".equals(requestMap.get("objCount"))) {
                String sLocPrefValue = (String) paramMap.get("New Value");
          //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                
                new DomainRelationship().setAttributeValue(context, relId,
                        DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE, sLocPrefValue);
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * This method shows locationstatus
     *
     * @param context
     * @param args
     *            holds object list,mode
     * @return vector location status
     * @throws FrameworkException
     */
    public Vector showLocationStatus(Context context, String[] args)
            throws FrameworkException {
        Vector columnVals = null;
        try {
            HashMap arguMap = (HashMap) JPO.unpackArgs(args);

            HashMap paramMap = (HashMap) arguMap.get("paramList");

            String sMode = (String) paramMap.get("editTableMode");

            MapList objList = (MapList) arguMap.get("objectList");

            int listSize = 0;
            Map map = null;
            String srelId = "";
            String attrRange = "";

            // String defaultValue = attrType.getDefaultValue(context);
          		
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x 
			StringList attrRanges = FrameworkUtil.getRanges(context, DomainConstants.ATTRIBUTE_LOCATION_STATUS);
            
            if (objList != null && (listSize = objList.size()) > 0) {
                columnVals = new Vector(listSize);
                for (int i = 0; i < listSize; i++) {
                    map = (Map) objList.get(i);

                    srelId = (String) map.get("id[connection]");
                   //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                    String oldLocStat = DomainRelationship.getAttributeValue(
                     context, srelId, DomainConstants.ATTRIBUTE_LOCATION_STATUS);
                    if (sMode != null && "true".equals(sMode)) {
                        StringBuffer sbOption = new StringBuffer();
                        int rangeSize = attrRanges.size();
                        sbOption.append("<select name=\"Status" + i + "\">");
                        for (int j = 0; j < rangeSize; j++) {
                            attrRange = (String) attrRanges.elementAt(j);
                            if (attrRange.equals(oldLocStat)) {
                                sbOption.append("<option value=\"" + attrRange
                                        + "\" selected >");
                                sbOption.append(attrRange);
                                sbOption.append("</option>");
                            } else {
                                sbOption.append("<option value=\"" + attrRange
                                        + "\">");
                                sbOption.append(attrRange);
                                sbOption.append("</option>");
                            }
                        }// end of for

                        sbOption.append("</select>");
                        columnVals.add(sbOption.toString());
                    }
                    if (sMode != null && "false".equals(sMode)) {
                        columnVals.add(oldLocStat);
                    }
                }// end of for
            } else {
                columnVals.add("");
            }

        }// end of try
        catch (Exception e) {
            e.printStackTrace();
            // throw new FrameworkException(e);
        }
        return columnVals;
    }

    /**
     * This method updates location status
     *
     * @param context
     * @param args
     *            holds relid
     * @throws FrameworkException
     */
    public void updateLocationStatus(Context context, String[] args)
            throws FrameworkException {
        try {
            HashMap arguMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) arguMap.get("paramMap");
            HashMap requestMap = (HashMap) arguMap.get("requestMap");
            String relId = (String) paramMap.get("relId");
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            if (requestMap.get("objCount") != null
                    || !"null".equals(requestMap.get("objCount"))) {
                String sLocStatValue = (String) paramMap.get("New Value");
             //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                new DomainRelationship().setAttributeValue(context, relId,
                        DomainConstants.ATTRIBUTE_LOCATION_STATUS, sLocStatValue);
            }
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * Gets the Locations associated with the Manufacturer Equivalent Part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap with the following entries: objectId -
     *            a String containing the Manufacturer Equivalent object id.
     * @return a MapList of Location information.
     * @throws Exception
     *             if the operation fails.
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMEPLocations(Context context, String[] args)
            throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        //Added for bug # 357589
        String TYPE_PLANT    = PropertyUtil.getSchemaProperty(context,"type_Plant");
        StringList selectStmts = new StringList(3);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_NAME);
        selectStmts.addElement(SELECT_TYPE);

        StringList validAllocRespFromTypes = new StringList(5);
        validAllocRespFromTypes.addElement(TYPE_ORGANIZATION);
        validAllocRespFromTypes.addElement(TYPE_COMPANY);
        validAllocRespFromTypes.addElement(TYPE_BUSINESS_UNIT);
        validAllocRespFromTypes.addElement(TYPE_DEPARTMENT);
        validAllocRespFromTypes.addElement(TYPE_LOCATION);
//      Added for bug :357589
        validAllocRespFromTypes.addElement(TYPE_PLANT);

        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

        StringBuffer sbRelPattern = new StringBuffer(
                RELATIONSHIP_MANUFACTURER_EQUIVALENT);
        sbRelPattern.append(',');
        sbRelPattern.append(RELATIONSHIP_ALLOCATION_RESPONSIBILITY);

        StringBuffer sbTypePattern = new StringBuffer(
                TYPE_LOCATION_EQUIVALENT_OBJECT);
        sbTypePattern.append(',');
        sbTypePattern.append(DomainConstants.TYPE_LOCATION);
        sbTypePattern.append(',');
        sbTypePattern.append(DomainConstants.TYPE_ORGANIZATION);

        StringBuffer sbCorpMEPLocTypePattern = new StringBuffer(
                DomainConstants.TYPE_LOCATION);
        sbCorpMEPLocTypePattern.append(',');
        sbCorpMEPLocTypePattern.append(DomainConstants.TYPE_ORGANIZATION);

        MapList locEquivMEPLocList = new MapList();
        MapList corpMEPLocList = new MapList();

        MapList locationList = new MapList();

        Vector vecLocId = new Vector();
        Vector vecLocName = new Vector();
        Vector vecLocEquivRelId = new Vector();
        Vector vecAllocRespRelId = new Vector();
        Vector vecIsLocEquiv = new Vector();

        try {

            setId(objectId);

            // fetching locations associated with MEP via location equib object
            // (Location Equiv MEP)
            locEquivMEPLocList = getRelatedObjects(context, sbRelPattern
                    .toString(), // relationship pattern
                    sbTypePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    false, // from direction
                    (short) 2, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            // fetching locations associated with MEP directly
            // (Corporate MEP)
            corpMEPLocList = getRelatedObjects(context,
                    RELATIONSHIP_ALLOCATION_RESPONSIBILITY,// relationship
                    // pattern
                    sbCorpMEPLocTypePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    false, // from direction
                    (short) 1, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            Map tempMap = null;
            String strType = null;
            String strLevel = null;

            for (int i = 0; i < locEquivMEPLocList.size(); i++) {
                tempMap = (Map) locEquivMEPLocList.get(i);
                strType = (String) tempMap.get(SELECT_TYPE);
                strLevel = (String) tempMap.get("level");

                // The above Map List would contain Location Object at level 2
                // and Allocation Resp relationship at level 1
                if ("2".equals(strLevel)) {
                    vecLocId.addElement(tempMap.get(SELECT_ID));
                    vecLocName.addElement(tempMap.get(SELECT_NAME));
                    vecAllocRespRelId.addElement(tempMap
                            .get(SELECT_RELATIONSHIP_ID));
                    //Added for 349412
                     vecLocEquivRelId.addElement(tempMap
                            .get(SELECT_RELATIONSHIP_ID));
                     //end
                    vecIsLocEquiv.addElement("(equiv)");
                }
                //commented for 349412 as this is handled already in the below list
            /*    if ("1".equals(strLevel)) {
                    vecLocEquivRelId.addElement(tempMap
                            .get(SELECT_RELATIONSHIP_ID));
                }*/

            }// end of for locEquivMEPLocations

            for (int j = 0; j < corpMEPLocList.size(); j++) {
                tempMap = (Map) corpMEPLocList.get(j);
                strType = (String) tempMap.get(SELECT_TYPE);

                // The above Map List would contain Location Object at level 2
                // and Allocation Resp relationship at level 1
                if (validAllocRespFromTypes.contains(strType)) {
                    vecLocId.addElement(tempMap.get(SELECT_ID));
                    vecLocName.addElement(tempMap.get(SELECT_NAME));
                    vecAllocRespRelId.addElement(tempMap
                            .get(SELECT_RELATIONSHIP_ID));
                    vecLocEquivRelId.addElement(tempMap
                            .get(SELECT_RELATIONSHIP_ID));
                    vecIsLocEquiv.addElement(" ");// dummy for corp MEP
                }
            }// end of for corpMEPLocList

            HashMap resultMap = null;

            for (int k = 0; k < vecLocId.size(); k++) {
                resultMap = new HashMap();
                resultMap.put(SELECT_ID, (String) vecLocId.elementAt(k));
                resultMap.put(SELECT_NAME, (String) vecLocName.elementAt(k));
                resultMap.put(KEY_ALLOCATION_REL_ID, (String) vecAllocRespRelId
                        .elementAt(k));
                resultMap.put(SELECT_RELATIONSHIP_ID, (String) vecLocEquivRelId
                        .elementAt(k));
                resultMap.put(KEY_LABEL_LOC_EQUIV, (String) vecIsLocEquiv
                        .elementAt(k));
                locationList.add(resultMap);
            }

        } catch (Exception Ex) {
            throw Ex;
        }

        return locationList;
    }// end of method getMEPLocations()

    /**
     * Thsi method displays the type
     *
     * @param context
     * @param args
     * @return String for type
     * @throws Exception
     */
    public String showTypeSelected(Context context, String[] args)
            throws Exception {
        HashMap arguMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) arguMap.get("requestMap");
        String type = (String) requestMap.get("selectType");
        String imageForType = "";
        if (type.indexOf("type_") == 0) {
            imageForType = FrameworkProperties.getProperty(context, "emxFramework.smallIcon." + type);
        } else {
            imageForType = FrameworkProperties.getProperty(context, "emxFramework.smallIcon.type_" + type);
        }
        StringBuffer sb = new StringBuffer(64);
        sb.append("<img src=\"../common/images/");
        sb.append(imageForType);
		sb.append("\"></img>");
        if (type.indexOf("type_") == 0) {
            sb.append(PropertyUtil.getSchemaProperty(context, type));
        } else {
            sb.append(type);
        }

        return sb.toString();
    }

    /**
     * This mthod displays the revision date field
     *
     * @param context
     * @param args
     * @return html string for Revision field
     * @throws Exception
     */
    public String showRevisionDateField(Context context, String[] args)
            throws Exception {
        String fieldName = "RevisionDateFirst";
        String fieldName1 = "RevisionDateLast";
        StringBuffer outStr = new StringBuffer(2048);
        outStr.append("<select name=\"DateOption\" extra=\"yes\" onChange=\"javascript:showDateBetweenField();\">");
        outStr.append("<option value=\"");
        outStr.append("*" + "\">");
        outStr.append('*');
        outStr.append("<option value=\"");
        outStr.append("Is On" + "\">");
        outStr.append("Is On");
        outStr.append("<option value=\"");
        outStr.append("Is On or Before " + "\">");
        outStr.append("Is On or Before");
        outStr.append("<option value=\"");
        outStr.append("Is On or After" + "\">");
        outStr.append("Is On or After");
        outStr.append("<option value=\"");
        outStr.append("Is Between" + "\">");
        outStr.append("Is Between");
        outStr.append("</select>&nbsp;&nbsp;&nbsp;");
        outStr.append("<input type=\"text\" readonly=\"readonly\" ");
        outStr.append("\" name=\"");
        outStr.append(fieldName);
        outStr.append("\" value=\"\" id=\"");
        outStr.append(fieldName);
        outStr.append("\">&nbsp;<a href=\"javascript:showCalendar('editDataForm', '");
        outStr.append(fieldName);
        outStr.append("', '', saveFieldObjByName('");
        outStr.append(fieldName);
        outStr.append("'))\"><img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\"></a>");
        outStr.append("<input type=\"hidden\" name=\"");
        outStr.append(fieldName).append("_msvalue");
        outStr.append("\"  value=\"");
        outStr.append("\">&nbsp;&nbsp");

        outStr.append("<input type=\"text\" readonly=\"readonly\" style=\"visibility:hidden\"");
        outStr.append("\" name=\"");
        outStr.append(fieldName1);
        outStr.append("\" value=\"\" id=\"");
        outStr.append(fieldName1);
        outStr.append("\">&nbsp;<a href=\"javascript:showCalendar('editDataForm', '");
        outStr.append(fieldName1);
        outStr.append("', '', saveFieldObjByName('");
        outStr.append(fieldName1);
        outStr.append("'))\"><img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\" name=\"picture\" id=\"picture\" style=\"visibility:hidden\"></a>");
        outStr.append("<input type=\"hidden\" name=\"");
        outStr.append(fieldName1).append("_msvalue");
        outStr.append("\"  value=\"");
        outStr.append("\">");
        outStr.append("<script language=\"JavaScript\">assignValidateMethod('RevisionDateFirst', 'emptyDateField');</script>");

        return outStr.toString();
    }

    /**
     * This method displays owner field
     *
     * @param context
     * @param args
     * @return String html for displaying owner field
     * @throws Exception
     */
    public String showOwnerField(Context context, String[] args)
            throws Exception {
    	String strClear = EnoviaResourceBundle.getProperty(context ,
                "emxManufacturerEquivalentPartStringResource",
                context.getLocale(),"emxManufacturerEquivalentPart.Common.Clear");
        String strURL = "../components/emxCommonSearch.jsp?formName=editDataForm&frameName=formEditDisplay&searchmode=PersonChooser&suiteKey=Components&searchmenu=APPMemberSearchInMemberList&searchcommand=APPFindPeople&fieldNameDisplay=OwnerDisplay&fieldNameActual=Owner";
        StringBuffer outPut = new StringBuffer(512);
        outPut.append("<input type=\"text\" name=\"OwnerDisplay");
        outPut.append("\"size=\"20\" value=\"*\"");
        outPut.append(" readonly=\"readonly\">&nbsp;");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnOwnerChooser\" size=\"200\" ");
        outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        outPut.append(strURL);
        outPut.append("', 700, 500)\">&nbsp;&nbsp;");
        outPut.append("<a href=\"JavaScript:clearField('editDataForm','Owner','OwnerDisplay')\">");
        outPut.append(strClear);
		outPut.append("</a>");
        outPut.append("<input type=\"hidden\" name=\"Owner\" value=\"*\"></input>");

				return outPut.toString();
    }

    /**
     * this method displays the originator field
     *
     * @param context
     * @param args
     * @return html string for Originator field
     * @throws Exception
     */
    public String showOrginatorField(Context context, String[] args)
            throws Exception {
    	String strClear = EnoviaResourceBundle.getProperty(context ,
                "emxManufacturerEquivalentPartStringResource",
                context.getLocale(),"emxManufacturerEquivalentPart.Common.Clear");
        String strURL = "../components/emxComponentsFindMemberDialogFS.jsp?formName=editDataForm&frameName=formEditDisplay&fieldNameDisplay=OrginatorDisplay&fieldNameActual=Orginator";
        StringBuffer outPut = new StringBuffer(512);
        outPut.append("<input type=\"text\" name=\"OrginatorDisplay");
        outPut.append("\"size=\"20\" value=\"*\"");
        outPut.append(" readonly=\"readonly\">&nbsp;");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
        outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        outPut.append(strURL);
        outPut.append("', 700, 500)\">&nbsp;&nbsp;");
        outPut.append("<a href=\"JavaScript:clearField('editDataForm','Orginator','OrginatorDisplay')\">");
        outPut.append(strClear);
		outPut.append("</a>");
        outPut.append("<input type=\"hidden\" name=\"Orginator\" value=\"*\"></input>");

        return outPut.toString();
    }

    /**
     * Thsi method displays the policy field
     *
     * @param context
     * @param args
     * @return html string for showPolicyfield
     * @throws Exception
     */
    public String showPolicyField(Context context, String[] args)
            throws Exception {
        StringBuffer sb = new StringBuffer(256);
        String typePart = PropertyUtil.getSchemaProperty(context, "type_Part");
        StringList policyList = Part.getEquivalentPolicies(context, typePart);
        String languageStr = context.getSession().getLanguage();
        StringList policyViewI18NStr = i18nNow.getAdminI18NStringList("Policy", policyList, languageStr);
        sb
                .append("<select name=\"Policy\" extra=\"yes\" onChange=\"javascript:showStatesForPolicy();\">");
        for (int polItr = 0; polItr < policyList.size(); polItr++) {
            sb.append("<option value=\"");
            sb.append(policyList.get(polItr));
			sb.append("\">");
            sb.append(policyViewI18NStr.get(polItr));
        }
        sb.append("</select>");
        sb.append("<script>");
        sb.append("window.onload=showStatesForPolicy;");
        sb.append("</script>");
        return sb.toString();

    }

    /**
     * This method returns a maplist of MEPs
     *
     * @param context
     * @param args
     * @return Maplist of MEP
     * @throws FrameworkException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMEPSearchResult(Context context, String[] args)
            throws FrameworkException {
        MapList organizationList = new MapList();
        try {
            String attrOriginator = PropertyUtil.getSchemaProperty(context,
                    "attribute_Originator");
            String attrRevisionDate = PropertyUtil.getSchemaProperty(context,
                    "attribute_ExternalRevisionDate");
            String attrExternalRevision = PropertyUtil
                    .getSchemaProperty(
                            context,
                            DomainSymbolicConstants.SYMBOLIC_attribute_ExternalRevisionLevel);
            String relManufacturingLocation = PropertyUtil.getSchemaProperty(
                    context, "relationship_ManufacturingLocation");
            String relManufacturingResponsibility = PropertyUtil
                    .getSchemaProperty(context,
                            "relationship_ManufacturingResponsibility");
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String sType = (String) paramMap.get("selectType");
            if (sType.indexOf("type_") == 0) {
                sType = PropertyUtil.getSchemaProperty(context, sType);
            }
            String strTimeZone = (String) paramMap.get("timeZone");
            double clientTZOffset = (new Double(strTimeZone)).doubleValue();
            java.util.Locale locale = (java.util.Locale) paramMap
                    .get("localeObj");
            String sName = (String) paramMap.get("Name");
            String sRev = (String) paramMap.get("Revision");
            String sOwner = (String) paramMap.get("OwnerDisplay");
            String sDescription = (String) paramMap.get("Description");
            String sOriginator = (String) paramMap.get("Orginator");
            String sPolicy = (String) paramMap.get("Policy");
            String sState = (String) paramMap.get("State");
            String sManufacturer = (String) paramMap.get("Manufacturer");
            String sManufacturerLocation = (String) paramMap
                    .get("ManufacturerLocationOID");
            String sRevisionDateFirst = (String) paramMap
                    .get("RevisionDateFirst");
            String sRevisionDateLast = (String) paramMap
                    .get("RevisionDateLast");
            String sDateOption = (String) paramMap.get("DateOption");
            String sRevisionLevel = (String) paramMap.get("RevisionLevel");

            boolean start = true;
            StringBuffer sbWhereExp = new StringBuffer(200);

            if (sDescription != null && (!"*".equals(sDescription))
                    && (!("").equals(sDescription))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                }
                sbWhereExp.append('(');
                sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
                sbWhereExp.append(" ~~ ");
                sbWhereExp.append('\"');
                sbWhereExp.append(sDescription);
                sbWhereExp.append('\"');
                sbWhereExp.append(')');
            }

            if (sState != null && (!("").equals(sState))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append('(');
                sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                sbWhereExp.append(" ~~ ");
                sbWhereExp.append('\"');
                sbWhereExp.append(sState);
                sbWhereExp.append('\"');
                sbWhereExp.append(')');
            }

            if (sPolicy != null && (!"".equals(sPolicy))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append('(');
                sbWhereExp.append(DomainConstants.SELECT_POLICY);
                sbWhereExp.append(" == ");
                sbWhereExp.append('\"');
                sbWhereExp.append(sPolicy);
                sbWhereExp.append('\"');
                sbWhereExp.append(')');
            }

            if (sOriginator != null
                    && (!"*".equals(sOriginator) && !"".equals(sOriginator))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append('(');
                sbWhereExp.append("attribute[" + attrOriginator + "]");
                sbWhereExp.append(" == ");
                sbWhereExp.append('\"');
                sbWhereExp.append(sOriginator);
                sbWhereExp.append('\"');
                sbWhereExp.append(')');
            }

            if (sRevisionDateFirst != null && !"".equals(sRevisionDateFirst)) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                if ("Is On".equals(sDateOption)) {
                    sbWhereExp.append('(');
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" <= ");
                    sbWhereExp.append('\"');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append('\"');
                    sbWhereExp.append(" && ");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" >= ");
                    sbWhereExp.append('\"');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "12:00:00 AM",
                                    clientTZOffset, locale));
                    sbWhereExp.append('\"');
                    sbWhereExp.append(')');
                } else if ("Is On or Before".equals(sDateOption)) {
                    sbWhereExp.append('(');
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" <= ");
                    sbWhereExp.append('\"');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append('\"');
                    sbWhereExp.append(')');
                } else if ("Is On or After".equals(sDateOption)) {
                    sbWhereExp.append('(');
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" >= ");
                    sbWhereExp.append('\"');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "12:00:00 AM",
                                    clientTZOffset, locale));
                    sbWhereExp.append('\"');
                    sbWhereExp.append(')');
                } else if ("Is Between".equals(sDateOption)) {
                    sbWhereExp.append('(');
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" >= ");
                    sbWhereExp.append('\"');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "12:00:00 AM",
                                    clientTZOffset, locale));
                    sbWhereExp.append('\"');
                    sbWhereExp.append(" && ");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" <= ");
                    sbWhereExp.append('\"');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateLast, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append('\"');
                    sbWhereExp.append(')');
                } else {
                    sbWhereExp.append('(');
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" matchlist ");
                    sbWhereExp.append('\'');
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\' \',\')");

                }
            }

            if (sManufacturer != null && (!"*".equals(sManufacturer) && !"".equals(sManufacturer))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append('(');
                sbWhereExp.append("to[" + relManufacturingResponsibility
                        + "].from.id");
                sbWhereExp.append(" matchlist ");
                sbWhereExp.append('\'');
                sbWhereExp.append(sManufacturer);
                sbWhereExp.append("\' \',\')");
            }

            if (sManufacturerLocation != null
                    && (!"*".equals(sManufacturerLocation) && !""
                            .equals(sManufacturerLocation))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append('(');
                sbWhereExp.append("from[" + relManufacturingLocation
                        + "].to.id");
                sbWhereExp.append(" matchlist ");
                sbWhereExp.append('\'');
                sbWhereExp.append(sManufacturerLocation);
                sbWhereExp.append("\' \',\')");
            }

            if (sRevisionLevel != null
                    && (!"*".equals(sRevisionLevel) && !""
                            .equals(sRevisionLevel))) {
                if (start) {
                    sbWhereExp.append('(');
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append('(');
                sbWhereExp.append("attribute[" + attrExternalRevision + "]");
                sbWhereExp.append(" matchlist ");
                sbWhereExp.append('\'');
                sbWhereExp.append(sRevisionLevel);
                sbWhereExp.append("\' \',\')");
            }

            if (!start) {
                sbWhereExp.append(')');
            }

            /**
             * ************************Vault Code
             * Start****************************
             */
            // Get the user's vault option & call corresponding methods to get
            // the vault's.
            String txtVault = "";
            String strVaults = "";
            StringList strListVaults = new StringList();

            String txtVaultOption = (String) paramMap.get("vaultOption");

            if (txtVaultOption == null || "".equals(txtVaultOption)) {
                txtVaultOption = (String) paramMap.get("vaultsDisplay");
                ;
            }

            if ("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption)) {
                // get ALL vaults
                Iterator mapItr = VaultUtil.getVaults(context).iterator();
                if (mapItr.hasNext()) {
                    txtVault = (String) ((Map) mapItr.next()).get("name");
                    while (mapItr.hasNext()) {
                        Map map = (Map) mapItr.next();
                        txtVault += "," + (String) map.get("name");
                    }
                }

            } else if ("LOCAL_VAULTS".equals(txtVaultOption)) {
                // get All Local vaults
                com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person
                        .getPerson(context);
                Company company = person.getCompany(context);
                strListVaults = OrganizationUtil.getLocalVaultsList(context,
                        company.getObjectId());

                StringItr strItr = new StringItr(strListVaults);
                if (strItr.next()) {
                    strVaults = strItr.obj().trim();
                }
                while (strItr.next()) {
                    strVaults += "," + strItr.obj().trim();
                }
                txtVault = strVaults;
            } else if ("DEFAULT_VAULT".equals(txtVaultOption)) {
                txtVault = context.getVault().getName();
            } else {
                txtVault = txtVaultOption;
            }
            // trimming
            txtVault = txtVault.trim();
            /**
             * *****************************Vault Code
             * End**************************************
             */
            String queryLimit = (String) paramMap.get("queryLimit");
            StringList selectStmts = new StringList(1);
            selectStmts.addElement("id");

            organizationList = DomainObject.findObjects(context, sType, sName,
                    sRev, sOwner, txtVault, sbWhereExp.toString(), null, true,
                    selectStmts, Short.parseShort(queryLimit));

            return organizationList;
        } catch (Exception Ex) {
            throw new FrameworkException(Ex);
        }
    }

    /**
     *
     * This method gets list of manufactureres
     *
     * @param context
     * @param args
     *            holds name and type
     * @return MapList of manufacturers
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getManufacturers(Context context, String[] args)
            throws Exception {

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String sWhereExp = "";
        String txtName = (String) paramMap.get("Name");
        String selectType = (String) paramMap.get("type");
        MapList totalresultList = new MapList();
        SelectList resultSelects = new SelectList(7);
        resultSelects.add(DomainObject.SELECT_ID);
        resultSelects.add(DomainObject.SELECT_TYPE);
        resultSelects.add(DomainObject.SELECT_NAME);
        resultSelects.add(DomainConstants.SELECT_CURRENT);
        resultSelects.add(DomainObject.SELECT_DESCRIPTION);
        resultSelects.add("current.access[fromconnect]");

        try {
            String manufacturerId = com.matrixone.apps.common.Company
                    .getHostCompany(context);
            com.matrixone.apps.common.Company company = new com.matrixone.apps.common.Company(
                    manufacturerId);
            if (selectType.equals(PropertyUtil.getSchemaProperty(context,
                    "type_BusinessUnit"))) {
                if (!"*".equals(txtName)) {
                    sWhereExp = " name ~~ \"" + txtName + "\" ";
                    sWhereExp += " && ";
                }
                sWhereExp += "current == Active";

                // Code Modified for bug Id - 356171 :Getting  all Business units
                totalresultList=   DomainObject.findObjects(context,
                        selectType, "*", sWhereExp , resultSelects);

            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            } else if (selectType.equals(DomainConstants.TYPE_COMPANY)) {
                //Start - Modified for bug 354734
                sWhereExp += "current == Active";
                //Modified for bug 354734
              
                //Modified for IR-048513V6R2012x, IR-118107V6R2012x start
				String sRelCustomer = PropertyUtil.getSchemaProperty(context, "relationship_Customer");
				totalresultList = Company.getCompanies(context, txtName, new StringList("to["+sRelCustomer+"].from.name"), sWhereExp);
              //Modified for IR-048513V6R2012x, IR-118107V6R2012x end
              
                // Code added for Bug Id  ;_024715 :looping assuming that there wont be more Manufacturers
                	for(int i=0;i<totalresultList.size();i++)
                	{
                		Map m =(Map) totalresultList.get(i);
                		//Modified for IR-048513V6R2012x, IR-118107V6R2012x
                		if(m.containsKey("to["+sRelCustomer+"].from.name"))
                		{
                		 totalresultList.remove(m);
                		}
                	}

                //End - Modified for bug 354734
            } else if (selectType.equals(DomainConstants.TYPE_LOCATION)) {
                if (!"*".equals(txtName)) {
                    sWhereExp = " name ~~ \"" + txtName + "\" ";
                    sWhereExp += " && ";
            }
                sWhereExp += "current == Active";
                totalresultList = company.getLocations(context, resultSelects,
                        sWhereExp);
            } else if (selectType.equals(DomainConstants.TYPE_DEPARTMENT)) {
                if (!"*".equals(txtName)) {
                    sWhereExp = " name ~~ \"" + txtName + "\" ";
                    sWhereExp += " && ";
                }
                sWhereExp += "current == Active";
                totalresultList = company.getRelatedObjects(context, DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT, DomainConstants.TYPE_DEPARTMENT, resultSelects, null, false, true, (short)1, sWhereExp, null);
            }
        } catch (FrameworkException Ex) {
            throw Ex;
        }

        return totalresultList;
    }

    /**
     * @param context
     * @param args
     * @return Maplist of MEPs
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getInProcessMEPs(Context context, String[] args)
            throws Exception {
    	String sInitialMEPLimit = "0";
    	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    	String sFromExportToExcel =  (String) paramMap.get("sFromExportToExcel");
    	String sFromCommonComponentFilter =(String) paramMap.get("fromForm");
    	if("true".equals(sFromCommonComponentFilter)){
    		MapList mlFilteredList = getFilteredMEP(context, args);
    		return mlFilteredList;
    	}
    	/*String sInitialMEPLimit = EnoviaResourceBundle.getProperty(context ,
                "emxManufacturerEquivalentPartStringResource",
                context.getLocale(),"emxManufacturerEquivalentPart.MEPInitialLoad.QueryLimit");*/

        MapList mepList = new MapList();
        try {
            Part partObj = new Part();
            StringList selectStmts = partObj.getMEPSelectList(context);
            
            
            String relManufacturingLocation = PropertyUtil.getSchemaProperty(
                    context, "relationship_ManufacturingLocation");
            selectStmts.addElement("from["
                    + relManufacturingLocation + "].to.name");
            
            String manEquAloResNameSel = "to["
				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
			
			String manEquAloResIdSel = "to["
				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";
			String aloResNameSel = "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
			String aloResIdSel = "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";
			
			String manEquAloResStaSel = "to["
				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT 
				+ "].from.to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY
				+ "].attribute[" 
				+ ATTRIBUTE_LOCATION_STATUS
				+ "].value";

			// Location Status select via the relationship route
			// Part --> Allocation Responsibility
			String aloResStaSel = "to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY
				+ "].attribute[" 
				+ ATTRIBUTE_LOCATION_STATUS
				+ "].value";
			
			// Location Preference select via the relationship route
            // Part --> Manufacture Equivalent --> Allocation Responsibility
            String manEquAloResPreSel = "to["
                    + RELATIONSHIP_MANUFACTURER_EQUIVALENT 
                    + "].from.to["
                    + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                    + "].attribute[" 
                    + ATTRIBUTE_LOCATION_PREFERENCE
                    + "].value";

            // Location Preference select via the relationship route
            // Part --> Allocation Responsibility
            String aloResPreSel = "to["
                    + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                    + "].attribute[" 
                    + ATTRIBUTE_LOCATION_PREFERENCE
                    + "].value";			
			
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResNameSel);
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResIdSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResNameSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResIdSel);
			
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResStaSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResStaSel);
			
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResPreSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResPreSel);
            
			selectStmts.addElement(manEquAloResNameSel);
			selectStmts.addElement(manEquAloResIdSel);
			selectStmts.addElement(aloResNameSel);
			selectStmts.addElement(aloResIdSel);
			
			selectStmts.addElement(manEquAloResStaSel);
			selectStmts.addElement(aloResStaSel);
			
			selectStmts.addElement(manEquAloResPreSel);
			selectStmts.addElement(aloResPreSel);
			
            
            // create where clause to filter out only MEP's in release state
            // Added for Bug: 308765
            //String strMEPRelease = Part
                    //.getReleaseState(context, POLICY_MANUFACTURER_EQUIVALENT);
            String whereCls = "";
            if(!("True".equals(sFromExportToExcel))){
            	//whereCls = " (current == " + strMEPRelease + ") ";
            	sInitialMEPLimit =EnoviaResourceBundle.getProperty(context, "emxManufacturerEquivalentPart.MEPInitialLoad.QueryLimit");
            }
            int iInitialMEPLimit = Integer.parseInt(sInitialMEPLimit);
            String vault = QUERY_WILDCARD;
            mepList = partObj.getManufacturerEquivalentsWithLimit(context, selectStmts,
                    vault, whereCls,iInitialMEPLimit);
            DomainObject.MULTI_VALUE_LIST.remove(manEquAloResNameSel);
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResIdSel);
			DomainObject.MULTI_VALUE_LIST.remove(aloResNameSel);
			DomainObject.MULTI_VALUE_LIST.remove(aloResIdSel);
			
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResStaSel);
			DomainObject.MULTI_VALUE_LIST.remove(aloResStaSel);
			
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResPreSel);
            DomainObject.MULTI_VALUE_LIST.remove(aloResPreSel);
            
        } catch (FrameworkException Ex) {
            throw Ex;
        }
        return mepList;
    }

    /**
     * Method to associate External interface to the equivalent part and set the
     * "Enable Compliance" attribute as "Enabled".
     *
     * @mx.whereUsed Invoked by the trigger object on creation of Part-
     *               TypePartCreateAction,
     * @mx.summary This method associates External Part Data interface to a
     *             Equivalent part. Uses <code>MqlUtil.mqlCommand</code> to
     *             associate the interface to the part. Uses
     *             <code>DomainObject.setAttribute() </code>to set an
     *             attribute value
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param sPartPolicyClassification
     *            <code>String</code>
     * @throws FrameworkException
     *             if the operation fails
     * @since EC 10-7
     */
    public int associateExternalInterface(Context context, String args[])
            throws Exception {
        int status = 0;
        try {
            // Get Created Part ObjectId from Environment Variables
            String sExternalPartData = PropertyUtil
                    .getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_interface_ExternalPartData);
            String sObjectId = MqlUtil.mqlCommand(context, "get env $1","OBJECTID");
            boolean isMEP = false;

            if (sObjectId != null && !"".equals(sObjectId)) {
                Part part = new Part(sObjectId);
                String sPolicyClassification = part.getInfo(context,
                        "policy.property[PolicyClassification].value");

                // enable MEP by default
                if (sPolicyClassification != null
                        && "Equivalent".equals(sPolicyClassification)) {
                    isMEP = true;
                }
                if (isMEP) {
                    /*
                     * MqlUtil.mqlCommand(context, "modify bus \"" + sObjectId +
                     * "\" add interface \"External Part Data\";");
                     */
                    MqlUtil
                            .mqlCommand(context, "modify bus $1 add interface $2;",sObjectId,sExternalPartData);
                }
            }
        } catch (Exception e) {
            status = 1;
            throw new FrameworkException(e);
        } finally {
            return status;
        }
    }

    /**
     * Returns whether the objects are Enterprise Parts or Equivalent Parts.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap with the following entries:
     *            objectList - a MapList of object information. paramList - a
     *            HashMap of parameters.
     * @return a Vector of part origin values.
     * @throws Exception
     *             if the operation fails.
     * @since AEF 10.0.0.0.
     */
    public Vector getPartOrigin(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");

        Vector columnValues = new Vector(relBusObjPageList.size());

        String bArr[] = new String[relBusObjPageList.size()];
        StringList bSel = new StringList();

        // Get the object elements - OIDs and RELIDs
        for (int i = 0; i < relBusObjPageList.size(); i++) {
            // Get Business object Id
            bArr[i] = (String) ((HashMap) relBusObjPageList.get(i)).get("id");
        }

        // Add Business object selects
        bSel.add("policy.property[PolicyClassification].value");
        try {
            // Process the OIDs to get the results
            BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, bArr, bSel);

            // Code for processing the result data obtained
            String strManufacturerEquivalent = EnoviaResourceBundle.getProperty(context ,
                    "emxManufacturerEquivalentPartStringResource",
                    context.getLocale(),"emxManufacturerEquivalentPart.Part.MfgEquivalent");
            String strEnterprise = EnoviaResourceBundle.getProperty(context ,
                    "emxManufacturerEquivalentPartStringResource",
                    context.getLocale(),"emxManufacturerEquivalentPart.Common.Enterprise");

            for (int i = 0; i < bwsl.size(); i++) {
                String propertyValue = bwsl.getElement(i).getSelectData(
                        "policy.property[PolicyClassification].value");

                // Build the Vector "columnValues" with the list of values to be
                // displayed in the column
                if ("Equivalent".equals(propertyValue)) {
                    columnValues.add(strManufacturerEquivalent);
                } else {
                    columnValues.add(strEnterprise);
                }
            }
        } catch (FrameworkException Ex) {
            throw Ex;
        }

        return columnValues;
    }
    /**
     * Method to display the Policy field in the create mep webform
     * Loads the revision for policy change if the uniquie identifier property setting is set to Policy
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap with the following entries:
     *            objectList - a MapList of object information. paramList - a
     *            HashMap of parameters.
     * @return a String having the type chooser URL .
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public String showMEPPolicy(Context context, String[] args)
            throws Exception {
        StringBuffer resultBuffer = new StringBuffer(256);
        String typePart = PropertyUtil.getSchemaProperty(context, "type_Part");
        StringList policyList = Part.getEquivalentPolicies(context, typePart);
        String languageStr = context.getSession().getLanguage();
        StringList policyViewI18NStr = i18nNow.getAdminI18NStringList("Policy", policyList, languageStr);

       resultBuffer
        .append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>");
        resultBuffer.append("<select name=\"Policy\" id=\"PolicyId\" onChange=\"javascript:loadRevision();\">");
        for (int polItr = 0; polItr < policyList.size(); polItr++) {
        resultBuffer.append("<option value=\"");
        resultBuffer.append(policyList.get(polItr));
		resultBuffer.append("\">");

        resultBuffer.append(policyViewI18NStr.get(polItr));
        resultBuffer.append("</option>");
        }
        resultBuffer.append("</select>");

        return resultBuffer.toString();
    }
    /**
     * Method to display the Type field in the webform as a chooser
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap with the following entries:
     *            objectList - a MapList of object information. paramList - a
     *            HashMap of parameters.
     * @return a String having the type chooser URL .
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public String showOrganizationTypeField(Context context, String[] args)
            throws Exception
    {
       HashMap programMap=(HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       String isManufacturingLocation=(String)requestMap.get("isManufacturingLocation");
       String clearManuLoc=(String)requestMap.get("clearManuLoc");
       String inclusionList="type_Location,type_Organization";
       String defaultType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Company);
       if(isManufacturingLocation!=null && "Yes".equals(isManufacturingLocation)){
          inclusionList = "type_Location";
          defaultType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Location);
       }
       if("true".equals(clearManuLoc)){
           inclusionList="type_Company,type_BusinessUnit";
           defaultType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Company);
       }
       String idefaultType = i18nNow.getTypeI18NString(defaultType, context.getSession().getLanguage());
       String strURL = "../common/emxTypeChooser.jsp?form=emxCreateForm&fieldNameActual=type&fieldNameDisplay=TypeDisplay&InclusionList="+inclusionList;
       StringBuffer returnString = new StringBuffer(512);
       returnString.append("<input type=\"text\" name=\"TypeDisplay\"");
       returnString.append(" size=\"20\" value=\"" +idefaultType+ "\"");
       returnString.append(" readonly=\"readonly\">");
       returnString.append("</input>");
       returnString.append("<input class=\"button\" type=\"button\"");
       returnString.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
       returnString.append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
       returnString.append(strURL);
       returnString.append("', 700, 500)\">");
       returnString.append("</input>");
       returnString.append("<input type=\"hidden\" name=\"type\" value=\""+ defaultType + "\"></input>");
       return returnString.toString();
    }
    /**
     * Method to display the revision field in the create mep webform
     * If the property key emxManufacturerEquivalentPart.MEP.allowCustomRevisions is false
     * then display revision field as read only
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap
     * @return a boolean to show the revision field .
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public String displayRevision(Context context, String[] args)
            throws Exception {
        StringBuffer outputStr = new StringBuffer(256);
        try {
            String revValue=getRevisionFieldValue(context);
               //Modified for IR072513V6R2012,  IR072515V6R2012
                outputStr
                        .append("<input type=\"text\" readonly=\"readonly\" name=\"Revision\" value =\""+XSSUtil.encodeForHTMLAttribute(context, revValue)+"\" size=\"20\"></input>");
                outputStr
                    .append("<input type=\"hidden\" name=\"rev\" value=\""+XSSUtil.encodeForHTMLAttribute(context,revValue)+"\"></input>");
        } catch (Exception e) {
            e.printStackTrace();
            throw (new FrameworkException(e));
        }
        return outputStr.toString();
    }
    /**
     * Method to display the customrevision field in the create mep webform
     * If the property key emxManufacturerEquivalentPart.MEP.allowCustomRevisions is true
     * custom revsion field as editable
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap
     * @return a boolean to show the revision field .
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public String displayCustomRevision(Context context, String[] args)
            throws Exception {
        StringBuffer outputStr = new StringBuffer(512);
        try {
            String custRevValue=getRevisionFieldValue(context);
                outputStr
			//Added IR-010681
            //      .append("<input type=\"text\" name=\"CustomRevision\" value =\""+custRevValue+"\" size=\"20\"></input>");
					.append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>")
					.append("<input type=\"text\" name=\"CustomRevision\" value =\""+XSSUtil.encodeForHTMLAttribute(context, custRevValue)+"\" size=\"20\" onChange=\"javascript:setMEPRevision();\"></input>")
					.append("<input type=\"hidden\" name=\"revision\" value=\""+XSSUtil.encodeForHTMLAttribute(context,custRevValue)+"\"></input>");
			//Ends IR-010681
            	outputStr
                    .append("<input type=\"hidden\" name=\"customrev\" value=\""+XSSUtil.encodeForHTMLAttribute(context,custRevValue)+"\"></input>");

        } catch (Exception e) {
            e.printStackTrace();
            throw (new FrameworkException(e));
        }
        return outputStr.toString();
    }
    /**
     * Access Method to display the revision field in the create mep webform
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap
     * @return a boolean to show the revision field .
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public Boolean showRevision(Context context, String[] args)
            throws Exception {
        try {
            String customRevision = FrameworkProperties
                    .getProperty(context, "emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            if (customRevision == null) {
                customRevision = "false";
            }
            if ("false".equals(customRevision)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw (new FrameworkException(e));
        }
    }
  /**
     * Access Method to display the custom revision field in the create mep webform
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap
     * @return a boolean to show the custom revision field .
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public Boolean showCustomRevision(Context context, String[] args)
            throws Exception {
        try {
            String customRevision = FrameworkProperties
            .getProperty(context, "emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            if (customRevision == null) {
                customRevision = "false";
            }
            if ("true".equals(customRevision)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw (new FrameworkException(e));
        }

}
    /**
     * <font color=maroon><b>Wrapper method</b></font>
     *
     * @mx.packedArgs HashMap { "id" : String;...}
     */

    public HashMap getRevisionValue(Context context, String args[])
            throws FrameworkException {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap returnValue = (HashMap) getRevisionValue(context,
                    programMap);
            return returnValue;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    /**
     * Method to get the revision values in the create mep webform based on the
     * unique identifier and custom revision keys
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap of orgid and locobject id
     * @return a HashMap
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public HashMap getRevisionValue(Context context, HashMap arguMap)
            throws Exception {

        HashMap hRevMap = new HashMap();
        try {
            String objId = (String) arguMap.get("orgId");
            String locObjId = (String) arguMap.get("locObjId");
            String attrOrgId = PropertyUtil.getSchemaProperty(context,
                    "attribute_OrganizationID");
            String attrCageCode = PropertyUtil.getSchemaProperty(context,
                    "attribute_CageCode");
            String attrPlantId = PropertyUtil.getSchemaProperty(context,
                    DomainSymbolicConstants.SYMBOLIC_attribute_PlantID);
            String customRevision = FrameworkProperties
                    .getProperty(context, "emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            String uniqueIdentifier = FrameworkProperties
                    .getProperty(context, "emxManufacturerEquivalentPart.MEP.UniquenessIdentifier");
            String CageCode = "";
            String CompId = "";
            String plantId = "";

            StringList objSelectList = new StringList(2);
            objSelectList.addElement("attribute[" + attrOrgId + "]");
            objSelectList.addElement("attribute[" + attrCageCode + "]");
            StringList selectList = new StringList(1);
            selectList.addElement("attribute[" + attrPlantId + "]");

            if (locObjId != null) {
                DomainObject locdomObj = DomainObject.newInstance(context,
                        locObjId);
                Map orgMap = locdomObj.getInfo(context, selectList);
                plantId = (String) orgMap.get("attribute[" + attrPlantId + "]");
            }
            if (uniqueIdentifier != null) {
                uniqueIdentifier = uniqueIdentifier.trim();
            }
            if (!"attribute_CageCode".equals(uniqueIdentifier)
                    && !"Policy".equals(uniqueIdentifier)) {
                uniqueIdentifier = "attribute_OrganizationID";
            }
            if (objId != null) {
                DomainObject dOmbj = DomainObject.newInstance(context, objId);
                Map orgMap = dOmbj.getInfo(context, objSelectList);
                CompId = (String) orgMap.get("attribute[" + attrOrgId + "]");
                CageCode = (String) orgMap.get("attribute[" + attrCageCode
                        + "]");
            }

            StringBuffer revSeqValue = new StringBuffer();
            String typePart = DomainConstants.TYPE_PART;
            PolicyList policyList = null;
            StringItr polItr = null;
            BusinessType partBusType = new BusinessType(typePart, new Vault(
                    Person.getPerson(context).getVaultName(context)));
            partBusType.open(context, false);
            policyList = partBusType.getPoliciesForPerson(context, false);
            partBusType.close(context);
            Policy policyObj = null;

            StringList polList = Part.getPoliciesforTypeClassification(context,
                    typePart, "Equivalent");

            polItr = new StringItr(polList);

            while (polItr.next()) {
                Policy partPolicy = new Policy(polItr.obj());
                for (int i = 0; i < policyList.size(); i++) {
                    policyObj = (Policy) policyList.elementAt(i);
                    String policyObjName = policyObj.getName().trim();
                    String partPolicyName = partPolicy.getName().trim();
                    if (policyObjName.equals(partPolicyName)) {
                        String firstRevSeq = partPolicy
                                .getFirstInSequence(context);
                        if (revSeqValue.length() > 0) {
                            revSeqValue.append(',');
                        }
                        revSeqValue.append(firstRevSeq);

                    }
                }
            }
            hRevMap.put("CompId", CompId);
            hRevMap.put("CageCode", CageCode);
            hRevMap.put("customRevision", customRevision);
            hRevMap.put("uniqueIdentifier", uniqueIdentifier);
            hRevMap.put("plantId", plantId);
            hRevMap.put("revSeqValue", revSeqValue);

        } catch (Exception e) {
            e.printStackTrace();
            throw (new FrameworkException(e));
        }
        return hRevMap;
    }
    /**
     * Method invokes the getRevisionValue to get the revision values to be
     * populated in the create MEP revision field
     * This is invoked for displayRevision method
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            contains a packed HashMap of orgid and locobject id
     * @return a HashMap
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009-1
     */
    public String getRevisionFieldValue(Context context)
    throws Exception {

        StringList selectList=new StringList(1);
        selectList.addElement(DomainObject.SELECT_ID);
        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person
        .getPerson(context);
        Company contextComp = contextPerson.getCompany(context);

        Map compMap = contextComp.getInfo(context, selectList);

        String defaultManufacturerId = (String) compMap
                .get(DomainObject.SELECT_ID);
        String revValue="";
        HashMap hargs=new HashMap();
        hargs.put("orgId",defaultManufacturerId);
        //hargs.put("orgId","");

        String[] objArgs=new String[]{defaultManufacturerId,""};
        HashMap hMap=getRevisionValue(context,hargs);
        String CompId=(String)hMap.get("CompId");
        String CageCode=(String)hMap.get("CageCode");
        String uniqueIdentifier=(String)hMap.get("uniqueIdentifier");
        String revSeq=((StringBuffer)hMap.get("revSeqValue")).toString();
        if( "attribute_OrganizationID".equals(uniqueIdentifier))
        {
            revValue=CompId;

        }
        else if("attribute_CageCode".equals(uniqueIdentifier))
        {
            revValue=CageCode;
        }
        else
        {

            String[] Idxvalues=StringUtils.split(revSeq, ",");
            revValue=Idxvalues[0];
        }
        return revValue;
    }

        /**
     * Range Program funtion to get the range values for Location Status Attribute
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     * @return a HashMap
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009
     */
 public Map getRangeValuesForLocationStatusAttribute(Context context, String[] args) throws Exception
    {
	  //Modified for IR-048513V6R2012x, IR-118107V6R2012x     
      HashMap rangeMap = new HashMap();
      matrix.db.AttributeType attribName = new matrix.db.AttributeType(DomainConstants.ATTRIBUTE_LOCATION_STATUS);
      attribName.open(context);
      List attributeRange = attribName.getChoices();
      rangeMap.put("field_choices" , attributeRange);
      return  rangeMap;
   }
    /**
     * Range Program funtion to get the range values for Location Preference Attribute
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     * @return a HashMap
     * @throws Exception
     *             if the operation fails.
     * @since V6R2009
     */
public Map getRangeValuesForLocationPreferenceAttribute(Context context, String[] args) throws Exception
    {
	  //Modified for IR-048513V6R2012x, IR-118107V6R2012x      
      HashMap rangeMap = new HashMap();
      //Modified for IR-048513V6R2012x, IR-118107V6R2012x 
      matrix.db.AttributeType attribName = new matrix.db.AttributeType(DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE);
      attribName.open(context);
      List attributeRange = attribName.getChoices();
      rangeMap.put("field_choices" , attributeRange);
      return  rangeMap;
   }
/**
 * excludeAlreadyConnectedObjects - returns the list of Object ids that are already connected
 * to parent Object.
 *
 * @param context
 *            the eMatrix <code>Context</code> object.
 * @param arguMap
 *            contains packed HashMap with the following entries:
	 *            objectId
 * @return StringList.
 * @throws FrameworkException
 *             if the operation fails.
 * @since MCC R208
 */
 @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
 public StringList excludeAlreadyConnectedObjects (Context context, String[] args)throws FrameworkException {
     try {
         StringList slExcludedIds = new StringList();
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         String strObjectId = (String)programMap.get("objectId");
         String strRelationship = (String)programMap.get("relation");

         if (strObjectId == null || "".equals(strObjectId)){
             throw new FrameworkException ("Does not has valid Obect for connection ");
         }
         else {
             DomainObject dmObject = newInstance (context, strObjectId);
             if ("relationship_ManufacturerEquivalent".equals(strRelationship)) {
                 slExcludedIds = dmObject.getInfoList(context, "from["+ DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT +"].to.id");
                 slExcludedIds.add(strObjectId);
             }         }
         return slExcludedIds;
     }
     catch (Exception e){
         throw new FrameworkException(e);
     }
 }

	 /**
	  * <font color=maroon><b>Wrapper method</b></font>
	  *
	  * Returns a vector of Selected Equivalent column values for Part equivalents (e.g. an MEP).
	  * If a part equivalent IS the Selected Equivalent (there can only be one if any) then it's
	  * column value will be "Yes", otherwise the value will be an empty string.
	  *
	  * @mx.packedArgs HashMap { "id" : String;....}
	  * @mx.packedReturn <code>Vector </code> column  values
	  */
	 public Vector displaySelectedEquivalent(Context context, String args[]) throws FrameworkException
	 {
	   try
	   {
	     HashMap programMap = (HashMap) JPO.unpackArgs(args);
	     Vector columnVals = displaySelectedEquivalent(context, programMap);
	     return columnVals;
	   }
	   catch (Exception e)
	   {
	     throw new FrameworkException(e);
	   }
	 }

	 /**
	  * Returns a vector of Selected Equivalent column values for Part equivalents (e.g. an MEP).
	  * If a part equivalent IS the Selected Equivalent (there can only be one if any) then it's
	  * column value will be "Yes", otherwise the value will be an empty string.
	  *
	  * @param context
	  *            the eMatrix <code>Context</code> object
	  * @param args
	  *            HashMap {objectList{ "id" : String;.} ColumnMap{settings}...}
	  * @returns vector column values
	  * @throws FrameworkException
	  *            if the operation fails
	  * @since MCC R209
	  */
	public Vector displaySelectedEquivalent(Context context, HashMap programMap) throws FrameworkException
	{
        String selRel = PropertyUtil.getSchemaProperty(context,"relationship_SelectedEquivalent");
	    Vector resultList = new Vector();
	    try
	    {
	        MapList objectList = (MapList) programMap.get("objectList");
	        HashMap paramMap = (HashMap) programMap.get("paramList");
	        String partId = (String) paramMap.get("objectId");

	        String isSelEquivLabel = EnoviaResourceBundle.getProperty(context ,
	                "emxManufacturerEquivalentPartStringResource",
	                context.getLocale(),"emxManufacturerEquivalentPart.Part.IsSelectedEquivalent");

	        DomainObject domObj = newInstance(context, partId);

	        String query = "from[" + selRel + "].to.id";

	        // Get the selected equivalent for this part, if any.
		    String selEquiv = domObj.getInfo(context, query);
	        for(int i=0; i< objectList.size(); i++)
	        {
	            Map partMap = (Map) objectList.get(i);
	            String equivObjId = (String)partMap.get(DomainObject.SELECT_ID);
	            resultList.add(equivObjId.equals(selEquiv) ? isSelEquivLabel : "");
	        }
	   }
	   catch (Exception e)
	   {
	       e.printStackTrace();
	       throw new FrameworkException();
	   }
	   return resultList;
	}
     /**
     * Checks for from connect access of Enterprise Part to display Create Placeholder MFG and Addexisting commands in the Equivalents Actions Menu.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @Added for IR-026971V6R2011.
     *
     */
 public Boolean checkMPNCreateAccess(Context context,String[] args)throws Exception
 {
     Boolean hasCreateAccess = Boolean.FALSE;
     HashMap paraMap = (HashMap)JPO.unpackArgs(args);
     String sCompEngrRole = PropertyUtil.getSchemaProperty(context,"role_ComponentEngineer");
     try {
         String objId = (String)paraMap.get("objectId");
         String obsoleteState = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_EC_PART, "state_Obsolete");
         //Added for 	IR-072745V6R2012 starts
         String approvedState = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_EC_PART, "state_Approved");
         String releaseState  = PropertyUtil.getSchemaProperty(context, "policy",DomainConstants.POLICY_EC_PART, "state_Release");
         //Added for 	IR-072745V6R2012 ends
         DomainObject dom = DomainObject.newInstance(context,objId);
         StringList selectList = new StringList(DomainConstants.SELECT_CURRENT);
         selectList.addElement(DomainConstants.SELECT_OWNER);
         selectList.addElement("current.access[fromconnect]");
         Map map = dom.getInfo(context,selectList);

         if (!obsoleteState.equals((String)map.get(DomainConstants.SELECT_CURRENT)))
             {
        	 if (context.isAssigned(sCompEngrRole)) {
        		 return Boolean.TRUE;
        	 }
             //checks  from connect access for the user  in all states except Obsolete
        //Modified for IR-072745V6R2012 
   if (((!approvedState.equals((String)map.get(DomainConstants.SELECT_CURRENT)))&&(!releaseState.equals((String)map.get(DomainConstants.SELECT_CURRENT))))&&(("User Agent".equals((String)map.get(DomainConstants.SELECT_OWNER)))||("TRUE".equals((String)map.get("current.access[fromconnect]")))))
                  hasCreateAccess = Boolean.TRUE;
         }
    }catch (Exception e) {
       throw new Exception(e.toString());
    }
    return hasCreateAccess;
 }

 	public static MapList ShowMEPIcon (Context context, String[] args) throws Exception
 	{
 		MapList iconList = new MapList();
 		try{
 			// unpack args array to get input map
 			Map programMap = (Map) JPO.unpackArgs(args);
 			// get object list
 			MapList ObjectList= (MapList) programMap.get("objectList");

 			String bArr [] = new String[ObjectList.size()];
 			StringList bSel = new StringList();
 			bSel.add("policy.property[PolicyClassification].value");
 			bSel.add(DomainConstants.SELECT_TYPE);

 			// Get the object elements - OIDs and RELIDs - if required
 			for (int i = 0; i < ObjectList.size(); i++)
 			{
 			// Get Business object Id
 			bArr [i] =(String)((Map) ObjectList.get(i)).get("id");
 			}

 			// Get the required information for the objects.
 			BusinessObjectWithSelectList bwsl =BusinessObject.getSelectBusinessObjectData (context, bArr, bSel);

 			for (int i = 0; i < ObjectList.size(); i++)
 			{
 				String currentObjectid =(String)((Map) ObjectList.get(i)).get("id");
 				// get the current state value
 				String sPolicyClassification = bwsl.getElement(i).getSelectData("policy.property[PolicyClassification].value");
 				String type = bwsl.getElement(i).getSelectData("type");

 				HashMap retMap = new HashMap();
 				// Based on the object state add the required icon
 				//Pass the required information to this method and get the required icon //name.
               	String objectIcon = getIcons(context,type,sPolicyClassification);
               	retMap.put(currentObjectid, objectIcon);
 			    // Size of the iconList should be as same as ObjectList.
               	iconList.add(retMap);
 			}
 		}
 		catch (FrameworkException e){
 			throw new FrameworkException (e.toString());
 		}
 		return iconList;
 	}

 	public static String getIcons(Context context, String typeName, String sPolicyClassification) {
        if (sPolicyClassification.equalsIgnoreCase("Equivalent"))
            return "iconSmallMEP.gif";
        else {
            String typeIcon = UINavigatorUtil.getTypeIconProperty(context,typeName);
            return typeIcon;
        }
 	}
 	
 	
    public String getMFGLocationSearchQuery(Context context, String[] args) throws Exception {
        //unpacking the Arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        Map fieldValuesMap = (HashMap)programMap.get("fieldValues");
        String ManufacturerId = (String)fieldValuesMap.get("Manufacturer");
     
        return "TYPES=type_Location:CURRENT=policy_Location.state_Active:MANUFACTURER_ID="+ManufacturerId;
        	
     }
 	
    
    public String showDefaultManufacturer(Context context, String[] args)
    throws Exception {
    	String defaultManufacturer 	 = "";

    	try{
    		StringList selectList = new StringList(2);
    		selectList.addElement(DomainObject.SELECT_NAME);
    		selectList.addElement(DomainObject.SELECT_ID);
	
    		com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person.getPerson(context);
    		
    		Company contextComp 	= contextPerson.getCompany(context);
    		Map compMap         	= contextComp.getInfo(context, selectList);

    		defaultManufacturer 	= (String) compMap.get(DomainObject.SELECT_NAME);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	System.out.println("defaultManufacturer ="+defaultManufacturer);
    	return defaultManufacturer;
    }
    public static MapList ShowEquivalentsIcon (Context context, String[] args) throws Exception
 	{
 		MapList iconList = new MapList();
 		try{
 			Map programMap = (Map) JPO.unpackArgs(args);
 			MapList ObjectList= (MapList) programMap.get("objectList");
 			HashMap retMap = new HashMap();
 			
 			for (int i = 0; i < ObjectList.size(); i++)
 			{
 				String currentObjectid =(String)((Map) ObjectList.get(i)).get("id");
               	retMap.put(currentObjectid, "iconSmallMEP.gif");
 			    // Size of the iconList should be as same as ObjectList.
               	iconList.add(retMap);
 			}
 		}
 		catch (FrameworkException e){
 			throw new FrameworkException (e.toString());
 		}
 		return iconList;
 	}
    
    /** This methods gets States of MEP for filter drop down.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return HashMap
	 * @throws Exception if any exception occurs.
	 */
    public HashMap getMEPStateFilter(Context context, String[] args) 
    throws Exception 
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String languageStr = (String) requestMap.get("languageStr");
		
		        String sAllStates  =  EnoviaResourceBundle.getProperty(context ,"emxManufacturerEquivalentPartStringResource",context.getLocale(),"emxManufacturerEquivalentPart.Common.DisplayAllStates");
		HashMap viewMap = new HashMap();
        StringList strListStateDisplay = new StringList();
        StringList strListStateValue = new StringList();
        StringList strListEquivalentPolicies = Part.getEquivalentPartPolicies(context);

        if(strListEquivalentPolicies != null && strListEquivalentPolicies.size() > 0)
        {
            Policy polObj                   = null;
            int stateListSize               = 0;
            StateRequirementList stReqLst   = null;
            StateRequirement stReq          = null;
            String strPolicyName            = "";
            String strStateName             = "";

            for(int i = 0 ; i < strListEquivalentPolicies.size(); i++)
            {
                strPolicyName   = (String)strListEquivalentPolicies.get(i);
                polObj          = new Policy(strPolicyName);
                stReqLst        = polObj.getStateRequirements(context);
                stateListSize   = (stReqLst != null)? stReqLst.size() : 0 ;
                for(int j = 0 ; j < stateListSize ; j++)
                {
                    stReq   = (StateRequirement) stReqLst.get(j);
                    strStateName = stReq.getName();
                    if(!strListStateValue.contains(strStateName))
                    {
                    	if(DomainConstants.STATE_PART_RELEASE.equals(strStateName)){
                    		strListStateValue.add(0,strStateName);
                            strListStateDisplay.add(0,i18nNow.getStateI18NString(strPolicyName,strStateName, languageStr));
                    	}
                    	else{
	                        strListStateValue.add(strStateName);
	                        strListStateDisplay.add(i18nNow.getStateI18NString(strPolicyName,strStateName, languageStr));
                    	}
                    }
                }
            }
        }
        strListStateValue.insertElementAt("*",1);
        strListStateDisplay.insertElementAt(sAllStates,1);
                
        viewMap.put("field_choices", strListStateValue);
        viewMap.put("field_display_choices", strListStateDisplay);
        
        return viewMap;
    }
    
    /** This methods gets Location Status values for filter drop down with ALL Status option.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return HashMap
	 * @throws Exception if any exception occurs.
	 */
    public HashMap getMEPStatusFilter(Context context, String[] args) 
    throws Exception 
    {
    	HashMap viewMap = getMEPLocationStatus(context, args);
    	String sAllStatus = EnoviaResourceBundle.getProperty(context ,"emxManufacturerEquivalentPartStringResource",context.getLocale(),"emxManufacturerEquivalentPart.Common.DisplayAllStatus");

    	((StringList)viewMap.get("field_choices")).insertElementAt("*",0);
    	((StringList)viewMap.get("field_display_choices")).insertElementAt(sAllStatus,0);
        
    	return viewMap;
    }
    
    /** This methods gets Location Status attribute values for filter drop down.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return HashMap
	 * @throws Exception if any exception occurs.
	 */
    public HashMap getMEPLocationStatus(Context context, String[] args) 
    throws Exception 
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String languageStr = (String) requestMap.get("languageStr");
		
		HashMap viewMap = new HashMap();
    	String attrLocStatus =  PropertyUtil.getSchemaProperty(context, "attribute_LocationStatus");
        StringList strListLocStatus = FrameworkUtil.getRanges(context, attrLocStatus );
        int size = strListLocStatus.size();
        StringList strListLocStatusDisplay = new StringList(size);
        int j = 0;
        while( j < size)
        {
            String location = (String)strListLocStatus.get(j);
            strListLocStatusDisplay.add(i18nNow.getRangeI18NString(attrLocStatus , location, languageStr));
            j++;
        }
        
        viewMap.put("field_choices", strListLocStatus);
        viewMap.put("field_display_choices", strListLocStatusDisplay);
        
    	return viewMap;
    }
    
    /** This methods gets Location preference values for filter drop down with ALL MEP option.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return HashMap
	 * @throws Exception if any exception occurs.
	 */
    public HashMap getMEPPreferenceFilter(Context context, String[] args) 
    throws Exception
    {
		HashMap viewMap = getMEPLocationPreference(context, args);
		String sAllMEPs = EnoviaResourceBundle.getProperty(context,"emxManufacturerEquivalentPartStringResource" ,context.getLocale(),"emxManufacturerEquivalentPart.Part.DisplayAllMEPs");

		((StringList)viewMap.get("field_choices")).insertElementAt("*",0);
		((StringList)viewMap.get("field_display_choices")).insertElementAt(sAllMEPs,0);
		
		return viewMap;
    }
    
    /** This methods gets Location preference value list for filter drop down.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return HashMap
	 * @throws Exception if any exception occurs.
	 */
    public HashMap getMEPLocationPreference(Context context, String[] args) 
    throws Exception 
    {    	
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String languageStr = (String) requestMap.get("languageStr");
		
		HashMap viewMap = new HashMap();
    	String attrLocPref =  PropertyUtil.getSchemaProperty(context, "attribute_LocationPreference");
        StringList strListLocPref = FrameworkUtil.getRanges(context, attrLocPref );
        int presize = strListLocPref.size();
        StringList strListLocPrefDisplay = new StringList(presize);
        int k = 0;
        while( k < presize)
        {
            String preference = (String)strListLocPref.get(k);
            strListLocPrefDisplay.add(i18nNow.getRangeI18NString(attrLocPref , preference,languageStr));
            k++;
        }
                
        viewMap.put("field_choices", strListLocPref);
        viewMap.put("field_display_choices", strListLocPrefDisplay);
        
    	return viewMap;
    }
    
    /** This methods gets all the parent parts connected to it depending upon selected options.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return MapList
	 * @throws Exception if any exception occurs.
	 */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getFilteredMEP(Context context, String[] args) throws Exception {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);

    	MapList filteredObjPageList = new MapList();;
    	StringBuffer sbfWhereClause = new StringBuffer(32);

    	try{
    		String locationId = (String)programMap.get("MEPManufacturer_actualValue");
    		if(UIUtil.isNullOrEmpty(locationId))
    			locationId = (String)programMap.get("MEPManufacturer");
    		String selectedName = (String)programMap.get("MEPName");
    		String selectedType =  (String)programMap.get("MEPType");
    		String selectedDesc = (String)programMap.get("MEPDescription");
    		String selectedStatus = (String)programMap.get("MEPStatus");
    		String selectedPref = (String)programMap.get("MEPPreference");
    		String selectedState = (String)programMap.get("MEPState");
    		String queryLimit = (String)programMap.get("QueryLimit");

    		if(locationId == null || "null".equals(locationId) || "".equals(locationId)) {
    			locationId = "*";
    		}
    		if(selectedStatus == null || "null".equals(selectedStatus) || "".equals(selectedStatus)) {
    			selectedStatus = "*";
    		}

    		if(selectedPref  == null || "null".equals(selectedPref ) || "".equals(selectedPref )) {
    			selectedPref  = "*";
    		}

    		if(selectedState == null || "null".equals(selectedState) || "".equals(selectedState)) {
    			selectedState = "*";
    		}

    		if(selectedName == null || "null".equals(selectedName) || "".equals(selectedName)) {
    			selectedName = "*";
    		}

    		String sPartType =  PropertyUtil.getSchemaProperty(context,"type_Part");
    		if(selectedType == null 
    				|| "null".equals(selectedType) 
    				|| "".equals(selectedType) 
    				|| "*".equals(selectedType)
    				|| i18nNow.getTypeI18NString(sPartType, context.getSession().getLanguage()).equals(selectedType)) 
    		{
    			selectedType = sPartType;
    		}

    		if(selectedDesc == null || "null".equals(selectedDesc) || "".equals(selectedDesc) ) {
    			selectedDesc = "*";
    		}

    		com.matrixone.apps.manufacturerequivalentpart.Part partObj = new com.matrixone.apps.manufacturerequivalentpart.Part();
    		StringList selectStmts = new StringList();
    		selectStmts.add(DomainConstants.SELECT_ID);
    		selectStmts.add(DomainConstants.SELECT_TYPE);
    		selectStmts.add(DomainConstants.SELECT_NAME);
    		selectStmts.add(DomainConstants.SELECT_ALLOCATION_RESPONSIBILITY_REL_ID);
    		
    		String relManufacturingLocation = PropertyUtil.getSchemaProperty(
                     context, "relationship_ManufacturingLocation");
            selectStmts.add("from["
                     + relManufacturingLocation + "].to.name");
			String manEquAloResNameSel = "to["
				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
			
			String manEquAloResIdSel = "to["
				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";
			String aloResNameSel = "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
			String aloResIdSel = "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";
			
			String manEquAloResStaSel = "to["
				+ RELATIONSHIP_MANUFACTURER_EQUIVALENT 
				+ "].from.to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY
				+ "].attribute[" 
				+ ATTRIBUTE_LOCATION_STATUS
				+ "].value";

			// Location Status select via the relationship route
			// Part --> Allocation Responsibility
			String aloResStaSel = "to["
				+ RELATIONSHIP_ALLOCATION_RESPONSIBILITY
				+ "].attribute[" 
				+ ATTRIBUTE_LOCATION_STATUS
				+ "].value";
			
			// Location Preference select via the relationship route
            // Part --> Manufacture Equivalent --> Allocation Responsibility
            String manEquAloResPreSel = "to["
                    + RELATIONSHIP_MANUFACTURER_EQUIVALENT 
                    + "].from.to["
                    + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                    + "].attribute[" 
                    + ATTRIBUTE_LOCATION_PREFERENCE
                    + "].value";

            // Location Preference select via the relationship route
            // Part --> Allocation Responsibility
            String aloResPreSel = "to["
                    + RELATIONSHIP_ALLOCATION_RESPONSIBILITY
                    + "].attribute[" 
                    + ATTRIBUTE_LOCATION_PREFERENCE
                    + "].value";			
			
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResNameSel);
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResIdSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResNameSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResIdSel);
			
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResStaSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResStaSel);
			
			DomainObject.MULTI_VALUE_LIST.add(manEquAloResPreSel);
			DomainObject.MULTI_VALUE_LIST.add(aloResPreSel);
            
			selectStmts.addElement(manEquAloResNameSel);
			selectStmts.addElement(manEquAloResIdSel);
			selectStmts.addElement(aloResNameSel);
			selectStmts.addElement(aloResIdSel);
			
			selectStmts.addElement(manEquAloResStaSel);
			selectStmts.addElement(aloResStaSel);
			
			selectStmts.addElement(manEquAloResPreSel);
			selectStmts.addElement(aloResPreSel);
    		
    		//352550
    		//352550 - instead of passing the vault as * we can pass the context vault

    		/* Modified by Prasanna L for fixing IR-038267V6R2011. Commented out the below vault definition
			and retrieving the vault definition from the company of the context user. 
    		 */

    		//String vault=DomainConstants.QUERY_WILDCARD;
    		//String vault=context.getVault().getName();

    		Person person = Person.getPerson(context);
    		String vault = person.getCompany(context).getAllVaults(context,false);
    		/* End of code change for IR-038267V6R2011 */

    		//352550
    		StringBuffer whereCls = new StringBuffer(128);
    		boolean blnCheck = false;

    		if(!"*".equals(locationId)) 
    		{
    			blnCheck = true;

    			/* Modified by Prasanna L for fixing IR-038267V6R2011
	             building the where condition if there are multiple vaults associated with the company.  
    			 */

    			java.util.List strVaultList	= FrameworkUtil.split(vault, ",");
    			Iterator itrVlt				= strVaultList.iterator(); 
    			String sVaultString           = "(";

    			while (itrVlt.hasNext())
    			{
    				String strVaultString = (String) itrVlt.next(); 
    				if ("(".equals(sVaultString)) {
    					sVaultString=sVaultString+DomainConstants.SELECT_VAULT+" == \""+strVaultString+"\"";
    				} else {
    					sVaultString = sVaultString + " || "+DomainConstants.SELECT_VAULT+"== \""+strVaultString+"\"";
    				}
    			}
    			sVaultString = sVaultString + ")";

    			//IR-038267V6R2011 changes : commented the below line	
    			sbfWhereClause.append(sVaultString);

    			/* End of code change for IR-038267V6R2011 */
    			//352550
    		}

    		if(!"*".equals(selectedState)) 
    		{
    			if(whereCls.length() > 0) {
    				whereCls.append(" && "); 
    			}
    			whereCls.append(DomainConstants.SELECT_CURRENT+" == '"+selectedState+"' ");
    			//352550
    			if(blnCheck) {
    				sbfWhereClause.append(" && ").append('(').append(DomainConstants.SELECT_CURRENT).append(" == \"")
    				.append(selectedState).append("\")");
    			}
    			//352550
    		}

    		if( !"*".equals(selectedStatus)) 
    		{
    			//Location Status select via the relationship route
    			//Part --> Manufacture Equivalent --> Allocation Responsibility
    			String manEquAloResStaSelect = "to[" + DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to["+ DomainConstants.RELATIONSHIP_ALLOCATION_RESPONSIBILITY +"].attribute[" + DomainConstants.ATTRIBUTE_LOCATION_STATUS + "]";
    			//Location Status select via the relationship route
    			//Part --> Allocation Responsibility
    			String aloResStaSelect = "to[" + DomainConstants.RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].attribute[" + DomainConstants.ATTRIBUTE_LOCATION_STATUS + "]";

    			if(whereCls.length() > 0) {
    				whereCls.append(" && ");
    			}
    			//352550
    			if(blnCheck) {
    				sbfWhereClause.append(" && ").append("  (").append(manEquAloResStaSelect).append(" == \"").append(selectedStatus)
    				.append("\" || ").append(aloResStaSelect).append(" == \"").append(selectedStatus).append("\")");
    			}
    			//352550
    			whereCls.append(" ("+manEquAloResStaSelect+" == '"+selectedStatus+"' || "+aloResStaSelect+" == '"+selectedStatus+"')");
    		}

    		if( !"*".equals(selectedPref)) 
    		{
    			//Location Preference select via the relationship route
    			//Part --> Manufacture Equivalent --> Allocation Responsibility
    			String manEquAloResPreSelect = "to[" + DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" +  DomainConstants.RELATIONSHIP_ALLOCATION_RESPONSIBILITY +"].attribute[" +  DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE + "]";
    			//Location Preference select via the relationship route
    			//Part --> Allocation Responsibility
    			String aloResPreSelect = "to[" + DomainConstants.RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].attribute[" + DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE + "]";

    			if(whereCls.length() > 0) {
    				whereCls.append(" && "); 
    			}
    			//352550
    			if(blnCheck) {
    				sbfWhereClause.append(" && ").append(" (").append(manEquAloResPreSelect).append(" == \"").append(selectedPref)
    				.append("\" || ").append(aloResPreSelect).append(" == \"").append(selectedPref).append("\")");
    			}
    			//352550
    			whereCls.append(" ("+manEquAloResPreSelect+" == '"+selectedPref+"' || "+aloResPreSelect+" == '"+selectedPref+"')");
    		}

    		if(!"*".equals(selectedName)) 
    		{
    			if(whereCls.length() > 0) {
    				whereCls.append(" && ");
    			}
    			//352550
    			if(blnCheck) {
    				sbfWhereClause.append(" && ").append('(').append(DomainConstants.SELECT_NAME).append(" ~= \"")
    				.append(selectedName).append("\")");
    			}
    			//352550
    			whereCls.append(DomainConstants.SELECT_NAME+" ~= '"+selectedName+"' ");
    		}

    		if(!"*".equals(selectedType)) 
    		{
    			if(whereCls.length() > 0) {
    				whereCls.append(" && "); 
    			}
    			String allTypes = MqlUtil.mqlCommand(context,"print type $1 select $2 dump $3",selectedType,"derivative",",");
    			if(allTypes.length() > 0)
    			{
    				allTypes += "," + selectedType;
    			}
    			else
    			{
    				allTypes = selectedType;
    			}
    			whereCls.append(" ( " + DomainConstants.SELECT_TYPE+" matchlist \""+allTypes+"\" \",\" ) ");
    			//352550
    			if(blnCheck) {
    				sbfWhereClause.append(" && ").append(" ( " + DomainConstants.SELECT_TYPE+".kindof["+selectedType+"] )");
    			}
    			//352550
    		}

    		if(!"*".equals(selectedDesc)) 
    		{
    			if(whereCls.length() > 0) {
    				whereCls.append(" && ");
    			}
    			//352550
    			if(blnCheck) {
    				sbfWhereClause.append(" && ").append('(').append(DomainConstants.SELECT_DESCRIPTION).append(" ~= \"")
    				.append(selectedDesc).append("\")");
    			}
    			//352550
    			whereCls.append(DomainConstants.SELECT_DESCRIPTION+" ~= '"+selectedDesc+"' ");
    		}

    		//352550 - Starts
    		String sResult;
    		Map mapObject;
    		Iterator itr;
    		java.util.List strlPartList = new StringList();
    		java.util.List strlObjList;
    		StringBuffer sbfQuery = new StringBuffer(32);
    		if (blnCheck)
    		{
    			sbfQuery.append("expand bus ").append("$1").append(" rel \"").append("$2")
    			.append("\" select $3 $4 ").append("where '").append("$5")
    			.append("' dump $6 recordsep $7");
    			sResult = MqlUtil.mqlCommand(context, sbfQuery.toString(),locationId,DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,"bus","id",sbfWhereClause.toString(),"|","~");

    			strlObjList = FrameworkUtil.split(sResult, "|", "~");
    			itr = strlObjList.iterator(); 
    			while (itr.hasNext())
    			{
    				strlPartList = (java.util.List) itr.next(); 
    				mapObject = new HashMap();
    				mapObject.put(DomainConstants.SELECT_ID, (String)strlPartList.get(6));
    				mapObject.put(DomainConstants.SELECT_NAME, (String)strlPartList.get(4));
    				mapObject.put(DomainConstants.SELECT_TYPE, (String)strlPartList.get(3));
    				filteredObjPageList.add(mapObject);
    			}    

    		}
    		else
    		{  
    			filteredObjPageList = partObj.getManufacturerEquivalentsWithLimit(context,selectStmts,vault,whereCls.toString(),Integer.parseInt(queryLimit));
    		}
    		//352550 - Ends
    		
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResNameSel);
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResIdSel);
			DomainObject.MULTI_VALUE_LIST.remove(aloResNameSel);
			DomainObject.MULTI_VALUE_LIST.remove(aloResIdSel);
			
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResStaSel);
			DomainObject.MULTI_VALUE_LIST.remove(aloResStaSel);
			
			DomainObject.MULTI_VALUE_LIST.remove(manEquAloResPreSel);
            DomainObject.MULTI_VALUE_LIST.remove(aloResPreSel);
    	} catch (Exception ex) 
    	{        
    		ex.printStackTrace();
    		throw new Exception(ex.getMessage());
    	} 

    	return filteredObjPageList;
    }
    
    /** Adds the settings to refresh the main page from location edit window.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return Map
	 * @throws Exception if any exception occurs.
	 */	
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map performLocationRefresh(Context context, String[] args) throws Exception {
    	HashMap<Object,Object> retMap = new HashMap<Object,Object>();
    	
    	StringBuffer returnMsgBuffer  = new StringBuffer(1024);
		returnMsgBuffer.append("{ main:function() { ");
    	returnMsgBuffer.append("var cached = emxUICore.selectNodes(oXML,\"/mxRoot/rows//r[(@status='changed')]\"); ");
    	returnMsgBuffer.append(" for(var i = 0; i < cached.length; i++){      " );
    	returnMsgBuffer.append("cached[i].removeAttribute(\"status\");"); 
    	returnMsgBuffer.append("for(k=0;k<cached[i].childNodes.length;k++){");
    	returnMsgBuffer.append("if(cached[i].childNodes[k].tagName == \"c\"){");
    	returnMsgBuffer.append("cached[i].childNodes[k].setAttribute(\"edited\",\"false\");   }");
    			
    	returnMsgBuffer.append("} }postDataXML.loadXML(\"<mxRoot/>\"); RefreshView(); ");   			
    	returnMsgBuffer.append(" getTopWindow().findFrame(getTopWindow().getWindowOpener(), \"listHidden\").parent.emxEditableTable.refreshStructureWithOutSort();");
    	returnMsgBuffer.append("}}");
    	
    	retMap.put("Action", "execScript");
    	retMap.put("Message", returnMsgBuffer.toString());

    	return retMap;
    }
    
    public String getMEPLabel(Context context, String[] args) throws Exception{
    	String strLanguage = context.getSession().getLanguage();
    	
    	String sMessage =  i18nNow.getI18nString("emxManufacturerEquivalentPart.Slidein.Message","emxManufacturerEquivalentPartStringResource", strLanguage);
    	
    	String initialQueryLimit =  FrameworkProperties.getProperty(context,
        "emxManufacturerEquivalentPart.MEPInitialLoad.QueryLimit");
    	int maxQeryLimit = 32000;
    	int initialLimit = Integer.parseInt(initialQueryLimit);
      	
    	if(initialLimit > maxQeryLimit)
		initialQueryLimit = Integer.toString(maxQeryLimit);
	
    	sMessage += " " + initialQueryLimit;
    	return "<label for=\"MEP\"><i><font size = \"1\">"+sMessage+"</font></i></label>";
    }
    public boolean showMEPLabel(Context context, String[] args) throws Exception{
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String sInitialLoad = (String)programMap.get("initialLoad");

    	return("true".equals(sInitialLoad));
    }
public static String getInitialLoadQueryLimit(Context context, String args[]) throws FrameworkException{
    	
    	String sInitialQueryLimit = FrameworkProperties.getProperty(context, "emxManufacturerEquivalentPart.MEPInitialLoad.QueryLimit");
    	int maxQeryLimit = 32000;
    	int initialLimit = Integer.parseInt(sInitialQueryLimit);
        if (sInitialQueryLimit != null && !"null".equals(sInitialQueryLimit) && !"".equals(sInitialQueryLimit)){
        	
        	if(initialLimit > maxQeryLimit)
        		sInitialQueryLimit = Integer.toString(maxQeryLimit);
              return sInitialQueryLimit;
        }
        else
              return "";
    }
}


