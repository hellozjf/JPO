//
// $Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.10 $"
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import matrix.db.Context;

/**
 *The Integration to Adlib as a PDF Renderer
 *@author     Ashish Shrivastava
 *@created    October 23,2003
 *
 *@since AEF 10.Minor1
 */
public class emxAdlibRNDIntegration_mxJPO extends emxAdlibRNDIntegrationBase_mxJPO
{
  /**
   *  Constructor for the emxAdlibRNDIntegrationBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  queue    Queue of documents to be rendered
   *
   *@since AEF 9.5.4.0
   */
  public emxAdlibRNDIntegration_mxJPO(Context context, emxPDFQueue_mxJPO queue) {
    super(context, queue);
  }
}
