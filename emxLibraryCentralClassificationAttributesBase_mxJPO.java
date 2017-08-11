/*
 * ${CLASSNAME}.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Collection;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.classification.Classification;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class emxLibraryCentralClassificationAttributesBase_mxJPO implements LibraryCentralConstants{

    /**
     * Creates the  emxLibraryCentralClassificationAttributesBase Object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxLibraryCentralClassificationAttributesBase_mxJPO(Context context, String[] args) throws Exception {

    }

    public static final String UOM_ASSOCIATEDWITHUOM     = "AssociatedWithUOM";
    public static final String DB_UNIT                   = "DB Unit";
    public static final String UOM_UNIT_LIST             = "DB UnitList";
    public static final String UOM_INPUT_UNIT            = "Input Unit";
    public static final String PROPNAME_START_DELIMITER  = "[";
    public static final String PROPNAME_END_DELIMITER    = "]";
    public static final String RESULT_DELIMITER          = " =";
    public static final String RANGE_START_DELIMITER     = "[";
    public static final String SETTING_REMOVE_RANGE_BLANK = "Remove Range Blank";
    
    public static final int    BOM_MAX_NO_OF_ATTR        = 4;

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
        if (!context.isConnected()) {
            throw new FrameworkException("not supported on desktop client");
        }
        return 0;
    }
    
    public Map<String,MapList> getClassificationAttributesWithClassName(Context context,String[] args) throws Exception{
    	Map<String,MapList> results = new HashMap<String,MapList>();
    	
    	MapList classificationAttributes = new MapList();
    	
    	HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    	
    	String objectId = (String)paramMap.get("objectId");
    	DomainObject domObj   = new DomainObject(objectId);
    	
    	if(domObj.isKindOf(context, TYPE_CLASSIFICATION)){
	    	classificationAttributes.addAll(getClassClassificationAttributes(context, objectId));
    	}else{
    		StringList objectSelects = new StringList();
            objectSelects.add(SELECT_ID);
            objectSelects.add(SELECT_NAME);
            
            MapList classificationList = domObj.getRelatedObjects(context,
                    "Classified Item",
                    "*",
                    objectSelects,
                    null,
                    true,//boolean getTo,
                    false,//boolean getFrom,
                    (short)0,
                    null,
                    null,
                    0);
            
            int noOfClasses = classificationList.size();

            if(noOfClasses>0){
                Iterator itr = classificationList.iterator();
                while(itr.hasNext()){
                    Map classMap = (Map)itr.next();

                    MapList classificationAttributesOfClass = getClassClassificationAttributes(context, (String)classMap.get(SELECT_ID));
					results.put((String)classMap.get(SELECT_NAME), classificationAttributesOfClass);
                }

            }
    	}
    	return results;
    }
    
    /**
     * this method returns all Classification Attributes in this class or endItem
     * @param context
     * @param args  - JPO Packed HashMap containing objectId of the class 
     * @return a MapList of AttributesGroups, each map with following key-Value Pairs
     *                 1) "name" - String containing AttributeGroup name
                       2) "attributes" - a MapList of Attributes, each map with following key-Value Pairs
                                    1) "qualifiedName" - String containing qualified name of the Attribute
                                    2) "name"          - String containing name of the Attribute
                                    3) "type"          - String containing type of the Attribute
                                    4) "default"       - String containing default value of the Attribute
                                    5) "description"   - String containing description of the Attribute
                                    6) "maxlength"     - String containing maxlength of the Attribute
                                    7) "dimension"     - name of the dimension
                                    8) "range"         - MapList of range Values
     * @throws Exception
     */
    public MapList getClassificationAttributes(Context context,String[] args) throws Exception{
    	
		//below code was added for G5C's requirement.Needs to be reviewed again.
    	/*MapList classificationAttributes = new MapList();    	
    	Map<String,MapList> mapElements = getClassificationAttributesWithClassName(context, args);
		Collection<MapList> attributesGroups = mapElements.values();
		for (MapList attributeGroup: attributesGroups){
			classificationAttributes.addAll(attributeGroup);
		}
    	return classificationAttributes;*/

		MapList classificationAttributes = new MapList();
    	
    	HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    	
    	String objectId = (String)paramMap.get("objectId");
    	DomainObject domObj   = new DomainObject(objectId);
    	
    	if(domObj.isKindOf(context, TYPE_CLASSIFICATION)){
	    	classificationAttributes.addAll(getClassClassificationAttributes(context, objectId));
    	}else{
    		StringList objectSelects = new StringList();
            objectSelects.add(SELECT_ID);
            
            MapList classificationList = domObj.getRelatedObjects(context,
                    "Classified Item",
                    "*",
                    objectSelects,
                    null,
                    true,//boolean getTo,
                    false,//boolean getFrom,
                    (short)0,
                    null,
                    null,
                    0);
            
            int noOfClasses = classificationList.size();

            if(noOfClasses>0){
                Iterator itr = classificationList.iterator();
                while(itr.hasNext()){
                    Map classMap = (Map)itr.next();
					MapList classificationAttributesOfClass = getClassClassificationAttributes(context, (String)classMap.get(SELECT_ID));
                    classificationAttributes.addAll(classificationAttributesOfClass);
                }

            }
    	}
    	return classificationAttributes;
    }


    public MapList getClassClassificationAttributes(Context context,String[] args) throws Exception{

    	String classObjectId = (String)JPO.unpackArgs(args);

    	return getClassClassificationAttributes(context, classObjectId);
    }

    /**
     * method to get the classification attributes of all the attribute groups connected to the class
     * @param context the eMatrix <code>Context</code> object
     * @param objectId - objectId of the class whose attributes are to be returned
     * @returns - a MapList of Attributes Groups, each map with following key-Value Pairs
                   1) "name" - String containing AttributeGroup name
                   2) "attributes" - a MapList of Attributes, each map with following key-Value Pairs
                                    1) "qualifiedName" - String containing qualified name of the Attribute
                                    2) "name"          - String containing name of the Attribute
                                    3) "type"          - String containing type of the Attribute
                                    4) "default"       - String containing default value of the Attribute
                                    5) "description"   - String containing description of the Attribute
                                    6) "maxlength"     - String containing maxlength of the Attribute
                                    7) "dimension"     - name of the dimension
                                    8) "range"         - StringList of range Values
                                    Note: no key-Value pair exists in this map if there is no value for that key
     */
    protected static MapList getClassClassificationAttributes(Context context, String objectId)throws Exception{
        DomainObject classObj   = new DomainObject(objectId);
        StringList selectables  = new StringList();
        String attribute_mxsysInterface = "attribute["+ATTRIBUTE_MXSYS_INTERFACE+"].value";
        selectables.add(attribute_mxsysInterface);
        Map classInfo = classObj.getInfo(context, selectables);
        String mxsysInterface = (String)classInfo.get(attribute_mxsysInterface);
        if(UIUtil.isNullOrEmpty(mxsysInterface)){
            // if mxsysInterface is empty then there will be no classification attributes associated with this object
            return new MapList();
        }

        StringList slAttributeGroups = new StringList();

        //get all the AttributeGroups of this class using mxsysInterface
        String mqlQuery = "print interface $1 select $2";
        String sAllParentInterfaces = MqlUtil.mqlCommand(context, mqlQuery, mxsysInterface,"allparents.derived");
        // iterate the values and check for Classification Attribute Groups
        // and then form Attribute groups

        HashMap hmAllParentInterfaces = parseMqlOutput(context, sAllParentInterfaces);
        Set setAllParentInterfaces = hmAllParentInterfaces.keySet();
        Iterator itr = setAllParentInterfaces.iterator();
        while(itr.hasNext()){
            String parentInterfaceName   = (String)itr.next();
            HashMap tempParentInterface  = (HashMap)hmAllParentInterfaces.get(parentInterfaceName);
            if(tempParentInterface != null){
                String parentInterfaceDerived = (String)tempParentInterface.get("derived");
                if(!UIUtil.isNullOrEmpty(parentInterfaceDerived) && parentInterfaceDerived.equals("Classification Attribute Groups")){
                    slAttributeGroups.add(parentInterfaceName);
                }
            }
        }

        MapList attributeGroups = new MapList();
        selectables = new StringList();
        selectables.add("type");
        selectables.add("range");
        selectables.add("multiline");
        selectables.add("valuetype");
        selectables.add("default");
        selectables.add("description");
        selectables.add("maxlength");
        selectables.add("dimension");
        
        //for each attribute group
        for(int i=0;i< slAttributeGroups.size();i++){
            HashMap attibuteGroup = new HashMap();
            String attibuteGroupName = (String)slAttributeGroups.get(i);
            attibuteGroup.put("name", attibuteGroupName);
            MapList attributes = getAttributeGroupAttributesDetails(context, attibuteGroupName, selectables);
            attibuteGroup.put("attributes", attributes);
            attributeGroups.add(attibuteGroup);
        }
        return attributeGroups;
    }

    
    /**
     * 
     * This Method removes the classification interface i.e., mxsysInterface on a cloned end item, 
     * Note : this method should be triggered only when 'Classified Item' Relationship is not replicated on clone of an end Item
     * 
     * //IR-275988V6R2015x
     * 
     * @param context
     * @param args - array of String s
     *               args[0] - objectId of the end Item being cloned
     *               args[1] - objectId of the clone 
     * @throws Exception
     */
    public void removeClassificationInterface(Context context, String[] args)throws Exception
    {
    	String objectId = args[0];
    	String newObjId = args[1];
    	MapList attributeGroups = new MapList();
    	String newInterface = "";
    	String existingInterfaces = "";
    	String output = "";
    	
    	try
    	{	
    		//Get the Interface(s) of the Clone
    		String query = "print bus $1 select interface dump";
    		existingInterfaces = MqlUtil.mqlCommand(context, query, newObjId);
    		String[] existingInterfacesArray = existingInterfaces.split(",");
    		
    		//Get the Class/Family Id(s)... using Original End Item ID
    		String classQuery = "print bus $1 select $2 dump";
    		String classIds = MqlUtil.mqlCommand(context, classQuery, objectId, "to[Classified Item].from.id");
    		
    		StringList agList = new StringList();
    		
    		if(!classIds.isEmpty() && classIds != null && !classIds.equalsIgnoreCase(""))
    		{
    			String[] ids = classIds.split(",");

    			//Get all the AG Names which the End Item is implementing
    			Iterator attrGroupIter = null;
    			HashMap attibuteGroup = new HashMap();
    			String agName = "";
    			for(int i=0 ; i<ids.length ; i++)
    			{
    				attributeGroups = getClassClassificationAttributes(context, ids[i].trim());
    				//Iterate and take AG names
    				attrGroupIter = attributeGroups.iterator();
    				while(attrGroupIter.hasNext())
    				{
    					attibuteGroup = (HashMap)attrGroupIter.next();
    					agName = (String)attibuteGroup.get("name");
    					agList.add(agName);
    				}
    			}
    		}
    		
    		//Create a new Temporary Interface for the Clone
    		if(newObjId != null && !"".equals(newObjId) && !"null".equals(newObjId))
    		{
    			DomainObject domainObj = new DomainObject();
    			domainObj.setId(newObjId);
    			newInterface = "Clone_"+domainObj.getInfo(context, "physicalid");
    			String strMQL       =  "add interface $1 type all";
    			ContextUtil.pushContext(context);
    			output = MqlUtil.mqlCommand(context, strMQL, newInterface);
    			ContextUtil.popContext(context);
    		}
    		
    		//Add the newly created interface to the Clone
    		String execute = "modify bus $1 add interface $2";
    		String[] params = {newObjId, newInterface};
    		output = MqlUtil.mqlCommand(context, execute, params);
    		
    		//Extend the newly created interface to the retrieved AG names if any
    		if(agList.size() > 0)
    		{
    			String[] interfaces = new String[agList.size()+1];
    			interfaces[0] = newInterface;
    			ContextUtil.pushContext(context);
    			StringBuffer modInterface = new StringBuffer();
    			modInterface.append("modify interface $1 derived ");
    			Iterator agIter = agList.iterator();
    			int index = 1;
    			while(agIter.hasNext())
    			{
    				modInterface.append("$"+(index+1)+",");
    				interfaces[index] = (String)agIter.next();
    				index++;
    			}
    			output = MqlUtil.mqlCommand(context, modInterface.substring(0, modInterface.lastIndexOf(",")).toString(), interfaces);
    			ContextUtil.popContext(context);
    		}
    		
    		//Remove the Old Interface(s) of the Clone.
    		String del = "modify bus $1 remove interface $2";
    		for(int j=0 ; j<existingInterfacesArray.length ; j++)
    		{
    			output = MqlUtil.mqlCommand(context, del, newObjId, existingInterfacesArray[j]);
    		}
    	}
    	catch (Exception e)
    	{
    		throw new Exception(e.getMessage());
    	}
    }
    
    
    /**
     * Method to get the Details of all attributes in a Attribute Group
     * @param context the eMatrix <code>Context</code> object
     * @param args JPO Packed arguments of a HashMap with following data
     *        name - Attribute Group Name Pattern
     *        selectables - StringList attribute selectables
     * @returns - a MapList of Attributes Groups, each map with following key-Value Pairs
                   1) "name" - String containing AttributeGroup name
                   2) "attributes" - a MapList of Attributes, each map with following key-Value Pairs
                                    1) "qualifiedName" - String containing qualified name of the Attribute
                                    2) "name"          - String containing name of the Attribute
                                    3) "type"          - String containing type of the Attribute
                                    4) "default"       - String containing default value of the Attribute
                                    5) "description"   - String containing description of the Attribute
                                    6) "maxlength"     - String containing maxlength of the Attribute
                                    7) "dimension"     - name of the dimension
                                    8) "range"         - StringList of range Values
                                    Note: no key-Value pair exists in this map if there is no value for that key
     */
    public static MapList getAttributeGroupsDetails(Context context,String[] args)throws Exception{
    	
    	HashMap paramMap = JPO.unpackArgs(args);
    	
    	String namePattern = (String)paramMap.get("name");
    	
    	MapList attributeGroups = new MapList();
        StringList selectables = new StringList();
        selectables.add("type");
        selectables.add("range");
        selectables.add("multiline");
        selectables.add("default");
        selectables.add("description");
        selectables.add("maxlength");
        selectables.add("dimension");
        
        
        String cmd = "list interface $1 where $2 select name description dump $3 recordsep $4";
        
        String result = MqlUtil.mqlCommand(context, cmd,namePattern,
        		        "derived==\""+LibraryCentralConstants.INTERFACE_CLASSIFICATION_ATTRIBUTE_GROUPS+"\"",
        		        ",",
        		        "#");
        

        StringList slAttributeGroups = FrameworkUtil.split(result, "#");
        
        //for each attribute group
        for(int i=0;i< slAttributeGroups.size();i++){
            HashMap attributeGroup = new HashMap();
            String attributeGroupNameDescription = (String)slAttributeGroups.get(i);
            String attributeGroupName = attributeGroupNameDescription.substring(0,attributeGroupNameDescription.indexOf(","));
            
            String attributeGroupDescription = attributeGroupNameDescription.substring(attributeGroupName.length()+1);
            		
            attributeGroup.put("name", attributeGroupName);
            attributeGroup.put("description", attributeGroupDescription);
            MapList attributes = getAttributeGroupAttributesDetails(context, attributeGroupName, selectables);
            attributeGroup.put("attributes", attributes);
            attributeGroups.add(attributeGroup);
        }
        return attributeGroups;
    }
    
    /**
     * Method to get the Details of all attributes in a Attribute Group
     * @param context the eMatrix <code>Context</code> object
     * @param agName - Attribute Group Name
     * @param selectables - StringList attribute selectables
     * @return MapList containing Maps of attribute Details, Each Map Contains
     *             key   - attribute's selectable, ex:type ,
     *             value - String or StringList Based on the Number of available values for the specified attribute's selectable
     *                     value will be MapList for the key "range" each map containing an operator and a value
     *             keys "name" and "qualifiedName" are included by default even though they are not in selectables
     */
    protected static MapList getAttributeGroupAttributesDetails(Context context,String agName,StringList selectables)throws Exception{
        StringBuffer cmd = new StringBuffer("print interface \"$1\" select "); // Move select
        
        selectables.add("owner");
        selectables.add("hidden");
        
        String[] newArgs = new String[selectables.size()+1];
        newArgs[0] = agName;
        for(int i=0;i<selectables.size();i++){
            cmd.append("\"$"+(i+2)+"\" ");
            newArgs[i+1] = "attribute."+(String)selectables.get(i);
        }

        String result = MqlUtil.mqlCommand(context,cmd.toString(),true,newArgs);

        HashMap hmAllAttributeDetails = parseMqlOutput(context, result);
        MapList agAttributesDetails = new MapList();

        Set setAllAttributeDetails = hmAllAttributeDetails.keySet();
        Iterator itr = setAllAttributeDetails.iterator();
        while(itr.hasNext()){
            String attributeName = (String)itr.next();
            HashMap hmAttributeDetails = (HashMap)hmAllAttributeDetails.get(attributeName);
            if(hmAttributeDetails != null){
                String hidden = (String)hmAllAttributeDetails.get("hidden");
                if("true".equalsIgnoreCase(hidden)){
                    continue;
                }
            	String owner = (String)hmAttributeDetails.get("owner");
            	String qualifiedName = attributeName;
            	if(UIUtil.isNotNullAndNotEmpty(owner)){
            		qualifiedName = owner+"."+attributeName;
            	}
                hmAttributeDetails.put("qualifiedName", qualifiedName);
                hmAttributeDetails.put("name", attributeName);
                
                // convert range StringList to MapList
                StringList range = (StringList)hmAttributeDetails.get("range");
                if(range != null){
                	MapList mlRange = new MapList();
                	Iterator<String> rangeItr = range.iterator();
                	while(rangeItr.hasNext()){
                		String rangeItem = rangeItr.next();
                		int rangeValueIndex = rangeItem.indexOf(" ");
                		if(rangeValueIndex == -1){
                			rangeItem = rangeItem + " ";
                			rangeValueIndex = rangeItem.indexOf(" ");
                			}
                		String rangeOperator = rangeItem.substring(0, rangeValueIndex);
                		if(rangeOperator.equals("uses")){
                			rangeOperator = "program";
                			rangeValueIndex = rangeItem.indexOf(" ", rangeValueIndex+1);
                		}
                		
                		String rangeValue = rangeItem.substring(rangeValueIndex+1);
                		
                		HashMap hmRangeItem = new HashMap();
                		hmRangeItem.put("operator", rangeOperator);
                		hmRangeItem.put("value", rangeValue);
                		
                		mlRange.add(hmRangeItem);
                	}
                	hmAttributeDetails.put("range", mlRange);
                }
                agAttributesDetails.add(hmAttributeDetails);
            }
        }

        return agAttributesDetails;
    }

    /**
     * method to get the MapList of column Maps for dynamic columns for the classification attributes
     * @param context
     * @param classificationAttributes - MapList containing Maps of attribute Details, Each Map Containing attribute details for
     *             keys   - "name", "type", "range"
     *
     * @return List of Column Maps
     * @throws FrameworkException
     */
    protected static List getDynamicColumnsMapList(Context context,MapList classificationAttributes) throws Exception{
        //Define a new MapList to return.
        MapList columnMapList = new MapList();

        String strLanguage =  context.getSession().getLanguage();

        // attributeAttributeGroupMap contains all the attribute group names to which each attribute belongs
        HashMap attributeAttributeGroupMap = new HashMap();

        for(int i=0;i<classificationAttributes.size();i++){
            HashMap attributeGroup = (HashMap)classificationAttributes.get(i);
            String attributeGroupName = (String)attributeGroup.get("name");
            MapList attributes = (MapList)attributeGroup.get("attributes");
            for(int j=0;j<attributes.size();j++){
                HashMap attribute =  (HashMap)attributes.get(j);
                String attributeQualifiedName = (String)attribute.get("qualifiedName");
                String attributeName = (String)attribute.get("name");
                HashMap colMap = new HashMap();
                HashMap settingsMap = new HashMap();
                // PSA11 set the auto filter to true for IR-442214. 
                settingsMap.put("Auto Filter", "true");
                settingsMap.put("Field Type", "attribute");
                settingsMap.put("Group Header",attributeGroupName);
                settingsMap.put("On Change Handler", "reloadDuplicateAttributes");
                settingsMap.put("Editable", "true");
                settingsMap.put("Registered Suite", "LibraryCentral");
                String adminName = UICache.getSymbolicName(context, attributeName, "attribute");
                settingsMap.put("Admin Type", adminName);
                
                //format date field
                String attributeType = (String)attribute.get("type");
                if(attributeType.equals("timestamp")){
                    settingsMap.put("format", "date");
					settingsMap.put("Sort Type", "date");
                }
                
                //format numeric field
                if(attributeType.equals("integer") || attributeType.equals("real")){
                	colMap.put("sorttype", "numeric");
                }

                //if range is defined , display as combobox
                MapList range = (MapList)attribute.get("range");
                if(range != null){
                    settingsMap.put("Input Type", "combobox");
                }

                //format boolean field
                if(attributeType.equals("boolean")){
                    if(range == null){
                        settingsMap.put("Input Type", "combobox");
                        settingsMap.put("Range Program", "emxLibraryCentralClassificationAttributes");
                        settingsMap.put("Range Function", "getRangeValuesForBooleanAttributes");
                    }
                }

                String isMultiline=(String)attribute.get("multiline");
                if (BOOLEAN_TRUE.equalsIgnoreCase(isMultiline)) {
                settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTAREA);
                }

                //apply UOM details (if Present)
                if(UOMUtil.isAssociatedWithDimension(context, attributeQualifiedName)) {
                    StringList listUOMunits = UOMUtil.getDimensionUnits(context, attributeQualifiedName);
                    String sDBunit = UOMUtil.getDBunit(context, attributeQualifiedName);
                    colMap.put(UOM_ASSOCIATEDWITHUOM, "true");
                    colMap.put(DB_UNIT, sDBunit);
                    colMap.put(UOM_UNIT_LIST, listUOMunits);
                }else {
                    colMap.put(UOM_ASSOCIATEDWITHUOM, "false");
                }

                colMap.put("name",attributeGroupName+"|"+attributeQualifiedName);
                
                colMap.put("label",i18nNow.getAttributeI18NString((String)attribute.get("name"),strLanguage));
                colMap.put("expression_businessobject","attribute["+attributeQualifiedName+"].value");
                colMap.put("settings",settingsMap);
                columnMapList.add(colMap);

                String attributeGroupNames = (String)attributeAttributeGroupMap.get(attributeQualifiedName);
                if(attributeGroupNames == null){
                    attributeAttributeGroupMap.put(attributeQualifiedName, attributeGroupName);
                }else{
                    attributeAttributeGroupMap.put(attributeQualifiedName, attributeGroupNames + "|" + attributeGroupName);
                }
            }
        }

        //update "AttributeGroups" and "Mass Update" settings
        // Mass update should be false for duplicate attributes
        // Attribute Groups information will be used for reloading the duplicate values
        Iterator itr = columnMapList.iterator();
        while(itr.hasNext()){
            HashMap colMap = (HashMap)itr.next();
            HashMap settingsMap = (HashMap)colMap.get("settings");
            String columnName = (String)colMap.get("name");
            String attributeGroupName = columnName.substring(0,columnName.indexOf('|'));
            String attributeName = columnName.substring(columnName.indexOf('|')+1);

            String allAttributeGroupsNames = (String)attributeAttributeGroupMap.get(attributeName);
            settingsMap.put("AttributeGroups",allAttributeGroupsNames);
            if(allAttributeGroupsNames.indexOf('|') == -1){
                allAttributeGroupsNames += "|";
            }
            if(!(allAttributeGroupsNames.startsWith(attributeGroupName+"|"))){
                //except for the First attribute group , add Mass Update = fasle setting
                settingsMap.put("Mass Update","false");
            }
        }

        return columnMapList;
    }

    /***
     * This method create the settingsMap and fieldMap to display all the Classification Attributes.
     * The list of Attributes are looped through and check is performed whether the attributes
     * is of type Integer/String/Real/Date/Boolean, the fieldMap is set with the appropriate
     * settings for each of the attribute type.
     * @param context
     * @param classificationAttributesList
     * @param formMode
     * @param sLanguage
     * @since R215
     * @return MapList containing the settingMap
     * @throws Exception
     */
    private MapList getDynamicFieldsMapList(Context context,MapList classificationAttributesList,String formName,boolean isCreate) throws Exception{

        //Define a new MapList to return.
        MapList fieldMapList = new MapList();
        String strLanguage =  context.getSession().getLanguage();

        // attributeAttributeGroupMap contains all the attribute group names to which each attribute belongs
        HashMap attributeAttributeGroupMap = new HashMap();

        if(classificationAttributesList == null)
            return fieldMapList;

        Iterator classItr = classificationAttributesList.iterator();
        while(classItr.hasNext()){

            Map classificationAttributesMap = (Map)classItr.next();


            MapList classificationAttributes = (MapList)classificationAttributesMap.get("attributes");
            String className = (String)classificationAttributesMap.get("className");
            if(classificationAttributes.size() > 0){
                HashMap settingsMapForClassHeader = new HashMap();
                HashMap fieldMapForClassHeader = new HashMap();
                settingsMapForClassHeader.put(SETTING_FIELD_TYPE,"Section Header");
                settingsMapForClassHeader.put(SETTING_REGISTERED_SUITE,"LibraryCentral");
                settingsMapForClassHeader.put("Section Level","1");
                fieldMapForClassHeader.put(LABEL,className);
                fieldMapForClassHeader.put("settings", settingsMapForClassHeader);
                fieldMapList.add(fieldMapForClassHeader);
            }
        for(int i=0;i<classificationAttributes.size();i++){
            HashMap attributeGroup = (HashMap)classificationAttributes.get(i);
            String attributeGroupName = (String)attributeGroup.get("name");
            MapList attributes = (MapList)attributeGroup.get("attributes");
            HashMap settingsMapForHeader = new HashMap();
            HashMap fieldMapForHeader = new HashMap();
            settingsMapForHeader.put(SETTING_FIELD_TYPE,"Section Header");
            settingsMapForHeader.put(SETTING_REGISTERED_SUITE,"LibraryCentral");
            settingsMapForHeader.put("Section Level","2");
            fieldMapForHeader.put(LABEL,attributeGroupName);
            fieldMapForHeader.put("settings", settingsMapForHeader);
            fieldMapList.add(fieldMapForHeader);
             for(int j=0;j<attributes.size();j++){
                HashMap attribute =  (HashMap)attributes.get(j);
                String attributeQualifiedName = (String)attribute.get("qualifiedName");
                String attributeName = (String)attribute.get("name");
                HashMap fieldMap = new HashMap();
                HashMap settingsMap = new HashMap();
                fieldMap.put(NAME,attributeGroupName+"|"+attributeQualifiedName);
                fieldMap.put(LABEL,i18nNow.getAttributeI18NString(attributeName,strLanguage));
                fieldMap.put(EXPRESSION_BUSINESSOBJECT,"attribute["+attributeQualifiedName+"].value");
                String attributeType = (String)attribute.get("type");
                if(attributeType.equals(FORMAT_TIMESTAMP)){
                    settingsMap.put(SETTING_FORMAT, FORMAT_DATE);
                    settingsMap.put(SETTING_CLEAR, "true");
                }
               else if(attributeType.equals(FORMAT_BOOLEAN) ){
                    settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                    settingsMap.put(SETTING_REMOVE_RANGE_BLANK,"true");
                    MapList range = (MapList)attribute.get("range");

                    if(range==null){
                    settingsMap.put(SETTING_RANGE_PROGRAM, "emxLibraryCentralClassificationAttributes");
                    settingsMap.put(SETTING_RANGE_FUNCTION, "getRangeValuesForBooleanAttributes");

                    }
                }
                else if(attributeType.equals(FORMAT_INTEGER)){
                		settingsMap.put(SETTING_FORMAT, FORMAT_INTEGER);
                		if(UOMUtil.isAssociatedWithDimension(context, attributeQualifiedName)) {
                        	addUOMDetailsToSettingsMap(context,attributeQualifiedName,fieldMap,settingsMap);
                        }
                        if(formName.equals("type_CreatePart"))
                            settingsMap.put(SETTING_VALIDATE, "isValidInteger");
                        //IR-195858V6R2014 checking whether attribute has range,if so
                        //setting the input type to combobox
                        if((MapList)attribute.get("range")!=null){
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                            settingsMap.put(SETTING_REMOVE_RANGE_BLANK,"true");
                        }
                }
                else if(attributeType.equals(FORMAT_REAL)){
                		settingsMap.put(SETTING_FORMAT, FORMAT_NUMERIC);
                		if(UOMUtil.isAssociatedWithDimension(context, attributeQualifiedName)) {
                        	addUOMDetailsToSettingsMap(context,attributeQualifiedName,fieldMap,settingsMap);
                        }
                        if(formName.equals("type_CreatePart"))
                            settingsMap.put(SETTING_VALIDATE, "checkPositiveReal");
                        //IR-195858V6R2014 checking whether attribute has range,if so
                        //setting the input type to combobox
                        if((MapList)attribute.get("range")!=null){
                            settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                            settingsMap.put(SETTING_REMOVE_RANGE_BLANK,"true");
                        }
                }
                else if(attributeType.equals(FORMAT_STRING))
    	        {
                	MapList range = (MapList)attribute.get("range");
                	String isMultiline=(String)attribute.get("multiline");
    	            if(range != null && range.size() > 0) {
    	            		//IR-227384V6R2014 due to FIELD_CHOICES & FIELD_DISPLAY_CHOICES
    	            		//setting ArryIndexoutofBounds exception is thrown,if the
    	            		//attribute having a range and if one of the values
    	            		//contains !, since BPS is removing this entry, there is a mismatch
    	            		//in the length of display values & actual values.
    	            		//hence settings has been removed. BPS will handle range values
    	            		settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_COMBOBOX);
                        	settingsMap.put(SETTING_FORMAT, FORMAT_STRING);
                        	settingsMap.put(SETTING_REMOVE_RANGE_BLANK,"true");
                        	settingsMap.put(SETTING_REGISTERED_SUITE,"Framework");
                        	String adminName = UICache.getSymbolicName(context, attributeName,"attribute");
                        	settingsMap.put("Admin Type",adminName);
    	            } else if (BOOLEAN_TRUE.equalsIgnoreCase(isMultiline)) {
    	            	settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTAREA);
    	            } else {
    	            	settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTBOX);
    	            }
    	        }
                else{

                }

                settingsMap.put(SETTING_FIELD_TYPE,FIELD_TYPE_ATTRIBUTE);
                if(isCreate){
                    settingsMap.put(SETTING_UPDATE_PROGRAM,"emxLibraryCentralCommon");
                    settingsMap.put(SETTING_UPDATE_FUNCTION,"dummyUpdateFunction");
                }

                //On Change Handler
                settingsMap.put("OnChange Handler", "reloadDuplicateAttributesInForm");
                fieldMap.put("settings",settingsMap);
                fieldMapList.add(fieldMap);
                String attributeGroupNames = (String)attributeAttributeGroupMap.get(attributeQualifiedName);
                if(attributeGroupNames == null){
                    attributeAttributeGroupMap.put(attributeQualifiedName, attributeGroupName);
                }else{
                    attributeAttributeGroupMap.put(attributeQualifiedName, attributeGroupNames + "|" + attributeGroupName);
                }
            }
        }

        }

        //update "AttributeGroups"
        //Attribute Groups information will be used for reloading the duplicate values
        Iterator itr = fieldMapList.iterator();
        while(itr.hasNext()){
            HashMap fieldMap = (HashMap)itr.next();
            HashMap settingsMap = (HashMap)fieldMap.get("settings");
            if( !"Section Header".equals(settingsMap.get("Field Type")) ){
	            String fieldName = (String)fieldMap.get("name");
	            String attributeName = fieldName.substring(fieldName.indexOf('|')+1);

	            String allAttributeGroupsNames = (String)attributeAttributeGroupMap.get(attributeName);
	            settingsMap.put("AttributeGroups",allAttributeGroupsNames);
            }
        }


        // Add a program HTML field which contains a javascript to reload duplicate attributes in the FORM
        HashMap reloadFunctionField = new HashMap();
        HashMap reloadFunctionFieldSettings = new HashMap();
        reloadFunctionFieldSettings.put(SETTING_FIELD_TYPE, "programHTMLOutput");
        reloadFunctionFieldSettings.put("program","emxLibraryCentralClassificationAttributes");
        reloadFunctionFieldSettings.put("function","getReloadDuplicateAttributesInForm");

        reloadFunctionField.put("name","reloadFunctionField");
        reloadFunctionField.put("settings",reloadFunctionFieldSettings);

        fieldMapList.add(reloadFunctionField);

        return fieldMapList;
    }
/***
 *  This method adds all the UOM details required to display Classification Attribute
 *  during create Generic Document/Part. To display UOM details settingsMap should
 *  contain Field Type=Attribute, otherwise the UI would display only textbox next to the
 *  UOM Field.Once the map contains FieldType=Attribute, BPS code assumes that this Attribute
 *  is defined on the Type, but in case of Classification Attributes it's not,
 *  Hence to overcome this bug a Dummy update program & function is used  here, If a update program
 *  & Function is defined BPS wouldn't check whether the attribute is defined on the type.
 * @param context
 * @param attributeName
 * @param fieldMap
 * @param settingsMap
 * @since R215
 * @throws FrameworkException
 */
    private void addUOMDetailsToSettingsMap(Context context,String attributeName,HashMap fieldMap,HashMap settingsMap) throws FrameworkException{
    	fieldMap.put(UOM_ASSOCIATEDWITHUOM, BOOLEAN_TRUE);
    	fieldMap.put(DB_UNIT, UOMUtil.getSystemunit(context, null,attributeName,null));
    	fieldMap.put(UOM_UNIT_LIST, UOMUtil.getDimensionUnits(context, attributeName));
        settingsMap.put(SETTING_EDITABLE_FIELD,BOOLEAN_TRUE);
        settingsMap.put(SETTING_INPUT_TYPE, INPUT_TYPE_TEXTBOX);
    }


    public static String getReloadDuplicateAttributesInForm(Context context,String[] args){

        String reloadFunction =  "<script type=\"text/javascript\" >" + "\n" +
                "//<!--" + "\n" +
                "function reloadDuplicateAttributesInForm(fieldName,fieldValue){"+ "\n" +
                    "var currentActualValue  = fieldValue.current.actual;"+ "\n" +
                    "var currentDisplayValue = fieldValue.current.display;"+ "\n" +
                    "var fieldNameValues     =  fieldName.split(\"|\");"+ "\n" +
                    "if (FormHandler) {"+ "\n" +
                        "var attributeGroups     = FormHandler.GetField(fieldName).GetSettingValue(\"AttributeGroups\");"+ "\n" +
                        "var attributeGroupsList = attributeGroups.split(\"|\");"+ "\n" +
                        "for(var i = 0; i < attributeGroupsList.length; i++) {"+
                            "if(attributeGroupsList[i] != \"\" && attributeGroupsList[i] != fieldNameValues[0]) {"+
                                "FormHandler.GetField(attributeGroupsList[i]+\"|\"+fieldNameValues[1]).SetFieldValue(currentActualValue,currentDisplayValue);"+
                            "}"+
                        "}"+

                    "}"+ "\n" +
                "}"+ "\n" +
                "//-->" + "\n" +
            "</script>";


        return reloadFunction;
    }

    /**
     * this method provides Dynamic Columns Map to display Classification Attributes in Classified Items Page Dynamically
     * @param context the eMatrix <code>Context</code> object
     * @param args - Packed Arguments containing requestMap which contains objectId of the class
     * @return List of column map for each Classification attribute in this class
     * @throws Exception
     */
    public static List getClassificationAttributesColumns(Context context,String[] args) throws Exception{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String classObjectId = (String)requestMap.get("objectId");
        MapList classificationAttributes = getClassClassificationAttributes(context, classObjectId);
        List columnMapList = getDynamicColumnsMapList(context,classificationAttributes);
        return columnMapList;
    }

    /***
     * This method is used to display all the Classification Attributes during create Generic Document
     * or Part. If the Class/PF contains Attribute Group all the attributes belonging each Attribute Group
     * is retrieved using  getClassClassificationAttributes method. Once the list is obtained settinsMap
     * and filedMap is constructed using getDynamicFieldsDuringCreate
     * Function Function_033728
     * @param context
     * @param args
     * @return MapList
     * @since R215
     * @throws Exception
     */
    public MapList getClassificationAttributesForForm(Context context,String[] args) throws Exception{
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String endItemObjectId = (String)requestMap.get("objectId");
        String formName = (String) requestMap.get("form");
        MapList fieldMapList=new MapList();

        MapList classificationAttributesList = new MapList();
        DomainObject endItemObj = new DomainObject(endItemObjectId);

        StringList objectSelects = new StringList();
        objectSelects.add(SELECT_ID);
        objectSelects.add(SELECT_NAME);

        MapList classificationList = endItemObj.getRelatedObjects(context,
                "Classified Item",
                "*",
                objectSelects,
                null,
                true,//boolean getTo,
                false,//boolean getFrom,
                (short)0,
                null,
                null,
                0);
        //MapList classificationAttributes = getClassClassificationAttributes(context, classObjectId);
        //fieldMapList=getDynamicFieldsDuringCreate(context,classificationAttributes,formName,sLanguage);

        int noOfClasses = classificationList.size();

        if(noOfClasses>0){
            Iterator itr = classificationList.iterator();
            while(itr.hasNext()){
                Map classMap = (Map)itr.next();

                MapList classificationAttributes = getClassClassificationAttributes(context, (String)classMap.get(SELECT_ID));
                if(classificationAttributes.size()>0){
                    HashMap classificationAttributesMap = new HashMap();
                    classificationAttributesMap.put("className", classMap.get(SELECT_NAME));
                    classificationAttributesMap.put("attributes", classificationAttributes);

                    classificationAttributesList.add(classificationAttributesMap);
                }
            }

            fieldMapList=getDynamicFieldsMapList(context,classificationAttributesList,formName,false);
        }

        return fieldMapList;
    }

    /***
     * This method is used to display all the Classification Attributes during create Generic Document
     * or Part. If the Class/PF contains Attribute Group all the attributes belonging each Attribute Group
     * is retrieved using  getClassClassificationAttributes method. Once the list is obtained settinsMap
     * and filedMap is constructed using getDynamicFieldsDuringCreate
     * Function Function_033728
     * @param context
     * @param args
     * @return MapList
     * @since R215
     * @throws Exception
     */
    public MapList displayClassificationAttributesDuringCreate(Context context,String[] args) throws Exception{
    	HashMap programMap=(HashMap)JPO.unpackArgs(args);
    	HashMap requestMap = (HashMap)programMap.get("requestMap");
        String classObjectId = (String)requestMap.get("objectId");
		String formName = (String) requestMap.get("form");
		MapList fieldMapList=new MapList();
		//checking whether the class has any Attribute Groups if yes, then only invoke getClassClassificationAttributes
		//Since from ENG, create part can be called from Global Toolbar
		if(UIUtil.isNotNullAndNotEmpty(classObjectId)){
            MapList classificationAttributesList = new MapList();
            HashMap classificationAttributesMap = new HashMap();
            DomainObject classObj = new DomainObject(classObjectId);
            String className = classObj.getInfo(context, SELECT_NAME);
            classificationAttributesMap.put("className", className);
			MapList classificationAttributes = getClassClassificationAttributes(context, classObjectId);
            classificationAttributesMap.put("attributes", classificationAttributes);
            classificationAttributesList.add(classificationAttributesMap);
            fieldMapList=getDynamicFieldsMapList(context,classificationAttributesList,formName,true);
		}
        return fieldMapList;
    }
    
    
    /***
     * This method is used to display all the Classification Attributes for the selected classes in the Classification Node of End Items
     * 
     * @param context
     * @param args
     * @return MapList
     * @since R215
     * @throws Exception
     */
    public MapList getClassificationAttributesForClassificationEditForm(Context context,String[] args) throws Exception{
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String endItemObjectId = (String)requestMap.get("objectId");
        String strSelectedClassids = (String)requestMap.get("selectedClassIds");
        if(strSelectedClassids == null){
            strSelectedClassids = "";
        }
        
        String formName = (String) requestMap.get("form");
        MapList fieldMapList=new MapList();
        
        MapList classificationAttributesList = new MapList();
        DomainObject endItemObj = new DomainObject(endItemObjectId);
        
        StringList objectSelects = new StringList();
        objectSelects.add(SELECT_ID);
        objectSelects.add(SELECT_NAME);
        
        StringBuffer objectWhere = new StringBuffer();
        StringList slSelectedClassids = FrameworkUtil.split(strSelectedClassids, ",");
        
        Iterator classIdItr = slSelectedClassids.iterator();
        while(classIdItr.hasNext()){
            if(objectWhere.length()>0){
                objectWhere.append(" || ");
            }
            objectWhere.append("id == ");
            objectWhere.append(classIdItr.next());
        }
        
        MapList classificationList = endItemObj.getRelatedObjects(context,
                "Classified Item",
                "*",
                objectSelects,
                null,
                true,//boolean getTo,
                false,//boolean getFrom,
                (short)0,
                objectWhere.toString(),
                null,
                0);
        
        int noOfClasses = classificationList.size();
        
        if(noOfClasses>0){
            Iterator itr = classificationList.iterator();
            while(itr.hasNext()){
                Map classMap = (Map)itr.next();
                
                MapList classificationAttributes = getClassClassificationAttributes(context, (String)classMap.get(SELECT_ID));
                if(classificationAttributes.size()>0){
                    HashMap classificationAttributesMap = new HashMap();
                    classificationAttributesMap.put("className", classMap.get(SELECT_NAME));
                    classificationAttributesMap.put("attributes", classificationAttributes);
                    
                    classificationAttributesList.add(classificationAttributesMap);
                }
            }
            
            fieldMapList=getDynamicFieldsMapList(context,classificationAttributesList,formName,false);
        }
        
        return fieldMapList;
    }
    
    /**
     * this method parse the mql Output of mutiple lines,
     *  each line is of the form
     *  property[propertyName].subProperty = result
     *     where
     *       property      - should be present
     *                     - should not contain characters [ ] . =
     *                     - property in all the lines should be same
     *       propertyName  - should be present
     *                     - may contain . or = characters
     *                     - should not contain characters [ ]
     *       subProperty   - should be present
     *                     - should not contain . or = characters
     *                     - can end with [i] ,where i is 0, 1, 2, 3 ...
     *       result        - may or may not present
     *
     * @param context the eMatrix <code>Context</code> object
     * @param output mql output to be parsed
     * @return a HashMap with following key value pair
     *            key   - propertyName
     *            value - HashMap with following key value pair
     *                      key   - subProperty
     *                      value - String result
     *
     * @throws Exception
     */
    protected static HashMap parseMqlOutput(Context context,String output) throws Exception{
        BufferedReader in = new BufferedReader(new StringReader(output));
        String resultLine;
        HashMap mqlResult = new HashMap();
        while((resultLine = in.readLine()) != null){
            String property = null;
            String propertyName = null;
            String subProperty = null;
            String result = null;

            try{
                //identify property propertyValue subProperty subPropertyValue  - start
                boolean hasRanges = false;
                int propNameStartDelimIndex = resultLine.indexOf(PROPNAME_START_DELIMITER);
                int resultDelimIndex        = resultLine.indexOf(RESULT_DELIMITER);

                property                    = resultLine.substring(0, propNameStartDelimIndex);

                int propNameEndDelimIndex   = resultLine.indexOf(PROPNAME_END_DELIMITER, propNameStartDelimIndex);
                propertyName                = resultLine.substring(propNameStartDelimIndex+1, propNameEndDelimIndex);

                String propertyAndValue     = property + PROPNAME_START_DELIMITER+propertyName+PROPNAME_END_DELIMITER;
                String remainingResultLine  = resultLine.substring(propertyAndValue.length());

                // if remaining result starts with .
                int rangeStartDelimIndex    = remainingResultLine.indexOf(RANGE_START_DELIMITER);
                resultDelimIndex            = remainingResultLine.indexOf(RESULT_DELIMITER);
                if((rangeStartDelimIndex != -1) && (rangeStartDelimIndex < resultDelimIndex)){
                    // if [ exists and comes before = , then anything before [ is the subProperty and subProperty contains range of results
                    subProperty = remainingResultLine.substring(1,rangeStartDelimIndex);
                    hasRanges   = true;
                }else{
                    // else , anything Before = is the subProperty
                    subProperty = remainingResultLine.substring(1,resultDelimIndex);
                }

                result   = remainingResultLine.substring(resultDelimIndex+RESULT_DELIMITER.length());

                property = property.trim();
                result   = result.trim();

                //identify property propertyValue subProperty subPropertyValue  - end

                //start building HashMap
                HashMap hmPropertyName;
                StringList slSubProperty;

                hmPropertyName = (HashMap)mqlResult.get(propertyName);
                if(hmPropertyName == null){
                    hmPropertyName = new HashMap();
                    mqlResult.put(propertyName, hmPropertyName);
                }
                if(hasRanges){
                    slSubProperty = (StringList)hmPropertyName.get(subProperty);
                    if(slSubProperty == null){
                        slSubProperty = new StringList();
                        hmPropertyName.put(subProperty,slSubProperty);
                    }
                    slSubProperty.add(result);
                }else{
                    hmPropertyName.put(subProperty,result);
                }

            }catch(Exception e){
                // if there is exception during parsing a line , proceed to next line
            }
        }

        return mqlResult;
    }

    /**
     * this method provides values for "Classification Attributes" column in BOM Page
     * @param context the eMatrix <code>Context</code> object
     * @param args - Packed Arguments containing objectList(a MapList of objects selected for disply in BOM page)
     * @return a Vector of ColumnValues for the selected objects , each ColumnValue Contains programHTMLOutput
     * @throws Exception during failure
     */
    public static Vector getBOMClassificationAttributes(Context context,String[] args) throws Exception{

        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        MapList objectList  = (MapList)programMap.get("objectList");
        int objectListSize  = objectList.size();
        HashMap paramMap    = (HashMap) programMap.get("paramList");
        String reportFormat = (String)paramMap.get("reportFormat");

        double clientTZOffset = (new Double((String)paramMap.get("timeZone"))).doubleValue();
        String strLanguage    = context.getSession().getLanguage();

        // vector of column values to return
        Vector columnValues = new Vector(objectListSize);

        // form the list of unique objectIds in objectList
        StringList uniqueObjectIds = new StringList();
        for (int i = 0; i < objectListSize; i++){
            String objectId = (String)((Map)objectList.get(i)).get("id");
            if(!uniqueObjectIds.contains(objectId)){
                uniqueObjectIds.add(objectId);
            }
        }

        // get all the classification attribute names for unique object ids
        HashMap classificationAttributesNames = getClassificationAttributeNames(context, uniqueObjectIds,reportFormat);

        // get attribute values for all unique objectIds
        // attributeValuesMap is hashmap with key --> objectid
        //                                    value --> String of attribute name = value pairs seperated by |
        HashMap attributeValuesMap = getClassificationAttributeValues(context, classificationAttributesNames, uniqueObjectIds,reportFormat,clientTZOffset);

        // view details link URL -start
        StringBuffer commonViewDetailsLinkUrl = new StringBuffer();
        commonViewDetailsLinkUrl.append("../common/emxForm.jsp?");
        commonViewDetailsLinkUrl.append("form=LBCObjectClassificationEditAttributes");
        commonViewDetailsLinkUrl.append("&formHeader=emxLibraryCentral.Common.ClassificationAttributes");
        commonViewDetailsLinkUrl.append("&suiteKey=LibraryCentral");
        commonViewDetailsLinkUrl.append("&Export=false");
        commonViewDetailsLinkUrl.append("&toolbar=LCClassificationAttributesToolbar");
        commonViewDetailsLinkUrl.append("&targetLocation=slidein");
        commonViewDetailsLinkUrl.append("&HelpMarker=emxhelpviewclassificationattribute");
        // view details link URL -end


        // add View Details Link to all column values
        for (int i = 0; i < objectListSize; i++){

            String objectId = (String)((Map)objectList.get(i)).get("id");
            String sbCellValue = (String)attributeValuesMap.get(objectId);

            if(sbCellValue.length()!=0){
                StringBuffer viewDetailsLink = new StringBuffer();
                String i18ViewDetails = EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(strLanguage),"emxLibraryCentral.Common.ViewDetails");
                if(reportFormat == null ){
                    StringBuffer viewDetailsLinkUrl = new StringBuffer(commonViewDetailsLinkUrl);
                    viewDetailsLinkUrl.append("&objectId="+XSSUtil.encodeForURL(context, objectId));
                    viewDetailsLinkUrl.append("&parentOID="+XSSUtil.encodeForURL(context, objectId));

                    viewDetailsLink.append("<a href=\"javascript:top.showSlideInDialog('");
                    viewDetailsLink.append(FrameworkUtil.encodeURL(viewDetailsLinkUrl.toString()));
                    viewDetailsLink.append("',true);\" >");
                    viewDetailsLink.append(i18ViewDetails);
                    viewDetailsLink.append("</a>");
                }else if( reportFormat.equals("HTML")){
                    viewDetailsLink.append(i18ViewDetails);
                }
                sbCellValue += " "+viewDetailsLink;
            }
            columnValues.add(sbCellValue);
        }
        return columnValues;
    }

    /**
     * this method returns the Classification Attribute Names for all the objectIds passed as argument
     * @param context the eMatrix <code>Context</code> object
     * @param slObjectIds StringList of ObjectIds (of end items)
     * @return a HashMap containing following values
     *          - allAttributeNames - StringList of all Classification Attribute Names for all objectIds
     *          - attributeNamesMap - HashMap having Classification Attribute Names for each objectId
     * @throws Exception
     */
    protected static HashMap getClassificationAttributeNames(Context context, StringList slObjectIds, String reportFormat)throws Exception{
        String[] objectIds = (String[]) slObjectIds.toArray(new String[0]);
        StringList objectSelects = new StringList();
        objectSelects.add("id");
        objectSelects.add("interface.attribute.interface.kindof");
        objectSelects.add("interface.allparents");

        //this DB query is to get Attribute Names
        MapList attributeNamesResult = DomainObject.getInfo(context, objectIds, objectSelects);

        // allAttributeNames is the string list containing unique "Classification attribute" names across all object ids
        StringList allAttributeNames = new StringList();// contains all unique classification attributes

        // attributeNamesMap is hashmap with key --> objectid
        //                                   value --> StringList of  "Classification attribute" names for this object id
        HashMap attributeNamesMap = new HashMap();

        // iterate db result for each object id
        Iterator itr = attributeNamesResult.iterator();
        while(itr.hasNext()){
            HashMap objectDetails = (HashMap)itr.next();
            String id = (String)objectDetails.get("id");

            // allParents is the StringList of all parents of this objects's interfaces, this list contains Attribute Groups
            StringList allParents = new StringList();
            String strAllParents = (String)objectDetails.get("interface.allparents");
            if(!UIUtil.isNullOrEmpty(strAllParents)){
                allParents = new StringList(strAllParents.split("\\a"));
            }
            StringList attributesNames = new StringList();
            int noOfAttributes = 0;

            //iterate all key-value pairs obtained from DB for this object id,
            //iterate till total legth of all parsed attribute names is less than 256 characters
            Set keys = objectDetails.keySet();
            Iterator keyItr = keys.iterator();
            while(keyItr.hasNext()){
                String key = (String)keyItr.next();
                String value = (String)objectDetails.get(key);
                if(!key.equals("id") && !key.equals("interface.allparents")){
                    // here  key is of the form "interface.attribute[Actual Cost].interface[AG002].kindof"
                    // extract attributeName and attributeGroupName from the key
                    String attributeName = key.substring(key.indexOf('[')+1, key.indexOf(']'));
                    String attributeGroupName = key.substring(key.lastIndexOf('[')+1,key.lastIndexOf(']'));

                    //select the attribute if attributeGroupName is in allParents list and interface.attribute.interface.kindof is "Classification Attribute Groups"
                    if((value.indexOf("Classification Attribute Groups")!=-1) && allParents.contains(attributeGroupName)){
                        if(!attributesNames.contains(attributeName)){
                            attributesNames.add(attributeName);
                            noOfAttributes ++;
                        }
                        if(!allAttributeNames.contains(attributeName)){
                            allAttributeNames.add(attributeName);
                        }
                    }
                }
                if((reportFormat == null || reportFormat.equals("HTML")) && noOfAttributes >= BOM_MAX_NO_OF_ATTR){
                    break;
                }
            }
            attributeNamesMap.put(id, attributesNames);
        }

        HashMap classificationAttributeNames = new HashMap();
        classificationAttributeNames.put("allAttributeNames", allAttributeNames);
        classificationAttributeNames.put("attributeNamesMap", attributeNamesMap);

        return classificationAttributeNames;
    }

    /**
     * this method returns String of attribute name=value  pairs seperated by | for all the unique objectIds
     * @param context the eMatrix <code>Context</code> object
     * @param classificationAttributesNames
     *           - a HashMap containing following values
     *                   - allAttributeNames - StringList of all Classification Attribute Names for all objectIds
     *                   - attributeNamesMap - HashMap having Classification Attribute Names for each objectId
     * @param uniqueObjectIds - String list of objectIds for which attribute values need to be returned
     * @param isHTMLFormat - true if return value is of HTML format , othervise false
     * @return HashMap with
     *                 key   = objectId
     *                 value = a HTML/plaintext string containing Attribute Name = Value pairs separated by |
     *                         this string contains names which are in attributeNamesMap for this objectid
     *                         this string contains a maximum of 256 displayed characters
     *                         contains 3 dots (...) at the end, if not all attribute Names in attributeNames StringList are included
     */
    protected static HashMap getClassificationAttributeValues(Context context,HashMap classificationAttributesNames,StringList uniqueObjectIds,String reportFormat, double clientTZOffset) throws Exception{

        // allAttributeNames is the string list containing unique "Classification attribute" names across all object ids
        StringList allAttributeNames = (StringList)classificationAttributesNames.get("allAttributeNames");

        // attributeNamesMap is hashmap with key --> objectid
        //                                   value --> StringList of  "Classification attribute" names for this object id
        HashMap attributeNamesMap = (HashMap)classificationAttributesNames.get("attributeNamesMap");

        StringList objectSelects = new StringList();
        objectSelects.add("id");
        Iterator clsAttrItr = allAttributeNames.iterator();
        while(clsAttrItr.hasNext()){
            String attributeName = (String)clsAttrItr.next();
            objectSelects.add("attribute["+attributeName+"].inputvalue");
            objectSelects.add("attribute["+attributeName+"].inputunit");
            objectSelects.add("attribute["+attributeName+"].type");

        }

        String[] objectIds = (String[]) uniqueObjectIds.toArray(new String[0]);

        // get attribute values
        MapList attributeValuesResult = DomainObject.getInfo(context, objectIds, objectSelects);

        Iterator attrValuesItr = attributeValuesResult.iterator();
        HashMap attributeValuesMap = new HashMap();
        while(attrValuesItr.hasNext()){
            HashMap attributeValues = (HashMap)attrValuesItr.next();
            String objectId = (String)attributeValues.get("id");
            StringList attributeNames = (StringList)attributeNamesMap.get(objectId);
            String strAttributesValue = "";
            strAttributesValue = mergeAttributeNameValues(context, attributeNames,attributeValues,reportFormat,clientTZOffset);
            attributeValuesMap.put(objectId, strAttributesValue);
        }

        return attributeValuesMap;
    }



    /**
     * this method merges attribute Names and Values and returns a combined String
     * @param context the eMatrix <code>Context</code> object
     * @param attributeNames - StringList of Attribute Names
     * @param attributeValues - HashMap containing "Attribute Name" - "Attribute Value" as key-value pairs
     * @return String - a HTML string containing Attribute Name = Value pairs separated by |
     *                  this string contains names from StringList attributeNames only
     *                  this string contains a maximum of 256 displayed characters
     *                  contains 3 dots (...) at the end, if not all attribute Names in attributeNames StringList are included
     */
    protected static String mergeAttributeNameValues(Context context, StringList attributeNames, HashMap attributeValues,String reportFormat,double clientTZOffset)throws Exception{
        StringBuffer sbAttributesNameValue = new StringBuffer();
        String attributesNameValue = "";

        Locale localeObj          = context.getLocale();
        String strLanguage        = context.getSession().getLanguage();
        String attributeDelimiter = " <br/> ";
        String equalDelimeter     = " <b>=</b> ";
        if(reportFormat!=null && reportFormat.equals("CSV")){
            attributeDelimiter = " \n ";
            equalDelimeter  = " = ";
        }

        // for each name in attributeNames List , find the corresponding value from attributeValues and form a HTML string to be displayed on BOM page
        // form the string till its length is less than 256 characters
        Iterator itr              = attributeNames.iterator();
        while(itr.hasNext()){
            String attributeName = (String)itr.next();
            String attributeI18Name = i18nNow.getAttributeI18NString(attributeName,strLanguage);
            sbAttributesNameValue.append(attributeI18Name);
            sbAttributesNameValue.append(equalDelimeter);
            String attributeValue     = (String)attributeValues.get("attribute["+attributeName+"].inputvalue");
            String attributeType      = (String)attributeValues.get("attribute["+attributeName+"].type");
            if(attributeType.equals("timestamp")){
                attributeValue = eMatrixDateFormat.getFormattedDisplayDate(attributeValue, clientTZOffset, localeObj);
            }

            String attributeInputUnit = (String)attributeValues.get("attribute["+attributeName+"].inputunit");
            if(!UIUtil.isNullOrEmpty(attributeInputUnit)){
                String attributeI18InputUnit = (String)i18nNow.getDimensionI18NString( attributeInputUnit, strLanguage );
                if(!UIUtil.isNullOrEmpty(attributeI18InputUnit)){
                    attributeValue += " "+attributeI18InputUnit;
                }else{
                    attributeValue += " "+attributeInputUnit;
                }
            }
            String attributeActualValue = attributeValue;
            /*if(reportFormat == null || reportFormat.equals("HTML")){
                attributeActualValue = XSSUtil.encodeForHTML(context, attributeValue);
            }*/
            sbAttributesNameValue.append(attributeActualValue);
            sbAttributesNameValue.append(attributeDelimiter);

        }

        attributesNameValue = sbAttributesNameValue.toString();
        if(reportFormat != null && reportFormat.equals("CSV")){
            attributesNameValue = "\""+attributesNameValue+"\"";
        }


        return attributesNameValue;
    }

    /**
     * This method returns objectids of all the classes and libraries to which a classified Item is connected
     * @param context
     * @param args - String array containing object id of the Classified Item
     * @return
     * @throws Exception
     */
    public String getClassification(Context context, String[] args) throws Exception{
        String objectId = args[0];
        DomainObject domObj = new DomainObject(objectId);
        StringList objectSelects = new StringList();
        objectSelects.add("id");
        MapList classificationList = domObj.getRelatedObjects(context,
                "Classified Item,Subclass",
                "*",
                objectSelects,
                null,
                true,//boolean getTo,
                false,//boolean getFrom,
                (short)0,
                null,
                null,
                0);

        String strDel = matrix.db.SelectConstants.cSelectDelimiter;
        StringBuffer classification = new StringBuffer();
        Iterator itr = classificationList.iterator();
        while(itr.hasNext()){
            Map classificationMap = (Map) itr.next();
            classification.append(classificationMap.get("id"));
            classification.append(strDel);
        }
        return classification.toString();
    }


    public static HashMap getRangeValuesForBooleanAttributes(Context context, String[] args) throws FrameworkException
    {
        HashMap rangeMap = new HashMap();

        try
        {
            StringList fieldChoices = new StringList();
            StringList fieldDisplayChoices = new StringList();
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");
            String language = (String)paramMap.get("languageStr");
            String trueStr = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(language),"emxFramework.Range.BooleanAttribute.TRUE");
            String falseStr = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(language),"emxFramework.Range.BooleanAttribute.FALSE");
            //Modifed for IR-057820
            fieldChoices.add("TRUE");
            fieldChoices.add("FALSE");
            fieldDisplayChoices.add(trueStr);
            fieldDisplayChoices.add(falseStr);
            rangeMap.put("field_choices", fieldChoices);
            rangeMap.put("field_display_choices", fieldDisplayChoices);
        }catch(Exception ex)
        {
            throw new FrameworkException(ex.toString());
        }


        return rangeMap;
    }
	
    
    /**
     * 
     * This method creates Attribute Group and creates supplied attributes local to this Attribute Group
     *  
     * @param context
     * @param args JPO packed args of a HashMap containing an attributeGroup as below
     *        1) "name" - String containing AttributeGroup name (Optional , autonName is used if omitted)
     *        2) "description" - String Containing Attribute Group Description (Optional)
     *        3) "attributes" - a MapList of Attributes (Optional), each map with following key-Value Pair
     *                               1) "qualifiedName" - String containing qualified name of the Attribute
     *                               2) "name"          - String containing name of the Attribute
     *                               3) "type"          - String containing type of the Attribute
     *                               4) "default"       - String containing default value of the Attribute
     *                               5) "description"   - String containing description of the Attribute
     *                               6) "maxlength"     - String containing maxlength of the Attribute
     *                               7) "dimension"     - name of the dimension
     *                               8) "range"         - MapList of range Values
     *        
     * @throws Exception
     */
    public String createAttributeGroupWithAttributes(Context context, String[] args) throws Exception{
    	try{

    		HashMap paramMap = JPO.unpackArgs(args);

    		ContextUtil.startTransaction(context, true);

    		Map attributeGroup = (Map)paramMap.get("attributeGroup");
    		String attributeGroupName = (String)attributeGroup.get("name");
    		if(UIUtil.isNullOrEmpty(attributeGroupName)){
    			attributeGroupName = generateAGName(context);
    		}
    		
    		String attributeGroupDesc = (String)attributeGroup.get("description");
    		if(attributeGroupDesc == null){
    			attributeGroupDesc = "";
    		}
    		
    		MapList attributesData = (MapList)attributeGroup.get("attributes");
    		if(attributesData == null){
    			attributesData = new MapList();
    		}
    		

    		// reset attribute group name and description in input map
    		attributeGroup.put("name",attributeGroupName);
    		attributeGroup.put("description",attributeGroupDesc);


    		//create Attribute Group 
    		HashMap hmAG = new HashMap();
    		hmAG.put("name",attributeGroupName);
    		hmAG.put("description",attributeGroupDesc);


    		try{
    			JPO.invoke(context, "emxMultipleClassificationAttributeGroup", null, "create", JPO.packArgs(hmAG));
    		}catch(Exception e){
    			throw e;
    		}


    		// create attributes
    		Iterator itr = attributesData.iterator();
    		StringList attributeNameList = new StringList();
    		while(itr.hasNext()){
    			HashMap attribute = (HashMap)itr.next();

    			attribute.put("ownerType", "interface");
    			attribute.put("ownerName", attributeGroupName);

    			String qualifiedName = createAttribute(context, attribute);

    			attribute.put("qualifiedName", qualifiedName);
    			attributeNameList.add(qualifiedName);
    		}

    		ContextUtil.commitTransaction(context);

    		return attributeGroupName;

    	}catch(Exception e){
    		ContextUtil.abortTransaction(context);
    		throw new FrameworkException(e.toString());
    	}

    }
    
    /**
     * 
     * This method creates supplied attributes local to specified Attribute Group
     *  
     * @param context
     * @param args JPO packed args of a HashMap containing an attributeGroup as below
     *        1) "name" - String containing AttributeGroup name
     *        2) "attributes" - a MapList of Attributes, each map with following key-Value Pairs
     *                               1) "qualifiedName" - String containing qualified name of the Attribute
     *                               2) "name"          - String containing name of the Attribute
     *                               3) "type"          - String containing type of the Attribute
     *                               4) "default"       - String containing default value of the Attribute
     *                               5) "description"   - String containing description of the Attribute
     *                               6) "maxlength"     - String containing maxlength of the Attribute
     *                               7) "dimension"     - name of the dimension
     *                               8) "range"         - MapList of range Values
     *        
     * @throws Exception
     */
    public MapList createNewAttributesInAttributeGroup(Context context, String[] args) throws Exception {
    	
    	try{
    		MapList returnList = new MapList();
    		HashMap paramMap = JPO.unpackArgs(args);

    		ContextUtil.startTransaction(context, true);

    		Map attributeGroup = (Map)paramMap.get("attributeGroup");
    		String attributeGroupName = (String)attributeGroup.get("name");
    		if(UIUtil.isNullOrEmpty(attributeGroupName)){
    			attributeGroupName = generateAGName(context);
    		}
    		
    		MapList attributesData = (MapList)attributeGroup.get("attributes");
    		if(attributesData == null){
    			attributesData = new MapList();
    		}

    		// create attributes
    		Iterator itr = attributesData.iterator();
    		while(itr.hasNext()){
    			HashMap attribute = (HashMap)itr.next();

    			attribute.put("ownerType", "interface");
    			attribute.put("ownerName", attributeGroupName);

    			String qualifiedName = createAttribute(context, attribute);

    			attribute.put("qualifiedName", qualifiedName);
    			returnList.add(attribute);
    		}

    		ContextUtil.commitTransaction(context);

    		return returnList;

    	}catch(Exception e){
    		ContextUtil.abortTransaction(context);
    		throw new FrameworkException(e.toString());
    	}
    }
    
    
    /**
     * 
     * This method deletes local attributes in a specified Attribute Group
     *  
     * @param context
     * @param args JPO packed args of a HashMap containing an attributeGroup as below
     *        1) "name" - String containing AttributeGroup name
     *        2) "attributes" - a StringList of Attribute Names
     *        
     * @throws Exception
     */
    public MapList deleteLocalAttributesInAttributeGroup(Context context, String[] args) throws Exception {
    	
    	try{
    		MapList returnList = new MapList();
    		HashMap paramMap = JPO.unpackArgs(args);

    		Map attributeGroup = (Map)paramMap.get("attributeGroup");
    		String attributeGroupName = (String)attributeGroup.get("name");
    		if(UIUtil.isNullOrEmpty(attributeGroupName)){
    			throw new FrameworkException("Attribute Group Name is required");
    		}
    		
    		StringList attributesData = (StringList)attributeGroup.get("attributes");
    		if(attributesData == null){
    			attributesData = new StringList();
    		}

    		// delete attributes
    		Iterator itr = attributesData.iterator();
    		ContextUtil.startTransaction(context, true);
    		ContextUtil.pushContext(context);
    		while(itr.hasNext()){
    			String attributeName = (String)itr.next();
    			
    			MqlUtil.mqlCommand(context, "delete attribute $1",attributeGroupName+"."+attributeName);
    			
    		}
    		ContextUtil.popContext(context);
    		ContextUtil.commitTransaction(context);
    		return returnList;

    	}catch(Exception e){
    		ContextUtil.popContext(context);
    		ContextUtil.abortTransaction(context);
    		throw new FrameworkException(e.toString());
    	}
    }
    
    /**
     * 
     * This method modifies local attributes in a specified Attribute Group
     *  
     * @param context
     * @param args JPO packed args of a HashMap containing an attributeGroup as below
     *        1) "name" - String containing AttributeGroup name
     *        2) "attributes" - a MapList of Attributes, each map with following key-Value Pairs
     *                               1) "name"          - String containing name of the Attribute
     *                               2) "default"       - String containing new default value of the Attribute
     *                               3) "description"   - String containing new description of the Attribute
     *                               4) "maxlength"     - String containing new maxlength of the Attribute
     *                               5) "dimension"     - name of the new dimension
     *        
     * @throws Exception
     */
    public void modifyLocalAttributesInAttributeGroup(Context context, String[] args) throws Exception {
    	
    	try{
    		HashMap paramMap = JPO.unpackArgs(args);

    		Map attributeGroup = (Map)paramMap.get("attributeGroup");
    		String attributeGroupName = (String)attributeGroup.get("name");
    		if(UIUtil.isNullOrEmpty(attributeGroupName)){
    			throw new FrameworkException("Attribute Group Name is required");
    		}
    		
    		MapList attributesData = (MapList)attributeGroup.get("attributes");
    		if(attributesData == null){
    			attributesData = new MapList();
    		}

    		// modify attributes
    		Iterator itr = attributesData.iterator();
    		ContextUtil.startTransaction(context, true);
    		ContextUtil.pushContext(context);
    		while(itr.hasNext()){
    			Map attribute = (Map)itr.next();
    			String attributeName = (String)attribute.get("name");
    			StringBuffer cmd = new StringBuffer();
    			StringList cmdArgs = new StringList();
    			cmd.append("mod attribute $1");
    			cmdArgs.add(attributeGroupName+"."+attributeName);
    			
    			String defaultValue = (String)attribute.get("default");
    			if(defaultValue != null){
    				cmd.append(" default $"+(cmdArgs.size()+1));
        			cmdArgs.add(defaultValue);
    			}
    			
    			String description = (String)attribute.get("description");
    			if(description != null){
    				cmd.append(" description $"+(cmdArgs.size()+1));
        			cmdArgs.add(description);
    			}
    			
    			String maxlength = (String)attribute.get("maxlength");
    			if(maxlength != null){
    				cmd.append(" maxlength $"+(cmdArgs.size()+1));
        			cmdArgs.add(maxlength);
    			}
    			
    			String dimension = (String)attribute.get("dimension");
    			if(dimension != null){
    				cmd.append(" dimension $"+(cmdArgs.size()+1));
        			cmdArgs.add(dimension);
    			}
    			
    			MqlUtil.mqlCommand(context, cmd.toString(),cmdArgs);
    			
    		}
    		ContextUtil.popContext(context);
    		ContextUtil.commitTransaction(context);
    		
    	}catch(Exception e){
    		ContextUtil.popContext(context);
    		ContextUtil.abortTransaction(context);
    		throw new FrameworkException(e.toString());
    	}
    }
    
    
    
    /**
     * 
     * This method returns the Attribute Group Name for a Given class
     * 
     * Attribute group name will be of form "Attribute Group (Class_Type Class_Name)[(repeating number)]"
     * 
     * ie  attribute group name will be in the format 
     * "Attribute Group (Class_Type Class_Name)"
     * "Attribute Group (Class_Type Class_Name)(1)"
     * "Attribute Group (Class_Type Class_Name)(2)"
     * .. so on
     * 
     * 
     * @param context 
     * @param classId - Object Id of the Class Object
     * 
     *
     */
    
    protected String generateAGName(Context context) throws Exception{
    	
    	String attribute_eServiceNamePrefix = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_eServiceNamePrefix")+"].value";
    	String attribute_eServiceNameSuffix = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_eServiceNameSuffix")+"].value";
    	String attribute_eServiceRetryDelay = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_eServiceRetryDelay")+"].value";
    	String attribute_eServiceRetryCount = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_eServiceRetryCount")+"].value";
    	String attribute_eServiceNextNumber = "attribute["+PropertyUtil.getSchemaProperty(context, "attribute_eServiceNextNumber")+"].value";
    	String relationship_eServiceNumberGenerator = "from["+PropertyUtil.getSchemaProperty(context, "relationship_eServiceNumberGenerator")+"].to.id";
    	
    	StringList objectSelects = new StringList();
    	
    	objectSelects.add(attribute_eServiceNamePrefix);
    	objectSelects.add(attribute_eServiceNameSuffix);
    	objectSelects.add(attribute_eServiceRetryDelay);
    	objectSelects.add(attribute_eServiceRetryCount);
    	objectSelects.add(relationship_eServiceNumberGenerator);
    	
    	MapList agObjectGenerators = DomainObject.findObjects (context,
									                PropertyUtil.getSchemaProperty (context,"policy_eServiceObjectGenerator"),
									                "Attribute Group", // name pattern e.g. value Test Everything,dmc author
									                "*",
									                "*",
									                PropertyUtil.getSchemaProperty (context,"vault_eServiceAdministration"),
									                null,
									                false,
									                objectSelects);
    	
    	if(agObjectGenerators.size() != 1){
    		throw new Exception("Error in Generating Attribute Group Name");
    	}
    	
    	Map agObjectGenerator = (Map)agObjectGenerators.get(0);
    	
    	String eServiceNamePrefix = (String)agObjectGenerator.get(attribute_eServiceNamePrefix);
    	String eServiceNameSuffix = (String)agObjectGenerator.get(attribute_eServiceNameSuffix);
    	String eServiceRetryDelayStr = (String)agObjectGenerator.get(attribute_eServiceRetryDelay);
    	int eServiceRetryDelay = Integer.parseInt(eServiceRetryDelayStr);
    	String eServiceRetryCountStr = (String)agObjectGenerator.get(attribute_eServiceRetryCount);
    	int eServiceRetryCount = Integer.parseInt(eServiceRetryCountStr);
    	
    	String agNumberGeneratorId = (String)agObjectGenerator.get(relationship_eServiceNumberGenerator);
    	
    	int i=0;
    	String currentNumber = "";
    	DomainObject agNumberGenerator = new DomainObject(agNumberGeneratorId);
    	
    	String thisThreadName = Thread.currentThread().getName();
    	
    	try{
    		ContextUtil.pushContext(context);
    		ContextUtil.startTransaction(context, true);
    		while(i < eServiceRetryCount){
    	
    			// try to lock the Number Generator
    			try{
    				agNumberGenerator.lockForUpdate(context);
    			}catch(Exception e){
    	
    				Thread.sleep(eServiceRetryDelay);
    				i++;
    				continue;
    			}
    	
    			// get the nextNumber from Number Generator
    			currentNumber = agNumberGenerator.getInfo(context, attribute_eServiceNextNumber);
		
    			// increment and set the next number
    			int currentNumberInt = Integer.parseInt(currentNumber);
    			int nextNumberInt = currentNumberInt + 1;

    			String nextNumber = Integer.toString(nextNumberInt);

    			int noOfLeadingZeros = currentNumber.length() - nextNumber.length();
    			while(noOfLeadingZeros > 0){
    				nextNumber = "0"+nextNumber;
    				noOfLeadingZeros--;
    			}

    			agNumberGenerator.setAttributeValue(context, PropertyUtil.getSchemaProperty(context, "attribute_eServiceNextNumber"), nextNumber);
    			
    			//unlock the number generator
    			agNumberGenerator.unlock(context);
    			
    			break;
    		}
    		ContextUtil.commitTransaction(context);
    		ContextUtil.popContext(context);
    	}catch(Exception e){
    		ContextUtil.abortTransaction(context);
    		ContextUtil.popContext(context);
    		
    		throw e;
    	}
    	
    	String agName = eServiceNamePrefix+currentNumber+eServiceNameSuffix;
    	
    	return agName;
    }
    
    /**
     * This method creates attribute in the matrix DB
     * @param context
     * @param args JPO Packed args of a HashMap containing data as follows
     *        name - Name of the Attribute , can not be empty or null
     *        type - type of the attribute being created , allowed values are  "date","integer","string","real","boolean"
     *        default - default value for the attribute
     *        description - description for the attribute
     *        maxlength - maximum length of the attribute values , valid only for type = "string"
     *        dimension - name of the Dimension Object , valid only for type = "integer" or type = "real"
     *        ownerType - type of the owner object to create local attributes , allowed values are "Type", "Relationship", "interface"
     *        ownerName - name of the owner object , ie either a Type , a Relationship or an Interface Object name 
     *        range - MapList containing the range of values, where each Map represents a Range Value along with Operator as defined below
     *                operator - range value operator , allowed values are "=", "!=", "<", ">", "<=", ">=", "smatch", "!smatch", "match", "!match"
     *                value - value of the range Item
     *        
     * @return
     * @throws Exception
     */
    
    private String createAttribute(Context context,HashMap paramMap) throws Exception{
    	String name = (String)paramMap.get("name");
    	if(UIUtil.isNullOrEmpty(name)){
    		throw new Exception("Attribute name can not be empty");
    	}

    	String type         = (String)paramMap.get("type");
    	type = type.toLowerCase();
    	StringList allowedTypes = new StringList();
    	allowedTypes.add("date");
    	allowedTypes.add("timestamp");
    	allowedTypes.add("integer");
    	allowedTypes.add("string");
    	allowedTypes.add("real");
    	allowedTypes.add("boolean");
    	if(!allowedTypes.contains(type)){
    		throw new Exception("Invalid attribute type : "+type);
    	}
    	
    	String defaultValue = (String)paramMap.get("default");
    	String description  = (String)paramMap.get("description");
    	String maxlength    = (String)paramMap.get("maxlength");
    	MapList range       = (MapList)paramMap.get("range");
    	String dimension    = (String)paramMap.get("dimension");
    	
    	String ownerType    = (String)paramMap.get("ownerType");
    	String ownerName    = (String)paramMap.get("ownerName");
    	
    	StringList mqlCmdArgs = new StringList(); 
    	StringBuffer mqlCmd = new StringBuffer();
    	
    	int count = 1;
    	
    	mqlCmd.append("add attribute $"+(count++));
    	mqlCmdArgs.add(name);
    	
    	mqlCmd.append(" type $"+(count++));
    	mqlCmdArgs.add(type);
    	
    	if(UIUtil.isNotNullAndNotEmpty(defaultValue)){
	    	mqlCmd.append(" default $"+(count++));
	    	mqlCmdArgs.add(defaultValue);
    	}
    	
    	if(UIUtil.isNotNullAndNotEmpty(description)){
    	    mqlCmd.append(" description $"+(count++));
	    	mqlCmdArgs.add(description);
    	}
    	if(UIUtil.isNotNullAndNotEmpty(maxlength) && type.equalsIgnoreCase("string")){
        	mqlCmd.append(" maxlength $"+(count++));
        	mqlCmdArgs.add(maxlength);
    	}
    	
    	String attributeName = name;
		if(UIUtil.isNotNullAndNotEmpty(ownerType) && UIUtil.isNotNullAndNotEmpty(ownerName)){
			mqlCmd.append(" owner "+ownerType+" $"+(count++));
			mqlCmdArgs.add(ownerName);
			attributeName = ownerName+"."+name;
		}
    	
    	if(range != null){
    		StringList allowedRangeOperators = new StringList();
    		allowedRangeOperators.add("=");
    		allowedRangeOperators.add("!=");
    		allowedRangeOperators.add("<");
    		allowedRangeOperators.add(">");
    		allowedRangeOperators.add("<=");
    		allowedRangeOperators.add(">=");
    		allowedRangeOperators.add("smatch");
    		allowedRangeOperators.add("!smatch");
    		allowedRangeOperators.add("match");
    		allowedRangeOperators.add("!match");
    		
    		Iterator rangeIterator = range.iterator();
        	while(rangeIterator.hasNext()){
        		Map rangeElem = (Map)rangeIterator.next();
        		
        		String operator = (String)rangeElem.get("operator");
        		if(!allowedRangeOperators.contains(operator)){
        			throw new Exception("Invalid range operator : "+operator);
        		}
        		String value = (String)rangeElem.get("value");
        		
        		mqlCmd.append(" range "+operator+" $"+(count++));
            	mqlCmdArgs.add(value);
        		
        	}
    	}
    	
    	if(UIUtil.isNotNullAndNotEmpty(dimension) && (type.equals("integer") || type.equals("real"))){
    		mqlCmd.append(" dimension $"+(count++));
        	mqlCmdArgs.add(dimension);
    	}
    	
    	try{
    		ContextUtil.pushContext(context);
    		MqlUtil.mqlCommand(context, mqlCmd.toString(), mqlCmdArgs);
    		ContextUtil.popContext(context);
    		
    	}catch(Exception e){
    		ContextUtil.popContext(context);
    		throw e;
    	}
    	return attributeName;
    	
    }
    

}
