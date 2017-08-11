/*
 *  emxWorkspaceSubscription.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import com.matrixone.apps.common.util.ComponentsUtil;

import   matrix.db.* ;

public class emxWorkspaceSubscription_mxJPO extends emxWorkspaceSubscriptionBase_mxJPO
{
  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since 10-7-0-0
   * @grade 0
   */
    public emxWorkspaceSubscription_mxJPO ()  throws Exception
    {
    }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns int
       * @throws Exception if the operation fails
       * @since 10-7-0-0
       */
      public int mxMain(Context context, String[] args)
          throws Exception
      {
          if (true)
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
          }
          return 0;
      }

}
