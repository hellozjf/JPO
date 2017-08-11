/*
 ** emxRMTSequenceOrderDataMigration
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 *
 */

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


public class emxRMTSequenceOrderDataMigration_mxJPO extends emxRMTSequenceOrderDataMigrationBase_mxJPO
{

    /**
         * Create a new emxRMTSequenceOrderDataMigration object from a given id
         *
         * @param context context for this request
         * @param args holds no arguments
         * @throws Exception when unable to find object id in the AEF
         * @since RequirementsManagement V6R2008-2
         */

        public emxRMTSequenceOrderDataMigration_mxJPO (Context context, String[] args)
            throws Exception {
          super(context, args);
        }


}
