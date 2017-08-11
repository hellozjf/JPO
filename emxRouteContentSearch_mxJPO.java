/*
 *  emxRouteContentSearch.java  ( JPO class)
 *
 * Copyright (c) 2004-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import com.matrixone.apps.common.util.ComponentsUtil;

import   matrix.db.* ;

public class emxRouteContentSearch_mxJPO extends emxRouteContentSearchBase_mxJPO
{

  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since AEF 10.5 next
   * @grade 0
   */
    public emxRouteContentSearch_mxJPO ()  throws Exception
    {
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
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.RouteContentSearch.MustSpecifyMethodInJPO", context.getLocale().getLanguage()));
          }
          return 0;
      }

}
