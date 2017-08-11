/*
 * ${CLASSNAME}.java

 */

import java.io.*;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.jsystem.util.MxPattern;
import com.matrixone.jsystem.util.MxMatcher;

import java.util.*;
import matrix.db.*;
import matrix.util.StringList;

/**
 * The <code>emxApprovalActionSortBase</code> class contains methods for comparision.
 *
 */

public class emxApprovalActionSortBase_mxJPO extends emxCommonBaseComparator_mxJPO
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

    public emxApprovalActionSortBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     *
     */

    public emxApprovalActionSortBase_mxJPO ()
    {
    }

    /**
    
     *
     * @param object1  The first map to compare.
     * @param object2  The second map to compare.
     * @return         A negative integer, zero, or a positive integer
     *                 as the first argument is less than, equal to,
     *                 or greater than the second.
   
     */

    public int compare(Object object1, Object object2) {
        int dirMult = 1;
        int retVal = 0;
        // Cast the objects to compare into maps.
        Map map1 = (Map) object1;
        Map map2 = (Map) object2;

        Map sortKeys = getSortKeys();

       // String keyName = (String) sortKeys.get("name");
        String keyDir  = (String) sortKeys.get("dir");

        // If the direction is not ascending, then set the
        // multiplier to -1.  Otherwise, set the multiplier to 1.

        if ("descending".equals(keyDir)) {
            dirMult = -1;
        } else {
            dirMult = 1;
        }
        String Task1=(String)map1.get("Action");
    	String Task2=(String)map2.get("Action");
        int  firstToken=containsNumber(Task1);
        int  secondToken=containsNumber(Task2);
        
        if(firstToken==-1 && secondToken==-1){
        	
        	retVal = Task1.compareTo(Task2);
        }else if(firstToken==-1 || secondToken==-1){
        	retVal = Task1.compareTo(Task2);
        }
        else{if(firstToken == secondToken) {
            	 retVal = 0;
            } else if (firstToken > secondToken) {
              retVal = 1;
            
            } else {
              retVal = -1;
            
            }
        }

        // Factor in the direction multiplier.
        retVal *= dirMult;
        return retVal;
    }
    
    private int containsNumber(String task){    	   	
    	
    		MxPattern p = MxPattern.compile("\\d+$");
    		MxMatcher m = p.matcher(task);
            if(m.find()){
            	String number=m.group(0);
            	return (Integer.parseInt((String)number));    
            }else{
        	return -1;
        }
 }
}
