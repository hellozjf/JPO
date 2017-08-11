/*
 *emxRetainedDocumentBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.12.1.4 Wed Oct 22 16:02:20 2008 przemek Experimental przemek $
 */
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.Context;
import matrix.db.FileItr;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.apps.domain.util.XSSUtil;


/**
 * The <code>emxRetainedDocumentBase</code> represents implementation of Retained Document Base
 *
 */
public class emxRetainedDocumentBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Creates emxRetainedDocumentBase object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxRetainedDocumentBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns int
     * @throws Exception if the operation fails
     * @exclude
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        return 0;
    }

    /**
    * This Method gets the Documents which are Connected to Retention Record or Retention Hold Type
    * with Retained Record Relationship
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - objectId
    * * @return MapList with the list of retained documents
    * @throws Exception if the operation fails
    */
  public MapList getDocuments(Context context,String[] args,String SelectedFilter)
        throws Exception
    {
        String strLanguage  =  context.getSession().getLanguage();
        MapList objectList=new MapList();
        HashMap map=(HashMap)JPO.unpackArgs(args);
        String strId=(String)map.get("objectId");
        DomainObject dom=DomainObject.newInstance(context,strId);
        String selectedType=dom.getInfo(context,SELECT_TYPE);
        String strPropertyFile = "emxLibraryCentralStringResource";
        String sFilterAll= EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Filter.All");
        String sFilterScheduled= EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Filter.Scheduled");
        String sFilterPurged= EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Filter.Purged");
        String relname=(String)PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
        String PurgeDoctype=(String)PropertyUtil.getSchemaProperty(context, "type_PurgedRecord");
        String sRetainedRecord=(String)PropertyUtil.getSchemaProperty(context, "type_RetentionRecord");
        String sAttrcom=PropertyUtil.getSchemaProperty(context, "attribute_Comments");
        String sAttrRetentionDate=PropertyUtil.getSchemaProperty(context, "attribute_RetentionDate");
        String policyPurgedRecord = PropertyUtil.getSchemaProperty(context, "policy_PurgedRecord");
        String strStatePurged  = FrameworkUtil.lookupStateName(context, policyPurgedRecord, "state_Purged");
        String DocRetDate ="attribute[" + sAttrRetentionDate +"]";
        SelectList relSelects=new SelectList(2);
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        relSelects.add(DocRetDate);
        SelectList typeSelects=new SelectList(3);
        typeSelects.add(DomainConstants.SELECT_ID);
        typeSelects.add(DomainRelationship.SELECT_ID);
        typeSelects.add(DomainConstants.SELECT_CURRENT);
        Pattern RelPattern=new Pattern(relname);

        if (sFilterPurged.equalsIgnoreCase(SelectedFilter))
        {
            objectList=dom.getRelatedObjects(context,
                                                RelPattern.getPattern(),
                                                PurgeDoctype,
                                                typeSelects,
                                                relSelects,
                                                false,
                                                true,
                                                (short)2,
                                                null,
                                                null);
        }//If filter is Purged.
        else
        {
            String sSupprotedtypes=EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.SupportedTypes");
            Pattern typePattern=null;
            StringTokenizer stringTokenizer=new StringTokenizer(sSupprotedtypes,",");
            boolean bfirstToken = true;
            while(stringTokenizer.hasMoreTokens())
            {
                String nextType=stringTokenizer.nextToken();
                String objType=PropertyUtil.getSchemaProperty(context,nextType);
                if(bfirstToken)
                {
                    typePattern = new Pattern(objType);
                    typePattern.addPattern(PurgeDoctype);
                    bfirstToken = false;
                }
                else
                    typePattern.addPattern(objType);
            }//End of while loop.
        if(SelectedFilter.equalsIgnoreCase(sFilterAll)){
            String sWhere="";
            objectList=dom.getRelatedObjects(context,
                                                RelPattern.getPattern(),
                                                typePattern.getPattern(),
                                                typeSelects,
                                                relSelects,
                                                false,
                                                true,
                                                (short)0,
                                                sWhere,
                                                null);
        }
        else if(SelectedFilter.equalsIgnoreCase(sFilterScheduled))
        {
                Calendar cal = Calendar.getInstance();
                Date date=new Date();
                Date RetentionDocDateFormat=new Date();
                String dateFormat = com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(dateFormat);
                String sCurrentDate  = sdf.format(date);
         objectList=dom.getRelatedObjects(context,
                                                RelPattern.getPattern(),
                                                typePattern.getPattern(),
                                                typeSelects,
                                                relSelects,
                                                false,
                                                true,
                                                (short)0,
                                                null,
                                                null);
        Iterator itrDocListItr = objectList.iterator();
        MapList objectScheduleList=new MapList();
        while( itrDocListItr.hasNext())
        {
                    Map mapObjectDocMap = (Map) itrDocListItr.next();
                    String sRelId = mapObjectDocMap.get(DomainRelationship.SELECT_ID).toString();
                    String docState = (String) mapObjectDocMap.get(DomainObject.SELECT_CURRENT);
                    String sRetentionDateDoc=DomainRelationship.getAttributeValue(context, sRelId, sAttrRetentionDate);
                    if(sRetentionDateDoc!=null && !sRetentionDateDoc.equals("")&&!sRetentionDateDoc.equalsIgnoreCase("null")){
                        RetentionDocDateFormat=eMatrixDateFormat.getJavaDate(sRetentionDateDoc);
                        boolean stDifDate=date.after(RetentionDocDateFormat);
                             if(stDifDate&&!docState.equals(sFilterPurged)){
                                     objectScheduleList.add(mapObjectDocMap);
                                     }//End of if loop.
                    }//End of if loop.
                    if(selectedType.equals(PurgeDoctype))
                    	mapObjectDocMap.put("disableSelection", "false");
                    else
                    	mapObjectDocMap.put("disableSelection", "true");
             }//End of while loop.
             objectList=objectScheduleList;
        }//End of else if
      }//End of else loop.
      return objectList;
   }//End of getDocument method.



    /**
    * This Method gets the Documents for "All" filter
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return Maplist the List of Retained Documents
    * @throws Exception if the operation fails
    */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllDocuments(Context context,String[] args)
        throws Exception
    {
        String strLanguage  =context.getSession().getLanguage();
        String strPropertyFile = "emxLibraryCentralStringResource";
        String sFilterAll= EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Filter.All");
        return getDocuments(context,args,sFilterAll);
    }

    /**
    * This Method gets the Documents for "Scheduled" filter
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList the Scheduled documents
    * @throws Exception if the operation fails
    */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getScheduledDocuments(Context context,String[] args)
        throws Exception
    {
        String strLanguage  =context.getSession().getLanguage();
        String strPropertyFile = "emxLibraryCentralStringResource";
        String sFilterScheduled= EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Filter.Scheduled");
        return getDocuments(context,args,sFilterScheduled);
    }

    /**
    * This Method gets the Documents for "Purged" filter
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return MapList with the List of purged document
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPurgedDocuments(Context context,String[] args)
        throws Exception
    {
        String strLanguage  =context.getSession().getLanguage();
        String strPropertyFile = "emxLibraryCentralStringResource";
        String sFilterPurged= EnoviaResourceBundle.getProperty(context, strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Filter.Purged");
        return getDocuments(context,args,sFilterPurged);
    }

    /**
    * This Method has been written to edit the Comments attribute for "Edit"
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - paramMap contains    relID the relationshipID and the newComment Value
    * @return 0
    * @throws Exception if the operation fails
    */

    public int UpdateComments(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String relname=(String)PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
        String sAttrcom=PropertyUtil.getSchemaProperty(context, "attribute_Comments");
        String objectId  = (String)paramMap.get("relId");
        String commentvalue  = (String)paramMap.get("New Value");
        DomainRelationship obj=new DomainRelationship(objectId);
        obj.setAttributeValue(context,sAttrcom,commentvalue);
        return 0;
    }

    /**
    * This Method has been written to edit the RetentionPeriodvalue attribute for "Edit"
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - paramMap contains    relID the relationshipID and the RetentionPeriodvalue Value
    * @return 0
    * @throws Exception if the operation fails
    */

    public int UpdateRetentionPeriod(Context context, String[] args) throws Exception
    {

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String relname=(String)PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
        String RetentionPeriod=PropertyUtil.getSchemaProperty(context, "attribute_DefaultRetentionPeriod");
        String objectId  = (String)paramMap.get("relId");
        String RetentionPeriodvalue  = (String)paramMap.get("New Value");
        DomainRelationship obj=new DomainRelationship(objectId);
        obj.setAttributeValue(context, RetentionPeriod,RetentionPeriodvalue);
        return 0;
    }

    /**
    * This Method has been written to disable the CheckBox if theDocument is of type Purged record
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    */

    public Vector displayCheckBoxColumn(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Map objectMap = null;
        Vector checkBoxCol = null;

        String PurgeDoctype=(String)PropertyUtil.getSchemaProperty(context, "type_PurgedRecord");
        int objectListSize = 0 ;
        if(objectList != null && (objectListSize = objectList.size()) > 0)
        {
            checkBoxCol = new Vector(objectListSize);
            String presentType = "";
            for(int i=0; i< objectListSize; i++)
            {
                objectMap = (Map) objectList.get(i);
               String sID = (String)objectMap.get(SELECT_ID);
           DomainObject dobj=new DomainObject(sID);
           presentType= dobj.getInfo(context,SELECT_TYPE);
                if(presentType.equals(PurgeDoctype))
                {
                    checkBoxCol.add("false");
                }
                else
                {
                    checkBoxCol.add("true");
                }
            }
        }
        return checkBoxCol;
    }

    /**
    * This Method has been written to set the Attributes
    * when a document is connected to Record Retenton with
    * Retained Record relationship
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */

  public int triggerCreateActionSettingRelationshipAttributes(Context context,String[] args)
        throws Exception
    {
        int ret = 0;
        if(args == null || args.length < 3)
        {
            throw new FrameworkException("ERROR - Invalid number of arguments");
        }

        try
        {
            String fromobjectId = args[0];
            String sRelId=args[1];
            String srelName=PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
            String sattrDefaultRetPeriod=PropertyUtil.getSchemaProperty(context, "attribute_DefaultRetentionPeriod");

            DomainObject fromId= (DomainObject)DomainObject.newInstance(context,fromobjectId);


            String sDefaultRetentionPeriod=fromId.getAttributeValue(context, sattrDefaultRetPeriod);
            DomainRelationship.setAttributeValue(context,sRelId,sattrDefaultRetPeriod,sDefaultRetentionPeriod);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            ret=1;
        }

        return ret;

    }



 /**
    * This Method has been written for Access Program
    * for Edit Retention Period and Add Existing
    * Depending upon the conditions
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */
    public boolean isInActiveState(Context context,String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String sobjectId = (String)programMap.get("objectId");
        StringList sList=new StringList(3);
        boolean bAccess=false;
        sList.add(DomainConstants.SELECT_ID);
        sList.add(DomainConstants.SELECT_CURRENT);
        DomainObject sOId=DomainObject.newInstance(context,sobjectId);
        Map List=sOId.getInfo(context,sList);
        String sState=(String)List.get("current");
        String policy = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
        String stateActive  = FrameworkUtil.lookupStateName(context, policy, "state_Active");

        if(sState.equals(stateActive)){
            bAccess= true;
        }
        else{
            bAccess= false;
        }
        return  bAccess;

    }
    /**
    * This Method has been written to dispaly the Retention Schedules
    * From The Document Tree Node
    * @param context the eMatrix <code>Context</code> object
    * @param args holds The ObjectId
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRetentionSchedules(Context context,String[] args)
        throws Exception
    {
        MapList objectList=new MapList();
        MapList  DocList=new MapList();
        HashMap map=(HashMap)JPO.unpackArgs(args);
        String strId=(String)map.get("objectId");
        DomainObject dom=DomainObject.newInstance(context,strId);
        SelectList relSelects=new SelectList(1);
        relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        String relname=(String)PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
        String documenttype=(String)PropertyUtil.getSchemaProperty(context, "type_DOCUMENTS");
        String GenDoctype=(String)PropertyUtil.getSchemaProperty(context, "type_GenericDocument");
        String sRetentionRecord=(String)PropertyUtil.getSchemaProperty(context, "type_RetentionRecord");
        String sRetentionHold=(String)PropertyUtil.getSchemaProperty(context, "type_RetentionHold");
        String sattrRetentionDate =(String)PropertyUtil.getSchemaProperty(context, "attribute_RetentionDate");
        String PurgeDoctype=(String)PropertyUtil.getSchemaProperty(context, "type_PurgedRecord");

        String Docdate = "relationship[" + relname+ "].attribute[" + sattrRetentionDate+ "].to.id";

        SelectList typeSelects=new SelectList(1);
        typeSelects.add(DomainConstants.SELECT_ID);
        typeSelects.add(DomainConstants.SELECT_TYPE);

        typeSelects.add(Docdate);


        Pattern typePattern=new Pattern(sRetentionRecord);
        typePattern.addPattern(sRetentionHold);

        Pattern RelPattern=new Pattern(relname);
        objectList=dom.getRelatedObjects(context,
                                            RelPattern.getPattern(),
                                            typePattern.getPattern(),
                                            typeSelects,
                                            relSelects,
                                            true,
                                            false,
                                            (short)1,
                                            null,
                                            null);

        Iterator itrContentList = objectList.iterator();
        while( itrContentList.hasNext())  {
            Map mapObjectMap = (Map) itrContentList.next();
            String strDocType = (String) mapObjectMap.get(DomainObject.SELECT_TYPE);
            DocList.add(mapObjectMap);
        }
        return DocList;
    }

    /**
    * This Method has been written to dispaly the images
    * Depending upon the conditions
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */

public Vector getImageObject(Context context,String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Vector returnData = new Vector();
        
        // Constants definition
        String typePurgedRecord      = PropertyUtil.getSchemaProperty(context, "type_PurgedRecord");
        String typeRetentionRecord   = PropertyUtil.getSchemaProperty(context, "type_RetentionRecord");
        String typeRetentionHold     = PropertyUtil.getSchemaProperty(context, "type_RetentionHold");
        String sattrRetentionDate    = (String)PropertyUtil.getSchemaProperty(context, "attribute_RetentionDate");
        String strLanguage           = context.getSession().getLanguage();
        String strPropertyFile       = "emxLibraryCentralStringResource";
        String sConnectedToHolds= EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Title.RetentionHolds");
        String sConnectedToRecords = EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Title.ConnectedToRetentionRecords");
        String sConnectedMoreRecords = EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Title.ConnectedToMoreRetentionRecords");
        String sDatePassed = EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Title.RetentionDatePassed");
        String sPurgedTitle = EnoviaResourceBundle.getProperty(context,strPropertyFile,new Locale(strLanguage),"emxLibraryCentral.Title.PurgedRecords");
        String relRetainedRecord     = PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");

        
        String[] objectIds = new String[objectList.size()];
        Iterator itr = objectList.iterator();
        int i=0;
        while(itr.hasNext()){
            objectIds[i++] = (String)((Map)itr.next()).get(DomainObject.SELECT_ID);
        }
        
        StringList resultSelects = new StringList();
        String selectKey_Type  = "type";
        String selectKey_RType = "to["+relRetainedRecord+"].from.type";
        String selectKey_RDate = "to["+relRetainedRecord+"].attribute["+sattrRetentionDate+"].value";
        
        
        resultSelects.add(selectKey_Type);
        resultSelects.add(selectKey_RType);
        resultSelects.add(selectKey_RDate);
        
   
        MULTI_VALUE_LIST.add(selectKey_RType);
        MULTI_VALUE_LIST.add(selectKey_RDate);
        
        MapList results = DomainObject.getInfo(context, objectIds, resultSelects);
        
        MULTI_VALUE_LIST.remove(selectKey_RType);
        MULTI_VALUE_LIST.remove(selectKey_RDate);
        
        itr = results.iterator();
        while(itr.hasNext()){
            Map result = (Map)itr.next();
            boolean purged = false;
            boolean datePassedNotpurged = true;
            boolean retentionHold = false;
            int noOfRetentionRecords = 0;
            String sRetentionDate = "";
            if(result.get(selectKey_Type).equals(typePurgedRecord)){
                purged = true;
            }else{
                StringList retentionTypes = (StringList)result.get(selectKey_RType);
                StringList retentionDates = (StringList)result.get(selectKey_RDate);
        
                if(retentionTypes !=null && retentionDates != null){
                    Iterator retTypeIterator = retentionTypes.iterator();
                    Iterator retDateIterator = retentionDates.iterator();
                    
                    while(retTypeIterator.hasNext() && retDateIterator.hasNext()){
                        String retType = (String)retTypeIterator.next();
                        String retDate = (String)retDateIterator.next();
                        if(retType.equals(typeRetentionHold)){
                            retentionHold = true;
                        }else if(retType.equals(typeRetentionRecord)){
                            noOfRetentionRecords++;
                            if(!UIUtil.isNullOrEmpty(retDate)){
                                sRetentionDate = retDate;
                                Date retentionDate = eMatrixDateFormat.getJavaDate(retDate);
                                boolean retentionDatePassed = (new Date()).after(retentionDate);
                                if(!retentionDatePassed){
                                    datePassedNotpurged = false;
                                }
                            }
                            else
                            	datePassedNotpurged = false;
                        }
                    }
                }
            }
            
            StringBuffer imageTag = new StringBuffer("");
            if(purged){
                imageTag.append("<img border=\"0\" src=\"../documentcentral/images/compiconDelete.gif\" name=\"red\" id=\"red\" alt=\" Purged Record\" title=\""+XSSUtil.encodeForHTML(context, sPurgedTitle)+"\"></img>");
            }else if( noOfRetentionRecords > 0 || retentionHold){
                if(datePassedNotpurged && noOfRetentionRecords > 0){
                    imageTag.append("<img border=\"0\" src=\"../common/images/iconStatusRetentionPassedNotPurged.gif\" name=\"red\" id=\"red\" alt='"+XSSUtil.encodeForHTML(context,sRetentionDate)+"' title=\""+XSSUtil.encodeForHTML(context,sDatePassed)+"\"></img>");
                }else if(noOfRetentionRecords == 1){
                    imageTag.append("<img border=\"0\" src=\"../common/images/iconStatusRetentionSet.gif\" name=\"morethanone\" id=\"morethanone\" alt=\"more than one\" title=\""+XSSUtil.encodeForHTML(context,sRetentionDate)+"\"></img>");
                }else if(noOfRetentionRecords > 1){
                    imageTag.append("<img border=\"0\" src=\"../common/images/iconStatusRetentionScheduleDocument.gif\" name=\"morethanone\" id=\"morethanone\" alt=\"more than one\" title=\""+XSSUtil.encodeForHTML(context,sConnectedMoreRecords)+"\"></img>");
                }
                
                if(retentionHold){
                    imageTag.append("<img border=\"0\" src=\"../common/images/iconStatusObjectHold.gif\" name=\"morethanone\" id=\"morethanone\" alt=\"more than one\" title=\""+XSSUtil.encodeForHTML(context,sConnectedToHolds)+"\"></img>");
                }
            }
            
            returnData.add(imageTag.toString());
            
        }
        
        
        return returnData;
    }





    /**
    * This Method has been written to Purge The Selected Documents
    * Those satisfying the condition for Purge
    * @param context the eMatrix <code>Context</code> object
    * @param args holds The ObjectId
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */
 public static void doPurge(Context context, String []args) throws Exception
    {
        // check for user roles to purge -start
	    String ObjectId         = args[0];

	    DomainObject doObj = new DomainObject(ObjectId);
	    Access boAccess    = doObj.getAccessMask(context);

	    // for purging user should have ChangeType, ChangeName, ChangePolicy accesses
	    if(!(boAccess.hasChangeTypeAccess() && boAccess.hasChangeNameAccess() && boAccess.hasChangePolicyAccess())){
            String errMessage = EnoviaResourceBundle.getProperty(context, "emxLibraryCentralStringResource", new Locale(context.getLocale().getLanguage()),"emxLibraryCentral.Purge.NoPurgeAccess");
            throw new MatrixException(errMessage);
        }

        Vector vecObjectIds = new Vector();
        String strName="";
        String strNameafterPurge="";
        String strPurgeFormat="";
        String sContextUser = context.getUser();

        String retainedRel=PropertyUtil.getSchemaProperty(context,"relationship_RetainedRecord");
        String strActiveVersionRel=PropertyUtil.getSchemaProperty(context,"relationship_ActiveVersion");
        String strLatestVersionRel=PropertyUtil.getSchemaProperty(context,"relationship_LatestVersion");
        String strObjectRouteRel=PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
        String strAttrTitle=PropertyUtil.getSchemaProperty(context,"attribute_Title");
        String initiationdateattr=PropertyUtil.getSchemaProperty(context,"attribute_InitiationDate");
        String purgedateattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedDate");
        String purgetypeattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedType");
        String purgepolicyattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedPolicy");
        String purgeformatattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedFormat");
        String purgestateattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedState");
        String purgeownerattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedOwner");
        String purgetitleattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedTitle");
        String purgedescriptionattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedDescription");
        String purgerevisionattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedRevision");
        String purgenameattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedName");
        String policyRetentionRecord = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");

        String ScheduledStateRecord  = FrameworkUtil.lookupStateName(context, policyRetentionRecord, "state_Scheduled");
        String PurgedStateRecord  = FrameworkUtil.lookupStateName(context, policyRetentionRecord, "state_Purged");
        String strType=PropertyUtil.getSchemaProperty(context,"type_PurgedRecord");
        String strRecordType = PropertyUtil.getSchemaProperty(context,"type_RetentionRecord");

        String strPolicy=PropertyUtil.getSchemaProperty(context,"policy_PurgedRecord");

        /*For Formatting Current Date */
        String strFormat = com.matrixone.apps.domain.util.eMatrixDateFormat.getEMatrixDateFormat();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(strFormat);
        Date date = new Date();
        String sCurrentDate     = sdf.format(date);
        String strPurgeTitle    = "";
        String strPurgeRev      = "";
        String strPurgeName     = "";
        String strPurgeType     = "";
        String strPurgePolicy   = "";
        String strPurgeState    = "";
        String strPurgeDesc     = "";
        String strPurgeOwner    = "";
        String strFileName      = "";
        String arguments[]      = new String[2];


        strName             = doObj.getInfo(context,DomainObject.SELECT_NAME);

        strNameafterPurge   = "Purge"+strName;
        strPurgeName        = doObj.getInfo(context,DomainObject.SELECT_NAME);
        strPurgeType        = doObj.getInfo(context,DomainObject.SELECT_TYPE);
        strPurgePolicy      = doObj.getInfo(context,DomainObject.SELECT_POLICY);
        strPurgeState       = doObj.getInfo(context,DomainObject.SELECT_CURRENT);
        strPurgeDesc        = doObj.getInfo(context,DomainObject.SELECT_DESCRIPTION);
        strPurgeOwner       = doObj.getInfo(context,DomainObject.SELECT_OWNER);
        strPurgeRev         = doObj.getInfo(context,DomainObject.SELECT_REVISION);
        String strCommand   = "print bus $1 select $2 dump $3";
        strPurgeTitle       = MqlUtil.mqlCommand(context, strCommand, ObjectId, "attribute[" + strAttrTitle + "]", "|");


        String strCommandFor    = "print bus $1 select format dump $2";
        strPurgeFormat          = MqlUtil.mqlCommand(context, strCommandFor, ObjectId, "|");

        if(strName != null)
            vecObjectIds.addElement(ObjectId);

        Map attrMap = new Hashtable();


        String strLanguage = context.getSession().getLanguage();


        String retainRelationship=EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.RetainRelationship");
        String retainHistory=EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.RetainHistory");
        String fileCopy=EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.Record.FileCopy");
        //Checking Condition for Retain Relationship


        if(retainRelationship.equalsIgnoreCase("FALSE"))
        {
            String strCommandRelName    = "print bus $1 select relationship dump $2";
            String strRelName           = MqlUtil.mqlCommand(context, strCommandRelName, ObjectId, "|");

            StringTokenizer stkrelname = new StringTokenizer(strRelName,"|");
            StringList strRel=new StringList();
            SelectList relationshipSelects=new SelectList();
            relationshipSelects.add(DomainConstants.SELECT_ID);
            while(stkrelname.hasMoreTokens())
            {
                String strRelTemp = (String)stkrelname.nextToken();
                if(!(strRelTemp.equals(retainedRel) || strRelTemp.equals(strLatestVersionRel) || strRelTemp.equals(strActiveVersionRel) || strRelTemp.equals(strObjectRouteRel) ))
                {
                    if(!strRel.contains(strRelTemp))
                        strRel.addElement(strRelTemp);
                }

                Iterator itrRel=strRel.iterator();
                while (itrRel.hasNext())
                {
                    String sRelName = itrRel.next().toString();

                    MapList mapConnectedObjList= doObj.getRelatedObjects(context,
                                                                            sRelName,
                                                                            "*",
                                                                            null,
                                                                            relationshipSelects,
                                                                            true,
                                                                            true,
                                                                            (short) 1,
                                                                            null,
                                                                            null);


                    Iterator itr22=mapConnectedObjList.iterator();
                    while(itr22.hasNext())
                    {
                        Map mp1 = (Map) itr22.next();
                        String selectrelId = (String)mp1.get(DomainConstants.SELECT_ID);

                        DomainRelationship domRel1= new DomainRelationship(selectrelId);
                        domRel1.disconnect(context,selectrelId);
                    }

                }

            }
        }//End of if for checking Retain the relationship condition


        //Condition for Checking History to be Retained or not
        if(retainHistory.equalsIgnoreCase("FALSE")){
            try{
                ContextUtil.pushContext(context);
                String strCommandHistory    = "modify bus $1 delete history before $2";
                MqlUtil.mqlCommand(context, strCommandHistory, ObjectId, sCurrentDate);
                ContextUtil.popContext(context);
            }catch (Exception e){
                ContextUtil.popContext(context);
                throw e;
            }

        }
        //End of if Condition for Checking History to be Retained or not

        arguments[0]= ObjectId ;
        StringTokenizer stkformat = new StringTokenizer(strPurgeFormat,"|");
        matrix.db.FileList flist=new matrix.db.FileList();
        CommonDocument commonDocument = new CommonDocument(doObj);
        while(stkformat.hasMoreTokens())
        {
            String format = (String)stkformat.nextToken();
            flist = doObj.getFiles(context,format);

        if(flist.size()>0)
        {
            matrix.db.FileItr fitr= new FileItr(flist);
            while ( fitr.next() )
            {
                strFileName = fitr.obj().getName();
                arguments[1]= strFileName;
                 if("TRUE".equalsIgnoreCase(fileCopy))
                 {
                     emxCommonDocument_mxJPO.purgePreviousVersions(context,arguments);
                 }else{
                	 commonDocument.deleteFile(context, strFileName, format);
                 }

            }

        }
        }
        String cmd = "temp query bus $1 $2 $3";

        String output = MqlUtil.mqlCommand(context, cmd, strType, strName+"*" ,strPurgeRev);
        StringList stList = FrameworkUtil.split(output, "\n");
        String strDocid = "";

        if(stList.size()!=0)
        {
            Iterator itr = stList.iterator();

            while (itr.hasNext())
            {

                String strNameafter = strName+"("+ stList.size() +")";
                String strCommand2  = "modify bus $1 name $2";
                String strResult    = MqlUtil.mqlCommand(context, strCommand2, ObjectId, strNameafter);

                String strCommand1  = "modify bus $1 type $2 policy $3 select $4 dump $5" ;
                strDocid            = MqlUtil.mqlCommand(context, strCommand1, ObjectId, strType, strPolicy, "id" , "|");
                itr.next();

            }
        }
        else
        {
            String strCommand1  = "modify bus $1 type $2 policy $3 select $4 dump $5" ;
            strDocid            = MqlUtil.mqlCommand(context, strCommand1, ObjectId, strType, strPolicy, "id" , "|");
        }

        DomainObject obj1=new DomainObject(strDocid);
        attrMap.put(purgedateattr,sCurrentDate.toString());
        attrMap.put(purgetypeattr,strPurgeType);
        attrMap.put(purgenameattr,strPurgeName);
        attrMap.put(purgeformatattr,strPurgeFormat);
        attrMap.put(purgepolicyattr,strPurgePolicy);
        attrMap.put(purgestateattr,strPurgeState);
        attrMap.put(purgeownerattr,strPurgeOwner);
        attrMap.put(purgetitleattr,strPurgeTitle);
        attrMap.put(purgedescriptionattr,strPurgeDesc);
        attrMap.put(purgerevisionattr,strPurgeRev);

        obj1.setAttributeValues(context, attrMap);

        obj1.setOwner(context,sContextUser);


        /*  Below code is commented to avoid automatic promotion of Retention Record to Purged State */
        /*
        MapList retRecords = new MapList();

        SelectList selectStmts = new SelectList();
        selectStmts.addElement(DomainObject.SELECT_ID);
        selectStmts.addElement("from["+retainedRel+"].to[|type != '"+strType+"'].id");

        retRecords = doObj.getRelatedObjects(context,
                     retainedRel,
                     strRecordType,
                     selectStmts,
                     null,
                     true,
                     false,
                     (short)0,
                     null,
                     null,
                     0);

        if(retRecords.size() > 0)
        {
            Iterator itr = retRecords.iterator();
            while(itr.hasNext()){
                Map retRecord = (Map)itr.next();
                Set retRecordKeySet = retRecord.keySet();
                boolean canBePurged = true;
                Iterator keyItr = retRecordKeySet.iterator();
                while(keyItr.hasNext()){
                    String retRecordKey = (String)keyItr.next();
                    if(retRecordKey.startsWith("from["+retainedRel+"].to[")){
                        canBePurged = false;
                        break;
                    }
                }
                if(canBePurged){
                    String retRecordId = (String)retRecord.get(DomainConstants.SELECT_ID);
                    DomainObject retRecordIdObj=new DomainObject(retRecordId);

                    retRecordIdObj.setState(context,PurgedStateRecord);
                }
            }
        }
        */
    }

    /**
    * This Method has been written to serch the selected type
    * Those satisfying the condition
    * @param context the eMatrix <code>Context</code> object
    * @param args holds The ObjectId
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */
    public MapList getExistingDocuments(Context context, String[] args)
            throws Exception
    {
        HashMap programMap      = (HashMap) JPO.unpackArgs(args);
        String whereExpression  = "",select="";
        String txtRev           = "";
        String Description      = "";
        String DisplayType      = (String) programMap.get("attribute_Type");
        String txtName          = (String) programMap.get("txtName");
        String txtOwner         = (String) programMap.get("txtOwner");
        String relname          = (String) PropertyUtil.getSchemaProperty(context, "relationship_RetainedRecord");
        String policyVersion    = (String) PropertyUtil.getSchemaProperty(context, "policy_Version");
        String sRecordType      = PropertyUtil.getSchemaProperty(context, "type_RetentionRecord");
        String sHoldType        = PropertyUtil.getSchemaProperty(context, "type_RetentionHold");
        String policy           = PropertyUtil.getSchemaProperty(context, "policy_RetentionRecord");
        String stateActive      = FrameworkUtil.lookupStateName(context, policy, "state_Active");
        String queryLimit       = (String) programMap.get("QueryLimit");
        String languageStr      = (String)programMap.get("languageStr");
        String ObjectId         =(String) programMap.get("objectId");
        boolean bQueryLimit     = false;
        int intLimit            = 0;
        if(DisplayType.equals(sRecordType) || DisplayType.equals(sHoldType))
        {
            txtRev="*";
            Description  = (String) programMap.get("attribute_Description");
            select = "relationship[" + relname + "].from.id";
            if(Description.equals("*") || Description.equals("null")  ||  Description.equals(""))
            Description="*";
            whereExpression="current=="+stateActive+"&&(!(from["+relname+"].to.id =="+ObjectId+"))";
        }
        StringList objectSelects=new StringList(SELECT_ID);
        objectSelects.add(SELECT_DESCRIPTION);
        MapList recordList=new MapList();
        MapList objectList=new MapList();
        String objectID="";
        DomainObject doj=new DomainObject(ObjectId);
        queryLimit = UIUtil.isNullOrEmpty(queryLimit)?"0":queryLimit;
        Integer integerLimit = new Integer(queryLimit);
        intLimit = integerLimit.intValue();

        objectList=DomainObject.findObjects(context,
                DisplayType,
                txtName,
                txtRev,
                txtOwner,
                "*",
                whereExpression,
                null,
                false,
                objectSelects,
                (short)intLimit,
                LibraryCentralConstants.QUERY_WILDCARD,
                null);

        if(intLimit!= 0 && objectList.size() == intLimit){

            StringBuffer sbObjLimitWarning = new StringBuffer();
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource", languageStr,"emxDocumentCentral.Message.ObjectFindLimit"));
            sbObjLimitWarning.append(" ("+queryLimit+") ");
            sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource", languageStr,"emxDocumentCentral.Message.ObjectFindLimitReached"));

            emxContextUtil_mxJPO.mqlWarning(context,sbObjLimitWarning.toString());
        }

        return objectList;

    }//end of getExistingDocuments function

    /**
    * This Method has been written to display Retention Date or Purge Date
    * Retention Date will be displayed for Documents other than Purged Record
    * And Purge Date for Purged Records.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds The ObjectId
    * @throws Exception if the operation fails
    * @since LC 10.7SP1
    */
    public Vector displayDate(Context context,String[] args)
            throws Exception
    {
        Vector dispDate=new Vector();
        String retainedRel=PropertyUtil.getSchemaProperty(context,"relationship_RetainedRecord");
        String retentiondateattr=PropertyUtil.getSchemaProperty(context,"attribute_RetentionDate");
        String PurgeDoctype=(String)PropertyUtil.getSchemaProperty(context,"type_PurgedRecord");

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");

        Iterator listItr=objectList.iterator();

        while( listItr.hasNext())
        {
            Map ObjectMap = (Map) listItr.next();
            String strId = (String) ObjectMap.get(DomainObject.SELECT_ID);

            DomainObject dom=DomainObject.newInstance(context,strId);

            String purgedateattr=PropertyUtil.getSchemaProperty(context,"attribute_PurgedDate");
            String sType=dom.getInfo(context,SELECT_TYPE);

            if(sType.equals(PurgeDoctype)){
                String sPurgeDate = dom.getAttributeValue(context,purgedateattr);
                dispDate.add(sPurgeDate);
            }

            else{
                String sRelId = ObjectMap.get(DomainRelationship.SELECT_ID).toString();
                String  sRetenDate=DomainRelationship.getAttributeValue(context, sRelId, retentiondateattr);
                dispDate.add(sRetenDate);
            }
        }

        return dispDate;
    } //end of displayDate function

    /**
    * Method to get the file names.
    * Used in File Summary, Version Summary Page.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds a packed HashMap of the following entries:
    * objectList- a MapList of objects information.
    * paramList- a HashMap of parameter values including reportFormat, relId, and trackUsagePartId
    * @returns Vector of File Names
    * @throws Exception if the operation fails
    *
    * @since Common 11.0
    */
  public Vector getFileName(Context context, String[] args)
            throws Exception
    {
        Vector fileRevisionVector = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        String ObjId ="";

           for(int i=0; i<objectList.size(); i++)
        {
            Map objectMap = (Map) objectList.get(i);
            ObjId = (String) objectMap.get(DomainConstants.SELECT_ID);



        Map paramList = (Map)programMap.get("paramList");
        String reportFormat = (String) paramList.get("reportFormat");

        String strDocumentPartRel = (String)paramList.get("relId");
        String strPartId = (String)paramList.get("trackUsagePartId");

        if(strPartId == null && strDocumentPartRel != null)
        {
            String[] relIds = {strDocumentPartRel};
            StringList slRelSelect = new StringList("from.id");
            MapList mlPart = DomainRelationship.getInfo(context, relIds, slRelSelect);

            if(mlPart.size()>0)
            {
                strPartId = (String) ((Map)mlPart.get(0)).get("from.id");

            }
        }


        String strLink = "<a href=\"JavaScript:emxTableColumnLinkClick(\'../common/emxTable.jsp?program=emxCommonFileUI:getFileVersions&amp;table=APPFileVersions&amp;disableSorting=true&amp;header=emxComponents.Common.DocumentVersionsPageHeading&amp;subHeader=emxComponents.Menu.SubHeaderDocuments&amp;suiteKey=Components&amp;HelpMarker=emxhelpdocumentfileversions&amp;objectId="+XSSUtil.encodeForURL(context, ObjId)+"&amp;FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1";

        String strFileName="";

        DomainObject doObj = new DomainObject(ObjId);

        String strCommandFor    = "print bus $1 select format dump $2";
        String  strPurgeFormat=MqlUtil.mqlCommand(context, strCommandFor, ObjId, "|");
        StringTokenizer stkformat = new StringTokenizer(strPurgeFormat,"|");

        matrix.db.FileList flist=new matrix.db.FileList();
        while(stkformat.hasMoreTokens())
        {
            String strTemp = (String)stkformat.nextToken();
            flist = doObj.getFiles(context,strTemp);
        }
        matrix.db.FileItr fitr= new FileItr(flist);

        while ( fitr.next() )
        {
            strFileName = fitr.obj().getName();
        }

            String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "type", (String)objectMap.get(DomainConstants.SELECT_TYPE), true);
            String typeIcon = "iconSmallPaperclipVertical.gif";
            String defaultTypeIcon = "<img src=\"../common/images/"+typeIcon+"\" border=\'0\' align=\'top\'></img>";


            if(!(strFileName==null || strFileName.equals("")))
            {
                String sLink = strLink + "&amp;parentOID="+XSSUtil.encodeForURL(context,(String)objectMap.get("masterId"))+"&amp;objectId="+XSSUtil.encodeForURL(context,(String)objectMap.get(DomainConstants.SELECT_ID))+"&amp;AppendParameters=true&amp;trackUsagePartId="+XSSUtil.encodeForURL(context,strPartId)+"', '', '', 'false', 'popup', '')\">";
                String strURL = sLink + defaultTypeIcon + "</a>";
                fileRevisionVector.add("<nobr>"+strURL+"</nobr>");
            }
            else
            {
                fileRevisionVector.add("");

            }
        }
    return fileRevisionVector;
    }


  /**
    * Method to get the Default Retention period.
    * Used in Record Retention Summary.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds a packed HashMap of the following entries:
    * objectList- a MapList of objects information.
    * paramList- a HashMap of parameter values including reportFormat, relId, and trackUsagePartId
    * @returns Vector of File Names
    * @throws Exception if the operation fails
    *
    * @since R210
  */
    public Vector getDefaultRetionPeriod (Context context, String[] args)
    throws Exception
    {
        HashMap programMap          = (HashMap)JPO.unpackArgs(args);
        HashMap paramList           = (HashMap) programMap.get("paramList");
        MapList objectList          = (MapList)programMap.get("objectList");
        String languageStr          = (String)paramList.get("languageStr");
        String sRetentionPeriod     = "";
        Vector columnVals           = new Vector(objectList.size());
        Map mRetentionData          = null;
        DomainObject dRetentionObj  = null;
        StringList slAttrSelect     = new StringList();
        slAttrSelect.add("attribute["+PropertyUtil.getSchemaProperty(context, "attribute_DefaultRetentionPeriod")+"]");

        for(int i = 0; i < objectList.size(); i++) {
            Map objectMap   = (Map) objectList.get(i);
            dRetentionObj   = new DomainObject((String) objectMap.get(DomainConstants.SELECT_ID));
            sRetentionPeriod= "";
            mRetentionData  = (Map)dRetentionObj.getInfo(context,slAttrSelect);

            if(((String)mRetentionData.get(DomainConstants.SELECT_TYPE)).equalsIgnoreCase(PropertyUtil.getSchemaProperty(context, "type_RetentionRecord"))) {
                sRetentionPeriod    = (String)mRetentionData.get(slAttrSelect.elementAt(0));
            }
            columnVals.addElement(sRetentionPeriod);
        }
        return columnVals;
    }

      /**
    /**
    * This Method has been written to display Retention Period
    * Only for Objects of type Retention Record
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds The ObjectId
    * @throws Exception if the operation fails
    *
    * @since R210
    */
    public boolean isRetentionRecordType(Context context,String[] args)
    throws Exception
    {
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        boolean bAccess     = false;

        if(((String)programMap.get("attribute_Type")).equalsIgnoreCase(PropertyUtil.getSchemaProperty(context, "type_RetentionRecord"))) {
            bAccess= true;
        }
        return bAccess;
    }

    /**
     * Method to get the Document Title For Documents and Purged Documents.
     * Used in Reatined Documents.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a packed HashMap of the following entries:
     * objectList- a MapList of objects information.
     * paramList- a HashMap of parameter values including reportFormat, relId, and trackUsagePartId
     * @returns Vector of File Names
     * @throws Exception if the operation fails
     *
     * @since R210
   */
     public Vector getDocumentTitle(Context context, String[] args)
     throws Exception
     {
         HashMap programMap          = (HashMap)JPO.unpackArgs(args);
         HashMap paramList           = (HashMap) programMap.get("paramList");
         MapList objectList          = (MapList)programMap.get("objectList");
         String languageStr          = (String)paramList.get("languageStr");
         String sTitle               = "";
         Vector columnVals           = new Vector(objectList.size());
         Map    mRetentionData       = null;
         DomainObject dRetentionObj  = null;
         StringList slAttrSelect     = new StringList();
         slAttrSelect.add("attribute["+PropertyUtil.getSchemaProperty(context, "attribute_Title")+"]");
         slAttrSelect.add("attribute["+PropertyUtil.getSchemaProperty(context, "attribute_PurgedTitle")+"]");

         for(int i = 0; i < objectList.size(); i++) {
             Map objectMap    = (Map) objectList.get(i);
             dRetentionObj    = new DomainObject((String) objectMap.get(DomainConstants.SELECT_ID));
             sTitle           = "";
              mRetentionData  = (Map)dRetentionObj.getInfo(context,slAttrSelect);
              if(((String)mRetentionData.get(DomainConstants.SELECT_TYPE)).equalsIgnoreCase(PropertyUtil.getSchemaProperty(context, "type_PurgedRecord"))) {
                 sTitle    = (String)mRetentionData.get(slAttrSelect.elementAt(1));
             }
             else {
                sTitle    = (String)mRetentionData.get(slAttrSelect.elementAt(0));
             }
             columnVals.addElement(sTitle);
         }
         return columnVals;
     }

     /**
      * this method returns the 'Purged State' attribute of type 'Purged Record' in I18n Format
      * @param context the eMatrix <code>Context</code> object
      * @param args holds a packed HashMap of the following entries
      *        paramMap - HashMap containing objectid
      * @return 'Purged State' attribute in I18n Format
      * @throws Exception
      */
     public static String getPurgedStateDisplay(Context context, String[] args) throws Exception{
         HashMap programMap          = (HashMap) JPO.unpackArgs(args);
         HashMap paramMap            = (HashMap) programMap.get("paramMap");
         String objectId             = (String)paramMap.get("objectId");
         DomainObject purgedRecord   = new DomainObject(objectId);
         String purgedPolicyAttr     = "attribute["+PropertyUtil.getSchemaProperty(context,"attribute_PurgedPolicy")+"].value";
         String purgedStateAttr      = "attribute["+PropertyUtil.getSchemaProperty(context,"attribute_PurgedState")+"].value";
         StringList purgedPolicyStateAttr = new StringList();
         purgedPolicyStateAttr.add(purgedPolicyAttr);
         purgedPolicyStateAttr.add(purgedStateAttr);

         Map purgedPolicyAndState    = purgedRecord.getInfo(context, purgedPolicyStateAttr);
         String purgedPolicy         = (String)purgedPolicyAndState.get(purgedPolicyAttr);
         String purgedState          = (String)purgedPolicyAndState.get(purgedStateAttr);
         String purgedStateDisplay   = purgedState;
         if(!(UIUtil.isNullOrEmpty(purgedPolicy) || UIUtil.isNullOrEmpty(purgedState))){
             purgedPolicy         = purgedPolicy.replaceAll(" ", "_");
             purgedStateDisplay   = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(context.getSession().getLanguage()),"emxFramework.State."+purgedPolicy+"."+purgedState);
         }
         return purgedStateDisplay;
     }

     /**
      * this method checks which are of type 'Purged Record' in a list of Objects
      *  - used in Retained Document List to disable Edit of Retention Period for Purged Records
      * @param context
      * @param args - packed objectList
      * @return StringList of 'true' or 'false' values,
      *         false for 'Purged Record' type
      *         true  for other types
      * @throws Exception during failure
      */
     public StringList isNotPurgedRecord (Context context,String [] args) throws Exception {
         Map programMap                   = (Map)JPO.unpackArgs(args);
         MapList objList                  = (MapList)programMap.get("objectList");

         StringList rtnStr = new StringList();

         int objectsLength = objList.size();
         String[] objectIdArray = new String[objectsLength];
         for(int i=0;i<objectsLength;i++){
        	 Map objMap = (Map)objList.get(i);
        	 objectIdArray[i] = (String)objMap.get("id");
         }

         StringList objectSelects = new StringList(DomainConstants.SELECT_TYPE);
         objectSelects.add(DomainConstants.SELECT_ID);

         MapList objectTypeList = DomainObject.getInfo(context, objectIdArray, objectSelects);

         String type_PurgedRecord =(String)PropertyUtil.getSchemaProperty(context, "type_PurgedRecord");

         Iterator itr = objectTypeList.iterator();
         while(itr.hasNext()){
        	 Map objMap = (Map)itr.next();
        	 String type = (String)objMap.get("type");
        	 if(type!=null && type.equals(type_PurgedRecord)){
            	 rtnStr.add(new Boolean(false));
        	 }else{
            	 rtnStr.add(new Boolean(true));
        	 }
         }
         return rtnStr;
     }


}
