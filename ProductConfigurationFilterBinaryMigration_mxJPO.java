
/*
 * ProductConfigurationFilterBinaryMigration.java program to migrate Filter Binary data in Product Configuration
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */	
import matrix.db.Context;

	public class ProductConfigurationFilterBinaryMigration_mxJPO extends
	ProductConfigurationFilterBinaryMigrationBase_mxJPO {
	    /**
	     *
	     * @param context
	     *            the eMatrix <code>Context</code> object
	     * @param args
	     *            holds no arguments
	     * @throws Exception
	     *             if the operation fails
	     * @since FTR V6R2012x
	     */
	    public ProductConfigurationFilterBinaryMigration_mxJPO(Context context,
	            String[] args) throws Exception {
	        super(context, args);
	    }

	}
