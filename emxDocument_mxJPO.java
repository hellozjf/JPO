/*
**  emxDocument
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc. Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;

/**
 * The <code>emxDocument</code> class contains methods for document.
 *
 * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxDocument_mxJPO extends emxDocumentBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *     0 - String that holds the document object id.
     * @throws Exception if the operation fails
     * @since EC 10.0.0.0
     */

    public emxDocument_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

}
