
/**
 * emxTypeAheadFullSearchBase.java
 *
 * Copyright (c) 2006-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONObject;

/**
 * Provides type ahead search for Persons.
 *  
 * @author nqg/mj2
 * @since AEF R211
 */
public class emxTypeAheadFullSearchBase_mxJPO extends emxTypeAhead_mxJPO
{ 
	/**
	 *
	 */
	public emxTypeAheadFullSearchBase_mxJPO ()
	{ 
	} 

	/**
	 *
	 */
	public emxTypeAheadFullSearchBase_mxJPO (Context context, String[] args) throws Exception 
	{ 
		super(context, args);
	} 

	/**
	 *
	 */
	public int mxMain(Context context, String []args) throws Exception 
	{ 
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));            
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TypeAheadFullSearch", new Locale(languageStr));            
            throw new Exception(exMsg);
        }
        
        return (0);
	} 


	public String getSearchResults(Context context, String[] args) throws Exception
	{
		doSearch(context, args);		
		return (toXML());
	}
	
	public String getTypeSearchResults(Context context, String[] args) throws Exception
	{
		String charTyped       = (args != null) ? (String) JPO.unpackArgs(args) : "*";	
		String rangeUrl        = UIUtil.getValue(getFieldMap(), "range");
		HashMap paramMap 	   = UIUtil.toQueryMap(rangeUrl);
		//Inclusion and Exclusion list are encoded in adjustTypeField (UIFormCommon.java) if typeField!=null and should be decoded here inorder to separate the types passed in the exclusion or inclusion list using ",".
		String excludedList    = XSSUtil.decodeFromURL(UIUtil.getValue(paramMap, "ExclusionList"));
		String includedList    = XSSUtil.decodeFromURL(UIUtil.getValue(paramMap, "InclusionList"));
		String observeHidden   = UIUtil.getValue(paramMap, "ObserveHidden");
		String abstractTypes   = UIUtil.getValue(paramMap, "SelectAbstractTypes");
		
		boolean bObserveHidden = true;
		boolean deep 		   = true;
		if("false".equalsIgnoreCase(observeHidden))
		{
		   bObserveHidden = false;
		}
		
		
		boolean babstractTypes 	= false;
		if("true".equalsIgnoreCase(abstractTypes))
		{
			babstractTypes = true;
		}
		
		MapList columns    = getColumns(context);
		MapList types      = UISearchUtil.getTypeAheadSearchTypes(context, charTyped, excludedList, includedList, getLanguage(), deep, bObserveHidden, babstractTypes);
		MapList typeList   = toTypeMapList(types, columns);
        if(typeList.size() < 1) 
        {           
            String languageStr = getLanguage();
            String displayText = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", 
            					 new Locale(languageStr), "emxFramework.TypeAheadFullTextSearch.NoResultsFound");
            
            Map noObject = new HashMap();
            noObject.put("id", "");
            noObject.put("name", displayText);
            
            typeList = new MapList();
            typeList.add(noObject);
            
            Map column = new HashMap();
            column.put("name", "name");
            columns = new MapList();
            columns.add(column);            
        }
        
		HashMap requestMap = (HashMap)getParamMap(context);
		String typeAheadTable = UIUtil.getValue(requestMap, "typeAheadTable");
		if( ! UIUtil.isNullOrEmpty(typeAheadTable)) {
			sortObjects(context, typeList);
		}

		generateXML(context, typeList, columns);
		
		return (toXML());
	}
	
	protected MapList toTypeMapList(MapList typeList, MapList columns)
	{
		MapList typeMapList   = new MapList();
		Iterator itr 	      = typeList.iterator();
		
        while (itr.hasNext())
        {
        	Map typeMap  = (Map) itr.next(); 
			HashMap item = new HashMap();
			item.put("id", UIUtil.getValue(typeMap,"actual"));
       		String type  = UIUtil.getValue(typeMap,"display");
			for(int i = 0; i < columns.size(); i++)
			{
				HashMap column = (HashMap)columns.get(i);
				item.put(UIComponent.getName(column), type);
			}
			typeMapList.add(item);
  		}
        
        return typeMapList;
	}
	
	public String getQueryField(Context context, String[] args) throws Exception
	{			
		String fieldProgram = UIUtil.getValue(getTypeAheadMap(),"fieldProgram");
		String field 		= getQueryField(context, fieldProgram);	
		JSONObject json = new JSONObject();
		json.put("field", field);		
		return json.toString();
	}
	
	private MapList toObjects(Context context, MapList objects) throws Exception
	{
		String bol_array[] = new String[objects.size()];
        Map elementMap;
        StringList sl_bus  = new StringList();
        sl_bus.addElement("name");
        sl_bus.addElement("id");
        
        for (int i = 0; i < objects.size(); i++) 
        {
            elementMap   = (Map) objects.get(i);
            bol_array[i] = (String) elementMap.get("id");
        }

       MapList results = FrameworkUtil.toMapList(BusinessObject.getSelectBusinessObjectData(context, bol_array, sl_bus));
       
       return results;
    }
		
	private void doSearch(Context context,  String[] args) throws Exception
	{
		MapList objects  = search(context, args);
		MapList columns 	 = getColumns(context);
		HashMap requestMap = (HashMap)getParamMap(context);
		if(objects.size() < 1) 
		{			
			String languageStr = getLanguage();
			String displayText = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", 
									new Locale(languageStr), "emxFramework.TypeAheadFullTextSearch.NoResultsFound"); 

			Map noObject = new HashMap();
			noObject.put("id", "");
			noObject.put("name", displayText);
			
			objects = new MapList();
			objects.add(noObject);
			
			Map column = new HashMap();
			column.put("name", "name");
			columns = new MapList();
			columns.add(column);			
		}
		
		String typeAheadTable = UIUtil.getValue(requestMap, "typeAheadTable");
		if( ! UIUtil.isNullOrEmpty(typeAheadTable)) {
		sortObjects(context, objects);
		}

		generateXML(context, objects, columns);
	}
	
	/**
	 * This method is used to sort maplist based on urlParameter - sortColumnName, sortDirection
	 * The default value for sortDirection is ascecnding
	 * Method added for - IR-154100
	 * 
	 * @param context
	 * @param objects
	 * @throws Exception
	 */
	private void sortObjects(Context context, MapList objects) throws Exception
	{
		HashMap requestMap = (HashMap)getParamMap(context);
		
		String sortColumnNames = (String)requestMap.get("sortColumnName");
		String sortDirections = (String)requestMap.get("sortDirection");
		String sortTypes = "";

		StringList sortColumnList = new StringList();
		
		if( ! UIUtil.isNullOrEmpty(sortColumnNames)){
			sortColumnList = FrameworkUtil.split(sortColumnNames, ",");

		if(UIUtil.isNullOrEmpty(sortDirections)  && sortColumnList.size() > 0)
		{
			sortDirections = "";
			sortColumnList = FrameworkUtil.split(sortColumnNames, ",");
			for(int i = 0 ; i < sortColumnList.size() ; i++)
			{
				sortDirections = sortDirections + "ascending" + ","; 
			}
		}

		if(sortColumnList.size() > 0)
		{
			UITableIndented indentedTable = new UITableIndented();
			String typeAheadTable = UIUtil.getValue(requestMap, "typeAheadTable");
			MapList columns = indentedTable.getColumns(context, typeAheadTable, PersonUtil.getAssignments(context));

			for(int i = 0 ; i < columns.size() ; i++)
			{
				HashMap mpColumn = (HashMap)columns.get(i);
				String columnName = (String)mpColumn.get("name");
				if(sortColumnList.contains(columnName)){
					sortTypes = sortTypes + getSortType(mpColumn) + ",";
				}
			}
		}

		if(sortDirections.endsWith(",")){
			sortDirections = sortDirections.substring(0,sortDirections.lastIndexOf(","));
        }
		if(sortTypes.endsWith(",")){
			sortTypes = sortTypes.substring(0,sortTypes.lastIndexOf(","));
        }

		if( ! UIUtil.isNullOrEmpty(sortColumnNames) && ! UIUtil.isNullOrEmpty(sortDirections) && ! UIUtil.isNullOrEmpty(sortTypes) )
		{
			objects.sortStructure(sortColumnNames, sortDirections, sortTypes);
		}
	}
	}

        /**
	 * This method is used to get "Sort Type" setting present on the column of TypeAhead table
	 * and return the type string, which will further be used for selection sorting algorithm.
	 * Method added for - IR-154100
	 * 
	 * @param sortColumMap
	 * @return
	 */
	private String getSortType(HashMap sortColumMap){
		final String SETTING_SORT_TYPE = "Sort Type";
		final String SORTTYPE_STRING = "string";
		final String SORTTYPE_REAL = "real";
		final String SETTING_SORT_PROGRAM = "Sort Program";
		final String SORTTYPE_DATE = "date";
		final String SORTTYPE_INTEGER = "integer";
		final String SORTTYPE_PROGRAM = "program";

		String sortType = UITableCommon.getSetting(sortColumMap, SETTING_SORT_TYPE);
        if("".equals(sortType))
        {
			sortType = SORTTYPE_STRING;
        }
        String sortProgram = "";
        if (UITableCommon.isSortAlpha(sortColumMap)) 
        {
            sortType = SORTTYPE_STRING;
        }
        else if (UITableCommon.isSortNumeric(sortColumMap)) 
        {
            sortType = SORTTYPE_REAL;
        } 
        else if (UITableCommon.isSortOther(sortColumMap)) 
        {
            sortProgram = UITableCommon.getSetting(sortColumMap, SETTING_SORT_PROGRAM);
            if(UITableCommon.isAssociatedWithDimension(sortColumMap) && !UITableCommon.isAlphanumericField(sortColumMap))
            {
                sortProgram = "";
            }
            String sortTypeSetting = UITableCommon.getSetting(sortColumMap, SETTING_SORT_TYPE);
            if(UITableCommon.isAssociatedWithDimension(sortColumMap) && !UITableCommon.isAlphanumericField(sortColumMap))
            {
                sortTypeSetting = SORTTYPE_REAL;
            }

            if (sortTypeSetting.equals(SORTTYPE_DATE) || sortTypeSetting.equals(SORTTYPE_INTEGER) || sortTypeSetting.equals(SORTTYPE_REAL)) 
            {
                sortType = sortTypeSetting;
            } 
            else if (sortProgram.length() > 0)
            {
                sortType = SORTTYPE_PROGRAM;
            }
        }
        else if(UITableCommon.isAssociatedWithDimension(sortColumMap) && !UITableCommon.isAlphanumericField(sortColumMap))
        {
                sortType = SORTTYPE_REAL;
        }

        return sortType;
	}

	private void generateXML(Context context, MapList objects, MapList columns)throws Exception
	{
		HashMap map;
		
		boolean searchLimitReached = false;
		String savedValuesLimit = UISearchUtil.getTypeAheadSavedValuesLimit(context, UIComponent.getSettings(getFieldMap()));
		if(objects.size() > Integer.parseInt(savedValuesLimit)) {
			searchLimitReached = true;
			objects.remove(Integer.parseInt(savedValuesLimit));
		}
		setLimitReachedFlag(searchLimitReached);
		
    	Iterator itr 		= objects.iterator();
        
        while (itr.hasNext())
        {
			map    = (HashMap) itr.next();
			String id = UIUtil.getValue(map, "id");
			String name = UIUtil.getValue(map, "name");
			try {
				if(UIUtil.isNullOrEmpty(name)) {
					map.put("name", new DomainObject(id).getInfo(context,"name"));
				}
			} catch (Exception e) {
				map.put("name", "");
			}
       		addValue(map, columns);
  		}        
	}

	private MapList getColumnValues(Context context,MapList columns, MapList objects, HashMap requestMap)throws FrameworkException
	{		
		UITableIndented indentedTable = new UITableIndented();
		MapList columnValues = indentedTable.getCellValues(context, objects, columns, requestMap);
		return columnValues;
	}
	
	public MapList search(Context context, String[] args) throws Exception
	{
		MapList objects    = searchObjects(context, args);		
		MapList columns    = getColumns(context, objects);
		HashMap requestMap = getParamMap(context);		

		if((getRequestMap().get("ImageData")) instanceof String){
			requestMap.put("ImageData", (String)getRequestMap().get("ImageData"));
		}else{
			HashMap ImageData = (HashMap) getRequestMap().get("ImageData");
		if(ImageData != null && ! ImageData.isEmpty()) {
			requestMap.put("ImageData", ImageData);
			}
		}

		objects			   = getColumnValues(context, columns, objects, requestMap);		
		return objects;
	}
	
	
	public MapList searchObjects(Context context, String[] args)
		throws Exception
	{
		String charTyped   = (args != null) ? (String) JPO.unpackArgs(args) : "*";
		
		Map requestMap          = getRequestMap();
		Map fieldMap			= getFieldMap();
		MapList results 	    = new MapList();		
		Map settings            = UIComponent.getSettings(fieldMap);
		HashMap paramMap        = getParamMap(context);
		String field     		= UIUtil.getValue(paramMap, "field");
	    JSONObject filters      = UISearchUtil.getFilters(context, field, paramMap);	    	    
	    String typeAheadMapping = UISearchUtil.getTypeAheadMapping(context, settings);
	    
	    if(!UIUtil.isNullOrEmpty(charTyped))
	    {	    	
	    	charTyped=charTyped.replaceAll("\\*+","\\*");
	    	if(!charTyped.startsWith("*"))
	    		charTyped  = "*" + charTyped;
	    	if(!charTyped.endsWith("*"))
	    		charTyped  =  charTyped + "*";
	    } else
	    {
	    	charTyped = "*";
	    }
	    
        StringList typeAheadMappingList = FrameworkUtil.split(typeAheadMapping, ",");
      
        for(int i = 0; i < typeAheadMappingList.size(); i++)
        {
    	   UISearchUtil.addToFilters(context, filters, typeAheadMappingList.get(i).toString(), charTyped, "Equals");
        }
        
        if(typeAheadMappingList.size() > 1)
        {
    	   UIUtil.setValue(paramMap, "Type Ahead Mapping", typeAheadMapping);
        }
	    
	    UIUtil.setValue(paramMap, "ftsFilters", filters.toString());
        Map xRequest = new HashMap(paramMap);
        xRequest.put("objectId", (String)requestMap.get("objectId"));
        xRequest.put("KeyValueMap", getFieldValuesMap());
        xRequest.put("requestMap", requestMap);
        xRequest.put("fieldMap", fieldMap);

        //JOE - BEGIN(Fix for IR-117673V6R2012x)
        String typeAheadVal = (String)settings.get("TypeAhead");
        if(typeAheadVal != null && "true".equalsIgnoreCase(typeAheadVal)){
        	Map typeAheadMap = getTypeAheadMap();
        	xRequest.put("typeAheadMap", typeAheadMap);
        }
        //JOE - END

	    results = (MapList) JPO.invoke(context, "emxAEFFullSearch", null, "search", JPO.packArgs(xRequest), MapList.class);
  		
	    if(results.size() > 0){
	    	results.remove(0);
	    	if(results.size() > 0 && results.get(0) instanceof Integer){
	    		results.remove(0);
	    	}
	    }
	    return results;
	}
}


