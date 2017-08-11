//
// $Id: emxProjectAccessList.java.rca 1.6 Wed Oct 22 16:21:21 2008 przemek Experimental przemek $ 
//
// emxProjectAccessList.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import matrix.db.*;

/**
 * The <code>emxProjectAccessList</code> class represents the Project Access
 * List JPO functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectAccessList_mxJPO extends emxProjectAccessListBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxProjectAccessList_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
