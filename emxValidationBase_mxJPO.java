/*   emxWhatIfBase
 **
 **   Copyright (c) 2003-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   This JPO contains the implementation of emxWorkCalendar
 **
 **   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9.2.2 Thu Dec  4 07:55:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9.2.1 Thu Dec  4 01:53:19 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:49:37 2008 przemek Experimental przemek $
 */


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.Dimension;
import matrix.db.JPO;
import matrix.db.Unit;
import matrix.db.UnitItr;
import matrix.db.UnitList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.CacheUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.program.ImportUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.Task;

/**
 * The <code>emxValidationBase</code> class contains methods for validation.
 *
 * @version PMC 10.5.1.2 - Copyright(c) 2013, MatrixOne, Inc.
 */

public class emxValidationBase_mxJPO extends DomainObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public emxValidationBase_mxJPO (Context context, String[] args)	throws Exception{
		// Call the super constructor
		super();
		if (args != null && args.length > 0){
			setId(args[0]);
		}
	}
	
	public emxValidationBase_mxJPO ()throws Exception{
		// Call the super constructor
		super();
	}
	
	/**
	 * Validate name of object.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateName(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		
		Map<?, ?> programMap = JPO.unpackArgs(args);
        String name = (String) programMap.get("Name");
        name = name.contains(ProgramCentralConstants.SPACE) ? name.replace(ProgramCentralConstants.SPACE, ProgramCentralConstants.EMPTY_STRING) : name;
		String error = DomainObject.EMPTY_STRING;
		String invalidCharacters = EnoviaResourceBundle.getProperty(context, "emxFramework.Javascript.NameBadChars");
		String valid = "true";
		
		if(ProgramCentralUtil.isNullString(name)){
        	error = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ProjectImport.FieldShouldNotBeEmpty", context.getSession().getLanguage());
        	valid = "false";
        }
		
        if(name.length()>128){
        	error = EnoviaResourceBundle.getProperty(context, "ProgramCentral","emxProgramCentral.ProjectImport.FieldExceedsMaxLength", context.getSession().getLanguage());
        	valid = "false";
        }
        
        //validation for special character
        for (int i = 0; i < invalidCharacters.length();i++){
            char testChar = invalidCharacters.charAt(i);
            if (name.indexOf(testChar) >= 0){
            	error = EnoviaResourceBundle.getProperty(context,"ProgramCentral", "emxProgramCentral.ProjectImport.InvalidCharacterInTaskName", context.getSession().getLanguage());
            	error += " '" + testChar + "'.";
            	valid = "false";
            }
        }
        
        returnMap.put("Error", error);
        returnMap.put("Valid", valid);
		
		return returnMap;
	}
	
	/**
	 * Validate type of object.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateType(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		String error = DomainObject.EMPTY_STRING;
		String valid = "true";

		Map<?, ?> programMap = JPO.unpackArgs(args);
		String taskType = (String) programMap.get("Type");

		StringList typeList = (StringList)CacheUtil.getCacheObject(context, "typeList");
		if(typeList ==  null || typeList.isEmpty()){
			StringList ojbectTypeList = ProgramCentralUtil.getSubTypesList(context, DomainObject.TYPE_TASK_MANAGEMENT);
			ojbectTypeList.addElement(DomainObject.TYPE_PROJECT_SPACE);

			if(ProgramCentralUtil.isNullString(taskType) && !ojbectTypeList.contains(taskType)){
				error = EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.ProjectImport.TaskTypeNotSupported", context.getSession().getLanguage())+ ".";
				valid = "false";
			}
			CacheUtil.setCacheObject(context, "typeList", ojbectTypeList);
		}else{
			if(ProgramCentralUtil.isNullString(taskType) || !typeList.contains(taskType)){
				error = EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.ProjectImport.TaskTypeNotSupported", context.getSession().getLanguage())+ ".";
				valid = "false";
			}
		}

		returnMap.put("Error", error);
		returnMap.put("Valid", valid);

		return returnMap;
	}
	
	/**
	 * Validate task WBS value.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateTaskWBS(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();

		String error = DomainObject.EMPTY_STRING;
		String valid = "true";
		
		Map <String,String>programMap = JPO.unpackArgs(args);
		String wbs =  programMap.get("Level");
		
		if(ProgramCentralUtil.isNotNullString(wbs)){
			for (int i = 0; i < wbs.length(); i++) {
				char thischar = wbs.charAt(i);
				if (thischar < '0' || thischar > '9') {
					if (thischar == '.') {
						// This is a valid condition
					}else{
						error = EnoviaResourceBundle.getProperty(context,"ProgramCentral", "emxProgramCentral.ProjectImport.InvalidImportFileFormat", context.getSession().getLanguage());
						valid = "false";
					}
				}
			}
		}else{
			error = EnoviaResourceBundle.getProperty(context,"ProgramCentral", "emxProgramCentral.ProjectImport.FieldShouldNotBeEmpty", context.getSession().getLanguage());
			valid = "false";
		}
		returnMap.put("Error", error);
        returnMap.put("Valid", valid);

		return returnMap;
	}
	
	/**
	 * Validate task duration.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateTaskDuration(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		StringBuffer invalidCharacters = new StringBuffer("*[]<>@$%?|,");
		String error = DomainObject.EMPTY_STRING;
		String valid = "true";
		
		Map <String,String>programMap = JPO.unpackArgs(args);
		String taskDuration =  programMap.get("Estimated Duration");
		
		if(ProgramCentralUtil.isNullString(taskDuration)){
			valid = "false";
			error = EnoviaResourceBundle.getProperty(context,"ProgramCentral", "emxProgramCentral.ProjectImport.FieldShouldNotBeEmpty", context.getSession().getLanguage()); 
		}else{
			for (int i = 0; i < invalidCharacters.length();i++){
                char testChar = invalidCharacters.charAt(i);
                if (taskDuration.indexOf(testChar) >= 0){
                  error = EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.Common.EstimatedDuration", context.getSession().getLanguage())+ " ";
                  error += EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.Import.MustBeARealNumber", context.getSession().getLanguage());
                  valid = "false";
                }
              }
		}
		
		returnMap.put("Error", error);
        returnMap.put("Valid", valid);
		return returnMap;
	}
	
	/**
	 * Validate Date.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateDate(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		String error = DomainObject.EMPTY_STRING;
		String valid = "true";
		
		Map <String,String>programMap = JPO.unpackArgs(args);
		String startDate =  programMap.get("Date");
		String strTimeZone 			= (String)programMap.get("timeZone");
		double dClientTimeZoneOffset = (new Double(strTimeZone)).doubleValue();
		
		if(ProgramCentralUtil.isNotNullString(startDate)){
			String validStartDate =   ImportUtil.dateValidator("mm/dd/yy", startDate, context.getSession().getLanguage(), context);
			 if(ProgramCentralUtil.isNullString(validStartDate)){
				 valid = "false";
				 error = startDate + " " + EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.Import.InvalidDate",context.getSession().getLanguage()) + ".";
			 }else{
				 try{
					 startDate = startDate.trim();
					 startDate = eMatrixDateFormat.getFormattedDisplayDate(startDate, dClientTimeZoneOffset,Locale.US);
				 }catch(Exception e){
					 e.printStackTrace();
				 }
			 }
		}else{
			error = EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.ProjectImport.FieldShouldNotBeEmpty",context.getSession().getLanguage()) + ".";
			valid = "false";
		}
		
		returnMap.put("Error", error);
        returnMap.put("Valid", valid);
        returnMap.put("Date", startDate);

		return returnMap;
	}
	
	
	
	public Map<String, String> validateConstraintDate(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		String error = DomainObject.EMPTY_STRING;
		String valid = "true";
		
		Map <String,String>programMap = JPO.unpackArgs(args);
		String startDate =  programMap.get("Date");
		String strTimeZone 			= (String)programMap.get("timeZone");
		double dClientTimeZoneOffset = (new Double(strTimeZone)).doubleValue();
		
		if(ProgramCentralUtil.isNotNullString(startDate)){
			String validStartDate =   ImportUtil.dateValidator("mm/dd/yy", startDate, context.getSession().getLanguage(), context);
			 if(ProgramCentralUtil.isNullString(validStartDate)){
				 valid = "false";
				 error = startDate + " " + EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.Import.InvalidDate",context.getSession().getLanguage()) + ".";
			 }else{
				 try{
					 startDate = startDate.trim();
					 startDate = eMatrixDateFormat.getFormattedDisplayDate(startDate, dClientTimeZoneOffset,Locale.US);
				 }catch(Exception e){
					 e.printStackTrace();
				 }
			 }
		}
		
		returnMap.put("Error", error);
        returnMap.put("Valid", valid);
        returnMap.put("Date", startDate);

		return returnMap;
	}
	
	/**
	 * Validate dependency.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateDependency(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		String error = DomainObject.EMPTY_STRING;
		String valid = "true";
		StringList dependencyTypeList = Task.getDependencyTypes(context);
		
		Map <String,String>programMap = JPO.unpackArgs(args);
		String dependencyType =  programMap.get("Dependencies");
		
        StringBuffer newDepBuffer = new StringBuffer();
        
        
        StringList slLagTimeUnits = new StringList();
    	final String ATTRIBUTE_LAG_TIME = PropertyUtil.getSchemaProperty(context, "attribute_LagTime");
        AttributeType attrType = new AttributeType(ATTRIBUTE_LAG_TIME);
        UnitList units = null;
        try
        {
            Dimension dimension = attrType.getDimension(context);
            if (dimension == null) {
                units = new UnitList();
            }
            else {
                units = dimension.getUnits(context);
            }
        }catch (Exception e) {
            units = new UnitList();
        }
        
        for (UnitItr unitItr = new UnitItr (units); unitItr.next();) {
            Unit unit = unitItr.obj();
            slLagTimeUnits.addElement(unit.getName());
        }
        
        if (dependencyType != null) {
            StringList slDependencies = FrameworkUtil.split (dependencyType, ProgramCentralConstants.COMMA);
            boolean isDependencyError = false;
            
            for (StringItr dependencyItr = new StringItr(slDependencies); dependencyItr.next();) {
                String strDependency = dependencyItr.obj();
                String strDependecyIndex = ProgramCentralConstants.EMPTY_STRING;
                String strDependencyType = ProgramCentralConstants.EMPTY_STRING;
                String strLagTimeSign = ProgramCentralConstants.EMPTY_STRING;
                String strLagTime = ProgramCentralConstants.EMPTY_STRING;
                String strLagTimeUnit = ProgramCentralConstants.EMPTY_STRING;
                String strDependencyWithoutIndex = ProgramCentralConstants.EMPTY_STRING;
                boolean isDependencyWithoutType = false;
                //1:FS+3 d
                if (ProgramCentralUtil.isNullString(strDependency)) {
                    isDependencyError = true;
                    break;
                }
                
                if (strDependency.indexOf(":") == -1) {
                	isDependencyWithoutType = true;
                	strDependecyIndex = strDependency;
                	strDependencyWithoutIndex = "FS"; // set default dependency type "FS"
                }
                
                if(!isDependencyWithoutType) {
                    StringList slDependencyIndexAndTypeLag = FrameworkUtil.split (strDependency, ":");
                    // Got 1
                    strDependecyIndex = (String)slDependencyIndexAndTypeLag.get(0);
                 // Got FS+3 d
                    strDependencyWithoutIndex = (String)slDependencyIndexAndTypeLag.get(1);
                }
                //
                // Validate this index
                //
                try {
                    int nDependecnyIndex = Integer.parseInt (strDependecyIndex);
                }
                catch (NumberFormatException nfe) {
                    isDependencyError = true;
                    break; 
                }
                
                if ( (strDependencyWithoutIndex.indexOf("+") != -1) || (strDependencyWithoutIndex.indexOf("-") != -1)) {
                    strLagTimeSign = (strDependencyWithoutIndex.indexOf("+") != -1) ? "+":"-";
                    
                    StringList slDependencyTypeAndLag = FrameworkUtil.split (strDependencyWithoutIndex, strLagTimeSign);
                    
                    // Got FS
                    strDependencyType = (String)slDependencyTypeAndLag.get (0);
                    strDependencyType = strDependencyType.trim();
                    
                    // Got 3.1 d
                    String strLagTimeAndUnit = (String)slDependencyTypeAndLag.get (1);
                    strLagTimeAndUnit = strLagTimeAndUnit.trim();
                    
                    if (strLagTimeAndUnit.indexOf(" ") == -1) {
                        isDependencyError = true;
                        break;
                    }
                    
                    StringList slLagTimeAndUnit = FrameworkUtil.split (strLagTimeAndUnit, " ");
                    
                    strLagTime = (String)slLagTimeAndUnit.get (0);
                    strLagTimeUnit = (String)slLagTimeAndUnit.get (1);
                }
                else {
                    strDependencyType = strDependencyWithoutIndex;
                    strLagTimeSign = "+";
                    strLagTime = "0.0";
                    strLagTimeUnit = "d";
                    
                }
                //
                // Validate dependency type
                //
                if (strDependencyType == null || "".equals(strDependencyType) || "null".equals(strDependencyType)) {
                    isDependencyError = true;
                    break;
                }
                
                Iterator<?> dependencyTypeItr = dependencyTypeList.iterator();
                boolean validType = false;
                String matrixDep = null;
                while (dependencyTypeItr.hasNext()) {
                  matrixDep = (String) dependencyTypeItr.next();
                  if (strDependencyType.equals(matrixDep)){
                    validType = true;
                    break;
                  }
                }
                
                if (!validType) {
                    isDependencyError = true;
                    break;
                }
                //
                // Validate lag time
                //
                if ("+".equals(strLagTimeSign) || "-".equals(strLagTimeSign)) {
                    try {
                        double dblLagTime = Task.parseToDouble(strLagTime);
                    }
                    catch (NumberFormatException nfe) {
                        isDependencyError = true;
                        break;
                    }
                    
                    if (!slLagTimeUnits.contains(strLagTimeUnit)) {
                        isDependencyError = true;
                        break;
                    }
                }
                
                if (newDepBuffer.length() != 0) {
                    newDepBuffer.append (",");
                }
                newDepBuffer.append (strDependecyIndex)
                			.append(":")
                			.append(strDependencyType)
                			.append(strLagTimeSign)
                			.append(strLagTime)
                			.append(" ")
                			.append(strLagTimeUnit);
            }// For each dependency
            
            if (isDependencyError) {
            	error = EnoviaResourceBundle.getProperty(context,"ProgramCentral","emxProgramCentral.ProjectImport.UnsupportedDependencyType",context.getSession().getLanguage()) + ".";
            	valid = "false";
            }
        }
		
		returnMap.put("Error", error);
        returnMap.put("Valid", valid);
        
		return returnMap;
	}
	
	/**
	 * Validate assignee.
	 * @param context - The eMatrix <code>Context</code> object.
	 * @param args holds information about object.
	 * @return a map which contains given value is valid or not.
	 * @throws Exception if operation fails.
	 */
	public Map<String, String> validateAssignee(Context context,String []args)throws Exception
	{
		Map <String,String>returnMap = new HashMap<String,String>();
		String userId = DomainObject.EMPTY_STRING;
		String error = DomainObject.EMPTY_STRING;
		String valid = "true";

		Map <String,String>programMap = JPO.unpackArgs(args);
		String assignee =  programMap.get("Assignees");
		
		Map<?, ?> personInfo = (Map<?, ?>)CacheUtil.getCacheMap(context, "PersonInfo");
		if(personInfo == null || personInfo.isEmpty()){
			String fullNameFormat = EnoviaResourceBundle.getProperty(context,"emxFramework.FullName.Format");
			
			StringList slSelect = new StringList(4);
			slSelect.addElement(DomainObject.SELECT_ID);
			slSelect.addElement(DomainObject.SELECT_NAME);
			slSelect.addElement(Person.SELECT_FIRST_NAME);
			slSelect.addElement(Person.SELECT_LAST_NAME);

			String typePattern = DomainObject.TYPE_PERSON;
			MapList personInfoList = DomainObject.findObjects(context,
					typePattern,
					null,
					null,
					slSelect);
			Map <String,Map<?,?>>personMap = new HashMap<String, Map<?, ?>>();
			String fullName = DomainObject.EMPTY_STRING;

			for(int i=0;i<personInfoList.size();i++){
				fullName = fullNameFormat;
				Map<?, ?> perInfoMap = (Map<?, ?>)personInfoList.get(i);
				String fName = (String)perInfoMap.get(Person.SELECT_FIRST_NAME);
				String lName = (String)perInfoMap.get(Person.SELECT_LAST_NAME);
				fullName = FrameworkUtil.findAndReplace(fullName, emxPersonBase_mxJPO.FIRST_NAME, fName);
				fullName = FrameworkUtil.findAndReplace(fullName, emxPersonBase_mxJPO.LAST_NAME, lName);

				personMap.put(fullName, perInfoMap);
			}

			if(personMap != null){
				CacheUtil.setCacheMap(context, "PersonInfo", personMap);
				
				StringList assigneeList = FrameworkUtil.split(assignee, ",");
				if(assigneeList != null && !assigneeList.isEmpty()){
					for(int i=0; i<assigneeList.size();i++){
						String assigneeName = (String)assigneeList.get(i);
						Map<?, ?> personInfoMap = (Map<?, ?>)personMap.get(assigneeName);
						
						if(personInfoMap != null){
							String personId = (String)personInfoMap.get(DomainObject.SELECT_ID);
							userId += assigneeName+":"+personId+"|";
						}else{
							userId += "Error:"+assigneeName+"|";
						}
					}
					userId = userId.substring(0, userId.length()-1);
				}
			}
		}else{
			StringList assigneeList = FrameworkUtil.split(assignee, ",");
			if(assigneeList != null && !assigneeList.isEmpty()){
				for(int i=0; i<assigneeList.size();i++){
					String assigneeName = (String)assigneeList.get(i);
					Map<?, ?> personMap = (Map<?, ?>)personInfo.get(assigneeName);
					if(personMap != null){
						String personId = (String)personMap.get(DomainObject.SELECT_ID);
						userId += assigneeName+":"+personId+"|";
					}else{
						userId += "Error:"+assigneeName+"|";
					}
				}
				userId = userId.substring(0, userId.length()-1);
			}
		}
		
		returnMap.put("Error", error);
		returnMap.put("Valid", valid);
		returnMap.put("AssigneeList", userId);
		return returnMap;
	}
}

