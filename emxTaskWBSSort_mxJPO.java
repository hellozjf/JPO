/*
 * emxTaskWBSSort.java
 *
 * Copyright (c) 2005-2016 Dassault Systemes.
 *
 * All Rights Reserved. This program contains proprietary and trade secret
 * information of MatrixOne, Inc. Copyright notice is precautionary only and
 * does not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */

import matrix.db.Context;

/**
 * The <code>emxTaskWBSSort</code> class contains methods for comparision.
 *
 * @author Wipro
 * @version PLC 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxTaskWBSSort_mxJPO extends emxTaskWBSSortBase_mxJPO {

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PLC 10-6
     */

    public emxTaskWBSSort_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     * @since         PLC 10-6
     *
     */

    public emxTaskWBSSort_mxJPO () {
        super();
    }

}
