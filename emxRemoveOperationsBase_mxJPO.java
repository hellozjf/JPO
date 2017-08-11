/*
 *   Copyright (c) 1992-2016 Dassault Systemes.
 *   All Rights Reserved.
 *   This program contains proprietary and trade secret information of
 *   MatrixOne, Inc.  Copyright notice is precautionary only
 *   and does not evidence any actual or intended publication of such program
 *
 *   FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *   Author   : Anil KJ
 *   Version  : "$Revision: 1.22 $"
 *   Date     : "$Date: Wed Oct 22 16:02:25 2008 $"
 *
 *   staic const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.22 Wed Oct 22 16:02:25 2008 przemek Experimental przemek $";
 */

/*
 * This JPO is @deprecated from V6R2013 onwords
 */


import matrix.db.Context;

import com.matrixone.apps.document.DocumentCentralConstants;
import com.matrixone.apps.document.FileOpConstants;
import com.matrixone.apps.domain.DomainObject;
/**
 *  The <code>${CLASSNAME}</code> class is used  to Remove Operations
 *
 *  @exclude
 *
 */
public class emxRemoveOperationsBase_mxJPO extends DomainObject
  implements DocumentCentralConstants, FileOpConstants
{
    private static final String THIS_FILE = "emxRemoveOperationsBase";
    /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args the Java <code>String[]</code> object
       *
       * @throws Exception if the operation fails
       *
       * @since AEF 9.5.0.0
       */
  public emxRemoveOperationsBase_mxJPO (Context context,
                       String[] args) throws Exception
  {
    super (args[0]);
  }
  /**
   * Constructor
   *
   * @param id the Java <code>String</code> object
   *
   * @throws Exception if the operation fails
   *
   */

  public emxRemoveOperationsBase_mxJPO (String id)
                        throws Exception
  {
    super (id);
  }
  /**
   * This method is executed if a specific method is not specified.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   *
   * @return the Java <code>int</code>
   *
   * @throws Exception if the operation fails
   *
   * @since AEF 9.5.0.0
   */
  public int mxMain (Context context, String[] args) throws Exception
  {
    if (true)
      throw new Exception ("Don't use this! " + THIS_FILE);
    return 0;
  }
}
