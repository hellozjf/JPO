//
// $Id: ${CLASSNAME}.java.rca 1.12 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $
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
 *  Version  : "$Revision: 1.12 $"
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.Format;
import matrix.db.FormatItr;
import matrix.db.FormatList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;

/**
 *  The Integration to Adlib as a PDF Renderer
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *@exclude
 */
public class emxAdlibRNDIntegrationBase_mxJPO extends emxAbsPDFIntegration_mxJPO {
  /**
   *  Constructor for the emxAdlibRNDIntegrationBase object
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  queue    Queue of documents to be rendered
   *
   *@since AEF 9.5.4.0
   */
  public emxAdlibRNDIntegrationBase_mxJPO(Context context, emxPDFQueue_mxJPO queue) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    super(context, queue);
  }


  /**
   *  Main processing method for the emxAdlibRNDIntegrationBase object
   *  This method checks out all the files to the input folder, writes out
   *  the adlib control .dpi file, and then polls the output and error folders
   *  until something appears or poll runs out of time.  As the files are
   *  appear, it will check them in to the PDF format, and if the config file
   *  states for it to happen, they will be promoted.
   *
   *@since AEF 9.5.4.0
   */
  public void run() {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //Get config
    String inputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.InputFolderPath");
    String outputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.OutputFolderPath");
    String renderingState = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.RenderingState");
    String promote = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.PromoteOnRendering");
    try {
      BusinessObject bo = new BusinessObject(_queue.getObjectId());
      bo.open(_context);

      //Checking to see if this object can support checkins to the PDF format
      boolean hasPDF = false;
      FormatList frmList = bo.getFormats(_context);
      FormatItr frmItr = new FormatItr(frmList);
      while(frmItr.next()) {
        Format frm = frmItr.obj();
        if(frm.toString().equalsIgnoreCase("PDF")) {
          hasPDF = true;
        }
      }

      //You must be able to check into the PDF format to be able to render.
      if(!hasPDF) {
        String errorMessage = _util.getString(_context,
            "emxBatchPrintPDF.PDF.NeedsPDFFormat",
            "emxDocumentCentralStringResource");
        //Write error to log file
        //Send Icon Mail
        sendErrorMessage(_queue.getObjectId(), errorMessage, inputFolder);
        throw new Exception(errorMessage);
      }
      //Create the dynamic file names and folders
      String job = createFolder(_queue.getObjectId(), inputFolder);
      String jobFolder = java.io.File.separator + job;

      //Go through the whole queue
      while (!_queue.isEmpty()) {
        emxPDFDocument_mxJPO document = _queue.getNext();
        String inputJobFolder = inputFolder + jobFolder;
        String dpiFileName = document.getFileName();
        document.checkOutFile(inputJobFolder);


        //Open the dpi file for writing
        FileWriter fileID = new FileWriter(inputJobFolder +
          java.io.File.separator + dpiFileName + ".dpi", true);
        StringBuffer sb = new StringBuffer();

        //write the file names to the dpi file buffer, and check the files out
        sb.append(document.getFileName());
        sb.append("\n");
        sb.append("\n");

        //If the system config wants headers, print them
        String printHeader = _util.getString(_context,
          "eServiceBatchPrintPDF.Batch.PrintHeader");
        if (printHeader.equalsIgnoreCase("True")) {
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

        //Write dpi file
        fileID.write(sb.toString());
        fileID.flush();
        fileID.close();

        //Poll till job is done
        pollJob(bo, jobFolder, dpiFileName);
      }

      DomainObject domObj = DomainObject.newInstance(_context, bo);

      // this will be true, if attribute Move Files To Version is True.
      String isVersion = domObj.getAttributeValue(_context, CommonDocument.ATTRIBUTE_IS_VERSION_OBJECT);

      String masterObjectId = "";

      // if this is version document, get masterdocument id and use that object as parent object for promote
      if("true".equalsIgnoreCase(isVersion)){
          masterObjectId = domObj.getInfo(_context, CommonDocument.SELECT_MASTER_ID);
          bo.close(_context);
          bo = new BusinessObject(masterObjectId);
          bo.open(_context);
      }
      //If appropriate, promote the object
      String cmd            = "print bus  $1 select current dump";
      String currentState   = MqlUtil.mqlCommand(_context, cmd, bo.getObjectId());
      if (promote.equalsIgnoreCase("True") && currentState != null &&
        currentState.equalsIgnoreCase(renderingState)) {
        try {
          bo.promote(_context);
        }
        catch (Exception e) {
          String errorMessage = e.getMessage();

          //Write error to log file

          //Send Icon Mail
          sendErrorMessage(_queue.getObjectId(), errorMessage, "");
        }
      }

      //Delete the input & output folders
      deleteFolder(outputFolder + jobFolder);
      deleteFolder(inputFolder + jobFolder);

      bo.close(_context);
    }
    catch (Exception e) {
      String errorMessage = _util.getString(_context,
          "emxBatchPrintPDF.FileWriteFailure",
          "emxDocumentCentralStringResource");
      errorMessage = errorMessage + ":\n" + e.getMessage();

      //Write error to log file

      //Send Icon Mail
      sendErrorMessage(_queue.getObjectId(), errorMessage, inputFolder);
    }
  }


  /**
   *  Polls the output folders, and cleans up
   *
   *@param  bo             Business Object that has been rendered
   *@param  jobFolder      name of the job folder
   *@param  dpiFileName    name of the dpi file
   *@exception  Exception  Adlib Failed
   *
   *@since AEF 9.5.4.0
   */
  protected void pollJob(BusinessObject bo,
                         String jobFolder,
                         String dpiFileName)
    throws Exception {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    //Get config info
    String inputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.InputFolderPath");
    String outputFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.OutputFolderPath");
    String errorFolder = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.ErrorFolderPath");
    String sleepInterval = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.SleepInterval");
    String timeoutInterval = _util.getString(_context,
      "eServiceBatchPrintPDF.PDF.TimeoutIntervals");
    try {
      //various things needed for the polling
      int sleepInt = parseInt(sleepInterval);
      int timeoutInt = parseInt(timeoutInterval);
      String jobFile = jobFolder + java.io.File.separator +
        dpiFileName + ".pdf";
      String erFile = java.io.File.separator + dpiFileName + "_Error.dpi";
      File outputFile = new File(outputFolder + jobFile);
      File errorFile = new File(errorFolder + erFile);

      //Poll for the file, and sleep the thread (according to config)
      while (!outputFile.exists() && !errorFile.exists() && timeoutInt >= 0) {
        Thread.sleep(sleepInt * 1000);
        timeoutInt--;
      }

      String fileDescription = _util.getString(_context,
        "emxBatchPrintPDF.PDF.RenderedFileDescription",
        "emxDocumentCentralStringResource");

      //Adlib Success
      if (outputFile.exists()) {
        // Check the file in and create version document
      checkinRenderedFile(_context, _queue.getObjectId(), dpiFileName + ".pdf", fileDescription, bo.getVault(), outputFolder + jobFolder);
      }
      else if (errorFile.exists()) {      //Adlib Failure
        //Delete the input folder
        deleteFolder(inputFolder + jobFolder);

        //Write out to the log file

        //Send an Icon Mail
        String errorMessage = _util.getString(_context,
          "emxBatchPrintPDF.PDF.RenderingOutputErrorMessage",
          "emxDocumentCentralStringResource") + "\n" + errorFolder + jobFolder +
          java.io.File.separator + dpiFileName + ".dpi ";
        sendErrorMessage(_queue.getObjectId(), errorMessage, outputFolder +
          jobFolder);
        throw new Exception(errorMessage);
      }
      else {
        //Write out to the log file & send a mail
        String errorMessage = _util.getString(_context,
          "emxBatchPrintPDF.PDF.RenderingTimedOutMessage",
          "emxDocumentCentralStringResource") + dpiFileName;
          sendErrorMessage(_queue.getObjectId(), errorMessage, outputFolder +
          jobFolder);
      }
    }
    catch (Exception e) {
      String errorMessage = _util.getString(_context,
        "emxBatchPrintPDF.PollingFailure",
        "emxDocumentCentralStringResource");

      //Write out to the log file

      //Send Icon Mail
      sendErrorMessage(_queue.getObjectId(), errorMessage, outputFolder +
        jobFolder);
      throw new Exception(errorMessage);
    }
  }

   /**
    *  Checks in the rendered files in to specified vaults
    *
    *@param context the eMatrix <code>Context</code> object
    *@param objectId  Object Id of file Object
    *@param fileName    name of the  file
    *@param fileDescription  Description for the file Object to be checked in
    *@param vault  Name of the vault
    *@param folder folder name
    *@exception  Exception  if check in Failed
    *
    *@since AEF 9.5.4.0
    */

  protected void checkinRenderedFile(Context context,
                                     String objectId,
                                     String fileName,
                                     String fileDescription,
                                     String vault,
                                     String folder) throws Exception
  {
    try
    {
      String masterObjectId = "";

      // this objectId could be master or version file id depending on Move Files To Version attribute
      DomainObject object = DomainObject.newInstance(context, objectId);

      // this will be true, if attribute Move Files To Version is True.
      String isVersion = object.getAttributeValue(context, CommonDocument.ATTRIBUTE_IS_VERSION_OBJECT);

      // if this is version document, get masterdocument id and use that object as parent object for checkin
      if("true".equalsIgnoreCase(isVersion)){
         masterObjectId = object.getInfo(context, CommonDocument.SELECT_MASTER_ID);
      } else {
         masterObjectId = objectId;
      }

      CommonDocument masterObject = (CommonDocument)DomainObject.newInstance(context, CommonDocument.TYPE_DOCUMENTS);
      masterObject.setId(masterObjectId);

      String moveFilesToVersion = masterObject.getInfo(context, CommonDocument.SELECT_MOVE_FILES_TO_VERSION);

      StringList selectList = new StringList(1);
      selectList.add(CommonDocument.SELECT_ID);

      // Lock the object
      String objectWhere = CommonDocument.SELECT_TITLE + "== '" + fileName +"'";
      MapList mlist = masterObject.getRelatedObjects(context,
                                      CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                                      CommonDocument.TYPE_DOCUMENTS,
                                      selectList,
                                      null,
                                      false,
                                      true,
                                      (short) 1,
                                      objectWhere,
                                      CommonDocument.EMPTY_STRING);

      DomainObject versionObject = null;
      if (mlist != null && mlist.size() > 0 )
      {
        Map versionMap = (Map) mlist.get(0);
        versionObject = DomainObject.newInstance(context, (String) versionMap.get(CommonDocument.SELECT_ID));
        versionObject.lock(context);
      }

      // Create/revise the version object
      String versionId = masterObject.reviseVersion(context, null, fileName, new HashMap());

      if(versionObject != null)
      {
          if(versionObject.isLocked(context))
          {
              versionObject.unlock(context);
          }
      }

      //Check the file in
      if(moveFilesToVersion != null && "false".equalsIgnoreCase(moveFilesToVersion))
      {
         object.setId(masterObjectId);
         object.checkinFile(context, true, true, "", "PDF", fileName, folder);
      }
      else if (moveFilesToVersion != null && "true".equalsIgnoreCase(moveFilesToVersion))
      {
          object.setId(versionId);
          object.checkinFile(context, true, true, "", "PDF", fileName, folder);
      }

    } catch (Exception ex) {
      throw ex;
    }
  }

   /**
    *  Updated the version of document object
    *
    *@param context the eMatrix <code>Context</code> object
    *@param objectId  Object Id of file Object
    *@param fileName   name of the  file
    *@exception  Exception  version updation failed
    *
    *@since AEF 9.5.4.0
    */

    protected void updateVersionDocument(Context context,
                                         String objectId,
                                         String fileName ) throws Exception
    {
        try
        {
            String checkInReason = _util.getString(_context,
              "emxBatchPrintPDF.PDF.RenderedFileDescription",
              "emxDocumentCentralStringResource");

            DomainObject busObject = (DomainObject) DomainObject.newInstance(context,objectId);

            //find the latest version for the file being checked in
            StringList objectSelects = new StringList();
            objectSelects.add(DomainConstants.SELECT_ID);
            objectSelects.add("attribute[" + DomainConstants.ATTRIBUTE_FILE_VERSION + "]");
            int latestVersion = 0;
            String latestVerDocId = "";

            String objWhereExpr = "attribute[" + DomainConstants.ATTRIBUTE_TITLE + "] == " + "\"" + fileName + "\"";

            short level = 1;
            MapList versionList = busObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_VERSION,
                        DomainConstants.TYPE_VERSION_DOCUMENT, objectSelects, null, false, true,
                level, objWhereExpr, "");

            for (int i=0; i<versionList.size(); i++)
            {
                Map tmpMap = (Hashtable)versionList.get(i);
                String fileVersion = (String)tmpMap.get("attribute[" + DomainConstants.ATTRIBUTE_FILE_VERSION + "]");
                int tmpVersion = Integer.parseInt(fileVersion);
                if (tmpVersion > latestVersion)
                {
                    latestVersion = tmpVersion;
                    latestVerDocId = (String)tmpMap.get(DomainConstants.SELECT_ID);
                }
            }

            //Now latestVerDocId represents the id of latest ver doc
            //move the file from BusinessObject to latest Version doc
            String strCommand = "modify bus $1 move from $2 format $3 file $4";
            MqlUtil.mqlCommand(context, strCommand, latestVerDocId, objectId, "PDF", fileName);

            //create the latest Version Doc for the file and connect it to BusinessObject
            DomainObject verDocObject = new DomainObject();

            String uniqueName = verDocObject.getUniqueName("VD_");

            verDocObject.createAndConnect(context, DomainConstants.TYPE_VERSION_DOCUMENT,
                    uniqueName, DomainConstants.RELATIONSHIP_VERSION, busObject, true);

            HashMap attribMap = new HashMap();
            attribMap.put(DomainConstants.ATTRIBUTE_TITLE, fileName);
            attribMap.put(DomainConstants.ATTRIBUTE_CHECKIN_REASON, checkInReason);
            attribMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR, context.getUser());
            attribMap.put(DomainConstants.ATTRIBUTE_FILE_VERSION, Integer.toString(latestVersion+1));
            verDocObject.setAttributeValues(context, attribMap);



        } catch (Exception ex) {
            throw ex;
        }

    }


  /**
   *  Mails an error message to the person who rendered, and to the person
   *  who is set to recieve Error Notifications
   *
   *@param  objectId      Object's Id
   *@param  errorMessage  The message to send
   *@param  outputFolder  Folder where the files are/were
   *
   *@since AEF 9.5.4.0
   *@grade 0
   */
  protected void sendErrorMessage(String objectId,
                                String errorMessage,
                                String outputFolder) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    try {
      //Get the user to send notifications to
      String errorNotify = _util.getString(_context,
        "eServiceBatchPrintPDF.PDF.ErrorNotify");

      BusinessObject bo = new BusinessObject(objectId);
      bo.open(_context);
      StringBuffer sbSubject = new StringBuffer("");
      StringBuffer sbMessage = new StringBuffer("");

      //Fill the user list - current user, and notification user
      StringList userList = new StringList();
      userList.addElement(_context.getUser());
      if (errorNotify != null) {
        userList.addElement(errorNotify);
      }

      //Build Subject Line
      sbSubject.append(_util.getString(_context,
        "emxBatchPrintPDF.PDF.RenderingFailed1",
        "emxDocumentCentralStringResource"));
      sbSubject.append(bo.getName() + ", ");
      sbSubject.append(_util.getString(_context,
        "emxBatchPrintPDF.Rev", "emxDocumentCentralStringResource"));
      sbSubject.append(": " + bo.getRevision());

      //Build Message Body
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.PDF.RenderingFailed1",
        "emxDocumentCentralStringResource"));
      sbMessage.append("\n\n");
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Type", "emxDocumentCentralStringResource"));
      sbMessage.append(": " + bo.getTypeName());
      sbMessage.append("\n");
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Name", "emxDocumentCentralStringResource"));
      sbMessage.append(": " + bo.getName());
      sbMessage.append("\n");
      sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.Rev", "emxDocumentCentralStringResource"));
      sbMessage.append(": " + bo.getRevision());
      sbMessage.append("\n\n");
      sbMessage.append(errorMessage);
      sbMessage.append("\n\n");

      //Message about folder if it was passed in
      if (!outputFolder.equals("")) {
        sbMessage.append(_util.getString(_context,
        "emxBatchPrintPDF.PDF.RenderingFailed3",
        "emxDocumentCentralStringResource"));
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
    catch (Exception e) {
      //Write out to the log file
    }
  }
}
