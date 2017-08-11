/*
*	 emxQuickFileAccess
*
*   Copyright (c) 1992-2016 Dassault Systemes.
*   All Rights Reserved.
*   This program contains proprietary and trade secret information of MatrixOne,
*   Inc.  Copyright notice is precautionary only
*   and does not evidence any actual or intended publication of such program
*
*   This JPO to implement quick file access feature
*
*/

import matrix.db.Context;

/**
 * The <code>emxCommonClearTrustAuthentication</code> jpo contains Trust Aunthentication methods.
 *
 * @version EC 10.5.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */


public class emxQuickFileAccess_mxJPO extends emxQuickFileAccessBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since EC 10.0.0.0
     */

    public emxQuickFileAccess_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
