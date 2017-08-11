/* emxDCUtils.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.19 Wed Oct 22 16:02:38 2008 przemek Experimental przemek $
*/

 import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> extends the Base JPO class used
 *  for DocumentCentral Utilty Programs
 *  @version AEF 10.0.1.0 - Copyright (c) 2002, MatrixOne, Inc.
 *
 */

public class emxDCUtils_mxJPO extends emxDCUtilsBase_mxJPO
{

    public  emxDCUtils_mxJPO  ()
    {
         //EMPTY CONSTRUCTOR
    }

  /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    *
    *  @since AEF 10.0.1.0
    */
    public emxDCUtils_mxJPO ( Context context, String[] args )
        throws Exception
    {
       super(context, args);
    }

}
