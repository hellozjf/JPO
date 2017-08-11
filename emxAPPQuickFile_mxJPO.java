/*
**   emxAPPQuickFile.java
** Created on Jun 28, 2007
** Dassault Systemes, 1993  2007. All rights reserved.
** All Rights Reserved
** This program contains proprietary and trade secret information of
** Dassault Systemes.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

/**
 * The <code>emxAPPQuickFile</code> class contains methods for the "Defualt RMB Menu" Common Component.
 *
 * @version AEF 10.0.Patch SP3 - Copyright (c) 2003, MatrixOne, Inc.
 */

import com.matrixone.apps.common.util.ComponentsUtil;

import matrix.db.*;

public class emxAPPQuickFile_mxJPO extends emxAPPQuickFileBase_mxJPO
{
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @throws Exception if the operation fails
       * @since Common 10.0.0.0
       * @grade 0
       */
      public emxAPPQuickFile_mxJPO (Context context, String[] args)
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
       * @since Common 10.0.0.0
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
          }
          return 0;
      }

}
