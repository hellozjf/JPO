/*
**   emxRouteDocument.java
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import com.matrixone.apps.common.util.ComponentsUtil;

import matrix.db.*;

public class emxRouteDocument_mxJPO extends emxRouteDocumentBase_mxJPO
{
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       * @since Common 10.5
       * @grade 0
       */
      public emxRouteDocument_mxJPO (Context context, String[] args)
          throws Exception
      {
          
		  super(context, args);

      }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns int
       * @throws Exception if the operation fails
       * @since Common 10.5
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.RouteDocument.MustSpecifyMethod", context.getSession().getLanguage()));
          }
          return 0;
      }
}
