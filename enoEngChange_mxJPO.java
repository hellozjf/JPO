/*
 *  emxChange.java
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
import com.matrixone.apps.domain.util.i18nNow;

/**
 * The <code>emxChange</code> JPO class is Wrapper JPO for emxChangeBase JPO which holds methods
 * for executing JPO operations related to objects of the type Change.
 * @author  Cambridge
 * @version Engineering Central - X3  - Copyright (c) 2007, MatrixOne, Inc.
 */
public class enoEngChange_mxJPO extends enoEngChangeBase_mxJPO {
    public enoEngChange_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}


    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args    holds no arguments
     * @return        an integer status code (0 = success)
     * @throws        Exception when problems occurred in the AEF
     * @since         EngineeringCentral X3
     */
    public int mxMain(Context context, String[] args)
        throws Exception {
        if (!context.isConnected()) {
            i18nNow i18nnow    = new i18nNow();
            String strLanguage = context.getSession().getLanguage();

            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.Error.UnsupportedClient");
            
            throw new Exception(strContentLabel);
       }
       return 0;
    }
}
