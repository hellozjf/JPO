/*
 *  emxOrganization.java
 *
 * Copyright (c) 1992-2015 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: emxOrganization.java.rca 1.6 Wed Oct 22 16:21:25 2008 przemek Experimental przemek $
 */
import matrix.db.*;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxOrganization_mxJPO extends emxOrganizationBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.SP4
     * @grade 0
     */
    public emxOrganization_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
