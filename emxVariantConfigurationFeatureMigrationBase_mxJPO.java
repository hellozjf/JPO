/*
 * emxVariantConfigurationFeatureMigrationBase.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = "$Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$"
 */

  import matrix.db.*;
  import matrix.util.*;

  import java.io.*;
  import java.util.*;
  import java.text.*;
  import com.matrixone.apps.domain.*;
  import com.matrixone.apps.domain.util.*;
  import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.configuration.ConfigurationConstants;

  public class emxVariantConfigurationFeatureMigrationBase_mxJPO extends emxDomainObject_mxJPO
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

      static String documentDirectory = "";
      static int minRange = 0;
      static int maxRange = 0;


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
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxVariantConfigurationFeatureMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
          super(context, args);
          writer     = new BufferedWriter(new MatrixWriter(context));
        mqlCommand = MqlUtil.getMQL(context);
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
          if(unconvertedObjectCount < unconvertedChunkSize)
          {
              errorLog.write(command);
              errorLog.flush();
              unconvertedObjectCount++;
          } else {
              errorLog.close();
              unconvertedObjectCount = 1;
              errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", true);
              unconvertedFileCount ++;
              errorLog.write("TYPE,NAME,REVISION,CLASSIFICATION\n");
              errorLog.write(command);
              errorLog.flush();
          }
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
              throw new Exception("not supported on desktop client");
          }
          int argsLength = args.length;
          error = "";


          try
          {
              // writer     = new BufferedWriter(new MatrixWriter(context));
              if (args.length < 3 )
              {
                  error = "Wrong number of arguments";
                  throw new IllegalArgumentException();
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
              } else {
                maxRange = Integer.parseInt(args[2]);
              }

              if (minRange > maxRange)
              {
                error = "Invalid range for arguments, minimum is greater than maximum range value";
                throw new IllegalArgumentException();
              }

              if (minRange == 0 || minRange < 1 || maxRange == 0 || maxRange < 1)
              {
                error = "Invalid range for arguments, minimum/maximum range value is 0 or negative";
                throw new IllegalArgumentException();
              }
          }
          catch (IllegalArgumentException iExp)
          {
              writer.write("====================================================================\n");
              writer.write(error + " \n");
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
          try
          {
              errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", true);
              unconvertedFileCount ++;
              errorLog.write("MASTER OID,TYPE,NAME,REVISION,CLASSIFICATION,VERSION OID,LOCKER\n");
              errorLog.flush();
              convertedOidsLog    = new FileWriter(documentDirectory + "convertedIds.txt", true);
              warningLog = new FileWriter(documentDirectory + "migration.log", true);
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
                      migrateObjects(context, objectList);
                      ContextUtil.commitTransaction(context);
                      logMigratedOids();
                      mqlLogRequiredInformationWriter("<<< Time taken for migration of objects & write ConvertedOid.txt for file in milliseconds :" + documentDirectory + "objectids_" + i + ".txt"+ ":=" +(System.currentTimeMillis() - startTime) + ">>>\n");

                      // write after completion of each file
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      mqlLogRequiredInformationWriter("Migration of Documents in file objectids_" + i + ".txt COMPLETE \n");
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
                  }
                  catch (Exception exp)
                  {
                      // abort if identifyModel or migration fail for a specific file
                      // continue the migration process for the remaining files
                      mqlLogRequiredInformationWriter("=======================================================\n");
                      mqlLogRequiredInformationWriter("Migration of Documents in file objectids_" + i + ".txt FAILED \n");
                      mqlLogRequiredInformationWriter("=="+ exp.getMessage() +"==\n");
                      mqlLogRequiredInformationWriter("=======================================================\n");
                      statusBuffer.append("objectids_");
                      statusBuffer.append(i);
                      statusBuffer.append(".txt,FAILED,");
                      statusBuffer.append(failureId);
                      statusBuffer.append("\n");
                      exp.printStackTrace();
                      ContextUtil.abortTransaction(context);

                  }
              }

              mqlLogRequiredInformationWriter("=======================================================\n");
              mqlLogRequiredInformationWriter("                Migrating Document Objects  COMPLETE\n");
              mqlLogRequiredInformationWriter("                Time: " + (System.currentTimeMillis() - migrationStartTime) + " ms\n");
              mqlLogRequiredInformationWriter(" \n");
              mqlLogRequiredInformationWriter("Step 2 of Migration :     SUCCESS \n");
              mqlLogRequiredInformationWriter(" \n");
              mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
              mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
              mqlLogRequiredInformationWriter("=======================================================\n");
          }
          catch (FileNotFoundException fEx)
          {
              ContextUtil.abortTransaction(context);
          }
          catch (Exception ex)
          {
              // abort if identifyModel fail
              mqlLogRequiredInformationWriter("=======================================================\n");
              mqlLogRequiredInformationWriter("Migration of Documents in file objectids_" + i + ".txt failed \n");
              mqlLogRequiredInformationWriter("Step 2 of Migration     : FAILED \n");
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

     /**
     * This method goes through all objects which will
     * be migrated. It will get count of subfeatures of
     * particular feature and update that as attribute value
     * for attribute 'Sub Feature Count'
     * @param context the eMatrix <code>Context</code> object
     * @param objectList contains the list of objects to be migrated.
     * @throws Exception
     */
    public void migrateObjects(Context context, StringList objectList) throws Exception
      {
          String  strDestAttribute = ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT;
          String strAttributeValue = null;
          String strObjectId = null;
          DomainObject dmoObject = DomainObject.newInstance (context);
          Object[] vDmnObjectIds = objectList.toArray();
          StringList slSubFtrLst = new StringList();
          for (int i=0; i<vDmnObjectIds.length; i++) {
              strObjectId = (String) vDmnObjectIds[i];
              dmoObject.setId (strObjectId);
              slSubFtrLst = dmoObject.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_FEATURE_LIST_FROM+"].id");
              Integer subFtrCount = new Integer(slSubFtrLst.size());
              strAttributeValue = subFtrCount.toString();
              dmoObject.setAttributeValue (context, strDestAttribute, strAttributeValue);
          }
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


      private void logMigratedOids() throws Exception
      {
          convertedOidsLog.write(migratedOids.toString());
          convertedOidsLog.flush();
      }

      private void logWarning (String message) throws Exception
      {
          warningLog.write( message );
          warningLog.flush();
      }
    

    public static StringList EXCLUDED_TYPES = new StringList(5);
    static
    {
        try
        {
            String excludedTypes = FrameworkProperties.getProperty("emxCommonMigration.Exclude.Types");
            StringList excludedTypeList = FrameworkUtil.split(excludedTypes,",");
            EXCLUDED_TYPES = new StringList(excludedTypeList.size());
            for (int i=0; i< excludedTypeList.size(); i++)
            {
                EXCLUDED_TYPES.add(PropertyUtil.getSchemaProperty((String)excludedTypeList.get(i)));
            }
        } catch(Exception ex) {
            EXCLUDED_TYPES = new StringList();
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
        String type = args[1];
        if ( !EXCLUDED_TYPES.contains(type) )
        {
            _objectidList.add(args[0]);
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

        return false;
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
                //_fileWriter.close();
                //_oidsFile.delete();
            }
        }
        catch(Exception Exp)
        {
            throw Exp;
        }
    }


    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" Migration is a two step process  \n");
        writer.write(" Step1: Find all objects derived from FEATURES and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxVariantConfigurationFeatureFindObjects 1000 Document C:/Temp/oids/ emxVariantConfigurationFeatureMigration name revision; \n");
        writer.write(" First parameter  = 1000 indicates no of oids per file \n");
        writer.write(" Second Parameter = Document is the type of the object should be found \n");
        writer.write(" Third parameter  = C:/Temp/oids/ is the directory where files should be written  \n");
        writer.write(" Optional Fourth Parameter = emxVariantConfigurationFeatureMigration is the program where can should be written \n");
        writer.write(" Optional Fifth parameter  = Name is the nmae of the object should be found \n");
        writer.write(" Optional Sixth Parameter = Revision is the revision of the object should be found \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxVariantConfigurationFeatureMigration 'C:/Temp/oids/' 1 n ; \n");
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
}
