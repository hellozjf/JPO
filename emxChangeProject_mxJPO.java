//
// $Id: /java/JPOsrc/custom/${CLASSNAME}.java 1.1 Fri Dec 19 16:45:25 2008 GMT ds-panem Experimental$ 
//
// emxECRProject.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
import matrix.db.Context;

/**
 * The <code>emxECRProject</code> class represents the Project Space JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxChangeProject_mxJPO extends emxChangeProjectBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxChangeProject_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

    /**
     * Constructs a new emxECRProject JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param String the business object id
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     */
    public emxChangeProject_mxJPO (String id)
        throws Exception
    {
        // Call the super constructor
        super(id);
    }
}
