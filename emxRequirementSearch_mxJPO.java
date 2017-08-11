/*
 * emxRequirementSearch
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENORequirementsManagementBase/CNext/Modules/ENORequirementsManagementBase/JPOsrc/custom/${CLASSNAME}.java 1.2.2.1.1.1 Wed Oct 29 22:20:01 2008 GMT przemek Experimental$
 */
import matrix.db.Context;

/**
 * @version ProductCentral 10-0-0-0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxRequirementSearch_mxJPO extends emxRequirementSearchBase_mxJPO
{
    /**
   * This JPO class has some methods pertaining to Person type
     *
     * @param context context for this request
     * @param arg[0] the objectid
     * @return a emxProductSearch
     * @exception Exception when unable to find object in the AEF
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public emxRequirementSearch_mxJPO (Context context, String[] args)
        throws Exception
    {
         super(context,args);
    }

    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @exception Exception when problems occurred in the AEF
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }

}
