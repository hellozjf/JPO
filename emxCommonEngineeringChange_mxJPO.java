/*
 *  emxCommonEngineeringChange.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;

/**
 * The <code>emxCommonEngineeringChange</code> JPO class is Wrapper JPO for emxCommonEngineeringChangeBase JPO which holds methods
 * for executing JPO operations related to objects of the type Engineering Change.
 * @author  Wipro
 * @version Common 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxCommonEngineeringChange_mxJPO extends emxCommonEngineeringChangeBase_mxJPO {
    /**
     * Create a new Engineering Change object from a given id
     *
     * @param context context for this request
     * @param args    holds no arguments
     * @throws        Exception when unable to find object id in the AEF
     * @since         Common 10-6
     */

    public emxCommonEngineeringChange_mxJPO (Context context, String[] args)
        throws Exception {
      super(context, args);
    }


    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args    holds no arguments
     * @return        an integer status code (0 = success)
     * @throws        Exception when problems occurred in the AEF
     * @since         Common 10-6
     */
    public int mxMain(Context context, String[] args)
        throws Exception {
        if (!context.isConnected()) {
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Error.UnsupportedClient");
            throw new Exception(strContentLabel);
       }
       return 0;
    }
}
