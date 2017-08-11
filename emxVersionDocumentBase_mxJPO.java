/**
 * Copyright (c) 1992-2016 Dassault Systemes. All Rights Reserved. This program
 * contains proprietary and trade secret information of MatrixOne,Inc.
 * Copyright notice is precautionary only and does not evidence any actual or
 * intended publication of such program.
 *
 * FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 * Author   : Kevin H. Olson
 * Version  : "$Revision: 1.16 $"
 * Date     : "$Date: Wed Oct 22 16:02:09 2008 $"
 * static const char RCSID[] = "$Id: ${CLASSNAME}.java.rca 1.16 Wed Oct 22 16:02:09 2008 przemek Experimental przemek $"
 *
 */


import com.matrixone.apps.domain.DomainObject;
import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  to represent Version Document
 *
 *  @exclude
 *
 */

public class emxVersionDocumentBase_mxJPO extends DomainObject
{

  /**
   * Constructor
   * @param - no arguments
   *
   */

  public emxVersionDocumentBase_mxJPO ()
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

  public emxVersionDocumentBase_mxJPO (Context context, String[] args)
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

  public emxVersionDocumentBase_mxJPO (String containerDocId) throws Exception
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

} //class
