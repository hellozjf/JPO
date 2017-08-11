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
 *  This class encapsulates all the document/file information necessary for the
 *  printing/rendering of a file from the matrix STORE.
 *
 *@author     Ashish Shrivastava
 *@created    October 23,2003
 *
 *@since AEF 10.Minor1
 *@grade 0
 */
public class emxPDFDocument_mxJPO extends emxPDFDocumentBase_mxJPO
{
  /**
   *  Constructor for the emxPDFDocumentBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  objectId        Object ID
   *@param  fileName        Name of file to this PDF Document represents
   *@since AEF 10.Minor1
   */
  public emxPDFDocument_mxJPO (Context context, String objectId,String fileName)
  {
    super(context, objectId,fileName);
  }
}
