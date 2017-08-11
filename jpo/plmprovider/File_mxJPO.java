package jpo.plmprovider;
// ${CLASSNAME}.java
//
// Created on 12-21-2007
//
// Copyright (c) 2006-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import com.matrixone.apps.plmprovider.FileRequest;
import com.matrixone.apps.plmprovider.FileResponse;
import com.matrixone.apps.plmprovider.FileResult;
import com.matrixone.apps.plmprovider.RawFileResponse;
import com.matrixone.apps.plmprovider.RawFileResult;
import com.matrixone.apps.plmprovider.Ticket;

import matrix.util.MatrixWrappedService;

/**
 * @author prasad
 *
 * The <code>File</code> class provides web services related to
 * associated files of an object viewed in 3DLive
 *
 * @version AEF X+3 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class File_mxJPO extends jpo.plmprovider.FileBase_mxJPO implements MatrixWrappedService
{
    /**
     * Constructor.
     *
     * @since AEF X+3
     */

    public File_mxJPO()
    {
    }

    /**
     * This function will get FCS urls for all the objects in the request object
     * given as an array of objIds.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of object ids
     * @return FileResponse
     * @since AEF X+3
     */

    public FileResponse getUrl(String username, String password, FileRequest request)
    {
        return (super.getUrl(username, password, request));
    }

    public FileResponse getFiles(String username, String password, FileRequest request)
    {
        return (super.getFiles(username, password, request));
    }

    public Ticket getUploadTicket (String username, String password, FileRequest request)
    {
    	return (super.getUploadTicket(username, password, request));
    }

	public String moveFileToVault (String username, String password, String receipt)
	{
		return (super.moveFileToVault(username, password, receipt));
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
	public FileResponse getCheckOutURLs(String username, String password, FileRequest[] request)
    {
         return (super.getCheckOutURLs(username, password, request));
    }

    /**
     * This function will get zipped stream of all required files associated with the
     * given array of object ids, file name and file format.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of FileRequest objects having object id, file format and file name
     * @return RawFileResponse
     * @since R215
     */


	public RawFileResponse getFilesAsZip(String username, String password, FileRequest[] request)
    {
         return (super.getFilesAsZip(username, password, request));
    }

    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return RawFileResult object
     * @since R215
     */

	public RawFileResult unusedRawFileResult()
	{
		return null;
	}

    // The public unused* methods exist merely to expose the return type to ServiceGenerator,
    // for proper registration of the Axis serializer.
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return FileResult object
     * @since AEF 10.7.1.0
     */
    public FileResult unusedFileResult()
    {
        return null;
    }

}
