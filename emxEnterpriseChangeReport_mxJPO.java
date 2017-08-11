/*
 ** emxEnterpriseChangeReport.java
 ** Copyright (c) 2003-2016 Dassault Systemes.
 ** All Rights Reserved
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 ** static const char RCSID[] = "$Id: ${CLASSNAME}.java 1.1 Fri Dec 19 16:45:25 2008 GMT ds-panem Experimental$";
 */

import  matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;

public class emxEnterpriseChangeReport_mxJPO extends emxEnterpriseChangeReportBase_mxJPO{

	/**
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211.HF3
	 * @grade 0
	 */
	public emxEnterpriseChangeReport_mxJPO (Context context, String[] args) throws Exception{
		super(context, args);
	}

	/**
	 * Main entry point
	 *
	 * @param context context for this request
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @exception Exception when problems occurred in the EnterpriseChangeReport
	 * @since EnterpriseChange R211.HF3
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			throw  new Exception(EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Error.UnsupportedClient",context.getSession().getLanguage()));
		}
		return  0;
	}
	
}

