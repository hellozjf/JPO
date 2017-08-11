/*
 *  emxGeneralLibraryBase.java
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 */

import matrix.db.Context;


/**
 * The <code>emxGeneralLibraryBase</code> class.
 *
 */

public class emxGeneralLibraryBase_mxJPO extends emxLibraries_mxJPO
{
    /**
     * Creates emxGeneralLibraryBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @throws Exception if the operation fails
     */

    public emxGeneralLibraryBase_mxJPO (Context context, String[] args) throws Exception
    {

        // Call the super constructor
        super (context, args);
    }


    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @return int
     * @throws Exception if the operation fails
     * @exclude
     */

    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "must specify method on emxGeneralLibraryBase invocation"
            );
        }

        return 0;
    }
}
