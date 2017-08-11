/*
 * emxVariantConfigurationHF6MigrationBase.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = "$Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/emxVariantConfigurationHF6MigrationBase.java 1.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$"
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.productline.ProductLineConstants;

/**
 * @version CFP V62011 - Copyright (c) 2002, MatrixOne, Inc.
 
 * @author pmogusala
 *
 */
/**
 * @author pmogusala
 *
 */
public class emxVariantConfigurationMigrationBase_mxJPO extends
        emxCommonMigration_mxJPO {
        
	protected static final String parentModelSelect="to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"]." +
	"from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]." +
	"from.to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_PLATFORM+"].from.id";

	protected static final String managedModelSelect="from["+ConfigurationConstants.RELATIONSHIP_MANAGED_MODEL+"].to.id";

	protected static final String productPF_ID = "to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"]." +
		"from.to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id";

	protected static final String MF_productPF_FLID = "to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.id";

	protected static final String MF_FeatureOrProduct_FLIDs = "from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].to.id";
	protected static final String MF_FeatureOrProduct_IDs = "from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]"+
			".to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id";
	protected static final String MF_Feature_PFL_IDs = "from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"]"+
	".to.from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id";
	
	protected static final String PFL_ID = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id";
	
	protected static final String PFL_Prd_ID = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id";
	protected static final String PFL_Prd_TYPE = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.type";

	protected static final String managedProdz="from["+ConfigurationConstants.RELATIONSHIP_MANAGED_MODEL+
	"].to.from["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].to.id";
	
	static {
		DomainObject.MULTI_VALUE_LIST.add(parentModelSelect);
		DomainObject.MULTI_VALUE_LIST.add(MF_FeatureOrProduct_FLIDs);
		DomainObject.MULTI_VALUE_LIST.add(MF_FeatureOrProduct_IDs);
		DomainObject.MULTI_VALUE_LIST.add(PFL_ID);
		DomainObject.MULTI_VALUE_LIST.add(PFL_Prd_ID);
		DomainObject.MULTI_VALUE_LIST.add(PFL_Prd_TYPE);
		DomainObject.MULTI_VALUE_LIST.add(managedProdz);
	}

	static protected String newline = System.getProperty("line.separator");

	//logs and reports
	 FileWriter deleteLog = null;
	 FileWriter deleteReport = null;
	 FileWriter ruleProcessingLog  = new FileWriter(documentDirectory + "RuleProcessingLog.txt", true);
	 protected static final String SUITE_KEY = "Configuration";
		

//initializing string resource
    private String language = "";

	// StringList FeatureListIDs= new StringList(1);

	/**
	 * @param context
	 * @param args
	 * @throws Exception
	 */    
	public emxVariantConfigurationMigrationBase_mxJPO(Context context,
			String[] args) throws Exception {
		super(context, args);
		language = context.getSession().getLanguage();
		// TODO Auto-generated constructor stub
	}
	
	public void deleteReport(boolean initialise,String log)throws Exception{
		if(initialise){
			deleteLog   = new FileWriter(documentDirectory + "DeleteLog.txt", true);	
			deleteReport   = new FileWriter(documentDirectory + "DeleteLReport.csv", true);
			
		}
		deleteReport.write(log);
		deleteReport.flush();
	}
    private String isrelId = "";

	/**
	 * @param context
	 * @param objectList
	 * @throws Exception
	 */

	public void migrateObjects(Context context, StringList objectList)
	throws Exception {
		try{
		         
		StringList productRevList = new StringList();
		StringList featureRevList = new StringList();
		boolean migrateObjSeriesCnt = false;
		boolean migrateFeaAllocType = false;
		String[] arrJPOArgs = new String[3];		 
		arrJPOArgs[1]="from";
		arrJPOArgs[2]=ConfigurationConstants.RELATIONSHIP_MANAGED_MODEL;
		 
		for (int i = 0; i < objectList.size(); i++) {
			arrJPOArgs[0]=(String)objectList.get(i);
			
			DomainObject domObject = new DomainObject(arrJPOArgs[0]);			
			// This will check for Object has "Model Template" relationship at to side  list is passed
			if(i==0){
					
				//If Rel ids are paased
				if (isrelId.equals("")) {
//		            String cmd = "print bus " + (String) objectList.get(0)
//		                    + " select exists dump";
					String sCommandStatement = "print bus $1 select exists dump";
		            isrelId = MqlUtil.mqlCommand(context, sCommandStatement,(String) objectList.get(0));
		        }
				
				
				
				if(!(isrelId.equals("TRUE"))){				
					migrateFeaAllocType =true;
					break;
				}
			}
			if(domObject.getType(context).equals(ConfigurationConstants.TYPE_MODEL)){
				migrateObjSeriesCnt = true;
				break;
			}
			
			String hasManModelRel =(String)JPO.invoke(context, "emxDomainObject", null,
					"hasRelationship", arrJPOArgs,
					String.class);
			
			if("true".equals(hasManModelRel)){	
				productRevList.add(objectList.get(i));

			}else{
				featureRevList.add(objectList.get(i));
			}
		}
		boolean migratedProductMF=true;
		boolean migratedFeatureMF=true;
		// Update "Series Count" attr value.
		if(migrateObjSeriesCnt){
			// Update "Series Count" attr value.s
			updateSeriesCount(context,objectList);
		
		}else if(migrateFeaAllocType){
			updateFeatureAllocationType(context, objectList);
		}else{
			//initialize delete log
			mqlLogWriter("");
			deleteReport(true, "MasterFeatureID,ProductPlatFormID,ProductPlatForm_MasterFeaure_FeatureListID,MasterFeature_productORFeaureRev_FeatureListID"+newline);
			
			if(productRevList.size()>0){
				migratedProductMF =migrateProductRev(context,productRevList);
			}
			if(featureRevList.size()>0){
				migratedFeatureMF=migrateFeatureRev(context,featureRevList); 
			}
			
			//closing all the log files;
					
			 
			if(migratedProductMF  && migratedFeatureMF){
				//doing the rule processing
				processMFForRuleMigration(context);
				//passing the handle to External JPO's
			 /// customMasterFeatureMigration(context);//uncomment this as soon as the tcl got checked in
			  //deleting  objects
			  DeleteObsoleteandMigratedObject(context);
			}
			
		}	
		}catch (Exception e) {
			// TODO: handle exception
			mqlLogRequiredInformationWriter("==========exception======"+e.toString());
		}
		finally
        {
			if(deleteLog != null)
				deleteLog.close();
			if(deleteReport != null)
				deleteReport.close();
			if(ruleProcessingLog != null)
				ruleProcessingLog.close();				
			 
        }
	}
	/*this method is written for debugggng*/
	private String checkModelTemplate(Context context,String objID) throws Exception{
			String[] argsJPO = new String[3];		 
			argsJPO[1]="to";
			argsJPO[2]=ConfigurationConstants.RELATIONSHIP_MODEL_MASTER_FEATURES;
			argsJPO[0]=objID;    
			
			return(String)JPO.invoke(context, "emxDomainObject", null,
					"hasRelationship", argsJPO, String.class);
		
	}
	/**
	 * @param context
	 * @param objID
	 * @throws Exception
	 */
	public boolean migrateProductRev(Context context, StringList objList) throws Exception {
		//if debug is on write teh follwing log
		boolean successful = true;
		mqlLogWriter(newline+"*** Start migrateProductRev ***"+newline);

		String[] oidsArray = new String[objList.size()];
		oidsArray = (String[]) objList.toArray(oidsArray);
		StringList selectStmts=new StringList(ConfigurationConstants.SELECT_ID);
		selectStmts.add(parentModelSelect);
		selectStmts.add(managedModelSelect);    
		selectStmts.add(MF_productPF_FLID); 
		selectStmts.add(productPF_ID);
		selectStmts.add(MF_FeatureOrProduct_FLIDs);
		selectStmts.add(managedProdz);

		 String mAsterFeature="";
		MapList mapList=DomainObject.getInfo(context, oidsArray, selectStmts);
		
		for (Iterator iterator = mapList.iterator(); iterator.hasNext();) {
			
			Map objectMAp = (Map) iterator.next();
			mAsterFeature = (String)objectMAp.get(ConfigurationConstants.SELECT_ID);
			
			
			mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,"MasteFeature.migration.start",language)+mAsterFeature+""+newline);
		
			//connect model and Modle with modelTEmplate Rel.
			String checkisconenctedtomodeltempalte = checkModelTemplate(context,(String)objectMAp.get(managedModelSelect));
			//mqlLogWriter("checkisconenctedtomodeltempalte ="+checkisconenctedtomodeltempalte);
			if(!(checkisconenctedtomodeltempalte.equals("true"))){
              
				boolean isConencted = connectWithModelTemplate(context, (String)((StringList)objectMAp.get(parentModelSelect)).get(0),
						ConfigurationConstants.RELATIONSHIP_MODEL_MASTER_FEATURES, (String)objectMAp.get(managedModelSelect));
				
			
				if(isConencted){
					//check for rule before deleteing them
					checkForRules(context,new StringList(mAsterFeature));
						//mqlLogRequiredInformationWriter("ERROR: MAsterfeature ("+mAsterFeature+")has rules connected to it, as teh master feature is been deleted please updat ethe IR related to it "+newline);

						//writing to delete log --> MasterFeatureID,ProductPlatFormID,ProductPlatForm_MsterFeaure_FeatureListID
						loadMigratedOids (mAsterFeature);
						deleteLog.write((String)objectMAp.get(ConfigurationConstants.SELECT_ID)+newline+(String)objectMAp.get(productPF_ID)+newline+(String)objectMAp.get(MF_productPF_FLID)+newline);           
						deleteReport.write((String)objectMAp.get(ConfigurationConstants.SELECT_ID)+","+(String)objectMAp.get(productPF_ID)+","+(String)objectMAp.get(MF_productPF_FLID)+", "+newline);			    
						deleteLog.flush(); 
						deleteReport.flush();

					mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                                "MasteFeature.migration.success",language)+"( "+mAsterFeature+") "+newline);

				}else{
					mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                                "MasteFeature.migration.Failure",language)+"( "+mAsterFeature+") "+newline);
					writeUnconvertedOID( mAsterFeature);
					continue;
				}
			}else{
				String[] mastefeaturenameArgs = new String[1];
				mastefeaturenameArgs[0]=mAsterFeature;				 
				mqlLogRequiredInformationWriter(MessageUtil.getMessage(context, null,
	                    "MasteFeature.migration.Migrated",
	                    mastefeaturenameArgs, null, context.getLocale(),
	                    "emxConfigurationStringResource")+newline);
				continue;
			}

			//update FeatureAllocationType attribute On REl Products
			if(!updateFeatureAllocationTypeOnProducts(context,objectMAp)){
				//MasteFeature.migration.featureAllocatuopntype.error
				mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,"MasteFeature.migration.featureAllocatuopntype.error",language)+(String)objectMAp.get(managedModelSelect)+newline);
			}

			if(objectMAp.get(MF_FeatureOrProduct_FLIDs) != null){
				//checking for rules
	            checkForRules(context,(StringList)objectMAp.get(MF_FeatureOrProduct_FLIDs));
	           //check and then migrating design effectivity
	            checkDesignEffecivelyMatrix(context,(StringList)objectMAp.get(MF_FeatureOrProduct_FLIDs));
			}
			
		}
		
		return successful;
	}
	public boolean migrateFeatureRev(Context context, StringList objList) throws Exception {
		boolean successful = true;
		
		String[] oidsArray = new String[objList.size()];
		oidsArray = (String[]) objList.toArray(oidsArray);
		StringList selectStmts=new StringList(ConfigurationConstants.SELECT_ID);
		selectStmts.add(parentModelSelect);        						
		selectStmts.add(productPF_ID);
		selectStmts.add(MF_productPF_FLID);
		selectStmts.add(MF_FeatureOrProduct_FLIDs);
		selectStmts.add(MF_FeatureOrProduct_IDs);			

		MapList mapList=DomainObject.getInfo(context, oidsArray, selectStmts);
		String masterFeature = "";
		for (Iterator iterator = mapList.iterator(); iterator.hasNext();) {
			
			Map objectMAp = (Map) iterator.next();
			masterFeature = (String)objectMAp.get(ConfigurationConstants.SELECT_ID);
			
			mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
            "MasteFeature.migration.start",language)+masterFeature+""+newline);
			 new DomainObject(masterFeature).setPolicy(context, PropertyUtil.getSchemaProperty(context,"policy_ManagedSeries"));
				
			 boolean isConnected = true;
			
			//connect model and Master Feature with modelTEmplate Rel.
			String checkisconenctedtomodeltempalte = checkModelTemplate(context,masterFeature);

			if(!(checkisconenctedtomodeltempalte.equals("true"))){
				StringList parentModelList = (StringList)objectMAp.get(parentModelSelect);
				for (int k = 0; k < parentModelList.size(); k++) {
					String strPArentModel= (String)parentModelList.get(k);
									
				isConnected = connectWithModelTemplate(context, strPArentModel,
					ConfigurationConstants.RELATIONSHIP_MODEL_MASTER_FEATURES, masterFeature);			

					
				}
			}else{
				String[] mastefeaturenameArgs = new String[1];
				mastefeaturenameArgs[0]=masterFeature;				 
				mqlLogRequiredInformationWriter(MessageUtil.getMessage(context, null,
	                    "MasteFeature.migration.Migrated",
	                    mastefeaturenameArgs, null, context.getLocale(),
	                    "emxConfigurationStringResource")+newline);
			}

			if (objectMAp.get(MF_FeatureOrProduct_IDs) != null) {
				mqlLogWriter("*** connecting with Managed Series "+newline);
				StringList fetaureRevIDs = (StringList)objectMAp.get(MF_FeatureOrProduct_IDs);
                String[] fateureidsArray = new String[fetaureRevIDs.size()];
                fateureidsArray = (String[]) fetaureRevIDs.toArray(fateureidsArray);
                boolean isConnectFeatRev = true;
                Map feature_relIDMAp=null;
                //IR-046034V6R2011WIM 
                //OLD SCHEMA:Attribute MArketing name is  Derived from FAEtures in the OLD Schema. 
                //NEW SCHEMA: as we are removing "derived" which indirectly removed all the inherited attributes and 
                //we are adding Marketing anme attribute to the master Features which makes this attribute empty.
                //So Setting the "MAster FEature" MArketing name Same as the MArketing name of the First Feature Revision Connected to it.
                if (fetaureRevIDs.size()>0){
                	String MarketingName = new DomainObject((String)fetaureRevIDs.get(fetaureRevIDs.size()-1)).getInfo(context,"attribute["+ConfigurationConstants.ATTRIBUTE_MARKETING_NAME+"]");
                	new DomainObject(masterFeature).setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_MARKETING_NAME, MarketingName );
                }
                try{
                //connect Master Feature and Feature Revision with REl "Managed Series" revisions
                	 feature_relIDMAp=DomainRelationship.connect(context,new DomainObject(masterFeature),
                		ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION,true,fateureidsArray);
                }catch (Exception e) {
					// TODO: handle exception
                	isConnectFeatRev = false;
				}
                if(isConnected && isConnectFeatRev){
					loadMigratedOids (masterFeature);
					//writing to delete log --> ProductPlatFormID,ProductPlatForm_MsterFeaure_FeatureListID
					deleteLog.write((String)objectMAp.get(productPF_ID)+newline+(String)objectMAp.get(MF_productPF_FLID)+newline);
					deleteReport.write(" "+","+(String)objectMAp.get(productPF_ID)+","+(String)objectMAp.get(MF_productPF_FLID)+", "+newline);
					deleteLog.flush();
					deleteReport.flush();
					mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                    "MasteFeature.migration.success",language)+"( "+masterFeature+") "+newline);
				}else{
					mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                    "MasteFeature.migration.Failure",language)+"( "+masterFeature+") "+newline);
					writeUnconvertedOID( masterFeature);
					continue;
				}
             
                //feature allocation Migration
                if(!(updateFeatureAllocationTypeOnManagedSeries(context,feature_relIDMAp,(StringList)objectMAp.get(MF_FeatureOrProduct_FLIDs)))){
                	mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                    "MasteFeature.migration.featureAllocatuopntype.ManagedSeries.error",language)+"( "+masterFeature+") "+newline);
                }
                //checking for rules
                checkForRules(context,(StringList)objectMAp.get(MF_FeatureOrProduct_FLIDs));
               //check and then migrating design effectivity
                checkDesignEffecivelyMatrix(context,(StringList)objectMAp.get(MF_FeatureOrProduct_FLIDs));
			 }
			
		}//for loop MF ids

		
		return successful;
	}

	/** This method check if teh FeatureList  object has ProductFeaurelist Relationship for further migration before deleting FeatureList
	 * @param context
	 * @param featureListIDs
	 * @throws Exception
	 */
	private boolean checkDesignEffecivelyMatrix(Context context,StringList featureListIDs)throws Exception{
		boolean successful = true;
		try{
			
			for (Iterator iterator = featureListIDs.iterator(); iterator.hasNext();) {
				String objectID = (String) iterator.next();
				//checking if object has Relationship "Product Feature List"
				String[] argsJPO = new String[3];
				argsJPO[1]="to";
				argsJPO[2]=ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST;
				argsJPO[0]=objectID;
				String hasPFLRel =(String)JPO.invoke(context, "emxDomainObject", null,
						"hasRelationship", argsJPO, String.class);
				if("true".equals(hasPFLRel)){
					//migratedesignefefctivity
					migrateDesignEffecivelyMatrix(context,objectID);

				}else{
					//write to Delete Log					
					mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
                    "MasteFeature.migration.FeatureList.delete",language)+newline);
					deleteLog.write(objectID+newline);
				    deleteReport.write(" "+","+" "+","+" "+","+objectID+newline);
				    deleteLog.flush();
				    deleteReport.flush();

				}

			}
		}catch(Exception e){
			successful=false;			
			mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
            "MasteFeature.migration.checkDesignEffecivelyMatrix.ERROR",language)+e.toString()+newline);
		}
		
		return successful;
		
	}
	/*
	 * This method check for ruel if any onthe FeatureLsit Object and will write the FeatureList ids to the log file for further Processing */
	private boolean checkForRules(Context context,StringList featureListIDs)throws Exception{
		boolean hasRules = false;
					
			for (Iterator iterator = featureListIDs.iterator(); iterator.hasNext();) {
				String objectID = (String) iterator.next();
				
					//check for R.e and L.e
					//checking if object has Relationship "Left Expression"
				String[] argsJPO = new String[3];		 
				argsJPO[1]="to";
				argsJPO[2]=ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION;
				argsJPO[0]=objectID;  
				try{
					// for right Expression 
					String hasRERel =(String)JPO.invoke(context, "emxDomainObject", null,
							"hasRelationship", argsJPO, String.class);
					
					// for Left Expression 
					argsJPO[2]=ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION;				    
					String hasLERel =(String)JPO.invoke(context, "emxDomainObject", null,
							"hasRelationship", argsJPO, String.class);
				
						if ("true".equals(hasRERel) || "true".equals(hasLERel)){
							//this boolean value helps in case of single object ID like "MAster feature" and product Platfirm ID
							hasRules=true;
							//write to Rule processing log
							mqlLogWriter("*** writing to Rule log for later migration ***"+newline);
							ruleProcessingLog.write(objectID+newline);
							ruleProcessingLog.flush();
						}
					}catch(Exception e){
						mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			            "MasteFeature.migration.checkForRules.ERROR",language)+e.toString()+newline);					
					}
			
			}
			
			
		return hasRules;
	}
	private boolean migrateDesignEffecivelyMatrix(Context context,String strObjId) throws Exception{
		boolean successful = true;
		String productMigrating="";//added this to be in the log
		try{
			String[] flObjArg = new String[1];
			flObjArg[0]=strObjId;				 
			mqlLogWriter(MessageUtil.getMessage(context, null,
                    "MasteFeature.migration.migrateDesignEffecivelyMatrix.start",
                    flObjArg, null, context.getLocale(),
                    "emxConfigurationStringResource")+newline);
			
			//strObjId//featurelist id
			StringList selectStmts = new StringList();
			selectStmts.add(PFL_ID);
			selectStmts.add(PFL_Prd_ID);


			selectStmts.add("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].id");//FeatureListFrom Relid
			selectStmts.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");//Featureid

			//queryiing thefetaurelist object
				Map objDataMap = new DomainObject(strObjId).getInfo(context,selectStmts);
				mqlLogWriter("objDataMap ="+objDataMap+newline);
				
				StringList strLstPFLFRelIds = (StringList) objDataMap.get(PFL_ID);
				StringList strLstPFFLFPRDids = (StringList) objDataMap.get(PFL_Prd_ID);


				String strFLF_RelId = (String) objDataMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].id");
				String strF_Id = (String) objDataMap.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");

				HashMap productsMap = new HashMap();
				HashMap productVariantMap = new HashMap();


				//separating the productIDs and product variant ids.
				for(int i=0; i<strLstPFLFRelIds.size();i++){
					String stinrprodRELid = (String) strLstPFLFRelIds.get(i);
					String stringPFLPRDId = (String) strLstPFFLFPRDids.get(i);
					DomainObject pobject = new DomainObject(stringPFLPRDId);
					String prType=pobject.getInfo(context, ConfigurationConstants.SELECT_TYPE);
								
						if (prType.equals(ConfigurationConstants.TYPE_PRODUCT_VARIANT )) {							   
							    productVariantMap.put(stringPFLPRDId,stinrprodRELid);
						}else{
							 productsMap.put(stringPFLPRDId,stinrprodRELid);
						}

				}
				//mqlLogWriter("productsMap ="+productsMap+newline);
				//mqlLogWriter("productVariantMap ="+productVariantMap+newline);
				
				if (productsMap.size()==1){
					//changing the form object
					
					mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		            "MasteFeature.migration.migrateDesignEffecivelyMatrix.debug1",language)+newline);
					
					productMigrating = (String)productsMap.keySet().toArray()[0];
					DomainRelationship.setFromObject(context,
							strFLF_RelId,
							new DomainObject((String)productsMap.keySet().toArray()[0]));
					
					//updating feature list object with PArent names
					updateFeatureListObjects(context, (String)productsMap.keySet().toArray()[0],strObjId,"FROM");

				}else{
					Object[] arrayPrdKey = productsMap.keySet().toArray();

					for (int i = 0; i < arrayPrdKey.length; i++) {
						String stringPrdID = (String)arrayPrdKey[i];
						productMigrating = stringPrdID;
						DomainObject prdObject=new DomainObject(stringPrdID);
						
						//mqlLogWriter("stringPrdID ="+stringPrdID);
						 if(i==0){
							//changing the from object
							 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
					            "MasteFeature.migration.migrateDesignEffecivelyMatrix.debug1",language)+newline);
							 
								DomainRelationship.setFromObject(context,
										strFLF_RelId,
										prdObject);
								//updating featureList object attributes with PArent names
								updateFeatureListObjects(context, stringPrdID,strObjId,"FROM");
								//getting PV' connecting to product and removing from the list as they are already connected
								if(productVariantMap.size()>0){
									StringList prodcutVariants=prdObject.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].to.id");
									for (int j = 0; j < prodcutVariants.size(); j++) {
										if(productVariantMap.keySet().contains((String)prodcutVariants.get(j))){
											productVariantMap.remove((String)prodcutVariants.get(j));
										}
									}
								}
						 }else{
							 //clonning and conecting
							 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
					            "MasteFeature.migration.migrateDesignEffecivelyMatrix.debug2",language)+newline);
								DomainObject domFLObj = new DomainObject(strObjId);

								String strObjectGeneratorName =
			                        FrameworkUtil.getAliasForAdmin(context,
			                                                       DomainConstants.SELECT_TYPE,
			                                                       ConfigurationConstants.TYPE_FEATURE_LIST,
			                                                       true);
			                    String strAutoName = DomainObject.getAutoGeneratedName(context,strObjectGeneratorName,null);
								DomainObject newClonedFL = new DomainObject(domFLObj.cloneObject(context, strAutoName));
								//Connect Product with Feature List From relationship.
								 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
						            "MasteFeature.migration.migrateDesignEffecivelyMatrix.debug3",language)+newline);
								newClonedFL.connect(context,
										new RelationshipType(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM),
										false,
										prdObject);
								//connecting with ProductFeatureListRelatiobship
								 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
						            "MasteFeature.migration.migrateDesignEffecivelyMatrix.debug4",language)+newline);
								newClonedFL.connect(context,
										new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST),
										false,
										prdObject);

								//updating feature list object with Child  names
								updateFeatureListObjects(context, stringPrdID,newClonedFL.getId(context),"FROM");

								//Connect Product with "Feature List To" relationship.
								//mqlLogWriter("*** Connect Product with Feature List To relationship. "+newline);
								/*newClonedFL.connect(context,
										new RelationshipType(ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO),
										true,
										new DomainObject(strF_Id));*/
								

								//updating feature list object with PArent  names
								updateFeatureListObjects(context, strF_Id,newClonedFL.getId(context),"TO");
								
								
								//updating PC on the variant o f the product
								connectMigrateProductConfiguration(context,domFLObj,stringPrdID,newClonedFL);
								
								
								
								if(productVariantMap.size()>0){
									StringList prodcutVariants=prdObject.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_PRODUCT_VERSION+"].to.id");
									for (int j = 0; j < prodcutVariants.size(); j++) {
										if(productVariantMap.keySet().contains((String)prodcutVariants.get(j))){
											newClonedFL.connect(context,
													new RelationshipType(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST),
													false,
													new DomainObject((String)prodcutVariants.get(j)));
											connectMigrateProductConfiguration(context,domFLObj,(String)prodcutVariants.get(j),newClonedFL);
										}

									}
								}

						 }

					}
				}



		}catch(Exception e){
			successful=false;						
			mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
            "MasteFeature.migration.migrateDesignEffecivelyMatrix.error",language)+"("+productMigrating+")"+e.toString()+newline);			
		}
		
		return successful;
	}
	
	private boolean connectMigrateProductConfiguration(Context context,DomainObject domFLObj, String productid,DomainObject newClonedFL)throws Exception{
		boolean isConnected=true;
		try{
			
			String relTypePatterns = ConfigurationConstants.RELATIONSHIP_SELECTED_OPTIONS;
			String  objTypePatterns = ConfigurationConstants.TYPE_PRODUCT_CONFIGURATION;
			StringList objectSelect =  new StringList(ConfigurationConstants.SELECT_ID);						
			StringList relSelect =  new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);													
			String objectWhereCond = "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_CONFIGURATION+"].from.id==\""+productid+"\"";
			MapList relSO_ID_Maplist = domFLObj.getRelatedObjects(
					context, relTypePatterns, objTypePatterns, true,
					false, 1, objectSelect, relSelect,
					objectWhereCond, ConfigurationConstants.EMPTY_STRING, 0,
					null, null, null);
			
			mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
            "MasteFeature.migration.ProductConfiguration",language)+newline);
			
			for (int i = 0; i < relSO_ID_Maplist.size(); i++) {
				Map relSO_IDMap = (Hashtable) relSO_ID_Maplist.get(0);
				DomainRelationship.setToObject(context, 
						(String)relSO_IDMap.get(SELECT_RELATIONSHIP_ID), 
						newClonedFL);
			}
			
		}catch(Exception e){
			isConnected=false;
			String[] pcArgs = new String[2];
			pcArgs[0]=productid;	
			pcArgs[1]=newClonedFL.getName();
			mqlLogRequiredInformationWriter(MessageUtil.getMessage(context, null,
                    "MasteFeature.migration.ProductConfiguration.error",
                    pcArgs, null, context.getLocale(),
                    "emxConfigurationStringResource")+e.toString()+newline);
		}
		
		return isConnected;
	}
	 /**
     * This method updates 4 attributes on Feature List when ever the new
     * featrure List from relationship is created The attribiurtes are: parent
     * object name,parent marketing name,child object name ,child marketing name
     */
	private void updateFeatureListObjects(Context context, String strObjectId,String strFeatureListId,String strDirection)  throws Exception {

		mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
        "MasteFeature.migration.featuelist.updateAttribute",language)+strFeatureListId+newline);
		
		// selects
		StringList lstSelect = new StringList("attribute["
		        + ProductLineConstants.ATTRIBUTE_MARKETING_NAME + "]");
		lstSelect.add(DomainConstants.SELECT_NAME);
		Map mapTemp = new DomainObject(strObjectId).getInfo(context, lstSelect);
		
		// update attributes
		HashMap attvaluesMap = new HashMap();
		if (strDirection.equals("FROM")) {
		    attvaluesMap.put(ConfigurationConstants.ATTRIBUTE_PARENT_OBJECT_NAME, (String) mapTemp
		            .get(DomainConstants.SELECT_NAME));
		    attvaluesMap.put(ConfigurationConstants.ATTRIBUTE_PARENT_MARKETING_NAME, (String) mapTemp
		            .get("attribute["
		                    + ProductLineConstants.ATTRIBUTE_MARKETING_NAME
		                    + "]"));
		
		} else {
		    attvaluesMap.put(ConfigurationConstants.ATTRIBUTE_CHILD_OBJECT_NAME, (String) mapTemp
		            .get(DomainConstants.SELECT_NAME));
		    attvaluesMap.put(ConfigurationConstants.ATTRIBUTE_CHILD_MARKETING_NAME, (String) mapTemp
		            .get("attribute["
		                    + ProductLineConstants.ATTRIBUTE_MARKETING_NAME
		                    + "]"));
		
		}
		try {
			mqlLogWriter("attributes map "+ attvaluesMap+newline);
		    DomainObject domObj = new DomainObject(strFeatureListId);
		
		    domObj.setAttributeValues(context, attvaluesMap);
		
		} catch (Exception e) {
		    e.printStackTrace();
		    mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	        "MasteFeature.migration.featuelist.updateAttribute.error",language)+strFeatureListId+"   " +e.toString()+newline);
		}
		
	}
	
	private boolean connectWithModelTemplate(Context context,String fromObject,String strRel,String toObject){
		boolean isConnected=true;
		
		try{
			mqlLogWriter("***  connecting "+fromObject+" and "+toObject+" WithModelTemplate ***"+newline);
			DomainRelationship.connect(context, new DomainObject(fromObject), strRel, new DomainObject(toObject));
		}catch(Exception e){
			isConnected=false;
		}
		return isConnected;
	}
	
	/**
	 * 
	 * @param context
	 * @param feature_relIDMApare you working on ODTS
	 * @param FLIDs
	 * @return
	 */
	private boolean updateFeatureAllocationTypeOnManagedSeries( Context context,Map feature_relIDMAp,StringList FLIDs)throws Exception{
		boolean successful=true;
		try{
			
			String [] strIdarr = new String[FLIDs.size()];
			FLIDs.copyInto(strIdarr);
			StringList strObjSelect = new StringList(ConfigurationConstants.SELECT_ID);		
			strObjSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");
			strObjSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");		
			strObjSelect.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
					
			
			MapList relData = DomainObject.getInfo(context, strIdarr, strObjSelect);
			String strDefaultSelection="";
			String strMandatory="";
			String strFeatureID="";
			
			for (Iterator iterator = relData.iterator(); iterator.hasNext();) {
				Map FL_att_Map = (HashMap) iterator.next();
				
				strMandatory = (String) FL_att_Map.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");
				strDefaultSelection = (String) FL_att_Map.get("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");
				strFeatureID=(String) FL_att_Map.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
				String strFeatureAllocationTypeAttrValue = ConfigurationConstants.RANGE_VALUE_STANDARD;
				if(strDefaultSelection.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NO)&&
		                  strMandatory.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NO)){
		              strFeatureAllocationTypeAttrValue = ConfigurationConstants.RANGE_VALUE_OPTIONAL;
				}
			
				 new DomainRelationship((String)feature_relIDMAp.get(strFeatureID)).setAttributeValue(context, 
						ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE, 
						strFeatureAllocationTypeAttrValue);
				 //logging
				 String[] flObjArg = new String[2];
					flObjArg[0]=(String)feature_relIDMAp.get(strFeatureID);		
					flObjArg[1]=strFeatureAllocationTypeAttrValue;
					mqlLogWriter(MessageUtil.getMessage(context, null,
		                    "MasteFeature.migration.FeatureAllocationTypeOnManagedSeries.info",
		                    flObjArg, null, context.getLocale(),
		                    "emxConfigurationStringResource")+newline);
				 
			}			
			
			
		}catch(Exception e){
			successful=false;			
		}

		return successful;
	}
	/**
	 * This method is used to update the Series Count attribute value for Model as well as Master Feature. 
	 * @param context
	 * @param contextObjectId - Model ID or Master Feature Id
	 * @return isUpdated True if update successfully.
	 */
	private boolean updateSeriesCount(Context context,StringList contextObjIds){
		boolean isUpdated=true;
		try{
			
			for (Iterator ctxtObjIdIttr = contextObjIds.iterator(); ctxtObjIdIttr
					.hasNext();) {
				String contextObjId = (String) ctxtObjIdIttr.next();
				DomainObject ctxObj = new DomainObject(contextObjId);
				String strTypePattern = ConfigurationConstants.TYPE_PRODUCTS+","+ConfigurationConstants.TYPE_FEATURES;
				String strRelPattern = ConfigurationConstants.RELATIONSHIP_PRODUCTS+","+ConfigurationConstants.RELATIONSHIP_MANAGED_REVISION;
				
				StringList strObjecSelect = new StringList(ConfigurationConstants.SELECT_ID );			
				StringList strRelSelect = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
				
				//Get related products or Feature maintained.
				MapList relatedObjectData = ctxObj.getRelatedObjects(context,
						strRelPattern, strTypePattern,
						strObjecSelect, strRelSelect, false, true, (short) 1,
	                    null, null, 0);
				
				// Get series count attribute value for context object.
				String seriesCount = ctxObj.getInfo(context,"attribute[" + ConfigurationConstants.ATTRIBUTE_REVISION_COUNT +"]");
				int seriesCntVal =  Integer.parseInt(seriesCount.toString());
				
				//Series count is not updated then only it will update value
				if(relatedObjectData.size() != seriesCntVal){
					seriesCntVal = relatedObjectData.size();
								
					ctxObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_REVISION_COUNT,Integer.toString(seriesCntVal));
					 
					 ///logging
					 String[] flObjArg = new String[2];
						flObjArg[0]=contextObjId;		
						flObjArg[1]=""+seriesCntVal;
						mqlLogWriter(MessageUtil.getMessage(context, null,
			                    "SeriesCount.migration.update.log",
			                    flObjArg, null, context.getLocale(),
			                    "emxConfigurationStringResource")+newline);	
				}
			}
			
		}catch(Exception e){
			isUpdated=false;
		}
		return isUpdated;
	}

	/**
	 * This method is used to update the Feature Allocation Type attribute value. 
	 * @param context
	 * @param RelIds  
	 * @return isUpdated True if update successfully.
	 */
	public boolean updateFeatureAllocationType(Context context,StringList strLstRelIds){
		boolean isUpdated=true;
		try{
			
			String [] strRelIdarr = new String[strLstRelIds.size()];
			strLstRelIds.copyInto(strRelIdarr);
			StringList strRelSelect = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
			strRelSelect.add(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
			strRelSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
			strRelSelect.add("from."+ConfigurationConstants.SELECT_ID);
					
			
			MapList relData = DomainRelationship.getInfo(context, strRelIdarr, strRelSelect);			
			for (Iterator relDataIttr = relData.iterator(); relDataIttr.hasNext();) {
				Hashtable relDataMap = (Hashtable) relDataIttr.next();
				String relName = (String) relDataMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_NAME);
				String relId = (String) relDataMap.get(ConfigurationConstants.SELECT_RELATIONSHIP_ID);
				DomainRelationship domRelObj = new DomainRelationship(relId);				
				// If relationship is Product Feature List
				if(relName.equals(ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST)){
					String strFeaAllocTypeVal = (String) relDataMap.get("attribute["+
							ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+
							"]");
					//Added condition IR-040613V6R2011 
					if(strFeaAllocTypeVal.equals(ConfigurationConstants.RANGE_VALUE_MANDATORY) ||
							strFeaAllocTypeVal.equals("")){
						domRelObj.setAttributeValue(context, 
								ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE, 
								ConfigurationConstants.RANGE_VALUE_STANDARD);
						//logging
						 String[] flObjArg = new String[1];
							flObjArg[0]=relId;								
							mqlLogWriter(MessageUtil.getMessage(context, null,
				                    "FeatureAllocationType.migration.update.log",
				                    flObjArg, null, context.getLocale(),
				                    "emxConfigurationStringResource")+newline);							
						}
					
				}//Migrating the relationships Managed Series and Products for Feature Allocation Type attribute value.   
				
					
			}
			mqlLogWriter("*** END updateFeatureAllocationType ***"+newline);
		}catch(Exception e){
			isUpdated=false;
		}
		return isUpdated;
	}
	private boolean updateFeatureAllocationTypeOnProducts( Context context,Map MasterFeaMap)throws Exception{
		boolean successful=true;
		try{
			
			String strMFId =  (String)MasterFeaMap.get(ConfigurationConstants.SELECT_ID);
			StringList strLstPrdId =  (StringList)MasterFeaMap.get(managedProdz);
			
			for (Iterator ittrPrd = strLstPrdId.iterator(); ittrPrd.hasNext();) {
				String strPrdId = (String) ittrPrd.next();							
				String relTypePatterns = ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM;
				String  objTypePatterns = ConfigurationConstants.TYPE_FEATURE_LIST;
				StringList objectSelect =  new StringList(ConfigurationConstants.SELECT_ID);
				objectSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");
				objectSelect.add("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");
				objectSelect.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");	
				objectSelect.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.to["+ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].id");	
				String objectWhereCond = "from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id==\""+strPrdId+"\"";
				
				DomainObject domFromObj = new DomainObject(strMFId);						
				MapList relFLObj = domFromObj.getRelatedObjects(
						context, relTypePatterns, objTypePatterns, false,
						true, 1, objectSelect, ConfigurationConstants.EMPTY_STRINGLIST,
						objectWhereCond, ConfigurationConstants.EMPTY_STRING, 0,
						null, null, null);
				if(relFLObj.size() >0){
					Map feaLisDataMap = (Hashtable) relFLObj.get(0);						
					String strMandatory = (String) feaLisDataMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_MANDATORY_FEATURE+"]");
					String strDefaultSelection = (String) feaLisDataMap.get("attribute["+ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION+"]");
					String strFeatureAllocationTypeAttrValue = ConfigurationConstants.RANGE_VALUE_STANDARD;
					if(strDefaultSelection.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NO)&&
			                  strMandatory.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_NO)){
			              strFeatureAllocationTypeAttrValue = ConfigurationConstants.RANGE_VALUE_OPTIONAL;
					}
					DomainRelationship domRelObj = new DomainRelationship((String)feaLisDataMap.get("from["+
							ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+
							"].to.to["+
							ConfigurationConstants.RELATIONSHIP_PRODUCTS+"].id"));
					
					
					domRelObj.setAttributeValue(context, 
							ConfigurationConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE, 
							strFeatureAllocationTypeAttrValue);
					//logging
					 String[] flObjArg = new String[2];
						flObjArg[0]=strFeatureAllocationTypeAttrValue;	
						flObjArg[1]=domRelObj.getName();	
						mqlLogWriter(MessageUtil.getMessage(context, null,
			                    "MAsterfeature.FeatureAllocationType.migration.update.log",
			                    flObjArg, null, context.getLocale(),
			                    "emxConfigurationStringResource")+newline);	
				}								
				
			}
			
		}catch(Exception e){
			successful=false;			
		}
		
		return successful;
	}
	
	private boolean processMFForRuleMigration( Context context)throws Exception{
		boolean successful=true;
		String objectId = "";
        StringList objectIds = new StringList();
        try
        {
        	mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
        			"MAsterFeature.processMFForRuleMigration.start",language)+newline);
        	
            java.io.File file = new java.io.File(documentDirectory + "RuleProcessingLog.txt");
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            while((objectId = fileReader.readLine()) != null){
              objectIds.add(objectId);
            }            
            
            for (Iterator FLIdIttr = objectIds.iterator(); FLIdIttr.hasNext();) {
				String strFLID = (String) FLIdIttr.next();				
				DomainObject domFLObj = new DomainObject(strFLID);
				StringList objectSelect =  new StringList("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type");
				objectSelect.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.type");
				objectSelect.add("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
				objectSelect.add("to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].id");
				objectSelect.add("to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].id");
				objectSelect.add("to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.id");//ruleid
				objectSelect.add("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id");//masterfeature id

				DomainObject.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].id");
				DomainObject.MULTI_VALUE_LIST.add("to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].id");
				
				Map flDataMap = domFLObj.getInfo(context, objectSelect);
				
				String frmType = (String) flDataMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type") ;
				String toType = (String) flDataMap.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.type") ;
				String toId = (String) flDataMap.get("from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id") ;
				
				if(frmType.equals(ConfigurationConstants.TYPE_PRODUCT_PLATFORM) && 
						toType.equals(ConfigurationConstants.TYPE_MASTER_FEATURE)){

					if(toType.equals(ConfigurationConstants.TYPE_PRODUCTS)){
						//logging
						 String[] flObjArg = new String[2];
							flObjArg[0]=(String)flDataMap.get("from["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.id");	
							flObjArg[1]=(String)flDataMap.get("to["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id");	
							mqlLogRequiredInformationWriter(MessageUtil.getMessage(context, null,
				                    "MasteFeature.migration.ruleMigration.error",
				                    flObjArg, null, context.getLocale(),
				                    "emxConfigurationStringResource")+newline);	
					}
					deleteLog.write(strFLID+newline);
				    deleteReport.write(strFLID+",");
				    deleteLog.flush(); 
				    deleteReport.flush();
				    //mqlLogWriter("***  ***"+newline);
				}else{
					StringList strREnLERelIds= new StringList(1);
					if(flDataMap.containsKey("to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].id")){
					 strREnLERelIds = (StringList) flDataMap.get("to["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].id");
					}
					if(flDataMap.containsKey("to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].id")){
					strREnLERelIds.addAll((StringList) flDataMap.get("to["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].id"));
					}
					
					
					for (Iterator strExpRelIttr = strREnLERelIds.iterator(); strExpRelIttr
							.hasNext();) {
						String strExpRelId = (String) strExpRelIttr.next();	
						//changing the toside of R.E/L.E
						mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	        			"MAsterFeature.processMFForRuleMigration.debug1",language)+newline);
						DomainRelationship.setToObject(context, strExpRelId, new DomainObject(toId));
						String[] strRelIdArr = new String[]{strExpRelId};
						StringList relSelect =  new StringList("from.id");
						relSelect.add("from.attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]");
						relSelect.add("from.attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]");
						
						
						MapList relData = DomainRelationship.getInfo(context,strRelIdArr, relSelect);
						Map relDataMap = (Hashtable) relData.get(0);						
						String strRuleId = (String) relDataMap.get("from.id");
						String strLEAttrVal = (String) relDataMap.get("from.attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]");
						String strREAttrVal = (String) relDataMap.get("from.attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]");
						
						strLEAttrVal = strLEAttrVal.replaceAll(strFLID, toId);
						strREAttrVal = strREAttrVal.replaceAll(strFLID, toId);
						
						//Updating the attributes 
						mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	        			"MAsterFeature.processMFForRuleMigration.debug.update",language)+ strRuleId+newline);
						DomainObject domRuleObj = new DomainObject(strRuleId);
						
						domRuleObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION, strLEAttrVal);
						domRuleObj.setAttributeValue(context, ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION, strREAttrVal);
						
						/// Block added for generating Duplicate Part and Equipment List report XML  for feature. 
						StringList strLEObjType = domRuleObj.getInfoList(context,"from["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].to.type" );
						StringList strREObjType = domRuleObj.getInfoList(context,"from["+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION+"].to.type" );
						
						mqlLogWriter("*** cheking to see if duplicate/Equipment part xml to be run"+newline);

						if(strLEObjType.contains(ConfigurationConstants.TYPE_GBOM) ||
								strREObjType.contains(ConfigurationConstants.TYPE_GBOM)){
							
							objectSelect.clear();
							objectSelect.add("to["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].from.id");
							
							MapList objectData = domRuleObj
									.getRelatedObjects(context,
											ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+","+ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION
											, ConfigurationConstants.TYPE_GBOM,
											false, true, 1,
											objectSelect, ConfigurationConstants.EMPTY_STRINGLIST,
											ConfigurationConstants.EMPTY_STRING, ConfigurationConstants.EMPTY_STRING,
											0, null,
											null, null);
							for (Iterator ittrGBOM = objectData.iterator(); ittrGBOM
									.hasNext();) {
								Map dataGBOM = (Map) ittrGBOM.next();
								String strFeatureId = (String) dataGBOM.get("to["+ConfigurationConstants.RELATIONSHIP_GBOM_FROM+"].from.id");
								String tempArgs[] = new String[1];
								tempArgs[0] = strFeatureId;					
								
								JPO.invoke(context,"emxFTRPart", null,"generateDuplicatePartXML", tempArgs,StringList.class);
								mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			        			"MAsterFeature.processMFForRuleMigration.updateDuplicatexml",language)+strFeatureId+newline);
								
								String[] arrEqp = new String[4];
								arrEqp[0] = "EQ";
								arrEqp[1] = "";
								arrEqp[2] = "Define Effectivity";
								arrEqp[3] = strFeatureId;								
								
								JPO.invoke(context,"emxProductConfigurationEBOM", null,"updateEquipmentListReportXML", arrEqp);
								mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			        			"MAsterFeature.processMFForRuleMigration.EquipmentListReportXML",language)+strFeatureId+newline);
							}
						}
					}
				}
			}
            mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"MAsterFeature.processMFForRuleMigration.end",language)+newline);
		}catch(Exception e){
			successful=false;
			mqlLogRequiredInformationWriter("*** ERROR processMFForRuleMigration *** "+e.toString()+newline);

		}
		return successful;
	}
	/* call the External JPO's through a command*/
	private boolean customMasterFeatureMigration(Context context)throws Exception{
		boolean successful=true;
		 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"MasteFeature.migration.CustomMigration.start",language)+newline);
		
			HashMap cmdMap = UICache.getCommand(context, PropertyUtil.getSchemaProperty(context,"command_FTRCustomMigration"));
	          if (cmdMap == null || "null".equals(cmdMap) || cmdMap.size() <= 0)
	          {
	        	  mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
	  			"MasteFeature.migration.CustomMigration.command.error",language)+newline);
	              return false;
	          }
	       HashMap settingsMap = (HashMap) cmdMap.get("settings");
	       //key pattern defined "MigrateJPO_<Application Trigram>"
	       //example :key =MigrateJPO_VPM
	     //VAlue pattern defined <progarnName>:<methodNAme>"
	       //  Example:Value=emxTestMigration:testMigration
	       
	      if(settingsMap != null && settingsMap.size() >0){
	          Object [] keyValues = settingsMap.keySet().toArray();
	          for (int i = 0; i < keyValues.length; i++) {
	        	  String strkey = (String)keyValues[i];
				if(strkey.startsWith("MigrateJPO")){
					String strJPOValue = (String)settingsMap.get(strkey);
					  StringTokenizer tokens = new StringTokenizer(strJPOValue, ":");
					  String strProgram = "";
			          String strMethod = "";
			          if (tokens.hasMoreTokens())
			          {
			              strProgram = tokens.nextToken();
			              strMethod = tokens.nextToken();
			          }
			          else
			          {
			        	  mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			  			"MasteFeature.migration.CustomMigration.pattern.error",language)+newline);
			        	  return false;
			          }
//			          StringBuffer command = new StringBuffer();
//			          command.append("print program '");
//			          command.append(strProgram);
//			          command.append("' select classname dump |");
//
//			          String className = MqlUtil.mqlCommand(context, command.toString());
			          String className = MqlUtil.mqlCommand(context, "print program $1 select $2 dump $3",strProgram,"classname","|");
			          try
			          {
			        	  JPO.invokeLocal(context, className, new String[0], strMethod, new String[0]);
			          }
			          catch (Throwable err)
			          {
			        	  mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			  			"MasteFeature.migration.CustomMigration.inovkingJPO.error",language)+newline);
			              throw new Exception(err);
			          }
				}
			 }	          
	      }
	          
		
		 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"MasteFeature.migration.CustomMigration.end",language)+newline);
		return successful;
		
	}
	/* this method deletes all the object that are written to the delete log*/
	private boolean DeleteObsoleteandMigratedObject(Context context)throws Exception {
		boolean successful=true;
		String objectId = "";
		mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
		"MasteFeature.migration.Deleteobjects.start",language)+newline);
		
        try
        {
        	
            java.io.File file = new java.io.File(documentDirectory + "DeleteLog.txt");
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            while((objectId = fileReader.readLine()) != null){
              
              if(objectId !=null && !objectId.equals("") && !"null".equals(objectId)){
            	 DomainObject dmObj = new DomainObject(objectId);
            	 if(dmObj.exists(context)){
            		 dmObj.deleteObject(context);
                	 mqlLogWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
         			"MasteFeature.migration.Deleteobjects",language)+" "+ objectId + newline);
            	 }
              }
              
            }   
            
        }catch(Exception e){
			successful=false;
			mqlLogRequiredInformationWriter(EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"MasteFeature.migration.Deleteobjects.error",language) +e.toString()+newline);
		}
		 mqlLogWriter(newline+EnoviaResourceBundle.getProperty(context, SUITE_KEY,
			"MasteFeature.migration.Deleteobjects.end",language)+newline);
		return successful;		
	}
	
	 public void help(Context context, String[] args) throws Exception {
	        if (!context.isConnected()) {
	            throw new Exception("not supported on desktop client");
	        }

	        writer.write("================================================================================================\n");
	        writer.write(" Migration is a two step process  \n");
	        writer.write(" Step1: Find all objects and write them into flat files \n");
	        writer.write(" Example 1: \n");
	        writer.write(" FindObjects for variantConfiguration MasterFeature Migration: \n");
	        writer.write(" execute program emxVariantConfigurationFindObjects -method findObjects 1000  C:/Temp/oids; \n");
	        writer.write(" First parameter  = 1000 indicates no of oids per file \n");
	        writer.write(" Second parameter  = C:/Temp/oids is the directory where files should be written  \n");
	        writer.write(" \n");
	        writer.write(" Step2: Migrate the objects \n");
	        writer.write(" Example: \n");
	        writer.write(" execute program emxVariantConfigurationMigration 'C:/Temp/oids' 1 n ; \n");
	        writer.write(" First parameter  = C:/Temp/oids directory to read the files from\n");
	        writer.write(" Second Parameter = 1 minimum range  \n");
	        writer.write(" Third Parameter  = n maximum range  \n");
	        writer.write("        - value of 'n' means all the files starting from mimimum range\n");

	        writer.write("================================================================================================\n");
	        writer.write(" \n");
	        writer.write(" \n");

	        writer.write("================================================================================================\n");
	        writer.write(" Sequence of Migration for Migrating \"Master Features\" when updraging V6R2010 OR V6R2010x \n");
	        writer.write("================================================================================================\n");
	        writer.write(" \n");
	        
	        writer.write(" 1. Migrate Master Features  \n");
	        writer.write(" run the following Commands from MQL for Migration \n \n");
	        writer.write(" execute program emxVariantConfigurationFindObjects -method findObjects 1000  C:/Temp/MFids; \n");
	        writer.write(" execute program emxVariantConfigurationMigration 'C:/Temp/MFids' 1 n ; \n");
	        writer.write(" \n");

	        writer.write(" 2. Migration for Updating SeriesCount on models and Features \n");
	        writer.write(" run the following Commands from MQL for Migration \n \n");
	        writer.write(" execute program emxVariantConfigurationFindObjects -method findMFnModObjects 1000  C:/Temp/scids; \n");
	        writer.write(" execute program emxVariantConfigurationMigration C:/Temp/scids 1 n ; \n");
	        writer.write(" \n");


	        writer.write(" 3. Migration for Updating Product Feature List  \n");
	        writer.write(" run the following Commands from MQL for Migration \n \n");
	        writer.write(" execute program emxVariantConfigurationFindObjects -method findRelwithFeaAllocType 1000  C:/Temp/FeaAllocTypeRelids; \n");
	        writer.write(" execute program emxVariantConfigurationMigration C:/Temp/FeaAllocTypeRelids 1 n ; \n");

	        writer.close();
	    }

}
