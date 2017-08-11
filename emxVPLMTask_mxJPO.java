//
// $Id: ${CLASSNAME}.java.rca 1.1.1.4.2.2 Thu Dec  4 07:56:08 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.4.2.1 Thu Dec  4 01:54:59 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.4 Wed Oct 22 15:50:25 2008 przemek Experimental przemek $
//
// emxVPLMTask.java
//
// Copyright (c) 2007-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import matrix.db.*;

/**
 * The <code>emxVPLMTask</code> class represents the VPLM Task JPO
 * functionality for the AEF type VPLM Task.
 *
 * @version AEF 10.7.SP1 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxVPLMTask_mxJPO extends emxVPLMTaskBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10-7-SP1
     */

    public emxVPLMTask_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

}
