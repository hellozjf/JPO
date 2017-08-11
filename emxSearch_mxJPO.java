/*
**  emxSearch
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

/**
 * The <code>emxSearch</code> class contains methods for the "Search" component.
 *
 * @version AEF 10.5.0.0 - Copyright(c) 2003, MatrixOne, Inc.
 */

public class emxSearch_mxJPO extends emxSearchBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public emxSearch_mxJPO (Context context, String[] args)
    	throws Exception
    {
		super(context, args);
    }

}
