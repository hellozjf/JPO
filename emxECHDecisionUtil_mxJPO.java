/*
 ** emxECHDecisionUtil.java
 ** Copyright (c) 2003-2016 Dassault Systemes.
 ** All Rights Reserved
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 ** static const char RCSID[] = "$Id: /java/JPOsrc/custom/${CLASSNAME}.java 1.1 Fri Dec 19 16:45:25 2008 GMT ds-panem Experimental$";
 */

import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;


/**
 * This JPO class has methods pertaining to Decision type extended by Enterprise Change
 * @version EnterpriseChange R212 - Copyright (c) 2008, ENOVIA, Inc.
 */
public class emxECHDecisionUtil_mxJPO extends emxECHDecisionUtilBase_mxJPO{

	/**
	 * Create a new emxECHDecisionUtil object from a given id
	 *
	 * @param context context for this request
	 * @param arg[0] the object id
	 * @exception Exception when unable to instantiate object in the ECHDecisionUtil
	 * @since EnterpriseChange R212
	 * @grade 0
	 */
	public emxECHDecisionUtil_mxJPO (Context context, String[] args) throws Exception{
		super(context, args);
	}

	/**
	 * Main entry point
	 *
	 * @param context context for this request
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @exception Exception when problems occurred in the DecisionUtil
	 * @since EnterpriseChange R212
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception{
		if(!context.isConnected()){
			String language = context.getSession().getLanguage();
			String strContentLabel = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Error.UnsupportedClient", language);
			throw  new Exception(strContentLabel);
		}
		return  0;
	}
}


