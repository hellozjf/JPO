//
// $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 16:02:24 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.9 $"
 *  Date     : "$Date: Wed Oct 22 16:02:24 2008 $"
 *
 */


import matrix.db.Context;
import matrix.db.JPO;

/**
 *  The <code>${CLASSNAME}</code> extends the Base JPO class used
 *  Copyright (c) 2002, MatrixOne, Inc.
 *
 */

public class emxApplicationDocumentCentral_mxJPO extends  emxApplicationDocumentCentralBase_mxJPO
{
   private static final String THIS_FILE = "emxApplicationBase";

   protected static boolean _bInitialized = false;

   public static final String  _baseAppKey
      = "eServiceSuiteDocumentCentral.BosServer.";

   public emxApplicationDocumentCentral_mxJPO ()
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

   public emxApplicationDocumentCentral_mxJPO (Context context, String[] args)
       throws Exception
   {
      super ();
   }

}
