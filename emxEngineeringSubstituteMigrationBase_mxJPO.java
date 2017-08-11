/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.engineering.RelToRelUtil;

public class emxEngineeringSubstituteMigrationBase_mxJPO  extends emxCommonMigration_mxJPO
{
	static String RELATIONSHIP_EBOM_Substitute     = PropertyUtil.getSchemaProperty("relationship_EBOMSubstitute");
	static String srealAttrEBOMId                    = PropertyUtil.getSchemaProperty("attribute_EBOMID");
	static String srealAttrQtyName                    = PropertyUtil.getSchemaProperty("attribute_Quantity");

      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxEngineeringSubstituteMigrationBase_mxJPO (Context context, String[] args)
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
              objectSelects.add(DomainConstants.SELECT_SUBSTITUTED_COMPONENT_ID);
              
              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
              Iterator itr = mapList.iterator();
              Map map = new HashMap();
              Map mCompSub = new HashMap();
              Map ebomSubMap = new HashMap();
              Map mEBOMattMap = new HashMap();
              String strSubsPartId = "";
              String sRefDesOnEBOMRel = "";
              String sFNOnEBOMRel = "";
              String sCreateSubs  = "";
              String strSUBObjId ="";
              String strattEBOMId ="";
              String strattQty = "";
              String sParentPartRev = "";
              String sParentPartName ="";
              String strChildPartName ="";
              StringList strSubsPartIdList = new StringList();
              DomainObject domSubsObj = null;
              RelToRelUtil reltoRel  = new RelToRelUtil();
              StringList selectlist = new StringList();
              selectlist.add(DomainConstants.SELECT_ID);
              selectlist.add(DomainConstants.SELECT_NAME);
              selectlist.add(DomainConstants.SELECT_REVISION);
              StringList relsel = new StringList();
              relsel.add(DomainRelationship.SELECT_ID);
              relsel.add("attribute[" + srealAttrQtyName + "]");
              relsel.add("attribute[" + srealAttrEBOMId + "]");
              while (itr.hasNext())
              {
                    map = (Map) itr.next();
                    strSUBObjId = (String)map.get(DomainConstants.SELECT_ID);                    
                    domSubsObj = DomainObject.newInstance(context,strSUBObjId);
                    strSubsPartId = (String)map.get(DomainConstants.SELECT_SUBSTITUTED_COMPONENT_ID);
                    strSubsPartIdList.add(strSubsPartId);
                    DomainObject domSubsPart = new DomainObject(strSubsPartId);
                    //Getting Substitute Part Revision
                    String strSubsPartRev = domSubsPart.getInfo(context,DomainConstants.SELECT_REVISION);
                    String strSubsPartName = domSubsPart.getInfo(context,DomainConstants.SELECT_NAME);
                   
                     MapList mlCompSub = domSubsObj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_COMPONENT_SUBSTITUTION,
                                                                                DomainConstants.TYPE_PART,
                                                                                selectlist,
                                                                                relsel,
                                                                                true,
                                                                                false,
                                                                                (short)1,
                                                                                "",
                                                                                null);


                     Iterator mlCompSubItr = mlCompSub.iterator();
                     while (mlCompSubItr.hasNext())
                     {
                            mCompSub = (Map) mlCompSubItr.next();                            
                            strattEBOMId = (String) mCompSub.get("attribute[" + srealAttrEBOMId + "]");
                            sParentPartName = (String) mCompSub.get(DomainConstants.SELECT_NAME);
                            sParentPartRev = (String) mCompSub.get(DomainConstants.SELECT_REVISION);
                            strChildPartName = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3",strattEBOMId,"to.name","|"); 
                            strattQty = (String) mCompSub.get("attribute[" + srealAttrQtyName + "]");
                            DomainRelationship domebomrel = new DomainRelationship(strattEBOMId);
                            mEBOMattMap = domebomrel.getAttributeMap(context);
                            sRefDesOnEBOMRel = (String) mEBOMattMap.get(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                            sFNOnEBOMRel = (String) mEBOMattMap.get(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                            ebomSubMap.put(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR,sRefDesOnEBOMRel);
                            ebomSubMap.put(DomainConstants.ATTRIBUTE_FIND_NUMBER,sFNOnEBOMRel);
                            ebomSubMap.put(DomainConstants.ATTRIBUTE_QUANTITY,strattQty);    
                            sCreateSubs  = reltoRel.connect(context,RELATIONSHIP_EBOM_Substitute,strattEBOMId,strSubsPartId,false,true);                            
                            RelToRelUtil reltoRelSetatt  = new RelToRelUtil(sCreateSubs);
                            reltoRelSetatt.setAttributeValues(context, ebomSubMap);
                            
                     }
                     mqlLogWriter ("Part- "+sParentPartName+" Revision :"+ sParentPartRev +" with EC Substitute to Part-  '"+strSubsPartName+"'"+" Revision :" + strSubsPartRev+ "for Child Part- "+ strChildPartName +" Revision :"+strSubsPartRev);  
                     loadMigratedOidsList(strSubsPartIdList);
                    
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
        writer.write(" Substitute Migration is a two step process  \n");
        writer.write(" Step1: Find substitute Parts and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringSubstituteMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringSubstituteMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
