//
// $Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 15:53:02 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.8 $"
 *  Date     : "$Date: Wed Oct 22 15:53:02 2008 $"
 *
 */
import java.util.Iterator;
import java.util.LinkedList;
import matrix.db.Context;

/**
 *  Main PDF Rendering JPO.  This is the implementation class
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@exclude
 */
public class emxAbsPDFEnqueueBase_mxJPO {
  /**  Object Id */
  protected String _objectId;
  /**  List of Files */
  protected LinkedList _fileList;

  /**  Utility Class */
  protected emxPDFUtil_mxJPO _util = new emxPDFUtil_mxJPO();


  /**
   *  Constructor for the emxAbsEnqueueImpl object
   *
   *@param  context        Matrix Context
   *@param  args           null
   *@exception  Exception  IllegalArgumentException, if wrong args are passed in
   *
   *@since AEF 9.5.4.0
   */
  public emxAbsPDFEnqueueBase_mxJPO(Context context, String[] args) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
  }


  /**
   *  Unpacks the passed in arguments, and uses them to set class properties
   *
   *@param  args           Contains a map that has been packed by JPO.packArgs
   *                       That map Contains:
   *                       "objectId" - String, Object Id
   *                       "fileList" - LinkedList, the list of files to print
   *@exception  Exception  IllegalArgumentException, if wrong args are passed in
   *
   *@since AEF 9.5.4.0
   */
  protected void unpackArgs(String[] args) throws Exception {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
  }


  /**
   *  Main execution method from a JavaBean
   *  This will unpack any args, and then it will invoke the protected
   *  enqueue method.  That method will build the PDFQueue, PDFDocuments
   *  and pass them to the integration
   *
   *@param context   the eMatrix <code>Context</code> object
   *@param args      holds no arguments
   *@return int      success code
   *
   *@since AEF 9.5.4.0
   */
  public int execute(Context context, String[] args) throws Exception {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    if (args == null || args.length < 1) {
      throw (new IllegalArgumentException());
    }

    unpackArgs(args);
    return execute(context, _objectId, _fileList);
  }


  /**
   *  This method will build the PDFQueue, PDFDocuments
   *  and pass them to the integration
   *
   *@param context         the eMatrix <code>Context</code> object
   *@param  objectId       Object Id
   *@param  fileList       List of Files to execute
   *@return int            success code
   *
   *@since AEF 9.5.4.0
   */
  protected int execute(Context context, String objectId, LinkedList fileList) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    emxPDFQueue_mxJPO queue = createPDFQueue(objectId);
    Iterator itr = fileList.iterator();
    while (itr.hasNext()) {
      String fileName = (String) itr.next();
      queue.add(createPDFDocuments(context, fileName, objectId));
    }
    runIntegration(context, queue);
    return 0;
  }


  /**
   *  Creates a PDF queue
   *
   *@param   objectId    Object Id
   *@return              a new PDF Queue
   *
   *@since AEF 9.5.4.0
   */
  protected emxPDFQueue_mxJPO createPDFQueue(String objectId) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    return new emxPDFQueue_mxJPO(objectId);
  }


  /**
   *  Creates a PDF Document
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  fileName       name of the file
   *@param  objectId       Object Id
   *@return                a new PDF Document
   *
   *@since AEF 9.5.4.0
   */
  protected emxPDFDocument_mxJPO createPDFDocuments(Context context,
                                                      String fileName,
                                                      String objectId) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    return new emxPDFDocument_mxJPO(context, objectId, fileName);
  }


  /**
   *  runs the appropriate integration
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  queue          The PDF Queue with
   *
   *@since AEF 9.5.4.0
   */
  protected void runIntegration(Context context, emxPDFQueue_mxJPO queue) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
  }
}

