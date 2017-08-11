/*
**  emxConversionManifestFile
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
 * The <code>emxConversionManifestFile</code> class contains method for the "Collection" Common Component.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxConversionManifestFile_mxJPO extends emxConversionManifestFileBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param conMftFile holds conversion manifest file handle
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxConversionManifestFile_mxJPO (Context context, File conMftFile)
        throws Exception
    {
        super(context, conMftFile);
    }
}
