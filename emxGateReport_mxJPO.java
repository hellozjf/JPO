/** ${CLASSNAME}

   Copyright (c) 1999-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program
   @since Program Central R210
   @author NR2
*/

import matrix.db.Context;
import matrix.util.MatrixException;

/**
 * The <code>emxGateReport</code> class represents the JPO to process Project
 * Hold and Cancel states.
 * 
 * @version PRG R210 - Copyright (c) 2010, MatrixOne, Inc.
 * @author NR2
 */
public class emxGateReport_mxJPO extends emxGateReportBase_mxJPO
{
    /**
     * Constructs 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since PRG R210
	 * @author NR2
     */
    public emxGateReport_mxJPO (Context context, String[] args) throws Exception
    {
        // Call the super constructor
        super(context,args);
    }
}

