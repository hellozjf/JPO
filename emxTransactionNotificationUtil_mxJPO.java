/*
**   emxTransactionNotificationUtil
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

/**
 * The <code>emxTransactionNotificationUtil</code> class contains static methods for sending email.
 *
 * @version  - V6R2009_HF0 Copyright (c) 2008, MatrixOne, Inc.
 */

public class emxTransactionNotificationUtil_mxJPO extends emxTransactionNotificationUtilBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since V6R2009_HF0
     */

    public emxTransactionNotificationUtil_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
