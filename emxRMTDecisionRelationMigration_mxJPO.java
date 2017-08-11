/*
 ** emxRMTDecisionRelationMigration
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /java/JPOsrc/custom/${CLASSNAME}.java 1.2 Wed Dec 17 14:41:10 2008 GMT ds-bcasto Experimental$
 *
 */

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


public class emxRMTDecisionRelationMigration_mxJPO extends emxRMTDecisionRelationMigrationBase_mxJPO
{

    /**
         * Create a new emxRMTDecisionRelationMigration object from a given id
         *
         * @param context context for this request
         * @param args holds no arguments
         * @throws Exception when unable to find object id in the AEF
         * @since RequirementsManagement R207
         */
	public emxRMTDecisionRelationMigration_mxJPO (Context context, String[] args)
	throws Exception 
	{
	  super(context, args);
	}


    /**
         * Main entry point
         *
         * @param context context for this request
         * @param args holds no arguments
         * @return an integer status code (0 = success)
         * @throws Exception when problems occurred in the AEF
         * @since RequirementsManagement R207
         */
	public int mxMain(Context context, String[] args)
	throws Exception 
	{
		if (!context.isConnected()) 
		{
			String strLanguage = context.getSession().getLanguage();
			String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed");
			throw new Exception(strContentLabel);
		}
		super.migrate(context, args);
		return 0;
	}
}
