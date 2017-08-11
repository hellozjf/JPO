 /* emxGetMarkup.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of
 **  MatrixOne, Inc.  Copyright notice is precautionary only and does
 **  not evidence any actual or intended publication of such program.
 **
 **  static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 16:02:37 2008 przemek Experimental przemek $
 */

import matrix.db.Context;

public class emxGetMarkup_mxJPO extends emxGetMarkupBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args that hold objectid"
     *
     * @throws Exception if the operation fails
     *
     * @since AEF
     */
    public emxGetMarkup_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

}
