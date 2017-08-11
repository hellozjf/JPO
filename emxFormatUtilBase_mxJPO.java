/*
**   emxFormatUtilBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;
import com.matrixone.apps.domain.util.FormatUtil;

/**
 * The <code>emxFormatUtilBase</code> class to load properties.
 *
 * @version AEF 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxFormatUtilBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public emxFormatUtilBase_mxJPO(Context context, String[] args)
      throws Exception
    {

    }

    /**
     * This method load the Format Viewer values.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public static void cacheFormatViewers(Context context, String[] args)
      throws Exception
    {
        try
        {
            FormatUtil.cacheFormatViewers(context);
        }
        catch (Exception ex)
        {
            throw ex;
        }

    }

}
