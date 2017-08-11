//
// $Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:20 2008 przemek Experimental przemek $ 
//
/*
 *   emxDOCUMENTCCLASSIFICATION.java
 *
 *   Copyright (c) 1992-2016 Dassault Systemes.
 *   All Rights Reserved.
 *   This program contains proprietary and trade secret information of MatrixOne,
 *   Inc.  Copyright notice is precautionary only
 *   and does not evidence any actual or intended publication of such program
 *
 *   FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *   Author   : "$Author: przemek $"
 *   Version  : "$Revision: 1.8 $"
 *   Date     : "$Date: Wed Oct 22 16:02:20 2008 $"
 */

import matrix.db.Context;

/**
 * The <code>emxDOCUMENTCLASSIFICATION</code> represents the DOCUMENT
 * CLASSIFICATION type in DC Schema
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxDOCUMENTCLASSIFICATION_mxJPO  extends emxDOCUMENTCLASSIFICATIONBase_mxJPO
{

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public emxDOCUMENTCLASSIFICATION_mxJPO (Context context, String[] args) throws Exception
    {

        // Call the super constructor

        super (context, args);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Creates a new emxDOCUMENTCCLASSIFICATION object.
     *
     * @param id String objectId
     *
     * @throws Exception
     */

    public emxDOCUMENTCLASSIFICATION_mxJPO (String id) throws Exception
    {

        // Call the super constructor

        super (id);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     *
     * @return nothing
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                    "must specify method on emxDOCUMENTCLASSIFICATION invocation"
            );
        }

        return 0;
    }
}
