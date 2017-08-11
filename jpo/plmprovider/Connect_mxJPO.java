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

import com.matrixone.apps.plmprovider.ConnectResponse;
import matrix.util.MatrixWrappedService;

/**
 * @author mkeirstead
 *
 * The <code>${CLASSNAME}</code> class provides web services associated with connecting to
 * the MatrixOne server.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class Connect_mxJPO extends jpo.plmprovider.ConnectBase_mxJPO implements MatrixWrappedService
{
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public Connect_mxJPO()
    {        
    }
    
    /**
     * Verifies the given username/password and creates a session.
     * @param username the matrix user name
     * @param password password of the user
     * 
     * @return null for successful connection, otherwise an error message
     * @since AEF 10.7.1.0
     */
    public String doConnect(String username, String password) throws Exception
    {
        return (super.doConnect(username, password));
    }
    
    public String getServerVersion(String username, String password) {
    	return (super.getServerVersion(username, password));
    }
    /**
     * Destroys the session created during doConnect.
     * 
     * @param username the matrix user name
     * @param password password of the user
     * 
     * @return SUCCESS/FAILURE
     * @since BPS R209
     */
    public String doDisconnect(String username, String password) 
    {
        return (super.doDisconnect(username, password));
    }
    
    
    /**
     * Returns Security Contexts for the given user
     * 
     * @param userName the matrix user name
     * @param password the user password
     * @return ConnectResponse consists of
     *      a) List of security contexts
     *      b) default security context
     *      c) encrypted key
     * 
     * @since BPS R214
     */
    public ConnectResponse getSecurityContextAssignments(String userName, String password) {
        return (super.getSecurityContextAssignments(userName, password));
    }
    
    
    /**
     * Creates a context with given user name, pass word and security context
     * 
     * if user name is null, (i.e context is already created before this call)
     * then it get the context details from password (encrypted key) 
     * 
     * @param userName the matrix user name
     * @param password the user password
     * @return String the encrypted key
     * 
     * @since BPS R214
     */
    public String doConnectWithSecurityContext(String userName, String password, String securityContext) {
        return (super.doConnectWithSecurityContext(userName, password, securityContext));
    }
}
