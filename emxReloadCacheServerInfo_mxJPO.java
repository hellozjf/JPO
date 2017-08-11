/*
 *  emxReloadCacheServerInfo.java
 *
 * Copyright (c) 2004-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.Context;

/**
 * The <code>emxReloadCacheServerInfo</code> class extends the base class that
 * contains code for properties to remotely reload cache.
 * @version AEF 10.5.SP1 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxReloadCacheServerInfo_mxJPO extends emxReloadCacheServerInfoBase_mxJPO
{
    /**
     * Constructor
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public emxReloadCacheServerInfo_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);

        /*
            Follwing is the example for configuring remote App server List:
            APP_SERVER_LIST = new String[] {
                                            "http://myserver1:7001/ematrix",
                                            "http://myserver2:7001/ematrix"
                                           };
        */

        // Update the follwing variable to include your app server list.
        // Note: Do not update this, in case of single APP server setup
        APP_SERVER_LIST = new String[] {};


        /*
            Follwing is the example for configuring RMI Gateway Server List:
            RMI_SERVER_LIST = new String[] {
                                            "//myserver01:1100",
                                            "//myserver01:1101"
                                           };
        */
        // Update the follwing variable to include your RMI gateway server list.
        // Note: Do not update this, in case of single RMI server or RIP setup
        RMI_SERVER_LIST = new String[] {};
    }

}
