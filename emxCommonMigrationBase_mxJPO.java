/*
 * emxCommonMigrationBase.java
 * program for common migrate model.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

 import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
  import com.matrixone.apps.framework.ui.UICache;

  public class emxCommonMigrationBase_mxJPO extends emxDomainObject_mxJPO
  {
      public static int _counter  = 0;
      public static int _sequence  = 1;
      public static java.io.File _oidsFile = null;
      public static BufferedWriter _fileWriter = null;
      public static StringList _objectidList = null;
      public static int _chunk = 0;

      BufferedWriter writer = null;
      FileWriter errorLog = null;
      FileWriter warningLog = null;
      FileWriter convertedOidsLog = null;
      FileWriter statusLog = null;
      FileWriter unconvertedOidsLog = null;

      static String documentDirectory = "";
      static int minRange = 0;
      static int maxRange = 0;

      static StringList mxMainObjectSelects = new StringList(51);
      public static MQLCommand mqlCommand = null;

      long startTime = System.currentTimeMillis();
      long migrationStartTime = System.currentTimeMillis();

      boolean isConverted = false;
      boolean scan = false;
      String error = null;

      //  Suspend Versiong attribute ***
      StringBuffer migratedOids = new StringBuffer(20000);
      boolean debug = false;
      static long unconvertedChunkSize = 50000;
      long unconvertedObjectCount = 0;
      int unconvertedFileCount = 1;
      StringBuffer statusBuffer = new StringBuffer(50000);
      String failureId = "";
      
      String _interOpCommand = "";
      Map _interOpMethods = new HashMap(10);
      ResourceBundle _propertyResource = null;
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxCommonMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
          writer     = new BufferedWriter(new MatrixWriter(context));
          mqlCommand = new MQLCommand();
      }


      public void mqlLogWriter(String command) throws Exception
      {
          if(debug)
          {
              writer.write(command);
              writer.flush();
              logWarning(command);
          }
      }
      public void mqlLogRequiredInformationWriter(String command) throws Exception
      {
          writer.write(command);
          writer.flush();
          logWarning(command);
      }

      public void writeUnconvertedOID(String command) throws Exception
      {
          writeUnconvertedOID(command, null);
      }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public int mxMain(Context context, String[] args) throws Exception
      {
          if(!context.isConnected())
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
          }
          int argsLength = args.length;
          error = "";
          
      //added for BugNo - 355683
          try
          {
              //getting the context user
              String contextUser=context.getUser();
              //checking whether context user has admin privilages
              String cmd = "print person '"+ contextUser +"' select system dump" ;
              String businessAccess = MqlUtil.mqlCommand(context,mqlCommand,cmd);
              //if context user does not have admin privilages then throwing Exception
              if("false".equalsIgnoreCase(businessAccess))
              {
                  error = "User does not have Admin Privileges";
                  throw new Exception(error);
              }
          }
          catch(Exception e)
          {
              writer.write("====================================================================\n");
              writer.write(e.getMessage() + " \n");
              writer.write("Step 2 of Migration :     FAILED \n");
              writer.write("====================================================================\n");     
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              return 0;
          } //End for BugNo - 355683

          try
          {
              // writer     = new BufferedWriter(new MatrixWriter(context));
        	  if (args.length < 3 )
              {
                  error = "Wrong number of arguments";
                  throw new IllegalArgumentException(error);
              }
              documentDirectory = args[0];
              
              // documentDirectory does not ends with "/" add it
              String fileSeparator = java.io.File.separator;
              if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
              {
                documentDirectory = documentDirectory + fileSeparator;
              }

              minRange = Integer.parseInt(args[1]);

              if ("n".equalsIgnoreCase(args[2]))
              {
                maxRange = getTotalFilesInDirectory();
                if(maxRange == 0){
					try{
                	error = "There are no objects available for Migration";
						throw new IllegalArgumentException(error);
					}
					catch (IllegalArgumentException iExp){
					    writer.write("====================================================================\n");
					    writer.write(iExp.getMessage() + " \n");
					    writer.write("Step 2 of Migration :     COMPLETED \n");
					    writer.write("====================================================================\n");
					    // if scan is true, writer will be closed by the caller
					    if(!scan){
						  writer.close();
					    }
					    return 0;
					}
                }                
              } else {
                maxRange = Integer.parseInt(args[2]);
              }

              if (minRange > maxRange)
              {
                error = "Invalid range for arguments, minimum file range is greater than maximum file range value";
                throw new IllegalArgumentException(error);
              }

              if (minRange == 0 || minRange < 1 || maxRange == 0 || maxRange < 1)
              {
                error = "Invalid range for arguments, minimum/maximum file range value is 0 or negative";
                throw new IllegalArgumentException(error);
              }
              //Fix for the chunk size starts
              java.io.File minRangeFile = new java.io.File(documentDirectory + "objectids_" + minRange + ".txt");
              java.io.File maxRangeFile = new java.io.File(documentDirectory + "objectids_" + maxRange + ".txt");
              
              if(!minRangeFile.exists() || !maxRangeFile.exists()) {
				error = "Invalid range for arguments.  Either minimum or maximum file range does not exist";
                throw new IllegalArgumentException(error);
              }
              //
          }
          catch (IllegalArgumentException iExp)
          {
              writer.write("====================================================================\n");
              writer.write(iExp.getMessage() + " \n");
              writer.write("Step 2 of Migration :     FAILED \n");
              writer.write("====================================================================\n");
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              return 0;
          }

          String debugString = "false";

          if( argsLength >= 4 )
          {
              debugString = args[3];
              if ( "debug".equalsIgnoreCase(debugString) )
              {
                  debug = true;
              }
          }
          
          //String scanString = "scan";
          String scanString = "";
          if( argsLength >= 5 )
          {
        	  scanString = args[4];
              if ( "scan".equalsIgnoreCase(scanString) )
              {
                  scan = true;
              }
          }
          try
          {
              createLogs();
          }
          catch(FileNotFoundException fExp)
          {
              // check if user has access to the directory
              // check if directory exists
              writer.write("=================================================================\n");
              writer.write("Directory does not exist or does not have access to the directory\n");
              writer.write("Step 2 of Migration :     FAILED \n");
              writer.write("=================================================================\n");
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              return 0;
          }

          int i = 0;
          try
          {
              ContextUtil.pushContext(context);
              String cmd = "trigger off";
              MqlUtil.mqlCommand(context, mqlCommand,  cmd);
              mqlLogRequiredInformationWriter("=======================================================\n\n");
              mqlLogRequiredInformationWriter("                Migrating Objects...\n");
              mqlLogRequiredInformationWriter("                File (" + minRange + ") to (" + maxRange + ")\n");
              mqlLogRequiredInformationWriter("                Reading files from: " + documentDirectory + "\n");
              mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
              mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
              mqlLogRequiredInformationWriter("=======================================================\n\n");
              migrationStartTime = System.currentTimeMillis();
              boolean migrationStatus = true;
              statusBuffer.append("File Name, Status, Object Failed (OR) Time Taken in MilliSec\n");
              for( i = minRange;i <= maxRange; i++)
              {
                  try
                  {
                      ContextUtil.startTransaction(context,true);
                      mqlLogWriter("Reading file: " + i + "\n");
                      StringList objectList = new StringList();
                      migratedOids = new StringBuffer(20000);
                      try
                      {
                          objectList = readFiles(i);
                      }
                      catch(FileNotFoundException fnfExp)
                      {
                          // throw exception if file does not exists
                          throw fnfExp;
                      }
                      loadInterOpMigrations(context);
                      migrateObjects(context, objectList);
                      if (!scan) {
                    	 
                    	  ContextUtil.commitTransaction(context);
                      } else {
                    	  ContextUtil.abortTransaction(context);
                      }
                    	  
                      logMigratedOids();
                      mqlLogRequiredInformationWriter("<<< Time taken for migration of objects & write ConvertedOid.txt for file in milliseconds :" + documentDirectory + "objectids_" + i + ".txt"+ ":=" +(System.currentTimeMillis() - startTime) + ">>>\n");

                      // write after completion of each file
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      mqlLogRequiredInformationWriter("Migration of Objects in file objectids_" + i + ".txt COMPLETE \n");
                      statusBuffer.append("objectids_");
                      statusBuffer.append(i);
                      statusBuffer.append(".txt,COMPLETE,");
                      statusBuffer.append((System.currentTimeMillis() - startTime));
                      statusBuffer.append("\n");
                      mqlLogRequiredInformationWriter("=================================================================\n");
                  }
                  catch(FileNotFoundException fnExp)
                  {
                      // log the error and proceed with migration for remaining files
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      mqlLogRequiredInformationWriter("File objectids_" + i + ".txt does not exist \n");
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      ContextUtil.abortTransaction(context);
                       migrationStatus = false;
                  }
                  catch (Exception exp)
                  {
                      // abort if identifyModel or migration fail for a specific file
                      // continue the migration process for the remaining files
                      mqlLogRequiredInformationWriter("=======================================================\n");
                      mqlLogRequiredInformationWriter("Migration of Objects in file objectids_" + i + ".txt FAILED \n");
                      mqlLogRequiredInformationWriter("=="+ exp.getMessage() +"==\n");
                      mqlLogRequiredInformationWriter("=======================================================\n");
                      statusBuffer.append("objectids_");
                      statusBuffer.append(i);
                      statusBuffer.append(".txt,FAILED,");
                      statusBuffer.append(failureId);
                      statusBuffer.append("\n");
                      exp.printStackTrace();
                      ContextUtil.abortTransaction(context);
                      migrationStatus = false;

                  }
              }

              mqlLogRequiredInformationWriter("=======================================================\n");
              if(migrationStatus){
              	mqlLogRequiredInformationWriter("                Step 2 of Migration COMPLETED SUCCESSFULLY\n");
              }else{
              	mqlLogRequiredInformationWriter("                Step 2 of Migration INCOMPLETE\n");
              	mqlLogRequiredInformationWriter("Please check the logs for further details on failures\n");
              	mqlLogRequiredInformationWriter("There could be failures in migrating objects in one or more files\n");
		      }
              mqlLogRequiredInformationWriter("                Time: " + (System.currentTimeMillis() - migrationStartTime) + " ms\n");
              mqlLogRequiredInformationWriter(" \n");
              mqlLogRequiredInformationWriter(" \n");
              mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
              mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
              mqlLogRequiredInformationWriter("=======================================================\n");
          }
          catch (Exception ex)
          {
              mqlLogRequiredInformationWriter("=======================================================\n");
              mqlLogRequiredInformationWriter("Step 2 of Migration     : INCOMPLETE \n");
              mqlLogRequiredInformationWriter("=======================================================\n");
              ex.printStackTrace();
              ContextUtil.abortTransaction(context);
          }
          finally
          {
              mqlLogRequiredInformationWriter("<<< Total time taken for migration in milliseconds :=" + (System.currentTimeMillis() - migrationStartTime) + ">>>\n");
              String cmd = "trigger on";
              MqlUtil.mqlCommand(context, mqlCommand,  cmd);

              statusLog   = new FileWriter(documentDirectory + "fileStatus.csv", true);
              statusLog.write(statusBuffer.toString());
              statusLog.flush();
              statusLog.close();

              ContextUtil.popContext(context);
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              errorLog.close();
              warningLog.close();
              convertedOidsLog.close();
          }

          // always return 0, even this gives an impression as success
          // this way, matrixWriter writes to console
          // else writer.write statements do not show up in Application console
          // but it works in mql console
          return 0;
      }

      public void migrateObjects(Context context, StringList objectList) throws Exception
      {
      }

      /**
       * This method goes thru all the Objects in files
       * but does NOT migrate any, but finds all the unConvertable Objects to the file
       * written to provide a way to see all the unConvertable Objects before
       * running the migration
       *
       * @param context the eMatrix <code>Context</code> object
       * @param writer - MatrixWriter object sent from calling JPO.
       * @param args - Context, directory name where files exist, Minimum range, Maximum range
       * @throws Exception if the operation fails
       */
      public void scanObjects(Context context, Map map) throws Exception
      {
          writer = (BufferedWriter)map.get("writer");
          String[] args = (String[])map.get("args");

          scan = true;
          mxMain(context, args);

          return;
      }

      /**
       * This method returns the total number of files in the directory.
       *
       * @returns int of total files present in the directory
       * @throws Exception if the operation fails
       */
      public int getTotalFilesInDirectory() throws Exception
      {
          int totalFiles = 0;
          try
          {
              String[] fileNames = null;
              java.io.File file = new java.io.File(documentDirectory);
              if(file.isDirectory())
              {
                  fileNames = file.list();
              } else {
                  throw new IllegalArgumentException();
              }
              for (int i=0; i<fileNames.length; i++)
              {
                  if(fileNames[i].startsWith("objectids_"))
                  {
                      totalFiles = totalFiles + 1;
                  }
              }
          }
          catch(Exception fExp)
          {
              // check if user has access to the directory
              // check if directory exists
              error = "Directory does not exist or does not have access to the directory";
              throw fExp;
          }

          return totalFiles;
      }

      /**
       * This method reads the contents of the file and puts in Arraylist.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args i holds the suffux of filename to identify the file.
       * @returns ArrayList of objectIds present in the file
       * @throws Exception if the operation fails
       */
      public StringList readFiles(int i) throws Exception
      {
          String objectId = "";
          StringList objectIds = new StringList();
          try
          {
              java.io.File file = new java.io.File(documentDirectory + "objectids_" + i + ".txt");
              BufferedReader fileReader = new BufferedReader(new FileReader(file));
              while((objectId = fileReader.readLine()) != null)
              {
                objectIds.add(objectId);
              }
          }
          catch(FileNotFoundException fExp)
          {
              throw fExp;
          }
          return objectIds;
      }


      /**
       * This method reads the contents of the file and puts in Arraylist.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args i holds the suffux of filename to identify the file.
       * @returns ArrayList of objectIds present in the file
       * @throws Exception if the operation fails
       */
      public StringList readFiles(String fileName) throws Exception
      {
          String objectId = "";
          StringList objectIds = new StringList();
          try
          {
              java.io.File file = new java.io.File(documentDirectory + fileName);
              BufferedReader fileReader = new BufferedReader(new FileReader(file));
              while((objectId = fileReader.readLine()) != null)
              {
                objectIds.add(objectId);
              }
          }
          catch(FileNotFoundException fExp)
          {
              throw fExp;
          }
          return objectIds;
      }


      public void loadMigratedOids (String objectId) throws Exception
      {
          		  String newLine = System.getProperty("line.separator");
		  migratedOids.append(objectId + newLine);
		  //migratedOids.append(objectId + "\n");
      }

      protected void logMigratedOids() throws Exception
      {
          convertedOidsLog.write(migratedOids.toString());
          convertedOidsLog.flush();
      }

      private void logWarning (String message) throws Exception
      {
          warningLog.write( message );
          warningLog.flush();
      }
      private void logWarning (String message, Map map) throws Exception
      {
          if ( debug)
          {
              writer.write("!!! WARNING !!! Object TNRV = " + map.get(DomainObject.SELECT_TYPE) +
                                 " " + map.get(DomainObject.SELECT_NAME) +
                                 " " + map.get(DomainObject.SELECT_REVISION) +
                                 " " + map.get(DomainObject.SELECT_VAULT) + "\n" +
                                 " Id:" + map.get(DomainObject.SELECT_ID) + "\n");
              writer.write("Above Object has Following warning \n" + message + "\n");
              writer.flush();

              warningLog.write("!!! WARNING !!! Object TNRV = " + map.get(DomainObject.SELECT_TYPE) +
                                 " " + map.get(DomainObject.SELECT_NAME) +
                                 " " + map.get(DomainObject.SELECT_REVISION) +
                                 " " + map.get(DomainObject.SELECT_VAULT) + "\n" +
                                 " Id:" + map.get(DomainObject.SELECT_ID) + "\n");
              warningLog.write("Above Object has Following warning \n" + message + "\n");
              warningLog.write("\n");
              warningLog.flush();
          }
      }


    /**
     * This method writes the objectId to the sequential file, called from within JPO query where clause
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[]  - [0]ObjectId, [1]type
     * @returns boolean
     * @throws Exception if the operation fails
     */
    public boolean writeOID(Context context, String[] args) throws Exception
    {
    	String writeIdStr = writeObjectId(context, args); 
    	if ( writeIdStr != null && !"".equals(writeIdStr) )
        {
        	fileWriter(writeIdStr);
        }
        return false;
    }
    public String writeObjectId(Context context, String[] args) throws Exception
    {
    	StringList EXCLUDED_TYPES = new StringList(5);
    	String excludedTypes = "";
    	try{
	    	EnoviaResourceBundle.getProperty(context, "emxCommonMigration.Exclude.Types");
	    	String[] excludedTypeArray = excludedTypes.split(",");
	    	EXCLUDED_TYPES = new StringList(excludedTypeArray.length);
	    	 for (int i=0; i< excludedTypeArray.length; i++)
	         {
	             EXCLUDED_TYPES.addElement(PropertyUtil.getSchemaProperty(context,(String)excludedTypeArray[i]));
	         }
    	}catch(Exception ex) {
            EXCLUDED_TYPES = new StringList();
        }
        String type = args[1];
        if ( !EXCLUDED_TYPES.contains(type) )
        {
        	return args[0];
        } else {
        	return null;
        }
    }
    
    public void fileWriter( String oid) throws Exception 
    {
        _objectidList.addElement(oid);
            _counter++;

            if (_counter == _chunk)
            {
                _counter=0;
                _sequence++;

                //write oid from _objectidList
                for (int s=0;s<_objectidList.size();s++)
                {
                    _fileWriter.write((String)_objectidList.elementAt(s));
                    _fileWriter.newLine();
                }

                _objectidList=new StringList();
                _fileWriter.close();

                //create new file
                _oidsFile = new java.io.File(documentDirectory + "objectids_" + _sequence + ".txt");
                _fileWriter = new BufferedWriter(new FileWriter(_oidsFile));
            }
    }

    /**
     * This method takes care of leftover objectIds which do add up to the limit specified
     *
     * @param none
     * @returns none
     * @throws Exception if the operation fails
     */
    public static void cleanup() throws Exception
    {
        try
        {
            if(_objectidList != null && _objectidList.size() > 0)
            {
                for (int s=0;s<_objectidList.size();s++)
                {
                  _fileWriter.write((String)_objectidList.elementAt(s));
                  _fileWriter.newLine();
                }
                _fileWriter.close();
            }
            else
            {
                // delete the empty file created
                _fileWriter.close();
                _oidsFile.delete();
            }
        }
        catch(Exception Exp)
        {
            throw Exp;
        }
    }

    /**
     * Fetches and maintains a cache of external methods to call for custom migrations.
     * The custom migration methods will be called during the internal migration process.
     *
     * @param context the ENOVIA <code>Context</code> object
     * @return none
     * @throws Exception if the operation fails
     */

    public void loadInterOpMigrations (Context context) throws Exception
    {
        //handle interoperability using command object
        if (_interOpCommand == null || _interOpCommand.equals("null") || _interOpCommand.equals(""))
        {
            //no interOpCommand name set, nothing to do
            return;
        }
        HashMap cmdMap = UICache.getCommand(context, PropertyUtil.getSchemaProperty(context,_interOpCommand));
        if (cmdMap != null && !"null".equals(cmdMap) && cmdMap.size() > 0)
        {
            HashMap settingsList = (HashMap) cmdMap.get("settings");
            if (settingsList != null && !"null".equals(settingsList) && settingsList.size() > 0)
            {
                Iterator settingsKeyIterator = settingsList.keySet().iterator();
                while (settingsKeyIterator.hasNext())
                {
                    String settingName = (String)settingsKeyIterator.next();
                    String settingsValue = (String)settingsList.get(settingName);
                    _interOpMethods.put(settingName,settingsValue);
                }
            }
        }        
    }

    /**
     * Calls the external methods for custom migrations.
     *
     * @param context the ENOVIA <code>Context</code> object
     * @param objectList MapList of data to pass to the external migration method.  
     * @return MapList of data specifying 
     * @throws Exception if the operation fails
     */

    public MapList callInterOpMigrations (Context context, MapList objectList) throws Exception
    {
    	String programMethod = "";
        String strProgram = "";
        String strMethod = "";
        String mapKey = "";
        Map resultsMap = null;
        HashMap paramMap = null;
        String className = null;
        MapList resultsList = new MapList();
        Iterator keyIterator = _interOpMethods.keySet().iterator();
        while (keyIterator.hasNext())
        {
            mapKey = (String)keyIterator.next();
            programMethod = (String)_interOpMethods.get(mapKey); 
            strProgram = "";
            strMethod = "";
            StringTokenizer tokens = new StringTokenizer(programMethod, ":");
            if (tokens.hasMoreTokens())
            {
                strProgram = tokens.nextToken();
                strMethod = tokens.nextToken();
            }
            else
            {
                break;
            }

            paramMap = new HashMap();
            paramMap.put("objectList", objectList);

            String[] methodargs = JPO.packArgs(paramMap);
            StringBuffer command = new StringBuffer();
            //no need to get mangled name, use program name directly
            //command.append("print program '");
            //command.append(strProgram);
            //command.append("' select classname dump |");

            //className = MqlUtil.mqlCommand(context, command.toString());
            className = strProgram;
            try
            {
            	resultsMap = (Map)JPO.invoke(context, className, new String[0], strMethod, methodargs, Map.class);
                if (resultsMap != null)
                {
                    resultsMap.put("name", mapKey);
                }
                resultsList.add(resultsMap);
                //Log custom migration results
                //String status = (String)resultsMap.get("status");
                Integer status = (Integer)resultsMap.get("status");
                if (status==0)
                {
                    mqlLogRequiredInformationWriter("Custom Migration '" + mapKey + "' COMPLETED\n");
                }
                else if (status==1)
                {
                    mqlLogRequiredInformationWriter("Custom Migration '" + mapKey + "' FAILED\n");
                    //String failureId = (String)resultsMap.get("failureId");
                    //String failureMsg = (String)resultsMap.get("failureMessage");
                    //writeUnconvertedOID(failureMsg, failureId);
                }                
            }
            catch (Throwable err)
            {
                mqlLogRequiredInformationWriter(err.getMessage());
                
            }
        }
       
        return resultsList;
    }
    
    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        }

        writer.write("================================================================================================\n");
        writer.write(" Migration is a two step process  \n");
        writer.write(" Step1: Find all objects derived from DOCUMENTS and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonFindObjects 1000 Document C:/Temp/oids/ emxCommonMigration name revision; \n");
        writer.write(" First parameter  = 1000 indicates no of oids per file \n");
        writer.write(" Second Parameter = Document is the type of the object should be found \n");
        writer.write(" Third parameter  = C:/Temp/oids/ is the directory where files should be written  \n");
        //writer.write(" Optional Fourth Parameter = emxCommonMigration is the program where can should be written \n");
        //writer.write(" Optional Fifth parameter  = Name is the nmae of the object should be found \n");
        //writer.write(" Optional Sixth Parameter = Revision is the revision of the object should be found \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = C:/Temp/oids/ directory to read the files from\n");
        writer.write(" Second Parameter = 1 minimum range  \n");
        writer.write(" Third Parameter  = n minimum range  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write(" Fourth Parameter is Optional if sent as 'true', will convert orphaned -  \n");
        writer.write("          objects without any revisions to ECModel\n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonMigration 'C:/Temp/oids/' 1 n true; \n");
        writer.write(" \n");
        writer.write(" Optional Step2: \n");
        writer.write(" execute program emxCommonFindUnConvertableObjects 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" The above command just scans all the files for any UnConvertable objects  \n");
        writer.write("     and writes to unConvertedObjectIds.csv without migrating any \n");
        writer.write(" Steps for Custom Migration: \n");
        writer.write(" execute program emxCommonMigration -method customMigration 'c:/Temp/oids/' file1.txt \n");
        writer.write(" First parameter  = C:/Temp/oids/ directory to read the files from\n");
        writer.write(" Second Parameter = file1.txt - file that has objectids to be migrated  \n");
        writer.write(" Third Parameter is Optional - to force run specific migration method on all objects in the file  \n");
        writer.write("          TeamSourcingModel        - to force run method migrateTeamSourcingModel \n");
        writer.write("          PMCModel                 - to force run method migratePMCModel  \n");
        writer.write("          DocumentProductSpecModel - to force run method migrateDocumentProductSpecModel  \n");
        writer.write("          EcModel                  - to force run method migrateEcModel  \n");
        writer.write("          IEFModel                 - to force run method migrateIEFModel \n");
        writer.write("          default                  - to force run method migrateCustomObject \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonMigration -method customMigration 'c:/Temp/oids/' file1.txt EcModel\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
    
    /**
     * Writes the command string to the unConvertedObjectIds_xx.csv log file 
     * and writes the objectId to the unConvertedObjectIds_xx.txt file.
     *
     * @param command specifies the value to write to the CSV file  
     * @param objectId object Id for the object that cannot be converted  
     * @return nothing 
     * @throws Exception if the operation fails
     */
    public void writeUnconvertedOID(String command,String objectId) throws Exception
    {
        if(unconvertedObjectCount < unconvertedChunkSize)
        {
            errorLog.write(command);
            errorLog.flush();
            unconvertedObjectCount++;
            if (objectId != null)
            {
                String newLine = System.getProperty("line.separator");
                unconvertedOidsLog.write(objectId+newLine);
                unconvertedOidsLog.flush();
            }
        } else {
            errorLog.close();
            unconvertedObjectCount = 1;
            errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", true);
            unconvertedFileCount ++;
            errorLog.write("TYPE,NAME,REVISION,CLASSIFICATION\n");
            errorLog.write(command);
            errorLog.flush();
            if (objectId != null)
            {
                unconvertedOidsLog.close();
                unconvertedOidsLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".txt", true);
                String newLine = System.getProperty("line.separator");
                unconvertedOidsLog.write(objectId+newLine);
                unconvertedOidsLog.flush();
             }            
        }
    }
    
    
    
    /**
     * Loads the specified resource file and maintains local cache for properties.
     *
     * @param context the ENOVIA <code>Context</code> object
     * @param resourceFileName specifies the name of the resource file
     * @throws Exception if the operation fails
     */
    public void loadResourceFile(Context context, String resourceFileName) throws Exception
    {
    	try
    	{
        	if (_propertyResource == null)
        	{
        	    _propertyResource = EnoviaResourceBundle.getBundle(context, resourceFileName, context.getLocale().getLanguage());
        	}  		
    	}
    	catch (Exception ex)
    	{
    		throw new Exception(ex.getMessage());
    	}
    }

    /**
     * Gets the property value for the specified key from the local migration property cache.
     *
     * @param context the ENOVIA <code>Context</code> object
     * @param propertyKey String specifying the property key
     * @throws Exception if the operation fails
     */
    public String getResourceProperty(Context context, String propertyKey) throws Exception
    {
    	String propertyValue = null;
    	try
    	{
    		if (_propertyResource == null)
    		{
    			throw new Exception ("Resource Property has not been loaded.  Call loadResourceFile method first.");
    		}
    		propertyValue = _propertyResource.getString(propertyKey);
    	}
    	catch (Exception ex)
    	{
    		throw new Exception(ex.getMessage());
    	}
    	if (propertyValue == null)
    	{
            throw new Exception("Missing resource for key '" + propertyKey + "'");
    	}
    	return propertyValue;
    }
    
    public void createLogs()throws Exception{
    	errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", false);
    	unconvertedOidsLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".txt", false);
    	unconvertedFileCount ++;
    	errorLog.write("TYPE,NAME,REVISION,CLASSIFICATION \n");
    	errorLog.flush();
    	convertedOidsLog    = new FileWriter(documentDirectory + "convertedIds.txt", false);
    	warningLog = new FileWriter(documentDirectory + "migration.log", false);
    }
}
