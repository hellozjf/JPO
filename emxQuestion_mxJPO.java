//
// $Id: emxQuestion.java.rca 1.6 Wed Oct 22 16:21:24 2008 przemek Experimental przemek $ 
//
// emxQuestion.java
//
// Copyright (c) 2002-2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import matrix.db.*;

/**
 * The <code>emxQuestion</code> class represents the Question JPO
 * functionality for the AEF type.
 *
 * @version AEF 10.0.SP4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxQuestion_mxJPO extends emxQuestionBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxQuestion_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
