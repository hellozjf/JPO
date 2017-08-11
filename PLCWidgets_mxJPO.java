/*
**  emxPLCCommon.java
**
** Copyright (c) 1999-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/

import matrix.db.Context;

/**
 * This JPO class is contians methods to support the PLC Widgets.
 * @author SE3
 * @version ProductLine - Copyright (c) 2013-2016, Dassault Systmes.
 */
public class PLCWidgets_mxJPO  extends PLCWidgetsBase_mxJPO {

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public PLCWidgets_mxJPO (Context context, String[] args)
        throws Exception
    {
    	super(context, args);
    }
}

