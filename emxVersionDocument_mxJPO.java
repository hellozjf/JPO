/* emxVersionDocumentBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 16:02:21 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  to represent Version Document
 *
 *  @version AEF 9.5.6.0 - Copyright (c) 2002, MatrixOne, Inc.
 *
 */

public class emxVersionDocument_mxJPO extends emxVersionDocumentBase_mxJPO
{
  /**
   * Constructor
   * @param - no arguments
   *
   */

  public emxVersionDocument_mxJPO ()
    {
      super();
    }

  /**
   * Constructor
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   *
   * @throws Exception if the operation fails
   */

  public emxVersionDocument_mxJPO (Context context, String[] args)
                        throws Exception
    {
      super (args[0]);
    }

  /**
   * Constructor
   *
   * @param containerDocId the Java <code>String</code> object
   *
   * @throws Exception if the operation fails
   *
   */

  public emxVersionDocument_mxJPO (String containerDocId) throws Exception
    {
      // Call the super constructor

      super (containerDocId);
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
        throw new Exception ("Don't call this!  Why do you think its here?");
      return 0;
    }


}
