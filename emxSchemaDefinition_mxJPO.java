/*
**  emxSchemaDefinition
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

import java.io.File;

/**
 * The <code>emxSchemaDefinition</code> class contains method for the "Collection" Common Component.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxSchemaDefinition_mxJPO extends emxSchemaDefinitionBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */

    public emxSchemaDefinition_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * Constructor.
     *
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */

    public emxSchemaDefinition_mxJPO ()
        throws Exception
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param schemaDefFile holds schema definition file handle
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxSchemaDefinition_mxJPO (Context context, File schemaDefFile, emxAppVersionUtil_mxJPO appVersions)
        throws Exception
    {
        super(context, schemaDefFile, appVersions);
    }

}
