//
// $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 16:02:11 2008 przemek Experimental przemek $ 
//
/*
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **   Author   : "$Author: przemek $"
 **   Version  : "$Revision: 1.9 $"
 **   Date     : "$Date: Wed Oct 22 16:02:11 2008 $"
 **
 */

import matrix.db.Context;


/**
 * The <code>emxBookshelf</code> represents anything on the "To Side" of "Has
 * Bookshelves" Relationship in DC Schema
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxBookshelf_mxJPO extends emxBookshelfBase_mxJPO
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
    public emxBookshelf_mxJPO (Context context, String[] args) throws Exception
    {

        // Call the super constructor

        super (context, args);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Constructor
     *
     * @param id the Java <code>String</code> object
     *
     * @throws Exception if the operation fails
     */
    public emxBookshelf_mxJPO  (String id) throws Exception
    {

        // Call the super constructor

        super (id);
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
                    "must specify method on emxBookshelf invocation"
            );
        }

        return 0;
    }
}
