/*
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
*/


import java.util.Map;

import matrix.db.Context;

import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.PersonUtil;

/**
 * @author SG2 * 
 */

public class emxSortPersonFullNameBase_mxJPO extends emxCommonBaseComparator_mxJPO
{
    private Context context;
   
    /**
     * emxSortPersonFullNameBase JPO Constructor
     * @param context
     * @param args
     * @throws Exception
     */
    public emxSortPersonFullNameBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        this.context = context;
    }

    /**
     * Default Constructor.
     */

    public emxSortPersonFullNameBase_mxJPO ()
    {
        try {
            this.context = ContextUtil.getAnonymousContext();
        } catch (Exception e) {}
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
        Map map1 = (Map)object1;
        Map map2 = (Map)object2;

          /*Get sort info keys*/
        Map sortKeys = getSortKeys();

        String keyName = (String) sortKeys.get("name");
        String keyDir  = (String) sortKeys.get("dir");

        /*
         * values will be retrieved for the lower case column names, this has been done to get actual data displayed in html output
         * If the value for lower case key is null, then get column values for original column name passed.
        */
        String htmlOutPutDisplayKey = keyName.toLowerCase();
        String stringValue1 = (String) map1.get(htmlOutPutDisplayKey); 
        stringValue1 = !(stringValue1 == null) ? stringValue1 : (String) map1.get(keyName);
        String stringValue2 = (String) map2.get(htmlOutPutDisplayKey);
        stringValue2 = !(stringValue2 == null) ? stringValue2 : (String) map2.get(keyName);


        boolean str1Empty = isEmpty(stringValue1);
        boolean str2Empty = isEmpty(stringValue2);
        
        /*      
         * If both values are null or empty diff =  0
         * If first string is null or empty diff = -1
         * If second string is null or empty diff = 1
         * If both the strings are not empty then compare the strings
        */        
        int diff = 0; 
            
        try {
            diff = str1Empty && str2Empty ?  0 :         
                                str1Empty ? -1 :  
                                str2Empty ?  1 :      
                                PersonUtil.getFullName(context, stringValue1).compareToIgnoreCase(PersonUtil.getFullName(context, stringValue2));
        } catch (FrameworkException e) {
            throw new RuntimeException(e.getMessage());
        } 
                    
        /* If the direction is not ascending, then invert the sign of 'diff' value */
        return ("ascending".equals(keyDir)? diff : -diff);
    }
    
    private boolean isEmpty(String checkValue) {
        return (checkValue == null || checkValue.equals("")); 
    }
}
