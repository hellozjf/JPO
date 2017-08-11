//
// $Id: ${CLASSNAME}.java.rca 1.11 Wed Oct 22 15:53:04 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.11 $"
 *  Date     : "$Date: Wed Oct 22 15:53:04 2008 $"
 *
 */
import matrix.db.Context;

/**
 *  This class is a catch all class for utility methods.  It contains what
 *  should be static methods, (but they can't be called from the JPO
 *  environment)  instantiate this class, and you can then call any of the
 *  utility functions it contains.
 *
 *@author     Ashish Shrivastava
 *@created    October 23,2003
 *
 *@since AEF 10.Minor1
 */
public class emxPDFUtil_mxJPO extends emxPDFUtilBase_mxJPO
{
  /**
   *  Constructor for the emxPDFUtilBase object
   *
   *@param  objectId  Object Id
   *
   *@since AEF 10.Minor1
   */
  public emxPDFUtil_mxJPO() {
  }

  /**
   *  Constructor for the emxPDFUtil object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  args   Contains a map that has been packed by JPO.packArgs
   *               That map Contains:
   *               "objectId" - String, Object Id
   *               "printerName" - String, the printer's name to print
   *               "numberOfCopies" - Integer, number of copies to print
   *               "fileList" - LinkedList, the list of files to print
   *@exception  Exception  IllegalArgumentException, if wrong args are passed in
   *
   *@since AEF 10.Minor1
   */
  public emxPDFUtil_mxJPO (Context context,
                       String[] args)
    throws Exception
  {
    super(context, args);
  }
}
