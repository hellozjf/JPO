/*
 *  ${${CLASSNAME}}.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 *
 * (c) Dassault Systemes, 1993 - 2010.  All rights reserved
 *
 *
 *  static const char RCSID[] = $Id: /java/JPOsrc/${CLASSNAME} 1.30.2.15.1.1.1.1 Thu Nov 13 08:27:30 2008 GMT ds-skumaran Experimental${CLASSNAME}.java 1.2 Mon Oct 01 04:47:29 2007 GMT sgudlavalleti Experimental$
 */
import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.classification.ClassificationUtil;
import com.matrixone.apps.classification.LibraryCentralJobs;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.library.LibraryCentralConstants;


/**
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxLibraryCentralJobsBase_mxJPO extends emxJob_mxJPO
{

	private String jobId=null;

	private File downloadFile=null;

	public static final String attrBeg                 ="attribute[";
	public static final String attrEnd                 ="]";
	public static final String SELECT_ATTRIBUTE_LBC_JOB_TYPE="to["+LibraryCentralConstants.RELATIONSHIP_JOBS+"]."+attrBeg+LibraryCentralConstants.ATTRIBUTE_JOB_TYPE+attrEnd;
	/**
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.1
	 */
	public emxLibraryCentralJobsBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		super(context, args);
		if(args.length==2)
		{
			Map hMap = (Map)JPO.unpackArgs(args);
			jobId=(String)hMap.get("Job ID");
		}
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return void
	 * @throws Exception if the operation fails
	 * @since AEF 9.5.1.1
	 */
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if (!context.isConnected())
			throw new Exception("not supported on desktop client");
		return 0;
	}

	/***
	 * Exports the Library, using the Library Id. Completes the associated Job
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args, String array containing the Library Id
	 * @return int
	 * @throws Exception
	 */
	public int exportLibraryASync(Context context, String args[]) throws Exception{
		try{
			LibraryCentralJobs job = (LibraryCentralJobs)Job.getInstance(context,jobId,"CLASSIFICATION");
			String objectId=args[0];
			HashMap paramMap=new HashMap();
			paramMap.put("LibraryObjectId",objectId);
			String packArgs[]=JPO.packArgs(paramMap);
			java.io.File exportFile=(java.io.File)JPO.invoke(context,"emxMultipleClassificationClassificationBase",null,"exportLibrary",packArgs,File.class);
			StringList strFileList = new StringList(1);
			strFileList.addElement(exportFile.getName());
			job.checkinFromServer(context,true,false,"Output","",strFileList);
			job.setProgressPercent(100);
			job.finish(context,"Succeeded");
			this.downloadFile=exportFile;
		}catch(Exception err){
			err.printStackTrace();
		}
		return 0;
	}
	/***
	 * Exports Attribute Group, using the Attribute Group name.Completes the associated Job
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args, containing the names of Attribute Group
	 * @return int
	 * @throws Exception
	 */
	public int exportAttributeGroupASync(Context context, String args[]) throws Exception{
		try{
			LibraryCentralJobs job = (LibraryCentralJobs)Job.getInstance(context,jobId,"CLASSIFICATION");
			String[] constructor = {null};
			HashMap paramMap=new HashMap();
			paramMap.put("AGNames",args);
			String packArgs[]=JPO.packArgs(paramMap);
			java.io.File exportFile=(java.io.File)JPO.invoke(context,"emxMultipleClassificationAttributeGroupBase",constructor,"exportAttributeGroup",packArgs,File.class);
			StringList strFileList = new StringList(1);
			strFileList.addElement(exportFile.getName());
			job.checkinFromServer(context,true,false,"Output","",strFileList);
			job.setProgressPercent(100);
			job.finish(context,"Succeeded");
			this.downloadFile=exportFile;
		}catch(Exception err){
			err.printStackTrace();
		}

		return 0;
	}

	/***
	 * Imports the Attribute Group,Creates a log file associated with the Job.Completes the associated Job
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args, String containing Attribute Group name
	 * @throws FrameworkException
	 */
	public void importAttributeGroupASync(Context context, String args[]) throws FrameworkException,Exception{
		try{
            StringBuffer messageBuff=new StringBuffer("1\t");
            messageBuff.append(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Command.Import"));
            if(args[0].indexOf("|") != -1)
            	messageBuff.append("\t").append(args[0]);
            else
            	messageBuff.append("\t").append(args[0].substring(args[0].indexOf("|")+1, args[0].length()));
            messageBuff.append("\t").append(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Command.ImportHeading"));
            messageBuff.append("\t").append(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Job.AGImportInitialSuccess"));
            messageBuff.append("\t");
			LibraryCentralJobs job = (LibraryCentralJobs)Job.getInstance(context,jobId,"CLASSIFICATION");
			String logMsg=EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Job.InitiateAGImport");
            job.logEntry(context, "import_log.txt", logMsg,messageBuff.toString()+logMsg+" "+args[1]);
            job.setProgressPercent(5);
            logMsg=EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Job.AGImportSuccess");
            job.logEntry(context, "import_log.txt", logMsg,messageBuff.toString()+logMsg+" ");
            job.setCurrentActivity(logMsg+" "+args[0]);
            job.setProgressPercent(100);
            job.finish(context,"Succeeded");
		}catch(Exception err){
            err.printStackTrace();

		}
	}
	/***
	 * Imports the Library,Creates a log file associated with the Job.Completes the associated Job
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args, String containing Library name
	 * @throws FrameworkException
	 * @throws Exception
	 */

	public void importLibraryASync(Context context, String args[]) throws FrameworkException,Exception{
		try{
            StringBuffer messageBuff=new StringBuffer("1\t");
            messageBuff.append(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Command.Import"));
            messageBuff.append("\t").append(args[0]);
            messageBuff.append("\t").append(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Command.ImportHeadingLibrary"));
            messageBuff.append("\t").append(EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Job.GLImportInitialSuccess"));
            messageBuff.append("\t");
			LibraryCentralJobs job = (LibraryCentralJobs)Job.getInstance(context,jobId,"CLASSIFICATION");
			String logMsg=EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Job.InitiateGLImport");
            job.logEntry(context, "import_log.txt", logMsg,messageBuff.toString()+logMsg+" "+args[1]);
            job.setProgressPercent(5);
            logMsg=EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Job.GLImportSuccess");
            job.logEntry(context, "import_log.txt", logMsg,messageBuff.toString()+logMsg+" ");
            job.setCurrentActivity(logMsg+" "+args[0]);
            job.setProgressPercent(100);
            job.finish(context,"Succeeded");
		}catch(Exception err){
            err.printStackTrace();

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
	public static MapList getLibraryJobs(Context context,String args[],String filter)
	throws FrameworkException
	{
		MapList resultList          =new MapList();
		try
		{
			HashMap programMap  = (HashMap)JPO.unpackArgs(args);
			String objectId=(String)programMap.get("objectId");
			String agName=(String)programMap.get("objectName");
			String user                    =context.getUser();
			StringList objectSelects       =new StringList();
			String typeJob                 =PropertyUtil.getSchemaProperty(context,"type_Job");
			String attrCompletionStatus    =PropertyUtil.getSchemaProperty(context,"attribute_CompletionStatus");
			String title=PropertyUtil.getSchemaProperty(context,"attribute_Title");
			String attrAbortRequested      =PropertyUtil.getSchemaProperty(context,"attribute_AbortRequested");
			String attrNextStepCommand      =    PropertyUtil.getSchemaProperty(context,"attribute_NextStepCommand");
			String attrProgressPercent      =    PropertyUtil.getSchemaProperty(context,"attribute_ProgressPercent");
			String formatLog               =PropertyUtil.getSchemaProperty(context,"format_Log");
			String attrProgramArgs=PropertyUtil.getSchemaProperty(context,"attribute_ProgramArguments");
			String description=PropertyUtil.getSchemaProperty(context,"attribute_Description");
			StringBuffer whereExp       =new StringBuffer();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.add(DomainConstants.SELECT_CURRENT);
			objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
			objectSelects.add(attrBeg+attrCompletionStatus+attrEnd);
			objectSelects.add(attrBeg+attrAbortRequested+attrEnd);
			objectSelects.add(attrBeg+attrNextStepCommand+attrEnd);
			objectSelects.add(attrBeg+attrProgressPercent+attrEnd);
			objectSelects.add(attrBeg+attrCompletionStatus+attrEnd);
			objectSelects.add("format["+formatLog+"].hasfile");
			objectSelects.add(attrBeg+title+attrEnd);
			objectSelects.add(SELECT_ATTRIBUTE_LBC_JOB_TYPE);
			if(agName==null){
				//objectSelects.add(attrBeg+attrCompletionStatus+attrEnd+agName+"*");
				whereExp.append("to["+LibraryCentralConstants.RELATIONSHIP_JOBS+"].from.id=='"+objectId+"'");
			}
			else{
				whereExp.append(DomainConstants.SELECT_DESCRIPTION+"="+"~~"+"\""+"*"+agName+"*"+"\"");
			}
			if(filter.equalsIgnoreCase("All"))
			{
				//do nothing
			}
			else if(filter.equalsIgnoreCase("Archive"))
			{
				whereExp.append(" && current == Archived");
			}
			else if(filter.equalsIgnoreCase("Current"))
			{
				whereExp.append(" && current ==" +Job.STATE_JOB_RUNNING);
			}
			else if(filter.equalsIgnoreCase("Completed"))
			{
				whereExp.append(" && ((");
				whereExp.append(attrBeg);
				whereExp.append(attrCompletionStatus);
				whereExp.append("]==Succeeded");
				whereExp.append(") && (");
				whereExp.append("current==Completed))");
			}
			else if(filter.equalsIgnoreCase("Failed"))
			{
				whereExp.append(" && ((");
				whereExp.append(attrBeg);
				whereExp.append(attrCompletionStatus);
				whereExp.append("] matchlist \"Failed,Aborted\" \",\"");
				whereExp.append(") && (");
				whereExp.append("current==Completed))");
			}
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
			e.printStackTrace();
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
		return getLibraryJobs(context,args,"All");
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public static MapList getMyCurrentBackgroundJobs(Context context,String args[])
	throws FrameworkException
	{
		return getLibraryJobs(context,args,"Current");
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public static MapList getMyArchivedBackgroundJobs(Context context,String args[])
	throws FrameworkException
	{
		return getLibraryJobs(context,args,"Archive");
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
		return getLibraryJobs(context,args,"Completed");
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
		return getLibraryJobs(context,args,"Failed");
	}
	/***
	 * Returns a link to download the exported file, the file could Attribute Group xml or Library xml
	 * @param context the ENOVIA <code>Context</code> object
	 * @param args
	 * @return a vector containing link to the download file
	 * @throws FrameworkException
	 */
	public Vector getDownloadedFile(Context context, String args[]) throws FrameworkException{
		Vector resultList=new Vector();
		try{
			Map programMap                  =   (Map)JPO.unpackArgs(args);
			MapList objectList              =   (MapList)programMap.get("objectList");
			String completionStatus,current,hasFile,title,hasLog;
	        String attrCompletionStatus    =    PropertyUtil.getSchemaProperty(context,"attribute_CompletionStatus");
	        String jobType=null;
	        for(Iterator itr=objectList.iterator();itr.hasNext();)
	        {
	            Map objectMap = (Map) itr.next();
	            completionStatus     = (String)objectMap.get(attrBeg+attrCompletionStatus+attrEnd);
	            current              = (String)objectMap.get(DomainConstants.SELECT_CURRENT);
	            hasFile	=(String)objectMap.get("format[Output].hasfile");
	            title=(String)objectMap.get("attribute[Title]");
	            hasLog	=(String)objectMap.get("format[Log].hasfile");
	            //System.out.println("Inside the getDownload File:title "+title);
	            String initMsg=EnoviaResourceBundle.getProperty(context,"emxLibraryCentralStringResource",new Locale(context.getSession().getLanguage()),"emxMultipleClassification.Command.ExportHeading");
	            jobType=(String)objectMap.get(SELECT_ATTRIBUTE_LBC_JOB_TYPE);
	            if(current.equals("Created") || current.equals("Submitted") || current.equals("Running") )
	            {
	                resultList.add("");
	            }
	            else if(current.equals("Completed") || current.equals("Archived"))
	            {
	            		String jobId = (String)objectMap.get(DomainConstants.SELECT_ID);
	                    String url="../documentcentral/emxLibraryCentralJobsDownloadFile.jsp?objectId="+XSSUtil.encodeForURL(context, jobId);
	                    if(!hasLog.equals("TRUE"))
	                    	resultList.add("<a class=\"object\" href=\""+url+"\"><img border=\"0\" src=\"../common/images/iconSmallPaperclipVertical.gif\"></img></a>");
	                    else
	                    	resultList.add("");
	            }
	            else
	            {
	                resultList.add("");
	            }
	        }
		}catch(Exception err){
			err.printStackTrace();
		}
		return resultList;
	}
}

