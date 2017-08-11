/*   emxWhatIfBase
 **
 **   Copyright (c) 2003-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   This JPO contains the implementation of emxWorkCalendar
 **
 **   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9.2.2 Thu Dec  4 07:55:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9.2.1 Thu Dec  4 01:53:19 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:49:37 2008 przemek Experimental przemek $
 */


import matrix.db.Context;

/**
 * The <code>emxWhatIfBase</code> class contains methods for Experiment.
 *
 * @version PMC 10.5.1.2 - Copyright(c) 2013, MatrixOne, Inc.
 */

public class emxValidation_mxJPO extends emxValidationBase_mxJPO
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public emxValidation_mxJPO()throws Exception
	{
		super();
	}
	
	public emxValidation_mxJPO(Context context,String[]args)throws Exception
	{
		super(context,args);
	}
}

