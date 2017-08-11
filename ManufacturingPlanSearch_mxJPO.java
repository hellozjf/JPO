/*
 * ManufacturingPlanSearch
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * 
 */
import matrix.db.*;


public class ManufacturingPlanSearch_mxJPO extends ManufacturingPlanSearchBase_mxJPO
{
    /**
   * This JPO class has some methods pertaining to Person type
     *
     * @param context context for this request
     * @param arg[0] the objectid
     * @return a ManufacturingPlanSearch
     * @exception Exception when unable to find object in the AEF
     * @since VR209
     * @grade 0
     */
    public ManufacturingPlanSearch_mxJPO (Context context, String[] args)
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
     * @since VR209
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
