//
// $Id: ${CLASSNAME}.java.rca 1.13 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $
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
 *  Version  : "$Revision: 1.13 $"
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import matrix.db.Context;

import com.matrixone.apps.domain.util.MqlUtil;
/**
 *  This class encapsulates all the document/file information necessary for the
 *  printing/rendering of a file from the matrix STORE.
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *  @exclude
 */
public class emxPDFDocumentBase_mxJPO
{
  /**  Matrix Context */
  protected Context _context;

  /**  Object ID */
  protected String _objectId;
  /**  Full Matrix STORE file name */
  protected String _fileName;


  /**
   *  Constructor for the emxPDFDocumentBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  objectId        Object ID
   *@param  fileName        Name of file to this PDF Document represents
   *
   *@since AEF 9.5.4.0
   */
  public emxPDFDocumentBase_mxJPO(Context context, String objectId, String fileName)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //this._context = context;

    try {
      Context framecontext = new Context(null, context, null);
      framecontext.instantiateContext();
      this._context = framecontext;
    } catch (Exception e) {
    }
    this._objectId = objectId;
    this._fileName = fileName;
  }


  /**
   *  Checks the file out to /directory/
   *
   *@param  directory      Directory to check out to
   *@exception  Exception  Exception on checkout failure,
   *
   *@since AEF 9.5.4.0
   */
  public void checkOutFile(String directory)
    throws Exception
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */

    // create a new context for every thread
    Context frameContext = null;

    try {
            frameContext = new Context(null, _context, null);
            frameContext.instantiateContext();
    } catch (Exception e) {
    }

    //Check out file
    String cmd      = "checkout bus $1 server format $2 file $3 directory $4 ";
    String test     = MqlUtil.mqlCommand(_context, cmd, _objectId, getFormat(), getFileName(), directory);
  }


  /**
   *  Gets the fileName attribute of the emxPDFDocumentBase object
   *
   *@return    The fileName value
   *
   *@since AEF 9.5.4.0
   */
  public String getFileName()
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //We only need the file name, and not the whole FILE STORE identifier
    return _fileName.substring(_fileName.lastIndexOf(":") + 1);
  }


  /**
   *  Gets the fileName attribute of the emxPDFDocumentBase object
   *
   *@return    The fileName value
   *
   *@since AEF 9.5.4.0
   */
  public String getFormat()
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //We only need the file name, and not the whole FILE STORE identifier

    return _fileName.substring(0, _fileName.lastIndexOf("|"));
  }


  /**
   *  Gets the objectId attribute of the emxPDFDocumentBase object
   *
   *@return    The objectId value
   *
   *@since AEF 9.5.4.0
   */
  public String getObjectId()
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    return _objectId;
  }
}

