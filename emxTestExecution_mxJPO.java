/*
 * emxTestExecution.java
 *
 * Copyright (c) 2005-2016 Dassault Systemes.
 *
 * All Rights Reserved. This program contains proprietary and trade secret
 * information of MatrixOne, Inc. Copyright notice is precautionary only and
 * does not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 */

import com.matrixone.apps.domain.util.EnoviaResourceBundle;

import matrix.db.Context;

/**
 * This JPO contains method pertaining to the Test Execution type.
 *
 * @author Enovia MatrixOne
 * @version ProductCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxTestExecution_mxJPO extends emxTestExecutionBase_mxJPO {

    /**
     * This is default constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the packed arguments
     * @throws Exception
     * @since ProductCentral 10.6
     */
    public emxTestExecution_mxJPO (Context context,
            String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the packed arguments
     * @return int indicating the status 0 - Success
     * @throws Exception when the context is not connected
     * @since ProductCentral 10.6
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String strLanguage = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine","emxProduct.Error.UnsupportedClient",strLanguage);
            throw new Exception(strContentLabel);
        }
        return 0;
    }

}
