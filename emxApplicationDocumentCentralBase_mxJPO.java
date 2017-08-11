//
// $Id: ${CLASSNAME}.java.rca 1.12 Wed Oct 22 16:02:22 2008 przemek Experimental przemek $ 
//
/**
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of
 *  MatrixOne,Inc.
 *  Copyright notice is precautionary only and does not evidence any
 *  actual or intended publication of such program.
 *
 *  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *  Author   : "$Author: przemek $"
 *  Version  : "$Revision: 1.12 $"
 *  Date     : "$Date: Wed Oct 22 16:02:22 2008 $"
 *
 */

import com.matrixone.apps.document.util.MxSettings;
import com.matrixone.apps.document.util.MxDebug;
import com.matrixone.apps.document.util.FlowControlException;

import com.matrixone.apps.domain.util.FrameworkException;

import matrix.db.Context;

import matrix.db.JPO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;

/**
 *  The <code>${CLASSNAME}</code> is DocumentCentral Application init code
 *  Copyright (c) 2002, MatrixOne, Inc.
 *
 * @exclude
 */

public class emxApplicationDocumentCentralBase_mxJPO
{
   private static final String THIS_FILE = "emxApplicationBase";

   protected static boolean _bInitialized = false;

   public static final String  _baseAppKey = "eServiceSuiteDocumentCentral.BosServer.";

   public emxApplicationDocumentCentralBase_mxJPO ()
   {
      super();
   }

   /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args the Java <code>String[]</code> object
    *
    * @throws Exception if the operation fails
    *
    */

   public emxApplicationDocumentCentralBase_mxJPO (Context context, String[] args)
       throws Exception
   {
      super ();
   }


   /**
    * This method is executed if a specific method is not specified.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args the Java <String[]</code> object
    *
    * @return the Java <code>int</code>
    *
    * @throws Exception if the operation fails
    *
    * @since AEF 9.5.7.0
    */
   public int mxMain (Context context, String[] args) throws Exception
   {
      if (true)
         throw new Exception ("must specify method on emxApplicationDocumentCentralBase invocation");
      return 0;
   }


  /**
    * Generates Logs
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args the Java <code>String<code> array object
    *
    */
  public void proveMyExistence(Context context, String[] args)
  {
     MxDebug.enter ();
     try
     {
     }
     catch (Exception e)
     {
     }
     MxDebug.exit ();
  }


  /**
   * Program init code
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args the Java <code>String<code> array object
   * @throws FrameworkException if the operation fails
   *
   * @since  AEF 9.5.6.0
   *
   */
  public static void initialize (Context  context,
                                 String[]  args)
     throws FrameworkException
  {
     /**
      *  Author   : Brian R. Tibbetts
      *  Date     : 2003/04/04
      *  Notes    :
      *  History  :
      */

      try
      {
         if (_bInitialized)
         {
             throw new FlowControlException ();
         }

         MxSettings.init ("emxDocumentCentral");
         String fileDirName = null;
         fileDirName = MxSettings.getValue (_baseAppKey + "Trace.FilePath");
         File dbgFile = null;
         try
         {
            if (fileDirName != null && fileDirName.length () > 0)
            {
               File dbgDir  = new File (fileDirName);
               if (dbgDir.exists ())
               {
                  dbgFile  = File.createTempFile ("DocumentCentral",".dbg",dbgDir);
                  MxDebug.message (MxDebug.DL_Info,"Created the debug logging file: "+ dbgFile.getAbsolutePath ());
               }

            }
         }
         catch (Exception e)
         {
            e.printStackTrace ();
         }

         MxDebug.init (dbgFile,dbgFile == null ? true : false,_baseAppKey + "DebugLevel");

         _bInitialized = true;
      }
      catch (FlowControlException fce)
      { }
      catch (Exception e)
      {
         _bInitialized = false;
         e.printStackTrace ();
         throw new FrameworkException (e);
      }

  }  //initialize ()

}
