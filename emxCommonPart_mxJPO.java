/*   emxCommonPart
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the common code for the "Part" business type
**
*/

import matrix.db.Context;

/**
 * The <code>emxCommonPart</code> class contains common code for the "Part" business type
 *
 * @version EC 9.5.JCI.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonPart_mxJPO extends emxCommonPartBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     * @grade 0
     */
    public emxCommonPart_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
}
