/*
 * emxCommonMigrationBase.java
 * program for common migrate model.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.io.FileNotFoundException;
import java.io.FileWriter;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;

public class emxProgramMigrationBase_mxJPO extends emxCommonMigrationBase_mxJPO
{

	public emxProgramMigrationBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}

	public void logMigratedOids() throws Exception
	{
		convertedOidsLog.write(migratedOids.toString());
		convertedOidsLog.flush();
	}

	public void stampDefaultProjectAndOrganization(Context context)throws Exception {

	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 */
	public int mxMain(Context context, String[] args) throws Exception
	{
		if(!context.isConnected())
		{
			throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
		}
		int argsLength = args.length;
		error = "";

		//added for BugNo - 355683
		try
		{
			//getting the context user
			String contextUser=context.getUser();
			//checking whether context user has admin privilages
			String cmd = "print person '"+ contextUser +"' select system dump" ;
			String businessAccess = MqlUtil.mqlCommand(context,mqlCommand,cmd);
			//if context user does not have admin privilages then throwing Exception
			if("false".equalsIgnoreCase(businessAccess))
			{
				error = "User does not have Admin Privileges";
				throw new Exception(error);
			}
		}
		catch(Exception e)
		{
			writer.write("====================================================================\n");
			writer.write(e.getMessage() + " \n");
			writer.write("Step 2 of Migration :     FAILED \n");
			writer.write("====================================================================\n");
			// if scan is true, writer will be closed by the caller
			if(!scan)
			{
				writer.close();
			}
			return 0;
		} //End for BugNo - 355683

		try
		{
			// writer     = new BufferedWriter(new MatrixWriter(context));
			if (args.length < 3 )
			{
				error = "Wrong number of arguments";
				throw new IllegalArgumentException(error);
			}
			documentDirectory = args[0];

			// documentDirectory does not ends with "/" add it
			String fileSeparator = java.io.File.separator;
			if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
			{
				documentDirectory = documentDirectory + fileSeparator;
			}

			minRange = Integer.parseInt(args[1]);

			if ("n".equalsIgnoreCase(args[2]))
			{
				maxRange = getTotalFilesInDirectory();
				if(maxRange == 0){
					try{
						error = "There are no objects available for Migration";
						throw new IllegalArgumentException(error);
					}
					catch (IllegalArgumentException iExp){
						writer.write("====================================================================\n");
						writer.write(iExp.getMessage() + " \n");
						writer.write("Step 2 of Migration :     COMPLETED \n");
						writer.write("====================================================================\n");
						// if scan is true, writer will be closed by the caller
						if(!scan){
							writer.close();
						}
						return 0;
					}
				}
			} else {
				maxRange = Integer.parseInt(args[2]);
			}

			if (minRange > maxRange)
			{
				error = "Invalid range for arguments, minimum file range is greater than maximum file range value";
				throw new IllegalArgumentException(error);
			}

			if (minRange == 0 || minRange < 1 || maxRange == 0 || maxRange < 1)
			{
				error = "Invalid range for arguments, minimum/maximum file range value is 0 or negative";
				throw new IllegalArgumentException(error);
			}
			//Fix for the chunk size starts
			java.io.File minRangeFile = new java.io.File(documentDirectory + "objectids_" + minRange + ".txt");
			java.io.File maxRangeFile = new java.io.File(documentDirectory + "objectids_" + maxRange + ".txt");

			if(!minRangeFile.exists() || !maxRangeFile.exists()) {
				error = "Invalid range for arguments.  Either minimum or maximum file range does not exist";
				throw new IllegalArgumentException(error);
			}
			//
		}
		catch (IllegalArgumentException iExp)
		{
			writer.write("====================================================================\n");
			writer.write(iExp.getMessage() + " \n");
			writer.write("Step 2 of Migration :     FAILED \n");
			writer.write("====================================================================\n");
			// if scan is true, writer will be closed by the caller
			if(!scan)
			{
				writer.close();
			}
			return 0;
		}

		String debugString = "false";

		if( argsLength >= 4 )
		{
			debugString = args[3];
			if ( "debug".equalsIgnoreCase(debugString) )
			{
				debug = true;
			}
		}

		//String scanString = "scan";
		String scanString = "";
		if( argsLength >= 5 )
		{
			scanString = args[4];
			if ( "scan".equalsIgnoreCase(scanString) )
			{
				scan = true;
			}
		}
		try
		{
			createLogs();
		}
		catch(FileNotFoundException fExp)
		{
			// check if user has access to the directory
			// check if directory exists
			writer.write("=================================================================\n");
			writer.write("Directory does not exist or does not have access to the directory\n");
			writer.write("Step 2 of Migration :     FAILED \n");
			writer.write("=================================================================\n");
			// if scan is true, writer will be closed by the caller
			if(!scan)
			{
				writer.close();
			}
			return 0;
		}

		int i = 0;
		try
		{
			ContextUtil.pushContext(context);
			String cmd = "trigger off";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);
			mqlLogRequiredInformationWriter("=======================================================\n\n");
			mqlLogRequiredInformationWriter("                Migrating Objects...\n");
			mqlLogRequiredInformationWriter("                File (" + minRange + ") to (" + maxRange + ")\n");
			mqlLogRequiredInformationWriter("                Reading files from: " + documentDirectory + "\n");
			mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
			mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
			mqlLogRequiredInformationWriter("=======================================================\n\n");
			migrationStartTime = System.currentTimeMillis();
			boolean migrationStatus = true;
			statusBuffer.append("File Name, Status, Object Failed (OR) Time Taken in MilliSec\n");

			// migration method for stamping
			stampDefaultProjectAndOrganization(context);

			for( i = minRange;i <= maxRange; i++)
			{
				try
				{
					ContextUtil.startTransaction(context,true);
					mqlLogWriter("Reading file: " + i + "\n");
					StringList objectList = new StringList();
					migratedOids = new StringBuffer(20000);
					try
					{
						objectList = readFiles(i);
					}
					catch(FileNotFoundException fnfExp)
					{
						// throw exception if file does not exists
						throw fnfExp;
					}
					loadInterOpMigrations(context);

					// for POV & SOV
					migrateObjects(context, objectList);
					if (!scan) {

						ContextUtil.commitTransaction(context);
					} else {
						ContextUtil.abortTransaction(context);
					}

					logMigratedOids();
					mqlLogRequiredInformationWriter("<<< Time taken for migration of objects & write ConvertedOid.txt for file in milliseconds :" + documentDirectory + "objectids_" + i + ".txt"+ ":=" +(System.currentTimeMillis() - startTime) + ">>>\n");

					// write after completion of each file
					mqlLogRequiredInformationWriter("=================================================================\n");
					mqlLogRequiredInformationWriter("Migration of Objects in file objectids_" + i + ".txt COMPLETE \n");
					statusBuffer.append("objectids_");
					statusBuffer.append(i);
					statusBuffer.append(".txt,COMPLETE,");
					statusBuffer.append((System.currentTimeMillis() - startTime));
					statusBuffer.append("\n");
					mqlLogRequiredInformationWriter("=================================================================\n");
				}
				catch(FileNotFoundException fnExp)
				{
					// log the error and proceed with migration for remaining files
					mqlLogRequiredInformationWriter("=================================================================\n");
					mqlLogRequiredInformationWriter("File objectids_" + i + ".txt does not exist \n");
					mqlLogRequiredInformationWriter("=================================================================\n");
					ContextUtil.abortTransaction(context);
					migrationStatus = false;
				}
				catch (Exception exp)
				{
					// abort if identifyModel or migration fail for a specific file
					// continue the migration process for the remaining files
					mqlLogRequiredInformationWriter("=======================================================\n");
					mqlLogRequiredInformationWriter("Migration of Objects in file objectids_" + i + ".txt FAILED \n");
					mqlLogRequiredInformationWriter("=="+ exp.getMessage() +"==\n");
					mqlLogRequiredInformationWriter("=======================================================\n");
					statusBuffer.append("objectids_");
					statusBuffer.append(i);
					statusBuffer.append(".txt,FAILED,");
					statusBuffer.append(failureId);
					statusBuffer.append("\n");
					exp.printStackTrace();
					ContextUtil.abortTransaction(context);
					migrationStatus = false;

				}
			}

			mqlLogRequiredInformationWriter("=======================================================\n");
			if(migrationStatus){
				mqlLogRequiredInformationWriter("                Step 2 of Migration COMPLETED SUCESSFULLY\n");
			}else{
				mqlLogRequiredInformationWriter("                Step 2 of Migration INCOMPLETE\n");
				mqlLogRequiredInformationWriter("Please check the logs for further details on failures\n");
				mqlLogRequiredInformationWriter("There could be failures in migrating objects in one or more files\n");
			}
			mqlLogRequiredInformationWriter("                Time: " + (System.currentTimeMillis() - migrationStartTime) + " ms\n");
			mqlLogRequiredInformationWriter(" \n");
			mqlLogRequiredInformationWriter(" \n");
			mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
			mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
			mqlLogRequiredInformationWriter("=======================================================\n");
		}
		catch (Exception ex)
		{
			mqlLogRequiredInformationWriter("=======================================================\n");
			mqlLogRequiredInformationWriter("Step 2 of Migration     : INCOMPLETE \n");
			mqlLogRequiredInformationWriter("=======================================================\n");
			ex.printStackTrace();
			ContextUtil.abortTransaction(context);
		}
		finally
		{
			mqlLogRequiredInformationWriter("<<< Total time taken for migration in milliseconds :=" + (System.currentTimeMillis() - migrationStartTime) + ">>>\n");
			String cmd = "trigger on";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);

			statusLog   = new FileWriter(documentDirectory + "fileStatus.csv", true);
			statusLog.write(statusBuffer.toString());
			statusLog.flush();
			statusLog.close();

			ContextUtil.popContext(context);
			// if scan is true, writer will be closed by the caller
			if(!scan)
			{
				writer.close();
			}
			errorLog.close();
			warningLog.close();
			convertedOidsLog.close();
		}

		// always return 0, even this gives an impression as success
		// this way, matrixWriter writes to console
		// else writer.write statements do not show up in Application console
		// but it works in mql console
		return 0;
	}


}
