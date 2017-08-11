/*
 * emxCommonDocumentFindObjectsBase.java program to get all document type Object Ids.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;

public class emxProgramManagementFindObjectsBase_mxJPO extends emxCommonFindObjectsBase_mxJPO
{
	public static final StringList typeList = new StringList();
	static{
		typeList.add("Project Template");
		typeList.add("Project Concept");
		typeList.add("Project Space");
		typeList.add("Weekly Timesheet");
		typeList.add("Work Calendar");
	}

	public emxProgramManagementFindObjectsBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}


	/**
	 * This method is executed if a specific method is not specified.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 */
	public int mxMain(Context context, String[] args)
			throws Exception
			{
		int chunkSize = 0;

		if(!context.isConnected())
		{
			throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
		}

		try
		{
			writer = new BufferedWriter(new MatrixWriter(context));
			if (args.length < 2 )
			{
				throw new IllegalArgumentException();
			}

			chunkSize = Integer.parseInt(args[0]);
			if (chunkSize == 0 || chunkSize < 1 )
			{
				throw new IllegalArgumentException();
			}
		}
		catch (IllegalArgumentException iExp)
		{
			writer.write("=================================================================\n");
			writer.write("Wrong number of arguments Or Invalid number of Oids per file\n");
			writer.write("Step 1 of Migration     : FAILED \n");
			writer.write("=================================================================\n");
			writer.close();
			return 0;
		}

		try
		{
			long startTime = System.currentTimeMillis();
			documentDirectory = args[1];
			// documentDirectory does not ends with "/" add it
			String fileSeparator = java.io.File.separator;
			if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
			{
				documentDirectory = documentDirectory + fileSeparator;
			}

			writer.write("=======================================================\n\n");
			writer.write("                Querying for Program Central Objects...\n");
			writer.write("                ("+ chunkSize + ") Objects per File\n");
			writer.write("                Writing files to: " + documentDirectory + "\n\n");
			writer.write("=======================================================\n\n");
			writer.flush();

			ContextUtil.pushContext(context);
			ContextUtil.startTransaction(context,true);
			try
			{
				getIds(context, chunkSize);
			}
			catch(FileNotFoundException fnfExp)
			{
				throw fnfExp;
			}

			writer.write("\n=======================================================\n\n");
			writer.write("                Querying for Program Central Objects  COMPLETE\n");
			writer.write("                Time:"+ (System.currentTimeMillis() - startTime) + "ms \n");
			writer.write("                Step 1 of Migration         :  SUCCESS \n\n");
			writer.write("=======================================================\n\n");
			writer.flush();
			ContextUtil.commitTransaction(context);
		}
		catch (FileNotFoundException fEx)
		{
			// check if user has access to the directory
			// check if directory exists
			writer.write("=================================================================\n");
			writer.write("Directory does not exist or does not have access to the directory\n");
			writer.write("Step 1 of Migration     : FAILED \n");
			writer.write("=================================================================\n");
			writer.flush();

			ContextUtil.abortTransaction(context);
		}
		catch (Exception ex)
		{
			// abort if getIds fail
			writer.write("=================================================================\n");
			writer.write("Find Documents Query failed \n");
			writer.write("Step 1 of Migration     : FAILED \n");
			writer.write("=================================================================\n");
			writer.flush();

			ex.printStackTrace();
			ContextUtil.abortTransaction(context);
		}
		finally
		{
			writer.close();
			ContextUtil.popContext(context);
		}

		// always return 0, even this gives an impression as success
		// this way, matrixWriter writes to console
		// else writer.write statements do not show up in Application console
		// but it works in mql console
		return 0;
			}

	private String getTypeExpression(Context context) throws Exception{
		StringBuffer sbTypes = new StringBuffer(100);
		return FrameworkUtil.join(typeList, ",");
	}

	@Override
	public void getIds(Context context, int chunkSize) throws Exception
	{
		String sTypeExpression = getTypeExpression(context);
		String command = "temp query bus $1 $2 $3 vault $4 limit $5 where $6";
		String result ="";
		StringBuffer sbWhere = new StringBuffer();
		sbWhere.append("current matchlist 'Create,Assign,Active,Review,Hold,Concept,Prototype,Rejected,Submit,Approved' ','");	
		sbWhere.append(" && ");
		sbWhere.append("program[emxCommonMigration -method writeOID ${OBJECTID} \"${TYPE}\"] == true");


		//reset/set static variabless back to original values every time this JPO is run
		emxCommonMigration_mxJPO._counter  = 0;
		emxCommonMigration_mxJPO._sequence  = 1;
		emxCommonMigration_mxJPO._oidsFile = null;
		emxCommonMigration_mxJPO._fileWriter = null;
		emxCommonMigration_mxJPO._objectidList = null;

		//set statics
		//create BW and file first time
		if (emxCommonMigration_mxJPO._fileWriter == null)
		{
			try
			{
				emxCommonMigration_mxJPO.documentDirectory = documentDirectory;
				emxCommonMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
				emxCommonMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxCommonMigration_mxJPO._oidsFile));
				emxCommonMigration_mxJPO._chunk = chunkSize;
				emxCommonMigration_mxJPO._objectidList = new StringList(chunkSize);
			}
			catch(FileNotFoundException eee)
			{
				throw eee;
			}
		}

		try
		{
			writer.write("\n=======================================================\n\n");
			writer.write(command);
			result  = MqlUtil.mqlCommand(context,command,sTypeExpression,"*","*","*","1",sbWhere.toString());
		}
		catch(Exception me)
		{
			throw me;
		}

		// call cleanup to write the left over oids to a file
		emxCommonMigration_mxJPO.cleanup();
	}
}
