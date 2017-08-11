/*
 * emxSortHTMLAlphaNumericBase
 * Copyright (c) 1999-2016 Dassault Systemes.
 * All Rights Reserved.
 */

import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

/**
 * The <code>emxSortHTMLAlphaNumericBase</code> class contains methods for comparision.
 * @author 3dPLM
 * @version 2012
 */
public class emxSortHTMLAlphaNumericBase_mxJPO extends emxCommonBaseComparator_mxJPO
{

    /** Declare Empty String variable. */
    protected static final String EMPTY_STRING = "";

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */

    public emxSortHTMLAlphaNumericBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * Default Constructor.
     */

    public emxSortHTMLAlphaNumericBase_mxJPO ()
    {
    }

    /**
     * This method used for comparing objects.
     *
     * @param object1 Map contains column Values
     * @param object2 Map contains column Values
     * @return integer representing the comparision value
     */

    public int compare (Object object1,Object object2)
    {

        int diff = 0;
        int dirMult = 1;
        Long long1  = null,long2 = null;
        boolean isStr1Number = true;
        boolean isStr2Number = true;
        Map map1 = (Map)object1;
        Map map2 = (Map)object2;

          //Get sort info keys
        Map sortKeys = getSortKeys();

        String keyName = (String) sortKeys.get("name");
        String keyDir  = (String) sortKeys.get("dir");

        // values will be retrieved for the lower case column names. This
        // has been done to get actual data displayed in case of html output)
        keyName = keyName.toLowerCase();

          // If the direction is not ascending, then set the
          // multiplier to -1.  Otherwise, set the multiplier to 1.

        if (! "ascending".equals(keyDir))
        {
            dirMult = -1;
        }
        else
        {
            dirMult = 1;
        }
          // Get column values
        String string1 = "";
        String string2 = "";
        boolean isToSortOnNameColumn = "name".equalsIgnoreCase(keyName)||"task name".equalsIgnoreCase(keyName);
        boolean isToSortOnTitleColumn = "title".equalsIgnoreCase(keyName);
        if(isToSortOnNameColumn)
        {
            string1 = getName(map1,keyName);
            string2 = getName(map2,keyName);
        }
        else if(isToSortOnTitleColumn)
        {
        	keyName = "Title";
            string1 = getName(map1,keyName);
            string2 = getName(map2,keyName);
        }
        else
        {
            string1 = (String) map1.get(keyName);
            string2 = (String) map2.get(keyName);
        }
        
        /*Get the "level" from map*/
        String level1 = (String) map1.get(ProgramCentralConstants.KEY_LEVEL);
        String level2 = (String) map2.get(ProgramCentralConstants.KEY_LEVEL);
        
        String parentId1 = (String) map1.get(DomainRelationship.SELECT_FROM_ID);
        String parentId2 = (String) map2.get(DomainRelationship.SELECT_FROM_ID);
        
        if(ProgramCentralUtil.isNullString(parentId1) || ProgramCentralUtil.isNullString(parentId2)){
        	
        	parentId1 = (String) map1.get("from.id");
        	parentId2 = (String) map2.get("from.id");
        }
        
        boolean sameLevel = false;
        boolean sameParentId = false;
        
        
        if(UIUtil.isNotNullAndNotEmpty(level1)&&UIUtil.isNotNullAndNotEmpty(level2))
        {
        	sameLevel = level1.equals(level2);
        }
        
        if(UIUtil.isNotNullAndNotEmpty(parentId1)&&UIUtil.isNotNullAndNotEmpty(parentId2))
        {
        	sameParentId = parentId1.equals(parentId2);
        }else{
        	parentId1 = (String) map1.get("id[parent]");
        	parentId2 = (String) map2.get("id[parent]");
        	if(ProgramCentralUtil.isNotNullString(parentId1) && ProgramCentralUtil.isNotNullString(parentId2)){
        		sameParentId = parentId1.equals(parentId2);
        	}
        }
        /*Compare the strings only if the level and parentId are same*/
        if (sameLevel && sameParentId){
          // If both values are null, then they are the same.
    		if (ProgramCentralUtil.isNullString(string1) && ProgramCentralUtil.isNullString(string2))
        {
            diff = 0;

           // If the first value is null, then it is first.
        }
    		else if (ProgramCentralUtil.isNullString(string1))
        {
            diff = -1;
           // If the second value is null, then it is first.
        }
    		else if (ProgramCentralUtil.isNullString(string2))
        {
            diff = 1;
              }
           // If both values are non-null, then compare
    		else {
    			diff = alphaNumericSort(string1,string2);
        }
        }

        return diff * dirMult ;
    }
    
	/**
	 * Returns the title of the object if type of the object is controlled folder.  
         * @param Map map containing object information
	 * @param String column name on which sorting is performed
	 * @return String name of object used for sorting  
	 */
    private String getName(Map mObject,String keyName)
    {
    	assert(null != mObject);
    	String sReturnObjName = "";
    	String sObjectName = (String)mObject.get("Name");
    	if(ProgramCentralUtil.isNullString(sObjectName))
    	{
    		sObjectName = (String)mObject.get("Task Name");
    		keyName = "name";
    	}
    	String sObjectType = (String)mObject.get("type");

    	sReturnObjName = (DomainConstants.TYPE_CONTROLLED_FOLDER.equalsIgnoreCase(sObjectType))? "" :(String)mObject.get(keyName);
    	if(ProgramCentralUtil.isNullString(sReturnObjName)) //if type is controlled folder then return title as name
    	{
    		StringList slParsedList = FrameworkUtil.split(sObjectName, "'");
    		assert(null != slParsedList);
    		String sParsedObj = (String)slParsedList.get(1);    		
    		sReturnObjName = sParsedObj.replace("\"", "");
    	}

    	return sReturnObjName;
    }
    /**
     * This function sort the two strings alphaNumerically,considering number as small compared to string.
     * @param string1 - first string to be sorted.
     * @param string2 - second string to sorted.
     * @return The result is zero if the strings are equal,
     *  The result is a Positive integer if string1 is greater than string2.
     *  The result is a Negative integer if string1 is less than string2.
     */
    public int alphaNumericSort(String string1,String string2) {

    	int lengthString1 = string1.length();
    	int lengthString2 = string2.length();
    	int index1 = 0;
    	int index2 = 0;
    	int result = 0;
    	
    	while (index1 < lengthString1 && index2 < lengthString2) {

    		char ch1 = string1.charAt(index1);
    		char ch2 = string2.charAt(index2);
    		char[] tempArray1 = new char[lengthString1];
    		char[] tempArray2 = new char[lengthString2];

    		int location1 = 0;
    		int location2 = 0;
    		//this do while loop separates numbers and string from first object to compare.
    		do {
    			tempArray1[location1++] = ch1;
    			index1++;

    			if (index1 < lengthString1) {
    				ch1 = string1.charAt(index1);
    			} else {
    				break;
    			}
    		} while (Character.isDigit(ch1) == Character.isDigit(tempArray1[0]));

    		//this do while loop separates numbers and string from second object to compare.
    		do {
    			tempArray2[location2++] = ch2;
    			index2++;

    			if (index2 < lengthString2) {
    				ch2 = string2.charAt(index2);
    			} else {
    				break;
    			}
    		} while (Character.isDigit(ch2) == Character.isDigit(tempArray2[0]));

    		String str1 = new String(tempArray1);
    		String str2 = new String(tempArray2);

    		if (Character.isDigit(tempArray1[0]) && Character.isDigit(tempArray2[0])) {
    			int firstNumberToCompare = Integer.parseInt(str1.trim());
    			int secondNumberToCompare = Integer.parseInt(str2.trim());
    			boolean tempResult = firstNumberToCompare > secondNumberToCompare;
    			if(firstNumberToCompare == secondNumberToCompare){
    				result = 0;
    			}else if(tempResult){
    				result = 1;
    			}else
    				result =-1;

    		} else {
    			str1 = str1.trim();  // Added for IR-496527
    			str2 = str2.trim();  // Added for IR-496527
    			result = str1.compareTo(str2);
    		}

    		if (result != 0) {
    			break;
    		}
    	}
    	return result;
    }
	
    //Method added for ODT 
    public Boolean toCallalphaNumericSort(Context context, String[] args) throws MatrixException { 
    	
    	int result = 0;
    	boolean isPositive = true;
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			StringList objectList = (StringList)programMap.get("objectList");
			String s1 = (String)objectList.get(0);
			String s2 = (String)objectList.get(1);
			
			result = alphaNumericSort(s1, s2);
			if(result<0){
				isPositive = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	    	return isPositive;	
		
    }
    
}
