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
import java.util.Map;
import java.util.LinkedList;
import matrix.db.Context;
import matrix.db.JPO;

/**
 *  Main Batch Printing JPO.  This is the implementation class
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@exclude
 */
public class emxBPEnqueueBase_mxJPO extends emxAbsPDFEnqueue_mxJPO
{
  /**  Printer's Name */
  protected String _printerName;
  /**  Number of Copies to Prnt */
  protected int _numberOfCopies;


  /**
   *  Constructor for the emxBPEnqueueBase object
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
  public emxBPEnqueueBase_mxJPO(Context context, String[] args)
  {
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
  protected void unpackArgs(String[] args)
    throws Exception
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    Map map = (Map) JPO.unpackArgs(args);
    if (map.size() < 4)
    {
      throw (new IllegalArgumentException());
    }
    this._objectId = (String) map.get("objectId");
    this._printerName = (String) map.get("printerName");
    Integer numberOfCopies = (Integer) map.get("numberOfCopies");
    this._numberOfCopies = numberOfCopies.intValue();
    this._fileList = (LinkedList) map.get("fileList");
  }


  /**
   *  runs the Adlib Batch Printing integration (on a seperate Thread)
   *  the integration will check out the files, and then poll the output and
   *  error direcotries until it's time runs out
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  queue          The PDF Queue with the documents to be Printed
   *
   *@since AEF 9.5.4.0
   */
  protected void runIntegration(Context context, emxPDFQueue_mxJPO queue)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    String integrationName = _util.getString(context,
      "eServiceBatchPrintPDF.Batch.Integration");

    try
    {
        if(integrationName.equals("emxAdlibRNDIntegration")) {
          //we have no way with this specific integration to print more then one
          //copy, so we have to loop calling the integration
          //Don't do this with other integrations unless you have to
          for(int i = 0; i < _numberOfCopies; i++) {
            new emxAdlibBPIntegration_mxJPO(context, queue, _printerName,
              _numberOfCopies).start();
          //Insert new else if's here for new integrations
          }
        } else { //when in doubt, call the emxAdlibRNDIntegration
          //we have no way with this specific integration to print more then one
          //copy, so we have to loop calling the integration
          for(int i = 0; i < _numberOfCopies; i++) {
            new emxAdlibBPIntegration_mxJPO(context, queue, _printerName,
              _numberOfCopies).start();

            Thread.sleep(5000);

          }
        }

    }
    catch(Exception e)
    { }


  }
}
