/**   emxProjectReport.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of
**   MatrixOne, Inc.  Copyright notice is precautionary only and does
**   not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.2.1.1.3.4.2.1 Thu Dec  4 07:56:11 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.2.1.1.3.4 Wed Oct 22 15:49:45 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

/**
 * The <code> emxProjectReport</code> class contains code for the
 * Governed Items,Folder Content Report,Deliverables Report.
 * @author Tanwir Fatima
 * @version PMC 10.6.SP2 - Copyright (c) 2006, MatrixOne, Inc.
 */
public class emxProjectReport_mxJPO extends emxProjectReportBase_mxJPO
{



  /**
   * Constructor.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since PMC 10-6.SP2
   * @grade 0
   */
  public emxProjectReport_mxJPO (Context context, String[] args)
	  throws Exception
  {
	  super(context, args);
  }



  /**
   * This method is executed if a specific method is not specified.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns int
   * @throws Exception if the operation fails
   * @since PMC PMC 10-6.SP2
   */
  public int mxMain(Context context, String[] args)
	  throws Exception
  {
	  if (true)
	  {
		  throw new Exception("must specify method on emxProjectReport invocation");
	  }
	  return 0;
  }



}
