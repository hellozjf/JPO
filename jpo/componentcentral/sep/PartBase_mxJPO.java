package jpo.componentcentral.sep;
// (c) Dassault Systemes, 1993-2016.  All rights reserved.

import matrix.db.*;
import matrix.util.*;

import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.manufacturerequivalentpart.Part;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.componentcentral.CPCConstants;
import com.matrixone.apps.componentcentral.CPCPart;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.framework.ui.UIForm;

/**
 *
 * The <code>Part</code> class in ComponentCentral...
 *
 *
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
        DebugUtil.debug("jpo.sep PartBase_mxJPO");
    }

    protected final String KEY_ALLOCATION_REL_ID = "AllocRespRelId";

    /** The String which is used to display content as label. */
    protected final String KEY_LABEL_LOC_EQUIV = "labelLocEquiv";

    protected MapList mepIdsforDisplay=new MapList();

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
    public MapList getSEP(Context context,
            String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");

        MapList listCorpMEPs = new MapList();

        try {
            DomainObject partObj = DomainObject.newInstance(context, objectId);

            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);

            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

            // fetching list of related SEPs
            DebugUtil.debug("Trying to retrieve the relation "+CPCConstants.RELATIONSHIP_SEP) ;
            listCorpMEPs = partObj.getRelatedObjects(context,
                    CPCConstants.RELATIONSHIP_SEP, // relationship
                    CPCConstants.TYPE_SEP, // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    null, // object where clause
                    null); // relationship where clause

        } catch (FrameworkException Ex) {
            throw Ex;
        }

        return listCorpMEPs;
    }


    /**
     * Gets the Manufacturer Equivalent Parts attached to an Supplier Equivalent Part.
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
     * CPC
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEnterpriseManufacturerEquivalents(Context context,
            String[] args) throws Exception {

		ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        DebugUtil.debug("Inside the JPO:"+paramMap);
        String objectId = (String) paramMap.get("objectId");
        String isMPN = (String) paramMap.get("isMPN");
        DebugUtil.debug("Inside the JPO:fromSEP"+objectId);
        MapList equivList = new MapList();
		Map tempMap = null;
		Vector vecMepId = new Vector();
		HashMap mapMepId = new HashMap();
		Vector vecRelId = new Vector();
        MapList listCorpMEPs = new MapList();
        MapList listLocEquivMEPs = new MapList();
        Map mapLocIds               = new HashMap();
        String sFromRelId           = "from.relationship["+ RELATIONSHIP_LOCATION_EQUIVALENT +"].id";

        try {
            DomainObject partObj = DomainObject.newInstance(context, objectId);

            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);
            StringList selectRelStmts = new StringList(2);
            selectRelStmts.addElement(sFromRelId);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
            StringBuffer typePattern = new StringBuffer(TYPE_PART);
            if (isMPN == null || isMPN.equalsIgnoreCase("True")) {
                typePattern.append(",");
                typePattern.append(TYPE_MPN);
            }
            StringBuffer sbTypePattern = new StringBuffer(typePattern.toString());
            sbTypePattern.append(",");
            sbTypePattern.append(TYPE_LOCATION_EQUIVALENT_OBJECT);
			String busWhere="(policy == \""+DomainConstants.POLICY_MANUFACTURER_EQUIVALENT +"\")";
            // fetching list of related MEPs via Supplier Equivalent
            if(partObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(DomainConstants.TYPE_PART)||
              (partObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(CPCConstants.TYPE_SUPPLIER_EQUIVALENT_PART))){
				DebugUtil.debug("Inside the JPO: "+selectStmts);

            // fetching list of related MEPs via location Equivalent Object
            listLocEquivMEPs = partObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_LOCATION_EQUIVALENT+","+DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT, // relationship pattern
                    sbTypePattern.toString(), // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 2, // recursion level
                    null, // object where clause
                    null); // relationship where clause

            	listCorpMEPs = partObj.getRelatedObjects(context,
                    CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT +","+DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,// relationship
                    // pattern
                    TYPE_PART, // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    busWhere, // object where clause
                    null); // relationship where clause
				}
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
            DebugUtil.debug("Inside the JPO:listCorpMEPs.size() "+listCorpMEPs.size());
            DebugUtil.debug("Inside the JPO:listLocEquivMEPs.size() "+listLocEquivMEPs.size());
            for (int i = 0; i < listCorpMEPs.size(); i++) {
                tempMap = (Map) listCorpMEPs.get(i);
                vecMepId.addElement(tempMap.get(SELECT_ID));
                mapMepId.put(tempMap.get(SELECT_ID), tempMap);
                vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
                vecRelId.addElement(tempMap.get(SELECT_ID));
            }// end of for (listCorpMEPs)
            for (int k = 0; k < vecMepId.size(); k++) {
                Map resultMap = (Map) mapMepId.get((String) vecMepId
                        .elementAt(k));
                //DebugUtil.debug("Inside the JPO:resultMap "+resultMap);
                resultMap.remove("level");// need to be removed , else show
                // message as - the level sequence
                // may not be as expected..
                String sLocEquiRelId = (String) mapLocIds.get(vecMepId.elementAt(k));
                if (sLocEquiRelId != null && !"".equals(sLocEquiRelId)) {
                    resultMap.put(SELECT_RELATIONSHIP_ID, sLocEquiRelId);
                }
                //IR-010504 - Ends
                equivList.add(resultMap);
            }
        } catch (FrameworkException Ex) {
			Ex.printStackTrace();
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
                    String displayValue = "&nbsp;";

                    String sTypeIcon = Part.getTypeIconProperty(context, type);
                    String imgSrc = "<img src='images/" + sTypeIcon
                            + "' border='0'>";

                    if (type.equals(TYPE_MPN)) {
                        String showBlankName = FrameworkProperties
                                .getProperty("emxManufacturerEquivalentPart.EngrPlaceholderMEP.ShowBlankName");
                        if ("false".equalsIgnoreCase(showBlankName)) {
                            displayValue = i18nNow
                                    .getI18nString(
                                            "emxManufacturerEquivalentPart.EngrPlaceholderMEP.DefaultName",
                                            "emxManufacturerEquivalentPartStringResource",
                                            context.getSession().getLanguage());

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
                        output.append(imgSrc + "&nbsp;" + displayValue);
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
                        output.append((String) dataMap.get(SELECT_ID));
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
                            output.append(XSSUtil.encodeForHTML(context,displayValue));//QBQ changed to support spl char in HTML tag
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
        Boolean isColumnVisible = new Boolean(false);
        try {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            String launched = (String) paramMap.get("launched");
            String portalMode = (String) paramMap.get("portalMode");

            if ((launched != null && launched.equalsIgnoreCase("true"))
                    && (portalMode != null && portalMode
                            .equalsIgnoreCase("false"))) {
                isColumnVisible = new Boolean(true);
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

                    StringBuffer output = new StringBuffer(" ");

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
                            output.append(locName);
                        }
                        // do not show hyperlinks if it is a printer friendly or
                        // excel export page
                        // length will be >0 when format is HTML, ExcelHTML, CSV
                        // or TXT
                        else if (null != reportFormat
                                && !reportFormat.equals("null")
                                && (reportFormat.length() > 0)) {
                            output.append(locName);
                        } else {
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + linkFile + "?emxSuiteDirectory=");
                            output.append(suiteDir);
                            output.append("&suiteKey=");
                            output.append(suiteKey);
                            output.append("&objectId=");
                            output.append(locId);
                            output
                                    .append("', '', '', 'false', 'popup', '')\">");
                            output.append(locName);
                            output.append("</a> <br>");
                            output.append(labelLocEquiv);
                            output.append(" <br>&nbsp;<br>");
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
                        allocStatus = i18nNow.getI18nString(allocStatus,
                                "emxFrameworkStringResource", context
                                        .getSession().getLanguage());

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
                        allocPref = i18nNow.getI18nString(allocPref,
                                "emxFrameworkStringResource", context
                                        .getSession().getLanguage());

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
        if (mccInstall) {
            return true;
        } else {
            return false;
        }
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
    public MapList getEnterpriseEquivalents(Context context, String[] args)
            throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);

        String objectId = (String) paramMap.get("objectId");
        MapList equivList = new MapList();

        MapList listMEPs = null;
        MapList corpMEPs = null;

        StringBuffer sbRelPattern = new StringBuffer(
                RELATIONSHIP_MANUFACTURER_EQUIVALENT);
        sbRelPattern.append(",");
        sbRelPattern.append(RELATIONSHIP_LOCATION_EQUIVALENT);

        // MCC Bug 330835 Fix to include MCC EP in the EP type pattern
        String sTypeCompliancePart = PropertyUtil.getSchemaProperty(context,
                "type_ComplianceEnterprisePart");

        StringBuffer sbTypePattern = new StringBuffer(
                TYPE_LOCATION_EQUIVALENT_OBJECT);
        sbTypePattern.append(",");
        sbTypePattern.append(TYPE_PART);
        sbTypePattern.append(",");
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
            scorpTypePattern.append(",");
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
        StringBuffer outPut = new StringBuffer();

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
        StringBuffer outPut = new StringBuffer();
        StringList selectList = new StringList(2);
        selectList.addElement(DomainObject.SELECT_NAME);
        selectList.addElement(DomainObject.SELECT_ID);
        String strReset = i18nNow.getI18nString(
                "emxManufacturerEquivalentPart.Common.Reset",
                "emxManufacturerEquivalentPartStringResource", context
                        .getSession().getLanguage());
        String strURL = "../common/emxSearch.jsp?defaultSearch=MEPUsageOrganizationSearchCommand";
        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person
                .getPerson(context);
        Company contextComp = contextPerson.getCompany(context);
        Map compMap = contextComp.getInfo(context, selectList);

        String defaultManufacturer = (String) compMap
                .get(DomainObject.SELECT_NAME);
        String defaultManufacturerId = (String) compMap
                .get(DomainObject.SELECT_ID);

        outPut
                .append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>");
        outPut.append("<input type=\"text\" name=\"UsageLocationDisplay\"");
        outPut.append(" size=\"20\" value=\"" + defaultManufacturer + "\"");
        outPut.append(" readonly=\"readonly\">");
        outPut.append("</input>");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
        outPut
                .append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        outPut.append(strURL);
        outPut.append("', 700, 500)\">");
        outPut.append("</input>");
        outPut
                .append("<a href=\"JavaScript:ResetField('emxCreateForm','UsageLocation','UsageLocationDisplay','"
                        + defaultManufacturer
                        + "','"
                        + defaultManufacturerId
                        + "')\">");
        outPut.append(strReset + "</a>");
        outPut.append("<input type=\"hidden\" name=\"UsageLocation\" value=\""
                + defaultManufacturerId + "\"></input>");
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
    public MapList getOraganizationSearchResult(Context context, String[] args)
            throws FrameworkException {
        MapList organizationList = new MapList();
        try {
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person
                    .getPerson(context);
            Company company = person.getCompany(context);
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String selectType = (String) paramMap.get("TypeDisplay");
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
                sWhereExp.append("(");
                sWhereExp.append("name ~~ \"");
                sWhereExp.append(txtName);
                sWhereExp.append("\"");
                sWhereExp.append(")");
                start = true;
            }

            // set the type & relationship names as per individual search
            if ("Company".equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_SUBSIDIARY;
            } else if ("Organization".equals(selectType)) {
                strTypeLocation = DomainConstants.TYPE_ORGANIZATION;
            } else if ("Business Unit".equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_DIVISION;
            } else if ("Department".equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT;
            } else if ("Location".equals(selectType)) {
                strRelLocation = DomainConstants.RELATIONSHIP_ORGANIZATION_LOCATION;
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

            if (txtVaultOption.equals("ALL_VAULTS")
                    || txtVaultOption.equals("")) {
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

                sWhereExp.append("(");
                sWhereExp.append("Vault");
                sWhereExp.append(" matchlist ");
                sWhereExp.append("'");
                sWhereExp.append(txtVault);
                sWhereExp.append("\' \',\')");

            } else if (txtVaultOption.equals("LOCAL_VAULTS")) {
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
                sWhereExp.append("(");
                sWhereExp.append("Vault");
                sWhereExp.append(" matchlist ");
                sWhereExp.append("'");
                sWhereExp.append(txtVault);
                sWhereExp.append("\' \',\')");

            } else if (txtVaultOption.equals("DEFAULT_VAULT")) {
                txtVault = context.getVault().getName();
                if (start) {
                    sWhereExp.append(" && ");
                }
                sWhereExp.append("(");
                sWhereExp.append("Vault == \"");
                sWhereExp.append(txtVault);
                sWhereExp.append("\"");
                sWhereExp.append(")");
            } else {
                txtVault = txtVaultOption;
                if (start) {
                    sWhereExp.append(" && ");
                }
                sWhereExp.append("(");
                sWhereExp.append("Vault");
                sWhereExp.append(" matchlist ");
                sWhereExp.append("'");
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
        StringBuffer returnString = new StringBuffer();
       try{
           HashMap paramMap = (HashMap) JPO.unpackArgs(args);

           HashMap requestMap = (HashMap) paramMap.get("requestMap");

           String isAddExisting = (String) requestMap.get("mepAddExisting");

        StringList selectList = new StringList(2);
        selectList.addElement(DomainObject.SELECT_NAME);
        selectList.addElement(DomainObject.SELECT_ID);
        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person
                .getPerson(context);
        Company contextComp = contextPerson.getCompany(context);
        Map compMap = contextComp.getInfo(context, selectList);

        String defaultManufacturer = (String) compMap
                .get(DomainObject.SELECT_NAME);
        String defaultManufacturerId = (String) compMap
                .get(DomainObject.SELECT_ID);
        String strReset = i18nNow.getI18nString(
                "emxManufacturerEquivalentPart.Common.Reset",
                "emxManufacturerEquivalentPartStringResource", context
                        .getSession().getLanguage());
        String strURL = "../common/emxSearch.jsp?defaultSearch=MepSearch&amp;clearManuLoc=true&amp;helpMarker=emxhelpselectorganization&amp;HelpMarker=emxhelpselectorganization";
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
        returnString
                .append("<a href=\"JavaScript:ResetField('forms[0]','Manufacturer','ManufacturerDisplay','"
                        + defaultManufacturer
                        + "','"
                        + defaultManufacturerId
                        + "');resetManuLoc();");
        if("true".equals(isAddExisting)){

        returnString.append("\">");
        }
        else{

            returnString.append("loadRevision();\">");
        }

        returnString.append(strReset + "</a>");
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

            radioOption = new StringBuffer(150);

            String strLocale = context.getSession().getLanguage();
            i18nNow i18nNowInstance = new i18nNow();

            String vaultDefaultSelection = PersonUtil
                    .getSearchDefaultSelection(context);

            String strAll = i18nNowInstance.GetString(
                    "emxFrameworkStringResource", strLocale,
                    "emxFramework.Preferences.AllVaults");
            String strDefault = i18nNowInstance.GetString(
                    "emxFrameworkStringResource", strLocale,
                    "emxFramework.Preferences.DefaultVault");
            String strSelected = i18nNowInstance.GetString(
                    "emxFrameworkStringResource", strLocale,
                    "emxFramework.Preferences.SelectedVaults");
            String strLocal = i18nNowInstance.GetString(
                    "emxFrameworkStringResource", strLocale,
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
            radioOption.append(">");
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
            radioOption.append(">");
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
            radioOption.append(">");
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
            radioOption.append(">");
            radioOption.append(strSelected);
            radioOption
                    .append("&nbsp;&nbsp;<input type=\"text\" READONLY name=\"vaultsDisplay\" value =\""
                            + selDisplayVault
                            + "\" id=\"\" size=\"20\"                onFocus=\"this.blur();\">");
            radioOption
                    .append("<script language=\"JavaScript\">assignValidateMethod('vaultsDisplay', 'vaultSelection');</script>");

            radioOption
                    .append("<input type=\"button\" name=\"VaultChooseButton\" value=\"...\" onclick=\"document.forms[0].vaultOption[3].checked=true;javascript:top.showChooser('../common/emxVaultChooser.jsp?fieldNameActual=vaults&fieldNameDisplay=vaultsDisplay&multiSelect=false&isFromSearchForm=true')\">");
            radioOption
                    .append("<input type=\"hidden\" name=\"vaults\" value=\"");
            radioOption.append(selVault);
            radioOption.append("\" size=15>");
            radioOption.append("<br>");

        } catch (Throwable excp) {

        }

        return radioOption.toString();

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
    public MapList getManufacturingLocationSearchResults(Context context, String[] args)
            throws FrameworkException {
        MapList organizationList = new MapList();
        try {

            HashMap paramMap = (HashMap) JPO.unpackArgs(args);

            String sType = (String) paramMap.get("TypeDisplay");
            String sName = (String) paramMap.get("Name");
            String sSelManufacturerId=(String)paramMap.get("companyId");

            String strRelLocation = "*";
            String strTypeLocation = "*";
            SelectList busSelects = new SelectList(1);
            busSelects.add(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_TYPE);
            busSelects.add(DomainConstants.SELECT_NAME);
            busSelects.add(DomainConstants.SELECT_DESCRIPTION);
            busSelects.add("current.access[fromconnect]");
            String attrPlantId   = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_PlantID);
            busSelects.add(DomainObject.getAttributeSelect(attrPlantId));
            StringBuffer sWhereExp = new StringBuffer();
            String txtVault   ="";
            String strVaults  ="";
            String relWhere = "";


            String isManufacturingLocation   = (String)paramMap.get("isManufacturingLocation");
            if("Company".equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_SUBSIDIARY;
                //strTypeLocation = DomainConstants.TYPE_COMPANY;
            }
                else if("Organization".equals(sType))
            {
                strTypeLocation = DomainConstants.TYPE_ORGANIZATION;
            }
            else if("Business Unit".equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_DIVISION;

            }
           else if("Department".equals(sType))
            {
                strRelLocation = DomainConstants.RELATIONSHIP_COMPANY_DEPARTMENT;
            }
            else if("Location".equals(sType))
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
              if (txtVaultOption.equals("DEFAULT_VAULT"))
                {
                    txtVault = context.getVault().getName();
                }

              else if(txtVaultOption.equals("ALL_VAULTS"))
                 {
                   txtVault="Vault ~= const\"*\"";

                 }
                 else if(txtVaultOption.equals("LOCAL_VAULTS"))
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
                     sWhereExp.append("(");
                     sWhereExp.append("name ~~ \"");
                     sWhereExp.append(sName);
                     sWhereExp.append("\"");
                     sWhereExp.append(")");

                 }

                 if(sWhereExp.length()>0) {sWhereExp.append(" && ");}
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






            //return organizationList;
        } catch (Exception Ex) {
            throw new FrameworkException(Ex);
        }
        return organizationList;
    }


    /**
     * Creates a Supplier Equivalent Part and connects to the Supplier using
     * Supplied By Relationship
     *
     * @param context
     *            The Matrix Context.
     * @param args
     *            holds a packed HashMap which contains supplier Id
          * @throws FrameworkException
     *             If the operation fails.
     */
@com.matrixone.apps.framework.ui.PostProcessCallable
    public void createSEP(Context context, String[] args) throws Exception {

        try {
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

			//DebugUtil.setDebug(true);
            ContextUtil.startTransaction(context, true);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            DebugUtil.debug("Inside the create SEP:requestMap"+requestMap);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            DebugUtil.debug("Inside the create SEP:paramMap"+paramMap);
            String sepObjId=(String)paramMap.get("objectId");
            String supplierId=(String)requestMap.get("SupplierOID");
            DebugUtil.debug("Inside the create SEP:sepObjId"+sepObjId);
            DebugUtil.debug("Inside the create SEP:supplierId"+supplierId);
            DomainRelationship.connect(context,new DomainObject(supplierId), CPCConstants.RELATIONSHIP_SUPPLIED_BY, new DomainObject(sepObjId));
            ContextUtil.commitTransaction(context);
            String parentOID=(String)requestMap.get("objectId");
            DebugUtil.debug("Inside the create SEP:parentOID"+parentOID);
            //This indicates that the method is invoked from Part Family-->Create New SEP
            //or Part Equivalents Page-->Create SEP
            //ParentOID would be passed in both the cases,checking for the type and
            //peforming appropriate action
            if((parentOID != null) && (new DomainObject(parentOID).getInfo(context,DomainConstants.SELECT_TYPE).equals(DomainConstants.TYPE_PART_FAMILY))){
				com.matrixone.apps.common.Part commonPart=new com.matrixone.apps.common.Part();
				String sepObjIdArr[]={sepObjId};
				commonPart.setId(parentOID);
				commonPart.addParts(context,sepObjIdArr);
			}

        } catch (Exception e) {
            e.printStackTrace();
             try{
            ContextUtil.abortTransaction(context);

             }catch(Exception ex){
                 throw (new Exception("SEP not created.Please create again with valid data"));
             }


            throw (new FrameworkException(e));
        }
    }

    /**
	     * Updates the supplier attached to the Supplier Equivalent Part
	     *
	     * @param context
	     *            The Matrix Context.
	     * @param args
	     *            holds a packed HashMap which contains supplier Id
	     * @throws FrameworkException
	     *             If the operation fails.
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void updateSupplier(Context context,String args[]) throws Exception{

		try{
			//DebugUtil.setDebug(true);
            DebugUtil.debug("Inside the updateSupplier S");
            ContextUtil.startTransaction(context, true);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            DebugUtil.debug("Inside the updateSupplier S:paramMap"+paramMap);
            String strSupplierId = (String) paramMap.get("SupplierOID");
            DebugUtil.debug("Inside the updateSupplier S:strSupplierId"+strSupplierId);
            //During the edit of SEP the post process JPO is called, if supplier is not changed
            //SupplierOID would be null.chking for the same
			if(!("").equals(strSupplierId)){
				String strSEPPartId = (String) paramMap.get("objectId");
				StringList existingSupplierList=new DomainObject(strSEPPartId).getInfoList(context,"to["+CPCConstants.RELATIONSHIP_SUPPLIED_BY+"].id");
				DebugUtil.debug("Inside the updateSupplier SEP:existingSupplierList"+existingSupplierList.size());
				DebugUtil.debug("Inside the updateSupplier SEP:existingSupplierList"+existingSupplierList.get(0));
				DomainRelationship.disconnect(context,(String)existingSupplierList.get(0));
				DomainRelationship.connect(context,new DomainObject(strSupplierId),CPCConstants.RELATIONSHIP_SUPPLIED_BY,new DomainObject(strSEPPartId));
				ContextUtil.commitTransaction(context);
			}

		}catch(Exception er){
			er.printStackTrace();
			ContextUtil.abortTransaction(context);
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

            if (strPartClassification.equals("Equivalent")) {
                return new Boolean(true);
            } else {
                return new Boolean(false);
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
        String languageStr = (String) paramMap.get("languageStr");

        setId(strPartId);
        // Get the part policy classification
        String strPartClassification = getInfo(context,
                "policy.property[PolicyClassification].value");
        String strManufacturerEquivalent = i18nNow.getI18nString(
                "emxManufacturerEquivalentPart.Part.MfgEquivalent",
                "emxManufacturerEquivalentPartStringResource", languageStr);
        String strEnterprise = i18nNow.getI18nString(
                "emxManufacturerEquivalentPart.Part.Enterprise",
                "emxManufacturerEquivalentPartStringResource", languageStr);
        StringList PartOriginList = new StringList(1);

        if (strPartClassification.equals("Equivalent")) {
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
            strBuf.append(strRevision);
            if (reportFormat == null || reportFormat.length() == 0
                    || "null".equals(reportFormat)) {
                strBuf.append(
                        "<input type=\"hidden\" name=\"Revision\" value=\"")
                        .append(strRevision).append("\">");
            }
        } else {

            String customRevision = FrameworkProperties
                    .getProperty("emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
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
                        .append(strRevision).append("\" ");
            }
            /* EC-MCC interoperability */
            /* Added one more condition to check MCC is installed or not. */
            if ("false".equals(customRevision) || isMCCInstalled(context, args)) {
                strBuf.append(" READONLY ");
            }
            strBuf.append(">");
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
        String languageStr = (String) paramMap.get("languageStr");

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
                (new com.matrixone.apps.common.EngineeringChange())
                        .setRevision(context, strPartId, strRevValue, strName);
            } else {
                String strMessage = i18nNow.getI18nString(
                        "emxManufacturerEquivalentPart.MEP.Exists",
                        "emxManufacturerEquivalentPartStringResource",
                        languageStr);
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
                    .getProperty("emxManufacturerEquivalentPart.Policy.EnablePartPolicyEditing");
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
                                            + partPolicyName
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
        return new Boolean(true);
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
                StringBuffer output = new StringBuffer();
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
                            output.append(entName + " " + entRev + " \n");
                        }
                        // do not show hyperlinks if it is a printer friendly or
                        // excel export page
                        // length will be >0 when format is HTML, ExcelHTML, CSV
                        // or TXT
                        else if (null != reportFormat
                                && !reportFormat.equals("null")
                                && (reportFormat.length() > 0)) {
                            output.append(entName + " " + entRev + "<br>");
                        } else {
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + linkFile
                                            + "?emxSuiteDirectory="
                                            + suiteDir
                                            + "&suiteKey="
                                            + suiteKey
                                            + "&objectId="
                                            + entId
                                            + "', '', '', 'false', 'popup', '')\">"
                                            + entName
                                            + " "
                                            + entRev
                                            + "</a> <br>");
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
                            output.append(entName + " " + entRev + " \n");
                        }
                        // do not show hyperlinks if it is a printer friendly or
                        // excel export page
                        // length will be >0 when format is HTML, ExcelHTML, CSV
                        // or TXT
                        else if (null != reportFormat
                                && !reportFormat.equals("null")
                                && (reportFormat.length() > 0)) {
                            output.append(entName + " " + entRev + "<br>");
                        } else {
                            output
                                    .append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"
                                            + linkFile
                                            + "?emxSuiteDirectory="
                                            + suiteDir
                                            + "&suiteKey="
                                            + suiteKey
                                            + "&objectId="
                                            + entId
                                            + "', '', '', 'false', 'popup', '')\">"
                                            + entName
                                            + " "
                                            + entRev
                                            + "</a> <br>");
                        }
                    }
                }
                if (!"".equals(output.toString())) {
                    result.add(output.toString());
                }

                if (!hasEquiv) {
                    result.add("&nbsp;");
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
        String strObjectId = "";
        DomainObject domObj = null;

        if (objList != null && (listSize = objList.size()) > 0) {
            columnVals = new Vector(listSize);
            String relManufacturingLocation = PropertyUtil.getSchemaProperty(
                    context, "relationship_ManufacturingLocation");
            String sManufacturingLocationName = "";
            for (int i = 0; i < listSize; i++) {
                map = (Map) objList.get(i);
                strObjectId = (String) map.get(DomainObject.SELECT_ID);
                domObj = new DomainObject(strObjectId);
                sManufacturingLocationName = domObj.getInfo(context, "from["
                        + relManufacturingLocation + "].to.name");

                // check if any Manufacturing Location is connected to any
                // company
                if (sManufacturingLocationName != null
                        && !"null".equals(sManufacturingLocationName)
                        && !"".equals(sManufacturingLocationName)) {
                    columnVals.add(sManufacturingLocationName);
                } else {
                    columnVals.add("");
                }
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
            StringList attrRanges = FrameworkUtil.getRanges(context,
                    "Location Preference");
            if (objList != null && (listSize = objList.size()) > 0) {
                columnVals = new Vector(listSize);
                for (int i = 0; i < listSize; i++) {
                    map = (Map) objList.get(i);
                    srelId = (String) map.get("id[connection]");
                    String oldLocPref = DomainRelationship.getAttributeValue(
                            context, srelId, "Location Preference");
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
            String attrLocPref = PropertyUtil.getSchemaProperty(context,
                    "attribute_LocationPreference");
            if (requestMap.get("objCount") != null
                    || !"null".equals(requestMap.get("objCount"))) {
                String sLocPrefValue = (String) paramMap.get("New Value");
                new DomainRelationship().setAttributeValue(context, relId,
                        attrLocPref, sLocPrefValue);
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
            // AttributeType attrType = new AttributeType("Location Status");

            // String defaultValue = attrType.getDefaultValue(context);
            StringList attrRanges = FrameworkUtil.getRanges(context,
                    "Location Status");
            if (objList != null && (listSize = objList.size()) > 0) {
                columnVals = new Vector(listSize);
                for (int i = 0; i < listSize; i++) {
                    map = (Map) objList.get(i);

                    srelId = (String) map.get("id[connection]");
                    String oldLocStat = DomainRelationship.getAttributeValue(
                            context, srelId, "Location Status");
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
            String attrLocStat = PropertyUtil.getSchemaProperty(context,
                    "attribute_LocationStatus");
            if (requestMap.get("objCount") != null
                    || !"null".equals(requestMap.get("objCount"))) {
                String sLocStatValue = (String) paramMap.get("New Value");
                new DomainRelationship().setAttributeValue(context, relId,
                        attrLocStat, sLocStatValue);
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

    public MapList getMEPLocations(Context context, String[] args)
            throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");

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

        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

        StringBuffer sbRelPattern = new StringBuffer(
                RELATIONSHIP_MANUFACTURER_EQUIVALENT);
        sbRelPattern.append(",");
        sbRelPattern.append(RELATIONSHIP_ALLOCATION_RESPONSIBILITY);

        StringBuffer sbTypePattern = new StringBuffer(
                TYPE_LOCATION_EQUIVALENT_OBJECT);
        sbTypePattern.append(",");
        sbTypePattern.append(DomainConstants.TYPE_LOCATION);
        sbTypePattern.append(",");
        sbTypePattern.append(DomainConstants.TYPE_ORGANIZATION);

        StringBuffer sbCorpMEPLocTypePattern = new StringBuffer(
                DomainConstants.TYPE_LOCATION);
        sbCorpMEPLocTypePattern.append(",");
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
            imageForType = FrameworkProperties
                    .getProperty("emxFramework.smallIcon." + type);
        } else {
            imageForType = FrameworkProperties
                    .getProperty("emxFramework.smallIcon.type_" + type);
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<img src=\"../common/images/");
        sb.append(imageForType + "\"></img>");
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
        StringBuffer outStr = new StringBuffer();
        outStr
                .append("<select name=\"DateOption\" extra=\"yes\" onChange=\"javascript:showDateBetweenField();\">");
        outStr.append("<option value=\"");
        outStr.append("*" + "\">");
        outStr.append("*");
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
        outStr
                .append("\">&nbsp;<a href=\"javascript:showCalendar('editDataForm', '");
        outStr.append(fieldName);
        outStr.append("', '', saveFieldObjByName('");
        outStr.append(fieldName);
        outStr
                .append("'))\"><img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\"></a>");
        outStr.append("<input type=\"hidden\" name=\"");
        outStr.append(fieldName).append("_msvalue");
        outStr.append("\"  value=\"");
        outStr.append("\">&nbsp;&nbsp");

        outStr
                .append("<input type=\"text\" readonly=\"readonly\" style=\"visibility:hidden\"");
        outStr.append("\" name=\"");
        outStr.append(fieldName1);
        outStr.append("\" value=\"\" id=\"");
        outStr.append(fieldName1);
        outStr
                .append("\">&nbsp;<a href=\"javascript:showCalendar('editDataForm', '");
        outStr.append(fieldName1);
        outStr.append("', '', saveFieldObjByName('");
        outStr.append(fieldName1);
        outStr
                .append("'))\"><img src=\"../common/images/iconSmallCalendar.gif\" alt=\"Date Picker\" border=\"0\" name=\"picture\" id=\"picture\" style=\"visibility:hidden\"></a>");
        outStr.append("<input type=\"hidden\" name=\"");
        outStr.append(fieldName1).append("_msvalue");
        outStr.append("\"  value=\"");
        outStr.append("\">");
        outStr
                .append("<script language=\"JavaScript\">assignValidateMethod('RevisionDateFirst', 'emptyDateField');</script>");

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
        String strClear = i18nNow.getI18nString(
                "emxManufacturerEquivalentPart.Common.Clear",
                "emxManufacturerEquivalentPartStringResource", context
                        .getSession().getLanguage());
        String strURL = "../components/emxCommonSearch.jsp?formName=editDataForm&frameName=formEditDisplay&searchmode=PersonChooser&suiteKey=Components&searchmenu=APPMemberSearchInMemberList&searchcommand=APPFindPeople&fieldNameDisplay=OwnerDisplay&fieldNameActual=Owner";
        StringBuffer outPut = new StringBuffer();
        outPut
                .append("<script language=\"javascript\" src=\"emxMEPFormValidation.jsp\"></script>");
        outPut.append("<input type=\"text\" name=\"OwnerDisplay");
        outPut.append("\"size=\"20\" value=\"*\"");
        outPut.append(" readonly=\"readonly\">&nbsp;");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnOwnerChooser\" size=\"200\" ");
        outPut
                .append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        outPut.append(strURL);
        outPut.append("', 700, 500)\">&nbsp;&nbsp;");
        outPut
                .append("<a href=\"JavaScript:clearField('editDataForm','Owner','OwnerDisplay')\">");
        outPut.append(strClear + "</a>");
        outPut
                .append("<input type=\"hidden\" name=\"Owner\" value=\"*\"></input>");
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
        String strClear = i18nNow.getI18nString(
                "emxManufacturerEquivalentPart.Common.Clear",
                "emxManufacturerEquivalentPartStringResource", context
                        .getSession().getLanguage());
        String strURL = "../components/emxComponentsFindMemberDialogFS.jsp?formName=editDataForm&frameName=formEditDisplay&fieldNameDisplay=OrginatorDisplay&fieldNameActual=Orginator";
        StringBuffer outPut = new StringBuffer();
        outPut
                .append("<script language=\"javascript\" src=\"emxMEPFormValidation.jsp\"></script>");
        outPut.append("<input type=\"text\" name=\"OrginatorDisplay");
        outPut.append("\"size=\"20\" value=\"*\"");
        outPut.append(" readonly=\"readonly\">&nbsp;");
        outPut.append("<input class=\"button\" type=\"button\"");
        outPut.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
        outPut
                .append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        outPut.append(strURL);
        outPut.append("', 700, 500)\">&nbsp;&nbsp;");
        outPut
                .append("<a href=\"JavaScript:clearField('editDataForm','Orginator','OrginatorDisplay')\">");
        outPut.append(strClear + "</a>");
        outPut
                .append("<input type=\"hidden\" name=\"Orginator\" value=\"*\"></input>");
        return outPut.toString();
    }

    /**
     * This method displays the policy field
     *
     * @param context
     * @param args
     * @return html string for showPolicyfield
     * @throws Exception
     */
    public String showPolicyField(Context context, String[] args)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        String typePart = PropertyUtil.getSchemaProperty(context, "type_Part");
        StringList policyList = Part.getEquivalentPolicies(context,typePart);
        String languageStr = context.getSession().getLanguage();
        StringList policyViewI18NStr = i18nNow.getAdminI18NStringList("Policy", policyList, languageStr);
        sb.append("<select name=\"Policy\" extra=\"yes\" onChange=\"javascript:showStatesForPolicy();\">");
        for (int polItr = 0; polItr < policyList.size(); polItr++) {
            sb.append("<option value=\"");
            sb.append(policyList.get(polItr) + "\">");
            sb.append(policyViewI18NStr.get(polItr));
        }
        sb.append("</select>");
        sb.append("<script>");
        sb.append("</script>");
        return sb.toString();

    }

/**
     * This method displays the policy field
     *
     * @param context
     * @param args
     * @return html string for showPolicyfield
     * @throws Exception
     */
    public String showStateField(Context context, String[] args)
            throws Exception {

		StringBuffer sb = new StringBuffer();
		StringList strListStateDisplay = new StringList();
        StringList strListStateValue = new StringList();
        String languageStr = context.getSession().getLanguage();
		String sPartType  = PropertyUtil.getSchemaProperty(context, "type_SupplierEquivalentPart");
		String sPartPolicy  = PropertyUtil.getSchemaProperty(context, "policy_SupplierEquivalent");
		StringList strListEquivalentPolicies = com.matrixone.apps.componentcentral.CPCPart.getPartPolicies(context,sPartType);
        int sizeOfPolicies = 0;
        DebugUtil.debug("Inside the state:strListEquivalentPolicies "+strListEquivalentPolicies.size());
        //if(strListEquivalentPolicies != null && (sizeOfPolicies = strListEquivalentPolicies.size()) > 0)
        //{
            Policy polObj                   = null;
            int stateListSize               = 0;
            StateRequirementList stReqLst   = null;
            StateRequirement stReq          = null;
            String strPolicyName            = "";
            String strStateName             = "";
                polObj          = new Policy(sPartPolicy);
                stReqLst        = polObj.getStateRequirements(context);
                DebugUtil.debug("Inside the state:size "+stateListSize);
                DebugUtil.debug("Inside the state:size "+stReqLst);
				sb.append("<select name=\"Policy\" extra=\"yes\"\">");
                for(int j = sizeOfPolicies; j < stReqLst.size() ; j++)
                {
					DebugUtil.debug("Inside the state:loop "+j);
                    stReq   = (StateRequirement) stReqLst.get(j);
                    strStateName = stReq.getName();
                    //if(!strListStateValue.contains(strStateName))
                    //{
                        strListStateValue.add(strStateName);
                        strListStateDisplay.add(i18nNow.getI18nString(strPolicyName,strStateName, languageStr));
                        DebugUtil.debug("Inside the state:value "+strStateName);
						sb.append("<option value=\""+"selected=\"true\"");
						sb.append(strStateName + "\">");
						sb.append(strStateName);
						DebugUtil.debug("Inside the state:value after option");
	                  //}

					//sb.append("</option>");
               }
               	sb.append("</select>");
               	sb.append("<script>");
               	sb.append("</script>");
		//}
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
			DebugUtil.debug("Inside the getMEPSearchResult:");
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
                    sbWhereExp.append("(");
                    start = false;
                }
                sbWhereExp.append("(");
                sbWhereExp.append(DomainConstants.SELECT_DESCRIPTION);
                sbWhereExp.append(" ~~ ");
                sbWhereExp.append("\"");
                sbWhereExp.append(sDescription);
                sbWhereExp.append("\"");
                sbWhereExp.append(")");
            }

            if (sState != null && (!("").equals(sState))) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append("(");
                sbWhereExp.append(DomainConstants.SELECT_CURRENT);
                sbWhereExp.append(" ~~ ");
                sbWhereExp.append("\"");
                sbWhereExp.append(sState);
                sbWhereExp.append("\"");
                sbWhereExp.append(")");
            }

            if (sPolicy != null && (!"".equals(sPolicy))) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append("(");
                sbWhereExp.append(DomainConstants.SELECT_POLICY);
                sbWhereExp.append(" == ");
                sbWhereExp.append("\"");
                sbWhereExp.append(sPolicy);
                sbWhereExp.append("\"");
                sbWhereExp.append(")");
            }

            if (sOriginator != null
                    && (!"*".equals(sOriginator) && !"".equals(sOriginator))) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append("(");
                sbWhereExp.append("attribute[" + attrOriginator + "]");
                sbWhereExp.append(" == ");
                sbWhereExp.append("\"");
                sbWhereExp.append(sOriginator);
                sbWhereExp.append("\"");
                sbWhereExp.append(")");
            }

            if (sRevisionDateFirst != null && !"".equals(sRevisionDateFirst)) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                if ("Is On".equals(sDateOption)) {
                    sbWhereExp.append("(");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" <= ");
                    sbWhereExp.append("\"");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\"");
                    sbWhereExp.append(" && ");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" >= ");
                    sbWhereExp.append("\"");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "12:00:00 AM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\"");
                    sbWhereExp.append(")");
                } else if ("Is On or Before".equals(sDateOption)) {
                    sbWhereExp.append("(");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" <= ");
                    sbWhereExp.append("\"");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\"");
                    sbWhereExp.append(")");
                } else if ("Is On or After".equals(sDateOption)) {
                    sbWhereExp.append("(");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" >= ");
                    sbWhereExp.append("\"");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "12:00:00 AM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\"");
                    sbWhereExp.append(")");
                } else if ("Is Between".equals(sDateOption)) {
                    sbWhereExp.append("(");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" >= ");
                    sbWhereExp.append("\"");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "12:00:00 AM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\"");
                    sbWhereExp.append(" && ");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" <= ");
                    sbWhereExp.append("\"");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateLast, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\"");
                    sbWhereExp.append(")");
                } else {
                    sbWhereExp.append("(");
                    sbWhereExp.append("attribute[" + attrRevisionDate + "]");
                    sbWhereExp.append(" matchlist ");
                    sbWhereExp.append("'");
                    sbWhereExp.append(eMatrixDateFormat
                            .getFormattedInputDateTime(context,
                                    sRevisionDateFirst, "11:59:59 PM",
                                    clientTZOffset, locale));
                    sbWhereExp.append("\' \',\')");

                }

            }

            if (sManufacturer != null
                    && (!"*".equals(sManufacturer) && !"".equals(sManufacturer))) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append("(");
                sbWhereExp.append("to[" + relManufacturingResponsibility
                        + "].from.id");
                sbWhereExp.append(" matchlist ");
                sbWhereExp.append("'");
                sbWhereExp.append(sManufacturer);
                sbWhereExp.append("\' \',\')");
            }

            if (sManufacturerLocation != null
                    && (!"*".equals(sManufacturerLocation) && !""
                            .equals(sManufacturerLocation))) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append("(");
                sbWhereExp.append("from[" + relManufacturingLocation
                        + "].to.id");
                sbWhereExp.append(" matchlist ");
                sbWhereExp.append("'");
                sbWhereExp.append(sManufacturerLocation);
                sbWhereExp.append("\' \',\')");
            }

            if (sRevisionLevel != null
                    && (!"*".equals(sRevisionLevel) && !""
                            .equals(sRevisionLevel))) {
                if (start) {
                    sbWhereExp.append("(");
                    start = false;
                } else {
                    sbWhereExp.append(" && ");
                }
                sbWhereExp.append("(");
                sbWhereExp.append("attribute[" + attrExternalRevision + "]");
                sbWhereExp.append(" matchlist ");
                sbWhereExp.append("'");
                sbWhereExp.append(sRevisionLevel);
                sbWhereExp.append("\' \',\')");
            }

            if (!start) {
                sbWhereExp.append(")");
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

            if (txtVaultOption.equals("ALL_VAULTS")
                    || txtVaultOption.equals("")) {
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
            } else if (txtVaultOption.equals("DEFAULT_VAULT")) {
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
			DebugUtil.debug("Inside the getMEPSearchResult: query "+sType);
			DebugUtil.debug("Inside the getMEPSearchResult: sName "+sName);
			DebugUtil.debug("Inside the getMEPSearchResult: sbWhereExp "+sbWhereExp);

            return organizationList;

        } catch (Exception Ex) {
            throw new FrameworkException(Ex);
        }
    }

    /**
     *
     * This method gets list of suppliers
     *
     * @param context
     * @param args
     *            holds name and type
     * @return MapList of manufacturers
     * @throws Exception
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSuppliers(Context context, String[] args)
            throws Exception {

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        DebugUtil.debug("Inside getSuppliers;"+paramMap);
        String sWhereExp = "";
        String txtName = (String) paramMap.get("Name");
        DebugUtil.debug("Inside getSuppliers;txtName"+txtName);
        String selectType = (String) paramMap.get("TypeDisplay");
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

                String srelPattern = PropertyUtil.getSchemaProperty(context,
                        "relationship_Division");
                totalresultList = company.getRelatedObjects(context,
                        srelPattern, // java.lang.String relationshipPattern,
                        selectType, // java.lang.String typePattern,
                        resultSelects, // matrix.util.StringList objectSelects,
                        null, // matrix.util.StringList relationshipSelects,
                        true, // boolean getTo,
                        true, // boolean getFrom,
                        (short) 1, // short recurseToLevel,
                        sWhereExp, // java.lang.String objectWhere,
                        null);
            } else if (selectType.equals("Company")) {
                if (!"*".equals(txtName)) {
                    sWhereExp = " name ~~ \"" + txtName + "\" ";
                    sWhereExp += " && ";
                }
                sWhereExp += "current == Active";
                totalresultList = company.getSuppliers(context, resultSelects,
                        null, sWhereExp, null);
            }
             else if (selectType.equals(DomainConstants.TYPE_LOCATION)) {
				if (!"*".equals(txtName)) {
						sWhereExp = " name ~~ \"" + txtName + "\" ";
						sWhereExp += " && ";
				}
					sWhereExp += "current == Active";
					totalresultList = company.getLocations(context, resultSelects,
							sWhereExp);
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
    public MapList getInProcessSEPs(Context context, String[] args)
            throws Exception {
        MapList sepList = new MapList();
        try {
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            DebugUtil.debug("Inside getInProcessSEPs;"+paramMap);
            String objectId=(String)paramMap.get("objectId");
            DebugUtil.debug("Inside getInProcessSEPs objectId"+objectId);
            Part partObj = new Part(objectId);
			com.matrixone.apps.componentcentral.CPCPart cpcPart=new com.matrixone.apps.componentcentral.CPCPart();
            //CPC commented building list
            //StringList selectStmts = partObj.getMEPSelectList(context);
            // create where clause to filter out only MEP's in release state
            // Added for Bug: 308765
            //String strSEPRelease = Part.getReleaseState(CPConstants.POLICY_SEP);
            //CPC change the hardcoded value, read from prop
            //String whereCls = " (current == " + "Release" + ") ";
            StringList selectStmts = new StringList();
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
            String whereCls = null;
            String vault = QUERY_WILDCARD;
            DebugUtil.debug("The vault:"+vault);
            if(objectId != null)
               sepList=partObj.getRelatedObjects(context,
                    CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT +","+ DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT, // relationship pattern
                    CPCConstants.TYPE_SEP, // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    null, // object where clause
                    null); // relationship where clause
            else
            	sepList = cpcPart.getSupplierEquivalents(context, selectStmts,vault, whereCls);
        } catch (FrameworkException Ex) {
            throw Ex;
        }
        return sepList;
    }

    /**
	     * @param context
	     * @param args
	     * @return Maplist of MEPs
	     * @throws Exception
	     */
	     @com.matrixone.apps.framework.ui.ProgramCallable
	    public MapList getInProcessECParts(Context context, String[] args)
	            throws Exception {
	        MapList ecList = new MapList();
	        try {
				ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

				HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	            DebugUtil.debug("Inside getInProcessECParts;"+paramMap);
	            String objectId=(String)paramMap.get("objectId");
	            DebugUtil.debug("Inside getInProcessECParts objectId"+objectId);
	            Part partObj = new Part(objectId);
				com.matrixone.apps.componentcentral.CPCPart cpcPart=new com.matrixone.apps.componentcentral.CPCPart();
	            StringList selectStmts = new StringList();
	            selectStmts.addElement(SELECT_ID);
	            selectStmts.addElement(SELECT_TYPE);
	            selectStmts.addElement(SELECT_NAME);
	            selectStmts.addElement(SELECT_REVISION);
	            String whereCls = null;
	            String vault = QUERY_WILDCARD;
	            DebugUtil.debug("The vault:"+vault);
	           	ecList = cpcPart.getECParts(context, selectStmts,vault, whereCls);
	        } catch (FrameworkException Ex) {
	            throw Ex;
	        }
	        return ecList;
    }

	/**
     *
     * @param context
     * @param ecIds
     * @throws FrameworkException
     */
     @com.matrixone.apps.framework.ui.PostProcessCallable
    public void addECParttoSEP(Context context,String ecPartIds[]) throws FrameworkException{
        try{
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

			ContextUtil.startTransaction(context, true);
            DebugUtil.debug("Inside the addECParttoSEP: "+ecPartIds);
			HashMap argsMap = (HashMap) JPO.unpackArgs(ecPartIds);
            HashMap requestMap=(HashMap)argsMap.get("requestMap");
            String parendId=(String)requestMap.get("parentOID");
            String policy=new DomainObject(parendId).getInfo(context,DomainConstants.SELECT_POLICY);
            HashMap paramMap=(HashMap)argsMap.get("paramMap");
            String sepPartId=(String)requestMap.get("objectId");
            String ecPartId=(String)paramMap.get("newObjectId");
            DebugUtil.debug("ecPartId: "+ecPartId);
            if(policy.equals(DomainConstants.POLICY_MANUFACTURER_EQUIVALENT))
            	DomainRelationship.connect(context,new DomainObject(ecPartId),DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,new DomainObject(sepPartId));
			else
			    DomainRelationship.connect(context,new DomainObject(ecPartId),CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,new DomainObject(sepPartId));
            ContextUtil.commitTransaction(context);
        }catch(Exception err){
            ContextUtil.abortTransaction(context);
            err.printStackTrace();
            throw new FrameworkException(err);
        }
	}

		/**
	     *
	     * @param context
	     * @param ecIds
	     * @throws FrameworkException
	     */
        @com.matrixone.apps.framework.ui.PostProcessCallable
	    public void addSEPParttoEC(Context context,String ecPartIds[]) throws FrameworkException{
	        try{
				ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

				//DebugUtil.setDebug(true);
				ContextUtil.startTransaction(context, true);
				createSEP(context,ecPartIds);
				HashMap argsMap = (HashMap) JPO.unpackArgs(ecPartIds);
				//DebugUtil.debug("argsMap: "+argsMap);
	            HashMap requestMap=(HashMap)argsMap.get("requestMap");
	            HashMap paramMap=(HashMap)argsMap.get("paramMap");
	            String ecPartId=(String)requestMap.get("objectId");
	            String sepPartId=(String)paramMap.get("newObjectId");
	            DebugUtil.debug("ecPartId: "+ecPartId);
	            DebugUtil.debug("sepPartId: "+sepPartId);
	            //DomainRelationship.connect(context,new DomainObject(ecPartId),CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,new DomainObject(sepPartId));

	            // Fix for IR-162712V6R2013x, IR-162708V6R2013x (Start)

	            // find the context of the part and relate accordingly
	            DomainObject partObj = DomainObject.newInstance(context,ecPartId);
	         	String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
	         	String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

	         	if (partType.equals(DomainConstants.TYPE_PART) &&
	             	partPolicy.equals(DomainConstants.POLICY_MANUFACTURER_EQUIVALENT))
	         	{
	         		DomainRelationship.connect(context,new DomainObject(sepPartId),DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT,new DomainObject(ecPartId));
		            DebugUtil.debug("addSEPParttoEC: Relating SEP in the MEP context.");
	         	}
	         	else
	         	{
	         		DomainRelationship.connect(context,new DomainObject(ecPartId),CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,new DomainObject(sepPartId));
		            DebugUtil.debug("addSEPParttoEC: Relating SEP in the Enterprise Part context.");
	         	}
	         	// Fix for IR-162712V6R2013x, IR-162708V6R2013x (End)

	            ContextUtil.commitTransaction(context);
	        }catch(Exception err){
	            ContextUtil.abortTransaction(context);
	            err.printStackTrace();
	            throw new FrameworkException(err);
	        }
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
  */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
 public StringList excludeAlreadyConnectedObjects (Context context, String[] args)throws FrameworkException {
     try {
		 ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

         StringList slExcludedIds = new StringList();
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         String strObjectId = (String)programMap.get("objectId");
         String strRelationship = (String)programMap.get("relation");
         DebugUtil.debug("Inside the exlude strRelationship:"+strRelationship);
         DebugUtil.debug("Inside the exlude strObjectId:"+strObjectId);
         if (strObjectId == null || "".equals(strObjectId)){
             throw new FrameworkException ("Does not has valid Obect for connection ");
         }
         else {
             DomainObject dmObject = newInstance (context, strObjectId);
             if (DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT.equals(strRelationship)) {
                 slExcludedIds = dmObject.getInfoList(context, "from["+ DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT +"].to.id");
                 DebugUtil.debug("Inside the exlude MEP:"+slExcludedIds);
                 if(slExcludedIds.size()==0)
                 	slExcludedIds = dmObject.getInfoList(context, "to["+ DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT +"].from.id");
                 	DebugUtil.debug("Inside the exlude MEP:"+slExcludedIds);
                 slExcludedIds.add(strObjectId);
             	}
             else if(CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT.equals(strRelationship)){
				 slExcludedIds = dmObject.getInfoList(context, "from["+ CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT +"].to.id");
				 DebugUtil.debug("Inside the exlude SEP:"+slExcludedIds);
				 if(slExcludedIds.size()==0)
					 	slExcludedIds = dmObject.getInfoList(context, "to["+ CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT +"].from.id");
					DebugUtil.debug("Inside the exlude SEP:11"+slExcludedIds);
					slExcludedIds.add(strObjectId);
			 	}
			 else if(CPCConstants.RELATIONSHIP_CLASSIFIED_ITEM.equals(strRelationship)){
				 	slExcludedIds = dmObject.getInfoList(context, "from["+ CPCConstants.RELATIONSHIP_CLASSIFIED_ITEM +"].to.id");
				 	DebugUtil.debug("Inside the exlude LBC:"+slExcludedIds);
			 }
            }
         return slExcludedIds;
     }
     catch (Exception e){
         throw new FrameworkException(e);
     }
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
                    .getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_interface_ExternalPartData);
            String sObjectId = MqlUtil.mqlCommand(context, "get env OBJECTID");
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
                	MqlUtil.mqlCommand(context,"modify bus $1 add interface $2",sObjectId,sExternalPartData);                	
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
        HashMap paramMap = (HashMap) programMap.get("paramList");

        Vector columnValues = new Vector(relBusObjPageList.size());

        String bArr[] = new String[relBusObjPageList.size()];
        StringList bSel = new StringList();

        // Get the required parameter values from "paramMap"
        String languageStr = context.getSession().getLanguage();

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
            String strManufacturerEquivalent = i18nNow.getI18nString(
                    "emxManufacturerEquivalentPart.Part.MfgEquivalent",
                    "emxManufacturerEquivalentPartStringResource", languageStr);
            String strEnterprise = i18nNow.getI18nString(
                    "emxManufacturerEquivalentPart.Common.Enterprise",
                    "emxManufacturerEquivalentPartStringResource", languageStr);

            for (int i = 0; i < bwsl.size(); i++) {
                String propertyValue = bwsl.getElement(i).getSelectData(
                        "policy.property[PolicyClassification].value");

                // Build the Vector "columnValues" with the list of values to be
                // displayed in the column
                if (propertyValue.equals("Equivalent")) {
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
        StringBuffer resultBuffer = new StringBuffer();
        String typePart = PropertyUtil.getSchemaProperty(context, "type_Part");
        StringList policyList = Part.getEquivalentPolicies(context, typePart);
        String languageStr = context.getSession().getLanguage();
        StringList policyViewI18NStr = i18nNow.getAdminI18NStringList("Policy", policyList, languageStr);

       resultBuffer
        .append("<script language=\"javascript\" src=\"../manufacturerequivalentpart/scripts/emxMEPFormValidation.js\"></script>");
        resultBuffer.append("<select name=\"Policy\" id=\"PolicyId\" onChange=\"javascript:loadRevision();\">");
        for (int polItr = 0; polItr < policyList.size(); polItr++) {
        resultBuffer.append("<option value=\"");
        resultBuffer.append(policyList.get(polItr) + "\">");

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
       String inclusionList="type_Organization";
       String defaultType = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Company);
       if(isManufacturingLocation!=null && "Yes".equals(isManufacturingLocation)){
          inclusionList = "type_Location";
          defaultType = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Location);
       }
       if("true".equals(clearManuLoc)){
           inclusionList="type_Company,type_BusinessUnit";
           defaultType = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Company);
       }
       String strURL = "../common/emxTypeChooser.jsp?form=emxCreateForm&fieldNameActual=TypeDisplay&fieldNameDisplay=type&InclusionList="+inclusionList;
       StringBuffer returnString = new StringBuffer();
       returnString.append("<input type=\"text\" name=\"TypeDisplay\"");
       returnString.append(" size=\"20\" value=\"" +defaultType+ "\"");
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
        StringBuffer outputStr = new StringBuffer();
        try {
            String revValue=getRevisionFieldValue(context);


                outputStr
                        .append("<input type=\"text\" readonly=\"readonly\" name=\"Revision\" value =\""+revValue+"\" size=\"20\"></input>");


                outputStr
                    .append("<input type=\"hidden\" name=\"rev\" value=\""+revValue+"\"></input>");





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
        StringBuffer outputStr = new StringBuffer();
        try {
            String custRevValue=getRevisionFieldValue(context);
                outputStr
                        .append("<input type=\"text\" name=\"CustomRevision\" value =\""+custRevValue+"\" size=\"20\"></input>");

            outputStr
                    .append("<input type=\"hidden\" name=\"customrev\" value=\""+custRevValue+"\"></input>");

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
                    .getProperty("emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            if (customRevision == null) {
                customRevision = "false";
            }
            if ("false".equals(customRevision)) {
                return new Boolean(true);
            } else {
                return new Boolean(false);
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
            .getProperty("emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            if (customRevision == null) {
                customRevision = "false";
            }
            if ("true".equals(customRevision)) {
                return new Boolean(true);
            } else {
                return new Boolean(false);
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
                    .getProperty("emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            String uniqueIdentifier = FrameworkProperties
                    .getProperty("emxManufacturerEquivalentPart.MEP.UniquenessIdentifier");
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
            int idx = 0;

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
                            revSeqValue.append(",");
                        }
                        revSeqValue.append(firstRevSeq);

                    }
                }
                idx++;
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
        String customRevision=(String)hMap.get("customRevision");
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

            String[] Idxvalues=revSeq.split(",");
            revValue=Idxvalues[0];
        }
        return revValue;
    }

    /**
     * Gets the Manufacturer Equivalent Parts attached to an Enterprise Part.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds a HashMap of the following entries:
     * objectId - a String containing the Enterprise Part id.
     * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
     * @throws Exception if the operation fails.
     * @since 10.5.
     */

     public MapList getEnterpriseSupplierEquivalents1 (Context context,String[] args)
            throws Exception
     {
		ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");

        MapList listLocEquivMEPs = new MapList();

        try
        {
            DomainObject partObj = DomainObject.newInstance(context,objectId);

            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);

            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

            StringBuffer typePattern = new StringBuffer(TYPE_PART);

            String relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"QSL1\"";

            listLocEquivMEPs = partObj.getRelatedObjects(context,
                                  CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,    // relationship pattern
                                  CPCConstants.TYPE_SEP,              // object pattern
                                  selectStmts,                 // object selects
                                  selectRelStmts,              // relationship selects
                                  false,                        // to direction
                                  true,                       // from direction
                                  (short) 1,                   // recursion level
                                  null,                        // object where clause
                                  relWhere);                        // relationship where clause

            DebugUtil.debug("listLocEquivMEPs::"+listLocEquivMEPs);
        }
        catch (FrameworkException Ex)
        {
             throw Ex;
        }

        //return equivList;
        return listLocEquivMEPs;
    }

     /**
      * Gets the Manufacturer Equivalent Parts attached to an Enterprise Part.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds a HashMap of the following entries:
      * objectId - a String containing the Enterprise Part id.
      * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
      * @throws Exception if the operation fails.
      * @since 10.5.
      */

	@com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getEnterpriseSupplierAllEquivalents (Context context,String[] args)
             throws Exception
      {
         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         String objectId = (String) paramMap.get("objectId");

         MapList listLocEquivMEPs = new MapList();

         try
         {
             DomainObject partObj = DomainObject.newInstance(context,objectId);

             StringList selectStmts = new StringList(4);
             selectStmts.addElement(SELECT_ID);
             selectStmts.addElement(SELECT_TYPE);
             selectStmts.addElement(SELECT_NAME);
             selectStmts.addElement(SELECT_REVISION);

             StringList selectRelStmts = new StringList(1);
             selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

             StringBuffer typePattern = new StringBuffer(TYPE_PART);

             String relWhere = "tomid[Qualification].from.id != \"\"";

             listLocEquivMEPs = partObj.getRelatedObjects(context,
                                   CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,    // relationship pattern
                                   CPCConstants.TYPE_SEP,              // object pattern
                                   selectStmts,                 // object selects
                                   selectRelStmts,              // relationship selects
                                   false,                        // to direction
                                   true,                       // from direction
                                   (short) 1,                   // recursion level
                                   null,                        // object where clause
                                   relWhere);                        // relationship where clause

             DebugUtil.debug("listLocEquivMEPs::"+listLocEquivMEPs);

         }
         catch (FrameworkException Ex)
         {
              throw Ex;
         }

         return listLocEquivMEPs;
     }

     /**
      *
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds a HashMap of the following entries:
      * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
      * @throws Exception if the operation fails.
      */
      public MapList getNonQualifiedSupplierEquivalents (Context context,String[] args)
             throws Exception
      {
		 ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         String objectId = (String) paramMap.get("objectId");

         MapList listLocEquivMEPs = new MapList();

         try
         {
             DomainObject partObj = DomainObject.newInstance(context,objectId);

             StringList selectStmts = new StringList(4);
             selectStmts.addElement(SELECT_ID);
             selectStmts.addElement(SELECT_TYPE);
             selectStmts.addElement(SELECT_NAME);
             selectStmts.addElement(SELECT_REVISION);

             StringList selectRelStmts = new StringList(1);
             selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

             StringBuffer typePattern = new StringBuffer(TYPE_PART);

             String relWhere = "!tomid[Qualification]";

             listLocEquivMEPs = partObj.getRelatedObjects(context,
                                   CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,    // relationship pattern
                                   CPCConstants.TYPE_SEP,              // object pattern
                                   selectStmts,                 // object selects
                                   selectRelStmts,              // relationship selects
                                   false,                        // to direction
                                   true,                       // from direction
                                   (short) 1,                   // recursion level
                                   null,                        // object where clause
                                   relWhere);                        // relationship where clause

             DebugUtil.debug("getNonQualifiedSupplierEquivalents::listLocEquivMEPs::"+listLocEquivMEPs);

         }
         catch (FrameworkException Ex)
         {
              throw Ex;
         }

         return listLocEquivMEPs;
     }

      /**
       * Gets the Manufacturer Equivalent Parts attached to an Enterprise Part.
       *
       * @param context the eMatrix <code>Context</code> object.
       * @param args holds a HashMap of the following entries:
       * objectId - a String containing the Enterprise Part id.
       * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
       * @throws Exception if the operation fails.
       * @since 10.5.
       */

       public MapList getEnterpriseSupplierEquivalents2 (Context context,String[] args)
              throws Exception
       {
		  ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

          HashMap paramMap = (HashMap)JPO.unpackArgs(args);
          String objectId = (String) paramMap.get("objectId");

          MapList listLocEquivMEPs = new MapList();

          try
          {
              DomainObject partObj = DomainObject.newInstance(context,objectId);

              StringList selectStmts = new StringList(4);
              selectStmts.addElement(SELECT_ID);
              selectStmts.addElement(SELECT_TYPE);
              selectStmts.addElement(SELECT_NAME);
              selectStmts.addElement(SELECT_REVISION);

              StringList selectRelStmts = new StringList(1);
              selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

              StringBuffer typePattern = new StringBuffer(TYPE_PART);

              String relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"QSL2\"";

              listLocEquivMEPs = partObj.getRelatedObjects(context,
                                    CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT,    // relationship pattern
                                    CPCConstants.TYPE_SEP,              // object pattern
                                    selectStmts,                 // object selects
                                    selectRelStmts,              // relationship selects
                                    false,                        // to direction
                                    true,                       // from direction
                                    (short) 1,                   // recursion level
                                    null,                        // object where clause
                                    relWhere);                        // relationship where clause

              DebugUtil.debug("listLocEquivMEPs::"+listLocEquivMEPs);

          }
          catch (FrameworkException Ex)
          {
               throw Ex;
          }

          return listLocEquivMEPs;
      }

     /**
      *
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds a HashMap of the following entries:
      * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
      * @throws Exception if the operation fails.
      */	  
	  @com.matrixone.apps.framework.ui.ProgramCallable
      public MapList getNonQualifiedManufacturerEquivalents (Context context,String[] args)
             throws Exception
      {
		 ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         String objectId = (String) paramMap.get("objectId");

         MapList listLocEquivMEPs = new MapList();

         try
         {
             DomainObject partObj = DomainObject.newInstance(context,objectId);

             StringList selectStmts = new StringList(4);
             selectStmts.addElement(SELECT_ID);
             selectStmts.addElement(SELECT_TYPE);
             selectStmts.addElement(SELECT_NAME);
             selectStmts.addElement(SELECT_REVISION);

             StringList selectRelStmts = new StringList(1);
             selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

             StringBuffer typePattern = new StringBuffer(TYPE_PART);

             String relWhere = "!tomid[Qualification]";

             listLocEquivMEPs = partObj.getRelatedObjects(context,
                                   RELATIONSHIP_MANUFACTURER_EQUIVALENT,    // relationship pattern
                                   typePattern.toString(),              // object pattern
                                   selectStmts,                 // object selects
                                   selectRelStmts,              // relationship selects
                                   false,                        // to direction
                                   true,                       // from direction
                                   (short) 1,                   // recursion level
                                   null,                        // object where clause
                                   relWhere);                        // relationship where clause

             DebugUtil.debug("getNonQualifiedManufacturerEquivalents::listLocEquivMEPs::"+listLocEquivMEPs);

         }
         catch (FrameworkException Ex)
         {
              throw Ex;
         }

         return listLocEquivMEPs;
     }

// EEQ: Changes

	  // To display
	@com.matrixone.apps.framework.ui.ProgramCallable
	  public MapList getEnterpriseManufacturerAllEquivalents (Context context,String[] args)
			 throws Exception
	  {
		 HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		 String objectId = (String) paramMap.get("objectId");

		 MapList listLocEquivMEPs = new MapList();

		 try
		 {
			 DomainObject partObj = DomainObject.newInstance(context,objectId);

			 StringList selectStmts = new StringList(4);
			 selectStmts.addElement(SELECT_ID);
			 selectStmts.addElement(SELECT_TYPE);
			 selectStmts.addElement(SELECT_NAME);
			 selectStmts.addElement(SELECT_REVISION);

			 StringList selectRelStmts = new StringList(1);
			 selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			 StringBuffer typePattern = new StringBuffer(TYPE_PART);

			 String relWhere = "tomid[Qualification].from.id != \"\"";

			 listLocEquivMEPs = partObj.getRelatedObjects(context,
								   RELATIONSHIP_MANUFACTURER_EQUIVALENT,    // relationship pattern
								   typePattern.toString(),              // object pattern
								   selectStmts,                 // object selects
								   selectRelStmts,              // relationship selects
								   false,                        // to direction
								   true,                       // from direction
								   (short) 1,                   // recursion level
								   null,                        // object where clause
								   relWhere);                        // relationship where clause

			 DebugUtil.debug("getEnterpriseManufacturerAllEquivalents::"+listLocEquivMEPs);

		 }
		 catch (FrameworkException Ex)
		 {
			  throw Ex;
		 }

		 return listLocEquivMEPs;
	 }
	
	 @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getEnterpriseManufacturerEquivalents1 (Context context,String[] args)
            throws Exception
     {
		ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");

        MapList listLocEquivMEPs = new MapList();

        try
        {
            DomainObject partObj = DomainObject.newInstance(context,objectId);

            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);

            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

            StringBuffer typePattern = new StringBuffer(TYPE_PART);

            String relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"QML1\"";

            listLocEquivMEPs = partObj.getRelatedObjects(context,
                                  RELATIONSHIP_MANUFACTURER_EQUIVALENT,    // relationship pattern
                                  typePattern.toString(),              // object pattern
                                  selectStmts,                 // object selects
                                  selectRelStmts,              // relationship selects
                                  false,                        // to direction
                                  true,                       // from direction
                                  (short) 1,                   // recursion level
                                  null,                        // object where clause
                                  relWhere);                        // relationship where clause

            DebugUtil.debug("getEnterpriseManufacturerEquivalents1::"+listLocEquivMEPs);
        }
        catch (FrameworkException Ex)
        {
             throw Ex;
        }

        return listLocEquivMEPs;
    }

	@com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getEnterpriseManufacturerEquivalents2 (Context context,String[] args)
            throws Exception
     {
		ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");

        MapList listLocEquivMEPs = new MapList();

        try
        {
            DomainObject partObj = DomainObject.newInstance(context,objectId);

            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);

            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

            StringBuffer typePattern = new StringBuffer(TYPE_PART);

            String relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"QML2\"";

            listLocEquivMEPs = partObj.getRelatedObjects(context,
                                  RELATIONSHIP_MANUFACTURER_EQUIVALENT,    // relationship pattern
                                  typePattern.toString(),              // object pattern
                                  selectStmts,                 // object selects
                                  selectRelStmts,              // relationship selects
                                  false,                        // to direction
                                  true,                       // from direction
                                  (short) 1,                   // recursion level
                                  null,                        // object where clause
                                  relWhere);                        // relationship where clause

            DebugUtil.debug("getEnterpriseManufacturerEquivalents2::"+listLocEquivMEPs);
        }
        catch (FrameworkException Ex)
        {
             throw Ex;
        }

        return listLocEquivMEPs;
    }

       /**
        * Gets the Manufacturer Equivalent Parts attached to an Enterprise Part.
        *
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds a HashMap of the following entries:
        * objectId - a String containing the Enterprise Part id.
        * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
        * @throws Exception if the operation fails.
        * @since 10.5.
        */

        public MapList getQualificationDetails (Context context,String[] args)
               throws Exception
        {
           HashMap paramMap = (HashMap)JPO.unpackArgs(args);
           String objectId = (String) paramMap.get("objectId");
           String relId = (String) paramMap.get("relId");
           DebugUtil.debug("getQualificationDetails::objectId "+objectId);
           DebugUtil.debug("getQualificationDetails::relId "+relId);

           MapList listLocEquivMEPs = new MapList();

           try
           {
               String[] suppEquivIds = new String[1];
               suppEquivIds[0] = relId;

               StringList relSelects = new StringList();
               relSelects.addElement(SELECT_ID);
               relSelects.addElement(SELECT_TYPE);
               relSelects.addElement(SELECT_NAME);
               relSelects.addElement(SELECT_REVISION);
               relSelects.addElement(SELECT_RELATIONSHIP_ID);
               relSelects.addElement("tomid[Qualification].from.attribute[Name]");
               relSelects.addElement("tomid[Qualification].from.attribute[Preference]");
               relSelects.addElement("tomid[Qualification].from.attribute[Preference Score]");
               relSelects.addElement("tomid[Qualification].from.attribute[Qualification Type ID]");
               relSelects.addElement("tomid[Qualification].from.attribute[Qualification Description]");
               relSelects.addElement("tomid[Qualification].from.attribute[Sourcing Model ID]");
               relSelects.addElement("tomid[Qualification].from.attribute[Sourcing Model Description]");

               listLocEquivMEPs = DomainRelationship.getInfo(context, suppEquivIds, relSelects);
               DebugUtil.debug("getQualificationDetails:"+listLocEquivMEPs);
           }
           catch (FrameworkException Ex)
           {
                throw Ex;
           }

           return listLocEquivMEPs;
       }

	  /**
		* Show the Supplier Location field.
		*
		* @param context
		*            the eMatrix <code>Context</code> object.
		* @param args
		*            holds a packed HashMap.
		* @return a html String to show the Supplier location field.
		* @throws Exception
		*             if the operation fails.
		*/
	    public String showSupplierLocationField(Context context, String[] args)
	               throws Exception {
			   DebugUtil.debug("showSupplierLocationField");
			   HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			   //HashMap requestMap = (HashMap)paramMap.get("requestMap");
			   String objectId = (String) paramMap.get("objectId");
			   StringBuffer outPut = new StringBuffer();

			   DomainObject domSepEqvPartID = DomainObject.newInstance(context, objectId);
			   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			   String relationSuppRes = CPCConstants.RELATIONSHIP_SUPPLIED_BY;
			   Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

			   if (relationshipMap != null && relationshipMap.size() > 0){
			     	String supplierID = (String) relationshipMap.get("id");
			     	DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
			     	String relationOrgLoc = PropertyUtil.getSchemaProperty(context,"relationship_OrganizationLocation");
			   		String typePattern = "*";
			   		StringList selectStmts = new StringList();
			   		selectStmts.add(DomainConstants.SELECT_ID);

			    	MapList relListSE = domSupplierID.getRelatedObjects(context,
			     			relationOrgLoc, //relationship Pattern
			   		  		DomainConstants.TYPE_LOCATION, //type Pattern
			   		  		selectStmts, //objectSelects
			   		  		relSelects, //relSelects
			   		  		false, //getTo
			   		  		true, //getFrom
			   		  		(short) 1, //recurse
			   		  		null, //objectWhere
			   		  		null //relWhere
			   		  		);

					outPut.append("<table>");
			    	outPut.append("<select name=\"Location\">");
			    	String Loc="";
			    	String relLocID ="";
			   		if(relListSE != null){
						Iterator itrRel = relListSE.iterator();
							outPut.append("<option value=\"");
							outPut.append("Not Selected");
							outPut.append("\">");
							outPut.append("Not Selected");
			   				outPut.append("</option>");
			   			while (itrRel.hasNext()){
			   				Hashtable relRecord = (Hashtable) itrRel.next();
			   				relLocID = (String) relRecord.get("id[connection]");
			   				String locationID = (String) relRecord.get("id");
			   				//DebugUtil.debug("JPO locationID "+locationID);
			   				DomainObject domLocObj=new DomainObject(locationID);
							Loc = domLocObj.getInfo(context,DomainConstants.SELECT_NAME);
							outPut.append("<option value=\"");
							outPut.append(Loc);
							outPut.append("\">");
							outPut.append(Loc);
			   				outPut.append("</option>");
			   				}
			   		 }
			   		 outPut.append("</select>");

			   		 outPut.append("</table>");
  				}
	           		return outPut.toString();
     		}


     	/**
		* Show the Supplier field.
		*
		* @param context
		*            the eMatrix <code>Context</code> object.
		* @param args
		*            holds a packed HashMap.
		* @return a html String to show the Supplier field.
		* @throws Exception
		*             if the operation fails.
		*/
		public String showSupplierField(Context context, String[] args)
				   throws Exception {
		   DebugUtil.debug("showSupplierField");

		   HashMap programMap = (HashMap)JPO.unpackArgs(args);
		   HashMap requestMap = (HashMap)programMap.get("requestMap");

		   DebugUtil.debug("showSupplierField::requestMap "+requestMap);

		   String objectId = (String) requestMap.get("objectId");
		   StringBuffer outPut = new StringBuffer();

		   DebugUtil.debug("showSupplierField::objectId "+objectId);

		   DomainObject domSepEqvPartID = DomainObject.newInstance(context, objectId);
		   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

		   String relationSuppRes = CPCConstants.RELATIONSHIP_SUPPLIED_BY;
		   Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

		   String supplierName = null;
		   if (relationshipMap != null && relationshipMap.size() > 0){
				String supplierID = (String) relationshipMap.get("id");
				DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
				supplierName = domSupplierID.getInfo(context,DomainConstants.SELECT_NAME);
			}

		  return supplierName;
		}

		public String showSupplierField1(Context context, String[] args)
				   throws Exception {
			String result = null;

			   DebugUtil.debug("showSupplierField1");
			   DebugUtil.debug("showSupplierField1");
			   HashMap requestMap = (HashMap)JPO.unpackArgs(args);
			   DebugUtil.debug("showSupplierField1::requestMap:"+requestMap);
			   HashMap paramMap = (HashMap)requestMap.get("paramMap");
			   String objectId = (String) paramMap.get("objectId");
			   //String vepId = (String) paramMap.get("relId");
			   //String vepId = MqlUtil.mqlCommand(context, "print bus "+ objectId + " select from[Qualification].torel[Supplier Equivalent].to.id dump");
			   String mqlCmdTxt = "print bus $1 select $2 dump";
			   String selectFieldValue = "from[Qualification].torel[Supplier Equivalent].to.id";
			   String vepId = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);

			   DebugUtil.debug("showSupplierField1::paramMap:"+paramMap);
			   DebugUtil.debug("showSupplierField1::objectId:"+vepId);
			   StringBuffer outPut = new StringBuffer();

			   //DomainObject domSepEqvPartID = DomainObject.newInstance(context, objectId);
			   DomainObject domSepEqvPartID = DomainObject.newInstance(context, vepId);
			   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			   String relationSuppRes = CPCConstants.RELATIONSHIP_SUPPLIED_BY;
			   Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

			   if (relationshipMap != null && relationshipMap.size() > 0){
					String supplierID = (String) relationshipMap.get("id");
					DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
					String supplierName = domSupplierID.getInfo(context,DomainConstants.SELECT_NAME);
					//outPut.append("<input type=\"text\" name=\"Supplier\"");
					//outPut.append(" size=\"20\" value=\"");
					outPut.append(supplierName);
					//outPut.append("\" readonly=\"readonly\">");
					//outPut.append("</input>");
				}
				DebugUtil.debug("showSupplierField1::outPut:"+outPut.toString());
				//String tmp1 = "<a "+outPut.toString()+"</a>"
				//result.add((String)tmp1);
				result = outPut.toString();

				return result;
		}
	  /**
		* Show the Supplier Location field.
		*
		* @param context
		*            the eMatrix <code>Context</code> object.
		* @param args
		*            holds a packed HashMap.
		* @return a html String to show the Supplier location field.
		* @throws Exception
		*             if the operation fails.
		*/
	    public HashMap showSupplierLocationField1(Context context, String[] args)
	               throws Exception {
			String result = null;

			   DebugUtil.debug("showSupplierLocationField");
			   HashMap requestMap = (HashMap)JPO.unpackArgs(args);
			   HashMap paramMap = (HashMap)requestMap.get("paramMap");
			   String objectId = (String) paramMap.get("objectId");

			   //String vepId = (String) paramMap.get("relId");
			   //String vepId = MqlUtil.mqlCommand(context, "print bus "+ objectId + " select from[Qualification].torel[Supplier Equivalent].to.id dump");

		    String mqlCmdTxt = "print bus $1 select $2 dump";
		    String selectFieldValue = "from[Qualification].torel[Supplier Equivalent].to.id";
		    String vepId = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);

			   DebugUtil.debug("showSupplierLocationField1::paramMap:"+paramMap);
			   DebugUtil.debug("showSupplierLocationField1::objectId:"+vepId);
			   StringBuffer outPut = new StringBuffer();

				// initialize the return variable HashMap tempMap = new HashMap();
				HashMap tempMap = new HashMap();

				// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
				StringList fieldRangeValues = new StringList();
				StringList fieldDisplayRangeValues = new StringList();

				// Process information to obtain the range values and add them to fieldRangeValues
				// Get the internationlized value of the range values and add them to fieldDisplayRangeValues

			   DomainObject domSepEqvPartID = DomainObject.newInstance(context, vepId);
			   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			   String relationSuppRes = CPCConstants.RELATIONSHIP_SUPPLIED_BY;
			   Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

			   if (relationshipMap != null && relationshipMap.size() > 0){
			     	String supplierID = (String) relationshipMap.get("id");
			     	DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
			     	String relationOrgLoc = PropertyUtil.getSchemaProperty(context,"relationship_OrganizationLocation");
			   		String typePattern = "*";
			   		StringList selectStmts = new StringList();
			   		selectStmts.add(DomainConstants.SELECT_ID);

			    	MapList relListSE = domSupplierID.getRelatedObjects(context,
			     			relationOrgLoc, //relationship Pattern
			   		  		DomainConstants.TYPE_LOCATION, //type Pattern
			   		  		selectStmts, //objectSelects
			   		  		relSelects, //relSelects
			   		  		false, //getTo
			   		  		true, //getFrom
			   		  		(short) 1, //recurse
			   		  		null, //objectWhere
			   		  		null //relWhere
			   		  		);

					outPut.append("<table>");
			    	outPut.append("<select name=\"Location\">");
			    	String Loc="";
			    	String relLocID ="";
			   		if(relListSE != null){
						Iterator itrRel = relListSE.iterator();
						fieldRangeValues.addElement("Not Selected");
						fieldDisplayRangeValues.addElement("Not Selected");

							outPut.append("<option value=\"");
							outPut.append("Not Selected");
							outPut.append("\">");
							outPut.append("Not Selected");
			   				outPut.append("</option>");
			   			while (itrRel.hasNext()){
			   				Hashtable relRecord = (Hashtable) itrRel.next();
			   				relLocID = (String) relRecord.get("id[connection]");
			   				String locationID = (String) relRecord.get("id");
			   				//DebugUtil.debug("JPO locationID "+locationID);
			   				DomainObject domLocObj=new DomainObject(locationID);
							Loc = domLocObj.getInfo(context,DomainConstants.SELECT_NAME);
							outPut.append("<option value=\"");
							outPut.append(Loc);
							outPut.append("\">");
							outPut.append(Loc);
			   				outPut.append("</option>");

			   				fieldRangeValues.addElement(Loc);
							fieldDisplayRangeValues.addElement(Loc);
			   				}
			   		 }
			   		 outPut.append("</select>");

			   		 outPut.append("</table>");
  				}
  				//DebugUtil.debug("showSupplierLocationField1::outPut:"+outPut.toString());

				//result.add((String)"<td>"+outPut.toString()+"</td>");
				result = outPut.toString();

				DebugUtil.debug("showSupplierLocationField1::fieldRangeValues:"+fieldRangeValues+" "+
									fieldDisplayRangeValues);

				tempMap.put("field_choices", fieldRangeValues);
				tempMap.put("field_display_choices", fieldDisplayRangeValues);

				return tempMap;
				//return result;
     	}

		/**
		 * Gets the Supplier Equivalent Parts attached to the child Enterprise Parts in aa EBOM.
		 *
		 * @param context
		 *            the eMatrix <code>Context</code> object.
		 * @param args
		 *            holds a HashMap of the following entries: objectId - a String
		 *            containing the Enterprise Part id.
		 * @return a MapList of Supplier Equivalent Part object ids and
		 *         relationship ids.
		 * @throws Exception
		 *             if the operation fails.
		 */
	 	public MapList getBOMSEP(Context context, String[] args)
		    	throws Exception {
			DebugUtil.debug("inside getBOMSEP");
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			//DebugUtil.debug("paramMap "+paramMap);

			String objectId = (String) paramMap.get("objectId");
			String ebomRelID = (String) paramMap.get("ebomRelID");
			DebugUtil.debug("ebomRelID "+ebomRelID);
			String isMPN = (String) paramMap.get("isMPN");

			MapList equivList = new MapList();

			MapList listLocEquivMEPs = new MapList();
			MapList listCorpMEPs = new MapList();

			String vepType = (String) paramMap.get("vepType");
			DebugUtil.debug("getBOMSEP::vepType:" +vepType);

			try {

				//DomainObject partObj = DomainObject.newInstance(context, objectId);
				if(ebomRelID==null || "null".equals(ebomRelID) || ebomRelID.length()==0 )
				{
					return equivList;
				}

				DomainObject partObj=null;
				DomainRelationship domRel = new DomainRelationship(ebomRelID);
				domRel.open(context);
				BusinessObject busChildObj = domRel.getTo();
				DebugUtil.debug("busChildObj "+busChildObj);
				partObj = new DomainObject(busChildObj);

				//partObj = new DomainObject(childId);
				StringList selectStmts = new StringList(4);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_TYPE);
				selectStmts.addElement(SELECT_NAME);
				selectStmts.addElement(SELECT_REVISION);

				StringList selectRelStmts = new StringList(1);
				selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

				//StringBuffer typePattern = new StringBuffer(TYPE_PART);
				StringBuffer typePattern = null;
				String relToUse = null;

				if ( vepType.equals("bommepType") ) {
				  typePattern = new StringBuffer(TYPE_PART);
				  relToUse = RELATIONSHIP_MANUFACTURER_EQUIVALENT;
				}
				else {
				  typePattern = new StringBuffer(CPCConstants.TYPE_SEP);
				  relToUse = CPCConstants.RELATIONSHIP_SEP;
				}

				// fetching list of related SEPs
				DebugUtil.debug("Trying to retrieve the relation "+CPCConstants.RELATIONSHIP_SEP) ;
				listCorpMEPs = partObj.getRelatedObjects(context,
						relToUse, // relationship
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

				// Ids
				DebugUtil.debug("The size of the SEP list is "+listCorpMEPs.size());
				for (int i = 0; i < listCorpMEPs.size(); i++) {
					tempMap = (Map) listCorpMEPs.get(i);

					vecMepId.addElement(tempMap.get(SELECT_ID));
					mapMepId.put(tempMap.get(SELECT_ID), tempMap);
					vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
				}// end of for (listCorpMEPs)

				DebugUtil.debug("Doing something else") ;
				for (int k = 0; k < vecMepId.size(); k++) {
					Map resultMap = (Map) mapMepId.get((String) vecMepId
							.elementAt(k));
					resultMap.remove("level");// need to be removed , else show
					// message as - the level sequence
					// may not be as expected..
					resultMap.put(SELECT_RELATIONSHIP_ID, (String) vecRelId
							.elementAt(k));
					equivList.add(resultMap);
					}

			} catch (FrameworkException Ex) {
				throw Ex;
			}

				return equivList;
		}


		public void updateQualificationAttributes(Context context, String[] args)
		       throws Exception {
				 DebugUtil.debug("updateQualificationAttributes");
		        try {
		            HashMap programMap = (HashMap) JPO.unpackArgs(args);
		            Map paramMap = (Map) programMap.get("paramMap");
		            String sNewValue = (String) paramMap.get("New Value");
		            String objectId = (String) paramMap.get("objectId");

		            Map fieldMap = (Map) programMap.get("fieldMap");
		            Map settings = (Map) fieldMap.get("settings");
		            String sAttrSymName = (String) settings.get("Sym Name");

		            DomainObject objComp = new DomainObject(objectId);
		            String attrName = PropertyUtil.getSchemaProperty(context,
		                    sAttrSymName);
		            objComp.setAttributeValue(context, attrName, sNewValue);

		        } catch (Exception e) {
		            throw new FrameworkException(e.toString());
		        }
    	  }


		public void updateQualificationRelations(Context context, String[] args)
		       throws Exception {
				 DebugUtil.debug("updateQualificationRelations");
		        try {
		            HashMap programMap = (HashMap) JPO.unpackArgs(args);
		            Map paramMap = (Map) programMap.get("paramMap");
		            String sNewValue = (String) paramMap.get("New Value");
		            String objectId = (String) paramMap.get("objectId");
		            String relId = (String) paramMap.get("relId");

		            Map fieldMap = (Map) programMap.get("fieldMap");
		            Map settings = (Map) fieldMap.get("settings");

		            String fieldName = (String) fieldMap.get("label");

		            if ( fieldName.equals("Choose Assembly") )
		            {
						DomainObject objComp = new DomainObject(objectId);
						String attrName = PropertyUtil.getSchemaProperty(context,
								"attribute_QualificationTypeID");
						objComp.setAttributeValue(context, attrName, "BQSL2");
					}

		            DebugUtil.debug("updateQualificationRelations::paramMap"+paramMap);
		            DebugUtil.debug("updateQualificationRelations::objectId"+objectId);
		            DebugUtil.debug("updateQualificationRelations::fieldMap"+fieldMap);
		            DebugUtil.debug("updateQualificationRelations::settings"+settings);
		        } catch (Exception e) {
		            throw new FrameworkException(e.toString());
		        }
    	  }
		public void processQualificationDetails(Context context, String[] args) throws Exception
		{
			try
			{
				DebugUtil.debug("processQualificationDetails() called");

				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				Map paramMap = (Map) programMap.get("paramMap");
				HashMap requestMap = (HashMap) paramMap.get("requestMap");
				DebugUtil.debug("paramMap::"+paramMap);
				DebugUtil.debug("requestMap::"+requestMap);

			} catch (Exception e) {
				throw new FrameworkException(e.toString());
			}
		}

		/**
		* Show the Manufacturer Locations.
		*
		* @param context
		*            the eMatrix <code>Context</code> object.
		* @param args
		*            holds a packed HashMap.
		* @return a html String to show the Supplier location field.
		* @throws Exception
		*             if the operation fails.
		*/
		public StringList showMfrLocations(Context context, String[] args)
			   throws Exception {

		   DebugUtil.debug("showMfrLocations");
		   HashMap programMap = (HashMap)JPO.unpackArgs(args);
		   HashMap requestMap = (HashMap)programMap.get("requestMap");

		   String objectId = (String) requestMap.get("objectId");

		   StringList result = new StringList();

		   DomainObject domSepEqvPartID = DomainObject.newInstance(context, objectId);
		   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
		   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

		   String relationSuppRes = PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingResponsibility");
		   Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

		   if (relationshipMap != null && relationshipMap.size() > 0){
				String supplierID = (String) relationshipMap.get("id");
				DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
				String relationOrgLoc = PropertyUtil.getSchemaProperty(context,"relationship_OrganizationLocation");
				String typePattern = "*";
				StringList selectStmts = new StringList();
				selectStmts.add(DomainConstants.SELECT_ID);

				MapList relListSE = domSupplierID.getRelatedObjects(context,
						relationOrgLoc, //relationship Pattern
						DomainConstants.TYPE_LOCATION, //type Pattern
						selectStmts, //objectSelects
						relSelects, //relSelects
						false, //getTo
						true, //getFrom
						(short) 1, //recurse
						null, //objectWhere
						null //relWhere
						);

				String Loc=""; String relLocID ="";
				result.add("Not Selected");
				if(relListSE != null){
					Iterator itrRel = relListSE.iterator();
					while (itrRel.hasNext()){
						Hashtable relRecord = (Hashtable) itrRel.next();
						relLocID = (String) relRecord.get("id[connection]");
						String locationID = (String) relRecord.get("id");
						DomainObject domLocObj=new DomainObject(locationID);
						Loc = domLocObj.getInfo(context,DomainConstants.SELECT_NAME);
						result.add(Loc);
					}
				 }
			}
			DebugUtil.debug("showMfrLocations::result "+result);
			return result;
		}


		/**
		* Show the Manufacturer field.
		*
		* @param context
		*            the eMatrix <code>Context</code> object.
		* @param args
		*            holds a packed HashMap.
		* @return a html String to show the Supplier field.
		* @throws Exception
		*             if the operation fails.
		*/
		public String showMfr(Context context, String[] args)
				   throws Exception {

			DebugUtil.debug("showMfr");

			HashMap programMap = (HashMap)JPO.unpackArgs(args);
		    HashMap requestMap = (HashMap)programMap.get("requestMap");
		    DebugUtil.debug("showMfr::requestMap "+requestMap);

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) requestMap.get("objectId");

			DebugUtil.debug("showMfr::objectID "+objectId);

			DomainObject domSepEqvPartID = DomainObject.newInstance(context, objectId);
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			String relationSuppRes = PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingResponsibility");
			Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

			DebugUtil.debug("showMfr::relationshipMap "+relationshipMap);

			String mfrName = null;
			if (relationshipMap != null && relationshipMap.size() > 0){
				String supplierID = (String) relationshipMap.get("id");
				DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
				mfrName = domSupplierID.getInfo(context,DomainConstants.SELECT_NAME);
			}
			DebugUtil.debug("showMfr::mfrName "+mfrName);

			return mfrName;
		}

		/**
		* Show the Supplier Locations.
		*
		* @param context
		*            the eMatrix <code>Context</code> object.
		* @param args
		*            holds a packed HashMap.
		* @return a html String to show the Supplier location field.
		* @throws Exception
		*             if the operation fails.
		*/
		public StringList showSupplierLocations(Context context, String[] args)
			   throws Exception {

			StringList result = new StringList();

		    DebugUtil.debug("showSupplierLocations");
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get("requestMap");

			DebugUtil.debug("showSupplierLocations::requestMap "+requestMap);

			String objectId = (String) requestMap.get("objectId");
			StringBuffer outPut = new StringBuffer();

			DebugUtil.debug("showSupplierLocations::objectId "+objectId);

			DomainObject domSepEqvPartID = DomainObject.newInstance(context, objectId);
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			String relationSuppRes = CPCConstants.RELATIONSHIP_SUPPLIED_BY;
			Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

			MapList relListSE = null;
			if (relationshipMap != null && relationshipMap.size() > 0){
				String supplierID = (String) relationshipMap.get("id");
				DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
				String relationOrgLoc = PropertyUtil.getSchemaProperty(context,"relationship_OrganizationLocation");
				String typePattern = "*";
				StringList selectStmts = new StringList();
				selectStmts.add(DomainConstants.SELECT_ID);

				relListSE = domSupplierID.getRelatedObjects(context,
						relationOrgLoc, //relationship Pattern
						DomainConstants.TYPE_LOCATION, //type Pattern
						selectStmts, //objectSelects
						relSelects, //relSelects
						false, //getTo
						true, //getFrom
						(short) 1, //recurse
						null, //objectWhere
						null //relWhere
						);

				String Loc=""; String relLocID ="";
				result.add("Not Selected");
				if(relListSE != null){
					Iterator itrRel = relListSE.iterator();
					while (itrRel.hasNext()){
						Hashtable relRecord = (Hashtable) itrRel.next();
						relLocID = (String) relRecord.get("id[connection]");
						String locationID = (String) relRecord.get("id");
						DomainObject domLocObj=new DomainObject(locationID);
						Loc = domLocObj.getInfo(context,DomainConstants.SELECT_NAME);
						result.add(Loc);
					}
				 }
			}
			DebugUtil.debug("showSupplierLocations::result "+result);

			return result;
		}

		public MapList getEnterpriseBOMSupplierAllEquivalents (Context context,String[] args)
			 throws Exception
		{
			DebugUtil.debug("inside getEnterpriseBOMSupplierAllEquivalents ");

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");
			String isMPN = (String) paramMap.get("isMPN");

			MapList equivList = new MapList();

			MapList listLocEquivMEPs = new MapList();

			String ebomRelID = (String) paramMap.get("ebomRelID");
			DebugUtil.debug("ebomRelID " +ebomRelID);

			String vepType = (String) paramMap.get("vepType");
			DebugUtil.debug("getEnterpriseBOMSupplierAllEquivalents::vepType:" +vepType);

			if(ebomRelID==null || "null".equals(ebomRelID) || ebomRelID.length()==0 )
			{
				return equivList;
			}

			DomainObject partObj=null;

			DomainRelationship domRel = new DomainRelationship(ebomRelID);
			domRel.open(context);
			BusinessObject busChildObj = domRel.getTo();
			DebugUtil.debug("busChildObj "+busChildObj);
			partObj = new DomainObject(busChildObj);

			try
			{
			 StringList selectStmts = new StringList(4);
			 selectStmts.addElement(SELECT_ID);
			 selectStmts.addElement(SELECT_TYPE);
			 selectStmts.addElement(SELECT_NAME);
			 selectStmts.addElement(SELECT_REVISION);

			 StringList selectRelStmts = new StringList(1);
			 selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			 StringBuffer sbRelPattern = new StringBuffer(CPCConstants.RELATIONSHIP_SEP);
			   sbRelPattern.append(",");
			   sbRelPattern.append(CPCConstants.RELATIONSHIP_QUALIFICATION);

			 //StringBuffer typePattern = new StringBuffer(TYPE_SEP);
			 StringBuffer typePattern = null;
			 String relToUse = null;

			 if ( vepType.equals("bommepType") ) {
				  typePattern = new StringBuffer(TYPE_PART);
				  relToUse = RELATIONSHIP_MANUFACTURER_EQUIVALENT;
			 }
			 else {
				  typePattern = new StringBuffer(CPCConstants.TYPE_SEP);
				  relToUse = CPCConstants.RELATIONSHIP_SEP;
			 }

			 String relWhere = "tomid[Qualification].from.id != \"\"";

			 listLocEquivMEPs = partObj.getRelatedObjects(context,
								   relToUse,    // relationship pattern
			//                                   sbRelPattern.toString() ,              // relationship pattern
								   typePattern.toString(),              // object pattern
								   selectStmts,                 // object selects
								   selectRelStmts,              // relationship selects
								   false,                        // to direction
								   true,                       // from direction
								   (short) 1,                   // recursion level
								   null,                        // object where clause
								   relWhere);                        // relationship where clause

			 DebugUtil.debug("listLocEquivMEPs::"+listLocEquivMEPs);

			}
			catch (FrameworkException Ex)
			{
			  throw Ex;
			}

			return listLocEquivMEPs;
		}


		public MapList getEnterpriseBOMSupplierEquivalents1 (Context context,String[] args)
			throws Exception
		{
			DebugUtil.debug("inside getEnterpriseBOMSupplierEquivalents1");

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");
			String isMPN = (String) paramMap.get("isMPN");
			String ebomRelID = (String) paramMap.get("ebomRelID");
			DebugUtil.debug("ebomRelID " +ebomRelID);

			MapList equivList = new MapList();

			MapList listLocEquivMEPs = new MapList();

			String vepType = (String) paramMap.get("vepType");
			DebugUtil.debug("getEnterpriseBOMSupplierEquivalents1::vepType:" +vepType);

			if(ebomRelID==null || "null".equals(ebomRelID) || ebomRelID.length()==0 )
			{
				return equivList;
			}

			DomainObject partObj=null;

			DomainRelationship domRel = new DomainRelationship(ebomRelID);
			domRel.open(context);
			BusinessObject busChildObj = domRel.getTo();
			DebugUtil.debug("busChildObj "+busChildObj);
			partObj = new DomainObject(busChildObj);

			try
			{
				StringList selectStmts = new StringList(4);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_TYPE);
				selectStmts.addElement(SELECT_NAME);
				selectStmts.addElement(SELECT_REVISION);

				StringList selectRelStmts = new StringList(1);
				selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

				 //StringBuffer typePattern = new StringBuffer(TYPE_SEP);
				 StringBuffer typePattern = null;
				 String relToUse = null;
				 String relWhere = null;

				 if ( vepType.equals("bommepType") ) {
					  typePattern = new StringBuffer(TYPE_PART);
					  relToUse = RELATIONSHIP_MANUFACTURER_EQUIVALENT;
					  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQML1\"";
				 }
				 else {
					  typePattern = new StringBuffer(CPCConstants.TYPE_SEP);
					  relToUse = CPCConstants.RELATIONSHIP_SEP;
					  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQSL1\"";
				 }

				listLocEquivMEPs = partObj.getRelatedObjects(context,
									  relToUse,    // relationship pattern
			//                                  sbRelPattern.toString() ,              // relationship pattern
									  typePattern.toString(),              // object pattern
									  selectStmts,                 // object selects
									  selectRelStmts,              // relationship selects
									  false,                        // to direction
									  true,                       // from direction
									  (short) 1,                   // recursion level
									  null,                        // object where clause
									  relWhere);                        // relationship where clause

				DebugUtil.debug("listLocEquivMEPs::"+listLocEquivMEPs);
			}
			catch (FrameworkException Ex)
			{
				 throw Ex;
			}

			return listLocEquivMEPs;
		}

		public MapList getEnterpriseBOMSupplierEquivalents2 (Context context,String[] args)
							 throws Exception
		{
			DebugUtil.debug("inside getEnterpriseBOMSupplierEquivalents2");

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");

			String ebomRelID = (String) paramMap.get("ebomRelID");
			DebugUtil.debug("ebomRelID " +ebomRelID);

			String isMPN = (String) paramMap.get("isMPN");

			MapList equivList = new MapList();

			MapList listLocEquivMEPs = new MapList();

			String vepType = (String) paramMap.get("vepType");
			DebugUtil.debug("getEnterpriseBOMSupplierEquivalents2::vepType:" +vepType);

			if(ebomRelID==null || "null".equals(ebomRelID) || ebomRelID.length()==0 ){
				return equivList;
			}

			DomainObject partObj=null;

			DomainRelationship domRel = new DomainRelationship(ebomRelID);
			domRel.open(context);
			BusinessObject busChildObj = domRel.getTo();
			DebugUtil.debug("busChildObj "+busChildObj);
			partObj = new DomainObject(busChildObj);

			try
			{
				StringList selectStmts = new StringList(4);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_TYPE);
				selectStmts.addElement(SELECT_NAME);
				selectStmts.addElement(SELECT_REVISION);

				StringList selectRelStmts = new StringList(1);
				selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

				StringBuffer typePattern = null;
				String relToUse = null;
				String relWhere = null;

				if ( vepType.equals("bommepType") ) {
				  typePattern = new StringBuffer(TYPE_PART);
				  relToUse = RELATIONSHIP_MANUFACTURER_EQUIVALENT;
				  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQML2\"";
				}
				else {
				  typePattern = new StringBuffer(CPCConstants.TYPE_SEP);
				  relToUse = CPCConstants.RELATIONSHIP_SEP;
				  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQSL2\"";
				}

				listLocEquivMEPs = partObj.getRelatedObjects(context,
									  relToUse,    // relationship pattern
									  typePattern.toString(),              // object pattern
									  selectStmts,                 // object selects
									  selectRelStmts,              // relationship selects
									  false,                        // to direction
									  true,                       // from direction
									  (short) 1,                   // recursion level
									  null,                        // object where clause
									  relWhere);                        // relationship where clause

				DebugUtil.debug("listLocEquivMEPs::"+listLocEquivMEPs);

			}
			catch (FrameworkException Ex)
			{
				 throw Ex;
			}

		  return listLocEquivMEPs;
		}

		public StringList getAssembliesForPart (Context context, String[] args)
			throws Exception
		{
			StringList result = new StringList();

			DebugUtil.debug("Inside the JPO: getAssembliesForPart");
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			DebugUtil.debug("getAssembliesForPart:programMap "+programMap);
			HashMap requestMap = (HashMap)programMap.get("requestMap");
//			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		    String partId = (String) requestMap.get("parentOID");
			DebugUtil.debug("getAssembliesForPart:requestMap "+requestMap);
			DebugUtil.debug("getAssembliesForPart:partId "+partId);
			MapList ebomList = new MapList();

			if ( null != partId )
			{
				com.matrixone.apps.common.Part partObj = new com.matrixone.apps.common.Part(partId);
				StringList selectStmts = new StringList(2);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_NAME);
				StringList selectRelStmts = new StringList(2);
				selectRelStmts.addElement(KEY_LEVEL);
        		selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
				ebomList=partObj.getRelatedObjects(context,
								DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
								DomainConstants.TYPE_PART,                  // object pattern
								selectStmts,                 // object selects
								selectRelStmts,              // relationship selects
								true,                        // to direction
								false,                       // from direction
								(short)1,                    // recursion level
								null,                        // object where clause
								null);                       // relationship where clause

				DebugUtil.debug("getAssembliesForPart "+ebomList);
			}  // end-if

			String Loc=""; String relLocID ="";
			result.add("Not Selected");
			if(ebomList != null){
						Iterator itrRel = ebomList.iterator();
						while (itrRel.hasNext()){
							Hashtable relRecord = (Hashtable) itrRel.next();
							relLocID = (String) relRecord.get("id[connection]");
							String locationID = (String) relRecord.get("id");
							DomainObject domLocObj=new DomainObject(locationID);
							Loc = domLocObj.getInfo(context,DomainConstants.SELECT_NAME);
							result.add(Loc);
			}
		    }

		    DebugUtil.debug("getAssembliesForPart::result"+result);

			return result;
		}
		@com.matrixone.apps.framework.ui.ProgramCallable
		public MapList getEnterpriseBOMEquivalents1 (Context context,String[] args)
			throws Exception
		{
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");
			String vepType = (String) paramMap.get("vepType");
			DebugUtil.debug("getEnterpriseBOMEquivalents1::paramMap"+paramMap);

			DebugUtil.debug("getEnterpriseBOMEquivalents1::vepType"+vepType);

			MapList listLocEquivMEPs = new MapList();

			try
			{
				DomainObject partObj = DomainObject.newInstance(context,objectId);

				StringList selectStmts = new StringList(4);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_TYPE);
				selectStmts.addElement(SELECT_NAME);
				selectStmts.addElement(SELECT_REVISION);

				StringList selectRelStmts = new StringList(1);
				selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

				StringBuffer typePattern = null;
				String relToUse = null;
				String relWhere = null;

				if ( vepType.equals("bommepType") ) {
				  typePattern = new StringBuffer(TYPE_PART);
				  relToUse = RELATIONSHIP_MANUFACTURER_EQUIVALENT;
				  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQML1\"";
				}
				else {
				  typePattern = new StringBuffer(CPCConstants.TYPE_SEP);
				  relToUse = CPCConstants.RELATIONSHIP_SEP;
				  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQSL1\"";
				}

				listLocEquivMEPs = partObj.getRelatedObjects(context,
									  relToUse,    // relationship pattern
									  typePattern.toString(),              // object pattern
									  selectStmts,                 // object selects
									  selectRelStmts,              // relationship selects
									  false,                        // to direction
									  true,                       // from direction
									  (short) 1,                   // recursion level
									  null,                        // object where clause
									  relWhere);                        // relationship where clause

				DebugUtil.debug("getEnterpriseBOMEquivalents1::listLocEquivMEPs:"+listLocEquivMEPs);
			}
			catch (FrameworkException Ex)
			{
				 throw Ex;
			}

        	return listLocEquivMEPs;
		}
		
		@com.matrixone.apps.framework.ui.ProgramCallable
		public MapList getEnterpriseBOMEquivalents2 (Context context,String[] args)
			throws Exception
		{
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");
			String vepType = (String) paramMap.get("vepType");
			DebugUtil.debug("getEnterpriseBOMEquivalents1::paramMap"+paramMap);

			DebugUtil.debug("getEnterpriseBOMEquivalents1::vepType"+vepType);

			MapList listLocEquivMEPs = new MapList();

			try
			{
				DomainObject partObj = DomainObject.newInstance(context,objectId);

				StringList selectStmts = new StringList(4);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_TYPE);
				selectStmts.addElement(SELECT_NAME);
				selectStmts.addElement(SELECT_REVISION);

				StringList selectRelStmts = new StringList(1);
				selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

				StringBuffer typePattern = null;
				String relToUse = null;
				String relWhere = null;

				if ( vepType.equals("bommepType") ) {
				  typePattern = new StringBuffer(TYPE_PART);
				  relToUse = RELATIONSHIP_MANUFACTURER_EQUIVALENT;
				  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQML2\"";
				}
				else {
				  typePattern = new StringBuffer(CPCConstants.TYPE_SEP);
				  relToUse = CPCConstants.RELATIONSHIP_SEP;
				  relWhere = "tomid[Qualification].from.attribute[Qualification Type ID] == \"BQSL2\"";
				}

				listLocEquivMEPs = partObj.getRelatedObjects(context,
									  relToUse,    // relationship pattern
									  typePattern.toString(),              // object pattern
									  selectStmts,                 // object selects
									  selectRelStmts,              // relationship selects
									  false,                        // to direction
									  true,                       // from direction
									  (short) 1,                   // recursion level
									  null,                        // object where clause
									  relWhere);                        // relationship where clause

				DebugUtil.debug("getEnterpriseBOMEquivalents2::listLocEquivMEPs:"+listLocEquivMEPs);
			}
			catch (FrameworkException Ex)
			{
				 throw Ex;
			}

        	return listLocEquivMEPs;
		}
	public static HashMap getQualificationPreferences(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");

		// Get the required parameter values from "programMap" - as required
		String objectId = (String) paramMap.get("objectId ");
		String relId = (String) paramMap.get("relId ");
		String languageStr = (String) paramMap.get("languageStr");

		// initialize the return variable HashMap tempMap = new HashMap();
		HashMap tempMap = new HashMap();

		// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();

		// Process information to obtain the range values and add them to fieldRangeValues
		// Get the internationlized value of the range values and add them to fieldDisplayRangeValues
		fieldRangeValues.addElement("Preferred");
		fieldDisplayRangeValues.add(EnoviaResourceBundle.getProperty(context,"emxComponentCentralStringResource",new Locale(context.getSession().getLanguage()),"emxComponentCentral.Attribute.LocationPreference.Preferred"));
		//fieldDisplayRangeValues.addElement("Preferred");

		fieldRangeValues.addElement("Non-Preferred");
		//fieldDisplayRangeValues.addElement("Non-Preferred");
		fieldDisplayRangeValues.add(EnoviaResourceBundle.getProperty(context,"emxComponentCentralStringResource",new Locale(context.getSession().getLanguage()),"emxComponentCentral.Attribute.LocationPreference.NotPreferred"));

		tempMap.put("field_choices", fieldRangeValues);
		tempMap.put("field_display_choices", fieldDisplayRangeValues);

		return tempMap;
	}
// EEQ: Changes
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getEnterpriseParts(Context context,String args[]) throws Exception{

		ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);

		DebugUtil.debug("Inside the JPO:"+args.length);
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);

        String objectId = (String) paramMap.get("objectId");
        DebugUtil.debug("The object Id:getEnterpriseParts "+objectId);
        MapList equivList = new MapList();
        MapList corpEPs = new MapList();
		Map tempMap = null;
		Vector vecMepId = new Vector();
		HashMap mapMepId = new HashMap();
		Vector vecRelId = new Vector();

        try {
            Part partObj = new Part(objectId);
            StringList selectStmts = new StringList(4);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);
            selectStmts.addElement(SELECT_REVISION);
            String busWhere="(policy != \""+DomainConstants.POLICY_MANUFACTURER_EQUIVALENT +"\")";
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
            corpEPs = partObj.getRelatedObjects(context,
                    CPCConstants.RELATIONSHIP_SUPPLIER_EQUIVALENT +","+DomainConstants.RELATIONSHIP_MANUFACTURER_EQUIVALENT, // relationship
                    TYPE_PART, // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    true, // to direction
                    false, // from direction
                    (short) 1, // recursion level
                    null, // object where clause
                    null); // relationship where clause
		 for (int i = 0; i < corpEPs.size(); i++) {
                tempMap = (Map) corpEPs.get(i);
				DebugUtil.debug("Inside the JPO:tempMap "+tempMap);
                vecMepId.addElement(tempMap.get(SELECT_ID));
                mapMepId.put(tempMap.get(SELECT_ID), tempMap);
                vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
            }// end of for (listCorpMEPs)

            for (int k = 0; k < vecMepId.size(); k++) {
                Map resultMap = (Map) mapMepId.get((String) vecMepId
                        .elementAt(k));
                DebugUtil.debug("Inside the JPO:resultMap "+resultMap);
                resultMap.remove("level");// need to be removed , else show
                // message as - the level sequence
                // may not be as expected..
                resultMap.put(SELECT_RELATIONSHIP_ID, (String) vecRelId.elementAt(k));
                equivList.add(resultMap);
            }

		DebugUtil.debug("Inside the JPO:Parts; "+corpEPs.size());
        } catch (FrameworkException Ex) {
            throw Ex;
        }
        return equivList;

}


public MapList getMultiLevelEBOMsforBQSL (Context context,
                                   String[] args)
    throws Exception
{

    DebugUtil.debug("Inside the JPO: ");
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    String partId = (String) paramMap.get("objectId");
    String level = (String) paramMap.get("Level");
    Short SLevel = new Short(level);
    short shLevel = SLevel.shortValue();
    MapList ebomList = new MapList();
    try {
        com.matrixone.apps.common.Part partObj = new com.matrixone.apps.common.Part(partId);
        StringList selectStmts = new StringList(3);
        selectStmts.addElement(SELECT_ID);
        //selectStmts.addElement(SELECT_REQUEST_PART_REVISION_STATE); - Deprecated from DomainConstants and hence commented
        //selectStmts.addElement(SELECT_NEW_PART_PART_REVISION_STATE);- Deprecated from DomainConstants and hence commented
        StringList selectRelStmts = new StringList(8);
        selectRelStmts.addElement(KEY_LEVEL);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_TYPE);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
        String relWhere = ".tomid[Qualification].from.attribute[Qualification Type ID] == \"BQSL1\"";
        relWhere+="|| (tomid[Qualification].from.attribute[Qualification Type ID] == \"BQSL2\")";
        ebomList=partObj.getRelatedObjects(context,
		                DomainConstants.RELATIONSHIP_EBOM + "," + CPCConstants.RELATIONSHIP_QUALIFICATION,  // relationship pattern
		                DomainConstants.TYPE_PART, // object pattern
		                                         selectStmts,                 // object selects
		                                         selectRelStmts,              // relationship selects
		                                         false,                        // to direction
		                                         true,                       // from direction
		                                         (short)shLevel,                    // recursion level
		                                         null,                        // object where clause
		                                         relWhere);                       // relationship where clause
   		DebugUtil.debug("Inside the JPO:ebomList final "+ebomList);
    }
    catch (FrameworkException Ex) {
		Ex.printStackTrace();
        throw Ex;
    }

    return ebomList;

}

public Vector getQualificationDescription(Context context,String [] args) throws Exception{
	Vector qualDesc=new Vector();
	String qualificationId=null;
	StringBuffer strBuf = new StringBuffer();

	MapList objectList=new MapList();
	String qualifiedIds=null;
	try{
	    DebugUtil.debug("Inside the JPO:Desc ");
	    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		objectList = getIds();
		DebugUtil.debug("Inside the JPO:objectList "+objectList);
	    for(int i=0;i<objectList.size();i++){
			Hashtable hash = (Hashtable) objectList.get(i);
			String ecPartId=(String)hash.get(SELECT_ID);
			DebugUtil.debug("Inside the JPO:ecPartId "+ecPartId);

		if(getTableName(paramMap).equals("CPCBQSLReportSummary")){
   			//qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQSL_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
   			qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Supplier Equivalent].tomid[Qualification].from.id");
		}
   		else{
			//qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQML_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
			qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Manufacturer Equivalent].tomid[Qualification].from.id");
		}
			DomainObject qualObj=new DomainObject(qualifiedIds);
			strBuf.append(qualObj.getAttributeValue(context,CPCConstants.QUALIFICATION_DESC));
			strBuf.append("&nbsp;<br/><br/>");
		}
			qualDesc.add(strBuf.toString());

			DebugUtil.debug("Inside the JPO:qualDesc "+qualDesc);
	}catch(Exception err){
		err.printStackTrace();
	}
return qualDesc;
}


public Vector getQualificationName(Context context,String [] args) throws Exception{
	Vector qualName=new Vector();
	String qualificationId=null;
	String qualifiedIds=null;
	MapList qualificationIds=new MapList();
	StringBuffer strBuf = new StringBuffer();


	try{
	    DebugUtil.debug("Inside the JPO:Name ");
	    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	    MapList objectList =getIds();
	    for(int i=0;i<objectList.size();i++){
			Hashtable hash = (Hashtable) objectList.get(i);
			String ecPartId=(String)hash.get(SELECT_ID);
			if(getTableName(paramMap).equals("CPCBQSLReportSummary")){
				//qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery());
				qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery(),
					CPCConstants.TYPE_QUALIFICATION,
					MQL_ALL_TYPE_OR_REV,
					MQL_ALL_TYPE_OR_REV,
					MQL_ATTR_QUAL_TYPE_ID,
					MQL_BQSL_QUAL_TYPE,
					CPCConstants.PIPE_SEPARATOR);
	   			qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Supplier Equivalent].tomid[Qualification].from.id");
			}
	   		else{
				//qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery());
		   		qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery(),
		   			    CPCConstants.TYPE_QUALIFICATION,
		   				MQL_ALL_TYPE_OR_REV,
		   				MQL_ALL_TYPE_OR_REV,
						MQL_ATTR_QUAL_TYPE_ID,
						MQL_BQML_QUAL_TYPE,
						CPCConstants.PIPE_SEPARATOR);
				qualifiedIds =new DomainObject(ecPartId).getInfo(context,"to[Manufacturer Equivalent].tomid[Qualification].from.id");
			}
				DomainObject qualObj=new DomainObject(qualifiedIds);
				strBuf.append(qualObj.getName(context));
				strBuf.append("&nbsp;<br/><br/>");
			}
				qualName.add(strBuf.toString());
				//strBuf.delete(0,strBuf.length());
				DebugUtil.debug("Inside the JPO:qualName "+qualName);

	}catch(Exception err){
		err.printStackTrace();
	}
return qualName;
}

public Vector getQualificationPreference(Context context,String [] args) throws Exception{
	Vector qualPrefernce=new Vector();
	String qualificationId=null;
	StringBuffer strBuf = new StringBuffer();
	String qualifiedIds=null;
	try{
	    DebugUtil.debug("Inside the JPO:Preference ");
	    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	    MapList objectList =getIds();

	    for(int i=0;i<objectList.size();i++){
			Hashtable hash = (Hashtable) objectList.get(i);
			String ecPartId=(String)hash.get(SELECT_ID);

		if(getTableName(paramMap).equals("CPCBQSLReportSummary")){
   			//qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQSL_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
			qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Supplier Equivalent].tomid[Qualification].from.id");
		}
   		else{
			//qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQML_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
	   		qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Manufacturer Equivalent].tomid[Qualification].from.id");
		}
			DomainObject qualObj=new DomainObject(qualifiedIds);
			strBuf.append(qualObj.getAttributeValue(context,CPCConstants.ATTRIBUTE_PREFERENCE));
			strBuf.append("&nbsp;<br/><br/>");

		}
			qualPrefernce.add(strBuf.toString());

			DebugUtil.debug("Inside the JPO:qualPrefernce "+qualPrefernce);
	}catch(Exception err){
		err.printStackTrace();
	}
return qualPrefernce;
}

public Vector getQualificationPreferenceScore(Context context,String [] args) throws Exception{
	Vector qualPrefernceScore=new Vector();
	String qualificationId=null;
	StringBuffer strBuf = new StringBuffer();
	String qualifiedIds=null;

	try{
	    DebugUtil.debug("Inside the JPO:PreferenceScore ");
	    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	    MapList objectList =getIds();

	    for(int i=0;i<objectList.size();i++){
			Hashtable hash = (Hashtable) objectList.get(i);
			String ecPartId=(String)hash.get(SELECT_ID);

	    if(getTableName(paramMap).equals("CPCBQSLReportSummary")){
   			//qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQSL_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
			qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Supplier Equivalent].tomid[Qualification].from.id");
		}
		  else{
			//qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQML_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
			qualifiedIds =new DomainObject(ecPartId).getInfo(context,"to[Manufacturer Equivalent].tomid[Qualification].from.id");
		}
			DomainObject qualObj=new DomainObject(qualifiedIds);
			strBuf.append(qualObj.getAttributeValue(context,CPCConstants.ATTRIBUTE_PREFERENCE_SCORE));
			strBuf.append("&nbsp;<br/><br/>");
		}
			qualPrefernceScore.add(strBuf.toString());
			DebugUtil.debug("Inside the JPO:qualPrefernceScore "+qualPrefernceScore);
	}catch(Exception err){
		err.printStackTrace();
	}
return qualPrefernceScore;
}

public Vector getQualificationEffectiveDate(Context context,String [] args) throws Exception{
	Vector qualEffDate=new Vector();
	String qualificationId=null;
	StringBuffer strBuf = new StringBuffer();
	String qualifiedIds=null;
	try{
	    DebugUtil.debug("Inside the JPO:EffectiveDate ");
	    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	    MapList objectList = getIds();

	    for(int i=0;i<objectList.size();i++){
			Hashtable hash = (Hashtable) objectList.get(i);
			String ecPartId=(String)hash.get(SELECT_ID);

		 if(getTableName(paramMap).equals("CPCBQSLReportSummary")){
				//qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery());
				qualificationId =  MqlUtil.mqlCommand(context,getBQSLMQLQuery(),
					CPCConstants.TYPE_QUALIFICATION,
					MQL_ALL_TYPE_OR_REV,
					MQL_ALL_TYPE_OR_REV,
					MQL_ATTR_QUAL_TYPE_ID,
					MQL_BQSL_QUAL_TYPE,
					CPCConstants.PIPE_SEPARATOR);
				qualifiedIds=new DomainObject(ecPartId).getInfo(context,"to[Supplier Equivalent].tomid[Qualification].from.id");
			}
			else{
			//qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery());
   			qualificationId =  MqlUtil.mqlCommand(context,getBQMLMQLQuery(),
   			    CPCConstants.TYPE_QUALIFICATION,
   				MQL_ALL_TYPE_OR_REV,
   				MQL_ALL_TYPE_OR_REV,
				MQL_ATTR_QUAL_TYPE_ID,
				MQL_BQML_QUAL_TYPE,
				CPCConstants.PIPE_SEPARATOR);
			qualifiedIds =new DomainObject(ecPartId).getInfo(context,"to[Manufacturer Equivalent].tomid[Qualification].from.id");
		}
			DomainObject qualObj=new DomainObject(qualifiedIds);
			strBuf.append(qualObj.getAttributeValue(context,CPCConstants.ATTRIBUTE_EFFECTIVE_DATE));
			strBuf.append("&nbsp;<br/><br/>");
		}
			qualEffDate.add(strBuf.toString());
		DebugUtil.debug("Inside the JPO:qualEffDate "+qualEffDate);
	}catch(Exception err){
		err.printStackTrace();
	}
return qualEffDate;
}

public MapList getMultiLevelEBOMsforBQML (Context context,
                                   String[] args)
    throws Exception
{

    DebugUtil.debug("Inside the JPO: ");
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)paramMap.get("objectList");
    DebugUtil.debug("inside the sepList objectList:MEP getlevel "+paramMap);
     String partId = (String) paramMap.get("objectId");
    String level = (String) paramMap.get("Level");
    DebugUtil.debug("Inside the JPO: level QML" +level);
    Short SLevel = new Short(level);
    short shLevel = SLevel.shortValue();
    MapList ebomList = new MapList();
    try {
        com.matrixone.apps.common.Part partObj = new com.matrixone.apps.common.Part(partId);
        StringList selectStmts = new StringList(3);
        selectStmts.addElement(SELECT_ID);
        //selectStmts.addElement(SELECT_REQUEST_PART_REVISION_STATE); - Deprecated from DomainConstants and hence commented
        //selectStmts.addElement(SELECT_NEW_PART_PART_REVISION_STATE);- Deprecated from DomainConstants and hence commented
        StringList selectRelStmts = new StringList(8);
        selectRelStmts.addElement(KEY_LEVEL);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_TYPE);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
        String relWhere = "(tomid[Qualification].from.attribute[Qualification Type ID] == \"BQML1\")";
        relWhere+="|| (tomid[Qualification].from.attribute[Qualification Type ID] == \"BQML2\")";
        ebomList=partObj.getRelatedObjects(context,
		                DomainConstants.RELATIONSHIP_EBOM + "," + CPCConstants.RELATIONSHIP_QUALIFICATION,  // relationship pattern
		                DomainConstants.TYPE_PART,                  // object pattern
		                                         selectStmts,                 // object selects
		                                         selectRelStmts,              // relationship selects
		                                         false,                        // to direction
		                                         true,                       // from direction
		                                         (short)shLevel,                    // recursion level
		                                         null,                        // object where clause
		                                         relWhere);                       // relationship where clause
   		DebugUtil.debug("Inside the JPO:ebomList final QML "+ebomList);
    }
    catch (FrameworkException Ex) {
		Ex.printStackTrace();
        throw Ex;
    }

    return ebomList;

}


public Vector getSEPart (Context context, String[] args)
    throws Exception
{
    Vector mePartVtr = new Vector();
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)programMap.get("objectList");
    DebugUtil.debug("inside the sepList objectList: "+objectList);
    //Short SLevel = new Short(level);
    //short shLevel = SLevel.shortValue();
    StringBuffer strBuf = new StringBuffer();
    StringBuffer sbURL = new StringBuffer();
    String partName = "";
    String partId = "";
    StringList busSelect=new StringList();
    busSelect.add(SELECT_NAME);
    busSelect.add(SELECT_ID);

    try {
        //first get all Name values for ids with one DB call
        for(int i=0;i<objectList.size();i++){
			Hashtable map=(Hashtable)objectList.get(i);
	        //StringList sepList=new DomainObject((String)map.get(SELECT_ID)).getRelatedObjects(context,RELATIONSHIP_SEP,TYPE_SEP,busSelect,);
			MapList sepList=new DomainObject((String)map.get(SELECT_ID)).getRelatedObjects(context,
		                CPCConstants.RELATIONSHIP_SEP,  // relationship pattern
		                CPCConstants.TYPE_SEP,                  // object pattern
		                                         busSelect,                 // object selects
		                                         null,              // relationship selects
		                                         false,                        // to direction
		                                         true,                       // from direction
		                                         (short)3,                    // recursion level
		                                         null,                        // object where clause
		                                         null);                       // relationship where clause
			if(sepList.size()>0){
	        for(int j=0;j<sepList.size();j++){
				Map tempMap=(Map)sepList.get(j);
				DebugUtil.debug("Inside the getSEPart:tempMap "+tempMap);
				sbURL.append("../common/emxTree.jsp?objectId="+tempMap.get(SELECT_ID));
				strBuf.append("<a href='javascript:showNonModalDialog(\""+sbURL.toString()+"\",575,575)'>");
				strBuf.append(tempMap.get(SELECT_NAME)+"</a>");
				strBuf.append("&nbsp;<br/><br/>");
				//mePartVtr.add(strBuf.toString());
				sbURL.delete(0,sbURL.length());
				}
				mePartVtr.add(strBuf.toString());
				strBuf.delete(0,strBuf.length());
				setIds(sepList);
			}
       	}
	}
    catch (Exception e){
		e.printStackTrace();
        throw e;
    }
return mePartVtr;
}


public Vector getMEPart (Context context, String[] args)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)programMap.get("objectList");
    DebugUtil.debug("inside the sepList objectList:MEP  objectList "+objectList);
    Vector mePartVtr = new Vector();
    //Short SLevel = new Short(level);
    //short shLevel = SLevel.shortValue();
    StringBuffer strBuf = new StringBuffer();
    StringBuffer sbURL = new StringBuffer();
    String partName = "";
    String partId = "";
    StringList busSelect=new StringList();
    busSelect.add(SELECT_NAME);
    busSelect.add(SELECT_ID);
    ArrayList mainMepList=new ArrayList();
    try {
        for(int i=0;i<objectList.size();i++){
			Hashtable map=(Hashtable)objectList.get(i);
			MapList mepList=new DomainObject((String)map.get(SELECT_ID)).getRelatedObjects(context,
		                CPCConstants.RELATIONSHIP_MEP,  // relationship pattern
		                CPCConstants.TYPE_MEP,                  // object pattern
		                                         busSelect,                 // object selects
		                                         null,              // relationship selects
		                                         false,                        // to direction
		                                         true,                       // from direction
		                                         (short)1,                    // recursion level
		                                         null,                        // object where clause
		                                         null);                       // relationship where clause
		    DebugUtil.debug("inside the mepList size: "+mepList);
		    DebugUtil.debug("inside the mepList size: objectList"+objectList.size());
			if(mepList.size()>0){
	        for(int j=0;j<mepList.size();j++){
				Map tempMap=(Map)mepList.get(j);
				DebugUtil.debug("Inside the getMEPart:MEP "+tempMap);
				DebugUtil.debug("Inside the getMEPart:MEP ID "+tempMap.get(SELECT_ID));
				DebugUtil.debug("Inside the getMEPart:MEP name "+tempMap.get(SELECT_NAME));
				sbURL.append("../common/emxTree.jsp?objectId="+tempMap.get(SELECT_ID));
				strBuf.append("<a href='javascript:showNonModalDialog(\""+sbURL.toString()+"\",575,575)'>");
				strBuf.append(tempMap.get(SELECT_NAME)+"</a>");
				strBuf.append("&nbsp;<br/><br/>");
				sbURL.delete(0,sbURL.length());
				}
				mePartVtr.add(strBuf.toString());
				strBuf.delete(0,strBuf.length());
				setIds(mepList);
			}
		}

	}
    catch (Exception e){
		e.printStackTrace();
        throw e;
    }
    DebugUtil.debug("Inside the getMEPart:mePartVtr Before returns "+mePartVtr);
	return mePartVtr;
}

/*
private String getBQSLMQLQuery(){
		String bqslMql = "temp query bus '"+"Qualification"+"' '*' ' *' " +
                   "where 'attribute["+"Qualification Type ID"+"] MATCH  BQSL*" +
                   "\" ' select id dump |;";
         return bqslMql;
}
private String getBQMLMQLQuery(){
		String bqmlMql = "temp query bus '"+"Qualification"+"' '*' ' *' " +
	                   "where 'attribute["+"Qualification Type ID"+"] MATCH  BQML*" +
	                   "\" ' select id dump |;";
	     return bqmlMql;
} */

// Though both the queries are same, just using different method names as used earlier
private String getBQSLMQLQuery(){
		String bqslMql = "temp query bus $1 $2 $3 " +
                   "where '$4 MATCH $5 ' " +
                   "select id dump $6;";
         return bqslMql;
}

private String getBQMLMQLQuery(){
		String bqmlMql = "temp query bus $1 $2 $3 " +
                   "where '$4 MATCH $5 ' " +
                   "select id dump $6;";
         return bqmlMql;
}

private String getTableName(HashMap paramMap){
		HashMap paramList=(HashMap)paramMap.get("paramList");
		String tableName=(String)paramList.get("table");
		return tableName;

}
private void setIds(MapList mepIds){
	this.mepIdsforDisplay=mepIds;

}

private MapList getIds(){
		return this.mepIdsforDisplay;
}

/**
 * excludeAlreadyConnectedGeneralClass - returns the list of Object ids that are already connected
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
  */

 public StringList excludeAlreadyConnectedGeneralClass (Context context, String[] args)throws FrameworkException {
     try {
         StringList slExcludedIds = new StringList();
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         String strObjectId = (String)programMap.get("selectedClassIds");
         //This check is for LBC Reclassify, since the same code is used for both LBC &CPC Reclassify
         if(programMap.get("parentOID")!=null){
				strObjectId = (String)programMap.get("parentOID");
		 }
         if (strObjectId == null || "".equals(strObjectId)){
             throw new FrameworkException ("Does not has valid Object for connection ");
         }
         else{
			slExcludedIds.add(strObjectId);
			DebugUtil.debug("Inside the exlude :"+slExcludedIds);
            }
         return slExcludedIds;
     }
     catch (Exception e){
         throw new FrameworkException(e);
     }
 }

	public StringList getQualificationLink(Context context,String []args)
	throws Exception
	{
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		Map paramList = (HashMap) paramMap.get("paramList");
		String suiteDir = (String) paramList.get("SuiteDirectory");
		String suiteKey = (String) paramList.get("suiteKey");
		String jsTreeID = (String) paramList.get("jsTreeID");
		String internalPartId = (String) paramList.get("objectId");
		String vepType = (String) paramList.get("vepType");
		String relToUse = "";

		String imgSrc = "<img src='images/" + "iconActionEdit.gif"
							+ "' border='0'/>";
		DebugUtil.debug("getQualificationLink::internalPartId:"+internalPartId);
		DebugUtil.debug("getQualificationLink::vepType:"+vepType);

		if ( vepType.equals("bommepType") )
		     relToUse = "Manufacturer Equivalent";
		else
			 relToUse = "Supplier Equivalent";

		//String href="../common/emxTree.jsp?treeMenu=CPCQualificationMenu&header=Qualification&objectId=";

		StringList returnList=new StringList();
		String mqlCmdTxt = ""; String selectFieldValue = "";

		try {
			MapList objectList = (MapList) paramMap.get("objectList");

			if (objectList != null && objectList.size() > 0) {
					// construct array of ids

			int objectListSize = objectList.size();
				for (int i = 0; i < objectListSize; i++) {
					StringBuffer output = new StringBuffer(" ");

					Map dataMap = (Map) objectList.get(i);
					String objId=(String) dataMap.get(SELECT_ID);
					String relId=(String) dataMap.get(SELECT_RELATIONSHIP_ID);
					DebugUtil.debug("Supplier Equivalent Part ObjectId:"+objId+"::relId:"+relId);

					String qualificationId = null;

					// Fix for IR-166135V6R2013x
					// Logic: If multiple qualifications for a given cross-reference part then
					//        loop through all qualifications and display them in the summary.

					if ( null != relId )
					{
					     //qualificationId=MqlUtil.mqlCommand(context, "print connection \"" + relId+ "\" select tomid[Qualification].from.id dump");
						 mqlCmdTxt = "print connection $1 select $2 dump $3";
						 selectFieldValue = "tomid[Qualification].from.id";
					     qualificationId=MqlUtil.mqlCommand(context, mqlCmdTxt, relId, selectFieldValue, CPCConstants.PIPE_SEPARATOR);
					}

					DebugUtil.debug("Check Qual exists >> qualId:"+qualificationId);

					// for example,
					// String qualificationId=MqlUtil.mqlCommand(context, "print bus \"" + objId+ "\" select to[Supplier Equivalent].tomid[Qualification].from.id dump");
					//String qualificationId=MqlUtil.mqlCommand(context, "print bus \"" + objId+ "\" select to["+relToUse+"].tomid[Qualification].from.id dump");

					if(qualificationId==null||qualificationId.equals(""))
					{
						returnList.add("");
					}
					else
					{
						//String qualRelId=MqlUtil.mqlCommand(context, "print bus \"" + objId+ "\" select to["+relToUse+"].tomid[Qualification].id dump");
						/*mqlCmdTxt = "print bus $1 select $2 dump";
						selectFieldValue = "to["+relToUse+"].tomid[Qualification].id";
						String qualRelId=MqlUtil.mqlCommand(context, mqlCmdTxt, objId, selectFieldValue);*/

						StringList qualIdList = FrameworkUtil.split(qualificationId, CPCConstants.PIPE_SEPARATOR);

						for(int q=0; q<qualIdList.size(); q++)
						{
							qualificationId=(String)qualIdList.get(q);
							DebugUtil.debug("qualificationId:"+qualificationId);
							//DebugUtil.debug("qualRelId:"+qualRelId);

							if ( vepType.equals("bommepType") )
							output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp"
											+ "?treeMenu=CPCQualificationMenuMEP&amp;emxSuiteDirectory=");
							else
							output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp"
											+ "?treeMenu=CPCQualificationMenu&amp;emxSuiteDirectory=");

							output.append(suiteDir);
							output.append("&amp;suiteKey=");
							output.append(suiteKey);
							output.append("&amp;jsTreeID=");
							output.append(jsTreeID);
							output.append("&amp;objectId=");
							output.append(qualificationId);
							//output.append("&relId=");
							//output.append(objId);
							//output.append(qualRelId);
							output.append("&amp;intPartId=");
							output.append(internalPartId);
							output.append("', '1000', '500', 'false', 'popup', '')\" class=\"object\">");
							output.append(imgSrc);
							output.append("</a>");
						} // for-loop qualIdList

						//String href1=href+qualificationId;
						//String hrefLink="<a target=popup href=\""+href1+"\">"+imgSrc+"</a>";
						//returnList.add(hrefLink);
						returnList.add(output.toString());
						output = null;
					}
				}
			}
			DebugUtil.debug("returnList:"+returnList);

        } catch (Exception e) {
            throw new FrameworkException(e);
        } finally {
            return returnList;
        }
	}  // getQualificationLink


		public HashMap getAssembliesForEditPart (Context context, String[] args)
			throws Exception
		{
			DebugUtil.debug("Inside the JPO: getAssembliesForEditPart");
			HashMap requestMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)requestMap.get("paramMap");
			String qualId = (String) paramMap.get("objectId");
			//DebugUtil.debug("getAssembliesForEditPart:paramMap "+paramMap);
			String partId = null;

			MapList ebomList = new MapList();

			// initialize the return variable HashMap tempMap = new HashMap();
			HashMap tempMap = new HashMap();

			// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
			StringList fieldRangeValues = new StringList();
			StringList fieldDisplayRangeValues = new StringList();

			try {
				DebugUtil.debug("print bus \"" + qualId+ "\" select from[Qualification].torel[Supplier Equivalent].from.id dump");
				//partId=MqlUtil.mqlCommand(context, "print bus \"" + qualId+ "\" select from[Qualification].torel[Supplier Equivalent].from.id dump");
			   String mqlCmdTxt = "print bus $1 select $2 dump";
			   String selectFieldValue = "from[Qualification].torel[Supplier Equivalent].from.id";
			   String vepId = MqlUtil.mqlCommand(context, mqlCmdTxt, qualId, selectFieldValue);

				DebugUtil.debug("getAssembliesForEditPart:partId "+partId);

				com.matrixone.apps.common.Part partObj = new com.matrixone.apps.common.Part(partId);
				StringList selectStmts = new StringList(2);
				selectStmts.addElement(SELECT_ID);
				selectStmts.addElement(SELECT_NAME);
				StringList selectRelStmts = new StringList(2);
				selectRelStmts.addElement(KEY_LEVEL);
				selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
				ebomList=partObj.getRelatedObjects(context,
								DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
								DomainConstants.TYPE_PART,                  // object pattern
								selectStmts,                 // object selects
								selectRelStmts,              // relationship selects
								true,                        // to direction
								false,                       // from direction
								(short)1,                    // recursion level
								null,                        // object where clause
								null);                       // relationship where clause

				DebugUtil.debug("getAssembliesForEditPart "+ebomList);

				String strDefaultState = "Not Selected";
				java.util.Iterator iter = ebomList.iterator();

				fieldRangeValues.insertElementAt("*",0);
				fieldDisplayRangeValues.insertElementAt(strDefaultState,0);

				String ebomID ="";
				String partName = "";
				while ( iter.hasNext() ) {
					Hashtable relRecord = (Hashtable) iter.next();
					ebomID = (String) relRecord.get("id[connection]");
					partName = (String) relRecord.get("name");

			   		fieldRangeValues.addElement(ebomID);
					fieldDisplayRangeValues.addElement(partName);
				}

				DebugUtil.debug("getAssembliesForEditPart::fieldRangeValues:"+fieldRangeValues+" "+
									fieldDisplayRangeValues);

				tempMap.put("field_choices", fieldRangeValues);
				tempMap.put("field_display_choices", fieldDisplayRangeValues);
			}
			catch (FrameworkException Ex) {
				Ex.printStackTrace();
				throw Ex;
			}

			return tempMap;
		}


   public boolean checkEditAccess(Context context, String args[]) throws FrameworkException{
	   boolean hasEditAccess=false;
	   try{
		   ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

		   HashMap programMap = (HashMap) JPO.unpackArgs(args);
		   StringList selectList=new StringList();
		   selectList.add("current.access[modify]");
		   selectList.add("current");
		   DomainObject domObj=new DomainObject((String)programMap.get("objectId"));
		   Map resultMap=domObj.getInfo(context,selectList);
		   DebugUtil.debug("Inside the edit access: "+resultMap);
		   DebugUtil.debug("Inside the edit access: "+resultMap.get("current.access[modify]"));
		   DebugUtil.debug("Inside the edit access: "+"Release".equalsIgnoreCase((String)resultMap.get("current")));
		   if("true".equalsIgnoreCase((String)resultMap.get("current.access[modify]")) && !"Release".equalsIgnoreCase((String)resultMap.get("current")))
				hasEditAccess=true;
	   }catch(Exception err){
		   err.printStackTrace();
	   }
	   return hasEditAccess;
   }

   public boolean checkDocumentAccess(Context context, String args[]) throws FrameworkException {
	   boolean hasDocumentAccess=false;
	   try{
		   DebugUtil.debug("Inside the checkDocument;");
  		   HashMap programMap = (HashMap) JPO.unpackArgs(args);
  		   String objectId=(String)programMap.get("objectId");
  		   DomainObject domObj=new DomainObject(objectId);
  		   if(domObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(CPCConstants.TYPE_SEP)){
  		   		DebugUtil.debug("Inside the checkDocument;"+domObj.getInfo(context,DomainConstants.SELECT_OWNER));
				DebugUtil.debug("Inside the checkDocument;"+context.getUser());
				if(context.getUser().equals(domObj.getInfo(context,DomainConstants.SELECT_OWNER)))
					hasDocumentAccess=true;
			}
			else
				hasDocumentAccess=true;

	   }catch(Exception err){
		   err.printStackTrace();
	   }
		return hasDocumentAccess;
   }

		public void clearCascadeQualificationObjects(Context context, String args[])
		throws Exception {
		   try{
			   DebugUtil.debug("Inside the clearCascadeQualificationObjects");
			   String qualId = null;
			   DebugUtil.debug("Inside the clearCascadeQualificationObjects::args[1]"+args[1]);

			   if ( null != args[0] && !"".equals(args[0]) )
			   {
					qualId = args[0];
					boolean checkQualRelExists = false;

					String cpcRelName = PropertyUtil.getGlobalRPEValue(context, "CPC_REL_NAME");
					DebugUtil.debug("clearCascadeQualificationObjects::cpcRelName==>"+cpcRelName);

					if ( "Supplier".startsWith(cpcRelName) || "Manufacturer".startsWith(cpcRelName) )
						 checkQualRelExists = true; // set to true if it relates to SE or ME

					if ( checkQualRelExists )
					{
						DomainObject partObj = new DomainObject(qualId);
						partObj.deleteObject(context);
						DebugUtil.debug("clearCascadeQualificationObjects::qualId deleted");
					}
			   }

		   }catch(Exception err){
			   err.printStackTrace();
		   }
		}

		public void setEquivalentRelationName(Context context, String args[])
		throws Exception {
		   try{
			   DebugUtil.debug("Inside the setEquivalentRelationName");

			   String qualRelId = null;

			   if ( null != args[0] && !"".equals(args[0]) )
			   {
					qualRelId = args[0];

					//String checkQualRelExistsSE = MqlUtil.mqlCommand(context, "print connection "+ qualRelId + " select torel[Supplier Equivalent] dump");
					//String checkQualRelExistsME = MqlUtil.mqlCommand(context, "print connection "+ qualRelId + " select torel[Manufacturer Equivalent] dump");

				   String mqlCmdTxt = "print connection $1 select $2 dump";
				   String selectFieldValue = "torel[Supplier Equivalent]";
				   String checkQualRelExistsSE = MqlUtil.mqlCommand(context, mqlCmdTxt, qualRelId, selectFieldValue);

				   selectFieldValue = "torel[Manufacturer Equivalent]";
				   String checkQualRelExistsME = MqlUtil.mqlCommand(context, mqlCmdTxt, qualRelId, selectFieldValue);

					DebugUtil.debug("setEquivalentRelationName::checkQualRelExistsSE==>"+checkQualRelExistsSE);
					DebugUtil.debug("setEquivalentRelationName::checkQualRelExistsME==>"+checkQualRelExistsME);

					boolean checkQualRelExists = false;

					if ( "Supplier Equivalent".equals(checkQualRelExistsSE) || "Manufacturer Equivalent".equals(checkQualRelExistsSE) )
						 checkQualRelExists = true; // set to true if it relates to SE or ME

					if ( checkQualRelExists )
					{
						if ( "".equals(checkQualRelExistsME) )
							 PropertyUtil.setGlobalRPEValue(context,"CPC_REL_NAME", checkQualRelExistsSE);
						else
							 PropertyUtil.setGlobalRPEValue(context,"CPC_REL_NAME", checkQualRelExistsME);
						DebugUtil.debug("setEquivalentRelationName::relation set");
					}
				}

		   }catch(Exception err){
			   err.printStackTrace();
		   }
		}

		public String showManufacturerField1(Context context, String[] args)
				   throws Exception {
			String result = null;

			   DebugUtil.debug("showManufacturerField1");
			   HashMap requestMap = (HashMap)JPO.unpackArgs(args);
			   DebugUtil.debug("showManufacturerField1::requestMap:"+requestMap);
			   HashMap paramMap = (HashMap)requestMap.get("paramMap");
			   String objectId = (String) paramMap.get("objectId");

			   String vepId = null; String relToUse = null; String relationSuppRes = null;

			   relToUse = "Manufacturer Equivalent";
			   relationSuppRes = RELATIONSHIP_MANUFACTURING_RESPONSIBILITY;

			if ( null != objectId )
			{
			   //vepId = MqlUtil.mqlCommand(context, "print bus "+ objectId + " select from[Qualification].torel["+relToUse+"].to.id dump");
			   String mqlCmdTxt = "print bus $1 select $2 dump";
			   String selectFieldValue = "from[Qualification].torel["+relToUse+"].to.id";
			   vepId = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);

			   DebugUtil.debug("showManufacturerField1::objectId:"+vepId);
			   StringBuffer outPut = new StringBuffer();

			   DomainObject domSepEqvPartID = DomainObject.newInstance(context, vepId);
			   StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			   StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			   Map relationshipMap = domSepEqvPartID.getRelatedObject(context, relationSuppRes, false, objectSelects, relSelects);

			   if (relationshipMap != null && relationshipMap.size() > 0){
					String supplierID = (String) relationshipMap.get("id");
					DomainObject domSupplierID = DomainObject.newInstance(context, supplierID);
					String supplierName = domSupplierID.getInfo(context,DomainConstants.SELECT_NAME);
					outPut.append(supplierName);
				}
				DebugUtil.debug("showManufacturerField1::outPut:"+outPut.toString());
				result = outPut.toString();
			}

				return result;
		}


	public String showMEPUsageLocations(Context context, String[] args)
           throws Exception {

		DebugUtil.debug("showMEPUsageLocations");
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)programMap.get("requestMap");
		HashMap paramMap = (HashMap)requestMap.get("paramMap");
		String objectId = null;
		String usageLocations = "";

		if ( null == paramMap )
		     objectId = (String) requestMap.get("objectId");
		else
		     objectId = (String) paramMap.get("objectId");

		String mqlCmdTxt = "print bus $1 select $2 dump";
		String selectFieldValue = "";

		if ( null != objectId )
		{
			DomainObject domObj = DomainObject.newInstance(context, objectId);
			DebugUtil.debug("showMEPUsageLocations::object type:"+domObj.getInfo(context,DomainConstants.SELECT_TYPE));

			if ( null != domObj && (domObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(DomainConstants.TYPE_PART) ||
					 domObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(CPCConstants.TYPE_SUPPLIER_EQUIVALENT_PART)))
			{
				 /* usageLocations = MqlUtil.mqlCommand(context, "print bus "+ objectId +
				  " select to[Allocation Responsibility].from.name dump"); */
				selectFieldValue = "to[Allocation Responsibility].from.name";
				usageLocations = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);
			}
			else
			{
				 /* usageLocations = MqlUtil.mqlCommand(context, "print bus "+ objectId +
				  " select from[Qualification].torel[Manufacturer Equivalent].to.to[Allocation Responsibility].from.name dump"); */
				selectFieldValue = "from[Qualification].torel[Manufacturer Equivalent].to.to[Allocation Responsibility].from.name";
				usageLocations = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);
			}
		}

		DebugUtil.debug("showMEPUsageLocations::usageLocations:"+usageLocations);

		return usageLocations;
   	}

	public String showSEPUsageLocations(Context context, String[] args)
           throws Exception {

		DebugUtil.debug("showSEPUsageLocations");

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)programMap.get("requestMap");
		HashMap paramMap = (HashMap)requestMap.get("paramMap");

		String objectId = null;
		String usageLocations = "";

		DebugUtil.debug("showSEPUsageLocations::requestMap:"+requestMap);
		DebugUtil.debug("showSEPUsageLocations::paramMap:"+paramMap);

		if ( null == paramMap )
		     objectId = (String) requestMap.get("objectId");
		else
		     objectId = (String) paramMap.get("objectId");

		String mqlCmdTxt = "print bus $1 select $2 dump";
		String selectFieldValue = "";

		if ( null != objectId )
		{
			DomainObject domObj = DomainObject.newInstance(context, objectId);
			DebugUtil.debug("showSEPUsageLocations::object type:"+domObj.getInfo(context,DomainConstants.SELECT_TYPE));

			if ( null != domObj && (domObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(DomainConstants.TYPE_PART) ||
					 domObj.getInfo(context,DomainConstants.SELECT_TYPE).equals(CPCConstants.TYPE_SUPPLIER_EQUIVALENT_PART)))
			{
				 /* usageLocations = MqlUtil.mqlCommand(context, "print bus "+ objectId +
				  " select to[Allocation Responsibility].from.name dump"); */
				 selectFieldValue = "to[Allocation Responsibility].from.name";
				 usageLocations = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);
			}
			else
			{
				 /* usageLocations = MqlUtil.mqlCommand(context, "print bus "+ objectId +
				  " select from[Qualification].torel[Supplier Equivalent].to.to[Allocation Responsibility].from.name dump"); */
				 selectFieldValue = "from[Qualification].torel[Supplier Equivalent].to.to[Allocation Responsibility].from.name";
				 usageLocations = MqlUtil.mqlCommand(context, mqlCmdTxt, objectId, selectFieldValue);
			}
		}

		DebugUtil.debug("showSEPUsageLocations::usageLocations:"+usageLocations);

		return usageLocations;
   	}

   public boolean checkProductLicense(Context context, String args[])
   throws FrameworkException {
	   boolean hasLicense=false;

	   try{
		   ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

		   hasLicense=true;
	   }catch(Exception err){
		   err.printStackTrace();
	   }

	   return hasLicense;
   }

   public StringList checkAVCLicense(Context context, String args[])  throws FrameworkException {
	   StringList slIncludeIds = new StringList();
	   StringList selectStmts=new StringList();
	   MapList mlChecklists=new MapList();
	   try{
		   //DebugUtil.debug("Inside the checkAVCLicense:"+slIncludeIds);
		   ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);
		   HashMap requestMap = (HashMap)JPO.unpackArgs(args);
		   //DebugUtil.debug("Inside the checkAVCLicense:requestMap"+requestMap);
		   String typeField=(String)requestMap.get("field");
		   //DebugUtil.debug("Inside the checkAVCLicense:typeField: "+typeField);
		   String type=typeField.substring(typeField.indexOf("=")+1,typeField.indexOf(":"));
		   //DebugUtil.debug("Inside the checkAVCLicense:type: "+type);
		   selectStmts.add(DomainConstants.SELECT_ID);
		   if(CPCConstants.TYPE_SEP.equalsIgnoreCase(type))
		   		 mlChecklists= DomainObject.findObjects(context, CPCConstants.TYPE_SEP, "*", null, selectStmts);
		   else
				mlChecklists= DomainObject.findObjects(context,TYPE_PART, "*", null, selectStmts);
		   //DebugUtil.debug("Inside the checkAVCLicense:"+mlChecklists);
		   for(int itr = 0; itr < mlChecklists.size(); itr++){
			   Map map = (Map)mlChecklists.get(itr);
			   slIncludeIds.add(map.get(DomainConstants.SELECT_ID));
			}

	   }catch(Exception err){
		   throw new FrameworkException(err);
	   }
	return slIncludeIds;
   }

   @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
   public StringList checkAVXLicense(Context context, String args[])  throws FrameworkException {
	   StringList slIncludeIds = new StringList();
	   StringList selectStmts=new StringList();
	   MapList mlChecklists=new MapList();
	   try{
		   //DebugUtil.debug("Inside the checkAVCLicense:"+slIncludeIds);
		   ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVX_PRODUCT_TRIGRAM);
		   HashMap requestMap = (HashMap)JPO.unpackArgs(args);
		   //DebugUtil.debug("Inside the checkAVCLicense:requestMap"+requestMap);
		   String typeField=(String)requestMap.get("field");
		   //DebugUtil.debug("Inside the checkAVCLicense:typeField: "+typeField);
		   String type=typeField.substring(typeField.indexOf("=")+1,typeField.indexOf(":"));
		   //DebugUtil.debug("Inside the checkAVCLicense:type: "+type);
		   selectStmts.add(DomainConstants.SELECT_ID);
		   if(CPCConstants.TYPE_SEP.equalsIgnoreCase(type))
		   		 mlChecklists= DomainObject.findObjects(context, CPCConstants.TYPE_SEP, "*", null, selectStmts);
		   else
				mlChecklists= DomainObject.findObjects(context,TYPE_PART, "*", null, selectStmts);
		   //DebugUtil.debug("Inside the checkAVCLicense:"+mlChecklists);
		   for(int itr = 0; itr < mlChecklists.size(); itr++){
			   Map map = (Map)mlChecklists.get(itr);
			   slIncludeIds.add(map.get(DomainConstants.SELECT_ID));
			}

	   }catch(Exception err){
		   throw new FrameworkException(err);
	   }
	return slIncludeIds;
   }

    /**
     * Creates a SEP Qualification and connects to related objects
     *
     * @param context
     *            The Matrix Context.
     * @param args
     *            holds a packed HashMap which contains supplier Id
          * @throws FrameworkException
     *             If the operation fails.
     */
@com.matrixone.apps.framework.ui.PostProcessCallable
    public void qualifySEP(Context context, String[] args) throws Exception {

        try {
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

            ContextUtil.startTransaction(context, true);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            //DebugUtil.setDebug(true);
            DebugUtil.debug("Inside the qualifySEP:requestMap"+requestMap);

            HashMap paramMap = (HashMap) programMap.get("paramMap");
            DebugUtil.debug("Inside the qualifySEP:paramMap"+paramMap);

            // part name in which qualification is done
            String partContext = (String) requestMap.get("parentOID");
            DomainObject partObj = new DomainObject(partContext);
            String partName = partObj.getInfo(context, DomainConstants.SELECT_NAME);
            DebugUtil.debug("Inside the qualifySEP:partName"+partName);

            String qualObjId=(String)paramMap.get("objectId");
            DebugUtil.debug("Inside the qualifySEP:qualObjId"+qualObjId);

			String vepInfo = (String)requestMap.get("emxTableRowId");
			String vepId = null; String vequivRelId = null;

			// Get relationship id of Supplier Equivalent (connects Part and SEP)
			if ( null != vepInfo )
			{
				StringTokenizer tokenizer = new StringTokenizer(vepInfo, "|");
				while(tokenizer.hasMoreTokens())
				{
					try{
						vequivRelId = tokenizer.nextToken();
						if ( tokenizer.hasMoreTokens() )
							 vepId = tokenizer.nextToken();
						break;
					}
					catch (Exception Ex)
					{
						DebugUtil.debug("caught in StringTokenizer"+Ex.getMessage());
					}
				}
			}

			DebugUtil.debug("qualifySEP::VEP object ID"+vepId);
			DebugUtil.debug("qualifySEP::EP-VEP rel ID"+vequivRelId);

			boolean isLocationBasedQual = false; boolean isBOMQual = false;

			String assemblyPartName = (String)requestMap.get("AssemblyPartName");
			String supplierName = (String)requestMap.get("Supplier");
			String supplierLocation = (String)requestMap.get("SupplierLocation");

			DebugUtil.debug("qualifySEP::assemblyPartName==>"+assemblyPartName);
			DebugUtil.debug("qualifySEP::supplierName==>"+supplierName);
			DebugUtil.debug("qualifySEP::supplierLocation==>"+supplierLocation);

			String qualTypeId = "QSL2";
			if ( null != supplierLocation && !"Not Selected".equals(supplierLocation) )
			{
			     isLocationBasedQual = true;
			     qualTypeId = "QSL1";
			}

			if ( null != assemblyPartName && !"Not Selected".equals(assemblyPartName) )
			{
			     isBOMQual = true;
			     qualTypeId = "B"+qualTypeId;
			}

			DomainObject qualObj = new DomainObject(qualObjId);
			String attrName = PropertyUtil.getSchemaProperty(context, "attribute_QualificationTypeID");
			qualObj.setAttributeValue(context, attrName, qualTypeId);

			DebugUtil.debug("qualifySEP::qualTypeId SET"+qualTypeId);

			String mqlQuery = null; String locRelationId = null; String eBOMRelId = null;

			String selectFieldValue = "";
			String whereCondTxt = "";

			/* NOTE: (eeq) - V6R2013 release [IR-154386V6R2013]
			         Part of the code here is commented due to implementation issue with Parameterized MQL API module.
			         This code needs to enabled again at later point of time once the issue is addressed.
			*/

			// Get relationship id of Organization Location
			// Using MQL command which is simplified than other API
			if ( isLocationBasedQual )
			{
				// sample query
				// temp query bus Location Albany * where "to[Organization Location].from.name == 'Retro Outfitters' "
				// select to[Organization Location].id dump |;

				mqlQuery =
				"temp query bus Location '"+supplierLocation+"' * where \""+"to[Organization Location].from.name == '"+supplierName+"'\""+
				" select to[Organization Location].id dump |";

				/*whereCondTxt = "\""+"to[Organization Location].from.name == '"+supplierName+"'"+"\"";
				selectFieldValue = "to[Organization Location].id";
				mqlQuery = "temp query bus $1 $2 $3 where $4 select $5 dump $6";

				DebugUtil.debug("qualifySEP::mqlQuery-location based==>"+mqlQuery);

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery,
									"Location", "'"+supplierLocation+"'", MQL_ALL_TYPE_OR_REV,
									whereCondTxt, selectFieldValue, CPCConstants.PIPE_SEPARATOR);*/

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery);

				locRelationId = this.getConnectionId(txtOutput, null);
				DebugUtil.debug("qualifySEP::locRelationId==>"+locRelationId);
			}

			// Get relationship id of EBOM
			// Using MQL command which is simplified than other API
			if ( isBOMQual )
			{
				// sample query
				// temp query bus Part eeqDevPart1 * where "to[EBOM].from.name == 'eeqDevPart2' " select to[EBOM].id dump |;

				mqlQuery =
				"temp query bus Part '"+partName+"' * where \""+"to[EBOM].from.name == '"+assemblyPartName+"'\""+
				" select to[EBOM].id dump |";

				/*whereCondTxt = "\""+"to[EBOM].from.name == '"+assemblyPartName+"'\"";
				selectFieldValue = "to[EBOM].id";
				mqlQuery = "temp query bus $1 $2 $3 where $4 select $5 dump $6";

				DebugUtil.debug("qualifySEP::mqlQuery-BOM specific==>"+mqlQuery+", whereCondTxt"+whereCondTxt);

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery,
									"Part", "'"+partName+"'", MQL_ALL_TYPE_OR_REV,
									whereCondTxt, selectFieldValue, CPCConstants.PIPE_SEPARATOR);*/

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery);
				DebugUtil.debug("qualifySEP::txtOutput-==>"+txtOutput);

				eBOMRelId = this.getConnectionId(txtOutput, null);
				DebugUtil.debug("qualifySEP::eBOMRelId-==>"+eBOMRelId);
			}

			// Create all qualifications
			// Qualify [Supplier Equivalent] relationship id

			//String createQualStmt = "add connection Qualification from " + qualObjId + " torel " + vequivRelId;
			String createQualStmt = "add connection $1 from $2 torel $3";

			String mqlCmdStatus = null;
			if (qualObjId!=null && vequivRelId!=null)
			{
				//mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true);
				mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true,
								CPCConstants.TYPE_QUALIFICATION, qualObjId, vequivRelId);
				DebugUtil.debug("qualifySEP:::createQualStmt1==>"+createQualStmt+"::status==>"+mqlCmdStatus);
			}

			// if location based, Qualify [Organization Location] relationship id
			if ( isLocationBasedQual )
			{
				//createQualStmt = "add connection Qualification from " + qualObjId + " torel " + locRelationId;
				createQualStmt = "add connection $1 from $2 torel $3";
				//mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true);
				mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true,
								CPCConstants.TYPE_QUALIFICATION, qualObjId, locRelationId);
				DebugUtil.debug("qualifySEP:::createQualStmt2==>"+createQualStmt+"::status==>"+mqlCmdStatus);
			}

			// if BOM specific, Qualify [EBOM] relationship id
			if ( isBOMQual )
			{
				//createQualStmt = "add connection Qualification from " + qualObjId + " torel " + eBOMRelId;
				createQualStmt = "add connection $1 from $2 torel $3";
				//mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true);
				mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true,
								CPCConstants.TYPE_QUALIFICATION, qualObjId, eBOMRelId);
				DebugUtil.debug("qualifySEP:::createQualStmt3==>"+createQualStmt+"::status==>"+mqlCmdStatus);
			}

            ContextUtil.commitTransaction(context);
            //DebugUtil.setDebug(false);
        } catch (Exception e) {
            e.printStackTrace();
             try{
            ContextUtil.abortTransaction(context);

             }catch(Exception ex){
                 throw (new Exception("SEP Qualification creation failed."));
             }
			//DebugUtil.setDebug(false);
            throw (new FrameworkException(e));
        }
    }

	// Utility method
	private String getConnectionId(String txtConnectionId, String tokenSep)
	{
		String connectionId = null; String tmpTxt = null; String useTokenSep = null;

		if ( null == tokenSep )
			 useTokenSep = "|";
		else
			 useTokenSep = tokenSep;

		// Default token separator is | (pipe)
		StringTokenizer sIds = new StringTokenizer(txtConnectionId, useTokenSep);

		// Sample output:: (pick the 4th token)
		// Location|Albany|-|11816.62360.12749.28539
		while(sIds.hasMoreTokens())
		{
			try{
				tmpTxt = sIds.nextToken(); // first
				if ( sIds.hasMoreTokens() )
					 tmpTxt = sIds.nextToken(); // second
				if ( sIds.hasMoreTokens() )
					 tmpTxt = sIds.nextToken(); // third
				if ( sIds.hasMoreTokens() )
					 connectionId = sIds.nextToken(); // fourth
			}
			catch (Exception Ex)
			{
				DebugUtil.debug("caught in StringTokenizer"+Ex.getMessage());
			}
		}

		DebugUtil.debug("getConnectionId ==>"+connectionId);

		return connectionId;
	}

    /**
     * Creates a MEP Qualification and connects to related objects
     *
     * @param context
     *            The Matrix Context.
     * @param args
     *            holds a packed HashMap which contains supplier Id
          * @throws FrameworkException
     *             If the operation fails.
     */
     @com.matrixone.apps.framework.ui.PostProcessCallable
    public void qualifyMEP(Context context, String[] args) throws Exception {

        try {
			ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

            ContextUtil.startTransaction(context, true);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            //DebugUtil.setDebug(true);
            DebugUtil.debug("Inside the qualifyMEP:requestMap"+requestMap);

            HashMap paramMap = (HashMap) programMap.get("paramMap");
            DebugUtil.debug("Inside the qualifyMEP:paramMap"+paramMap);

            // part name in which qualification is done
            String partContext = (String) requestMap.get("parentOID");
            DomainObject partObj = new DomainObject(partContext);
            String partName = partObj.getInfo(context, DomainConstants.SELECT_NAME);
            DebugUtil.debug("Inside the qualifyMEP:partName"+partName);

            String qualObjId=(String)paramMap.get("objectId");
            DebugUtil.debug("Inside the qualifyMEP:qualObjId"+qualObjId);

			String vepInfo = (String)requestMap.get("emxTableRowId");
			String vepId = null; String vequivRelId = null;

			// Get relationship id of Manufacturer Equivalent (connects Part and MEP)
			if ( null != vepInfo )
			{
				StringTokenizer tokenizer = new StringTokenizer(vepInfo, "|");
				while(tokenizer.hasMoreTokens())
				{
					try{
						vequivRelId = tokenizer.nextToken();
						if ( tokenizer.hasMoreTokens() )
							 vepId = tokenizer.nextToken();
						break;
					}
					catch (Exception Ex)
					{
						DebugUtil.debug("caught in StringTokenizer"+Ex.getMessage());
					}
				}
			}

			DebugUtil.debug("qualifyMEP::VEP object ID"+vepId);
			DebugUtil.debug("qualifyMEP::EP-VEP rel ID"+vequivRelId);

			boolean isLocationBasedQual = false; boolean isBOMQual = false;

			String assemblyPartName = (String)requestMap.get("AssemblyPartName");
			String manufacturerName = (String)requestMap.get("Manufacturer");
			String manufacturerLocation = (String)requestMap.get("ManufacturerLocation");

			DebugUtil.debug("qualifyMEP::assemblyPartName==>"+assemblyPartName);
			DebugUtil.debug("qualifyMEP::Manufacturer==>"+manufacturerName);
			DebugUtil.debug("qualifyMEP::ManufacturerLocation==>"+manufacturerLocation);

			String qualTypeId = "QML2";
			if ( null != manufacturerLocation && !"Not Selected".equals(manufacturerLocation) )
			{
			     isLocationBasedQual = true;
			     qualTypeId = "QML1";
			}

			if ( null != assemblyPartName && !"Not Selected".equals(assemblyPartName) )
			{
			     isBOMQual = true;
			     qualTypeId = "B"+qualTypeId;
			}

			DomainObject qualObj = new DomainObject(qualObjId);
			String attrName = PropertyUtil.getSchemaProperty(context, "attribute_QualificationTypeID");
			qualObj.setAttributeValue(context, attrName, qualTypeId);

			DebugUtil.debug("qualifyMEP::qualTypeId SET"+qualTypeId);

			String mqlQuery = null; String locRelationId = null; String eBOMRelId = null;

			String selectFieldValue = "";
			String whereCondTxt = "";

			/* NOTE: (eeq) - V6R2013 release [IR-154386V6R2013]
			         Part of the code here is commented due to implementation issue with Parameterized MQL API module.
			         This code needs to enabled again at later point of time once the issue is addressed.
			*/

			// Get relationship id of Organization Location
			// Using MQL command which is simplified than other API
			if ( isLocationBasedQual )
			{
				// sample query
				// temp query bus Location Albany * where "to[Organization Location].from.name == 'Retro Outfitters' "
				// select to[Organization Location].id dump |;

				mqlQuery =
				"temp query bus Location '"+manufacturerLocation+"' * where \""+"to[Organization Location].from.name == '"+manufacturerName+"'\""+
				" select to[Organization Location].id dump |";

				/*whereCondTxt = "\""+"to[Organization Location].from.name == '"+manufacturerLocation+"'"+"\"";
				selectFieldValue = "to[Organization Location].id";
				mqlQuery = "temp query bus $1 $2 $3 where $4 select $5 dump $6";

				DebugUtil.debug("qualifyMEP::mqlQuery-location based==>"+mqlQuery);

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery,
									"Location", "'"+manufacturerLocation+"'", MQL_ALL_TYPE_OR_REV,
									whereCondTxt, selectFieldValue, CPCConstants.PIPE_SEPARATOR);*/

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery);

				locRelationId = this.getConnectionId(txtOutput, null);
				DebugUtil.debug("qualifyMEP::locRelationId==>"+locRelationId);
			}

			// Get relationship id of EBOM
			// Using MQL command which is simplified than other API
			if ( isBOMQual )
			{
				// sample query
				// temp query bus Part eeqDevPart1 * where "to[EBOM].from.name == 'eeqDevPart2' " select to[EBOM].id dump |;

				mqlQuery =
				"temp query bus Part '"+partName+"' * where \""+"to[EBOM].from.name == '"+assemblyPartName+"'\""+
				" select to[EBOM].id dump |";

				/*whereCondTxt = "\""+"to[EBOM].from.name == '"+assemblyPartName+"'"+"\"";
				selectFieldValue = "to[EBOM].id";
				mqlQuery = "temp query bus $1 $2 $3 where $4 select $5 dump $6";

				DebugUtil.debug("qualifyMEP::mqlQuery-BOM specific==>"+mqlQuery);

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery,
									"Part", "'"+partName+"'", MQL_ALL_TYPE_OR_REV,
									whereCondTxt, selectFieldValue, CPCConstants.PIPE_SEPARATOR);*/

				String txtOutput = MqlUtil.mqlCommand(context, mqlQuery);

				eBOMRelId = this.getConnectionId(txtOutput, null);
				DebugUtil.debug("qualifyMEP::eBOMRelId-==>"+eBOMRelId);
			}

			// Create all qualifications
			// Qualify [Manufacturer Equivalent] relationship id

			//String createQualStmt = "add connection Qualification from " + qualObjId + " torel " + vequivRelId;
			String createQualStmt = "add connection $1 from $2 torel $3";

			String mqlCmdStatus = null;
			if (qualObjId!=null && vequivRelId!=null)
			{
				//mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true);
				mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true,
										CPCConstants.TYPE_QUALIFICATION, qualObjId, vequivRelId);

				DebugUtil.debug("qualifyMEP:::createQualStmt1==>"+createQualStmt+"::status==>"+mqlCmdStatus);
			}

			// if location based, Qualify [Organization Location] relationship id
			if ( isLocationBasedQual )
			{
				//createQualStmt = "add connection Qualification from " + qualObjId + " torel " + locRelationId;
				createQualStmt = "add connection $1 from $2 torel $3";
				//mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true);
				mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true,
										CPCConstants.TYPE_QUALIFICATION, qualObjId, locRelationId);

				DebugUtil.debug("qualifyMEP:::createQualStmt2==>"+createQualStmt+"::status==>"+mqlCmdStatus);
			}

			// if BOM specific, Qualify [EBOM] relationship id
			if ( isBOMQual )
			{
				//createQualStmt = "add connection Qualification from " + qualObjId + " torel " + eBOMRelId;
				createQualStmt = "add connection $1 from $2 torel $3";
				//mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true);
				mqlCmdStatus = MqlUtil.mqlCommand(context, createQualStmt, true,
										CPCConstants.TYPE_QUALIFICATION, qualObjId, eBOMRelId);

				DebugUtil.debug("qualifyMEP:::createQualStmt3==>"+createQualStmt+"::status==>"+mqlCmdStatus);
			}

            ContextUtil.commitTransaction(context);
            //DebugUtil.setDebug(false);
        } catch (Exception e) {
            //DebugUtil.setDebug(false);
            e.printStackTrace();
             try{
            ContextUtil.abortTransaction(context);

             }catch(Exception ex){
                 throw (new Exception("MEP Qualification creation failed."));
             }

            throw (new FrameworkException(e));
        }
    }	// qualifyMEP

	public void clearQualifySign(Context context, String args[])
		throws Exception {
		   try{
			DebugUtil.debug("Inside the clearQualifySign::args[0]"+args[0]);

			if ( null != args[0] )
			{
				DomainObject qualObj = new DomainObject(args[0]);
				String tnrType = qualObj.getInfo(context, SELECT_TYPE);
				String tnrName = qualObj.getInfo(context, SELECT_NAME);
				String tnrRevn = qualObj.getInfo(context, SELECT_REVISION);

				DebugUtil.debug("Inside the clearQualifySign::TNR"+tnrType +"=>"+tnrName+"=>"+tnrRevn);

				// Hard-coding the signature name as it is unlikely to change (schema change)
				//String unsignStmt = "unsign businessobject \""+tnrType+"\" "+"\""+tnrName+"\" "+tnrRevn+" signature Qualify";
				String unsignStmt = "unsign businessobject \"$1\" \"$2\" $3 signature $4";

				String mqlCmdStatus = null;
				DebugUtil.debug("clearQualifySign:::unsignStmt==>"+unsignStmt+"::status==>"+mqlCmdStatus);

				//mqlCmdStatus = MqlUtil.mqlCommand(context, unsignStmt);
				mqlCmdStatus = MqlUtil.mqlCommand(context, unsignStmt, tnrType, tnrName, tnrRevn, "Qualify");
				DebugUtil.debug("clearQualifySign:::unsignStmt==>"+unsignStmt+"::status==>"+mqlCmdStatus);

			}
		   }catch(Exception err){
			   err.printStackTrace();
		   }
		}

	public void clearDisqualifySign(Context context, String args[])
	throws Exception {
	   try{
		   DebugUtil.debug("Inside the clearDisqualifySign::args[0]"+args[0]);

			if ( null != args[0] )
			{
				DomainObject qualObj = new DomainObject(args[0]);
				String tnrType = qualObj.getInfo(context, SELECT_TYPE);
				String tnrName = qualObj.getInfo(context, SELECT_NAME);
				String tnrRevn = qualObj.getInfo(context, SELECT_REVISION);

				DebugUtil.debug("Inside the clearDisqualifySign::TNR"+tnrType +"=>"+tnrName+"=>"+tnrRevn);

				// Hard-coding the signature name as it is unlikely to change (schema change)
				//String unsignStmt = "unsign businessobject \""+tnrType+"\" "+"\""+tnrName+"\" "+tnrRevn+" signature Disqualify";
				String unsignStmt = "unsign businessobject \"$1\" \"$2\" $3 signature $4";

				String mqlCmdStatus = null;
				DebugUtil.debug("clearDisqualifySign:::unsignStmt==>"+unsignStmt+"::status==>"+mqlCmdStatus);

				//mqlCmdStatus = MqlUtil.mqlCommand(context, unsignStmt);
				mqlCmdStatus = MqlUtil.mqlCommand(context, unsignStmt, tnrType, tnrName, tnrRevn, "Disqualify");
				DebugUtil.debug("clearDisqualifySign:::unsignStmt==>"+unsignStmt+"::status==>"+mqlCmdStatus);
			}
	   }catch(Exception err){
		   err.printStackTrace();
	   }
	}

	// Following methods are defined as part of highlight - Flat table to Structure Browser conversion
	// BEGIN
	/**
	 * Gets available policy states of Part.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args settings of the command
	 * @return list of available policy states
	 * @throws Exception if the operation fails
	 * @exclude
	 */
	public HashMap getFilterPartStates(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map columnMap=(Map)programMap.get("columnMap");
		Map settings=(Map)columnMap.get("settings");

		// initialize the return variable
		HashMap stateMap = new HashMap();

		String languageStr = context.getSession().getLanguage();
		String adminPartType = (String)settings.get("AdminPartType"); // type_Part or type_SupplierEquivalentPart
		String sPartType = PropertyUtil.getSchemaProperty(context, adminPartType);

		// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();

		String sAllStates = i18nNow.getI18nString("emxComponentCentral.Common.DisplayAllStates","emxComponentCentralStringResource",languageStr);

        StringList strListEquivalentPolicies = com.matrixone.apps.componentcentral.CPCPart.getPartPolicies(context,sPartType);
        int sizeOfPolicies = 0;
        if(strListEquivalentPolicies != null && (sizeOfPolicies = strListEquivalentPolicies.size()) > 0)
        {
            Policy polObj                   = null;
            int stateListSize               = 0;
            StateRequirementList stReqLst   = null;
            StateRequirement stReq          = null;
            String strPolicyName            = "";
            String strStateName             = "";

            for(int i = 0 ; i < sizeOfPolicies ; i++)
            {
                strPolicyName   = (String)strListEquivalentPolicies.get(i);
                polObj          = new Policy(strPolicyName);
                stReqLst        = polObj.getStateRequirements(context);
                stateListSize   = (stReqLst != null)? stReqLst.size() : 0 ;
                for(int j = 0 ; j < stateListSize ; j++)
                {
                    stReq   = (StateRequirement) stReqLst.get(j);
                    strStateName = stReq.getName();
                    if(!fieldRangeValues.contains(strStateName))
                    {
                        fieldRangeValues.addElement(strStateName);
                        fieldDisplayRangeValues.addElement(i18nNow.getStateI18NString(strPolicyName, strStateName, languageStr));
                    }
                }
            }
        }

        fieldRangeValues.insertElementAt("*",0);
        fieldDisplayRangeValues.insertElementAt(sAllStates,0);

		stateMap.put("field_choices", fieldRangeValues);
		stateMap.put("field_display_choices", fieldDisplayRangeValues);

		DebugUtil.debug("getFilterPartStates:stateMap==>"+stateMap);

		return stateMap;
	}

	/**
	 * Gets Part or Supplier Equivalent Part[SEP] result summary based on applied filter conditions.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args Filter conditions set by the user
	 * @returns Part or SEP results summary
	 * @throws Exception if the operation fails
	 * @exclude
	 */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllFilterPartsSummary(Context context, String[] args) throws Exception
	{
		HashMap programMap = ( HashMap ) JPO.unpackArgs( args );
		String cpcSearchType = (String)programMap.get("cpcSearchType"); // to differentiate Part or SEP search

		String  selectedState = "";
		String  selectedSupplier = "";
		if ( "partType".equals(cpcSearchType) ) // if Part search
			 selectedState = ( String ) programMap.get("CPCPartsSummaryStateFilter");
		else // SEP search
		{
			 selectedState = ( String ) programMap.get("CPCSEPartsSummaryStateFilter");
			 selectedSupplier = (String) programMap.get("CPCSEPartsSummarySupplierFilter");
		}

		String  selectedName  = ( String ) programMap.get("CPCPartsSummaryNameFilter");
		String  selectedDesc  = ( String ) programMap.get("CPCPartsSummaryDescriptionFilter");

		DebugUtil.debug("getAllFilterPartsSummary::cpcSearchType==>"+cpcSearchType);
		DebugUtil.debug("getAllFilterPartsSummary::selectedState==>"+selectedState);
		DebugUtil.debug("getAllFilterPartsSummary::selectedName==>"+selectedName);
		DebugUtil.debug("getAllFilterPartsSummary::selectedDesc==>"+selectedDesc);
		DebugUtil.debug("getAllFilterPartsSummary::selectedSupplier==>"+selectedSupplier);

		if (selectedState == null || "null".equals(selectedState) || "".equals(selectedState))
			selectedState = "*";

		if (selectedName == null || "null".equals(selectedName) || "".equals(selectedName))
			selectedName = "*";

		if (selectedDesc == null || "null".equals(selectedDesc) || "".equals(selectedDesc) )
			selectedDesc = "*";

		com.matrixone.apps.componentcentral.CPCPart partObj = new com.matrixone.apps.componentcentral.CPCPart();

		StringList selectStmts = new StringList();
		String vault = DomainConstants.QUERY_WILDCARD;
		StringBuffer whereCls = new StringBuffer();

		if(!"*".equals(selectedName))
		{
		  if(whereCls.length() > 0) {
			  whereCls.append(" && ");
		  }
		  whereCls.append(DomainConstants.SELECT_NAME+" ~= '"+selectedName+"' ");
		}

		if(!"*".equals(selectedState))
		{
		  if(whereCls.length() > 0) {
			  whereCls.append(" && ");
		  }
		  whereCls.append(DomainConstants.SELECT_CURRENT+" == '"+selectedState+"' ");
		}

		if(!"*".equals(selectedDesc))
		{
		  if(whereCls.length() > 0) {
			  whereCls.append(" && ");
		  }
		  whereCls.append(DomainConstants.SELECT_DESCRIPTION+" ~= '"+selectedDesc+"' ");
		}

		if ( "separtType".equals(cpcSearchType) ) // if SEP search only
		{
		    if( !"*".equals(selectedSupplier) )  // set condition only when supplier is selected
		    {
				if (whereCls.length() > 0) {
					whereCls.append(" && ");
				}
		        String strSupplierId = "to["+CPCConstants.RELATIONSHIP_SUPPLIED_BY+"].from.id";
		        whereCls.append(strSupplierId+" == '"+selectedSupplier+"' ");
		    }
        }

		DebugUtil.debug("getAllFilterPartsSummary::whereCls "+whereCls.toString());

		MapList filteredObjPageList = null;
		if ( "partType".equals(cpcSearchType) ) // if Part search
			 filteredObjPageList = partObj.getECParts(context, selectStmts, vault, whereCls.toString());
		else // SEP search
		     filteredObjPageList = partObj.getSupplierEquivalents(context,selectStmts,vault,whereCls.toString());

		DebugUtil.debug("getAllFilterPartsSummary::filteredObjPageList size==> "+filteredObjPageList.size());

		return filteredObjPageList;
	}

	/**
	 * Gets list of suppliers based on the logged on user.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args dummy; not used in the method
	 * @returns List of suppliers
	 * @throws Exception if the operation fails
	 * @exclude
	 */
	public HashMap getSuppliersByContext(Context context, String[] args) throws Exception
	{
		HashMap suppliersMap = new HashMap();

		String companyId = com.matrixone.apps.common.Person.getPerson(context).getCompanyId(context);
		String languageStr = context.getSession().getLanguage();

		DebugUtil.debug("getSuppliersByContext::companyId==>"+companyId);

		Map map = new HashMap(1);
		map.put("objectId", companyId);

		String[] sArgs = JPO.packArgs(map);
		String[] constructor = { null };

		String sAll = i18nNow.getI18nString("emxComponentCentral.Common.All","emxComponentCentralStringResource",languageStr);

		MapList supplierList = (MapList) JPO.invoke(context, "emxCompany", constructor, "getSuppliers",
													sArgs, MapList.class);

		DebugUtil.debug("getSuppliersByContext::supplierList==>"+supplierList);

		// initialize the Stringlists fieldRangeValues, fieldDisplayRangeValues
		StringList fieldRangeValues = new StringList();
		StringList fieldDisplayRangeValues = new StringList();

		Map tmpMap = null;
		for(int i=0; i < supplierList.size(); i++)
		{
			tmpMap = (Map)supplierList.get(i);
			fieldRangeValues.addElement((String)tmpMap.get("id"));
            fieldDisplayRangeValues.addElement((String)tmpMap.get("name"));
		}

		fieldRangeValues.insertElementAt("*",0);
		fieldDisplayRangeValues.insertElementAt(sAll,0);

		suppliersMap.put("field_choices", fieldRangeValues);
		suppliersMap.put("field_display_choices", fieldDisplayRangeValues);

		DebugUtil.debug("getSuppliersByContext::suppliersMap==>"+suppliersMap);

		return suppliersMap;
	}
	// END - Flat table to Structure Browser conversion

    /**
     * Generic method to check whether license exists before object creation
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *      0 - requestMap
     * @return Map contains created objectId
     * @throws Exception
     * @exclude
     */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createCPCObject(Context context, String[] args)
    throws Exception {

        HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
        Map returnMap       = new HashMap();

		ComponentsUtil.checkLicenseReserved(context, CPCConstants.AVC_PRODUCT_TRIGRAM);

        try {
			UIForm uiForm       = new UIForm();
            String objectId = uiForm.createObject(context, requestMap);
            returnMap.put("id", objectId);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }

        return returnMap;
    }

	// Following methods are defined as part of fix for IR-162712V6R2013x, IR-162708V6R2013x
	// BEGIN
    /**
      * Enable/Disable the command based on the context in Manufacturer Equivalent tab.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds a HashMap of the following entries:
      * objectId - a String containing the Part id
      * @return Boolean to activate command or not
      * @throws Exception if the operation fails
      * @exclude
      */
      public Boolean enableCmdsInManufacturerEquivalentsTab1(Context context,String[] args)
             throws Exception
      {
    	  HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    	  String objectId = (String) paramMap.get("objectId");
    	  Boolean hasAccess = new Boolean(false);

    	  DebugUtil.debug("enableCmdsInManufacturerEquivalentsTab1 objectId:"+objectId);
    	  DebugUtil.debug("enableCmdsInManufacturerEquivalentsTab1 paramMap:"+paramMap);

    	  DomainObject partObj = DomainObject.newInstance(context,objectId);
    	  String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
    	  String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

    	  if ( partType.equals(DomainConstants.TYPE_PART) &&
    		  (partPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART) ||
    		   partPolicy.equals(DomainConstants.POLICY_EC_PART) ||
	       	   partPolicy.equals(CPCConstants.POLICY_STANDARD_PART) ||
	       	   partPolicy.equals(CPCConstants.POLICY_CONFIGURED_PART)) )
    	  {
    		  hasAccess = new Boolean(true);
    	  }

    	  return hasAccess;
 	}

      /**
       * Enable/Disable the command based on the context in Manufacturer Equivalent tab.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds a HashMap of the following entries:
       * objectId - a String containing the Part id
       * @return Boolean to activate command or not
       * @throws Exception if the operation fails.
       * @exclude
       */
       public Boolean enableCmdsInManufacturerEquivalentsTab2(Context context,String[] args)
              throws Exception
       {
     	  HashMap paramMap = (HashMap)JPO.unpackArgs(args);
     	  String objectId = (String) paramMap.get("objectId");
     	  Boolean hasAccess = new Boolean(false);

     	  DebugUtil.debug("enableCmdsInManufacturerEquivalentsTab2 objectId:"+objectId);
     	  DebugUtil.debug("enableCmdsInManufacturerEquivalentsTab2 paramMap:"+paramMap);

     	  DomainObject partObj = DomainObject.newInstance(context,objectId);
     	  String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
     	  String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

     	  if (partType.equals(CPCConstants.TYPE_SUPPLIER_EQUIVALENT_PART) &&
     		  partPolicy.equals(CPCConstants.POLICY_SEP))
     	  {
     		  hasAccess = new Boolean(true);
     	  }

     	  return hasAccess;
  	}

  	 /**
  	  * Enable/Disable the command based on the context in Supplier Equivalent tab.
  	  *
  	  * @param context the eMatrix <code>Context</code> object
  	  * @param args holds a HashMap of the following entries:
  	  * objectId - a String containing the Part id
  	  * @return Boolean to activate command or not
  	  * @throws Exception if the operation fails
  	  * @exclude
  	  */
  	  public Boolean enableCmdsInSupplierEquivalentsTab1(Context context,String[] args)
  	         throws Exception
  	  {
  		  HashMap paramMap = (HashMap)JPO.unpackArgs(args);
  		  String objectId = (String) paramMap.get("objectId");
  		  Boolean hasAccess = new Boolean(false);

  		  DebugUtil.debug("enableCmdsInSupplierEquivalentsTab1 objectId:"+objectId);
  		  DebugUtil.debug("enableCmdsInSupplierEquivalentsTab1 paramMap:"+paramMap);

  		  DomainObject partObj = DomainObject.newInstance(context,objectId);
  		  String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
  		  String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

  		  if ( partType.equals(DomainConstants.TYPE_PART) &&
  			  (partPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART) ||
  			   partPolicy.equals(DomainConstants.POLICY_EC_PART) ||
	       	   partPolicy.equals(CPCConstants.POLICY_STANDARD_PART) ||
	       	   partPolicy.equals(CPCConstants.POLICY_CONFIGURED_PART)) )
  		  {
  			  hasAccess = new Boolean(true);
  		  }

  		  return hasAccess;
  	}

        /**
         * Enable/Disable the command based on the context in Supplier Equivalent tab.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap of the following entries:
         * objectId - a String containing the Part id
         * @return Boolean to activate command or not
         * @throws Exception if the operation fails
         * @exclude
         */
         public Boolean enableCmdsInSupplierEquivalentsTab2(Context context,String[] args)
                throws Exception
         {
       	  HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       	  String objectId = (String) paramMap.get("objectId");
       	  Boolean hasAccess = new Boolean(false);

       	  DebugUtil.debug("enableCmdsInSupplierEquivalentsTab2 objectId:"+objectId);
       	  DebugUtil.debug("enableCmdsInSupplierEquivalentsTab2 paramMap:"+paramMap);

       	  DomainObject partObj = DomainObject.newInstance(context,objectId);
       	  String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
       	  String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

       	  if (partType.equals(DomainConstants.TYPE_PART) &&
       		  partPolicy.equals(DomainConstants.POLICY_MANUFACTURER_EQUIVALENT))
       	  {
       		  hasAccess = new Boolean(true);
       	  }

       	  return hasAccess;
    	}

	/**
	 * Enable/Disable the command based on the context in Parts Equivalent tab.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a HashMap of the following entries:
	 * objectId - a String containing the Part id
	 * @return Boolean to activate command or not
	 * @throws Exception if the operation fails
	 * @exclude
	 */
    public Boolean enableCmdsInPartEquivalentsTab1(Context context, String[] args)
    		throws Exception
    {
    	HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    	String objectId = (String) paramMap.get("objectId");
    	Boolean hasAccess = new Boolean(false);

     	DebugUtil.debug("enableCmdsInPartEquivalentsTab1 objectId:"+objectId);
     	DebugUtil.debug("enableCmdsInPartEquivalentsTab1 paramMap:"+paramMap);

     	DomainObject partObj = DomainObject.newInstance(context,objectId);
     	String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
     	String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

     	if (partType.equals(DomainConstants.TYPE_PART) &&
     		partPolicy.equals(DomainConstants.POLICY_MANUFACTURER_EQUIVALENT))
     	{
     		  hasAccess = new Boolean(true);
     	}

    	return hasAccess;
    }

	/**
	 * Enable/Disable the command based on the context in Parts Equivalent tab.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds a HashMap of the following entries:
	 * objectId - a String containing the Part id
	 * @return Boolean to activate command or not
	 * @throws Exception if the operation fails
	 * @exclude
	 */
    public Boolean enableCmdsInPartEquivalentsTab2(Context context, String[] args)
    		throws Exception
    {
    	HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    	String objectId = (String) paramMap.get("objectId");
    	Boolean hasAccess = new Boolean(false);

     	DebugUtil.debug("enableCmdsInPartEquivalentsTab2 objectId:"+objectId);
     	DebugUtil.debug("enableCmdsInPartEquivalentsTab2 paramMap:"+paramMap);

     	DomainObject partObj = DomainObject.newInstance(context,objectId);
     	String partType = partObj.getInfo(context,DomainConstants.SELECT_TYPE);
     	String partPolicy = partObj.getInfo(context,DomainConstants.SELECT_POLICY);

     	if (partType.equals(CPCConstants.TYPE_SUPPLIER_EQUIVALENT_PART) &&
     		partPolicy.equals(CPCConstants.POLICY_SEP))
     	{
     		  hasAccess = new Boolean(true);
     	}

    	return hasAccess;
    }
    // END - IR-162712V6R2013x, IR-162708V6R2013x

	// Local final variables to be used as MQL bind values
	private static final String MQL_ALL_TYPE_OR_REV = "*";
	private static final String MQL_ATTR_QUAL_TYPE_ID = "attribute[Qualification Type ID]";
	private static final String MQL_BQSL_QUAL_TYPE = "BQSL*";
	private static final String MQL_BQML_QUAL_TYPE = "BQML*";
}

