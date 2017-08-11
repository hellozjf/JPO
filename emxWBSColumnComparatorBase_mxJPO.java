/*
**  emxWBSColumnComparatorBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.Map;
import matrix.util.MatrixException;

/**
 * The <code>emxWBSColumnComparatorBase</code> class is an Abstract wrapper class contains methods for Java <code>Comparator</code> class.
 *
 * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
*/

public class emxWBSColumnComparatorBase_mxJPO extends emxCommonBaseComparator_mxJPO
{
	 /** Map object for storing key values. */

    protected Map keys = null;

    /**
     * Default Constructor.
     */

    public emxWBSColumnComparatorBase_mxJPO ()
        throws Exception
    {
             super();
    }

     /**
     * This method compares the two objects.
     * @author RG6
     * @param object1 Map containing column Values
     * @param object2 Map containing column Values
     * @return an integer representing the result of the comparision.
     * @since PRG R210
     */

    public int compare(Object object1,Object object2) {
    	int retVal = 1;
    	Map firstMap = (Map)object1;
    	Map secondMap = (Map)object2;
    	try{
    		Map sortKeys = getSortKeys();
    		String keyName = (String) sortKeys.get("name");
    		String keyDir  = (String) sortKeys.get("dir");
    		if("ascending".equals(keyDir))
    			retVal = compareByWBSId(firstMap,secondMap,keyName);
    		else{
    			retVal = compareByWBSId(secondMap,firstMap,keyName);
    		}
    	}
    	catch(MatrixException me){
    		me.printStackTrace();
    		//Do Nothing
    	}
    	return retVal;
    }	
    /**
     * This method is called from the compare method which pass the maps of the objects
     * to compare for the given column name specified by keyName.
     * @author RG6
     * @param object1 Map containing column Values
     * @param object2 Map containing column Values
     * @return an integer representing the result of the comparison.
     * @throws MatrixException
     *         if the operation fails
     * @since PRG R210
     */	
    private int compareByWBSId(Map firstMap,Map secondMap,String keyName) throws MatrixException{
    	int returnVal = 1;
    	try{
            
        	String WBSID1 = (String)firstMap.get(keyName);
        	String WBSID2 = (String)secondMap.get(keyName);
        	String[] arrWBSID1 = null;
        	String[] arrWBSID2 = null;
        	if(WBSID1 != null && WBSID1.contains(".")){
        		arrWBSID1 = WBSID1.split("\\.");
        	}else{
        		arrWBSID1 = new String[1];
        		arrWBSID1[0] = WBSID1;
        	}
        	
        	if(WBSID2 != null && WBSID2.contains(".")){
        		arrWBSID2 = WBSID2.split("\\.");
        	}else{
        		arrWBSID2 = new String[1];
        		arrWBSID2[0] = WBSID2;
        	}
        	
        	
        	String[] tempArr = null;
        	int len1 = 0;
        	int len2 = 0;
        	boolean firstGreaterThanSecond = true;
        	if((len1 = arrWBSID1.length) > (len2 = arrWBSID2.length)){
        		tempArr = new String[len1];
        		for(int i=0;i<len2;i++){
        			tempArr[i] = arrWBSID2[i];
        		}
        		
        		for(int i= len2;i<len1;i++){
        			tempArr[i] = "0";
        		}
        	}
        	else if((len1 = arrWBSID1.length) < (len2 = arrWBSID2.length)){
        		tempArr = new String[len2];
        		for(int i=0;i<len1;i++){
        			tempArr[i] = arrWBSID1[i];
        		}
        		
        		for(int i= len1;i<len2;i++){
        			tempArr[i] = "0";
        		}
        		
        		firstGreaterThanSecond = false;
        	}
        	else
        		tempArr = arrWBSID2;
        	
        	if(firstGreaterThanSecond)
        		arrWBSID2 = tempArr;
        	else
        		arrWBSID1 = tempArr;
        	
        	//Now Compare
        	for(int i=0;i<arrWBSID1.length;i++){
        		if(Integer.valueOf(arrWBSID1[i]) > Integer.valueOf(arrWBSID2[i])){
        			returnVal =  1;
        			break;
        		}
        		else if(Integer.valueOf(arrWBSID1[i]) < Integer.valueOf(arrWBSID2[i])){
        			returnVal =  -1;
        			break;
        		}
        	}
    	}
    	catch(Exception e){
    		throw new MatrixException(e);
    	}
    	return returnVal;
    }    	
}
