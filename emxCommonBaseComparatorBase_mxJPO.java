/*
**  emxCommonBaseComparatorBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.Map;
import java.util.Comparator;
import matrix.db.Context;

/**
 * The <code>emxCommonBaseComparatorBase</code> class is an Abstract wrapper class contains methods for Java <code>Comparator</code> class.
 *
 * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public abstract class emxCommonBaseComparatorBase_mxJPO implements Comparator
{
    /** Map object for storing key values. */

    protected Map keys = null;

    /**
     * Default Constructor.
     */

    public emxCommonBaseComparatorBase_mxJPO ()
    {
    }

    /**
     * This method used to set sortKeys to keys.
     *
     * @param sortKeys is a Map containing keys
     * @see #getSortKeys
     * @since AEF 10.0.1.0
     */

    public void setSortKeys(Map sortKeys)
    {
        keys = sortKeys;
    }

    /**
     * This method used to get key value.
     *
     * @return a Map object representing the sort keys value
     * @see #setSortKeys
     * @since AEF 10.0.1.0
     */

    public Map getSortKeys()
    {
        return keys;
    }

    /**
     * This method used to get the class name.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a String containing the class name
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     */

    public String getClassName(Context context,String[] args) throws Exception
    {
        return getClass().getName();
    }

    /**
     * This Abstract method need to implement and used for comparing objects.
     *
     * @param object1 Map containing column Values
     * @param object2 Map containing column Values
     * @return an integer representing the result of the comparision.
     * @since AEF 10.0.1.0
     */

    public abstract int compare(Object object1,Object object2);

}
