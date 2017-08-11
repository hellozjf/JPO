/*
**  emxAppVersionUtil
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

/**
 * The <code>emxAppVersionUtil</code> class contains method for the "Collection" Common Component.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxAppVersionUtil_mxJPO extends emxAppVersionUtilBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sAppInfoFile AppInfo.rul file name
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    @Deprecated
    public emxAppVersionUtil_mxJPO (Context context, String sApplicationName, String sAppInfoFile)
        throws Exception
    {
        super(context, sApplicationName, sAppInfoFile);
    }

    public emxAppVersionUtil_mxJPO (Context context, String sApplicationName, String sAppInfoFile, String schemaName)
        throws Exception
    {
        super(context, sApplicationName, sAppInfoFile, schemaName);
    }
}
