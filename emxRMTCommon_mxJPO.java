/*
** emxRMTCommon
**
** Copyright (c) 2007 MatrixOne, Inc.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * This JPO class has some methods pertaining to the generic RMT usage
 * @author: Brian Casto
 * @version RequirementManagement V6R2009x - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxRMTCommon_mxJPO extends emxRMTCommonBase_mxJPO
{

	/**
	 * Create a new emxRMTCommon object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return a emxRMTCommon object.
	 * @throws Exception if the operation fails
	 * @since RequirementManagement V6R2009x
	 * @grade 0
	 */
	public emxRMTCommon_mxJPO(Context context, String[] args) throws Exception
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
	 * @since RequirementManagement V6R2009x
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception
	{
		if (!context.isConnected())
		{
			String language = context.getSession().getLanguage();
			String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed");
			throw  new Exception(strContentLabel);
		}
		return  0;
	}

}//END of class
