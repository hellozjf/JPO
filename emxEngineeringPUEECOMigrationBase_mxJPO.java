/*
 * emxEngineeringPUEECOMigrationBase.java
 * program migrates Existing PUEECO and its appicability attribute to NE object and set the expression
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.effectivitymigration.EffectivityFramework;

  public class emxEngineeringPUEECOMigrationBase_mxJPO  extends emxCommonMigration_mxJPO {
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxEngineeringPUEECOMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }


      /**
       * This method does the migration work.  Existing PUEECO and its 
       * applicability attribute will migrated to NE object and the expression
       * will be set for 2011x env.
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList) throws Exception {
    	  
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
    	  
          if (scan) {
              return;
          }
          
          int listSize = (objectIdList == null) ? 0 : objectIdList.size();
          
          if (listSize > 0) {
        	  ContextUtil.startTransaction(context, true);
	         try {
	        	  String strApplicability = PropertyUtil.getSchemaProperty(context, "attribute_Applicability");
        		  String strApplicableItem = PropertyUtil.getSchemaProperty(context, "relationship_ApplicableItem");
        		  String strRelEffectivityContext = PropertyUtil.getSchemaProperty(context, "relationship_EffectivityContext");
        		  String strTypeProducts = PropertyUtil.getSchemaProperty(context, "type_Products");

        		  String SELECT_ATTRIBUTE_APPLICABILITY = "attribute[" + strApplicability + "]";
        		  String SELECT_APPLICABILITY_ITEM_PHYSICAL_ID = "relationship[" + strApplicableItem + "].to.physicalid";
        	      String SELECT_APPLICABILITY_ITEM_PRODUCT_ID = "relationship[" + strApplicableItem + "].to.id";
        		  String SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION = EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION;
	        	 
	              EffectivityFramework effectivity  = new EffectivityFramework();
	              
	              StringList objectSelects = new StringList(6);
	              objectSelects.add(DomainConstants.SELECT_TYPE);
	              objectSelects.add(DomainConstants.SELECT_NAME);
	              objectSelects.add(DomainConstants.SELECT_ID);
	              objectSelects.add(DomainConstants.SELECT_REVISION);
	              objectSelects.add(SELECT_ATTRIBUTE_APPLICABILITY);
	              objectSelects.add(SELECT_APPLICABILITY_ITEM_PHYSICAL_ID);
                  objectSelects.add(SELECT_APPLICABILITY_ITEM_PRODUCT_ID);
                  //objectSelects.add(SELECT_ATTRIBUTE_MODSTACK);
	              
	              String[] oidsArray = (String[]) objectIdList.toArray(new String[listSize]);
	              
	              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);	              
	              MapList changeExprList = new MapList(listSize);
	              
	              String strPUEECOId;
	              String strApp;              
	              String strphysicalid;
	              
	              Map map;
	              Map mapTemp;
	              
	              StringList strECOIdList = new StringList();
	              
	              StringBuffer sb;
	              
	              Iterator itr = mapList.iterator();
	              while (itr.hasNext()) {
						map = (Map) itr.next();
						
						strApp = (String) map.get(SELECT_ATTRIBUTE_APPLICABILITY);
						
                        if (strApp.contains("!")|| strApp.contains(",^")) {
                            strApp= getApplicability(strApp);                            
                        }
                        
						strPUEECOId   = (String) map.get(DomainConstants.SELECT_ID);
						strphysicalid = (String) map.get(SELECT_APPLICABILITY_ITEM_PHYSICAL_ID);
						
						strECOIdList.add(strPUEECOId);
						
						sb = new StringBuffer(50);
						sb.append("@EF_UT(PHY@EF:").append(strphysicalid).append('[').append(strApp).append(']').append(')');
						
						mapTemp = new HashMap(2);
						mapTemp.put(DomainConstants.SELECT_ID, strPUEECOId);							
						mapTemp.put(SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION, sb.toString());
						changeExprList.add(mapTemp);
                        

	              }
	              
	              MqlUtil.mqlCommand(context, "trigger on;");
	              MqlUtil.mqlCommand(context, "mod rel $1 to add type $2",
									 strRelEffectivityContext, strTypeProducts);
					try {						
						effectivity.setEffectivityOnChange(context,changeExprList);						
					} catch (Exception e) {
						throw e;
					}
					finally {
						MqlUtil.mqlCommand(context, "mod rel $1 to remove type $2",
											strRelEffectivityContext, strTypeProducts);
						MqlUtil.mqlCommand(context, "trigger off;");
					}
                  
	              loadMigratedOidsList(strECOIdList);
	              ContextUtil.commitTransaction(context);	              
	          } catch (Exception ex) {
	        	  ContextUtil.abortTransaction(context);
	              ex.printStackTrace();
	              throw ex;
	          }
          }
      }

      private String getApplicability(String strApp){
          String App="";
          String splt[];
          String splt1;
          int num,i,k=0;
          String[] sAppValue2=strApp.split(",");
          
          for(i=0;i<sAppValue2.length;i++)
          {
              if("".equals(sAppValue2[k]))
              {
                  if("^".equals(sAppValue2[k+1]))
                  {
                   App="1-^";
                   break;
                  }
              }
              
           if(sAppValue2[i].contains("!"))
           {
               if(sAppValue2[i].contains("-"))
               {
                   splt=sAppValue2[i].split("-");
                   num=Integer.parseInt(splt[1]);
                   num=num+1;
                   splt1 = Integer.toString(num);
                   
               }
               else{
                   splt=sAppValue2[i].split("!");
                   num=Integer.parseInt(splt[0]);
                   num=num+1;
                   splt1 = Integer.toString(num);
               }
                   
                   if(sAppValue2[i+1].contains("-"))
                   {
                       splt=sAppValue2[i+1].split("-");
                       App=App+","+splt1+"-"+splt[1];
                   }
                   else{
                       App=App+","+splt1+"-"+sAppValue2[i+1];
                       }
                
           }
           else{
               if(i==sAppValue2.length-1)
                   break;
               if(i==0)
                   App=sAppValue2[i];
               else
                   App=App+","+sAppValue2[i];
          }
          }
          return App;
      }
      
    private void loadMigratedOidsList (StringList objectIdList) throws Exception {
        Iterator itr = objectIdList.iterator();
        String objectId;
        
        while (itr.hasNext()) {
            objectId = (String) itr.next();
            loadMigratedOids(objectId);
        }
    }

    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" ECO Migration is a two step process  \n");
        writer.write(" Step1: Find PUE ECO objects with policy PUE ECO and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringPUEECOMigrationFindObjects 1000 'PUE ECO' C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringPUEECOMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
