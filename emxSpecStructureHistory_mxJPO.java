/*
** emxSpecStructureHistory
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
 * This JPO class has some methods pertaining to the Spec Structure History Reports.
 * @author: Brian Casto
 * @version RequirementManagement V6R2009x - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxSpecStructureHistory_mxJPO extends emxSpecStructureHistoryBase_mxJPO
{

	/**
	 * Create a new emxSpecStructureHistory object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return a emxSpecStructureHistory object.
	 * @throws Exception if the operation fails
	 * @since RequirementManagement V6R2009x
	 * @grade 0
	 */
	public emxSpecStructureHistory_mxJPO(Context context, String[] args) throws Exception
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
