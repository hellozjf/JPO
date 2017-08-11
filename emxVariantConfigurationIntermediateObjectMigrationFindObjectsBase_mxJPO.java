/*
 * emxVariantConfigurationIntermediateObjectMigrationFindObjectsBase
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;


/**
 * The <code>emxVariantConfigurationFindIntermediateObjectsBase</code> class contains implementation code for emxVariantConfigurationFindIntermediateObjectsBase.
 *
 * @version FTR V6R2012x - Copyright (c) 2011-2016, Dassault Systemes, Inc.
 */
public class emxVariantConfigurationIntermediateObjectMigrationFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
	
	  public static String ATTRIBUTE_FTRMigrationConflict = "FTRMigrationConflict";
	  FileWriter conflictIDWriter = null;
	
	
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since FTR V6R2012x
     */
    public emxVariantConfigurationIntermediateObjectMigrationFindObjectsBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
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

        StringBuffer typeList = new StringBuffer(100);
        typeList.append(PropertyUtil.getSchemaProperty(context,"type_Features"));
        
		emxVariantConfigurationIntermediateObjectMigration_mxJPO e= new emxVariantConfigurationIntermediateObjectMigration_mxJPO(context, new String[0]);
        StringList slDerivationChangedType = e.getDerivationChangedType(context,new String[0]);
        for(int i=0;i<slDerivationChangedType.size();i++){
        	typeList.append(",");
        	typeList.append(slDerivationChangedType.get(i));
        }
        typeList.append(",");
        typeList.append(PropertyUtil.getSchemaProperty(context,"type_Products"));
        typeList.append(",");
        typeList.append(PropertyUtil.getSchemaProperty(context,"type_GBOM"));
        typeList.append(",");
        typeList.append(PropertyUtil.getSchemaProperty(context,"type_Rule"));
        typeList.append(",");
        typeList.append(PropertyUtil.getSchemaProperty(context,"type_ProductConfiguration"));
        
        try
        {
            if (args.length < 2 )
            {
                throw new IllegalArgumentException();
            }
            String[] newArgs = new String[7];
            newArgs[0] = args[0];
            newArgs[1] = typeList.toString();
            newArgs[2] = args[1];
            newArgs[3] = "emxCommonMigration";
            newArgs[4] = "*";
            newArgs[5] = "*";            
            newArgs[6] = "type";
            return super.mxMain(context, newArgs);


        }
        catch (IllegalArgumentException iExp)
        {
            writer.write("=================================================================\n");
            writer.write("Wrong number of arguments Or Invalid number of Oids per file\n");
            writer.write("Step 1 of Migration ${CLASS:emxVariantConfigurationIntermediateObjectFindObjectsBase} :   " + iExp.toString() + "   : FAILED1111 \n");
            writer.write("=================================================================\n");
            writer.close();
            return 0;
        }
    }  
    
    /**
     * Evalutes a query to get all the Features, GBOM and RUle objects in the system
     * @param context the eMatrix <code>Context</code> object
     * @param chunksize has the no. of objects to be stored in file.
     * @return void
     * @exception Exception if the operation fails.
     */
    public void getIds(Context context, int chunkSize) throws Exception
    {
        // Written using temp query to optimize performance in anticipation of
        // large 1m+ objects in system.
        // Utilize query limit to use different algorithm in memory allocation

        String vaultList = "*";

        

        //First tokenize type for list of object types, will be in form of "Features,GBOM,Inclusion Rule"
        StringTokenizer stTypes = new StringTokenizer(type, ",");
        String typeName = "";
        StringList commandList = new StringList(3);
        while (stTypes.hasMoreTokens())
        {
        	typeName = stTypes.nextToken();
        	if(typeName!=null)
        	{
        		if(typeName.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCTS)){
        			commandList.addElement("temp query bus '" + typeName + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where 'program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true &&" +
        			"attribute["+ATTRIBUTE_FTRMigrationConflict+"]!=\"\"'");            
        		}else{
        			commandList.addElement("temp query bus '" + typeName + "' '" + name + "' '" + revision + "'  vault '" + vaultList + "' limit 1 where 'program[" + migrationProgramName + " -method writeOID ${OBJECTID} \"${TYPE}\"] == true'");            
        		}
        	}

        }
        //reset/set static variables back to original values every time this JPO is run
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
            String command = "";
            for (int i=0; i< commandList.size(); i++)
            {
                command = (String)commandList.get(i);
                MqlUtil.mqlCommand(context, command);
            }
        }
        catch(Exception me)
        {
            throw me;
        }

        // call cleanup to write the left over oids to a file
        emxCommonMigration_mxJPO.cleanup();
    }
    
    
    
    /**
     * Evalutes a query to get all the Features, GBOM and RUle objects in the system
     * @param context the eMatrix <code>Context</code> object
     * @param chunksize has the no. of objects to be stored in file.
     * @return void
     * @exception Exception if the operation fails.
     */
    public void preMigration(Context context, String[] args) throws Exception
    {
        // Written using temp query to optimize performance in anticipation of
        // large 1m+ objects in system.
        // Utilize query limit to use different algorithm in memory allocation
        try{
        	
         // documentDirectory does not ends with "/" add it
           documentDirectory = args[1];
            String fileSeparator = java.io.File.separator;
            if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
            {
                documentDirectory = documentDirectory + fileSeparator;
            }
        	
        	
   	    String[] newArgs = new String[3];
        newArgs[0] = args[0];
    	String str = newArgs[0].toString();
        int chunkSize = Integer.parseInt(str);
          
         String strMqlCommandOff = "trigger off";
		  MqlUtil.mqlCommand(context,strMqlCommandOff,true);
		  System.out.println("\n\n\n"+ " 'trigger off' command executed " + "\n");
        
          //Check for mal formed FL  objects
		  StringBuffer sBFeatListTypePattern = new StringBuffer(50);
		  sBFeatListTypePattern.append(ConfigurationConstants.TYPE_FEATURE_LIST);

		  StringBuffer sBFeatListObjWhere = new StringBuffer(100);
		  sBFeatListObjWhere.append("!((");
		  sBFeatListObjWhere.append("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"]");
		  sBFeatListObjWhere.append("&&");
		  sBFeatListObjWhere.append("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]");
		  sBFeatListObjWhere.append(")");
		  
		  sBFeatListObjWhere.append("||");		  
		  		  
		  sBFeatListObjWhere.append("(");
		  sBFeatListObjWhere.append("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"]");
		  sBFeatListObjWhere.append("&&");
		  sBFeatListObjWhere.append("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"]");
		  sBFeatListObjWhere.append(")");
		  
		  sBFeatListObjWhere.append("||");	
		  		  
		  sBFeatListObjWhere.append("(");
		  sBFeatListObjWhere.append("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"]");
		  sBFeatListObjWhere.append("&&");
		  sBFeatListObjWhere.append("to["+ConfigurationConstants.RELATIONSHIP_COMMITED_ITEM+"]");
		  sBFeatListObjWhere.append("))");
		  

		  StringList sLFeatListobjSelects = new StringList();
		  sLFeatListobjSelects.addElement(ConfigurationConstants.SELECT_NAME);
		  sLFeatListobjSelects.addElement(ConfigurationConstants.SELECT_TYPE);
		  sLFeatListobjSelects.addElement(ConfigurationConstants.SELECT_ID);

		  System.out.println("\n\n");
		  System.out.println("Query executed to find the Malformed Feature List type objects in database."+"\n");
		  System.out.println("This will include the 'Feature List' type objects with missing/incorrect connections or are standalone.");
		  System.out.println("\n\n");
		  
		  List lsMalFormedFLObjects = DomainObject.findObjects(context,
				  sBFeatListTypePattern.toString(),
				  DomainConstants.QUERY_WILDCARD,
				  sBFeatListObjWhere.toString(),
				  sLFeatListobjSelects);


		  //Check for mal formed GBOM objects
		  StringBuffer sBGBOMTypePattern = new StringBuffer(50);
		  sBGBOMTypePattern.append(ConfigurationConstants.TYPE_GBOM);

		  StringBuffer sBGBOMObjWhere = new StringBuffer(100);
		  sBGBOMObjWhere.append("!((");
		  sBGBOMObjWhere.append("from["+ConfigurationConstants.RELATIONSHIP_GBOM_TO+"]");
		  sBGBOMObjWhere.append("&&");
		  sBGBOMObjWhere.append("to["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"]");
		  sBGBOMObjWhere.append(")");

		  sBGBOMObjWhere.append("||");

		  sBGBOMObjWhere.append("(");
		  sBGBOMObjWhere.append("from["+ConfigurationConstants.RELATIONSHIP_GBOM_TO+"]");
		  sBGBOMObjWhere.append("&&");
		  sBGBOMObjWhere.append("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+"]");
		  sBGBOMObjWhere.append(")");

		  sBGBOMObjWhere.append("||");

		  sBGBOMObjWhere.append("(");
		  sBGBOMObjWhere.append("from["+ConfigurationConstants.RELATIONSHIP_GBOM_TO+"]");
		  sBGBOMObjWhere.append("&&");
		  sBGBOMObjWhere.append("to["+ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM+"]");

		  sBGBOMObjWhere.append("))");

		  StringList sLGBOMobjSel = new StringList();
		  sLGBOMobjSel.addElement(ConfigurationConstants.SELECT_NAME);
		  sLGBOMobjSel.addElement(ConfigurationConstants.SELECT_TYPE);
		  sLGBOMobjSel.addElement(ConfigurationConstants.SELECT_ID);
		  
		  System.out.println("\n\n");
		  System.out.println("Query executed to find the Malformed 'GBOM' type objects in database."+"\n");
		  System.out.println("This will include the 'GBOM' type objects with missing/incorrect connections or are standalone.");
		  System.out.println("\n\n");
		  
		  List lsMalFormedGBOMObjects = DomainObject.findObjects(context,
				  sBGBOMTypePattern.toString(),
				  DomainConstants.QUERY_WILDCARD,
				  sBGBOMObjWhere.toString(),
				  sLGBOMobjSel);
		  
		  
		  StringList sLMalFormedGBOMObjects = new StringList();
	      StringList sLMalFormedFLObjects = new StringList();
	         
	         
		  Map FLObjMap = new HashMap();
		  for(int i=0;i<lsMalFormedFLObjects.size();i++){
	  		  FLObjMap = (Map)lsMalFormedFLObjects.get(i);
	  		  String strFLId = (String)FLObjMap.get(ConfigurationConstants.SELECT_ID);
	  		  sLMalFormedFLObjects.add(strFLId);
	  		  writeOIDToTextFile(strFLId,"MalFormedFeatureListOIDs_deleted");
		  }
  	  
		  Map GBOMObjMap = new HashMap();
		  for(int i=0;i<lsMalFormedGBOMObjects.size();i++){
	  		  GBOMObjMap = (Map)lsMalFormedGBOMObjects.get(i);
	  		  String strGBOMId = (String)GBOMObjMap.get(ConfigurationConstants.SELECT_ID);
	  		  sLMalFormedGBOMObjects.add(strGBOMId);
	  		  writeOIDToTextFile(strGBOMId,"MalFormedGBOMOIDs_deleted");
		  }
       
		 if(!lsMalFormedFLObjects.isEmpty()){
			 System.out.println("MalFormed FeatureList OIDs found... " + "\n");
			 System.out.println("Deletion of MalFormed FeatureList OIDs started... " + "\n");
	         deleteObjects(context,sLMalFormedFLObjects);
	         System.out.println("Deletion of MalFormed FeatureList OIDs ended... " + "\n"+"\n"+"\n");
	         System.out.println("Please view file 'MalFormedFeatureListOIDs_deleted.txt' for a list of objects deleted " + "\n");
		 }
		 
	     if(!lsMalFormedGBOMObjects.isEmpty()){
	    	 System.out.println("MalFormed GBOM OIDs found... " + "\n");
	    	 System.out.println("Deletion of MalFormed GBOM OIDs started... " + "\n");
	      	 deleteObjects(context,sLMalFormedGBOMObjects);
	         System.out.println("Deletion of MalFormed GBOM OIDs ended... " + "\n"+"\n"+"\n");	
	         System.out.println("Please view file 'MalFormedGBOMOIDs_deleted.txt' for a list of objects deleted " + "\n");
	     }
	     
         String strMqlCommandOn = "trigger on";
		 MqlUtil.mqlCommand(context,strMqlCommandOn,true);
		 System.out.println("\n\n" + "'trigger on' command executed " + "\n");
       
       
        System.out.println("\n\n\n\n");
		System.out.println("This step queries in databse, to find the top level object ids in the structure..." + "\n");
		System.out.println("This include type Product Line,Model,Products and Feature..." + "\n");
		System.out.println("These top level object ids will be used in next step to traverse each structure in database..." + "\n");
        
        
        String vaultList = "";

        try
        {
            EnoviaResourceBundle.getProperty(context,"emxComponents.CommonMigration.VaultList");
        }
        catch(Exception e)
        {
            //do nothing
        }

        if(vaultList == null || "null".equals(vaultList) || "".equals(vaultList))
        {
            vaultList = "*";
        }

           // Getting top level objects (this includes Feature,Products and ProductLines and also standalone features
			StringBuffer sbTypePattern = new StringBuffer(50);
			sbTypePattern.append(ConfigurationConstants.TYPE_FEATURES);
			sbTypePattern.append(",");
			sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
			sbTypePattern.append(",");
			sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCT_LINE);
			
			emxVariantConfigurationIntermediateObjectMigration_mxJPO e= new emxVariantConfigurationIntermediateObjectMigration_mxJPO(context, new String[0]);


			StringList slDerivationChangedType = e.getDerivationChangedType(context,new String[0]);

			StringBuffer sbWhereClause = new StringBuffer(50);
			sbWhereClause.append("(");
			sbWhereClause.append("type.kindof == \"" + ConfigurationConstants.TYPE_FEATURES + "\"");
			for(int i=0;i<slDerivationChangedType.size();i++){
    			sbTypePattern.append(",");
    			sbTypePattern.append(slDerivationChangedType.get(i));
    			sbWhereClause.append("||");
    			sbWhereClause.append("type.kindof == \"" + slDerivationChangedType.get(i).toString() + "\"");
            }
			sbWhereClause.append(")");
			String strTypePattern = sbTypePattern.toString();

			StringBuffer sbObjWhere = new StringBuffer(50);
			sbObjWhere.append("((");
			
			sbObjWhere.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "]==False");
			sbObjWhere.append("&&");
			sbObjWhere.append(sbWhereClause);
			sbObjWhere.append(")");
			
			sbObjWhere.append("||");
			
			sbObjWhere.append("(");
			sbObjWhere.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "]==False");
			sbObjWhere.append("&&");
			sbObjWhere.append("from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+ "]==True");
			sbObjWhere.append("&&");
			sbObjWhere.append("(");
			sbObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
			sbObjWhere.append("||");
			sbObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCT_LINE + "\"");
			sbObjWhere.append(")");
			sbObjWhere.append(")");
			
			sbObjWhere.append("||");
			
			sbObjWhere.append("(");
			sbObjWhere.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "]==True");
			sbObjWhere.append("&&");
			sbObjWhere.append("from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+ "]==True");
			sbObjWhere.append("&&");
			sbObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
			sbObjWhere.append("))");
			
			StringList objSelects = new StringList();
			objSelects.addElement("id");
			objSelects.addElement("type");
			objSelects.addElement("name");
			
			MapList topLevelObjects = DomainObject.findObjects(context, // context
					                            strTypePattern, // typePattern
							  					"*", // vaultPattern
							  					sbObjWhere.toString(), // whereExpression
							  					objSelects); // objectSelects
		System.out.println("\n\n");
        System.out.println("Total count of Top Level Object ids found in database is ------> "+topLevelObjects.size() + "\n\n\n\n");
        
      //reset/set static variables back to original values every time this JPO is run
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

        for (int i = 0; i < topLevelObjects.size(); i++) {
				Map tmpMap = (Map) topLevelObjects.get(i);
				String topLevelObjectId = (String) tmpMap.get(ConfigurationConstants.SELECT_ID);
				String topLevelObjectType = (String) tmpMap.get(ConfigurationConstants.SELECT_TYPE);
        	
				String[] args1 = new String [2];
				args1[0] = topLevelObjectId;  //Path of Output File
				args1[1] = topLevelObjectType;  //Path of Output File
				
				JPO.invoke(context, "emxCommonMigrationBase", null,"writeOID", args1, null);
        	
        }
        
        // call cleanup to write the left over oids to a file
        emxCommonMigration_mxJPO.cleanup();
        
		}        
		catch(Exception e){

		 writer.write("PreMigration :   " + e.toString() + "   : FAILED \n");
		}
    }
    
    
    /**
	 * Deletes the intermediate objects that have been converted sucessfully.
     * @param context the eMatrix <code>Context</code> object
	 * @param strLstFLToDel
	 * @throws Exception
	 */
	private void deleteObjects(Context context, StringList strLstFLToDel) throws Exception
	{
	  
	  try {
		
		// Iterate the stringList containing the FeatureLists Objects to delete
		String [] strFLToDel = new String[strLstFLToDel.size()];
	        
	        for (int m = 0; m < strLstFLToDel.size(); m++) {
	        	strFLToDel[m] = (String) strLstFLToDel.get(m);
	        }
	        
	        // Call DomainObject's deleteObjects method to delete all the Objects in the String array in a single transaction
            DomainObject.deleteObjects(context, strFLToDel);
         	

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException("Object Delete Failed :"+e.getMessage());
		}
	}
    
	public void writeOIDToTextFile(String objectId,String strTextFileName) throws Exception
    {
            if (objectId != null)
            {
          	  try {
              	    conflictIDWriter   = new FileWriter(documentDirectory + strTextFileName +".txt", true);
                    String newLine = System.getProperty("line.separator");
                    conflictIDWriter.write(objectId+newLine);
                    conflictIDWriter.flush();
          		} catch (Exception e) {
          			e.printStackTrace();
          			System.out.println("write OID failed. \n"+e.getMessage() + "\n");
          		} finally {
          		    // Close resource.
          		    if (conflictIDWriter != null) 
          		    	conflictIDWriter.close();
          		}

             }  
     }
    
}

