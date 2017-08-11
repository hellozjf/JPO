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
import java.io.File;

/**
 *  This is an abstract class intended to be the base of integration classes
 *  to various PDF Renderers/Batch Printers.  This class provides a number of
 *  utility methods.
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@exclude
 */
public abstract class emxAbsPDFIntegrationBase_mxJPO extends Thread
{
  /**  The PDFQueue to render/print */
  protected emxPDFQueue_mxJPO _queue;

  /**  Matrix Context */
  protected Context _context;

  /**  Utility Class */
  protected emxPDFUtil_mxJPO _util = new emxPDFUtil_mxJPO();


  /**
   *  Constructor for the emxAbsPDFIntegrationBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  queue    The PDFQueue to render/print
   *
   *@since AEF 9.5.4.0
   */
  public emxAbsPDFIntegrationBase_mxJPO(Context context, emxPDFQueue_mxJPO queue)
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

    this._queue = queue;
  }


  /**
   *  Creates a job folder in the Adlib input folder
   *
   *@param  objectId       Object Id
   *@param  stagingFolder  name of the folder to create the job folder in
   *@return String         The name of the Job Folder
   *
   *@since AEF 9.5.4.0
   */
  protected String createFolder(String objectId, String stagingFolder)
    throws Exception
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    String jobFolder = "";
    //Get a time stamp and append it to the object id
    String timestamp = Long.toString((long) (System.currentTimeMillis()));
    jobFolder = objectId + "_" + timestamp;

    //Create the folder
    String jobFolderPath = stagingFolder + java.io.File.separator + jobFolder;
    File fId = new File(jobFolderPath);
    fId.delete();
    if(!fId.mkdir()) {
      throw new Exception("Directory creation failed: \n" + stagingFolder +
        java.io.File.separator + jobFolder);
    }
    return jobFolder;
  }


  /**
   *  Deletes a directory and it's children recursivly
   *
   *@param  dir  Name of the top level directory to delete
   *@return boolean success or failure
   *
   *@since AEF 9.5.4.0
   */
  protected boolean deleteFolder(String dir)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    File fileObj = new File(dir);
    if (fileObj.isDirectory())
    {
      //Get a list of files
      File[] flist = fileObj.listFiles();
      for (int iCount = 0; iCount < flist.length; iCount++)
      {
        //Recursivly delete any files or folders
        deleteFolder(flist[iCount].getAbsolutePath());
      }
    }
    //Delete
    return fileObj.delete();
  }


  /**
   *  Parses a string into an int, and if it fails or is less then one,
   *  it returns 1
   *
   *@param  stringInt  String containing an integer value
   *@return            the integer
   *
   *@since AEF 9.5.4.0
   *@grade 0
   */
  protected int parseInt(String stringInt)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    int retInt = 1;

    //Parse the integer
    try
    {
      retInt = Integer.parseInt(stringInt);
    }
    catch (Exception e)
    {
    }

    // If the integer is less then 1, set it to 1
    if (retInt < 1)
    {
      retInt = 1;
    }
    return retInt;
  }
}

