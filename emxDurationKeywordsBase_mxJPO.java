/*
**	emxDurationKeywordsBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.Dimension;
import matrix.db.JPO;
import matrix.db.Unit;
import matrix.db.UnitList;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.DependencyRelationship;
import com.matrixone.apps.common.TaskDateRollup;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.DurationKeyword;
import com.matrixone.apps.program.DurationKeywords;
import com.matrixone.apps.program.DurationKeywordsUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.Task;


/**
 * @author WQY
 *
 */
public class emxDurationKeywordsBase_mxJPO 
{
	private static final String STRING_WBSTASK = "WBSTask";
	private static final String STRING_LAGTIME = "SlackTime";
	private static final String STRING_ALL = "All";
	
    /**
     * Constructor 
     * 
     * @param context The Matrix Context object
     * @param args The arguments array
     * @throws Exception if operation fails
     */
    public emxDurationKeywordsBase_mxJPO(Context context, String[] args) throws Exception 
    {
        
    }
   
    /**
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    public int mxMain(Context context, String[] args) throws MatrixException 
    {
        throw new MatrixException("This JPO cannot be run stand alone.");
    }

    
    /**
     * Get table data of Resource Utilization Report.
     *
     * @param context The Matrix Context object
     * @param args Packed program and request maps for the table
     * @return MapList containing all table data
     * @throws MatrixException if the operation fails
     * @since PRG R207
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableDurationKeywordsData(Context context, String[] args)
            throws MatrixException 
    {      
        try 
        {            
            if (context == null) 
            {
                throw new IllegalArgumentException("context");
            }
            MapList mlDurationKeywordsDataList  = new MapList();
            Map programMap = (Map) JPO.unpackArgs(args);
            String strObjectId = (String)programMap.get("objectId");
            
            /*
             * The Non - project member has access to the task assigned to it but 
             * not the Project space.
             * In order to access the Duration Keywords the context is temporarily 
             * pushed to Super user and 
             * popped again when its use is over.
             * */
            
			boolean accessFlag = DurationKeywordsUtil.hasProjectAccess(context, strObjectId, "PROJECT_MEMBER");
			if(!accessFlag)
			{
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
			}
            strObjectId = DurationKeywordsUtil.getProjectId(context, strObjectId);
			String strFunctionMode = (String)programMap.get("FunctionMode");
			DurationKeywords durationKeywords = new DurationKeywords(context, strObjectId);
			if(!accessFlag)
			{
				ContextUtil.popContext(context);
			}			
			DurationKeyword [] durationKeyword = durationKeywords.getDurationKeywords();
            if(null!=durationKeyword)
            {
            	for(int i=0; i<durationKeyword.length; i++)
            	{
            		Map keywordNewValueMap = new HashMap();
            		keywordNewValueMap.put(DurationKeyword.ATTRIBUTE_NAME, durationKeyword[i].getName());
            		keywordNewValueMap.put(DurationKeyword.ATTRIBUTE_TYPE, durationKeyword[i].getType());
            		keywordNewValueMap.put(DurationKeyword.ATTRIBUTE_DURATION, durationKeyword[i].getDuration());
            		keywordNewValueMap.put(DurationKeyword.ATTRIBUTE_DURATIONUNIT, durationKeyword[i].getUnit());
            		keywordNewValueMap.put(DurationKeyword.ATTRIBUTE_DESCRIPTION, durationKeyword[i].getDescription());
            		if(!"editall".equals(strFunctionMode))
            		{
            			keywordNewValueMap.put(DomainConstants.SELECT_ID, durationKeyword[i].getName());
            		}
            		mlDurationKeywordsDataList.add(keywordNewValueMap);
            	}
            }
            return mlDurationKeywordsDataList;
        } 
        catch (IllegalArgumentException iaexp) 
        {
            iaexp.printStackTrace();
            throw new MatrixException(iaexp);
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnNameData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");
            MapList mlObjectListModify = new MapList();
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                vecResult.add(mapObjectInfo.get(DurationKeyword.ATTRIBUTE_NAME));
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnTypeData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            i18nNow loc = new i18nNow();
            HashMap paramMap = (HashMap) programMap.get("paramList");
    	    String strLanguage = (String) paramMap.get("languageStr");
    	    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                String strDurationMap = (String)mapObjectInfo.get(DurationKeyword.ATTRIBUTE_TYPE);
                String strDurationMapLang = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.DurationKeyword."+XSSUtil.encodeForHTML(context,strDurationMap)+"");
                vecResult.add (strDurationMapLang);
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
   public Vector getColumnDurationData(Context context, String[] args) throws Exception 
	{
		
		
		Vector vecResult = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList mlObjectList = (MapList) programMap.get("objectList");
		MapList mlObjectListModify = new MapList();
		Map mapObjectInfo = null;
		for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
		{
			mapObjectInfo = (Map) itrTableRows.next();
			vecResult.add(mapObjectInfo.get(DurationKeyword.ATTRIBUTE_DURATION).toString());
		}
		return vecResult;
	}

	
	/**
	 * @param mapObjectInfo
	 * @param objCount
	 * @param unitList
	 * @return
	 */
    	private static StringBuffer getDurationUnitField(Context context, String objCount, String strDuration, String strDurationUnit,
			UnitList unitList) throws MatrixException {
		Unit unit;
		StringBuffer strDurationUnitField = new StringBuffer(100);
		strDurationUnitField.append("<input type=\"text\" size=\"10\" name=\"Duration"+objCount+"\" id=\"Duration"+objCount+"\" value=\""+XSSUtil.encodeForHTML(context,strDuration)+"\"/>");
		strDurationUnitField.append("<select name=\"DurationUnit"+objCount+"\" size=\"1\">");
		for (int i = 0; i < unitList.size(); i++) 
		{
		    unit = (Unit)unitList.get(i);
		    String lable=ProgramCentralConstants.EMPTY_STRING;
		    String unitLabel=unit.getLabel();
		    if("Days".equals(unitLabel))
			{
				lable =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Days", context.getSession().getLanguage()) ;
			}else if("Hours".equals(unitLabel))
			{
				lable =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Hours", context.getSession().getLanguage()) ;
			}
		    strDurationUnitField.append("<option value=\"").append(unit.getName()).append("\"")
		    .append((strDurationUnit.equals(unit.getName()))?" selected='selected'":"").append(">")
		            .append(lable)
		            .append("</option>");
		}
		strDurationUnitField.append("</select>");
		return strDurationUnitField;
	}

	public static UnitList getUnitList(Context context, String strAttributeName) 
	{
		UnitList unitList = null;
		AttributeType attrType = new AttributeType(strAttributeName);
		try
		{
		    Dimension dimension = attrType.getDimension(context);
		    if (dimension == null) {
		        unitList = new UnitList();
		    }
		    else {
		        unitList = dimension.getUnits(context);
		    }
		}
		catch (Exception e) 
		{
		    unitList = new UnitList();
		}
		return unitList;
	}
    
    /**
     * Gets the data for the column "<name>" for table "<name>"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnDescriptionData(Context context, String[] args) throws Exception 
    {
        try 
        {
            // Create result vector
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList mlObjectList = (MapList) programMap.get("objectList");
            
            Map mapObjectInfo = null;
            for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
            {
                mapObjectInfo = (Map) itrTableRows.next();
                vecResult.add (mapObjectInfo.get(DurationKeyword.ATTRIBUTE_DESCRIPTION));
            }
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * @param context
     * @param args
     * @throws Exception
     */
    public void createDurationKeywords(Context context, String[] args)  throws Exception 
    {
    	try 
    	{
    		Map paramMap = (Map) JPO.unpackArgs(args);
    		String strObjectId = (String)paramMap.get("objectId");
    		DurationKeyword durationKeyword = (DurationKeyword)paramMap.get("durationKeyword");
    		DurationKeywords durationKeywords = new DurationKeywords(context, strObjectId);
			//Added for special character.(Passed context)    		
			durationKeywords.addDurationKeyword(context,durationKeyword);
    		durationKeywords.setProjectDurationKeywords(context);
    	} catch (Exception exp) {
    		exp.printStackTrace();
    		throw exp;
    	}
    } 
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static Map getDurationKeywordsTypes(Context context, String[] args) throws Exception
    {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    HashMap paramMap = (HashMap) programMap.get("paramMap");
	    i18nNow loc = new i18nNow();
	    String strLanguage = (String) paramMap.get("languageStr");
	    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
	    HashMap tempMap = new HashMap();
	    StringList fieldRangeValues = new StringList();
	    StringList fieldDisplayRangeValues = new StringList();
	    String strDurationMapKey = EnoviaResourceBundle.getProperty(context, "emxProgramCentral.DurationKeyword.DurationMap");
	    StringList slDurationMap = FrameworkUtil.split(strDurationMapKey, ",");
	    if(null!=slDurationMap)
	    {
	    	for(int i=0; i<slDurationMap.size(); i++)
	    	{
	    		String strDurationMap = (String)slDurationMap.get(i);
			    String strDurationMapLang = (String)loc.GetString(RESOURCE_BUNDLE, strLanguage, "emxProgramCentral.DurationKeyword."+strDurationMap+"");
		        fieldRangeValues.addElement(strDurationMap);
			    fieldDisplayRangeValues.addElement(strDurationMapLang);
	    	}
	    	tempMap.put("field_choices", fieldRangeValues);
			tempMap.put("field_display_choices", fieldDisplayRangeValues);
	    }
	    return tempMap;
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static String getDurationUnitFieldValue(Context context, String[] args) throws Exception
    {
    	StringBuffer strDurationUnitField = new StringBuffer(100);
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strFunctionMode = (String)requestMap.get("FunctionMode");
	    String strDuration = "";
	    String strDurationUnit = "";
	    Unit unit = null;
        UnitList unitList = null;
        unitList = getUnitList(context,DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION);
	    if("edit".equals(strFunctionMode))
	    {
	    	DurationKeyword[] sDurationKeywords = getDurationKeywordsByNameFromRequestMap(
					context, requestMap);
            strDuration = String.valueOf(sDurationKeywords[0].getDuration());
            strDurationUnit = sDurationKeywords[0].getUnit();
	    }
    	String taskEstimatedDuration = PropertyUtil.getSchemaProperty(context, "attribute_TaskEstimatedDuration");
    	strDurationUnitField = getDurationUnitField(context,"", strDuration, strDurationUnit,unitList);
    	return strDurationUnitField.toString();
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static String getNameFieldValue(Context context, String[] args) throws Exception
    {
    	StringBuffer strNameField = new StringBuffer(100);
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strFunctionMode = (String)requestMap.get("FunctionMode");
	    String strName = "";
	    if("edit".equals(strFunctionMode))
	    {
	    	DurationKeyword[] sDurationKeywords = getDurationKeywordsByNameFromRequestMap(
					context, requestMap);
	    	strName = sDurationKeywords[0].getName();
	    }
	   //ADDED:hp5:10/12/10:IR-031515V6R2012:PRG
	    
	   /* strNameField.append("<input type=\"text\" size=\"10\" name=\"Name\" id=\"Name\" value=\""+strName+"\"");
	    if("edit".equals(strFunctionMode))
	    {
	    	strNameField.append("readonly=\"readonly\"");
	    }
	    strNameField.append(">");*/
	    
	    if("edit".equals(strFunctionMode))
	    {
	    	strNameField.append("<div id=\"Name\" name=\"Name\">"+XSSUtil.encodeForHTML(context,strName)+"</div>").append("<input type=\"hidden\" size=\"10\" name=\"Name\" id=\"Name\" value=\""+XSSUtil.encodeForHTML(context,strName)+"\"");
	    }
	    else
	    {
	    	strNameField.append("<input type=\"text\" size=\"10\" name=\"Name\" id=\"Name\" value=\""+XSSUtil.encodeForHTML(context,strName)+"\"");
	    strNameField.append(">");
	    }
	    //END:hp5
	    
	    if("create".equals(strFunctionMode))
	    {
	    	String strObjectId = (String)requestMap.get("objectId");
	    	DurationKeywords durationKeywords = new DurationKeywords(context,strObjectId);
	    	DurationKeyword[] durationKeyword = durationKeywords.getDurationKeywords();
	    	strNameField.append("<INPUT TYPE=\"hidden\" ID=\"validNameCount\" NAME=\"validNameCount\" value=\""+durationKeyword.length+"\">");
	    	for(int i=0;i<durationKeyword.length;i++)
	    	{
	    		strNameField.append("<INPUT TYPE=\"hidden\" ID=\"validateName"+i+"\" NAME=\"validateName"+i+"\" value=\""+durationKeyword[i].getName()+"\">");
	    	}
	    }
		return strNameField.toString();
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static String getDurationMapFieldValue(Context context, String[] args) throws Exception
    {
    	StringBuffer strDurationMapField = new StringBuffer(100);
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strFunctionMode = (String)requestMap.get("FunctionMode");
	    String strDurationMap = "";
	    if("edit".equals(strFunctionMode))
	    {
	    	DurationKeyword[] sDurationKeywords = getDurationKeywordsByNameFromRequestMap(
					context, requestMap);
	    	strDurationMap = sDurationKeywords[0].getType();
	    }
	    Map getDurationMapRange = getDurationKeywordsTypes(context, args);
	    StringList slFieldChoices = (StringList)getDurationMapRange.get("field_choices");
	    StringList slFieldChoicesDisplay = (StringList)getDurationMapRange.get("field_display_choices");
	    
	    strDurationMapField.append("<select name=\"DurationMap\" size=\"1\" id=\"DurationMapId\">");
		for (int i = 0; i < slFieldChoices.size(); i++) {
            String strDurationMapFields = (String)slFieldChoices.get(i);
            String strDurationMapFieldsDisplay = (String)slFieldChoicesDisplay.get(i);
            strDurationMapField.append("<option value=\"").append(strDurationMapFields).append("\"")
            .append((strDurationMap.equals(strDurationMapFields))?"selected":"")
            .append(">").append(strDurationMapFieldsDisplay).append("</option>");
        }
		strDurationMapField.append("</select>");
		return strDurationMapField.toString();
    }
    
    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static String getDescriptionFieldValue(Context context, String[] args) throws Exception
    {
    	StringBuffer strDescriptionField = new StringBuffer(100);
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    String strFunctionMode = (String)requestMap.get("FunctionMode");
	    String strDescription = "";
	    if("edit".equals(strFunctionMode))
	    {
	    	DurationKeyword[] sDurationKeywords = getDurationKeywordsByNameFromRequestMap(
					context, requestMap);
	    	strDescription =  sDurationKeywords[0].getDescription();
	    }
	    strDescriptionField.append("<TEXTAREA name=\"Description\" rows=\"5\" cols=\"25\">"+XSSUtil.encodeForHTML(context,strDescription)+"</TEXTAREA>");
		return strDescriptionField.toString();
    }

	/**
	 * @param context
	 * @param requestMap
	 * @return
	 * @throws MatrixException
	 */
	private static DurationKeyword[] getDurationKeywordsByNameFromRequestMap(
			Context context, Map requestMap) throws MatrixException {
		String strKeywordName = (String)requestMap.get("DurationKeywordName");
		String strObjectId = (String)requestMap.get("objectId");
		strObjectId = DurationKeywordsUtil.getProjectId(context, strObjectId);
		DurationKeywords durationKeywords = new DurationKeywords(context, strObjectId);
		//Added for special character.(Passed context)		
		DurationKeyword [] sDurationKeywords = durationKeywords.getDurationKeywords(context,DurationKeyword.ATTRIBUTE_NAME, strKeywordName);
		return sDurationKeywords;
	}
    
    /**
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateMassDurationKeywords(Context context, String[] args) throws Exception 
    {
    	try 
    	{
    		Map paramMap = (Map) JPO.unpackArgs(args);
    		String strObjectId = (String)paramMap.get("objectId");
    		DurationKeyword[] beforeDurationKeyword = (DurationKeyword[])paramMap.get("oldDurationKeyword"); 
    		DurationKeyword[] newDurationKeyword = (DurationKeyword[])paramMap.get("newDurationKeyword");  
    		if(beforeDurationKeyword !=null && newDurationKeyword !=null){
    			DurationKeywords oldDurationKeywords = new DurationKeywords(context, strObjectId);
    			DurationKeywords durationKeywords = new DurationKeywords(context, strObjectId);
    			durationKeywords.addDurationKeyword(context,beforeDurationKeyword,newDurationKeyword); 
    			durationKeywords.setProjectDurationKeywords(context);
    			for(int i=0; i<newDurationKeyword.length; i++){
    				DurationKeyword newDurationKeywords = newDurationKeyword[i];
    				checkAndRollUpDuration(context, strObjectId, oldDurationKeywords, newDurationKeywords);
    			}
    		}else{
    		DurationKeyword[] durationKeyword = (DurationKeyword[])paramMap.get("durationKeyword");
    		DurationKeywords oldDurationKeywords = new DurationKeywords(context, strObjectId);
    		DurationKeywords durationKeywords = new DurationKeywords(context, strObjectId);
			//Added for special character.(Passed context)    		
			durationKeywords.addDurationKeyword(context,durationKeyword);
        	durationKeywords.setProjectDurationKeywords(context);
        		for(int i=0; i<durationKeyword.length; i++){
            		DurationKeyword newDurationKeywords = durationKeyword[i];
            		checkAndRollUpDuration(context, strObjectId, oldDurationKeywords, newDurationKeywords);
            	}
        	}
    	} 
    	catch (Exception exp) 
    	{
    		exp.printStackTrace();
    		throw exp;
    	}
    }
    
    /**
     * @param context
     * @param args
     * @throws Exception
     */
    public void removeDurationKeywordsValue(Context context, String[] args) throws Exception 
    {
    	try 
    	{
    		Map paramMap = (Map) JPO.unpackArgs(args);
    		String strObjectId = (String)paramMap.get("objectId");
    		DurationKeyword[] sDurationKeyword = (DurationKeyword[])paramMap.get("durationKeyword");
    		DurationKeywords durationKeywords = new DurationKeywords(context, strObjectId); 
    		for(int i=0; i<sDurationKeyword.length; i++)
        	{
        		removeWBSDurationKeyword(context, strObjectId, sDurationKeyword[i]);
        	}
			//Added for special character.(Passed context)    		
			durationKeywords.removeDurationKeyword(context,sDurationKeyword);
        	durationKeywords.setProjectDurationKeywords(context);
    	} 
    	catch (Exception exp) 
    	{
    		exp.printStackTrace();
    		throw exp;
    	}
    }
    
    /**
     * @param context
     * @param strObjectId
     * @param durationKeywords
     * @param newDurationKeyword
     * @throws Exception
     */
    private void removeWBSDurationKeyword(Context context, String strObjectId, DurationKeyword durationKeywords) throws Exception 
    {
    	DurationKeyword oldDurationKeywords = durationKeywords;
    	String strName = oldDurationKeywords.getName();
    	String strOldType = oldDurationKeywords.getType();
    	boolean removeTaskValue = false;
    	boolean removeDependencyValue = false;
    	if(strOldType.equals(STRING_WBSTASK))
    	{
    		removeTaskValue = true;
    	}
    	else if(strOldType.equals(STRING_LAGTIME))
    	{
    		removeDependencyValue = true;
    	}
    	else if(strOldType.equals(STRING_ALL))
    	{
    		removeTaskValue = true;
    		removeDependencyValue = true;
    	}
    	if(removeTaskValue)
    	{
    		StringList slTaskIdList = getAllTaskAssociatedWithDurationKeyword(context, strObjectId, strName);
    		if(removeTaskValue)
    		{
    			for(int i=0; i<slTaskIdList.size(); i++)
    			{
    				String strTaskId = (String)slTaskIdList.get(i);
    				removeDurationKeywordFromTask(context, strTaskId);
    			}
    		}
    	}
    	if(removeDependencyValue)
    	{
    		Map dependencyTaskConnectionIdMap = getAllDependencyTaskAssociatedWithDurationKeyword(context, strObjectId, strName);
    		StringList slDependencyTaskIdList = new StringList();
    		if(null!=dependencyTaskConnectionIdMap.get("DependencyTaskIdList"))
    		{
    			slDependencyTaskIdList = (StringList)dependencyTaskConnectionIdMap.get("DependencyTaskIdList");
    		}
    		StringList slConnectionIdList = new StringList();
    		if(null!=dependencyTaskConnectionIdMap.get("ConnectionIdList"))
    		{
    			slConnectionIdList = (StringList)dependencyTaskConnectionIdMap.get("ConnectionIdList");
    		}
    		if(removeDependencyValue)
    		{
    			for(int i=0; i<slConnectionIdList.size(); i++)
    			{
    				String strConnectionId = (String)slConnectionIdList.get(i);
    				removeDurationKeywordFromDependancy(context, strConnectionId);
    			}
    		}
    	}
    }
    
    /**
     * @param context
     * @param strObjectId
     * @param durationKeywords
     * @param newDurationKeyword
     * @throws Exception
     */
    private void checkAndRollUpDuration(Context context, String strObjectId, DurationKeywords durationKeywords, DurationKeyword newDurationKeyword) throws Exception 
    {
    	String strName = newDurationKeyword.getName();
		String strNewType = newDurationKeyword.getType();
		double nNewDuration = newDurationKeyword.getDuration();
		String strNewDurationUnit = newDurationKeyword.getUnit();
		//Added for special character.(Passed context)    	
		DurationKeyword[] oldDurationKeywords = durationKeywords.getDurationKeywords(context,DurationKeyword.ATTRIBUTE_NAME, strName);
    	if(null!= oldDurationKeywords && oldDurationKeywords.length !=0)
    	{
	    	String strOldType = oldDurationKeywords[0].getType();
			double nOldDuration = oldDurationKeywords[0].getDuration();
			String strOldDurationUnit = oldDurationKeywords[0].getUnit();
	    	boolean updateTaskValue = false;
	    	boolean updateDependencyValue = false;
	    	boolean removeTaskValue = false;
	    	boolean removeDependencyValue = false;
	    	if(strNewType.equals(STRING_WBSTASK))
	    	{
	    		if(strNewType.equals(strOldType))
	    		{
	    			updateTaskValue = true;
	    		}
	    		else
	    		{
	    			removeDependencyValue = true;
	    		}
	    	}
	    	else if(strNewType.equals(STRING_LAGTIME))
	    	{
	    		if(strNewType.equals(strOldType))
	    		{
	    			updateDependencyValue = true;
	    		}
	    		else
	    		{
	    			removeTaskValue = true;
	    		}
	    	}
	    	else if(strNewType.equals(STRING_ALL))
	    	{
	    		if(strNewType.equals(strOldType))
	    		{
	    			updateTaskValue = true;
	    			updateDependencyValue = true;
	    		}
	    	}
	    	if(updateTaskValue || removeTaskValue)
	    	{
	    		StringList slTaskIdList = getAllTaskAssociatedWithDurationKeyword(context, strObjectId, strName);
	    		if(updateTaskValue)
		    	{
			    	if((nNewDuration!=nOldDuration || !strNewDurationUnit.equals(strOldDurationUnit)))
			    	{
			    		String strDuration = nNewDuration+" "+strNewDurationUnit;
			    		for(int i=0; i<slTaskIdList.size(); i++)
			    		{
			    			String strTaskId = (String)slTaskIdList.get(i);
			    			rollupTaskDuration(context, strTaskId, strDuration);
			    		}
			    	}
		    	}
		    	if(removeTaskValue)
		    	{
		    		for(int i=0; i<slTaskIdList.size(); i++)
		    		{
		    			String strTaskId = (String)slTaskIdList.get(i);
		    			removeDurationKeywordFromTask(context, strTaskId);
		    		}
		    	}
	    	}
	    	if(updateDependencyValue || removeDependencyValue)
	    	{
	    		Map dependencyTaskConnectionIdMap = getAllDependencyTaskAssociatedWithDurationKeyword(context, strObjectId, strName);
		    	StringList slDependencyTaskIdList = new StringList();
		    	if(null!=dependencyTaskConnectionIdMap.get("DependencyTaskIdList"))
		    	{
		    		slDependencyTaskIdList = (StringList)dependencyTaskConnectionIdMap.get("DependencyTaskIdList");
		    	}
		    	StringList slConnectionIdList = new StringList();
		    	if(null!=dependencyTaskConnectionIdMap.get("ConnectionIdList"))
		    	{
		    		slConnectionIdList = (StringList)dependencyTaskConnectionIdMap.get("ConnectionIdList");
		    	}
//Added: 11-Mar-10:di1:R209:PRG Bug :031412
				StringList slDependencyTypeList = new StringList();
				if(null!=dependencyTaskConnectionIdMap.get("DependencyTypeList"))
				{
					slDependencyTypeList = (StringList)dependencyTaskConnectionIdMap.get("DependencyTypeList");
				}
//end: 11-Mar-10:di1:R209:PRG Bug :031412
		    	if(removeDependencyValue)
		    	{
		    		for(int i=0; i<slConnectionIdList.size(); i++)
		    		{
		    			String strConnectionId = (String)slConnectionIdList.get(i);
		    			removeDurationKeywordFromDependancy(context, strConnectionId);
		    		}
		    	}
			    if(updateDependencyValue)
		    	{
			    	for(int i=0; i<slDependencyTaskIdList.size(); i++)
		    		{
			    		String strLagDuration = nNewDuration+" "+strNewDurationUnit;
			    		String strConnectionId = (String)slConnectionIdList.get(i);
						String strTaskId = (String)slDependencyTaskIdList.get(i);
						String strDepenedencyType = (String)slDependencyTypeList.get(i);
						setLagDurationFromDependancy(context, strConnectionId, strLagDuration,strTaskId,strDepenedencyType);
		    		}
		    	}
	    	}
    	}
    }
    
    /**
     * @param context
     * @param strTaskId
     * @param newAttrValue
     * @throws MatrixException
     */
    private void rollupTaskDuration(Context context, String strTaskId, String newAttrValue) throws MatrixException
    {
    	rollupTaskDuration(context, strTaskId, newAttrValue, "", false);
    }
    /**
     * @param context
     * @param strTaskId
     * @param newAttrValue
     * @throws MatrixException
     */
    private void rollupTaskDuration(Context context, String strTaskId, String newAttrValue,String strNewDurationKeyword,boolean updateDurationKeyword) throws MatrixException
    {
    	com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
                DomainConstants.TYPE_TASK, "PROGRAM");
    	StringList selectList = new StringList(3);
        selectList.addElement(task.SELECT_NAME);
        selectList.addElement(task.SELECT_TYPE);
        selectList.addElement(task.SELECT_CURRENT);
        selectList.addElement(task.SELECT_HAS_SUBTASK);
        task.setId(strTaskId);

        Map mp = task.getInfo(context,selectList);

        String strCurrentState = (String)mp.get(task.SELECT_CURRENT);
        String strName  = (String)mp.get(task.SELECT_NAME);
        String strType  = (String)mp.get(task.SELECT_TYPE);
        String strHasSubTask = (String)mp.get(task.SELECT_HAS_SUBTASK);
        String strOldDurationKeyword =task.getAttributeValue(context,task.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
        String [] args = new String[1];
        args[0] = strTaskId;
        try 
        {
        	//Modified:29-Mar-10:s4e:R209:PRG:Bug:031403
			//Modified to remove "Duration keyword" while editing "Duration Keyword" when the task is in "Review" or "Complete" state
			emxTask_mxJPO taskBase = new emxTask_mxJPO(context, args);
			if("true".equalsIgnoreCase(strHasSubTask))
			{
				removeDurationKeywordFromTask(context, strTaskId);
			}
			else if(taskBase.checkEditable(0,strCurrentState,strHasSubTask))
			{
				if(updateDurationKeyword)
				{
					task.setAttributeValue(context,task.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD,strNewDurationKeyword);
				}
				//taskBase.updateEstimatedDate(context, strTaskId, "durationS",newAttrValue);
				
				Map<String,String> taskInfoMap = new HashMap<String,String>();
				taskInfoMap.put("durationS",newAttrValue);

				Map<String,Map<String,String>> taskMap 	= new HashMap<String,Map<String,String>>();
				taskMap.put(strTaskId,taskInfoMap);

				try {
					ContextUtil.startTransaction(context,true);
					String strMsg = Task.updateDates(context,taskMap);

					if(!"false".equalsIgnoreCase(strMsg)){
						ContextUtil.abortTransaction(context);
						throw new Exception(strMsg);
					}
					ContextUtil.commitTransaction(context);
				}catch(Exception e){
					e.printStackTrace();
					ContextUtil.abortTransaction(context);
				}
				
			}
			else if(!taskBase.checkEditable(0,strCurrentState,strHasSubTask))
			{
				//Modified:16-Nov-10:s4e:R209:PRG:IR-049564V6R2012 
				if(("".equalsIgnoreCase(strNewDurationKeyword))||(strNewDurationKeyword.equals(strOldDurationKeyword)))
				{
				removeDurationKeywordFromTask(context, strTaskId);
			}		
				//Modified:16-Nov-10:s4e:R209:PRG:IR-049564V6R2012 
			}		
			//End:29-Mar-10:s4e:R209:PRG:Bug:031403
		} 
        catch (Exception e) 
        {
        	throw new MatrixException(e);
		}
    	
    }
    
    /**
     * @param context
     * @param strTaskId
     * @throws MatrixException
     */
    private void removeDurationKeywordFromTask(Context context, String strTaskId) throws MatrixException
    {
    	DomainObject domainTaskObject = DomainObject.newInstance(context,strTaskId);
    	domainTaskObject.setAttributeValue(context, DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
    }
    
    /**
     * @param context
     * @param strTaskId
     * @throws MatrixException
     */
    private void removeDurationKeywordFromDependancy(Context context, String strConnectionId) throws MatrixException
    {
    	DomainRelationship domainRelObject = DomainRelationship.newInstance(context,strConnectionId);
    	domainRelObject.setAttributeValue(context, DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
    }
    /**
     * @param context
     * @param strTaskId
     * @throws MatrixException
     */
//Added: 11-Mar-10:di1:R209:PRG Bug :031412
	private void setLagDurationFromDependancy(Context context, String strConnectionId, String strLagDuration,String strTaskId,String strDependencyType) throws MatrixException
	{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
				DomainConstants.TYPE_TASK, "PROGRAM");
		StringList selectList = new StringList(3);
		selectList.addElement(task.SELECT_NAME);
		selectList.addElement(task.SELECT_TYPE);
		selectList.addElement(task.SELECT_CURRENT);
		selectList.addElement(task.SELECT_HAS_SUBTASK);
		task.setId(strTaskId);

		Map mp = task.getInfo(context,selectList);

		String strCurrentState = (String)mp.get(task.SELECT_CURRENT);
		String strName  = (String)mp.get(task.SELECT_NAME);
		String strType  = (String)mp.get(task.SELECT_TYPE);
		String strHasSubTask = (String)mp.get(task.SELECT_HAS_SUBTASK);
		String [] args = new String[1];
		args[0] = strTaskId;
		try 
		{
			emxTask_mxJPO taskBase = new emxTask_mxJPO(context, args);
			if(isValidDependency(context, strCurrentState, strDependencyType))
			{
				setLagDurationFromDependancy(context, strConnectionId,strLagDuration);
				TaskDateRollup rollup = new TaskDateRollup(strTaskId);
				rollup.validateTask(context);
			}
			else
			{
				String [] argsDependancy = new String[2];
				argsDependancy[0] = strConnectionId;
				argsDependancy[1] = strTaskId;
				triggerModifyDependencyDurationKeyword(context,argsDependancy);
			}
		} 
		catch (Exception e) 
		{
			throw new MatrixException(e);
		}
	}

	/**
	 * @param context
	 * @param strConnectionId
	 * @param strLagDuration
	 * @throws FrameworkException
	 */
	private void setLagDurationFromDependancy(Context context,String strConnectionId, String strLagDuration)throws FrameworkException 
    {
    	DomainRelationship domainRelObject = DomainRelationship.newInstance(context,strConnectionId);
    	domainRelObject.setAttributeValue(context, DependencyRelationship.ATTRIBUTE_LAG_TIME, strLagDuration);
    }
//end of Addition: 11-Mar-10:di1:R209:PRG Bug :031412	
    /**
     * @param context
     * @param strProjectId
     * @param strDurationKeywordName
     * @return
     * @throws MatrixException
     */
    private StringList getAllTaskAssociatedWithDurationKeyword(Context context, String strProjectId, String strDurationKeywordName) throws MatrixException
    {
    	DomainObject domainProjectObject = DomainObject.newInstance(context,strProjectId);
    	StringList slTaskIdList = new StringList();
    	
    	final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "attribute[" + DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD + "]";
    	String strTypePattern = DomainConstants.TYPE_TASK_MANAGEMENT;
    	String strRelPattern = DomainConstants.RELATIONSHIP_SUBTASK;
    	StringList slBusSelect = new StringList();
    	slBusSelect.add(DomainConstants.SELECT_ID);
    	slBusSelect.add(DomainConstants.SELECT_NAME);
    	slBusSelect.add(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);

    	StringList slRelSelect = new StringList();
    	short recurseToLevel = 0;
    	//String strBusWhere = ""+SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD+"smatch\""+strDurationKeywordName+"\"";
    	String strBusWhere = "";
    	String strRelWhere = "";
    	
    	MapList mlContextResourcePools = domainProjectObject.getRelatedObjects(context,
    			strRelPattern, //pattern to match relationships
    			strTypePattern, //pattern to match types
    			slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
    			slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
    			false, //get To relationships
    			true, //get From relationships
    			recurseToLevel, //the number of levels to expand, 0 equals expand all.
    			strBusWhere, //where clause to apply to objects, can be empty ""
    			strRelWhere,0); //where clause to apply to relationship, can be empty ""
        
    	for (Iterator iterator = mlContextResourcePools.iterator(); iterator.hasNext();) 
    	{
			Map mapObject = (Map) iterator.next();
			String strDurationKeyword = (String)mapObject.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
			if(null!=strDurationKeyword && !"".equals(strDurationKeyword) && !"null".equals(strDurationKeyword))
			{
				if(strDurationKeyword.equals(strDurationKeywordName))
				{
					slTaskIdList.add((String)mapObject.get(DomainConstants.SELECT_ID));
				}
			}
			
		}
    	return slTaskIdList;
    }
    

    /**
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static Map getKeywordNameWBSFieldValue(Context context, String[] args) throws Exception
    {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    HashMap requestMap = (HashMap) programMap.get("requestMap");
	    HashMap paramMap = (HashMap) programMap.get("paramMap");
	    String   strObjectId       	= (String) paramMap.get("objectId");
	    i18nNow loc = new i18nNow();
	    String strLanguage = (String) paramMap.get("languageStr");
	    final String RESOURCE_BUNDLE = "emxProgramCentralStringResource";
	    HashMap tempMap = new HashMap();
	    StringList fieldRangeValues = new StringList();
	    StringList fieldDisplayRangeValues = new StringList();
	    fieldRangeValues.addElement("");
	    fieldDisplayRangeValues.addElement("");
	    DurationKeyword [] durationKeywords = DurationKeywordsUtil.getDurationKeywordsValueForWBS(context, strObjectId);  
	    if(null!=durationKeywords)
	    {
	    	for(int i=0; i<durationKeywords.length; i++)
	    	{
	    		String strDurationName = (String)durationKeywords[i].getName();
		        fieldRangeValues.addElement(strDurationName);
			    fieldDisplayRangeValues.addElement(strDurationName);
	    	}
	    	tempMap.put("field_choices", fieldRangeValues);
			tempMap.put("field_display_choices", fieldDisplayRangeValues);
	    }
	    return tempMap;
    }
    
    
    /**
     * @param context
     * @param args
     * @throws MatrixException
     */
    public void updateDurationKeywordWBSFieldValue(Context context, String[] args) throws MatrixException
    {
    	try {
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			HashMap columnMap = (HashMap) inputMap.get("columnMap");
			HashMap requestMap = (HashMap) inputMap.get("requestMap");
			String strProjectId = (String)requestMap.get("objectId");
			HashMap paramMap = (HashMap) inputMap.get("paramMap");
			String strObjectId = (String) paramMap.get("objectId");
			String newDurationKeyword = (String) paramMap.get("New Value");

			com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
			task.setId(strObjectId);
			if(null!=newDurationKeyword && !"".equals(newDurationKeyword))
			{
				Map requestValueMap = new HashMap();
				requestValueMap.put("DurationKeywordName",newDurationKeyword);
				strProjectId = DurationKeywordsUtil.getProjectId(context, strProjectId);
				String strParentId = DurationKeywordsUtil.getProjectId(context, strObjectId);
				requestValueMap.put("objectId",strParentId);
				if(strParentId.equals(strProjectId))
				{
    				// [MODIFIED::Feb 18, 2011:S4E:R211:IR-087362V6R2012::Start] 
    				//Added pushpop block because Task Assignee don't have access on project
    				//and method below "getDurationKeywordsByNameFromRequestMap" is used to get the duration keyword associated with Project and
    				//method "rollupTaskDuration" is used to rollup WBS dates which also modifies project dates.
    				ContextUtil.pushContext(context, PropertyUtil
    						.getSchemaProperty(context, "person_UserAgent"),
    						DomainConstants.EMPTY_STRING,
    						DomainConstants.EMPTY_STRING);
    				try {
					DurationKeyword [] durationKeywords = getDurationKeywordsByNameFromRequestMap(context, requestValueMap);
					String strDuration = durationKeywords[0].getDuration()+" "+durationKeywords[0].getUnit();
					rollupTaskDuration(context, strObjectId, strDuration,newDurationKeyword, true);
    				} finally {
    						ContextUtil.popContext(context);
    					}
    				// [MODIFIED::Feb 18, 2011:S4E:R211:IR-087362V6R2012::End]
				}
			}
			else
			{
				final String SELECT_PREFERRED_DURATION_UNIT = "attribute["+ DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION + "].inputunit";
				StringList slSelectList = new StringList();
				slSelectList.add(SELECT_PREFERRED_DURATION_UNIT);
				Map mp = task.getInfo(context,slSelectList);
				String strPreferredDurationUnit = (String)mp.get(SELECT_PREFERRED_DURATION_UNIT);

				// [MODIFIED::Feb 18, 2011:S4E:R211:IR-087362V6R2012::Start] 
				//Added pushpop block because Task Assignee don't have access on project
				//method "rollupTaskDuration" is used to rollup WBS dates which also modifies project dates.
    			ContextUtil.pushContext(context, PropertyUtil
    					.getSchemaProperty(context, "person_UserAgent"),
    					DomainConstants.EMPTY_STRING,
    					DomainConstants.EMPTY_STRING);
    			try {
				rollupTaskDuration(context, strObjectId, 0+" "+strPreferredDurationUnit,"", true);
    			} finally {
    					ContextUtil.popContext(context);
    				}
    			// [MODIFIED::Feb 18, 2011:S4E:R211:IR-087362V6R2012::End] 
			}
		} 
    	catch (Exception e) 
		{
    		throw new MatrixException(e);
		}
    }
    
    /**
     * @param context
     * @param strProjectId
     * @param strDurationKeywordName
     * @return
     * @throws MatrixException
     */
    private Map getAllDependencyTaskAssociatedWithDurationKeyword(Context context, String strProjectId, String strDurationKeywordName) throws MatrixException
    {
    	DomainObject domainProjectObject = DomainObject.newInstance(context,strProjectId);
    	StringList slDependencyTaskIdList = new StringList();
    	StringList slConnectionIdList = new StringList();
		StringList slDependencyTypeList = new StringList();
    	final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "from["+DomainConstants.RELATIONSHIP_DEPENDENCY+"].attribute["+DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD+"]";
    	final String SELECT_TASK_ID = DomainConstants.SELECT_ID;
    	final String SELECT_CONNECTION_ID = "from["+DomainConstants.RELATIONSHIP_DEPENDENCY+"].id";
		final String SELECT_DEPENDENCY_TYPE = "from["+DomainConstants.RELATIONSHIP_DEPENDENCY+"].attribute["+DomainConstants.ATTRIBUTE_DEPENDENCY_TYPE+"]";
    	String strTypePattern = DomainConstants.TYPE_TASK_MANAGEMENT;
    	String strRelPattern = DomainConstants.RELATIONSHIP_SUBTASK;
    	StringList slBusSelect = new StringList();
    	slBusSelect.add(SELECT_TASK_ID);
    	slBusSelect.add(SELECT_CONNECTION_ID);
    	slBusSelect.add(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
		slBusSelect.add(SELECT_DEPENDENCY_TYPE);
    	StringList slRelSelect = new StringList();
    	short recurseToLevel = 0;
    	//String strBusWhere = ""+SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD+"smatch\""+strDurationKeywordName+"\"";
    	String strBusWhere = "";
    	String strRelWhere = "";
    	
    	MapList mlContextResourcePools = domainProjectObject.getRelatedObjects(context,
    			strRelPattern, //pattern to match relationships
    			strTypePattern, //pattern to match types
    			slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
    			slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
    			false, //get To relationships
    			true, //get From relationships
    			recurseToLevel, //the number of levels to expand, 0 equals expand all.
    			strBusWhere, //where clause to apply to objects, can be empty ""
    			strRelWhere,0); //where clause to apply to relationship, can be empty ""
        
    	for (Iterator iterator = mlContextResourcePools.iterator(); iterator.hasNext();) 
    	{
			Map mapObject = (Map) iterator.next();
			Object objDurationKeyword = (Object)mapObject.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
			String objTaskId = (String)mapObject.get(SELECT_TASK_ID);
	        Object objConnectionId = (Object)mapObject.get(SELECT_CONNECTION_ID);
			Object objDependancyType = (Object)mapObject.get(SELECT_DEPENDENCY_TYPE);
	        StringList slDurationKeyword = new StringList();
	        StringList slConnectionId = new StringList();
			StringList slDependancyType = new StringList();
	        //check whether the dependency list has one or many ids
	        if (objConnectionId instanceof String){
	        	slDurationKeyword.add((String)objDurationKeyword);
	        	slConnectionId.add((String)objConnectionId);
				slDependancyType.add(objDependancyType);
	        } else if (objConnectionId instanceof StringList) {
	        	slDurationKeyword = (StringList) objDurationKeyword;
	        	slConnectionId = (StringList) objConnectionId;
	        }
	        for(int i=0;i<slConnectionId.size();i++)
	        {
	        	String strDurationKeyword = (String)slDurationKeyword.get(i);
	        	if(null!=strDurationKeyword && !"".equals(strDurationKeyword) && !"null".equals(strDurationKeyword))
	        	{
	        		if(strDurationKeyword.equals(strDurationKeywordName))
	        		{
	        			slDependencyTaskIdList.add((String)mapObject.get(SELECT_TASK_ID));
	        			slConnectionIdList.add((String)slConnectionId.get(i));
						slDependencyTypeList.add((String)slDependancyType.get(i));
	        		}
	        	}
	        }
		}
    	Map dependencyTaskConnectionIdMap = new HashMap();
    	dependencyTaskConnectionIdMap.put("DependencyTaskIdList",slDependencyTaskIdList);
    	dependencyTaskConnectionIdMap.put("ConnectionIdList",slConnectionIdList);
		dependencyTaskConnectionIdMap.put("DependencyTypeList",slDependencyTypeList);
    	return dependencyTaskConnectionIdMap;
    }
    
    /**
     * Checks if the Duration keyword toolbar actions are allowed to context user.
     * @param context the ENOVIA Context object.
     * @param args request arguments
     * @return true if context user is allowed to work on Duration Keyword feature. 
     * @throws Exception if operation fails.
     */
    public boolean hasAccessDurationKeywordView(Context context, String args[]) throws Exception
     {
         HashMap inputMap = (HashMap)JPO.unpackArgs(args);
         String strObjectId = (String)inputMap.get("objectId");
         com.matrixone.apps.program.ProjectSpace projectSpace=
             (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                     DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
         boolean blAccess = false;
         if (ProgramCentralUtil.isNotNullString(strObjectId)){
        	DomainObject project = DomainObject.newInstance(context, strObjectId);
        	blAccess = project.checkAccess(context, (short)AccessConstants.cModify);
         }
//Old security implementation         
//         if ((strObjectId != null) && !strObjectId.equals(""))
//         {
//        	${CLASS:emxProjectSpace} project =
//                 new ${CLASS:emxProjectSpace}(strObjectId);
//            String args1[]=new String[] {"PROJECT_MEMBER"};     
//            blAccess = project.hasAccess(context, args1); 
//         	if (blAccess)
//         	{
//         		strObjectId = DurationKeywordsUtil.getProjectId(context, strObjectId);
//         		projectSpace.setId(strObjectId);
//                String strAccess = projectSpace.getAccess(context);
//                if (!strAccess.equals(ProgramCentralConstants.PROJECT_ROLE_PROJECT_LEAD) &&
//                         !strAccess.equals(ProgramCentralConstants.PROJECT_ROLE_PROJECT_OWNER))
//                {
//                	blAccess = false;
//                }
//         	}
//         }
         return blAccess;
     }
//Added: 11-Mar-10:di1:R209:PRG Bug :031412
	public int triggerModifyDependencyDurationKeyword(Context context, String[] args) throws MatrixException
	{		
		try 
		{
			final String SELECT_LAG_TIME_UNIT = "attribute[" + DependencyRelationship.ATTRIBUTE_LAG_TIME + "].inputunit";
			final String SELECT_LAG_TIME = "attribute[" + DependencyRelationship.ATTRIBUTE_LAG_TIME + "]";
			final String SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD = "attribute[" + DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD + "]";
			String strRelobjectId = args[0];
			String strTaskobjectId = args[1];
			StringList slSelectList = new StringList();
			slSelectList.add(SELECT_LAG_TIME);
			slSelectList.add(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
			slSelectList.add(SELECT_LAG_TIME_UNIT);
			MapList mlInfo = DomainRelationship.getInfo(context, new String[]{strRelobjectId}, slSelectList);			
			String strDurationKeyword= null;
			String strDuration= null;
			String strPreferredDurationUnit=null;
			//strPreferredDurationUnit= 
			if (mlInfo.size() > 0) {
				 Map mapInfo = (Map)mlInfo.get(0);				 
				 strDurationKeyword = (String)mapInfo.get(SELECT_ATTRIBUTE_ESTIMATED_DURATION_KEYWORD);
				 strDuration  = (String)mapInfo.get(SELECT_LAG_TIME);				
				 strPreferredDurationUnit = (String)mapInfo.get(SELECT_LAG_TIME_UNIT);
			}
			if(null!=strDurationKeyword && !"".equals(strDurationKeyword) && !"null".equals(strDurationKeyword))
			{
				Unit unit = null;
				UnitList unitList = new UnitList();				
				unitList = emxDurationKeywords_mxJPO.getUnitList(context,DependencyRelationship.ATTRIBUTE_LAG_TIME);
				Map unitsLabelMap = new HashMap(); 
				if(null!=unitList && unitList.size()>0)
				{
					for (int i = 0; i < unitList.size(); i++) 
					{
						unit = (Unit)unitList.get(i);
						unitsLabelMap.put(unit.getName(), unit);
					}
				}
				unit = (Unit) unitsLabelMap.get(strPreferredDurationUnit);
				if (unit != null)
				{
					String strProjectId = DurationKeywordsUtil.getProjectId(context,strTaskobjectId);
					DurationKeywords durationKeywords = new DurationKeywords(context, strProjectId);
					//Added for special character.(Passed context)					
					DurationKeyword [] sDurationKeywords = durationKeywords.getDurationKeywords(context,DurationKeyword.ATTRIBUTE_NAME, strDurationKeyword);
					double nTaskEstDuration = Task.parseToDouble(unit.denormalize(strDuration));
					
					String strTaskEstDurationUnit = unit.getName();
					if(null!=sDurationKeywords)
					{
						double nDuration = sDurationKeywords[0].getDuration();
						String strDurationUnit = sDurationKeywords[0].getUnit();						
						if(nDuration!=nTaskEstDuration)
						{							
							DomainRelationship.setAttributeValue(context, strRelobjectId ,DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
						}
						else if(!strDurationUnit.equals(strTaskEstDurationUnit))
						{
							DomainRelationship.setAttributeValue(context, strRelobjectId ,DomainConstants.ATTRIBUTE_ESTIMATED_DURATION_KEYWORD, "");
						}
					}
				}
			}
			return 0;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	private boolean isValidDependency(Context context, String strDependentTaskState,String strDependencyType) throws FrameworkException
	{   
		final String POLICY_PROJECT_TASK_STATE_ACTIVE = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_PROJECT_TASK, "state_Active");
		final String POLICY_PROJECT_TASK_STATE_REVIEW = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_PROJECT_TASK, "state_Review");
		final String POLICY_PROJECT_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_PROJECT_TASK, "state_Complete");

		if(POLICY_PROJECT_TASK_STATE_COMPLETE.equals(strDependentTaskState))
		{
			return false;
		}   
		else
		{
			if(POLICY_PROJECT_TASK_STATE_ACTIVE.equals(strDependentTaskState)||POLICY_PROJECT_TASK_STATE_REVIEW.equals(strDependentTaskState))
			{
				if(null!= strDependencyType && (Task.FINISH_TO_START.equals(strDependencyType)||Task.START_TO_START.equals(strDependencyType)))
				{
					return false;
				} 
			}
		}
		return true;
	}


	public void updateDurationMap(Context context, String[] args) throws Exception
	{
		Map inputMap 		= JPO.unpackArgs(args);
		Map requestValueMap = (HashMap) inputMap.get("requestMap");
		Map columnMap 		= (Map) inputMap.get("columnMap");
		Map paramMap 		= (Map) inputMap.get("paramMap");

		String projectId 	= (String) requestValueMap.get("parentOID");
		String keywordName 	= (String) paramMap.get("objectId");
		String DurationMap 	= (String) paramMap.get("New Value");
		
		DurationKeywords durationKeywords = new DurationKeywords(context, projectId);
		DurationKeyword [] durationKeyword = durationKeywords.getDurationKeywords();
		
		if(durationKeyword != null && durationKeyword.length>0){
			DurationKeyword [] newDurationKeyword = new DurationKeyword[durationKeyword.length];
			
			for(int i=0; i<durationKeyword.length; i++){
				String sDKName 		= durationKeyword[i].getName();
				String sDKType 		= durationKeyword[i].getType();
				String sDKUnit 		= durationKeyword[i].getUnit();
				double dDKDuration 	= durationKeyword[i].getDuration();
				String sDescription = durationKeyword[i].getDescription();

				if(ProgramCentralUtil.isNotNullString(keywordName)&& keywordName.equalsIgnoreCase(sDKName)){
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, DurationMap, dDKDuration,sDKUnit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}else{
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, dDKDuration,sDKUnit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}
			}
			DurationKeywordsUtil durationKeywordsUtil = new DurationKeywordsUtil();
			durationKeywordsUtil.updateMassDurationKeywords(context, projectId, newDurationKeyword);
		}
	}
	
	public void updateDuration(Context context, String[] args) throws Exception
	{
		Map inputMap 		= JPO.unpackArgs(args);
		Map requestValueMap = (HashMap) inputMap.get("requestMap");
		Map columnMap 		= (Map) inputMap.get("columnMap");
		Map paramMap 		= (Map) inputMap.get("paramMap");

		String projectId 	= (String) requestValueMap.get("parentOID");
		String keywordName 	= (String) paramMap.get("objectId");
		double Duration 	= Task.parseToDouble((String) paramMap.get("New Value"));
		
		DurationKeywords durationKeywords = new DurationKeywords(context, projectId);
		DurationKeyword [] durationKeyword = durationKeywords.getDurationKeywords();
		
		if(durationKeyword != null && durationKeyword.length>0){
			DurationKeyword [] newDurationKeyword = new DurationKeyword[durationKeyword.length];
			
			for(int i=0; i<durationKeyword.length; i++){
				String sDKName 		= durationKeyword[i].getName();
				String sDKType 		= durationKeyword[i].getType();
				String sDKUnit 		= durationKeyword[i].getUnit();
				double dDKDuration 	= durationKeyword[i].getDuration();
				String sDescription = durationKeyword[i].getDescription();

				if(ProgramCentralUtil.isNotNullString(keywordName)&& keywordName.equalsIgnoreCase(sDKName)){
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, Duration,sDKUnit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}else{
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, dDKDuration,sDKUnit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}
			}
			DurationKeywordsUtil durationKeywordsUtil = new DurationKeywordsUtil();
			durationKeywordsUtil.updateMassDurationKeywords(context, projectId, newDurationKeyword);
		}
		}

	public void updateDurationDescription(Context context, String[] args) throws Exception
	{
		Map inputMap 		= JPO.unpackArgs(args);
		Map requestValueMap = (HashMap) inputMap.get("requestMap");
		Map columnMap 		= (Map) inputMap.get("columnMap");
		Map paramMap 		= (Map) inputMap.get("paramMap");

		String projectId 	= (String) requestValueMap.get("parentOID");
		String keywordName 	= (String) paramMap.get("objectId");
		String description 	= (String) paramMap.get("New Value");
		
		DurationKeywords durationKeywords = new DurationKeywords(context, projectId);
		DurationKeyword [] durationKeyword = durationKeywords.getDurationKeywords();
		
		if(durationKeyword != null && durationKeyword.length>0){
			DurationKeyword [] newDurationKeyword = new DurationKeyword[durationKeyword.length];
			
			for(int i=0; i<durationKeyword.length; i++){
				String sDKName 		= durationKeyword[i].getName();
				String sDKType 		= durationKeyword[i].getType();
				String sDKUnit 		= durationKeyword[i].getUnit();
				double dDKDuration 	= durationKeyword[i].getDuration();
				String sDescription = durationKeyword[i].getDescription();

				if(ProgramCentralUtil.isNotNullString(keywordName)&& keywordName.equalsIgnoreCase(sDKName)){
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, dDKDuration,sDKUnit,description);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}else{
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, dDKDuration,sDKUnit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}
			}
			DurationKeywordsUtil durationKeywordsUtil = new DurationKeywordsUtil();
			durationKeywordsUtil.updateMassDurationKeywords(context, projectId, newDurationKeyword);
		}

    }
	public void updateDurationUnit(Context context, String[] args) throws Exception
	{
		Map inputMap 		= JPO.unpackArgs(args);
		Map requestValueMap = (HashMap) inputMap.get("requestMap");
		Map columnMap 		= (Map) inputMap.get("columnMap");
		Map paramMap 		= (Map) inputMap.get("paramMap");

		String projectId 	= (String) requestValueMap.get("parentOID");
		String keywordName 	= (String) paramMap.get("objectId");
		String unit 	= (String) paramMap.get("New Value");
				
		DurationKeywords durationKeywords = new DurationKeywords(context, projectId);
		DurationKeyword [] durationKeyword = durationKeywords.getDurationKeywords();
		
		if(durationKeyword != null && durationKeyword.length>0){
			DurationKeyword [] newDurationKeyword = new DurationKeyword[durationKeyword.length];
			
			for(int i=0; i<durationKeyword.length; i++){
				String sDKName 		= durationKeyword[i].getName();
				String sDKType 		= durationKeyword[i].getType();
				String sDKUnit 		= durationKeyword[i].getUnit();
				double dDKDuration 	= durationKeyword[i].getDuration();
				String sDescription = durationKeyword[i].getDescription();

				if(ProgramCentralUtil.isNotNullString(keywordName)&& keywordName.equalsIgnoreCase(sDKName)){
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, dDKDuration,unit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}else{
					DurationKeyword durationKeywordObjectObject = new DurationKeyword(sDKName, sDKType, dDKDuration,sDKUnit,sDescription);
					newDurationKeyword[i] = durationKeywordObjectObject;
				}
			}
			DurationKeywordsUtil durationKeywordsUtil = new DurationKeywordsUtil();
			durationKeywordsUtil.updateMassDurationKeywords(context, projectId, newDurationKeyword);
		}

    }
	
	public Vector getColumnUnitData(Context context, String[] args) throws Exception 
	{
		
		Vector vecResult = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList mlObjectList = (MapList) programMap.get("objectList");
		MapList mlObjectListModify = new MapList();
		Map mapObjectInfo = null;
	    String lable1 = ProgramCentralConstants.EMPTY_STRING;
		for (Iterator itrTableRows = mlObjectList.iterator(); itrTableRows.hasNext();) 
		{
			mapObjectInfo = (Map) itrTableRows.next();
			String DisplayUnit=ProgramCentralConstants.EMPTY_STRING;
			DisplayUnit =(String) mapObjectInfo.get(DurationKeyword.ATTRIBUTE_DURATIONUNIT);
			if("d".equalsIgnoreCase(DisplayUnit)){
				lable1 =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Days", context.getSession().getLanguage()) ;
				vecResult.add(lable1);
			}
			else{
	        lable1 =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Hours", context.getSession().getLanguage()) ;
	        vecResult.add(lable1);
		}
		}
		return vecResult;
	}
	
	 public HashMap getDurationUnitRange(Context context, String[] args) throws MatrixException {

 	        HashMap durationUnitRanges = new HashMap();
	       
	        StringList durationUnitList = new StringList();
	        StringList durationUnitDisplayList = new StringList();

	        durationUnitList.add("d");
	        durationUnitList.add("h");
	        	              
	        String lable1=ProgramCentralConstants.EMPTY_STRING;
	        String lable2=ProgramCentralConstants.EMPTY_STRING;
	        lable1 =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Days", context.getSession().getLanguage()) ;
	        lable2 =ProgramCentralUtil.getPMCI18nString(context, "emxProgramCentral.DurationUnits.Hours", context.getSession().getLanguage()) ;
			
	        durationUnitDisplayList.add(lable1);
	        durationUnitDisplayList.add(lable2);
	        	        
	        durationUnitRanges.put("field_choices", durationUnitList);
	        durationUnitRanges.put("field_display_choices", durationUnitDisplayList);
	        return durationUnitRanges;
	    }
	 public boolean isDurationKeywordVisible(Context context,String[] args)throws Exception  {

		 Map programMap = (HashMap) JPO.unpackArgs(args);
		 String selectedNodeId = (String)programMap.get("objectId");

		 DurationKeyword[] durationKeyword = DurationKeywordsUtil.getDurationKeywordsValueForWBS(context,selectedNodeId);
		 if(durationKeyword.length == 0){
			 return false;				
		 }
		 return true;
	 }
}
