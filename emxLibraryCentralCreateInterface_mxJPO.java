/*
**  emxLibraryCentralCreateInterface
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.6 Wed Oct 22 16:02:38 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

/**
 * The <code>emxLibraryCentralCreateInterface</code> class contains methods for the "Search" component.
 *
 * @version AEF 10.5.0.0 - Copyright(c) 2003, MatrixOne, Inc.
 */

public class emxLibraryCentralCreateInterface_mxJPO extends emxLibraryCentralCreateInterfaceBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.0.0
     */

    public emxLibraryCentralCreateInterface_mxJPO (Context context, String[] args)
    	throws Exception
    {
		super(context, args);
    }

}
