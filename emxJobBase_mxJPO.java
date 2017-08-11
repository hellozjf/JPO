/*
**  emxJobBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne Inc.
**  Copyright notice is precautionary only  and does not evidence
**  any actual or intended publication of such program
**
*/

import java.util.Locale;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.Integer;

import matrix.db.Context;

import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainRelationship;

import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.Job;

import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.domain.util.FrameworkUtil;

/**
 * The <code>Job</code> class represents Job JPO in common
 * implements methods to access Job lists, and also triggers related to background jobs
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */

public class emxJobBase_mxJPO extends emxDomainObject_mxJPO
{

    public static final String attrBeg                 ="attribute[";
    public static final String attrEnd                 ="]";


    /**
     * Constructs a new JobBase JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF V11-0-0-0
     */
    public emxJobBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      // Call the super constructor
      super( context, args);
      if(args != null && args.length > 0)
      {
          setId(args[0]);
      }
    }

  /**
   * Returns the Background Jobs owned by the context user, and matching the
   * filter criteria
   *
   * @mx.whereUsed Invoked by the user from Background Job List view
   * @param context
   *            the eMatrix <code>Context</code> object
   * @param filter
   *            filter value chosen by the user
   * @throws FrameworkException
   *             if the operation fails
   * @since AEF V11-0-0-0
   * @mx.pseudocode Call the appropriate method based on the filter value<br>
   *                if filter = = 'All' call getMyBackgroundJobs(Context
   *                context)<br>
   *                else if 'Completed' call
   *                getMyCompletedBackgroundJobs(Context context)<br>
   *                else if 'Failed' call getMyFailedBackgroundJobs(Context
   *                context)<br>
   */
  public static MapList getBackgroundJobs(Context context, String filter)
    throws FrameworkException
  {
        String user                    =context.getUser();
        StringList objectSelects       =new StringList();
        String typeJob                 =PropertyUtil.getSchemaProperty(context,"type_Job");
        String attrCompletionStatus    =PropertyUtil.getSchemaProperty(context,"attribute_CompletionStatus");
        String attrAbortRequested      =PropertyUtil.getSchemaProperty(context,"attribute_AbortRequested");
        String attrNextStepCommand      =    PropertyUtil.getSchemaProperty(context,"attribute_NextStepCommand");
        String attrProgressPercent      =    PropertyUtil.getSchemaProperty(context,"attribute_ProgressPercent");
        String formatLog               =PropertyUtil.getSchemaProperty(context,"format_Log");

        MapList resultList          =new MapList();
        StringBuffer whereExp       =new StringBuffer();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_CURRENT);
        objectSelects.add(attrBeg+attrCompletionStatus+attrEnd);
        objectSelects.add(attrBeg+attrAbortRequested+attrEnd);
        objectSelects.add(attrBeg+attrNextStepCommand+attrEnd);
        objectSelects.add(attrBeg+attrProgressPercent+attrEnd);
        objectSelects.add("format["+formatLog+"].hasfile");

        if(filter.equalsIgnoreCase("All"))
        {
            //do nothing
        }
        else if(filter.equalsIgnoreCase("Archive"))
        {
            whereExp.append("current == Archived");
        }
        else if(filter.equalsIgnoreCase("Completed"))
        {
            whereExp.append("((");
            whereExp.append(attrBeg);
            whereExp.append(attrCompletionStatus);
            whereExp.append("]==Succeeded");
            whereExp.append(") && (");
            whereExp.append("current==" + Job.STATE_JOB_COMPLETED + "))");
        }
        else if(filter.equalsIgnoreCase("Failed"))
        {
            whereExp.append("((");
            whereExp.append(attrBeg);
            whereExp.append(attrCompletionStatus);
            whereExp.append("] matchlist \"Failed,Aborted\" \",\"");
            whereExp.append(") && (");
            whereExp.append("current==" + Job.STATE_JOB_COMPLETED + "))");
        }
        else if(filter.equalsIgnoreCase("Current"))
        {
            //Code modified by IXI-to exclude job which are aborted and Failed from the Current List 
        	whereExp.append("( ((");
            whereExp.append(attrBeg);
            whereExp.append(attrCompletionStatus);
            whereExp.append("]!='Failed') && ("+attrBeg+attrCompletionStatus+"]!='Aborted')");
            whereExp.append(") && (");
            whereExp.append("current!=" + Job.STATE_JOB_ARCHIVED +"&& current!=" + Job.STATE_JOB_COMPLETED +"))");
           
        
        	//whereExp.append("current!=" + Job.STATE_JOB_ARCHIVED + "");
        }
        try
        {
            resultList=    DomainObject.findObjects(context,
                                         typeJob,
                                         DomainConstants.QUERY_WILDCARD,
                                         DomainConstants.QUERY_WILDCARD,
                                         user,
                                         DomainConstants.QUERY_WILDCARD,
                                         whereExp.toString(),
                                         null,
                                         true,
                                         objectSelects,
                                         (short)0,
                                         null,
                                         null);

         }
         catch(Exception e)
         {
              throw new FrameworkException(e);
         }
         return resultList;

  }

  /**
   * Returns the all Background Jobs owned by the context user
   *
   * @mx.whereUsed Invoked by the user from Background Job List view
   * @param context
   *            the eMatrix <code>Context</code> object
   * @throws FrameworkException
   *             if the operation fails
   * @since AEF V11-0-0-0
   * @mx.pseudocode temp query 'Background Job' * * where 'owner = =
   *                context.user'
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getMyBackgroundJobs(Context context,String args[])
    throws FrameworkException
  {
       return getBackgroundJobs(context,"All");
  }

  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getMyCurrentBackgroundJobs(Context context,String args[])
    throws FrameworkException
  {
        return getBackgroundJobs(context,"Current");
  }

  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getMyArchivedBackgroundJobs(Context context,String args[])
    throws FrameworkException
  {
        return getBackgroundJobs(context,"Archive");
  }

  /**
   * Returns the all completed Background Jobs owned by the context user
   *
   * @mx.whereUsed Invoked by the user from Background Job List view
   * @param context
   *            the eMatrix <code>Context</code> object
   * @throws FrameworkException
   *             if the operation fails
   * @since AEF V11-0-0-0
   * @mx.pseudocode temp query 'Background Job' * * where "owner = =
   *                context.user && attribute[Completion Status] = =
   *                'Completed'
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getMyCompletedBackgroundJobs(Context context,String[] args)
    throws FrameworkException
  {
        return getBackgroundJobs(context,"Completed");
  }

  /**
   * Returns the all failed Background Jobs owned by the context user
   *
   * @mx.whereUsed Invoked by the user from Background Job List view
   * @param context the eMatrix <code>Context</code> object
   * @throws FrameworkException if the operation fails
   * @since AEF V11-0-0-0
   * @mx.pseudocode temp query bus 'Background Job' * * where "owner ==
   *                context.user && attribute[Completion Status] = = 'Failed'
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public static MapList getMyFailedBackgroundJobs(Context context,String[] args)
    throws FrameworkException
  {
        return getBackgroundJobs(context,"Failed");
  }

  /**
   * Starts a job running when it is promoted to Running.
   *
   * @mx.whereUsed invoked as a trigger when a Job object is promoted to
   *               Running. Calls job.start()
   * @param context
     * @since AEF V11-0-0-0
   * @param args
   */
  public static int startJobOnPromoteToRunning(Context context, String[] args)
    throws FrameworkException
  {
/*
    String jobId = PropertyUtil.getRPEValue (context, "OBJECTID", false);

    Job job = new Job ();
    job.setId (jobId);

    job.start (context);
*/
    return 0;
  }

  /**
   * Automatically promte a Job from Submitted to Running.
   *
   * @mx.whereUsed invoked as a trigger when a Job object is is created.
   *               Currently there is no concept of queueing; submitted jobs
   *               are immediately promoted to Running
   *
   * @param c
   * @param args
   */
  public static int autoPromoteSubmittedJobToRunning(Context context, String[] args)
    throws MatrixException
  {
    String jobId = PropertyUtil.getRPEValue (context, "OBJECTID", false);

        try
        {
      Job job = (Job)DomainObject.newInstance(context, jobId);
      job.start(context);
      return 0;
      }
    catch (Exception ex)
    {
      throw new MatrixException ();
    }
  }

    /**
     * Displays the "Status Icon" column based on the current state and "Completion Status" attribute
     * @mx.Used as a "programHTMLOutput" function for "Status Icon" column
     * @param context the ematrix Context
     * @param args
     * @return Vector containing the column values
     * @throws Exception
     */


    public Vector showStatusGif(Context context, String[] args) throws Exception
    {
        Map programMap                  =   (Map)JPO.unpackArgs(args);
        MapList objectList              =   (MapList)programMap.get("objectList");
        String completionStatus,abortRequested,current;
        Vector resultList               =   new Vector();
        String attrCompletionStatus    =    PropertyUtil.getSchemaProperty(context,"attribute_CompletionStatus");
        String attrAbortRequested      =    PropertyUtil.getSchemaProperty(context,"attribute_AbortRequested");

        for(Iterator itr=objectList.iterator();itr.hasNext();)
        {
            Map objectMap = (Map) itr.next();
            completionStatus     = (String)objectMap.get(attrBeg+attrCompletionStatus+attrEnd);
            current              = (String)objectMap.get(DomainConstants.SELECT_CURRENT);
            abortRequested       = (String)objectMap.get(attrBeg+attrCompletionStatus+attrEnd);
            if(current.equals(Job.STATE_JOB_CREATED) || current.equals(Job.STATE_JOB_SUBMITTED))
            {
                resultList.add("<img src=\"../common/images/iconStatusLoading.gif\"></img>");
            }
            else if(current.equals(Job.STATE_JOB_RUNNING))
            {
                if(abortRequested.equals("Yes"))
                {
                    resultList.add("<img src=\"../common/images/iconStatusError.gif\"></img>");
                }
                else
                {
                    resultList.add("<img src=\"../common/images/iconResponsibilityManufacturing.gif\"></img>");
                }
            }
            else if(current.equals(Job.STATE_JOB_COMPLETED) || current.equals(Job.STATE_JOB_ARCHIVED))
            {
                if(completionStatus.equals("Succeeded"))
                {
                    resultList.add("<img src=\"../common/images/buttonDialogDone.gif\"></img>");
                }
                else
                {
                    resultList.add("<img src=\"../common/images/buttonDialogCancel.gif\"></img>");
                }
            }
            else
            {
                resultList.add("");
            }
        }
        //XSSOK
        return resultList;
    }

    /**
     * Notifies the owner with a message.
     * Notifying is controlled thru "Notify Owner" attribute
     * @mx.whereUsed invoked as a trigger when a Job is promoted from running to completed.
     * @param context the ematrix Context
     * @param args
     * @throws Exception
     */
    public static int notifyOwner(Context context,String[] args)
    throws Exception
    {
        String objectId = PropertyUtil.getRPEValue(context,"OBJECTID",false);
        Job job= new Job();
        job.setId(objectId);
        job.notifyUser(context);
        return 0;
    }

    /**
     * Displays the log column based on teh current state and file chekced into log file.
     * @mx.Used as a "programHTMLOutput" function for "Log" column
     * @param context the ematrix Context
     * @param args
     * @return Vector containing the column values
     * @throws Exception
     */

    public Vector showLog(Context context, String[] args) throws Exception
    {
        String formatLog = PropertyUtil.getSchemaProperty(context,"format_Log");
        Map programMap = (Map)JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");

        Vector resultList = new Vector();
        StringBuffer rowBuffer = null;
        for(Iterator itr=objectList.iterator();itr.hasNext();)
        {
            rowBuffer = new StringBuffer();
            Map objectMap = (Map)itr.next();
            String logFileExists = (String)objectMap.get("format["+formatLog+"].hasfile");
            String jobId = (String)objectMap.get(DomainConstants.SELECT_ID);

            if (logFileExists.equalsIgnoreCase("TRUE")) {
                rowBuffer.append("<a href=\"");
                rowBuffer.append("javascript:showModalDialog('emxImportViewLogFileDialogFS.jsp?objectId=" + XSSUtil.encodeForJavaScript(context, jobId) + "'" + ",600,700)");
                rowBuffer.append("\"><img src=\"images/iconSmallPaperclipVertical.gif\" border=\"0\" style=\"text-align:center\"></img></a>");
            }

            resultList.addElement(rowBuffer.toString());
        }
        return resultList;
    }

    /**
     * Displays the action column based on teh current state and "Completion Status" attribute
     * @mx.Used as a "programHTMLOutput" function for "Action" column
     * @param context the ematrix Context
     * @param args
     * @return Vector containing the column values
     * @throws Exception
     */

    public Vector showAction(Context context, String[] args) throws Exception
    {
        Map programMap                  =   (Map)JPO.unpackArgs(args);
        MapList objectList              =   (MapList)programMap.get("objectList");
        String completionStatus,nextStepCommand,current;
        String attrCompletionStatus    =    PropertyUtil.getSchemaProperty(context,"attribute_CompletionStatus");
        String attrNextStepCommand      =    PropertyUtil.getSchemaProperty(context,"attribute_NextStepCommand");
        String curRowLink               = "";
        String curRowLabel              = "";
        String sLanguage                = context.getSession().getLanguage();
        Map objectMap,commandMap;
        StringBuffer rowBuffer;
        Vector resultList = new Vector();
        String jobId ;
        for(Iterator itr=objectList.iterator();itr.hasNext();)
        {
            objectMap        = (Map)itr.next();
            completionStatus     = (String)objectMap.get(attrBeg+attrCompletionStatus+attrEnd);
            current              = (String)objectMap.get(DomainConstants.SELECT_CURRENT);
            nextStepCommand      = (String)objectMap.get(attrBeg+attrNextStepCommand+attrEnd);
            curRowLink           = "";
            curRowLabel          = "";
            rowBuffer            = new StringBuffer();
            jobId = (String)objectMap.get(DomainConstants.SELECT_ID);

            if(current.equals(Job.STATE_JOB_RUNNING))
            {
                curRowLink  = "javascript:showModalDialog('../common/emxJobProcessFS.jsp?objectId=" + XSSUtil.encodeForJavaScript(context, jobId) + "',600,700)";
                curRowLabel = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Label.MonitorProgress", new Locale(sLanguage));                
            }
            if((current.equals(Job.STATE_JOB_COMPLETED) || current.equals(Job.STATE_JOB_ARCHIVED)) && (completionStatus.equals("Succeeded")))
            {
                if(nextStepCommand!=null && !"".equals(nextStepCommand) && !"null".equals(nextStepCommand))
                {
                    String windowHeight =  null;
                    String windowWidth = null;
                    String popupSize = null;
                    commandMap = getCommandData(context,jobId,nextStepCommand);
                    if(null!=commandMap.get("settings")) {
	                    windowWidth  = (String) ((Map)commandMap.get("settings")).get("Window Width");
	                    windowHeight = (String) ((Map)commandMap.get("settings")).get("Window Height");
	                    popupSize  = (String) ((Map)commandMap.get("settings")).get("Popup Size");
                    }
                    if (windowWidth == null || windowWidth.equals(""))
                    	windowWidth = "600";
                    if (windowHeight == null || windowHeight.equals(""))
                    	windowHeight = "700";
                    if(null == popupSize || "".equals(popupSize))
                    	popupSize = "Medium";
                    
                    curRowLink = "javascript:showModalDialog('"+commandMap.get("HREF")+"','"+XSSUtil.encodeForJavaScript(context, windowWidth)+"','"+XSSUtil.encodeForJavaScript(context, windowHeight)+"',true,'"+XSSUtil.encodeForJavaScript(context, popupSize)+"')";
                    curRowLabel= (String)commandMap.get("LABEL");
                }
            }
            if(curRowLink!=null && curRowLabel != null)
            {
                rowBuffer.append("<a href=\"");
                rowBuffer.append(curRowLink);
                rowBuffer.append("\">");
                rowBuffer.append(XSSUtil.encodeForHTML(context, curRowLabel));
                rowBuffer.append("</a>");
            }
            resultList.addElement(rowBuffer.toString());
        }

        return resultList;
    }

    /**
     * Gets the command data from UICache and formulates a map containing the translated HREF and LABEL values
     * @mx.Used in "showAction()" method
     * @param context the ematrix Context
     * @param commandName the name of the command that has been set as  the value for the attribute "Next Step Command"
     * @throws Exception
     * @return Map containing the Command information the keys will be "LABEL" and "HREF"
     */

    public Map getCommandData(Context context,String jobId,String commandName) throws Exception
    {


        Map commandMap=UICache.getCommand(context,commandName);

        String sLanguage = context.getSession().getLanguage();
        String commandLabel ;
        String nextHREF                         =(String)commandMap.get("href");

        String registeredSuite           =(String)((Map)commandMap.get("settings")).get("Registered Suite");
        commandLabel              =(String)commandMap.get("label");
        commandLabel              =EnoviaResourceBundle.getProperty(context, "emx"+registeredSuite+"StringResource", new Locale(sLanguage), commandLabel);        
        String actualDirectory    = null;
        String sourceStr          = "";
        HashMap resultMap        = new HashMap();
        if(nextHREF.startsWith("${COMPONENT_DIR}"))
        {
            actualDirectory = "../"+EnoviaResourceBundle.getProperty(context, "eServiceSuiteComponents.Directory");
            sourceStr       = "${COMPONENT_DIR}";
        }
        else if(nextHREF.startsWith("${COMMON_DIR}"))
        {
            //actualDirectory =  "../"+FrameworkProperties.getProperty(context, "eServiceSuiteCommon.Directory");
          actualDirectory =  "../"+EnoviaResourceBundle.getProperty(context, "eServiceSuiteFramework.Directory");
            sourceStr       = "${COMMON_DIR}";
        }
        else if(nextHREF.startsWith("${SUITE_DIR}"))
        {
            actualDirectory = "../"+EnoviaResourceBundle.getProperty(context, "eServiceSuite"+registeredSuite+".Directory");
            sourceStr       = "${SUITE_DIR}";
        }
        else
        {
            actualDirectory = null;
        }
        if(actualDirectory!=null)
        {
            nextHREF=FrameworkUtil.findAndReplace(nextHREF,sourceStr,actualDirectory);

        }

        if(nextHREF.indexOf("?")==-1)
        {
            nextHREF+="?";
        }
        else
        {
            nextHREF+="&";
        }
        nextHREF+="oid=";
        nextHREF+=jobId;

        nextHREF+="&objectId=";
        nextHREF+=jobId;

        resultMap.put("LABEL",commandLabel);
        resultMap.put("HREF",nextHREF);
        resultMap.put("settings",(Map)commandMap.get("settings"));

        return resultMap;
    }

    /**
     * Displays the Progress Percent column based on teh current state and "Progress Percent" attribute
     * @mx.Used as a "program" function for "Action" column
     * @param context the ematrix Context
     * @param args
     * @throws Exception
     * @return Vector containing the column values
     */

    public Vector showProgressPercent(Context context,String[] args) throws Exception
    {
        Map programMap              =   (Map)JPO.unpackArgs(args);
        MapList objectList          =   (MapList)programMap.get("objectList");
        String sLanguage            =    context.getSession().getLanguage();
        String attrProgressPercent  =    PropertyUtil.getSchemaProperty(context,"attribute_ProgressPercent");
        String unknownProgress      =    EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.BackgroundProcess.UnknownProgress", new Locale(sLanguage));        
        String current,progressPercent;
        Vector resultList = new Vector();
        String emptyResult = "0";
        String completeResult = "100";
        Map objectMap;
        for(Iterator itr=objectList.iterator();itr.hasNext();)
        {
            objectMap        = (Map)itr.next();
            progressPercent  = (String)objectMap.get(attrBeg+attrProgressPercent+attrEnd);
            current          = (String)objectMap.get(DomainConstants.SELECT_CURRENT);

            if(current.equals(Job.STATE_JOB_RUNNING))
            {

              try
              {
                  Integer.parseInt(progressPercent);

                  resultList.addElement(progressPercent);
              }
              catch(NumberFormatException nfe){resultList.addElement(unknownProgress);}
            }
            else if(current.equals(Job.STATE_JOB_COMPLETED) || current.equals(Job.STATE_JOB_ARCHIVED))
            {
                resultList.addElement(completeResult);
            }
            else
            {
                resultList.addElement(emptyResult);
            }
        }
        return resultList;
    }

   /**
     * Deletes the  Jobs
     *
     * @mx.whereUsed Invoked by the user from Background Job List view action
     *               menu
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param BackgroundJobIds
     *            list of Background Job objects that will be deleted
     * @throws FrameworkException
     *             if the operation fails
     * @since AEF V11-0-0-0
     */
    public void delete(Context context,String[] args) throws FrameworkException
    {

    	try
    	{
    		Map programMap              =   (Map)JPO.unpackArgs(args);
    		String[] tableRowIds          =   (String[])programMap.get("JobIds");
    		String[] jobIds = FrameworkUtil.getSplitTableRowIds(tableRowIds);
    		//String current;

    		String sLanguage            =    context.getSession().getLanguage();
    		String objectNotDeleted      =    EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.NoNCompletedOrArchivedDeleteWithList", new Locale(sLanguage));      
    		StringList objectIdsNotDeleted = new StringList();

    		String result;
    		StringList objectSelects = new StringList();
    		objectSelects.add(DomainConstants.SELECT_NAME);
    		objectSelects.add(DomainConstants.SELECT_CURRENT);


    		for(int index=0;index<jobIds.length;index++)
    		{
    			String objectId =  jobIds[index];
    			Job jobObject = new Job(objectId);
    			jobObject.open(context);
    			Map resMap = jobObject.getInfo(context,objectSelects);
    			String current = (String) resMap.get(DomainConstants.SELECT_CURRENT);

    			if(current.equals(Job.STATE_JOB_COMPLETED) || current.equals(Job.STATE_JOB_ARCHIVED))
    			{
    				jobObject.deleteObject(context, true);
    			}
    			else
    			{
    				objectIdsNotDeleted.add(resMap.get(DomainConstants.SELECT_NAME));
    			}
    		}


    		if(objectIdsNotDeleted.size()>0)
    		{
    			result = objectNotDeleted+FrameworkUtil.join(objectIdsNotDeleted,",");
    			MqlUtil.mqlCommand(context, "notice $1",result);
    		}
    	}
    	catch(Exception e)
    	{
    		throw new FrameworkException(e);
    	}
    }


   /**
     * Archives the  Jobs
     *
     * @mx.whereUsed Invoked by the user from Background Job List view action
     *               menu
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param BackgroundJobIds
     *            list of Background Job objects that will be archived
     * @throws FrameworkException
     *             if the operation fails
     * @since AEF V11-0-0-0
     */
    public void archive(Context context,String[] args) throws FrameworkException
    {

    try
    {
    	
		Map programMap              =   (Map)JPO.unpackArgs(args);
		String[] tableRowIds          =   (String[])programMap.get("JobIds");
		String[] jobIds = FrameworkUtil.getSplitTableRowIds(tableRowIds);

      String sLanguage            = context.getSession().getLanguage();
      String strNonCompletedProcessAlert  =  EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.NonCompletedArchiveList", new Locale(sLanguage));      

      String message = null;
      StringList objectIdsNotArchived = new StringList();

      StringList objectSelects = new StringList();
      objectSelects.add(DomainConstants.SELECT_NAME);
      objectSelects.add(DomainConstants.SELECT_CURRENT);


      for(int index=0;index<jobIds.length;index++)
      {
        String objectId =  jobIds[index];
        DomainObject dom = new DomainObject(objectId);
        dom.open(context);

        Map resMap = dom.getInfo(context,objectSelects);

        if(((String)resMap.get(DomainConstants.SELECT_CURRENT)).equals(Job.STATE_JOB_COMPLETED))
        {
          dom.setState(context,Job.STATE_JOB_ARCHIVED);
        }
        else
        {
          objectIdsNotArchived.add((String)resMap.get(DomainConstants.SELECT_NAME));
        }
      }
      if(objectIdsNotArchived.size()>0)
      {
        message = strNonCompletedProcessAlert+FrameworkUtil.join(objectIdsNotArchived,",");
        MqlUtil.mqlCommand(context, "notice $1",message);
      }
    }
    catch(Exception e)
    {    
    	throw new FrameworkException(e);
    }
      }


  /**
  * Abort the  Jobs
  *
  * @mx.whereUsed Invoked by the user from Background Job List view action
  *               menu
  * @param context
  *            the eMatrix <code>Context</code> object
  * @param BackgroundJobIds
  *            list of Background Job objects that will be archived
  * @throws FrameworkException
  *             if the operation fails
  * @since AEF V11-0-0-0
  */
  public void abort(Context context,String[] args) throws FrameworkException
  {

    try
    {
		Map programMap              =   (Map)JPO.unpackArgs(args);
		String[] tableRowIds          =   (String[])programMap.get("JobIds");
		String[] jobIds = FrameworkUtil.getSplitTableRowIds(tableRowIds);
      String sLanguage            = context.getSession().getLanguage();
      String attrAbortRequested   = PropertyUtil.getSchemaProperty(context,"attribute_AbortRequested");
      String strCannotAbort       = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Messages.CannotAbortList", new Locale(sLanguage));      
      boolean isCompleted = true;

      String message=null;
      StringList objectIdsNotAborted = new StringList();

      StringList objectSelects = new StringList();
      objectSelects.add(DomainConstants.SELECT_NAME);
      objectSelects.add(DomainConstants.SELECT_CURRENT);

      for(int index=0;index<jobIds.length;index++)
      {
        String objectId =  jobIds[index];
        DomainObject dom = new DomainObject(objectId);
        dom.open(context);

        Map resMap = dom.getInfo(context,objectSelects);
        String current = (String) resMap.get(DomainConstants.SELECT_CURRENT);

        if(current.equals(Job.STATE_JOB_COMPLETED) ||current.equals(Job.STATE_JOB_ARCHIVED))
        {
          isCompleted=false;
        }

        if(isCompleted)
        {
          dom.setAttributeValue(context,attrAbortRequested,"Yes");

          String attrCompletionStatus    =PropertyUtil.getSchemaProperty(context,"attribute_CompletionStatus");
          dom.setAttributeValue(context, attrCompletionStatus, "Failed");
          dom.setState(context, Job.STATE_JOB_COMPLETED);

        }
        else
        {
          objectIdsNotAborted.add((String)resMap.get(DomainConstants.SELECT_NAME));
        }
      }
      if(objectIdsNotAborted.size()>0)
      {
        message = strCannotAbort+FrameworkUtil.join(objectIdsNotAborted,",");
        MqlUtil.mqlCommand(context, "notice $1",message);
      }
    }
    catch(Exception e)
    {
            throw new FrameworkException(e);
    }
  } // end of method
  public void initialLoad(Context context, String[] args) throws Exception
  {
      //System.out.println("emxJobBase:initialLoad System.getProperty(\"ServerUniqueIdentifier\") == " + System.getProperty("ServerUniqueIdentifier") );
      try
      {
          String serverIndentity = FrameworkUtil.getServerUniqueIdentifier(context);
          String typeJob                 =PropertyUtil.getSchemaProperty(context,"type_Job");
          String attrServerUniqueIdentifier  = PropertyUtil.getSchemaProperty(context,"attribute_ServerUniqueIdentifier");
          
          StringBuffer	whereExp	= new StringBuffer("((");
          whereExp.append(attrBeg);
          whereExp.append(attrServerUniqueIdentifier);
          whereExp.append(attrEnd);
          whereExp.append(" == '");
          whereExp.append(serverIndentity);
          whereExp.append("'");
          whereExp.append(") && (");
          whereExp.append("current!=" + Job.STATE_JOB_ARCHIVED +"&& current!=" + Job.STATE_JOB_COMPLETED +"))");
          //String whereExpression = attrBeg + attrServerUniqueIdentifier + attrEnd + " == '"+ serverIndentity +"'";         
          StringList objSelects = new StringList(5);
          objSelects.addElement(SELECT_ID);
          MapList mlist = DomainObject.findObjects(context, typeJob, DomainConstants.QUERY_WILDCARD, whereExp.toString(), objSelects);
          Iterator itr = mlist.iterator();
          Job job;
          while(itr.hasNext())
          {
              Map m = (Map)itr.next();
              String jobId = (String)m.get(SELECT_ID);
              job = Job.getInstance(context, jobId);
              job.reStartOnServerStartUp(context);
          }
      }
      catch(Exception ex)
      {
          ex.printStackTrace();
      }

  }
  /**
   * Notify if Equipment List Report Job is completed, if job is
   * complete it removes pending job relationship.
   * @mx.whereUsed invoked as a trigger when a Equipment List Report job is pending.
   * @param context the ematrix Context
   * @param args
   * @throws Exception
   */
  public static void removePendingJob(Context context,String[] args)
  throws Exception
  {
      String objectId = args[0];
      Job job= new Job();
      job.setId(objectId);
      try
      {
    	  ContextUtil.pushContext(context);
	      StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
	      String relPattern = PropertyUtil.getSchemaProperty(context,"relationship_PendingJob");
	      Map mapRel = job.getRelatedObject(context,relPattern, false, null, objectSelects);
	
	      if (mapRel!=null && ! mapRel.isEmpty()) {
	
	          String relId = (String)mapRel.get(DomainConstants.SELECT_ID);
	          DomainRelationship.disconnect(context,relId);
	      }
      } finally
      {
    	  ContextUtil.popContext(context);
      }
   }//end of method "removePendingJob"
} // end of class
