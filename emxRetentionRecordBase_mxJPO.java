/*   emxRetentionRecordBase
*
*   Copyright (c) 2003-2016 Dassault Systemes.
*   All Rights Reserved.
*   This program contains proprietary and trade secret information of MatrixOne,
*   Inc.  Copyright notice is precautionary only
*   and does not evidence any actual or intended publication of such program
*   This JPO contains the implementation of emxRetentionRecord
*
*/


import matrix.db.*;
import matrix.util.*;

import java.text.*;
import java.util.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.util.DateUtil;

/**
 * The <code>emxRetentionRecordBase</code> represents implementation of Retention Record Base
 *
 */
public class emxRetentionRecordBase_mxJPO extends emxDomainObject_mxJPO

{
    /**
     * Creates emxRetentionRecordBase object
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments:
     * @throws Exception if the operation fails
     */
    public emxRetentionRecordBase_mxJPO (Context context, String[] args)
        throws Exception
    {
       super(context, args);

    }


    /*
    * This method returns Retention Records in a specific state
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @param Selectedstate the selected state
    * @return MapList with the Retentions Records in a specific state
    * @throws Exception if the operation fails
    */

    public MapList getRetentionRecords(Context context, String[] args,String Selectedstate) throws Exception
    {

        MapList objectStatusList=new MapList();
        MapList objectList=new MapList();
        StringList objectStringList=new StringList();
        MapList objectFinalList=new MapList();
        MapList tempList=new MapList();

        objectStringList.add(SELECT_ID);
        objectStringList.add(SELECT_CURRENT);


        String retentionRecordpolicy = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
        String retentionRecordScheduled  = FrameworkUtil.lookupStateName(context, retentionRecordpolicy, "state_Scheduled");

        String typeRetentionRecord = (String) PropertyUtil.getSchemaProperty(context, "type_RetentionRecord");
        String typeRecordHold = (String) PropertyUtil.getSchemaProperty(context, "type_RetentionHold");

        String all=getPropertyKeyValue(context,"emxComponentsStringResource","emxComponents.Route.Approval");

        Pattern typePattern=new Pattern(typeRetentionRecord);
        typePattern.addPattern(typeRecordHold);

        String whereState="";

        int stateIndex=Selectedstate.indexOf("|");

        if(stateIndex>=0)
            whereState="current=="+Selectedstate.substring(0,stateIndex)+"|| current=="+retentionRecordScheduled;
        else if(Selectedstate.equals(all))
            whereState=null;
        else  whereState="current=="+Selectedstate;

        objectList=DomainObject.findObjects(context,
                                  typePattern.getPattern(),
                                  "*",
                                  "*",
                                  "*",
                                  "*",
                                  whereState,
                                  false,
                                  objectStringList);

         return objectList;

       }


    /*
    * This method returns All Retention Records
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList with All the Retentions Records
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllRetentionRecords(Context context, String[] args)
             throws Exception
    {
         String all=getPropertyKeyValue(context,"emxComponentsStringResource","emxComponents.Route.Approval");
         return getRetentionRecords(context,args,all);
    }



    /*
    * This method returns Only Active Scheduled Retained records
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return MapList with Active or Retained records
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getActiveScheduledStateRecords(Context context, String[] args)
          throws Exception
    {
        String retentionRecordpolicy = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
        String retentionRecordActive  = FrameworkUtil.lookupStateName(context, retentionRecordpolicy, "state_Active");
        return getRetentionRecords(context,args,retentionRecordActive+"|");
    }



    /*
    * This method returns Only Purged  Retention Records
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return MapList with APurged Records
    * @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getPurgedStateRecords(Context context, String[] args)throws Exception
   {
        String retentionRecordpolicy = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
        String retentionRecordPurged  = FrameworkUtil.lookupStateName(context, retentionRecordpolicy, "state_Purged");
        return getRetentionRecords(context,args,retentionRecordPurged);
    }


    /*
    * This method returns Only Creted state's Retention Records
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return MapList with Created Retained records
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCreatedStateRecords(Context context, String[] args)throws Exception
    {
        String retentionRecordpolicy = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
        String retentionRecordCreated  = FrameworkUtil.lookupStateName(context, retentionRecordpolicy, "state_Created");
        return getRetentionRecords(context,args,retentionRecordCreated);
    }


    /*
    * This method checks the route state based on which trigger
    * will fire and record will be promoted
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - objectId
    * @return int 0 for success
    * @throws Exception if the operation fails
    */
    public int triggerCheckPromoteApprovalRoute(Context context, String[] args) throws Exception
    {

         HashMap hmaplist = new HashMap();
         String objectId = args[0];
         DomainObject domainObject=new DomainObject(objectId);
         String currentState=domainObject.getInfo(context,DomainConstants.SELECT_CURRENT);
         MapList maplist = new MapList();
         MapList mlist = new MapList();
         boolean flag=false;
         String strStatus ="";
         String strBasePurpose ="";
         String stateCheck="";
         String relname = (String) PropertyUtil.getSchemaProperty(context,"relationship_RetainedRecord");
         String basePurpose="attribute[" + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE  + "]";
         String routeBaseState = "attribute[" +Route.ATTRIBUTE_ROUTE_BASE_STATE +"]";

         StringList objectSelects=new StringList();
         objectSelects.add(DomainConstants.SELECT_ID);
         objectSelects.add(DomainConstants.SELECT_CURRENT);
         objectSelects.add(basePurpose);

         StringList relSelects=new StringList();
         relSelects.add(routeBaseState);
         relSelects.add(DomainConstants.SELECT_NAME);

         String whereExp="relationship[" + RELATIONSHIP_OBJECT_ROUTE + "].attribute[" +Route.ATTRIBUTE_ROUTE_BASE_STATE +"] == state_"+currentState;

         String routeApprovalNotice=getPropertyKeyValue(context,"emxLibraryCentralStringResource","emxLibraryCentral.Promote.Approval");
         String routeEmptyNotice=getPropertyKeyValue(context,"emxLibraryCentralStringResource","emxLibraryCentral.Promote.Empty");
         String policy = PropertyUtil.getSchemaProperty(context, "policy_Route");
         String recordPolicy = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
         String stateComplete  = FrameworkUtil.lookupStateName(context, policy, "state_Complete");
         String routeBasePurpose=getPropertyKeyValue(context,"emxComponentsStringResource","emxComponents.Route.Approval");

         maplist=Route.getRoutes(context,objectId, objectSelects, relSelects,whereExp,true);
         if(maplist.isEmpty())
         {
             emxContextUtil_mxJPO.mqlNotice(context,routeEmptyNotice);
             return 1;
         }
         Iterator mapItr=maplist.iterator();
         while (mapItr.hasNext())
         {
             Map mapValue=(Map)mapItr.next();
             String Id=(String)mapValue.get(DomainConstants.SELECT_ID);
             strStatus = (String)mapValue.get(DomainConstants.SELECT_CURRENT);
             strBasePurpose=(String)mapValue.get(basePurpose);
             String baseState=(String)mapValue.get(routeBaseState);
             if("Ad Hoc".equals(baseState))
             baseState="state_Created";
             stateCheck = FrameworkUtil.lookupStateName(context, recordPolicy, baseState);
             if(!((strStatus.equals(stateComplete)) && (strBasePurpose.equals(routeBasePurpose))))
             {
                 flag=true;
                 break;
             }
         }
         if (flag)
         {
             emxContextUtil_mxJPO.mqlNotice(context,routeApprovalNotice);return 1;
         } else { return 0;}

  }// enf of triggerCheckPromoteApprovalRoute


    /*
    * This method calculates and set the Initiation Date for Retention Record
    * when trigger will fire from Active state to Schedule state
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - objectId
    * @return int 0 for success
    * @throws Exception if the operation fails
    */
    public void triggerActionPromoteRetentionDate(Context context, String[] args) throws Exception
    {

        boolean bfirstToken = true;
        String strFormat=com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat();
        SimpleDateFormat sdf=new SimpleDateFormat(strFormat,Locale.US);
        Date date = new Date();
        String sCurredantDate  = sdf.format(date);
        String objectId = args[0];
        String strRetentionDate = (String) PropertyUtil.getSchemaProperty(context, "attribute_RetentionDate");
        String strInitiationDate = (String) PropertyUtil.getSchemaProperty(context, "attribute_InitiationDate");
        String strAttrRetentionDate="attribute["+strRetentionDate+"]";
        String defaultRetentionPeriod = (String) PropertyUtil.getSchemaProperty(context, "attribute_DefaultRetentionPeriod");
        String defaultRetentionPeriodValue="attribute["+defaultRetentionPeriod+"]";
        String relname = (String) PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");

        String sSupprotedtypes=EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.SupportedTypes");
        	


        Pattern typePattern=null;
        StringTokenizer stringTokenizer=new StringTokenizer(sSupprotedtypes,",");
        while(stringTokenizer.hasMoreTokens())
            {
                String nextType=stringTokenizer.nextToken();
                String objType=PropertyUtil.getSchemaProperty(context,nextType);
                if(bfirstToken)
                {
                    typePattern = new Pattern(objType);
                    bfirstToken = false;
                }
                else
                    typePattern.addPattern(objType);
            }



            StringList relationshipSelects=new StringList(strAttrRetentionDate);
             StringList typeSelects=new StringList(SELECT_NAME);
            relationshipSelects.add(defaultRetentionPeriodValue);
            relationshipSelects.add(SELECT_ID);

            DomainObject dobj1=new DomainObject(objectId);
            dobj1.setAttributeValue(context,strInitiationDate,sCurredantDate);

            MapList mlist=dobj1.getRelatedObjects
                                              (context,
                                               relname,
                                               typePattern.getPattern(),
                                               typeSelects,
                                               relationshipSelects,
                                               true,
                                               true,
                                               (short)0,
                                               null,
                                               null);
           if(mlist.isEmpty())
            {
                String routeApprovalNotice=getPropertyKeyValue(context,"emxLibraryCentralStringResource","emxLibraryCentral.Check.Document");
                emxContextUtil_mxJPO.mqlNotice(context,routeApprovalNotice);
            }

            Iterator itr=mlist.iterator();
            while(itr.hasNext())
            {
                Map mapValue=(Map)itr.next();
                String relID=(String)mapValue.get(SELECT_ID);
                String defRetPeriod=(String)mapValue.get(defaultRetentionPeriodValue);
                String retRecDate=calculateRetentionDate(context,defRetPeriod,sCurredantDate);
                DomainRelationship rel=new DomainRelationship(relID);
                rel.setAttributeValue(context,strRetentionDate,retRecDate);
            }

    }//end of function statePromoteFromCreatedToActive


    /*
    * This method calculates Retention Date
    * Date will be caluculated when trigger will fire from Active state to Schedule state
    *
    * @param context the eMatrix <code>Context</code> object
    * @param attValue
    * @param sCurredantDate the curent date
    * @return the Retention date
    * @throws Exception if the operation fails
    */
    public String calculateRetentionDate(Context context, String  attValue,String sCurredantDate) throws Exception
    {
        Date dateVar=new Date();
        String decpart="";
        String intpart="";
        String strFormat=com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat();;
        int dateIndex=0,monValue=0;
        float monthValue,dec;
        dateIndex=attValue.indexOf(".");

        if(dateIndex>=0)
        {
            intpart=attValue.substring(0,attValue.indexOf("."));
            decpart=attValue.substring(attValue.indexOf(".")+1);

            dec=Float.parseFloat(decpart);
            //Modified for  Bug No 335000 0 X+2 Begin--from dec/10 to dec/(Math.pow(10,decpart.length))
            monthValue=(float)(dec/(Math.pow(10,decpart.length())))*12;
            //Modifed for Bug No 335000 0 X+2 End
            Float monthFloat=new Float(monthValue);
            monValue=monthFloat.intValue();

        }
        else
        {
            intpart=attValue;
            monValue=0;
        }
        int firstpart=Integer.parseInt(intpart);
        Date dateCalc=new Date(sCurredantDate);
        dateVar=DateUtil.computeFinishDate(dateCalc,firstpart,monValue);
        
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(dateVar);
        
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dateCalendar.set(Calendar.MINUTE, 0);
        dateCalendar.set(Calendar.SECOND, 0);
        dateCalendar.set(Calendar.MILLISECOND,0);
        
        Date dateVarWithoutTime = dateCalendar.getTime();
        SimpleDateFormat sdf=new SimpleDateFormat(strFormat,Locale.US);
        
        String retentionDateValue= sdf.format(dateVarWithoutTime);
        
        return  retentionDateValue;

    }//end of calculateDate


    /*
    * This method Deletes a document when delete trigger fires
    * Delete trigger will be fire iff Retention feature is in enable state
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - objectId
    * @return int 0 for success
    * @throws Exception if the operation fails
    */
    public int triggerCheckAssociatedRetentionRecord(Context context, String[] args) throws Exception
    {

        String objId = args[0];
        DomainObject dObj =new DomainObject(objId);
        String objectTypeName=dObj.getInfo(context,SELECT_TYPE);

        String enableFeature	= EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.RetentionRecordFeature");
        String sSupprotedtypes	= EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.SupportedTypes");

        boolean flag=false;
        flag=checkSupportedTypes(context,objectTypeName);

        if("TRUE".equalsIgnoreCase(enableFeature))
        {
            StringTokenizer stringTokenizer=new StringTokenizer(sSupprotedtypes,",");
            while(stringTokenizer.hasMoreTokens())
            {
                String nextType=stringTokenizer.nextToken();
                String objType=(String)PropertyUtil.getSchemaProperty(context,nextType);
                if(objectTypeName.equals(objType) || flag==true)
                {
                    String objectId = args[0];
                    DomainObject dobj=new DomainObject(objectId);
                    String relname = (String) PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
                    StringList typeSelects=new StringList(SELECT_NAME);
                    Pattern RelPattern=new Pattern(relname);
                    String typeName = (String) PropertyUtil.getSchemaProperty(context, "type_RetentionRecord");
                    Pattern typePattern=new Pattern(typeName);
                    MapList objectList=dobj.getRelatedObjects(context,
                                           RelPattern.getPattern(),
                                           typePattern.getPattern(),
                                           typeSelects,
                                           null,
                                           true,
                                           false,
                                           (short)0,
                                           null,
                                           null);
                    String notice=getPropertyKeyValue(context,"emxLibraryCentralStringResource","emxLibraryCentral.Delete.Document");
                    if(!objectList.isEmpty())
                    {
                        emxContextUtil_mxJPO.mqlNotice(context,notice);
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }//end of inside if
        }//end of while

    }//end of main if

    return 0;
   }//end of triggerCheckAssociatedRetentionRecord



   /**
    * This method checks whether child Object Type is Document Type or not
    * If yes return true else return false
    *
    * @param context the eMatrix <code>Context</code> object
    * @param objectTypeName the Object Type
    * @return boolean true for supported types
    * @throws Exception if the operation fails
    */
   public boolean checkSupportedTypes(Context context,String objectTypeName) throws Exception
   {
   		boolean flag=false;
   		String typeGenericDocument= (String) PropertyUtil.getSchemaProperty(context, "type_GenericDocument");
   		String typeDocument=(String) PropertyUtil.getSchemaProperty(context, "type_Document");
   		if(!(typeDocument.equalsIgnoreCase(objectTypeName) || typeGenericDocument.equalsIgnoreCase(objectTypeName)))
   		{
   			flag=mxType.isOfParentType(context,objectTypeName,typeDocument);
   		}
     	return flag;
    }


    /**
    * This method checks whether Record Retention Feature is true or false in properties setting.
    * listed as property emxLibraryCentral.RetentionRecordFeature in the emxLibraryCentral.properties file
    *
    * @param context The ematrix context of the request.
    * @param args holds the following list of arguments:
    *       0 - programMap contains the objectId
    * @return Boolean  - true if Record Retention Feature is true otherwise false
    */
    public Boolean retentionFeatureEnabledCheck(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId  = (String) programMap.get("objectId");
        DomainObject dobj =new DomainObject(objectId);
        String typeName=dobj.getInfo(context,SELECT_TYPE);
		String strPolicy=dobj.getInfo(context,SELECT_POLICY);

        boolean bfirstToken = true;
        boolean booleanValue = false;

        boolean flag=false;
        flag=checkSupportedTypes(context,typeName);

        String RetentionEnabled = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.RetentionRecordFeature");
        String sSupprotedtypes= EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.SupportedTypes");
        String strPolicyVersion = PropertyUtil.getSchemaProperty(context, "policy_Verion");

        StringTokenizer stringTokenizer=new StringTokenizer(sSupprotedtypes,",");
        if("TRUE".equalsIgnoreCase(RetentionEnabled))
        {
            while(stringTokenizer.hasMoreTokens())
            {
                String nextType=stringTokenizer.nextToken();
                String objType=(String)PropertyUtil.getSchemaProperty(context,nextType);

                if(!strPolicy.equals(strPolicyVersion) && (typeName.equals(objType) || flag==true)){booleanValue = true;}
            }

        }
        return new Boolean(booleanValue);
    }//end of RetentionFeatureEnabled


    /**
    * Gets the property key value
    * @param context the eMatrix <code>Context</code> object
    * @param  propertyFileName
    * @param  getPropertyKeyName
    * @return String key value
    * @throws Exception if the operation fails
    * @exclude
    */
    public String getPropertyKeyValue(Context context, String propertyFileName,String getPropertyKeyName) throws Exception
    {

        String strLanguage  =  context.getSession().getLanguage();
        String propertyKeyValue= EnoviaResourceBundle.getProperty(context,propertyFileName,new Locale(strLanguage),getPropertyKeyName);
        return(propertyKeyValue);
    }


    /**
    * This method notifies Originator of the Issue while promoting from create to assign.
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args holds no arguments:
    * @return - int 0 if success
    * @throws Exception if the operation fails
    */

    public int notifyOriginator(Context context, String[] args) throws Exception
    {
        try
        {
            String strDateTemp ="";
            String selectObjOwnerRRN="";
            String strLanguage  =context.getSession().getLanguage();

            String strRetentionRecord = PropertyUtil.getSchemaProperty(context,"type_RetentionRecord");
            String strRetentionHold = PropertyUtil.getSchemaProperty(context,"type_RetentionHold");
            String policyRetentionRecord = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
            String policyRetentionHold = PropertyUtil.getSchemaProperty(context, "policy_RetentionHold");
            String relRetainedRecord = PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
            String retentionDate = (String) PropertyUtil.getSchemaProperty(context,"attribute_RetentionDate");
            String recordStateCreated  = FrameworkUtil.lookupStateName(context, policyRetentionRecord, "state_Created");
            String recordStateActive  = FrameworkUtil.lookupStateName(context, policyRetentionRecord, "state_Active");
            String holdStateReleased  = FrameworkUtil.lookupStateName(context, policyRetentionHold, "state_Released");

            //Getting Current Date from System
            String strDateFormat = com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(strDateFormat,Locale.US);
            Date date = new Date();
            String sCurrentDate  = sdf.format(date);
            Date sCurrentDateFormate=sdf.parse(sCurrentDate);

            //Type Pattern For searching all Document objects.
            Pattern typePatternDocument=null;
            String sSupprotedtypes= EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.SupportedTypes"); 


            StringTokenizer stringTokenizer=new StringTokenizer(sSupprotedtypes,",");
            boolean bfirstToken = true;

            while(stringTokenizer.hasMoreTokens()){
                String nextType=stringTokenizer.nextToken();
                String objType=PropertyUtil.getSchemaProperty(context,nextType);
                if(bfirstToken)
                {
                    typePatternDocument = new Pattern(objType);
                    bfirstToken = false;
                }
                else
                    typePatternDocument.addPattern(objType);
            }


            //Where clause for Only those Document which is connected Retained Record Relationship.
            String relDoc="to["+relRetainedRecord+"]== true";

            //Where clause for Only those RetentionHold which is in Created,Active and Hold State.
            String currentState="current!=="+holdStateReleased;



            //Searching all Documents which is related to Retained Record Relationship.
            MapList objectDocList=new MapList();

            //Object Selects For Document.
            StringList objectDocStringList=new StringList();
            objectDocStringList.add(SELECT_ID);
            objectDocStringList.add(SELECT_NAME);

            objectDocList=DomainObject.findObjects(context,
                                  typePatternDocument.getPattern(),
                                  "*",
                                  "*",
                                  "*",
                                  "*",
                                  relDoc,
                                  true,
                                  objectDocStringList);//End searching Document

            //Start Iteration for sending IconMail.
            Map keyPairOID=null;
            Iterator itrForMail=objectDocList.iterator();
            while(itrForMail.hasNext())
            {
                Map strDocID = (Map) itrForMail.next();
                keyPairOID = new HashMap();
                String objectDocID=(String)strDocID.get(SELECT_ID);
                String objectDocName=(String)strDocID.get(SELECT_NAME);
                DomainObject DocObj=new DomainObject(objectDocID);
                String strRetentionDate="attribute["+retentionDate+"]";

    //Object Selects For Related Objects to Document.
                SelectList objeselects= new SelectList();
                objeselects.addElement(DomainObject.SELECT_ID);
                objeselects.addElement(DomainObject.SELECT_TYPE);
                objeselects.addElement(DomainObject.SELECT_NAME);
                objeselects.addElement(DomainObject.SELECT_OWNER);
                objeselects.addElement(DomainObject.SELECT_CURRENT);


//Searching all Retention Hold which is in Created,Active and Hold state.
                MapList mlRetentionHoldList= DocObj.getRelatedObjects(context,
                                                            relRetainedRecord,
                                                            strRetentionHold,
                                                            objeselects,
                                                            null,
                                                            true,
                                                            true,
                                                            (short)0,
                                                            currentState,
                                                            null);//End searching for Retention Hold


//if MapList is not Empty then start searching for next Document else checking for all Retention Record condition.
                if(!mlRetentionHoldList.isEmpty()){
                    continue;//Exit from the loop
                }
    //checking for all Retention Record condition.
                else
                {
                    boolean sendMailtoOwner=true;
                    //SelectList for getting Retention date.
                    SelectList relRetentionDateselects= new SelectList();
                    relRetentionDateselects.addElement(strRetentionDate);
                    //searching for all Retention Record related to Document
                    MapList retentionRecordList= DocObj.getRelatedObjects(context,
                                                                relRetainedRecord,
                                                                strRetentionRecord,
                                                                objeselects,
                                                                relRetentionDateselects,
                                                                true,
                                                                true,
                                                                (short)0,
                                                                null,
                                                                null);//End of search.

                    StringList attrRetentionDateList = new StringList();
                    StringList ownerList=new StringList();
                    String selectObjIdRRN="";
                    String selectObjStateRRN="";
                    Integer retentiondifDate=null;
                    int difRetentionDate;
                    int mapSize=0;


                    //Iteration for checking all Retention Record condition
                    Iterator itrRetentionRecord=retentionRecordList.iterator();
                    while(itrRetentionRecord.hasNext())
                    {
                        Map mpRetentionRecord = (Map) itrRetentionRecord.next();
                        selectObjIdRRN = (String)mpRetentionRecord.get(DomainConstants.SELECT_ID);
                        String selectObjNameRRN = (String)mpRetentionRecord.get(DomainConstants.SELECT_NAME);
                        selectObjOwnerRRN = (String)mpRetentionRecord.get(DomainConstants.SELECT_OWNER);
                        selectObjStateRRN = (String)mpRetentionRecord.get(DomainConstants.SELECT_CURRENT);

                        //if Retention Record is in Created or Active state then dont send mail.
                        if(selectObjStateRRN.equals(recordStateCreated)||selectObjStateRRN.equals(recordStateActive)){
                            sendMailtoOwner=false;
                        }//End of if.

                        //else check all related Retention Date which is less then current date.
                        else
                        {
                            String attrRetentionDate=(String)mpRetentionRecord.get(strRetentionDate);
                            attrRetentionDateList.addElement(attrRetentionDate);
                            if (!ownerList.contains(selectObjOwnerRRN))
                            {
                                ownerList.add(selectObjOwnerRRN);
                                StringList idList=new StringList();
                                idList.addElement(selectObjIdRRN);
                                keyPairOID.put(selectObjOwnerRRN, idList);
                            }
                            else{
                                StringList tempList = (StringList)keyPairOID.get(selectObjOwnerRRN);
                                tempList.addElement(selectObjIdRRN);
                                keyPairOID.put(selectObjOwnerRRN, tempList);
                            }
                            mapSize = keyPairOID.size();
                        }//End of else.

                    }//End of While.

                    //Iteration for checking all Retention Date which is less then todays(System) Date.
                    Iterator retentionDateItr=attrRetentionDateList.iterator();
                    Vector   vctCompareRetentionDate=new Vector();
                    Integer  intObjOne=new Integer(1);
                    Integer  intObjZero=new Integer(0);

                    while(retentionDateItr.hasNext())
                    {
                        strDateTemp = (String)retentionDateItr.next();
                        Date sRetentionDateFormate=sdf.parse(strDateTemp);
                        difRetentionDate=sRetentionDateFormate.compareTo(sCurrentDateFormate);
                        retentiondifDate= new Integer(difRetentionDate);
                        vctCompareRetentionDate.add(retentiondifDate);
                    }

                    //if Retention Date is today or less then dont send IconMail.
                    if(vctCompareRetentionDate.contains(intObjOne)|| vctCompareRetentionDate.contains(intObjZero)){
                        sendMailtoOwner=false;
                    }

                    String key="";
                    StringList value=new StringList();
                    StringList bccList = new StringList();
                    StringList ccList = new StringList();
                    MailUtil mail = new MailUtil();
                    String strPropertyFile = "emxLibraryCentralStringResource";
                    String subject = EnoviaResourceBundle.getProperty(context, strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Message.SubjectForPurging");
                    String message = EnoviaResourceBundle.getProperty(context, strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Message.MessageForPurging");

                    //Getting All key Value pair for Map.
                    Object[] keyValuePairs = keyPairOID.entrySet().toArray();
                    for (int i = 0; i < mapSize; i++)
                    {
                        StringList finalKey=new StringList();
                        Map.Entry entry = (Map.Entry) keyValuePairs[i];

                        key   = (String)entry.getKey();
                        value = (StringList)entry.getValue();
                        finalKey.addElement(key);


                        if( sendMailtoOwner){
                            mail.sendMessage(context,
                            finalKey,
                            ccList,
                            bccList,
                            subject,
                            message,
                            value);
                        }//End of if.

                    }//End of for.

                }//End of else.

            }//End of while.
        }//End of try.
        catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw new FrameworkException((String) ex.getMessage());
        }//End of catch.
        return 0;
    }//End of method notifyOriginator.

    /**
    * This method checks whether Record Retention Feature is true or false in properties setting.
    * listed as property emxLibraryCentral.RetentionRecordFeature in the emxLibraryCentral.properties file
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments:
    * @return Boolean object - true if Record Retention Feature is true otherwise false
    */

    public Boolean retentionFeatureEnabled (Context context, String[] args)
    {
        boolean booleanValue = false;
        try
        {
            String RetentionEnabled = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.RetentionRecordFeature");
            //if returns true Retention Record Feature is enabled else not enabled.
            if("true".equalsIgnoreCase(RetentionEnabled))
            {
                booleanValue = true;
            }
        }
        catch (Exception fe)
        {
            booleanValue = false;
        }

        finally {
            return new Boolean(booleanValue);
        }
    }
}
