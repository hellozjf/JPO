/*
 * emxEnterpriseChangeMigrationBase.java program to get all document type Object Ids.
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;

public class emxEnterpriseChangeMigrationBase_mxJPO extends emxCommonMigration_mxJPO {

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
	public emxEnterpriseChangeMigrationBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
		writer     = new BufferedWriter(new MatrixWriter(context));
		mqlCommand = MqlUtil.getMQL(context);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 */
	public int mxMain(Context context, String[] args) throws Exception{
		if(!context.isConnected()){
			throw new Exception("not supported on desktop client");
		}
		int argsLength = args.length;
		error = "";
		try{
			// writer     = new BufferedWriter(new MatrixWriter(context));
			if(args.length < 3){
				error = "Wrong number of arguments";
				throw new IllegalArgumentException();
			}
			documentDirectory = args[0];

			// documentDirectory does not ends with "/" add it
			String fileSeparator = java.io.File.separator;
			if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator)){
				documentDirectory = documentDirectory + fileSeparator;
			}

			minRange = Integer.parseInt(args[1]);

			if("n".equalsIgnoreCase(args[2])){
				maxRange = getTotalFilesInDirectory();
			}else{
				maxRange = Integer.parseInt(args[2]);
			}

			if(minRange > maxRange){
				error = "Invalid range for arguments, minimum is greater than maximum range value";
				throw new IllegalArgumentException();
			}

			if(minRange == 0 || minRange < 1 || maxRange == 0 || maxRange < 1){
				error = "Invalid range for arguments, minimum/maximum range value is 0 or negative";
				throw new IllegalArgumentException();
			}
		}catch (IllegalArgumentException iExp){
			writer.write("====================================================================\n");
			writer.write(error + " \n");
			writer.write("Step 2 of Migration :     FAILED \n");
			writer.write("====================================================================\n");
			// if scan is true, writer will be closed by the caller
			if(!scan){
				writer.close();
			}
			return 0;
		}

		String debugString = "false";

		if(argsLength >= 4){
			debugString = args[3];
			if("debug".equalsIgnoreCase(debugString)){
				debug = true;
			}
		}
		try{
			errorLog = new FileWriter(documentDirectory + "unConvertedIds.csv", true);
			errorLog.write("OID,TYPE,NAME,REVISION\n");
			errorLog.flush();
			convertedOidsLog = new FileWriter(documentDirectory + "convertedIds.csv", true);
			convertedOidsLog.write("OID,TYPE,NAME,REVISION\n");
			convertedOidsLog.flush();
			warningLog = new FileWriter(documentDirectory + "migration.log", true);
		}catch(FileNotFoundException fExp){
			// check if user has access to the directory
			// check if directory exists
			writer.write("=================================================================\n");
			writer.write("Directory does not exist or does not have access to the directory\n");
			writer.write("Step 2 of Migration :     FAILED \n");
			writer.write("=================================================================\n");
			// if scan is true, writer will be closed by the caller
			if(!scan){
				writer.close();
			}
			return 0;
		}

		int i = 0;
		try{
			ContextUtil.pushContext(context);
			String cmd = "trigger off";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);
			mqlLogRequiredInformationWriter("=======================================================\n\n");
			mqlLogRequiredInformationWriter("                Migrating Objects...\n");
			mqlLogRequiredInformationWriter("                File (" + minRange + ") to (" + maxRange + ")\n");
			mqlLogRequiredInformationWriter("                Reading files from: " + documentDirectory + "\n");
			mqlLogRequiredInformationWriter("                Objects which have be migrated will be written to:  convertedIds.csv\n");
			mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedIds.csv\n");
			mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
			mqlLogRequiredInformationWriter("=======================================================\n\n");
			migrationStartTime = System.currentTimeMillis();
			statusBuffer.append("File Name, Status, Object Failed (OR) Time Taken in MilliSec\n");
			for(i = minRange;i <= maxRange; i++){
				try{
					ContextUtil.startTransaction(context,true);
					mqlLogWriter("Reading file: " + i + "\n");
					StringList objectList = new StringList();
					migratedOids = new StringBuffer(20000);
					try{
						objectList = readFiles(i);
					}catch(FileNotFoundException fnfExp){
						// throw exception if file does not exists
						throw fnfExp;
					}
					migrateObjects(context, objectList);
					ContextUtil.commitTransaction(context);
					//logMigratedOids();
					mqlLogRequiredInformationWriter("<<< Time taken for migration of objects & write ConvertedOid.txt for file in milliseconds :" + documentDirectory + "objectids_" + i + ".txt"+ ":=" +(System.currentTimeMillis() - startTime) + ">>>\n");

					// write after completion of each file
					mqlLogRequiredInformationWriter("=================================================================\n");
					mqlLogRequiredInformationWriter("Migration of Documents in file objectids_" + i + ".txt COMPLETE \n");
					statusBuffer.append("objectids_");
					statusBuffer.append(i);
					statusBuffer.append(".txt,COMPLETE,");
					statusBuffer.append((System.currentTimeMillis() - startTime));
					statusBuffer.append("\n");
					mqlLogRequiredInformationWriter("=================================================================\n");
				}catch(FileNotFoundException fnExp){
					// log the error and proceed with migration for remaining files
					mqlLogRequiredInformationWriter("=================================================================\n");
					mqlLogRequiredInformationWriter("File objectids_" + i + ".txt does not exist \n");
					mqlLogRequiredInformationWriter("=================================================================\n");
					ContextUtil.abortTransaction(context);
				}catch (Exception exp){
					// abort if identifyModel or migration fail for a specific file
					// continue the migration process for the remaining files
					mqlLogRequiredInformationWriter("=======================================================\n");
					mqlLogRequiredInformationWriter("Migration of Documents in file objectids_" + i + ".txt FAILED \n");
					mqlLogRequiredInformationWriter("=="+ exp.getMessage() +"==\n");
					mqlLogRequiredInformationWriter("=======================================================\n");
					statusBuffer.append("objectids_");
					statusBuffer.append(i);
					statusBuffer.append(".txt,FAILED,");
					statusBuffer.append(failureId);
					statusBuffer.append("\n");
					exp.printStackTrace();
					ContextUtil.abortTransaction(context);
				}
			}

			mqlLogRequiredInformationWriter("=======================================================\n");
			mqlLogRequiredInformationWriter("                Migrating Document Objects  COMPLETE\n");
			mqlLogRequiredInformationWriter("                Time: " + (System.currentTimeMillis() - migrationStartTime) + " ms\n");
			mqlLogRequiredInformationWriter(" \n");
			mqlLogRequiredInformationWriter("Step 2 of Migration :     SUCCESS \n");
			mqlLogRequiredInformationWriter(" \n");
			mqlLogRequiredInformationWriter("                Objects which have be migrated will be written to:  convertedIds.csv\n");
			mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedIds.csv\n");
			mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
			mqlLogRequiredInformationWriter("=======================================================\n");
		}catch (FileNotFoundException fEx){
			ContextUtil.abortTransaction(context);
		}
		catch (Exception ex){
			// abort if identifyModel fail
			mqlLogRequiredInformationWriter("=======================================================\n");
			mqlLogRequiredInformationWriter("Migration of Documents in file objectids_" + i + ".txt failed \n");
			mqlLogRequiredInformationWriter("Step 2 of Migration     : FAILED \n");
			mqlLogRequiredInformationWriter("=======================================================\n");
			ex.printStackTrace();
			ContextUtil.abortTransaction(context);
		}finally{
			mqlLogRequiredInformationWriter("<<< Total time taken for migration in milliseconds :=" + (System.currentTimeMillis() - migrationStartTime) + ">>>\n");
			String cmd = "trigger on";
			MqlUtil.mqlCommand(context, mqlCommand,  cmd);

			statusLog   = new FileWriter(documentDirectory + "fileStatus.csv", true);
			statusLog.write(statusBuffer.toString());
			statusLog.flush();
			statusLog.close();

			ContextUtil.popContext(context);
			// if scan is true, writer will be closed by the caller
			if(!scan){
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

	/**
	 * @param context
	 * @param objectList
	 * @throws Exception
	 */

	public void migrateObjects(Context context, StringList objectList) throws Exception {
		try{
			if(objectList!=null && !objectList.isEmpty()){
				for(int i=0; i<objectList.size(); i++) {
					String objectId = (String)objectList.get(i);
					if(objectId!=null && !objectId.isEmpty()){
						Boolean isBusinessObject = new DomainObject(objectId).exists(context);

						if(isBusinessObject){
							//This is a Business Object
							migrateBusinessObject(context, objectId);
						}else{
							//This is a Relationship
							migrateRelationship(context, objectId);
						}
					}
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}


	public void migrateBusinessObject(Context context, String busId) throws Exception {
		try{
			if(busId!=null && !busId.isEmpty()){
				DomainObject domObj = new DomainObject(busId);

				BusinessInterface interfaceChangeDiscipline = new BusinessInterface(EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE, context.getVault());

				//Check if an the change discipline interface has been already connected
				if((domObj.getInfo(context, "interface[" + interfaceChangeDiscipline + "]")).equalsIgnoreCase("FALSE")){
					//If no interface --> add one
					domObj.addBusinessInterface(context, interfaceChangeDiscipline);
					logMigratedObjectIds(context, busId);
				}else{
					//Already has an interface
					logUnMigratedObjectIds(context, busId);
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public void migrateRelationship(Context context, String relId) throws Exception {
		try{
			if(relId!=null && !relId.isEmpty()){
				BusinessInterface interfaceChangeDiscipline = new BusinessInterface(EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE, context.getVault());
				AttributeTypeList listInterfaceAttributes = interfaceChangeDiscipline.getAttributeTypes(context);

				MapList changeDisciplinesTypes = new MapList();

				Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
				while(listInterfaceAttributesItr.hasNext()){
					Map changeDisciplineTypes = new HashMap();
					String changeDisciplineName = ((AttributeType)listInterfaceAttributesItr.next()).getName();
					String changeDisciplineNameSmall = changeDisciplineName.replaceAll(" ", "");

					changeDisciplineTypes.put("discipline",changeDisciplineName);
					changeDisciplineTypes.put("type",com.matrixone.apps.domain.util.FrameworkProperties.getProperty(context, "emxEnterpriseChange." + changeDisciplineNameSmall + ".ApplicableItemTypes"));

					changeDisciplinesTypes.add(changeDisciplineTypes);
				}

				StringList selectRelStmts = new StringList(1);
				selectRelStmts.addElement(DomainConstants.SELECT_TO_ID);
				selectRelStmts.addElement("interface[" + interfaceChangeDiscipline + "]");
				String relIds[] = new String[1];
				relIds[0] = relId;

				DomainRelationship domRel = new DomainRelationship(relId);
				MapList mapList = domRel.getInfo(context, relIds, selectRelStmts);

				if(mapList!=null && !mapList.isEmpty()){
					Map map = (Map)mapList.get(0);
					if(map!=null && !map.isEmpty()){
						//Check if an the change discipline interface has been already connected
						if(((String)map.get("interface[" + interfaceChangeDiscipline + "]")).equalsIgnoreCase("FALSE")){
							//If no interface --> add one
							domRel.addBusinessInterface(context, interfaceChangeDiscipline);
							logMigratedRelIds(context, relId);
						}else{
							logUnMigratedRelIds(context, relId);
						}
						//Get the To object Id of the relationship
						String toId = (String)map.get(DomainConstants.SELECT_TO_ID);
						if(toId!=null && !toId.isEmpty()){
							DomainObject toBusDom = new DomainObject(toId);

							Iterator changeDisciplinesTypesItr = changeDisciplinesTypes.iterator();
							while(changeDisciplinesTypesItr.hasNext()){
								Boolean flag = false;
								Map changeDisciplineTypes = (Map)changeDisciplinesTypesItr.next();
								if(changeDisciplineTypes!=null && !changeDisciplineTypes.isEmpty()){
									String type = (String)changeDisciplineTypes.get("type");
									String discipline = (String)changeDisciplineTypes.get("discipline");
									if(type!=null && !type.isEmpty()){
										StringList listAvailableTypes = FrameworkUtil.split(type, ",");
										if(listAvailableTypes!=null && !listAvailableTypes.isEmpty()){
											Iterator listAvailableTypesItr = listAvailableTypes.iterator();
											while(listAvailableTypesItr.hasNext()){
												String availableType = (String) listAvailableTypesItr.next();
												if(availableType!=null && !availableType.equalsIgnoreCase("")){
													//Check if type exists
													if(PropertyUtil.getSchemaProperty(context,availableType)!=null && !PropertyUtil.getSchemaProperty(context,availableType).equalsIgnoreCase("")){
														if(toBusDom.isKindOf(context, PropertyUtil.getSchemaProperty(context,availableType))){
															flag = true;
														}
													}
												}
											}
										}
									}
									if(flag){
										domRel.setAttributeValue(context, discipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE);
									}else{
										domRel.setAttributeValue(context, discipline, EnterpriseChangeConstants.CHANGE_DISCIPLINE_FALSE);
									}
								}
							}
						}
					}
				}
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

    private void logMigratedObjectIds(Context context, String objectId) throws Exception{
        DomainObject domObj = new DomainObject(objectId);
    	convertedOidsLog.write(objectId+",");
    	convertedOidsLog.write(domObj.getInfo(context, DomainConstants.SELECT_TYPE)+",");
    	convertedOidsLog.write(domObj.getInfo(context, DomainConstants.SELECT_NAME)+",");
    	convertedOidsLog.write(domObj.getInfo(context, DomainConstants.SELECT_REVISION)+",");
        convertedOidsLog.write(" \n");
        convertedOidsLog.flush();
    }

    private void logUnMigratedObjectIds(Context context, String objectId) throws Exception{
        DomainObject domObj = new DomainObject(objectId);
        errorLog.write(objectId+",");
        errorLog.write(domObj.getInfo(context, DomainConstants.SELECT_TYPE)+",");
        errorLog.write(domObj.getInfo(context, DomainConstants.SELECT_NAME)+",");
        errorLog.write(domObj.getInfo(context, DomainConstants.SELECT_REVISION)+",");
        errorLog.write(" \n");
        errorLog.flush();
    }

    private void logMigratedRelIds(Context context, String objectId) throws Exception{
        DomainObject domObj = new DomainObject(objectId);
    	convertedOidsLog.write(objectId+",");
    	convertedOidsLog.write(domObj.getInfo(context, DomainConstants.SELECT_TYPE)+",");
    	convertedOidsLog.write(domObj.getInfo(context, DomainConstants.SELECT_NAME)+",");
    	convertedOidsLog.write(domObj.getInfo(context, DomainConstants.SELECT_REVISION)+",");
        convertedOidsLog.write(" \n");
        convertedOidsLog.flush();
    }

    private void logUnMigratedRelIds(Context context, String relId) throws Exception{
        DomainObject domObj = new DomainObject(relId);
        errorLog.write(relId);
        errorLog.write(" \n");
        errorLog.flush();
    }

	public void help(Context context, String[] args) throws Exception {
		try{
			if (!context.isConnected()) {
				throw new Exception("not supported on desktop client");
			}

			writer.write("================================================================================================\n");
			writer.write(" Migration is a two step process  \n");
			writer.write(" Step1: Find all objects and write them into flat files \n");
			writer.write(" Example 1: \n");
			writer.write(" FindObjects for enterpriseChange Change Project and Change Task Migration: \n");
			writer.write(" execute program emxEnterpriseChangeFindObjects -method findObjects 1000  C:/Temp/oids; \n");
			writer.write(" First parameter  = 1000 indicates no of oids per file \n");
			writer.write(" Second parameter  = C:/Temp/oids is the directory where files should be written  \n");
			writer.write(" \n");
			writer.write(" Step2: Migrate the objects \n");
			writer.write(" Example: \n");
			writer.write(" execute program emxEnterpriseChangeMigration 'C:/Temp/oids' 1 n ; \n");
			writer.write(" First parameter  = C:/Temp/oids directory to read the files from\n");
			writer.write(" Second Parameter = 1 minimum range  \n");
			writer.write(" Third Parameter  = n maximum range  \n");
			writer.write("        - value of 'n' means all the files starting from mimimum range\n");

			writer.write("================================================================================================\n");
			writer.write(" \n");
			writer.write(" \n");

			writer.write("================================================================================================\n");
			writer.write(" Sequence of Migration for Migrating \"Change Project, Change Task and Applicable Item\" when upgrading to V6R2012\n");
			writer.write("================================================================================================\n");
			writer.write(" \n");

			writer.write(" 1. Migrate Change Project and Change Task  \n");
			writer.write(" run the following Commands from MQL for Migration \n \n");
			writer.write(" execute program emxEnterpriseChangeFindObjects -method findObjects 1000  C:/Temp/objectIds; \n");
			writer.write(" execute program emxEnterpriseChangeMigration C:/Temp/objectIds 1 n ; \n");
			writer.write(" \n");

			writer.write(" 2. Migrate Applicable Item \n");
			writer.write(" run the following Commands from MQL for Migration \n \n");
			writer.write(" execute program emxEnterpriseChangeFindObjects -method findRelationships 1000  C:/Temp/relationshipIds; \n");
			writer.write(" execute program emxEnterpriseChangeMigration C:/Temp/relationshipIds 1 n ; \n");
			writer.write(" \n");

			writer.close();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
}

