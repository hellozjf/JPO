/*
 ** ${CLASS:MarketingFeature}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import matrix.db.Context;

/**
 * The <code>emxPartFamily</code> class contains code for the "Part Family" business type.
 *
 * @version EC 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
  public class emxPartFamily_mxJPO extends emxPartFamilyBase_mxJPO
  {
      /**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object.
       * @param args holds no arguments.
       * @throws Exception if the operation fails.
       * @since EC 10-6
       */

      public emxPartFamily_mxJPO (Context context, String[] args)
          throws Exception
      {
          super(context, args);
      }
  }
