//
// $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/custom/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
//
/*
 *  emxPortfolio.java
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
 * This JPO class has some method pertaining to Portfolio type
 * @version ProductCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxPortfolio_mxJPO extends emxPortfolioBase_mxJPO
{

    /**
         * Create a new Porfolio object from a given id
         *
         * @param context context for this request
         * @param args holds no arguments
         * @throws Exception when unable to find object id in the AEF
         * @since AEF10.6
         */

        public emxPortfolio_mxJPO (Context context, String[] args)
            throws Exception {
          super(context, args);
        }


        /**
         * Main entry point
         *
         * @param context context for this request
         * @param args holds no arguments
         * @return an integer status code (0 = success)
         * @throws Exception when problems occurred in the AEF
         * @since  AEF10.6
         */
        public int mxMain(Context context, String[] args)
            throws Exception {
            if (!context.isConnected()) {
                i18nNow i18nnow    = new i18nNow();
                String strLanguage = context.getSession().getLanguage();

                String strContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.UnsupportedClient",strLanguage);
                throw new Exception(strContentLabel);
           }
           return 0;
    }
}
