/*
 ** emxPMCSequenceOrderMigration
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.1.1.2.3.2.2.1 Thu Dec  4 07:56:08 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.1.1.2.3.2 Wed Oct 22 15:49:17 2008 przemek Experimental przemek $
 *
 */

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.i18nNow;


public class emxPMCSequenceOrderMigration_mxJPO extends emxPMCSequenceOrderMigrationBase_mxJPO
{

    /**
         * Create a new ProductCentralMigration object from a given id
         *
         * @param context context for this request
         * @param args holds no arguments
         * @throws Exception when unable to find object id in the AEF
         * @since ProgramCentral V6R2008-1
         */

        public emxPMCSequenceOrderMigration_mxJPO (Context context, String[] args)
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
         * @since  ProgramCentral V6R2008-1
         */
        public int mxMain(Context context, String[] args)
            throws Exception {
            if (!context.isConnected()) {
                i18nNow i18nnow    = new i18nNow();
                String strLanguage = context.getSession().getLanguage();
                String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
                		"emxProgramCentral.Error.UnsupportedClient", strLanguage);
                throw new Exception(strContentLabel);
           }
            super.migrate(context, args);
           return 0;
    }
}
