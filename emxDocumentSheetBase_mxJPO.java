//
// $Id: ${CLASSNAME}.java.rca 1.12 Wed Oct 22 16:02:23 2008 przemek Experimental przemek $ 
//
/*
**   emxDocumentSheetBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade
**   secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended
**   publication of such program
**
**  FileName : emxDocumentSheetBase.java
**  Author   : "$Author: przemek $"
**  Version  : "$Revision: 1.12 $"
**  Date     : "$Date: Wed Oct 22 16:02:23 2008 $"
*/

////////////////////////////////////////////////////////////
////Please ignore log comments for debugging purpose    ////
////////////////////////////////////////////////////////////

import com.matrixone.apps.document.DocumentCentralConstants;
import matrix.db.Context;

/**
 * The <code>emxDocumentSheetBase</code> class.
 *
 * @exclude
 */

public class emxDocumentSheetBase_mxJPO extends emxDocumentCentralRoot_mxJPO
  implements DocumentCentralConstants
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public emxDocumentSheetBase_mxJPO(Context context, String[] args)
           throws Exception
    {
        super(args[0]);

        if (args != null && args.length > 0)
        {
            setId(args[0]);
        }
    }

    public emxDocumentSheetBase_mxJPO(String id)
            throws Exception
    {
        // Call the super constructor
        super(id);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */
    public int mxMain(Context context, String[] args)
            throws Exception
    {
        if (true)
        {
            throw new Exception(
                "Must specify method on emxDocumentSheetBase invocation");
        }
        return 0;
    }

}
