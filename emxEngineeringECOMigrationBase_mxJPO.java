/*
 * emxEngineeringECOMigrationBase.java
 * program migrates Existing EC Substitute Part to New Substitute Data Model(i.e. Create EBOM Substitute rel).
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */


import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

public class emxEngineeringECOMigrationBase_mxJPO  extends emxCommonMigration_mxJPO
{
	static String RELATIONSHIP_NEW_PART_PART_REVISION     = PropertyUtil.getSchemaProperty("relationship_NewPartPartRevision");
	static String RELATIONSHIP_NEW_SPECIFICATION_REVISION     = PropertyUtil.getSchemaProperty("relationship_NewSpecificationRevision");
	static String RELATIONSHIP_MAKE_OBSOLETE     = PropertyUtil.getSchemaProperty("relationship_MakeObsolete");
	static String RELATIONSHIP_AFFECTED_ITEM =    PropertyUtil.getSchemaProperty("relationship_AffectedItem");
	static String POLICY_ECO = PropertyUtil.getSchemaProperty("policy_ECO");
	static String POLICY_ECOSTANDARD = PropertyUtil.getSchemaProperty("policy_ECOStandard");

	static String strFieldReturn   = PropertyUtil.getSchemaProperty("attribute_DispositionFieldReturn");
	static String strOnOrder       = PropertyUtil.getSchemaProperty("attribute_DispositionOnOrder");
	static String strInProcess     = PropertyUtil.getSchemaProperty("attribute_DispositionInProcess");
	static String strInStock       = PropertyUtil.getSchemaProperty("attribute_DispositionInStock");
	static String strInField       = PropertyUtil.getSchemaProperty("attribute_DispositionInField");
	static String strSpecificDescriptionofChange       = PropertyUtil.getSchemaProperty("attribute_SpecificDescriptionofChange");
	static String strWhereUsedComponentReference       = PropertyUtil.getSchemaProperty("attribute_WhereUsedComponentReference");

	/**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxEngineeringECOMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }


      /**
       * This method does the migration work.  New Part Master objects
       * are created for those that do not have any.  All revisions of a Part are
       * connected to a single Part Master with the Part Revision relationship.
       * Only parts with Production or Development policies are migrated.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList)
                                                          throws Exception
      {

          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }
          
         try{
              StringList objectSelects = new StringList();

              objectSelects.add(DomainConstants.SELECT_TYPE);
              objectSelects.add(DomainConstants.SELECT_NAME);
              objectSelects.add(DomainConstants.SELECT_REVISION);
              objectSelects.add(DomainConstants.SELECT_ID);              
              
              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
              Iterator itr = mapList.iterator();
              String strECOId="";
              String strPartId ="";
              String strRelId ="";
              String strRelName ="";
              Map map = new HashMap();
              Map mNewPartPartRevision = new HashMap();
              Map mAttribMap = new HashMap();
              StringList strECOIdList = new StringList();
              StringList selectlist = new StringList();
              selectlist.add(DomainConstants.SELECT_ID);
              StringList relselect = new StringList();
              relselect.add(DomainRelationship.SELECT_ID);
              relselect.add(DomainRelationship.SELECT_NAME);
              relselect.add("attribute[" + strFieldReturn + "]");
              relselect.add("attribute[" + strOnOrder + "]");
              relselect.add("attribute[" + strInProcess + "]");
              relselect.add("attribute[" + strInStock + "]");
              relselect.add("attribute[" + strInField + "]");
              relselect.add("attribute[" + strSpecificDescriptionofChange + "]");
              relselect.add("attribute[" + strWhereUsedComponentReference + "]");
              String strECOName ="";
              String strrelFieldReturn ="";
              String strrelOnOrder ="";
              String strrelInProcess ="";
              String strrelInStock ="";
              String strrelInField ="";
              Pattern relPattern        = new Pattern("");
              relPattern.addPattern(RELATIONSHIP_NEW_PART_PART_REVISION);
              relPattern.addPattern(RELATIONSHIP_NEW_SPECIFICATION_REVISION);
              relPattern.addPattern(RELATIONSHIP_MAKE_OBSOLETE);

              while (itr.hasNext())
              {
                    map = (Map) itr.next();

                    strECOId = (String)map.get(DomainConstants.SELECT_ID);
                    strECOName = (String)map.get(DomainConstants.SELECT_NAME);
                    strECOIdList.add(strECOId);
                    DomainObject domECO = new DomainObject(strECOId);
                  
                    MapList mlNewPartPartRevision = domECO.getRelatedObjects(context,
                                                                                relPattern.getPattern(),
                                                                                "*",
                                                                                selectlist,
                                                                                relselect,
                                                                                false,
                                                                                true,
                                                                                (short)1,
                                                                                "",
                                                                                null);



                    Iterator mlNewPartPartRevisionItr = mlNewPartPartRevision.iterator();
                    while (mlNewPartPartRevisionItr.hasNext())
                    {
                           mNewPartPartRevision = (Map) mlNewPartPartRevisionItr.next();                            
                           strPartId = (String) mNewPartPartRevision.get(DomainConstants.SELECT_ID);
                           DomainObject domPart = new DomainObject(strPartId);                           
                           strRelId = (String) mNewPartPartRevision.get(DomainRelationship.SELECT_ID);
                           strRelName = (String) mNewPartPartRevision.get(DomainRelationship.SELECT_NAME);
                           strrelFieldReturn = (String) mNewPartPartRevision.get("attribute[" + strFieldReturn + "]");
                           strrelOnOrder = (String) mNewPartPartRevision.get("attribute[" + strOnOrder + "]");
                           strrelInProcess = (String) mNewPartPartRevision.get("attribute[" + strInProcess + "]");
                           strrelInStock = (String) mNewPartPartRevision.get("attribute[" + strInStock + "]");
                           strrelInField = (String) mNewPartPartRevision.get("attribute[" + strInField + "]");
                           
                           mAttribMap.put(DomainConstants.ATTRIBUTE_DISPOSITION_FIELD_RETURN,strrelFieldReturn);
                           mAttribMap.put(DomainConstants.ATTRIBUTE_DISPOSITION_ON_ORDER,strrelOnOrder);
                           mAttribMap.put(DomainConstants.ATTRIBUTE_DISPOSITION_IN_PROCESS,strrelInProcess);
                           mAttribMap.put(DomainConstants.ATTRIBUTE_DISPOSITION_IN_STOCK,strrelInStock);
                           mAttribMap.put(DomainConstants.ATTRIBUTE_DISPOSITION_IN_FIELD,strrelInField);
                           if (RELATIONSHIP_NEW_PART_PART_REVISION.equals(strRelName)|| RELATIONSHIP_NEW_SPECIFICATION_REVISION.equals(strRelName) ){
                           mAttribMap.put(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE,"For Release");                          
                           }
                           else if (RELATIONSHIP_MAKE_OBSOLETE.equals(strRelName)){
                               mAttribMap.put(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE,"For Obsolescence");    
                           }
                                             
                           DomainRelationship doRelNew = DomainRelationship.connect(context, domECO, RELATIONSHIP_AFFECTED_ITEM, domPart);
                           doRelNew.setAttributeValues(context, mAttribMap);
                           DomainRelationship.disconnect(context,strRelId);
                                        
                    } 
                    domECO.setPolicy(context,POLICY_ECO);
                    
                    mqlLogWriter ("ECO- "+strECOName + "migrated to new policy with all its relationship");  
                     loadMigratedOidsList(strECOIdList);
                    
                   }         
         
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

    private void loadMigratedOidsList (StringList objectIdList) throws Exception
    {
        Iterator itr = objectIdList.iterator();
        String objectId=null;
        while (itr.hasNext())
        {
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
        writer.write(" Step1: Find ECO objects with policy ECO (Standard) and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringECOMigrationFindObjects 1000 ECO C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringECOMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
