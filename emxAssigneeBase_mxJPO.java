/*
 **  emxAssigneeBase.java
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


import  matrix.db.JPO;
import  java.util.Map;
import  java.util.Vector;
import  matrix.db.Context;
import  matrix.db.BusinessObjectWithSelectList;
import  java.util.HashMap;
import  java.util.Hashtable;
import  matrix.util.StringList;
import  com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import  com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.productline.ProductLineConstants;


/**
 * This JPO class has some methods pertaining to Person type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxAssigneeBase_mxJPO extends emxDomainObject_mxJPO {

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxAssigneeBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context context for this request
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine","emxProduct.Error.UnsupportedClient", language);
            throw  new Exception(strContentLabel);
        }
        return  0;
    }

    /**
     * Method call to get the list of all Assignees.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Holds the Hashmap containing the following arguments:
     *      objectId - string containing the object Id
     *      relationName - string containing the relationship name
     * @return Maplist of bus ids of Assignees
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */


    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllAssignee (Context context, String[] args) throws Exception {
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = new MapList();
        //Gets the objectids and the relation names from args
        String paramMap = (String)programMap.get("objectId");
        String relationshipName = (String)programMap.get("relationName");
        //Domain Object initialized with the object id.
        DomainObject dom = new DomainObject(paramMap);
        short sLevel = 1;
        //Gets the relationship name
        String strRelName = PropertyUtil.getSchemaProperty(context,relationshipName);
        String strType = ProductLineConstants.TYPE_PERSON;
        //The getRelatedObjects method is invoked to get the list of Assignees
        relBusObjPageList = dom.getRelatedObjects(context, strRelName, strType,
                objectSelects, relSelects, true, false, sLevel, "", "");
        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");

       return  relBusObjPageList;
    }

    /**
     * Method call to get the Name in the Last Name, First Name format.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the HashMap containing the following arguments
     *      objectList - MapList containn the list of busines objetcs
     *      paramList - HasMap containg the arguments like reportFormat,ObjectId, SuiteDirectory, TreeId
     * @return Object - Vector containing names in last name, first name format
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public Vector getCompleteName (Context context, String[] args) throws Exception {
         //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectList from args
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        HashMap paramList = (HashMap)programMap.get("paramList");
        String strReportFormat = (String) paramList.get("reportFormat");

        //Used to construct the HREF
        String strSuiteDir = (String)paramList.get("SuiteDirectory");
        String strJsTreeID = (String)paramList.get("jsTreeID");
        String strParentObjectId = (String)paramList.get("objectId");
        Vector vctCompleteName = new Vector();
        //No of objects
        int iNoOfObjects = relBusObjPageList.size();
        String strObjId = null;
        String strRelId = null;
        String strFirstName = null;
        String strLastName = null;
        String arrObjId[] = new String[iNoOfObjects];
        String arrRelId[] = new String[iNoOfObjects];
        //Getting the bus ids for objects in the table
        for (int i = 0; i < iNoOfObjects; i++) {
            Object obj = relBusObjPageList.get(i);
            if (obj instanceof HashMap) {
                arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                arrRelId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }
            else if (obj instanceof Hashtable)
            {
                arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                arrRelId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            }
        }
        StringList listSelect = new StringList(2);
        String strAttrb1 = "attribute[" + ProductLineConstants.ATTRIBUTE_FIRST_NAME+ "]";
        String strAttrb2 = "attribute[" + ProductLineConstants.ATTRIBUTE_LAST_NAME+ "]";
        listSelect.addElement(strAttrb1);
        listSelect.addElement(strAttrb2);

        //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
        BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

        for (int i = 0; i < iNoOfObjects; i++) {
            strObjId = arrObjId[i];
            strRelId = arrRelId[i];
            strFirstName = attributeList.getElement(i).getSelectData(strAttrb1);
            strLastName = attributeList.getElement(i).getSelectData(strAttrb2);
            //Constructing the HREF
            String strFullName = null;
            if(strReportFormat!=null&&strReportFormat.equals("null")==false&&strReportFormat.equals("")==false)
            {
                  strFullName = strLastName + " " + strFirstName;
            }
            else
            {
            	// Modified to fix IR-013980
            	strFullName = "<img src = \"images/iconSmallPerson.gif\"></img><b><A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?" 
                    + "mode=insert&amp;name="+XSSUtil.encodeForHTMLAttribute(context,strLastName)+XSSUtil.encodeForHTMLAttribute(context,strFirstName)+"&amp;treeMenu=type_PLCPerson&amp;emxSuiteDirectory="
                    + XSSUtil.encodeForHTMLAttribute(context,strSuiteDir) + "&amp;relId=" + XSSUtil.encodeForHTMLAttribute(context,strRelId) + "&amp;parentOID="
                    + XSSUtil.encodeForHTMLAttribute(context,strParentObjectId) + "&amp;jsTreeID=" + XSSUtil.encodeForHTMLAttribute(context,strJsTreeID) + "&amp;objectId="
                    + XSSUtil.encodeForHTMLAttribute(context,strObjId) + "', 'null', 'null', 'false', 'popup')\" class=\"object\">"
                    + XSSUtil.encodeForHTML(context,strLastName) + ", " + XSSUtil.encodeForHTML(context,strFirstName) + "</A></b>";
            }
            //Adding into the vector
            vctCompleteName.add(strFullName);
        }
        return  vctCompleteName;
    }

    /**
     * Method call to get the email as an link to send mails using the client.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the HashMap containing the following arguments
     *      objectList - MapList containn the list of busines objetcs
     *      paramList - HasMap containg the argument reportFormat
     * @return Object - Vector of email ids
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public Vector getAssigneeEmail (Context context, String[] args) throws Exception {
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectList from args
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        Vector vctAssigneeMailList = new Vector();
        HashMap paramList = (HashMap)programMap.get("paramList");
        String strReportFormat=(String)paramList.get("reportFormat");

        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");
        //Number of objects
        int iNoOfObjects = relBusObjPageList.size();
        String arrObjId[] = new String[iNoOfObjects];
        //Getting the bus ids for objects in the table
        for (int i = 0; i < iNoOfObjects; i++) {
            Object obj = relBusObjPageList.get(i);
            if (obj instanceof HashMap) {
                arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
            }
            else if (obj instanceof Hashtable)
            {
                arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
            }
        }

        StringList listSelect = new StringList(1);
        String strAttrb1 = "attribute[" + ProductLineConstants.ATTRIBUTE_EMAIL_ADDRESS+ "]";
        listSelect.addElement(strAttrb1);

        //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
        BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

        for (int i = 0; i < iNoOfObjects; i++) {
            //Getting the email ids from the Map
            String strEmailId = attributeList.getElement(i).getSelectData(strAttrb1);
            String strEmailAdd = null;
           if(strReportFormat!=null&&strReportFormat.equals("null")==false&&strReportFormat.equals("")==false)
            {
                vctAssigneeMailList.add(strEmailId);
            }
            else
            {
                strEmailAdd = "<B><A HREF=\"mailto:" + XSSUtil.encodeForHTMLAttribute(context,strEmailId) + "\">" + XSSUtil.encodeForHTML(context,strEmailId)+ "</A></B>";
                vctAssigneeMailList.add(strEmailAdd);
            }
        }
        return  vctAssigneeMailList;
    }

    /**
     * Method call to get the email as an link to send mails using the client for the webform.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Holds the HashMap containing the following arguments:
     *          paramMap - HashMap containing the object id.
     * @return Object - Vector of email ids
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public String getEmailId (Context context, String[] args) throws Exception {
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectList from args
        HashMap relBusObjPageList = (HashMap)programMap.get("paramMap");
        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");
        //Number of objects
        String strObjIdArray = "";
        String strAttrb1 = "attribute[" + ProductLineConstants.ATTRIBUTE_EMAIL_ADDRESS
                + "]";
        StringList listSelectIds = new StringList(1);
        listSelectIds.addElement(strAttrb1);
        //Getting the object ids
        try {
            strObjIdArray = (String)relBusObjPageList.get("objectId");
        } catch (Exception e) {
                            throw e;
        }
        //Domain Object initialized with the object id.
        DomainObject domObj = new DomainObject(strObjIdArray);
        //Creating a Map of email id of objects
        Map mapIds = domObj.getInfo(context, listSelectIds);
        //Getting the email ids from the Map
        String strEmailId = (mapIds.get(strAttrb1)).toString();
        String strEmailAdd = null;
        strEmailAdd = "<B><A HREF=\"mailto:" + strEmailId + "\">" + strEmailId
                + "</A>";
        return  (strEmailAdd);
    }

    /**
     * Method call to get the Web site as a hyperlink for the webform.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args Holds the HashMap containing the following arguments:
     *          paramMap - HashMap containing the object id.
     * @return Object - String containing the HTML tag.
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public String getWebSite (Context context, String[] args) throws Exception {
        //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectList from args
        HashMap relBusObjPageList = (HashMap)programMap.get("paramMap");
        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");
        //Number of objects
        String strObjIdArray = "";
        String strAttrb1 = "attribute[" + ProductLineConstants.ATTRIBUTE_WEB_SITE
                + "]";
        StringList listSelectIds = new StringList(1);
        listSelectIds.addElement(strAttrb1);
        //Getting the object ids
        try {
            strObjIdArray = (String)relBusObjPageList.get("objectId");
        } catch (Exception e) {
                        throw e;
        }
        //Domain Object initialized with the object id.
        DomainObject domObj = new DomainObject(strObjIdArray);
        //Creating a Map of email id of objects
        Map mapIds = domObj.getInfo(context, listSelectIds);
        //Getting the email ids from the Map
        String strWebSite = (mapIds.get(strAttrb1)).toString();
        String strWebSiteAdd = null;
        strWebSiteAdd = "<B><A HREF=\"" + strWebSite + "\">" + strWebSite +
                "</A>";
        return  (strWebSiteAdd);
    }
}
