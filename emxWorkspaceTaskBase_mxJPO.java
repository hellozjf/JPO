/*
 *  emxWorkspaceTaskBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;

/**
 * @version 10-7-0-0
 */
public class emxWorkspaceTaskBase_mxJPO extends emxDomainObject_mxJPO
{

  private static final String strAttrBracket  = "attribute[";
  private static final String strCloseBracket = "]";

  private static final String sAttrTitle                     = PropertyUtil.getSchemaProperty("attribute_Title");
  private static final String policyTask                     = PropertyUtil.getSchemaProperty("policy_InboxTask");
  private static final String sAttrAllowDelegation           = PropertyUtil.getSchemaProperty("attribute_AllowDelegation");
  private static final String attrReviewTask                 = PropertyUtil.getSchemaProperty("attribute_ReviewTask");
  private static final String attrDueDateOffset              = PropertyUtil.getSchemaProperty("attribute_DueDateOffset");
  private static final String attrDueDateOffsetFrom          = PropertyUtil.getSchemaProperty("attribute_DateOffsetFrom");
 
  private StringBuffer sSelectAllowDelegation = new StringBuffer(strAttrBracket);
  private  StringBuffer sSelectReviewTask = new StringBuffer(strAttrBracket);
  private StringBuffer sDueDateOffset = new StringBuffer(strAttrBracket);
  private StringBuffer sDueDateOffsetFrom = new StringBuffer(strAttrBracket);
  private StringBuffer sRouteAction = new StringBuffer(strAttrBracket);
  private  StringBuffer sRouteInst = new StringBuffer(strAttrBracket);
  private StringBuffer SchCompletionDate = new StringBuffer(strAttrBracket);
  private StringBuffer ActCompletionDate = new StringBuffer(strAttrBracket);
  com.matrixone.apps.common.Person personObject = null;

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since 10-7-0-0 next
     * @grade 0
     */
    public emxWorkspaceTaskBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
      personObject = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
      sSelectAllowDelegation.append(sAttrAllowDelegation);
      sSelectAllowDelegation.append(strCloseBracket);
      sSelectReviewTask.append(attrReviewTask);
      sSelectReviewTask.append(strCloseBracket);
      sDueDateOffset.append(attrDueDateOffset);
      sDueDateOffset.append(strCloseBracket);
      sDueDateOffsetFrom.append(attrDueDateOffsetFrom);
      sDueDateOffsetFrom.append(strCloseBracket);
      sRouteAction.append(personObject.ATTRIBUTE_ROUTE_ACTION);
      sRouteAction.append(strCloseBracket);
      sRouteInst.append(personObject.ATTRIBUTE_ROUTE_INSTRUCTIONS);
      sRouteInst.append(strCloseBracket);
      SchCompletionDate.append(personObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
      SchCompletionDate.append(strCloseBracket);
      ActCompletionDate.append(personObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE);
      ActCompletionDate.append(strCloseBracket);


    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since 10-7-0-0
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

  /**
     * getWorkspaceTasks - gets the list of Tasks 
     * @param context the eMatrix <code>Context</code> object
     * @param busWhere condition to query
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10.7-0-0
     * @grade 0
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public Object getWorkspaceTasks(Context context, String args[]) throws Exception
   {
        try
        {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
         String objectId = (String)programMap.get("objectId");
          String stateAssigned           = FrameworkUtil.lookupStateName(context,policyTask, "state_Assigned");
		  //<--!Modified for the Bug No:340003 08/27/2007 1:00PM Start--> 
          String sTypeWhere ="from["+personObject.RELATIONSHIP_ROUTE_TASK+"].to.to["+personObject.RELATIONSHIP_ROUTE_SCOPE+"].from.id== " + objectId+" && current == '"+stateAssigned+"'";
		  //<--!Modified for the Bug No:340003 08/27/2007 1:00PM End--> 
          StringList selectTypeStmts = new StringList();
          selectTypeStmts.add(personObject.SELECT_NAME);
          selectTypeStmts.add(personObject.SELECT_ID);
          selectTypeStmts.add(personObject.SELECT_TYPE);
          selectTypeStmts.add(personObject.SELECT_DESCRIPTION);
          selectTypeStmts.add(personObject.SELECT_CURRENT);
          selectTypeStmts.add("from[" + personObject.RELATIONSHIP_ROUTE_TASK + "].to.id");
          selectTypeStmts.add("from[" + personObject.RELATIONSHIP_ROUTE_TASK + "].to.name");
          selectTypeStmts.add(strAttrBracket + sAttrTitle + strCloseBracket);
          selectTypeStmts.add(SchCompletionDate.toString());
          selectTypeStmts.add(ActCompletionDate.toString());
          selectTypeStmts.add(sRouteAction.toString());
          selectTypeStmts.add(sSelectAllowDelegation.toString());
          selectTypeStmts.add(sRouteInst.toString());
          selectTypeStmts.add(sSelectReviewTask.toString());
          selectTypeStmts.add(sDueDateOffset.toString());
          selectTypeStmts.add(sDueDateOffsetFrom.toString());
          ContextUtil.startTransaction(context,false);
          ExpansionIterator expIter =  personObject.getPerson(context).getExpansionIterator(context,personObject.RELATIONSHIP_PROJECT_TASK,
                                                                                  personObject.TYPE_INBOX_TASK,
                                                                              selectTypeStmts,
                                                                              new StringList(),
                                                                              true,
                                                                              false,
                                                                              (short)1,
                                                                              sTypeWhere,
                                                                              null,
                                                                              (short)0,
                                                                              false,
                                                                              false,
                                                                              (short)100,
                                                                              false);
          MapList taskMapList = null;
          try {
              taskMapList = FrameworkUtil.toMapList(expIter,(short)0,null,null,null,null);
          } finally {
              expIter.close();
          }
          ContextUtil.commitTransaction(context);
          
        return taskMapList;
        }

        catch (Exception ex)
        {
            throw ex;
        }
   }
        
  /**
     * showRoute - Displays the Route name this Task belongs
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since 10-7-0-0
     * @grade 0
     */
    public Vector showRoute(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            
            Map objectMap = null;
            Vector showRoute = new Vector();
            String sRouteString = "";
            String sRouteId = "";
            String sRouteName ="";
            String sRouteNextUrl = "";
            String sRouteUrl  = "";
            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            for(int i=0; i< objectListSize; i++)
            {
             sRouteString = "";
              try{
                objectMap = (HashMap) objectList.get(i);
              }catch(ClassCastException cce){
                objectMap = (Hashtable) objectList.get(i);
              }
              sRouteId                = (String)objectMap.get("from[" + personObject.RELATIONSHIP_ROUTE_TASK + "].to.id");
              sRouteName              = (String)objectMap.get("from[" + personObject.RELATIONSHIP_ROUTE_TASK + "].to.name");
              sRouteNextUrl = "./emxTree.jsp?objectId=" + XSSUtil.encodeForJavaScript(context, sRouteId);
              sRouteUrl  = "javascript:showModalDialog('" + sRouteNextUrl + "',800,575)";
              sRouteString = "<a href="+sRouteUrl+">"+XSSUtil.encodeForHTML(context, sRouteName)+"</a>&nbsp;";
              showRoute.add(sRouteString);
            }
            return showRoute;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
  /**
     * showAllowDelegation - Displays the allowdelegation ICON 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since 10-7-0-0
     * @grade 0
     */
    public Vector showAllowDelegation(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Map objectMap = null;
            Vector showAllowDelegation = new Vector();
            String statusImageString = "";
            String allowDelegation = "";
            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            for(int i=0; i< objectListSize; i++)
            {

              statusImageString = "";
              try{
                objectMap = (HashMap) objectList.get(i);
              }catch(ClassCastException cce){
                objectMap = (Hashtable) objectList.get(i);
              }
              allowDelegation =(String) objectMap.get(sSelectAllowDelegation.toString());
              if(allowDelegation.equals("TRUE"))
               statusImageString = "<img src=\"images/iconAssignee.gif\">";
               else
                statusImageString = "&nbsp;";
 
              showAllowDelegation.add(statusImageString);
            }
            //XSSOK
            return showAllowDelegation;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
 /**
     * showAllowDelegation - Displays the allowdelegation ICON 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since 10-7-0-0
     * @grade 0
     */
    public Vector showNeedsOwnerReview(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Map objectMap = null;
            Vector showNeedsOwnerReview = new Vector();
            String statusImageString = "";
            String NeedsReview = "";
            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            for(int i=0; i< objectListSize; i++)
            {

              statusImageString = "";
              try{
                objectMap = (HashMap) objectList.get(i);
              }catch(ClassCastException cce){
                objectMap = (Hashtable) objectList.get(i);
              }
               NeedsReview =(String) objectMap.get(sSelectReviewTask.toString());
              if(NeedsReview.equals("Yes"))
               statusImageString = "<img src=\"images/iconSmallOwnerReview.gif\" >";
               else
                statusImageString = "&nbsp;";
 
              showNeedsOwnerReview.add(statusImageString);
            }
            //XSSOK
            return showNeedsOwnerReview;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    public Vector showStatusGif(Context context,String[] args) throws Exception
    {
      try
        {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Map objectMap = null;
        Vector showStatusGif = new Vector();
        StringBuffer strbuff = new StringBuffer();
        String sState = "";
        String scheduledCompletionDate =null;
        String duedateOffset           =null;
        String ActualCompletionDate    = null;
        Date sAcutalCompletionDate;
        Date currentDate       = new Date();
        Date scheduledDate     = null;
        boolean bDueDateEmpty = false;
        boolean bDeltaDueDate = false;
        int objectListSize = 0 ;
        if(objectList != null)
        {
            objectListSize = objectList.size();
        }
        for(int i=0; i< objectListSize; i++)
        {
           try{
                objectMap = (HashMap) objectList.get(i);
              }catch(ClassCastException cce){
                objectMap = (Hashtable) objectList.get(i);
             }
           scheduledCompletionDate = (String)objectMap.get(SchCompletionDate.toString());
           if(scheduledCompletionDate != null && !scheduledCompletionDate.equals(""))
               scheduledDate           =  eMatrixDateFormat.getJavaDate(scheduledCompletionDate);
           duedateOffset           = (String)objectMap.get(sDueDateOffset.toString());
           ActualCompletionDate    = (String)objectMap.get(ActCompletionDate.toString());
           sState                  = (String)objectMap.get(DomainObject.SELECT_CURRENT);
          if(ActualCompletionDate != null && !ActualCompletionDate.equals("") && sState.equals("Complete"))
          {
             sAcutalCompletionDate = eMatrixDateFormat.getJavaDate(ActualCompletionDate);
          }
          else
          {
             sAcutalCompletionDate = currentDate;
          }
          if(scheduledCompletionDate == null || "".equals(scheduledCompletionDate) || "null".equals(scheduledCompletionDate)){
              bDueDateEmpty  = true;
          }
          if(duedateOffset != null && !"".equals(duedateOffset) && !"null".equals(duedateOffset) && bDueDateEmpty){
              bDeltaDueDate = true;
           }
           if(bDueDateEmpty || bDeltaDueDate ){
             strbuff.append("&nbsp;");
           }else if (scheduledDate != null && sAcutalCompletionDate != null && sAcutalCompletionDate.after(scheduledDate) ) {
              strbuff.append("<img src=\"images/iconStatusRed.gif\" name=\"imgYellow\" id=\"imgYellow\" alt=\"Red\" />");
           } else {
              strbuff.append("<img src=\"images/iconStatusGreen.gif\" name=\"imageRed\" id=\"imageRed\" alt=\"Green\" />");
           }
        showStatusGif.add(strbuff.toString());
        strbuff = null;
        strbuff = new StringBuffer();
      }
    //XSSOK
     return showStatusGif;
  }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * showTaskName - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since 10-7-0-0
     * @grade 0
     */
    public Vector showTaskName(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            DomainObject taskObject = new DomainObject();
            boolean blnToDisplayTaskName = true;
            Vector vecShowTaskName  = new Vector();
            String taskId = "";
            String name= "";
            String sTaskTitle  ="";
            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                blnToDisplayTaskName=true;
                Map objectMap = (Map) objectListItr.next();
                sTaskTitle  = (String)objectMap.get(strAttrBracket + sAttrTitle + strCloseBracket);
                if(!sTaskTitle.equals("") && sTaskTitle!= null)
                    vecShowTaskName.add(sTaskTitle);
                else 
                {
                    taskId = (String)objectMap.get(taskObject.SELECT_ID);
                    taskObject.setId(taskId);
                    name=taskObject.getInfo(context,"name");
                    vecShowTaskName.add(name);
                }
            }
            return vecShowTaskName;
        }
        catch (Exception ex)
        {
            System.out.println("Error in showTaskName= " + ex.getMessage());
            throw ex;
        }
    }
}
