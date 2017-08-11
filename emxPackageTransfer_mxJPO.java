/*   emxPackageTransfer.
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program.
**
**   This JPO contains the implementation of emxPart.
**
*/

import matrix.db.Context;

/**
 * The <code>emxPackageTransfer</code> class contains common code for the FCS enabled chacking.
 *
 * @version EC 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */

@Deprecated
public class emxPackageTransfer_mxJPO extends emxPackageTransferBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since Common 10-5.
     */
    public emxPackageTransfer_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
