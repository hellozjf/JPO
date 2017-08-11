/*
**   emxSubscriptionReplyHandler
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
 * The <code>emxSubscriptionReplyHandler</code> class contains static methods for sending email.
 *
 * @version Common V6R2009-1 - Copyright (c) 2007-2016, Dassault Systemes..
 */

public class emxSubscriptionReplyHandler_mxJPO extends emxSubscriptionReplyHandlerBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     */

    public emxSubscriptionReplyHandler_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
