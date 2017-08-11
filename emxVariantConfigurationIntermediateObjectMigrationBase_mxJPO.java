/*
 * emxVariantConfigurationIntermediateObjectMigrationBase.java
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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.configuration.ProductVariant;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.util.MxXMLUtils;

/**
 * The <code>emxVariantConfigurationIntermediateObjectMigrationBase</code> class contains the utilities
 * necessary to migrate the Variant Configuration schema to V6R2012x.  It must be run on all FTR data
 * created prior to V6R2012x.
 */
  public class emxVariantConfigurationIntermediateObjectMigrationBase_mxJPO extends emxCommonMigration_mxJPO
  {
      //For Rule Migration
      public static final String SELECT_PHYSICALID ="physicalid";
      public static String ATTRIBUTE_NEW_FEATURE_TYPE = "NewFeatureType";
      public static String ATTRIBUTE_FTRMigrationConflict = "FTRMigrationConflict";
      public static final String SYMB_SPACE = " ";
	  public static String RELATIONSHIP_MANAGED_SERIES = PropertyUtil.getSchemaProperty("relationship_ManagedSeries");
	  public static String INTERFACE_FTRIntermediateObjectMigration = "FTRIntermediateObjectMigration";
	  public static final String ATTRIBUTE_TOKEN                  = PropertyUtil.getSchemaProperty("attribute_Token");
	  public static final String SELECT_ATTRIBUTE_TOKEN  = "attribute[".concat(ATTRIBUTE_TOKEN).concat("]");
	  public static final String ATTRIBUTE_LOGICAL_SELECTION_CRITERIA         = PropertyUtil.getSchemaProperty("attribute_LogicalSelectionCriteria");
	  public static final String ATTRIBUTE_USER_DEFINED_EFFECTIVITY                    = PropertyUtil.getSchemaProperty("attribute_UserDefinedEffectivity");
	  public static final String ATTRIBUTE_INAVLID_CONTEXTS                  = PropertyUtil.getSchemaProperty("attribute_InvalidContexts");
	  public static final String RANGE_VALUE_INPUT                          = "Input";
	  private final static String STR_ATTRIBUTE = "attribute";
	  protected static final String OPEN_BRACE = "[";
	  protected static final String CLOSE_BRACE = "]";
	  public static String TYPE_CONFIGURATION_FEATURE				= PropertyUtil.getSchemaProperty("type_ConfigurationFeature");
	  protected final static String STR_EMPTY = "";
	  protected final static String STR_ATTRIBUTE_OPEN_BRACE = "attribute[";
	  protected final static String EXPRESSION_CLOSE = "]";
	  protected final static String STR_TRUE = "TRUE";
	  protected final static String STR_FALSE = "FALSE";
	  protected static final String AND = "AND";
	  protected static final String OR = "OR";
	  protected static final String NOT = "NOT";
	  FileWriter conflictIDWriter = null;
	  public boolean IS_CONFLICT = false;
	  public static String RESOLVE_CONFLICTS = " ";
	  /**
      * Default constructor.
      * @param context the eMatrix <code>Context</code> object
      * @param args String array containing program arguments
      * @throws Exception if the operation fails
      */
      public emxVariantConfigurationIntermediateObjectMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {

          super(context, args);
          this._interOpCommand = "command_FTRInterOpIntermediateObjectMigration";
          this.warningLog = new FileWriter(documentDirectory + "migration.log", true);

      }

      /**
       * Main migration method to handle migrating Features, GBOM and Rules.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList)
                                                          throws Exception
      {

    	  mqlLogRequiredInformationWriter("In 'migrateObjects' method \n");

    	  int migrationStatus = getAdminMigrationStatus(context);
    	  mqlLogRequiredInformationWriter("Migration Status value :: "+ migrationStatus +"\n");

          warningLog = new FileWriter(documentDirectory + "migrateObjects.log", true);

          if(migrationStatus<2)
          {
        	  mqlLogRequiredInformationWriter("Pre-migration is not complete.Please complete Pre-migration before running Feature migration. \n");
        	  return;
          }
          try
          {
        	  //loadResourceFile(context, "emxConfigurationMigration");
        	  mqlLogRequiredInformationWriter("Resource file loaded in 'migrateObjects' method ------> emxConfigurationMigration.properties"+"\n");

        	  StringList featureIds = new StringList(500);
              StringList gbomIds = new StringList(500);
              StringList ruleIds = new StringList(500);
              StringList pcIds = new StringList(500);
              //StringList objectSelects = new StringList(5);
              StringList objectSelects = new StringList(2);

              StringList sLFeatAndRuleIds = new StringList(500);

              objectSelects.add(SELECT_TYPE);
              //objectSelects.add(SELECT_NAME);
              //objectSelects.add(SELECT_VAULT);
              //objectSelects.add(SELECT_REVISION);
              objectSelects.add(SELECT_ID);

              
              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
              Iterator itr = mapList.iterator();
              Map map = new HashMap();
              String objectId = "";
              String strType = "";
              //String strName = "";
              //String strRev = "";
              //String revId = "";
              while (itr.hasNext())
              {
                  map = (Map) itr.next();
                  objectId = (String) map.get(SELECT_ID);
                  strType = (String) map.get(SELECT_TYPE);
                  //strName = (String) map.get(SELECT_NAME);
                  //strRev = (String) map.get(SELECT_REVISION);
				  

                  if (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_FEATURES)
                		  ||
                		  mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_PRODUCTS))//To handle use case if "Product" is added as Technical Feature
                  {
                      featureIds.addElement(objectId);
                      sLFeatAndRuleIds.addElement(objectId);

                  }
                  else if (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_GBOM))
                  {
                      gbomIds.addElement(objectId);
                  }
                  else if (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_RULE))
                  {
                      ruleIds.addElement(objectId);
                      sLFeatAndRuleIds.addElement(objectId);

                  }else if (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES))
                  {
                      featureIds.addElement(objectId);
                      sLFeatAndRuleIds.addElement(objectId);
                  }else if (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
                		    || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)
                		    || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_SOFTWARE_FEATURE)
                		  )
                  {
                      featureIds.addElement(objectId);
                      sLFeatAndRuleIds.addElement(objectId);

                  }else if (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION))
		          {
                	  pcIds.addElement(objectId);
		          }
                  else
                  {
                      //invalid type to migrate
                      mqlLogRequiredInformationWriter("Invalid migration object type for id=" + objectId);
                      String failureMessage = objectId + " : " + "INVALID TYPE"+"\n";
                      writeUnconvertedOID(failureMessage,objectId);
                  }
              }

              //Now call the migration method based on type
              //It is not expected that all these will be run each time, most likely
              //only one of these list will be filled at a time.  The individual method checks
              //for empty list and just returns.


                  mqlLogRequiredInformationWriter("Size of object Ids of different types in this text file.\n");
              	  mqlLogRequiredInformationWriter("Size of Feature Ids                ---------------->"+ featureIds.size() + "\n");
              	  mqlLogRequiredInformationWriter("Size of GBOM Ids                   ---------------->"+ gbomIds.size() + "\n");
              	  mqlLogRequiredInformationWriter("Size of Rule Ids                   ---------------->"+ ruleIds.size() + "\n");
              	  mqlLogRequiredInformationWriter("Size of Product Configuration Ids  ---------------->"+ pcIds.size() + "\n\n");

              
		  if(migrationStatus == 2 || migrationStatus == 4) { // PreMigration completed or Features in progress
            	  migrateFeatures(context, featureIds);
			migrationStatus = getAdminMigrationStatus(context);
		  }
            	  if(migrationStatus == 5 || migrationStatus == 7) { // Features completed or GBOM in progress
            	  migrateGBOM(context, gbomIds);
			migrationStatus = getAdminMigrationStatus(context);
		  }
            	  if(migrationStatus == 8 || migrationStatus == 9 || migrationStatus == 10 ) { // GBOM completed or Rule in progress
            	  migrateRule(context, ruleIds);
			migrationStatus = getAdminMigrationStatus(context);
		  }

                  removalOfTemporaryAttSet(context, sLFeatAndRuleIds);
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

      /**
       * Migrates Feature objects and all connections to those features.  The intermediate
       * Feature List objects will be deleted and replaced with appropriate relationship connections.
       * Also calls external migration methods to allow external applicatons to migrate custom schema.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      private void  migrateFeatures(Context context, StringList objectIdList)throws Exception{

    	  if (objectIdList == null || objectIdList.size() <= 0){
		setAdminMigrationStatus(context,"FeatureComplete");
		mqlLogRequiredInformationWriter("Feature migration done.\n");
		return;
		  }

    	  try{
        		  //STEP1 - turn triggers off
        		  String strMqlCommand = "trigger off";
        		  MqlUtil.mqlCommand(context,strMqlCommand,true);

        		  //STEP2 - set admin property
        		  setAdminMigrationStatus(context,"FeatureInProcess");
        		  mqlLogRequiredInformationWriter("Migration Status set to :: FeatureInProcess\n");

        		  //STEP3 - get "Feature" object list related data.. in one "getinfo" call
            	  String[] oidsArray = new String[objectIdList.size()];
                  oidsArray = (String[])objectIdList.toArray(oidsArray);

                  StringList featureObjSelects = new StringList();
    			  featureObjSelects.addElement(SELECT_TYPE);
    			  featureObjSelects.addElement(SELECT_NAME);
    			  featureObjSelects.addElement(SELECT_REVISION);
    			  featureObjSelects.addElement(SELECT_VAULT);
    			  featureObjSelects.addElement(SELECT_ID);
    			  featureObjSelects.addElement(DomainObject.SELECT_TO_ID);

    			  featureObjSelects.addElement("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");
    			  featureObjSelects.addElement("attribute["+ATTRIBUTE_FTRMigrationConflict+"]");

    			  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT+"]");//goes into LF or MF type
    			  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML+"]");//goes into LF,MF,CF type
    			  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");//goes into LF,MF,CF type
    			  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]");//goes into LF,MF,CF type
    			  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_ORIGINATOR+"]");//goes into LF,MF,CF type

    			  //For DV
    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");//Varies By rel id if present
    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.id");//Varies By rel id if present
    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.name");//Varies By rel id if present

    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");//Varies By rel id if present
    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_INAVLID_CONTEXTS+"]");//Varies By rel id if present
    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_USER_DEFINED_EFFECTIVITY+"]");//Varies By rel id if present

    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");//Varies By rel id if present
    			  featureObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");//Varies By rel id if present

    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.id");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.name");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_INAVLID_CONTEXTS+"]");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");
    			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_USER_DEFINED_EFFECTIVITY+"]");

                  MapList mapList = DomainObject.getInfo(context, oidsArray, featureObjSelects);

                  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");


                  Iterator itr = mapList.iterator();
                  Map featObjMap = new HashMap();

                  String featureObjId = "";
                  //String strType = "";
                  //String strName = "";
                  //String strRevision = "";

                  while (itr.hasNext())
                  {
                	featObjMap = (Map) itr.next();
    				featureObjId = (String) featObjMap.get(SELECT_ID);

    				//strType = (String) featObjMap.get(SELECT_TYPE);
    				//strName = (String) featObjMap.get(SELECT_NAME);
    				//strRevision = (String) featObjMap.get(SELECT_REVISION);

    				mqlLogRequiredInformationWriter("\n\n\n");
    				mqlLogRequiredInformationWriter("Processing for feature object ::" + featureObjId + " started\n\n");

    				//migrateFeatures Step1:
                	try{
                		migrateFeaturesStep_1(context,featObjMap);

                	 }catch(Exception e)
    				   {
                		e.printStackTrace();
        				String strCommand = featureObjId + ":" + e.getMessage() + "\n";
        				writeUnconvertedOID(strCommand, featureObjId);
                        continue; // really?
    				   }

                	 mqlLogRequiredInformationWriter("Migration for Feature id :: "+ featureObjId + " is done.\n");
                   }

             	  //check if any FLs present in database.If not set migration status as FeatureComplete
          		  if(!isTypeExists(context,ConfigurationConstants.TYPE_FEATURE_LIST))
          		  {
          			  setAdminMigrationStatus(context,"FeatureComplete");
          			  mqlLogRequiredInformationWriter("Feature Migration Completed.\n");
          		  }else{
          			  mqlLogRequiredInformationWriter("Feature Migration batch successful, proceeding with next batch.\n");
          		  }

                //turn triggers off
          		  strMqlCommand = "trigger on";
          		  MqlUtil.mqlCommand(context,strMqlCommand,true);
      	}catch(Exception e)
      	  {
      		  e.printStackTrace();
      		  mqlLogRequiredInformationWriter("Feature Migration Failed.\n");
      		  throw e;
      	  }
      	  return;
      }

      /**
       * Connects the given parent feature with the given child feature using the relationship specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param strParentFeatureType String specifying the type of the parent feature
       * @param newChildFeatureChangeType String specifying the type of the child feature
       * @param strParentFeatureId String specifying parent feature object id
       * @param strChildFeatureId String specifying child feature object id
       * @param strNewRelToConnect String specifying relationship name to connect the parent and child
       * @return String specifying the new relationship id
       * @throws Exception if the operation fails
       */

      //private String  connectFeaturesWithNewRel(Context context,Map strParentFeatureId,String strChildFeatureId,String strChildFeatureType,String strNewRelToConnect) throws FrameworkException, Exception{
      private String  connectFeaturesWithNewRel(Context context,Map htParentObjData,String strChildFeatureId,String strChildFeatureType,String strNewRelToConnect) throws FrameworkException, Exception{

    	  String strParentFeatureType = (String)htParentObjData.get(SELECT_TYPE);
    	  String strParentFeatureId = (String)htParentObjData.get(SELECT_ID);
    	  String strParentFeatureName = (String)htParentObjData.get(SELECT_NAME);
    	  String strParentFeatureRev = (String)htParentObjData.get(SELECT_REVISION);
    	  String newParentFeatureTypeSymbolic = (String)htParentObjData.get("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");
    	  String newParentFeatureType = PropertyUtil.getSchemaProperty(newParentFeatureTypeSymbolic);

    	  String strRelId = null;
    	  try{
    	  String[] sARelId = new String[1];
    	  sARelId[0] = strChildFeatureId;

    	  RelationshipType RelToConnect = new RelationshipType(strNewRelToConnect);

    	  //String strMqlCmd = "expand bus \""+strParentFeatureId+"\" from relationship \""+ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES+"\" preventduplicates select rel id where \"to.id =='"+strChildFeatureId +"'\" dump | " ;
    	  String strMqlCmd = "expand bus $1 from relationship $2 preventduplicates select $3 $4 where $5 dump $6 " ;

		  String strRelID = MqlUtil.mqlCommand(context ,strMqlCmd ,true,strParentFeatureId,ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES,"rel","id","to.id =="+strChildFeatureId +"","|") ;

		  StringTokenizer selRelTokenizer = new StringTokenizer(strRelID,"|");

			while(selRelTokenizer.hasMoreTokens()){
				strRelId = selRelTokenizer.nextToken();
			}
			if (strRelId == null ){
				strRelId = "";
			}
			String strTempRelId[] = strRelId.split("\n");
			strRelId = strTempRelId[0];

          if(strRelId!=null && strRelId.trim().equalsIgnoreCase("")){
        	  
        	  mqlLogRequiredInformationWriter("Connection between   " +strParentFeatureId+ "  and " +strChildFeatureId+ "  rel "+ strNewRelToConnect + " executed."+ strParentFeatureType +  "\n\n");

        	  
        	  if(mxType.isOfParentType(context, newParentFeatureType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES) 
        	      && ProductLineCommon.isOfParentRel(context, strNewRelToConnect, ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES)
        	      )
        	  {
        		  mqlLogRequiredInformationWriter("Write to log file as CF-CF realtionship no longer supported" + strParentFeatureId);
        		  String strCommand = strParentFeatureType + "," + strParentFeatureName + "," + strParentFeatureRev +"\n";
    			  writeUnconvertedOID(strCommand,strParentFeatureId); 
        	  }else{

        	  mqlLogRequiredInformationWriter("Connection between   " +strParentFeatureId+ "  and " +strChildFeatureId+ "  rel "+ strNewRelToConnect + " executed."+  "\n\n");

        	  Map mRelIds = DomainRelationship.connect(context,
  					   new DomainObject(strParentFeatureId),
  					   RelToConnect,
  					   true,
  					   sARelId);

          	  	strRelId =(String)mRelIds.get(strChildFeatureId);
        	  }
          }else{

        	  mqlLogRequiredInformationWriter("Duplicate connection between   " +strParentFeatureId+ "  and" +strChildFeatureId+ "  rel "+ strNewRelToConnect + " skipped as connection already exists\n");
          }

    	  }catch (Exception e) {
    		  e.printStackTrace();
    		  mqlLogRequiredInformationWriter("Write to log file " + strParentFeatureId);
    		  //To log the Parent Feature id also.
    		  String strCommand = strParentFeatureType + "," + strParentFeatureName + "," + strParentFeatureRev +"\n";
			  writeUnconvertedOID(strCommand,strParentFeatureId);
			  throw new FrameworkException(e.getMessage());
		}
    	  return strRelId;
      }



      /**
       * Migrates GBOM objects and all connections to those objects.  The intermediate
       * GBOM objects will be deleted and replaced with GBOM relationship connections.  All connections
       * to GBOM objects will be floated to the new GBOM relationships.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @return nothing
       * @throws Exception if the operation fails
       */
      private void  migrateGBOM(Context context, StringList objectIdList)throws Exception {

    	  if (objectIdList == null || objectIdList.size() <= 0) {
		setAdminMigrationStatus(context,"GBOMComplete");
		mqlLogRequiredInformationWriter("GBOM migration done\n");
		  }
    	  try {
    	    		  //STEP1 - turn triggers off
            		  String strMqlCommand = "trigger off";
            		  MqlUtil.mqlCommand(context,strMqlCommand,true);

    	        	  setAdminMigrationStatus(context,"GBOMInProcess");
			mqlLogRequiredInformationWriter("Migration Status set to GBOMInProcess\n");

    	        	  String[] oidsArray = new String[objectIdList.size()];
    	              oidsArray = (String[])objectIdList.toArray(oidsArray);

    	              StringList gbomObjSelects = new StringList();
    				  gbomObjSelects.addElement(SELECT_TYPE);
    				  gbomObjSelects.addElement(SELECT_NAME);
    				  gbomObjSelects.addElement(SELECT_REVISION);
    				  gbomObjSelects.addElement(SELECT_ID);

    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM+ "].from.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM+ "].id");
    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].from.id");
    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM+ "].from.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM+ "].id");
    				  gbomObjSelects.addElement("from[" + ConfigurationConstants.RELATIONSHIP_GBOM_TO+ "].to.id");
    				  gbomObjSelects.addElement("attribute[" + ConfigurationConstants.ATTRIBUTE_COMMITTED+ "]");
    				  gbomObjSelects.addElement("attribute[" + ConfigurationConstants.ATTRIBUTE_RULE_TYPE+ "]");
    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].attribute["+ ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_PART+ "]");
    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+ "].attribute["+ ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_VARIANT+ "]");

    				  gbomObjSelects.addElement(DomainObject.SELECT_FROM_ID);
    				  gbomObjSelects.addElement(DomainObject.SELECT_TO_ID);
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+ "].tomid.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+ "].frommid.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].tomid.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM+ "].frommid.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM+ "].tomid.id");
    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM+ "].frommid.id");

    				  gbomObjSelects.addElement("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].from.id");
    				  gbomObjSelects.addElement("from[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to.id");
    				  gbomObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+"]");

    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.type");
    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].from.type");
    				  gbomObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM + "].from.type");

    				 DomainConstants.MULTI_VALUE_LIST.add("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM+ "].id");

    	              MapList mapList = DomainObject.getInfo(context, oidsArray, gbomObjSelects);
    	              Iterator itr = mapList.iterator();
    	              Map gbomObjMap = new HashMap();
    	              String gbomObjId = "";
    	              //String strType = "";
    	              //String strName = "";
    	              //String strRevision = "";
    	              //String revId = "";
    	              
    	              MapList gbomInfoList = new MapList();
     	              MapList gbomInfoListForExtMigration = new MapList();

    	              StringList sLgbomObjToDelete = new StringList();

    	              while (itr.hasNext())
    	              {
    	            	  try{
    	            	  gbomObjMap = (Map) itr.next();
    	                  String newReltoConnect = "";
    					  String strFeatureId = "";
    					  Map gbomInfoMap = new HashMap();
    					  gbomObjId = (String) gbomObjMap.get(SELECT_ID);

    					  mqlLogRequiredInformationWriter("\n\n\n");
    	    			  mqlLogRequiredInformationWriter("Processing for GBOM object ::" + gbomObjId + " started\n\n");

    					  //strType = (String) gbomObjMap.get(SELECT_TYPE);
    					  //strName = (String) gbomObjMap.get(SELECT_NAME);
    					  //strRevision = (String) gbomObjMap.get(SELECT_REVISION);

    					  mqlLogRequiredInformationWriter("GBOM Object Data ---->"+ "\n" + gbomObjMap +"\n\n");

    					  if (gbomObjMap.get("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.id") != null) {
    						  strFeatureId = (String) gbomObjMap.get("to["+ ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.id");
    						  newReltoConnect = ConfigurationConstants.RELATIONSHIP_GBOM;
    					  } else if (gbomObjMap.get("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].from.id") != null) {
    						  strFeatureId = (String) gbomObjMap.get("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].from.id");
    						  newReltoConnect = ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM;
    					  } else if (gbomObjMap.get("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM+ "].from.id") != null) {
    						  strFeatureId = (String) gbomObjMap.get("to["+ ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM + "].from.id");
    						  newReltoConnect = ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM;
    					  }
    					  String strPartId = (String) gbomObjMap.get("from["+ ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to.id");
    					  String strCommitted = (String) gbomObjMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_COMMITTED + "]");
    					  String strRuleType = (String) gbomObjMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");


    					  DomainRelationship domGBOMRel = DomainRelationship.connect(context, new DomainObject(strFeatureId),newReltoConnect, new DomainObject(strPartId));


    					  StringList relDataSelects = new StringList();
    					  relDataSelects.addElement(DomainRelationship.SELECT_ID);
    					  Map gbomRelData = domGBOMRel.getRelationshipData(context,relDataSelects);
    					  String gbomRelId = (String) (((StringList) gbomRelData.get(DomainRelationship.SELECT_ID)).get(0));
    					  Map attribMap = new HashMap();
    					  attribMap.put(ConfigurationConstants.ATTRIBUTE_COMMITTED, strCommitted);
    					  attribMap.put(ConfigurationConstants.ATTRIBUTE_RULE_TYPE, strRuleType);

    					  if (newReltoConnect.equals(ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM)) {
    						  String strInactiveFromPart = (String)gbomObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_PART+"]");
    						  String strInactiveFromVariant = (String)gbomObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_VARIANT+"]");
    						  attribMap.put(ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_PART,strInactiveFromPart);
    						  attribMap.put(ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_VARIANT,strInactiveFromVariant);
    					  }

    					  mqlLogRequiredInformationWriter("GBOM Rel id :: "+ domGBOMRel +"\n");
    					  mqlLogRequiredInformationWriter("Attribute value Map set for this Rel id ---->" + "\n" + attribMap +"\n\n");
    					  domGBOMRel.setAttributeValues(context, attribMap);

    	    			  //preparing MapList to pass to external migration
    					  gbomInfoMap.put("objectId",gbomObjId);
    					  gbomInfoListForExtMigration.add(gbomInfoMap);

    					  //preparing MapList to pass to "floatGBOMConnections"
    					  gbomInfoMap.put("newRelId",gbomRelId);
    					  gbomInfoMap.put("infoMapGBOMConnection",gbomObjMap);
    					  gbomInfoList.add(gbomInfoMap);

    					  //Add FL object to delete list
    					  sLgbomObjToDelete.add(gbomObjId);
    					  loadMigratedOids(gbomObjId);

    	            	  }catch(Exception e)
    	    			  {

    	            		  e.printStackTrace();
     	    				  String strCommand = gbomObjId  + " : " + e.getMessage()+"\n";
    	    				  writeUnconvertedOID(strCommand,gbomObjId);
    	    				  continue;
    	    			  }
    	              }
    	              		// - Get all the relationship ids which are connected to GBOM object except for "GBOM From","GBOM To", "Inactive GBOM From"
    	              		// - float those relationships onto newly formed "GBOM" or "Inactive GBOM" relationship
    	              		floatGBOMConnections(context,gbomInfoList);

    						//call external migration
    						MapList customResults = callInterOpMigrations(context, gbomInfoListForExtMigration);
    						Iterator itrCustomResults = customResults.iterator();
    						while (itrCustomResults.hasNext()) {
    	                        Map customResultsMap = (Map)itrCustomResults.next();
    	  					  Integer status = (Integer)customResultsMap.get("status");
    	  					  String failureMessage = (String)customResultsMap.get("failureMessage");
    	  					  if(status==1){
    	  						throw new FrameworkException(failureMessage);
    	  					  }
    	  				  }

    						mqlLogRequiredInformationWriter("GBOM id deleted list :: "+ sLgbomObjToDelete +"\n\n");
    						 deleteObjects(context,sLgbomObjToDelete);

    	    		  //check if any GBOM objects exist in database.If not, set migration  status as "GBOMComplete"
    	    		  if(!isTypeExists(context,ConfigurationConstants.TYPE_GBOM))
    	    		  {
    	    			  setAdminMigrationStatus(context,"GBOMComplete");
    	    			  mqlLogRequiredInformationWriter("GBOM Migration Completed. \n\n");

    	    			  //Now need to update the "DuplicatePartXML" attribute on every "Logical Feature"
    	    			  StringBuffer sbTypePattern = new StringBuffer(50);
    	    			  sbTypePattern.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);

    	    			  StringList objSelects = new StringList();
    	    			  objSelects.addElement("id");
    	    			  //objSelects.addElement("type");
    	    			  //objSelects.addElement("name");

    	    			  MapList mLLogicalFeatIds = DomainObject.findObjects(context, // context
															  					sbTypePattern.toString(), // typePattern
															  					"*", // vaultPattern
															  					null, // whereExpression
															  					objSelects); // objectSelects



    	    			  for(int i=0;i<mLLogicalFeatIds.size();i++){

    	    				  Map tmpMap = (Map) mLLogicalFeatIds.get(i);
    	    				  String strLogicalFeatId = (String) tmpMap.get(ConfigurationConstants.SELECT_ID);

    	    				  //Call

    	    				  LogicalFeature logicalFTR= new LogicalFeature(strLogicalFeatId);
    	    				  logicalFTR.updateDuplicatePartXML(context);
    	    			  }

    	    		  }else{
    	    			  mqlLogRequiredInformationWriter("GBOM Migration batch Successful, proceeding with next batch.\n");
    	    		  }
    	    		//turn triggers off
              		  strMqlCommand = "trigger on";
              		  MqlUtil.mqlCommand(context,strMqlCommand,true);
    	  } catch (Exception e) {
    		  e.printStackTrace();
    		  mqlLogRequiredInformationWriter("GBOM Migration Failed. \n");
    		  throw e;
    	  }
    	  return;
   }

      /**
       * Migrates Rule related data.  Updates the cached attributes to reflect the migrated
       * Feature and GBOM schema.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @return nothing
       * @throws Exception if the operation fails
       */
      private void  migrateRule(Context context, StringList objectIdList)
                                                          throws Exception
      {
    		  int migrationStatus = getAdminMigrationStatus(context);
    	  
	if ((objectIdList == null || objectIdList.size() <= 0) && (migrationStatus == 9 || migrationStatus == 10 )) {		
		setAdminMigrationStatus(context,"RuleComplete");
		mqlLogRequiredInformationWriter("All Rule Migration Completely done\n");
		
    			  }
	else if ((objectIdList == null || objectIdList.size() <= 0)) {		
    			  setAdminMigrationStatus(context,"RuleObjectsNotFound");
		mqlLogRequiredInformationWriter("Rules not found in the file\n");
		
    		  }
	else{

         try{
       		   //STEP1 - turn triggers off
    		  String strMqlCommand = "trigger off";
    		  MqlUtil.mqlCommand(context,strMqlCommand,true);

           	  setAdminMigrationStatus(context,"RuleInProcess");
		mqlLogRequiredInformationWriter("Migration Status set to RuleInProcess" + "\n");

           	  //Get the info related to all Rule Objects in the list
           	  String[] oidsArray = new String[objectIdList.size()];
                 oidsArray = (String[])objectIdList.toArray(oidsArray);

                 StringList objectSelects = new StringList();
                 objectSelects.addElement(SELECT_TYPE);
                 objectSelects.addElement(SELECT_NAME);
                 objectSelects.addElement(SELECT_REVISION);
                 objectSelects.addElement(SELECT_ID);
                 objectSelects.addElement(ConfigurationConstants.SELECT_DESIGNVARIANTS);
                 objectSelects.addElement(ConfigurationConstants.SELECT_RULE_COMPLEXITY);
                 objectSelects.addElement("interface["+INTERFACE_FTRIntermediateObjectMigration +"]");

                 //Related to LE
                 objectSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].id");
                 //Related to RE
                 objectSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+ "].id");
                 objectSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+ "].to.id");
                 DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].id");
                 DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+ "].id");
                 DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+ "].to.id");
		         //DomainConstants.MULTI_VALUE_LIST.add(ConfigurationConstants.SELECT_DESIGNVARIANTS);


                 MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
                 Iterator itr = mapList.iterator();
                 Map ruleInfoMap = new HashMap();
                 String strRuleId = "";
                 //String strType = "";
                 //String strName = "";
                 //String strRevision = "";
                 //String revId = "";
                 String strInterfaceAdded ="";
                 boolean isAllRulesStamped = true;
                 while (itr.hasNext())
                 {
               	  //convert Rule objects
               	  ruleInfoMap = (Map) itr.next();
               	  //strType = (String)ruleInfoMap.get(SELECT_TYPE);
                  //strName = (String)ruleInfoMap.get(SELECT_NAME);
                  //strRevision = (String)ruleInfoMap.get(SELECT_REVISION);
                  strRuleId =  (String)ruleInfoMap.get(SELECT_ID);

                  mqlLogRequiredInformationWriter("Rule Id in Process                  :: "+ strRuleId +"\n\n");

                  strInterfaceAdded =  (String)ruleInfoMap.get("interface["+INTERFACE_FTRIntermediateObjectMigration +"]");

                     try{
                      boolean bRuleMigrated = updateRuleCache(context,ruleInfoMap);
       	        	  //Need to stamp the converted Rules with the migration stamp interface to indicate that it has been converted.
       	        	  if(bRuleMigrated){
                         if(strInterfaceAdded!=null && strInterfaceAdded.equalsIgnoreCase("FALSE")){
       	        			stampRuleObject(context,strRuleId);
       	        		  }
       	        	  }else{
       	        		  //To indicate that Rule is not stamped.
       	        		  isAllRulesStamped = false;
       	        	  }

       	        	  loadMigratedOids(strRuleId);
       	        	 mqlLogRequiredInformationWriter("Migration for Rule id :: "+ strRuleId + " is done."+"\n\n\n\n");

               	  }catch(Exception e)
               	  {
               		  e.printStackTrace();
               		  String strCommand = strRuleId + " : " + e.getMessage()+"\n";
       				  writeUnconvertedOID(strCommand,strRuleId);
               		  continue;
               	  }
                 }

                 if(isAllRulesStamped)
                 {
                  // E2W Commented out below to not stop rule migration mid way  	 
               	  //setAdminMigrationStatus(context,"RuleComplete");
                  mqlLogRequiredInformationWriter("!!!!!!!!!!!!!!                  Rule Migration for Ids in this file are Complete. \n");
                  mqlLogRequiredInformationWriter("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
               	  mqlLogRequiredInformationWriter("!!!!!!!!!!!!!!                  Rule Migration Complete. \n");
               	  mqlLogRequiredInformationWriter("\n");
                 }
                 else{

                	 mqlLogRequiredInformationWriter("Rule Migration Successful but not complete as still migration of some Rule objects is not done.\n");
                	 mqlLogRequiredInformationWriter("\n");
                 }

                 //turn triggers off
         		 strMqlCommand = "trigger on";
         		 MqlUtil.mqlCommand(context,strMqlCommand,true);
         }catch(Exception e)
         {
        	 e.printStackTrace();
        	 mqlLogRequiredInformationWriter("Rule Migration Failed. \n");
        	 mqlLogRequiredInformationWriter("\n");
        	 throw e;
         }
    }
          return;
      }


     /**
      * Recomputes the expression attributes, left and/or right expressions depending on the rule type.
      * @param context the eMatrix <code>Context</code> object
      * @param ruleInfoMap Map containing the rule data
      * @return true on success and false on failure
      * @throws Exception
      */
     private boolean updateRuleCache(Context context,Map ruleInfoMap) throws Exception
     {
    	 String strNewExpression = null;
    	 boolean bsucceed = true;

    	 String strRuleId = (String)ruleInfoMap.get(SELECT_ID);
    	 DomainObject domRuleObj = DomainObject.newInstance(context,strRuleId);
    	 String strRuleType = (String) ruleInfoMap.get(DomainConstants.SELECT_TYPE);
     	 String strDVAttrVal = (String) ruleInfoMap.get(ConfigurationConstants.SELECT_DESIGNVARIANTS);
     	 String strRuleComplexity = (String) ruleInfoMap.get(ConfigurationConstants.SELECT_RULE_COMPLEXITY);

         try{
    	 /* Depending upon the Rule Type Left and Right expression
    	  * attributes will be computed
    	  */
    	 StringList sLOfExpAttr = new StringList();

    	 if(strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)
    			 || strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)
    			 || strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE)
    			 || strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_INCLUSION_RULE)){

    		 sLOfExpAttr.add(ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION);
    		 sLOfExpAttr.add(ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);

    	 }else if(strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_QUANTITY_RULE)){

    		 sLOfExpAttr.add(ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION);
    	 }

    	 mqlLogRequiredInformationWriter("Rule Info                                 :: "+ ruleInfoMap + "\n");
    	 mqlLogRequiredInformationWriter("Type of Rule                              :: "+ strRuleType + "\n");
    	 mqlLogRequiredInformationWriter("Rule Complexity                           :: "+ strRuleComplexity + "\n");
    	 mqlLogRequiredInformationWriter("Design Variant Attribute Value            :: "+ strDVAttrVal + "\n");

    	 StringList RelationshipSelect = new StringList(ConfigurationConstants.SELECT_ID);
    	 RelationshipSelect.addElement("torel.id");
    	 RelationshipSelect.addElement("to.id");
    	 RelationshipSelect.addElement("torel.physicalid");
    	 RelationshipSelect.addElement("to.physicalid");
    	 RelationshipSelect.addElement(SELECT_PHYSICALID);

    	 RelationshipSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
    	 RelationshipSelect.addElement(SELECT_ATTRIBUTE_TOKEN);


    	 StringList sLRelationshipWhere = new StringList();
    	 sLRelationshipWhere.addElement("from.id" + "==" + strRuleId);


    	 //Compute the LE and RE attributes and set attribute values accordingly
    	 for(int iCntExpAttr=0;iCntExpAttr<sLOfExpAttr.size() ;iCntExpAttr++){

	    	//StringList of RE Rel( delete token AND,OR,NOT specific related rel ids ) to be deleted
			StringList sLRERelIdsToBeDeleted = new StringList();

    		String strExpAttr = (String)sLOfExpAttr.get(iCntExpAttr);

    		//Get the LE or RE Rel ids
    		StringList sLOfExp = new StringList();
    	    String ExpSelect = "from["+ strExpAttr+ "].id";

    	    if((StringList)ruleInfoMap.get(ExpSelect)!=null){
    	    	sLOfExp = (StringList)ruleInfoMap.get(ExpSelect);
    	    }

    	    mqlLogRequiredInformationWriter("Expression is                             :: "+ ExpSelect + "\n");
    	    mqlLogRequiredInformationWriter("Expression Value                          :: "+ sLOfExp + "\n");

			MapList mLOfExpToSideRelInfo = new MapList();

			for(int iCnt=0;iCnt<sLOfExp.size();iCnt++) {
				String strRelId = (String)sLOfExp.get(iCnt);

				//Get the Rel Id info
				DomainRelationship domRelId = new DomainRelationship(strRelId);
				Map mRelData = domRelId.getRelationshipData(context,RelationshipSelect);
				/*"Sequence Order" is stored in StringList format...
				  converted to String as this is used further in "addSortKey" method*/
				if(!((StringList)mRelData.get(SELECT_ATTRIBUTE_SEQUENCE_ORDER)).isEmpty()){
					mRelData.put("attribute["+ATTRIBUTE_SEQUENCE_ORDER +"]",
							    ((StringList)mRelData.get(SELECT_ATTRIBUTE_SEQUENCE_ORDER)).get(0));
   			  }

				mLOfExpToSideRelInfo.add(mRelData);
			}

    		 //Need to sort the Map on the basis of "Sequence Number" attribute value in ascending order
 			    StringBuffer strBuffer = new StringBuffer(200);
				strBuffer = strBuffer.append(STR_ATTRIBUTE)
							.append(OPEN_BRACE)
							.append(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER)
							.append(CLOSE_BRACE);

				mLOfExpToSideRelInfo.addSortKey(strBuffer.toString(), "ascending", "integer");
				mLOfExpToSideRelInfo.sort();

    		 StringBuffer strExpBuffer = new StringBuffer();

    		 mqlLogRequiredInformationWriter("Exp To Side Rel Info                      :: "+ mLOfExpToSideRelInfo + "\n");
    		 for(int m=0;m<mLOfExpToSideRelInfo.size();m++){

    			 Map mpInfo = new HashMap();
    			 mpInfo = (Map) mLOfExpToSideRelInfo.get(m);

    			 String strToken ="";
    			 String strPhysicalId  ="";
    			 String strToRelPhyId ="";
    			 String strToPhyId ="";
    			 String strRERelId ="";


    			 if(!((StringList)mpInfo.get(SELECT_ATTRIBUTE_TOKEN)).isEmpty()){
    				 strToken = (String)((StringList)mpInfo.get(SELECT_ATTRIBUTE_TOKEN)).get(0);
    				 strRERelId = (String)((StringList)mpInfo.get("id")).get(0);
    				 mqlLogRequiredInformationWriter("Existing RE Rel to be deleted for this Rule:: "+ strRERelId + "\n");
    				 sLRERelIdsToBeDeleted.add(strRERelId);
    			 }

    			 if(!((StringList)mpInfo.get("physicalid")).isEmpty()){
    				  strPhysicalId = (String)((StringList)mpInfo.get("physicalid")).get(0);
    			 }

    			 if(!((StringList)mpInfo.get("torel.physicalid")).isEmpty()){
    				strToRelPhyId = (String)((StringList)mpInfo.get("torel.physicalid")).get(0);
    			 }

    			 if(!((StringList)mpInfo.get("to.physicalid")).isEmpty()){
    				  strToPhyId = (String)((StringList)mpInfo.get("to.physicalid")).get(0);
    			 }

    			 mqlLogRequiredInformationWriter("Token value if any                        :: "+ strToken + "\n");
    			 mqlLogRequiredInformationWriter("Physical Id of LE/RE                      :: "+ strPhysicalId + "\n");
    			 mqlLogRequiredInformationWriter("Rel Physical Id of 'To side' of LE/RE Rel :: "+ strToRelPhyId + "\n");
    			 mqlLogRequiredInformationWriter("Obj Physical Id of 'To side' of LE/RE Rel :: "+ strToPhyId + "\n\n");

    			 //Append the AND,OR,(,),NOT
    			 if(strToken!=null && strToken.length()!=0){
    				 strExpBuffer = strExpBuffer.append(strToken).append(SYMB_SPACE);
    			 }else{
    				 //Add to the String for attribute
    				 if(strToRelPhyId!=null && strToRelPhyId.length()!=0) {
    					 strExpBuffer = strExpBuffer.append("R")
    					 .append(strToRelPhyId)
    					 .append(SYMB_SPACE);
    				 }else if(strToPhyId!=null && strToPhyId.length()!=0) {
    					 strExpBuffer = strExpBuffer.append("B")
    					 .append(strToPhyId)
    					 .append(SYMB_SPACE);
    				 }
    			 }
    		 }

    		 strNewExpression = strExpBuffer.toString();
    		 StringBuffer strBuf= new StringBuffer();

    		 if(strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_MARKETING_PREFERENCE)
    				 && strExpAttr.equalsIgnoreCase(ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION)){
    			 StringList slRtExpTokenised = FrameworkUtil.split(strNewExpression, SYMB_SPACE);
    			 for(int i=0;i<slRtExpTokenised.size();i++){
    				 String strElement = (String) slRtExpTokenised.get(i);
    				 if(!strElement.trim().isEmpty())
    				 strBuf.append(strElement).append(SYMB_SPACE).append("AND").append(SYMB_SPACE);
    			 }
    			 String strMPRRExpressionFinal = strBuf.toString();

    			 if (strMPRRExpressionFinal.endsWith(" AND ")) {
    				 int i = strMPRRExpressionFinal.lastIndexOf(" AND ");
    				 strNewExpression = strMPRRExpressionFinal.substring(0, i);
    			 }
    		 }
    		 mqlLogRequiredInformationWriter("Set attribute values on Rule Id start     :: "+"\n");
    		 mqlLogRequiredInformationWriter("Rule Attribute                            :: "+ strExpAttr + "\n");
    		 mqlLogRequiredInformationWriter("Value of Attribute                        :: "+ strNewExpression + "\n\n");
    		 domRuleObj.setAttributeValue(context, strExpAttr, strNewExpression);
    		 mqlLogRequiredInformationWriter("Set Attribute values on Rule Id done      :: "+"\n");

    		 //If Rule Type = Inclusion Rule then only update the below Attribute
  			if(strRuleType.equalsIgnoreCase(ConfigurationConstants.TYPE_INCLUSION_RULE)
      				 && strDVAttrVal!=null
      				 && !strDVAttrVal.equals("")
      				 && !strDVAttrVal.isEmpty()){

  				if(strRuleComplexity!=null && strRuleComplexity.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_SIMPLE)){
  					 mqlLogRequiredInformationWriter("Rule Complexity :: "+ strRuleComplexity + "\n");
  					 String strDVVal = "";
  	         		 StringBuffer sBDVPhyIds = new StringBuffer();
  	         		 StringTokenizer newValueTZ = new StringTokenizer(strDVAttrVal, ",");
  	     			 while(newValueTZ.hasMoreElements()) {
  	      				strDVVal = newValueTZ.nextToken();

  	      				DomainObject domDVId = new DomainObject(strDVVal);
  	      				String strDVPhyId ="";
  	     				if(domDVId.exists(context)){
  	     					strDVPhyId= domDVId.getInfo(context, SELECT_PHYSICALID);
  	     					sBDVPhyIds.append(strDVPhyId);
  	     					sBDVPhyIds.append(",");
  	     				}else{
  	     					mqlLogRequiredInformationWriter("\n\n\n"
  	     													+"This Design Variant doesn't exist now"
  	     													+ strDVVal
  	     													+"\n"
  	     													+"\n");
  	     				}
  	      			}
  	     			 if(sBDVPhyIds!=null && sBDVPhyIds.length()!=0){
  	     				 String strDVPhyId = sBDVPhyIds.toString().substring(0,sBDVPhyIds.length()-1);
  	     				 mqlLogRequiredInformationWriter("Attrubute 'Design Variant' value to be set as :: "+ strDVPhyId + "\n");
  	     				 domRuleObj.setAttributeValue(context,  ConfigurationConstants.ATTRIBUTE_DESIGNVARIANTS, strDVPhyId );
  	     			 }
  				}
      		 }

  			//To delete the RE rel
  			if(!sLRERelIdsToBeDeleted.isEmpty()){
  				mqlLogRequiredInformationWriter("List of Rel Ids to be deleted :: "+ sLRERelIdsToBeDeleted + "\n");
  	  			disconnectRel(context, sLRERelIdsToBeDeleted);
  	  		    mqlLogRequiredInformationWriter("Rel id's deletion done."  + "\n");
  			}

      	   }
          }catch(Exception e)
         {
        	 e.printStackTrace();
        	 bsucceed = false;
        	 throw new FrameworkException("reCompute Expression Attributes failed " + e.getMessage());
         }

    	 return bsucceed;
     }

      /**
       * Determines if the specified type exists in the database.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param typeName String containing the type to query
       * @return boolean true if the type of object exists, else returns false
       * @throws Exception if the operation fails
       */
      private boolean isTypeExists(Context context, String typeName) throws Exception
      {
          boolean typeExists = false;
          //String command = "temp query bus '" + typeName + "' * *";
          String command = "temp query bus $1 $2 $3";

          String result = MqlUtil.mqlCommand(context, command,typeName,"*","*");
          if (result != null && !result.isEmpty())
          {
              typeExists = true;
          }
          return typeExists;
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
              throw new Exception("not supported on desktop client");
          }

          writer.write("================================================================================================\n");
          writer.write(" Variant Configuration Intermediate Object Migration is a two step process  \n");
          writer.write(" Step1: Find all objects that need to be migrated and save them into flat files \n");
          writer.write(" Example: \n");
          writer.write(" execute program emxVariantConfigurationIntermediateObjectMigrationFindObjects 1000 C:/Temp/oids/; \n");
          writer.write(" First parameter  = indicates number of object per file \n");
          writer.write(" Second Parameter  = the directory where files should be written \n");
          writer.write(" \n");
          writer.write(" Step2: Migrate the objects \n");
          writer.write(" Example: \n");
          writer.write(" execute program emxVariantConfigurationIntermediateObjectMigration 'C:/Temp/oids/' 1 n ; \n");
          writer.write(" First parameter  = the directory to read the files from\n");
          writer.write(" Second Parameter = minimum range of file to start migrating  \n");
          writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
          writer.write("        - value of 'n' means all the files starting from mimimum range\n");
          writer.write("================================================================================================\n");
          writer.close();
      }

       public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

       public static String now() {
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
         return sdf.format(cal.getTime());

       }
       	public boolean isOfDerivationChangedType(Context context, String topLevelObjectType) throws Exception{
    	   //loadResourceFile(context, "emxConfigurationMigration");
		   boolean isOfDerivationChangedType= false;
    	   //String strTypeDerivationChange = getResourceProperty(context,"emxConfiguration.Migration.TypeDerivationChanged");
		   //String strTypeDerivationChange ="type_SoftwareFeature";
		   String strTypeDerivationChange = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.TypeDerivationChanged");

    	   StringList slTypeDerivationTokenised=FrameworkUtil.split(strTypeDerivationChange.trim(), ",");
    	   StringList slTypes=new StringList();
    	   for(int i=0;i<slTypeDerivationTokenised.size();i++){
    		   slTypes.add(getSchemaProperty(context, slTypeDerivationTokenised.get(i).toString()));
    	   }
    	   for(int i=0;i<slTypes.size();i++){

    		   if(mxType.isOfParentType(context, topLevelObjectType,slTypes.get(i).toString())){

    			   isOfDerivationChangedType=true;
    			   break;
    		   }
    	   }

    	   return isOfDerivationChangedType;
       }
       public StringList getDerivationChangedType(Context context,String [] args) throws Exception{
    	   //loadResourceFile(context, "emxConfigurationMigration");
		   StringList slTypes=new StringList();
    	   try{

         	   //String strTypeDerivationChange = getResourceProperty(context,"emxConfiguration.Migration.TypeDerivationChanged");
    		   String strTypeDerivationChange = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.TypeDerivationChanged");

			   //String strTypeDerivationChange = "type_SoftwareFeature";

         	   StringList slTypeDerivationTokenised=FrameworkUtil.split(strTypeDerivationChange.trim(), ",");
         	   mqlLogRequiredInformationWriter("Value of setting 'emxConfiguration.Migration.TypeDerivationChanged' in Property file is::"
         			  						  +"\n"
         			  						  +slTypeDerivationTokenised);
         	   mqlLogRequiredInformationWriter("\n");

         	   for(int i=0;i<slTypeDerivationTokenised.size();i++){

         		   slTypes.add(getSchemaProperty(context, slTypeDerivationTokenised.get(i).toString()));
         	   }
    	   }catch (Exception e) {

    		   e.printStackTrace();
		}

     	   return slTypes;
        }
       /**
        * Stamps the Feature objects to the new appropriate Object Type
        * Also computes if the Feature is in "Mixed Composition" or "Mixed Usage"
        * If yes then Feature is stamped as "Conflict".
        * @param context the eMatrix <code>Context</code> object
        * @param args String array containing program arguments
        * @throws Exception
        */
  public void preMigration(Context context , String[]args) throws Exception
  {
	  try {
		  mqlLogRequiredInformationWriter("\n\n");
		  mqlLogRequiredInformationWriter("Inside PreMigration method to stamp the ids ----->"+ "\n");
		  //loadResourceFile(context, "emxConfigurationMigration");
		  mqlLogRequiredInformationWriter("Resource file loaded ------> emxConfigurationMigration.properties");
		  mqlLogRequiredInformationWriter("\n\n");

		  String strMqlCommandOff = "trigger off";
		  MqlUtil.mqlCommand(context,strMqlCommandOff,true);
		  mqlLogRequiredInformationWriter(" 'trigger off' done at the start of preMigration " + "\n");

		  IS_CONFLICT = false;
		  StringBuffer sbRelPattern = new StringBuffer(50);
		  sbRelPattern.append(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM);
		  sbRelPattern.append(",");
		  sbRelPattern.append(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO);
		  sbRelPattern.append(",");
		  sbRelPattern.append(ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM);

		  StringBuffer sbTypePattern1 = new StringBuffer(50);
		  sbTypePattern1.append(ConfigurationConstants.TYPE_FEATURES);
		  sbTypePattern1.append(",");
		  sbTypePattern1.append(ConfigurationConstants.TYPE_FEATURE_LIST);
		  sbTypePattern1.append(",");
		  sbTypePattern1.append(ConfigurationConstants.TYPE_PRODUCTS);

		  emxVariantConfigurationIntermediateObjectMigration_mxJPO jpoInst= new emxVariantConfigurationIntermediateObjectMigration_mxJPO(context, new String[0]);
		  StringList slDerivationChangedType1 = jpoInst.getDerivationChangedType(context,new String[0]);
		  for(int i=0;i<slDerivationChangedType1.size();i++){
			  sbTypePattern1.append(",");
			  sbTypePattern1.append(slDerivationChangedType1.get(i));
            }

		  StringList fsObjSelects = new StringList();
		  fsObjSelects.addElement(SELECT_ID);
		  fsObjSelects.addElement(SELECT_TYPE);
		  fsObjSelects.addElement(SELECT_NAME);
		  fsObjSelects.addElement(SELECT_REVISION);
		  fsObjSelects.addElement(SELECT_LEVEL);
		  fsObjSelects.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE + "]");
		  fsObjSelects.addElement("attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT + "]");
		  fsObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.type");
		  fsObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
		  fsObjSelects.addElement("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].from.attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE + "]");

		  //Added to check for GBOM connection if any
		  fsObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id");
		  fsObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.from["+ConfigurationConstants.RELATIONSHIP_GBOM_TO+"].to.id");


		  StringBuffer sbRelPattern1 = new StringBuffer(50);
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_GBOM);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_GBOM_FROM);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_VARIES_BY);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(RELATIONSHIP_MANAGED_SERIES);
		  sbRelPattern1.append(",");
		  sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_QUANTITY_RULE);

		  StringBuffer sbTypePattern2 = new StringBuffer(50);
		  sbTypePattern2.append(ConfigurationConstants.TYPE_GBOM);
		  sbTypePattern2.append(",");
		  sbTypePattern2.append(ConfigurationConstants.TYPE_FEATURES);
		  sbTypePattern2.append(",");
		  sbTypePattern2.append(ConfigurationConstants.TYPE_QUANTITY_RULE);
		  sbTypePattern2.append(",");
		  sbTypePattern2.append(ConfigurationConstants.TYPE_MASTER_FEATURE);


		  warningLog = new FileWriter(documentDirectory + "migration.log", true);


		  documentDirectory = args[0];
		  String fileSeparator = java.io.File.separator;
		  if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator)){
			  documentDirectory = documentDirectory + fileSeparator;
		  }


		  if(args.length > 3){

			  RESOLVE_CONFLICTS = args[3];
			  mqlLogRequiredInformationWriter( "\n"
					  						  + "\n"
					  						  + "\n"
					  						  + "This step will also resolve the conflicts related "
                      						  + "\n"
                      						  + "to 'Mixed Composition' and 'Malformed GBOM/Feature List' Object ids"
                      						  + "\n"
                      						  + "at the end of the step, "
                      						  + " if present."
                      						  + "\n"
					  						  + "\n"
                      						  + "NOTE : User has to resolve the conflicts related "
					                          + "\n"
					  						  + "to 'Mixed Usage' and 'Incorrect Mapping' related Object ids"
					                          + "\n"
					  						  + "at the end of the step, "
					  						  + " if present."
                      						  + "\n"
                      						  + "\n"
                      						  + "\n");

		  }else{

			  mqlLogRequiredInformationWriter( "\n"
						  					 + "\n"
						  					 + "\n"
						  					 + "This step will resolve the conflicts related "
                    						 + "\n"
                    						 + "to 'Malformed GBOM/Feature List' Object ids"
                    						 + "\n"
                    						 + "at the end of the step, "
                     						 + " if present."
                    						 + "\n"
                    						 + "\n"
						  					 + "NOTE : User has to resolve the conflicts related "
					                         + "\n"
					  						 + "to 'Mixed Composition','Mixed Usage' and 'Incorrect Mapping' related Object ids"
					                         + "\n"
					  						 + "at the end of the step, "
					  						 + " if present."
											 +"\n"
											 +"\n"
											 +"\n");
		  }

			  String strMinRange = args[1].toString();
			  minRange = Integer.parseInt(strMinRange);
			  if ("n".equalsIgnoreCase(args[2])){
				  maxRange = getTotalFilesInDirectory();
			  } else {
				  maxRange = Integer.parseInt(args[2]);
			  }

			  // Execute schema changes needed for migration
			  //we should only call this once the first time the Find Objects Pre Migration is called.
			  //If In process of PreMigration, do not call again
			  int migrationStatus = getAdminMigrationStatus(context);
			  mqlLogRequiredInformationWriter("If Migration Status is -------> 0 "
					  						  +"\n"
					  						  + "'VariantConfigurationR212SchemaChangesForMigration.tcl' will be executed."+"\n\n");

			  mqlLogRequiredInformationWriter("If Migration Status is ------->'PreMigrationInProcess' "
					  						  +"\n"
					  						  + "'VariantConfigurationR212SchemaChangesForMigration.tcl' will not be executed"
			  								  +"\n"
			  								  + "as this TCL is already executed"+"\n\n");

			  mqlLogRequiredInformationWriter("If Migration Status is ------->'PreMigrationComplete' "
					  						  +"\n"
					  						  + "Pre Migration step is done."
					  						  +"\n"
					  						  + "Please execute the next step to continue with Migration."
					  						  +"\n\n");

			  mqlLogRequiredInformationWriter("Now the Migration Status is "+ migrationStatus +"\n\n");

			  if(migrationStatus == 0){
				  mqlLogRequiredInformationWriter("'VariantConfigurationR212SchemaChangesForMigration.tcl' execution started..." + "\n\n");
				  String cmdString = "execute program VariantConfigurationR212SchemaChangesForMigration.tcl";
				  ContextUtil.pushContext(context);
				  MqlUtil.mqlCommand(context, cmdString);

				  //${CLASS:emxAdminCache}.reloadSymbolicName(context, "type_ConfigurationFeature");

				  TYPE_CONFIGURATION_FEATURE = getSchemaProperty(context, "type_ConfigurationFeature");

				  ContextUtil.popContext(context);

				  mqlLogRequiredInformationWriter("\n\n");
				  mqlLogRequiredInformationWriter("'VariantConfigurationR212SchemaChangesForMigration.tcl' execution is done..."+ "\n");
			  }
			  //------------------------------------------------
			  //Now to read the files
			  //------------------------------------------------
			  int m = 0;
			  for( m = minRange;m <= maxRange; m++){
				  StringList objectList = new StringList();
				  try{
					  mqlLogRequiredInformationWriter("\n");
					  mqlLogRequiredInformationWriter("Pre-migration in process for objects in objectids_"+ m +".txt file. \n\n");

					  //setting admin property MigrationR212VariantConfiguration in eServiceSystemInformation.tcl
					  setAdminMigrationStatus(context,"PreMigrationInProcess");
					  mqlLogRequiredInformationWriter("Migration Status set to value ----->"+ "'PreMigrationInProcess'" +"\n\n");

					  objectList = readFiles(m);
					  mqlLogRequiredInformationWriter("objectids_"+ m +".txt file traversal started " + "\n\n");
					  for(int iCntObjList=0 ;iCntObjList<objectList.size();iCntObjList++){
						  String topLevelObjectId = (String)objectList.get(iCntObjList);

						  String topLevelObjectType ="";
						  DomainObject domTopLevelObject = DomainObject.newInstance(context, topLevelObjectId);

						  //mqlLogRequiredInformationWriter("topLevelObjectType------------"+ topLevelObjectType + "\n");
						  //topLevelObjectType = domTopLevelObject.getInfo(context, ConfigurationConstants.SELECT_TYPE);
						  Map htTopLevelObjData = (Map) domTopLevelObject.getInfo(context, fsObjSelects);
						  topLevelObjectType = (String)htTopLevelObjData.get(ConfigurationConstants.SELECT_TYPE);
						  String topLevelObjectName= (String)htTopLevelObjData.get(ConfigurationConstants.SELECT_NAME);
						  String topLevelObjectRevision = (String)htTopLevelObjData.get(ConfigurationConstants.SELECT_REVISION);
						  Integer iCnt;
						  iCnt= iCntObjList+1;

						  mqlLogRequiredInformationWriter("------------------------------------------------------------------------------------------------------------------------\n");
						  mqlLogRequiredInformationWriter("Stamping of objects for below Top level object's structure started----->"+" --- "+now()+"\n");
						  mqlLogRequiredInformationWriter("Object Count in text file ----->"+iCnt+"\n");
						  mqlLogRequiredInformationWriter("Top level object Id ----------->"+topLevelObjectId+"\n");
						  mqlLogRequiredInformationWriter("Top level object Type --------->"+topLevelObjectType+"\n");
						  mqlLogRequiredInformationWriter("Top level object Name --------->"+topLevelObjectName+"\n");
						  mqlLogRequiredInformationWriter("Top level object Revision ----->"+topLevelObjectRevision+"\n");

						  //mqlLogRequiredInformationWriter("------------------------------------------------------------\n");
						  //Check if "GBOM" object is connected to Configuration Feature or Configuration Option
						 //DomainObject domFeatObj = new DomainObject(strFeatureId);
						   StringList TopLevelfeatureObjSelects = new StringList();
						   TopLevelfeatureObjSelects.addElement(SELECT_TYPE);
						   TopLevelfeatureObjSelects.addElement(SELECT_NAME);
						   TopLevelfeatureObjSelects.addElement(SELECT_REVISION);
						   TopLevelfeatureObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id");

						   DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id");
						   DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id");

						  //topLevelObjectType = domTopLevelObject.getInfo(context, ConfigurationConstants.SELECT_TYPE);
						   topLevelObjectType = (String)htTopLevelObjData.get(SELECT_TYPE);

							  StringList sLTopLevelGBOMIds = new StringList();

							  String TopLevelGBOMSelect = "from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id";
							  Object objTopLevelGBOMId = htTopLevelObjData.get(TopLevelGBOMSelect);

							  if (objTopLevelGBOMId instanceof StringList) {
								  sLTopLevelGBOMIds = (StringList) htTopLevelObjData.get(TopLevelGBOMSelect);
							  } else if (objTopLevelGBOMId instanceof String) {
								  sLTopLevelGBOMIds.addElement((String) htTopLevelObjData.get(TopLevelGBOMSelect));
							  }

						   emxVariantConfigurationIntermediateObjectMigration_mxJPO VariantConfigInterObjMigrationInst= new emxVariantConfigurationIntermediateObjectMigration_mxJPO(context, new String[0]);
						   StringList slDerivationChangedType = VariantConfigInterObjMigrationInst.getDerivationChangedType(context,new String[0]);


	    StringBuffer sbWhereClause = new StringBuffer(50);
		sbWhereClause.append("((");
		sbWhereClause.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+ "].from.type.kindof != \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
		sbWhereClause.append(")||(");
		sbWhereClause.append("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+ "].from.type.kindof != \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
		sbWhereClause.append("))||");
		sbWhereClause.append("((");
		sbWhereClause.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+ "].from.type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
		sbWhereClause.append("&&");
		sbWhereClause.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+ "].from.id == "+topLevelObjectId +"");
		sbWhereClause.append(")||(");
		sbWhereClause.append("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+ "].from.type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
		sbWhereClause.append("&&");
		sbWhereClause.append("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+ "].from.id == "+topLevelObjectId +"");
		sbWhereClause.append(")");
		sbWhereClause.append(")");

							StringBuffer sbObjWhere = new StringBuffer(50);
							sbObjWhere.append("(");
							sbObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_FEATURES + "\"");
							for(int i=0;i<slDerivationChangedType.size();i++){
								sbObjWhere.append("||");
				    			sbObjWhere.append("type == \"" + slDerivationChangedType.get(i).toString() + "\"");
				            }
							sbObjWhere.append(")");

							sbObjWhere.append("||");
							sbObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
							sbObjWhere.append("||");

							sbObjWhere.append("(");
							sbObjWhere.append("type == \"" + ConfigurationConstants.TYPE_FEATURE_LIST + "\"");
							sbObjWhere.append("&&");
							sbObjWhere.append(sbWhereClause);
							sbObjWhere.append(")");


							DomainConstants.MULTI_VALUE_LIST.add("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id");

						  //mqlLogRequiredInformationWriter("------------------------------------------------------------\n");
						  mqlLogRequiredInformationWriter("Query to get the Top level object structure started ------------>"+" --- "+ now() +"\n");

						  MapList featureStructureList = domTopLevelObject.getRelatedObjects(context,
								  sbRelPattern.toString(), // relationshipPattern
								  sbTypePattern1.toString(), // typePattern
								  fsObjSelects, // objectSelects
								  null, // relationshipSelects
								  false, // getTo
								  true, // getFrom
								  (short) 0, // recurseToLevel
								  sbObjWhere.toString(), // objectWhere,
								  null, // relationshipWhere
								  (int)0); // limit

						  DomainConstants.MULTI_VALUE_LIST.remove("from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id");
						  mqlLogRequiredInformationWriter("Query to get the Top level object structure ended   ------------>"+" --- "+ now() +"\n");
						  //mqlLogRequiredInformationWriter("------------------------------------------------------------\n\n\n");

						  String strNewFeatureType = "";
						  String strResolvedNewFeatureType= "";

						  // use case for standalone  features
						  if (featureStructureList.size() == 0){

						  //mqlLogRequiredInformationWriter("------------------------------------------------------------\n");
					      mqlLogRequiredInformationWriter(" This Top level object structure doesn't have 'Feature List From' and 'Feature List To' connections "+"\n");
					      //mqlLogRequiredInformationWriter(" Check if this feature has 'Design Variants'or 'GBOM' or 'Managed Series' or 'Quantity Rules' connected "+"\n");
					      //mqlLogRequiredInformationWriter(" If 'Yes' then stamp this Feature as 'type_LogicalFeature'"+"\n");
					      //mqlLogRequiredInformationWriter(" If 'No' then check for the  property setting for this Feature "+"\n");
					      //mqlLogRequiredInformationWriter(" If property setting not found for the given type..Check for Parent Type and then set the Type "+"\n\n\n");


					      mqlLogRequiredInformationWriter("Query to check for 'Design Variants' or 'GBOM' or 'Managed Series' or 'Quantity Rules' connections started ------------>"+" --- "+ now() +"\n");
						  MapList LF_RelatedList = domTopLevelObject.getRelatedObjects(context,
									  sbRelPattern1.toString(), // relationshipPattern
									  sbTypePattern2.toString(), // typePattern
									  null, // objectSelects
									  null, // relationshipSelects
									  true, // getTo
									  true, // getFrom
									  (short) 1, // recurseToLevel
									  null, // objectWhere,
									  null, // relationshipWhere
									  (int) 0); // limit
						  mqlLogRequiredInformationWriter("Query to check for 'Design Variants' or 'GBOM' or 'Managed Series' or 'Quantity Rules' connections ended   ------------>"+" --- "+ now() +"\n");


							  // if feature has DVs or GBOM or Managed Series or Quantity Rules, it should be stamped as Logical
							  if (!LF_RelatedList.isEmpty()) {

								  mqlLogRequiredInformationWriter("This Top level object has 'Design Variants' or 'GBOM' or 'Managed Series' or 'Quantity Rules' connections'"+"\n");
								  mqlLogRequiredInformationWriter("Hence stamp this Feature as attribute ---> NewFeatureType=type_LogicalFeature "+"\n");
								  //strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration.TechnicalFeatureType");
								  strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.TechnicalFeatureType");
								  stampFeature(context,topLevelObjectId,strNewFeatureType,"",EMPTY_STRINGLIST);

							  } else {
								  mqlLogRequiredInformationWriter("This Top level object doesn't have the 'Design Variants' or 'GBOM' or 'Managed Series' or 'Quantity Rules' connections'"+"\n");
								  if(topLevelObjectType!=null
										  && !topLevelObjectType.equalsIgnoreCase(ConfigurationConstants.TYPE_PRODUCT_LINE)
										  && !topLevelObjectType.equalsIgnoreCase(ConfigurationConstants.TYPE_GBOM)
										  && !mxType.isOfParentType(context, topLevelObjectType,ConfigurationConstants.TYPE_PRODUCTS)){
										  //&& !isOfDerivationChangedType(context,topLevelObjectType)){

									  String strSymbolicType = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, topLevelObjectType, true);
									  try{
										  //strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration."+strSymbolicType);
										  strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration."+strSymbolicType);
									  }catch (Exception e){
										  //If property setting not found for the given type..Check for Parent Type and then set the Type
										  String strParentType = UICache.getParentTypeName(context,topLevelObjectType);
										  String strParSymbolicType = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strParentType, true);
										  //strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration."+strParSymbolicType);
										  strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration."+strParSymbolicType);
									  }
									  // stamping the features
									  stampFeature(context,topLevelObjectId,strNewFeatureType,"",EMPTY_STRINGLIST);
								  }
							  }
						  }else{ // usecase for structures

							  boolean isMixedUsage = false;
							  boolean isMixedComposition = false;
							  String referenceContextUsage="";

							  StringList featureTypesList = new StringList();
							  // if toplevelobject is feature
							  mqlLogRequiredInformationWriter("Top level object is of Type :: "+ topLevelObjectType +"\n");

							  if (mxType.isOfParentType(context, topLevelObjectType,ConfigurationConstants.TYPE_FEATURES)
									  || isOfDerivationChangedType(context, topLevelObjectType)){
								  // check for mixed usage
								  mqlLogRequiredInformationWriter("	Code to check Mixed Usage starts "+ "\n");

								  StringList allFeatureTypesList = new StringList();
								 
								  mqlLogRequiredInformationWriter("	List of Features where this Toplevel Object is used. "+ featureStructureList.size() +"\n");
								  outer: for (int j = 0; j < featureStructureList.size(); j++){

									  Map fsMap = new HashMap();

									  fsMap = (Map) featureStructureList.get(j);

									  String strObjectType = (String) fsMap.get(SELECT_TYPE);
									  referenceContextUsage = (String) fsMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]");

									  if(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_FEATURES)
											  || mxType.isOfParentType(context,strObjectType, ConfigurationConstants.TYPE_PRODUCTS)
											  || isOfDerivationChangedType(context, strObjectType)){

										  mqlLogRequiredInformationWriter("	Object Type. "+ strObjectType +"\n");

										  String featureTypeSelect = "to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].from.attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]";
										  Object objFeatureTypes = fsMap.get(featureTypeSelect);
										  featureTypesList.clear();

										  if (objFeatureTypes instanceof StringList) {
											  featureTypesList = (StringList) fsMap.get(featureTypeSelect);
										  } else if (objFeatureTypes instanceof String) {
											  featureTypesList.addElement((String) fsMap.get(featureTypeSelect));
										  }

										  mqlLogRequiredInformationWriter("	List of 'Feature Type' attribute values of connected 'Feature List' objects. "+ featureTypesList +"\n");

										  if (featureTypesList.size() > 0){

											  for (int k = 0; k < featureTypesList.size(); k++){
												  String strFeatureType = (String) featureTypesList.get(k);
												  if (!strFeatureType.equals((String) featureTypesList.get(0))){
													  mqlLogRequiredInformationWriter("As the above list is not consisitent Feature will be marked as 'MixedUsage'."+"\n");
													  isMixedUsage = true;
													  strNewFeatureType = "MixedUsage";
													  break outer;

												  }
											  }
										  }
									  }

									  mqlLogRequiredInformationWriter("	Code to check Mixed Usage ends "+ "\n\n");

									  mqlLogRequiredInformationWriter("	Code to check Mixed Composition starts "+ "\n");

									  // check for mixed composition
									  if (!isMixedUsage) {
										  if (mxType.isOfParentType(context,strObjectType, ConfigurationConstants.TYPE_FEATURE_LIST)) {

											  String strFeatureType = (String) fsMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]");
											  allFeatureTypesList.addElement(strFeatureType);

											  mqlLogRequiredInformationWriter("		List of 'Feature Type' attribute values of connected 'Feature List' objects. "+ allFeatureTypesList +"\n");

											  if(!strFeatureType.equals(allFeatureTypesList.get(0))) {
												  mqlLogRequiredInformationWriter("As the above list is not consisitent Feature will be marked as 'MixedComposition'."+"\n");
												  isMixedComposition = true;
												  strNewFeatureType = "MixedComposition";
												  break outer;
											  }
										  }
									  }

									  mqlLogRequiredInformationWriter("	Code to check Mixed Composition ends "+ "\n\n");

									  //Code to check the "Feature List" attribute "Feature Type" is correctly mapped against the Type
									  //Check for incorrect mapping
									  mqlLogRequiredInformationWriter("	Code to check  Incorrect Mapping starts "+ "\n");
										if (!isMixedUsage
												&& !isMixedComposition
												&& !mxType
														.isOfParentType(
																context,
																strObjectType,
																ConfigurationConstants.TYPE_FEATURES)
												&& !mxType
														.isOfParentType(
																context,
																strObjectType,
																ConfigurationConstants.TYPE_FEATURE_LIST)
												&& !mxType
														.isOfParentType(
																context,
																strObjectType,
																ConfigurationConstants.TYPE_PRODUCTS)) {

										  //Check for incorrect mapping
										  String strFeatureType = (String) fsMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]");
										  if (strFeatureType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_TECHNICAL)) {

											  if(!mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)){
												  strNewFeatureType = "IncorrectMapping";
												  break outer;
											  }

										  }else if(strFeatureType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MARKETING)) {

											  if(!mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)){
												  strNewFeatureType = "IncorrectMapping";
												  break outer;
											  }
										  }

									  }

										mqlLogRequiredInformationWriter("	Code to check Incorrect Mapping ends "+ "\n\n");
								  }

								  // stamp the structure including topLevelObject.

								  mqlLogRequiredInformationWriter("Stamp starts "+ "\n");

								  for (int j = 0; j < featureStructureList.size(); j++) {
									  Map fsMap = (Map) featureStructureList.get(j);

									  StringList sLGBOMIds = new StringList();

									  String GBOMSelect = "from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id";
									  Object objGBOMType = fsMap.get(GBOMSelect);

									  if (objGBOMType instanceof StringList) {
										  sLGBOMIds = (StringList) fsMap.get(GBOMSelect);
									  } else if (objGBOMType instanceof String) {
										  sLGBOMIds.addElement((String) fsMap.get(GBOMSelect));
									  }

									  String strObjectId = (String) fsMap.get(SELECT_ID);
									  String strObjectType = (String) fsMap.get(SELECT_TYPE);
									  boolean isLeafLevel = false;

									  if(mxType.isOfParentType(context,strObjectType,ConfigurationConstants.TYPE_FEATURES)){
										  int subFeatureCount = Integer.parseInt((String) fsMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT + "]"));
										  if(subFeatureCount==0)
											  isLeafLevel = true;
									  }

									  if(!isMixedUsage && !isMixedComposition){
										  //strNewFeatureType = getNewFeatureType(context,(String)featureTypesList.get(0),isLeafLevel,strObjectType);
										  strNewFeatureType = getNewFeatureType(context,(String)allFeatureTypesList.get(0),isLeafLevel,strObjectType);
									  }else if(isMixedComposition){
										  //Need to get the NewResolvedFeatureType
										  mqlLogRequiredInformationWriter("Top Level Feature Type.."+referenceContextUsage);
										  mqlLogRequiredInformationWriter("Is this feature LeafLevel.."+isLeafLevel);
										  mqlLogRequiredInformationWriter("Type of Object.."+strObjectType);

										  strResolvedNewFeatureType = getNewFeatureType(context,referenceContextUsage,isLeafLevel,strObjectType);
									  }

									    if (mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_FEATURES) ||
											  mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS)||
											  isOfDerivationChangedType(context, strObjectType)){

										  stampFeature(context,strObjectId,strNewFeatureType,strResolvedNewFeatureType,sLGBOMIds);
									  }
								  }

								  //toplevel object stamping
								  //strNewFeatureType = getNewFeatureType(context,(String)featureTypesList.get(0),false,topLevelObjectType);
								  strNewFeatureType = getNewFeatureType(context,(String)allFeatureTypesList.get(0),false,topLevelObjectType);
								  stampFeature(context,topLevelObjectId,strNewFeatureType,strResolvedNewFeatureType,sLTopLevelGBOMIds);
                                  mqlLogRequiredInformationWriter("Stamp ends "+ "\n");

							  }//end if toplevelobject is feature
							  // if top level object is Product or ProductLine
							  if (mxType.isOfParentType(context, topLevelObjectType,ConfigurationConstants.TYPE_PRODUCTS)
									  || mxType.isOfParentType(context,topLevelObjectType, ConfigurationConstants.TYPE_PRODUCT_LINE)) {

								  Map fsMap=new HashMap();
								  
								  for (int j = 0; j < featureStructureList.size(); j++) {

									  fsMap= (Map) featureStructureList.get(j);

									  int level = Integer.parseInt((String) fsMap.get(SELECT_LEVEL));
									  if (level == 1) {
										  String referenceContextUsage2 = (String)fsMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]");

										  // check for substructure mixed usage
										  StringList allFeatureTypesList = new StringList();
										  outer2:for (int k = j; k < featureStructureList.size(); k++) {
											  Map subFSMap = (Map) featureStructureList.get(k);

											  String strObjectType = (String) subFSMap.get(SELECT_TYPE);

											  int currentLevel = Integer.parseInt((String) subFSMap.get(SELECT_LEVEL));
											  if (currentLevel == 1 && k > j) {
												  break;
											  }
											  featureTypesList.clear();
											  if (mxType.isOfParentType(context,strObjectType, ConfigurationConstants.TYPE_FEATURES)
													  || mxType.isOfParentType(context,strObjectType,ConfigurationConstants.TYPE_PRODUCTS)
													  ||isOfDerivationChangedType(context, strObjectType)) {

												  String featureTypeSelect = "to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].from.attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]";
												  Object objFeatureTypes = subFSMap.get(featureTypeSelect);
												  if (objFeatureTypes instanceof StringList) {
													  featureTypesList = (StringList) subFSMap.get(featureTypeSelect);
												  } else if (objFeatureTypes instanceof String) {
													  featureTypesList.addElement((String) subFSMap.get(featureTypeSelect));
												  }
												  objFeatureTypes=null;
												  if (featureTypesList.size() > 0) {
													  for (int l = 0; l < featureTypesList.size(); l++) {
														  String strFeatureType = (String) featureTypesList.get(l);
														  if (!strFeatureType.equals((String) featureTypesList.get(0))) {

															  isMixedUsage = true;
															  strNewFeatureType = "MixedUsage";
															  break outer2;
														  }
													  }
												  }
											  }
											  if(!isMixedUsage){
												  if (mxType.isOfParentType(context,strObjectType,ConfigurationConstants.TYPE_FEATURE_LIST)) {
													  String strFeatureType = (String) subFSMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]");
													  allFeatureTypesList.addElement(strFeatureType);

													  if (!strFeatureType.equals(allFeatureTypesList.get(0))) {
														  isMixedComposition = true;
														  strNewFeatureType = "MixedComposition";
														  break;
												    }
        									     }
											  }
											  subFSMap=null;
										  }
										  allFeatureTypesList=null;
										  // stamp
										  mqlLogRequiredInformationWriter("\nstamp started -----> "+now()+"\n\n");
										  String strObjectType="";
										  String strObjectId ="";

										  for (int k = j + 1; k < featureStructureList.size(); k++) {
											  Map subFSMap= (Map) featureStructureList.get(k);

											  strObjectType = (String) subFSMap.get(SELECT_TYPE);
											  strObjectId = (String) subFSMap.get(SELECT_ID);

											  StringList sLGBOMIds = new StringList();
											  String GBOMSelect = "from["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].to.id";
											  Object objGBOMType = subFSMap.get(GBOMSelect);

											  if (objGBOMType instanceof StringList) {
												  sLGBOMIds = (StringList) subFSMap.get(GBOMSelect);
											  } else if (objGBOMType instanceof String) {
												  sLGBOMIds.addElement((String) subFSMap.get(GBOMSelect));
											  }

											  int currentLevel = Integer.parseInt((String) subFSMap.get(SELECT_LEVEL));
											  if (currentLevel == 1) {
												  //mqlLogRequiredInformationWriter("\ncurrentLevel == 1\n");
												  //j=k;
												  break;
											  }

											  if ((mxType.isOfParentType(context,strObjectType, ConfigurationConstants.TYPE_FEATURES)
          										   || mxType.isOfParentType(context,strObjectType, ConfigurationConstants.TYPE_PRODUCTS)
										           || isOfDerivationChangedType(context, strObjectType)))
											        //)&&
        											//!isOfDerivationChangedType(context, strObjectType)) {
          										     {

												  boolean isLeafLevel = false;
												  if(mxType.isOfParentType(context,strObjectType,ConfigurationConstants.TYPE_FEATURES)
														  && currentLevel != 2){
													  int subFeatureCount = Integer.parseInt((String) subFSMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT + "]"));
													  if(subFeatureCount==0)
														  isLeafLevel = true;
												  }
												  if(!isMixedUsage && !isMixedComposition &&
												  !mxType
														.isOfParentType(
																context,
																strObjectType,
																ConfigurationConstants.TYPE_FEATURES)
												&& !mxType
														.isOfParentType(
																context,
																strObjectType,
																ConfigurationConstants.TYPE_FEATURE_LIST)
												&& !mxType
														.isOfParentType(
																context,
																strObjectType,
																ConfigurationConstants.TYPE_PRODUCTS)){

													  //Code to check the "Feature List" attribute "Feature Type" is correctly mapped against the Type
														  //Check for incorrect mapping
														  String strFeatureType = (String) fsMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+ "]");
														  if (strFeatureType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_TECHNICAL)) {

															  if(!(mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)||
																	  mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_PRODUCTS))){
																  strNewFeatureType = "IncorrectMapping";
															  }else{
																  strNewFeatureType = getNewFeatureType(context,referenceContextUsage2,isLeafLevel,strObjectType);
															  }

														  }else if(strFeatureType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MARKETING)) {

															  if(!mxType.isOfParentType(context, strObjectType,ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)){
																  strNewFeatureType = "IncorrectMapping";
															  }else{
																  strNewFeatureType = getNewFeatureType(context,referenceContextUsage2,isLeafLevel,strObjectType);
															  }
														  }
												  }else if(!isMixedUsage && !isMixedComposition && (mxType.isOfParentType(context,strObjectType,ConfigurationConstants.TYPE_FEATURES)
												  ||mxType.isOfParentType(context,strObjectType,ConfigurationConstants.TYPE_PRODUCTS))){

												      strNewFeatureType = getNewFeatureType(context,referenceContextUsage2,isLeafLevel,strObjectType);
												  }else if(isMixedComposition){

													  //Need to get the NewResolvedFeatureType
													//Need to get the NewResolvedFeatureType
													  mqlLogRequiredInformationWriter("Top Level Feature Type.."+referenceContextUsage);
													  mqlLogRequiredInformationWriter("Is this feature LeafLevel.."+isLeafLevel);
													  mqlLogRequiredInformationWriter("Type of Object.."+strObjectType);
													  strResolvedNewFeatureType = getNewFeatureType(context,referenceContextUsage2,isLeafLevel,strObjectType);
												  }

												  stampFeature(context,strObjectId,strNewFeatureType,strResolvedNewFeatureType,sLGBOMIds);
											  }
											  subFSMap=null;
											  strObjectType=null;
											  strObjectId=null;
										  }
										  //System.gc();
										  mqlLogRequiredInformationWriter("\nstamp END     -----> "+now()+"\n");
									  }
								  }
								  featureTypesList=null;
							  }
						  }
						  mqlLogRequiredInformationWriter("------------------------------------------------------------\n");
						  mqlLogRequiredInformationWriter("Object Count in text file DONE ----->"+ iCnt + "\n");
						  mqlLogRequiredInformationWriter("Toplevel id.DONE               ----->"+ topLevelObjectId +"--"+now()+"\n");
						  mqlLogRequiredInformationWriter("------------------------------------------------------------\n");
						  domTopLevelObject=null;
						  topLevelObjectId=null;
						  //System.gc();
					  }
					  mqlLogRequiredInformationWriter("Pre-migration done for "+objectList.size()+" objects in objectids_"+m+".txt file. \n");
					  mqlLogRequiredInformationWriter("\n");
				  }catch(FileNotFoundException fnfExp){
					  // throw exception if file does not exists
					  throw fnfExp;
				  }
			  }

			  // Getting top level objects (this includes Feature,Products and ProductLines and also standalone features
			  StringBuffer sbTypePattern = new StringBuffer(50);
			  sbTypePattern.append(ConfigurationConstants.TYPE_FEATURES);
			  sbTypePattern.append(",");
			  sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
			  sbTypePattern.append(",");
			  sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCT_LINE);

			  StringBuffer sbObjWhere = new StringBuffer(50);
			  sbObjWhere.append("(");
			  sbObjWhere.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "]==False");
			  sbObjWhere.append("&&");
			  sbObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_FEATURES + "\"");
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

			  StringList objSelects = new StringList();
			  objSelects.addElement(SELECT_ID);
			  objSelects.addElement(SELECT_TYPE);
			  objSelects.addElement(SELECT_NAME);


			  //finding if any Feature objects in database or Product objects which have FLT relationship are not stamped
			  StringBuffer unStampedTypePattern = new StringBuffer(50);
			  unStampedTypePattern.append(ConfigurationConstants.TYPE_FEATURES);
			  unStampedTypePattern.append(",");
			  unStampedTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);

			  emxVariantConfigurationIntermediateObjectMigration_mxJPO VariantConfigInterObjMigrationInst= new emxVariantConfigurationIntermediateObjectMigration_mxJPO(context, new String[0]);
			  StringList slDerivationChangedType = VariantConfigInterObjMigrationInst.getDerivationChangedType(context,new String[0]);

				StringBuffer sbWhereClause = new StringBuffer(50);
				sbWhereClause.append("(");
				sbWhereClause.append("type.kindof == \"" + ConfigurationConstants.TYPE_FEATURES + "\"");

		        for(int i=0;i<slDerivationChangedType.size();i++){
		        	unStampedTypePattern.append(",");
		        	unStampedTypePattern.append(slDerivationChangedType.get(i));
	    			sbWhereClause.append("||");
	    			sbWhereClause.append("type.kindof == \"" + slDerivationChangedType.get(i).toString() + "\"");
		        }
		        sbWhereClause.append(")");

              StringBuffer unStampedObjWhere = new StringBuffer(100);
              unStampedObjWhere.append("(");
              unStampedObjWhere.append(sbWhereClause);
              unStampedObjWhere.append("&&");
			  unStampedObjWhere.append("interface["+ INTERFACE_FTRIntermediateObjectMigration +"]==FALSE");
			  unStampedObjWhere.append(")");
			  /*unStampedObjWhere.append("||");
			  unStampedObjWhere.append("(");
			  unStampedObjWhere.append("type.kindof == \"" + ConfigurationConstants.TYPE_PRODUCTS + "\"");
			  unStampedObjWhere.append("&&");
			  unStampedObjWhere.append("to["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "]==True");
			  unStampedObjWhere.append("&&");
			  unStampedObjWhere.append("interface["+ INTERFACE_FTRIntermediateObjectMigration +"]==FALSE");
			  unStampedObjWhere.append(")");*/

			  StringList sLobjSelects = new StringList();
			  sLobjSelects.addElement(SELECT_NAME);
			  sLobjSelects.addElement(SELECT_TYPE);
			  sLobjSelects.addElement(SELECT_ID);

			  mqlLogRequiredInformationWriter("Check for unStamped Objects starts "+"\n");

			  List unStampedObjectsList = DomainObject.findObjects(
					  context,
					  unStampedTypePattern.toString(),
					  DomainConstants.QUERY_WILDCARD,
					  unStampedObjWhere.toString(),
					  sLobjSelects);
			  mqlLogRequiredInformationWriter("Check for unStamped Objects ends "+"\n");

			  //setting premigration status
			  if(unStampedObjectsList.size()==0)
			  {
				  mqlLogRequiredInformationWriter("Unstamped object list size is 0.\n\n");
				  mqlLogRequiredInformationWriter("Pre-migration is complete.\n");
				  setAdminMigrationStatus(context,"PreMigrationComplete");
			  }
			  else
	                {
	                	Map unStampedObjMap = new HashMap();
	                	for(int i=0;i<unStampedObjectsList.size();i++){

	                		unStampedObjMap= (Map)unStampedObjectsList.get(i);
	                		String strUnStampId =(String)unStampedObjMap.get(SELECT_ID);
	                		writeOIDToTextFile(strUnStampId,"unStampedOIDs");
	                	}

	                  mqlLogRequiredInformationWriter("Pre-migration is not complete.\n");
	                  mqlLogRequiredInformationWriter("WARNING:  Some Feature objects are not stamped.\n");
	                  mqlLogRequiredInformationWriter("Please refer unStampedOIDs.txt for details.\n");
	                  mqlLogRequiredInformationWriter("\n");

	                }

			 if(IS_CONFLICT){

				 mqlLogRequiredInformationWriter("\n");
				  mqlLogRequiredInformationWriter("\n");
				  mqlLogRequiredInformationWriter("WARNING:  Conflict MixedUsage/MixedComposition/MarketingGBOM exist." + "\n");
				  mqlLogRequiredInformationWriter("Please view file 'Conflict.txt'for a list of these objects" + "\n");
				  mqlLogRequiredInformationWriter("These objects need to be deleted or fixed in order to proceed with the next step of migration.." + "\n");
				  mqlLogRequiredInformationWriter("\n");
				  mqlLogRequiredInformationWriter("\n");
			 }


			 String strMqlCommandOn = "trigger on";
			 MqlUtil.mqlCommand(context,strMqlCommandOn,true);
			 mqlLogRequiredInformationWriter("End of preMigration 'trigger on' done" + "\n");

	  }catch (Exception e) {
  			e.printStackTrace();
  			mqlLogRequiredInformationWriter("Pre-migration failed. \n"+e.getMessage() + "\n");
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
            			mqlLogRequiredInformationWriter("write OID failed. \n"+e.getMessage() + "\n");
            		} finally {
            		    // Close resource.
            		    if (conflictIDWriter != null)
            		    	conflictIDWriter.close();
            		}

               }
       }


      /**
       * Floats the connections from Feature List object to the new relationship which replaces the Feature List object.
       * @param context the eMatrix <code>Context</code> object
       * @param FLInfoList
       * @throws Exception
       */
      private void floatFLConnections(Context context,MapList FLInfoList ) throws Exception
      {
    	  Iterator FLInfoListItr = FLInfoList.iterator();
    	  while(FLInfoListItr.hasNext())
    	  {
    		  try{
    			  Map FLInfoMap = (Map)FLInfoListItr.next();
    			  
    			  String strNewRelId = (String)FLInfoMap.get("newRelId");
    			  String strNewRelType1 = (String)FLInfoMap.get("newRelType");

    			  Map mFLConnections = (Map)FLInfoMap.get("infoMapFLConnection");

    			  String strFeatType = (String) mFLConnections.get("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+"]");

    			  String strNewFeatType =(String) mFLConnections.get("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].from.attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");
    			  if(strNewFeatType==null || strNewFeatType.trim().equals(""))
    				  strNewFeatType =(String) mFLConnections.get("to[" + ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM + "].from.attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");

    			  if(ProductLineCommon.isNotNull(strNewFeatType)){
    				  strNewFeatType = PropertyUtil.getSchemaProperty(context, strNewFeatType);
    			  }
    			  else{
    				  strNewFeatType = "";
    			  }

    			  String strMandAtt = (String) mFLConnections.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");

    			  //Get the Context Product id
    			  String strContxtProdId = (String) mFLConnections.get("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].from.id");
    			  if(strContxtProdId==null || strContxtProdId.trim().equals(""))
    				  strContxtProdId = (String) mFLConnections.get("to[" + ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM + "].from.id");
    			  String strCommittedFeat = (String) mFLConnections.get("from[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO + "].to.id");
    			  
    			  Set FLMapKeySet = (Set) mFLConnections.keySet();
    			  Iterator FLMapKeySetItr = FLMapKeySet.iterator();
    			  while (FLMapKeySetItr.hasNext()) {
    				  String key = (String) FLMapKeySetItr.next();

    				  Object objRelIds = mFLConnections.get(key);
    				  StringList slRelIds = new StringList();

    				  if (objRelIds instanceof StringList) {
    					  slRelIds = (StringList) mFLConnections.get(key);
    				  } else if (objRelIds instanceof String) {
    					  slRelIds.addElement((String) mFLConnections.get(key));
    				  }

    				  Iterator relIdsItr = slRelIds.iterator();
    				  while (relIdsItr.hasNext()){
    					  String relId = (String) relIdsItr.next();

    					  /*In case of "Manufacturing Features,Marketing Features" we are not going to add PFL in 2012x
    					   * which we used to have in 2012.
    					   */
    					  if(key!=null && key.equalsIgnoreCase("to[" + ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].id")){

    						  if(strFeatType!=null
    							 && !strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MANUFACTURING)
    							 && !strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MARKETING)
    							 //&& !strNewFeatType.equalsIgnoreCase(ConfigurationConstants.TYPE_CONFIGURATION_FEATURE)
    							 //&& !strNewFeatType.equalsIgnoreCase(ConfigurationConstants.TYPE_CONFIGURATION_OPTION)
    							 && !mxType.isOfParentType(context, strNewFeatType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
    							 && !strNewFeatType.equalsIgnoreCase("emxR212~Configuration Feature")
    							 && (strNewRelType1!=null && !strNewRelType1.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS))
    							 && (strNewRelType1!=null && !strNewRelType1.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES))
    							 ){

    							    DomainRelationship domRel = new DomainRelationship(relId);
    						        //Get the attributes on Relationship
    						        Map attributeMap = new HashMap();
    						        attributeMap = domRel.getAttributeMap(context,true);

	    						      //Check for the value of "Feature Allocation Type" attribute value
	      							  String strFATVal = (String)attributeMap.get(ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE);
	      							  if(ProductLineCommon.isNotNull(strFATVal)
	      								  && strFATVal.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MANDATORY)){

	      								  attributeMap.put(ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,
	      										           ConfigurationConstants.RANGE_VALUE_REQUIRED);
	      								  mqlLogRequiredInformationWriter("Value of 'Feature Allocation Type' attribute for Rel id : " + relId
	      										                                   + "\n"
	      										                                   + "changed from 'Mandatory' to 'Required'."
	      										                                   + "\n\n");
	      							  }else if(!ProductLineCommon.isNotNull(strFATVal)
	      									  || " ".equalsIgnoreCase(strFATVal)){

	      								  mqlLogRequiredInformationWriter("Value of 'Feature Allocation Type' attribute for Rel id : " + relId
				                                    + "\n"
				                                   + "is blank."
				                                   + "\n\n");
	      								  attributeMap.put(ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,
										           ConfigurationConstants.RANGE_VALUE_OPTIONAL);
	      								  mqlLogRequiredInformationWriter("Value of 'Feature Allocation Type' attribute for Rel id : " + relId
				                                   + "\n"
				                                   + "changed from blank to 'Optional'."
				                                   + "\n\n");

	      							  }
	      							 replaceIntermediateObjectWithRel(context, relId, strNewRelId, "to",attributeMap);
    						  }

    					  }else if(key!=null && key.equalsIgnoreCase("from.from[" + ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE + "].id")){

    						    DomainRelationship domRel = new DomainRelationship(relId);
						        //Get the attributes on Relationship
						        Map attributeMap = new HashMap();
						        attributeMap = domRel.getAttributeMap(context,true);
    						    replaceIntermediateObjectWithRel(context, relId, strNewRelId, "from",attributeMap);

    					  }else if(key!=null && key.equalsIgnoreCase("to[" + ConfigurationConstants.RELATIONSHIP_COMMITED_ITEM + "].id")) {


    						  String ifFLFromRelExits =(String) mFLConnections.get("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].id");
						     						  String strNewRelType="";
						     						  if(ifFLFromRelExits!=null && ifFLFromRelExits.length()!=0){

						     							  //Depending upon the "Feature Type" we are going to change the relationship "Committed Item"
						         						  if(strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_TECHNICAL)){
						         							  strNewRelType = ConfigurationConstants.RELATIONSHIP_COMMITTED_LOGICAL_FEATURES;
						         						  }else if(strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MARKETING)){

						         							 /*In R211, Mandatory CF where getting added as Committed Features.
						         							   In R212 they should be added as "Mandatory Configuration Features".*/

						         							 if(strMandAtt!=null && strMandAtt.equalsIgnoreCase("Yes")){
						         								  strNewRelType = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;
						         							  }else{
						         								 strNewRelType = ConfigurationConstants.RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES;
						         							  }

						         							 /*if(strMandAtt!=null && !strMandAtt.equalsIgnoreCase("Yes")){
							         								strNewRelType = ConfigurationConstants.RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES;
							         						 }*/


						         						  }else if(strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MANUFACTURING)){

						         							  strNewRelType = ConfigurationConstants.RELATIONSHIP_COMMITTED_MANUFACTURING_FEATURES;

						         						  }else if(strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NONE)){

						         							  strNewRelType = ConfigurationConstants.RELATIONSHIP_COMMITTED_CONFIGURATION_FEATURES;
						         						  }

						     						  }else{


						         						  if(strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MARKETING)
						         							  || strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NONE))
						         						  {
						         							  strNewRelType = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;

						         						  }/*else if(strFeatType!=null && strFeatType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NONE)){
						         							  strNewRelType = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;

						         						  }*/
						     						  }


						     						  if(ProductLineCommon.isNotNull(strNewRelType)){

						     							//Change the "To Side" of "Committed Item" relationship to Committed CF/LF/MF
							     						  //String strMqlCommand = "modify connection "	+ relId + " to " + strCommittedFeat;
						     							 String strMqlCommand = "modify connection $1 to $2";

							     						  MqlUtil.mqlCommand(context, strMqlCommand,true,relId,strCommittedFeat);

							     						  //"Committed Item" to "Committed CF/LF/MF" relationship

							     						  DomainRelationship.setType(context, relId, strNewRelType);

							     						  HashMap mapRelAttributes = new HashMap();
							     						  if(strNewRelType!=null && strNewRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)){

							     							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER,
							       									  (String)mFLConnections.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]"));
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION,
							       									  (String)mFLConnections.get("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]"));
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_LIST_PRICE,
							       									  (String)mFLConnections.get("attribute["+ConfigurationConstants.ATTRIBUTE_LIST_PRICE+"]"));
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY,
							       									  (String)mFLConnections.get("attribute["+ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY+"]"));
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY,
							       									  (String)mFLConnections.get("attribute["+ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY+"]"));
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,ConfigurationConstants.RANGE_VALUE_SYSTEM);

							       							  //Set "Inherited" attribute set to to True ,CF is inherited from PL
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_INHERITED,ConfigurationConstants.RANGE_VALUE_TRUE);
							       							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_FALSE);

							       							  DomainRelationship domRel1 = new DomainRelationship(relId);
							       							  mqlLogRequiredInformationWriter("Rel id :: "+ relId +"\n");
							          					      mqlLogRequiredInformationWriter("Attribute value Map set for this Rel id :: "+ mapRelAttributes +"\n\n");
							       							  domRel1.setAttributeValues(context, mapRelAttributes);

							     						  }

							     						  //Connect new Rel with Contxt Prod Id with new Relationship "Committed Context"
							     						  if(ProductLineCommon.isNotNull(strContxtProdId)){
							     							 String strRel = ConfigurationConstants.RELATIONSHIP_COMMITTED_CONTEXT;
								     						  RelationshipType strRelType = new RelationshipType(strRel);
								     						  ProductLineCommon PL = new ProductLineCommon(relId);
								     						  PL.connectObject(context,
								 										 strRelType,
								 										 strContxtProdId,
								 									     false);

								     						  mqlLogRequiredInformationWriter("Connection between ::"
								     								  						  + relId
								     								  						  + strRelType
								     								  						  +	strContxtProdId
								     						  									);
							     						  }

						     						  }
    					  }
    					  else if(key.startsWith("to")
    							  && !(key.equalsIgnoreCase("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].type"))
    							  && !(key.equalsIgnoreCase("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].id"))
    							  && !(key.equalsIgnoreCase("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].from.id"))
    							  && !(key.equalsIgnoreCase("to[" + ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM + "].from.type"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].attribute["+ATTRIBUTE_SEQUENCE_ORDER+"]"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.id"))
							      && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.type"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE+"]"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id"))
    							  && !(key.equalsIgnoreCase("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id")))
    							  {
		    						      DomainRelationship domRel = new DomainRelationship(relId);
								          //Get the attributes on Relationship
								          Map attributeMap = new HashMap();
								          attributeMap = domRel.getAttributeMap(context,true);
    						              replaceIntermediateObjectWithRel(context, relId, strNewRelId,"to",attributeMap);
	    					      }
	    				  }
    			  }
    		  }catch(Exception e)
    		  {
    			  e.printStackTrace();
    			  throw new FrameworkException(e.getMessage() + "\n");
    		  }
    	  }
      }


      /**
       * Floats the connections from GBOM object to GBOM relationship.
       * @param context the eMatrix <code>Context</code> object
       * @param gbomObjId
       * @param gbomRelId
       * @throws Exception
       */
      private void floatGBOMConnections(Context context,MapList gbomInfoList) throws Exception
      {
    	  Iterator FLInfoListItr = gbomInfoList.iterator();
    	  while(FLInfoListItr.hasNext())
    	  {
    		  try{
		    	  Map gbomMap = (Map)FLInfoListItr.next();
				  String gbomRelId = (String)gbomMap.get("newRelId");
				  Map mGBOMConnections = (Map)gbomMap.get("infoMapGBOMConnection");

				  Set GBOMMapKeySet = (Set) mGBOMConnections.keySet();
				  Iterator gbomMapKeySetItr = GBOMMapKeySet.iterator();
				  while (gbomMapKeySetItr.hasNext()) {

				  String key = (String) gbomMapKeySetItr.next();

        			// process for relationships on GBOM Object
        			if(key.startsWith("to") || key.startsWith("from") ){

        				if (key.indexOf("tomid") == -1 && key.indexOf("frommid") == -1) {
        					// do not consider GBOM From , Inactive GBOM From, Custom GBOM and GBOM To relationships as they are already taken care
        					if (!key.equals("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].id")
        							&& !key.equals("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.id")
        							&& !key.equals("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].id")
        							&& !key.equals("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM + "].id")
        							&& !key.equals("from[" + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].id")
        							&& !key.equals("from[" + ConfigurationConstants.RELATIONSHIP_GBOM_TO + "].to.id")
        							&& !key.equals("to[" + ConfigurationConstants.RELATIONSHIP_GBOM_FROM + "].from.type")
        							&& !key.equals("to[" + ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].from.type")
        							&& !key.equals("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM + "].from.type")
        							&& !key.equals("to["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM + "].from.id")
        							&& !key.equals("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_PART+"]")
        							&& !key.equals("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_GBOM_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_FROM_VARIANT+"]")
        							&& !key.equals("to[" + ConfigurationConstants.RELATIONSHIP_CUSTOM_GBOM + "].from.id")
        					) {

        						Object objRelIds = mGBOMConnections.get(key);
        						StringList slRelIds = new StringList();
        						if (objRelIds instanceof StringList) {
        								slRelIds = (StringList) mGBOMConnections.get(key);
        						} else if (objRelIds instanceof String) {
        								slRelIds.addElement((String) mGBOMConnections.get(key));
        						}

        						Iterator relIdsItr = slRelIds.iterator();

        						while (relIdsItr.hasNext()) {
        							String relId = (String) relIdsItr.next();
        							if (key.startsWith("to")) {
	        								DomainRelationship domRel = new DomainRelationship(relId);
	        						        //Get the attributes on Relationship
	        						        Map attributeMap = new HashMap();
	        						        attributeMap = domRel.getAttributeMap(context,true);
        									replaceIntermediateObjectWithRel(context, relId, gbomRelId,"to",attributeMap);

       								} else if (key.startsWith("from")) {
	           								DomainRelationship domRel = new DomainRelationship(relId);
	        						        //Get the attributes on Relationship
	        						        Map attributeMap = new HashMap();
	        						        attributeMap = domRel.getAttributeMap(context,true);
        									replaceIntermediateObjectWithRel(context, relId, gbomRelId,"from",attributeMap);
       								}
       							}
       						}
       					}

        				// process for relationships on GBOM From, Inactive GBOM From
        				// and Custom GBOM relationships
        				if (key.indexOf("tomid") != -1 || key.indexOf("frommid") != -1) {
        					
        					//String toOrFromrel = "torel";
        					String toOrFromrel = "to";
        					if (key.indexOf("frommid") != -1) {
        							//toOrFromrel = "fromrel";
        							toOrFromrel = "from";
        					}

        					String midRelType = key.substring(key.lastIndexOf("[") + 1,key.lastIndexOf("]"));
        					Object objMidRelIds = mGBOMConnections.get(key);
        					StringList slMidRelIds = new StringList();
        					if (objMidRelIds instanceof StringList) {
        							slMidRelIds = (StringList) mGBOMConnections.get(key);
        					} else if (objMidRelIds instanceof String) {
        							slMidRelIds.addElement((String) mGBOMConnections.get(key));
        					}



        					Iterator midRelIdsItr = slMidRelIds.iterator();
        					while (midRelIdsItr.hasNext()) {
        						String relId = (String) midRelIdsItr.next();

        							/*String strMqlCommand = "modify connection " + relId	+ " " + toOrFromrel + " " + gbomRelId;
        							MqlUtil.mqlCommand(context, strMqlCommand, true);*/
	    						    DomainRelationship domRel = new DomainRelationship(relId);
							        //Get the attributes on Relationship
							        Map attributeMap = new HashMap();
							        attributeMap = domRel.getAttributeMap(context,true);
	        						String strNewRelId = replaceIntermediateObjectWithRel(context, relId, gbomRelId,toOrFromrel,attributeMap);

        	                        //"Inactive Varies By GBOM From" relationship is changed to "Inactive Varies By GBOM" in 2012x
        							if (midRelType.equals(ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY_GBOM_FROM)) {
    								DomainRelationship.setType(context, strNewRelId, ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY_GBOM);
        							}

        					}
       					}
        			}
     		     }
       		  }catch (Exception e) {
       			        e.printStackTrace();
        				throw new FrameworkException(e.getMessage());
       	         }
    	  }
	}



	private String getNewFeatureType(Context context,String strContextUsage,boolean isLeafLevel,String strObjType) throws Exception
	{
		String strNewFeatureType = "";
		if(mxType.isOfParentType(context, strObjType, ConfigurationConstants.TYPE_SOFTWARE_FEATURE))
		{
			String strSymbolicType = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strObjType, true);
			try{
				//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration."+strSymbolicType);
				strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration."+strSymbolicType);
			}catch (Exception e){

				//If property setting not found for the given type..Check for Parent Type and then set the Type
				String strParentType = UICache.getParentTypeName(context,strObjType);
				String strParSymbolicType = FrameworkUtil.getAliasForAdmin(context,DomainConstants.SELECT_TYPE, strParentType, true);
				//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration."+strParSymbolicType);
				strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration."+strParSymbolicType);
			}finally{
				return strNewFeatureType;
			}

		}
		if(strContextUsage.equals("Marketing"))
		{
			if(isLeafLevel)
			{
				//String leafLevelSetting = getResourceProperty(context,"emxConfiguration.Migration.LeafLevelAsOption");
				String leafLevelSetting = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.LeafLevelAsOption");
				if("True".equalsIgnoreCase(leafLevelSetting))
				{
					//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration.ConfigurationOption");
					strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.ConfigurationOption");
				}else{
					//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration.MarketingFeatureType");
					strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.MarketingFeatureType");
				}
			}else{
				//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration.MarketingFeatureType");
				strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.MarketingFeatureType");
			}

		}else if(strContextUsage.equals("Technical"))
		{
			//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration.TechnicalFeatureType");
			strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.TechnicalFeatureType");
		}else if(strContextUsage.equals("Manufacturing"))
		{
			//strNewFeatureType = getResourceProperty(context,"emxConfiguration.Migration.ManufacturingFeatureType");
			strNewFeatureType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.ManufacturingFeatureType");
		}
		return strNewFeatureType;
	}

	private void stampFeature(Context context, String strFeatureId, String strNewFeatureType,String strResolvedNewFeatureType,StringList sLGBOMIds) throws Exception {
		DomainObject domObj=null;
		String strConflictValue="";
		Map interfaceAttribMap = new HashMap();

		String strOriginalStampValue = strNewFeatureType;

		try{
			String strConflict = "No";
			domObj= DomainObject.newInstance(context, strFeatureId);
			strConflictValue = domObj.getAttributeValue(context,ATTRIBUTE_FTRMigrationConflict);
			if (strConflictValue == null || "null".equals(strConflictValue)	|| "".equals(strConflictValue)) {

				if(strNewFeatureType.equals("MixedUsage")
				   //|| strNewFeatureType.equals("MixedComposition")
				   || strNewFeatureType.equals("IncorrectMapping")){

					strConflict = "Yes";
					//Set the value to true so that user will get notification about "Conflict.txt" at end of Pre-Migration
					IS_CONFLICT = true;
					writeOIDToTextFile(strFeatureId
							           +"-->Conflict Due to: "
							           +strNewFeatureType,
							           "Conflict_IncorrectMapping");

				}else if(strNewFeatureType.equals("MixedComposition")){
					if(ProductLineCommon.isNotNull(RESOLVE_CONFLICTS) && RESOLVE_CONFLICTS.equalsIgnoreCase("ResolveConflicts")){

						writeOIDToTextFile(strFeatureId
						           +"-->Conflict resolved from : "
						           +strNewFeatureType
						           +" "
						           +"to   "
						           +strResolvedNewFeatureType,"Resolved_Conflict");

						//Change the attribute values to suggested new values
						strConflict = "No";
						strNewFeatureType = strResolvedNewFeatureType;

					}else{

						//Set the value to true so that user will get notification about "Conflict.txt" at end of Pre-Migration
						strConflict = "Yes";
						IS_CONFLICT = true;
						writeOIDToTextFile(strFeatureId
								           +"-->Conflict Due to: "
								           +strNewFeatureType
								           +" "
								           +"Recommended to change this value to "
								           +strResolvedNewFeatureType,"Recommendations_For_Conflicts");
					}
				}

				if(strNewFeatureType!=null &&
						(strNewFeatureType.equalsIgnoreCase("type_ConfigurationFeature")
						  ||
					     strNewFeatureType.equalsIgnoreCase("type_ConfigurationOption"))){

						//Check if "GBOM" object is connected to Configuration Feature or Configuration Option
						//DomainObject domFeatObj = new DomainObject(strFeatureId);
						//Boolean bHasGBOM = domFeatObj.hasRelatedObjects(context, ConfigurationConstants.RELATIONSHIP_GBOM_FROM, true);
						//if(bHasGBOM){

					    if(!sLGBOMIds.isEmpty()){

					    	if(ProductLineCommon.isNotNull(RESOLVE_CONFLICTS) && RESOLVE_CONFLICTS.equalsIgnoreCase("ResolveConflicts")){

					    	 String strMqlCommandOFF = "trigger off";
					   		 MqlUtil.mqlCommand(context,strMqlCommandOFF,true);

					   		 mqlLogRequiredInformationWriter("GBOM objects connected to Marketing Feature found... " + "\n");
					   		 mqlLogRequiredInformationWriter("Deletion of GBOM objects connected to Marketing Feature started... " + "\n");
					   	     deleteObjects(context,sLGBOMIds);
					   	     mqlLogRequiredInformationWriter("Deletion of GBOM objects connected to Marketing Feature ended... " + "\n"+"\n"+"\n");

					   	     String strMqlCommandON = "trigger on";
					 		 MqlUtil.mqlCommand(context,strMqlCommandON,true);

					    		//String strFeatWithGBOM = "Marketing Feature connected to GBOM";
					    	 writeOIDToTextFile("Feature Id---->"
					    			       +strFeatureId
								           +"---->Conflict resolved   : "
								           +"GBOM connected to Marketing Feature deleted--->"
								           + sLGBOMIds,
								           "Resolved_MarketingFeatureWithGBOM");

					    	 if(ProductLineCommon.isNotNull(strResolvedNewFeatureType)){

					    		 writeOIDToTextFile("---->Also this Feature had conflict : "
								           +"was resolved from --->"
								           +strOriginalStampValue
								           +"  to --->"
								           +strResolvedNewFeatureType,"Resolved_MarketingFeatureWithGBOM");
					    	 }

								//Change the attribute values to suggested new values
								strConflict = "No";

					    	}else{
					    		strNewFeatureType = "MarketingWithGBOM";
								strConflict = "Yes";
								//Set the value to "true" so that user will get notification about "Conflict.txt" at end of Pre-Migration
								IS_CONFLICT = true;
								writeOIDToTextFile(strFeatureId
										           +"-->Conflict Due to: "
										           +strNewFeatureType,
										           "Conflict_MarketingFeatureWithGBOM");

					    	}
						}
					}

				interfaceAttribMap.put(ATTRIBUTE_NEW_FEATURE_TYPE,strNewFeatureType);
				interfaceAttribMap.put(ATTRIBUTE_FTRMigrationConflict, strConflict);

				//ConfigurationUtil.addInterfaceAndSetAttributes(context,strFeatureId,"bus",INTERFACE_FTRIntermediateObjectMigration,interfaceAttribMap);
				addInterfaceAndSetAttributes(context,strFeatureId,"bus",INTERFACE_FTRIntermediateObjectMigration,interfaceAttribMap);
			}

		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			domObj=null;
			strConflictValue=null;
			interfaceAttribMap=null;
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



	/**
	 * Sets the migration status as a property setting.
     * @param context the eMatrix <code>Context</code> object
 	 * @param strStatus String containing the status setting
	 * @throws Exception
	 */
	public void setAdminMigrationStatus(Context context,String strStatus) throws Exception
	{
		String cmd = "modify program eServiceSystemInformation.tcl property MigrationR212VariantConfiguration value "+strStatus;
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

		String cmd = "print program eServiceSystemInformation.tcl select property[MigrationR212VariantConfiguration].value dump";
	    String result =	MqlUtil.mqlCommand(context, mqlCommand, cmd);

	    if(result.equalsIgnoreCase("PreMigrationInProcess"))
		{
			return 1;
		}else if(result.equalsIgnoreCase("PreMigrationComplete"))
		{
			return 2;
		}else if(result.equalsIgnoreCase("FeatureObjectsNotFound")) //else if(result.equalsIgnoreCase("GBOMInProcess"))
		{
			return 3;
		}else if(result.equalsIgnoreCase("FeatureInProcess")) //else if(result.equalsIgnoreCase("GBOMInProcess"))
		{
			return 4;
		}else if(result.equalsIgnoreCase("FeatureComplete")) //(result.equalsIgnoreCase("GBOMComplete"))
		{
			return 5;
		}else if(result.equalsIgnoreCase("GBOMObjectsNotFound")) //(result.equalsIgnoreCase("FeatureInProcess"))
		{
			return 6;
		}else if(result.equalsIgnoreCase("GBOMInProcess")) //(result.equalsIgnoreCase("FeatureInProcess"))
		{
			return 7;
		}else if(result.equalsIgnoreCase("GBOMComplete")) //(result.equalsIgnoreCase("FeatureComplete"))
		{
			return 8;
		}else if(result.equalsIgnoreCase("RuleObjectsNotFound")) //(result.equalsIgnoreCase("FeatureInProcess"))
		{
			return 9;
		}else if(result.equalsIgnoreCase("RuleInProcess"))
		{
			return 10;
		}else if(result.equalsIgnoreCase("RuleComplete"))
		{
			return 11;
		}else if(result.equalsIgnoreCase("ProductConfigurationObjectsNotFound")) //(result.equalsIgnoreCase("FeatureInProcess"))
		{
			return 12;
		}else if(result.equalsIgnoreCase("UpdatePCBOMXMLInProcess"))
		{
			return 13;
		}else if(result.equalsIgnoreCase("UpdatePCBOMXMLComplete"))
		{
			return 14;
		}
	    return 0;

	}

	/**
	 * Stamps the Rule object with the migration interface to indicate that the expression
	 * attributes for this Rule have been converted.
     * @param context the eMatrix <code>Context</code> object
     * @param strRuleId the Rule object id
     * @param bRuleMigrated
 	 * @return integer representing the status
	 * @throws Exception
	 */
	private void stampRuleObject(Context context, String strRuleId) throws Exception {

		DomainObject domObj = DomainObject.newInstance(context, strRuleId);

//		String strCommand = "modify bus " + strRuleId+ " add interface \'"+ INTERFACE_FTRIntermediateObjectMigration + "\'";
//		MqlUtil.mqlCommand(context, strCommand, true);
		String strCommand = "modify bus $1 add interface $2";
		MqlUtil.mqlCommand(context, strCommand, true,strRuleId,INTERFACE_FTRIntermediateObjectMigration);

		/*This means the Rule Object is successfully stamped..
		 hence we added the interface and attribute "Conflict" value as "No" */

		Map interfaceAttribMap = new HashMap();
		interfaceAttribMap.put(ATTRIBUTE_FTRMigrationConflict, "No");
		mqlLogRequiredInformationWriter("Rule Id stamped                           :: "+ strRuleId +"\n");
	    mqlLogRequiredInformationWriter("Attribute value Map set for this id       :: "+ interfaceAttribMap +"\n\n");
		domObj.setAttributeValues(context, interfaceAttribMap);

	}

	/**
	 * Replaces the intermediate Object i.e.FL/GBOM object with the specified relationship.
     * @param context the eMatrix <code>Context</code> object
     * @param strOldRelId --Old Rel Id
     * @param strNewRelId --New Rel Id
     * @param strNewRelIdSide --"from" or "To" side
	 * @throws Exception
	 */
  private String replaceIntermediateObjectWithRel(Context context, String strOldRelId ,String strNewRelId ,String strNewRelIdSide,Map attributeMap) throws Exception {

		String strNewConnId="";
		boolean isRelToRel = true;
		boolean isFrom = true;

		try{

		DomainRelationship domRel = new DomainRelationship(strOldRelId);
        //Get the attributes on Relationship
        /*Map attributeMap = new HashMap();
        attributeMap = domRel.getAttributeMap(context,true);*/


		//Get the "Type" of rel
		StringList slRelSelects = new StringList(1);
        slRelSelects.addElement(DomainRelationship.SELECT_TYPE);

        if(strNewRelIdSide!=null && strNewRelIdSide.equalsIgnoreCase("from")){
        	//New Rel replaces FL/GBOM which is on "from" side of Old Rel hence we query now "to" side of Old Rel
        	slRelSelects.add("to.id");
            slRelSelects.add("torel.id");

        }else if(strNewRelIdSide!=null && strNewRelIdSide.equalsIgnoreCase("to")){
        	//New Rel replaces FL/GBOM which is on "to" side of Old Rel hence we query now "from" side of Old Rel
        	slRelSelects.add("from.id");
            slRelSelects.add("fromrel.id");
        }

        Hashtable htRelData = domRel.getRelationshipData(context,slRelSelects);

        String strFromSideOfRel ="";
        String strToSideOfRel ="";
        String strRelId ="";
        String strObjId ="";

        StringList slRelTypes = (StringList) htRelData.get(DomainRelationship.SELECT_TYPE);
        String strRelType = (String) slRelTypes.get(0);
        RelationshipType RelType = new RelationshipType(strRelType);


        if((StringList) htRelData.get("from.id")!=null || ((StringList)htRelData.get("fromrel.id"))!=null ){

        	StringList slFromSideOfRel = new StringList();
        	if(!((StringList)htRelData.get("from.id")).isEmpty()){
        		slFromSideOfRel = (StringList) htRelData.get("from.id");
        		strFromSideOfRel = (String) slFromSideOfRel.get(0);

        		isRelToRel = false;
        		//isFrom = false;
        		strObjId = strFromSideOfRel;


        	}else if(!((StringList)htRelData.get("fromrel.id")).isEmpty()){
        		slFromSideOfRel = (StringList) htRelData.get("fromrel.id");
        		strFromSideOfRel = (String) slFromSideOfRel.get(0);

        		strRelId = strFromSideOfRel;
        		isFrom = false;
        	}
        }

        if((StringList) htRelData.get("to.id")!=null || ((StringList)htRelData.get("torel.id"))!=null ){

        	StringList slToSideOfRel = new StringList();
        	if(!((StringList)htRelData.get("to.id")).isEmpty()){
        		slToSideOfRel = (StringList) htRelData.get("to.id");
        		strToSideOfRel = (String) slToSideOfRel.get(0);

        		isRelToRel = false;
        		strObjId = strToSideOfRel;
        		isFrom = false;
        	}else if(!((StringList)htRelData.get("torel.id")).isEmpty()){

        		slToSideOfRel = (StringList) htRelData.get("torel.id");

        		strToSideOfRel = (String) slToSideOfRel.get(0);
        		strRelId =strToSideOfRel;
        		//isFrom = false;
        	}
        }


        if(isRelToRel){

          strNewConnId = connectRelationship(context, RelType, strRelId,strNewRelId, isFrom);

        }else{

        	strNewConnId = connectObject(context, RelType, strObjId,strNewRelId, isFrom);
        }

			  StringList relDataSelects = new StringList();
			  relDataSelects.addElement("frommid.id");
			  relDataSelects.addElement("tomid.id");

			  Map gbomRelData = domRel.getRelationshipData(context,relDataSelects);
			  StringList sLFrommid = new StringList();
			  StringList sLTomid = new StringList();

			  sLFrommid = (StringList) gbomRelData.get("frommid.id");
			  if(!sLFrommid.isEmpty()){
				for(int i=0;i<sLFrommid.size();i++){

					String strFromMid = (String)sLFrommid.get(i);
					setToRelationship(context,strFromMid,strNewConnId,true);
			    }
			  }


			  sLTomid = (StringList) gbomRelData.get("tomid.id");
			  if(!sLTomid.isEmpty()){
				for(int i=0;i<sLTomid.size();i++){

					String strToMid = (String)sLTomid.get(i);
					setToRelationship(context,strToMid,strNewConnId,false);
			    }
			  }

			DomainRelationship domNewConnId = new DomainRelationship(strNewConnId);
			mqlLogRequiredInformationWriter("Rel id :: "+ strNewConnId +"\n");
		    mqlLogRequiredInformationWriter("Attribute value Map set for this rel id :: "+ attributeMap +"\n\n");
	        domNewConnId.setAttributeValues(context, attributeMap);



		}catch (Exception e) {
			e.printStackTrace();
		}

		return strNewConnId;
	}

    /**Need to call from DomainRelationship
     * Connects the specified relationship with the given relationship.
     * @param context
     * @param relationshipType
     * @param relId
     * @param strNewRelId
     * @param isFrom
     * @return
     * @throws FrameworkException
     */
    public String connectRelationship(Context context,
    		RelationshipType relationshipType,
    		java.lang.String relId, String strNewRelId,
    		boolean isFrom)throws Exception
    {
    	String fromRelId = null;
    	String toRelId = null;
    	String connId = null;
    	StringBuffer sbCmd = new StringBuffer();
    	StringBuffer sbCmd2 = new StringBuffer();
        try
        {
        	if(isFrom)
        	{
        		fromRelId = strNewRelId;
        		toRelId = relId;
        	}else
        	{
        		fromRelId = relId;
        		toRelId = strNewRelId;
        	}

        	sbCmd.append("add connection \"");
        	sbCmd.append(relationshipType);
        	sbCmd.append("\" fromrel \"");
        	sbCmd.append(fromRelId);
        	sbCmd.append("\" torel \"");
        	sbCmd.append(toRelId);
        	sbCmd.append("\" select id dump;");
        	
        	sbCmd2.append("add connection ");
        	sbCmd2.append("$1");
        	sbCmd2.append(" fromrel ");
        	sbCmd2.append("$2");
        	sbCmd2.append(" torel ");
        	sbCmd2.append("$3");
        	sbCmd2.append(" select $4 dump");

        	mqlLogRequiredInformationWriter("MQL command to be executed ::" + "\n" + sbCmd.toString()+ "\n");
        	connId = MqlUtil.mqlCommand(context, sbCmd2.toString(), true,relationshipType.getName(),fromRelId,toRelId,"id");

        }
        catch (Exception e)
        {
        	mqlLogRequiredInformationWriter("MQL command execution failed in 'connectRelationship' API ::" + sbCmd.toString()+ "\n");
        	e.printStackTrace();
            throw new FrameworkException(e);
        }
		return connId;
    }


    /** Need to call from DomainRelationship
     * Connects the specified relationship with the given Object.
     * @param context
     * @param relationshipType
     * @param objectId
     * @param strNewRelId
     * @param isFrom
     * @return
     * @throws FrameworkException
     */
    public String connectObject(Context context,
    		RelationshipType relationshipType,
    		java.lang.String objectId,String strNewRelId,
    		boolean isFrom)throws Exception
    {
    	String fromId = null;
    	String toId = null;
    	String connId = null;
    	StringBuffer sbCmd = new StringBuffer();
    	StringBuffer sbCmd2 = new StringBuffer();
    	String strFrom = null;
    	String strTo = null;

        try
        {
        	if(!isFrom)
        	{
        		fromId = strNewRelId;
        		toId = objectId;
        		strFrom = "fromrel";
        		strTo = "to";
        	}else
        	{
        		fromId = objectId;
        		toId = strNewRelId;
        		strFrom = "from";
        		strTo = "torel";
        	}

        	sbCmd.append("add connection \"");
        	sbCmd.append(relationshipType);
        	sbCmd.append("\" ");
        	sbCmd.append(strFrom);
        	sbCmd.append(" \"");
        	sbCmd.append(fromId);
        	sbCmd.append("\" ");
        	sbCmd.append(strTo);
        	sbCmd.append(" \"");
        	sbCmd.append(toId);
        	sbCmd.append("\" select id dump;");
        	
        	sbCmd2.append("add connection ");
        	sbCmd2.append("$1");
        	sbCmd2.append(" ");
        	sbCmd2.append(strFrom);
        	sbCmd2.append(" ");
        	sbCmd2.append("$2");
        	sbCmd2.append(" ");
        	sbCmd2.append(strTo);
        	sbCmd2.append(" ");
        	sbCmd2.append("$3");
        	sbCmd2.append(" select $4 dump");

        	mqlLogRequiredInformationWriter("MQL command to be executed ::" + "\n" + sbCmd.toString()+ "\n");
        	connId = MqlUtil.mqlCommand(context, sbCmd2.toString(), true,relationshipType.getName(),fromId,toId,"id");

        }
        catch (Exception e)
        {
        	mqlLogRequiredInformationWriter("MQL command execution failed in 'connectObject' API ::" + sbCmd.toString()+ "\n");
        	e.printStackTrace();
            throw new FrameworkException(e);
        }
		return connId;
    }


	/**
	 *  Replaces the Feature List object with the specified relationship.
	 *  Computes new Relationship and internally calls "floatConnections" method.
     * @param context the eMatrix <code>Context</code> object
     * @param featObjMap Feature related info which is to be processed
     * "FL" objects related to Feature retrieved then "FloatFLConnection"
     *  method called.
	 * @throws Exception
	 */

    private void  migrateFeaturesStep_1(Context context, Map featObjMap)
    throws Exception{

    	  mqlLogRequiredInformationWriter("Inside method  migrateFeaturesStep_1 "+ "\n");
    	  //These selectables are for Parent Feature
    	  StringList featureObjSelects = new StringList();
		  featureObjSelects.addElement(SELECT_TYPE);
		  featureObjSelects.addElement(SELECT_NAME);
		  featureObjSelects.addElement(SELECT_REVISION);
		  featureObjSelects.addElement(SELECT_VAULT);
		  featureObjSelects.addElement(SELECT_ID);

		  featureObjSelects.addElement("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");
		  featureObjSelects.addElement("attribute["+ATTRIBUTE_FTRMigrationConflict+"]");

		  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT+"]");//goes into LF or MF type
		  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML+"]");//goes into LF,MF,CF type
		  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");//goes into LF,MF,CF type
		  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]");//goes into LF,MF,CF type
		  featureObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_ORIGINATOR+"]");//goes into LF,MF,CF type

		  String featureObjId = "";
          String strType = "";
          String strName = "";
          String strRevision = "";
             String strNewRelId = "";
    	  featureObjId = (String) featObjMap.get(SELECT_ID);


    	  String isConflictFeature = (String)featObjMap.get("attribute["+ATTRIBUTE_FTRMigrationConflict+"]");
		  String strContextUsage = (String)featObjMap.get("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");

		  StringList sLFLToDelete = new StringList(); //FLs list to delete

		  strType = (String) featObjMap.get(SELECT_TYPE);
		  strName = (String) featObjMap.get(SELECT_NAME);
		  strRevision = (String) featObjMap.get(SELECT_REVISION);


		  if((strType!=null
				  &&(mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
	                  || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
	                  || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))))
           {
			  isConflictFeature = "No";
		  }

		  if(isConflictFeature.equalsIgnoreCase("No")
		       || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
			   || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
			   || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
		  {


		  DomainObject domFeatureObj  = new DomainObject(featureObjId);

		  HashMap mapAttribute = new HashMap();

		  //check whether feature is standalone or Toplevel or is in the Structure
		  //String relPattern = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO;

		    StringBuffer sbRelPattern1 = new StringBuffer(50);
			sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO);
			sbRelPattern1.append(",");
			sbRelPattern1.append(ConfigurationConstants.RELATIONSHIP_CANDIDATE_ITEM);

			StringBuffer sbTypePattern2 = new StringBuffer(50);
				sbTypePattern2.append(ConfigurationConstants.TYPE_FEATURE_LIST);
				sbTypePattern2.append(",");
				sbTypePattern2.append(ConfigurationConstants.TYPE_MODEL);




		  StringList flObjSelects = new StringList();
		  flObjSelects.addElement(ConfigurationConstants.SELECT_ID);
		  flObjSelects.addElement(DomainObject.SELECT_FROM_ID);
		  flObjSelects.addElement(DomainObject.SELECT_TO_ID);

		  StringList flRelSelects = new StringList();
		  flRelSelects.addElement(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
		  flRelSelects.addElement("from.from[" + ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE + "].id");

	      flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_CANDIDATE_ITEM+"].from.id");
	      flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_CANDIDATE_ITEM+"].id");

		  //Get the Feature Ids,Type and New Feature Type attribute values... when "Feature List To" rel traversed
		  flObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
		  flObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.type");
		  flObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");

		  //Get the Feature Ids,Type and New Feature Type attribute values... when "Feature List From" rel traversed
		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id");
		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type");
		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");

		  //In DV id Inactive in context of Product or Invalid in context of PV
		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].id");
		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.id");
		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.type");
		  //flObjSelects.addElement("from[" + ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE + "].id");
		  //selectables of all attributes on FL which are to be migrated
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_OBJECT_NAME+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_USAGE+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_QUANTITY+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION+"]"); //goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR+"]");//goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE+"]");//goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_FIND_NUMBER+"]");//goes on to LF and MF relationships
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_LIST_PRICE+"]");//goes on to CF and CO relationship
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY+"]"); //goes on to CF and CO relationship
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY+"]");//goes on to CF and CO relationship
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");//goes on to CF and CO relationship
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_RULE_TYPE+"]");//goes on to CF,LF,MF relationship
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE+"]"); //goes on to CF type
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]"); //will go as interface attribute onto relationship mentioned in PES
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");//this will be used as described in PES
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_SELECTION_TYPE+"]"); //will get split
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_TYPE+"]"); //will get split
		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
   		  flObjSelects.addElement("attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");

   		  //selectables of attribute on FLF relationship
   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]");

   		  //selectables to determine if the Feature can have Key-In Type = Input
   		  flObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE+"]");
   		  flObjSelects.addElement("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_SELECTION_TYPE+"]");

   		  //selectable to get Key-In Value on Selected Option Relationship
   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");
   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE+"]");
   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id");
   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");

   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id");
   		  flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");

   		  //flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id");
   		  //flObjSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.id");


   		  mqlLogRequiredInformationWriter("Database query on the given Feature Id started ----->"+" --- "+now()+"\n");

   		  MapList FL_List = domFeatureObj.getRelatedObjects(context,
															  //relPattern,              //relationshipPattern
				  											  sbRelPattern1.toString(),
															  //ConfigurationConstants.TYPE_FEATURE_LIST,          //typePattern
				  											  sbTypePattern2.toString(),
															  flObjSelects,               //objectSelects
															  flRelSelects,                  //relationshipSelects
															  true,                //getTo
															  false,                  //getFrom
															  (short)1,               //recurseToLevel
															  null,                         //objectWhere,
															  null,                     //relationshipWhere
															  (int)0,                  //limit
															  null ,                  //includeType
															  null,                   //includeRelationship
															  null);                 //includeMap


   		  mqlLogRequiredInformationWriter("Database query on the given Feature Id end     ----->"+" --- "+now()+"\n");
		   //To check whether the "Feature Selection Type" has any conflict for this Feature
		  boolean bConflictSelType = false;
		  String strFST ="";
		  StringList sLFST = new StringList();
		  for(int iCntFL=0;iCntFL<FL_List.size();iCntFL++){
			  Map flMap = (Map)FL_List.get(iCntFL);
			  strFST = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_SELECTION_TYPE+"]");
			  if(sLFST.size()>0){
				  if(!sLFST.contains(strFST)){
					  bConflictSelType = true;
					  break;
				  }else{
					  sLFST.add(strFST);
				  }
			  }else{
				  if(strFST!=null && !strFST.equals("")){
					  sLFST.add(strFST);
				  }
			  }
		  }

		  //If FL object is absent
		  if(FL_List.size()==0)
		  {
			 mqlLogRequiredInformationWriter("For given Feature id there are no 'Feature List From' and 'Feature List To' connections."+"\n");
			 if(strType!=null
				 &&(!mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
				 &&!mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
				 &&!mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))||
				 isOfDerivationChangedType(context, strType)) {
				  if(strType!=null && !isOfDerivationChangedType(context, strType)){


				  //Get the new Feature Type
				  String newFeatureType = (String)featObjMap.get("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");


				  //String newFeatureChangeType = PropertyUtil.getSchemaProperty(context,newFeatureType);
				  String newFeatureChangeType = getSchemaProperty(context,newFeatureType);

				  //Get the new Feature Policy
				  Map mNewFeaturePolicy = mxType.getDefaultPolicy(context, newFeatureChangeType, true);
				  mqlLogRequiredInformationWriter("New Feature Change Type :: "+ newFeatureChangeType +"\n");
				  mqlLogRequiredInformationWriter("New Feature Policy 	   :: "+ mNewFeaturePolicy +"\n");

				  String newFeaturePolicy = (String) mNewFeaturePolicy.get(ConfigurationConstants.SELECT_NAME);
				  //change the feature to new type
				  //BusinessObject featureBO = changeType(context,featObjMap,featureObjId,newFeatureChangeType,newFeaturePolicy);
				  BusinessObject boFeatureObj  = new DomainObject(featureObjId);
				  boFeatureObj.change(context,
						  											newFeatureChangeType,
																	(String) featObjMap.get(DomainConstants.SELECT_NAME),
																	(String) featObjMap.get(DomainConstants.SELECT_REVISION),
																	(String) featObjMap.get(DomainConstants.SELECT_VAULT),
																	newFeaturePolicy);

				  mqlLogRequiredInformationWriter("Object id 				:: "+featureObjId +"\n");
		  		  mqlLogRequiredInformationWriter("Object changed from type :: "
		  											+ strType
		  											+ " to new type "
		  											+ newFeatureChangeType
		  										    + " new policy "
		  										    + newFeaturePolicy
		  											+ "\n");


				  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT,
						  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]"));
				  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME,
						  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"));
				  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT,
						  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT+"]"));
				  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_ORIGINATOR,
						  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_ORIGINATOR+"]"));
				  if(newFeatureChangeType!=null &&
						  (newFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE)
								  ||newFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_MANUFACTURING_FEATURE)
								  )){
					  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML,
							  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML+"]"));
				  }
				  //Set the new Feat Type attribute values when Feat is standalone
				  mqlLogRequiredInformationWriter("Object id 					   :: "+ featureObjId + "\n");
				  mqlLogRequiredInformationWriter("Attribute value set for this id :: "+ mapAttribute +"\n");
				  domFeatureObj.setAttributeValues(context,mapAttribute);
				  }else{
				      Map mNewFeaturePolicy = mxType.getDefaultPolicy(context, strType, true);
				      mqlLogRequiredInformationWriter("New Feature Change Type 		:: "+ strType +"\n");
					  mqlLogRequiredInformationWriter("New Feature Policy 			:: "+ mNewFeaturePolicy +"\n");

					  String newFeaturePolicy = (String) mNewFeaturePolicy.get(ConfigurationConstants.SELECT_NAME);
					  DomainObject boFeatureObj  = new DomainObject(featureObjId);
					  boFeatureObj.setPolicy(context, newFeaturePolicy);
					  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT,
							  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]"));
					  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME,
							  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"));
					  domFeatureObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT,
							  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]"));
					  //Set the attribute values for "software Feature" when Feat is standalone
					  mqlLogRequiredInformationWriter("Object id :: "+ featureObjId + "\n");
					  mqlLogRequiredInformationWriter("Attribute value set for this id :: "+ mapAttribute +"\n");
					  domFeatureObj.setAttributeValues(context,mapAttribute);
				  }
			  }

		  }else //if feature is not standalone
		  {
			  MapList FLInfoList = new MapList(); // FLs info MapList to pass to external migrations
			  
			  mqlLogRequiredInformationWriter("\n\n");
			  mqlLogRequiredInformationWriter("For given Feature id there are  'Feature List From' and 'Feature List To' connections."+"\n");
			  mqlLogRequiredInformationWriter("Traverse through the given list of 'Feature List' object start ."+"\n\n");

			  for(int i=0;i<FL_List.size();i++)
			  {
				  Map flMap = (Map)FL_List.get(i);
				  String strFLId = (String)flMap.get(DomainConstants.SELECT_ID);
				  mqlLogRequiredInformationWriter("Feature List id in process :: "+ strFLId +"\n");

				  try{
					  String strRelType = (String)flMap.get("relationship");
					  if(strRelType!=null && strRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CANDIDATE_ITEM)){

						  mqlLogRequiredInformationWriter("Feature List id related 'Candiadate Item' rel in process ---> relName "+ strRelType +"\n");
						  String strNewRelType =  "";
						  //String strManAttr =(String) flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");
						  //Use case related to Candidate Item
						  String strCandItemRel = (String)flMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
						  DomainRelationship domrel = new DomainRelationship(strCandItemRel);
						  Map attributeMap = new HashMap();
						  attributeMap = domrel.getAttributeMap(context,true);
						  String strManAttr = (String) attributeMap.get(ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE);
						  String strInheritedAttr = (String) attributeMap.get(ConfigurationConstants.ATTRIBUTE_INHERITED);
						  String newFeatureChangeType ="";

  						 if(strType!=null
  							 &&!mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
  					         &&!mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
  					         &&!mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))
  						 {
  							  //Get the new Feature Type
  							  String newFeatureType = (String)featObjMap.get("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");
  							  //String newFeatureChangeType = PropertyUtil.getSchemaProperty(context,newFeatureType);
  							  newFeatureChangeType = getSchemaProperty(context,newFeatureType);

  						  						  //Get the new Feature Policy
  						  						  Map mNewFeaturePolicy = mxType.getDefaultPolicy(context, newFeatureChangeType, true);
  						  						  mqlLogRequiredInformationWriter("New Feature Change Type :: "+newFeatureChangeType +"\n");
  						  						  mqlLogRequiredInformationWriter("New Feature Policy :: "+mNewFeaturePolicy +"\n");

  						  						  String newFeaturePolicy = (String) mNewFeaturePolicy.get(ConfigurationConstants.SELECT_NAME);

  						  						  //change the feature to new type
  						  						  BusinessObject boFeatureObj  = new DomainObject(featureObjId);


  						  						  boFeatureObj.change(context,
  						  								  											newFeatureChangeType,
  						  																			(String) featObjMap.get(DomainConstants.SELECT_NAME),
  						  																			(String) featObjMap.get(DomainConstants.SELECT_REVISION),
  						  																			(String) featObjMap.get(DomainConstants.SELECT_VAULT),
  						  																			newFeaturePolicy);

  						  						  mqlLogRequiredInformationWriter("Object id :: "+featureObjId +"\n");
			  						  			  mqlLogRequiredInformationWriter("Object changed from type :: "
			  						  											+ strType
			  						  											+ " to new type "
			  						  											+ newFeatureChangeType
			  						  										    + " new policy "
			  						  										    + newFeaturePolicy
			  						  											+ "\n");

  						  						  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT,
  						  								  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]"));
  						  						  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME,
  						  								  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]"));
  						  						  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT,
  						  								  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT+"]"));
  						  						  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_ORIGINATOR,
  						  								  (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_ORIGINATOR+"]"));

  						  						  if(newFeatureChangeType!=null
  						  							 && (newFeatureChangeType.equalsIgnoreCase(TYPE_CONFIGURATION_FEATURE))
  						  							 && (strManAttr!=null && strManAttr.equalsIgnoreCase("Yes"))
  						  							 && (strInheritedAttr!=null && strInheritedAttr.equalsIgnoreCase("False")))
  						  					      {

  						  							  strNewRelType = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;

  						  						  }

  						  						if(newFeatureChangeType!=null
  							  							 && (newFeatureChangeType.equalsIgnoreCase(TYPE_CONFIGURATION_FEATURE))
  							  							 && (strManAttr!=null && strManAttr.equalsIgnoreCase("No")))
  							  					 {
  							  							   strNewRelType = ConfigurationConstants.RELATIONSHIP_CANDIDTAE_CONFIGURATION_FEATURES;
  							  					 }

  						  						  if(newFeatureChangeType!=null
  						  								   &&(newFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE)
  						  									   ||newFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_MANUFACTURING_FEATURE)))
  						  						  {
  						  							  strNewRelType = ConfigurationConstants.RELATIONSHIP_CANDIDTAE_LOGICAL_FEATURES;
  						  							  mapAttribute.put(ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML,
  						  							 (String)featObjMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML+"]"));
  						  						  }

  						  						  //Set the new Feat Type attribute values when Feat is standalone
  						  						  if(ProductLineCommon.isNotNull(strNewRelType)
  						  							 && !strNewRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)){

  						  							  mqlLogRequiredInformationWriter("Object id :: "+ featureObjId + "\n");
  						  							  mqlLogRequiredInformationWriter("Attribute value set for this id :: "+ mapAttribute +"\n");
  						  							  domFeatureObj.setAttributeValues(context,mapAttribute);
  							  						  DomainRelationship.setType(context, strCandItemRel,strNewRelType);
  						  						  }

  						  }else{

  							newFeatureChangeType = strType;

  						  }


					  						/*  if(strNewRelType!=null && strNewRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)){
					  							  HashMap mapRelAttributes = new HashMap();


					   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,
					   									  	ConfigurationConstants.RANGE_VALUE_SYSTEM);

					   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_INHERITED, strInheritedAttr);

					   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,
					   									  	ConfigurationConstants.RANGE_VALUE_FALSE);

					   							   domrel.setAttributeValues(context, mapRelAttributes);

					  						  }*/

					  						  if(newFeatureChangeType!=null
						  							 && (newFeatureChangeType.equalsIgnoreCase(TYPE_CONFIGURATION_FEATURE))
						  							 && (strManAttr!=null && strManAttr.equalsIgnoreCase("Yes"))
						  							 && (strInheritedAttr!=null && strInheritedAttr.equalsIgnoreCase("True")))
						  					      {
					  							     DomainRelationship.disconnect(context, strCandItemRel);
						  						  }


					  }else if(strRelType!=null && strRelType.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO)){

					  mqlLogRequiredInformationWriter("Feature List id related 'Feature list To' rel in process ---> relName "+ strRelType +"\n\n");
						  //Get the "From Side" info of FL object
					  String strParentFeatureId ="";
					  String strParentFeatureType ="";
					  
					  if((String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id")!=null){
						  strParentFeatureId = (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id");
						  strParentFeatureType = (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type");
					  }else if((String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.id")!=null){
						  strParentFeatureId = (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.id");
						  strParentFeatureType = (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].from.type");
					  }else if((String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id")!=null){
						  strParentFeatureId = (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
						  strParentFeatureType = (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.type");
					  }else if((String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_COMMITED_ITEM+"].from.id")!=null){
					  }

					//Use Case related to hanging FL Objects
					  if(strParentFeatureId!=null && strParentFeatureId.length()!=0){
					  String isConflictParentFeature ="No";
					  DomainObject domParentFeat = new DomainObject(strParentFeatureId);
					  Map htParentObjData = (Map) domParentFeat.getInfo(context, featureObjSelects);

					  if(mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_FEATURES)){

						  isConflictParentFeature = (String)htParentObjData.get("attribute["+ATTRIBUTE_FTRMigrationConflict+"]");

					  }


					  //Both the side Objects of FL should be convertible
         			  if(isConflictFeature.equalsIgnoreCase("No")&& isConflictParentFeature.equalsIgnoreCase("No")){

					  /* First ...Check if Parent is already converted to New Type or not
					   * If Not then convert it to the new Type then continue
					   * Attributes will be set later ..whenever the Feature comes as child in the list.*/
					  if(strParentFeatureType!=null
						 && !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
						 && !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
						 && !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)
						 && !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_PRODUCTS)
						 && !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_PRODUCT_LINE)){

						 //Get the new Feature Type
						  String newParentFeatureType = (String)htParentObjData.get("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");


						  //String newParentFeatureChangeType = PropertyUtil.getSchemaProperty(context,newParentFeatureType);
						  String newParentFeatureChangeType = getSchemaProperty(context,newParentFeatureType);

						  //Get the new Feature Policy
						  Map mNewParentFeatPolicy = mxType.getDefaultPolicy(context, newParentFeatureChangeType, true);
						  String strNewParentFeatPolicy = (String) mNewParentFeatPolicy.get(ConfigurationConstants.SELECT_NAME);

						  mqlLogRequiredInformationWriter("Feature New Type   :: "+ newParentFeatureChangeType +"\n");
	  					  mqlLogRequiredInformationWriter("Feature New Policy :: "+ strNewParentFeatPolicy +"\n");

	  					String MarketTextPar = (String)htParentObjData.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]");
	  				    String MarketNamePar = (String)htParentObjData.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");
	  				    
	  				  
	  					  
	  					//Set the necessary Text and name attribute values of new Type Parent Feature object which will be lost once type is changed below.
	  					  HashMap mapParentFeatAttribute = new HashMap();
	  					if(MarketTextPar!=null && !MarketTextPar.equals("")){
	  					mapParentFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT, MarketTextPar);
	  					}
	  					if(MarketNamePar!=null && !MarketNamePar.equals("")){
	  					mapParentFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME, MarketNamePar);

	  					}
	  					
						  //change the feature to new type
						  BusinessObject boParFeatureObj  = new DomainObject(strParentFeatureId);

						  boParFeatureObj.change(context,
								  												  newParentFeatureChangeType,
																			      (String) htParentObjData.get(DomainConstants.SELECT_NAME),
																			      (String) htParentObjData.get(DomainConstants.SELECT_REVISION),
																			      (String) htParentObjData.get(DomainConstants.SELECT_VAULT),
																			      strNewParentFeatPolicy);


						  mqlLogRequiredInformationWriter("Object id :: "+strParentFeatureId +"\n");
				  		  mqlLogRequiredInformationWriter("Object changed from type :: "
				  											+ strType
				  											+ " to new type "
				  											+ newParentFeatureChangeType
				  										    + " new policy "
				  										    + strNewParentFeatPolicy
				  											+ "\n");
				  		  
				  		  
				  		DomainObject domainParentFeat  = new DomainObject(strParentFeatureId);
						mqlLogRequiredInformationWriter("Attribute value Map set for this id ---->" + "\n" + mapParentFeatAttribute +"\n\n");
						domainParentFeat.setAttributeValues(context,mapParentFeatAttribute);
					  }

					  String newChildFeatureChangeType ="";
					  //Get the new Feature Relationship
					   String newReltoConnect = "";

					  if(strType!=null
							     && !mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
								 && !mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
								 && !mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)
								 ){

						//Check whether "To Side" i.e. Child Feature is CF/LF/MF.. Do the processing
						  //newChildFeatureChangeType = PropertyUtil.getSchemaProperty(context,strContextUsage);
						  newChildFeatureChangeType = getSchemaProperty(context,strContextUsage);

						  if(newChildFeatureChangeType!=null && newChildFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_CONFIGURATION_OPTION)){
							  newReltoConnect = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS;

						  }else if(newChildFeatureChangeType!=null && newChildFeatureChangeType.equalsIgnoreCase(TYPE_CONFIGURATION_FEATURE)){

								//Varies By,Valid Context,Invalid Context related code
								  newReltoConnect = getNewRelForConfigFeatType(context,flMap,featObjMap,strParentFeatureId,strParentFeatureType);

						  }else if(newChildFeatureChangeType!=null
								  && (newChildFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE)
										  || newChildFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_SOFTWARE_FEATURE))){

							  newReltoConnect = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;

						  }else if(newChildFeatureChangeType!=null && newChildFeatureChangeType.equalsIgnoreCase(ConfigurationConstants.TYPE_MANUFACTURING_FEATURE)){
							  newReltoConnect = ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES;

						  }

					  }else{
						  if(strType!=null && (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_CONFIGURATION_OPTION))){
							  newReltoConnect = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS;

						  }else if(strType!=null && strType.equalsIgnoreCase(TYPE_CONFIGURATION_FEATURE)){

							  newReltoConnect = getNewRelForConfigFeatType(context,flMap,featObjMap,strParentFeatureId,strParentFeatureType);

						  }else if(strType!=null
								  && (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
										  || mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_SOFTWARE_FEATURE)))
						    {
							  newReltoConnect = ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES;

						  }else if(strType!=null && (mxType.isOfParentType(context,strType, ConfigurationConstants.TYPE_MANUFACTURING_FEATURE))){

							  newReltoConnect = ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES;
						  }

					  }

						   //Set attribute values for "To Side" of the "Feature List To" relationship i.e. Child Feat Obj
						   String strSelCriterion = setAttributeValuesOfChildFeatureObj(context,flMap,featObjMap,bConflictSelType);

   					  //Do the connection between 2 Feature Objects

   					  //restricting the connection if FST = Key-In and Key-In Type = Input
   					  String strFSTAttrValue = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_SELECTION_TYPE+"]");
   					  String strKITAttrValue = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE+"]");
   					  if(strKITAttrValue.equals(RANGE_VALUE_INPUT)
   						 && strFSTAttrValue.equals(ConfigurationConstants.RANGE_VALUE_KEY_IN)
   						 &&  (newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES)||
   							   newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS)))
   					  {
   						  StringList slPCIds = new StringList();
   						  StringList slSOAttrKIVs = new StringList();
   						  StringList slParentPCIds = new StringList();
   						  StringList slParentSORelIds = new StringList();

   						  Object objPCIds = flMap.get("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");
   						  if (objPCIds instanceof StringList) {
   							  slPCIds = (StringList) flMap.get("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id");

   							} else if (objPCIds instanceof String) {
   								slPCIds.addElement((String) flMap.get("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id"));
   							}


   						  Object objSOAttrKIVs = flMap.get("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE+"]");
   						  if (objSOAttrKIVs instanceof StringList) {
   							  slSOAttrKIVs = (StringList) flMap.get("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE+"]");

   							} else if (objSOAttrKIVs instanceof String) {
   								slSOAttrKIVs.addElement((String) flMap.get("to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE+"]"));
   							}

   						  String strParentPCIdSel = "to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id";
   						  Object objParentPCIds = flMap.get(strParentPCIdSel);

   						  if(objParentPCIds==null || "".equals(objParentPCIds)){
   							strParentPCIdSel ="to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].from.id";
   							objParentPCIds = flMap.get(strParentPCIdSel);
   						  }

   						  if (objParentPCIds instanceof StringList) {
   							  slParentPCIds = (StringList) flMap.get(strParentPCIdSel);

   							} else if (objParentPCIds instanceof String) {
   								slParentPCIds.addElement((String) flMap.get(strParentPCIdSel));
   							}																		//to[Feature List From].from.to[Feature List To].from.to[Selected Options].from.type

   						  String strParentSORelIdSel = "to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.to["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id";
   						  Object objParentSORelIds = flMap.get(strParentSORelIdSel);
   						  if(objParentSORelIds==null || "".equals(objParentSORelIds)){
   							strParentSORelIdSel = "to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.to["+ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES+"].tomid["+ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS+"].id";
   							  objParentSORelIds = flMap.get(strParentSORelIdSel);

   						  }

   						  if (objParentSORelIds instanceof StringList) {
   							  slParentSORelIds = (StringList) flMap.get(strParentSORelIdSel);

   							} else if (objParentSORelIds instanceof String) {
   								slParentSORelIds.addElement((String) flMap.get(strParentSORelIdSel));
   							}

   						  //VJB: TODO: seems a bug, int j is never used
   						  for(int j=0;slPCIds!=null && i<slPCIds.size();i++ )
   						  {
   							  String strPCId = (String)slPCIds.get(i);
   							  String strAttrKIV = (String)slSOAttrKIVs.get(i);
   							  if(slParentPCIds!=null && slParentPCIds.contains(strPCId))
   							  {
   								  for(int k=0;k<slParentPCIds.size();k++)
   								  {
   									  String strParentPCId = (String)slParentPCIds.get(k);
   									  if(strParentPCId.equals(strPCId))
   									  {
   										  String strParentSORelId = (String)slParentSORelIds.get(k);
   										  DomainRelationship domRel = new DomainRelationship(strParentSORelId);
   										  domRel.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_KEY_IN_VALUE, strAttrKIV);
   										  break;
   									  }
   								  }
   							  }
   						  }

   					  }
   					else
   					   {
   						//strNewRelId = connectFeaturesWithNewRel(context,strParentFeatureId,featureObjId,strType,newReltoConnect);
   						strNewRelId = connectFeaturesWithNewRel(context,htParentObjData,featureObjId,strType,newReltoConnect);

   					   }



   					  //Migrate attributes from FL to Rel id
   					  HashMap mapRelAttributes = new HashMap();
   					  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME,
   							  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME+"]"));
   					  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CHILD_OBJECT_NAME,
   							  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_CHILD_OBJECT_NAME+"]"));
   					  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME,
   							  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME+"]"));
   					  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME,
   							  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME+"]"));
   					  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_RULE_TYPE,
   							  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_RULE_TYPE+"]"));


   					  if(newReltoConnect!=null
            				 && (newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES)
   						     || newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_MANUFACTURING_FEATURES))){

   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_COMPONENT_LOCATION+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_FIND_NUMBER,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_FIND_NUMBER+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_FORCE_PART_REUSE+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_QUANTITY,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_QUANTITY+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_REFERENCE_DESIGNATOR+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_USAGE,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_USAGE+"]"));
   						  }

   					 if(newReltoConnect!=null
               			&& newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES)){

   						  mapRelAttributes.put(ATTRIBUTE_LOGICAL_SELECTION_CRITERIA,strSelCriterion);
       				  }


   					  if(newReltoConnect!=null
   	    				 && newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_OPTIONS)){

   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER,
   								  (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_LIST_PRICE,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_LIST_PRICE+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY+"]"));
   					  }

   					  //Configuration Feature relationship
   					  if(newReltoConnect!=null
   						  && newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES)){

   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER,
   								  (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_LIST_PRICE,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_LIST_PRICE+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY+"]"));
   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY,
   								  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY+"]"));


   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_CRITERIA,strSelCriterion);


   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,
   								  	ConfigurationConstants.RANGE_VALUE_SYSTEM);

   						  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_INHERITED,
   								    (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]"));

   						  //mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_FALSE);
   					  }

   					  //Mandatory Configuration Feature relationship
   					  if(newReltoConnect!=null
   							  && newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES)){

   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER,
   									  (String)flMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].attribute["+ConfigurationConstants.ATTRIBUTE_SEQUENCE_ORDER+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_LIST_PRICE,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_LIST_PRICE+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MAXIMUM_QUANTITY+"]"));
   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY,
   									  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MINIMUM_QUANTITY+"]"));


   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,
   									  	ConfigurationConstants.RANGE_VALUE_SYSTEM);

   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_INHERITED,
   									    (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]"));

   							  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_FALSE);

   					   }


   					  //"Varies By" and "Inactive Varies By" relationship
   					  if(newReltoConnect!=null
   						&&
   						(newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_VARIES_BY)
   						 ||newReltoConnect.equalsIgnoreCase(ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY)))
   					   {
						  if((String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]")!=null
									&&
							 ((String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]")).equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_YES)){


							          mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,
											  				ConfigurationConstants.RANGE_VALUE_MANDATORY);

									  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_INHERITED,
											  (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]"));

									  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,
								  				ConfigurationConstants.RANGE_VALUE_TRUE);

								  }else if((String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]")!=null
											&&
											((String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]")).equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NO)){


									  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,ConfigurationConstants.RANGE_VALUE_SYSTEM);
									  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_INHERITED,ConfigurationConstants.RANGE_VALUE_FALSE);
									  mapRelAttributes.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_TRUE);

							      }
					     }
   					 
   					  if(strNewRelId!=null && strNewRelId.length()!=0){

   						  DomainRelationship domRel = new DomainRelationship(strNewRelId);
   						  mqlLogRequiredInformationWriter("Rel id :: "+ strNewRelId + "\n");
   						  mqlLogRequiredInformationWriter("Attribute value Map set for this Rel id :: "+ mapRelAttributes +"\n\n");
   						  domRel.setAttributeValues(context, mapRelAttributes);
   						  //preparing MapList to pass to external migration
   						  Map FLInfoMap = new HashMap();
   						  FLInfoMap.put("objectId",strFLId);
   						  FLInfoMap.put("newRelId",strNewRelId);

   						  DomainRelationship domRel1 = new DomainRelationship(strNewRelId);
   						  domRel1.open(context);
   					      String connectionName = domRel1.getTypeName();
   					      FLInfoMap.put("newRelType",connectionName);
   						  FLInfoMap.put("newRelName",newReltoConnect);
   						  FLInfoMap.put("infoMapFLConnection",flMap);

   						  FLInfoList.add(FLInfoMap);
   					  }

						  //Add FL object to delete list
						  sLFLToDelete.add(strFLId);

					  }
   				  }else{
   						  //This is hanging FL Object ID need to b removed
   						  sLFLToDelete.add(strFLId);
   						  Map FLInfoMap = new HashMap();
   						  FLInfoMap.put("objectId",strFLId);
   						  FLInfoMap.put("infoMapFLConnection",flMap);
   						  FLInfoList.add(FLInfoMap);
   					  }
   				  }

   			  }catch(Exception e)
   			  {
   				  e.printStackTrace();
   				  throw new FrameworkException(e.getMessage());
   			  }
   	      }

   			//Float the connections on FL objects to new Relationships formed
   			  if(strNewRelId!=null && strNewRelId.length()!=0){

   				  floatFLConnections(context,FLInfoList);
   			  }


   			//call external migration
   			MapList customResults = callInterOpMigrations(context, FLInfoList);
   			Iterator itrCustomResults = customResults.iterator();
   			while (itrCustomResults.hasNext()) {
                     Map customResultsMap = (Map)itrCustomResults.next();
   				  Integer status = (Integer)customResultsMap.get("status");
				  String failureMessage = (String)customResultsMap.get("failureMessage");

				  if(status==1)
				  {
					  throw new FrameworkException(failureMessage);
				  }
			  }
		  }


		//Check the "Varies By"   and "Effectivity Status" values
		  StringList sLVariesByRelId =new StringList();
		  StringList sLEffectivityStatusValue=new StringList();
		  StringList sLActiveCntValue=new StringList();
		  StringList sLInActiveCntValue=new StringList();
		  StringList sLParType=new StringList();


		if((StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id")!=null){

			//sLName = (StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.name");
			//sLVariesByRelId = (StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");
			//sLEffectivityStatusValue = (StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");
			//sLActiveCntValue = (StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
			//sLInActiveCntValue = (StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");
			//sLParentId = (StringList)featObjMap.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.id");

			/*Not using the above statements as there is issue in sequence of ids in String list
			 * coming from above getInfo
			 * Doing the getInfo again on the given Feature Id
			 */

			String stFeaId = (String) featObjMap.get(SELECT_ID);
			DomainObject domFd = new DomainObject(stFeaId);
			StringList objSel = new StringList();
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");//Varies By rel id if present
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.id");//Varies By rel id if present
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.name");//Varies By rel id if present
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.type");//Varies By rel id if present

			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");//Varies By rel id if present
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_INAVLID_CONTEXTS+"]");//Varies By rel id if present
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_USER_DEFINED_EFFECTIVITY+"]");//Varies By rel id if present

			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");//Varies By rel id if present
			objSel.addElement("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");//Varies By rel id if present

			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.id");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.name");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.type");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_INAVLID_CONTEXTS+"]");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");
			  DomainConstants.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_USER_DEFINED_EFFECTIVITY+"]");


			  Map MFeatObj = domFd.getInfo(context, objSel);


			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.id");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.name");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.type");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_INAVLID_CONTEXTS+"]");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");
			  DomainConstants.MULTI_VALUE_LIST.remove("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ATTRIBUTE_USER_DEFINED_EFFECTIVITY+"]");


			sLVariesByRelId = (StringList) MFeatObj.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].id");
			sLParType= (StringList) MFeatObj.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].from.type");
			sLEffectivityStatusValue= (StringList) MFeatObj.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_EFFECTIVITY_STATUS+"]");
			sLActiveCntValue= (StringList) MFeatObj.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
			sLInActiveCntValue=(StringList) MFeatObj.get("to["+ConfigurationConstants.RELATIONSHIP_VARIES_BY+"].attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");

		}

		HashMap mapRelAtt = null;

		for(int iEffStatusCnt=0;iEffStatusCnt<sLEffectivityStatusValue.size();iEffStatusCnt++){
			mapRelAtt = new HashMap();
			String strEffectStatus = (String)sLEffectivityStatusValue.get(iEffStatusCnt);
			if(strEffectStatus!=null && strEffectStatus.equalsIgnoreCase(ConfigurationConstants.EFFECTIVITY_STATUS_INACTIVE)){
				DomainRelationship.setType(context, (String)sLVariesByRelId.get(iEffStatusCnt), ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY);
				mapRelAtt.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_TYPE,ConfigurationConstants.RANGE_VALUE_SYSTEM);
				mapRelAtt.put(ConfigurationConstants.ATTRIBUTE_INHERITED,ConfigurationConstants.RANGE_VALUE_FALSE);
			}


			String strInActiveCntValue = (String)sLInActiveCntValue.get(iEffStatusCnt);
			int inactiveCount = Integer.parseInt(strInActiveCntValue);

			String strActiveCntValue = (String)sLActiveCntValue.get(iEffStatusCnt);
			int activeCount = Integer.parseInt(strActiveCntValue);

			if( inactiveCount==0 && activeCount==0 ){
				String strParType = (String)sLParType.get(iEffStatusCnt);
				if(mxType.isOfParentType(context,strParType, ConfigurationConstants.TYPE_PRODUCTS)){
					mapRelAtt.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_TRUE);
				}else{
					mapRelAtt.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_FALSE);
				}
			}else{
					mapRelAtt.put(ConfigurationConstants.ATTRIBUTE_ROLLED_UP,ConfigurationConstants.RANGE_VALUE_TRUE);
			}
			DomainRelationship domRel = new DomainRelationship((String)sLVariesByRelId.get(iEffStatusCnt));
			mqlLogRequiredInformationWriter("Rel id :: "+(String)sLVariesByRelId.get(iEffStatusCnt)+ "\n");
			mqlLogRequiredInformationWriter("Attribute value Map set for this Rel id :: "+ mapRelAtt +"\n\n");
			domRel.setAttributeValues(context, mapRelAtt);
		}


		//Cleanup - delete related Feature List objects on successful migration of Feature
		  deleteObjects(context,sLFLToDelete);

		  loadMigratedOids(featureObjId);

	  }else{ // for those Features which has "Conflict" stamp "Yes"
		  String strCommand = strType + "," + strName + "," + strRevision + "," + strContextUsage+"\n";
			  writeUnconvertedOID(strCommand, featureObjId);
	  }

    }

    /**
	 * To set attribute values of the "To Side" of the "Feature List To" relationship i.e. Child Feat Obj
     * @param context the eMatrix <code>Context</code> object
     * @param flMap "Feature List" related information
     * @param htChildObjData -- Child Feat Obj related information
     * @param iCntFLObjs -- Count of FL objects
     * @throws Exception
	 */

    private String  setAttributeValuesOfChildFeatureObj(Context context,Map flMap,Map htChildObjData,boolean bConflictSelType)
    throws Exception{

    	String strChildFeatureId = (String)htChildObjData.get(ConfigurationConstants.SELECT_ID);
    	String strChildFeatureType = (String)htChildObjData.get(ConfigurationConstants.SELECT_TYPE);

    	mqlLogRequiredInformationWriter("Inside method 'setAttributeValuesOfChildFeatureObj' " +"\n\n");
    	mqlLogRequiredInformationWriter("Feature List Info ---->"+ "\n" + flMap +"\n\n");
    	mqlLogRequiredInformationWriter("Child Object Data ---->"+ "\n" + htChildObjData +"\n\n");


    	String strChildNewFeatureType = strChildFeatureType;

		//Check if child is already converted to New Type
		if(strChildFeatureType!=null
		     && !mxType.isOfParentType(context,strChildFeatureType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
			 && !mxType.isOfParentType(context,strChildFeatureType, ConfigurationConstants.TYPE_LOGICAL_FEATURE)
			 && !mxType.isOfParentType(context,strChildFeatureType, ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)
			 && !mxType.isOfParentType(context,strChildFeatureType, ConfigurationConstants.TYPE_PRODUCTS)){

				String newChildFeatureType = (String)htChildObjData.get("attribute["+ATTRIBUTE_NEW_FEATURE_TYPE+"]");
				//String newChildFeatureChangeType = PropertyUtil.getSchemaProperty(context,newChildFeatureType);
				String newChildFeatureChangeType = getSchemaProperty(context,newChildFeatureType);


			  mqlLogRequiredInformationWriter("Child Object id          :: "+strChildFeatureId +"\n");
			  //Get the new Feature Policy
			  Map mNewChildFeatPolicy = mxType.getDefaultPolicy(context, newChildFeatureChangeType, true);
			  mqlLogRequiredInformationWriter("Child Feature New Type   :: "+ newChildFeatureChangeType +"\n");


			  String strNewChildFeatPolicy = (String) mNewChildFeatPolicy.get(ConfigurationConstants.SELECT_NAME);
			  mqlLogRequiredInformationWriter("Child Feature New Policy :: "+ strNewChildFeatPolicy +"\n\n");

			  //change the feature to new type
			  //BusinessObject childFeatureBO = changeType(context,htChildObjData,strChildFeatureId,newChildFeatureChangeType,strNewChildFeatPolicy);
			  BusinessObject boChildFeatureObj  = new DomainObject(strChildFeatureId);

			  boChildFeatureObj.change(context,
					  						 newChildFeatureChangeType,
											 (String) htChildObjData.get(DomainConstants.SELECT_NAME),
											 (String) htChildObjData.get(DomainConstants.SELECT_REVISION),
											 (String) htChildObjData.get(DomainConstants.SELECT_VAULT),
											  strNewChildFeatPolicy);


	  		  mqlLogRequiredInformationWriter("Child Object changed from type :: "
	  											+ strChildFeatureType
	  											+ " to new type "
	  											+ newChildFeatureChangeType
	  										    + " new policy "
	  										    + strNewChildFeatPolicy
	  											+ "\n");

			  strChildNewFeatureType = newChildFeatureChangeType;

		}

		if(strChildFeatureType!=null && isOfDerivationChangedType(context, strChildFeatureType)){
			  Map mNewFeaturePolicy = mxType.getDefaultPolicy(context, strChildFeatureType, true);
			  String newFeaturePolicy = (String) mNewFeaturePolicy.get(ConfigurationConstants.SELECT_NAME);
			  mqlLogRequiredInformationWriter("Child Object id          :: "+strChildFeatureId +"\n");
			  mqlLogRequiredInformationWriter("Child Feature New Type   :: "+ strChildNewFeatureType +"\n");
			  mqlLogRequiredInformationWriter("Child Feature New Policy :: "+ newFeaturePolicy +"\n\n");

			  DomainObject boFeatureObj  = new DomainObject(strChildFeatureId);
			  boFeatureObj.setPolicy(context, newFeaturePolicy);
		}
		  //Need to set the attribute values for the child object
		  String strFeatSelType = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_SELECTION_TYPE+"]");
		  String strSelType ="";
		  String strSelCriterion ="";

		  //Set the attribute values of new Type Child Feature object
		  HashMap mapChildFeatAttribute = new HashMap();
		  
		  
		  String MarketText = (String)htChildObjData.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_TEXT+"]");
		  String MarketName = (String)htChildObjData.get("attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");

		  
		  
		  if(MarketText!=null && !MarketText.equals("")){
		  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_VARIANT_DISPLAY_TEXT, MarketText);
		  }
		  if(MarketName!=null && !MarketName.equals("")){
		  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME, MarketName);
		  }
		  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT,
				  (String)htChildObjData.get("attribute["+ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT+"]"));
		  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_ORIGINATOR,
				  (String)htChildObjData.get("attribute["+ConfigurationConstants.ATTRIBUTE_ORIGINATOR+"]"));


		  if(strFeatSelType!=null && strFeatSelType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_KEY_IN)){
			  strSelType = ConfigurationConstants.RANGE_VALUE_SINGLE;  //Default values
			  strSelCriterion = ConfigurationConstants.RANGE_VALUE_MUST;
		  }else if(strFeatSelType!=null && strFeatSelType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MAY_SELECT_ONE_OR_MORE)){
			  strSelType = ConfigurationConstants.RANGE_VALUE_MULTIPLE;
			  strSelCriterion = ConfigurationConstants.RANGE_VALUE_MAY;
		  }else if(strFeatSelType!=null && strFeatSelType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MAY_SELECT_ONLY_ONE)){
			  strSelType = ConfigurationConstants.RANGE_VALUE_SINGLE;
			  strSelCriterion = ConfigurationConstants.RANGE_VALUE_MAY;
		  }else if(strFeatSelType!=null && strFeatSelType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MUST_SELECT_AT_LEAST_ONE)){
			  strSelType = ConfigurationConstants.RANGE_VALUE_MULTIPLE;
			  strSelCriterion = ConfigurationConstants.RANGE_VALUE_MUST;
		  }else if(strFeatSelType!=null && strFeatSelType.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_MUST_SELECT_ONLY_ONE)){
			  strSelType = ConfigurationConstants.RANGE_VALUE_SINGLE;
			  strSelCriterion = ConfigurationConstants.RANGE_VALUE_MUST;
		  }

		  //When Feature is used in more than one context with Conflict Sel Type
		  if(bConflictSelType){
				  //strSelType = getResourceProperty(context,"emxConfiguration.Migration.DefaultSelectionType");
				  strSelType = EnoviaResourceBundle.getProperty(context, "emxConfigurationMigration", Locale.US, "emxConfiguration.Migration.DefaultSelectionType");
		  }

		  if(strChildNewFeatureType!=null & mxType.isOfParentType(context,strChildNewFeatureType, ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)){
				  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_CONFIGURATION_SELECTION_TYPE,strSelType);
		  }

		  //For type "Configuration Feature" add "Key-In Type"
		  if(strChildNewFeatureType!=null && strChildNewFeatureType.equalsIgnoreCase(TYPE_CONFIGURATION_FEATURE)){


			  StringList slKITOnChildFLs = new StringList();
			  Object objKITOnChildFLs = flMap.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE+"]");
			  if (objKITOnChildFLs instanceof StringList) {
				  slKITOnChildFLs = (StringList) flMap.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE+"]");

				} else if (objKITOnChildFLs instanceof String) {
					slKITOnChildFLs.addElement((String) flMap.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.attribute["+ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE+"]"));
				}

			  if(slKITOnChildFLs.contains(RANGE_VALUE_INPUT))
			  {
				  String strKeyInType = RANGE_VALUE_INPUT;
				  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_KEY_IN_TYPE,strKeyInType);
			  }
		  }else if(strChildNewFeatureType!=null && strChildNewFeatureType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE)){
				  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_LOGICAL_SELECTION_TYPE,strSelType);
		  }

		  if(strChildNewFeatureType!=null
			  && (strChildNewFeatureType.equalsIgnoreCase(ConfigurationConstants.TYPE_LOGICAL_FEATURE)
				  || strChildNewFeatureType.equalsIgnoreCase(ConfigurationConstants.TYPE_MANUFACTURING_FEATURE))
				 ){

			  String strDupPartXML = (String)htChildObjData.get(ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML);
			  mapChildFeatAttribute.put(ConfigurationConstants.ATTRIBUTE_DUPLICATE_PART_XML,strDupPartXML);
		  }

		  //Set attribute values on Feature Object
		  DomainObject domChildFeat  = new DomainObject(strChildFeatureId);
		  mqlLogRequiredInformationWriter("Attribute value Map set for this id ---->" + "\n" + mapChildFeatAttribute +"\n\n");
		  domChildFeat.setAttributeValues(context,mapChildFeatAttribute);
		  return strSelCriterion;
    }



    /**
	 * This method gets the Relationship Type with which the "Configuration Feature" Object
	 * is to be connected with the Parent Object.
	 *
     * @param context the eMatrix <code>Context</code> object
     * @param flMap --"Feature List" related information
     * @param htChildObjData -- Child Feat Obj(Configurature Feature) related information
     * @param strParentFeatureId -- Parent Object Id
     * @param strParentFeatureType -- Parent Object Type
     * @throws Exception
	 */

    private String  getNewRelForConfigFeatType(Context context,Map flMap,Map htChildObjData,String strParentFeatureId,String strParentFeatureType)
    throws Exception{

    	String newReltoConnect="";
    	try{

    		String strInherited = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]");
    		String strMandatory = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");

    		//Need to check "Inactive Varies By" relationship
    		/*if(strInherited!=null && strInherited.equalsIgnoreCase("True")
    		    && strMandatory!=null && strMandatory.equalsIgnoreCase("No")){*/
    		/*This means CF is added to the Product due to add DV.. it is rolled up
    		 * Need to decide it should be "Varies By" or "Inactive Varies By" rel
    		 * Also simultaneously check for new "Valid Context" & "Invalid Context" relationship
    		 */
    		//Get the Product Id.. compute whether DV is Active /Inactive under this Product context
    		if(strParentFeatureType!=null
    				&& strParentFeatureType.equals(ConfigurationConstants.TYPE_PRODUCT_VARIANT)){

    			//Get the PFL Id from PV Id for connection
    			DomainObject domPVId = new DomainObject(strParentFeatureId);

    			StringBuffer sbRelPattern = new StringBuffer(50);
    			sbRelPattern.append(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST);

    			StringList sLRelSelects = new StringList();
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].id");
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.id");
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.id");
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.name");



    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].torel.to.from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "].to.type");
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "].to.type");

    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].torel.id");
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].frommid.type");
    			sLRelSelects.addElement("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].frommid["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY+ "].id");

    			DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].torel.to.from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "].to.type");
    			DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "].to.type");
    			DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].id");
    			DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].frommid["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY+ "].id");
    			DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.id");
    			DomainConstants.MULTI_VALUE_LIST.add("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.name");

    			Map featureListMapList = domPVId.getInfo(context,sLRelSelects);

    			DomainConstants.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].torel.to.from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "].to.type");
    			DomainConstants.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_VARIES_BY+ "].to.type");
    			DomainConstants.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].id");
    			DomainConstants.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].frommid["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY+ "].id");
    			DomainConstants.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.id");
    			DomainConstants.MULTI_VALUE_LIST.remove("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].to.from["+ ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+ "].to.name");


    			StringList sLInactiveVBy = new StringList();
    			sLInactiveVBy = (StringList)featureListMapList.get("from["+ ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+ "].frommid["+ ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY+ "].id");


    			//Check the PV id is present in "Invalid Contexts" attribute value

    			if(sLInactiveVBy!=null && !sLInactiveVBy.isEmpty()){

    				for(int j=0;j<sLInactiveVBy.size();j++){

    					String strRelInaVaBy = (String)sLInactiveVBy.get(j);
    					DomainRelationship.setType(context, strRelInaVaBy, ConfigurationConstants.RELATIONSHIP_INVALID_CONTEXT);

    				}
    			}
    			if(strInherited!=null && strInherited.equalsIgnoreCase("True")
    					&& strMandatory!=null && strMandatory.equalsIgnoreCase("No")){

    				//Varies by/ Inactive Varies By
    				if(flMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].id")!=null){
    					//DV is Inactive
    					newReltoConnect = ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY;
    				}else{
    					//DV is Active
    					newReltoConnect = ConfigurationConstants.RELATIONSHIP_VARIES_BY;
    				}
    			}else if(strInherited!=null && strMandatory.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_YES)){

    						 //MCF2: If Parent type is not eaqual to Product or Product Line , then change rel to "Configuration Feature"
						  if(!mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_PRODUCTS) &&
							 !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_PRODUCT_LINE)){

							   newReltoConnect = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES;
						  }else{
							  	newReltoConnect = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;
						  }

                      }else{
    				newReltoConnect = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES;
    			}
    		}else{
    			if(strInherited!=null && strInherited.equalsIgnoreCase("True")
    					&& strMandatory!=null && strMandatory.equalsIgnoreCase("No")){
    				//Varies by/Inactive Varies By
    				if(flMap.get("to["+ConfigurationConstants.RELATIONSHIP_INACTIVE_FEATURE_LIST_FROM+"].id")!=null){
    					//DV is Inactive
    					newReltoConnect = ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY;

    				}else{
    					//DV is Active
    					newReltoConnect = ConfigurationConstants.RELATIONSHIP_VARIES_BY;
    				}
    			}else{

    				//To check it should be "Varies by"/"Inactive Varies By"/"Configuration Feaures" rel
    				String attrActCount = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_ACTIVE_COUNT+"]");
    				String attrIncActCount = (String)flMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_INACTIVE_COUNT+"]");

    				int iattrActCount = 0;
    				iattrActCount = Integer.parseInt(attrActCount);

    				int iattrIncActCount = 0;
    				iattrIncActCount = Integer.parseInt(attrIncActCount);

    				if(iattrActCount > 0){
    					newReltoConnect = ConfigurationConstants.RELATIONSHIP_VARIES_BY;
    				}else if(iattrIncActCount>0){
    					newReltoConnect = ConfigurationConstants.RELATIONSHIP_INACTIVE_VARIES_BY;
    				}else{
                       if(strInherited!=null && strMandatory.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_YES)){

    					//MCF3: If Parent type is not eaqual to Product or Product Line , then change rel to "Configuration Feature"
  						  if(!mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_PRODUCTS) &&
  								 !mxType.isOfParentType(context,strParentFeatureType, ConfigurationConstants.TYPE_PRODUCT_LINE)){

  								   newReltoConnect = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES;

  							  }else{
  								  	newReltoConnect = ConfigurationConstants.RELATIONSHIP_MANDATORY_CONFIGURATION_FEATURES;
  							  }
                             }else{
                                    newReltoConnect = ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES;
                             }
                        }
    				}
    		}
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	return newReltoConnect;
    }

	/**
	 * Used for set to Relation
	 * @param context
	 * @param relID -- relationship Id for modification
	 * @param targetrelationshipId --replace relationship Id
	 * @param isFrom -- "true" for from side
	 * @return
	 * @throws FrameworkException
	 */

	public String setToRelationship(Context context,
			java.lang.String relID, String targetrelationshipId,
    		boolean isFrom)throws Exception
    {

    	String connId = null;
    	//StringBuffer sbCmd = new StringBuffer();
    	StringBuffer sbCmd2 = new StringBuffer();

        try
        {
        	//sbCmd.append("mod connection \"");
        	sbCmd2.append("mod connection $1 ");
        	//sbCmd.append(relID);
        	if(isFrom)
        	{
        		//sbCmd.append("\" fromrel \"");
        		sbCmd2.append(" fromrel $2 ");
            	//sbCmd.append(targetrelationshipId);
        	}
        	else
        	{
        		//sbCmd.append("\" torel \"");
        		sbCmd2.append(" torel $2 ");
            	//sbCmd.append(targetrelationshipId);

        	}
        	//sbCmd.append("\" select id dump;");
        	sbCmd2.append(" select $3 dump");
        	connId = MqlUtil.mqlCommand(context, sbCmd2.toString(), true,relID,targetrelationshipId,"id");
        }
        catch (Exception e)
        {
            throw new FrameworkException(e);
        }

		return connId;
    }
    

    String getSchemaProperty(Context context,String sSymbolicName) throws Exception{

    	//String strResults = MqlUtil.mqlCommand(context,"print program eServiceSchemaVariableMapping.tcl select property["+ sSymbolicName +"] dump |;");
    	String strMQLCommand="print program $1 select $2 dump $3";
		String strResults = MqlUtil.mqlCommand(context,strMQLCommand,"eServiceSchemaVariableMapping.tcl","property["+ sSymbolicName +"]","|");

        StringTokenizer token = new StringTokenizer(strResults,"|");
        String val = null;
        while (token.hasMoreTokens()){
            String preParse = token.nextToken();

		    //property returned as 'relationsip_xyz to relationship xyz'
		    int toIndex = preParse.indexOf(" to ");
		    if (toIndex > -1){

			//split on " to "
			val = preParse.substring(toIndex+4,preParse.length());
			if (val != null){
			    val.trim();

			    //split on space and place result in hashtable
			    val = val.substring(val.indexOf(' ')+1,val.length());
			 }
		    }
		}

    	return val;
    }

           
    	 	 /**
      	     * This method is executed to remove the  interface temporary added on objects during Migration
      	     *
      	     * @param context
      	     * @param sLFeatAndRuleIds --Stringlist of Feature and Rule Ids on which the interface was added.
      	     * @return Returns boolean true  : If it is executed for all given objects
      	     * 						   false : If it is not executed for all given objects
      	     *  							   (In case interface doesnt exist on the given object)
      	     * @throws Exception
      	     */
    	 	 private void  removalOfTemporaryAttSet(Context context, StringList sLFeatAndRuleIds)
    	     throws Exception{

    	 		StringBuffer sbTypePattern = new StringBuffer(50);
    			sbTypePattern.append(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES);
    			sbTypePattern.append(",");
    			sbTypePattern.append(ConfigurationConstants.TYPE_CONFIGURATION_FEATURES);
    			sbTypePattern.append(",");
    			sbTypePattern.append(ConfigurationConstants.TYPE_PRODUCTS);
    			sbTypePattern.append(",");
    			sbTypePattern.append(ConfigurationConstants.TYPE_RULE);

    			StringBuffer sbWhereClause = new StringBuffer(50);
    			sbWhereClause.append("(");
    			sbWhereClause.append("interface[FTRIntermediateObjectMigration] == \"" + ConfigurationConstants.RANGE_VALUE_TRUE + "\"");
    			sbWhereClause.append(")");

    			StringList objectSelects = new StringList();
    			objectSelects.add(SELECT_TYPE);
    			objectSelects.add("interface");
                objectSelects.add(SELECT_NAME);
                objectSelects.add(SELECT_REVISION);
                objectSelects.add(SELECT_ID);

    	 		MapList MapListObjects = DomainObject.findObjects(context, // context
    	 												sbTypePattern.toString(), // typePattern
									  					"*", // vaultPattern
									  					sbWhereClause.toString(), // whereExpression
									  					objectSelects); // objectSelects


    	 		StringList sLObjIds = new StringList();
    	 		for(int i=0;i<MapListObjects.size();i++){
    	 			  Map mObj =  (Map) MapListObjects.get(i);
    	 			  String strObjId = (String) mObj.get(SELECT_ID);
    	 			  sLObjIds.add(strObjId);
    	 		}



    	 		//Intersection of 2 lists
    	 		sLObjIds.retainAll(sLFeatAndRuleIds);


    	 		 try {
					for(int i=0;i<sLObjIds.size();i++){
						 String strObjId = (String) sLObjIds.get(i);
						 //String strMqlCommand = "modify bus " + strObjId + " remove interface" + " " + INTERFACE_FTRIntermediateObjectMigration;
						 String strMqlCommand = "modify $1 $2 remove interface $3";
						 MqlUtil.mqlCommand(context,strMqlCommand, "bus", strObjId, INTERFACE_FTRIntermediateObjectMigration);
					 }
				} catch (Exception e) {
					e.printStackTrace();
				}

    	     }


    	 	 /**
    	      * This is used to encode selected string - Bug No. 361962
    	      * @param context
    	      * @param strObjId
    	      * @param strSelectedFeatures
    	      * @param strParams
    	      * @param featureType
    	      * @return String
    	      * @throws Exception
    	      */
    	     public static String stringDecode(Context context,String  strFeatureName)
    	             throws Exception {
    	       
    	       if(strFeatureName.indexOf("&")>-1){

    	    	     strFeatureName = strFeatureName.replaceAll("&","&amp;");

    	        }else if(strFeatureName.indexOf("<")>-1){

    	        	 strFeatureName = strFeatureName.replaceAll("<","&lt;");

    	        }else if(strFeatureName.indexOf(">")>-1){

    	        	strFeatureName = strFeatureName.replaceAll(">","&gt;");
    	        }

    	       return strFeatureName;
    	     }


    	     /**
    	 	 * This is an Utility method to add Interface to Business Object or Relationship and set the Interface Attributes
    	 	 *
    	 	 * @param context
    	 	 * @param strId - Object Id or connection Id on which Interface to be added
    	 	 * @param strAdmin - Interface to be added on bus or relationship
    	 	 * 					 Possible values : bus or rel
    	 	 * @param strInterfaceName - Name of the Interface to be added
    	 	 * @param mAttrDetails - Map containing the key-value pair of interface attributes.
    	 	 * @return - bResult - true if attribute is set successfully
    	 	 * 					 - false in case of failure.
    	 	 * @throws Exception
    	 	 *
    	 	 */
    	 	public boolean addInterfaceAndSetAttributes(Context context, String strId, String strAdmin,
    	 										 String strInterfaceName, Map mAttrDetails)throws Exception{
    	 		boolean bResult = true;
    	 		String strCommand ="";
    	 		try{
    	 			strCommand = "modify "+ strAdmin + " " + strId +" add interface \'"+ strInterfaceName + "\'";
    	 			MqlUtil.mqlCommand(context,strCommand);

    	 			// check if the attribute Map is not null, if it is not null then set the attributes on the admin
    	 			if(mAttrDetails!=null && mAttrDetails.size()>0){

    	 				//DomainObject dom = new DomainObject(strId);
    	 				//dom.setAttributeValues(context,mAttrDetails);
    	 				String strNewFeatType = (String)mAttrDetails.get(ATTRIBUTE_NEW_FEATURE_TYPE);
    	 				String strConflict = (String)mAttrDetails.get(ATTRIBUTE_FTRMigrationConflict);

    	 				AttributeList attributes = new AttributeList(1);
    	 	            attributes.addElement(new Attribute(new AttributeType(ATTRIBUTE_NEW_FEATURE_TYPE), strNewFeatType));
    	 	            attributes.addElement(new Attribute(new AttributeType(ATTRIBUTE_FTRMigrationConflict), strConflict));

						//STEP1 - turn triggers off

        				/*String strMqlCommandOff = "trigger off";
        				MqlUtil.mqlCommand(context,strMqlCommandOff,true);
						mqlLogRequiredInformationWriter("trigger off .." + "\n");*/

    	 	            BusinessObject boFeatureObj  = new DomainObject(strId);
    	 				boFeatureObj.open(context);
    	 				boFeatureObj.setAttributeValues(context, attributes);
    	 				boFeatureObj.close(context);

						/*String strMqlCommandOn = "trigger on";
        				MqlUtil.mqlCommand(context,strMqlCommandOn,true);
						mqlLogRequiredInformationWriter("trigger on .." + "\n");*/

						mqlLogRequiredInformationWriter("Stamped Feature Id                      ---->"+strId +"\n");
						mqlLogRequiredInformationWriter("Value of attribute NewFeatureType       ---->"+strNewFeatType +"\n");
    	 				mqlLogRequiredInformationWriter("Value of attribute FTRMigrationConflict ---->"+strConflict +"\n\n");

    	 			}

    	 		}catch(Exception e){
    	 			bResult = false;
    	 			mqlLogRequiredInformationWriter("add Interface And Set Attributes Command failed .."+ strCommand + "\n");
    	 			e.printStackTrace();
    	 			throw new FrameworkException(e.getMessage());
    	 		}
    	 		return bResult;
    	 	}


    	 	/**
    		 * Disconnects the given rel ids
    	     * @param context the eMatrix <code>Context</code> object
    		 * @param sLRERelIdsToBeDeleted StringList of Rel ids to be deleted
    		 * @throws Exception
    		 */
    		private void disconnectRel(Context context, StringList sLRERelIdsToBeDeleted) throws Exception
    		{
    		  try {
    			// Iterate the stringList containing the Rel ids to delete
    			String [] stArrRelIdToDel = new String[sLRERelIdsToBeDeleted.size()];

    		        for (int m = 0; m < sLRERelIdsToBeDeleted.size(); m++) {
    		        	stArrRelIdToDel[m] = (String) sLRERelIdsToBeDeleted.get(m);
    		        }

    		        // Call DomainRelationship's disconnect method to delete all the Rel ids in the String array in a single transaction
    		        DomainRelationship.disconnect(context, stArrRelIdToDel);

    			} catch (Exception e) {
    				e.printStackTrace();
    				throw new FrameworkException("Relationship disconnect Failed :"+e.getMessage());
    			}
    		}

  }

