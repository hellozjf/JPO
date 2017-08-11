/*
 ** emxProductCentralMigrationBase
 **
 ** Copyright (c) 2007-2016 Dassault Systemes. All Rights Reserved.
 **
 ** This program contains proprietary and trade secret information of Dassault Systemes. 
 ** Copyright notice is precautionary only and does not evidence 
 ** any actual or intended publication of such program.
 **
 */

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;


import java.io.BufferedWriter;
import  java.io.FileOutputStream;
import java.io.PrintStream;
import  java.util.Iterator;
import  java.util.Map;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Vector;
import  java.util.List;
import  java.util.ArrayList;
import  matrix.db.*;
import  matrix.util.StringList;

import  com.matrixone.apps.domain.DomainObject;
import  com.matrixone.apps.domain.DomainRelationship;
import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import  com.matrixone.apps.domain.util.FrameworkException;
import  com.matrixone.apps.domain.util.MapList;
import  com.matrixone.apps.domain.util.MqlUtil;
import  com.matrixone.apps.domain.util.PropertyUtil;
import  com.matrixone.apps.productline.ProductLineCommon;
import  com.matrixone.apps.productline.ProductLineConstants;
import  com.matrixone.apps.productline.ProductLineUtil;
import  com.matrixone.apps.domain.util.FrameworkUtil;
import  java.io.*;
import  java.util.Date;

public class emxProductCentralMigrationBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
         * Create a new ProductCentralMigration object from a given id
         *
         * @param context context for this request
         * @param args holds no arguments
         * @throws Exception when unable to find object id in the AEF
         * @since AEF10.6
         */
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

        private static final String BUNDLE_STR = "emxProductLineStringResource";
        private static boolean bWriteOnMqlConsole = true;
        private static String strFileURI="";
        private static final String MIGRATION_INFO_FILE_URI = "emxProduct.Migration.Log.MigrationInformation";
        private static FileOutputStream foLogFileInfo;
        private static PrintStream psWriter;
        public static MQLCommand mqlCommand = null;
        private static Date dtCurrentDate = new Date();
        private static BufferedWriter writer = null;
        private static final String ALREADY_DONE = "emxProduct.Migration.Log.AlreadyDone";
        private static final String SUCCESS_MIGRATION = "emxProduct.Migration.Log.SuccessMigration";
        private static final String FAILURE_MIGRATION = "emxProduct.Migration.Log.FailureMigration";

        public emxProductCentralMigrationBase_mxJPO (Context context, String[] args)
            throws Exception {
            super(context, args);
        }
        public void migrate(Context context,String[] args)
        throws Exception 
        {
            MQLCommand mqlCommand = new MQLCommand();
            //set the trigger off
            mqlCommand.executeCommand(context,"trigger off");
	    try {
            if (args!=null && args.length>0)
            {
                String strVerbose = args[0];
                if (strVerbose!=null && ! "".equals(strVerbose) && "verboseoff".equalsIgnoreCase(strVerbose))
                {
                    bWriteOnMqlConsole = false;
                }
                else {
                    bWriteOnMqlConsole = true;
                }
            }

            try
            {
                strFileURI = EnoviaResourceBundle.getProperty(context,MIGRATION_INFO_FILE_URI);
            }
            catch(Exception e)
            {
                strFileURI  = "c:/migrationLog.txt";
            }

            foLogFileInfo = new FileOutputStream(strFileURI,false);
            psWriter = new PrintStream(foLogFileInfo);
            //BufferedWriter writer to write on MQL Console
            writer = new BufferedWriter(new MatrixWriter(context));

            String command = "print program eServiceSystemInformation.tcl select property[PLCX+4Migration] dump |";
            String result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
            String strReturnVal = null;

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
                migrationInfoWriter(bWriteOnMqlConsole,getString(context,ALREADY_DONE)+"\n");
            }
            else  if (migrateProductsToConnectBuilds(context,args)==0 && migrateBuildsAndModels(context,args)==0)
             {
                  String cmd = "modify program eServiceSystemInformation.tcl add property PLCX+4Migration value Migrated";
                  MqlUtil.mqlCommand(context, mqlCommand,  cmd);
                  migrationInfoWriter(bWriteOnMqlConsole,getString(context,SUCCESS_MIGRATION)+"\n");
             }
             else
             {
               migrationInfoWriter(bWriteOnMqlConsole,getString(context,FAILURE_MIGRATION)+"\n");
             }
            //Closes this fileoutput stream and releases any system resources associated with this stream
            foLogFileInfo.close();
            foLogFileInfo = null;
	    } finally {
		//          set the trigger off
		mqlCommand.executeCommand(context,"trigger on");
	    }
}
        private int migrateProductsToConnectBuilds(Context context, String [] args) throws Exception
        {                       
            StringList selectStmts = new StringList(4);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement(DomainConstants.SELECT_REVISION);
            MapList objProductList = DomainObject.findObjects(context, ProductLineConstants.TYPE_PRODUCTS, "*","",selectStmts);
            DomainObject objProduct=null;
            int iProductCount = objProductList.size();
            try
            {
                ContextUtil.startTransaction(context,true);
                for(int i=0; i<iProductCount; i++)
                {
                    
                    Map objProductMap = (Map)objProductList.get(i);
                    String strProductId =(String)objProductMap.get(ProductLineConstants.SELECT_ID);
                    objProduct = DomainObject.newInstance(context, strProductId);
                    MapList objBuildAndPCList = objProduct.getRelatedObjects(context,
                                                                    ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION + "," + ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD,
                                                                    ProductLineConstants.TYPE_PRODUCT_CONFIGURATION + "," + ProductLineConstants.TYPE_BUILDS,
                                                                    true,
                                                                    true,
                                                                    2,
                                                                    selectStmts,
                                                                    null, "",
                                                                    "",
                                                                    "",
                                                                    "",
                                                                    null);
                    if(!objBuildAndPCList.isEmpty())
                    {                        
                        int iObjectCount = objBuildAndPCList.size();
                        StringList buildIdList = new StringList();
                        for(int j=0; j<iObjectCount; j++)
                        {                            
                            Map objMap = (Map)objBuildAndPCList.get(j);
                            String strObjId = (String)objMap.get(DomainConstants.SELECT_ID);
                            String strObjType = (String)objMap.get(DomainConstants.SELECT_TYPE);
                            String strObjName = (String)objMap.get(DomainConstants.SELECT_NAME);
                            String strObjRev = (String)objMap.get(DomainConstants.SELECT_REVISION);
                            DomainObject domObj = DomainObject.newInstance(context, strObjId);
                            if(domObj.isKindOf(context,ProductLineConstants.TYPE_BUILDS))
                            {
                                
                                String strConnectedProductId = domObj.getInfo(context, "to[" + ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD + "].from.id");
                                if(strConnectedProductId == null || strConnectedProductId.equals("null ")|| strConnectedProductId.equals(""))
                                {                                    
                                    buildIdList.add(strObjId);
                                }
                                else if(strConnectedProductId.equals(strProductId))
                                {
                                    
                                }
                                else
                                {                                    
                                    migrationInfoWriter(bWriteOnMqlConsole,strObjType + " " + strObjName + " " + strObjRev + getString(context,"emxProductLine.Migration.Error"));
                                }
                            }
                        }
                        if(!buildIdList.isEmpty())
                        {                            
                            String[] arrBuildIds = new String[buildIdList.size()];
                            for(int k= 0; k<buildIdList.size();k++)
                            {                                
                                arrBuildIds[k]= (String)buildIdList.get(k);
                            }                            
                            DomainRelationship.connect(context,objProduct,ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD, true, arrBuildIds );
                        }
                    }
                }
                ContextUtil.commitTransaction(context);
            }
            catch(Exception exp)
            {
                ContextUtil.abortTransaction(context);
                return 1;
            }
            return 0;
        }
        private static String getString(Context context,String strKey)throws FrameworkException
        {
           String strLocale = context.getSession().getLanguage();
           return EnoviaResourceBundle.getProperty(context, "ProductLine",strKey,strLocale);
        }
        private static void migrationInfoWriter(boolean bVerbose,String strLogEnrty)
        throws Exception
        {
            if (strLogEnrty!=null && strLogEnrty.length()>0)
            {
                psWriter.println(strLogEnrty);
                if (bVerbose)
                    {
                     writer.write(strLogEnrty);
                     writer.flush();
                     }
            }
        }
        private int migrateBuildsAndModels(Context context, String[] args)throws Exception
        {
            StringList selectStmts = new StringList(5);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement(DomainConstants.SELECT_REVISION); 
            selectStmts.addElement(DomainConstants.SELECT_ORIGINATED);
            MapList objModelList = DomainObject.findObjects(context, ProductLineConstants.TYPE_MODEL, "*","",selectStmts);
            DomainObject objModel = null;
            int iModelCount = objModelList.size();
            try
            {
                ContextUtil.startTransaction(context,true);
                for(int i=0; i< iModelCount; i++)
                {                    
                    Map objModelMap = (Map)objModelList.get(i);
                    String strModelId =(String)objModelMap.get(ProductLineConstants.SELECT_ID);
                    objModel = DomainObject.newInstance(context, strModelId);
                    String strModelPrefix = objModel.getAttributeValue(context,ProductLineConstants.ATTRIBUTE_PREFIX);
                    if(strModelPrefix == null || strModelPrefix.equals("null"))
                    {
                        strModelPrefix = "";
                    }
                    MapList objProductList = objModel.getRelatedObjects(context,
                                                                    ProductLineConstants.RELATIONSHIP_PRODUCTS,
                                                                    ProductLineConstants.TYPE_PRODUCTS,
                                                                    true,
                                                                    true,
                                                                    1,
                                                                    selectStmts,
                                                                    null, "",
                                                                    "",
                                                                    "",
                                                                    "",
                                                                    null);
                    MapList objModelBuildMapList = new MapList();
                    if(!objProductList.isEmpty())
                    {
                        int iProductCount = objProductList.size();
                        for (int j=0;j<iProductCount; j++)
                        {
                            Map objProductMap = (Map)objProductList.get(j);
                            String strProductId = (String)objProductMap.get(DomainConstants.SELECT_ID);
                            DomainObject domObj = newInstance(context,strProductId) ;
                            MapList objProductBuildList = domObj.getRelatedObjects(context,
                                    ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD,
                                    ProductLineConstants.TYPE_BUILDS,
                                    true,
                                    true,
                                    1,
                                    selectStmts,
                                    null, "",
                                    "",
                                    "",
                                    "",
                                    null);
                            if(!objProductBuildList.isEmpty())
                            {
                                int iProductBuildCount = objProductBuildList.size();
                                for(int k=0; k<iProductBuildCount; k++)
                                {
                                   Map objBuildMap = (Map)objProductBuildList.get(k);
                                   String strBuildId = (String)objBuildMap.get(DomainConstants.SELECT_ID);
                                   String strBuildType = (String)objBuildMap.get(DomainConstants.SELECT_TYPE);
                                   String strBuildName = (String)objBuildMap.get(DomainConstants.SELECT_NAME);
                                   String strBuildRev = (String)objBuildMap.get(DomainConstants.SELECT_REVISION);      
                                   DomainObject buildObj = newInstance(context, strBuildId);
                                   StringList pcIdList =  domObj.getInfoList(context,"from["+ ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION + "].to.id");
                                   String connectedPCId = buildObj.getInfo(context, "to[" + ProductLineConstants.RELATIONSHIP_PRODUCT_CONFIGURATION_BUILD + "].from.id");
                                   if(connectedPCId != null && !connectedPCId.equals("null") &&!connectedPCId.equals(""))
                                   {
                                       if(pcIdList.contains(connectedPCId))
                                       {
                                           objModelBuildMapList.add(objBuildMap);
                                       }
                                       else
                                       {
                                           migrationInfoWriter(bWriteOnMqlConsole,strBuildType + " " + strBuildName + " " + strBuildRev + " " +getString(context,"emxProductLine.Migration.BuildAmbiguous"));
                                       }
                                   }
                                   if(connectedPCId == null || connectedPCId.equals("null") || connectedPCId.equals(""))
                                   {
                                       objModelBuildMapList.add(objBuildMap);
                                   }
                                }
                            }
                        }
                    }
                    objModelBuildMapList.addSortKey(DomainConstants.SELECT_ORIGINATED,"ascending","date");
                    objModelBuildMapList.sort();
                    int iModelBuildListSize = objModelBuildMapList.size();
                    for(int l=0; l<iModelBuildListSize ; l++)
                    {
                        Map objBuildMap = (Map)objModelBuildMapList.get(l);
                        String strBuildId = (String)objBuildMap.get(DomainConstants.SELECT_ID);
                        DomainObject objBuid = newInstance(context, strBuildId);
                        String strBuildUnitNumber = String.valueOf(l+1);
                        HashMap buildAttribMap = new HashMap();
                        buildAttribMap.put(ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER,strBuildUnitNumber);
                        String strBuildSHNotation = strModelPrefix + strBuildUnitNumber;
                        buildAttribMap.put(ProductLineConstants.ATTRIBUTE_PREFIX,strBuildSHNotation);
                        objBuid.setAttributeValues(context,buildAttribMap);                    
                    }
                    String strLastBuildUnitNumber = String.valueOf(iModelBuildListSize);
                    objModel.setAttributeValue(context,ProductLineConstants.ATTRIBUTE_LAST_BUILD_UNIT_NUMBER,strLastBuildUnitNumber);
                    
                }
                ContextUtil.commitTransaction(context);
            }
            catch(Exception exp)
            {
                ContextUtil.abortTransaction(context);
                return 1;
            }            
            return 0;
        }
}
