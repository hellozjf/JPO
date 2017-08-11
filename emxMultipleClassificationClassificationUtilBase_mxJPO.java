/*
 *  emxMultipleClassificationClassificationUtilBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 16:54:22 2008 przemek Experimental przemek $";
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;


import com.matrixone.apps.classification.AttributeGroup;
import com.matrixone.apps.classification.ClassificationConstants;
import com.matrixone.apps.classification.ClassificationUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.MapList;


/**
 * The <code>emxMultipleClassificationClassificationUtilBase</code> class contains utility methods for
 * Clasifications
 *
 *  @exclude
 */
public class emxMultipleClassificationClassificationUtilBase_mxJPO extends
        emxDomainObject_mxJPO implements ClassificationConstants {





    protected static String _lang;

    // In this map, the key is an interface name, and the value is
    // a StringList of id and name of the corresponding Classification object
    //public static HashMap _if2clsInfo = new HashMap();


    /**
     *
     * Creates emxMultipleClassificationClassificationUtilBase object
     * @param context the eMatrix <code>Context</code> object
     * @param args  holds no arguments
     * @throws Exception if the operation fails
     */
    public emxMultipleClassificationClassificationUtilBase_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @exclude
     */
    public int mxMain(Context context, String[] args) throws FrameworkException {
        if (!context.isConnected())
            throw new FrameworkException("not supported on desktop client");
        return 0;
    }

    /** 
     * This mehtod sorts list of attribute group names by their i18n looked up name
     *  
     * @param objNames list of Object name
     * @param type the type name
     * @return StringList of the i18 names
    */
    public static StringList sortAdminObjectsByI18NName(StringList objNames, String type) {
        StringList i18nNames = new StringList();
        Iterator i = objNames.iterator();
        while (i.hasNext()) {
            String objName = (String) i.next();
            try {
                String uiName = i18nNow.getAdminI18NString(type, objName, _lang);
                i18nNames.addElement(uiName);
            } catch (MatrixException me) {
                i18nNames.addElement(objName);
            }
        }
        return sortByCorrespondence(objNames, i18nNames);
    }

    /**
     * THis method sorts first list, but the sort is based on comparisons
     * made on corresponding items in second list.  Returns new list (not in-place sort).
     * 
     * @param sortThis the list to be sorted
     * @param basedOnThis the list used for comparisoon for sorts
     * @return StringList the sorted List
     */
    public static StringList sortByCorrespondence(StringList sortThis, StringList basedOnThis) {
        int[] map = new int[sortThis.size()];
        basedOnThis.sortMap(map);
        StringList sorted = new StringList(map.length);
        for (int i=0; i < map.length; i++) {
            sorted.addElement(sortThis.get(map[i]));
        }
        return sorted;
    }

    /** 
     * The method sorts list of attribute group names by their i18n looked up name
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - attributeGroupList the list of Attribute Groups
     *      1 - languageStr language
     * @return StringList the sorted list
     * @throws Exception if the operation fails
     */
    public static StringList sortAttributeGroups(Context context, String[] args) throws Exception {

        StringList attributeGroups = new StringList();
        try
        {
            HashMap Map = (HashMap) JPO.unpackArgs(args);
            attributeGroups = (StringList)Map.get("attributeGroupList");
            _lang =  (String)Map.get("languageStr");

        }
        catch(Exception ex)
        {
            throw ex;
        }

        return sortAdminObjectsByI18NName(attributeGroups, "type");
    }

    /** 
     * The method sorts list of attribute names by their i18n looked up name
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - attributeGroupList the list of Attributes 
     *      1 - languageStr language
     * @return StringList the sorted list
     * @throws Exception if the operation fails
     */
    public static StringList sortAttributes(Context context, String[] args) throws Exception {
        StringList attributeNames = new StringList();
        try
        {
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            attributeNames = (StringList)paramMap.get("attributeList");
            _lang =  (String)paramMap.get("languageStr");

        }
        catch(Exception ex)
        {
            throw ex;
        }

        return sortAdminObjectsByI18NName(attributeNames, "attribute");
    }


    /** 
     * The method sorts list of classification interface names by by the classification object's display name
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - attributeGroupList the list of classification interface names
     *      1 - languageStr language
     * @return StringList the sorted list
     * @throws Exception if the operation fails
     */
    public static StringList sortClassifications(Context context, String[] args) throws Exception {
        StringList classifications = new StringList();
        StringList l = new StringList();
        try
        {

            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            classifications = (StringList)paramMap.get("classificationInterfaces");
           _lang =  (String)paramMap.get("languageStr");

           HashMap if2clsName = new HashMap();
           // Now, for each classification interface get the corresponding
           // classification object. Fetch the id, and the name.

        String getClsNameCmd        = "temp query bus $1 $2 $3 where \"$4\" select \"$5\" name dump $6 recordsep $7";
        emxLibraryCentralUtil_mxJPO.commaPipeQueryToMapSkipTNR(context, getClsNameCmd.toString(), true, if2clsName,
                                                                "Classification",
                                                                "*",
                                                                "*",
                                                                "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"] matchlist '" + FrameworkUtil.join(classifications, ",") + "' ','",
                                                                "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"]",
                                                                ",",
                                                                "|"
                                                               );

            Iterator i          = classifications.iterator();
            while (i.hasNext()) {
                String clsIfName = (String) i.next();
                String clsUiName = (String) ((StringList)(if2clsName.get(clsIfName))).get(0);
                l.addElement(clsUiName);
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        return sortByCorrespondence(classifications, l);
    }

    /**
     * The method returns the sorted attributes group and group names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId The id of the classification object
     * @param langStr language
     * @return The list of attribute group names and the attribute list
     * @throws Exception if the operation fails
     */

    public static Map getSortAttributeGroups(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId    = (String)programMap.get("objectId");
        String langStr = (String)programMap.get("languageStr");
        HashMap sortAttributeGroupsList = new HashMap();
        
        boolean hierarchyParentOrChild = ((Boolean) programMap.get("derivedOrDerivative")).booleanValue();
        boolean allDerivatives = ((Boolean)programMap.get("allDerivatives")).booleanValue();
        boolean allAncestors = ((Boolean)programMap.get("allAncestors")).booleanValue();

        String  strTypeClassification =  ClassificationConstants.TYPE_CLASSIFICATION;
        DomainObject domainObject     = new DomainObject(objectId);
        String strTypeOfTheObject     = domainObject.getType(context);

        if (strTypeOfTheObject == null || "null".equals(strTypeOfTheObject) || "".equals(strTypeOfTheObject))
        {
            throw (new FrameworkException("Failed to find the parent type of object with id'"+objectId+"'"));
        }

        BusinessType businessType = new BusinessType(strTypeOfTheObject, context.getVault());
        String strParentType = businessType.getParent(context);

        if (strParentType == null || "null".equals(strParentType) || "".equals(strParentType))
        {
            throw (new FrameworkException("Failed to find the parent type of object with id'"+objectId+"'"));
        }

        StringList strlistAttributeGroups = new StringList();

        if (strParentType != null)
        {
            
            /*Start - Search Within enhancement - Modified following code block to get ancestor attribute groups 
            or child attribute groups based on the parameters passed to this page.*/

            if(strTypeClassification!=null && strTypeClassification.equals(strParentType))
            {
                com.matrixone.apps.classification.Classification objClassification = (com.matrixone.apps.classification.Classification)DomainObject.newInstance (context, objectId, ClassificationConstants.TYPE_CLASSIFICATION);

                strlistAttributeGroups = objClassification.getAttributeGroups(context, 
                                                                            hierarchyParentOrChild,
                                                                            allAncestors,
                                                                            allDerivatives);
            }
            else if(ClassificationConstants.TYPE_LIBRARIES.equals(strParentType) && !hierarchyParentOrChild)
            {
                StringList objectSelects = new StringList(1);
                MapList subClassObjects = new MapList();
                objectSelects.addElement(DomainConstants.SELECT_ID);
                DomainObject libDomObj = new DomainObject(objectId);
                subClassObjects = libDomObj.getRelatedObjects(context,
                                                            LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
                                                            "*",
                                                            objectSelects,null,false,true,(short)1,"","",null,null,null);
                Iterator itrSubClassObjects = subClassObjects.iterator();
                Map mapSubClassObjects = null;

                while(itrSubClassObjects.hasNext())
                {
                    
                    mapSubClassObjects = (Map)itrSubClassObjects.next();
                    String subClassId = (String)mapSubClassObjects.get(DomainConstants.SELECT_ID);
                    com.matrixone.apps.classification.Classification objClassification = (com.matrixone.apps.classification.Classification)DomainObject.newInstance (context, subClassId, ClassificationConstants.TYPE_CLASSIFICATION);

                    StringList libraryClassesAttributeGroupList = objClassification.getAttributeGroups(context, true, allAncestors, allDerivatives);
                    
                    strlistAttributeGroups.addAll(libraryClassesAttributeGroupList);
                    
                    libraryClassesAttributeGroupList = objClassification.getAttributeGroups(context, false, allAncestors, allDerivatives);

                    strlistAttributeGroups.addAll(libraryClassesAttributeGroupList);

                }
            }
            //End - Search Within enhancement
        }

        //To remove the duplicates in resultant attribute group list, 
        //added to new Set and back to the StringList
        Set hashSetAttributeGroups = new HashSet(strlistAttributeGroups);

        strlistAttributeGroups = new StringList(hashSetAttributeGroups.size());
        strlistAttributeGroups.addAll(hashSetAttributeGroups);
        strlistAttributeGroups = ClassificationUtil.sortAttributeGroups(context,strlistAttributeGroups,langStr);

        sortAttributeGroupsList.put("sortedAttributeGroupList", strlistAttributeGroups);

        if (strlistAttributeGroups != null)
        {
            for (int i=0; i<strlistAttributeGroups.size(); i++)
            {
                String strAttributeGroupName = (String)strlistAttributeGroups.elementAt(i);

                if (strAttributeGroupName == null || "null".equals(strAttributeGroupName) || "".equals(strAttributeGroupName))
                {
                    continue;
                }
                AttributeGroup attrGroup = AttributeGroup.getInstance(context, strAttributeGroupName);

                if (attrGroup == null)
                {
                    throw (new FrameworkException("Failed to create AttributeGroup object for the attribute group '"+strAttributeGroupName+"'"));
                }

                // Find the attributes in this attribute group
                StringList strlistAttributes = attrGroup.getAttributes();
                if(strlistAttributes!=null)
                {
                    strlistAttributes = ClassificationUtil.sortAttributes(context,strlistAttributes,langStr);
                }
                sortAttributeGroupsList.put(strAttributeGroupName, strlistAttributes);
            }
        }
        return sortAttributeGroupsList;
    }
}
