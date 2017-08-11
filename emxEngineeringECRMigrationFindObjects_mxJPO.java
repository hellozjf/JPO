/*
 * emxEngineeringSubstituteMigrationFindObjects.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.2.3.2.2.1 Fri Dec  5 04:07:44 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.2.3.2 Wed Oct 22 15:52:01 2008 przemek Experimental przemek $
 */

import matrix.db.Context;

/**
 * @version AEF 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxEngineeringECRMigrationFindObjects_mxJPO extends emxEngineeringECRMigrationFindObjectsBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxEngineeringECRMigrationFindObjects_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
