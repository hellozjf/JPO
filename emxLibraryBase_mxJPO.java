/*
 *  emxLibraryBase.java
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 */

import matrix.db.Context;


/**
 * The <code>emxLibrary</code> class represents the emxLibraryBase.
 *
 */

public class emxLibraryBase_mxJPO extends emxDOCUMENTCLASSIFICATION_mxJPO
{
    /**
     * Creates emxLibraryBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     * @throws Exception if the operation fails
     */

    public emxLibraryBase_mxJPO (Context context, String[] args) throws Exception
    {

        // Call the super constructor

        super (context, args);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Creates emxLibraryBase object given the objectId.
     *
     * @param id the Java <code>String</code> object
     * @throws Exception if the operation fails
     */

    public emxLibraryBase_mxJPO (String id) throws Exception
    {

        // Call the super constructor

        super (id);
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
                    "must specify method on emxLibraryBase invocation"
            );
        }

        return 0;
    }
}
