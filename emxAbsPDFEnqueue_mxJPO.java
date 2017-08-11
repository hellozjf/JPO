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

/**
 *  Main Batch Printing JPO.  Invoked from a JavaBean, and calls it's
 *  implementation class
 *
 *@author     Ashish Shrivastava
 *@created    October 23,2003
 *
 *@since AEF 10.Minor1
 */
public class emxAbsPDFEnqueue_mxJPO extends emxAbsPDFEnqueueBase_mxJPO
{
  /**
   *  Constructor for the emxAbsEnqueueImpl object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param args holds no arguments
   *
   *@since AEF 10.Minor1
   */
  public emxAbsPDFEnqueue_mxJPO (Context context, String[] args)
  {
    super(context, args);
  }
}
