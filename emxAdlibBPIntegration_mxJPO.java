//
// $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $ 
//
/*
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 *  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *  Author   : "$Author: przemek $"
 *  Version  : "$Revision: 1.9 $"
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import matrix.db.Context;

  public class emxAdlibBPIntegration_mxJPO extends emxAdlibBPIntegrationBase_mxJPO
  {
  /**
   *  Constructor for the emxAdlibBPIntegrationBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  queue           Queue of documents to be printed
   *@param  printerName     Printer Name
   *@param  numberOfCopies  Number of copies to be printed
   *
   *@since AEF 10.Minor1
   */
  public emxAdlibBPIntegration_mxJPO(Context context, emxPDFQueue_mxJPO queue,
    String printerName, int numberOfCopies)
  {
    super(context, queue,printerName,numberOfCopies);
  }
}
