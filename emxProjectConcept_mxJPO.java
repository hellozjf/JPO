//
// $Id: emxProjectConcept.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $ 
//
// emxProjectConcept.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import matrix.db.*;

/**
 * The <code>emxProjectConcept</code> class represents the Project Concept JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxProjectConcept_mxJPO extends emxProjectConceptBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxProjectConcept_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
