/*
**   emxCommonInitiateRoute.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import com.matrixone.apps.common.util.ComponentsUtil;

/**
 * The <code>emxCommonInitiateRoute</code> class contains 
 *
 * @version AEF 10 Minor 1 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxCommonInitiateRoute_mxJPO extends emxCommonInitiateRouteBase_mxJPO
{


   /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10 Minor 1
     * @grade 0
     */
    public emxCommonInitiateRoute_mxJPO (Context context, String[] args)
        throws Exception
    {
     super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AEF 10 Minor 1
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonInitiateRoute.SpecifyMethodOnInitiateRouteInvocation", context.getLocale().getLanguage()));
        }
        return 0;
    }

    
}// eof class
