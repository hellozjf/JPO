/*
**  emxUITableGridBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program.
**
*/

import matrix.db.Context;

/**
 * The <code>emxUITableGridBase</code> class contains utility methods for the Grid Table UI Component.
 */
public class emxUITableGridBase_mxJPO extends com.matrixone.apps.framework.ui.UITableGrid
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation
     * @since V6R2012x
     * @grade 0
     */
    public emxUITableGridBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
