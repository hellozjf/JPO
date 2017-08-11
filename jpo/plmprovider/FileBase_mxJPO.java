package jpo.plmprovider;
// FileBase.java
//
// Created on Dec 20, 2007
//
// Copyright (c) 2006-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectProxy;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.TicketWrapper;

import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.client.fcs.FcsClient;
import com.matrixone.fcs.mcs.CheckinEnd;
import com.matrixone.fcs.mcs.CheckinStart;
import com.matrixone.fcs.mcs.Checkout;
import com.matrixone.apps.plmprovider.FileRequest;
import com.matrixone.apps.plmprovider.FileRequestWrapper;
import com.matrixone.apps.plmprovider.FileResponse;
import com.matrixone.apps.plmprovider.FileResult;
import com.matrixone.apps.plmprovider.RawFileResponse;
import com.matrixone.apps.plmprovider.RawFileResult;
import com.matrixone.apps.plmprovider.PlmProviderUtil;
import com.matrixone.apps.plmprovider.NodeType;
import com.matrixone.apps.plmprovider.Ticket;


/**
 * @author prasad
 *
 * The <code>FileBase</code> class provides web services related to
 * associated files of an object viewed in 3DLive
 *
 * @version AEF X+3 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class FileBase_mxJPO extends jpo.plmprovider.Mat3DLive_mxJPO {


    /**
     * Default constructor.
     *
     * @since AEF X+3
     */

    public FileBase_mxJPO()
    {
    }

    /**
     * This function will get FCS url that corresponds to the file/format/object in the request object
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input object id, filename, format
     * @return FileResponse
     */
    public FileResponse getUrl(String username, String password, FileRequest request)
    {
        return getUrl(getContext(username,password),request);
    }

    /**
     * This function will get FCS url that corresponds to the file/format/object in the request object.
     * This method is for testing purpose only.
     *
     * @param context the matrix context
     * @param args the array should contain FileRequest
     * @return FileResponse
     */
    public FileResponse getUrl(Context context, String args[]) throws Exception
    {
        HashMap programMap   = (HashMap)JPO.unpackArgs(args);
        FileRequest request  = (FileRequest)programMap.get("request");

        return getUrl(context,request);
    }    

    /**
     * This function will get FCS url that corresponds to the file/format/object in the request object
     *
     * @param context the matrix context
     * @param request input object id, filename, format
     * @return FileResponse
     * @since AEF X+3
     */
    private FileResponse getUrl(Context context, FileRequest request)
    {
        log("File.getUrl start");
        FileResponse response = new FileResponse();

        try
        {
            String objectId = request.getObjectId();
            String fileName = request.getFileName();
            String fileFormat = request.getFileFormat();
            String mcsURL = request.getMcsURL();

            // return requested formats for each object id in the list
            FileResult[] results = new FileResult[1];

            try{
                results[0] = getResult(context, mcsURL, objectId, fileName, fileFormat);
            }
            catch(Exception e)
            {
                e.printStackTrace(System.out);
                log("File.getUrl exception " + e);
            }

            response.setResults(results);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.out);
            log("File.getUrl exception " + ex);
        }

        log("File.getUrl complete");
        return (response);
    }

    /**
     * This function will get a FileResult for the given object and given filename and given format.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL the inital portion of the URL
     * @param objectId the object to process
     * @param filename name of the file for which fcsURl needs to calculated
     * @param fileFormat format of the file for which fcsURl needs to calculated
     * @return FileResult the FCS url of the file
     * @throws MatrixException
     * @since AEF X+3
     */

    protected FileResult getResult(Context context, String mcsURL, String objectId, String fileName, String fileFormat)
    throws MatrixException
    {
        log("File.getResult start");
        FileResult result = new FileResult();
        String fcsUrl = "";

        try
        {
            ContextUtil.startTransaction(context, true);
			fcsUrl = getFCSUrl(context, mcsURL, objectId, fileName, fileFormat);

			result.setObjectId(objectId);
			result.setUrl(fcsUrl);
			result.setFileName(fileName);
			result.setFileFormat(fileFormat);
            ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            log("File.getResult exception " + e);
        }
        log("File.getResult complete");
        return result;
    }

    /**
     * Returns FCS url for the file checked into object whose id is passed in the input
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL
     * @param oid
     * @param fileName
     * @param format
     * @return String - Returns FCS url for the file checked into objects whose id is passed as String
     * @throws MatrixException
     * @since AEF X+3
     */
    protected String getFCSUrl(Context context, String mcsURL, String oid, String fileName, String format)
    throws Exception
    {
        DomainObject dObj = new DomainObject(oid);
        if(dObj.isKindOf(context, TYPE_VPM_REP_REFERENCE)){
        	context.setApplication(APPLICATION_VPLM);
        }
    	
        BusinessObject bo = new BusinessObject(oid);
        try
        {
            bo.open(context);

            ArrayList bops = new ArrayList();
            BusinessObjectProxy bproxy = new BusinessObjectProxy(oid,
                    format,
                    fileName,
                    false,
                    false);
            bops.add(bproxy);
            TicketWrapper ticket = Checkout.doIt(context, mcsURL, bops);
            StringBuffer fcsUrl = new StringBuffer();
            fcsUrl.append(ticket.getActionURL());
            fcsUrl.append("?");
            fcsUrl.append(FcsClient.resolveFcsParam("jobTicket"));
            fcsUrl.append("=");
            fcsUrl.append(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ticket.getExportString()));
            fcsUrl.append("&CATCacheKey=");
            fcsUrl.append(oid + format + fileName);

            return (fcsUrl.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

   /**
     * This function will get fcs urls of all required files associated with 
     * given as an array of object ids, file name and file format.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of FileRequest objects having object id, file format and name
     * @return FileResponse
     * @since 
     */
	public FileResponse getCheckOutURLs(String username, String password,
			FileRequest[] request) 
	{
		FileResponse response = null;
		
		try {
			response = getCheckOutURLs(getContext(username, password), request);
			
		}catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
    
	//for rest 
	public FileResponse getCheckOutURLs(Context context, String[] args) 
	{
		FileResponse response = null;
		try {
		Map inputMap = JPO.unpackArgs(args);
		FileRequestWrapper getCheckOutURLs =(FileRequestWrapper) inputMap.get("Key");
		FileRequest[] request =getCheckOutURLs.getRequest();
		
		
		
			response = getCheckOutURLs(context, request);
			
		}catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		return response;
	}
    
   /**
     * This function will get fcs urls of all required files associated with 
     * given as an array of object ids, file name and file format.
     * @param context session context
     * @param request input list of FileRequest objects having object id, file format and name
     * @return FileResponse
     * @since 
     */
	protected FileResponse getCheckOutURLs(Context context,
			FileRequest[] request)throws MatrixException {
		FileResponse response = null;
		
		log("File.getCheckOutURLs start");
		int iSize = request.length;
		try
		{
			response = new FileResponse();
			FileResult[] objFileResults = new FileResult[iSize];
			for (int j = 0; j < iSize; j++) {
				FileRequest objRequest = request[j];
				String objectId = objRequest.getObjectId();
				String strFormat = objRequest.getFileFormat();
				String strFileName = objRequest.getFileName();
				String strMCSURL = objRequest.getMcsURL();
				
				String strFCSURL = getFCSUrl(context, strMCSURL, objectId, strFileName, strFormat);
				if(strFCSURL.length()> 0)
				{
					objFileResults[j] = new FileResult();
					objFileResults[j].setUrl(strFCSURL);
				}else{
					//No URL Found
					log("File.getCheckOutURLs NO URL found for Object:" + objectId + " File Name:" + strFileName + " File Format:" + strFormat );
				}
			}//For loop end
			//Add all result to response
			
			response.setResults(objFileResults);
			
		}catch(Exception ex)
		{
			log("File.getCheckOutURLs exception " + ex);
			
		} finally {
		 
			 
		}
		
		
		log("File.getCheckOutURLs end");
		return response;
	}
    
	
    /**
     * This method will return RawFileResponse object containing 
     * streamed output of zipped file containing mutliple requested files.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request FileRequest[] object containing object id, file name and file format 
     * @return RawFileResponse
     */
    
	public RawFileResponse getFilesAsZip(String username, String password,
			FileRequest[] request) {
		//FileResponse result = null;
		RawFileResponse result = null;
		try {
			result = getFilesAsZip(getContext(username, password), request);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	//for rest
	public RawFileResponse getFilesAsZip(Context context, String [] args) {
		//FileResponse result = null;
		RawFileResponse result = null;
		try {
		Map inputMap = JPO.unpackArgs(args);
		FileRequestWrapper getFileAsZip = (FileRequestWrapper) inputMap.get("Key");
		FileRequest[] request = getFileAsZip.getRequest();		
		result = getFilesAsZip(context, request);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
			
		}
		return result;
	}
	
	protected RawFileResponse getFilesAsZip(Context context, FileRequest[] request)
		throws MatrixException {
		log("File.getFilesAsZip start");
		RawFileResponse response = null;
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;
		String strZipFilePath;
		int iSize = request.length;
		try {
			// Path is upto newly created folder in workspace directory
			String strWorkspace = context.createWorkspace();
			int iindex = strWorkspace.lastIndexOf("\\");
			//Retrieve name of workspace
			String strWsName = strWorkspace.substring(iindex + 1);
			// File object for workspace folder.
			File fileWs = new File(strWorkspace);
			// Destination zip file with complete path. use time stamp for unique name
			strZipFilePath = strWorkspace + "\\" + strWsName + new Date().getTime()+ ".zip";
			fileWriter = new FileOutputStream(strZipFilePath);
			zip = new ZipOutputStream(fileWriter);
			byte[] buf = new byte[1024];

			for (int j = 0; j < iSize; j++) {
				FileRequest objRequest = request[j];
				String objectId = objRequest.getObjectId();
				String strFormat = objRequest.getFileFormat();
				String strFileName = objRequest.getFileName();
				BusinessObject bo = new BusinessObject(objectId);
				bo.open(context);
				bo.checkoutFile(context, false, strFormat,
						strFileName, strWorkspace);
				bo.close(context);
				String strFileToZip = fileWs.getAbsolutePath() + "\\"
							+ strFileName;
				
				File fileToDelete = new File(strFileToZip);
				//Check whether file exists
				if (fileToDelete.isFile())
				{
					int iFileCount = j + 1;
					String strtoZipFileName = Integer.toString(iFileCount)
							+ "_" + strFileName;
					
					FileInputStream in = new FileInputStream(strFileToZip);
					zip.putNextEntry(new ZipEntry(strtoZipFileName));
					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						zip.write(buf, 0, len);
					}
					// Complete the entry
					zip.closeEntry();
					in.close();
					// Delete added file here.
					fileToDelete.delete();
					fileToDelete = null; 
				}

			}// End of For loop
			zip.close();
			zip = null;
			fileWriter = null;
	 
			File fileZip = new File(strZipFilePath);
			InputStream in = new FileInputStream(fileZip);
			response = new RawFileResponse();
			//Need to write data in multiple buffers in the loop
			long dataLength = fileZip.length();
			//Divide this file length into multiple parts
			int bufferSize =  10485760;
			// Figure out how many loops we'll need to write the 100 MB chunk
			int bufferLoops =(int) ((dataLength + (bufferSize - 1)) / bufferSize);
			if (bufferSize > dataLength){
				bufferSize = (int) dataLength;
			}
			RawFileResult[] results = new RawFileResult[bufferLoops];
			
			// Write the file data block to the output stream
			int lngOffset = 0;
			int iByttArraySize = (int) fileZip.length();
			
			if(iByttArraySize != dataLength){
				log("File.getFilesAsZip:Byte Allocation Size is smaller than actual file size");
			}
			
			byte[] buf1 = new byte[iByttArraySize];
			for(int i=0; i<bufferLoops; i++)
			{
				RawFileResult result = new RawFileResult();
				results[i] = result;
				byte[] byteResult = null;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int read;
				read = in.read(buf1,lngOffset,bufferSize);
				if(read!= -1)
				{
					bos.write(buf1, lngOffset, bufferSize);
					byteResult = bos.toByteArray();
				 	results[i].setFileData(byteResult);
				 	lngOffset = lngOffset + bufferSize;
				    if((lngOffset + bufferSize) > dataLength )
				    {
				    	bufferSize = ( int) ( dataLength - lngOffset);
				    } 
				}else
				{
					log("File.getFilesAsZip error in writing multiple stream at position " + i);
					
				}
			}
			response.setResults(results);
			in.close();//Close input stream
			// Delete zip file also.
			fileZip.delete();
			

		} catch (Exception e) {
			log("File.getFilesAsZip exception " + e);
		} finally {
			context.deleteWorkspace();
			zip = null;
			fileWriter = null;
		}
		
		log("File.getFilesAsZip end");
		return response;
}

    /**
     * This method will return the list of files associated to the object whose id is sent as input
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input object id
     * @return FileResponse
     */
    public FileResponse getFiles(String username, String password, FileRequest request)
    {   
        return getFiles(getContext(username,password),request);
    }

    /**
     * This method will return the list of files associated to the object whose id is sent as input
     * This method is for testing purpose only
     *
     * @param context the matrix context
     * @param args the array should contain FileRequest
     * @return FileResponse
     */
    public FileResponse getFiles(Context context, String args[]) throws Exception
    {
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        FileRequest request = (FileRequest)programMap.get("request");

        return getFiles(context, request);
    }    
    
    /**
     * This function will return the list of files associated to the object whose id is sent as input
     *
     * @param context the matrix context
     * @param request input object id
     * @return FileResponse
     * @since AEF X+3
     */
    public FileResponse getFiles(Context context, FileRequest request)
    {
                FileResponse response = null;

                log("File.getFiles start");

                try
                {
    	            String objectId = request.getObjectId();
    	            DomainObject object = new DomainObject(objectId);
    	            String type = object.getInfo(context, DomainObject.SELECT_TYPE);
    	            NodeType node = (NodeType) nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", type, true));
    	            String documentRelationshipPattern = null;
    	            if(node != null)
    	            {
    	            	documentRelationshipPattern = node.giveDocumentRelationshipPattern();
    	            }
    	            if(documentRelationshipPattern == null)
    	            {
    	            	documentRelationshipPattern = "";
    				}

    				HashMap paramMap = new HashMap();
    				paramMap.put("objectId", objectId);
    				paramMap.put("parentRelName",documentRelationshipPattern);

    				String[] args = JPO.packArgs(paramMap);
    				MapList documents =(MapList)JPO.invoke(context, "emxCommonDocumentUI", null, "getDocuments", args, MapList.class);

    				Map documentMap = new HashMap();
    				String documentId;
    				MapList totalListOfFiles = new MapList();
    				MapList listOfFiles = new MapList();

    				if (documents != null && documents.size() > 0)
    				{
    					for (int i =0; i < documents.size(); i++)
    					{
    						documentMap = (Hashtable) documents.get(i);
    						documentId = (String) documentMap.get("id");
    						boolean isVersionable = CommonDocument.allowFileVersioning(context, documentId);

    						paramMap = new HashMap();
    						paramMap.put("objectId", documentId);
    						args = JPO.packArgs(paramMap);
    						if( isVersionable)
    						{
    						listOfFiles =(MapList)JPO.invoke(context, "emxCommonFileUI", null, "getFiles", args, MapList.class);
    						} else {
    							listOfFiles =(MapList)JPO.invoke(context, "emxCommonFileUI", null, "getNonVersionableFiles", args, MapList.class);    							
    						}

    						if(listOfFiles != null && listOfFiles.size() > 0)
    						{
    							for (int k =0; k < listOfFiles.size(); k++)
    							{
    								totalListOfFiles.add(listOfFiles.get(k));
    							}
    						}
    					}
    				}
    				
                    //  add the files from this object, if "SELF" is part of rel pattern  
    				if (documentRelationshipPattern != null && !"".equals(documentRelationshipPattern) && documentRelationshipPattern.indexOf("SELF") >= 0 )
    				{
    					paramMap = new HashMap();
						paramMap.put("objectId", objectId);
						args = JPO.packArgs(paramMap);
						boolean isVersionable = CommonDocument.allowFileVersioning(context, objectId);						
						if( isVersionable)
						{
						listOfFiles =(MapList)JPO.invoke(context, "emxCommonFileUI", null, "getFiles", args, MapList.class);
						} else {
							listOfFiles =(MapList)JPO.invoke(context, "emxCommonFileUI", null, "getNonVersionableFiles", args, MapList.class);    							
						}
						if(listOfFiles != null && listOfFiles.size() > 0)
						{
							for (int k =0; k < listOfFiles.size(); k++)
							{
								totalListOfFiles.add(listOfFiles.get(k));
    						}
    					}
    				}



					Map cgmFileMap = getDrawingFileMap(context, object);
                    if (cgmFileMap != null )
					{
						totalListOfFiles.add(cgmFileMap);
					}


    				int fileCount = totalListOfFiles.size();
    				response = new FileResponse();
    				FileResult[] results = new FileResult[fileCount];
    				FileResult result;
    				Map fileMap;
    				String timestamp;

    				for (int i = 0; i < fileCount; i++)
                	{
    					fileMap = (Map) totalListOfFiles.get(i);
    					result = new FileResult();
    					results[i] = result;

    					// set the object id and object type hash name into the result
    					String fileId = (String) fileMap.get("fileId");
    					if(fileId == null || "".equals(fileId))
    					{
    						fileId = (String) fileMap.get("objectId");
    					}
    					result.setObjectId(fileId);
    					result.setFileName((String) fileMap.get(DomainObject.SELECT_FILE_NAME));
    					result.setFileFormat((String) fileMap.get(DomainObject.SELECT_FILE_FORMAT));
    					result.setFileSize((String) fileMap.get(DomainObject.SELECT_FILE_SIZE));

    					// set the file modified timestamp, this is used by the client for caching
    					timestamp = (String) fileMap.get(DomainObject.SELECT_FILE_MODIFIED);
                        Date date = eMatrixDateFormat.getJavaDate(timestamp);
                        int timestampIntValue = new Double((date.getTime() / 1000)).intValue();
                        result.setTimestamp(timestampIntValue);
    				}

    				response.setResults(results);
    		    }
    			catch (Exception e)
    			{
    				log("File.getFiles exception " + e);
    				// do nothing
    			}

    			log("File.getFiles complete");
    			return (response);
    }


    protected HashMap getDrawingFileMap(Context context, DomainObject object)
    throws Exception
    {
    	log("File.getDrawingFileMap start");
    	HashMap cgmFileMap = null;

		StringList pathList = FrameworkUtil.split(cgmFilePath, ",");
		String strCMGFormat = PropertyUtil.getSchemaProperty(context, cgmFileFormat);
		String selectFormatFiles = "format["+strCMGFormat+"].file.name";
		String selectFileTimestamps = "format["+strCMGFormat+"].file.modified";
		String selectFileSize = "format["+strCMGFormat+"].file.size";

		String cgmFileName = null;
		String cgmFileSize = null;
		String timestampValue = null;
		for(int i=0; i<pathList.size(); i++)
		{
			StringList objectsList = new StringList();
			String symbolicImagePath = (String)pathList.get(i);
			String strImagePath = PlmProviderUtil.substituteValues(context, symbolicImagePath);
			log("File.getFiles CMG path " + strImagePath);
			objectsList = (StringList)object.getInfoList(context, strImagePath);
			log("File.getFiles CMG objects found " + objectsList);
			if(objectsList != null && objectsList.size() >0)
			{
				for(int j=0; j<objectsList.size(); j++)
				{
					String imageId = (String)objectsList.get(j);
					DomainObject imageObject = DomainObject.newInstance(context, imageId);
					// could combine the next two calls into a single call to getInfo() but since we
					// are in a transaction it may not be too bad a performance hit
					StringList fileList			 = (StringList)imageObject.getInfoList(context, selectFormatFiles);
					log("File.getFiles <<"+strCMGFormat+">> format files found " + fileList);
					if(fileList != null && fileList.size() > 0)
					{
						for (int k=0; k<fileList.size(); k++)
						{
							// for now just return the first file
							cgmFileName = (String) fileList.elementAt(k);
							log("File.getFiles CMG file <<"+cgmFileName+">>");

							// just in case multiple files checked in under the same format
							if (!getFileExtension(cgmFileName).equalsIgnoreCase("CGM"))
							{
								log("But skipping, wrong extension");
								continue;
							}
							StringList fileSizeList		 = (StringList)imageObject.getInfoList(context, selectFileSize);
							StringList fileTimestampList = (StringList)imageObject.getInfoList(context, selectFileTimestamps);

							cgmFileSize	   = (String) fileSizeList.elementAt(k);
							timestampValue = (String) fileTimestampList.elementAt(k);
							cgmFileMap = new HashMap();

							cgmFileMap.put("fileId", imageId);
							cgmFileMap.put(DomainObject.SELECT_FILE_NAME, cgmFileName);
							cgmFileMap.put(DomainObject.SELECT_FILE_FORMAT, strCMGFormat);
							cgmFileMap.put(DomainObject.SELECT_FILE_SIZE, cgmFileSize);
							cgmFileMap.put(DomainObject.SELECT_FILE_MODIFIED, timestampValue);

							break;
						}
					}
					if (cgmFileName != null && cgmFileName.length() > 0)
					{
						break;
					}
				}
			}
			if (cgmFileName != null && cgmFileName.length() > 0)
			{
				break;
			}
		}



        log("File.getDrawingFileMap complete");
        return cgmFileMap;

    }

    /**
    * This method is used to return the extension of the given file name.
    *
    * @param strFileName the complete name of the file
    * @return file extension
    * @since AEF 10.7.1.0
    */
   static private String getFileExtension(String strFileName) {
       int index = strFileName.lastIndexOf('.');

       if (index == -1)
       {
           return strFileName;
       } else
       {
           return strFileName.substring(index + 1, strFileName.length());
       }
   }

    /**
     * This function returns the ticket and URL to upload a file
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input FileRequest, this contains the objectID, file Name to upload, file format (can be left blank), MCS url
     * @return String[], first element is the URL to upload the file, second element is the ticket to upload
     * @since AEF X+4
     */
    public Ticket getUploadTicket (String username, String password, FileRequest request)
    {
    	log("File.getUploadTicket Start");
    	Ticket ticket = new Ticket();

		try
		{
			initContext(username, password);
			Context context = getContext();

			String objectID = request.getObjectId();
			String fileName = request.getFileName();
			String mcsURL = request.getMcsURL();
			log("File.getUploadTicket objectID :" + objectID);
			log("File.getUploadTicket fileName :" + fileName);
			log("File.getUploadTicket mcsURL :" + mcsURL);

			// always connect the Markup object to the Minor object
			// this way we can easily manage seperate Markup object for every version of the CAD Model
			// bcz, you have one minor object for every version of CAD model
			DomainObject markUpHolderObject = getMinorObject(context, objectID);
			String markupID = getMarkupID(context, markUpHolderObject.getObjectId(context));
			log("File.getUploadTicket markupID :" + markupID);

			/* to do
			 * add  markup name generation logic
			 */
			if(markupID == null)
			{
				CommonDocument markup = (CommonDocument) DomainObject.newInstance(context,"Markup");
                markup.createObject(context, "Markup", markup.getUniqueName("Mkp-"), null, "Markup", context.getVault().getName());

                ContextUtil.pushContext(context);
                markUpHolderObject.connect(context, new RelationshipType("Markup"), true,markup);
                ContextUtil.popContext(context);

				markup.createVersion(context, "", fileName, null);
				// markup.connect(context, "Markup", (DomainObject) parentObject,false);
				markupID = markup.getObjectId(context);
				log("File.getUploadTicket markupID created :" + markupID);
			}

			/* to do
			 * if markupId is not null: if the filename is different
			 * then deleteVersion that corresponds to the existing file
			 */


			ArrayList bops = new ArrayList();
			BusinessObjectProxy bproxy = new BusinessObjectProxy(markupID, "Markup", fileName, false, false);
			bops.add(bproxy);
			log("File.getUploadTicket before ticket genration :");
			TicketWrapper ticketWrapper = CheckinStart.doIt(context, mcsURL, bops);

			ticket.setActionURL(ticketWrapper.getActionURL());
			ticket.setExportString(ticketWrapper.getExportString());

			log("File.getUploadTicket after ticket genration [0]:" +ticket.getActionURL());
			log("File.getUploadTicket after ticket genration [1]:" +ticket.getExportString());
		}
		catch (Exception e)
		{
			log("checkinStart exception " + e);
			// result[0] = "ERROR:" + e.getMessage();
		}
		log("File.getUploadTicket complete");
		return ticket;

    }

    //for rest
    public Ticket getUploadTicket (Context context, String args[])
    {
    	Ticket ticket = new Ticket();
    	try
		{
    	Map inputMap = JPO.unpackArgs(args);
    	FileRequest request = (FileRequest)inputMap.get("Key");
    	log("File.getUploadTicket Start");
    	

		
			

			String objectID = request.getObjectId();
			String fileName = request.getFileName();
			String mcsURL = request.getMcsURL();
			log("File.getUploadTicket objectID :" + objectID);
			log("File.getUploadTicket fileName :" + fileName);
			log("File.getUploadTicket mcsURL :" + mcsURL);

			// always connect the Markup object to the Minor object
			// this way we can easily manage seperate Markup object for every version of the CAD Model
			// bcz, you have one minor object for every version of CAD model
			DomainObject markUpHolderObject = getMinorObject(context, objectID);
			String markupID = getMarkupID(context, markUpHolderObject.getObjectId(context));
			log("File.getUploadTicket markupID :" + markupID);

			/* to do
			 * add  markup name generation logic
			 */
			if(markupID == null)
			{
				CommonDocument markup = (CommonDocument) DomainObject.newInstance(context,"Markup");
                markup.createObject(context, "Markup", markup.getUniqueName("Mkp-"), null, "Markup", context.getVault().getName());

                ContextUtil.pushContext(context);
                markUpHolderObject.connect(context, new RelationshipType("Markup"), true,markup);
                ContextUtil.popContext(context);

				markup.createVersion(context, "", fileName, null);
				// markup.connect(context, "Markup", (DomainObject) parentObject,false);
				markupID = markup.getObjectId(context);
				log("File.getUploadTicket markupID created :" + markupID);
			}

			/* to do
			 * if markupId is not null: if the filename is different
			 * then deleteVersion that corresponds to the existing file
			 */


			ArrayList bops = new ArrayList();
			BusinessObjectProxy bproxy = new BusinessObjectProxy(markupID, "Markup", fileName, false, false);
			bops.add(bproxy);
			log("File.getUploadTicket before ticket genration :");
			TicketWrapper ticketWrapper = CheckinStart.doIt(context, mcsURL, bops);

			ticket.setActionURL(ticketWrapper.getActionURL());
			ticket.setExportString(ticketWrapper.getExportString());

			log("File.getUploadTicket after ticket genration [0]:" +ticket.getActionURL());
			log("File.getUploadTicket after ticket genration [1]:" +ticket.getExportString());
		}
		catch (Exception e)
		{
			log("checkinStart exception " + e);
			// result[0] = "ERROR:" + e.getMessage();
		}
		log("File.getUploadTicket complete");
		return ticket;

    }

    /**
     * this utility method returns Markup object ID, if it exists
     * @param context the eMatrix <code>Context</code> object
     * @param objectID - objectID for which Markup is needed
     * @return objectID of the Markup object
     */
    private String getMarkupID(Context context, String objectID) throws Exception
    {
    	log("File.getMarkupID Start");
    	String markupID = null;
    	DomainObject parentObject = new DomainObject(objectID);

        // object selectables
        StringList objectSelects = new StringList(1);
        objectSelects.add(DomainObject.SELECT_ID);

        /* to do
           change this to add where clause, for just Markup type, not any sub-types
        */
        Map map = parentObject.getRelatedObject(context, "Markup", true,objectSelects, null);
        if (map != null)
        {
            markupID = (String) map.get(DomainObject.SELECT_ID);
        }
        log("File.getMarkupID complete");
    	return markupID;
    }

    /**
	 * This service finishes the check-in .
	 *
	 * @param username the matrix user name
	 * @param password password of the user
	 * @param receipt, the return from the upload of the file used to finalize the check-in
	 * @return a string containing success or error message
	 * @since AEF X+4
	 */
	public String moveFileToVault (String username, String password, String receipt)
	{
		// to do//
		log("checkinEnd start");
		String result = "Success";

		try
		{
			initContext(username, password);
			Context context = getContext();
			CheckinEnd.doIt(context, receipt);
		}
		catch (Exception e)
		{
			log("checkinEnd exception " + e);
			result = "ERROR:" + e.getMessage();
		}
		log("checkinEnd end");
		return result;
    }
	//for rest
	public String moveFileToVault (Context context, String [] args)
	{
		String result = "Success";
		try
		{
		Map inputMap = JPO.unpackArgs(args);
		String receipt = (String) inputMap.get("receipt");
		// to do//
		log("checkinEnd start");
		
		System.out.println(receipt);
		
			//initContext(username, password);
			//Context context = getContext();
			CheckinEnd.doIt(context, receipt);
		}
		catch (Exception e)
		{
			log("checkinEnd exception " + e);
			result = "ERROR:" + e.getMessage();
		}
		log("checkinEnd end");
		return result;
    }


    /**
     * dummy method for testing purposes only
     * @param args
     */
    public static void main(String args[])
    {
    	FileBase_mxJPO nr = new FileBase_mxJPO();
    	FileRequest nrr = new FileRequest();
		String objId = "40896.50684.50552.50595"; //"40896.50684.38002.26937";
		String strFileName = "CATPRD-0024.CATProduct";// "Gears.CATPart";//
		String strFormat = "asm";// "CATPart"; //

		nrr.setFileFormat(strFormat);
		nrr.setFileName(strFileName);
		nrr.setObjectId(objId);
		nrr.setMcsURL("http://localhost:8090/enovia");

		FileRequest[] listRequest = new FileRequest[1];

		listRequest[0] = nrr;
 
		
	    RawFileResponse response = 	nr.getFilesAsZip("yap", "yap", listRequest);
		RawFileResult [] results = response.getResults();
		int iLength = results.length;
		
		
		// To test whether buffer is correct or not
		 
		 File createdFile = new File("C:\\test.zip"); 
		 FileOutputStream fos;
		try {
			fos = new FileOutputStream(createdFile);
  		    for (int k=0; k < iLength; k++){
			  fos.write(results[k].getFileData());
			}
			fos.flush();
			fos.close();
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	System.exit(0);
    }
}
