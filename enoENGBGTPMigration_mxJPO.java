/*
 * enoENGBGTPMigrationBase.java
 * Program to update the Unit of Measure Type and values on Parts and 
 * EBOM, EBOM Pending, EBOM History, EBOM Substiture relationships
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

public class enoENGBGTPMigration_mxJPO extends enoENGBGTPMigrationBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public enoENGBGTPMigration_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
