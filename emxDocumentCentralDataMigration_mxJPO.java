/* emxDocumentCentralDataMigration.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.15 Wed Oct 22 16:02:09 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

public class emxDocumentCentralDataMigration_mxJPO extends emxDocumentCentralDataMigrationBase_mxJPO
{
  //~ Constructors -----------------------------------------------------------
  /**
   *  Constructs a new JPO object.
   *
   *  @param context the eMatrix <code>Context</code> object
   *  @param args holds no arguments
   *  @throws Exception if the operation fails
   *
   *  @since AEF 10.0.1.0
   */
  public emxDocumentCentralDataMigration_mxJPO ( Context context, String[] args )
      throws Exception
  {
      super (context, args);
  }

}
