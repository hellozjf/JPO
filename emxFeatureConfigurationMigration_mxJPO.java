/*
 ** emxFeatureConfigurationMigration
 **
 ** Copyright (c) 2007-2016 Dassault Systemes. All Rights Reserved.
 **
 ** This program contains proprietary and trade secret information of Dassault Systemes. 
 ** Copyright notice is precautionary only and does not evidence 
 ** any actual or intended publication of such program.
 **
 */

import matrix.db.Context;
import com.matrixone.apps.domain.util.i18nNow;


public class emxFeatureConfigurationMigration_mxJPO extends emxFeatureConfigurationMigrationBase_mxJPO
{

    /**
         * Create a new Feature Configuration object from a given id
         *
         * @param context context for this request
         * @param args holds no arguments
         * @throws Exception when unable to find object id in the AEF
         * @since Feature Configuration X3
         */

        public emxFeatureConfigurationMigration_mxJPO (Context context, String[] args)
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
         * @since Feature Configuration X3
         */
        public int mxMain(Context context, String[] args)
            throws Exception {
            if (!context.isConnected()) {
                i18nNow i18nnow    = new i18nNow();
                String strLanguage = context.getSession().getLanguage();

                String strContentLabel = i18nnow.GetString("emxProduct.Error.UnsupportedClient",
                                                           "emxProductLineStringResource",
                                                            strLanguage);
                throw new Exception(strContentLabel);
           }
            super.migrate(context, args);
           return 0;
    }
}
