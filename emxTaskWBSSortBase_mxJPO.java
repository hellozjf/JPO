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
 * The <code>emxTaskWBSSortBase</code> class contains methods for comparision.
 *
 * @author Wipro
 * @version PLC 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxTaskWBSSortBase_mxJPO extends emxCommonBaseComparator_mxJPO
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

    public emxTaskWBSSortBase_mxJPO (Context context, String[] args)
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

    public emxTaskWBSSortBase_mxJPO ()
    {
    }

    /**
     * Compares two maps based on the key values of the MapComparator.
     * Key name values are splitted by '.' delimiter and compared.
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
        String keyType = null;
        int retVal = 0;
        // Cast the objects to compare into maps.
        Map map1 = (Map) object1;
        Map map2 = (Map) object2;

        Map sortKeys = getSortKeys();

        String keyName = (String) sortKeys.get("name");
        String keyDir  = (String) sortKeys.get("dir");

        // If the direction is not ascending, then set the
        // multiplier to -1.  Otherwise, set the multiplier to 1.

        if (! "ascending".equals(keyDir)) {
            dirMult = -1;
        } else {
            dirMult = 1;
        }

        String string1 = (String) map1.get(keyName);
        String string2 = (String) map2.get(keyName);

        // construct the 2 tokenizers of of the passed value
        StringTokenizer firstTok = new StringTokenizer(string1, ".");
        StringTokenizer secondTok = new StringTokenizer(string2, ".");

        // determine the lowest token count among the 2 to loop thru
        int loopCount = firstTok.countTokens();
        int secondTokCount = secondTok.countTokens();

        if(loopCount > secondTokCount) {
          loopCount = secondTokCount;
        }

        int count =1;
        while(count <= loopCount ) {
            count++;
            int firstToken=0;
            int secondToken=0;

            firstToken = Integer.parseInt((String)firstTok.nextToken());
            secondToken = Integer.parseInt((String)secondTok.nextToken());

            if(firstToken == secondToken) {

                //  retrun 0 if the both tokenizer has no more tokens ie. first == second
                //  return 1 if first tok has more elements and second has no more elements
                //  i.e first > second
                //  retrun -1 if first tok has no more elements and second more elements
                //  i.e first < second
                //  else contiue loop since both has more elements to compare

                if(!firstTok.hasMoreTokens() && !secondTok.hasMoreTokens()) {
                    retVal = 0;
                    break;
                } else if(firstTok.hasMoreTokens() && !secondTok.hasMoreTokens()) {
                    retVal = 1;
                    break;
                } else if(!firstTok.hasMoreTokens() && secondTok.hasMoreTokens()) {
                    retVal = -1;
                    break;
                }
            } else if (firstToken > secondToken) {
              retVal = 1;
              break;
            } else {
              retVal = -1;
              break;
            }
        }

        // Factor in the direction multiplier.
        retVal *= dirMult;
        return retVal;
    }

}
