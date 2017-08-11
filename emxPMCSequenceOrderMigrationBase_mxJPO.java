/*
** emxPMCSequenceOrderMigrationBase
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*
*/

import  java.io.FileOutputStream;
import  java.io.PrintStream;
import  java.util.Map;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.StringList;

import  com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.i18nNow;


/**
 * The <code>emxPMCSequenceOrderMigrationBase</code> class contains migration script
 * @author 
 * @version ProgramCentral V6R2008-1 - Copyright (c) 2007, MatrixOne, Inc.
 *
 */
public class emxPMCSequenceOrderMigrationBase_mxJPO 
{
    private static final String BUNDLE_STR = "emxProgramCentralStringResource";
    private static final String MIGRATION_LOG_FILE = "emxProgramCentral.Migration.LogInformation";
    private static final String MIGRATION_ERR_FILE = "emxProgramCentral.Migration.ERRInformation";
    private static final String END_LINE = "\n";
    private static final String MIGRATE_SUCCESS = "emxProgramCentral.Migration.Log.MigrateSuccess";
    private static final String MIGRATE_FAILURE = "emxProgramCentral.Migration.Log.MigrateFailure";
    private static FileOutputStream foLogFileInfo;
    private static FileOutputStream foErrFile;
    private static PrintStream psLogWriter;
    private static PrintStream psErrWriter;
    public static  MQLCommand mqlCommand = null;
    private static String strFileURI= DomainConstants.EMPTY_STRING;


    /**
    * Create a new emxPMCSequenceOrderMigrationBase object from a given id.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return a emxPMCSequenceOrderMigrationBase Object
    * @throws Exception if the operation fails
    * @since ProgramCentral V6R2008-1
    */
    public emxPMCSequenceOrderMigrationBase_mxJPO (Context context, String[] args) throws Exception
    {
        //super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProgramCentral V6R2008-1
     */
    public int mxMain (Context context, String[] args)
    throws Exception
    {
        if (!context.isConnected())
        {
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
    				"emxProgramCentral.Error.UnsupportedClient", context.getSession().getLanguage());
            throw  new Exception(strContentLabel);
        }
        migrate(context,args);
        return  0;
     }

    /**
     * this method migartes the data.
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @return void
     * @throws Exception if the operation fails
     * @since ProgramCentral V6R2008-1
     */

    public void migrate(Context context,String[] args)
    throws Exception
    {
        MQLCommand mqlCommand = new MQLCommand();
        //set the trigger off
        mqlCommand.executeCommand(context,"trigger off");
try {
        try
        {
            foLogFileInfo = new FileOutputStream(getString(context,MIGRATION_LOG_FILE,"emxProgramCentral"),false);
            psLogWriter = new PrintStream(foLogFileInfo);
        }
        catch(Exception e)
        {
            Exception ex=new Exception("Migration Log File Entry Not Present in the Properties File");
            throw e;
        }
        //open file for error logging
        try
        {
            foErrFile = new FileOutputStream(getString(context,MIGRATION_ERR_FILE,"emxProgramCentral"),false);
            psErrWriter = new PrintStream(foErrFile);
        }
        catch(Exception e)
        {
            Exception ex=new Exception("Migration Error File Entry Not Present in the Properties File");
            throw ex;
        }

       // Migrate all the Image objects to checkin them to appropriate formats
        migrateSequenceOrder(context, args);
} finally {
        //set the trigger on
        mqlCommand.executeCommand(context,"trigger on");
}
    }

    /**
     * this method migrates all the Image objects to the present data definition
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @return void
     * @throws Exception if the operation fails
     * @since ProgramCentral V6R2008-1
     */
    protected void migrateSequenceOrder(Context context,String[] args) throws Exception
    {
            String projectId = "";
            String projectName = "";
            String projectRev = "";
            String projectType = "";
        try
        {
            StringList objSelect = new StringList(4);
            StringList relSelect = new StringList(2);
            MapList mapList = null;
            String strType=DomainConstants.TYPE_PROJECT_SPACE+","+DomainConstants.TYPE_PROJECT_CONCEPT;
            StringBuffer sbWhereExp = new StringBuffer(150);
            String strWhereExp="";
            // Adding this where condition because the TaskManagement extends ProjectManagement type
            // and the Task type objects are also being returned.
            //object selects
            objSelect.addElement(DomainConstants.SELECT_ID);
            objSelect.addElement(DomainConstants.SELECT_TYPE);
            objSelect.addElement(DomainConstants.SELECT_NAME);
            objSelect.addElement(DomainConstants.SELECT_REVISION);
            // fetching all the ProjectManagement objects in the database
            mapList = DomainObject.findObjects(context,strType,DomainConstants.QUERY_WILDCARD,null,objSelect);

            Task task = new Task();
            migrationInfoWriter("Updating the Sequence Order attribute for the following Objects");
            for(int i=0;i<mapList.size();i++)
            {
                projectId = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_ID);
                projectName = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_NAME);
                projectRev = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_REVISION);
                projectType = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_TYPE);
                task.reSequence(context,projectId);
                migrationInfoWriter(" <Type> "+projectType+" <Name> "+projectName+" <Revision> "+projectRev);
            }
            migrationInfoWriter(getString(context,MIGRATE_SUCCESS));
        }
        catch(Exception ex)
        {
            migrationInfoWriter(getString(context,MIGRATE_FAILURE+" for the following object" +" <Type> "+projectType+" <Name> "+projectName+" <Revision> "+projectRev));
            throw ex;
        }
    }


    /**
    * This method writes to the log file using the Printwriter object
    * @param strLogEnrty - Message to write
    * @return void
    * @throws Exception if the operation fails
    * @since ProgramCentral V6R2008-1
    * @grade 0
    */
    protected void migrationInfoWriter(String strLogEnrty)
    throws Exception
    {
        if (strLogEnrty!=null && strLogEnrty.length()>0)
        {
            psLogWriter.println(strLogEnrty);

        }
    }
    /**
    * This method writes to the ERR file using the Printwriter object
    * @param strLogEnrty - Message to write
    * @return void
    * @throws Exception if the operation fails
    * @since ProgramCentral V6R2008-1
    * @grade 0
    */
    protected void migrationErrWriter(String strLogEnrty)
    throws Exception
    {
        if (strLogEnrty!=null && strLogEnrty.length()>0)
        {
            psErrWriter.println(strLogEnrty);
        }
    }


    /**
    * This method returns internalized value for the passed key in
    * ProductCentral string resouce file.
    * @param context - The eMatrix <code>Context</code> object
    * @param strKey - Property file entry
    * @since ProgramCentral V6R2008-1
    * @grade 0
    */
    private String getString(Context context,String strKey, String strBundle) throws Exception
    {
        i18nNow i18nNowInstance = new i18nNow();
        String strLocale = context.getSession().getLanguage();
        return i18nNowInstance.GetString(strBundle,strLocale,strKey);
    }


    /**
    * This method returns internalized value for the passed key in
    * ProductCentral string resouce file.
    * @param context - The eMatrix <code>Context</code> object
    * @param strKey - Property file entry
    * @since ProgramCentral V6R2008-1
    * @grade 0
    */
    private String getString(Context context,String strKey) throws Exception
    {
        i18nNow i18nNowInstance = new i18nNow();
        String strLocale = context.getSession().getLanguage();
        return i18nNowInstance.GetString(BUNDLE_STR,strLocale,strKey);
    }

}
