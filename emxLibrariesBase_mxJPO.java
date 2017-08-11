/*
 *  emxLibrariesBase.java
 *
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of
 *  MatrixOne, Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 */

import matrix.db.Context;

/**
 * The <code>emxLibrariesBase</code> class.
 *
 */
public class emxLibrariesBase_mxJPO extends emxLibraryCentralCommon_mxJPO
{

    /**
     * Creates emxLibrariesBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @throws Exception if the operation fails
     */

    public emxLibrariesBase_mxJPO (Context context,
                         String[] args) throws Exception
    {
        super(context, args);
    }


    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <String[]</code> object
     * @return the Java <code>int</code>
     * @throws Exception if the operation fails
     * @exclude
     */

    public int mxMain (Context context, String[] args ) throws Exception
    {
        if ( true )
        {
            throw new Exception ("Do not call this method!");
        }

        return 0;
    }
}
