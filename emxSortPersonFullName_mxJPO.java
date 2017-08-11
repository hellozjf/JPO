/*
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
*/

import matrix.db.Context;

/**
 * The <code>${CLASSNAME}</code> class contains methods to compare full name of a person.
 * @author SG2
 */

public class emxSortPersonFullName_mxJPO extends emxSortPersonFullNameBase_mxJPO
{

    /**
     * emxSortPersonFullName JPO constructor
     * @param context
     * @param args
     * @throws Exception
     */
    public emxSortPersonFullName_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * Default Constructor.
     */

    public emxSortPersonFullName_mxJPO ()
    {
        super();
    }

}
