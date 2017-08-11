/**
 * emxTypeAhead.java
 *
 * Copyright (c) 2006-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.util.MxXMLUtils;

/**
 * This is the base class for all type ahead jpos that
 * may be called via the type ahead tag libarary.  This
 * class provides methods that will build a DOM Document
 * based on display and hidden field values that are 
 * appropriate for the field on the form.  This Document
 * will then be converted to xml and returned to the type
 * ahead javascript implementation in the web browser.
 *  
 * @author Mike Keirstead
 * @since AEF 10.6.SP2
 */
public class emxTypeAhead_mxJPO 
{ 
	private Document _document;
	private Element _values;
	private String _form;
	private String _field;
	private String _language;
	
	private Map _fieldMap;
	private Map _requestMap;
	private Map _fieldValuesMap;
	private Map _typeAheadMap;
	
	/**
	 *
	 */
	public emxTypeAhead_mxJPO ()
	{ 
	} 

	/**
	 *
	 */
	public emxTypeAhead_mxJPO (Context context, String[] args) throws Exception 
	{ 
		Map map = (Map) JPO.unpackArgs(args);
		_form = (String) map.get("form");
		_typeAheadMap = (Map) map.get("typeAheadMap");
		_field = (String) map.get("field");
		_language = (String) map.get("language");
		_fieldMap = (Map) map.get("fieldMap");
		_requestMap = (Map) map.get("requestMap");
		_fieldValuesMap = (Map)map.get("fieldValuesMap");
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
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TypeAhead", new Locale(languageStr));            
            throw new Exception(exMsg);
        }
        
        return (0);
	} 

	/**
	 * Sample call to retrieve information.
	 *
	 * @param context the matrix user context
	 * @param args one argument, the filter value
	 * @return xml representing the values 
	 * @since AEF 10.6.SP2
	 */
	public String test(Context context, String[] args)
	{
		String filter = args[0];
		
		addValue("first", "hidden_first");
		addValue("second", "hidden_second");
		addValue("third", "hidden_third");
		setAllDataSentAttribute(true);
		return (toXML());
	}
	
	/**
	 * Add a value to the list of values.
	 *
	 * @param display the display value
	 * @param hidden the underlying hidden value
	 * @since AEF 10.6.SP2
	 */
	protected void addValue(String display, String hidden)
	{
		// make sure we have a document to work with
		getDocument();
		
		// add new value to list of submitted
		Element value = new Element("v");
		
		Element elementDisplay = new Element("d");
		elementDisplay.setText(display);
		Element elementHidden = new Element("h");
		elementHidden.setText(hidden);
			
		value.addContent(elementDisplay);
		value.addContent(elementHidden);
		_values.addContent(value);
	
		// update value count
		_values.setAttribute("count", Integer.toString(_values.getChildren().size()));
	}
	
	protected void addValue(HashMap object, MapList columns)
	{
		// make sure we have a document to work with
		getDocument();
		
		// add new value to list of submitted
		Element value = new Element("v");
		
		String id     = UIUtil.getValue(object, "id");
		String name   = UIUtil.getValue(object, "name");
		
		value.setAttribute("id",id);
		value.setAttribute("name", name);
		
		for(int i = 0; i < columns.size(); i++)
		{
			HashMap column = (HashMap)columns.get(i);
			String cvalue  = UIUtil.getValue(object, UIComponent.getName(column));
			String columnType = UIComponent.getSetting(column, "Column Type");
			
			Element elementDisplay = new Element("c");
			if("programHTMLOutput".equals(columnType)){
				elementDisplay.setAttribute("isHTML", "true");
			}
			elementDisplay.setText(cvalue);		
			value.addContent(elementDisplay);
		}
		
		_values.addContent(value);
	
		// update value count
		if(UIUtil.isNullOrEmpty(id)) {
			_values.setAttribute("count", Integer.toString(0));
		}
		else {
		_values.setAttribute("count", Integer.toString(_values.getChildren().size()));
		}
	}

	/**
	 * Set the "all" attribute, that all of the data possible
	 * has been returned by this JPO.  This will prevent the calling
	 * of the JPO for other possible input values.
	 *
	 * @param allDataSent true to set the attribute to true
	 * @since AEF 10.6.SP2
	 */
	protected void setAllDataSentAttribute(boolean allDataSent)
	{
		// make sure we have a document to work with
		getDocument();
		
		// set the "all" attribute for the values node
		_values.setAttribute("all", allDataSent ? "true" : "false");
	}
	
	protected void setLimitReachedFlag(boolean searchLimitReached)
	{
		// make sure we have a document to work with
		getDocument();
		
		// set the "searchLimitReached" attribute for the values node
		_values.setAttribute("searchLimitReached", searchLimitReached ? "true" : "false");
	}

	/**
	 * Get the DOM Document to write into.  If it does
	 * not exist then create one and return it to the caller. 
	 *
	 * @returns the DOM Document object
	 * @since AEF 10.6.SP2
	 */
	private Document getDocument()
	{
		if (_document == null)
		{
			// build empty document
			Element root = new Element("form");
			root.setAttribute("name", _form);
			_document = new Document(root);
		
			Element field = new Element("field");
			field.setAttribute("name", _field);
			root.addContent(field);
			
			_values = new Element("values");
			_values.setAttribute("count", "0");
			_values.setAttribute("all", "false");
			field.addContent(_values);
		}
		
		return (_document);
	}

	/**
	 * Generate the XML to return to the caller.
	 *
	 * @returns the XML represent the values set.
	 * @since AEF 10.6.SP2
	 */
	protected String toXML()
	{
		String xml = null;
		
		// if the document is null, create an empty one
		if (_document == null)
		{
			getDocument();
		}

		try
		{
			XMLOutputter xmlOut = MxXMLUtils.getOutputter();
			xml = xmlOut.outputString(_document);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.out);
		}
			
		return (xml);
	}

	private HashMap _paramMap = null;
	
	protected String getQueryField(Context context, String field) throws Exception
	{
		if(field.indexOf("=") == -1 && field.indexOf(":") > -1 && field.lastIndexOf(":") == field.indexOf(":")) 
		{			
            StringList dynamicQuery  = FrameworkUtil.split(field, ":");
            String program           = (String)dynamicQuery.get(0);
            String function          = (String)dynamicQuery.get(1);
            
            HashMap inputMap = new HashMap();
            inputMap.put("requestMap",getRequestMap());
            inputMap.put("fieldMap", getFieldMap());
            inputMap.put("fieldValues",_fieldValuesMap);
            inputMap.put("typeAheadMap",_typeAheadMap); 
            
            field = (String) JPO.invoke(context, program, null, function, JPO.packArgs(inputMap), String.class);
		}
		
		return field;
	}
	
	protected HashMap getParamMap(Context context) throws Exception
	{
		if(_paramMap == null)
		{
			Map requestMap          = getRequestMap();
			Map fieldMap			= getFieldMap();
			String timeZone 		= UIUtil.getValue(requestMap, "timeZone");	
			Map settings            = UIComponent.getSettings(fieldMap);		
			String rangeUrl         = UIUtil.getValue(fieldMap, "range");
			HashMap paramMap 	    = UIUtil.toQueryMap(rangeUrl);
            String field            = UIUtil.getValue(paramMap, "field");
            String fieldProgram     = UIUtil.getValue(paramMap, "fieldProgram");
            if(!UIUtil.isNullOrEmpty(fieldProgram)){
                fieldProgram        = getQueryField(context, fieldProgram);
                field = field.length() > 0 ? (field + ":" + fieldProgram) : fieldProgram;
            }
			String defualt          = UIUtil.getValue(paramMap, "default");		

			String savedValuesLimit = UISearchUtil.getTypeAheadSavedValuesLimit(context, UIComponent.getSettings(getFieldMap()));
			savedValuesLimit 		= Integer.toString(Integer.parseInt(savedValuesLimit) + 1);
			String fieldSeperator   = UISearchUtil.getFieldSeperator(context, paramMap);
			String field_actual     = UISearchUtil.convertSymbolicNames(context, field , fieldSeperator);
			String default_actual   = UISearchUtil.convertSymbolicNames(context, defualt, fieldSeperator); 

			UIUtil.setValue(paramMap, "field_actual", field);
			UIUtil.setValue(paramMap, "field", field_actual);
			UIUtil.setValue(paramMap, "default", default_actual);
			UIUtil.setValue(paramMap, "default_actual", defualt);
			UIUtil.setValue(paramMap, "queryLimit", savedValuesLimit);
			UIUtil.setValue(paramMap, "firstTimeFormBased", "true");
			UIUtil.setValue(paramMap, "caseSensitiveSearch", "false");
			UIUtil.setValue(paramMap, "formSearch", "true");
			UIUtil.setValue(paramMap, "showWarning", "false");
			UIUtil.setValue(paramMap, "timeZone", timeZone);
			UIUtil.setValue(paramMap, "languageStr", getLanguage());
			UIUtil.setValue(paramMap, "fullTextSearchTimestamp", UIComponent.getTimeStamp());

			_paramMap = paramMap;
		}

		return _paramMap;
	}
	
	private MapList _columns = null;
	
	protected MapList getColumns(Context context) throws Exception
	{
		if(_columns == null)
		{
			HashMap requestMap            = getParamMap(context);
			UITableIndented indentedTable = new UITableIndented();
			String typeAheadTable 	      = UIUtil.getValue(requestMap, "typeAheadTable");
			MapList columns       		  = !UIUtil.isNullOrEmpty(typeAheadTable) ? indentedTable.getColumns(context, typeAheadTable, PersonUtil.getAssignments(context)) :
											UISearchUtil.getTypeAheadMappingColumns(context, UIComponent.getSettings(getFieldMap()));
			_columns					  = indentedTable.processColumns(context, new HashMap(), columns, requestMap);
		}
		
		return _columns;
	}
	
	protected MapList getColumns(Context context, MapList objectList) throws Exception
	{
		if(_columns == null)
		{
			HashMap requestMap            = getParamMap(context);
			UITableIndented indentedTable = new UITableIndented();
			String typeAheadTable 	      = UIUtil.getValue(requestMap, "typeAheadTable");
			MapList columns       		  = null;
			
			if(!UIUtil.isNullOrEmpty(typeAheadTable))
			{			
				columns = indentedTable.getColumns(context, typeAheadTable, PersonUtil.getAssignments(context));				
				HashMap xRequestMap = new HashMap(requestMap);				
				xRequestMap.put("userTable", Boolean.valueOf(indentedTable.isUserTable(context, typeAheadTable)));
				columns = indentedTable.getDynamicColumns(context, typeAheadTable, columns, xRequestMap, objectList);
			}
			else
			{
				columns = UISearchUtil.getTypeAheadMappingColumns(context, UIComponent.getSettings(getFieldMap()));
			}
			
			_columns	 = indentedTable.processColumns(context, new HashMap(), columns, requestMap);
		}
		
		return _columns;
	}

	/**
	 * Get the language passed in the constructor
	 *
	 * @returns the language string
	 * @since AEF 10.6.SP2
	 */
	protected String getLanguage()
	{
		return (_language);
	}
	
	protected Map getTypeAheadMap()
	{
		return (_typeAheadMap);
	}
	
	protected Map getFieldMap()
	{
		return (_fieldMap);
	}

    protected Map getFieldValuesMap()
    {
        return (_fieldValuesMap);
    }
	
	protected Map getRequestMap()
	{
		return (_requestMap);
	}
}
