/* emxTableDCObjectsStandard.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9 Wed Oct 22 16:02:15 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> class extends the Base JPO that is used
 *  to generate HTML code for the business type column on business type
 *  list page for display rule.
 *
 *  @version AEF 10.0.1.0  - Copyright (c) 2002, MatrixOne, Inc.
 *
*/
public class emxTableDCObjectsStandard_mxJPO extends emxTableDCObjectsStandardBase_mxJPO
{

   /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    *
    *  @since AEF 10.0.1.0
    */
   public emxTableDCObjectsStandard_mxJPO ( Context context, String[] args )
       throws Exception
   {
       super (context, args);
   }

}
