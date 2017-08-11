/*
 **  emxPartLibrary.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of MatrixOne,
 **  Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 **  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **  Author   :
 **  Version  :
 **  Date     :
 **  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:36 2008 przemek Experimental przemek $";
 **
 */

import matrix.db.Context;


/**
 * The <code>emxPartLibrary</code> class.
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxPartLibrary_mxJPO extends emxPartLibraryBase_mxJPO
{
   /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
    public emxPartLibrary_mxJPO (Context context, String[] args) throws Exception
    {
        // Call the super constructor
        super (context, args);
    }


    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     *
     * @return the Java <code>int</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "must specify method on emxPartLibrary invocation"
            );
        }

        return 0;
    }
}
