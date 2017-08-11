/*
 *  emxPartLibraryBase.java
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 */

import matrix.db.Context;


/**
 * The <code>emxPartLibraryBase</code> class.
 *
 */

public class emxPartLibraryBase_mxJPO extends emxLibraries_mxJPO
{
    /**
     * Creates emxPartLibraryBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments:
     * @throws Exception if the operation fails
     */

    public emxPartLibraryBase_mxJPO (Context context, String[] args) throws Exception
    {

        super (context, args);
    }


    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments:
     * @return int
     * @throws Exception if the operation fails
     * @exclude 
     */

    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "must specify method on emxPartLibraryBase invocation"
            );
        }

        return 0;
    }
}
