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
import com.matrixone.apps.common.*;

public class emxEngineeringRDOMigrationBase_mxJPO  extends emxCommonMigration_mxJPO
{
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxEngineeringRDOMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }


      /**
       * This method does the migration work. This method is used to Organization and
       * Project information. The Organization value will be updated based on the RDO/RCO.
       * If RDO/RCO is blank, then updates with Host Company Name. The Project will be Default
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
              String strProject = DomainAccess.getDefaultProject(context);//"Default";
              StringList objectSelects = new StringList();
              String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility");
              
              objectSelects.add(DomainConstants.SELECT_NAME);             
              objectSelects.add(DomainConstants.SELECT_ID);             
              objectSelects.add("to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.name");    
              objectSelects.add("to["+RELATIONSHIP_CHANGE_RESPONSIBILITY+"].from.name"); 
              objectSelects.add(DomainConstants.SELECT_TYPE);
              
              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);              
              Iterator itr = mapList.iterator();
              Map map = new HashMap();                      
              String strObjId ="";             
              String strObjName ="";             
              String strRDOName ="";                  
              DomainObject domObj = null;
              StringList strObjIdList = new StringList();
                                      
              DomainObject dCompanyObj = DomainObject.newInstance(context,Company.getHostCompany(context));
              String strCmp = dCompanyObj.getInfo(context, SELECT_NAME);            
              
              while (itr.hasNext())
              {
                    map = (Map) itr.next();                   
                    strObjId = (String)map.get(DomainConstants.SELECT_ID);  
                    strObjName =(String)map.get(DomainConstants.SELECT_NAME); 
                    domObj = DomainObject.newInstance(context,strObjId);                                    
                                        
                    if(domObj.isKindOf(context, TYPE_ECR)) 
                    	strRDOName = (String)map.get("to["+RELATIONSHIP_CHANGE_RESPONSIBILITY+"].from.name");
                    else
                    	strRDOName = (String)map.get("to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.name");                                      
                    	
                    
                    //If RDO is blank
                    if(strRDOName == null || "".equals(strRDOName))
                    	strRDOName = strCmp; 
                    
                    DomainObject dObj = DomainObject.newInstance(context,strObjId);
                    dObj.setPrimaryOwnership(context, strProject, strRDOName);
                    
                    
                    strObjIdList.add(strObjId);
                    
                     mqlLogWriter ("Object- "+strObjName+" AltOwner1 :"+ strRDOName +" updated ");  
                     loadMigratedOidsList(strObjIdList);                    
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
        writer.write(" RDO Migration is a two step process  \n");
        writer.write(" Step1: Find the objects based on the type(Part,ECO,Specification) passed as input parameter and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringRDOMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringRDOMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
