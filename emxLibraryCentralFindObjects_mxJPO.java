/* emxLibraryCentralFindObjects.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.
* static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.7 Wed Oct 22 16:02:34 2008 przemek Experimental przemek $";
*/


import matrix.db.Context;

 /**
  *  The <code>${CLASSNAME}</code> class extends the Base class
  *  used for Searching DC objects
  *  Copyright (c) 2002, MatrixOne, Inc.
  *
  */
 public class emxLibraryCentralFindObjects_mxJPO extends emxLibraryCentralFindObjectsBase_mxJPO
 {

   /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    */
    public emxLibraryCentralFindObjects_mxJPO(Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }
 }
