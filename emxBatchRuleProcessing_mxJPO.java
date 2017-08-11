/*
 * emxBatchRuleProcessing.java
 *
 * Copyright (c) 2003-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 * v 0.1 2003/02/19 15:00:00 sandeepk_singh Enovia MatrixOne $
 *
 */


import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * @version ProductCentral 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxBatchRuleProcessing_mxJPO extends emxBatchRuleProcessingBase_mxJPO {

    public emxBatchRuleProcessing_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.0.0.0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        String language = context.getSession().getLanguage();
        String strDesktopClientFailed = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Alert.DesktopClientFailed",language);
        if (!context.isConnected())
            throw  new Exception(strDesktopClientFailed);
        return  0;
    }
}
