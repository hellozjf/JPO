/*
 ** emxLibraryCentralSubscription.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of MatrixOne,
 **  Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 **  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **  Author   : 
 **  Version  : 
 **  Date     : 
 **
 **  staic const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.6 Wed Oct 22 16:02:17 2008 przemek Experimental przemek $";
 */

import matrix.db.Context;

public class emxLibraryCentralSubscription_mxJPO extends emxLibraryCentralSubscriptionBase_mxJPO
{

    public emxLibraryCentralSubscription_mxJPO(Context context, String []args)
        throws Exception
    {
        super(context, args);
    }

     /**
      * This method is executed if a specific method is not specified.
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args the Java <code>String[]</code> object
      * @return int
      * @throws Exception if the operation fails
      */
     public int mxMain (Context context, String[] args) throws Exception
     {
        if (true)
        {
            throw new Exception (
            "Must specify method on emxDocumentCentralSubscription invocation");
        }

        return 0;
    }
}
