/*
**  emxWBSIDComparatorBase
**
** Copyright (c) 1999-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**  @since Program Central R210
**  @author NR2

*/

import java.util.Date;
import java.util.Map;
import java.util.Comparator;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.eMatrixDateFormat;

import matrix.db.Context;
import matrix.util.MatrixException;

/**
 * The <code>emxWBSIDComparatorBase</code> class is an Abstract wrapper class contains methods for Java <code>Comparator</code> class.
 *
 * @version PRG R210 - Copyright (c) 2003, MatrixOne, Inc.
 * @since Program Central R210
 * @author NR2
 */
public class emxWBSIDComparatorBase_mxJPO extends emxCommonBaseComparator_mxJPO
{
    /** Map object for storing key values. */

    protected Map keys = null;

    /**
     * Construct
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since Program Central R210
     * @author NR2
    */
    public emxWBSIDComparatorBase_mxJPO ()
        throws Exception
    {
             super();
    }

     /**
     * This method need to implement and used for comparing objects.
     *
     * @param object1 Map containing column Values
     * @param object2 Map containing column Values
     * @return an integer representing the result of the comparision.
     * @since Program Central R210
     * @author NR2
     */
    public int compare(Object object1,Object object2){
    	Map firstMap = (Map)object1;
    	Map secondMap = (Map)object2;
  
    	/*
    	 * Compare based on Estimated start date in ascending order
    	 * if Est. start dates same, order based on Type (Gate before phase).
    	 * Else compare based on WBS Id
    	 */
    	
    	String attrEstStartDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]";
    	String estStartDate = (String)firstMap.get(attrEstStartDate);
    	String estFinishDate = (String)secondMap.get(attrEstStartDate);
    	
		Date dtstartDate = eMatrixDateFormat.getJavaDate(estStartDate);
		Date dtfinishDate = eMatrixDateFormat.getJavaDate(estFinishDate);
		
		if(dtstartDate.before(dtfinishDate)){
			return -1;
		}
		else if(dtstartDate.after(dtfinishDate)){
			return 1;
		}
		else{ //Est start dates same
			//Added:PRG:rg6:R210
			int retVal = 1;
			try{
				retVal = compareByWBSId(firstMap,secondMap);
			}catch(MatrixException me){
	    		//Do Nothing
	    	}
			return retVal;
			//End:PRG:rg6:R210
		}
    }	

     /**
     * This method is used to compare a map based on WBS Id.
     *
     * @param firstMap Map containing 1st Map
     * @param secondMap Map containing 2nd Map
     * @return an integer representing the result of the comparision.
     * @throws MatrixException if the operation fails
     * @since Program Central R210
     * @author NR2
     */
    private int compareByWBSId(Map firstMap,Map secondMap)throws MatrixException{
    	int returnVal = 1;
    	try{
        	String attrDependencyTaskWBS =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
        	String WBSID1 = (String)firstMap.get(attrDependencyTaskWBS);
        	String WBSID2 = (String)secondMap.get(attrDependencyTaskWBS);
        /* rg6:PRG:R210
        	String[] arrWBSID1 = WBSID1.split("\\.");
        	String[] arrWBSID2 = WBSID2.split("\\.");
        */
        //Added:PRG:rg6:R210
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
        //End:PRG:rg6:R210	
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
