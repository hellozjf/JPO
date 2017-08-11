/*
 * ${CLASSNAME}.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 */

import matrix.db.*;
import java.io.*;

import com.matrixone.apps.configuration.ConfigurationConstants;

public class emxVariantConfigurationFindObjectsBase_mxJPO extends
        emxCommonFindObjectsBase_mxJPO {
    

	/**
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @throws Exception
     *             if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
	public emxVariantConfigurationFindObjectsBase_mxJPO(Context context,
			String[] args) throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}

    /**
     * This method is executed to find the object ids for migration.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @returns nothing
     * @throws Exception
     *             if the operation fails
     */
    public int findObjects(Context context, String[] args) throws Exception {

        int chunkSize = 0;

        if (!context.isConnected()) {
            throw new Exception("not supported on desktop client");
        }

        try {
            writer = new BufferedWriter(new MatrixWriter(context));
            if (args.length < 2) {
                throw new IllegalArgumentException();
            }

            chunkSize = Integer.parseInt(args[0]);
            if (chunkSize == 0 || chunkSize < 1) {
                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException iExp) {
            writer
                    .write("=================================================================\n");
            writer
                    .write("Wrong number of arguments Or Invalid number of Oids per file\n");
            writer.write("Step 1 of Migration     : FAILED \n");
            writer
                    .write("=================================================================\n");
            writer.close();
            return 0;
        }

        String[] sargs = new String[3];
        sargs[0] = "" + chunkSize;
        sargs[1] = ConfigurationConstants.TYPE_MASTER_FEATURE;
        sargs[2] = "" + args[1];
        super.mxMain(context, sargs);

        return 0;
    }
    
    /**
     * This method is executed to find the object ids for migration.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @returns nothing
     * @throws Exception
     *             if the operation fails
     */
    public int findMFnModObjects(Context context, String[] args) throws Exception {

        int chunkSize = 0;

        if (!context.isConnected()) {
            throw new Exception("not supported on desktop client");
        }

        try {
            writer = new BufferedWriter(new MatrixWriter(context));
            if (args.length < 2) {
                throw new IllegalArgumentException();
            }

            chunkSize = Integer.parseInt(args[0]);
            if (chunkSize == 0 || chunkSize < 1) {
                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException iExp) {
            writer
                    .write("=================================================================\n");
            writer
                    .write("Wrong number of arguments Or Invalid number of Oids per file\n");
            writer.write("Step 1 of Migration     : FAILED \n");
            writer
                    .write("=================================================================\n");
            writer.close();
            return 0;
        }

        String[] sargs = new String[3];
        sargs[0] = "" + chunkSize;
        sargs[1] =  ConfigurationConstants.TYPE_MASTER_FEATURE + ","
        + ConfigurationConstants.TYPE_MODEL ;
        sargs[2] = "" + args[1];
        super.mxMain(context, sargs);

        return 0;
    }
    
    /**
     * This method is executed to find the rel ids for Feature allocation type attr value migration.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @returns nothing
     * @throws Exception
     *             if the operation fails
     */
    public int findRelwithFeaAllocType(Context context, String[] args) throws Exception {

        int chunkSize = 0;

        if (!context.isConnected()) {
            throw new Exception("not supported on desktop client");
        }

        try {
            writer = new BufferedWriter(new MatrixWriter(context));
            if (args.length < 2) {
                throw new IllegalArgumentException();
            }

            chunkSize = Integer.parseInt(args[0]);
            if (chunkSize == 0 || chunkSize < 1) {
                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException iExp) {
            writer
                    .write("=================================================================\n");
            writer
                    .write("Wrong number of arguments Or Invalid number of Oids per file\n");
            writer.write("Step 1 of Migration     : FAILED \n");
            writer
                    .write("=================================================================\n");
            writer.close();
            return 0;
        }

        String[] sargs = new String[3];
        sargs[0] = "" + chunkSize;
        sargs[1] =  ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST;
        sargs[2] = "" + args[1];
        super.mxMain(context, sargs);

        return 0;
    }
}
