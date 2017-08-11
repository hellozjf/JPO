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
import java.util.Iterator;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;

public class emxProgramFindObjectsBase_mxJPO extends emxCommonFindObjectsBase_mxJPO
{


	private static final String SELECT_POLICY = "policy";
	public static final StringList programPolicyList = new StringList();
	static{
		programPolicyList.add("Actual Transaction");
		programPolicyList.add("Assessment");
		programPolicyList.add("Baseline Log");
		programPolicyList.add("Business Skill");
		programPolicyList.add("Calendar");
		programPolicyList.add("Checklist");
		programPolicyList.add("Checklist Item");
		programPolicyList.add("Controlled Folder");
		/*programPolicyList.add("Deliverable Intent");
		programPolicyList.add("Deliverable Template");*/
		programPolicyList.add("Effort");
		programPolicyList.add("Financial Categories");
		programPolicyList.add("Financial Items");
		programPolicyList.add("Program");
		programPolicyList.add("Project Access List");
		programPolicyList.add("Project Concept");
		programPolicyList.add("Project Review");
		programPolicyList.add("Project Risk");
		programPolicyList.add("Project Space");
		programPolicyList.add("Project Space Hold Cancel");
		programPolicyList.add("Project Task");
		programPolicyList.add("Project Template");
		programPolicyList.add("Quality");
		programPolicyList.add("Quality Metric");
		programPolicyList.add("Question");
		programPolicyList.add("Resource Request");
		programPolicyList.add("RPN");
		programPolicyList.add("Template Workspace Vault");
		programPolicyList.add("URL");
		programPolicyList.add("Weekly Timesheet");
		programPolicyList.add("Interval Item Data");
		programPolicyList.add("Business Goal");
	}

	public emxProgramFindObjectsBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}


	/**
	 * This method is executed if a specific method is not specified.
	 *
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
		Iterator itr = programPolicyList.iterator();
		
		StringBuffer sbTypes = new StringBuffer(100);
		while(itr.hasNext()){
			String policyName = (String)itr.next();
			//String mqlCommand = "list policy \""+policyName+"\" select type dump ,";
			String mqlCommand = "list $1 $2 select $3 dump $4";
			String result = MqlUtil.mqlCommand(context,mqlCommand,"policy",policyName,"type",",");
			if(result != null)
				sbTypes.append(result.trim());
			
			if(itr.hasNext()){
				sbTypes.append(",");
			}
			writer.write("\n policyName = "+policyName);
			writer.write("\n subtypes = "+result);
		}
		
		return sbTypes.toString();
	}

	/**
	 * Evalutes a temp query to get all the DOCUEMENTS objects in the system
	 * @param context the eMatrix <code>Context</code> object
	 * @param chunksize has the no. of objects to be stored in file.
	 * @return void
	 * @exception Exception if the operation fails.
	 */
	@Override
	public void getIds(Context context, int chunkSize) throws Exception
	{
		// Written using temp query to optimize performance in anticipation of
		// large 1m+ documents in system.
		// Utilize query limit to use different algorithim in memory allocation

		// get the PRG objects

		String vaultList = "";

		/* try
        {
            EnoviaResourceBundle.getProperty(context,"emxComponents.CommonDocumentMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }*/

		if(vaultList == null || "null".equals(vaultList) || "".equals(vaultList))
		{
			vaultList = "*";
		}

		String sTypeExpression = getTypeExpression(context);
		//String command = "temp query bus \""+sTypeExpression+"\" * *  vault '" + vaultList + "' limit 1 where 'program[emxProgramMigration -method writeOID ${OBJECTID} \"${TYPE}\"] == true'";
		String command = "temp query bus $1 $2 $3 vault $4 limit $5 where $6";
		String result ="";

		//reset/set static variabless back to original values every time this JPO is run
		emxProgramMigration_mxJPO._counter  = 0;
		emxProgramMigration_mxJPO._sequence  = 1;
		emxProgramMigration_mxJPO._oidsFile = null;
		emxProgramMigration_mxJPO._fileWriter = null;
		emxProgramMigration_mxJPO._objectidList = null;

		//set statics
		//create BW and file first time
		if (emxProgramMigration_mxJPO._fileWriter == null)
		{
			try
			{
				emxProgramMigration_mxJPO.documentDirectory = documentDirectory;
				emxProgramMigration_mxJPO._oidsFile = new java.io.File(documentDirectory + "objectids_1.txt");
				emxProgramMigration_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxProgramMigration_mxJPO._oidsFile));
				emxProgramMigration_mxJPO._chunk = chunkSize;
				emxProgramMigration_mxJPO._objectidList = new StringList(chunkSize);
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
			result  = MqlUtil.mqlCommand(context,command,sTypeExpression,"*","*",vaultList,"1","'program[emxProgramMigration -method writeOID ${OBJECTID} \"${TYPE}\"] == true'");
		}
		catch(Exception me)
		{
			throw me;
		}

		// call cleanup to write the left over oids to a file
		emxProgramMigration_mxJPO.cleanup();
	}
}
