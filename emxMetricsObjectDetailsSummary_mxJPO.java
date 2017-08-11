/*
**  emxMetricsObjectDetailsSummary.java
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
 * The <code>emxMetricsObjectDetailsSummary</code> class represents the Object Details Summary
 * functionality for the BM type.
 *
 * @version BusinessMetrics 10.6 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxMetricsObjectDetailsSummary_mxJPO extends emxMetricsObjectDetailsSummaryBase_mxJPO
{

    /**
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since BusinessMetrics 10-6
     */
    public emxMetricsObjectDetailsSummary_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
