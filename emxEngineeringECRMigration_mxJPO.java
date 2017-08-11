/*
 * emxEngineeringECROMigration.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = "$Id: ${CLASSNAME}.java.rca 1.2.3.2.2.1 Fri Dec  5 04:07:46 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.2.3.2 Wed Oct 22 15:51:46 2008 przemek Experimental przemek $"
 */

import matrix.db.Context;

/**
 * @version AEF 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxEngineeringECRMigration_mxJPO extends emxEngineeringECRMigrationBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxEngineeringECRMigration_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
