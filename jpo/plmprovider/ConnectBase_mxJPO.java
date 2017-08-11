package jpo.plmprovider;
// ${CLASSNAME}.java
//
// Created on Oct 3, 2006
//
// Copyright (c) 2005-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.util.Vector;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.plmprovider.PlmProviderUtil;
import com.matrixone.apps.plmprovider.ConnectResponse;

import matrix.db.Context;

/**
 * @author kcox
 *
 * The <code>${CLASSNAME}</code> class provides web services associated with connecting to
 * the MatrixOne server.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class ConnectBase_mxJPO extends jpo.plmprovider.Mat3DLive_mxJPO {
    
    private static String version = null;
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public ConnectBase_mxJPO() {        
    }
    
    /**
     * Verifies the given username/password and creates a session.
     * @param username the matrix user name
     * @param password password of the user
     * 
     * @return null for successful connection, otherwise an error message
     * @since AEF 10.7.1.0
     */
    public String doConnect(String username, String password) throws Exception {
    	
        log("connect: start");
        String s = null;
        try {
            s = initContext(username, password);
            log("connect: initContext OK");
        } catch (Exception e) {
            s = e.toString();
            log("connect: ERROR:"+e);
        }
        log("connect: returning: "+s);
        return s;
    }
    
    /**
     * This service returns the server version
     * @param username the matrix user name
     * @param password password of the user
     * 
     * @return server version (BPS, formerly known as AEF), null if there is an exception
     * @since BPS X+3
     */
    public String getServerVersion(String username, String password) {
    	log("getServerVersion: start");
    	// String version = null;
        try{
            if (version == null || "".equals(version))
            {
                Context context = getContext(username, password);
                version = FrameworkUtil.getApplicationVersion( context, "BusinessProcessServices");
            }
            
            // version = PropertyUtil.getAdminProperty(context, "program", "eServiceSystemInformation.tcl", "appVersionBusinessProcessServices");
            log("connect.getServerVersion: " + version);
        }
        catch(Exception e)
        {
            log("connect.getServerVersion exception " + e);
            e.printStackTrace(System.out);
            // do nothing      	
        }       
        
        log("connect.getServerVersion: returning: " + version);
        return version;
    }
    
    /**
     * Destroys the session created during doConnect.
     * 
     * @param username the matrix user name
     * @param password password of the user
     * 
     * @return SUCCESS/FAILURE
     * @since AEF 10.7.1.0
     */
    public String doDisconnect(String username, String password) 
    {
        
        log("Connect.doDisconnect  Start");
        String retValue = null;;
        try
        {
            PlmProviderUtil.cleanCache(password);
            retValue    = "SUCCESS";
        }
        catch (Exception ex)
        {
            log("Connect.doDisconnect Exception "+ex);
            ex.printStackTrace(System.out);
            retValue    = "FAILURE";
        }
        log("Connect.doDisconnect  End " +retValue);
        return retValue;
    }
    
    
    /**
     * Cleans the context cache.
     * 
     * This method will be called from timer servlet after specified time interval
     *
     * @param context the matrix context
     * @param args the String array
     * 
     * @since BPS R209
     */
    public void cleanCache(Context context, String[] args)throws Exception
    {
        log("Connect.cleanCache  Start");
        try
        {
            PlmProviderUtil.cleanCache();
        }
        catch (Exception ex)
        {
            log("Connect.cleanCache Exception "+ex);
            ex.printStackTrace(System.out);
        }
        log("Connect.cleanCache  End");
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
        log("getSecurityContextAssignments:  Start");
        ConnectResponse connect = new ConnectResponse();
        try{       

            connect.setEncryptedKey(initContext(userName, password));
            Context context = getContext();
            connect.setSecurityContextAssignments(PersonUtil.getSecurityContextAssignments(context));
            connect.setdefaultSecurityContext(PersonUtil.getDefaultSecurityContext(context));
            
        } catch (Exception ex) {
            log("getSecurityContextAssignments: ERROR:"+ex.toString());
        }
        log("getSecurityContextAssignments:  End");
        return connect;
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
        log("doConnectWithSecurityContext: start");
        String s = null;
        try {
            s = initContext(userName, password, securityContext);
            log("doConnectWithSecurityContext: initContext OK");
        } catch (Exception e) {
            s = e.toString();
            log("doConnectWithSecurityContext: ERROR:"+e);
        }
        log("doConnectWithSecurityContext: returning: "+s);
        return s;
    }

}
