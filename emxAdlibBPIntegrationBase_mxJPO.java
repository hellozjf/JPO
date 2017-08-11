//
// $Id: ${CLASSNAME}.java.rca 1.11 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $ 
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
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.util.StringList;
import java.io.File;
import java.io.FileWriter;

/**
 *  The integration to Adlib as a Batch Print provider.
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@exclude
 */
public class emxAdlibBPIntegrationBase_mxJPO extends emxAbsPDFIntegration_mxJPO
{
  /**  Number of copies to print */
  protected int _numberOfCopies;
  /**  Name of the printer to print to */
  protected String _printerName;


  /**
   *  Constructor for the emxAdlibBPIntegrationBase object
   *
   *@param context the eMatrix <code>Context</code> object
   @param  queue           Queue of documents to be printed
   *@param  printerName     Printer Name
   *@param  numberOfCopies  Number of copies to be printed
   *
   *@since AEF 9.5.4.0
   */
  public emxAdlibBPIntegrationBase_mxJPO(Context context,
                      emxPDFQueue_mxJPO queue,
                      String printerName,
                      int numberOfCopies)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    super(context, queue);
    this._numberOfCopies = numberOfCopies;
    this._printerName = printerName;
  }


  /**
   *  Main processing method for the emxAdlibBPIntegrationBase object
   *  This method checks out all the files to the input folder, writes out
   *  the adlib control .dpi file, and then polls the output and error folders
   *  until something appears or poll runs out of time
   *
   *@since AEF 9.5.4.0
   */
  public void run()
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //Get config
    String inputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.Batch.InputFolderPath");
    try
    {
      BusinessObject bo = new BusinessObject(_queue.getObjectId());
      bo.open(_context);

      //Create the dynamic file names and folders
      String job = createFolder(_queue.getObjectId(), inputFolder);
      String jobFolder = java.io.File.separator + job;
      String dpiFileName = job;
      String inputJobFolder = inputFolder + jobFolder;

      //Open the dpi file for writing
      FileWriter fileID = new FileWriter(inputJobFolder + java.io.File.separator
        + dpiFileName + ".dpi", true);
      StringBuffer sb = new StringBuffer();

      //write the file names to the dpi file buffer, and check the files out
      for(int i = 0; i < _queue.size(); i++)
      {
        emxPDFDocument_mxJPO document = (emxPDFDocument_mxJPO)_queue.get(i);
        document.checkOutFile(inputJobFolder);
        sb.append(document.getFileName());
        sb.append("\n");
      }
      sb.append("\n");

      //If the system config wants headers, have adlib render them
      String printHeader = _util.getString(_context,
        "eServiceBatchPrintPDF.Batch.PrintHeader");
      if (printHeader.equalsIgnoreCase("True"))
      {
        sb.append("[Header]");
        sb.append("\n");
        sb.append("FontName=Times-Roman");
        sb.append("\n");
        sb.append("FontSize=8");
        sb.append("\n");
        sb.append("TextCenter=Rendered PDF File  Page &[Page] of &[Pages]");
        sb.append("\n");
        sb.append("TextRight=");
        sb.append(bo.getName());
        sb.append(", Rev:");
        sb.append(bo.getRevision());
        sb.append("\n");
        sb.append("\n");
      }

      //Output Printer information
      sb.append("[OutputPDF]");
      sb.append("\n");
      sb.append("Printer=");
      sb.append(_printerName);
      sb.append("\n");

      //Write dpi file
      fileID.write(sb.toString());
      fileID.flush();
      fileID.close();

      //Poll till job is done
      pollJob(jobFolder, dpiFileName);

      bo.close(_context);
    }
    catch (Exception e)
    {
      String errorMessage = _util.getString(_context,
          "emxBatchPrintPDF.FileWriteFailure",
          "emxDocumentCenralStringResource");

      //Write error to log file

      //Send Icon Mail
      sendErrorMessage(_queue.getObjectId(), errorMessage, inputFolder);
    }
  }


  /**
   *  Polls the output folders, and cleans up
   *
   *@param  jobFolder    name of the job folder
   *@param  dpiFileName  name of the dpi file
   *
   *@since AEF 9.5.4.0
   */
  protected void pollJob(String jobFolder,
                         String dpiFileName)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //Get config info
    String inputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.Batch.InputFolderPath");
    String outputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.Batch.OutputFolderPath");
    String errorFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.Batch.ErrorFolderPath");
    String sleepInterval = _util.getString(_context,
      "eServiceBatchPrintPDF.Batch.SleepInterval");
    String timeoutInterval = _util.getString(_context,
      "eServiceBatchPrintPDF.Batch.TimeoutIntervals");
    try
    {
      //various things needed for the polling
      int sleepInt = parseInt(sleepInterval);
      int timeoutInt = parseInt(timeoutInterval);
      String jobFile = jobFolder + java.io.File.separator + dpiFileName +
        ".dpi";
      java.io.File outputFile = new java.io.File(outputFolder + jobFile);
      java.io.File errorFile = new java.io.File(errorFolder +
        java.io.File.separator + dpiFileName + "_Error.dpi");

      //Poll for the file, and sleep the thread (according to config)
      while (!outputFile.exists() && !errorFile.exists() && timeoutInt >= 0)
      {
        Thread.sleep(sleepInt * 1000);
        timeoutInt--;
      }

      //Adlib Success
      if (outputFile.exists())
      {
        //Delete the input & output folders
        deleteFolder(outputFolder + jobFolder);
        deleteFolder(inputFolder + jobFolder);
      }
      //Adlib Failure
      else if (errorFile.exists())
      {
        //Delete the input folder
        deleteFolder(inputFolder + jobFolder);

        //Write out to the log file

        //Send an Icon Mail
        String errorMessage = _util.getString(_context,
          "emxBatchPrintPDF.Batch.PrintingOutputErrorMessage",
          "emxDocumentCenralStringResource") + "\n" + errorFolder + jobFolder +
          java.io.File.separator + dpiFileName + ".dpi ";
        sendErrorMessage(_queue.getObjectId(), errorMessage, outputFolder +
          jobFolder);
        throw new Exception(errorMessage);
      }
      else
      {
        //Write out to the log file & send a mail
        String errorMessage = _util.getString(_context,
          "emxBatchPrintPDF.PDF.RenderingTimedOutMessage",
          "emxDocumentCenralStringResource") + dpiFileName;
        sendErrorMessage(_queue.getObjectId(), errorMessage, outputFolder +
          jobFolder);
      }
    }
    catch (Exception e)
    {
      String errorMessage = _util.getString(_context,
        "emxBatchPrintPDF.PollingFailure",
        "emxDocumentCenralStringResource");

      //Write out to the log file

      //Send Icon Mail
      sendErrorMessage(_queue.getObjectId(), errorMessage, outputFolder +
        jobFolder);
    }
  }


  /**
   *  Mails an error message to the person who printed, and to the person
   *  who is set to recieve Error Notifications
   *
   *@param  objectId      Object's Id
   *@param  errorMessage  The message to send
   *@param  outputFolder  Folder where the files are/were
   *
   *@since AEF 9.5.4.0
   */
  protected void sendErrorMessage(String objectId,
                                  String errorMessage,
                                  String outputFolder)  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    try
    {
      //Get the user to send notifications to
      String errorNotify = _util.getString(_context,
        "eServiceBatchPrintPDF.Batch.ErrorNotify");

      BusinessObject bo = new BusinessObject(objectId);
      bo.open(_context);
      StringBuffer sbSubject = new StringBuffer("");
      StringBuffer sbMessage = new StringBuffer("");

      //Fill the user list - current user, and notification user
      StringList userList = new StringList();
      userList.addElement(_context.getUser());
      if (errorNotify != null && !errorNotify.equals(_context.getUser()))
      {
        userList.addElement(errorNotify);
      }

      //Build Subject Line
      sbSubject.append(_util.getString(_context,
        "emxBatchPrintPDF.Batch.PrintingFailed1",
        "emxDocumentCenralStringResource"));
      sbSubject.append(" "+ bo.getName() + ", ");
      sbSubject.append(_util.getString(_context,
        "emxBatchPrintPDF.Rev", "emxDocumentCenralStringResource"));
      sbSubject.append(": " + bo.getRevision());

      //Build Message Body
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Batch.PrintingFailed2",
        "emxDocumentCenralStringResource"));
      sbMessage.append("\n\n");
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Type", "emxDocumentCenralStringResource"));
      sbMessage.append(": " + bo.getTypeName());
      sbMessage.append("\n");
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Name", "emxDocumentCenralStringResource"));
      sbMessage.append(": " + bo.getName());
      sbMessage.append("\n");
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Rev", "emxDocumentCenralStringResource"));
      sbMessage.append(": " + bo.getRevision());
      sbMessage.append("\n\n");
      sbMessage.append(errorMessage);
      sbMessage.append("\n\n");

      if (!outputFolder.equals(""))
      {
        sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Batch.PrintingFailed3",
        "emxDocumentCenralStringResource"));
        sbMessage.append("\n");
        int idx = outputFolder.lastIndexOf("/");
        sbMessage.append(outputFolder.substring(idx + 1,
          outputFolder.length()));
      }

      //Send the mail
      _util.sendMail(_context, userList, null, null, sbSubject.toString(),
        sbMessage.toString());

      bo.close(_context);
    }
    catch (Exception e)
    {
      //Write out to the log file
    }
  }
}

