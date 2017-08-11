/*
 * emxTaskWBSSortBase.java
 *
 * Copyright (c) 2005-2016 Dassault Systemes.
 *
 * All Rights Reserved. This program contains proprietary and trade secret
 * information of MatrixOne, Inc. Copyright notice is precautionary only and
 * does not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */

import java.io.*;
import java.util.*;
import matrix.db.*;

/**
 * The <code>emxRouteTaskSortBase</code> class contains methods for comparision.
 *
 * @author Wipro
 * @version PLC 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxRouteTaskSortBase_mxJPO extends emxCommonBaseComparator_mxJPO
{

    /** Declare Empty String variable. */
    protected static final String EMPTY_STRING = "";

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PLC 10-6
     */

    public emxRouteTaskSortBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     * @since         PLC 10-6
     *
     */

    public emxRouteTaskSortBase_mxJPO ()
    {
    }

    /**
     * Compares two maps based on the key values of the MapComparator.
     * 
     *
     * <p><dl><dt><b>Example:</b><dd><tt>
     * <pre>
     * ArrayList sortKeys = new ArrayList(2);
     * sortKeys.add("name");
     * sortKeys.add("dir");
     * MapComparator mapCompare = new MultilevelMapComparator(sortKeys);
     * HashMap map1 = new HashMap(1);
     * map1.put("name", "1.1");
     * HashMap map2 = new HashMap(1);
     * map2.put("name", "1.2.1");
     * int diff = mapCompare.compare(map1, map2);
     * </pre>
     * </tt></dl>
     *
     * @param object1  The first map to compare.
     * @param object2  The second map to compare.
     * @return         A negative integer, zero, or a positive integer
     *                 as the first argument is less than, equal to,
     *                 or greater than the second.
     * @since PLC 10-6
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

        if (! "ascending".equals(keyDir)) {
            dirMult = -1;
        } else {
            dirMult = 1;
        }

  
          int  firstToken = Integer.parseInt((String) map1.get("attribute[Route Sequence]"));
          int  secondToken = Integer.parseInt((String)map2.get("attribute[Route Sequence]"));

            if(firstToken == secondToken) {
            	 retVal = 0;
            } else if (firstToken > secondToken) {
              retVal = 1;
            
            } else {
              retVal = -1;
            
            }
        

        // Factor in the direction multiplier.
        retVal *= dirMult;
        return retVal;
    }

}
