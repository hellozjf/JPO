/*
 ** ${CLASS:enoECMChangeOrder}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

import matrix.db.Context;

/**
 * The <code>enoECMChangeOrder</code> class contains code for the "Change Order" business type.
 *
 * @version ECM R215  - # Copyright (c) 1992-2016 Dassault Systemes.
 */
  public class enoECMChangeOrder_mxJPO extends enoECMChangeOrderBase_mxJPO
  {
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object.
       * @param args holds no arguments.
       * @throws Exception if the operation fails.
       * @since ECM R215.
       */

      public enoECMChangeOrder_mxJPO (Context context, String[] args) throws Exception {
          super(context, args);
      }
  }
