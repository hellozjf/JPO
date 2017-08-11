/*
 * emxProductLine
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


import  matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * This JPO class has some method pertaining to Product Line type
 * @version ProductCentral 10-0-0-0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxProductLine_mxJPO extends emxProductLineBase_mxJPO {

    /**
     * Create a new emxProductLine object from a given id
     *
     * @param context context for this request
     * @param arg[0] the objectid
     * @return a emxProductLine
     * @exception Exception when unable to find object in the ProductCentral
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxProductLine_mxJPO (Context context, String[] args) throws Exception
    {
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
     * @grade 0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Alert.FeaturesCheckFailed",language);
            throw  new Exception(strContentLabel);
        }
        return  0;
    }
}






