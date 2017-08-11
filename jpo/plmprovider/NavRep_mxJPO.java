package jpo.plmprovider;
// ${CLASSNAME}.java
//
// Created on 12-19-2006
//
// Copyright (c) 2006-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import com.matrixone.apps.plmprovider.NavRepRequest;
import com.matrixone.apps.plmprovider.NavRepResponse;
import com.matrixone.apps.plmprovider.NavRepResult;

import matrix.util.MatrixWrappedService;

/**
 * @author mkeirstead
 *
 * The <code>${CLASSNAME}</code> class provides web services associated with providing
 * graphical representations of a tree structure.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class NavRep_mxJPO extends jpo.plmprovider.NavRepBase_mxJPO implements MatrixWrappedService
{
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public NavRep_mxJPO()
    {
    }

    /**
     * This function will get FCS urls for all the objects in the request object
     * given as an array of objIds.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of object ids
     * @return NavRepResponse
     * @since AEF 10.7.1.0
     */

    public NavRepResponse getUrls(String username, String password, NavRepRequest request)
    {
        return (super.getUrls(username, password, request));
    }


    /**
     * This function will get urls with fake server name but proper associated object ids
     * file name and file format. The same input will be used later in 
     * getFilesAsZip web service function.     
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of object ids
     * @return NavRepResponse
     * @since XXXXXXXX
     */
    
   public NavRepResponse getStaticUrls(String username, String password,NavRepRequest request) {
		super.doNotCheckout = true;
		return (super.getUrls(username, password, request));
  }
	



    // The public unused* methods exist merely to expose the return type to ServiceGenerator,
    // for proper registration of the Axis serializer.
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return NavRepResult object
     * @since AEF 10.7.1.0
     */
    public NavRepResult unusedNavRepResult()
    {
        return null;
    }

}
