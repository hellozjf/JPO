/*   emxCommonECR
**
**   Copyright (c) 2004-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the common code for the "ECR" business type
**
*/

import matrix.db.Context;

/**
 * The <code>emxCommonECR</code> class contains common code for the "ECR" business type
 *
 * @version 10-5-SP1 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxCommonECR_mxJPO extends emxCommonECRBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10-5-SP1
     */
    public emxCommonECR_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
