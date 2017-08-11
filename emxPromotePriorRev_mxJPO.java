/* emxPromotePriorRev.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.12 Wed Oct 22 16:02:32 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> class extends the Base JPO that is used
 *  to generate all DR managed business types
 *  list for display rule.
 *
 *  @version AEF 10.0.1.0 - Copyright (c) 2002, MatrixOne, Inc.
 *
 */

public class emxPromotePriorRev_mxJPO extends emxPromotePriorRevBase_mxJPO
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
   public emxPromotePriorRev_mxJPO ( Context context, String[] args )
       throws Exception
   {
       super(context, args);
   }

}
