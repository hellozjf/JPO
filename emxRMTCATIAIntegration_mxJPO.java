/*
** emxRMTCATIAIntegration
** 
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * This JPO contains the methods required by CATIA RFLP Navigator. 
 * @author: Nicolas Dintzner
 * @version RequirementManagement V6R2012 - Copyright (c) 2010-2016, Dassault Systemes.
 */
public class emxRMTCATIAIntegration_mxJPO extends emxRMTCATIAIntegrationBase_mxJPO
{

	/**
	 * Create a new emxRMTCATIAIntegration object.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return a emxRMTCATIAIntegration object.
	 * @throws Exception if the operation fails
	 * @since RequirementManagement V6R2012
	 * @grade 0
	 */
	public emxRMTCATIAIntegration_mxJPO(Context context, String[] args) throws Exception
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
	 * @since RequirementManagement V6R2012
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
