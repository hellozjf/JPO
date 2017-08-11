/*
**  emxSchemaMapping
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: emxSchemaMapping.java.rca 1.8 Wed Oct 22 16:33:44 2008 przemek Experimental przemek $
*/

import matrix.db.Context;
import java.io.File;

/**
 * The <code>emxSchemaMapping</code> class contains method for the "Collection" Common Component.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxSchemaMapping_mxJPO extends emxSchemaMappingBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */

    public emxSchemaMapping_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */

    public emxSchemaMapping_mxJPO (Context context, File xmlFile, boolean bReload)
        throws Exception
    {
        super(context, xmlFile, bReload);
    }

}
