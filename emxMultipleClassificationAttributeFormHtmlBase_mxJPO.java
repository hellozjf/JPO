/*
 *  emxMultipleClassificationAttributeFormHtmlBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.27 Tue Oct 28 19:04:45 2008 przemek Experimental przemek $";
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.classification.Classification;
import com.matrixone.apps.classification.ClassificationConstants;
import com.matrixone.apps.classification.ClassificationUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIForm;
import com.matrixone.apps.framework.ui.UINavigatorUtil;


/**
 * The <code>emxMultipleClassificationAttributeFormHtmlBase</code> class manages the Clasification HTML outputs.
 *
 *  @exclude
 */
public class emxMultipleClassificationAttributeFormHtmlBase_mxJPO extends
        emxDomainObject_mxJPO implements ClassificationConstants {

    Context _context;

    // These two HashMaps are for minor optimizations. We gather up
    // some information about each classification and each attribute group
    // up front, with minimal mql queries.

    // In this map, the key is the attribute group name (interface name) and
    // the value is a StringList of the attributes for that group
    protected Map _ag2attrs = new HashMap();

    // In this map, the key is the attribute name, and the value
    // is the attribute's value for the object being rendered
    protected Map _attr2val = null;

    // In this map, the key is an interface name, and the value is
    // a StringList of id and name of the corresponding Classification object
    protected HashMap _if2clsInfo = new HashMap();

    // In this map, the key is an interface name corresponding to a classification
    // and the value is a StringList of interface names representing the attribute
    // groups for that classification
    protected HashMap _clsif2ags = new HashMap();

    protected HashMap _requestMap = null;
    protected HashMap _settingMap = null;

    // Maximum number of colums in the table
    protected int _maxCol;

    // For i18n purposes
    protected String _lang;

    //To Display dateFormat
    protected String _localDateFormat;

    protected String _allowKeyableDates = "false";

    protected String _timeZone;



    // the attributes of the base type
    StringList _baseAttrs = null;
    StringList _attributeGrRDO = null;              // for the list of RDOs mentioned in emxMultipleClassification.properties
    boolean _isSpecialAttribute;                    // to identify whether the attribute is Special Attribute or not.
    boolean _hasPermission;                         // to identify whether the user has access to the particular Attribute Group or not.
    StringList lstSpecialAttr = new StringList();   // to store the list of all Special Attributes available in the Attribute Group.
    String _parentId = "";
    // used in field name generation to ensure unique names
    protected int _fieldCounter = 0;

    // for special handling of redundant attributes
    protected HashMap _redundancyCounts = new HashMap();
    protected int _totalRedundancyCount = 0;
    public static final String SETTING_REMOVE_RANGE_BLANK = "Remove Range Blank";
    // We instantiate our own UIForm and use it to render fields
    UIForm _uif = new UIForm();

    /**
     * Creates emxMultipleClassificationAttributeFormHtmlBase Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxMultipleClassificationAttributeFormHtmlBase_mxJPO (Context context, String[] args) throws Exception {
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
     * Gets Fields in HTML for the Attribute in view mode
     *
     * @param attributeName the attributeName
     * @param attributeValue the attributeValue
     * @return String the HTML for the diplay for the Attribute name and filed
     * @throws Exception if the operation fails
     */

    public String getFieldViewHTML(String attributeName, String attributeValue)
            throws Exception {

        StringBuffer buf = new StringBuffer();
        AttributeType attribType = new AttributeType(attributeName);
        attribType.open(_context);
        String sDataType = attribType.getDataType();

        HashMap settings = new HashMap();
        settings.put("Required", "false");
        settings.put("Field Type", "attribute");
        settings.put("Registered Suite", "Framework");
        settings.put("Editable", "true");

        if ("timestamp".equalsIgnoreCase(sDataType)) {
            settings.put("format", "date");
        }
        if ("boolean".equalsIgnoreCase(sDataType)){
           StringList aChoices = new StringList();
           aChoices.add("FALSE");
           aChoices.add("TRUE");
           aChoices = UINavigatorUtil.getAttrRangeI18NStringList(attributeName, aChoices,_lang);
           attributeValue = (String)aChoices.get(attributeValue.equalsIgnoreCase("TRUE") ? 1 : 0);
        }

        HashMap field = new HashMap();
        field.put("settings", settings);
        field.put("field_value", attributeValue);
        field.put("hasAccess", "true");
//      field.put("field_display_value", attributeValue);
        field.put("label", i18nNow.getAttributeI18NString(attributeName, _lang));
        field.put("StringResourceFileId", (String)_requestMap.get("StringResourceFileId"));
        field.put("suiteDirectory", (String)_requestMap.get("SuiteDirectory"));
        field.put("suiteKey", (String)_requestMap.get("suiteKey"));
        field.put("name", attributeName);
        field.put("ClassificationAttribute","true");


        if (UOMUtil.isAssociatedWithDimension(_context, attributeName))
        {
            String strBusObjExp = "attribute[" + attributeName + "]";
            StringList busSelects = _uif.getUOMSelectables(_context, strBusObjExp);

            busSelects.add(strBusObjExp);
            field.put("expression_businessobject", strBusObjExp);
            BusinessObjectWithSelect boselect = select(_context, busSelects);
            Hashtable values = boselect.getHashtable();
            StringList fieldValues = (StringList)values.get(strBusObjExp);
            field.putAll(_uif.getUOMInputAndSystemValues(_context, values, fieldValues, strBusObjExp, _lang));
            field.put(UICache.UOM_ASSOCIATEDWITHUOM, UOMUtil.isAssociatedWithDimension(_context, attributeName) + "");
            field.put(UICache.DB_UNIT, UOMUtil.getSystemunit(_context, null, attributeName, null));
            field.put(UICache.UOM_UNIT_LIST, UOMUtil.getDimensionUnits(_context, attributeName));
        }

        buf.append("<tr>");
        buf.append(_uif.drawFormViewElement(_context, _requestMap, field,
                _timeZone, "false", true, _maxCol));
        buf.append("</tr>");

        return buf.toString();
    }


    /**
     * Gets Fields in HTML for the Attribute in edit mode
     *
     * @param attributeName the attributeName
     * @param attributeValue the attributeValue
     * @return String the HTML for the diplay for the Attribute name and filed
     * @throws Exception if the operation fails
     */
    public String getFieldEditHTML(String attributeName, String attributeValue)
            throws Exception {

        StringBuffer buf = new StringBuffer();

        AttributeType attribType = new AttributeType(attributeName);
        attribType.open(_context);
        String dataType = attribType.getDataType(_context);

		HashMap field = new HashMap();
		HashMap settings = new HashMap();
		settings.put("Required", "false");
		settings.put("Field Type", "attribute");
		if(attribType.isMultiLine()) {
			settings.put("Input Type", "textArea");
		}
		if(attribType.isMultiVal()){
			field.put("Input Type", "combobox");
			field.put("isMultiVal", "true");
		}
		String manualEdit = (String)_settingMap.get("Allow Manual Edit");

        if(manualEdit != null && !"null".equals(manualEdit)) {
            settings.put("Allow Manual Edit", manualEdit);

            field.put("field_manual_choices", new StringList("~~Add Manually~~"));

        }
        // Modified for Bug 345817
        // Added validation for real and integer also
        if (dataType.equalsIgnoreCase("string")||"real".equalsIgnoreCase(dataType) || "integer".equalsIgnoreCase(dataType) ) {
            StringList aChoices = attribType.getChoices();
            if (aChoices != null && aChoices.size() > 0) {
                settings.put("Input Type", "combobox");
                settings.put(SETTING_REMOVE_RANGE_BLANK,"true");
                field.put("field_choices", aChoices);

                StringList aDispChoices = UINavigatorUtil.getAttrRangeI18NStringList(attributeName, aChoices,_lang);
                field.put("field_display_choices", aDispChoices);
                String dispValue = UINavigatorUtil.getAttrRangeI18NString(attributeName, attributeValue,_lang);
                field.put("field_display_value", dispValue);
            }
        } else if (dataType.equalsIgnoreCase("boolean")) {
            settings.put("Input Type", "combobox");

            StringList aChoices = new StringList();
            aChoices.add("FALSE");
            aChoices.add("TRUE");
            field.put("field_choices", aChoices);

            StringList aDispChoices = UINavigatorUtil.getAttrRangeI18NStringList(attributeName, aChoices,_lang);
            field.put("field_display_choices", aDispChoices);
            String dispValue = UINavigatorUtil.getAttrRangeI18NString(attributeName, attributeValue,_lang);
            field.put("field_display_value", dispValue);
            settings.put(SETTING_REMOVE_RANGE_BLANK,"true");
        } else if ("timestamp".equalsIgnoreCase(dataType)) {
            settings.put("format", "date");
            settings.put("Show Clear Button","true");
        }

        settings.put("Registered Suite", "Framework");
        settings.put("Editable", "true");

        HashMap inputMap = new HashMap();
        inputMap.put("componentType", "form");
        inputMap.put("localDateFormat", _localDateFormat);
        inputMap.put("allowKeyableDates", _allowKeyableDates);

        field.put("settings", settings);
        field.put("field_value", attributeValue);
        field.put("hasAccess", "true");
        //field.put("expression_businessobject",attrSym);
//      field.put("field_display_value", attributeValue);
        field.put("label", i18nNow.getAttributeI18NString(attributeName, _lang));
        field.put("StringResourceFileId", (String)_requestMap.get("StringResourceFileId"));
        field.put("suiteDirectory", (String)_requestMap.get("SuiteDirectory"));
        field.put("suiteKey", (String)_requestMap.get("suiteKey"));
        field.put(_uif.CLASSIFICATIONATTRIBUTE,"true");

        if (UOMUtil.isAssociatedWithDimension(_context, attributeName))
        {
            String strBusObjExp = "attribute[" + attributeName + "]";
            StringList busSelects = _uif.getUOMSelectables(_context, strBusObjExp);

            busSelects.add(strBusObjExp);
            field.put("expression_businessobject", strBusObjExp);
            BusinessObjectWithSelect boselect = select(_context, busSelects);
            Hashtable values = boselect.getHashtable();
            StringList fieldValues = (StringList)values.get(strBusObjExp);
            // TO DO
            // Added try/catch since the code is failing during classification/reclassification.
            try{
                field.putAll(_uif.getUOMInputAndSystemValues(_context, values, fieldValues, strBusObjExp, _lang));
            }catch (Exception ex) {
            }
            field.put(UICache.UOM_ASSOCIATEDWITHUOM, UOMUtil.isAssociatedWithDimension(_context, attributeName) + "");
            field.put(UICache.DB_UNIT, UOMUtil.getSystemunit(_context, null, attributeName, null));
            field.put(UICache.UOM_UNIT_LIST, UOMUtil.getDimensionUnits(_context, attributeName));
        }

        String inputName;
        Integer redundancyCount = (Integer)_redundancyCounts.get(attributeName);

        if (redundancyCount == null) {
            redundancyCount = new Integer(1);
        } else {
            redundancyCount = new Integer(redundancyCount.intValue() + 1);
            _totalRedundancyCount++;
        }
        _redundancyCounts.put(attributeName, redundancyCount);

        inputName = _uif.CLASSIFICATION_PREFIX + redundancyCount.intValue() + "," + attributeName;
        field.put("name", inputName);

        boolean isReadOnly = false;
        String editable = (String)_settingMap.get("Editable");
        if(editable != null && !"null".equals(editable)) {
            if("false".equalsIgnoreCase(editable)) {
                isReadOnly = true;
            }
        }


        buf.append("<tr>");
        String uifOut = _uif.drawFormEditElement(_context, _requestMap, field,
                inputMap,
                _timeZone,
                true, // drawLabel
                _maxCol,
                -1, // fieldCounter
                isReadOnly);
        buf.append(uifOut);
        buf.append("</tr>");
        return buf.toString();
    }

    /**
     * Gets Classification heading
     *
     * @param interfaceName the the interface Name
     * @return the Interface Heading in HTML
     * @throws String Exception if the operation fails
     */
    public String getClassificationHeadingHTML(String interfaceName) {
        String classificationName = (String) ((StringList) _if2clsInfo
                .get(interfaceName)).get(1);
        StringBuffer buf = new StringBuffer();
        buf.append("<TR><TD colspan='");
        buf.append(_maxCol);
        buf.append("' class='heading1'>");
        buf.append(classificationName);
        buf.append("</TD></TR>");
        return buf.toString();
    }

    /**
     * Gets AttributeGroup heading
     *
     * @param attributeGroupName the Attribute Group Name
     * @return String the Attribute Group Name heading in HTML
     * @throws Exception if the operation fails
     */
    public String getAttributeGroupHeadingHTML(String attributeGroupName) {
        String name;
        try {
            name = i18nNow.getTypeI18NString(attributeGroupName, _lang);
        } catch (MatrixException me) {
            name = attributeGroupName;
        }
        StringBuffer buf = new StringBuffer();
        buf.append("<TR><TD colspan='");
        buf.append(_maxCol);
        buf.append("' class='heading2'>");
        buf.append(name);
        buf.append("</TD></TR>");
        return buf.toString();
    }

    /**
     * Checks if the Attributs in AG has access or not
     *
     * @param attributeGroupName the Attribute Group Name
     * @return boolean true/false for each Attribute in AG based on Acess defined for the attribute
     * @throws Exception if the operation fails
     */
    public boolean hasAccessToAttributeGroup(String attributeGroupName) throws Exception {
        StringList attributes = (StringList) _ag2attrs.get(attributeGroupName);

        attributes = ClassificationUtil.sortAttributes(_context,attributes,_lang);
        _hasPermission = false;         //by default Attribute Group will not have access
        lstSpecialAttr.clear();         // for every new attribute group, clear the list
        Iterator iter = attributes.iterator();
        while (iter.hasNext()) {
            String attributeName  = (String) iter.next();
            String attributeValue = (String) _attr2val.get(attributeName);
            attributeValue=attributeValue==null?" ":attributeValue;
            _isSpecialAttribute = false;    //by default, no attribute will be considered as a Special Attribute
            // If the name of attribute matches with the name defined in the Properties file, then it will be considered as a Special Attribute
            for (int i=0 ; i<_attributeGrRDO.size() ; i++)
            {
                if (_attributeGrRDO.get(i).toString().equals(attributeName))
                {
                    _isSpecialAttribute = true;
                    lstSpecialAttr.addElement(attributeName);
               }
            }
            if(!attributeValue.equals("#DENIED!") && _isSpecialAttribute)
            {
                _hasPermission = true;
            }
        }
        if(_hasPermission || lstSpecialAttr.isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets AttributeGroup Attributes HTML
     *
     * @param mode edit or view
     * @param attributeGroupName the Attribute Group Name
     * @return Attributes HTML based on the mode
     * @throws Exception if the operation fails
     */
    public String getAttributeGroupAttributesHTML(String mode,
            String attributeGroupName) throws Exception {

        boolean hasPermission = hasAccessToAttributeGroup(attributeGroupName);
        StringBuffer buf = new StringBuffer();
        StringList attributes = (StringList) _ag2attrs.get(attributeGroupName);
        attributes = ClassificationUtil.sortAttributes(_context,attributes,_lang);
        Iterator iter1 = attributes.iterator();
        while (iter1.hasNext()) {
            String attributeName1  = (String) iter1.next();
            String attributeValue1 = (String) _attr2val.get(attributeName1);
            attributeValue1=attributeValue1==null?" ":attributeValue1;
            _isSpecialAttribute = false;    //by default, no attribute will be considered as a Special Attribute
            for (int i=0 ; i<_attributeGrRDO.size() ; i++)
            {
                if (_attributeGrRDO.get(i).toString().equals(attributeName1))
                {
                    _isSpecialAttribute = true;
               }
            }
            if(hasPermission)
            {
            // If the value is null or #DENIED or it is not a special attrbute, then skip that attrbute.
                if (!attributeValue1.equals(null) && !_isSpecialAttribute && !attributeValue1.equals("#DENIED!"))
                {
                    //Modified equals to equalsIgnoreCase for IR-021946V6R2011
                    if ("edit".equalsIgnoreCase(mode)) {
                        buf.append(getFieldEditHTML(attributeName1, attributeValue1));
                    } else {
                        buf.append(getFieldViewHTML(attributeName1, attributeValue1));
                        }
                }
            }

        }
        if (hasPermission)
        {
            return buf.toString();
        }
        else
        {
            return "#DENIED!";
        }
    }


    /**
     * Gets AttributeGroup HTML
     *
     * @param mode edit or view
     * @param attributeGroupName the Attribute Group Name
     * @return Attributes HTML based on the mode
     * @throws Exception if the operation fails
     */
    public String getAttributeGroupHTML(String mode, String attributeGroupName)
            throws Exception {
        String access = getAttributeGroupAttributesHTML(mode, attributeGroupName);
        // if user has access, then it will return the HTML for entire attribute group and the attributes under it. Otherwise it will return blank string.
        if (!access.equals("#DENIED!"))
        {
        StringBuffer buf = new StringBuffer(
                getAttributeGroupHeadingHTML(attributeGroupName));
            buf.append(access);
        return buf.toString();
        }
        else
        {
            return "";
        }
    }

    /**
     * Gets Classification HTML
     *
     * @param mode edit or view
     * @param attributeGroupName the Attribute Group Name
     * @return Attributes HTML based on the mode
     * @throws Exception if the operation fails
     */
    public String getClassificationHTML(String mode,
            String ifName) throws Exception {
        StringBuffer buf = new StringBuffer();
        StringList attributeGroups = (StringList) _clsif2ags.get(ifName);
        attributeGroups = ClassificationUtil.sortAttributeGroups(_context,attributeGroups,_lang);
        Iterator agIter = attributeGroups.iterator();
        while (agIter.hasNext()) {
            String agName = (String) agIter.next();

            buf.append(getAttributeGroupHTML(mode, agName));
        }

        if (buf.length() == 0) {
            return "";
        } else {
            return getClassificationHeadingHTML(ifName)
                   + buf.toString();
        }
    }
    /**
    * Gets the Final Java Script
    * @return the Java Scritps
    * @exclude
    */
    protected String getFinalJS(Context context,String args[])
    throws Exception {
        if (_totalRedundancyCount == 0) {
            return "";
        }
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap) programMap.get("requestMap");
        _context=context;
        _requestMap=requestMap;
        _lang=(String)requestMap.get("languageStr");
        StringBuffer buf = new StringBuffer();
    	String _showRedundantMsg = EnoviaResourceBundle.getProperty(context,"emxMultipleClassification.Attribute.Duplicate.Message.ShowRedundantMsg");
        buf.append("\n<script language=\"JavaScript\" src='../documentcentral/emxMultipleClassificationUIFormValidation.js'></script>\n");
        buf.append("<script language=\"JavaScript\">\n");
        if(_showRedundantMsg != null && !"null".equals(_showRedundantMsg)) {
            if(_showRedundantMsg.equalsIgnoreCase("true")) {
                buf.append("document.emxRedundantAttrsHintStr = \"");
                buf.append( EnoviaResourceBundle.getProperty(_context,"emxLibraryCentralStringResource",new Locale(_lang)
                                                        , "emxMultipleClassification.Attribute.Duplicate.Message"));
                buf.append("\";\n");
            }
        }
       if (_parentId != null && !"null".equals(_parentId) && !"".equals(_parentId))
       {
           DomainObject dObject = new DomainObject(_parentId);
           String parentType = dObject.getInfo(_context,DomainConstants.SELECT_TYPE);
           if(!"Part".equalsIgnoreCase(parentType))
           {
               buf.append("modifyRedundantFields();\n");
           }
       }
        buf.append("</script>\n");

        return buf.toString();
    }

    /**
     * Gets the HTML for the Interfaces
     *
     * @param classificationInterfaces the list of Intercases for which HTML has to be formed 
     * @param mode edit or view
     * @return Attributes HTML based on the mode
     * @throws Exception if the operation fails
     */
    protected String getAllHTML(Context context,String args[],StringList classificationInterfaces, String mode)
            throws Exception {
        StringBuffer bodyBuf = new StringBuffer();
        if(classificationInterfaces.size() != 0)
        {
            classificationInterfaces = ClassificationUtil.sortClassifications(_context,classificationInterfaces);
            Iterator clsIfIter = classificationInterfaces.iterator();
            while (clsIfIter.hasNext()) {
                String clsIf = (String) clsIfIter.next();
                bodyBuf.append(getClassificationHTML(mode,
                        clsIf));
            }
        }


        return bodyBuf.toString()+getFinalJS(context,args);

    }

    /**
     * This method gets the Classification Attribute Groups for the form fields for a JPO2HTML Output Field
     *
     * @param context the eMatrix code>Context</code> object
     * @param args holds the following arguments
     *      0 - requestMap contains mode and objectIds
     *      1 - paramMap maximum number of columns and the fieldMap with fild details.
     * @return a StringList of Interfaces for the form fields.
     * @throws Exception if the operation fails
     */
    public StringList getClassificationAttributeGroups(Context context, String args[])
            throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap = (HashMap) programMap.get("paramMap");

        String mode = (String) requestMap.get("mode");
        String objectId = (String) paramMap.get("objectId");

        Integer maxColInt = (Integer) programMap.get("maxCols");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");

        _context = context;
        _requestMap = requestMap;
        _maxCol = maxColInt.intValue() * 2;  // label+value
        _lang = (String)requestMap.get("languageStr");
        _localDateFormat = ((java.text.SimpleDateFormat)java.text.DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(),
                            (Locale)_requestMap.get("localeObj"))).toLocalizedPattern();
        _timeZone = (String)requestMap.get("timeZone");
        if(fieldMap != null) {
            _settingMap = (HashMap) fieldMap.get("settings");
        }

        try {
            _allowKeyableDates = EnoviaResourceBundle.getProperty(context,"emxFramework.AllowKeyableDates");
        } catch(Exception e) {
            _allowKeyableDates = "false";
        }


        _fieldCounter = 0;

        setId(objectId);
        _parentId = objectId;

        String classificationMode   = (String) requestMap.get("classificationMode");
        String selectedClassIds     = (String) requestMap.get("selectedClassIds");
        StringList classificationInterfaces =
            emxLibraryCentralUtil_mxJPO.getClassificationInterfaces(_context, objectId);
         //retrieving the RDO values available in emxMultipleClassification.properties file.
         _attributeGrRDO = FrameworkUtil.split(EnoviaResourceBundle.getProperty(context,"emxMultipleClassification.AttributeGroups.OrganizationAttributes"),",");
        if(classificationInterfaces.size() != 0)
            {
            // Now, for each classification interface get the corresponding
            // classification object. Fetch the id, and the name.
            StringBuffer buf        = new StringBuffer();
            StringList accessible   =  null;
            if ( classificationMode != null && "editAttributes".equals(classificationMode)
                    && selectedClassIds != null && !"".equals(selectedClassIds) )
                { // called from classification page
                buf.append("temp query bus $1 $2 $3 where \"$4 and $5\" select $6 $7 name dump $8 recordsep $9");
                accessible = emxLibraryCentralUtil_mxJPO.
                commaPipeQueryToMapSkipTNR(_context, buf.toString(), false, _if2clsInfo, 
                                            "Classification",
                                            "*",
                                            "*",
                                            "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"] matchlist '"+FrameworkUtil.join(classificationInterfaces, ",")+"' ','",
                                            " id matchlist "+selectedClassIds+" ','' ','",
                                            "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"]",
                                            "id",
                                            ",",
                                            "|"
                                           );             
                
                }else {
                        // The query does not run as root, and will automatically filter out
                        // class objects that are not read accessible to the user
                        buf = new StringBuffer();
                        buf.append("temp query bus $1 $2 $3 where \"$4\" select \"$5\" $6 name dump $7 recordsep $8");
                        accessible = emxLibraryCentralUtil_mxJPO.
                                                        commaPipeQueryToMapSkipTNR(_context, buf.toString(), false, _if2clsInfo,
                                                                                    "Classification",
                                                                                    "*",
                                                                                    "*",
                                                                                    "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"] matchlist '"+FrameworkUtil.join(classificationInterfaces, ",")+"' ','",
                                                                                    "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"]",
                                                                                    "id",
                                                                                    ",",
                                                                                    "|"
                                                                                   );
                }
            // Next, for each classification, get its corresponding attribute
            // groups. Also, build up a list of the complete set of attribute
            // groups for all this item's classifications.
            StringList allAttributeGroups = new StringList();
            Iterator i1 = accessible.iterator();
            while (i1.hasNext()) {
                String ifName = (String) i1.next();
                StringList clsData = (StringList) _if2clsInfo.get(ifName);
                String clsId = (String) clsData.get(0);
                Classification cls = new Classification(clsId);

                StringList clsAgList = cls.getAttributeGroups(_context, true);
                _clsif2ags.put(ifName, clsAgList);
                // The full list. uniqified.
                allAttributeGroups.removeAll(clsAgList);
                allAttributeGroups.addAll(clsAgList);
            }

            // delist any interfaces corresponding to inaccessible classes
            classificationInterfaces.retainAll(accessible);

            // Almost there. Now get the individual attributes for each attribute
            // group. The AttributeGroup JPO could do this, but really it's more
            // convenient to do it here.
            StringBuffer getAgAttrsCmd  = new StringBuffer();
            getAgAttrsCmd.append("print interface \"$1\" select name attribute dump $2 recordsep $3");
            for(int i = 0; i < allAttributeGroups.size(); i++) {            
                emxLibraryCentralUtil_mxJPO.commaPipeQueryToMap(_context, 
                                                                getAgAttrsCmd.toString(), 
                                                                true, 
                                                                _ag2attrs,
                                                                (String)allAttributeGroups.get(i), 
                                                                ",", 
                                                                "|"
                                                                );
            }

            // And for every attribute of the object, [even non-classification ones],
            // let's get the current value. NOTE: if no value comes back for an
            // attribute, it means it's hidden. We will subsequently ignore those
            // when attribute groups. The Classification JPO's do not offer this
            // filtering capability.
            _attr2val = getAttributeMap(_context);

            // This block causes attributes which occur in the base type of the
            // object to be treated as redundant.  See Bug 308627
            String getBaseAttrsCmd  = "print bus $1 select $2 dump";
            String baseAttrsData    = MqlUtil.mqlCommand(_context, getBaseAttrsCmd, true, objectId, "type.attribute").trim();
            _baseAttrs              = FrameworkUtil.split(baseAttrsData,",");
            Iterator baseAttrIter   = _baseAttrs.iterator();
            while (baseAttrIter.hasNext()) {
                String baseAttrName = (String) baseAttrIter.next();
                _redundancyCounts.put(baseAttrName, new Integer(1));
            }
        }
        /*String attributesHTML = getAllHTML(classificationInterfaces, mode);
        if (attributesHTML.length() == 0 && classificationMode != null && "editAttributes".equals(classificationMode)) {
            attributesHTML = "<tr><td class=\"error\" align=\"center\">No Attributes Found</td></tr>";
        }*/
        return classificationInterfaces;

    }//eom

    /**
     * This method gets Classification Attributes for the form fields for a JPO2HTML Output Field
     *
     * @param context the eMatrix code>Context</code> object
     * @param args holds the following arguments
     *      0 - requestMap contains mode and objectIds
     *      1 - paramMap maximum number of columns and the fieldMap with fild details.
     * @return a StringList of Attributes
     * @throws Exception if the operation fails
     */
    public StringList getClassificationAttributes(Context context, String args[])
    throws Exception {
        StringList attrList = new StringList();
        StringList classificationInterfaces = getClassificationAttributeGroups(context ,args);
        Iterator clsIfIter = classificationInterfaces.iterator();
        while (clsIfIter.hasNext()) {
            String clsIf = (String) clsIfIter.next();
            StringList attributeGroups = (StringList) _clsif2ags.get(clsIf);
            
            Iterator agIter = attributeGroups.iterator();
            while (agIter.hasNext()) {
                String agName = (String) agIter.next();
                StringList attributes = (StringList) _ag2attrs.get(agName);
                attrList.addAll(attributes);
            }
        }
        return attrList;
    }

   /**
     * This method to gets HTML fields based on the mode view/edit
     *
     * @param context the eMatrix code>Context</code> object
     * @param args holds the following arguments
     *      0 - requestMap contains mode and objectIds
     *      1 - paramMap maximum number of columns and the fieldMap with fild details.
     * @return a StringList of Attributes
     * @throws Exception if the operation fails
     */
    public String getHTMLFields(Context context, String args[])
    throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String classificationMode   = (String) requestMap.get("classificationMode");
        String mode   = (String) requestMap.get("mode");
        _lang=(String)requestMap.get("languageStr");
        String attributesHTML = getAllHTML(context,args,getClassificationAttributeGroups(context ,args), mode);
        if (attributesHTML.length() == 0 && classificationMode != null && "editAttributes".equals(classificationMode)) {
            attributesHTML = "<tr><td class=\"error\" align=\"center\">";
            	attributesHTML+=EnoviaResourceBundle.getProperty(_context, "emxLibraryCentralStringResource",new Locale(_lang),"emxLibraryCentral.Message.NoAttributesFound");
            attributesHTML=attributesHTML+"</td></tr>";
        }
        return attributesHTML;
    }

    // This method is necessary because some forms, e.g. emxForm.jsp, submit
    // requestMaps wherein each value is an array of strings, out of which
    // we will always want the first element.  Most other forms submit a
    // string value for each param value.  This method hides that differece.
    // Finally, in some instances, non-strings are passed in (localeObj)
    // and we do not care about these, and prefer to deal only with Strings.
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

    /**
     * This method updates all the fields in web form
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      0 - paramMap contains the objectID
     *      1 - requestMap contains localeObj and timeZone
     * @throws Exception if the operation fails
     */
    public void updateFields(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String objectId = (String) paramMap.get("objectId");
        Locale locale   = (Locale)requestMap.get("localeObj");
        String timeZone = (String)requestMap.get("timeZone");
        double iClientTimeOffset = 0.0;
        if(timeZone != null)
        {
            iClientTimeOffset = (new Double(timeZone)).doubleValue();
        }

        setId(objectId);

        // following code is destructive of requestMap;
        // just work on a copy of it, so as not to mess
        // up other uses of requestMap furhter down the chain
        HashMap requestMap2 = (HashMap)requestMap.clone();

        // date values must be converted to emx grokable format
        // UI widget submits #1,foo=display-val and #1,foo_msvalue=millisecval
        // for dates fields
        // one of the other of these needs to be converted to input format.
        // we'll go with the display val
        String msSuffix = "_msvalue";
        Iterator it1 = requestMap.keySet().iterator();
        while (it1.hasNext()) {
            String key = (String) it1.next();
            if (key.endsWith(msSuffix)) {
                String baseName = key.substring(0, key.length() - msSuffix.length());
                String dispValue = extractVal(requestMap.get(baseName));
                requestMap2.remove(key);
                String dateVal = "";

                if(dispValue != null && !dispValue.trim().equals("") && timeZone != null)
                {
                    dateVal = eMatrixDateFormat.getFormattedInputDate(context,
                                                        dispValue, iClientTimeOffset, locale);
                }
                else
                {
                    dateVal = dispValue;
                }
                requestMap2.put(baseName, dateVal);
            }
        }

        requestMap=null;  // no further need for this; avoid erroneous references below

		Iterator it2 = requestMap2.keySet().iterator();
		DomainObject partObj=new DomainObject(objectId);
		HashMap newAttrValuesMap = new HashMap();
		while (it2.hasNext()) {
			// Get key
			String key = (String) it2.next();
			String value = extractVal(requestMap2.get(key));
			if (key.startsWith("#") && key.indexOf(",units_")==-1 && key.indexOf("_tmp")==-1
					&& ! key.endsWith("fieldValue") && ! key.endsWith("AmPm") && ! key.endsWith("fieldValueOID")) {
				//this is a dynamic attribute drawn on form;
				//add its name and value to the new values map
				String attributeName= key.substring(key.indexOf(",")+1);
				String attributeUnitFieldName = key.substring(0,key.indexOf(",")+1)+"units_"+attributeName;
				String strUnitOfMeasure = extractVal(requestMap2.get(attributeUnitFieldName));
				if(strUnitOfMeasure!=null && !"null".equalsIgnoreCase(strUnitOfMeasure) && !"".equals(strUnitOfMeasure))
				{
					if("".equals(value.trim()))
					{
						value="0";
					}
					value+= " "+strUnitOfMeasure;
					//pseudo code check attribute name contains mva
					//if yes, then get the name of the attribute and its index
					//check whether the same key exists in the map
					//if yes , check for value is null, then just add
					//if values is String, create a Map,add an index with value
					//if value is Map, then put index with value
					//finally put the attribute with the Map in original Map
					createMultiValuesMap(attributeName,value,newAttrValuesMap);
				}
				else{
					createMultiValuesMap(attributeName,value,newAttrValuesMap);
				}
			}
		}
		
		AttributeList attributeList = new AttributeList();
		Set keysSet = newAttrValuesMap.keySet();
		Iterator itr = keysSet.iterator();
		while(itr.hasNext()){
			String key  = (String)itr.next();
			Object value = newAttrValuesMap.get(key);
			AttributeType newAttrrType = new AttributeType(key);
			if(value instanceof String){
				Attribute attribute = new Attribute(newAttrrType, (String)value);
				attributeList.add(attribute);
			}else{
				HashMap newMap = new HashMap();
				Set mvMapKeySet = ((Map)value).keySet();
				Iterator itr1 = mvMapKeySet.iterator();
				int newIndex = 1;
				while(itr1.hasNext()){
					newMap.put(newIndex,((Map)value).get(itr1.next()));
					newIndex++;
				}
				Attribute newAttr = new Attribute(newAttrrType, newMap);
				attributeList.add(newAttr);
			}
		}
		
		setAttributeValues(context, attributeList);
	}
			
	private void createMultiValuesMap(String attributeName, String value, HashMap newAttrValuesMap) {
		//mva attribute
		if(attributeName.indexOf("mva")!=-1){
			String originalAttributeName=attributeName.substring(0,attributeName.indexOf("_mva_"));
			String index=attributeName.substring(attributeName.indexOf("_mva_")+5);
			Object existingValue=newAttrValuesMap.get(originalAttributeName);
			if(existingValue==null || existingValue instanceof String){
				//inside the null
				HashMap map=new HashMap();
				if(existingValue instanceof String)
					map.put(1,existingValue);
				
				map.put(Integer.parseInt(index), value);
				newAttrValuesMap.put(originalAttributeName, map);
			}
			//if the newAttrValuesMap already has a Map, meaning mva attribute value and attribute is already present in the Map 
			else{
                // IR-505169-3DEXPERIENCER2017x start.
				if(existingValue instanceof String)
				{
					HashMap map=new HashMap();
					map.put(Integer.parseInt(index), value);
				}
				else{
                    ((Map)existingValue).put(Integer.parseInt(index), value);   
                }
                // IR end.
			}

		}
		//normal attribute
		else{
			Object existingValue=newAttrValuesMap.get(attributeName);
			if(existingValue==null)
				newAttrValuesMap.put(attributeName, value);
			else{
                // IR-505169-3DEXPERIENCER2017x start.
                if(existingValue instanceof String)
				{
					HashMap map=new HashMap();
					map.put(1,value);
				}
				else {
                    ((Map)existingValue).put(1, value); 
                }
                // IR end.			
			}
	
		}
		
			
	}
	//eom

    /**
     * This method returns Attributes HTML for Classification/reClassification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return String - the attributes HTML
     * @throws Exception if the operation fails
     */
    public String getHTMLFieldsForClassify(Context context, String args[])
    throws Exception {

        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap  = (HashMap) programMap.get("requestMap");
        HashMap fieldMap    = (HashMap) programMap.get("fieldMap");
        HashMap fieldValues = (HashMap) programMap.get("fieldValues");

        if(fieldMap != null) {
            _settingMap = (HashMap) fieldMap.get("settings");
        }

        String mode         = (String) requestMap.get("mode");
        String objectId     = (String) requestMap.get("objectId");
        Integer maxColInt   = new Integer(1);
        if (programMap.get("maxCols") != null) {
            maxColInt       = (Integer) programMap.get("maxCols");
        }

        _context            = context;
        _requestMap         = requestMap;
        _maxCol             = maxColInt.intValue() * 2;  // label+value
        _lang               = (String)requestMap.get("languageStr");
        _localDateFormat    = ((java.text.SimpleDateFormat)java.text.DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(),
                            (Locale)_requestMap.get("localeObj"))).toLocalizedPattern();
        _timeZone           = (String)requestMap.get("timeZone");
        _fieldCounter       = 0;

        try {
            _allowKeyableDates  = EnoviaResourceBundle.getProperty(context,"emxFramework.AllowKeyableDates");
        } catch(Exception e) {
            _allowKeyableDates  = "false";
        }
        String selectedClassIds = (String) fieldValues.get("ClassOID");

        if(selectedClassIds != null && selectedClassIds.indexOf("|") > 0){
            selectedClassIds = FrameworkUtil.findAndReplace(selectedClassIds,"|",",");
        }

        setId(objectId);
        _parentId           = objectId;

        StringList classificationInterfaces = new StringList();
         _attributeGrRDO = FrameworkUtil.split(EnoviaResourceBundle.getProperty(context,"emxMultipleClassification.AttributeGroups.OrganizationAttributes"),",");
        if(selectedClassIds.length() > 0 )
        {
            String cmd                  = "temp query bus $1 $2 $3 WHERE \"$4\" select \"$5\" $6 name dump $7 recordsep $8";
            classificationInterfaces    = emxLibraryCentralUtil_mxJPO.commaPipeQueryToMapSkipTNR(_context, cmd, false, _if2clsInfo,
                                                                                                    "Classification",
                                                                                                    "*",
                                                                                                    "*",
                                                                                                    "id matchlist '"+selectedClassIds+"' ',' ",
                                                                                                    "attribute["+ClassificationConstants.ATTRIBUTE_MXSYS_INTERFACE+"]",
                                                                                                    "id",
                                                                                                    ",",
                                                                                                    "|"
                                                                                                );

            StringList allAttributeGroups   = new StringList();
            Iterator itr                    = classificationInterfaces.iterator();
            while (itr.hasNext()) {
                String ifName       = (String) itr.next();
                StringList clsData  = (StringList) _if2clsInfo.get(ifName);
                String clsId        = (String) clsData.get(0);
                Classification cls  = new Classification(clsId);

                StringList clsAgList    = cls.getAttributeGroups(_context, true);
                _clsif2ags.put(ifName, clsAgList);
                allAttributeGroups.removeAll(clsAgList);
                allAttributeGroups.addAll(clsAgList);
            }

            String getAgAttrsCmd        = "print interface \"$1\" select name attribute dump $2 recordsep $3";
            for(int i = 0; i < allAttributeGroups.size(); i++) {
                emxLibraryCentralUtil_mxJPO.commaPipeQueryToMap(_context, 
                                                                getAgAttrsCmd, 
                                                                true, 
                                                                _ag2attrs,
                                                                (String)allAttributeGroups.get(i),
                                                                ",",
                                                                "|"
                                                                );
            }
            _attr2val                   = new HashMap();
            Iterator iterator           = _ag2attrs.keySet().iterator();
            while (iterator.hasNext()) {
                StringList slAttributes = (StringList)(_ag2attrs.get(iterator.next()));
                for (int i = 0; i < slAttributes.size() ; i++) {
                    AttributeType attribute = new AttributeType((String)slAttributes.get(i));
                    _attr2val.put((String)slAttributes.get(i),attribute.getDefaultValue(context) );
                }
            }
            _redundancyCounts = new HashMap();
        }
        // And finally, at long last, render each classification's data in its entirety
        return getAllHTML(context,args,classificationInterfaces, mode);

    }

    /**
     * This method returns initial Attributes HTML for
     * Classification/reClassification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @return String - the attributes HTML
     * @throws Exception if the operation fails
     */
    public String getAttributesHTML(Context context, String args[])
    throws Exception {
        _totalRedundancyCount =1;
        //changing the method signature, for Move Properties to DB. Since language & context is not
        //available in the method
         return getFinalJS(context,args);
    }

    /**
     * This method reloads attributes on change of class
     * during classificaion/reclassification
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     * @returns Map containg Attributes HTML
     * @throws Exception if the operation fails
     */
    public Map reloadAttributesHTML(Context context, String args[])
    throws Exception
    {
        StringBuffer selectedValues = new StringBuffer();
        selectedValues.append("<table class=\"list\">");
        selectedValues.append(getHTMLFieldsForClassify(context,args));
        selectedValues.append("</table>");
        Map map = new HashMap();
        map.put("SelectedValues", selectedValues.toString());
        map.put("SelectedDisplayValues", selectedValues.toString());
        return map;
    }


}

