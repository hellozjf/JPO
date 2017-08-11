/*
 *  emxCommonPartFamily.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.*;

/**
 * The <code>emxCommonPartFamily</code> class is customizable class
 * for <code>emxCommonPartFamilyBase</code>
 *
 * @since Common 10.6
 * @grade 0
 */

public class emxCommonPartFamily_mxJPO extends emxCommonPartFamilyBase_mxJPO
{
    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.6
     * @grade 0
     */
    public emxCommonPartFamily_mxJPO (Context context, String[] args)
        throws Exception
    {
      	super(context, args);
    }
}
