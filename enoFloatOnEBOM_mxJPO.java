/*
 ** ${CLASS:enoFloatOnEBOM}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import com.matrixone.apps.domain.util.FrameworkException;

import matrix.db.Context;

/**
 * The <code>enoFloatOnEBOM</code> functionality methods.
 *
 * @version R418
 */
public class enoFloatOnEBOM_mxJPO extends enoFloatOnEBOMBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since R418
     */
    public enoFloatOnEBOM_mxJPO (Context context, String[] args) throws FrameworkException
    {
        super(context, args);
    }
}
