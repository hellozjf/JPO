/*
 ** ${CLASS:enoECMChangeutil}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

import matrix.db.Context;

/**
 * The <code>enoECMChangeUtil</code> class contains code for the "Part" business type.
 *
 * @version ECM R215  - # Copyright (c) 1992-2016 Dassault Systemes.
 */
  public class enoECMChangeUtil_mxJPO extends enoECMChangeUtilBase_mxJPO
  {
      /**
	 * 
	 */
	private static final long serialVersionUID = -8149651804367039959L;

	/**
       * Constructor.
       *
       * @param context the eMatrix <code>Context</code> object.
       * @param args holds no arguments.
       * @throws Exception if the operation fails.
       * @since ECM R215
       */

      public enoECMChangeUtil_mxJPO (Context context, String[] args) throws Exception {
          super(context, args);
      }
  }
