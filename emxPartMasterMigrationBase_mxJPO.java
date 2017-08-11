/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

  import matrix.db.*;
  import matrix.util.*;
  import java.util.*;
  import com.matrixone.apps.domain.*;
  import com.matrixone.apps.domain.util.*;

  public class emxPartMasterMigrationBase_mxJPO	extends emxCommonMigration_mxJPO
  {

      static String ATTRIBUTE_PART_MODE          = PropertyUtil.getSchemaProperty("attribute_PartMode");
      static String RELATIONSHIP_PART_REVISION   = PropertyUtil.getSchemaProperty("relationship_PartRevision");

      /** type "Part". */
      public static String TYPE_PART             = PropertyUtil.getSchemaProperty("type_Part");
      public static String TYPE_PART_MASTER      = PropertyUtil.getSchemaProperty("type_PartMaster");
      public static String POLICY_PART_MASTER    = PropertyUtil.getSchemaProperty("policy_PartMaster");

      // IR-013341

      public static String SELECT_NAME     = "name";
      public static String SELECT_REVISION = "revision";
      public static String SELECT_ID       = "id";
      public static String SELECT_CURRENT  = "current";
      public static String SELECT_TYPE     = "type";
      public static String SELECT_POLICY   = "policy";
      public static String SELECT_VAULT    = "vault";

      static String SELECT_RELATIONSHIP_PART_REVISION = "to[" + RELATIONSHIP_PART_REVISION + "]";


      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxPartMasterMigrationBase_mxJPO (Context context, String[] args)
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

          try
          {
              StringList objectSelects = new StringList(6);

              objectSelects.add(SELECT_TYPE);
              objectSelects.add(SELECT_NAME);
              objectSelects.add(SELECT_VAULT);
              objectSelects.add(SELECT_REVISION);
              objectSelects.add(SELECT_CURRENT);
              objectSelects.add(SELECT_ID);
              objectSelects.add(SELECT_RELATIONSHIP_PART_REVISION);
			  RelationshipType relType = new RelationshipType(RELATIONSHIP_PART_REVISION);

              StringList revisionSelects = new StringList(1);
              StringList revisionMultiValueSelects = new StringList(0);
              revisionSelects.add(SELECT_ID);

              String[] toObjectIds;
              StringList toObjectList = new StringList();

              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
              Iterator itr = mapList.iterator();
              Map map = new HashMap();
              Map revMap = new HashMap();
              String objectId = null;
              String revId = null;
			  String strPartType = null;
              String strPartName = null;
              String strPartVault = null;       
              String strPartMasterId = null;
              DomainObject partObject = null;
              DomainObject partMasterObject = null;
              BusinessObject boPartMaster = null;
              MapList revisionsMapList = null;
              String sModQuery = null;
              String sNameOrg = null;             
              StringList partRevDetailsList = null;
              while (itr.hasNext())
              {
                  map = (Map) itr.next();
                  objectId = (String) map.get(SELECT_ID);
                  partObject = DomainObject.newInstance(context, objectId);

                  strPartType = (String)map.get(SELECT_TYPE);
                  strPartName = (String)map.get(SELECT_NAME);
                  strPartVault = (String)map.get(SELECT_VAULT);
                  strPartMasterId = (String)map.get(SELECT_RELATIONSHIP_PART_REVISION);
              
                  // IR-013341
                  //Start : IR366583V6R2011. this is the same fix for IR-029792V6R2011, IR-036618V6R2011, IR-038820V6R2011
                    boPartMaster = new BusinessObject(TYPE_PART_MASTER, strPartName ,"", strPartVault);
                    //End : IR366583V6R2011
                    
                  //do not create Part Master object if there already exist one
                  if (strPartMasterId == null || "null".equalsIgnoreCase(strPartMasterId) || "".equals(strPartMasterId) || "FALSE".equalsIgnoreCase(strPartMasterId))
                  {
                      // create Part Master object
                      partObject.setId(objectId);
                      // Look for the Part Master object with the same name of the Part.                   
                     
                      if (boPartMaster.exists(context))
                      {
                          // Part Master for the part exists.
                          strPartMasterId = boPartMaster.getObjectId(context);
                          mqlLogWriter ( "Part Master Exists id/name = " + strPartMasterId + " " + strPartName + "\n");
                      }
                      else
                      {
                    	  boPartMaster = new BusinessObject(TYPE_PART_MASTER, strPartName ,strPartType, strPartVault);
                          // Part Master does not exist. Create one.
                    	  if(!boPartMaster.exists(context))
                    	  {                    		
                    		  boPartMaster.create(context, POLICY_PART_MASTER);                             
                    	  }
                    	  strPartMasterId = boPartMaster.getObjectId(context);
                          mqlLogWriter ( "Part Master Create id/name = " + strPartMasterId + " " + strPartName + "\n");
                          //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                          boPartMaster.setAttributeValue(context, PropertyUtil.getSchemaProperty(context,"attribute_PartMode"), "Resolved");                           
                      }
                      if(strPartName!=null)
    					{
    						sNameOrg = strPartName;
    						
    						sModQuery = "mod bus $1 name $2_migrate revision $3";    
    						MqlUtil.mqlCommand(context, sModQuery,strPartMasterId,strPartName,strPartType);
    						    						
    						sModQuery = "mod bus $1 name $2";    						
    						MqlUtil.mqlCommand(context, sModQuery,strPartMasterId,sNameOrg);    
    						
    					}

                      //get all revisions of the part and add the ids to the list of Parts to connect to this Part Master
                      revisionsMapList = partObject.getRevisionsInfo(context, revisionSelects, revisionMultiValueSelects);
     				  //start with clean list
                      toObjectList.clear();
                      if (revisionsMapList != null && revisionsMapList.size() > 0)
                      {
                          Iterator itr2 = revisionsMapList.iterator();
                          while (itr2.hasNext())
                          {
                              revMap = (Map) itr2.next();
                              revId = (String) revMap.get(SELECT_ID);
                              toObjectList.add(revId);
                          }
                      }

                      toObjectIds = new String[toObjectList.size()];
                      toObjectIds = (String[])toObjectList.toArray(toObjectIds);
                      partMasterObject = DomainObject.newInstance(context,boPartMaster);
                      partMasterObject.addRelatedObjects(context, relType, true, toObjectIds);
                      mqlLogWriter ( "Connect Part Master ids  = " + toObjectList + "\n");
                      loadMigratedOidsList (toObjectList);
                  }
                 else
                  {               	  		
            	  		if(boPartMaster.exists(context))
            	  		{            	  		
            	  			strPartMasterId = boPartMaster.getObjectId(context);
                	  		
	      					if(strPartName!=null)
	      					{
	      						sNameOrg = strPartName;
	      						
	      						sModQuery = "mod bus $1 name $2_migrate revision $3";  
	      						MqlUtil.mqlCommand(context, sModQuery,strPartMasterId,strPartName,strPartType);
	      						    						
	      						sModQuery = "mod bus $1 name $2";    						
	      						MqlUtil.mqlCommand(context, sModQuery,strPartMasterId,sNameOrg);    
	      					}	      			
	      					mqlLogWriter ( "Connect Part Master ids  = " + partRevDetailsList + "\n");
            	  		} 
            	  		
                  }
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
        writer.write(" Part Master Migration is a two step process  \n");
        writer.write(" Step1: Find all first revision Parts and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxPartMasterMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxPartMasterMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
