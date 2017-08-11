/*
 ** ${CLASS:MarketingFeature}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */


import matrix.db.Context;

/**
 * The <code>emxPUEECO</code> class contains code for the "PUEECO" business type.
 *
 * @version EC Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxPUEECO_mxJPO extends emxPUEECOBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */
    public emxPUEECO_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
