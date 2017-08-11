/*
 * emxClassificationBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 * static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 16:02:33 2008 przemek Experimental przemek $";
 */

import com.matrixone.apps.document.DCWorkspaceVault;
import com.matrixone.apps.library.LibraryCentralConstants;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import matrix.db.BusinessObject;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.Vault;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.classification.Classification;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import java.util.HashMap;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;

/**
 * The <code>emxClassificationBase</code> class.
 *
 */
public class emxClassificationBase_mxJPO extends emxLibraryCentralCommon_mxJPO

{

    /**
     * Creates the ${CLASSNAME} Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @throws Exception if the operation fails
     */

    public emxClassificationBase_mxJPO (Context context,
                         String[] args) throws Exception
    {
        super(context, args);
    }



    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <String[]</code> object
     * @return the Java <code>int</code>
     * @throws Exception if the operation fails
     * @exclude
     */

    public int mxMain (Context context, String[] args ) throws Exception
    {
        if ( true )
        {
            throw new Exception ("Do not call this method!");
        }

        return 0;
    }

    /**
     * JPO invocation decoding wrapper for method below.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
        0 - strPartId The id of the part
        1- strparentObj Id The id of the parent object
        2 - strPartType The type of the part
        3 - strName The name of the part
        4 - strPartRevision The revision of the part
        5 - strPartPolicy The policy of the part
        6 - strDescription The description of the part
    * @return a Map containing the part object and boolean state whether the part exists or not
    * @throws FrameworkException if the operation fails
    */
    public static Map createAndConnectPart(Context context, String[] args)
    throws FrameworkException {
    try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strparentObjId = (String) programMap.get("strparentObjId");
        String strPartId = (String) programMap.get("strPartId");
        String strPartType = (String) programMap.get("strPartType");
        String strName = (String) programMap.get("strName");
        String strPartRevision = (String) programMap.get("strPartRevision");
        String strPartPolicy = (String) programMap.get("strPartPolicy");
        String strDescription = (String) programMap.get("strDescription");
        return createAndConnectPart(context,strPartId,strparentObjId,strPartType,strName, strPartRevision,strPartPolicy,strDescription);
       } catch (Exception e) {
            throw new FrameworkException (e);
       }
    }

    /**
     * Creates and Connects a Part to the Parent Object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strPartId The id of the part
     * @param strparentObjId The id of the parent object
     * @param strPartType The type of the part
     * @param strName The name of the part
     * @param strPartRevision The revision of the part
     * @param strPartPolicy The policy of the part
     * @param strDescription The description of the part
     * @return a Map containing the part object and boolean state whether the part exists or not
     * @throws FrameworkException if the operation fails
     */

    public static Map createAndConnectPart(Context context,String strPartId,String strparentObjId,String strPartType,String strName, String strPartRevision,String strPartPolicy,String strDescription)
    throws FrameworkException {

        boolean bPartExists = false;
        Map partMap = new HashMap();
        try {
            BusinessObject busPart = new BusinessObject(strPartType, strName, strPartRevision, context.getVault().getName() );
            if(strPartId == null || "".equalsIgnoreCase(strPartId)) {
                bPartExists = busPart.exists(context);
            }
            if(bPartExists){
                partMap.put("bPartExists",new Boolean(bPartExists));
                partMap.put("busPart",busPart);
                return partMap;
            }
            if(strPartId == null || "".equalsIgnoreCase(strPartId)) {
                busPart.create(context, strPartPolicy);
            } else {
                busPart = new BusinessObject(strPartId);
            }
            strPartId = busPart.getObjectId();

            if (strDescription != null) {
                strDescription = strDescription.trim();
            }
            //Update the description of the object
            busPart.setDescription(context,strDescription);


            if (strparentObjId != null && !strparentObjId.equals("") && !strparentObjId.equals("null") ) {
                BusinessObject parentObj = new BusinessObject(strparentObjId);
                parentObj.open(context);
                String relationshipName = LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM;
                parentObj.connect(context, new RelationshipType(relationshipName), true, busPart);
                parentObj.close(context);
            }

            partMap.put("bPartExists",new Boolean(bPartExists));
            partMap.put("busPart",busPart);

        } catch (Exception e) {
            throw new FrameworkException (e);
        }
        return partMap;
    }

    /**
     * Adds the Classified Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      0 - parentId is the id of the parent object
     *      1 - childIds is the array of the child objects to be added
     * @return The string result of mql command execution
     * @throws FrameworkException if the operation fails
     */
    public static String addEndItems(Context context, String[] args)
    throws FrameworkException {
    try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String parentId = (String) programMap.get("parentId");
        String[] childIds = (String[])programMap.get("childIds");
        return addEndItems(context,parentId,childIds);
       } catch (Exception e) {
            throw new FrameworkException (e);
       }
    }

    /**
     * Adds the Classified Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param parentId is the id of the parent object
     * @param childIds is the array of the child objects to be added
     * @return The string result of mql command execution
     * @throws FrameworkException if the operation fails
     */
    public static String addEndItems(Context context, String parentId, String[] childIds)
    throws FrameworkException
    {
    try{
        DomainObject parentobj = new DomainObject(parentId);

        String sParentType = parentobj.getInfo(context,DomainObject.SELECT_TYPE);
        String strVault = parentobj.getVault();
        BusinessType busType = new BusinessType(sParentType,new Vault(strVault));
        String strParentType = busType.getParent(context);
        String sRelSubclass = LibraryCentralConstants.RELATIONSHIP_SUBCLASS;
        String workSpaceVault = PropertyUtil.getSchemaProperty(context,
                                                "type_ProjectVault");

        //In case of Adding DC Types to workSpace Vault

        if(sParentType.equals(workSpaceVault))
        {
            String []folderIds= new String[1];
            folderIds[0]=parentId;
            String objNameNotAdded =
            DCWorkspaceVault.addToFolders(context,folderIds, childIds);
        }
        else
        {
            if(parentobj.isKindOf(context, LibraryCentralConstants.TYPE_CLASSIFICATION))
            {
                DomainRelationship rel = new DomainRelationship();
                rel.connect(context, parentobj, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, true, childIds);
            }
        }
        String strQuery     = "expand bus $1 to relationship $2 recurse to all select bus $3 dump $4";
        String strTemp      = "";
        String strResultID  = "";

        String strResult    = MqlUtil.mqlCommand(context,strQuery, parentId, sRelSubclass, "id", ",");
        return strResult;
    }catch (Exception e) {
            throw new FrameworkException (e);
       }
    }

    /**
     * This method returns Classification Ids for a given object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - objectId
     * @return StringList - Classification Path List
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getClassificationIds(Context context,String[] args)
    throws FrameworkException
    {
        MapList classificationIds   = new MapList();
        try {
            HashMap paramMap            = (HashMap)JPO.unpackArgs(args);
            String objectId             = (String)paramMap.get("objectId");
            SelectList selectStmts      = new SelectList(2);
            selectStmts.add(DomainObject.SELECT_ID);
            selectStmts.add(DomainObject.SELECT_NAME);
            DomainObject domObj         = new DomainObject(objectId);
            classificationIds           = (MapList)domObj.getRelatedObjects(context,
                                            LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,   // relationship pattern
                                            LibraryCentralConstants.QUERY_WILDCARD,                 // type pattern
                                            selectStmts,        // Object selects
                                            null,               // relationship selects
                                            true,               // from
                                            false,              // to
                                            (short)1,           // expand level
                                            null,               // object where
                                            null,               // relationship where
                                            0);                 // limit
        }catch (Exception ex) {
            throw new FrameworkException (ex);
        }
        return classificationIds;
    }

    /**
     * This method returns Classification path
     * ex. PL1 -> PF1
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - objectList
     * @return StringList - Classification Path List
     * @throws Exception if the operation fails
     */
    public Vector getClassificationPath(Context context,String[] args)
    throws FrameworkException
    {
        Vector classficationPaths   = new Vector();

        try
        {
            HashMap programMap          = (HashMap) JPO.unpackArgs(args);
            MapList objectList          = (MapList)programMap.get("objectList");
            HashMap paramMap            = (HashMap) programMap.get("paramList");
            String reportFormat         = (String)paramMap.get("reportFormat");
            SelectList selectStmts      = new SelectList(3);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            Iterator objectListItr      = objectList.iterator();

            String elemSeperator        = "<img style=\"padding-left:2px; padding-right:2px;\" src=\"../common/images/iconTreeToArrow.gif\"></img>";
            String pathWrapperOpen      = "<span>";
            String pathWrapperClose     = "</span>";

            if (reportFormat != null && "CSV".equals(reportFormat)) {
                elemSeperator    = " -> ";
                pathWrapperOpen  = "";
                pathWrapperClose = "";
            }

            while(objectListItr.hasNext())
            {
                Map objectMap           = (Map) objectListItr.next();
                String ObjectId         = (String)objectMap.get(DomainConstants.SELECT_ID);
                String className        = (String)objectMap.get(DomainConstants.SELECT_NAME);
                StringList elemHtmlList = new StringList();
                DomainObject domObj     = new DomainObject(ObjectId);
                if (UIUtil.isNullOrEmpty(className)) {
                    className           = (String)domObj.getInfo(context, DomainConstants.SELECT_NAME);
                }
                MapList parentClasses   = (MapList)domObj.getRelatedObjects(context,
                                            LibraryCentralConstants.RELATIONSHIP_SUBCLASS,  // relationship pattern
                                            LibraryCentralConstants.QUERY_WILDCARD,         // type pattern
                                            selectStmts,        // Object selects
                                            null,               // relationship selects
                                            true,               // from
                                            false,              // to
                                            (short)0,           // expand level
                                            null,               // object where
                                            null,               // relationship where
                                            0);                 // limit
                parentClasses           = parentClasses.sortStructure(context,DomainConstants.SELECT_LEVEL,"descending","emxSortNumericAlphaSmallerBase");
                Iterator itr            = parentClasses.iterator();
                while (itr.hasNext())
                {
                    Map parentClassMap      = (Map) itr.next();
                    String parentclassName  = (String)parentClassMap.get(DomainConstants.SELECT_NAME);
                    String parentClassId    = (String)parentClassMap.get(DomainConstants.SELECT_ID);
                    elemHtmlList.add(renderPathElem(context, parentclassName,parentClassId,reportFormat ));
                }
                elemHtmlList.add(renderPathElem(context, className,ObjectId,reportFormat));
                classficationPaths.add(pathWrapperOpen +FrameworkUtil.join(elemHtmlList, elemSeperator) +pathWrapperClose);
            }
        }
        catch(Exception err)
        {
            throw new FrameworkException (err);
        }
        return classficationPaths;
    }

    /**
     * This method renders the href for given object
     *
     * @param strName the name of the object
     * @param strObjectId the objectId
     * @return String - href
     * @throws Exception if the operation fails
     */
    public String renderPathElem(Context context, String strName, String strObjectId, String reportFormat)
    throws FrameworkException
    {
        if (reportFormat != null) {
            return strName;
        }
        /*If the Library/Class name contains "&" character, we need to replace it with the code "amp;" because getXML() of BPS code base
        doesn't support the "&" character.
        IR-261969 */
        if(strName.contains("&"))
        {
        	String test = strName.replaceAll("&", "&amp;");
        	strName = test.trim();
        }
        /* removed the encodeForHTML wrap, as it is messing up when french chars are present in the object name
        BPS does this again for programHTML fields inside SB code, which causing double encoding
        IR-261969 */
         return "<a href=\"javascript:void(0)\" onClick=\"javascript:showModalDialog('../common/emxTree.jsp?objectId="
         + XSSUtil.encodeForJavaScript(context, strObjectId) + "','860','520');\" >" + XSSUtil.encodeForXML(context, strName) + "</a>";
    }

    /**
     * This method returns exclude class id for classify/reclassify
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     * @return StringList - exlussion Ids
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getClassificationExclusionIds(Context context,String[] args)
    throws FrameworkException
    {
        StringList slExclusionIds  = new StringList();
        try {
            HashMap paramMap        = (HashMap)JPO.unpackArgs(args);
            String objectId         = (String)paramMap.get("objectId");
            DomainObject domObj     = new DomainObject(objectId);
            slExclusionIds          = domObj.getInfoList(context, "to["+ LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM+"].from.id");
            StringBuffer sbWhereExpression  = new StringBuffer("current.access[fromconnect]~~FALSE");

            // find the allowed types used for search
            String[] types     = new String[0];
            String field       = (String)paramMap.get("field");
            if(!UIUtil.isNullOrEmpty(field)){
                StringTokenizer fieldValues = new StringTokenizer(field,"=:");
                while(fieldValues.hasMoreTokens()){
                    String fieldValue  = fieldValues.nextToken();
                    if(fieldValue.equalsIgnoreCase("TYPES")){
                        types = fieldValues.nextToken().split(",");
                        break;
                    }
                }
            }
            // find the objects of allowed types and not having fromconnect access
            StringList objSelects   = new StringList();
            objSelects.add(DomainConstants.SELECT_ID);
            for(int i=0;i<types.length;i++){
                MapList mlExcludeOIDs = DomainObject.findObjects(context, types[i], "*", sbWhereExpression.toString(), objSelects);
                for(int j=0;j<mlExcludeOIDs.size();j++){
                    Map map = (Map)mlExcludeOIDs.get(j);
                    String excludeOID = (String)map.get(DomainObject.SELECT_ID);
                    if(!slExclusionIds.contains(excludeOID)){
                        slExclusionIds.add(excludeOID);
                    }
                }
            }
        }catch (Exception ex) {
            throw new FrameworkException (ex);
        }
        return slExclusionIds;
    }

    /**
     * This method returns search query for Library field
     * during classification/reClassification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - rowIds
     * @return String - the search query
     * @throws Exception if the operation fails
     */
    public String getLibrayDynamicQuery(Context context,String args[])
    throws Exception {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap)programMap.get("requestMap");
        String objectId     = (String) requestMap.get("objectId");
        DomainObject doObj  = new DomainObject(objectId);
        String type         = doObj.getInfo(context,DomainObject.SELECT_TYPE);
        String dyanmicQuery = "";
        if (doObj.isKindOf(context, TYPE_PART)) {
            dyanmicQuery    = "TYPES=type_GeneralLibrary,type_PartLibrary";
        }else if (type.equals(PropertyUtil.getSchemaProperty(context,"type_Document"))
                  ||type.equals(PropertyUtil.getSchemaProperty(context,"type_GenericDocument"))) {
            dyanmicQuery    = "TYPES=type_GeneralLibrary,type_Library";
        }
        return dyanmicQuery;
    }

    /**
     * This method returns search query for Class field
     * during classification/reClassification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - object Id
     *        1 - rowIds
     * @return String - the search query
     * @throws Exception if the operation fails
     */
    public String getClassDynamicQuery(Context context,String args[])
    throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        Map fieldValuesMap  = (HashMap)programMap.get("fieldValues");
        HashMap requestMap  = (HashMap)programMap.get("requestMap");
        String objectId     = (String) requestMap.get("objectId");
        String dyanmicQuery = "";
        DomainObject doObj  = new DomainObject(objectId);
        String type         = doObj.getInfo(context,DomainObject.SELECT_TYPE);
        if (doObj.isKindOf(context, TYPE_PART)) {
            dyanmicQuery    = "TYPES=type_GeneralClass,type_PartFamily";
        }else if (type.equals(PropertyUtil.getSchemaProperty(context,"type_Document"))
                  ||type.equals(PropertyUtil.getSchemaProperty(context,"type_GenericDocument"))) {
            dyanmicQuery    = "TYPES=type_GeneralClass,type_DocumentFamily";
        }
        String librayId     = fieldValuesMap.containsKey("LibraryOID") ? (String)fieldValuesMap.get("LibraryOID") : "";
        dyanmicQuery        += !"".equals(librayId) ? ":REL_SUBCLASS_FROM_ID="+librayId : "";

        return dyanmicQuery;
    }

    /**
     * This method updates Class filed during classification/reclassification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - objectId
     *        1 - rowIds
     *        2 - ClassOID
     * @throws Exception if the operation fails
     */
    public void updateClassField(Context context, String[] args)
    throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap          = (HashMap) programMap.get("requestMap");
        String classificationMode   = extractVal(requestMap.get("classificationMode"));
        String objectId             = extractVal(requestMap.get("objectId"));
        String newClassId           = extractVal(requestMap.get("ClassOID"));
        StringList slNewClassIds    = FrameworkUtil.split(newClassId, "|");
        if(classificationMode != null && !"null".equals(classificationMode) && "classification".equals(classificationMode)) {
            for (int i = 0; i < slNewClassIds.size() ; i++) {
                Classification.addEndItems(context, (String)slNewClassIds.get(i), new String[]{objectId});
            }
        }else if(classificationMode != null && !"null".equals(classificationMode) && "reClassification".equals(classificationMode)) {
            String oldClassId       = extractVal(requestMap.get("selectedClassIds"));
            for (int i = 0; i < slNewClassIds.size() ; i++) {
                oldClassId = (i == 0)? oldClassId :null;
                Classification.reclassify(context, new String[]{objectId}, oldClassId, (String)slNewClassIds.get(i));
            }
        }
    }

    /**
     * This method is necessary because some forms, e.g. emxForm.jsp, submit
     * requestMaps wherein each value is an array of strings, out of which
     * we will always want the first element.  Most other forms submit a
     * string value for each param value.  This method hides that difference.
     *
     * @param valObj the  string/string array
     * @returns String
     * @throws Exception if the operation fails
     */
    private static String extractVal(Object valObj) {
        String[] strArr = {};
        if (valObj !=null && valObj.getClass() == strArr.getClass()) {
            return ((String[])valObj)[0];
        } else if (valObj !=null && valObj.getClass() == String.class) {
            return (String)valObj;
        } else {
            return "";
        }
    }
}
