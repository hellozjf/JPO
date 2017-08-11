//
// $Id: emxRisk.java.rca 1.6 Wed Oct 22 16:21:24 2008 przemek Experimental przemek $ 
//
// emxRisk.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import matrix.db.*;

/**
 * The <code>emxRisk</code> class represents the Risk JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxRisk_mxJPO extends emxRiskBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxRisk_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
