//
// $Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 15:53:02 2008 przemek Experimental przemek $ 
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
 *  Date     : "$Date: Wed Oct 22 15:53:02 2008 $"
 *
 */
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;

/**
 *  Main PDF Rendering JPO.  This is the implementation class
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@exclude
 */
public class emxRNDEnqueueBase_mxJPO extends emxAbsPDFEnqueue_mxJPO {

  /**
   *  Constructor for the emxRNDEnqueueBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param args holds the following input arguments:
   *                       "objectId" - String, Object Id
   *                       "fileList" - LinkedList, the list of files to print
   *@exception  Exception  IllegalArgumentException, if wrong args are passed in
   *
   *@since AEF 9.5.4.0
   */
  public emxRNDEnqueueBase_mxJPO(Context context, String[] args) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    super(context, args);
  }


  /**
   *  Unpacks the passed in arguments, and uses them to set class properties
   *
   *@param args holds the following input arguments:
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
    Map map = (Map) JPO.unpackArgs(args);
    if(map.size() < 2) {
      throw (new IllegalArgumentException());
    }
    this._objectId = (String) map.get("objectId");
    this._fileList = (LinkedList) map.get("fileList");
  }


  /**
   *  mxMain method, intended to be called as a trigger.
   *
   *@param context the eMatrix <code>Context</code> object
   *@param args holds the following input arguments:
   *                  Object Id
   *@return                int, success
   *@exception  Exception  throws exceptions on any mql errors
   *
   *@since AEF 9.5.4.0
   */
  public int mxMain(Context context, String[] args) throws Exception {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */

    try {
      String objectId = args[0];
      doFiles(context, objectId);
    } catch(Exception e) {
      return 1;
    }
    return 0;
  }


  /**
   *  Goes through all the formats on an object, and adds all their files to the
   *  list of waht is to be rendered, then executes the rendering method.
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  objectID       Object Id
   *@exception  Exception  Throws an exception on any mql errors
   *
   *@since AEF 9.5.4.0
   */
  public void doFiles(Context context, String objectId) throws Exception {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */

    String command      = "print businessobject $1 select format dump $2";;
    String result       = MqlUtil.mqlCommand(context, command, objectId, "|");

    //Tokenize the return, using testTokenizer to determine if there was a retun
    StringTokenizer testTokenizer = new StringTokenizer(result, "\n");
    StringTokenizer formatTokenizer = new StringTokenizer(result.substring(0,
      result.length() - 1), "|");
    if(testTokenizer.hasMoreTokens() && !result.trim().equals("\n")) {
      LinkedList fileList = new LinkedList();

      //iterate through the list of formats, and add them to the list
      while(formatTokenizer.hasMoreTokens()) {
        String format = formatTokenizer.nextToken();
        if(!format.equals("PDF")) {

          //Get files for the format
          command           = "print businessobject $1 select $2 dump $3";
          String fileResult = MqlUtil.mqlCommand(context, command, objectId, "format["+format+"].file", "|");

          //iterate through the files, adding them to the file list.
          StringTokenizer testTokenizer2 = new
            StringTokenizer(fileResult, "\n");
          StringTokenizer fileTokenizer = new StringTokenizer(
            fileResult.substring(0, fileResult.length() - 1), "|");
          if(testTokenizer2.hasMoreTokens() && !result.trim().equals("\n")) {
            while(fileTokenizer.hasMoreTokens()) {
              //Attaching the format to the front of the file string,
              //so that they can stay associated.
              fileList.add(format + "|" + fileTokenizer.nextToken());
            }
          }
        }
      }

      //Invoke the rendering function
      execute(context, objectId, fileList);
    }
  }


  /**
   *  runs the Adlib PDF Rendering integration (on a seperate Thread)
   *  the integration will check out the files, and then poll the output and
   *  error directories until it's time runs out
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
    String integrationName = _util.getString(context,
      "eServiceBatchPrintPDF.PDF.Integration");
    if(integrationName.equals("emxAdlibRNDIntegration")) {
      new emxAdlibRNDIntegration_mxJPO(context, queue).start();
      //Insert new else if's here for new integrations
    } else { //when in doubt, call the emxAdlibRNDIntegration
      new emxAdlibRNDIntegration_mxJPO(context, queue).start();
    }
  }
}

