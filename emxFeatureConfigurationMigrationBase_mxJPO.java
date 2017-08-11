/*
 ** emxFeatureConfigurationMigrationBase
 **
 ** Copyright (c) 2007-2016 Dassault Systemes. All Rights Reserved.
 **
 ** This program contains proprietary and trade secret information of Dassault Systemes. 
 ** Copyright notice is precautionary only and does not evidence 
 ** any actual or intended publication of such program.
 **
 */

    import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;
import matrix.db.Policy;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurableRulesUtil;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.PartFamily;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;

    /**
     * The <code>emxProductCentralMigrationBase</code> class contains migration script for PRCV10-5 Data
     * @author Raman,Enovia MatrixOne
     * @version ProductCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
     *
     */
    public class emxFeatureConfigurationMigrationBase_mxJPO extends emxDomainObject_mxJPO
    {
        private static final char OPEN_BRACE = '[';
        private static final char CLOSE_BRACE = ']';
        private static final char AND_SYMBOL = '&';
        private static final char OR_SYMBOL = '|';
        private static final char NOT_SYMBOL = '!';
        private static final char OBJ_SUBSTITUTE = 'X';
        private static final char C_INTER_TNR_SEPERATOR = '"';
        private static final String STR_INTRA_TNR_SEPERATOR = "::";
        private static int I_TNR_SEPER_LENGTH = STR_INTRA_TNR_SEPERATOR.length();
        private static final char C_FOPT_SEPERATOR = '~';

        private static final String FILE_HEADER = "emxProduct.Migration.Log.FileHeader";
        private static final String INVALID_RULE_FILE_URI = "emxProduct.Migration.Log.InvalidRulesFileURI";
        private static final String MIGRATION_INFO_FILE_URI = "emxProduct.Migration.Log.MigrationInformation";
        private static final String INVALID_FO = "emxProduct.Migration.Log.InvalidFOPairs";
        private static final String PARENT_NAME = "emxProduct.Migration.Log.ParentName";
        private static final String PARENT_TYPE = "emxProduct.Migration.Log.ParentType";
        private static final String RULE_NAME = "emxProduct.Migration.Log.RuleName";
        private static final String NO_INVALID_RULES = "emxProduct.Migration.Log.NoInvalidRules";
        private static final String START_MSG = "emxProduct.Migration.Log.StartMsg";
        private static final String ALREADY_DONE = "emxProduct.Migration.Log.AlreadyDone";
        private static final String SUCCESS_MIGRATION = "emxProduct.Migration.Log.SuccessMigration";
        private static final String FAILURE_MIGRATION = "emxProduct.Migration.Log.FailureMigration";
        private static final String SUCCESS_FEATURE_TYPE = "emxProduct.Migration.Log.SuccessFeatureType";
        private static final String SUCCESS_BCR = "emxProduct.Migration.Log.SuccessBCR";
        private static final String SUCCESS_IR = "emxProduct.Migration.Log.SuccessIR";
        private static final String MIGRATING_FEATURE_TYPE = "emxProduct.Migration.Log.MigratingFeatureType";
        private static final String MIGRATING_BCR = "emxProduct.Migration.Log.MigratingBCR";
        private static final String MIGRATING_IR = "emxProduct.Migration.Log.MigratingIR";
        private static final String ERROR_FL = "emxProduct.Migration.Log.ErrorFeatureList";
        private static final String ERROR_FEATURE_TYPE = "emxProduct.Migration.Log.ErrorFeatureType";
        private static final String ERROR_BCR = "emxProduct.Migration.Log.ErrorBCR";
        private static final String IR_CREATED = "emxProduct.Migration.Log.IRCreated";
        private static final String ERROR_IR1 = "emxProduct.Migration.Log.ErrorIR1";
        private static final String ERROR_IR2 = "emxProduct.Migration.Log.ErrorIR2";
        private static final String BCR = "emxProduct.Migration.Log.BCR";
        private static final String FL = "emxProduct.Migration.Log.FL";
        private static final String MIGRATED = "emxProduct.Migration.Log.Migrated";

        private static final String MIGRATING_GBOM_FROM = "emxProduct.Migration.Log.GBOMFROM";
        private static final String ERROR_GBOM_FROM = "emxProduct.Migration.Log.ErrorGBOMFrom";
        private static final String SUCCESS_GBOM_FROM = "emxProduct.Migration.Log.SuccessGBOMFrom";

        private static final String MIGRATING_PRODUCT_FEATURE_LIST = "emxProduct.Migration.Log.ProductFeatureList";
        private static final String ERROR_PRODUCT_FEATURE_LIST = "emxProduct.Migration.Log.ErrorProductFeatureList";
        private static final String SUCCESS_PRODUCT_FEATURE_LIST = "emxProduct.Migration.Log.SuccessProductFeatureList";


        private static FileOutputStream foLogFileInfo;
        private static PrintStream psWriter;
        public static MQLCommand mqlCommand = null;
        private static Date dtCurrentDate = new Date();
        private static String strFileURI="";
        private static BufferedWriter writer = null;
        private static boolean bWriteOnMqlConsole = true;
        private static String upgradeOnVersion="";
        //ADDED FOR PREVIEW BOM START
        private static final String TOP_LEVEL_PF_TYPE = "eServiceSuiteConfiguration.Configuration.DefaultTopLevelPartFamilyType";
        private static final String TOP_LEVEL_PF_NAME = "eServiceSuiteConfiguration.Configuration.DefaultTopLevelPartFamilyName";
        private static final String TOP_LEVEL_PF_REVISION = "eServiceSuiteConfiguration.Configuration.DefaultTopLevelPartFamilyRevision";
        //ADDED FOR PREVIEW BOM END

    /**
     * Create a new emxProductCentralMigrationBase object from a given id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxProductCentralMigrationBase Object
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     */
       public emxFeatureConfigurationMigrationBase_mxJPO (Context context, String[] args) throws Exception
        {
            super(context, args);
        }

        /**
         * Main entry point.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @return an integer status code (0 = success)
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         */
        public int mxMain (Context context, String[] args) throws Exception {
            if (!context.isConnected()) {
                String strContentLabel =EnoviaResourceBundle.getProperty
                        (context,"ProductLine","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
                throw  new Exception(strContentLabel);
            }
             migrate(context,args);
             return  0;
         }

        /**
         * this method calls the method migrateImpl with passing args array.
         * @param context the eMatrix <code>Context</code> object
         * @param args  - is a string array
         * @return void
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         */
        public void migrate(Context context,String[] args)
                    throws Exception {  	
        	//setting the file name and path 
           try{
             strFileURI = EnoviaResourceBundle.getProperty(context,MIGRATION_INFO_FILE_URI);
            }catch(Exception e){
                       strFileURI  = "c:/migrationLog.txt";
            }
            foLogFileInfo = new FileOutputStream(strFileURI,false);
            psWriter = new PrintStream(foLogFileInfo);
            //BufferedWriter writer to write on MQL Console
            writer = new BufferedWriter(new MatrixWriter(context));
            
            //getting .checking and initializing the args
            if (args!=null && args.length ==2)
            {
              String strVerbose = args[0];
              if (strVerbose!=null && ! "".equals(strVerbose) && "verboseoff".equalsIgnoreCase(strVerbose))
              {
                bWriteOnMqlConsole = false;
              }
              else {
              bWriteOnMqlConsole = true;
              }
              upgradeOnVersion = args[1];
              if(upgradeOnVersion!=null && ! "".equals(upgradeOnVersion)&& "null".equals(upgradeOnVersion)){
            	 return; 
              }
            	 
            }else{
            	migrationInfoWriter(bWriteOnMqlConsole,"Insufficient Arguments"+"\n");
            	foLogFileInfo.close();                
            	return;
            }


           
           
            String command = "print program eServiceSystemInformation.tcl select property[PRC10-5Migration] dump |";
            if(upgradeOnVersion.equals("POST_10.6")){
             command = "print program eServiceSystemInformation.tcl select property[PRC10-6Migration] dump |";
            }
            String result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
           

             boolean bAlreadyMigrated = false;
             if( result == null || "null".equals(result) || "".equals(result))
              {
                  bAlreadyMigrated = false;
              }
             else if ( result.length() > 0)
              {
               result = result.substring(result.indexOf(" value ") + 7);
               if(result != null && "Migrated".equalsIgnoreCase(result))
                  {
                      bAlreadyMigrated = true;
                  }
              }
              if (bAlreadyMigrated)
              {
                  migrationInfoWriter(bWriteOnMqlConsole,getString(context,"Once again we will do")+"\n");
                  if(upgradeOnVersion.equals("POST_10.6")){
                	  migrationInfoWriter(bWriteOnMqlConsole,getString(context,"emxProduct.Migration.Log.AlreadyDone10_6")+"\n");
                	  migrationInfoWriter(bWriteOnMqlConsole,getString(context,ALREADY_DONE)+"\n");
                  }else{
                	  
                	  migrationInfoWriter(bWriteOnMqlConsole,getString(context,ALREADY_DONE)+"\n");
                  }
              }
           else  if (migrateImpl(context,args)==0)
             {
                  String cmd = "modify program eServiceSystemInformation.tcl add property PRC10-5Migration value Migrated";
                  if(upgradeOnVersion.equals("POST_10.6")){
                	  cmd = "modify program eServiceSystemInformation.tcl add property PRC10-6Migration value Migrated";                	  
                	   migrationInfoWriter(bWriteOnMqlConsole,"emxProduct.Migration.Log.SuccessMigration10_6"+"\n");
                     }else{
                    	 migrationInfoWriter(bWriteOnMqlConsole,getString(context,SUCCESS_MIGRATION)+"\n");
                     }
                  MqlUtil.mqlCommand(context, mqlCommand,  cmd);
                 
             }
             else
             {
            	 if(upgradeOnVersion.equals("POST_10.6")){
            		 
            		 migrationInfoWriter(bWriteOnMqlConsole,getString(context,"emxProduct.Migration.Log.FailureMigration10_6")+"\n");
            	 }else{
            		 
            		 migrationInfoWriter(bWriteOnMqlConsole,getString(context,FAILURE_MIGRATION)+"\n");
            	 }
             }
       //Closes this fileoutput stream and releases any system resources associated with this stream
        foLogFileInfo.close();
        foLogFileInfo = null;
        }
        /**
         * Migrate the PRCV10-5 objects to PRCV10-6.
         * @param context the eMatrix <code>Context</code> object
         * @param args  - is a string array
         * @return an integer status code (0 = success)
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         */
        public int migrateImpl(Context context,String[] args)
                    throws Exception {
           try
          {
             migrationInfoWriter(bWriteOnMqlConsole,"=========================================\n");
             migrationInfoWriter(bWriteOnMqlConsole,"============"+dtCurrentDate+" ===========\n");
            
          if(upgradeOnVersion.equals("PRE_10.6")){
        	  migrationInfoWriter(bWriteOnMqlConsole,getString(context,START_MSG)+"\n");
        	  migrationInfoWriter(true,"=======================PRE_10.6==================\n");
              migrateFeatureType(context);
              migrateBCR(context);
              migrateIR(context);
          }else if (upgradeOnVersion.equals("POST_10.6")){
        	 // migrationInfoWriter(bWriteOnMqlConsole,getString(context,"emxProduct.Migration.Log.StartMsg10_6")+"\n");
        	  migrationInfoWriter(true,"==========================POST_10.6===============\n");
              migrationTechnicalFeature(context);
              migrationProductFeatureList(context);
              
           //Commenting for Task PRC 10.6 - Remove Create EBOM Feature
           migrateManufacturingFeature(context);
          }
          }
          catch (Exception ex){
            return 1;
          }
          return 0;

  }//end method
        
    /**
     * This method sets the Value of the attribute Feature Type on Feature List Object
       according to the value of the attributes Marketing Feature and Technical Feature
       of the corresponding Feature object.
     * @param context - The eMatrix <code>Context</code> object
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     * @grade 0
     */
      public static void migrateFeatureType(Context context)
                              throws Exception{

       migrationInfoWriter(bWriteOnMqlConsole,"\n\n=============="+getString(context,MIGRATING_FEATURE_TYPE)+" ====================\n\n");

       try
       {
        DomainObject domFL=null;
        String strSelMF = "";
        String strSelTF = "";
        String strName = "";
        StringBuffer sbWhereExpression = new StringBuffer();
        sbWhereExpression.append("relationship[");
        sbWhereExpression.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
        sbWhereExpression.append("]==True");
        //objectselects for Marketing Feature of the Feature
        StringBuffer sbSelMF = new StringBuffer("from[");
        sbSelMF.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
        sbSelMF.append("].to.attribute["+ProductLineConstants.ATTRIBUTE_MARKETING_FEATURE+"]");
        //objectselects for Technical Feature of the Feature
        StringBuffer sbSelTF = new StringBuffer("from[");
        sbSelTF.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO);
        sbSelTF.append("].to.attribute["+ProductLineConstants.ATTRIBUTE_TECHNICAL_FEATURE+"]");
        //Form the object select list for the Feature List
        List lstObjectSelects = new StringList();
        lstObjectSelects.add(DomainConstants.SELECT_NAME);
        lstObjectSelects.add(DomainConstants.SELECT_ID);
        lstObjectSelects.add(sbSelMF.toString());
        lstObjectSelects.add(sbSelTF.toString());
        //Get all the Feature List objects in the database
        List lstFLList = DomainObject.findObjects(
                                             context,
                                             ProductLineConstants.TYPE_FEATURE_LIST,
                                             DomainConstants.QUERY_WILDCARD,
                                             sbWhereExpression.toString(),
                                             (StringList) lstObjectSelects);
        if (lstFLList!=null)
        {
          Map flMap = new HashMap();
          for (int i = 0;i<lstFLList.size() ;i++ )
          {
             try
             {
                     flMap = (HashMap)lstFLList.get(i);
                     strName=(String)flMap.get(DomainConstants.SELECT_NAME);
                     strSelMF=(String)flMap.get(sbSelMF.toString());
                     strSelTF=(String)flMap.get(sbSelTF.toString());
                     //create a feature list object
                   domFL = DomainObject.newInstance(context,
                                                                 (String)flMap.get(DomainConstants.SELECT_ID));
                   if (strSelMF!=null &&
                       !(strSelMF.equalsIgnoreCase("null"))&&
                       !(strSelMF.equals(""))&&
                        strSelMF.equalsIgnoreCase("Yes"))
                   {

                     domFL.setAttributeValue(context,
                                             "Feature Type",
                                             "Marketing");
                   }
                   else if (strSelTF!=null &&
                           !(strSelTF.equalsIgnoreCase("null"))&&
                           !(strSelTF.equals(""))&&
                            strSelTF.equalsIgnoreCase("Yes"))
                   {
                     domFL.setAttributeValue(context,
                                             "Feature Type",
                                             "Technical");
                   }
                   else if (strSelTF!=null && !(strSelTF.equalsIgnoreCase("null")) && !(strSelTF.equals(""))&&
                            strSelTF.equalsIgnoreCase("No") &&
                            strSelMF!=null && !(strSelMF.equalsIgnoreCase("null")) && !(strSelMF.equals(""))&&
                            strSelMF.equalsIgnoreCase("No"))
                    {
                      domFL.setAttributeValue(context,
                                             "Feature Type",
                                             "None");
                    }
                    else {
                     domFL.setAttributeValue(context,
                                             "Feature Type",
                                             "Marketing");
                    }
              migrationInfoWriter(bWriteOnMqlConsole,getString(context,FL)+" "+strName+" "+getString(context,MIGRATED)+"\n");
             }
             catch (Exception e)
             {
               migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_FL)+"  "+strName+":\n");
               migrationInfoWriter(bWriteOnMqlConsole,getStackTrace(e));
               continue;
             }
          }//end for
        }//end if
              migrationInfoWriter(bWriteOnMqlConsole,"\n"+getString(context,SUCCESS_FEATURE_TYPE)+"\n\n");
        }
        catch (Exception e)
        {
             migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_FEATURE_TYPE)+" :\n");
             migrationInfoWriter(bWriteOnMqlConsole,getStackTrace(e));
        }
}//end Method

    /**
     * This method migrate the BCRs created in PRC10-5 to PRC10-6.
     * @param context - The eMatrix <code>Context</code> object
     * @return void
     * @since ProductCentral 10-6
     * @grade 0
     */
       public static void migrateBCR(Context context)
                              throws Exception{

       migrationInfoWriter(bWriteOnMqlConsole,"\n================"+getString(context,MIGRATING_BCR)+"=================\n\n");
       try
       {
        BusinessObject boTmp = null;
        List lstBCRParentId = new StringList();
        Object objBCRParentId = null;

        //select clause to get Parent Product/Feature
        StringBuffer sbSelParent = new StringBuffer("to[");
        sbSelParent.append(ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE);
        sbSelParent.append("].from.id");
        //Form the object select list for the BCR
        List lstObjectSelects = new StringList();
        lstObjectSelects.add(DomainConstants.SELECT_NAME);
        lstObjectSelects.add(DomainConstants.SELECT_ID);
        lstObjectSelects.add("attribute["+ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION+"]");
        lstObjectSelects.add("attribute["+ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]");
        lstObjectSelects.add(sbSelParent.toString());
        //Get all the BCR objects in the database
        List lstBCRList = DomainObject.findObjects(
                                             context,
                                             ProductLineConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE,
                                             DomainConstants.QUERY_WILDCARD,
                                             null,
                                             (StringList) lstObjectSelects);
        if (lstBCRList!=null)
        {
          List lstFOID = null;
          String strRuleId = "";
          String strName = "";
          Map bcrMap = new HashMap();
          for (int i = 0;i<lstBCRList.size() ;i++ )
          {
              try
                        {
                           bcrMap = (Map)lstBCRList.get(i);
                           strRuleId = (String)bcrMap.get(DomainConstants.SELECT_ID);
                           strName = (String)bcrMap.get(DomainConstants.SELECT_NAME);
                           DomainObject domObj = newInstance(context, strRuleId);

                          //in case of OCR LE and RE are blank.they dont need to be migrated.
                          if ("".equals((String)bcrMap.get("attribute["+ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION+"]")) &&
                              "".equals((String)bcrMap.get("attribute["+ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]")) )
                          {
                           continue;
                          }
                          //Open transaction boundary
                           ContextUtil.startTransaction(context, true);
                          //convert Left Expression attribute to Rel attribute in new data model

                           /*list of Maps containing the FeatureList Ids' of all the
                             feature-option pairs in the given expression.*/
                           lstFOID = getFOPT_OIDs(context,(String)bcrMap.get("attribute["+ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION+"]"));
                           //store the expression tokens in relationship attributes

                           RelationshipType rtLE  = new RelationshipType(ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION);
                           //DISCONNECT ALL THE Left Expression Relationships
                           for (int j = 0; j < lstFOID.size(); j++)
                           {
                             String strFLId = (String)lstFOID.get(j);
                             if(strFLId!=null&&!strFLId.equalsIgnoreCase("null")&&!strFLId.equals(""))
                                  {
                                     if (isAlreadyConnected(context,
                                                            strRuleId,
                                                            strFLId,
                                                            ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION))
                                     {
                                      boTmp = new BusinessObject(strFLId);
                                      domObj.disconnect(context, rtLE, true, boTmp);
                                     }
                                  }
                           }
                           ConnectExpression(context,
                                            (String)bcrMap.get("attribute["+ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION+"]"),
                                            rtLE,
                                            strRuleId,
                                            lstFOID);

                         //Repeat the process for Right Expression

                           lstFOID = getFOPT_OIDs(context,(String)bcrMap.get("attribute["+ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]"));
                           RelationshipType rtRE  = new RelationshipType(ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION);
                           //disconnect ALL THE Right Expression Relationships
                           for (int j = 0; j < lstFOID.size(); j++)
                           {
                             String strFLId = (String)lstFOID.get(j);
                             if(strFLId!=null&&!strFLId.equalsIgnoreCase("null")&&!strFLId.equals("")){
                             if (isAlreadyConnected(context,
                                                    strRuleId,
                                                    strFLId,
                                                    ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION))
                                 {
                                  boTmp = new BusinessObject(strFLId);
                                  domObj.disconnect(context, rtRE, true, boTmp);
                                 }
                             }
                           }
                           ConnectExpression(context,
                                            (String)bcrMap.get("attribute["+ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]"),
                                            rtRE,
                                            strRuleId,
                                            lstFOID);
                           //close the transaction boundary in case of successful updation
                            ContextUtil.commitTransaction(context);


                         objBCRParentId = (Object)bcrMap.get(sbSelParent.toString());
                         if(objBCRParentId != null)
                                {
                                   if(objBCRParentId instanceof List)
                                    {
                                     lstBCRParentId.addAll((StringList)objBCRParentId);
                                    }
                                   else if(objBCRParentId instanceof String)
                                    {
                                     lstBCRParentId.add((String)objBCRParentId);
                                    }
                                }
                     migrationInfoWriter(bWriteOnMqlConsole,(getString(context,BCR)) +" "+strName+ " "+getString(context,MIGRATED)+"\n");

                  }
                  catch (Exception e)
                  {
                      migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_BCR)+" "+strName+":\n");
                      migrationInfoWriter(bWriteOnMqlConsole,getStackTrace(e));
                      continue;
                  }
           }//end for
        }//end if

             //Make a log of the Invalid Rules
             logInvalidRules(context,lstBCRParentId);
             migrationInfoWriter(bWriteOnMqlConsole,"\n"+getString(context,SUCCESS_BCR)+"  "+strFileURI+".");
        }
        catch (Exception e)
        {
             migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_BCR)+"s \n"+getStackTrace(e));
        }
    }//end Method

     /**
     * This method will make a log file of  all the Invalid Rules in the Database.
     * @param context - The eMatrix <code>Context</code> object
     * @param lstBCRParentId - List : list of parent Product/Feature
     * @return void
     * @since ProductCentral 10-6
     * @grade 0
     */
     public static void logInvalidRules(Context context,List lstBCRParentId)
               throws Exception{
          boolean bInvalidRuleFound = false;
          StringBuffer sbLogEntry = new StringBuffer();
          try{
           strFileURI = EnoviaResourceBundle.getProperty(context,INVALID_RULE_FILE_URI);
          }catch(Exception e){
                      strFileURI  = "c:/invalidRulesLog.txt";
          }
          FileOutputStream foLogFile = new FileOutputStream(strFileURI,false);
          //create the file headers
          sbLogEntry.append("=========== "+dtCurrentDate+" ====================\n");
          sbLogEntry.append("===========");
          sbLogEntry.append(getString(context,FILE_HEADER));
          sbLogEntry.append("===========\n");
          foLogFile.write((sbLogEntry.toString()).getBytes());
          sbLogEntry.delete(0,sbLogEntry.length());


    if (lstBCRParentId != null)
      {

          lstBCRParentId = removeDupilacate(lstBCRParentId);
          Map invalidMap = new HashMap();
          String strBCRname = "";
          String strParentType = "";
          List lstInvalidBCR = new MapList();
          List invalidFOlist = new ArrayList();
          String strFOname ="";


          /*for each Product/feature Connected to rule call the method validateBCRforInvalidFOReference
            to return the list of invalid Rules*/
          for (int j = 0;j<lstBCRParentId.size() ; j++)//outer for
           {

            DomainObject domParent = DomainObject.newInstance(context,(String)lstBCRParentId.get(j));
            //get the List of Invalid Rules for each Parent product/feature
            lstInvalidBCR = ConfigurableRulesUtil.validateBCRforInvalidFOReference
                                                      (context,(String)lstBCRParentId.get(j));

                //loop thru the list of Invalid BCRs and generate the Message String
           for (int k = 0;k<lstInvalidBCR.size() ; k++)
               {
                    bInvalidRuleFound = true;
                    invalidMap = (HashMap)lstInvalidBCR.get(k);
                    strBCRname = (String)invalidMap.get("BCRNAME");
                    sbLogEntry.append(getString(context,RULE_NAME)).append(":");
                    sbLogEntry.append(strBCRname);
                    sbLogEntry.append("\n");
                    sbLogEntry.append(getString(context,PARENT_NAME)).append(":");
                    sbLogEntry.append(domParent.getInfo(context,DomainConstants.SELECT_NAME));
                    sbLogEntry.append("\n");
                    sbLogEntry.append(getString(context,PARENT_TYPE)).append(":");
                    strParentType = domParent.getInfo(context,DomainConstants.SELECT_TYPE);
                    sbLogEntry.append(i18nNow.getTypeI18NString(strParentType,context.getSession().getLanguage()));
                    sbLogEntry.append("\n");
                    invalidFOlist = (ArrayList)invalidMap.get("INVALID_LIST");
                    sbLogEntry.append(getString(context,INVALID_FO)).append(":");

                    //loop thru the Invalid FO List
                    for(int l=0;l<invalidFOlist.size();l++)
                        {
                             strFOname = (String)invalidFOlist.get(l);
                             if (l!=0)
                               {
                                 sbLogEntry.append("\n                             ");
                               }
                             sbLogEntry.append(strFOname);
                         }//end for
                   sbLogEntry.append("\n==========================");
                   sbLogEntry.append("=================");
                   sbLogEntry.append("\n==========================-");
                   sbLogEntry.append("===============\n");
                   foLogFile.write((sbLogEntry.toString()).getBytes());
                   sbLogEntry.delete(0,sbLogEntry.length());
               }//end for
               }//end outer for
      }//end if
      if (!bInvalidRuleFound)
        {
         sbLogEntry.append(getString(context,NO_INVALID_RULES));
         foLogFile.write((sbLogEntry.toString()).getBytes());
         sbLogEntry.delete(0,sbLogEntry.length());
        }
     }//end Method

    /**
     * This method will return a list of Maps containing the FeatureList Ids' of all the feature-option pairs in the given expression.
     * @param context - The eMatrix <code>Context</code> object
     * @param strExpression - String : The expression to be parsed
     * @return List - a list of Maps containing the FeatureList Ids' of all the feature-option pairs in the given expression
     * @since ProductCentral 10-6
     * @grade 0
     */
    public static List getFOPT_OIDs(Context context, String strExpr) throws FrameworkException
    {
        if (strExpr!=null &&strExpr.length()>0)
        {
                List lstTNR = lstGetTNR(strExpr);
                List lstFLids=new MapList();
                Map mapFOPT = null;
                List lstDBRes = null;
                StringList lstObjSel=new StringList();
                lstObjSel.addElement(DomainConstants.SELECT_ID);
                StringBuffer stbWhrCl=null;

                for (int iTmp = 0; iTmp < lstTNR.size(); iTmp++)
                {
                    mapFOPT = (Map) lstTNR.get(iTmp);
                    stbWhrCl=new StringBuffer("(\"relationship[");
                    stbWhrCl.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                    .append("].from.")
                    .append(DomainConstants.SELECT_NAME)
                    .append("\"~=\"");
                    stbWhrCl.append((String) mapFOPT.get(ConfigurableRulesUtil.STR_FNAM));
                    stbWhrCl.append("\")");

                    stbWhrCl.append("&&(\"relationship[");
                    stbWhrCl.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                    .append("].from.")
                    .append(DomainConstants.SELECT_TYPE)
                    .append("\"~=\"");
                    stbWhrCl.append((String) mapFOPT.get(ConfigurableRulesUtil.STR_FTYP));
                    stbWhrCl.append("\")");

                    stbWhrCl.append("&&(\"relationship[");
                    stbWhrCl.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)
                    .append("].from.")
                    .append(DomainConstants.SELECT_REVISION)
                    .append("\"~=\"");
                    stbWhrCl.append((String) mapFOPT.get(ConfigurableRulesUtil.STR_FREV));
                    stbWhrCl.append("\")");

                    stbWhrCl.append("&&(\"relationship[");
                    stbWhrCl.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO)
                    .append("].to.")
                    .append(DomainConstants.SELECT_TYPE)
                    .append("\"~=\"");
                    stbWhrCl.append((String) mapFOPT.get(ConfigurableRulesUtil.STR_OTYP));
                    stbWhrCl.append("\")");

                    stbWhrCl.append("&&(\"relationship[");
                    stbWhrCl.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO)
                    .append("].to.")
                    .append(DomainConstants.SELECT_REVISION)
                    .append("\"~=\"");
                    stbWhrCl.append((String) mapFOPT.get(ConfigurableRulesUtil.STR_OREV));
                    stbWhrCl.append("\")");

                    stbWhrCl.append("&&(\"relationship[");
                    stbWhrCl.append(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO)
                    .append("].to.")
                    .append(DomainConstants.SELECT_NAME)
                    .append("\"~=\"");
                    stbWhrCl.append((String) mapFOPT.get(ConfigurableRulesUtil.STR_ONAM));
                    stbWhrCl.append("\")");
                    lstDBRes =
                        DomainObject.findObjects(
                            context,
                            ProductLineConstants.TYPE_FEATURE_LIST,
                            DomainConstants.QUERY_WILDCARD,
                            stbWhrCl.toString(),
                            lstObjSel
                            );
                    if (lstDBRes==null ||lstDBRes.size()==0||lstDBRes.isEmpty())
                      {
                        lstFLids.add("null");
                      }
                    else
                      {
                       lstFLids.add((String)((Map)(lstDBRes.get(0))).get(DomainConstants.SELECT_ID));
                      }
                }
              return lstFLids;
     }
     else {
            return new StringList("");
      }
    }

/**
     * This method is called by Create/Edit of Rules ( Boolean Compatibility, Product Compatibility Rules)
     * This method stores the expression tokens in relationship attributes. It calls getParsedExpression
     * to get a space trimmed expression which will be easy to process.
     *
     * @param context -          The eMatrix <code>Context</code> object.
     * @param strExpression -       The expression that user enters in the UI form; Create/Edit dialog.
     * @param relationshipType - The relationship with which the objects need to be connected.
     *                           in the current data Model it is either Left Expression/Right Expression.
     * @param strRuleId-             The ObjectId of the context Rule.
     * @param lstobjIds -            The List of Object Ids corresponding to the objects selected while forming the
     *                           Left/Right Expression.
     * @return -                 void
     *
     * @throws Exception
     * @since ProductCentral 10-6
     * @grade 0
    **/

    public static void ConnectExpression(Context context,
                                         String strExpression,
                                         RelationshipType relationshipType,
                                         String strRuleId,
                                         List lstobjIds)
        throws Exception
    {
        strExpression=getParsedExpression(strExpression);
        DomainObject domTmp = DomainObject.newInstance(context);
        DomainRelationship rel=new DomainRelationship();

        int i=0;
        int iobjCounter=0;
        int iseqOrder=1;
        String strTemp="";
        boolean bobjConnect=false;
        try{

                domTmp.setId(strRuleId);
                relationshipType.open(context);

                while(i<strExpression.length())
                {
                    // Begin of modify by Enovia MatrixOne for Bug # 306380 Date 06/21/2005
                    if(strExpression.charAt(i)==OPEN_BRACE)
                    {
                        strTemp="(";
                    }
                    else if(strExpression.charAt(i)==CLOSE_BRACE)
                    {
                        strTemp=")";
                    }
                    // End of modify by Enovia MatrixOne for Bug # 306380 Date 06/21/2005
                    else if(strExpression.charAt(i)==AND_SYMBOL)
                    {
                        strTemp="AND";
                    }
                    else if(strExpression.charAt(i)==OR_SYMBOL)
                    {
                        strTemp="OR";
                    }
                    else if(strExpression.charAt(i)==NOT_SYMBOL)
                    {
                        strTemp="NOT";
                    }
                    else if(strExpression.charAt(i)==OBJ_SUBSTITUTE)
                    {
                        bobjConnect=true;
                    }
                    if(bobjConnect)
                    {

                        if (!((String) lstobjIds.get(iobjCounter)).equals("null"))
                        {
                          rel=domTmp.addToObject(
                                        context,
                                        relationshipType,
                                        (String) lstobjIds.get(iobjCounter));
                          rel.setAttributeValue(context,ProductLineConstants.ATTRIBUTE_SEQUENCE_ORDER,String.valueOf(iseqOrder));
                        }

                        iobjCounter++;
                        bobjConnect=false;
                    }
                    else
                    {
                        rel=domTmp.addToObject(
                                        context,
                                        relationshipType,
                                        strRuleId);
                        rel.setAttributeValue(context,"Token",strTemp);
                        rel.setAttributeValue(context,ProductLineConstants.ATTRIBUTE_SEQUENCE_ORDER,String.valueOf(iseqOrder));

                    }
                    iseqOrder++;
                    i++;
                }
                relationshipType.close(context);
            }

            catch (Exception e)
            {
                e.printStackTrace();
                //The exception with appropriate message is thrown to the caller.
                throw e;
            }
    }

    /**
     * This method migrate the Iunclusion Rules created in PRC10-5 to PRC10-6.
     * @param context - The eMatrix <code>Context</code> object
     * @return void
     * @since ProductCentral 10-6
     * @grade 0
     */

       public static void migrateIR(Context context)
                      throws Exception{

       migrationInfoWriter(bWriteOnMqlConsole,"\n\n================"+getString(context,MIGRATING_IR)+"=================\n\n");
       String strSelFLParentId = "to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.id";
       String strSelFLParentType = "to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].from.type";
       String strSelGBOMParentId = "to["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"].from.id";
       String strSelGBOMParentType = "to["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"].from.type";

       List lstProductTypes = ProductLineUtil.getChildrenTypes(context,
                                                                                               ProductLineConstants.TYPE_PRODUCTS);

       try
       {
          ProductLineCommon PRCCommonInstance = new ProductLineCommon();
          //type pattern
        StringBuffer sbTypePattern = new StringBuffer(ProductLineConstants.TYPE_FEATURE_LIST);
        sbTypePattern.append(",");
        sbTypePattern.append(ProductLineConstants.TYPE_GBOM);
        //Form the object select list for the BCR
        List lstObjectSelects = new StringList();
        lstObjectSelects.add(DomainConstants.SELECT_NAME);
        lstObjectSelects.add(DomainConstants.SELECT_ID);
        lstObjectSelects.add(DomainConstants.SELECT_TYPE);
        lstObjectSelects.add(DomainConstants.SELECT_VAULT);
        lstObjectSelects.add(DomainConstants.SELECT_OWNER);
        lstObjectSelects.add("attribute["+ProductLineConstants.ATTRIBUTE_EXPRESSION+"]");
        lstObjectSelects.add(strSelFLParentId);
        lstObjectSelects.add(strSelFLParentType);
        lstObjectSelects.add(strSelGBOMParentId);
        lstObjectSelects.add(strSelGBOMParentType);


        //Get all the FL and GBOM objects in the database
        List lstFLGBOMList = DomainObject.findObjects(
                                             context,
                                             sbTypePattern.toString(),
                                             DomainConstants.QUERY_WILDCARD,
                                             "attribute["+ProductLineConstants.ATTRIBUTE_EXPRESSION+"]!=\"\"",
                                             (StringList) lstObjectSelects);
        if (lstFLGBOMList!=null)
        {
          List lstFOID = null;
          Map fLGBOMMap = new HashMap();
          String strExp = "";
          String strIncRuleID = "";
          String strVault = "";
          String strName = "";
          String strType = "";
          String strFLParentId = "";
          String strFLParentType = "";
          String strGBOMParentId = "";
          String strGBOMParentType = "";
          String strParentProductId = "";
          String strOwner = "";
          boolean bConnectThroughLR = false;
          DomainRelationship domRel = null;
          /*Loop through the list of FL and GBOMs ,fetch the Expression attribute ,
            create Inclusion Rule object and finally connect it to other objects*/
          for (int i = 0;i<lstFLGBOMList.size() ;i++ )
          {
              try
                      {
                      fLGBOMMap = (Map)lstFLGBOMList.get(i);
                      strExp = (String)fLGBOMMap.get("attribute["+ProductLineConstants.ATTRIBUTE_EXPRESSION+"]");
                      strVault = (String)fLGBOMMap.get(DomainConstants.SELECT_VAULT);
                      strName = (String)fLGBOMMap.get(DomainConstants.SELECT_NAME);
                      strType = (String)fLGBOMMap.get(DomainConstants.SELECT_TYPE);
                      strOwner = (String)fLGBOMMap.get(DomainConstants.SELECT_OWNER);
                      strFLParentId = (String)fLGBOMMap.get(strSelFLParentId);
                      strFLParentType = (String)fLGBOMMap.get(strSelFLParentType);
                      strGBOMParentId = (String)fLGBOMMap.get(strSelGBOMParentId);
                      strGBOMParentType = (String)fLGBOMMap.get(strGBOMParentType);
                      strParentProductId = "";
                      bConnectThroughLR = false;

                      if (strExp!=null &&
                         (!(strExp.equalsIgnoreCase("null")))&&
                         (!(strExp.equals("")))
                          )
                       {
                           //Open transaction boundary
                           ContextUtil.startTransaction(context, true);
                           //create a new Inclusion rule object
                           strIncRuleID =
                                    PRCCommonInstance.create(
                                        context,
                                        ProductLineConstants.TYPE_INCLUSION_RULE,
                                        DomainConstants.EMPTY_STRING,
                                        "-",
                                        DomainConstants.EMPTY_STRING,
                                        DomainConstants.EMPTY_STRING,
                                        strVault,
                                        null,
                                        strOwner,
                                        (String)fLGBOMMap.get(DomainConstants.SELECT_ID),
                                        ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION,
                                        false);
                         //convert Expression attribute to Rel attribute in new data model

                           /*list of Maps containing the FeatureList Ids' of all the
                             feature-option pairs in the given expression.*/
                           lstFOID = getFOPT_OIDs(context,strExp);
                           //store the expression tokens in relationship attributes
                           ConnectExpression(context,
                                            strExp,
                                            new RelationshipType(ProductLineConstants.RELATIONSHIP_RIGHT_EXPRESSION),
                                            strIncRuleID,
                                            lstFOID);

                           if (strType!=null && !"".equals(strType) && !"null".equalsIgnoreCase(strType))
                           {
                              if ((ProductLineConstants.TYPE_FEATURE_LIST).equals(strType))
                              {
                                if ( (strFLParentId!=null && !"".equals(strFLParentId) && !"null".equalsIgnoreCase(strFLParentId)) &&
                                     (strFLParentType!=null && !"".equals(strFLParentType) && !"null".equalsIgnoreCase(strFLParentType) &&
                                      lstProductTypes.contains(strFLParentType))
                                    )
                                {
                                  strParentProductId = strFLParentId;
                                  bConnectThroughLR = true;
                                }
                              }
                            else if ((ProductLineConstants.TYPE_GBOM).equals(strType))
                              {
                                if ( (strGBOMParentId!=null && !"".equals(strGBOMParentId) && !"null".equalsIgnoreCase(strGBOMParentId)) &&
                                     (strGBOMParentType!=null && !"".equals(strGBOMParentType) && !"null".equalsIgnoreCase(strGBOMParentType) &&
                                      lstProductTypes.contains(strGBOMParentType))
                                    )
                                {
                                  strParentProductId = strGBOMParentId;
                                  bConnectThroughLR = true;
                                }
                              }
                           }
                        if (strIncRuleID!=null && !"".equals(strIncRuleID) && !"null".equalsIgnoreCase(strIncRuleID) &&
                             bConnectThroughLR)
                        {
                          domRel.connect(context,
                                   strParentProductId,
                                   ProductLineConstants.RELATIONSHIP_LOCAL_RULE,
                                   strIncRuleID,
                                   false);
                        }
                          ContextUtil.commitTransaction(context);
                       }//end if
                     migrationInfoWriter(bWriteOnMqlConsole,getString(context,IR_CREATED)+" "+strName+"\n");
                      }
                      catch (Exception e)
                      {
                         migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_IR1)+" "+strName+":\n");
                         migrationInfoWriter(bWriteOnMqlConsole,getStackTrace(e));
                         continue;
                      }
          }//end for
        }//end if
             migrationInfoWriter(bWriteOnMqlConsole,"\n"+getString(context,SUCCESS_IR)+"\n\n");
        }
        catch (Exception e)
        {
             migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_IR2)+" \n"+getStackTrace(e));
        }
}//end Method

/**
     * This method is called by ConnectExpression method
     * This method parses the input expression and removes the spaces simplifies the format
     * For example ["A" AND "B"] OR NOT "C" will be converted to [X&X]|!X.
     *
     * @param strExpression -           The expression to be parsed
     * @return strParsedExpression-     The parsed String
     * @since ProductCentral 10-6
     * @grade 0
    **/
    public static String getParsedExpression(String strExpression)
    {

          StringBuffer strParsedExpression = new StringBuffer(400);
          int i=0;

          while(i<strExpression.length())
          {
                if(strExpression.charAt(i)==OPEN_BRACE||strExpression.charAt(i)==CLOSE_BRACE)
                {
                    strParsedExpression = strParsedExpression.append(strExpression.charAt(i));
                    i++;
                }
                else if(strExpression.charAt(i)==C_INTER_TNR_SEPERATOR)
                {
                    i = strExpression.indexOf(C_INTER_TNR_SEPERATOR,i+1) + 1;
                    strParsedExpression = strParsedExpression.append(OBJ_SUBSTITUTE);
                }
                else if (strExpression.charAt(i)=='A' || strExpression.charAt(i)=='a')
                {
                    i=i+3;
                    strParsedExpression = strParsedExpression.append(AND_SYMBOL);
                }
                else if (strExpression.charAt(i)=='O' || strExpression.charAt(i)=='o')
                {
                    i=i+2;
                    strParsedExpression = strParsedExpression.append(OR_SYMBOL);
                }
                else if (strExpression.charAt(i)=='N' || strExpression.charAt(i)=='n')
                {
                    i=i+3;
                    strParsedExpression = strParsedExpression.append(NOT_SYMBOL);
                }
                else
                {
                i++;
                }
        }
        return strParsedExpression.toString();
    }

   /**
     * This method is called by migrateBCR method
     * This method removes the dupliacte values from the list
     * @param lstInput -       The Input List
     * @return lstOutput-      The Output List
     * @since ProductCentral 10-6
     * @grade 0
    **/
   public static List removeDupilacate(List lstInput)
       throws Exception
    {
      int iNoOfOccurence = 0;
      List lstOutput = new ArrayList(lstInput);
      for (int i=0;i<lstInput.size() ;i++ )
        {
           iNoOfOccurence = 0;
           for (int j=0;j<lstOutput.size() ;j++ )
            {
             if ( ((String)lstOutput.get(j)).equals((String)lstInput.get(i)) )
             {
              iNoOfOccurence++;
              if(iNoOfOccurence>1)
                 {
                   lstOutput.remove(j);
                   j--;
                 }
             }//end if

            }//end inner for
        }//end outer for
    return lstOutput;
    }


    /**
     * This method is called by migrateBCR method
     * It finds whether two objects are connected through a Particular Relationship or not.
     * @param context - The eMatrix <code>Context</code> object
     * @param strFromId -       from side Object Id
     * @return strToId-         to side Object Id
     * @return strRelName-      relationship name
     * @since ProductCentral 10-6
     * @grade 0
    **/


   public static boolean isAlreadyConnected(Context context,
                                            String strFromId,
                                            String strToId,
                                            String strRelName)
       throws Exception
    {
      DomainObject domFL = DomainObject.newInstance(context,strToId);
      StringBuffer sbWhereExpression = new StringBuffer("from[");
      sbWhereExpression.append(strRelName);
      sbWhereExpression.append("].to.id==").append(strToId);
      sbWhereExpression.append("&& id==").append(strFromId);
      List lstRelatedObj = (MapList)domFL.getRelatedObjects(context,
                                                   strRelName,
                                                   DomainConstants.QUERY_WILDCARD,
                                                   null,
                                                   new StringList(DomainConstants.SELECT_ID),
                                                   true,
                                                   false,
                                                   (short)1,
                                                   sbWhereExpression.toString(),
                                                   DomainConstants.EMPTY_STRING, 0 );
      if (lstRelatedObj!=null&&lstRelatedObj.size()!=0)
      {
        return true;
      }
      else
      {
        return false;
      }
  }//end method

   /**
    * This method sets the Value of the attribute Feature Type on Feature List Object
    * according to the value of the attribute Manufacturing Feature
    * @param context - The eMatrix <code>Context</code> object
    * @return void
    * @throws Exception if the operation fails
    * @since ProductCentral 10-6
    * @grade 0
    */
    public static void migrateManufacturingFeature(Context context)
                              throws Exception
    {
        try
        {
            //objectselects for Manufacturing Feature of the Feature List
            StringList objSelect = new StringList(2);
            objSelect.addElement("attribute[" + ProductLineConstants.ATTRIBUTE_MANUFACTURING_FEATURE + "]");
            objSelect.addElement(DomainConstants.SELECT_ID);

            //Get all the Feature List objects in the database
            MapList lstFLList = DomainObject.findObjects(
                                           context,
                                           ProductLineConstants.TYPE_FEATURE_LIST,
                                           DomainConstants.QUERY_WILDCARD,
                                           "",
                                           objSelect);
            if (lstFLList!=null)
            {
                for (int i = 0;i<lstFLList.size() ;i++ )
                {
                    Map flMap = (Map)lstFLList.get(i);

                    String attMFGFeature = (String)flMap.get("attribute[" + ProductLineConstants.ATTRIBUTE_MANUFACTURING_FEATURE + "]");
                    //Manufacturing Feature setting will take the precedence over Marketting & Technical
                    if (attMFGFeature != null && "Yes".equalsIgnoreCase(attMFGFeature))
                    {
                        DomainObject domFL = DomainObject.newInstance(context,
                                                           (String)flMap.get(DomainConstants.SELECT_ID));

                        domFL.setAttributeValue(context,
                                   ProductLineConstants.ATTRIBUTE_FEATURE_TYPE,
                                   "Manufacturing");
                    }
                }//end for
            }//end if
        }
        catch (Exception e)
        {
            throw e;
        }
  }//end Method

    /**
     * This method returns internalized value for the passed key in
       ProductCentral string resouce file.
     * @param context - The eMatrix <code>Context</code> object
     * @param strKey - Property file entry
     * @since ProductCentral 10-6
     * @grade 0
     */
  private static String getString(Context context,String strKey)throws FrameworkException
        {
           String strLocale = context.getSession().getLanguage();
           return EnoviaResourceBundle.getProperty(context,"Configuration",strKey,strLocale);
        }

    /**
     * This method returns the stacktrace for any Throwable object.
     * @param throwable - Any throwable object
     * @since ProductCentral 10-6
     * @grade 0
     */
  public static String getStackTrace(Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
    /**
     * This method will return a list of Maps containing the TNR details of all the feature-option pairs in the given expression
     * @param context - The eMatrix <code>Context</code> object
     * @param strExpression - String : The expression to be parsed
     * @return List - a list of Maps containing the TNR details of all the feature-option pairs in the given expression
     * @since ProductCentral 10-0-5-0
     * @grade 0
     */
    public static List lstGetTNR(String strExpression)
    {
        List lstTNR = new ArrayList();
        if (strExpression.equals(""))
            return lstTNR;
        Map mapTNR = null;
        int iIndexInterTNR = -1;
        int iIndexIntraTNR = 0;
        int iIndexFOPT = 0;
        int iIndexIntraTNROld = 0;
       
        do
        {
            iIndexInterTNR =
                strExpression.indexOf(
                    C_INTER_TNR_SEPERATOR,
                    iIndexInterTNR + 1);
            if (iIndexInterTNR == -1)
                break;

            mapTNR = new HashMap();

            iIndexIntraTNROld = iIndexIntraTNR;
            iIndexIntraTNR =
                strExpression.indexOf(
                    STR_INTRA_TNR_SEPERATOR,
                    iIndexInterTNR + 1);

            mapTNR.put(
                ConfigurableRulesUtil.STR_FTYP,
                strExpression.substring(iIndexInterTNR + 1, iIndexIntraTNR));

            iIndexIntraTNROld = iIndexIntraTNR;
            iIndexIntraTNR =
                strExpression.indexOf(
                    STR_INTRA_TNR_SEPERATOR,
                    iIndexIntraTNR + 1);
            mapTNR.put(
                ConfigurableRulesUtil.STR_FNAM,
                strExpression.substring(
                    iIndexIntraTNROld + I_TNR_SEPER_LENGTH,
                    iIndexIntraTNR));

            iIndexFOPT =
                strExpression.indexOf(C_FOPT_SEPERATOR, iIndexInterTNR + 1);

            mapTNR.put(
                ConfigurableRulesUtil.STR_FREV,
                strExpression.substring(
                    iIndexIntraTNR + I_TNR_SEPER_LENGTH,
                    iIndexFOPT));

            iIndexIntraTNROld = iIndexIntraTNR;
            iIndexIntraTNR =
                strExpression.indexOf(
                    STR_INTRA_TNR_SEPERATOR,
                    iIndexIntraTNR + 1);
            mapTNR.put(
                ConfigurableRulesUtil.STR_OTYP,
                strExpression.substring(iIndexFOPT + 1, iIndexIntraTNR));

            iIndexIntraTNROld = iIndexIntraTNR;
            iIndexIntraTNR =
                strExpression.indexOf(
                    STR_INTRA_TNR_SEPERATOR,
                    iIndexIntraTNR + 1);
            mapTNR.put(
                ConfigurableRulesUtil.STR_ONAM,
                strExpression.substring(
                    iIndexIntraTNROld + I_TNR_SEPER_LENGTH,
                    iIndexIntraTNR));

            iIndexInterTNR =
                strExpression.indexOf(
                    C_INTER_TNR_SEPERATOR,
                    iIndexInterTNR + 1);
            mapTNR.put(
                ConfigurableRulesUtil.STR_OREV,
                strExpression.substring(
                    iIndexIntraTNR + I_TNR_SEPER_LENGTH,
                    iIndexInterTNR));
            lstTNR.add(mapTNR);

        }
        while (iIndexInterTNR != -1);
        return lstTNR;
    }
    /**
     * This method writes to the log file using the Printwriter object
       and to MQL console using BufferedWriter object.
     * @param bVerbose - to show output on MQL console
     * @param strLogEnrty - Message to write
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     * @grade 0
     */
      public static void migrationInfoWriter(boolean bVerbose,String strLogEnrty)
                              throws Exception
          {
              if (strLogEnrty!=null && strLogEnrty.length()>0)
                      {
                          psWriter.print(strLogEnrty);
                          if (bVerbose)
                              {
                               writer.write(strLogEnrty);
                               writer.flush();
                               }
                      }
              }
      





      /**
       * This method sets the Value of the GBOM From to Feature List Object
         to the attributes Marketing Feature and Technical Feature
         of the corresponding Feature object.
       * @param context - The eMatrix <code>Context</code> object
       * @return void
       * @throws Exception if the operation fails
       * @since ProductCentral 10-6
       * @grade 0
       */
      
    public static void migrationTechnicalFeature(Context context)
            throws Exception {
        try 
        {
            migrationInfoWriter(bWriteOnMqlConsole,"\n\n=============="+getString(context,MIGRATING_GBOM_FROM)+" ====================\n\n");
            StringList objSelect = new StringList(2);
            objSelect.addElement(DomainConstants.SELECT_ID);
            objSelect.addElement(DomainConstants.SELECT_NAME);            
            
            // Get all the Feature List objects in the database
            MapList lstFLList = DomainObject.findObjects(context,ProductLineConstants.TYPE_FEATURES,DomainConstants.QUERY_WILDCARD,"",objSelect);
            if (lstFLList!=null)
            {
                Vector vtr = null;
                for (int i = 0;i<lstFLList.size() ;i++ )
                {
                    Map flMap = (Map)lstFLList.get(i);                    
                    String ftrObjId= (String)flMap.get(DomainConstants.SELECT_ID);
                    String ftrObjName= (String)flMap.get(DomainConstants.SELECT_NAME);
                    DomainObject domFeature = new DomainObject(ftrObjId);
                    StringList strListGBOM = domFeature.getInfoList(context, "from["+ProductLineConstants.RELATIONSHIP_GBOM_FROM+"].id");
                    if (strListGBOM.size()>0)
                    {
                        int sizeofGBOM = 0;
                        String gBomId = null;
                        Map attributeMap = new HashMap();
                        DomainRelationship domGBOmFrom = null;
                        vtr = new Vector();
                        int count =0;                        
                        if(strListGBOM != null && (sizeofGBOM = strListGBOM.size())>0)
                        {
                            for(int k=0;k<sizeofGBOM;k++)
                            {
                                gBomId = strListGBOM.get(k).toString();
                                domGBOmFrom = new DomainRelationship(gBomId);
                                attributeMap = domGBOmFrom.getAttributeMap(context,false);
                                vtr.add(domGBOmFrom.getAttributeMap(context,false));
                            }
                            if (strListGBOM.size()>1)
                            {
                                Iterator itr = vtr.iterator();
                                while(itr.hasNext()){
                                    if ((vtr.get(0)).equals(itr.next()))
                                    {
                                        count ++;
                                    }
                                }
                            }
                         }
                        
                        if (count==strListGBOM.size()||strListGBOM.size()==1)
                        {
                        
                            StringList strListFeatureList = domFeature.getInfoList(context, "to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.id");
                            StringList strListFeatureListName = domFeature.getInfoList(context, "to["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].from.name");
                            
                            if (strListFeatureList.size()>0)
                            {
                               
                                String orgId = null;
                                String orgName = null;
                                DomainObject domFeatureList=null;
                                int size = 0;
                                if((size= strListFeatureList.size()) > 0)
                                {
                                    for(int j=0;j<size;j++)
                                    {
                                        orgId = strListFeatureList.get(j).toString();
                                        orgName =strListFeatureListName.get(j).toString();
                                        domFeatureList = new DomainObject(orgId);
                                        domFeatureList.setAttributeValues(context,attributeMap);
                                        migrationInfoWriter(bWriteOnMqlConsole,"========= Migrating GBOM attributes of Feature "+ftrObjName+" to Feature List====="+orgName+"======\n");                                    
                                    }
                                }
                            }
                            else
                            {
                                migrationInfoWriter(bWriteOnMqlConsole,"=====Feature==== "+ftrObjName+"=== Has no Feature List objects associated, Hence cannot Migrate===========\n");                            
                            }
                         }
                         else
                         {
                            migrationInfoWriter(bWriteOnMqlConsole,"=====Feature==== "+ftrObjName+"=== Has differing GBOM Attributes, Hence cannot Migrate===========\n");                           
                         }
                          
                    }
                    else
                    {
                        migrationInfoWriter(bWriteOnMqlConsole,"========= No GBOM is connected to "+ftrObjName+", Hence cannot  migrated==============\n");
                    }
                }
              migrationInfoWriter(bWriteOnMqlConsole,"\n"+getString(context,SUCCESS_GBOM_FROM)+"\n\n");

            }
        }
            catch (Exception e) {
                migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_GBOM_FROM)+" \n"+getStackTrace(e));
            
        }
} 
      

    /**
     * This method  is used to connect all the Features  and SubFeatures to the Product using the 
	   Product Feature Relationship
     * @param context - The eMatrix <code>Context</code> object
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     * @grade 0
     */
    
  public static void migrationProductFeatureList(Context context)
          throws Exception {
      try 
      {
            migrationInfoWriter(bWriteOnMqlConsole,"\n\n=============="+getString(context,MIGRATING_PRODUCT_FEATURE_LIST)+" ====================\n\n");
          StringList objSelect = new StringList(2);
          objSelect.addElement(DomainConstants.SELECT_ID);
          objSelect.addElement(DomainConstants.SELECT_NAME);
          objSelect.addElement(DomainConstants.SELECT_TYPE);
          
          StringBuffer strWhereCondition = new StringBuffer();
          strWhereCondition.append(DomainConstants.EMPTY_STRING);
          strWhereCondition.append("(type!=\"");
          strWhereCondition.append(PropertyUtil.getSchemaProperty(context,"type_ProductVariant"));
          strWhereCondition.append("\")");
          
          // Get all the Product objects in the database
          MapList lstProductList = DomainObject.findObjects(context,ProductLineConstants.TYPE_PRODUCTS,DomainConstants.QUERY_WILDCARD,strWhereCondition.toString(),objSelect);
          
          if (lstProductList!=null)
          {
              //Iterate each Product for Migration
              for (int i = 0;i<lstProductList.size() ;i++ )
              {
                  Map productMap = (Map)lstProductList.get(i);                    
                  String productObjId= (String)productMap.get(DomainConstants.SELECT_ID);
                  String productObjName= (String)productMap.get(DomainConstants.SELECT_NAME);
                  
                  DomainObject domProduct = new DomainObject(productObjId);
                  
                  String strRelationshipFrom = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM;
                  String strRelationshipTo = ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO;
                  String strRelPattern = strRelationshipFrom + "," + strRelationshipTo;
                  StringList relSelects = new StringList();
                  //Get all the Features and the Feature List objects that are connected to the Product
                  MapList listFeatures = domProduct.getRelatedObjects(context,strRelPattern,"*",false,true,(int)0,objSelect,relSelects,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING, null);
                  if (listFeatures.size()>0)
                  {
                      for (int k = 0;k<listFeatures.size();k++ )
                      {
                          Map featureMap = (Map)listFeatures.get(k); 
                          String relationship= (String)featureMap.get("relationship");
                          String objType= (String)featureMap.get("type");
                          // Condition to Connect only the Feature List object to the Product
                          if(relationship.equals(ProductLineConstants.RELATIONSHIP_FEATURE_LIST_FROM)&& objType.equals(ProductLineConstants.TYPE_FEATURE_LIST))
                          {
                              String fLObjId = (String)featureMap.get("id");
                              DomainObject dom = new DomainObject(fLObjId);
                              StringList strProductIds = dom.getInfoList(context, "to["+ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
                              if(!strProductIds.contains(productObjId)){
                                  DomainRelationship.connect(context,productObjId,ConfigurationConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST,fLObjId,false);
                                  migrationInfoWriter(bWriteOnMqlConsole,"========= Connecting Product==="+productObjName+"===to Feature List==="+featureMap.get("name")+"===\n");
                              }
                          }
                      }
                  }
                  else
                  {
                      migrationInfoWriter(bWriteOnMqlConsole,"========= Product === "+productObjName+"=== is not Connected to Any Features. Hence,cannot  migrated ==============\n");
                  }
              }
              migrationInfoWriter(bWriteOnMqlConsole,"\n"+getString(context,SUCCESS_PRODUCT_FEATURE_LIST)+"\n\n");
          }

      }
          catch (Exception e) {
              migrationInfoWriter(bWriteOnMqlConsole,getString(context,ERROR_PRODUCT_FEATURE_LIST)+" \n"+getStackTrace(e));
          
      }
}

public static void migrateBOMForTopLevel(Context context, String arts[]) throws Exception
      {
          strFileURI = EnoviaResourceBundle.getProperty(context,MIGRATION_INFO_FILE_URI);
          foLogFileInfo = new FileOutputStream(strFileURI,false);
          psWriter = new PrintStream(foLogFileInfo);
          writer = new BufferedWriter(new MatrixWriter(context));
          
          StringList selectStmts = new StringList();
          StringList relSelectsList = new StringList();
          List pcList = new MapList();
          
          
          selectStmts.addElement(ProductLineConstants.SELECT_ID);
          relSelectsList.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
          
          String ACTION = "ACTION";
          String TOP_LEVEL_PART = "TOP LEVEL PART";
          String EXECUTION_RESULT = "EXECUTION RESULT";
          String initialPart = "Product Central BOM Migration Process Executed on "+dtCurrentDate+" by user "+context.getUser()+":";
          
          migrationInfoWriter(bWriteOnMqlConsole,"================"+initialPart+"=================\n");
          
          
          
          pcList = DomainObject.findObjects(context,"Product Configuration",null,null,selectStmts);
          
          
          Iterator itrPC = pcList.iterator();
          HashMap hsPC = new HashMap();
          while(itrPC.hasNext())
          {
              try{
//                String strPFType = "Part Family";//
                  String strPFType = getString(context,TOP_LEVEL_PF_TYPE);
                  //String strPFName = "PF1";//
                  String strPFName = getString(context,TOP_LEVEL_PF_NAME);
                  
                  //String strPFRevision = "-";//
                  String strPFRevision = getString(context,TOP_LEVEL_PF_REVISION);    
              hsPC = (HashMap)itrPC.next();
              String strProductConfiguration = (String)hsPC.get(ProductLineConstants.SELECT_ID);
              //create domain object of product configuration
              DomainObject objProductConfiguration = new DomainObject(strProductConfiguration);
              String strPCdetail = "";
              boolean generatedFromPF = false;
              migrationInfoWriter(bWriteOnMqlConsole,"-------------------------------------------------------------------\n");
              strPCdetail = objProductConfiguration.getInfo(context,"type")+" "+objProductConfiguration.getInfo(context,"name")+" "+objProductConfiguration.getInfo(context,"revision");
              migrationInfoWriter(bWriteOnMqlConsole,strPCdetail+"\n");
              String topLevelPart = objProductConfiguration.getInfo(context,"from["+ProductLineConstants.RELATIONSHIP_TOP_LEVEL_PART+"].to.id");
              if(topLevelPart==null || topLevelPart.length()<=0)
              {
                  //check part family is there with product
                  String objProductId = objProductConfiguration.getInfo(context,"to[" + ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION +
                                                                                  "].from." + ProductLineConstants.SELECT_ID);
                  DomainObject domProductObject = new DomainObject(objProductId);
                  
                  List gbomList = new MapList();
                  StringList selectable = new StringList();
                  selectable.addElement("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.type");
                  selectable.addElement("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.name");
                  selectable.addElement("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.revision");
                  StringBuffer stbWhereClause = new StringBuffer();
                  stbWhereClause.append("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.type=='"+ProductLineConstants.TYPE_PART_FAMILY+"'");
                  
                  gbomList = domProductObject.getRelatedObjects(context,
                                                                  ProductLineConstants.RELATIONSHIP_GBOM_FROM,
                                                                  ProductLineConstants.TYPE_GBOM,
                                                                  false,
                                                                  true,
                                                                  1,
                                                                  selectable,
                                                                  null,
                                                                  stbWhereClause.toString(),
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  null);
                  
                  
                 //if(gbomList!=null){
                      
                      if(gbomList.size()>1){
                          
                          String strMoreThanOnePF = "More than one Part Famili are attached with Product";
                          throw  new Exception(strMoreThanOnePF);
                          
                      }
                  if(gbomList.size()==1) {
                      
                          strPFType = (String) ((Map)gbomList.get(0)).get("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.type");
                         
                          strPFName = (String) ((Map)gbomList.get(0)).get("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.name");
                          strPFRevision = (String) ((Map)gbomList.get(0)).get("from["+ProductLineConstants.RELATIONSHIP_GBOM_TO+"].to.revision");
                          generatedFromPF = true;
                          
                  }
                          
                      
                  //}
                  
                  //generate part from part family
                  if((strPFType!=null && strPFType.length()>0) && 
                     (strPFName!=null && strPFName.length()>0) && 
                     (strPFRevision!=null && strPFRevision.length()>0))
                  {
                      DomainObject objPart = new DomainObject();
                      
                          
                          objPart = generatePart(context,strPFType,strPFName,strPFRevision,strProductConfiguration);
                      
                      //connect pc and part with TLP relationship
                      objProductConfiguration.connectTo(context,ProductLineConstants.RELATIONSHIP_TOP_LEVEL_PART,objPart);
                      //get all parts which are connected to product configuration with EBOM relationship
                      MapList mapListEBOMRelatedObject = objProductConfiguration.getRelatedObjects(context,
                                                                                              DomainConstants.RELATIONSHIP_EBOM,
                                                                                              DomainConstants.TYPE_PART,
                                                                                              selectStmts,
                                                                                              relSelectsList,
                                                                                              false,
                                                                                              true,
                                                                                              (short)1,
                                                                                              null,
                                                                                              null, 0);
                      Iterator itrEBOM = mapListEBOMRelatedObject.iterator();
                      Hashtable hsEBOM = new Hashtable();
                      while(itrEBOM.hasNext())
                      {
                          hsEBOM = (Hashtable)itrEBOM.next();
                          String strEBOMPartId = (String)hsEBOM.get(ProductLineConstants.SELECT_ID);
                          String strEBOMRelId = (String)hsEBOM.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                          DomainRelationship domEBOMRelId = new DomainRelationship(strEBOMRelId);
                          Map mapEBOMAttributes=domEBOMRelId.getAttributeMap(context);
                          DomainObject objEBOMPartId = new DomainObject(strEBOMPartId);
                          //connect new generated part and ebom parts wiht EBOM relationship
                          DomainRelationship objrelEBOM  = objPart.connectTo(context,ProductLineConstants.RELATIONSHIP_EBOM,objEBOMPartId);
                          //replicate attribute values
                          objrelEBOM.setAttributeValues(context,mapEBOMAttributes);
                          //disconnect pc and parts ebom relationship
                          RelationshipType relEBOM = new matrix.db.RelationshipType(ProductLineConstants.RELATIONSHIP_EBOM);
                          objProductConfiguration.disconnect(context,relEBOM,true,objEBOMPartId);
                      }
                      
                      if(generatedFromPF)
                      {
                          migrationInfoWriter(bWriteOnMqlConsole,ACTION+" : "+"Part Family Found "+"\n");
                      }
                      else 
                      {
                          migrationInfoWriter(bWriteOnMqlConsole,ACTION+" : "+"Default Part Family used "+"\n");
                      }
                      migrationInfoWriter(bWriteOnMqlConsole,TOP_LEVEL_PART+" : "+objPart.getInfo(context,"type")+" "+objPart.getInfo(context,"name")+" "+objPart.getInfo(context,"revision")+"\n");
                      migrationInfoWriter(bWriteOnMqlConsole,EXECUTION_RESULT+" : "+"Success."+"\n");
                  
                  }
                  else
                  {
                      migrationInfoWriter(bWriteOnMqlConsole,ACTION+" : "+"Details for Default Part Family are not Specified"+"\n");
                      migrationInfoWriter(bWriteOnMqlConsole,EXECUTION_RESULT+" : "+"Failed : Details for Default Part Family are not Specified"+"\n");
                  }
              }
              else
              {
                  DomainObject objTLP = new DomainObject(topLevelPart);
                  migrationInfoWriter(bWriteOnMqlConsole,ACTION+" : "+"Top level Part Found "+"\n");
                  migrationInfoWriter(bWriteOnMqlConsole,TOP_LEVEL_PART+" : "+objTLP.getInfo(context,"type")+" "+objTLP.getInfo(context,"name")+" "+objTLP.getInfo(context,"revision")+"\n");
                  migrationInfoWriter(bWriteOnMqlConsole,EXECUTION_RESULT+" : "+"Failed : Top level Part Found"+"\n");
                  
              }
          }catch(Exception e){
              migrationInfoWriter(bWriteOnMqlConsole,EXECUTION_RESULT+" : "+"Failed : "+getStackTrace(e)+"\n");
              continue;
          } 
          }
      }
      private static DomainObject generatePart(Context context, String type,String name,String revision,String strProductConfiguration)throws Exception{
          List pfList = new MapList();
          StringList selectStmts = new StringList();
          selectStmts.addElement(ProductLineConstants.SELECT_ID);
          pfList = DomainObject.findObjects(context,type,name,revision,null,null,null,false,selectStmts);
          Iterator itrPF = pfList.iterator();
          HashMap hsPF = new HashMap();
          String PartFamilyId= "";
          DomainObject domPart=new DomainObject();
          while(itrPF.hasNext())
          {
              hsPF = (HashMap)itrPF.next();
              PartFamilyId = (String)hsPF.get(ProductLineConstants.SELECT_ID);
              
          }
          String attrDefaultPartType = "attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_DefaultPartType") + "]";
          String attrDefaultPartPolicy = "attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_DefaultPartPolicy") + "]";
          
          com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
         
          String strVaultName = person.getVaultName(context);
          DomainObject PartFamilyObject = new DomainObject(PartFamilyId);
          StringList strList = new StringList();
          strList.add(attrDefaultPartType);
          strList.add(attrDefaultPartPolicy);
          Map map = PartFamilyObject.getInfo(context, strList);
          
          String sDefaultPolicy = (String)map.get(attrDefaultPartPolicy);
          
          String sAbsPolicy = PropertyUtil.getSchemaProperty(context, sDefaultPolicy);
          String sDefaultType = (String)map.get(attrDefaultPartType);
          String sAbsType = PropertyUtil.getSchemaProperty(context, sDefaultType);
          if(sAbsType != null && !"".equals(sAbsType))
          {
              sDefaultType = sAbsType;
          }
          if(sAbsPolicy != null && !"".equals(sAbsPolicy))
          {
              sDefaultPolicy = sAbsPolicy;
          }
          if ( sDefaultPolicy == null || "".equals(sDefaultPolicy))
          {
              sDefaultPolicy = DomainObject.POLICY_EC_PART;
          }
          revision = "";
          
          if ( revision == null || revision.equals("") || revision.equals("None"))
          {
              Policy policyObj = new Policy (sDefaultPolicy);
              policyObj.open(context);
              if (policyObj.hasSequence())
              {
                  revision = policyObj.getFirstInSequence();
              }
              else
              {
                  revision = "";
              }
         }
         // Use Part Family for naming the Part
         PartFamily PartFamily = new PartFamily(PartFamilyId);
         PartFamily.open(context);
         // check the "Part Family Name Generator On" attribute
                  
         //creating a part object with default values
         
         domPart.createObject(context, sDefaultType, new DomainObject(strProductConfiguration).getInfo(context,"name"), revision, sDefaultPolicy, strVaultName);
         //connecting it with Part Family
         domPart.connect(context, new RelationshipType(ProductLineConstants.RELATIONSHIP_CLASSIFIED_ITEM), false, PartFamily);
         
         return domPart;
      }
      
      public void help(Context context, String[] args) throws Exception {
          if (!context.isConnected()) {
              throw new Exception("not supported on desktop client");          }

          writer.write("================================================================================================\n");
          writer.write(" INFO about Migartion  \n");          
          writer.write(" execute program emxFeatureConfigurationMigration <Param1> <Param2> \n");
          writer.write(" Param1 can be either verboseon or verboseoff \n");
          writer.write(" Param2 can be PRE_10.6 or POST_10.6  \n");
          writer.write("================================================================================================\n\n");
         
          writer.write(" How to Run this Migartion Script??? \n"); 
          writer.write(" If Current Version is PRE 10.6 then the following command should be executed in MQL \n");
          writer.write(" execute program emxFeatureConfigurationMigration verboseon PRE_10.6; \n\n"); 
                   
          writer.write(" If Current Version is POST 10.6 then the following command should be executed in MQL \n");
          writer.write(" execute program emxFeatureConfigurationMigration verboseon POST_10.6; \n\n"); 
         
          writer.write("================================================================================================\n");
          writer.write(" \n");
          writer.write(" \n");

          writer.close();
      }
  
}//End of class
