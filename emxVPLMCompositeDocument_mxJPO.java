/*
** Copyright (c) 2007 MatrixOne, Inc.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

import matrix.db.Context;

/**
 * Customization class for managing a Composite Document
 * 
 * @author srickus
 * @version RequirementsManagement V6R2008-2.0
 */
public class emxVPLMCompositeDocument_mxJPO extends emxVPLMCompositeDocumentBase_mxJPO
{
   /**
    * Extensions to the base emxVPLMCompositeDocument object.
    * 
    * @param context
    *                the eMatrix <code>Context</code> object
    * @param args
    *                holds no arguments
    * 
    * @return        an emxVPLMCompositeDocument object.
    * @throws Exception
    *                 if the operation fails
    */
   public emxVPLMCompositeDocument_mxJPO(Context context, String[] args)
        throws Exception
   {
      super(context, args);
   }

}
