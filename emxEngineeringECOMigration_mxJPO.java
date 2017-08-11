/*
 * emxEngineeringECOMigration.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.Context;

/**
 * @version AEF 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxEngineeringECOMigration_mxJPO extends emxEngineeringECOMigrationBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxEngineeringECOMigration_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
