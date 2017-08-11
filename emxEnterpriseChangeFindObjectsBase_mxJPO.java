/*
 * emxEnterpriseChangeFindObjectsBase.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOEnterpriseChange/ENOECHJPO.mj/src/${CLASSNAME}.java 1.1.1.1 Thu Oct 28 22:27:16 2010 GMT przemek Experimental$
 */

import java.io.BufferedWriter;

import matrix.db.Context;
import matrix.db.MatrixWriter;

import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;

public class emxEnterpriseChangeFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO {


	/**
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @throws Exception
	 *             if the operation fails
	 * @since EnterpriseChange R211
	 * @grade 0
	 */
	public emxEnterpriseChangeFindObjectsBase_mxJPO(Context context, String[] args) throws Exception {
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
		sargs[1] = EnterpriseChangeConstants.TYPE_CHANGE_PROJECT + "," + EnterpriseChangeConstants.TYPE_CHANGE_TASK;
		sargs[2] = "" + args[1];
		super.mxMain(context, sargs);

		return 0;
	}

	/**
	 * This method is executed to find the relationship ids for migration.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @returns nothing
	 * @throws Exception
	 *             if the operation fails
	 */
	public int findRelationships(Context context, String[] args) throws Exception {

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
		sargs[1] = EnterpriseChangeConstants.RELATIONSHIP_APPLICABLE_ITEM;
		sargs[2] = "" + args[1];
		super.mxMain(context, sargs);

		return 0;
	}
	
	/**
	 * This method is executed to find the Decision object ids for migration.
	 *
	 * @param context the eMatrix Context object
	 * @param args - holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R212_HFDerivations
	 */
	public int findDecisionObjects(Context context, String[] args) throws Exception {
		try {
			int chunkSize = 0;

			if (!context.isConnected()) {
				throw new Exception("not supported on desktop client");
			}
			
			writer = new BufferedWriter(new MatrixWriter(context));
			if (args.length < 2) {
				throw new IllegalArgumentException();
			}

			chunkSize = Integer.parseInt(args[0]);
			if (chunkSize == 0 || chunkSize < 1) {
				throw new IllegalArgumentException();
			}
			
			String[] sargs = new String[3];
			sargs[0] = "" + chunkSize;
			sargs[1] = EnterpriseChangeConstants.TYPE_DECISION;
			sargs[2] = "" + args[1];
			super.mxMain(context, sargs);

			return 0;
			
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
	}

}

