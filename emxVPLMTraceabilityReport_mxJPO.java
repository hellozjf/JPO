/*
** emxVPLMTraceabilityReport
**
** Copyright (c) 2007 MatrixOne, Inc.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

import matrix.db.Context;
import com.matrixone.apps.domain.util.i18nNow;


/**
 * This JPO class has some methods pertaining to Traceability Reports.
 * @author: Brian Casto
 * @version ProductCentral 10.7 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxVPLMTraceabilityReport_mxJPO extends emxVPLMTraceabilityReportBase_mxJPO
{

	/**
	 * Create a new emxVPLMTraceabilityReport object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return a emxVPLMTraceabilityReport object.
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.7.1.0
	 * @grade 0
	 */
	public emxVPLMTraceabilityReport_mxJPO(Context context, String[] args) throws Exception
	{
		super(context, args);
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if the operation fails
	 * @since ProductCentral 10.7.1.0
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception
	{
		if (!context.isConnected())
		{
			i18nNow i18nnow = new i18nNow();
			String language = context.getSession().getLanguage();
			String strContentLabel = i18nnow.GetString("emxRequirementsStringResource", language, "emxRequirements.Alert.FeaturesCheckFailed");
			throw  new Exception(strContentLabel);
		}
		return  0;
	}

}//END of class
