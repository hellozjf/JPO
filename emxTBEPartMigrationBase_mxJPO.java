/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.engineering.EngineeringConstants;

/**
 * @author QZV
 *
 * The &lt;code&gt;${CLASSNAME}&lt;/code&gt; class contains ...
 * 
 * Script to migrate "Design Part", "Engineering Part" policy parts to "Development Part" policy
 *
 * @version TBE 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxTBEPartMigrationBase_mxJPO	extends emxCommonMigration_mxJPO {

      static String ATTRIBUTE_VPM_VISIBLE           = PropertyUtil.getSchemaProperty("attribute_isVPMVisible");
      static String ATTRIBUTE_V_NAME                = PropertyUtil.getSchemaProperty("attribute_V_Name");

      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxTBEPartMigrationBase_mxJPO (Context context, String[] args)
              throws Exception  {
          super(context, args);
      }

      /**
       * This method does the migration work.  All exsting "Engineering Part", "Design Part" policy objects
       * will be migrated to "Development Part" policy
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList) throws Exception {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan) {
              return;
          }

          try {
              StringList objectSelects = new StringList(SELECT_POLICY);
              objectSelects.add(SELECT_ID);
              objectSelects.add(SELECT_CURRENT);

              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
              
              Iterator itr = mapList.iterator();
              Map map = new HashMap();
              
              String objectId = null;
              String strPartPolicy = null;
              String strPartCurrent = null;
              String vpmProductName = null;
              StringList altOwnerList = new StringList();;
              
              MqlUtil.mqlCommand(context, "trigg off");
              
              Map stateMappingMap  = new HashMap();
              stateMappingMap.put("In Work", STATE_DEVELOPMENT_PART_CREATE);
              stateMappingMap.put("Approved", STATE_DEVELOPMENT_PART_PEER_REVIEW);
              stateMappingMap.put("Limited Release", STATE_DEVELOPMENT_PART_PEER_REVIEW);
              stateMappingMap.put("Released", STATE_DEVELOPMENT_PART_COMPLETE);
              stateMappingMap.put("Obsolete", PropertyUtil.getSchemaProperty(context, "policy", POLICY_DEVELOPMENT_PART, "state_Obsolete"));
              
              while (itr.hasNext()) {
                  map = (Map) itr.next();
                  objectId = (String) map.get(SELECT_ID);
                  strPartPolicy = (String)map.get(SELECT_POLICY);
                  strPartCurrent = (String)map.get(SELECT_CURRENT);
                  
                  //Check the VPM product's V_Name value
                  vpmProductName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",objectId,
                        "from["+ EngineeringConstants.RELATIONSHIP_PART_SPECIFICATION +"|to.type.kindof==\"PLMEntity\"].to.attribute[PLMEntity.V_Name]");
                  vpmProductName =  FrameworkUtil.findAndReplace(vpmProductName, "\"", "\\\"");
                	  
                  
                  //Check for altowner1, altowner2 values 
                  altOwnerList = FrameworkUtil.split(MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",objectId,
                                                                        "from["+ EngineeringConstants.RELATIONSHIP_PART_SPECIFICATION +"|to.type.kindof==\"PLMEntity\"].to.altowner1 from["+ EngineeringConstants.RELATIONSHIP_PART_SPECIFICATION +"|to.type.kindof==\"PLMEntity\"].to.altowner2","|"), "|");
                  

                  //Policy Modification
                  StringBuffer sModQuery = new StringBuffer(64);
				  sModQuery.append("mod bus " + objectId + " policy '"+ EngineeringConstants.POLICY_DEVELOPMENT_PART +"'");

                  //Setting the VPM Visible value to FALSE if policy is "Engineering Part"
                  if("Engineering Part".equalsIgnoreCase(strPartPolicy)) {
                      sModQuery.append(" '"+ATTRIBUTE_VPM_VISIBLE+"' FALSE");
                  } else {
                      sModQuery.append(" '"+ATTRIBUTE_VPM_VISIBLE+"' TRUE");
                  }
                  sModQuery.append(" '"+ATTRIBUTE_V_NAME+"' \""+vpmProductName+"\"");

                  if(altOwnerList.size() == 2) {
                      sModQuery.append(" altowner1 '"+altOwnerList.get(0)+"' altowner2 '"+altOwnerList.get(1)+"'");
                      MqlUtil.mqlCommand(context, "mod bus $1 policy $2 $3 $4 $5 $6 altowner1 $7 altowner2 $8",
                                         objectId,EngineeringConstants.POLICY_DEVELOPMENT_PART,
                                         ATTRIBUTE_VPM_VISIBLE, Boolean.toString(!"Engineering Part".equalsIgnoreCase(strPartPolicy)),
                                         ATTRIBUTE_V_NAME,vpmProductName,
                                         (String)altOwnerList.get(0),(String)altOwnerList.get(1));
                  } else  {
                      mqlLogRequiredInformationWriter(objectId + " doesnt have altowner information \n");
                      MqlUtil.mqlCommand(context, "mod bus $1 policy $2 $3 $4 $5 $6",
                                         objectId,EngineeringConstants.POLICY_DEVELOPMENT_PART,
                                         ATTRIBUTE_VPM_VISIBLE, Boolean.toString(!"Engineering Part".equalsIgnoreCase(strPartPolicy)),
                                         ATTRIBUTE_V_NAME,vpmProductName);
                  }

                  //Set state of the object
                  MqlUtil.mqlCommand(context, "mod bus $1 current $2",objectId,(String)stateMappingMap.get(strPartCurrent));

                  //mqlLogWriter (sModQuery.toString() + "\n");
                  mqlLogRequiredInformationWriter(sModQuery.toString() + ";\n");
                  
              }
              
              //Make Design Part and Engineering Part policies hidden
              String designPartPolicy = PropertyUtil.getSchemaProperty(context,"policy_DesignPart");
              String engPartPolicy = PropertyUtil.getSchemaProperty(context,"policy_EngineeringPart");

              MqlUtil.mqlCommand(context, "modify policy $1 hidden",designPartPolicy);
              MqlUtil.mqlCommand(context, "modify policy $1 hidden",engPartPolicy);
              
          } catch(Exception ex) {
              ex.printStackTrace();
              throw ex;
          } finally {
              MqlUtil.mqlCommand(context, "trigg on");
          }
      }

    public void help(Context context, String[] args) throws Exception {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" TBE Part Migration is a two step process  \n");
        writer.write(" Step1: Find all first Parts and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxTBEPartMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxTBEPartMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
