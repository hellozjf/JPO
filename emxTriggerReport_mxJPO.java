/*
**  emxTriggerReport
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
 * The <code>emxTriggerReport</code> class contains methods for Trigger Tool
 *
 * @version AEF 11-0 - Copyright (c) 2005, MatrixOne, Inc.
 */

public class emxTriggerReport_mxJPO extends emxTriggerReportBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */

    public emxTriggerReport_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

}
