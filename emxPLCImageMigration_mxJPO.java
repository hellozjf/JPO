/*
**  emxPLCImageMigration106SP2.java
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/

import  matrix.db.Context;
import  com.matrixone.apps.framework.ui.UINavigatorUtil;


/**
 * The <code>emxPLCImageMigration106SP2Base</code> class contains migration script for PLCV10-6-SP2 Data
 * @author Mayukh,Enovia MatrixOne
 * @version ProductCentral 10.6SP2 - Copyright (c) 2005, MatrixOne, Inc.
 *
 */

public class emxPLCImageMigration_mxJPO extends emxPLCImageMigrationBase_mxJPO


{
    //Defining the static variable
    protected static final String ERROR_UNSUPPORTEDCLIENT = "emxProduct.Error.UnsupportedClient";

    /**
    * Create a new emxPLCImageMigration106SP2 object from a given id.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return a emxPLCImageMigration106SP2 Object
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6.SP2
    */

    public emxPLCImageMigration_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

}
