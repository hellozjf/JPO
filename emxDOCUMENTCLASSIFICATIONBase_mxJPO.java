//
// $Id: ${CLASSNAME}.java.rca 1.7 Wed Oct 22 16:02:14 2008 przemek Experimental przemek $ 
//
/*
 *   emxDOCUMENTCCLASSIFICATIONBase.java
 *
 *   Copyright (c) 1992-2016 Dassault Systemes.
 *   All Rights Reserved.
 *   This program contains proprietary and trade secret information of MatrixOne,
 *   Inc.  Copyright notice is precautionary only
 *   and does not evidence any actual or intended publication of such program
 *
 *   FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *   Author   : "$Author: przemek $"
 *   Version  : "$Revision: 1.7 $"
 *   Date     : "$Date: Wed Oct 22 16:02:14 2008 $"
 *
 */

import matrix.db.Context;


/**
 * The <code>emxDOCUMENTCLASSIFICATIONBase</code> class.
 *
 * @exclude
 */
public class emxDOCUMENTCLASSIFICATIONBase_mxJPO extends emxDocumentCentralCommon_mxJPO
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
    public emxDOCUMENTCLASSIFICATIONBase_mxJPO (Context context, String[] args) throws Exception
    {
        super (args[0]);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Creates a new emxDOCUMENTCLASSIFICATIONBase object.
     *
     * @param id the Java <code>String</code>
     *
     * @throws Exception if the operation fails
     */
    public emxDOCUMENTCLASSIFICATIONBase_mxJPO (String id) throws Exception
    {

        // Call the super constructor

        super (id);
    }

    //~ Methods ----------------------------------------------------------------



    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]<code> object
     *
     * @return the Java <code>int</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception ("must specify method on " +
                                 "emxDOCUMENTCLASSIFICATIONBase invocation");
        }

        return 0;
    }

}
