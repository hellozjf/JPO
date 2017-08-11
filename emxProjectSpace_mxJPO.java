//
// $Id: emxProjectSpace.java.rca 1.6 Wed Oct 22 16:21:26 2008 przemek Experimental przemek $ 
//
// emxProjectSpace.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
import matrix.db.*;

/**
 * The <code>emxProjectSpace</code> class represents the Project Space JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectSpace_mxJPO extends emxProjectSpaceBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxProjectSpace_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * Constructs a new emxProjectSpace JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param String the business object id
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     */
    public emxProjectSpace_mxJPO (String id)
        throws Exception
    {
        // Call the super constructor
        super(id);
    }
}
