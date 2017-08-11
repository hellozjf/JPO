/*
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   FileName : "$RCSfile: ${CLASSNAME}.java.rca $
 **   Version  : "$Revision: 1.8 $"
 **
 **  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:26 2008 przemek Experimental przemek $";
 */

import matrix.db.Context;


/**
 * The <code>emxGeneralClass</code> represents anything on the "To Side" of "Subclass"
 * Relationship in LC Schema
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxGeneralClass_mxJPO extends emxGeneralClassBase_mxJPO
{


    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     *    0 - String entry for "objectId"
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
    public emxGeneralClass_mxJPO (Context context, String[] args) throws Exception
    {

        // Call the super constructor
        super (context, args);
    }



    //~ Methods ----------------------------------------------------------------

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
                    "must specify method on emxGeneralClass invocation"
            );
        }

        return 0;
    }
}
