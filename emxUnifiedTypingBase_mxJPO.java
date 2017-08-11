/*
**   ${CLASSNAME}.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**
*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.util.MatrixException;

/**
 * The <code>emxUnifiedTypingBase</code> JPO contains the utility methods for Customer Attributes used in emxForm.jsp and emxCreate.jsp
 *
 * @version R214 (V6R2013x)
 * @author ZGX
 */

public class emxUnifiedTypingBase_mxJPO {
	   
    static final String EMPTY_STRING = "";
    
    private static final String FIELD_CHOICES = "field_choices";
    private static final String FIELD_DISPLAY_CHOICES = "field_display_choices";
    private static final String FIELD_TYPE_ATTRIBUTE = "attribute";
        
    private static final String SETTING_EDITABLE_FIELD = "Editable";
    private static final String SETTING_REQUIRED_FIELD = "Required";
    protected static final String SETTING_INPUT_TYPE = "Input Type";
    private static final String SETTING_REGISTERED_SUITE = "Registered Suite";
    private static final String SETTING_FIELD_TYPE = "Field Type";
    private static final String SETTING_ADMIN_TYPE = "Admin Type";
    private static final String SETTING_FORMAT = "format"; 
    
    private static final String INPUT_TYPE_TEXTBOX = "textbox";
    private static final String INPUT_TYPE_TEXTAREA = "textarea";
    private static final String INPUT_TYPE_COMBOBOX = "combobox";
    private static final String INPUT_TYPE_CHECKBOX = "checkbox";
    
    private static final String FORMAT_DATE = "date";
    private static final String FORMAT_NUMERIC = "numeric";
    private static final String FORMAT_INTEGER = "integer";
    private static final String FORMAT_BOOLEAN = "boolean";
    private static final String FORMAT_REAL = "real";
    private static final String FORMAT_TIMESTAMP = "timestamp";
    private static final String FORMAT_STRING = "string";
    private static final String FORMAT_CHOICES = "choices";
    
    private static final String EXPRESSION_BUSINESSOBJECT = "expression_businessobject";
    private static final String BOOLEAN_TRUE = "true";
    private static final String BOOLEAN_FALSE = "false";
    private static final String DYNAMICATTRIBUTE = "DynamicAttribute";
    private static final String DEFAULT_REGISTERED_SUITE = "Framework";
	   
	   
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */

    public emxUnifiedTypingBase_mxJPO (Context context, String[] args) throws Exception
    {
        
    }
    
    /**
     * Method contains the logic to identify customer attributes and return the form fields corresponding to them. i.e., attributes having 
     * property IPML.CustoUserAccess with one of the possible values = "ReadOnly", "ReadWrite" or "None"
     * 
     * RFL reference: Function_030361
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args contains the packed values of objAttributeMap, slFormAttrLst, formMode and sLanguage
     * @return MapList of fields for customer attributes
     * @throws Exception
     */
    public MapList getCustomerAttributes(Context context,String[] args) throws Exception {
    	MapList customerAttFields = new MapList();
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap attributesMap = (HashMap) programMap.get("objAttributeMap");
		StringList formAttrLst = (StringList) programMap.get("slFormAttrLst");
		String formMode = (String) programMap.get("formMode");
		String sLanguage = (String) programMap.get("languageStr");
		    
	    try
    	{
	    	String sMultiLine = "";
    		String strAttName = "";
    		String attrType = "";
    		String strBusObjExp = "";
    		StringList choicesList = null;
    		String symbolicAttrName = "";
    		
    		//Maps to store field and setting definitions
    		HashMap fieldMap = null;

    		Set attributeDetailsSet = attributesMap.keySet();
    		Iterator keySetitr = attributeDetailsSet.iterator();
    		
    		while (keySetitr.hasNext())
    		{
    			fieldMap = new HashMap();
    			sMultiLine = "false";

    			//To get each attribute name, type & and its businesss expression
    			strAttName = (String) keySetitr.next();

    			HashMap attrMap = (HashMap)attributesMap.get(strAttName);
    			attrType = getValue(attrMap, UICache.TYPE);
    			choicesList = (StringList) attrMap.get(FORMAT_CHOICES);
    			sMultiLine = getValue(attrMap, "multiline");
    			strBusObjExp = "attribute[" + strAttName + "].value";

    			// To get the Symbolic Name
    			symbolicAttrName = FrameworkUtil.getAliasForAdmin(context, "attribute", strAttName, true);

    			// ignore non customer attributes and already added attributes as this method is specific for customer attributes    			
    			boolean isVisibleCustomerAttr = isVisibleCustomerAttribute(context, strAttName);
    			boolean isAlreadyDefined = false;
    			if(formAttrLst != null && formAttrLst.contains(strAttName)){
    				isAlreadyDefined = true;
    			}
    			
    			// If the field is alread defined, do not create/add the field. Avoid duplicates in the form
    			if (!isVisibleCustomerAttr || isAlreadyDefined)
    			{
    				continue;
    			}

    			setFieldSettings(context, fieldMap, attrType, choicesList, sMultiLine, formMode, sLanguage, symbolicAttrName);

    			fieldMap.put(EXPRESSION_BUSINESSOBJECT, strBusObjExp);

    			fieldMap.put(UICache.UOM_ASSOCIATEDWITHUOM, UOMUtil.isAssociatedWithDimension(context, strAttName) + "");
    			fieldMap.put(UICache.DB_UNIT, UOMUtil.getSystemunit(context, null, strAttName, null));
    			fieldMap.put(UICache.UOM_UNIT_LIST, UOMUtil.getDimensionUnits(context, strAttName));
    			fieldMap.put(DYNAMICATTRIBUTE, "false");

    			String slabel = EnoviaResourceBundle.getAttributeI18NString(context, strAttName, sLanguage);

    			fieldMap.put(UICache.LABEL, slabel);
    			fieldMap.put(UICache.NAME, strAttName);
    			
    			
    			
    			boolean isEditable = isEditableCustomerAttribute(context, strAttName); 
    			if (isEditable) {
    				UIComponent.modifySetting(fieldMap, SETTING_EDITABLE_FIELD, BOOLEAN_TRUE);
    				if(isMandatory(context, strAttName)){
    					UIComponent.modifySetting(fieldMap, SETTING_REQUIRED_FIELD, BOOLEAN_TRUE);
    				}
    			} else {
    				UIComponent.modifySetting(fieldMap, SETTING_EDITABLE_FIELD, BOOLEAN_FALSE);
    			}
    			
    			customerAttFields.add(fieldMap);
    		}
	    		
    	} catch(Exception e) {
    		throw  (new FrameworkException("Error while building customer dynamic attribute fields - " + e.toString()));
    	}
    	return customerAttFields;
		  
  }
	  
	    /**
	     * Private method to set the settings for the dynamic fields added in case of dynamic attributes and customer attributes cases 
	     * 
	     * EXTRACTED METHOD FROM UIFormCommon.java. Any changes to this method should be made to UIFormCommon.setFieldSettings method as well.
	     * 
	     * @throws MatrixException
	     * @see <link>UIFormCommon.getDynamicAttributes</link>
	     */
	    private void setFieldSettings(Context context, Map fieldMap, String attrType, StringList choicesList, 
	    		String sMultiLine, String formMode, String sLanguage, String symbolicAttrName) throws MatrixException 
	    {
	    	
	    	String strFieldFormat = "";
	    	String strFieldIPType = INPUT_TYPE_TEXTBOX;

	        if(FORMAT_STRING.equalsIgnoreCase(attrType))
	        {
	            if(choicesList != null && choicesList.size() > 0) {
	                strFieldIPType = INPUT_TYPE_COMBOBOX;
	            } else if ("true".equalsIgnoreCase(sMultiLine)) {
	                strFieldIPType = INPUT_TYPE_TEXTAREA;
	            } else {
	                strFieldIPType = INPUT_TYPE_TEXTBOX;
	            }
	        }
	        else if(FORMAT_BOOLEAN.equalsIgnoreCase(attrType))
	        {
	            //Add the range values for the boolean attribute
	            if(choicesList == null || choicesList.size() == 0) {
	            	
	                //String Lists for Boolean attribute display in dynamic webform
	                //Range values for Boolean attribute display in dynamic webform
	                StringList boolAttValues = new StringList(2);
	                boolAttValues.addElement("FALSE");
	                boolAttValues.addElement("TRUE");
	                
	                StringList boolAttDisplayValues = EnoviaResourceBundle.getAttrRangeI18NStringList(context, "BooleanAttribute", boolAttValues, sLanguage);
	                fieldMap.put(FIELD_CHOICES, boolAttValues);
	                fieldMap.put(FIELD_DISPLAY_CHOICES, boolAttDisplayValues);
	            }
	            //Displaying combo box for boolean attributes in create form for dynamic attributes
	            //Displaying single checkbox for Boolean type attributes in other modes of form
	            if("Create".equalsIgnoreCase(formMode)){
	                strFieldIPType = INPUT_TYPE_COMBOBOX;
	            } else {
	                strFieldIPType = INPUT_TYPE_CHECKBOX;
	            }
	        }
	        else if(FORMAT_REAL.equalsIgnoreCase(attrType))
	        {
	            if(choicesList != null && choicesList.size() > 0) {
	                strFieldIPType = INPUT_TYPE_COMBOBOX;
	            }
	            strFieldFormat = FORMAT_NUMERIC;
	        }
	        else if(FORMAT_TIMESTAMP.equalsIgnoreCase(attrType))
	        {
	            strFieldFormat = FORMAT_DATE;
	        }
	        else if(FORMAT_INTEGER.equalsIgnoreCase(attrType))
	        {
	            if(choicesList != null && choicesList.size() > 0) {
	                strFieldIPType = INPUT_TYPE_COMBOBOX;
	            }
	            strFieldFormat = FORMAT_INTEGER;
	        }

	        //To build the settings map
	        UIComponent.modifySetting(fieldMap, SETTING_INPUT_TYPE, strFieldIPType);
	        if(strFieldFormat.length()>0)
	        {
	        	UIComponent.modifySetting(fieldMap, SETTING_FORMAT, strFieldFormat);
	        }
	        UIComponent.modifySetting(fieldMap, SETTING_FIELD_TYPE, FIELD_TYPE_ATTRIBUTE);
	        UIComponent.modifySetting(fieldMap, SETTING_ADMIN_TYPE, symbolicAttrName);
			
			// To get the String Resource File
	        UIComponent.modifySetting(fieldMap, SETTING_REGISTERED_SUITE, DEFAULT_REGISTERED_SUITE);
	    }
	    
	    /**
	     * Utility method to get the value stored in the map.  If the value is
	     * null or an empty string after trimming, then return
	     * an empty string.
	     *
	     * @param map  the map containing the key
	     * @param key  the key to the value in the map
	     * @return     the value from the map
	     */
	    private static String getValue(Map map, String key) {
	        String value = (String) map.get(key);
	        return ((value == null) ? EMPTY_STRING : value.trim());
	    }
	    
	    /**
	     * Returns property value on the attribute.
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param strAttribute the attribute name
	     * @return String
	     */
	    private String getPropertyValue(Context context, String strAttribute, String propertyName) {
	        String propertyValue = EMPTY_STRING;

	        try {
	            String sCommd = "print attribute $1 select $2 dump $3";
	            String selectProp = "property["+propertyName +"]";
	            String result = MqlUtil.mqlCommand(context, sCommd,strAttribute,selectProp,"|");


	            if (result.indexOf("value") != -1) {
	                propertyValue = result.substring(result.indexOf("value")+6, result.length()).trim();
	            }
	        } catch(Exception e) {
	            System.out.println("emxUnifiedTypingBase_mxJPO: Exception in getPropertyValue " + e);
	        }

	        return propertyValue;
	    }
	    
	    private boolean isVisibleCustomerAttribute(Context context, String strAttName) {
			boolean isVisibleAttribute = false;
			String prot = getPropertyValue(context, strAttName, UIComponent.CustoUserAccessPropName);
			if (UIUtil.isNotNullAndNotEmpty(prot) && (UIComponent.RW.equals(prot) || UIComponent.RO.equals(prot))){
	      		isVisibleAttribute = true;
	      	}
			return isVisibleAttribute;
	  	}
	    
	    private boolean isEditableCustomerAttribute(Context context, String strAttName) {
	    	boolean isEditable = false;
	    	String prot = getPropertyValue(context, strAttName, UIComponent.CustoUserAccessPropName);
	    	if (UIUtil.isNotNullAndNotEmpty(prot) && UIComponent.RW.equals(prot)){
	    		isEditable = true;
	    	}
	    	return isEditable;
		}
	    
	    private boolean isMandatory(Context context, String strAttName) {
	    	boolean isMandatory = false;
	    	String mand = getPropertyValue(context, strAttName, UIComponent.MANDATORY);
	    	if (UIUtil.isNotNullAndNotEmpty(mand) && UIComponent.YES.equals(mand)){
	    		isMandatory = true;
	    	}
	    	return isMandatory;
		}
   }

