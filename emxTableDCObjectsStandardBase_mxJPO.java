/* emxTableDCObjectsStandardBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:26 2008 przemek Experimental przemek $
*/

import matrix.db.Context;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  to generate HTML code for the business type column on business type
 *  list page for display rule.
 *
 *  @exclude
 *
*/
public class emxTableDCObjectsStandardBase_mxJPO extends emxTableDocumentCentral_mxJPO
{

   /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public emxTableDCObjectsStandardBase_mxJPO ( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : MT
      *  Date      : 04/07/2003
      *  Notes     :
      *  History   :
      */

      super (context, args);

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }
   }

   /**
    *  This method is executed if a specific method is not specified.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @return int 0
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public int mxMain( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : MT
      *  Date      : 04/07/2003
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }

      return 0;
   }


}
