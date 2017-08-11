/*
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 *  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.6 Wed Oct 22 16:02:11 2008 przemek Experimental przemek $";
 **
 */

import matrix.db.Context;


/**
 * The <code>emxLibraryCentralClassificationRule</code> class contains utility methods for
 * disconnecting the non released revisions
 *
 * @version LC 10.6.0.1 - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxLibraryCentralClassificationRule_mxJPO extends emxLibraryCentralClassificationRuleBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public emxLibraryCentralClassificationRule_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

}
