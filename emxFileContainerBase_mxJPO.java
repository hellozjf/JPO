//
// $Id: ${CLASSNAME}.java.rca 1.15 Wed Oct 22 16:02:39 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.15 $"
 *  Date     : "$Date: Wed Oct 22 16:02:39 2008 $"
 *
 */

import com.matrixone.apps.domain.DomainObject;
import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  as DocumentCentral FileContainer object to hold File Information
 * 
 * @exclude
 *
 */

public class emxFileContainerBase_mxJPO extends DomainObject
{

  protected static final String THIS_FILE = "emxFileContainerBase";

  public emxFileContainerBase_mxJPO ()
  {
    super();

  }

  /**
   *  Constructs a new JPO object.
   *
   *  @param context the eMatrix <code>Context</code> object
   *  @param args contains object id
   *  @throws Exception if the operation fails
   *
   *  @since AEF 9.5.6.0
   */

  public emxFileContainerBase_mxJPO (Context context, String[] args)
    throws Exception
    {
      super (args[0]);

      if ((args != null) && (args.length > 0))
      {
        setId (args[0]);
      }
    }

  /**
   * Constructor
   *
   * @param id the Java <code>String</code> object
   *
   * @throws Exception if the operation fails
   *
   */

  public emxFileContainerBase_mxJPO (String id) throws Exception
    {
      // Call the super constructor

      super (id);
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
   * @since AEF 9.5.0.0
   */
  public int mxMain (Context context, String[] args) throws Exception
    {
      if (true)
        throw new Exception ("Don't use this! " + THIS_FILE);
      return 0;
    }

} //class
