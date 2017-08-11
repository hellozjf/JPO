/*
**   emxPropertyUtilBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 * The <code>emxPropertyUtilBase</code> class has property utility methods.
 *
 * @version AEF 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxPropertyUtilBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.0.0
     */

    public emxPropertyUtilBase_mxJPO(Context context, String[] args)
      throws Exception
    {

    }

    /**
     * This method loads the Admin Property values
     * The properties must be on an admin object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.0.0
     */

    public static void cacheSymbolicNames(Context context, String[] args)
      throws Exception
    {
        try
        {
            PropertyUtil.cacheSymbolicNames(context);
        }
        catch (Exception ex)
        {
            throw ex;
        }

    }

}
