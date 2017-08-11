/*
 * VariantConfigurationDerivationMigrationBase.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.effectivity.EffectivityFramework;

import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;

/**
 * The <code>VariantConfigurationMPDerivationMigrationBase</code> class contains code to migrate Manufacturing Plan structure
 * of the Models found in step1. This needs to be done for supporting Branching in 2012x
 */
  public class VariantConfigurationGBOMPhaseOneMigrationBase_mxJPO extends emxCommonMigration_mxJPO
  {   
	  private VariantConfigurationGBOMPhaseOneFindObjects_mxJPO statusJPO;
	  
	  public static String GLOBAL_MODEL_ID = null;
	  public static String GLOBAL_MODEL_PHYSICAL_ID = null;
	 
	  /**
      * Default constructor.
      * @param context the eMatrix <code>Context</code> object
      * @param args String array containing program arguments
      * @throws Exception if the operation fails
      */
      public VariantConfigurationGBOMPhaseOneMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
          super(context, args);          
          statusJPO = new VariantConfigurationGBOMPhaseOneFindObjects_mxJPO(context, new String[0]);
          warningLog = new FileWriter(documentDirectory + "migration.log", true);
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
    	  try{ 	
    		  int migrationStatus = statusJPO.getAdminMigrationStatus(context);
    		  
    		  mqlLogRequiredInformationWriter("Migration Status is: "+ migrationStatus+" \n");
    	/*	  
              if(migrationStatus<2)
              {
            	  mqlLogRequiredInformationWriter("Pre-migration Find Object is not complete. Please complete Pre-migration before running Object Migration. \n");
            	  return -1;
              }
              else if(migrationStatus == 2){
            	  */
            	      statusJPO.setAdminMigrationStatus(context,"GBOMMigrationInProgress");  
            		  mqlLogRequiredInformationWriter("GBOM Migration In Progress. \n \n");
            		  
            		  String cmd = "print bus $1 $2 $3 select $4 $5 dump $6";
            		  String strGlobalModelInfo =	MqlUtil.mqlCommand(context, cmd, true, "Model", "Global", "", "id", "physicalid", "|");
            		  
            		  StringTokenizer stGlobalModelInfo = new StringTokenizer(strGlobalModelInfo, "|");
                      
            		  GLOBAL_MODEL_ID = stGlobalModelInfo.nextToken();                      
                      GLOBAL_MODEL_PHYSICAL_ID = stGlobalModelInfo.nextToken();
                      
            		  if(ProductLineCommon.isNotNull(GLOBAL_MODEL_ID)){
            			  mqlLogRequiredInformationWriter("Found Global Model ID::"+ GLOBAL_MODEL_ID +" \n \n");
            		  }
            		  else{
            			  mqlLogRequiredInformationWriter("Global Model ID Not Found, Migration Will Not Continue. \n \n");
            			  return -1;
            		  }
            		  mqlLogRequiredInformationWriter("GBOM Migration In Progress. \n \n");
            		  
                	  super.mxMain(context, args);
					  //As log files are closing in end of emxCommonMigrationBase:mxMain method, we need to reopen them for final logging statement 
            		  writer     = new BufferedWriter(new MatrixWriter(context));
                	  warningLog = new FileWriter(documentDirectory + "migration.log", true);
                	  mqlLogRequiredInformationWriter("GBOM Migration Completed. \n \n");            	           	  
       /*       }
              else if(migrationStatus == 3){
            	  mqlLogRequiredInformationWriter("GBOM migration is already in process. Please wait for migration to be completed or abort the transaction and reset the status Property. \n");
            	  return -1;
              }
              else if(migrationStatus == 4 ){
            	  mqlLogRequiredInformationWriter("GBOM migration is already completed. Please do the find object again or reset the status Property. \n");
            	  return -1;
              } */
    	  }
    	  catch(Exception e){    		  
        	  mqlLogRequiredInformationWriter("\n");
        	  mqlLogRequiredInformationWriter("GBOM Migration Failed :: "+e.getMessage());
        	  mqlLogRequiredInformationWriter("\n");
              e.printStackTrace();
              throw e;
    	  }
    	  finally{
    		  statusJPO.setAdminMigrationStatus(context,"GBOMMigrationCompleted");
    	  }
    	  
    	  return 0;
      }
      /**
       * Main migration method to handle migrating Manufacturing Plan structure under found Models.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList)throws Exception
      {  
    	     	  
          try
          {
        	  String logString = ""; 
              String strObjectId = "";
              String gbomRelId = "";
              String partName = "";
              Map infoMap = new Hashtable();
              MapList infoMapList;
              Iterator itr;
              short iLevel = 0;
          	  int limit = 0;
          	  String strObjWhere = DomainConstants.EMPTY_STRING;
          	  String strRelWhere = DomainConstants.EMPTY_STRING;
          	                
              StringList slObjSelects = new StringList(ConfigurationConstants.SELECT_ID);
              slObjSelects.add(ConfigurationConstants.SELECT_NAME);
              slObjSelects.add(ConfigurationConstants.SELECT_TYPE);
              slObjSelects.add(ConfigurationConstants.SELECT_REVISION);
                          
              StringList slRelSelects = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
              slRelSelects.add(ConfigurationConstants.SELECT_RELATIONSHIP_TYPE);              
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.id");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.type");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.name");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.revision");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY +"].value");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION +"].value");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION +"].value");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.id");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.physicalid");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.type");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].to.type");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].to.name");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].to.revision");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].to.id");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.to.id");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.to.physicalid");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.to.type");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.to.name");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.to.revision");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.from.id");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.from.physicalid");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.from.type");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.from.name");
              slRelSelects.add("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.from.revision");
              
              boolean getTo = false;
              boolean getFrom = true;
              
              StringBuffer strTypePattern = new StringBuffer(ConfigurationConstants.TYPE_PART);
              strTypePattern.append(",");
              strTypePattern.append(ConfigurationConstants.TYPE_PART_FAMILY);
              
              StringBuffer strRelPattern = new StringBuffer(ConfigurationConstants.RELATIONSHIP_GBOM);
              strRelPattern.append(",");
              strRelPattern.append(ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM);
              strRelPattern.append(",");
              strRelPattern.append(ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM);
              strRelPattern.append(",");
              strRelPattern.append(ConfigurationConstants.RELATIONSHIP_INACTIVE_CUSTOM_GBOM);
                  
              DomainObject domContextBus = new DomainObject();
              
              Iterator iterator = objectIdList.iterator();
              
              //For each Object found
              while (iterator.hasNext())
              {
            	  try{            		  
            		  infoMap.clear();
            		  
            		  mqlLogRequiredInformationWriter("\n");
            		  
                      strObjectId = (String)iterator.next();
                      
                      mqlLogRequiredInformationWriter("GBOM Migration Starts For Object Id= " + strObjectId +" \n");
                                            
                      domContextBus.setId(strObjectId);
                      
                      logString = ","+strObjectId+",,";
                      
                      infoMapList = (MapList)domContextBus.getRelatedObjects(context, strRelPattern.toString(), strTypePattern.toString(), slObjSelects, slRelSelects, getTo,
                      		getFrom, iLevel, strObjWhere, strRelWhere, limit);
                      
                      mqlLogRequiredInformationWriter("Info Map For Object Id= " + strObjectId +" Is: "+ infoMapList +"\n");
                      
                      if(infoMapList.size() != 0){
                    	  itr = infoMapList.iterator();
                          
                          while(itr.hasNext()){
                          	infoMap = (Hashtable)itr.next();
                          	gbomRelId = (String)infoMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
                          	partName = (String)infoMap.get(ConfigurationConstants.SELECT_TYPE) + 
                          	" " +
                          	(String)infoMap.get(ConfigurationConstants.SELECT_NAME) + 
                          	" " +
                          	(String)infoMap.get(ConfigurationConstants.SELECT_REVISION);
                          	
                          	mqlLogRequiredInformationWriter("Processing GBOM ID: "+ gbomRelId +" Connected With "+ partName +"\n");
                          	
                          	if(infoMap.containsKey("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.id")){
                          		mqlLogRequiredInformationWriter("Found Inclusion Rule Id: "+ (String)infoMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.id") +" Name: "+ (String)infoMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.name") +"\n");
                          		processGBOMForEffectivity(context, strObjectId, infoMap);
                          	}
                          	else{
                          		 mqlLogRequiredInformationWriter("Inclusion Rule Not Found On This GBOM \n");
                          	}
                          }
                      }
                      else{
                    	  mqlLogRequiredInformationWriter("Object Does Not Have Any GBOM Connected To It \n");
                      }
                      
                      loadMigratedOids(strObjectId);                      
                      mqlLogRequiredInformationWriter("GBOM Migration Completed For Object id= " + strObjectId);
                      mqlLogRequiredInformationWriter("\n");
                     
            	  }
            	  catch(Exception ex){            		  
                	  mqlLogRequiredInformationWriter(ex.getMessage());
                	  mqlLogRequiredInformationWriter("\n \n");
                	  ex.printStackTrace();
                	  writeUnconvertedOID(logString+ex.getMessage()+"\n", strObjectId);  
                	  mqlLogRequiredInformationWriter("\n");
                	  mqlLogRequiredInformationWriter("########################## GBOM Migration Failed For Object id= " + strObjectId + " ##########################");
                	  mqlLogRequiredInformationWriter("\n");
            	  }               
              }
          }
          catch(Exception e)
          {        	  
              throw e;
          }              
      }


      /**
       * Method to be called from migrateObjects method to migrate Manufacturing Plan Structure below the Model 
       * @param context the eMatrix <code>Context</code> object
       * @param MPMap Map containing information about all the Manufacturing Plan under Master Manufacturing Plan connected with the Model
       * 					 
       * @throws Exception
       */
      private void  processGBOMForEffectivity(Context context, String strLogicalFeatureID, Map gbomMap)throws Exception {    	  
    	  try{
    		  Object tempObj = null;
    		  String gbomRelId = (String)gbomMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);    		  
    		  String jsonString = null;
    		  StringBuffer strFormatedExpr = new StringBuffer();	
    		  String ruleComplexity = (String)gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY +"].value");
    		  String IRid = (String)gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.id");
    		  String IRType = (String)gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.type");
    		  String IRName = (String)gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.name");
    		  String IRRevision = (String)gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.revision");
    		  String attributeRightExpression = (String)gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION +"].value");    		  
    		  tempObj = gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.type");
    		  StringList rightExpressionToRel = ConfigurationUtil.convertObjToStringList(context, tempObj);
    		  
    		  String logString = IRType+ "," +IRName+ "," +IRRevision; 
    		  if(ruleComplexity.trim().equals("Complex")){
    			  writeUnconvertedOID(logString+"IR is Complex, Not Supported! "+"\n", IRid);  
    			  mqlLogRequiredInformationWriter(logString+"IR is Complex, Not Supported! "+"\n");
    		  }
    		  else if(ruleComplexity.trim().equals("Simple") && rightExpressionToRel.contains("Common Group")){
    			  writeUnconvertedOID(logString+"IR Right Expression Is Connection With Common Group, Not Supported! "+"\n", IRid);  
    			  mqlLogRequiredInformationWriter(logString+"IR Right Expression Is Connection With Common Group, Not Supported! "+"\n");
    		  }
    		  else if(ruleComplexity.trim().equals("Simple") && attributeRightExpression.contains(" OR ")){
    			  writeUnconvertedOID(logString+"IR Right Expression Was Connection With Common Group, Not Supported! "+"\n", IRid);  
    			  mqlLogRequiredInformationWriter(logString+"IR Right Expression Was Connection With Common Group, Not Supported! "+"\n");
    		  }
    		  else if(ruleComplexity.trim().equals("Simple")){
    			  mqlLogRequiredInformationWriter(logString+"IR Is Simple And Will Be Processed For Migration"+"\n");
    			  
    			  tempObj = gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.physicalid");
    			  StringList slRelCOPhysicalId = ConfigurationUtil.convertObjToStringList(context, tempObj);

    			  tempObj = gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.from.physicalid");
    			  StringList slParentPhysicalId = ConfigurationUtil.convertObjToStringList(context, tempObj);

    			  tempObj = gbomMap.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION +"].from.from[Right Expression].torel.to.physicalid");
    			  StringList slChildPhysicalId = ConfigurationUtil.convertObjToStringList(context, tempObj);

    			  com.matrixone.apps.effectivity.EffectivityFramework Eff = new com.matrixone.apps.effectivity.EffectivityFramework(); 
				  com.matrixone.json.JSONObject effObj = new com.matrixone.json.JSONObject();
				  
    			  for(int k = 0; k < slParentPhysicalId.size(); k++){
    				  
    				  effObj.put("contextId", GLOBAL_MODEL_PHYSICAL_ID); //physicalid of the Model context
    				  effObj.put("parentId", slParentPhysicalId.get(k)); //physicalid of the CF 
    				  effObj.put("objId", slChildPhysicalId.get(k)); //physicalid of the CO
    				  effObj.put("relId", slRelCOPhysicalId.get(k)); //physicalid of the CO rel
    				  effObj.put("insertAsRange", false);

    				  jsonString = effObj.toString();
    				  Map formatedExpr =Eff.formatExpression(context, "FeatureOption", jsonString);
    				  
    				  strFormatedExpr.append((String)formatedExpr.get(Eff.ACTUAL_VALUE));
    				  strFormatedExpr.append(" AND ");
    			  }

    			  if(strFormatedExpr.indexOf("AND") != -1){
    				  strFormatedExpr.delete(strFormatedExpr.lastIndexOf("AND"),(strFormatedExpr.lastIndexOf("AND")+3));	
    			  }
    			  mqlLogRequiredInformationWriter("strFormatedExpr= " + strFormatedExpr + "\n");
    			  mqlLogRequiredInformationWriter("Connecting :"+ strLogicalFeatureID +" To Global Model: "+ GLOBAL_MODEL_ID +" Using Relationship "+ EffectivityFramework.RELATIONSHIP_CONFIGURATION_CONTEXT +" \n");
    			  DomainRelationship.connect(context, strLogicalFeatureID, EffectivityFramework.RELATIONSHIP_CONFIGURATION_CONTEXT, GLOBAL_MODEL_ID, false);
    			  mqlLogRequiredInformationWriter("Setting Effectivity Expression On Rel: "+ gbomRelId +" \n");
    			  Eff.setRelExpression(context, gbomRelId , strFormatedExpr.toString());  
    			  mqlLogRequiredInformationWriter("Deleting Inclusion Rule Object ID: "+ IRid +" \n");
    			  DomainObject.deleteObjects(context, new String[]{IRid});    			  
    		  }
    	  }
    	  catch(Exception e){
    		  throw e;
    	  }   	  
      }
       
       public boolean writeOID(Context context, String[] args) throws Exception
       {
    	   _objectidList.add(args[0]);
    	   _counter++;

    	   if (_counter == _chunk)
    	   {
    		   _counter=0;
    		   _sequence++;

    		   //write oid from _objectidList
    		   for (int s=0;s<_objectidList.size();s++)
    		   {
    			   _fileWriter.write((String)_objectidList.elementAt(s));
    			   _fileWriter.newLine();
    		   }

    		   _objectidList=new StringList();
    		   _fileWriter.close();

    		   //create new file
    		   _oidsFile = new java.io.File(documentDirectory + "objectids_" + _sequence + ".txt");
    		   _fileWriter = new BufferedWriter(new FileWriter(_oidsFile));
    	   }


    	   return false;
       }
}
