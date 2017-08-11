/*
 *  emxResourcePlan.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: emxProjectMember.java.rca 1.6 Wed Apr  2 16:08:07 2008 przemek Experimental przemek $
 */
import matrix.db.*;

/**
 * The <code>emxResourcePlan</code> class represents the Resource Plan JPO
 * functionality for the PMC type.
 *
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxResourcePlan_mxJPO extends emxResourcePlanBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxResourcePlan_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }

}
