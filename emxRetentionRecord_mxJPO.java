/*
 *  emxRetentionRecord.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.2.1.4 Wed Oct 22 16:02:37 2008 przemek Experimental przemek $
 */
import matrix.db.*;
import java.lang.*;
/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxRetentionRecord_mxJPO extends emxRetentionRecordBase_mxJPO
{

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public emxRetentionRecord_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }
}
