/*
 * ProductConfigurationFilterBinaryMigrationBase.java program to migrate Filter Binary data in Product Configuration
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;

public class ProductConfigurationFilterBinaryMigrationBase_mxJPO extends emxCommonMigrationBase_mxJPO{

	public ProductConfigurationFilterBinaryMigrationBase_mxJPO(Context context,
			String[] args) throws Exception {
		super(context, args);
	}
	/**
     * This method is executed if a specific method is not specified.
     * This method checked for the status property and go ahead with MP migration if find object step is completed.
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args) throws Exception
    {  
  	  boolean bIsException=false;
  	  try{
		  String strMqlCommand = "trigger off";
		  MqlUtil.mqlCommand(context,strMqlCommand,true);
  			  int migrationStatus = getAdminMigrationStatus(context);
  			 /*mqlLogRequiredInformationWriter("Product Configuration Filter Binary Migration Status is: "+ migrationStatus+" \n");

  	  		   This code needs to be uncommented after phase 2 implementation is done and Pre and Post checks are implemented 
  	            if(migrationStatus<2)
  	            {
  	          	  mqlLogRequiredInformationWriter("Product Configuration Filter Binary Migration PreChecks not executed.Please complete PreChecks before migration. \n");
  	          	  bIsException=true;
  	          	  return -1;
  	            }*/
  			  super.mxMain(context, args);
    		  strMqlCommand = "trigger on";
    		  MqlUtil.mqlCommand(context,strMqlCommand,true);
  	
  	  
  	  }
  	  catch(Exception e){
  		  bIsException=true;
      	  mqlLogRequiredInformationWriter("\n");
      	  mqlLogRequiredInformationWriter(" Product Configuration Filter Binary Migration Failed :: "+e.getMessage());
      	  mqlLogRequiredInformationWriter("\n");
            e.printStackTrace();
            throw e;
  	  }
  	  finally{
  		  if(!bIsException){
  		  setAdminMigrationStatus(context,"MigrationCompleted");
  		  }
  	  }
  	  
  	  return 0;
    }
	 /**
     * This method is used to find and write Product Configuration Ids into text file
     * @param context the eMatrix <code>Context</code> object
     * @param args accepts a String array holding the arguments context and [Chunk size,Path to save text file]
     * @return void
     * @throws Exception when the operation fails. 
     */
	
	public void getPCIds(Context context, String[] args) throws Exception
    {
		String[] newArgs 	= new String[2];
		String[] args1 		= new String[2];
        newArgs[0] 			= args[0];
    	String str 			= newArgs[0].toString();
    	
    	String topLevelObjectId 	= null;
	    String topLevelObjectType	= null;
    	StringList objSelects 		= new StringList(3);
    	StringBuffer whereExpnSB 	= new StringBuffer();
        int chunkSize 				= Integer.parseInt(str);
        String fileSeparator 		= java.io.File.separator;
        
        emxCommonMigration_mxJPO._counter  	   = 0;
	    emxCommonMigration_mxJPO._sequence 	   = 1;
	    emxCommonMigration_mxJPO._oidsFile 	   = null;
	    emxCommonMigration_mxJPO._fileWriter   = null;
	    emxCommonMigration_mxJPO._objectidList = null;
	    
		 
        
        documentDirectory = args[1]; // Output Path
        
        if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
        {
            documentDirectory = documentDirectory + fileSeparator;
        }
		
		objSelects.addElement(DomainConstants.SELECT_ID);
		objSelects.addElement(DomainConstants.SELECT_TYPE);
		
		Pattern sType = new Pattern(ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION);
		
			 try{
				 MapList mlPCObj = DomainObject.findObjects(context, // context
						 	sType.getPattern(), 				     // typePattern
		  					"*", 									 // vaultPattern
		  					null, 				 // whereExpression
		  					objSelects); 		 					 // objectSelects
				 
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
			            catch(FileNotFoundException ex)
			            {
			            	ex.printStackTrace();
			                throw ex;
			            }
			        }
			     
				     for (int i = 0; i < mlPCObj.size(); i++) {
							Map tmpMap = (Map) mlPCObj.get(i);
							topLevelObjectId = (String) tmpMap.get(ConfigurationConstants.SELECT_ID);
							topLevelObjectType = (String) tmpMap.get(ConfigurationConstants.SELECT_TYPE);
							
							args1[0] = topLevelObjectId;  	//PC Id
							args1[1] = topLevelObjectType;  
							JPO.invoke(context, "emxCommonMigrationBase", null,"writeOID", args1, null);
				     	}
			     			// call cleanup to write the left over oids to a file
			     			emxCommonMigration_mxJPO.cleanup();
			     			
		 		}catch(FileNotFoundException ex)
	            {
		 			ex.printStackTrace();
	                throw ex;
	            }
	        }
	
	/**
     * This method overrides method ${CLASS:emxCommonMigrationBase} and is used to read Product Configuration Ids from text file and update them
     * @param context the eMatrix <code>Context</code> object
     * @param args accepts a String array holding the arguments context and PC Ids list
     * @return void
     * @throws Exception when the operation fails. 
     */
	public void migrateObjects(Context context, StringList objectList) throws Exception
    {
		String[] argsList = new String[objectList.size()];
		int iObjListSize = objectList.size();
		warningLog = new FileWriter(documentDirectory + "ProductConfigurationFilterBinaryMigration.log", true);
		PropertyUtil.setGlobalRPEValue(context,"UpdatePCFilterBinary", "TRUE");
		//Total count of PC ids
		mqlLogRequiredInformationWriter("Migration will be done for :" +objectList.size()+" Ids \n\n");
        for(int count=0;count < iObjListSize;count++){
	        	argsList[0] = "id="+(String)objectList.get(count)+"type=triggerName=";
	            emxProductConfigurationBase_mxJPO classProductConfigurationBase = new emxProductConfigurationBase_mxJPO(context,argsList);
	            String strCurrentObjId=(String)objectList.get(count);
	            mqlLogRequiredInformationWriter("PC Migration for Id : "+ strCurrentObjId +" in Process \n\n");
	            try{
	            classProductConfigurationBase.updateSelectedOptionsFilter(context, argsList);
	            //Since the BOM XML  format is changed in V6R2014x, we will be setting this attribute to empty value. This attribute would be auto populated during BOM generation (through UI) again
	           // MqlUtil.mqlCommand(context, "mod bus $1 $2 $3",strCurrentObjId,ConfigurationConstants.ATTRIBUTE_BOMXML,"");
	           // MqlUtil.mqlCommand(context, "mod bus $1 $2 $3",strCurrentObjId,ConfigurationConstants.ATTRIBUTE_COMPLETENESS_STATUS,"Complete");
	           // MqlUtil.mqlCommand(context, "mod bus $1 $2 $3",strCurrentObjId,ConfigurationConstants.ATTRIBUTE_EVALUATE_RULES,"True");
	            DomainObject prodConfigObj = new DomainObject();
	            prodConfigObj.setId(strCurrentObjId);
	          //  prodConfigObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_BOMXML, "");
	            prodConfigObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_COMPLETENESS_STATUS, "Complete");
	            prodConfigObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_EVALUATE_RULES, "True");
	            
	            String ValStatus = prodConfigObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_VALIDATION_STATUS);
	            if(ValStatus.equals("Validation Failed")){
	            	prodConfigObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_RULE_COMPLIANCY_STATUS, "Invalid");
	            }else if(ValStatus.equals("Validation Passed")){
	            	prodConfigObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_RULE_COMPLIANCY_STATUS, "Valid");
	            }
	            
	            String BOMXML = prodConfigObj.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_BOMXML);
		          
	            mqlLogRequiredInformationWriter("PC BOM XML modification for Id : "+ strCurrentObjId +" started... \n\n");
	            
	            SAXBuilder saxb = new SAXBuilder();
				Document document = saxb.build(new StringReader(BOMXML));

				 int notPresent = 0;
				Element rootelem =  document.getRootElement();
				List parentNodesFeature = rootelem.getChildren("Feature");
				int calFeatureLen = parentNodesFeature.size();
				int sizeFeature = calFeatureLen;
				for(int i=0;i<sizeFeature;i++){
					
					Element NodeFeature = (Element)parentNodesFeature.get(i);
		             NodeFeature.setAttribute("isLeaf", "No");
					
					Element elemPending = NodeFeature.getChild("Pending");
					Element elemResolved = NodeFeature.getChild("Resolved");
					
					
					if(elemPending != null || elemResolved != null){
					NodeFeature.removeChild("Usage");
					NodeFeature.removeChild("Quantity");
					NodeFeature.removeChild("Duplicate");
					NodeFeature.removeChild("PartFamily");
					}
					// On detaching the current feature tag from document, the document & it's size get modified 
					if(elemPending == null && elemResolved == null){
						notPresent++;
						NodeFeature.detach();
						i--;
						sizeFeature--;
					}
					
				}

				XMLOutputter xmOut=new XMLOutputter(); 
			
				BOMXML = xmOut.outputString(document);
				// If resolved or pending id not present in any Feature tags, make BOM XML as blank as corresponding BOM XML for the case will be blank in 2014x. 		
		        if(notPresent == calFeatureLen){
					
					BOMXML = "";
				}
		        
		        prodConfigObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_BOMXML, BOMXML);
		        
		        mqlLogRequiredInformationWriter("PC BOM XML modification for Id : "+ strCurrentObjId +" done. \n\n");
	            	            	            	                      	            	            
	            }catch(Exception exception){
	            	
	            	String sMqlCommand = "print bus $1 select $2 $3 $4 dump $5";	           	
	            	writeUnconvertedOID(MqlUtil.mqlCommand(context, sMqlCommand, strCurrentObjId, "type", "name", "revision", ","),strCurrentObjId);
	            	//writeUnconvertedOID(MqlUtil.mqlCommand(context,"print bus "+strCurrentObjId+" select type name revision dump ," ),strCurrentObjId);
	            //	throw exception;
	            }
	            mqlLogRequiredInformationWriter("PC Migration for Id : "+ strCurrentObjId+" Completed \n\n");
	            writeConvertedOID((String)objectList.get(count));
       	   }
    	}
    /**
	 * Sets the migration status as a property setting.
	 * Status could be :-
	 * MigrationPreCheckCompleted
	 * MigrationCompleted
	 * MigrationPostCheckCompleted
	 * 
     * @param context the eMatrix <code>Context</code> object
 	 * @param strStatus String containing the status setting
	 * @throws Exception
	 */
	public void setAdminMigrationStatus(Context context,String strStatus) throws Exception
	{
		String cmd = "modify program eServiceSystemInformation.tcl property ProductConfigurationFilterBinaryMigration value "+strStatus;
		MqlUtil.mqlCommand(context, mqlCommand,  cmd);
	}

	/**
	 * Gets the migration status as an integer value.  Used to enforce an order of migration.
     * @param context the eMatrix <code>Context</code> object
 	 * @return integer representing the status
	 * @throws Exception
	 */
	public int getAdminMigrationStatus(Context context) throws Exception
	{
		String cmd = "print program eServiceSystemInformation.tcl select property[ProductConfigurationFilterBinaryMigration].value dump";
	    String result =	MqlUtil.mqlCommand(context, mqlCommand, cmd);
	   
	    if(result.equalsIgnoreCase("MigrationPreCheckCompleted"))
		{
			return 1;
		}else if(result.equalsIgnoreCase("MigrationCompleted"))
		{
			return 2;
		}else if(result.equalsIgnoreCase("MigrationPostCheckCompleted"))
		{
			return 3;
		}
	 
	    return 0;
	}
	
	
    
    /**
     * Writes the command string to the ConvertedObjectIds_xx.csv log file 
     * and writes the objectId to the ConvertedObjectIds_xx.txt file.
     *
     * @param command specifies the value to write to the CSV file  
     * @param objectId object Id for the object that cannot be converted  
     * @return nothing 
     * @throws Exception if the operation fails
     */
    public void writeConvertedOID(String objectId) throws Exception
    {

    	String newLine = System.getProperty("line.separator");
    	convertedOidsLog.write(objectId+newLine);
    	convertedOidsLog.flush();

    }
    /**
     * Outputs the help for this migration.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args String[] containing the command line arguments
     * @throws Exception if the operation fails
     */
     public void help(Context context, String[] args) throws Exception
    {
         if(!context.isConnected())
         {
             throw new FrameworkException("not supported on desktop client");
         }

         writer.write("================================================================================================\n");
         writer.write(" Variant Configuration Product Configuration Filter Binary Migration steps::-  \n");
         writer.write("\n");
         writer.write(" Step1: Create the directory hierarchy like:- \n");
         writer.write(" C:/Migration \n");
         writer.write(" C:/Migration/PCFilterBinary \n");           
         writer.write("\n");
         writer.write(" Step2: Perform Product Configuration Filter Binary Migration \n");    
         writer.write(" \n");
         writer.write(" Step2.1: Find all objects that need filter binary generated and save the ids to a list of files \n");
         writer.write(" Example: \n");
         writer.write(" execute program ProductConfigurationFilterBinaryMigration -method getPCIds 1000 C:/Migration/PCFilterBinary ; \n");
         writer.write(" First parameter  = indicates number of object per file \n");
         writer.write(" Second Parameter  = the directory where files should be written \n");
         writer.write(" \n");
         writer.write(" Step2.2: Generate Filter Binary \n");
         writer.write(" Example: \n");
         writer.write(" execute program ProductConfigurationFilterBinaryMigration  C:/Migration/PCFilterBinary 1 n; \n");
         writer.write(" First parameter  = the directory to read the files from\n");
         writer.write(" Second Parameter = minimum range of file to start migrating  \n");
         writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
         writer.write("        - value of 'n' means all the files starting from mimimum range\n");           
         writer.write("================================================================================================\n");
         writer.close();
     }
	}
