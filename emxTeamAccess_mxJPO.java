/*
 *  emxTeamAccess.java
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
import java.lang.*;

/**
 * @version AEF 10-Minor1 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxTeamAccess_mxJPO extends emxTeamAccessBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10-Minor1
     */
    public emxTeamAccess_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
