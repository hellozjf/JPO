/*
**  emxJob.java
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne Inc.
**  Copyright notice is precautionary only  and does not evidence
**  any actual or intended publication of such program
**
*/

import matrix.db.Context;


/**
 * The <code>Job</code> class represents Job JPO in common
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2006, MatrixOne, Inc.
 */
public class emxJob_mxJPO extends emxJobBase_mxJPO
{

    /**
     * Constructs a new JobBase JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 11.0.0.0
     */
    public  emxJob_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super(context, args);
    }


}
