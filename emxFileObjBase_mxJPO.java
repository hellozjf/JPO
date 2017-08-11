//
// $Id: ${CLASSNAME}.java.rca 1.12 Wed Oct 22 16:02:25 2008 przemek Experimental przemek $ 
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
 *  Date     : "$Date: Wed Oct 22 16:02:25 2008 $"
 *
 */

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;

import matrix.db.Context;
/**
 *  The <code>${CLASSNAME}</code> class is used
 *  for DocumentCentral File Objects
 * @exclude
 *
 */

public class emxFileObjBase_mxJPO extends DomainObject
{
  protected static final String THIS_FILE = "emxFileObjBase";

  public emxFileObjBase_mxJPO ()
    {
      super();
    }

  /**
   *  Constructs a new JPO object.
   *
   *  @param context the eMatrix <code>Context</code> object
   *  @param args holds object id
   *  @throws Exception if the operation fails
   *
   *  @since AEF 9.5.0.0
   */
  public emxFileObjBase_mxJPO (Context context, String[] args)
                        throws Exception
    {
      super ();

      setDocishObjectId (args[0]);
    }

  /**
   * Constructor
   *
   * @param id the Java <code>String</code> object
   *
   * @throws Exception if the operation fails
   *
   */

  public emxFileObjBase_mxJPO (String containerDocId) throws Exception
    {
      // Call the super constructor

      super (containerDocId);
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
        throw new Exception ("Don't call this!  Why do you think its here?");
      return 0;
    }
  
  /**
   * temporary method added while code cleanup 
   */
  protected void setDocishObjectId(String objectid) throws FrameworkException{
	  this.setId(objectid);
  }
 
}
