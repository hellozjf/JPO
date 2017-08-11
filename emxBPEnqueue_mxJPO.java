//
// $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:53:02 2008 przemek Experimental przemek $ 
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
 *  Date     : "$Date: Wed Oct 22 15:53:02 2008 $"
 *
 */
import matrix.db.Context;

/**
 *  Main Batch Printing JPO.  Invoked from a JavaBean, and calls it's
 *  implementation class
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@since AEF 9.5.4.0
 */
public class emxBPEnqueue_mxJPO extends emxBPEnqueueBase_mxJPO
{
  /**
   *  Constructor for the emxBPEnqueue object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  args           Contains a map that has been packed by JPO.packArgs
   *                       That map Contains:
   *                       "objectId" - String, Object Id
   *                       "printerName" - String, the printer's name to print
   *                       "numberOfCopies" - Integer, number of copies to print
   *                       "fileList" - LinkedList, the list of files to print
   *@exception  Exception  IllegalArgumentException, if wrong args are passed in
   *
   *@since AEF 9.5.4.0
   */
  public emxBPEnqueue_mxJPO (Context context, String[] args)
    throws Exception
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    super(context, args);
  }
}
